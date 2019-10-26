// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, Vector3}
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
  * @param facingYaw a "yaw" angle
  * @param facingPitch a "pitch" angle
  * @param facingYawUpper a "yaw" angle that represents the angle of the avatar's upper body with respect to its forward-facing direction;
  *                       this number is normally 0 for forward facing;
  *                       the range is limited between approximately 61 degrees of center turned to left or right
  * @param seq_time the "time frame" according to the server;
  *                 starts at 0; max value is 1023 before resetting
  * @param unk1 na
  * @param is_crouching avatar is crouching
  * @param is_jumping avatar is jumping;
  *                   must remain flagged for jump to maintain animation
  * @param jump_thrust provide a measure of vertical stability when really close to the avatar character
  * @param is_cloaked avatar is cloaked by virtue of an Infiltration Suit
  * @param unk2 na
  * @param unk3 na
  */
final case class PlayerStateMessageUpstream(avatar_guid : PlanetSideGUID,
                                            pos : Vector3,
                                            vel : Option[Vector3],
                                            facingYaw : Float,
                                            facingPitch : Float,
                                            facingYawUpper : Float,
                                            seq_time : Int,
                                            unk1 : Int,
                                            is_crouching : Boolean,
                                            is_jumping : Boolean,
                                            jump_thrust : Boolean,
                                            is_cloaked : Boolean,
                                            unk2 : Int,
                                            unk3 : Int)
  extends PlanetSideGamePacket {
  assert(seq_time > -1 && seq_time < 1024)

  type Packet = PlayerStateMessageUpstream
  def opcode = GamePacketOpcode.PlayerStateMessageUpstream
  def encode = PlayerStateMessageUpstream.encode(this)
}

object PlayerStateMessageUpstream extends Marshallable[PlayerStateMessageUpstream] {
  implicit val codec : Codec[PlayerStateMessageUpstream] = (
    ("avatar_guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("vel" | optional(bool, Vector3.codec_vel)) ::
      ("facingYaw" | Angular.codec_yaw()) ::
      ("facingPitch" | Angular.codec_pitch) ::
      ("facingYawUpper" | Angular.codec_yaw(0f)) ::
      ("seq_time" | uintL(10)) ::
      ("unk1" | uintL(3)) ::
      ("is_crouching" | bool) ::
      ("is_jumping" | bool) ::
      ("jump_thrust" | bool) ::
      ("is_cloaked" | bool) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint16L)
    ).as[PlayerStateMessageUpstream]
}
