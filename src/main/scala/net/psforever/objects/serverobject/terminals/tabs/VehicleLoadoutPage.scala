// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.VehicleLoadout
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition.BuildSimplifiedPattern
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

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
final case class VehicleLoadoutPage(lineOffset: Int) extends LoadoutTab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    player.avatar.loadouts.suit(msg.unk1 + lineOffset) match {
      case Some(loadout: VehicleLoadout) =>
        val weapons = loadout.visible_slots
          .map(entry => {
            InventoryItem(BuildSimplifiedPattern(entry.item), entry.index)
          })
        val inventory = loadout.inventory
          .map(entry => {
            InventoryItem(BuildSimplifiedPattern(entry.item), entry.index)
          })
          .filterNot { entry => Exclude.exists(_.checkRule(player, msg, entry.obj)) }
        Terminal.VehicleLoadout(loadout.vehicle_definition, weapons, inventory)
      case _ =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    val player = msg.player
    player.Zone.GUID(player.avatar.vehicle) match {
      case Some(vehicle: Vehicle) => vehicle.Actor ! msg
      case _                      => sender ! Terminal.TerminalMessage(player, msg.msg, Terminal.NoDeal())
    }
  }
}
