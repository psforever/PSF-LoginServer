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
  val canBeRepairedByNanoDispenser : Receive = {
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
    if(o) {
      o
    }
    else {
      org.log4s.getLogger.info(
        if(!definition.Repairable) {
          "This is not repairable."
        }
        else if(obj.Health == definition.MaxHealth) {
          "This does not need repairs."
        }
        else if(!definition.RepairIfDestroyed && obj.Destroyed) {
          "This is destroyed and can not be repaired from such a state."
        }
        else if(Vector3.Distance(obj.Position, player.Position) >= 5) {
          "This is too far away to be repaired."
        }
        else {
          "Who knows why this doesn't need repairs?"
        }
      )
      false
    }
  }

  protected def PerformRepairs(obj : Repairable.Target, player : Player, item : Tool) : Unit = {
    val definition = obj.Definition
    val zone = obj.Zone
    val events = zone.AvatarEvents
    val pname = player.Name
    val tguid = obj.GUID
    val originalHealth = obj.Health
    val health = originalHealth + 12 + RepairValue(item) + definition.RepairMod
    if(!(player.isMoving || obj.isMoving)) { //only allow stationary repairs
      obj.Health = health
      val zoneId = zone.Id
      val magazine = item.Discharge
      events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
      events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, health))
      if(obj.Destroyed && health > definition.RepairRestoresAt) {
        Restoration(obj)
      }
    }
    //progress bar remains visible for all repair attempts
    events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, health * 100 / definition.MaxHealth)))
  }

  override def RepairValue(item : Tool) : Int = item.FireMode.Modifiers.Damage4
}
