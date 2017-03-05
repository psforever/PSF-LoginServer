// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.{BitVector, ByteOrdering, ByteVector}
import scodec.codecs._

final case class SlottedMetaAck(slot : Int, subslot : Int)
  extends PlanetSideControlPacket {
  type Packet = SlottedMetaAck

  assert(slot >= 0 && slot <= 7, s"Slot number ($slot) is out of range")

  def opcode = {
    val base = ControlPacketOpcode.RelatedB0.id
    ControlPacketOpcode(base + slot % 4)
  }

  // XXX: a nasty hack to ignore the "slot" field
  // There is so much wrong with this it's not even funny. Why scodec, whyyyy...
  // I've never had a library make me feel so stupid and smart at the same time
  def encode = SlottedMetaAck.encode(this).map(vect => vect.drop(8))
}

object SlottedMetaAck extends Marshallable[SlottedMetaAck] {
  implicit val codec : Codec[SlottedMetaAck] = (
    ("slot" | uint8L.xmap[Int](a => a - ControlPacketOpcode.RelatedB0.id, a=>a) ) ::
      ("subslot" | uint16)
    ).as[SlottedMetaAck]

  def decodeWithOpcode(slot : ControlPacketOpcode.Value)(bits : BitVector) = {
    decode(ControlPacketOpcode.codec.encode(slot).require ++ bits)
  }
}