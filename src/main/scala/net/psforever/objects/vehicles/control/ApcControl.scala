// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{TriggerEffectMessage, TriggeredEffectLocation}
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * A vehicle control agency exclusive to the armored personnel carrier (APC) ground transport vehicles.
  * These vehicles include the Juggernaut (`apc_tr`), the Vindicator (`apc_nc`), the and Leviathan (`apc_vs`).
  * A completely faction-neutral transport in the same sytle (`apc`) does exist but is unused.
  * A common characteristic of this type of vehicle is the ability to discharge a defensive wide-area electromagnetic pulse.
  * @param vehicle the APC
  */
class ApcControl(vehicle: Vehicle)
  extends VehicleControl(vehicle) {
  protected var capacitor = Default.Cancellable

  startCapacitorTimer()

  override def postStop() : Unit = {
    super.postStop()
    capacitor.cancel()
  }

  override def commonEnabledBehavior : Receive =
    super.commonEnabledBehavior
      .orElse {
        case ApcControl.CapacitorCharge(amount) =>
          if (vehicle.Capacitor < vehicle.Definition.MaxCapacitor) {
            val capacitance = vehicle.Capacitor += amount
            vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
              self.toString(),
              VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 113, capacitance)
            )
            startCapacitorTimer()
          } else {
            capacitor = Default.Cancellable
          }

        case SpecialEmp.Burst() =>
          if (vehicle.Capacitor == vehicle.Definition.MaxCapacitor) { //only if the capacitor is full
            val zone = vehicle.Zone
            val events = zone.VehicleEvents
            val pos = vehicle.Position
            val GUID0 = Service.defaultPlayerGUID
            val emp = vehicle.Definition.innateDamage.getOrElse { SpecialEmp.emp }
            val faction = vehicle.Faction
            //drain the capacitor
            vehicle.Capacitor = 0
            events ! VehicleServiceMessage(
              self.toString(),
              VehicleAction.PlanetsideAttribute(GUID0, vehicle.GUID, 113, 0)
            )
            //cause the emp
            events ! VehicleServiceMessage(
              zone.id,
              VehicleAction.SendResponse(
                GUID0,
                TriggerEffectMessage(
                  GUID0,
                  s"apc_explosion_emp_${faction.toString.toLowerCase}",
                  None,
                  Some(TriggeredEffectLocation(pos, vehicle.Orientation))
                )
              )
            )
            //resolve what targets are affected by the emp
            Zone.serverSideDamage(
              zone,
              vehicle,
              emp,
              SpecialEmp.createEmpInteraction(emp, pos),
              ExplosiveDeployableControl.detectionForExplosiveSource(vehicle),
              Zone.findAllTargets
            )
            //start charging again
            startCapacitorTimer()
          }
      }

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    capacitor.cancel()
  }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    capacitor.cancel()
    vehicle.Capacitor = 0
  }

  //TODO switch from magic numbers to definition numbers?
  private def startCapacitorTimer(): Unit = {
    capacitor = context.system.scheduler.scheduleOnce(
      delay = 1000 millisecond,
      self,
      ApcControl.CapacitorCharge(10)
    )
  }
}

object ApcControl {
  /**
    * Charge the vehicle's internal capacitor by the given amount during the schedulefd charge event.
    * @param amount how much energy in the charge
    */
  private case class CapacitorCharge(amount: Int)
}
