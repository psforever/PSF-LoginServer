// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.objects.vehicles.Seat
import net.psforever.packet.game.objectcreate.{InventoryItemData, ObjectClass, PlayerData, VehicleData}

object SeatConverter {
  def MakeSeat(player: Player, offset: Long): PlayerData = {
    VehicleData.PlayerData(
      AvatarConverter.MakeAppearanceData(player),
      AvatarConverter.MakeCharacterData(player),
      AvatarConverter.MakeInventoryData(player),
      AvatarConverter.GetDrawnSlot(player),
      offset
    )
  }

  //TODO do not use for now; causes seat access permission issues with many passengers; may not mesh with workflows; GUID requirements
  def MakeSeats(seats: Map[Int, Seat], initialOffset: Long): List[InventoryItemData.InventoryItem] = {
    var offset = initialOffset
    seats
      .filter({ case (_, seat) => seat.isOccupied })
      .map({
        case (index, seat) =>
          val player = seat.Occupant.get
          val entry  = InventoryItemData(ObjectClass.avatar, player.GUID, index, SeatConverter.MakeSeat(player, offset))
          offset += entry.bitsize
          entry
      })
      .toList
  }
}
