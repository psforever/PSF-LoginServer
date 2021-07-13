// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskUnregisterToolTest extends ActorTest {
  "UnregisterEquipment -> UnregisterTool" in {
    val (guid, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj, "dynamic")
    guid.register(obj.AmmoSlots.head.Box, "dynamic")

    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterEquipment(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
  }
}
