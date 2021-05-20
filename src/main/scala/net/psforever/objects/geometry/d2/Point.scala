// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d2

import net.psforever.objects.geometry
import net.psforever.objects.geometry.{AxisAlignment, AxisAlignment2D}
import net.psforever.types.Vector3

/**
  * The instance of a coordinate position in two-dimensional space.
  * @see `Vector3`
  * @param a the first coordinate of the position
  * @param b the second coordinate of the position
  * @param inPlane the planar orientation of "out", e.g., the XY-axis excludes z-coordinates;
  *                includes the identity of the coordinates as they are in-order
  */
final case class Point(a: Float, b: Float, inPlane: AxisAlignment2D)
  extends Geometry2D
  with geometry.Point {
  def center: Point = this

  def moveCenter(point: geometry.Point): Point = {
    point match {
      case p: Point if inPlane == p.inPlane => Point(p.a, p.b, inPlane)
      case _                                => this
    }
  }

  def asVector3: Vector3 = inPlane.asVector3(a, b)
}

object Point {
  /**
    * An overloaded constructor that assigns world origin coordinates.
    * By default, the planar frame is the common XY-axis.
    * @return a `Point2D` entity
    */
  def apply(): Point = Point(0,0, AxisAlignment.XY)

  /**
    * An overloaded constructor that assigns world origin coordinates.
    * By default, the planar frame is the common XY-axis.
    * @return a `Point2D` entity
    */
  def apply(point: geometry.Point): Point = {
    val p = point.asVector3
    Point(p.x, p.y, AxisAlignment.XY)
  }

  /**
    * An overloaded constructor that assigns world origin coordinates in the given planar frame.
    * @return a `Point2D` entity
    */
  def apply(frame: AxisAlignment2D): Point = Point(0,0, frame)

  /**
    * An overloaded constructor that uses the same coordinates from a `Vector3` entity.
    * By default, the planar frame is the common XY-axis.
    * @param v the entity with the corresponding points
    * @return a `Point2D` entity
    */
  def apply(v: Vector3): Point = Point(v.x, v.y, AxisAlignment.XY)

  /**
    * An overloaded constructor that uses the same coordinates from a `Vector3` entity in the given planar frame.
    * By default, the planar frame is the common XY-axis.
    * @param v the entity with the corresponding points
    * @return a `Point2D` entity
    */
  def apply(v: Vector3, frame: AxisAlignment2D): Point = {
    frame match {
      case AxisAlignment.XY => Point(v.x, v.y, frame)
      case AxisAlignment.YZ => Point(v.y, v.z, frame)
      case AxisAlignment.XZ => Point(v.x, v.z, frame)
    }
  }
}
