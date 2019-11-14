// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Angular
import scodec.Codec
import scodec.codecs._

final case class DamageMessage(guid1 : PlanetSideGUID,
                               unk1 : Int,
                               guid2 : PlanetSideGUID,
                               unk2 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = DamageMessage
  def opcode = GamePacketOpcode.DamageMessage
  def encode = DamageMessage.encode(this)
}

object DamageMessage extends Marshallable[DamageMessage] {
  implicit val codec : Codec[DamageMessage] = (
    ("guid1" | PlanetSideGUID.codec) ::
      ("unk1" | uint8) ::
      ("guid1" | PlanetSideGUID.codec) ::
      ("unk2" | bool)
    ).as[DamageMessage]
}
