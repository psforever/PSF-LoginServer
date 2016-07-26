// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/** PlayerStateMessageUpstream is constantly sent from the client to the server to update avatar properties.
  *
  * Note: seq_time appears to be used in other message definitions as well. It
  * seems to represent a timestamp for ordering of e.g. player and weapon events.
  */
final case class PlayerStateMessageUpstream(avatar_guid : PlanetSideGUID,
                                            pos : Vector3,
                                            vel : Option[Vector3],
                                            unk1 : Int,
                                            aim_pitch : Int,
                                            unk2 : Int,
                                            seq_time : Int,
                                            unk3 : Int,
                                            is_crouching : Boolean,
                                            unk4 : Boolean,
                                            unk5 : Boolean,
                                            unk6 : Boolean,
                                            unk7 : Int,
                                            unk8 : Int)
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
      ("seq_time" | uintL(10)) ::
      ("unk3" | uintL(3)) ::
      ("is_crouching" | bool) ::
      ("unk4" | bool) ::
      ("unk5" | bool) ::
      ("unk6" | bool) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint16L)
    ).as[PlayerStateMessageUpstream]
}
