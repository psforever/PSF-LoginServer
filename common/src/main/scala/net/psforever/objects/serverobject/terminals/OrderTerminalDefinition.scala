// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition._

import scala.annotation.switch

/**
  * The definition for any `Terminal` that is of a type "order_terminal".
  * `Buy` and `Sell` `Equipment` items and `AmmoBox` items.
  * Change `ExoSuitType` and retrieve `Loadout` entries.
  */
class OrderTerminalDefinition extends EquipmentTerminalDefinition(612) {
  Name = "order_terminal"

  /**
    * The `Equipment` available from this `Terminal` on specific pages.
    */
  private val page0Stock : Map[String, ()=>Equipment] = infantryAmmunition ++ infantryWeapons
  private val page2Stock : Map[String, ()=>Equipment] = supportAmmunition ++ supportWeapons

  /**
    * Process a `TransactionType.Buy` action by the user.
    * Either attempt to purchase equipment or attempt to switch directly to a different exo-suit.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange =  {
    (msg.item_page : @switch) match {
      case 0 => //Weapon tab
        page0Stock.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 2 => //Support tab
        page2Stock.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 3 => //Vehicle tab
        vehicleAmmunition.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 1 => //Armor tab
        suits.get(msg.item_name) match {
          case Some((suit, subtype)) =>
            Terminal.BuyExosuit(suit, subtype)
          case None =>
            Terminal.NoDeal()
        }
      case _ =>
        maxAmmo.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
    }
  }

  /**
    * Process a `TransactionType.Sell` action by the user.
    * There is no specific `order_terminal` tab associated with this action.
    * Additionally, the equipment to be sold ia almost always in the player's `FreeHand` slot.
    * Selling `Equipment` is always permitted.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    Terminal.SellEquipment()
  }

  /**
    * Process a `TransactionType.InfantryLoadout` action by the user.
    * `Loadout` objects are blueprints composed of exo-suit specifications and simplified `Equipment`-to-slot mappings.
    * If a valid loadout is found, its data is transformed back into actual `Equipment` for return to the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout) =>
          val holsters = loadout.VisibleSlots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.Inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          Terminal.InfantryLoadout(loadout.ExoSuit, loadout.Subtype, holsters, inventory)
        case None =>
          Terminal.NoDeal()
      }
    }
    else {
      Terminal.NoDeal()
    }
  }
}
