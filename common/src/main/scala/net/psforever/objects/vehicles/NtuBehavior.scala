// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.entity.{Identifiable, WorldEntity}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{StructureType, WarpGate}
import net.psforever.objects.zones.ZoneAware
import net.psforever.objects.{Default, Vehicle}
import net.psforever.types.{DriveState, Vector3}
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait NtuBehavior {
  _ : Actor =>
  var ntuChargingEvent : NtuBehavior.ChargeEvent.Value = NtuBehavior.ChargeEvent.None
  var ntuChargingTarget : Option[PlanetSideServerObject] = None
  var ntuChargingTick : Cancellable = Default.Cancellable

  def NtuChargeableObject : Vehicle

  val ntuBehavior : Receive = {
    case NtuBehavior.Charging()
      if ntuChargingEvent == NtuBehavior.ChargeEvent.None || ntuChargingEvent == NtuBehavior.ChargeEvent.Charging =>
      TryChargingActivity()

    case NtuBehavior.Discharging()
      if ntuChargingEvent == NtuBehavior.ChargeEvent.None || ntuChargingEvent == NtuBehavior.ChargeEvent.Discharging =>
      TryDischargingActivity()

    case NtuBehavior.Stopping() =>
      TryStopChargingEvent(NtuChargeableObject)

    case NtuBehavior.Charging() | NtuBehavior.Discharging() => ; //message while in wrong state

    case Ntu.Grant(0) | Ntu.Request(0, 0) =>
      TryStopChargingEvent(NtuChargeableObject)

    case Ntu.Request(0, max)
      if ntuChargingEvent == NtuBehavior.ChargeEvent.Discharging =>
      sender ! WithdrawAndTransmit(NtuChargeableObject, max)

    case Ntu.Grant(amount)
      if ntuChargingEvent == NtuBehavior.ChargeEvent.Charging =>
      val obj = NtuChargeableObject
      if(ReceiveAndDeposit(obj, amount)) {
        TryStopChargingEvent(obj)
        sender ! Ntu.Request(0,0)
      }
  }

  /** Charging */
  def TryChargingActivity() : Unit = {
    if(ntuChargingEvent != NtuBehavior.ChargeEvent.Discharging) {
      val chargeable = NtuChargeableObject
      NtuBehavior.FindChargingSource(chargeable, ntuChargingTarget) match {
        case Some(obj) if ntuChargingEvent == NtuBehavior.ChargeEvent.None =>
          if(HandleNtuCharging(chargeable, obj)) {
            StartNtuChargingEvent(chargeable)
            UpdateNtuUI(chargeable)
          }
        case Some(obj) =>
          HandleNtuCharging(chargeable, obj)
          UpdateNtuUI(chargeable)
        case None if ntuChargingEvent == NtuBehavior.ChargeEvent.Charging =>
          TryStopChargingEvent(chargeable)
        case _ => ;
      }
    }
  }

  def StartNtuChargingEvent(vehicle : Vehicle) : Unit = {
    ActivatePanelsForChargingEvent(vehicle)
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.Id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 49, 1L)
    ) // orb particle effect on
  }

  def ActivatePanelsForChargingEvent(vehicle : Vehicle) : Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.Id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 52, 1L)
    ) // panel glow on
  }

  def UpdateNtuUI(vehicle : Vehicle) : Unit = {
    if(vehicle.Seats.values.exists(_.isOccupied)) {
      val display = scala.math.ceil((vehicle.NtuCapacitor.toFloat / vehicle.Definition.MaxNtuCapacitor.toFloat) * 10).toLong
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
        vehicle.Actor.toString,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 45, display)
      )
    }
  }

  def HandleNtuCharging(vehicle : Vehicle, target : PlanetSideServerObject) : Boolean = {
    ntuChargingTick.cancel
    //log.trace(s"NtuCharging: Vehicle $guid is charging NTU capacitor.")
    if(vehicle.NtuCapacitor < vehicle.Definition.MaxNtuCapacitor) {
      //charging
      ntuChargingTarget = Some(target)
      ntuChargingEvent = NtuBehavior.ChargeEvent.Charging
      target.Actor ! Ntu.Request(0, vehicle.Definition.MaxNtuCapacitor - vehicle.NtuCapacitor)
      ntuChargingTick = context.system.scheduler.scheduleOnce(delay = 1000 milliseconds, self, NtuBehavior.Charging()) // Repeat until fully charged, or minor delay
      true
    }
    else {
      // Fully charged
      TryStopChargingEvent(vehicle)
      false
    }
  }

  def ReceiveAndDeposit(vehicle : Vehicle, amount : Int) : Boolean = {
    val isFull = (vehicle.NtuCapacitor += amount) == vehicle.Definition.MaxNtuCapacitor
    UpdateNtuUI(vehicle)
    isFull
  }

  /** Discharging */
  def TryDischargingActivity() : Unit = {
    if(ntuChargingEvent != NtuBehavior.ChargeEvent.Charging) {
      val chargeable = NtuChargeableObject
      //determine how close we are to something that we can discharge into
      NtuBehavior.FindDischargingTarget(chargeable, ntuChargingTarget) match {
        case Some(obj) if ntuChargingEvent == NtuBehavior.ChargeEvent.None =>
          if(HandleNtuDischarging(NtuChargeableObject, obj)) {
            ActivatePanelsForChargingEvent(chargeable)
          }
        case Some(obj) =>
          HandleNtuDischarging(chargeable, obj)
          UpdateNtuUI(chargeable)
        case None if ntuChargingEvent == NtuBehavior.ChargeEvent.Discharging =>
          TryStopChargingEvent(chargeable)
        case _ => ;
      }
    }
  }

  def HandleNtuDischarging(vehicle : Vehicle, target : PlanetSideServerObject) : Boolean = {
    //log.trace(s"NtuDischarging: Vehicle $guid is discharging NTU into silo $silo_guid")
    if(vehicle.NtuCapacitor > 0) {
      // Make sure we don't exceed the silo maximum charge or remove much NTU from ANT if maximum is reached, or try to make ANT go below 0 NTU
      ntuChargingTarget = Some(target)
      ntuChargingEvent = NtuBehavior.ChargeEvent.Discharging
      target.Actor ! Ntu.Offer()
      ntuChargingTick.cancel
      ntuChargingTick = context.system.scheduler.scheduleOnce(delay = 1000 milliseconds, self, NtuBehavior.Discharging())
      true
    }
    else {
      TryStopChargingEvent(vehicle)
      false
    }
  }

  def WithdrawAndTransmit(vehicle : Vehicle, maxRequested : Int) : Any = {
    val chargeable = NtuChargeableObject
    var chargeToDeposit = Math.min(Math.min(chargeable.NtuCapacitor, 100), maxRequested)
    chargeable.NtuCapacitor -= chargeToDeposit
    UpdateNtuUI(chargeable)
    Ntu.Grant(chargeToDeposit)
  }

  def _HandleNtuDischarging(vehicle : Vehicle, target : PlanetSideServerObject) : Boolean = {
    val zone = vehicle.Zone
    //log.trace(s"NtuDischarging: Vehicle $guid is discharging NTU into silo $silo_guid")
    val silo = target.asInstanceOf[ResourceSilo]
    if(vehicle.NtuCapacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {
      // Make sure we don't exceed the silo maximum charge or remove much NTU from ANT if maximum is reached, or try to make ANT go below 0 NTU
      ntuChargingTarget = Some(target)
      ntuChargingEvent = NtuBehavior.ChargeEvent.Discharging
      var chargeToDeposit = Math.min(Math.min(vehicle.NtuCapacitor, 100), silo.MaximumCharge - silo.ChargeLevel)
      val isEmpty = (vehicle.NtuCapacitor -= chargeToDeposit) == 0
      silo.Actor ! ResourceSilo.UpdateChargeLevel(chargeToDeposit)
      zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, silo.GUID, 49, 1L)) // panel glow on & orb particles on
      UpdateNtuUI(vehicle)
      //todo: grant BEP to user
      //todo: grant BEP to squad in range
      //todo: handle silo orb / panel glow properly if more than one person is refilling silo and one stops. effects should stay on until all players stop
      //log.trace(s"NtuDischarging: ANT not empty and Silo not full. Scheduling another discharge")
      val nextChargeTime = if(isEmpty) 10 else 1000
      ntuChargingTick.cancel
      ntuChargingTick = context.system.scheduler.scheduleOnce(nextChargeTime milliseconds, self, NtuBehavior.Discharging())
      true
    }
    else {
      TryStopChargingEvent(vehicle)
      false
    }
  }

  /** Stopping */
  def TryStopChargingEvent(vehicle : Vehicle) : Unit = {
    ntuChargingTick.cancel
    if(ntuChargingEvent != NtuBehavior.ChargeEvent.None) {
      if(vehicle.DeploymentState == DriveState.Deployed) {
        //turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT first
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
        ntuChargingTick = context.system.scheduler.scheduleOnce(250 milliseconds, self, NtuBehavior.Stopping())
      }
      else {
        //vehicle is not deployed; just do cleanup
        val vguid = vehicle.GUID
        val zone = vehicle.Zone
        val zoneId = zone.Id
        val events = zone.VehicleEvents
        if(ntuChargingEvent == NtuBehavior.ChargeEvent.Charging) {
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)) // panel glow off
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 49, 0L)) // orb particle effect off
        }
        else if(ntuChargingEvent == NtuBehavior.ChargeEvent.Discharging) {
          events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, 52, 0L)) // panel glow off
          ntuChargingTarget match {
            case Some(obj : ResourceSilo) =>
              obj.Actor ! Ntu.Grant(0)
            case _ => ;
          }
        }
        ntuChargingEvent = NtuBehavior.ChargeEvent.None
        ntuChargingTarget = None
      }
    }
  }
}

object NtuBehavior {
  object ChargeEvent extends Enumeration {
    val
    None,
    Charging,
    Discharging
    = Value
  }

  final case class Charging()

  final case class Discharging()

  final case class Stopping()

  def FindChargingSource(obj : Vehicle, ntuChargingTarget : Option[PlanetSideServerObject]) : Option[PlanetSideServerObject] = {
    //determine if we are close enough to charge from something
    //TODO BFR's can siphon from NTU silos too
    (ntuChargingTarget match {
      case out @ Some(target : WarpGate) if {
        val soiRadius = target.Definition.SOIRadius
        Vector3.DistanceSquared(obj.Position.xy, target.Position.xy) < soiRadius * soiRadius
      } =>
        out
      case None =>
        None
    }).orElse {
      val position = obj.Position.xy
      obj.Zone.Buildings.values
        .collect { case gate : WarpGate => gate }
        .find { gate =>
          val soiRadius = gate.Definition.SOIRadius
          Vector3.DistanceSquared(position, gate.Position) < soiRadius * soiRadius
        }
    }
  }

  def FindDischargingTarget(obj : Vehicle, ntuChargingTarget : Option[PlanetSideServerObject]) : Option[PlanetSideServerObject] = {
    (ntuChargingTarget match {
      case out @ Some(target : ResourceSilo) if {
        Vector3.DistanceSquared(obj.Position.xy, target.Position.xy) < 400 //20m is generous ...
      } =>
        out
      case _ =>
        None
    }).orElse {
      val position = obj.Position.xy
      obj.Zone.Buildings.values
        .find { building =>
          building.BuildingType == StructureType.Facility && {
            val soiRadius = building.Definition.SOIRadius
            Vector3.DistanceSquared(position, building.Position.xy) < soiRadius * soiRadius
          }
        } match {
        case Some(building) =>
          building.Amenities.find { amenity =>
            amenity.isInstanceOf[ResourceSilo] && Vector3.DistanceSquared(position, amenity.Position.xy) < 400 //20m is generous ...
          }
        case None =>
          None
      }
    }
  }
}

object Ntu {
  final case class Offer()
  final case class Request(min : Int, max : Int)
  final case class Grant(amount : Int)
}

trait NtuContainer extends Identifiable
  with ZoneAware
  with WorldEntity {
  def NtuCapacitor : Int

  def NtuCapacitor_=(value: Int) : Int

  def Definition : NtuContainerDefinition
}

trait CommonNtuContainer extends NtuContainer {
  private var ntuCapacitor : Int = 0

  def NtuCapacitor : Int = ntuCapacitor

  def NtuCapacitor_=(value: Int) : Int = {
    ntuCapacitor = scala.math.min(0, scala.math.max(value, Definition.MaxNtuCapacitor))
    NtuCapacitor
  }

  def Definition : NtuContainerDefinition
}

trait NtuContainerDefinition {
  private var maxNtuCapacitor : Int = 0

  def MaxNtuCapacitor : Int = maxNtuCapacitor

  def MaxNtuCapacitor_=(max: Int) : Int = {
    maxNtuCapacitor = max
    MaxNtuCapacitor
  }
}
