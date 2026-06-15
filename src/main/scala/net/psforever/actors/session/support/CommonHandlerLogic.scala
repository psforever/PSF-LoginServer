// Copyright (c) 2026 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import akka.actor.ActorContext
import net.psforever.objects.Tool
import net.psforever.packet.game.{ChangeAmmoMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, GenericObjectActionMessage, HitHint, ObjectDeleteMessage, PlanetsideAttributeMessage, ReloadMessage, SetEmpireMessage, WeaponDryFireMessage}
import net.psforever.services.base.message.{ChangeAmmo, ChangeFireState_Start, ChangeFireState_Stop, ConcealPlayer, GenericObjectAction, HintsAtAttacker, ObjectDelete, PlanetsideAttribute, ReloadTool, SendResponse, SetEmpire, WeaponDryFire}

class CommonHandlerLogic(val sessionLogic: SessionData, implicit val context: ActorContext)
  extends CommonSessionInterfacingFunctionality with CommonHandlerFunctions {

  def receive: Receive = {
    case PlanetsideAttribute(target_guid, attributeType, attributeValue)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(PlanetsideAttributeMessage(target_guid, attributeType, attributeValue))

    case GenericObjectAction(objectGuid, actionCode)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(GenericObjectActionMessage(objectGuid, actionCode))

    case ObjectDelete(itemGuid, unk)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(ObjectDeleteMessage(itemGuid, unk))

    case ChangeFireState_Start(weaponGuid)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(ChangeFireStateMessage_Start(weaponGuid))

    case ChangeFireState_Stop(weaponGuid)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(ChangeFireStateMessage_Stop(weaponGuid))

    case ReloadTool(itemGuid)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(ReloadMessage(itemGuid, ammo_clip=1, unk1=0))

    case ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data)
      if TestFilter(_ => isNotSameTarget) =>
      sessionLogic.avatarResponse.changeAmmoProcedure(weapon_guid, previous_guid, ammo_id, ammo_guid, weapon_slot, ammo_data)
      sendResponse(ChangeAmmoMessage(weapon_guid, 1))

    case WeaponDryFire(weaponGuid)
      if TestFilter(_ => isNotSameTarget) =>
      continent.GUID(weaponGuid).collect {
        case tool: Tool if tool.Magazine == 0 =>
          sendResponse(WeaponDryFireMessage(weaponGuid))
      }

    case HintsAtAttacker(sourceGuid)
      if TestFilter(_ => { player.isAlive }) =>
      sendResponse(HitHint(sourceGuid, filterGuid))
      sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_dmg")

    case SetEmpire(objectGuid, faction)
      if TestFilter(_ => isNotSameTarget) =>
      sendResponse(SetEmpireMessage(objectGuid, faction))

    case ConcealPlayer(_) =>
      sendResponse(GenericObjectActionMessage(filterGuid, code=9))

    case SendResponse(msgs) =>
      msgs.foreach(sendResponse)
  }
}
