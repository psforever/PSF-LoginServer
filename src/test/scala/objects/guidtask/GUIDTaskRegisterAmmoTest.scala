// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}

import scala.concurrent.duration._

class GUIDTaskRegisterAmmoTest extends ActorTest {
  "RegisterEquipment -> RegisterObjectTask" in {
    val (_, uns, probe) = GUIDTaskTest.CommonTestSetup
    val obj             = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.registerEquipment(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(obj.HasGUID)
  }
}
