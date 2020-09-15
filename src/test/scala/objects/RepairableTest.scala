// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.generator.{Generator, GeneratorControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl}
import net.psforever.objects.serverobject.turret.{FacilityTurret, FacilityTurretControl}
import net.psforever.objects.vehicles.VehicleControl
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

/*
the generator is used to test basic entity repair
essentially, treat it more as a generic entity whose object type is repairable
see GeneratorTest in relation to what the generator does above and beyond that during repair
 */
class RepairableEntityRepairTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen      = Generator(GlobalDefinitions.generator)                        //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref
  val tool = Tool(GlobalDefinitions.nano_dispenser) //4 & 5
  guid.register(tool, 4)
  guid.register(tool.AmmoSlot.Box, 5)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "RepairableEntity" should {
    "handle repairs" in {
      assert(gen.Health == gen.Definition.DefaultHealth) //ideal
      val originalHealth = gen.Health -= 50
      assert(gen.Health < gen.Definition.DefaultHealth)   //damage
      gen.Actor ! CommonMessages.Use(player1, Some(tool)) //repair

      val msg123 = avatarProbe.receiveN(3, 500 milliseconds)
      assert(
        msg123.head match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction
                  .SendResponse(PlanetSideGUID(0), InventoryStateMessage(PlanetSideGUID(5), _, PlanetSideGUID(4), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg123(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg123(2) match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction.SendResponse(PlanetSideGUID(0), RepairMessage(PlanetSideGUID(2), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(originalHealth < gen.Health) //generator repaired a bit
    }
  }
}

class RepairableEntityNotRepairTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen      = Generator(GlobalDefinitions.generator)                        //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref
  val tool = Tool(GlobalDefinitions.nano_dispenser) //4 & 5
  guid.register(tool, 4)
  guid.register(tool.AmmoSlot.Box, 5)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "RepairableEntity" should {
    "not repair if health is already full" in {
      assert(gen.Health == gen.Definition.DefaultHealth)  //ideal
      gen.Actor ! CommonMessages.Use(player1, Some(tool)) //repair?
      avatarProbe.expectNoMessage(1000 milliseconds)      //no messages
    }
  }
}

class RepairableAmenityTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val term     = Terminal(GlobalDefinitions.order_terminal)                    //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  guid.register(building, 1)
  guid.register(term, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = term
  term.Position = Vector3(1, 0, 0)
  term.Actor = system.actorOf(Props(classOf[TerminalControl], term), "terminal-control")
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref

  val tool = Tool(GlobalDefinitions.nano_dispenser) //4 & 5
  guid.register(tool, 4)
  guid.register(tool.AmmoSlot.Box, 5)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "RepairableAmenity" should {
    "send initialization messages upon restoration" in {
      //the decimator does enough damage to one-shot this terminal from any initial health
      val originalHealth = term.Health = term.Definition.RepairRestoresAt - 1 //initial state manip
      term.Destroyed = true
      assert(originalHealth < term.Definition.RepairRestoresAt)
      assert(term.Destroyed)

      term.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg12345 = avatarProbe.receiveN(5, 500 milliseconds)
      assert(
        msg12345.head match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction
                  .SendResponse(PlanetSideGUID(0), InventoryStateMessage(PlanetSideGUID(5), _, PlanetSideGUID(4), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg12345(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg12345(2) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 50, 0)) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg12345(3) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 51, 0)) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg12345(4) match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction.SendResponse(PlanetSideGUID(0), RepairMessage(PlanetSideGUID(2), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(term.Health > term.Definition.RepairRestoresAt)
      assert(!term.Destroyed)
    }
  }
}

class RepairableTurretWeapon extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val building      = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref
  building.Actor = buildingProbe.ref

  val turret = new FacilityTurret(GlobalDefinitions.manned_turret) //2, 5, 6
  turret.Actor = system.actorOf(Props(classOf[FacilityTurretControl], turret), "turret-control")
  turret.Zone = zone
  turret.Position = Vector3(1, 0, 0)
  val turretWeapon = turret.Weapons.values.head.Equipment.get.asInstanceOf[Tool]

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref

  guid.register(building, 1)
  guid.register(turret, 2)
  guid.register(player1, 3)
  guid.register(turretWeapon, 5)
  guid.register(turretWeapon.AmmoSlot.Box, 6)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = turret

  val tool = Tool(GlobalDefinitions.nano_dispenser) //7 & 8
  guid.register(tool, 7)
  guid.register(tool.AmmoSlot.Box, 8)

  "RepairableTurretWeapon" should {
    "handle repairs and restoration" in {
      turret.Health = turret.Definition.RepairRestoresAt - 1 //initial state manip
      turret.Destroyed = true                                //initial state manip
      assert(turret.Health < turret.Definition.RepairRestoresAt)
      assert(turret.Destroyed)

      turret.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg12345 = avatarProbe.receiveN(5, 500 milliseconds)
      val msg4     = vehicleProbe.receiveOne(500 milliseconds)
      assert(
        msg12345.head match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction
                  .SendResponse(PlanetSideGUID(0), InventoryStateMessage(PlanetSideGUID(8), _, PlanetSideGUID(7), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg12345(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      //msg12345(2) and msg12345(3) are related to RepairableAmenity
      assert(
        msg12345(4) match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction.SendResponse(PlanetSideGUID(0), RepairMessage(PlanetSideGUID(2), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg4 match {
          case VehicleServiceMessage("test", VehicleAction.EquipmentInSlot(_, PlanetSideGUID(2), 1, t))
              if t eq turretWeapon =>
            true
          case _ => false
        }
      )
      assert(turret.Health > turret.Definition.RepairRestoresAt)
      assert(!turret.Destroyed)
    }
  }
}

class RepairableVehicleRepair extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadassault) //guid=1, 2, 3
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "vehicle-control")
  atv.Position = Vector3(1, 0, 0)
  val atvWeapon = atv.Weapons(1).Equipment.get.asInstanceOf[Tool]

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref

  guid.register(atv, 1)
  guid.register(atvWeapon, 2)
  guid.register(atvWeapon.AmmoSlot.Box, 3)
  guid.register(player1, 4)
  atv.Zone = zone

  val tool = Tool(GlobalDefinitions.nano_dispenser) //5 & 6
  guid.register(tool, 5)
  guid.register(tool.AmmoSlot.Box, 6)

  "RepairableVehicle" should {
    "handle repairs" in {
      val originalHealth = atv.Health = atv.Definition.DamageDestroysAt + 1 //initial state manip
      assert(atv.Health == originalHealth)

      atv.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg123 = avatarProbe.receiveN(3, 500 milliseconds)
      assert(
        msg123.head match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction
                  .SendResponse(PlanetSideGUID(0), InventoryStateMessage(PlanetSideGUID(6), _, PlanetSideGUID(5), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg123(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg123(2) match {
          case AvatarServiceMessage(
                "TestCharacter1",
                AvatarAction.SendResponse(PlanetSideGUID(0), RepairMessage(PlanetSideGUID(1), _))
              ) =>
            true
          case _ => false
        }
      )
      assert(atv.Health > originalHealth)
    }
  }
}

class RepairableVehicleRestoration extends ActorTest {
  /*
  no messages are dispatched, in this case, because most vehicles are flagged to not be repairable if destroyed
   */
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadassault) //guid=1, 2, 3
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "vehicle-control")
  atv.Position = Vector3(1, 0, 0)
  val atvWeapon = atv.Weapons(1).Equipment.get.asInstanceOf[Tool]

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref

  guid.register(atv, 1)
  guid.register(atvWeapon, 2)
  guid.register(atvWeapon.AmmoSlot.Box, 3)
  guid.register(player1, 4)
  atv.Zone = zone

  val tool = Tool(GlobalDefinitions.nano_dispenser) //5 & 6
  guid.register(tool, 5)
  guid.register(tool.AmmoSlot.Box, 6)

  "RepairableVehicle" should {
    "will not restore a destroyed vehicle to working order" in {
      atv.Health = atv.Definition.DamageDestroysAt - 1 //initial state manip
      atv.Destroyed = true                             //initial state manip
      assert(atv.Health <= atv.Definition.DamageDestroysAt)
      assert(atv.Destroyed)

      atv.Actor ! CommonMessages.Use(player1, Some(tool))
      avatarProbe.expectNoMessage(500 milliseconds)
      assert(atv.Health == 0) //set to zero explicitly
      assert(atv.Destroyed)
    }
  }
}

object RepairableTest {}
