// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.Vector3

object VehicleAction {
  trait Action

  final case class Awareness(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID) extends Action
  final case class ChildObjectState(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, pitch : Float, yaw : Float) extends Action
  final case class DismountVehicle(player_guid : PlanetSideGUID, unk1 : Int, unk2 : Boolean) extends Action
  final case class InventoryState(player_guid : PlanetSideGUID, obj : PlanetSideGameObject, parent_guid : PlanetSideGUID, start : Int, con_data : ConstructorData) extends Action
  final case class KickPassenger(player_guid : PlanetSideGUID, unk1 : Int, unk2 : Boolean, vehicle_guid : PlanetSideGUID) extends Action
  final case class LoadVehicle(player_guid : PlanetSideGUID, vehicle : Vehicle, vtype : Int, vguid : PlanetSideGUID, vdata : ConstructorData) extends Action
  final case class MountVehicle(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, seat : Int) extends Action
  final case class SeatPermissions(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, seat_group : Int, permission : Long) extends Action
  final case class StowEquipment(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class UnloadVehicle(player_guid : PlanetSideGUID, continent : Zone, vehicle : Vehicle) extends Action
  final case class UnstowEquipment(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID) extends Action
  final case class VehicleState(player_guid : PlanetSideGUID, vehicle_guid : PlanetSideGUID, unk1 : Int, pos : Vector3, ang : Vector3, vel : Option[Vector3], unk2 : Option[Int], unk3 : Int, unk4 : Int, wheel_direction : Int, unk5 : Boolean, unk6 : Boolean) extends Action
}
