// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.definition.converter.{ACEConverter, CharacterSelectConverter, REKConverter}
import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment.CItem.{DeployedItem, Unit}
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{CharacterGender, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.util.Success

class ConverterTest extends Specification {
  "AmmoBox" should {
    val bullet_9mm = AmmoBoxDefinition(28)
        bullet_9mm.Capacity = 50

    "convert to packet" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedAmmoBoxData(8, 50)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual AmmoBoxData()
        case _ =>
          ko
      }
    }
  }

  "Tool" should {
    "convert to packet" in {
      val tdef = ToolDefinition(1076)
      tdef.Size = EquipmentSize.Rifle
      tdef.AmmoTypes += Ammo.shotgun_shell
      tdef.AmmoTypes += Ammo.shotgun_shell_AP
      tdef.FireModes += new FireModeDefinition
      tdef.FireModes.head.AmmoTypeIndices += 0
      tdef.FireModes.head.AmmoTypeIndices += 1
      tdef.FireModes.head.AmmoSlotIndex = 0
      val obj : Tool = Tool(tdef)
      val box = AmmoBox(PlanetSideGUID(90), new AmmoBoxDefinition(Ammo.shotgun_shell.id))
      obj.AmmoSlots.head.Box = box
      obj.AmmoSlots.head.Magazine = 30

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedWeaponData(4,8, Ammo.shotgun_shell.id, PlanetSideGUID(90), 0, DetailedAmmoBoxData(8, 30))
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual WeaponData(4,8, 0, Ammo.shotgun_shell.id, PlanetSideGUID(90), 0, AmmoBoxData())
        case _ =>
          ko
      }
    }
  }

  "Kit" should {
    "convert to packet" in {
      val kdef = KitDefinition(Kits.medkit)
      val obj = Kit(PlanetSideGUID(90), kdef)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedAmmoBoxData(0, 1)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual AmmoBoxData()
        case _ =>
          ko
      }
    }

    "ConstructionItem" should {
      "convert to packet" in {
        val cdef = ConstructionItemDefinition(Unit.advanced_ace)
        cdef.Modes += DeployedItem.tank_traps
        cdef.Modes += DeployedItem.portable_manned_turret_tr
        cdef.Modes += DeployedItem.deployable_shield_generator
        cdef.Tile = InventoryTile.Tile63
        cdef.Packet = new ACEConverter()
        val obj = ConstructionItem(PlanetSideGUID(90), cdef)
        obj.Definition.Packet.DetailedConstructorData(obj) match {
          case Success(pkt) =>
            pkt mustEqual DetailedACEData(0)
          case _ =>
            ko
        }
        obj.Definition.Packet.ConstructorData(obj) match {
          case Success(pkt) =>
            pkt mustEqual ACEData(0,0)
          case _ =>
            ko
        }
      }
    }
  }

  "SimpleItem" should {
    "convert to packet" in {
      val sdef = SimpleItemDefinition(SItem.remote_electronics_kit)
      sdef.Packet = new REKConverter()
      val obj = SimpleItem(PlanetSideGUID(90), sdef)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedREKData(8)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual REKData(8,0)
        case _ =>
          ko
      }
    }
  }

  "Player" should {
    val obj : Player = {
      /*
      Create an AmmoBoxDefinition with which to build two AmmoBoxes
      Create a ToolDefinition with which to create a Tool
      Load one of the AmmoBoxes into that Tool
      Create a Player
      Give the Player's Holster (2) the Tool
      Place the remaining AmmoBox into the Player's inventory in the third slot (8)
       */
      val bullet_9mm = AmmoBoxDefinition(28)
      bullet_9mm.Capacity = 50
      val box1 = AmmoBox(PlanetSideGUID(90), bullet_9mm)
      val box2 = AmmoBox(PlanetSideGUID(91), bullet_9mm)
      val tdef = ToolDefinition(1076)
      tdef.Name = "sample_weapon"
      tdef.Size = EquipmentSize.Rifle
      tdef.AmmoTypes += Ammo.bullet_9mm
      tdef.FireModes += new FireModeDefinition
      tdef.FireModes.head.AmmoTypeIndices += 0
      tdef.FireModes.head.AmmoSlotIndex = 0
      tdef.FireModes.head.Magazine = 18
      val tool = Tool(PlanetSideGUID(92), tdef)
      tool.AmmoSlots.head.Box = box1
      val obj = Player(PlanetSideGUID(93), "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      obj.Slot(2).Equipment = tool
      obj.Slot(5).Equipment.get.GUID = PlanetSideGUID(94)
      obj.Inventory += 8 -> box2
      obj
    }
    val converter = new CharacterSelectConverter

    "convert to packet (BR < 24)" in {
      obj.BEP = 0
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
    }

    "convert to packet (BR >= 24)" in {
      obj.BEP = 10000000
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
    }

    "convert to simple packet (BR < 24)" in {
      obj.BEP = 0
      converter.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
      converter.ConstructorData(obj).isFailure mustEqual true
      converter.ConstructorData(obj).get must throwA[Exception]
    }

    "convert to simple packet (BR >= 24)" in {
      obj.BEP = 10000000
      converter.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          ok
        case _ =>
          ko
      }
      converter.ConstructorData(obj).isFailure mustEqual true
      converter.ConstructorData(obj).get must throwA[Exception]
    }
  }

  "LockerContainer" should {
    "convert to packet (empty)" in {
      val obj = LockerContainer()
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedLockerContainerData(8, None)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual LockerContainerData(InventoryData(List.empty))
        case _ =>
          ko
      }
    }

    "convert to packet (occupied)" in {
      import GlobalDefinitions._
      val obj = LockerContainer()
      val rek = SimpleItem(remote_electronics_kit)
      rek.GUID = PlanetSideGUID(1)
      obj.Inventory += 0 -> rek

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedLockerContainerData(8, InternalSlot(remote_electronics_kit.ObjectId, PlanetSideGUID(1), 0, DetailedREKData(8)) :: Nil)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual LockerContainerData(InventoryData(InternalSlot(remote_electronics_kit.ObjectId, PlanetSideGUID(1), 0, REKData(8,0)) :: Nil))
        case _ =>
          ko
      }
    }
  }

  "Vehicle" should {
    "convert to packet" in {
      val hellfire_ammo = AmmoBoxDefinition(Ammo.hellfire_ammo.id)

      val fury_weapon_systema_def = ToolDefinition(ObjectClass.fury_weapon_systema)
          fury_weapon_systema_def.Size = EquipmentSize.VehicleWeapon
          fury_weapon_systema_def.AmmoTypes += Ammo.hellfire_ammo
          fury_weapon_systema_def.FireModes += new FireModeDefinition
          fury_weapon_systema_def.FireModes.head.AmmoTypeIndices += 0
          fury_weapon_systema_def.FireModes.head.AmmoSlotIndex = 0
          fury_weapon_systema_def.FireModes.head.Magazine = 2

      val fury_def = VehicleDefinition(ObjectClass.fury)
          fury_def.Seats += 0 -> new SeatDefinition()
          fury_def.Seats(0).Bailable = true
          fury_def.Seats(0).ControlledWeapon = Some(1)
          fury_def.MountPoints += 0 -> 0
          fury_def.MountPoints += 2 -> 0
          fury_def.Weapons += 1 -> fury_weapon_systema_def
          fury_def.TrunkSize = InventoryTile(11, 11)
          fury_def.TrunkOffset = 30

      val hellfire_ammo_box = AmmoBox(PlanetSideGUID(432), hellfire_ammo)

      val fury = Vehicle(PlanetSideGUID(413), fury_def)
          fury.Faction = PlanetSideEmpire.VS
          fury.Position = Vector3(3674.8438f, 2732f, 91.15625f)
          fury.Orientation = Vector3(0.0f, 0.0f, 90.0f)
          fury.WeaponControlledFromSeat(0).get.GUID = PlanetSideGUID(400)
          fury.WeaponControlledFromSeat(0).get.AmmoSlots.head.Box = hellfire_ammo_box

      fury.Definition.Packet.ConstructorData(fury).isSuccess mustEqual true
      ok //TODO write more of this test
    }
  }
}