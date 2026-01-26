// Copyright (c) 2021 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.{ActorTest, FreedContextActorTest}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{Avatar, Certification, PlayerControl}
import net.psforever.objects.{ConstructionItem, Deployables, GlobalDefinitions, Player}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.zones.{Zone, ZoneDeployableActor, ZoneMap}
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class DeployableBehaviorSetupTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid=1
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(jmine, number = 1)
  jmine.Faction = PlanetSideEmpire.TR
  jmine.Position = Vector3(1,2,3)
  jmine.Orientation = Vector3(4,5,6)

  "DeployableBehavior" should {
    "perform self-setup" in {
      assert(deployableList.isEmpty, "self-setup test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(jmine)

      val eventsMsgs = eventsProbe.receiveN(2, 10.seconds)
      eventsMsgs.head match {
        case LocalServiceMessage("test", _, LocalAction.DeployItem(obj)) =>
          assert(obj eq jmine, "self-setup test - not same mine")
        case _ =>
          assert( false, "self-setup test - wrong deploy message")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage("TR", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1,2,3), PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "self-setup test - no icon or wrong icon")
      }
      assert(deployableList.contains(jmine), "self-setup test - deployable not appended to list")
    }
  }
}

class DeployableBehaviorSetupOwnedP1Test extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val avatar = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player = Player(avatar) //guid=3
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid=1
  val citem = new ConstructionItem(GlobalDefinitions.ace) //guid = 2
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar)
    override def LivePlayers = List(player)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(jmine, number = 1)
  guid.register(citem, number = 2)
  guid.register(player, number = 3)
  jmine.Faction = PlanetSideEmpire.TR
  jmine.Position = Vector3(1,2,3)
  jmine.Orientation = Vector3(4,5,6)
  jmine.AssignOwnership(player)

  "DeployableBehavior" should {
    "receive setup functions after asking owner" in {
      val playerProbe = new TestProbe(system)
      player.Actor = playerProbe.ref
      assert(deployableList.isEmpty, "owned setup test, 1 - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.BuildByOwner(jmine, player, citem)

      playerProbe.receiveOne(200.milliseconds) match {
        case Player.BuildDeployable(a, b) =>
          assert((a eq jmine) && (b eq citem), "owned setup test, 1 - process echoing wrong mine or wrong construction item")
        case _ =>
          assert(false, "owned setup test, 1 - not echoing messages to owner")
      }
    }
  }
}

class DeployableBehaviorSetupOwnedP2Test extends FreedContextActorTest {
  val eventsProbe = new TestProbe(system)
  val avatar = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player = Player(avatar) //guid=3
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid=1
  val citem = new ConstructionItem(GlobalDefinitions.ace) //guid = 2
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar)
    override def LivePlayers = List(player)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(jmine, number = 1)
  guid.register(citem, number = 2)
  guid.register(player, number = 3)
  guid.register(avatar.locker, number = 4)
  jmine.Faction = PlanetSideEmpire.TR
  jmine.Position = Vector3(1,2,3)
  jmine.Orientation = Vector3(4,5,6)
  jmine.AssignOwnership(player)
  avatar.deployables.UpdateMaxCounts(Set(Certification.CombatEngineering, Certification.AssaultEngineering))
  player.Zone = zone
  player.Slot(slot = 0).Equipment = citem
  player.Actor = system.actorOf(Props(classOf[PlayerControl], player, null), name = "deployable-test-player-control")

  "DeployableBehavior" should {
    "perform setup functions after asking owner" in {
      assert(player.Slot(slot = 0).Equipment.contains(citem), "owned setup test, 2 - player hand is empty")
      assert(deployableList.isEmpty, "owned setup test, 2 - deployable list is not empty")
      assert(!avatar.deployables.Contains(jmine), "owned setup test, 2 - avatar already owns deployable")
      zone.Deployables ! Zone.Deployable.BuildByOwner(jmine, player, citem)
      //assert(false, "test needs to be fixed")
      val eventsMsgs = eventsProbe.receiveN(7, 10.seconds)
      eventsMsgs.head match {
        case AvatarServiceMessage(
          "TestCharacter1",
          _,
          AvatarAction.SendResponse(
            ObjectDeployedMessage(0, "jammer_mine", DeployOutcome.Success, 1, 20)
          )
        ) => ;
        case _ =>
          assert(false, "owned setup test, 2 - did not receive build confirmation")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage("TestCharacter1", _, LocalAction.DeployableUIFor(DeployedItem.jammer_mine)) => ;
        case _ => assert(false, "owned setup test, 2 - did not receive ui update")
      }
      eventsMsgs(2) match {
        case LocalServiceMessage(
          "test",
          PlanetSideGUID(3),
          LocalAction.TriggerEffectLocation("spawn_object_effect", Vector3(1,2,3), Vector3(4,5,6))
        )      => ;
        case _ => assert(false, "owned setup test, 2 - no spawn fx")
      }
      eventsMsgs(3) match {
        case LocalServiceMessage("test", _, LocalAction.DeployItem(obj)) =>
          assert(obj eq jmine, "owned setup test, 2 - not same mine")
        case _ =>
          assert( false, "owned setup test, 2 - wrong deploy message")
      }
      //the message order can be jumbled from here-on
      eventsMsgs(4) match {
        case LocalServiceMessage("TR", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1,2,3), PlanetSideGUID(3))
          )
        ) => ;
        case _ =>
          assert(false, "owned setup test, 2 - no icon or wrong icon")
      }
      eventsMsgs(5) match {
        case AvatarServiceMessage(
          "TestCharacter1", _,
          AvatarAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(1), 21))
        ) => ;
        case _ =>
          assert(false, "owned setup test, 2 - build action not reset (GOAM21)")
      }
      eventsMsgs(6) match {
        case AvatarServiceMessage("test", _, AvatarAction.ObjectDelete(PlanetSideGUID(2), 0)) => ;
        case _ =>
          assert(false, "owned setup test, 2 - construction tool not deleted")
      }
      assert(player.Slot(slot = 0).Equipment.isEmpty, "owned setup test, 2 - player hand should be empty")
      assert(deployableList.contains(jmine), "owned setup test, 2 - deployable not appended to list")
      assert(avatar.deployables.Contains(jmine), "owned setup test, 2 - avatar does not own deployable")
    }
  }
}

class DeployableBehaviorDeconstructTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid = 1
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(jmine, number = 1)
  jmine.Faction = PlanetSideEmpire.TR
  jmine.Position = Vector3(1,2,3)
  jmine.Orientation = Vector3(4,5,6)

  "DeployableBehavior" should {
    "deconstruct, by self" in {
      zone.Deployables ! Zone.Deployable.Build(jmine)
      eventsProbe.receiveN(2, 10.seconds) //all of the messages from the construction (see other testing)
      assert(deployableList.contains(jmine), "deconstruct test - deployable not appended to list")

      jmine.Actor ! Deployable.Deconstruct()
      val eventsMsgs = eventsProbe.receiveN(2, 10.seconds)
      eventsMsgs.head match {
        case LocalServiceMessage("test", _, LocalAction.EliminateDeployable(_, PlanetSideGUID(1), Vector3(1,2,3), 2)) => ;
        case _ => assert(false, "deconstruct test - not eliminating deployable")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "TR", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(0))
          )
        ) => ;
        case _ => assert(false, "owned deconstruct test - not removing icon")
      }
      assert(!deployableList.contains(jmine), "deconstruct test - deployable not removed from list")
    }
  }
}

class DeployableBehaviorDeconstructOwnedTest extends FreedContextActorTest {
  val eventsProbe = new TestProbe(system)
  val avatar = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val player = Player(avatar) //guid=3
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid=1
  val citem = new ConstructionItem(GlobalDefinitions.ace) //guid = 2
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Players = List(avatar)
    override def LivePlayers = List(player)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(jmine, number = 1)
  guid.register(citem, number = 2)
  guid.register(player, number = 3)
  guid.register(avatar.locker, number = 4)
  jmine.Faction = PlanetSideEmpire.TR
  jmine.Position = Vector3(1,2,3)
  jmine.Orientation = Vector3(4,5,6)
  jmine.AssignOwnership(player)
  avatar.deployables.UpdateMaxCounts(Set(Certification.CombatEngineering, Certification.AssaultEngineering))
  player.Zone = zone
  player.Slot(slot = 0).Equipment = citem
  player.Actor = system.actorOf(Props(classOf[PlayerControl], player, null), name = "deployable-test-player-control")

  "DeployableBehavior" should {
    "deconstruct and alert owner" in {
      zone.Deployables ! Zone.Deployable.BuildByOwner(jmine, player, citem)
      eventsProbe.receiveN(7, 10.seconds)
      assert(deployableList.contains(jmine), "owned deconstruct test - deployable not appended to list")
      assert(avatar.deployables.Contains(jmine), "owned deconstruct test - avatar does not own deployable")

      jmine.Actor ! Deployable.Deconstruct()
      val eventsMsgs = eventsProbe.receiveN(3, 10.seconds)
      eventsMsgs.head match {
        case LocalServiceMessage("test", _, LocalAction.EliminateDeployable(mine, ValidPlanetSideGUID(1), Vector3(1.0,2.0,3.0),2))
          if mine eq jmine => ;
        case _ => assert(false, "owned deconstruct test - not eliminating deployable")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "TR", _,
          LocalAction.DeployableMapIcon(
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(0))
          )
        ) => ;
        case _ => assert(false, "owned deconstruct test - not removing icon")
      }
      eventsMsgs(2) match {
        case LocalServiceMessage("TestCharacter1", _, LocalAction.DeployableUIFor(DeployedItem.jammer_mine)) => ;
        case _ => assert(false, "")
      }

      assert(deployableList.isEmpty, "owned deconstruct test - deployable still in list")
      assert(!avatar.deployables.Contains(jmine), "owned deconstruct test - avatar still owns deployable")
    }
  }
}

object DeployableBehaviorTest {
  //...
}
