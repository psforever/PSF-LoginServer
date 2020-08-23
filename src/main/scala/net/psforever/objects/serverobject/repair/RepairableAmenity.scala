//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * The "control" `Actor` mixin for repair-handling code
  * for the majority of `Repairable` `Amenity` objects installed in a facility or a field tower.
  */
trait RepairableAmenity extends RepairableEntity {
  def RepairableObject: Amenity

  override def Restoration(obj: Repairable.Target): Unit = {
    super.Restoration(obj)
    RepairableAmenity.Restoration(obj)
  }
}

object RepairableAmenity {

  /**
    * A resotred `Amenity` target dispatches two messages to chance its model and operational states.
    * These `PlanetSideAttributeMessage` attributes are the same as reported during zone load client configuration.
    * @see `AvatarAction.PlanetsideAttributeToAll`
    * @see `AvatarServiceMessage`
    * @see `Zone.AvatarEvents`
    * @param target the entity being destroyed
    */
  def Restoration(target: Repairable.Target): Unit = {
    val zone       = target.Zone
    val zoneId     = zone.id
    val events     = zone.AvatarEvents
    val targetGUID = target.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 0))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 0))
  }
}
