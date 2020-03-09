// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.Actor
import net.psforever.objects.{DefaultCancellable, ImplantSlot, Player}
import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile, SourceEntry}
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.vital.{PlayerSuicide, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.{ExoSuitType, ImplantType, PlanetSideGUID}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * na;
  * stub for future development
  */
class PlayerControl(player : Player) extends Actor
  with JammableBehavior {
  def JammableObject = player

  private [this] val log = org.log4s.getLogger(player.Name)
  private [this] val damageLog = org.log4s.getLogger("DamageResolution")

  // A collection of timers for each slot to trigger stamina drain on an interval
  val implantSlotStaminaDrainTimers = mutable.HashMap(0 -> DefaultCancellable.obj, 1 -> DefaultCancellable.obj, 2 -> DefaultCancellable.obj)

  def receive : Receive = jammableBehavior.orElse {
    case Player.ImplantActivation(slot: Int, status : Int) =>
      // todo: disable implants with stamina cost when changing armour type
      val implantSlot = player.ImplantSlot(slot)

      if(status == 0 && implantSlot.Active) {
        // Cancel stamina drain timer
        implantSlotStaminaDrainTimers(slot).cancel()
        implantSlotStaminaDrainTimers(slot) = DefaultCancellable.obj

        implantSlot.Active = false
        player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.PlanetsideAttribute(player.GUID, 28, player.Implant(slot).id * 2)) // Deactivation sound / effect
        player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.DeactivateImplantSlot(player.GUID, slot))
      } else if (status == 1 && implantSlot.Initialized && !implantSlot.Active && !player.Fatigued) {
        implantSlot.Installed match {
          case Some(implant: ImplantDefinition) =>
            implantSlot.Active = true

            if (implant.ActivationStaminaCost >= 0) {
              player.Stamina -= implant.ActivationStaminaCost // Activation stamina drain
            }

            if(implant.StaminaCost > 0 && implant.GetCostIntervalByExoSuit(player.ExoSuit) > 0) { // Ongoing stamina drain, if applicable
              implantSlotStaminaDrainTimers(slot) = context.system.scheduler.schedule(0 seconds, implant.GetCostIntervalByExoSuit(player.ExoSuit) milliseconds, self, Player.DrainStamina(implant.StaminaCost))
            }

            player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.PlanetsideAttribute(player.GUID, 28, player.Implant(slot).id * 2 + 1)) // Activation sound / effect
            player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.ActivateImplantSlot(player.GUID, slot))
        }
      }

    case Player.UninitializeImplant(slot: Int) => {
      PlayerControl.UninitializeImplant(player, slot)
    }

    case Player.ImplantInitializationStart(slot: Int) =>
      val implantSlot = player.ImplantSlot(slot)
      if(implantSlot.Installed.isDefined) {
        if(implantSlot.Initialized) {
          PlayerControl.UninitializeImplant(player, slot)
        }

        // Start client side initialization timer
        player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.SendResponse(player.GUID, ActionProgressMessage(slot + 6, 0)))

        // Callback after initialization timer to complete initialization
        implantSlot.InitializeTimer = context.system.scheduler.scheduleOnce(implantSlot.MaxTimer milliseconds, self, Player.ImplantInitializationComplete(slot))
      }

    case Player.ImplantInitializationComplete(slot: Int) =>
      val implantSlot = player.ImplantSlot(slot)
      if(implantSlot.Installed.isDefined) {
        player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.SendResponse(player.GUID, AvatarImplantMessage(player.GUID, ImplantAction.Initialization, slot, 1)))
        implantSlot.Initialized = true
        implantSlot.InitializeTimer = DefaultCancellable.obj
      }

    case Player.DrainStamina(amount : Int) =>
      player.Stamina -= amount

    case Player.StaminaChanged(currentStamina : Int) =>
    if(currentStamina == 0) {
        player.Fatigued = true
        player.skipStaminaRegenForTurns += 4
        for(slot <- 0 to player.Implants.length - 1) { // Disable all implants
          self ! Player.ImplantActivation(slot, 0)
          player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.SendResponseTargeted(player.GUID, AvatarImplantMessage(player.GUID, ImplantAction.OutOfStamina, slot, 1)))
        }
      } else if (player.Fatigued && currentStamina >= 20) {
        player.Fatigued = false
        for(slot <- 0 to player.Implants.length - 1) { // Re-enable all implants
          player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.SendResponseTargeted(player.GUID, AvatarImplantMessage(player.GUID, ImplantAction.OutOfStamina, slot, 0)))
        }
      }

      player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.PlanetsideAttributeSelf(player.GUID, 2, player.Stamina))

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
          damageLog.info(s"${player.Name}-infantry: BEFORE=$originalHealth/$originalArmor/$originalCapacitor, AFTER=$health/$armor/$capacitor, CHANGE=$damageToHealth/$damageToArmor/$damageToCapacitor")
        }
      }
    case _ => ;
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

  def UninitializeImplant(player: Player, slot: Int): Unit = {
    val implantSlot = player.ImplantSlot(slot)

    implantSlot.Initialized = false
    player.Zone.AvatarEvents ! AvatarServiceMessage(player.Zone.Id, AvatarAction.SendResponse(player.GUID, AvatarImplantMessage(player.GUID, ImplantAction.Initialization, slot, 0)))
  }
}
