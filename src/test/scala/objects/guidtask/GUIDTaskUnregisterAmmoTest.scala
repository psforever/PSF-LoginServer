// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskUnregisterAmmoTest extends ActorTest {
  "UnregisterEquipment -> UnregisterObjectTask" in {
    val (guid, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj, "dynamic")

    assert(obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterEquipment(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
  }
}
