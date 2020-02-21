// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem, Tool, Vehicles}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.vital.Vitality
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, SpawnGroup, TransactionType, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term : Terminal) extends Actor with FactionAffinityBehavior.Check with HackableBehavior.GenericHackable {
  def FactionObject : FactionAffinity = term
  def HackableObject = term

  def receive : Receive = checkBehavior
      .orElse(hackableBehavior)
      .orElse {
      case Terminal.Request(player, msg) =>
        sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

      case CommonMessages.Use(player, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
        if(!player.isMoving && Vector3.Distance(player.Position, term.Position) < 5 &&
          player.Faction == term.Faction && term.Health < term.Definition.MaxHealth &&
          item.AmmoType == Ammo.armor_canister && item.Magazine > 0) {
          val zone = term.Zone
          val zoneId = zone.Id
          val events = zone.AvatarEvents
          val pname = player.Name
          val tguid = term.GUID
          val magazine = item.Magazine -= 1
          val health = term.Health += 12 + item.FireMode.Modifiers.Damage4
          val repairPercent: Long = health * 100 / term.Definition.MaxHealth
          events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
          events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, repairPercent)))
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, health))
          if(term.Destroyed && health > term.Definition.RepairRestoresAt) {
            term.Destroyed = false
            events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 0))
            events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 0))
          }
        }

      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        term.Owner match {
          case b : Building if (b.Faction != player.Faction || b.CaptureConsoleIsHacked) && term.HackedBy.isEmpty =>
            sender ! CommonMessages.Hack(player, term, Some(item))
          case _ => ;
        }

//      case CommonMessages.Use(player, None) if term.Faction == player.Faction =>
//        val tdef = term.Definition
//        if(tdef.isInstanceOf[MatrixTerminalDefinition]) {
//          //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
//          term.Zone.AvatarEvents ! AvatarServiceMessage(
//            player.Name,
//            AvatarAction.SendResponse(Service.defaultPlayerGUID, BindPlayerMessage(BindStatus.Bind, "", true, true, SpawnGroup.Sanctuary, 0, 0, term.Position))
//          )
//        }
//        else if(tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
//          tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal) {
//          FindLocalVehicle match {
//            case Some(vehicle) =>
//              sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
//              sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), vehicle.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, vehicle.Definition.ObjectId))
//            case None =>
//              log.error("UseItem: expected seated vehicle, but found none")
//          }
//        }
//        else if(tdef == GlobalDefinitions.teleportpad_terminal) {
//          //explicit request
//          term.Actor ! Terminal.Request(
//            player,
//            ItemTransactionMessage(term.GUID, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
//          )
//        }
//        else {
//          sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), term.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, tdef.ObjectId))
//        }

      case Vitality.Damage(damage_func) =>
        if(term.Health > 0) {
          val originalHealth = term.Health
          val cause = damage_func(term)
          val health = term.Health
          val damageToHealth = originalHealth - health
          TerminalControl.HandleDamageResolution(term, cause, damageToHealth)
          if(damageToHealth > 0) {
            val name = term.Actor.toString
            val slashPoint = name.lastIndexOf("/")
            org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
          }
        }

      case _ => ;
    }

  override def toString : String = term.Definition.Name
}

object TerminalControl {
  def HandleDamageResolution(target : Terminal, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health > target.Definition.DamageDestroysAt) {
      if(damage > 0) {
        HandleDamageAwareness(target, playerGUID, cause)
      }
    }
    else {
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(targetGUID, 0, target.Health))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : Terminal, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val targetGUID = target.GUID
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      lastShot.projectile.owner.Name,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageFeedbackMessage(5, true, Some(attribution), None, None, false, Some(targetGUID), None, None, None, 0, 0L, 0))
    )
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : Terminal, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val targetGUID = target.GUID
    target.Destroyed = true
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.Destroy(targetGUID, attribution, Service.defaultPlayerGUID, target.Position))
  }
}
