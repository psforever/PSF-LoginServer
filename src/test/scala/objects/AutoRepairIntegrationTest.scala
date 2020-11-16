// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.FreedContextActorTest
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.ballistics.{Projectile, ProjectileResolution, ResolvedProjectile, SourceEntry}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.resourcesilo.{ResourceSilo, ResourceSiloControl}
import net.psforever.objects.serverobject.structures.{AutoRepairStats, Building, StructureType}
import net.psforever.objects.serverobject.terminals.{OrderTerminalDefinition, Terminal, TerminalControl}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.services.galaxy.GalaxyService
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}

import scala.concurrent.duration._

class AutoRepairFacilityIntegrationTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(200 milliseconds)

  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairIntegrationTest.terminal_definition)
  val silo = new ResourceSilo()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = new TestProbe(system)
  zone.AvatarEvents = avatarProbe.ref

  guid.register(player, number = 1)
  guid.register(weapon, number = 2)
  guid.register(weapon.AmmoSlot.Box, number = 3)
  guid.register(terminal, number = 4)
  guid.register(silo, number = 5)

  val building = Building.Structure(StructureType.Facility)(name = "test-building", guid = 6, map_id = 0, zone, context)
  building.Invalidate()
  guid.register(building, number = 6)
  building.Amenities = silo
  building.Amenities = terminal

  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")

  silo.NtuCapacitor = 1000
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! "startup"
  building.Actor ! BuildingActor.PowerOn() //artificial

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    projectile,
    SourceEntry(terminal),
    terminal.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)

  "AutoRepair" should {
    "should activate on damage and trade NTU from the facility's resource silo for repairs" in {
      assert(silo.NtuCapacitor == silo.MaxNtuCapacitor)
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
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

class AutoRepairTowerIntegrationTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  system.spawn(InterstellarClusterService(Nil), InterstellarClusterService.InterstellarClusterServiceKey.id)
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(200 milliseconds)

  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairIntegrationTest.terminal_definition)
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = new TestProbe(system)
  zone.AvatarEvents = avatarProbe.ref

  guid.register(player, number = 1)
  guid.register(weapon, number = 2)
  guid.register(weapon.AmmoSlot.Box, number = 3)
  guid.register(terminal, number = 4)

  val building = Building.Structure(StructureType.Tower)(name = "test-building", guid = 6, map_id = 0, zone, context)
  building.Invalidate()
  guid.register(building, number = 6)
  building.Amenities = terminal

  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")

  val wep_fmode  = weapon.FireMode
  val wep_prof   = wep_fmode.Add
  val proj       = weapon.Projectile
  val proj_prof  = proj.asInstanceOf[DamageProfile]
  val projectile = Projectile(proj, weapon.Definition, wep_fmode, player, Vector3(2, 0, 0), Vector3.Zero)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    projectile,
    SourceEntry(terminal),
    terminal.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)

  "AutoRepair" should {
    "should activate on damage and trade NTU from the tower for repairs" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
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
}
