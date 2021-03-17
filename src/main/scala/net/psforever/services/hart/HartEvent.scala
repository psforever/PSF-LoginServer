// Copyright (c) 2021 PSForever
package net.psforever.services.hart

import net.psforever.types.HartSequence

/**
  * The various `flying` states assigned to the orbital shuttle
  * in close to an order in which they are assigned.
  */
object ShuttleState extends Enumeration {
  type Type = Value

  val State13 = Value(13)
  val State14 = Value(14)
  val State10 = Value(10)
  val State11 = Value(11)
  val State12 = Value(12)
  val State15 = Value(15)
}

/**
  * Produce the specific animation sequence and the ???.
  * @see `OrbitalShuttleEvent`
  * @see `OrbitalShuttleTimeMsg`
  * @see `HartEvent`
  * @param u1 the animation code for the HART
  * @param u2 ???
  */
final case class HartEventStateFields(u1: HartSequence, u2: Int)

/**
  * Produce the time data of this event in the sequence.
  * @see `OrbitalShuttleEvent`
  * @see `OrbitalShuttleTimeMsg`
  * @see `HartEvent`
  * @param t1 in general, time for the shuttle to arrive
  * @param t2 in general, `8000L`;
  *           when being useful, time for the shuttle to board passengers
  * @param t3 in general, time elasped
  */
final case class HartEventTimeFields(t1: Long, t2: Long, t3: Long)

/**
  * An event in the sequence of the high-altitude rapid transport (HART) system
  * encompassing both ground facility conditions and conditions of the orbital shuttle.
  */
sealed trait HartEvent {
  /** HART facility and shuttle animation */
  def u1: HartSequence
  /** counter? */
  def u2: Int
  /** starting time on the clock; typically seen on the display */
  def timeOnClock: Long
  /** for how long this event goes on */
  def duration: Long
  /** are the managed doors for the HART facility locked closed;
    * this is an active state field: `true` - locked right now and `false` - unlocked right now
    */
  def lockedDoors: Boolean = true
  /** the shuttle has a unique state to expose to the zone;
    * the state is related to a value in the `Flying` field of a `VehicleStateMessage` packet
    */
  def shuttleState: Option[ShuttleState.Value]
  /** how the shuttle and the HART facility interact;
    * this is an active state field:
    * `Some(true)` - the shuttle is docked right now;
    * `Some(false)` - the shuttle has freed itself from the facility's dock right now;
    * `None` - the shuttle is acting freely apart from its facility */
  def docked: Option[Boolean]
  /** these fields must be including prior to an update if the shuttle state was not previous known;
    * the primary purpose is to place the shuttle platform at the correct elevation
    */
  def prerequisiteUpdate: Option[HartEventStateFields]

  /**
    * Get the animation state fields for this event.
    * @param time during update requests, the amount of time that has elapsed during the start of this event
    * @return the animation state data
    */
  def stateFields(time: Option[Long] = None): HartEventStateFields = {
    HartEventStateFields(u1, u2)
  }

  /**
    * Get the primary time fields for this event.
    * @param time during update requests, the amount of time that has elapsed during the start of this event
    * @return the time data
    */
  def timeFields(time: Option[Long] = None): HartEventTimeFields = {
    HartEventTimeFields(
      time match {
        case Some(t) if timeOnClock > t  => timeOnClock - t
        case Some(t) if timeOnClock <= t => 0L
        case _                           => timeOnClock
      },
      8000L,
      time match {
        case Some(t) => t
        case _       => 0
      }
    )
  }
}

object HartEvent {
  private val prepareForDepartureOnUpdate: Option[HartEventStateFields] =
    Some(HartEventStateFields(HartSequence.PrepareForDeparture, 1))

  final case class Boarding(duration: Long) extends HartEvent {
    def u1: HartSequence = HartSequence.State0
    def u2: Int = 0
    def timeOnClock: Long = duration
    override def lockedDoors: Boolean = false
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State10)
    def docked: Option[Boolean] = Some(true)
    def prerequisiteUpdate: Option[HartEventStateFields] = None

    override def timeFields(time: Option[Long]): HartEventTimeFields = {
      /*
      the full progress bar only displays 60s
      for other times, the progress bar will only display the portion necessary to represent the time in respect to 60s
      */
      HartEventTimeFields(
        0L,
        super.timeFields(time).t1,
        time match {
          case None    => 0L
          case Some(_) => timeOnClock
        }
      )
    }
  }

  final case class ShuttleTakeoffOps(timeOnClock: Long) extends HartEvent {
    def u1: HartSequence = HartSequence.PrepareForDeparture
    def u2: Int = 1
    def duration: Long = 8000
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State11)
    def docked: Option[Boolean] = Some(true)
    def prerequisiteUpdate: Option[HartEventStateFields] = None
  }

  object ShuttleTakeoffOps {
    final val duration: Long = 8000L
  }

  final case class Takeoff(timeOnClock: Long) extends HartEvent {
    def u1: HartSequence = HartSequence.TakeOff
    def u2: Int = 2
    def duration: Long = Takeoff.duration
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State12)
    def docked: Option[Boolean] = Some(false)
    def prerequisiteUpdate: Option[HartEventStateFields] = prepareForDepartureOnUpdate
  }

  object Takeoff {
    final val duration: Long = 13300L
  }

  final case class InTransit(
                              timeOnClock: Long,
                              duration: Long,
                              boardingDuration: Long
                            ) extends HartEvent {
    def u1: HartSequence = HartSequence.State7
    def u2: Int = 3
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State15)
    def docked: Option[Boolean] = None
    def prerequisiteUpdate: Option[HartEventStateFields] = prepareForDepartureOnUpdate

    override def stateFields(time: Option[Long] = None): HartEventStateFields = {
      HartEventStateFields(
        time match {
          case Some(_) => HartSequence.State5
          case _       => u1
        },
        u2
      )
    }

    override def timeFields(time: Option[Long]): HartEventTimeFields = {
      HartEventTimeFields(
        time match {
          case Some(t) if timeOnClock > t  => timeOnClock - t
          case Some(t) if timeOnClock <= t => 0L
          case _                           => timeOnClock
        },
        8000L,
        time match {
          case Some(_) => boardingDuration
          case _       => 0
        }
      )
    }
  }

  case object Arrival extends HartEvent {
    def u1: HartSequence = HartSequence.Land
    def u2: Int = 4
    def timeOnClock: Long = 23700
    def duration: Long = 15700
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State13)
    def docked: Option[Boolean] = None
    def prerequisiteUpdate: Option[HartEventStateFields] = prepareForDepartureOnUpdate
  }

  case object ShuttleDockingOps extends HartEvent {
    def u1: HartSequence = HartSequence.PrepareForBoarding
    def u2: Int = 5
    def timeOnClock: Long = 8000
    def duration: Long = 8000
    def shuttleState: Option[ShuttleState.Value] = Some(ShuttleState.State14)
    def docked: Option[Boolean] = Some(true)
    def prerequisiteUpdate: Option[HartEventStateFields] = None
  }

  case object Blanking extends HartEvent {
    def u1: HartSequence = HartSequence.State0
    def u2: Int = 5
    def timeOnClock: Long = 4294967295L
    def duration: Long = 1 //for how long?
    def shuttleState: Option[ShuttleState.Value] = None
    def docked: Option[Boolean] = Some(true)
    def prerequisiteUpdate: Option[HartEventStateFields] = None

    override def timeFields(time: Option[Long]): HartEventTimeFields =
      HartEventTimeFields(timeOnClock, 8000L, 0L)
  }

  /**
    * The high alititude rapid transport (HART) system is centered around a series of animations
    * of a component orbital shuttle landing and taking off from a given facility.
    * The two important times are the length pof the time the shuttle is away from the facility and
    * the length of time that the shuttle is docked at the facility to allow for passenger boarding.
    * The sequence progresses through stages from the shuttle being landed, to the shuttle departing,
    * to the shuttle returning, and then starting back with the shuttle being landed.
    * <br>
    * As the shuttle animates, the facility also animates.
    * As both the shuttle and the facility animate, various other components connect to the facility and to the shuttle
    * undergo state changes, allowing or denying access to the shuttle's boarding routines.
    * When boarding is permitted, this phase is considered as part of a single event in the sequence,
    * and boarding duration lasts for that entire event.
    * The remainder of the sequence is devoted to a remainder of time from the other duration
    * once the known time of fixed animation events are deducted.
    * @param inFlightDuration for how long the orbital shuttle is away from being docked at the HART building
    *                         and not allowing passengers to board
    * @param boardingDuration for how long the orbital shuttle is landed at its component HART building
    *                         and is allowing passnegers to board
    * @return the final sequence of events
    */
  def buildEventSequence(inFlightDuration: Long, boardingDuration: Long): Seq[HartEvent] = {
    val returnDurations = Arrival.duration + ShuttleDockingOps.duration
    val fixedDurations = ShuttleTakeoffOps.duration + Takeoff.duration + returnDurations
    val full = if (inFlightDuration > fixedDurations) {
      inFlightDuration
    } else {
      inFlightDuration + fixedDurations
    }
    val firstTime = full - ShuttleTakeoffOps.duration
    val secondTime = firstTime - Takeoff.duration
    val awayDuration = secondTime - returnDurations
    Seq(
      Boarding(boardingDuration),
      ShuttleTakeoffOps(full),
      Takeoff(firstTime),
      InTransit(secondTime, awayDuration, boardingDuration),
      Arrival,
      ShuttleDockingOps,
      Blanking
    )
  }
}
