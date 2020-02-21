// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Tool}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior
import net.psforever.objects.vital.Vitality
import net.psforever.packet.game.{DamageFeedbackMessage, DestroyMessage, InventoryStateMessage, RepairMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}
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
          item.Magazine > 0) {
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
    val playerName = lastShot.projectile.owner.Name
    val playerGUID = target.Zone.LivePlayers.find { p => playerName.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => targetGUID
    }
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      playerName,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageFeedbackMessage(5, true, Some(playerGUID), None, None, false, Some(targetGUID), None, None, None, 0, 0L, 0))
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
    val playerName = lastShot.projectile.owner.Name
    val playerGUID = target.Zone.LivePlayers.find { p => playerName.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => targetGUID
    }
    target.Destroyed = true
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 1))
    events ! AvatarServiceMessage(
      zoneId,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DestroyMessage(targetGUID, playerGUID, Service.defaultPlayerGUID, target.Position)) //how many players get this message?
    )
  }
}
