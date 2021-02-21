// Copyright (c) 2021 PSForever
package net.psforever.services.time

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.zones.Zone
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.{GenericEventBus, GenericEventBusMsg}
import net.psforever.services.time.ShuttleTimer.OrbitalShuttleEvent
import net.psforever.services.time.TimedShuttleEvent._
import net.psforever.types.PlanetSideGUID

class ShuttleTimer(zone: Zone) extends Actor {
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  var padAndShuttlePairs: List[(PlanetSideGUID, PlanetSideGUID)] = List()
  var currTime: Long = System.currentTimeMillis()
  var timer: Cancellable = context.system.scheduler.scheduleOnce(0 milliseconds, self, ShuttleTimer.NextEvent(1))
  var index: Int = 0

  val events = new GenericEventBus[ShuttleTimer.Command]

  def receive: Receive = {
    case ShuttleTimer.PairWith(_, pad, shuttle, from) =>
      events.subscribe(from, to = "")
      padAndShuttlePairs = (padAndShuttlePairs :+ (pad, shuttle)).distinct
      if (ShuttleTimer.sequence(index).lockedDoors) {
        from ! ShuttleTimer.LockDoors
      }

    case ShuttleTimer.NextEvent(next) =>
      val currEvent = ShuttleTimer.sequence(index)
      val event = ShuttleTimer.sequence(next)
      index = next
      currTime = System.currentTimeMillis()
      timer = context.system.scheduler.scheduleOnce(event.duration milliseconds, self, ShuttleTimer.NextEvent((next + 1) % 7))
      //updates
      event.docked match {
        case Some(true) if currEvent.docked.isEmpty =>
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(analyzeEvent(event)))
          events.publish( ShuttleTimer.ShuttleDocked )
        case Some(false) if currEvent.docked.contains(true) =>
          events.publish( ShuttleTimer.ShuttleFreeFromDock )
          context.system.scheduler.scheduleOnce(
            delay = 10 milliseconds,
            zone.LocalEvents,
            LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(analyzeEvent(event)))
          )
        case _ =>
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(analyzeEvent(event)))
      }
      if (currEvent.lockedDoors != event.lockedDoors) {
        events.publish( if(event.lockedDoors) ShuttleTimer.LockDoors else ShuttleTimer.UnlockDoors )
      }
      event.shuttleState match {
        case Some(state) =>
          events.publish( ShuttleTimer.ShuttleStateUpdate(state) )
        case None => ;
      }

    case ShuttleTimer.Update(_, forChannel) =>
      val event = ShuttleTimer.sequence(index)
      if (event.docked.contains(true)) {
        events.publish( ShuttleTimer.ShuttleDocked )
      }
      zone.LocalEvents ! LocalServiceMessage(
        forChannel,
        LocalAction.ShuttleEvent(analyzeEvent(event, Some(event.t - (System.currentTimeMillis() - currTime))))
      )
      event.shuttleState match {
        case Some(state) =>
          events.publish( ShuttleTimer.ShuttleStateUpdate(state) )
        case None => ;
      }

    case _ => ;
  }

  def analyzeEvent(event: TimedShuttleEvent, time: Option[Long] = None): OrbitalShuttleEvent = {
    import net.psforever.services.time.TimedShuttleEvent._
    val (t1, t2, pairs) = event match {
      case Boarding =>
        (0L, time.getOrElse(event.t), Seq(20, 20, 20))
      case Takeoff =>
        (time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Event1 =>
        (time.getOrElse(event.t), 8000L, Seq(6, 25, 5))
      case Event2 =>
        (time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Event3 =>
        (time.getOrElse(event.t), 8000L, Seq(5, 5, 27))
      case Event4 =>
        (time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Blanking =>
        (time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
    }
    OrbitalShuttleEvent(event.u1, event.u2, t1, t2, padAndShuttlePairs zip pairs)
  }
}

object ShuttleTimer {
  private case class NextEvent(index: Int)

  final case class Update(inZone: String, forChannel: String)

  final case class PairWith(zone: Zone, pad: PlanetSideGUID, shuttle: PlanetSideGUID, from: ActorRef)

  final case class OrbitalShuttleEvent(u1: Int, u2: Int, t1: Long, t2: Long, pairs: List[((PlanetSideGUID, PlanetSideGUID), Int)])

  trait Command extends GenericEventBusMsg { def channel: String = "" }

  case object LockDoors extends Command

  case object UnlockDoors extends Command

  final case class ShuttleStateUpdate(state: Int) extends Command

  case object ShuttleDocked extends Command

  case object ShuttleFreeFromDock extends Command

  final val sequence: Seq[TimedShuttleEvent] = Seq(
    Boarding,
    Takeoff,
    Event1,
    Event2,
    Event3,
    Event4,
    Blanking
  )
}
