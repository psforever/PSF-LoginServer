// Copyright (c) 2017 PSForever
package services.galaxy

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.packet.game.BuildingInfoUpdateMessage
import services.galaxy.support.HotSpotProjector
import services.{GenericEventBus, Service}

class GalaxyService extends Actor {
  private [this] val log = org.log4s.getLogger

  val hotSpots : ActorRef = context.actorOf(Props[HotSpotProjector], "hot-spot-projector")

  override def preStart = {
    log.info("Starting...")
  }

  val GalaxyEvents = new GenericEventBus[GalaxyServiceResponse]

  def receive = {
    // Service.Join requires a channel to be passed in normally but GalaxyService is an exception in that messages go to ALL connected players
    case Service.Join(_) =>
      val path = s"/Galaxy"
      val who = sender()
      log.info(s"$who has joined $path")
      GalaxyEvents.subscribe(who, path)

    case Service.Leave(None) =>
      GalaxyEvents.unsubscribe(sender())

    case Service.Leave(_) =>
      val path = s"/Galaxy"
      val who = sender()
      log.info(s"$who has left $path")
      GalaxyEvents.unsubscribe(who, path)

    case Service.LeaveAll() =>
      GalaxyEvents.unsubscribe(sender())

    case GalaxyServiceMessage(action) =>
      action match {
        case GalaxyAction.HotSpotUpdate(zone_id, priority, host_spot_info) =>
          GalaxyEvents.publish(
            GalaxyServiceResponse(s"/Galaxy", GalaxyResponse.HotSpotUpdate(zone_id, priority, host_spot_info))
          )
        case GalaxyAction.MapUpdate(msg: BuildingInfoUpdateMessage) =>
          GalaxyEvents.publish(
            GalaxyServiceResponse(s"/Galaxy", GalaxyResponse.MapUpdate(msg))
          )
        case _ => ;
      }

    case msg @ HotSpotProjector.Activity(_, _, _, _) =>
      hotSpots forward msg

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
