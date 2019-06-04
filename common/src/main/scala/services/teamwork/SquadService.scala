// Copyright (c) 2019 PSForever
package services.teamwork

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.objects.teamwork.Squad
import net.psforever.packet.game._
import net.psforever.types.PlanetSideEmpire
import services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

//import scala.concurrent.duration._

class SquadService extends Actor {
  private var memberToSquad : TrieMap[String, Squad] = new TrieMap[String, Squad]()
  private var idToSquad : TrieMap[PlanetSideGUID, Squad] = new TrieMap[PlanetSideGUID, Squad]()
  private var i : Int = 1
  private var publishedLists : TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]] = TrieMap[PlanetSideEmpire.Value, ListBuffer[SquadInfo]](
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

  def GetSquadFromPlayer(player : Player) : Squad = {
    val name = player.Name
    val faction = player.Faction
    memberToSquad.get(name) match {
      case Some(squad) =>
        squad
      case None =>
        val id = GetNextSquadId()
        val squad = new Squad(id, faction)
        val leadPosition = squad.Membership(squad.LeaderPositionIndex)
        leadPosition.Name = name
        leadPosition.Health = player.Health
        leadPosition.Armor = player.Armor
        leadPosition.Position = player.Position
        leadPosition.ZoneId = 1 //player.Continent //TODO how to resolve this?
        log.info(s"$name-$faction has started a new squad")
        memberToSquad += name -> squad
        idToSquad += id -> squad
        squad
    }
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
        case Some(squad) =>
          sender ! SquadServiceMessage.RecoverSquadMembership()
      }

    case Service.Leave(Some(name)) => ;
      SquadEvents.unsubscribe(sender())
      //TODO leave squad, if joined to one, and perform clean-up

    case Service.Leave(None) | Service.LeaveAll() =>
      SquadEvents.unsubscribe(sender())
      //TODO might be better to invalidate these

    case SquadServiceMessage.SquadDefinitionAction(tplayer, zone_ordinal_number, _, _, action) =>
      import net.psforever.packet.game.SquadAction._
      val squad = GetSquadFromPlayer(tplayer)
      val member = squad.Membership.find(_.Name == tplayer.Name).get //should never fail
      member.ZoneId = zone_ordinal_number //TODO improve this requirement
      if(tplayer.Name.equals(squad.Leader)) {
        var listingChanged : List[Int] = Nil
        action match {
          case ChangeSquadPurpose(purpose) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed his squad's task to $purpose")
            squad.Description = purpose
            listingChanged = List(SquadInfo.Field.Task)

          case ChangeSquadZone(zone) =>
            log.info(s"${tplayer.Name}-${tplayer.Faction} has changed his squad's ops zone to $zone")
            squad.ZoneId = zone.zoneId.toInt
            listingChanged = List(SquadInfo.Field.ZoneId)

          case CloseSquadMemberPosition(position) =>
            if(position != squad.LeaderPositionIndex) {
              squad.Availability.lift(position) match {
                case Some(true) =>
                  squad.Availability.update(position, false)
                  log.info(s"${tplayer.Name}-${tplayer.Faction} has closed the #$position position in his squad")
                  val memberPosition = squad.Membership(position)
                  listingChanged = if(memberPosition.Name.nonEmpty) {
                    List(SquadInfo.Field.Size, SquadInfo.Field.Capacity)
                  }
                  else {
                    List(SquadInfo.Field.Capacity)
                  }
                  memberPosition.Close()
                case Some(false) => ;
                case None => ;
              }
            }
            else {
              log.warn(s"can not close the leader position in squad-${squad.GUID.guid}")
            }

          case AddSquadMemberPosition(position) =>
            squad.Availability.lift(position) match {
              case Some(false) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has opened the #$position position in his squad")
                squad.Availability.update(position, true)
                listingChanged = List(SquadInfo.Field.Capacity)
              case Some(true) => ;
              case None => ;
            }

          case ChangeSquadMemberRequirementsRole(position, role) =>
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the role of squad position #$position")
                squad.Membership(position).Role = role
              case Some(false) => ;
              case None => ;
            }

          case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the orders for squad position #$position")
                squad.Membership(position).Orders = orders
              case Some(false) => ;
              case None => ;
            }

          case ChangeSquadMemberRequirementsCertifications(position, certs) =>
            squad.Availability.lift(position) match {
              case Some(true) =>
                log.info(s"${tplayer.Name}-${tplayer.Faction} has changed the requirements for squad position #$position")
                squad.Membership(position).Requirements = certs
              case Some(false) => ;
              case None => ;
            }

          case ListSquad() =>
            if(!squad.Listed) {
              log.info(s"${tplayer.Name}-${tplayer.Faction} has opened recruitment for his squad")
              squad.Listed = true
            }

          case ResetAll() =>
            squad.Description = ""
            squad.ZoneId = None
            squad.Availability.indices.foreach { i =>
              squad.Availability.update(i, true)
            }
          //TODO squad members?

          case _ => ;
        }
        //queue updates
        if(squad.Listed) {
          val entry = SquadService.Publish(squad)
          val faction = squad.Faction
          val factionListings = publishedLists(faction)
          factionListings.find(info => {
            info.squad_guid match {
              case Some(guid) => guid == squad.GUID
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

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}

object SquadService {
  def Publish(squad : Squad) : SquadInfo = {
    SquadInfo(
      squad.Leader,
      squad.Description,
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
