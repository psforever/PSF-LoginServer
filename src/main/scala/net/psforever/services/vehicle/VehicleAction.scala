// Copyright (c) 2017-2026 PSForever
package net.psforever.services.vehicle

import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.{ConstructorData, ObjectCreateMessageParent}
import net.psforever.services.base.{EventMessage, EventResponse, SelfResponseMessage}
import net.psforever.types.{BailType, DriveState, PlanetSideGUID, Vector3}

object VehicleAction {
  trait Action extends EventMessage {
    def response(): EventResponse = null
  }

  final case class ChangeAmmo(
                               weapon_guid: PlanetSideGUID,
                               weapon_slot: Int,
                               old_ammo_guid: PlanetSideGUID,
                               ammo_id: Int,
                               ammo_guid: PlanetSideGUID,
                               ammo_data: ConstructorData
                             ) extends SelfResponseMessage

  final case class ChangeFireState_Start(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class ChangeFireState_Stop(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class ChildObjectState(object_guid: PlanetSideGUID, pitch: Float, yaw: Float) extends SelfResponseMessage

  final case class ConcealPlayer(player_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class DeployRequest(
                                  object_guid: PlanetSideGUID,
                                  state: DriveState.Value,
                                  unk1: Int,
                                  unk2: Boolean,
                                  pos: Vector3
                                ) extends SelfResponseMessage

  final case class DismountVehicle(bailType: BailType.Value, unk2: Boolean) extends SelfResponseMessage

  final case class EquipmentCreatedInSlot(pkt: ObjectCreateMessage) extends EventResponse

  final case class EquipmentInSlot(target_guid: PlanetSideGUID, slot: Int, equipment: Equipment) extends EventMessage {
    override def response(): EventResponse = {
      val definition = equipment.Definition
      val pkt = ObjectCreateMessage(
        definition.ObjectId,
        equipment.GUID,
        ObjectCreateMessageParent(target_guid, slot),
        definition.Packet.ConstructorData(equipment).get
      )
      ObjectCreateMessageParent(target_guid, slot)
      VehicleAction.EquipmentCreatedInSlot(pkt)
    }
  }

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
                                    ) extends SelfResponseMessage

  final case class GenericObjectAction(guid: PlanetSideGUID, action: Int) extends SelfResponseMessage

  final case class HitHint(source_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class InventoryState(
                                   obj: PlanetSideGameObject,
                                   parent_guid: PlanetSideGUID,
                                   start: Int,
                                   con_data: ConstructorData
                                 ) extends SelfResponseMessage

  final case class InventoryState2(obj_guid: PlanetSideGUID, parent_guid: PlanetSideGUID, value: Int) extends SelfResponseMessage

  final case class KickPassenger(unk1: Int, unk2: Boolean, vehicle_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class LoadVehicle(
                                vehicle: Vehicle,
                                vtype: Int,
                                vguid: PlanetSideGUID,
                                vdata: ConstructorData
                              ) extends SelfResponseMessage

  final case class MountVehicle(object_guid: PlanetSideGUID, seat: Int) extends SelfResponseMessage

  final case class ObjectDelete(guid: PlanetSideGUID) extends SelfResponseMessage

  final case class Ownership(vehicle_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class LoseOwnership(owner_guid: PlanetSideGUID, vehicle_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class PlanetsideAttribute(
                                        target_guid: PlanetSideGUID,
                                        attribute_type: Int,
                                        attribute_value: Long
                                      ) extends SelfResponseMessage

  final case class Reload(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class RevealPlayer(player_guid: PlanetSideGUID) extends EventResponse

  final case class SeatPermissions(vehicle_guid: PlanetSideGUID, seat_group: Int, permission: Long) extends SelfResponseMessage

  final case class StowCreatedEquipment(
                                         vehicle_guid: PlanetSideGUID,
                                         slot: Int,
                                         itype: Int,
                                         iguid: PlanetSideGUID,
                                         idata: ConstructorData
                                       ) extends EventResponse

  final case class StowEquipment(vehicle_guid: PlanetSideGUID, slot: Int, item: Equipment) extends SelfResponseMessage

  final case class UnloadVehicle(vehicle: Vehicle, vehicle_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class UnstowEquipment(item_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class WeaponDryFire(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

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
                               ) extends SelfResponseMessage

  final case class SendResponse(msg: PlanetSideGamePacket) extends SelfResponseMessage

  final case class UpdateAmsSpawnList(list: List[SpawnTube]) extends EventResponse

  final case class UpdateAmsSpawnPoint(zone: Zone) extends EventMessage {
    override def response(): EventResponse = {
      VehicleAction.UpdateAmsSpawnList(AmsSpawnPoints(zone))
    }
  }

  final case class AMSDeploymentChange(zone: Zone) extends EventMessage {
    override def response(): EventResponse = {
      VehicleAction.UpdateAmsSpawnList(AmsSpawnPoints(zone))
    }
  }

  final case class TransferPassengerChannel(
                                             temp_channel: String,
                                             new_channel: String,
                                             vehicle: Vehicle,
                                             vehicle_to_delete: PlanetSideGUID
                                           ) extends SelfResponseMessage

  final case class KickCargo(cargo: Vehicle, speed: Int, delay: Long) extends SelfResponseMessage

  final case class ChangeLoadout(
                                  target_guid: PlanetSideGUID,
                                  removed_weapons: List[(Equipment, PlanetSideGUID)],
                                  new_weapons: List[InventoryItem],
                                  old_inventory: List[(Equipment, PlanetSideGUID)],
                                  new_inventory: List[InventoryItem]
                                ) extends SelfResponseMessage

  import net.psforever.objects.serverobject.tube.SpawnTube
  private def AmsSpawnPoints(zone: Zone): List[SpawnTube] = {
    import net.psforever.objects.vehicles.UtilityType
    import net.psforever.objects.GlobalDefinitions
    zone.Vehicles
      .filter(veh =>
        veh.Health > 0 && veh.Definition == GlobalDefinitions.ams && veh.DeploymentState == DriveState.Deployed
      )
      .flatMap(veh => veh.Utilities.values.filter(util => util.UtilType == UtilityType.ams_respawn_tube))
      .map(util => util().asInstanceOf[SpawnTube])
  }
}
