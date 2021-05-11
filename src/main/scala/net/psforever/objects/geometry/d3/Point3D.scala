// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a geometric coordinate position.
  * @see `Vector3`
  * @param x the 'x' coordinate of the position
  * @param y the 'y' coordinate of the position
  * @param z the 'z' coordinate of the position
  */
final case class Point3D(x: Float, y: Float, z: Float)
  extends Geometry3D
  with geometry.Point {
  def center: Point3D = this

  def asVector3: Vector3 = Vector3(x, y, z)
}

object Point3D {
  /**
    * An overloaded constructor that assigns world origin coordinates.
    * @return a `Point3D` entity
    */
  def apply(): Point3D = Point3D(0,0,0)

  /**
    * An overloaded constructor that uses the same coordinates from a `Vector3` entity.
    * @param v the entity with the corresponding points
    * @return a `Point3D` entity
    */
  def apply(v: Vector3): Point3D = Point3D(v.x, v.y, v.z)
}
