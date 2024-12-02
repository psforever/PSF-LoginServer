// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.support.AvatarHandlerFunctions
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.Players
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.packet.game.{AvatarImplantMessage, ImplantAction}

import scala.concurrent.duration._
//
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionAvatarHandlers, SessionData}
import net.psforever.login.WorldSession.{DropEquipmentFromInventory, DropLeftovers, HoldNewEquipmentUp}
import net.psforever.objects.{GlobalDefinitions, Player, Tool, Vehicle}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vital.etc.ExplodingEntityReason
import net.psforever.objects.zones.Zoning
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game.{ArmorChangedMessage, AvatarDeadStateMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ChatMsg, DeadState, DestroyMessage, DrowningTarget, GenericActionMessage, GenericObjectActionMessage, HitHint, ItemTransactionResultMessage, ObjectCreateDetailedMessage, ObjectCreateMessage, ObjectDeleteMessage, ObjectHeldMessage, OxygenStateMessage, PlanetsideAttributeMessage, PlayerStateMessage, ProjectileStateMessage, ReloadMessage, SetEmpireMessage, UseItemMessage, WeaponDryFireMessage}
import net.psforever.services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage}
import net.psforever.services.Service
import net.psforever.types.{ChatMessageType, PlanetSideGUID, TransactionType, Vector3}
import net.psforever.util.Config

object AvatarHandlerLogic {
  def apply(ops: SessionAvatarHandlers): AvatarHandlerLogic = {
    new AvatarHandlerLogic(ops, ops.context)
  }
}

class AvatarHandlerLogic(val ops: SessionAvatarHandlers, implicit val context: ActorContext) extends AvatarHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: AvatarResponse.Response): Unit = {
    val resolvedPlayerGuid = if (player != null && player.HasGUID) {
      player.GUID
    } else {
      Service.defaultPlayerGUID
    }
    val isNotSameTarget = resolvedPlayerGuid != guid
    val isSameTarget = !isNotSameTarget
    reply match {
      /* special messages */
      case AvatarResponse.TeardownConnection() =>
        log.trace(s"ending ${player.Name}'s old session by event system request (relog)")
        context.stop(context.self)

      /* really common messages (very frequently, every life) */
      case pstate @ AvatarResponse.PlayerState(
      pos,
      vel,
      yaw,
      pitch,
      yawUpper,
      _,
      isCrouching,
      isJumping,
      jumpThrust,
      isCloaking,
      isNotRendered,
      canSeeReallyFar
      ) if isNotSameTarget =>
        val pstateToSave = pstate.copy(timestamp = 0)
        val (lastMsg, lastTime, lastPosition, wasVisible, wasShooting) = ops.lastSeenStreamMessage.get(guid.guid) match {
          case Some(SessionAvatarHandlers.LastUpstream(Some(msg), visible, shooting, time)) => (Some(msg), time, msg.pos, visible, shooting)
          case _ => (None, 0L, Vector3.Zero, false, None)
        }
        val drawConfig = Config.app.game.playerDraw //m
        val maxRange = drawConfig.rangeMax * drawConfig.rangeMax //sq.m
        val ourPosition = player.Position //xyz
        val currentDistance = Vector3.DistanceSquared(ourPosition, pos) //sq.m
        val inDrawableRange = currentDistance <= maxRange
        val now = System.currentTimeMillis() //ms
        if (
          sessionLogic.zoning.zoningStatus != Zoning.Status.Deconstructing &&
            !isNotRendered && inDrawableRange
        ) {
          //conditions where visibility is assured
          val durationSince = now - lastTime //ms
          lazy val previouslyInDrawableRange = Vector3.DistanceSquared(ourPosition, lastPosition) <= maxRange
          lazy val targetDelay = {
            val populationOver = math.max(
              0,
              sessionLogic.localSector.livePlayerList.size - drawConfig.populationThreshold
            )
            val distanceAdjustment = math.pow(populationOver / drawConfig.populationStep * drawConfig.rangeStep, 2) //sq.m
            val adjustedDistance = currentDistance + distanceAdjustment //sq.m
            drawConfig.ranges.lastIndexWhere { dist => adjustedDistance > dist * dist } match {
              case -1 => 1
              case index => drawConfig.delays(index)
            }
          } //ms
          if (!wasVisible ||
            !previouslyInDrawableRange ||
            durationSince > drawConfig.delayMax ||
            (!lastMsg.contains(pstateToSave) &&
              (canSeeReallyFar ||
                currentDistance < drawConfig.rangeMin * drawConfig.rangeMin ||
                sessionLogic.general.canSeeReallyFar ||
                durationSince > targetDelay
                )
              )
          ) {
            //must draw
            sendResponse(
              PlayerStateMessage(
                guid,
                pos,
                vel,
                yaw,
                pitch,
                yawUpper,
                timestamp = 0, //is this okay?
                isCrouching,
                isJumping,
                jumpThrust,
                isCloaking
              )
            )
            ops.lastSeenStreamMessage.put(guid.guid, SessionAvatarHandlers.LastUpstream(Some(pstateToSave), visible=true, wasShooting, now))
          } else {
            //is visible, but skip reinforcement
            ops.lastSeenStreamMessage.put(guid.guid, SessionAvatarHandlers.LastUpstream(Some(pstateToSave), visible=true, wasShooting, lastTime))
          }
        } else {
          //conditions where the target is not currently visible
          if (wasVisible) {
            //the target was JUST PREVIOUSLY visible; one last draw to move target beyond a renderable distance
            val lat = (1 + ops.hidingPlayerRandomizer.nextInt(continent.map.scale.height.toInt)).toFloat
            sendResponse(
              PlayerStateMessage(
                guid,
                Vector3(1f, lat, 1f),
                vel=None,
                facingYaw=0f,
                facingPitch=0f,
                facingYawUpper=0f,
                timestamp=0, //is this okay?
                is_cloaked = isCloaking
              )
            )
            ops.lastSeenStreamMessage.put(guid.guid, SessionAvatarHandlers.LastUpstream(Some(pstateToSave), visible=false, wasShooting, now))
          } else {
            //skip drawing altogether
            ops.lastSeenStreamMessage.put(guid.guid, SessionAvatarHandlers.LastUpstream(Some(pstateToSave), visible=false, wasShooting, lastTime))
          }
        }

      case AvatarResponse.ObjectHeld(slot, _)
        if isSameTarget && player.VisibleSlots.contains(slot) =>
        sendResponse(ObjectHeldMessage(guid, slot, unk1=true))
        //Stop using proximity terminals if player unholsters a weapon
        continent.GUID(sessionLogic.terminals.usingMedicalTerminal).collect {
          case term: Terminal with ProximityUnit => sessionLogic.terminals.StopUsingProximityUnit(term)
        }
        if (sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing) {
          sessionLogic.zoning.spawn.stopDeconstructing()
        }

      case AvatarResponse.ObjectHeld(slot, _)
        if isSameTarget && slot > -1 =>
        sendResponse(ObjectHeldMessage(guid, slot, unk1=true))

      case AvatarResponse.ObjectHeld(_, _)
        if isSameTarget => ()

      case AvatarResponse.ObjectHeld(_, previousSlot) =>
        sendResponse(ObjectHeldMessage(guid, previousSlot, unk1=false))

      case AvatarResponse.ChangeFireState_Start(weaponGuid)
        if isNotSameTarget && ops.lastSeenStreamMessage.get(guid.guid).exists { _.visible } =>
        sendResponse(ChangeFireStateMessage_Start(weaponGuid))
        val entry = ops.lastSeenStreamMessage(guid.guid)
        ops.lastSeenStreamMessage.put(guid.guid, entry.copy(shooting = Some(weaponGuid)))

      case AvatarResponse.ChangeFireState_Start(weaponGuid)
        if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Start(weaponGuid))

      case AvatarResponse.ChangeFireState_Stop(weaponGuid)
        if isNotSameTarget && ops.lastSeenStreamMessage.get(guid.guid).exists { msg => msg.visible || msg.shooting.nonEmpty } =>
        sendResponse(ChangeFireStateMessage_Stop(weaponGuid))
        val entry = ops.lastSeenStreamMessage(guid.guid)
        ops.lastSeenStreamMessage.put(guid.guid, entry.copy(shooting = None))

      case AvatarResponse.ChangeFireState_Stop(weaponGuid)
        if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Stop(weaponGuid))

      case AvatarResponse.LoadPlayer(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.EquipmentInHand(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.PlanetsideAttribute(attributeType, attributeValue) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

      case AvatarResponse.PlanetsideAttributeToAll(attributeType, attributeValue) =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

      case AvatarResponse.PlanetsideAttributeSelf(attributeType, attributeValue) if isSameTarget =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

      case AvatarResponse.GenericObjectAction(objectGuid, actionCode) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(objectGuid, actionCode))

      case AvatarResponse.HitHint(sourceGuid) if player.isAlive =>
        sendResponse(HitHint(sourceGuid, guid))
        sessionLogic.zoning.CancelZoningProcess()

      case AvatarResponse.Destroy(victim, killer, weapon, pos) =>
        // guid = victim // killer = killer
        sendResponse(DestroyMessage(victim, killer, weapon, pos))

      case AvatarResponse.DestroyDisplay(killer, victim, method, unk) =>
        sendResponse(ops.destroyDisplayMessage(killer, victim, method, unk))

      case AvatarResponse.TerminalOrderResult(terminalGuid, action, result)
        if result && (action == TransactionType.Buy || action == TransactionType.Loadout) =>
        sendResponse(ItemTransactionResultMessage(terminalGuid, action, result))
        sessionLogic.terminals.lastTerminalOrderFulfillment = true
        AvatarActor.savePlayerData(player)

      case AvatarResponse.TerminalOrderResult(terminalGuid, action, result) =>
        sendResponse(ItemTransactionResultMessage(terminalGuid, action, result))
        sessionLogic.terminals.lastTerminalOrderFulfillment = true

      case AvatarResponse.ChangeExosuit(
      target,
      armor,
      exosuit,
      subtype,
      _,
      maxhand,
      oldHolsters,
      holsters,
      oldInventory,
      inventory,
      drop,
      delete
      ) if resolvedPlayerGuid == target =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, attribute_type=4, armor))
        //happening to this player
        //cleanup
        sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, unk1=false))
        (oldHolsters ++ oldInventory ++ delete).foreach {
          case (_, dguid) => sendResponse(ObjectDeleteMessage(dguid, unk1=0))
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
        player.FreeHand.Equipment.foreach { obj =>
          val definition = obj.Definition
          sendResponse(
            ObjectCreateDetailedMessage(
              definition.ObjectId,
              obj.GUID,
              ObjectCreateMessageParent(target, Player.FreeHandSlot),
              definition.Packet.DetailedConstructorData(obj).get
            )
          )
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

      case AvatarResponse.ChangeExosuit(target, armor, exosuit, subtype, slot, _, oldHolsters, holsters, _, _, _, delete) =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, attribute_type=4, armor))
        //happening to some other player
        sendResponse(ObjectHeldMessage(target, slot, unk1 = false))
        //cleanup
        (oldHolsters ++ delete).foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, unk1=0)) }
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

      case AvatarResponse.ChangeLoadout(
      target,
      armor,
      exosuit,
      subtype,
      _,
      maxhand,
      oldHolsters,
      holsters,
      oldInventory,
      inventory,
      drops
      ) if resolvedPlayerGuid == target =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, attribute_type = 4, armor))
        //happening to this player
        sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, unk1=true))
        //cleanup
        (oldHolsters ++ oldInventory).foreach {
          case (obj, objGuid) =>
            sendResponse(ObjectDeleteMessage(objGuid, unk1=0))
            TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
        }
        drops.foreach(item => sendResponse(ObjectDeleteMessage(item.obj.GUID, unk1=0)))
        //redraw
        if (maxhand) {
          TaskWorkflow.execute(HoldNewEquipmentUp(player)(
            Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
            slot = 0
          ))
        }
        sessionLogic.general.applyPurchaseTimersBeforePackingLoadout(player, player, holsters ++ inventory)
        DropLeftovers(player)(drops)

      case AvatarResponse.ChangeLoadout(target, armor, exosuit, subtype, slot, _, oldHolsters, _, _, _, _) =>
        //redraw handled by callbacks
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, attribute_type=4, armor))
        //happening to some other player
        sendResponse(ObjectHeldMessage(target, slot, unk1=false))
        //cleanup
        oldHolsters.foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, unk1=0)) }

      case AvatarResponse.UseKit(kguid, kObjId) =>
        sendResponse(
          UseItemMessage(
            resolvedPlayerGuid,
            kguid,
            resolvedPlayerGuid,
            unk2 = 4294967295L,
            unk3 = false,
            unk4 = Vector3.Zero,
            unk5 = Vector3.Zero,
            unk6 = 126,
            unk7 = 0, //sequence time?
            unk8 = 137,
            kObjId
          )
        )
        sendResponse(ObjectDeleteMessage(kguid, unk1=0))

      case AvatarResponse.KitNotUsed(_, "") =>
        sessionLogic.general.kitToBeUsed = None

      case AvatarResponse.KitNotUsed(_, msg) =>
        sessionLogic.general.kitToBeUsed = None
        sendResponse(ChatMsg(ChatMessageType.UNK_225, msg))

      case AvatarResponse.UpdateKillsDeathsAssists(_, kda: Kill) if kda.experienceEarned > 0 =>
        continent.actor ! ZoneActor.RewardOurSupporters(
          PlayerSource(player),
          Players.produceContributionTranscriptFromKill(continent, player, kda),
          kda,
          kda.experienceEarned
        )

      case AvatarResponse.AwardBep(charId, bep, expType) =>
        //if the target player, always award (some) BEP
        if (charId == player.CharId) {
          avatarActor ! AvatarActor.AwardBep(bep, expType)
        }

      case AvatarResponse.AwardCep(charId, cep)  =>
        //if the target player, always award (some) CEP
        if (charId == player.CharId) {
          avatarActor ! AvatarActor.AwardCep(cep)
        }

      case AvatarResponse.FacilityCaptureRewards(buildingId, zoneNumber, cep) =>
        ops.facilityCaptureRewards(buildingId, zoneNumber, cep)

      case AvatarResponse.SendResponse(pkt: AvatarImplantMessage)
        if pkt.player_guid == player.GUID && pkt.action == ImplantAction.Initialization =>
        //special spectator implants stay initialized and do not deinitialize
        ()

      case AvatarResponse.SendResponse(msg) =>
        sendResponse(msg)

      case AvatarResponse.SendResponseTargeted(targetGuid, msg) if resolvedPlayerGuid == targetGuid =>
        sendResponse(msg)

      /* common messages (maybe once every respawn) */
      case AvatarResponse.Reload(itemGuid)
        if isNotSameTarget && ops.lastSeenStreamMessage.get(guid.guid).exists { _.visible } =>
        sendResponse(ReloadMessage(itemGuid, ammo_clip=1, unk1=0))

      case AvatarResponse.Killed(_, mount) =>
        //log and chat messages
        val cause = player.LastDamage.flatMap { damage =>
          val interaction = damage.interaction
          val reason = interaction.cause
          val adversarial = interaction.adversarial.map { _.attacker }
          reason match {
            case r: ExplodingEntityReason if r.entity.isInstanceOf[VehicleSpawnPad] =>
              //also, @SVCP_Killed_TooCloseToPadOnCreate^n~ or "... within n meters of pad ..."
              sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SVCP_Killed_OnPadOnCreate"))
            case _ => ()
          }
          adversarial.map {_.Name }.orElse { Some(s"a ${reason.getClass.getSimpleName}") }
        }.getOrElse { s"an unfortunate circumstance (probably ${player.Sex.pronounObject} own fault)" }
        log.info(s"${player.Name} has died, killed by $cause")
        if (sessionLogic.shooting.shotsWhileDead > 0) {
          log.warn(
            s"SHOTS_WHILE_DEAD: client of ${avatar.name} fired ${sessionLogic.shooting.shotsWhileDead} rounds while character was dead on server"
          )
          sessionLogic.shooting.shotsWhileDead = 0
        }
        sessionLogic.zoning.CancelZoningProcess()

        //player state changes
        AvatarActor.updateToolDischargeFor(avatar)
        player.FreeHand.Equipment.foreach { item =>
          DropEquipmentFromInventory(player)(item)
        }
        sessionLogic.general.dropSpecialSlotItem()
        sessionLogic.general.toggleMaxSpecialState(enable = false)
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive
        sessionLogic.zoning.zoningStatus = Zoning.Status.None
        sessionLogic.zoning.spawn.deadState = DeadState.Dead
        continent.GUID(mount).collect { case obj: Vehicle =>
          sessionLogic.vehicles.ConditionalDriverVehicleControl(obj)
          sessionLogic.general.unaccessContainer(obj)
        }
        sessionLogic.actionsToCancel()
        sessionLogic.terminals.CancelAllProximityUnits()
        AvatarActor.savePlayerLocation(player)
        sessionLogic.zoning.spawn.shiftPosition = Some(player.Position)

        //respawn
        sessionLogic.zoning.spawn.reviveTimer.cancel()
        if (player.death_by == 0) {
          sessionLogic.zoning.spawn.randomRespawn(300.seconds)
        } else {
          sessionLogic.zoning.spawn.HandleReleaseAvatar(player, continent)
        }

      case AvatarResponse.Release(tplayer) if isNotSameTarget =>
        sessionLogic.zoning.spawn.DepictPlayerAsCorpse(tplayer)

      case AvatarResponse.Revive(revivalTargetGuid) if resolvedPlayerGuid == revivalTargetGuid =>
        log.info(s"No time for rest, ${player.Name}.  Back on your feet!")
        sessionLogic.zoning.spawn.reviveTimer.cancel()
        sessionLogic.zoning.spawn.deadState = DeadState.Alive
        player.Revive
        val health = player.Health
        sendResponse(PlanetsideAttributeMessage(revivalTargetGuid, attribute_type=0, health))
        sendResponse(AvatarDeadStateMessage(DeadState.Alive, timer_max=0, timer=0, player.Position, player.Faction, unk5=true))
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.PlanetsideAttributeToAll(revivalTargetGuid, attribute_type=0, health)
        )

      /* uncommon messages (utility, or once in a while) */
      case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data)
        if isNotSameTarget && ops.lastSeenStreamMessage.get(guid.guid).exists { _.visible } =>
        ops.changeAmmoProcedures(weapon_guid, previous_guid, ammo_id, ammo_guid, weapon_slot, ammo_data)
        sendResponse(ChangeAmmoMessage(weapon_guid, 1))

      case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data)
        if isNotSameTarget =>
        ops.changeAmmoProcedures(weapon_guid, previous_guid, ammo_id, ammo_guid, weapon_slot, ammo_data)

      case AvatarResponse.ChangeFireMode(itemGuid, mode) if isNotSameTarget =>
        sendResponse(ChangeFireModeMessage(itemGuid, mode))

      case AvatarResponse.ConcealPlayer() =>
        sendResponse(GenericObjectActionMessage(guid, code=9))

      case AvatarResponse.EnvironmentalDamage(_, _, _) =>
        //TODO damage marker?
        sessionLogic.zoning.CancelZoningProcess()

      case AvatarResponse.DropItem(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.ObjectDelete(itemGuid, unk) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(itemGuid, unk))

      /* rare messages */
      case AvatarResponse.SetEmpire(objectGuid, faction) if isNotSameTarget =>
        sendResponse(SetEmpireMessage(objectGuid, faction))

      case AvatarResponse.DropSpecialItem() =>
        sessionLogic.general.dropSpecialSlotItem()

      case AvatarResponse.OxygenState(player, vehicle) =>
        sendResponse(OxygenStateMessage(
          DrowningTarget(player.guid, player.progress, player.state),
          vehicle.flatMap { vinfo => Some(DrowningTarget(vinfo.guid, vinfo.progress, vinfo.state)) }
        ))

      case AvatarResponse.LoadProjectile(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.ProjectileState(projectileGuid, shotPos, shotVel, shotOrient, seq, end, targetGuid) if isNotSameTarget =>
        sendResponse(ProjectileStateMessage(projectileGuid, shotPos, shotVel, shotOrient, seq, end, targetGuid))

      case AvatarResponse.ProjectileExplodes(projectileGuid, projectile) =>
        sendResponse(
          ProjectileStateMessage(
            projectileGuid,
            projectile.Position,
            shot_vel = Vector3.Zero,
            projectile.Orientation,
            sequence_num=0,
            end=true,
            hit_target_guid=PlanetSideGUID(0)
          )
        )
        sendResponse(ObjectDeleteMessage(projectileGuid, unk1=2))

      case AvatarResponse.ProjectileAutoLockAwareness(mode) =>
        sendResponse(GenericActionMessage(mode))

      case AvatarResponse.PutDownFDU(target) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(target, code=53))

      case AvatarResponse.StowEquipment(target, slot, item) if isNotSameTarget =>
        val definition = item.Definition
        sendResponse(
          ObjectCreateDetailedMessage(
            definition.ObjectId,
            item.GUID,
            ObjectCreateMessageParent(target, slot),
            definition.Packet.DetailedConstructorData(item).get
          )
        )

      case AvatarResponse.WeaponDryFire(weaponGuid)
        if isNotSameTarget && ops.lastSeenStreamMessage.get(guid.guid).exists { _.visible } =>
        continent.GUID(weaponGuid).collect {
          case tool: Tool if tool.Magazine == 0 =>
            // check that the magazine is still empty before sending WeaponDryFireMessage
            // if it has been reloaded since then, other clients will not see it firing
            sendResponse(WeaponDryFireMessage(weaponGuid))
        }

      case _ => ()
    }
  }
}
