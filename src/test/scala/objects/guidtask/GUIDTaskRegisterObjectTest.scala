// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}

import scala.concurrent.duration._

class GUIDTaskRegisterObjectTest extends ActorTest {
  "RegisterObjectTask" in {
    val (_, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = new GUIDTaskTest.TestObject

    assert(!obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.registerObject(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(obj.HasGUID)
  }
}
