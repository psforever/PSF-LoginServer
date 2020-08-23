//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * The "control" `Actor` mixin for damage-handling code
  * for the majority of `Damageable` `Amenity` objects installed in a facility or a field tower,
  * with specific exceptions for the `ImplantTerminalMech` and the `Generator`.
  */
trait DamageableAmenity extends DamageableEntity {
  def DamageableObject: Amenity

  override protected def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    DamageableAmenity.DestructionAwareness(target, cause)
    target.ClearHistory()
  }
}

object DamageableAmenity {

  /**
    * A destroyed `Amenity` target dispatches two messages to chance its model and operational states.
    * The common manifestation is a sparking entity that will no longer report being accessible.
    * These `PlanetSideAttributeMessage` attributes are the same as reported during zone load client configuration.
    * @see `AvatarAction.PlanetsideAttributeToAll`
    * @see `AvatarServiceMessage`
    * @see `Zone.AvatarEvents`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile): Unit = {
    val zone       = target.Zone
    val zoneId     = zone.id
    val events     = zone.AvatarEvents
    val targetGUID = target.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 1))
  }
}
