// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.VehicleLoadout
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.terminals.{EquipmentTerminalDefinition, Terminal}
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The special page used by the `bfr_terminal` to select a vehicle to be spawned
  * based on the player's previous loadouts for battleframe vehicles.
  * Vehicle loadouts are defined by a superfluous redefinition of the vehicle's mounted weapons
  * and equipment in the trunk.
  * @see `Equipment`
  * @see `Loadout`
  * @see `Vehicle`
  * @see `VehicleLoadout`
  */
final case class BattleframeSpawnLoadoutPage(vehicles: Map[String, () => Vehicle]) extends LoadoutTab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    player.avatar.loadouts(msg.unk1 + 15) match {
      case Some(loadout: VehicleLoadout) if !Exclude.contains(loadout.vehicle_definition) =>
        vehicles.get(loadout.vehicle_definition.Name) match {
          case Some(vehicle) =>
            val weapons = loadout.visible_slots.map(entry => {
              InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index)
            })
            val inventory = loadout.inventory.map(entry => {
              InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index)
            })
              .filterNot { entry => Exclude.exists(_.checkRule(player, msg, entry.obj)) }
            Terminal.BuyVehicle(vehicle(), weapons, inventory)
          case None =>
            Terminal.NoDeal()
        }

      case _ =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    sender ! msg
  }
}
