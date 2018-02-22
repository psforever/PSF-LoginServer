// Copyright (c) 2017 PSForever
package objects.guidtask

import akka.actor.{Actor, ActorSystem, Props}
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import org.specs2.mutable.Specification

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class GUIDTaskRegister1Test extends Specification {
  "RegisterObjectTask" should {
    "register (1)" in {
      val system = ActorSystem("sys")
      val test = system.actorOf(Props(classOf[GUIDTaskRegister1TestActor], system), "test")

      implicit val timeout = Timeout(5 seconds)
      val future = test ? "test"
      val result = Await.result(future, timeout.duration).asInstanceOf[String]
      result mustEqual "success"
    }
  }
}

private class GUIDTaskRegister1TestActor(implicit system : ActorSystem) extends Actor {
  def receive : Receive = {
    case "test" =>
      val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
      val obj = new GUIDTaskTest.TestObject

      assert(!obj.HasGUID)
      taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterObjectTask(obj)(uns)))
      probe.expectMsg(scala.util.Success)
      assert(obj.HasGUID)
      sender ! "success"
    case _ => ;
  }
}

//class GUIDTaskRegister1Test extends ActorTest() {
//  "RegisterObjectTask" in {
//    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
//    val obj = new GUIDTaskTest.TestObject
//
//    assert(!obj.HasGUID)
//    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterObjectTask(obj)(uns)))
//    probe.expectMsg(scala.util.Success)
//    assert(obj.HasGUID)
//  }
//}
