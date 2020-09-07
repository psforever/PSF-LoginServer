// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.{SimpleItemDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.locker.LockerEquipment
import net.psforever.types.{PlanetSideGUID, _}
import org.specs2.mutable._

import scala.util.Success

class PlayerTest extends Specification {
  def TestPlayer(
      name: String,
      faction: PlanetSideEmpire.Value,
      sex: CharacterGender.Value,
      head: Int,
      voice: CharacterVoice.Value
  ): Player = {
    new Player(Avatar(0, name, faction, sex, head, voice))
  }

  "Player" should {
    "construct" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.isAlive mustEqual false
      obj.FacingYawUpper mustEqual 0
      obj.Jumping mustEqual false
      obj.Crouching mustEqual false
      obj.Cloaked mustEqual false

      obj.FacingYawUpper = 1.3f
      obj.Jumping = true
      obj.Crouching = true
      obj.Cloaked = true
      obj.FacingYawUpper mustEqual 1.3f
      obj.Jumping mustEqual true
      obj.Crouching mustEqual true
      obj.Cloaked mustEqual true
    }

    "(re)spawn" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.isAlive mustEqual false
      obj.Health mustEqual 0
      obj.Armor mustEqual 0
      obj.MaxHealth mustEqual 100
      obj.MaxArmor mustEqual 50
      obj.Spawn()
      obj.isAlive mustEqual true
      obj.Health mustEqual 100
      obj.Armor mustEqual 50
    }

    "will not (re)spawn if not dead" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Spawn()
      obj.Health mustEqual 100
      obj.Armor mustEqual 50
      obj.isAlive mustEqual true

      obj.Health = 10
      obj.Armor = 10
      obj.Health mustEqual 10
      obj.Armor mustEqual 10
      obj.Spawn()
      obj.Health mustEqual 10
      obj.Armor mustEqual 10
    }

    "can die" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Spawn()
      obj.Armor = 35 //50 -> 35
      obj.isAlive mustEqual true
      obj.Health mustEqual obj.MaxHealth
      obj.Armor mustEqual 35
      obj.Die
      obj.isAlive mustEqual false
      obj.Health mustEqual 0
      obj.Armor mustEqual 35
    }

    "can not become a backpack if alive" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Spawn()
      obj.isAlive mustEqual true
      obj.isBackpack mustEqual false
      obj.Release
      obj.isAlive mustEqual true
      obj.isBackpack mustEqual false
    }

    "can become a backpack" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.isAlive mustEqual false
      obj.isBackpack mustEqual false
      obj.Release
      obj.isAlive mustEqual false
      obj.isBackpack mustEqual true
    }

    "set new maximum values (health, stamina)" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.MaxHealth mustEqual 100
      obj.MaxHealth = 123
      obj.MaxHealth mustEqual 123
      obj.MaxHealth = None
      //MaxStamina has no equivalent
      obj.MaxHealth mustEqual 100
    }

//    "set new values (health, armor, stamina) but only when alive" in {
//      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
//      obj.Health = 23
//      obj.Armor = 34
//      obj.Stamina = 45
//      obj.Health mustEqual 0
//      obj.Armor mustEqual 0
//      obj.Stamina mustEqual 0
    //
    //      obj.Spawn()
    //      obj.Health mustEqual obj.MaxHealth
//      obj.Armor mustEqual obj.MaxArmor
//      obj.Stamina mustEqual obj.MaxStamina
//      obj.Health = 23
//      obj.Armor = 34
//      obj.Stamina = 45
//      obj.Health mustEqual 23
//      obj.Armor mustEqual 34
//      obj.Stamina mustEqual 45
//    }

    "has visible slots" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.VisibleSlots mustEqual Set(0, 2, 4) //Standard
      obj.ExoSuit = ExoSuitType.Agile
      obj.VisibleSlots mustEqual Set(0, 1, 2, 4)
      obj.ExoSuit = ExoSuitType.Reinforced
      obj.VisibleSlots mustEqual Set(0, 1, 2, 3, 4)
      obj.ExoSuit = ExoSuitType.Infiltration
      obj.VisibleSlots mustEqual Set(0, 4)
      obj.ExoSuit = ExoSuitType.MAX
      obj.VisibleSlots mustEqual Set(0)
    }

    "init (Standard Exo-Suit)" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
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

    "draw equipped holsters only" in {
      val wep = SimpleItem(SimpleItemDefinition(149))
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
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
      val obj  = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Slot(0).Size = EquipmentSize.Pistol
      obj.Slot(0).Equipment = wep1
      obj.Slot(1).Size = EquipmentSize.Pistol
      obj.Slot(1).Equipment = wep2
      obj.DrawnSlot mustEqual Player.HandsDownSlot     //default value
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
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Slot(Player.FreeHandSlot).Equipment = wep

      obj.Slot(Player.FreeHandSlot).Equipment.get.Definition.ObjectId mustEqual 149
    }

    "provide an invalid hand that can not hold anything" in {
      val wep = SimpleItem(SimpleItemDefinition(149))
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Slot(-1).Equipment = wep

      obj.Slot(-1).Equipment.isEmpty mustEqual true
    }

    "search for the smallest available slot in which to store equipment" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Inventory.Resize(3, 3) //fits one item

      obj.Fit(Tool(GlobalDefinitions.beamer)).contains(0) mustEqual true

      obj.Fit(Tool(GlobalDefinitions.suppressor)).contains(2) mustEqual true

      val ammo  = AmmoBox(GlobalDefinitions.bullet_9mm)
      val ammo2 = AmmoBox(GlobalDefinitions.bullet_9mm)
      val ammo3 = AmmoBox(GlobalDefinitions.bullet_9mm)
      obj.Fit(ammo).contains(6) mustEqual true
      obj.Slot(6).Equipment = ammo
      obj.Fit(ammo2).contains(Player.FreeHandSlot) mustEqual true
      obj.Slot(Player.FreeHandSlot).Equipment = ammo2
      obj.Fit(ammo3).isEmpty mustEqual true
    }

    "can use their free hand to hold things" in {
      val obj  = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val ammo = AmmoBox(GlobalDefinitions.bullet_9mm)
      obj.FreeHand.Equipment.isEmpty mustEqual true

      obj.FreeHand.Equipment = ammo
      obj.FreeHand.Equipment.contains(ammo) mustEqual true
    }

    "can access the player's locker-space" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Slot(5).Equipment.get.isInstanceOf[LockerEquipment] mustEqual true
    }

    "can find equipment" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Slot(0).Equipment = {
        val item = Tool(beamer)
        item.GUID = PlanetSideGUID(1)
        item
      }
      obj.Slot(4).Equipment = {
        val item = Tool(forceblade)
        item.GUID = PlanetSideGUID(2)
        item
      }
      obj.Slot(6).Equipment = {
        val item = ConstructionItem(ace)
        item.GUID = PlanetSideGUID(3)
        item
      }
      obj.avatar.locker.Slot(6).Equipment = {
        val item = Kit(medkit)
        item.GUID = PlanetSideGUID(4)
        item
      }
      obj.FreeHand.Equipment = {
        val item = SimpleItem(remote_electronics_kit)
        item.GUID = PlanetSideGUID(5)
        item
      }

      obj.Find(PlanetSideGUID(1)).contains(0) mustEqual true                   //holsters
      obj.Find(PlanetSideGUID(2)).contains(4) mustEqual true                   //holsters, melee
      obj.Find(PlanetSideGUID(3)).contains(6) mustEqual true                   //inventory
      obj.Find(PlanetSideGUID(4)).isEmpty mustEqual true                       //can not find in locker-space
      obj.Find(PlanetSideGUID(5)).contains(Player.FreeHandSlot) mustEqual true //free hand
      obj.Find(PlanetSideGUID(6)).isEmpty mustEqual true                       //not here
    }

    "does equipment collision checking (are we already holding something there?)" in {
      val obj   = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val item1 = Tool(beamer)
      val item2 = Kit(medkit)
      val item3 = AmmoBox(GlobalDefinitions.bullet_9mm)
      obj.Slot(0).Equipment = item1
      obj.Slot(6).Equipment = item2
      obj.FreeHand.Equipment = item3

      obj.Collisions(0, 1, 1) match {
        case Success(List(item)) =>
          item.obj mustEqual item1
          item.start mustEqual 0
        case _ =>
          ko
      } //holsters

      obj.Collisions(1, 1, 1) match {
        case Success(List()) => ;
        case _ =>
          ko
      } //holsters, nothing

      obj.Collisions(6, 1, 1) match {
        case Success(List(item)) =>
          item.obj mustEqual item2
          item.start mustEqual 6
        case _ =>
          ko
      } //inventory

      obj.Collisions(Player.FreeHandSlot, 1, 1) match {
        case Success(List(item)) =>
          item.obj mustEqual item3
          item.start mustEqual Player.FreeHandSlot
        case _ =>
          ko
      } //free hand
    }

    "seat in a vehicle" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.VehicleSeated.isEmpty mustEqual true
      obj.VehicleSeated = PlanetSideGUID(65)
      obj.VehicleSeated.contains(PlanetSideGUID(65)) mustEqual true
      obj.VehicleSeated = None
      obj.VehicleSeated.isEmpty mustEqual true
    }

    "own in a vehicle" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.avatar.vehicle.isEmpty mustEqual true
      obj.avatar.vehicle = Some(PlanetSideGUID(65))
      obj.avatar.vehicle.contains(PlanetSideGUID(65)) mustEqual true
      obj.avatar.vehicle = None
      obj.avatar.vehicle.isEmpty mustEqual true
    }

    "remember what zone he is in" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.Continent mustEqual "home2"
      obj.Continent = "ugd01"
      obj.Continent mustEqual "ugd01"
    }

    "special is typically normal and can not be changed from normal" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
    }

    "a TR MAX can change its special to Overdrive or Anchored" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.ExoSuit = ExoSuitType.MAX
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Overdrive
      //note
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Overdrive
    }

    "an NC MAX can change its special to Shielded" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.ExoSuit = ExoSuitType.MAX
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Shielded
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
    }

    "one faction can not use the other's specials" in {
      val objtr = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      objtr.ExoSuit = ExoSuitType.MAX
      objtr.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      objtr.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
      objtr.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal

      val objnc = TestPlayer("Chord", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Voice5)
      objnc.ExoSuit = ExoSuitType.MAX
      objnc.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      objnc.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
      objnc.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      objnc.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
      objnc.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
    }

    "changing exo-suit type resets the special to Normal (and changing back does not revert it again)" in {
      val obj = TestPlayer("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      obj.ExoSuit = ExoSuitType.MAX
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Anchored

      val test = obj.UsingSpecial
      obj.ExoSuit = ExoSuitType.Standard
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
      obj.ExoSuit = ExoSuitType.MAX
      obj.UsingSpecial != test mustEqual true
    }
  }
}
