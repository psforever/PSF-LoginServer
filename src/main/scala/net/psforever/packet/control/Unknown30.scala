package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

final case class Unknown30(clientNonce: Long) extends PlanetSideControlPacket {
  type Packet = Unknown30
  def opcode = ControlPacketOpcode.Unknown30
  def encode = Unknown30.encode(this)
}

object Unknown30 extends Marshallable[Unknown30] {
  implicit val codec: Codec[Unknown30] = ("client_nonce" | uint32L).as[Unknown30]
}
