// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.geometry.VolumetricEnvironmentCollision
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentCollision, EnvironmentTrait, PieceOfEnvironment}

final case class InteriorDoorPassage(door: Door)
  extends PieceOfEnvironment {
  assert(door.Definition.geometryInteractionRadius.nonEmpty, s"door ${door.GUID} needs an interaction radius to be volumetric")
  //assert(door.Outwards != Vector3.Zero, s"door ${door.GUID} does not have an outwards direction")

  /** a general description of this environment */
  override def attribute: EnvironmentTrait = EnvironmentAttribute.InteriorField

  /** a special representation of the region that qualifies as "this environment" */
  override def collision: EnvironmentCollision = VolumetricEnvironmentCollision(door)
}
