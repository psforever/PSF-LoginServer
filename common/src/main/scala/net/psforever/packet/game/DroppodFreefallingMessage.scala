// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class DroppodFreefallingMessage(guid : PlanetSideGUID,
                                           pos : Vector3,
                                           vel : Vector3,
                                           pos2 : Vector3,
                                           orientation1 : Vector3,
                                           orientation2 : Vector3)
  extends PlanetSideGamePacket {
  type Packet = DroppodFreefallingMessage
  def opcode = GamePacketOpcode.DroppodFreefallingMessage
  def encode = DroppodFreefallingMessage.encode(this)
}

object DroppodFreefallingMessage extends Marshallable[DroppodFreefallingMessage] {
  implicit val codec : Codec[DroppodFreefallingMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_float) ::
      ("vel" | Vector3.codec_float) ::
      ("pos2" | Vector3.codec_float) ::
      ("unkA" | Angular.codec_roll) ::
      ("unkB" | Angular.codec_pitch) ::
      ("unkC" | Angular.codec_yaw()) ::
      ("unkD" | Angular.codec_roll) ::
      ("unkE" | Angular.codec_pitch) ::
      ("unkF" | Angular.codec_yaw())
    ).xmap[DroppodFreefallingMessage](
    {
      case guid :: pos :: vel :: pos2 :: uA :: uB :: uC :: uD :: uE :: uF :: HNil =>
        DroppodFreefallingMessage(guid, pos, vel, pos2, Vector3(uA, uB, uC), Vector3(uD, uE, uF))
    },
    {
      case DroppodFreefallingMessage(guid, pos, vel, pos2, Vector3(uA, uB, uC), Vector3(uD, uE, uF)) =>
        guid :: pos :: vel :: pos2 :: uA :: uB :: uC :: uD :: uE :: uF :: HNil
    }
  )
}
