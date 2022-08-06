// Copyright (c) 2019 PSForever
package net.psforever.types

import scodec.codecs._

/**
  * Distinction of purpose of the waypoint.
  */
object WaypointSubtype extends Enumeration {
  type Type = Value

  val Squad, Laze = Value
}

/**
  * Base of all waypoints visible to members of a particular squad.
  */
sealed trait SquadWaypoint {
  /** the index of this kind of waypoint */
  def value: Int
  /** the distinction of this kind of waypoint */
  def subtype: WaypointSubtype.Value
}

/**
  * Permanently-defined waypoints known to all squads, set only by the squad leader, accessible by command rank status.
  */
sealed abstract class StandardWaypoint(override val value: Int) extends SquadWaypoint {
  def subtype: WaypointSubtype.Value = WaypointSubtype.Squad
}

/**
  * General waypoint produced by the Flail targeting laser (laze pointer)
  * that is visible by all squad members for a short duration.
  * Any squad member may place this waypoint.
  * A laze waypoint is yellow,
  * is indicated in the game world, on the proximity map, and on the continental map,
  * and is designated by the number of the squad member that produced it.
  * Only one laze waypoint may be made visible from any one squad member at any given time, overwritten when replaced.
  * When viewed by a squad member seated in a Flail, the waypoint includes an elevation reticule for aiming purposes.
  * YMMV.
  * @see `SquadWaypointEvent`
  * @see `SquadWaypointRequest`
  * @param value the index of the waypoint can be any number five and above
  */
sealed case class LazeWaypoint(value: Int) extends SquadWaypoint {
  def subtype: WaypointSubtype.Value = WaypointSubtype.Laze
}

object SquadWaypoint {
  /**
    * Overloaded constructor
    * that returns either the specific squad waypoint as the index value
    * or a laze waypoint with the same value.
    * @param value the index of this kind of waypoint
    * @return a waypoint object
    */
  def apply(value: Int): SquadWaypoint = {
    if(value < 5) {
      values(value)
    } else {
      LazeWaypoint(value)
    }
  }

  /** the five squad-specific waypoints */
  //does not include the multitude of possible laze waypoints
  def values = Seq(One, Two, Three, Four, ExperienceRally)
  /** the first squad rally */
  case object One extends StandardWaypoint(value = 0)
  /** the second squad rally */
  case object Two extends StandardWaypoint(value = 1)
  /** the third squad rally */
  case object Three extends StandardWaypoint(value = 2)
  /** the fourth squad rally */
  case object Four extends StandardWaypoint(value = 3)
  /** the squad experience bonus rally */
  case object ExperienceRally extends StandardWaypoint(value = 4)

  implicit val codec = uint8L.xmap[SquadWaypoint]( n => apply(n), waypoint => waypoint.value )
}
