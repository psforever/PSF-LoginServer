// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployableCategory}
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.ShieldGeneratorConverter
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.PlanetSideGUID
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

class ShieldGeneratorDeployable(cdef: ShieldGeneratorDefinition)
    extends ComplexDeployable(cdef)
    with Hackable
    with JammableUnit

class ShieldGeneratorDefinition extends ComplexDeployableDefinition(240) {
  Packet = new ShieldGeneratorConverter
  DeployCategory = DeployableCategory.ShieldGenerators

  override def Initialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    obj.Actor =
      context.actorOf(Props(classOf[ShieldGeneratorControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

class ShieldGeneratorControl(gen: ShieldGeneratorDeployable)
    extends Actor
    with JammableBehavior
    with DamageableEntity
    with RepairableEntity {
  def JammableObject                         = gen
  def DamageableObject                       = gen
  def RepairableObject                       = gen

  def receive: Receive =
    jammableBehavior
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse {
        case _ => ;
      }

  /**
    * The shield generator has two upgrade paths - blocking projectiles, and providing ammunition like a terminal.
    * Both upgrade paths are possible using the nano dispenser with an armor canister,
    * and can only be started when the generator is undamaged.
    * @see `PlanetSideGameObject.CanRepair`
    * @see `RepairableEntity.CanPerformRepairs`
    * @param player the user of the nano dispenser tool
    * @param item the nano dispenser tool
    */
  override def CanBeRepairedByNanoDispenser(player: Player, item: Tool): Unit = {
    if (gen.CanRepair) {
      super.CanBeRepairedByNanoDispenser(player, item)
    } else if (!gen.Destroyed) {
      //TODO reinforced shield upgrade not implemented yet
      //TODO ammunition supply upgrade not implemented yet
    }
  }

  override protected def PerformDamage(
      target: Damageable.Target,
      applyDamageTo: ResolutionCalculations.Output
  ): Unit = {
    val originalHealth  = gen.Health
    val originalShields = gen.Shields
    val cause           = applyDamageTo(target)
    val health          = gen.Health
    val shields         = gen.Shields
    val damageToHealth  = originalHealth - health
    val damageToShields = originalShields - shields
    val damage          = damageToHealth + damageToShields
    if (WillAffectTarget(target, damage, cause)) {
      target.History(cause)
      DamageLog(
        target,
        s"BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields"
      )
      HandleDamage(target, cause, (damageToHealth, damageToShields))
    } else {
      gen.Health = originalHealth
      gen.Shields = originalShields
    }
  }

  override protected def DamageAwareness(target: Damageable.Target, cause: ResolvedProjectile, amount: Any): Unit = {
    val (damageToHealth, damageToShields) = amount match {
      case (a: Int, b: Int) => (a, b)
      case _ => (0, 0)
    }
    super.DamageAwareness(target, cause, damageToHealth)
    ShieldGeneratorControl.DamageAwareness(gen, cause, damageToShields > 0)
  }

  override protected def DestructionAwareness(target: Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    ShieldGeneratorControl.DestructionAwareness(gen, PlanetSideGUID(0))
  }

  /*
  while the shield generator is technically a supported jammable target, how that works is currently unknown
  check the object definition for proper feature activation
   */
  override def StartJammeredSound(target: Any, dur: Int): Unit = {}

  override def StartJammeredStatus(target: Any, dur: Int): Unit =
    target match {
      case obj: PlanetSideServerObject with JammableUnit if !obj.Jammed =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(
          obj.Zone.id,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 1)
        )
        super.StartJammeredStatus(obj, dur)
      case _ => ;
    }

  override def CancelJammeredSound(target: Any): Unit = {}

  override def CancelJammeredStatus(target: Any): Unit = {
    target match {
      case obj: PlanetSideServerObject with JammableUnit if obj.Jammed =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(
          obj.Zone.id,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 0)
        )
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }
}

object ShieldGeneratorControl {

  /**
    * na
    * @param target na
    * @param cause na
    * @param damageToShields na
    */
  def DamageAwareness(target: ShieldGeneratorDeployable, cause: ResolvedProjectile, damageToShields: Boolean): Unit = {
    //shields
    if (damageToShields) {
      val zone = target.Zone
      zone.VehicleEvents ! VehicleServiceMessage(
        zone.id,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 68, target.Shields)
      )
    }
  }

  /**
    * na
    * @param target na
    * @param attribution na
    */
  def DestructionAwareness(target: Damageable.Target with Deployable, attribution: PlanetSideGUID): Unit = {
    Deployables.AnnounceDestroyDeployable(target, None)
  }
}
