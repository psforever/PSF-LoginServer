// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import enumeratum.{Enum, EnumEntry}
import net.psforever.types.Vector3

/**
  * For geometric entities that exist only in a given cardinal direction, using a plane of reference,
  * the plane of reference that maps the values to a cordinate vector
  * @see `Vector3D`
  * @param direction the coordinate vector of "relative up"
  */
sealed abstract class AxisAlignment(direction: Vector3) extends EnumEntry {
  /**
    * Project one vector as a vector that can be represented in this coordinate axis.
    * @param v the original vector
    * @return the projected vector
    */
  def asVector3(v: Vector3): Vector3
}

/**
  * For geometric entities that exist in a two-dimensional context.
  * @param direction the coordinate vector of "relative up"
  */
sealed abstract class AxisAlignment2D(direction: Vector3) extends AxisAlignment(direction) {
  /**
    * Project two values as a vector that can be represented in this coordinate axis.
    * @param a the first value
    * @param b the second value
    * @return the projected vector
    */
  def asVector3(a: Float, b: Float): Vector3
}

/**
  * For geometric entities that exist in a three-dimensional context.
  * More ceremonial, than anything else.
  */
sealed abstract class AxisAlignment3D extends AxisAlignment(Vector3.Zero) {
  /**
    * Project three values as a vector that can be represented in this coordinate axis.
    * @param a the first value
    * @param b the second value
    * @param c the third value
    * @return the projected vector
    */
  def asVector3(a: Float, b: Float, c: Float): Vector3
}

object AxisAlignment extends Enum[AxisAlignment] {
  val values: IndexedSeq[AxisAlignment] = findValues

  /**
    * Geometric entities in the XY-axis.
    * Coordinates are x- and y-; up is the z-axis.
    */
  case object XY extends AxisAlignment2D(Vector3(0,0,1)) {
    def asVector3(v: Vector3): Vector3 = v.xy

    def asVector3(a: Float, b: Float): Vector3 = Vector3(a,b,0)
  }
  /**
    * Geometric entities in the YZ-axis.
    * Coordinates are y- and z-; up is the x-axis.
    */
  case object YZ extends AxisAlignment2D(Vector3(1,0,0)) {
    def asVector3(v: Vector3): Vector3 = Vector3(0,v.y,v.z)

    def asVector3(a: Float, b: Float): Vector3 = Vector3(0,a,b)
  }
  /**
    * Geometric entities in the XZ-axis.
    * Coordinates are x- and z-; up is the y-axis.
    */
  case object XZ extends AxisAlignment2D(Vector3(0,1,0)) {
    def asVector3(v: Vector3): Vector3 = Vector3(v.x,0,v.z)

    def asVector3(a: Float, b: Float): Vector3 = Vector3(a,0,b)
  }
  /**
    * For geometric entities that exist in a three-dimensional context.
    * More ceremonial, than anything else.
    */
  case object Free extends AxisAlignment3D() {
    def asVector3(v: Vector3): Vector3 = v

    def asVector3(a: Float, b: Float, c: Float): Vector3 = Vector3(a,b,c)
  }
}
