// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * 6,7,8 - Start implant initialization timer for slots 0,1,2 respectively. Allowed values: 0-100 (50 will start timer at 50% complete)
  */
final case class ActionProgressMessage(action: Int, unk2: Long) extends PlanetSideGamePacket {
  type Packet = ActionProgressMessage
  def opcode = GamePacketOpcode.ActionProgressMessage
  def encode = ActionProgressMessage.encode(this)
}

object ActionProgressMessage extends Marshallable[ActionProgressMessage] {
  implicit val codec: Codec[ActionProgressMessage] = (
    ("action" | uint4L) ::
      ("unk2" | uint32L)
  ).as[ActionProgressMessage]
}
