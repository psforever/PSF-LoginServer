package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class HartSequence(val value: Int) extends IntEnumEntry

object HartSequence extends IntEnum[HartSequence] {
  val values = findValues

  /** no effect, but is used when the shuttle is docked */
  case object State0 extends HartSequence(value = 0)
  /** gantries retract, lights on, bay doors open, platform up */
  case object PrepareForDeparture extends HartSequence(value = 1)
  /** shuttle takes off, bay doors close, lights off */
  case object TakeOff extends HartSequence(value = 2)
  /** lights on, bay doors open, shuttle lands */
  case object Land extends HartSequence(value = 3)
  /** platform down, bay doors closed, gantries extend, lights off */
  case object PrepareForBoarding extends HartSequence(value = 4)
  /** no effect, but is used when the shuttle is away;
    * a substitute for 7 occasionally or used as its supplement, e.g., 2-2, 5-3, 3-4 OR 2-2, 7-3, 5-3, 3-4
    */
  case object State5 extends HartSequence(value = 5)
  /** no effect, but is used when the shuttle is away */
  case object State7 extends HartSequence(value = 7)
}
