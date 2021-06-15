// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.vehicles.CargoBehavior
import net.psforever.objects.vital.interaction.DamageResult

//dropship (galaxy)
//lodestar
class CargoCarrierControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
    with CargoBehavior {
  def CargoObject = vehicle

  override def commonEnabledBehavior: Receive =
    super.commonEnabledBehavior
      .orElse(cargoBehavior)
      .orElse {
        case CargoCarrierControl.Damage(cause, damage) =>
          //cargo vehicles inherit feedback from carrier
          reportDamageToVehicle = damage > 0
          DamageAwareness(DamageableObject, cause, amount = 0)

        case CargoCarrierControl.Destruction(cause) =>
          //cargo vehicles are destroyed when carrier is destroyed
          val obj = DamageableObject
          obj.Health = 0
          obj.History(cause)
          DestructionAwareness(obj, cause)
      }

  override def PrepareForDisabled(kickPassengers : Boolean) : Unit = {
    //abandon all cargo
    vehicle.CargoHolds.values
      .collect {
        case hold if hold.isOccupied =>
          val cargo = hold.occupant.get
          CargoBehavior.HandleVehicleCargoDismount(
            cargo.GUID,
            cargo,
            vehicle.GUID,
            vehicle,
            bailed = false,
            requestedByPassenger = false,
            kicked = false
          )
      }
    super.PrepareForDisabled(kickPassengers)
  }

  /**
    * A damaged carrier alerts its cargo vehicles of the source of the damage,
    * but that cargo will not be affected by either damage directly or by other effects applied to the carrier.
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    * @param amount how much damage was performed
    */
  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any): Unit = {
    val totalDamage = amount match {
      case (a: Int, b: Int) => a + b
      case a: Int => a
      case _ => 0
    }
    val announceConfrontation: Boolean = reportDamageToVehicle || totalDamage > 0
    super.DamageAwareness(target, cause, amount)
    if (announceConfrontation) {
      //alert cargo occupants to damage source
      vehicle.CargoHolds.values.foreach(hold => {
        hold.occupant match {
          case Some(cargo) =>
            cargo.Actor ! CargoCarrierControl.Damage(cause, totalDamage)
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
        case Some(cargo) =>
          cargo.Actor ! CargoCarrierControl.Destruction(cause)
        case None => ;
      }
    }
    super.DestructionAwareness(target, cause)
  }
}

object CargoCarrierControl {
  /**
    * Message for instructing the target's cargo vehicles about a damage source affecting their carrier.
    * @param cause historical information about damage
    */
  private case class Damage(cause: DamageResult, amount: Int)

  /**
    * Message for instructing the target's cargo vehicles that their carrier is destroyed,
    * and they should be destroyed too.
    * @param cause historical information about damage
    */
  private case class Destruction(cause: DamageResult)
}
