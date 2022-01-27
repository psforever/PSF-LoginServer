// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.objects.ballistics.{PlayerSource, Projectile, SourceEntry}
import net.psforever.objects.geometry.d3._
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player}
import net.psforever.types.{ExoSuitType, Vector3}

object GeometryForm {
  /** this point can not be used for purposes of geometric representation */
  lazy val invalidPoint: d3.Point     = d3.Point(Float.MinValue, Float.MinValue, Float.MinValue)
  /** this cylinder can not be used for purposes of geometric representation */
  lazy val invalidCylinder: Cylinder = Cylinder(invalidPoint.asVector3, Vector3.Zero, Float.MinValue, 0)

  /**
    * The geometric representation is the entity's centroid.
    * @param o the entity
    * @return the representation
    */
  def representByPoint()(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject => Point(p.Position)
      case s: SourceEntry          => Point(s.Position)
      case _                       => invalidPoint
    }
  }

  /**
    * The geometric representation is a sphere using a position as the entity's centroid
    * and all points exist a distance from that point.
    * @param radius how wide a quarter-sphere is
    * @param o the entity
    * @return the representation
    */
  def representBySphere(radius: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject =>
        Sphere(p.Position, radius)
      case s: SourceEntry          =>
        Sphere(s.Position, radius)
      case _                       =>
        Sphere(invalidPoint, radius)
    }
  }

  /**
    * The geometric representation is a sphere using a position as the entity's base (foot position)
    * and the centroid is located just above it a fixed distance.
    * All points exist a distance from that centroid.
    * @param radius how wide a quarter-sphere is
    * @param o the entity
    * @return the representation
    */
  def representBySphereOnBase(radius: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject =>
        Sphere(p.Position + Vector3.z(radius), radius)
      case s: SourceEntry          =>
        Sphere(s.Position + Vector3.z(radius), radius)
      case _                       =>
        Sphere(invalidPoint, radius)
    }
  }

  /**
    * The geometric representation is a sphere around the entity's centroid
    * positioned following the axis of rotation (the entity's base).
    * @param radius how wide a hemisphere is
    * @param o the entity
    * @return the representation
    */
  def representByRaisedSphere(radius: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject =>
        Sphere(p.Position + Vector3.relativeUp(p.Orientation) * radius, radius)
      case s: SourceEntry          =>
        Sphere(s.Position + Vector3.relativeUp(s.Orientation) * radius, radius)
      case _                       =>
        Sphere(invalidPoint, radius)
    }
  }

  /**
    * The geometric representation is a sphere around the entity's centroid
    * positioned following the axis of rotation (the entity's base).
    * The specific entity should be a projectile, else the result is invalid.
    * @param o the entity
    * @return the representation
    */
  def representProjectileBySphere()(o: Any): VolumetricGeometry = {
    o match {
      case p: Projectile =>
        Sphere(p.Position, p.Definition.DamageRadius)
      case _ =>
        invalidPoint
    }
  }

  /**
    * The geometric representation is a cylinder around the entity's base.
    * @param radius half the distance across
    * @param height how tall the cylinder is (the distance of the top to the base)
    * @param o the entity
    * @return the representation
    */
  def representByCylinder(radius: Float, height: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject => Cylinder(p.Position, Vector3.relativeUp(p.Orientation), radius, height)
      case s: SourceEntry          => Cylinder(s.Position, Vector3.relativeUp(s.Orientation), radius, height)
      case _                       => invalidCylinder
    }
  }

  /**
    * The geometric representation is a cylinder around the entity's base
    * if the target represents a player entity.
    * @param radius a measure of the player's bulk
    * @param o the entity
    * @return the representation
    */
  def representPlayerByCylinder(radius: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: Player =>
        val radialOffset = if(p.ExoSuit == ExoSuitType.MAX) 0.25f else 0f
        Cylinder(
          p.Position,
          radius + radialOffset,
          GlobalDefinitions.MaxDepth(p)
        )
      case p: PlayerSource =>
        val radialOffset = if(p.ExoSuit == ExoSuitType.MAX) 0.125f else 0f
        val heightOffset = if(p.crouching) 1.093750f else GlobalDefinitions.avatar.MaxDepth
        Cylinder(
          p.Position,
          radius + radialOffset,
          heightOffset
        )
      case _ =>
        invalidCylinder
    }
  }

  /**
    * The geometric representation is a cylinder around the entity's base
    * as if the target is displaced from the ground at an expected (fixed?) distance.
    * @param radius half the distance across
    * @param height how tall the cylinder is (the distance of the top to the base)
    * @param hoversAt how far off the base coordinates the actual cylinder begins
    * @param o the entity
    * @return the representation
    */
  def representHoveringEntityByCylinder(radius: Float, height: Float, hoversAt: Float)(o: Any): VolumetricGeometry = {
    o match {
      case p: PlanetSideGameObject =>
        Cylinder(p.Position, Vector3.relativeUp(p.Orientation), radius, height)
      case s: SourceEntry =>
        Cylinder(s.Position, Vector3.relativeUp(s.Orientation), radius, height)
      case _ =>
        invalidCylinder
    }
  }
}
