// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.pad.shuttle

import akka.actor.Actor
import net.psforever.objects.zones.Zone
import net.psforever.services.time.ShuttleTimer
import net.psforever.services.{Service, ServiceManager}

class OrbitalShuttlePadControl(pad: OrbitalShuttlePad) extends Actor {
  def receive: Receive = startUp

  val taxiing: Receive = {
    case _ => ;
  }

  val shuttleTime: Receive = {
    case ServiceManager.LookupResult("shuttleTimer", timer) =>
      timer ! ShuttleTimer.PairWith(pad.Zone, pad.GUID, pad.shuttle.GUID)
      context.become(taxiing)

    case _ => ;
  }

  val startUp: Receive = {
    case Service.Startup() if pad.shuttle.HasGUID =>
      pad.shuttle.Faction = pad.Faction
      pad.shuttle.Zone = pad.Zone
      pad.Zone.Transport ! Zone.Vehicle.Spawn(pad.shuttle)
      ServiceManager.serviceManager ! ServiceManager.Lookup("shuttleTimer")
      context.become(shuttleTime)

    case _ => ;
  }
}
