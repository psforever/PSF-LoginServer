// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import akka.actor.{ActorContext, ActorRef, Cancellable, Props}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.pad.process.{VehicleSpawnControlBase, VehicleSpawnControlConcealPlayer}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{DefaultCancellable, Player, Vehicle}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * The purpose of the base actor is to serve as the entry point for the spawning process.
  * A spawn pad receives vehicle orders from an attached `Terminal` object.
  * The control object accepts orders, enqueues them, and,
  * whenever prompted by a previous complete order or by an absence of active orders,
  * will select the first available order to be completed.
  * This order will be "tracked" and will be given to the first functional "spawn control" object of the process.
  * If the process is completed, or is ever aborted by any of the subsequent tasks,
  * control will propagate down back to this control object.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControl(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) with FactionAffinityBehavior.Check {
  /** a reminder sent to future customers */
  var periodicReminder : Cancellable = DefaultCancellable.obj
  /** a list of vehicle orders that have been submitted for this spawn pad */
  private var orders : List[VehicleSpawnControl.Order] = List.empty[VehicleSpawnControl.Order]
  /** the current vehicle order being acted upon;
    * used as a guard condition to control order processing rate */
  private var trackedOrder : Option[VehicleSpawnControl.Order] = None

  def LogId = ""

  /**
    * The first chained action of the vehicle spawning process.
    */
  val concealPlayer = context.actorOf(Props(classOf[VehicleSpawnControlConcealPlayer], pad), s"${context.parent.path.name}-conceal")

  def FactionObject : FactionAffinity = pad

  def receive : Receive = checkBehavior.orElse {
    case VehicleSpawnPad.VehicleOrder(player, vehicle) =>
      trace(s"order from $player for $vehicle received")
      orders = orders :+ VehicleSpawnControl.Order(player, vehicle, sender)
      if(trackedOrder.isEmpty && orders.length == 1) {
        self ! VehicleSpawnControl.ProcessControl.GetOrder
      }
      else {
        sender ! VehicleSpawnControl.RenderOrderRemainderMsg(orders.length + 1)
      }

    case VehicleSpawnControl.ProcessControl.GetOrder =>
      trackedOrder match {
        case None =>
          periodicReminder.cancel
          val (completeOrder, remainingOrders) : (Option[VehicleSpawnControl.Order], List[VehicleSpawnControl.Order]) = orders match {
            case x :: Nil =>
              (Some(x), Nil)
            case x :: b =>
              trace(s"order backlog size: ${b.size}")
              VehicleSpawnControl.recursiveOrderReminder(b.iterator)
              (Some(x), b)
            case Nil =>
              (None, Nil)
          }
          orders = remainingOrders
          completeOrder match {
            case Some(entry) =>
              trace(s"processing next order - a ${entry.vehicle.Definition.Name} for ${entry.driver.Name}")
              trackedOrder = completeOrder //guard on
              context.system.scheduler.scheduleOnce(2000 milliseconds, concealPlayer, VehicleSpawnControl.Process.ConcealPlayer(entry))
            case None =>
              trackedOrder = None
          }
        case Some(_) => ; //do not work on new orders
      }

    case VehicleSpawnControl.ProcessControl.CancelOrder =>
      VehicleSpawnControl.recursiveFindOrder(orders.iterator, sender) match {
        case None => ;
        case Some(index) =>
          val dequeuedOrder = orders(index)
          orders = orders.take(index - 1) ++ orders.drop(index + 1)
          trace(s"${dequeuedOrder.driver}'s vehicle order has been cancelled")
      }

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      if(sender == concealPlayer) {
        trackedOrder = None //guard off
        self ! VehicleSpawnControl.ProcessControl.GetOrder
      }

    case VehicleSpawnControl.ProcessControl.Reminder =>
      /*
      When the vehicle is spawned and added to the pad, it will "occupy" the pad and block it from further action.
      Normally, the player who wanted to spawn the vehicle will be automatically put into the driver seat.
      If this is blocked, the vehicle will idle on the pad and must be moved far enough away from the point of origin.
      During this time, a periodic message about the spawn pad being blocked
      will be broadcast to all current customers in the order queue.
       */
      if(periodicReminder.isCancelled) {
        trace(s"the pad has become blocked by ${trackedOrder.get.vehicle.Definition.Name}")
        periodicReminder = context.system.scheduler.schedule(
          VehicleSpawnControl.initialReminderDelay,
          VehicleSpawnControl.periodicReminderDelay,
          self, VehicleSpawnControl.ProcessControl.Reminder
        )
      }
      else {
        VehicleSpawnControl.BlockedReminder(trackedOrder, trackedOrder.get +: orders)
      }

    case _ => ;
  }
}

object VehicleSpawnControl {
  private final val initialReminderDelay : FiniteDuration = 10000 milliseconds
  private final val periodicReminderDelay : FiniteDuration = 10000 milliseconds

  /**
    * A `TaskResolver` to assist with the deconstruction of vehicles.
    * Treated like a `lazy val`, this only gets defined once and then keeps getting reused.
    * Since the use case is "if something goes wrong," a limited implementation should be fine.
    */
  private var emergencyResolver : Option[ActorRef] = None

  /**
    * An `Enumeration` of non-data control messages for the vehicle spawn process.
    */
  object ProcessControl extends Enumeration {
    val
    Reminder,
    GetOrder,
    GetNewOrder,
    CancelOrder
    = Value
  }
  /**
    * An `Enumeration` of the stages of a full vehicle spawning process, passing the current order being processed.
    * Messages in this group are used by the `receive` entry points of the multiple child objects
    * that perform the vehicle spawning operation.
    */
  object Process {
    sealed class Order(entry : VehicleSpawnControl.Order)

    final case class ConcealPlayer(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class LoadVehicle(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class SeatDriver(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class AwaitDriverInSeat(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class DriverInSeat(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class RailJackAction(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class RailJackRelease(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class ServerVehicleOverride(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class DriverVehicleControl(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class FinalClearance(entry : VehicleSpawnControl.Order) extends Order(entry)
  }

  /**
    * An entry that stores vehicle spawn pad spawning tasks (called "orders").
    * @param driver the player who wants the vehicle
    * @param vehicle the vehicle
    * @param sendTo a callback `Actor` associated with the player (in other words, `WorldSessionActor`)
    */
  final case class Order(driver : Player, vehicle : Vehicle, sendTo : ActorRef)

  /**
    * Properly clean up a vehicle that has been registered, but not yet been spawned into the game world.
    * @param vehicle the vehicle
    * @param player the driver
    * @param zone the continent on which the vehicle was registered
    * @param context an `ActorContext` object for which to create the `TaskResolver` object
    */
  def DisposeVehicle(vehicle : Vehicle, player : Player, zone: Zone)(implicit context : ActorContext) : Unit = {
    import net.psforever.objects.guid.GUIDTask
    emergencyResolver.getOrElse({
      import akka.routing.SmallestMailboxPool
      import net.psforever.objects.guid.TaskResolver
      val resolver = context.actorOf(SmallestMailboxPool(10).props(Props[TaskResolver]), "vehicle-spawn-control-emergency-decon-resolver")
      emergencyResolver = Some(resolver)
      resolver
    }) ! GUIDTask.UnregisterVehicle(vehicle)(zone.GUID)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(player.GUID, zone.Id)
  }
  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * @param vehicle the vehicle
    * @param player the driver
    * @param zone the continent on which the vehicle was registered
    */
  def DisposeSpawnedVehicle(vehicle : Vehicle, player : Player, zone: Zone) : Unit = {
    zone.VehicleEvents ! VehicleSpawnPad.DisposeVehicle(vehicle, zone)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(player.GUID, zone.Id)
  }

  /**
    * Remind a customer how long it will take for their vehicle order to be processed.
    * @param position position in the queue
    * @return an index-appropriate `VehicleSpawnPad.PeriodicReminder` object
    */
  def RenderOrderRemainderMsg(position : Int) : VehicleSpawnPad.PeriodicReminder = {
    VehicleSpawnPad.PeriodicReminder(s"Your position in the vehicle spawn queue is $position.")
  }

  /**
    *
    * @param blockedOrder the previous order whose vehicle is blocking the spawn pad from operating
    * @param recipients all of the customers who will be receiving the message
    */
  def BlockedReminder(blockedOrder : Option[VehicleSpawnControl.Order], recipients : Seq[VehicleSpawnControl.Order]) : Unit = {
    blockedOrder match {
      case Some(entry) =>
        val msg : String = if(entry.vehicle.Health == 0) {
          "The vehicle spawn where you placed your order is blocked by wreckage."
        }
        else {
          "The vehicle spawn where you placed your order is blocked."
        }
        VehicleSpawnControl.recursiveBlockedReminder(recipients.iterator, msg)
      case None => ;
    }
  }

  @tailrec private final def recursiveFindOrder(iter : Iterator[VehicleSpawnControl.Order], target : ActorRef, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val recipient = iter.next
      if(recipient.sendTo == target) {
        Some(index)
      }
      else {
        recursiveFindOrder(iter, target, index + 1)
      }
    }
  }

  @tailrec private final def recursiveBlockedReminder(iter : Iterator[VehicleSpawnControl.Order], msg : String) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      if(recipient.sendTo != ActorRef.noSender) {
        recipient.sendTo ! VehicleSpawnPad.PeriodicReminder(msg)
      }
      recursiveBlockedReminder(iter, msg)
    }
  }

  @tailrec private final def recursiveOrderReminder(iter : Iterator[VehicleSpawnControl.Order], position : Int = 2) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      if(recipient.sendTo != ActorRef.noSender) {
        recipient.sendTo ! RenderOrderRemainderMsg(position)
      }
      recursiveOrderReminder(iter, position + 1)
    }
  }
}
