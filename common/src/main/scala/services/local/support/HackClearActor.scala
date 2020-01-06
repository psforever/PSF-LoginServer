// Copyright (c) 2017 PSForever
package services.local.support

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.DefaultCancellable
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * Restore original functionality to an object that has been hacked after a certain amount of time has passed.
  * This `Actor` is intended to sit on top of the event system that handles broadcast messaging regarding hacking events.
  * @see `LocalService`
  */
class HackClearActor() extends Actor {
  /** The periodic `Executor` that checks for server objects to be unhacked */
  private var clearTrigger : Cancellable = DefaultCancellable.obj
  /** A `List` of currently hacked server objects */
  private var hackedObjects : List[HackClearActor.HackEntry] = Nil
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case HackClearActor.ObjectIsHacked(target, zone, unk1, unk2, duration, time) =>
      val durationNanos = TimeUnit.NANOSECONDS.convert(duration, TimeUnit.SECONDS)
      hackedObjects = hackedObjects :+ HackClearActor.HackEntry(target, zone, unk1, unk2, time, durationNanos)

      // Restart the timer, in case this is the first object in the hacked objects list
      RestartTimer()

    case HackClearActor.TryClearHacks() =>
      clearTrigger.cancel
      val now : Long = System.nanoTime
      //TODO we can just walk across the list of doors and extract only the first few entries
      val (unhackObjects, stillHackedObjects) = PartitionEntries(hackedObjects, now)
      hackedObjects = stillHackedObjects
      unhackObjects.foreach(entry => {
        entry.target.Actor ! CommonMessages.ClearHack()
        context.parent ! HackClearActor.ClearTheHack(entry.target.GUID, entry.zone.Id, entry.unk1, entry.unk2) //call up to the main event system
      })

      RestartTimer()

    case HackClearActor.ObjectIsResecured(target) =>
      val obj = hackedObjects.filter(x => x.target == target).headOption
      obj match {
        case Some(entry: HackClearActor.HackEntry) =>
          hackedObjects = hackedObjects.filterNot(x => x.target == target)
          entry.target.Actor ! CommonMessages.ClearHack()
          context.parent ! HackClearActor.ClearTheHack(entry.target.GUID, entry.zone.Id, entry.unk1, entry.unk2) //call up to the main event system

          // Restart the timer in case the object we just removed was the next one scheduled
          RestartTimer()
        case None => ;
      }

    case _ => ;
  }

  private def RestartTimer(): Unit = {
    if(hackedObjects.length != 0) {
      val now = System.nanoTime()
      val (unhackObjects, stillHackedObjects) = PartitionEntries(hackedObjects, now)

      stillHackedObjects.headOption match {
        case Some(hackEntry) =>
          val short_timeout : FiniteDuration = math.max(1, hackEntry.duration - (now - hackEntry.time)) nanoseconds

          log.info(s"HackClearActor: Still items left in hacked objects list. Checking again in ${short_timeout.toSeconds} seconds")
          import scala.concurrent.ExecutionContext.Implicits.global
          clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackClearActor.TryClearHacks())
        case None => log.info("HackClearActor: No objects left in hacked objects list. Not rescheduling check.")
      }

    }
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
  private def PartitionEntries(list : List[HackClearActor.HackEntry], now : Long) : (List[HackClearActor.HackEntry], List[HackClearActor.HackEntry]) = {
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
  @tailrec private def recursivePartitionEntries(iter : Iterator[HackClearActor.HackEntry], now : Long, index : Int = 0) : Int = {
    if(!iter.hasNext) {
      index
    }
    else {
      val entry = iter.next()
      if(now - entry.time >= entry.duration) {
        recursivePartitionEntries(iter, now, index + 1)
      }
      else {
        index
      }
    }
  }
}

object HackClearActor {
  /**
    * Message that carries information about a server object that has been hacked.
    * @param target the server object
    * @param zone the zone in which the object resides
    * @param time when the object was hacked
    * @param duration how long the object is to stay hacked for in seconds
    * @see `HackEntry`
    */
  final case class ObjectIsHacked(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, duration: Int, time : Long = System.nanoTime())

  /**
    * Message used to request that a hack is cleared from the hacked objects list and the unhacked status returned to all clients
    *
    */
  final case class ObjectIsResecured(target: PlanetSideServerObject with Hackable)

  /**
    * Message that carries information about a server object that needs its functionality restored.
    * Prompting, as compared to `ObjectIsHacked` which is reactionary.
    * @param obj the server object
    * @param zone_id the zone in which the object resides
    */
  final case class ClearTheHack(obj : PlanetSideGUID, zone_id : String, unk1 : Long, unk2 : Long)


  /**
    * Internal message used to signal a test of the queued door information.
    */
  private final case class TryClearHacks()

  /**
    * Entry of hacked server object information.
    * The `zone` is maintained separately to ensure that any message resulting in an attempt to close doors is targeted.
    * @param target the server object
    * @param zone the zone in which the object resides
    * @param time when the object was hacked
    * @param duration The hack duration in nanoseconds
    * @see `ObjectIsHacked`
    */
  private final case class HackEntry(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, time : Long, duration: Long)
}
