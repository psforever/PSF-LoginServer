// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.newcodecs._
import scodec.Codec
import scodec.codecs._

final case class Vector3(x : Float,
                         y : Float,
                         z : Float) {
  /**
    * Operator for vector addition, treating `Vector3` objects as actual mathematical vectors.
    * The application of this definition is "vector1 + vector2."
    * @param vec the other `Vector3` object
    * @return a new `Vector3` object with the summed values
    */
  def +(vec : Vector3) : Vector3 = {
    Vector3(x + vec.x, y + vec.y, z + vec.z)
  }

  /**
    * Operator for vector subtraction, treating `Vector3` objects as actual mathematical vectors.
    * The application of this definition is "vector1 - vector2."
    * @param vec the other `Vector3` object
    * @return a new `Vector3` object with the difference values
    */
  def -(vec : Vector3) : Vector3 = {
    Vector3(x - vec.x, y - vec.y, z - vec.z)
  }

  /**
    * Operator for vector scaling, treating `Vector3` objects as actual mathematical vectors.
    * The application of this overload is "vector * scalar" exclusively.
    * "scalar * vector" is invalid.
    * @param scalar the value to multiply this vector
    * @return a new `Vector3` object
    */
  def *(scalar : Float) : Vector3 = {
    Vector3(x*scalar, y*scalar, z*scalar)
  }

  /**
    * Operator for returning the ground-planar coordinates
    * and ignoring the perpendicular distance from the world floor.
    * The application of this definition is "vector.xy" or "vector xy."
    * @return a new `Vector3` object with only two of the components of the original
    */
  def xy : Vector3 = Vector3(x, y, 0)

  /**
    * Perform the x-axis rotation of this `Vector3` element where the angle of rotation is assumed in degrees.
    * For chaining rotations.
    * @see `Vector3.Rx`
    * @param ang a rotation angle
    * @return the rotated vector
    */
  def Rx(ang : Float) : Vector3 = Vector3.Rx(this, ang)
  /**
    * Perform the y-axis rotation of this `Vector3` element where the angle of rotation is assumed in degrees.
    * For chaining rotations.
    * @see `Vector3.Ry`
    * @param ang a rotation angle
    * @return the rotated vector
    */
  def Ry(ang : Float) : Vector3 = Vector3.Ry(this, ang)
  /**
    * Perform the z-axis rotation of this `Vector3` element where the angle of rotation is assumed in degrees.
    * For chaining rotations.
    * @see `Vector3.Rz`
    * @param ang a rotation angle
    * @return the rotated vector
    */
  def Rz(ang : Float) : Vector3 = Vector3.Rz(this, ang)
}

object Vector3 {
  final val Zero : Vector3 = Vector3(0f, 0f, 0f)

  private def closeToInsignificance(d : Float, epsilon : Float = 10f) : Float = {
    val ulp = math.ulp(epsilon)
    math.signum(d) match {
      case -1f =>
        val n = math.abs(d)
        val p = math.abs(n - n.toInt)
        if(p < ulp || d > ulp) d + p else d
      case _ =>
        val p = math.abs(d - d.toInt)
        if(p < ulp || d < ulp) d - p else d
    }
  }

  implicit val codec_pos : Codec[Vector3] = (
      ("x" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("y" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("z" | newcodecs.q_float(0.0, 1024.0, 16))
    ).as[Vector3]

  implicit val codec_vel : Codec[Vector3] = (
      ("x" | newcodecs.q_float(-256.0, 256.0, 14)) ::
      ("y" | newcodecs.q_float(-256.0, 256.0, 14)) ::
      ("z" | newcodecs.q_float(-256.0, 256.0, 14))
    ).as[Vector3]

  implicit val codec_float : Codec[Vector3] = (
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL)
    ).as[Vector3]

  /**
    * A common vector object that only concerns itself
    * with rotation around the world-up axis
    * or the "world up" coordinate direction.
    * @param value the third coordinate
    * @return a `Vector3` object
    */
  def z(value : Float) : Vector3 = Vector3(0, 0, value)

  /**
    * Calculate the actual distance between two points.
    * @param pos1 the first point
    * @param pos2 the second point
    * @return the distance
    */
  def Distance(pos1 : Vector3, pos2 : Vector3) : Float = {
    math.sqrt(DistanceSquared(pos1, pos2)).toFloat
  }

  /**
    * Calculate the squared distance between two points.
    * Though some time is saved care must be taken that any comparative distance is also squared.
    * @param pos1 the first point
    * @param pos2 the second point
    * @return the distance
    */
  def DistanceSquared(pos1 : Vector3, pos2 : Vector3) : Float = {
    val dvec : Vector3 = pos1 - pos2
    (dvec.x * dvec.x) + (dvec.y * dvec.y) + (dvec.z * dvec.z)
  }

  /**
    * Calculate the actual magnitude of a vector.
    * @param vec the vector
    * @return the magnitude
    */
  def Magnitude(vec : Vector3) : Float = {
    math.sqrt(MagnitudeSquared(vec)).toFloat
  }

  /**
    * Calculate the squared magnitude of a vector.
    * Though some time is saved care must be taken that any comparative magnitude is also squared.
    * @param vec the vector
    * @return the magnitude
    */
  def MagnitudeSquared(vec : Vector3) : Float = {
    val dx : Float = vec.x
    val dy : Float = vec.y
    val dz : Float = vec.z
    (dx * dx) + (dy * dy) + (dz * dz)
  }

  /**
    * Given a vector, find that's vector's unit vector.<br>
    * <br>
    * A unit vector is a vector in the direction of the original vector but with a magnitude of 1.
    * @param vec the original vector
    * @return the unit vector;
    *         if the original vector has no magnitude, a zero-vector is returned
    */
  def Unit(vec : Vector3) : Vector3 = {
    val mag : Float = Magnitude(vec)
    if(mag == 0) {
      Vector3.Zero
    }
    else {
      Vector3(vec.x / mag, vec.y / mag, vec.z / mag)
    }
  }

  /**
    * Given two vectors, find their dot product.<br>
    * <br>
    * The dot product is the sum of the products of the corresponding component parts of two vectors.
    * It is equal to the product of the Euclidean magnitude of the vectors and cosine of the angle between them.
    * If the dot product of two vectors of non-zero magnitude is 0, then the vectors are perpendicular to each other.
    * @param vec1 the first vector
    * @param vec2 the second vector
    * @return the dot product
    */
  def DotProduct(vec1 : Vector3, vec2 : Vector3) : Float = {
    vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z
  }

  /**
    * For two vectors, find a vector that is simultaneously parallel to both vectors.<br>
    * <br>
    * The magnitude of the cross product is equal to
    * the product of the magnitudes of both vectors
    * and the sine of the angle between them.
    * If the two original vectors are parallel or antiparallel, the cross product is a zero vector.
    * Due to handiness rules, two non-zero cross product vectors that are antiparallel to each other can be calculated.
    * @param vec1 the first vector
    * @param vec2 the second vector
    * @return the cross product
    */
  def CrossProduct(vec1 : Vector3, vec2 : Vector3) : Vector3 = {
    Vector3(
      vec1.y * vec2.z - vec2.y * vec1.z,
      vec2.x * vec1.z - vec1.x * vec2.z,
      vec1.x * vec2.y - vec2.x * vec1.y
    )
  }

  /**
    * Given two vectors, find the scalar value of the projection of one vector on the other.<br>
    * <br>
    * The value of the resulting scalar is the magnitude of the vector resulting from a vector projection of `vec1` onto `vec2`.
    * For perpendicular vectors, the scalar projection result will be the same as the dot product result - zero.
    * A positive value indicates a projected vector in the same direction as `vec2`;
    * a negative value indicates an antiparallel vector.
    * @see `VectorProjection`
    * @param vec1 the vector being projected
    * @param vec2 the vector projected onto
    * @return the magnitude of the resulting projected vector
    */
  def ScalarProjection(vec1 : Vector3, vec2 : Vector3) : Float = {
    val mag : Float = Magnitude(vec2)
    if(mag == 0f) {
      0f
    }
    else {
      DotProduct(vec1, vec2) / mag
    }
  }

  /**
    * Given two vectors, find the projection of one vector on the other.<br>
    * <br>
    * The vector projection of `vec1` on `vec2` produces a vector that is
    * the direction of (parallel to) `vec2`
    * with a magnitude equal to the product of `vec1` and the cosine of the angle between the two vectors.
    * @see `ScalarProjection`
    * @param vec1 the vector being projected
    * @param vec2 the vector projected onto
    * @return the resulting projected vector
    */
  def VectorProjection(vec1 : Vector3, vec2 : Vector3) : Vector3 = {
    Unit(vec2) * ScalarProjection(vec1, vec2)
  }

  /**
    * Perform the x-axis rotation of a `Vector3` element where the angle of rotation is assumed in degrees.
    * @see `Vector3.RxRadians(Vector3, Double)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in degrees
    * @return the rotated vector
    */
  def Rx(vec : Vector3, ang : Float) : Vector3 = Rx(vec, math.toRadians(ang))
  /**
    * Perform the x-axis rotation of a `Vector3` element where the angle of rotation is assumed in radians.
    * @see `Vector3.Rx(Vector3, Float)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in radians
    * @return the rotated vector
    */
  def Rx(vec : Vector3, ang : Double) : Vector3 = {
    val cos = math.cos(ang).toFloat
    val sin = math.sin(ang).toFloat
    val (x, y, z) = (vec.x, vec.y, vec.z)
    Vector3(
      x,
      closeToInsignificance(y * cos - z * sin),
      closeToInsignificance(y * sin + z * cos)
    )
  }

  /**
    * Perform the x-axis rotation of a `Vector3` element where the angle of rotation is assumed in degrees.
    * @see `Vector3.Ry(Vector3, Double)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in degrees
    * @return the rotated vector
    */
  def Ry(vec : Vector3, ang : Float) : Vector3 = Ry(vec, math.toRadians(ang))
  /**
    * Perform the y-axis rotation of a `Vector3` element where the angle of rotation is assumed in radians.
    * @see `Vector3.Ry(Vector3, Float)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in radians
    * @return the rotated vector
    */
  def Ry(vec : Vector3, ang : Double) : Vector3 = {
    val cos = math.cos(ang).toFloat
    val sin = math.sin(ang).toFloat
    val (x, y, z) = (vec.x, vec.y, vec.z)
    Vector3(
      closeToInsignificance(x * cos + z * sin),
      y,
      closeToInsignificance(z * cos - x * sin)
    )
  }

  /**
    * Perform the x-axis rotation of a `Vector3` element where the angle of rotation is assumed in degrees.
    * @see `Vector3.Rz(Vector3, Double)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in degrees
    * @return the rotated vector
    */
  def Rz(vec : Vector3, ang : Float) : Vector3 = Rz(vec, math.toRadians(ang))
  /**
    * Perform the z-axis rotation of a `Vector3` element where the angle of rotation is assumed in radians.
    * @see `Vector3.Rz(Vector3, Float)`
    * @param vec a mathematical vector representing direction
    * @param ang a rotation angle, in radians
    * @return the rotation vector
    */
  def Rz(vec : Vector3, ang : Double) : Vector3 = {
    val cos = math.cos(ang).toFloat
    val sin = math.sin(ang).toFloat
    val (x, y, z) = (vec.x, vec.y, vec.z)
    Vector3(
      closeToInsignificance(x * cos - y * sin),
      closeToInsignificance(x * sin - y * cos),
      z
    )
  }
}
