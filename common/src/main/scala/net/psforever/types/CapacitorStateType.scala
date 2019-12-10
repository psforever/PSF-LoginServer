package net.psforever.types

object CapacitorStateType extends Enumeration {
  type Type = Value

  val Idle        = Value(0)
  val Charging    = Value(1)
  val ChargeDelay = Value(2)
  val Discharging = Value(3)
}
