// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.objects.Vehicle
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID

final case class VehicleServiceMessage(forChannel : String, actionMessage : VehicleAction.Action)

object VehicleServiceMessage {
  final case class DelayedVehicleDeconstruction(vehicle : Vehicle, continent : Zone, timeAlive : Long)
  final case class GiveActorControl(vehicle : Vehicle, actorName : String)
  final case class RevokeActorControl(vehicle : Vehicle)
  final case class RequestDeleteVehicle(vehicle : Vehicle, continent : Zone)
  final case class UnscheduleDeconstruction(vehicle_guid : PlanetSideGUID)

  final case class AMSDeploymentChange(zone : Zone)
}
