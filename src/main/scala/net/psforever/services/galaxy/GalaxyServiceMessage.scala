// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleManifest
import net.psforever.packet.game.{BuildingInfoUpdateMessage, CaptureFlagUpdateMessage}
import net.psforever.types.PlanetSideGUID

final case class GalaxyServiceMessage(forChannel: String, actionMessage: GalaxyAction.Action)

object GalaxyServiceMessage {
  def apply(actionMessage: GalaxyAction.Action): GalaxyServiceMessage = GalaxyServiceMessage("", actionMessage)
}

object GalaxyAction {
  trait Action

  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends Action
  final case class FlagMapUpdate(msg: CaptureFlagUpdateMessage) extends Action

  final case class TransferPassenger(
      player_guid: PlanetSideGUID,
      temp_channel: String,
      vehicle: Vehicle,
      vehicle_to_delete: PlanetSideGUID,
      manifest: VehicleManifest
  ) extends Action
}
