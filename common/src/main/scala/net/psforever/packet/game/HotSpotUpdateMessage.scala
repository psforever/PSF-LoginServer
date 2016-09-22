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
  * Hotspot coordinates range across from 0 (`000`) to 4096 (`FFF`) on both axes.
  * The scale is typically set as 128 (`80000`) but can also be made smaller or even made absurdly big.<br>
  * <br>
  * Exploration:<br>
  * Are those really unknown values or are they just extraneous spacers between the components of the coordinates?
  * @param unk1 na; always zero?
  * @param x the x-coord of the center of the hotspot
  * @param unk2 na; always zero?
  * @param y the y-coord of the center of the hotspot
  * @param scale how big the hotspot explosion icon appears
  */
final case class HotSpotInfo(unk1 : Int,
                             x : Int,
                             unk2 : Int,
                             y : Int,
                             scale : Int)

/**
  * A list of data for creating hotspots on a continental map.
  * Hotspots indicate player activity, almost always some form of combat or aggressive encounter.<br>
  * <br>
  * The hotspot system is an all-or-nothing affair.
  * The received packet indicates the hotspots to display and the map will display only those hotspots.
  * Inversely, if the received packet indicates no hotspots, the map will display no hotspots at all.
  * This "no hotspots" packet is always initially sent during zone setup during server login.
  * To clear away only some hotspots, but retains others, a continental `List` would have to be pruned selectively for the client.<br>
  * <br>
  * Exploration:<br>
  * The unknown parameter has been observed with various non-zero values such as 1, 2, and 5.
  * Visually, however, `unk` does not affect anything.
  * (Originally, I thought it might be a layering index but that is incorrect.)
  * Does it do something internally?
  * @param continent_guid the zone (continent)
  * @param unk na
  * @param spots a List of HotSpotInfo
  */
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

  /**
    * This alternate constructor ignores the unknown values.
    * @param x the x-coord of the center of the hotspot
    * @param y the y-coord of the center of the hotspot
    * @param scale how big the hotspot explosion icon appears
    * @return valid HotSpotInfo
    */
  def apply(x : Int, y : Int, scale : Int) : HotSpotInfo = {
    HotSpotInfo(0, x, 0 ,y, scale)
  }
}

object HotSpotUpdateMessage extends Marshallable[HotSpotUpdateMessage] {
  implicit val codec : Codec[HotSpotUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint4L) ::
      ("spots" | PacketHelpers.listOfNAligned(uint8L, 4, HotSpotInfo.codec))
    ).as[HotSpotUpdateMessage]
}
