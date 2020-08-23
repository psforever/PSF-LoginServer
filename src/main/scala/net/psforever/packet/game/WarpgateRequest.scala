// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Alert the server that a player wishes to engage in warp gate transport.<br>
  * <br>
  * This packet is dispatched after a player interacts with the transportation beam in the center of a warp gate.
  * The player has either chosen a destination from the Interstellar Map or was assigned a fixed destination.
  * When the the destination is limited to a specific continent and warp gate - "fixed" - the destination fields can be blanked.
  * Otherwise, they must be specified.
  * The process of gate transportation should not start until the server responds to this packet.<br>
  * <br>
  * Exploration 1:<br>
  * Does this packet apply to geowarp transport as well?
  * @param from_zone the continent (zone)
  * @param from_gate_guid the warp gate
  * @param to_gate_mapid the destination warp gate
  * @param to_zone the destination continent (zone)
  * @param unk1 na; always zero?
  * @param unk2 na; always zero?
  */
final case class WarpgateRequest(
    from_zone: PlanetSideGUID,
    from_gate_guid: PlanetSideGUID,
    to_gate_mapid: PlanetSideGUID,
    to_zone: PlanetSideGUID,
    unk1: Int,
    unk2: Int
) extends PlanetSideGamePacket {
  type Packet = WarpgateRequest
  def opcode = GamePacketOpcode.WarpgateRequest
  def encode = WarpgateRequest.encode(this)
}

object WarpgateRequest extends Marshallable[WarpgateRequest] {
  implicit val codec: Codec[WarpgateRequest] = (
    ("from_zone" | PlanetSideGUID.codec) ::
      ("from_gate_guid" | PlanetSideGUID.codec) ::
      ("to_gate_mapid" | PlanetSideGUID.codec) ::
      ("to_zone" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
  ).as[WarpgateRequest]
}
