// Copyright (c) 2017-2026 PSForever
package net.psforever.services.galaxy

import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleManifest
import net.psforever.objects.zones.{HotSpotInfo, Zone}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{BuildingInfoUpdateMessage, CaptureFlagUpdateMessage}
import net.psforever.services.base.SelfResponseMessage
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

object GalaxyAction {
  final case class HotSpotUpdate(zone_id: Int, priority: Int, host_spot_info: List[HotSpotInfo]) extends SelfResponseMessage

  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends SelfResponseMessage

  final case class FlagMapUpdate(msg: CaptureFlagUpdateMessage) extends SelfResponseMessage

  final case class TransferPassenger(
                                      player_guid: PlanetSideGUID,
                                      temp_channel: String,
                                      vehicle: Vehicle,
                                      vehicle_to_delete: PlanetSideGUID,
                                      manifest: VehicleManifest
                                    ) extends SelfResponseMessage

  final case class UpdateBroadcastPrivileges(
                                              zoneId: Int,
                                              gateMapId: Int,
                                              fromFactions: Set[PlanetSideEmpire.Value],
                                              toFactions: Set[PlanetSideEmpire.Value]
                                            ) extends SelfResponseMessage

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long) extends SelfResponseMessage

  final case class UnlockedZoneUpdate(zone: Zone) extends SelfResponseMessage

  final case class LogStatusChange(name: String) extends SelfResponseMessage

  final case class SendResponse(msg: PlanetSideGamePacket) extends SelfResponseMessage
}
