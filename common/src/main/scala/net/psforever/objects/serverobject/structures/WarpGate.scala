// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.zones.Zone

class WarpGate(building_guid : Int, map_id : Int, zone : Zone) extends Building(building_guid, map_id, zone, StructureType.WarpGate) {
  //TODO stuff later
}

object WarpGate {
  def apply(guid : Int, map_id : Int, zone : Zone) : WarpGate = {
    new WarpGate(guid, map_id, zone)
  }

  def Structure(guid : Int, map_id : Int, zone : Zone, context : ActorContext) : WarpGate = {
    import akka.actor.Props
    val obj = new WarpGate(guid, map_id, zone)
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-gate")
    obj
  }
}
