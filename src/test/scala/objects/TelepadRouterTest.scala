// Copyright (c) 2021 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.ce.{DeployableCategory, DeployedItem, TelepadLike}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vehicles.{Utility, UtilityType, VehicleControl}
import net.psforever.objects.zones.{Zone, ZoneDeployableActor, ZoneMap}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{DriveState, PlanetSideGUID, Vector3}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class TelepadDeployableNoRouterTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val telepad = Deployables.Make(DeployedItem.router_telepad_deployable)() //guid=1
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
  }
  guid.register(telepad, number = 1)

  "TelepadDeployable" should {
    "fail to activate without a router" in {
      assert(deployableList.isEmpty, "no-router telepad deployable test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = eventsProbe.receiveN(4, 10.seconds)
      eventsMsgs.head match {
        case AvatarServiceMessage("test", AvatarAction.DeployItem(PlanetSideGUID(0), obj)) =>
          assert(obj eq telepad, "no-router telepad deployable testt - not same telepad")
        case _ =>
          assert( false, "no-router telepad deployable test - wrong deploy message")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "no-router telepad deployable test - no icon or wrong icon")
      }
      eventsMsgs(2) match {
        case LocalServiceMessage("test", LocalAction.EliminateDeployable(`telepad`, PlanetSideGUID(1), Vector3.Zero, 2)) => ;
        case _ => assert(false, "no-router telepad deployable test - not eliminating deployable")
      }
      eventsMsgs(3) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "no-router telepad deployable test - no icon or wrong icon cleared")
      }
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
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)
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

      val eventsMsgs = eventsProbe.receiveN(4, 10.seconds)
      eventsMsgs.head match {
        case AvatarServiceMessage("test", AvatarAction.DeployItem(PlanetSideGUID(0), obj)) =>
          assert(obj eq telepad, "no-activate telepad deployable testt - not same telepad")
        case _ =>
          assert( false, "no-activate telepad deployable test - wrong deploy message")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ =>
          assert( false, "no-activate telepad deployable test - no icon or wrong icon")
      }
      eventsMsgs(2) match {
        case LocalServiceMessage("test", LocalAction.EliminateDeployable(`telepad`, PlanetSideGUID(1), Vector3.Zero, 2)) => ;
        case _ => assert(false, "no-activate telepad deployable test - not eliminating deployable")
      }
      eventsMsgs(3) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Dismiss,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "no-activate telepad deployable test - no icon or wrong icon")
      }
      routerProbe.expectNoMessage(100.millisecond)
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
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)
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

      val eventsMsgs = eventsProbe.receiveN(2, 10.seconds)
      val routerMsgs = routerProbe.receiveN(1, 10.seconds)
      eventsMsgs.head match {
        case AvatarServiceMessage("test", AvatarAction.DeployItem(PlanetSideGUID(0), obj)) =>
          assert(obj eq telepad, "link attempt telepad deployable testt - not same telepad")
        case _ =>
          assert( false, "link attempt telepad deployable test - wrong deploy message")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "link attempt telepad deployable test - no icon or wrong icon")
      }
      routerMsgs.head match {
        case TelepadLike.RequestLink(tpad) if tpad eq telepad => ;
        case _ => assert(false, "link attempt telepad deployable test - did not try to link")
      }
      assert(deployableList.contains(telepad), "link attempt telepad deployable test - deployable list is not empty")
    }
  }
}

class TelepadDeployableResponseFromRouterTest extends ActorTest {
  val eventsProbe = new TestProbe(system)
  val telepad = new TelepadDeployable(TelepadRouterTest.router_telepad_deployable) //guid=1
  val router = Vehicle(GlobalDefinitions.router) //guid=2
  val internal = router
    .Utility(UtilityType.internal_router_telepad_deployable)
    .get
    .asInstanceOf[Utility.InternalTelepad] //guid=3
  val deployableList = new ListBuffer()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
    private val deployables = system.actorOf(Props(classOf[ZoneDeployableActor], this, deployableList), name = "test-zone-deployables")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = eventsProbe.ref
    override def LocalEvents: ActorRef = eventsProbe.ref
    override def VehicleEvents: ActorRef = eventsProbe.ref
    override def Deployables: ActorRef = deployables
    override def Vehicles: List[Vehicle] = List(router)
  }
  guid.register(telepad, number = 1)
  guid.register(router, number = 2)
  guid.register(internal, number = 3)
  guid.register(router.Utility(UtilityType.teleportpad_terminal).get, number = 4) //necessary
  router.Zone = zone
  router.Actor = system.actorOf(Props(classOf[VehicleControl], router), "test-router")
  telepad.Router = PlanetSideGUID(2) //artificial

  "TelepadDeployable" should {
    "link with a connected router" in {
      assert(!telepad.Active, "link to router test - telepad active earlier than intended (1)")
      assert(!internal.Active, "link to router test - router internals active earlier than intended")
      router.Actor.tell(Deployment.TryDeploy(DriveState.Deploying), new TestProbe(system).ref)
      eventsProbe.receiveN(10, 10.seconds) //flush all messages related to deployment
      assert(!telepad.Active, "link to router test - telepad active earlier than intended (2)")
      assert(internal.Active, "link to router test - router internals active not active when expected")

      assert(deployableList.isEmpty, "link to router test - deployable list is not empty")
      zone.Deployables ! Zone.Deployable.Build(telepad)

      val eventsMsgs = eventsProbe.receiveN(9, 10.seconds)
      eventsMsgs.head match {
        case AvatarServiceMessage("test", AvatarAction.DeployItem(PlanetSideGUID(0), obj)) =>
          assert(obj eq telepad, "link to router test - not same telepad")
        case _ =>
          assert( false, "link to router test - wrong deploy message")
      }
      eventsMsgs(1) match {
        case LocalServiceMessage(
          "NEUTRAL",
          LocalAction.DeployableMapIcon(
            PlanetSideGUID(0),
            DeploymentAction.Build,
            DeployableInfo(PlanetSideGUID(1), DeployableIcon.RouterTelepad, Vector3.Zero, PlanetSideGUID(0))
          )
        )      => ;
        case _ => assert(false, "link to router test - no icon or wrong icon")
      }
      eventsMsgs(2) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(
            ObjectCreateMessage(_, 744, PlanetSideGUID(3), Some(ObjectCreateMessageParent(PlanetSideGUID(2), 2)), _)
          )
        ) => ;
        case _ => assert(false, "link to router test - did not create the internal router telepad (1)")
      }
      eventsMsgs(3) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(3), 27))
        ) => ;
        case _ => assert(false, "link to router test - did not create the internal router telepad (2)")
      }
      eventsMsgs(4) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(3), 30))
        ) => ;
        case _ => assert(false, "link to router test - did not create the internal router telepad (3)")
      }
      eventsMsgs(5) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(3), 27))
        ) => ;
        case _ => assert(false, "link to router test - did not link the internal telepad (1)")
      }
      eventsMsgs(6) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(3), 28))
        ) => ;
        case _ => assert(false, "link to router test - did not link the internal telepad (2)")
      }
      eventsMsgs(7) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(1), 27))
        ) => ;
        case _ => assert(false, "link to router test - did not link the telepad (1)")
      }
      eventsMsgs(8) match {
        case LocalServiceMessage(
          "test",
          LocalAction.SendResponse(GenericObjectActionMessage(PlanetSideGUID(1), 28))
        ) => ;
        case _ => assert(false, "link to router test - did not link the telepad (2)")
      }
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
