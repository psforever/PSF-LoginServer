// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects._
import net.psforever.objects.guid.actor.{Register, TaskBundle, Unregister, Task}
import net.psforever.objects.inventory.Container
import net.psforever.objects.locker.{LockerContainer, LockerEquipment}
import net.psforever.objects.serverobject.turret.WeaponTurret

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.Future

/**
  * The basic compiled tasks for assigning (registering) and revoking (unregistering) globally unique identifiers.<br>
  * <br>
  * Almost all of these functions will be invoked from `WorldSessionActor`.
  * Some of the "unregistering" functions will invoke on delayed `Service` operations,
  * indicating behavior that is not user/observer dependent.
  * The object's (current) `Zone` must also be knowable since the GUID systems are tied to individual zones.
  * For simplicity, all functions have the same format where the hook into the GUID system is an `implicit` parameter.
  * It will get passed from the more complicated functions down into the less complicated functions,
  * until it has found the basic number assignment functionality.<br>
  * <br>
  * All functions produce a `TaskBundle` container object
  * or a list of `TaskBundle` container objects that is expected to be used by a `TaskBundle` container.
  * These "task containers" can also be unpackaged into their component tasks, sorted into other containers,
  * and combined with other tasks to enact more complicated sequences of operations.
  * Almost all tasks have an explicit registering and an unregistering activity defined for it.
  */

object GUIDTask {
  private implicit val timeout = Timeout(2.seconds)

  //registration tasking
  private case class RegisterObjectTask(
                                         guid: ActorRef,
                                         obj: IdentifiableEntity
                                       ) extends Task {
    def action(): Future[Any] = {
      ask(guid, Register(obj, "dynamic"))
    }

    def undo(): Unit = {
      guid.tell(Unregister(obj), guid)
    }

    def isSuccessful() : Boolean = obj.HasGUID

    override def description(): String = s"register $obj"
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers.<br>
    * <br>
    * Regardless of the complexity of the object provided to this function, only the current depth will be assigned a GUID.
    * This is the most basic operation that all objects that can be assigned a GUID must perform.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def registerObject(guid: ActorRef, obj: IdentifiableEntity): TaskBundle = TaskBundle(RegisterObjectTask(guid, obj))

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers, as a `Tool`.<br>
    * <br>
    * `Tool` objects are complicated by an internal structure informally called a "magazine feed."
    * The objects in the magazine feed are called `AmmoBox` objects.
    * Each `AmmoBox` object can be registered to a unique number system much like the `Tool` itself; and,
    * each must be registered properly for the whole of the `Tool` to be communicated from the server to the client.
    * While the matter has been abstracted for convenience, most `Tool` objects will have only one `AmmoBox` at a time
    * and the common outlier will only be two.<br>
    * <br>
    * Do not invoke this function unless certain the object will be of type `Tool`,
    * else use a more general function to differentiate between simple and complex objects.
    * @param obj the `Tool` object being registered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerEquipment`
    * @return a `TaskBundle` message
    */
  def registerTool(guid: ActorRef, obj: Tool): TaskBundle = {
    TaskBundle(
      RegisterObjectTask(guid, obj),
      (0 until obj.MaxAmmoSlot).map(ammoIndex => registerObject(guid, obj.AmmoSlots(ammoIndex).Box))
    )
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers,
    * after determining whether the object is complex (`Tool` or `Locker`) or is simple.<br>
    * <br>
    * The objects in this case are specifically `Equipment`, a subclass of the basic register-able `IdentifiableEntity`.
    * About five subclasses of `Equipment` exist, but they decompose into two groups - "complex objects" and "simple objects."
    * "Simple objects" are most groups of `Equipment` and just their own GUID to be registered.
    * "Complex objects" are just the `Tool` category of `Equipment`.
    * They have internal objects that must also have their GUID's registered to function.<br>
    * <br>
    * Using this function when passing unknown `Equipment` is recommended.
    * The type will be sorted and the object will be handled according to its complexity level.
    * @param obj the `Equipment` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def registerEquipment(guid: ActorRef, obj: Equipment): TaskBundle = {
    obj match {
      case tool: Tool => registerTool(guid, tool)
      case _ =>          registerObject(guid, obj)
    }
  }

  /**
    * Construct tasking that registers the objects that are within the given container's inventory
    * with a globally unique identifier selected from a pool of numbers for each object.
    * @param container the storage unit in which objects can be found
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.unregisterInventory`<br>
    *       `Container`
    * @return a list of `TaskBundle` messages
    */
  def registerInventory(guid: ActorRef, container: Container): List[TaskBundle] = {
    container.Inventory.Items.map{ entry => registerEquipment(guid, entry.obj) }
  }

  /**
    * Construct tasking that registers a `LockerContainer` object
    * with a globally unique identifier selected from a pool of numbers.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.unregisterLocker`
    * @return a `TaskBundle` message
    */
  def registerLocker(guid: ActorRef, obj: LockerContainer): TaskBundle = {
    TaskBundle(RegisterObjectTask(guid, obj), registerInventory(guid, obj))
  }

  /**
    * Construct tasking that registers a `LockerContainer` object
    * with a globally unique identifier selected from a pool of numbers.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.unregisterLocker`
    * @return a `TaskBundle` message
    */
  def registerLocker(guid: ActorRef, obj: LockerEquipment): TaskBundle = {
    TaskBundle(RegisterObjectTask(guid, obj), registerInventory(guid, obj))
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers, as a `Player`.<br>
    *  <br>
    * `Player` objects are far more complicated than `Tools` (but they are not `Equipment`).
    * A player has an inventory in which it can hold a countable number of `Equipment`; and,
    * this inventory holds a sub-inventory with its own countable number of `Equipment`.
    * Although a process of completing and inserting `Equipment` into the inventories that looks orderly can be written,
    * this function assumes that the player is already fully composed.
    * Use this function for an sudden introduction of the player into his environment
    * (as defined by the scope of the unique number system).
    * For working with processes concerning these "orderly insertions,"
    * a task built of lesser registration tasks and supporting tasks should be written instead.
    * @param tplayer the `Player` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def registerAvatar(guid: ActorRef, tplayer: Player): TaskBundle = {
    val holsterTasks   = visibleSlotTaskBuilding(guid, tplayer.Holsters(), registerEquipment)
    val lockerTask     = List(registerObject(guid, tplayer.avatar.locker))
    val inventoryTasks = registerInventory(guid, tplayer)
    TaskBundle(RegisterObjectTask(guid, tplayer), holsterTasks ++ lockerTask ++ inventoryTasks)
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers, as a `Player`.<br>
    * <br>
    * Similar to `RegisterAvatar` but the locker components are skipped.
    * @param tplayer the `Player` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def registerPlayer(guid: ActorRef, tplayer: Player): TaskBundle = {
    val holsterTasks   = visibleSlotTaskBuilding(guid, tplayer.Holsters(), registerEquipment)
    val inventoryTasks = registerInventory(guid, tplayer)
    TaskBundle(RegisterObjectTask(guid, tplayer), holsterTasks ++ inventoryTasks)
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers, as a `Vehicle`.<br>
    *  <br>
    * `Vehicle` objects are far more complicated than `Tools` (but they are not `Equipment`).
    * A vehicle has an inventory in which it can hold a countable number of `Equipment`; and,
    * it may possess weapons (`Tools`, usually) that are firmly mounted on its outside.
    * (This is similar to the holsters on a `Player` object but they can not be swapped out for other `Equipment` or for nothing.)
    * Although a process of completing and inserting `Equipment` into the inventories that looks orderly can be written,
    * this function assumes that the vehicle is already fully composed.
    * Use this function for an sudden introduction of the vehicle into its environment
    * (as defined by the scope of the unique number system).
    * For working with processes concerning these "orderly insertions,"
    * a task built of lesser registration tasks and supporting tasks should be written instead.
    * @param vehicle the `Vehicle` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def registerVehicle(guid: ActorRef, vehicle: Vehicle): TaskBundle = {
    val weaponTasks = visibleSlotTaskBuilding(guid, vehicle.Weapons.values, registerEquipment)
    val utilTasks =
      Vehicle.EquipmentUtilities(vehicle.Utilities).values.map(util => { registerObject(guid, util()) }).toList
    val inventoryTasks = registerInventory(guid, vehicle)
    TaskBundle(RegisterObjectTask(guid, vehicle), weaponTasks ++ utilTasks ++ inventoryTasks)
  }

  def registerDeployableTurret(guid: ActorRef, obj: PlanetSideGameObject with WeaponTurret): TaskBundle = {
    TaskBundle(
      RegisterObjectTask(guid, obj),
      visibleSlotTaskBuilding(guid, obj.Weapons.values, registerEquipment) ++ registerInventory(guid, obj)
    )
  }

  //unregistration tasking
  private case class UnregisterObjectTask(
                                         guid: ActorRef,
                                         obj: IdentifiableEntity
                                       ) extends Task {
    def action(): Future[Any] = {
      ask(guid, Unregister(obj))
    }

    def undo(): Unit = {
      guid.tell(Register(obj, "dynamic"), guid)
    }

    def isSuccessful() : Boolean = !obj.HasGUID

    override def description(): String = s"unregister $obj"
  }

  /**
    * Construct tasking that unregisters an object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterObjectTask`.
    * It is the most basic operation that all objects that can have their GUIDs revoked must perform.
    * @param obj the object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerObjectTask`
    * @return a `TaskBundle` message
    */
  def unregisterObject(guid: ActorRef, obj: IdentifiableEntity): TaskBundle = TaskBundle(UnregisterObjectTask(guid, obj))

  /**
    * Construct tasking that unregisters an object from a globally unique identifier system
    * after determining whether the object is complex (`Tool` or `Locker`) or is simple.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterEquipment`.
    * @param obj the `Equipment` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerEquipment`
    * @return a `TaskBundle` message
    */
  def unregisterTool(guid: ActorRef, obj: Tool): TaskBundle = {
    TaskBundle(
      UnregisterObjectTask(guid, obj),
      (0 until obj.MaxAmmoSlot).map(ammoIndex => unregisterObject(guid, obj.AmmoSlots(ammoIndex).Box))
    )
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers,
    * after determining whether the object is complex (`Tool` or `Locker`) or is simple.<br>
    * <br>
    * The objects in this case are specifically `Equipment`, a subclass of the basic register-able `IdentifiableEntity`.
    * About five subclasses of `Equipment` exist, but they decompose into two groups - "complex objects" and "simple objects."
    * "Simple objects" are most groups of `Equipment` and just their own GUID to be registered.
    * "Complex objects" are just the `Tool` category of `Equipment`.
    * They have internal objects that must also have their GUID's registered to function.<br>
    * <br>
    * Using this function when passing unknown `Equipment` is recommended.
    * The type will be sorted and the object will be handled according to its complexity level.
    * @param obj the `Equipment` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskBundle` message
    */
  def unregisterEquipment(guid: ActorRef, obj: Equipment): TaskBundle = {
    obj match {
      case tool: Tool => unregisterTool(guid, tool)
      case _ =>          unregisterObject(guid, obj)
    }
  }

  /**
    * Construct tasking that unregisters the objects that are within the given container's inventory
    * from a globally unique identifier system.
    * @param container the storage unit in which objects can be found
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerInventory`<br>
    *       `Container`
    * @return a list of `TaskBundle` messages
    */
  def unregisterInventory(guid: ActorRef, container: Container): List[TaskBundle] = {
    container.Inventory.Items.map{ entry => unregisterEquipment(guid, entry.obj) }
  }

  /**
    * Construct tasking that unregisters a `LockerContainer` object from a globally unique identifier system.
    * @param obj the object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerLocker`
    * @return a `TaskBundle` message
    */
  def unregisterLocker(guid: ActorRef, obj: LockerContainer): TaskBundle = {
    TaskBundle(UnregisterObjectTask(guid, obj), unregisterInventory(guid, obj))
  }

  /**
    * Construct tasking that unregisters a `LockerContainer` object from a globally unique identifier system.
    * @param obj the object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerLocker`
    * @return a `TaskBundle` message
    */
  def unregisterLocker(guid: ActorRef, obj: LockerEquipment): TaskBundle = {
    TaskBundle(UnregisterObjectTask(guid, obj), unregisterInventory(guid, obj))
  }

  /**
    * Construct tasking that unregisters a `Player` object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterAvatar`.
    * @param tplayer the `Player` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerAvatar`
    * @return a `TaskBundle` message
    */
  def unregisterAvatar(guid: ActorRef, tplayer: Player): TaskBundle = {
    val holsterTasks   = visibleSlotTaskBuilding(guid, tplayer.Holsters(), unregisterEquipment)
    val lockerTask     = List(unregisterObject(guid, tplayer.avatar.locker))
    val inventoryTasks = unregisterInventory(guid, tplayer)
    TaskBundle(UnregisterObjectTask(guid, tplayer), holsterTasks ++ lockerTask ++ inventoryTasks)
  }

  /**
    * Construct tasking that unregisters a portion of a `Player` object from a globally unique identifier system.<br>
    * <br>
    * Similar to `UnregisterAvatar` but the locker components are skipped.
    * This task performs an operation that reverses the effect of `RegisterPlayer`.
    * @param tplayer the `Player` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerAvatar`
    * @return a `TaskBundle` message
    */
  def unregisterPlayer(guid: ActorRef, tplayer: Player): TaskBundle = {
    val holsterTasks   = visibleSlotTaskBuilding(guid, tplayer.Holsters(), unregisterEquipment)
    val inventoryTasks = unregisterInventory(guid, tplayer)
    TaskBundle(UnregisterObjectTask(guid, tplayer), holsterTasks ++ inventoryTasks)
  }

  /**
    * Construct tasking that unregisters a `Vehicle` object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterVehicle`.
    * @param vehicle the `Vehicle` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.registerVehicle`
    * @return a `TaskBundle` message
    */
  def unregisterVehicle(guid: ActorRef, vehicle: Vehicle): TaskBundle = {
    val weaponTasks = visibleSlotTaskBuilding(guid, vehicle.Weapons.values, unregisterEquipment)
    val utilTasks =
      Vehicle.EquipmentUtilities(vehicle.Utilities).values.map(util => { unregisterObject(guid, util()) }).toList
    val inventoryTasks = unregisterInventory(guid, vehicle)
    TaskBundle(UnregisterObjectTask(guid, vehicle), weaponTasks ++ utilTasks ++ inventoryTasks)
  }

  def unregisterDeployableTurret(guid: ActorRef, obj: PlanetSideGameObject with WeaponTurret): TaskBundle = {
    TaskBundle(
      UnregisterObjectTask(guid, obj),
      visibleSlotTaskBuilding(guid, obj.Weapons.values, unregisterEquipment) ++ unregisterInventory(guid, obj)
    )
  }

  //support
  /**
    * Construct tasking that allocates work upon encountered `Equipment` objects
    * in reference to a globally unique identifier system of a pool of numbers.
    * "Visible slots" are locations that can be viewed by multiple observers across a number of clients.
    * @param list an `Iterable` sequence of `EquipmentSlot` objects that may or may not have equipment
    * @param func the function used to build tasking from any discovered `Equipment`;
    *             strictly either `RegisterEquipment` or `UnregisterEquipment`
    * @param guid implicit reference to a unique number system
    * @return a list of `TaskBundle` messages
    */
  private def visibleSlotTaskBuilding(
                               guid: ActorRef,
                               list: Iterable[EquipmentSlot],
                               func: (ActorRef, Equipment) => TaskBundle
                             ): List[TaskBundle] = {
    recursiveVisibleSlotTaskBuilding(guid, list.iterator, func)
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Use `func` on any discovered `Equipment` to transform items into tasking, and add the tasking to a `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param func the function used to build tasking from any discovered `Equipment`;
    *             strictly either `RegisterEquipment` or `UnregisterEquipment`
    * @param list a persistent `List` of `Equipment` tasking
    * @see `VisibleSlotTaskBuilding`
    * @return a `List` of `Equipment` tasking
    */
  @tailrec private def recursiveVisibleSlotTaskBuilding(
                                                         guid: ActorRef,
                                                         iter: Iterator[EquipmentSlot],
                                                         func: (ActorRef, Equipment) => TaskBundle,
                                                         list: List[TaskBundle] = Nil
                                                       ): List[TaskBundle] = {
    if (!iter.hasNext) {
      list
    } else {
      iter.next().Equipment match {
        case Some(item) => recursiveVisibleSlotTaskBuilding(guid, iter, func, list :+ func(guid, item))
        case None =>       recursiveVisibleSlotTaskBuilding(guid, iter, func, list)
      }
    }
  }
}
