//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor
import net.psforever.objects.{Vehicle, Vehicles}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.DamageType
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.DamageWithPositionMessage
import net.psforever.types.Vector3

import scala.concurrent.duration._

/**
  * The "control" `Actor` mixin for damage-handling code for `Vehicle` objects.
  */
trait DamageableVehicle
  extends DamageableEntity
  with AggravatedBehavior {
  _ : Actor =>

  def damageableVehiclePostStop(): Unit = {
    EndAllAggravation()
  }

  /** whether or not the vehicle has been damaged directly, report that damage has occurred */
  private var reportDamageToVehicle: Boolean = false

  def DamageableObject: Vehicle
  def AggravatedObject : Vehicle = DamageableObject

  override val takesDamage: Receive =
    originalTakesDamage
      .orElse(aggravatedBehavior)
      .orElse {
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
  override protected def PerformDamage(
      target: Damageable.Target,
      applyDamageTo: ResolutionCalculations.Output
  ): Unit = {
    val obj             = DamageableObject
    val originalHealth  = obj.Health
    val originalShields = obj.Shields
    val cause           = applyDamageTo(obj)
    val health          = obj.Health
    val shields         = obj.Shields
    val damageToHealth  = originalHealth - health
    val damageToShields = originalShields - shields
    if (WillAffectTarget(target, damageToHealth + damageToShields, cause)) {
      target.History(cause)
      DamageLog(
        target,
        s"BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields"
      )
      HandleDamage(target, cause, (damageToHealth, damageToShields))
    } else {
      obj.Health = originalHealth
      obj.Shields = originalShields
    }
  }

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
    * @param amount how much damage was performed
    */
  override protected def DamageAwareness(target: Target, cause: ResolvedProjectile, amount: Any): Unit = {
    val obj            = DamageableObject
    val zone           = target.Zone
    val events         = zone.VehicleEvents
    val targetGUID     = target.GUID
    val zoneId         = zone.id
    val vehicleChannel = s"${obj.Actor}"
    val (damageToHealth, damageToShields, totalDamage) = amount match {
      case (a: Int, b: Int) => (a, b, a+b)
      case _ => (0, 0, 0)
    }
    var announceConfrontation: Boolean = reportDamageToVehicle || totalDamage > 0
    val aggravated = TryAggravationEffectActivate(cause) match {
      case Some(_) =>
        announceConfrontation = true
        false
      case _ =>
        cause.projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated)
    }
    reportDamageToVehicle = false

    //log historical event
    target.History(cause)
    //damage
    if (Damageable.CanDamageOrJammer(target, totalDamage, cause)) {
      //jammering
      if (Damageable.CanJammer(target, cause)) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
      //stat changes
      if (damageToShields > 0) {
        events ! VehicleServiceMessage(
          vehicleChannel,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 68, obj.Shields)
        )
        announceConfrontation = true
      }
      if (damageToHealth > 0) {
        events ! VehicleServiceMessage(
          zoneId,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, obj.Health)
        )
        announceConfrontation = true
      }
    }
    if (announceConfrontation) {
      if (aggravated) {
        val msg = VehicleAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(totalDamage, Vector3.Zero))
        obj.Seats.values
          .collect { case seat if seat.Occupant.nonEmpty => seat.Occupant.get.Name }
          .foreach { channel =>
            events ! VehicleServiceMessage(channel, msg)
          }
      }
      else {
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert to damage source
        DamageableMountable.DamageAwareness(obj, cause, totalDamage)
      }
      //alert cargo occupants to damage source
      obj.CargoHolds.values.foreach(hold => {
        hold.Occupant match {
          case Some(cargo) =>
            cargo.Actor ! DamageableVehicle.Damage(cause, totalDamage)
          case None => ;
        }
      })
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
  override protected def DestructionAwareness(target: Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    val obj = DamageableObject
    DamageableMountable.DestructionAwareness(obj, cause)
    val zone = target.Zone
    //aggravation cancel
    EndAllAggravation()
    //cargo vehicles die with us
    obj.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Actor ! DamageableVehicle.Destruction(cause)
        case None => ;
      }
    })
    //special considerations for certain vehicles
    Vehicles.BeforeUnloadVehicle(obj, zone)
    //shields
    if (obj.Shields > 0) {
      obj.Shields = 0
      zone.VehicleEvents ! VehicleServiceMessage(
        zone.id,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 68, 0)
      )
    }
    target.Actor ! Vehicle.Deconstruct(Some(1 minute))
    target.ClearHistory()
    DamageableWeaponTurret.DestructionAwareness(obj, cause)
  }
}

object DamageableVehicle {

  /**
    * Message for instructing the target's cargo vehicles about a damage source affecting their carrier.
    * @param cause historical information about damage
    */
  private case class Damage(cause: ResolvedProjectile, amount: Int)

  /**
    * Message for instructing the target's cargo vehicles that their carrier is destroyed,
    * and they should be destroyed too.
    * @param cause historical information about damage
    */
  private case class Destruction(cause: ResolvedProjectile)
}
