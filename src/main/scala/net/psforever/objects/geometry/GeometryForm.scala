// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.objects.ballistics.{PlayerSource, SourceEntry}
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player}
import net.psforever.types.ExoSuitType

object GeometryForm {
  /** this point can not be used for purposes of geometric representation */
  lazy val invalidPoint: Point3D     = Point3D(Float.MinValue, Float.MinValue, Float.MinValue)
  /** this circle can not be used for purposes of geometric representation */
  lazy val invalidCircle: Circle     = Circle(Point2D(invalidPoint.asVector3), 0)
  /** this cylinder can not be used for purposes of geometric representation */
  lazy val invalidCylinder: Cylinder = Cylinder(invalidCircle, Float.MinValue, 0)

  /**
    * The geometric representation is the entity's centroid.
    * @param o the entity
    * @return the representation
    */
  def representByPoint()(o: Any): Geometry3D = {
    o match {
      case p: PlanetSideGameObject => Point3D(p.Position)
      case s: SourceEntry          => Point3D(s.Position)
      case _                       => invalidPoint
    }
  }

  /**
    * The geometric representation is the a sphere around the entity's centroid.
    * @param radius how wide a hemisphere is
    * @param o the entity
    * @return the representation
    */
  def representBySphere(radius: Float)(o: Any): Geometry3D = {
    o match {
      case p: PlanetSideGameObject => Sphere(p.Position, radius)
      case s: SourceEntry          => Sphere(s.Position, radius)
      case _                       => Sphere(invalidPoint, radius)
    }
  }

  /**
    * The geometric representation is the a cylinder around the entity's base.
    * @param radius half the distance across
    * @param height how tall the cylinder is (the distance of the top to the base)
    * @param o the entity
    * @return the representation
    */
  def representByCylinder(radius: Float, height: Float)(o: Any): Geometry3D = {
    o match {
      case p: PlanetSideGameObject => Cylinder(Circle(p.Position.x, p.Position.y, radius), p.Position.z, height)
      case s: SourceEntry          => Cylinder(Circle(s.Position.x, s.Position.y, radius), s.Position.z, height)
      case _                       => invalidCylinder
    }
  }

  /**
    * The geometric representation is the a cylinder around the entity's base
    * if the target represents a player entity.
    * @param radius a measure of the player's bulk
    * @param o the entity
    * @return the representation
    */
  def representPlayerByCylinder(radius: Float)(o: Any): Geometry3D = {
    o match {
      case p: Player =>
        val radialOffset = if(p.ExoSuit == ExoSuitType.MAX) 0.25f else 0f
        Cylinder(
          Circle(p.Position.x, p.Position.y, radius + radialOffset),
          p.Position.z,
          GlobalDefinitions.MaxDepth(p)
        )
      case p: PlayerSource =>
        val radialOffset = if(p.ExoSuit == ExoSuitType.MAX) 0.25f else 0f
        Cylinder(
          Circle(p.Position.x, p.Position.y, radius + radialOffset),
          p.Position.z,
          GlobalDefinitions.avatar.MaxDepth
        )
      case _ =>
        invalidCylinder
    }
  }

  /**
    * The geometric representation is the a cylinder around the entity's base
    * as if the target is displaced from the ground at an expected (fixed?) distance.
    * @param radius half the distance across
    * @param height how tall the cylinder is (the distance of the top to the base)
    * @param hoversAt how far off the base coordinates the actual cylinder begins
    * @param o the entity
    * @return the representation
    */
  def representHoveringEntityByCylinder(radius: Float, height: Float, hoversAt: Float)(o: Any): Geometry3D = {
    o match {
      case p: PlanetSideGameObject =>
        Cylinder(
          Circle(p.Position.x, p.Position.y, radius),
          p.Position.z + hoversAt,
          height
        )
      case s: SourceEntry =>
        Cylinder(
          Circle(s.Position.x, s.Position.y, radius),
          s.Position.z + hoversAt,
          height
        )
      case _ =>
        invalidCylinder
    }
  }
}
