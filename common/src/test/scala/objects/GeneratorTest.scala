// Copyright (c) 2020 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.ballistics._
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Tool}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.generator.{Generator, GeneratorControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types._
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class GeneratorTest extends Specification {
  "Generator" should {
    "construct" in {
      Generator(GlobalDefinitions.generator)
      ok
    }

    "start in 'Normal' condition" in {
      val obj = Generator(GlobalDefinitions.generator)
      obj.Condition mustEqual PlanetSideGeneratorState.Normal
    }
  }
}

class GeneratorControlConstructTest extends ActorTest {
  "GeneratorControl" should {
    "construct" in {
      val gen = Generator(GlobalDefinitions.generator)
      gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "gen-control")
      assert(gen.Actor != ActorRef.noSender)
    }
  }
}

class GeneratorControlCriticalTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(5))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = { }
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref

  val gen = Generator(GlobalDefinitions.generator) //guid=2
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Position = Vector3(14, 0, 0) //<14m from generator; dies
  player1.Spawn

  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  val buildingProbe = TestProbe()
  building.Actor = buildingProbe.ref

  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)

  val weapon = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, PlayerSource(player1), 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "manage damage control until generator critical state" in {
      gen.Health = 1 //no matter what, the next shot pushes it to critical status
      assert(gen.Health == 1)
      assert(!gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Normal)

      gen.Actor ! Vitality.Damage(applyDamageTo)
      avatarProbe.receiveOne( 500 milliseconds)
      val msg = buildingProbe.receiveOne(500 milliseconds)
      assert(
        msg match {
          case Building.AmenityStateChange(o) => o eq gen
          case _ => false
        }
      )
      assert(gen.Health == 0)
      assert(gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Critical)
    }
  }
}

class GeneratorControlDestroyedTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(5))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = { }
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref

  val gen = Generator(GlobalDefinitions.generator) //guid=2
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Position = Vector3(14, 0, 0) //<14m from generator; dies
  player1.Spawn

  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  val buildingProbe = TestProbe()
  building.Actor = buildingProbe.ref

  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)

  val weapon = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, PlayerSource(player1), 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "manage damage control through critical state until destroyed" in {
      gen.Health = 1 //no matter what, the next shot pushes it to critical status
      assert(gen.Health == 1)
      assert(!gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Normal)

      gen.Actor ! Vitality.Damage(applyDamageTo)
      avatarProbe.receiveN(2, 500 milliseconds) //see DamageableEntity test file
      val msg1 = buildingProbe.receiveOne(500 milliseconds)
      assert(
        msg1 match {
          case Building.AmenityStateChange(o) => o eq gen
          case _ => false
        }
      )
      assert(gen.Health == 0)
      assert(gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Critical)

      expectNoMsg(9 seconds)
      avatarProbe.expectNoMsg(50 milliseconds) //no prior messages
      buildingProbe.expectNoMsg(50 milliseconds) //no prior messages
      val msg2 = buildingProbe.receiveOne(1000 milliseconds)
      assert(
        msg2 match {
          case Building.AmenityStateChange(o) => o eq gen
          case _ => false
        }
      )
      assert(gen.Health == 0)
      assert(gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Destroyed)
    }
  }
}

class GeneratorControlKillsTest extends ActorTest {
  /*
  to perform this test, players need to be added to the SOI organization of the test base in proximity of the generator
  under normal player scenario, this is an automatic process
  extending from the act of players being in a zone
  and players being within the SOI radius from the center of a facility on a periodic check
  the test base being used has no established SOI region or automatic SOI check refresh,
  but its SOI information can be loaded with the players manually
  the players need something to catch the die message
   */
  val guid = new NumberPoolHub(new LimitedNumberSource(5))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = { }
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref

  val gen = Generator(GlobalDefinitions.generator) //guid=2
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Position = Vector3(14, 0, 0) //<14m from generator; dies
  player1.Spawn
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Female, 1, CharacterVoice.Mute)) //guid=4
  player2.Position = Vector3(15, 0, 0) //<14m from generator; lives
  player2.Spawn
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref

  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  building.PlayersInSOI = List(player1, player2)
  val buildingProbe = TestProbe()
  building.Actor = buildingProbe.ref

  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)

  val weapon = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, PlayerSource(player1), 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "kill players when the generator is destroyed" in {
      gen.Health = 1 //no matter what, the next shot pushes it to critical status
      assert(gen.Health == 1)
      assert(!gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Normal)

      gen.Actor ! Vitality.Damage(applyDamageTo)
      buildingProbe.receiveOne(1000 milliseconds)
      assert(gen.Health == 0)
      assert(gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Critical)

      expectNoMsg(9 seconds)
      buildingProbe.expectNoMsg(100 milliseconds) //no prior messages
      buildingProbe.receiveOne(1000 milliseconds)
      assert(gen.Health == 0)
      assert(gen.Destroyed)
      assert(gen.Condition == PlanetSideGeneratorState.Destroyed)
      val msg = player1Probe.receiveOne(100 milliseconds)
      assert(
        msg match {
          case _ @ Player.Die() => true
          case _ => false
        }
      )
      player2Probe.expectNoMsg(200 milliseconds)
    }
  }
}

class GeneratorControlNotDestroyTwice extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen = Generator(GlobalDefinitions.generator) //guid=2
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref

  val weapon = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, PlayerSource(player1), 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "not send a status update if destroyed and partially repaired, but destroyed again" in {
      val originalHealth = gen.Health = gen.Definition.DamageDestroysAt + 1 //damaged, not yet restored
      gen.Condition = PlanetSideGeneratorState.Destroyed //initial state manip
      gen.Destroyed = true
      assert(gen.Destroyed)
      assert(originalHealth < gen.Definition.DefaultHealth)
      assert(originalHealth < gen.Definition.RepairRestoresAt)
      assert(originalHealth > gen.Definition.DamageDestroysAt)

      gen.Actor ! Vitality.Damage(applyDamageTo)
      avatarProbe.receiveOne(500 milliseconds)
      activityProbe.receiveOne(500 milliseconds)
      buildingProbe.expectNoMsg(1000 milliseconds)
      assert(gen.Health < originalHealth)
      assert(gen.Destroyed)
      assert(originalHealth < gen.Definition.DefaultHealth)
      assert(originalHealth < gen.Definition.RepairRestoresAt)
      assert(gen.Health <= gen.Definition.DamageDestroysAt)
    }
  }
}

class GeneratorControlNotRepairCriticalTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen = Generator(GlobalDefinitions.generator) //guid=2
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref
  val tool = Tool(GlobalDefinitions.nano_dispenser) //4 & 5
  guid.register(tool, 4)
  guid.register(tool.AmmoSlot.Box, 5)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "not repair if the generator is critical" in {
      assert(gen.Health == gen.Definition.DefaultHealth) //ideal
      val originalHealth = gen.Health -= 50 //damage
      gen.Condition = PlanetSideGeneratorState.Critical //initial state manip

      gen.Actor ! CommonMessages.Use(player1, Some(tool)) //repair?
      avatarProbe.expectNoMsg(1000 milliseconds) //no messages
      assert(gen.Health == originalHealth)

      gen.Condition = PlanetSideGeneratorState.Normal //restore
      gen.Actor ! CommonMessages.Use(player1, Some(tool)) //repair?
      avatarProbe.receiveN(3, 500 milliseconds) //expected
      assert(gen.Health > originalHealth)
    }
  }
}

class GeneratorControlRepairPastRestorePoint extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen = Generator(GlobalDefinitions.generator) //guid=2
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  guid.register(building, 1)
  guid.register(gen, 2)
  guid.register(player1, 3)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = gen
  gen.Position = Vector3(1, 0, 0)
  gen.Actor = system.actorOf(Props(classOf[GeneratorControl], gen), "generator-control")
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref
  val tool = Tool(GlobalDefinitions.nano_dispenser) //4 & 5
  guid.register(tool, 4)
  guid.register(tool.AmmoSlot.Box, 5)
  expectNoMsg(200 milliseconds)
  //we're not testing that the math is correct

  "GeneratorControl" should {
    "send a status update if destroyed and repairing past the restoration point" in {
      val originalHealth = gen.Health = gen.Definition.RepairRestoresAt - 1 //damage
      gen.Condition = PlanetSideGeneratorState.Destroyed //initial state manip
      gen.Destroyed = true
      assert(originalHealth < gen.Definition.DefaultHealth)
      assert(originalHealth < gen.Definition.RepairRestoresAt)
      assert(gen.Destroyed)

      gen.Actor ! CommonMessages.Use(player1, Some(tool)) //repair
      avatarProbe.receiveN(3, 500 milliseconds) //expected
      val msg = buildingProbe.receiveOne(200 milliseconds)
      assert(
        msg match {
          case Building.AmenityStateChange(o) => o eq gen
          case _ => false
        }
      )
      assert(gen.Condition == PlanetSideGeneratorState.Normal)
      assert(gen.Health > gen.Definition.RepairRestoresAt)
      assert(!gen.Destroyed)
    }
  }
}
