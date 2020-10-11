// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.FreedContextActorTest
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.ballistics.{Projectile, ProjectileResolution, ResolvedProjectile, SourceEntry}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.structures.{AutoRepairStats, Building, StructureType}
import net.psforever.objects.serverobject.terminals.{OrderTerminalDefinition, Terminal, TerminalControl}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.services.ServiceManager
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}

import scala.concurrent.duration._

class AutoRepairRequestNtuTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "asks owning building for NTU after damage" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      val buildingMsg = buildingProbe.receiveOne(max = 600 milliseconds)
      assert(buildingMsg match {
        case BuildingActor.Ntu(NtuCommand.Request(drain, _)) =>
          drain == terminal.Definition.autoRepair.get.drain
        case _ =>
          false
      })
    }
  }
}

class AutoRepairRequestNtuRepeatTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "repeatedly asks owning building for NTU after damage" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      (0 to 3).foreach { _ =>
        val buildingMsg = buildingProbe.receiveOne(max = 1000 milliseconds)
        assert(buildingMsg match {
          case BuildingActor.Ntu(NtuCommand.Request(drain, _)) =>
            drain == terminal.Definition.autoRepair.get.drain
          case _ =>
            false
        })
      }
    }
  }
}

class AutoRepairNoRequestNtuTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "not ask for NTU after damage if it expects no NTU" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! BuildingActor.NtuDepleted()
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      buildingProbe.expectNoMessage(max = 2000 milliseconds)
    }
  }
}

class AutoRepairRestoreRequestNtuTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "ask for NTU after damage if its expectation of NTU is restored" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! BuildingActor.NtuDepleted()
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      buildingProbe.expectNoMessage(max = 2000 milliseconds)

      terminal.Actor ! BuildingActor.SuppliedWithNtu()
      val buildingMsg = buildingProbe.receiveOne(max = 600 milliseconds)
      assert(buildingMsg match {
        case BuildingActor.Ntu(NtuCommand.Request(drain, _)) =>
          drain == terminal.Definition.autoRepair.get.drain
        case _ =>
          false
      })
    }
  }
}

class AutoRepairRepairWithNtuTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "repair some of the damage when it receives NTU" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! BuildingActor.NtuDepleted() //don't worry about requests
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      val reducedHealth = terminal.Health
      buildingProbe.expectNoMessage(max = 2000 milliseconds)
      terminal.Actor ! NtuCommand.Grant(null, 1)
      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health > reducedHealth)
    }
  }
}

class AutoRepairRepairWithNtuUntilDoneTest extends FreedContextActorTest {
  ServiceManager.boot
  val player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val weapon = new Tool(GlobalDefinitions.suppressor)
  val terminal = new Terminal(AutoRepairTest.terminal_definition)
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

  val building = Building("test-building", 1, 1, zone, StructureType.Facility)
  building.Invalidate()
  guid.register(building, number = 6)
  val buildingProbe = new TestProbe(system)
  building.Actor = buildingProbe.ref
  building.Zone = zone
  terminal.Actor = context.actorOf(Props(classOf[TerminalControl], terminal), name = "test-terminal")
  terminal.Owner = building

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
    "ask for NTU after damage and repair some of the damage when it receives NTU, until fully-repaired" in {
      assert(terminal.Health == terminal.MaxHealth)
      terminal.Actor ! Vitality.Damage(applyDamageTo)

      avatarProbe.receiveOne(max = 200 milliseconds) //health update event
      assert(terminal.Health < terminal.MaxHealth)
      var i = 0
      while(terminal.Health < terminal.MaxHealth && i < 100) {
        i += 1 //safety counter
        val buildingMsg = buildingProbe.receiveOne(max = 1000 milliseconds)
        buildingMsg match {
          case BuildingActor.Ntu(NtuCommand.Request(_, _)) =>
            terminal.Actor ! NtuCommand.Grant(null, 1)
          case _ => ;
        }
      }
      assert(terminal.Health == terminal.MaxHealth)
    }
  }
}

object AutoRepairTest {
  val terminal_definition = new OrderTerminalDefinition(objId = 612) {
    Name = "order_terminal"
    MaxHealth = 500
    Damageable = true
    Repairable = true
    autoRepair = AutoRepairStats(1, 500, 500, 1)
    RepairIfDestroyed = true
  }
}
