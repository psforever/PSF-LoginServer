// Copyright (c) 2017 PSForever
package service

import akka.actor.Props
import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import net.psforever.services.local.support.RouterTelepadActivation
import net.psforever.services.support.SupportActor

import scala.concurrent.duration._

class RouterTelepadActivationTest extends ActorTest {
  "RouterTelepadActivation" should {
    "construct" in {
      system.actorOf(Props[RouterTelepadActivation](), "activation-test-actor")
    }
  }
}

class RouterTelepadActivationSimpleTest extends ActorTest {
  "RouterTelepadActivation" should {
    "handle a task" in {
      val telepad = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad.GUID = PlanetSideGUID(1)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad, Zone.Nowhere, Some(2 seconds))
      expectMsg(3 seconds, RouterTelepadActivation.ActivateTeleportSystem(telepad, Zone.Nowhere))
    }
  }
}

class RouterTelepadActivationComplexTest extends ActorTest {
  "RouterTelepadActivation" should {
    "handle multiple tasks" in {
      val telepad1 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad1.GUID = PlanetSideGUID(1)
      val telepad2 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad2.GUID = PlanetSideGUID(2)
      val telepad3 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad3.GUID = PlanetSideGUID(3)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad1, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad2, Zone.Nowhere, Some(3 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad3, Zone.Nowhere, Some(1 seconds))
      val msgs = receiveN(3, 5 seconds) //organized by duration
      assert(msgs.head.isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs.head.asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad3)
      assert(msgs(1).isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs(1).asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad1)
      assert(msgs(2).isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs(2).asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad2)
    }
  }
}

class RouterTelepadActivationHurryTest extends ActorTest {
  "RouterTelepadActivation" should {
    "hurry specific tasks" in {
      val telepad1 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad1.GUID = PlanetSideGUID(1)
      val telepad2 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad2.GUID = PlanetSideGUID(2)
      val telepad3 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad3.GUID = PlanetSideGUID(3)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad1, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad2, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad3, Zone.Nowhere, Some(2 seconds))
      obj ! SupportActor.HurrySpecific(List(telepad1, telepad2), Zone.Nowhere)
      val msgs = receiveN(2, 1 seconds)
      assert(msgs.head.isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs.head.asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad1)
      assert(msgs(1).isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs(1).asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad2)
      val last = receiveOne(3 seconds)
      assert(last.isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(last.asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad3)
    }
  }
}

class RouterTelepadActivationHurryAllTest extends ActorTest {
  "RouterTelepadActivation" should {
    "hurry all tasks" in {
      val telepad1 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad1.GUID = PlanetSideGUID(1)
      val telepad2 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad2.GUID = PlanetSideGUID(2)
      val telepad3 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad3.GUID = PlanetSideGUID(3)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad1, Zone.Nowhere, Some(7 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad2, Zone.Nowhere, Some(5 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad3, Zone.Nowhere, Some(6 seconds))
      obj ! SupportActor.HurryAll()
      val msgs =
        receiveN(
          3,
          4 seconds
        ) //organized by duration; note: all messages received before the earliest task should be performed
      assert(msgs.head.isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs.head.asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad2)
      assert(msgs(1).isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs(1).asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad3)
      assert(msgs(2).isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs(2).asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad1)
    }
  }
}

class RouterTelepadActivationClearTest extends ActorTest {
  "RouterTelepadActivation" should {
    "clear specific tasks" in {
      val telepad1 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad1.GUID = PlanetSideGUID(1)
      val telepad2 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad2.GUID = PlanetSideGUID(2)
      val telepad3 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad3.GUID = PlanetSideGUID(3)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad1, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad2, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad3, Zone.Nowhere, Some(2 seconds))
      obj ! SupportActor.ClearSpecific(List(telepad1, telepad2), Zone.Nowhere)
      val msgs = receiveN(1, 3 seconds) //should only receive telepad3
      assert(msgs.head.isInstanceOf[RouterTelepadActivation.ActivateTeleportSystem])
      assert(msgs.head.asInstanceOf[RouterTelepadActivation.ActivateTeleportSystem].telepad == telepad3)
    }
  }
}

class RouterTelepadActivationClearAllTest extends ActorTest {
  "RouterTelepadActivation" should {
    "clear all tasks" in {
      val telepad1 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad1.GUID = PlanetSideGUID(1)
      val telepad2 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad2.GUID = PlanetSideGUID(2)
      val telepad3 = new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      telepad3.GUID = PlanetSideGUID(3)
      val obj = system.actorOf(
        Props(classOf[ActorTest.SupportActorInterface], Props[RouterTelepadActivation](), self),
        "activation-test-actor"
      )

      obj ! RouterTelepadActivation.AddTask(telepad1, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad2, Zone.Nowhere, Some(2 seconds))
      obj ! RouterTelepadActivation.AddTask(telepad3, Zone.Nowhere, Some(2 seconds))
      obj ! SupportActor.ClearAll()
      expectNoMessage(4 seconds)
    }
  }
}
