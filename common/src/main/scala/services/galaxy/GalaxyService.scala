// Copyright (c) 2017 PSForever
package services.galaxy

import akka.actor.Actor
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.BuildingInfoUpdateMessage
import services.{GenericEventBus, Service}

class GalaxyService extends Actor {
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
  }

  val GalaxyEvents = new GenericEventBus[GalaxyServiceResponse]

  def receive : Receive = {
    case Service.Join(faction) if "TRNCVS".containsSlice(faction) =>
      val path = s"/$faction/Galaxy"
      val who = sender()
      log.info(s"$who has joined $path")
      GalaxyEvents.subscribe(who, path)

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
        case GalaxyAction.MapUpdate(msg: BuildingInfoUpdateMessage) =>
          GalaxyEvents.publish(
            GalaxyServiceResponse(s"/Galaxy", GalaxyResponse.MapUpdate(msg))
          )
        case _ => ;
      }

    case Zone.HotSpot.Update(faction, zone_num, priority, info) =>
      GalaxyEvents.publish(
        GalaxyServiceResponse(s"/$faction/Galaxy", GalaxyResponse.HotSpotUpdate(zone_num, priority, info))
      )

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
