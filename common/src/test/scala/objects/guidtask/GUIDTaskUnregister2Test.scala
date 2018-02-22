// Copyright (c) 2017 PSForever
package objects.guidtask

import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import objects.ActorTest

class GUIDTaskUnregister2Test extends ActorTest() {
  "UnregisterEquipment -> UnregisterObjectTask" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj, "dynamic")

    assert(obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.UnregisterEquipment(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
  }
}
