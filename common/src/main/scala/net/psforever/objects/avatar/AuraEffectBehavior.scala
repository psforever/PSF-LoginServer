// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.Player
import net.psforever.objects.ballistics.{AggravatedDamage, AggravatedInfo, ResolvedProjectile}
import net.psforever.objects.vital.DamageType

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait AuraEffectBehavior {
  _ : Actor =>
  private var activeEffectIndex : Long = 0
  private var effectsToIds : mutable.HashMap[Aura.Value, List[Long]]   = mutable.HashMap.empty[Aura.Value, List[Long]]
  private var idsToTimers : mutable.LongMap[Cancellable]               = mutable.LongMap.empty[Cancellable]
  private var idsToEntries : mutable.LongMap[AuraEffectBehavior.Entry] = mutable.LongMap.empty[AuraEffectBehavior.Entry]

  def AuraTargetObject : Player

  val auraBehavior : Receive = {
    case AuraEffectBehavior.Aggravate(id, 0, 0) =>
      CancelEffectTimer(id)
      PerformCleanupEffect(id)

    case AuraEffectBehavior.Aggravate(id, 0, leftoverTime) =>
      PerformAggravationAndRetimeEvent(id, iteration = 0, Some(leftoverTime), leftoverTime = 0)

    case AuraEffectBehavior.Aggravate(id, iteration, leftover) => ;
      PerformAggravationAndRetimeEvent(id, iteration - 1, None, leftover)
  }

  def PerformAggravationAndRetimeEvent(id : Long, iteration : Int, time : Option[Long], leftoverTime : Long) : Unit = {
    CancelEffectTimer(id)
    idsToEntries.get(id) match {
      case Some(entry) =>
        //TODO stuff ...
        idsToTimers += id -> context.system.scheduler.scheduleOnce(
          time.getOrElse(entry.effect.infliction_rate) milliseconds,
          self,
          AuraEffectBehavior.Aggravate(id, iteration, leftoverTime)
        )
      case _ =>
        PerformCleanupEffect(id)
    }
  }

  def CancelEffectTimer(id : Long) : Unit = {
    idsToTimers.remove(id) match {
      case Some(timer) => timer.cancel
      case _ => ;
    }
  }

  def PerformCleanupEffect(id : Long) : Unit = {
    CleanupEffect(id) match {
      case Aura.None => ;
      case _ => UpdateAggravatedEffect(AuraTargetObject)
    }
  }

  def TryAggravationEffect(data : ResolvedProjectile) : Unit = {
    data.projectile.profile.Aggravated match {
      case Some(damage)
        if data.projectile.profile.ProjectileDamageType == DamageType.Aggravated && damage.effect_type != Aura.None =>
        TryAggravationEffect(damage, data)
      case _ => ;
    }
  }

  private def TryAggravationEffect(aggravation : AggravatedDamage, data : ResolvedProjectile) : Unit = {
    val effect = aggravation.effect_type
    val obj = AuraTargetObject
    if(obj.Aura.contains(effect)) { //TODO cumulative?
      SetupAggravationEntry(aggravation)
    }
    else if(obj.Aura.diff(obj.AddEffectToAura(effect)).contains(effect)) {
      SetupAggravationEntry(aggravation)
      UpdateAggravatedEffect(obj)
    }
  }

  private def SetupAggravationEntry(aggravation : AggravatedDamage) : Unit = {
    val effect = aggravation.effect_type
    aggravation.info.foreach { infos =>
      //get unused id
      val id = activeEffectIndex
      activeEffectIndex += 1
      //pair aura effect with id
      effectsToIds.get(effect) match {
        case None | Some(Nil) => effectsToIds += effect -> List(id)
        case Some(list) => effectsToIds -> (list :+ id)
      }
      //pair id with entry
      idsToEntries += id -> AuraEffectBehavior.Entry(id, infos, aggravation, 0)
      //pair id with timer
      val iterations = (aggravation.duration / infos.infliction_rate).toInt
      val leftoverTime = aggravation.duration % infos.infliction_rate
      idsToTimers += id -> context.system.scheduler.scheduleOnce(infos.infliction_rate milliseconds, self, AuraEffectBehavior.Aggravate(id, iterations, leftoverTime))
    }
  }

  def CleanupEffect(id : Long) : Aura.Value = {
    //remove and cancel timer
    idsToTimers.remove(id) match {
      case Some(timer) => timer.cancel
      case _ => ;
    }
    //remove entry and cache effect
    val out = idsToEntries.remove(id) match {
      case Some(entry) => entry.aggravation.effect_type
      case _ => Aura.None
    }
    //remove id and, if now unsupported, effect
    (effectsToIds.get(out) match {
      case Some(list) => (out, list.filterNot(_ == id))
      case _ => (Aura.None, Nil)
    }) match {
      case (Aura.None, _) =>
        Aura.None
      case (effect, Nil) =>
        effectsToIds.remove(effect)
        effect
      case (effect, list) =>
        effectsToIds += effect -> list
        Aura.None
    }
  }

  def EndAllEffects() : Unit = {
    idsToEntries.clear
    idsToTimers.values.foreach { _.cancel }
    idsToTimers.clear
    effectsToIds.clear
  }

  def EndAllEffectsAndUpdate() : Unit = {
    EndAllEffects()
    UpdateAggravatedEffect(AuraTargetObject)
  }

  def UpdateAggravatedEffect(target : Player) : Unit = {
    import services.avatar.{AvatarAction, AvatarServiceMessage}
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + _.id)
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(target.GUID, 54, value))
  }
}

object AuraEffectBehavior {
  private case class Entry(id : Long, effect : AggravatedInfo, aggravation : AggravatedDamage, damage : Any)

  private case class Aggravate(id : Long, iterations : Int, leftover : Long)
}
