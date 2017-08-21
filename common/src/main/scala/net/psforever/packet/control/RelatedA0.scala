// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the client in regards to errors trying to process prior `ControlPackets`.
  * Explains which packet was in error by sending back its `subslot` number.
  * @param subslot identification of a control packet
  */
final case class RelatedA0(subslot : Int)
  extends PlanetSideControlPacket {
  type Packet = RelatedA0
  def opcode = ControlPacketOpcode.RelatedA0
  def encode = RelatedA0.encode(this)
}

object RelatedA0 extends Marshallable[RelatedA0] {
  implicit val codec : Codec[RelatedA0] = ("subslot" | uint16).as[RelatedA0]
}
