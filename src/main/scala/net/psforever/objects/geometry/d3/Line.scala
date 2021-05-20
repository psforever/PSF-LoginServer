// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a geometric coordinate position and a specific direction from that position.
  * Mathematical lines have infinite length and their slope is represented as a unit vector.
  * The point is merely a point used to assist in defining the line.
  * @param p the point of origin
  * @param d the direction
  */
final case class Line(p: Point, d: Vector3)
  extends Geometry3D
  with geometry.Line {
  def center: Point = p

  def moveCenter(point: geometry.Point): Geometry3D = Line(Point(point), d)
}

object Line {
  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Line` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Line = {
    Line(Point(x,y,z), d)
  }

  /**
    * An overloaded constructor that uses a pair of individual coordinates
    * and uses their difference to produce a unit vector to define a direction.
    * @param ax the 'x' coordinate of the position
    * @param ay the 'y' coordinate of the position
    * @param az the 'z' coordinate of the position
    * @param bx the 'x' coordinate of a destination position
    * @param by the 'y' coordinate of a destination position
    * @param bz the 'z' coordinate of a destination position
    * @return a `Line` entity
    */
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Line = {
    Line(Point(ax, ay, az), Vector3.Unit(Vector3(bx - ax, by - ay, bz - az)))
  }

  /**
    * An overloaded constructor that uses a pair of points
    * and uses their difference to produce a unit vector to define a direction.
    * @param p1 the coordinates of the position
    * @param p2 the coordinates of a destination position
    * @return a `Line` entity
    */
  def apply(p1: Point, p2: Point): Line = {
    Line(p1, Vector3.Unit(Vector3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z)))
  }
}
