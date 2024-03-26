// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.geometry.d2.Rectangle
import net.psforever.types.Vector3

/**
  * The coordinate representation of a feature of the game world that is not a formal game object,
  * usually terrain, but can be used to represent any bounded region.
  * Calling this "geometry" would be accurate yet still generous.
  */
trait EnvironmentCollision {
  /** in general, the highest point in this geometry */
  def altitude: Float

  /**
   * Is the test point "within" the bounds of the represented environment?
   * @param obj entity to test
   * @return `true`, if the point is sufficiently "deep";
   *        `false`, otherwise
   */
  def testInteraction(obj: PlanetSideGameObject): Boolean = testInteraction(obj, varDepth = 0)

  /**
    * Is the test point "within" the bounds of the represented environment?
    * @param obj entity to test
    * @param varDepth how far "into" the environment the point must be
    * @return `true`, if the point is sufficiently "deep";
    *        `false`, otherwise
    */
  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean

  def bounding: Rectangle
}

/**
  * A mathematical plane that is always perpendicular to world-up.
  * The modifier "deep" indicates that the valid area goes down from the altitude to the bottom of the world.
  * @param altitude the z-coordinate of the geometry (height)
  */
final case class DeepPlane(altitude: Float)
  extends EnvironmentCollision {
  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    obj.Position.z + varDepth < altitude
  }

  def bounding: Rectangle = {
    val max = Float.MaxValue * 0.25f
    val min = Float.MinValue * 0.25f
    Rectangle(max, max, min, min)
  }
}

/**
  * From above, a rectangular region that is always perpendicular to world-up
  * and whose sides align with the X-axis and Y-axis, respectively.
  * The modifier "deep" indicates that the valid area goes down from the altitude to the bottom of the world.
  * @param altitude the z-coordinate of the geometry (height)
  * @param north the y-coordinate of the greatest side
  * @param east the x-coordinate of the other greatest side
  * @param south the y-coordinate of the least side
  * @param west the x-coordinate of the other least side
  */
final case class DeepSquare(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends EnvironmentCollision {
  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    val pos = obj.Position
    pos.z + varDepth < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }

  def bounding: Rectangle = Rectangle(north, east, south, west)
}

/**
  * Similar to `DeepRectangle`,
  * from above, a rectangular region that is always perpendicular to world-up
  * and whose sides align with the X-axis and Y-axis, respectively.
  * The modifier "deep" indicates that the valid area goes down from the altitude to the bottom of the world.
  * It is never subject to variable intersection depth during testing.
  * @param altitude the z-coordinate of the geometry (height)
  * @param north the y-coordinate of the greatest side
  * @param east the x-coordinate of the other greatest side
  * @param south the y-coordinate of the least side
  * @param west the x-coordinate of the other least side
  */
final case class DeepSurface(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends EnvironmentCollision {
  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    val pos = obj.Position
    pos.z < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }

  def bounding: Rectangle = Rectangle(north, east, south, west)
}

/**
  * From above, a circular region that is always perpendicular to world-up.
  * The modifier "deep" indicates that the valid area goes down from the altitude to the bottom of the world.
  * @param center the center of the geometry (height)
  * @param radius how large the circle is
  */
final case class DeepCircularSurface(center: Vector3, radius: Float)
  extends EnvironmentCollision {
  def altitude: Float = center.z

  def bounding: Rectangle = Rectangle(center.y + radius, center.x + radius, center.y - radius, center.x - radius)

  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    val pos = obj.Position
    pos.z < center.z && Vector3.DistanceSquared(pos.xy, center.xy) < radius * radius
  }
}
