// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * This object forces the prospective driver to take the driver seat.
  * Multiple separate but sequentially significant steps occur within the scope of this object.
  * First, this step waits for the vehicle to be completely ready to accept the driver.
  * Second, this step triggers the player to actually be moved into the driver seat.
  * Finally, this step waits until the driver is properly in the driver seat.
  * It has failure cases should the driver or the vehicle be in an incorrect state.
  * @see `ZonePopulationActor`
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlSeatDriver(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-usher"

  val vehicleOverride = context.actorOf(Props(classOf[VehicleSpawnControlServerVehicleOverride], pad), s"${context.parent.path.name}-override")

  def receive : Receive = {
    case order @ VehicleSpawnControl.Order(_, vehicle) =>
      if(vehicle.Actor == ActorRef.noSender) { //wait for a necessary vehicle component to be loaded
        context.system.scheduler.scheduleOnce(50 milliseconds, self, order)
      }
      else {
        trace("vehicle ready")
        self ! VehicleSpawnControlSeatDriver.BeginDriverInSeat(order)
      }

    case VehicleSpawnControlSeatDriver.BeginDriverInSeat(entry) =>
      val driver = entry.driver
      if(entry.vehicle.Health > 0 && driver.isAlive && driver.Continent == pad.Continent && driver.VehicleSeated.isEmpty) {
        trace("driver to be made seated in vehicle")
        pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.StartPlayerSeatedInVehicle(entry.driver.Name, entry.vehicle, pad)
      }
      else{
        trace("driver lost; vehicle stranded on pad")
      }
      context.system.scheduler.scheduleOnce(2500 milliseconds, self, VehicleSpawnControlSeatDriver.DriverInSeat(entry))

    case VehicleSpawnControlSeatDriver.DriverInSeat(entry) =>
      if(entry.driver.isAlive && entry.vehicle.PassengerInSeat(entry.driver).contains(0)) {
        trace(s"driver ${entry.driver.Name} has taken the wheel")
        pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.PlayerSeatedInVehicle(entry.driver.Name, entry.vehicle, pad)
      }
      else {
        trace("driver lost, but operations can continue")
      }
      context.system.scheduler.scheduleOnce(250 milliseconds, vehicleOverride, entry)

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

object VehicleSpawnControlSeatDriver {
  final case class BeginDriverInSeat(entry : VehicleSpawnControl.Order)

  final case class DriverInSeat(entry : VehicleSpawnControl.Order)
}
