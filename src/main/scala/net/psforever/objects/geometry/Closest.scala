// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

object Closest {
  object Distance {
    def apply(point : Vector3, seg : Segment2D) : Float = {
      val segdx = seg.bx - seg.ax
      val segdy = seg.by - seg.ay
      ((point.x - seg.ax) * segdx + (point.y - seg.ay) * segdy) /
      Vector3.MagnitudeSquared(Vector3(segdx, segdy, 0))
    }

    def apply(line1 : Line2D, line2 : Line2D) : Float = {
      if (Intersection.Test(line1, line2)) { //intersecting lines
        0f
      } else {
        math.abs(
          Vector3.DotProduct(
            Vector3(line2.x - line1.x, line2.y - line1.y, 0),
            Vector3(-1/line1.d.y, 1/line1.d.x, 0)
          )
        )
      }
    }

    def apply(seg1: Segment2D, seg2: Segment2D): Float = {
      if (Intersection.Test(seg1, seg2)) { //intersecting line segments
        0f
      } else {
        val v1a = Vector3(seg1.ax, seg1.ay, 0)
        val v2a = Vector3(seg2.ax, seg2.ay, 0)
        val v1b = Vector3(seg1.bx, seg1.by, 0)
        val v2b = Vector3(seg2.bx, seg2.by, 0)
        math.min(
          apply(v1a, seg2),
          math.min(
            apply(v1b, seg2),
            math.min(
              apply(v2a, seg1),
              apply(v2b, seg1)
            )
          )
        )
      }
    }

    def apply(c1: Circle, c2 : Circle): Float = {
      math.max(0, Vector3.Magnitude(Vector3(c1.x - c2.x, c1.y - c2.y, 0)) - c1.radius - c2.radius)
    }

    /**
      * na
      * @param line1 na
      * @param line2 na
      * @return the shortest distance between the lines;
      *         if parallel, the common perpendicular distance between the lines;
      *         if coincidental, this distance will be 0
      */
    def apply(line1: Line3D, line2: Line3D): Float = {
      val cross = Vector3.CrossProduct(line1.d, line2.d)
      if(cross != Vector3.Zero) {
        math.abs(
          Vector3.DotProduct(cross, Vector3(line1.x - line2.x, line1.y - line2.y, line1.z - line2.z))
        ) / Vector3.Magnitude(cross)
      } else {
        // lines are parallel or coincidental
        // construct a right triangle with one leg on line1 and the hypotenuse between the line's known points
        val hypotenuse = Vector3(line2.x - line1.x, line2.y - line1.y, line2.z - line1.z)
        val legOnLine1 = line1.d * Vector3.DotProduct(hypotenuse, line1.d)
        Vector3.Magnitude(hypotenuse - legOnLine1)
      }
    }

    def apply(seg1: Segment3D, seg2: Segment3D): Float = {
      //TODO make not as expensive as finding the plotted closest distance segment
      Segment(seg1, seg2) match {
        case Some(seg) => seg.length
        case None      => Float.MaxValue
      }
    }

    def apply(s1: Sphere, s2 : Sphere): Float = {
      math.max(0, Vector3.Magnitude(Vector3(s1.x - s2.x, s1.y - s2.y, s1.z - s2.z)) - s1.radius - s2.radius)
    }
  }

  object Segment {
    /**
      * na
      * @param c1 na
      * @param c2 na
      * @return a line segment that represents the closest distance between the circle's circumferences;
      *         `None`, if the circles have no distance between them (overlapping)
      */
    def apply(c1 : Circle, c2 : Circle): Option[Segment2D] = {
      val distance = Distance(c1, c2)
      if (distance > 0) {
        val c1x = c1.x
        val c1y = c1.y
        val v = Vector3.Unit(Vector3(c2.x - c1x, c2.y - c1y, 0f))
        val c1d = v * c1.radius
        val c2d = v * c2.radius
        Some(
          Segment2D(
            c1x + c1d.x, c1y + c1d.y,
            c1x + c2d.x, c1y + c2d.y,
          )
        )
      } else {
        None
      }
    }

    /**
      * na
      * @param line1 na
      * @param line2 na
      * @return a line segment representing the closest distance between the two not intersecting lines;
      *         in the case of parallel lines, one of infinite closest distances is plotted;
      *         `None`, if the lines intersect with each other
      */
    def apply(line1 : Line3D, line2 : Line3D): Option[Segment3D] = {
      val p1 = Vector3(line1.x, line1.y, line1.z)
      val p3 = Vector3(line2.x, line2.y, line2.z)
      val p13 = p1 - p3 // vector between point on first line and point on second line
      val p43 = line2.d
      val p21 = line1.d
      if (Vector3.MagnitudeSquared(p43) < Float.MinPositiveValue ||
          Vector3.MagnitudeSquared(p21) < Float.MinPositiveValue) {
        None
      } else {
        val d2121 = Vector3.MagnitudeSquared(p21)
        val d4343 = Vector3.MagnitudeSquared(p43)
        val d4321 = Vector3.DotProduct(p43, p21)
        val denom = d2121 * d4343 - d4321 * d4321 // n where d = (m/n) and a(x,y,z) + d * V<u,v,w> = b(x,y,z) for line1
        if (math.abs(denom) < Float.MinPositiveValue) {
          // without a denominator, we have no cross product solution
          val p13u = Vector3.Unit(p13)
          if (p21 == p13u || p21 == Vector3.neg(p13u)) { //coincidental lines overlap / intersect
            None
          } else { //parallel lines
            val connecting = Vector3(line2.x - line1.x, line2.y - line1.y, line2.z - line1.z)
            val legOnLine1 = line1.d * Vector3.DotProduct(connecting, line1.d)
            val v = connecting - legOnLine1
            Some(Segment3D(
              line1.x, line1.y, line1.z,
              line1.x + v.x, line1.y + v.y, line1.z + v.z
            ))
          }
        } else {
          val d1343 = Vector3.DotProduct(p13, p43)
          val numer = d1343 * d4321 -d4343 * Vector3.DotProduct(p13, p21) // m where d = (m/n) and ..., etc.
          val mua = numer / denom
          val mub = (d1343 + d4321 * mua) / d4343
          Some(Segment3D(
            p1.x + mua * p21.x,
            p1.y + mua * p21.y,
            p1.z + mua * p21.z,
            p3.x + mub * p43.x,
            p3.y + mub * p43.y,
            p3.z + mub * p43.z
          ))
        }
      }
    }

    def apply(line1 : Segment3D, line2 : Segment3D): Option[Segment3D] = {
      val uline1 =  Vector3.Unit(line1.d)
      val uline2 =  Vector3.Unit(line2.d)
      apply(Line3D(line1.ax, line1.ay, line1.az, uline1), Line3D(line2.ax, line2.ay, line2.az, uline2)) match {
        case Some(seg: Segment3D) => // common skew lines and parallel lines
          val sega = Vector3(seg.ax, seg.ay, seg.az)
          val p1 = Vector3(line1.ax, line1.ay, line1.az)
          val d1 = sega - p1
          val out1 = if (!Geometry.equalVectors(Vector3.Unit(d1), uline1)) { //clamp seg.a(xyz) to segment line1's bounds
            p1
          } else if (Vector3.MagnitudeSquared(d1) > Vector3.MagnitudeSquared(line1.d)) {
            Vector3(line1.bx, line1.by, line1.bz)
          } else {
            sega
          }
          val segb = Vector3(seg.bx, seg.by, seg.bz)
          val p2 = Vector3(line2.ax, line2.ay, line2.az)
          val d2 = segb - p2
          val out2 = if (!Geometry.equalVectors(Vector3.Unit(d2), uline2)) { //clamp seg.b(xyz) to segment line2's bounds
            p2
          } else if (Vector3.MagnitudeSquared(d2) > Vector3.MagnitudeSquared(line2.d)) {
            Vector3(line2.bx, line2.by, line2.bz)
          } else {
            segb
          }
          Some(Segment3D(
            out1.x, out1.y, out1.z,
            out2.x, out2.y, out2.z
          ))
        case None =>
          val connectingU = Vector3.Unit(Vector3(line2.ax - line1.ax, line2.ay - line1.ay, line2.az - line1.az))
          if (uline1 == connectingU || uline1 == Vector3.neg(connectingU)) { // coincidental line segments
            val line1a = Vector3(line1.ax, line1.ay, line1.az)
            val line1b = Vector3(line1.bx, line1.by, line1.bz)
            val line2a = Vector3(line2.ax, line2.ay, line2.az)
            val line2b = Vector3(line2.bx, line2.by, line2.bz)
            if (Vector3.Unit(line2a - line1a) != Vector3.Unit(line2b - line1a) ||
                Vector3.Unit(line2a - line1b) != Vector3.Unit(line2b - line1b) ||
                Vector3.Unit(line1a - line2a) != Vector3.Unit(line1b - line2a) ||
                Vector3.Unit(line1a - line2b) != Vector3.Unit(line1b - line2b)) {
              Some(Segment3D(
                line1.ax, line1.ay, line1a.z,
                line1.ax, line1.ay, line1a.z
              )) // overlap regions
            }
            else {
              val segs = List((line1a, line2a), (line1a, line2b), (line2a, line1b))
              val (a, b) = segs({
                //val dist = segs.map { case (_a, _b) => Vector3.DistanceSquared(_a, _b) }
                //dist.indexOf(dist.min)
                var index = 0
                var minDist = Vector3.DistanceSquared(segs.head._1, segs.head._2)
                (1 to 2).foreach { i =>
                  val dist = Vector3.DistanceSquared(segs(i)._1, segs(i)._2)
                  if (minDist < dist) {
                    index = i
                    minDist = dist
                  }
                }
                index
              })
              Some(Segment3D(a.x, a.y, a.z, b.x, b.y, b.z)) // connecting across the smallest gap
            }
          } else {
            None
          }
      }
    }

    /**
      * na
      * @param s1 na
      * @param s2 na
      * @return a line segment that represents the closest distance between the sphere's surface areas;
      *         `None`, if the spheres have no distance between them (overlapping)
      */
    def apply(s1 : Sphere, s2 : Sphere): Option[Segment3D] = {
      val distance = Distance(s1, s2)
      if (distance > 0) {
        val s1x = s1.x
        val s1y = s1.y
        val s1z = s1.z
        val v = Vector3.Unit(Vector3(s2.x - s1x, s2.y - s1y, s2.z - s1z))
        val s1d = v * s1.radius
        val s2d = v * (s1.radius + distance)
        Some(Segment3D(s1x + s1d.x, s1y + s1d.y, s1y + s1d.y, s1x + s2d.x, s1y + s2d.y, s1y + s2d.y))
      } else {
        None
      }
    }

    def apply(line : Line3D, sphere : Sphere): Option[Segment3D] = {
      val sphereAsPoint = Vector3(sphere.x, sphere.y, sphere.z)
      val lineAsPoint = Vector3(line.x, line.y, line.z)
      val direct = sphereAsPoint - lineAsPoint
      val projectionOfDirect = line.d * Vector3.DotProduct(direct, line.d)
      val heightFromProjection = projectionOfDirect - direct
      val heightFromProjectionDist = Vector3.Magnitude(heightFromProjection)
      if (heightFromProjectionDist <= sphere.radius) { //intersection
        None
      } else {
        val pointOnLine = lineAsPoint + projectionOfDirect
        val pointOnSphere = pointOnLine +
                            Vector3.Unit(heightFromProjection) * (heightFromProjectionDist - sphere.radius)
        Some(Segment3D(
          pointOnLine.x, pointOnLine.y, pointOnLine.z,
          pointOnSphere.x, pointOnSphere.y, pointOnSphere.z
        ))
      }
    }
  }
}
