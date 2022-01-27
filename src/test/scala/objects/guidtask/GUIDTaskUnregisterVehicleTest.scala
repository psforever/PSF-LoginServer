// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}

import scala.concurrent.duration._

class GUIDTaskUnregisterVehicleTest extends ActorTest {
  "RegisterVehicle" in {
    val (guid, uns, probe) = GUIDTaskTest.CommonTestSetup
    val obj                = Vehicle(GlobalDefinitions.fury)
    val obj_wep            = obj.WeaponControlledFromSeat(0).head
    val obj_wep_ammo       = (obj_wep.asInstanceOf[Tool].AmmoSlots.head.Box =
      AmmoBox(GlobalDefinitions.hellfire_ammo)).get
    obj.Trunk += 30 -> AmmoBox(GlobalDefinitions.hellfire_ammo)
    val obj_trunk_ammo = obj.Trunk.Items(0).obj
    guid.register(obj, name = "vehicles")
    guid.register(obj_wep, name = "tools")
    guid.register(obj_wep_ammo, name = "ammo")
    guid.register(obj_trunk_ammo, name = "ammo")

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_trunk_ammo.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterVehicle(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_trunk_ammo.HasGUID)
  }
}
