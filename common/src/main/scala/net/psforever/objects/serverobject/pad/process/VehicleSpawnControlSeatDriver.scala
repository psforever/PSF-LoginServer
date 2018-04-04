// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * This object forces the prospective driver to take the driver seat.
  * Three separate but sequentially significant steps occur within the scope of this object.
  * First, this step waits for the vehicle to be completely ready to accept the driver.
  * Second, this step triggers the player to actually be moved into the driver seat.
  * Finally, this step waits until the driver is properly in the driver seat.
  * It has failure cases should the driver or the vehicle be in an incorrect state.
  * @see `ZonePopulationActor`
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlSeatDriver(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-usher"

  val railJack = context.actorOf(Props(classOf[VehicleSpawnControlRailJack], pad), s"${context.parent.path.name}-rails")

  def receive : Receive = {
    case VehicleSpawnControl.Process.SeatDriver(entry) =>
      if(entry.vehicle.Actor == ActorRef.noSender) { //wait for the component of the vehicle needed for seating to be loaded
        context.system.scheduler.scheduleOnce(50 milliseconds, railJack, VehicleSpawnControl.Process.SeatDriver(entry))
      }
      else {
        val driver = entry.driver
        if(entry.vehicle.Health == 0) {
          //TODO detach vehicle from pad rails if necessary
          trace("vehicle was already destroyed; clean it up")
          VehicleSpawnControl.DisposeSpawnedVehicle(entry.vehicle, driver, Continent)
          context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
        }
        else if(entry.sendTo != ActorRef.noSender && driver.isAlive && driver.Continent == Continent.Id && driver.VehicleSeated.isEmpty) {
          trace("driver to be made seated in vehicle")
          entry.sendTo ! VehicleSpawnPad.StartPlayerSeatedInVehicle(entry.vehicle)
          entry.vehicle.Actor.tell(Mountable.TryMount(driver, 0), entry.sendTo) //entry.sendTo should handle replies to TryMount
          context.system.scheduler.scheduleOnce(1000 milliseconds, self, VehicleSpawnControl.Process.AwaitDriverInSeat(entry))
        }
        else {
          trace("driver lost; vehicle stranded on pad")
          context.system.scheduler.scheduleOnce(1000 milliseconds, railJack, VehicleSpawnControl.Process.RailJackAction(entry))
        }
      }

    case VehicleSpawnControl.Process.AwaitDriverInSeat(entry) =>
      val driver = entry.driver
      if(entry.vehicle.Health == 0) {
        //TODO detach vehicle from pad rails if necessary
        trace("vehicle was already destroyed; clean it up")
        VehicleSpawnControl.DisposeSpawnedVehicle(entry.vehicle, driver, Continent)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }
      else if(entry.sendTo == ActorRef.noSender) {
        trace("driver lost, but operations can continue")
        self ! VehicleSpawnControl.Process.RailJackAction(entry)
      }
      else if(driver.isAlive && driver.Continent == Continent.Id && driver.VehicleSeated.isEmpty) {
        context.system.scheduler.scheduleOnce(1000 milliseconds, self, VehicleSpawnControl.Process.AwaitDriverInSeat(entry))
      }
      else {
        trace(s"driver is sitting down")
        context.system.scheduler.scheduleOnce(1000 milliseconds, self, VehicleSpawnControl.Process.DriverInSeat(entry))
      }

    case VehicleSpawnControl.Process.DriverInSeat(entry) =>
      if(entry.vehicle.Health == 0) {
        //TODO detach vehicle from pad rails if necessary
        trace(s"vehicle was already destroyed; clean it up")
        VehicleSpawnControl.DisposeSpawnedVehicle(entry.vehicle, entry.driver, Continent)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }
      else if(entry.sendTo != ActorRef.noSender) {
        trace(s"driver ${entry.driver.Name} has taken the wheel")
        entry.sendTo ! VehicleSpawnPad.PlayerSeatedInVehicle(entry.vehicle)
        context.system.scheduler.scheduleOnce(10 milliseconds, railJack, VehicleSpawnControl.Process.RailJackAction(entry))
      }
      else {
        trace("driver lost, but operations can continue")
        context.system.scheduler.scheduleOnce(10 milliseconds, railJack, VehicleSpawnControl.Process.RailJackAction(entry))
      }

    case VehicleSpawnControl.ProcessControl.Reminder =>
      context.parent ! VehicleSpawnControl.ProcessControl.Reminder

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder

    case _ => ;
  }
}
