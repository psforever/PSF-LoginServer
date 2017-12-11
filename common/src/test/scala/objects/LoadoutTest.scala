// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.types.{CharacterGender, ExoSuitType, PlanetSideEmpire}
import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import org.specs2.mutable._

class LoadoutTest extends Specification {
  def CreatePlayer() : Player = {
    val
    player = Player("TestCharacter", PlanetSideEmpire.VS, CharacterGender.Female, 41, 1)
    player.Slot(0).Equipment = Tool(beamer)
    player.Slot(2).Equipment = Tool(suppressor)
    player.Slot(4).Equipment = Tool(forceblade)
    player.Slot(6).Equipment = AmmoBox(bullet_9mm)
    player.Slot(9).Equipment = AmmoBox(bullet_9mm)
    player.Slot(12).Equipment = AmmoBox(bullet_9mm)
    player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
    player.Slot(36).Equipment = AmmoBox(energy_cell)
    player.Slot(39).Equipment = SimpleItem(remote_electronics_kit)
    player
  }

  "Player Loadout" should {
    "test sample player" in {
      val obj : Player = CreatePlayer()
      obj.Holsters()(0).Equipment.get.Definition mustEqual beamer
      obj.Holsters()(2).Equipment.get.Definition mustEqual suppressor
      obj.Holsters()(4).Equipment.get.Definition mustEqual forceblade
      obj.Slot(6).Equipment.get.Definition mustEqual bullet_9mm
      obj.Slot(9).Equipment.get.Definition mustEqual bullet_9mm
      obj.Slot(12).Equipment.get.Definition mustEqual bullet_9mm
      obj.Slot(33).Equipment.get.Definition mustEqual bullet_9mm_AP
      obj.Slot(36).Equipment.get.Definition mustEqual energy_cell
      obj.Slot(39).Equipment.get.Definition mustEqual remote_electronics_kit
    }

    "do not load, if never saved" in {
      CreatePlayer().LoadLoadout(0) mustEqual None
    }

    "save but incorrect load" in {
      val obj : Player = CreatePlayer()
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(1) mustEqual None
    }

    "save and load" in {
      val obj : Player = CreatePlayer()
      obj.Slot(0).Equipment.get.asInstanceOf[Tool].Magazine = 1 //non-standard but legal
      obj.Slot(2).Equipment.get.asInstanceOf[Tool].AmmoSlot.Magazine = 100 //non-standard (and out of range, real=25)
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case Some(items) =>
          items.Label mustEqual "test"
          items.ExoSuit mustEqual obj.ExoSuit
          items.Subtype mustEqual 0

          items.VisibleSlots.length mustEqual 3
          val holsters = items.VisibleSlots.sortBy(_.index)
          holsters.head.index mustEqual 0
          holsters.head.item.asInstanceOf[Loadout.ShorthandTool].tdef mustEqual beamer
          holsters.head.item.asInstanceOf[Loadout.ShorthandTool].ammo.head.ammo.capacity mustEqual 1
          holsters(1).index mustEqual 2
          holsters(1).item.asInstanceOf[Loadout.ShorthandTool].tdef mustEqual suppressor
          holsters(1).item.asInstanceOf[Loadout.ShorthandTool].ammo.head.ammo.capacity mustEqual 100
          holsters(2).index mustEqual 4
          holsters(2).item.asInstanceOf[Loadout.ShorthandTool].tdef mustEqual forceblade

          items.Inventory.length mustEqual 6
          val inventory = items.Inventory.sortBy(_.index)
          inventory.head.index mustEqual 6
          inventory.head.item.asInstanceOf[Loadout.ShorthandAmmoBox].adef mustEqual bullet_9mm
          inventory(1).index mustEqual 9
          inventory(1).item.asInstanceOf[Loadout.ShorthandAmmoBox].adef mustEqual bullet_9mm
          inventory(2).index mustEqual 12
          inventory(2).item.asInstanceOf[Loadout.ShorthandAmmoBox].adef mustEqual bullet_9mm
          inventory(3).index mustEqual 33
          inventory(3).item.asInstanceOf[Loadout.ShorthandAmmoBox].adef mustEqual bullet_9mm_AP
          inventory(4).index mustEqual 36
          inventory(4).item.asInstanceOf[Loadout.ShorthandAmmoBox].adef mustEqual energy_cell
          inventory(5).index mustEqual 39
          inventory(5).item.asInstanceOf[Loadout.ShorthandSimpleItem].sdef mustEqual remote_electronics_kit
        case None =>
          ko
      }
    }

    "save without inventory contents" in {
      val obj : Player = CreatePlayer()
      obj.Inventory.Clear()
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case Some(items) =>
          items.Label mustEqual "test"
          items.ExoSuit mustEqual obj.ExoSuit
          items.Subtype mustEqual 0
          items.VisibleSlots.length mustEqual 3
          items.Inventory.length mustEqual 0 //empty
        case None =>
          ko
      }
    }

    "save without visible slot contents" in {
      val obj : Player = CreatePlayer()
      obj.Slot(0).Equipment = None
      obj.Slot(2).Equipment = None
      obj.Slot(4).Equipment = None
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case Some(items) =>
          items.Label mustEqual "test"
          items.ExoSuit mustEqual obj.ExoSuit
          items.Subtype mustEqual 0
          items.VisibleSlots.length mustEqual 0 //empty
          items.Inventory.length mustEqual 6
        case None =>
          ko
      }
    }

    "save (a construction item) and load" in {
      val obj : Player = CreatePlayer()
      obj.Inventory.Clear()
      obj.Slot(6).Equipment = ConstructionItem(advanced_ace)
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case Some(items) =>
          items.Inventory.length mustEqual 1
          items.Inventory.head.index mustEqual 6
          items.Inventory.head.item.asInstanceOf[Loadout.ShorthandConstructionItem].cdef mustEqual advanced_ace
        case None =>
          ko
      }
    }

    "save (a kit) and load" in {
      val obj : Player = CreatePlayer()
      obj.Inventory.Clear()
      obj.Slot(6).Equipment = Kit(medkit)
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case Some(items) =>
          items.Inventory.length mustEqual 1
          items.Inventory.head.index mustEqual 6
          items.Inventory.head.item.asInstanceOf[Loadout.ShorthandKit].kdef mustEqual medkit
        case None =>
          ko
      }
    }

    "save, load, delete" in {
      val obj : Player = CreatePlayer()
      obj.SaveLoadout("test", 0)

      obj.LoadLoadout(0) match {
        case None =>
          ko
        case Some(_) => ; //good; keep going
      }
      obj.DeleteLoadout(0)
      obj.LoadLoadout(0) mustEqual None
    }

    "distinguish MAX subtype information" in {
      val obj : Player = CreatePlayer()
      val slot = obj.Slot(2)
      slot.Equipment = None //only an unequipped slot can have its Equipment Size changed (Rifle -> Max)
      Player.SuitSetup(obj, ExoSuitType.MAX)
      obj.SaveLoadout("generic", 0) //weaponless
      slot.Equipment = None
      slot.Equipment = Tool(trhev_dualcycler)
      obj.SaveLoadout("cycler", 1)
      slot.Equipment = None
      slot.Equipment = Tool(trhev_pounder)
      obj.SaveLoadout("pounder", 2)
      slot.Equipment = None
      slot.Equipment = Tool(trhev_burster)
      obj.SaveLoadout("burster", 3)

      obj.LoadLoadout(0).get.Subtype mustEqual 0
      obj.LoadLoadout(1).get.Subtype mustEqual 1
      obj.LoadLoadout(2).get.Subtype mustEqual 2
      obj.LoadLoadout(3).get.Subtype mustEqual 3
    }
  }
}
