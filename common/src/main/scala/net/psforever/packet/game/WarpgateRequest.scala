// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
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
  * Does this packet apply to geowarp transport as well?<br>
  * <br>
  * Exploration 2:<br>
  * Those last two fields that are usually blanked do something?
  * @param continent_guid the continent (zone)
  * @param building_guid the warp gate
  * @param dest_building_guid the destination warp gate
  * @param dest_continent_guid the destination continent (zone)
  * @param unk1 na; always zero?
  * @param unk2 na; always zero?
  */
final case class WarpgateRequest(continent_guid : PlanetSideGUID,
                                 building_guid : PlanetSideGUID,
                                 dest_building_guid : PlanetSideGUID,
                                 dest_continent_guid : PlanetSideGUID,
                                 unk1 : Int,
                                 unk2 : Int)
  extends PlanetSideGamePacket {
  type Packet = WarpgateRequest
  def opcode = GamePacketOpcode.WarpgateRequest
  def encode = WarpgateRequest.encode(this)
}

object WarpgateRequest extends Marshallable[WarpgateRequest] {
  implicit val codec : Codec[WarpgateRequest] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("dest_building_guid" | PlanetSideGUID.codec) ::
      ("dest_continent_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
    ).as[WarpgateRequest]
}
