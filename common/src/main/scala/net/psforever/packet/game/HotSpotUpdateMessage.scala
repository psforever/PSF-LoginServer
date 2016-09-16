// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param unk na
  * @param x the x-coord of the center of the hotspot
  * @param y the y-coord of the center of the hotspot
  * @param scale the scaling of the hotspot graphic
  */
final case class HotSpotInfo(unk : Int,
                             x : Int,
                             y : Int,
                             scale : Int)

/**
  * na
  * @param continent_guid the zone (continent)
  * @param unk na
  * @param spots a list of HotSpotInfo, or Nil if empty
  */
// TODO test for size > 0 (e.g., > hex'00')
// TODO test for size > 15 (e.g., > hex'F0')
final case class HotSpotUpdateMessage(continent_guid : PlanetSideGUID,
                                      unk : Int,
                                      spots : List[HotSpotInfo] = Nil)
  extends PlanetSideGamePacket {
  type Packet = HotSpotUpdateMessage
  def opcode = GamePacketOpcode.HotSpotUpdateMessage
  def encode = HotSpotUpdateMessage.encode(this)
}

object HotSpotUpdateMessage extends Marshallable[HotSpotUpdateMessage] {
  implicit val hotspot_codec : Codec[HotSpotInfo] = {
    ("unk" | uint8L) ::
      ("x" | uint16L) ::
      ("y" | uint16L) ::
      ("scale" | uintL(20))
  }.as[HotSpotInfo]

  implicit val codec : Codec[HotSpotUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint8L) ::
      ("spots" | listOfN(uint8L, hotspot_codec))
    ).as[HotSpotUpdateMessage]
}
