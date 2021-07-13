// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.actor.{TaskBundle, TaskWorkflow}
import net.psforever.objects.guid.GUIDTask

class GUIDTaskRegisterAmmoTest extends ActorTest {
  "RegisterEquipment -> RegisterObjectTask" in {
    val (_, uns, _, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.registerEquipment(uns, obj)
    ))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}
