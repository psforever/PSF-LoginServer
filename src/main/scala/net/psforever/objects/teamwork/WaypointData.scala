// Copyright (c) 2022 PSForever
package net.psforever.objects.teamwork

import net.psforever.types.Vector3

/**
  * Information necessary to display a specific map marker.
  */
class WaypointData() {
  var zone_number: Int = 1
  var pos: Vector3     = Vector3.z(1) //a waypoint with a non-zero z-coordinate will flag as not getting drawn
}

object WaypointData{
  def apply(zone_number: Int, pos: Vector3): WaypointData = {
    val data = new WaypointData()
    data.zone_number = zone_number
    data.pos = pos
    data
  }
}
