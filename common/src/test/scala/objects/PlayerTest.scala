// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.definition.{ImplantDefinition, SimpleItemDefinition}
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, ExoSuitType, ImplantType, PlanetSideEmpire}
import org.specs2.mutable._

class PlayerTest extends Specification {
  "construct" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.isAlive mustEqual false
  }

  "different players" in {
    (Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)) mustEqual true
    (Player("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)) mustEqual false
    (Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord", PlanetSideEmpire.NC, CharacterGender.Male, 0, 5)) mustEqual false
    (Player("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord2", PlanetSideEmpire.TR, CharacterGender.Female, 0, 5)) mustEqual false
    (Player("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 1, 5)) mustEqual false
    (Player("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5) ==
      Player("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 6)) mustEqual false
  }

  "become a backpack" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.isAlive mustEqual false
    obj.isBackpack mustEqual false
    obj.Release
    obj.isAlive mustEqual false
    obj.isBackpack mustEqual true
  }

  "(re)spawn" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.isAlive mustEqual false
    obj.Health mustEqual 0
    obj.Stamina mustEqual 0
    obj.Armor mustEqual 0
    obj.MaxHealth mustEqual 100
    obj.MaxStamina mustEqual 100
    obj.MaxArmor mustEqual 50
    obj.Spawn
    obj.isAlive mustEqual true
    obj.Health mustEqual 100
    obj.Stamina mustEqual 100
    obj.Armor mustEqual 50
  }

  "set new maximum values (health, stamina)" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.MaxHealth mustEqual 100
    obj.MaxStamina mustEqual 100
    obj.MaxHealth = 123
    obj.MaxStamina = 456
    obj.Spawn
    obj.Health mustEqual 123
    obj.Stamina mustEqual 456
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
    obj.LastDrawnSlot mustEqual Player.HandsDownSlot //default value

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 1

    obj.DrawnSlot = 0
    obj.DrawnSlot mustEqual 0
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = Player.HandsDownSlot
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 1

    obj.DrawnSlot = 0
    obj.DrawnSlot mustEqual 0
    obj.LastDrawnSlot mustEqual 0

    obj.DrawnSlot = 1
    obj.DrawnSlot mustEqual 1
    obj.LastDrawnSlot mustEqual 1

    obj.DrawnSlot = Player.HandsDownSlot
    obj.DrawnSlot mustEqual Player.HandsDownSlot
    obj.LastDrawnSlot mustEqual 1
  }

  "hold something in their free hand" in {
    val wep = SimpleItem(SimpleItemDefinition(149))
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Slot(Player.FreeHandSlot).Equipment = wep

    obj.Slot(Player.FreeHandSlot).Equipment.get.Definition.ObjectId mustEqual 149
  }

  "provide an invalid hand that can not hold anything" in {
    val wep = SimpleItem(SimpleItemDefinition(149))
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Slot(-1).Equipment = wep

    obj.Slot(-1).Equipment mustEqual None
  }

  "search for the smallest available slot in which to satore equipment" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Inventory.Resize(3,3)

    obj.Fit(Tool(GlobalDefinitions.beamer)) mustEqual Some(0)

    obj.Fit(Tool(GlobalDefinitions.suppressor)) mustEqual Some(2)

    val ammo = AmmoBox(GlobalDefinitions.bullet_9mm)
    val ammo2 = AmmoBox(GlobalDefinitions.bullet_9mm)
    val ammo3 = AmmoBox(GlobalDefinitions.bullet_9mm)
    obj.Fit(ammo) mustEqual Some(6)
    obj.Slot(6).Equipment = ammo
    obj.Fit(ammo2) mustEqual Some(Player.FreeHandSlot)
    obj.Slot(Player.FreeHandSlot).Equipment = ammo2
    obj.Fit(ammo2) mustEqual None
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

  "seat in a vehicle" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.VehicleSeated mustEqual None
    obj.VehicleSeated = PlanetSideGUID(65)
    obj.VehicleSeated mustEqual Some(PlanetSideGUID(65))
    obj.VehicleSeated = None
    obj.VehicleSeated mustEqual None
  }

  "own in a vehicle" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.VehicleOwned mustEqual None
    obj.VehicleOwned = PlanetSideGUID(65)
    obj.VehicleOwned mustEqual Some(PlanetSideGUID(65))
    obj.VehicleOwned = None
    obj.VehicleOwned mustEqual None
  }

  "remember what zone he is in" in {
    val obj = new Player("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
    obj.Continent mustEqual "home2"
    obj.Continent = "ugd01"
    obj.Continent mustEqual "ugd01"
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
