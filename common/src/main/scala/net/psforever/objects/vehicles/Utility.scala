// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.ActorContext
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.terminals.{OrderTerminalABDefinition, Terminal}

/**
  * An `Enumeration` of the available vehicular utilities.<br>
  * <br>
  * These values are used to connect `Amenity` objects and their extra logic encapsulated in this class
  * with information in the `VehicleDefinition` object for that kind of vehicle.
  * @see `Vehicle.LoadDefinition`
  * @see `VehicleDefinition.Utilities`
  */
object UtilityType extends Enumeration {
  type Type = Value
  val
  order_terminala,
  order_terminalb
  = Value
}

/**
  * Build a specific functional extension that is a component of a certain `Vehicle` object.<br>
  * <br>
  * A `Utility` object is a variation of an `Amenity` object that might be found in a `Building` object.
  * The object itself is stored inside the `Utility` as if it were a container.
  * `Amenity` objects are required because they are to be owned by the `vehicle` for purposes of faction affinity.
  * Only specific kinds of objects count for being `Utility` contents/objects.
  * Additional "setup" logic can be supplied that will be called when the owner vehicle's control `Actor` is created.
  * Ostensibly, the purpose of the additional logic, when it is called,
  * is to initialize a control `Actor` for the contained object.
  * This `Actor` is expected by other logic.
  * @see `Vehicle.LoadDefinition`
  * @see `VehicleDefinition.Utilities`
  * @param util the type of the `Amenity` object to be created
  * @param vehicle the owner of this object
  * @see `Amenity.Owner`
  */
class Utility(util : UtilityType.Value, vehicle : Vehicle) {
  private val obj : Amenity = Utility.BuildUtilityFunc(util)
  obj.Owner = vehicle
  private val setupFunc : Utility.UtilLogic = Utility.SelectUtilitySetupFunc(util)

  /**
    * Access the contained object in this `Utility`.
    * @return the contained `Amenity` object
    */
  def apply() : Amenity = obj

  /**
    * Run the setup code that was provided in the object constructor parameters.
    * While it is expected to construct an `Actor`, that is not required.
    * @param context an `ActorContext` potentially useful for the function
    */
  def Setup(implicit context : ActorContext) : Unit = setupFunc(obj, context)
}

object Utility {
  type UtilLogic = (Amenity, ActorContext)=>Unit

  /**
    * Overloaded constructor.
    * @param util the type of the `Amenity` object to be created
    * @param vehicle the owner of this object
    * @return a `Utility` object
    */
  def apply(util : UtilityType.Value, vehicle : Vehicle) : Utility = {
    new Utility(util, vehicle)
  }

  /**
    * Create the called-out object.
    * @param util the type of the `Amenity` object
    * @return the `Amenity` object
    */
  private def BuildUtilityFunc(util : UtilityType.Value) : Amenity = util match {
    case UtilityType.order_terminala =>
      Terminal(GlobalDefinitions.order_terminala)
    case UtilityType.order_terminalb =>
      Terminal(GlobalDefinitions.order_terminalb)
  }

  /**
    * Provide the called-out object's logic.
    * @param util the type of the `Amenity` object
    * @return the `Amenity` object
    */
  private def SelectUtilitySetupFunc(util : UtilityType.Value) : UtilLogic = util match {
    case UtilityType.order_terminala =>
      OrderTerminalABDefinition.Setup
    case UtilityType.order_terminalb =>
      OrderTerminalABDefinition.Setup
  }
}
