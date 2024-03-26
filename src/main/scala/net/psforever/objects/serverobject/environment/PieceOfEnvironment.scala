// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.types.{PlanetSideGUID, Vector3}

/**
  * The representation of a feature of the game world that is not a formal game object,
  * usually terrain, but can be used to represent any bounded region.
  */
trait PieceOfEnvironment
  extends BlockMapEntity {
  /** a general description of this environment */
  def attribute: EnvironmentTrait
  /** a special representation of the region that qualifies as "this environment" */
  def collision: EnvironmentCollision

  /**
    * Is the test point "within" the bounds of the represented environment?
    * @param obj entity to test
    * @param varDepth how far "into" the environment the point must be
    * @return `true`, if the point is sufficiently "deep";
    *        `false`, otherwise
    */
  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = collision.testInteraction(obj, varDepth)

  /**
    * Did the test point move into or leave the bounds of the represented environment since its previous test?
    * @param pos the test point
    * @param previousPos the previous test point which is being compared against
    * @param varDepth how far "into" the environment the point must be
    * @return `Some(true)`, if the point has become sufficiently "deep";
    *        `Some(false)`, if the point has left the sufficiently "deep" region;
    *        `None`, otherwise
    */
  def testStepIntoInteraction(pos: Vector3, obj: PlanetSideGameObject, previousPos: Vector3, varDepth: Float): Option[Boolean] =
    PieceOfEnvironment.testStepIntoInteraction(body = this, obj, pos, previousPos, varDepth)

  def Position: Vector3 = collision.bounding.center.asVector3 + Vector3.z(collision.altitude)

  def Position_=(vec : Vector3) : Vector3 = Position

  def Orientation: Vector3 = Vector3.Zero

  def Orientation_=(vec: Vector3): Vector3 = Vector3.Zero

  def Velocity: Option[Vector3] = None

  def Velocity_=(vec: Option[Vector3]): Option[Vector3] = None
}

object PieceOfEnvironment {
  /**
   * Did the test point move into or leave the bounds of the represented environment since its previous test?
   * @param body the environment
   * @param pos the test point
   * @param previousPos the previous test point which is being compared against
   * @param varDepth how far "into" the environment the point must be
   * @return `Some(true)`, if the point has become sufficiently "deep";
   *        `Some(false)`, if the point has left the sufficiently "deep" region;
   *        `None`, if the described points only exist outside of or only exists inside of the critical region
   */
  def testStepIntoInteraction(
                               body: PieceOfEnvironment,
                               obj: PlanetSideGameObject,
                               pos: Vector3,
                               previousPos: Vector3,
                               varDepth: Float
                             ): Option[Boolean] = {
    val originalPosition = obj.Position
    obj.Position = pos
    val isEncroaching = body.collision.testInteraction(obj, varDepth)
    obj.Position = previousPos
    val wasEncroaching = body.collision.testInteraction(obj, varDepth)
    obj.Position = originalPosition
    if (isEncroaching != wasEncroaching) {
      Some(isEncroaching)
    } else {
      None
    }
  }
}

/**
  * A planar environment that spans the whole of the game world
  * and starts at and below a certain altitude.
  * @param attribute of what the environment is composed
  * @param altitude how high the environment starts
  */
final case class SeaLevel(attribute: EnvironmentTrait, altitude: Float)
  extends PieceOfEnvironment {
  private val planar = DeepPlane(altitude)

  def collision : EnvironmentCollision = planar

  override def Position: Vector3 = Vector3.Zero
}

object SeaLevel {
  /**
    * An overloaded constructor that applies only to water.
    * @param altitude how high the environment starts
    * @return a `SeaLevel` `PieceOfEnvironment` object
    */
  def apply(altitude: Float): SeaLevel = SeaLevel(EnvironmentAttribute.Water, altitude)
}

/**
  * A limited environment that spans no specific region.
  * @param attribute of what the environment is composed
  * @param collision a special representation of the region that qualifies as "this environment"
  */
final case class Pool(attribute: EnvironmentTrait, collision: EnvironmentCollision)
  extends PieceOfEnvironment

object Pool {
  /**
    * An overloaded constructor that creates environment backed by a `DeepSquare`.
    * @param attribute of what the environment is composed
    * @param altitude the z-coordinate of the geometry (height)
    * @param north the y-coordinate of the greatest side
    * @param east the x-coordinate of the other greatest side
    * @param south the y-coordinate of the least side
    * @param west the x-coordinate of the other least side
    * @return a `Pool` `PieceOfEnvironment` object
    */
  def apply(attribute: EnvironmentTrait, altitude: Float, north: Float, east: Float, south: Float, west: Float): Pool =
    Pool(attribute, DeepSquare(altitude, north, east, south, west))
}

final case class GantryDenialField(
                                    obbasemesh: PlanetSideGUID,
                                    mountPoint: Int,
                                    collision: EnvironmentCollision
                                  ) extends PieceOfEnvironment {
  def attribute: EnvironmentTrait = EnvironmentAttribute.GantryDenialField
}

final case class GeneralMovementField(
                                       triggerAction: PlanetSideGameObject => Unit,
                                       collision: EnvironmentCollision
                                     ) extends PieceOfEnvironment {
  def attribute: EnvironmentTrait = EnvironmentAttribute.MovementFieldTrigger
}
