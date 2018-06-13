// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.Player
import net.psforever.objects.loadouts.InfantryLoadout
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition._
import net.psforever.types.ExoSuitType

/**
  * The definition for any `Terminal` that is of a type "ams_order_terminal".
  * As the name indicates, paired on the flanks of an advanced mobile spawn vehicle.<br>
  * <br>
  * `Buy` and `Sell` `Equipment` items and `AmmoBox` items.
  * Change `ExoSuitType` and retrieve `Loadout` entries.
  * Changing into mechanized assault exo-suits (MAXes) is not permitted.
  */
class OrderTerminalABDefinition(object_id : Int) extends EquipmentTerminalDefinition(object_id) {
  if(object_id == 613) {
    Name = "order_terminala"
  }
  else if(object_id == 614) {
    Name = "order_terminalb"
  }
  else {
    throw new IllegalArgumentException("terminal must be either object id 613 or object id 614")
  }

  /**
    * The `Equipment` available from this `Terminal` on specific pages.
    */
  private val buyFunc : (Player, ItemTransactionMessage)=>Terminal.Exchange =
    EquipmentTerminalDefinition.Buy(
      infantryAmmunition ++ infantryWeapons,
      supportAmmunition ++ supportWeapons,
      suits
    )

  override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = buyFunc(player, msg)

  /**
    * Process a `TransactionType.Loadout` action by the user.
    * `Loadout` objects are blueprints composed of exo-suit specifications and simplified `Equipment`-to-slot mappings.
    * If a valid loadout is found, its data is transformed back into actual `Equipment` for return to the user.
    * Loadouts that would suit the player into a mechanized assault exo-suit are not permitted.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout : InfantryLoadout) =>
          if(loadout.exosuit != ExoSuitType.MAX) {
            val holsters = loadout.visible_slots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
            val inventory = loadout.inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
            Terminal.InfantryLoadout(loadout.exosuit, loadout.subtype, holsters, inventory)
          }
          else {
            Terminal.NoDeal()
          }
        case Some(_) | None =>
          Terminal.NoDeal()
      }
    }
    else {
      Terminal.NoDeal()
    }
  }
}

object OrderTerminalABDefinition {
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
