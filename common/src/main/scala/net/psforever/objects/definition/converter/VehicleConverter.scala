// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{InventoryData, InventoryItemData, ObjectClass, PlacementData, SpecificVehicleData, VehicleData, VehicleFormat}
import net.psforever.types.DriveState

import scala.util.{Failure, Success, Try}

class VehicleConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj : Vehicle) : Try[VehicleData] =
    Failure(new Exception("VehicleConverter should not be used to generate detailed VehicleData (nothing should)"))

  override def ConstructorData(obj : Vehicle) : Try[VehicleData] = {
    val health = 255 * obj.Health / obj.MaxHealth //TODO not precise
    if(health > 3) { //active
      Success(
        VehicleData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          obj.Faction,
          bops = false,
          destroyed = false,
          unk1 = 0,
          obj.Jammered,
          unk2 = false,
          obj.Owner match {
            case Some(owner) => owner
            case None => PlanetSideGUID(0)
          },
          unk3 = false,
          health,
          unk4 = false,
          no_mount_points = false,
          obj.DeploymentState,
          unk5 = false,
          unk6 = false,
          obj.Cloaked,
          SpecificFormatData(obj),
          Some(InventoryData(MakeDriverSeat(obj) ++ MakeUtilities(obj) ++ MakeMountings(obj)))
        )(SpecificFormatModifier)
      )
    }
    else { //destroyed
      Success(
        VehicleData(
          PlacementData(obj.Position, obj.Orientation),
          obj.Faction,
          bops = false,
          destroyed = true,
          unk1 = 0,
          jammered = false,
          unk2 = false,
          owner_guid = PlanetSideGUID(0),
          unk3 = false,
          health = 0,
          unk4 = false,
          no_mount_points = true,
          driveState = DriveState.Mobile,
          unk5 = false,
          unk6 = false,
          cloak = false,
          SpecificFormatData(obj),
          inventory = None
        )(SpecificFormatModifier)
      )
    }
  }

  private def MakeDriverSeat(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
    val offset : Long = VehicleData.InitialStreamLengthToSeatEntries(obj.Velocity.nonEmpty, SpecificFormatModifier)
    obj.Seats(0).Occupant match {
      case Some(player) =>
        val mountedPlayer = VehicleData.PlayerData(
          AvatarConverter.MakeAppearanceData(player),
          AvatarConverter.MakeCharacterData(player),
          AvatarConverter.MakeInventoryData(player),
          AvatarConverter.GetDrawnSlot(player),
          offset
        )
        List(InventoryItemData(ObjectClass.avatar, player.GUID, 0, mountedPlayer))
      case None =>
        Nil
    }
  }

  //TODO do not use for now; causes vehicle access permission issues; may not mesh with workflows; player GUID requirements
//  private def MakeSeats(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
//    var offset : Long = VehicleData.InitialStreamLengthToSeatEntries(obj.Velocity.nonEmpty, SpecificFormatModifier)
//    obj.Seats
//      .filter({ case (_, seat) => seat.isOccupied })
//      .map({case (index, seat) =>
//        val player = seat.Occupant.get
//        val mountedPlayer = VehicleData.PlayerData(
//          AvatarConverter.MakeAppearanceData(player),
//          AvatarConverter.MakeCharacterData(player),
//          AvatarConverter.MakeInventoryData(player),
//          AvatarConverter.GetDrawnSlot(player),
//          offset
//        )
//        val entry = InventoryItemData(ObjectClass.avatar, player.GUID, index, mountedPlayer)
//        //println(s"seat 0 offset: $offset, size: ${entry.bitsize}, pad: ${mountedPlayer.basic_appearance.NamePadding}")
//        offset += entry.bitsize
//        entry
//      }).toList
//  }
  
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
