// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.VehicleLoadout
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.terminals.{EquipmentTerminalDefinition, Terminal}
import net.psforever.packet.game.ItemTransactionMessage

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
final case class VehiclePage(stock: Map[String, () => Vehicle], trunk: Map[String, Contents]) extends ScrutinizedTab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some(vehicle) =>
        val createdVehicle = vehicle()
        if(!Exclude.exists(_.checkRule(player, msg, createdVehicle))) {
          val (weapons, inventory) = trunk.get(msg.item_name) match {
            case Some(loadout: VehicleLoadout) =>
              (
                loadout.visible_slots.map(entry => {
                  InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index)
                }),
                loadout.inventory.map(entry => {
                  InventoryItem(EquipmentTerminalDefinition.BuildSimplifiedPattern(entry.item), entry.index)
                })
                  .filterNot( item => Exclude.exists(_.checkRule(player, msg, item.obj)))
              )
            case _ =>
              (List.empty, List.empty)
          }
          Terminal.BuyVehicle(createdVehicle, weapons, inventory)
        } else {
          Terminal.NoDeal()
        }
      case None =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    sender ! msg
  }
}
