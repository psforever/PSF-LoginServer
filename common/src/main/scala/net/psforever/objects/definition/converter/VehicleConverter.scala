// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{InventoryItemData, _}

import scala.util.{Failure, Success, Try}

class VehicleConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj : Vehicle) : Try[VehicleData] =
    Failure(new Exception("VehicleConverter should not be used to generate detailed VehicleData"))

  override def ConstructorData(obj : Vehicle) : Try[VehicleData] = {
    Success(
      VehicleData(
        CommonFieldData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          obj.Faction,
          0,
          PlanetSideGUID(0) //if(obj.Owner.isDefined) { obj.Owner.get } else { PlanetSideGUID(0) } //TODO is this really Owner?
        ),
        0,
        obj.Health / obj.MaxHealth * 255, //TODO not precise
        false, false,
        obj.Drive,
        false,
        false,
        obj.Cloaked,
        SpecificFormatData(obj),
        Some(InventoryData((MakeUtilities(obj) ++ MakeMountings(obj)).sortBy(_.parentSlot)))
      )(SpecificFormatModifier)
    )
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
