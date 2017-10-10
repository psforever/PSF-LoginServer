// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.{ExoSuitType, PlanetSideEmpire, TransactionType, Vector3}

/**
  * na
  * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Terminal(tdef : TerminalDefinition) extends PlanetSideServerObject {
  //the following fields and related methods are neither finalized no integrated; GOTO Request
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var hackedBy : Option[(Player, Vector3)] = None
  private var health : Int = 100 //TODO not real health value

  def Faction : PlanetSideEmpire.Value = faction

  def HackedBy : Option[(Player, Vector3)] = hackedBy

  def HackedBy_=(agent : Player) : Option[(Player, Vector3)] = HackedBy_=(Some(agent))

  def HackedBy_=(agent : Option[Player]) : Option[(Player, Vector3)] = {
    hackedBy match {
      case None =>
        if(agent.isDefined) {
          hackedBy = Some(agent.get, agent.get.Position)
        }
      case Some(_) =>
        if(agent.isEmpty) {
          hackedBy = None
        }
        else if(agent.get.Faction != hackedBy.get._1.Faction) {
          hackedBy = Some(agent.get, agent.get.Position) //overwrite
        }
    }
    HackedBy
  }

  def Health : Int = health

  def Convert(toFaction : PlanetSideEmpire.Value) : Unit = {
    hackedBy = None
    faction = toFaction
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
      case TransactionType.Buy | TransactionType.Learn =>
        tdef.Buy(player, msg)

      case TransactionType.Sell =>
        tdef.Sell(player, msg)

      case TransactionType.InfantryLoadout =>
        tdef.Loadout(player, msg)

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

  import net.psforever.types.CertificationType
  final case class LearnCertification(cert : CertificationType.Value, cost : Int) extends Exchange

  final case class SellCertification(cert : CertificationType.Value, cost : Int) extends Exchange

  /**
    * Recover a former exo-suit and `Equipment` configuration that the `Player` possessed.
    * A result of a processed request.
    * @param exosuit the type of exo-suit
    * @param subtype the exo-suit subtype, if any
    * @param holsters the contents of the `Player`'s holsters
    * @param inventory the contents of the `Player`'s inventory
    */
  final case class InfantryLoadout(exosuit : ExoSuitType.Value, subtype : Int = 0, holsters : List[InventoryItem], inventory : List[InventoryItem]) extends Exchange

  def apply(tdef : TerminalDefinition) : Terminal = {
    new Terminal(tdef)
  }

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID, tdef : TerminalDefinition) : Terminal = {
    val obj = new Terminal(tdef)
    obj.GUID = guid
    obj
  }
}
