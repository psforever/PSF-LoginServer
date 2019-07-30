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
  private var i : Int = 1
  private val publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
    PlanetSideEmpire.TR -> ListBuffer.empty,
    PlanetSideEmpire.NC -> ListBuffer.empty,
    PlanetSideEmpire.VS -> ListBuffer.empty
  )
  private val bids : mutable.LongMap[PositionBid] = mutable.LongMap[PositionBid]()
  private val viewDetails : mutable.LongMap[PlanetSideGUID] = mutable.LongMap[PlanetSideGUID]()

  private [this] val log = org.log4s.getLogger

  override def preStart : Unit = {
    log.info("Starting...")

    val testSquad = new Squad(PlanetSideGUID(3), PlanetSideEmpire.VS)
    testSquad.Task = "\\#66CCFF Sentinels of Auraxis\\#FFFFFF ... \\#40FF40 Squad Up!!"
    testSquad.ZoneId = 5
    testSquad.Membership(0).Name = "Wizkid45"
    testSquad.Membership(0).Role = "Fearless Leader"
    testSquad.Membership(0).CharId = 30910985L
    testSquad.Membership(0).ZoneId = 5
    testSquad.Membership(0).Health = 64
    testSquad.Membership(0).Armor = 34
    testSquad.Membership(0).Position = Vector3(5526.5234f, 3818.7344f, 54.59375f)
    testSquad.Membership(1).Name = "xoBLADEox"
    testSquad.Membership(1).Role = "Right Hand"
    testSquad.Membership(1).CharId = 42781919L
    testSquad.Membership(1).ZoneId = 5
    testSquad.Membership(1).Health = 54
    testSquad.Membership(1).Armor = 44
    testSquad.Membership(1).Position = Vector3(4673.5312f, 2604.8047f, 40.015625f)
    testSquad.Membership(3).Name = "cabal0428"
    testSquad.Membership(3).Role = "Left Hand"
    testSquad.Membership(3).CharId = 353380L
    testSquad.Membership(3).ZoneId = 5
    testSquad.Membership(3).Health = 44
    testSquad.Membership(3).Armor = 54
    testSquad.Membership(3).Position = Vector3(4727.492f, 2613.5312f, 51.390625f)
    testSquad.Membership(4).Name = "xSkiku"
    testSquad.Membership(4).Role = "Right Foot's Middle Toe's Nail"
    testSquad.Membership(4).CharId = 41588340L
    testSquad.Membership(4).ZoneId = 5
    testSquad.Membership(4).Health = 34
    testSquad.Membership(4).Armor = 64
    testSquad.Membership(4).Position = Vector3(3675.0f, 4789.8047f, 63.21875f)
    idToSquad(PlanetSideGUID(3)) = testSquad
    testSquad.Listed = true
    UpdateSquadList(testSquad, None)
  }

  def GetNextSquadId() : PlanetSideGUID = {
    val out = i
    val j = i + 1
    if(j == 65536) {
      i = 1
    }
    else {
      i = j
    }
    PlanetSideGUID(out)
  }

  def GetParticipatingSquad(player : Player) : Option[Squad] = {
    memberToSquad.get(player.CharId) match {
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

  def StartSquad(player : Player) : Squad = {
    val charId = player.CharId
    val faction = player.Faction
    val id = GetNextSquadId()
    val name = player.Name
    val squad = new Squad(id, faction)
    val leadPosition = squad.Membership(squad.LeaderPositionIndex)
    leadPosition.Name = name
    leadPosition.CharId = charId
    leadPosition.Health = player.Health
    leadPosition.Armor = player.Armor
    leadPosition.Position = player.Position
    leadPosition.ZoneId = 1
    val switchboard = context.actorOf(Props[SquadSwitchboard], s"squad${id.guid}")
    memberToSquad += charId -> squad
    idToSquad += id -> squad
    idToSwitchboard += id -> switchboard
    log.info(s"$name-$faction has started a new squad")
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
      case SquadAction.Membership(request_type, char_id, optional_char_id, _, _) => request_type match {
        case SquadRequestType.Invite =>
          //char_id is the inviter, e.g., the (prospective) squad leader
          //this is just busy work; for actual joining operations, see SquadRequestType.Accept
          (optional_char_id, memberToSquad.get(char_id)) match {
            case (Some(invitee), Some(squad)) =>
              bids(invitee) = VacancyBid(char_id, squad.GUID)
              log.info(s"$invitee has been invited to squad ${squad.Task} by $char_id")
              SquadEvents.publish( SquadServiceResponse(s"/$invitee/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, char_id, Some(invitee), tplayer.Name, false, Some(None))) )
              SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitee, Some(char_id), tplayer.Name, true, Some(None))) )
            case (Some(invitee), None) =>
              //the inviter does not currently belong to a squad; check for an existing placeholder, or create a new one
              val ourSquad = {
                bids.find { case (inviter, _) => inviter == char_id } match {
                  case Some((_, SpontaneousBid(_, _squad))) =>
                    _squad //borrow
                  case _ =>
                    val _squad = StartSquad(tplayer)
                    memberToSquad.remove(char_id) //completely unlist until assured the squad is necessary
                    _squad
                }
              }
              bids(invitee) = SpontaneousBid(char_id, ourSquad)
              SquadEvents.publish( SquadServiceResponse(s"/$invitee/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, char_id, Some(invitee), tplayer.Name, false, Some(None))) )
              SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, invitee, Some(char_id), tplayer.Name, true, Some(None))) )
            case _ => ;
          }

        case SquadRequestType.Accept =>
          //char_id is the invitee, e.g., the person joining the squad
          bids.remove(char_id) match {
            case Some(NormalBid(_/*inviterCharId*/, squadGUID, line)) if idToSquad.get(squadGUID).nonEmpty =>
              //player requested to join a squad's specific position
              JoinSquad(tplayer, idToSquad(squadGUID), line)

            case Some(VacancyBid(inviterCharId, squadGUID)) if idToSquad.get(squadGUID).nonEmpty =>
              //we were invited by the squad leader into an existing squad
              val squad = idToSquad(squadGUID)
              squad.Membership.zipWithIndex.find({ case (member, index) =>
                ValidOpenSquadPosition(squad, index, member, tplayer.Certifications)
              }) match {
                case Some((_, line)) =>
                  SquadEvents.publish( SquadServiceResponse(s"/$inviterCharId/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, inviterCharId, Some(char_id), tplayer.Name, false, Some(None))) )
                  SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, char_id, Some(inviterCharId), "", true, Some(None))) )
                  JoinSquad(tplayer, squad, line)
                case _ => ;
              }

            case Some(SpontaneousBid(inviterCharId, placeholderSquad)) =>
              //we were invited by someone into a new squad they would form
              (GetParticipatingSquad(tplayer) match {
                case Some(participating) =>
                  if(participating.Leader.CharId == inviterCharId) {
                    Some(participating)
                  }
                  else {
                    //inviter joined a squad and is not its leader; bounce this request off of the squad leader
                    //TODO squad leader receives "<player> wants to join squad" prompt
                    val leaderCharId = participating.Leader.CharId
                    bids(char_id) = VacancyBid(leaderCharId, participating.GUID) //reframed request
                    SquadEvents.publish(SquadServiceResponse(s"/$leaderCharId/Squad", SquadResponse.Invite(char_id, leaderCharId, tplayer.Name)))
                    None
                  }
                case None =>
                  placeholderSquad.Task = s"${tplayer.Name}'s Squad"
                  memberToSquad(inviterCharId) = placeholderSquad
                  SquadEvents.publish(SquadServiceResponse(s"/$inviterCharId/Squad", SquadResponse.InitSquad(placeholderSquad.GUID)))
                  Some(placeholderSquad)
              }) match {
              case Some(squad) =>
                squad.Membership.zipWithIndex.find({ case (member, index) =>
                  ValidOpenSquadPosition(squad, index, member, tplayer.Certifications)
                }) match {
                  case Some((_, line)) =>
                    SquadEvents.publish( SquadServiceResponse(s"/$inviterCharId/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, inviterCharId, Some(char_id), tplayer.Name, false, Some(None))) )
                    SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Accept, 0, 0, char_id, Some(inviterCharId), "", true, Some(None))) )
                    JoinSquad(tplayer, squad, line)
                  case _ => ;
                }
              case None => ;
              }

            case _ => ;
          }

        case SquadRequestType.Leave =>
          //char_id is the player leaving
          val squad = memberToSquad(char_id)
          val leader = squad.Leader.CharId
          if(char_id == leader) {
            //squad leader is leaving his own squad, so it will be disbanded
            squad.Membership
              .filterNot { _.CharId == leader }
              .foreach { member =>
                val charId = member.CharId
                SquadEvents.publish(SquadServiceResponse(s"/$charId/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", false, Some(None))))
              }
            CloseOutSquad(squad)
            SquadEvents.publish(SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, leader, None, "", true, Some(None))))
          }
          else {
            if(optional_char_id.contains(char_id)) {
              //leaving the squad of our own accord
              LeaveSquad(tplayer, squad)
            }
            else if(optional_char_id.contains(leader)) {
              //kicked by the squad leader
              SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, char_id, Some(leader), tplayer.Name, false, Some(None))) )
              SquadEvents.publish( SquadServiceResponse(s"/$leader/Squad", SquadResponse.Membership(SquadResponseType.Leave, 0, 0, leader, Some(char_id), "", true, Some(None))) )
              LeaveSquad(tplayer, squad)
            }
          }

        case SquadRequestType.Reject =>
          //a player has opted out of joining a squad
          (bids.remove(char_id) match {
            case Some(SpontaneousBid(inviterCharId, squad)) if squad.Leader.CharId != char_id =>
              (inviterCharId, squad)
            case Some(VacancyBid(inviterCharId, guid)) if idToSquad(guid).Leader.CharId != char_id =>
              (inviterCharId, idToSquad.get(guid))
            case _ => ;
              (0L, None)
          }) match {
            case (inviterCharId, Some(squad)) =>
              SquadEvents.publish( SquadServiceResponse(s"/$inviterCharId/Squad", SquadResponse.Membership(SquadResponseType.Reject, 0, 0, inviterCharId, Some(char_id), tplayer.Name, false, Some(None))) )
              SquadEvents.publish( SquadServiceResponse(s"/$char_id/Squad", SquadResponse.Membership(SquadResponseType.Reject, 0, 0, char_id, Some(inviterCharId), "", true, Some(None))) )
            case _ => ;
          }

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
            UpdateSquadDetailWhenListed(squad.GUID, squad, SquadDetail().Task(purpose))

          case ChangeSquadZone(zone) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed squad's ops zone to $zone")
            val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
            squad.ZoneId = zone.zoneId.toInt
            UpdateSquadListWhenListed(squad, SquadInfo().ZoneId(zone))
            UpdateSquadDetailWhenListed(squad.GUID, squad, SquadDetail().ZoneId(zone))

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
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
            UpdateSquadDetailWhenListed(squad.GUID, squad)

          case _ =>
        }
        // etc..
        (pSquadOpt, action) match {
          //the following action can be performed by the squad leader and maybe an unaffiliated player
          case (Some(squad), SelectRoleForYourself(position)) =>
            log.info(s"${tplayer.Name} would like the #${position+1} spot in this squad")
            val membership = squad.Membership.zipWithIndex
            val toMember = squad.Membership(position)
            //the squad leader may swap to any open position; a normal member has to validate against requirements
            if((squad.Leader.CharId == tplayer.CharId && toMember.CharId == 0) || ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
              membership.find { case (member, _) => member.CharId == tplayer.CharId } match {
                case Some((fromMember, fromIndex)) =>
                  SwapMemberPosition(squad, toMember, fromMember)
                  if(fromIndex == squad.LeaderPositionIndex) {
                    squad.LeaderPositionIndex = position
                  }
                  membership
                    .filter { case (_member, _) => _member.CharId > 0 }
                    .foreach { case (_member, _) =>
                      SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromIndex, position)))
                    }
                  UpdateSquadDetailWhenListed(squad.GUID, squad)
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
                  bids(tplayer.CharId) = NormalBid(squad.Leader.CharId, guid, position)
                  //val leader = squad.Leader
                  //TODO ask permission from the squad leader, unless auto-approve is in effect
                  self ! SquadServiceMessage(tplayer, SquadAction.Membership(SquadRequestType.Accept, tplayer.CharId, None, "", None))
                }
              case None => ;
              //squad does not exist? assume old local data; force update to correct discrepancy
            }

          //the following action can be performed by ???
          case (Some(squad), AssignSquadMemberToRole(position, char_id)) =>
            val membership = squad.Membership.zipWithIndex
            (membership.find({ case (member, _) => member.CharId == char_id}), membership(position)) match {
              case (Some((fromMember, fromPosition)), (toMember, _)) =>
                val name = fromMember.Name
                SwapMemberPosition(squad, toMember, fromMember)
                //
                membership
                  .filter({ case (_member, _) => _member.CharId > 0 })
                  .foreach { case (_member, _) =>
                    SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.AssignMember(squad, fromPosition, position)))
                  }
                UpdateSquadDetailWhenListed(squad.GUID, squad,
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
                sender ! SquadServiceResponse(s"/${tplayer.CharId}/Squad", SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
              case None => ;
            }

          case _ => ;
        }

      case msg =>
        log.info(s"Unhandled message $msg from $sender")
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
        UpdateSquadDetailWhenListed(squad.GUID, squad,
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
        memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0

        val size = squad.Size
        if(size < 2) {
          //squad is rendered to just one person or less; collapse it
          squad.Membership.foreach { _member =>
              SquadEvents.publish(SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", false, Some(None))))
          }
          CloseOutSquad(squad, membership, updateList)
        }
        else {
          //squad continues, despite player's parting
          //member leaves the squad completely
          sender ! SquadServiceResponse("", SquadResponse.Leave(squad, updateList))
          //other squad members see the member leaving
          val leavingMember = List((charId, index))
          membership
            .filter { case (_member, _) => _member.CharId > 0 }
            .foreach { case (_member, _) =>
              SquadEvents.publish( SquadServiceResponse(s"/${_member.CharId}/Squad", SquadResponse.Leave(squad, leavingMember)) )
            }
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
    val entry = SquadService.SquadList.Publish(squad)
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
            factionListings(index) = entry
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
        factionListings += entry
        SquadEvents.publish(
          SquadServiceResponse(s"/$faction/Squad", SquadResponse.InitList(factionListings.toVector))
        )
    }
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    UpdateSquadDetailWhenListed(guid, squad, SquadService.Detail.Publish(squad))
  }

  def UpdateSquadDetailWhenListed(guid : PlanetSideGUID, squad : Squad) : Unit = {
    if(squad.Listed || squad.Size > 1) {
      UpdateSquadDetail(guid, squad, SquadService.Detail.Publish(squad))
    }
  }

  def UpdateSquadDetailWhenListed(guid : PlanetSideGUID, squad : Squad, detail : SquadDetail) : Unit = {
    if(squad.Listed || squad.Size > 1) {
      UpdateSquadDetail(guid, squad, detail)
    }
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
  trait PositionBid

  final case class NormalBid(char_id : Long, squad_guid : PlanetSideGUID, position : Int) extends PositionBid

  final case class VacancyBid(char_id : Long, squad_guid : PlanetSideGUID) extends PositionBid

  final case class SpontaneousBid(char_id : Long, placeholder : Squad) extends PositionBid

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
