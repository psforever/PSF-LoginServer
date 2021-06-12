// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * The instance of a volumetric region that encapsulates all points within a certain distance of a central point.
  * (That's what a sphere is.)
  * When described by its center point, a sphere has no distinct "top", "base", or "side";
  * all directions are described in the same way in reference to this center.
  * It can be considered having a "base" and other "faces" for the purposes of settling on a surface (the ground).
  * @param p the point
  * @param radius a distance that spans all points in any direction from the central point
  */
final case class Sphere(p: Point, radius: Float)
  extends VolumetricGeometry {
  def center: Point = p

  def moveCenter(point: geometry.Point): Sphere = Sphere(Point(point), radius)

  /**
    * Find a point on the exterior of the geometry if a line was drawn outwards from the centroid.
    * All points that exist on the exterior of a sphere are on the surface of that sphere
    * and are equally distant from the central point.
    * @param v the vector in the direction of the point on the exterior
    * @return a point
    */
  override def pointOnOutside(v: Vector3): Point = {
    val slope = Vector3.Unit(v)
    val mult = radius / Vector3.Magnitude(slope)
    Point(center.asVector3 + slope * mult)
  }
}

object Sphere {
  /**
    * An overloaded constructor that only defines the radius of the sphere
    * and places it at the world origin.
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(radius: Float): Sphere = Sphere(Point(), radius)

  /**
    * An overloaded constructor that uses individual coordinates to define the central point.
    * * @param x the 'x' coordinate of the position
    * * @param y the 'y' coordinate of the position
    * * @param z the 'z' coordinate of the position
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(x: Float, y: Float, z: Float, radius: Float): Sphere = Sphere(Point(x,y,z), radius)

  /**
    * An overloaded constructor that uses vector coordinates to define the central point.
    * @param v the coordinates of the position
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(v: Vector3, radius: Float): Sphere = Sphere(Point(v), radius)
}
