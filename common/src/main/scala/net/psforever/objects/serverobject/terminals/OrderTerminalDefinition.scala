// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.loadouts.{InfantryLoadout, VehicleLoadout}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition._
import net.psforever.types.{CertificationType, ExoSuitType}

import scala.collection.mutable

/**
  * The definition for any `Terminal` that is of a type "order_terminal".
  * This kind of "order_terminal" is applicable to facilities.<br>
  * <br>
  * `Buy` and `Sell` `Equipment` items and `AmmoBox` items.
  * Change `ExoSuitType` and retrieve `Loadout` entries.
  */
class OrderTerminalDefinition extends EquipmentTerminalDefinition(612) {
  Name = "order_terminal"

  /**
    * The `Equipment` available from this `Terminal` on specific pages.
    */
  private val buyFunc : (Player, ItemTransactionMessage)=>Terminal.Exchange = EquipmentTerminalDefinition.Buy(
    infantryAmmunition ++ infantryWeapons,
    supportAmmunition ++ supportWeapons,
    suits ++ maxSuits)

  override def Buy(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = buyFunc(player, msg)

  /**
    * Process a `TransactionType.Loadout` action by the user.
    * `Loadout` objects are blueprints composed of exo-suit specifications and simplified `Equipment`-to-slot mappings.
    * If a valid loadout is found, its data is transformed back into actual `Equipment` for return to the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout : InfantryLoadout) =>
          val holsters = loadout.visible_slots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          Terminal.InfantryLoadout(loadout.exosuit, loadout.subtype, holsters, inventory)
        case Some(_) | None =>
          Terminal.NoDeal()
      }
    }
    else {
      Terminal.NoDeal()
    }
  }
}

class _OrderTerminalDefinition(objId : Int) extends TerminalDefinition(objId) {
  private val pages : mutable.HashMap[Int, _OrderTerminalDefinition.PageDefinition] =
    new mutable.HashMap[Int, _OrderTerminalDefinition.PageDefinition]()
  private var sellEquipmentDefault : Boolean = true

  def Page : mutable.HashMap[Int, _OrderTerminalDefinition.PageDefinition] = pages

  def SellEquipmentByDefault : Boolean = sellEquipmentDefault

  def SellEquipmentByDefault_=(sell : Boolean) : Boolean = {
    sellEquipmentDefault = sell
    SellEquipmentByDefault
  }

  override def Buy(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    pages.get(msg.item_page) match {
      case Some(page) =>
        page.Buy(player, msg)
      case _ =>
        Terminal.NoDeal()
    }
  }

  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Buy(player, msg)

  override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(sellEquipmentDefault) {
      Terminal.SellEquipment()
    }
    else {
      pages.get(msg.item_page) match {
        case Some(page) =>
          page.Sell(player, msg)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }
}

object _OrderTerminalDefinition {
  abstract class PageDefinition(stock : Map[String, Any]) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange
    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange
  }

  final case class ArmorPage(stock : Map[String, (ExoSuitType.Value, Int)]) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some((suit : ExoSuitType.Value, subtype : Int)) =>
          Terminal.BuyExosuit(suit, subtype)
        case _ =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
  }

  final case class CertificationPage(stock : Map[String, CertificationType.Value]) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(cert : CertificationType.Value) =>
          Terminal.LearnCertification(cert)
        case _ =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(cert : CertificationType.Value) =>
          Terminal.SellCertification(cert)
        case None =>
          Terminal.NoDeal()
      }
    }
  }

  final case class EquipmentPage(stock : Map[String, ()=>Equipment]) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(item : (()=>Equipment)) =>
          Terminal.BuyEquipment(item())
        case _ =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.SellEquipment()
  }

  final case class ImplantPage(stock : Map[String, ImplantDefinition]) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(implant : ImplantDefinition) =>
          Terminal.LearnImplant(implant)
        case None =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(implant : ImplantDefinition) =>
          Terminal.SellImplant(implant)
        case None =>
          Terminal.NoDeal()
      }
    }
  }

  final case class InfantryLoadoutPage() extends PageDefinition(Map.empty) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout : InfantryLoadout) =>
          val holsters = loadout.visible_slots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          Terminal.InfantryLoadout(loadout.exosuit, loadout.subtype, holsters, inventory)
        case _ =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
  }

  final case class VehicleLoadoutPage() extends PageDefinition(Map.empty) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      player.LoadLoadout(msg.unk1 + 10) match {
        case Some(loadout : VehicleLoadout) =>
          val weapons = loadout.visible_slots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          Terminal.VehicleLoadout(loadout.vehicle_definition, weapons, inventory)
        case _ =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
  }

  import net.psforever.objects.loadouts.{Loadout => Contents} //distinguish from Terminal.Loadout message
  final case class VehiclePage(stock : Map[String, ()=>Vehicle], trunk : Map[String, Contents]) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(vehicle) =>
          val (weapons, inventory) = trunk.get(msg.item_name) match {
            case Some(loadout : VehicleLoadout) =>
              (
                loadout.visible_slots.map(entry => { InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index) }),
                loadout.inventory.map(entry => { InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index) })
              )
            case _ =>
              (List.empty, List.empty)
          }
          Terminal.BuyVehicle(vehicle(), weapons, inventory)
        case None =>
          Terminal.NoDeal()
      }
    }

    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
  }

  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj : Amenity, context : ActorContext) : Unit = {
    import akka.actor.{ActorRef, Props}
    if(obj.Actor == ActorRef.noSender) {
      obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
    }
  }
}
