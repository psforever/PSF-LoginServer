// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.packet.game.objectcreate._

import scala.util.{Success, Try}

object AvatarConverter extends ObjectCreateConverter[Player] {
  import CharacterConverter._

  override def ConstructorData(obj: Player): Try[PlayerData] = {
    Success(
      if (obj.VehicleSeated.isEmpty) {
        PlayerData(
          PlacementData(obj.Position, obj.Orientation, None),
          MakeAppearanceData(obj),
          MakeCharacterData(obj),
          MakeInventoryData(obj),
          GetDrawnSlot(obj)
        )
      } else {
        PlayerData(
          MakeAppearanceData(obj),
          MakeCharacterData(obj),
          MakeInventoryData(obj),
          DrawnSlot.None
        )
      }
    )
  }

  override def DetailedConstructorData(obj: Player): Try[DetailedPlayerData] = {
    Success(
      if (obj.VehicleSeated.isEmpty) {
        DetailedPlayerData.apply(
          PlacementData(obj.Position, obj.Orientation, None),
          MakeAppearanceData(obj),
          MakeDetailedCharacterData(obj),
          MakeDetailedInventoryData(obj),
          GetDrawnSlot(obj)
        )
      } else {
        DetailedPlayerData.apply(
          MakeAppearanceData(obj),
          MakeDetailedCharacterData(obj),
          MakeDetailedInventoryData(obj),
          GetDrawnSlot(obj)
        )
      }
    )
  }
}

