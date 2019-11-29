// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import akka.actor.{ActorContext, Cancellable, Props}
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

  import akka.actor.SupervisorStrategy._
  override val supervisorStrategy = {
    import akka.actor.OneForOneStrategy
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _ : akka.actor.ActorKilledException => Restart
      case _ => Resume
    }
  }

  def receive : Receive = checkBehavior.orElse {
    case VehicleSpawnPad.VehicleOrder(player, vehicle) =>
      trace(s"order from $player for $vehicle received")
      orders = orders :+ VehicleSpawnControl.Order(player, vehicle)
      if(trackedOrder.isEmpty && orders.length == 1) {
        SelectOrder()
      }
      else {
        pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(player.Name, VehicleSpawnPad.Reminders.Queue, Some(orders.length + 1))
      }

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      if(sender == concealPlayer) {
        trackedOrder = None //guard off
        SelectOrder()
      }

    /*
    When the vehicle is spawned and added to the pad, it will "occupy" the pad and block it from further action.
    Normally, the player who wanted to spawn the vehicle will be automatically put into the driver seat.
    If this is blocked, the vehicle will idle on the pad and must be moved far enough away from the point of origin.
    During this time, a periodic message about the spawn pad being blocked
    will be broadcast to all current customers in the order queue.
     */
    case VehicleSpawnControl.ProcessControl.Reminder =>
      trackedOrder match {
        case Some(entry) =>
          if(periodicReminder.isCancelled) {
            trace (s"the pad has become blocked by ${entry.vehicle.Definition.Name}")
            periodicReminder = context.system.scheduler.schedule(
              VehicleSpawnControl.initialReminderDelay,
              VehicleSpawnControl.periodicReminderDelay,
              self, VehicleSpawnControl.ProcessControl.Reminder
            )
          }
          else {
            BlockedReminder(entry, entry +: orders)
          }
        case None => ;
          periodicReminder.cancel
      }

    case VehicleSpawnControl.ProcessControl.Flush =>
      periodicReminder.cancel
      orders.foreach { CancelOrder }
      orders = Nil
      trackedOrder match {
        case Some(entry) =>
          CancelOrder(entry)
        case None => ;
      }
      trackedOrder = None
      concealPlayer ! akka.actor.Kill //will cause the actor to restart, which will abort any trapped messages

    case _ => ;
  }

  def SelectOrder() : Unit = {
    trackedOrder match {
      case None =>
        periodicReminder.cancel
        val (completeOrder, remainingOrders) : (Option[VehicleSpawnControl.Order], List[VehicleSpawnControl.Order]) = orders match {
          case x :: Nil =>
            (Some(x), Nil)
          case x :: b =>
            trace(s"order backlog size: ${b.size}")
            recursiveOrderReminder(b.iterator)
            (Some(x), b)
          case Nil =>
            (None, Nil)
        }
        orders = remainingOrders
        completeOrder match {
          case Some(entry) =>
            trace(s"processing next order - a ${entry.vehicle.Definition.Name} for ${entry.driver.Name}")
            trackedOrder = completeOrder //guard on
            context.system.scheduler.scheduleOnce(2000 milliseconds, concealPlayer, entry)
          case None =>
            trackedOrder = None
        }
      case Some(_) => ; //do not work on new orders
    }
  }

  /**
    * na
    * @param blockedOrder the previous order whose vehicle is blocking the spawn pad from operating
    * @param recipients all of the customers who will be receiving the message
    */
  def BlockedReminder(blockedOrder : VehicleSpawnControl.Order, recipients : Seq[VehicleSpawnControl.Order]) : Unit = {
    val wrecked : Option[Any] = if(blockedOrder.vehicle.Health == 0) {
      Option("Clear the wreckage.")
    }
    else {
      None
    }
    recursiveBlockedReminder(recipients.iterator, wrecked)
  }

  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param entry the order being cancelled
    * @param context an `ActorContext` object for which to create the `TaskResolver` object
    */
  def CancelOrder(entry : VehicleSpawnControl.Order)(implicit context : ActorContext) : Unit = {
    val vehicle = entry.vehicle
    if(vehicle.Seats.values.count(_.isOccupied) == 0) {
      VehicleSpawnControl.DisposeSpawnedVehicle(entry, pad.Owner.Zone)
      pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(entry.driver.Name, VehicleSpawnPad.Reminders.Cancelled)
    }
  }

  @tailrec private final def recursiveBlockedReminder(iter : Iterator[VehicleSpawnControl.Order], cause : Option[Any]) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(recipient.driver.Name, VehicleSpawnPad.Reminders.Blocked, cause)
      recursiveBlockedReminder(iter, cause)
    }
  }

  @tailrec private final def recursiveOrderReminder(iter : Iterator[VehicleSpawnControl.Order], position : Int = 2) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(recipient.driver.Name, VehicleSpawnPad.Reminders.Queue, Some(position))
      recursiveOrderReminder(iter, position + 1)
    }
  }
}

object VehicleSpawnControl {
  private final val initialReminderDelay : FiniteDuration = 10000 milliseconds
  private final val periodicReminderDelay : FiniteDuration = 10000 milliseconds

  /**
    * An `Enumeration` of non-data control messages for the vehicle spawn process.
    */
  object ProcessControl extends Enumeration {
    val
    Reminder,
    GetNewOrder,
    Flush
    = Value
  }

  /**
    * An entry that stores vehicle spawn pad spawning tasks (called "orders").
    * @param driver the player who wants the vehicle
    * @param vehicle the vehicle
    */
  final case class Order(driver : Player, vehicle : Vehicle) {
    assert(driver.HasGUID, "when ordering a vehicle, driver does not have GUID")
    val DriverGUID = driver.GUID
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * @param entry the order being cancelled
    * @param zone the continent on which the vehicle was registered
    */
  def DisposeSpawnedVehicle(entry : VehicleSpawnControl.Order, zone: Zone) : Unit = {
    zone.VehicleEvents ! VehicleSpawnPad.DisposeVehicle(entry.vehicle)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(entry.DriverGUID)
  }
}
