// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskUnregisterVehicleTest extends ActorTest {
  "RegisterVehicle" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = Vehicle(GlobalDefinitions.fury)
    val obj_wep                          = obj.WeaponControlledFromSeat(0).get
    val obj_wep_ammo = (obj.WeaponControlledFromSeat(0).get.asInstanceOf[Tool].AmmoSlots.head.Box =
      AmmoBox(GlobalDefinitions.hellfire_ammo)).get
    obj.Trunk += 30 -> AmmoBox(GlobalDefinitions.hellfire_ammo)
    val obj_trunk_ammo = obj.Trunk.Items(0).obj
    guid.register(obj, "dynamic")
    guid.register(obj_wep, "dynamic")
    guid.register(obj_wep_ammo, "dynamic")
    guid.register(obj_trunk_ammo, "dynamic")

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_trunk_ammo.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.UnregisterVehicle(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_trunk_ammo.HasGUID)
  }
}
