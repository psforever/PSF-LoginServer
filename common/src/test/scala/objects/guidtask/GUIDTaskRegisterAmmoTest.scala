// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskRegisterAmmoTest extends ActorTest {
  "RegisterEquipment -> RegisterObjectTask" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.RegisterEquipment(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}
