// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

object ClosestDistance {
  object Between {
    def apply(origin1 : Vector3, origin2 : Vector3, point : Vector3, seg : Segment2D) : Float = {
      val segdx = seg.bx - seg.ax
      val segdy = seg.by - seg.ay
      ((point.x + origin1.x - seg.ax + origin2.x) * segdx + (point.y + origin1.y - seg.ay + origin2.y) * segdy) /
      Vector3.MagnitudeSquared(Vector3(segdx, segdy, 0))
    }

    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Line2D, line2 : Line2D) : Float = {
      if (Intersection.Test(origin1, origin2, line1, line2)) { //intersecting lines
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

    def apply(origin1: Vector3, origin2: Vector3, seg1: Segment2D, seg2: Segment2D): Float = {
      if (Intersection.Test(origin1, origin2, seg1, seg2)) { //intersecting line segments
        0f
      } else {
        val v1a = Vector3(seg1.ax, seg1.ay, 0)
        val v2a = Vector3(seg2.ax, seg2.ay, 0)
        val v1b = Vector3(seg1.bx, seg1.by, 0)
        val v2b = Vector3(seg2.bx, seg2.by, 0)
        math.min(
          apply(origin1, origin2, v1a, seg2),
          math.min(
            apply(origin1, origin2, v1b, seg2),
            math.min(
              apply(origin1, origin2, v2a, seg1),
              apply(origin1, origin2, v2b, seg1)
            )
          )
        )
      }
    }

    def apply(origin1: Vector3, origin2: Vector3, line1: Line3D, line2: Line3D): Float = {
      val cross = Vector3.CrossProduct(line1.d, line2.d)
      if(cross != Vector3.Zero) {
        math.abs(
          Vector3.DotProduct(cross, Vector3(line1.x - line2.x, line1.y - line2.y, line1.z - line2.z))
        ) / Vector3.Magnitude(cross)
      } else {
        //lines are parallel
        Vector3.Magnitude(
          Vector3.CrossProduct(
            line1.d,
            Vector3(line2.x - line1.x, line2.y - line1.y, line2.z - line1.z)
          )
        )
      }
    }

    def apply(origin1: Vector3, origin2: Vector3, seg1: Segment3D, seg2: Segment3D): Float = {
      //TODO make not as expensive as finding the plotted closest distance segment
      Plotted(origin1, origin2, seg1, seg2) match {
        case Some(seg) => seg.length
        case None      => Float.MaxValue
      }
    }
  }

  object Plotted {
    /**
      * na
      * This function can only operate normally if a perpendicular line segment between the two lines can be established,
      * this is, if the cross product of the two lines exists.
      * As such, for coincidental lines, a segment of zero length from the first line's point is produced.
      * @param origin1 na
      * @param origin2 na
      * @param line1 na
      * @param line2 na
      * @return na
      */
    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Line3D, line2 : Line3D): Option[Segment3D] = {
      val p1 = Vector3(line1.x, line1.y, line1.z)
      val p2 = p1 + line1.d
      val p3 = Vector3(line2.x, line2.y, line2.z)
      val p4 = p3 + line2.d
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
          val p13u = Vector3.Unit(p13)
          if (p21 == p13u || p21 == Vector3.neg(p13u)) { //coincidental lines
            // can not produce a valid cross product, but a coincidental line does produce an overlap
            Some(Segment3D(
              line1.x, line1.y, line1.z,
              line1.x, line1.y, line1.z
            ))
          } else {
            None
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

    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Segment3D, line2 : Segment3D): Option[Segment3D] = {
      val uline1 =  Vector3.Unit(line1.d)
      val uline2 =  Vector3.Unit(line2.d)
      apply(
        origin1,
        origin2,
        Line3D(line1.ax, line1.ay, line1.az, uline1),
        Line3D(line2.ax, line2.ay, line2.az, uline2)
      ) match {
        case out @ Some(seg: Segment3D)
          if seg.length == 0 && (uline1 == uline2 || uline1 == Vector3.neg(uline2)) => //coincidental lines
          out
        case Some(seg: Segment3D) => //segment of shortest distance when two segments treated as lines
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
          None
      }
    }
  }
}
