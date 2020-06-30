// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits._
import scodec.codecs._

final case class ClientStart(clientNonce: Long) extends PlanetSideControlPacket {
  type Packet = ClientStart
  def opcode = ControlPacketOpcode.ClientStart
  def encode = ClientStart.encode(this)
}

object ClientStart extends Marshallable[ClientStart] {
  implicit val codec: Codec[ClientStart] = (
    ("unknown" | constant(hex"00000002".bits)) ::
      ("client_nonce" | uint32L) ::
      ("unknown" | constant(hex"000001f0".bits))
  ).as[ClientStart]
}
