// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.packet.game.QuantityUpdateMessage
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

object Tools {
  def ChargeFireMode(holder: Player, weapon: Tool)(progress: Float): Boolean = {
    if (weapon.Magazine > 0) {
      val magazine = weapon.Magazine -= 1
      holder.Zone.AvatarEvents ! AvatarServiceMessage(
        holder.Name,
        AvatarAction.SendResponse(
          Service.defaultPlayerGUID,
          QuantityUpdateMessage(weapon.AmmoSlot.Box.GUID, magazine)
        )
      )
      holder.isAlive
    }
    else {
      false
    }
  }
}
