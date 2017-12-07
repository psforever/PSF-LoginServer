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

    "addition" in {
      val obj1 = Vector3(3.0f, 4.0f, 5.0f)
      val obj2 = Vector3(3.0f, 4.0f, 5.0f)
      obj1 + obj2 mustEqual Vector3(6f, 8f, 10f)
    }

    "subtraction" in {
      val obj1 = Vector3(3.0f, 4.0f, 5.0f)
      val obj2 = Vector3(3.0f, 4.0f, 5.0f)
      obj1 - obj2 mustEqual Vector3(0f, 0f, 0f)
    }

    "scalar" in {
      vec * 3f mustEqual Vector3(3.8999999f, -7.7999997f, 11.700001f)
    }
  }
}

