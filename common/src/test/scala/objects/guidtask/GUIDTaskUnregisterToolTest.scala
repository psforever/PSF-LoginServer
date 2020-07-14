// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskUnregisterToolTest extends ActorTest {
  "UnregisterEquipment -> UnregisterTool" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj, "dynamic")
    guid.register(obj.AmmoSlots.head.Box, "dynamic")

    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.UnregisterEquipment(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
  }
}
