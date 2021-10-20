// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects.{Default, Vehicle}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.interaction.DamageResult

/**
  * ...
  */
class BfrFlightControl(vehicle: Vehicle)
  extends BfrControl(vehicle)
  with VehicleCapacitance {
  def CapacitanceObject: Vehicle = vehicle
  var flying: Boolean = false

  override def postStop() : Unit = {
    super.postStop()
    capacitancePostStop()
  }

  override def commonEnabledBehavior : Receive = super.commonEnabledBehavior
    .orElse(capacitorBehavior)
    .orElse {
      case BfrFlight.Soaring =>
        capacitanceStop()
        shieldCharge.cancel()
        shieldCharge = Default.Cancellable
        if (vehicle.Capacitor > 0) {
          vehicle.Capacitor -= 10
          showCapacitorCharge()
        }
        if (vehicle.Shields > 0) {
          vehicle.Definition.ShieldDrain match {
            case Some(drain) if !flying =>
              disableShield()
              vehicle.Shields -= drain
              showShieldCharge()
            case Some(drain) =>
              vehicle.Shields -= drain
              showShieldCharge()
            case _ => ;
          }
        }
        flying = true

      case BfrFlight.Landed =>
        flying = false
        if (vehicle.Shields > 0) {
          enableShield()
        }
        shieldCharge(delay = 2000)
        startCapacitorTimer()

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
}

object BfrFlight {
  case object Soaring
  case object Landed
}
