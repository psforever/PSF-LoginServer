package net.psforever.objects.serverobject.structures

import enumeratum.{EnumEntry, Enum}

sealed trait StructureType extends EnumEntry

object StructureType extends Enum[StructureType] {
  val values: IndexedSeq[StructureType] = findValues

  // technically, a "bridge section"
  case object Bridge extends StructureType

  // generic
  case object Building extends StructureType

  // low accessible ground cover
  case object Bunker extends StructureType

  // large base
  case object Facility extends StructureType

  // outdoor amenities disconnected from a proper base like the vehicle spawn pads in sanctuary
  case object Platform extends StructureType

  // also called field towers: watchtower, air tower, gun tower
  case object Tower extends StructureType

  // transport point between zones
  case object WarpGate extends StructureType

}
