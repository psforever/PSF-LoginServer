// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.Seat
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import scripts.GUIDTask
import services.ServiceManager
import services.ServiceManager.Lookup
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * Manage a previously-functioning vehicle as it is being deconstructed.<br>
  * <br>
  * A reference to a vehicle should be passed to this object as soon as it is going to be cleaned-up from the game world.
  * Once accepted, only a few seconds will remain before the vehicle is deleted.
  * To ensure that no players are lost in the deletion, all occupants of the vehicle are kicked out.
  * Furthermore, the vehicle is rendered "dead" and inaccessible right up to the point where it is removed.
  */
class DeconstructionActor extends Actor {
  /** The periodic `Executor` that scraps the next vehicle on the list */
  private var scrappingProcess : Cancellable = DeconstructionActor.DefaultProcess
  /** A `List` of currently doomed vehicles */
  private var vehicles : List[DeconstructionActor.VehicleEntry] = Nil
  /** The manager that helps unregister the vehicle from its current GUID scope */
  private var taskResolver : ActorRef = Actor.noSender
  //private[this] val log = org.log4s.getLogger


  def receive : Receive = {
    /*
    ask for a resolver to deal with the GUID system
    when the TaskResolver is finally delivered, switch over to a behavior that actually deals with submitted vehicles
     */
    case DeconstructionActor.RequestTaskResolver =>
      ServiceManager.serviceManager ! Lookup("taskResolver")

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = {
    case DeconstructionActor.RequestDeleteVehicle(vehicle, zone, time) =>
      vehicles = vehicles :+ DeconstructionActor.VehicleEntry(vehicle, zone, time)
      vehicle.Actor ! Vehicle.PrepareForDeletion
      //kick everyone out
      vehicle.Definition.MountPoints.values.foreach(seat_num => {
        val zone_id : String = zone.Id
        val seat : Seat = vehicle.Seat(seat_num).get
        seat.Occupant match {
          case Some(tplayer) =>
            seat.Occupant = None
            tplayer.VehicleSeated = None
            context.parent ! VehicleServiceMessage(zone_id, VehicleAction.KickPassenger(tplayer.GUID, 4, false))
          case None => ;
        }
      })
      if(vehicles.size == 1) { //we were the only entry so the event must be started from scratch
        import scala.concurrent.ExecutionContext.Implicits.global
        scrappingProcess = context.system.scheduler.scheduleOnce(DeconstructionActor.timeout, self, DeconstructionActor.TryDeleteVehicle())
      }

    case DeconstructionActor.TryDeleteVehicle() =>
      scrappingProcess.cancel
      val now : Long = System.nanoTime
      val (vehiclesToScrap, vehiclesRemain) = PartitionEntries(vehicles, now)
      vehicles = vehiclesRemain
      vehiclesToScrap.foreach(entry => {
        val vehicle = entry.vehicle
        val zone = entry.zone
        entry.zone.Transport ! Zone.DespawnVehicle(vehicle)
        context.parent ! DeconstructionActor.DeleteVehicle(vehicle.GUID, zone.Id) //call up to the main event system
        context.parent ! VehicleServiceMessage.RevokeActorControl(vehicle) //call up to a sibling manager
        taskResolver ! GUIDTask.UnregisterVehicle(vehicle)(zone.GUID)
      })

      if(vehiclesRemain.nonEmpty) {
        val short_timeout : FiniteDuration = math.max(1, DeconstructionActor.timeout_time - (now - vehiclesRemain.head.time)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        scrappingProcess = context.system.scheduler.scheduleOnce(short_timeout, self, DeconstructionActor.TryDeleteVehicle())
      }

    case _ => ;
  }

  /**
    * Iterate over entries in a `List` until an entry that does not exceed the time limit is discovered.
    * Separate the original `List` into two:
    * a `List` of elements that have exceeded the time limit,
    * and a `List` of elements that still satisfy the time limit.
    * As newer entries to the `List` will always resolve later than old ones,
    * and newer entries are always added to the end of the main `List`,
    * processing in order is always correct.
    * @param list the `List` of entries to divide
    * @param now the time right now (in nanoseconds)
    * @see `List.partition`
    * @return a `Tuple` of two `Lists`, whose qualifications are explained above
    */
  private def PartitionEntries(list : List[DeconstructionActor.VehicleEntry], now : Long) : (List[DeconstructionActor.VehicleEntry], List[DeconstructionActor.VehicleEntry]) = {
    val n : Int = recursivePartitionEntries(list.iterator, now)
    (list.take(n), list.drop(n)) //take and drop so to always return new lists
  }

  /**
    * Mark the index where the `List` of elements can be divided into two:
    * a `List` of elements that have exceeded the time limit,
    * and a `List` of elements that still satisfy the time limit.
    * @param iter the `Iterator` of entries to divide
    * @param now the time right now (in nanoseconds)
    * @param index a persistent record of the index where list division should occur;
    *              defaults to 0
    * @return the index where division will occur
    */
  @tailrec private def recursivePartitionEntries(iter : Iterator[DeconstructionActor.VehicleEntry], now : Long, index : Int = 0) : Int = {
    if(!iter.hasNext) {
      index
    }
    else {
      val entry = iter.next()
      if(now - entry.time >= DeconstructionActor.timeout_time) {
        recursivePartitionEntries(iter, now, index + 1)
      }
      else {
        index
      }
    }
  }
}

object DeconstructionActor {
  /** The wait before completely deleting a vehicle; as a Long for calculation simplicity */
  private final val timeout_time : Long = 5000000000L //nanoseconds (5s)
  /** The wait before completely deleting a vehicle; as a `FiniteDuration` for `Executor` simplicity */
  private final val timeout : FiniteDuration = timeout_time nanoseconds

  private final val DefaultProcess : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  final case class RequestTaskResolver()

  /**
    * Message that carries information about a vehicle to be deconstructed.
    * @param vehicle the `Vehicle` object
    * @param zone the `Zone` in which the vehicle resides
    * @param time when the vehicle was doomed
    * @see `VehicleEntry`
    */
  final case class RequestDeleteVehicle(vehicle : Vehicle, zone : Zone, time : Long = System.nanoTime())
  /**
    * Message that carries information about a vehicle to be deconstructed.
    * Prompting, as compared to `RequestDeleteVehicle` which is reactionary.
    * @param vehicle_guid the vehicle
    * @param zone_id the `Zone` in which the vehicle resides
    */
  final case class DeleteVehicle(vehicle_guid : PlanetSideGUID, zone_id : String)
  /**
    * Internal message used to signal a test of the queued vehicle information.
    */
  private final case class TryDeleteVehicle()

  /**
    * Entry of vehicle information.
    * The `zone` is maintained separately as a necessity, required to complete the deletion of the vehicle
    * via unregistering of the vehicle and all related, registered objects.
    * @param vehicle the `Vehicle` object
    * @param zone the `Zone` in which the vehicle resides
    * @param time when the vehicle was doomed
    * @see `RequestDeleteVehicle`
    */
  private final case class VehicleEntry(vehicle : Vehicle, zone : Zone, time : Long)
}
