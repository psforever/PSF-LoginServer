// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.pad.shuttle

import akka.actor.Actor
import net.psforever.objects.zones.Zone
import net.psforever.services.Service

class OrbitalShuttlePadControl(pad: OrbitalShuttlePad) extends Actor {
  def receive: Receive = startUp

  val taxiing: Receive = {
    case _ => ;
  }

  val startUp: Receive = {
    case Service.Startup() if pad.shuttle.HasGUID =>
      pad.shuttle.Faction = pad.Faction
      pad.Zone.Transport ! Zone.Vehicle.Spawn(pad.shuttle)
      context.become(taxiing)
    case _ => ;
  }
}
