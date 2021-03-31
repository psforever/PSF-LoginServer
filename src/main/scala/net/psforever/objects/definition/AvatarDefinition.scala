// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.avatar.Avatars
import net.psforever.objects.definition.converter.AvatarConverter
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.vital.VitalityDefinition

/**
  * The definition for game objects that look like players.
  * @param objectId the object type number
  */
class AvatarDefinition(objectId: Int) extends ObjectDefinition(objectId) with VitalityDefinition {
  Avatars(objectId) //let throw NoSuchElementException
  Packet = AvatarDefinition.converter
  Geometry = GeometryForm.representPlayerByCylinder(radius = 1.6f)
}

object AvatarDefinition {
  private val converter = new AvatarConverter()

  def apply(objectId: Int): AvatarDefinition = {
    new AvatarDefinition(objectId)
  }

  def apply(avatar: Avatars.Value): AvatarDefinition = {
    new AvatarDefinition(avatar.id)
  }
}
