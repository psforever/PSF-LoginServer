// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to promote a warp gate's broadcast functionality.<br>
  * <br>
  * Changes the map name of a warp gate into "Broadcast"
  * and allow a given faction to access the gate's intercontinental transport functionality to/from that gate,
  * even if the gate is not properly owned.
  * If an actual warp gate is not designated, nothing happens.
  * If not set, the map name of the warp gate will default to whatever is normally written on the map.
  * The map designation of geowarps is not affected by this packet.
  * @see `BuildingInfoUpdateMessage`
  * @param zone_id the zone ordinal number
  * @param building_id the warp gate map id
  * @param tr players belonging to the Terran Republic interact with this warp gate as a "broadcast gate"
  * @param nc players belonging to the New Conglomerate interact with this warp gate as a "broadcast gate"
  * @param vs players belonging to the Vanu Sovereignty interact with this warp gate as a "broadcast gate"
  */
final case class BroadcastWarpgateUpdateMessage(
                                                 zone_id: Int,
                                                 building_id: Int,
                                                 tr: Boolean,
                                                 nc: Boolean,
                                                 vs: Boolean
                                               ) extends PlanetSideGamePacket {
  type Packet = BroadcastWarpgateUpdateMessage
  def opcode = GamePacketOpcode.BroadcastWarpgateUpdateMessage
  def encode = BroadcastWarpgateUpdateMessage.encode(this)
}

object BroadcastWarpgateUpdateMessage extends Marshallable[BroadcastWarpgateUpdateMessage] {
  def apply(
             zoneId: Int,
             buildingId: Int,
             faction: PlanetSideEmpire.Value
           ): BroadcastWarpgateUpdateMessage = {
    BroadcastWarpgateUpdateMessage(zoneId, buildingId, Set(faction))
  }

  def apply(
             zoneId: Int,
             buildingId: Int,
             factions: Set[PlanetSideEmpire.Value]
           ): BroadcastWarpgateUpdateMessage = {
    val f = {
      val out = Array.fill(PlanetSideEmpire.values.size)(false)
      factions.map(_.id).foreach { i => out.update(i, true) }
      out
    }
    BroadcastWarpgateUpdateMessage(zoneId, buildingId, f(0), f(1), f(2))
  }

  implicit val codec: Codec[BroadcastWarpgateUpdateMessage] = (
    ("zone_id" | uint16L) ::
      ("building_id" | uint16L) ::
      ("tr" | bool) ::
      ("nc" | bool) ::
      ("vs" | bool)
  ).as[BroadcastWarpgateUpdateMessage]
}
