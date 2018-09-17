// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.objects.Vehicle
import net.psforever.objects.zones.Zone

final case class VehicleServiceMessage(forChannel : String, actionMessage : VehicleAction.Action)

object VehicleServiceMessage {
  final case class GiveActorControl(vehicle : Vehicle, actorName : String)
  final case class RevokeActorControl(vehicle : Vehicle)

  final case class Decon(msg : Any)

  final case class TurretUpgrade(msg : Any)

  final case class Router(msg : Any)

  final case class AMSDeploymentChange(zone : Zone)
}
