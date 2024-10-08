// Copyright (c) 2017 PSForever
package net.psforever.services.vehicle

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.pad.VehicleSpawnPad.Reminders
import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.types.{BailType, DriveState, PlanetSideGUID, Vector3}
import net.psforever.services.GenericEventBusMsg

final case class VehicleServiceResponse(
    channel: String,
    avatar_guid: PlanetSideGUID,
    replyMessage: VehicleResponse.Response
) extends GenericEventBusMsg

object VehicleResponse {
  trait Response

  final case class ChangeAmmo(
                               weapon_guid: PlanetSideGUID,
                               weapon_slot: Int,
                               old_ammo_guid: PlanetSideGUID,
                               ammo_id: Int,
                               ammo_guid: PlanetSideGUID,
                               ammo_data: ConstructorData
                             )                                                             extends Response
  final case class ChangeFireState_Start(weapon_guid: PlanetSideGUID)   extends Response
  final case class ChangeFireState_Stop(weapon_guid: PlanetSideGUID)    extends Response
  final case class ChildObjectState(object_guid: PlanetSideGUID, pitch: Float, yaw: Float) extends Response
  final case class ConcealPlayer(player_guid: PlanetSideGUID)                              extends Response
  final case class DeployRequest(
      object_guid: PlanetSideGUID,
      state: DriveState.Value,
      unk1: Int,
      unk2: Boolean,
      pos: Vector3
  )                                                                         extends Response
  final case class DismountVehicle(bailType: BailType.Value, unk2: Boolean) extends Response
  final case class EquipmentInSlot(pkt: ObjectCreateMessage)                extends Response
  final case class FrameVehicleState(
      vehicle_guid: PlanetSideGUID,
      unk1: Int,
      pos: Vector3,
      orient: Vector3,
      vel: Option[Vector3],
      unk2: Boolean,
      unk3: Int,
      unk4: Int,
      is_crouched: Boolean,
      unk6: Boolean,
      unk7: Boolean,
      unk8: Int,
      unk9: Long,
      unkA: Long
  ) extends Response
  final case class GenericObjectAction(guid: PlanetSideGUID, action: Int)   extends Response
  final case class HitHint(source_guid: PlanetSideGUID)                     extends Response
  final case class InventoryState(
      obj: PlanetSideGameObject,
      parent_guid: PlanetSideGUID,
      start: Int,
      con_data: ConstructorData
  )                                                                                                    extends Response
  final case class InventoryState2(obj_guid: PlanetSideGUID, parent_guid: PlanetSideGUID, value: Int)  extends Response
  final case class KickPassenger(seat_num: Int, kickedByDriver: Boolean, vehicle_guid: PlanetSideGUID) extends Response
  final case class LoadVehicle(vehicle: Vehicle, vtype: Int, vguid: PlanetSideGUID, vdata: ConstructorData)
      extends Response
  final case class MountVehicle(object_guid: PlanetSideGUID, seat: Int) extends Response
  final case class ObjectDelete(guid: PlanetSideGUID)                   extends Response
  final case class Ownership(vehicle_guid: PlanetSideGUID)              extends Response
  final case class LoseOwnership(owner_guid: PlanetSideGUID, vehicle_guid: PlanetSideGUID)
      extends Response
  final case class PlanetsideAttribute(vehicle_guid: PlanetSideGUID, attribute_type: Int, attribute_value: Long)
      extends Response
  final case class Reload(weapon_guid: PlanetSideGUID)                                              extends Response
  final case class RevealPlayer(player_guid: PlanetSideGUID)                                        extends Response
  final case class SeatPermissions(vehicle_guid: PlanetSideGUID, seat_group: Int, permission: Long) extends Response
  final case class StowEquipment(
      vehicle_guid: PlanetSideGUID,
      slot: Int,
      itype: Int,
      iguid: PlanetSideGUID,
      idata: ConstructorData
  )                                                                              extends Response
  final case class WeaponDryFire(weapon_guid: PlanetSideGUID)                    extends Response
  final case class UnloadVehicle(vehicle: Vehicle, vehicle_guid: PlanetSideGUID) extends Response
  final case class UnstowEquipment(item_guid: PlanetSideGUID)                    extends Response
  final case class VehicleState(
      vehicle_guid: PlanetSideGUID,
      unk1: Int,
      pos: Vector3,
      ang: Vector3,
      vel: Option[Vector3],
      unk2: Option[Int],
      unk3: Int,
      unk4: Int,
      wheel_direction: Int,
      unk5: Boolean,
      unk6: Boolean
  )                                                           extends Response
  final case class SendResponse(msg: PlanetSideGamePacket)    extends Response
  final case class UpdateAmsSpawnPoint(list: List[SpawnTube]) extends Response

  final case class AttachToRails(vehicle_guid: PlanetSideGUID, rails_guid: PlanetSideGUID) extends Response
  final case class StartPlayerSeatedInVehicle(vehicle: Vehicle, pad: VehicleSpawnPad)      extends Response
  final case class PlayerSeatedInVehicle(vehicle: Vehicle, pad: VehicleSpawnPad)           extends Response
  final case class DetachFromRails(
      vehicle_guid: PlanetSideGUID,
      rails_guid: PlanetSideGUID,
      rails_pos: Vector3,
      rails_rot: Float
  )                                                                                    extends Response
  final case class ServerVehicleOverrideStart(vehicle: Vehicle, pad: VehicleSpawnPad)  extends Response
  final case class ServerVehicleOverrideEnd(vehicle: Vehicle, pad: VehicleSpawnPad)    extends Response
  final case class ResetSpawnPad(pad_guid: PlanetSideGUID)                             extends Response
  final case class PeriodicReminder(reason: Reminders.Value, data: Option[Any] = None) extends Response

  final case class TransferPassengerChannel(
      old_channel: String,
      temp_channel: String,
      vehicle: Vehicle,
      vehicle_to_delete: PlanetSideGUID
  ) extends Response

  final case class KickCargo(cargo: Vehicle, speed: Int, delay: Long) extends Response

  final case class ChangeLoadout(
      target_guid: PlanetSideGUID,
      removed_weapons: List[(Equipment, PlanetSideGUID)],
      new_weapons: List[InventoryItem],
      old_inventory: List[(Equipment, PlanetSideGUID)],
      new_inventory: List[InventoryItem]
  ) extends Response
}
