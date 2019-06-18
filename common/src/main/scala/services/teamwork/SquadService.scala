// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.objects.teamwork.{Member, Squad}
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
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

  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
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
        if(squad.Leader.equals(name)) {
          squad
        }
        else {
          GetLeadingSquad(player, zone, None)
        }

      case None =>
        memberToSquad.get(name) match {
          case Some(squad) if squad.Leader.equals(name) =>
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
          if(squad.Leader.equals(name)) {
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

    case SquadServiceMessage.SquadDefinitionAction(tplayer, zone_ordinal_number, guid, _, action) =>
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

        case SelectRoleForYourself(line) =>
          //TODO need to ask permission from the squad leader, unless our character is the squad leader or already currently in the squad
          val name = tplayer.Name
          squadOpt match {
            case Some(squad) =>
              {
                if(squad.Availability(line))
                  squad.Membership.lift(line)
                else
                  None
              } match {
                case Some(desiredPosition : Member)
                  if desiredPosition.Requirements.intersect(tplayer.Certifications) == desiredPosition.Requirements =>
                  //our character is qualified for this new position
                  if(squad.Leader.equals(tplayer.Name)) {
                    squad.LeaderPositionIndex = line //update
                  }
                  val hadPreviousPosition = squad.Membership.find(_.Name == name) match {
                    case Some(currentPosition)=>
                      currentPosition.Name = ""
                      currentPosition.CharId = 0L
                      currentPosition.ZoneId = 0
                      currentPosition.Health = 0
                      currentPosition.Armor = 0
                      currentPosition.Position = Vector3.Zero
                      true
                    case None =>
                      false
                  }
                  desiredPosition.Name = name
                  desiredPosition.CharId = tplayer.CharId
                  desiredPosition.ZoneId = zone_ordinal_number
                  desiredPosition.Health = tplayer.Health
                  desiredPosition.Armor = tplayer.Armor
                  desiredPosition.Position = tplayer.Position
                  if(!hadPreviousPosition) {
                    UpdateSquadList(squad, List(SquadInfo.Field.Size))
                  }
                  UpdateSquadDetail(squad)
                case None => ;
              }

            case None => ;
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
      squad.Leader,
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
      squad.Leader,
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
