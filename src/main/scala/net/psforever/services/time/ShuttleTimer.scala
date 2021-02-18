// Copyright (c) 2021 PSForever
package net.psforever.services.time

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.zones.Zone
import net.psforever.services.{GenericEventBus, GenericEventBusMsg}
import net.psforever.services.time.ShuttleTimer.OrbitalShuttleEvent
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
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
    case ShuttleTimer.PairWith(_, pad, shuttle) =>
      val from = sender()
      events.subscribe(from, to = "")
      padAndShuttlePairs = (padAndShuttlePairs :+ (pad, shuttle)).distinct
      if (TimedShuttleEvent.eventTimeDeltas(index).lockedDoors) {
        from ! ShuttleTimer.LockDoors
      }

    case ShuttleTimer.NextEvent(next) =>
      val currEvent = TimedShuttleEvent.eventTimeDeltas(index)
      val event = TimedShuttleEvent.eventTimeDeltas(next)
      index = next
      currTime = System.currentTimeMillis()
      timer = context.system.scheduler.scheduleOnce(event.d milliseconds, self, ShuttleTimer.NextEvent((next + 1) % 7))
      zone.VehicleEvents ! VehicleServiceMessage(zone.id, VehicleAction.OrbitalShuttleTimerEvent(analyzeEvent(event)))
      if (currEvent.lockedDoors != event.lockedDoors) {
        events.publish( if(event.lockedDoors) ShuttleTimer.LockDoors else ShuttleTimer.UnlockDoors )
      }

    case ShuttleTimer.Update(_, forChannel) =>
      val event = TimedShuttleEvent.eventTimeDeltas(index)
      zone.VehicleEvents ! VehicleServiceMessage(
        forChannel,
        VehicleAction.OrbitalShuttleTimerEvent(analyzeEvent(event, Some(event.t - (System.currentTimeMillis() - currTime))))
      )

    case _ => ;
  }

  def analyzeEvent(event: TimedShuttleEvent, time: Option[Long] = None): OrbitalShuttleEvent = {
    import net.psforever.services.time.TimedShuttleEvent._
    val (u1, u2, t1, t2, pairs) = event match {
      case Boarding =>
        (0, 0, 0L, time.getOrElse(event.t), Seq(20, 20, 20))
      case Takeoff =>
        (1, 1, time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Event1 =>
        (2, 2, time.getOrElse(event.t), 8000L, Seq(6, 25, 5))
      case Event2 =>
        (7, 3, time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Event3 =>
        (3, 4, time.getOrElse(event.t), 8000L, Seq(5, 5, 27))
      case Event4 =>
        (4, 5, time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
      case Blanking =>
        (0, 5, time.getOrElse(event.t), 8000L, Seq(20, 20, 20))
    }
    OrbitalShuttleEvent(u1, u2, t1, t2, padAndShuttlePairs zip pairs)
  }
}

object ShuttleTimer {
  private case class NextEvent(index: Int)

  final case class Update(inZone: String, forChannel: String)

  final case class PairWith(zone: Zone, pad: PlanetSideGUID, shuttle: PlanetSideGUID)

  final case class OrbitalShuttleEvent(u1: Int, u2: Int, t1: Long, t2: Long, pairs: List[((PlanetSideGUID, PlanetSideGUID), Int)])

  trait Command extends GenericEventBusMsg { def channel: String = "" }

  case object LockDoors extends Command

  case object UnlockDoors extends Command
}
