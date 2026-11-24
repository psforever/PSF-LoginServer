// Copyright (c) 2021 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.{ActorTest, EventEnvelopeAssertions, FreedContextActorTest}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{Avatar, Certification, PlayerControl}
import net.psforever.objects.{ConstructionItem, Deployables, GlobalDefinitions, Player}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.zones.{Zone, ZoneDeployableActor, ZoneMap}
import net.psforever.packet.game.packets.{DeployOutcome, DeployableIcon, DeployableInfo, DeploymentAction, GenericObjectActionMessage, ObjectDeployedMessage}
import net.psforever.services.base.message.ObjectDelete
import net.psforever.services.local.LocalAction
import net.psforever.types._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class DeployableBehaviorSetupTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val jmine = Deployables.Make(DeployedItem.jammer_mine)() //guid=1
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
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

      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 2.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq jmine
          case _                                        => false
        },
        s"self-setup test - wrong deploy message: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "TR",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"self-setup test - no icon or wrong icon: $eventsMsgs"
      )
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
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
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
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
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
      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 2.seconds)
      assert(
        EventEnvelopeAssertions.containsPacket(eventsMsgs) {
          case ObjectDeployedMessage(0, "jammer_mine", DeployOutcome.Success, 1, 20) => true
          case _                                                                     => false
        },
        s"owned setup test, 2 - did not receive build confirmation: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("TestCharacter1", _, LocalAction.DeployableUIFor(DeployedItem.jammer_mine)) => true
          case _                                                                             => false
        },
        s"owned setup test, 2 - did not receive ui update: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "test",
                PlanetSideGUID(3),
                LocalAction.TriggerEffectLocation("spawn_object_effect", Vector3(1, 2, 3), Vector3(4, 5, 6))
              ) =>
            true
          case _ => false
        },
        s"owned setup test, 2 - no spawn fx: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq jmine
          case _                                        => false
        },
        s"owned setup test, 2 - wrong deploy message: $eventsMsgs"
      )
      //the message order can be jumbled from here-on
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "TR",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(3))
                )
              ) =>
            true
          case _ => false
        },
        s"owned setup test, 2 - no icon or wrong icon: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.containsPacket(eventsMsgs) {
          case GenericObjectActionMessage(PlanetSideGUID(1), 21) => true
          case _                                                  => false
        },
        s"owned setup test, 2 - build action not reset (GOAM21): $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, ObjectDelete(PlanetSideGUID(2), 0)) => true
          case _                                               => false
        },
        s"owned setup test, 2 - construction tool not deleted: $eventsMsgs"
      )
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
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
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
      EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 2.seconds) //all of the messages from the construction (see other testing)
      assert(deployableList.contains(jmine), "deconstruct test - deployable not appended to list")

      jmine.Actor ! Deployable.Deconstruct()
      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 3.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.EliminateDeployable(_, PlanetSideGUID(1), Vector3(1, 2, 3), 2)) => true
          case _                                                                                       => false
        },
        s"deconstruct test - not eliminating deployable: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "TR",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Dismiss,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"owned deconstruct test - not removing icon: $eventsMsgs"
      )
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
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
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
      EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 2.seconds)
      assert(deployableList.contains(jmine), "owned deconstruct test - deployable not appended to list")
      assert(avatar.deployables.Contains(jmine), "owned deconstruct test - avatar does not own deployable")

      jmine.Actor ! Deployable.Deconstruct()
      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 10.seconds, 3.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.EliminateDeployable(mine, ValidPlanetSideGUID(1), Vector3(1.0, 2.0, 3.0), 2)) =>
            mine eq jmine
          case _ => false
        },
        s"owned deconstruct test - not eliminating deployable: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "TR",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Dismiss,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.DisruptorMine, Vector3(1, 2, 3), PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"owned deconstruct test - not removing icon: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("TestCharacter1", _, LocalAction.DeployableUIFor(DeployedItem.jammer_mine)) => true
          case _                                                                             => false
        },
        s"owned deconstruct test - missing ui update: $eventsMsgs"
      )

      assert(deployableList.isEmpty, "owned deconstruct test - deployable still in list")
      assert(!avatar.deployables.Contains(jmine), "owned deconstruct test - avatar still owns deployable")
    }
  }
}

object DeployableBehaviorTest {
  //...
}
