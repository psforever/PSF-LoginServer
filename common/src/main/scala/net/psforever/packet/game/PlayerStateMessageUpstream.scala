// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * Constantly sent from the client to the server to update player avatar properties.<br>
  * <br>
  * Exploration:<br>
  * `seq_time` appears to be used in other message definitions as well.
  * It seems to represent a timestamp for ordering, e.g., player and weapon events.
  * @param avatar_guid the player's GUID
  * @param pos where the player is in the world
  * @param vel how the player is moving
  * @param unk1 na
  * @param aim_pitch the vertical angle of viewing
  * @param unk2 na
  * @param seq_time na
  * @param unk3 na
  * @param is_crouching whether the player is crouched
  * @param unk4 na
  * @param unk5 na
  * @param is_cloaking whether the player is cloaked by virtue of an Infiltration Suit
  * @param unk6 na
  * @param unk7 na
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
                                            is_cloaking : Boolean,
                                            unk6 : Int,
                                            unk7 : Int)
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
      ("is_cloaking" | bool) ::
      ("unk6" | uint8L) ::
      ("unk7" | uint16L)
    ).as[PlayerStateMessageUpstream]
}
