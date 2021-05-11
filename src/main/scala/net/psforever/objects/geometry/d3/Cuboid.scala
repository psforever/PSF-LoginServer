// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry.d3

import net.psforever.objects.geometry.Geometry
import net.psforever.types.Vector3

/**
  * Untested geometry.
  * @param p na
  * @param relativeForward na
  * @param relativeUp na
  * @param length na
  * @param width na
  * @param height na
  */
final case class Cuboid(
                         p: Point3D,
                         relativeForward: Vector3,
                         relativeUp: Vector3,
                         length: Float,
                         width: Float,
                         height: Float,
                       ) extends VolumetricGeometry {
  def center: Point3D = Point3D(p.asVector3 + relativeUp * height * 0.5f)

  override def pointOnOutside(v: Vector3): Point3D = {
    import net.psforever.types.Vector3.{CrossProduct, DotProduct, neg}
    val height2 = height * 0.5f
    val relativeSide = CrossProduct(relativeForward, relativeUp)
    //val forwardVector = relativeForward * length
    //val sideVector = relativeSide * width
    //val upVector = relativeUp * height2
    val closestVector: Vector3 = Seq(
      relativeForward, relativeSide, relativeUp,
      neg(relativeForward), neg(relativeSide), neg(relativeUp)
    ).maxBy { dir => DotProduct(dir, v) }
    def dz(): Float = {
      if (Geometry.closeToInsignificance(v.z) != 0) {
        closestVector.z / v.z
      } else {
        0f
      }
    }
    def dy(): Float = {
      if (Geometry.closeToInsignificance(v.y) != 0) {
        val fyfactor = closestVector.y / v.y
        if (v.z * fyfactor <= height2) {
          fyfactor
        } else {
          dz()
        }
      } else {
        dz()
      }
    }

    val scaleFactor: Float = {
      if (Geometry.closeToInsignificance(v.x) != 0) {
        val fxfactor = closestVector.x / v.x
        if (v.y * fxfactor <= length) {
          if (v.z * fxfactor <= height2) {
            fxfactor
          } else {
            dy()
          }
        } else {
          dy()
        }
      } else {
        dy()
      }
    }
    Point3D(center.asVector3 + (v * scaleFactor))
  }
}
