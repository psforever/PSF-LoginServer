//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.{DriveState, PlanetSideGUID}
import services.{RemoverActor, Service}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleService, VehicleServiceMessage}

import scala.concurrent.duration._

trait DamageableVehicle extends DamageableEntity {
  def DamageableObject : Vehicle

  override protected def TakesDamage : Receive = super.TakesDamage.orElse {
    //handle cargo vehicles inheriting feedback from carrier
    case DamageableVehicle.Damage(cause) =>
      DamageAwareness(DamageableObject, cause, amount = 1) //non-zero

    case DamageableVehicle.Destruction(cause) =>
      val obj = DamageableObject
      obj.Health = 0
      obj.Shields = 0
      obj.History(cause)
      DestructionAwareness(obj, cause)
  }

  override protected def PerformDamage(target : Damageable.Target, applyDamageTo : ResolutionCalculations.Output) : Unit = {
    val obj = DamageableObject
    val originalHealth = obj.Health
    val originalShields = obj.Shields
    val cause = applyDamageTo(obj)
    val health = obj.Health
    val shields = obj.Shields
    val damageToHealth = originalHealth - health
    val damageToShields = originalShields - shields
    if(damageToHealth > 0 || damageToShields > 0) {
      val name = target.Actor.toString
      val slashPoint = name.lastIndexOf("/")
      DamageLog(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
      val damage = damageToHealth + damageToShields
      HandleDamage(target, cause, damage)
    }
  }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    val obj = DamageableObject
    DamageableMountable.DamageAwareness(obj, cause, amount)
    DamageableVehicle.DamageAwareness(obj, cause, amount)
    DamageableWeaponTurret.DamageAwareness(obj, cause, amount)
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
  private case class Damage(cause : ResolvedProjectile)
  private case class Destruction(cause : ResolvedProjectile)

  /**
    * na
    * @param target na
    * @param cause na
    * @param amount na
    */
  def DamageAwareness(target : Vehicle, cause : ResolvedProjectile, amount : Int) : Unit = {
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Actor ! DamageableVehicle.Damage(cause)
        case None => ;
      }
    })
    //shields
    val zone = target.Zone
    zone.VehicleEvents ! VehicleServiceMessage(s"${target.Actor}", VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 68, target.Shields))
  }

  /**
    * na
    * @param target na
    * @param cause na
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
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), zone))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, zone, Some(1 minute)))
  }
}
