// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskRegisterToolTest extends ActorTest {
  "RegisterEquipment -> RegisterTool" in {
    val (_, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.registerEquipment(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
  }
}
