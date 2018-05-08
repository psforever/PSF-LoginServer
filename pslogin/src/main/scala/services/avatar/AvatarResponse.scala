// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.{ExoSuitType, Vector3}

object AvatarResponse {
  trait Response

  final case class ArmorChanged(suit : ExoSuitType.Value, subtype : Int) extends Response
  final case class ChangeAmmo(weapon_guid : PlanetSideGUID, weapon_slot : Int, old_ammo_guid : PlanetSideGUID, ammo_id : Int, ammo_guid : PlanetSideGUID, ammo_data : ConstructorData) extends Response
  final case class ChangeFireMode(item_guid : PlanetSideGUID, mode : Int) extends Response
  final case class ChangeFireState_Start(weapon_guid : PlanetSideGUID) extends Response
  final case class ChangeFireState_Stop(weapon_guid : PlanetSideGUID) extends Response
  final case class ConcealPlayer() extends Response
  final case class EquipmentInHand(target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Response
  final case class EquipmentOnGround(pos : Vector3, orient : Vector3, item_id : Int, item_guid : PlanetSideGUID, item_data : ConstructorData) extends Response
  final case class LoadPlayer(pdata : ConstructorData) extends Response
  //  final case class unLoadMap() extends Response
  //  final case class LoadMap() extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ObjectHeld(slot : Int) extends Response
  final case class PlanetsideAttribute(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlanetsideAttributeSelf(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlayerState(msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Response
  final case class Release(player : Player) extends Response
  final case class Reload(weapon_guid : PlanetSideGUID) extends Response
  final case class StowEquipment(target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Response
  final case class WeaponDryFire(weapon_guid : PlanetSideGUID) extends Response
  //  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
  final case class DestroyDisplay(killer : Player, victim : Player) extends Response
  final case class Destroy(victim : PlanetSideGUID, killer : PlanetSideGUID, weapon : PlanetSideGUID, pos : Vector3) extends Response
  //  final case class HitHintReturn(itemID : PlanetSideGUID) extends Response
  //  final case class ChangeWeapon(facingYaw : Int) extends Response
}
