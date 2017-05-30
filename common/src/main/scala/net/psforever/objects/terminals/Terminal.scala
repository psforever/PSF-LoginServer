// Copyright (c) 2017 PSForever
package net.psforever.objects.terminals

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.{ExoSuitType, PlanetSideEmpire, TransactionType}

/**
  * na
  * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Terminal(tdef : TerminalDefinition) extends PlanetSideGameObject {
  /** Internal reference to the `Actor` for this `Terminal`, sets up by this `Terminal`. */
  private var actor = ActorRef.noSender

  /**
    * Get access to the internal `TerminalControl` `Actor` for this `Terminal`.
    * If called for the first time, create the said `Actor`.
    * Must be called only after the globally unique identifier has been set.
    * @param context the `ActorContext` under which this `Terminal`'s `Actor` will be created
    * @return the `Terminal`'s `Actor`
    */
  def Actor(implicit context : ActorContext) : ActorRef =  {
    if(actor == ActorRef.noSender) {
      actor = context.actorOf(Props(classOf[TerminalControl], this), s"${tdef.Name}_${GUID.guid}")
    }
    actor
  }

  //the following fields and related methods are neither finalized no integrated; GOTO Request
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var hackedBy : Option[PlanetSideEmpire.Value] = None
  private var health : Int = 100 //TODO not real health value

  def Faction : PlanetSideEmpire.Value = faction

  def HackedBy : Option[PlanetSideEmpire.Value] = hackedBy

  def Health : Int = health

  def Convert(toFaction : PlanetSideEmpire.Value) : Unit = {
    hackedBy = None
    faction = toFaction
  }

  def HackedBy(toFaction : Option[PlanetSideEmpire.Value]) : Unit = {
    hackedBy = if(toFaction.contains(faction)) { None } else { toFaction }
  }

  def Damaged(dam : Int) : Unit = {
    health = Math.max(0, Health - dam)
  }

  def Repair(rep : Int) : Unit = {
    health = Math.min(Health + rep, 100)
  }

  /**
    * Process some `TransactionType` action requested by the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains what resulted from interacting with this `Terminal`
    */
  def Request(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    msg.transaction_type match {
      case TransactionType.Buy =>
        tdef.Buy(player, msg)

      case TransactionType.Sell =>
        tdef.Sell(player, msg)

      case TransactionType.InfantryLoadout =>
        tdef.InfantryLoadout(player, msg)

      case _ =>
        Terminal.NoDeal()
    }
  }

  def Definition : TerminalDefinition = tdef
}

object Terminal {
  /**
    * Entry message into this `Terminal` that carries the request.
    * Accessing an option in a `Terminal` normally always results in this message.
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    */
  final case class Request(player : Player, msg : ItemTransactionMessage)

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
  final case class TerminalMessage(player : Player, msg : ItemTransactionMessage, response : Exchange)

  /**
    * No action will result from interacting with this `Terminal`.
    * A result of a processed request.
    */
  final case class NoDeal() extends Exchange
  /**
    * The `Player` exo-suit will be changed to the prescribed one.
    * The subtype will be important if the user is swapping to an `ExoSuitType.MAX` exo-suit.
    * A result of a processed request.
    * @param exosuit the type of exo-suit
    * @param subtype the exo-suit subtype, if any
    */
  final case class BuyExosuit(exosuit : ExoSuitType.Value, subtype : Int = 0) extends Exchange
  /**
    * A single piece of `Equipment` has been selected and will be given to the `Player`.
    * The `Player` must decide what to do with it once it is in their control.
    * A result of a processed request.
    * @param item the `Equipment` being given to the player
    */
  final case class BuyEquipment(item : Equipment) extends Exchange
  /**
    * A roundabout message oft-times.
    * Most `Terminals` should always allow `Player`s to dispose of some piece of `Equipment`.
    * A result of a processed request.
    */
  //TODO if there are exceptions, find them
  final case class SellEquipment() extends Exchange
  /**
    * Recover a former exo-suit and `Equipment` configuration that the `Player` possessed.
    * A result of a processed request.
    * @param exosuit the type of exo-suit
    * @param subtype the exo-suit subtype, if any
    * @param holsters the contents of the `Player`'s holsters
    * @param inventory the contents of the `Player`'s inventory
    */
  final case class InfantryLoadout(exosuit : ExoSuitType.Value, subtype : Int = 0, holsters : List[InventoryItem], inventory : List[InventoryItem]) extends Exchange

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID, tdef : TerminalDefinition) : Terminal = {
    val obj = new Terminal(tdef)
    obj.GUID = guid
    obj
  }
}
