//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Tool}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.ZoneAware
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait RepairableVehicle extends Repairable {
  def RepairableObject : PlanetSideGameObject with Vitality with FactionAffinity with ZoneAware with MountedWeapons

  val canBeRepairedByNanoDispenser : Receive = {
    case CommonMessages.Use(player, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
      val obj = RepairableObject
      val definition = obj.Definition
      //by default, vehicles can not be repaired after being destroyed
      //supporting that requires appropriating health and sending a new ObjectCreateMessage
      if(definition.Repairable && obj.Health < definition.MaxHealth && !obj.Destroyed &&
        (obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL) &&
        item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
        !player.isMoving && Vector3.Distance(obj.Position, player.Position) < 20) {
        val zone = obj.Zone
        val zoneId = zone.Id
        val events = zone.AvatarEvents
        val pname = player.Name
        val tguid = obj.GUID
        val magazine = item.Magazine -= 1
        val health = obj.Health += 12 + item.FireMode.Modifiers.Damage1
        val repairPercent: Long = health * 100 / obj.Definition.MaxHealth
        events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
        events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, repairPercent)))
        events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, health))
      }
  }
}
