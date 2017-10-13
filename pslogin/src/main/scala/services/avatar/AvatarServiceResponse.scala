// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.{ExoSuitType, Vector3}
import services.GenericEventBusMsg

final case class AvatarServiceResponse(toChannel : String,
                                       avatar_guid : PlanetSideGUID,
                                       replyMessage : AvatarServiceResponse.Response
                                      ) extends GenericEventBusMsg

object AvatarServiceResponse {
  trait Response

  final case class ArmorChanged(suit : ExoSuitType.Value, subtype : Int) extends Response
  //final case class DropItem(pos : Vector3, orient : Vector3, item : PlanetSideGUID) extends Response
  final case class EquipmentInHand(slot : Int, item : Equipment) extends Response
  final case class EquipmentOnGround(pos : Vector3, orient : Vector3, item : Equipment) extends Response
  final case class LoadPlayer(pdata : ConstructorData) extends Response
//  final case class unLoadMap() extends Response
//  final case class LoadMap() extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ObjectHeld(slot : Int) extends Response
  final case class PlanetSideAttribute(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlayerState(msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Response
  final case class Reload(mag : Int) extends Response
//  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
//  final case class DestroyDisplay(itemID : PlanetSideGUID) extends Response
//  final case class HitHintReturn(itemID : PlanetSideGUID) extends Response
//  final case class ChangeWeapon(facingYaw : Int) extends Response
}