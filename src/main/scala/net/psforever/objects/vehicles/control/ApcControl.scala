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

//apc, apc_nc, apc_tr, apc_vs
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
            val emp = SpecialEmp.emp
            vehicle.Capacitor = 0
            events ! VehicleServiceMessage(
              self.toString(),
              VehicleAction.PlanetsideAttribute(GUID0, vehicle.GUID, 113, 0)
            )
            events ! VehicleServiceMessage(
              zone.id,
              VehicleAction.SendResponse(
                GUID0,
                TriggerEffectMessage(
                  GUID0,
                  s"apc_explosion_emp_${vehicle.Faction}",
                  None,
                  Some(TriggeredEffectLocation(pos, vehicle.Orientation))
                )
              )
            )
            Zone.serverSideDamage(
              zone,
              vehicle,
              emp,
              SpecialEmp.createEmpInteraction(emp, pos),
              Zone.distanceCheck,
              Zone.findAllTargets
            )
          }
          startCapacitorTimer()
      }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    capacitor.cancel()
    vehicle.Capacitor = 0
  }

  private def startCapacitorTimer(): Unit = {
    capacitor = context.system.scheduler.scheduleOnce(
      delay = 1000 millisecond,
      self,
      ApcControl.CapacitorCharge(10)
    )
  }
}

object ApcControl {
  private case class CapacitorCharge(amount: Int)
}
