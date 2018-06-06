// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{InventoryItemData, _}

import scala.util.{Failure, Success, Try}

class VehicleConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj : Vehicle) : Try[VehicleData] =
    Failure(new Exception("VehicleConverter should not be used to generate detailed VehicleData (nothing should)"))

  override def ConstructorData(obj : Vehicle) : Try[VehicleData] = {
    val health = 255 * obj.Health / obj.MaxHealth //TODO not precise
    Success(
      VehicleData(
        PlacementData(obj.Position, obj.Orientation, obj.Velocity),
        obj.Faction,
        false, //bops
        health < 3, //destroyed
        0,
        obj.Jammered, //jammered
        false,
        obj.Owner match {
          case Some(owner) => owner
          case None => PlanetSideGUID(0)
        },
        false,
        health,
        false, false,
        obj.DeploymentState,
        false,
        false,
        obj.Cloaked,
        SpecificFormatData(obj),
        Some(InventoryData((MakeSeats(obj) ++ MakeUtilities(obj) ++ MakeMountings(obj)).sortBy(_.parentSlot)))
      )(SpecificFormatModifier)
    )
  }

  private def MakeSeats(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
    val offset : Long = VehicleData.InitialStreamLengthToSeatEntries(true, SpecificFormatModifier)
    obj.Seats(0).Occupant match { //TODO just the driver for now to avoid issues with seat permissions
      case Some(player) =>
        val mountedPlayer = VehicleData.PlayerData(
          AvatarConverter.MakeAppearanceData(player),
          AvatarConverter.MakeCharacterData(player),
          AvatarConverter.MakeInventoryData(player),
          AvatarConverter.GetDrawnSlot(player),
          offset
        )
        val entry = InventoryItemData(ObjectClass.avatar, player.GUID, 0, mountedPlayer)
        //println(s"seat 0 offset: $offset, size: ${entry.bitsize}, pad: ${mountedPlayer.basic_appearance.NamePadding}")
        //offset += entry.bitsize
        List(entry)
      case None =>
        Nil
    }
  }
  
  private def MakeMountings(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
    obj.Weapons.map({
      case((index, slot)) =>
        val equip : Equipment = slot.Equipment.get
        val equipDef = equip.Definition
        InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
    }).toList
  }

  protected def MakeUtilities(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
    Vehicle.EquipmentUtilities(obj.Utilities).map({
      case(index, utilContainer) =>
        val util = utilContainer()
        val utilDef = util.Definition
        InventoryItemData(utilDef.ObjectId, util.GUID, index, utilDef.Packet.ConstructorData(util).get)
    }).toList
  }

  protected def SpecificFormatModifier : VehicleFormat.Value = VehicleFormat.Normal

  protected def SpecificFormatData(obj : Vehicle) : Option[SpecificVehicleData] = None
}
