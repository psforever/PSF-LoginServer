// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * There is nothing left to do
  * except make certain that the vehicle has moved far enough away from the spawn pad
  * to not block the next order that may  be queued.
  * A long call is made to the root of this `Actor` object chain to start work on any subsequent vehicle order.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlFinalClearance(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-clearer"

  def receive : Receive = {
    case VehicleSpawnControl.Process.FinalClearance(entry) =>
      if(Vector3.DistanceSquared(entry.vehicle.Position, pad.Position) > 100.0f) { //10m away from pad
        trace("pad cleared")
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }
      else {
        context.system.scheduler.scheduleOnce(2000 milliseconds, self, VehicleSpawnControl.Process.FinalClearance(entry))
      }

    case _ => ;
  }
}
