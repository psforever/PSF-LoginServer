// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}

import scala.concurrent.duration._

class GUIDTaskUnregisterObjectTest extends ActorTest {
  "UnregisterObjectTask" in {
    val (guid, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = new GUIDTaskTest.TestObject
    guid.register(obj)

    assert(obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterObject(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(!obj.HasGUID)
  }
}
