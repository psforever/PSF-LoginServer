// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

object DeadState extends Enumeration {
  type Type = Value

  val
  Nothing,
  Dead,
  Release,
  RespawnTime
    = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(3))
}

/**
  * na
  * @param state avatar's relationship with the world
  * @param timer_max total length of respawn countdown, in milliseconds
  * @param timer initial length of the respawn timer, in milliseconds
  * @param pos last position
  * @param unk4 na
  * @param unk5 na
  */
final case class AvatarDeadStateMessage(state : DeadState.Value,
                                        timer_max : Long,
                                        timer : Long,
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
    ("state" | DeadState.codec) ::
      ("timer_max" | uint32L) ::
      ("timer" | uint32L) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk4" | uint32L) ::
      ("unk5" | bool)
    ).as[AvatarDeadStateMessage]
}
