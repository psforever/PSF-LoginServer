// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.types.Vector3

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
    case VehicleSpawnControl.Process.SeatDriver(entry) =>
      self ! VehicleSpawnControlSeatDriver.AwaitVehicleReadiness(entry)

    case VehicleSpawnControlSeatDriver.AwaitVehicleReadiness(entry) =>
      if(entry.vehicle.Actor == ActorRef.noSender) { //wait for a necessary vehicle component to be loaded
        context.system.scheduler.scheduleOnce(50 milliseconds, self, VehicleSpawnControlSeatDriver.AwaitVehicleReadiness(entry))
      }
      else {
        trace("vehicle ready")
        self ! VehicleSpawnControlSeatDriver.BeginDriverInSeat(entry)
      }

    case VehicleSpawnControlSeatDriver.BeginDriverInSeat(entry) =>
      val driver = entry.driver
      if(entry.sendTo != ActorRef.noSender && entry.vehicle.Health > 0 && driver.isAlive && driver.Continent == Continent.Id && driver.VehicleSeated.isEmpty) {
        trace("driver to be made seated in vehicle")
        entry.sendTo ! VehicleSpawnPad.StartPlayerSeatedInVehicle(entry.vehicle, pad)
        entry.vehicle.Actor.tell(Mountable.TryMount(driver, 0), entry.sendTo) //entry.sendTo should handle replies to TryMount
        context.system.scheduler.scheduleOnce(1500 milliseconds, self, VehicleSpawnControlSeatDriver.AwaitDriverInSeat(entry))
      }
      else {
        trace("driver lost; vehicle stranded on pad")
        context.system.scheduler.scheduleOnce(1000 milliseconds, vehicleOverride, VehicleSpawnControl.Process.ServerVehicleOverride(entry))
      }

    case VehicleSpawnControlSeatDriver.AwaitDriverInSeat(entry) =>
      val driver = entry.driver
      if(entry.sendTo == ActorRef.noSender || !driver.isAlive || driver.Continent != Continent.Id) {
        trace("driver lost, but operations can continue")
        vehicleOverride ! VehicleSpawnControl.Process.ServerVehicleOverride(entry)
      }
      else if(entry.vehicle.Health == 0 || entry.vehicle.Position == Vector3.Zero) {
        //skip ahead for cleanup
        vehicleOverride ! VehicleSpawnControl.Process.ServerVehicleOverride(entry)
      }
      else if(driver.isAlive && driver.VehicleSeated.isEmpty) {
        if(pad.Railed) {
          Continent.VehicleEvents ! VehicleSpawnPad.DetachFromRails(entry.vehicle, pad, Continent.Id)
        }
        context.system.scheduler.scheduleOnce(100 milliseconds, self, VehicleSpawnControlSeatDriver.AwaitDriverInSeat(entry))
      }
      else {
        trace(s"driver is sitting down")
        val time = if(pad.Railed) 1000 else VehicleSpawnControlSeatDriver.RaillessSeatAnimationTimes(entry.vehicle.Definition.Name)
        context.system.scheduler.scheduleOnce(time milliseconds, self, VehicleSpawnControlSeatDriver.DriverInSeat(entry))
      }

    case VehicleSpawnControlSeatDriver.DriverInSeat(entry) =>
      if(entry.sendTo != ActorRef.noSender || entry.driver.Continent != Continent.Id) {
        trace(s"driver ${entry.driver.Name} has taken the wheel")
        entry.sendTo ! VehicleSpawnPad.PlayerSeatedInVehicle(entry.vehicle, pad)
      }
      else {
        trace("driver lost, but operations can continue")
      }
      context.system.scheduler.scheduleOnce(250 milliseconds, vehicleOverride, VehicleSpawnControl.Process.ServerVehicleOverride(entry))

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

object VehicleSpawnControlSeatDriver {
  final case class AwaitVehicleReadiness(entry : VehicleSpawnControl.Order)

  final case class BeginDriverInSeat(entry : VehicleSpawnControl.Order)

  final case class AwaitDriverInSeat(entry : VehicleSpawnControl.Order)

  final case class DriverInSeat(entry : VehicleSpawnControl.Order)

  /**
    * If the spawn pad associated with this `Actor` chain is not `Railed` -
    * not guaranteed to have the correct ingame globally unique id of the spawn pad -
    * then the animation of the driver boarding their vehicle will be displayed.
    * Although the network is finicky, these times should compensate a beneficial visual delay.
    * The BFRs, the Switchblade, and the Flail are all untested.
    */
  private val RaillessSeatAnimationTimes : Map[String, Int] = Map(
    "fury" -> 600,
    "quadassault" -> 600,
    "quadstealth" -> 600,
    "two_man_assault_buggy" -> 1000,
    "skyguard" -> 1300,
    "threemanheavybuggy" -> 1000,
    "twomanheavybuggy" -> 1800,
    "twomanhoverbuggy" -> 1800,
    "mediumtransport" -> 1300,
    "battlewagon" -> 1300,
    "thunderer" -> 1300,
    "aurora" -> 1300,
    "apc_tr" -> 2300,
    "apc_nc" -> 2300,
    "apc_vs" -> 2300,
    "prowler" -> 1000,
    "vanguard" -> 2000,
    "magrider" -> 1800,
    "ant" -> 2500,
    "ams" -> 1000,
    "router" -> 2500,
    "mosquito" -> 2000,
    "lightgunship" -> 2000,
    "wasp" -> 2000,
    "liberator" -> 1800,
    "vulture" -> 1800,
    "dropship" -> 2000,
    "galaxy_gunship" -> 2000,
    "lodestar" -> 2000,
    "phantasm" -> 1800
  ).withDefaultValue(1000)
}
