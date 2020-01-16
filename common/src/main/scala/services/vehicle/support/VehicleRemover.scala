// Copyright (c) 2017 PSForever
package services.vehicle.support

import net.psforever.objects.Vehicle
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.zones.Zone
import net.psforever.types.{DriveState, PlanetSideGUID}
import services.{RemoverActor, Service}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

class VehicleRemover extends RemoverActor {
  final val FirstStandardDuration : FiniteDuration = 5 minutes

  final val SecondStandardDuration : FiniteDuration = 5 seconds

  def InclusionTest(entry : RemoverActor.Entry) : Boolean = {
    entry.obj.isInstanceOf[Vehicle]
  }

  def InitialJob(entry : RemoverActor.Entry) : Unit = { }

  def FirstJob(entry : RemoverActor.Entry) : Unit = {
    val vehicleGUID = entry.obj.GUID
    entry.zone.GUID(vehicleGUID) match {
      case Some(vehicle : Vehicle) if vehicle.HasGUID =>
        val zoneId = entry.zone.Id
        vehicle.Actor ! Vehicle.PrepareForDeletion()
        //escape being someone else's cargo
        (vehicle.MountedIn match {
          case Some(carrierGUID) =>
            entry.zone.Vehicles.find(v => v.GUID == carrierGUID)
          case None =>
            None
        }) match {
          case Some(carrier : Vehicle) =>
            val driverName = carrier.Seats(0).Occupant match {
              case Some(driver) => driver.Name
              case _ => zoneId
            }
            context.parent ! VehicleServiceMessage(s"$driverName", VehicleAction.ForceDismountVehicleCargo(PlanetSideGUID(0), vehicleGUID, true, false, false))
          case _ => ;
        }
        //kick out all passengers
        vehicle.Seats.values.foreach(seat => {
          seat.Occupant match {
            case Some(tplayer) =>
              seat.Occupant = None
              tplayer.VehicleSeated = None
              if(tplayer.HasGUID) {
                context.parent ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(tplayer.GUID, 4, false, vehicleGUID))
              }
            case None => ;
          }
          //abandon all cargo
          vehicle.CargoHolds.values
            .collect { case hold if hold.isOccupied =>
              val cargo = hold.Occupant.get
              context.parent ! VehicleServiceMessage(zoneId, VehicleAction.ForceDismountVehicleCargo(PlanetSideGUID(0), cargo.GUID, true, false, false))
            }
        })
      case _ => ;
    }
  }

  override def SecondJob(entry : RemoverActor.Entry) : Unit = {
    val vehicleGUID = entry.obj.GUID
    entry.zone.GUID(vehicleGUID) match {
      case Some(vehicle : Vehicle) if vehicle.HasGUID =>
        val zone = entry.zone
        vehicle.DeploymentState = DriveState.Mobile
        zone.Transport ! Zone.Vehicle.Despawn(vehicle)
        context.parent ! VehicleServiceMessage(zone.Id, VehicleAction.UnloadVehicle(Service.defaultPlayerGUID, zone, vehicle, vehicleGUID))
        super.SecondJob(entry)
      case _ => ;
    }
  }

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean = entry.obj.asInstanceOf[Vehicle].Seats.values.count(_.isOccupied) == 0

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    GUIDTask.UnregisterVehicle(entry.obj.asInstanceOf[Vehicle])(entry.zone.GUID)
  }
}
