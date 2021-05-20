// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import enumeratum.{Enum, EnumEntry}
import net.psforever.types.Vector3

sealed abstract class AxisAlignment(direction: Vector3) extends EnumEntry {
  def asVector3(v: Vector3): Vector3
}

sealed abstract class AxisAlignment2D(direction: Vector3) extends AxisAlignment(direction) {
  def asVector3(a: Float, b: Float): Vector3
}

sealed abstract class AxisAlignment3D extends AxisAlignment(Vector3.Zero) {
  def asVector3(a: Float, b: Float, c: Float): Vector3
}

object AxisAlignment extends Enum[AxisAlignment] {
  val values: IndexedSeq[AxisAlignment] = findValues

  case object XY extends AxisAlignment2D(Vector3(0,0,1)) {
    def asVector3(v: Vector3): Vector3 = v.xy

    def asVector3(a: Float, b: Float): Vector3 = Vector3(a,b,0)
  }
  case object YZ extends AxisAlignment2D(Vector3(1,0,0)) {
    def asVector3(v: Vector3): Vector3 = Vector3(0,v.y,v.z)

    def asVector3(a: Float, b: Float): Vector3 = Vector3(0,a,b)
  }
  case object XZ extends AxisAlignment2D(Vector3(0,1,0)) {
    def asVector3(v: Vector3): Vector3 = Vector3(v.x,0,v.z)

    def asVector3(a: Float, b: Float): Vector3 = Vector3(a,0,b)
  }
  case object Free extends AxisAlignment3D() {
    def asVector3(v: Vector3): Vector3 = v

    def asVector3(a: Float, b: Float, c: Float): Vector3 = Vector3(a,b,c)
  }
}
