// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class AvatarFirstTimeEventMessage(avatar_guid : PlanetSideGUID,
                                             object_guid : PlanetSideGUID,
                                             unk1 : Long,
                                             message : String)
  extends PlanetSideGamePacket {
  type Packet = AvatarFirstTimeEventMessage
  def opcode = GamePacketOpcode.AvatarFirstTimeEventMessage
  def encode = AvatarFirstTimeEventMessage.encode(this)
}

object AvatarFirstTimeEventMessage extends Marshallable[AvatarFirstTimeEventMessage] {
  implicit val codec : Codec[AvatarFirstTimeEventMessage] = (
     ("avatar_guid" | PlanetSideGUID.codec) ::
     ("object_guid" | PlanetSideGUID.codec) ::
     ("unk1" | uint32L ) ::
     ("message" | PacketHelpers.encodedString)
    ).as[AvatarFirstTimeEventMessage]
}
