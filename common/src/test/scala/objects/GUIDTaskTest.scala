// Copyright (c) 2017 PSForever
package objects

import java.util.logging.LogManager

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import net.psforever.objects._
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.actor.{NumberPoolActor, UniqueNumberSystem}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.guid.{GUIDTask, NumberPoolHub, Task, TaskResolver}
import net.psforever.types.{CharacterGender, PlanetSideEmpire}

class GUIDTaskRegister1Test extends ActorTest() {
  "RegisterObjectTask" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = new GUIDTaskTest.TestObject

    assert(!obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterObjectTask(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}

class GUIDTaskRegister2Test extends ActorTest() {
  "RegisterEquipment -> RegisterObjectTask" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = AmmoBox(GlobalDefinitions.energy_cell)

    assert(!obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterEquipment(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
  }
}

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

class GUIDTaskRegister4Test extends ActorTest() {
  "RegisterVehicle" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Vehicle(GlobalDefinitions.fury)
    val obj_wep = obj.WeaponControlledFromSeat(0).get
    val obj_wep_ammo = (obj.WeaponControlledFromSeat(0).get.asInstanceOf[Tool].AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.hellfire_ammo)).get
    obj.Trunk += 30 -> AmmoBox(GlobalDefinitions.hellfire_ammo)
    val obj_trunk_ammo = obj.Trunk.Items(0).obj

    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_trunk_ammo.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterVehicle(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_trunk_ammo.HasGUID)
  }
}

class GUIDTaskRegister5Test extends ActorTest() {
  "RegisterAvatar" in {
    val (_, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val obj_wep = Tool(GlobalDefinitions.beamer)
    obj.Slot(0).Equipment = obj_wep
    val obj_wep_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_wep.AmmoSlots.head.Box = obj_wep_ammo
    val obj_inv_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj.Slot(6).Equipment = obj_inv_ammo
    val obj_locker = obj.Slot(5).Equipment.get
    val obj_locker_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_locker.asInstanceOf[LockerContainer].Inventory += 0 -> obj_locker_ammo

    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_inv_ammo.HasGUID)
    assert(!obj_locker.HasGUID)
    assert(!obj_locker_ammo.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.RegisterAvatar(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(obj.HasGUID)
    assert(obj_wep.HasGUID)
    assert(obj_wep_ammo.HasGUID)
    assert(obj_inv_ammo.HasGUID)
    assert(obj_locker.HasGUID)
    assert(obj_locker_ammo.HasGUID)
  }
}

class GUIDTaskUnregister1Test extends ActorTest() {
  "UnregisterObjectTask" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = new GUIDTaskTest.TestObject
    guid.register(obj, "dynamic")

    assert(obj.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.UnregisterObjectTask(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
  }
}

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

class GUIDTaskUnregister3Test extends ActorTest() {
  "UnregisterEquipment -> UnregisterTool" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Tool(GlobalDefinitions.beamer)
    obj.AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.energy_cell)
    guid.register(obj, "dynamic")
    guid.register(obj.AmmoSlots.head.Box, "dynamic")

    assert(obj.HasGUID)
    assert(obj.AmmoSlots.head.Box.HasGUID)
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.UnregisterEquipment(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj.AmmoSlots.head.Box.HasGUID)
  }
}

class GUIDTaskUnregister4Test extends ActorTest() {
  "RegisterVehicle" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Vehicle(GlobalDefinitions.fury)
    val obj_wep = obj.WeaponControlledFromSeat(0).get
    val obj_wep_ammo = (obj.WeaponControlledFromSeat(0).get.asInstanceOf[Tool].AmmoSlots.head.Box = AmmoBox(GlobalDefinitions.hellfire_ammo)).get
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
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.UnregisterVehicle(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_trunk_ammo.HasGUID)
  }
}

class GUIDTaskUnregister5Test extends ActorTest() {
  "UnregisterAvatar" in {
    val (guid, uns, taskResolver, probe) = GUIDTaskTest.CommonTestSetup
    val obj = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val obj_wep = Tool(GlobalDefinitions.beamer)
    obj.Slot(0).Equipment = obj_wep
    val obj_wep_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_wep.AmmoSlots.head.Box = obj_wep_ammo
    val obj_inv_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj.Slot(6).Equipment = obj_inv_ammo
    val obj_locker = obj.Slot(5).Equipment.get
    val obj_locker_ammo = AmmoBox(GlobalDefinitions.energy_cell)
    obj_locker.asInstanceOf[LockerContainer].Inventory += 0 -> obj_locker_ammo
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
    taskResolver ! TaskResolver.GiveTask(new GUIDTaskTest.RegisterTestTask(probe.ref), List(GUIDTask.UnregisterAvatar(obj)(uns)))
    probe.expectMsg(scala.util.Success)
    assert(!obj.HasGUID)
    assert(!obj_wep.HasGUID)
    assert(!obj_wep_ammo.HasGUID)
    assert(!obj_inv_ammo.HasGUID)
    assert(!obj_locker.HasGUID)
    assert(!obj_locker_ammo.HasGUID)
  }
}

object GUIDTaskTest {
  class TestObject extends IdentifiableEntity

  class RegisterTestTask(probe : ActorRef) extends Task {
    def Execute(resolver : ActorRef) : Unit = {
      probe ! scala.util.Success
      resolver ! scala.util.Success(this)
    }
  }

  def CommonTestSetup(implicit system : ActorSystem) : (NumberPoolHub, ActorRef, ActorRef, TestProbe) = {
    import akka.actor.Props
    import akka.routing.RandomPool
    import akka.testkit.TestProbe

    val guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(110))
    guid.AddPool("dynamic", (1 to 100).toList).Selector = new RandomSelector //TODO name is hardcoded for now
    val uns = system.actorOf(RandomPool(25).props(Props(classOf[UniqueNumberSystem], guid, GUIDTaskTest.AllocateNumberPoolActors(guid))), "uns")
    val taskResolver = system.actorOf(RandomPool(15).props(Props[TaskResolver]), "resolver")
    LogManager.getLogManager.reset() //suppresses any internal loggers created by the above elements
    (guid, uns, taskResolver, TestProbe())
  }

  /**
    * @see `UniqueNumberSystem.AllocateNumberPoolActors(NumberPoolHub)(implicit ActorContext)`
    */
  def AllocateNumberPoolActors(poolSource : NumberPoolHub)(implicit system : ActorSystem) : Map[String, ActorRef] = {
    poolSource.Pools.map({ case ((pname, pool)) =>
      pname -> system.actorOf(Props(classOf[NumberPoolActor], pool), pname)
    }).toMap
  }
}
