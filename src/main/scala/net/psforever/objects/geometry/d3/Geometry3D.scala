// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry
import net.psforever.types.Vector3

/**
  * Basic interface of all three-dimensional geometry.
  * For the only real requirement for a three-dimensional geometric figure is that it has three components of position
  * and an equal number of components demonstrating equal that said dimensionality.
  */
trait Geometry3D extends geometry.PrimitiveGeometry {
  def center: Point

  def moveCenter(point: geometry.Point): Geometry3D
}

trait VolumetricGeometry extends Geometry3D {

  def moveCenter(point: geometry.Point): VolumetricGeometry
  /**
    * Find a point on the exterior of the geometry if a line was drawn outwards from the centroid.
    * What counts as "the exterior" is limited to the complexity of the geometry.
    * @param v the vector in the direction of the point on the exterior
    * @return a point
    */
  def pointOnOutside(v: Vector3): Point
}
