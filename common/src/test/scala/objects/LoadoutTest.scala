// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
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
    val obj = Loadout.Create(player, "test")

    obj.Label mustEqual "test"
    obj.ExoSuit mustEqual obj.ExoSuit
    obj.Subtype mustEqual 0

    obj.VisibleSlots.length mustEqual 3
    val holsters = obj.VisibleSlots.sortBy(_.index)
    holsters.head.index mustEqual 0
    holsters.head.item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual beamer
    holsters(1).index mustEqual 2
    holsters(1).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual suppressor
    holsters(2).index mustEqual 4
    holsters(2).item.asInstanceOf[Loadout.ShorthandTool].definition mustEqual forceblade

    obj.Inventory.length mustEqual 5
    val inventory = obj.Inventory.sortBy(_.index)
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

  "distinguish MAX subtype information" in {
    val player = CreatePlayer()
    val slot = player.Slot(0)
    slot.Equipment = None //only an unequipped slot can have its Equipment Size changed (Rifle -> Max)
    Player.SuitSetup(player, ExoSuitType.MAX)

    val ldout1 = Loadout.Create(player, "weaponless")
    slot.Equipment = None
    slot.Equipment = Tool(trhev_dualcycler)
    val ldout2 = Loadout.Create(player, "cycler")
    slot.Equipment = None
    slot.Equipment = Tool(trhev_pounder)
    val ldout3 = Loadout.Create(player, "pounder")
    slot.Equipment = None
    slot.Equipment = Tool(trhev_burster)
    val ldout4 = Loadout.Create(player, "burster")

    ldout1.Subtype mustEqual 0
    ldout2.Subtype mustEqual 1
    ldout3.Subtype mustEqual 2
    ldout4.Subtype mustEqual 3
  }
}
