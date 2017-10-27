// Copyright (c) 2017 PSForever
package scripts

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
  * All functions produce a `TaskResolver.GiveTask` container object that is expected to be used by a `TaskResolver`.
  * These "task containers" can also be unpackaged into their tasks, sorted into other containers,
  * and combined with other "task containers" to enact more complicated sequences of operations.
  */
object GUIDTask {
  import akka.actor.ActorRef
  import net.psforever.objects.entity.IdentifiableEntity
  import net.psforever.objects.equipment.Equipment
  import net.psforever.objects.guid.{Task, TaskResolver}
  import net.psforever.objects.{EquipmentSlot, Player, Tool, Vehicle}

  import scala.annotation.tailrec
  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers.<br>
    * <br>
    * Regardless of the complexity of the object provided to this function, only the current depth will be assigned a GUID.
    * This is the most basic operation that all objects that can be assigned a GUID must perform.
    * @param obj the object being registered
    * @param guid implicit reference to a unique number system
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterObjectTask(obj : IdentifiableEntity)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localObject = obj
        private val localAccessor = guid

        override def isComplete : Task.Resolution.Value = if(localObject.HasGUID) {
          Task.Resolution.Success
        }
        else {
          Task.Resolution.Incomplete
        }

        def Execute(resolver : ActorRef) : Unit = {
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
  def RegisterTool(obj : Tool)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    val ammoTasks : List[TaskResolver.GiveTask] = (0 until obj.MaxAmmoSlot).map(ammoIndex => RegisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that registers an object with a globally unique identifier selected from a pool of numbers,
    * after determining whether the object is complex (`Tool`) or simple.<br>
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
  def RegisterEquipment(obj : Equipment)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    obj match {
      case tool : Tool =>
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
  def RegisterAvatar(tplayer : Player)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    import net.psforever.objects.LockerContainer
    import net.psforever.objects.inventory.InventoryItem
    val holsterTasks = recursiveHolsterTaskBuilding(tplayer.Holsters().iterator, RegisterEquipment)
    val fifthHolsterTask = tplayer.Slot(5).Equipment match {
      case Some(locker) =>
        RegisterObjectTask(locker) :: locker.asInstanceOf[LockerContainer].Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => RegisterEquipment(entry.obj)}).toList
      case None =>
        List.empty[TaskResolver.GiveTask];
    }
    val inventoryTasks = tplayer.Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => RegisterEquipment(entry.obj)})
    TaskResolver.GiveTask(RegisterObjectTask(tplayer).task, holsterTasks ++ fifthHolsterTask ++ inventoryTasks)
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
  def RegisterVehicle(vehicle : Vehicle)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    import net.psforever.objects.inventory.InventoryItem
    val weaponTasks = vehicle.Weapons.map({ case(_ : Int, entry : EquipmentSlot) => RegisterEquipment(entry.Equipment.get)}).toList
    val inventoryTasks = vehicle.Trunk.Items.map({ case((_ : Int, entry : InventoryItem)) => RegisterEquipment(entry.obj)})
    TaskResolver.GiveTask(RegisterObjectTask(vehicle).task, weaponTasks ++ inventoryTasks)
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
  def UnregisterObjectTask(obj : IdentifiableEntity)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localObject = obj
        private val localAccessor = guid

        override def isComplete : Task.Resolution.Value = if(!localObject.HasGUID) {
          Task.Resolution.Success
        }
        else {
          Task.Resolution.Incomplete
        }

        def Execute(resolver : ActorRef) : Unit = {
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
  def UnregisterTool(obj : Tool)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    val ammoTasks : List[TaskResolver.GiveTask] = (0 until obj.MaxAmmoSlot).map(ammoIndex => UnregisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(UnregisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that unregisters an object from a globally unique identifier system
    * after determining whether the object is complex (`Tool`) or simple.<br>
    * <br>
    * This task performs an operation that reverses the effect of `RegisterEquipment`.
    * @param obj the `Equipment` object being unregistered
    * @param guid implicit reference to a unique number system
    * @see `GUIDTask.RegisterEquipment`
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterEquipment(obj : Equipment)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    obj match {
      case tool : Tool =>
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
  def UnregisterAvatar(tplayer : Player)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    import net.psforever.objects.LockerContainer
    import net.psforever.objects.inventory.InventoryItem
    val holsterTasks = recursiveHolsterTaskBuilding(tplayer.Holsters().iterator, UnregisterEquipment)
    val inventoryTasks = tplayer.Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => UnregisterEquipment(entry.obj)})
    val fifthHolsterTask = tplayer.Slot(5).Equipment match {
      case Some(locker) =>
        UnregisterObjectTask(locker) :: locker.asInstanceOf[LockerContainer].Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => UnregisterEquipment(entry.obj)}).toList
      case None =>
        List.empty[TaskResolver.GiveTask];
    }
    TaskResolver.GiveTask(UnregisterObjectTask(tplayer).task, holsterTasks ++ fifthHolsterTask ++ inventoryTasks)
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
  def UnregisterVehicle(vehicle : Vehicle)(implicit guid : ActorRef) : TaskResolver.GiveTask = {
    import net.psforever.objects.inventory.InventoryItem
    val weaponTasks = vehicle.Weapons.map({ case(_ : Int, entry : EquipmentSlot) => UnregisterTool(entry.Equipment.get.asInstanceOf[Tool]) }).toList
    val inventoryTasks = vehicle.Trunk.Items.map({ case((_ : Int, entry : InventoryItem)) => UnregisterEquipment(entry.obj)})
    TaskResolver.GiveTask(UnregisterObjectTask(vehicle).task, weaponTasks ++ inventoryTasks)
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Use `func` on any discovered `Equipment` to transform items into tasking, and add the tasking to a `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param func the function used to build tasking from any discovered `Equipment`;
    *             strictly either `RegisterEquipment` or `UnregisterEquipment`
    * @param list a persistent `List` of `Equipment` tasking
    * @return a `List` of `Equipment` tasking
    */
  @tailrec private def recursiveHolsterTaskBuilding(iter : Iterator[EquipmentSlot], func : ((Equipment)=>TaskResolver.GiveTask), list : List[TaskResolver.GiveTask] = Nil)(implicit guid : ActorRef) : List[TaskResolver.GiveTask] = {
    if(!iter.hasNext) {
      list
    }
    else {
      iter.next.Equipment match {
        case Some(item) =>
          recursiveHolsterTaskBuilding(iter, func, list :+ func(item))
        case None =>
          recursiveHolsterTaskBuilding(iter, func, list)
      }
    }
  }
}