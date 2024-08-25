// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import akka.actor.{ActorRef, Cancellable, OneForOneStrategy, Props}
import net.psforever.objects.avatar.SpecialCarry
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.pad.process.{VehicleSpawnControlBase, VehicleSpawnControlConcealPlayer}
import net.psforever.objects.sourcing.AmenitySource
import net.psforever.objects.vital.TerminalUsedActivity
import net.psforever.objects.zones.{Zone, ZoneAware, Zoning}
import net.psforever.objects.{Default, PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.{PlanetSideGUID, TransactionType, Vector3}

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
  *
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControl(pad: VehicleSpawnPad)
    extends VehicleSpawnControlBase(pad)
    with FactionAffinityBehavior.Check {

  /** a reminder sent to future customers */
  private var periodicReminder: Cancellable = Default.Cancellable

  /** repeatedly test whether queued orders are valid */
  private var queueManagement: Cancellable = Default.Cancellable

  /** a list of vehicle orders that have been submitted for this spawn pad */
  private var orders: List[VehicleSpawnPad.VehicleOrder] = List.empty[VehicleSpawnPad.VehicleOrder]

  /** the current vehicle order being acted upon;
    * used as a guard condition to control order processing rate
    */
  private var trackedOrder: Option[VehicleSpawnControl.Order] = None

  /** how to process either the first order or every subsequent order */
  private var handleOrderFunc: VehicleSpawnPad.VehicleOrder => Unit = NewTasking

  /** ... */
  private var reminderSeq: Seq[Int] = Seq()

  def LogId = ""

  /**
    * The first chained action of the vehicle spawning process.
    */
  private val concealPlayer: ActorRef =
    context.actorOf(Props(classOf[VehicleSpawnControlConcealPlayer], pad), s"${context.parent.path.name}-conceal")

  def FactionObject: FactionAffinity = pad

  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _ =>
        log.warn(s"vehicle spawn pad restarted${trackedOrder.map { o => s"; an unfulfilled order for ${o.driver.Name} will be expunged" }.getOrElse("")}")
        Restart
    }
  }

  override def postStop() : Unit = {
    periodicReminder.cancel()
    queueManagement.cancel()
  }

  def receive: Receive =
    checkBehavior.orElse {
      case msg @ VehicleSpawnPad.VehicleOrder(player, vehicle, _) =>
        trace(s"order from ${player.Name} for a ${vehicle.Definition.Name} received")
        try {
          handleOrderFunc(msg)
        } catch {
          case _: AssertionError => () //ehhh
          case e: Exception => e.printStackTrace() //something unexpected
        }

      case VehicleSpawnControl.ProcessControl.OrderCancelled =>
        trackedOrder.collect {
          case entry if sender() == concealPlayer =>
            CancelOrder(
              entry,
              VehicleSpawnControl.validateOrderCredentials(pad, entry.driver, entry.vehicle)
                .orElse(Some("@SVCP_RemovedFromVehicleQueue_Generic"))
            )
        }
        trackedOrder = None //guard off
        SelectOrder()

      case VehicleSpawnControl.ProcessControl.GetNewOrder =>
        if (sender() == concealPlayer) {
          trackedOrder = None //guard off
          SelectOrder()
        }

      case VehicleSpawnControl.ProcessControl.QueueManagement =>
        queueManagementTask()

      case VehicleSpawnControl.ProcessControl.Reminder =>
        evaluateBlockedReminder()

      case VehicleSpawnControl.ProcessControl.Flush =>
        periodicReminder.cancel()
        orders.foreach { CancelOrder(_, Some("@SVCP_RemovedFromVehicleQueue_Generic")) }
        orders = Nil
        trackedOrder.foreach {
          entry => CancelOrder(entry, Some("@SVCP_RemovedFromVehicleQueue_Generic"))
        }
        trackedOrder = None
        handleOrderFunc = NewTasking
        pad.Zone.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad) //cautious animation reset
        self ! akka.actor.Kill //should cause the actor to restart, which will abort any trapped messages

      case _ => ()
    }

  /**
    * Take this order - the "first order" - and immediately begin processing it.
    * All orders accepted in the meantime will be queued and a note about priority will be issued.
    * @param order the order being accepted
    */
  private def NewTasking(order: VehicleSpawnPad.VehicleOrder): Unit = {
    handleOrderFunc = QueuedTasking
    ProcessOrder(Some(order))
  }

  /**
    * While an order is being processed,
    * all orders accepted in the meantime will be queued and a note about priority will be issued.
    * @param order the order being accepted
    */
  private def QueuedTasking(order: VehicleSpawnPad.VehicleOrder): Unit = {
    val name = order.player.Name
    if (trackedOrder match {
      case Some(tracked) =>
        !tracked.driver.Name.equals(name)
      case None =>
        handleOrderFunc = NewTasking
        NewTasking(order)
        false
    }) {
      orders.indexWhere { _.player.Name.equals(name) } match {
        case -1 if orders.isEmpty =>
          //first queued order
          orders = List(order)
          queueManagementTask()
          pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
            name,
            VehicleSpawnPad.Reminders.Queue,
            Some(s"@SVCP_PositionInQueue^2~^2~")
          )
        case -1 =>
          //new order
          orders = orders :+ order
          val size = orders.size + 1
          pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
            name,
            VehicleSpawnPad.Reminders.Queue,
            Some(s"@SVCP_PositionInQueue^$size~^$size~")
          )
        case n if orders(n).vehicle.Definition ne order.vehicle.Definition =>
          //replace existing order with new order
          val zone = pad.Zone
          val originalOrder = orders(n)
          val originalVehicle = originalOrder.vehicle.Definition.Name
          orders = (orders.take(n) :+ order) ++ orders.drop(n+1)
          VehicleSpawnControl.DisposeVehicle(originalOrder.vehicle, zone)
          zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
            name,
            VehicleSpawnPad.Reminders.Queue,
            Some(s"@SVCP_ReplacedVehicleWithVehicle^@$originalVehicle~^@${order.vehicle.Definition.Name}~")
          )
        case _ =>
          //order is the duplicate of an existing order; do nothing to the queue
          CancelOrder(order, None)
      }
    }
  }

  /**
    * Select the next available queued order and begin processing it.
    */
  private def SelectOrder(): Unit = ProcessOrder(SelectFirstOrder())

  /**
    * Select the next-available queued order if there is no current order being fulfilled.
    * If the queue has been exhausted, set functionality to prepare to accept the next order as a "first order."
    * @return the next-available order
    */
  private def SelectFirstOrder(): Option[VehicleSpawnPad.VehicleOrder] = {
    trackedOrder match {
      case None =>
        val (completeOrder, remainingOrders): (Option[VehicleSpawnPad.VehicleOrder], List[VehicleSpawnPad.VehicleOrder]) =
          orderCredentialsCheck(orders) match {
            case x :: Nil =>
              queueManagement.cancel()
              (Some(x), Nil)
            case x :: b =>
              (Some(x), b)
            case Nil =>
              handleOrderFunc = NewTasking
              queueManagement.cancel()
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
  private def ProcessOrder(order: Option[VehicleSpawnPad.VehicleOrder]): Unit = {
    periodicReminder.cancel()
    order.collect {
      case VehicleSpawnPad.VehicleOrder(driver, vehicle, terminal) =>
        val size = orders.size + 1
        val name = driver.Name
        val newOrder = VehicleSpawnControl.Order(driver, vehicle)
        recursiveOrderReminder(orders.iterator, size)
        trace(s"processing next order - a ${vehicle.Definition.Name} for $name")
        pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
          name,
          VehicleSpawnPad.Reminders.Queue,
          Some(s"@SVCP_PositionInQueue^1~^$size~")
        )
        trackedOrder = Some(newOrder) //guard on
        context.system.scheduler.scheduleOnce(2000 milliseconds, concealPlayer, newOrder)
        driver.LogActivity(TerminalUsedActivity(AmenitySource(terminal), TransactionType.Buy))
    }
  }

  /**
    * One-stop shop to test queued vehicle spawn pad orders for valid credentials and
    * either start a periodic examination of those credentials until the queue has been emptied or
    * cancel a running periodic examination if the queue is already empty.
    */
  private def queueManagementTask(): Unit = {
    if (orders.nonEmpty) {
      orders = orderCredentialsCheck(orders).toList
      if (queueManagement.isCancelled) {
        queueManagement = context.system.scheduler.scheduleWithFixedDelay(
          1.second,
          1.second,
          self,
          VehicleSpawnControl.ProcessControl.QueueManagement
        )
      }
    } else {
      queueManagement.cancel()
    }
  }

  /**
    * For all orders, ensure that that order's details match acceptable specifications
    * and partition all orders that should be cancelled for one reason or another.
    * Generate informative error messages for the failing orders, cancel those partitioned orders,
    * and only return all orders that are still valid.
    * @param recipients the original list of orders
    * @return the list of still-acceptable orders
    */
  private def orderCredentialsCheck(recipients: Iterable[VehicleSpawnPad.VehicleOrder]): Iterable[VehicleSpawnPad.VehicleOrder] = {
    recipients
      .map { order =>
        (order, VehicleSpawnControl.validateOrderCredentials(order.terminal, order.player, order.vehicle))
      }
      .foldRight(List.empty[VehicleSpawnPad.VehicleOrder]) {
        case (f, list) =>
          f match {
            case (order, msg @ Some(_)) =>
              CancelOrder(order, msg)
              list
            case (order, None) =>
              list :+ order
          }
      }
  }

  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param entry the order being cancelled
    */
  private def CancelOrder(entry: VehicleSpawnControl.Order, msg: Option[String]): Unit = {
    CancelOrder(entry.vehicle, entry.driver, msg)
  }
  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param entry the order being cancelled
    */
  private def CancelOrder(entry: VehicleSpawnPad.VehicleOrder, msg: Option[String]): Unit = {
    CancelOrder(entry.vehicle, entry.player, msg)
  }
  /**
    * Cancel this vehicle order and inform the person who made it, if possible.
    * @param vehicle the vehicle from the order being cancelled
    * @param player the player who would driver the vehicle from the order being cancelled
    */
  private def CancelOrder(vehicle: Vehicle, player: Player, msg: Option[String]): Unit = {
    if (vehicle.Seats.values.count(_.isOccupied) == 0) {
      VehicleSpawnControl.DisposeSpawnedVehicle(vehicle, player, pad.Zone)
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(player.Name, VehicleSpawnPad.Reminders.Cancelled, msg)
    }
  }

  /**
   * When the vehicle is spawned and added to the pad, it will "occupy" the pad and block it from further action.
   * During this time, a periodic message about the spawn pad being blocked will be broadcast to the order queue.<br>
   * The vehicle is also queued to deconstruct in 30s if no one assumes the driver seat.
   */
  private def evaluateBlockedReminder(): Unit = {
    /*
    Normally, the player who wanted to spawn the vehicle will be automatically put into the driver mount.
    If this is blocked or aborted, the vehicle will idle on the pad and must be moved far enough away from the point of origin.
    */
    trackedOrder
      .collect {
        case entry =>
          if (reminderSeq.isEmpty) {
            //begin reminder
            trace(s"the pad has become blocked by a ${entry.vehicle.Definition.Name} in its current order")
            retimePeriodicReminder(
              shaveOffFirstElementAndDiffSecondElement(pad.Definition.BlockedReminderMessageDelays)
            )
          } else if (reminderSeq.size == 1) {
            //end reminder
            standaloneBlockedReminder(
              VehicleSpawnPad.VehicleOrder(entry.driver, entry.vehicle, null),
              Some("@PadDeconstruct_Done")
            )
            periodicReminder.cancel()
            periodicReminder = Default.Cancellable
            reminderSeq = List()
          } else {
            //continue reminder
            BlockedReminder(entry, orders)
            retimePeriodicReminder(
              shaveOffFirstElementAndDiffSecondElement(reminderSeq)
            )
          }
          trackedOrder
      }
      .orElse {
        periodicReminder.cancel()
        periodicReminder = Default.Cancellable
        reminderSeq = List()
        None
      }
  }

  /**
   * na
   * @param blockedOrder the previous order whose vehicle is blocking the spawn pad from operating
   * @param recipients all of the other customers who will be receiving the message
   */
  private def BlockedReminder(
                               blockedOrder: VehicleSpawnControl.Order,
                               recipients: Seq[VehicleSpawnPad.VehicleOrder]
                             ): Unit = {
    //everyone else
    recursiveBlockedReminder(
      recipients.iterator,
      if (blockedOrder.vehicle.Health == 0)
        Option("The vehicle spawn pad where you placed your order is blocked.  Clearing the wreckage ...")
      else
        Option("The vehicle spawn pad where you placed your order is blocked.")
    )
    //would-be driver
    blockedOrder.vehicle
      .Seats(0).occupant
      .orElse(pad.Zone.GUID(blockedOrder.vehicle.OwnerGuid))
      .orElse(pad.Zone.GUID(blockedOrder.DriverGUID)) collect {
      case p: Player if p.isAlive =>
        standaloneBlockedReminder(
          VehicleSpawnPad.VehicleOrder(blockedOrder.driver, blockedOrder.vehicle, null),
          Some(s"@PadDeconstruct_secsA^${reminderSeq.head}~")
        )
    }
  }

  /**
   * Clip the first entry in a list of numbers and
   * get the difference between the clipped entry and the next entry.
   * The clipped-off list will be made to be the new sequence of reminder delays.
   * @param sequence reminder delay test values
   * @return difference between first delay and second delay
   */
  private def shaveOffFirstElementAndDiffSecondElement(sequence: Seq[Int]): Int = {
    val startTime = sequence.take(1).headOption.getOrElse(0)
    val restTimes = sequence.drop(1)
    val headOfRestTimes = restTimes.headOption.getOrElse(startTime)
    reminderSeq = restTimes
    startTime - headOfRestTimes
  }

  /**
   * Set a single instance of the "periodic reminder" to this kind of delay.
   * @param delay how long until the next reminder
   */
  private def retimePeriodicReminder(delay: Int): Unit = {
    periodicReminder = context.system.scheduler.scheduleOnce(
      delay.seconds,
      self,
      VehicleSpawnControl.ProcessControl.Reminder
    )
  }

  @tailrec private final def recursiveBlockedReminder(
      iter: Iterator[VehicleSpawnPad.VehicleOrder],
      cause: Option[Any]
  ): Unit = {
    if (iter.hasNext) {
      standaloneBlockedReminder(iter.next(), cause)
      recursiveBlockedReminder(iter, cause)
    }
  }

  private def standaloneBlockedReminder(
                                         entry: VehicleSpawnPad.VehicleOrder,
                                         cause: Option[Any]
                                       ): Unit = {
    pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
      entry.player.Name,
      VehicleSpawnPad.Reminders.Blocked,
      cause
    )
  }

  @tailrec private final def recursiveOrderReminder(
      iter: Iterator[VehicleSpawnPad.VehicleOrder],
      size: Int,
      position: Int = 2
  ): Unit = {
    if (iter.hasNext) {
      val recipient = iter.next()
      pad.Zone.VehicleEvents ! VehicleSpawnPad.PeriodicReminder(
        recipient.player.Name,
        VehicleSpawnPad.Reminders.Queue,
        Some(s"@SVCP_PositionInQueue^$position~^$size~")
      )
      recursiveOrderReminder(iter, size, position + 1)
    }
  }
}

object VehicleSpawnControl {
  /**
    * Control messages for the vehicle spawn process.
    */
  sealed trait ProcessControlOperation
  object ProcessControl {
    case object Flush extends ProcessControlOperation
    case object OrderCancelled extends ProcessControlOperation
    case object GetNewOrder extends ProcessControlOperation
    case object Reminder extends ProcessControlOperation
    case object QueueManagement extends ProcessControlOperation
  }

  /**
    * An entry that stores a vehicle spawn pad spawning task (called an "order").
    * @param driver the player who wants the vehicle
    * @param vehicle the vehicle
    */
  final case class Order(driver: Player, vehicle: Vehicle) {
    assert(driver.HasGUID, s"when ordering a vehicle, the prospective driver ${driver.Name} does not have a GUID")
    assert(vehicle.HasGUID, s"when ordering a vehicle, the ${vehicle.Definition.Name} does not have a GUID")
    val DriverGUID: PlanetSideGUID = driver.GUID
    val time: Long = System.currentTimeMillis()
  }

  /**
    * Assess the applicable details of an order that is being processed (is usually enqueued)
    * and determine whether it is is still valid based on the current situation of those details.
    * @param inZoneThing some physical aspect of this system through which the order will be processed;
    *                    either the vehicle spawn pad or the vehicle spawn terminal are useful;
    *                    this entity and the player are subject to a distance check
    * @param player the player who would be the driver of the vehicle filed in the order
    * @param vehicle the vehicle filed in the order
    * @param tooFarDistance the distance check;
    *                       defaults to 1225 (35m squared) relative to the anticipation of a `Terminal` entity
    * @return whether or not a cancellation message is associated with these entry details,
    *         explaining why the order should be cancelled
    */
  def validateOrderCredentials(
                                inZoneThing: PlanetSideGameObject with WorldEntity with ZoneAware,
                                player: Player,
                                vehicle: Vehicle,
                                tooFarDistance: Float = 1225
                              ): Option[String] = {
    if (!player.HasGUID || player.Zone != inZoneThing.Zone || !vehicle.HasGUID || vehicle.Destroyed) {
      Some("@SVCP_RemovedFromVehicleQueue_Generic")
    } else if (!player.isAlive || player.isReleased) {
      Some("@SVCP_RemovedFromVehicleQueue_Destroyed")
    } else if (vehicle.PassengerInSeat(player).isEmpty) {
      //once seated, these are not a concern anymore
      if (inZoneThing.Destroyed) {
        Some("@SVCP_RemovedFromQueue_TerminalDestroyed")
      } else if (Vector3.DistanceSquared(inZoneThing.Position, player.Position) > tooFarDistance) {
        Some("@SVCP_RemovedFromVehicleQueue_MovedTooFar")
      } else if (player.VehicleSeated.nonEmpty) {
        Some("@SVCP_RemovedFromVehicleQueue_ParentChanged")
      } else if (!vehicle.Seats(0).definition.restriction.test(player)) {
        Some("@SVCP_RemovedFromVehicleQueue_ArmorChanged")
      } else if (player.Carrying.contains(SpecialCarry.CaptureFlag)) {
        Some("@SVCP_RemovedFromVehicleQueue_CaptureFlag")
      } else if (player.Carrying.contains(SpecialCarry.VanuModule)) {
        Some("@SVCP_RemovedFromVehicleQueue_VanuModule")
      } else if (player.Carrying.contains(SpecialCarry.MonolithUnit)) {
        Some("@SVCP_RemovedFromVehicleQueue_MonolithUnit")
      } else if ( player.ZoningRequest == Zoning.Method.Quit) {
        Some("@SVCP_RemovedFromVehicleQueue_Quit")
      } else if ( player.ZoningRequest == Zoning.Method.InstantAction) {
        Some("@SVCP_RemovedFromVehicleQueue_InstantAction")
      } else if ( player.ZoningRequest == Zoning.Method.Recall) {
        Some("@SVCP_RemovedFromVehicleQueue_Recall")
      } else if ( player.ZoningRequest == Zoning.Method.OutfitRecall) {
        Some("@SVCP_RemovedFromVehicleQueue_OutfitRecall")
      } else {
        None
      }
    } else {
      None
    }
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * Call this downstream of "`ConcealPlayer`".
    * @param vehicle the vehicle being disposed
    * @param player the player who would own the vehicle being disposed
    * @param zone the zone in which the vehicle is registered (should be located)
    */
  private def DisposeSpawnedVehicle(vehicle: Vehicle, player: Player, zone: Zone): Unit = {
    DisposeVehicle(vehicle, zone)
    zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(player.GUID)
  }

  /**
    * Properly clean up a vehicle that has been registered and spawned into the game world.
    * @param vehicle the vehicle being disposed
    * @param zone the zone in which the vehicle is registered (should be located)
    */
  def DisposeVehicle(vehicle: Vehicle, zone: Zone): Unit = {
    if (zone.Vehicles.contains(vehicle)) { //already added to zone
      vehicle.Actor ! Vehicle.Deconstruct(Some(0.seconds))
    } else { //just registered to zone
      TaskWorkflow.execute(GUIDTask.unregisterVehicle(zone.GUID, vehicle))
    }
  }
}
