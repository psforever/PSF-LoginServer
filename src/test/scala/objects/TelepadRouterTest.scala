// Copyright (c) 2021 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestProbe
import base.{ActorTest, EventEnvelopeAssertions, FreedContextActorTest}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects._
import net.psforever.objects.ce.{DeployableCategory, DeployedItem, TelepadLike}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.objects.zones.{Zone, ZoneDeployableActor, ZoneMap}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game.packets.{DeployableIcon, DeployableInfo, DeploymentAction, GenericObjectActionMessage, ObjectCreateMessage}
import net.psforever.services.local.LocalAction
import net.psforever.types.{DriveState, PlanetSideGUID, Vector3}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class TelepadDeployableNoRouterTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val telepad = Deployables.Make(DeployedItem.router_telepad_deployable)() //guid=1
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
  guid.register(telepad, number = 1)

  "TelepadDeployable" should {
    "fail to activate without a router" in {
      assert(deployableList.isEmpty, "no-router telepad deployable test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 12.seconds, 3.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq telepad
          case _                                        => false
        },
        s"no-router telepad deployable test - wrong deploy message: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"no-router telepad deployable test - no icon or wrong icon: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.EliminateDeployable(obj, PlanetSideGUID(1), Vector3.Zero, 2)) =>
            obj eq telepad
          case _ => false
        },
        s"no-router telepad deployable test - not eliminating deployable: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Dismiss,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"no-router telepad deployable test - no icon or wrong icon cleared: $eventsMsgs"
      )
      assert(deployableList.isEmpty, "no-router telepad deployable test - deployable is being tracked")
    }
  }
}

class TelepadDeployableNoActivationTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val routerProbe = new TestProbe(system)
  val telepad = Deployables.Make(DeployedItem.router_telepad_deployable)() //guid=1
  val router = Vehicle(GlobalDefinitions.router) //guid=2
  val internal = router.Utility(UtilityType.internal_router_telepad_deployable).get //guid=3
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(telepad, number = 1)
  guid.register(router, number = 2)
  guid.register(internal, number = 3)
  router.Actor = eventsProbe.ref
  internal.Actor = routerProbe.ref

  "TelepadDeployable" should {
    "fail to activate without a connected router" in {
      assert(deployableList.isEmpty, "no-activate telepad deployable test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 12.seconds, 3.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq telepad
          case _                                        => false
        },
        s"no-activate telepad deployable test - wrong deploy message: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"no-activate telepad deployable test - no icon or wrong icon: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.EliminateDeployable(obj, PlanetSideGUID(1), Vector3.Zero, 2)) =>
            obj eq telepad
          case _ => false
        },
        s"no-activate telepad deployable test - not eliminating deployable: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Dismiss,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"no-activate telepad deployable test - no icon or wrong icon: $eventsMsgs"
      )
      routerProbe.expectNoMessage(500.milliseconds)
      assert(deployableList.isEmpty, "no-activate telepad deployable test - deployable is being tracked")
    }
  }
}

class TelepadDeployableAttemptTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val routerProbe = new TestProbe(system)
  val telepad = new TelepadDeployable(TelepadRouterTest.router_telepad_deployable) //guid=1
  val router = Vehicle(GlobalDefinitions.router) //guid=2
  val internal = router.Utility(UtilityType.internal_router_telepad_deployable).get //guid=3
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(telepad, number = 1)
  guid.register(router, number = 2)
  guid.register(internal, number = 3)
  router.Actor = eventsProbe.ref
  internal.Actor = routerProbe.ref
  telepad.Router = PlanetSideGUID(2) //artificial

  "TelepadDeployable" should {
    "attempt to link with a connected router" in {
      assert(deployableList.isEmpty, "link attempt telepad deployable test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 12.seconds, 3.seconds)
      val routerMsgs = EventEnvelopeAssertions.drain(routerProbe, 10.seconds, 2.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq telepad
          case _                                        => false
        },
        s"link attempt telepad deployable test - wrong deploy message: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"link attempt telepad deployable test - no icon or wrong icon: $eventsMsgs"
      )
      assert(
        routerMsgs.exists {
          case TelepadLike.RequestLink(tpad) => tpad eq telepad
          case _                             => false
        },
        s"link attempt telepad deployable test - did not try to link: $routerMsgs"
      )
      assert(deployableList.contains(telepad), "link attempt telepad deployable test - deployable list is not empty")
    }
  }
}

class TelepadDeployableResponseFromRouterTest extends FreedContextActorTest {
  val eventsProbe = new TestProbe(system)
  val telepad = new TelepadDeployable(TelepadRouterTest.router_telepad_deployable) //guid=1
  val router = Vehicle(GlobalDefinitions.router) //guid=2
  val internal = router
    .Utility(UtilityType.internal_router_telepad_deployable)
    .get
    .asInstanceOf[Utility.InternalTelepad] //guid=3
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(zoneId = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList, mutable.HashMap[Int, Int]()), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def VehicleEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)

    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  guid.register(telepad, number = 1)
  guid.register(router, number = 2)
  guid.register(internal, number = 3)
  guid.register(router.Utility(UtilityType.teleportpad_terminal).get, number = 4) //necessary
  router.Zone = zone
  router.Definition.Initialize(router, context)
  telepad.Router = PlanetSideGUID(2) //artificial

  "TelepadDeployable" should {
    "link with a connected router" in {
      assert(!telepad.Active, "link to router test - telepad active earlier than intended (1)")
      assert(!internal.Active, "link to router test - router internals active earlier than intended")
      val deploymentProbe = new TestProbe(system)
      router.Actor.tell(Deployment.TryDeploy(DriveState.Deploying), deploymentProbe.ref)
      eventsProbe.receiveN(10, 10.seconds) //flush all messages related to deployment
      deploymentProbe.receiveN(2, 10.seconds) //CanDeploy
      deploymentProbe.expectNoMessage(2.seconds) //intentional delay
      assert(internal.Active, "link to router test - router internals not active when expected")
      assert(!telepad.Active, "link to router test - telepad active earlier than intended (2)")

      assert(deployableList.isEmpty, "link to router test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = EventEnvelopeAssertions.drain(eventsProbe, 12.seconds, 3.seconds)
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case ("test", _, LocalAction.DeployItem(obj)) => obj eq telepad
          case _                                        => false
        },
        s"link to router test - wrong deploy message: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.contains(eventsMsgs) {
          case (
                "NEUTRAL",
                _,
                LocalAction.DeployableMapIcon(
                  DeploymentAction.Build,
                  DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
                )
              ) =>
            true
          case _ => false
        },
        s"link to router test - no icon or wrong icon: $eventsMsgs"
      )
      assert(
        EventEnvelopeAssertions.containsPacket(eventsMsgs) {
          case ObjectCreateMessage(_, 744, PlanetSideGUID(3), Some(ObjectCreateMessageParent(PlanetSideGUID(2), 2)), _) =>
            true
          case _ => false
        },
        s"link to router test - did not create the internal router telepad: $eventsMsgs"
      )
      def containsAction(guid: PlanetSideGUID, action: Int): Boolean =
        EventEnvelopeAssertions.containsPacket(eventsMsgs) {
          case GenericObjectActionMessage(`guid`, `action`) => true
          case _                                            => false
        }
      assert(containsAction(PlanetSideGUID(3), 27), s"link to router test - did not create the internal router telepad: $eventsMsgs")
      assert(containsAction(PlanetSideGUID(3), 30), s"link to router test - did not create the internal router telepad: $eventsMsgs")
      assert(containsAction(PlanetSideGUID(3), 28), s"link to router test - did not link the internal telepad: $eventsMsgs")
      assert(containsAction(PlanetSideGUID(1), 27), s"link to router test - did not link the telepad: $eventsMsgs")
      assert(containsAction(PlanetSideGUID(1), 28), s"link to router test - did not link the telepad: $eventsMsgs")
      assert(telepad.Active, "link to router test - telepad not active when expected")
      assert(internal.Active, "link to router test - router internals active not active when expected (2)")
      assert(deployableList.contains(telepad), "link to router test - deployable list is not empty")
    }
  }
}

object TelepadRouterTest {
  val router_telepad_deployable = new TelepadDeployableDefinition(DeployedItem.router_telepad_deployable.id) {
    Name = "test_telepad_dep"
    DeployTime = Duration.create(1, "ms")
    DeployCategory = DeployableCategory.Telepads
    linkTime = 1.second
  }
}
