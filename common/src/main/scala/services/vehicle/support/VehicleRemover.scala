// Copyright (c) 2017 PSForever
package services.vehicle.support

import net.psforever.objects.Vehicle
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.zones.Zone
import net.psforever.types.DriveState
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
    val vehicle = entry.obj.asInstanceOf[Vehicle]
    val vehicleGUID = vehicle.GUID
    val zoneId = entry.zone.Id
    vehicle.Actor ! Vehicle.PrepareForDeletion
    //kick out all passengers
    vehicle.Definition.MountPoints.values.foreach(mount => {
      val seat = vehicle.Seat(mount).get
      seat.Occupant match {
        case Some(tplayer) =>
          seat.Occupant = None
          tplayer.VehicleSeated = None
          if(tplayer.HasGUID) {
            context.parent ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(tplayer.GUID, 4, false, vehicleGUID))
          }
        case None => ;
      }
    })
  }

  override def SecondJob(entry : RemoverActor.Entry) : Unit = {
    val vehicle = entry.obj.asInstanceOf[Vehicle]
    val zone = entry.zone
    vehicle.DeploymentState = DriveState.Mobile
    zone.Transport ! Zone.Vehicle.Despawn(vehicle)
    context.parent ! VehicleServiceMessage(zone.Id, VehicleAction.UnloadVehicle(Service.defaultPlayerGUID, zone, vehicle, vehicle.GUID))
    super.SecondJob(entry)
  }

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean = entry.obj.asInstanceOf[Vehicle].Seats.values.count(_.isOccupied) == 0

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    GUIDTask.UnregisterVehicle(entry.obj.asInstanceOf[Vehicle])(entry.zone.GUID)
  }
}
