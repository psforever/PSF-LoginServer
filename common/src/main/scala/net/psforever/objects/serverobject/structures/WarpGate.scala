// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.zones.Zone

class WarpGate(id : Int, zone : Zone) extends Building(id, zone, StructureType.WarpGate) {
  //TODO stuff later
}

object WarpGate {
  def apply(id : Int, zone : Zone) : WarpGate = {
    new WarpGate(id, zone)
  }

  def Structure(id : Int, zone : Zone, context : ActorContext) : WarpGate = {
    import akka.actor.Props
    val obj = new WarpGate(id, zone)
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$id-gate")
    obj
  }
}
