// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to coordinate information regarding `ControlPacket` packets between the client and server.
  * When dispatched by the client, it relates the current (or last received) `SlottedMetaPacket` `subslot` number back to the server.
  * When dispatched by the server, it relates ???
  * @param subslot identification of a control packet
  */
final case class RelatedB0(subslot : Int)
  extends PlanetSideControlPacket {
  type Packet = RelatedB0
  def opcode = ControlPacketOpcode.RelatedB0
  def encode = RelatedB0.encode(this)
}

object RelatedB0 extends Marshallable[RelatedB0] {
  implicit val codec : Codec[RelatedB0] = ("subslot" | uint16).as[RelatedB0]
}
