//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.{DriveState, PlanetSideGUID}
import services.Service
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleService, VehicleServiceMessage}

import scala.concurrent.duration._

/**
  * The "control" `Actor` mixin for damage-handling code for `Vehicle` objects.
  */
trait DamageableVehicle extends DamageableEntity {
  /** vehicles (may) have shields; they need to be handled */
  private var handleDamageToShields : Boolean = false
  /** whether or not the vehicle has been damaged directly, report that damage has occurred */
  private var reportDamageToVehicle : Boolean = false

  def DamageableObject : Vehicle

  override protected def TakesDamage : Receive =
    super.TakesDamage.orElse {
      case DamageableVehicle.Damage(cause, damage) =>
      //cargo vehicles inherit feedback from carrier
      reportDamageToVehicle = damage > 0
      DamageAwareness(DamageableObject, cause, amount = 0)

    case DamageableVehicle.Destruction(cause) =>
      //cargo vehicles are destroyed when carrier is destroyed
      val obj = DamageableObject
      obj.Health = 0
      obj.History(cause)
      DestructionAwareness(obj, cause)
  }

  /**
    * Vehicles may have charged shields that absorb damage before the vehicle's own health is affected.
    * @param target the entity to be damaged
    * @param applyDamageTo the function that applies the damage to the target in a target-tailored fashion
    */
  override protected def PerformDamage(target : Damageable.Target, applyDamageTo : ResolutionCalculations.Output) : Unit = {
    val obj = DamageableObject
    val originalHealth = obj.Health
    val originalShields = obj.Shields
    val cause = applyDamageTo(obj)
    val health = obj.Health
    val shields = obj.Shields
    val damageToHealth = originalHealth - health
    val damageToShields = originalShields - shields
    if(WillAffectTarget(target, damageToHealth + damageToShields, cause)) {
      target.History(cause)
      DamageLog(target, s"BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
      handleDamageToShields = damageToShields > 0
      HandleDamage(target, cause, damageToHealth + damageToShields)
    }
    else {
      obj.Health = originalHealth
      obj.Shields = originalShields
    }
  }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    val obj = DamageableObject
    val handleShields = handleDamageToShields
    handleDamageToShields = false
    val handleReport = reportDamageToVehicle || amount > 0
    reportDamageToVehicle = false
    if(Damageable.CanDamageOrJammer(target, amount, cause)) {
      super.DamageAwareness(target, cause, amount)
    }
    if(handleReport) {
      DamageableMountable.DamageAwareness(obj, cause)
    }
    DamageableVehicle.DamageAwareness(obj, cause, amount, handleShields)
  }

  override protected def DestructionAwareness(target : Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    val obj = DamageableObject
    DamageableMountable.DestructionAwareness(obj, cause)
    DamageableVehicle.DestructionAwareness(obj, cause)
    DamageableWeaponTurret.DestructionAwareness(obj, cause)
  }
}

object DamageableVehicle {
  /**
    * Message for instructing the target's cargo vehicles about a damage source affecting their carrier.
    * @param cause historical information about damage
    */
  private case class Damage(cause : ResolvedProjectile, amount : Int)
  /**
    * Message for instructing the target's cargo vehicles that their carrier is destroyed,
    * and they should be destroyed too.
    * @param cause historical information about damage
    */
  private case class Destruction(cause : ResolvedProjectile)

  /**
    * Most all vehicles and the weapons mounted to them can jam
    * if the projectile that strikes (near) them has jammering properties.
    * A damaged carrier alerts its cargo vehicles of the source of the damage,
    * but it will not be affected by the same jammering effect.
    * If this vehicle has shields that were affected by previous damage, that is also reported to the clients.
    * @see `Service.defaultPlayerGUID`
    * @see `Vehicle.CargoHolds`
    * @see `VehicleAction.PlanetsideAttribute`
    * @see `VehicleServiceMessage`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    * @param damage how much damage was performed
    * @param damageToShields dispatch a shield strength update
    */
  def DamageAwareness(target : Vehicle, cause : ResolvedProjectile, damage : Int, damageToShields : Boolean) : Unit = {
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Actor ! DamageableVehicle.Damage(cause, damage + (if(damageToShields) 1 else 0))
        case None => ;
      }
    })
    //shields
    if(damageToShields) {
      val zone = target.Zone
      zone.VehicleEvents ! VehicleServiceMessage(s"${target.Actor}", VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 68, target.Shields))
    }
  }

  /**
    * A destroyed carrier informs its cargo vehicles that they should also be destroyed
    * for reasons of the same cause being inherited as the source of damage.
    * Regardless of the amount of damage they carrier takes or some other target would take,
    * its cargo vehicles die immediately.
    * The vehicle's shields are zero'd out if they were previously energized
    * so that the vehicle's corpse does not act like it is still protected by vehicle shields.
    * Finally, the vehicle is tasked for deconstruction.
    * @see `Deployment.TryDeploymentChange`
    * @see `DriveState.Undeploying`
    * @see `Service.defaultPlayerGUID`
    * @see `Vehicle.CargoHolds`
    * @see `VehicleAction.PlanetsideAttribute`
    * @see `RemoverActor.AddTask`
    * @see `RemoverActor.ClearSpecific`
    * @see `VehicleServiceMessage`
    * @see `VehicleServiceMessage.Decon`
    * @see `Zone.VehicleEvents`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target : Vehicle, cause : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    //cargo vehicles die with us
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Actor ! DamageableVehicle.Destruction(cause)
        case None => ;
      }
    })
    //special considerations for certain vehicles
    target.Definition match {
      case GlobalDefinitions.ams =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
        VehicleService.BeforeUnloadVehicle(target, zone)
        zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), target, None))
      case _ => ;
    }
    //shields
    if(target.Shields > 0) {
      target.Shields = 0
      zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 68, 0))
    }
    target.Actor ! Vehicle.Deconstruct(Some(1 minute))
    target.ClearHistory()
  }
}
