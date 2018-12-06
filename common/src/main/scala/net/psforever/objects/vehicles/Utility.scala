// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.ActorContext
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects._
import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.tube.{SpawnTube, SpawnTubeDefinition}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types.Vector3

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
  ams_respawn_tube,
  bfr_rearm_terminal,
  lodestar_repair_terminal,
  matrix_terminalc,
  multivehicle_rearm_terminal,
  order_terminala,
  order_terminalb,
  teleportpad_terminal,
  internal_router_telepad_deployable
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

  /**
    * Recover the original value used to initialize this object.
    * @return the type of the `Amenity` object that was created
    */
  def UtilType : UtilityType.Value = util
}

object Utility {
  type UtilLogic = (Amenity, ActorContext)=>Unit

  /**
    * Embedded (owned) entities are known in relation to their parent entity.
    * These overrides to the `Position` method and the `Orientation` method reflect this.
    */
  sealed trait UtilityWorldEntity {
    this : Amenity =>

    override def Position : Vector3 = {
      val oPos = Owner.Position
      (Owner, LocationOffset) match {
        case (_, Vector3.Zero) =>
          oPos
        case (_ : Vehicle, v) =>
          oPos + v.Rz(Orientation.z + 90)
        case _ =>
          oPos
      }
    }

    override def Orientation : Vector3 = Owner.Orientation
  }

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
    case UtilityType.ams_respawn_tube =>
      new SpawnTubeUtility(GlobalDefinitions.ams_respawn_tube)
    case UtilityType.bfr_rearm_terminal =>
      new TerminalUtility(GlobalDefinitions.bfr_rearm_terminal)
    case UtilityType.lodestar_repair_terminal =>
      new ProximityTerminalUtility(GlobalDefinitions.lodestar_repair_terminal)
    case UtilityType.matrix_terminalc =>
      new TerminalUtility(GlobalDefinitions.matrix_terminalc)
    case UtilityType.multivehicle_rearm_terminal =>
      new TerminalUtility(GlobalDefinitions.multivehicle_rearm_terminal)
    case UtilityType.order_terminala =>
      new TerminalUtility(GlobalDefinitions.order_terminala)
    case UtilityType.order_terminalb =>
      new TerminalUtility(GlobalDefinitions.order_terminalb)
    case UtilityType.teleportpad_terminal =>
      new TeleportPadTerminalUtility(GlobalDefinitions.teleportpad_terminal)
    case UtilityType.internal_router_telepad_deployable =>
      new InternalTelepad(GlobalDefinitions.internal_router_telepad_deployable)
  }

  /**
    * Override for `SpawnTube` objects so that they inherit the spatial characteristics of their `Owner`.
    * @param tubeDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  class SpawnTubeUtility(tubeDef : SpawnTubeDefinition) extends SpawnTube(tubeDef) with UtilityWorldEntity

  /**
    * Override for a `Terminal` object so that it inherits the spatial characteristics of its `Owner`.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  class TerminalUtility(tdef : TerminalDefinition) extends Terminal(tdef) with UtilityWorldEntity

  /**
    * Override for a `Terminal` object so that it inherits the spatial characteristics of its `Owner`.
    * The `Terminal` `Utility` produced has proximity effects.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  class ProximityTerminalUtility(tdef : TerminalDefinition) extends Terminal(tdef)
    with UtilityWorldEntity
    with ProximityUnit

  /**
    * Override for a `Terminal` object so that it inherits the spatial characteristics of its `Owner`.
    * The `Terminal` `Utility` produced dispenses a specific item
    * that retain knowledge of the `Owner` of the `Terminal` that dispensed it.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  class TeleportPadTerminalUtility(tdef : TerminalDefinition) extends TerminalUtility(tdef) {
    /**
      * na
      * @param player na
      * @param msg na
      * @return na
      */
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      val reply = super.Buy(player, msg)
      reply match {
        case Terminal.BuyEquipment(obj : Telepad) =>
          obj.Router = Owner.GUID
        case _ => ;
      }
      reply
    }
  }

  /**
    * The internal telepad is a component that is contained by the Router when it deploys
    * and allows it to serve as one of the terminal points of a Router-telepad teleportation system.
    * @param ddef na
    */
  class InternalTelepad(ddef : DeployableDefinition) extends Amenity
    with TelepadLike {
    /** a link to the telepad that serves as the other endpoint of this teleportation system */
    private var activeTelepad : Option[PlanetSideGUID] = None

    def Telepad : Option[PlanetSideGUID] = activeTelepad

    def Telepad_=(rguid : PlanetSideGUID) : Option[PlanetSideGUID] = Telepad_=(Some(rguid))

    def Telepad_=(rguid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
      activeTelepad = rguid
      Telepad
    }

    override def Position = Owner.Position
    override def Orientation = Owner.Orientation
    /** the router is the owner */
    override def Router : Option[PlanetSideGUID] = Some(Owner.GUID)

    def Definition = ddef
  }

  /**
    * Provide the called-out object's logic.
    * @param util the type of the `Amenity` object
    * @return the `Amenity` object
    */
  private def SelectUtilitySetupFunc(util : UtilityType.Value) : UtilLogic = util match {
    case UtilityType.ams_respawn_tube =>
      SpawnTubeDefinition.Setup
    case UtilityType.bfr_rearm_terminal =>
      _OrderTerminalDefinition.Setup
    case UtilityType.lodestar_repair_terminal =>
      ProximityTerminal.Setup
    case UtilityType.matrix_terminalc =>
      MatrixTerminalDefinition.Setup
    case UtilityType.multivehicle_rearm_terminal =>
      _OrderTerminalDefinition.Setup
    case UtilityType.order_terminala =>
      OrderTerminalABDefinition.Setup
    case UtilityType.order_terminalb =>
      OrderTerminalABDefinition.Setup
    case UtilityType.teleportpad_terminal =>
      TeleportPadTerminalDefinition.Setup
    case UtilityType.internal_router_telepad_deployable =>
      TelepadLike.Setup
  }

  //private def defaultSetup(o1 : Amenity, o2 : ActorContext) : Unit = { }
}
