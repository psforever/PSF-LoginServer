// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

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
  * To clear away only some hotspots but retains others, a continental list would have to be pruned selectively for the client.<br>
  * <br>
  * Exploration:<br>
  * What does (zone) priority entail?
  * @param zone_index the zone
  * @param priority na
  * @param spots a List of HotSpotInfo
  */
final case class HotSpotUpdateMessage(zone_index : Int,
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
    ("zone_index" | uint16L) ::
      ("priority" | uint4L) ::
      ("spots" | PacketHelpers.listOfNAligned(longL(12), 0, HotSpotInfo.codec))
    ).xmap[HotSpotUpdateMessage] (
    {
      case zone_index :: priority :: spots :: HNil =>
        HotSpotUpdateMessage(zone_index, priority, spots)
    },
    {
      case HotSpotUpdateMessage(zone_index, priority, spots) if spots.size > 4095 =>
        //maximum number of points is 4095 (12-bit integer) but provided list of hotspot information is greater
        //focus on depicting the "central" 4095 points only
        val size = spots.size
        val center = Vector3(
          spots.foldLeft(0f)(_ + _.x) / size,
          spots.foldLeft(0f)(_ + _.y) / size,
          0
        )
        zone_index :: priority :: spots.sortBy(spot => Vector3.DistanceSquared(Vector3(spot.x, spot.y, 0), center)).take(4095) :: HNil

      case HotSpotUpdateMessage(zone_index, priority, spots) =>
        zone_index :: priority :: spots :: HNil
    }
  )
}
