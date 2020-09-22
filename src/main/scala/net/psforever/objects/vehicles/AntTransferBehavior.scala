// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{ActorRef, Cancellable}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.serverobject.transfer.{TransferBehavior, TransferContainer}
import net.psforever.objects.{NtuContainer, _}
import net.psforever.types.DriveState
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import akka.actor.typed.scaladsl.adapter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait AntTransferBehavior extends TransferBehavior with NtuStorageBehavior {
  var ntuChargingTick: Cancellable   = Default.Cancellable
  var panelAnimationFunc: () => Unit = NoCharge

  def TransferMaterial = Ntu.Nanites

  def ChargeTransferObject: Vehicle with NtuContainer

  def antBehavior: Receive = storageBehavior.orElse(transferBehavior)

  def ActivatePanelsForChargingEvent(vehicle: NtuContainer): Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 52, 1L)
    ) // panel glow on
  }

  /** Charging */
  def StartNtuChargingEvent(vehicle: NtuContainer): Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 49, 1L)
    ) // orb particle effect on
  }

  def UpdateNtuUI(vehicle: Vehicle with NtuContainer): Unit = {
    if (vehicle.Seats.values.exists(_.isOccupied)) {
      val display = scala.math.ceil(vehicle.NtuCapacitorScaled).toLong
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
        vehicle.Actor.toString,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 45, display)
      )
    }
  }

  def HandleChargingEvent(target: TransferContainer): Boolean = {
    ntuChargingTick.cancel()
    val obj = ChargeTransferObject
    //log.trace(s"NtuCharging: Vehicle $guid is charging NTU capacitor.")
    if (obj.NtuCapacitor < obj.Definition.MaxNtuCapacitor) {
      //charging
      panelAnimationFunc = InitialCharge
      transferTarget = Some(target)
      transferEvent = TransferBehavior.Event.Charging
      target match {
        case _: WarpGate =>
          //ANTs would charge from 0-100% in roughly 75s on live (https://www.youtube.com/watch?v=veOWToR2nSk&feature=youtu.be&t=1194)
          val max = obj.Definition.MaxNtuCapacitor - obj.NtuCapacitor
          target.Actor ! BuildingActor.Ntu(
            NtuCommand.Request(scala.math.min(obj.Definition.MaxNtuCapacitor / 75f, max), context.self)
          )
        case _ =>
      }

      ntuChargingTick = context.system.scheduler.scheduleOnce(
        delay = 1000 milliseconds,
        self,
        TransferBehavior.Charging(TransferMaterial)
      ) // Repeat until fully charged, or minor delay
      true
    } else {
      // Fully charged
      TryStopChargingEvent(obj)
      false
    }
  }

  def ReceiveAndDepositUntilFull(vehicle: Vehicle, amount: Float): Boolean = {
    val isNotFull = (vehicle.NtuCapacitor += amount) < vehicle.Definition.MaxNtuCapacitor
    UpdateNtuUI(vehicle)
    isNotFull
  }

  /** Discharging */
  def HandleDischargingEvent(target: TransferContainer): Boolean = {
    //log.trace(s"NtuDischarging: Vehicle $guid is discharging NTU into silo $silo_guid")
    val obj = ChargeTransferObject
    if (obj.NtuCapacitor > 0) {
      panelAnimationFunc = InitialDischarge
      transferTarget = Some(target)
      transferEvent = TransferBehavior.Event.Discharging
      target.Actor ! Ntu.Offer(obj)
      ntuChargingTick.cancel()
      ntuChargingTick = context.system.scheduler.scheduleOnce(
        delay = 1000 milliseconds,
        self,
        TransferBehavior.Discharging(TransferMaterial)
      )
      true
    } else {
      TryStopChargingEvent(obj)
      false
    }
  }

  def NoCharge(): Unit = {}

  def InitialCharge(): Unit = {
    panelAnimationFunc = NoCharge
    val obj = ChargeTransferObject
    ActivatePanelsForChargingEvent(obj)
    StartNtuChargingEvent(obj)
  }

  def InitialDischarge(): Unit = {
    panelAnimationFunc = NoCharge
    ActivatePanelsForChargingEvent(ChargeTransferObject)
  }

  def WithdrawAndTransmit(vehicle: Vehicle, maxRequested: Float): Any = {
    val chargeable      = ChargeTransferObject
    var chargeToDeposit = Math.min(Math.min(chargeable.NtuCapacitor, 100), maxRequested)
    chargeable.NtuCapacitor -= chargeToDeposit
    UpdateNtuUI(chargeable)
    Ntu.Grant(chargeable, chargeToDeposit)
  }

  /** Stopping */
  override def TryStopChargingEvent(container: TransferContainer): Unit = {
    val vehicle = ChargeTransferObject
    ntuChargingTick.cancel()
    if (transferEvent != TransferBehavior.Event.None) {
      if (vehicle.DeploymentState == DriveState.Deployed) {
        //turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT first
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
        ntuChargingTick = context.system.scheduler.scheduleOnce(250 milliseconds, self, TransferBehavior.Stopping())
      } else {
        //vehicle is not deployed; just do cleanup
        val vguid  = vehicle.GUID
        val zone   = vehicle.Zone
        val zoneId = zone.id
        val events = zone.VehicleEvents
        if (transferEvent == TransferBehavior.Event.Charging) {
          events ! VehicleServiceMessage(
            zoneId,
            VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)
          ) // panel glow off
          events ! VehicleServiceMessage(
            zoneId,
            VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 49, 0L)
          ) // orb particle effect off
        } else if (transferEvent == TransferBehavior.Event.Discharging) {
          events ! VehicleServiceMessage(
            zoneId,
            VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)
          ) // panel glow off
        }
      }
      panelAnimationFunc = NoCharge
      super.TryStopChargingEvent(vehicle)
    }
  }

  def StopNtuBehavior(sender: ActorRef): Unit = TryStopChargingEvent(ChargeTransferObject)

  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit = {}

  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit = {
    if (transferEvent == TransferBehavior.Event.Discharging) {
      val chargeable = ChargeTransferObject
      val chargeToDeposit = if (min == 0) {
        transferTarget match {
          case Some(silo: ResourceSilo) =>
            // Silos would charge from 0-100% in roughly 105s on live (~20%-100% https://youtu.be/veOWToR2nSk?t=1402)
            scala.math.min(scala.math.min(silo.MaxNtuCapacitor / 105, chargeable.NtuCapacitor), max)
          case _ =>
            0
        }
      } else {
        scala.math.min(min, chargeable.NtuCapacitor)
      }
      //      var chargeToDeposit = Math.min(Math.min(chargeable.NtuCapacitor, 100), max)
      chargeable.NtuCapacitor -= chargeToDeposit
      UpdateNtuUI(chargeable)
      sender ! Ntu.Grant(chargeable, chargeToDeposit)
    }
  }

  def HandleNtuGrant(sender: ActorRef, src: NtuContainer, amount: Float): Unit = {
    if (transferEvent == TransferBehavior.Event.Charging) {
      val obj = ChargeTransferObject
      if (ReceiveAndDepositUntilFull(obj, amount)) {
        panelAnimationFunc()
      } else {
        TryStopChargingEvent(obj)
        sender ! Ntu.Request(0, 0)
      }
    }
  }
}
