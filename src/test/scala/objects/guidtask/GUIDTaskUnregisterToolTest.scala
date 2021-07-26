// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask
import scala.concurrent.duration._

class GUIDTaskUnregisterToolTest extends ActorTest {
  "UnregisterEquipment -> UnregisterTool" in {
    val (guid, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj)
    guid.register(obj.AmmoSlots.head.Box)

    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterEquipment(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
  }
}
