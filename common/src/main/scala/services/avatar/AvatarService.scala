// Copyright (c) 2017 PSForever
package services.avatar

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.{DroppedItemData, ObjectCreateMessageParent, PlacementData}
import services.avatar.support.{CorpseRemovalActor, DroppedItemRemover}
import services.{GenericEventBus, RemoverActor, Service}

class AvatarService extends Actor {
  private val undertaker : ActorRef = context.actorOf(Props[CorpseRemovalActor], "corpse-removal-agent")
  private val janitor = context.actorOf(Props[DroppedItemRemover], "item-remover-agent")
  //undertaker ! "startup"

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

    case Service.Leave(None) =>
      AvatarEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Avatar"
      val who = sender()
      log.info(s"$who has left $path")
      AvatarEvents.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      AvatarEvents.unsubscribe(sender())

    case AvatarServiceMessage(forChannel, action) =>
      action match {
        case AvatarAction.ArmorChanged(player_guid, suit, subtype) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ArmorChanged(suit, subtype))
          )
        case AvatarAction.ChangeAmmo(player_guid, weapon_guid, weapon_slot, old_ammo_guid, ammo_id, ammo_guid, ammo_data) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, old_ammo_guid, ammo_id, ammo_guid, ammo_data))
          )
        case AvatarAction.ChangeFireMode(player_guid, item_guid, mode) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeFireMode(item_guid, mode))
          )
        case AvatarAction.ChangeFireState_Start(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeFireState_Start(weapon_guid))
          )
        case AvatarAction.ChangeFireState_Stop(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeFireState_Stop(weapon_guid))
          )
        case AvatarAction.ConcealPlayer(player_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ConcealPlayer())
          )
        case AvatarAction.DropItem(player_guid, item, zone) =>
          val definition = item.Definition
          val objectData = DroppedItemData(
            PlacementData(item.Position, item.Orientation),
            definition.Packet.ConstructorData(item).get
          )
          janitor forward RemoverActor.AddTask(item, zone)
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid,
              AvatarResponse.DropItem(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
            )
          )
        case AvatarAction.EquipmentInHand(player_guid, target_guid, slot, item) =>
          val definition = item.Definition
          val containerData = ObjectCreateMessageParent(target_guid, slot)
          val objectData = definition.Packet.ConstructorData(item).get
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid,
              AvatarResponse.EquipmentInHand(ObjectCreateMessage(definition.ObjectId, item.GUID, containerData, objectData))
            )
          )
        case AvatarAction.LoadPlayer(player_guid, object_id, target_guid, cdata, pdata) =>
          val pkt = pdata match {
            case Some(data) =>
              ObjectCreateMessage(object_id, target_guid, data, cdata)
            case None =>
              ObjectCreateMessage(object_id, target_guid, cdata)
          }
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.LoadPlayer(pkt))
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
        case AvatarAction.PickupItem(player_guid, zone, target, slot, item, unk) =>
          janitor forward RemoverActor.ClearSpecific(List(item), zone)
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, {
              val itemGUID = item.GUID
              if(target.VisibleSlots.contains(slot)) {
                val definition = item.Definition
                val containerData = ObjectCreateMessageParent(target.GUID, slot)
                val objectData = definition.Packet.ConstructorData(item).get
                AvatarResponse.EquipmentInHand(ObjectCreateMessage(definition.ObjectId, itemGUID, containerData, objectData))
              }
              else {
                AvatarResponse.ObjectDelete(itemGUID, unk)
              }
            })
          )
        case AvatarAction.Release(player, zone, time) =>
          undertaker forward RemoverActor.AddTask(player, zone, time)
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player.GUID, AvatarResponse.Release(player))
          )
        case AvatarAction.Reload(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.Reload(weapon_guid))
          )
        case AvatarAction.StowEquipment(player_guid, target_guid, slot, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.StowEquipment(target_guid, slot, obj))
          )
        case AvatarAction.WeaponDryFire(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.WeaponDryFire(weapon_guid))
          )

        case _ => ;
    }

    //message to Undertaker
    case AvatarServiceMessage.Corpse(msg) =>
      undertaker forward msg

    case AvatarServiceMessage.Ground(msg) =>
      janitor forward msg

      /*
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
      */
    case msg =>
      log.warn(s"Unhandled message $msg from $sender")
  }
}
