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
import net.psforever.types.{CertificationType, ExoSuitType, TransactionType}

import scala.collection.mutable

/**
  * The definition for any `Terminal` from which specifications can be altered.
  * These specification alternations involve three classifications:
  * the exchange of denominations of in-game hardware, i.e., `Equipment`,
  * the modification of lists of personal statistics, e.g., `Certifications`,
  * and saving and loading of preset configurations, i.e., `Loadouts`.
  * This hardware is organized as "stock," occasionally supplemented.
  * Terminals have tabs (visually) that are organized by different stock (internally)
  * that determines the behavior available from that tab
  * and what stock can be drawn or returned.<br>
  * <br>
  * Equipment terminals are the property of bases and vehicles ("amenities").
  * To bases, the `Terminal` object is coupled loosely and may be allowed to diverge.
  * To vehicles, the `Terminal` object is coupled directly to the faction affiliation of the vehicle.
  * @see `Amenity`
  * @see `Terminal`
  * @see `Utility`
  */
class OrderTerminalDefinition(objId : Int) extends TerminalDefinition(objId) {
  /** An internal object organizing the different specification options found on a terminal's UI. */
  private val tabs : mutable.HashMap[Int, OrderTerminalDefinition.Tab] =
    new mutable.HashMap[Int, OrderTerminalDefinition.Tab]()
  /** Disconnect the ability to return stock back to the terminal
    * from the type of stock available from the terminal in general
    * or the type of stock available from its denoted page.
    * Will always return a message of type `SellEquipment`.*/
  private var sellEquipmentDefault : Boolean = false

  def Tab : mutable.HashMap[Int, OrderTerminalDefinition.Tab] = tabs

  def SellEquipmentByDefault : Boolean = sellEquipmentDefault

  def SellEquipmentByDefault_=(sell : Boolean) : Boolean = {
    sellEquipmentDefault = sell
    SellEquipmentByDefault
  }

  def Request(player : Player, msg : Any) : Terminal.Exchange = msg match {
    case message : ItemTransactionMessage =>
      message.transaction_type match {
        case TransactionType.Buy | TransactionType.Learn =>
          Buy(player, message)
        case TransactionType.Loadout =>
          Loadout(player, message)
        case TransactionType.Sell =>
          Sell(player, message)
        case _ =>
          Terminal.NoDeal()
      }
    case _ =>
      Terminal.NoDeal()
  }

  private def Buy(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    tabs.get(msg.item_page) match {
      case Some(page) =>
        page.Buy(player, msg)
      case _ =>
        Terminal.NoDeal()
    }
  }

  private def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Buy(player, msg)

  private def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(sellEquipmentDefault) {
      Terminal.SellEquipment()
    }
    else {
      tabs.get(msg.item_page) match {
        case Some(page) =>
          page.Sell(player, msg)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }
}

object OrderTerminalDefinition {
  /**
    * A basic tab outlining the specific type of stock available from this part of the terminal's interface.
    * @see `ItemTransactionMessage`
    */
  sealed trait Tab {
    def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
    def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
  }

  /**
    * The tab used to select an exo-suit to be worn by the player.
    * @see `ExoSuitType`
    * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a tuple composed of an `ExoSuitType` value and a subtype value
    */
  final case class ArmorPage(stock : Map[String, (ExoSuitType.Value, Int)]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some((suit : ExoSuitType.Value, subtype : Int)) =>
          Terminal.BuyExosuit(suit, subtype)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * An expanded form of the tab used to select an exo-suit to be worn by the player that also provides some equipment.
    * @see `ExoSuitType`
    * @see `Equipment`
    * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a tuple composed of an `ExoSuitType` value and a subtype value
    * @param items the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a curried function that produces an `Equipment` object
    */
  final case class ArmorWithAmmoPage(stock : Map[String, (ExoSuitType.Value, Int)], items : Map[String, ()=>Equipment]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some((suit : ExoSuitType.Value, subtype : Int)) =>
          Terminal.BuyExosuit(suit, subtype)
        case _ =>
          items.get(msg.item_name) match {
            case Some(item) =>
              Terminal.BuyEquipment(item())
            case _ =>
              Terminal.NoDeal()
          }
      }
    }
  }

  /**
    * The tab used to select a certification to be utilized by the player.
    * Only certifications may be returned to the interface defined by this page.
    * @see `CertificationType`
    * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a `CertificationType` value
    */
  final case class CertificationPage(stock : Map[String, CertificationType.Value]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(cert : CertificationType.Value) =>
          Terminal.LearnCertification(cert)
        case _ =>
          Terminal.NoDeal()
      }
    }

    override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(cert : CertificationType.Value) =>
          Terminal.SellCertification(cert)
        case None =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * The tab used to produce an `Equipment` object to be used by the player.
    * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a curried function that produces an `Equipment` object
    */
  final case class EquipmentPage(stock : Map[String, ()=>Equipment]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(item) =>
          Terminal.BuyEquipment(item())
        case _ =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * The tab used to select an implant to be utilized by the player.
    * A maximum of three implants can be obtained by any player at a time depending on the player's battle rank.
    * Only implants may be returned to the interface defined by this page.
    * @see `ImplantDefinition`
    * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
    *              the value is a `CertificationType` value
    */
  final case class ImplantPage(stock : Map[String, ImplantDefinition]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(implant : ImplantDefinition) =>
          Terminal.LearnImplant(implant)
        case None =>
          Terminal.NoDeal()
      }
    }

    override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      stock.get(msg.item_name) match {
        case Some(implant : ImplantDefinition) =>
          Terminal.SellImplant(implant)
        case None =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * The base class for "loadout" type tabs.
    * Defines logic for enumerating items and entities that should be eliminated from being loaded.
    * The method for filtering those excluded items, if applicable,
    * and management of the resulting loadout object
    * is the responsibility of the specific tab that is instantiated.
    */
  abstract class LoadoutTab extends Tab {
    private var contraband : Seq[Any] = Nil

    def Exclude : Seq[Any] = contraband

    def Exclude_=(equipment : Any) : Seq[Any] = {
      contraband = Seq(equipment)
      Exclude
    }

    def Exclude_=(equipmentList : Seq[Any]) : Seq[Any] = {
      contraband = equipmentList
      Exclude
    }
  }

  /**
    * The tab used to select which custom loadout the player is using.
    * Player loadouts are defined by an exo-suit to be worn by the player
    * and equipment in the holsters and the inventory.
    * In this case, the reference to the player that is a parameter of the functions maintains information about the loadouts;
    * no extra information specific to this page is necessary.
    * If an exo-suit type is considered excluded, the whole loadout is blocked.
    * If the exclusion is written as a `Tuple` object `(A, B)`,
    * `A` will be expected as an exo-suit type, and `B` will be expected as its subtype,
    * and the pair must both match to block the whole loadout.
    * If any of the player's inventory is considered excluded, only those items will be filtered.
    * @see `ExoSuitType`
    * @see `Equipment`
    * @see `InfantryLoadout`
    * @see `Loadout`
    */
  //TODO block equipment by blocking ammunition type
  final case class InfantryLoadoutPage() extends LoadoutTab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout : InfantryLoadout) if !Exclude.contains(loadout.exosuit) && !Exclude.contains((loadout.exosuit, loadout.subtype)) =>
          val holsters = loadout.visible_slots
            .map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
            .filterNot { entry => Exclude.contains(entry.obj.Definition) }
          val inventory = loadout.inventory
            .map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
            .filterNot { entry => Exclude.contains(entry.obj.Definition) }
          Terminal.InfantryLoadout(loadout.exosuit, loadout.subtype, holsters, inventory)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * The tab used to select which custom loadout the player's vehicle is using.
    * Vehicle loadouts are defined by a (superfluous) redefinition of the vehicle's mounted weapons
    * and equipment in the trunk.
    * In this case, the reference to the player that is a parameter of the functions maintains information about the loadouts;
    * no extra information specific to this page is necessary.
    * If a vehicle type (by definition) is considered excluded, the whole loadout is blocked.
    * If any of the vehicle's inventory is considered excluded, only those items will be filtered.
    * @see `Equipment`
    * @see `Loadout`
    * @see `VehicleLoadout`
    */
  final case class VehicleLoadoutPage() extends LoadoutTab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
      player.LoadLoadout(msg.unk1 + 10) match {
        case Some(loadout : VehicleLoadout) if !Exclude.contains(loadout.vehicle_definition) =>
          val weapons = loadout.visible_slots
            .map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.inventory
            .map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
            .filterNot { entry => Exclude.contains(entry.obj.Definition) }
          Terminal.VehicleLoadout(loadout.vehicle_definition, weapons, inventory)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }

  /**
    * The tab used to select a vehicle to be spawned for the player.
    * Vehicle loadouts are defined by a superfluous redefinition of the vehicle's mounted weapons
    * and equipment in the trunk
    * for the purpose of establishing default contents.
    * @see `Equipment`
    * @see `Loadout`
    * @see `Vehicle`
    * @see `VehicleLoadout`
    */
  import net.psforever.objects.loadouts.{Loadout => Contents} //distinguish from Terminal.Loadout message
  final case class VehiclePage(stock : Map[String, ()=>Vehicle], trunk : Map[String, Contents]) extends Tab {
    override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
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
