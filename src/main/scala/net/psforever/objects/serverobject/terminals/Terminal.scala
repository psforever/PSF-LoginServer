// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.avatar.Certification
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.{ItemTransactionMessage, TriggeredSound}
import net.psforever.types.{PlanetSideGUID, Vector3}

/**
  * A server object that can be accessed for net.psforever.services and other amenities.
  * Terminals are owned by both `Structure` objects and by `Vehicle` objects
  * and generally conform to the faction affiliation of the owner.
  * Some `Structure`-owned terminals may be compromised
  * to extend functionality to other's not of faction affiliation for a short time
  * while `Vehicle`-owned terminals may not.
  *
  * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Terminal(tdef: TerminalDefinition) extends Amenity with Hackable {
  HackSound = TriggeredSound.HackTerminal
  HackEffectDuration = Array(0, 30, 60, 90)
  HackDuration = Array(0, 10, 5, 3)

  //the following fields and related methods are neither finalized nor integrated; GOTO Request

  def Damaged(dam: Int): Unit = {
    Health = Math.max(0, Health - dam)
  }

  def Repair(rep: Int): Unit = {
    Health = Math.min(Health + rep, 100)
  }

  /**
    * Process a message (a "request") dispatched by the user.
    * To be accessible, the terminal must be:
    *   owned by the same faction by the user
    *   or must be compromised
    *   or must be a zone owned object (GUID == 0, e.g. non-facility buildings in caves)
    * @see `FactionAffinity`
    * @see `PlanetSideEmpire`
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains what resulted from interacting with this `Terminal`
    */
  def Request(player: Player, msg: Any): Terminal.Exchange = {
    if (Faction == player.Faction || HackedBy.isDefined || Owner.GUID == PlanetSideGUID(0)) {
      tdef.Request(player, msg)
    } else {
      Terminal.NoDeal()
    }
  }

  def Definition: TerminalDefinition = tdef
}

object Terminal {

  /**
    * Entry message into this `Terminal` that carries the request.
    * Accessing an option in a `Terminal` normally always results in this message.
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    */
  final case class Request(player: Player, msg: ItemTransactionMessage)

  /**
    * A basic `Trait` connecting all of the actionable `Terminal` response messages.
    */
  sealed trait Exchange

  /**
    * Message that carries the result of the processed request message back to the original user (`player`).
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    * @param response the result of the processed request
    */
  final case class TerminalMessage(player: Player, msg: ItemTransactionMessage, response: Exchange)

  /**
    * No action will result from interacting with this `Terminal`.
    * A result of a processed request.
    */
  final case class NoDeal() extends Exchange

  import net.psforever.types.ExoSuitType

  /**
    * The `Player` exo-suit will be changed to the prescribed one.
    * The subtype will be important if the user is swapping to an `ExoSuitType.MAX` exo-suit.
    * A result of a processed request.
    * @param exosuit the type of exo-suit
    * @param subtype the exo-suit subtype, if any
    */
  final case class BuyExosuit(exosuit: ExoSuitType.Value, subtype: Int = 0) extends Exchange

  import net.psforever.objects.equipment.Equipment

  /**
    * A single piece of `Equipment` has been selected and will be given to the `Player`.
    * The `Player` must decide what to do with it once it is in their control.
    * A result of a processed request.
    * @param item the `Equipment` being given to the player
    */
  final case class BuyEquipment(item: Equipment) extends Exchange

  /**
    * A roundabout message oft-times.
    * Most `Terminals` should always allow `Player`s to dispose of some piece of `Equipment`.
    * A result of a processed request.
    */
  final case class SellEquipment() extends Exchange //TODO if there are exceptions, find them

  /**
    * Provide the certification type unlocked by the player.
    *
    * @param cert the certification unlocked
    */
  final case class LearnCertification(cert: Certification) extends Exchange

  /**
    * Provide the certification type freed-up by the player.
    *
    * @param cert the certification returned
    */
  final case class SellCertification(cert: Certification) extends Exchange

  import net.psforever.objects.definition.ImplantDefinition

  /**
    * Provide the implant type unlocked by the player.
    * @param implant the implant (definition) requested
    */
  final case class LearnImplant(implant: ImplantDefinition) extends Exchange

  /**
    * Provide the implant type freed-up by the player.
    * @param implant the implant (definition) returned
    */
  final case class SellImplant(implant: ImplantDefinition) extends Exchange

  import net.psforever.objects.Vehicle
  import net.psforever.objects.inventory.InventoryItem

  /**
    * Provide a vehicle that was constructed for the player.
    * @param vehicle the vehicle
    * @param weapons the vehicle's mounted armament
    * @param inventory the vehicle's trunk contents
    */
  final case class BuyVehicle(vehicle: Vehicle, weapons: List[InventoryItem], inventory: List[InventoryItem])
      extends Exchange

  /**
    * Recover a former exo-suit and `Equipment` configuration that the `Player` possessed.
    * A result of a processed request.
    * @param exosuit the type of exo-suit
    * @param subtype the exo-suit subtype, if any
    * @param holsters the contents of the `Player`'s holsters
    * @param inventory the contents of the `Player`'s inventory
    */
  final case class InfantryLoadout(
      exosuit: ExoSuitType.Value,
      subtype: Int = 0,
      holsters: List[InventoryItem],
      inventory: List[InventoryItem]
  ) extends Exchange

  final case class VehicleLoadout(
      vehicle_definition: VehicleDefinition,
      weapons: List[InventoryItem],
      inventory: List[InventoryItem]
  ) extends Exchange

  /**
    * Start the special effects caused by a proximity-base service.
    * @param terminal the proximity-based unit
    */
  final case class StartProximityEffect(terminal: Terminal with ProximityUnit) extends Exchange

  /**
    * Stop the special effects caused by a proximity-base service.
    * @param terminal the proximity-based unit
    */
  final case class StopProximityEffect(terminal: Terminal with ProximityUnit) extends Exchange

  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef: TerminalDefinition): Terminal = {
    new Terminal(tdef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate and configure a `Terminal` object
    *
    * @param pos     The location of the object
    * @param tdef    the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param id      the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Terminal` object
    */
  def Constructor(pos: Vector3, tdef: TerminalDefinition)(id: Int, context: ActorContext): Terminal = {
    import akka.actor.Props

    val obj = Terminal(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
