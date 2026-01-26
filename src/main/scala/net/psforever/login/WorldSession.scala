package net.psforever.login

import akka.actor.ActorRef
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import net.psforever.objects._
import net.psforever.objects.equipment.{Ammo, Equipment, EquipmentSize}
import net.psforever.objects.guid._
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.locker.LockerContainer
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.sourcing.AmenitySource
import net.psforever.objects.vital.TerminalUsedActivity
import net.psforever.objects.zones.Zone
import net.psforever.types.{ExoSuitType, PlanetSideGUID, TransactionType, Vector3}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object WorldSession {

  /**
    * Convert a boolean value into an integer value.
    * Use: `true:Int` or `false:Int`
    *
    * @param b `true` or `false` (or `null`)
    * @return 1 for `true`; 0 for `false`
    */
  implicit def boolToInt(b: Boolean): Int = if (b) 1 else 0

  private implicit val timeout: Timeout = new Timeout(5000 milliseconds)

  /**
    * Use this for placing equipment that has already been registered into a container,
    * such as in support of changing ammunition types in `Tool` objects (weapons).
    * If the object can not be placed into the container, it will be dropped onto the ground.
    * It will also be dropped if it takes too long to be placed.
    * Item swapping during the placement is not allowed.
    * @see `ask`
    * @see `ChangeAmmoMessage`
    * @see `Containable.CanNotPutItemInSlot`
    * @see `Containable.PutItemAway`
    * @see `Future.onComplete`
    * @see `Future.recover`
    * @see `tell`
    * @see `Zone.Ground.DropItem`
    * @param obj the container
    * @param item the item being manipulated
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def PutEquipmentInInventoryOrDrop(obj: PlanetSideServerObject with Container)(item: Equipment): Future[Any] = {
    val localContainer = obj
    val localItem      = item
    val result         = ask(localContainer.Actor, Containable.PutItemAway(localItem))
    result.onComplete {
      case Failure(_) | Success(_: Containable.CanNotPutItemInSlot) =>
        localContainer.Zone.Ground.tell(
          Zone.Ground.DropItem(localItem, localContainer.Position, Vector3.z(localContainer.Orientation.z)),
          localContainer.Actor
        )
      case _ => ()
    }
    result
  }

  /**
    * Use this for placing equipment that has yet to be registered into a container,
    * such as in support of changing ammunition types in `Tool` objects (weapons).
    * Equipment will go wherever it fits in containing object, or be dropped if it fits nowhere.
    * Item swapping during the placement is not allowed.
    * @see `ChangeAmmoMessage`
    * @see `GUIDTask.registerEquipment`
    * @see `PutEquipmentInInventoryOrDrop`
    * @see `Task`
    * @see `TaskBundle`
    * @param obj the container
    * @param item the item being manipulated
    * @return a `TaskBundle` object
    */
  def PutNewEquipmentInInventorySlot(
                                      obj: PlanetSideServerObject with Container
                                    )(item: Equipment, slot: Int): TaskBundle = {
    val localZone = obj.Zone
    TaskBundle(
      new StraightforwardTask() {
        private val localContainer = obj
        private val localItem      = item
        private val localSlot      = slot

        def action(): Future[Any] = {
          PutEquipmentInInventorySlot(localContainer)(localItem, localSlot)
        }
      },
      GUIDTask.registerEquipment(localZone.GUID, item)
    )
  }

  /**
    * Use this for placing equipment that has yet to be registered into a container,
    * such as in support of changing ammunition types in `Tool` objects (weapons).
    * Equipment will go wherever it fits in containing object, or be dropped if it fits nowhere.
    * Item swapping during the placement is not allowed.
    * @see `ChangeAmmoMessage`
    * @see `GUIDTask.registerEquipment`
    * @see `PutEquipmentInInventoryOrDrop`
    * @see `Task`
    * @see `TaskBundle`
    * @param obj the container
    * @param item the item being manipulated
    * @return a `TaskBundle` object
    */
  def PutNewEquipmentInInventoryOrDrop(
      obj: PlanetSideServerObject with Container
  )(item: Equipment): TaskBundle = {
    val localZone = obj.Zone
    TaskBundle(
      new StraightforwardTask() {
        private val localContainer = obj
        private val localItem      = item

        def action(): Future[Any] = {
          PutEquipmentInInventoryOrDrop(localContainer)(localItem)
        }
      },
      GUIDTask.registerEquipment(localZone.GUID, item)
    )
  }

  /**
    * Use this for obtaining new equipment from a loadout specification.
    * The loadout specification contains a specific slot position for placing the item.
    * This request will (probably) be  coincidental with a number of other such requests based on that loadout
    * so items must be rigidly placed else cascade into a chaostic order.
    * Item swapping during the placement is not allowed.
    * @see `ask`
    * @see `AvatarAction.ObjectDelete`
    * @see `ChangeAmmoMessage`
    * @see `Containable.CanNotPutItemInSlot`
    * @see `Containable.PutItemAway`
    * @see `Future.onComplete`
    * @see `Future.recover`
    * @see `GUIDTask.unregisterEquipment`
    * @see `tell`
    * @see `Zone.AvatarEvents`
    * @param obj the container
    * @param item the item being manipulated
    * @param slot na
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def PutEquipmentInInventorySlot(
      obj: PlanetSideServerObject with Container
  )(item: Equipment, slot: Int): Future[Any] = {
    val localContainer = obj
    val localItem      = item
    val result         = ask(localContainer.Actor, Containable.PutItemInSlotOnly(localItem, slot))
    result.onComplete {
      case Failure(_) | Success(_: Containable.CanNotPutItemInSlot) =>
        TaskWorkflow.execute(GUIDTask.unregisterEquipment(localContainer.Zone.GUID, localItem))
      case _ => ()
    }
    result
  }

  /**
    * Use this for obtaining new equipment from a loadout specification.
    * The loadout specification contains a specific slot position for placing the item.
    * This request will (probably) be  coincidental with a number of other such requests based on that loadout
    * so items must be rigidly placed else cascade into a chaostic order.
    * Item swapping during the placement is not allowed.
    * @see `GUIDTask.registerEquipment`
    * @see `PutEquipmentInInventorySlot`
    * @see `Task`
    * @see `TaskBundle`
    * @param obj the container
    * @param item the item being manipulated
    * @param slot where the item will be placed in the container
    * @return a `TaskBundle` object
    */
  def PutLoadoutEquipmentInInventory(
      obj: PlanetSideServerObject with Container
  )(item: Equipment, slot: Int): TaskBundle = {
    val localZone = obj.Zone
    TaskBundle(
      new StraightforwardTask() {
        private val localItem                                  = item
        private val localSlot                                  = slot
        private val localFunc: (Equipment, Int) => Future[Any] = PutEquipmentInInventorySlot(obj)

        override def description(): String = s"PutEquipmentInInventorySlot - ${localItem.Definition.Name}"

        def action(): Future[Any] = {
          localFunc(localItem, localSlot)
        }
      },
      GUIDTask.registerEquipment(localZone.GUID, item)
    )
  }

  /**
    * Used for purchasing new equipment from a terminal and placing it somewhere in a player's loadout.
    * Two levels of query are performed here based on the behavior expected of the item.
    * First, an attempt is made to place the item anywhere in the target container as long as it does not cause swap items to be generated.
    * Second, if it fails admission to the target container, an attempt is made to place it into the target player's free hand.
    * If the container and the suggested player are the same, it will skip the second attempt.
    * As a terminal operation, the player must receive a report regarding whether the transaction was successful.
    * @see `ask`
    * @see `Containable.CanNotPutItemInSlot`
    * @see `Containable.PutItemInSlotOnly`
    * @see `GUIDTask.registerEquipment`
    * @see `GUIDTask.unregisterEquipment`
    * @see `Future.onComplete`
    * @see `PutEquipmentInInventorySlot`
    * @see `TerminalMessageOnTimeout`
    * @param obj the container
    * @param player na
    * @param term na
    * @param item the item being manipulated
    * @return a `TaskBundle` object
    */
  def BuyNewEquipmentPutInInventory(
      obj: PlanetSideServerObject with Container,
      player: Player,
      term: PlanetSideGUID
  )(item: Equipment): TaskBundle = {
    val localZone = obj.Zone
    TaskBundle(
      new StraightforwardTask() {
        private val localContainer                = obj
        private val localItem                     = item
        private val localPlayer                   = player
        private val localTermMsg: Boolean => Unit = TerminalResult(term, localPlayer, TransactionType.Buy)

        def action(): Future[Any] = {
          TerminalMessageOnTimeout(
            ask(localContainer.Actor, Containable.PutItemAway(localItem)),
            localTermMsg
          )
            .onComplete {
              case Failure(_) | Success(_: Containable.CanNotPutItemInSlot) =>
                if (localContainer != localPlayer) {
                  TerminalMessageOnTimeout(
                    PutEquipmentInInventorySlot(localPlayer)(localItem, Player.FreeHandSlot),
                    localTermMsg
                  )
                    .onComplete {
                      case Failure(_) | Success(_: Containable.CanNotPutItemInSlot) =>
                        localTermMsg(false)
                      case _ =>
                        localTermMsg(true)
                    }
                } else {
                  TaskWorkflow.execute(GUIDTask.unregisterEquipment(localContainer.Zone.GUID, localItem))
                  localTermMsg(false)
                }
              case _ =>
                localTermMsg(true)
            }
          Future(true)
        }
      },
      GUIDTask.registerEquipment(localZone.GUID, item)
    )
  }

  /**
    * The primary use is to register new mechanized assault exo-suit armaments,
    * place the newly registered weapon in hand,
    * and then raise that hand (draw that slot) so that the weapon is active.
    * (Players in MAX suits can not manipulate their drawn slot manually.)
    * In general, this can be used for any equipment that is to be equipped to a player's hand then immediately drawn.
    * Do not allow the item to be (mis)placed in any available slot.
    * Item swapping during the placement is not allowed and the possibility should be proactively avoided.
    * @throws `RuntimeException` if slot is not a player visible slot (holsters)
    * @see `ask`
    * @see `AvatarAction.ObjectDelete`
    * @see `AvatarAction.SendResponse`
    * @see `Containable.CanNotPutItemInSlot`
    * @see `Containable.PutItemInSlotOnly`
    * @see `GUIDTask.registerEquipment`
    * @see `GUIDTask.unregisterEquipment`
    * @see `Future.onComplete`
    * @see `ObjectHeldMessage`
    * @see `Player.DrawnSlot`
    * @see `Player.LastDrawnSlot`
    * @see `Service.defaultPlayerGUID`
    * @see `TaskBundle`
    * @see `Zone.AvatarEvents`
    * @param player the player whose visible slot will be equipped and drawn
    * @param item the item to equip
    * @param slot the slot in which the item will be equipped
    * @return a `TaskBundle` object
    */
  def HoldNewEquipmentUp(player: Player)(item: Equipment, slot: Int): TaskBundle = {
    if (player.VisibleSlots.contains(slot)) {
      val localZone = player.Zone
      TaskBundle(
        TaskToHoldEquipmentUp(player)(item, slot),
        GUIDTask.registerEquipment(localZone.GUID, item)
      )
    } else {
      //TODO log.error
      throw new RuntimeException(s"provided slot $slot is not a player visible slot (holsters)")
    }
  }

  def TaskToHoldEquipmentUp(player: Player)(item: Equipment, slot: Int): Task = {
    new StraightforwardTask() {
      private val localPlayer = player
      private val localGUID   = player.GUID
      private val localItem   = item
      private val localSlot   = slot
      private val localZone   = player.Zone

      def action(): Future[Any] = {
        ask(localPlayer.Actor, Containable.PutItemInSlotOnly(localItem, localSlot))
          .onComplete {
            case Failure(_) | Success(_: Containable.CanNotPutItemInSlot) =>
              TaskWorkflow.execute(GUIDTask.unregisterEquipment(localZone.GUID, localItem))
            case _ =>
              forcedTolowerRaisedArm(localPlayer, localPlayer.GUID, localZone)
              localPlayer.DrawnSlot = localSlot
              localZone.AvatarEvents ! AvatarServiceMessage(localZone.id, localGUID, AvatarAction.ObjectHeld(localSlot, localSlot))
          }
        Future(this)
      }
    }
  }

  /**
    * Get an item from the ground and put it into the given container.
    * The zone in which the item is found is expected to be the same in which the container object is located.
    * If the object can not be placed into the container, it is put back on the ground.
    * The item that was collected off the ground, if it is placed back on the ground,
    * will be positioned with respect to the container object rather than its original location.
    * @see `ask`
    * @see `AvatarAction.ObjectDelete`
    * @see `Future.onComplete`
    * @see `Zone.AvatarEvents`
    * @see `Zone.Ground.CanNotPickUpItem`
    * @see `Zone.Ground.ItemInHand`
    * @see `Zone.Ground.PickUpItem`
    * @see `PutEquipmentInInventoryOrDrop`
    * @param obj the container into which the item will be placed
    * @param item the item being collected from off the ground of the container's zone
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def PickUpEquipmentFromGround(obj: PlanetSideServerObject with Container)(item: Equipment): Future[Any] = {
    val localZone      = obj.Zone
    val localContainer = obj
    val localItem      = item
    val future         = ask(localZone.Ground, Zone.Ground.PickupItem(item.GUID))
    future.onComplete {
      case Success(Zone.Ground.ItemInHand(_)) =>
        PutEquipmentInInventoryOrDrop(localContainer)(localItem).onComplete {
          case Success(Containable.ItemPutInSlot(_, _, Player.FreeHandSlot, _)) =>
            localContainer.Actor ! Zone.Ground.CanNotPickupItem(localZone, localItem.GUID, "@InventoryPickupNoRoom")
          case _ => ()
        }
      case Success(Zone.Ground.CanNotPickupItem(_, item_guid, _)) =>
        localZone.GUID(item_guid) match {
          case Some(_) => ()
          case None => //acting on old data?
            localZone.AvatarEvents ! AvatarServiceMessage(localZone.id, AvatarAction.ObjectDelete(item_guid))
        }
      case _ => ()
    }
    future
  }

  /**
    * Remove an item from a container and drop it on the ground.
    * @see `ask`
    * @see `AvatarAction.ObjectDelete`
    * @see `Containable.ItemFromSlot`
    * @see `Containable.RemoveItemFromSlot`
    * @see `Future.onComplete`
    * @see `Future.recover`
    * @see `tell`
    * @see `Zone.AvatarEvents`
    * @see `Zone.Ground.DropItem`
    * @param obj the container to search
    * @param item the item to find and remove from the container
    * @param pos an optional position where to drop the item on the ground;
    *            expected override from original container's position
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def DropEquipmentFromInventory(
      obj: PlanetSideServerObject with Container
  )(item: Equipment, pos: Option[Vector3] = None): Future[Any] = {
    val localContainer = obj
    val localItem      = item
    val localPos       = pos
    val result         = ask(localContainer.Actor, Containable.RemoveItemFromSlot(localItem))
    result.onComplete {
      case Success(Containable.ItemFromSlot(_, Some(_), Some(_))) =>
        localContainer.Zone.Ground.tell(
          Zone.Ground
            .DropItem(localItem, localPos.getOrElse(localContainer.Position), Vector3.z(localContainer.Orientation.z)),
          localContainer.Actor
        )
      case _ => ()
    }
    result
  }

  /**
    * Remove an item from a container and delete it.
    * @see `ask`
    * @see `AvatarAction.ObjectDelete`
    * @see `Containable.ItemFromSlot`
    * @see `Containable.RemoveItemFromSlot`
    * @see `Future.onComplete`
    * @see `Future.recover`
    * @see `GUIDTask.unregisterEquipment`
    * @see `Zone.AvatarEvents`
    * @param obj the container to search
    * @param item the item to find and remove from the container
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def RemoveOldEquipmentFromInventory(obj: PlanetSideServerObject with Container)(
      item: Equipment
  ): Future[Any] = {
    val localContainer = obj
    val localItem      = item
    val result         = ask(localContainer.Actor, Containable.RemoveItemFromSlot(localItem))
    result.onComplete {
      case Success(Containable.ItemFromSlot(_, Some(_), Some(_))) =>
        TaskWorkflow.execute(GUIDTask.unregisterEquipment(localContainer.Zone.GUID, localItem))
      case _ =>
    }
    result
  }

  /**
    * Primarily, remove an item from a container and delete it.
    * As a terminal operation, the player must receive a report regarding whether the transaction was successful.
    * At the end of a successful transaction, and only a successful transaction,
    * the item that was removed is no longer considered a valid game object.
    * Contrasting `RemoveOldEquipmentFromInventory` which identifies the actual item to be eliminated,
    * this function uses the slot where the item is (should be) located.
    * @see `ask`
    * @see `Containable.ItemFromSlot`
    * @see `Containable.RemoveItemFromSlot`
    * @see `Future.onComplete`
    * @see `Future.recover`
    * @see `GUIDTask.unregisterEquipment`
    * @see `RemoveOldEquipmentFromInventory`
    * @see `TerminalMessageOnTimeout`
    * @see `TerminalResult`
    * @param obj the container to search
    * @param player the player who used the terminal
    * @param term the unique identifier number of the terminal
    * @param slot from which slot the equipment is to be removed
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def SellEquipmentFromInventory(
      obj: PlanetSideServerObject with Container,
      player: Player,
      term: PlanetSideGUID
  )(slot: Int): Future[Any] = {
    val localContainer                = obj
    val localPlayer                   = player
    val localSlot                     = slot
    val localTermMsg: Boolean => Unit = TerminalResult(term, localPlayer, TransactionType.Sell)
    val result = TerminalMessageOnTimeout(
      ask(localContainer.Actor, Containable.RemoveItemFromSlot(localSlot)),
      localTermMsg
    )
    result.onComplete {
      case Success(Containable.ItemFromSlot(_, Some(item), Some(_))) =>
        TaskWorkflow.execute(GUIDTask.unregisterEquipment(localContainer.Zone.GUID, item))
        localTermMsg(true)
      case _ =>
        localTermMsg(false)
    }
    result
  }

  /**
    * Move an item from one container to another.
    * If the source or if the destination is a kind of container called a `LockerContainer`,
    * then a special procedure for the movement of the item must be respected.
    * If the source and the destination are both `LockerContainer` objects, however,
    * the normal operations for moving an item may be executed.
    * @see `ActorRef`
    * @see `Containable.MoveItem`
    * @see `Container`
    * @see `Equipment`
    * @see `LockerContainer`
    * @see `RemoveEquipmentFromLockerContainer`
    * @see `StowEquipmentInLockerContainer`
    * @see `TaskBundle`
    * @param toChannel broadcast channel name for a manual packet callback
    * @param source the container in which the item is to be removed
    * @param destination the container into which the item is to be placed
    * @param item the item
    * @param dest where in the destination container the item is being placed
    */
  def ContainableMoveItem(
                           toChannel: String,
                           source: PlanetSideServerObject with Container,
                           destination: PlanetSideServerObject with Container,
                           item: Equipment,
                           dest: Int
                         ) : Unit = {
    (source, destination) match {
      case (locker: LockerContainer, _) if !destination.isInstanceOf[LockerContainer] =>
        RemoveEquipmentFromLockerContainer(toChannel, locker, destination, item, dest)
      case (_, locker: LockerContainer) =>
        StowEquipmentInLockerContainer(toChannel, source, locker, item, dest)
      case _ =>
        source.Actor ! Containable.MoveItem(destination, item, dest)
    }
  }

  /**
    * Move an item into a player's locker inventory.
    * Handle any swap item that might become involved in the transfer.
    * Failure of this process is not supported and may lead to irregular behavior.
    * @see `ActorRef`
    * @see `AvatarAction.ObjectDelete`
    * @see `AvatarServiceMessage`
    * @see `Containable.MoveItem`
    * @see `Container`
    * @see `Equipment`
    * @see `GridInventory.CheckCollisionsVar`
    * @see `GUIDTask.registerEquipment`
    * @see `GUIDTask.unregisterEquipment`
    * @see `IdentifiableEntity.Invalidate`
    * @see `LockerContainer`
    * @see `Service`
    * @see `Task`
    * @see `TaskBundle`
    * @see `TaskBundle`
    * @see `Zone.AvatarEvents`
    * @param toChannel broadcast channel name for a manual packet callback
    * @param source the container in which the item is to be removed
    * @param destination the container into which the item is to be placed
    * @param item the item
    * @param dest where in the destination container the item is being placed
    */
  def StowEquipmentInLockerContainer(
                                      toChannel: String,
                                      source: PlanetSideServerObject with Container,
                                      destination: PlanetSideServerObject with Container,
                                      item: Equipment,
                                      dest: Int
                                    ): Unit = {
    val (performSwap, swapItemGUID): (Boolean, Option[PlanetSideGUID]) = {
      val tile = item.Definition.Tile
      destination.Inventory.CheckCollisionsVar(dest, tile.Width, tile.Height)
    } match {
      case Success(Nil)
        if ContainableBehavior.PermitEquipmentStow(destination, item, dest) =>
        //no swap item
        (true, None)
      case Success(List(swapEntry: InventoryItem))
        if ContainableBehavior.PermitEquipmentStow(destination, item, dest) =>
        //the swap item is to be registered to the source's zone
        (true, Some(swapEntry.obj.GUID))
      case _ =>
        //too many swap items or other error; this attempt will not execute
        (false, None)
    }
    if (performSwap) {
      def moveItemTaskFunc(toSlot: Int): Task = new StraightforwardTask() {
        val localGUID: Option[PlanetSideGUID] = swapItemGUID //the swap item's original GUID, if any swap item
        val localChannel: String = toChannel
        val localSource: PlanetSideServerObject with Container = source
        val localDestination: PlanetSideServerObject with Container = destination
        val localItem: Equipment = item
        val localDestSlot: Int = dest
        val localSrcSlot: Int = toSlot
        val localMoveOnComplete: Try[Any] => Unit = {
          case Success(Containable.ItemPutInSlot(_, _, _, Some(swapItem))) =>
            //swapItem is not registered right now, we can not drop the item without re-registering it
            TaskWorkflow.execute(PutNewEquipmentInInventorySlot(localSource)(swapItem, localSrcSlot))
          case _ => ()
        }

        override def description(): String = s"unregistering $localItem before stowing in $localDestination"

        def action(): Future[Any] = {
          localGUID match {
            case Some(guid) =>
              //see LockerContainerControl.RemoveItemFromSlotCallback
              localSource.Zone.AvatarEvents ! AvatarServiceMessage(localChannel, AvatarAction.ObjectDelete(guid))
            case None => ()
          }
          val moveResult = ask(localDestination.Actor, Containable.PutItemInSlotOrAway(localItem, Some(localDestSlot)))
          moveResult.onComplete(localMoveOnComplete)
          moveResult
        }
      }
      val resultOnComplete: Try[Any] => Unit = {
        case Success(Containable.ItemFromSlot(fromSource, Some(itemToMove), Some(fromSlot))) =>
          TaskWorkflow.execute(TaskBundle(
            moveItemTaskFunc(fromSlot),
            GUIDTask.unregisterEquipment(fromSource.Zone.GUID, itemToMove)
          ))
        case _ => ()
      }
      val result = ask(source.Actor, Containable.RemoveItemFromSlot(item))
      result.onComplete(resultOnComplete)
    }
  }

  /**
    * Remove an item from a player's locker inventory.
    * Failure of this process is not supported and may lead to irregular behavior.
    * @see `ActorRef`
    * @see `AvatarAction.ObjectDelete`
    * @see `AvatarServiceMessage`
    * @see `Containable.MoveItem`
    * @see `Container`
    * @see `Equipment`
    * @see `GridInventory.CheckCollisionsVar`
    * @see `GUIDTask.registerEquipment`
    * @see `GUIDTask.unregisterEquipment`
    * @see `IdentifiableEntity.Invalidate`
    * @see `LockerContainer`
    * @see `Service`
    * @see `Task`
    * @see `TaskBundle`
    * @see `TaskBundle`
    * @see `Zone.AvatarEvents`
    * @param toChannel broadcast channel name for a manual packet callback
    * @param source the container in which the item is to be removed
    * @param destination the container into which the item is to be placed
    * @param item the item
    * @param dest where in the destination container the item is being placed
    */
  def RemoveEquipmentFromLockerContainer(
                                          toChannel: String,
                                          source: PlanetSideServerObject with Container,
                                          destination: PlanetSideServerObject with Container,
                                          item: Equipment,
                                          dest: Int
                                        ): Unit = {
    val (performSwap, swapItemGUID): (Boolean, Option[PlanetSideGUID]) = {
      val destInv = destination.Inventory
      if (destInv.Offset <= dest && destInv.Offset + destInv.TotalCapacity >= dest) {
        val tile = item.Definition.Tile
        destInv.CheckCollisionsVar(dest, tile.Width, tile.Height)
      } else {
        val slot = destination.Slot(dest)
        if (slot.Size != EquipmentSize.Blocked) {
          slot.Equipment match {
            case Some(thing) => Success(List(InventoryItem(thing, dest)))
            case None        => Success(Nil)
          }
        } else {
          Failure(new Exception(""))
        }
      }
    } match {
      case Success(Nil) =>
        //no swap item
        (true, None)
      case Success(List(swapEntry: InventoryItem)) =>
        //the swap item is to be registered to the source's zone
        (true, Some(swapEntry.obj.GUID))
      case _ =>
        //too many swap items or other error; this attempt will not execute
        (false, None)
    }
    if (performSwap) {
      def moveItemTaskFunc(toSlot: Int): Task = new StraightforwardTask() {
        val localGUID: Option[PlanetSideGUID] = swapItemGUID //the swap item's original GUID, if any swap item
        val localChannel: String = toChannel
        val localSource: PlanetSideServerObject with Container = source
        val localDestination: PlanetSideServerObject with Container = destination
        val localItem: Equipment = item
        val localDestSlot: Int = dest
        val localSrcSlot: Int = toSlot
        val localMoveOnComplete: Try[Any] => Unit = {
          case Success(Containable.ItemPutInSlot(_, _, _, Some(swapItem))) =>
            //swapItem is not registered right now, we can not drop the item without re-registering it
            TaskWorkflow.execute(PutNewEquipmentInInventorySlot(localSource)(swapItem, localSrcSlot))
          case _ => ()
        }

        override def description(): String = s"registering $localItem in ${localDestination.Zone.id} before removing from $localSource"

        def action(): Future[Any] = {
          localGUID match {
            case Some(guid) =>
              //see LockerContainerControl.RemoveItemFromSlotCallback
              localSource.Zone.AvatarEvents ! AvatarServiceMessage(localChannel, AvatarAction.ObjectDelete(guid))
            case None => ()
          }
          val moveResult = ask(localDestination.Actor, Containable.PutItemInSlotOrAway(localItem, Some(localDestSlot)))
          moveResult.onComplete(localMoveOnComplete)
          moveResult
        }
      }
      val resultOnComplete: Try[Any] => Unit = {
        case Success(Containable.ItemFromSlot(fromSource, Some(itemToMove), Some(fromSlot))) =>
          TaskWorkflow.execute(TaskBundle(
            moveItemTaskFunc(fromSlot),
            GUIDTask.registerEquipment(fromSource.Zone.GUID, itemToMove)
          ))
        case _ => ()
      }
      val result = ask(source.Actor, Containable.RemoveItemFromSlot(item))
      result.onComplete(resultOnComplete)
    }
  }

  /**
   * Quickly draw a grenade from anywhere on the player's person and place it into a certain hand
   * at the ready to be used as a weapon.
   * Soldiers in mechanized assault exo-suits can not perform this action.<br>
   * <br>
   * This is not vanilla behavior.<br>
   * <br>
   * Search for a grenade of either fragmentation- or plasma-type in the hands (holsters) or backpack (inventory)
   * and bring it to hand and draw that grenade as a weapon as quickly as possible.
   * If the player has a weapon already drawn, remove it from his active hand quickly.
   * It may be placed back into the slot once the hand is / will be occupied by a grenade.
   * For anything in the first sidearm weapon slot, where the grenade will be placed,
   * either find room in the backpack for it or drop it on the ground.
   * If the player's already-drawn hand is the same as the one that will hold the grenade (first sidearm holster),
   * treat it like the sidearm occupier rather than the already-drawn weapon -
   * the old weapon goes into the backpack or onto the ground.
   * @see `AvatarAction.ObjectHeld`
   * @see `AvatarServiceMessage`
   * @see `Containable.RemoveItemFromSlot`
   * @see `countRestrictAttempts`
   * @see `forcedTolowerRaisedArm`
   * @see `GlobalDefinitions.isGrenade`
   * @see `InventoryItem`
   * @see `Player.DrawnSlot`
   * @see `Player.HandsDownSlot`
   * @see `Player.Holsters`
   * @see `Player.ResistArmMotion`
   * @see `Player.Slot`
   * @see `PutEquipmentInInventoryOrDrop`
   * @see `PutEquipmentInInventorySlot`
   * @see `TaskBundle`
   * @see `TaskToHoldEquipmentUp`
   * @see `TaskWorkflow.execute`
   * @param tplayer player who wants to draw a grenade
   * @param equipSlot slot being used as the final destination for any discovered grenade
   * @param log reference to the messaging protocol
   * @return if there was a discovered grenade
   */
  def QuickSwapToAGrenade(
                           tplayer: Player,
                           equipSlot: Int,
                           log: org.log4s.Logger): Boolean = {
    if (tplayer.ExoSuit != ExoSuitType.MAX) {
      val previouslyDrawnSlot = tplayer.DrawnSlot
      val optGrenadeInSlot = {
        tplayer.Holsters().zipWithIndex.find { case (slot, _) =>
          slot.Equipment match {
            case Some(equipment) =>
              val definition = equipment.Definition
              val name = definition.Name
              GlobalDefinitions.isGrenade(definition) && (name.contains("frag") || name.contains("plasma"))
            case _ =>
              false
          }
        } match {
          case Some((_, slotNum)) if slotNum == previouslyDrawnSlot =>
            //grenade already in hand; do nothing
            None
          case Some((grenadeSlot, slotNum)) =>
            //grenade is holstered in some other slot; just extend it (or swap hands)
            val guid = tplayer.GUID
            val zone = tplayer.Zone
            val grenade = grenadeSlot.Equipment.get
            val drawnSlotItem = tplayer.Slot(tplayer.DrawnSlot).Equipment
            if (forcedTolowerRaisedArm(tplayer, guid, zone)) {
              log.info(s"${tplayer.Name} has dropped ${tplayer.Sex.possessive} ${drawnSlotItem.get.Definition.Name}")
            }
            //put up hand with grenade in it
            tplayer.DrawnSlot = slotNum
            zone.AvatarEvents ! AvatarServiceMessage(zone.id, guid, AvatarAction.ObjectHeld(slotNum, slotNum))
            log.info(s"${tplayer.Name} has quickly drawn a ${grenade.Definition.Name}")
            None
          case None =>
            //check inventory for a grenade
            tplayer.Inventory.Items.find { case InventoryItem(equipment, _) =>
              val definition = equipment.Definition
              val name = definition.Name
              GlobalDefinitions.isGrenade(definition) && (name.contains("frag") || name.contains("plasma"))
            } match {
              case Some(InventoryItem(equipment, slotNum)) => Some(equipment.asInstanceOf[Tool], slotNum)
              case _                                       => None
            }
        }
      }
      optGrenadeInSlot match {
        case Some((grenade, slotNum)) =>
          val itemInPreviouslyDrawnSlotToDrop = if (equipSlot != previouslyDrawnSlot) {
            forcedTolowerRaisedArm(tplayer, tplayer.GUID, tplayer.Zone)
            tplayer.Slot(previouslyDrawnSlot).Equipment match {
              case out @ Some(_) =>
                tplayer.ResistArmMotion(countRestrictAttempts(count=1))
                out
              case _ =>
                None
            }
          } else {
            None
          }
          val itemPreviouslyInPistolSlot = tplayer.Slot(equipSlot).Equipment
          val result = for {
            //remove grenade from inventory
            a <- ask(tplayer.Actor, Containable.RemoveItemFromSlot(slotNum))
            //remove equipment from pistol slot, where grenade will go
            b <- itemPreviouslyInPistolSlot match {
              case Some(_) => ask(tplayer.Actor, Containable.RemoveItemFromSlot(equipSlot))
              case _       => Future(true)
            }
            //remove held equipment (if any)
            c <- itemInPreviouslyDrawnSlotToDrop match {
              case Some(_) => ask(tplayer.Actor, Containable.RemoveItemFromSlot(previouslyDrawnSlot))
              case _       => Future(false)
            }
          } yield (a, b, c)
          result.onComplete {
            case Success((_, _, _)) =>
              //put equipment in hand and hold grenade up
              TaskWorkflow.execute(TaskBundle(TaskToHoldEquipmentUp(tplayer)(grenade, equipSlot)))
              //what to do with the equipment that was removed for the grenade
              itemPreviouslyInPistolSlot match {
                case Some(e) =>
                  log.info(s"${tplayer.Name} has dropped ${tplayer.Sex.possessive} ${e.Definition.Name}")
                  PutEquipmentInInventoryOrDrop(tplayer)(e)
                case _ => ()
              }
              //restore previously-held-up equipment
              itemInPreviouslyDrawnSlotToDrop match {
                case Some(e) => PutEquipmentInInventorySlot(tplayer)(e, previouslyDrawnSlot)
                case _ => ()
              }
              log.info(s"${tplayer.Name} has quickly drawn a ${grenade.Definition.Name}")
            case _ => ()
          }
        case None => ()
      }
      optGrenadeInSlot.nonEmpty
    } else {
      false
    }
  }

  /**
   * If the player has a raised arm, lower it.
   * Do it manually, bypassing the checks in the normal procedure.
   * @see `AvatarAction.ObjectHeld`
   * @see `AvatarServiceMessage`
   * @see `Player.DrawnSlot`
   * @see `Player.HandsDownSlot`
   * @param tplayer the player
   * @param guid target guid (usually the player)
   * @param zone the zone of reporting
   * @return if the hand has a drawn equipment in it and tries to lower
   */
  private def forcedTolowerRaisedArm(tplayer: Player, guid:PlanetSideGUID, zone: Zone): Boolean = {
    val slot = tplayer.DrawnSlot
    if (slot != Player.HandsDownSlot) {
      tplayer.DrawnSlot = Player.HandsDownSlot
      zone.AvatarEvents ! AvatarServiceMessage(zone.id, guid, AvatarAction.ObjectHeld(Player.HandsDownSlot, slot))
      true
    } else {
      false
    }
  }

  /**
   * Restriction logic that stops the player
   * from lowering or raising any drawn equipment a certain number of times.
   * Reset to default restriction behavior when no longer valid.
   * @see `Player.neverRestrict`
   * @see `Player.ResistArmMotion`
   * @param count number of times to stop the player from adjusting their arm
   * @param player target player
   * @param slot slot being switched to (unused here)
   * @return if the motion is restricted
   */
  def countRestrictAttempts(count: Int)(player: Player, slot: Int): Boolean = {
    if (count > 0) {
      player.ResistArmMotion(countRestrictAttempts(count - 1))
      true
    } else {
      player.ResistArmMotion(Player.neverRestrict) //reset
      false
    }
  }

  /**
    * If a timeout occurs on the manipulation, declare a terminal transaction failure.
    * @see `AskTimeoutException`
    * @see `recover`
    * @param future the item manipulation's `Future` object
    * @param terminalMessage how to call the terminal message
    * @return a `Future` that anticipates the resolution to this manipulation
    */
  def TerminalMessageOnTimeout(future: Future[Any], terminalMessage: Boolean => Unit): Future[Any] = {
    future.recover {
      case _: AskTimeoutException =>
        terminalMessage(false)
    }
  }

  /**
    * Announced the result of this player's terminal use, to the player that used the terminal.
    * This is a necessary step for regaining terminal use which is naturally blocked by the client after a transaction request.
    * @see `AvatarAction.TerminalOrderResult`
    * @see `ItemTransactionResultMessage`
    * @see `TransactionType`
    * @param guid the terminal's unique identifier
    * @param player the player who used the terminal
    * @param transaction what kind of transaction was involved in terminal use
    * @param result the result of that transaction
    */
  def TerminalResult(guid: PlanetSideGUID, player: Player, transaction: TransactionType.Value)(
      result: Boolean
  ): Unit = {
    if (result) {
      player.Zone.GUID(guid).collect { case term: Terminal =>
        player.LogActivity(TerminalUsedActivity(AmenitySource(term), transaction))
        player.ContributionFrom(term)
      }
    }
    player.Zone.AvatarEvents ! AvatarServiceMessage(
      player.Name,
      AvatarAction.TerminalOrderResult(guid, transaction, result)
    )
  }

  /**
    * Drop some items on the ground is a given location.
    * The location corresponds to the previous container for those items.
    * @see `Zone.Ground.DropItem`
    * @param container the original object that contained the items
    * @param drops the items to be dropped on the ground
    */
  def DropLeftovers(container: PlanetSideServerObject with Container)(drops: Iterable[InventoryItem]): Unit = {
    //drop or retire
    val zone   = container.Zone
    val pos    = container.Position
    val orient = Vector3.z(container.Orientation.z)
    //TODO make a sound when dropping stuff?
    drops.foreach { entry => zone.Ground.tell(Zone.Ground.DropItem(entry.obj, pos, orient), container.Actor) }
  }

  /**
    * Within a specified `Container`, find the smallest number of `Equipment` objects of a certain qualifying type
    * whose sum count is greater than, or equal to, a `desiredAmount` based on an accumulator method.<br>
    * <br>
    * In an occupied `List` of returned `Inventory` entries, all but the last entry is typically considered "emptied."
    * For objects with contained quantities, the last entry may require having that quantity be set to a non-zero number.
    * @param obj the `Container` to search
    * @param filterTest test used to determine inclusivity of `Equipment` collection
    * @param desiredAmount how much is requested
    * @param counting test used to determine value of found `Equipment`;
    *                 defaults to one per entry
    * @return a `List` of all discovered entries totaling approximately the amount requested
    */
  def FindEquipmentStock(
      obj: Container,
      filterTest: Equipment => Boolean,
      desiredAmount: Int,
      counting: Equipment => Int = DefaultCount
  ): List[InventoryItem] = {
    var currentAmount: Int = 0
    obj.Inventory.Items
      .filter(item => filterTest(item.obj))
      .sortBy(_.start)
      .takeWhile(entry => {
        val previousAmount = currentAmount
        currentAmount += counting(entry.obj)
        previousAmount < desiredAmount
      })
  }

  /**
    * The default counting function for an item.
    * Counts the number of item(s).
    * @param e the `Equipment` object
    * @return the quantity;
    *         always one
    */
  def DefaultCount(e: Equipment): Int = 1

  /**
    * The counting function for an item of `AmmoBox`.
    * Counts the `Capacity` of the ammunition.
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountAmmunition(e: Equipment): Int = {
    e match {
      case a: AmmoBox => a.Capacity
      case _          => 0
    }
  }

  /**
    * The counting function for an item of `Tool` where the item is also a grenade.
    * Counts the number of grenades.
    * @see `GlobalDefinitions.isGrenade`
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountGrenades(e: Equipment): Int = {
    e match {
      case t: Tool => (GlobalDefinitions.isGrenade(t.Definition): Int) * t.Magazine
      case _       => 0
    }
  }

  /**
    * Flag an `AmmoBox` object that matches for the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is an `AmmoBox` of the correct ammunition type; `false`, otherwise
    */
  def FindAmmoBoxThatUses(ammo: Ammo.Value)(e: Equipment): Boolean = {
    e match {
      case t: AmmoBox => t.AmmoType == ammo
      case _          => false
    }
  }

  /**
    * Flag a `Tool` object that matches for loading the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is a `Tool` that loads the correct ammunition type; `false`, otherwise
    */
  def FindToolThatUses(ammo: Ammo.Value)(e: Equipment): Boolean = {
    e match {
      case t: Tool =>
        t.Definition.AmmoTypes.map { _.AmmoType }.contains(ammo)
      case _ =>
        false
    }
  }

  /**
   * Perform a task and, upon completion, dispatch a message.
   * @param task task to be completed first
   * @param sendTo where to send the message
   * @param pass message
   * @return a `TaskBundle` object
   */
  def CallBackForTask(task: TaskBundle, sendTo: ActorRef, pass: Any): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localDesc   = task.description()
        private val destination = sendTo
        private val passMsg     = pass

        override def description(): String = s"callback for tasking $localDesc"

        def action() : Future[Any] = {
          destination ! passMsg
          Future(this)
        }
      },
      task
    )
  }

  /**
   * Perform a task and, upon completion, dispatch a message whose origin is specific and different from normal.
   * @param task task to be completed first
   * @param sendTo where to send the message
   * @param pass message
   * @param replyTo whom to attribute the message being passed (e.g., `tell`)
   * @return a `TaskBundle` object
   */
  def CallBackForTask(task: TaskBundle, sendTo: ActorRef, pass: Any, replyTo: ActorRef): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localDesc   = task.description()
        private val destination = sendTo
        private val passMsg     = pass

        override def description(): String = s"callback for tasking $localDesc"

        def action() : Future[Any] = {
          destination.tell(passMsg, replyTo)
          Future(this)
        }
      },
      task
    )
  }
}
