// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  *
  */
final case class ActionProgressMessage(unk1 : Int,
                                       unk2 : Long)
  extends PlanetSideGamePacket {
  type Packet = ActionProgressMessage
  def opcode = GamePacketOpcode.ActionProgressMessage
  def encode = ActionProgressMessage.encode(this)
}

object ActionProgressMessage extends Marshallable[ActionProgressMessage] {
  implicit val codec : Codec[ActionProgressMessage] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint32L)
    ).as[ActionProgressMessage]
}