// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param pos the position to move the character to in the world environment (in three coordinates)
  * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
  * @param vel the velocity to apply to to the character at the given position (in three coordinates)
  */
final case class PlayerState(pos : Vector3,
                             viewYawLim : Int,
                             vel : Option[Vector3])

/**
  * Force the client's character to adhere to the influence of specific external stimulus.
  * @param unk1 na
  * @param state the state to influence the character with respect to his environment in the current zone

  */
final case class PlayerStateShiftMessage(unk1 : Int,
                                         state : Option[PlayerState],
                                         unk2 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.PlayerStateShiftMessage
  def encode = PlayerStateShiftMessage.encode(this)
}

object PlayerState extends Marshallable[PlayerState] {
  /**
    * An abbreviated constructor for creating `PlayerState`, assuming velocity is not applied.
    * @param pos the position of the character in the world environment (in three coordinates)
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @param vel the velocity to apply to to the character at the given position (in three coordinates)
    * @return a `PlayerState` object
    */
  def apply(pos : Vector3, viewYawLim : Int, vel : Vector3) : PlayerState =
    PlayerState(pos, viewYawLim, Some(vel))

  /**
    * An abbreviated constructor for creating `PlayerState`, removing the optional condition of all parameters.
    * @param pos the position of the character in the world environment (in three coordinates)
    * @param viewYawLim an angle with respect to the horizon towards which the avatar is looking (to some respect)
    * @return a `PlayerState` object
    */
  def apply(pos : Vector3, viewYawLim : Int) : PlayerState =
    PlayerState(pos, viewYawLim, None)

  implicit val codec : Codec[PlayerState] = (
      ("pos" | Vector3.codec_pos) ::
      ("unk2" | uint8L) ::
      (bool >>:~ { test =>
        ignore(0) ::
          conditional(test, "pos" | Vector3.codec_vel)
      })
    ).xmap[PlayerState] (
      {
        case a :: b :: false :: _ :: None :: HNil =>
          PlayerState(a, b, None)
        case a :: b :: true :: _ :: Some(vel) :: HNil =>
          PlayerState(a, b, Some(vel))
      },
      {
        case PlayerState(a, b, None) =>
          a :: b :: false :: () :: None :: HNil
        case PlayerState(a, b, Some(vel)) =>
          a :: b :: true :: () :: Some(vel) :: HNil
      }
    ).as[PlayerState]
}

object PlayerStateShiftMessage extends Marshallable[PlayerStateShiftMessage] {
  private type pattern = Int :: Option[PlayerState] :: Boolean :: HNil

  /**
    * An abbreviated constructor for creating `PlayerStateShiftMessage`, removing the optional condition of `state`.
    * @param unk1 na
    * @param state the state to which to influence the character with respect to his environment in the current zone
    * @param unk2 na
    * @return a `PlayerStateShiftMessage` packet
    */
  def apply(unk1 : Int, state : PlayerState, unk2 : Boolean) : PlayerStateShiftMessage =
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
    bool >>:~ { test1 =>
      ("unk1" | uintL(3)) ::
        conditional(test1, "pos" | PlayerState.codec) ::
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
