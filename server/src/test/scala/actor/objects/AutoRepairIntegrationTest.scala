// Copyright (c) 2020 PSForever
package actor.objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.FreedContextActorTest
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.ballistics.{Projectile, SourceEntry}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.{ResourceSilo, ResourceSiloControl}
import net.psforever.objects.serverobject.structures.{AutoRepairStats, Building, StructureType}
import net.psforever.objects.serverobject.terminals.{OrderTerminalDefinition, Terminal, TerminalControl}
import net.psforever.objects.vehicles.VehicleControl
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Player, Tool, Vehicle}
import net.psforever.services.galaxy.GalaxyService
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types._

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

class AutoRepairFacilityIntegrationTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
  }
  val building = Building.Structure(StructureType.Facility)(name = "integ-fac-test-building", guid = 6, map_id = 0, zone, context)
  building.Invalidate()

  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairIntegrationTest.terminal_definition)
  val silo = new ResourceSilo()
  guid.register(player, number = 1)
  guid.register(weapon, number = 2)
  guid.register(weapon.AmmoSlot.Box, number = 3)
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)

  building.Amenities = silo
  building.Amenities = terminal
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  silo.NtuCapacitor = 1000
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = DamageInteraction(
    SourceEntry(terminal),
    ProjectileReason(
      DamageResolution.Hit,
      projectile,
      terminal.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.calculate()

  "AutoRepair" should {
    "should activate on damage and trade NTU from the facility's resource silo for repairs" in {
      assert(silo.NtuCapacitor == silo.MaxNtuCapacitor)
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 100) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      }
      assert(silo.NtuCapacitor < silo.MaxNtuCapacitor)
      assert(terminal.Health == terminal.MaxHealth)
    }
  }
}

class AutoRepairFacilityIntegrationGiveNtuTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
  }
  val building = Building.Structure(StructureType.Facility)(name = "integ-fac-test-building", guid = 6, map_id = 0, zone, context)
  building.Invalidate()

  val terminal = new Terminal(AutoRepairIntegrationTest.terminal_definition)
  val silo = new ResourceSilo()
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)

  building.Amenities = silo
  building.Amenities = terminal
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Health = 0
  terminal.Destroyed = true
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"

  "AutoRepair" should {
    "should activate and trade NTU frpom the silo only when NTU is made available" in {
      assert(silo.NtuCapacitor == 0)
      assert(terminal.Health == 0)
      assert(terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      silo.Actor ! ResourceSilo.UpdateChargeLevel(1000) //then ...

      avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 1000) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      }
      assert(silo.NtuCapacitor > 0 && silo.NtuCapacitor < silo.MaxNtuCapacitor)
      assert(terminal.Health == terminal.MaxHealth)
      assert(!terminal.Destroyed)
    }
  }
}

class AutoRepairFacilityIntegrationAntGiveNtuTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  var buildingMap = new TrieMap[Int, Building]()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val ant = Vehicle(GlobalDefinitions.ant)
  val terminal = new Terminal(AutoRepairIntegrationTest.slow_terminal_definition)
  val silo = new ResourceSilo()
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
    override def Vehicles = List(ant)
    override def Buildings = { buildingMap.toMap }
  }
  val building = new Building(
    name = "integ-fac-test-building",
    building_guid = 6,
    map_id = 0,
    zone,
    StructureType.Facility,
    GlobalDefinitions.cryo_facility
  )
  buildingMap += 6 -> building
  building.Actor = context.spawn(BuildingActor(zone, building), "integ-fac-test-building-control").toClassic
  building.Invalidate()

  guid.register(player, number = 1)
  guid.register(ant, number = 2)
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)

  val maxNtuCap = ant.Definition.MaxNtuCapacitor
  player.Spawn()
  ant.NtuCapacitor = maxNtuCap
  ant.Actor = context.actorOf(Props(classOf[VehicleControl], ant), name = "test-ant")
  ant.Zone = zone
  ant.Seats(0).mount(player)
  ant.DeploymentState = DriveState.Deployed
  building.Amenities = terminal
  building.Amenities = silo
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Health = 0
  terminal.Destroyed = true
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"

  "AutoRepair" should {
    "should activate and trade NTU from the silo only when NTU is made available from an ANT" in {
      assert(silo.NtuCapacitor == 0)
      assert(ant.NtuCapacitor == maxNtuCap)
      assert(terminal.Health == 0)
      assert(terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      silo.Actor ! CommonMessages.Use(player) //then ...

      avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 1000) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      }
      assert(silo.NtuCapacitor > 0 && silo.NtuCapacitor < silo.MaxNtuCapacitor)
      val ntuAfterRepairs = ant.NtuCapacitor
      assert(ntuAfterRepairs < maxNtuCap)
      assert(terminal.Health == terminal.MaxHealth)
      assert(!terminal.Destroyed)
      if(silo.NtuCapacitor < maxNtuCap) {
        var j = 0 //safety counter
        while(silo.NtuCapacitor < silo.MaxNtuCapacitor && j < 1000) {
          j += 1
          avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
        }
      }
      assert(silo.NtuCapacitor == silo.MaxNtuCapacitor)
      assert(ant.NtuCapacitor < ntuAfterRepairs)
      println(s"Test '${testNames.head}' successful.")
    }
  }
}

class AutoRepairFacilityIntegrationTerminalDestroyedTerminalAntTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  var buildingMap = new TrieMap[Int, Building]()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val ant = Vehicle(GlobalDefinitions.ant)
  val terminal = new Terminal(AutoRepairIntegrationTest.slow_terminal_definition)
  val silo = new ResourceSilo()
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
    override def Vehicles = List(ant)
    override def Buildings = { buildingMap.toMap }
  }
  val building = new Building(
    name = "integ-fac-test-building",
    building_guid = 6,
    map_id = 0,
    zone,
    StructureType.Facility,
    GlobalDefinitions.cryo_facility
  )
  buildingMap += 6 -> building
  building.Actor = context.spawn(BuildingActor(zone, building), "integ-fac-test-building-control").toClassic
  building.Invalidate()

  guid.register(player, number = 1)
  guid.register(ant, number = 2)
  guid.register(weapon, number = 3)
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)
  guid.register(weapon.AmmoSlot.Box, number = 7)

  val maxNtuCap = ant.Definition.MaxNtuCapacitor
  player.Spawn()
  ant.NtuCapacitor = maxNtuCap
  ant.Actor = context.actorOf(Props(classOf[VehicleControl], ant), name = "test-ant")
  ant.Zone = zone
  ant.Seats(0).mount(player)
  ant.DeploymentState = DriveState.Deployed
  building.Amenities = terminal
  building.Amenities = silo
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Health = 1 //not yet destroyed, but one shot away from it
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = DamageInteraction(
    SourceEntry(terminal),
    ProjectileReason(
      DamageResolution.Hit,
      projectile,
      terminal.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.calculate()

  "AutoRepair" should {
    "should activate upon destruction and trade NTU from the silo only when NTU is made available from an ANT" in {
      assert(silo.NtuCapacitor == 0)
      assert(ant.NtuCapacitor == maxNtuCap)
      assert(!terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      terminal.Actor ! Vitality.Damage(applyDamageTo)
      while(avatarProbe.receiveOne(max = 1000 milliseconds) != null) { /* health loss event(s) + state updates */ }
      assert(terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      silo.Actor ! CommonMessages.Use(player) //then ...

      avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 1000) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      }
      assert(silo.NtuCapacitor > 0 && silo.NtuCapacitor <= silo.MaxNtuCapacitor)
      assert(ant.NtuCapacitor < maxNtuCap)
      assert(terminal.Health == terminal.MaxHealth)
      assert(!terminal.Destroyed)
      println(s"Test '${testNames.head}' successful.")
    }
  }
}

class AutoRepairFacilityIntegrationTerminalIncompleteRepairTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  var buildingMap = new TrieMap[Int, Building]()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val ant = Vehicle(GlobalDefinitions.ant)
  val terminal = new Terminal(AutoRepairIntegrationTest.slow_terminal_definition)
  val silo = new ResourceSilo()
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
    override def Vehicles = List(ant)
    override def Buildings = { buildingMap.toMap }
  }
  val building = new Building(
    name = "integ-fac-test-building",
    building_guid = 6,
    map_id = 0,
    zone,
    StructureType.Facility,
    GlobalDefinitions.cryo_facility
  )
  buildingMap += 6 -> building
  building.Actor = context.spawn(BuildingActor(zone, building), "integ-fac-test-building-control").toClassic
  building.Invalidate()

  guid.register(player, number = 1)
  guid.register(ant, number = 2)
  guid.register(weapon, number = 3)
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)
  guid.register(weapon.AmmoSlot.Box, number = 7)

  val maxNtuCap = ant.Definition.MaxNtuCapacitor
  player.Spawn()
  ant.NtuCapacitor = maxNtuCap
  ant.Actor = context.actorOf(Props(classOf[VehicleControl], ant), name = "test-ant")
  ant.Zone = zone
  ant.Seats(0).mount(player)
  ant.DeploymentState = DriveState.Deployed
  building.Amenities = terminal
  building.Amenities = silo
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Health = 1 //not yet destroyed, but one shot away from it
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = DamageInteraction(
    SourceEntry(terminal),
    ProjectileReason(
      DamageResolution.Hit,
      projectile,
      terminal.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.calculate()

  "AutoRepair" should {
    "should activate and trade NTU from the silo; if the ANT stops depositing, auto-repair continues" in {
      assert(silo.NtuCapacitor == 0)
      assert(ant.NtuCapacitor == maxNtuCap)
      assert(!terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      terminal.Actor ! Vitality.Damage(applyDamageTo)
      while(avatarProbe.receiveOne(max = 1000 milliseconds) != null) { /* health loss event(s) + state updates */ }
      assert(terminal.Destroyed)
      avatarProbe.expectNoMessage(max = 1000 milliseconds) //nothing
      silo.Actor ! CommonMessages.Use(player) //then ...

      avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 10) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //some health update events ...
      }
      ant.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      ant.Actor ! Deployment.TryUndeploy(DriveState.Mobile)
      while( avatarProbe.receiveOne(max = 1000 milliseconds) != null ) { /* remainder of the messages */ }
      val siloCapacitor = silo.NtuCapacitor
      val antCapacitor = ant.NtuCapacitor
      val termHealth = terminal.Health
      assert(ant.DeploymentState == DriveState.Mobile)
      assert(siloCapacitor > 0 && siloCapacitor < silo.MaxNtuCapacitor)
      assert(antCapacitor > 0 && antCapacitor < maxNtuCap)
      assert(termHealth > 0 && termHealth < terminal.MaxHealth)
      while(terminal.Health < terminal.MaxHealth && i < 20) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //some health update events ...
      }
      //while( avatarProbe.receiveOne(max = 1000 milliseconds) != null ) { /* remainder of the messages */ }
      assert(siloCapacitor != silo.NtuCapacitor) //changing ...
      assert(antCapacitor == ant.NtuCapacitor) //not supplying anymore
      assert(terminal.Health > termHealth && terminal.Health <= terminal.MaxHealth) //still auto-repairing
      println(s"Test '${testNames.head}' successful.")
    }
  }
}

class AutoRepairTowerIntegrationTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val avatarProbe = new TestProbe(system)
  val catchall = new TestProbe(system).ref
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def AvatarEvents = avatarProbe.ref
    override def LocalEvents = catchall
    override def VehicleEvents = catchall
    override def Activity = catchall
  }
  val building = Building.Structure(StructureType.Tower)(name = "integ-twr-test-building", guid = 6, map_id = 0, zone, context)
  building.Invalidate()

  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairIntegrationTest.terminal_definition)
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  guid.register(player, number = 1)
  guid.register(weapon, number = 2)
  guid.register(weapon.AmmoSlot.Box, number = 3)
  guid.register(terminal, number = 4)
  guid.register(building, number = 6)

  building.Amenities = terminal
  building.Actor ! BuildingActor.SuppliedWithNtu() //artificial
  building.Actor ! BuildingActor.PowerOn() //artificial

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = DamageInteraction(
    SourceEntry(terminal),
    ProjectileReason(
      DamageResolution.Hit,
      projectile,
      terminal.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.calculate()

  "AutoRepair" should {
    "should activate on damage and trade NTU from the tower for repairs" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 500 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0 //safety counter
      while(terminal.Health < terminal.MaxHealth && i < 100) {
        i += 1
        avatarProbe.receiveOne(max = 1000 milliseconds) //health update event
      }
      assert(terminal.Health == terminal.MaxHealth)
    }
  }
}

object AutoRepairIntegrationTest {
  val terminal_definition = new OrderTerminalDefinition(objId = 612) {
    Name = "order_terminal"
    MaxHealth = 500
    Damageable = true
    Repairable = true
    autoRepair = AutoRepairStats(200, 500, 500, 1)
    RepairIfDestroyed = true
  }

  val slow_terminal_definition = new OrderTerminalDefinition(objId = 612) {
    Name = "order_terminal"
    MaxHealth = 500
    Damageable = true
    Repairable = true
    autoRepair = AutoRepairStats(5, 500, 500, 1)
    RepairIfDestroyed = true
  }
}
