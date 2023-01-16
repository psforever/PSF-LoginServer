// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleManifest
import net.psforever.objects.zones.{HotSpotInfo, Zone}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{BuildingInfoUpdateMessage, CaptureFlagUpdateMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import net.psforever.services.GenericEventBusMsg

final case class GalaxyServiceResponse(channel: String, replyMessage: GalaxyResponse.Response)
    extends GenericEventBusMsg

object GalaxyResponse {
  trait Response

  final case class HotSpotUpdate(zone_id: Int, priority: Int, host_spot_info: List[HotSpotInfo]) extends Response
  final case class MapUpdate(msg: BuildingInfoUpdateMessage)                                     extends Response
  final case class FlagMapUpdate(msg: CaptureFlagUpdateMessage)                                  extends Response


  final case class TransferPassenger(
      temp_channel: String,
      vehicle: Vehicle,
      vehicle_to_delete: PlanetSideGUID,
      manifest: VehicleManifest
  ) extends Response

  final case class UpdateBroadcastPrivileges(
                                              zoneId: Int,
                                              gateMapId: Int,
                                              fromFactions: Set[PlanetSideEmpire.Value],
                                              toFactions: Set[PlanetSideEmpire.Value]
                                            ) extends Response

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long) extends Response

  final case class UnlockedZoneUpdate(zone: Zone) extends Response

  final case class LogStatusChange(name: String) extends Response

  final case class SendResponse(msg: PlanetSideGamePacket) extends Response
}
