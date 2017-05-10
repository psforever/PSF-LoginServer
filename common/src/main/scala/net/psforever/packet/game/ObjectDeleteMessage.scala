// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ObjectDeleteMessage(object_guid : PlanetSideGUID,
                                     unk1 : Int)
  extends PlanetSideGamePacket {
  type Packet = ObjectDeleteMessage
  def opcode = GamePacketOpcode.ObjectDeleteMessage
  def encode = ObjectDeleteMessage.encode(this)
}

object ObjectDeleteMessage extends Marshallable[ObjectDeleteMessage] {
  implicit val codec : Codec[ObjectDeleteMessage] = (
      ("object_guid" | PlanetSideGUID.codec) ::
        ("unk1" | uintL(3))
    ).as[ObjectDeleteMessage]
}
