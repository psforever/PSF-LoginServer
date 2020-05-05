// Copyright (c) 2017-2020 PSForever
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects._
import net.psforever.objects.equipment.{Ammo, Equipment}
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.serverobject.{Containable, PlanetSideServerObject}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ItemTransactionResultMessage, ObjectHeldMessage}
import net.psforever.types.{PlanetSideGUID, TransactionType, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.implicitConversions

object WorldSession {
  /**
    * Convert a boolean value into an integer value.
    * Use: `true:Int` or `false:Int`
    * @param b `true` or `false` (or `null`)
    * @return 1 for `true`; 0 for `false`
    */
  implicit def boolToInt(b : Boolean) : Int = if(b) 1 else 0
  private implicit val timeout = new Timeout(1000 milliseconds)

  def PutEquipmentInInventorySlot(obj : PlanetSideServerObject with Container, taskResolver : ActorRef)(item : Equipment, slot : Int) : Unit = {
    val localContainer = obj
    val localItem = item
    val localResolver = taskResolver
    implicit val timeout = new Timeout(1000 milliseconds)
    ask(localContainer.Actor, Containable.PutItemInSlotOnly(localItem, slot)).onComplete {
      case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
        localResolver ! GUIDTask.UnregisterEquipment(localItem)(localContainer.Zone.GUID)
      case _ => ;
    }
  }

  def PutEquipmentInInventoryOrDrop(obj : PlanetSideServerObject with Container, to : ActorRef)(item : Equipment, slot : Int) : Unit = {
    val localContainer = obj
    val localItem = item
    val sendTo = to
    ask(localContainer.Actor, Containable.PutItemAway(localItem)).onComplete {
      case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
        localContainer.Zone.Ground.tell(Zone.Ground.DropItem(localItem, localContainer.Position, Vector3.z(localContainer.Orientation.z)), sendTo)
      case _ => ;
    }
  }

  def PutNewEquipmentInInventory(obj : PlanetSideServerObject with Container, taskResolver : ActorRef)(item : Equipment, slot : Int) : TaskResolver.GiveTask = {
    val localZone = obj.Zone
    TaskResolver.GiveTask(
      new Task() {
        private val localContainer = obj
        private val localItem = item
        private val localResolver = taskResolver

        override def isComplete : Task.Resolution.Value = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          ask(localContainer.Actor, Containable.PutItemAway(localItem)).onComplete {
            case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
              localResolver ! GUIDTask.UnregisterEquipment(localItem)(localContainer.Zone.GUID)
            case _ => ;
          }
          resolver ! scala.util.Success(this)
        }
      },
      List(GUIDTask.RegisterEquipment(item)(localZone.GUID))
    )
  }

  def PutLoadoutEquipmentInInventory(obj : PlanetSideServerObject with Container, taskResolver : ActorRef)(item : Equipment, slot : Int) : TaskResolver.GiveTask = {
    val localZone = obj.Zone
    TaskResolver.GiveTask(
      new Task() {
        private val localContainer = obj
        private val localItem = item
        private val localSlot = slot
        private val localResolver = taskResolver

        override def isComplete : Task.Resolution.Value = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          ask(localContainer.Actor, Containable.PutItemInSlotOnly(localItem, localSlot)).onComplete {
            case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
              localResolver ! GUIDTask.UnregisterEquipment(localItem)(localContainer.Zone.GUID)
            case _ => ;
          }
          resolver ! scala.util.Success(this)
        }
      },
      List(GUIDTask.RegisterEquipment(item)(localZone.GUID))
    )
  }

  def BuyNewEquipmentPutInInventory(obj : PlanetSideServerObject with Container, taskResolver : ActorRef, player : Player, term : PlanetSideGUID)(item : Equipment) : TaskResolver.GiveTask = {
    val localZone = obj.Zone
    TaskResolver.GiveTask(
      new Task() {
        private val localContainer = obj
        private val localItem = item
        private val localPlayer = player
        private val localResolver = taskResolver
        private val localTermMsg : Boolean=>Unit = TerminalResult(term, localPlayer, TransactionType.Buy)

        override def isComplete : Task.Resolution.Value = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          ask(localContainer.Actor, Containable.PutItemAway(localItem)).onComplete {
            case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
              ask(localPlayer.Actor, Containable.PutItemInSlotOnly(localItem, Player.FreeHandSlot)).onComplete {
                case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
                  localResolver ! GUIDTask.UnregisterEquipment(localItem)(localContainer.Zone.GUID)
                  localTermMsg(false)
                case _ =>
                  localTermMsg(true)
              }
            case _ =>
              localTermMsg(true)
          }
          resolver ! scala.util.Success(this)
        }
      },
      List(GUIDTask.RegisterEquipment(item)(localZone.GUID))
    )
  }

  def SellEquipmentFromInventory(obj : PlanetSideServerObject with Container, taskResolver : ActorRef, player : Player, term : PlanetSideGUID)(slot : Int) : Unit = {
    val localContainer = obj
    val localPlayer = player
    val localSlot = slot
    val localResolver = taskResolver
    val localTermMsg : Boolean=>Unit = TerminalResult(term, localPlayer, TransactionType.Sell)

    ask(localContainer.Actor, Containable.RemoveItemFromSlot(localSlot)).onComplete {
      case scala.util.Success(Containable.ItemFromSlot(_, Some(item), Some(_))) =>
        localResolver ! GUIDTask.UnregisterEquipment(item)(localContainer.Zone.GUID)
        localTermMsg(true)
      case _ =>
        localTermMsg(false)
    }
  }

  def DropEquipmentFromInventory(obj : PlanetSideServerObject with Container, to : ActorRef)(item : Equipment, pos : Option[Vector3] = None) : Unit = {
    val localContainer = obj
    val localItem = item
    val localPos = pos
    val sendTo = to

    ask(localContainer.Actor, Containable.RemoveItemFromSlot(localItem)).onComplete {
      case scala.util.Success(Containable.ItemFromSlot(_, Some(_), Some(_))) =>
        localContainer.Zone.Ground.tell(Zone.Ground.DropItem(localItem, localPos.getOrElse(localContainer.Position), Vector3.z(localContainer.Orientation.z)), sendTo)
      case _ => ;
    }
  }

  def RemoveOldEquipmentFromInventory(obj : PlanetSideServerObject with Container, taskResolver : ActorRef)(item : Equipment) : Unit = {
    val localContainer = obj
    val localItem = item
    val localResolver = taskResolver

    ask(localContainer.Actor, Containable.RemoveItemFromSlot(localItem)).onComplete {
      case scala.util.Success(Containable.ItemFromSlot(_, Some(_), Some(_))) =>
        localResolver ! GUIDTask.UnregisterEquipment(localItem)(localContainer.Zone.GUID)
      case _ =>
    }
  }

  def HoldNewEquipmentUp(player : Player, taskResolver : ActorRef)(item : Equipment, slot : Int) : TaskResolver.GiveTask = {
    if(player.VisibleSlots.contains(slot)) {
      val localZone = player.Zone
      TaskResolver.GiveTask(
        new Task() {
          private val localPlayer = player
          private val localGUID = player.GUID
          private val localItem = item
          private val localSlot = slot
          private val localResolver = taskResolver

          override def isComplete : Task.Resolution.Value = Task.Resolution.Success

          def Execute(resolver : ActorRef) : Unit = {
            ask(localPlayer.Actor, Containable.PutItemInSlotOnly(localItem, localSlot)).onComplete {
              case scala.util.Failure(_) | scala.util.Success(_ : Containable.CanNotPutItemInSlot) =>
                localResolver ! GUIDTask.UnregisterEquipment(localItem)(localZone.GUID)
              case _ =>
                localPlayer.DrawnSlot = localSlot
                localZone.AvatarEvents ! AvatarServiceMessage(localPlayer.Name,
                  AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectHeldMessage(localGUID, localSlot, true))
                )
                localZone.AvatarEvents ! AvatarServiceMessage(localZone.Id,
                  AvatarAction.ObjectHeld(localGUID, localSlot)
                )
            }
            resolver ! scala.util.Success(this)
          }
        },
        List(GUIDTask.RegisterEquipment(item)(localZone.GUID))
      )
    }
    else {
      //TODO log.error rather than println
      println(s"HoldNewEquipmentUp: slot $slot is not visible for player model")
      //TODO null is okay?
      null
    }
  }

  def TerminalResult(guid : PlanetSideGUID, player : Player, transaction : TransactionType.Value)(result : Boolean) : Unit = {
    player.Zone.AvatarEvents ! AvatarServiceMessage(player.Name, AvatarAction.TerminalOrderResult(guid, transaction, true))
  }

  /**
    * na
    * @param dropOrDelete na
    */
  def DropOrDeleteLeftovers(player : Player, taskResolver : ActorRef)(dropOrDelete : List[InventoryItem]) : Unit = {
    //drop or retire
    val zone = player.Zone
    val pos = player.Position
    val orient = Vector3.z(player.Orientation.z)
    val (finalDroppedItems, retiredItems) = dropOrDelete.partition(Containable.DropPredicate(player))
    //drop special items on ground
    //TODO make a sound when dropping stuff?
    finalDroppedItems.foreach { entry => zone.Ground ! Zone.Ground.DropItem(entry.obj, pos, orient) }
    //deconstruct normal items
    retiredItems.foreach{ entry => taskResolver ! GUIDTask.UnregisterEquipment(entry.obj)(zone.GUID) }
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
  def FindEquipmentStock(obj : Container,
                         filterTest : Equipment=>Boolean,
                         desiredAmount : Int,
                         counting : Equipment=>Int = DefaultCount) : List[InventoryItem] = {
    var currentAmount : Int = 0
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
  def DefaultCount(e : Equipment) : Int = 1

  /**
    * The counting function for an item of `AmmoBox`.
    * Counts the `Capacity` of the ammunition.
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountAmmunition(e : Equipment) : Int = {
    e match {
      case a : AmmoBox => a.Capacity
      case _ => 0
    }
  }

  /**
    * The counting function for an item of `Tool` where the item is also a grenade.
    * Counts the number of grenades.
    * @see `GlobalDefinitions.isGrenade`
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountGrenades(e : Equipment) : Int = {
    e match {
      case t : Tool => (GlobalDefinitions.isGrenade(t.Definition):Int) * t.Magazine
      case _ => 0
    }
  }

  /**
    * Flag an `AmmoBox` object that matches for the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is an `AmmoBox` of the correct ammunition type; `false`, otherwise
    */
  def FindAmmoBoxThatUses(ammo : Ammo.Value)(e : Equipment) : Boolean = {
    e match {
      case t : AmmoBox => t.AmmoType == ammo
      case _ => false
    }
  }

  /**
    * Flag a `Tool` object that matches for loading the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is a `Tool` that loads the correct ammunition type; `false`, otherwise
    */
  def FindToolThatUses(ammo : Ammo.Value)(e : Equipment) : Boolean = {
    e match {
      case t : Tool =>
        t.Definition.AmmoTypes.map { _.AmmoType }.contains(ammo)
      case _ =>
        false
    }
  }
}
