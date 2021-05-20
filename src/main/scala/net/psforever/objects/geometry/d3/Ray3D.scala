// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a geometric coordinate position and a specific direction from that position.
  * Rays are like mathematical lines in that they have infinite length;
  * but, that infinite length is only expressed in a single direction,
  * rather than proceeding in both a direction and its opposite direction from a target point.
  * Infinity just be like that.
  * Additionally, the point is not merely any point on the ray used to assist defining it
  * and is instead considered the clearly-defined origin of the ray.
  * @param p the point of origin
  * @param d the direction
  */
final case class Ray3D(p: Point, d: Vector3)
  extends Geometry3D
  with geometry.Line {
  def center: Point = p

  def moveCenter(point: geometry.Point): Geometry3D = Ray3D(Point(point), d)
}

object Ray3D {
  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Ray3D` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Ray3D = Ray3D(Point(x,y,z), d)

  /**
    * An overloaded constructor that uses a `Vector3` entity to express coordinates.
    * @param v the coordinates of the position
    * @param d the direction
    * @return a `Ray3D` entity
    */
  def apply(v: Vector3, d: Vector3): Ray3D = Ray3D(Point(v.x, v.y, v.z), d)
}
