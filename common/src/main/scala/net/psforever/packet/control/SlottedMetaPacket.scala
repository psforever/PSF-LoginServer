// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.{BitVector, ByteOrdering, ByteVector}
import scodec.codecs._

final case class SlottedMetaPacket(slot : Int, subslot : Int, packet : ByteVector)
  extends PlanetSideControlPacket {
  type Packet = SlottedMetaPacket

  assert(slot >= 0 && slot <= 7, s"Slot number ($slot) is out of range")

  def opcode = {
    val base = ControlPacketOpcode.SlottedMetaPacket0.id
    ControlPacketOpcode(base + slot)
  }

  // XXX: a nasty hack to ignore the "slot" field
  // There is so much wrong with this it's not even funny. Why scodec, whyyyy...
  // I've never had a library make me feel so stupid and smart at the same time
  def encode = SlottedMetaPacket.encode(this).map(vect => vect.drop(8))
}

object SlottedMetaPacket extends Marshallable[SlottedMetaPacket] {
  implicit val codec : Codec[SlottedMetaPacket] = (
    ("slot" | uint8L.xmap[Int](a => a - ControlPacketOpcode.SlottedMetaPacket0.id, a=>a) ) ::
    ("subslot" | uint16) :: // the slot is big endian. see 0x00A42F76
    ("rest" | bytes)
  ).as[SlottedMetaPacket]

  def decodeWithOpcode(slot : ControlPacketOpcode.Value)(bits : BitVector) = {
    decode(ControlPacketOpcode.codec.encode(slot).require ++ bits)
  }
}