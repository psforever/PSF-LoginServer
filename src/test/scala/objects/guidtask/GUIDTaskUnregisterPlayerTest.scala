// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.locker.LockerEquipment
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}

class GUIDTaskUnregisterPlayerTest extends ActorTest {
  "UnregisterPlayer" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj                              = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
    val obj_wep                          = Tool(GlobalDefinitions.beamer)
    obj.Slot(0).Equipment = obj_wep
    val obj_wep_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_wep.AmmoSlots.head.Box = obj_wep_ammo
    val obj_inv_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj.Slot(6).Equipment = obj_inv_ammo
    val obj_locker      = obj.Slot(5).Equipment.get
    val obj_locker_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_locker.asInstanceOf[LockerEquipment].Inventory += 0 -> obj_locker_ammo
    guid.register(obj, "dynamic")
    guid.register(obj_wep, "dynamic")
    guid.register(obj_wep_ammo, "dynamic")
    guid.register(obj_inv_ammo, "dynamic")
    guid.register(obj_locker, "dynamic")
    guid.register(obj_locker_ammo, "dynamic")

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_inv_ammo.HasGUID)
    assert(obj_locker.HasGUID)
    assert(obj_locker_ammo.HasGUID)
    taskResolver ! TaskResolver.GiveTask(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      List(GUIDTask.UnregisterPlayer(obj)(uns))
    )
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_inv_ammo.HasGUID)
    assert(obj_locker.HasGUID)
    assert(obj_locker_ammo.HasGUID)
  }
}
