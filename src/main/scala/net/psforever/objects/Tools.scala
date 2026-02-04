// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.equipment.ChargeFireModeDefinition
import net.psforever.packet.game.QuantityUpdateMessage
import net.psforever.services.avatar.AvatarServiceMessage
import net.psforever.services.base.messages.SendResponse

object Tools {
  /**
    *
    * @param player the player performing the revive action
    * @param tool the tool being used to execute the attack;
    *             should have a selected chargeable fire mode
    * @param progress the current progress value
    * @see `ChargeFireModeDefinition`
    * @see `QuantityUpdateMessage`
    * @return `true`, if the next cycle of progress should occur;
    *         `false`, otherwise
    */
  def ChargeFireMode(player: Player, tool: Tool)(progress: Float): Boolean = {
    tool.FireMode match {
      case mode: ChargeFireModeDefinition if tool.Magazine > 0 =>
        val magazine = tool.Magazine -= mode.RoundsPerInterval
        player.Zone.AvatarEvents ! AvatarServiceMessage(
          player.Name,
          SendResponse(QuantityUpdateMessage(tool.AmmoSlot.Box.GUID, magazine))
        )
        player.isAlive
      case _ =>
        false
    }
  }
}
