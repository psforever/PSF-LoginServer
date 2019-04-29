//Copyright (c) 2019 PSForever
package services.galaxy.support

import akka.actor.Actor
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.HotSpotInfo
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.galaxy.{GalaxyAction, GalaxyServiceMessage}

class HotSpotProjector extends Actor {
  def receive : Receive = {
    case HotSpotProjector.Activity(zone, defender, attacker, location) =>
      context.parent ! GalaxyServiceMessage(
        GalaxyAction.HotSpotUpdate(zone.Number, 0, List(HotSpotInfo(location.x, location.y, 64)))
      )
    case _ => ;
  }
}

object HotSpotProjector {
  final case class Activity(zone : Zone, defender : PlanetSideEmpire.Value, attacker : PlanetSideEmpire.Value, location : Vector3)
}