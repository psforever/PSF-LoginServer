// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.ballistics._
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.mount.{MountInfo, Mountable, SeatDefinition}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{Zone, ZoneDeployableActor, ZoneMap}
import net.psforever.objects.{TurretDeployable, _}
import net.psforever.packet.game.{DeployableIcon, DeployableInfo, DeploymentAction}
import net.psforever.types._
import org.specs2.mutable.Specification
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import akka.actor.typed.scaladsl.adapter._
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}

import scala.collection.mutable
import scala.concurrent.duration._

class DeployableTest extends Specification {
  "Deployable" should {
    "know its owner by GUID" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.OwnerGuid.isEmpty mustEqual true
      obj.OwnerGuid = PlanetSideGUID(10)
      obj.OwnerGuid.contains(PlanetSideGUID(10)) mustEqual true
    }

    "know its owner by name" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      val owner = Player(Avatar(1, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 1, CharacterVoice.Mute))
      owner.GUID = PlanetSideGUID(1)
      owner.Spawn()
      obj.AssignOwnership(owner)
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
        DeployedItem.spitfire_turret.id,
        DeployedItem.spitfire_cloaked.id,
        DeployedItem.spitfire_aa.id,
        DeployedItem.portable_manned_turret.id,
        DeployedItem.portable_manned_turret_tr.id,
        DeployedItem.portable_manned_turret_nc.id,
        DeployedItem.portable_manned_turret_vs.id
      ).foreach(id => {
        try { new TurretDeployableDefinition(id) { } }
        catch { case _: Exception => ko }
      })
      ok
    }

    "define (invalid object)" in {
      new TurretDeployableDefinition(objectId = 5) { } must throwA[NoSuchElementException] //wrong object id altogether
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
      new TurretDeployable(GlobalDefinitions.spitfire_turret).MountPoints.isEmpty mustEqual true

      val pmt = new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
      pmt.MountPoints.get(1).contains(MountInfo(0, Vector3(0, 0, 0))) mustEqual true
      pmt.MountPoints.get(2).contains(MountInfo(0, Vector3(0, 0, 0))) mustEqual true
    }
  }
}

class DeployableMake extends Specification {
  "Deployables.Make" should {
    "construct a boomer" in {
      val func = Deployables.Make(DeployedItem.boomer)
      func() match {
        case _: BoomerDeployable => ok
        case _                   => ko
      }
    }

    "construct an he mine" in {
      val func = Deployables.Make(DeployedItem.he_mine)
      func() match {
        case obj: ExplosiveDeployable if obj.Definition == GlobalDefinitions.he_mine => ok
        case _                                                                       => ko
      }
    }

    "construct a disruptor mine" in {
      val func = Deployables.Make(DeployedItem.jammer_mine)
      func() match {
        case obj: ExplosiveDeployable if obj.Definition == GlobalDefinitions.jammer_mine => ok
        case _                                                                           => ko
      }
    }

    "construct a spitfire turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_turret)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_turret => ok
        case _                                                                            => ko
      }
    }

    "construct a shadow turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_cloaked)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_cloaked => ok
        case _                                                                             => ko
      }
    }

    "construct a cerberus turret" in {
      val func = Deployables.Make(DeployedItem.spitfire_aa)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.spitfire_aa => ok
        case _                                                                        => ko
      }
    }

    "construct a motion sensor" in {
      val func = Deployables.Make(DeployedItem.motionalarmsensor)
      func() match {
        case obj: SensorDeployable if obj.Definition == GlobalDefinitions.motionalarmsensor => ok
        case _                                                                              => ko
      }
    }

    "construct a sensor disruptor" in {
      val func = Deployables.Make(DeployedItem.sensor_shield)
      func() match {
        case obj: SensorDeployable if obj.Definition == GlobalDefinitions.sensor_shield => ok
        case _                                                                          => ko
      }
    }

    "construct three metal i-beams so huge that a driver must be blind to drive into them but does anyway" in {
      val func = Deployables.Make(DeployedItem.tank_traps)
      func() match {
        case obj: TrapDeployable if obj.Definition == GlobalDefinitions.tank_traps => ok
        case _                                                                     => ko
      }
    }

    "construct a generic field turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret => ok
        case _                                                                                   => ko
      }
    }

    "construct an avenger turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_tr)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_tr => ok
        case _                                                                                      => ko
      }
    }

    "construct an aegis shield generator" in {
      val func = Deployables.Make(DeployedItem.deployable_shield_generator)
      func() match {
        case obj: ShieldGeneratorDeployable if obj.Definition == GlobalDefinitions.deployable_shield_generator => ok
        case _                                                                                                 => ko
      }
    }

    "construct a telepad" in {
      val func = Deployables.Make(DeployedItem.router_telepad_deployable)
      func() match {
        case obj: TelepadDeployable if obj.Definition == GlobalDefinitions.router_telepad_deployable => ok
        case _                                                                                       => ko
      }
    }

    "construct an osprey turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_nc)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_nc => ok
        case _                                                                                      => ko
      }
    }

    "construct an orion turret" in {
      val func = Deployables.Make(DeployedItem.portable_manned_turret_vs)
      func() match {
        case obj: TurretDeployable if obj.Definition == GlobalDefinitions.portable_manned_turret_vs => ok
        case _                                                                                      => ko
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
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val eventsProbe = new TestProbe(system)

  val mine = Deployables.Make(DeployedItem.he_mine)().asInstanceOf[ExplosiveDeployable] //guid=1
  val avatar1 = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player1 = Player(avatar1) //guid=3
  val avatar2 = Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player2 = Player(avatar2) //guid=4
  val weapon = Tool(GlobalDefinitions.jammer_grenade) //guid=5
  val deployableList = new mutable.ListBuffer[Deployable]()
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools() = {}
    GUID(guid)
    override def Activity: ActorRef = eventsProbe.ref
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar1, avatar2)
    override def LivePlayers = List(player1, player2)
  }
  player1.Spawn()
  player2.Spawn()
  guid.register(mine, 1)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  mine.Zone = zone
  mine.OwnerGuid = player2
  //j_mine.OwnerName = player2.Name
  mine.Faction = PlanetSideEmpire.NC
  mine.Actor = system.actorOf(Props(classOf[MineDeployableControl], mine), "j-mine-control")

  val jMineSource = SourceEntry(mine)
  val pSource     = PlayerSource(player1)
  val projectile  = weapon.Projectile
  val resolved = DamageInteraction(
    jMineSource,
    ProjectileReason(
      DamageResolution.Hit,
      Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
      mine.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageToJ = resolved.calculate()

  "ExplosiveDeployable" should {
    "handle being jammered appropriately (no detonation)" in {
      assert(!mine.Destroyed)

      mine.Actor ! Vitality.Damage(applyDamageToJ)
      val eventMsgs  = eventsProbe.receiveN(4, 200 milliseconds)
      eventMsgs.head match {
        case Zone.HotSpot.Conflict(mineSrc, playerSrc, Vector3(1.0,0.0,0.0)) => ;
        case _ => assert(false, "")
      }
//      eventMsgs.head match {
//        case LocalServiceMessage(
//          "NC",
//          LocalAction.DeployableMapIcon(
//            ValidPlanetSideGUID(0),
//            DeploymentAction.Dismiss,
//            DeployableInfo(ValidPlanetSideGUID(1), DeployableIcon.HEMine, Vector3.Zero, ValidPlanetSideGUID(0))
//          )
//        ) => ;
//        case _ => assert(false, "")
//      }
      eventMsgs(1) match {
        case LocalServiceMessage("test", _, LocalAction.Detonate(PlanetSideGUID(1), _)) => ;
        case _ => assert(false, "")
      }
      eventMsgs(2) match {
        case LocalServiceMessage(
          "NC", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Dismiss,
            DeployableInfo(ValidPlanetSideGUID(1), DeployableIcon.HEMine, Vector3.Zero, ValidPlanetSideGUID(0))
          )
        ) => ;
        case _ => assert(false, "")
      }
      eventMsgs(3) match {
        case AvatarServiceMessage(
          "test",
          _,
          AvatarAction.Destroy(
          ValidPlanetSideGUID(1),
            ValidPlanetSideGUID(3),
            ValidPlanetSideGUID(0),
            Vector3.Zero
          )
        ) => ;
        case _ => assert(false, "")
      }
      assert(mine.Destroyed)
    }
  }
}

class ExplosiveDeployableJammerExplodeTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val eventsProbe = new TestProbe(system)
  val player1Probe = new TestProbe(system)
  val player2Probe = new TestProbe(system)

  val h_mine = Deployables.Make(DeployedItem.he_mine)().asInstanceOf[ExplosiveDeployable] //guid=2
  val avatar1 = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player1 = Player(avatar1) //guid=3
  val avatar2 = Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player2 = Player(avatar2) //guid=4
  val weapon = Tool(GlobalDefinitions.jammer_grenade) //guid=5
  val deployableList = new mutable.ListBuffer[Deployable]()
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools() = {}
    GUID(guid)
    override def Activity: ActorRef = eventsProbe.ref
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar1, avatar2)
    override def LivePlayers = List(player1, player2)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  player1.Spawn()
  player1.Actor = player1Probe.ref
  avatar2.deployables.AddOverLimit(h_mine) //cram it down your throat
  player2.Spawn()
  player2.Position = Vector3(10,0,0)
  player2.Actor = player2Probe.ref
  guid.register(h_mine, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  h_mine.Zone = zone
  h_mine.OwnerGuid = player2
  //h_mine.OwnerName = player2.Name
  h_mine.Faction = PlanetSideEmpire.NC
  h_mine.Actor = system.actorOf(Props(classOf[MineDeployableControl], h_mine), "h-mine-control")
  zone.blockMap.addTo(player1)
  zone.blockMap.addTo(player2)

  val pSource     = PlayerSource(player1)
  val projectile  = weapon.Projectile
  val resolved = DamageInteraction(
    SourceEntry(h_mine),
    ProjectileReason(
      DamageResolution.Hit,
      Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
      h_mine.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageToH = resolved.calculate()

  "ExplosiveDeployable" should {
    "handle being jammered appropriately (detonation)" in {
      assert(avatar2.deployables.Contains(h_mine))
      assert(!h_mine.Destroyed)

      h_mine.Actor ! Vitality.Damage(applyDamageToH)
      val p1Msgs = player1Probe.receiveN(1, 5000 milliseconds)
      val eventMsgs = eventsProbe.receiveN(3, 5000 milliseconds)
      eventMsgs.head match {
        case Zone.HotSpot.Conflict(target, attacker, _)
          if (target.Definition eq h_mine.Definition) && (attacker eq pSource) => ()
        case _ => assert(false, "")
      }
      eventMsgs(1) match {
        case LocalServiceMessage("test", _, LocalAction.Detonate(PlanetSideGUID(2), target))
          if target eq h_mine => ()
        case _ => assert(false, "")
      }
      eventMsgs(2) match {
        case LocalServiceMessage(
          "NC", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(2), DeployableIcon.HEMine, _, PlanetSideGUID(0))
          )
        )  => ;
        case _ => assert(false, "")
      }
      p1Msgs.head match {
        case Vitality.Damage(_) => ()
        case _                  => assert(false, "")
      }
      assert(h_mine.Destroyed)
    }
  }
}

class ExplosiveDeployableDestructionTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val eventsProbe = new TestProbe(system)
  val player1Probe = new TestProbe(system)
  val player2Probe = new TestProbe(system)

  val h_mine = Deployables.Make(DeployedItem.he_mine)().asInstanceOf[ExplosiveDeployable] //guid=2
  val avatar1 = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player1 = Player(avatar1) //guid=3
  val avatar2 = Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player2 = Player(avatar2) //guid=4
  val weapon = Tool(GlobalDefinitions.suppressor) //guid=5
  val deployableList = new mutable.ListBuffer[Deployable]()
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools() = {}
    GUID(guid)
    override def Activity: ActorRef = eventsProbe.ref
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar1, avatar2)
    override def LivePlayers = List(player1, player2)
  }
  player1.Spawn()
  player1.Actor = player1Probe.ref
  avatar2.deployables.AddOverLimit(h_mine) //cram it down your throat
  player2.Spawn()
  player2.Position = Vector3(10,0,0)
  player2.Actor = player2Probe.ref
  guid.register(h_mine, 2)
  guid.register(player1, 3)
  guid.register(player2, 4)
  guid.register(weapon, 5)
  h_mine.Zone = zone
  h_mine.AssignOwnership(player2)
  h_mine.Faction = PlanetSideEmpire.NC
  h_mine.Actor = system.actorOf(Props(classOf[MineDeployableControl], h_mine), "h-mine-control")

  val hMineSource = SourceEntry(h_mine)
  val pSource     = PlayerSource(player1)
  val projectile  = weapon.Projectile
  val resolved = DamageInteraction(
    hMineSource,
    ProjectileReason(
      DamageResolution.Hit,
      Projectile(projectile, weapon.Definition, weapon.FireMode, pSource, 0, Vector3.Zero, Vector3.Zero),
      h_mine.DamageModel
    ),
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.calculate()

  val activityProbe = TestProbe()
  val avatarProbe   = TestProbe()
  val localProbe    = TestProbe()

  "ExplosiveDeployable" should {
    "handle being destroyed" in {
      h_mine.Health = h_mine.Definition.DamageDestroysAt + 1
      assert(h_mine.Health > h_mine.Definition.DamageDestroysAt)
      assert(!h_mine.Destroyed)

      h_mine.Actor ! Vitality.Damage(applyDamageTo)
      val eventMsgs  = eventsProbe.receiveN(3, 200 milliseconds)
      player1Probe.expectNoMessage(200 milliseconds)
      val p2Msgs = player2Probe.receiveN(1, 200 milliseconds)
      eventMsgs.head match {
        case LocalServiceMessage(
          "NC", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(2), DeployableIcon.HEMine, _, PlanetSideGUID(0))
          )
        )  => ;
        case _ => assert(false, "")
      }
      eventMsgs(1) match {
        case AvatarServiceMessage(
          "test", _,
          AvatarAction.Destroy(PlanetSideGUID(2), PlanetSideGUID(3), Service.defaultPlayerGUID, Vector3.Zero)
        ) => ;
        case _ => assert(false, "")
      }
      eventMsgs(2) match {
        case LocalServiceMessage("test", _, LocalAction.TriggerEffect("detonate_damaged_mine", PlanetSideGUID(2))) => ;
        case _ => assert(false, "")
      }
      p2Msgs.head match {
        case Player.LoseDeployable(_) => ;
        case _                        => assert(false, "")
      }
      assert(h_mine.Health <= h_mine.Definition.DamageDestroysAt)
      assert(h_mine.Destroyed)
    }
  }
}
class TurretControlConstructTest extends ActorTest {
  "TurretControl" should {
    "construct" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      system.actorOf(Props(classOf[SmallTurretControl], obj), s"${obj.Definition.Name}_test")
    }
  }
}

class TurretControlInitializeTest extends ActorTest {
  "TurretControl" should {
    "initialize" in {
      val obj = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      obj.GUID = PlanetSideGUID(1)
      assert(obj.Actor == Default.Actor)
      val init = system.actorOf(Props(classOf[DeployableTest.TurretInitializer], obj), "init_turret_test")
      init ! "initialize"
      expectNoMessage(200 milliseconds)
      assert(obj.Actor != Default.Actor)
    }
  }
}

class TurretControlUninitializeTest extends ActorTest {
  "TurretControl" should {
    "uninitialize" in {
      val obj  = new TurretDeployable(GlobalDefinitions.spitfire_turret)
      val init = system.actorOf(Props(classOf[DeployableTest.TurretInitializer], obj), "init_turret_test")
      obj.GUID = PlanetSideGUID(1)
      init ! "initialize"
      expectNoMessage(200 milliseconds)
      assert(obj.Actor != Default.Actor)

      init ! "uninitialize"
      expectNoMessage(200 milliseconds)
      assert(obj.Actor == Default.Actor)
    }
  }
}

class TurretControlMountTest extends ActorTest {
  "TurretControl" should {
    "control mounting" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Zone = new Zone("test", new ZoneMap("test"), 0) {
        override def SetupNumberPools() = {}
        this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
      }
      obj.Actor = system.actorOf(Props(classOf[FieldTurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).occupant.isEmpty)
      val player1 = Player(Avatar(0, "test1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player1, 1)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player1)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).occupant.contains(player1))
    }
  }
}

class TurretControlBlockMountTest extends ActorTest {
  "TurretControl" should {
    "block mounting by others if already mounted by someone" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[FieldTurretControl], obj), s"${obj.Definition.Name}_test")
      obj.Zone = new Zone("test", new ZoneMap("test"), 0) {
        override def SetupNumberPools() = {}
        this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
      }

      assert(obj.Seats(0).occupant.isEmpty)
      val player1 = Player(Avatar(0, "test1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player1, 1)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player1)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).occupant.contains(player1))

      val player2 = Player(Avatar(1, "test2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player2, 1)
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
      obj.Actor = system.actorOf(Props(classOf[FieldTurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).occupant.isEmpty)
      val player = Player(Avatar(0, "test", PlanetSideEmpire.VS, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player, 1)
      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanNotMount])
      assert(obj.Seats(0).occupant.isEmpty)
    }
  }
}

class TurretControlDismountTest extends ActorTest {
  "TurretControl" should {
    "control dismounting" in {
      val obj = new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) { GUID = PlanetSideGUID(1) }
      obj.Faction = PlanetSideEmpire.TR
      obj.Zone = new Zone("test", new ZoneMap("test"), 0) {
        override def SetupNumberPools() = {}
        this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
      }
      obj.Actor = system.actorOf(Props(classOf[FieldTurretControl], obj), s"${obj.Definition.Name}_test")

      assert(obj.Seats(0).occupant.isEmpty)
      val player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Actor ! Mountable.TryMount(player, 1)

      val reply1a = receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).occupant.contains(player))

      obj.Actor ! Mountable.TryDismount(player, 0)
      val reply2a = receiveOne(200 milliseconds)
      assert(reply2a.isInstanceOf[Mountable.MountMessages])
      val reply2b = reply2a.asInstanceOf[Mountable.MountMessages]
      assert(reply2b.player == player)
      assert(reply2b.response.isInstanceOf[Mountable.CanDismount])
      assert(obj.Seats(0).occupant.isEmpty)
    }
  }
}

class TurretControlBetrayalMountTest extends ActorTest {
  "TurretControl" should {
    "allow all allegiances" in {
      val obj = new TurretDeployable(
        new TurretDeployableDefinition(685) {
          MountPoints += 1 -> MountInfo(0, Vector3.Zero)
          Seats += 0 -> new SeatDefinition()
          FactionLocked = false
        } //required (defaults to true)
      ) {
        GUID = PlanetSideGUID(1)
        Zone = new Zone("test", new ZoneMap("test"), 0) {
          override def SetupNumberPools() = {}
          this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
        }
      }
      obj.Faction = PlanetSideEmpire.TR
      obj.Actor = system.actorOf(Props(classOf[FieldTurretControl], obj), s"${obj.Definition.Name}_test")
      val probe = new TestProbe(system)

      assert(obj.Seats(0).occupant.isEmpty)
      val player = Player(Avatar(0, "test", PlanetSideEmpire.NC, CharacterSex.Male, 0, CharacterVoice.Mute))
      assert(player.Faction != obj.Faction)
      obj.Actor.tell(Mountable.TryMount(player, 1), probe.ref)
      val reply1a = probe.receiveOne(200 milliseconds)
      assert(reply1a.isInstanceOf[Mountable.MountMessages])
      val reply1b = reply1a.asInstanceOf[Mountable.MountMessages]
      assert(reply1b.player == player)
      assert(reply1b.response.isInstanceOf[Mountable.CanMount])
      assert(obj.Seats(0).occupant.contains(player))
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
  class TurretInitializer(obj: TurretDeployable) extends Actor {
    def receive: Receive = {
      case "initialize" =>
        obj.Definition.Initialize(obj, context)
      case "uninitialize" =>
        obj.Definition.Uninitialize(obj, context)
    }
  }
}
