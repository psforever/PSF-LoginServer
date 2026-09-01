// Copyright (c) 2026 PSForever
package net.psforever.objects.definition

import net.psforever.objects.avatar.Avatars
import net.psforever.objects.definition.converter.AvatarBotConverter
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.vital.VitalityDefinition

/**
  * The definition for game objects that look like players.
  * @param objectId the object type number
  */
class AvatarBotDefinition(objectId: Int) extends ObjectDefinition(objectId) with VitalityDefinition {
  Name = "avatar_bot"
  Avatars(objectId) //let throw NoSuchElementException
  Packet = AvatarBotConverter
  Geometry = GeometryForm.representPlayerByCylinder(radius = 1.6f)
  //do NOT attempt to create AvatarBot's outside of the VR Shooting Range zones with this definition, 
  //space will need to be made to add a "bots" pool to the target zone
  registerAs = "bots"
}

object AvatarBotDefinition {
  def apply(objectId: Int): AvatarBotDefinition = {
    new AvatarBotDefinition(objectId)
  }

  def apply(avatar: Avatars.Value): AvatarBotDefinition = {
    new AvatarBotDefinition(avatar.id)
  }
}
