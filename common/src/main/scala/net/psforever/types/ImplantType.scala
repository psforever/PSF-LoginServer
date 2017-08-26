// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * An `Enumeration` of the available implants.<br>
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
  * 07 - Second Wind `(na)`<br>
  * 08 - Sensor Shield (silent_run)<br>
  * 09 - Surge (surge)<br>
  * `
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
  SecondWind, //technically
  SilentRun,
  Surge = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}
