// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.Vector3

class SimpleWorldEntity extends WorldEntity {
  private var coords: Vector3      = Vector3.Zero
  private var orient: Vector3      = Vector3.Zero
  private var vel: Option[Vector3] = None

  def Position: Vector3 = coords

  def Position_=(vec: Vector3): Vector3 = {
    coords = SimpleWorldEntity.validatePositionEntry(vec)
    Position
  }

  def Orientation: Vector3 = orient

  def Orientation_=(vec: Vector3): Vector3 = {
    orient = SimpleWorldEntity.validateOrientationEntry(vec)
    Orientation
  }

  def Velocity: Option[Vector3] = vel

  def Velocity_=(vec: Option[Vector3]): Option[Vector3] = {
    vel = vec
    Velocity
  }

  override def toString: String = WorldEntity.toString(this)
}

object SimpleWorldEntity {
  def validatePositionEntry(vec: Vector3): Vector3 = vec

  def validateOrientationEntry(vec: Vector3): Vector3 = {
    val x = clampAngle(vec.x)
    val y = clampAngle(vec.y)
    val z = clampAngle(vec.z)
    Vector3(x, y, z)
  }

  def clampAngle(ang: Float): Float = {
    var ang2 = ang % 360f
    if (ang2 < 0f) {
      ang2 += 360f
    }
    ang2
  }
}
