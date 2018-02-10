// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.{DefaultCancellable, Player, Vehicle}
import net.psforever.types.Vector3

import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `VehicleSpawnPad`.<br>
  * <br>
  * A spawn pad receives vehicle orders from an attached `Terminal` object.
  * At the time when the order is received, the player who submitted the order is completely visible
  * and waiting back by the said `Terminal` from where the order was submitted.
  * Assuming no other orders are currently being processed, the repeated self message will retrieve this as the next order.
  * The player character is first made transparent with a `GenericObjectActionMessage` packet.
  * The vehicle model itself is then introduced to the game and three things happen with the following order, more or less:<br>
  * 1. the vehicle is attached to a lifting platform that is designed to introduce the vehicle;<br>
  * 2. the player is seated in the vehicle's driver seat (seat 0) and is thus declared the owner; <br>
  * 3. various properties of the player, the vehicle, and the spawn pad itself are set `PlanetsideAttributesMessage`.<br>
  * When this step is finished, the lifting platform raises the vehicle and the mounted player into the game world.
  * The vehicle detaches and is made to roll off the spawn pad a certain distance before being released to user control.
  * That is what is supposed to happen within a certain measure of timing.<br>
  * <br>
  * The orders that are submitted to the spawn pad must be composed of at least three elements:
  * 1. a player, specifically the one that submitted the order and will be declared the "owner;"
  * 2. a vehicle;
  * 3. a callback location for sending messages.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControl(pad : VehicleSpawnPad) extends Actor with FactionAffinityBehavior.Check {
  /** an executor for progressing a vehicle order through the normal spawning logic */
  private var process : Cancellable = DefaultCancellable.obj
  /** a list of vehicle orders that have been submitted for this spawn pad */
  private var orders : List[VehicleSpawnControl.OrderEntry] = List.empty[VehicleSpawnControl.OrderEntry]
  /** the current vehicle order being acted upon */
  private var trackedOrder : Option[VehicleSpawnControl.OrderEntry] = None
  /** how many times a spawned vehicle (spatially) disrupted the next vehicle from being spawned */
  private var blockingViolations : Int = 0
  private[this] val log = org.log4s.getLogger
  private[this] def trace(msg : String) : Unit = log.trace(msg)

  def FactionObject : FactionAffinity = pad

  def receive : Receive = checkBehavior.orElse {
    case VehicleSpawnPad.VehicleOrder(player, vehicle) =>
      trace(s"order from $player for $vehicle received")
      orders = orders :+ VehicleSpawnControl.OrderEntry(player, vehicle, sender)
      if(trackedOrder.isEmpty && orders.length == 1) {
        self ! VehicleSpawnControl.Process.GetOrder
      }

    case VehicleSpawnControl.Process.GetOrder =>
      process.cancel
      blockingViolations = 0
      val (completeOrder, remainingOrders) : (Option[VehicleSpawnControl.OrderEntry], List[VehicleSpawnControl.OrderEntry]) = orders match {
        case x :: Nil =>
          (Some(x), Nil)
        case x :: b =>
          (Some(x), b)
        case Nil =>
          (None, Nil)
      }
      orders = remainingOrders
      completeOrder match {
        case Some(entry) =>
          trace(s"processing order $entry")
          trackedOrder = completeOrder
          import scala.concurrent.ExecutionContext.Implicits.global
          process = context.system.scheduler.scheduleOnce(VehicleSpawnControl.concealPlayerTimeout, self, VehicleSpawnControl.Process.ConcealPlayer)
        case None => ;
      }

    case VehicleSpawnControl.Process.ConcealPlayer =>
      process.cancel
      trackedOrder match {
        case Some(entry) =>
          if(entry.player.isAlive && entry.vehicle.Actor != ActorRef.noSender && entry.sendTo != ActorRef.noSender && entry.player.VehicleSeated.isEmpty) {
            trace(s"hiding player: ${entry.player}")
            entry.sendTo ! VehicleSpawnPad.ConcealPlayer
            import scala.concurrent.ExecutionContext.Implicits.global
            process = context.system.scheduler.scheduleOnce(VehicleSpawnControl.loadVehicleTimeout, self, VehicleSpawnControl.Process.LoadVehicle)
          }
          else {
            trace("integral component lost; abort order fulfillment")
            //TODO Unregister vehicle ... somehow
            trackedOrder = None
            self ! VehicleSpawnControl.Process.GetOrder
          }
        case None =>
          self ! VehicleSpawnControl.Process.GetOrder
      }

    case VehicleSpawnControl.Process.LoadVehicle =>
      process.cancel
      trackedOrder match {
        case Some(entry) =>
          if(entry.vehicle.Actor != ActorRef.noSender && entry.sendTo != ActorRef.noSender) {
            trace(s"loading vehicle: ${entry.vehicle} defined in order")
            entry.sendTo ! VehicleSpawnPad.LoadVehicle(entry.vehicle, pad)
            import scala.concurrent.ExecutionContext.Implicits.global
            process = context.system.scheduler.scheduleOnce(VehicleSpawnControl.awaitSeatedTimeout, self, VehicleSpawnControl.Process.AwaitSeated)
          }
          else {
            trace("owner or vehicle lost; abort order fulfillment")
            //TODO Unregister vehicle ... somehow
            trackedOrder = None
            self ! VehicleSpawnControl.Process.GetOrder
          }

        case None =>
          self ! VehicleSpawnControl.Process.GetOrder
      }

    case VehicleSpawnControl.Process.AwaitSeated =>
      process.cancel
      trackedOrder match {
        case Some(entry) =>
          if(entry.sendTo != ActorRef.noSender) {
            trace("owner seated in vehicle")
            import scala.concurrent.ExecutionContext.Implicits.global
            process = if(entry.player.VehicleOwned.contains(entry.vehicle.GUID)) {
              entry.sendTo ! VehicleSpawnPad.PlayerSeatedInVehicle(entry.vehicle)
              context.system.scheduler.scheduleOnce(VehicleSpawnControl.awaitClearanceTimeout, self, VehicleSpawnControl.Process.AwaitClearance)
            }
            else {
              context.system.scheduler.scheduleOnce(VehicleSpawnControl.awaitSeatedTimeout, self, VehicleSpawnControl.Process.AwaitSeated)
            }
          }
          else {
            trace("owner lost; abort order fulfillment")
            trackedOrder = None
            self ! VehicleSpawnControl.Process.GetOrder
          }
        case None =>
          self ! VehicleSpawnControl.Process.GetOrder
      }

      //TODO raise spawn pad rails from ground

      //TODO start auto drive away

      //TODO release auto drive away

    case VehicleSpawnControl.Process.AwaitClearance =>
      process.cancel
      trackedOrder match {
        case Some(entry) =>
          if(entry.sendTo == ActorRef.noSender || entry.vehicle.Actor == ActorRef.noSender) {
            trace("integral component lost, but order fulfilled; process next order")
            trackedOrder = None
            self ! VehicleSpawnControl.Process.GetOrder
          }
          else if(Vector3.DistanceSquared(entry.vehicle.Position, pad.Position) > 100.0f) { //10m away from pad
            trace("pad cleared; process next order")
            trackedOrder = None
            entry.sendTo ! VehicleSpawnPad.SpawnPadUnblocked(entry.vehicle.GUID)
            self ! VehicleSpawnControl.Process.GetOrder
          }
          else {
            trace(s"pad blocked by ${entry.vehicle} ...")
            blockingViolations += 1
            entry.sendTo ! VehicleSpawnPad.SpawnPadBlockedWarning(entry.vehicle, blockingViolations)
            import scala.concurrent.ExecutionContext.Implicits.global
            process = context.system.scheduler.scheduleOnce(VehicleSpawnControl.awaitClearanceTimeout, self, VehicleSpawnControl.Process.AwaitClearance)
          }
        case None =>
          self ! VehicleSpawnControl.Process.GetOrder
      }

    case _ => ;
  }
}

object VehicleSpawnControl {
  final val concealPlayerTimeout : FiniteDuration = 2000000000L nanoseconds //2s
  final val loadVehicleTimeout : FiniteDuration = 1000000000L nanoseconds //1s
  final val awaitSeatedTimeout : FiniteDuration = 1000000000L nanoseconds //1s
  final val awaitClearanceTimeout : FiniteDuration = 5000000000L nanoseconds //5s

  /**
    * An `Enumeration` of the stages of a full vehicle spawning process, associated with certain messages passed.
    * Some stages are currently TEMPORARY.
    * @see VehicleSpawnPad
    */
  object Process extends Enumeration {
    val
    GetOrder,
    ConcealPlayer,
    LoadVehicle,
    AwaitSeated,
    AwaitClearance
    = Value
  }

  /**
    * An entry that stores vehicle spawn pad spawning tasks (called "orders").
    * @param player the player
    * @param vehicle the vehicle
    * @param sendTo a callback `Actor` associated with the player (in other words, `WorldSessionActor`)
    */
  private final case class OrderEntry(player : Player, vehicle : Vehicle, sendTo : ActorRef)
}
