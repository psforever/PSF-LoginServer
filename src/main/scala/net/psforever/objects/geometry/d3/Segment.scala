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
final case class Segment(p1: Point, p2: Point)
  extends Geometry3D
  with geometry.Segment {
  /**
    * The center point of a segment is a position that is equally in between both endpoints.
    * @return a point
    */
  def center: Point = Point((p2.asVector3 + p1.asVector3) * 0.5f)

  def moveCenter(point: geometry.Point): Geometry3D = {
    Segment(
      Point(point.asVector3 - Vector3.Unit(d) * Vector3.Magnitude(d) * 0.5f),
      d
    )
  }

  def d: Vector3 = p2.asVector3 - p1.asVector3

  def asLine: Line = Line(p1, Vector3.Unit(d))
}

object Segment {
  /**
    * An overloaded constructor that uses a pair of individual coordinates
    * and uses their difference to define a direction.
    * @param ax the 'x' coordinate of the position
    * @param ay the 'y' coordinate of the position
    * @param az the 'z' coordinate of the position
    * @param bx the 'x' coordinate of a destination position
    * @param by the 'y' coordinate of a destination position
    * @param bz the 'z' coordinate of a destination position
    * @return a `Segment` entity
    */
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Segment = {
    Segment(Point(ax, ay, az), Point(bx, by, bz))
  }

  /**
    * An overloaded constructor.
    * @param p the point of origin
    * @param d the direction and distance (of the second point)
    */
  def apply(p: Point, d: Vector3): Segment = {
    Segment(p, Point(p.x + d.x, p.y + d.y, p.z + d.z))
  }

  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Segment` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment = {
    Segment(Point(x, y, z), Point(x + d.x, y + d.y, z + d.z))
  }
}
