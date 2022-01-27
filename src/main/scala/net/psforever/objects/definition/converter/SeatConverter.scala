// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.objects.serverobject.mount.Seat
import net.psforever.packet.game.objectcreate._

object SeatConverter {
  def MakeSeat(player: Player, offset: Long): PlayerData = {
    MountableInventory.PlayerData(
      AvatarConverter.MakeAppearanceData(player),
      AvatarConverter.MakeCharacterData(player),
      AvatarConverter.MakeInventoryData(player),
      AvatarConverter.GetDrawnSlot(player),
      offset
    )
  }

  //TODO do not use for now; causes mount access permission issues with many passengers; may not mesh with workflows; GUID requirements
  def MakeSeats(seats: Map[Int, Seat], initialOffset: Long): List[InventoryItemData.InventoryItem] = {
    var offset = initialOffset
    seats
      .filter({ case (_, seat) => seat.isOccupied })
      .map({
        case (index: Int, seat: Seat) =>
          val player = seat.occupant.get
          val entry  = InventoryItemData(ObjectClass.avatar, player.GUID, index, SeatConverter.MakeSeat(player, offset))
          offset += entry.bitsize
          entry
      })
      .toList
  }
}
