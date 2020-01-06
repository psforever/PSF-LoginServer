// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class ObjectHeldMessage(avatar_guid : PlanetSideGUID,
                                   held_holsters : Int,
                                   unk1 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = ObjectHeldMessage
  def opcode = GamePacketOpcode.ObjectHeldMessage
  def encode = ObjectHeldMessage.encode(this)
}

object ObjectHeldMessage extends Marshallable[ObjectHeldMessage] {
  implicit val codec : Codec[ObjectHeldMessage] = (
      ("avatar_guid" | PlanetSideGUID.codec) ::
        ("held_holsters" | uint8L) ::
        ("unk1" | bool)
    ).as[ObjectHeldMessage]
}
