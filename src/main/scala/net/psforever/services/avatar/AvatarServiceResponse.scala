// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.avatar.scoring.KDAStat
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.sourcing.{SourceEntry, UniquePlayer}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.packet.game.{ImplantAction, ObjectCreateMessage}
import net.psforever.types.{ExoSuitType, ExperienceType, PlanetSideEmpire, PlanetSideGUID, TransactionType, Vector3}
import net.psforever.services.base.{EventResponse, GenericEventBusMsg}

final case class AvatarServiceResponse(
    channel: String,
    avatar_guid: PlanetSideGUID,
    replyMessage: AvatarResponse.Response
) extends GenericEventBusMsg

object AvatarResponse {
  sealed trait Response extends EventResponse

  final case class ArmorChanged(suit: ExoSuitType.Value, subtype: Int) extends Response
  final case class AvatarImplant(action: ImplantAction.Value, implantSlot: Int, status: Int) extends Response
  final case class ChangeAmmo(
      weapon_guid: PlanetSideGUID,
      weapon_slot: Int,
      old_ammo_guid: PlanetSideGUID,
      ammo_id: Int,
      ammo_guid: PlanetSideGUID,
      ammo_data: ConstructorData
  )                                                                     extends Response
  final case class ChangeFireMode(item_guid: PlanetSideGUID, mode: Int) extends Response
  final case class ChangeFireState_Start(weapon_guid: PlanetSideGUID)   extends Response
  final case class ChangeFireState_Stop(weapon_guid: PlanetSideGUID)    extends Response
  final case class ConcealPlayer()                                      extends Response
  final case class EnvironmentalDamage(target: PlanetSideGUID, source_guid: PlanetSideGUID, amount: Int)
      extends Response
  final case class Destroy(victim: PlanetSideGUID, killer: PlanetSideGUID, weapon: PlanetSideGUID, pos: Vector3)
      extends Response
  final case class DestroyDisplay(killer: SourceEntry, victim: SourceEntry, method: Int, unk: Int) extends Response
  final case class DropItem(pkt: ObjectCreateMessage)                                              extends Response
  final case class EquipmentInHand(pkt: ObjectCreateMessage)                                       extends Response
  final case class GenericObjectAction(object_guid: PlanetSideGUID, action_code: Int)              extends Response
  final case class HitHint(source_guid: PlanetSideGUID)                                            extends Response
  final case class Killed(cause: DamageResult, mount_guid: Option[PlanetSideGUID])                 extends Response
  final case class LoadPlayer(pkt: ObjectCreateMessage)                                            extends Response
  final case class LoadProjectile(pkt: ObjectCreateMessage)                                        extends Response
  final case class ObjectDelete(item_guid: PlanetSideGUID, unk: Int)                               extends Response
  final case class ObjectHeld(slot: Int, previousSLot: Int)                                        extends Response
  final case class OxygenState(player: OxygenStateTarget, vehicle: Option[OxygenStateTarget])      extends Response
  final case class PlanetsideAttribute(attribute_type: Int, attribute_value: Long)                 extends Response
  final case class PlanetsideAttributeToAll(attribute_type: Int, attribute_value: Long)            extends Response
  final case class PlanetsideAttributeSelf(attribute_type: Int, attribute_value: Long)             extends Response
  final case class PlanetsideStringAttribute(attribute_type: Int, attribute_value: String)         extends Response
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
  )                                                                                            extends Response
  final case class ProjectileAutoLockAwareness(mode: Int)                                      extends Response
  final case class ProjectileExplodes(projectile_guid: PlanetSideGUID, projectile: Projectile) extends Response
  final case class ProjectileState(
      projectile_guid: PlanetSideGUID,
      shot_pos: Vector3,
      shot_vel: Vector3,
      shot_orient: Vector3,
      sequence: Int,
      end: Boolean,
      hit_target: PlanetSideGUID
  )                                                                                        extends Response
  final case class PutDownFDU(target_guid: PlanetSideGUID)                                 extends Response
  final case class Release(player: Player)                                                 extends Response
  final case class Reload(weapon_guid: PlanetSideGUID)                                     extends Response
  final case class Revive(target_guid: PlanetSideGUID)                                     extends Response
  final case class SetEmpire(object_guid: PlanetSideGUID, faction: PlanetSideEmpire.Value) extends Response
  final case class StowEquipment(target_guid: PlanetSideGUID, slot: Int, item: Equipment)  extends Response
  final case class WeaponDryFire(weapon_guid: PlanetSideGUID)                              extends Response

  final case class SendResponse(msg: PlanetSideGamePacket)                                      extends Response
  final case class SendResponseTargeted(target_guid: PlanetSideGUID, msg: PlanetSideGamePacket) extends Response

  final case class TerminalOrderResult(terminal_guid: PlanetSideGUID, action: TransactionType.Value, result: Boolean)
      extends Response
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
  ) extends Response
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
  ) extends Response
  final case class DropSpecialItem() extends Response

  final case class TeardownConnection() extends Response
  //  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
  final case class UseKit(kit_guid: PlanetSideGUID, kit_objid: Int) extends Response
  final case class KitNotUsed(kit_guid: PlanetSideGUID, msg: String) extends Response

  final case class UpdateKillsDeathsAssists(charId: Long, kda: KDAStat) extends Response
  final case class AwardBep(charId: Long, bep: Long, expType: ExperienceType) extends Response
  final case class AwardCep(charId: Long, bep: Long) extends Response
  final case class FacilityCaptureRewards(building_id: Int, zone_number: Int, exp: Long) extends Response
  final case class ShareKillExperienceWithSquad(killer: Player, exp: Long) extends Response
  final case class ShareAntExperienceWithSquad(owner: UniquePlayer, exp: Long, vehicle: Vehicle) extends Response
  final case class RemoveFromOutfitChat(outfit_id: Long) extends Response
}
