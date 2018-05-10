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
      orders = orders :+ VehicleSpawnControl.Order(player, vehicle, sender)
      if(trackedOrder.isEmpty && orders.length == 1) {
        SelectOrder()
      }
      else {
        sender ! VehicleSpawnControl.RenderOrderRemainderMsg(orders.length + 1)
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
            VehicleSpawnControl.BlockedReminder(entry, entry +: orders)
          }
        case None => ;
          periodicReminder.cancel
      }

    case VehicleSpawnControl.ProcessControl.Flush =>
      if(!periodicReminder.isCancelled) {
        periodicReminder.cancel
        orders.foreach { VehicleSpawnControl.CancelOrder(_, Continent) }
        orders = Nil
        trackedOrder match {
          case Some(entry) =>
            VehicleSpawnControl.CancelOrder(entry, Continent)
          case None => ;
        }
        trackedOrder = None
        concealPlayer ! akka.actor.Kill //will cause the actor to restart, which will abort any trapped messages
      }

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
    * An `Enumeration` of the stages of a full vehicle spawning process, passing the current order being processed.
    * Messages in this group are used by the `receive` entry points of the multiple child objects
    * that perform the vehicle spawning operation.
    */
  object Process {
    sealed class Order(entry : VehicleSpawnControl.Order)

    final case class ConcealPlayer(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class LoadVehicle(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class SeatDriver(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class RailJackAction(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class ServerVehicleOverride(entry : VehicleSpawnControl.Order) extends Order(entry)
    final case class StartGuided(entry : VehicleSpawnControl.Order) extends Order(entry)
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
    * Properly clean up a vehicle that has been registered, but not yet been spawned into the game world.<br>
    * <br>
    * Constructs a temporary `TaskResolver` to deal with the vehicle's registration status.
    * This "temporary" router will persist as if it were a `static` variable in some other language
    * due to the fact that the `ActorSystem` object will remember it existing.
    * After the primary task is complete, the router that was created is stopped so that it can be garbage collected.
    * We could re-use it theoretically, but the `context` might be untrustworthy.
    * @param entry the order being cancelled
    * @param zone the continent on which the vehicle was registered
    * @param context an `ActorContext` object for which to create the `TaskResolver` object
    */
  def DisposeVehicle(entry : VehicleSpawnControl.Order, zone: Zone)(implicit context : ActorContext) : Unit = {
    import akka.actor.{ActorRef, PoisonPill}
    import akka.routing.SmallestMailboxPool
    import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
    import net.psforever.types.Vector3
    val vehicle = entry.vehicle
    vehicle.Position = Vector3.Zero
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(entry.driver.GUID, zone.Id)

    val router = context.actorOf(
      SmallestMailboxPool(10).props(Props[TaskResolver]),
      s"vehicle-spawn-control-emergency-decon-resolver-${System.nanoTime}"
    )
    router !
    TaskResolver.GiveTask(
      new Task() {
        private val localRouter = router

        override def isComplete = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }

        override def Cleanup() : Unit = { localRouter ! PoisonPill } //where the router is stopped
      }, List(GUIDTask.UnregisterVehicle(vehicle)(zone.GUID))
    )
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * @param entry the order being cancelled
    * @param zone the continent on which the vehicle was registered
    */
  def DisposeSpawnedVehicle(entry : VehicleSpawnControl.Order, zone: Zone) : Unit = {
    //TODO this cleanup will handle the vehicle; but, the former driver may be thrown into the void
    zone.VehicleEvents ! VehicleSpawnPad.DisposeVehicle(entry.vehicle, zone)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(entry.driver.GUID, zone.Id)
  }

  /**
    * Remind a customer how long it will take for their vehicle order to be processed.
    * @param position position in the queue
    * @return an index-appropriate `VehicleSpawnPad.PeriodicReminder` object
    */
  def RenderOrderRemainderMsg(position : Int) : VehicleSpawnPad.PeriodicReminder = {
    VehicleSpawnPad.PeriodicReminder(VehicleSpawnPad.Reminders.Queue, Some(s"$position"))
  }

  /**
    *
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
      VehicleSpawnControl.recursiveBlockedReminder(recipients.iterator, wrecked)
  }

  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param entry the order being cancelled
    * @param zone the continent on which the vehicle was registered
    * @param context an `ActorContext` object for which to create the `TaskResolver` object
    */
  def CancelOrder(entry : VehicleSpawnControl.Order, zone : Zone)(implicit context : ActorContext) : Unit = {
    val vehicle = entry.vehicle
    if(vehicle.Seats.values.count(_.isOccupied) == 0) {
      if(vehicle.Actor != ActorRef.noSender) {
        VehicleSpawnControl.DisposeSpawnedVehicle(entry, zone)
      }
      else {
        VehicleSpawnControl.DisposeVehicle(entry, zone)
      }
      if(entry.sendTo != ActorRef.noSender) {
        entry.sendTo ! VehicleSpawnPad.PeriodicReminder(VehicleSpawnPad.Reminders.Cancelled)
      }
    }
  }

//  @tailrec private final def recursiveFindOrder(iter : Iterator[VehicleSpawnControl.Order], target : ActorRef, index : Int = 0) : Option[Int] = {
//    if(!iter.hasNext) {
//      None
//    }
//    else {
//      val recipient = iter.next
//      if(recipient.sendTo == target) {
//        Some(index)
//      }
//      else {
//        recursiveFindOrder(iter, target, index + 1)
//      }
//    }
//  }

  @tailrec private final def recursiveBlockedReminder(iter : Iterator[VehicleSpawnControl.Order], cause : Option[Any]) : Unit = {
    if(iter.hasNext) {
      val recipient = iter.next
      recipient.sendTo ! VehicleSpawnPad.PeriodicReminder(VehicleSpawnPad.Reminders.Blocked, cause)
      recursiveBlockedReminder(iter, cause)
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
