// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.geometry.{GeometryForm, VolumetricEnvironmentCollision}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentCollision, EnvironmentTrait, PieceOfEnvironment}

final case class InteriorDoorPassage(
                                      door: Door,
                                      cylinderHeight: Option[Float] = None,
                                      centerOn: Boolean = false
                                    )
  extends PieceOfEnvironment {
  assert(door.Definition.UseRadius > 0f, s"door ${door.GUID} needs an interaction radius to be positive")
  private lazy val collisionObject = {
    val radius = door.Definition.UseRadius
    val g = (cylinderHeight, centerOn) match {
      case (Some(h), false) => GeometryForm.representByCylinder(radius, h) _
      case (Some(h), true)  => GeometryForm.representByRaisedCylinder(radius, h) _
      case (None, false)    => GeometryForm.representBySphereOnBase(radius) _
      case _                => GeometryForm.representBySphere(radius) _
    }
    VolumetricEnvironmentCollision(g.apply(door))
  }

  /** a general description of this environment */
  override def attribute: EnvironmentTrait = EnvironmentAttribute.InteriorField

  /** a special representation of the region that qualifies as "this environment" */
  override def collision: EnvironmentCollision = collisionObject
}
