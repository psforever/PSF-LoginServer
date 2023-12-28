// Copyright (c) 2019-2022 PSForever
package net.psforever.services.teamwork

import akka.actor.{Actor, ActorRef, Terminated}
import java.io.{PrintWriter, StringWriter}
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
//
import net.psforever.objects.{LivePlayerList, Player}
import net.psforever.objects.teamwork.{Member, Squad, SquadFeatures}
import net.psforever.objects.avatar.{Avatar, Certification}
import net.psforever.objects.definition.converter.StatConverter
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.SquadAction._
import net.psforever.packet.game.{PlanetSideZoneID, SquadDetail, SquadInfo, SquadPositionDetail, SquadPositionEntry, SquadAction => SquadRequestAction}
import net.psforever.services.Service
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, SquadRequestType, SquadResponseType}

class SquadService extends Actor {
  import SquadService._

  /**
    * The current unique squad identifier, to be wrapped in a `PlanetSideGUID` object later.
    * The count always starts at 1, even when reset.
    * A squad of `PlanetSideGUID(0)` indicates both a nonexistent squad and the service itself to clients.
    */
  private var sid: Int = 1

  /**
    * All squads.<br>
    * key - squad unique number; value - the squad wrapped around its attributes object
    */
  private val squadFeatures: TrieMap[PlanetSideGUID, SquadFeatures] = new TrieMap[PlanetSideGUID, SquadFeatures]()

  /**
    * The list of squads that each of the factions see for the purposes of keeping track of changes to the list.
    * These squads are considered public "listed" squads -
    * all the players of a certain faction can see those squads in the squad list
    * and may have limited interaction with their squad definition windows.<br>
    * key - squad unique number; value - the squad's unique identifier number
    */
  private val publishedLists: TrieMap[PlanetSideEmpire.Value, mutable.ListBuffer[PlanetSideGUID]] =
    TrieMap[PlanetSideEmpire.Value, mutable.ListBuffer[PlanetSideGUID]](
      PlanetSideEmpire.TR -> mutable.ListBuffer.empty,
      PlanetSideEmpire.NC -> mutable.ListBuffer.empty,
      PlanetSideEmpire.VS -> mutable.ListBuffer.empty
    )

  /**
    * key - a unique character identifier number; value - the squad to which this player is a member
    */
  private val memberToSquad: mutable.LongMap[PlanetSideGUID] = mutable.LongMap[PlanetSideGUID]()

  /**
    * Information relating to player searches to reconstruct the results.
    * The field of criteria involved includes details like the name and the certification requirements of the role.
    * key - a list of unique character identifier numbers; value - the information to compare squad member positions against
    */
  private val searchData: mutable.LongMap[SquadService.SearchCriteria] =
    mutable.LongMap[SquadService.SearchCriteria]()

  /**
    * A separate register to keep track of players to their client reference.
    */
  private implicit val subs: SquadSubscriptionEntity = new SquadSubscriptionEntity()

  private val invitations = new SquadInvitationManager(subs, self)

  private val log = org.log4s.getLogger

  private def info(msg: String): Unit = log.info(msg)

  private def debug(msg: String): Unit = log.debug(msg)

  override def postStop(): Unit = {
    //squads and members (users)
    squadFeatures.foreach {
      case (_, features) =>
        CloseSquad(features.Squad)
    }
    memberToSquad.clear()
    publishedLists.clear()
    //misc
    searchData.clear()
    subs.postStop()
    invitations.postStop()
  }

  /**
    * Produce the next available unique squad identifier.
    * The first number is always 1.
    * The greatest possible identifier is 65535 (an unsigned 16-bit integer)
    * before it wraps back around to 1.
    * @return the current squad unique identifier number
    */
  def GetNextSquadId(): PlanetSideGUID = {
    val out = sid
    val j   = sid + 2
    if (j == 65535) {
      sid = 1
    } else {
      sid = j
    }
    PlanetSideGUID(out)
  }

  /**
    * Set the unique squad identifier back to the start if no squads are active.
    * @return `true`, if the identifier is reset; `false`, otherwise
    */
  def TryResetSquadId(): Boolean = {
    if (squadFeatures.isEmpty) {
      sid = 1
      true
    } else {
      false
    }
  }

  /**
    * If a squad exists for an identifier, return that squad.
    * @param id the squad unique identifier number
    * @return the discovered squad, or `None`
    */
  def GetSquad(id: PlanetSideGUID): Option[SquadFeatures] = {
    squadFeatures.get(id)
  }

  /**
    * If this player is a member of any squad, discover that squad.
    * @param player the potential member
    * @return the discovered squad, or `None`
    */
  def GetParticipatingSquad(player: Player): Option[SquadFeatures] = GetParticipatingSquad(player.CharId)

  /**
    * If the player associated with this unique character identifier number is a member of any squad, discover that squad.
    * @param charId the potential member identifier
    * @return the discovered squad, or `None`
    */
  def GetParticipatingSquad(charId: Long): Option[SquadFeatures] =
    memberToSquad.get(charId) match {
      case Some(id) =>
        squadFeatures.get(id)
      case None =>
        None
    }

  /**
    * If this player is a member of any squad, discover that squad.
    * @see `GetParticipatingSquad`
    * @see `Squad::Leader`
    * @param player the potential member
    * @param opt an optional squad to check;
    *            the expectation is that the provided squad is a known participating squad
    * @return the discovered squad, or `None`
    */
  def GetLeadingSquad(player: Player, opt: Option[SquadFeatures]): Option[SquadFeatures] = GetLeadingSquad(player.CharId, opt)

  /**
    * If the player associated with this unique character identifier number is the leader of any squad, discover that squad.
    * @see `GetParticipatingSquad`
    * @see `Squad->Leader`
    * @param charId the potential member identifier
    * @param opt an optional squad to check;
    *            the expectation is that the provided squad is a known participating squad
    * @return the discovered squad, or `None`
    */
  def GetLeadingSquad(charId: Long, opt: Option[SquadFeatures]): Option[SquadFeatures] =
    opt.orElse(GetParticipatingSquad(charId)) match {
      case Some(features) =>
        if (features.Squad.Leader.CharId == charId) {
          Some(features)
        } else {
          None
        }
      case _ =>
        None
    }

  def receive: Receive = {
    //subscribe to a faction's channel - necessary to receive updates about listed squads
    case Service.Join(faction) if "TRNCVS".indexOf(faction) > -1 =>
      JoinByFaction(faction, sender())

    //subscribe to the player's personal channel - necessary for future and previous squad information
    case Service.Join(char_id) =>
      JoinByCharacterId(char_id, sender())

    case Service.Leave(Some(faction)) if "TRNCVS".indexOf(faction) > -1 =>
      LeaveByFaction(faction, sender())

    case Service.Leave(Some(char_id)) =>
      LeaveByCharacterId(char_id, sender())

    case Service.Leave(None) | Service.LeaveAll() =>
      LeaveInGeneral(sender())

    case Terminated(actorRef) =>
      TerminatedBy(actorRef)

    case message @ SquadServiceMessage(tplayer, zone, squad_action) =>
      squad_action match {
        case SquadAction.InitSquadList() =>
          SquadActionInitSquadList(tplayer, sender())

        case SquadAction.InitCharId() =>
          SquadActionInitCharId(tplayer)

        case SquadAction.ReloadDecoration() =>
          ApplySquadDecorationToEntriesForUser(tplayer.Faction, tplayer.CharId)

        case _: SquadAction.Membership =>
          SquadActionMembership(tplayer, zone, squad_action)

        case sqd: SquadAction.Definition =>
          SquadActionDefinition(message, sqd.action, sqd.guid)

        case _: SquadAction.Waypoint =>
          SquadActionWaypoint(message, tplayer)

        case _: SquadAction.Update => //try to avoid using; use the squad actor itself for updates
          SquadActionUpdate(message, tplayer.CharId, sender())

        case msg =>
          log.warn(s"Unhandled action $msg from ${sender()}")
      }

    case SquadService.PerformStartSquad(invitingPlayer) =>
      performStartSquad(sender(), invitingPlayer)

    case SquadService.PerformJoinSquad(player, features, position) =>
      JoinSquad(player, features, position)

    case SquadService.UpdateSquadList(features, changes) =>
      UpdateSquadList(features, changes)

    case SquadService.UpdateSquadListWhenListed(features, changes) =>
      UpdateSquadListWhenListed(features, changes)

    case SquadService.ResendActiveInvite(charId) =>
      invitations.resendActiveInvite(charId)

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }

  def JoinByFaction(faction: String, sender: ActorRef): Unit = {
    val path = s"/$faction/Squad"
    log.trace(s"$sender has joined $path")
    subs.SquadEvents.subscribe(sender, path)
  }

  def JoinByCharacterId(charId: String, sender: ActorRef): Unit = {
    try {
      val longCharId = charId.toLong
      val path       = s"/$charId/Squad"
      log.trace(s"$sender has joined $path")
      context.watch(sender)
      subs.UserEvents += longCharId -> sender
      invitations.handleJoin(longCharId)
    } catch {
      case _: ClassCastException =>
        log.warn(s"Service.Join: tried $charId as a unique character identifier, but it could not be casted")
      case e: Exception =>
        log.error(s"Service.Join: unexpected exception using $charId as data - ${e.getLocalizedMessage}")
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.error(sw.toString)
    }
  }

  def LeaveByFaction(faction: String, sender: ActorRef): Unit = {
    val path = s"/$faction/Squad"
    log.trace(s"$sender has left $path")
    subs.SquadEvents.unsubscribe(sender, path)
  }

  def LeaveByCharacterId(charId: String, sender: ActorRef): Unit = {
    try {
      LeaveService(charId.toLong, sender)
    } catch {
      case _: ClassCastException =>
        log.warn(s"Service.Leave: tried $charId as a unique character identifier, but it could not be casted")
      case e: Exception =>
        log.error(s"Service.Leave: unexpected exception using $charId as data - ${e.getLocalizedMessage}")
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.error(sw.toString)
    }
  }

  def LeaveInGeneral(sender: ActorRef): Unit = {
    subs.UserEvents find { case (_, subscription) => subscription.path.equals(sender.path) } match {
      case Some((to, _)) =>
        LeaveService(to, sender)
      case _ => ;
    }
  }

  def TerminatedBy(requestee: ActorRef): Unit = {
    context.unwatch(requestee)
    subs.UserEvents find { case (_, subscription) => subscription eq requestee } match {
      case Some((to, _)) =>
        LeaveService(to, requestee)
      case _ => ;
    }
  }

  def performStartSquad(sender: ActorRef, player: Player): Unit = {
    val invitingPlayerCharId = player.CharId
    if (EnsureEmptySquad(invitingPlayerCharId)) {
      GetParticipatingSquad(player) match {
        case Some(participating) =>
          //invitingPlayer became part of a squad while invited player was answering the original summons
          Some(participating)
        case _ =>
          //generate a new squad, with invitingPlayer as the leader
          val features = StartSquad(player)
          val squad = features.Squad
          squad.Task = s"${player.Name}'s Squad"
          subs.Publish(invitingPlayerCharId, SquadResponse.IdentifyAsSquadLeader(squad.GUID))
          sender.tell(SquadInvitationManager.FinishStartSquad(features), self)
          Some(features)
      }
    }
  }

  def SquadActionInitSquadList(
                                tplayer: Player,
                                sender: ActorRef
                              ): Unit = {
    //send initial squad catalog
    val faction = tplayer.Faction
    val squads = PublishedLists(faction)
    subs.Publish(sender, SquadResponse.InitList(squads))
    squads.foreach { squad =>
      val guid = squad.squad_guid.get
      subs.Publish(tplayer.CharId, SquadResponse.SquadDecoration(guid, squadFeatures(guid).Squad))
    }
  }

  def SquadActionInitCharId(tplayer: Player): Unit = {
    val charId = tplayer.CharId
    GetParticipatingSquad(charId) match {
      case None => ;
      case Some(features) =>
        features.Switchboard ! SquadSwitchboard.Join(tplayer, 0, sender())
    }
  }

  def SquadServiceReloadSquadDecoration(faction: PlanetSideEmpire.Value, to: Long): Unit = {
    ApplySquadDecorationToEntriesForUser(faction, to)
  }

  def SquadActionMembership(tplayer: Player, zone: Zone, action: Any): Unit = {
    action match {
      case SquadAction.Membership(SquadRequestType.Invite, invitingPlayer, Some(_invitedPlayer), invitedName, _) =>
        SquadActionMembershipInvite(tplayer, invitingPlayer, _invitedPlayer, invitedName)

      case SquadAction.Membership(SquadRequestType.ProximityInvite, invitingPlayer, _, _, _) =>
        SquadActionMembershipProximityInvite(zone, invitingPlayer)

      case SquadAction.Membership(SquadRequestType.Accept, invitedPlayer, _, _, _) =>
        SquadActionMembershipAccept(tplayer, invitedPlayer)

      case SquadAction.Membership(SquadRequestType.Leave, actingPlayer, _leavingPlayer, name, _) =>
        SquadActionMembershipLeave(tplayer, actingPlayer, _leavingPlayer, name)

      case SquadAction.Membership(SquadRequestType.Reject, rejectingPlayer, _, _, _) =>
        SquadActionMembershipReject(tplayer, rejectingPlayer)

      case SquadAction.Membership(SquadRequestType.Disband, char_id, _, _, _) =>
        SquadActionMembershipDisband(char_id)

      case SquadAction.Membership(SquadRequestType.Cancel, cancellingPlayer, _, _, _) =>
        SquadActionMembershipCancel(cancellingPlayer)

      case SquadAction.Membership(SquadRequestType.Promote, promotingPlayer, Some(_promotedPlayer), promotedName, _) =>
        SquadActionMembershipPromote(promotingPlayer, _promotedPlayer, promotedName, SquadServiceMessage(tplayer, zone, action), sender())

      case SquadAction.Membership(event, _, _, _, _) =>
        debug(s"SquadAction.Membership: $event is not yet supported")

      case _ => ;
    }
  }

  def SquadActionMembershipInvite(
                                   tplayer: Player,
                                   invitingPlayer: Long,
                                   _invitedPlayer: Long,
                                   invitedName: String
                                 ): Unit = {
    //this is just busy work; for actual joining operations, see SquadRequestType.Accept
    (if (invitedName.nonEmpty) {
      //validate player with name exists
      LivePlayerList
        .WorldPopulation({ case (_, a: Avatar) => a.name.equalsIgnoreCase(invitedName) && a.faction == tplayer.Faction })
        .headOption match {
        case Some(a) => subs.UserEvents.keys.find(_ == a.id)
        case None    => None
      }
    } else {
      //validate player with id exists
      LivePlayerList
        .WorldPopulation({ case (_, a: Avatar) => a.id == _invitedPlayer && a.faction == tplayer.Faction })
        .headOption match {
        case Some(_) => Some(_invitedPlayer)
        case None         => None
      }
    }) match {
      case Some(invitedPlayer) if invitingPlayer != invitedPlayer =>
        (GetParticipatingSquad(invitingPlayer), GetParticipatingSquad(invitedPlayer)) match {
          case (Some(features1), Some(features2))
            if features1.Squad.GUID == features2.Squad.GUID =>
          //both players are in the same squad; no need to do anything

          case (Some(invitersFeatures), Some(invitedFeatures)) if {
            val squad1 = invitersFeatures.Squad
            val squad2 = invitedFeatures.Squad
            squad1.Leader.CharId == invitingPlayer && squad2.Leader.CharId == invitedPlayer &&
              squad1.Size > 1 && squad2.Size > 1 } =>
          //we might do some platoon chicanery with this case later
          //TODO platoons

          case (Some(invitersFeatures), Some(invitedFeatures))
            if invitedFeatures.Squad.Size == 1 =>
            //both players belong to squads, but the invitedPlayer's squad (invitedFeatures) is underutilized
            //treat the same as "the classic situation" using invitersFeatures
            invitations.createVacancyInvite(tplayer, invitedPlayer, invitersFeatures)

          case (Some(invitersFeatures), Some(invitedFeatures))
            if invitersFeatures.Squad.Size == 1 =>
            //both players belong to squads, but the invitingPlayer's squad is underutilized by comparison
            //treat the same as "indirection ..." using squad2
            invitations.createIndirectInvite(tplayer, invitedPlayer, invitedFeatures)

          case (Some(features), None) =>
            //the classic situation
            invitations.createVacancyInvite(tplayer, invitedPlayer, features)

          case (None, Some(features)) =>
            //indirection;  we're trying to invite ourselves to someone else's squad
            invitations.createIndirectInvite(tplayer, invitedPlayer, features)

          case (None, None) =>
            //neither the invited player nor the inviting player belong to any squad
            invitations.createSpontaneousInvite(tplayer, invitedPlayer)

          case _ => ;
        }
      case _ => ;
    }
  }

  def SquadActionMembershipProximityInvite(zone: Zone, invitingPlayer: Long): Unit = {
    GetLeadingSquad(invitingPlayer, None) match {
      case Some(features) =>
        invitations.handleProximityInvite(zone, invitingPlayer, features)
      case _ => ;
    }
  }

  def SquadActionMembershipAccept(tplayer: Player, invitedPlayer: Long): Unit = {
    invitations.handleAcceptance(tplayer, invitedPlayer, GetParticipatingSquad(tplayer))
  }

  def SquadActionMembershipLeave(tplayer: Player, actingPlayer: Long, _leavingPlayer: Option[Long], name: String): Unit = {
    GetParticipatingSquad(actingPlayer) match {
      case Some(features) =>
        val squad = features.Squad
        val leader = squad.Leader.CharId
        (if (name.nonEmpty) {
          //validate player with name
          LivePlayerList
            .WorldPopulation({ case (_, a: Avatar) => a.name.equalsIgnoreCase(name) })
            .headOption match {
            case Some(a) => subs.UserEvents.keys.find(_ == a.id)
            case None    => None
          }
        } else {
          //validate player with id
          _leavingPlayer match {
            case Some(id) => subs.UserEvents.keys.find(_ == id)
            case None     => None
          }
        }) match {
          case _ @ Some(leavingPlayer)
            if GetParticipatingSquad(leavingPlayer).contains(features) => //kicked player must be in the same squad
            if (actingPlayer == leader) {
              if (leavingPlayer == leader || squad.Size == 2) {
                //squad leader is leaving his own squad, so it will be disbanded
                //OR squad is only composed of two people, so it will be closed-out when one of them leaves
                DisbandSquad(features)
              } else {
                //kicked by the squad leader
                subs.Publish(
                  leavingPlayer,
                  SquadResponse.Membership(
                    SquadResponseType.Leave,
                    0,
                    0,
                    leavingPlayer,
                    Some(leader),
                    tplayer.Name,
                    unk5=false,
                    Some(None)
                  )
                )
                subs.Publish(
                  leader,
                  SquadResponse.Membership(
                    SquadResponseType.Leave,
                    0,
                    0,
                    leader,
                    Some(leavingPlayer),
                    "",
                    unk5=true,
                    Some(None)
                  )
                )
                LeaveSquad(leavingPlayer, features)
              }
            } else if (leavingPlayer == actingPlayer) {
              if (squad.Size == 2) {
                //squad is only composed of two people, so it will be closed-out when one of them leaves
                DisbandSquad(features)
              } else {
                //leaving the squad of own accord
                LeaveSquad(actingPlayer, features)
              }
            }

          case _ => ;
        }
      case _ => ;
    }
  }

  def SquadActionMembershipReject(tplayer: Player, rejectingPlayer: Long): Unit = {
    invitations.handleRejection(
      tplayer,
      rejectingPlayer,
      squadFeatures.map { case (guid, features) => (guid, features.Squad.Leader.CharId) }.toList
    )
  }

  def SquadActionMembershipDisband(charId: Long): Unit = {
    GetLeadingSquad(charId, None) match {
      case Some(features) =>
        DisbandSquad(features)
      case None => ;
    }
  }

  def SquadActionMembershipCancel(cancellingPlayer: Long): Unit = {
    //get rid of SpontaneousInvite objects and VacancyInvite objects
    invitations.handleCancelling(cancellingPlayer)
  }

  def SquadActionMembershipPromote(
                                    sponsoringPlayer: Long,
                                    promotionCandidatePlayer: Long,
                                    promotionCandidateName: String,
                                    msg: SquadServiceMessage,
                                    ref: ActorRef
                                  ): Unit = {
    val promotedPlayer: Long = subs.UserEvents.keys.find(_ == promotionCandidatePlayer).orElse({
      LivePlayerList
        .WorldPopulation({ case (_, a: Avatar) => a.name.equalsIgnoreCase(promotionCandidateName) })
        .headOption match {
        case Some(a) => Some(a.id)
        case None    => None
      }
    }) match {
      case Some(player: Long) => player
      case _                  => -1L
    }
    //sponsorPlayer should be squad leader
    (GetLeadingSquad(sponsoringPlayer, None), GetParticipatingSquad(promotedPlayer)) match {
      case (Some(features), Some(features2)) if features.Squad.GUID == features2.Squad.GUID =>
        SquadActionMembershipPromote(sponsoringPlayer, promotedPlayer, features, msg, ref)
      case _ => ;
    }
  }

  def SquadActionMembershipPromote(
                                    sponsoringPlayer: Long,
                                    promotedPlayer: Long,
                                    features: SquadFeatures,
                                    msg: SquadServiceMessage,
                                    ref: ActorRef
                                  ): Unit = {
    features.Switchboard.tell(msg, ref)
    invitations.handlePromotion(sponsoringPlayer, promotedPlayer)
  }

  def SquadActionWaypoint(
                           message: SquadServiceMessage,
                           tplayer: Player
                         ): Unit = {
    GetParticipatingSquad(tplayer) match {
      case Some(features) =>
        features.Switchboard.tell(message, sender())
      case None =>
        log.warn(s"Unsupported squad waypoint behavior: $message")
    }
  }

  def SquadActionDefinition(
                             message: SquadServiceMessage,
                             action: SquadRequestAction,
                             guid: PlanetSideGUID
                           ): Unit = {
    val tplayer = message.tplayer
    (action match {
      //the following actions only perform an action upon the squad
      case _: ChangeSquadPurpose =>                          GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: ChangeSquadZone =>                             GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: AddSquadMemberPosition =>                      GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: ChangeSquadMemberRequirementsRole =>           GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: ChangeSquadMemberRequirementsDetailedOrders => GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: ChangeSquadMemberRequirementsCertifications => GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: LocationFollowsSquadLead =>                    GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: RequestListSquad =>                            GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: StopListSquad =>                               GetLeadingSquad(tplayer, None)
      //the following actions cause changes with the squad composition or with invitations
      case AutoApproveInvitationRequests(_) =>
        GetOrCreateSquadOnlyIfLeader(tplayer) match {
          case out @ Some(features) =>
            invitations.handleDefinitionAction(tplayer, action, features)
            out
          case None =>
            None
        }
      case CloseSquadMemberPosition(position) =>
        GetOrCreateSquadOnlyIfLeader(tplayer) match {
          case out @ Some(features)
            if features.Squad.Membership(position).CharId > 0 =>
            val squad = features.Squad
            LeaveSquad(squad.Membership(position).CharId, features)
            out
          case _ =>
            None
        }
      case FindLfsSoldiersForRole(_) =>
        GetLeadingSquad(tplayer, None) match {
          case Some(features) =>
            invitations.handleDefinitionAction(tplayer, action, features)
          case _ => ;
        }
        None
      case CancelFind() =>
        GetLeadingSquad(tplayer, None) match {
          case Some(features) =>
            invitations.handleDefinitionAction(tplayer, action, features)
          case _ => ;
        }
        None
      case SelectRoleForYourself(_) =>
        GetParticipatingSquad(tplayer) match {
          case out @ Some(features) =>
            if (features.Squad.GUID == guid) {
              out
            } else {
              //this isn't the squad we're looking for by GUID; as a precaution, reload all of the published squad list
              val faction = tplayer.Faction
              subs.Publish(faction, SquadResponse.InitList(PublishedLists(tplayer.Faction)))
              None
            }
          case _ =>
            GetSquad(guid) match {
              case Some(features) =>
                invitations.handleDefinitionAction(tplayer, action, features)
              case _ => ;
            }
            None
        }
      case _: CancelSelectRoleForYourself =>
        GetSquad(guid) match {
          case Some(features) =>
            invitations.handleDefinitionAction(tplayer, action, features)
          case _ => ;
        }
        None
      case search: SearchForSquadsWithParticularRole =>
        SquadActionDefinitionSearchForSquadsWithParticularRole(tplayer, search)
        None
      case _: CancelSquadSearch =>
        SquadActionDefinitionCancelSquadSearch(tplayer.CharId)
        None
      case _: DisplaySquad =>
        GetSquad(guid) match {
          case out @ Some(_) =>
            SquadActionDefinitionDisplaySquad(tplayer, guid)
            out
          case None =>
            None
        }
      case _: SquadInitializationIssue =>
        SquadActionDefinitionSquadInitializationIssue(tplayer, guid)
        None
      case _ =>
        GetSquad(guid)
    }) match {
      case Some(features) => features.Switchboard.tell(message, sender())
      case None => ;
    }
  }

  def SquadActionUpdate(
                         message: SquadServiceMessage,
                         char_id: Long,
                         replyTo: ActorRef,
                       ): Unit = {
    GetParticipatingSquad(char_id) match {
      case Some(features) => features.Switchboard.tell(message, replyTo)
      case None => ;
    }
  }

  def GetOrCreateSquadOnlyIfLeader(player: Player): Option[SquadFeatures] = {
    val participatingSquadOpt = GetParticipatingSquad(player)
    val leadingSquadOpt = GetLeadingSquad(player, participatingSquadOpt)
    if (participatingSquadOpt.isEmpty) {
      Some(StartSquad(player))
    } else if (participatingSquadOpt == leadingSquadOpt) {
      leadingSquadOpt
    } else {
      None
    }
  }

  def SquadActionDefinitionSearchForSquadsWithParticularRole(
                                                              tplayer: Player,
                                                              criteria: SearchForSquadsWithParticularRole
                                                            ): Unit = {
    val charId = tplayer.CharId
    searchData.get(charId) match {
      case Some(_) => ;
      //already searching, so do nothing(?)
      case None =>
        val data = SquadService.SearchCriteria(tplayer.Faction, criteria)
        searchData.put(charId, data)
        SquadActionDefinitionSearchForSquadsUsingCriteria(charId, data)
    }
  }

  private def SquadActionDefinitionSearchForSquadsUsingCriteria(
                                                                 charId: Long,
                                                                 criteria: SquadService.SearchCriteria
                                                               ): Unit = {
    subs.Publish(
      charId,
      SquadResponse.SquadSearchResults(SearchForSquadsResults(criteria))
    )
  }

  private def SearchForSquadsResults(criteria: SquadService.SearchCriteria): List[PlanetSideGUID] = {
    publishedLists.get(criteria.faction) match {
      case Some(squads) if squads.nonEmpty =>
        squads.flatMap { guid => SearchForSquadsResults(criteria, guid) }.toList
      case _ =>
        Nil
    }
  }

  def SquadActionDefinitionCancelSquadSearch(charId: Long): Unit = {
    searchData.remove(charId) match {
      case None => ;
      case Some(data) =>
        SearchForSquadsResults(data).foreach { guid =>
          subs.Publish(charId, SquadResponse.SquadDecoration(guid, squadFeatures(guid).Squad))
        }
    }
  }

  private def SearchForSquadsResults(
                                      criteria: SearchCriteria,
                                      guid: PlanetSideGUID
                                    ): Option[PlanetSideGUID] = {
    val squad = squadFeatures(guid).Squad
    val positions = if (criteria.mode == SquadRequestAction.SearchMode.AnyPositions) {
      //includes occupied positions and closed positions that retain assignment information
      squad.Membership
    } else {
      squad.Membership.zipWithIndex.filter { case (_, b) => squad.Availability(b) }.map { _._1 }
    }
    if (
      positions.nonEmpty &&
        (criteria.zoneId == 0 || criteria.zoneId == squad.ZoneId) &&
        (criteria.role.isEmpty || positions.exists(_.Role.equalsIgnoreCase(criteria.role))) &&
        (criteria.requirements.isEmpty || positions.exists { p =>
          val results = p.Requirements.intersect(criteria.requirements)
          if (criteria.mode == SquadRequestAction.SearchMode.SomeCertifications) {
            results.size > 1
          } else {
            results == criteria.requirements
          }
        })
    ) {
      Some(guid)
    } else {
      None
    }
  }

  /** the following action can be performed by anyone */
  def SquadActionDefinitionDisplaySquad(tplayer: Player, guid: PlanetSideGUID): Unit = {
    subs.MonitorSquadDetails += tplayer.CharId -> SquadSubscriptionEntity.MonitorEntry(guid)
  }

  def SquadActionDefinitionSquadInitializationIssue(tplayer: Player, guid: PlanetSideGUID): Unit = {
    //the following message is feedback from a specific client, awaiting proper initialization
    //this tends to happen when the client receives listing details about a squad it has never known before
    val reason = GetSquad(guid) match {
      case Some(features) =>
        val squad = features.Squad
        if (squad.Faction != tplayer.Faction) {
          s"about an enemy ${squad.Faction} squad"
        } else if(!features.Listed) {
          s"about a squad that may not yet be listed - ${squad.Task}"
        } else {
          "for an unknown reason"
        }
      case None =>
        s"about a squad that does not exist - ${guid.guid}"
    }
    log.warn(s"${tplayer.Name} has a potential squad issue; might be exchanging information $reason")
  }

  def CleanUpSquadFeatures(removed: List[Long], guid: PlanetSideGUID, position: Int): Unit = {
    GetSquad(guid) match {
      case Some(features) =>
        features.ProxyInvites = features.ProxyInvites.filterNot(removed.contains)
        if (features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
        }
      case None => ;
    }
  }

  /**
    * Establish a new squad.
    * Create all of the support structures for the squad and link into them.
    * At a minimum, by default, the squad needs a squad leader
    * and a stronger, more exposed connection between the squad and leader needs to be recognized.<br>
    * <br>
    * Usually, a squad is created by modifying some aspect of its necessary fields.
    * The primary necessary fields required for a squad include the squad's task and the squad's zone of operation.
    * @see `GetNextSquadId`
    * @see `Squad`
    * @see `SquadFeatures`
    * @see `SquadFeatures::Start`
    * @param player the player who would become the squad leader
    * @return the squad that has been created
    */
  def StartSquad(player: Player): SquadFeatures = {
    val faction      = player.Faction
    val name         = player.Name
    val sguid        = GetNextSquadId()
    val squad        = new Squad(sguid, faction)
    val leadPosition = squad.Membership(0)
    leadPosition.Name = name
    leadPosition.CharId = player.CharId
    leadPosition.Health = StatConverter.Health(player.Health, player.MaxHealth, min = 1, max = 64)
    leadPosition.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min = 1, max = 64)
    leadPosition.Position = player.Position
    leadPosition.ZoneId = 1
    leadPosition.Certifications = player.avatar.certifications
    val features = new SquadFeatures(squad).Start
    squadFeatures += sguid               -> features
    memberToSquad += squad.Leader.CharId -> sguid
    info(s"$name-$faction has created a new squad (#${sguid.guid})")
    features
  }

  /**
    * Behaviors and exchanges necessary to complete the fulfilled recruitment process for the squad role.<br>
    * <br>
    * This operation is fairly safe to call whenever a player is to be inducted into a squad.
    * The aforementioned player must have a callback retained in `subs.UserEvents`
    * and conditions imposed by both the role and the player must be satisfied.
    * @see `CleanUpAllInvitesWithPlayer`
    * @see `Squad.isAvailable`
    * @see `Squad.Switchboard`
    * @see `SquadSubscriptionEntity.MonitorSquadDetails`
    * @see `SquadSubscriptionEntity.Publish`
    * @see `SquadSubscriptionEntity.Join`
    * @see `SquadSubscriptionEntity.UserEvents`
    * @param player the new squad member;
    *               this player is NOT the squad leader
    * @param features the squad the player is joining
    * @param position the squad member role that the player will be filling
    * @return `true`, if the player joined the squad in some capacity;
    *         `false`, if the player did not join the squad or is already a squad member
    */
  def JoinSquad(player: Player, features: SquadFeatures, position: Int): Boolean = {
    val charId = player.CharId
    val squad = features.Squad
    subs.UserEvents.get(charId) match {
      case Some(events)
        if squad.isAvailable(position, player.avatar.certifications) &&
          EnsureEmptySquad(charId) =>
        memberToSquad(charId) = squad.GUID
        subs.MonitorSquadDetails.subtractOne(charId)
        invitations.handleCleanup(charId)
        features.Switchboard ! SquadSwitchboard.Join(player, position, events)
        true
      case _ =>
        false
    }
  }

  /**
    * Determine whether a player is sufficiently unemployed
    * and has no grand delusions of being a squad leader.
    * @see `CloseSquad`
    * @param charId the player
    * @return `true`, if the target player possesses no squad or a squad that is suitably nonexistent;
    *        `false`, otherwise
    */
  def EnsureEmptySquad(charId: Long): Boolean = {
    GetParticipatingSquad(charId) match {
      case None =>
        true
      case Some(features) if features.Squad.Size == 1 =>
        CloseSquad(features.Squad)
        true
      case _ =>
        log.warn("EnsureEmptySquad: the invited player is already a member of a squad and can not join a second one")
        false
    }
  }

  /**
    * Behaviors and exchanges necessary to undo the recruitment process for the squad role.
    * @see `PanicLeaveSquad`
    * @see `SquadSubscriptionEntity.Publish`
    * @param charId the player
    * @param features the squad
    * @return `true`, if the player, formerly a normal member of the squad, has been ejected from the squad;
    *        `false`, otherwise
    */
  def LeaveSquad(charId: Long, features: SquadFeatures): Boolean = {
    val squad = features.Squad
    val membership = squad.Membership.zipWithIndex
    membership.find { case (_member, _) => _member.CharId == charId } match {
      case Some(_) if squad.Leader.CharId != charId =>
        memberToSquad.remove(charId)
        features.Switchboard ! SquadSwitchboard.Leave(charId)
        true
      case _ =>
        false
    }
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.
    * Any member of the squad missing an `ActorRef` object used to message the player's client
    * will still leave the squad, but will not attempt to send feedback to the said unreachable client.
    * If the player is in the process of unsubscribing from the service,
    * the no-messaging pathway is useful to avoid accumulating dead letters.
    * @see `CleanUpAllInvitesToSquad`
    * @see `SquadDetail`
    * @see `SquadSubscriptionEntity.Publish`
    * @see `TryResetSquadId`
    * @see `UpdateSquadList`
    * @param squad the squad
    */
  def CloseSquad(squad: Squad): Unit = {
    val guid = squad.GUID
    val membership = squad.Membership.zipWithIndex
    val (updateMembers, updateIndices) = membership.collect {
      case (member, index) if member.CharId > 0 =>
        ((member, member.CharId, index, subs.UserEvents.get(member.CharId)), (member.CharId, index))
    }.unzip
    val updateIndicesList          = updateIndices.toList
    val completelyBlankSquadDetail = SquadDetail().Complete
    val features                   = squadFeatures(guid)
    val channel                    = s"/${features.ToChannel}/Squad"
    if (features.Listed) {
      subs.Publish(squad.Leader.CharId, SquadResponse.SetListSquad(PlanetSideGUID(0)))
    }
    invitations.handleClosingSquad(features)
    updateMembers
      .foreach {
        case (member, charId, _, None) =>
          memberToSquad.remove(charId)
          member.Name = ""
          member.CharId = 0L
        case (member, charId, index, Some(actor)) =>
          memberToSquad.remove(charId)
          member.Name = ""
          member.CharId = 0L
          subs.SquadEvents.unsubscribe(actor, channel)
          subs.Publish(
            charId,
            SquadResponse.Leave(
              squad,
              updateIndicesList.filterNot {
                case (_, outIndex) => outIndex == index
              } :+ (charId, index) //we need to be last
            )
          )
          subs.Publish(charId, SquadResponse.IdentifyAsSquadLeader(PlanetSideGUID(0)))
          subs.Publish(charId, SquadResponse.Detail(PlanetSideGUID(0), completelyBlankSquadDetail))
      }
    UpdateSquadListWhenListed(features.Stop, None)
    //I think this is right, otherwise squadFeatures will never be empty and TryResetSquadId will not reset to 1
    squadFeatures.remove(guid)
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.
    * Essentially, perform the same operations as `CloseSquad`
    * but treat the process as if the squad is being disbanded in terms of messaging.
    * @see `PanicDisbandSquad`
    * @see `SquadResponse.Membership`
    * @see `SquadSubscriptionEntity.Publish`
    * @param features the squad
    */
  def DisbandSquad(features: SquadFeatures): Unit = {
    val squad = features.Squad
    val leader = squad.Leader.CharId
    PanicDisbandSquad(
      features,
      squad.Membership.collect { case member if member.CharId > 0 && member.CharId != leader => member.CharId }
    )
    //the squad is being disbanded, the squad events channel is also going away; use cached character ids
    info(s"Squad #${squad.GUID.guid} has been disbanded.")
    subs.Publish(leader, SquadResponse.Membership(SquadResponseType.Disband, 0, 0, leader, None, "", unk5=true, Some(None)))
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.<br>
    * <br>
    * The complement of the prior `DisbandSquad` method.
    * This method deals entirely with other squad members observing the squad become abandoned.
    * The distinction is useful when unsubscribing from this service,
    * as the `ActorRef` object used to message the player's client is no longer reliable
    * and has probably ceased to exist.
    * @see `CloseSquad`
    * @see `DisbandSquad`
    * @see `SquadResponse.Membership`
    * @see `SquadResponseType`
    * @see `SquadSubscriptionEntity.Publish`
    * @param features the squad
    * @param membership the unique character identifier numbers of the other squad members
    * @return if a role/index pair is provided
    */
  def PanicDisbandSquad(features: SquadFeatures, membership: Iterable[Long]): Unit = {
    val squad = features.Squad
    val leader = squad.Leader.CharId
    CloseSquad(squad)
    //alert former members and anyone watching this squad for updates
    (membership.filterNot(_ == leader) ++ subs.PublishToMonitorTargets(squad.GUID, Nil))
      .toSet
      .foreach { charId : Long =>
        subs.Publish(charId, SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", unk5=false, Some(None)))
      }
  }

  /**
    * na
    * @param charId the player's unique character identifier number
    * @param sender the `ActorRef` associated with this character
    */
  def LeaveService(charId: String, sender: ActorRef): Unit = {
    LeaveService(charId.toLong, sender)
  }

  /**
    * na
    * @param charId the player's unique character identifier number
    * @param sender the `ActorRef` associated with this character
    */
  def LeaveService(charId: Long, sender: ActorRef): Unit = {
    subs.MonitorSquadDetails.subtractOne(charId)
    invitations.handleLeave(charId)
    val pSquadOpt = GetParticipatingSquad(charId)
    pSquadOpt match {
      //member of the squad; leave the squad
      case Some(features) =>
        LeaveSquad(charId, features)
        val squad = features.Squad
        val size = squad.Size
        subs.UserEvents.remove(charId) match {
          case Some(events) =>
            subs.SquadEvents.unsubscribe(events, s"/${features.ToChannel}/Squad")
          case _ => ;
        }
        if (size > 2) {
          GetLeadingSquad(charId, pSquadOpt) match {
            case Some(_) =>
              //leader of a squad; the squad will be disbanded. Same logic as when a SL uses /leave and the squad is disbanded.
              PanicDisbandSquad(
                features,
                squad.Membership.collect { case member if member.CharId > 0 && member.CharId != charId => member.CharId }
              )
            case None =>
              //not the leader of a full squad; tell other members that we are leaving
              SquadSwitchboard.PanicLeaveSquad(
                charId,
                features,
                squad.Membership.zipWithIndex.find { case (_member, _) => _member.CharId == charId },
                subs,
                self,
                log
              )
          }
        } else {
          //with only two members before our leave, the squad will be disbanded
          PanicDisbandSquad(
            features,
            squad.Membership.collect { case member if member.CharId > 0 && member.CharId != charId => member.CharId }
          )
        }
      case None =>
        //not a member of any squad; nothing really to do here
        subs.UserEvents.remove(charId)
    }
    subs.SquadEvents.unsubscribe(sender) //just to make certain
    searchData.remove(charId)
    TryResetSquadId()
  }

  /**
    * Dispatch a message entailing the composition of this squad when that squad is publicly available
    * and focus on any specific aspects of it, purported as being changed recently.
    * @see `SquadInfo`
    * @see `UpdateSquadList(Squad, Option[SquadInfo])`
    * @param features the related information about the squad
    * @param changes the highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  private def UpdateSquadListWhenListed(features: SquadFeatures, changes: SquadInfo): Unit = {
    UpdateSquadListWhenListed(features, Some(changes))
  }

  /**
    * Dispatch a message entailing the composition of this squad when that squad is publicly available
    * and focus on any specific aspects of it, purported as being changed recently.
    * The only requirement is that the squad is publicly available for recruitment ("listed").
    * @see `SquadInfo`
    * @see `UpdateSquadList(Squad, Option[SquadInfo])`
    * @param features the related information about the squad
    * @param changes the optional highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  private def UpdateSquadListWhenListed(features: SquadFeatures, changes: Option[SquadInfo]): Unit = {
    if (features.Listed) {
      UpdateSquadList(features, changes)
    }
  }

  /**
    * Dispatch a message entailing the composition of this squad
    * and focus on any specific aspects of it, purported as being changed recently.<br>
    * <br>
    * What sort of message is dispatched is not only based on the input parameters
    * but also on the state of previously listed squad information.
    * Listed squad information is queued when it is first published, organized first by faction affinity, then by chronology.
    * The output is first determinate on whether the squad had previously been listed as available.
    * If so, it will either update its data to all valid faction associated entities with the provided changed data;
    * or, it will be removed from the list of available squads, if there is no provided change data.
    * If the squad can not be found,
    * the change data, whatever it is, is unimportant, and the squad will be listed in full for the first time.<br>
    * <br>
    * When a squad is first introduced to the aforementioned list,
    * thus first being published to all faction-associated parties,
    * the entirety of the squad list for that faction will be updated in one go.
    * It is not necessary to do this, but doing so saves index and unique squad identifier management
    * at the cost of the size of the packet to be dispatched.
    * When a squad is removed to the aforementioned list,
    * the same process occurs where the full list for that faction affiliation is sent as an update.
    * The procedure for updating individual squad fields is precise and targeted,
    * and has been or should be prepared in advance by the caller to this method.
    * As a consequence, when updating the entry for that squad,
    * the information used as the update does not necessarily reflect the actual information currently in the squad.
    * @see `SquadResponse.InitList`
    * @see `SquadResponse.UpdateList`
    * @see `SquadService.SquadList.Publish`
    * @param features the squad
    * @param changes the optional highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  def UpdateSquadList(features: SquadFeatures, changes: Option[SquadInfo]): Unit = {
    val squad           = features.Squad
    val guid            = squad.GUID
    val faction         = squad.Faction
    val factionListings = publishedLists(faction)
    factionListings.find(_ == guid) match {
      case Some(listedSquad) =>
        val index = factionListings.indexOf(listedSquad)
        changes match {
          case Some(changedFields) =>
            //squad information update
            subs.Publish(faction, SquadResponse.UpdateList(Seq((index, changedFields))))
            ApplySquadDecorationToEntry(faction, guid, squad)
          case None =>
            //remove squad from listing
            factionListings.remove(index)
            subs.Publish(faction, SquadResponse.InitList(PublishedLists(factionListings.flatMap { GetSquad })))
        }
      case None =>
        //first time being published
        factionListings += guid
        subs.Publish(faction, SquadResponse.InitList(PublishedLists(factionListings.flatMap { GetSquad })))
        ApplySquadDecorationToEntry(faction, guid, squad)
    }
  }

  /**
    * Transform a list of squad unique identifiers into a list of `SquadInfo` objects for updating the squad list window.
    * @param faction the faction to which the squads belong
    * @return a `Vector` of transformed squad data
    */
  def PublishedLists(faction: PlanetSideEmpire.Type): Vector[SquadInfo] = {
    PublishedLists(publishedLists(faction).flatMap { GetSquad })
  }

  /**
    * Transform a list of squad unique identifiers into a list of `SquadInfo` objects for updating the squad list window.
    * @param squads the list of squads
    * @return a `Vector` of transformed squad data
    */
  def PublishedLists(squads: Iterable[SquadFeatures]): Vector[SquadInfo] = {
    squads.map { features => SquadService.PublishFullListing(features.Squad) }.toVector
  }

  /**
    * Squad decoration are the colors applied to entries in the squad listing based on individual assessments.
    * Apply these colors to one squad at a time.
    * This sends out the least amount of messages -
    * one for the whole faction and one message for each search for which this squad is a positive result.
    * @param faction empire whose squad is being decorated
    * @param guid the squad's identifier
    * @param squad the squad
    */
  def ApplySquadDecorationToEntry(
                                   faction: PlanetSideEmpire.Value,
                                   guid: PlanetSideGUID,
                                   squad: Squad
                                 ): Unit = {
    //search result decoration (per user)
    val result = SquadResponse.SquadSearchResults(List(guid))
    val excluded = searchData.collect {
      case (charId: Long, data: SearchCriteria)
        if data.faction == faction && SearchForSquadsResults(data, guid).nonEmpty =>
        subs.Publish(charId, result)
        (charId, charId)
    }.keys.toList
    //normal decoration (whole faction, later excluding the former users)
    subs.Publish(faction, SquadResponse.SquadDecoration(guid, squad), excluded)
  }

  def ApplySquadDecorationToEntriesForUser(
                                            faction: PlanetSideEmpire.Value,
                                            targetCharId: Long
                                          ): Unit = {
    publishedLists(faction)
      .flatMap { GetSquad }
      .foreach { features =>
        val squad = features.Squad
        val guid = squad.GUID
        val result = SquadResponse.SquadSearchResults(List(guid))
        if (searchData.get(targetCharId) match {
          case Some(data)
            if data.faction == faction && SearchForSquadsResults(data, guid).nonEmpty =>
            subs.Publish(targetCharId, result)
            false
          case _ =>
            true
        }) {
          subs.Publish(targetCharId, SquadResponse.SquadDecoration(guid, squad))
        }
      }
  }
}

object SquadService {
  final case class PerformStartSquad(player: Player)

  final case class PerformJoinSquad(player: Player, features: SquadFeatures, position: Int)

  final case class ResendActiveInvite(charId: Long)

  /**
    * A message to indicate that the squad list needs to update for the clients.
    * @param features the squad
    * @param changes optional changes to the squad details
    */
  final case class UpdateSquadList(features: SquadFeatures, changes: Option[SquadInfo])
  /**
    * A message to indicate that the squad list needs to update for the clients,
    * but only if that squad is already listed.
    * @param features the squad
    * @param changes the changes to the squad details
    */
  final case class UpdateSquadListWhenListed(features: SquadFeatures, changes: SquadInfo)

  private case class SearchCriteria(
                                     faction: PlanetSideEmpire.Value,
                                     zoneId: Int,
                                     role: String,
                                     requirements: Set[Certification],
                                     mode: SquadRequestAction.SearchMode.Value
                                   )
  private object SearchCriteria {
    def apply(
               faction: PlanetSideEmpire.Value,
               criteria: SquadRequestAction.SearchForSquadsWithParticularRole
             ): SearchCriteria = {
      SearchCriteria(faction, criteria.zone_id, criteria.role, criteria.requirements, criteria.mode)
    }
  }

  /**
    * Move one player into one squad role and,
    * if encountering a player already recruited to the destination role,
    * swap that other player into the first player's position.
    * If no encounter, just blank the original role.
    * Certification requirements for the role are not respected.
    * @see `AssignSquadMemberToRole`
    * @see `SelectRoleForYourself`
    * @param toMember the squad role where the player is being placed
    * @param fromMember the squad role where the player is being encountered;
    *                   if a conflicting player is discovered, swap that player into `fromMember`
    */
  def SwapMemberPosition(toMember: Member, fromMember: Member): Unit = {
    val (name, charId, zoneId, pos, health, armor) =
      (fromMember.Name, fromMember.CharId, fromMember.ZoneId, fromMember.Position, fromMember.Health, fromMember.Armor)
    if (toMember.CharId > 0) {
      fromMember.Name = toMember.Name
      fromMember.CharId = toMember.CharId
      fromMember.ZoneId = toMember.ZoneId
      fromMember.Position = toMember.Position
      fromMember.Health = toMember.Health
      fromMember.Armor = toMember.Armor
    } else {
      fromMember.Name = ""
      fromMember.CharId = 0L
    }
    toMember.Name = name
    toMember.CharId = charId
    toMember.ZoneId = zoneId
    toMember.Position = pos
    toMember.Health = health
    toMember.Armor = armor
  }

  /**
    * Produce complete squad information.
    * @see `SquadInfo`
    * @param squad the squad
    * @return the squad's information to be used in the squad list
    */
  def PublishFullListing(squad: Squad): SquadInfo = {
    SquadInfo(
      squad.Leader.Name,
      squad.Task,
      PlanetSideZoneID(squad.ZoneId),
      squad.Size,
      squad.Capacity,
      squad.GUID
    )
  }

  /**
    * Produce complete squad membership details.
    * @see `SquadDetail`
    * @param squad the squad
    * @return the squad's information to be used in the squad's detail window
    */
  def PublishFullDetails(squad: Squad): SquadDetail = {
    SquadDetail()
      .Guid(squad.GUID.guid)
      .LeaderCharId(squad.Leader.CharId)
      .LeaderName(squad.Leader.Name)
      .Task(squad.Task)
      .ZoneId(PlanetSideZoneID(squad.ZoneId))
      .Members(
        squad.Membership.zipWithIndex
          .map({
            case (p, index) =>
              SquadPositionEntry(
                index,
                if (squad.Availability(index)) {
                  SquadPositionDetail(p.Role, p.Orders, p.Requirements, p.CharId, p.Name)
                } else {
                  SquadPositionDetail.Closed
                }
              )
          })
          .toList
      )
      .Complete
  }
}
