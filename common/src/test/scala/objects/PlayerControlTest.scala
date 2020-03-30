// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.ballistics._
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects._
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.packet.game._
import net.psforever.types._
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

class PlayerControlHealTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2, 0, 0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player2.Zone = zone
  player2.Spawn
  player2.Actor = system.actorOf(Props(classOf[PlayerControl], player2), "player2-control")

  val tool = Tool(GlobalDefinitions.medicalapplicator) //guid=3 & 4

  guid.register(player1, 1)
  guid.register(player2, 2)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)

  "PlayerControl" should {
    "handle being healed by another player" in {
      val originalHealth = player2.Health = 0 //initial state manip
      val originalMagazine = tool.Magazine
      assert(originalHealth < player2.MaxHealth)

      player2.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar = avatarProbe.receiveN(4, 500 milliseconds)
      assert(
        msg_avatar.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(2) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 55, 1)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(3) match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, RepairMessage(PlanetSideGUID(2), _))) => true
          case _ => false
        }
      )
      val raisedHealth = player2.Health
      assert(raisedHealth > originalHealth)
      assert(tool.Magazine < originalMagazine)

      player1.Position = Vector3(10,0,0) //moved more than 5m away
      player2.Actor ! CommonMessages.Use(player1, Some(tool))
      avatarProbe.expectNoMsg(500 milliseconds)
      assert(raisedHealth == player2.Health)
    }
  }
}

class PlayerControlHealSelfTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2, 0, 0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")

  val tool = Tool(GlobalDefinitions.medicalapplicator) //guid=3 & 4

  guid.register(player1, 1)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)

  "PlayerControl" should {
    "handle healing own self" in {
      val originalHealth = player1.Health = 1 //initial state manip
      val originalMagazine = tool.Magazine
      assert(originalHealth < player1.MaxHealth)

      player1.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar1 = avatarProbe.receiveN(2, 500 milliseconds)
      assert(
        msg_avatar1.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar1(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 0, _)) => true
          case _ => false
        }
      )
      val raisedHealth = player1.Health
      assert(raisedHealth > originalHealth)
      assert(tool.Magazine < originalMagazine)

      player1.Position = Vector3(10,0,0) //trying to move away from oneself doesn't work
      player1.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar2 = avatarProbe.receiveN(2, 500 milliseconds)
      assert(
        msg_avatar2.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar2(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 0, _)) => true
          case _ => false
        }
      )
      assert(player1.Health > raisedHealth)
    }
  }
}

class PlayerControlRepairTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2, 0, 0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player2.Zone = zone
  player2.Spawn
  player2.Actor = system.actorOf(Props(classOf[PlayerControl], player2), "player2-control")

  val tool = Tool(GlobalDefinitions.bank) //guid=3 & 4

  guid.register(player1, 1)
  guid.register(player2, 2)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)

  "PlayerControl" should {
    "handle being repaired by another player" in {
      val originalArmor = player2.Armor = 0 //initial state manip
      val originalMagazine = tool.Magazine
      assert(originalArmor < player2.MaxArmor)

      player2.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar = avatarProbe.receiveN(5, 500 milliseconds)
      assert(
        msg_avatar.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 4, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(2) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 56, 1)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(3) match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, RepairMessage(PlanetSideGUID(2), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(4) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 56, 1)) => true
          case _ => false
        }
      )
      assert(player2.Armor > originalArmor)
      assert(tool.Magazine < originalMagazine)

      val fixedArmor = player2.Armor
      player1.Position = Vector3(10,0,0) //moved more than 5m away
      player2.Actor ! CommonMessages.Use(player1, Some(tool))
      avatarProbe.expectNoMsg(500 milliseconds)
      assert(fixedArmor == player2.Armor)
    }
  }
}

class PlayerControlRepairSelfTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2, 0, 0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")

  val tool = Tool(GlobalDefinitions.bank) //guid=3 & 4

  guid.register(player1, 1)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)

  "PlayerControl" should {
    "handle repairing own self" in {
      val originalArmor = player1.Armor = 0 //initial state manip
      val originalMagazine = tool.Magazine
      assert(originalArmor < player1.MaxArmor)

      player1.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar1 = avatarProbe.receiveN(2, 500 milliseconds)
      assert(
        msg_avatar1.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar1(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 4, _)) => true
          case _ => false
        }
      )
      val fixedArmor = player1.Armor
      assert(fixedArmor > originalArmor)
      assert(tool.Magazine < originalMagazine)

      player1.Position = Vector3(10,0,0) //trying to move away from oneself doesn't work
      player1.Actor ! CommonMessages.Use(player1, Some(tool))
      val msg_avatar2 = avatarProbe.receiveN(2, 500 milliseconds)
      assert(
        msg_avatar2.head match {
          case AvatarServiceMessage("TestCharacter1", AvatarAction.SendResponse(_, InventoryStateMessage(PlanetSideGUID(4), _, PlanetSideGUID(3), _))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar2(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(1), 4, _)) => true
          case _ => false
        }
      )
      assert(player1.Armor > fixedArmor)
    }
  }
}

class PlayerControlDamageTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val activityProbe = TestProbe()
  val avatarProbe = TestProbe()
  zone.Activity = activityProbe.ref
  zone.AvatarEvents = avatarProbe.ref
  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2, 0, 0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player2.Zone = zone
  player2.Spawn
  player2.Actor = system.actorOf(Props(classOf[PlayerControl], player2), "player2-control")
  val tool = Tool(GlobalDefinitions.suppressor) //guid 3 & 4
  val projectile = tool.Projectile
  val playerSource = SourceEntry(player2)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(projectile, tool.Definition, tool.FireMode, PlayerSource(player1), 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    playerSource,
    player1.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  guid.register(player1, 1)
  guid.register(player2, 2)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)
  expectNoMsg(200 milliseconds)
  "PlayerControl" should {
    "handle damage" in {
      assert(player2.Health == player2.Definition.DefaultHealth)
      assert(player2.Armor == player2.MaxArmor)
      player2.Actor ! Vitality.Damage(applyDamageTo)
      val msg_avatar = avatarProbe.receiveN(3, 500 milliseconds)
      val msg_activity = activityProbe.receiveOne(200 milliseconds)
      assert(
        msg_avatar.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(1) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 4, _)) => true
          case _ => false
        }
      )
      assert(
        msg_activity match {
          case activity : Zone.HotSpot.Activity =>
            activity.attacker == PlayerSource(player1) &&
              activity.defender == playerSource &&
              activity.location == Vector3(1, 0, 0)
          case _ => false
        }
      )
      assert(
        msg_avatar(2) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(10, Vector3(2, 0, 0)))) => true
          case _ => false
        }
      )
      assert(player2.Health < player2.Definition.DefaultHealth)
      assert(player2.Armor < player2.MaxArmor)
    }
  }
}

class PlayerControlDeathStandingTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2,0,0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player2.Zone = zone
  player2.Spawn
  player2.Actor = system.actorOf(Props(classOf[PlayerControl], player2), "player2-control")

  val tool = Tool(GlobalDefinitions.suppressor) //guid 3 & 4
  val projectile = tool.Projectile
  val player1Source = SourceEntry(player1)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(projectile, tool.Definition, tool.FireMode, player1Source, 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(player2),
    player2.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  guid.register(player1, 1)
  guid.register(player2, 2)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)
  expectNoMsg(200 milliseconds)

  "PlayerControl" should {
    "handle death" in {
      player2.Health = player2.Definition.DamageDestroysAt + 1 //initial state manip
      player2.ExoSuit = ExoSuitType.MAX
      player2.Armor = 1 //initial state manip
      player2.Capacitor = 1 //initial state manip
      assert(player2.Health > player2.Definition.DamageDestroysAt)
      assert(player2.Armor == 1)
      assert(player2.Capacitor == 1)
      assert(player2.isAlive)

      player2.Actor ! Vitality.Damage(applyDamageTo)
      val msg_avatar = avatarProbe.receiveN(8, 500 milliseconds)
      activityProbe.expectNoMsg(200 milliseconds)
      assert(
        msg_avatar.head match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 4, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(1) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.Killed(PlanetSideGUID(2))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(2) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(3) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 2, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(4) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 7, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(5) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(_, DestroyMessage(PlanetSideGUID(2), PlanetSideGUID(2), _, Vector3.Zero))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(6) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(_, AvatarDeadStateMessage(DeadState.Dead, 300000, 300000, Vector3.Zero, PlanetSideEmpire.NC, true))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(7) match {
          case AvatarServiceMessage("test", AvatarAction.DestroyDisplay(killer, victim, _, _))
            if killer == player1Source && victim == PlayerSource(player2) => true
          case _ => false
        }
      )
      assert(player2.Health <= player2.Definition.DamageDestroysAt)
      assert(player2.Armor == 0)
      assert(!player2.isAlive)
    }
  }
}

class PlayerControlDeathSeatedTest extends ActorTest {
  val guid = new NumberPoolHub(new LimitedNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  val avatarProbe = TestProbe()
  zone.AvatarEvents = avatarProbe.ref
  val activityProbe = TestProbe()
  zone.Activity = activityProbe.ref

  val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  player1.Zone = zone
  player1.Spawn
  player1.Position = Vector3(2,0,0)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1), "player1-control")
  val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=2
  player2.Zone = zone
  player2.Spawn
  player2.Actor = system.actorOf(Props(classOf[PlayerControl], player2), "player2-control")

  val vehicle = Vehicle(GlobalDefinitions.quadstealth) //guid=5
  vehicle.Faction = player2.Faction

  val tool = Tool(GlobalDefinitions.suppressor) //guid 3 & 4
  val projectile = tool.Projectile
  val player1Source = SourceEntry(player1)
  val resolved = ResolvedProjectile(
    ProjectileResolution.Hit,
    Projectile(projectile, tool.Definition, tool.FireMode, player1Source, 0, Vector3(2, 0, 0), Vector3(-1, 0, 0)),
    SourceEntry(player2),
    player2.DamageModel,
    Vector3(1, 0, 0)
  )
  val applyDamageTo = resolved.damage_model.Calculate(resolved)
  guid.register(player1, 1)
  guid.register(player2, 2)
  guid.register(tool, 3)
  guid.register(tool.AmmoSlot.Box, 4)
  guid.register(vehicle, 5)
  expectNoMsg(200 milliseconds)

  "PlayerControl" should {
    "handle death when seated (in something)" in {
      player2.Health = player2.Definition.DamageDestroysAt + 1 //initial state manip
      player2.VehicleSeated = vehicle.GUID //initial state manip, anything
      vehicle.Seats(0).Occupant = player2
      player2.Armor = 0 //initial state manip
      assert(player2.Health > player2.Definition.DamageDestroysAt)
      assert(player2.isAlive)

      player2.Actor ! Vitality.Damage(applyDamageTo)
      val msg_avatar = avatarProbe.receiveN(9, 500 milliseconds)
      activityProbe.expectNoMsg(200 milliseconds)
      assert(
        msg_avatar.head match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.Killed(PlanetSideGUID(2))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(1) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(_,
          ObjectDetachMessage(PlanetSideGUID(5), PlanetSideGUID(2), _, _, _, _))
          ) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(2) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 29, 1)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(3) match {
          case AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(2), PlanetSideGUID(2), _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(4) match {
          case AvatarServiceMessage("test", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 0, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(5) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.PlanetsideAttributeToAll(PlanetSideGUID(2), 2, _)) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(6) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(_, DestroyMessage(PlanetSideGUID(2), PlanetSideGUID(2), _, Vector3.Zero))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(7) match {
          case AvatarServiceMessage("TestCharacter2", AvatarAction.SendResponse(_, AvatarDeadStateMessage(DeadState.Dead, 300000, 300000, Vector3.Zero, PlanetSideEmpire.NC, true))) => true
          case _ => false
        }
      )
      assert(
        msg_avatar(8) match {
          case AvatarServiceMessage("test", AvatarAction.DestroyDisplay(killer, victim, _, _))
            if killer == player1Source && victim == PlayerSource(player2) => true
          case _ => false
        }
      )
      assert(player2.Health <= player2.Definition.DamageDestroysAt)
      assert(!player2.isAlive)
    }
  }
}


object PlayerControlTest { }
