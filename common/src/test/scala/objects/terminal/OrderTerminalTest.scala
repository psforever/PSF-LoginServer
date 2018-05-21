// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.{AmmoBox, Avatar, GlobalDefinitions, Player, Tool}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable.Specification

class OrderTerminalTest extends Specification {
  "Order_Terminal" should {
    val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    val terminal = Terminal(GlobalDefinitions.order_terminal)
    terminal.Owner = new Building(0, Zone.Nowhere, StructureType.Building)
    terminal.Owner.Faction = PlanetSideEmpire.TR

    "construct" in {
      val terminal = Terminal(GlobalDefinitions.order_terminal)
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can buy a box of ammunition ('9mmbullet_AP')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "9mmbullet_AP", 0, PlanetSideGUID(0))
      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.bullet_9mm_AP
      reply2.item.asInstanceOf[AmmoBox].Capacity mustEqual 50
    }

    "player can buy a weapon ('suppressor')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "suppressor", 0, PlanetSideGUID(0))
      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[Tool] mustEqual true
      reply2.item.asInstanceOf[Tool].Definition mustEqual GlobalDefinitions.suppressor
    }

    "player can buy a box of vehicle ammunition ('105mmbullet')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 3, "105mmbullet", 0, PlanetSideGUID(0))
      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.bullet_105mm
      reply2.item.asInstanceOf[AmmoBox].Capacity mustEqual 100
    }

    "player can buy a support tool ('bank')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 2, "bank", 0, PlanetSideGUID(0))
      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[Tool] mustEqual true
      reply2.item.asInstanceOf[Tool].Definition mustEqual GlobalDefinitions.bank
    }

    "player can buy different armor ('lite_armor')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.BuyExosuit(ExoSuitType.Agile)
    }

    "player can not buy fake equipment ('sabot')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "sabot", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can not buy equipment from the wrong page ('9mmbullet_AP', page 1)" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "9mmbullet_AP", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can retrieve an infantry loadout" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      player2.ExoSuit = ExoSuitType.Agile
      player2.Slot(0).Equipment = Tool(GlobalDefinitions.beamer)
      player2.Slot(6).Equipment = Tool(GlobalDefinitions.beamer)
      avatar.SaveLoadout(player2, "test", 0)

      val msg = terminal.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 0, PlanetSideGUID(0)))
      msg.isInstanceOf[Terminal.InfantryLoadout] mustEqual true
      val loadout = msg.asInstanceOf[Terminal.InfantryLoadout]
      loadout.exosuit mustEqual ExoSuitType.Agile
      loadout.subtype mustEqual 0
      loadout.holsters.size mustEqual 1
      loadout.holsters.head.obj.Definition mustEqual GlobalDefinitions.beamer
      loadout.holsters.head.start mustEqual 0
      loadout.inventory.head.obj.Definition mustEqual GlobalDefinitions.beamer
      loadout.inventory.head.start mustEqual 6
    }

    "player can not retrieve an infantry loadout from the wrong page" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      player2.ExoSuit = ExoSuitType.Agile
      player2.Slot(0).Equipment = Tool(GlobalDefinitions.beamer)
      player2.Slot(6).Equipment = Tool(GlobalDefinitions.beamer)
      avatar.SaveLoadout(player2, "test", 0)

      val msg = terminal.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 3, "", 0, PlanetSideGUID(0))) //page 3
      msg.isInstanceOf[Terminal.NoDeal] mustEqual true
    }

    "player can not retrieve an infantry loadout from the wrong line" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      player2.ExoSuit = ExoSuitType.Agile
      player2.Slot(0).Equipment = Tool(GlobalDefinitions.beamer)
      player2.Slot(6).Equipment = Tool(GlobalDefinitions.beamer)
      avatar.SaveLoadout(player2, "test", 0)

      val msg = terminal.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 1, PlanetSideGUID(0)))
      msg.isInstanceOf[Terminal.NoDeal] mustEqual true
    }
  }
}
