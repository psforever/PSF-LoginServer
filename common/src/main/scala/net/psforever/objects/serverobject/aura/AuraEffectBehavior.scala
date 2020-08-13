// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aura

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.PlanetSideServerObject

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait AuraEffectBehavior {
  _ : Actor =>
  private var activeEffectIndex: Long = 0
  private val effectToEntryId: mutable.HashMap[Aura, List[Long]] =
    mutable.HashMap.empty[Aura, List[Long]]
  private val effectIdToTimer: mutable.LongMap[Cancellable] =
    mutable.LongMap.empty[Cancellable]

  def AuraTargetObject: AuraEffectBehavior.Target

  val auraBehavior: Receive = {
    case AuraEffectBehavior.StartEffect(effect, duration) =>
      StartAuraEffect(effect, duration)

    case AuraEffectBehavior.EndEffect(Some(id), None) =>
      EndAuraEffect(id)

    case AuraEffectBehavior.EndEffect(None, Some(effect)) =>
      EndAuraEffect(effect)

    case AuraEffectBehavior.EndAllEffects() =>
      EndAllEffectsAndUpdate()
  }

  final def GetUnusedEffectId: Long = {
    val id = activeEffectIndex
    activeEffectIndex += 1
    id
  }

  def StartAuraEffect(effect: Aura, duration: Long): Long = {
    StartAuraEffect(GetUnusedEffectId, effect, duration)
  }

  def StartAuraEffect(id: Long, effect: Aura, duration: Long): Long = {
    //pair aura effect with id
    effectToEntryId.get(effect) match {
      case None | Some(Nil) => effectToEntryId += effect -> List(id)
      case Some(list) => effectToEntryId -> (list :+ id)
    }
    //pair id with timer
    effectIdToTimer += id -> context.system.scheduler.scheduleOnce(duration milliseconds, self, AuraEffectBehavior.EndEffect(id))
    //update visuals
    UpdateAuraEffect(AuraTargetObject)
    id
  }

  def EndAuraEffect(id: Long): Unit = {
    EndActiveEffect(id) match {
      case Aura.Nothing => ;
      case effect =>
        CancelEffectTimer(id)
        val obj = AuraTargetObject
        obj.RemoveEffectFromAura(effect)
        UpdateAuraEffect(obj)
    }
  }

  def EndActiveEffect(id: Long): Aura = {
    effectToEntryId.find { case (_, ids) => ids.contains(id) } match {
      case Some((effect, ids)) if ids.size == 1 =>
        effectToEntryId.remove(effect)
        effect
      case Some((effect, ids)) =>
        effectToEntryId += effect -> ids.filterNot(_ == id)
        Aura.Nothing
      case None =>
        Aura.Nothing
    }
  }

  def CancelEffectTimer(id: Long) : Unit = {
    effectIdToTimer.remove(id) match {
      case Some(timer) => timer.cancel
      case _ => ;
    }
  }

  def EndAuraEffect(effect: Aura): Unit = {
    effectToEntryId.remove(effect) match {
      case Some(idList) =>
        idList.foreach { id =>
          val obj = AuraTargetObject
          CancelEffectTimer(id)
          obj.RemoveEffectFromAura(effect)
          UpdateAuraEffect(obj)
        }
      case _ => ;
    }
  }

  def EndAllEffects() : Unit = {
    effectIdToTimer.values.foreach { _.cancel }
    effectIdToTimer.clear
    effectToEntryId.clear
    val obj = AuraTargetObject
    obj.Aura.foreach { obj.RemoveEffectFromAura }
  }

  def EndAllEffectsAndUpdate() : Unit = {
    EndAllEffects()
    UpdateAuraEffect(AuraTargetObject)
  }

  def UpdateAuraEffect(target: AuraEffectBehavior.Target) : Unit = {
    import services.avatar.{AvatarAction, AvatarServiceMessage}
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + AuraEffectBehavior.effectToAttributeValue(_))
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(target.GUID, 54, value))
  }

  def TestForEffect(id: Long): Aura = {
    effectToEntryId.find { case (_, ids) => ids.contains(id) } match {
      case Some((effect, _)) => effect
      case _ => Aura.Nothing
    }
  }
}

object AuraEffectBehavior {
  type Target = PlanetSideServerObject with AuraContainer

  final case class StartEffect(effect: Aura, duration: Long)

  final case class EndEffect(id: Option[Long], aura: Option[Aura])

  object EndEffect {
    def apply(id: Long): EndEffect = EndEffect(Some(id), None)

    def apply(aura: Aura): EndEffect = EndEffect(None, Some(aura))
  }

  final case class EndAllEffects()

  private def effectToAttributeValue(effect: Aura): Int = effect match {
    case Aura.None => 0
    case Aura.Plasma => 1
    case Aura.Comet => 2
    case Aura.Napalm => 4
    case Aura.Fire => 8
    case _ => Int.MinValue
  }
}
