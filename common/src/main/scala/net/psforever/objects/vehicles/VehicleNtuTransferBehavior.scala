// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{ActorRef, Cancellable}
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.ntutransfer.NtuTransferBehavior
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.{NtuContainer, _}
import net.psforever.types.DriveState
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait VehicleNtuTransferBehavior extends NtuTransferBehavior {
  var ntuChargingTick : Cancellable = Default.Cancellable

  def NtuChargeableObject : Vehicle with NtuContainer

  def ActivatePanelsForChargingEvent(vehicle : NtuContainer) : Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.Id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 52, 1L)
    ) // panel glow on
  }

  /** Charging */
  def StartNtuChargingEvent(vehicle : NtuContainer) : Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.Id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 49, 1L)
    ) // orb particle effect on
  }

  def UpdateNtuUI(vehicle : Vehicle with NtuContainer) : Unit = {
    if(vehicle.Seats.values.exists(_.isOccupied)) {
      val display = scala.math.ceil((vehicle.NtuCapacitor.toFloat / vehicle.Definition.MaxNtuCapacitor.toFloat) * 10).toLong
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
        vehicle.Actor.toString,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 45, display)
      )
    }
  }

  def HandleNtuCharging(vehicle : NtuContainer, target : NtuContainer) : Boolean = {
    ntuChargingTick.cancel
    //log.trace(s"NtuCharging: Vehicle $guid is charging NTU capacitor.")
    if(vehicle.NtuCapacitor < vehicle.Definition.MaxNtuCapacitor) {
      //charging
      ntuChargingTarget = Some(target)
      ntuChargingEvent = Ntu.ChargeEvent.Charging
      val max = vehicle.Definition.MaxNtuCapacitor - vehicle.NtuCapacitor
      target.Actor ! Ntu.Request(scala.math.min(0, max), max) //warp gates only?
      ntuChargingTick = context.system.scheduler.scheduleOnce(delay = 1000 milliseconds, self, NtuTransferBehavior.Charging()) // Repeat until fully charged, or minor delay
      true
    }
    else {
      // Fully charged
      TryStopChargingEvent(vehicle)
      false
    }
  }

  def InitialCharge(container : NtuContainer) : Unit = {
    ActivatePanelsForChargingEvent(container)
    StartNtuChargingEvent(container)
  }

  def IncrementalCharge(container : NtuContainer) : Unit = {}

  def FinalCharge(container : NtuContainer) : Unit = {}

  def ReceiveAndDeposit(vehicle : Vehicle, amount : Int) : Boolean = {
    val isFull = (vehicle.NtuCapacitor += amount) == vehicle.Definition.MaxNtuCapacitor
    UpdateNtuUI(vehicle)
    isFull
  }

  /** Discharging */
  def HandleNtuDischarging(vehicle : NtuContainer, target : NtuContainer) : Boolean = {
    //log.trace(s"NtuDischarging: Vehicle $guid is discharging NTU into silo $silo_guid")
    if(vehicle.NtuCapacitor > 0) {
      // Make sure we don't exceed the silo maximum charge or remove much NTU from ANT if maximum is reached, or try to make ANT go below 0 NTU
      ntuChargingTarget = Some(target)
      ntuChargingEvent = Ntu.ChargeEvent.Discharging
      target.Actor ! Ntu.Offer()
      ntuChargingTick.cancel
      ntuChargingTick = context.system.scheduler.scheduleOnce(delay = 1000 milliseconds, self, NtuTransferBehavior.Discharging())
      true
    }
    else {
      TryStopChargingEvent(vehicle)
      false
    }
  }

  def InitialDischarge(container : NtuContainer) : Unit = {
    ActivatePanelsForChargingEvent(container)
  }

  def IncrementalDischarge(container : NtuContainer) : Unit = {}

  def FinalDischarge(container : NtuContainer) : Unit = {}

  def WithdrawAndTransmit(vehicle : Vehicle, maxRequested : Int) : Any = {
    val chargeable = NtuChargeableObject
    var chargeToDeposit = Math.min(Math.min(chargeable.NtuCapacitor, 100), maxRequested)
    chargeable.NtuCapacitor -= chargeToDeposit
    UpdateNtuUI(chargeable)
    Ntu.Grant(chargeToDeposit)
  }

  /** Stopping */
  override def TryStopChargingEvent(container : NtuContainer) : Unit = {
    val vehicle = NtuChargeableObject
    ntuChargingTick.cancel
    if(ntuChargingEvent != Ntu.ChargeEvent.None) {
      if(vehicle.DeploymentState == DriveState.Deployed) {
        //turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT first
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
        ntuChargingTick = context.system.scheduler.scheduleOnce(250 milliseconds, self, NtuTransferBehavior.Stopping())
      }
      else {
        //vehicle is not deployed; just do cleanup
        val vguid = vehicle.GUID
        val zone = vehicle.Zone
        val zoneId = zone.Id
        val events = zone.VehicleEvents
        if(ntuChargingEvent == Ntu.ChargeEvent.Charging) {
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)) // panel glow off
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 49, 0L)) // orb particle effect off
        }
        else if(ntuChargingEvent == Ntu.ChargeEvent.Discharging) {
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)) // panel glow off
          ntuChargingTarget match {
            case Some(obj : ResourceSilo) =>
              obj.Actor ! Ntu.Grant(0)
            case _ => ;
          }
        }
        super.TryStopChargingEvent(vehicle)
      }
    }
  }

  def HandleNtuOffer(sender : ActorRef) : Unit = {}

  def StopNtuBehavior(sender : ActorRef) : Unit = TryStopChargingEvent(NtuChargeableObject)

  def HandleNtuRequest(sender : ActorRef, min : Int, max : Int) : Unit = {
    if(ntuChargingEvent == Ntu.ChargeEvent.Discharging) {
      sender ! WithdrawAndTransmit(NtuChargeableObject, max)
    }
  }

  def HandleNtuGrant(sender : ActorRef, amount : Int) : Unit = {
    if(ntuChargingEvent == Ntu.ChargeEvent.Charging) {
      val obj = NtuChargeableObject
      if(ReceiveAndDeposit(obj, amount)) {
        TryStopChargingEvent(obj)
        sender ! Ntu.Request(0, 0)
      }
    }
  }
}
