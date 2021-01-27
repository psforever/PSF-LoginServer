// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

trait PrimitiveGeometry {
  def center: Point

  def pointOnOutside(line: Line) : Point = pointOnOutside(line.d)

  def pointOnOutside(v: Vector3) : Point
}

trait Geometry2D extends PrimitiveGeometry {
  def center: Point2D

  def pointOnOutside(v: Vector3): Point2D = center
}

trait Geometry3D extends PrimitiveGeometry {
  def center: Point3D

  def pointOnOutside(v: Vector3): Point3D = center
}

trait Point {
  def asVector3: Vector3
}

trait Slope {
  def d: Vector3

  def length: Float
}

trait Line extends Slope {
  assert({
    val mag = Vector3.Magnitude(d)
    mag - 0.05f < 1f && mag + 0.05f > 1f
  }, "not a unit vector")

  def p: Point

  def length: Float = Float.PositiveInfinity
}

trait Segment extends Slope {
  def p1: Point

  def p2: Point

  def length: Float = Vector3.Magnitude(d)

  def asLine: PrimitiveGeometry
}

final case class Point2D(x: Float, y: Float) extends Geometry2D with Point {
  def center: Point2D = this

  def asVector3: Vector3 = Vector3(x, y, 0)
}

object Point2D {
  def apply(): Point2D = Point2D(0, 0)

  def apply(v: Vector3): Point2D = Point2D(v.x, v.y)
}

final case class Ray2D(p: Point2D, d: Vector3) extends Geometry2D with Line {
  def center: Point2D = p
}

object Ray2D {
  def apply(x: Float, y: Float, d: Vector3): Ray2D = Ray2D(Point2D(x, y), d)
}

final case class Line2D(p: Point2D, d: Vector3) extends Geometry2D with Line {
  def center: Point2D = p
}

object Line2D {
  def apply(ax: Float, ay: Float, d: Vector3): Line2D = {
    Line2D(Point2D(ax, ay), d)
  }

  def apply(ax: Float, ay: Float, bx: Float, by: Float): Line2D = {
    Line2D(Point2D(ax, ay), Vector3.Unit(Vector3(bx-ax, by-ay, 0)))
  }

  def apply(p1: Point2D, p2: Point2D): Line2D = {
    Line2D(p1, Vector3.Unit(Vector3(p2.x-p1.x, p2.y-p1.y, 0)))
  }
}

final case class Segment2D(p1: Point2D, p2: Point2D) extends Geometry2D with Segment {
  def center: Point2D = Point2D(d * 0.5f)

  def d: Vector3 = p2.asVector3 - p1.asVector3

  def asLine: Line2D = Line2D(p1, Vector3.Unit(d))
}

object Segment2D {
  def apply(ax: Float, ay: Float, bx: Float, by: Float): Segment2D = {
    Segment2D(Point2D(ax, ay), Point2D(bx, by))
  }

  def apply(x: Float, y: Float, d: Vector3): Segment2D = {
    Segment2D(x, y, x + d.x, y + d.y)
  }
}

final case class Circle(p: Point2D, radius: Float) extends Geometry2D {
  def center : Point2D = p

  override def pointOnOutside(v: Vector3) : Point2D = {
    val slope = Vector3.Unit(v)
    val pointOnRim = p.asVector3 + slope * radius
    Point2D(pointOnRim.x, pointOnRim.y)
  }
}

object Circle {
  def apply(radius: Float): Circle = Circle(Point2D(), radius)

  def apply(x: Float, y: Float, radius: Float): Circle = Circle(Point2D(x, y), radius)
}


final case class Point3D(x: Float, y: Float, z: Float) extends Geometry3D with Point {
  def center: Point3D = this

  def asVector3: Vector3 = Vector3(x, y, z)
}

object Point3D {
  def apply(): Point3D = Point3D(0,0,0)

  def apply(v: Vector3): Point3D = Point3D(v.x, v.y, v.z)
}

final case class Ray3D(p: Point3D, d: Vector3) extends Geometry3D with Line {
  def center: Point3D = p
}

object Ray3D {
  def apply(x: Float, y: Float, z: Float, d: Vector3): Ray3D = Ray3D(Point3D(x,y,z), d)
}

final case class Line3D(p: Point3D, d: Vector3) extends Geometry3D with Line {
  def center: Point3D = p
}

object Line3D {
  def apply(x: Float, y: Float, z: Float, d: Vector3): Line3D = {
    Line3D(Point3D(x,y,z), d)
  }

  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Line3D = {
    Line3D(Point3D(ax, ay, az), Vector3.Unit(Vector3(bx-ax, by-ay, bz-az)))
  }

  def apply(p1: Point3D, p2: Point3D): Line3D = {
    Line3D(p1, Vector3.Unit(Vector3(p2.x-p1.x, p2.y-p1.y, p2.z-p1.z)))
  }
}

final case class Segment3D(p1: Point3D, p2: Point3D) extends Geometry3D with Segment {
  def center: Point3D = Point3D(d * 0.5f)

  def d: Vector3 = p2.asVector3 - p1.asVector3

  def asLine: Line3D = Line3D(p1, Vector3.Unit(d))
}

object Segment3D {
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Segment3D = {
    Segment3D(Point3D(ax, ay, az), Point3D(bx, by, bz))
  }

  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment3D = {
    Segment3D(Point3D(x, y, z), Point3D(x + d.x, y + d.y, z + d.z))
  }
}

final case class Sphere(p: Point3D, radius: Float) extends Geometry3D {
  def center: Point3D = p

  override def pointOnOutside(v: Vector3): Point3D = {
    val slope = Vector3.Unit(v)
    val mult = radius / math.sqrt(slope.x * slope.x + slope.y * slope.y + slope.z * slope.z)
    val pointOnSurface = center.asVector3 + slope * mult.toFloat
    Point3D(pointOnSurface.x, pointOnSurface.y, pointOnSurface.z)
  }
}

object Sphere {
  def apply(radius: Float): Sphere = Sphere(Point3D(), radius)

  def apply(x: Float, y: Float, z: Float, radius: Float): Sphere = Sphere(Point3D(x,y,z), radius)

  def apply(v: Vector3, radius: Float): Sphere = Sphere(Point3D(v), radius)
}

final case class Cylinder(circle: Circle, z: Float, height: Float) extends Geometry3D {
  def center: Point3D = Point3D(circle.p.x, circle.p.y, z + height * 0.5f)

  override def pointOnOutside(v: Vector3): Point3D = {
    val centerVector = center.asVector3
    val slope = Vector3.Unit(v)
    val mult = circle.radius / math.sqrt(slope.x * slope.x + slope.y * slope.y)
    val pointOnRim = centerVector + slope * mult.toFloat
    val point = if (z >= pointOnRim.z && pointOnRim.z <= height) { //side
      pointOnRim
    } else { //top or base
      val rise = height * 0.5f / slope.z
      centerVector + slope * rise
    }
    Point3D(point.x, point.y, point.z)
  }
}

object Cylinder {
  def apply(x: Float, y: Float, z: Float, radius: Float, height: Float): Cylinder = {
    Cylinder(Circle(x, y, radius), z, height)
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
