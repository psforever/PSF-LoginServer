// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d2

import net.psforever.objects.geometry.{AxisAlignment2D, PrimitiveGeometry}
import net.psforever.types.Vector3

/**
  * Basic interface of all two-dimensional geometry.
  * For the only real requirement for a three-dimensional geometric figure is that it has three components of position
  * and an equal number of components demonstrating equal that said dimensionality.
  */
trait Geometry2D extends PrimitiveGeometry {
  def center: Point

  def inPlane: AxisAlignment2D

  def pointOnOutside(v: Vector3): Point = center
}

trait Flat extends Geometry2D
