// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import net.psforever.objects.Player
import net.psforever.objects.avatar.scoring.KDAStat
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.ImplantAction
import net.psforever.packet.game.objectcreate.{ConstructorData, ObjectCreateMessageParent}
import net.psforever.types.{ExoSuitType, ExperienceType, PlanetSideEmpire, PlanetSideGUID, TransactionType, Vector3}

import scala.concurrent.duration.FiniteDuration

final case class AvatarServiceMessage(forChannel: String, actionMessage: AvatarAction.Action)

object AvatarServiceMessage {
  final case class Corpse(msg: Any)
  final case class Ground(msg: Any)
}

object AvatarAction {
  sealed trait Action

  final case class ArmorChanged(player_guid: PlanetSideGUID, suit: ExoSuitType.Value, subtype: Int) extends Action
  final case class AvatarImplant(player_guid: PlanetSideGUID, action: ImplantAction.Value, implantSlot: Int, status: Int) extends Action
  final case class ChangeAmmo(
      player_guid: PlanetSideGUID,
      weapon_guid: PlanetSideGUID,
      weapon_slot: Int,
      old_ammo_guid: PlanetSideGUID,
      ammo_id: Int,
      ammo_guid: PlanetSideGUID,
      ammo_data: ConstructorData
  )                                                                                                  extends Action
  final case class ChangeFireMode(player_guid: PlanetSideGUID, item_guid: PlanetSideGUID, mode: Int) extends Action
  final case class ChangeFireState_Start(player_guid: PlanetSideGUID, weapon_guid: PlanetSideGUID)   extends Action
  final case class ChangeFireState_Stop(player_guid: PlanetSideGUID, weapon_guid: PlanetSideGUID)    extends Action
  final case class ConcealPlayer(player_guid: PlanetSideGUID)                                        extends Action
  final case class EnvironmentalDamage(player_guid: PlanetSideGUID, source_guid: PlanetSideGUID, amount: Int)
      extends Action
  final case class DeactivateImplantSlot(player_guid: PlanetSideGUID, slot: Int)                       extends Action
  final case class ActivateImplantSlot(player_guid: PlanetSideGUID, slot: Int)                         extends Action
  final case class Destroy(victim: PlanetSideGUID, killer: PlanetSideGUID, weapon: PlanetSideGUID, pos: Vector3)
      extends Action
  final case class DestroyDisplay(killer: SourceEntry, victim: SourceEntry, method: Int, unk: Int = 121) extends Action
  final case class DropItem(player_guid: PlanetSideGUID, item: Equipment)                                extends Action
  final case class EquipmentInHand(player_guid: PlanetSideGUID, target_guid: PlanetSideGUID, slot: Int, item: Equipment)
      extends Action
  final case class GenericObjectAction(player_guid: PlanetSideGUID, object_guid: PlanetSideGUID, action_code: Int)
      extends Action
  final case class HitHint(source_guid: PlanetSideGUID, player_guid: PlanetSideGUID)       extends Action
  final case class Killed(player_guid: PlanetSideGUID, cause: DamageResult, mount_guid: Option[PlanetSideGUID]) extends Action
  final case class LoadPlayer(
      player_guid: PlanetSideGUID,
      object_id: Int,
      target_guid: PlanetSideGUID,
      cdata: ConstructorData,
      pdata: Option[ObjectCreateMessageParent]
  ) extends Action
  final case class LoadProjectile(
      player_guid: PlanetSideGUID,
      object_id: Int,
      projectile_guid: PlanetSideGUID,
      cdata: ConstructorData
  )                                                                                                   extends Action
  final case class ObjectDelete(player_guid: PlanetSideGUID, item_guid: PlanetSideGUID, unk: Int = 0) extends Action
  final case class ObjectHeld(player_guid: PlanetSideGUID, slot: Int, previousSLot: Int)              extends Action
  final case class OxygenState(player: OxygenStateTarget, vehicle: Option[OxygenStateTarget])         extends Action
  final case class PlanetsideAttribute(player_guid: PlanetSideGUID, attribute_type: Int, attribute_value: Long)
      extends Action
  final case class PlanetsideAttributeToAll(player_guid: PlanetSideGUID, attribute_type: Int, attribute_value: Long)
      extends Action
  final case class PlanetsideAttributeSelf(player_guid: PlanetSideGUID, attribute_type: Int, attribute_value: Long)
      extends Action
  final case class PlayerState(
      player_guid: PlanetSideGUID,
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
  )                                                                                       extends Action
  final case class PickupItem(player_guid: PlanetSideGUID, item: Equipment, unk: Int = 0) extends Action
  final case class ProjectileAutoLockAwareness(mode: Int)                                 extends Action
  final case class ProjectileExplodes(
      player_guid: PlanetSideGUID,
      projectile_guid: PlanetSideGUID,
      projectile: Projectile
  ) extends Action
  final case class ProjectileState(
      player_guid: PlanetSideGUID,
      projectile_guid: PlanetSideGUID,
      shot_pos: Vector3,
      shot_vel: Vector3,
      shot_orient: Vector3,
      sequence: Int,
      end: Boolean,
      hit_target: PlanetSideGUID
  )                                                                                         extends Action
  final case class PutDownFDU(player_guid: PlanetSideGUID)                                  extends Action
  final case class Release(player: Player, zone: Zone, time: Option[FiniteDuration] = None) extends Action
  final case class Revive(target_guid: PlanetSideGUID)                                      extends Action
  final case class Reload(player_guid: PlanetSideGUID, weapon_guid: PlanetSideGUID)         extends Action
  final case class SetEmpire(player_guid: PlanetSideGUID, object_guid: PlanetSideGUID, faction: PlanetSideEmpire.Value)
      extends Action
  final case class StowEquipment(player_guid: PlanetSideGUID, target_guid: PlanetSideGUID, slot: Int, item: Equipment)
      extends Action
  final case class WeaponDryFire(player_guid: PlanetSideGUID, weapon_guid: PlanetSideGUID) extends Action

  final case class SendResponse(player_guid: PlanetSideGUID, msg: PlanetSideGamePacket)         extends Action
  final case class SendResponseTargeted(target_guid: PlanetSideGUID, msg: PlanetSideGamePacket) extends Action

  final case class TerminalOrderResult(terminal_guid: PlanetSideGUID, action: TransactionType.Value, result: Boolean)
      extends Action
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
  ) extends Action
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
  ) extends Action
  final case class DropSpecialItem() extends Action
  final case class UseKit(kit_guid: PlanetSideGUID, kit_objid: Int) extends Action
  final case class KitNotUsed(kit_guid: PlanetSideGUID, msg: String) extends Action

  final case class UpdateKillsDeathsAssists(charId: Long, kda: KDAStat) extends Action
  final case class AwardBep(charId: Long, bep: Long, expType: ExperienceType) extends Action
  final case class AwardCep(charId: Long, bep: Long) extends Action
  final case class FacilityCaptureRewards(building_id: Int, zone_number: Int, exp: Long) extends Action
  final case class ShareKillExperienceWithSquad(killer: Player, exp: Long) extends Action

  final case class TeardownConnection() extends Action
  //  final case class PlayerStateShift(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
  //  final case class DestroyDisplay(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
}
