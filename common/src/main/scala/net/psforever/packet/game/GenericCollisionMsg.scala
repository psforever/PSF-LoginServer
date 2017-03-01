// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

final case class GenericCollisionMsg(unk1 : Int,
                                     player : PlanetSideGUID,
                                     target : PlanetSideGUID,
                                     player_health : Int,
                                     target_health : Int,
                                     player_velocity_x : Float,
                                     player_velocity_y : Float,
                                     player_velocity_z : Float,
                                     target_velocity_x : Float,
                                     target_velocity_y : Float,
                                     target_velocity_z : Float,
                                     player_pos : Vector3,
                                     target_pos : Vector3,
                                     unk2 : Long,
                                     unk3 : Long,
                                     unk4 : Long)
  extends PlanetSideGamePacket {
  type Packet = GenericCollisionMsg
  def opcode = GamePacketOpcode.GenericCollisionMsg
  def encode = GenericCollisionMsg.encode(this)
}

object GenericCollisionMsg extends Marshallable[GenericCollisionMsg] {
  implicit val codec : Codec[GenericCollisionMsg] = (
    ("unk1" | uint2) ::
      ("p" | PlanetSideGUID.codec) ::
      ("t" | PlanetSideGUID.codec) ::
      ("p_health" | uint16L) ::
      ("t_health" | uint16L) ::
      ("p_vel_x" | floatL) ::
      ("p_vel_y" | floatL) ::
      ("p_vel_z" | floatL) ::
      ("t_vel_x" | floatL) ::
      ("t_vel_y" | floatL) ::
      ("t_vel_z" | floatL) ::
      ("p_pos" | Vector3.codec_pos) ::
      ("t_pos" | Vector3.codec_pos) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L) ::
      ("unk4" | uint32L)
    ).as[GenericCollisionMsg]
}
