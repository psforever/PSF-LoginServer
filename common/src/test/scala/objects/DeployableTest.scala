// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import base.ActorTest
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.{TurretDeployable, _}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class DeployableTest extends Specification {
  "Deployable" should {
    "know its owner by GUID" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Owner mustEqual None
      obj.Owner = PlanetSideGUID(10)
      obj.Owner mustEqual Some(PlanetSideGUID(10))
    }

    "know its owner by GUID" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.OwnerName mustEqual None
      obj.OwnerName = "TestCharacter"
      obj.OwnerName mustEqual Some("TestCharacter")
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
      obj.Exploded mustEqual false
    }

    "explode" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.he_mine)
      obj.Exploded mustEqual false
      obj.Exploded = true
      obj.Exploded mustEqual true
    }
  }
}

class BoomerDeployableTest extends Specification {
  "BoomerDeployable" should {
    "construct" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Exploded mustEqual false
      obj.Trigger mustEqual None
    }

    "explode" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Exploded mustEqual false
      obj.Exploded = true
      obj.Exploded mustEqual true
    }

    "manage its trigger" in {
      val obj = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Trigger mustEqual None

      val trigger = new BoomerTrigger
      obj.Trigger = trigger
      obj.Trigger mustEqual Some(trigger)

      obj.Trigger = None
      obj.Trigger mustEqual None
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
      obj.Router mustEqual None
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
      obj.Router mustEqual None
      obj.Router = PlanetSideGUID(1)
      obj.Router mustEqual Some(PlanetSideGUID(1))
      obj.Router = None
      obj.Router mustEqual None
      obj.Router = PlanetSideGUID(1)
      obj.Router mustEqual Some(PlanetSideGUID(1))
      obj.Router = PlanetSideGUID(0)
      obj.Router mustEqual None
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
