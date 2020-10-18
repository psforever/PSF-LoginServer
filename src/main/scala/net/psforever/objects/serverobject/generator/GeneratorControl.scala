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
  var imminentExplosion: Boolean   = false
  var queuedExplosion: Cancellable = Default.Cancellable
  var alarmCooldownPeriod: Boolean = false
  private[this] val log = org.log4s.getLogger

  def receive: Receive = withNtu

  val commonBehavior: Receive =
    checkBehavior
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)
      .orElse {
        case GeneratorControl.UnderThreatAlarm() =>
          if (!alarmCooldownPeriod) {
            alarmCooldownPeriod = true
            GeneratorControl.BroadcastGeneratorEvent(gen, GeneratorControl.Event.UnderAttack)
            queuedExplosion = context.system.scheduler.scheduleOnce(delay = 5 seconds, self, GeneratorControl.AlarmReset())
          }

        case GeneratorControl.AlarmReset() =>
          alarmCooldownPeriod = false
      }

  def withNtu: Receive =
    commonBehavior
      .orElse {
        case GeneratorControl.Destabilized() =>
          imminentExplosion = true
          GeneratorControl.BroadcastGeneratorEvent(gen, GeneratorControl.Event.Overloaded)
          queuedExplosion.cancel()
          queuedExplosion = context.system.scheduler.scheduleOnce(10 seconds, self, GeneratorControl.GeneratorExplodes())

        case GeneratorControl.GeneratorExplodes() =>
          //TODO this only works with projectiles right now!
          val zone = gen.Zone
          gen.Health = 0
          super.DestructionAwareness(gen, gen.LastShot.get)
          gen.Condition = PlanetSideGeneratorState.Destroyed
          GeneratorControl.UpdateOwner(gen, Some("destroyed"))
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
          GeneratorControl.UpdateOwner(gen, Some("restored"))
          GeneratorControl.BroadcastGeneratorEvent(gen, GeneratorControl.Event.Online)

        case _ => ;
      }

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
        GeneratorControl.UpdateOwner(gen, Some("destroyed"))
        gen.ClearHistory()

      case _ =>
    }

  override protected def CanPerformRepairs(obj: Target, player: Player, item: Tool): Boolean = {
    !imminentExplosion && super.CanPerformRepairs(obj, player, item)
  }

  override protected def WillAffectTarget(target: Target, damage: Int, cause: ResolvedProjectile): Boolean = {
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
    if (!target.Destroyed) {
      target.Health = 1 //temporary
      GeneratorControl.UpdateOwner(gen, Some("overloaded"))
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
    self ! GeneratorControl.Restored()
  }

  override def withNtuSupplyCallback() : Unit = {
    context.become(withNtu)
    super.withNtuSupplyCallback()
    if(!gen.Destroyed) {
      self ! GeneratorControl.Restored()
    }
  }

  override def noNtuSupplyCallback() : Unit = {
    //auto-repair must stop naturally
    context.become(withoutNtu)
    super.noNtuSupplyCallback()
    if(!gen.Destroyed) {
      GeneratorControl.UpdateOwner(gen, Some("overloaded"))
    }
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
    val UnderAttack = Value(15)
    val Critical = Value(0)
    val Overloaded = Value(16)
    val Online = Value(17)
  }

  /**
    * na
    * @param obj na
    */
  private def UpdateOwner(obj: Generator, data: Option[Any] = None): Unit = {
    obj.Owner match {
      case b: Building => b.Actor ! BuildingActor.AmenityStateChange(obj, data)
      case _           => ;
    }
  }

  /**
    * na
    * @param target the generator
    * @param event the action code for the event
    */
  private def BroadcastGeneratorEvent(target: Generator, event: Event.Value): Unit = {
    target.Owner match {
      case b: Building =>
        val events = target.Zone.AvatarEvents
        val msg = event match {
          case Event.Critical => AvatarAction.PlanetsideAttributeToAll(b.GUID, 46, 1) //critical status warning
          case _ =>              AvatarAction.GenericObjectAction(Service.defaultPlayerGUID, b.GUID, event.id)
        }
        b.PlayersInSOI.foreach { player =>
          events ! AvatarServiceMessage(player.Name, msg)
        }
      case _ => ;
    }
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
        GeneratorControl.UpdateOwner(target, Some("critical"))
      }
      //the generator is under attack
      target.Actor ! UnderThreatAlarm()
    }
  }
}
