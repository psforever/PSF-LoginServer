// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.types.Vector3

/**
  * Instructions to be processed by `VehicleSpawnControlGuided`.
  * These instructions coordinate basic vehicle manipulations such as driving, turning, and stopping.
  * If defined, they will operate on a newly formed vehicle after it is released from its spawn pad lifting platform
  * and after it has been issued at least one `ServerVehicleOverrideMsg` packet.
  */
object AutoDriveControls {

  /**
    * A container that translates to a new `Setting` instruction.
    * Instructions are maintained in this form until they will be used due to the nature of the `Setting` object.
    * The least that this object needs to do is accept parameters that matches the specific `Setting` that it outputs.
    */
  sealed trait Configuration {
    def Create : Setting
  }

  /**
    * An instruction to be consumed by the cyclic operation of `VehicleSpawnControlGuided`
    * and are created by a `Configuration` object.
    * They are considered semi-immutable `case class` objects.
    * Externally, they are immutable by proper Scala standards.
    * Internally, they will be permitted `private` fields that can be modified the first time the object is used.
    */
  sealed trait Setting {
    /**
      * The nature of the action being performed.
      * @return an enumerated value that explains the purpose of the action
      */
    def Type : State.Value
    /**
      * The delay in between checks to determine if this setting has accomplished its goal or has entered an invalid state.
      * @return the length of the delay
      */
    def Delay : Long = 200L
    /**
      * Data that is important for fulfilling the instruction on a user's client.
      * Highly specific to the implementation.
      * @return any data deemed important; `None`, if unnecessary
      */
    def Data : Option[Any] = None
    /**
      * Perform a test to determine if the vehicle is capable of performing the action this instruction requires.
      * The test is typically simplistic in nature and often boils down to whether o not the vehicle is mobile.
      * @param vehicle the vehicle being controlled
      * @return `true`, if the action can (probably) be accomplished under the current conditions; `false`, otherwise
      */
    def Validate(vehicle : Vehicle) : Boolean = Vector3.MagnitudeSquared(vehicle.Velocity.getOrElse(Vector3.Zero).xy) > 0
    /**
      * Perform a test to determine if the vehicle has reached a set of conditions
      * where the action performed by the instruction has been fulfilled.
      * This should count as the "end of this step" and the "beginning of the next step."
      * @param vehicle the vehicle being controlled
      * @return `true`, if the action has run to completion; `false`, otherwise
      */
    def CompletionTest(vehicle : Vehicle) : Boolean
  }

  /**
    * The nature of the action being performed.
    * Different actions can be divided into types.
    */
  object State extends Enumeration {
    val
    Cancel,
    Climb,
    Drive,
    Stop,
    Turn,
    Wait
    = Value
  }

  protected final case class AutoDrive(speed : Int) extends Setting {
    def Type = State.Drive

    override def Data = Some(speed)

    override def Validate(vehicle : Vehicle) : Boolean = true

    def CompletionTest(vehicle : Vehicle) = Vector3.MagnitudeSquared(vehicle.Velocity.getOrElse(Vector3.Zero).xy) > 0
  }

  protected final case class AutoDriveDistance(start : Vector3, sqDistance : Float) extends Setting {
    def Type = State.Wait

    def CompletionTest(vehicle : Vehicle) : Boolean = {
      Vector3.DistanceSquared(vehicle.Position.xy, start) > sqDistance
    }
  }

  protected final case class AutoDriveDistanceFromHere(sqDistance : Float) extends Setting {
    private var start : Option[Vector3] = None

    def Type = State.Wait

    def CompletionTest(vehicle : Vehicle) : Boolean = {
      val startLoc = start.getOrElse({
        start = Some(vehicle.Position.xy)
        start.get
      })
      Vector3.DistanceSquared(vehicle.Position.xy, startLoc) > sqDistance
    }
  }

  protected final case class AutoDriveForTime(length : Long) extends Setting {
    private var start : Option[Long] = None

    def Type = State.Wait

    def CompletionTest(vehicle : Vehicle) : Boolean = {
      val time : Long = System.currentTimeMillis
      val startTime = start.getOrElse({
        start = Some(time)
        time
      })
      time - startTime >= length
    }

    override def Validate(vehicle : Vehicle) : Boolean = true
  }

  protected final case class AutoDriveTurnBy(angle : Float, direction : Int) extends Setting {
    private var end : Option[Float] = None
    private var currAng : Float = 0f

    def Type = State.Turn

    override def Delay : Long = 100L //increased frequency

    override def Data = Some(direction)

    def CompletionTest(vehicle : Vehicle) : Boolean = {
      val endAng = end.getOrElse {
        currAng = vehicle.Orientation.z
        var ang = (currAng + angle) % 360f
        if(ang < 0f) {
          ang += 360f
        }
        end = Some(ang)
        ang
      }
      val lastAng = currAng
      currAng = vehicle.Orientation.z
      //check that the expected angle is sandwiched between the previous angle and the current angle
      currAng == endAng || (lastAng < endAng && endAng <= currAng) || (lastAng > endAng && endAng >= currAng)
    }

    override def Validate(vehicle : Vehicle) : Boolean = direction != 15 && super.Validate(vehicle)
  }

  protected final case class AutoDriveFirstGear() extends Setting {
    private var speed : Int = 0

    def Type = State.Drive

    override def Data = Some(speed)

    def CompletionTest(vehicle : Vehicle) = Vector3.MagnitudeSquared(vehicle.Velocity.getOrElse(Vector3.Zero)) > 0

    override def Validate(vehicle : Vehicle) : Boolean = {
      speed = vehicle.Definition.AutoPilotSpeed1
      true
    }
  }

  protected final case class AutoDriveSecondGear() extends Setting {
    private var speed : Int = 0

    def Type = State.Drive

    override def Data = Some(speed)

    def CompletionTest(vehicle : Vehicle) = Vector3.MagnitudeSquared(vehicle.Velocity.getOrElse(Vector3.Zero)) > 0

    override def Validate(vehicle : Vehicle) : Boolean = {
      speed = vehicle.Definition.AutoPilotSpeed2
      true
    }
  }

  protected final case class AutoDriveClimb(altitude : Float) extends Setting {
    def Type = State.Climb

    override def Data = Some(altitude)

    def CompletionTest(vehicle : Vehicle) = {
      vehicle.Position.z >= altitude
    }

    override def Validate(vehicle : Vehicle) : Boolean = GlobalDefinitions.isFlightVehicle(vehicle.Definition)
  }

  protected final case class AutoDriveCancelEarly(test : (Vehicle) => Boolean) extends Setting {
    def Type = State.Cancel

    def CompletionTest(vehicle : Vehicle) = true

    override def Validate(vehicle : Vehicle) : Boolean = test(vehicle)
  }

  protected final case class AutoDriveStop() extends Setting {
    def Type = State.Stop

    override def Validate(vehicle : Vehicle) : Boolean = true

    def CompletionTest(vehicle : Vehicle) = Validate(vehicle)
  }

  /**
    * Use a validation test to determine if the remainder of the instructions should be processed.
    * @param test the custom valid conditions of the vehicle for continuing
    */
  final case class CancelEarly(test : (Vehicle)=>Boolean) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveCancelEarly(test)
  }

  /**
    * Gain altitude with a flying vehicle.
    * The climb speed is fixed.
    * @param altitude the vertical distance to ascend
    */
  final case class Climb(altitude : Float) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveClimb(altitude)
  }

  /**
    * Drive a certain distance from somewhere.
    * @param start the fixed coordinates of the origin point
    * @param distance how far from the origin point the vehicle should travel
    */
  final case class Distance(start : Vector3, distance : Float) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveDistance(start, distance * distance)
  }

  /**
    * Drive a certain distance from where the vehicle is at the time that the instruction is called.
    * The starting position is the current position of the vehicle.
    * @param distance how far from the origin point the vehicle should travel
    */
  final case class DistanceFromHere(distance : Float) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveDistanceFromHere(distance * distance)
  }

  /**
    * Basic drive forward instruction.
    * @see `ServerVehicleOverrideMsg.forward_speed`
    * @param speed the speed that the vehicle accelerates to;
    *              scaled in a curious way
    */
  final case class Drive(speed : Int) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDrive(speed)
  }

  /**
    * Special drive forward instruction.
    * @see `ServerVehicleOverrideMsg.forward_speed`
    * @see `VehicleDefinition.AutoPilotSpeed1`
    */
  final case class FirstGear() extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveFirstGear()
  }

  /**
    * Drive or idle for a certain duration.
    * The starting position is the current position of the vehicle.
    * @param time how long to contiue driving under the current conditions
    */
  final case class ForTime(time : Long) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveForTime(time)
  }

  /**
    * Special drive forward instruction.
    * @see `ServerVehicleOverrideMsg.forward_speed`
    * @see `VehicleDefinition.AutoPilotSpeed2`
    */
  final case class SecondGear() extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveSecondGear()
  }

  /**
    * Stop driving (but do not cancel the server override state).
    */
  final case class Stop() extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveStop()
  }

  /**
    * Cause the vehicle to turn a certain amount.
    * @see `VehicleMessage.wheel_direction`
    * @param angle the angle by which to turn the vehicle
    * @param direction the wheel direction of the vehicle
    */
  final case class TurnBy(angle : Float, direction : Int) extends Configuration {
    def Create : Setting = AutoDriveControls.AutoDriveTurnBy(angle, direction)
  }
}
