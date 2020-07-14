// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

final case class TeardownConnection(targetNonce: Long) extends PlanetSideControlPacket {
  type Packet = TeardownConnection
  def opcode = ControlPacketOpcode.TeardownConnection
  def encode = TeardownConnection.encode(this)
}

object TeardownConnection extends Marshallable[TeardownConnection] {
  implicit val codec: Codec[TeardownConnection] = (
    ("nonce" | uint32L) ::
      ("unk" | uint16).unit(6)
  ).as[TeardownConnection]
}
