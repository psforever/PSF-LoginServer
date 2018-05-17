// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import services.vehicle.VehicleServiceMessage

import scala.concurrent.duration._

/**
  * Maintain and curate a list of timed `vehicle` object deconstruction tasks.<br>
  * <br>
  * These tasks are queued or dismissed by player activity but they are executed independent of player activity.
  * A common disconnected cause of deconstruction is neglect for an extended period of time.
  * At that point, the original owner of the vehicle no longer matters.
  * Deconstruction neglect, however, is averted by having someone become seated.
  * A realized deconstruction is entirely based on a fixed interval after an unresolved request has been received.
  * The actual process of deconstructing the vehicle and cleaning up its resources is performed by an external agent.<br>
  * <br>
  * This `Actor` is intended to sit on top of the event system that handles broadcast messaging.
  */
class DelayedDeconstructionActor extends Actor {
  /** The periodic `Executor` that scraps the next vehicle on the list */
  private var monitor : Cancellable = DefaultCancellable.obj
  /** A `List` of currently doomed vehicles */
  private var vehicles : List[DelayedDeconstructionActor.VehicleEntry] = Nil
  private[this] val log = org.log4s.getLogger
  private[this] def trace(msg : String) : Unit = log.trace(msg)


  def receive : Receive = {
    case DelayedDeconstructionActor.ScheduleDeconstruction(vehicle, zone, timeAlive) =>
      trace(s"delayed deconstruction order for $vehicle in $timeAlive")
      val oldHead = vehicles.headOption
      val now : Long = System.nanoTime
      vehicles = (vehicles :+ DelayedDeconstructionActor.VehicleEntry(vehicle, zone, timeAlive * 1000000000L))
        .sortBy(entry => entry.survivalTime - (now - entry.logTime))
      if(vehicles.size == 1 || oldHead != vehicles.headOption) { //we were the only entry so the event must be started from scratch
        RetimePeriodicTest()
      }

    case DelayedDeconstructionActor.UnscheduleDeconstruction(vehicle_guid) =>
      //all tasks for this vehicle are cleared from the queue
      //clear any task that is no longer valid by determination of unregistered GUID
      val before = vehicles.length
      val now : Long = System.nanoTime
      vehicles = vehicles.filter(entry => { entry.vehicle.HasGUID && entry.vehicle.GUID != vehicle_guid })
        .sortBy(entry => entry.survivalTime - (now - entry.logTime))
      trace(s"attempting to clear deconstruction order for vehicle $vehicle_guid, found ${before - vehicles.length}")
      RetimePeriodicTest()

    case DelayedDeconstructionActor.PeriodicTaskCulling =>
      //filter the list of deconstruction tasks for any that are need to be triggered
      monitor.cancel
      val now : Long = System.nanoTime
      val (vehiclesToDecon, vehiclesRemain) = vehicles.partition(entry => { now - entry.logTime >= entry.survivalTime })
      vehicles = vehiclesRemain.sortBy(_.survivalTime)
      trace(s"vehicle culling - ${vehiclesToDecon.length} deconstruction tasks found; ${vehiclesRemain.length} tasks remain")
      vehiclesToDecon.foreach(entry => { context.parent ! VehicleServiceMessage.RequestDeleteVehicle(entry.vehicle, entry.zone) })
      RetimePeriodicTest()

    case _ => ;
  }

  def RetimePeriodicTest() : Unit = {
    monitor.cancel
    vehicles.headOption match {
      case None => ;
      case Some(entry) =>
        val retime = math.max(1, entry.survivalTime - (System.nanoTime - entry.logTime)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        monitor = context.system.scheduler.scheduleOnce(retime, self, DelayedDeconstructionActor.PeriodicTaskCulling)
    }
  }
}

object DelayedDeconstructionActor {
  /**
    * Timer for the repeating executor.
    */
  private final val periodicTest : FiniteDuration = 5000000000L nanoseconds //5s

  /**
    * Queue a future vehicle deconstruction action.
    * @param vehicle the `Vehicle` object
    * @param zone the `Zone` that the vehicle currently occupies
    * @param survivalTime how long until the vehicle will be deconstructed in seconds
    */
  final case class ScheduleDeconstruction(vehicle : Vehicle, zone : Zone, survivalTime : Long)

  /**
    * Dequeue a vehicle from being deconstructed.
    * @param vehicle_guid the vehicle
    */
  final case class UnscheduleDeconstruction(vehicle_guid : PlanetSideGUID)

  /**
    * A message the `Actor` sends to itself.
    * The trigger for the periodic deconstruction task.
    */
  private final case class PeriodicTaskCulling()

  /**
    * An entry that stores vehicle deconstruction tasks.
    * @param vehicle the `Vehicle` object
    * @param zone the `Zone` that the vehicle currently occupies
    * @param survivalTime how long until the vehicle will be deconstructed in nanoseconds
    * @param logTime when this deconstruction request was initially created in nanoseconds;
    *                initialized by default to a "now"
    */
  private final case class VehicleEntry(vehicle : Vehicle, zone : Zone, survivalTime : Long, logTime : Long = System.nanoTime())
}
