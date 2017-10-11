// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec
import scala.concurrent.duration._

class DoorCloseActor() extends Actor {
  import DoorCloseActor._
  private var doorCloserTrigger : Cancellable = DefaultCloser
  private var openDoors : List[DoorEntry] = Nil
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case DoorIsOpen(door, zone, time) =>
      openDoors = openDoors :+ DoorEntry(door, zone, time)
      if(openDoors.size == 1) {
        import scala.concurrent.ExecutionContext.Implicits.global
        doorCloserTrigger = context.system.scheduler.scheduleOnce(timeout, self, DoorCloseActor.TryCloseDoors())
      }

    case TryCloseDoors() =>
      doorCloserTrigger.cancel
      val now : Long = System.nanoTime
      //TODO we can just walk across the list of doors and extract only the first few entries
      val (doorsToClose, doorsLeftOpen) = recursivePartitionDoors(openDoors.iterator, now)
      openDoors = doorsLeftOpen
      doorsToClose.foreach(entry => {
        entry.door.Open = false //permissible
        context.parent ! DoorCloseActor.CloseTheDoor(entry.door.GUID, entry.zone.Id)
      })

      if(doorsLeftOpen.nonEmpty) {
        val short_timeout : FiniteDuration = math.max(1, timeout_time - (now - doorsLeftOpen.head.opened_at_time)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        doorCloserTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, DoorCloseActor.TryCloseDoors())
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
    *         the entries for all `Door`s that are closing,
    *         and the entries for all doors that are staying open
    */
  @tailrec private def recursivePartitionDoors(iter : Iterator[DoorEntry], now : Long, list : List[DoorEntry] = Nil) : (List[DoorEntry], List[DoorEntry]) = {
    if(!iter.hasNext) {
      (list, iter.toList)
    }
    else {
      val entry = iter.next()
      if(now - entry.opened_at_time >= timeout_time) {
        recursivePartitionDoors(iter, now, list :+ entry)
      }
      else {
        (list, entry +: iter.toList)
      }
    }
  }
}

object DoorCloseActor {
  private final val timeout_time : Long = 5000000000L //nanoseconds
  private final val timeout : FiniteDuration = timeout_time nanoseconds

  private final val DefaultCloser : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  final case class DoorIsOpen(door : Door, zone : Zone, opened_at_time : Long = System.nanoTime())

  final case class CloseTheDoor(door_guid : PlanetSideGUID, zone_id : String)

  private final case class DoorEntry(door : Door, zone : Zone, opened_at_time : Long)

  private final case class TryCloseDoors()
}
