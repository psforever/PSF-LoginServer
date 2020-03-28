// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.ballistics._
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{TurretDeployable, _}
import net.psforever.packet.game.{DeployableIcon, DeployableInfo, DeploymentAction}
import net.psforever.types._
import org.specs2.mutable.Specification
import services.{RemoverActor, Service}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.support.SupportActor

import scala.concurrent.duration._

class DeployableTest extends Specification {
  "Deployable" should {
    "know its owner by GUID" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Owner.isEmpty mustEqual true
      obj.Owner = PlanetSideGUID(10)
      obj.Owner.contains(PlanetSideGUID(10)) mustEqual true
    }

    "know its owner by GUID" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.OwnerName.isEmpty mustEqual true
      obj.OwnerName = "TestCharacter"
      obj.OwnerName.contains("TestCharacter") mustEqual true
    }

    "know its faction allegiance" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Faction mustEqual PlanetSideEmpire.NEUTRAL
      obj.Faction = PlanetSideEmpire.TR
      obj.Faction mustEqual PlanetSideEmpire.TR
    }
  }
}

class SensorDeployableTest extends Specification {
  "SensorDeployable" should {
    "construct" in {
      new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      ok
    }
  }
}

class ExplosiveDeployableTest extends Specification {
  "ExplosiveDeployable" should {
    "construct" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Destroyed mustEqual false
    }

    "explode" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Destroyed mustEqual false
      obj.Destroyed = true
      obj.Destroyed mustEqual true
    }
  }
}

class BoomerDeployableTest extends Specification {
  "BoomerDeployable" should {
    "construct" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Destroyed mustEqual false
      obj.Trigger.isEmpty mustEqual true
    }

    "explode" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Destroyed mustEqual false
      obj.Destroyed = true
      obj.Destroyed mustEqual true
    }

    "manage its trigger" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Trigger.isEmpty mustEqual true

      val trigger = new BoomerTrigger
      obj.Trigger = trigger
      obj.Trigger.contains(trigger) mustEqual true

      obj.Trigger = None
      obj.Trigger.isEmpty mustEqual true
    }
  }
}

class TrapDeployableTest extends Specification {
  "SensorDeployable" should {
    "construct" in {
      val obj = new TrapDeployable(GlobalDefinitions.tank_traps)
      obj.Health mustEqual GlobalDefinitions.tank_traps.MaxHealth
    }

    "update health values" in {
      val obj = new TrapDeployable(GlobalDefinitions.tank_traps)
      obj.Health mustEqual GlobalDefinitions.tank_traps.MaxHealth
      obj.Health = 0
      obj.Health mustEqual 0
    }
  }
}

class TurretDeployableTest extends Specification {
  "TurretDeployable" should {
    "define (valid turret objects)" in {
      List(
        DeployedItem.spitfire_turret.id, DeployedItem.spitfire_cloaked.id, DeployedItem.spitfire_aa.id,
        DeployedItem.portable_manned_turret.id, DeployedItem.portable_manned_turret_tr.id,
        DeployedItem.portable_manned_turret_nc.id, DeployedItem.portable_manned_turret_vs.id
      ).foreach(id => {
        try { new TurretDeployableDefinition(id) } catch { case _ : Exception => ko }
      })
      ok
    }

    "define (invalid object)" in {
      new TurretDeployableDefinition(5) must throwA[NoSuchElementException] //wrong object id altogether
    }

    "construct" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      obj.Health mustEqual obj.MaxHealth
    }

    "update health values" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      obj.Health mustEqual GlobalDefinitions.spitfire_turret.MaxHealth
      obj.Health = 0
      obj.Health mustEqual 0
    }

    "may have mount point" in {
      new TurretDeployable(GlobalDefinitions.spitfire_turret).MountPoints mustEqual Map()
      new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs).MountPoints mustEqual Map(1 -> 0, 2 -> 0)
    }
  }
}

class DeployableMake extends Specification {
  "Deployables.Make" should {
    "construct a boomer" in {
      val func = Deployables.Make(DeployedItem.boomer)
      func() match {
        case _ : BoomerDeployable => ok
        case _ => ko
      }
    }

    "construct an he mine" in {
      val func = Deployables.Make(DeployedItem.he_mine)
      func() match {
        case obj : ExplosiveDeployable if obj.Definition == GlobalDefinitions.he_mine => ok
        case _ => ko
      }
    }

    "construct a disruptor mine" in {
      val func = Deployables.Make(DeployedItem.jammer_mine)
      func() match {
        case obj : ExplosiveDeployable if obj.Definition == GlobalDefinitions.jammer_mine => ok
        case _ => ko
      }
    }

    "construct a spitfire turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_turret)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_turret => ok
        case _ => ko
      }
    }

    "construct a shadow turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_cloaked)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_cloaked => ok
        case _ => ko
      }
    }

    "construct a cerebus turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_aa)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_aa => ok
        case _ => ko
      }
    }

    "construct a motion sensor" in {
      val func = Deployables.Make(DeployedItem.motionalarmsensor)
      func() match {
        case obj : SensorDeployable if obj.Definition == GlobalDefinitions.motionalarmsensor => ok
        case _ => ko
      }
    }

    "construct a sensor disruptor" in {
      val func = Deployables.Make(DeployedItem.sensor_shield)
      func() match {
        case obj : SensorDeployable if obj.Definition == GlobalDefinitions.sensor_shield => ok
        case _ => ko
      }
    }

    "construct three metal i-beams so huge that a driver must be blind to drive into them but does anyway" in {
      val func = Deployables.Make(DeployedItem.tank_traps)
      func() match {
        case obj : TrapDeployable if obj.Definition == GlobalDefinitions.tank_traps => ok
        case _ => ko
      }
    }

    "construct a generic field turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret => ok
        case _ => ko
      }
    }

    "construct an avenger turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_tr)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_tr => ok
        case _ => ko
      }
    }

    "construct an aegis shield generator" in {
      val func = Deployables.Make(DeployedItem.deployable_shield_generator)
      func() match {
        case obj : ShieldGeneratorDeployable if obj.Definition == GlobalDefinitions.deployable_shield_generator => ok
        case _ => ko
      }
    }

    "construct a telepad" in {
      val func = Deployables.Make(DeployedItem.router_telepad_deployable)
      func() match {
        case obj : TelepadDeployable if obj.Definition == GlobalDefinitions.router_telepad_deployable => ok
        case _ => ko
      }
    }

    "construct an osprey turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_nc)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_nc => ok
        case _ => ko
      }
    }

    "construct an orion turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_vs)
      func() match {
        case obj : TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_vs => ok
        case _ => ko
      }
    }
  }
}

class ShieldGeneratorDeployableTest extends Specification {
  "ShieldGeneratorDeployable" should {
    "construct" in {
      val obj = new ShieldGeneratorDeployable(GlobalDefinitions.deployable_shield_generator)
      obj.Health mustEqual obj.MaxHealth
    }

    "update health values" in {
      val obj = new ShieldGeneratorDeployable(GlobalDefinitions.deployable_shield_generator)
      obj.Health mustEqual GlobalDefinitions.deployable_shield_generator.MaxHealth
      obj.Health = 0
      obj.Health mustEqual 0
    }
  }
}


class ExplosiveDeployableJammerTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val localProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.LocalEvents = localProbe.ref

  val j_mine = Deployables.Make(DeployedItem.jammer_mine)().asInstanceOf[ExplosiveDeployable] //guid=1
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn
  val weapon = Tool(GlobalDefinitions.jammer_grenade) //guid=5
  guid.register(j_mine, 1)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  j_mine.Zone = zone
  j_mine.Owner = player2
  j_mine.OwnerName = player2.Name
  j_mine.Faction = PlanetSideEmpire.NC
  j_mine.Actor = system.actorOf(Props(classOf[ExplosiveDeployableControl], j_mine), "j-mine-control")

  val jMineSource = SourceEntry(j_mine)
  val pSource = PlayerSource(player1)
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
    jMineSource,
    j_mine.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToJ = resolved.damage_model.Calculate(resolved)

  "ExplosiveDeployable" should {
    "handle being jammered appropriately (no detonation)" in {
      assert(!j_mine.Destroyed)

      j_mine.Actor ! Vitality.Damage(applyDamageToJ)
      val msg_local = localProbe.receiveN(4, 200 milliseconds)
      val msg_avatar = avatarProbe.receiveOne(200 milliseconds)
      activityProbe.expectNoMsg(200 milliseconds)
      assert(
        msg_local.head match {
          case LocalServiceMessage("TestCharacter2", LocalAction.AlertDestroyDeployable(PlanetSideGUID(0), target)) => target eq j_mine
          case _ => false
        }
      )
      assert(
        msg_local(1) match {
          case LocalServiceMessage("NC", LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, _, PlanetSideGUID(0))
            )) => true
          case _ => false
        }
      )
      assert(
        msg_local(2) match {
          case LocalServiceMessage.Deployables(SupportActor.ClearSpecific(List(target), _zone)) => (target eq j_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_local(3) match {
          case LocalServiceMessage.Deployables(RemoverActor.AddTask(target, _zone, _)) => (target eq j_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_avatar match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(1), _, Service.defaultPlayerGUID, _)) => true
          case _ => false
        }
      )
      assert(j_mine.Destroyed)
    }
  }
}

class ExplosiveDeployableJammerExplodeTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val localProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.LocalEvents = localProbe.ref

  val h_mine = Deployables.Make(DeployedItem.he_mine)().asInstanceOf[ExplosiveDeployable] //guid=2
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn
  val weapon = Tool(GlobalDefinitions.jammer_grenade) //guid=5
  guid.register(h_mine, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  h_mine.Zone = zone
  h_mine.Owner = player2
  h_mine.OwnerName = player2.Name
  h_mine.Faction = PlanetSideEmpire.NC
  h_mine.Actor = system.actorOf(Props(classOf[ExplosiveDeployableControl], h_mine), "h-mine-control")

  val hMineSource = SourceEntry(h_mine)
  val pSource = PlayerSource(player1)
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
    hMineSource,
    h_mine.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageToH = resolved.damage_model.Calculate(resolved)

  "ExplosiveDeployable" should {
    "handle being jammered appropriately (detonation)" in {
      assert(!h_mine.Destroyed)

      h_mine.Actor ! Vitality.Damage(applyDamageToH)
      val msg_local = localProbe.receiveN(5, 200 milliseconds)
      val msg_avatar = avatarProbe.receiveOne(200 milliseconds)
      val msg_activity = activityProbe.receiveOne(200 milliseconds)
      assert(
        msg_local.head match {
          case LocalServiceMessage("test", LocalAction.Detonate(PlanetSideGUID(2), target)) => target eq h_mine
          case _ => false
        }
      )
      assert(
        msg_local(1) match {
          case LocalServiceMessage("TestCharacter2", LocalAction.AlertDestroyDeployable(PlanetSideGUID(0), target)) => target eq h_mine
          case _ => false
        }
      )
      assert(
        msg_local(2) match {
          case LocalServiceMessage("NC", LocalAction.DeployableMapIcon(
          PlanetSideGUID(0),
          DeploymentAction.Dismiss,
          DeployableInfo(PlanetSideGUID(2), DeployableIcon.HEMine, _, PlanetSideGUID(0))
          )) => true
          case _ => false
        }
      )
      assert(
        msg_local(3) match {
          case LocalServiceMessage.Deployables(SupportActor.ClearSpecific(List(target), _zone)) => (target eq h_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_local(4) match {
          case LocalServiceMessage.Deployables(RemoverActor.AddTask(target, _zone, _)) => (target eq h_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_avatar match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, Service.defaultPlayerGUID, _)) => true
          case _ => false
        }
      )
      assert(
        msg_activity match {
          case Zone.HotSpot.Activity(target, attacker, _) => (target eq hMineSource) && (attacker eq pSource)
          case _ => false
        }
      )
      assert(h_mine.Destroyed)
    }
  }
}

class ExplosiveDeployableDestructionTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  val localProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  zone.LocalEvents = localProbe.ref

  val h_mine = Deployables.Make(DeployedItem.he_mine)().asInstanceOf[ExplosiveDeployable] //guid=2
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=3
  player1.Spawn
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=4
  player2.Spawn
  val weapon = Tool(GlobalDefinitions.suppressor) //guid=5
  guid.register(h_mine, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  h_mine.Zone = zone
  h_mine.Owner = player2
  h_mine.OwnerName = player2.Name
  h_mine.Faction = PlanetSideEmpire.NC
  h_mine.Actor = system.actorOf(Props(classOf[ExplosiveDeployableControl], h_mine), "h-mine-control")

  val hMineSource = SourceEntry(h_mine)
  val pSource = PlayerSource(player1)
  val projectile = weapon.Projectile
  val resolved = ResolvedProjectile(
    ProjectileResolution.Splash,
    Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
    hMineSource,
    h_mine.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)

  "ExplosiveDeployable" should {
    "handle being destroyed" in {
      h_mine.Health = h_mine.Definition.DamageDestroysAt + 1
      assert(h_mine.Health > h_mine.Definition.DamageDestroysAt)
      assert(!h_mine.Destroyed)

      h_mine.Actor ! Vitality.Damage(applyDamageTo)
      val msg_local = localProbe.receiveN(5, 200 milliseconds)
      val msg_avatar = avatarProbe.receiveOne(200 milliseconds)
      activityProbe.expectNoMsg(200 milliseconds)
      assert(
        msg_local.head match {
          case LocalServiceMessage("TestCharacter2", LocalAction.AlertDestroyDeployable(PlanetSideGUID(0), target)) => target eq h_mine
          case _ => false
        }
      )
      assert(
        msg_local(1) match {
          case LocalServiceMessage("NC", LocalAction.DeployableMapIcon(
          PlanetSideGUID(0),
          DeploymentAction.Dismiss,
          DeployableInfo(PlanetSideGUID(2), DeployableIcon.HEMine, _, PlanetSideGUID(0))
          )) => true
          case _ => false
        }
      )
      assert(
        msg_local(2) match {
          case LocalServiceMessage.Deployables(SupportActor.ClearSpecific(List(target), _zone)) => (target eq h_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_local(3) match {
          case LocalServiceMessage.Deployables(RemoverActor.AddTask(target, _zone, _)) => (target eq h_mine) && (_zone eq zone)
          case _ => false
        }
      )
      assert(
        msg_local(4) match {
          case LocalServiceMessage("test", LocalAction.TriggerEffect(_, "detonate_damaged_mine", PlanetSideGUID(2))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar match {
          case AvatarServiceMessage("test", AvatarAction.Destroy(PlanetSideGUID(2), _, Service.defaultPlayerGUID, _)) => true
          case _ => false
        }
      )
      assert(h_mine.Health <= h_mine.Definition.DamageDestroysAt)
      assert(h_mine.Destroyed)
    }
  }
}
class TurretControlConstructTest extends ActorTest {
  "TurretControl" should {
    "construct" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")
    }
  }
}

class TurretControlInitializeTest extends ActorTest {
  "TurretControl" should {
    "initialize" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      obj.GUID = PlanetSideGUID(1)
      assert(obj.Actor == ActorRef.noSender)
      val init = system.actorOf(Props(classOf[DeployableTest.TurretInitializer], obj), "init_turret_test")
      init ! "initialize"
      expectNoMsg(200 milliseconds)
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class TurretControlUninitializeTest extends ActorTest {
  "TurretControl" should {
    "uninitialize" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      val init = system.actorOf(Props(classOf[DeployableTest.TurretInitializer], obj), "init_turret_test")
      obj.GUID = PlanetSideGUID(1)
      init ! "initialize"
      expectNoMsg(200 milliseconds)
      assert(obj.Actor != ActorRef.noSender)

      init ! "uninitialize"
      expectNoMsg(200 milliseconds)
      assert(obj.Actor == ActorRef.noSender)
    }
  }
}

class TurretControlMountTest extends ActorTest {
  "TurretControl" should {
    "control mounting" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).Occupant.isEmpty)
      val player1 = Player(Avatar("test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player1, 0)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player1)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).Occupant.contains(player1))
    }
  }
}

class TurretControlBlockMountTest extends ActorTest {
  "TurretControl" should {
    "block mounting by others if already mounted by someone" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).Occupant.isEmpty)
      val player1 = Player(Avatar("test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player1, 0)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player1)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).Occupant.contains(player1))

      val player2 = Player(Avatar("test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player2, 0)
      val reply2a = receiveOne(200 milliseconds)
      assert(reply2a.isInstanceOf[Mountable.MountMessages])
      val reply2b = reply2a.asInstanceOf[Mountable.MountMessages]
      assert(reply2b.player == player2)
      assert(reply2b.response.isInstanceOf[Mountable.CanNotMount])
    }
  }
}

class TurretControlBlockBetrayalMountTest extends ActorTest {
  "TurretControl" should {
    "block mounting by players of another faction" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).Occupant.isEmpty)
      val player = Player(Avatar("test", PlanetSideEmpire.VS, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player, 0)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanNotMount])
      assert(obj.Seats(0).Occupant.isEmpty)
    }
  }
}

class TurretControlDismountTest extends ActorTest {
  "TurretControl" should {
    "control dismounting" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).Occupant.isEmpty)
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player, 0)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).Occupant.contains(player))

      obj.Actor ! Mountable.TryDismount(player, 0)
      val reply2a = receiveOne(200 milliseconds)
      assert(reply2a.isInstanceOf[Mountable.MountMessages])
      val reply2b = reply2a.asInstanceOf[Mountable.MountMessages]
      assert(reply2b.player == player)
      assert(reply2b.response.isInstanceOf[Mountable.CanDismount])
      assert(obj.Seats(0).Occupant.isEmpty)
    }
  }
}

class TurretControlBetrayalMountTest extends ActorTest {
  "TurretControl" should {
    "allow all allegiances" in {
      val obj = new TurretDeployable(
        new TurretDeployableDefinition(685) { FactionLocked = false } //required (defaults to true)
      ) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).Occupant.isEmpty)
      val player = Player(Avatar("test", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute))
      assert(player.Faction != obj.Faction)
      obj.Actor ! Mountable.TryMount(player, 0)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).Occupant.contains(player))
    }
  }
}

class TelepadDeployableTest extends Specification {
  "Telepad" should {
    "construct" in {
      val obj = new Telepad(GlobalDefinitions.router_telepad)
      obj.Active mustEqual false
      obj.Router.isEmpty mustEqual true
    }

    "activate and deactivate" in {
      val obj = new Telepad(GlobalDefinitions.router_telepad)
      obj.Active mustEqual false
      obj.Active = true
      obj.Active mustEqual true
      obj.Active = false
      obj.Active mustEqual false
    }

    "keep track of a Router" in {
      val obj = new Telepad(GlobalDefinitions.router_telepad)
      obj.Router.isEmpty mustEqual true
      obj.Router = PlanetSideGUID(1)
      obj.Router.contains(PlanetSideGUID(1)) mustEqual true
      obj.Router = None
      obj.Router.isEmpty mustEqual true
      obj.Router = PlanetSideGUID(1)
      obj.Router.contains(PlanetSideGUID(1)) mustEqual true
      obj.Router = PlanetSideGUID(0)
      obj.Router.isEmpty mustEqual true
    }
  }
}

object DeployableTest {
  class TurretInitializer(obj : TurretDeployable) extends Actor {
    def receive : Receive = {
      case "initialize" =>
        obj.Definition.Initialize(obj, context)
      case "uninitialize" =>
        obj.Definition.Uninitialize(obj, context)
    }
  }
}
