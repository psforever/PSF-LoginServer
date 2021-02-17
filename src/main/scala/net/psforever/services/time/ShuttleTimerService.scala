// Copyright (c) 2021 PSForever
package net.psforever.services.time

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import net.psforever.objects.zones.Zone
import net.psforever.services.time.ShuttleTimer.OrbitalShuttleEvent
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

import scala.collection.concurrent.TrieMap

class ShuttleTimerService extends Actor {
  val channels: TrieMap[String, ActorRef] = TrieMap[String, ActorRef]()

  def receive: Receive = {
    case out : ShuttleTimer.PairWith =>
      val zone = out.zone
      val channel = zone.id
      (channels.get(channel) match {
        case Some(o) =>
          o
        case None =>
          val actor = context.actorOf(Props(classOf[ShuttleTimer], zone), s"$channel-shuttle-timer")
          channels.put(channel, actor)
          actor
      }) ! out

    case out @ ShuttleTimer.Update(inZone, _) =>
      channels.get(inZone) match {
        case Some(o) => o ! out
        case _ =>
      }

    case _ => ;
  }
}

class ShuttleTimer(zone: Zone) extends Actor {
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  var padAndShuttlePairs: List[(PlanetSideGUID, PlanetSideGUID)] = List()
  var currTime: Long = System.currentTimeMillis()
  var timer: Cancellable = context.system.scheduler.scheduleOnce(0 milliseconds, self, ShuttleTimer.NextEvent(1))
  var index: Int = 0

  def receive: Receive = {
    case ShuttleTimer.PairWith(_, pad, shuttle) =>
      padAndShuttlePairs = (padAndShuttlePairs :+ (pad, shuttle)).distinct

    case ShuttleTimer.NextEvent(next) =>
      index = next
      val event = ShuttleTimer.eventTimeDeltas(next)
      currTime = System.currentTimeMillis()
      timer = context.system.scheduler.scheduleOnce(event.d milliseconds, self, ShuttleTimer.NextEvent((next + 1) % 7))
      zone.VehicleEvents ! VehicleServiceMessage(zone.id, VehicleAction.OrbitalShuttleTimerEvent(analyzeEvent(event)))

    case ShuttleTimer.Update(_, forChannel) =>
      val event = ShuttleTimer.eventTimeDeltas(index)
      zone.VehicleEvents ! VehicleServiceMessage(
        forChannel,
        VehicleAction.OrbitalShuttleTimerEvent(analyzeEvent(event, Some(event.t - (System.currentTimeMillis() - currTime))))
      )

    case _ => ;
  }

  def analyzeEvent(event: ShuttleTimer.TimedShuttleEvent, time: Option[Long] = None): OrbitalShuttleEvent = {
    import net.psforever.services.time.ShuttleTimer._
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
  final val fullTime: Long = 225000L //ms

  trait TimedShuttleEvent {
    def d: Long //for how long this event goes on
    def t: Long //starting time on the clock
  }
  case object Boarding extends TimedShuttleEvent {
    def d: Long = 60000
    def t: Long = 60000
  }
  case object Takeoff extends TimedShuttleEvent {
    def t: Long = fullTime //225000ms
    def d: Long = 8000
  }
  case object Event1 extends TimedShuttleEvent {
    def t: Long = 217000
    def d: Long = 13300
  } //217000ms
  case object Event2 extends TimedShuttleEvent {
    def t: Long = 203700
    def d: Long = 180000
  }
  case object Event3 extends TimedShuttleEvent {
    def t: Long = 23700
    def d: Long = 15700
  }
  case object Event4 extends TimedShuttleEvent {
    def t: Long = 8000
    def d: Long = 8000
  }
  case object Blanking extends TimedShuttleEvent {
    def t: Long = Int.MaxValue.toLong
    def d: Long = 500 //for how long?
  }

  final val eventTimeDeltas: Seq[TimedShuttleEvent] = Seq(
    Boarding,
    Takeoff,
    Event1,
    Event2,
    Event3,
    Event4,
    Blanking
  )

  private case class NextEvent(index: Int)

  final case class Update(inZone: String, forChannel: String)

  final case class PairWith(zone: Zone, pad: PlanetSideGUID, shuttle: PlanetSideGUID)

  final case class OrbitalShuttleEvent(u1: Int, u2: Int, t1: Long, t2: Long, pairs: List[((PlanetSideGUID, PlanetSideGUID), Int)])

  trait Command
}
