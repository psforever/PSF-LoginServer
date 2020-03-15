//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.{DriveState, PlanetSideGUID}
import services.{RemoverActor, Service}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleService, VehicleServiceMessage}

import scala.concurrent.duration._

/**
  * The "control" `Actor` mixin for damage-handling code for `Vehicle` objects.
  */
trait DamageableVehicle extends DamageableEntity {
  /** vehicles (may) have shields; they need to be handled */
  private var handleDamageToShields : Boolean = false

  def DamageableObject : Vehicle

  override protected def TakesDamage : Receive = super.TakesDamage.orElse {
    case DamageableVehicle.Damage(cause) =>
      //cargo vehicles inherit feedback from carrier
      DamageAwareness(DamageableObject, cause, amount = 1) //non-zero

    case DamageableVehicle.Destruction(cause) =>
      //cargo vehicles are destroyed when carrier is destroyed
      val obj = DamageableObject
      obj.Health = 0
      obj.Shields = 0
      obj.History(cause)
      DestructionAwareness(obj, cause)
  }

  override def WillAffectTarget(damage : Int, cause : ResolvedProjectile) : Boolean = {
    //jammable
    super.WillAffectTarget(damage, cause) || cause.projectile.profile.JammerProjectile
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
    if(WillAffectTarget(damageToHealth + damageToShields, cause)) {
      DamageLog(target, s"BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
      handleDamageToShields = damageToShields > 0
      HandleDamage(target, cause, damageToHealth)
    }
  }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    val obj = DamageableObject
    val handleShields = handleDamageToShields
    handleDamageToShields = false
    DamageableMountable.DamageAwareness(obj, cause)
    DamageableVehicle.DamageAwareness(obj, cause, handleShields)
    DamageableWeaponTurret.DamageAwareness(obj, cause)
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
  private case class Damage(cause : ResolvedProjectile)
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
    * @see `JammableUnit.Jammered`
    * @see `Service.defaultPlayerGUID`
    * @see `Vehicle.CargoHolds`
    * @see `VehicleAction.PlanetsideAttribute`
    * @see `VehicleServiceMessage`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    * @param damageToShields dispatch a shield strength update
    */
  def DamageAwareness(target : Vehicle, cause : ResolvedProjectile, damageToShields : Boolean) : Unit = {
    if(target.MountedIn.isEmpty && cause.projectile.profile.JammerProjectile) {
      target.Actor ! JammableUnit.Jammered(cause)
    }
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Actor ! DamageableVehicle.Damage(cause)
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
    * @see `JammableUnit.ClearJammeredSound`
    * @see `JammableUnit.ClearJammeredStatus`
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
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
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
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), zone))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, zone, Some(1 minute)))
  }
}
