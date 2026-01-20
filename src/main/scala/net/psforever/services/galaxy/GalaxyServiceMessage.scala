// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleManifest
import net.psforever.objects.zones.{HotSpotInfo, Zone}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{BuildingInfoUpdateMessage, CaptureFlagUpdateMessage}
import net.psforever.services.Service
import net.psforever.services.base.{EventMessage, EventResponse, GenericMessageEnvelope, SelfResponseMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

final case class GalaxyServiceMessage(channel: String, msg: EventMessage)
  extends GenericMessageEnvelope {
  def exclude: PlanetSideGUID = Service.defaultPlayerGUID

  def response(outChannel: String): GalaxyServiceResponse = {
    GalaxyServiceResponse(outChannel, msg.response())
  }
}

object GalaxyServiceMessage {
  def apply(actionMessage: GalaxyAction.Action): GalaxyServiceMessage = GalaxyServiceMessage("", actionMessage)
}

object GalaxyAction {
  trait Action extends EventMessage
  trait Response extends EventResponse

  final case class HotSpotUpdate(zone_id: Int, priority: Int, host_spot_info: List[HotSpotInfo])
    extends Action
      with Response
      with SelfResponseMessage

  final case class MapUpdate(msg: BuildingInfoUpdateMessage)
    extends Action
      with Response
      with SelfResponseMessage

  final case class FlagMapUpdate(msg: CaptureFlagUpdateMessage) extends Action
    with Response
    with SelfResponseMessage

  final case class TransferPassenger(
      player_guid: PlanetSideGUID,
      temp_channel: String,
      vehicle: Vehicle,
      vehicle_to_delete: PlanetSideGUID,
      manifest: VehicleManifest
  ) extends Action
      with Response
      with SelfResponseMessage

  final case class UpdateBroadcastPrivileges(
                                              zoneId: Int,
                                              gateMapId: Int,
                                              fromFactions: Set[PlanetSideEmpire.Value],
                                              toFactions: Set[PlanetSideEmpire.Value]
                                            ) extends Action
    with Response
    with SelfResponseMessage

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long) extends Action
    with Response
    with SelfResponseMessage

  final case class UnlockedZoneUpdate(zone: Zone) extends Action
    with Response
    with SelfResponseMessage

  final case class LogStatusChange(name: String) extends Action
    with Response
    with SelfResponseMessage

  final case class SendResponse(msg: PlanetSideGamePacket) extends Action
    with Response
    with SelfResponseMessage
}
