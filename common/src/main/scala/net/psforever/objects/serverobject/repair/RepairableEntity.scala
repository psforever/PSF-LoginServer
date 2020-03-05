//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Tool}
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
      else {
        //TODO temp debug
        val definition = obj.Definition
        val rep = definition.Repairable
        val health = obj.Health < definition.MaxHealth
        val notDest = !obj.Destroyed
        val repIf = definition.RepairIfDestroyed
        val fact = obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL
        val ammoGood = item.AmmoType == Ammo.armor_canister && item.Magazine > 0
        org.log4s.getLogger.warn(s"${obj.asInstanceOf[PlanetSideGameObject].Definition.Name} not repairable for reason: hp=$health, destroyed=$notDest, canrep=$rep | revive=$repIf, faction=$fact, ammo=$ammoGood")
      }
  }

  protected def CanPerformRepairs(obj : Repairable.Target, player : Player, item : Tool) : Boolean = {
    val definition = obj.Definition
    definition.Repairable && obj.Health < definition.MaxHealth && (definition.RepairIfDestroyed || !obj.Destroyed) &&
      (obj.Faction == player.Faction || obj.Faction == PlanetSideEmpire.NEUTRAL) &&
      item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
      Vector3.Distance(obj.Position, player.Position) < 5
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
