// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskRegisterObjectTest extends ActorTest {
  "RegisterObjectTask" in {
    val (_, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = new GUIDTaskTest.TestObject

    assert(!obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.registerObject(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}
