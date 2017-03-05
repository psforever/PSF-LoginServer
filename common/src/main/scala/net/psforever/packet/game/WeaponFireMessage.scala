// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/** WeaponFireMessage seems to be sent each time a weapon actually shoots.
  *
  * See [[PlayerStateMessageUpstream]] for explanation of seq_time.
  */
final case class WeaponFireMessage(seq_time : Int,
                                   weapon_guid : PlanetSideGUID,
                                   projectile_guid : PlanetSideGUID,
                                   shot_origin : Vector3,
                                   unk1 : Int,
                                   unk2 : Int,
                                   unk3 : Int,
                                   unk4 : Int,
                                   unk5 : Int,
                                   unk6 : Int,
                                   unk7 : Option[Option[Vector3]])
  extends PlanetSideGamePacket {
  type Packet = WeaponFireMessage
  def opcode = GamePacketOpcode.WeaponFireMessage
  def encode = WeaponFireMessage.encode(this)
}

object WeaponFireMessage extends Marshallable[WeaponFireMessage] {
  implicit val codec : Codec[WeaponFireMessage] = (
      ("seq_time" | uintL(10)) ::
        ("weapon_guid" | PlanetSideGUID.codec) ::
        ("projectile_guid" | PlanetSideGUID.codec) ::
        ("shot_origin" | Vector3.codec_pos) ::
        ("unk1" | uint16L) ::
        ("unk2" | uint16L) ::
        ("unk3" | uint16L) ::
        ("unk4" | uint16L) ::
        ("unk5" | uint8L) ::
        (("unk6" | uintL(3)) >>:~ { unk6_value =>
          conditional(unk6_value == 3, ("unk7" | optional(bool, Vector3.codec_vel))).hlist
        })
    ).as[WeaponFireMessage]
}
