// Copyright (c) 2021 PSForever
package net.psforever.services.hart

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.Default
import net.psforever.objects.zones.Zone
import net.psforever.services.base.{EventResponse, GenericEventBus, GenericEventBusMsg}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
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
  /** since the system is zone-locked, caching this value is fine */
  val zoneId: String = zone.id
  /** all of the paired HART facility amenities and the shuttle housed in that facility (in that order) */
  var padAndShuttlePairs: List[(PlanetSideGUID, PlanetSideGUID)] = List()

  /* the HART system is controlled by a sequence of events;
   * the sequence describes key state changes and animation cues
   * to produce the effect of the orbital shuttle being used
   */
  var sequence = Seq.empty[HartEvent]
  /** index keeping track of the current event in the sequence */
  var sequenceIndex: Int = 0
  /** how many events are a part of this sequence */
  var sequenceLength = 0
  /** when the timing of the events in the system changes,
    * do not push the changes until completion of the current routine
    */
  var delayedScheduleChange: Option[Seq[HartEvent]] = None
  /** the time at the start of the previous event */
  var lastStartTime: Long = 0
  /** scheduler for each event in the sequence */
  var timer: Cancellable = Default.Cancellable

  /** a message bus to which all associated orbital shuttle pads are subscribed */
  val padEvents = new GenericEventBus[HartTimer.Command]
  /** cache common messages */
  val shuttleDockedInThisZone: HartTimer.ShuttleDocked = HartTimer.ShuttleDocked(zoneId)
  val shuttleFreeFromDockInThisZone: HartTimer.ShuttleFreeFromDock = HartTimer.ShuttleFreeFromDock(zoneId)

  /** the behaviors common to both the inert and active operations of the hart */
  val commonBehavior: Receive = {
    case HartTimer.SetEventDurations(_, awayDuration: Long, boardingDuration: Long) =>
      val newSequence = HartEvent.buildEventSequence(awayDuration, boardingDuration)
      if (newSequence.nonEmpty) {
        if (timer.isCancelled) {
          sequence = newSequence
          sequenceLength = newSequence.length
          nextEvent(sequenceIndex)
        } else {
          delayedScheduleChange = Some(newSequence)
        }
        context.become(flightsScheduled)
      }
  }

  /** behaviors that are valid while no sequence of events is defined; the hart is inert */
  def grounded: Receive = commonBehavior
    .orElse {
      case HartTimer.PairWith(_, pad, shuttle, from) =>
        pairWith(pad, shuttle, from)

      case _ => ;
    }

  /** behaviors that are valid after a sequence of events is defined; the hart is active */
  def flightsScheduled: Receive = commonBehavior
    .orElse {
      case HartTimer.PairWith(_, pad, shuttle, from) =>
        pairWith(pad, shuttle, from)
        val event = sequence(sequenceIndex)
        if (event.lockedDoors) {
          from ! HartTimer.LockDoors
        }
        if (event.docked.contains(true)) {
          from ! HartTimer.ShuttleDocked(zoneId)
        }

      case HartTimer.NextEvent(next) if next == 0 =>
        sequence = delayedScheduleChange.getOrElse(sequence)
        sequenceLength = sequence.length
        delayedScheduleChange = None
        nextEvent(next)

      case HartTimer.NextEvent(next) =>
        nextEvent(next)

      case HartTimer.Update(_, forChannel) =>
        val seq = sequence
        val event = seq(sequenceIndex)
        val time = Some(System.currentTimeMillis() - lastStartTime)
        if (event.docked.contains(true)) {
          padEvents.publish( HartTimer.ShuttleDocked(forChannel) )
        }
        event.prerequisiteUpdate match {
          case Some(fields) =>
            val times = event.timeFields(time)
            zone.LocalEvents ! LocalServiceMessage(
              forChannel,
              LocalAction.ShuttleEvent(HartTimer.OrbitalShuttleEvent(
                fields.u1, fields.u2, times.t1, times.t2, times.t3, padAndShuttlePairs zip Seq(20, 20, 20)
              ))
            )
          case None => ;
        }
        zone.LocalEvents ! LocalServiceMessage(
          forChannel,
          LocalAction.ShuttleEvent(
            HartTimer.analyzeEvent(event, padAndShuttlePairs, time)
          )
        )
        event.shuttleState match {
          case Some(state) =>
            padEvents.publish( HartTimer.ShuttleStateUpdate(forChannel, state.id) )
          case None =>
            //find previous valid shuttle state
            var i = sequenceIndex - 1
            while(seq(i).shuttleState.isEmpty) { i = if (i - 1 < 0) sequenceLength - 1 else i - 1 }
            padEvents.publish( HartTimer.ShuttleStateUpdate(forChannel, seq(i).shuttleState.get.id) )
        }

      case _ => ;
    }

  def receive: Receive = grounded

  def pairWith(pad: PlanetSideGUID, shuttle: PlanetSideGUID, from: ActorRef): Unit = {
    padEvents.subscribe(from, to = "")
    padAndShuttlePairs = (padAndShuttlePairs :+ (pad, shuttle)).distinct
  }

  def nextEvent(next: Int): Unit = {
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
        zone.LocalEvents ! LocalServiceMessage(zoneId, LocalAction.ShuttleEvent(evt))
        padEvents.publish( shuttleDockedInThisZone )
      case Some(false) if currEvent.docked.contains(true) =>
        padEvents.publish( shuttleFreeFromDockInThisZone )
        context.system.scheduler.scheduleOnce(
          delay = 10 milliseconds,
          zone.LocalEvents,
          LocalServiceMessage(zoneId, LocalAction.ShuttleEvent(evt))
        )
      case _ =>
        zone.LocalEvents ! LocalServiceMessage(zoneId, LocalAction.ShuttleEvent(evt))
    }
    if (currEvent.lockedDoors != event.lockedDoors) {
      padEvents.publish( if(event.lockedDoors) HartTimer.LockDoors else HartTimer.UnlockDoors )
    }
    event.shuttleState match {
      case Some(state) =>
        padEvents.publish( HartTimer.ShuttleStateUpdate(zoneId, state.id) )
      case None => ;
    }
  }
}

object HartTimer {
  /**
    * Transform `HartEvent` data into `OrbitalShuttleEvent` data.
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
      case _: Boarding          => Seq(20, 20, 20)
      case _: ShuttleTakeoffOps => Seq(20, 20, 20)
      case _: Takeoff           => Seq( 6, 25,  5)
      case _: InTransit         => Seq(20, 20, 20)
      case Arrival              => Seq( 5,  5, 27)
      case ShuttleDockingOps    => Seq(20, 20, 20)
      case Blanking             => Seq(20, 20, 20)
      case _                    => Seq(20, 20, 20)
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

  trait MessageToHartInZone {
    def inZone: String
  }

  /**
    * Personalized messages that align the state of the shuttle to one's perspective (client).
    * @param inZone the zone for which the update will be composed
    * @param forChannel to whom to address the reply
    */
  final case class Update(inZone: String, forChannel: String) extends MessageToHartInZone

  final case class SetEventDurations(inZone: String, away: Long, boarding: Long) extends MessageToHartInZone
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
  trait Command extends EventResponse with GenericEventBusMsg { def channel: String = "" }
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
  final case class ShuttleStateUpdate(forChannel: String, state: Int) extends Command
  /**
    * The shuttle has landed on the pad and will (soon) accept passengers.
    */
  final case class ShuttleDocked(forChannel: String) extends Command
  /**
    * The shuttle has disengaged from the pad, will no longer accept passengers, and may take off soon.
    */
  final case class ShuttleFreeFromDock(forChannel: String) extends Command
}
