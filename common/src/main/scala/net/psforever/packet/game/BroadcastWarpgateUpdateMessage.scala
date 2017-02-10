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
  * The input state is a byte starting at `00` where the first valid "on" state is `20`.
  * Subsequent "on" states are '40' values apart - '20', '60', 'A0', and 'E0'.
  * 'C0' is an uncommon value.
  * All other numbers (seem to) default to the normal map designation for the warpgate.
  *
  * Exploration: is this packet merely cosmetic or does it change the functionality of a warpgate too?
  * Exploration: are there any differences between the states besides "on" and "off"?
  *
  * @param continent_guid identifies the zone (continent)
  * @param building_guid identifies the warpgate (see `BuildingInfoUpdateMessage`)
  * @param state1 na
  * @param state2 na
  * @param is_broadcast if true, the gate replaces its destination text with "Broadcast;"
  *                     the owner faction may shortcut between disconnected gates along the intercontinental lattice
  */
final case class BroadcastWarpgateUpdateMessage(continent_guid : PlanetSideGUID,
                                                building_guid : PlanetSideGUID,
                                                state1 : Boolean,
                                                state2 : Boolean,
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
      ("state1" | bool) ::
      ("state2" | bool) ::
      ("is_broadcast" | bool)
    ).as[BroadcastWarpgateUpdateMessage]
}
