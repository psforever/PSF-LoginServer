// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskUnregisterObjectTest extends ActorTest {
  "UnregisterObjectTask" in {
    val (guid, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = new GUIDTaskTest.TestObject
    guid.register(obj, "dynamic")

    assert(obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterObject(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
  }
}
