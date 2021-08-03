// Copyright (c) 2017 PSForever
package objects.guidtask

import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.locker.LockerEquipment
import net.psforever.types.{CharacterSex, CharacterVoice, PlanetSideEmpire}

import scala.concurrent.duration._

class GUIDTaskUnregisterPlayerTest extends ActorTest {
  "UnregisterPlayer" in {
    val (guid, uns, probe) = GUIDTaskTest.CommonTestSetup
    val obj                = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
    val obj_wep            = Tool(GlobalDefinitions.beamer)
    obj.Slot(0).Equipment = obj_wep
    val obj_wep_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_wep.AmmoSlots.head.Box = obj_wep_ammo
    val obj_inv_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj.Slot(6).Equipment = obj_inv_ammo
    val obj_locker      = obj.Slot(5).Equipment.get
    val obj_locker_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_locker.asInstanceOf[LockerEquipment].Inventory += 0 -> obj_locker_ammo
    guid.register(obj, name = "players")
    guid.register(obj_wep, name = "tools")
    guid.register(obj_wep_ammo, name = "ammo")
    guid.register(obj_inv_ammo, name = "ammo")
    guid.register(obj_locker, name = "lockers")
    guid.register(obj_locker_ammo, name = "ammo")

    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_inv_ammo.HasGUID)
    assert(obj_locker.HasGUID)
    assert(obj_locker_ammo.HasGUID)
    TaskWorkflow.execute(TaskBundle(
      new GUIDTaskTest.RegisterTestTask(probe.ref),
      GUIDTask.unregisterPlayer(uns, obj)
    ))
    probe.expectMsg(5.second, scala.util.Success(true))
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_inv_ammo.HasGUID)
    assert(obj_locker.HasGUID)
    assert(obj_locker_ammo.HasGUID)
  }
}
