// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects._
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types.{CharacterGender, PlanetSideEmpire, TransactionType}
import org.specs2.mutable.Specification

class RepairRearmSiloTest extends Specification {
  "RepairRearmSilo" should {
    val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    val silo = Terminal(GlobalDefinitions.repair_silo)
    silo.Owner = new Building(0, Zone.Nowhere, StructureType.Building)
    silo.Owner.Faction = PlanetSideEmpire.TR

    "define" in {
      GlobalDefinitions.repair_silo.ObjectId mustEqual 729
    }

    "construct" in {
      val obj = Terminal(GlobalDefinitions.repair_silo)
      obj.Actor mustEqual ActorRef.noSender
    }

    "player can buy a box of ammunition ('bullet_35mm')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 3, "35mmbullet", 0, PlanetSideGUID(0))
      val reply = silo.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyEquipment] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyEquipment]
      reply2.item.isInstanceOf[AmmoBox] mustEqual true
      reply2.item.asInstanceOf[AmmoBox].Definition mustEqual GlobalDefinitions.bullet_35mm
      reply2.item.asInstanceOf[AmmoBox].Capacity mustEqual 100
    }

    "player can not buy fake equipment ('sabot')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 3, "sabot", 0, PlanetSideGUID(0))

      silo.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can not buy equipment from the wrong page ('35mmbullet', page 1)" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "35mmbullet", 0, PlanetSideGUID(0))

      silo.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can retrieve a vehicle loadout" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      val vehicle = Vehicle(GlobalDefinitions.fury)
      vehicle.Slot(30).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
      avatar.SaveLoadout(vehicle, "test", 10)

      val msg = silo.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 0, PlanetSideGUID(0)))
      msg.isInstanceOf[Terminal.VehicleLoadout] mustEqual true
      val loadout = msg.asInstanceOf[Terminal.VehicleLoadout]
      loadout.vehicle_definition mustEqual GlobalDefinitions.fury
      loadout.weapons.size mustEqual 1
      loadout.weapons.head.obj.Definition mustEqual GlobalDefinitions.fury_weapon_systema
      loadout.weapons.head.start mustEqual 1
      loadout.inventory.head.obj.Definition mustEqual GlobalDefinitions.bullet_9mm
      loadout.inventory.head.start mustEqual 30
    }

    "player can not retrieve a vehicle loadout from the wrong line" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      val vehicle = Vehicle(GlobalDefinitions.fury)
      vehicle.Slot(30).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
      avatar.SaveLoadout(vehicle, "test", 10)

      val msg = silo.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 3, "", 0, PlanetSideGUID(0))) //page 3
      msg.isInstanceOf[Terminal.NoDeal] mustEqual true
    }

    "player can not retrieve a vehicle loadout from the wrong line" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player(avatar)
      val vehicle = Vehicle(GlobalDefinitions.fury)
      vehicle.Slot(30).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
      avatar.SaveLoadout(vehicle, "test", 10)

      val msg = silo.Request(player2, ItemTransactionMessage(PlanetSideGUID(10), TransactionType.Loadout, 4, "", 1, PlanetSideGUID(0))) //line 11
      msg.isInstanceOf[Terminal.NoDeal] mustEqual true
    }
  }
}
