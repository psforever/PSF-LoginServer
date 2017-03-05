// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ChangeAmmoMessage(item_guid : PlanetSideGUID,
                                   unk1 : Long)
  extends PlanetSideGamePacket {
  type Packet = ChangeAmmoMessage
  def opcode = GamePacketOpcode.ChangeAmmoMessage
  def encode = ChangeAmmoMessage.encode(this)
}

object ChangeAmmoMessage extends Marshallable[ChangeAmmoMessage] {
  implicit val codec : Codec[ChangeAmmoMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("unk1" | uint32L)
    ).as[ChangeAmmoMessage]
}
