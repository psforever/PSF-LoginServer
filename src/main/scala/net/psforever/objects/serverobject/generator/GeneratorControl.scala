// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, Player, Tool}
import net.psforever.objects.ballistics._
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, Repairable, RepairableEntity}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.vital.DamageFromExplosion
import net.psforever.packet.game.TriggerEffectMessage
import net.psforever.types.{PlanetSideGeneratorState, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * An `Actor` that handles messages being dispatched to a specific `Generator`.
  *
  * @param gen the `Generator` object being governed
  */
class GeneratorControl(gen: Generator)
    extends Actor
    with FactionAffinityBehavior.Check
    with DamageableEntity
    with RepairableEntity
    with AmenityAutoRepair {
  def FactionObject      = gen
  def DamageableObject   = gen
  def RepairableObject   = gen
  def AutoRepairObject   = gen
  /** flagged to explode after some time */
  var imminentExplosion: Boolean   = false
  /** explode when this timer completes */
  var queuedExplosion: Cancellable = Default.Cancellable
  /** when damaged, announce that damage was dealt on a schedule */
  var alarmCooldown: Cancellable   = Default.Cancellable

  /*
  behavior of the generator piggybacks from the logic used in `AmenityAutoRepair`
  AAR splits its logic based on whether or not it has detected a source of nanite transfer units (NTU)
  this amenity is the bridge between NTU and facility power so it leverages that logic
  it is split between when detecting ntu and when starved for ntu
   */

  def receive: Receive = withNtu

  /** behavior that is valid for both "with-ntu" and "without-ntu" */
  val commonBehavior: Receive =
    checkBehavior
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)
      .orElse {
        case GeneratorControl.UnderThreatAlarm() =>
          //alert to damage and block other damage alerts for a time
          if (alarmCooldown.isCancelled) {
            GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.UnderAttack))
            alarmCooldown.cancel()
            alarmCooldown = context.system.scheduler.scheduleOnce(delay = 5 seconds, self, GeneratorControl.AlarmReset())
          }

        case GeneratorControl.AlarmReset() =>
          //clear the blocker for alerting to damage
          alarmCooldown = Default.Cancellable
      }

  /*
  when NTU is detected,
  the generator can be properly destabilized and explode
  the generator can be repaired to operational status and power the facility in which it is installed
   */
  def withNtu: Receive =
    commonBehavior
      .orElse {
        case GeneratorControl.Destabilized() =>
          imminentExplosion = true
          //the generator's condition is technically destroyed, but avoid official reporting until the explosion
          gen.Condition = PlanetSideGeneratorState.Destroyed
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destabilized))
          queuedExplosion.cancel()
          queuedExplosion = context.system.scheduler.scheduleOnce(10 seconds, self, GeneratorControl.GeneratorExplodes())

        case GeneratorControl.GeneratorExplodes() =>
          //TODO this only works with projectiles right now!
          val zone = gen.Zone
          gen.Health = 0
          super.DestructionAwareness(gen, gen.LastShot.get)
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destroyed))
          //kaboom
          zone.AvatarEvents ! AvatarServiceMessage(
            zone.id,
            AvatarAction.SendResponse(
              Service.defaultPlayerGUID,
              TriggerEffectMessage(gen.GUID, "explosion_generator", None, None)
            )
          )
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false
          //kill everyone within 14m
          gen.Owner match {
            case b: Building =>
              val genDef = gen.Definition
              b.PlayersInSOI.collect {
                case player if player.isAlive && Vector3.DistanceSquared(player.Position, gen.Position) < 196 =>
                  player.History(DamageFromExplosion(PlayerSource(player), genDef))
                  player.Actor ! Player.Die()
              }
            case _ => ;
          }
          gen.ClearHistory()

        case GeneratorControl.Restored() =>
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Online))

        case _ => ;
      }

  /*
  when ntu is not expected,
  the generator can still be destroyed but will not explode
  handles the possibility that ntu was lost during an ongoing destabilization and cancels the explosion
   */
  def withoutNtu: Receive =
    commonBehavior
      .orElse {
        case GeneratorControl.GeneratorExplodes() =>
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false

        case GeneratorControl.Destabilized() =>
          //if the generator is destabilized but has no ntu, it will not explode
          gen.Health = 0
          super.DestructionAwareness(gen, gen.LastShot.get)
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false
          gen.Condition = PlanetSideGeneratorState.Destroyed
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destroyed))
          gen.ClearHistory()

        case _ =>
      }

  override protected def CanPerformRepairs(obj: Target, player: Player, item: Tool): Boolean = {
    //if an explosion is queued, disallow repairs
    !imminentExplosion && super.CanPerformRepairs(obj, player, item)
  }

  override protected def WillAffectTarget(target: Target, damage: Int, cause: ResolvedProjectile): Boolean = {
    //if an explosion is queued, disallow further damage
    !imminentExplosion && super.WillAffectTarget(target, damage, cause)
  }

  override protected def DamageAwareness(target: Target, cause: ResolvedProjectile, amount: Any): Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
    val damageTo = amount match {
      case a: Int => a
      case _ => 0
    }
    GeneratorControl.DamageAwareness(gen, cause, damageTo)
  }

  override protected def DestructionAwareness(target: Target, cause: ResolvedProjectile): Unit = {
    tryAutoRepair()
    //if the target is already destroyed, do not let it be destroyed again
    if (!target.Destroyed) {
      target.Health = 1 //temporary
      GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Offline))
      self ! GeneratorControl.Destabilized()
    }
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def Restoration(obj: Repairable.Target): Unit = {
    super.Restoration(obj)
    gen.Condition = PlanetSideGeneratorState.Normal
    GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Normal))
    self ! GeneratorControl.Restored()
  }

  override def withNtuSupplyCallback() : Unit = {
    context.become(withNtu)
    super.withNtuSupplyCallback()
    //if not destroyed when a source of ntu is detected, restore facility power
    if(!gen.Destroyed) {
      self ! GeneratorControl.Restored()
    }
  }

  override def noNtuSupplyCallback() : Unit = {
    //auto-repair must stop naturally
    context.become(withoutNtu)
    super.noNtuSupplyCallback()
    //if not destroyed when cutoff from a source of ntu, stop facility power generation
    if(!gen.Destroyed) {
      GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Offline))
    }
    //can any explosion (see withoutNtu->GenweratorControl.Destabilized)
    if(!queuedExplosion.isCancelled) {
      queuedExplosion.cancel()
      self ! GeneratorControl.Destabilized()
    }
  }
}

object GeneratorControl {
  /**
    * na
    */
  private case class Destabilized()

  /**
    * na
    */
  private case class GeneratorExplodes()

  /**
    * na
    */
  private case class UnderThreatAlarm()

  /**
    * na
    */
  private case class AlarmReset()

  /**
    * na
    */
  private case class Restored()

  /**
    * na
    */
  object Event extends Enumeration {
    val
    Critical, //PlanetSideGeneratorState.Critical
    UnderAttack,
    Destabilized,
    Destroyed, //PlanetSideGeneratorState.Destroyed
    Offline,
    Normal, //PlanetSideGeneratorState.Normal
    Online
    = Value
  }

  /**
    * Send a message back to the owner for which this `Amenity` entity is installed.
    * @param obj the entity doing the self-reporting
    * @param data optional information that indicates the nature of the state change
    */
  private def UpdateOwner(obj: Generator, data: Option[Any] = None): Unit = {
    obj.Owner.Actor ! BuildingActor.AmenityStateChange(obj, data)
  }

  /**
    * If not destroyed, it will complain about being damaged.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    * @param amount the amount of damage
    */
  def DamageAwareness(target: Generator, cause: ResolvedProjectile, amount: Int): Unit = {
    if (!target.Destroyed) {
      val health: Float = target.Health.toFloat
      val max: Float    = target.MaxHealth.toFloat
      if (target.Condition != PlanetSideGeneratorState.Critical && health / max < 0.51f) { //becoming critical
        target.Condition = PlanetSideGeneratorState.Critical
        GeneratorControl.UpdateOwner(target, Some(GeneratorControl.Event.Critical))
      }
      //the generator is under attack
      target.Actor ! UnderThreatAlarm()
    }
  }
}
