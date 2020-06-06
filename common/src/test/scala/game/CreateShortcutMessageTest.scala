// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class CreateShortcutMessageTest extends Specification {
  val stringMedkit = hex"28 7210 01 00 90 C0 6D65646B6974 80 80"
  val stringMacro = hex"28 4C05 08 00 B1 C0 73686F72746375745F6D6163726F 83 4E00 5400 5500 9B 2F00 7000 6C00 6100 7400 6F00 6F00 6E00 2000 4900 6E00 6300 6F00 6D00 6900 6E00 6700 2000 4E00 5400 5500 2000 7300 7000 6100 6D00 2100"
  val stringRemove = hex"28 4C05 01 00 00"

  "decode (medkit)" in {
    PacketCoding.DecodePacket(stringMedkit).require match {
      case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
        player_guid mustEqual PlanetSideGUID(4210)
        slot mustEqual 1
        unk mustEqual 0
        addShortcut mustEqual true
        shortcut.isDefined mustEqual true
        shortcut.get.purpose mustEqual 0
        shortcut.get.tile mustEqual "medkit"
        shortcut.get.effect1 mustEqual ""
        shortcut.get.effect2 mustEqual ""
      case _ =>
        ko
    }
  }

  "decode (macro)" in {
    PacketCoding.DecodePacket(stringMacro).require match {
      case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
        player_guid mustEqual PlanetSideGUID(1356)
        slot mustEqual 8
        unk mustEqual 0
        addShortcut mustEqual true
        shortcut.isDefined mustEqual true
        shortcut.get.purpose mustEqual 1
        shortcut.get.tile mustEqual "shortcut_macro"
        shortcut.get.effect1 mustEqual "NTU"
        shortcut.get.effect2 mustEqual "/platoon Incoming NTU spam!"
      case _ =>
        ko
    }
  }

  "decode (remove)" in {
    PacketCoding.DecodePacket(stringRemove).require match {
      case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
        player_guid mustEqual PlanetSideGUID(1356)
        slot mustEqual 1
        unk mustEqual 0
        addShortcut mustEqual false
        shortcut.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (medkit)" in {
    val msg = CreateShortcutMessage(PlanetSideGUID(4210), 1, 0, true, Some(Shortcut(0, "medkit")))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringMedkit
  }

  "encode (macro)" in {
    val msg = CreateShortcutMessage(PlanetSideGUID(1356), 8, 0, true, Some(Shortcut(1, "shortcut_macro", "NTU", "/platoon Incoming NTU spam!")))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringMacro
  }

  "encode (remove)" in {
    val msg = CreateShortcutMessage(PlanetSideGUID(1356), 1, 0, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringRemove
  }

  "macro" in {
    val MACRO : Some[Shortcut] = Shortcut.MACRO("NTU", "/platoon Incoming NTU spam!")
    MACRO.get.purpose mustEqual 1
    MACRO.get.tile mustEqual "shortcut_macro"
    MACRO.get.effect1 mustEqual "NTU"
    MACRO.get.effect2 mustEqual "/platoon Incoming NTU spam!"
  }

  "presets" in {
    Shortcut.AudioAmplifier.get.purpose mustEqual 2
    Shortcut.AudioAmplifier.get.tile mustEqual "audio_amplifier"
    Shortcut.DartklightVision.get.purpose mustEqual 2
    Shortcut.DartklightVision.get.tile mustEqual "darklight_vision"
    Shortcut.EnhancedTargeting.get.purpose mustEqual 2
    Shortcut.EnhancedTargeting.get.tile mustEqual "targeting"
    Shortcut.Medkit.get.purpose mustEqual 0
    Shortcut.Medkit.get.tile mustEqual "medkit"
    Shortcut.MeleeBooster.get.purpose mustEqual 2
    Shortcut.MeleeBooster.get.tile mustEqual "melee_booster"
    Shortcut.PersonalShield.get.purpose mustEqual 2
    Shortcut.PersonalShield.get.tile mustEqual "personal_shield"
    Shortcut.RangeMagnifier.get.purpose mustEqual 2
    Shortcut.RangeMagnifier.get.tile mustEqual "range_magnifier"
    Shortcut.Regeneration.get.purpose mustEqual 2
    Shortcut.Regeneration.get.tile mustEqual "advanced_regen"
    Shortcut.SecondWind.get.purpose mustEqual 2
    Shortcut.SecondWind.get.tile mustEqual "second_wind"
    Shortcut.SensorShield.get.purpose mustEqual 2
    Shortcut.SensorShield.get.tile mustEqual "silent_run"
    Shortcut.Surge.get.purpose mustEqual 2
    Shortcut.Surge.get.tile mustEqual "surge"
  }
}
