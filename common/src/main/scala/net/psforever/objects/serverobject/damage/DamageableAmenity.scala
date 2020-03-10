//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.structures.Amenity
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait DamageableAmenity extends DamageableEntity {
  def DamageableObject : Amenity

  override protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    val zone = target.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val targetGUID = target.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 1))
  }
}
