// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.newcodecs._
import scodec.Codec
import scodec.codecs._

final case class Vector3(x : Float,
                         y : Float,
                         z : Float) {
  /**
    * Operator override for vector addition, treating `Vector3` objects as actual mathematical vectors.
    * The application of this overload is "vector1 + vector2."
    * @param vec the other `Vector3` object
    * @return a new `Vector3` object with the summed values
    */
  def +(vec : Vector3) : Vector3 = {
    new Vector3(x + vec.x, y + vec.y, z + vec.z)
  }

  /**
    * Operator override for vector subtraction, treating `Vector3` objects as actual mathematical vectors.
    * The application of this overload is "vector1 - vector2."
    * @param vec the other `Vector3` object
    * @return a new `Vector3` object with the difference values
    */
  def -(vec : Vector3) : Vector3 = {
    new Vector3(x - vec.x, y - vec.y, z - vec.z)
  }

  /**
    * Operator override for vector scaling, treating `Vector3` objects as actual mathematical vectors.
    * The application of this overload is "vector * scalar" exclusively.
    * "scalar * vector" is invalid.
    * @param scalar the value to multiply this vector
    * @return a new `Vector3` object
    */
  def *(scalar : Float) : Vector3 = {
    new Vector3(x*scalar, y*scalar, z*scalar)
  }
}

object Vector3 {
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
}
