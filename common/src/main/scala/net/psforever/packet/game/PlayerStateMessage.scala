// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The server instructs some clients to render a player (usually not that client's avatar) to move in a certain way.<br>
  * <br>
  * This packet instructs the basic aspects of how the player character is positioned and how the player character moves.
  * Each client keeps track of where a character "currently" is according to that client.
  * `pos` reflects an update in regards to where the character should be moved.
  * Data between this "currently" and "new" are interpolated over a fixed time interval.
  * Position and velocity data is standard to normal PlanetSide ranges.
  * All angles follow the convention that every `0x1` is about 2.8125 degrees; so, `0x10` is 45.0 degrees.<br>
  * <br>
  * The avatar model normally moves from where it "currently" is to `pos`.
  * When `vel` is defined, `pos` is treated as where the avatar model starts its animation.
  * In that case, it appears to teleport to `pos` to carry out the interpolated movement according to `vel`.
  * After the move, it remains at essentially `pos + vel * t`.
  * The repositioning always takes the same amount of time.
  * The player model is left in a walking/running animation (in place) until directed otherwise.<br>
  * <br>
  * If the model must interact with the environment during a velocity-driven move, it copes with local physics.
  * A demonstration of this is what happens when one player "runs past"/"into" another player running up stairs.
  * The climbing player is frequently reported by the other to appear to bounce over that player's head.
  * If the other player is off the ground, passing too near to the observer can cause a rubber band effect on trajectory.
  * This effect is entirely client-side to the observer and affects the moving player in no way.
  * @param guid the avatar's guid
  * @param pos the position of the avatar in the world environment (in three coordinates)
  * @param vel an optional velocity
  * @param facingYaw a "yaw" angle
  * @param facingPitch a "pitch" angle
  * @param facingYawUpper a "yaw" angle that represents the angle of the avatar's upper body with respect to its forward-facing direction;
  *                       this number is normally 0 for forward facing;
  *                       the range is limited between approximately 61 degrees of center turned to left or right
  * @param timestamp A sequential counter
  * @param is_crouching avatar is crouching
  * @param is_jumping avatar is jumping;
  *                   must remain flagged for jump to maintain animation
  * @param jump_thrust provide a measure of vertical stability when really close to the avatar character
  * @param is_cloaked avatar is cloaked by virtue of an Infiltration Suit
  */
final case class PlayerStateMessage(guid : PlanetSideGUID,
                                    pos : Vector3,
                                    vel : Option[Vector3],
                                    facingYaw : Float,
                                    facingPitch : Float,
                                    facingYawUpper : Float,
                                    timestamp : Int,
                                    is_crouching : Boolean = false,
                                    is_jumping : Boolean = false,
                                    jump_thrust : Boolean = false,
                                    is_cloaked : Boolean = false)
  extends PlanetSideGamePacket {
  type Packet = PlayerStateMessage
  def opcode = GamePacketOpcode.PlayerStateMessage
  def encode = PlayerStateMessage.encode(this)
}

object PlayerStateMessage extends Marshallable[PlayerStateMessage] {
  type fourBoolPattern = Boolean :: Boolean :: Boolean :: Boolean :: HNil

  /**
    * A `Codec` for reading out the four `Boolean` values near the end of the formal packet.
    */
  val booleanCodec : Codec[fourBoolPattern] = (
    ("is_crouching" | bool) ::
      ("is_jumping" | bool) ::
      ("jump_thrust" | bool) ::
      ("is_cloaked" | bool)
    ).as[fourBoolPattern]

  /**
    * A `Codec` for ignoring the four values at the end of the formal packet (all set to `false`).
    */
  val defaultCodec : Codec[fourBoolPattern] = ignore(0).hlist.xmap[fourBoolPattern] (
    {
      case _ :: HNil =>
        false :: false :: false :: false :: HNil
    },
    {
      case _ :: _ :: _ :: _ :: HNil =>
        () :: HNil
    }
  ).as[fourBoolPattern]

  implicit val codec : Codec[PlayerStateMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      optional(bool, "vel" | Vector3.codec_vel) ::
      ("facingYaw" | Angular.codec_yaw()) ::
      ("facingPitch" | Angular.codec_zero_centered) ::
      ("facingYawUpper" | Angular.codec_zero_centered) ::
      ("unk1" | uintL(10)) ::
      (bool >>:~ { fourBools =>
        newcodecs.binary_choice(!fourBools, booleanCodec, defaultCodec)
      })
    ).xmap[PlayerStateMessage] (
    {
      case uid :: pos :: vel :: f1 :: f2 :: f3 :: u :: _ :: b1 :: b2 :: b3 :: b4 :: HNil =>
        PlayerStateMessage(uid, pos, vel, f1, f2, f3, u, b1, b2, b3, b4)
    },
    {
      case PlayerStateMessage(uid, pos, vel, f1, f2, f3, u, b1, b2, b3, b4) =>
        val b : Boolean = !(b1 || b2 || b3 || b4)
        uid :: pos :: vel :: f1 :: f2 :: f3 :: u :: b :: b1 :: b2 :: b3 :: b4 :: HNil
    }
  )
}
