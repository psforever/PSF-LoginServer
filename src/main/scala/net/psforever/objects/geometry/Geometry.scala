// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

object Geometry {
  def equalFloats(value1: Float, value2: Float, off: Float = 0.001f): Boolean = {
    val diff = value1 - value2
    if (diff >= 0) diff <= off else diff > -off
  }

  def equalVectors(value1: Vector3, value2: Vector3, off: Float = 0.001f): Boolean = {
    equalFloats(value1.x, value2.x, off) &&
    equalFloats(value1.y, value2.y, off) &&
    equalFloats(value1.z, value2.z, off)
  }

  def closeToInsignificance(d: Float, epsilon: Float = 10f): Float = {
    val ulp = math.ulp(epsilon)
    math.signum(d) match {
      case -1f =>
        val n = math.abs(d)
        val p = math.abs(n - n.toInt)
        if (p < ulp || d > ulp) d + p else d
      case _ =>
        val p = math.abs(d - d.toInt)
        if (p < ulp || d < ulp) d - p else d
    }
  }
}
