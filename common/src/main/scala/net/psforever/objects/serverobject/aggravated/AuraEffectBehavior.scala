// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aggravated

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.ballistics._
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.vital.{DamageType, Vitality}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait AuraEffectBehavior {
  _ : Actor with Damageable =>
  private var activeEffectIndex: Long = 0
  private val effectToEntryId: mutable.HashMap[Aura, List[Long]] =
    mutable.HashMap.empty[Aura, List[Long]]
  private val entryIdToTimer: mutable.LongMap[Cancellable] =
    mutable.LongMap.empty[Cancellable]
  private val entryIdToEntry: mutable.LongMap[AuraEffectBehavior.Entry] =
    mutable.LongMap.empty[AuraEffectBehavior.Entry]

  def AuraTargetObject: AuraEffectBehavior.Target

  val auraBehavior: Receive = {
    case AuraEffectBehavior.Aggravate(id, 0, 0) =>
      CancelEffectTimer(id)
      PerformCleanupEffect(id)

    case AuraEffectBehavior.Aggravate(id, 0, leftoverTime) =>
      RemoveEffectEntry(id)
      RetimeEvent(id, iteration = 0, Some(leftoverTime), leftoverTime = 0)

    case AuraEffectBehavior.Aggravate(id, iteration, leftover) =>
      RetimeEventAndPerformAggravation(id, iteration, None, leftover)
  }

  private def RetimeEvent(
                           id: Long,
                           iteration: Int,
                           time: Option[Long],
                           leftoverTime: Long
                         ): Option[AuraEffectBehavior.Entry] = {
    CancelEffectTimer(id)
    entryIdToEntry.get(id) match {
      case out @ Some(oldEntry) =>
        entryIdToTimer += id -> context.system.scheduler.scheduleOnce(
          time.getOrElse(oldEntry.retime) milliseconds,
          self,
          AuraEffectBehavior.Aggravate(id, iteration, leftoverTime)
        )
        out
      case _ =>
        PerformCleanupEffect(id)
        None
    }
  }

  private def RetimeEventAndPerformAggravation(id: Long, iteration: Int, time: Option[Long], leftoverTime: Long) : Unit = {
    RetimeEvent(id, iteration - 1, time, leftoverTime) match {
      case Some(entry) =>
        PerformAggravation(entry, iteration)
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
          damage.effect_type != Aura.None && //TODO aphelion starfire
          damage.targets.exists(validation => validation.test(AuraTargetObject)) =>
        TryAggravationEffect(damage, data)
      case _ => ;
    }
  }

  private def TryAggravationEffect(aggravation: AggravatedDamage, data: ResolvedProjectile) : Unit = {
    val effect = aggravation.effect_type
    val obj = AuraTargetObject
    if(CheckForUniqueUnqueuedProjectile(data.projectile)) {
      val auraEffects = obj.Aura
      if(auraEffects.contains(effect) && aggravation.cumulative_damage_degrade) {
        SetupAggravationEntry(aggravation, data)
      }
      else if(obj.AddEffectToAura(effect).diff(auraEffects).contains(effect)) {
        SetupAggravationEntry(aggravation, data)
        UpdateAggravatedEffect(obj)
      }
    }
  }

  private def CheckForUniqueUnqueuedProjectile(projectile : Projectile) : Boolean = {
    !entryIdToEntry.values.exists { entry => entry.data.projectile.id == projectile.id }
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
        //setup timer data
        val tick = 1000 //each second
        val duration = aggravation.duration
        val iterations = (duration / tick).toInt
        val leftoverTime = duration - (iterations * tick)
        //quality per tick
        val totalPower = (duration.toFloat / info.infliction_rate).toInt - 1
        val averagePowerPerTick = math.max(1, totalPower.toFloat / iterations).toInt
        val lastTickRemainder = totalPower - averagePowerPerTick * iterations
        val qualityPerTick: List[Int] = if (lastTickRemainder > 0) {
          0 +: List.fill[Int](iterations - 1)(averagePowerPerTick) :+ (lastTickRemainder + averagePowerPerTick)
        }
        else {
          0 +: List.fill[Int](iterations)(averagePowerPerTick)
        }
        //pair id with entry
        PairIdWithAggravationEntry(id, effect, tick, data, data.target, qualityPerTick)
        //pair id with timer
        entryIdToTimer += id -> context.system.scheduler.scheduleOnce(tick milliseconds, self, AuraEffectBehavior.Aggravate(id, iterations, leftoverTime))
      case _ => ;
    }
  }

  private def PairIdWithAggravationEntry(
                                  id: Long,
                                  effect: Aura,
                                  retime: Long,
                                  data: ResolvedProjectile,
                                  target: SourceEntry,
                                  powerOffset: List[Int]
                                ): AuraEffectBehavior.Entry = {
    val aggravatedDamageInfo = ResolvedProjectile(
      AuraEffectBehavior.burning(data.resolution),
      data.projectile,
      target,
      data.damage_model,
      data.hit_pos
    )
    val entry = AuraEffectBehavior.Entry(id, effect, retime, aggravatedDamageInfo, powerOffset)
    entryIdToEntry += id -> entry
    entry
  }

  def RemoveEffectEntry(id: Long) : Aura = {
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

  def CleanupEffect(id: Long) : Aura = {
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

  def UpdateAggravatedEffect(target: AuraEffectBehavior.Target) : Unit = {
    import services.avatar.{AvatarAction, AvatarServiceMessage}
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + _.id)
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(target.GUID, 54, value))
  }

  private def PerformAggravation(entry: AuraEffectBehavior.Entry, tick: Int = 0) : Unit = {
    val data = entry.data
    val info = ResolvedProjectile(
      data.resolution,
      data.projectile.quality(entry.qualityPerTick(tick).toFloat),
      data.target,
      data.damage_model,
      data.hit_pos
    )
    TakesDamage.apply(Vitality.Damage(info.damage_model.Calculate(info)))
  }
}

object AuraEffectBehavior {
  type Target = PlanetSideServerObject with Vitality with AuraContainer

  private case class Entry(id: Long, effect: Aura, retime: Long, data: ResolvedProjectile, qualityPerTick: List[Int])

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
