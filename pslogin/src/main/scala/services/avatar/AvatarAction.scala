// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.{ExoSuitType, Vector3}

object AvatarAction {
  trait Action

  final case class ArmorChanged(player_guid : PlanetSideGUID, suit : ExoSuitType.Value, subtype : Int) extends Action
  final case class ChangeAmmo(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID, weapon_slot : Int, ammo_id : Int, ammo_guid : PlanetSideGUID, ammo_data : ConstructorData) extends Action
  final case class ChangeFireState_Start(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class ChangeFireState_Stop(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class ConcealPlayer(player_guid : PlanetSideGUID) extends Action
  //final case class DropItem(pos : Vector3, orient : Vector3, item : PlanetSideGUID) extends Action
  final case class EquipmentInHand(player_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class EquipmentOnGround(player_guid : PlanetSideGUID, pos : Vector3, orient : Vector3, item_id : Int, item_guid : PlanetSideGUID, item_data : ConstructorData) extends Action
  final case class LoadPlayer(player_guid : PlanetSideGUID, pdata : ConstructorData) extends Action
//  final case class LoadMap(msg : PlanetSideGUID) extends Action
//  final case class unLoadMap(msg : PlanetSideGUID) extends Action
  final case class ObjectDelete(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID, unk : Int = 0) extends Action
  final case class ObjectHeld(player_guid : PlanetSideGUID, slot : Int) extends Action
  final case class PlanetsideAttribute(player_guid : PlanetSideGUID, attribute_type : Int, attribute_value : Long) extends Action
  final case class PlayerState(player_guid : PlanetSideGUID, msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Action
  final case class Reload(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class WeaponDryFire(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
//  final case class PlayerStateShift(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class DestroyDisplay(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class HitHintReturn(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class ChangeWeapon(unk1 : Int, sessionId : Long) extends Action
}
