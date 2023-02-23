// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
 * Dispatched from server to client to instigate a zone change.
 * The client should respond with a `BeginZoningMessage` packet.
 * `map_name` and `zone_id` should correspond or the final product will be disorienting, even if it works.
 * @param map_name designation of the physical zone;
 *                 determines the (deployment) map screen
 * @param zone_id designation of the entirety of the zone;
 *                determines the loading screen
 * @param unk1 na;
 *             seems to match the initial projectile index (that can be assigned)
 * @param unk2 na;
 *             seems to match the total number of unique projectile indices (that can be assigned) (before looping)
 * @param weapons_unlocked live fire is permissible;
 *                         restricts all actions instigated by that key bind
 * @param checksum challenge number so that client can confirm server is using the correct version of this zone
 */
final case class LoadMapMessage(
                                 map_name: String,
                                 zone_id: String, // Also determines loading screen
                                 unk1: Int,
                                 unk2: Long,
                                 weapons_unlocked: Boolean,
                                 checksum: Long
) extends PlanetSideGamePacket {
  type Packet = LoadMapMessage
  def opcode = GamePacketOpcode.LoadMapMessage
  def encode = LoadMapMessage.encode(this)
}

object LoadMapMessage extends Marshallable[LoadMapMessage] {
  implicit val codec: Codec[LoadMapMessage] = (
    ("map_name" | PacketHelpers.encodedString) :: // TODO: Implement encodedStringWithLimit
      ("zone_id" | PacketHelpers.encodedString) :: //TODO: Implement encodedStringWithLimit
      ("unk1" | uint16L) ::
      ("unk2" | uint32L) ::
      ("weapons_unlocked" | bool) ::
      ("checksum" | uint32L)
  ).as[LoadMapMessage]
}
