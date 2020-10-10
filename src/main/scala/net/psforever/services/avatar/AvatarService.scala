// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.{DroppedItemData, ObjectCreateMessageParent, PlacementData}
import net.psforever.types.PlanetSideGUID
import net.psforever.services.avatar.support.{CorpseRemovalActor, DroppedItemRemover}
import net.psforever.services.{GenericEventBus, RemoverActor, Service}

class AvatarService(zone: Zone) extends Actor {
  private val undertaker: ActorRef = context.actorOf(Props(classOf[CorpseRemovalActor], zone.tasks), s"${zone.id}-corpse-removal-agent")
  private val janitor              = context.actorOf(Props(classOf[DroppedItemRemover], zone.tasks), s"${zone.id}-item-remover-agent")

  private[this] val log = org.log4s.getLogger

  override def preStart() = {
    log.trace(s"Awaiting ${zone.id} avatar events ...")
  }

  val AvatarEvents = new GenericEventBus[AvatarServiceResponse] //AvatarEventBus

  def receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/Avatar"
      val who  = sender()
      log.info(s"$who has joined $path")
      AvatarEvents.subscribe(who, path)

    case Service.Leave(None) =>
      AvatarEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Avatar"
      val who  = sender()
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
        case AvatarAction.ChangeAmmo(
              player_guid,
              weapon_guid,
              weapon_slot,
              old_ammo_guid,
              ammo_id,
              ammo_guid,
              ammo_data
            ) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, old_ammo_guid, ammo_id, ammo_guid, ammo_data)
            )
          )
        case AvatarAction.ChangeFireMode(player_guid, item_guid, mode) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeFireMode(item_guid, mode))
          )
        case AvatarAction.ChangeFireState_Start(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.ChangeFireState_Start(weapon_guid)
            )
          )
        case AvatarAction.ChangeFireState_Stop(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ChangeFireState_Stop(weapon_guid))
          )
        case AvatarAction.ConcealPlayer(player_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ConcealPlayer())
          )
        case AvatarAction.EnvironmentalDamage(player_guid, source_guid, amount) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.EnvironmentalDamage(player_guid, source_guid, amount)
            )
          )

        case AvatarAction.DeployItem(player_guid, item) =>
          val definition = item.Definition
          val objectData = definition.Packet.ConstructorData(item).get
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.DropItem(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
            )
          )
        case AvatarAction.Destroy(victim, killer, weapon, pos) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", victim, AvatarResponse.Destroy(victim, killer, weapon, pos))
          )
        case AvatarAction.DestroyDisplay(killer, victim, method, unk) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              Service.defaultPlayerGUID,
              AvatarResponse.DestroyDisplay(killer, victim, method, unk)
            )
          )
        case AvatarAction.DropItem(player_guid, item) =>
          val definition = item.Definition
          val objectData = DroppedItemData(
            PlacementData(item.Position, item.Orientation),
            definition.Packet.ConstructorData(item).get
          )
          janitor forward RemoverActor.AddTask(item, zone)
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.DropItem(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
            )
          )
        case AvatarAction.EquipmentInHand(player_guid, target_guid, slot, item) =>
          val definition    = item.Definition
          val containerData = ObjectCreateMessageParent(target_guid, slot)
          val objectData    = definition.Packet.ConstructorData(item).get
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.EquipmentInHand(
                ObjectCreateMessage(definition.ObjectId, item.GUID, containerData, objectData)
              )
            )
          )
        case AvatarAction.GenericObjectAction(player_guid, object_guid, action_code) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.GenericObjectAction(object_guid, action_code)
            )
          )
        case AvatarAction.HitHint(source_guid, player_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.HitHint(source_guid))
          )
        case AvatarAction.Killed(player_guid, mount_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.Killed(mount_guid))
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
        case AvatarAction.LoadProjectile(player_guid, object_id, object_guid, cdata) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.LoadProjectile(
                ObjectCreateMessage(object_id, object_guid, cdata)
              )
            )
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
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              guid,
              AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value)
            )
          )
        case AvatarAction.PlanetsideAttributeToAll(guid, attribute_type, attribute_value) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              guid,
              AvatarResponse.PlanetsideAttributeToAll(attribute_type, attribute_value)
            )
          )
        case AvatarAction.PlanetsideAttributeSelf(guid, attribute_type, attribute_value) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              guid,
              AvatarResponse.PlanetsideAttributeSelf(attribute_type, attribute_value)
            )
          )
        case AvatarAction.PlayerState(
              guid,
              pos,
              vel,
              yaw,
              pitch,
              yaw_upper,
              seq_time,
              is_crouching,
              is_jumping,
              jump_thrust,
              is_cloaking,
              spectating,
              weaponInHand
            ) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              guid,
              AvatarResponse.PlayerState(
                pos,
                vel,
                yaw,
                pitch,
                yaw_upper,
                seq_time,
                is_crouching,
                is_jumping,
                jump_thrust,
                is_cloaking,
                spectating,
                weaponInHand
              )
            )
          )
        case AvatarAction.ProjectileAutoLockAwareness(mode) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              PlanetSideGUID(0),
              AvatarResponse.ProjectileAutoLockAwareness(mode)
            )
          )
        case AvatarAction.ProjectileExplodes(player_guid, projectile_guid, projectile) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.ProjectileExplodes(projectile_guid, projectile)
            )
          )
        case AvatarAction.ProjectileState(
              player_guid,
              projectile_guid,
              shot_pos,
              shot_vel,
              shot_orient,
              sequence,
              end,
              target
            ) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.ProjectileState(projectile_guid, shot_pos, shot_vel, shot_orient, sequence, end, target)
            )
          )
        case AvatarAction.PickupItem(player_guid, item, unk) =>
          janitor forward RemoverActor.ClearSpecific(List(item), zone)
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.ObjectDelete(item.GUID, unk))
          )
        case AvatarAction.PutDownFDU(player_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.PutDownFDU(player_guid))
          )
        case AvatarAction.Release(player, _, time) =>
          undertaker forward RemoverActor.AddTask(player, zone, time)
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player.GUID, AvatarResponse.Release(player))
          )
        case AvatarAction.Reload(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.Reload(weapon_guid))
          )
        case AvatarAction.SetEmpire(player_guid, target_guid, faction) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.SetEmpire(target_guid, faction))
          )
        case AvatarAction.StowEquipment(player_guid, target_guid, slot, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              player_guid,
              AvatarResponse.StowEquipment(target_guid, slot, obj)
            )
          )
        case AvatarAction.WeaponDryFire(player_guid, weapon_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.WeaponDryFire(weapon_guid))
          )
        case AvatarAction.SendResponse(player_guid, msg) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", player_guid, AvatarResponse.SendResponse(msg))
          )
        case AvatarAction.SendResponseTargeted(target_guid, msg) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              target_guid,
              AvatarResponse.SendResponseTargeted(target_guid, msg)
            )
          )
        case AvatarAction.Revive(target_guid) =>
          AvatarEvents.publish(
            AvatarServiceResponse(s"/$forChannel/Avatar", target_guid, AvatarResponse.Revive(target_guid))
          )

        case AvatarAction.TeardownConnection() =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              Service.defaultPlayerGUID,
              AvatarResponse.TeardownConnection()
            )
          )

        case AvatarAction.TerminalOrderResult(terminal, term_action, result) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              Service.defaultPlayerGUID,
              AvatarResponse.TerminalOrderResult(terminal, term_action, result)
            )
          )
        case AvatarAction.ChangeExosuit(
              target,
              armor,
              exosuit,
              subtype,
              slot,
              maxhand,
              old_holsters,
              holsters,
              old_inventory,
              inventory,
              drop,
              delete
            ) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              Service.defaultPlayerGUID,
              AvatarResponse.ChangeExosuit(
                target,
                armor,
                exosuit,
                subtype,
                slot,
                maxhand,
                old_holsters,
                holsters,
                old_inventory,
                inventory,
                drop,
                delete
              )
            )
          )
        case AvatarAction.ChangeLoadout(
              target,
              armor,
              exosuit,
              subtype,
              slot,
              maxhand,
              old_holsters,
              holsters,
              old_inventory,
              inventory,
              drop
            ) =>
          AvatarEvents.publish(
            AvatarServiceResponse(
              s"/$forChannel/Avatar",
              Service.defaultPlayerGUID,
              AvatarResponse.ChangeLoadout(
                target,
                armor,
                exosuit,
                subtype,
                slot,
                maxhand,
                old_holsters,
                holsters,
                old_inventory,
                inventory,
                drop
              )
            )
          )

        case _ => ;
      }

    //message to Undertaker
    case AvatarServiceMessage.Corpse(msg) =>
      undertaker forward msg

    //message to Janitor
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
     */

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }
}
