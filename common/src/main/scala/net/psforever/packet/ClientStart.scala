// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class ClientStart(clientNonce : Long)
  extends PlanetSideControlPacket {
  type Packet = ClientStart
  def opcode = ControlPacketOpcode.ClientStart
  def encode = ClientStart.encode(this)
}

object ClientStart extends Marshallable[ClientStart] {
  implicit val codec : Codec[ClientStart] = (
    ("unknown" | constant(hex"00000002".bits)) ::
      ("client_nonce" | uint32L) ::
      ("unknown" | constant(hex"000001f0".bits))
    ).as[ClientStart]
}