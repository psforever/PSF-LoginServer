// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs._

/**
  * Dispatched from the client in regards to errors trying to process prior `ControlPackets`.
  * Explains which packet was in error by sending back its `subslot` number.
  * @param slot the type of `ResultA` packet;
  *             valid types are integers 0-3
  * @param subslot identification of a control packet
  */
final case class RelatedA(slot: Int, subslot: Int) extends PlanetSideControlPacket {
  type Packet = RelatedA
  if (slot < 0 || slot > 3) {
    throw new IllegalArgumentException(s"slot number is out of range - $slot")
  }

  def opcode = {
    val base = ControlPacketOpcode.RelatedA0.id
    ControlPacketOpcode(base + slot)
  }
  def encode = RelatedA.encode(this).map(vect => vect.drop(8))
}

object RelatedA extends Marshallable[RelatedA] {
  implicit val codec: Codec[RelatedA] = (
    ("slot" | uint8L.xmap[Int](a => a - ControlPacketOpcode.RelatedA0.id, a => a)) ::
      ("subslot" | uint16) // the slot is big endian. see 0x00A42F76
  ).as[RelatedA]

  def decodeWithOpcode(slot: ControlPacketOpcode.Value)(bits: BitVector) = {
    decode(ControlPacketOpcode.codec.encode(slot).require ++ bits)
  }
}
