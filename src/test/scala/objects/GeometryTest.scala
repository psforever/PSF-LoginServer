// Copyright (c) 2020 PSForever
package objects

import net.psforever.objects.geometry.{Intersection, Line3D, Segment3D}
import net.psforever.types.Vector3
import org.specs2.mutable.Specification

class IntersectionTest extends Specification {
  "Line3D" should {
    "detect intersection on target point(s)" in {
      //these lines intersect at (0, 0, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3(1,0,0)),
        Line3D(0,0,0, Vector3(0,1,0))
      )
      result mustEqual true
    }

    "detect intersection on a target point" in {
      //these lines intersect at (0, 0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3(0,1,0)),
        Line3D(-1,0,0, Vector3(1,0,0))
      )
      result mustEqual true
    }

    "detect intersection in the middle(s)" in {
      //these lines intersect at (0.5f, 0.5f, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3.Unit(Vector3(1, 1, 0))),
        Line3D(1,0,0, Vector3(0,1,0))
      )
      result mustEqual true
    }

    "detect intersection in the middle " in {
      //these lines intersect at (0, 0.5, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3(1,0,0)),
        Line3D(0.5f,1,0, Vector3.Unit(Vector3(0.5f,-1,0)))
      )
      result mustEqual true
    }

    "detect intersection if the point of intersection would be before the start of the segments" in {
      //these lines would intersect at (0, 0, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(1,1,0, Vector3.Unit(Vector3(2, 2, 0))),
        Line3D(1,0,0, Vector3.Unit(Vector3(2,0,0)))
      )
      result mustEqual true
    }

    "detect intersection if the point of intersection would be after the end of the segments" in {
      //these lines would intersect at (2, 2, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3.Unit(Vector3(1,1,0))),
        Line3D(2,0,0, Vector3.Unit(Vector3(2,1,0)))
      )
      result mustEqual true
    }

    "not detect intersection if the line segments are parallel" in {
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3.Unit(Vector3(1,1,1))),
        Line3D(1,1,2, Vector3.Unit(Vector3(1,1,1)))
      )
      result mustEqual false
    }

    "detect overlap" in {
      //the sub-segment (1,0,0) to (2,0,0) is an overlap region shared between the two segments
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Line3D(0,0,0, Vector3.Unit(Vector3(2,0,0))),
        Line3D(1,0,0, Vector3.Unit(Vector3(3,0,0)))
      )
      result mustEqual true
    }

    "not detect intersection (generic skew)" in {
      //these segments will not intersect
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(-3,-8,7, Vector3.Unit(Vector3(-3,-9,8))),
        Segment3D(6,3,0, Vector3.Unit(Vector3(2,0,0)))
      )
      result mustEqual false
    }
  }

  "Segment3D" should {
    "detect intersection of the first point(s)" in {
      //these segments intersect at (0, 0, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 1,0,0),
        Segment3D(0,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection of the first point" in {
      //these segments intersect at (0, 0, 0); start of segment 1, middle of segment 2
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 0,2,0),
        Segment3D(-1,0,0, 1,0,0)
      )
      result mustEqual true
    }

    "detect intersection on the farther point(s)" in {
      //these segments intersect at (0, 1, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,1, 0,1,0),
        Segment3D(1,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection on the farther point" in {
      //these segments intersect at (1, 1, 0); end of segment 1, middle of segment 2
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(1,0,0, 1,1,0),
        Segment3D(2,0,0, 0,2,0)
      )
      result mustEqual true
    }

    "detect intersection in the middle(s)" in {
      //these segments intersect at (0.5f, 0.5f, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 1,1,0),
        Segment3D(1,0,0, 0,1,0)
      )
      result mustEqual true
    }

    "detect intersection in the middle " in {
      //these segments intersect at (0, 0.5, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 1,0,0),
        Segment3D(0.5f,1,0, 0.5f,-1,0)
      )
      result mustEqual true
    }

    "not detect intersection if the point of intersection would be before the start of the segments" in {
      //these segments will not intersect as segments; but, as lines, they would intersect at (0, 0, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(1,1,0, 2,2,0),
        Segment3D(1,0,0, 2,0,0)
      )
      result mustEqual false
    }

    "not detect intersection if the point of intersection would be after the end of the segments" in {
      //these segments will not intersect as segments; but, as lines, they would intersect at (2, 2, 0)
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 1,1,0),
        Segment3D(2,0,0, 2,1,0)
      )
      result mustEqual false
    }

    "not detect intersection if the line segments are parallel" in {
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 1,1,1),
        Segment3D(1,1,2, 2,2,3)
      )
      result mustEqual false
    }

    "detect overlap" in {
      //the sub-segment (1,0,0) to (2,0,0) is an overlap region shared between the two segments
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(0,0,0, 2,0,0),
        Segment3D(1,0,0, 3,0,0)
      )
      result mustEqual true
    }

    "not detect intersection (generic skew)" in {
      //these segments will not intersect
      val result = Intersection.Test(Vector3.Zero, Vector3.Zero,
        Segment3D(-3,-8,7, -3,-9,8),
        Segment3D(6,3,0, 2,0,0)
      )
      result mustEqual false
    }
  }
}

object GeometryTest {

}
