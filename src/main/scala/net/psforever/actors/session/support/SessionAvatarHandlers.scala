// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, typed}
import net.psforever.services.Service

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
  //TODO player characters only exist within a certain range of GUIDs for a given zone; this is overkill
  private[support] var lastSeenStreamMessage: Array[Long] = Array.fill[Long](65535)(elem = 0L)

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
    reply match {
      case AvatarResponse.TeardownConnection() =>
        log.trace(s"ending ${player.Name}'s old session by event system request (relog)")
        context.stop(context.self)

      case AvatarResponse.PlayerState(
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
      spectating,
      _
      ) if isNotSameTarget =>
        val now = System.currentTimeMillis()
        val (location, time, distanceSq): (Vector3, Long, Float) = if (spectating) {
          val r = new scala.util.Random
          val r1 = 2 + r.nextInt(30).toFloat
          val r2 = 2 + r.nextInt(4000).toFloat
          (Vector3(r2, r2, r1), 0L, 0f)
        } else {
          val before = lastSeenStreamMessage(guid.guid)
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
              yawUpper,
              timestamp=0,
              isCrouching,
              isJumping,
              jumpThrust,
              isCloaking
            )
          )
          lastSeenStreamMessage(guid.guid) = now
        }

      case AvatarResponse.SendResponse(msg) =>
        sendResponse(msg)

      case AvatarResponse.SendResponseTargeted(targetGuid, msg) if resolvedPlayerGuid == targetGuid =>
        sendResponse(msg)

      case AvatarResponse.Revive(revivalTargetGuid) if resolvedPlayerGuid == revivalTargetGuid =>
        log.info(s"No time for rest, ${player.Name}.  Back on your feet!")
        sessionData.zoning.spawn.reviveTimer.cancel()
        sessionData.zoning.spawn.deadState = DeadState.Alive
        player.Revive
        val health = player.Health
        sendResponse(PlanetsideAttributeMessage(revivalTargetGuid, attribute_type=0, health))
        sendResponse(AvatarDeadStateMessage(DeadState.Alive, timer_max=0, timer=0, player.Position, player.Faction, unk5=true))
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.PlanetsideAttributeToAll(revivalTargetGuid, attribute_type=0, health)
        )

      case AvatarResponse.ConcealPlayer() =>
        sendResponse(GenericObjectActionMessage(guid, code=9))

      case AvatarResponse.EnvironmentalDamage(_, _, _) =>
        //TODO damage marker?
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_dmg")

      case AvatarResponse.Destroy(victim, killer, weapon, pos) =>
        // guid = victim // killer = killer
        sendResponse(DestroyMessage(victim, killer, weapon, pos))

      case AvatarResponse.DestroyDisplay(killer, victim, method, unk) =>
        sendResponse(sessionData.destroyDisplayMessage(killer, victim, method, unk))
        // TODO Temporary thing that should go somewhere else and use proper xp values
        if (killer.CharId == avatar.id && killer.Faction != victim.Faction) {
          avatarActor ! AvatarActor.AwardBep((1000 * Config.app.game.bepRate).toLong, ExperienceType.Normal)
          avatarActor ! AvatarActor.AwardCep((100 * Config.app.game.cepRate).toLong)
        }

      case AvatarResponse.DropItem(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.EquipmentInHand(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.GenericObjectAction(objectGuid, actionCode) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(objectGuid, actionCode))

      case AvatarResponse.HitHint(source_guid) if player.isAlive =>
        sendResponse(HitHint(source_guid, guid))
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_dmg")

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
        player.FreeHand.Equipment.foreach { item =>
          DropEquipmentFromInventory(player)(item)
        }
        sessionData.dropSpecialSlotItem()
        sessionData.toggleMaxSpecialState(enable = false)

        if (player.LastDamage.flatMap { damage =>
          Some(damage.interaction.cause match {
            case cause: ExplodingEntityReason => cause.entity.isInstanceOf[VehicleSpawnPad]
            case _ => false
          }) }.contains(true)
        ) {
          //also, @SVCP_Killed_TooCloseToPadOnCreate^n~ or "... within n meters of pad ..."
          sendResponse(ChatMsg(ChatMessageType.UNK_227, wideContents = false, "", "@SVCP_Killed_OnPadOnCreate", None))
        }
        sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive
        sessionData.zoning.zoningStatus = Zoning.Status.None
        sessionData.zoning.spawn.deadState = DeadState.Dead
        continent.GUID(mount).collect { case obj: Vehicle =>
          sessionData.vehicles.ConditionalDriverVehicleControl(obj)
          sessionData.vehicles.serverVehicleControlVelocity = None
          sessionData.unaccessContainer(obj)
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

      case AvatarResponse.LoadPlayer(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.LoadProjectile(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case AvatarResponse.ObjectDelete(itemGuid, unk) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(itemGuid, unk))

      case AvatarResponse.ObjectHeld(slot, _) if resolvedPlayerGuid == guid && slot > -1 =>
        sendResponse(ObjectHeldMessage(guid, slot, unk1=true))
        //Stop using proximity terminals if player unholsters a weapon
        if (player.VisibleSlots.contains(slot)) {
          continent.GUID(sessionData.terminals.usingMedicalTerminal).collect {
            case term: Terminal with ProximityUnit =>
              sessionData.terminals.StopUsingProximityUnit(term)
          }
        }

      case AvatarResponse.ObjectHeld(_, previousSlot) =>
        sendResponse(ObjectHeldMessage(guid, previousSlot, unk1=false))

      case AvatarResponse.OxygenState(player, vehicle) =>
        sendResponse(OxygenStateMessage(
          DrowningTarget(player.guid, player.progress, player.state),
          vehicle.flatMap { vinfo => Some(DrowningTarget(vinfo.guid, vinfo.progress, vinfo.state)) }
        ))

      case AvatarResponse.PlanetsideAttribute(attributeType, attributeValue) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

      case AvatarResponse.PlanetsideAttributeToAll(attributeType, attributeValue) =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

      case AvatarResponse.PlanetsideAttributeSelf(attributeType, attributeValue) if resolvedPlayerGuid == guid =>
        sendResponse(PlanetsideAttributeMessage(guid, attributeType, attributeValue))

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

      case AvatarResponse.ProjectileState(projectileGuid, shotPos, shotVel, shotOrient, seq, end, targetGuid) if isNotSameTarget =>
        sendResponse(ProjectileStateMessage(projectileGuid, shotPos, shotVel, shotOrient, seq, end, targetGuid))

      case AvatarResponse.PutDownFDU(target) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(target, code=53))

      case AvatarResponse.Release(tplayer) if isNotSameTarget =>
        sessionData.zoning.spawn.DepictPlayerAsCorpse(tplayer)

      case AvatarResponse.Reload(itemGuid) if isNotSameTarget =>
        sendResponse(ReloadMessage(itemGuid, ammo_clip=1, unk1=0))

      case AvatarResponse.SetEmpire(objectGuid, faction) if isNotSameTarget =>
        sendResponse(SetEmpireMessage(objectGuid, faction))

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

      case AvatarResponse.WeaponDryFire(weaponGuid) if isNotSameTarget =>
        continent.GUID(weaponGuid).collect {
          case tool: Tool if tool.Magazine == 0 =>
            // check that the magazine is still empty before sending WeaponDryFireMessage
            // if it has been reloaded since then, other clients not see it firing
            sendResponse(WeaponDryFireMessage(weaponGuid))
          case _ =>
            sendResponse(WeaponDryFireMessage(weaponGuid))
        }

      case AvatarResponse.TerminalOrderResult(terminalGuid, action, result) =>
        sendResponse(ItemTransactionResultMessage(terminalGuid, action, result))
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
        //redraw
        if (maxhand) {
          TaskWorkflow.execute(HoldNewEquipmentUp(player)(
            Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
            slot = 0
          ))
        }
        sessionData.applyPurchaseTimersBeforePackingLoadout(player, player, holsters ++ inventory)
        DropLeftovers(player)(drops)

      case AvatarResponse.ChangeLoadout(target, armor, exosuit, subtype, slot, _, oldHolsters, _, _, _, _) =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, attribute_type=4, armor))
        //happening to some other player
        sendResponse(ObjectHeldMessage(target, slot, unk1=false))
        //cleanup
        oldHolsters.foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, unk1=0)) }
      //redraw handled by callback

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
        sessionData.kitToBeUsed = None

      case AvatarResponse.KitNotUsed(_, msg) =>
        sessionData.kitToBeUsed = None
        sendResponse(ChatMsg(ChatMessageType.UNK_225, wideContents=false, recipient="", msg, note=None))

      case _ => ()
    }
  }
}
