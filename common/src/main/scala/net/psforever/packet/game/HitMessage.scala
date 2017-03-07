// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

final case class HitInfo(shot_origin : Vector3,
                         hit_pos : Vector3,
                         hitobject_guid : Option[PlanetSideGUID])

final case class HitMessage(seq_time : Int,
                            projectile_guid : PlanetSideGUID,
                            unk1 : Int,
                            hit_info : Option[HitInfo],
                            unk2 : Boolean,
                            unk3 : Boolean,
                            unk4 : Option[Int])
  extends PlanetSideGamePacket {
  type Packet = HitMessage
  def opcode = GamePacketOpcode.HitMessage
  def encode = HitMessage.encode(this)
}

object HitMessage extends Marshallable[HitMessage] {
  implicit val codec_hitinfo : Codec[HitInfo] = (
    ("shot_origin" | Vector3.codec_pos) ::
      ("hit_pos" | Vector3.codec_pos) ::
      ("hitobject_guid" | optional(bool, PlanetSideGUID.codec))
    ).as[HitInfo]

  implicit val codec : Codec[HitMessage] = (
    ("seq_time" | uintL(10)) ::
      ("projectile_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(3)) ::
      ("hit_info" | optional(bool, codec_hitinfo)) ::
      ("unk2" | bool) ::
      ("unk3" | bool) ::
      ("unk4" | optional(bool, uint16L))
    ).as[HitMessage]
}
