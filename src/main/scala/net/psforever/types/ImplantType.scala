// Copyright (c) 2017 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.PacketHelpers
import net.psforever.packet.game.Shortcut
import net.psforever.packet.game.objectcreate.ImplantEffects
import scodec.Codec
import scodec.codecs._

sealed abstract class ImplantType(
    val value: Int,
    val shortcut: Shortcut,
    val effect: Option[ImplantEffects.Value] = None,
    /** Some armor types are excluded from using some implants */
    val disabledFor: Set[ExoSuitType.Value] = Set()
) extends IntEnumEntry

case object ImplantType extends IntEnum[ImplantType] {
  case object AdvancedRegen
      extends ImplantType(
        value = 0,
        shortcut = Shortcut.Implant("advanced_regen"),
        effect = Some(ImplantEffects.RegenEffects)
      )

  case object Targeting extends ImplantType(value = 1, shortcut = Shortcut.Implant("targeting"))

  case object AudioAmplifier extends ImplantType(value = 2, shortcut = Shortcut.Implant("audio_amplifier"))

  case object DarklightVision
      extends ImplantType(
        value = 3,
        shortcut = Shortcut.Implant("darklight_vision"),
        effect = Some(ImplantEffects.DarklightEffects)
      )

  case object MeleeBooster extends ImplantType(value = 4, shortcut = Shortcut.Implant("melee_booster"))

  case object PersonalShield
      extends ImplantType(
        value = 5,
        shortcut = Shortcut.Implant("personal_shield"),
        disabledFor = Set(ExoSuitType.Infiltration),
        effect = Some(ImplantEffects.PersonalShieldEffects)
      )

  case object RangeMagnifier extends ImplantType(value = 6, shortcut = Shortcut.Implant("range_magnifier"))

  case object SecondWind extends ImplantType(value = 7, shortcut = Shortcut.Implant("second_wind"))

  case object SilentRun extends ImplantType(value = 8, shortcut = Shortcut.Implant("silent_run"))

  case object Surge extends ImplantType(
    value = 9,
    shortcut = Shortcut.Implant("surge"),
    disabledFor = Set(ExoSuitType.MAX),
    effect = Some(ImplantEffects.SurgeEffects)
  )

  case object None extends ImplantType(
    value = 15,
    shortcut = Shortcut.Macro(acronym="ERR", msg=""),
    disabledFor = ExoSuitType.values
  )

  def values: IndexedSeq[ImplantType] = findValues

  final val names: Seq[String] = Seq(
    "advanced_regen", "targeting", "audio_amplifier",
    "darklight_vision", "melee_booster", "personal_shield", "range_magnifier",
    "second_wind", "silent_run", "surge"
  )

  implicit val codec: Codec[ImplantType] = PacketHelpers.createIntEnumCodec(this, uint4L)
}
