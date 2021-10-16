// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{IntEnum, IntEnumEntry}

abstract class VehicleSubsystem(val value: Int) extends IntEnumEntry

object VehicleSubsystem extends IntEnum[VehicleSubsystem] {
  val values = findValues

  case object Controls extends VehicleSubsystem(value = 0)

  case object Ejection extends VehicleSubsystem(value = 50) //GOAM50

  case object MosquitoRadar extends VehicleSubsystem(value = 2)

  case object BattleframeTrunk extends VehicleSubsystem(value = 3)

  case object BattleframeSensorArray extends VehicleSubsystem(value = 4)

  case object BattleframeShieldGenerator extends VehicleSubsystem(value = 5)

  case object BattleframeWeaponry extends VehicleSubsystem(value = 6)

  case object BattleframeFlightPod extends VehicleSubsystem(value = 7)

  case object BattleframeMovementServo extends VehicleSubsystem(value = 8)
}
