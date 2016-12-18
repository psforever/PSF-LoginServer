// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
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
  * `viewYawLim` defines a "range of angles" that the avatar may look centered on the supplied angle.
  * The avatar must be facing within 60-degrees of that direction, subjectively his left or his right.
  * The avatar's view is immediately set to the closest 60-degree mark if it is outside of that range.
  * The absolute angular displacement of the avatar is considered before applying this corrective behavior.
  * After rotating any number of times:
  * stopping in a valid part of the range is acceptable;
  * stopping in an invalid part of the range will cause the avatar to align to the __earliest__ still-valid 60-degree mark.
  * For that reason, even if the avatar's final angle is closest to the "left mark," it may re-align to the "right mark."
  * This also resets the avatar's angular displacement.
  * @param pos the position to move the character to in the world environment
  * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
  * @param vel if defined, the velocity to apply to to the character at the given position
  */
final case class ShiftState(pos : Vector3,
                             viewYawLim : Int,
                             vel : Option[Vector3])

/**
  * Push specific motion-based stimuli on a specific character.<br>
  * <br>
  * `PlayerStateMessageUpstream` involves data transmitted from a client to the server regarding its avatar.
  * `PlayerStateMessage` involves data transmitted from the server to the clients regarding characters other than that client's avatar.
  * `PlayerStateShiftMessage` involves data transmitted from the server to a client about that client's avatar.
  * It temporarily asserts itself before normal player movement and asserts specific placement and motion.
  * An application of this packet is being `/warp`ed within a zone via a non-triggering agent (like a teleporter).
  * Another, more common, application of this packet is being thrown about when the target of an attempted roadkill.<br>
  * <br>
  * Exploration:<br>
  * What do the leading and trailing values do?
  * @param unk1 na;
  *             seems to have different purposes depending on whether `state` is defined
  * @param state if defined, the behaviors to influence the character
  * @param unk2 na
  */
final case class PlayerStateShiftMessage(unk1 : Int,
                                         state : Option[ShiftState],
                                         unk2 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.PlayerStateShiftMessage
  def encode = PlayerStateShiftMessage.encode(this)
}

object ShiftState extends Marshallable[ShiftState] {
  /**
    * An abbreviated constructor for creating `ShiftState`, assuming velocity is not applied.
    * @param pos the position of the character in the world environment
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @param vel the velocity to apply to to the character at the given position
    * @return a `ShiftState` object
    */
  def apply(pos : Vector3, viewYawLim : Int, vel : Vector3) : ShiftState =
    ShiftState(pos, viewYawLim, Some(vel))

  /**
    * An abbreviated constructor for creating `ShiftState`, removing the optional condition of all parameters.
    * @param pos the position of the character in the world environment
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @return a `ShiftState` object
    */
  def apply(pos : Vector3, viewYawLim : Int) : ShiftState =
    ShiftState(pos, viewYawLim, None)

  implicit val codec : Codec[ShiftState] = (
      ("pos" | Vector3.codec_pos) ::
        ("unk2" | uint8L) ::
        (bool >>:~ { test =>
          ignore(0) ::
            conditional(test, "pos" | Vector3.codec_vel)
        })
    ).xmap[ShiftState] (
      {
        case a :: b :: false :: _ :: None :: HNil =>
          ShiftState(a, b, None)
        case a :: b :: true :: _ :: Some(vel) :: HNil =>
          ShiftState(a, b, Some(vel))
      },
      {
        case ShiftState(a, b, None) =>
          a :: b :: false :: () :: None :: HNil
        case ShiftState(a, b, Some(vel)) =>
          a :: b :: true :: () :: Some(vel) :: HNil
      }
    ).as[ShiftState]
}

object PlayerStateShiftMessage extends Marshallable[PlayerStateShiftMessage] {
  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, removing the optional condition of `state`.
    * @param unk1 na
    * @param state the behaviors to influence the character
    * @param unk2 na
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(unk1 : Int, state : ShiftState, unk2 : Boolean) : PlayerStateShiftMessage =
    PlayerStateShiftMessage(unk1, Some(state), unk2)

  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, assuming the parameter `state` is not defined.
    * @param unk1 na
    * @param unk2 na
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(unk1 : Int, unk2 : Boolean) : PlayerStateShiftMessage =
    PlayerStateShiftMessage(unk1, None, unk2)

  implicit val codec : Codec[PlayerStateShiftMessage] = (
    bool >>:~ { test =>
      ("unk1" | uintL(3)) ::
        conditional(test, "state" | ShiftState.codec) ::
        ("unk2" | bool)
    }).xmap[PlayerStateShiftMessage] (
      {
        case false :: a :: None :: b :: HNil =>
          PlayerStateShiftMessage(a, None, b)
        case true :: a :: Some(pos) :: b :: HNil =>
          PlayerStateShiftMessage(a, Some(pos), b)
      },
      {
        case PlayerStateShiftMessage(a, None, b) =>
          false :: a :: None :: b :: HNil
        case PlayerStateShiftMessage(a, Some(pos), b) =>
          true :: a :: Some(pos) :: b :: HNil
      }
    ).as[PlayerStateShiftMessage]
}
