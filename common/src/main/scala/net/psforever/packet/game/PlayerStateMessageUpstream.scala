// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class PlayerStateMessageUpstream(some_field : Int) extends PlanetSideGamePacket {
  type Packet = PlayerStateMessageUpstream
  def opcode = GamePacketOpcode.PlayerStateMessageUpstream
  def encode = PlayerStateMessageUpstream.encode(this)
}

object PlayerStateMessageUpstream extends Marshallable[PlayerStateMessageUpstream] {
  implicit val codec : Codec[PlayerStateMessageUpstream] = ("some_field" | uint8L).as[PlayerStateMessageUpstream]
}