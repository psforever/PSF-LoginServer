// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.objects.teamwork.Squad
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, SquadRequestType, Vector3}
import services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

//import scala.concurrent.duration._

class SquadService extends Actor {
  private var memberToSquad : TrieMap[String, Squad] = new TrieMap[String, Squad]()
  private var idToSquad : TrieMap[PlanetSideGUID, Squad] = new TrieMap[PlanetSideGUID, Squad]()
  private var i : Int = 1
  private val publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
    PlanetSideEmpire.TR -> ListBuffer.empty,
    PlanetSideEmpire.NC -> ListBuffer.empty,
    PlanetSideEmpire.VS -> ListBuffer.empty
  )
  private val bids : mutable.LongMap[(PlanetSideGUID, Int)] = mutable.LongMap[(PlanetSideGUID, Int)]()

  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")

    val testSquad = new Squad(PlanetSideGUID(3), PlanetSideEmpire.VS)
    testSquad.Task = "\\#66CCFF Sentinels of Auraxis\\#FFFFFF ... \\#40FF40 Squad Up!!"
    testSquad.ZoneId = 5
    testSquad.Membership(0).Name = "Wizkid45"
    testSquad.Membership(0).CharId = 30910985L
    testSquad.Membership(0).ZoneId = 5
    testSquad.Membership(0).Position = Vector3(5526.5234f, 3818.7344f, 54.59375f)
    testSquad.Membership(1).Name = "xoBLADEox"
    testSquad.Membership(1).CharId = 42781919L
    testSquad.Membership(1).ZoneId = 5
    testSquad.Membership(1).Position = Vector3(4673.5312f, 2604.8047f, 40.015625f)
    testSquad.Membership(3).Name = "cabal0428"
    testSquad.Membership(3).CharId = 353380L
    testSquad.Membership(3).ZoneId = 5
    testSquad.Membership(3).Position = Vector3(4727.492f, 2613.5312f, 51.390625f)
    testSquad.Membership(4).Name = "xSkiku"
    testSquad.Membership(4).CharId = 41588340L
    testSquad.Membership(4).ZoneId = 5
    testSquad.Membership(4).Position = Vector3(3675.0f, 4789.8047f, 63.21875f)
    idToSquad(PlanetSideGUID(3)) = testSquad
    testSquad.Listed = true
    UpdateSquadList(testSquad, List())
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
    memberToSquad.get(player.Name) match {
      case opt @ Some(squad) =>
        squad.Membership.find(_.Name == player.Name).get.ZoneId = zone
        opt
      case None =>
        None
    }
  }

  def GetLeadingSquad(player : Player, zone : Int, opt : Option[Squad]) : Squad = {
    val name = player.Name
    val squadOut = opt match {
      case Some(squad) =>
        if(squad.Leader.Name.equals(name)) {
          squad
        }
        else {
          GetLeadingSquad(player, zone, None)
        }

      case None =>
        memberToSquad.get(name) match {
          case Some(squad) if squad.Leader.Name.equals(name) =>
            squad
          case _ =>
            val faction = player.Faction
            val id = GetNextSquadId()
            val squad = new Squad(id, faction)
            val leadPosition = squad.Membership(squad.LeaderPositionIndex)
            leadPosition.Name = name
            leadPosition.Health = player.Health
            leadPosition.Armor = player.Armor
            leadPosition.Position = player.Position
            leadPosition.ZoneId = zone
            log.info(s"$name-$faction has started a new squad")
            memberToSquad += name -> squad
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
      sender ! SquadServiceResponse(s"$faction/Squad", SquadResponse.Init(publishedLists(PlanetSideEmpire(faction)).toVector))

    //subscribe to the player's personal channel - necessary only to inform about any previous squad association
    case Service.Join(name) =>
      val path = s"$name/Squad"
      val who = sender()
      log.info(s"$who has joined $path")
      SquadEvents.subscribe(who, path) //TODO squad-specific switchboard
      //check for renewable squad information
      memberToSquad.get(name) match {
        case None => ;
        case Some(_) =>
          sender ! SquadServiceMessage.RecoverSquadMembership() //TODO?
      }

    case Service.Leave(Some(name)) => ;
      SquadEvents.unsubscribe(sender())
      memberToSquad.get(name) match {
        case Some(squad) =>
          if(squad.Leader.Name.equals(name)) {
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
            memberToSquad.remove(name)
            idToSquad.remove(squad.GUID)
            UpdateSquadList(squad, List())
          }
          else {
            //we were just a grunt in the squad
            val position = squad.Membership.find(_.Name == name).get
            position.Name = ""
            position.ZoneId = 0
            position.Position = Vector3.Zero
            position.Health = 0
            position.Armor = 0
            UpdateSquadDetail(squad)
          }
        case None => ;
      }
    //TODO leave squad, if joined to one, and perform clean-up

    case Service.Leave(None) | Service.LeaveAll() => ;

    case SquadServiceMessage(tplayer, squad_action) => squad_action match {
      case SquadAction.Membership(request_type, char_id, optional_char_id, name, unk5) => request_type match {
        case SquadRequestType.Accept =>
          bids.get(char_id) match {
            case Some((squadGUID, line)) if idToSquad.get(squadGUID).nonEmpty =>
              //join squad
              val squad = idToSquad(squadGUID)
              val position = squad.Membership(line)
              if(squad.Availability(line) && position.CharId == 0 &&
                tplayer.Certifications.intersect(position.Requirements) == position.Requirements) {
                position.Name = tplayer.Name
                position.CharId = char_id
                position.Health = tplayer.Health
                position.Armor = tplayer.Armor
                position.Position = tplayer.Position
                position.ZoneId = 13
                memberToSquad(tplayer.Name) = squad
                sender ! SquadServiceResponse("", SquadResponse.Join(
                  squad,
                  squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
                ))
              }
              bids.remove(char_id)
            case _ => ;
          }

        case SquadRequestType.Leave =>
          if(optional_char_id.contains(char_id)) {
            //we're leaving the squad on our own
            val name = tplayer.Name
            val squad = memberToSquad(name)
            val membership = squad.Membership.zipWithIndex
            val (member, index) = membership
              .find { case (_member, _) => _member.Name == name }
              .get
            val updateList = membership.collect({ case (_member, _) if _member.CharId > 0 => (_member.CharId, index) }).toList
            memberToSquad.remove(name)
            member.Name = ""
            member.CharId = 0
            sender ! SquadServiceResponse("", SquadResponse.Leave(squad, updateList))
          }

        case _ => ;
      }

      case SquadAction.Definition(tplayer : Player, zone_ordinal_number : Int, guid : PlanetSideGUID, line : Int, action : SquadAction) =>
        import net.psforever.packet.game.SquadAction._
        val squadOpt = GetParticipatingSquad(tplayer, zone_ordinal_number)
        action match {
          case SaveSquadDefinition() =>

          case ChangeSquadPurpose(purpose) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed his squad's task to $purpose")
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Task = purpose
            UpdateSquadList(squad, List(SquadInfo.Field.Task))
            UpdateSquadDetail(squad)

          case ChangeSquadZone(zone) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed squad's ops zone to $zone")
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.ZoneId = zone.zoneId.toInt
            UpdateSquadList(squad, List(SquadInfo.Field.ZoneId))
            UpdateSquadDetail(squad)

          case CloseSquadMemberPosition(position) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                squad.Availability.update(position, false)
                log.info(s"${tplayer.Name}-${tplayer.Faction} has closed the #$position position in squad")
                val memberPosition = squad.Membership(position)
                val listingChanged = if(memberPosition.Name.nonEmpty) {
                  List(SquadInfo.Field.Size, SquadInfo.Field.Capacity)
                }
                else {
                  List(SquadInfo.Field.Capacity)
                }
                memberPosition.Close()
                UpdateSquadList(squad, listingChanged)
                UpdateSquadDetail(squad)
              case Some(false) | None => ;
            }

          case AddSquadMemberPosition(position) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(false) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has opened the #$position position in squad")
                squad.Availability.update(position, true)
                UpdateSquadList(squad, List(SquadInfo.Field.Capacity))
                UpdateSquadDetail(squad)
              case Some(true) | None => ;
            }

          case ChangeSquadMemberRequirementsRole(position, role) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the role of squad position #$position")
                squad.Membership(position).Role = role
                UpdateSquadDetail(squad)
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the orders for squad position #$position")
                squad.Membership(position).Orders = orders
                UpdateSquadDetail(squad)
              case Some(false) | None => ;
            }

          case ChangeSquadMemberRequirementsCertifications(position, certs) =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the requirements for squad position #$position")
                squad.Membership(position).Requirements = certs
                UpdateSquadDetail(squad)
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
            val name = tplayer.Name
            squadOpt match {
              case Some(squad) if squad.GUID == guid =>
              //already a member of this squad; swap positions freely
              case Some(squad) =>
              //not a member of the requesting squad; do nothing
              case None =>
                //not a member of any squad; consider request of joining the target squad
                idToSquad.get(guid) match {
                  case Some(squad) =>
                    val member = squad.Membership(position)
                    if(squad.Availability(position) && member.CharId == 0 &&
                      tplayer.Certifications.intersect(member.Requirements) == member.Requirements) {
                      bids(tplayer.CharId) = (guid, position)
                      val leader = squad.Leader
                      //TODO need to ask permission from the squad leader, unless auto-approve is in effect
                      sender ! SquadServiceResponse("", SquadResponse.Membership(SquadRequestType.Invite, 0, 0, leader.CharId, Some(tplayer.CharId), leader.Name, false, None))
                    }

                  case None =>
                  //squad does not exist!? assume old data
                  //reload squad list data and blank the squad definition the user is looking at
                }
            }

          case ListSquad() =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
            if(!squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has opened recruitment for this squad")
              squad.Listed = true
            }
            UpdateSquadList(squad, List())

          case ResetAll() =>
            val squad = GetLeadingSquad(tplayer, zone_ordinal_number, squadOpt)
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
            UpdateSquadList(squad, List(SquadInfo.Field.Task, SquadInfo.Field.ZoneId, SquadInfo.Field.Size, SquadInfo.Field.Capacity))
            UpdateSquadDetail(squad)

          case DisplaySquad() =>
            idToSquad.get(guid) match {
              case Some(squad) =>
                sender ! SquadServiceResponse(s"${tplayer.Name}/Squad", GenSquadDetail(squad))
              case None => ;
            }

          case _ => ;
        }

      case msg =>
        log.info(s"Unhandled message $msg from $sender")
    }
  }

  def UpdateSquadList(squad : Squad, listingChanged : List[Int]) : Unit = {
    //queue updates
    if(squad.Listed) {
      val entry = SquadService.Publish(squad)
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
          val changes = if(listingChanged.nonEmpty) {
            SquadService.Differences(listingChanged, entry)
          }
          else {
            SquadService.Differences(listedSquad, entry)
          }
          if(changes != SquadInfo.Blank) {
            //squad information update
            log.info(s"Squad will be updated")
            factionListings(index) = entry
            SquadEvents.publish(
              SquadServiceResponse(s"$faction/Squad", SquadResponse.Update(Seq((index, changes))))
            )
          }
          else {
            //remove squad from listing
            log.info(s"Squad will be removed")
            factionListings.remove(index)
            SquadEvents.publish(
              SquadServiceResponse(s"$faction/Squad", SquadResponse.Remove(Seq(index)))
            )
          }
        case None =>
          //first time being published
          log.info(s"Squad will be introduced")
          factionListings += SquadService.Publish(squad)
          SquadEvents.publish(
            SquadServiceResponse(s"$faction/Squad", SquadResponse.Init(factionListings.toVector))
          )
      }
    }
  }

  def GenSquadDetail(squad : Squad) : SquadResponse.Detail = {
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

  def UpdateSquadDetail(squad : Squad) : Unit = {
    val detail = GenSquadDetail(squad)
    squad.Membership.collect {
      case member if !member.Name.equals("") =>
        member.Name
    }.foreach { name =>
      SquadEvents.publish(
        SquadServiceResponse(s"$name/Squad", detail)
      )
    }
  }
}

object SquadService {
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

  def Differences(updates : List[Int], info : SquadInfo) : SquadInfo = {
    if(updates.nonEmpty) {
      val list = Seq(
        SquadInfo.Blank, //must be index-0
        SquadInfo(info.leader, None, None, None, None),
        SquadInfo(None, info.task, None, None, None),
        SquadInfo(None, None, info.zone_id, None, None),
        SquadInfo(None, None, None, info.size, None),
        SquadInfo(None, None, None, None, info.capacity)
      )
      var out = SquadInfo.Blank
      updates
        .map(i => list(i))
        .filterNot { _ == SquadInfo.Blank }
        .foreach(sinfo => out = out And sinfo )
      out
    }
    else {
      SquadInfo.Blank
    }
  }

  def Differences(before : SquadInfo, after : SquadInfo) : SquadInfo = {
    SquadInfo(
      if(!before.leader.equals(after.leader)) after.leader else None,
      if(!before.task.equals(after.task)) after.task else None,
      if(!before.zone_id.equals(after.zone_id)) after.zone_id else None,
      if(!before.size.equals(after.size)) after.size else None,
      if(!before.capacity.equals(after.capacity)) after.capacity else None
    )
  }
}
