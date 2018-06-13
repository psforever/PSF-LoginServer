// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.ExoSuitType

object AvatarResponse {
  trait Response

  final case class ArmorChanged(suit : ExoSuitType.Value, subtype : Int) extends Response
  final case class ChangeAmmo(weapon_guid : PlanetSideGUID, weapon_slot : Int, old_ammo_guid : PlanetSideGUID, ammo_id : Int, ammo_guid : PlanetSideGUID, ammo_data : ConstructorData) extends Response
  final case class ChangeFireMode(item_guid : PlanetSideGUID, mode : Int) extends Response
  final case class ChangeFireState_Start(weapon_guid : PlanetSideGUID) extends Response
  final case class ChangeFireState_Stop(weapon_guid : PlanetSideGUID) extends Response
  final case class ConcealPlayer() extends Response
  final case class EquipmentInHand(pkt : ObjectCreateMessage) extends Response
  final case class DropItem(pkt : ObjectCreateMessage) extends Response
  final case class LoadPlayer(pkt : ObjectCreateMessage) extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ObjectHeld(slot : Int) extends Response
  final case class PlanetsideAttribute(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlayerState(msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Response
  final case class Release(player : Player) extends Response
  final case class Reload(weapon_guid : PlanetSideGUID) extends Response
  final case class StowEquipment(target_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Response
  final case class WeaponDryFire(weapon_guid : PlanetSideGUID) extends Response

  final case class SendResponse(msg: PlanetSideGamePacket) extends Response
  //  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
  //  final case class DestroyDisplay(itemID : PlanetSideGUID) extends Response
  //  final case class HitHintReturn(itemID : PlanetSideGUID) extends Response
}
