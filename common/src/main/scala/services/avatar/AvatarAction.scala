// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.Container
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.packet.game.objectcreate.{ConstructorData, ObjectCreateMessageParent}
import net.psforever.types.ExoSuitType

import scala.concurrent.duration.FiniteDuration

object AvatarAction {
  trait Action

  final case class ArmorChanged(player_guid : PlanetSideGUID, suit : ExoSuitType.Value, subtype : Int) extends Action
  final case class ChangeAmmo(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID, weapon_slot : Int, old_ammo_guid : PlanetSideGUID, ammo_id : Int, ammo_guid : PlanetSideGUID, ammo_data : ConstructorData) extends Action
  final case class ChangeFireMode(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID, mode : Int) extends Action
  final case class ChangeFireState_Start(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class ChangeFireState_Stop(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class ConcealPlayer(player_guid : PlanetSideGUID) extends Action
  final case class DropItem(player_guid : PlanetSideGUID, item : Equipment, zone : Zone) extends Action
  final case class EquipmentInHand(player_guid : PlanetSideGUID, target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class LoadPlayer(player_guid : PlanetSideGUID, object_id : Int, target_guid : PlanetSideGUID, cdata : ConstructorData, pdata : Option[ObjectCreateMessageParent]) extends Action
  final case class ObjectDelete(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID, unk : Int = 0) extends Action
  final case class ObjectHeld(player_guid : PlanetSideGUID, slot : Int) extends Action
  final case class PlanetsideAttribute(player_guid : PlanetSideGUID, attribute_type : Int, attribute_value : Long) extends Action
  final case class PlayerState(player_guid : PlanetSideGUID, msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Action
  final case class PickupItem(player_guid : PlanetSideGUID, zone : Zone, target : PlanetSideGameObject with Container, slot : Int, item : Equipment, unk : Int = 0) extends Action
  final case class Release(player : Player, zone : Zone, time : Option[FiniteDuration] = None) extends Action
  final case class Reload(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
  final case class StowEquipment(player_guid : PlanetSideGUID, target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class WeaponDryFire(player_guid : PlanetSideGUID, weapon_guid : PlanetSideGUID) extends Action
//  final case class PlayerStateShift(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class DestroyDisplay(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class HitHintReturn(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
}
