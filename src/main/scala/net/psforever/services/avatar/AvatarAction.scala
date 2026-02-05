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
import net.psforever.services.base.messages.ObjectDelete
import net.psforever.services.base.{EventMessage, EventResponse, SelfRespondingEvent}
import net.psforever.types.{ExoSuitType, ExperienceType, PlanetSideGUID, TransactionType, Vector3}

import scala.concurrent.duration.FiniteDuration

object AvatarAction {
  final case class ArmorChanged(suit: ExoSuitType.Value, subtype: Int) extends SelfRespondingEvent

  final case class AvatarImplant(action: ImplantAction.Value, implantSlot: Int, status: Int) extends SelfRespondingEvent

  final case class ChangeFireMode(item_guid: PlanetSideGUID, mode: Int) extends SelfRespondingEvent

  final case class ConcealPlayer(player_guid: PlanetSideGUID) extends SelfRespondingEvent

  final case class EnvironmentalDamage(player_guid: PlanetSideGUID, source_guid: PlanetSideGUID, amount: Int) extends SelfRespondingEvent

  final case class DeactivateImplantSlot(player_guid: PlanetSideGUID, slot: Int) extends SelfRespondingEvent

  final case class ActivateImplantSlot(player_guid: PlanetSideGUID, slot: Int) extends SelfRespondingEvent

  final case class Destroy(victim: PlanetSideGUID, killer: PlanetSideGUID, weapon: PlanetSideGUID, pos: Vector3) extends SelfRespondingEvent

  final case class DestroyDisplay(killer: SourceEntry, victim: SourceEntry, method: Int, unk: Int = 121) extends SelfRespondingEvent

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
    }
  }

  final case class Killed(cause: DamageResult, mount_guid: Option[PlanetSideGUID]) extends SelfRespondingEvent

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

  final case class ObjectHeld(slot: Int, previousSLot: Int) extends SelfRespondingEvent

  final case class OxygenState(player: OxygenStateTarget, vehicle: Option[OxygenStateTarget]) extends SelfRespondingEvent

  final case class PlanetsideStringAttribute(attribute_type: Int, attribute_value: String) extends SelfRespondingEvent

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
                              ) extends SelfRespondingEvent

  final case class PickupItem(item: Equipment, unk: Int = 0) extends EventMessage {
    def response(): EventResponse = {
      ObjectDelete(item.GUID, unk)
    }
  }

  final case class ProjectileAutoLockAwareness(mode: Int) extends SelfRespondingEvent

  final case class ProjectileExplodes(projectile_guid: PlanetSideGUID, projectile: Projectile) extends SelfRespondingEvent

  final case class ProjectileState(
                                    projectile_guid: PlanetSideGUID,
                                    shot_pos: Vector3,
                                    shot_vel: Vector3,
                                    shot_orient: Vector3,
                                    sequence: Int,
                                    end: Boolean,
                                    hit_target: PlanetSideGUID
                                  ) extends SelfRespondingEvent

  final case class PutDownFDU(player_guid: PlanetSideGUID) extends SelfRespondingEvent

  final case class ReleasePlayer(player: Player) extends EventResponse

  final case class Release(player: Player, zone: Zone, time: Option[FiniteDuration] = None) extends EventMessage {
    def response(): EventResponse = {
      ReleasePlayer(player)
    }
  }

  final case class Revive(target_guid: PlanetSideGUID) extends SelfRespondingEvent

  final case class StowEquipment(target_guid: PlanetSideGUID, slot: Int, item: Equipment)
    extends SelfRespondingEvent

  final case class SendResponseTargeted(target_guid: PlanetSideGUID, msg: PlanetSideGamePacket) extends SelfRespondingEvent

  final case class TerminalOrderResult(terminal_guid: PlanetSideGUID, action: TransactionType.Value, result: Boolean)
    extends SelfRespondingEvent

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
                                ) extends SelfRespondingEvent

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
                                ) extends SelfRespondingEvent

  final case class DropSpecialItem() extends SelfRespondingEvent

  final case class UseKit(kit_guid: PlanetSideGUID, kit_objid: Int) extends SelfRespondingEvent

  final case class KitNotUsed(kit_guid: PlanetSideGUID, msg: String) extends SelfRespondingEvent

  final case class UpdateKillsDeathsAssists(charId: Long, kda: KDAStat) extends SelfRespondingEvent

  final case class AwardBep(charId: Long, bep: Long, expType: ExperienceType) extends SelfRespondingEvent

  final case class AwardCep(charId: Long, bep: Long) extends SelfRespondingEvent

  final case class FacilityCaptureRewards(building_id: Int, zone_number: Int, exp: Long) extends SelfRespondingEvent

  final case class ShareKillExperienceWithSquad(killer: Player, exp: Long) extends SelfRespondingEvent

  final case class ShareAntExperienceWithSquad(owner: UniquePlayer, exp: Long, vehicle: Vehicle) extends SelfRespondingEvent

  final case class RemoveFromOutfitChat(outfit_id: Long) extends SelfRespondingEvent

  final case class TeardownConnection() extends SelfRespondingEvent
}
