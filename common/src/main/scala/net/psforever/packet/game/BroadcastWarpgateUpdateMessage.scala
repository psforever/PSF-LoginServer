// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Promotes a warpgate's "broadcast" functionality.<br>
  * <br>
  * Change the map name of a warpgate into "Broadcast" when the proper state is set.
  * If a proper warpgate is not designated, nothing happens.
  * If not set, the map name of the warpgate will default to whatever is normally written on the map.
  * The map designation of geowarps is not affected by this packet.<br>
  * <br>
  * Exploration:<br>
  * I believe these `Boolean` values actually indicate some measure of warpgate operation.
  * Geowarps, for example, though their appearance does not change, recieve this packet.
  * Moreover, they can operate as a receiving-end broadcast gate.
  * @param continent_guid identifies the zone (continent)
  * @param building_guid identifies the warpgate (see `BuildingInfoUpdateMessage`)
  * @param unk1 na
  * @param unk2 na
  * @param is_broadcast if true, the gate replaces its destination text with "Broadcast"
  */
final case class BroadcastWarpgateUpdateMessage(continent_guid : PlanetSideGUID,
                                                building_guid : PlanetSideGUID,
                                                unk1 : Boolean,
                                                unk2 : Boolean,
                                                is_broadcast : Boolean)
  extends PlanetSideGamePacket {
  type Packet = BroadcastWarpgateUpdateMessage
  def opcode = GamePacketOpcode.BroadcastWarpgateUpdateMessage
  def encode = BroadcastWarpgateUpdateMessage.encode(this)
}

object BroadcastWarpgateUpdateMessage extends Marshallable[BroadcastWarpgateUpdateMessage] {
  implicit val codec : Codec[BroadcastWarpgateUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("unk1" | bool) ::
      ("unk2" | bool) ::
      ("is_broadcast" | bool)
    ).as[BroadcastWarpgateUpdateMessage]
}
