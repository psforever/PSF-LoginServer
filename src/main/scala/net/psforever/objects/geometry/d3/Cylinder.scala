// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry._
import net.psforever.types.Vector3

/**
  * The instance of a volumetric region that encapsulates all points within a certain distance of a central point.
  * The region is characterized by a regular circular cross-section when observed from above or below
  * and a flat top and a flat base when viewed from the side.
  * The "base" is where the origin point is defined (at the center of a circular cross-section)
  * and the "top" is discovered a `height` from the base along what the cylinder considers its `relativeUp` direction.
  * @param p the point
  * @param relativeUp what the cylinder considers its "up" direction
  * @param radius a distance expressed in all circular cross-sections along the `relativeUp` direction
  * @param height the distance between the "base" and the "top"
  */
final case class Cylinder(p: Point3D, relativeUp: Vector3, radius: Float, height: Float)
  extends VolumetricGeometry {
  Slope.assertUnitVector(relativeUp)

  /**
    * The center point of a cylinder is halfway between the "top" and the "base" along the direction of `relativeUp`.
    * @return a point
    */
  def center: Point3D = Point3D(p.asVector3 + relativeUp * height * 0.5f)

  /**
    * Find a point on the exterior of the geometry if a line was drawn outwards from the centroid.
    * A cylinder is composed of three clearly-defined regions on its exterior -
    * two flat but circular surfaces that are the "top" and the "base"
    * and a wrapped "sides" surface that defines all points connecting the "base" to the "top"
    * along the `relativeUp` direction.
    * The requested point may exist on any of these surfaces.
    * @param v the vector in the direction of the point on the exterior
    * @return a point
    */
  override def pointOnOutside(v: Vector3): Point3D = {
    val centerVector = center.asVector3
    val slope = Vector3.Unit(v)
    val dotProdOfSlopeAndUp = Vector3.DotProduct(slope, relativeUp)
    if (Geometry.equalFloats(dotProdOfSlopeAndUp, value2 = 1) || Geometry.equalFloats(dotProdOfSlopeAndUp, value2 = -1)) {
      // very rare condition: 'slope' and 'relativeUp' are parallel or antiparallel
      Point3D(centerVector + slope * height * 0.5f)
    } else {
      val acrossTopAndBase = slope - relativeUp * dotProdOfSlopeAndUp
      val pointOnSide = centerVector + slope * (radius / Vector3.Magnitude(acrossTopAndBase))
      val pointOnBase = p.asVector3 + acrossTopAndBase * radius
      val pointOnTop = pointOnBase + relativeUp * height
      val fromPointOnTopToSide = Vector3.Unit(pointOnTop - pointOnSide)
      val fromPointOnSideToBase = Vector3.Unit(pointOnSide - pointOnBase)
      val target = if(Geometry.equalVectors(fromPointOnTopToSide, Vector3.Zero) ||
                      Geometry.equalVectors(fromPointOnSideToBase, Vector3.Zero) ||
                      Geometry.equalVectors(fromPointOnTopToSide, fromPointOnSideToBase)) {
        //on side, including top rim or base rim
        pointOnSide
      } else {
        //on top or base
        // the full equation would be 'centerVector + slope * (height * 0.5f / Vector3.Magnitude(relativeUp))'
        // 'relativeUp` is already a unit vector (magnitude of 1)
        centerVector + slope * height * 0.5f
      }
      Point3D(target)
    }
  }
}

object Cylinder {
  /**
    * An overloaded constructor where the 'relativeUp' of the cylinder is perpendicular to the xy-plane.
    * @param p the point
    * @param radius a distance expressed in all circular cross-sections along the `relativeUp` direction
    * @param height the distance between the "base" and the "top"
    * @return
    */
  def apply(p: Point3D, radius: Float, height: Float): Cylinder = Cylinder(p, Vector3(0,0,1), radius, height)

  /**
    * An overloaded constructor where the origin point is expressed as a vector
    * and the 'relativeUp' of the cylinder is perpendicular to the xy-plane.
    * @param p the point
    * @param radius a distance expressed in all circular cross-sections along the `relativeUp` direction
    * @param height the distance between the "base" and the "top"
    * @return
    */
  def apply(p: Vector3, radius: Float, height: Float): Cylinder = Cylinder(Point3D(p), Vector3(0,0,1), radius, height)

  /**
    * An overloaded constructor the origin point is expressed as a vector.
    * @param p the point
    * @param v what the cylinder considers its "up" direction
    * @param radius a distance expressed in all circular cross-sections along the `relativeUp` direction
    * @param height the distance between the "base" and the "top"
    * @return
    */
  def apply(p: Vector3, v: Vector3, radius: Float, height: Float): Cylinder = Cylinder(Point3D(p), v, radius, height)
}
