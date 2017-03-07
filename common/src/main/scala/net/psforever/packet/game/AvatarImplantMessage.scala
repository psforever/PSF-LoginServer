// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * An `Enumeration` of the available implants.
  */
object ImplantType extends Enumeration {
  type Type = Value
  val AdvancedRegen,
      Targeting,
      AudioAmplifier,
      DarklightVision,
      MeleeBooster,
      PersonalShield,
      RangeMagnifier,
      Unknown7,
      SilentRun,
      Surge = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * Change the state of the implant.<br>
  * Write better comments.
  * <br>
  * Implant:<br>
  * `
  * 00 - Regeneration (advanced_regen)<br>
  * 01 - Enhanced Targeting (targeting)<br>
  * 02 - Audio Amplifier (audio_amplifier)<br>
  * 03 - Darklight Vision (darklight_vision)<br>
  * 04 - Melee Booster (melee_booster)<br>
  * 05 - Personal Shield (personal_shield)<br>
  * 06 - Range Magnifier (range_magnifier)<br>
  * 07 - `None`<br>
  * 08 - Sensor Shield (silent_run)<br>
  * 09 - Surge (surge)<br>
  * `
  * <br>
  * Exploration<br>
  * Where is Second Wind (second_wind)?
  * @param player_guid the player
  * @param unk1 na
  * @param unk2 na
  * @param implant the implant
  */
final case class AvatarImplantMessage(player_guid : PlanetSideGUID,
                                      unk1 : Int,
                                      unk2 : Int,
                                      implant : ImplantType.Value)
  extends PlanetSideGamePacket {
  type Packet = AvatarImplantMessage
  def opcode = GamePacketOpcode.AvatarImplantMessage
  def encode = AvatarImplantMessage.encode(this)
}

object AvatarImplantMessage extends Marshallable[AvatarImplantMessage] {
  implicit val codec : Codec[AvatarImplantMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(3)) ::
      ("unk2" | uint2L) ::
      ("implant" | ImplantType.codec)
    ).as[AvatarImplantMessage]
}
