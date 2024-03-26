// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.serverobject.structures.{Amenity, AmenityDefinition, CreateEnvironmentField}

final case class InteriorDoorField(
                                    cylinderHeight: Option[Float] = None,
                                    centerOn: Boolean = false
                                  ) extends CreateEnvironmentField {
  def attribute: EnvironmentTrait = EnvironmentAttribute.InteriorField

  def create(obj: Amenity): PieceOfEnvironment = {
    obj match {
      case door: Door => InteriorDoorPassage(door, cylinderHeight, centerOn)
      case _ => throw new IllegalArgumentException("expecting door")
    }
  }
}

/**
  * The definition for any `Door`.
  */
class DoorDefinition(objectId: Int)
  extends AmenityDefinition(objectId) {
  Name = "door"
  /** range wherein the door may first be opened
   * (note: intentionally inflated as the initial check on the client occurs further than expected) */
  var initialOpeningDistance: Float = 7.5f
  /** range within which the door must detect a target player to remain open */
  var continuousOpenDistance: Float = 5.05f

  override def Geometry: Any => VolumetricGeometry = GeometryForm.representBySphere(UseRadius)
}
