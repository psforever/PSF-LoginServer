// Copyright (c) 2017 PSForever
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
  * @param facingYaw the angle with respect to the horizon towards which the avatar is looking;
  *                  the model's whole body is facing this direction;
  *                  measurements are counter-clockwise from East
  * @param facingPitch the angle with respect to the sky and the ground towards which the avatar is looking
  * @param facingYawUpper the angle of the avatar's upper body with respect to its forward-facing direction
  * @param seq_time na
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
                                            facingYaw : Int,
                                            facingPitch : Int,
                                            facingYawUpper : Int,
                                            seq_time : Int,
                                            unk1 : Int,
                                            is_crouching : Boolean,
                                            is_jumping : Boolean,
                                            jump_thrust : Boolean,
                                            is_cloaked : Boolean,
                                            unk2 : Int,
                                            unk3 : Int)
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
      ("facingYaw" | uint8L) ::
      ("facingPitch" | uint8L) ::
      ("facingYawUpper" | uint8L) ::
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
