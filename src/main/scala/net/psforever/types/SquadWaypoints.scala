// Copyright (c) 2019 PSForever
package net.psforever.types

import scodec.codecs._

object WaypointSubtype extends Enumeration {
  type Type = Value

  val Squad, Laze = Value
}

sealed trait SquadWaypoint {
  def value: Int
  def subtype: WaypointSubtype.Value
}

sealed abstract class StandardWaypoint(override val value: Int) extends SquadWaypoint {
  def subtype: WaypointSubtype.Value = WaypointSubtype.Squad
}

sealed class LazeWaypoint(override val value: Int) extends SquadWaypoint {
  def subtype: WaypointSubtype.Value = WaypointSubtype.Laze
}

object SquadWaypoint {
  def apply(value: Int): SquadWaypoint = {
    if(value < 5) {
      values(value)
    } else {
      new LazeWaypoint(value)
    }
  }

  def values = Seq(One, Two, Three, Four, ExperienceRally)

  case object One extends StandardWaypoint(value = 0)
  case object Two extends StandardWaypoint(value = 1)
  case object Three extends StandardWaypoint(value = 2)
  case object Four extends StandardWaypoint(value = 3)
  case object ExperienceRally extends StandardWaypoint(value = 4)

  implicit val codec = uint8L.xmap[SquadWaypoint]( n => apply(n), waypoint => waypoint.value )
}
