// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskUnregisterObjectTest extends ActorTest {
  "UnregisterObjectTask" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = new GUIDTaskTest.TestObject
    guid.register(obj, "dynamic")

    assert(obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.UnregisterObjectTask(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
  }
}
