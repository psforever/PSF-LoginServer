//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import services.avatar.{AvatarAction, AvatarServiceMessage}

trait RepairableAmenity extends RepairableEntity {
  override def Restoration(obj : Repairable.Target) : Unit = {
    super.Restoration(obj)
    val zone = obj.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val targetGUID = obj.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 0))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 0))
  }
}
