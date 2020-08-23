// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types.{DriveState, PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable.Specification
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration.Duration

class DeploymentTest extends Specification {
  "Deployment" should {
    "construct" in {
      val obj = new DeploymentTest.DeploymentObject()
      obj.DeploymentState mustEqual DriveState.Mobile
      obj.DeployTime mustEqual 0
      obj.UndeployTime mustEqual 0
    }

    "change deployment state" in {
      val obj = new DeploymentTest.DeploymentObject()
      obj.DeploymentState mustEqual DriveState.Mobile

      obj.DeploymentState = DriveState.Deployed
      obj.DeploymentState mustEqual DriveState.Deployed
      obj.DeploymentState = DriveState.Deploying
      obj.DeploymentState mustEqual DriveState.Deploying
      obj.DeploymentState = DriveState.Undeploying
      obj.DeploymentState mustEqual DriveState.Undeploying
      obj.DeploymentState = DriveState.State7
      obj.DeploymentState mustEqual DriveState.State7
    }

    "have custom deployment time by object" in {
      val ams = Vehicle(GlobalDefinitions.ams)
      (ams.DeployTime == 0) mustEqual false   //not default
      (ams.UndeployTime == 0) mustEqual false //not default
    }
  }
}

class DeploymentBehavior1Test extends ActorTest {
  "Deployment" should {
    "construct" in {
      val obj = DeploymentTest.SetUpAgent
      assert(obj.Actor != ActorRef.noSender)
      assert(obj.DeploymentState == DriveState.Mobile)
    }
  }
}

class DeploymentBehavior2Test extends ActorTest {
  "Deployment" should {
    "change following a deployment cycle using TryDeploymentChange" in {
      val obj         = DeploymentTest.SetUpAgent
      val probe       = new TestProbe(system)
      val eventsProbe = new TestProbe(system)
      obj.Zone.VehicleEvents = eventsProbe.ref
      assert(obj.DeploymentState == DriveState.Mobile)
      //to Deploying
      obj.Actor.tell(Deployment.TryDeploymentChange(DriveState.Deploying), probe.ref)
      val reply1a = probe.receiveOne(Duration.create(500, "ms"))
      assert(reply1a match {
        case Deployment.CanDeploy(_, DriveState.Deploying) => true
        case _                                             => false
      })
      val reply1b = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply1b match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Deploying, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      //to Deployed
      val reply2 = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply2 match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Deployed, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      assert(obj.DeploymentState == DriveState.Deployed)
      //to Undeploying
      obj.Actor.tell(Deployment.TryDeploymentChange(DriveState.Undeploying), probe.ref)
      val reply3a = probe.receiveOne(Duration.create(500, "ms"))
      assert(reply3a match {
        case Deployment.CanUndeploy(_, DriveState.Undeploying) => true
        case _                                                 => false
      })
      val reply3b = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply3b match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Undeploying, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      //to Mobile
      val reply4 = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply4 match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Mobile, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      assert(obj.DeploymentState == DriveState.Mobile)
    }
  }
}

class DeploymentBehavior3Test extends ActorTest {
  "Deployment" should {
    "change following a deployment cycle using TryDeploy and TryUndeploy" in {
      val obj         = DeploymentTest.SetUpAgent
      val probe       = new TestProbe(system)
      val eventsProbe = new TestProbe(system)
      obj.Zone.VehicleEvents = eventsProbe.ref
      assert(obj.DeploymentState == DriveState.Mobile)
      //to Deploying
      obj.Actor.tell(Deployment.TryDeploy(DriveState.Deploying), probe.ref)
      val reply1a = probe.receiveOne(Duration.create(500, "ms"))
      assert(reply1a match {
        case Deployment.CanDeploy(_, DriveState.Deploying) => true
        case _                                             => false
      })
      val reply1b = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply1b match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Deploying, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      //to Deployed
      val reply2 = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply2 match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Deployed, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      assert(obj.DeploymentState == DriveState.Deployed)
      //to Undeploying
      obj.Actor.tell(Deployment.TryUndeploy(DriveState.Undeploying), probe.ref)
      val reply3a = probe.receiveOne(Duration.create(500, "ms"))
      assert(reply3a match {
        case Deployment.CanUndeploy(_, DriveState.Undeploying) => true
        case _                                                 => false
      })
      val reply3b = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply3b match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Undeploying, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      //to Mobile
      val reply4 = eventsProbe.receiveOne(Duration.create(500, "ms"))
      assert(reply4 match {
        case VehicleServiceMessage(
              "test",
              VehicleAction.DeployRequest(_, PlanetSideGUID(1), DriveState.Mobile, 0, false, Vector3.Zero)
            ) =>
          true
        case _ => false
      })
      assert(obj.DeploymentState == DriveState.Mobile)
    }
  }
}

class DeploymentBehavior4Test extends ActorTest {
  "Deployment" should {
    "not deploy to an out of order state" in {
      val obj = DeploymentTest.SetUpAgent
      assert(obj.DeploymentState == DriveState.Mobile)

      obj.Actor ! Deployment.TryDeploymentChange(DriveState.Deployed)
      val reply1 = receiveOne(Duration.create(100, "ms"))
      assert(reply1.isInstanceOf[Deployment.CanNotChangeDeployment])
      assert(reply1.asInstanceOf[Deployment.CanNotChangeDeployment].obj == obj)
      assert(reply1.asInstanceOf[Deployment.CanNotChangeDeployment].to_state == DriveState.Deployed)
      assert(obj.DeploymentState == DriveState.Mobile)

      obj.Actor ! Deployment.TryDeploy(DriveState.Deployed)
      val reply2 = receiveOne(Duration.create(100, "ms"))
      assert(reply2.isInstanceOf[Deployment.CanNotChangeDeployment])
      assert(reply2.asInstanceOf[Deployment.CanNotChangeDeployment].obj == obj)
      assert(reply2.asInstanceOf[Deployment.CanNotChangeDeployment].to_state == DriveState.Deployed)
      assert(obj.DeploymentState == DriveState.Mobile)
    }
  }
}

class DeploymentBehavior5Test extends ActorTest {
  "Deployment" should {
    "not deploy to an undeploy state" in {
      val obj = DeploymentTest.SetUpAgent
      assert(obj.DeploymentState == DriveState.Mobile)
      obj.Actor ! Deployment.TryDeploymentChange(DriveState.Deploying)
      receiveOne(Duration.create(100, "ms")) //consume
      obj.Actor ! Deployment.TryDeploymentChange(DriveState.Deployed)
      receiveOne(Duration.create(100, "ms")) //consume
      assert(obj.DeploymentState == DriveState.Deployed)

      obj.Actor ! Deployment.TryDeploy(DriveState.Undeploying)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Deployment.CanNotChangeDeployment])
      assert(reply.asInstanceOf[Deployment.CanNotChangeDeployment].obj == obj)
      assert(reply.asInstanceOf[Deployment.CanNotChangeDeployment].to_state == DriveState.Undeploying)
      assert(obj.DeploymentState == DriveState.Deployed)
    }
  }
}

class DeploymentBehavior6Test extends ActorTest {
  "Deployment" should {
    "not undeploy to a deploy state" in {
      val obj = DeploymentTest.SetUpAgent
      assert(obj.DeploymentState == DriveState.Mobile)

      obj.Actor ! Deployment.TryUndeploy(DriveState.Deploying)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Deployment.CanNotChangeDeployment])
      assert(reply.asInstanceOf[Deployment.CanNotChangeDeployment].obj == obj)
      assert(reply.asInstanceOf[Deployment.CanNotChangeDeployment].to_state == DriveState.Deploying)
      assert(obj.DeploymentState == DriveState.Mobile)
    }
  }
}

object DeploymentTest {
  class DeploymentObject extends PlanetSideServerObject with Deployment {
    def Faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
    def Definition                      = null
  }

  private class DeploymentControl(obj: Deployment.DeploymentObject) extends Actor with DeploymentBehavior {
    override def DeploymentObject = obj
    def receive                   = deployBehavior.orElse { case _ => }
  }

  def SetUpAgent(implicit system: ActorSystem) = {
    val obj = new DeploymentObject()
    obj.GUID = PlanetSideGUID(1)
    obj.Zone = Zone("test", new ZoneMap("test"), 1)
    obj.Actor = system.actorOf(Props(classOf[DeploymentControl], obj), "test")
    obj
  }
}
