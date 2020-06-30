// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.Vector3

trait WorldEntity {
  def Position: Vector3

  def Position_=(vec: Vector3): Vector3

  def Orientation: Vector3

  def Orientation_=(vec: Vector3): Vector3

  def Velocity: Option[Vector3]

  def Velocity_=(vec: Option[Vector3]): Option[Vector3]

  def Velocity_=(vec: Vector3): Option[Vector3] = Velocity = Some(vec)

  /**
    * A velocity of non-zero is the same as moving.
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving: Boolean = WorldEntity.isMoving(Velocity)

  /**
    * This object is not considered moving unless it is moving at least as fast as a certain velocity.
    * @param test the velocity to test against
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving(test: Vector3): Boolean = WorldEntity.isMoving(Velocity, test)

  /**
    * This object is not considered moving unless it is moving at least as fast as a certain velocity.
    * @param test the (squared) velocity to test against
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving(test: Float): Boolean = WorldEntity.isMoving(Velocity, test)
}

object WorldEntity {

  /**
    * A velocity of non-zero is the same as moving.
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving(velocity: Option[Vector3]): Boolean = {
    velocity match {
      case None               => false
      case Some(Vector3.Zero) => false
      case Some(_)            => true
    }
  }

  /**
    * This object is not considered moving unless it is moving at least as fast as a certain velocity.
    * @param velocity the optional sample velocity
    * @param test the (squared) velocity to test against
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving(velocity: Option[Vector3], test: Vector3): Boolean =
    WorldEntity.isMoving(velocity, Vector3.MagnitudeSquared(test))

  /**
    * This object is not considered moving unless it is moving at least as fast as a certain velocity.
    * @param velocity the optional sample velocity
    * @param test the (squared) velocity to test against
    * @return `true`, if we are moving; `false`, otherwise
    */
  def isMoving(velocity: Option[Vector3], test: Float): Boolean = {
    velocity match {
      case None               => false
      case Some(Vector3.Zero) => false
      case Some(v)            => Vector3.MagnitudeSquared(v) >= test
    }
  }

  def toString(obj: WorldEntity): String = {
    s"pos=${obj.Position}, ori=${obj.Orientation}"
  }
}
