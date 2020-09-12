// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import akka.actor.ActorRef
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects._
import net.psforever.objects.inventory.Container
import net.psforever.objects.locker.{LockerContainer, LockerEquipment}
import net.psforever.objects.serverobject.turret.WeaponTurret

import scala.annotation.tailrec

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
  * All functions produce a `TaskResolver.GiveTask` container object
  * or a list of `TaskResolver.GiveTask` container objects that is expected to be used by a `TaskResolver` `Actor`.
  * These "task containers" can also be unpackaged into their component tasks, sorted into other containers,
  * and combined with other tasks to enact more complicated sequences of operations.
  * Almost all tasks have an explicit registering and an unregistering activity defined for it.
  */
object GUIDTask {

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers.<br>
    * <br>
    * Regardless of the complexity of the object provided to this function, only the current depth will be assigned a GUID.
    * This is the most basic operation that all objects that can be assigned a GUID must perform.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterObjectTask(obj: IdentifiableEntity)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(new Task() {
      private val localObject   = obj
      private val localAccessor = guid

      override def Description: String = s"register $localObject"

      override def isComplete: Task.Resolution.Value =
        if (localObject.HasGUID) {
          Task.Resolution.Success
        } else {
          Task.Resolution.Incomplete
        }

      def Execute(resolver: ActorRef): Unit = {
        import net.psforever.objects.guid.actor.Register
        localAccessor ! Register(localObject, "dynamic", resolver) //TODO pool should not be hardcoded
      }
    })
  }

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
    * @see `GUIDTask.RegisterEquipment`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterTool(obj: Tool)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val ammoTasks: List[TaskResolver.GiveTask] =
      (0 until obj.MaxAmmoSlot).map(ammoIndex => RegisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that registers a `LockerContainer` object
    * with a globally unique identifier selected from a pool of numbers.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.UnregisterLocker`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterLocker(obj: LockerContainer)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, RegisterInventory(obj))
  }
  def RegisterLocker(obj: LockerEquipment)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, RegisterInventory(obj))
  }

  /**
    * Construct tasking that registers the objects that are within the given container's inventory
    * with a globally unique identifier selected from a pool of numbers for each object.
    * @param container the storage unit in which objects can be found
    * @param guid implicit reference to a unique number system
    * @see `GUID.UnregisterInventory`<br>
    *       `Container`
    * @return a list of `TaskResolver.GiveTask` messages
    */
  def RegisterInventory(container: Container)(implicit guid: ActorRef): List[TaskResolver.GiveTask] = {
    container.Inventory.Items.map(entry => { RegisterEquipment(entry.obj) })
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
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterEquipment(obj: Equipment)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    obj match {
      case tool: Tool =>
        RegisterTool(tool)
      case _ =>
        RegisterObjectTask(obj)
    }
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
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterAvatar(tplayer: Player)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val holsterTasks   = VisibleSlotTaskBuilding(tplayer.Holsters(), RegisterEquipment)
    val lockerTask     = List(RegisterObjectTask(tplayer.avatar.locker))
    val inventoryTasks = RegisterInventory(tplayer)
    TaskResolver.GiveTask(RegisterObjectTask(tplayer).task, holsterTasks ++ lockerTask ++ inventoryTasks)
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers, as a `Player`.<br>
    * <br>
    * Similar to `RegisterAvatar` but the locker components are skipped.
    * @param tplayer the `Player` object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterPlayer(tplayer: Player)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val holsterTasks   = VisibleSlotTaskBuilding(tplayer.Holsters(), RegisterEquipment)
    val inventoryTasks = RegisterInventory(tplayer)
    TaskResolver.GiveTask(GUIDTask.RegisterObjectTask(tplayer)(guid).task, holsterTasks ++ inventoryTasks)
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
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterVehicle(vehicle: Vehicle)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val weaponTasks = VisibleSlotTaskBuilding(vehicle.Weapons.values, RegisterEquipment)
    val utilTasks =
      Vehicle.EquipmentUtilities(vehicle.Utilities).values.map(util => { RegisterObjectTask(util()) }).toList
    val inventoryTasks = RegisterInventory(vehicle)
    TaskResolver.GiveTask(RegisterObjectTask(vehicle).task, weaponTasks ++ utilTasks ++ inventoryTasks)
  }

  def RegisterDeployableTurret(
      obj: PlanetSideGameObject with WeaponTurret
  )(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      RegisterObjectTask(obj).task,
      VisibleSlotTaskBuilding(obj.Weapons.values, GUIDTask.RegisterEquipment) ++ RegisterInventory(obj)
    )
  }

  /**
    * Construct tasking that unregisters an object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterObjectTask`.
    * It is the most basic operation that all objects that can have their GUIDs revoked must perform.
    * @param obj the object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterObjectTask`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterObjectTask(obj: IdentifiableEntity)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localObject   = obj
        private val localAccessor = guid

        override def Description: String = s"unregister $localObject"

        override def isComplete: Task.Resolution.Value =
          if (!localObject.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }

        def Execute(resolver: ActorRef): Unit = {
          import net.psforever.objects.guid.actor.Unregister
          localAccessor ! Unregister(localObject, resolver)
        }
      }
    )
  }

  /**
    * Construct tasking that unregisters a `Tool` object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterTool`.
    * @param obj the `Tool` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterTool`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterTool(obj: Tool)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val ammoTasks: List[TaskResolver.GiveTask] =
      (0 until obj.MaxAmmoSlot).map(ammoIndex => UnregisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(UnregisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that unregisters a `LockerContainer` object from a globally unique identifier system.
    * @param obj the object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterLocker`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterLocker(obj: LockerContainer)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(UnregisterObjectTask(obj).task, UnregisterInventory(obj))
  }
  def UnregisterLocker(obj: LockerEquipment)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, RegisterInventory(obj))
  }

  /**
    * Construct tasking that unregisters the objects that are within the given container's inventory
    * from a globally unique identifier system.
    * @param container the storage unit in which objects can be found
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterInventory`<br>
    *       `Container`
    * @return a list of `TaskResolver.GiveTask` messages
    */
  def UnregisterInventory(container: Container)(implicit guid: ActorRef): List[TaskResolver.GiveTask] = {
    container.Inventory.Items.map(entry => { UnregisterEquipment(entry.obj) })
  }

  /**
    * Construct tasking that unregisters an object from a globally unique identifier system
    * after determining whether the object is complex (`Tool` or `Locker`) or is simple.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterEquipment`.
    * @param obj the `Equipment` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterEquipment`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterEquipment(obj: Equipment)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    obj match {
      case tool: Tool =>
        UnregisterTool(tool)
      case _ =>
        UnregisterObjectTask(obj)
    }
  }

  /**
    * Construct tasking that unregisters a `Player` object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterAvatar`.
    * @param tplayer the `Player` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterAvatar`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterAvatar(tplayer: Player)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val holsterTasks   = VisibleSlotTaskBuilding(tplayer.Holsters(), UnregisterEquipment)
    val lockerTask     = List(UnregisterObjectTask(tplayer.avatar.locker))
    val inventoryTasks = UnregisterInventory(tplayer)
    TaskResolver.GiveTask(UnregisterObjectTask(tplayer).task, holsterTasks ++ lockerTask ++ inventoryTasks)
  }

  /**
    * Construct tasking that unregisters a portion of a `Player` object from a globally unique identifier system.<br>
    * <br>
    * Similar to `UnregisterAvatar` but the locker components are skipped.
    * This task performs an operation that reverses the effect of `RegisterPlayer`.
    * @param tplayer the `Player` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterAvatar`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterPlayer(tplayer: Player)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val holsterTasks   = VisibleSlotTaskBuilding(tplayer.Holsters(), UnregisterEquipment)
    val inventoryTasks = UnregisterInventory(tplayer)
    TaskResolver.GiveTask(GUIDTask.UnregisterObjectTask(tplayer).task, holsterTasks ++ inventoryTasks)
  }

  /**
    * Construct tasking that unregisters a `Vehicle` object from a globally unique identifier system.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterVehicle`.
    * @param vehicle the `Vehicle` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterVehicle`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterVehicle(vehicle: Vehicle)(implicit guid: ActorRef): TaskResolver.GiveTask = {
    val weaponTasks = VisibleSlotTaskBuilding(vehicle.Weapons.values, UnregisterEquipment)
    val utilTasks =
      Vehicle.EquipmentUtilities(vehicle.Utilities).values.map(util => { UnregisterObjectTask(util()) }).toList
    val inventoryTasks = UnregisterInventory(vehicle)
    TaskResolver.GiveTask(UnregisterObjectTask(vehicle).task, weaponTasks ++ utilTasks ++ inventoryTasks)
  }

  def UnregisterDeployableTurret(
      obj: PlanetSideGameObject with WeaponTurret
  )(implicit guid: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      UnregisterObjectTask(obj).task,
      VisibleSlotTaskBuilding(obj.Weapons.values, GUIDTask.UnregisterEquipment) ++ UnregisterInventory(obj)
    )
  }

  /**
    * Construct tasking that allocates work upon encountered `Equipment` objects
    * in reference to a globally unique identifier system of a pool of numbers.
    * "Visible slots" are locations that can be viewed by multiple observers across a number of clients.
    * @param list an `Iterable` sequence of `EquipmentSlot` objects that may or may not have equipment
    * @param func the function used to build tasking from any discovered `Equipment`;
    *             strictly either `RegisterEquipment` or `UnregisterEquipment`
    * @param guid implicit reference to a unique number system
    * @return a list of `TaskResolver.GiveTask` messages
    */
  def VisibleSlotTaskBuilding(list: Iterable[EquipmentSlot], func: Equipment => TaskResolver.GiveTask)(implicit
      guid: ActorRef
  ): List[TaskResolver.GiveTask] = {
    recursiveVisibleSlotTaskBuilding(list.iterator, func)
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
      iter: Iterator[EquipmentSlot],
      func: Equipment => TaskResolver.GiveTask,
      list: List[TaskResolver.GiveTask] = Nil
  )(implicit guid: ActorRef): List[TaskResolver.GiveTask] = {
    if (!iter.hasNext) {
      list
    } else {
      iter.next().Equipment match {
        case Some(item) =>
          recursiveVisibleSlotTaskBuilding(iter, func, list :+ func(item))
        case None =>
          recursiveVisibleSlotTaskBuilding(iter, func, list)
      }
    }
  }
}
