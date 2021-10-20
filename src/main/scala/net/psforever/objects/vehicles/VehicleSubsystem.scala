// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class VehicleSubsystemEntry(val value: Int) extends IntEnumEntry

object VehicleSubsystemEntry extends IntEnum[VehicleSubsystemEntry] {
  val values = findValues

  case object Controls extends VehicleSubsystemEntry(value = 0)

  case object Ejection extends VehicleSubsystemEntry(value = 50) //GOAM50

  case object MosquitoRadar extends VehicleSubsystemEntry(value = 2)

  case object BattleframeTrunk extends VehicleSubsystemEntry(value = 3)

  case object BattleframeSensorArray extends VehicleSubsystemEntry(value = 4)

  case object BattleframeShieldGenerator extends VehicleSubsystemEntry(value = 5)

  case object BattleframeWeaponry extends VehicleSubsystemEntry(value = 6)

  case object BattleframeFlightPod extends VehicleSubsystemEntry(value = 7)

  case object BattleframeMovementServo extends VehicleSubsystemEntry(value = 8)

  case object BattleframeLeftArm extends VehicleSubsystemEntry(value = 9)

  case object BattleframeRightArm extends VehicleSubsystemEntry(value = 10)
}

class VehicleSubsystem(sys: VehicleSubsystemEntry) {
  var enabled: Boolean = true
}
