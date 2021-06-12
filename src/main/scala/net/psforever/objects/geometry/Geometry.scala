// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

/**
  * Calculation support for the geometric code.
  */
object Geometry {
  /**
    * Are two `Float` numbers equal enough to be considered equal?
    * @param value1 the first value
    * @param value2 the second value
    * @param off how far the number can be inequal from each other
    * @return `true`, if the two `Float` numbers are close enough to be considered equal;
    *        `false`, otherwise
    */
  def equalFloats(value1: Float, value2: Float, off: Float = 0.001f): Boolean = {
    val diff = value1 - value2
    if (diff >= 0) diff <= off else diff > -off
  }

  /**
    * Are two `Vector3` entities equal enough to be considered equal?
    * @see `equalFloats`
    * @param value1 the first coordinate triple
    * @param value2 the second coordinate triple
    * @param off how far any individual coordinate can be inequal from each other
    * @return `true`, if the two `Vector3` entities are close enough to be considered equal;
    *        `false`, otherwise
    */
  def equalVectors(value1: Vector3, value2: Vector3, off: Float = 0.001f): Boolean = {
    equalFloats(value1.x, value2.x, off) &&
    equalFloats(value1.y, value2.y, off) &&
    equalFloats(value1.z, value2.z, off)
  }

  /**
    * Are two `Vector3` entities equal enough to be considered equal?
    * @see `equalFloats`
    * @param value1 the first coordinate triple
    * @param value2 the second coordinate triple
    * @param off how far each individual coordinate can be inequal from the other
    * @return `true`, if the two `Vector3` entities are close enough to be considered equal;
    *        `false`, otherwise
    */
  def equalVectors(value1: Vector3, value2: Vector3, off: Vector3): Boolean = {
    equalFloats(value1.x, value2.x, off.x) &&
    equalFloats(value1.y, value2.y, off.y) &&
    equalFloats(value1.z, value2.z, off.z)
  }

  /**
    * Is the value close enough to be zero to be equivalently replaceable with zero?
    * @see `math.abs`
    * @see `math.signum`
    * @see `math.ulp`
    * @see `Vector3.closeToInsignificance`
    * @param d the original number
    * @param epsilon how far from zero the value is allowed to stray
    * @return the original number, or zero
    */
  def closeToInsignificance(d: Float, epsilon: Float = 10f): Float = {
    val ulp = math.ulp(epsilon)
    math.signum(d) match {
      case -1f =>
        val n = math.abs(d)
        val p = math.abs(n - n.toInt)
        if (p < ulp || d > ulp) 0f else d
      case _ =>
        val p = math.abs(d - d.toInt)
        if (p < ulp || d < ulp) 0f else d
    }
  }
}
