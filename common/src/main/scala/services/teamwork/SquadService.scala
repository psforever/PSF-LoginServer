// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.Actor
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
  private var i : Int = 1
  private val publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
    PlanetSideEmpire.TR -> ListBuffer.empty,
    PlanetSideEmpire.NC -> ListBuffer.empty,
    PlanetSideEmpire.VS -> ListBuffer.empty
  )
  private val bids : mutable.LongMap[PositionBid] = mutable.LongMap[PositionBid]()

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

  def GetParticipatingSquad(player : Player, zone : Int) : Option[Squad] = {
    memberToSquad.get(player.CharId) match {
      case opt @ Some(squad) =>
        squad.Membership.find(_.Name == player.Name).get.ZoneId = zone
        opt
      case None =>
        None
    }
  }

  def GetLeadingSquad(player : Player, zone : Int, opt : Option[Squad]) : Squad = {
    val charId = player.CharId
    val squadOut = opt match {
      case Some(squad) =>
        if(squad.Leader.CharId == charId) {
          squad
        }
        else {
          GetLeadingSquad(player, zone, None)
        }

      case None =>
        memberToSquad.get(charId) match {
          case Some(squad) if squad.Leader.CharId.equals(charId) =>
            squad
          case _ =>
            val faction = player.Faction
            val id = GetNextSquadId()
            val squad = new Squad(id, faction)
            val leadPosition = squad.Membership(squad.LeaderPositionIndex)
            val name = player.Name
            leadPosition.Name = name
            leadPosition.CharId = charId
            leadPosition.Health = player.Health
            leadPosition.Armor = player.Armor
            leadPosition.Position = player.Position
            leadPosition.ZoneId = zone
            log.info(s"$name-$faction has started a new squad")
            memberToSquad += charId -> squad
            idToSquad += id -> squad
            squad
        }
    }
    squadOut.Membership(squadOut.LeaderPositionIndex).ZoneId = zone
    squadOut
  }

  val SquadEvents = new GenericEventBus[SquadServiceResponse]

  def receive : Receive = {
    //subscribe to a faction's channel - necessary to receive updates about listed squads
    case Service.Join(faction) if "TRNCVS".indexOf(faction) > -1 =>
      val path = s"$faction/Squad"
      val who = sender()
      log.info(s"$who has joined $path")
      SquadEvents.subscribe(who, path)
      //send initial squad catalog
      sender ! SquadServiceResponse(s"$faction/Squad", SquadResponse.InitList(publishedLists(PlanetSideEmpire(faction)).toVector))

    //subscribe to the player's personal channel - necessary only to inform about any previous squad association
    case Service.Join(char_id) =>
      val path = s"$char_id/Squad"
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
          (optional_char_id, memberToSquad.get(char_id)) match {
            case (Some(toCharId), Some(squad)) =>
              bids(toCharId) = VacancyBid(char_id, squad.GUID)
              SquadEvents.publish( SquadServiceResponse(s"$toCharId/Squad", SquadResponse.Invite(char_id, toCharId, tplayer.Name)) )
            case (Some(toCharId), None) =>
              val ourSquad = GetLeadingSquad(tplayer, 1, None)
              memberToSquad.remove(char_id)
              bids(toCharId) = CamraderieBid(char_id, ourSquad)
              SquadEvents.publish( SquadServiceResponse(s"$toCharId/Squad", SquadResponse.Invite(char_id, toCharId, tplayer.Name)) )
            case _ => ;
          }

        case SquadRequestType.Leave =>
          val squad = memberToSquad(char_id)
          if(optional_char_id.contains(char_id)) {
            //leaving the squad of our own accord
          }
          else {
            val leader = squad.Leader
            if(optional_char_id.contains(leader.CharId)) {
              //kicked by the squad leader
            }
          }
          val membership = squad.Membership.zipWithIndex
          val (member, index) = membership
            .find { case (_member, _) => _member.CharId == char_id }
            .get
          val updateList = membership.collect({ case (_member, _index) if _member.CharId > 0 => (_member.CharId, _index) }).toList
          memberToSquad.remove(char_id)
          member.Name = ""
          member.CharId = 0

          val size = squad.Size
          if(size == 1) {
            //squad is rendered to just one person; collapse it
            (membership.collect({ case (_member, _) if _member.CharId > 0 => _member.CharId }) :+ char_id)
              .foreach { charId =>
                SquadEvents.publish( SquadServiceResponse(s"$charId/Squad", SquadResponse.Leave(squad, updateList)) )
                memberToSquad.remove(charId)
              }
            idToSquad.remove(squad.GUID)
          }
          else {
            //squad continues, despite player's parting
            //member leaves the squad completely
            sender ! SquadServiceResponse("", SquadResponse.Leave(squad, updateList))
            //other squad members see the member leaving only
            val leavingMember = List((char_id, index))
            membership
              .collect({ case (_member, _) if _member.CharId > 0 => _member.CharId })
              .foreach { charId =>
                SquadEvents.publish( SquadServiceResponse(s"$charId/Squad", SquadResponse.Leave(squad, leavingMember)) )
              }
          }

        case SquadRequestType.Accept =>
          bids.remove(char_id) match {
            case Some(NormalBid(inviterCharId, squadGUID, line)) if idToSquad.get(squadGUID).nonEmpty =>
              JoinSquad(tplayer, idToSquad(squadGUID), line)

            case Some(VacancyBid(inviterCharId, squadGUID)) if idToSquad.get(squadGUID).nonEmpty =>
              val squad = idToSquad(squadGUID)
              squad.Membership.zipWithIndex.find({ case (member, index) =>
                ValidOpenSquadPosition(squad, index, squad.Membership(index), tplayer.Certifications)
              }) match {
                case Some((_, line)) =>
                  JoinSquad(tplayer, squad, line)
                case _ => ;
              }

            case Some(CamraderieBid(inviterCharId, placeholderSquad)) =>
              (GetParticipatingSquad(tplayer, 1) match {
                case Some(participating) =>
                  if(participating.Leader.CharId == inviterCharId) {
                    Some(participating)
                  }
                  else {
                    //inviter is not leader of squad; bounce this request off of the squad leader
                    val leaderCharId = participating.Leader.CharId
                    bids(char_id) = VacancyBid(leaderCharId, participating.GUID) //reframed request
                    //TODO squad leader receives "<player> wants to join squad" prompt
                    //SquadEvents.publish(SquadServiceResponse(s"$leaderCharId/Squad", SquadResponse.Invite(char_id, leaderCharId, tplayer.Name)))
                    None
                  }
                case None =>
                  placeholderSquad.Task = s"${tplayer.Name}'s Squad"
                  memberToSquad(inviterCharId) = placeholderSquad
                  SquadEvents.publish(SquadServiceResponse(s"$inviterCharId/Squad", SquadResponse.InitSquad(placeholderSquad.GUID)))
                  Some(placeholderSquad)
              }) match {
              case Some(squad) =>
                squad.Membership.zipWithIndex.find({ case (member, index) =>
                  ValidOpenSquadPosition(squad, index, squad.Membership(index), tplayer.Certifications)
                }) match {
                  case Some((_, line)) =>
                    JoinSquad(tplayer, squad, line)
                  case _ => ;
                }
              case None => ;
              }

            case _ => ;
          }

        case _ => ;
      }

      case SquadAction.Update(char_id, health, max_health, armor, max_armor, pos, zone_number) =>
        memberToSquad.get(char_id) match {
          case Some(squad) =>
            squad.Membership.find(_.CharId == char_id) match {
              case Some(member) =>
                val newHealth = StatConverter.Health(health, max_health, min=1, max=64)
                val newArmor = StatConverter.Health(armor, max_armor, min=1, max=64)
                member.Health = newHealth
                member.Armor = newArmor
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
        val squadOpt = GetParticipatingSquad(tplayer, zone_ordinal_number)
        action match {
          case SaveSquadFavorite() =>
            squadOpt match {
              case Some(squad) if squad.Leader.CharId == tplayer.CharId && squad.Task.nonEmpty && squad.ZoneId > 0 =>
                tplayer.SquadLoadouts.SaveLoadout(squad, squad.Task, line)
                sender ! SquadServiceResponse("", SquadResponse.ListSquadFavorite(line, squad.Task))
              case _ => ;
            }

          case LoadSquadFavorite() =>
            (squadOpt match {
              case Some(squad) if squad.Size == 1 =>
                //we are the leader of our own squad, but no one else has joined yet
                Some(squad)
              case None => ;
                //we are not yet member of a squad; start a squad to support our favorite definition
                Some(GetLeadingSquad(tplayer, zone_ordinal_number, None))
              case _ => ;
                //player is member of populated squad; should not overwrite squad definition with favorite
                None
            }, tplayer.SquadLoadouts.LoadLoadout(line)) match {
              case (Some(squad : Squad), Some(loadout : SquadLoadout)) =>
                log.info(s"${tplayer.Name} is loading a squad composition: $loadout")
                SquadService.LoadSquadDefinition(squad, loadout)
                sender ! SquadServiceResponse("", SquadResponse.InitSquad(squad.GUID))
                UpdateSquadList(squad, SquadInfo().Task(squad.Task).ZoneId(PlanetSideZoneID(squad.ZoneId)).Capacity(squad.Capacity))
                UpdateSquadDetail(PlanetSideGUID(0), squad)

              case _ => ;
            }

          case DeleteSquadFavorite() =>
            tplayer.SquadLoadouts.DeleteLoadout(line)
            sender ! SquadServiceResponse("", SquadResponse.ListSquadFavorite(line, ""))

          case ChangeSquadPurpose(purpose) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed his squad's task to $purpose")
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Task = purpose

            UpdateSquadListWhenListed(squad, SquadInfo().Task(purpose))
            SquadDetail().Task(purpose)
            UpdateListedSquadDetail(squad.GUID, squad)

          case ChangeSquadZone(zone) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed squad's ops zone to $zone")
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.ZoneId = zone.zoneId.toInt
            UpdateSquadListWhenListed(squad, SquadInfo().ZoneId(zone))
            SquadDetail().ZoneId(zone)
            UpdateListedSquadDetail(squad.GUID, squad)

          case CloseSquadMemberPosition(position) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
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
                SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Closed)))
                UpdateListedSquadDetail(squad.GUID, squad)
              case Some(false) | None => ;
            }

          case AddSquadMemberPosition(position) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(false) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has opened the #$position position in squad")
                squad.Availability.update(position, true)
                UpdateSquadListWhenListed(squad, SquadInfo().Capacity(squad.Capacity))
                SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Open)))
                UpdateListedSquadDetail(squad.GUID, squad)
              case Some(true) | None => ;
            }

          case ChangeSquadMemberRequirementsRole(position, role) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the role of squad position #$position")
                squad.Membership(position).Role = role
                SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Role(role))))
                UpdateListedSquadDetail(squad.GUID, squad)
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the orders for squad position #$position")
                squad.Membership(position).Orders = orders
                SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().DetailedOrders(orders))))
                UpdateListedSquadDetail(squad.GUID, squad)
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsCertifications(position, certs) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the requirements for squad position #$position")
                squad.Membership(position).Requirements = certs
                SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Requirements(certs))))
                UpdateListedSquadDetail(squad.GUID, squad)
              case Some(false) | None => ;
            }

          case LocationFollowsSquadLead(state) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            if(state) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has moves the rally to the leader's position")
            }
            else {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has let the rally move freely")
            }
            squad.LocationFollowsSquadLead = state

          case AutoApproveInvitationRequests(state) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            if(state) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} is allowing all requests to join the squad")
            }
            else {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has started screening invitation requests")
            }
            squad.AutoApproveInvitationRequests = state

          case SelectRoleForYourself(position) =>
            //TODO need to ask permission from the squad leader, unless our character is the squad leader or already currently in the squad
            squadOpt match {
              case Some(squad) if squad.GUID == guid || squad.Leader.Name == tplayer.Name =>
                //already a member of this squad, or the leader; swap positions freely
                val membership = squad.Membership.zipWithIndex
                val toMember = squad.Membership(position)
                if(ValidOpenSquadPosition(squad, position, toMember, tplayer.Certifications)) {
                  //acquire this membership position
                  membership.find { case (member, _) => member.Name == tplayer.Name } match {
                    case Some((fromMember, fromIndex)) =>
                      //copy member details
                      toMember.Name = fromMember.Name
                      toMember.CharId = fromMember.CharId
                      toMember.Health = fromMember.Health
                      toMember.Armor = fromMember.Armor
                      toMember.Position = fromMember.Position
                      toMember.ZoneId = fromMember.ZoneId
                      //old membership no longer valid
                      fromMember.Name = ""
                      fromMember.CharId = 0L
                      if(fromIndex == squad.LeaderPositionIndex) {
                        squad.LeaderPositionIndex = position
                      }
                      membership
                        .collect({ case (_member, _) if _member.CharId > 0 => _member.Name })
                        .foreach { name =>
                          SquadEvents.publish(SquadServiceResponse(s"$name/Squad", SquadResponse.SwapMember(squad, fromIndex, position)))
                        }
                    case None =>
                      toMember.Name = tplayer.Name
                      toMember.CharId = tplayer.CharId
                      toMember.Health = StatConverter.Health(tplayer.Health, tplayer.MaxHealth, min = 1, max = 64)
                      toMember.Armor = StatConverter.Health(tplayer.Armor, tplayer.MaxArmor, min = 1, max = 64)
                      toMember.Position = tplayer.Position
                      toMember.ZoneId = 13
                      memberToSquad(tplayer.CharId) = squad
                  }
                }
                UpdateSquadDetail(squad.GUID, squad)

              case Some(_) =>
                //not a member of the requesting squad; do nothing
              case None =>
                //not a member of any squad; consider request of joining the target squad
                idToSquad.get(guid) match {
                  case Some(squad) =>
                    val member = squad.Membership(position)
                    if(squad.Availability(position) && member.CharId == 0 &&
                      tplayer.Certifications.intersect(member.Requirements) == member.Requirements) {
                      bids(tplayer.CharId) = NormalBid(squad.Leader.CharId, guid, position)
                      val leader = squad.Leader
                      //TODO need to ask permission from the squad leader, unless auto-approve is in effect
                      sender ! SquadServiceResponse("", SquadResponse.Membership(SquadResponseType.Invite, 0, 0, leader.CharId, Some(tplayer.CharId), leader.Name, false, None))
                    }

                  case None =>
                  //squad does not exist!? assume old data
                  //reload squad list data and blank the squad definition the user is looking at
                }
            }

          case RequestListSquad() =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            if(!squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has opened public recruitment for squad ${squad.Task}")
              squad.Listed = true
              sender ! SquadServiceResponse("", SquadResponse.InitSquad(squad.GUID))
              UpdateSquadList(squad, None)
              sender ! SquadServiceResponse("", SquadResponse.Unknown17(squad, tplayer.CharId))
            }

          case StopListSquad() =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            if(!squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has closed public recruitment for squad ${squad.Task}")
              squad.Listed = false
              sender ! SquadServiceResponse("", SquadResponse.InitSquad(PlanetSideGUID(0)))
              UpdateSquadList(squad, None)
            }

          case ResetAll() =>
            squadOpt match {
              case Some(squad) if squad.Leader.CharId == tplayer.CharId =>
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
                UpdateSquadList(squad, SquadInfo().Task("").ZoneId(None).Capacity(squad.Capacity))
                UpdateSquadDetail(squad.GUID, squad)
              case None => ;
            }

          case DisplaySquad() =>
            idToSquad.get(guid) match {
              case Some(squad) =>
                sender ! SquadServiceResponse(s"${tplayer.Name}/Squad", GenSquadDetail(squad.GUID, squad))
              case None => ;
            }

//          case AnswerSquadJoinRequest() =>
//            idToSquad.get(guid) match {
//              case Some(squad) =>
//                if(squad.Leader.Name == tplayer.Name && squad.Listed) {
//                  //squad was just listed but we have not yet declared ourselves as the leader
//                  UpdateSquadDetail(squad)
//                }
//              case None => ;
//            }

          case _ => ;
        }

      case msg =>
        log.info(s"Unhandled message $msg from $sender")
    }
  }

  def JoinSquad(player : Player, squad : Squad, line : Int) : Boolean = {
    val charId = player.CharId
    val position = squad.Membership(line)
    if(squad.Availability(line) && position.CharId == 0 &&
      player.Certifications.intersect(position.Requirements) == position.Requirements) {
      position.Name = player.Name
      position.CharId = charId
      position.Health = StatConverter.Health(player.Health, player.MaxHealth, min=1, max=64)
      position.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min=1, max=64)
      position.Position = player.Position
      position.ZoneId = 13
      memberToSquad(player.CharId) = squad

      val size = squad.Size
      if(size == 1) {
        //leader joins the squad? do nothing?
        squad.LeaderPositionIndex = line
      }
      else if(size == 2) {
        //first squad member after leader; both members fully initialize
        val indices = squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
        squad.Membership
          .collect({ case member if member.CharId != 0 => member.CharId })
          .foreach { charId =>
            SquadEvents.publish(SquadServiceResponse(s"$charId/Squad", SquadResponse.Join(squad, indices)))
          }
      }
      else {
        //joining an active squad; everybody updates differently
        //new member gets full UI updates
        sender ! SquadServiceResponse("", SquadResponse.Join(
          squad,
          squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
        ))
        //other squad members see us joining the squad
        val updatedIndex = List(line)
        squad.Membership
          .collect({ case member if member.CharId != 0 && member.CharId != charId => member.CharId })
          .foreach { charId =>
            SquadEvents.publish(SquadServiceResponse(s"$charId/Squad", SquadResponse.Join(squad, updatedIndex)))
          }
      }
      true
    }
    else {
      false
    }
  }

  def UpdateSquadList(squad : Squad, changes : SquadInfo) : Unit = {
    UpdateSquadList(squad, Some(changes))
  }

  def UpdateSquadListWhenListed(squad : Squad, changes : SquadInfo) : Unit = {
    UpdateSquadListWhenListed(squad, Some(changes))
  }

  def UpdateSquadListWhenListed(squad : Squad, changes : Option[SquadInfo]) : Unit = {
    if(squad.Listed) {
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
              SquadServiceResponse(s"$faction/Squad", SquadResponse.UpdateList(Seq((index, changedFields))))
            )
          case None =>
            //remove squad from listing
            log.info(s"Squad will be removed")
            factionListings.remove(index)
            SquadEvents.publish(
              //SquadServiceResponse(s"$faction/Squad", SquadResponse.RemoveFromList(Seq(index)))
              SquadServiceResponse(s"$faction/Squad", SquadResponse.InitList(factionListings.toVector))
            )
        }
      case None =>
        //first time being published
        log.info(s"Squad will be introduced")
        factionListings += entry
        SquadEvents.publish(
          SquadServiceResponse(s"$faction/Squad", SquadResponse.InitList(factionListings.toVector))
        )
    }
  }

  def GenSquadDetail(guid : PlanetSideGUID, squad : Squad) : SquadResponse.Detail = {
    SquadResponse.Detail(
      squad.GUID,
      squad.Leader.Name,
      squad.Task,
      PlanetSideZoneID(squad.ZoneId),
      squad.Membership.zipWithIndex.map({ case (p, index) =>
        if(squad.Availability(index)) {
          SquadPositionDetail(p.Role, p.Orders, p.Requirements, p.CharId, p.Name)
        }
        else {
          SquadPositionDetail.Closed
        }
      }).toList
    )
  }

  def UpdateListedSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    if(squad.Listed) {
      UpdateSquadDetail(guid, squad)
    }
  }

  def UpdateSquadDetail(guid : PlanetSideGUID, squad : Squad) : Unit = {
    val detail = GenSquadDetail(guid, squad)
    squad.Membership
      .collect { case member if member.CharId > 0L => member.CharId }
      .foreach { charId =>
        SquadEvents.publish(SquadServiceResponse(s"$charId/Squad", detail))
      }
  }
}

object SquadService {
  trait PositionBid

  final case class NormalBid(char_id : Long, squad_guid : PlanetSideGUID, position : Int) extends PositionBid

  final case class VacancyBid(char_id : Long, squad_guid : PlanetSideGUID) extends PositionBid

  final case class CamraderieBid(char_id : Long, placeholder : Squad) extends PositionBid

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

    def Differences(updates : List[Int], info : SquadDetail) : SquadDetail = {
      if(updates.nonEmpty) {
        val list = Seq(
          SquadDetail.Blank, //must be index-0
          SquadDetail(info.unk1, None, None, None, None, None, None, None, None),
          SquadDetail(None, None, info.leader_char_id, None, None, None, None, None, None),
          SquadDetail(None, None, None, info.unk3, None, None, None, None, None),
          SquadDetail(None, None, None, None, info.leader_name, None, None, None, None),
          SquadDetail(None, None, None, None, None, info.task, None, None, None),
          SquadDetail(None, None, None, None, None, None, info.zone_id, None, None),
          SquadDetail(None, None, None, None, None, None, None, info.unk7, None),
          SquadDetail(None, None, None, None, None, None, None, None, info.member_info)
        )
        var out = SquadDetail.Blank
        updates
          .map(i => list(i))
          .filterNot { _ == SquadDetail.Blank }
          .foreach(sinfo => out = out And sinfo )
        out
      }
      else {
        SquadDetail.Blank
      }
    }

    def Differences(before : SquadDetail, after : SquadDetail) : SquadDetail = {
      SquadDetail(
        if(!before.unk1.equals(after.unk1)) after.unk1 else None,
        None,
        if(!before.leader_char_id.equals(after.leader_char_id)) after.leader_char_id else None,
        if(!before.unk3.equals(after.unk3)) after.unk3 else None,
        if(!before.leader_name.equals(after.leader_name)) after.leader_name else None,
        if(!before.task.equals(after.task)) after.task else None,
        if(!before.zone_id.equals(after.zone_id)) after.zone_id else None,
        if(!before.unk7.equals(after.unk7)) after.unk7 else None,
        {
          (before.member_info, after.member_info) match {
            case (Some(beforeEntry), Some(afterEntry)) =>
              val out = beforeEntry.zip(afterEntry)
                .map { case (b, a) => PositionEquality(b, a) }
                .collect { case Some(entry) => entry }
              if(out.nonEmpty) {
                Some(out)
              }
              else {
                None
              }
            case _ =>
              None
          }
        }
      )
    }

    private def PositionEquality(before : SquadPositionEntry, after : SquadPositionEntry) : Option[SquadPositionEntry] = {
      (before.info, after.info) match {
        case (Some(binfo), Some(ainfo)) =>
          val out = MemberEquality(binfo, ainfo)
          if(out == SquadPositionDetail.Blank) {
            None
          }
          else {
            Some(SquadPositionEntry(before.index, out))
          }
        case _ =>
          None
      }
    }

    private def MemberEquality(before : SquadPositionDetail, after : SquadPositionDetail) : SquadPositionDetail = {
      SquadPositionDetail(
        if(!before.is_closed.equals(after.is_closed)) after.is_closed else None,
        if(!before.role.equals(after.role)) after.role else None,
        if(!before.detailed_orders.equals(after.detailed_orders)) after.detailed_orders else None,
        if(!before.requirements.equals(after.requirements)) after.requirements else None,
        if(!before.char_id.equals(after.char_id)) after.char_id else None,
        if(!before.name.equals(after.name)) after.name else None
      )
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
    squad.Availability(position) && member.CharId == 0 && reqs.intersect(member.Requirements) == member.Requirements
  }
}
