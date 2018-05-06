// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * After the vehicle has been released from the spawn pad lifting platform,
  * it enters into an auto-drive mode that has at least two stages.
  * An undefined number of stages cane be included, however.
  * This can lead the newly-spawned vehicle through a rough pre-defined path.<br>
  * <br>
  * Throughout this process, the conditions of `ServerVehicleOverrideMsg` are still in effect.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlGuided(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-guide"

  val driverControl = context.actorOf(Props(classOf[VehicleSpawnControlDriverControl], pad), s"${context.parent.path.name}-driver")

  def receive : Receive = {
    case VehicleSpawnControl.Process.StartGuided(entry) =>
      pad.Guide match {
        case Nil =>
          trace("no guided path for this pad")
          driverControl ! VehicleSpawnControl.Process.DriverVehicleControl(entry)
        case path =>
          self ! VehicleSpawnControlGuided.InitialGuided(entry, path.map { _.Create })
      }

    case VehicleSpawnControlGuided.SelectNextGuided(entry, actions) =>
      actions match {
        case Nil | _ :: Nil =>
          trace("custom vehicle path completed")
          driverControl ! VehicleSpawnControl.Process.DriverVehicleControl(entry)
        case _ :: xs =>
          self ! VehicleSpawnControlGuided.InitialGuided(entry, xs)
      }

    case VehicleSpawnControlGuided.InitialGuided(entry, actions) =>
      val vehicle = entry.vehicle
      if(entry.sendTo != ActorRef.noSender && vehicle.Health != 0 && entry.driver.VehicleSeated.contains(vehicle.GUID) && actions.head.Validate(vehicle)) {
        trace(s"custom vehicle path plotted - ${actions.head.Type}")
        entry.sendTo ! VehicleSpawnControlGuided.GuidedControl(actions.head.Type, vehicle, actions.head.Data)
        self ! VehicleSpawnControlGuided.ContinueGuided(entry, actions)
      }
      else {
        trace(s"projected ${vehicle.Definition.Name} path interruption; exit guided mode")
        driverControl ! VehicleSpawnControl.Process.DriverVehicleControl(entry)
      }

    case VehicleSpawnControlGuided.ValidateGuided(entry, actions) =>
      val vehicle = entry.vehicle
      if(entry.sendTo != ActorRef.noSender && vehicle.Health != 0 && entry.driver.VehicleSeated.contains(vehicle.GUID) && actions.head.Validate(vehicle)) {
        self ! VehicleSpawnControlGuided.ContinueGuided(entry, actions)
      }
      else {
        trace(s"plotted ${vehicle.Definition.Name} path interruption; exit guided mode")
        driverControl ! VehicleSpawnControl.Process.DriverVehicleControl(entry)
      }

    case VehicleSpawnControlGuided.ContinueGuided(entry, actions) =>
      if(actions.head.CompletionTest(entry.vehicle)) {
        trace("step completed")
        self ! VehicleSpawnControlGuided.SelectNextGuided(entry, actions)
      }
      else {
        context.system.scheduler.scheduleOnce(actions.head.Delay milliseconds, self, VehicleSpawnControlGuided.ValidateGuided(entry, actions))
      }

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case msg @ VehicleSpawnControl.Process.FinalClearance(_) =>
      driverControl ! msg

    case _ => ;
  }
}

object VehicleSpawnControlGuided {
  /**
    * Select the first instruction from the list.
    * @param entry the vehicle order
    * @param actions the list of instructions related to this spawn pad
    */
  private final case class InitialGuided(entry : VehicleSpawnControl.Order, actions : List[AutoDriveControls.Setting])
  /**
    * Swap to the next instruction, if it exists.
    * @param entry the vehicle order
    * @param actions the list of instructions related to this spawn pad
    */
  private final case class SelectNextGuided(entry : VehicleSpawnControl.Order, actions : List[AutoDriveControls.Setting])
  /**
    * The validation test determines whether the vehicle, the driver, and any other important elements
    * are still in a state where the current instruction can be accomplished.
    * If the validation test passes, the current instruction can continue to run to completion.
    * If the validation test fails, the remainder of the instructions are aborted.
    * @param entry the vehicle order
    * @param actions the list of instructions related to this spawn pad
    */
  private final case class ValidateGuided(entry : VehicleSpawnControl.Order, actions : List[AutoDriveControls.Setting])
  /**
    * If the previous validation test passes, the current instruction can continue to run to completion.
    * Once completed, the next instruction can be selected.
    * @param entry the vehicle order
    * @param actions the list of instructions related to this spawn pad
    */
  private final case class ContinueGuided(entry : VehicleSpawnControl.Order, actions : List[AutoDriveControls.Setting])

  /**
    * A message that explains the current instruction in the guided mode to another agency.
    * @param command the nature of the action being performed
    * @param vehicle the vehicle being controlled
    * @param data optional data used to process the instruction
    */
  final case class GuidedControl(command : AutoDriveControls.State.Value, vehicle : Vehicle, data : Option[Any])
}
