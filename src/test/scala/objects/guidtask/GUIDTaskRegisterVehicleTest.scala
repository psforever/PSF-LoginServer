// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}

class GUIDTaskRegisterVehicleTest extends ActorTest {
  "RegisterVehicle" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                           = Vehicle(GlobalDefinitions.fury)
    val obj_wep                       = obj.WeaponControlledFromSeat(0).get
    val obj_wep_ammo = (obj.WeaponControlledFromSeat(0).get.asInstanceOf[Tool].AmmoSlots.head.Box =
      AmmoBox(GlobalDefinitions.hellfire_ammo)).get
    obj.Trunk += 30 -> AmmoBox(GlobalDefinitions.hellfire_ammo)
    val obj_trunk_ammo = obj.Trunk.Items(0).obj

    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_trunk_ammo.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.RegisterVehicle(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_trunk_ammo.HasGUID)
  }
}
