// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d2

import net.psforever.objects.geometry
import net.psforever.objects.geometry.{AxisAlignment, AxisAlignment2D}

final case class Rectangle(top: Float, right: Float, base: Float, left: Float, inPlane: AxisAlignment2D)
  extends Flat {
  assert(right > left, s"right needs to be greater than left - $right > $left")
  assert(top > base, s"top needs to be greater than base - $top > $base")

  def center: Point = Point((right + left) * 0.5f, (top + base) * 0.5f, inPlane)

  def moveCenter(point: geometry.Point): Rectangle = {
    point match {
      case p: Point if inPlane == p.inPlane =>
        val halfWidth = (right - left) * 0.5f
        val halfHeight = (top - base) * 0.5f
        Rectangle(p.b + halfHeight, p.a + halfWidth, p.b - halfHeight, p.a - halfWidth, inPlane)
      case _ =>
        this
    }
  }
}

object Rectangle {
  def apply(top: Float, right: Float, base: Float, left: Float): Rectangle =
    Rectangle(top, right, base, left, AxisAlignment.XY)
}
