// Copyright (c) 2020 PSForever
package objects

import net.psforever.objects.geometry._
import net.psforever.types.Vector3
import org.specs2.mutable.Specification

class IntersectionTest extends Specification {
  "Line2D" should {
    "detect intersection on target points(s)" in {
      //these lines intersect at (0, 0)
      val result = Intersection.Test(
        Line2D(0,0, 1,0),
        Line2D(0,0, 0,1)
      )
      result mustEqual true
    }

    "detect intersection on a target point" in {
      //these lines intersect at (0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(
        Line2D( 0,0, 0,1),
        Line2D(-1,0, 1,0)
      )
      result mustEqual true
    }

    "detect intersection anywhere else" in {
      //these lines intersect at (0.5f, 0.5f)
      val result = Intersection.Test(
        Line2D(0,0, 1,1),
        Line2D(1,0, 0,1)
      )
      result mustEqual true
    }

    "detect intersection anywhere else (2)" in {
      //these lines intersect at (0, 0.5)
      val result = Intersection.Test(
        Line2D(0,   0, 1,    0),
        Line2D(0.5f,1, 0.5f,-1)
      )
      result mustEqual true
    }

    "not detect intersection if the lines are parallel" in {
      val result = Intersection.Test(
        Line2D(0,0, 1,1),
        Line2D(1,0, 2,1)
      )
      result mustEqual false
    }

    "detect intersection if the lines overlap" in {
      //the lines are coincidental
      val result = Intersection.Test(
        Line2D(0,0, 1,1),
        Line2D(1,1, 2,2)
      )
      result mustEqual true
    }
  }

  "Segment2D" should {
    "detect intersection on target points(s)" in {
      //these line segments intersect at (0, 0)
      val result = Intersection.Test(
        Segment2D(0,0, 1,0),
        Segment2D(0,0, 0,1)
      )
      result mustEqual true
    }

    "detect intersection on a target point" in {
      //these line segments intersect at (0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(
        Segment2D( 0,0, 0,1),
        Segment2D(-1,0, 1,0)
      )
      result mustEqual true
    }

    "detect intersection anywhere else" in {
      //these line segments intersect at (0.5f, 0.5f)
      val result = Intersection.Test(
        Segment2D(0,0, 1,1),
        Segment2D(1,0, 0,1)
      )
      result mustEqual true
    }

    "detect intersection anywhere else (2)" in {
      //these line segments intersect at (0, 0.5)
      val result = Intersection.Test(
        Segment2D(0,   0, 1,    0),
        Segment2D(0.5f,1, 0.5f,-1)
      )
      result mustEqual true
    }

    "not detect intersection if the lines are parallel" in {
      val result = Intersection.Test(
        Segment2D(0,0, 1,1),
        Segment2D(1,0, 2,1)
      )
      result mustEqual false
    }

    "detect intersection if the lines overlap" in {
      //the lines are coincidental
      val result = Intersection.Test(
        Line2D(0,0, 1,1),
        Line2D(1,1, 2,2)
      )
      result mustEqual true
    }
  }

  "Circle" should {
    "intersect when overlapping (coincidental)" in {
      val result = Intersection.Test(
        Circle(0,0, 1),
        Circle(0,0, 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (engulfed)" in {
      val result = Intersection.Test(
        Circle(0,0, 2),
        Circle(1,0, 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (partial 1)" in {
      val result = Intersection.Test(
        Circle(0,0, 2),
        Circle(2,0, 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (partial 2)" in {
      val result = Intersection.Test(
        Circle(0,   0, 2),
        Circle(2.5f,0, 1)
      )
      result mustEqual true
    }

    "intersect when the circumferences are touching" in {
      val result = Intersection.Test(
        Circle(0,0, 2),
        Circle(3,0, 1)
      )
      result mustEqual true
    }

    "not intersect when not touching" in {
      val result = Intersection.Test(
        Circle(0,0, 2),
        Circle(4,0, 1)
      )
      result mustEqual false
    }
  }

  "Line3D" should {
    "detect intersection on target point(s)" in {
      //these lines intersect at (0, 0, 0)
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3(1,0,0)),
        Line3D(0,0,0, Vector3(0,1,0))
      )
      result mustEqual true
    }

    "detect intersection on a target point" in {
      //these lines intersect at (0, 0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3(0,1,0)),
        Line3D(-1,0,0, Vector3(1,0,0))
      )
      result mustEqual true
    }

    "detect intersection anywhere else" in {
      //these lines intersect at (0.5f, 0.5f, 0)
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3.Unit(Vector3(1, 1, 0))),
        Line3D(1,0,0, Vector3(0,1,0))
      )
      result mustEqual true
    }

    "detect intersection anywhere else (2)" in {
      //these lines intersect at (0, 0.5, 0)
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3(1,0,0)),
        Line3D(0.5f,1,0, Vector3.Unit(Vector3(0.5f,-1,0)))
      )
      result mustEqual true
    }

    "not detect intersection if the lines are parallel" in {
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3.Unit(Vector3(1,1,1))),
        Line3D(1,1,2, Vector3.Unit(Vector3(1,1,1)))
      )
      result mustEqual false
    }

    "detect intersection if the lines overlap" in {
      //the sub-segment (1,0,0) to (2,0,0) is an overlap region shared between the two segments
      val result = Intersection.Test(
        Line3D(0,0,0, Vector3.Unit(Vector3(2,0,0))),
        Line3D(1,0,0, Vector3.Unit(Vector3(3,0,0)))
      )
      result mustEqual true
    }

    "not detect intersection (generic skew)" in {
      //these segments will not intersect
      val result = Intersection.Test(
        Segment3D(-3,-8,7, Vector3.Unit(Vector3(-3,-9,8))),
        Segment3D(6,3,0, Vector3.Unit(Vector3(2,0,0)))
      )
      result mustEqual false
    }
  }

  "Segment3D" should {
    "detect intersection of the first point(s)" in {
      //these segments intersect at (0, 0, 0)
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,0,0),
        Segment3D(0,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection of the first point" in {
      //these segments intersect at (0, 0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(
        Segment3D(0,0,0, 0,2,0),
        Segment3D(-1,0,0, 1,0,0)
      )
      result mustEqual true
    }

    "detect intersection on the farther point(s)" in {
      //these segments intersect at (0, 1, 0)
      val result = Intersection.Test(
        Segment3D(0,0,1, 0,1,0),
        Segment3D(1,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection on the farther point" in {
      //these segments intersect at (1, 1, 0); end of segment 1, middle of segment 2
      val result = Intersection.Test(
        Segment3D(1,0,0, 1,1,0),
        Segment3D(2,0,0, 0,2,0)
      )
      result mustEqual true
    }

    "detect intersection in the middle(s)" in {
      //these segments intersect at (0.5f, 0.5f, 0)
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,1,0),
        Segment3D(1,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection in the middle " in {
      //these segments intersect at (0, 0.5, 0)
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,0,0),
        Segment3D(0.5f,1,0, 0.5f,-1,0)
      )
      result mustEqual true
    }

    "not detect intersection if the point of intersection would be before the start of the segments" in {
      //these segments will not intersect as segments; but, as lines, they would intersect at (0, 0, 0)
      val result = Intersection.Test(
        Segment3D(1,1,0, 2,2,0),
        Segment3D(1,0,0, 2,0,0)
      )
      result mustEqual false
    }

    "not detect intersection if the point of intersection would be after the end of the segments" in {
      //these segments will not intersect as segments; but, as lines, they would intersect at (2, 2, 0)
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,1,0),
        Segment3D(2,0,0, 2,1,0)
      )
      result mustEqual false
    }

    "not detect intersection if the line segments are parallel" in {
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,1,1),
        Segment3D(1,1,2, 2,2,3)
      )
      result mustEqual false
    }

    "detect intersection with overlapping" in {
      //the sub-segment (1,0,0) to (2,0,0) is an overlap region shared between the two segments
      val result = Intersection.Test(
        Segment3D(0,0,0, 2,0,0),
        Segment3D(1,0,0, 3,0,0)
      )
      result mustEqual true
    }

    "not detect intersection with coincidental, non-overlapping" in {
      //the sub-segment (1,0,0) to (2,0,0) is an overlap region shared between the two segments
      val result = Intersection.Test(
        Segment3D(0,0,0, 1,0,0),
        Segment3D(2,0,0, 3,0,0)
      )
      result mustEqual false
    }

    "not detect intersection (generic skew)" in {
      //these segments will not intersect
      val result = Intersection.Test(
        Segment3D(-3,-8,7, -3,-9,8),
        Segment3D(6,3,0, 2,0,0)
      )
      result mustEqual false
    }
  }

  "Sphere" should {
    "intersect when overlapping (coincidental)" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero, 1),
        Sphere(Vector3.Zero, 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (engulfed)" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero,   5),
        Sphere(Vector3(1,0,0), 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (partial 1)" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero,   2),
        Sphere(Vector3(2,0,0), 1)
      )
      result mustEqual true
    }

    "intersect when overlapping (partial 2)" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero,      2),
        Sphere(Vector3(2.5f,0,0), 1)
      )
      result mustEqual true
    }

    "intersect when the circumferences are touching" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero,   2),
        Sphere(Vector3(3,0,0), 1)
      )
      result mustEqual true
    }

    "not intersect when not touching" in {
      val result = Intersection.Test(
        Sphere(Vector3.Zero,   2),
        Sphere(Vector3(4,0,0), 1)
      )
      result mustEqual false
    }
  }

  "Cylinder" should {
    "detect intersection if overlapping" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 2),
        Cylinder(0, 0, 0, 1, 2)
      )
      result mustEqual true
    }

    "detect intersection if sides clip" in {
      val result = Intersection.Test(
        Cylinder(0,    0,    0, 1, 2),
        Cylinder(0.5f, 0.5f, 0, 1, 2)
      )
      result mustEqual true
    }

    "detect intersection if touching" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 2),
        Cylinder(1, 0, 0, 1, 2)
      )
      result mustEqual true
    }

    "detect intersection if stacked" in {
      val result = Intersection.Test(
        Cylinder(1, 0, 0, 1, 2),
        Cylinder(1, 0, 2, 1, 2)
      )
      result mustEqual true
    }

    "detect intersection if one is sunken into the other" in {
      val result = Intersection.Test(
        Cylinder(1, 0, 0, 1, 2),
        Cylinder(1, 0, 1, 1, 2)
      )
      result mustEqual true
    }

    "not detect intersection if not near each other" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 2),
        Cylinder(2, 2, 0, 1, 2)
      )
      result mustEqual false
    }

    "not detect intersection if one is too high / low" in {
      val result = Intersection.Test(
        Cylinder(1, 0, 0, 1, 2),
        Cylinder(1, 0, 5, 1, 2)
      )
      result mustEqual false
    }
  }

  "Cylinder and Sphere" should {
    "detect intersection if overlapping" in {
      val result = Intersection.Test(
        Cylinder(1, 0, 0, 1, 1),
        Sphere(1, 0, 2, 1)
      )
      result mustEqual true
    }

    "detect intersection if cylinder top touches sphere base" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(1, 0, 2, 1)
      )
      result mustEqual true
    }

    "detect intersection if cylinder base touches sphere top" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(-1, 0, -1, 1)
      )
      result mustEqual true
    }

    "detect intersection if cylinder edge touches sphere edge" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(2, 0, 0.5f, 1)
      )
      result mustEqual true
    }

    "detect intersection if on cylinder top rim" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(1.75f, 0, 1.25f, 1)
      )
      result mustEqual true
    }

    "detect intersection if on cylinder base rim" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(1.75f, 0, -0.5f, 1)
      )
      result mustEqual true
    }

    "not detect intersection if too far above" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(0, 0, 3, 1)
      )
      result mustEqual false
    }

    "not detect intersection if too far below" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(0, 0, -3, 1)
      )
      result mustEqual false
    }

    "not detect intersection if too far out (sideways)" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(2, 2, 0, 1)
      )
      result mustEqual false
    }

    "not detect intersection if too far out (skew)" in {
      val result = Intersection.Test(
        Cylinder(0, 0, 0, 1, 1),
        Sphere(1.5f, 1.5f, 1.5f, 1)
      )
      result mustEqual false
    }
  }
}

object GeometryTest { }
