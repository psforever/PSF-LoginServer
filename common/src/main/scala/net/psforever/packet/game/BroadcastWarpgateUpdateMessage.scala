// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Promotes a Broadcast Warpgate.
  *
  * Change the map name of a warpgate into "Broadcast Warpgate."
  * If a proper warpgate is not designated, nothing happens.
  * The input state is a byte starting at 00 where the first valid "on" state is 20.
  * Subsequent "on" states are 40 values apart - 20, 60, A0, and E0.
  * All other numbers (seem to) default to the normal map designation for the warpgate.
  *
  * Exploration: is this packet merely cosmetic or does it change the functionality of a warpgate too?
  * Exploration: are there any differences between the states besides "on" and "off"?
  *
  * @param continent_guid identifies the zone (continent)
  * @param building_guid identifies the warpgate (see BuildingInfoUpdateMessage)
  * @param state whether or not a warpgate is considered "broadcast;"
  *            00, 20, 80, and A0 are common values; C0 is an uncommon value
  */
final case class BroadcastWarpgateUpdateMessage(continent_guid : PlanetSideGUID,
                                                building_guid : PlanetSideGUID,
                                                state : Int)
  extends PlanetSideGamePacket {
  type Packet = BroadcastWarpgateUpdateMessage
  def opcode = GamePacketOpcode.BroadcastWarpgateUpdateMessage
  def encode = BroadcastWarpgateUpdateMessage.encode(this)
}

object BroadcastWarpgateUpdateMessage extends Marshallable[BroadcastWarpgateUpdateMessage] {
  implicit val codec : Codec[BroadcastWarpgateUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("state" | uint8L)
    ).as[BroadcastWarpgateUpdateMessage]
}
