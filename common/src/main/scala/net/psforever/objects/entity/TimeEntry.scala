// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.Vector3

case class TimeEntry(entry : net.psforever.types.Vector3)(implicit time : Long = org.joda.time.DateTime.now.getMillis)

object TimeEntry {
  val invalid = TimeEntry(Vector3(0f, 0f, 0f))(0L)

  def apply(x : Float, y : Float, z : Float) : TimeEntry =
    TimeEntry(Vector3(x, y, z))
}
