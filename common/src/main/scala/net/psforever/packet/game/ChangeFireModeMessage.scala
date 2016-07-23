// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ChangeFireModeMessage(item_guid : PlanetSideGUID,
                                       fire_mode : Int)
  extends PlanetSideGamePacket {
  type Packet = ChangeFireModeMessage
  def opcode = GamePacketOpcode.ChangeFireModeMessage
  def encode = ChangeFireModeMessage.encode(this)
}

object ChangeFireModeMessage extends Marshallable[ChangeFireModeMessage] {
  implicit val codec : Codec[ChangeFireModeMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("fire_mode" | uintL(3))
    ).as[ChangeFireModeMessage]
}
