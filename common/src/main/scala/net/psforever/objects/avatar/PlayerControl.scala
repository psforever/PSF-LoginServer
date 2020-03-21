// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile}
import net.psforever.objects.equipment.{Ammo, JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.repair.Repairable
import net.psforever.objects.vital.{PlayerSuicide, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.{ExoSuitType, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

/**
  * na;
  * stub for future development
  */
class PlayerControl(player : Player) extends Actor
  with JammableBehavior
  with Damageable {
  def JammableObject = player
  def DamageableObject = player

  //private [this] val log = org.log4s.getLogger(player.Name)
  private [this] val damageLog = org.log4s.getLogger("DamageResolution")

  def receive : Receive = jammableBehavior
    .orElse(takesDamage)
    .orElse {
      case Player.Die() =>
        if(player.isAlive) {
          PlayerControl.DestructionAwareness(player, None)
        }

      case CommonMessages.Use(user, Some(item : Tool)) if item.Definition == GlobalDefinitions.medicalapplicator && player.isAlive =>
        //heal
        val originalHealth = player.Health
        if(player.MaxHealth > 0 && originalHealth < player.MaxHealth &&
          user.Faction == player.Faction &&
          item.Magazine > 0 &&
          Vector3.Distance(user.Position, player.Position) < 5) {
          val definition = player.Definition
          val zone = player.Zone
          val events = zone.AvatarEvents
          val uname = user.Name
          val guid = player.GUID
          if(!(player.isMoving || user.isMoving)) { //only allow stationary heals
            player.Health = originalHealth + 10
            val magazine = item.Discharge
            events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
            events ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(guid, 0, player.Health))
          }
          if(player != user) {
            //progress bar remains visible for all repair attempts
            events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(guid, player.Health * 100 / definition.MaxHealth)))
          }
        }

      case CommonMessages.Use(user, Some(item : Tool)) if item.Definition == GlobalDefinitions.medicalapplicator && !player.isAlive =>
        //revive
        if(user != player && user.isAlive && !user.isMoving &&
          !player.isBackpack &&
          item.Magazine >= 25) {
          sender ! CommonMessages.Use(player, Some((item, user)))
        }

      case CommonMessages.Use(user, Some(item : Tool)) if item.Definition == GlobalDefinitions.bank =>
        val originalArmor = player.Armor
        if(player.MaxArmor > 0 && originalArmor < player.MaxArmor &&
          user.Faction == player.Faction &&
          item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
          Vector3.Distance(user.Position, player.Position) < 5) {
          val definition = player.Definition
          val zone = player.Zone
          val events = zone.AvatarEvents
          val uname = user.Name
          val guid = player.GUID
          if(!(player.isMoving || user.isMoving)) { //only allow stationary repairs
            player.Armor = originalArmor + Repairable.Quality + RepairValue(item) + definition.RepairMod
            val magazine = item.Discharge
            events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
            events ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(guid, 4, player.Armor))
          }
          if(player != user) {
            //progress bar remains visible for all repair attempts
            events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(guid, player.Armor * 100 / player.MaxArmor)))
          }
        }

      case _ => ;
    }

  protected def TakesDamage : Receive = {
    case Vitality.Damage(applyDamageTo) =>
      if(player.isAlive) {
        val originalHealth = player.Health
        val originalArmor = player.Armor
        val originalCapacitor = player.Capacitor.toInt
        val cause = applyDamageTo(player)
        val health = player.Health
        val armor = player.Armor
        val capacitor = player.Capacitor.toInt
        val damageToHealth = originalHealth - health
        val damageToArmor = originalArmor - armor
        val damageToCapacitor = originalCapacitor - capacitor
        PlayerControl.HandleDamage(player, cause, damageToHealth, damageToArmor, damageToCapacitor)
        if(damageToHealth > 0 || damageToArmor > 0 || damageToCapacitor > 0) {
          damageLog.info(s"${player.Name}-infantry: BEFORE=$originalHealth/$originalArmor/$originalCapacitor, AFTER=$health/$armor/$capacitor, CHANGE=$damageToHealth/$damageToArmor/$damageToCapacitor")
        }
      }
  }

  /**
    * Start the jammered buzzing.
    * Although, as a rule, the jammering sound effect should last as long as the jammering status,
    * Infantry seem to hear the sound for a bit longer than the effect.
    * @see `JammableBehavior.StartJammeredSound`
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds;
    *            by default, 30000
    */
  override def StartJammeredSound(target : Any, dur : Int) : Unit = target match {
    case obj : Player if !jammedSound =>
      obj.Zone.AvatarEvents ! AvatarServiceMessage(obj.Zone.Id, AvatarAction.PlanetsideAttributeToAll(obj.GUID, 27, 1))
      super.StartJammeredSound(obj, 3000)
    case _ => ;
  }

  /**
    * Perform a variety of tasks to indicate being jammered.
    * Deactivate implants (should also uninitialize them),
    * delay stamina regeneration for a certain number of turns,
    * and set the jammered status on specific holstered equipment.
    * @see `JammableBehavior.StartJammeredStatus`
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds
    */
  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : Player =>
      //TODO these features
      obj.Zone.AvatarEvents ! AvatarServiceMessage(obj.Zone.Id, AvatarAction.DeactivateImplantSlot(obj.GUID, 1))
      obj.Zone.AvatarEvents ! AvatarServiceMessage(obj.Zone.Id, AvatarAction.DeactivateImplantSlot(obj.GUID, 2))
      obj.skipStaminaRegenForTurns = math.max(obj.skipStaminaRegenForTurns, 10)
      super.StartJammeredStatus(target, dur)
    case _ => ;
  }

  /**
    * Stop the jammered buzzing.
    * @see `JammableBehavior.CancelJammeredSound`
    * @param target an object that can be affected by the jammered status
    */
  override def CancelJammeredSound(target : Any) : Unit = target match {
    case obj : Player if jammedSound =>
      obj.Zone.AvatarEvents ! AvatarServiceMessage(obj.Zone.Id, AvatarAction.PlanetsideAttributeToAll(obj.GUID, 27, 0))
      super.CancelJammeredSound(obj)
    case _ => ;
  }

  def RepairValue(item : Tool) : Int = if(player.ExoSuit != ExoSuitType.MAX) {
    item.FireMode.Modifiers.Damage0
  }
  else {
    item.FireMode.Modifiers.Damage3
  }
}

object PlayerControl {
  /**
    * na
    * @param target na
    */
  def HandleDamage(target : Player, cause : ResolvedProjectile, damageToHealth : Int, damageToArmor : Int, damageToCapacitor : Int) : Unit = {
    val targetGUID = target.GUID
    val zone = target.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val health = target.Health
    if(health > 0) {
      if(damageToCapacitor > 0) {
        events ! AvatarServiceMessage(target.Name, AvatarAction.PlanetsideAttributeSelf(targetGUID, 7, target.Capacitor.toLong))
      }
      if(damageToHealth > 0 || damageToArmor > 0) {
        if(damageToHealth > 0) {
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 0, health))
        }
        if(damageToArmor > 0) {
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 4, target.Armor))
        }
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert damage source
        DamageAwareness(target, cause)
      }
      if(Damageable.CanJammer(target, cause)) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
    }
    else {
      if(damageToArmor > 0) {
        events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 4, target.Armor))
      }
      DestructionAwareness(target, Some(cause))
    }
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  def DamageAwareness(target : Player, cause : ResolvedProjectile) : Unit = {
    val name = target.Name
    val zone = target.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      name,
      cause.projectile.owner match {
        case pSource : PlayerSource => //player damage
          val name = pSource.Name
          (zone.LivePlayers.find(_.Name == name).orElse(zone.Corpses.find(_.Name == name)) match {
            case Some(player) => AvatarAction.HitHint(player.GUID, player.GUID)
            case None => AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(10, pSource.Position))
          }) match {
            case AvatarAction.HitHint(_, guid) => AvatarAction.HitHint(target.GUID, guid)
            case msg => msg
          }
        case source => AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(10, source.Position))
      }
    )
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
    * @param cause na
    */
  def DestructionAwareness(target : Player, cause : Option[ResolvedProjectile]) : Unit = {
    val player_guid = target.GUID
    val pos = target.Position
    val respawnTimer = 300000 //milliseconds
    val zone = target.Zone
    val events = zone.AvatarEvents
    val nameChannel = target.Name
    val zoneChannel = zone.Id
    val attribute = cause match {
      case Some(resolved) =>
        resolved.projectile.owner match {
          case pSource : PlayerSource =>
            val name = pSource.Name
            zone.LivePlayers.find(_.Name == name) match {
              case Some(player) => player.GUID
              case None => player_guid
            }
          case _ => player_guid
        }
      case _ => player_guid
    }
    target.Die
    //unjam
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    events ! AvatarServiceMessage(nameChannel, AvatarAction.Killed(player_guid)) //align client interface fields with state
    if(target.VehicleSeated.nonEmpty) {
      //make player invisible (if not, the cadaver sticks out the side in a seated position)
      events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 29, 1))
      //only the dead player should "see" their own body, so that the death camera has something to focus on
      events ! AvatarServiceMessage(zoneChannel, AvatarAction.ObjectDelete(player_guid, player_guid))
    }
    events ! AvatarServiceMessage(zoneChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 0, 0)) //health
    events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 2, 0)) //stamina
    if(target.Capacitor > 0) {
      target.Capacitor = 0
      events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 7, 0)) // capacitor
    }
    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DestroyMessage(player_guid, attribute, Service.defaultPlayerGUID, pos)) //how many players get this message?
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
        cause.orElse { target.LastShot } match {
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
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(shot.projectile.owner, pentry, shot.projectile.attribute_to))
      case None =>
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(pentry, pentry, 0))
    }
  }
}
