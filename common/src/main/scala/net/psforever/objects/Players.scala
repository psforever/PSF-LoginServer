// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

object Players {
  private val log = org.log4s.getLogger("Players")

  /**
    * Evaluate the progress of the user applying a tool to modify some other server object.
    * This action is using the medical applicator to revive a fallen (dead but not released) ally.
    * @param target the player being affected by the revive action
    * @param user the player performing the revive action
    * @param item the tool being used to revive the target player
    * @param progress the current progress value
    * @return `true`, if the next cycle of progress should occur;
    *         `false`, otherwise
    */
  def RevivingTickAction(target : Player, user : Player, item : Tool)(progress : Float) : Boolean = {
    if(!target.isAlive && !target.isBackpack &&
      user.isAlive && !user.isMoving &&
      user.Slot(user.DrawnSlot).Equipment.contains(item) && item.Magazine > 0) {
      val magazine = item.Discharge
      val events = target.Zone.AvatarEvents
      val uname = user.Name
      events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine)))
      events ! AvatarServiceMessage(uname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(target.GUID, progress.toInt)))
      true
    }
    else {
      false
    }
  }

  /**
    * na
    * @see `AvatarAction.Revive`
    * @see `AvatarResponse.Revive`
    * @param target the player being revived
    * @param medic the name of the player doing the reviving
    */
  def FinishRevivingPlayer(target : Player, medic : String)() : Unit = {
    val name = target.Name
    log.info(s"$medic had revived $name")
    target.Zone.AvatarEvents ! AvatarServiceMessage(name, AvatarAction.Revive(target.GUID))
  }
}
