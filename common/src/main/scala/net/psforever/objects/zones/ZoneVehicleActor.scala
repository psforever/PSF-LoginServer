// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor

/**
  * Synchronize management of the list of `Vehicles` maintained by some `Zone`.
  * @param zone the `Zone` object
  */
class ZoneVehicleActor(zone : Zone) extends Actor {
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.SpawnVehicle(vehicle) =>
      zone.AddVehicle(vehicle)

    case Zone.DespawnVehicle(vehicle) =>
      zone.RemoveVehicle(vehicle)

    case _ => ;
  }
}
