// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a minute geometric coordinate position in three-dimensional space.
  * The point is allowed to substitute for a sphere of zero radius, hence why it is volumetric
  * (ignoring that a sphere of zero radius has no volume).
  * @see `Vector3`
  * @param x the 'x' coordinate of the position
  * @param y the 'y' coordinate of the position
  * @param z the 'z' coordinate of the position
  */
final case class Point(x: Float, y: Float, z: Float)
  extends VolumetricGeometry
  with geometry.Point {
  def center: Point = this

  def moveCenter(point: geometry.Point): VolumetricGeometry = Point(point)

  def asVector3: Vector3 = Vector3(x, y, z)

  def pointOnOutside(v: Vector3): Point = center
}

object Point {
  /**
    * An overloaded constructor that assigns world origin coordinates.
    * @return a `Point` entity
    */
  def apply(): Point = Point(0,0,0)

  def apply(point: geometry.Point): Point = Point(point.asVector3)

  /**
    * An overloaded constructor that uses the same coordinates from a `Vector3` entity.
    * @param v the entity with the corresponding points
    * @return a `Point` entity
    */
  def apply(v: Vector3): Point = Point(v.x, v.y, v.z)
}
