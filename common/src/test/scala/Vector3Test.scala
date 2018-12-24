// Copyright (c) 2017 PSForever
import org.specs2.mutable._
import net.psforever.types.Vector3

class Vector3Test extends Specification {
  val vec = Vector3(1.3f, -2.6f, 3.9f)

  "Vector3" should {
    "construct" in {
      vec.x mustEqual 1.3f
      vec.y mustEqual -2.6f
      vec.z mustEqual 3.9f
    }

    "isolate x,y components" in {
      vec.xy mustEqual Vector3(1.3f, -2.6f, 0)
    }

    "promote float values into a specific z-format" in {
      Vector3.z(3.9f) mustEqual Vector3(0, 0, 3.9f)
    }

    "calculate magnitude (like a vector) 1" in {
      val obj = Vector3(2.0f, 0.0f, 0.0f)
      Vector3.Magnitude(obj) mustEqual 2.0f
    }

    "calculate magnitude (like a vector) 2" in {
      val obj = Vector3(3.0f, 4.0f, 0.0f)
      Vector3.Magnitude(obj) mustEqual 5.0f
    }

    "calculate magnitude (like a vector) 3" in {
      Vector3.Magnitude(vec) mustEqual 4.864155f
    }

    "calculate square magnitude (like a vector)" in {
      Vector3.MagnitudeSquared(vec) mustEqual 23.66f
    }

    "calculate distance 1" in {
      val obj1 = Vector3(0.0f, 0.0f, 0.0f)
      val obj2 = Vector3(2.0f, 0.0f, 0.0f)
      Vector3.Distance(obj1, obj2) mustEqual 2.0f
    }

    "calculate distance 2" in {
      val obj1 = Vector3(0.0f, 0.0f, 0.0f)
      val obj2 = Vector3(2.0f, 0.0f, 0.0f)
      Vector3.Distance(obj1, obj2) mustEqual Vector3.Magnitude(obj2)
    }

    "calculate distance 3" in {
      val obj1 = Vector3(3.0f, 4.0f, 5.0f)
      val obj2 = Vector3(3.0f, 4.0f, 5.0f)
      Vector3.Distance(obj1, obj2) mustEqual 0f
    }

    "perform addition" in {
      val obj1 = Vector3(3.0f, 4.0f, 5.0f)
      val obj2 = Vector3(3.0f, 4.0f, 5.0f)
      obj1 + obj2 mustEqual Vector3(6f, 8f, 10f)
    }

    "perform subtraction" in {
      val obj1 = Vector3(3.0f, 4.0f, 5.0f)
      val obj2 = Vector3(3.0f, 4.0f, 5.0f)
      obj1 - obj2 mustEqual Vector3(0f, 0f, 0f)
    }

    "multiply by a scalar" in {
      vec * 3f mustEqual Vector3(3.8999999f, -7.7999997f, 11.700001f)
    }

    "separate into x-component and y-component only" in {
      val obj = Vector3(1.1f, 2.2f, 3.3f)
      obj.xy mustEqual Vector3(1.1f, 2.2f, 0f)
    }

    "calculate the unit vector (zero)" in {
      Vector3.Unit(Vector3.Zero) mustEqual Vector3(0,0,0)
    }

    "calculate the unit vector (normal)" in {
      import Vector3._
      val one_root_two : Float = (1/math.sqrt(2)).toFloat
      val one_root_three : Float = (1/math.sqrt(3)).toFloat
      val ulp : Float = math.ulp(1) //measure of insignificance

      Unit(Vector3(1, 0, 0)) mustEqual Vector3(1, 0, 0)
      1 - Magnitude(Vector3(1, 0, 0)) < ulp mustEqual true

      Unit(Vector3(1, 1, 0)) mustEqual Vector3(one_root_two, one_root_two, 0)
      1 - Magnitude(Vector3(one_root_two, one_root_two, 0)) < ulp mustEqual true

      Unit(Vector3(1, 1, 1)) mustEqual Vector3(one_root_three, one_root_three, one_root_three)
      1 - Magnitude(Vector3(one_root_three, one_root_three, one_root_three)) < ulp mustEqual true
    }

    "calculate the dot product (magnitude-squared)" in {
      Vector3.DotProduct(vec, vec) mustEqual Vector3.MagnitudeSquared(vec)
    }

    "calculate the dot product (two vectors)" in {
      Vector3.DotProduct(vec, Vector3(3.4f, -5.6f, 7.8f)) mustEqual 49.4f
    }

    "calculate the dot product (zero vector)" in {
      Vector3.DotProduct(vec, Vector3.Zero) mustEqual 0f
    }

    "calculate the cross product (identity)" in {
      val Vx : Vector3 = Vector3(1, 0, 0)
      val Vy : Vector3 = Vector3(0, 1, 0)
      val Vz : Vector3 = Vector3(0, 0, 1)

      Vector3.CrossProduct(Vx, Vy) mustEqual Vz
      Vector3.CrossProduct(Vy, Vz) mustEqual Vx
      Vector3.CrossProduct(Vz, Vx) mustEqual Vy

      Vector3.CrossProduct(Vy, Vx) mustEqual Vector3(0, 0, -1)
      Vector3.CrossProduct(Vz, Vy) mustEqual Vector3(-1, 0, 0)
      Vector3.CrossProduct(Vx, Vz) mustEqual Vector3(0, -1, 0)
    }

    "calculate the cross product (full)" in {
      val A : Vector3 = Vector3(2, 1, -1)
      val B : Vector3 = Vector3(-3, 4, 1)

      Vector3.CrossProduct(A, B) mustEqual Vector3(5, 1, 11)
      Vector3.CrossProduct(B, A) mustEqual Vector3(-5, -1, -11)
    }

    "find a perpendicular vector with cross product" in {
      val A : Vector3 = Vector3(2, 1, -1)
      val B : Vector3 = Vector3(-3, 4, 1)
      val C : Vector3 = Vector3.CrossProduct(A, B)

      Vector3.DotProduct(A, C) mustEqual 0
      Vector3.DotProduct(B, C) mustEqual 0
    }

    "calculate the scalar projection (perpendicular vectors)" in {
      val Vx : Vector3 = Vector3(1, 0, 0)
      val Vy : Vector3 = Vector3(0, 1, 0)

      Vector3.ScalarProjection(Vx, Vy) mustEqual 0
    }

    "calculate the scalar projection (parallel vectors)" in {
      val A : Vector3 = Vector3(2, 0, 0)
      val B : Vector3 = Vector3(10, 0, 0)

      Vector3.ScalarProjection(A, B) mustEqual 2
      Vector3.ScalarProjection(B, A) mustEqual 10
    }

    "calculate the scalar projection (antiparallel vectors)" in {
      val A : Vector3 = Vector3(2, 0, 0)
      val B : Vector3 = Vector3(-10, 0, 0)

      Vector3.ScalarProjection(A, B) mustEqual -2
      Vector3.ScalarProjection(B, A) mustEqual -10
    }

    "calculate the scalar projection (normal)" in {
      val A : Vector3 = Vector3(2, 1, -1)
      val B : Vector3 = Vector3(3, 4, 1)

      Vector3.ScalarProjection(A, B) mustEqual 1.7650452f
      Vector3.ScalarProjection(B, A) mustEqual 3.6742344f
    }

    "calculate the vector projection (perpendicular vectors)" in {
      val Vx : Vector3 = Vector3(1, 0, 0)
      val Vy : Vector3 = Vector3(0, 1, 0)

      Vector3.VectorProjection(Vx, Vy) mustEqual Vector3.Zero
    }

    "calculate the vector projection (parallel vectors)" in {
      val A : Vector3 = Vector3(2, 0, 0)
      val B : Vector3 = Vector3(10, 0, 0)

      Vector3.VectorProjection(A, B) mustEqual A
      Vector3.VectorProjection(B, A) mustEqual B
    }

    "calculate the vector projection (antiparallel vectors)" in {
      val A : Vector3 = Vector3(2, 0, 0)
      val B : Vector3 = Vector3(-10, 0, 0)

      Vector3.VectorProjection(A, B) mustEqual A
      Vector3.VectorProjection(B, A) mustEqual B
    }

    "calculate the vector projection (normal)" in {
      val A : Vector3 = Vector3(2, 1, -1)
      val B : Vector3 = Vector3(3, 4, 1)

      Vector3.VectorProjection(A, B) mustEqual Vector3(1.0384614f, 1.3846153f, 0.34615383f)
      Vector3.VectorProjection(B, A) mustEqual Vector3(2.9999998f, 1.4999999f, -1.4999999f)
    }

    "rotate positive x-axis-vector 90-degrees around the z-axis" in {
      val A : Vector3 = Vector3(1, 0, 0)
      A.Rz(0) mustEqual A
      A.Rz(90) mustEqual Vector3(0, 1, 0)
      A.Rz(180) mustEqual Vector3(-1, 0, 0)
      A.Rz(270) mustEqual Vector3(0, -1, 0)
      A.Rz(360) mustEqual A
    }

    "rotate positive y-axis-vector 90-degrees around the x-axis" in {
      val A : Vector3 = Vector3(0, 1, 0)
      A.Rx(0) mustEqual A
      A.Rx(90) mustEqual Vector3(0, 0, 1)
      A.Rx(180) mustEqual Vector3(0, -1, 0)
      A.Rx(270) mustEqual Vector3(0, 0, -1)
      A.Rx(360) mustEqual A
    }

    "rotate positive x-axis-vector 90-degrees around the y-axis" in {
      val A : Vector3 = Vector3(1, 0, 0)
      A.Ry(0) mustEqual A
      A.Ry(90) mustEqual Vector3(0, 0, -1)
      A.Ry(180) mustEqual Vector3(-1, 0, 0)
      A.Ry(270) mustEqual Vector3(0, 0, 1)
      A.Ry(360) mustEqual A
    }

    "compound rotation" in {
      val A : Vector3 = Vector3(1, 0, 0)
      A.Rz(90)
        .Rx(90)
        .Ry(90) mustEqual A
    }

    "45-degree rotation" in {
      val A : Vector3 = Vector3(1, 0, 0)
      A.Rz(45) mustEqual Vector3(0.70710677f, 0.70710677f, 0)
    }
  }
}
