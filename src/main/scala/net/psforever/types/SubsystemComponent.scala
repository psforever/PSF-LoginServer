// Copyright (c) 2021 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class SubsystemComponent(val value: Int) extends IntEnumEntry

object SubsystemComponent extends IntEnum[SubsystemComponent] {
  val values = findValues

  case object SiphonTransferEfficiency extends SubsystemComponent(value = 0)
  case object SiphonTransferRateA extends SubsystemComponent(value = 1)

  //DamageToken_BFRDestroyed = 2 (visible using weapon guid)

  case object FlightSystemsRechargeRate extends SubsystemComponent(value = 3)
  case object FlightSystemsUseRate extends SubsystemComponent(value = 4)
  case object FlightSystemsDestroyed extends SubsystemComponent(value = 5)
  case object FlightSystemsHorizontalForce extends SubsystemComponent(value = 6)
  case object FlightSystemsOffline extends SubsystemComponent(value = 7)
  case object FlightSystemsVerticalForce extends SubsystemComponent(value = 8)

  case object MovementServosTransit extends SubsystemComponent(value = 9)
  case object MovementServosBackward extends SubsystemComponent(value = 10)
  case object MovementServosForward extends SubsystemComponent(value = 11)
  case object MovementServosPivotSpeed extends SubsystemComponent(value = 12)
  case object MovementServosStrafeSpeed extends SubsystemComponent(value = 13)

  case object SiphonDrainOnly extends SubsystemComponent(value = 14)
  case object SiphonStorageCapacity extends SubsystemComponent(value = 15)
  case object SiphonTransferRateB extends SubsystemComponent(value = 16)

  case object SensorArrayNoEnemies extends SubsystemComponent(value = 17)
  case object SensorArrayNoEnemyAircraft extends SubsystemComponent(value = 18)
  case object SensorArrayNoEnemyGroundVehicles extends SubsystemComponent(value = 19)
  case object SensorArrayNoEnemyProjectiles extends SubsystemComponent(value = 20)
  case object SensorArrayRange extends SubsystemComponent(value = 21)

  case object ShieldGeneratorDestroyed extends SubsystemComponent(value = 22)
  case object ShieldGeneratorOffline extends SubsystemComponent(value = 23)
  case object ShieldGeneratorRechargeRate extends SubsystemComponent(value = 24)

  case object Trunk extends SubsystemComponent(value = 25)

  case object WeaponSystemsCOFRecovery extends SubsystemComponent(value = 26)
  case object WeaponSystemsCOF extends SubsystemComponent(value = 27)
  case object WeaponSystemsDestroyed extends SubsystemComponent(value = 28)
  case object WeaponSystemsAmmoLoss extends SubsystemComponent(value = 29)
  case object WeaponSystemsOffline extends SubsystemComponent(value = 30)

  case object UnknownProjectileRange extends SubsystemComponent(value = 31)
  case object UnknownSensorRange extends SubsystemComponent(value = 32)
  case object UnknownRechargeInterval extends SubsystemComponent(value = 33)

  case object WeaponSystemsRefireTime extends SubsystemComponent(value = 34)
  case object WeaponSystemsReloadTime extends SubsystemComponent(value = 35)

  case class Unknown(override val value: Int) extends SubsystemComponent(value)
}
