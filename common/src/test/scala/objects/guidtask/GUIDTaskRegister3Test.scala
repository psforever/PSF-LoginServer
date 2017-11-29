// Copyright (c) 2017 PSForever
package objects.guidtask

import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import objects.ActorTest

class GUIDTaskRegister3Test extends ActorTest() {
  "RegisterEquipment -> RegisterTool" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterEquipment(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
  }
}
