// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.types.Vector3

/**
  * The instance of a volumetric region that encapsulates all points within a certain distance of a central point.
  * (That's what a sphere is.)
  * A sphere has no real "top", "base", or "side" as all directions are described the same.
  * @param p the point
  * @param radius a distance that spans all points in any direction from the central point
  */
final case class Sphere(p: Point3D, radius: Float)
  extends VolumetricGeometry {
  def center: Point3D = p

  /**
    * Find a point on the exterior of the geometry if a line was drawn outwards from the centroid.
    * All points that exist on the exterior of a sphere are on the surface of that sphere
    * and are equally distant from the central point.
    * @param v the vector in the direction of the point on the exterior
    * @return a point
    */
  override def pointOnOutside(v: Vector3): Point3D = {
    val slope = Vector3.Unit(v)
    val mult = radius / Vector3.Magnitude(slope)
    Point3D(center.asVector3 + slope * mult)
  }
}

object Sphere {
  /**
    * An overloaded constructor that only defines the radius of the sphere
    * and places it at the world origin.
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(radius: Float): Sphere = Sphere(Point3D(), radius)

  /**
    * An overloaded constructor that uses individual coordinates to define the central point.
    * * @param x the 'x' coordinate of the position
    * * @param y the 'y' coordinate of the position
    * * @param z the 'z' coordinate of the position
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(x: Float, y: Float, z: Float, radius: Float): Sphere = Sphere(Point3D(x,y,z), radius)

  /**
    * An overloaded constructor that uses vector coordinates to define the central point.
    * @param v the coordinates of the position
    * @param radius a distance around the world origin coordinates
    * @return a `Sphere` entity
    */
  def apply(v: Vector3, radius: Float): Sphere = Sphere(Point3D(v), radius)
}
