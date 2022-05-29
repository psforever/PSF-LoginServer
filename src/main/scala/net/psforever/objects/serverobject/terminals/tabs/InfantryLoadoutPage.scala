// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.InfantryLoadout
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition.BuildSimplifiedPattern
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

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
final case class InfantryLoadoutPage() extends LoadoutTab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    player.avatar.loadouts(msg.unk1) match {
      case Some(loadout: InfantryLoadout)
        if !Exclude.exists(_.checkRule(player, msg, loadout.exosuit)) &&
           !Exclude.exists(_.checkRule(player, msg, (loadout.exosuit, loadout.subtype))) =>
        val holsters = loadout.visible_slots
          .map(entry => {
            InventoryItem(BuildSimplifiedPattern(entry.item), entry.index)
          })
          .filterNot { entry => Exclude.exists(_.checkRule(player, msg, entry.obj)) }
        val inventory = loadout.inventory
          .map(entry => {
            InventoryItem(BuildSimplifiedPattern(entry.item), entry.index)
          })
          .filterNot { entry => Exclude.exists(_.checkRule(player, msg, entry.obj)) }
        Terminal.InfantryLoadout(loadout.exosuit, loadout.subtype, holsters, inventory)
      case _ =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.player.Actor ! msg
  }
}
