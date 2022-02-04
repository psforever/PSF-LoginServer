//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{Vehicle, Vehicles}
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.DamageWithPositionMessage
import net.psforever.types.Vector3

import scala.concurrent.duration._

/**
  * The mixin for damage-handling code for `Vehicle` entities.
  */
trait DamageableVehicle
  extends DamageableEntity
  with AggravatedBehavior {
  _ : Actor =>

  def damageableVehiclePostStop(): Unit = {
    EndAllAggravation()
  }

  /** whether or not the vehicle has been damaged directly, report that damage has occurred */
  protected var reportDamageToVehicle: Boolean = false
  /** when the vehicle is destroyed, its major explosion is delayed */
  protected var queuedDestruction: Option[Cancellable] = None

  def DamageableObject: Vehicle
  def AggravatedObject : Vehicle = DamageableObject

  override val takesDamage: Receive = originalTakesDamage
    .orElse(aggravatedBehavior)
    .orElse {
      case DamageableVehicle.Damage(cause, damage) =>
        //cargo vehicles inherit feedback from carrier
        reportDamageToVehicle = damage > 0
        DamageAwareness(DamageableObject, cause, amount = 0)

      case DamageableVehicle.Destruction(cause) =>
        //cargo vehicles are destroyed when carrier is destroyed
        //bfrs undergo a shiver spell before exploding
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
    queuedDestruction match {
      case Some(_) => ;
      case None =>
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
  }

  /**
    * Produce the event system channel names required for updating helath and shield values.
    * @param obj the vehicle
    * @return the channel for updating health values, the channel for updating shield values
    */
  def damageChannels(obj: Vehicle): (String, String) = {
    (obj.Zone.id, obj.Actor.toString)
  }

  /**
    * Most all vehicles and the weapons mounted to them can jam
    * if the projectile that strikes (near) them has jammering properties.
    * If this vehicle has shields that were affected by previous damage, that is also reported to the clients.
    * @see `Service.defaultPlayerGUID`
    * @see `Vehicle.CargoHolds`
    * @see `VehicleAction.PlanetsideAttribute`
    * @see `VehicleServiceMessage`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    * @param amount how much damage was performed
    */
  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any): Unit = {
    val obj            = DamageableObject
    val zone           = target.Zone
    val events         = zone.VehicleEvents
    val targetGUID     = target.GUID
    val (healthChannel, shieldChannel) = damageChannels(obj)
    val (damageToHealth, damageToShields, totalDamage) = amount match {
      case (a: Int, b: Int) => (a, b, a+b)
      case _ => (0, 0, 0)
    }
    var announceConfrontation: Boolean = reportDamageToVehicle || totalDamage > 0
    val showAsAggravated = (TryAggravationEffectActivate(cause) match {
      case Some(_) =>
        announceConfrontation = true
        false
      case _ =>
        cause.interaction.cause.source.Aggravated.nonEmpty
    }) || cause.interaction.cause.resolution == DamageResolution.Collision
    reportDamageToVehicle = false

    if (obj.MountedIn.nonEmpty) {
      //log historical event
      target.History(cause)
    }
    //damage
    if (Damageable.CanDamageOrJammer(target, totalDamage, cause.interaction)) {
      //jammering
      if (Damageable.CanJammer(target, cause.interaction)) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
      //stat changes
      if (damageToShields > 0) {
        events ! VehicleServiceMessage(
          shieldChannel,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, obj.Definition.shieldUiAttribute, obj.Shields)
        )
        announceConfrontation = true
      }
      if (damageToHealth > 0) {
        events ! VehicleServiceMessage(
          healthChannel,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, obj.Health)
        )
        announceConfrontation = true
      }
    }
    if (announceConfrontation) {
      if (showAsAggravated) {
        val msg = VehicleAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(totalDamage, Vector3.Zero))
        obj.Seats.values
          .collect { case seat if seat.occupant.nonEmpty => seat.occupant.get.Name }
          .foreach { channel =>
            events ! VehicleServiceMessage(channel, msg)
          }
      }
      else {
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause)
        //alert to damage source
        DamageableMountable.DamageAwareness(obj, cause, totalDamage)
      }
    }
  }

  /**
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
  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    (queuedDestruction, DamageableObject.Definition.destructionDelay) match {
      case (None, Some(delay)) => //set a future explosion for later
        destructionDelayed(delay, cause)
      case (Some(_), _) | (None, None) => //explode now
        super.DestructionAwareness(target, cause)
        val obj = DamageableObject
        val zone = target.Zone
        //aggravation cancel
        EndAllAggravation()
        //passengers die with us
        DamageableMountable.DestructionAwareness(obj, cause)
        Zone.serverSideDamage(obj.Zone, target, Zone.explosionDamage(Some(cause)))
        //special considerations for certain vehicles
        Vehicles.BeforeUnloadVehicle(obj, zone)
        //shields
        if (obj.Shields > 0) {
          obj.Shields = 0
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, obj.Definition.shieldUiAttribute, 0)
          )
        }
        //clean up
        target.Actor ! Vehicle.Deconstruct(Some(1 minute))
        target.ClearHistory()
        DamageableWeaponTurret.DestructionAwareness(obj, cause)
      case _ => ;
    }
  }

  def destructionDelayed(delay: Long, cause: DamageResult): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val obj = DamageableObject
    //health to 1, shields to 0
    obj.Health = 1
    obj.Shields = 0
    val guid = obj.GUID
    val guid0 = Service.defaultPlayerGUID
    val zone = obj.Zone
    val zoneid = zone.id
    val events = zone.VehicleEvents
    events ! VehicleServiceMessage(
      zoneid,
      VehicleAction.PlanetsideAttribute(guid0, guid, 0, 1)
    )
    events ! VehicleServiceMessage(
      zoneid,
      VehicleAction.PlanetsideAttribute(guid0, guid, obj.Definition.shieldUiAttribute, 0)
    )
    //passengers die with us
    DamageableMountable.DestructionAwareness(DamageableObject, cause)
    //come back to this death later
    queuedDestruction = Some(context.system.scheduler.scheduleOnce(delay milliseconds, self, DamageableVehicle.Destruction(cause)))
  }
}

object DamageableVehicle {
  /**
    * Message for instructing the target's cargo vehicles about a damage source affecting their carrier.
    * @param cause historical information about damage
    */
  final case class Damage(cause: DamageResult, amount: Int)

  /**
    * Message for instructing the target's cargo vehicles that their carrier is destroyed,
    * and they should be destroyed too.
    * @param cause historical information about damage
    */
  final case class Destruction(cause: DamageResult)
}
