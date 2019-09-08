// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.Player
import net.psforever.objects.definition.converter.StatConverter
import net.psforever.objects.loadouts.SquadLoadout
import net.psforever.objects.teamwork.{Member, Squad}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types._
import services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

//import scala.concurrent.duration._

class SquadService extends Actor {
  import SquadService._

  /**
    * The current unique squad identifier, to be wrapped in a `PlanetSideGUID` object later.
    * The count always starts at 1, even when reset.
    * A squad of `PlanetSideGUID(0)` indicates both a nonexistent squad and the default no-squad for clients.
    */
  private var sid : Int = 1

  private var memberToSquad : mutable.LongMap[Squad] = mutable.LongMap[Squad]()
  private val invites : mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()
  private val queuedInvites : mutable.LongMap[List[Invitation]] = mutable.LongMap[List[Invitation]]()
  /**
    * A placeholder for an absent active invite that has not (yet) been accepted or rejected, equal to the then-current active invite.
    * Created when removing an active invite.
    * Checked when trying to add a new invite (if found, the invite is queued).
    * Cleared when the next queued invite becomes active.
    */
  private val previousInvites : mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  private var squadFeatures : TrieMap[PlanetSideGUID, SquadService.SquadFeatures] = new TrieMap[PlanetSideGUID, SquadService.SquadFeatures]()
  private val publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
    PlanetSideEmpire.TR -> ListBuffer.empty,
    PlanetSideEmpire.NC -> ListBuffer.empty,
    PlanetSideEmpire.VS -> ListBuffer.empty
  )

  private [this] val log = org.log4s.getLogger

  override def preStart : Unit = {
    log.info("Starting...")
  }

  def GetNextSquadId() : PlanetSideGUID = {
    val out = sid
    val j = sid + 1
    if(j == 65536) {
      sid = 1
    }
    else {
      sid = j
    }
    PlanetSideGUID(out)
  }

  def TryResetSquadId() : Boolean = {
    if(squadFeatures.isEmpty) {
      sid = 1
      true
    }
    else {
      false
    }
  }

  def GetSquad(id : PlanetSideGUID) : Option[Squad] = {
    squadFeatures.get(id) match {
      case Some(features) => Some(features.Squad)
      case None => None
    }
  }

  def GetParticipatingSquad(player : Player) : Option[Squad] = {
    GetParticipatingSquad(player.CharId)
  }

  def GetParticipatingSquad(charId : Long) : Option[Squad] = {
    memberToSquad.get(charId) match {
      case opt @ Some(_) =>
        opt
      case None =>
        None
    }
  }

  def GetLeadingSquad(player : Player, opt : Option[Squad]) : Option[Squad] = {
    val charId = player.CharId
    opt match {
      case Some(squad) =>
        if(squad.Leader.CharId == charId) {
          Some(squad)
        }
        else {
          None
        }

      case None =>
        memberToSquad.get(charId) match {
          case Some(squad) if squad.Leader.CharId == charId =>
            Some(squad)
          case _ =>
            None
        }
    }
  }

  def GetLeadingSquad(charId : Long, opt : Option[Squad]) : Option[Squad] = {
    opt.orElse(memberToSquad.get(charId)) match {
      case Some(squad) =>
        if(squad.Leader.CharId == charId) {
          Some(squad)
        }
        else {
          None
        }
      case _ =>
        None
    }
  }

  def CreateSquad(player : Player) : Squad = {
    val faction = player.Faction
    val name = player.Name
    val squad = new Squad(GetNextSquadId(), faction)
    val leadPosition = squad.Membership(squad.LeaderPositionIndex)
    leadPosition.Name = name
    leadPosition.CharId = player.CharId
    leadPosition.Health = player.Health
    leadPosition.Armor = player.Armor
    leadPosition.Position = player.Position
    leadPosition.ZoneId = 1
    log.info(s"$name-$faction has created a new squad")
    squad
  }

  def StartSquad(squad : Squad) : Squad = {
    squadFeatures += squad.GUID -> new SquadService.SquadFeatures(squad).Start
    memberToSquad += squad.Leader.CharId -> squad
    squad
  }

  def StartSquad(player : Player) : Squad = {
    val squad = CreateSquad(player)
    StartSquad(squad)
    squad
  }

  val SquadEvents = new GenericEventBus[SquadServiceResponse]

  def receive : Receive = {
    //subscribe to a faction's channel - necessary to receive updates about listed squads
    case Service.Join(faction) if "TRNCVS".indexOf(faction) > -1 =>
      val path = s"/$faction/Squad"
      val who = sender()
      log.info(s"$who has joined $path")
      SquadEvents.subscribe(who, path)
      //send initial squad catalog
      sender ! SquadServiceResponse(s"/$faction/Squad", SquadResponse.InitList(publishedLists(PlanetSideEmpire(faction)).toVector))

    //subscribe to the player's personal channel - necessary for future and previous squad information
    case Service.Join(char_id) =>
      val path = s"/$char_id/Squad"
      val who = sender()
      log.info(s"$who has joined $path")
      SquadEvents.subscribe(who, path)
      //check for renewable squad information
      val longCharId = char_id.toLong
      memberToSquad.get(longCharId) match {
        case None => ;
        case Some(squad) =>
          val guid = squad.GUID
          val indices = squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
          SquadEvents.publish(SquadServiceResponse(s"/$char_id/Squad", SquadResponse.AssociateWithSquad(guid)))
          SquadEvents.publish(SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Join(squad, indices)))
          InitSquadDetail(guid, Seq(longCharId), squad)
          InitWaypoints(longCharId, guid)
      }

    case Service.Leave(Some(char_id)) =>
      SquadEvents.unsubscribe(sender())
      val longCharId = char_id.toLong
      val pSquadOpt = GetParticipatingSquad(longCharId)
      (pSquadOpt, GetLeadingSquad(longCharId, pSquadOpt)) match {
        case (Some(_), Some(squad)) =>
          //leader of a squad; the squad will be disbanded
          DisbandSquad(squad)
        case (Some(squad), None) if squad.Size == 2 =>
          //one of the last two members of a squad; the squad will be disbanded
          DisbandSquad(squad)
        case (Some(squad), None) =>
          //member of the squad; leave the squad
          LeaveSquad(longCharId, squad)
        case _ =>
          //not a member of any squad; nothing to do here
      }
      CleanupInvitesFromPlayer(longCharId)

    case Service.Leave(None) | Service.LeaveAll() => ;

    case SquadServiceMessage(tplayer, zone, squad_action) => squad_action match {
      case SquadAction.Membership(SquadRequestType.Invite, invitingPlayer, Some(invitedPlayer), _, _) =>
        //this is just busy work; for actual joining operations, see SquadRequestType.Accept
        (memberToSquad.get(invitingPlayer), memberToSquad.get(invitedPlayer)) match {
          case (Some(squad1), Some(squad2))
            if squad1.GUID == squad2.GUID =>
            //both players are in the same squad; no need to do anything

          case (Some(squad1), Some(squad2))
            if squad1.Leader.CharId == invitingPlayer && squad2.Leader.CharId == invitedPlayer &&
              squad1.Size > 1 && squad2.Size > 1 =>
            //we might do some platoon chicanery with this case later
            //TODO platoons

          case (Some(squad1), Some(squad2))
            if squad2.Size == 1 && !squadFeatures(squad1.GUID).Refuse.contains(invitedPlayer) =>
            //both players belong to squads, but the invitedplayer's squad (squad2) is underutilized by comparison
            //treat the same as "the classic situation" using squad1
            log.info(s"$invitedPlayer has been invited to squad ${squad1.Task} by $invitingPlayer")
            val charId = tplayer.CharId
            AddInviteAndRespond(
              invitedPlayer,
              VacancyInvite(charId, tplayer.Name, squad1.GUID),
              charId,
              tplayer.Name
            )

          case (Some(squad1), Some(squad2))
            if squad1.Size == 1 && !squadFeatures(squad2.GUID).Refuse.contains(invitingPlayer) =>
            //both players belong to squads, but the invitingPlayer's squad is underutilized by comparison
            //treat the same as "indirection ..." using squad2
            log.warn(s"$invitedPlayer has asked $invitingPlayer for an invitation to squad ${squad2.Task}, but the squad leader may need to approve")
            AddInviteAndRespond(
              squad2.Leader.CharId,
              IndirectInvite(tplayer, squad2.GUID),
              invitingPlayer,
              tplayer.Name
            )

          case (Some(squad), None)
            if !squadFeatures(squad.GUID).Refuse.contains(invitedPlayer) =>
            //the classic situation
            log.info(s"$invitedPlayer has been invited to squad ${squad.Task} by $invitingPlayer")
            AddInviteAndRespond(
              invitedPlayer,
              VacancyInvite(tplayer.CharId, tplayer.Name, squad.GUID),
              invitingPlayer,
              tplayer.Name
            )

          case (None, Some(squad))
            if !squadFeatures(squad.GUID).Refuse.contains(invitingPlayer) =>
            //indirection;  we're trying to invite ourselves to someone else's squad
            log.warn(s"$invitedPlayer has asked $invitingPlayer for an invitation to squad ${squad.Task}, but the squad leader may need to approve")
            AddInviteAndRespond(
              squad.Leader.CharId,
              IndirectInvite(tplayer, squad.GUID),
              invitingPlayer,
              tplayer.Name
            )

          case (None, None) =>
            //neither the invited player nor the inviting player belong to any squad
            log.info(s"$invitedPlayer has been invited to join $invitingPlayer's spontaneous squad")
            AddInviteAndRespond(
              invitedPlayer,
              SpontaneousInvite(tplayer),
              invitingPlayer,
              tplayer.Name
            )

          case _ => ;
        }

      case SquadAction.Membership(SquadRequestType.ProximityInvite, invitingPlayer, _, _, _) =>
        memberToSquad.get(invitingPlayer) match {
          case Some(squad) =>
            val sguid = squad.GUID
            val features = squadFeatures(sguid)
            features.SearchForRole match {
              case Some(-1) =>
                //we've already issued a proximity invitation; no need to do another
                log.info("ProximityInvite: waiting for existing proximity invitations to clear")
              case _ =>
                log.info("ProximityInvite: looking for invitation targets ...")
                val outstandingActiveInvites = features.SearchForRole match {
                  case Some(pos) =>
                    RemoveQueuedInvitesForSquadAndPosition(sguid, pos)
                    invites.collect { case(charId, InviteForRole(_,_, squad_guid, role)) if squad_guid == sguid && role == pos => charId }
                  case None =>
                    List.empty[Long]
                }
                features.SearchForRole = Some(-1)
                val faction = squad.Faction
                val center = tplayer.Position
                val excusedInvites = features.Refuse
                //positions that can be recruited to
                val positions = squad.Membership.zipWithIndex
                  .collect { case(member, index) if member.CharId == 0 && squad.Availability(index) => member }
                /*
                players who are:
                - the same faction as the squad
                - have Looking For Squad enabled
                - do not currently belong to a squad
                - are denied the opportunity to be invited
                - are a certain distance from the squad leader
                 */
                (zone.LivePlayers
                  .collect { case player
                    if player.Faction == faction && player.LFS && memberToSquad.get(player.CharId).isEmpty &&
                      !excusedInvites.contains(player.CharId) &&
                      Vector3.DistanceSquared(player.Position, center) < 100f &&
                      {
                        positions
                          .map { role =>
                            val requirementsToMeet = role.Requirements
                            requirementsToMeet.intersect(player.Certifications) == requirementsToMeet
                          }
                          .foldLeft(false)(_ || _)
                      } => player.CharId
                  }
                  .partition { charId => outstandingActiveInvites.exists(_ == charId) } match {
                  case (Nil, Nil) =>
                    //no one found
                    log.info("ProximityInvite: no invitation targets found")
                    outstandingActiveInvites foreach RemoveInvite
                    features.ProxyInvites = Nil
                    None
                  case (outstandingPlayerList, invitedPlayerList) =>
                    //players who were actively invited for the previous position and are eligible for the new position
                    log.info(s"ProximityInvite: found ${outstandingPlayerList.size} players already having been invited, and ${invitedPlayerList.size} players to invite")
                    outstandingPlayerList.foreach { charId =>
                      val bid = invites(charId).asInstanceOf[InviteForRole]
                      invites(charId) = ProximityInvite(bid.char_id, bid.name, sguid)
                    }
                    //players who were actively invited for the previous position but are ineligible for the new position
                    (features.ProxyInvites filterNot (outstandingPlayerList contains)) foreach RemoveInvite
                    features.ProxyInvites = outstandingPlayerList ++ invitedPlayerList
                    Some(invitedPlayerList)
                }) match {
                  //add invitations for position in squad
                  case Some(invitedPlayers) =>
                    val invitingPlayer = tplayer.CharId
                    val name = tplayer.Name
                    invitedPlayers.foreach { invitedPlayer =>
                      AddInviteAndRespond(
                        invitedPlayer,
                        ProximityInvite(invitingPlayer, name, sguid),
                        invitingPlayer,
                        name
                      )
                    }
                  case None => ;
                }
            }

          case None =>
        }

      case SquadAction.Membership(SquadRequestType.Accept, invitedPlayer, _, _, _) =>
        val acceptedInvite = RemoveInvite(invitedPlayer)
        val msg = "Accept: the invited player is already a member of a squad and can not join a second one"
        acceptedInvite match {
          case Some(BidForRole(petitioner, guid, position)) if EnsureEmptySquad(petitioner.CharId, msg) && squadFeatures.get(guid).nonEmpty =>
            //player requested to join a squad's specific position
            //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
            val features = squadFeatures(guid)
            JoinSquad(petitioner, features.Squad, position)
            RemoveInvitesForSquadAndPosition(guid, position)

          case Some(IndirectInvite(recruit, guid)) if EnsureEmptySquad(recruit.CharId, msg) =>
            //tplayer / invitedPlayer is actually the squad leader
            val recruitCharId = recruit.CharId
            HandleVacancyInvite(guid, recruitCharId, invitedPlayer, recruit) match {
              case Some((squad, line)) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(recruitCharId), recruit.Name, true, Some(None))))
                JoinSquad(recruit, squad, line)
                RemoveInvitesForSquadAndPosition(squad.GUID, line)
              //since we are the squad leader, we do not want to brush off our queued squad invite tasks
              case _ => ;
            }

          case Some(VacancyInvite(invitingPlayer, _, guid)) if EnsureEmptySquad(invitedPlayer, msg) =>
            //accepted an invitation to join an existing squad
            HandleVacancyInvite(guid, invitedPlayer, invitingPlayer, tplayer) match {
              case Some((squad, line)) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, false, Some(None))))
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayer), "", true, Some(None))))
                JoinSquad(tplayer, squad, line)
                RemoveQueuedInvites(invitedPlayer) //TODO deal with these somehow
                RemoveInvitesForSquadAndPosition(squad.GUID, line)
              case _ => ;
            }

          case Some(SpontaneousInvite(invitingPlayer)) if EnsureEmptySquad(invitedPlayer, msg) =>
            //originally, we were invited by someone into a new squad they would form
            val invitingPlayerCharId = invitingPlayer.CharId
            (GetParticipatingSquad(invitingPlayer) match {
              case Some(participating) =>
                //invitingPlayer became part of a squad while invited player was answering the original summons
                Some(participating)
              case _ =>
                //generate a new squad, with invitingPlayer as the leader
                val squad = StartSquad(invitingPlayer)
                squad.Task = s"${invitingPlayer.Name}'s Squad"
                SquadEvents.publish( SquadServiceResponse(s"/$invitingPlayerCharId/Squad", SquadResponse.AssociateWithSquad(squad.GUID)) )
                Some(squad)
            }) match {
              case Some(squad) =>
                HandleVacancyInvite(squad.GUID, tplayer.CharId, invitingPlayerCharId, tplayer) match {
                  case Some((_, line)) =>
                    SquadEvents.publish( SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayerCharId), "", true, Some(None))) )
                    SquadEvents.publish( SquadServiceResponse(s"/$invitingPlayerCharId/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayerCharId, Some(invitedPlayer), tplayer.Name, false, Some(None))) )
                    JoinSquad(tplayer, squad, line)
                    RemoveQueuedInvites(tplayer.CharId) //TODO deal with these somehow
                  case _ => ;
                }
              case _ => ;
            }

          case Some(InviteForRole(invitingPlayer, name, guid, position)) if EnsureEmptySquad(invitedPlayer, msg) =>
            squadFeatures.get(guid) match {
              case Some(features) if JoinSquad(tplayer, features.Squad, position) =>
                //join this squad
                SquadEvents.publish( SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayer), "", true, Some(None))) )
                SquadEvents.publish( SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, false, Some(None))) )
                RemoveQueuedInvites(tplayer.CharId)
                features.ProxyInvites = Nil
                features.SearchForRole = None
                RemoveInvitesForSquadAndPosition(guid, position)

              case Some(features) =>
                //can not join squad; position is unavailable or other reasons block action
                features.ProxyInvites = features.ProxyInvites.filterNot(_ == invitedPlayer)
                NextInviteAndRespond(invitedPlayer)

              case _ =>
                //squad no longer exists?
                NextInviteAndRespond(invitedPlayer)
            }

          case Some(ProximityInvite(invitingPlayer, _, guid)) if EnsureEmptySquad(invitedPlayer, msg) =>
            squadFeatures.get(guid) match {
              case Some(features) =>
                val squad = features.Squad
                if(squad.Size < squad.Capacity) {
                  val positions = squad.Membership.zipWithIndex
                    .collect { case (member, index) if member.CharId == 0 && squad.Availability(index) && {
                      val requirementsToMeet = member.Requirements
                      requirementsToMeet.intersect(tplayer.Certifications) == requirementsToMeet
                    } =>
                      (index, member.Requirements.size)
                    }
                    .sortBy({ case (_, requirements) => requirements })
                  ((positions.headOption, positions.lastOption) match {
                    case (Some((first, size1)), Some((_, size2))) if size1 == size2 => Some(first) //join the first available position
                    case (Some(_), Some((last, _))) => Some(last) //join the most demanding position
                    case _ => None //(None, None)
                  }) match {
                    case Some(position) if JoinSquad(tplayer, squad, position) =>
                      //join this squad
                      SquadEvents.publish( SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayer), "", true, Some(None))) )
                      SquadEvents.publish( SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, false, Some(None))) )
                      RemoveQueuedInvites(invitedPlayer)
                      features.ProxyInvites = features.ProxyInvites.filterNot(_ == invitedPlayer)
                    case _ =>
                  }
                }
                if(features.ProxyInvites.isEmpty) {
                  //all invitations exhausted; this invitation period is concluded
                  features.SearchForRole = None
                }
                else if(squad.Size == squad.Capacity) {
                  //all available squad positions filled; terminate all remaining invitations
                  features.SearchForRole = None
                  features.ProxyInvites = Nil
                  CleanupInvitesForSquad(guid)
                  //CleanupInvitesFromPlayer(invitingPlayer)
                }

              case _ =>
                //squad no longer exists?
                NextInviteAndRespond(invitedPlayer)
            }

          case _ =>
            //the invite either timed-out or was withdrawn or is now invalid; select a new one?
            NextInviteAndRespond(invitedPlayer)
        }

      case SquadAction.Membership(SquadRequestType.Leave, leavingPlayer, optionalPlayer, _, _) =>
        val squad = memberToSquad(leavingPlayer)
        val leader = squad.Leader.CharId
        if(leavingPlayer == leader || squad.Size == 2) {
          //squad leader is leaving his own squad, so it will be disbanded
          //alternately, squad is only composed of two people, so it will be closed-out when one of them leaves
          DisbandSquad(squad)
        }
        else {
          if(optionalPlayer.contains(leavingPlayer)) {
            //leaving the squad of own accord
            LeaveSquad(tplayer.CharId, squad)
          }
          else if(optionalPlayer.contains(leader)) {
            //kicked by the squad leader
            SquadEvents.publish( SquadServiceResponse(s"/$leavingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, leavingPlayer, Some(leader), tplayer.Name, false, Some(None))) )
            SquadEvents.publish( SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, leader, Some(leavingPlayer), "", true, Some(None))) )
            squadFeatures(squad.GUID).Refuse = leavingPlayer
            LeaveSquad(leavingPlayer, squad)
          }
        }

      case SquadAction.Membership(SquadRequestType.Reject, rejectingPlayer, _, _, _) =>
        val rejectedBid = RemoveInvite(rejectingPlayer)
        //(A, B) -> person who made the rejection, person who was rejected
        (rejectedBid match {
          case Some(SpontaneousInvite(invitingPlayer)) =>
            //rejectingPlayer is the would-be squad member
            (Some(rejectingPlayer), Some(invitingPlayer.CharId))
          case Some(VacancyInvite(invitingPlayer, _, guid))
            if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
            //rejectingPlayer is the would-be squad member
            (Some(rejectingPlayer), Some(invitingPlayer))
          case Some(BidForRole(_, guid, _))
            if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId == rejectingPlayer =>
            //rejectingPlayer is the squad leader
            (Some(rejectingPlayer), None)
          case Some(InviteForRole(invitingPlayer, _, guid, position))
            if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
            //rejectingPlayer is the would-be squad member
            val features = squadFeatures(guid)
            features.Refuse = rejectingPlayer //do not bother this player anymore
            features.ProxyInvites = features.ProxyInvites.filterNot(_ == rejectingPlayer)
            if(features.ProxyInvites.isEmpty) {
              features.SearchForRole = None
            }
            (None, None)
          case Some(ProximityInvite(_, _, guid))
            if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
            //rejectingPlayer is the would-be squad member
            val features = squadFeatures(guid)
            features.Refuse = rejectingPlayer //do not bother this player anymore
            features.ProxyInvites = features.ProxyInvites.filterNot(_ == rejectingPlayer)
            if(features.ProxyInvites.isEmpty) {
              //all invitations exhausted; this invitation is concluded
              features.SearchForRole = None
            }
            (None, None)
          case _ => ;
            (None, None)
        }) match {
          case (Some(rejected), Some(invited)) =>
            SquadEvents.publish( SquadServiceResponse(s"/$rejected/Squad", SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(invited), "", true, Some(None))) )
            SquadEvents.publish( SquadServiceResponse(s"/$invited/Squad", SquadResponse.Membership(SquadResponseType.Reject, 0, 0, invited, Some(rejected), tplayer.Name, false, Some(None))) )
          case (Some(rejected), None) =>
            SquadEvents.publish( SquadServiceResponse(s"/$rejected/Squad", SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(rejected), "", true, Some(None))) )
          case _ => ;
        }
        NextInviteAndRespond(rejectingPlayer)

      case SquadAction.Membership(SquadRequestType.Cancel, cancellingPlayer, _, _, _) =>
        //huh?
        log.warn(s"Huh? what does player $cancellingPlayer want to cancel?")

      case SquadAction.Membership(SquadRequestType.Promote, promotingPlayer, Some(promotedPlayer), _, _) =>
        (memberToSquad.get(promotingPlayer), memberToSquad.get(promotedPlayer)) match {
          case (Some(squad), Some(squad2)) if squad.GUID == squad2.GUID && squad.Leader.CharId == promotingPlayer =>
            val membership = squad.Membership.filter { _member => _member.CharId > 0 }
            val (leader, position) = (squad.Leader, 0)
            val (member, index) = membership.zipWithIndex.find { case (_member, _) => _member.CharId == promotedPlayer }.get
            log.info(s"Player ${leader.Name} steps down from leading ${squad.Task}")
            SwapMemberPosition(squad, leader, member)
            //move around invites so that the proper squad leader deals with them
            val leaderInvite = invites.remove(promotingPlayer)
            val leaderQueuedInvites = queuedInvites.remove(promotingPlayer).toList.flatten
            invites.get(promotedPlayer).orElse(previousInvites.get(promotedPlayer)) match {
              case Some(_) =>
                //the promoted player has an active invite; queue these
                queuedInvites += promotedPlayer -> (leaderInvite.toList ++ leaderQueuedInvites ++ queuedInvites.remove(promotedPlayer).toList.flatten)
              case None if leaderInvite.nonEmpty =>
                //no active invite for the promoted player, but the leader had an active invite; trade the queued invites
                val invitation = leaderInvite.get
                AddInviteAndRespond(promotedPlayer, invitation, invitation.InviterCharId, invitation.InviterName)
                queuedInvites += promotedPlayer -> (leaderQueuedInvites ++ queuedInvites.remove(promotedPlayer).toList.flatten)
              case None =>
                //no active invites for anyone; assign the first queued invite from the promoting player, if available, and queue the rest
                leaderQueuedInvites match {
                  case Nil => ;
                  case x :: xs =>
                    AddInviteAndRespond(promotedPlayer, x, x.InviterCharId, x.InviterName)
                    queuedInvites += promotedPlayer -> (xs ++ queuedInvites.remove(promotedPlayer).toList.flatten)
                }
            }
            log.info(s"Promoting player ${leader.Name} to be the leader of ${squad.Task}")
            membership.foreach { _member =>
              SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.PromoteMember(squad, promotedPlayer, index, position)))
            }
            SquadEvents.publish(SquadServiceResponse(s"/$promotingPlayer/Squad", SquadResponse.AssociateWithSquad(PlanetSideGUID(0))))
            SquadEvents.publish(SquadServiceResponse(s"/$promotedPlayer/Squad", SquadResponse.AssociateWithSquad(squad.GUID)))
            UpdateSquadListWhenListed(
              squad,
              SquadInfo().Leader(leader.Name)
            )
            UpdateSquadDetail(squad.GUID, squad,
              SquadDetail()
                .LeaderCharId(leader.CharId)
                .Field3(value = 0L)
                .LeaderName(leader.Name)
                .Members(List(
                  SquadPositionEntry(position, SquadPositionDetail().CharId(member.CharId).Name(member.Name)),
                  SquadPositionEntry(index, SquadPositionDetail().CharId(leader.CharId).Name(leader.Name))
                ))
            )

          case msg =>
            log.warn(s"Unsupported squad behavior: $msg")
        }

      case SquadAction.Waypoint(_, wtype, _, info) =>
        val playerCharId = tplayer.CharId
        (GetLeadingSquad(tplayer, None) match {
          case Some(squad) =>
            info match {
              case Some(winfo) =>
                (Some(squad), AddWaypoint(squad.GUID, wtype, winfo))
              case _ =>
                RemoveWaypoint(squad.GUID, wtype)
                (Some(squad), None)
            }
          case _ => (None, None)
        }) match {
          case (Some(squad), Some(_)) =>
            //waypoint added or updated
            squad.Membership
              .filterNot { member => member.CharId == tplayer.CharId }
              .foreach { member =>
                val charId = member.CharId
                SquadEvents.publish(
                  SquadServiceResponse(s"/$charId/Squad", SquadResponse.WaypointEvent(WaypointEventAction.Add, playerCharId, wtype, None, info, 1))
                )
              }

          case (Some(squad), None) =>
            //waypoint removed?
            squad.Membership
              .filterNot { member => member.CharId == tplayer.CharId }
              .foreach { member =>
                val charId = member.CharId
                SquadEvents.publish(
                  SquadServiceResponse(s"/$charId/Squad", SquadResponse.WaypointEvent(WaypointEventAction.Remove, playerCharId, wtype, None, None, 0))
                )
              }

          case msg =>
            log.warn(s"Unsupported squad waypoint behavior: $msg")
        }

      case SquadAction.Definition(guid, line, action) =>
        import net.psforever.packet.game.SquadAction._
        val pSquadOpt = GetParticipatingSquad(tplayer)
        val lSquadOpt = GetLeadingSquad(tplayer, pSquadOpt)
        //the following actions can only be performed by a squad's leader
        action match {
          case SaveSquadFavorite() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            if(squad.Task.nonEmpty && squad.ZoneId > 0) {
              tplayer.SquadLoadouts.SaveLoadout(squad, squad.Task, line)
              sender ! SquadServiceResponse("", SquadResponse.ListSquadFavorite(line, squad.Task))
            }

          case LoadSquadFavorite() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            tplayer.SquadLoadouts.LoadLoadout(line) match {
              case Some(loadout : SquadLoadout) if squad.Size == 1 =>
                log.info(s"${tplayer.Name} is loading a squad composition: $loadout")
                SquadService.LoadSquadDefinition(squad, loadout)
                sender ! SquadServiceResponse("", SquadResponse.AssociateWithSquad(squad.GUID))
                UpdateSquadList(squad, SquadService.SquadList.Publish(squad))
                UpdateSquadDetail(PlanetSideGUID(0), squad)
              case _ =>
            }

          case DeleteSquadFavorite() =>
            tplayer.SquadLoadouts.DeleteLoadout(line)
            sender ! SquadServiceResponse("", SquadResponse.ListSquadFavorite(line, ""))

          case ChangeSquadPurpose(purpose) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed his squad's task to $purpose")
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Task = purpose
            UpdateSquadListWhenListed(squad, SquadInfo().Task(purpose))
            UpdateSquadDetail(squad.GUID, squad, SquadDetail().Task(purpose))

          case ChangeSquadZone(zone_id) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed squad's ops zone to $zone_id")
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.ZoneId = zone_id.zoneId.toInt
            UpdateSquadListWhenListed(squad, SquadInfo().ZoneId(zone_id))
            InitialAssociation(squad)
            sender ! SquadServiceResponse("", SquadResponse.Detail(
              squad.GUID,
              SquadService.Detail.Publish(squad))
            )
            UpdateSquadDetail(squad.GUID, squad.Membership.map { _m => _m.CharId }.filterNot { _ == squad.Leader.CharId }, SquadDetail().ZoneId(zone_id))

          case CloseSquadMemberPosition(position) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(true) =>
                squad.Availability.update(position, false)
                log.info(s"${tplayer.Name}-${tplayer.Faction} has closed the #$position position in squad")
                val memberPosition = squad.Membership(position)
                if(memberPosition.CharId > 0) {
                  LeaveSquad(memberPosition.CharId, squad)
                }
                memberPosition.Close()
                UpdateSquadListWhenListed(squad, SquadInfo().Capacity(squad.Capacity))
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Closed)))
                )
              case Some(false) | None => ;
            }

          case AddSquadMemberPosition(position) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(false) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has opened the #$position position in squad")
                squad.Availability.update(position, true)
                UpdateSquadListWhenListed(squad, SquadInfo().Capacity(squad.Capacity))
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Open)))
                )
              case Some(true) | None => ;
            }

          case ChangeSquadMemberRequirementsRole(position, role) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the role of squad position #$position")
                squad.Membership(position).Role = role
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Role(role))))
                )
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the orders for squad position #$position")
                squad.Membership(position).Orders = orders
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().DetailedOrders(orders))))
                )
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsCertifications(position, certs) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the requirements for squad position #$position")
                squad.Membership(position).Requirements = certs
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Requirements(certs))))
                )
              case Some(false) | None => ;
            }

          case LocationFollowsSquadLead(state) =>
            if(state) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has moves the rally to the leader's position")
            }
            else {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has let the rally move freely")
            }
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.LocationFollowsSquadLead = state

          case AutoApproveInvitationRequests(state) =>
            if(state) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} is allowing all requests to join the squad")
            }
            else {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has started screening invitation requests")
            }
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.AutoApproveInvitationRequests = state

          case FindLfsSoldiersForRole(position) =>
            lSquadOpt match {
              case Some(squad) =>
                val sguid = squad.GUID
                val features = squadFeatures(sguid)
                features.SearchForRole match {
                  case Some(-1) =>
                    //a proximity invitation has not yet cleared; nothing will be gained by trying to invite for a specific role
                    log.debug("FindLfsSoldiersForRole: waiting for proximity invitations to clear")
                  case _ =>
                    //either no role has ever been recruited, or some other role has been recruited
                    //normal LFS recruitment for the given position
                    val excusedInvites = features.Refuse
                    val faction = squad.Faction
                    val requirementsToMeet = squad.Membership(position).Requirements
                    val outstandingActiveInvites = features.SearchForRole match {
                      case Some(pos) =>
                        RemoveQueuedInvitesForSquadAndPosition(sguid, pos)
                        invites.collect { case(charId, InviteForRole(_,_, squad_guid, role)) if squad_guid == sguid && role == pos => charId }
                      case None =>
                        List.empty[Long]
                    }
                    features.SearchForRole = position
                    //this will update the role entry in the GUI to visually indicate being searched for; only one will be displayed at a time
                    SquadEvents.publish(
                      SquadServiceResponse(s"/${tplayer.CharId}/Squad", SquadResponse.Detail(
                        sguid,
                        SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().CharId(char_id = 0L).Name(name = ""))))
                      ))
                    )
                    //collect all players that are eligible for invitation to the new position
                    //divide into players with an active invite (A) and players with a queued invite (B)
                    //further filter (A) into players whose invitation is renewed (A1) and new invitations (A2)
                    //TODO only checks the leader's current zone; should check all zones
                    (zone.LivePlayers
                      .collect { case player
                        if !excusedInvites.contains(player.CharId) &&
                          faction == player.Faction && player.LFS && memberToSquad.get(player.CharId).isEmpty &&
                          requirementsToMeet.intersect(player.Certifications) == requirementsToMeet =>
                        player.CharId
                      }
                      .partition { charId => outstandingActiveInvites.exists(charId == _) } match {
                      case (Nil, Nil) =>
                        outstandingActiveInvites foreach RemoveInvite
                        features.ProxyInvites = Nil
                        //TODO cancel the LFS search from the server so that the client updates properly; how?
                        None
                      case (outstandingPlayerList, invitedPlayerList) =>
                        //players who were actively invited for the previous position and are eligible for the new position
                        outstandingPlayerList.foreach { charId =>
                          val bid = invites(charId).asInstanceOf[InviteForRole]
                          invites(charId) = InviteForRole(bid.char_id, bid.name, sguid, position)
                        }
                        //players who were actively invited for the previous position but are ineligible for the new position
                        (features.ProxyInvites filterNot (outstandingPlayerList contains)) foreach RemoveInvite
                        features.ProxyInvites = outstandingPlayerList ++ invitedPlayerList
                        Some(invitedPlayerList)
                    }) match {
                      //add invitations for position in squad
                      case Some(invitedPlayers) =>
                        val invitingPlayer = tplayer.CharId
                        val name = tplayer.Name
                        invitedPlayers.foreach { invitedPlayer =>
                          AddInviteAndRespond(
                            invitedPlayer,
                            InviteForRole(invitingPlayer, name, sguid, position),
                            invitingPlayer,
                            name
                          )
                        }
                      case None => ;
                    }
                }

              case _ => ;
            }

          case CancelFind() =>
            lSquadOpt match {
              case Some(squad) =>
                val sguid = squad.GUID
                val position = squadFeatures(sguid).SearchForRole
                squadFeatures(sguid).SearchForRole = None
                //remove active invites
                invites.filter {
                  case (_, InviteForRole(_, _, _guid, pos)) => _guid == sguid && position.contains(pos)
                  case _ => false
                }
                  .keys.foreach { charId =>
                  RemoveInvite(charId)
                }
                //remove queued invites
                queuedInvites.foreach { case (charId, queue) =>
                  val filtered = queue.filterNot {
                    case InviteForRole(_, _, _guid, _) => _guid == sguid
                    case _ => false
                  }
                  queuedInvites += charId -> filtered
                  if(filtered.isEmpty) {
                    queuedInvites.remove(charId)
                  }
                }
                //remove yet-to-be invitedPlayers
                squadFeatures(sguid).ProxyInvites = Nil
              case _ => ;
            }

          case RequestListSquad() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            if(!squad.Listed && squad.Task.nonEmpty && squad.ZoneId > 0) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has opened public recruitment for squad ${squad.Task}")
              squad.Listed = true
              InitialAssociation(squad)
              sender ! SquadServiceResponse("", SquadResponse.SetListSquad(squad.GUID))
              UpdateSquadList(squad, None)
            }

          case StopListSquad() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            if(squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has closed public recruitment for squad ${squad.Task}")
              squad.Listed = false
              sender ! SquadServiceResponse("", SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
              UpdateSquadList(squad, None)
            }

          case ResetAll() =>
            lSquadOpt match {
              case Some(squad) if squad.Size > 1 =>
                val guid = squad.GUID
                squad.Task = ""
                squad.ZoneId = None
                squad.Availability.indices.foreach { i =>
                  squad.Availability.update(i, true)
                }
                squad.Membership.foreach(position => {
                  position.Role = ""
                  position.Orders = ""
                  position.Requirements = Set()
                })
                squad.LocationFollowsSquadLead = false
                squad.AutoApproveInvitationRequests = false
                UpdateSquadListWhenListed(squad, SquadInfo().Task("").ZoneId(None).Capacity(squad.Capacity))
                UpdateSquadDetail(guid, squad)
                sender ! SquadServiceResponse("", SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
                squadFeatures(guid).InitialAssociation = true
                //do not unlist an already listed squad
              case Some(squad) =>
                //underutilized squad; just close it out
                CloseSquad(squad)
              case _ => ;
            }

          case _ =>
            (pSquadOpt, action) match {
              //the following action can be performed by the squad leader and maybe an unaffiliated player
              case (Some(squad), SelectRoleForYourself(position)) =>
                log.info(s"${tplayer.Name} would like the #${position+1} spot in the same squad")
                val membership = squad.Membership.zipWithIndex
                val toMember = squad.Membership(position)
                if(squad.Leader.CharId == tplayer.CharId) {
                  //TODO squad leader currently disallowed
                } else
                //the squad leader may swap to any open position; a normal member has to validate against requirements
                if((squad.Leader.CharId == tplayer.CharId && toMember.CharId == 0) || ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
                  membership.find { case (member, _) => member.CharId == tplayer.CharId } match {
                    case Some((fromMember, fromIndex)) =>
                      SwapMemberPosition(squad, toMember, fromMember)
                      if(fromIndex == squad.LeaderPositionIndex) {
                        squad.LeaderPositionIndex = position
                      }
                      //RemoveInvite(tplayer.CharId).foreach { _ =>
                      //close the old bids out
                      //}
                      membership
                        .filter { case (_member, _) => _member.CharId > 0 }
                        .foreach { case (_member, _) =>
                          SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromIndex, position)))
                        }
                      UpdateSquadDetail(squad.GUID, squad)
                    case _ => ;
                    //somehow, this is not our squad; do nothing, for now
                  }
                }
                else {
                  //not qualified for requested position
                }

              //the following action can be performed by an unaffiliated player
              case (None, SelectRoleForYourself(position)) =>
                //not a member of any squad, but we might become a member of this one
                GetSquad(guid) match {
                  case Some(squad) =>
                    val toMember = squad.Membership(position)
                    if(ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
                      //we could join but we may need permission from the squad leader first
                      log.info(s"${tplayer.Name} would like the #${position+1} spot in the squad ${squad.Task}.")
                      AddInviteAndRespond(
                        squad.Leader.CharId,
                        BidForRole(tplayer, guid, position),
                        invitingPlayer = 0L, //we ourselves technically are ...
                        tplayer.Name
                      )
                    }
                  case None => ;
                  //squad does not exist? assume old local data; force update to correct discrepancy
                }

              //the following action can be performed by anyone who has tried to join a squad
              case (_, CancelSelectRoleForYourself(_)) =>
                val cancellingPlayer = tplayer.CharId
                GetSquad(guid) match {
                  case Some(squad) =>
                    //assumption: a player who is cancelling will rarely end up with their invite queued
                    val leaderCharId = squad.Leader.CharId
                    //clean up any active BidForRole invite entry where we are the player who wants to join the leader's squad
                    ((invites.get(leaderCharId) match {
                      case out @ Some(entry) if entry.isInstanceOf[BidForRole] &&
                        entry.asInstanceOf[BidForRole].player.CharId == cancellingPlayer =>
                        out
                      case _ =>
                        None
                    }) match {
                      case Some(entry : BidForRole) =>
                        RemoveInvite(leaderCharId)
                        SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                        NextInviteAndRespond(leaderCharId)
                        Some(true)
                      case _ =>
                        None
                    }).orElse(
                      //look for a queued BidForRole entry where we are the player who wants to join the leader's squad
                      (queuedInvites.get(leaderCharId) match {
                        case Some(_list) =>
                          (_list, _list.indexWhere { entry =>
                            entry.isInstanceOf[BidForRole] &&
                              entry.asInstanceOf[BidForRole].player.CharId == cancellingPlayer
                          })
                        case None =>
                          (Nil, -1)
                      }) match {
                        case (_, -1) =>
                          None //no change
                        case (list, index) if list.size == 1 =>
                          val entry = list.head.asInstanceOf[BidForRole]
                          SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                          queuedInvites.remove(leaderCharId)
                          Some(true)
                        case (list, index) =>
                          val entry = list(index).asInstanceOf[BidForRole]
                          SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                          queuedInvites(leaderCharId) = list.take(index) ++ list.drop(index+1)
                          Some(true)
                      }
                    )

                  case _ => ;
                }

              //the following action can be performed by ???
              case (Some(squad), AssignSquadMemberToRole(position, char_id)) =>
                val membership = squad.Membership.zipWithIndex
                (membership.find({ case (member, _) => member.CharId == char_id}), membership(position)) match {
                  //TODO squad leader currently disallowed
                  case (Some((fromMember, fromPosition)), (toMember, _)) if fromPosition != 0 =>
                    val name = fromMember.Name
                    SwapMemberPosition(squad, toMember, fromMember)
                    if(fromPosition == squad.LeaderPositionIndex) {
                      squad.LeaderPositionIndex = position
                    }
                    membership
                      .filter({ case (_member, _) => _member.CharId > 0 })
                      .foreach { case (_member, _) =>
                        SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromPosition, position)))
                      }
                    UpdateSquadDetail(squad.GUID, squad,
                      SquadDetail().Members(List(
                        SquadPositionEntry(position, SquadPositionDetail().CharId(fromMember.CharId).Name(fromMember.Name)),
                        SquadPositionEntry(fromPosition, SquadPositionDetail().CharId(char_id).Name(name))
                      ))
                    )
                  case _ => ;
                }

              //the following action can be peprformed by anyone
              case (_, SearchForSquadsWithParticularRole(_/*role*/, _/*requirements*/, _/*zone_id*/, _/*search_mode*/)) =>
                //though we should be able correctly search squads as is intended
                //I don't know how search results should be prioritized or even how to return search results to the user
                sender ! SquadServiceResponse("", SquadResponse.SquadSearchResults())

              //the following action can be performed by anyone
              case (_, DisplaySquad()) =>
                GetSquad(guid) match {
                  case Some(squad) =>
                    sender ! SquadServiceResponse("", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
                  case None => ;
                }

              //the following message is feedback from a specific client, awaiting proper initialization
              case (_, SquadMemberInitializationIssue()) =>
              //            GetSquad(guid) match {
              //              case Some(squad) =>
              //                sender ! SquadServiceResponse("", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
              //              case None => ;
              //            }

              case msg => ;
                log.warn(s"Unsupported squad definition behavior: $msg")
            }
        }
        // etc..
        (pSquadOpt, action) match {
          //the following action can be performed by the squad leader and maybe an unaffiliated player
          case (Some(squad), SelectRoleForYourself(position)) =>
            log.info(s"${tplayer.Name} would like the #${position+1} spot in this squad")
            val membership = squad.Membership.zipWithIndex
            val toMember = squad.Membership(position)
            if(squad.Leader.CharId == tplayer.CharId) {
              //TODO squad leader currently disallowed
            } else
            //the squad leader may swap to any open position; a normal member has to validate against requirements
            if((squad.Leader.CharId == tplayer.CharId && toMember.CharId == 0) || ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
              membership.find { case (member, _) => member.CharId == tplayer.CharId } match {
                case Some((fromMember, fromIndex)) =>
                  SwapMemberPosition(squad, toMember, fromMember)
                  if(fromIndex == squad.LeaderPositionIndex) {
                    squad.LeaderPositionIndex = position
                  }
                  //RemoveInvite(tplayer.CharId).foreach { _ =>
                    //close the old bids out
                  //}
                  membership
                    .filter { case (_member, _) => _member.CharId > 0 }
                    .foreach { case (_member, _) =>
                      SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromIndex, position)))
                    }
                  UpdateSquadDetail(squad.GUID, squad)
                case _ => ;
                //somehow, this is not our squad; do nothing, for now
              }
            }
            else {
              //not qualified for requested position
            }

          //the following action can be performed by an unaffiliated player
          case (None, SelectRoleForYourself(position)) =>
            //not a member of any squad, but we might become a member of this one
            GetSquad(guid) match {
              case Some(squad) =>
                val toMember = squad.Membership(position)
                if(ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
                  //we could join but we may need permission from the squad leader first
                  log.info(s"Player ${tplayer.Name} would like to join the squad ${squad.Task}.")
                  AddInviteAndRespond(
                    squad.Leader.CharId,
                    BidForRole(tplayer, guid, position),
                    invitingPlayer = 0L, //we ourselves technically are ...
                    tplayer.Name
                  )
                }
              case None => ;
              //squad does not exist? assume old local data; force update to correct discrepancy
            }

          //the following action can be performed by anyone who has tried to join a squad
          case (_, CancelSelectRoleForYourself(_)) =>
            val cancellingPlayer = tplayer.CharId
            GetSquad(guid) match {
              case Some(squad) =>
                //assumption: a player who is cancelling will rarely end up with their invite queued
                val leaderCharId = squad.Leader.CharId
                //clean up any active BidForRole invite entry where we are the player who wants to join the leader's squad
                ((invites.get(leaderCharId) match {
                  case out @ Some(entry) if entry.isInstanceOf[BidForRole] &&
                    entry.asInstanceOf[BidForRole].player.CharId == cancellingPlayer =>
                    out
                  case _ =>
                    None
                }) match {
                  case Some(entry : BidForRole) =>
                    RemoveInvite(leaderCharId)
                    SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                    NextInviteAndRespond(leaderCharId)
                    Some(true)
                  case _ =>
                    None
                }).orElse(
                  //look for a queued BidForRole entry where we are the player who wants to join the leader's squad
                  (queuedInvites.get(leaderCharId) match {
                    case Some(_list) =>
                      (_list, _list.indexWhere { entry =>
                        entry.isInstanceOf[BidForRole] &&
                          entry.asInstanceOf[BidForRole].player.CharId == cancellingPlayer
                      })
                    case None =>
                      (Nil, -1)
                  }) match {
                    case (_, -1) =>
                      None //no change
                    case (list, index) if list.size == 1 =>
                      val entry = list.head.asInstanceOf[BidForRole]
                      SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                      queuedInvites.remove(leaderCharId)
                      Some(true)
                    case (list, index) =>
                      val entry = list(index).asInstanceOf[BidForRole]
                      SquadEvents.publish( SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, cancellingPlayer, None, entry.player.Name, false, Some(None))))
                      queuedInvites(leaderCharId) = list.take(index) ++ list.drop(index+1)
                      Some(true)
                  }
                )

              case _ => ;
            }

          //the following action can be performed by ???
          case (Some(squad), AssignSquadMemberToRole(position, char_id)) =>
            val membership = squad.Membership.zipWithIndex
            (membership.find({ case (member, _) => member.CharId == char_id}), membership(position)) match {
              //TODO squad leader currently disallowed
              case (Some((fromMember, fromPosition)), (toMember, _)) if fromPosition != 0 =>
                val name = fromMember.Name
                SwapMemberPosition(squad, toMember, fromMember)
                if(fromPosition == squad.LeaderPositionIndex) {
                  squad.LeaderPositionIndex = position
                }
                membership
                  .filter({ case (_member, _) => _member.CharId > 0 })
                  .foreach { case (_member, _) =>
                    SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromPosition, position)))
                  }
                UpdateSquadDetail(squad.GUID, squad,
                  SquadDetail().Members(List(
                    SquadPositionEntry(position, SquadPositionDetail().CharId(fromMember.CharId).Name(fromMember.Name)),
                    SquadPositionEntry(fromPosition, SquadPositionDetail().CharId(char_id).Name(name))
                  ))
                )
              case _ => ;
            }

          //the following action can be peprformed by anyone
          case (_, SearchForSquadsWithParticularRole(_/*role*/, _/*requirements*/, _/*zone_id*/, _/*search_mode*/)) =>
            //though we should be able correctly search squads as is intended
            //I don't know how search results should be prioritized or even how to return search results to the user
            sender ! SquadServiceResponse("", SquadResponse.SquadSearchResults())

          //the following action can be performed by anyone
          case (_, DisplaySquad()) =>
            GetSquad(guid) match {
              case Some(squad) =>
                sender ! SquadServiceResponse("", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
              case None => ;
            }

          //the following message is feedback from a specific client, awaiting proper initialization
          case (_, SquadMemberInitializationIssue()) =>
//            GetSquad(guid) match {
//              case Some(squad) =>
//                sender ! SquadServiceResponse("", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
//              case None => ;
//            }

          case _ => ;
        }

      case SquadAction.Update(char_id, health, max_health, armor, max_armor, pos, zone_number) =>
        memberToSquad.get(char_id) match {
          case Some(squad) =>
            squad.Membership.find(_.CharId == char_id) match {
              case Some(member) =>
                member.Health = StatConverter.Health(health, max_health, min=1, max=64)
                member.Armor = StatConverter.Health(armor, max_armor, min=1, max=64)
                member.Position = pos
                member.ZoneId = zone_number
                sender ! SquadServiceResponse("", SquadResponse.UpdateMembers(
                  squad,
                  squad.Membership
                    .filterNot { _.CharId == 0 }
                    .map { member => SquadAction.Update(member.CharId, member.Health, 0, member.Armor, 0, member.Position, member.ZoneId) }
                    .toList
                ))
              case _ => ;
            }

          case None => ;
        }

      case msg =>
        log.info(s"Unhandled message $msg from $sender")
    }
  }

  /**
    * na
    * @param invitedPlayer the person who will handle the invitation, eventually if not immediately
    * @param invite the "new" invitation envelop object
    * @return an optional invite;
    *         if added to the active invite position, return the parameter bid;
    *         if added to the queued invite, return the invite in the active position;
    *         if not added, return `None`
    */
  def AddInvite(invitedPlayer : Long, invite : Invitation) : Option[Invitation] = {
    invites.get(invitedPlayer).orElse(previousInvites.get(invitedPlayer)) match {
      case Some(_bid) =>
        //the active invite does not interact with the given invite; add to queued invites
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            //ensure that new invite does not interact with the queue's invites by invitingPlayer info
            if(_bid.InviterCharId != invite.InviterCharId && !bidList.exists { eachBid => eachBid.InviterCharId == invite.InviterCharId }) {
              log.debug(s"Invite from ${invite.InviterCharId} to $invitedPlayer stored in queue while active invite request pending")
              queuedInvites(invitedPlayer) = invite match {
                case _: BidForRole =>
                  val (normals, others) = bidList.partition(_.isInstanceOf[BidForRole])
                  (normals :+ invite) ++ others
                case _ =>
                  bidList :+ invite
              }
              Some(_bid)
            }
            else {
              None
            }
          case None =>
            if(_bid.InviterCharId != invite.InviterCharId) {
              log.debug(s"Invite from ${invite.InviterCharId} to $invitedPlayer stored while active invite request pending")
              queuedInvites(invitedPlayer) = List[Invitation](invite)
              Some(_bid)
            }
            else {
              None
            }
        }

      case None =>
        invites(invitedPlayer) = invite
        Some(invite)
    }
  }

  def RemoveInvite(invitedPlayer : Long) : Option[Invitation] = {
    invites.remove(invitedPlayer) match {
      case out @ Some(invite) =>
        previousInvites += invitedPlayer -> invite
        out
      case None =>
        None
    }
  }

  def RemoveQueuedInvites(invitedPlayer : Long) : List[Invitation] = {
    queuedInvites.remove(invitedPlayer) match {
      case Some(_bidList) => _bidList
      case None => Nil
    }
  }

  def RemoveInvites(invitedPlayer : Long, invitingPlayer : Long) : Unit = {
    queuedInvites.get(invitedPlayer) match {
      case Some(bidList) =>
        val list = bidList.filterNot { _.InviterCharId == invitingPlayer }
        if(list.nonEmpty) {
          queuedInvites(invitedPlayer) = list
        }
        else {
          queuedInvites.remove(invitedPlayer)
        }
      case None => ;
    }
    invites.get(invitedPlayer) match {
      case Some(_bid) =>
        if(_bid.InviterCharId == invitingPlayer) {
          //drop bid, try reload new bid
        }
      case None => ;
    }
  }

  def NextInvite(invitedPlayer : Long) : Option[Invitation] = {
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

  def HandleVacancyInvite(squad_guid : PlanetSideGUID, invitedPlayer : Long, invitingPlayer : Long, recruit : Player) : Option[(Squad, Int)] = {
    //accepted an invitation to join an existing squad
    if(squadFeatures.get(squad_guid).isEmpty) {
      log.warn(s"Accept->Invite: the squad #${squad_guid.guid} no longer exists")
      None
    }
    else if(memberToSquad.get(invitedPlayer).nonEmpty) {
      log.warn(s"Accept->Invite: ${recruit.Name} is already a member of a squad and can not join squad #${squad_guid.guid}")
      None
    }
    else {
      val squad = squadFeatures(squad_guid).Squad
      if(!squad.AutoApproveInvitationRequests && squad.Leader.CharId != invitingPlayer) {
        //the inviting player was not the squad leader and this decision should be bounced off the squad leader
        AltAddInviteAndRespond(
          squad.Leader.CharId,
          IndirectInvite(recruit, squad_guid),
          invitingPlayer,
          name = ""
        )
        log.info(s"Accept->Invite: ${recruit.Name} must await an invitation from the leader of squad #${squad_guid.guid}")
        None
      }
      else {
        //if a suitable position in the squad can be found, player may occupy it
        squad.Membership.zipWithIndex.find({ case (member, index) =>
          ValidOpenSquadPosition(squad, index, member, recruit.Certifications)
        }) match {
          case Some((_, line)) =>
            Some((squad, line))
          case _ =>
            if(squad.Size == squad.Capacity) {
              log.warn(s"Accept->Invite: squad #${squad_guid.guid} is already full and ${recruit.Name} can not join it")
            }
            else {
              log.warn(s"Accept->Invite: squad #${squad_guid.guid} has no positions available that satisfy ${recruit.Name}")
            }
            None
        }
      }
    }
  }

  def InitialAssociation(squad : Squad) : Boolean = {
    val guid = squad.GUID
    if(squadFeatures(guid).InitialAssociation) {
      squadFeatures(guid).InitialAssociation = false
      val charId = squad.Leader.CharId
      SquadEvents.publish(
        SquadServiceResponse(s"/$charId/Squad", SquadResponse.AssociateWithSquad(guid))
      )
      SquadEvents.publish(
        SquadServiceResponse(s"/$charId/Squad", SquadResponse.Detail(
          guid,
          SquadService.Detail.Publish(squad))
        )
      )
    }
    false
  }

  def HandleBidForRole(bid : BidForRole, player : Player) : Boolean = {
    HandleBidForRole(bid, bid.squad_guid, bid.player.Name, player)
  }
  def HandleBidForRole(bid : IndirectInvite, player : Player) : Boolean = {
    HandleBidForRole(bid, bid.squad_guid, bid.player.Name, player)
  }

  def HandleBidForRole(bid : Invitation, squad_guid : PlanetSideGUID, name : String, player : Player) : Boolean = {
    GetSquad(squad_guid) match {
      case Some(squad) =>
        val leaderCharId = squad.Leader.CharId
        if(squad.AutoApproveInvitationRequests) {
          self ! SquadServiceMessage(player, Zone.Nowhere, SquadAction.Membership(SquadRequestType.Accept, leaderCharId, None, "", None))
        }
        else {
          SquadEvents.publish(SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.WantsSquadPosition(name)))
        }
        true
      case _ =>
        //squad is missing; will this properly short-circuit?
        log.error(s"Attempted to process ${bid.InviterName}'s bid for a position in a squad (id:${squad_guid.guid}) that does not exist")
        false
    }
  }

  def JoinSquad(player : Player, squad : Squad, line : Int) : Boolean = {
    val charId = player.CharId
    val position = squad.Membership(line)
    if(ValidOpenSquadPosition(squad, line, position, player.Certifications)) {
      log.info(s"Player ${player.Name} will join the squad ${squad.Task} at position ${line+1}!")
      position.Name = player.Name
      position.CharId = charId
      position.Health = StatConverter.Health(player.Health, player.MaxHealth, min=1, max=64)
      position.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min=1, max=64)
      position.Position = player.Position
      position.ZoneId = 13
      memberToSquad(charId) = squad

      InitialAssociation(squad)
      SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.AssociateWithSquad(squad.GUID)) )
      val size = squad.Size
      if(size == 1) {
        //leader joins the squad? do nothing?
        squad.LeaderPositionIndex = line
      }
      else if(size == 2) {
        //first squad member after leader; both members fully initialize
        val indices = squad.Membership.zipWithIndex
          .collect({ case (member, index) if member.CharId != 0 => index }).toList
        squad.Membership
          .filterNot { _.CharId == 0 }
          .foreach { member =>
            SquadEvents.publish(SquadServiceResponse(s"/${member.CharId}/Squad", SquadResponse.Join(squad, indices)))
            InitWaypoints(member.CharId, squad.GUID)
          }
        //fully update for all users
        UpdateSquadDetail(squad.GUID, squad)
      }
      else {
        //joining an active squad; everybody updates differently
        //new member gets full squad UI updates
        val indices = squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
        SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Join(squad, indices)))
        InitSquadDetail(squad.GUID, Seq(charId), squad)
        InitWaypoints(charId, squad.GUID)
        //other squad members see new member joining the squad
        val updatedIndex = List(line)
        val otherMembers = squad.Membership.filterNot { member => member.CharId == 0 || member.CharId == charId }.map{ _.CharId }
        otherMembers.foreach { member =>
          SquadEvents.publish(SquadServiceResponse(s"/$member/Squad", SquadResponse.Join(squad, updatedIndex)))
        }
        val details = SquadDetail().Members(List(SquadPositionEntry(line, SquadPositionDetail().CharId(charId).Name(player.Name))))
        UpdateSquadDetail(squad.GUID, otherMembers, details)
      }
      UpdateSquadListWhenListed(squad, SquadInfo().Size(size))
      true
    }
    else {
      false
    }
  }

  def RemoveInvitesForSquadAndPosition(guid : PlanetSideGUID, position : Int) : Unit = {
    //eliminate active invites for this role
    invites.collect {
      case(charId, InviteForRole(_,_, sguid, pos)) if sguid == guid && pos == position =>
        RemoveInvite(charId)
      case (charId, BidForRole(_, sguid, pos)) if sguid == guid && pos == position =>
        RemoveInvite(charId)
    }
    RemoveQueuedInvitesForSquadAndPosition(guid, position)
  }

  def RemoveQueuedInvitesForSquadAndPosition(guid : PlanetSideGUID, position : Int) : Unit = {
    //eliminate other invites for this role
    queuedInvites.foreach { case(charId, queue) =>
      val filtered = queue.filterNot {
        case InviteForRole(_,_, sguid, pos) => sguid == guid && pos == position
        case BidForRole(_, sguid, pos) => sguid == guid && pos == position
        case _ => false
      }
      if(filtered.isEmpty) {
        queuedInvites.remove(charId)
      }
      else if(queue.size != filtered.size) {
        queuedInvites += charId -> filtered
      }
    }
  }

  def EnsureEmptySquad(char_id : Long, msg : String = "default warning message") : Boolean = {
    memberToSquad.get(char_id) match {
      case None =>
        true
      case Some(squad) if squad.Size == 1 =>
        CloseSquad(squad)
        true
      case _ =>
        log.warn(msg)
        false
    }
  }

  def LeaveSquad(charId : Long, squad : Squad) : Boolean = {
    val membership = squad.Membership.zipWithIndex
    membership.find { case (_member, _) => _member.CharId == charId } match {
      case Some((member, index)) =>
        val entry = (charId, index)
        val updateList = entry +: membership
          .collect { case (_member, _index) if _member.CharId > 0 && _member.CharId != charId => (_member.CharId, _index) }
          .toList
        //member leaves the squad completely
        memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0
        SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.Leave(squad, updateList)))
        //other squad members see the member leaving
        val leavingMember = List(entry)
        membership
          .filter { case (_member, _) => _member.CharId > 0 }
          .foreach { case (_member, _) =>
            SquadEvents.publish( SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.Leave(squad, leavingMember)) )
          }
        UpdateSquadListWhenListed(squad, SquadInfo().Size(squad.Size))
        UpdateSquadDetail(squad.GUID, squad,
          SquadDetail().Members(List(SquadPositionEntry(index, SquadPositionDetail().Player(char_id = 0, name = ""))))
        )
        true
      case None =>
        false
    }
  }

  def CloseSquad(squad : Squad) : Unit = {
    val guid = squad.GUID
    val membership = squad.Membership.zipWithIndex
    val (updateMembers, updateIndices) = membership
      .collect { case (member, index) if member.CharId > 0 => ((member, member.CharId, index), (member.CharId, index)) }
      .unzip
    val updateIndicesList = updateIndices.toList
    val completelyBlankSquadDetail = SquadDetail().Complete
    updateMembers
      .foreach { case (member, charId, index) =>
        memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0L
        SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.Leave(squad,
          updateIndicesList.filterNot { case (_, outIndex) => outIndex == index } :+ (charId, index) //we need to be last to leave to see the events
        )) )
        SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.AssociateWithSquad(PlanetSideGUID(0))) )
        SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.Detail(PlanetSideGUID(0), completelyBlankSquadDetail)) )
      }
    UpdateSquadListWhenListed(squad, None)
    CleanupInvitesForSquad(guid)
    squadFeatures.remove(guid).get.Stop
    TryResetSquadId()
  }

  def DisbandSquad(squad : Squad) : Unit = {
    CloseSquad(squad)
    val leader = squad.Leader.CharId
    SquadEvents.publish(SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, leader, None, "", true, Some(None))))
    squad.Membership
      .collect { case member if member.CharId > 0 && member.CharId != leader => member.CharId }
      .foreach { charId =>
        SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", false, Some(None))))
      }
  }

  def CleanupInvitesForSquad(squadGUID : PlanetSideGUID) : Unit = {
    //clean up invites
    invites.collect {
      case (id, VacancyInvite(_, _, guid)) if squadGUID == guid =>
        RemoveInvite(id)
      case (id, IndirectInvite(_, guid)) if squadGUID == guid =>
        RemoveInvite(id)
      case (id, InviteForRole(_, _, guid, _)) if squadGUID == guid =>
        RemoveInvite(id)
      case (id, BidForRole(_, guid, _)) if squadGUID == guid =>
        RemoveInvite(id)
      case (id, ProximityInvite(_, _, guid)) if squadGUID == guid =>
        RemoveInvite(id)
    }
    //tidy the queued invitations
    queuedInvites.foreach { case(id, queue) =>
      val filteredQueue = queue.filterNot {
        case VacancyInvite(_, _, guid) => squadGUID == guid
        case IndirectInvite(_, guid) => squadGUID == guid
        case InviteForRole(_, _, guid, _) => squadGUID == guid
        case BidForRole(_, guid, _) => squadGUID == guid
        case ProximityInvite(_, _, guid) => squadGUID == guid
        case _ => false
      }
      if(filteredQueue.isEmpty) {
        queuedInvites.remove(id)
      }
      else if(filteredQueue.size != queue.size) {
        queuedInvites.update(id, filteredQueue)
      }
    }
    squadFeatures(squadGUID).SearchForRole match {
      case None => ;
      case Some(_) =>
        squadFeatures(squadGUID).SearchForRole = None
    }
  }

  def CleanupInvitesFromPlayer(charId : Long) : Unit = {
    invites.remove(charId)
    invites.collect {
      case (id, SpontaneousInvite(player)) if player.CharId == charId =>
        RemoveInvite(id)
      case (id, VacancyInvite(_charId, _, _)) if _charId == charId =>
        RemoveInvite(id)
      case (id, IndirectInvite(player, _)) if player.CharId == charId =>
        RemoveInvite(id)
      case (id, InviteForRole(_charId, _, _, _)) if _charId == charId =>
        RemoveInvite(id)
      case (id, BidForRole(player, _, _)) if player.CharId == charId =>
        RemoveInvite(id)
      case (id, ProximityInvite(_charId, _, _)) if charId == _charId =>
        RemoveInvite(id)
    }
    //tidy the queued invitations
    queuedInvites.remove(charId)
    queuedInvites.foreach { case(id, queue) =>
      val filteredQueue = queue.filterNot {
        case SpontaneousInvite(player) => player.CharId == charId
        case VacancyInvite(player, _, _) => player == charId
        case IndirectInvite(player, _) => player.CharId == charId
        case InviteForRole(player, _, _, _) => player == charId
        case BidForRole(player, _, _) => player.CharId == charId
        case ProximityInvite(_charId, _, _) => _charId == charId
        case _ => false
      }
      if(filteredQueue.isEmpty) {
        queuedInvites.remove(id)
      }
      else if(filteredQueue.size != queue.size) {
        queuedInvites.update(id, filteredQueue)
      }
    }
    previousInvites.remove(charId)
  }

  def CleanupInvitesToPosition(position : Int) : Unit = {
    invites.collect {
      case (id, InviteForRole(_, _, _, _position)) if _position == position =>
        RemoveInvite(id)
      case (id, BidForRole(_, _, _position)) if _position == position =>
        RemoveInvite(id)
    }
    //tidy the queued invitations
    queuedInvites.foreach { case(id, queue) =>
      val filteredQueue = queue.filterNot {
        case InviteForRole(_, _, _, _position) => _position == position
        case BidForRole(_, _, _position) => _position == position
        case _ => false
      }
      if(filteredQueue.isEmpty) {
        queuedInvites.remove(id)
      }
      else if(filteredQueue.size != queue.size) {
        queuedInvites.update(id, filteredQueue)
      }
    }
  }

  def SwapMemberPosition(squad : Squad, toMember : Member, fromMember : Member) : Unit = {
    val (name, charId, zoneId, pos, health, armor) = (fromMember.Name, fromMember.CharId, fromMember.ZoneId, fromMember.Position, fromMember.Health, fromMember.Armor)
    if(toMember.CharId > 0) {
      fromMember.Name = toMember.Name
      fromMember.CharId = toMember.CharId
      fromMember.ZoneId = toMember.ZoneId
      fromMember.Position = toMember.Position
      fromMember.Health = toMember.Health
      fromMember.Armor = toMember.Armor
    }
    else {
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

  def UpdateSquadList(faction : PlanetSideEmpire.Value): Unit = {
    val factionListings = publishedLists(faction)
    SquadEvents.publish(
      SquadServiceResponse(s"/$faction/Squad", SquadResponse.InitList(factionListings.toVector))
    )
  }

  def UpdateSquadList(squad : Squad, changes : SquadInfo) : Unit = {
    UpdateSquadList(squad, Some(changes))
  }

  def UpdateSquadListWhenListed(squad : Squad, changes : SquadInfo) : Unit = {
    UpdateSquadListWhenListed(squad, Some(changes))
  }

  def UpdateSquadListWhenListed(squad : Squad, changes : Option[SquadInfo]) : Unit = {
    if(squad.Listed || squad.Size > 1) {
      UpdateSquadList(squad, changes)
    }
  }

  def UpdateSquadList(squad : Squad, changes : Option[SquadInfo]) : Unit = {
    val faction = squad.Faction
    val factionListings = publishedLists(faction)
    factionListings.find(info => {
      info.squad_guid match {
        case Some(sguid) => sguid == squad.GUID
        case _ => false
      }
    }) match {
      case Some(listedSquad) =>
        val index = factionListings.indexOf(listedSquad)
        changes match {
          case Some(changedFields) =>
            //squad information update
            log.info(s"Squad will be updated")
            factionListings(index) = SquadService.SquadList.Publish(squad)
            SquadEvents.publish(
              SquadServiceResponse(s"/$faction/Squad", SquadResponse.UpdateList(Seq((index, changedFields))))
            )
          case None =>
            //remove squad from listing
            log.info(s"Squad will be removed")
            factionListings.remove(index)
            SquadEvents.publish(
              //SquadServiceResponse(s"$faction/Squad", SquadResponse.RemoveFromList(Seq(index)))
              SquadServiceResponse(s"/$faction/Squad", SquadResponse.InitList(factionListings.toVector))
            )
        }
      case None =>
        //first time being published
        log.info(s"Squad will be introduced")
        factionListings += SquadService.SquadList.Publish(squad)
        SquadEvents.publish(
          SquadServiceResponse(s"/$faction/Squad", SquadResponse.InitList(factionListings.toVector))
        )
    }
  }

  def InitSquadDetail(squad : Squad) : Unit = {
    InitSquadDetail(squad.GUID, squad.Membership.map { member => member.CharId }, squad)
  }

  def InitSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    InitSquadDetail(guid, squad.Membership.map { member => member.CharId }, squad)
  }

  def InitSquadDetail(guid : PlanetSideGUID, toMembers : Iterable[Long], squad : Squad) : Unit = {
    val output = SquadResponse.Detail(guid, SquadService.Detail.Publish(squad))
    toMembers.foreach { charId => SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", output)) }
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    UpdateSquadDetail(guid, squad, SquadService.Detail.Publish(squad))
  }

  def UpdateSquadDetail(squad : Squad, details : SquadDetail) : Unit = {
    UpdateSquadDetail(squad.GUID, squad.Membership.map { member => member.CharId }, details)
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad, details : SquadDetail) : Unit = {
    UpdateSquadDetail(guid, squad.Membership.map { member => member.CharId }, details)
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, toMembers : Iterable[Long], details : SquadDetail) : Unit = {
    if(toMembers.nonEmpty) {
      val output = SquadResponse.Detail(guid, details)
      toMembers.foreach { charId => SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", output)) }
    }
  }

  def AddWaypoint(guid : PlanetSideGUID, waypointType : Int, info : WaypointInfo) : Option[WaypointData] = {
    squadFeatures(guid).Waypoints.lift(waypointType) match {
      case Some(point) =>
        //update the waypoint
        log.debug(s"rendering squad waypoint $waypointType for squad #${guid.guid}")
        point.zone_number = info.zone_number
        point.pos = info.pos
        Some(point)
      case _ =>
        log.warn(s"no squad waypoint $waypointType found")
        None
    }
  }

  def RemoveWaypoint(guid : PlanetSideGUID, waypointType : Int) : Unit = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        features.Waypoints.lift(waypointType) match {
          case Some(point) =>
            //update the waypoint
            log.debug(s"removing squad waypoint $waypointType for squad #${guid.guid}")
            point.zone_number = 1
            point.pos = Vector3.z(1)
          case _ =>
            log.warn(s"no squad waypoint $waypointType found")
        }
      case _ =>
        log.warn(s"no squad #$guid found")
    }
  }

  def InitWaypoints(toCharId : Long, guid : PlanetSideGUID) : Unit = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        val squad = features.Squad
        val vz1 = Vector3.z(value = 1)
        val list = features.Waypoints
        SquadEvents.publish(
          SquadServiceResponse(s"/$toCharId/Squad", SquadResponse.InitWaypoints(squad.Leader.CharId,
            list.zipWithIndex.collect { case (point, index) if point.pos != vz1 =>
              (index, WaypointInfo(point.zone_number, point.pos), 1)
            }
          ))
        )
      case None => ;
    }
  }

  def indirectInviteResp(bid : IndirectInvite, player : Player, invitedPlayer : Long, invitingPlayer : Long, name : String) : Boolean = {
    HandleBidForRole(bid, player)
  }

  def altIndirectInviteResp(bid : IndirectInvite, player : Player, invitedPlayer : Long, invitingPlayer : Long, name : String) : Boolean = {
    SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayer, Some(invitedPlayer), player.Name, false, Some(None))))
    HandleBidForRole(bid, player)
  }

  def InviteResponseTemplate(indirectVacancyFunc : (IndirectInvite, Player, Long, Long, String) => Boolean)(targetInvite : Invitation, actualInvite : Option[Invitation], invitedPlayer : Long, invitingPlayer : Long, name : String) : Unit = {
    if(actualInvite.contains(targetInvite)) {
      //immediately respond
      targetInvite match {
        case VacancyInvite(charId, _name, _) =>
          SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, charId, Some(invitedPlayer), _name, false, Some(None))))
          SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(charId), _name, true, Some(None))))

        case _bid @ IndirectInvite(player, _) =>
          indirectVacancyFunc(_bid, player, invitedPlayer, invitingPlayer, name)

        case _bid @ SpontaneousInvite(player) =>
          val bidInvitingPlayer = _bid.InviterCharId
          SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, bidInvitingPlayer, Some(invitedPlayer), player.Name, false, Some(None))))
          SquadEvents.publish(SquadServiceResponse(s"/$bidInvitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(bidInvitingPlayer), player.Name, true, Some(None))))

        case _bid @ BidForRole(player, _, _) =>
          HandleBidForRole(_bid, player)

        case InviteForRole(charId, _name, _, _) =>
          SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(charId), _name, false, Some(None))))

        case ProximityInvite(charId, _name, _) =>
          SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(charId), _name, false, Some(None))))

        case _ =>
          log.warn(s"AddInviteAndRespond: can not parse discovered unhandled invitation type - $targetInvite")
      }
    }
  }

  def AddInviteAndRespond(invitedPlayer : Long, targetInvite : Invitation, invitingPlayer : Long, name : String) : Unit = {
    InviteResponseTemplate(indirectInviteResp)(
      targetInvite,
      AddInvite(invitedPlayer, targetInvite),
      invitedPlayer,
      invitingPlayer,
      name
    )
  }

  def AltAddInviteAndRespond(invitedPlayer : Long, targetInvite : Invitation, invitingPlayer : Long, name : String) : Unit = {
    InviteResponseTemplate(altIndirectInviteResp)(
      targetInvite,
      AddInvite(invitedPlayer, targetInvite),
      invitedPlayer,
      invitingPlayer,
      name
    )
  }

  def NextInviteAndRespond(invitedPlayer : Long) : Unit = {
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
}

object SquadService {
  class WaypointData() {
    var zone_number : Int = 1
    var pos : Vector3 = Vector3.z(1) //a waypoint with a non-zero z-coordinate will flag as not getting drawn
  }

  abstract class Invitation(char_id : Long, name : String) {
    def InviterCharId : Long = char_id
    def InviterName : String = name
  }

  /**
    * Utilized when one player attempts to join an existing squad in a specific role.
    * Accessed by the joining player from the squad detail window.
    * @param player the player who requested the role
    * @param squad_guid the squad with the role
    * @param position the index of the role
    */
  final case class BidForRole(player : Player, squad_guid : PlanetSideGUID, position : Int)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized when one squad member issues an invite for some other player.
    * Accessed by an existing squad member using the "Invite" menu option on another player.
    * @param char_id the unique character identifier of the player who sent the invite
    * @param name the name the player who sent the invite
    * @param squad_guid the squad
    */
  final case class VacancyInvite(char_id : Long, name : String, squad_guid : PlanetSideGUID)
    extends Invitation(char_id, name)

  /**
    * Utilized to redirect an (accepted) invitation request to the proper squad leader.
    * No direct action causes this message.
    * @param player the player who would be joining the squad;
    *               may or may not have actually requested it in the first place
    * @param squad_guid the squad
    */
  final case class IndirectInvite(player : Player, squad_guid : PlanetSideGUID)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out unoccupied squad roles.
    * @param char_id the unique character identifier of the squad leader
    * @param name the name of the squad leader
    * @param squad_guid the squad
    */
  final case class ProximityInvite(char_id : Long, name : String, squad_guid : PlanetSideGUID)
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
  final case class InviteForRole(char_id : Long, name : String, squad_guid : PlanetSideGUID, position : Int)
    extends Invitation(char_id, name)

  /**
    * Utilized when one player issues an invite for some other player for a squad that does not yet exist.
    * @param player na
    */
  final case class SpontaneousInvite(player : Player)
    extends Invitation(player.CharId, player.Name)

  class SquadFeatures(val Squad : Squad) {
    /**
      * `initialAssociation` per squad is similar to "Does this squad want to recruit members?"
      * The squad does not have to be flagged.
      * Dispatches an `AssociateWithSquad` `SDAM` to the squad leader and ???
      * and then a `SDDUM` that includes at least the squad owner name and char id.
      * Dispatched only once when a squad is first listed
      * or when the squad leader searches for recruits by proximity or for certain roles or by invite
      * or when a spontaneous squad forms,
      * whichever happens first.
      * Additionally, the packets are also sent when the check is made when the continent is changed (or set).
      */
    private var initialAssociation : Boolean = true
    /**
      * na
      */
    private var switchboard : ActorRef = ActorRef.noSender
    /**
      * Waypoint data.
      * The first four slots are used for squad waypoints.
      * The fifth slot is used for the squad leader experience waypoint.
      * @see `Start`
      */
    private var waypoints : Array[WaypointData] = Array[WaypointData]()
    /**
      * The particular position being recruited right at the moment.
      * When `None`. no highlighted searches have been indicated.
      * When a positive integer or 0, indicates distributed `InviteForRole` messages as recorded by `proxyInvites`.
      * Only one position may bne actively recruited at a time in this case.
      * When -1, indicates distributed `ProximityIvite` messages as recorded by `proxyInvites`.
      * Previous efforts may or may not be forgotten if there is a switch between the two modes.
      */
    private var searchForRole : Option[Int] = None
    /**
      * Handle persistent data related to `ProximityInvite` and `InviteForRole` messages
      */
    private var proxyInvites : List[Long] = Nil
    /**
      * These useres rejected invitation to this squad.
      * For the purposes of wide-searches for membership
      * such as Looking For Squad checks and proximity invitation,
      * the unique character identifier numbers in this list are skipped.
      * Direct invitation requests from the non sqad member should remain functional.
      */
    private var refusedPlayers : List[Long] = Nil

    def Start(implicit context : ActorContext) : SquadFeatures = {
      switchboard = context.actorOf(Props[SquadSwitchboard], s"squad${Squad.GUID.guid}")
      waypoints = Array.fill[WaypointData](5)(new WaypointData())
      this
    }

    def Stop : SquadFeatures = {
      switchboard ! akka.actor.PoisonPill
      switchboard = Actor.noSender
      waypoints = Array.empty
      this
    }

    def InitialAssociation : Boolean = initialAssociation

    def InitialAssociation_=(assoc : Boolean) : Boolean = {
      initialAssociation = assoc
      InitialAssociation
    }

    def Switchboard : ActorRef = switchboard

    def Waypoints : Array[WaypointData] = waypoints

    def SearchForRole : Option[Int] = searchForRole

    def SearchForRole_=(role : Int) : Option[Int] = SearchForRole_=(Some(role))

    def SearchForRole_=(role : Option[Int]) : Option[Int] = {
      searchForRole = role
      SearchForRole
    }

    def ProxyInvites : List[Long] = proxyInvites

    def ProxyInvites_=(list : List[Long]) : List[Long] = {
      proxyInvites = list
      ProxyInvites
    }

    def Refuse : List[Long] = refusedPlayers

    def Refuse_=(charId : Long) : List[Long] = {
      Refuse_=(List(charId))
    }

    def Refuse_=(list : List[Long]) : List[Long] = {
      refusedPlayers = list ++ refusedPlayers
      Refuse
    }
  }

  object SquadList {
    def Publish(squad : Squad) : SquadInfo = {
      SquadInfo(
        squad.Leader.Name,
        squad.Task,
        PlanetSideZoneID(squad.ZoneId),
        squad.Size,
        squad.Capacity,
        squad.GUID
      )
    }
  }

  object Detail {
    def Publish(squad : Squad) : SquadDetail = {
      SquadDetail()
        .Field1(squad.GUID.guid)
        .LeaderCharId(squad.Leader.CharId)
        .LeaderName(squad.Leader.Name)
        .Task(squad.Task)
        .ZoneId(PlanetSideZoneID(squad.ZoneId))
        .Members(
          squad.Membership.zipWithIndex.map({ case (p, index) =>
            SquadPositionEntry(index, if(squad.Availability(index)) {
              SquadPositionDetail(p.Role, p.Orders, p.Requirements, p.CharId, p.Name)
            }
            else {
              SquadPositionDetail.Closed
            })
          }).toList
        )
        .Complete
    }
  }

  def LoadSquadDefinition(squad : Squad, favorite : SquadLoadout) : Unit = {
    squad.Task = favorite.task
    squad.ZoneId = favorite.zone_id.getOrElse(squad.ZoneId)
    squad.Availability.indices.foreach { index => squad.Availability.update(index, false) }
    squad.Membership.foreach { position =>
      position.Role = ""
      position.Orders = ""
      position.Requirements = Set()
    }
    favorite.members.foreach { position =>
      squad.Availability.update(position.index, true)
      val member = squad.Membership(position.index)
      member.Role = position.role
      member.Orders = position.orders
      member.Requirements = position.requirements
    }
  }

  def ValidOpenSquadPosition(squad : Squad, position : Int, member : Member, reqs : Set[CertificationType.Value]) : Boolean = {
    ValidSquadPosition(squad, position, member, reqs) && member.CharId == 0
  }

  def ValidSquadPosition(squad : Squad, position : Int, member : Member, reqs : Set[CertificationType.Value]) : Boolean = {
    squad.Availability(position) && reqs.intersect(member.Requirements) == member.Requirements
  }
}
