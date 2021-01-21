// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

object Intersection {
  object Test {
    /**
      * Do these two lines intersect?
      * Lines in 2D space will always intersect unless they are parallel or antiparallel.
      * In that case, they can still "intersect" if the lines are coincidental.
      */
    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Line2D, line2 : Line2D): Boolean = {
      line1.d != line2.d || {
        //parallel or antiparallel?
        val u = Vector3.Unit(Vector3(line2.x - line1.x, line2.y - line1.y, 0))
        line1.d == u || line1.d == Vector3.neg(u)
      }
    }

    private def pointOnSegment(ax : Float, ay : Float, px : Float, py : Float, bx : Float, by : Float): Boolean = {
      px <= math.max(ax, bx) && px >= math.min(ax, bx) && py <= math.max(ay, by) && py >= math.min(ay, by)
    }

    object PointTripleOrientation extends Enumeration {
      val Colinear, Clockwise, Counterclockwise = Value
    }

    /**
      * Determine the orientation of the given three two-dimensional points.
      * Any triple has one of three orientations:
      * clockwise - the third point is to the right side of a line plotted by the first two points;
      * counterclockwise - the third point is to the left side of a line plotted by the first two points;
      * and, colinear - the third point is reachable along the line plotted by the first two points.
      * @param ax x-coordinate of the first point
      * @param ay y-coordinate of the first point
      * @param px x-coordinate of the second point
      * @param py y-coordinate of the second point
      * @param bx x-coordinate of the third point
      * @param by y-coordinate of the third point
      * @return the orientation value
      */
    private def orientationOfPoints(
                                     ax : Float, ay : Float,
                                     px : Float, py : Float,
                                     bx : Float, by : Float
                                   ): PointTripleOrientation.Value = {
      val out = (py - ay) * (bx - px) - (px - ax) * (by - py)
      if (out == 0) PointTripleOrientation.Colinear
      else if (out > 0) PointTripleOrientation.Clockwise
      else PointTripleOrientation.Counterclockwise
    }

    /**
      * Do these two line segments intersect?
      * Intersection of two two-dimensional line segments can be determined by the orientation of their endpoints.
      * If a test of multiple ordered triple points reveals that certain triples have different orientations,
      * then we can safely assume the intersection state of the segments.
      */
    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Segment2D, line2 : Segment2D): Boolean = {
      //setup
      val ln1ax = line1.ax + origin1.x
      val ln1ay = line1.ay + origin1.y
      val ln1bx = ln1ax + origin1.x
      val ln1by = ln1ay + origin1.y
      val ln2ax = line2.ax + origin2.x
      val ln2ay = line2.ay + origin2.y
      val ln2bx = ln2ax + origin2.x
      val ln2by = ln2ay + origin2.y
      val ln1_ln2a = orientationOfPoints(ln1ax, ln1ay, ln1bx, ln1by, ln2ax, ln2ay)
      val ln1_ln2b = orientationOfPoints(ln1ax, ln1ay, ln1bx, ln1by, ln2bx, ln2by)
      val ln2_ln1a = orientationOfPoints(ln2ax, ln2ay, ln2bx, ln2by, ln1ax, ln1ay)
      val ln2_ln1b = orientationOfPoints(ln2ax, ln2ay, ln2bx, ln2by, ln1bx, ln1by)
      //results
      import PointTripleOrientation._
      (ln1_ln2a != ln1_ln2b && ln2_ln1a != ln2_ln1b) ||
      (ln1_ln2a == Colinear && pointOnSegment(ln1ax, ln1ay, ln2ax, ln2ay, ln1bx, ln1by)) || // line2 A is on line1
      (ln1_ln2b == Colinear && pointOnSegment(ln1ax, ln1ay, ln2bx, ln2by, ln1bx, ln1by)) || // line2 B is on line1
      (ln2_ln1a == Colinear && pointOnSegment(ln2ax, ln2ay, ln1ax, ln1ay, ln2bx, ln2by)) || // line1 A is on line2
      (ln2_ln1b == Colinear && pointOnSegment(ln2ax, ln2ay, ln1bx, ln1by, ln2bx, ln2by))    // line1 B is on line2
    }

    /**
      * Do these two lines intersect?
      * Actual mathematically-sound intersection between lines and line segments in 3D-space is terribly uncommon.
      * Instead, check that the closest distance between two line segments is below a threshold value.
      */
    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Line3D, line2 : Line3D): Boolean = {
      apply(origin1, origin2, line1, line2, 0.15f)
    }
    def apply(origin1 : Vector3, origin2 : Vector3, line1 : Line3D, line2 : Line3D, threshold: Float): Boolean = {
      ClosestDistance.Between(origin1, origin2, line1, line2) < threshold
    }

    /**
      * Do these two line segments intersect?
      * Actual mathematically-sound intersection between lines and line segments in 3D-space is terribly uncommon.
      * Instead, check that the closest distance between two line segments is below a threshold value.
      */
    def apply(origin1 : Vector3, origin2 : Vector3, seg1 : Segment3D, seg2 : Segment3D): Boolean = {
      apply(origin1, origin2, seg1, seg2, 0.15f)
    }
    def apply(origin1 : Vector3, origin2 : Vector3, seg1 : Segment3D, seg2 : Segment3D, threshold: Float): Boolean = {
      ClosestDistance.Between(origin1, origin2, seg1, seg2) < threshold
    }
  }
}
