// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.loadouts._
import net.psforever.types.{CharacterGender, ExoSuitType, PlanetSideEmpire}
import net.psforever.objects.GlobalDefinitions._
import org.specs2.mutable._

class LoadoutTest extends Specification {
  val avatar = Avatar("TestCharacter", PlanetSideEmpire.VS, CharacterGender.Female, 41, 1)

  def CreatePlayer() : Player = {
    new Player(avatar) {
      Slot(0).Equipment = Tool(beamer)
      Slot(2).Equipment = Tool(suppressor)
      Slot(4).Equipment = Tool(forceblade)
      Slot(6).Equipment = ConstructionItem(ace)
      Slot(9).Equipment = AmmoBox(bullet_9mm)
      Slot(12).Equipment = AmmoBox(bullet_9mm)
      Slot(33).Equipment = Kit(medkit)
      Slot(39).Equipment = SimpleItem(remote_electronics_kit)
    }
  }

  "test sample player" in {
    val player = CreatePlayer()
    player.Holsters()(0).Equipment.get.Definition mustEqual beamer
    player.Holsters()(2).Equipment.get.Definition mustEqual suppressor
    player.Holsters()(4).Equipment.get.Definition mustEqual forceblade
    player.Slot(6).Equipment.get.Definition mustEqual ace
    player.Slot(9).Equipment.get.Definition mustEqual bullet_9mm
    player.Slot(12).Equipment.get.Definition mustEqual bullet_9mm
    player.Slot(33).Equipment.get.Definition mustEqual medkit
    player.Slot(39).Equipment.get.Definition mustEqual remote_electronics_kit
  }

  "create a loadout that contains a player's inventory" in {
    val player = CreatePlayer()
    val obj = Loadout.Create(player, "test").asInstanceOf[InfantryLoadout]

    obj.label mustEqual "test"
    obj.exosuit mustEqual ExoSuitType.Standard
    obj.subtype mustEqual 0

    obj.visible_slots.length mustEqual 3
    val holsters = obj.visible_slots.sortBy(_.index)
    holsters.head.index mustEqual 0
    holsters.head.item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual beamer
    holsters(1).index mustEqual 2
    holsters(1).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual suppressor
    holsters(2).index mustEqual 4
    holsters(2).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual forceblade

    obj.inventory.length mustEqual 5
    val inventory = obj.inventory.sortBy(_.index)
    inventory.head.index mustEqual 6
    inventory.head.item.asInstanceOf[Loadout.ShorthandConstructionItem].definition mustEqual ace
    inventory(1).index mustEqual 9
    inventory(1).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
    inventory(2).index mustEqual 12
    inventory(2).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
    inventory(3).index mustEqual 33
    inventory(3).item.asInstanceOf[Loadout.ShorthandKit].definition mustEqual medkit
    inventory(4).index mustEqual 39
    inventory(4).item.asInstanceOf[Loadout.ShorthandSimpleItem].definition mustEqual remote_electronics_kit
  }

  "create a loadout that contains a vehicle's inventory" in {
    val vehicle = Vehicle(mediumtransport)
    vehicle.Inventory += 30 -> AmmoBox(bullet_9mm)
    vehicle.Inventory += 33 -> AmmoBox(bullet_9mm_AP)
    val obj = Loadout.Create(vehicle, "test").asInstanceOf[VehicleLoadout]

    obj.label mustEqual "test"
    obj.vehicle_definition mustEqual mediumtransport

    obj.visible_slots.length mustEqual 2
    val holsters = obj.visible_slots.sortBy(_.index)
    holsters.head.index mustEqual 5
    holsters.head.item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual mediumtransport_weapon_systemA
    holsters(1).index mustEqual 6
    holsters(1).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual mediumtransport_weapon_systemB

    obj.inventory.length mustEqual 2
    val inventory = obj.inventory.sortBy(_.index)
    inventory.head.index mustEqual 30
    inventory.head.item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm
    inventory(1).index mustEqual 33
    inventory(1).item.asInstanceOf[Loadout.ShorthandAmmoBox].definition mustEqual bullet_9mm_AP
  }

  "distinguish MAX subtype information" in {
    val player = CreatePlayer()
    val slot = player.Slot(0)
    slot.Equipment = None //only an unequipped slot can have its Equipment Size changed (Rifle -> Max)
    player.ExoSuit = ExoSuitType.MAX

    val ldout1 = Loadout.Create(player, "weaponless").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_dualcycler)
    val ldout2 = Loadout.Create(player, "cycler").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_pounder)
    val ldout3 = Loadout.Create(player, "pounder").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_burster)
    val ldout4 = Loadout.Create(player, "burster").asInstanceOf[InfantryLoadout]

    ldout1.subtype mustEqual 0
    ldout2.subtype mustEqual 1
    ldout3.subtype mustEqual 2
    ldout4.subtype mustEqual InfantryLoadout.DetermineSubtype(player) //example
  }

  "players have additional uniform subtype" in {
    val player = CreatePlayer()
    val slot = player.Slot(0)
    slot.Equipment = None //only an unequipped slot can have its Equipment Size changed (Rifle -> Max)

    player.ExoSuit = ExoSuitType.Standard
    val ldout0 = Loadout.Create(player, "standard").asInstanceOf[InfantryLoadout]
    player.ExoSuit = ExoSuitType.Agile
    val ldout1 = Loadout.Create(player, "agile").asInstanceOf[InfantryLoadout]
    player.ExoSuit = ExoSuitType.Reinforced
    val ldout2 = Loadout.Create(player, "rein").asInstanceOf[InfantryLoadout]
    player.ExoSuit = ExoSuitType.Infiltration
    val ldout7 = Loadout.Create(player, "inf").asInstanceOf[InfantryLoadout]

    player.ExoSuit = ExoSuitType.MAX
    val ldout3 = Loadout.Create(player, "weaponless").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_dualcycler)
    val ldout4 = Loadout.Create(player, "cycler").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_pounder)
    val ldout5 = Loadout.Create(player, "pounder").asInstanceOf[InfantryLoadout]
    slot.Equipment = None
    slot.Equipment = Tool(trhev_burster)
    val ldout6 = Loadout.Create(player, "burster").asInstanceOf[InfantryLoadout]

    InfantryLoadout.DetermineSubtypeB(ldout0.exosuit, ldout0.subtype) mustEqual 0
    InfantryLoadout.DetermineSubtypeB(ldout1.exosuit, ldout1.subtype) mustEqual 1
    InfantryLoadout.DetermineSubtypeB(ldout2.exosuit, ldout2.subtype) mustEqual 2
    InfantryLoadout.DetermineSubtypeB(ldout3.exosuit, ldout3.subtype) mustEqual 3
    InfantryLoadout.DetermineSubtypeB(ldout4.exosuit, ldout4.subtype) mustEqual 4
    InfantryLoadout.DetermineSubtypeB(ldout5.exosuit, ldout5.subtype) mustEqual 5
    InfantryLoadout.DetermineSubtypeB(ldout6.exosuit, ldout6.subtype) mustEqual 6
    InfantryLoadout.DetermineSubtypeB(ldout7.exosuit, ldout7.subtype) mustEqual 7
  }
}
