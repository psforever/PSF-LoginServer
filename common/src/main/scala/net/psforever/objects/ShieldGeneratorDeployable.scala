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
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.PlanetSideGUID
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

class ShieldGeneratorDeployable(cdef : ShieldGeneratorDefinition) extends ComplexDeployable(cdef)
  with Hackable
  with JammableUnit

class ShieldGeneratorDefinition extends ComplexDeployableDefinition(240) {
  Packet = new ShieldGeneratorConverter
  DeployCategory = DeployableCategory.ShieldGenerators

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[ShieldGeneratorControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

class ShieldGeneratorControl(gen : ShieldGeneratorDeployable) extends Actor
  with JammableBehavior
  with DamageableEntity
  with RepairableEntity {
  def JammableObject = gen
  def DamageableObject = gen
  def RepairableObject = gen

  def receive : Receive = jammableBehavior
    .orElse(takesDamage)
    .orElse {
      case msg @ CommonMessages.Use(_, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
        if(gen.CanRepair) {
          canBeRepairedByNanoDispenser.apply(msg)
        }
        else if(!gen.Destroyed) {
          if(gen.Shields < gen.MaxShields) {
            //TODO reinforced shield upgrade not implemented yet
          }
          else {
            //TODO ammunition supply upgrade not implemented yet
          }
        }

      case _ => ;
    }

//  override def WillAffectTarget(damage : Int, cause : ResolvedProjectile) : Boolean = {
//    super.WillAffectTarget(damage, cause) || cause.projectile.profile.JammerProjectile
//  }

  override protected def PerformDamage(target : Damageable.Target, applyDamageTo : ResolutionCalculations.Output) : Unit = {
    val originalHealth = gen.Health
    val originalShields = gen.Shields
    val cause = applyDamageTo(target)
    val health = gen.Health
    val shields = gen.Shields
    val damageToHealth = originalHealth - health
    val damageToShields = originalShields - shields
    val damage = damageToHealth + damageToShields
    if(WillAffectTarget(damage, cause)) {
      val name = target.Actor.toString
      val slashPoint = name.lastIndexOf("/")
      DamageLog(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
      HandleDamage(target, cause, damage)
    }
  }

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    //TODO shield damage; need to implement shield upgrades
    ShieldGeneratorControl.DamageAwareness(target, PlanetSideGUID(0), cause)
  }

  override protected def DestructionAwareness(target : Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    ShieldGeneratorControl.DestructionAwareness(gen, PlanetSideGUID(0))
  }

  /*
  while the shield generator is technically a supported jammable target, how that works is currently unknown
  check the object definition for proper feature activation
   */
  override def StartJammeredSound(target : Any, dur : Int) : Unit =  { }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with JammableUnit if !obj.Jammed =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 1))
      super.StartJammeredStatus(obj, dur)
    case _ => ;
  }

  override def CancelJammeredSound(target : Any) : Unit =  { }

  override def CancelJammeredStatus(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject with JammableUnit  if obj.Jammed =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 0))
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }
}

object ShieldGeneratorControl {
  /**
    * na
    * @param target na
    * @param attribution na
    * @param cause na
    */
  def DamageAwareness(target : Damageable.Target, attribution : PlanetSideGUID, cause : ResolvedProjectile) : Unit = {
    if(cause.projectile.profile.JammerProjectile) {
      target.Actor ! JammableUnit.Jammered(cause)
    }
  }

  /**
    * na
    * @param target na
    * @param attribution na
    */
  def DestructionAwareness(target : Damageable.Target with Deployable, attribution : PlanetSideGUID) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    Deployables.AnnounceDestroyDeployable(target, None)
  }
}
