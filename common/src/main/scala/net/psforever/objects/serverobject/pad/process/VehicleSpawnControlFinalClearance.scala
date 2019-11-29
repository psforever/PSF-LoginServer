// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3
import services.vehicle.{VehicleAction, VehicleServiceMessage}

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
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad)
      if(vehicle.PassengerInSeat(driver).isEmpty) {
        //ensure the vehicle is outside of the trench
        val zone = pad.Owner.Zone
        val z = (zone.Map
          .TerminalToSpawnPad
          .find { case (_, b) => b == pad.GUID.guid } match {
          case Some((a, _)) => zone.GUID(a)
          case None => None
        }) match {
          //most terminals are at least as high as the pad
          case Some(term : Terminal) => (term.Position.z + pad.Position.z) / 2
          case _ => pad.Position.z + 9
        }
        vehicle.Position = pad.Position.xy + Vector3.z(z)
        val definition = vehicle.Definition
        pad.Owner.Zone.VehicleEvents ! VehicleServiceMessage(s"${pad.Continent}", VehicleAction.LoadVehicle(PlanetSideGUID(0), vehicle, definition.ObjectId, vehicle.GUID, definition.Packet.ConstructorData(vehicle).get))
      }
      context.parent ! VehicleSpawnControl.ProcessControl.Reminder
      self ! VehicleSpawnControlFinalClearance.Test(order)

    case test @ VehicleSpawnControlFinalClearance.Test(entry) =>
      if(Vector3.DistanceSquared(entry.vehicle.Position, pad.Position) > 100.0f) { //10m away from pad
        trace("pad cleared")
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }
      else {
        context.system.scheduler.scheduleOnce(2000 milliseconds, self, test)
      }

    case _ => ;
  }
}

object VehicleSpawnControlFinalClearance {
  private final case class Test(entry : VehicleSpawnControl.Order)
}
