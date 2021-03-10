// Copyright (c) 2021 PSForever
package net.psforever.services.hart

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.zones.Zone
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.{GenericEventBus, GenericEventBusMsg}
import net.psforever.types.{HartSequence, PlanetSideGUID}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Within each zone, all high-altitude rapid transport (HART) systems are controlled in unison.
  * A HART system is composed of a facility (amenity) that embodies passenger onboarding services
  * and a semi-interactive shuttle that gateways to the orbital droppod system.
  * Provide supervision to these components by managing the over-all HART sequence.
  * @param zone the zone being represented by this particular HART service
  */
class HartTimer(zone: Zone) extends Actor {
  /** all of the paired HART facility amenities and the shuttle housed in that facility (in that order) */
  var padAndShuttlePairs: List[(PlanetSideGUID, PlanetSideGUID)] = List()
  /** the current time at the start of the previous event */
  var lastStartTime: Long = System.currentTimeMillis()
  /** scheduler for each subsequent event in the sequence */
  var timer: Cancellable = context.system.scheduler.scheduleOnce(delay = 0 milliseconds, self, HartTimer.NextEvent(1))
  /** index keeping track of the current event in the sequence*/
  var sequenceIndex: Int = 0
  /* the HART system is controlled by a sequence of events (just called the "sequence" at times);
   * the sequence describes key state changes and animation cues
   * to produce the effect of the orbital shuttle being used
   */
  var sequence = HartEvent.buildEventSequence(
    inFlightDuration = 55000L, //225000L,
    boardingDuration = 10000L //60000L
  )
  /** how many events are a part of this sequence */
  var sequenceLength = sequence.length
  /** a message bus to which all associated orbital shuttle pads are subscribed */
  val events = new GenericEventBus[HartTimer.Command]

  def receive: Receive = {
    case HartTimer.PairWith(_, pad, shuttle, from) =>
      events.subscribe(from, to = "")
      padAndShuttlePairs = (padAndShuttlePairs :+ (pad, shuttle)).distinct
      if (sequence(sequenceIndex).lockedDoors) {
        from ! HartTimer.LockDoors
      }

    case HartTimer.NextEvent(next) =>
      val currEvent = sequence(sequenceIndex)
      val event = sequence(next)
      sequenceIndex = next
      lastStartTime = System.currentTimeMillis()
      timer = context.system.scheduler.scheduleOnce(
        event.duration milliseconds,
        self,
        HartTimer.NextEvent((next + 1) % sequenceLength)
      )
      //updates
      val evt = HartTimer.analyzeEvent(event, padAndShuttlePairs)
      event.docked match {
        case Some(true) if currEvent.docked.isEmpty =>
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(evt))
          events.publish( HartTimer.ShuttleDocked )
        case Some(false) if currEvent.docked.contains(true) =>
          events.publish( HartTimer.ShuttleFreeFromDock )
          context.system.scheduler.scheduleOnce(
            delay = 10 milliseconds,
            zone.LocalEvents,
            LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(evt))
          )
        case _ =>
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleEvent(evt))
      }
      if (currEvent.lockedDoors != event.lockedDoors) {
        events.publish( if(event.lockedDoors) HartTimer.LockDoors else HartTimer.UnlockDoors )
      }
      event.shuttleState match {
        case Some(state) =>
          events.publish( HartTimer.ShuttleStateUpdate(state.id) )
        case None => ;
      }

    case HartTimer.Update(_, forChannel) =>
      val seq = sequence
      val event = seq(sequenceIndex)
      if (event.docked.contains(true)) {
        events.publish( HartTimer.ShuttleDocked )
      }
      zone.LocalEvents ! LocalServiceMessage(
        forChannel,
        LocalAction.ShuttleEvent(
          HartTimer.analyzeEvent(event, padAndShuttlePairs, Some(System.currentTimeMillis() - lastStartTime))
        )
      )
      event.shuttleState match {
        case Some(state) =>
          events.publish( HartTimer.ShuttleStateUpdate(state.id) )
        case None =>
          //find last valid shuttle state
          var i = sequenceIndex - 1
          while(seq(i).shuttleState.isEmpty) { i = if (i - 1 < 0) sequenceLength - 1 else i - 1 }
          events.publish( HartTimer.ShuttleStateUpdate(seq(i).shuttleState.get.id) )
      }

    case _ => ;
  }
}

object HartTimer {
  /**
    * Transform `TimeShuttleEvent` data into `OrbitalShuttleEvent` data.
    * The former is treated as something internal.
    * The latter is treated as something external.
    * @see `OrbitalShuttleEvent`
    * @see `HartEvent`
    * @param event the `TimeShuttleEvent` data
    * @param time how long has the current event in th sequence been occurring
    * @return the `OrbitalShuttleEvent` data
    */
  def analyzeEvent(
                    event: HartEvent,
                    padAndShuttlePairs: List[(PlanetSideGUID, PlanetSideGUID)],
                    time: Option[Long] = None
                  ): OrbitalShuttleEvent = {
    import net.psforever.services.hart.HartEvent._
    val stateFields = event.stateFields(time)
    val timeFields = event.timeFields(time)
    //these control codes are taken from packets samples for VS sanctuary during a specific few sequences
    //while the number varies - from 5 to 37 and an actual maximum of 63 - their purpose seems indeterminate
    val pairs = event match {
      case _: Boarding             => Seq(20, 20, 20)
      case _: RaiseShuttlePlatform => Seq(20, 20, 20)
      case _: Takeoff              => Seq( 6, 25,  5)
      case _: Event2               => Seq(20, 20, 20)
      case Event3                  => Seq( 5,  5, 27)
      case Docking                 => Seq(20, 20, 20)
      case Blanking                => Seq(20, 20, 20)
      case _                       => Seq(20, 20, 20)
    }
    OrbitalShuttleEvent(
      stateFields.u1, stateFields.u2,
      timeFields.t1, timeFields.t2, timeFields.t3,
      padAndShuttlePairs zip pairs
    )
  }

  /**
    * Internal message to advance the sequence event.
    * @param index the position of the next event
    */
  private case class NextEvent(index: Int)
  /**
    * Personalized messages that align the state of the shuttle to one's perspective (client).
    * @param inZone the zone for which the update will be composed
    * @param forChannel to whom to address the reply
    */
  final case class Update(inZone: String, forChannel: String)
  /**
    * Append information about a building amenity and shuttle combination in this zone.
    * @param zone the relevant zone
    * @param pad the orbital shuttle pad (`obbasemesh`)
    * @param shuttle the orbital shuttle
    * @param from the control agency of the pad
    */
  final case class PairWith(zone: Zone, pad: PlanetSideGUID, shuttle: PlanetSideGUID, from: ActorRef)
  /**
    * Data structure for passing information about the event to client-local space.
    * The fields match the `OrbitalShuttleTimeMsg` packet that is created using this data.
    * @see `OrbitalShuttleTimeMsg`
    */
  final case class OrbitalShuttleEvent(
                                        u1: HartSequence,
                                        u2: Int,
                                        t1: Long,
                                        t2: Long,
                                        t3: Long,
                                        pairs: List[((PlanetSideGUID, PlanetSideGUID), Int)]
                                      )

  /**
    * Design for the envelop for the message bus
    * to relay instructions back to the individual facility amenity portions of this HART system.
    * The channel is blank because it does not need special designation.
    */
  trait Command extends GenericEventBusMsg { def channel: String = "" }
  /**
    * Forbid entry through the boartding gantry doors.
    */
  case object LockDoors extends Command
  /**
    * Permit entry through the boartding gantry doors.
    */
  case object UnlockDoors extends Command
  /**
    * The state exists to be turned into, ultimately, a `VehicleStateMessage` packet for the shuttle.
    * This state is to be loaded into the `flying` field.
    * @see `VehicleStateMessage`
    * @param state shuttle state, probably more symbolic of a gvien state than anything else
    */
  final case class ShuttleStateUpdate(state: Int) extends Command
  /**
    * The shuttle has landed on the pad and will (soon) accept passengers.
    */
  case object ShuttleDocked extends Command
  /**
    * The shuttle has disengaged from the pad, will no longer accept passengers, and may take off soon.
    */
  case object ShuttleFreeFromDock extends Command
}
