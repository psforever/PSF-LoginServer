// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry.PrimitiveGeometry
import net.psforever.types.Vector3

/**
  * Basic interface of all three-dimensional geometry.
  * For the only real requirement for a three-dimensional geometric figure is that it has three components of position
  * and an equal number of components demonstrating equal that said dimensionality.
  */
trait Geometry3D extends PrimitiveGeometry {
  def center: Point3D

  def pointOnOutside(v: Vector3): Point3D = center
}

trait VolumetricGeometry extends Geometry3D
