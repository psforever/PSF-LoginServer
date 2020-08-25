// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aura

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.Default
import net.psforever.objects.serverobject.PlanetSideServerObject

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * A mixin that governs the addition, display, and removal of aura particle effects
  * on a target with control agency.
  * @see `Aura`
  * @see `AuraContainer`
  * @see `PlayerControl`
  */
trait AuraEffectBehavior {
  _ : Actor =>
  /** active aura effects are monotonic, but the timer will be updated for continuing and cancelling effects as well<br>
    * only effects that are initialized to this mapping are approved for display on this target<br>
    * key - aura effect; value - the timer for that effect
    * @see `ApplicableEffect`
    */
  private val effectToTimer: mutable.HashMap[Aura, AuraEffectBehavior.Entry] = mutable.HashMap.empty[Aura, AuraEffectBehavior.Entry]

  def AuraTargetObject: AuraEffectBehavior.Target

  val auraBehavior: Receive = {
    case AuraEffectBehavior.StartEffect(effect, duration) =>
      StartAuraEffect(effect, duration)

    case AuraEffectBehavior.EndEffect(effect) =>
      EndAuraEffectAndUpdate(effect)

    case AuraEffectBehavior.EndAllEffects() =>
      EndAllEffectsAndUpdate()
  }

  /**
    * Only pre-apporved aura effects will be emitted by this target.
    * @param effect the aura effect
    */
  def ApplicableEffect(effect: Aura): Unit = {
    //create entry
    effectToTimer += effect -> AuraEffectBehavior.Entry()
  }

  /**
    * An aura particle effect is to be emitted by the target.
    * If the effect was not previously applied to the target in an ongoing manner,
    * animate it appropriately.
    * @param effect the effect to be emitted
    * @param duration for how long the effect will be emitted
    * @return the active effect index number
    */
  def StartAuraEffect(effect: Aura, duration: Long): Unit = {
    val obj = AuraTargetObject
    val auraEffectsBefore = obj.Aura.size
    if(StartAuraTimer(effect, duration) && obj.AddEffectToAura(effect).size > auraEffectsBefore) {
      //new effect; update visuals
      UpdateAuraEffect(AuraTargetObject)
    }
  }

  /**
    * As long as the effect has been approved for this target,
    * the timer will either start if it is stopped or has never been started,
    * or the timer will stop and be recreated with the new duration if is currently running for a shoreter amount of time.
    * @param effect the effect to be emitted
    * @param duration for how long the effect will be emitted
    * @return `true`, if the timer was started or restarted;
    *        `false`, otherwise
    */
  private def StartAuraTimer(effect: Aura, duration: Long): Boolean = {
    //pair aura effect with entry
    (effectToTimer.get(effect) match {
      case Some(timer) if timer.start + timer.duration < System.currentTimeMillis() + duration =>
        timer.cancel()
        Some(effect)
      case _ =>
        None
    }) match {
      case None =>
        false
      case Some(_) =>
        //retime
        effectToTimer(effect) = AuraEffectBehavior.Entry(
          duration,
          context.system.scheduler.scheduleOnce(duration milliseconds, self, AuraEffectBehavior.EndEffect(effect))
        )
        true
    }
  }

  /**
    * Stop the target entity from emitting the aura particle effect, if it currently is.
    * @param effect the target effect
    * @return `true`, if the effect was being emitted but has been stopped
    *        `false`, if the effect was not approved or is not being emitted
    */
  def EndAuraEffect(effect: Aura): Boolean = {
    effectToTimer.get(effect) match {
      case Some(timer) if !timer.isCancelled =>
        timer.cancel()
        //effectToTimer(effect) = Default.Cancellable
        AuraTargetObject.RemoveEffectFromAura(effect)
        true
      case _ =>
        false
    }
  }

  /**
    * Stop the target entity from emitting all aura particle effects.
    */
  def EndAllEffects() : Unit = {
    effectToTimer.keysIterator.foreach { effect =>
      effectToTimer(effect).cancel()
      //effectToTimer(effect) = Default.Cancellable
    }
    val obj = AuraTargetObject
    obj.Aura.foreach { obj.RemoveEffectFromAura }
  }

  /**
    * Stop the target entity from emitting the aura particle effect, if it currently is.
    * If the effect has been stopped, animate the new particle effect state.
    */
  def EndAuraEffectAndUpdate(effect: Aura) : Unit = {
    if(EndAuraEffect(effect)) {
      UpdateAuraEffect(AuraTargetObject)
    }
  }

  /**
    * Stop the target entity from emitting all aura particle effects.
    * Animate the new particle effect state.
    */
  def EndAllEffectsAndUpdate() : Unit = {
    EndAllEffects()
    UpdateAuraEffect(AuraTargetObject)
  }

  /**
    * Is the target entity emitting the aura effect?
    * @param effect the effect being tested
    * @return `true`, if the effect is currently being emitted;
    *        `false`, otherwise
    */
  def TestForEffect(effect: Aura): Boolean = {
    effectToTimer.get(effect) match {
      case None => false
      case Some(timer) => timer.isCancelled
    }
  }

  /**
    * An override callback to display aura effects emitted.
    * @param target the entity from which the aura effects are being emitted
    */
  def UpdateAuraEffect(target: AuraEffectBehavior.Target) : Unit
}

object AuraEffectBehavior {
  type Target = PlanetSideServerObject with AuraContainer

  case class Entry(duration: Long, timer: Cancellable) extends Cancellable {
    val start: Long = System.currentTimeMillis()

    override def isCancelled : Boolean = timer.isCancelled

    override def cancel(): Boolean = timer.cancel()
  }

  object Entry {
    def apply(): Entry = Entry(0, Default.Cancellable)
  }

  final case class StartEffect(effect: Aura, duration: Long)

  final case class EndEffect(aura: Aura)

  final case class EndAllEffects()
}
