// Copyright (c) 2026 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

/**
  * An `Enumeration` of the mobility states of vehicles.<br>
  * <br>
  * In general, two important mobility states exist - `Mobile` and `Deployed`.
  * There are stages of a formal deployment.
  * For any deployment state other than the defined ones, the vehicle assumes it is in one of the transitional states.
  * If the target vehicle has no deployment behavior, a non-`Mobile` value will not affect it.
  */
sealed abstract class DriveState(val value: Int) extends IntEnumEntry {
  /**
   * This is how classic value enumeration works.
   * @see `scala.Enumeration`
   * @return value this state was created with
   */
  def id: Int = value
}

object DriveState extends IntEnum[DriveState] {
  def apply(number: Int): DriveState = {
    values.find(_.value == number).getOrElse {
      throw new NoSuchElementException(s"DriveState does not define a $number state")
    }
  }

  def values: IndexedSeq[DriveState] = findValues

  /* standard mobility to deployment spectrum */
  case object Mobile extends DriveState(value = 0)
  case object Undeploying extends DriveState(value = 1)
  case object Deploying extends DriveState(value = 2)
  case object Deployed extends DriveState(value = 3)
  /** unknown; not encountered on a vehicle that can deploy; functions like Mobile */
  case object State7 extends DriveState(value = 7)
  /** falling(?) droppod state */
  case object Droppod extends DriveState(value = 30)
  /** unknown but used */
  case object State127 extends DriveState(value = 127)
  /** undocked orbital shuttle state */
  case object OrbitalShuttleDocked extends DriveState(value = 129)

  /* defined to keep decoding tool working */
  case object UNK4 extends DriveState(value = 4)
  case object UNK5 extends DriveState(value = 5)
  case object UNK6 extends DriveState(value = 6)

  /* values denied encoding */
  /** flag bfr kneeling state */
  case object Kneeling extends DriveState(value = -1) //flag bfr kneeling state
  /** when emerging from spawn pad, or being kicked from a ferry, during server guidance */
  case object AutoPilot extends DriveState(value = -2)
}
