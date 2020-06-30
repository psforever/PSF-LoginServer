// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * An `Enumeration` of the available implants.<br>
  * <br>
  * Implant:<br>
  * `
  * 0 - Regeneration (advanced_regen)<br>
  * 1 - Enhanced Targeting (targeting)<br>
  * 2 - Audio Amplifier (audio_amplifier)<br>
  * 3 - Darklight Vision (darklight_vision)<br>
  * 4 - Melee Booster (melee_booster)<br>
  * 5 - Personal Shield (personal_shield)<br>
  * 6 - Range Magnifier (range_magnifier)<br>
  * 7 - Second Wind `(na)`<br>
  * 8 - Sensor Shield (silent_run)<br>
  * 9 - Surge (surge)
  * `
  */
object ImplantType extends Enumeration {
  type Type = Value

  val AdvancedRegen, Targeting, AudioAmplifier, DarklightVision, MeleeBooster, PersonalShield, RangeMagnifier,
      SecondWind, //technically
  SilentRun, Surge = Value

  val None = Value(15) //TODO unconfirmed

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}
