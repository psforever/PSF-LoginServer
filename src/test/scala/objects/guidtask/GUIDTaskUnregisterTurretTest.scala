// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}

import scala.concurrent.duration._

class GUIDTaskUnregisterTurretTest extends ActorTest {
  "UnregisterDeployableTurret" in {
    val (guid, uns, probe) = GUIDTaskTest.CommonTestSetup
    val obj                = new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
    val obj_wep            = obj.Weapons(1).Equipment.get
    val obj_ammo           = obj_wep.asInstanceOf[Tool].AmmoSlot.Box
    val obj_res            = obj.Inventory.Items.map(_.obj)
    guid.register(obj, name = "deployables")
    guid.register(obj_wep, name = "tools")
    guid.register(obj_ammo, name = "ammo")
    obj_res.foreach(box => guid.register(box, name = "ammo"))

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_ammo.HasGUID)
    obj_res.foreach(box => box.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterDeployableTurret(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_ammo.HasGUID)
    obj_res.foreach(box => !box.HasGUID)
  }
}
