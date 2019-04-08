// Copyright (c) 2017 PSForever
package objects.terminal

import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable.Specification

class OrderTerminalTest extends Specification {
  val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val player = Player(avatar)

  val building = new Building(building_guid = 0, map_id = 0, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building)
  building.Faction = PlanetSideEmpire.TR
  val infantryTerminal = Terminal(GlobalDefinitions.order_terminal)
  infantryTerminal.Owner = building

  "General terminal behavior" should {
    "player can not buy equipment from the wrong page ('9mmbullet_AP', page 10)" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "9mmbullet_AP", 0, PlanetSideGUID(0))

      infantryTerminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }

  "Infantry Order Terminal" should {
    "player can buy a box of ammunition ('9mmbullet_AP')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "9mmbullet_AP", 0, PlanetSideGUID(0))
      val reply = infantryTerminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.bullet_9mm_AP
    }

    "player can buy a weapon ('suppressor')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "suppressor", 0, PlanetSideGUID(0))
      val reply = infantryTerminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[Tool] mustEqual true
      reply2.item.asInstanceOf[Tool].Definition mustEqual GlobalDefinitions.suppressor
    }

    "player can buy different armor ('lite_armor')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))

      infantryTerminal.Request(player, msg) mustEqual Terminal.BuyExosuit(ExoSuitType.Agile)
    }

    "player can buy a box of ammunition belonging to a special armor type ('dualcycler_ammo')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "dualcycler_ammo", 0, PlanetSideGUID(0))
      val reply = infantryTerminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.dualcycler_ammo
    }

    "player can buy a support tool ('bank')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 2, "bank", 0, PlanetSideGUID(0))
      val reply = infantryTerminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[Tool] mustEqual true
      reply2.item.asInstanceOf[Tool].Definition mustEqual GlobalDefinitions.bank
    }

    "player can buy a box of vehicle ammunition ('105mmbullet')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 3, "105mmbullet", 0, PlanetSideGUID(0))
      val reply = infantryTerminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.bullet_105mm
    }

    "player can not buy fake equipment ('sabot')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "sabot", 0, PlanetSideGUID(0))
      infantryTerminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can retrieve an infantry loadout" in {
      player.ExoSuit = ExoSuitType.Agile
      player.Slot(0).Equipment = Tool(GlobalDefinitions.beamer)
      player.Slot(6).Equipment = Tool(GlobalDefinitions.beamer)
      avatar.SaveLoadout(player, "test", 0)

      val msg = infantryTerminal.Request(player, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 0, PlanetSideGUID(0)))
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

    "player can not retrieve an infantry loadout from the wrong line" in {
      val msg = infantryTerminal.Request(player, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 1, PlanetSideGUID(0)))
      msg.isInstanceOf[Terminal.NoDeal] mustEqual true
    }
  }

  "Vehicle Terminal" should {
    val terminal = Terminal(GlobalDefinitions.ground_vehicle_terminal)
    terminal.Owner = building

    "player can spawn a vehicle and its default trunk contents" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 46769, "quadassault", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) match {
        case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
          vehicle.Definition mustEqual GlobalDefinitions.quadassault

          weapons.size mustEqual 0 //note: vehicles never have custom weapons using the default loadout

          trunk.size mustEqual 4
          trunk.head.obj.Definition mustEqual GlobalDefinitions.bullet_12mm
          trunk(1).obj.Definition mustEqual GlobalDefinitions.bullet_12mm
          trunk(2).obj.Definition mustEqual GlobalDefinitions.bullet_12mm
          trunk(3).obj.Definition mustEqual GlobalDefinitions.bullet_12mm
        case _ =>
          ko
      }
    }

    "player can not spawn a fake vehicle ('harasser')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 46769, "harasser", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can retrieve a vehicle loadout" in {
      val fury = Vehicle(GlobalDefinitions.fury)
      fury.Slot(30).Equipment = AmmoBox(GlobalDefinitions.hellfire_ammo)
      avatar.SaveLoadout(fury, "test", 10)

      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Loadout, 4, "test", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) match {
        case Terminal.VehicleLoadout(definition, weapons, trunk) =>
          definition mustEqual GlobalDefinitions.fury

          weapons.size mustEqual 1
          weapons.head.obj.Definition mustEqual GlobalDefinitions.fury_weapon_systema

          trunk.size mustEqual 1
          trunk.head.obj.Definition mustEqual GlobalDefinitions.hellfire_ammo
        case _ =>
          ko
      }

      ok
    }
  }

  "Certification Terminal" should {
    val terminal = Terminal(GlobalDefinitions.cert_terminal)
    terminal.Owner = building

    "player can learn a certification ('medium_assault')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "medium_assault", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.LearnCertification(CertificationType.MediumAssault)
    }

    "player can not learn a fake certification ('juggling')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "juggling", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can forget a certification ('medium_assault')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "medium_assault", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.SellCertification(CertificationType.MediumAssault)
    }

    "player can not forget a fake certification ('juggling')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "juggling", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }

  "Implant_Terminal_Interface" should {
    val terminal = Terminal(GlobalDefinitions.implant_terminal_interface)
    terminal.Owner = building

    "player can learn an implant ('darklight_vision')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "darklight_vision", 0, PlanetSideGUID(0))

      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.LearnImplant] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.LearnImplant]
      reply2.implant mustEqual GlobalDefinitions.darklight_vision
    }

    "player can not learn a fake implant ('aimbot')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "aimbot", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can un-learn an implant ('darklight_vision')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "darklight_vision", 0, PlanetSideGUID(0))

      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.SellImplant] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.SellImplant]
      reply2.implant mustEqual GlobalDefinitions.darklight_vision
    }

    "player can not un-learn a fake implant ('aimbot')" in {
      val terminal = Terminal(GlobalDefinitions.implant_terminal_interface)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "aimbot", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
