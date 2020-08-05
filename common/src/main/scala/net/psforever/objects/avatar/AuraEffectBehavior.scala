// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.Player
import net.psforever.objects.ballistics._
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.vital.{DamageType, Vitality}
import net.psforever.types.Vector3

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait AuraEffectBehavior {
  _ : Actor with Damageable =>
  private var activeEffectIndex: Long = 0
  private val effectToEntryId: mutable.HashMap[Aura.Value, List[Long]]   =
    mutable.HashMap.empty[Aura.Value, List[Long]]
  private val entryIdToTimer: mutable.LongMap[Cancellable]               =
    mutable.LongMap.empty[Cancellable]
  private val entryIdToEntry: mutable.LongMap[AuraEffectBehavior.Entry] =
    mutable.LongMap.empty[AuraEffectBehavior.Entry]

  def AuraTargetObject : Player

  val auraBehavior : Receive = {
    case AuraEffectBehavior.Aggravate(id, 0, 0) =>
      CancelEffectTimer(id)
      PerformCleanupEffect(id)

    case AuraEffectBehavior.Aggravate(id, 0, leftoverTime) =>
      RemoveEffectEntry(id)
      RetimeEvent(id, iteration = 0, Some(leftoverTime), leftoverTime = 0)

    case AuraEffectBehavior.Aggravate(id, iteration, leftover) => ;
      RetimeEventAndPerformAggravation(id, iteration - 1, None, leftover)
  }

  private def RetimeEvent(
                           id: Long,
                           iteration: Int,
                           time: Option[Long],
                           leftoverTime: Long
                         ): Option[AuraEffectBehavior.Entry] = {
    CancelEffectTimer(id)
    entryIdToEntry.get(id) match {
      case Some(oldEntry) =>
        val target = SourceEntry(AuraTargetObject)
        val entry = PairIdWithAggravationEntry(
          id,
          oldEntry.effect,
          oldEntry.retime,
          oldEntry.data,
          target,
          target.Position - oldEntry.data.target.Position
        )
        entryIdToTimer += id -> context.system.scheduler.scheduleOnce(
          time.getOrElse(entry.retime) milliseconds,
          self,
          AuraEffectBehavior.Aggravate(id, iteration, leftoverTime)
        )
        Some(entry)
      case _ =>
        PerformCleanupEffect(id)
        None
    }
  }

  private def RetimeEventAndPerformAggravation(id: Long, iteration: Int, time: Option[Long], leftoverTime: Long) : Unit = {
    RetimeEvent(id, iteration, time, leftoverTime) match {
      case Some(entry) =>
        PerformAggravation(entry)
      case _  => ;
    }
  }

  def CancelEffectTimer(id: Long) : Unit = {
    entryIdToTimer.remove(id) match {
      case Some(timer) => timer.cancel
      case _ => ;
    }
  }

  def PerformCleanupEffect(id: Long) : Unit = {
    CleanupEffect(id) match {
      case Aura.None => ;
      case _ => UpdateAggravatedEffect(AuraTargetObject)
    }
  }

  def TryAggravationEffect(data: ResolvedProjectile) : Unit = {
    data.projectile.profile.Aggravated match {
      case Some(damage)
        if data.projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated) &&
          damage.effect_type != Aura.None =>
        TryAggravationEffect(damage, data)
      case _ => ;
    }
  }

  private def TryAggravationEffect(aggravation: AggravatedDamage, data: ResolvedProjectile) : Unit = {
    val effect = aggravation.effect_type
    val obj = AuraTargetObject
    if(CheckForUniqueUnqueuedProjectile(data.projectile)) {
      val auraEffects = obj.Aura
      if(auraEffects.contains(effect) && aggravation.cumulative_damage_degrade) { //TODO cumulative?
        SetupAggravationEntry(aggravation, data)
      }
      else if(obj.AddEffectToAura(effect).diff(auraEffects).contains(effect)) {
        SetupAggravationEntry(aggravation, data)
        UpdateAggravatedEffect(obj)
      }
    }
  }

  private def CheckForUniqueUnqueuedProjectile(projectile : Projectile) : Boolean = {
    !entryIdToEntry.values.exists { entry => entry.data.projectile eq projectile }
  }

  private def SetupAggravationEntry(aggravation: AggravatedDamage, data: ResolvedProjectile) : Unit = {
    val effect = aggravation.effect_type
    aggravation.info.find(_.damage_type == AuraEffectBehavior.basicDamageType(data.resolution)) match {
      case Some(info) =>
        //get unused id
        val id = activeEffectIndex
        activeEffectIndex += 1
        //pair aura effect with id
        effectToEntryId.get(effect) match {
          case None | Some(Nil) => effectToEntryId += effect -> List(id)
          case Some(list) => effectToEntryId -> (list :+ id)
        }
        //pair id with timer
        val inflictionRate = info.infliction_rate
        val iterations = (aggravation.duration / inflictionRate).toInt
        val leftoverTime = aggravation.duration % inflictionRate
        entryIdToTimer += id -> context.system.scheduler.scheduleOnce(inflictionRate milliseconds, self, AuraEffectBehavior.Aggravate(id, iterations, leftoverTime))
        //pair id with entry
        PairIdWithAggravationEntry(id, effect, inflictionRate, data, data.target, Vector3.Zero)
      case _ => ;
    }
  }

  private def PairIdWithAggravationEntry(
                                  id: Long,
                                  effect: Aura.Value,
                                  retime:Long,
                                  data: ResolvedProjectile,
                                  target: SourceEntry,
                                  offset: Vector3
                                ): AuraEffectBehavior.Entry = {
    val aggravatedDamageInfo = ResolvedProjectile(
      AuraEffectBehavior.burning(data.resolution),
      data.projectile,
      target,
      data.damage_model,
      data.hit_pos + offset
    )
    val entry = AuraEffectBehavior.Entry(id, effect, retime, aggravatedDamageInfo)
    entryIdToEntry += id -> entry
    entry
  }

  def RemoveEffectEntry(id: Long) : Aura.Value = {
    entryIdToEntry.remove(id) match {
      case Some(entry) =>
        entry.data.projectile.profile.Aggravated.get.effect_type
      case None =>
        effectToEntryId.find { case (_, values) => values.contains(id) } match {
          case Some((effect, _)) => effect
          case _ => Aura.None
        }
    }
  }

  def CleanupEffect(id: Long) : Aura.Value = {
    //remove and cancel timer
    entryIdToTimer.remove(id) match {
      case Some(timer) => timer.cancel
      case _ => ;
    }
    //remove entry and cache effect
    val out = RemoveEffectEntry(id)
    //remove id and, if now unsupported, effect
    (effectToEntryId.get(out) match {
      case Some(list) => (out, list.filterNot(_ == id))
      case _ => (Aura.None, Nil)
    }) match {
      case (Aura.None, _) =>
        Aura.None
      case (effect, Nil) =>
        AuraTargetObject.RemoveEffectFromAura(effect)
        effectToEntryId.remove(effect)
        effect
      case (effect, list) =>
        effectToEntryId += effect -> list
        Aura.None
    }
  }

  def EndAllEffects() : Unit = {
    entryIdToEntry.clear
    entryIdToTimer.values.foreach { _.cancel }
    entryIdToTimer.clear
    effectToEntryId.clear
    val obj = AuraTargetObject
    obj.Aura.foreach { obj.RemoveEffectFromAura }
  }

  def EndAllEffectsAndUpdate() : Unit = {
    EndAllEffects()
    UpdateAggravatedEffect(AuraTargetObject)
  }

  def UpdateAggravatedEffect(target: Player) : Unit = {
    import services.avatar.{AvatarAction, AvatarServiceMessage}
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + _.id)
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(target.GUID, 54, value))
  }

  private def PerformAggravation(entry: AuraEffectBehavior.Entry) : Unit = {
    TakesDamage.apply(Vitality.Damage(entry.data.damage_model.Calculate(entry.data)))
  }
}

object AuraEffectBehavior {
  private case class Entry(id: Long, effect: Aura.Value, retime: Long, data: ResolvedProjectile)

  private case class Aggravate(id: Long, iterations: Int, leftover: Long)

  private def burning(resolution: ProjectileResolution.Value): ProjectileResolution.Value = {
    resolution match {
      case ProjectileResolution.AggravatedDirect => ProjectileResolution.AggravatedDirectBurn
      case ProjectileResolution.AggravatedSplash => ProjectileResolution.AggravatedSplashBurn
      case _ => resolution
    }
  }

  private def basicDamageType(resolution: ProjectileResolution.Value): DamageType.Value = {
    resolution match {
      case ProjectileResolution.AggravatedDirect | ProjectileResolution.AggravatedDirectBurn =>
        DamageType.Direct
      case ProjectileResolution.AggravatedSplash | ProjectileResolution.AggravatedSplashBurn =>
        DamageType.Splash
      case _ =>
        DamageType.None
    }
  }
}
