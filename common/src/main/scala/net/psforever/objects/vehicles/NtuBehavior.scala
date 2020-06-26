// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{StructureType, WarpGate}
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

    case NtuBehavior.Discharging if ntuChargingEvent == NtuBehavior.ChargeEvent.StopDischarging =>
      TryStopDischarging(NtuChargeableObject)

    case NtuBehavior.Charging | NtuBehavior.Discharging => ; //message while in wrong state
  }

  def TryChargingActivity() : Unit = {
    if(ntuChargingEvent != NtuBehavior.ChargeEvent.Discharging) {
      //determine how close we are to something that we can charge from
      FindChargingSource() match {
        case Some(obj) =>
          HandleNtuCharging(NtuChargeableObject, obj)
        case None if ntuChargingEvent == NtuBehavior.ChargeEvent.Charging =>
          StopCharging(NtuChargeableObject)
        case _ => ;
      }
    }
  }

  def FindChargingSource() : Option[PlanetSideServerObject] = {
    //TODO BFR's can siphon from NTU silos too
    (ntuChargingTarget match {
      case out @ Some(target : WarpGate) if {
        val soiRadius = target.Definition.SOIRadius
        Vector3.DistanceSquared(NtuChargeableObject.Position.xy, target.Position.xy) < soiRadius * soiRadius
      } =>
        out
      case None =>
        None
    }).orElse {
      val obj = NtuChargeableObject
      val position = obj.Position.xy
      obj.Zone.Buildings.values
        .collect { case gate : WarpGate => gate }
        .find { gate =>
          val soiRadius = gate.Definition.SOIRadius
          Vector3.DistanceSquared(position, gate.Position) < soiRadius * soiRadius
        }
    }
  }

  def HandleNtuCharging(vehicle : Vehicle, target : PlanetSideServerObject) : Unit = {
    val guid = vehicle.GUID
    val zone = vehicle.Zone
    val events = zone.VehicleEvents
    //log.trace(s"NtuCharging: Vehicle $guid is charging NTU capacitor.")
    if(vehicle.NtuCapacitor < vehicle.Definition.MaxNtuCapacitor) {
      //charging
      ntuChargingTarget = Some(target)
      ntuChargingEvent = NtuBehavior.ChargeEvent.Charging
      val isFull = (vehicle.NtuCapacitor += 100) == vehicle.Definition.MaxNtuCapacitor
      if(vehicle.Seats.values.exists(_.isOccupied)) {
        events ! VehicleServiceMessage(vehicle.Actor.toString, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, guid, 45, scala.math.ceil((vehicle.NtuCapacitor.toFloat / vehicle.Definition.MaxNtuCapacitor.toFloat) * 10).toLong)) // set ntu on vehicle UI
      }
      events ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, guid, 52, 1L)) // panel glow on
      events ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, guid, 49, 1L)) // orb particle effect on
      val nextChargeTime = if(isFull) 100 else 1000
      ntuChargingTick.cancel
      ntuChargingTick = context.system.scheduler.scheduleOnce(nextChargeTime milliseconds, self, NtuBehavior.Charging()) // Repeat until fully charged, or minor delay
    }
    else {
      // Fully charged
      println(s"fully changed")
      StopCharging(vehicle)
    }
  }

  def StopCharging(vehicle : Vehicle) : Unit = {
    ntuChargingTick.cancel
    ntuChargingTarget = None
    ntuChargingEvent = NtuBehavior.ChargeEvent.None
    if(vehicle.Seats.values.exists(_.isOccupied)) {
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(vehicle.Actor.toString, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 45, scala.math.ceil((vehicle.NtuCapacitor.toFloat / vehicle.Definition.MaxNtuCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI
    }
    //turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
    context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, self, Deployment.TryUndeploy(DriveState.Undeploying))
  }

  def TryDischargingActivity() : Unit = {
    if(ntuChargingEvent != NtuBehavior.ChargeEvent.Charging) {
      //determine how close we are to something that we can discharge into
      FindDischargingSource() match {
        case Some(obj) =>
          HandleNtuDischarging(NtuChargeableObject, obj)
        case None if ntuChargingEvent == NtuBehavior.ChargeEvent.Discharging =>
          TryStopDischarging(NtuChargeableObject)
        case _ => ;
      }
    }
  }

  def FindDischargingSource() : Option[PlanetSideServerObject] = {
    (ntuChargingTarget match {
      case out @ Some(_ : ResourceSilo) if {
        true//Vector3.DistanceSquared(NtuChargeableObject.Position.xy, target.Position.xy) < 400 //20m is generous ...
      } =>
        out
      case _ =>
        None
    }).orElse {
      val obj = NtuChargeableObject
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
            amenity.isInstanceOf[ResourceSilo] //&& Vector3.DistanceSquared(position, amenity.Position.xy) < 400 //20m is generous ...
          }
        case None =>
          None
      }
    }
  }

  def HandleNtuDischarging(vehicle : Vehicle, target : PlanetSideServerObject) : Unit = {
    val sguid = target.GUID
    val guid = vehicle.GUID
    val zone = vehicle.Zone
    val zoneId = zone.Id
    val events = zone.VehicleEvents
    val GUID0 = Service.defaultPlayerGUID
    //log.trace(s"NtuDischarging: Vehicle $guid is discharging NTU into silo $silo_guid")
    events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(GUID0, guid, 49, 0L)) // orb particle effect off
    events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(GUID0, guid, 52, 1L)) // panel glow on

    val silo = target.asInstanceOf[ResourceSilo]
    // Check vehicle is still deployed before continuing. User can undeploy manually or vehicle may not longer be present.
    if(vehicle.DeploymentState == DriveState.Deployed) {
      if(vehicle.NtuCapacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {
        // Make sure we don't exceed the silo maximum charge or remove much NTU from ANT if maximum is reached, or try to make ANT go below 0 NTU
        ntuChargingTarget = Some(target)
        ntuChargingEvent = NtuBehavior.ChargeEvent.Discharging
        var chargeToDeposit = Math.min(Math.min(vehicle.NtuCapacitor, 100), silo.MaximumCharge - silo.ChargeLevel)
        vehicle.NtuCapacitor -= chargeToDeposit
        silo.Actor ! ResourceSilo.UpdateChargeLevel(chargeToDeposit)
        if(vehicle.Seats.values.exists(_.isOccupied)) {
          events ! VehicleServiceMessage(vehicle.Actor.toString, VehicleAction.PlanetsideAttribute(GUID0, guid, 45, scala.math.ceil((vehicle.NtuCapacitor.toFloat / vehicle.Definition.MaxNtuCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI
        }
        events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(GUID0, sguid, 49, 1L)) // panel glow on & orb particles on

        //todo: grant BEP to user
        //todo: grant BEP to squad in range
        //todo: handle silo orb / panel glow properly if more than one person is refilling silo and one stops. effects should stay on until all players stop
        if(vehicle.NtuCapacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {
          //log.trace(s"NtuDischarging: ANT not empty and Silo not full. Scheduling another discharge")
          // Silo still not full and ant still has charge left - keep rescheduling ticks
          ntuChargingTick.cancel
          ntuChargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuBehavior.Discharging())
        }
        else {
          //log.trace(s"NtuDischarging: ANT NTU empty or Silo NTU full.")
          TryStopDischarging(vehicle)
        }
      }
      else {
        // This shouldn't normally be run, only if the client thinks the ANT has capacitor charge when it doesn't, or thinks the silo isn't full when it is.
        //log.warn(s"NtuDischarging: Invalid discharge state. ANT Capacitor: ${vehicle.NtuCapacitor} Silo Capacitor: ${silo.ChargeLevel}")
        TryStopDischarging(vehicle)
      }
    }
    else {
      TryStopDischarging(vehicle)
    }
  }

  def TryStopDischarging(vehicle : Vehicle) : Unit = {
    //turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
    if(vehicle.DeploymentState == DriveState.Deployed) {
      ntuChargingEvent = NtuBehavior.ChargeEvent.StopDischarging
      context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))
      ntuChargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuBehavior.Discharging())
    }
    else {
      //vehicle has changed from deployed and this should be the last timer tick sent
      val zone = vehicle.Zone
      val zoneId = zone.Id
      val events = zone.VehicleEvents
      events ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 52, 0L)) // panel glow off
      ntuChargingTarget match {
        case Some(obj : ResourceSilo) =>
          val zone = obj.Zone
          zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 49, 0L)) // panel glow off & orb particles off
        case _ => ;
      }
      ntuChargingEvent = NtuBehavior.ChargeEvent.None
      ntuChargingTarget = None
      ntuChargingTick.cancel
    }
  }
}

object NtuBehavior {
  object ChargeEvent extends Enumeration {
    val
    None,
    Charging,
    Discharging,
    StopDischarging
    = Value
  }

  final case class Charging()

  final case class Discharging()
}
