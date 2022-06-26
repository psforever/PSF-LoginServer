// Copyright (c) 2019-2022 PSForever
package net.psforever.services.teamwork

import akka.actor.{Actor, ActorRef, Terminated}
import java.io.{PrintWriter, StringWriter}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
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
  private var squadFeatures: TrieMap[PlanetSideGUID, SquadFeatures] = new TrieMap[PlanetSideGUID, SquadFeatures]()

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
  private var memberToSquad: mutable.LongMap[PlanetSideGUID] = mutable.LongMap[PlanetSideGUID]()

  /**
    * key - a unique character identifier number; value - the active invitation object
    */
  private val invites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  /**
    * key - a unique character identifier number; value - a list of inactive invitation objects waiting to be resolved
    */
  private val queuedInvites: mutable.LongMap[List[Invitation]] = mutable.LongMap[List[Invitation]]()

  /**
    * The given player has refused participation into this other player's squad.<br>
    * key - a unique character identifier number;
    * value - a list of unique character identifier numbers, squad leaders or once-squad leaders
    */
  /*
    When a player refuses an invitation by a squad leader,
    that squad leader will not be able to send further invitations (this field).
    If the player submits an invitation request for that squad,
    the current squad leader is cleared from the blocked list.
    When a squad leader refuses an invitation by a player,
    that player will not be able to send further invitations (field on sqaud's features).
    If the squad leader sends an invitation request for that player,
    the current player is cleared from the blocked list.
   */
  private val refused: mutable.LongMap[List[Long]] = mutable.LongMap[List[Long]]()

  private val searchData: mutable.LongMap[SquadService.SearchCriteria] =
    mutable.LongMap[SquadService.SearchCriteria]()

  /**
    * A placeholder for an absent active invite that has not (yet) been accepted or rejected,
    * equal to the then-current active invite.
    * Created when removing an active invite.
    * Checked when trying to add a new invite (if found, the new invite is queued).
    * Cleared when the next queued invite becomes active.<br>
    * key - unique character identifier number; value, unique character identifier number
    */
  private val previousInvites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  private implicit val subs = new SquadSubscriptionEntity()

  private[this] val log = org.log4s.getLogger

  private def info(msg: String): Unit = log.info(msg)

  private def debug(msg: String): Unit = log.debug(msg)

  override def postStop(): Unit = {
    //invitations
    invites.clear()
    queuedInvites.clear()
    previousInvites.clear()
    refused.clear()
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
    if (j == 65536) {
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
          SquadActionDefinition(message, sqd.action, sqd.guid, sender())

        case _: SquadAction.Waypoint =>
          SquadActionWaypoint(message, tplayer, sender())

        case _: SquadAction.Update => //try to avoid using; use the squad actor itself for updates
          SquadActionUpdate(message, tplayer.CharId, sender())

        case msg =>
          log.warn(s"Unhandled action $msg from ${sender()}")
      }

    case SquadService.UpdateSquadList(features, changes) =>
      UpdateSquadList(features, changes)

    case SquadService.UpdateSquadListWhenListed(features, changes) =>
      UpdateSquadListWhenListed(features, changes)

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
      refused(longCharId) = Nil
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
        SquadActionMembershipProximityInvite(tplayer, zone, invitingPlayer)

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
        SquadActionMembershipPromote(promotingPlayer, _promotedPlayer, promotedName)

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
            Allowed(invitedPlayer, invitingPlayer)
            val squad1 = invitersFeatures.Squad
            if (squad1.Size == squad1.Capacity) {
              debug(s"$invitingPlayer tried to invite $invitedPlayer to a squad without available positions")
            } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
              debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
            } else {
              val charId = tplayer.CharId
              invitersFeatures.AllowedPlayers(charId)
              AddInviteAndRespond(
                invitedPlayer,
                VacancyInvite(charId, tplayer.Name, squad1.GUID),
                charId,
                tplayer.Name
              )
            }

          case (Some(invitersFeatures), Some(invitedFeatures))
            if invitersFeatures.Squad.Size == 1 =>
            //both players belong to squads, but the invitingPlayer's squad is underutilized by comparison
            //treat the same as "indirection ..." using squad2
            val squad2 = invitedFeatures.Squad
            val leader = squad2.Leader.CharId
            Allowed(invitedPlayer, invitingPlayer)
            Allowed(leader, invitingPlayer)
            if (squad2.Size == squad2.Capacity) {
              debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but the squad has no available positions")
            } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
              debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
            } else if (Refused(invitingPlayer).contains(leader)) {
              debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $leader repeated a previous refusal to $invitingPlayer's invitation offer")
            } else if (invitedFeatures.DeniedPlayers().contains(invitingPlayer)) {
              debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $invitingPlayer is denied the invitation")
            } else {
              invitersFeatures.AllowedPlayers(invitedPlayer)
              AddInviteAndRespond(
                leader,
                IndirectInvite(tplayer, squad2.GUID),
                invitingPlayer,
                tplayer.Name
              )
            }

          case (Some(features), None) =>
            //the classic situation
            val squad = features.Squad
            Allowed(invitedPlayer, invitingPlayer)
            if (squad.Size == squad.Capacity) {
              debug(s"$invitingPlayer tried to invite $invitedPlayer to a squad without available positions")
            } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
              debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
            } else {
              features.AllowedPlayers(invitedPlayer)
              AddInviteAndRespond(
                invitedPlayer,
                VacancyInvite(tplayer.CharId, tplayer.Name, squad.GUID),
                invitingPlayer,
                tplayer.Name
              )
            }

          case (None, Some(features)) =>
            //indirection;  we're trying to invite ourselves to someone else's squad
            val squad = features.Squad
            val leader = squad.Leader.CharId
            Allowed(invitedPlayer, invitingPlayer)
            Allowed(leader, invitingPlayer)
            if (squad.Size == squad.Capacity) {
              debug(s"$invitingPlayer tried to invite to $invitedPlayer's squad, but the squad has no available positions")
            } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
              debug(s"invitingPlayer tried to invite to $invitedPlayer's squad, but $invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
            } else if (Refused(invitingPlayer).contains(leader)) {
              debug(s"invitingPlayer tried to invite to $invitedPlayer's squad, but $leader repeated a previous refusal to $invitingPlayer's invitation offer")
            } else if (features.DeniedPlayers().contains(invitingPlayer)) {
              debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $invitingPlayer is denied the invitation")
            } else {
              AddInviteAndRespond(
                squad.Leader.CharId,
                IndirectInvite(tplayer, squad.GUID),
                invitingPlayer,
                tplayer.Name
              )
            }

          case (None, None) =>
            //neither the invited player nor the inviting player belong to any squad
            Allowed(invitedPlayer, invitingPlayer)
            if (Refused(invitingPlayer).contains(invitedPlayer)) {
              debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
            } else if (Refused(invitedPlayer).contains(invitingPlayer)) {
              debug(s"$invitingPlayer repeated a previous refusal to $invitedPlayer's invitation offer")
            } else {
              AddInviteAndRespond(
                invitedPlayer,
                SpontaneousInvite(tplayer),
                invitingPlayer,
                tplayer.Name
              )
            }

          case _ => ;
        }
      case _ => ;
    }
  }

  def SquadActionMembershipProximityInvite(tplayer: Player, zone: Zone, invitingPlayer: Long): Unit = {
    GetLeadingSquad(invitingPlayer, None) match {
      case Some(features) =>
        val squad = features.Squad
        val sguid    = squad.GUID
        val origSearchForRole = features.SearchForRole
        var newRecruitment = List[Long]()
        val positionsToRecruitFor = (origSearchForRole match {
          case Some(-1) =>
            //we've already issued a proximity invitation; no need to do another
            debug("ProximityInvite: wait for existing proximity invitations to clear")
            Array[(Member, Int)]()
          case Some(position) =>
            //already searching for the given position; retain the key if the invite is active, or clear if if not
            features.SearchForRole = -1
            invites.find {
              case (_, LookingForSquadRoleInvite(_, _, squad_guid, role)) =>
                squad_guid == sguid && role == position
              case _ =>
                false
            } match {
              case Some((key, _)) =>
                newRecruitment = newRecruitment :+ key
                squad.Membership.zipWithIndex.filterNot { case (_, index) => index == position }
              case None =>
                CleanUpQueuedInvitesForSquadAndPosition(squad.GUID, position)
                squad.Membership.zipWithIndex
            }
          case _ =>
            features.SearchForRole = -1
            squad.Membership.zipWithIndex
        })
          .collect { case (member, index) if member.CharId == 0 && squad.Availability(index) => (member, index) }
          .sortBy({ _._1.Requirements.foldLeft(0)(_ + _.value) })(Ordering.Int.reverse)
        //find recruits
        val players = zone.LivePlayers.map { _.avatar }
        if (positionsToRecruitFor.nonEmpty && players.nonEmpty) {
          val leaderCharId = squad.Leader.CharId
          val name = squad.Leader.Name
          //does this do anything?
          subs.Publish(
            invitingPlayer,
            SquadResponse.Membership(
              SquadResponseType.ProximityInvite,
              19,
              0,
              invitingPlayer,
              None,
              "",
              unk5=true,
              Some(None)
            )
          )
          positionsToRecruitFor.foreach { case (_, position) =>
            FindSoldiersWithinScopeAndInvite(
              leaderCharId,
              name,
              sguid,
              position,
              players,
              features.ProxyInvites ++ newRecruitment,
              proximityInviteEnvelope
            ) match {
              case None => ;
              case Some(id) =>
                newRecruitment = newRecruitment :+ id
            }
          }
        }
        if (newRecruitment.isEmpty) {
          features.SearchForRole = origSearchForRole
        } else {
          features.ProxyInvites = features.ProxyInvites ++ newRecruitment
          //if searching for a position originally, convert the active invite to proximity invite, or remove it
          val key = newRecruitment.head
          (origSearchForRole, invites.get(key)) match {
            case (Some(-1), _) => ;
            case (Some(position), Some(LookingForSquadRoleInvite(charId, name, _, _))) =>
              invites(key) = ProximityInvite(charId, name, sguid, position)
            case _ => ;
          }
        }
      case None => ;
    }
  }

  def SquadActionMembershipAccept(tplayer: Player, invitedPlayer: Long): Unit = {
    SquadActionMembershipAcceptInvite(tplayer, invitedPlayer, RemoveInvite(invitedPlayer))
    NextInviteAndRespond(invitedPlayer)
  }

  def SquadActionMembershipAcceptInvite(
                                         tplayer: Player,
                                         invitedPlayer: Long,
                                         acceptedInvite: Option[SquadService.Invitation]
                                       ): Unit = {
    acceptedInvite match {
      case Some(RequestRole(petitioner, guid, position))
        if EnsureEmptySquad(petitioner.CharId) && squadFeatures.get(guid).nonEmpty =>
        //player requested to join a squad's specific position
        //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
        JoinSquad(petitioner, squadFeatures(guid), position)
        CleanUpAllInvitesWithPlayer(petitioner.CharId)
        CleanUpInvitesForSquadAndPosition(guid, position)

      case Some(IndirectInvite(recruit, guid)) if EnsureEmptySquad(recruit.CharId) =>
        //tplayer / invitedPlayer is actually the squad leader
        val recruitCharId = recruit.CharId
        HandleVacancyInvite(guid, recruitCharId, invitedPlayer, recruit) match {
          case Some((features, line)) =>
            val squad = features.Squad
            subs.Publish(
              invitedPlayer,
              SquadResponse.Membership(
                SquadResponseType.Accept,
                0,
                0,
                invitedPlayer,
                Some(recruitCharId),
                recruit.Name,
                unk5=true,
                Some(None)
              )
            )
            JoinSquad(recruit, features, line)
            CleanUpAllInvitesWithPlayer(recruitCharId)
            CleanUpInvitesForSquadAndPosition(squad.GUID, line)
          //TODO since we are the squad leader, we do not want to brush off our queued squad invite tasks
          case _ => ;
        }

      case Some(VacancyInvite(invitingPlayer, _, guid)) if EnsureEmptySquad(invitedPlayer) =>
        //accepted an invitation to join an existing squad
        HandleVacancyInvite(guid, invitedPlayer, invitingPlayer, tplayer) match {
          case Some((features, line)) =>
            val squad = features.Squad
            subs.Publish(
              invitingPlayer,
              SquadResponse.Membership(
                SquadResponseType.Accept,
                0,
                0,
                invitingPlayer,
                Some(invitedPlayer),
                tplayer.Name,
                unk5=false,
                Some(None)
              )
            )
            subs.Publish(
              invitedPlayer,
              SquadResponse.Membership(
                SquadResponseType.Accept,
                0,
                0,
                invitedPlayer,
                Some(invitingPlayer),
                "",
                unk5=true,
                Some(None)
              )
            )
            JoinSquad(tplayer, features, line)
            CleanUpQueuedInvites(invitedPlayer)
            CleanUpInvitesForSquadAndPosition(squad.GUID, line)
          case _ => ;
        }

      case Some(SpontaneousInvite(invitingPlayer)) if EnsureEmptySquad(invitedPlayer) =>
        //originally, we were invited by someone into a new squad they would form
        val invitingPlayerCharId = invitingPlayer.CharId
        (GetParticipatingSquad(invitingPlayer) match {
          case Some(participating) =>
            //invitingPlayer became part of a squad while invited player was answering the original summons
            Some(participating)
          case _ =>
            //generate a new squad, with invitingPlayer as the leader
            val features = StartSquad(invitingPlayer)
            val squad = features.Squad
            squad.Task = s"${invitingPlayer.Name}'s Squad"
            subs.Publish(invitingPlayerCharId, SquadResponse.AssociateWithSquad(squad.GUID))
            Some(features)
        }) match {
          case Some(features) =>
            HandleVacancyInvite(features, tplayer.CharId, invitingPlayerCharId, tplayer) match {
              case Some((_, line)) =>
                subs.Publish(
                  invitedPlayer,
                  SquadResponse.Membership(
                    SquadResponseType.Accept,
                    0,
                    0,
                    invitedPlayer,
                    Some(invitingPlayerCharId),
                    "",
                    unk5=true,
                    Some(None)
                  )
                )
                subs.Publish(
                  invitingPlayerCharId,
                  SquadResponse.Membership(
                    SquadResponseType.Accept,
                    0,
                    0,
                    invitingPlayerCharId,
                    Some(invitedPlayer),
                    tplayer.Name,
                    unk5=false,
                    Some(None)
                  )
                )
                JoinSquad(tplayer, features, line)
                CleanUpQueuedInvites(tplayer.CharId)
              case _ => ;
            }
          case _ => ;
        }

      case Some(LookingForSquadRoleInvite(invitingPlayer, _, guid, position))
        if EnsureEmptySquad(invitedPlayer) =>
        squadFeatures.get(guid) match {
          case Some(features) =>
            features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
            if (JoinSquad(tplayer, features, position)) {
              //join this squad
              subs.Publish(
                invitedPlayer,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitedPlayer,
                  Some(invitingPlayer),
                  "",
                  unk5=true,
                  Some(None)
                )
              )
              subs.Publish(
                invitingPlayer,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitingPlayer,
                  Some(invitedPlayer),
                  tplayer.Name,
                  unk5=false,
                  Some(None)
                )
              )
              CleanUpQueuedInvites(tplayer.CharId)
              CleanUpInvitesForSquadAndPosition(guid, position)
            }
          case _ => ;
          //squad no longer exists?
        }

      case Some(ProximityInvite(invitingPlayer, _, guid, position)) if EnsureEmptySquad(invitedPlayer) =>
        squadFeatures.get(guid) match {
          case Some(features) =>
            features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
            if (JoinSquad(tplayer, features, position)) {
              //join this squad
              subs.Publish(
                invitedPlayer,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitedPlayer,
                  Some(invitingPlayer),
                  "",
                  unk5 = true,
                  Some(None)
                )
              )
              subs.Publish(
                invitingPlayer,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitingPlayer,
                  Some(invitedPlayer),
                  tplayer.Name,
                  unk5 = false,
                  Some(None)
                )
              )
              CleanUpAllInvitesWithPlayer(invitedPlayer)
              val squad = features.Squad
              if (squad.Size == squad.Capacity) {
                //all available squad positions filled; terminate all remaining invitations
                CleanUpAllInvitesToSquad(guid)
              }
            } else {
              ReloadProximityInvite(tplayer.Zone.Players, invitedPlayer, guid, position) //TODO ?
            }
          case _ =>
          //squad no longer exists?
        }

      case _ =>
        //the invite either timed-out or was withdrawn or is now invalid
        (previousInvites.get(invitedPlayer) match {
          case Some(SpontaneousInvite(leader))                     => (leader.CharId, leader.Name)
          case Some(VacancyInvite(charId, name, _))                => (charId, name)
          case Some(ProximityInvite(charId, name, _, _))           => (charId, name)
          case Some(LookingForSquadRoleInvite(charId, name, _, _)) => (charId, name)
          case _                                                   => (0L, "")
        }) match {
          case (0L, "") => ;
          case (charId, name) =>
            subs.Publish(
              charId,
              SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, charId, Some(0L), name, unk5=false, Some(None))
            )
        }
    }
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
    val rejectedBid = RemoveInvite(rejectingPlayer)
    //(A, B) -> person who rejected the invitation, person who was rejected
    (rejectedBid match {
      case Some(SpontaneousInvite(leader)) =>
        //rejectingPlayer is the would-be squad member; the would-be squad leader sent the request and was rejected
        val invitingPlayerCharId = leader.CharId
        Refused(rejectingPlayer, invitingPlayerCharId)
        (Some(rejectingPlayer), Some(invitingPlayerCharId))

      case Some(VacancyInvite(leader, _, guid))
        if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        Refused(rejectingPlayer, leader)
        (Some(rejectingPlayer), Some(leader))

      case Some(ProximityInvite(_, _, guid, position))
        if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        ReloadProximityInvite(
          tplayer.Zone.Players,
          rejectingPlayer,
          guid,
          position
        )
        (Some(rejectingPlayer), None)

      case Some(LookingForSquadRoleInvite(leaderCharId, _, guid, position))
        if squadFeatures.get(guid).nonEmpty && leaderCharId != rejectingPlayer =>
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        ReloadSearchForRoleInvite(
          LivePlayerList.WorldPopulation({ case (_, avatar: Avatar) => avatar.lookingForSquad }),
          rejectingPlayer,
          guid,
          position
        )
        (Some(rejectingPlayer), Some(leaderCharId))

      case Some(RequestRole(rejected, guid, _))
        if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId == rejectingPlayer =>
        //rejected is the would-be squad member; rejectingPlayer is the squad leader who rejected the request
        squadFeatures(guid).DeniedPlayers(rejected.CharId)
        (Some(rejectingPlayer), None)

      case _ => ; //TODO IndirectInvite, etc., but how to handle them?
        (None, None)
    }) match {
      case (Some(rejected), Some(invited)) =>
        subs.Publish(
          rejected,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(invited), "", unk5=true, Some(None))
        )
        subs.Publish(
          invited,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, invited, Some(rejected), tplayer.Name, unk5=false, Some(None))
        )
      case (Some(rejected), None) =>
        subs.Publish(
          rejected,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(rejected), "", unk5=true, Some(None))
        )
      case _ => ;
    }
    NextInviteAndRespond(rejectingPlayer)
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
    invites.collect {
      case (id, invite: SpontaneousInvite) if invite.InviterCharId == cancellingPlayer =>         RemoveInvite(id)
      case (id, invite: VacancyInvite) if invite.InviterCharId == cancellingPlayer =>             RemoveInvite(id)
      case (id, invite: LookingForSquadRoleInvite) if invite.InviterCharId == cancellingPlayer => RemoveInvite(id)
    }
    queuedInvites.foreach {
      case (id: Long, inviteList) =>
        val inList = inviteList.filterNot {
          case invite: SpontaneousInvite if invite.InviterCharId == cancellingPlayer         => true
          case invite: VacancyInvite if invite.InviterCharId == cancellingPlayer             => true
          case invite: LookingForSquadRoleInvite if invite.InviterCharId == cancellingPlayer => true
          case _                                                                             => false
        }
        if (inList.isEmpty) {
          queuedInvites.remove(id)
        } else {
          queuedInvites(id) = inList
        }
    }
    //get rid of ProximityInvite objects
    CleanUpAllProximityInvites(cancellingPlayer)
  }

  def SquadActionMembershipPromote(promotingPlayer: Long, _promotedPlayer: Long, promotedName: String): Unit = {
    val promotedPlayer = (if (promotedName.nonEmpty) {
      //validate player with name exists
      LivePlayerList
        .WorldPopulation({ case (_, a: Avatar) => a.name.equalsIgnoreCase(promotedName) })
        .headOption match {
        case Some(a) => subs.UserEvents.keys.find(_ == a.id)
        case None    => Some(_promotedPlayer)
      }
    } else {
      Some(_promotedPlayer)
    }) match {
      case Some(player) => player
      case None         => -1L
    }
    (GetLeadingSquad(promotingPlayer, None), GetParticipatingSquad(promotedPlayer)) match {
      case (Some(features1), Some(features2)) if features1.Squad.GUID == features2.Squad.GUID =>
        val squad1 = features1.Squad
        val membership = squad1.Membership.filter { _member => _member.CharId > 0 }
        val leader     = squad1.Leader
        val (member, index) = membership.zipWithIndex.find {
          case (_member, _) => _member.CharId == promotedPlayer
        }.get
        SwapMemberPosition(leader, member)
        //move around invites so that the proper squad leader deals with them
        val leaderInvite        = invites.remove(promotingPlayer)
        val leaderQueuedInvites = queuedInvites.remove(promotingPlayer).toList.flatten
        invites.get(promotedPlayer).orElse(previousInvites.get(promotedPlayer)) match {
          case Some(_) =>
            //the promoted player has an active invite; queue these
            queuedInvites += promotedPlayer -> (leaderInvite.toList ++ leaderQueuedInvites ++ queuedInvites
              .remove(promotedPlayer)
              .toList
              .flatten)
          case None if leaderInvite.nonEmpty =>
            //no active invite for the promoted player, but the leader had an active invite; trade the queued invites
            val invitation = leaderInvite.get
            AddInviteAndRespond(promotedPlayer, invitation, invitation.InviterCharId, invitation.InviterName)
            queuedInvites += promotedPlayer -> (leaderQueuedInvites ++ queuedInvites
              .remove(promotedPlayer)
              .toList
              .flatten)
          case None =>
            //no active invites for anyone; assign the first queued invite from the promoting player, if available, and queue the rest
            leaderQueuedInvites match {
              case Nil => ;
              case x :: xs =>
                AddInviteAndRespond(promotedPlayer, x, x.InviterCharId, x.InviterName)
                queuedInvites += promotedPlayer -> (xs ++ queuedInvites.remove(promotedPlayer).toList.flatten)
            }
        }
        info(s"Promoting player ${leader.Name} to be the leader of ${squad1.Task}")
        subs.Publish(features1.ToChannel, SquadResponse.PromoteMember(squad1, promotedPlayer, index, 0))
        if (features1.Listed) {
          subs.Publish(promotingPlayer, SquadResponse.SetListSquad(PlanetSideGUID(0)))
          subs.Publish(promotedPlayer, SquadResponse.SetListSquad(squad1.GUID))
        }
        UpdateSquadListWhenListed(
          features1,
          SquadInfo().Leader(leader.Name)
        )
        subs.UpdateSquadDetail(
          features1,
          SquadDetail()
            .LeaderCharId(leader.CharId)
            .OutfitId(outfit = 0L)
            .LeaderName(leader.Name)
            .Members(
              List(
                SquadPositionEntry(0, SquadPositionDetail().CharId(leader.CharId).Name(leader.Name)),
                SquadPositionEntry(index, SquadPositionDetail().CharId(member.CharId).Name(member.Name))
              )
            )
        )
      case _ => ;
    }
  }

  def SquadActionWaypoint(
                           message: SquadServiceMessage,
                           tplayer: Player,
                           sendTo: ActorRef
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
                             guid: PlanetSideGUID,
                             sendTo: ActorRef
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
      case _: AutoApproveInvitationRequests =>               GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: RequestListSquad =>                            GetOrCreateSquadOnlyIfLeader(tplayer)
      case _: StopListSquad =>                               GetLeadingSquad(tplayer, None)
        //the following actions cause changes with the squad composition or with invitations
      case CloseSquadMemberPosition(position) =>
        GetOrCreateSquadOnlyIfLeader(tplayer) match {
          case out @ Some(features)
           if features.Squad.Membership(position).CharId > 0 =>
            val squad = features.Squad
            LeaveSquad(squad.Membership(position).CharId, features)
            out
          case None =>
            None
        }
      case FindLfsSoldiersForRole(position) =>
        SquadActionDefinitionFindLfsSoldiersForRole(tplayer, message.zone, position)
        None
      case CancelFind() =>
        SquadActionDefinitionCancelFind(None)
        None
      case SelectRoleForYourself(position) =>
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
            SquadActionDefinitionSelectRoleForYourselfAsInvite(tplayer, guid, position)
            None
        }
      case _: CancelSelectRoleForYourself =>
        SquadActionDefinitionCancelSelectRoleForYourself(tplayer, guid)
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

  def SquadActionDefinitionAutoApproveInvitationRequests(
                                                          tplayer: Player,
                                                          pSquadOpt: Option[SquadFeatures],
                                                          lSquadOpt: Option[SquadFeatures],
                                                          state: Boolean
                                                        ): Unit = {
    GetOrCreateSquadOnlyIfLeader(tplayer) match {
      case Some(features) =>
        features.AutoApproveInvitationRequests = state
        if (state) {
          //allowed auto-approval - resolve the requests (only)
          val charId = tplayer.CharId
          val (requests, others) = (invites.get(charId).toList ++ queuedInvites.get(charId).toList)
            .partition({ case _: RequestRole => true })
          invites.remove(charId)
          queuedInvites.remove(charId)
          previousInvites.remove(charId)
          requests.foreach {
            case request: RequestRole =>
              JoinSquad(request.player, features, request.position)
            case _ => ;
          }
          others.collect { case invite: Invitation => invite } match {
            case Nil => ;
            case x :: Nil =>
              AddInviteAndRespond(charId, x, x.InviterCharId, x.InviterName)
            case x :: xs =>
              AddInviteAndRespond(charId, x, x.InviterCharId, x.InviterName)
              queuedInvites += charId -> xs
          }
        }
      case None => ;
    }
  }

  def SquadActionDefinitionFindLfsSoldiersForRole(
                                                   tplayer: Player,
                                                   zone: Zone,
                                                   position: Int
                                                 ): Unit = {
    GetLeadingSquad(tplayer, None) match {
      case Some(features) =>
        val squad = features.Squad
        val sguid = squad.GUID
        (features.SearchForRole match {
          case None =>
            Some(Nil)
          case Some(-1) =>
            //a proximity invitation has not yet cleared; nothing will be gained by trying to invite for a specific role
            log.debug("FindLfsSoldiersForRole: waiting for proximity invitations to clear")
            None
          case Some(pos) if pos == position =>
            //already recruiting for this specific position in the squad? do nothing
            log.debug("FindLfsSoldiersForRole: already recruiting for this position; client-server mismatch?")
            None
          case Some(pos) =>
            //some other role is undergoing recruitment; cancel and redirect efforts for new position
            features.SearchForRole = None
            CleanUpQueuedInvitesForSquadAndPosition(sguid, pos)
            Some(
              invites.filter {
                case (_, LookingForSquadRoleInvite(_, _, squad_guid, role)) => squad_guid == sguid && role == pos
                case _                                                      => false
              }.keys.toList
            )
        }) match {
          case None =>
            features.SearchForRole = None
          case Some(list) =>
            //this will update the role entry in the GUI to visually indicate being searched for; only one will be displayed at a time
            subs.Publish(
              tplayer.CharId,
              SquadResponse.Detail(
                sguid,
                SquadDetail().Members(
                  List(SquadPositionEntry(position, SquadPositionDetail().CharId(char_id = 0L).Name(name = "")))
                )
              )
            )
            //search!
            FindSoldiersWithinScopeAndInvite(
              tplayer.CharId,
              tplayer.Name,
              sguid,
              position,
              LivePlayerList.WorldPopulation({ case (_, avatar: Avatar) => avatar.lookingForSquad }),
              list,
              lookingForSquadRoleEnvelope
            ) match {
              case None => ;
              case Some(id) =>
                features.ProxyInvites = List(id)
                features.SearchForRole = position
            }
        }
      case None => ;
    }
  }

  def SquadActionDefinitionCancelFind(lSquadOpt: Option[SquadFeatures]): Unit = {
    lSquadOpt match {
      case Some(features) =>
        features.SearchForRole match {
          case Some(position) if position > -1 =>
            val squad = features.Squad
            val sguid = squad.GUID
            features.SearchForRole = None
            //remove active invites
            invites
              .filter {
                case (_, LookingForSquadRoleInvite(_, _, _guid, _)) => _guid == sguid
                case _                                              => false
              }
              .keys
              .foreach { charId =>
                RemoveInvite(charId)
              }
            //remove queued invites
            queuedInvites.foreach {
              case (charId, queue) =>
                val filtered = queue.filterNot {
                  case LookingForSquadRoleInvite(_, _, _guid, _) => _guid == sguid
                  case _                                         => false
                }
                queuedInvites += charId -> filtered
                if (filtered.isEmpty) {
                  queuedInvites.remove(charId)
                }
            }
            //remove yet-to-be invitedPlayers
            features.ProxyInvites = Nil
          case _ => ;
        }
      case _ => ;
    }
  }

  /** the following action can be performed by an unaffiliated player */
  def SquadActionDefinitionSelectRoleForYourselfAsInvite(
                                                          tplayer: Player,
                                                          guid: PlanetSideGUID,
                                                          position: Int
                                                        ): Unit = {
    //not a member of any squad, but we might become a member of this one
    GetSquad(guid) match {
      case Some(features) =>
        val squad = features.Squad
        if (squad.isAvailable(position, tplayer.avatar.certifications)) {
          //we could join directly but we need permission from the squad leader first
          if (features.AutoApproveInvitationRequests) {
            SquadActionMembershipAcceptInvite(
              tplayer,
              squad.Leader.CharId,
              Some(RequestRole(tplayer, squad.GUID, position))
            )
          } else {
            //circumvent tests in AddInviteAndRespond
            val requestRole = RequestRole(tplayer, squad.GUID, position)
            val charId = squad.Leader.CharId
            InviteResponseTemplate(indirectInviteResp)(
              requestRole,
              AddInvite(charId, requestRole),
              charId,
              invitingPlayer = 0L, //we ourselves technically are ...
              tplayer.Name
            )
          }
        }
      case None =>
    }
  }

  /** the following action can be performed by anyone who has tried to join a squad */
  def SquadActionDefinitionCancelSelectRoleForYourself(tplayer: Player, guid: PlanetSideGUID): Unit = {
    val cancellingPlayer = tplayer.CharId
    GetSquad(guid) match {
      case Some(features) =>
        //assumption: a player who is cancelling will rarely end up with their invite queued
        val squad = features.Squad
        val leaderCharId = squad.Leader.CharId
        //clean up any active RequestRole invite entry where we are the player who wants to join the leader's squad
        ((invites.get(leaderCharId) match {
          case out @ Some(entry)
            if entry.isInstanceOf[RequestRole] &&
               entry.asInstanceOf[RequestRole].player.CharId == cancellingPlayer =>
            out
          case _ =>
            None
        }) match {
          case Some(entry: RequestRole) =>
            RemoveInvite(leaderCharId)
            subs.Publish(
              leaderCharId,
              SquadResponse.Membership(
                SquadResponseType.Cancel,
                0,
                0,
                cancellingPlayer,
                None,
                entry.player.Name,
                unk5=false,
                Some(None)
              )
            )
            NextInviteAndRespond(leaderCharId)
            Some(true)
          case _ =>
            None
        }).orElse(
          //look for a queued RequestRole entry where we are the player who wants to join the leader's squad
          (queuedInvites.get(leaderCharId) match {
            case Some(_list) =>
              (
                _list,
                _list.indexWhere { entry =>
                  entry.isInstanceOf[RequestRole] &&
                  entry.asInstanceOf[RequestRole].player.CharId == cancellingPlayer
                }
              )
            case None =>
              (Nil, -1)
          }) match {
            case (_, -1) =>
              None //no change
            case (list, _) if list.size == 1 =>
              val entry = list.head.asInstanceOf[RequestRole]
              subs.Publish(
                leaderCharId,
                SquadResponse.Membership(
                  SquadResponseType.Cancel,
                  0,
                  0,
                  cancellingPlayer,
                  None,
                  entry.player.Name,
                  unk5=false,
                  Some(None)
                )
              )
              queuedInvites.remove(leaderCharId)
              Some(true)
            case (list, index) =>
              val entry = list(index).asInstanceOf[RequestRole]
              subs.Publish(
                leaderCharId,
                SquadResponse.Membership(
                  SquadResponseType.Cancel,
                  0,
                  0,
                  cancellingPlayer,
                  None,
                  entry.player.Name,
                  unk5=false,
                  Some(None)
                )
              )
              queuedInvites(leaderCharId) = list.take(index) ++ list.drop(index + 1)
              Some(true)
          }
        )

      case _ => ;
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
        squads.flatMap { guid => SearchForSquadsResults(criteria, guid, squadFeatures(guid).Squad) }.toList
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
                                      criteria: SquadService.SearchCriteria,
                                      guid: PlanetSideGUID,
                                      squad: Squad
                                    ): Option[PlanetSideGUID] = {
    val squad = squadFeatures(guid).Squad
    val positions = if (criteria.mode == SquadRequestAction.SearchMode.AnyPositions) {
      //includes occupied positions and closed positions that retain assignment information
      squad.Membership
    } else {
      squad.Membership.zipWithIndex.filter { case (_, b) => squad.Availability(b) }.unzip._1
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
    subs.MonitorSquadDetails += tplayer.CharId -> guid
  }

  /**
    * This player has been refused to join squads by these players, or to form squads with these players.
    * @param charId the player who refused other players
    * @return the list of other players who have refused this player
    */
  def Refused(charId: Long): List[Long] = refused.getOrElse(charId, Nil)

  /**
    * This player has been refused to join squads by this squad leaders, or to form squads with this other player.
    * @param charId the player who is being refused
    * @param refusedCharId the player who refused
    * @return the list of other players who have refused this player
    */
  def Refused(charId: Long, refusedCharId: Long): List[Long] = {
    if (charId != refusedCharId) {
      Refused(charId, List(refusedCharId))
    } else {
      Nil
    }
  }

  /**
    * This player has been refused to join squads by these squad leaders, or to form squads with these other players.
    * @param charId the player who is being refused
    * @param list the players who refused
    * @return the list of other players who have refused this player
    */
  def Refused(charId: Long, list: List[Long]): List[Long] = {
    refused.get(charId) match {
      case Some(refusedList) =>
        refused(charId) = list ++ refusedList
        Refused(charId)
      case None =>
        Nil
    }
  }

  /**
    * This player has been refused to join squads by this squad leaders, or to form squads with this other player.
    * They are now allowed.
    * @param charId the player who is being refused
    * @param permittedCharId the player who was previously refused
    * @return the list of other players who have refused this player
    */
  def Allowed(charId: Long, permittedCharId: Long): List[Long] = {
    if (charId != permittedCharId) {
      Allowed(charId, List(permittedCharId))
    } else {
      Nil
    }
  }

  /**
    * This player has been refused to join squads by these squad leaders, or to form squads with these other players.
    * They are now allowed.
    * @param charId the player who is being refused
    * @param list the players who was previously refused
    * @return the list of other players who have refused this player
    */
  def Allowed(charId: Long, list: List[Long]): List[Long] = {
    refused.get(charId) match {
      case Some(refusedList) =>
        refused(charId) = refusedList.filterNot(list.contains)
        Refused(charId)
      case None =>
        Nil
    }
  }

  /**
    * Assign a provided invitation object to either the active or inactive position for a player.<br>
    * <br>
    * The determination for the active position is whether or not something is currently in the active position
    * or whether some mechanism tried to shift invitation object into the active position
    * but found nothing to shift.
    * If an invitation object originating from the reported player already exists,
    * a new one is not appended to the inactive queue.
    * This method should always be used as the entry point for the active and inactive invitation options
    * or as a part of the entry point for the aforesaid options.
    * @see `AddInviteAndRespond`
    * @see `AltAddInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invite the "new" invitation envelop object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if it is not added to either the active option or inactive position
    */
  def AddInvite(invitedPlayer: Long, invite: Invitation): Option[Invitation] = {
    invites.get(invitedPlayer).orElse(previousInvites.get(invitedPlayer)) match {
      case Some(_bid) =>
        //the new invite may not interact with the active invite; add to queued invites
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            //ensure that new invite does not interact with the queue's invites by invitingPlayer info
            val inviteInviterCharId = invite.InviterCharId
            if (
              _bid.InviterCharId != inviteInviterCharId && !bidList.exists { eachBid =>
                eachBid.InviterCharId == inviteInviterCharId
              }
            ) {
              queuedInvites(invitedPlayer) = invite match {
                case _: RequestRole =>
                  //RequestRole is to be expedited
                  val (normals, others) = bidList.partition(_.isInstanceOf[RequestRole])
                  (normals :+ invite) ++ others
                case _ =>
                  bidList :+ invite
              }
              Some(_bid)
            } else {
              None
            }
          case None =>
            if (_bid.InviterCharId != invite.InviterCharId) {
              queuedInvites(invitedPlayer) = List(invite)
              Some(_bid)
            } else {
              None
            }
        }

      case None =>
        invites(invitedPlayer) = invite
        Some(invite)
    }
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    * @see `HandleRequestRole`
    * @param invite the original invitation object that started this process
    * @param player the target of the response and invitation
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object;
    *                      not useful here
    * @param invitingPlayer the unique character identifier for the player who invited the former;
    *                       not useful here
    * @param name a name to be used in message composition;
    *             not useful here
    * @return na
    */
  def indirectInviteResp(
                          invite: IndirectInvite,
                          player: Player,
                          invitedPlayer: Long,
                          invitingPlayer: Long,
                          name: String
                        ): Boolean = {
    HandleRequestRole(player, invite)
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    * @see `HandleRequestRole`
    * @param invite the original invitation object that started this process
    * @param player the target of the response and invitation
    * @param invitedPlayer the unique character identifier for the player being invited
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    * @return na
    */
  def altIndirectInviteResp(
                             invite: IndirectInvite,
                             player: Player,
                             invitedPlayer: Long,
                             invitingPlayer: Long,
                             name: String
                           ): Boolean = {
    subs.Publish(
      invitingPlayer,
      SquadResponse.Membership(
        SquadResponseType.Accept,
        0,
        0,
        invitingPlayer,
        Some(invitedPlayer),
        player.Name,
        unk5=false,
        Some(None)
      )
    )
    HandleRequestRole(player, invite)
  }

  /**
    * A branched response for processing (new) invitation objects that have been submitted to the system.<br>
    * <br>
    * A comparison is performed between the original invitation object and an invitation object
    * that represents the potential modification or redirection of the current active invitation obect.
    * Any further action is only performed when an "is equal" comparison is `true`.
    * When passing, the system publishes up to two messages
    * to users that would anticipate being informed of squad join activity.
    * @param indirectInviteFunc the method that cans the responding behavior should an `IndirectInvite` object being consumed
    * @param targetInvite a comparison invitation object;
    *                     represents the unmodified, unadjusted invite
    * @param actualInvite a comparaison invitation object;
    *                     proper use of this field should be the output of another process upon the following `actualInvite`
    * @param invitedPlayer the unique character identifier for the player being invited
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def InviteResponseTemplate(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    targetInvite: Invitation,
    actualInvite: Option[Invitation],
    invitedPlayer: Long,
    invitingPlayer: Long,
    name: String
  ): Unit = {
    if (actualInvite.contains(targetInvite)) {
      //immediately respond
      targetInvite match {
        case VacancyInvite(charId, _name, _) =>
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              charId,
              Some(invitedPlayer),
              _name,
              unk5=false,
              Some(None)
            )
          )
          subs.Publish(
            charId,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              unk5=true,
              Some(None)
            )
          )

        case _bid @ IndirectInvite(player, _) =>
          indirectInviteFunc(_bid, player, invitedPlayer, invitingPlayer, name)

        case _bid @ SpontaneousInvite(player) =>
          val bidInvitingPlayer = _bid.InviterCharId
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              bidInvitingPlayer,
              Some(invitedPlayer),
              player.Name,
              unk5=false,
              Some(None)
            )
          )
          subs.Publish(
            bidInvitingPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(bidInvitingPlayer),
              player.Name,
              unk5=true,
              Some(None)
            )
          )

        case _bid @ RequestRole(player, _, _) =>
          HandleRequestRole(player, _bid)

        case LookingForSquadRoleInvite(charId, _name, _, _) =>
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              unk5=false,
              Some(None)
            )
          )

        case ProximityInvite(charId, _name, _, _) =>
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              unk5=false,
              Some(None)
            )
          )

        case _ =>
          log.warn(s"AddInviteAndRespond: can not parse discovered unhandled invitation type - $targetInvite")
      }
    }
  }

  def ReloadSearchForRoleInvite(
                                 scope: List[Avatar],
                                 rejectingPlayer: Long,
                                 guid: PlanetSideGUID,
                                 position: Int
                               ) : Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val features = squadFeatures(guid)
    val squadLeader = features.Squad.Leader
    Refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    FindSoldiersWithinScopeAndInvite(
      squadLeader.CharId,
      squadLeader.Name,
      guid,
      position,
      scope,
      features.ProxyInvites,
      lookingForSquadRoleEnvelope
    ) match {
      case None =>
        if (features.SearchForRole.contains(position) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          //TODO message the squadLeader.CharId to indicate that there are no more candidates for this position
        }
      case Some(id) =>
        features.ProxyInvites = List(id)
    }
    if (features.ProxyInvites.isEmpty) {
      features.SearchForRole = None
    }
  }

  def ReloadProximityInvite(
                             scope: List[Avatar],
                             rejectingPlayer: Long,
                             guid: PlanetSideGUID,
                             position: Int
                           ): Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val features = squadFeatures(guid)
    val squadLeader = features.Squad.Leader
    Refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    FindSoldiersWithinScopeAndInvite(
      squadLeader.CharId,
      squadLeader.Name,
      guid,
      position,
      scope,
      features.ProxyInvites,
      proximityInviteEnvelope
    ) match {
      case None =>
        if (features.SearchForRole.contains(-1) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          //TODO message the squadLeader.CharId to indicate that there are no more candidates for this position
        }
      case Some(id) =>
        features.ProxyInvites = features.ProxyInvites :+ id
    }
  }

  def FindSoldiersWithinScopeAndInvite(
                                        invitingPlayerCharId: Long,
                                        invitingPlayerName: String,
                                        sguid: PlanetSideGUID,
                                        position: Int,
                                        scope: List[Avatar],
                                        excluded: List[Long],
                                        invitationEnvelopFunc: (Long, String, PlanetSideGUID, Int) => Invitation
                                      ): Option[Long] = {
    val features = squadFeatures(sguid)
    val squad = features.Squad
    val faction = squad.Faction
    val squadLeader = squad.Leader.CharId
    val deniedAndExcluded = features.DeniedPlayers() ++ excluded
    val requirementsToMeet = squad.Membership(position).Requirements
    //find a player who is of the same faction as the squad, is LFS, and is eligible for the squad position
    scope
      .find { avatar =>
        val charId = avatar.id
        faction == avatar.faction &&
        !memberToSquad.contains(charId) &&
        !deniedAndExcluded.contains(charId) &&
        !refused(charId).contains(squadLeader) &&
        requirementsToMeet.intersect(avatar.certifications) == requirementsToMeet
      } match {
      case None =>
        None
      case Some(invitedPlayer) =>
        //add invitation for position in squad
        val invite = invitationEnvelopFunc(invitingPlayerCharId, invitingPlayerName, sguid, position)
        val id = invitedPlayer.id
        AddInviteAndRespond(id, invite, invitingPlayerCharId, invitingPlayerName)
        Some(id)
    }
  }

  def proximityInviteEnvelope(
                               invitingPlayerCharId: Long,
                               invitingPlayerName: String,
                               squadGuid: PlanetSideGUID,
                               position: Int
                             ): Invitation = {
    ProximityInvite(invitingPlayerCharId, invitingPlayerName, squadGuid, position)
  }

  def lookingForSquadRoleEnvelope(
                                   invitingPlayerCharId: Long,
                                   invitingPlayerName: String,
                                   squadGuid: PlanetSideGUID,
                                   position: Int
                                 ): Invitation = {
    LookingForSquadRoleInvite(invitingPlayerCharId, invitingPlayerName, squadGuid, position)
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    * @see `AddInvite`
    * @see `indirectInviteResp`
    * @param targetInvite a comparison invitation object
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def AddInviteAndRespond(invitedPlayer: Long, targetInvite: Invitation, invitingPlayer: Long, name: String): Unit = {
    val (player, approval) = targetInvite match {
      case IndirectInvite(_player, squad_guid) => (_player, squadFeatures(squad_guid).AutoApproveInvitationRequests)
      case RequestRole(_player, squad_guid, _) => (_player, squadFeatures(squad_guid).AutoApproveInvitationRequests)
      case _                                   => (null, false)
    }
    if (approval) {
      SquadActionMembershipAcceptInvite(player, invitingPlayer, Some(targetInvite))
    } else {
      InviteResponseTemplate(indirectInviteResp)(
        targetInvite,
        AddInvite(invitedPlayer, targetInvite),
        invitedPlayer,
        invitingPlayer,
        name
      )
    }
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    * @see `AddInvite`
    * @see `altIndirectInviteResp`
    * @param targetInvite a comparison invitation object
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def AltAddInviteAndRespond(
                              invitedPlayer: Long,
                              targetInvite: Invitation,
                              invitingPlayer: Long,
                              name: String
                            ): Unit = {
    val (player, approval) = targetInvite match {
      case IndirectInvite(_player, squad_guid) => (_player, squadFeatures(squad_guid).AutoApproveInvitationRequests)
      case RequestRole(_player, squad_guid, _) => (_player, squadFeatures(squad_guid).AutoApproveInvitationRequests)
      case _                                   => (null, false)
    }
    if (approval) {
      SquadActionMembershipAcceptInvite(player, invitingPlayer, Some(targetInvite))
    } else {
      InviteResponseTemplate(altIndirectInviteResp)(
        targetInvite,
        AddInvite(invitedPlayer, targetInvite),
        invitedPlayer,
        invitingPlayer,
        name
      )
    }
  }

  /**
    * Select the next invitation object to be shifted into the active position.<br>
    * <br>
    * The determination for the active position is whether or not something is currently in the active position
    * or whether some mechanism tried to shift invitation object into the active position
    * but found nothing to shift.
    * After handling of the previous invitation object has completed or finished,
    * the temporary block on adding new invitations is removed
    * and any queued inactive invitation on the head of the inactive queue is shifted into the active position.
    * @see `NextInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if not shifted into the active position
    */
  def NextInvite(invitedPlayer: Long): Option[Invitation] = {
    previousInvites.remove(invitedPlayer)
    invites.get(invitedPlayer) match {
      case None =>
        queuedInvites.get(invitedPlayer) match {
          case Some(list) =>
            list match {
              case Nil =>
                None
              case x :: Nil =>
                invites(invitedPlayer) = x
                queuedInvites.remove(invitedPlayer)
                Some(x)
              case x :: xs =>
                invites(invitedPlayer) = x
                queuedInvites(invitedPlayer) = xs
                Some(x)
            }

          case None =>
            None
        }
      case Some(_) =>
        None
    }
  }

  /**
    * Select the next invitation object to be shifted into the active position
    * and dispatch a response for any invitation object that is discovered.
    * @see `InviteResponseTemplate`
    * @see `NextInvite`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if not shifted into the active position
    */
  def NextInviteAndRespond(invitedPlayer: Long): Unit = {
    NextInvite(invitedPlayer) match {
      case Some(invite) =>
        InviteResponseTemplate(indirectInviteResp)(
          invite,
          Some(invite),
          invitedPlayer,
          invite.InviterCharId,
          invite.InviterName
        )
      case None => ;
    }
  }

  /**
    * Remove any invitation object from the active position.
    * Flag the temporary field to indicate that the active position, while technically available,
    * should not yet have a new invitation object shifted into it yet.
    * This is the "proper" way to demote invitation objects from the active position
    * whether or not they are to be handled
    * except in cases of manipulative cleanup.
    * @see `NextInvite`
    * @see `NextInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object formerly in the active invite position;
    *         `None`, if no invitation was in the active position
    */
  def RemoveInvite(invitedPlayer: Long): Option[Invitation] = {
    invites.remove(invitedPlayer) match {
      case out @ Some(invite) =>
        previousInvites += invitedPlayer -> invite
        out
      case None =>
        None
    }
  }

  /**
    * Remove all inactive invites associated with this player.
    * @param charId the unique character identifier for the player being invited;
    *               in actuality, represents the player who will address the invitation object
    * @return a list of the removed inactive invitation objects
    */
  def CleanUpQueuedInvites(charId: Long): Unit = {
    val allSquadGuids = queuedInvites.remove(charId) match {
      case Some(bidList) =>
        bidList.collect {
          case VacancyInvite(_, _, guid)                => guid
          case IndirectInvite(_, guid)                  => guid
          case LookingForSquadRoleInvite(_, _, guid, _) => guid
          case ProximityInvite(_, _, guid, _)           => guid
          case RequestRole(_, guid, _)                  => guid
        }
      case None =>
        Nil
    }
    val list = List(charId)
    allSquadGuids.foreach { CleanUpSquadFeatures(list, _, position = -1) }
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
    * Remove all invitation objects
    * that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
    * Affects only certain invitation object types
    * including "player requesting role" and "leader requesting recruiting role".
    * @see `RemoveActiveInvitesForSquadAndPosition`
    * @see `RemoveQueuedInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    */
  def CleanUpInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): Unit = {
    CleanUpSquadFeatures(
      RemoveActiveInvitesForSquadAndPosition(guid, position) ++ RemoveQueuedInvitesForSquadAndPosition(guid, position),
      guid,
      position
    )
  }

  /**
    * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `CleanUpInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    */
  def CleanUpQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): Unit = {
    CleanUpSquadFeatures(
      RemoveQueuedInvitesForSquadAndPosition(guid, position),
      guid,
      position
    )
  }

  /**
    * Remove all invitation objects that are related to the particular squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the squad.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `ProximityInvite`
    * @see `RemoveInvite`
    * @see `VacancyInvite`
    * @param guid the squad identifier
    */
  def CleanUpAllInvitesToSquad(guid: PlanetSideGUID): Unit = {
    //clean up invites
    val activeInviteIds = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (VacancyInvite(_, _, sguid), index) if sguid == guid                => index
          case (IndirectInvite(_, sguid), index) if sguid == guid                  => index
          case (LookingForSquadRoleInvite(_, _, sguid, _), index) if sguid == guid => index
          case (ProximityInvite(_, _, sguid, _), index) if sguid == guid           => index
          case (RequestRole(_, sguid, _), index) if sguid == guid                  => index
        }
        .map { index =>
          val key = keys(index)
          RemoveInvite(key)
          key
        }
        .toList
    }
    //tidy the queued invitations
    val queuedInviteIds = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case VacancyInvite(_, _, sguid)                => sguid == guid
              case IndirectInvite(_, sguid)                  => sguid == guid
              case LookingForSquadRoleInvite(_, _, sguid, _) => sguid == guid
              case ProximityInvite(_, _, sguid, _)           => sguid == guid
              case RequestRole(_, sguid, _)                  => sguid == guid
              case _                                         => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some(key)
            } else {
              None
            }
        }
        .flatten
        .toList
    }
    CleanUpSquadFeatures(activeInviteIds ++ queuedInviteIds, guid, position = -1)
  }

  /**
    * Remove all active and inactive invitation objects that are related to the particular player.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the player.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `RemoveInvite`
    * @see `CleanUpAllProximityInvites`
    * @see `VacancyInvite`
    * @param charId the player's unique identifier number
    */
  def CleanUpAllInvitesWithPlayer(charId: Long): Unit = {
    //clean up our active invitation
    val charIdInviteSquadGuid = RemoveInvite(charId) match {
      case Some(VacancyInvite(_, _, guid))                => Some(guid)
      case Some(IndirectInvite(_, guid))                  => Some(guid)
      case Some(LookingForSquadRoleInvite(_, _, guid, _)) => Some(guid)
      case Some(ProximityInvite(_, _, guid, _))           => Some(guid)
      case Some(RequestRole(_, guid, _))                  => Some(guid)
      case _                                              => None
    }
    //clean up invites
    val (activeInviteIds, activeSquadGuids) = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (SpontaneousInvite(player), index) if player.CharId == charId              => (index, None)
          case (VacancyInvite(player, _, guid), index) if player == charId                => (index, Some(guid))
          case (IndirectInvite(player, guid), index) if player.CharId == charId           => (index, Some(guid))
          case (LookingForSquadRoleInvite(player, _, guid, _), index) if player == charId => (index, Some(guid))
          case (ProximityInvite(player, _, guid, _), index) if player == charId           => (index, Some(guid))
          case (RequestRole(player, guid, _), index) if player.CharId == charId           => (index, Some(guid))
        }
        .map { case (index, guid) =>
          val key = keys(index)
          RemoveInvite(key)
          (key, guid)
        }
        .unzip
    }
    //tidy the queued invitations
    val (queuedInviteIds, queuedSquadGuids) = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = if(key != charId) {
              queue.partition {
                case SpontaneousInvite(player)                  => player.CharId == charId
                case VacancyInvite(player, _, _)                => player == charId
                case IndirectInvite(player, _)                  => player.CharId == charId
                case LookingForSquadRoleInvite(player, _, _, _) => player == charId
                case ProximityInvite(player, _, _, _)           => player == charId
                case RequestRole(player, _, _)                  => player.CharId == charId
                case _                                          => false
              }
            } else {
              (queue, Nil)
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some((
                key,
                targets.collect {
                  case VacancyInvite(_, _, guid)                => guid
                  case IndirectInvite(_, guid)                  => guid
                  case LookingForSquadRoleInvite(_, _, guid, _) => guid
                  case ProximityInvite(_, _, guid, _)           => guid
                  case RequestRole(_, guid, _)                  => guid
                }
              ))
            } else {
              None
            }
        }
        .flatten
        .toList
        .unzip
    }
    val allInvites = (activeInviteIds ++ queuedInviteIds).toList.distinct
    ((activeSquadGuids.toSeq :+ charIdInviteSquadGuid) ++ queuedSquadGuids)
      .flatten
      .distinct
      .foreach { guid => CleanUpSquadFeatures(allInvites, guid, position = -1) }
  }

  /**
    * Remove all active and inactive proximity squad invites.
    * This is related to recruitment from the perspective of the recruiter.
    * @param charId the player
    */
  def CleanUpAllProximityInvites(charId: Long): Unit = {
    //clean up invites
    val (activeInviteIds, activeSquadGuids) = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect { case (ProximityInvite(player, _, guid, _), index) if player == charId => (index, Some(guid)) }
        .map { case (index, guid) =>
          val key = keys(index)
          RemoveInvite(key)
          (key, guid)
        }
        .unzip
    }
    //tidy the queued invitations
    val (queuedInviteIds, queuedSquadGuids) = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case ProximityInvite(player, _, _, _) => player == charId
              case _                                => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some((key, targets.collect { case ProximityInvite(_, _, guid, _) => guid } ))
            } else {
              None
            }
        }
        .flatten
        .toList
        .unzip
    }
    val allInvites = (activeInviteIds ++ queuedInviteIds).toList.distinct
    (activeSquadGuids.toSeq ++ queuedSquadGuids)
      .flatten
      .distinct
      .foreach { guid => CleanUpSquadFeatures(allInvites, guid, position = -1) }
  }

  /**
    * Remove all active and inactive proximity squad invites for a specific squad.
    * @param squadGuid the squad
    */
  def CleanUpProximityInvites(squadGuid: PlanetSideGUID): Unit = {
    //clean up invites
    val activeInviteIds = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (ProximityInvite(_, _, guid, _), index) if guid == squadGuid => index
        }
        .map { index =>
          val key = keys(index)
          RemoveInvite(key)
          key
        }
    }
    //tidy the queued invitations
    val queuedInviteIds = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case ProximityInvite(_, _, guid, _) => squadGuid == guid
              case _                              => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              keys.lift(index)
            } else {
              None
            }
        }
        .flatten
        .toList
    }
    CleanUpSquadFeatures((activeInviteIds ++ queuedInviteIds).toList.distinct, squadGuid, position = -1)
  }

  /**
    * Remove all active invitation objects
    * that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
    * Affects only certain invitation object types
    * including "player requesting role" and "leader requesting recruiting role".
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `ProximityInvite`
    * @see `RemoveInvite`
    * @param guid the squad identifier
    * @param position the role position index
    * @return the character ids of all players whose invites were removed
    */
  def RemoveActiveInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[Long] = {
    val keys = invites.keys.toSeq
    invites.values.zipWithIndex
      .collect {
        case (LookingForSquadRoleInvite(_, _, sguid, pos), index) if sguid == guid && pos == position => index
        case (ProximityInvite(_, _, sguid, pos), index) if sguid == guid && pos == position           => index
        case (RequestRole(_, sguid, pos), index) if sguid == guid && pos == position                  => index
      }
      .map { index =>
        val key = keys(index)
        RemoveInvite(key)
        key
      }
      .toList
  }

  /**
    * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `CleanUpInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    * @return the character ids of all players whose invites were removed
    */
  def RemoveQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[Long] = {
    val keys = queuedInvites.keys.toSeq
    queuedInvites.values.zipWithIndex
      .collect {
        case (queue, index) =>
          val key = keys(index)
          val (targets, retained) = queue.partition {
            case LookingForSquadRoleInvite(_, _, sguid, pos) => sguid == guid && pos == position
            case ProximityInvite(_, _, sguid, pos)           => sguid == guid && pos == position
            case RequestRole(_, sguid, pos)                  => sguid == guid && pos == position
            case _                                           => false
          }
          if (retained.isEmpty) {
            queuedInvites.remove(key)
          } else {
            queuedInvites += key -> retained
          }
          if (targets.nonEmpty) {
            Some(key)
          } else {
            None
          }
      }
      .flatten
      .toList
  }

  /**
    * Resolve an invitation to a general, not guaranteed, position in someone else's squad.
    * For the moment, just validate the provided parameters and confirm the eligibility of the user.
    * @see `VacancyInvite`
    * @param squad_guid the unique squad identifier number
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param recruit the player being invited
    * @return the squad object and a role position index, if properly invited;
    *         `None`, otherwise
    */
  def HandleVacancyInvite(
                           squad_guid: PlanetSideGUID,
                           invitedPlayer: Long,
                           invitingPlayer: Long,
                           recruit: Player
                         ): Option[(SquadFeatures, Int)] = {
    squadFeatures.get(squad_guid) match {
      case Some(features) =>
        val squad = features.Squad
        GetParticipatingSquad(invitedPlayer) match {
          case Some(enrolledSquad) =>
            if (enrolledSquad eq features) {
              log.warn(s"HandleVacancyInvite: ${recruit.Name} is already a member of squad ${squad.Task}")
            } else {
              log.warn(
                s"HandleVacancyInvite: ${recruit.Name} is a member of squad ${enrolledSquad.Squad.Task} and can not join squad ${squad.Task}"
              )
            }
            None
          case _ =>
            HandleVacancyInvite(features, invitedPlayer, invitingPlayer, recruit)
        }

      case _ =>
        log.warn(s"HandleVacancyInvite: the squad #${squad_guid.guid} no longer exists")
        None
    }
  }

  /**
    * Resolve an invitation to a general, not guaranteed, position in someone else's squad.<br>
    * <br>
    * Originally, the instigating type of invitation object was a "`VacancyInvite`"
    * which indicated a type of undirected invitation extended from the squad leader to another player
    * but the resolution is generalized enough to suffice for a number of invitation objects.
    * First, an actual position is determined;
    * then, the squad is tested for recruitment conditions,
    * including whether the person who solicited the would-be member is still the squad leader.
    * If the recruitment is manual and the squad leader is not the same as the recruiting player,
    * then the real squad leader is sent an indirect query regarding the player's eligibility.
    * These `IndirectInvite` invitation objects also are handled by calls to `HandleVacancyInvite`.
    * @see `AltAddInviteAndRespond`
    * @see `IndirectInvite`
    * @see `SquadFeatures::AutoApproveInvitationRequests`
    * @see `VacancyInvite`
    * @param features the squad
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param recruit the player being invited
    * @return the squad object and a role position index, if properly invited;
    *         `None`, otherwise
    */
  def HandleVacancyInvite(
                           features: SquadFeatures,
                           invitedPlayer: Long,
                           invitingPlayer: Long,
                           recruit: Player
                         ): Option[(SquadFeatures, Int)] = {
    //accepted an invitation to join an existing squad
    val squad = features.Squad
    squad.Membership.zipWithIndex.find({
      case (_, index) =>
        squad.isAvailable(index, recruit.avatar.certifications)
    }) match {
      case Some((_, line)) =>
        //position in squad found
        val sguid    = squad.GUID
        if (!features.AutoApproveInvitationRequests && squad.Leader.CharId != invitingPlayer) {
          //the inviting player was not the squad leader and this decision should be bounced off the squad leader
          AltAddInviteAndRespond(
            squad.Leader.CharId,
            IndirectInvite(recruit, sguid),
            invitingPlayer,
            name = ""
          )
          debug(s"HandleVacancyInvite: ${recruit.Name} must await an invitation from the leader of squad ${squad.Task}")
          None
        } else {
          Some((features, line))
        }
      case _ =>
        None
    }
  }

  /**
    * An overloaded entry point to the functionality for handling one player requesting a specific squad role.
    * @param player the player who wants to join the squad
    * @param bid a specific kind of `Invitation` object
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, bid: RequestRole) : Boolean = {
    HandleRequestRole(player, bid.squad_guid, bid)
  }

  /**
    * An overloaded entry point to the functionality for handling indirection when messaging the squad leader about an invite.
    * @param player the player who wants to join the squad
    * @param bid a specific kind of `Invitation` object
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, bid: IndirectInvite) : Boolean = {
    HandleRequestRole(player, bid.squad_guid, bid)
  }

  /**
    * The functionality for handling indirection
    * for handling one player requesting a specific squad role
    * or when messaging the squad leader about an invite.<br>
    * <br>
    * At this point in the squad join process, the only consent required is that of the squad leader.
    * An automatic consent flag exists on the squad;
    * but, if that is not set, then the squad leader must be asked whether or not to accept or to reject the recruit.
    * If the squad leader changes in the middle or the latter half of the process,
    * the invitation may still fail even if the old squad leader accepts.
    * If the squad leader changes in the middle of the latter half of the process,
    * the inquiry might be posed again of the new squad leader, of whether to accept or to reject the recruit.
    * @param player the player who wants to join the squad
    * @param guid the unique squad identifier number
    * @param bid the `Invitation` object that was the target of this request
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, guid: PlanetSideGUID, bid: Invitation) : Boolean = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        val leaderCharId = features.Squad.Leader.CharId
        subs.Publish(leaderCharId, SquadResponse.WantsSquadPosition(leaderCharId, player.Name))
        true
      case _ =>
        //squad is missing
        log.error(s"Attempted to process ${bid.InviterName}'s bid for a position in a squad that does not exist")
        false
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
    * @see `InitialAssociation`
    * @see `InitSquadDetail`
    * @see `InitWaypoints`
    * @see `Publish`
    * @see `CleanUpAllInvitesWithPlayer`
    * @see `SquadDetail`
    * @see `SquadInfo`
    * @see `SquadPositionDetail`
    * @see `SquadPositionEntry`
    * @see `SquadResponse.Join`
    * @see `StatConverter.Health`
    * @see `UpdateSquadListWhenListed`
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
        if squad.Leader.CharId != charId && squad.isAvailable(position, player.avatar.certifications) =>
        memberToSquad(charId) = squad.GUID
        CleanUpAllInvitesWithPlayer(charId)
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
    * @see `Publish`
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
    * @see `Publish`
    * @see `CleanUpAllInvitesToSquad`
    * @see `SquadDetail`
    * @see `TryResetSquadId`
    * @see `UpdateSquadList`
    * @param squad the squad
    */
  def CloseSquad(squad: Squad): Unit = {
    val guid = squad.GUID
    CleanUpAllInvitesToSquad(guid)
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
          subs.Publish(charId, SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
          subs.Publish(charId, SquadResponse.Detail(PlanetSideGUID(0), completelyBlankSquadDetail))
      }
    UpdateSquadListWhenListed(
      squadFeatures.remove(guid).get.Stop,
      None
    )
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.
    * Essentially, perform the same operations as `CloseSquad`
    * but treat the process as if the squad is being disbanded in terms of messaging.
    * @see `PanicDisbandSquad`
    * @see `Publish`
    * @see `SquadResponse.Membership`
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
    * @see `Publish`
    * @see `SquadResponse.Membership`
    * @param features the squad
    * @param membership the unique character identifier numbers of the other squad members
    * @return if a role/index pair is provided
    */
  def PanicDisbandSquad(features: SquadFeatures, membership: Iterable[Long]): Unit = {
    val squad = features.Squad
    CloseSquad(squad)
    membership.foreach { charId =>
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
    refused.remove(charId)
    subs.MonitorSquadDetails.subtractOne(charId)
    CleanUpAllInvitesWithPlayer(charId)
    val pSquadOpt = GetParticipatingSquad(charId)
    pSquadOpt match {
      //member of the squad; leave the squad
      case Some(features) =>
        val squad = features.Squad
        val size = squad.Size
        subs.UserEvents.remove(charId) match {
          case Some(events) =>
            subs.SquadEvents.unsubscribe(events, s"/${features.ToChannel}/Squad")
          case _ => ;
        }
        GetLeadingSquad(charId, pSquadOpt) match {
          case Some(_) =>
            //leader of a squad; the squad will be disbanded
            PanicDisbandSquad(
              features,
              squad.Membership.collect { case member if member.CharId > 0 && member.CharId != charId => member.CharId }
            )
          case None if size == 2 =>
            //one of the last two members of a squad; the squad will be disbanded
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
      case None =>
        //not a member of any squad; nothing to do here
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
            //Publish(faction, SquadResponse.RemoveFromList(Seq(index)))
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
        if data.faction == faction && SearchForSquadsResults(data, guid, squad).nonEmpty =>
        subs.Publish(charId, result)
        (charId, charId)
    }.unzip._1.toList
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
            if data.faction == faction && SearchForSquadsResults(data, guid, squad).nonEmpty =>
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
    * The base of all objects that exist for the purpose of communicating invitation from one player to the next.
    * @param char_id the inviting player's unique identifier number
    * @param name the inviting player's name
    */
  abstract class Invitation(char_id: Long, name: String) {
    def InviterCharId: Long = char_id
    def InviterName: String = name
  }

  /**
    * Utilized when one player attempts to join an existing squad in a specific role.
    * Accessed by the joining player from the squad detail window.
    * @param player the player who requested the role
    * @param squad_guid the squad with the role
    * @param position the index of the role
    */
  final case class RequestRole(player: Player, squad_guid: PlanetSideGUID, position: Int)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized when one squad member issues an invite for some other player.
    * Accessed by an existing squad member using the "Invite" menu option on another player.
    * @param char_id the unique character identifier of the player who sent the invite
    * @param name the name the player who sent the invite
    * @param squad_guid the squad
    */
  final case class VacancyInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID)
    extends Invitation(char_id, name)

  /**
    * Utilized to redirect an (accepted) invitation request to the proper squad leader.
    * No direct action causes this message.
    * @param player the player who would be joining the squad;
    *               may or may not have actually requested it in the first place
    * @param squad_guid the squad
    */
  final case class IndirectInvite(player: Player, squad_guid: PlanetSideGUID)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out unoccupied squad roles.
    * @param char_id the unique character identifier of the squad leader
    * @param name the name of the squad leader
    * @param squad_guid the squad
    * @param position the index of a role
    */
  final case class ProximityInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID, position: Int)
    extends Invitation(char_id, name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out an unoccupied squad role.
    * @param char_id the unique character identifier of the squad leader
    * @param name the name of the squad leader
    * @param squad_guid the squad with the role
    * @param position the index of the role
    */
  final case class LookingForSquadRoleInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID, position: Int)
    extends Invitation(char_id, name)

  /**
    * Utilized when one player issues an invite for some other player for a squad that does not yet exist.
    * @param player na
    */
  final case class SpontaneousInvite(player: Player) extends Invitation(player.CharId, player.Name)

  /**
    * Move one player into one squad role and,
    * if encountering a player already recruited to the destination role,
    * swap that other player into the first player's position.
    * If no encounter, just blank the original role.
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
