// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.{BailType, DriveState, Vector3}

object VehicleAction {
  trait Action

  final case class ChildObjectState(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, pitch : Float, yaw : Float) extends Action
  final case class DeployRequest(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, state : DriveState.Value, unk1 : Int, unk2 : Boolean, pos : Vector3) extends Action
  final case class DismountVehicle(player_guid : PlanetSideGUID, bailType : BailType.Value, unk2 : Boolean) extends Action
  final case class EquipmentInSlot(player_guid : PlanetSideGUID, target_guid : PlanetSideGUID, slot : Int, equipment : Equipment) extends Action
  final case class InventoryState(player_guid : PlanetSideGUID, obj : PlanetSideGameObject, parent_guid : PlanetSideGUID, start : Int, con_data : ConstructorData) extends Action
  final case class InventoryState2(player_guid : PlanetSideGUID, obj_guid : PlanetSideGUID, parent_guid : PlanetSideGUID, value : Int) extends Action
  final case class KickPassenger(player_guid : PlanetSideGUID, unk1 : Int, unk2 : Boolean, vehicle_guid : PlanetSideGUID) extends Action
  final case class LoadVehicle(player_guid : PlanetSideGUID, vehicle : Vehicle, vtype : Int, vguid : PlanetSideGUID, vdata : ConstructorData) extends Action
  final case class MountVehicle(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, seat : Int) extends Action
  final case class ObjectDelete(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class Ownership(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID) extends Action
  final case class PlanetsideAttribute(player_guid : PlanetSideGUID, target_guid : PlanetSideGUID, attribute_type : Int, attribute_value : Long) extends Action
  final case class SeatPermissions(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, seat_group : Int, permission : Long) extends Action
  final case class StowEquipment(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class UnloadVehicle(player_guid : PlanetSideGUID, continent : Zone, vehicle : Vehicle, vehicle_guid : PlanetSideGUID) extends Action
  final case class UnstowEquipment(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID) extends Action
  final case class VehicleState(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, unk1 : Int, pos : Vector3, ang : Vector3, vel : Option[Vector3], unk2 : Option[Int], unk3 : Int, unk4 : Int, wheel_direction : Int, unk5 : Boolean, unk6 : Boolean) extends Action
  final case class SendResponse(player_guid: PlanetSideGUID, msg : PlanetSideGamePacket) extends Action
  final case class UpdateAmsSpawnPoint(zone : Zone) extends Action

  final case class TransferPassengerChannel(player_guid : PlanetSideGUID, temp_channel : String, new_channel : String, vehicle : Vehicle) extends Action
  final case class TransferPassenger(player_guid : PlanetSideGUID, temp_channel : String, vehicle : Vehicle, vehicle_to_delete : PlanetSideGUID) extends Action

  final case class KickCargo(player_guid : PlanetSideGUID, cargo : Vehicle, speed : Int) extends Action
}
