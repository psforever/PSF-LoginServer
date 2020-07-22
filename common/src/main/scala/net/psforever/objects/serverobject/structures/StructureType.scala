// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import enumeratum.{EnumEntry, Enum}

sealed trait StructureType extends EnumEntry

object StructureType extends Enum[StructureType] {
  val values = findValues

  case object Bridge   extends StructureType // technically, a "bridge section"
  case object Building extends StructureType // generic
  case object Bunker   extends StructureType // low accessible ground cover
  case object Facility extends StructureType // large base
  case object Platform
      extends StructureType // outdoor amenities disconnected from a proper base like the vehicle spawn pads in sanctuary
  case object Tower    extends StructureType // also called field towers: watchtower, air tower, gun tower
  case object WarpGate extends StructureType // transport point between zones
}
