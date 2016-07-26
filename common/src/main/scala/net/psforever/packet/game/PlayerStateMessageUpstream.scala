// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

final case class PlayerStateMessageUpstream(avatar_guid : PlanetSideGUID,
                                            pos : Vector3,
                                            vel : Option[Vector3],
                                            unk1 : Int,
                                            aim_pitch : Int,
                                            unk2 : Int,
                                            unk3 : Int,
                                            unk4 : Int,
                                            is_crouching : Boolean,
                                            unk5 : Boolean,
                                            unk6 : Boolean,
                                            unk7 : Boolean,
                                            unk8 : Int,
                                            unk9 : Int)
  extends PlanetSideGamePacket {
  type Packet = PlayerStateMessageUpstream
  def opcode = GamePacketOpcode.PlayerStateMessageUpstream
  def encode = PlayerStateMessageUpstream.encode(this)
}

object PlayerStateMessageUpstream extends Marshallable[PlayerStateMessageUpstream] {
  implicit val codec : Codec[PlayerStateMessageUpstream] = (
      ("avatar_guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("vel" | optional(bool, Vector3.codec_vel)) ::
      ("unk1" | uint8L) ::
      ("aim_pitch" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uintL(10)) ::
      ("unk4" | uintL(3)) ::
      ("is_crouching" | bool) ::
      ("unk5" | bool) ::
      ("unk6" | bool) ::
      ("unk7" | bool) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint16L)
    ).as[PlayerStateMessageUpstream]
}
