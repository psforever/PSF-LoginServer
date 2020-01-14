// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile, SourceEntry}
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.vital.{PlayerSuicide, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.{ExoSuitType, PlanetSideGUID}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

/**
  * na;
  * stub for future development
  */
class PlayerControl(player : Player) extends Actor
  with JammableBehavior {
  def JammableObject = player

  def receive : Receive = jammableBehavior.orElse {
    case Player.Die() =>
      PlayerControl.HandleDestructionAwareness(player, player.GUID, None)

    case Vitality.Damage(resolution_function) =>
      if(player.isAlive) {
        val originalHealth = player.Health
        val originalArmor = player.Armor
        val originalCapacitor = player.Capacitor.toInt
        val cause = resolution_function(player)
        val health = player.Health
        val armor = player.Armor
        val capacitor = player.Capacitor.toInt
        val damageToHealth = originalHealth - health
        val damageToArmor = originalArmor - armor
        val damageToCapacitor = originalCapacitor - capacitor
        PlayerControl.HandleDamageResolution(player, cause, damageToHealth, damageToArmor, damageToCapacitor)
        if(damageToHealth != 0 || damageToArmor != 0 || damageToCapacitor != 0) {
          org.log4s.getLogger("DamageResolution")
            .info(s"${player.Name}-infantry: BEFORE=$originalHealth/$originalArmor/$originalCapacitor, AFTER=$health/$armor/$capacitor, CHANGE=$damageToHealth/$damageToArmor/$damageToCapacitor")
        }
      }
    case _ => ;
  }
}

object PlayerControl {
  /**
    * na
    * @param target na
    */
  def HandleDamageResolution(target : Player, cause : ResolvedProjectile, damageToHealth : Int, damageToArmor : Int, damageToCapacitor : Int) : Unit = {
    val targetGUID = target.GUID
    val playerGUID = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health > 0) {
      //activity on map
      if(damageToHealth + damageToArmor > 0) {
        target.Zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert damage source
        HandleDamageAwareness(target, playerGUID, cause)
      }
      if(cause.projectile.profile.JammerProjectile) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
    }
    else {
      HandleDestructionAwareness(target, playerGUID, Some(cause))
    }
    if(damageToHealth > 0) {
      target.Zone.AvatarEvents ! AvatarServiceMessage(target.Zone.Id, AvatarAction.PlanetsideAttributeToAll(targetGUID, 0, target.Health))
    }
    if(damageToArmor > 0) {
      target.Zone.AvatarEvents ! AvatarServiceMessage(target.Zone.Id, AvatarAction.PlanetsideAttributeToAll(targetGUID, 4, target.Armor))
    }
    if(damageToCapacitor > 0) {
      target.Zone.AvatarEvents ! AvatarServiceMessage(target.Name, AvatarAction.PlanetsideAttributeSelf(targetGUID, 7, target.Capacitor.toLong))
    }
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : Player, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val owner = lastShot.projectile.owner
    owner match {
      case pSource : PlayerSource =>
        target.Zone.LivePlayers.find(_.Name == pSource.Name) match {
          case Some(tplayer) =>
            target.Zone.AvatarEvents ! AvatarServiceMessage(
              target.Name,
              AvatarAction.HitHint(tplayer.GUID, target.GUID)
            )
          case None => ;
        }
      case vSource : SourceEntry =>
        target.Zone.AvatarEvents ! AvatarServiceMessage(
          target.Name,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(10, vSource.Position))
        )
      case _ => ;
    }
  }

  /**
    * The player has lost all his vitality and must be killed.<br>
    * <br>
    * Shift directly into a state of being dead on the client by setting health to zero points,
    * whereupon the player will perform a dramatic death animation.
    * Stamina is also set to zero points.
    * If the player was in a vehicle at the time of demise, special conditions apply and
    * the model must be manipulated so it behaves correctly.
    * Do not move or completely destroy the `Player` object as its coordinates of death will be important.<br>
    * <br>
    * A maximum revive waiting timer is started.
    * When this timer reaches zero, the avatar will attempt to spawn back on its faction-specific sanctuary continent.
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : Player, attribution : PlanetSideGUID, lastShot : Option[ResolvedProjectile]) : Unit = {
    val player_guid = target.GUID
    val pos = target.Position
    val respawnTimer = 300000 //milliseconds
    val zone = target.Zone
    val events = zone.AvatarEvents
    val nameChannel = target.Name
    val zoneChannel = zone.Id
    target.Die
    events ! AvatarServiceMessage(nameChannel, AvatarAction.Killed(player_guid))
    if(target.VehicleSeated.nonEmpty) {
      //make player invisible (if not, the cadaver sticks out the side in a seated position)
      events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 29, 1))
      events ! AvatarServiceMessage(zoneChannel, AvatarAction.ObjectDelete(player_guid, player_guid)) ///dead player still "sees" self
    }
    events ! AvatarServiceMessage(zoneChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 0, 0)) //health
    events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 2, 0)) //stamina
    events ! AvatarServiceMessage(zoneChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 4, target.Armor)) //armor
    if(target.ExoSuit == ExoSuitType.MAX) {
      events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 7, 0)) // capacitor
    }
    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DestroyMessage(player_guid, player_guid, Service.defaultPlayerGUID, pos)) //how many players get this message?
    )
    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, AvatarDeadStateMessage(DeadState.Dead, respawnTimer, respawnTimer, pos, target.Faction, true))
    )
    //TODO other methods of death?
    val pentry = PlayerSource(target)
    (target.History.find({p => p.isInstanceOf[PlayerSuicide]}) match {
      case Some(PlayerSuicide(_)) =>
        None
      case _ =>
        lastShot.orElse { target.LastShot } match {
          case out @ Some(shot) =>
            if(System.nanoTime - shot.hit_time < (10 seconds).toNanos) {
              out
            }
            else {
              None //suicide
            }
          case None =>
            None //suicide
        }
    }) match {
      case Some(shot) =>
        zone.Activity ! Zone.HotSpot.Activity(pentry, shot.projectile.owner, shot.hit_pos)
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(shot.projectile.owner, pentry, shot.projectile.attribute_to))
      case None =>
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(pentry, pentry, 0))
    }
  }
}
