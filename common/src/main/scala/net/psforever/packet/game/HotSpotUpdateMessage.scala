// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Information for positioning a hotspot on the continental map.<br>
  * <br>
  * The origin point is the lowest left corner of the map grid.
  * The coordinates of the hotspot do necessarily match up to the map's internal coordinate system - what you learn using the `/loc` command.
  * Instead, all maps use a 0 - 8192 coordinate overlay.
  * @param x the x-coord of the center of the hotspot
  * @param y the y-coord of the center of the hotspot
  * @param scale how big the hotspot explosion icon appears
  */
final case class HotSpotInfo(x : Float,
                             y : Float,
                             scale : Float)

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
  * What does (zone) priority entail?
  * @param continent_id the zone
  * @param priority na
  * @param spots a List of HotSpotInfo
  */
final case class HotSpotUpdateMessage(continent_id : Int,
                                      priority : Int,
                                      spots : List[HotSpotInfo])
  extends PlanetSideGamePacket {
  type Packet = HotSpotUpdateMessage
  def opcode = GamePacketOpcode.HotSpotUpdateMessage
  def encode = HotSpotUpdateMessage.encode(this)
}

object HotSpotInfo extends Marshallable[HotSpotInfo] {
  /*
  the client is looking for a normal 0-8192 value where default is 1.0f
  we try to enforce a more modest graphic scale where default is 64.0f (arbitrary)
  personally, I'd like scale to equal the sprite width in map units but the pulsation makes it hard to apply
   */
  implicit val codec : Codec[HotSpotInfo] = {
    ("x" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("y" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("scale" | newcodecs.q_float(0.0, 524288.0, 20))
  }.as[HotSpotInfo]
}

object HotSpotUpdateMessage extends Marshallable[HotSpotUpdateMessage] {
  implicit val codec : Codec[HotSpotUpdateMessage] = (
    ("continent_guid" | uint16L) ::
      ("priority" | uint4L) ::
      ("spots" | PacketHelpers.listOfNAligned(longL(12), 0, HotSpotInfo.codec))
    ).as[HotSpotUpdateMessage]
}
