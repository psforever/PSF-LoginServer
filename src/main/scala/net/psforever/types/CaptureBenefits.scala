// Copyright (c) 2022 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed trait CaptureBenefit extends IntEnumEntry {
  def value: Int
}

sealed abstract class LatticeBenefit(val value: Int) extends CaptureBenefit

sealed abstract class CavernBenefit(val value: Int) extends CaptureBenefit

object LatticeBenefit extends IntEnum[LatticeBenefit] {
  def values = findValues

  case object None extends LatticeBenefit(value = 0)

  case object AmpStation extends LatticeBenefit(value = 1)

  case object DropshipCenter extends LatticeBenefit(value = 2)

  case object BioLaboratory extends LatticeBenefit(value = 4)

  case object InterlinkFacility extends LatticeBenefit(value = 8)

  case object TechnologyPlant extends LatticeBenefit(value = 16)
}

object CavernBenefit extends IntEnum[CavernBenefit] {
  def values = findValues

  case object None extends CavernBenefit(value = 0)

  case object SpeedModule extends CavernBenefit(value = 4)

  case object ShieldModule extends CavernBenefit(value = 8)

  case object VehicleModule extends CavernBenefit(value = 16)

  case object EquipmentModule extends CavernBenefit(value = 32)

  case object HealthModule extends CavernBenefit(value = 64)

  case object PainModule extends CavernBenefit(value = 128)
}
