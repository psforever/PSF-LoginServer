// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.packet.game.PlanetSideGUID
import scala.concurrent.duration._

import scala.annotation.tailrec

class DoorCloseControl(implicit val environment : ActorRef) extends Actor {
  import DoorCloseControl._
  private var doorCloserTrigger : Cancellable = DefaultCloser
  private var openDoors : List[DoorEntry] = Nil

  def receive : Receive = {
    case DoorIsOpen(guid, time) =>
      openDoors = openDoors :+ DoorEntry(guid, time)
      if(doorCloserTrigger.isCancelled) {
        import scala.concurrent.ExecutionContext.Implicits.global
        doorCloserTrigger = context.system.scheduler.scheduleOnce(timeout, self, DoorCloseControl.CloseTheDoor())
      }

    case CloseTheDoor() =>
      doorCloserTrigger.cancel
      val now : Long = System.nanoTime
      recursiveCloseDoors(openDoors.iterator, now) match {
        case entry :: rest =>
          openDoors = entry :: rest
          import scala.concurrent.ExecutionContext.Implicits.global
          doorCloserTrigger = context.system.scheduler.scheduleOnce((now - entry.opened_at_time + timeout_time)*1000 milliseconds, self, DoorCloseControl.CloseTheDoor())
        case Nil =>
          openDoors = Nil
      }

    case _ => ;
  }

  @tailrec private def recursiveCloseDoors(iter : Iterator[DoorEntry], now : Long) : List[DoorEntry] = {
    if(!iter.hasNext) {
      Nil
    }
    else {
      val entry = iter.next
      if(now - entry.opened_at_time < timeout_time) {
        entry +: iter.toList
      }
      else {
        //TODO close this door entry
        recursiveCloseDoors(iter, now)
      }
    }
  }
}

object DoorCloseControl {
  private final val timeout_time = 5000
  private final val timeout : FiniteDuration = timeout_time milliseconds

  private final val DefaultCloser : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  private final case class DoorEntry(door_guid : PlanetSideGUID, opened_at_time : Long)

  final case class DoorIsOpen(door_guid : PlanetSideGUID, opened_at_time : Long = System.nanoTime())

  private final case class CloseTheDoor()
}
