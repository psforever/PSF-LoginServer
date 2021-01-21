// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

trait Slope {
  def d: Vector3

  def length: Float
}

trait Line extends Slope {
  assert({
    val mag = Vector3.Magnitude(d)
    mag - 0.05f < 1f && mag + 0.05f > 1f
  }, "not a unit vector")

  def length: Float = Float.PositiveInfinity
}

trait Segment extends Slope {
  def length: Float = Vector3.Magnitude(d)
}

final case class Line2D(x: Float, y: Float, d: Vector3) extends Line

object Line2D {
  def apply(ax: Float, ay: Float, bx: Float, by: Float): Line2D = {
    Line2D(ax, ay, Vector3.Unit(Vector3(bx-ax, by-ay, 0)))
  }
}

final case class Segment2D(ax: Float, ay: Float, bx: Float, by: Float) extends Segment {
  def d: Vector3 = Vector3(bx - ax, by - ay, 0)
}

object Segment2D {
  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment2D = {
    Segment2D(x, y, x + d.x, y + d.y)
  }
}

final case class Line3D(x: Float, y: Float, z: Float, d: Vector3) extends Line

final case class Segment3D(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float) extends Segment {
  def d: Vector3 = Vector3(bx - ax, by - ay, bz - az)
}

object Segment3D {
  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment3D = {
    Segment3D(x, y, z, z+d.x, y+d.y, z+d.z)
  }
}

object Geometry {
  def equalFloats(value1: Float, value2: Float, off: Float = 0.001f): Boolean = {
    val diff = value1 - value2
    (diff >= 0 && diff <= off) || diff > -off
  }

  def equalVectors(value1: Vector3, value2: Vector3, off: Float = 0.001f): Boolean = {
    equalFloats(value1.x, value2.x, off) &&
    equalFloats(value1.y, value2.y, off) &&
    equalFloats(value1.z, value2.z, off)
  }

  def closeToInsignificance(d: Float, epsilon: Float = 10f): Float = {
    val ulp = math.ulp(epsilon)
    math.signum(d) match {
      case -1f =>
        val n = math.abs(d)
        val p = math.abs(n - n.toInt)
        if (p < ulp || d > ulp) d + p else d
      case _ =>
        val p = math.abs(d - d.toInt)
        if (p < ulp || d < ulp) d - p else d
    }
  }
}
