// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.vehicles._
import net.psforever.types.DriveState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * A vehicle control agency exclusive to the advanced nanite transport (ANT).
  * When deployed, nanites in the package of nanite transfer units (NTU) are moved around
  * and may be may be acquired from a Warp Gate structure
  * or supplied to a nanite resource silo belonging to a mjaor facility.
  * @param vehicle the ANT
  */
class AntControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle)
    with AntTransferBehavior {
  def ChargeTransferObject = vehicle

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior.orElse(antBehavior)

  /**
    * React to a deployment state change.
    * Make ourselves available to nanite charging or discharging.
    * @param state the deployment state
    */
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

  /**
    * React to an undeployment state change.
    * Stop charging or discharging and tell the partner entity that it is no longer interacting with this vehicle.
    * @param state the undeployment state
    */
  override def specificResponseToUndeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Undeploying =>
        TryStopChargingEvent(vehicle)
      case _ => ;
    }
  }
}
