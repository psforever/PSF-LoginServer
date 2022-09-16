// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleManifest
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{BuildingInfoUpdateMessage, CaptureFlagUpdateMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

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

  final case class UpdateBroadcastPrivileges(
                                              zoneId: Int,
                                              gateMapId: Int,
                                              fromFactions: Set[PlanetSideEmpire.Value],
                                              toFactions: Set[PlanetSideEmpire.Value]
                                            ) extends Action

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long) extends Action

  final case class UnlockedZoneUpdate(zone: Zone) extends Action

  final case class LogStatusChange(name: String) extends Action

  final case class SendResponse(msg: PlanetSideGamePacket) extends Action
}
