// Copyright (c) 2017 PSForever
package service

import akka.actor.{ActorRef, Props}
import akka.routing.RandomPool
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Tool}
import net.psforever.objects.definition.{EquipmentDefinition, ObjectDefinition}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.PlanetSideGUID
import services.{RemoverActor, ServiceManager}

import scala.concurrent.duration._

//class StandardRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//
//  "RemoverActor" should {
//    "handle a simple task" in {
//      expectNoMsg(500 milliseconds)
//      val remover = system.actorOf(
//        Props(classOf[ActorTest.SupportActorInterface], Props[RemoverActorTest.TestRemover], self),
//        "test-remover"
//      )
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere)
//
//      val reply1 = receiveOne(500 milliseconds)
//      assert(reply1.isInstanceOf[RemoverActorTest.InclusionTestAlert])
//      val reply2 = receiveOne(500 milliseconds)
//      assert(reply2.isInstanceOf[RemoverActorTest.InitialJobAlert])
//      expectNoMsg(1 seconds) //delay
//      val reply3 = receiveOne(500 milliseconds)
//      assert(reply3.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4 = receiveOne(500 milliseconds)
//      assert(reply4.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5 = receiveOne(500 milliseconds)
//      assert(reply5.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6 = receiveOne(500 milliseconds)
//      assert(reply6.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7 = receiveOne(500 milliseconds)
//      assert(reply7.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}

//class DelayedRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//
//  "RemoverActor" should {
//    "handle a simple task (timed)" in {
//      expectNoMsg(500 milliseconds)
//      val remover = system.actorOf(
//        Props(classOf[ActorTest.SupportActorInterface], Props[RemoverActorTest.TestRemover], self),
//        "test-remover"
//      )
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(100 milliseconds))
//
//      val reply1 = receiveOne(500 milliseconds)
//      assert(reply1.isInstanceOf[RemoverActorTest.InclusionTestAlert])
//      val reply2 = receiveOne(500 milliseconds)
//      assert(reply2.isInstanceOf[RemoverActorTest.InitialJobAlert])
//      //no delay
//      val reply3 = receiveOne(500 milliseconds)
//      assert(reply3.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4 = receiveOne(500 milliseconds)
//      assert(reply4.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5 = receiveOne(500 milliseconds)
//      assert(reply5.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6 = receiveOne(500 milliseconds)
//      assert(reply6.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7 = receiveOne(500 milliseconds)
//      assert(reply7.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}

//class ExcludedRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  val AlternateTestObject = new PlanetSideGameObject() { def Definition = new ObjectDefinition(0) { } }
//
//  "RemoverActor" should {
//    "allow only specific objects" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(
//        Props(classOf[ActorTest.SupportActorInterface], Props[RemoverActorTest.TestRemover], self),
//        "test-remover"
//      )
//      remover ! RemoverActor.AddTask(AlternateTestObject, Zone.Nowhere)
//
//      val reply1 = probe.receiveOne(200 milliseconds)
//      assert(reply1.isInstanceOf[RemoverActorTest.InclusionTestAlert])
//      expectNoMsg(2 seconds)
//      //RemoverActor is stalled because it received an object that it was not allowed to act upon
//    }
//  }
//}

//class MultipleRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//
//  "RemoverActor" should {
//    "work on parallel tasks" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere)
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere)
//
//      val replies = probe.receiveN(14, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      var fja : Int = 0
//      var cta : Int = 0
//      var sja : Int = 0
//      var dta : Int = 0
//      var dtr : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case RemoverActorTest.FirstJobAlert() => fja += 1
//        case RemoverActorTest.ClearanceTestAlert() => cta += 1
//        case RemoverActorTest.SecondJobAlert() => sja += 1
//        case RemoverActorTest.DeletionTaskAlert() => dta += 1
//        case RemoverActorTest.DeletionTaskRunAlert() => dtr += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2 && fja == 2 && cta == 2 && sja == 2 && dta == 2 && dtr == 2)
//    }
//  }
//}
//
//class HurrySpecificRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//
//  "RemoverActor" should {
//    "be able to hurry certain tasks" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(10 minutes)) //TEN MINUTE WAIT
//
//      val reply1 = probe.receiveOne(200 milliseconds)
//      assert(reply1.isInstanceOf[RemoverActorTest.InclusionTestAlert])
//      val reply2 = probe.receiveOne(200 milliseconds)
//      assert(reply2.isInstanceOf[RemoverActorTest.InitialJobAlert])
//      probe.expectNoMsg(3 seconds) //long delay, longer than standard but not yet 10 minutes
//      remover ! RemoverActor.HurrySpecific(List(RemoverActorTest.TestObject), Zone.Nowhere) //hurried
//      val reply3 = probe.receiveOne(300 milliseconds)
//      assert(reply3.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4 = probe.receiveOne(300 milliseconds)
//      assert(reply4.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5 = probe.receiveOne(300 milliseconds)
//      assert(reply5.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6 = probe.receiveOne(500 milliseconds)
//      assert(reply6.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7 = probe.receiveOne(500 milliseconds)
//      assert(reply7.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}
//
//class HurrySelectionRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//
//  "RemoverActor" should {
//    "be able to hurry certain tasks, but let others finish normally" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(10 seconds))
//
//      val replies = probe.receiveN(4, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2)
//      probe.expectNoMsg(3 seconds) //long delay, longer than standard but not yet 5 seconds
//      remover ! RemoverActor.HurrySpecific(List(RemoverActorTest.TestObject), Zone.Nowhere) //hurried
//      //first
//      val reply3a = probe.receiveOne(300 milliseconds)
//      assert(reply3a.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4a = probe.receiveOne(300 milliseconds)
//      assert(reply4a.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5a = probe.receiveOne(300 milliseconds)
//      assert(reply5a.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6a = probe.receiveOne(500 milliseconds)
//      assert(reply6a.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7a = probe.receiveOne(500 milliseconds)
//      assert(reply7a.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//      //second
//      remover ! RemoverActor.HurrySpecific(List(TestObject2), Zone.Nowhere) //hurried
//      val reply3b = probe.receiveOne(300 milliseconds)
//      assert(reply3b.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4b = probe.receiveOne(300 milliseconds)
//      assert(reply4b.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5b = probe.receiveOne(300 milliseconds)
//      assert(reply5b.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6b = probe.receiveOne(500 milliseconds)
//      assert(reply6b.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7b = probe.receiveOne(500 milliseconds)
//      assert(reply7b.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}
//
//class HurryMultipleRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//  final val TestObject3 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(3) } }
//
//  "RemoverActor" should {
//    "be able to hurry certain tasks, but only valid ones" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(5 seconds))
//
//      val replies = probe.receiveN(4, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2)
//      probe.expectNoMsg(3 seconds) //long delay, longer than standard but not yet 5 seconds
//      remover ! RemoverActor.HurrySpecific(List(RemoverActorTest.TestObject, TestObject3), Zone.Nowhere) //multiple hurried, only one valid
//      //first
//      val reply3a = probe.receiveOne(300 milliseconds)
//      assert(reply3a.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4a = probe.receiveOne(300 milliseconds)
//      assert(reply4a.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5a = probe.receiveOne(300 milliseconds)
//      assert(reply5a.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6a = probe.receiveOne(500 milliseconds)
//      assert(reply6a.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7a = probe.receiveOne(500 milliseconds)
//      assert(reply7a.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//      //second
//      remover ! RemoverActor.HurrySpecific(List(TestObject2), Zone.Nowhere) //hurried
//      val reply3b = probe.receiveOne(300 milliseconds)
//      assert(reply3b.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4b = probe.receiveOne(300 milliseconds)
//      assert(reply4b.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5b = probe.receiveOne(300 milliseconds)
//      assert(reply5b.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6b = probe.receiveOne(500 milliseconds)
//      assert(reply6b.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7b = probe.receiveOne(500 milliseconds)
//      assert(reply7b.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}
//
//class HurryByZoneRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//  final val TestObject3 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(3) } }
//  final val zone = new Zone("test", new ZoneMap("test-map"), 11)
//
//  "RemoverActor" should {
//    "be able to hurry certain tasks by their zone" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, zone, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject3, Zone.Nowhere, Some(5 seconds))
//
//      val replies1 = probe.receiveN(6, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies1.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 3 && ija == 3)
//      probe.expectNoMsg(3 seconds) //long delay, longer than standard but not yet 5 seconds
//      remover ! RemoverActor.HurrySpecific(List(), Zone.Nowhere) //multiple hurried, only the two entries with Zone.Nowhere
//      //
//      val replies2 = probe.receiveN(10, 5 seconds)
//      var fja : Int = 0
//      var cta : Int = 0
//      var sja : Int = 0
//      var dta : Int = 0
//      var dtr : Int = 0
//      replies2.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case RemoverActorTest.FirstJobAlert() => fja += 1
//        case RemoverActorTest.ClearanceTestAlert() => cta += 1
//        case RemoverActorTest.SecondJobAlert() => sja += 1
//        case RemoverActorTest.DeletionTaskAlert() => dta += 1
//        case RemoverActorTest.DeletionTaskRunAlert() => dtr += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(fja == 2 && cta == 2 && sja == 2 && dta == 2 && dtr == 2)
//      //final
//      remover ! RemoverActor.HurrySpecific(List(), zone) //hurried
//      val reply3b = probe.receiveOne(300 milliseconds)
//      assert(reply3b.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4b = probe.receiveOne(300 milliseconds)
//      assert(reply4b.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5b = probe.receiveOne(300 milliseconds)
//      assert(reply5b.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6b = probe.receiveOne(500 milliseconds)
//      assert(reply6b.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7b = probe.receiveOne(500 milliseconds)
//      assert(reply7b.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//    }
//  }
//}
//
//class HurryAllRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//  final val TestObject3 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(3) } }
//
//  "RemoverActor" should {
//    "be able to hurry all tasks to completion" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(20 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(15 seconds))
//      remover ! RemoverActor.AddTask(TestObject3, Zone.Nowhere, Some(10 seconds))
//
//      val replies1 = probe.receiveN(6, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies1.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 3 && ija == 3)
//      probe.expectNoMsg(3 seconds) //long delay, longer than standard but not yet longer than any of the tasks
//      remover ! RemoverActor.HurryAll() //all hurried
//      //
//      val replies2 = probe.receiveN(15, 5 seconds)
//      var fja : Int = 0
//      var cta : Int = 0
//      var sja : Int = 0
//      var dta : Int = 0
//      var dtr : Int = 0
//      replies2.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case RemoverActorTest.FirstJobAlert() => fja += 1
//        case RemoverActorTest.ClearanceTestAlert() => cta += 1
//        case RemoverActorTest.SecondJobAlert() => sja += 1
//        case RemoverActorTest.DeletionTaskAlert() => dta += 1
//        case RemoverActorTest.DeletionTaskRunAlert() => dtr += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(fja == 3 && cta == 3 && sja == 3 && dta == 3 && dtr == 3)
//    }
//  }
//}
//
//class ClearSelectionRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//
//  "RemoverActor" should {
//    "be able to clear certain tasks" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(5 seconds))
//
//      val replies = probe.receiveN(4, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2)
//      probe.expectNoMsg(4 seconds) //long delay, longer than standard but not yet 5 seconds
//      remover ! RemoverActor.ClearSpecific(List(RemoverActorTest.TestObject), Zone.Nowhere) //cleared
//      //
//      val reply3 = probe.receiveOne(2 seconds)
//      assert(reply3.isInstanceOf[RemoverActorTest.FirstJobAlert])
//      val reply4 = probe.receiveOne(300 milliseconds)
//      assert(reply4.isInstanceOf[RemoverActorTest.ClearanceTestAlert])
//      val reply5 = probe.receiveOne(300 milliseconds)
//      assert(reply5.isInstanceOf[RemoverActorTest.SecondJobAlert])
//      val reply6 = probe.receiveOne(500 milliseconds)
//      assert(reply6.isInstanceOf[RemoverActorTest.DeletionTaskAlert])
//      val reply7 = probe.receiveOne(500 milliseconds)
//      assert(reply7.isInstanceOf[RemoverActorTest.DeletionTaskRunAlert])
//      //wait
//      probe.expectNoMsg(2 seconds) //nothing more to do
//    }
//  }
//}
//
//class ClearAllRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//
//  "RemoverActor" should {
//    "be able to clear all tasks, with no more work on them" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(5 seconds))
//
//      val replies = probe.receiveN(4, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2)
//      probe.expectNoMsg(4 seconds) //long delay, longer than standard but not yet 5 seconds
//      remover ! RemoverActor.ClearAll() //cleared
//      //wait
//      probe.expectNoMsg(3 seconds) //nothing more to do
//    }
//  }
//}
//
//class EarlyDeathRemoverActorTest extends ActorTest {
//  ServiceManager.boot ! ServiceManager.Register(RandomPool(2).props(Props[TaskResolver]), "taskResolver")
//  final val TestObject2 = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(2) } }
//
//  "RemoverActor" should {
//    "be able to hurry certain tasks" in {
//      expectNoMsg(500 milliseconds)
//      val probe = TestProbe()
//      val remover = system.actorOf(Props(classOf[RemoverActorTest.TestRemover], probe), "test-remover")
//      remover ! RemoverActor.AddTask(RemoverActorTest.TestObject, Zone.Nowhere, Some(5 seconds))
//      remover ! RemoverActor.AddTask(TestObject2, Zone.Nowhere, Some(5 seconds))
//
//      val replies = probe.receiveN(4, 5 seconds)
//      var ita : Int = 0
//      var ija : Int = 0
//      replies.collect {
//        case RemoverActorTest.InclusionTestAlert() => ita += 1
//        case RemoverActorTest.InitialJobAlert() => ija += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(ita == 2 && ija == 2)
//      probe.expectNoMsg(2 seconds)
//      remover ! akka.actor.PoisonPill
//      //
//      val replies2 = probe.receiveN(8, 5 seconds)
//      var fja : Int = 0
//      var cta : Int = 0
//      var sja : Int = 0
//      var dta : Int = 0
//      var dtr : Int = 0
//      replies2.collect {
//        case RemoverActorTest.FirstJobAlert() => fja += 1
//        case RemoverActorTest.ClearanceTestAlert() => cta += 1
//        case RemoverActorTest.SecondJobAlert() => sja += 1
//        case RemoverActorTest.DeletionTaskAlert() => dta += 1
//        case RemoverActorTest.DeletionTaskRunAlert() => dtr += 1
//        case msg => assert(false, s"$msg")
//      }
//      assert(fja == 2 && cta == 0 && sja == 2 && dta == 2 && dtr == 2) //no clearance tests
//    }
//  }
//}

object RemoverActorTest {
  final val TestObject = new Equipment() { def Definition = new EquipmentDefinition(0) { GUID = PlanetSideGUID(1) } }

  final case class InclusionTestAlert()

  final case class InitialJobAlert()

  final case class FirstJobAlert()

  final case class SecondJobAlert()

  final case class ClearanceTestAlert()

  final case class DeletionTaskAlert()

  final case class DeletionTaskRunAlert()

  class TestRemover extends RemoverActor {
    import net.psforever.objects.guid.{Task, TaskResolver}
    val FirstStandardDuration = 1 seconds

    val SecondStandardDuration = 100 milliseconds

    def InclusionTest(entry : RemoverActor.Entry) : Boolean = {
      context.parent ! InclusionTestAlert()
      true
    }

    def InitialJob(entry : RemoverActor.Entry) : Unit = {
      context.parent ! InitialJobAlert()
    }

    def FirstJob(entry : RemoverActor.Entry) : Unit = {
      context.parent ! FirstJobAlert()
    }

    override def SecondJob(entry : RemoverActor.Entry) : Unit = {
      context.parent ! SecondJobAlert()
      super.SecondJob(entry)
    }

    def ClearanceTest(entry : RemoverActor.Entry) : Boolean = {
      context.parent ! ClearanceTestAlert()
      true
    }

    def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
      context.parent ! DeletionTaskAlert()
      TaskResolver.GiveTask(new Task() {
        private val localProbe = context.parent

        override def isComplete = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          context.parent ! DeletionTaskRunAlert()
          resolver ! scala.util.Success(this)
        }
      })
    }
  }
}
