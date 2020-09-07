// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects._
import net.psforever.objects.avatar.{Avatar, BattleRank, Implant}
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.locker.LockerEquipment
import net.psforever.types.{CharacterGender, CharacterVoice, ImplantType, PlanetSideEmpire}
import org.specs2.mutable._

class AvatarTest extends Specification {
  def CreatePlayer(): (Player, Avatar) = {
    val avatar = Avatar(0, "TestCharacter", PlanetSideEmpire.VS, CharacterGender.Female, 41, CharacterVoice.Voice1)
    val player = Player(avatar)
    player.Slot(0).Equipment = Tool(beamer)
    player.Slot(2).Equipment = Tool(suppressor)
    player.Slot(4).Equipment = Tool(forceblade)
    player.Slot(6).Equipment = AmmoBox(bullet_9mm)
    player.Slot(9).Equipment = AmmoBox(bullet_9mm)
    player.Slot(12).Equipment = AmmoBox(bullet_9mm)
    player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
    player.Slot(36).Equipment = AmmoBox(energy_cell)
    player.Slot(39).Equipment = SimpleItem(remote_electronics_kit)
    (player, avatar)
  }

  "construct" in {
    val av = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.name mustEqual "Chord"
    av.faction mustEqual PlanetSideEmpire.TR
    av.sex mustEqual CharacterGender.Male
    av.head mustEqual 0
    av.voice mustEqual CharacterVoice.Voice5
    av.bep mustEqual 0
    av.cep mustEqual 0
    av.certifications mustEqual Set.empty
    av.definition.ObjectId mustEqual 121
  }

  "can not maintain experience point values below zero" in {
    val av = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.bep mustEqual 0
    av.copy(bep = -1) must throwA[AssertionError]
    av.copy(cep = -1) must throwA[AssertionError]
  }

  //refer to ImplantTest.scala for more tests
  "maximum of three implant slots" in {
    val obj = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.implants.length mustEqual 3
    obj.implants(0) must beNone
    obj.implants(1) must beNone
    obj.implants(2) must beNone
    obj.implants.lift(3) must beNone
  }

  "can install an implant" in {
    val testplant = Implant(new ImplantDefinition(ImplantType.AdvancedRegen))
    var obj = Avatar(
      0,
      "Chord",
      PlanetSideEmpire.TR,
      CharacterGender.Male,
      0,
      CharacterVoice.Voice5,
      bep = BattleRank.BR6.experience
    )
    obj.implants.nonEmpty must beTrue
    obj.implants.length mustEqual 3
    obj = obj.copy(implants = obj.implants.updated(0, Some(testplant)))
    obj.implants.flatten.find(_.definition.implantType == ImplantType.AdvancedRegen) match {
      case Some(slot) =>
        slot.definition mustEqual testplant.definition
      case _ =>
        ko
    }
    ok
  }

  "can not install the same type of implant twice" in {
    val testplant1 = Implant(new ImplantDefinition(ImplantType.AdvancedRegen))
    val testplant2 = Implant(new ImplantDefinition(ImplantType.AdvancedRegen))
    val obj        = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.copy(implants = obj.implants.updated(0, Some(testplant1)).updated(1, Some(testplant2))) must throwA[
      AssertionError
    ]
  }

  "can not install more implants than slots available" in {
    val testplant1 = Implant(new ImplantDefinition(ImplantType.AdvancedRegen))
    val testplant2 = Implant(new ImplantDefinition(ImplantType.Surge))
    val testplant3 = Implant(new ImplantDefinition(ImplantType.DarklightVision))
    val obj        = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.copy(
      bep = BattleRank.BR12.value,
      implants = Seq(Some(testplant1), Some(testplant2), Some(testplant3))
    ) must throwA[
      AssertionError
    ]
  }

  "the fifth slot is the locker wrapped in an EquipmentSlot" in {
    val (_, avatar) = CreatePlayer()
    avatar.fifthSlot().Equipment match {
      case Some(slot: LockerEquipment) => slot.Inventory mustEqual avatar.locker.Inventory
      case _                           => ko
    }
  }

}
