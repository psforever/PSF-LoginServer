// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.Vector3

object VehicleResponse {
  trait Response

  final case class Awareness(vehicle_guid : PlanetSideGUID) extends Response
  final case class ChildObjectState(object_guid : PlanetSideGUID, pitch : Float, yaw : Float) extends Response
  final case class DismountVehicle(unk1 : Int, unk2 : Boolean) extends Response
  final case class InventoryState(obj : PlanetSideGameObject, parent_guid : PlanetSideGUID, start : Int, con_data : ConstructorData) extends Response
  final case class KickPassenger(unk1 : Int, unk2 : Boolean, vehicle_guid : PlanetSideGUID) extends Response
  final case class LoadVehicle(vehicle : Vehicle, vtype : Int, vguid : PlanetSideGUID, vdata : ConstructorData) extends Response
  final case class MountVehicle(object_guid : PlanetSideGUID, seat : Int) extends Response
  final case class SeatPermissions(vehicle_guid : PlanetSideGUID, seat_group : Int, permission : Long) extends Response
  final case class StowEquipment(vehicle_guid : PlanetSideGUID, slot : Int, itype : Int, iguid : PlanetSideGUID, idata : ConstructorData) extends Response
  final case class UnloadVehicle(vehicle_guid : PlanetSideGUID) extends Response
  final case class UnstowEquipment(item_guid : PlanetSideGUID) extends Response
  final case class VehicleState(vehicle_guid : PlanetSideGUID, unk1 : Int, pos : Vector3, ang : Vector3, vel : Option[Vector3], unk2 : Option[Int], unk3 : Int, unk4 : Int, wheel_direction : Int, unk5 : Boolean, unk6 : Boolean) extends Response
  final case class PlanetsideAttribute(vehicle_guid : PlanetSideGUID, attribute_type : Int, attribute_value : Long) extends Response
  final case class ProximityTerminalUse(pad_guid : PlanetSideGUID, bool : Boolean) extends Response
}
