// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Instructs an avatar to be stood, to look, and to move, in a certain way.<br>
  * <br>
  * The position defines a coordinate location in the avatar's current zone to which the avatar is immediately moved
  * This movement is instantaneous and has no associated animation.
  * If velocity is defined, the avatar is provided an "external force" that "pushes" the avatar in a given direction.
  * This external force is not accumulative.
  * Also, the external force is only applied once the avatar is set to the provided position.<br>
  * <br>
  * The angle defines the center of a range of angles that count as "in front of the avatar."
  * Specifically, this range is the upper body's turn limit.
  * A stationary player may look left and right, rotating their upper body only, until they hit a certain angle.
  * Normally, the player's whole body will then turn to accommodate turning further than this angle.
  * This packet marks that limit as a hard limit for rotation and will reset the player's model and camera if necessary.
  * While it is in effect, the player will not turn their whole body once they can no longer turn their upper body.
  * @param unk na
  * @param pos the position to move the character to in the world environment
  * @param viewYawLim the center of the range of upper body angles, the player's actual yaw;
  *                   if this value is beyond its angular limit values,
  *                   the model will attempt to snap to what it considers the closest upper body turning limit angle;
  *                   the actual range is approximately `viewYawLimit +/- 61.8215`;
  * @param vel if defined, the velocity to apply to to the character at the given position
  * @see `PlayerStateMessageUpstream.facingYawUpper`
  * @see `PlayerStateMessage.facingYawUpper`
  */
final case class ShiftState(unk : Int,
                            pos : Vector3,
                            viewYawLim : Float,
                            vel : Option[Vector3])

/**
  * Push specific motion-based stimuli on a specific character.<br>
  * <br>
  * `PlayerStateMessageUpstream` involves data transmitted from a client to the server regarding its avatar.
  * `PlayerStateMessage` involves data transmitted from the server to the clients regarding characters other than that client's avatar.
  * `PlayerStateShiftMessage` involves data transmitted from the server to a client about that client's avatar.
  * It temporarily asserts itself before normal player movement and asserts specific placement and motion.
  * An application of this packet is being `/warp`ed within a zone via a non-triggering agent (like a teleporter).
  * Another, more common, application of this packet is being thrown about when the target of an attempted roadkill.
  * @param state if defined, the behaviors to influence the character
  * @param unk na
  */
final case class PlayerStateShiftMessage(state : Option[ShiftState],
                                         unk : Option[Int] = None)
  extends PlanetSideGamePacket {
  type Packet = PlayerStateShiftMessage
  def opcode = GamePacketOpcode.PlayerStateShiftMessage
  def encode = PlayerStateShiftMessage.encode(this)
}

object ShiftState {
  /**
    * An abbreviated constructor for creating `ShiftState`, assuming velocity is not applied.
    * @param unk na
    * @param pos the position of the character in the world environment
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @param vel the velocity to apply to to the character at the given position
    * @return a `ShiftState` object
    */
  def apply(unk : Int, pos : Vector3, viewYawLim : Float, vel : Vector3) : ShiftState =
    ShiftState(unk, pos, viewYawLim, Some(vel))

  /**
    * An abbreviated constructor for creating `ShiftState`, removing the optional condition of all parameters.
    * @param unk na
    * @param pos the position of the character in the world environment
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @return a `ShiftState` object
    */
  def apply(unk : Int, pos : Vector3, viewYawLim : Float) : ShiftState =
    ShiftState(unk, pos, viewYawLim, None)
}

object PlayerStateShiftMessage extends Marshallable[PlayerStateShiftMessage] {
  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, removing the optional condition of `state`.
    * @param state the behaviors to influence the character
    * @param unk na
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(state : ShiftState, unk : Int) : PlayerStateShiftMessage =
    PlayerStateShiftMessage(Some(state), Some(unk))

  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, removing the optional condition of `unk2`.
    * @param state the behaviors to influence the character
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(state : ShiftState) : PlayerStateShiftMessage =
    PlayerStateShiftMessage(Some(state), None)

  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, assuming the parameters `unk1` and `state` are not defined.
    * @param unk na
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(unk : Int) : PlayerStateShiftMessage =
    PlayerStateShiftMessage(None, Some(unk))

  private val shift_codec : Codec[ShiftState] = (
    /*
    IMPORTANT:
    Packet data indicates that viewYawLimit is an 8u value.
    When read as an 8u value, the resulting number does not map to directions properly.
    As a 7u value, the numbers maps better so the first bit will be ignored.
     */
    ("unk" | uintL(3)) ::
      ("pos" | Vector3.codec_pos) ::
      ("viewYawLim" | Angular.codec_yaw()) ::
      optional(bool, "pos" | Vector3.codec_vel)
    ).xmap[ShiftState] (
    {
      case a :: b :: c :: d :: HNil =>
        ShiftState(a, b, c, d)
    },
    {
      case ShiftState(a, b, c, d) =>
        a :: b :: c :: d :: HNil
    }
  ).as[ShiftState]

  implicit val codec : Codec[PlayerStateShiftMessage] = (
    optional(bool, "state" | shift_codec) ::
      optional(bool, "unk" | uintL(3))
    ).xmap[PlayerStateShiftMessage] (
      {
        case a :: b :: HNil =>
          PlayerStateShiftMessage(a, b)
      },
      {
        case PlayerStateShiftMessage(a, b) =>
          a :: b :: HNil
      }
    ).as[PlayerStateShiftMessage]
}
