// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._


/**
 * map_name and nav_map_name should match (unless you want to be lost :))
 * 
 * ex:
 * map13 & home3 = vs sanc
 * map10 & z10 = amerish
 * map07 & z7 = esamir
 */
final case class LoadMapMessage(map_name : String,
                                nav_map_name : String, // Also determines loading screen
                                unk1 : Int,
                                unk2 : Long,
                                weapons_unlocked : Boolean,
                                checksum : Long) //?
  extends PlanetSideGamePacket {
  type Packet = LoadMapMessage
  def opcode = GamePacketOpcode.LoadMapMessage
  def encode = LoadMapMessage.encode(this)
}

object LoadMapMessage extends Marshallable[LoadMapMessage] {
  implicit val codec : Codec[LoadMapMessage] = (
      ("map_name" | PacketHelpers.encodedString) :: // TODO: Implement encodedStringWithLimit
      ("nav_map_name" | PacketHelpers.encodedString) :: //TODO: Implement encodedStringWithLimit
      ("unk1" | uint16L) ::
      ("unk2" | uint32L) ::
      ("weapons_unlocked" | bool) ::
      ("checksum" | uint32L)
    ).as[LoadMapMessage]
}
