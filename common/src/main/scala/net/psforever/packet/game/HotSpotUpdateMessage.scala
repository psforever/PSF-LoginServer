// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Information for positioning a hotspot on the continental map.<br>
  * <br>
  * The origin point is the lowest left corner of the map grid.
  * The coordinates of the hotspot do not match up to the map's internal coordinate system - what you learn using the `/loc` command.
  * Hotspot coordinates ranges across from 0 (`000`) to 4096 (`FFF`) on both axes.
  * The scale is typically set as 128 (`80000`) but can also be made smaller or even made absurdly big.
  * @param unk1 na
  * @param x the x-coord of the center of the hotspot
  * @param unk2 na
  * @param y the y-coord of the center of the hotspot
  * @param scale how big the hotspot explosion icon appears
  */
final case class HotSpotInfo(unk1 : Int,
                             x : Int,
                             unk2 : Int,
                             y : Int,
                             scale : Int)

/**
  * A list of data for creating hotspots on a continental map.<br>
  * <br>
  * The hotspot system is a forgetful all-or-nothing affair.
  * The packet that is always initially sent during server login clears any would-be hotspots from the map.
  * Each time a hotspot packet is received for a zone, all of the previous hotspots for that zone are forgotten.
  * To simply add a hotspot, the next packet has to contain information that re-explains the packets that were originally rendered.<br>
  * <br>
  * Exploration 1:<br>
  * The unknown parameter has been observed with various non-zero values such as 1, 2, and 5.
  * Visually, however, `unk` does not affect anything.
  * Does it do something internally?
  * @param continent_guid the zone (continent)
  * @param unk na
  * @param spots a List of HotSpotInfo, or `Nil` if empty
  */
// TODO need aligned/padded list support
// TODO test with sendRawResponse(hex"9F 0D00 5 02 0 00 D07 00 8CA 00020 00 BEA 00 4C4 40000")
final case class HotSpotUpdateMessage(continent_guid : PlanetSideGUID,
                                      unk : Int,
                                      spots : List[HotSpotInfo] = Nil)
  extends PlanetSideGamePacket {
  type Packet = HotSpotUpdateMessage
  def opcode = GamePacketOpcode.HotSpotUpdateMessage
  def encode = HotSpotUpdateMessage.encode(this)
}

object HotSpotInfo extends Marshallable[HotSpotInfo] {
  implicit val codec : Codec[HotSpotInfo] = {
    ("unk1" | uint8L) ::
      ("x" | uintL(12)) ::
      ("unk2" | uint8L) ::
      ("y" | uintL(12)) ::
      ("scale" | uintL(20))
  }.as[HotSpotInfo]
}

object HotSpotUpdateMessage extends Marshallable[HotSpotUpdateMessage] {
  implicit val codec : Codec[HotSpotUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint4L) ::
      ("spots" | PacketHelpers.listOfNAligned(uint8L, 4, HotSpotInfo.codec))
    ).as[HotSpotUpdateMessage]
}
