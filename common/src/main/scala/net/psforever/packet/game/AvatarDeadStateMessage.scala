// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param unk1 0 = nothing, 1 = waiting for a rez, 2 = auto map to select spawn, 3 = respawn time
  * @param unk2 na
  * @param unk3 spawn penality
  * @param pos last victim's position
  * @param unk4 na
  * @param unk5 na
  */
final case class AvatarDeadStateMessage(unk1 : Int,
                                        unk2 : Long,
                                        unk3 : Long,
                                        pos : Vector3,
                                        unk4 : Long,
                                        unk5 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = AvatarDeadStateMessage
  def opcode = GamePacketOpcode.AvatarDeadStateMessage
  def encode = AvatarDeadStateMessage.encode(this)
}

object AvatarDeadStateMessage extends Marshallable[AvatarDeadStateMessage] {
  implicit val codec : Codec[AvatarDeadStateMessage] = (
    ("unk1" | uintL(3)) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk4" | uint32L) ::
      ("unk5" | bool)
    ).as[AvatarDeadStateMessage]
}
