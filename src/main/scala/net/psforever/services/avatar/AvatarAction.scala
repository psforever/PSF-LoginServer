// Copyright (c) 2017-2026 PSForever
package net.psforever.services.avatar

import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.avatar.scoring.KDAStat
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.sourcing.{SourceEntry, UniquePlayer}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ImplantAction, ObjectCreateMessage}
import net.psforever.packet.game.objectcreate.{ConstructorData, DroppedItemData, ObjectCreateMessageParent, PlacementData}
import net.psforever.services.base.{EventMessage, EventResponse, SelfResponseMessage}
import net.psforever.types.{ExoSuitType, ExperienceType, PlanetSideEmpire, PlanetSideGUID, TransactionType, Vector3}

import scala.concurrent.duration.FiniteDuration

object AvatarAction {
  final case class ArmorChanged(suit: ExoSuitType.Value, subtype: Int) extends SelfResponseMessage

  final case class AvatarImplant(action: ImplantAction.Value, implantSlot: Int, status: Int) extends SelfResponseMessage

  final case class ChangeAmmo(
                               weapon_guid: PlanetSideGUID,
                               weapon_slot: Int,
                               old_ammo_guid: PlanetSideGUID,
                               ammo_id: Int,
                               ammo_guid: PlanetSideGUID,
                               ammo_data: ConstructorData
                             ) extends SelfResponseMessage

  final case class ChangeFireMode(item_guid: PlanetSideGUID, mode: Int) extends SelfResponseMessage

  final case class ChangeFireState_Start(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class ChangeFireState_Stop(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class ConcealPlayer(player_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class EnvironmentalDamage(player_guid: PlanetSideGUID, source_guid: PlanetSideGUID, amount: Int) extends SelfResponseMessage

  final case class DeactivateImplantSlot(player_guid: PlanetSideGUID, slot: Int) extends SelfResponseMessage

  final case class ActivateImplantSlot(player_guid: PlanetSideGUID, slot: Int) extends SelfResponseMessage

  final case class Destroy(victim: PlanetSideGUID, killer: PlanetSideGUID, weapon: PlanetSideGUID, pos: Vector3) extends SelfResponseMessage

  final case class DestroyDisplay(killer: SourceEntry, victim: SourceEntry, method: Int, unk: Int = 121) extends SelfResponseMessage

  final case class DropCreatedItem(packet: ObjectCreateMessage) extends EventResponse

  final case class DropItem(item: Equipment) extends EventMessage {
    def response(): EventResponse = {
      val definition = item.Definition
      val objectData = DroppedItemData(
        PlacementData(item.Position, item.Orientation),
        definition.Packet.ConstructorData(item).get
      )
      AvatarAction.DropCreatedItem(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
    }
  }

  final case class EquipmentCreatedInHand(packet: ObjectCreateMessage) extends EventResponse

  final case class EquipmentInHand(target_guid: PlanetSideGUID, slot: Int, item: Equipment) extends EventMessage {
    def response(): EventResponse = {
      val definition    = item.Definition
      val containerData = ObjectCreateMessageParent(target_guid, slot)
      val objectData    = definition.Packet.ConstructorData(item).get
      AvatarAction.EquipmentCreatedInHand(
        ObjectCreateMessage(definition.ObjectId, item.GUID, containerData, objectData)
      )

      AvatarAction.DropCreatedItem(
        ObjectCreateMessage(definition.ObjectId, item.GUID, containerData, objectData)
      )
    }
  }

  final case class GenericObjectAction(object_guid: PlanetSideGUID, action_code: Int) extends SelfResponseMessage

  final case class HitHint(source_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class Killed(cause: DamageResult, mount_guid: Option[PlanetSideGUID]) extends SelfResponseMessage

  final case class LoadCreatedPlayer(pkt: ObjectCreateMessage) extends EventResponse

  final case class LoadPlayer(
                               object_id: Int,
                               target_guid: PlanetSideGUID,
                               cdata: ConstructorData,
                               pdata: Option[ObjectCreateMessageParent]
                             ) extends EventMessage {
    def response(): EventResponse = {
      val pkt = pdata match {
        case Some(data) =>
          ObjectCreateMessage(object_id, target_guid, data, cdata)
        case None =>
          ObjectCreateMessage(object_id, target_guid, cdata)
      }
      LoadCreatedPlayer(pkt)
    }
  }

  final case class LoadCreatedProjectile(pkt: ObjectCreateMessage) extends EventResponse

  final case class LoadProjectile(
                                   object_id: Int,
                                   projectile_guid: PlanetSideGUID,
                                   cdata: ConstructorData
                                 ) extends EventMessage {
    def response(): EventResponse = {
      LoadCreatedProjectile(ObjectCreateMessage(object_id, projectile_guid, cdata))
    }
  }

  final case class ObjectDelete(item_guid: PlanetSideGUID, unk: Int = 0) extends SelfResponseMessage

  final case class ObjectHeld(slot: Int, previousSLot: Int) extends SelfResponseMessage

  final case class OxygenState(player: OxygenStateTarget, vehicle: Option[OxygenStateTarget]) extends SelfResponseMessage

  final case class PlanetsideAttribute(attribute_type: Int, attribute_value: Long) extends SelfResponseMessage

  final case class PlanetsideAttributeToAll(attribute_type: Int, attribute_value: Long) extends SelfResponseMessage

  final case class PlanetsideAttributeSelf(attribute_type: Int, attribute_value: Long) extends SelfResponseMessage

  final case class PlanetsideStringAttribute(attribute_type: Int, attribute_value: String) extends SelfResponseMessage

  final case class PlayerState(
                                pos: Vector3,
                                vel: Option[Vector3],
                                facingYaw: Float,
                                facingPitch: Float,
                                facingYawUpper: Float,
                                timestamp: Int,
                                is_crouching: Boolean,
                                is_jumping: Boolean,
                                jump_thrust: Boolean,
                                is_cloaked: Boolean,
                                spectator: Boolean,
                                weaponInHand: Boolean
                              ) extends SelfResponseMessage

  final case class PickupItem(item: Equipment, unk: Int = 0) extends EventMessage {
    def response(): EventResponse = {
      ObjectDelete(item.GUID, unk)
    }
  }

  final case class ProjectileAutoLockAwareness(mode: Int) extends SelfResponseMessage

  final case class ProjectileExplodes(projectile_guid: PlanetSideGUID, projectile: Projectile) extends SelfResponseMessage

  final case class ProjectileState(
                                    projectile_guid: PlanetSideGUID,
                                    shot_pos: Vector3,
                                    shot_vel: Vector3,
                                    shot_orient: Vector3,
                                    sequence: Int,
                                    end: Boolean,
                                    hit_target: PlanetSideGUID
                                  ) extends SelfResponseMessage

  final case class PutDownFDU(player_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class ReleasePlayer(player: Player) extends EventResponse

  final case class Release(player: Player, zone: Zone, time: Option[FiniteDuration] = None) extends EventMessage {
    def response(): EventResponse = {
      ReleasePlayer(player)
    }
  }

  final case class Reload(weapon_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class Revive(target_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class SetEmpire(object_guid: PlanetSideGUID, faction: PlanetSideEmpire.Value)
    extends SelfResponseMessage

  final case class StowEquipment(target_guid: PlanetSideGUID, slot: Int, item: Equipment)
    extends SelfResponseMessage

  final case class WeaponDryFire(weapon_guid: PlanetSideGUID) extends SelfResponseMessage


  final case class SendResponse(msg: PlanetSideGamePacket)         extends SelfResponseMessage

  final case class SendResponseTargeted(target_guid: PlanetSideGUID, msg: PlanetSideGamePacket) extends SelfResponseMessage

  final case class TerminalOrderResult(terminal_guid: PlanetSideGUID, action: TransactionType.Value, result: Boolean)
    extends SelfResponseMessage

  final case class ChangeExosuit(
                                  target_guid: PlanetSideGUID,
                                  armor: Int,
                                  exosuit: ExoSuitType.Value,
                                  subtype: Int,
                                  last_drawn_slot: Int,
                                  new_max_hand: Boolean,
                                  old_holsters: List[(Equipment, PlanetSideGUID)],
                                  holsters: List[InventoryItem],
                                  old_inventory: List[(Equipment, PlanetSideGUID)],
                                  inventory: List[InventoryItem],
                                  drop: List[InventoryItem],
                                  delete: List[(Equipment, PlanetSideGUID)]
                                ) extends SelfResponseMessage

  final case class ChangeLoadout(
                                  target_guid: PlanetSideGUID,
                                  armor: Int,
                                  exosuit: ExoSuitType.Value,
                                  subtype: Int,
                                  last_drawn_slot: Int,
                                  new_max_hand: Boolean,
                                  old_holsters: List[(Equipment, PlanetSideGUID)],
                                  holsters: List[InventoryItem],
                                  old_inventory: List[(Equipment, PlanetSideGUID)],
                                  inventory: List[InventoryItem],
                                  drop: List[InventoryItem]
                                ) extends SelfResponseMessage

  final case class DropSpecialItem() extends SelfResponseMessage

  final case class UseKit(kit_guid: PlanetSideGUID, kit_objid: Int) extends SelfResponseMessage

  final case class KitNotUsed(kit_guid: PlanetSideGUID, msg: String) extends SelfResponseMessage

  final case class UpdateKillsDeathsAssists(charId: Long, kda: KDAStat) extends SelfResponseMessage

  final case class AwardBep(charId: Long, bep: Long, expType: ExperienceType) extends SelfResponseMessage

  final case class AwardCep(charId: Long, bep: Long) extends SelfResponseMessage

  final case class FacilityCaptureRewards(building_id: Int, zone_number: Int, exp: Long) extends SelfResponseMessage

  final case class ShareKillExperienceWithSquad(killer: Player, exp: Long) extends SelfResponseMessage

  final case class ShareAntExperienceWithSquad(owner: UniquePlayer, exp: Long, vehicle: Vehicle) extends SelfResponseMessage

  final case class RemoveFromOutfitChat(outfit_id: Long) extends SelfResponseMessage

  final case class TeardownConnection() extends SelfResponseMessage
}
