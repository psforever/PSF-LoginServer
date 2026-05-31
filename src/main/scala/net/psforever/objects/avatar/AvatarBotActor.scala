// Copyright (c) 2026 PSForever
package net.psforever.objects.avatar

import akka.actor.{Actor, ActorRef}
import net.psforever.actors.zone.ShootingRangeTargetSpawner
import net.psforever.objects.{GlobalDefinitions, Tool}
import net.psforever.objects.equipment._
import net.psforever.objects.serverobject.aura.{Aura, AuraEffectBehavior}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, Damageable, DamageableEntity}
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.zones._
import net.psforever.packet.game._
import net.psforever.types._
import net.psforever.services.avatar.AvatarAction
import net.psforever.objects.serverobject.environment.interaction.RespondsToZoneEnvironment
import net.psforever.objects.serverobject.repair.Repairable
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.{HealFromEquipment, RepairFromEquipment}
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.{PlanetsideAttribute, SendResponse}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class AvatarBotActor(bot: AvatarBot, spawnerActor: ActorRef)
    extends Actor
    with JammableBehavior
    with Damageable
    with AggravatedBehavior
    with AuraEffectBehavior
    with RespondsToZoneEnvironment {
  def JammableObject: AvatarBot = bot

  def DamageableObject: AvatarBot = bot

  def ContainerObject: AvatarBot = bot

  def AggravatedObject: AvatarBot = bot

  def AuraTargetObject: AvatarBot = bot
  ApplicableEffect(Aura.Plasma)
  ApplicableEffect(Aura.Napalm)
  ApplicableEffect(Aura.Comet)
  ApplicableEffect(Aura.Fire)

  def InteractiveObject: AvatarBot = bot

  //private[this] val log = org.log4s.getLogger(bot.Name)
  private[this] val damageLog = org.log4s.getLogger(Damageable.LogChannel)
  private val scheduler = Executors.newScheduledThreadPool(2)
  /** suffocating, or regaining breath? */
  var submergedCondition: Option[OxygenState] = None
  private var canEmote = false
  private var canRotate = false

  override def postStop(): Unit = {
    EndAllEffects()
    EndAllAggravation()
    respondToEnvironmentPostStop()
    scheduler.shutdown()
  }

  def receive: Receive = Enabled

  def Enabled: Receive =
    jammableBehavior
      .orElse(takesDamage)
      .orElse(aggravatedBehavior)
      .orElse(auraBehavior)
      .orElse(environmentBehavior)
      .orElse {
        case AvatarBot.Spawn() =>
          spawn()

        case AvatarBot.Die(Some(reason)) =>
          dieWithReason(reason)

        case AvatarBot.Die(None) =>
          suicide()

        case AvatarBot.Release() =>
          release()

        case CommonMessages.Use(user, Some(item: Tool))
          if item.Definition == GlobalDefinitions.medicalapplicator && bot.isAlive =>
          //heal
          val originalHealth = bot.Health
          val definition = bot.Definition
          if (
            bot.MaxHealth > 0 && originalHealth < bot.MaxHealth &&
            user.Faction == bot.Faction &&
            item.Magazine > 0 &&
            Vector3.Distance(user.Position, bot.Position) < definition.RepairDistance
          ) {
            val zone = bot.Zone
            val events = zone.AvatarEvents
            val uname = user.Name
            val guid = bot.GUID
            if (!(bot.isMoving || user.isMoving)) { //only allow stationary heals
              val newHealth = bot.Health = originalHealth + 10
              val magazine = item.Discharge()
              events ! MessageEnvelope(
                uname,
                SendResponse(
                  InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)
                )
              )
              events ! MessageEnvelope(zone.id, PlanetsideAttribute(guid, 0, newHealth))
              bot.LogActivity(
                HealFromEquipment(
                  PlayerSource(user),
                  GlobalDefinitions.medicalapplicator,
                  newHealth - originalHealth
                )
              )
            }
            //progress bar remains visible for all heal attempts
            events ! MessageEnvelope(
              uname,
              SendResponse(
                RepairMessage(guid, bot.Health * 100 / definition.MaxHealth)
              )
            )
          }

        case CommonMessages.Use(user, Some(item: Tool)) if item.Definition == GlobalDefinitions.bank =>
          val originalArmor = bot.Armor
          val definition = bot.Definition
          if (
            bot.MaxArmor > 0 && originalArmor < bot.MaxArmor &&
            user.Faction == bot.Faction &&
            item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
            Vector3.Distance(user.Position, bot.Position) < definition.RepairDistance
          ) {
            val zone = bot.Zone
            val events = zone.AvatarEvents
            val uname = user.Name
            val guid = bot.GUID
            if (!(bot.isMoving || user.isMoving)) { //only allow stationary repairs
              val newArmor = bot.Armor =
                originalArmor + Repairable.applyLevelModifier(user, item, RepairToolValue(item)).toInt + definition.RepairMod
              val magazine = item.Discharge()
              events ! MessageEnvelope(
                uname,
                SendResponse(
                  InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)
                )
              )
              events ! MessageEnvelope(zone.id, PlanetsideAttribute(guid, 4, bot.Armor))
              bot.LogActivity(
                RepairFromEquipment(
                  PlayerSource(user),
                  GlobalDefinitions.bank,
                  newArmor - originalArmor
                )
              )
            }
            //progress bar remains visible for all repair attempts
            events ! MessageEnvelope(
              uname,
              SendResponse(RepairMessage(guid, bot.Armor * 100 / bot.MaxArmor))
            )
          }

        case _ => ;
      }

  def Disabled: Receive = {
    case AvatarBot.Spawn() =>
      spawn()

    case AvatarBot.Die(Some(reason)) =>
      dieWithReason(reason)

    case AvatarBot.Die(None) =>
      suicide()

    case AvatarBot.Release() =>
      release()

    case _ => ;
  }

  override protected def PerformDamage(
    target: Target,
    applyDamageTo: Output
  ): Unit = {
    if (bot.isAlive) {
      val originalHealth    = bot.Health
      val originalArmor     = bot.Armor
      val originalStamina   = bot.stamina
      val cause             = applyDamageTo(bot)
      val health            = bot.Health
      val armor             = bot.Armor
      val stamina           = bot.stamina
      val damageToHealth    = originalHealth - health
      val damageToArmor     = originalArmor - armor
      val damageToStamina   = originalStamina - stamina
      HandleDamage(bot, cause, damageToHealth, damageToArmor, damageToStamina)
      if (damageToHealth > 0 || damageToArmor > 0 || damageToStamina > 0) {
        damageLog.info(
          s"${bot.Name}-infantry: BEFORE=$originalHealth/$originalArmor/$originalStamina, AFTER=$health/$armor/$stamina, CHANGE=$damageToHealth/$damageToArmor/$damageToStamina"
        )
      }
    }
  }

  /**
    * na
    * @param target na
    */
  def HandleDamage(
                    target: AvatarBot,
                    cause: DamageResult,
                    damageToHealth: Int,
                    damageToArmor: Int,
                    damageToStamina: Int
                  ): Unit = {
    //always do armor update
    if (damageToArmor > 0) {
      val zone = target.Zone
      zone.AvatarEvents ! MessageEnvelope(
        zone.id,
        PlanetsideAttribute(target.GUID, 4, target.Armor)
      )
    }
    //choose
    if (target.Health > 0) {
      //alive, take damage/update
      DamageAwareness(target, cause, damageToHealth, damageToArmor, damageToStamina)
    } else {
      //ded
      DestructionAwareness(target, cause)
    }
  }

  def DamageAwareness(
                       target: AvatarBot,
                       cause: DamageResult,
                       damageToHealth: Int,
                       damageToArmor: Int,
                       damageToStamina: Int
                     ): Unit = {
    val targetGUID            = target.GUID
    val zone                  = target.Zone
    val zoneId                = zone.id
    val events                = zone.AvatarEvents
    val health                = target.Health
    var announceConfrontation = damageToArmor > 0
    //special effects
    if (Damageable.CanJammer(target, cause.interaction)) {
      TryJammerEffectActivate(target, cause)
    }
    val aggravated: Boolean = TryAggravationEffectActivate(cause) match {
      case Some(aggravation) =>
        StartAuraEffect(aggravation.effect_type, aggravation.timing.duration)
        announceConfrontation = true //useful if initial damage (to anything) is zero
        //initial damage for aggravation, but never treat as "aggravated"
        false
      case _ =>
        cause.interaction.cause.source.Aggravated.nonEmpty
    }
    //log historical event (always)
    target.LogActivity(cause)
    //stat changes
    if (damageToStamina > 0) {
      target.stamina = math.max(0, target.stamina - damageToStamina)
      announceConfrontation = true //TODO should we?
    }
    if (damageToHealth > 0) {
      events ! MessageEnvelope(zoneId, PlanetsideAttribute(targetGUID, 0, health))
      announceConfrontation = true
    }
    val countableDamage = damageToHealth + damageToArmor
    if(announceConfrontation) {
      if (aggravated) {
        events ! MessageEnvelope(zoneId, targetGUID, SendResponse(AggravatedDamageMessage(targetGUID, countableDamage)))
      } else {
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause)
      }
    }
  }

  /**
    * The bot has lost all his vitality and must be killed.<br>
    * <br>
    * Shift directly into a state of being dead on the client by setting health to zero points,
    * whereupon the bot will perform a dramatic death animation.
    * Stamina is also set to zero points.
    * Do not move or completely destroy the `AvatarBot` object as its coordinates of death will be important.<br>
    * <br>
    * @param target na
    * @param cause na
    */
  def DestructionAwareness(target: AvatarBot, cause: DamageResult): Unit = {
    val bot_guid     = target.GUID
    val pos          = target.Position
    val zone         = target.Zone
    val events       = zone.AvatarEvents
    val nameChannel  = target.Name
    val zoneChannel  = zone.id
    target.Die
    //aura effects cancel
    EndAllEffects()
    //aggravation cancel
    EndAllAggravation()
    //unjam
    CancelJammeredSound(target)
    super.CancelJammeredStatus(target)
    //no stamina
    target.stamina = 0

    //log historical event
    target.LogActivity(cause)
    //log message
    cause.adversarial match {
      case Some(a) =>
        damageLog.info(s"${a.defender.Name} was killed by ${a.attacker.Name}")
        events ! MessageEnvelope(
          zoneChannel,
          AvatarAction.DestroyDisplay(a.attacker, a.defender, a.implement)
        )
      case _ =>
        damageLog.info(s"${bot.Name} killed ${bot.Sex.pronounObject}self")
        events ! MessageEnvelope(zoneChannel, AvatarAction.DestroyDisplay(cause.interaction.target, cause.interaction.target, 0))
    }

    events ! MessageEnvelope(nameChannel, bot_guid, AvatarAction.Killed(cause, None)) //align client interface fields with state
    events ! MessageEnvelope(zoneChannel, PlanetsideAttribute(bot_guid, 0, 0)) //health
    val attribute = DamageableEntity.attributionTo(cause, target.Zone, bot_guid)
    events ! MessageEnvelope(
      nameChannel,
      bot_guid,
      SendResponse(
        DestroyMessage(bot_guid, attribute, bot_guid, pos)
      ) //how many players get this message?
    )

    context.self ! AvatarBot.Release()
  }

  def suicide() : Unit = {
    if (bot.Health > 0 || bot.isAlive) {
      PerformDamage(
        bot,
        DamageInteraction(
          PlayerSource(bot),
          SuicideReason(),
          bot.Position
        ).calculate()
      )
    }
  }

  private def spawn(): Unit = {
    bot.Spawn()
    bot.Zone.Population ! Zone.Bots.Spawn(bot)
    canEmote = true
    canRotate = true
    scheduler.scheduleAtFixedRate(new Runnable() { override def run(): Unit = tickLogic() }, 0, 250, TimeUnit.MILLISECONDS)
  }

  private def dieWithReason(reason: DamageInteraction): Unit = {
    if (bot.isAlive) {
      //primary death
      val health = bot.Health
      val psource = PlayerSource(bot)
      bot.Health = 0
      HandleDamage(
        bot,
        DamageResult(psource, psource.copy(health = 0), reason),
        health,
        damageToArmor = 0,
        damageToStamina = 0
      )
      damageLog.info(s"${bot.Name}-infantry: dead by explicit reason - ${reason.cause.resolution}")
    }
  }

  private def release(): Unit = {
    bot.Zone.Population ! Zone.Bots.Release(bot)
    spawnerActor ! ShootingRangeTargetSpawner.InfantryTargetReleased(bot)
    scheduler.shutdown()
  }

  private def performEmote(): Unit = {
    val zone = bot.Zone
    zone.blockMap.sector(bot).livePlayerList.collect { t =>
      zone.LocalEvents ! MessageEnvelope(t.Name, SendResponse(TriggerBotAction(bot.GUID)))
    }
  }

  private def tickLogic(): Unit = {
    val zone = bot.Zone
    if (!bot.Destroyed && zone.AllPlayers.nonEmpty) {
      bot.zoneInteractions()
      val rotateRNG = Random.nextDouble()
      if (canRotate) {
        if (rotateRNG > 0.95) {
          val amount = 5f + Random.nextInt(10)
          val finalRotation = if (Random.nextBoolean()) -amount else amount
          bot.Orientation = Vector3(bot.Orientation.x, bot.Orientation.y, (bot.Orientation.z + finalRotation) % 360)
          canRotate = false
          //rotation cooldown
          context.system.scheduler.scheduleOnce(
            2.seconds,
            new Runnable() { override def run(): Unit = if (!bot.Destroyed) canRotate = true }
          )
        }
      }
      zone.AvatarEvents ! MessageEnvelope(
        zone.id,
        bot.GUID,
        AvatarAction.PlayerState(
          bot.Position,
          bot.Velocity,
          bot.Orientation.z,
          bot.Orientation.y,
          bot.FacingYawUpper,
          0,
          bot.Crouching,
          bot.Jumping,
          jump_thrust = false,
            is_cloaked = bot.Cloaked,
            spectator = false,
            weaponInHand = false
          )
      )
      if (canEmote) {
        val emoteRNG = Random.nextDouble()
        if (emoteRNG > 0.98) {
          performEmote()
          canEmote = false
          //emote cooldown
          context.system.scheduler.scheduleOnce(
            5.seconds,
            new Runnable() { override def run(): Unit = if (!bot.Destroyed) canEmote = true }
          )
        }
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
  override def StartJammeredSound(target: Any, dur: Int): Unit =
    target match {
      case obj: AvatarBot if !jammedSound =>
        obj.Zone.AvatarEvents ! MessageEnvelope(
          obj.Zone.id,
          PlanetsideAttribute(obj.GUID, 27, 1)
        )
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
  override def StartJammeredStatus(target: Any, dur: Int): Unit = {
    super.StartJammeredStatus(target, dur)
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    super.CancelJammeredStatus(target)
  }

  /**
    * Stop the jammered buzzing.
    * @see `JammableBehavior.CancelJammeredSound`
    * @param target an object that can be affected by the jammered status
    */
  override def CancelJammeredSound(target: Any): Unit =
    target match {
      case obj: AvatarBot if jammedSound =>
        obj.Zone.AvatarEvents ! MessageEnvelope(
          obj.Zone.id,
          PlanetsideAttribute(obj.GUID, 27, 0)
        )
        super.CancelJammeredSound(obj)
      case _ => ;
    }

  def RepairToolValue(item: Tool): Float = {
    item.AmmoSlot.Box.Definition.repairAmount +
    (if (bot.ExoSuit != ExoSuitType.MAX) {
      item.FireMode.Add.Damage0
    }
    else {
      item.FireMode.Add.Damage3
    })
  }

  def UpdateAuraEffect(target: AuraEffectBehavior.Target) : Unit = {
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + AvatarBotActor.auraEffectToAttributeValue(_))
    zone.AvatarEvents ! MessageEnvelope(zone.id, PlanetsideAttribute(target.GUID, 54, value))
  }
}

object AvatarBotActor {
  /**
    * Transform an applicable Aura effect into its `PlanetsideAttributeMessage` value.
    * @see `Aura`
    * @see `PlanetsideAttributeMessage`
    * @param effect the aura effect
    * @return the attribute value for that effect
    */
  private def auraEffectToAttributeValue(effect: Aura): Int = effect match {
    case Aura.Plasma => 1
    case Aura.Comet  => 2
    case Aura.Napalm => 4
    case Aura.Fire   => 8
    case _           => 0
  }
}
