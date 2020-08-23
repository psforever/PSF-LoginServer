// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskUnregisterTurretTest extends ActorTest {
  "UnregisterDeployableTurret" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
    val obj_wep                          = obj.Weapons(1).Equipment.get
    val obj_ammo                         = obj_wep.asInstanceOf[Tool].AmmoSlot.Box
    val obj_res                          = obj.Inventory.Items.map(_.obj)
    guid.register(obj, "dynamic")
    guid.register(obj_wep, "dynamic")
    guid.register(obj_ammo, "dynamic")
    obj_res.foreach(box => guid.register(box, "dynamic"))

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_ammo.HasGUID)
    obj_res.foreach(box => box.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.UnregisterDeployableTurret(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_ammo.HasGUID)
    obj_res.foreach(box => !box.HasGUID)
  }
}
