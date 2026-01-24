//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.Tool
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.sourcing.{SourceEntry, SourceWithHealthEntry}
import net.psforever.objects.vital.{DamagingActivity, RepairFromEquipment, SpawningActivity}
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

  override def RepairToolValue(item: Tool): Float = super.RepairToolValue(item) + item.FireMode.Add.Damage1
}

object RepairableAmenity {

  /**
    * A restored `Amenity` target dispatches two messages to chance its model and operational states.
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
    events ! AvatarServiceMessage(zoneId, targetGUID, AvatarAction.PlanetsideAttributeToAll(50, 0))
    events ! AvatarServiceMessage(zoneId, targetGUID, AvatarAction.PlanetsideAttributeToAll(51, 0))
    RestorationOfHistory(target)
  }

  /**
   * The vitality change history will be forgotten as this entity, once destroyed, has been rebuilt.
   * For the purpose of inheritance of experience due to interaction,
   * the users who made an effort to repair the entity in its resurgence.
   * @param target na
   */
  def RestorationOfHistory(target: Repairable.Target): Unit = {
    val list = target.ClearHistory()
    val effort = list.slice(
      list.lastIndexWhere {
        case dam: DamagingActivity => dam.data.targetAfter.asInstanceOf[SourceWithHealthEntry].health == 0
        case _                     => false
      },
      list.size
    ).collect {
      case entry: RepairFromEquipment => Some(entry.user)
      case _                          => None
    }.flatten.distinctBy(_.Name)
    target.LogActivity(SpawningActivity(SourceEntry(target), target.Zone.Number, effort.headOption))
  }
}
