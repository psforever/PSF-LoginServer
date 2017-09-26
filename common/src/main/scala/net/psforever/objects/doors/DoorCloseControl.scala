// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.packet.game.PlanetSideGUID

import scala.collection.mutable.ListBuffer

class DoorCloseControl(implicit val environment : ActorRef) extends Actor {
  import DoorCloseControl._
  private var doorCloser : Cancellable = DefaultCloser
  private var openDoors : List[DoorEntry] = Nil

  def receive : Receive = {
    case DoorIsOpen(guid, time) =>
      if(openDoors.isEmpty) {
       //doorCloser = context.system.scheduler.scheduleOnce(timeout, environment, Door.DoorMessage())
      }
      else {
        openDoors = openDoors :+ DoorEntry(guid, time)
      }

    case _ => ;
  }
}

object DoorCloseControl {
  private final val timeout : Long = 5000L

  private final val DefaultCloser : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  private final case class DoorEntry(door_guid : PlanetSideGUID, opened_at_time : Long)

  final case class DoorIsOpen(door_guid : PlanetSideGUID, opened_at_time : Long = System.nanoTime())

  final case class CloseTheDoor(door_guid : PlanetSideGUID)
}
