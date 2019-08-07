// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.Player
import net.psforever.objects.definition.converter.StatConverter
import net.psforever.objects.loadouts.SquadLoadout
import net.psforever.objects.teamwork.{Member, Squad}
import net.psforever.packet.game._
import net.psforever.types._
import services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

//import scala.concurrent.duration._

class SquadService extends Actor {
  import SquadService._

  private var memberToSquad : mutable.LongMap[Squad] = mutable.LongMap[Squad]()
  private var idToSquad : TrieMap[PlanetSideGUID, Squad] = new TrieMap[PlanetSideGUID, Squad]()
  private var idToSwitchboard : TrieMap[PlanetSideGUID, ActorRef] = new TrieMap[PlanetSideGUID, ActorRef]()
  private var sid : Int = 1
  private val publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
    PlanetSideEmpire.TR -> ListBuffer.empty,
    PlanetSideEmpire.NC -> ListBuffer.empty,
    PlanetSideEmpire.VS -> ListBuffer.empty
  )
  private val invites : mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()
  private val queuedInvites : mutable.LongMap[List[Invitation]] = mutable.LongMap[List[Invitation]]()
  private val viewDetails : mutable.LongMap[PlanetSideGUID] = mutable.LongMap[PlanetSideGUID]()

  private [this] val log = org.log4s.getLogger

  override def preStart : Unit = {
    log.info("Starting...")

//    val testSquad = new Squad(PlanetSideGUID(3), PlanetSideEmpire.VS)
//    testSquad.Task = "\\#66CCFF Sentinels of Auraxis\\#FFFFFF ... \\#40FF40 Squad Up!!"
//    testSquad.ZoneId = 5
//    testSquad.Membership(0).Name = "Wizkid45"
//    testSquad.Membership(0).Role = "Fearless Leader"
//    testSquad.Membership(0).CharId = 30910985L
//    testSquad.Membership(0).ZoneId = 5
//    testSquad.Membership(0).Health = 64
//    testSquad.Membership(0).Armor = 34
//    testSquad.Membership(0).Position = Vector3(5526.5234f, 3818.7344f, 54.59375f)
//    testSquad.Membership(1).Name = "xoBLADEox"
//    testSquad.Membership(1).Role = "Right Hand"
//    testSquad.Membership(1).CharId = 42781919L
//    testSquad.Membership(1).ZoneId = 5
//    testSquad.Membership(1).Health = 54
//    testSquad.Membership(1).Armor = 44
//    testSquad.Membership(1).Position = Vector3(4673.5312f, 2604.8047f, 40.015625f)
//    testSquad.Membership(3).Name = "cabal0428"
//    testSquad.Membership(3).Role = "Left Hand"
//    testSquad.Membership(3).CharId = 353380L
//    testSquad.Membership(3).ZoneId = 5
//    testSquad.Membership(3).Health = 44
//    testSquad.Membership(3).Armor = 54
//    testSquad.Membership(3).Position = Vector3(4727.492f, 2613.5312f, 51.390625f)
//    testSquad.Membership(4).Name = "xSkiku"
//    testSquad.Membership(4).Role = "Right Foot's Middle Toe's Nail"
//    testSquad.Membership(4).CharId = 41588340L
//    testSquad.Membership(4).ZoneId = 5
//    testSquad.Membership(4).Health = 34
//    testSquad.Membership(4).Armor = 64
//    testSquad.Membership(4).Position = Vector3(3675.0f, 4789.8047f, 63.21875f)
//    idToSquad(PlanetSideGUID(3)) = testSquad
//    testSquad.Listed = true
//    UpdateSquadList(testSquad, None)
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
    if(idToSquad.isEmpty) {
      sid = 1
      true
    }
    else {
      false
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

  def GetLeadingSquad(player : Player, zone : Int, opt : Option[Squad]) : Option[Squad] = {
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
    val charId = player.CharId
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
    log.info(s"$name-$faction has started a new squad")
    squad
  }

  def StartSquad(squad : Squad) : Squad = {
    val charId = squad.Leader.CharId
    val id = squad.GUID
    val switchboard = context.actorOf(Props[SquadSwitchboard], s"squad${id.guid}")
    memberToSquad += charId -> squad
    idToSquad += id -> squad
    idToSwitchboard += id -> switchboard
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

    //subscribe to the player's personal channel - necessary only to inform about any previous squad association
    case Service.Join(char_id) =>
      val path = s"/$char_id/Squad"
      val who = sender()
      log.info(s"$who has joined $path")
      SquadEvents.subscribe(who, path) //TODO squad-specific switchboard
      //check for renewable squad information
      memberToSquad.get(char_id.toLong) match {
        case None => ;
        case Some(_) =>
          sender ! SquadServiceMessage.RecoverSquadMembership() //TODO?
      }

    case Service.Leave(Some(char_id)) => ;
      SquadEvents.unsubscribe(sender())
      val longCharId = char_id.toLong
      memberToSquad.get(longCharId) match {
        case Some(squad) =>
          if(squad.Leader.Name.equals(char_id)) {
            //we were the leader
            if(squad.Membership.count(p => p.Name.equals("")) > 1) {
              //other players were in the squad; publicly disband it
              squad.Membership.foreach(position => {
                position.Name = ""
                position.CharId = 0L
                position.ZoneId = 0
                position.Position = Vector3.Zero
                position.Health = 0
                position.Armor = 0
              })
            }
            memberToSquad.remove(longCharId)
            idToSquad.remove(squad.GUID)
            UpdateSquadList(squad, None)
          }
          else {
            //we were just a grunt in the squad
            val position = squad.Membership.find(_.CharId == longCharId).get
            position.Name = ""
            position.ZoneId = 0
            position.Position = Vector3.Zero
            position.Health = 0
            position.Armor = 0
            UpdateSquadDetail(squad.GUID, squad)
          }
        case None => ;
      }
    //TODO leave squad, if joined to one, and perform clean-up

    case Service.Leave(None) | Service.LeaveAll() => ;

    case SquadServiceMessage(tplayer, squad_action) => squad_action match {
      case SquadAction.Membership(SquadRequestType.Invite, invitingPlayer, Some(invitedPlayer), _, _) =>
        //this is just busy work; for actual joining operations, see SquadRequestType.Accept
//        FindBid(invitingPlayer, invitedPlayer) match {
//          case Some(bid) =>
//            //invitingPlayer and invitedPlayer have both tried to join each others's squads
//            //treat this junction as consent
//            self ! SquadServiceMessage(tplayer, SquadAction.Membership(SquadRequestType.Accept, invitingPlayer, Some(invitedPlayer), "", None))
//          case _ => ;
//        }
        (memberToSquad.get(invitingPlayer), memberToSquad.get(invitedPlayer)) match {
          case (Some(squad1), Some(squad2)) =>
            //both players are in squads
            if(squad1.GUID == squad2.GUID) {
              //both players are in the same squad; no need to do anything
            }
            else {
              //we might do some platoon chicanery with this case later
              //TODO platoons
            }

          case (Some(squad), None) =>
            //the classic situation
            log.info(s"$invitedPlayer has been invited to squad ${squad.Task} by $invitingPlayer")
            val leader = squad.Leader
            val leaderCharId = leader.CharId
            val bid = VacancyInvite(leaderCharId, leader.Name, squad.GUID)
            AddInvite(invitedPlayer, bid) match {
              case out @ Some(_) if out.contains(bid) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, leaderCharId, Some(invitedPlayer), tplayer.Name, false, Some(None))))
                SquadEvents.publish(SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(leaderCharId), tplayer.Name, true, Some(None))))
              case Some(_) =>
                SquadEvents.publish(SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(leaderCharId), tplayer.Name, true, Some(None))))
              case _ => ;
            }

          case (None, Some(squad)) =>
            //flip around the roles - the inviting becomes the invited
            //TODO needs work
            log.info(s"$invitedPlayer has asked $invitingPlayer for an invition to squad ${squad.Task}")
            val bid = VacancyInvite(invitedPlayer, "", squad.GUID)
            AddInvite(invitingPlayer, bid) match {
              case out @ Some(_) if out.contains(bid) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(invitingPlayer), tplayer.Name, false, Some(None))))
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, true, Some(None))))
              case Some(_) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, true, Some(None))))
              case _ => ;
            }

          case (None, None) =>
            //neither the invited player nor the inviting player belong to any squad
            log.info(s"$invitedPlayer has been invited to join $invitingPlayer's spontaneous squad")
            val bid = SpontaneousInvite(tplayer)
            AddInvite(invitedPlayer, bid) match {
              case out @ Some(_) if out.contains(bid) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, false, Some(None))))
                SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(invitingPlayer), tplayer.Name, true, Some(None))))
              case Some(_) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitedPlayer, Some(invitingPlayer), tplayer.Name, true, Some(None))))
              case _ => ;
            }

          case _ => ;
        }

      case SquadAction.Membership(SquadRequestType.Accept, invitedPlayer, _, _, _) =>
        val acceptedInvite = RemoveInvite(invitedPlayer)
        acceptedInvite match {
          case Some(BidForPosition(petitioner, guid, position)) if idToSquad.get(guid).nonEmpty =>
            //player requested to join a squad's specific position
            //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
            if(memberToSquad.get(petitioner.CharId).isEmpty) {
              JoinSquad(petitioner, idToSquad(guid), position)
            }
            else {
              log.warn("Accept -> Bid: the invited player is already a member of a squad and can not join a second one")
            }

          case Some(VacancyInvite(invitingPlayer, _, guid))
            if idToSquad.get(guid).nonEmpty =>
            //we were invited by the squad leader into an existing squad
            if(memberToSquad.get(invitedPlayer).isEmpty) {
              val squad = idToSquad(guid)
              squad.Membership.zipWithIndex.find({ case (member, index) =>
                ValidOpenSquadPosition(squad, index, member, tplayer.Certifications)
              }) match {
                case Some((_, line)) =>
                  SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayer, Some(invitedPlayer), tplayer.Name, false, Some(None))))
                  SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayer), "", true, Some(None))))
                  JoinSquad(tplayer, squad, line)
                  RemoveQueuedInvites(invitedPlayer) //TODO deal with these somehow
                case _ => ;
              }
            }
            else {
              log.warn("Accept -> Invite: the invited player is already a member of a squad and can not join a second one")
            }

          case Some(SpontaneousInvite(invitingPlayer)) =>
            //we were invited by someone into a new squad they would form
            val invitingPlayerCharId = invitingPlayer.CharId
            (GetParticipatingSquad(invitingPlayer) match {
              case Some(participating) =>
                if(participating.Leader.CharId == invitingPlayerCharId) {
                  Some(participating)
                }
                else {
                  //inviter joined a squad and is not its leader; bounce this request off of the squad leader
                  participating.Membership.zipWithIndex.find({ case (member, index) =>
                    ValidOpenSquadPosition(participating, index, member, tplayer.Certifications)
                  }) match {
                    case Some((_, line)) =>
                      val bid = BidForPosition(tplayer, participating.GUID, line)
                      AddInvite(participating.Leader.CharId, bid) match {
                        case out @ Some(_) if out.contains(bid) =>
                          HandleBidForPosition(bid, tplayer)
                        case _ => ;
                      }
                    case _ => ;
                  }
                  None
                }
              case None =>
                val squad = StartSquad(invitingPlayer)
                squad.Task = s"${tplayer.Name}'s Squad"
                SquadEvents.publish(SquadServiceResponse(s"/$invitingPlayer/Squad", SquadResponse.InitSquad(squad.GUID)))
                Some(squad)
            }) match {
              case Some(squad) =>
                squad.Membership.zipWithIndex.find({ case (member, index) =>
                  ValidOpenSquadPosition(squad, index, member, tplayer.Certifications)
                }) match {
                  case Some((_, line)) =>
                    SquadEvents.publish( SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitedPlayer, Some(invitingPlayerCharId), "", true, Some(None))) )
                    JoinSquad(tplayer, squad, line)
                    SquadEvents.publish( SquadServiceResponse(s"/$invitingPlayerCharId/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, invitingPlayerCharId, Some(invitedPlayer), tplayer.Name, false, Some(None))) )
                    RemoveQueuedInvites(invitedPlayer) //TODO deal with these somehow
                  case _ => ;
                }
              case None => ;
            }

          case None =>
            //the invite either timed-out or was withdrawn; select a new one?
            NextInvite(invitedPlayer) match {
              case Some(bid : BidForPosition) if !acceptedInvite.contains(bid) =>
                HandleBidForPosition(bid, tplayer)
              case Some(bid) if !acceptedInvite.contains(bid) =>
                SquadEvents.publish(SquadServiceResponse(s"/$invitedPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, bid.InviterCharId, Some(invitedPlayer), bid.InviterName, false, Some(None))))
              case None => ;
            }
        }

      case SquadAction.Membership(SquadRequestType.Leave, leavingPlayer, optionalPlayer, _, _) =>
        val squad = memberToSquad(leavingPlayer)
        val leader = squad.Leader.CharId
        if(leavingPlayer == leader || squad.Size == 2) {
          //squad leader is leaving his own squad, so it will be disbanded
          //alternately, squad is only composed of two people, so it will be closed-out when one of them leaves
          val membership = squad.Membership.collect { case member if member.CharId > 0 => member.CharId }
          membership.foreach { charId =>
            SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, charId, None, "", false, Some(None))))
          }
          CloseOutSquad(squad)
          SquadEvents.publish(SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, leader, None, "", true, Some(None))))
          membership
            .filterNot(_ == leader)
            .foreach { charId =>
            SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", false, Some(None))))
          }
          SquadEvents.publish( SquadServiceResponse(s"/$leader/Squad", SquadResponse.InitSquad(PlanetSideGUID(0))) )
          SquadEvents.publish( SquadServiceResponse(s"/$leader/Squad", SquadResponse.Detail(PlanetSideGUID(0), SquadDetail().Complete)) )
        }
        else {
          if(optionalPlayer.contains(leavingPlayer)) {
            //leaving the squad of own accord
            LeaveSquad(tplayer, squad)
          }
          else if(optionalPlayer.contains(leader)) {
            //kicked by the squad leader
            SquadEvents.publish( SquadServiceResponse(s"/$leavingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, leavingPlayer, Some(leader), tplayer.Name, false, Some(None))) )
            SquadEvents.publish( SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, leader, Some(leavingPlayer), "", true, Some(None))) )
            LeaveSquad(tplayer, squad)
          }
        }

      case SquadAction.Membership(SquadRequestType.Reject, rejectingPlayer, optionalPlayer, _, _) =>
        val rejectedBid = RemoveInvite(rejectingPlayer)
        //(A, B) -> person who made the rejection, person who was rejected
        (rejectedBid match {
          case Some(SpontaneousInvite(invitingPlayer)) =>
            //rejectingPlayer is the would-be squad member
            (Some(rejectingPlayer), Some(invitingPlayer.CharId))
          case Some(VacancyInvite(invitingPlayer, _, guid)) if idToSquad(guid).Leader.CharId != rejectingPlayer =>
            //rejectingPlayer is the would-be squad member
            (Some(rejectingPlayer), Some(invitingPlayer))
          case Some(BidForPosition(_, guid, _)) if idToSquad(guid).Leader.CharId != rejectingPlayer =>
            //rejectingPlayer is the squad leader
            (Some(rejectingPlayer), None)
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
        NextInvite(rejectingPlayer) match {
          case Some(bid : BidForPosition) if rejectedBid.isEmpty || !rejectedBid.contains(bid) =>
            HandleBidForPosition(bid, tplayer)
          case Some(bid) if rejectedBid.isEmpty || !rejectedBid.contains(bid) =>
            SquadEvents.publish(SquadServiceResponse(s"/$rejectingPlayer/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, bid.InviterCharId, Some(rejectingPlayer), bid.InviterName, false, Some(None))))
          case None => ;
        }

      case SquadAction.Membership(SquadRequestType.Promote, promotingPlayer, Some(promotedPlayer), _, _) =>
        (memberToSquad.get(promotingPlayer), memberToSquad.get(promotedPlayer)) match {
          case (Some(squad), Some(squad2)) if squad.GUID == squad2.GUID && squad.Leader.CharId == promotingPlayer =>
            val membership = squad.Membership.filter { _member => _member.CharId > 0 }
            val (leader, position) = (squad.Leader, 0)
            val (member, index) = membership.zipWithIndex.find { case (_member, _) => _member.CharId == promotedPlayer }.get
            SwapMemberPosition(squad, leader, member)
            log.info(s"Promoting player ${leader.Name} to be the leader of ${squad.Task}")
            membership.foreach { _member =>
              SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.PromoteMember(squad, promotedPlayer, index, position)))
            }
            SquadEvents.publish(SquadServiceResponse(s"/$promotingPlayer/Squad", SquadResponse.InitSquad(PlanetSideGUID(0))))
            SquadEvents.publish(SquadServiceResponse(s"/$promotedPlayer/Squad", SquadResponse.InitSquad(squad.GUID)))
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

      case SquadAction.Definition(tplayer : Player, zone_ordinal_number : Int, guid : PlanetSideGUID, line : Int, action : SquadAction) =>
        import net.psforever.packet.game.SquadAction._
        val pSquadOpt = GetParticipatingSquad(tplayer)
        val lSquadOpt = GetLeadingSquad(tplayer, zone_ordinal_number, pSquadOpt)
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
                sender ! SquadServiceResponse("", SquadResponse.InitSquad(squad.GUID))
                UpdateSquadList(squad, SquadInfo().Task(squad.Task).ZoneId(PlanetSideZoneID(squad.ZoneId)).Capacity(squad.Capacity))
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

          case ChangeSquadZone(zone) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed squad's ops zone to $zone")
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.ZoneId = zone.zoneId.toInt
            UpdateSquadListWhenListed(squad, SquadInfo().ZoneId(zone))
            UpdateSquadDetail(squad.GUID, squad, SquadDetail().ZoneId(zone))

          case CloseSquadMemberPosition(position) =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.Availability.lift(position) match {
              case Some(true) =>
                squad.Availability.update(position, false)
                log.info(s"${tplayer.Name}-${tplayer.Faction} has closed the #$position position in squad")
                val memberPosition = squad.Membership(position)
                val listingChanged = if(memberPosition.Name.nonEmpty) {
                  SquadInfo().Size(squad.Size).Capacity(squad.Capacity)
                }
                else {
                  SquadInfo().Capacity(squad.Capacity)
                }
                memberPosition.Close()
                UpdateSquadListWhenListed(squad, listingChanged)
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

          case RequestListSquad() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            if(!squad.Listed && squad.Task.nonEmpty && squad.ZoneId > 0) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has opened public recruitment for squad ${squad.Task}")
              squad.Listed = true
              sender ! SquadServiceResponse("", SquadResponse.InitSquad(squad.GUID))
              UpdateSquadList(squad, None)
            }

          case StopListSquad() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            if(squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has closed public recruitment for squad ${squad.Task}")
              squad.Listed = false
              sender ! SquadServiceResponse("", SquadResponse.InitSquad(PlanetSideGUID(0)))
              UpdateSquadList(squad, None)
            }

          case ResetAll() =>
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
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
            UpdateSquadDetail(squad.GUID, squad)

          case _ =>
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
            idToSquad.get(guid) match {
              case Some(squad) =>
                val toMember = squad.Membership(position)
                if(ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
                  //we could join but we may need permission from the squad leader first
                  log.info(s"Player ${tplayer.Name} would like to join the squad ${squad.Task}.")
                  val leader = squad.Leader
                  val bid = BidForPosition(tplayer, guid, position)
                  val leaderCharId = leader.CharId
                  AddInvite(leaderCharId, bid) match {
                    case out @ Some(_) if out.contains(bid) =>
                      HandleBidForPosition(bid, tplayer)
                    case _ => ;
                  }
                }
              case None => ;
              //squad does not exist? assume old local data; force update to correct discrepancy
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

          //the following action can be performed by anyone
          case (_, DisplaySquad()) =>
            idToSquad.get(guid) match {
              case Some(squad) =>
                viewDetails(tplayer.CharId) = guid
                sender ! SquadServiceResponse("", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
              case None => ;
            }

          //the following message is feedback from a specific client, awaiting proper initialization
          case (_, DisplayFullSquad()) =>
            idToSquad.get(guid) match {
              case Some(squad) =>
                sender ! SquadServiceResponse("", SquadResponse.InitSquad(squad.GUID))
              case None => ;
            }

          case _ => ;
        }

      case msg =>
        log.info(s"Unhandled message $msg from $sender")
    }
  }

  /**
    *
    * @param invitedPlayer
    * @param bid
    * @return an optional invite;
    *         if added to the active invite position, return the parameter bid;
    *         if added to the queued invite, return the invite in the active position;
    *         if not added, return `None`
    */
  def AddInvite(invitedPlayer : Long, bid : Invitation) : Option[Invitation] = {
    invites.get(invitedPlayer) match {
      case Some(_bid) =>
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            if(_bid.InviterCharId != bid.InviterCharId && !bidList.exists { eachBid => eachBid.InviterCharId == bid.InviterCharId }) {
              log.debug(s"Invite from ${bid.InviterCharId} to $invitedPlayer stored in queue while active invite request pending")
              queuedInvites(invitedPlayer) = bid match {
                case _: BidForPosition =>
                  val (normals, others) = bidList.partition(_.isInstanceOf[BidForPosition])
                  (normals :+ bid) ++ others
                case _ =>
                  bidList :+ bid
              }
              None
            }
            else {
              Some(_bid)
            }
          case None =>
            if(_bid.InviterCharId != bid.InviterCharId) {
              log.debug(s"Invite from ${bid.InviterCharId} to $invitedPlayer stored while active invite request pending")
              queuedInvites(invitedPlayer) = List[Invitation](bid)
            }
            Some(_bid)
        }

      case None =>
        invites(invitedPlayer) = bid
        Some(bid)
    }
  }

  def RemoveInvite(invitedPlayer : Long) : Option[Invitation] = {
    invites.remove(invitedPlayer)
  }

  def RemoveQueuedInvites(invitedPlayer : Long) : List[Invitation] = {
    queuedInvites.remove(invitedPlayer) match {
      case Some(_bidList) => _bidList.toList
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

  def HandleBidForPosition(bid : BidForPosition, player : Player) : Unit = {
    idToSquad.get(bid.squad_guid) match {
      case Some(squad) =>
        val leaderCharId = squad.Leader.CharId
        if(squad.AutoApproveInvitationRequests) {
          self ! SquadServiceMessage(player, SquadAction.Membership(SquadRequestType.Accept, leaderCharId, None, "", None))
        }
        else {
          SquadEvents.publish(SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.WantsSquadPosition(bid.player.Name)))
        }
      case _ =>
        //squad is missing; will this properly short-circuit?
        log.error(s"Attempted to process ${bid.InviterName}'s bid for a position in a squad (id:${bid.squad_guid.guid}) that does not exist")
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
          }
        //fully update for all users
        UpdateSquadDetail(squad.GUID, squad)
      }
      else {
        //joining an active squad; everybody updates differently
        //new member gets full UI updates
        val indices = squad.Membership.zipWithIndex
          .collect({ case (member, index) if member.CharId != 0 => index }).toList
        SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Join(squad, indices)))
        //other squad members see us joining the squad
        val updatedIndex = List(line)
        squad.Membership
          .filterNot { member => member.CharId == 0 || member.CharId == charId }
          .foreach { member =>
            SquadEvents.publish(SquadServiceResponse(s"/${member.CharId}/Squad", SquadResponse.Join(squad, updatedIndex)))
          }
        UpdateSquadDetail(squad.GUID, squad,
          SquadDetail().Members(List(SquadPositionEntry(line, SquadPositionDetail().CharId(charId).Name(player.Name))))
        )
      }
      UpdateSquadListWhenListed(squad, SquadInfo().Size(size))
      true
    }
    else {
      false
    }
  }

  def LeaveSquad(player : Player, squad : Squad) : Boolean = {
    val charId = player.CharId
    val membership = squad.Membership.zipWithIndex
    membership.find { case (_member, _) => _member.CharId == charId } match {
      case Some((member, index)) =>
        val updateList = membership.collect({ case (_member, _index) if _member.CharId > 0 => (_member.CharId, _index) }).toList
        //member leaves the squad completely
        memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0
        sender ! SquadServiceResponse("", SquadResponse.Leave(squad, updateList))
        //other squad members see the member leaving
        val leavingMember = List((charId, index))
        membership
          .filter { case (_member, _) => _member.CharId > 0 }
          .foreach { case (_member, _) =>
            SquadEvents.publish( SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.Leave(squad, leavingMember)) )
          }
        true
      case None =>
        false
    }
  }

  def CloseOutSquad(squad : Squad) : Unit = {
    val membership = squad.Membership.zipWithIndex
    CloseOutSquad(
      squad,
      membership,
      membership.collect({ case (_member, _index) if _member.CharId > 0 => (_member.CharId, _index) }).toList
    )
  }

  def CloseOutSquad(squad : Squad, membership : Iterable[(Member, Int)], updateList : List[(Long, Int)]) : Unit = {
    val leaderCharId = squad.Leader.CharId
    membership.foreach {
      case (member, _) =>
        val charId = member.CharId
        member.Name = ""
        member.CharId = 0L
        memberToSquad.remove(charId)
        SquadEvents.publish( SquadServiceResponse(s"/$charId/Squad", SquadResponse.Leave(squad, updateList)) )
    }
    idToSquad.remove(squad.GUID)
    UpdateSquadList(squad, None)
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

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    UpdateSquadDetail(guid, squad, SquadService.Detail.Publish(squad))
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad, detail : SquadDetail) : Unit = {
    val output = SquadResponse.Detail(guid, detail)
    squad.Membership
      .filter { _.CharId > 0L }
      .foreach { member =>
        SquadEvents.publish(SquadServiceResponse(s"/${member.CharId}/Squad", output))
      }
  }
}

object SquadService {
  abstract class Invitation(char_id : Long, name : String) {
    def InviterCharId : Long = char_id
    def InviterName : String = name
  }

  final case class BidForPosition(player : Player, squad_guid : PlanetSideGUID, position : Int)
    extends Invitation(player.CharId, player.Name)

  final case class VacancyInvite(char_id : Long, name : String, squad_guid : PlanetSideGUID)
    extends Invitation(char_id, name)

  final case class SpontaneousInvite(player : Player)
    extends Invitation(player.CharId, player.Name)

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
