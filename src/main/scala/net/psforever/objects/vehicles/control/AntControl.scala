// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.vehicles._
import net.psforever.types.DriveState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//ant
class AntControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle)
    with AntTransferBehavior {
  def ChargeTransferObject = vehicle

  findChargeTargetFunc = Vehicles.FindANTChargingSource
  findDischargeTargetFunc = Vehicles.FindANTDischargingTarget

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior.orElse(antBehavior)

  override def specificResponseToDeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Deployed =>
        // Start ntu regeneration
        // If vehicle sends UseItemMessage with silo as target NTU regeneration will be disabled and orb particles will be disabled
        context.system.scheduler.scheduleOnce(
          delay = 1000 milliseconds,
          vehicle.Actor,
          TransferBehavior.Charging(Ntu.Nanites)
        )
      case _ => ;
    }
  }

  override def specificResponseToUndeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Undeploying =>
        TryStopChargingEvent(vehicle)
      case _ => ;
    }
  }
}
