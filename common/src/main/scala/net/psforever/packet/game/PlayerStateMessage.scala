// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param guid the avatar's guid
  * @param pos the position of the avatar in the world environment (in three coordinates)
  * @param facingYaw the angle with respect to the horizon towards which the avatar is looking;
  *                  every `0x01` is 5.625 degrees counter clockwise from North;
  *                  every `0x10` is 90-degrees;
  *                  it wraps to North every `0x40`
  * @param facingPitch the angle with respect to the sky and the ground towards which the avatar is looking;
  *                    every '0x01' is about 5.625 degrees;
  *                    `0x00` to `0x10` are downwards-facing angles, with `0x00` as forwards-facing;
  *                    nothing from `0x11` to `0x29`;
  *                    `0x30` to `0x40` are upwards-facing angles, with `0x30` starting at full-up;
  *                    starting at `0x40` == `0x00` this system repeats
  * @param facingYawUpper the angle of the avatar's upper body with respect to its forward-facing direction;
  *                       `0x00` to `0x10` are the avatar turning to its left, with `0x00` being forward-facing;
  *                       nothing from `0x11` to `0x29`;
  *                       `0x30` to `0x40` are the avatar turning to its right, with `0x40` being forward-facing;
  *                       starting at `0x40` == `0x00` this system repeats
  * @param unk4 na
  * @param more activate parsing for the following four fields
  * @param unk5 na
  * @param isCrouching avatar is crouching;
  *                  must remain flagged for crouch to maintain animation;
  *                  turn off to stand up
  * @param isJumping avatar is jumping;
  *                  must remain flagged for jump to maintain animation;
  *                  turn off when landed
  * @param unk8 na
  */
final case class PlayerStateMessage(guid : PlanetSideGUID,
                                    pos : Vector3,
                                    facingYaw : Int,
                                    facingPitch : Int,
                                    facingYawUpper : Int,
                                    unk4 : Int,
                                    more : Boolean,
                                    unk5 : Boolean = false,
                                    isCrouching : Boolean = false,
                                    isJumping : Boolean = false,
                                    unk8 : Boolean = false)
  extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.PlayerStateMessage
  def encode = PlayerStateMessage.encode(this)
}

object PlayerStateMessage extends Marshallable[PlayerStateMessage] {
  type fourBoolPattern = Boolean :: Boolean :: Boolean :: Boolean :: HNil

  val booleanCodec : Codec[fourBoolPattern] = (
    bool ::
      bool ::
      bool ::
      bool
    ).as[fourBoolPattern]

  val defaultCodec : Codec[fourBoolPattern] = ignore(0).xmap[fourBoolPattern] (
    {
      case _ =>
        false :: false :: false :: false :: HNil
    },
    {
      case _ =>
        ()
    }
  ).as[fourBoolPattern]

  implicit val codec : Codec[PlayerStateMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("facingYaw" | uint8L) ::
      ("facingPitch" | uint8L) ::
      ("facingYawUpper" | uint8L) ::
      ("unk4" | uintL(10)) ::
      ("more" | bool >>:~ { test =>
        ignore(0) ::
          newcodecs.binary_choice(test, booleanCodec, defaultCodec)
      })
    ).as[PlayerStateMessage]
}
