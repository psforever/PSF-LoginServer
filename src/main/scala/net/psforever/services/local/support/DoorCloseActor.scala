// Copyright (c) 2017 PSForever
package net.psforever.services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{Default, Doors}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.zones.Zone
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.PlanetSideGUID

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * Close an opened door after a certain amount of time has passed.
  * This `Actor` is intended to sit on top of the event system that handles broadcast messaging regarding doors opening.
  * @see `LocalService`
  */
class DoorCloseActor() extends Actor {
  /** The periodic `Executor` that checks for doors to be closed */
  private var doorCloserTrigger: Cancellable = Default.Cancellable

  /** A `List` of currently open doors */
  private var openDoors: List[DoorCloseActor.DoorEntry] = Nil
  //private[this] val log = org.log4s.getLogger

  def receive: Receive = {
    case DoorCloseActor.DoorIsOpen(door, zone, time) =>
      openDoors = openDoors :+ DoorCloseActor.DoorEntry(door, zone, time)
      if (openDoors.size == 1) { //we were the only entry so the event must be started from scratch
        import scala.concurrent.ExecutionContext.Implicits.global
        doorCloserTrigger =
          context.system.scheduler.scheduleOnce(DoorCloseActor.timeout, self, DoorCloseActor.TryCloseDoors())
      }

    case DoorCloseActor.TryCloseDoors() =>
      doorCloserTrigger.cancel()
      val now: Long                       = System.currentTimeMillis()
      val (doorsToClose1, doorsLeftOpen1) = PartitionEntries(openDoors, now)
      val (doorsToClose2, doorsLeftOpen2) = doorsToClose1.partition { entry =>
        Doors.testForTargetHoldingDoorOpen(entry.door, entry.door.Definition.continuousOpenDistance).isEmpty
      }
      openDoors = (
        doorsLeftOpen1 ++
          doorsLeftOpen2.map(entry => DoorCloseActor.DoorEntry(entry.door, entry.zone, now))
      ).sortBy(_.time)
      doorsToClose2.foreach { case DoorCloseActor.DoorEntry(door, zone, _) =>
        door.Open = None //permissible break from synchronization
        zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorCloses(door.GUID)) //call up to the main event system
      }

      if (openDoors.nonEmpty) {
        val short_timeout: FiniteDuration = math.max(1, DoorCloseActor.timeout_time - (now - openDoors.head.time)).milliseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        doorCloserTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, DoorCloseActor.TryCloseDoors())
      }

    case _ => ()
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
    * @param now the time right now (in milliseconds)
    * @see `List.partition`
    * @return a `Tuple` of two `Lists`, whose qualifications are explained above
    */
  private def PartitionEntries(
      list: List[DoorCloseActor.DoorEntry],
      now: Long
  ): (List[DoorCloseActor.DoorEntry], List[DoorCloseActor.DoorEntry]) = {
    val n: Int = recursivePartitionEntries(list.iterator, now)
    (list.take(n), list.drop(n)) //take and drop so to always return new lists
  }

  /**
    * Mark the index where the `List` of elements can be divided into two:
    * a `List` of elements that have exceeded the time limit,
    * and a `List` of elements that still satisfy the time limit.
    * @param iter the `Iterator` of entries to divide
    * @param now the time right now (in milliseconds)
    * @param index a persistent record of the index where list division should occur;
    *              defaults to 0
    * @return the index where division will occur
    */
  @tailrec private def recursivePartitionEntries(
      iter: Iterator[DoorCloseActor.DoorEntry],
      now: Long,
      index: Int = 0
  ): Int = {
    if (!iter.hasNext) {
      index
    } else {
      val entry = iter.next()
      if (now - entry.time >= DoorCloseActor.timeout_time) {
        recursivePartitionEntries(iter, now, index + 1)
      } else {
        index
      }
    }
  }
}

object DoorCloseActor {

  /** The wait before an open door closes; as a Long for calculation simplicity */
  private final val timeout_time: Long = 5000L //milliseconds (5s)
  /** The wait before an open door closes; as a `FiniteDuration` for `Executor` simplicity */
  private final val timeout: FiniteDuration = timeout_time milliseconds

  /**
    * Message that carries information about a door that has been opened.
    * @param door the door object
    * @param zone the zone in which the door resides
    * @param time when the door was opened
    * @see `DoorEntry`
    */
  final case class DoorIsOpen(door: Door, zone: Zone, time: Long = System.currentTimeMillis())

  /**
    * Message that carries information about a door that needs to close.
    * Prompting, as compared to `DoorIsOpen` which is reactionary.
    * @param door_guid the door
    * @param zone_id the zone in which the door resides
    */
  final case class CloseTheDoor(door_guid: PlanetSideGUID, zone_id: String)

  /**
    * Internal message used to signal a test of the queued door information.
    */
  private final case class TryCloseDoors()

  /**
    * Entry of door information.
    * The `zone` is maintained separately to ensure that any message resulting in an attempt to close doors is targetted.
    * @param door the door object
    * @param zone the zone in which the door resides
    * @param time when the door was opened
    * @see `DoorIsOpen`
    */
  private final case class DoorEntry(door: Door, zone: Zone, time: Long)
}
