// Copyright (c) 2017 PSForever
package services.avatar

import akka.actor.Actor
import services.{GenericEventBus, Service}

class AvatarService extends Actor {
  //import AvatarServiceResponse._
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
  }

  val AvatarEvents = new GenericEventBus[AvatarServiceResponse] //AvatarEventBus

  def receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/Avatar"
      val who = sender()

      log.info(s"$who has joined $path")

      AvatarEvents.subscribe(who, path)
    case Service.Leave() =>
      AvatarEvents.unsubscribe(sender())
    case Service.LeaveAll() =>
      AvatarEvents.unsubscribe(sender())

    case AvatarServiceMessage(forChannel, action) =>
      action match {
        case AvatarAction.ArmorChanged(player_guid, suit, subtype) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ArmorChanged(suit, subtype))
          )
        case AvatarAction.ConcealPlayer(player_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ConcealPlayer())
          )
        case AvatarAction.EquipmentInHand(player_guid, slot, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.EquipmentInHand(slot, obj))
          )
        case AvatarAction.EquipmentOnGround(player_guid, pos, orient, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.EquipmentOnGround(pos, orient, obj))
          )
        case AvatarAction.LoadPlayer(player_guid, pdata) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.LoadPlayer(pdata))
          )
        case AvatarAction.ObjectDelete(player_guid, item_guid, unk) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ObjectDelete(item_guid, unk))
          )
        case AvatarAction.ObjectHeld(player_guid, slot) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ObjectHeld(slot))
          )
        case AvatarAction.PlanetsideAttribute(guid, attribute_type, attribute_value) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", guid, AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value))
          )
        case AvatarAction.PlayerState(guid, msg, spectator, weapon) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", guid, AvatarResponse.PlayerState(msg, spectator, weapon))
          )
        case AvatarAction.Reload(player_guid, mag) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.Reload(mag))
          )
        case _ => ;
    }

      /*
    case AvatarService.PlayerStateMessage(msg) =>
      //      log.info(s"NEW: ${m}")
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.avatar_guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, msg.avatar_guid,
          AvatarServiceReply.PlayerStateMessage(msg.pos, msg.vel, msg.facingYaw, msg.facingPitch, msg.facingYawUpper, msg.is_crouching, msg.is_jumping, msg.jump_thrust, msg.is_cloaked)
        ))

      }
    case AvatarService.LoadMap(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.LoadMap()
        ))
      }
    case AvatarService.unLoadMap(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.unLoadMap()
        ))
      }
    case AvatarService.ObjectHeld(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.ObjectHeld()
        ))
      }
    case AvatarService.PlanetsideAttribute(guid, attribute_type, attribute_value) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, guid,
          AvatarServiceReply.PlanetSideAttribute(attribute_type, attribute_value)
        ))
      }
    case AvatarService.PlayerStateShift(killer, guid) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, guid,
          AvatarServiceReply.PlayerStateShift(killer)
        ))
      }
    case AvatarService.DestroyDisplay(killer, victim) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(victim)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, victim,
          AvatarServiceReply.DestroyDisplay(killer)
        ))
      }
    case AvatarService.HitHintReturn(source_guid,victim_guid) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(source_guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, victim_guid,
          AvatarServiceReply.DestroyDisplay(source_guid)
        ))
      }
    case AvatarService.ChangeWeapon(unk1, sessionId) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(sessionId)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(player.guid),
          AvatarServiceReply.ChangeWeapon(unk1)
        ))
      }
      */
    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
