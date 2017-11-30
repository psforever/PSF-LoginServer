// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{Player, SimpleItem}
import net.psforever.objects.definition.{ImplantDefinition, SimpleItemDefinition}
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.types.{CharacterGender, ExoSuitType, ImplantType, PlanetSideEmpire}
import org.specs2.mutable._

class PlayerTest extends Specification {
  "construct" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.isAlive mustEqual false
  }

  "(re)spawn" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.isAlive mustEqual false
    obj.Health mustEqual 0
    obj.Stamina mustEqual 0
    obj.Armor mustEqual 0
    obj.Spawn
    obj.isAlive mustEqual true
    obj.Health mustEqual obj.MaxHealth
    obj.Stamina mustEqual obj.MaxStamina
    obj.Armor mustEqual obj.MaxArmor
  }

  "init (Standard Exo-Suit)" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.ExoSuit mustEqual ExoSuitType.Standard
    obj.Slot(0).Size mustEqual EquipmentSize.Pistol
    obj.Slot(1).Size mustEqual EquipmentSize.Blocked
    obj.Slot(2).Size mustEqual EquipmentSize.Rifle
    obj.Slot(3).Size mustEqual EquipmentSize.Blocked
    obj.Slot(4).Size mustEqual EquipmentSize.Melee
    obj.Inventory.Width mustEqual 9
    obj.Inventory.Height mustEqual 6
    obj.Inventory.Offset mustEqual 6
  }

  "die" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Spawn
    obj.Armor = 35 //50 -> 35
    obj.isAlive mustEqual true
    obj.Health mustEqual obj.MaxHealth
    obj.Stamina mustEqual obj.MaxStamina
    obj.Armor mustEqual 35
    obj.Die
    obj.isAlive mustEqual false
    obj.Health mustEqual 0
    obj.Stamina mustEqual 0
    obj.Armor mustEqual 35
  }

  "draw equipped holsters only" in {
    val wep = SimpleItem(SimpleItemDefinition(149))
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Slot(1).Size = EquipmentSize.Pistol
    obj.Slot(1).Equipment = wep
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.DrawnSlot = 0
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
  }

  "remember the last drawn holster" in {
    val wep1 = SimpleItem(SimpleItemDefinition(149))
    val wep2 = SimpleItem(SimpleItemDefinition(149))
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Slot(0).Size = EquipmentSize.Pistol
    obj.Slot(0).Equipment = wep1
    obj.Slot(1).Size = EquipmentSize.Pistol
    obj.Slot(1).Equipment = wep2
    obj.DrawnSlot mustEqual Player.HandsDownSlot //default value
    obj.LastDrawnSlot mustEqual 0 //default value

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 0 //default value; sorry

    obj.DrawnSlot = 0
    obj.DrawnSlot mustEqual 0
    obj.LastDrawnSlot mustEqual 1

    obj.DrawnSlot = Player.HandsDownSlot
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = 0
    obj.DrawnSlot mustEqual 0
    obj.LastDrawnSlot mustEqual 1

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = Player.HandsDownSlot
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.LastDrawnSlot mustEqual 1
  }

  "install an implant" in {
    val testplant : ImplantDefinition = ImplantDefinition(1)
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Implants(0).Unlocked = true
    obj.InstallImplant(testplant) mustEqual Some(0)
    obj.Implants.find({p => p.Implant == ImplantType(1)}) match { //find the installed implant
      case Some(slot) =>
        slot.Installed mustEqual Some(testplant)
      case _ =>
        ko
    }
    ok
  }

  "can not install the same type of implant twice" in {
    val testplant1 : ImplantDefinition = ImplantDefinition(1)
    val testplant2 : ImplantDefinition = ImplantDefinition(1)
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Implants(0).Unlocked = true
    obj.Implants(1).Unlocked = true
    obj.InstallImplant(testplant1) mustEqual Some(0)
    obj.InstallImplant(testplant2) mustEqual Some(1)
  }

  "uninstall implants" in {
    val testplant : ImplantDefinition = ImplantDefinition(1)
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Implants(0).Unlocked = true
    obj.InstallImplant(testplant) mustEqual Some(0)
    obj.Implants(0).Installed mustEqual Some(testplant)

    obj.UninstallImplant(testplant.Type)
    obj.Implants(0).Installed mustEqual None
  }

  "administrate" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Admin mustEqual false
    Player.Administrate(obj, true)
    obj.Admin mustEqual true
    Player.Administrate(obj, false)
    obj.Admin mustEqual false
  }

  "spectate" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Spectator mustEqual false
    Player.Spectate(obj, true)
    obj.Spectator mustEqual true
    Player.Spectate(obj, false)
    obj.Spectator mustEqual false
  }
}
