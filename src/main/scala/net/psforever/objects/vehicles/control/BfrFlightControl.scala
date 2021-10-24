// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.types.Vector3

/**
  * ...
  */
class BfrFlightControl(vehicle: Vehicle)
  extends BfrControl(vehicle)
  with VehicleCapacitance {
  def CapacitanceObject: Vehicle = vehicle

  var flying: Option[Boolean] = None

  override def postStop() : Unit = {
    super.postStop()
    capacitancePostStop()
  }

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior
    .orElse(capacitorBehavior)
    .orElse {
      case BfrFlight.Soaring(flightValue) =>
        val localFlyingValue = flying
        vehicle.Flying = Some(flightValue)
        //capacitor drain
        if (vehicle.Capacitor > 0) {
          val definition = vehicle.Definition
          val (_, cdrain) = if (flightValue == 0 || flightValue == -0) {
            (0, vehicle.Capacitor)
          } else {
            val vdrain = if (flightValue > 0) definition.CapacitorDrain else 0
            val hdrain = if ({
              val vec = vehicle.Velocity.getOrElse(Vector3.Zero).xy
              vec.x > 0.5f || vec.y > 0.5f
            }) definition.CapacitorDrainSpecial else 0
            (vdrain, vdrain + hdrain)
          }
          flying = Some(if (cdrain > 0) {
            if (super.capacitorOnlyCharge(-cdrain) || vehicle.Capacitor < vehicle.Definition.MaxCapacitor) {
              startCapacitorTimer()
            }
            true
          } else {
            false
          })
        }
        //shield drain
        if (vehicle.Shields > 0) {
          vehicle.Definition.ShieldDrain match {
            case Some(drain) if localFlyingValue.isEmpty =>
              disableShield()
              vehicle.Shields -= drain
              showShieldCharge()
            case Some(drain) =>
              vehicle.Shields -= drain
              showShieldCharge()
            case _ => ;
          }
        }

      case BfrFlight.Landed =>
        if (flying.nonEmpty) {
          flying = None
          vehicle.Flying = None
          if (vehicle.Shields > 0) {
            enableShield()
          }
          shieldCharge(delay = 2000)
        }

      case _ => ;
    }

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    capacitanceStop()
  }

  override def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    capacitancePostStop()
  }

  override def chargeShieldsOnly(amount: Int): Unit = {
    if (flying != null && (flying.isEmpty || flying.contains(false))) {
      super.chargeShieldsOnly(amount)
    }
  }

  override protected def capacitorOnlyCharge(amount: Int): Boolean = {
    if (flying.isEmpty || flying.contains(false)) {
      super.capacitorOnlyCharge(amount)
    } else {
      false
    }
  }
}

object BfrFlight {
  final case class Soaring(flyingValue: Int)
  case object Landed
}
