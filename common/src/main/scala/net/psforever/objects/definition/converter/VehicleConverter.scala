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
        Some(InventoryData((MakeMountings(obj) ++ MakeTrunk(obj)).sortBy(_.parentSlot)))
      )(SpecificFormatModifier)
    )
  }

  /**
    * na
    * @param obj the `Player` game object
    * @return a list of all tools that were in the mounted weapon slots in decoded packet form
    */
  private def MakeMountings(obj : Vehicle) : List[InventoryItemData.InventoryItem] = {
    obj.Weapons.map({
      case((index, slot)) =>
        val equip : Equipment = slot.Equipment.get
        InventoryItemData(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.ConstructorData(equip).get)
    }).toList
  }

  /**
    * na
    * @param obj the `Player` game object
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeTrunk(obj : Vehicle) : List[InternalSlot] = {
    obj.Trunk.Items.map({
      case(_, item) =>
        val equip : Equipment = item.obj
        InventoryItemData(equip.Definition.ObjectId, equip.GUID, item.start, equip.Definition.Packet.ConstructorData(equip).get)
    }).toList
  }

//  @tailrec private def recursiveMakeSeats(iter : Iterator[(Int, Seat)], list : List[InventoryItemData.InventoryItem] = Nil) : List[InventoryItemData.InventoryItem] = {
//    if(!iter.hasNext) {
//      list
//    }
//    else {
//      val (index, seat) = iter.next
//      seat.Occupant match {
//        case Some(avatar) =>
//          val definition = avatar.Definition
//          recursiveMakeSeats(
//            iter,
//            list :+ InventoryItemData(definition.ObjectId, avatar.GUID, index, definition.Packet.ConstructorData(avatar).get)
//          )
//        case None =>
//          recursiveMakeSeats(iter, list)
//      }
//    }
//  }

  protected def SpecificFormatModifier : VehicleFormat.Value = VehicleFormat.Normal

  protected def SpecificFormatData(obj : Vehicle) : Option[SpecificVehicleData] = None
}
