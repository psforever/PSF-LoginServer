// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ChildObjectStateMessage(unk1 : Int,
                                         unk2 : Int,
                                         unk3 : Int)
  extends PlanetSideGamePacket {
  type Packet = ChildObjectStateMessage
  def opcode = GamePacketOpcode.ChildObjectStateMessage
  def encode = ChildObjectStateMessage.encode(this)
}

object ChildObjectStateMessage extends Marshallable[ChildObjectStateMessage] {
  implicit val codec : Codec[ChildObjectStateMessage] = (
    ("unk1" | uint16L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L)
    ).as[ChildObjectStateMessage]
}
