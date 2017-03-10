// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits._
import scodec.codecs._

final case class ServerStart(clientNonce : Long, serverNonce : Long)
  extends PlanetSideControlPacket {
  type Packet = ServerStart
  def opcode = ControlPacketOpcode.ServerStart
  def encode = ServerStart.encode(this)
}

object ServerStart extends Marshallable[ServerStart] {
  implicit val codec : Codec[ServerStart] = (
    ("client_nonce" | uint32L) ::
      ("server_nonce" | uint32L) ::
      ("unknown" | constant(hex"000000000001d300000002".bits))
    ).as[ServerStart]
}