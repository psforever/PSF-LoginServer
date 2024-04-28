package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

// Probably a more lightweight variant of ClientStart, containing client and server nonce
// will be received when sending a ConnectionClose() (after client requests world connection info)
final case class Unknown30(
    clientNonce: Long,
    serverNonce: Long
  ) extends PlanetSideControlPacket {
  type Packet = Unknown30
  def opcode = ControlPacketOpcode.Unknown30
  def encode = Unknown30.encode(this)
}

object Unknown30 extends Marshallable[Unknown30] {
  implicit val codec: Codec[Unknown30] = (
    ("client_nonce" | uint32L) ::
      ("server_nonce" | uint32L)
    ).as[Unknown30]
}
