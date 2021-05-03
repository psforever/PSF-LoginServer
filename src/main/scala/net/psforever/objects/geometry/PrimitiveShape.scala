// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

/**
  * Basic interface for all geometry.
  */
trait PrimitiveGeometry {
  /**
    * The centroid of the geometry.
    * @return a point
    */
  def center: Point

  /**
    * Find a point on the exterior of the geometry if a line was drawn outwards from the centroid.
    * What counts as "the exterior" is limited to the complexity of the geometry.
    * @param v the vector in the direction of the point on the exterior
    * @return a point
    */
  def pointOnOutside(v: Vector3) : Point
}

//trait Geometry2D extends PrimitiveGeometry {
//  def center: Point2D
//
//  def pointOnOutside(v: Vector3): Point2D = center
//}

/**
  * Basic interface of all three-dimensional geometry.
  * For the only real requirement for a hree-dimensional geometric figure is that it has three components of position
  * and an equal number of components demonstrating equal that said dimensionality.
  */
trait Geometry3D extends PrimitiveGeometry {
  def center: Point3D

  def pointOnOutside(v: Vector3): Point3D = center
}

/**
  * Characteristics of a geometric figure with only three coordinates to define a position.
  */
trait Point {
  /**
    * Transform the point into the common interchangeable format for coordinates.
    * They're very similar, anyway.
    * @return a `Vector3` entity of the same denomination
    */
  def asVector3: Vector3
}

/**
  * Characteristics of a geometric figure defining a direction or a progressive change in coordinates.
  */
trait Slope {
  /**
    * The slope itself.
    * @return a `Vector3` entity
    */
  def d: Vector3

  /**
    * How long the slope goes on for.
    * @return The length of the slope
    */
  def length: Float
}

object Slope {
  /**
    * On occasions, the defined slope should have a length of one unit.
    * It is a unit vector.
    * @param v the input slope as a `Vector3` entity
    * @throws `AssertionError` if the length is more or less than 1.
    */
  def assertUnitVector(v: Vector3): Unit = {
    assert({
      val mag = Vector3.Magnitude(v)
      mag - 0.05f < 1f && mag + 0.05f > 1f
    }, "not a unit vector")
  }
}

/**
  * Characteristics of a geometric figure indicating an infinite slope - a mathematical line.
  * The slope is always a unit vector.
  * The point that assists to define the line is a constraint that the line must pass through.
  */
trait Line extends Slope {
  Slope.assertUnitVector(d)

  def p: Point

  /**
    * The length of a mathematical line is infinite.
    * @return The length of the slope
    */
  def length: Float = Float.PositiveInfinity
}

/**
  * Characteristics of a geometric figure that have two endpoints, defining a fixed-length slope.
  */
trait Segment extends Slope {
  /** The first point, considered the "start". */
  def p1: Point
  /** The second point, considered the "end". */
  def p2: Point

  def length: Float = Vector3.Magnitude(d)

  /**
    * Transform the segment into a matheatical line of the same slope.
    * @return
    */
  def asLine: PrimitiveGeometry
}

/**
  * The instance of a geometric coordinate position.
  * @see `Vector3`
  * @param x the 'x' coordinate of the position
  * @param y the 'y' coordinate of the position
  * @param z the 'z' coordinate of the position
  */
final case class Point3D(x: Float, y: Float, z: Float) extends Geometry3D with Point {
  def center: Point3D = this

  def asVector3: Vector3 = Vector3(x, y, z)
}

object Point3D {
  /**
    * An overloaded constructor that assigns world origin coordinates.
    * @return a `Point3D` entity
    */
  def apply(): Point3D = Point3D(0,0,0)

  /**
    * An overloaded constructor that uses the same coordinates from a `Vector3` entity.
    * @param v the entity with the corresponding points
    * @return a `Point3D` entity
    */
  def apply(v: Vector3): Point3D = Point3D(v.x, v.y, v.z)
}

/**
  * The instance of a geometric coordinate position and a specific direction from that position.
  * Rays are like mathematical lines in that they have infinite length;
  * but, that infinite length is only expressed in a single direction,
  * rather than proceeding in both a direction and its opposite direction from a target point.
  * Infinity just be like that.
  * Additionally, the point is not merely any point on the ray used to assist defining it
  * and is instead considered the clearly-defined origin of the ray.
  * @param p the point of origin
  * @param d the direction
  */
final case class Ray3D(p: Point3D, d: Vector3) extends Geometry3D with Line {
  def center: Point3D = p
}

object Ray3D {
  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Ray3D` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Ray3D = Ray3D(Point3D(x,y,z), d)

  /**
    * An overloaded constructor that uses a `Vector3` entity to express coordinates.
    * @param v the coordinates of the position
    * @param d the direction
    * @return a `Ray3D` entity
    */
  def apply(v: Vector3, d: Vector3): Ray3D = Ray3D(Point3D(v.x, v.y, v.z), d)
}

/**
  * The instance of a geometric coordinate position and a specific direction from that position.
  * Mathematical lines have infinite length and their slope is represented as a unit vector.
  * The point is merely a point used to assist in defining the line.
  * @param p the point of origin
  * @param d the direction
  */
final case class Line3D(p: Point3D, d: Vector3) extends Geometry3D with Line {
  def center: Point3D = p
}

object Line3D {
  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Line3D` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Line3D = {
    Line3D(Point3D(x,y,z), d)
  }

  /**
    * An overloaded constructor that uses a pair of individual coordinates
    * and uses their difference to produce a unit vector to define a direction.
    * @param ax the 'x' coordinate of the position
    * @param ay the 'y' coordinate of the position
    * @param az the 'z' coordinate of the position
    * @param bx the 'x' coordinate of a destination position
    * @param by the 'y' coordinate of a destination position
    * @param bz the 'z' coordinate of a destination position
    * @return a `Line3D` entity
    */
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Line3D = {
    Line3D(Point3D(ax, ay, az), Vector3.Unit(Vector3(bx-ax, by-ay, bz-az)))
  }

  /**
    * An overloaded constructor that uses a pair of points
    * and uses their difference to produce a unit vector to define a direction.
    * @param p1 the coordinates of the position
    * @param p2 the coordinates of a destination position
    * @return a `Line3D` entity
    */
  def apply(p1: Point3D, p2: Point3D): Line3D = {
    Line3D(p1, Vector3.Unit(Vector3(p2.x-p1.x, p2.y-p1.y, p2.z-p1.z)))
  }
}

/**
  * The instance of a limited span between two geometric coordinate positions, called "endpoints".
  * Unlike mathematical lines, slope is treated the same as the vector leading from one point to the other
  * and is the length of the segment.
  * @param p1 a point
  * @param p2 another point
  */
final case class Segment3D(p1: Point3D, p2: Point3D) extends Geometry3D with Segment {
  /**
    * The center point of a segment is a position that is equally in between both endpoints.
    * @return a point
    */
  def center: Point3D = Point3D((p2.asVector3 + p1.asVector3) * 0.5f)

  def d: Vector3 = p2.asVector3 - p1.asVector3

  def asLine: Line3D = Line3D(p1, Vector3.Unit(d))
}

object Segment3D {
  /**
    * An overloaded constructor that uses a pair of individual coordinates
    * and uses their difference to define a direction.
    * @param ax the 'x' coordinate of the position
    * @param ay the 'y' coordinate of the position
    * @param az the 'z' coordinate of the position
    * @param bx the 'x' coordinate of a destination position
    * @param by the 'y' coordinate of a destination position
    * @param bz the 'z' coordinate of a destination position
    * @return a `Segment3D` entity
    */
  def apply(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float): Segment3D = {
    Segment3D(Point3D(ax, ay, az), Point3D(bx, by, bz))
  }

  /**
    * An overloaded constructor.
    * @param p the point of origin
    * @param d the direction and distance (of the second point)
    */
  def apply(p: Point3D, d: Vector3): Segment3D = {
    Segment3D(p, Point3D(p.x + d.x, p.y + d.y, p.z + d.z))
  }

  /**
    * An overloaded constructor that uses individual coordinates.
    * @param x the 'x' coordinate of the position
    * @param y the 'y' coordinate of the position
    * @param z the 'z' coordinate of the position
    * @param d the direction
    * @return a `Segment3D` entity
    */
  def apply(x: Float, y: Float, z: Float, d: Vector3): Segment3D = {
    Segment3D(Point3D(x, y, z), Point3D(x + d.x, y + d.y, z + d.z))
  }
}

/**
  * The instance of a volumetric region that encapsulates all points within a certain distance of a central point.
  * (That's what a sphere is.)
  * A sphere has no real "top", "base", or "side" as all directions are described the same.
  * @param p the point
  * @param radius a distance that spans all points in any direction from the central point
  */
final case class Sphere(p: Point3D, radius: Float) extends Geometry3D {
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
final case class Cylinder(p: Point3D, relativeUp: Vector3, radius: Float, height: Float) extends Geometry3D {
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

/**
  * Untested geometry.
  * @param p na
  * @param relativeForward na
  * @param relativeUp na
  * @param length na
  * @param width na
  * @param height na
  */
final case class Cuboid(
                         p: Point3D,
                         relativeForward: Vector3,
                         relativeUp: Vector3,
                         length: Float,
                         width: Float,
                         height: Float,
                       ) extends Geometry3D {
  def center: Point3D = Point3D(p.asVector3 + relativeUp * height * 0.5f)

  override def pointOnOutside(v: Vector3): Point3D = {
    import net.psforever.types.Vector3.{CrossProduct, DotProduct, neg}
    val height2 = height * 0.5f
    val relativeSide = CrossProduct(relativeForward, relativeUp)
    //val forwardVector = relativeForward * length
    //val sideVector = relativeSide * width
    //val upVector = relativeUp * height2
    val closestVector: Vector3 = Seq(
      relativeForward, relativeSide, relativeUp,
      neg(relativeForward), neg(relativeSide), neg(relativeUp)
    ).maxBy { dir => DotProduct(dir, v) }
    def dz(): Float = {
      if (Geometry.closeToInsignificance(v.z) != 0) {
        closestVector.z / v.z
      } else {
        0f
      }
    }
    def dy(): Float = {
      if (Geometry.closeToInsignificance(v.y) != 0) {
        val fyfactor = closestVector.y / v.y
        if (v.z * fyfactor <= height2) {
          fyfactor
        } else {
          dz()
        }
      } else {
        dz()
      }
    }

    val scaleFactor: Float = {
      if (Geometry.closeToInsignificance(v.x) != 0) {
        val fxfactor = closestVector.x / v.x
        if (v.y * fxfactor <= length) {
          if (v.z * fxfactor <= height2) {
            fxfactor
          } else {
            dy()
          }
        } else {
          dy()
        }
      } else {
        dy()
      }
    }
    Point3D(center.asVector3 + (v * scaleFactor))
  }
}
