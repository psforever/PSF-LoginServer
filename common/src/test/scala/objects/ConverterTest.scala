// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.definition.converter.{CharacterSelectConverter, DestroyedVehicleConverter, REKConverter}
import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.vehicles.UtilityType
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable.Specification

import scala.util.{Failure, Success}

class ConverterTest extends Specification {
  "AmmoBox" should {
    val bullet_9mm = AmmoBoxDefinition(28)
        bullet_9mm.Capacity = 50

    "convert to packet" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedAmmoBoxData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL,
              bops = false,
              alternate = false,
              true,
              None,
              false,
              None,
              None,
              PlanetSideGUID(0)
            ),
            obj.Capacity
          )
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual CommonFieldData(
            PlanetSideEmpire.NEUTRAL,
            bops = false,
            alternate = false,
            false,
            None,
            false,
            Some(false),
            None,
            PlanetSideGUID(0)
          )
        case _ =>
          ko
      }
    }
  }

  "Tool" should {
    "convert to packet (1 fire mode slot)" in {
      val obj : Tool = Tool(GlobalDefinitions.flechette)
      obj.AmmoSlot.Box.GUID = PlanetSideGUID(90)

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedWeaponData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
            0,
            List(InternalSlot(Ammo.shotgun_shell.id, PlanetSideGUID(90), 0, DetailedAmmoBoxData(8, 12)))
          )
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual WeaponData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
            0,
            List(InternalSlot(Ammo.shotgun_shell.id, PlanetSideGUID(90), 0,
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, Some(false), None, PlanetSideGUID(0)))
            )
          )
        case _ =>
          ko
      }
    }

    "convert to packet (2 fire mode slots)" in {
      val obj : Tool = Tool(GlobalDefinitions.punisher)
      obj.AmmoSlots.head.Box.GUID = PlanetSideGUID(90)
      obj.AmmoSlots(1).Box.GUID = PlanetSideGUID(91)

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedWeaponData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
            0,
            List(
              InternalSlot(Ammo.bullet_9mm.id, PlanetSideGUID(90), 0, DetailedAmmoBoxData(8, 30)),
              InternalSlot(Ammo.rocket.id, PlanetSideGUID(91), 1, DetailedAmmoBoxData(8, 1))
            )
          )
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual WeaponData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL, //TODO need faction affinity
              bops = false,
              alternate = false,
              true,
              None,
              false,
              None,
              None,
              PlanetSideGUID(0)
            ),
            0,
            List(
              InternalSlot(Ammo.bullet_9mm.id, PlanetSideGUID(90), 0, CommonFieldData()(false)),
              InternalSlot(Ammo.rocket.id, PlanetSideGUID(91), 1, CommonFieldData()(false))
            )
          )
        case _ =>
          ko
      }
    }
  }

  "Kit" should {
    "convert to packet" in {
      val kdef = KitDefinition(Kits.medkit)
      val obj = Kit(kdef)
      obj.GUID = PlanetSideGUID(90)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedAmmoBoxData(0, 1)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual CommonFieldData()(false)
        case _ =>
          ko
      }
    }

    "ConstructionItem" should {
      "convert to packet" in {
        val obj = ConstructionItem(GlobalDefinitions.ace)
        obj.GUID = PlanetSideGUID(90)
        obj.Definition.Packet.DetailedConstructorData(obj) match {
          case Success(pkt) =>
            pkt mustEqual DetailedConstructionToolData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0))
            )
          case _ =>
            ko
        }

        obj.Definition.Packet.ConstructorData(obj) match {
          case Success(pkt) =>
            pkt mustEqual HandheldData(
              CommonFieldData(
                PlanetSideEmpire.NEUTRAL,
                false,
                false,
                true,
                None,
                false,
                None,
                None,
                PlanetSideGUID(0)
              )
            )
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
      val obj = SimpleItem(sdef)
      obj.GUID = PlanetSideGUID(90)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedREKData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL, //TODO faction affinity
              false,
              false,
              true,
              None,
              false,
              Some(false),
              None,
              PlanetSideGUID(0)
            )
          )
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual REKData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL,
              false,
              false,
              true,
              None,
              false,
              Some(false),
              None,
              PlanetSideGUID(0)
            )
          )
        case _ =>
          ko
      }
    }
  }

  "BoomerTrigger" should {
    "convert" in {
      val obj = new BoomerTrigger
      obj.GUID = PlanetSideGUID(90)
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedConstructionToolData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0))
          )
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual HandheldData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, None, None, PlanetSideGUID(0))
          )
        case _ =>
          ko
      }
    }
  }

  "Telepad" should {
    "convert (success)" in {
      val obj = new Telepad(GlobalDefinitions.router_telepad)
      obj.Router = PlanetSideGUID(1001)
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual HandheldData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL,
              false,
              false,
              false,
              None,
              false,
              None,
              Some(1001),
              PlanetSideGUID(0)
            )
          )
        case _ =>
          ko
      }

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DetailedConstructionToolData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, Some(1001), PlanetSideGUID(0))
          )
        case _ =>
          ko
      }
    }

    "convert (failure; no router)" in {
      val obj = new Telepad(GlobalDefinitions.router_telepad)
      //obj.Router = PlanetSideGUID(1001)
      obj.Definition.Packet.ConstructorData(obj).isFailure mustEqual true


      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true
    }
  }

  "SmallDeployable" should {
    "convert" in {
      val obj = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj.Faction = PlanetSideEmpire.TR
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual CommonFieldDataWithPlacement(
            PlacementData(Vector3.Zero, Vector3.Zero),
            CommonFieldData(
              PlanetSideEmpire.TR,
              false,
              false,
              false,
              None,
              false,
              Some(false),
              None,
              PlanetSideGUID(0)
            )
          )
        case _ =>
          ko
      }
    }
  }

  "SmallTurret" should {
    "convert" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Weapons(1).Equipment.get.GUID = PlanetSideGUID(91)
      obj.Weapons(1).Equipment.get.asInstanceOf[Tool].AmmoSlot.Box.GUID = PlanetSideGUID(92)
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual SmallTurretData(
            CommonFieldDataWithPlacement(
              PlacementData(Vector3.Zero, Vector3.Zero),
              CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, Some(true), None, PlanetSideGUID(0))
            ),
            255,
            InventoryData(
              List(InternalSlot(ObjectClass.spitfire_weapon, PlanetSideGUID(91), 1,
                WeaponData(
                  CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
                  0,
                  List(InternalSlot(Ammo.spitfire_ammo.id, PlanetSideGUID(92), 0,
                    CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, Some(false), None, PlanetSideGUID(0)))
                  ))
                )
              )
            )
          )
        case _ =>
          ko
      }
    }
  }

  "FieldTurret" should {
    "convert" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Weapons(1).Equipment.get.GUID = PlanetSideGUID(91)
      obj.Weapons(1).Equipment.get.asInstanceOf[Tool].AmmoSlot.Box.GUID = PlanetSideGUID(92)
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual OneMannedFieldTurretData(
            CommonFieldDataWithPlacement(
              PlacementData(Vector3.Zero, Vector3.Zero),
              CommonFieldData(PlanetSideEmpire.TR, false, false, true, None, false, Some(false), None, PlanetSideGUID(0))
            ),
            255,
            InventoryData(
              List(InternalSlot(ObjectClass.energy_gun_tr, PlanetSideGUID(91), 1,
                WeaponData(
                  CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
                  0,
                  List(InternalSlot(Ammo.energy_gun_ammo.id, PlanetSideGUID(92), 0,
                    CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, Some(false), None, PlanetSideGUID(0)))
                  ))
                )
              )
            )
          )
        case _ =>
          ko
      }
    }
  }

  "TRAP" should {
    "convert" in {
      val obj = new TrapDeployable(GlobalDefinitions.tank_traps)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual TRAPData(
            CommonFieldDataWithPlacement(
              PlacementData(Vector3.Zero, Vector3.Zero),
              CommonFieldData(
                PlanetSideEmpire.TR,
                bops = false,
                alternate = false,
                true,
                None,
                false,
                Some(true),
                None,
                PlanetSideGUID(0)
              )
            ),
            255
          )
        case _ =>
          ko
      }
    }
  }

  "ShieldGenerator" should {
    "convert" in {
      val obj = new ShieldGeneratorDeployable(GlobalDefinitions.deployable_shield_generator)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual AegisShieldGeneratorData(
            CommonFieldDataWithPlacement(
              PlacementData(Vector3.Zero, Vector3.Zero),
              PlanetSideEmpire.TR,
              0
            ),
            255
          )
        case _ =>
          ko
      }
    }
  }

  "TelepadDeployable" should {
    "convert (success)" in {
      val obj = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Router = PlanetSideGUID(1001)
      obj.Owner = PlanetSideGUID(5001)
      obj.Health = 1
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DroppedItemData(
            PlacementData(Vector3.Zero, Vector3.Zero),
            TelepadDeployableData(
              CommonFieldData(
                PlanetSideEmpire.TR,
                bops = false,
                alternate = false,
                true,
                None,
                false,
                None,
                Some(1001),
                PlanetSideGUID(5001)
              ),
              unk1 = 87,
              unk2 = 12
            )
          )
        case _ =>
          ko
      }
    }

    "convert (success; destroyed)" in {
      val obj = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Router = PlanetSideGUID(1001)
      obj.Owner = PlanetSideGUID(5001)
      obj.Health = 0
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual DroppedItemData(
            PlacementData(Vector3.Zero, Vector3.Zero),
            TelepadDeployableData(
              CommonFieldData(
                PlanetSideEmpire.TR,
                bops = false,
                alternate = true,
                true,
                None,
                false,
                None,
                Some(1001),
                PlanetSideGUID(0)
              ),
              unk1 = 0,
              unk2 = 6
            )
          )
        case _ =>
          ko
      }
    }

    "convert (failure; no router)" in {
      val obj = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      //obj.Router = PlanetSideGUID(1001)
      obj.Owner = PlanetSideGUID(5001)
      obj.Health = 1
      obj.Definition.Packet.ConstructorData(obj).isFailure mustEqual true

      obj.Router = PlanetSideGUID(0)
      obj.Definition.Packet.ConstructorData(obj).isFailure mustEqual true
    }

    "convert (failure; detailed)" in {
      val obj = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      obj.Faction = PlanetSideEmpire.TR
      obj.GUID = PlanetSideGUID(90)
      obj.Router = PlanetSideGUID(1001)
      obj.Owner = PlanetSideGUID(5001)
      obj.Health = 1
      obj.Definition.Packet.DetailedConstructorData(obj).isFailure mustEqual true
    }
  }

  "Player" should {
    val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
    val obj : Player = {
      /*
      Create an AmmoBoxDefinition with which to build two AmmoBoxes
      Create a ToolDefinition with which to create a Tool
      Load one of the AmmoBoxes into that Tool
      Create a Player
      Give the Player's Holster (2) the Tool
      Place the remaining AmmoBox into the Player's inventory in the third slot (8)
       */
      val tdef = ToolDefinition(1076)
      tdef.Name = "sample_weapon"
      tdef.Size = EquipmentSize.Rifle
      tdef.AmmoTypes += GlobalDefinitions.bullet_9mm
      tdef.FireModes += new FireModeDefinition
      tdef.FireModes.head.AmmoTypeIndices += 0
      tdef.FireModes.head.AmmoSlotIndex = 0
      tdef.FireModes.head.Magazine = 18
      val tool = Tool(tdef)
      tool.GUID = PlanetSideGUID(92)
      tool.AmmoSlot.Box.GUID = PlanetSideGUID(90)
      val obj = Player(avatar)
      obj.GUID = PlanetSideGUID(93)
      obj.Slot(2).Equipment = tool
      obj.Slot(5).Equipment.get.GUID = PlanetSideGUID(94)
      obj.Inventory += 8 -> AmmoBox(GlobalDefinitions.bullet_9mm)
      obj.Slot(8).Equipment.get.GUID = PlanetSideGUID(91)
      obj
    }
    val converter = new CharacterSelectConverter

    "convert to packet (BR < 24)" in {
      avatar.BEP = 0
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
    }

    "convert to packet (BR >= 24)" in {
      avatar.BEP = 10000000
      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
    }

    "convert to simple packet (BR < 24)" in {
      avatar.BEP = 0
      converter.DetailedConstructorData(obj) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
      converter.ConstructorData(obj).isFailure mustEqual true
      converter.ConstructorData(obj).get must throwA[Exception]
    }

    "convert to simple packet (BR >= 24)" in {
      avatar.BEP = 10000000
      converter.DetailedConstructorData(obj) match {
        case Success(_) =>
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
          pkt mustEqual DetailedLockerContainerData(CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)), None)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual LockerContainerData(None)
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
          pkt mustEqual DetailedLockerContainerData(8, InternalSlot(remote_electronics_kit.ObjectId, PlanetSideGUID(1), 0, DetailedREKData(CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)))) :: Nil)
        case _ =>
          ko
      }
      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual LockerContainerData(InventoryData(InternalSlot(remote_electronics_kit.ObjectId, PlanetSideGUID(1), 0, REKData(CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)))) :: Nil))
        case _ =>
          ko
      }
    }
  }

  "Terminal" should {
    "convert to packet" in {
      val obj = Terminal(GlobalDefinitions.order_terminala)

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Failure(err) =>
          err.isInstanceOf[NoSuchMethodException] mustEqual true
        case _ =>
          ko
      }

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual CommonFieldData(PlanetSideEmpire.NEUTRAL)(false)
        case _ =>
          ko
      }
    }
  }

  "Spawn Tube" should {
    "convert to packet" in {
      val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)

      obj.Definition.Packet.DetailedConstructorData(obj) match {
        case Failure(err) =>
          err.isInstanceOf[NoSuchMethodException] mustEqual true
        case _ =>
          ko
      }

      obj.Definition.Packet.ConstructorData(obj) match {
        case Success(pkt) =>
          pkt mustEqual CommonFieldData(PlanetSideEmpire.NEUTRAL)(false)
        case _ =>
          ko
      }
    }
  }

  "Vehicle" should {
    "convert to packet (1)" in {
      val hellfire_ammo = AmmoBoxDefinition(Ammo.hellfire_ammo.id)

      val fury_weapon_systema_def = ToolDefinition(ObjectClass.fury_weapon_systema)
          fury_weapon_systema_def.Size = EquipmentSize.VehicleWeapon
          fury_weapon_systema_def.AmmoTypes += GlobalDefinitions.hellfire_ammo
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

      val hellfire_ammo_box = AmmoBox(hellfire_ammo)
          hellfire_ammo_box.GUID = PlanetSideGUID(432)

      val fury = Vehicle(fury_def)
          fury.GUID = PlanetSideGUID(413)
          fury.Faction = PlanetSideEmpire.VS
          fury.Position = Vector3(3674.8438f, 2732f, 91.15625f)
          fury.Orientation = Vector3(0.0f, 0.0f, 90.0f)
          fury.WeaponControlledFromSeat(0).get.GUID = PlanetSideGUID(400)
          fury.WeaponControlledFromSeat(0).get.asInstanceOf[Tool].AmmoSlots.head.Box = hellfire_ammo_box

      fury.Definition.Packet.ConstructorData(fury).isSuccess mustEqual true
      ok //TODO write more of this test
    }

    "convert to packet (2)" in {
      val
      ams = Vehicle(GlobalDefinitions.ams)
      ams.GUID = PlanetSideGUID(413)
      ams.Utilities(1)().GUID = PlanetSideGUID(414)
      ams.Utilities(2)().GUID = PlanetSideGUID(415)
      ams.Utilities(3)().GUID = PlanetSideGUID(416)
      ams.Utilities(4)().GUID = PlanetSideGUID(417)

      ams.Definition.Packet.ConstructorData(ams).isSuccess mustEqual true
    }

    "convert to packet (3)" in {
      val
      ams = Vehicle(GlobalDefinitions.ams)
      ams.GUID = PlanetSideGUID(413)
      ams.Health = 0 //destroyed vehicle

      ams.Definition.Packet.ConstructorData(ams).isSuccess mustEqual true
      //did not initialize the utilities, but the converter did not fail
    }

    "convert to packet (4)" in {
      val
      router = Vehicle(GlobalDefinitions.router)
      router.GUID = PlanetSideGUID(413)
      router.Utility(UtilityType.teleportpad_terminal).get.GUID = PlanetSideGUID(1413)
      router.Utility(UtilityType.internal_router_telepad_deployable).get.GUID = PlanetSideGUID(2413)
      router.Definition.Packet.ConstructorData(router).isSuccess mustEqual true
    }
  }

  "DestroyedVehicle" should {
    "not convert a working vehicle" in {
      val ams = Vehicle(GlobalDefinitions.ams)
      ams.GUID = PlanetSideGUID(413)
      ams.Health mustEqual 3000 //not destroyed vehicle
      DestroyedVehicleConverter.converter.ConstructorData(ams).isFailure mustEqual true
    }

    "convert to packet" in {
      val ams = Vehicle(GlobalDefinitions.ams)
      ams.GUID = PlanetSideGUID(413)
      ams.Health = 0
      DestroyedVehicleConverter.converter.ConstructorData(ams).isSuccess mustEqual true
      //did not initialize the utilities, but the converter did not fail
    }

    "not convert into a detailed packet" in {
      val ams = Vehicle(GlobalDefinitions.ams)
      ams.GUID = PlanetSideGUID(413)
      ams.Health = 0
      DestroyedVehicleConverter.converter.DetailedConstructorData(ams).isFailure mustEqual true
    }
  }
}