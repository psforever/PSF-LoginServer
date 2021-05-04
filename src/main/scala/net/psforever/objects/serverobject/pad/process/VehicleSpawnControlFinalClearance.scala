// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Cancellable
import net.psforever.objects.Default
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.types.{PlanetSideGUID, Vector3}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * There is nothing left to do except make certain that the vehicle has moved far enough away from the spawn pad
  * to not block the next order that may  be queued.
  * A long call is made to the root of this `Actor` object chain to start work on any subsequent vehicle order.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlFinalClearance(pad: VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-clearer"

  var temp: Cancellable = Default.Cancellable

  override def postStop() : Unit = {
    temp.cancel()
  }

  def receive: Receive = {
    case order @ VehicleSpawnControl.Order(_, vehicle) =>
      if (!vehicle.Seats(0).isOccupied) {
        //ensure the vacant vehicle is above the trench and the doors
        vehicle.Position = pad.Position + Vector3.z(pad.Definition.VehicleCreationZOffset)
        val definition = vehicle.Definition
        pad.Zone.VehicleEvents ! VehicleServiceMessage(
          s"${pad.Continent}",
          VehicleAction.LoadVehicle(
            PlanetSideGUID(0),
            vehicle,
            definition.ObjectId,
            vehicle.GUID,
            definition.Packet.ConstructorData(vehicle).get
          )
        )
      }
      context.parent ! VehicleSpawnControl.ProcessControl.Reminder
      self ! VehicleSpawnControlFinalClearance.Test(order)

    case test @ VehicleSpawnControlFinalClearance.Test(entry) =>
      //the vehicle has an initial decay of 30s in which time it needs to be mounted
      //once mounted, it will complain to the current driver that it is blocking the spawn pad
      //no time limit exists for that state
      val vehicle = entry.vehicle
      if (Vector3.DistanceSquared(vehicle.Position, pad.Position) > 144) { //12m away from pad
        trace("pad cleared")
        pad.Zone.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      } else if (vehicle.Destroyed) {
        trace("clearing the pad of vehicle wreckage")
        VehicleSpawnControl.DisposeVehicle(vehicle, pad.Zone)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      } else {
        temp = context.system.scheduler.scheduleOnce(2000 milliseconds, self, test)
      }

    case _ => ;
  }
}

object VehicleSpawnControlFinalClearance {
  private final case class Test(entry: VehicleSpawnControl.Order)
}
