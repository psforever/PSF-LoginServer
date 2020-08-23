// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

// NOTE: this packet has a ton of bytes left over at the end and is usually all zeros
// except for the server name and a 0x80 near the end
final case class ConnectToWorldRequestMessage(
    server: String,
    token: String,
    majorVersion: Long,
    minorVersion: Long,
    revision: Long,
    buildDate: String,
    unknown: Int
) extends PlanetSideGamePacket {
  type Packet = ConnectToWorldRequestMessage
  def opcode = GamePacketOpcode.ConnectToWorldRequestMessage
  def encode = ConnectToWorldRequestMessage.encode(this)
}

object ConnectToWorldRequestMessage extends Marshallable[ConnectToWorldRequestMessage] {
  implicit val codec: Codec[ConnectToWorldRequestMessage] = (
    ("server_name" | PacketHelpers.encodedString) ::
      ("token" | paddedFixedSizeBytes(32, cstring, ignore(8))) :: // must be an ignore 8 as the memory might not be 0x00
      ("major_version" | uint32L) ::
      ("minor_version" | uint32L) ::
      ("revision" | uint32L) ::
      ("build_date" | PacketHelpers.encodedString) ::
      ("unknown_short" | uint16L)
  ).as[ConnectToWorldRequestMessage]
}
