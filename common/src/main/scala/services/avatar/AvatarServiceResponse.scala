// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.Player
import net.psforever.objects.ballistics.{Projectile, SourceEntry}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.types.{ExoSuitType, PlanetSideEmpire, Vector3}
import services.GenericEventBusMsg

final case class AvatarServiceResponse(toChannel : String,
                                       avatar_guid : PlanetSideGUID,
                                       replyMessage : AvatarResponse.Response
                                      ) extends GenericEventBusMsg

object AvatarResponse {
  sealed trait Response

  final case class ArmorChanged(suit : ExoSuitType.Value, subtype : Int) extends Response
  final case class ChangeAmmo(weapon_guid : PlanetSideGUID, weapon_slot : Int, old_ammo_guid : PlanetSideGUID, ammo_id : Int, ammo_guid : PlanetSideGUID, ammo_data : ConstructorData) extends Response
  final case class ChangeFireMode(item_guid : PlanetSideGUID, mode : Int) extends Response
  final case class ChangeFireState_Start(weapon_guid : PlanetSideGUID) extends Response
  final case class ChangeFireState_Stop(weapon_guid : PlanetSideGUID) extends Response
  final case class ConcealPlayer() extends Response
  final case class EnvironmentalDamage(target : PlanetSideGUID, amount : Int) extends Response
  final case class DamageResolution(target : Player, resolution_function : ResolutionCalculations.Output) extends Response
  final case class Destroy(victim : PlanetSideGUID, killer : PlanetSideGUID, weapon : PlanetSideGUID, pos : Vector3) extends Response
  final case class DestroyDisplay(killer : SourceEntry, victim : SourceEntry, method : Int, unk : Int) extends Response
  final case class DropItem(pkt : ObjectCreateMessage) extends Response
  final case class EquipmentInHand(pkt : ObjectCreateMessage) extends Response
  final case class GenericObjectAction(object_guid : PlanetSideGUID, action_code : Int) extends Response
  final case class HitHint(source_guid : PlanetSideGUID) extends Response
  final case class KilledWhileInVehicle() extends Response
  final case class LoadPlayer(pkt : ObjectCreateMessage) extends Response
  final case class LoadProjectile(pkt : ObjectCreateMessage) extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ObjectHeld(slot : Int) extends Response
  final case class PlanetsideAttribute(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlanetsideAttributeToAll(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlanetsideAttributeSelf(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlayerState(pos : Vector3, vel : Option[Vector3], facingYaw : Float, facingPitch : Float, facingYawUpper : Float, timestamp : Int, is_crouching : Boolean, is_jumping : Boolean, jump_thrust : Boolean, is_cloaked : Boolean, spectator : Boolean, weaponInHand : Boolean) extends Response
  final case class ProjectileAutoLockAwareness(mode : Int) extends Response
  final case class ProjectileExplodes(projectile_guid : PlanetSideGUID, projectile : Projectile) extends Response
  final case class ProjectileState(projectile_guid : PlanetSideGUID, shot_pos : Vector3, shot_vel : Vector3, shot_orient : Vector3, sequence : Int, end : Boolean, hit_target : PlanetSideGUID) extends Response
  final case class PutDownFDU(target_guid : PlanetSideGUID) extends Response
  final case class Release(player : Player) extends Response
  final case class Reload(weapon_guid : PlanetSideGUID) extends Response
  final case class Revive(target_guid: PlanetSideGUID) extends Response
  final case class SetEmpire(object_guid : PlanetSideGUID, faction : PlanetSideEmpire.Value) extends Response
  final case class StowEquipment(target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Response
  final case class WeaponDryFire(weapon_guid : PlanetSideGUID) extends Response

  final case class SendResponse(msg: PlanetSideGamePacket) extends Response
  final case class SendResponseTargeted(target_guid : PlanetSideGUID, msg: PlanetSideGamePacket) extends Response
  //  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
}
