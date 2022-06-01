// Copyright (c) 2022 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

/**
  * Perks gained through certain empire acquisitions.
  */
sealed trait CaptureBenefit extends IntEnumEntry {
  def value: Int
}

/**
  * Perks that carry between faction affiliated facilities connected across the continental lattice.
  */
sealed abstract class LatticeBenefit(val value: Int) extends CaptureBenefit

/**
  * Perks that carry between faction affiliated facilities connected across the continental lattice
  * where one of those facilities is connected to a geo warp gate
  * that is connected to an faction affiliated cavern.
  */
sealed abstract class CavernBenefit(val value: Int) extends CaptureBenefit

object LatticeBenefit extends IntEnum[LatticeBenefit] {
  def values = findValues

  /** no perk */
  case object None extends LatticeBenefit(value = 0)
  /** perk attached to an amp_station */
  case object AmpStation extends LatticeBenefit(value = 1)
  /** perk attached to a comm_station_dsp */
  case object DropshipCenter extends LatticeBenefit(value = 2)
  /** perk attached to a cryo_facility */
  case object BioLaboratory extends LatticeBenefit(value = 4)
  /** perk attached to a comm_station */
  case object InterlinkFacility extends LatticeBenefit(value = 8)
  /** perk attached to a tech_plant */
  case object TechnologyPlant extends LatticeBenefit(value = 16)
}

object CavernBenefit extends IntEnum[CavernBenefit] {
  def values = findValues.filterNot(_ eq NamelessBenefit)

  /** no perk */
  case object None extends CavernBenefit(value = 0)
  /** similar to no perk; but can be used for positive statusing */
  case object NamelessBenefit extends CavernBenefit(value = 2)
  /** perk attached to a cavern or cavern module */
  case object SpeedModule extends CavernBenefit(value = 4)
  /** perk attached to a cavern or cavern module */
  case object ShieldModule extends CavernBenefit(value = 8)
  /** perk attached to a cavern or cavern module */
  case object VehicleModule extends CavernBenefit(value = 16)
  /** perk attached to a cavern or cavern module */
  case object EquipmentModule extends CavernBenefit(value = 32)
  /** perk attached to a cavern or cavern module */
  case object HealthModule extends CavernBenefit(value = 64)
  /** perk attached to a cavern or cavern module */
  case object PainModule extends CavernBenefit(value = 128)
}
