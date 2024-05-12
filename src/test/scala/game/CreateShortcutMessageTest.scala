// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{ImplantType, PlanetSideGUID}
import scodec.bits._

class CreateShortcutMessageTest extends Specification {
  val stringMedkit = hex"28 7210 01 00 90 C0 6D65646B6974 80 80"
  val stringMacro =
    hex"28 4C05 08 00 B1 C0 73686F72746375745F6D6163726F 83 4E00 5400 5500 9B 2F00 7000 6C00 6100 7400 6F00 6F00 6E00 2000 4900 6E00 6300 6F00 6D00 6900 6E00 6700 2000 4E00 5400 5500 2000 7300 7000 6100 6D00 2100"
  val stringRemove = hex"28 4C05 01 00 00"

  "decode (medkit)" in {
    PacketCoding.decodePacket(stringMedkit).require match {
      case CreateShortcutMessage(player_guid, slot, shortcut) =>
        player_guid mustEqual PlanetSideGUID(4210)
        slot mustEqual 1
        shortcut match {
          case Some(Shortcut.Medkit) => ok
          case _ => ko
        }
      case _ =>
        ko
    }
  }

  "decode (macro)" in {
    PacketCoding.decodePacket(stringMacro).require match {
      case CreateShortcutMessage(player_guid, slot, shortcut) =>
        player_guid mustEqual PlanetSideGUID(1356)
        slot mustEqual 8
        shortcut match {
          case Some(Shortcut.Macro("NTU", "/platoon Incoming NTU spam!")) => ok
          case _ => ko
        }
      case _ =>
        ko
    }
  }

  "decode (remove)" in {
    PacketCoding.decodePacket(stringRemove).require match {
      case CreateShortcutMessage(player_guid, slot, shortcut) =>
        player_guid mustEqual PlanetSideGUID(1356)
        slot mustEqual 1
        shortcut.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (medkit)" in {
    val msg = CreateShortcutMessage(PlanetSideGUID(4210), 1, Some(Shortcut.Medkit))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual stringMedkit
  }

  "encode (macro)" in {
    val msg = CreateShortcutMessage(
      PlanetSideGUID(1356),
      8,
      Some(Shortcut.Macro("NTU", "/platoon Incoming NTU spam!"))
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual stringMacro
  }

  "encode (remove)" in {
    val msg = CreateShortcutMessage(PlanetSideGUID(1356), 1, None)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual stringRemove
  }

  "macro" in {
    val MACRO: Shortcut.Macro = Shortcut.Macro("NTU", "/platoon Incoming NTU spam!")
    MACRO.acronym mustEqual "NTU"
    MACRO.msg mustEqual "/platoon Incoming NTU spam!"
  }

  "presets" in {
    ImplantType.AudioAmplifier.shortcut.code mustEqual 2
    ImplantType.AudioAmplifier.shortcut.tile mustEqual "audio_amplifier"
    ImplantType.DarklightVision.shortcut.code mustEqual 2
    ImplantType.DarklightVision.shortcut.tile mustEqual "darklight_vision"
    ImplantType.Targeting.shortcut.code mustEqual 2
    ImplantType.Targeting.shortcut.tile mustEqual "targeting"
    Shortcut.Medkit.code mustEqual 0
    Shortcut.Medkit.tile mustEqual "medkit"
    ImplantType.MeleeBooster.shortcut.code mustEqual 2
    ImplantType.MeleeBooster.shortcut.tile mustEqual "melee_booster"
    ImplantType.PersonalShield.shortcut.code mustEqual 2
    ImplantType.PersonalShield.shortcut.tile mustEqual "personal_shield"
    ImplantType.RangeMagnifier.shortcut.code mustEqual 2
    ImplantType.RangeMagnifier.shortcut.tile mustEqual "range_magnifier"
    ImplantType.AdvancedRegen.shortcut.code mustEqual 2
    ImplantType.AdvancedRegen.shortcut.tile mustEqual "advanced_regen"
    ImplantType.SecondWind.shortcut.code mustEqual 2
    ImplantType.SecondWind.shortcut.tile mustEqual "second_wind"
    ImplantType.SilentRun.shortcut.code mustEqual 2
    ImplantType.SilentRun.shortcut.tile mustEqual "silent_run"
    ImplantType.Surge.shortcut.code mustEqual 2
    ImplantType.Surge.shortcut.tile mustEqual "surge"
  }
}
