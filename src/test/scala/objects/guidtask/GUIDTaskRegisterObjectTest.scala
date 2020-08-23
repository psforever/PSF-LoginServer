// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskRegisterObjectTest extends ActorTest {
  "RegisterObjectTask" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = new GUIDTaskTest.TestObject

    assert(!obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.RegisterObjectTask(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}
