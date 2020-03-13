//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait RepairableEntity extends Repairable {
  final val canBeRepairedByNanoDispenser : Receive = {
    case CommonMessages.Use(player, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
      val obj = RepairableObject
      if(CanPerformRepairs(obj, player, item)) {
        PerformRepairs(obj, player, item)
      }
  }

  protected def CanPerformRepairs(obj : Repairable.Target, player : Player, item : Tool) : Boolean = {
    val definition = obj.Definition
    val o = definition.Repairable && obj.Health < definition.MaxHealth && (definition.RepairIfDestroyed || !obj.Destroyed) &&
      (obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL) &&
      item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
      Vector3.Distance(obj.Position, player.Position) < 5
    if(!o) {
      import net.psforever.objects.definition.ObjectDefinition
      org.log4s.getLogger.warn(
        if(!definition.Repairable) s"The ${definition.asInstanceOf[ObjectDefinition].Name} object type is not repairable."
        else if(obj.Destroyed && !definition.RepairIfDestroyed) s"The ${definition.asInstanceOf[ObjectDefinition].Name} object type can not be repaired if already destroyed."
        else if(obj.Health == obj.MaxHealth) "This entity does not require repairs."
        else if(Vector3.Distance(obj.Position, player.Position) > 5) s"This entity is too far away to repair - pos=${obj.Position}, dist=${Vector3.Distance(obj.Position, player.Position)}."
        else if(!obj.CanRepair) "There is some other reason this entity can not be repaired."
        else "Who knows why this entity can not be repaired!"
      )
    }
    o
  }

  protected def PerformRepairs(obj : Repairable.Target, player : Player, item : Tool) : Unit = {
    val definition = obj.Definition
    val zone = obj.Zone
    val events = zone.AvatarEvents
    val pname = player.Name
    val tguid = obj.GUID
    val originalHealth = obj.Health
    val health = originalHealth + 12 + RepairValue(item) + definition.RepairMod
    val updatedHealth = if(!(player.isMoving || obj.isMoving)) { //only allow stationary repairs
      val newHealth = obj.Health = health
      val zoneId = zone.Id
      val magazine = item.Discharge
      events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
      events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, newHealth))
      if(obj.Destroyed && health > definition.RepairRestoresAt) {
        Restoration(obj)
      }
      newHealth
    }
    else {
      originalHealth
    }
    //progress bar remains visible for all repair attempts
    events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, updatedHealth * 100 / definition.MaxHealth)))
  }

  override def RepairValue(item : Tool) : Int = item.FireMode.Modifiers.Damage4
}
