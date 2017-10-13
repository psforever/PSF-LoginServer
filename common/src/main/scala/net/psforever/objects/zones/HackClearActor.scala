// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec
import scala.concurrent.duration._

class HackClearActor() extends Actor {
  import HackClearActor._
  private var clearTrigger : Cancellable = DefaultClearer
  private var hackedObjects : List[HackEntry] = Nil
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case ObjectIsHacked(door, zone, unk1, unk2, time) =>
      hackedObjects = hackedObjects :+ HackEntry(door, zone, unk1, unk2, time)
      if(hackedObjects.size == 1) {
        import scala.concurrent.ExecutionContext.Implicits.global
        clearTrigger = context.system.scheduler.scheduleOnce(timeout, self, HackClearActor.TryClearHacks())
      }

    case TryClearHacks() =>
      clearTrigger.cancel
      val now : Long = System.nanoTime
      //TODO we can just walk across the list of doors and extract only the first few entries
      val (unhackObjects, stillHackedObjects) = recursivePartitionHacks(hackedObjects.iterator, now)
      hackedObjects = stillHackedObjects
      unhackObjects.foreach(entry => {
        entry.target.Actor ! CommonMessages.ClearHack()
        context.parent ! HackClearActor.ClearTheHack(entry.target.GUID, entry.zone.Id, entry.unk1, entry.unk2)
      })

      if(stillHackedObjects.nonEmpty) {
        val short_timeout : FiniteDuration = math.max(1, timeout_time - (now - stillHackedObjects.head.hacked_at_time)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackClearActor.TryClearHacks())
      }

    case _ => ;
  }

  /**
    * na
    * @param iter na
    * @param now na
    * @param list na
    * @see `List.partition`
    * @return a `Tuple` of two `Lists`:
    *         the entries for all objects that are no longer hacked,
    *         and the entries for all objects that are still hacked
    */
  @tailrec private def recursivePartitionHacks(iter : Iterator[HackEntry], now : Long, list : List[HackEntry] = Nil) : (List[HackEntry], List[HackEntry]) = {
    if(!iter.hasNext) {
      (list, iter.toList)
    }
    else {
      val entry = iter.next()
      if(now - entry.hacked_at_time >= timeout_time) {
        recursivePartitionHacks(iter, now, list :+ entry)
      }
      else {
        (list, entry +: iter.toList)
      }
    }
  }
}

object HackClearActor {
  private final val timeout_time : Long = 60000000000L //nanoseconds (1 minute)
  private final val timeout : FiniteDuration = timeout_time nanoseconds

  private final val DefaultClearer : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  final case class ObjectIsHacked(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, hacked_at_time : Long = System.nanoTime())

  final case class ClearTheHack(door_guid : PlanetSideGUID, zone_id : String, unk1 : Long, unk2 : Long)

  private final case class HackEntry(target : PlanetSideServerObject, zone : Zone, unk1 : Long, unk2 : Long, hacked_at_time : Long)

  private final case class TryClearHacks()
}
