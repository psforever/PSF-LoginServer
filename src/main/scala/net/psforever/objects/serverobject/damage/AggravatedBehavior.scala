// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.ballistics._
import net.psforever.objects.serverobject.aura.Aura
import net.psforever.objects.vital.{DamageType, Vitality}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait AggravatedBehavior {
  _ : Actor with Damageable =>
  private val entryIdToEntry: mutable.LongMap[AggravatedBehavior.Entry] =
    mutable.LongMap.empty[AggravatedBehavior.Entry]
  private val aggravationToTimer: mutable.LongMap[Cancellable] =
    mutable.LongMap.empty[Cancellable]
  /** ongoing flag to indicate whether the target is being afflicted by any form of aggravated damage */
  private var ongoingAggravated: Boolean = false

  def AggravatedObject: AggravatedBehavior.Target

  def TryAggravationEffectActivate(data: ResolvedProjectile): Option[AggravatedDamage] = {
    val projectile = data.projectile
    projectile.profile.Aggravated match {
      case Some(damage)
        if projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated) &&
           damage.info.exists(_.damage_type == AggravatedDamage.basicDamageType(data.resolution)) &&
           damage.effect_type != Aura.Nothing &&
          (projectile.quality == ProjectileQuality.AggravatesTarget ||
           damage.targets.exists(validation => validation.test(AggravatedObject))) =>
        TryAggravationEffectActivate(damage, data)
      case _ =>
        None
    }
  }

  private def TryAggravationEffectActivate(
                                            aggravation: AggravatedDamage,
                                            data: ResolvedProjectile
                                          ): Option[AggravatedDamage] = {
    val effect = aggravation.effect_type
    if(CheckForUniqueUnqueuedProjectile(data.projectile)) {
      val sameEffect = entryIdToEntry.values.filter(entry => entry.effect == effect)
      if(sameEffect.isEmpty || sameEffect.nonEmpty && aggravation.cumulative_damage_degrade) {
        SetupAggravationEntry(aggravation, data)
        Some(aggravation)
      }
      else {
        None
      }
    }
    else {
      None
    }
  }

  private def CheckForUniqueUnqueuedProjectile(projectile : Projectile): Boolean = {
    !entryIdToEntry.values.exists { entry => entry.data.projectile.id == projectile.id }
  }

  private def SetupAggravationEntry(aggravation: AggravatedDamage, data: ResolvedProjectile): Boolean = {
    val effect = aggravation.effect_type
    aggravation.info.find(_.damage_type == AggravatedDamage.basicDamageType(data.resolution)) match {
      case Some(info) =>
        val timing = aggravation.timing
        val duration = timing.duration
        //setup effect
        val id = data.projectile.id
        //setup timer data
        val (tick: Long, iterations: Int) = timing.ticks match {
          case Some(n) if n < 1 =>
            val rate = info.infliction_rate
            (rate, (duration / rate).toInt)
          case Some(ticks) =>
            (duration / ticks, ticks)
          case None =>
            (1000L, (duration / 1000).toInt)
        }
        //quality per tick
        val totalPower = (duration.toFloat / info.infliction_rate).toInt - 1
        val averagePowerPerTick = totalPower.toFloat / iterations
        val lastTickRemainder = totalPower - averagePowerPerTick * iterations
        val qualityPerTick: List[Float] = if (lastTickRemainder > 0) {
          0f +: List.fill[Float](iterations - 1)(averagePowerPerTick) :+ (lastTickRemainder + averagePowerPerTick)
        }
        else {
          0f +: List.fill[Float](iterations)(averagePowerPerTick)
        }
        //pair id with entry
        PairIdWithAggravationEntry(id, effect, tick, data, data.target, qualityPerTick)
        //pair id with timer
        aggravationToTimer += id -> context.system.scheduler.scheduleOnce(tick milliseconds, self, AggravatedBehavior.Aggravate(id, iterations))
        ongoingAggravated = true
        true
      case _ =>
        false
    }
  }

  private def PairIdWithAggravationEntry(
                                          id: Long,
                                          effect: Aura,
                                          retime: Long,
                                          data: ResolvedProjectile,
                                          target: SourceEntry,
                                          powerOffset: List[Float]
                                        ): AggravatedBehavior.Entry = {
    val aggravatedDamageInfo = ResolvedProjectile(
      AggravatedDamage.burning(data.resolution),
      data.projectile,
      target,
      data.damage_model,
      data.hit_pos
    )
    val entry = AggravatedBehavior.Entry(id, effect, retime, aggravatedDamageInfo, powerOffset)
    entryIdToEntry += id -> entry
    entry
  }

  val aggravatedBehavior: Receive = {
    case AggravatedBehavior.Aggravate(id, 0) =>
      AggravationCleanup(id)

    case AggravatedBehavior.Aggravate(id, iteration) =>
      RetimeEventAndPerformAggravation(id, iteration, None)
  }

  private def RetimeEventAndPerformAggravation(id: Long, iteration: Int, time: Option[Long]) : Unit = {
    RetimeAggravation(id, iteration - 1, time) match {
      case Some(entry) =>
        PerformAggravation(entry, iteration)
      case _  => ;
    }
  }

  private def RetimeAggravation(
                                 id: Long,
                                 iteration: Int,
                                 time: Option[Long]
                               ): Option[AggravatedBehavior.Entry] = {
    CleanupAggravationTimer(id)
    entryIdToEntry.get(id) match {
      case out @ Some(oldEntry) =>
        aggravationToTimer += id -> context.system.scheduler.scheduleOnce(
          time.getOrElse(oldEntry.retime) milliseconds,
          self,
          AggravatedBehavior.Aggravate(id, iteration)
        )
        out
      case _ =>
        AggravationCleanup(id)
        None
    }
  }

  def RemoveAggravatedEntry(id: Long): Aura = {
    entryIdToEntry.remove(id) match {
      case Some(entry) =>
        ongoingAggravated = entryIdToEntry.nonEmpty
        entry.data.projectile.profile.Aggravated.get.effect_type
      case _ =>
        Aura.Nothing
    }
  }

  def CleanupAggravationTimer(id: Long): Unit = {
    //remove and cancel timer
    aggravationToTimer.remove(id) match {
      case Some(timer) => timer.cancel()
      case _ => ;
    }
  }

  def AggravationCleanup(id: Long): Unit = {
    RemoveAggravatedEntry(id)
    CleanupAggravationTimer(id)
  }

  def EndAllAggravation(): Unit = {
    entryIdToEntry.clear()
    aggravationToTimer.values.foreach { _.cancel() }
    aggravationToTimer.clear()
  }

  def AggravatedReaction: Boolean = ongoingAggravated

  private def PerformAggravation(entry: AggravatedBehavior.Entry, tick: Int = 0): Unit = {
    val data = entry.data
    val model = data.damage_model
    val aggravatedProjectileData = ResolvedProjectile(
      data.resolution,
      data.projectile.quality(ProjectileQuality.Modified(entry.qualityPerTick(tick))),
      data.target,
      model,
      data.hit_pos
    )
    takesDamage.apply(Vitality.Damage(model.Calculate(aggravatedProjectileData)))
  }
}

object AggravatedBehavior {
  type Target = Damageable.Target

  private case class Entry(id: Long, effect: Aura, retime: Long, data: ResolvedProjectile, qualityPerTick: List[Float])

  private case class Aggravate(id: Long, iterations: Int)
}
