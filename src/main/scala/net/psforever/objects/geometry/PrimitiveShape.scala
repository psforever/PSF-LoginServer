// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

trait PrimitiveGeometry {
  def center: Point

  def pointOnOutside(line: Line) : Point = pointOnOutside(line.d)

  def pointOnOutside(v: Vector3) : Point
}

//trait Geometry2D extends PrimitiveGeometry {
//  def center: Point2D
//
//  def pointOnOutside(v: Vector3): Point2D = center
//}

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

final case class Cylinder(position: Vector3, relativeUp: Vector3, radius: Float, height: Float) extends Geometry3D {
  def center: Point3D = Point3D(position + relativeUp * height * 0.5f)

  override def pointOnOutside(v: Vector3): Point3D = {
    val centerVector = center.asVector3
    val slope = Vector3.Unit(v)
    val acrossTopAndBase = slope - relativeUp
    val pointOnSide = centerVector + slope * (radius / Vector3.Magnitude(acrossTopAndBase))
    val pointOnBase = position + acrossTopAndBase * radius
    val pointOnTop = pointOnBase + relativeUp * height
    val fromPointOnTopToSide = Vector3.Unit(pointOnTop - pointOnSide)
    val fromPointOnSideToBase = Vector3.Unit(pointOnSide - pointOnBase)
    val target = if(fromPointOnTopToSide  == Vector3.Zero ||
                    fromPointOnSideToBase == Vector3.Zero ||
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

object Cylinder {
  def apply(v: Vector3, radius: Float, height: Float): Cylinder = Cylinder(v, Vector3(0,0,1), radius, height)

  def apply(p: Point3D, radius: Float, height: Float): Cylinder = Cylinder(p.asVector3, Vector3(0,0,1), radius, height)

  def apply(p: Point3D, v: Vector3, radius: Float, height: Float): Cylinder = Cylinder(p.asVector3, v, radius, height)
}
