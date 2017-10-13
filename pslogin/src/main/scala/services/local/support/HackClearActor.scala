// Copyright (c) 2017 PSForever
package services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * Restore original functionality to an object that has been hacked after a certain amount of time has passed.
  * This `Actor` is intended to sit on top of the event system that handles broadcast messaging regarding hacking events.
  * @see `LocalService`
  */
class HackClearActor() extends Actor {
  /** The periodic `Executor` that checks for server objects to be unhacked */
  private var clearTrigger : Cancellable = HackClearActor.DefaultClearer
  /** A `List` of currently hacked server objects */
  private var hackedObjects : List[HackClearActor.HackEntry] = Nil
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case HackClearActor.ObjectIsHacked(target, zone, unk1, unk2, time) =>
      hackedObjects = hackedObjects :+ HackClearActor.HackEntry(target, zone, unk1, unk2, time)
      if(hackedObjects.size == 1) { //we were the only entry so the event must be started from scratch
        import scala.concurrent.ExecutionContext.Implicits.global
        clearTrigger = context.system.scheduler.scheduleOnce(HackClearActor.timeout, self, HackClearActor.TryClearHacks())
      }

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

      if(stillHackedObjects.nonEmpty) {
        val short_timeout : FiniteDuration = math.max(1, HackClearActor.timeout_time - (now - stillHackedObjects.head.time)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackClearActor.TryClearHacks())
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
      if(now - entry.time >= HackClearActor.timeout_time) {
        recursivePartitionEntries(iter, now, index + 1)
      }
      else {
        index
      }
    }
  }
}

object HackClearActor {
  /** The wait before a server object is to unhack; as a Long for calculation simplicity */
  private final val timeout_time : Long = 60000000000L //nanoseconds (60s)
  /** The wait before a server object is to unhack; as a `FiniteDuration` for `Executor` simplicity */
  private final val timeout : FiniteDuration = timeout_time nanoseconds

  private final val DefaultClearer : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  /**
    * Message that carries information about a server object that has been hacked.
    * @param target the server object
    * @param zone the zone in which the object resides
    * @param time when the object was hacked
    * @see `HackEntry`
    */
  final case class ObjectIsHacked(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, time : Long = System.nanoTime())
  /**
    * Message that carries information about a server object that needs its functionality restored.
    * Prompting, as compared to `ObjectIsHacked` which is reactionary.
    * @param door_guid the server object
    * @param zone_id the zone in which the object resides
    */
  final case class ClearTheHack(door_guid : PlanetSideGUID, zone_id : String, unk1 : Long, unk2 : Long)
  /**
    * Internal message used to signal a test of the queued door information.
    */
  private final case class TryClearHacks()

  /**
    * Entry of hacked server object information.
    * The `zone` is maintained separately to ensure that any message resulting in an attempt to close doors is targetted.
    * @param target the server object
    * @param zone the zone in which the object resides
    * @param time when the object was hacked
    * @see `ObjectIsHacked`
    */
  private final case class HackEntry(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, time : Long)
}
