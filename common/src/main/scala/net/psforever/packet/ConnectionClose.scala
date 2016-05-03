// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.Codec

final case class ConnectionClose()
  extends PlanetSideControlPacket {
  type Packet = ConnectionClose
  def opcode = ControlPacketOpcode.ConnectionClose
  def encode = ConnectionClose.encode(this)
}

object ConnectionClose extends Marshallable[ConnectionClose] {
  implicit val codec: Codec[ConnectionClose] = PacketHelpers.emptyCodec(ConnectionClose())
}