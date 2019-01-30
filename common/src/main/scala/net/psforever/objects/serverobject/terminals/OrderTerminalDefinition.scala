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
  * The definition for any `Terminal` that is of a type "order_terminal,"
  * i.e., an amenity from/through which the user can exchange denominations of in-game hardware (items).
  * This hardware is organized as "stock," occasionally supplemented.
  * The pages of any given type of terminal determines the behavior available from that page
  * and what stock can be drawn or returned.
  */
class OrderTerminalDefinition(objId : Int) extends TerminalDefinition(objId) {
  private val pages : mutable.HashMap[Int, OrderTerminalDefinition.PageDefinition] =
    new mutable.HashMap[Int, OrderTerminalDefinition.PageDefinition]()
  private var sellEquipmentDefault : Boolean = false

  def Page : mutable.HashMap[Int, OrderTerminalDefinition.PageDefinition] = pages

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

object OrderTerminalDefinition {
  abstract class PageDefinition(stock : Map[String, Any]) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange
    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange
  }

  final case class ArmorPage(stock : Map[String, (ExoSuitType.Value, Int)], ammo : Map[String, ()=>Equipment] = Map.empty) extends PageDefinition(stock) {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some((suit : ExoSuitType.Value, subtype : Int)) =>
          Terminal.BuyExosuit(suit, subtype)
        case _ =>
          ammo.get(msg.item_name) match {
            case Some(item : (()=>Equipment)) =>
              Terminal.BuyEquipment(item())
            case _ =>
              Terminal.NoDeal()
          }
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
