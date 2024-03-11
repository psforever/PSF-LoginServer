// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.structures.AmenityDefinition

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

  var geometryInteractionRadius: Option[Float] = None
  var geometryInteractionHeight: Option[Float] = None
  var geometryInteractionCenterOn: Boolean = false

  override def Geometry: Any => VolumetricGeometry = {
   (geometryInteractionRadius, geometryInteractionHeight, geometryInteractionCenterOn) match {
     case (Some(r), Some(h), false) => GeometryForm.representByCylinder(r, h)
     case (Some(r), Some(h), true)  => GeometryForm.representByRaisedCylinder(r, h)
     case (Some(r), None, false)    => GeometryForm.representBySphereOnBase(r)
     case (Some(r), None, true)     => GeometryForm.representBySphere(r)
     case _                         => super.Geometry
    }
  }
}
