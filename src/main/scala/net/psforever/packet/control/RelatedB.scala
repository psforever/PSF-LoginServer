// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs._

/**
  * Dispatched to coordinate information regarding `ControlPacket` packets between the client and server.
  * When dispatched by the client, it relates the current (or last received) `SlottedMetaPacket` `subslot` number back to the server.
  * When dispatched by the server, it relates ???
  * @param slot the type of `ResultB` packet;
  *             valid types are integers 0-3
  * @param subslot identification of a control packet
  */
final case class RelatedB(slot: Int, subslot: Int) extends PlanetSideControlPacket {
  type Packet = RelatedB
  if (slot < 0 || slot > 3) {
    throw new IllegalArgumentException(s"slot number is out of range - $slot")
  }

  def opcode = {
    val base = ControlPacketOpcode.RelatedB0.id
    ControlPacketOpcode(base + slot)
  }
  def encode = RelatedB.encode(this).map(vect => vect.drop(8))
}

object RelatedB extends Marshallable[RelatedB] {
  implicit val codec: Codec[RelatedB] = (
    ("slot" | uint8L.xmap[Int](a => a - ControlPacketOpcode.RelatedB0.id, a => a)) ::
      ("subslot" | uint16) // the slot is big endian. see 0x00A42F76
  ).as[RelatedB]

  def decodeWithOpcode(slot: ControlPacketOpcode.Value)(bits: BitVector) = {
    decode(ControlPacketOpcode.codec.encode(slot).require ++ bits)
  }
}
