// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, typed}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
//
import net.psforever.actors.session.{AvatarActor, ChatActor}
import net.psforever.login.WorldSession.{DropEquipmentFromInventory, DropLeftovers, HoldNewEquipmentUp}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vital.etc.ExplodingEntityReason
import net.psforever.objects.zones.Zoning
import net.psforever.objects.{GlobalDefinitions, Player, Tool, Vehicle}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage}
import net.psforever.services.{InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.Config
import net.psforever.zones.Zones

class SessionAvatarHandlers(
                             val sessionData: SessionData,
                             avatarActor: typed.ActorRef[AvatarActor.Command],
                             chatActor: typed.ActorRef[ChatActor.Command],
                             implicit val context: ActorContext
                           ) extends CommonSessionInterfacingFunctionality {
  /**
   * na
   *
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: AvatarResponse.Response): Unit = {
    val tplayer_guid =
      if (player != null && player.HasGUID) player.GUID
      else PlanetSideGUID(0)
    reply match {
      case AvatarResponse.TeardownConnection() =>
        log.trace(s"ending ${player.Name}'s old session by event system request (relog)")
        context.stop(context.self)

      case AvatarResponse.SendResponse(msg) =>
        sendResponse(msg)

      case AvatarResponse.SendResponseTargeted(target_guid, msg) =>
        if (tplayer_guid == target_guid) {
          sendResponse(msg)
        }

      case AvatarResponse.Revive(target_guid) =>
        if (tplayer_guid == target_guid) {
          log.info(s"No time for rest, ${player.Name}.  Back on your feet!")
          sessionData.zoning.spawn.reviveTimer.cancel()
          sessionData.zoning.spawn.deadState = DeadState.Alive
          player.Revive
          val health = player.Health
          sendResponse(PlanetsideAttributeMessage(target_guid, 0, health))
          sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, player.Position, player.Faction, unk5=true))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttributeToAll(target_guid, 0, health)
          )
        }

      case AvatarResponse.ArmorChanged(suit, subtype) =>
        if (tplayer_guid != guid) {
          sendResponse(ArmorChangedMessage(guid, suit, subtype))
        }

      case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDetachMessage(weapon_guid, previous_guid, Vector3.Zero, 0))
          sendResponse(
            ObjectCreateMessage(
              ammo_id,
              ammo_guid,
              ObjectCreateMessageParent(weapon_guid, weapon_slot),
              ammo_data
            )
          )
          sendResponse(ChangeAmmoMessage(weapon_guid, 1))
        }

      case AvatarResponse.ChangeFireMode(item_guid, mode) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireModeMessage(item_guid, mode))
        }

      case AvatarResponse.ChangeFireState_Start(weapon_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Start(weapon_guid))
        }

      case AvatarResponse.ChangeFireState_Stop(weapon_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
        }

      case AvatarResponse.ConcealPlayer() =>
        sendResponse(GenericObjectActionMessage(guid, 9))

      case AvatarResponse.EnvironmentalDamage(_, _, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_dmg")
      //TODO damage marker?

      case AvatarResponse.Destroy(victim, killer, weapon, pos) =>
        // guid = victim // killer = killer ;)
        sendResponse(DestroyMessage(victim, killer, weapon, pos))

      case AvatarResponse.DestroyDisplay(killer, victim, method, unk) =>
        sendResponse(sessionData.destroyDisplayMessage(killer, victim, method, unk))
        // TODO Temporary thing that should go somewhere else and use proper xp values
        if (killer.CharId == avatar.id && killer.Faction != victim.Faction) {
          avatarActor ! AvatarActor.AwardBep((1000 * Config.app.game.bepRate).toLong)
          avatarActor ! AvatarActor.AwardCep((100 * Config.app.game.cepRate).toLong)
        }

      case AvatarResponse.DropItem(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.EquipmentInHand(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.GenericObjectAction(object_guid, action_code) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(object_guid, action_code))
        }

      case AvatarResponse.HitHint(source_guid) =>
        if (player.isAlive) {
          sendResponse(HitHint(source_guid, guid))
          sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_dmg")
        }

      case AvatarResponse.DropSpecialItem() =>
        sessionData.dropSpecialSlotItem()

      case AvatarResponse.Killed(mount) =>
        val cause = (player.LastDamage match {
          case Some(reason) => (Some(reason), reason.adversarial)
          case None => (None, None)
        }) match {
          case (_, Some(adversarial)) => adversarial.attacker.Name
          case (Some(reason), None) => s"a ${reason.interaction.cause.getClass.getSimpleName}"
          case _ => s"an unfortunate circumstance (probably ${player.Sex.pronounObject} own fault)"
        }
        log.info(s"${player.Name} has died, killed by $cause")
        val respawnTimer = 300.seconds
        //drop free hand item
        player.FreeHand.Equipment match {
          case Some(item) =>
            DropEquipmentFromInventory(player)(item)
          case None => ;
        }
        sessionData.dropSpecialSlotItem()
        sessionData.toggleMaxSpecialState(enable = false)
        if (player.LastDamage match {
          case Some(damage) => damage.interaction.cause match {
            case cause: ExplodingEntityReason => cause.entity.isInstanceOf[VehicleSpawnPad]
            case _ => false
          }
          case None => false
        }) {
          //also, @SVCP_Killed_TooCloseToPadOnCreate^n~ or "... within n meters of pad ..."
          sendResponse(ChatMsg(ChatMessageType.UNK_227, wideContents=false, "", "@SVCP_Killed_OnPadOnCreate", None))
        }
        sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive
        sessionData.zoning.zoningStatus = Zoning.Status.None
        sessionData.zoning.spawn.deadState = DeadState.Dead
        continent.GUID(mount) match {
          case Some(obj: Vehicle) =>
            sessionData.vehicles.ConditionalDriverVehicleControl(obj)
            sessionData.vehicles.serverVehicleControlVelocity = None
            sessionData.unaccessContainer(obj)
          case _ => ;
        }
        sessionData.playerActionsToCancel()
        sessionData.terminals.CancelAllProximityUnits()
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
        if (sessionData.shooting.shotsWhileDead > 0) {
          log.warn(
            s"KillPlayer/SHOTS_WHILE_DEAD: client of ${avatar.name} fired ${sessionData.shooting.shotsWhileDead} rounds while character was dead on server"
          )
          sessionData.shooting.shotsWhileDead = 0
        }
        sessionData.zoning.spawn.reviveTimer.cancel()
        if (player.death_by == 0) {
          sessionData.zoning.spawn.reviveTimer = context.system.scheduler.scheduleOnce(respawnTimer) {
            sessionData.cluster ! ICS.GetRandomSpawnPoint(
              Zones.sanctuaryZoneNumber(player.Faction),
              player.Faction,
              Seq(SpawnGroup.Sanctuary),
              context.self
            )
          }
        } else {
          sessionData.zoning.spawn.HandleReleaseAvatar(player, continent)
        }
        AvatarActor.savePlayerLocation(player)
        sessionData.renewCharSavedTimer(fixedLen = 1800L, varLen = 0L)

      case AvatarResponse.LoadPlayer(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.LoadProjectile(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.ObjectDelete(item_guid, unk) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDeleteMessage(item_guid, unk))
        }

      case AvatarResponse.ObjectHeld(slot, previousSlot) =>
        if (tplayer_guid == guid) {
          if (slot > -1) {
            sendResponse(ObjectHeldMessage(guid, slot, unk1 = true))
            //Stop using proximity terminals if player unholsters a weapon
            if (player.VisibleSlots.contains(slot)) {
              continent.GUID(sessionData.terminals.usingMedicalTerminal) match {
                case Some(term: Terminal with ProximityUnit) =>
                  sessionData.terminals.StopUsingProximityUnit(term)
                case _ => ;
              }
            }
          }
        } else {
          sendResponse(ObjectHeldMessage(guid, previousSlot, unk1 = false))
        }

      case AvatarResponse.OxygenState(player, vehicle) =>
        sendResponse(
          OxygenStateMessage(
            DrowningTarget(player.guid, player.progress, player.state),
            vehicle match {
              case Some(vinfo) => Some(DrowningTarget(vinfo.guid, vinfo.progress, vinfo.state))
              case None => None
            }
          )
        )

      case AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
        }

      case AvatarResponse.PlanetsideAttributeToAll(attribute_type, attribute_value) =>
        sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))

      case AvatarResponse.PlanetsideAttributeSelf(attribute_type, attribute_value) =>
        if (tplayer_guid == guid) {
          sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
        }

      case AvatarResponse.PlayerState(
      pos,
      vel,
      yaw,
      pitch,
      yaw_upper,
      _,
      is_crouching,
      is_jumping,
      jump_thrust,
      is_cloaking,
      spectating,
      _
      ) =>
        if (tplayer_guid != guid) {
          val now = System.currentTimeMillis()
          val (location, time, distanceSq): (Vector3, Long, Float) = if (spectating) {
            val r = new scala.util.Random
            val r1 = 2 + r.nextInt(30).toFloat
            val r2 = 2 + r.nextInt(4000).toFloat
            (Vector3(r2, r2, r1), 0L, 0f)
          } else {
            val before = player.lastSeenStreamMessage(guid.guid)
            val dist = Vector3.DistanceSquared(player.Position, pos)
            (pos, now - before, dist)
          }
          if (distanceSq < 302500 || time > 5000) { // Render distance seems to be approx 525m. Reduce update rate at ~550m to be safe
            sendResponse(
              PlayerStateMessage(
                guid,
                location,
                vel,
                yaw,
                pitch,
                yaw_upper,
                timestamp = 0,
                is_crouching,
                is_jumping,
                jump_thrust,
                is_cloaking
              )
            )
            player.lastSeenStreamMessage(guid.guid) = now
          }
        }

      case AvatarResponse.ProjectileExplodes(projectile_guid, projectile) =>
        sendResponse(
          ProjectileStateMessage(
            projectile_guid,
            projectile.Position,
            Vector3.Zero,
            projectile.Orientation,
            0,
            end=true,
            PlanetSideGUID(0)
          )
        )
        sendResponse(ObjectDeleteMessage(projectile_guid, 2))

      case AvatarResponse.ProjectileAutoLockAwareness(mode) =>
        sendResponse(GenericActionMessage(mode))

      case AvatarResponse.ProjectileState(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ProjectileStateMessage(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid))
        }

      case AvatarResponse.PutDownFDU(target) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(target, 53))
        }

      case AvatarResponse.Release(tplayer) =>
        if (tplayer_guid != guid) {
          sessionData.zoning.spawn.DepictPlayerAsCorpse(tplayer)
        }

      case AvatarResponse.Reload(item_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ReloadMessage(item_guid, 1, 0))
        }

      case AvatarResponse.SetEmpire(object_guid, faction) =>
        if (tplayer_guid != guid) {
          sendResponse(SetEmpireMessage(object_guid, faction))
        }

      case AvatarResponse.StowEquipment(target, slot, item) =>
        if (tplayer_guid != guid) {
          val definition = item.Definition
          sendResponse(
            ObjectCreateDetailedMessage(
              definition.ObjectId,
              item.GUID,
              ObjectCreateMessageParent(target, slot),
              definition.Packet.DetailedConstructorData(item).get
            )
          )
        }

      case AvatarResponse.WeaponDryFire(weapon_guid) =>
        if (tplayer_guid != guid) {
          continent.GUID(weapon_guid) match {
            case Some(tool: Tool) =>
              // check that the magazine is still empty before sending WeaponDryFireMessage
              // if it has been reloaded since then, other clients not see it firing
              if (tool.Magazine == 0) {
                sendResponse(WeaponDryFireMessage(weapon_guid))
              }
            case Some(_) =>
              sendResponse(WeaponDryFireMessage(weapon_guid))
            case None => ;
          }
        }

      case AvatarResponse.TerminalOrderResult(terminal_guid, action, result) =>
        sendResponse(ItemTransactionResultMessage(terminal_guid, action, result))
        sessionData.terminals.lastTerminalOrderFulfillment = true
        if (result &&
          (action == TransactionType.Buy || action == TransactionType.Loadout)) {
          AvatarActor.savePlayerData(player)
          sessionData.renewCharSavedTimer(
            Config.app.game.savedMsg.interruptedByAction.fixed,
            Config.app.game.savedMsg.interruptedByAction.variable
          )
        }

      case AvatarResponse.ChangeExosuit(
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
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, 4, armor))
        if (tplayer_guid == target) {
          //happening to this player
          //cleanup
          sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, unk1=false))
          (old_holsters ++ old_inventory ++ delete).foreach {
            case (_, dguid) => sendResponse(ObjectDeleteMessage(dguid, 0))
          }
          //functionally delete
          delete.foreach { case (obj, _) => TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj)) }
          //redraw
          if (maxhand) {
            TaskWorkflow.execute(HoldNewEquipmentUp(player)(
              Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
              0
            ))
          }
          //draw free hand
          player.FreeHand.Equipment match {
            case Some(obj) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, Player.FreeHandSlot),
                  definition.Packet.DetailedConstructorData(obj).get
                )
              )
            case None => ;
          }
          //draw holsters and inventory
          (holsters ++ inventory).foreach {
            case InventoryItem(obj, index) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, index),
                  definition.Packet.DetailedConstructorData(obj).get
                )
              )
          }
          DropLeftovers(player)(drop)
        } else {
          //happening to some other player
          sendResponse(ObjectHeldMessage(target, slot, unk1=false))
          //cleanup
          (old_holsters ++ delete).foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
          //draw holsters
          holsters.foreach {
            case InventoryItem(obj, index) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, index),
                  definition.Packet.ConstructorData(obj).get
                )
              )
          }
        }

      case AvatarResponse.ChangeLoadout(
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
      drops
      ) =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, 4, armor))
        if (tplayer_guid == target) {
          //happening to this player
          sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, unk1=true))
          //cleanup
          (old_holsters ++ old_inventory).foreach {
            case (obj, objGuid) =>
              sendResponse(ObjectDeleteMessage(objGuid, 0))
              TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
          }
          //redraw
          if (maxhand) {
            TaskWorkflow.execute(HoldNewEquipmentUp(player)(
              Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
              slot = 0
            ))
          }
          sessionData.applyPurchaseTimersBeforePackingLoadout(player, player, holsters ++ inventory)
          DropLeftovers(player)(drops)
        } else {
          //happening to some other player
          sendResponse(ObjectHeldMessage(target, slot, unk1=false))
          //cleanup
          old_holsters.foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
          //redraw handled by callback
        }

      case AvatarResponse.UseKit(kguid, kObjId) =>
        sendResponse(
          UseItemMessage(
            tplayer_guid,
            kguid,
            tplayer_guid,
            4294967295L,
            unk3=false,
            Vector3.Zero,
            Vector3.Zero,
            126,
            0, //sequence time?
            137,
            kObjId
          )
        )
        sendResponse(ObjectDeleteMessage(kguid, 0))

      case AvatarResponse.KitNotUsed(_, "") =>
        sessionData.kitToBeUsed = None

      case AvatarResponse.KitNotUsed(_, msg) =>
        sessionData.kitToBeUsed = None
        sendResponse(ChatMsg(ChatMessageType.UNK_225, wideContents=false, "", msg, None))

      case _ => ;
    }
  }
}
