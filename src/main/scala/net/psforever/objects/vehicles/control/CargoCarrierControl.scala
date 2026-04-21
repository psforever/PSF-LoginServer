// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.damage.{Damageable, DamageableVehicle}
import net.psforever.objects.vehicles.{Cargo, CarrierBehavior}
import net.psforever.objects.vital.interaction.DamageResult

/**
  * A vehicle control agency exclusive to vehicles that can physically transport other vehicles.
  * This includes the Galaxy (`dropship`) and the Lodestar.
  * @param vehicle the vehicle
  */
class CargoCarrierControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
    with CarrierBehavior {
  def CarrierObject: Vehicle = vehicle

  override def TestToStartSelfReporting(): Boolean = {
    super.TestToStartSelfReporting() &&
      !CarrierObject
        .CargoHolds
        .values
        .flatMap(_.occupants)
        .exists(_.Seats.values.exists(_.isOccupied))
  }

  override def postStop() : Unit = {
    super.postStop()
    endAllCarrierOperations()
  }

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior.orElse(carrierBehavior)

  /**
    * If the vehicle becomes disabled, the safety and autonomy of the cargo should be prioritized.
    * @param kickPassengers passengers need to be ejected "by force";
   *                       actually controls bail protection and flavors a log message (further down the line)
    */
  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    //abandon all cargo
    vehicle.CargoHolds.collect {
      case (index, hold : Cargo) if hold.isOccupied =>
        val cargo = hold.occupant.get
        checkCargoDismount(cargo.GUID, index, iteration = 0, bailed = kickPassengers)
    }
  }

  /**
    * A damaged carrier alerts its cargo vehicles of the source of the damage,
    * but that cargo will not be affected by either damage directly or by other effects applied to the carrier.
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    * @param amount how much damage was performed
    */
  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any): Unit = {
    val report = amount match {
      case (a: Int, b: Int) => a + b
      case a: Int => a
      case _ => 0
    }
    val announceConfrontation: Boolean = reportDamageToVehicle || report > 0
    super.DamageAwareness(target, cause, amount)
    if (announceConfrontation) {
      //alert cargo occupants to damage source
      vehicle.CargoHolds.values.foreach(hold => {
        hold.occupant match {
          case Some(cargo) => cargo.Actor ! DamageableVehicle.Damage(cause, report)
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
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    //cargo vehicles die with us
    vehicle.CargoHolds.values.foreach { hold =>
      hold.occupant match {
        case Some(cargo) => cargo.Actor ! DamageableVehicle.Destruction(cause)
        case None => ;
      }
    }
  }
}
