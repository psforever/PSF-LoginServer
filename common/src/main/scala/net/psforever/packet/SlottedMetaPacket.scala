// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs._

final case class SlottedMetaPacket(/*slot : Int,*/ packet : ByteVector)
  extends PlanetSideControlPacket {
  type Packet = SlottedMetaPacket

  //assert(slot >= 0 && slot <= 7, "Slot number is out of range")

  def opcode = {
    val base = ControlPacketOpcode.SlottedMetaPacket0.id
    ControlPacketOpcode(base/* + slot*/)
  }

  def encode = SlottedMetaPacket.encode(this)
}

object SlottedMetaPacket extends Marshallable[SlottedMetaPacket] {
  implicit val codec : Codec[SlottedMetaPacket] = (
    ("unknown" | constant(0)) ::
    ("unknown" | constant(0)) ::
    ("rest" | bytes)
  ).as[SlottedMetaPacket]
}