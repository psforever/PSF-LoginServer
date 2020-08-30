// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.packet.game.InventoryStateMessage
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

object Tools {
  def ChargeFireMode(holder: Player, weapon: Tool)(progress: Float): Boolean = {
    if(weapon.Magazine > 0) {
      if(progress % 2 == 0) {
        val magazine = weapon.Magazine -= 1
        holder.Zone.AvatarEvents ! AvatarServiceMessage(
          holder.Name,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, magazine)
          )
        )
      }
      true
    }
    else {
      false
    }
  }

  def FinishChargeFireMode(holder: Player, weapon: Tool)(): Unit = {
    if(weapon.Magazine > 0) {
      val magazine = weapon.Magazine -= 1
      holder.Zone.AvatarEvents ! AvatarServiceMessage(
        holder.Name,
        AvatarAction.SendResponse(
          Service.defaultPlayerGUID,
          InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, magazine)
        )
      )
    }
  }
}