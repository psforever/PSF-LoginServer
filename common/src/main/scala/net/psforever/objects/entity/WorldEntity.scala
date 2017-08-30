// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.Vector3

trait WorldEntity {
  def Position : Vector3

  def Position_=(vec : Vector3) : Vector3

  def Orientation : Vector3

  def Orientation_=(vec : Vector3) : Vector3

  def Velocity : Option[Vector3]

  def Velocity_=(vec : Option[Vector3]) : Option[Vector3]

  def Velocity_=(vec : Vector3) : Option[Vector3] = Velocity = Some(vec)
}

object WorldEntity {
  def toString(obj : WorldEntity) : String = {
    s"pos=${obj.Position}, ori=${obj.Orientation}"
  }
}
