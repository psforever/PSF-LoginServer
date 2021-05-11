// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a limited span between two geometric coordinate positions, called "endpoints".
  * Unlike mathematical lines, slope is treated the same as the vector leading from one point to the other
  * and is the length of the segment.
  * @param p1 a point
  * @param p2 another point
  */
final case class Segment3D(p1: Point3D, p2: Point3D)
  extends Geometry3D
  with geometry.Segment {
  /**
    * The center point of a segment is a position that is equally in between both endpoints.
    * @return a point
    */
  def center: Point3D = Point3D((p2.asVector3 + p1.asVector3) * 0.5f)

  def d: Vector3 = p2.asVector3 - p1.asVector3

  def asLine: Line3D = Line3D(p1, Vector3.Unit(d))
}

object Segment3D {
  /**
    * An overloaded constructor that uses a pair of individual coordinates
    * and uses their difference to define a direction.
    * @param ax the 'x' coordinate of the position
    * @param ay the 'y' coordinate of the position
    * @param az the 'z' coordinate of the position
    * @param bx the 'x' coordinate of a destination position
    * @param by the 'y' coordinate of a destination position
    * @param bz the 'z' coordinate of a destination position
    * @return a `Segment3D` entity
    */
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Segment3D = {
    Segment3D(Point3D(ax, ay, az), Point3D(bx, by, bz))
  }

  /**
    * An overloaded constructor.
    * @param p the point of origin
    * @param d the direction and distance (of the second point)
    */
  def apply(p: Point3D, d: Vector3): Segment3D = {
    Segment3D(p, Point3D(p.x + d.x, p.y + d.y, p.z + d.z))
  }

  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Segment3D` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment3D = {
    Segment3D(Point3D(x, y, z), Point3D(x + d.x, y + d.y, z + d.z))
  }
}
