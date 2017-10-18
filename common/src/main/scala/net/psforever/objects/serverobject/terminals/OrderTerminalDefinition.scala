// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.InfantryLoadout.Simplification
import net.psforever.objects.{Player, Tool}
import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.packet.game.ItemTransactionMessage

import scala.annotation.switch

/**
  * The definition for any `Terminal` that is of a type "order_terminal".
  * `Buy` and `Sell` `Equipment` items and `AmmoBox` items.
  * Change `ExoSuitType` and retrieve `InfantryLoadout` entries.
  */
class OrderTerminalDefinition extends TerminalDefinition(612) {
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
        Terminal.NoDeal()
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
  def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    Terminal.SellEquipment()
  }

  /**
    * Process a `TransactionType.InfantryLoadout` action by the user.
    * `InfantryLoadout` objects are blueprints composed of exo-suit specifications and simplified `Equipment`-to-slot mappings.
    * If a valid loadout is found, its data is transformed back into actual `Equipment` for return to the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout) =>
          val holsters = loadout.Holsters.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
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

  /**
    * Accept a simplified blueprint for some piece of `Equipment` and create an actual piece of `Equipment` based on it.
    * Used specifically for the reconstruction of `Equipment` via an `InfantryLoadout`.
    * @param entry the simplified blueprint
    * @return some `Equipment` object
    * @see `TerminalDefinition.MakeTool`<br>
    *       `TerminalDefinition.MakeAmmoBox`<br>
    *       `TerminalDefinition.MakeSimpleItem`<br>
    *       `TerminalDefinition.MakeConstructionItem`<br>
    *       `TerminalDefinition.MakeKit`
    */
  private def BuildSimplifiedPattern(entry : Simplification) : Equipment = {
    import net.psforever.objects.InfantryLoadout._
    entry match {
      case obj : ShorthandTool =>
        val ammo : List[AmmoBoxDefinition] = obj.ammo.map(fmode => { fmode.ammo.adef })
        val tool = Tool(obj.tdef)
        //makes Tools where an ammo slot may have one of its alternate ammo types
        (0 until tool.MaxAmmoSlot).foreach(index => {
          val slot = tool.AmmoSlots(index)
          slot.AmmoTypeIndex += obj.ammo(index).ammoIndex
          slot.Box = MakeAmmoBox(ammo(index), Some(obj.ammo(index).ammo.capacity))
        })
        tool

      case obj : ShorthandAmmoBox =>
        MakeAmmoBox(obj.adef, Some(obj.capacity))

      case obj : ShorthandConstructionItem =>
        MakeConstructionItem(obj.cdef)

      case obj : ShorthandSimpleItem =>
        MakeSimpleItem(obj.sdef)

      case obj : ShorthandKit =>
        MakeKit(obj.kdef)
    }
  }
}
