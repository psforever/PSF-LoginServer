// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d2

import net.psforever.objects.geometry.{AxisAlignment2D, PrimitiveGeometry}
import net.psforever.types.Vector3

/**
  * Basic interface of all two-dimensional geometry.
  */
trait Geometry2D extends PrimitiveGeometry {
  def center: Point

  def inPlane: AxisAlignment2D

  def pointOnOutside(v: Vector3): Point = center
}

trait Flat extends Geometry2D
