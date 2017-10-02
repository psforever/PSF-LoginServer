// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._

final case class HandleGamePacket(len : Int,
                                  stream : ByteVector,
                                  rest : BitVector = BitVector.empty)
  extends PlanetSideControlPacket {
  def opcode = ControlPacketOpcode.HandleGamePacket
  def encode = HandleGamePacket.encode(this)
}

object HandleGamePacket extends Marshallable[HandleGamePacket] {
  def apply(stream : ByteVector) : HandleGamePacket = {
    new HandleGamePacket(stream.length.toInt, stream)
  }

  implicit val codec : Codec[HandleGamePacket] = (
    ("len" | uint16) >>:~ { len =>
      ("stream" | bytes(len)) ::
        ("rest" | bits)
    }).as[HandleGamePacket]
}
