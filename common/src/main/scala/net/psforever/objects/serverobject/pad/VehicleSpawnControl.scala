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
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad` entity.
  * The basic `VehicleSpawnControl` is the root of a sequence of "spawn control" objects that chain to each other.
  * Each object performs one (or more related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * The purpose of the base actor is to serve as the entry point for the spawning process
  * and to manage the order queue.
  * A spawn pad receives vehicle orders from a related `Terminal` object.
  * The control object accepts orders, enqueues them, and,
  * whenever prompted by a previous complete order or by an absence of active orders,
  * will select the first available order to be completed.
  * This order will be "tracked" and will be given to the first functional "spawn control" object of the process.
  * If the process is completed, or is ever aborted by any of the subsequent tasks,
  * control will propagate down back to this control object.
  * At this time, (or) once again, a new order can be submitted or will be selected.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControl(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) with FactionAffinityBehavior.Check {
  /** a reminder sent to future customers */
  var periodicReminder : Cancellable = DefaultCancellable.obj
  /** a list of vehicle orders that have been submitted for this spawn pad */
  var orders : List[VehicleSpawnControl.Order] = List.empty[VehicleSpawnControl.Order]
  /** the current vehicle order being acted upon;
    * used as a guard condition to control order processing rate */
  var trackedOrder : Option[VehicleSpawnControl.Order] = None
  /** how to process either the first order or every subsequent order */
  var handleOrderFunc : VehicleSpawnControl.Order => Unit = NewTasking

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
      trace(s"order from ${player.Name} for a ${vehicle.Definition.Name} received")
      try {
        handleOrderFunc(VehicleSpawnControl.Order(player, vehicle))
      }
      catch {
        case _ : AssertionError if vehicle.HasGUID => //same as order being dropped
          VehicleSpawnControl.DisposeSpawnedVehicle(vehicle, pad.Zone)
        case _ : AssertionError => ; //shrug
        case e : Exception => //something unexpected
          e.printStackTrace()
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
            BlockedReminder(entry, orders)
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
      concealPlayer ! akka.actor.Kill //should cause the actor to restart, which will abort any trapped messages

    case _ => ;
  }

  /**
    * Take this order - the "first order" - and immediately begin processing it.
    * All orders accepted in the meantime will be queued and a note about priority will be issued.
    * @param order the order being accepted
    */
  def NewTasking(order : VehicleSpawnControl.Order) : Unit = {
    handleOrderFunc = QueuedTasking
    ProcessOrder(Some(order))
  }

  /**
    * While an order is being processed,
    * all orders accepted in the meantime will be queued and a note about priority will be issued.
    * @param order the order being accepted
    */
  def QueuedTasking(order : VehicleSpawnControl.Order) : Unit = {
    val name = order.driver.Name
    if((trackedOrder match {
      case Some(tracked) => !tracked.driver.Name.equals(name)
      case None => true
    }) && orders.forall { !_.driver.Name.equals(name) }) {
      //not a second order from an existing order's player
      orders = orders :+ order
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(name, VehicleSpawnPad.Reminders.Queue, Some(orders.length + 1))
    }
    else {
      VehicleSpawnControl.DisposeSpawnedVehicle(order, pad.Zone)
    }
  }

  /**
    * Select the next available queued order and begin processing it.
    */
  def SelectOrder() : Unit = ProcessOrder(SelectFirstOrder())

  /**
    * Select the next-available queued order if there is no current order being fulfilled.
    * If the queue has been exhausted, set functionality to prepare to accept the next order as a "first order."
    * @return the next-available order
    */
  def SelectFirstOrder() : Option[VehicleSpawnControl.Order] = {
    trackedOrder match {
      case None =>
        val (completeOrder, remainingOrders) : (Option[VehicleSpawnControl.Order], List[VehicleSpawnControl.Order]) = orders match {
          case x :: Nil =>
            (Some(x), Nil)
          case x :: b =>
            (Some(x), b)
          case Nil =>
            handleOrderFunc = NewTasking
            (None, Nil)
        }
        orders = remainingOrders
        completeOrder
      case Some(_) =>
        None
    }
  }

  /**
    * If a new order is accepted, begin processing it.
    * Inform all customers whose orders are still queued of their priority number
    * and activate the guard to ensure multiple orders don't get processed at the same time.
    * @param order the order being accepted;
    *              `None`, if no order found or submitted
    */
  def ProcessOrder(order : Option[VehicleSpawnControl.Order]) : Unit = {
    periodicReminder.cancel
    order match {
      case Some(_order) =>
        recursiveOrderReminder(orders.iterator)
        trace(s"processing next order - a ${_order.vehicle.Definition.Name} for ${_order.driver.Name}")
        trackedOrder = order //guard on
        context.system.scheduler.scheduleOnce(2000 milliseconds, concealPlayer, _order)
      case None => ;
    }
  }

  /**
    * na
    * @param blockedOrder the previous order whose vehicle is blocking the spawn pad from operating
    * @param recipients all of the other customers who will be receiving the message
    */
  def BlockedReminder(blockedOrder : VehicleSpawnControl.Order, recipients : Seq[VehicleSpawnControl.Order]) : Unit = {
    val relevantRecipients = blockedOrder.vehicle.Seats(0).Occupant.orElse(pad.Zone.GUID(blockedOrder.vehicle.Owner)) match {
      case Some(p : Player) =>
        (VehicleSpawnControl.Order(p, blockedOrder.vehicle) +: recipients).iterator //who took possession of the vehicle
      case _ =>
        (blockedOrder +: recipients).iterator //who ordered the vehicle
    }
    recursiveBlockedReminder(relevantRecipients,
      if(blockedOrder.vehicle.Health == 0)
        Option("Clear the wreckage.")
      else
        None
    )
  }

  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param entry the order being cancelled
    * @param context an `ActorContext` object for which to create the `TaskResolver` object
    */
  def CancelOrder(entry : VehicleSpawnControl.Order)(implicit context : ActorContext) : Unit = {
    val vehicle = entry.vehicle
    if(vehicle.Seats.values.count(_.isOccupied) == 0) {
      VehicleSpawnControl.DisposeSpawnedVehicle(entry, pad.Zone)
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(entry.driver.Name, VehicleSpawnPad.Reminders.Cancelled)
    }
  }

  @tailrec private final def recursiveBlockedReminder(iter : Iterator[VehicleSpawnControl.Order], cause : Option[Any]) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(recipient.driver.Name, VehicleSpawnPad.Reminders.Blocked, cause)
      recursiveBlockedReminder(iter, cause)
    }
  }

  @tailrec private final def recursiveOrderReminder(iter : Iterator[VehicleSpawnControl.Order], position : Int = 2) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(recipient.driver.Name, VehicleSpawnPad.Reminders.Queue, Some(position))
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
    * An entry that stores a vehicle spawn pad spawning task (called an "order").
    * @param driver the player who wants the vehicle
    * @param vehicle the vehicle
    */
  final case class Order(driver : Player, vehicle : Vehicle) {
    assert(driver.HasGUID, s"when ordering a vehicle, the prospective driver ${driver.Name} does not have a GUID")
    assert(vehicle.HasGUID, s"when ordering a vehicle, the ${vehicle.Definition.Name} does not have a GUID")
    val DriverGUID = driver.GUID
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * Call this downstream of "`ConcealPlayer`".
    * @param entry the order being cancelled
    * @param zone the continent on which the vehicle was registered
    */
  def DisposeSpawnedVehicle(entry : VehicleSpawnControl.Order, zone: Zone) : Unit = {
    DisposeSpawnedVehicle(entry.vehicle, zone)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(entry.DriverGUID)
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * @param vehicle the vehicle being cancelled
    * @param zone the continent on which the vehicle was registered
    */
  def DisposeSpawnedVehicle(vehicle : Vehicle, zone: Zone) : Unit = {
    zone.VehicleEvents ! VehicleSpawnPad.DisposeVehicle(vehicle)
  }
}
