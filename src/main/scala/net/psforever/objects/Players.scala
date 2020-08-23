// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ExoSuitDefinition
import net.psforever.objects.equipment.EquipmentSlot
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.InfantryLoadout
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types.{ExoSuitType, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.annotation.tailrec

object Players {
  private val log = org.log4s.getLogger("Players")

  /**
    * Evaluate the progress of the user applying a tool to modify some other server object.
    * This action is using the medical applicator to revive a fallen (dead but not released) ally.
    *
    * @param target   the player being affected by the revive action
    * @param user the player performing the revive action
    * @param item the tool being used to revive the target player
    * @param progress the current progress value
    * @return `true`, if the next cycle of progress should occur;
    *         `false`, otherwise
    */
  def RevivingTickAction(target: Player, user: Player, item: Tool)(progress: Float): Boolean = {
    if (
      !target.isAlive && !target.isBackpack &&
      user.isAlive && !user.isMoving &&
      user.Slot(user.DrawnSlot).Equipment.contains(item) && item.Magazine >= 25 &&
      Vector3.Distance(target.Position, user.Position) < target.Definition.RepairDistance
    ) {
      val events = target.Zone.AvatarEvents
      val uname  = user.Name
      events ! AvatarServiceMessage(
        uname,
        AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(target.GUID, progress.toInt))
      )
      true
    } else {
      false
    }
  }

  /**
    * na
    * @see `AvatarAction.Revive`
    * @see `AvatarResponse.Revive`
    * @param target the player being revived
    * @param medic the name of the player doing the reviving
    * @param item the tool being used to revive the target player
    */
  def FinishRevivingPlayer(target: Player, medic: String, item: Tool)(): Unit = {
    val name = target.Name
    log.info(s"$medic had revived $name")
    val magazine = item.Discharge(Some(25))
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      medic,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine)
      )
    )
    target.Zone.AvatarEvents ! AvatarServiceMessage(name, AvatarAction.Revive(target.GUID))
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Remove any encountered items and add them to an output `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param index a number that equals the "current" holster slot (`EquipmentSlot`)
    * @param list a persistent `List` of `Equipment` in the holster slots
    * @return a `List` of `Equipment` in the holster slots
    */
  @tailrec def clearHolsters(
      iter: Iterator[EquipmentSlot],
      index: Int = 0,
      list: List[InventoryItem] = Nil
  ): List[InventoryItem] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot = iter.next()
      slot.Equipment match {
        case Some(equipment) =>
          slot.Equipment = None
          clearHolsters(iter, index + 1, InventoryItem(equipment, index) +: list)
        case None =>
          clearHolsters(iter, index + 1, list)
      }
    }
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * For any slots that are not yet occupied by an item, search through the `List` and find an item that fits in that slot.
    * Add that item to the slot and remove it from the list.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param list a `List` of all `Equipment` that is not yet assigned to a holster slot or an inventory slot
    * @return the `List` of all `Equipment` not yet assigned to a holster slot or an inventory slot
    */
  @tailrec def fillEmptyHolsters(iter: Iterator[EquipmentSlot], list: List[InventoryItem]): List[InventoryItem] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot = iter.next()
      if (slot.Equipment.isEmpty) {
        list.find(item => item.obj.Size == slot.Size) match {
          case Some(obj) =>
            val index = list.indexOf(obj)
            slot.Equipment = obj.obj
            fillEmptyHolsters(iter, list.take(index) ++ list.drop(index + 1))
          case None =>
            fillEmptyHolsters(iter, list)
        }
      } else {
        fillEmptyHolsters(iter, list)
      }
    }
  }

  def CertificationToUseExoSuit(player: Player, exosuit: ExoSuitType.Value, subtype: Int): Boolean = {
    ExoSuitDefinition.Select(exosuit, player.Faction).Permissions match {
      case Nil =>
        true
      case permissions if subtype != 0 =>
        val certs = player.avatar.certifications
        certs.intersect(permissions.toSet).nonEmpty &&
        certs.intersect(InfantryLoadout.DetermineSubtypeC(subtype)).nonEmpty
      case permissions =>
        player.avatar.certifications.intersect(permissions.toSet).nonEmpty
    }
  }
}
