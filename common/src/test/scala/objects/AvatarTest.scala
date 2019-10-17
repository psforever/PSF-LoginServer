// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects._
import net.psforever.objects.loadouts._
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.types.{CharacterGender, CharacterVoice, ImplantType, PlanetSideEmpire}
import org.specs2.mutable._

class AvatarTest extends Specification {
  def CreatePlayer() : (Player, Avatar) = {
    val avatar = Avatar("TestCharacter", PlanetSideEmpire.VS, CharacterGender.Female, 41, CharacterVoice.Voice1)
    val
    player = Player(avatar)
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
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.name mustEqual "Chord"
    av.faction mustEqual PlanetSideEmpire.TR
    av.sex mustEqual CharacterGender.Male
    av.head mustEqual 0
    av.voice mustEqual CharacterVoice.Voice5
    av.BEP mustEqual 0
    av.CEP mustEqual 0
    av.Certifications mustEqual Set.empty
    av.Definition.ObjectId mustEqual 121
  }

  "can maintain cumulative battle experience point values" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.BEP mustEqual 0
    av.BEP = 100
    av.BEP mustEqual 100
    av.BEP = 700
    av.BEP mustEqual 700
  }

  "can maintain battle experience point values up to a maximum (Long.MaxValue)" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.BEP mustEqual 0
    av.BEP = 4294967295L
    av.BEP mustEqual 4294967295L
  }

  "can not maintain battle experience point values below zero" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.BEP mustEqual 0
    av.BEP = -1
    av.BEP mustEqual 0
    av.BEP = 100
    av.BEP mustEqual 100
    av.BEP = -1
    av.BEP mustEqual 0
  }

  "can maintain cumulative command experience point values" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.CEP mustEqual 0
    av.CEP = 100
    av.CEP mustEqual 100
    av.CEP = 700
    av.CEP mustEqual 700
  }

  "can maintain command experience point values up to a maximum (Long.MaxValue)" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.CEP mustEqual 0
    av.CEP = 4294967295L
    av.CEP mustEqual 4294967295L
  }

  "can not maintain command experience point values below zero" in {
    val av = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    av.CEP mustEqual 0
    av.CEP = -1
    av.CEP mustEqual 0
    av.CEP = 100
    av.CEP mustEqual 100
    av.CEP = -1
    av.CEP mustEqual 0
  }

  "can tell the difference between avatars" in {
    (Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)) mustEqual true

    (Avatar("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)) mustEqual false

    (Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Voice5)) mustEqual false

    (Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Female, 0, CharacterVoice.Voice5)) mustEqual false

    (Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 1, CharacterVoice.Voice5)) mustEqual false

    (Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5) ==
      Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice4)) mustEqual false
  }

  //refer to ImplantTest.scala for more tests
  "maximum of three implant slots" in {
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants.length mustEqual 3
    obj.Implants(0).Unlocked mustEqual false
    obj.Implants(0).Initialized mustEqual false
    obj.Implants(0).Active mustEqual false
    obj.Implants(0).Implant mustEqual ImplantType.None
    obj.Implant(0) mustEqual ImplantType.None
    obj.Implants(0).Installed.isEmpty mustEqual true
    obj.Implants(1).Unlocked mustEqual false
    obj.Implants(1).Initialized mustEqual false
    obj.Implants(1).Active mustEqual false
    obj.Implants(1).Implant mustEqual ImplantType.None
    obj.Implant(1) mustEqual ImplantType.None
    obj.Implants(1).Installed.isEmpty mustEqual true
    obj.Implants(2).Unlocked mustEqual false
    obj.Implants(2).Initialized mustEqual false
    obj.Implants(2).Active mustEqual false
    obj.Implants(2).Implant mustEqual ImplantType.None
    obj.Implant(2) mustEqual ImplantType.None
    obj.Implants(2).Installed.isEmpty mustEqual true

    obj.Implant(3) mustEqual ImplantType.None //invalid slots beyond the third always reports as ImplantType.None
  }

  "can install an implant" in {
    val testplant : ImplantDefinition = ImplantDefinition(1)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.InstallImplant(testplant).contains(0) mustEqual true
    obj.Implants.find({p => p.Implant == ImplantType(1)}) match { //find the installed implant
      case Some(slot) =>
        slot.Installed.contains(testplant) mustEqual true
      case _ =>
        ko
    }
    ok
  }

  "can install implants in sequential slots" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true

    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
  }

  "can not install the same type of implant twice" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(1)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true

    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).isEmpty mustEqual true
  }

  "can not install more implants than slots available (two unlocked)" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val testplant3 : ImplantDefinition = ImplantDefinition(3)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true

    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
    obj.InstallImplant(testplant3).isEmpty mustEqual true
  }

  "can not install more implants than slots available (four implants)" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val testplant3 : ImplantDefinition = ImplantDefinition(3)
    val testplant4 : ImplantDefinition = ImplantDefinition(4)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true
    obj.Implants(2).Unlocked = true

    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
    obj.InstallImplant(testplant3).contains(2) mustEqual true
    obj.InstallImplant(testplant4).isEmpty mustEqual true
  }

  "can uninstall an implant" in {
    val testplant : ImplantDefinition = ImplantDefinition(1)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.InstallImplant(testplant).contains(0) mustEqual true
    obj.Implants(0).Installed.contains(testplant) mustEqual true

    obj.UninstallImplant(testplant.Type).contains(0) mustEqual true
    obj.Implants(0).Installed.isEmpty mustEqual true
  }

  "can uninstall just a specific implant" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val testplant3 : ImplantDefinition = ImplantDefinition(3)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true
    obj.Implants(2).Unlocked = true
    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
    obj.InstallImplant(testplant3).contains(2) mustEqual true

    obj.Implant(0) mustEqual testplant1.Type
    obj.Implant(1) mustEqual testplant2.Type
    obj.Implant(2) mustEqual testplant3.Type
    obj.UninstallImplant(testplant2.Type).contains(1) mustEqual true
    obj.Implant(0) mustEqual testplant1.Type
    obj.Implant(1) mustEqual ImplantType.None
    obj.Implant(2) mustEqual testplant3.Type
  }

  "can install implants to any available slot" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val testplant3 : ImplantDefinition = ImplantDefinition(3)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true
    obj.Implants(2).Unlocked = true
    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
    obj.InstallImplant(testplant3).contains(2) mustEqual true
    obj.UninstallImplant(testplant2.Type).contains(1) mustEqual true
    obj.Implant(0) mustEqual testplant1.Type
    obj.Implant(1) mustEqual ImplantType.None
    obj.Implant(2) mustEqual testplant3.Type

    val testplant4 : ImplantDefinition = ImplantDefinition(4)
    obj.InstallImplant(testplant4).contains(1) mustEqual true
    obj.Implant(0) mustEqual testplant1.Type
    obj.Implant(1) mustEqual testplant4.Type
    obj.Implant(2) mustEqual testplant3.Type
  }

  "can reset implants to uninitialized state" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(2)
    val obj = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true
    obj.InstallImplant(testplant1).contains(0) mustEqual true
    obj.InstallImplant(testplant2).contains(1) mustEqual true
    obj.Implants(0).Initialized = true
    obj.Implants(0).Active = true
    obj.Implants(1).Initialized = true

    obj.Implants(0).Initialized mustEqual true
    obj.Implants(0).Active mustEqual true
    obj.Implants(1).Initialized mustEqual true
    obj.ResetAllImplants()
    obj.Implants(0).Initialized mustEqual false
    obj.Implants(0).Active mustEqual false
    obj.Implants(1).Initialized mustEqual false
  }

  "does not have any loadout specifications by default" in {
    val (_, avatar) = CreatePlayer()
    (0 to 9).foreach { avatar.EquipmentLoadouts.LoadLoadout(_).isEmpty mustEqual true }
    ok
  }

  "save player's current inventory as a loadout" in {
    val (obj, avatar) = CreatePlayer()
    obj.Slot(0).Equipment.get.asInstanceOf[Tool].Magazine = 1 //non-standard but legal
    obj.Slot(2).Equipment.get.asInstanceOf[Tool].AmmoSlot.Magazine = 100 //non-standard (and out of range, real=25)
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 0)

    avatar.EquipmentLoadouts.LoadLoadout(0) match {
      case Some(items : InfantryLoadout) =>
        items.label mustEqual "test"
        items.exosuit mustEqual obj.ExoSuit
        items.subtype mustEqual 0

        items.visible_slots.length mustEqual 3
        val holsters = items.visible_slots.sortBy(_.index)
        holsters.head.index mustEqual 0
        holsters.head.item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual beamer
        holsters.head.item.asInstanceOf[Loadout.ShorthandTool].ammo.head.ammo.capacity mustEqual 1 //we changed this
        holsters(1).index mustEqual 2
        holsters(1).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual suppressor
        holsters(1).item.asInstanceOf[Loadout.ShorthandTool].ammo.head.ammo.capacity mustEqual 100 //we changed this
        holsters(2).index mustEqual 4
        holsters(2).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual forceblade

        items.inventory.length mustEqual 6
        val inventory = items.inventory.sortBy(_.index)
        inventory.head.index mustEqual 6
        inventory.head.item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
        inventory(1).index mustEqual 9
        inventory(1).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
        inventory(2).index mustEqual 12
        inventory(2).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
        inventory(3).index mustEqual 33
        inventory(3).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm_AP
        inventory(4).index mustEqual 36
        inventory(4).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual energy_cell
        inventory(5).index mustEqual 39
        inventory(5).item.asInstanceOf[Loadout.ShorthandSimpleItem].definition mustEqual remote_electronics_kit
      case _ =>
        ko
    }
  }

  "save player's current inventory as a loadout, only found in the called-out slot number" in {
    val (obj, avatar) = CreatePlayer()
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 0)

    avatar.EquipmentLoadouts.LoadLoadout(1).isDefined mustEqual false
    avatar.EquipmentLoadouts.LoadLoadout(0).isDefined mustEqual true
  }

  "try to save player's current inventory as a loadout, but will not save to an invalid slot" in {
    val (obj, avatar) = CreatePlayer()
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 50)

    avatar.EquipmentLoadouts.LoadLoadout(50).isEmpty mustEqual true
  }

  "save player's current inventory as a loadout, without inventory contents" in {
    val (obj, avatar) = CreatePlayer()
    obj.Inventory.Clear()
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 0)

    avatar.EquipmentLoadouts.LoadLoadout(0) match {
      case Some(items : InfantryLoadout) =>
        items.label mustEqual "test"
        items.exosuit mustEqual obj.ExoSuit
        items.subtype mustEqual 0
        items.visible_slots.length mustEqual 3
        items.inventory.length mustEqual 0 //empty
      case _ =>
        ko
    }
  }

  "save player's current inventory as a loadout, without visible slot contents" in {
    val (obj, avatar) = CreatePlayer()
    obj.Slot(0).Equipment = None
    obj.Slot(2).Equipment = None
    obj.Slot(4).Equipment = None
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 0)

    avatar.EquipmentLoadouts.LoadLoadout(0) match {
      case Some(items : InfantryLoadout) =>
        items.label mustEqual "test"
        items.exosuit mustEqual obj.ExoSuit
        items.subtype mustEqual 0
        items.visible_slots.length mustEqual 0 //empty
        items.inventory.length mustEqual 6
      case _ =>
        ko
    }
  }

  "save, load, delete; rapidly" in {
    val (obj, avatar) = CreatePlayer()
    avatar.EquipmentLoadouts.SaveLoadout(obj, "test", 0)

    avatar.EquipmentLoadouts.LoadLoadout(0).isDefined mustEqual true
    avatar.EquipmentLoadouts.DeleteLoadout(0)
    avatar.EquipmentLoadouts.LoadLoadout(0).isEmpty mustEqual true
  }

  "the fifth slot is the locker wrapped in an EquipmentSlot" in {
    val (_, avatar) = CreatePlayer()
    avatar.FifthSlot.Equipment.contains(avatar.Locker)
  }

  "toString" in {
    Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5).toString mustEqual "TR Chord"
  }
}
