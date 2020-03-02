//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Tool}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait RepairableAmenity extends Repairable {
  val canBeRepairedByNanoDispenser : Receive = {
    case CommonMessages.Use(player, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
      val obj = RepairableObject
      val definition = obj.Definition
      if(definition.Repairable && obj.Health < definition.MaxHealth && (definition.RepairIfDestroyed || !obj.Destroyed) &&
        (obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL) &&
        item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
        !player.isMoving && Vector3.Distance(obj.Position, player.Position) < 10) {
        val zone = obj.Zone
        val zoneId = zone.Id
        val events = zone.AvatarEvents
        val pname = player.Name
        val tguid = obj.GUID
        val magazine = item.Discharge
        val health = obj.Health += 12 + item.FireMode.Modifiers.Damage4 + definition.RepairMod
        val repairPercent: Long = health * 100 / definition.MaxHealth
        events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
        events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, repairPercent)))
        events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, health))
        if(obj.Destroyed && health > definition.RepairRestoresAt) {
          obj.Destroyed = false
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 0))
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 0))
        }
      }
      else {
        //TODO temp debug
        val rep = definition.Repairable
        val health = obj.Health < definition.MaxHealth
        val repIf = definition.RepairIfDestroyed
        val notDest = !obj.Destroyed
        val fact = obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL
        val ammoGood = item.AmmoType == Ammo.armor_canister && item.Magazine > 0
        val playerGood = !player.isMoving && Vector3.Distance(obj.Position, player.Position) < 5
        org.log4s.getLogger.warn(s"${obj.asInstanceOf[PlanetSideGameObject].Definition.Name} not repairable for reason: $rep, $health, $repIf, $notDest, $fact, $ammoGood, $playerGood")
      }
  }
}
