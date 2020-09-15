// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.ballistics._
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.generator.{Generator, GeneratorControl}
import net.psforever.objects.serverobject.implantmech.{ImplantTerminalMech, ImplantTerminalMechControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl, TerminalDefinition}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.{FacilityTurret, FacilityTurretControl, TurretUpgrade}
import net.psforever.objects.vehicles.VehicleControl
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.DamageWithPositionMessage
import net.psforever.types._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.support.SupportActor
import net.psforever.services.vehicle.support.TurretUpgrader
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import org.specs2.mutable.Specification
import scala.concurrent.duration._
import net.psforever.objects.avatar.Avatar

class DamageableTest extends Specification {
  val player1     = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val pSource     = PlayerSource(player1)
  val weaponA     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectileA = weaponA.Projectile

  "Damageable" should {
    "permit damage" in {
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileA, weaponA.Definition, weaponA.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual true
    }

    "ignore attempts at non-zero damage" in {
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(
          projectileA,
          weaponA.Definition,
          weaponA.FireMode,
          PlayerSource(player1),
          0,
          Vector3.Zero,
          Vector3.Zero
        ),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      Damageable.CanDamage(target, 0, resolved) mustEqual false
    }

    "ignore attempts at damaging friendly targets not designated for friendly fire" in {
      val target = new Generator(GlobalDefinitions.generator)
      target.Owner =
        new Building("test-building", 0, 0, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building) {
          Faction = player1.Faction
        }
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileA, weaponA.Definition, weaponA.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      target.Definition.DamageableByFriendlyFire mustEqual false
      target.Faction == player1.Faction mustEqual true
      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual false

      target.Owner.Faction = PlanetSideEmpire.NC
      target.Faction != player1.Faction mustEqual true
      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual true
    }

    "ignore attempts at damaging a target that is not damageable" in {
      val target = new SpawnTube(GlobalDefinitions.respawn_tube_sanctuary)
      target.Owner =
        new Building("test-building", 0, 0, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building) {
          Faction = PlanetSideEmpire.NC
        }
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileA, weaponA.Definition, weaponA.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      target.Definition.Damageable mustEqual false
      target.Faction != player1.Faction mustEqual true
      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual false
    }

    "permit damaging friendly targets, even those not designated for friendly fire, if the target is hacked" in {
      val player2 =
        Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute))
      player2.GUID = PlanetSideGUID(1)
      val target = new Terminal(new TerminalDefinition(0) {
        Damageable = true
        DamageableByFriendlyFire = false

        override def Request(player: Player, msg: Any): Terminal.Exchange = null
      })
      target.Owner =
        new Building("test-building", 0, 0, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building) {
          Faction = player1.Faction
        }
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileA, weaponA.Definition, weaponA.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      target.Definition.DamageableByFriendlyFire mustEqual false
      target.Faction == player1.Faction mustEqual true
      target.HackedBy.isEmpty mustEqual true
      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual false

      target.HackedBy = player2
      target.Faction == player1.Faction mustEqual true
      target.HackedBy.nonEmpty mustEqual true
      Damageable.CanDamage(target, projectileA.Damage0, resolved) mustEqual true
    }

    val weaponB     = Tool(GlobalDefinitions.jammer_grenade)
    val projectileB = weaponB.Projectile

    "permit jamming" in {
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileB, weaponB.Definition, weaponB.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      resolved.projectile.profile.JammerProjectile mustEqual true
      Damageable.CanJammer(target, resolved) mustEqual true
    }

    "ignore attempts at jamming if the projectile is does not cause the effect" in {
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileA, weaponA.Definition, weaponA.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      ) //decimator

      resolved.projectile.profile.JammerProjectile mustEqual false
      Damageable.CanJammer(target, resolved) mustEqual false
    }

    "ignore attempts at jamming friendly targets" in {
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      target.Faction = player1.Faction
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileB, weaponB.Definition, weaponB.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      resolved.projectile.profile.JammerProjectile mustEqual true
      resolved.projectile.owner.Faction == target.Faction mustEqual true
      Damageable.CanJammer(target, resolved) mustEqual false
    }

    "ignore attempts at jamming targets that are not jammable" in {
      val target = new TrapDeployable(GlobalDefinitions.tank_traps)
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileB, weaponB.Definition, weaponB.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      resolved.projectile.profile.JammerProjectile mustEqual true
      resolved.projectile.owner.Faction == target.Faction mustEqual false
      target.isInstanceOf[JammableUnit] mustEqual false
      Damageable.CanJammer(target, resolved) mustEqual false
    }

    "permit jamming friendly targets if the target is hacked" in {
      val player2 =
        Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute))
      player2.GUID = PlanetSideGUID(1)
      val target = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      target.Faction = player1.Faction
      val resolved = ResolvedProjectile(
        ProjectileResolution.Hit,
        Projectile(projectileB, weaponB.Definition, weaponB.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )

      resolved.projectile.profile.JammerProjectile mustEqual true
      resolved.projectile.owner.Faction == target.Faction mustEqual true
      target.isInstanceOf[JammableUnit] mustEqual true
      target.HackedBy.nonEmpty mustEqual false
      Damageable.CanJammer(target, resolved) mustEqual false

      target.HackedBy = player2
      target.HackedBy.nonEmpty mustEqual true
      Damageable.CanJammer(target, resolved) mustEqual true
    }
  }
}

/*
the damage targets, Generator, Terminal, etc., are used to test basic destruction
essentially, treat them more as generic entities whose object types are damageable (because they are)
see specific object type tests in relation to what those object types does above and beyond that during damage
 */
class DamageableEntityDamageTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(5))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val gen      = Generator(GlobalDefinitions.generator)                        //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
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

  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)

  "DamageableEntity" should {
    "handle taking damage" in {
      gen.Actor ! Vitality.Damage(applyDamageTo)
      val msg1 = avatarProbe.receiveOne(500 milliseconds)
      val msg2 = activityProbe.receiveOne(500 milliseconds)
      assert(
        msg1 match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg2 match {
          case activity: Zone.HotSpot.Activity =>
            activity.attacker == PlayerSource(player1) &&
              activity.defender == SourceEntry(gen) &&
              activity.location == Vector3(1, 0, 0)
          case _ => false
        }
      )
    }
  }
}

class DamageableEntityDestroyedTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(5))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref
  val mech = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech) //guid=2
  mech.Position = Vector3(1, 0, 0)
  mech.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], mech), "mech-control")
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Position = Vector3(14, 0, 0)                                                                     //<14m from generator; dies
  player1.Spawn()
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = mech
  val buildingProbe = TestProbe()
  building.Actor = buildingProbe.ref
  guid.register(building, 1)
  guid.register(mech, 2)
  guid.register(player1, 3)
  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(mech),
    mech.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableEntity" should {
    "manage taking damage until being destroyed" in {
      mech.Health = 1 //no matter what, the next shot destoys it
      assert(mech.Health == 1)
      assert(!mech.Destroyed)

      mech.Actor ! Vitality.Damage(applyDamageTo)
      val msg1_2 = avatarProbe.receiveN(2, 500 milliseconds)
      assert(
        msg1_2.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg1_2(1) match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, _, Vector3(1, 0, 0))) => true
          case _                                                                                             => false
        }
      )
      assert(mech.Health == 0)
      assert(mech.Destroyed)
    }
  }
}

class DamageableEntityNotDestroyTwice extends ActorTest {
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

  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(gen),
    gen.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableEntity" should {
    "not be destroyed twice (skirting around the 'damage-destroys-at' value)" in {
      val originalHealth = gen.Health = gen.Definition.DamageDestroysAt + 1 //damaged, not yet restored
      gen.Condition = PlanetSideGeneratorState.Destroyed //initial state manip
      gen.Destroyed = true
      assert(gen.Destroyed)
      assert(originalHealth < gen.Definition.DefaultHealth)
      assert(originalHealth < gen.Definition.RepairRestoresAt)
      assert(originalHealth > gen.Definition.DamageDestroysAt)

      gen.Actor ! Vitality.Damage(applyDamageTo)
      avatarProbe.receiveOne(500 milliseconds)      //only one message
      avatarProbe.expectNoMessage(500 milliseconds) //only one message
      activityProbe.receiveOne(500 milliseconds)    //triggers activity hotspot, like it's not a killing blow
      assert(gen.Health < originalHealth)
      assert(gen.Destroyed)
      assert(originalHealth < gen.Definition.DefaultHealth)
      assert(originalHealth < gen.Definition.RepairRestoresAt)
      assert(gen.Health <= gen.Definition.DamageDestroysAt)
    }
  }
}

class DamageableAmenityTest extends ActorTest {
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

  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(term),
    term.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableAmenity" should {
    "send de-initialization messages upon destruction" in {
      //the decimator does enough damage to one-shot this terminal from any initial health
      term.Health = term.Definition.DamageDestroysAt + 1
      assert(term.Health > term.Definition.DamageDestroysAt)
      assert(!term.Destroyed)

      term.Actor ! Vitality.Damage(applyDamageTo)
      val msg1234 = avatarProbe.receiveN(4, 500 milliseconds)
      assert(
        msg1234.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg1234(1) match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, _, Vector3(1, 0, 0))) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg1234(2) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 50, 1)) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg1234(3) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 51, 1)) => true
          case _                                                                                             => false
        }
      )
      assert(term.Health <= term.Definition.DamageDestroysAt)
      assert(term.Destroyed)
    }
  }
}

class DamageableMountableDamageTest extends ActorTest {
  //TODO this test with not send HitHint packets because LivePlayers is not being allocated for the players in the zone
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val mech     = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)  //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn()
  guid.register(building, 1)
  guid.register(mech, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = mech
  mech.Position = Vector3(1, 0, 0)
  mech.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], mech), "mech-control")
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref

  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(mech),
    mech.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  mech.Seats(0).Occupant = player2        //seat the player
  player2.VehicleSeated = Some(mech.GUID) //seat the player
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableMountable" should {
    "alert seated occupants about incoming damage (damage with position)" in {
      assert(mech.Health == mech.Definition.DefaultHealth)

      mech.Actor ! Vitality.Damage(applyDamageTo)
      val msg1_3 = avatarProbe.receiveN(2, 500 milliseconds)
      val msg2   = activityProbe.receiveOne(500 milliseconds)
      assert(
        msg1_3.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg2 match {
          case activity: Zone.HotSpot.Activity =>
            activity.attacker == PlayerSource(player1) &&
              activity.defender == SourceEntry(mech) &&
              activity.location == Vector3(1, 0, 0)
          case _ => false
        }
      )
      assert(
        msg1_3(1) match {
          case AvatarServiceMessage(
                "TestCharacter2",
                AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(_, Vector3(2, 2, 2)))
              ) =>
            true
          case _ => false
        }
      )
    }
  }
}

class DamageableMountableDestroyTest extends ActorTest {
  //TODO this test with not send HitHint packets because LivePlayers is not being allocated for the players in the zone
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}

    GUID(guid)
  }
  val building = Building("test-building", 1, 1, zone, StructureType.Facility) //guid=1
  val mech     = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)  //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref
  guid.register(building, 1)
  guid.register(mech, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = mech
  mech.Position = Vector3(1, 0, 0)
  mech.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], mech), "mech-control")
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val buildingProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  building.Actor = buildingProbe.ref
  val weapon     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    SourceEntry(mech),
    mech.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  mech.Seats(0).Occupant = player2        //seat the player
  player2.VehicleSeated = Some(mech.GUID) //seat the player
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableMountable" should {
    "alert seated occupants that the mountable object has been destroyed and that they should have died" in {
      val originalHealth = mech.Health = mech.Definition.DamageDestroysAt + 1 //initial state manip
      assert(originalHealth > mech.Definition.DamageDestroysAt)
      assert(!mech.Destroyed)

      mech.Actor ! Vitality.Damage(applyDamageTo)
      val msg12 = avatarProbe.receiveN(2, 500 milliseconds)
      player1Probe.expectNoMessage(500 milliseconds)
      val msg3 = player2Probe.receiveOne(200 milliseconds)
      assert(
        msg12.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg12(1) match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, _, Vector3(1, 0, 0))) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg3 match {
          case Player.Die() => true
          case _            => false
        }
      )
      assert(mech.Health <= mech.Definition.DamageDestroysAt)
      assert(mech.Destroyed)
    }
  }
}

class DamageableWeaponTurretDamageTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe   = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref
  val turret = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) //2
  turret.Actor = system.actorOf(Props(classOf[TurretControl], turret), "turret-control")
  turret.Zone = zone
  turret.Position = Vector3(1, 0, 0)
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref
  guid.register(turret, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  turret.Seats(0).Occupant = player2
  player2.VehicleSeated = turret.GUID

  val weapon       = Tool(GlobalDefinitions.suppressor)
  val projectile   = weapon.Projectile
  val turretSource = SourceEntry(turret)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    turretSource,
    turret.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableWeaponTurret" should {
    "handle damage" in {
      assert(turret.Health == turret.Definition.DefaultHealth)

      turret.Actor ! Vitality.Damage(applyDamageTo)
      val msg12 = vehicleProbe.receiveOne(500 milliseconds)
      val msg3  = activityProbe.receiveOne(500 milliseconds)
      val msg4  = avatarProbe.receiveOne(500 milliseconds)
      assert(
        msg12 match {
          case VehicleServiceMessage("test", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), PlanetSideGUID(2), 0, _)) => true
          case _                                                                                                            => false
        }
      )
      assert(
        msg3 match {
          case activity: Zone.HotSpot.Activity =>
            activity.attacker == PlayerSource(player1) &&
              activity.defender == turretSource &&
              activity.location == Vector3(1, 0, 0)
          case _ => false
        }
      )
      assert(
        msg4 match {
          case AvatarServiceMessage(
                "TestCharacter2",
                AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(_, Vector3(2, 2, 2)))
              ) =>
            true
          case _ => false
        }
      )
      assert(turret.Health < turret.Definition.DefaultHealth)
    }
  }
}

class DamageableWeaponTurretJammerTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val turret = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) //2, 5, 6
  turret.Actor = system.actorOf(Props(classOf[TurretControl], turret), "turret-control")
  turret.Zone = zone
  turret.Position = Vector3(1, 0, 0)
  val turretWeapon = turret.Weapons.values.head.Equipment.get.asInstanceOf[Tool]

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn()
  player1.Position = Vector3(2, 2, 2)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref

  guid.register(turret, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(turretWeapon, 5)
  guid.register(turretWeapon.AmmoSlot.Box, 6)
  turret.Seats(0).Occupant = player2
  player2.VehicleSeated = turret.GUID

  val weapon       = Tool(GlobalDefinitions.jammer_grenade)
  val projectile   = weapon.Projectile
  val turretSource = SourceEntry(turret)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    turretSource,
    turret.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableWeaponTurret" should {
    "handle jammer effect" in {
      assert(turret.Health == turret.Definition.DefaultHealth)
      assert(!turret.Jammed)

      turret.Actor ! Vitality.Damage(applyDamageTo)
      val msg12 = vehicleProbe.receiveN(2, 500 milliseconds)
      assert(
        msg12.head match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, PlanetSideGUID(2), 27, 1)
              ) =>
            true
          case _ => false
        }
      )
      assert(
        msg12(1) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, PlanetSideGUID(5), 27, 1)
              ) =>
            true
          case _ => false
        }
      )
      expectNoMessage(100 milliseconds) // FIXME this is a hack to make it pass
      assert(turret.Health == turret.Definition.DefaultHealth)
      assert(turret.Jammed)
    }
  }
}

class DamageableWeaponTurretDestructionTest extends ActorTest {
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
  val player2 =
    Player(Avatar(1, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref

  guid.register(building, 1)
  guid.register(turret, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(turretWeapon, 5)
  guid.register(turretWeapon.AmmoSlot.Box, 6)
  turret.Seats(0).Occupant = player2
  player2.VehicleSeated = turret.GUID
  building.Position = Vector3(1, 0, 0)
  building.Zone = zone
  building.Amenities = turret

  val turretSource = SourceEntry(turret)
  val weaponA      = Tool(GlobalDefinitions.jammer_grenade)
  val projectileA  = weaponA.Projectile
  val resolvedA = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectileA,
      weaponA.Definition,
      weaponA.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    turretSource,
    turret.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToA = resolvedA.damage_model.Calculate(resolvedA)

  val weaponB     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectileB = weaponB.Projectile
  val resolvedB = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectileB,
      weaponB.Definition,
      weaponB.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    turretSource,
    turret.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToB = resolvedB.damage_model.Calculate(resolvedB)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableWeaponTurret" should {
    "handle being destroyed gracefully" in {
      turret.Health = turret.Definition.DamageDestroysAt + 1 //initial state manip
      turret.Upgrade = TurretUpgrade.AVCombo                 //initial state manip; act like having being upgraded properly
      assert(turret.Health > turret.Definition.DamageDestroysAt)
      assert(!turret.Jammed)
      assert(!turret.Destroyed)

      turret.Actor ! Vitality.Damage(applyDamageToA) //also test destruction while jammered
      vehicleProbe.receiveN(2, 500 milliseconds)     //flush jammered messages (see above)
      assert(turret.Health > turret.Definition.DamageDestroysAt)
      assert(turret.Jammed)
      assert(!turret.Destroyed)

      turret.Actor ! Vitality.Damage(applyDamageToB) //destroy
      val msg12_4 = avatarProbe.receiveN(3, 500 milliseconds)
      player1Probe.expectNoMessage(500 milliseconds)
      val msg3  = player2Probe.receiveOne(200 milliseconds)
      val msg56 = vehicleProbe.receiveN(2, 200 milliseconds)
      assert(
        msg12_4.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg12_4(1) match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, _, Vector3(1, 0, 0))) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg3 match {
          case Player.Die() => true
          case _            => false
        }
      )
      assert(
        msg12_4(2) match {
          case AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(0), PlanetSideGUID(5), _)) => true
          case _                                                                                                => false
        }
      )
      assert(
        msg56.head match {
          case VehicleServiceMessage.TurretUpgrade(SupportActor.ClearSpecific(List(t), _)) if turret eq t => true
          case _                                                                           => false
        }
      )
      assert(
        msg56(1) match {
          case VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(t, _, TurretUpgrade.None, _))
              if t eq turret =>
            true
          case _ => false
        }
      )
      assert(turret.Health <= turret.Definition.DamageDestroysAt)
      assert(!turret.Jammed)
      assert(turret.Destroyed)
    }
  }
}

class DamageableVehicleDamageTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadstealth) //guid=1
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "vehicle-control")
  atv.Position = Vector3(1, 0, 0)

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player1.Spawn()
  player1.Position = Vector3(2, 0, 0)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref

  guid.register(atv, 1)
  guid.register(player1, 2)
  guid.register(player2, 3)
  atv.Zone = zone
  atv.Seats(0).Occupant = player2
  player2.VehicleSeated = atv.GUID

  val weapon        = Tool(GlobalDefinitions.suppressor)
  val projectile    = weapon.Projectile
  val vehicleSource = SourceEntry(atv)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    atv.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableVehicle" should {
    "handle damage" in {
      atv.Shields = 1 //initial state manip
      assert(atv.Health == atv.Definition.DefaultHealth)
      assert(atv.Shields == 1)

      atv.Actor ! Vitality.Damage(applyDamageTo)
      val msg12   = vehicleProbe.receiveN(2, 200 milliseconds)
      val msg3    = activityProbe.receiveOne(200 milliseconds)
      val msg4   = avatarProbe.receiveOne(200 milliseconds)
      assert(
        msg12.head match {
          case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), PlanetSideGUID(1), 68, _)) => true
          case _                                                                                                        => false
        }
      )
      assert(
        msg12(1) match {
          case VehicleServiceMessage("test", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), PlanetSideGUID(1), 0, _)) => true
          case _                                                                                                            => false
        }
      )
      assert(
        msg3 match {
          case activity: Zone.HotSpot.Activity =>
            activity.attacker == PlayerSource(player1) &&
            activity.defender == vehicleSource &&
            activity.location == Vector3(1, 0, 0)
          case _ => false
        }
      )
      assert(
        msg4 match {
          case AvatarServiceMessage(
          "TestCharacter2",
          AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(9, Vector3(2, 0, 0)))
          ) =>
            true
          case _ => false
        }
      )
      assert(atv.Health < atv.Definition.DefaultHealth)
      assert(atv.Shields == 0)
    }
  }
}

class DamageableVehicleDamageMountedTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val lodestar = Vehicle(GlobalDefinitions.lodestar) //guid=1 & 4,5,6,7,8,9
  lodestar.Position = Vector3(1, 0, 0)
  val atv = Vehicle(GlobalDefinitions.quadstealth) //guid=11
  atv.Position = Vector3(1, 0, 0)
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "atv-control")

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player1.Spawn()
  player1.Position = Vector3(2, 0, 0)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref
  val player3 =
    Player(Avatar(0, "TestCharacter3", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=10
  player3.Spawn()
  val player3Probe = TestProbe()
  player3.Actor = player3Probe.ref

  guid.register(lodestar, 1)
  guid.register(player1, 2)
  guid.register(player2, 3)
  guid.register(lodestar.Utilities(2)(), 4)
  guid.register(lodestar.Utilities(3)(), 5)
  guid.register(lodestar.Utilities(4)(), 6)
  guid.register(lodestar.Utilities(5)(), 7)
  guid.register(lodestar.Utilities(6)(), 8)
  guid.register(lodestar.Utilities(7)(), 9)
  guid.register(player3, 10)
  guid.register(atv, 11)

  //the lodestar control actor needs to load after the utilities have guid's assigned
  lodestar.Actor = system.actorOf(Props(classOf[VehicleControl], lodestar), "lodestar-control")
  lodestar.Zone = zone
  lodestar.Seats(0).Occupant = player2
  player2.VehicleSeated = lodestar.GUID
  atv.Zone = zone
  atv.Seats(0).Occupant = player3
  player3.VehicleSeated = atv.GUID
  lodestar.CargoHolds(1).Occupant = atv
  atv.MountedIn = lodestar.GUID

  val weapon        = Tool(GlobalDefinitions.phoenix) //decimator
  val projectile    = weapon.Projectile
  val vehicleSource = SourceEntry(lodestar)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    lodestar.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "handle damage with mounted vehicles" in {
    lodestar.Shields = 1 //initial state manip
    atv.Shields = 1      //initial state manip
    assert(lodestar.Health == lodestar.Definition.DefaultHealth)
    assert(lodestar.Shields == 1)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(atv.Shields == 1)

    lodestar.Actor ! Vitality.Damage(applyDamageTo)
    val msg12   = vehicleProbe.receiveN(2, 200 milliseconds)
    val msg3    = activityProbe.receiveOne(200 milliseconds)
    val msg45   = avatarProbe.receiveN(2,200 milliseconds)
    assert(
      msg12.head match {
        case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), PlanetSideGUID(1), 68, _)) => true
        case _                                                                                                        => false
      }
    )
    assert(
      msg12(1) match {
        case VehicleServiceMessage("test", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), PlanetSideGUID(1), 0, _)) => true
        case _                                                                                                            => false
      }
    )
    assert(
      msg3 match {
        case activity: Zone.HotSpot.Activity =>
          activity.attacker == PlayerSource(player1) &&
            activity.defender == vehicleSource &&
            activity.location == Vector3(1, 0, 0)
        case _ => false
      }
    )
    assert(
      msg45.head match {
        case AvatarServiceMessage(
              "TestCharacter2",
              AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(400, Vector3(2, 0, 0)))
            ) =>
          true
        case _ => false
      }
    )
    assert(
      msg45(1) match {
        case AvatarServiceMessage(
              "TestCharacter3",
              AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(0, Vector3(2, 0, 0)))
            ) =>
          true
        case _ => false
      }
    )
    assert(lodestar.Health < lodestar.Definition.DefaultHealth)
    assert(lodestar.Shields == 0)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(atv.Shields == 1)
  }
}

class DamageableVehicleJammeringMountedTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadassault) //guid=1
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "atv-control")
  atv.Position = Vector3(1, 0, 0)
  val atvWeapon = atv.Weapons(1).Equipment.get.asInstanceOf[Tool] //guid=4 & 5

  val lodestar = Vehicle(GlobalDefinitions.lodestar) //guid=6
  lodestar.Position = Vector3(1, 0, 0)

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=7
  player1.Spawn()
  player1.Position = Vector3(2, 0, 0)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=8
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref
  val player3 =
    Player(Avatar(0, "TestCharacter3", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=9
  player3.Spawn()
  val player3Probe = TestProbe()
  player3.Actor = player3Probe.ref

  guid.register(atv, 1)
  guid.register(atvWeapon, 2)
  guid.register(atvWeapon.AmmoSlot.Box, 3)
  guid.register(lodestar, 4)
  guid.register(lodestar.Utilities(2)(), 5)
  guid.register(lodestar.Utilities(3)(), 6)
  guid.register(lodestar.Utilities(4)(), 7)
  guid.register(lodestar.Utilities(5)(), 8)
  guid.register(lodestar.Utilities(6)(), 9)
  guid.register(lodestar.Utilities(7)(), 10)
  guid.register(player1, 11)
  guid.register(player2, 12)
  guid.register(player3, 13)

  lodestar.Actor = system.actorOf(Props(classOf[VehicleControl], lodestar), "lodestar-control")
  atv.Zone = zone
  lodestar.Zone = zone
  atv.Seats(0).Occupant = player2
  player2.VehicleSeated = atv.GUID
  lodestar.Seats(0).Occupant = player3
  player3.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).Occupant = atv
  atv.MountedIn = lodestar.GUID

  val vehicleSource = SourceEntry(lodestar)
  val weapon        = Tool(GlobalDefinitions.jammer_grenade)
  val projectile    = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    lodestar.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "handle jammering with mounted vehicles" in {
    assert(lodestar.Health == lodestar.Definition.DefaultHealth)
    assert(!lodestar.Jammered)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(!atv.Jammered)

    lodestar.Actor ! Vitality.Damage(applyDamageTo)
    val msg12 = vehicleProbe.receiveOne(500 milliseconds)
    avatarProbe.expectNoMessage(500 milliseconds)
    player1Probe.expectNoMessage(200 milliseconds)
    player2Probe.expectNoMessage(200 milliseconds)
    player3Probe.expectNoMessage(200 milliseconds)
    assert(
      msg12 match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, PlanetSideGUID(4), 27, 1)
            ) =>
          true
        case _ => false
      }
    )
    assert(lodestar.Health == lodestar.Definition.DefaultHealth)
    assert(lodestar.Jammed)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(!atv.Jammed)
  }
}

class DamageableVehicleDestroyTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadassault) //guid=1
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "vehicle-control")
  atv.Position = Vector3(1, 0, 0)
  val atvWeapon = atv.Weapons(1).Equipment.get.asInstanceOf[Tool] //guid=4 & 5

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player1.Spawn()
  player1.Position = Vector3(2, 0, 0)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref

  guid.register(atv, 1)
  guid.register(player1, 2)
  guid.register(player2, 3)
  guid.register(atvWeapon, 4)
  guid.register(atvWeapon.AmmoSlot.Box, 5)
  atv.Zone = zone
  atv.Seats(0).Occupant = player2
  player2.VehicleSeated = atv.GUID

  val weapon        = Tool(GlobalDefinitions.suppressor)
  val projectile    = weapon.Projectile
  val vehicleSource = SourceEntry(atv)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectile,
      weapon.Definition,
      weapon.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    atv.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "DamageableVehicle" should {
    "handle destruction" in {
      atv.Health = atv.Definition.DamageDestroysAt + 1 //initial state manip
      atv.Shields = 1
      assert(atv.Health > atv.Definition.DamageDestroysAt)
      assert(atv.Shields == 1)
      assert(!atv.Destroyed)

      atv.Actor ! Vitality.Damage(applyDamageTo)
      val msg124 = avatarProbe.receiveN(3, 500 milliseconds)
      val msg3   = player2Probe.receiveOne(200 milliseconds)
      assert(
        msg124.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 0, _)) => true
          case _                                                                                            => false
        }
      )
      assert(
        msg124(1) match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(1), _, _, Vector3(1, 0, 0))) => true
          case _                                                                                             => false
        }
      )
      assert(
        msg3 match {
          case Player.Die() => true
          case _            => false
        }
      )
      assert(
        msg124(2) match {
          case AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(0), PlanetSideGUID(4), _)) => true
          case _                                                                                                => false
        }
      )
      assert(atv.Health <= atv.Definition.DamageDestroysAt)
      assert(atv.Destroyed)
      //
    }
  }
}

class DamageableVehicleDestroyMountedTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val vehicleProbe  = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.VehicleEvents = vehicleProbe.ref

  val atv = Vehicle(GlobalDefinitions.quadassault) //guid=1
  atv.Actor = system.actorOf(Props(classOf[VehicleControl], atv), "atv-control")
  atv.Position = Vector3(1, 0, 0)
  val atvWeapon = atv.Weapons(1).Equipment.get.asInstanceOf[Tool] //guid=4 & 5

  val lodestar = Vehicle(GlobalDefinitions.lodestar) //guid=6
  lodestar.Position = Vector3(1, 0, 0)

  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=7
  player1.Spawn()
  player1.Position = Vector3(2, 0, 0)
  val player1Probe = TestProbe()
  player1.Actor = player1Probe.ref
  val player2 =
    Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=8
  player2.Spawn()
  val player2Probe = TestProbe()
  player2.Actor = player2Probe.ref
  val player3 =
    Player(Avatar(0, "TestCharacter3", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=9
  player3.Spawn()
  val player3Probe = TestProbe()
  player3.Actor = player3Probe.ref

  guid.register(atv, 1)
  guid.register(atvWeapon, 2)
  guid.register(atvWeapon.AmmoSlot.Box, 3)
  guid.register(lodestar, 4)
  guid.register(lodestar.Utilities(2)(), 5)
  guid.register(lodestar.Utilities(3)(), 6)
  guid.register(lodestar.Utilities(4)(), 7)
  guid.register(lodestar.Utilities(5)(), 8)
  guid.register(lodestar.Utilities(6)(), 9)
  guid.register(lodestar.Utilities(7)(), 10)
  guid.register(player1, 11)
  guid.register(player2, 12)
  guid.register(player3, 13)

  lodestar.Actor = system.actorOf(Props(classOf[VehicleControl], lodestar), "lodestar-control")
  atv.Zone = zone
  lodestar.Zone = zone
  atv.Seats(0).Occupant = player2
  player2.VehicleSeated = atv.GUID
  lodestar.Seats(0).Occupant = player3
  player3.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).Occupant = atv
  atv.MountedIn = lodestar.GUID

  val vehicleSource = SourceEntry(lodestar)
  val weaponA       = Tool(GlobalDefinitions.jammer_grenade)
  val projectileA   = weaponA.Projectile
  val resolvedA = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectileA,
      weaponA.Definition,
      weaponA.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    lodestar.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToA = resolvedA.damage_model.Calculate(resolvedA)

  val weaponB     = Tool(GlobalDefinitions.phoenix) //decimator
  val projectileB = weaponB.Projectile
  val resolvedB = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(
      projectileB,
      weaponB.Definition,
      weaponB.FireMode,
      PlayerSource(player1),
      0,
      Vector3(2, 0, 0),
      Vector3(-1, 0, 0)
    ),
    vehicleSource,
    lodestar.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToB = resolvedB.damage_model.Calculate(resolvedB)
  expectNoMessage(200 milliseconds)
  //we're not testing that the math is correct

  "handle jammering with mounted vehicles" in {
    lodestar.Health = lodestar.Definition.DamageDestroysAt + 1 //initial state manip
    atv.Shields = 1                                            //initial state manip
    assert(lodestar.Health > lodestar.Definition.DamageDestroysAt)
    assert(!lodestar.Jammered)
    assert(!lodestar.Destroyed)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(atv.Shields == 1)
    assert(!atv.Jammered)
    assert(!atv.Destroyed)

    lodestar.Actor ! Vitality.Damage(applyDamageToA)
    vehicleProbe.receiveOne(500 milliseconds) //flush jammered message
    avatarProbe.expectNoMessage(200 milliseconds)
    player1Probe.expectNoMessage(200 milliseconds)
    player2Probe.expectNoMessage(200 milliseconds)
    player3Probe.expectNoMessage(200 milliseconds)
    assert(lodestar.Health > lodestar.Definition.DamageDestroysAt)
    assert(lodestar.Jammed)
    assert(!lodestar.Destroyed)
    assert(atv.Health == atv.Definition.DefaultHealth)
    assert(atv.Shields == 1)
    assert(!atv.Jammed)
    assert(!atv.Destroyed)

    lodestar.Actor ! Vitality.Damage(applyDamageToB)
    val msg_avatar = avatarProbe.receiveN(5, 500 milliseconds)
    avatarProbe.expectNoMessage(10 milliseconds)
    val msg_player2 = player2Probe.receiveOne(200 milliseconds)
    player2Probe.expectNoMessage(10 milliseconds)
    val msg_player3 = player3Probe.receiveOne(200 milliseconds)
    player3Probe.expectNoMessage(10 milliseconds)
    val msg_vehicle = vehicleProbe.receiveN(2, 200 milliseconds)
    vehicleProbe.expectNoMessage(10 milliseconds)
    assert(
      msg_avatar.exists({
        case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(4), 0, _)) => true
        case _                                                                                            => false
      })
    )
    assert(
      msg_avatar.exists({
        case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(4), _, _, Vector3(1, 0, 0))) => true
        case _                                                                                             => false
      })
    )
    assert(
      msg_avatar.exists({
        case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 0, _)) => true
        case _                                                                                            => false
      })
    )
    assert(
      msg_avatar.exists({
        case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(1), _, _, Vector3(1, 0, 0))) => true
        case _                                                                                             => false
      })
    )
    assert(
      msg_avatar.exists({
        case AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(0), PlanetSideGUID(2), _)) => true
        case _                                                                                                => false
      })
    )
    assert(
      msg_player2 match {
        case Player.Die() => true
        case _            => false
      }
    )
    assert(
      msg_player3 match {
        case Player.Die() => true
        case _            => false
      }
    )
    assert(
      msg_vehicle.exists({
        case VehicleServiceMessage(
              "test",
              VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, PlanetSideGUID(4), 27, 0)
            ) =>
          true
        case _ => false
      })
    )
    assert(
      msg_vehicle.exists({
        case VehicleServiceMessage(
              "test",
              VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, PlanetSideGUID(1), 68, 0)
            ) =>
          true
        case _ => false
      })
    )
    assert(lodestar.Health <= lodestar.Definition.DamageDestroysAt)
    assert(!lodestar.Jammed)
    assert(lodestar.Destroyed)
    assert(atv.Health <= atv.Definition.DefaultHealth)
    assert(atv.Shields == 0)
    assert(!atv.Jammed)
    assert(atv.Destroyed)
  }
}

object DamageableTest {}
