// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

final case class UseItemMessage(avatar_guid : PlanetSideGUID,
                                unk1 : Int,
                                object_guid : PlanetSideGUID,
                                unk2 : Long,
                                unk3 : Boolean,
                                unk4 : Vector3,
                                unk5 : Vector3,
                                unk6 : Int,
                                unk7 : Int,
                                unk8 : Int,
                                unk9 : Long)
  extends PlanetSideGamePacket {
  type Packet = UseItemMessage
  def opcode = GamePacketOpcode.UseItemMessage
  def encode = UseItemMessage.encode(this)
}

object UseItemMessage extends Marshallable[UseItemMessage] {
  implicit val codec : Codec[UseItemMessage] = (
    ("avatar_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint16L) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("unk2" | uint32L) ::
      ("unk3" | bool) ::
      ("unk4" | Vector3.codec_pos) ::
      ("unk5" | Vector3.codec_pos) ::
      ("unk6" | uint8L) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint32L)
    ).as[UseItemMessage]
}
