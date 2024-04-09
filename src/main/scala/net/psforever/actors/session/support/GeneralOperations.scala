// Copyright (c) 2024 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital.etc.SuicideReason

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
//
import net.psforever.actors.session.{AvatarActor, ChatActor, SessionActor}
import net.psforever.login.WorldSession._
import net.psforever.objects._
import net.psforever.objects.avatar._
import net.psforever.objects.ballistics._
import net.psforever.objects.ce._
import net.psforever.objects.definition._
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment._
import net.psforever.objects.guid._
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.locker.LockerContainer
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObject}
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vehicles._
import net.psforever.objects.vital._
import net.psforever.objects.vital.collision.{CollisionReason, CollisionWithReason}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones._
import net.psforever.packet._
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeShortcutBankMessage, CharacterRequestMessage, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DisplayedAwardMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FavoritesRequest, FriendsRequest, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, SetChatFilterMessage, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UseItemMessage, ZipLineMessage}
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game._
import net.psforever.services.account.{AccountPersistenceService, RetrieveAccountData}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{RemoverActor, Service}
import net.psforever.types._
import net.psforever.util.Config

class GeneralOperations(
                         val sessionLogic: SessionLogic,
                         avatarActor: typed.ActorRef[AvatarActor.Command],
                         chatActor: typed.ActorRef[ChatActor.Command],
                         implicit val context: ActorContext
                       ) extends CommonSessionInterfacingFunctionality {
  private[support] var progressBarValue: Option[Float] = None
  private[support] var accessedContainer: Option[PlanetSideGameObject with Container] = None
  private[support] var recentTeleportAttempt: Long = 0
  private[support] var kitToBeUsed: Option[PlanetSideGUID] = None
  // If a special item (e.g. LLU) has been attached to the player the GUID should be stored here, or cleared when dropped, since the drop hotkey doesn't send the GUID of the object to be dropped.
  private[support] var specialItemSlotGuid: Option[PlanetSideGUID] = None
  private[support] val collisionHistory: mutable.HashMap[ActorRef, Long] = mutable.HashMap()
  private var heightLast: Float = 0f
  private var heightTrend: Boolean = false //up = true, down = false
  private var heightHistory: Float = 0f
  private[support] var progressBarUpdate: Cancellable = Default.Cancellable
  private var charSavedTimer: Cancellable = Default.Cancellable

  /* packets */

  def handleConnectToWorldRequest(pkt: ConnectToWorldRequestMessage)(implicit context: ActorContext): Unit = {
    val ConnectToWorldRequestMessage(_, token, majorVersion, minorVersion, revision, buildDate, _, _) = pkt
    log.trace(
      s"ConnectToWorldRequestMessage: client with versioning $majorVersion.$minorVersion.$revision, $buildDate has sent a token to the server"
    )
    sendResponse(ChatMsg(ChatMessageType.CMT_CULLWATERMARK, wideContents=false, "", "", None))
    sessionLogic.accountIntermediary ! RetrieveAccountData(token)
  }

  def handleCharacterCreateRequest(pkt: CharacterCreateRequestMessage): Unit = {
    val CharacterCreateRequestMessage(name, head, voice, gender, empire) = pkt
    avatarActor ! AvatarActor.CreateAvatar(name, head, voice, gender, empire)
  }

  def handleCharacterRequest(pkt: CharacterRequestMessage): Unit = {
    val CharacterRequestMessage(charId, action) = pkt
    action match {
      case CharacterRequestAction.Delete =>
        avatarActor ! AvatarActor.DeleteAvatar(charId.toInt)
      case CharacterRequestAction.Select =>
        avatarActor ! AvatarActor.SelectAvatar(charId.toInt, context.self)
    }
  }

  def handlePlayerStateUpstream(pkt: PlayerStateMessageUpstream): Unit = {
    val PlayerStateMessageUpstream(
    avatarGuid,
    pos,
    vel,
    yaw,
    pitch,
    yawUpper,
    seqTime,
    _,
    isCrouching,
    isJumping,
    jumpThrust,
    isCloaking,
    _,
    _
    )= pkt
    sessionLogic.persist()
    sessionLogic.turnCounterFunc(avatarGuid)
    sessionLogic.updateBlockMap(player, pos)
    val isMoving     = WorldEntity.isMoving(vel)
    val isMovingPlus = isMoving || isJumping || jumpThrust
    if (isMovingPlus) {
      if (sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing) {
        sessionLogic.zoning.spawn.stopDeconstructing()
      } else if (sessionLogic.zoning.zoningStatus != Zoning.Status.None) {
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_motion")
      }
    }
    fallHeightTracker(pos.z)
    //    if (isCrouching && !player.Crouching) {
    //      //dev stuff goes here
    //    }
    player.Position = pos
    player.Velocity = vel
    player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
    player.FacingYawUpper = yawUpper
    player.Crouching = isCrouching
    player.Jumping = isJumping
    if (isCloaking && !player.Cloaked) {
      sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_cloak")
    }
    player.Cloaked = player.ExoSuit == ExoSuitType.Infiltration && isCloaking
    maxCapacitorTick(jumpThrust)
    if (isMovingPlus && sessionLogic.terminals.usingMedicalTerminal.isDefined) {
      continent.GUID(sessionLogic.terminals.usingMedicalTerminal) match {
        case Some(term: Terminal with ProximityUnit) =>
          sessionLogic.terminals.StopUsingProximityUnit(term)
        case _ => ()
      }
    }
    accessedContainer match {
      // Ensure we don't unload the contents of the vehicle trunk for players seated in the vehicle.
      // This can happen if PSUM arrives during the mounting process
      case Some(veh: Vehicle) if player.VehicleSeated.isEmpty || player.VehicleSeated.get != veh.GUID =>
        if (isMoving || veh.isMoving(test = 1) || Vector3.DistanceSquared(player.Position, veh.TrunkLocation) > 9) {
          val guid = player.GUID
          sendResponse(UnuseItemMessage(guid, veh.GUID))
          sendResponse(UnuseItemMessage(guid, guid))
          unaccessContainer(veh)
        }
      case Some(container) => //just in case
        if (isMovingPlus && (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID)) {
          // Ensure we don't close the container if the player is seated in it
          val guid = player.GUID
          // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
          if (container.HasGUID) {
            sendResponse(UnuseItemMessage(guid, container.GUID))
          }
          sendResponse(UnuseItemMessage(guid, guid))
          unaccessContainer(container)
        }
      case None => ()
    }
    val eagleEye: Boolean = canSeeReallyFar
    val isNotVisible: Boolean = player.spectator ||
      sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing ||
      (player.isAlive && sessionLogic.zoning.spawn.deadState == DeadState.RespawnTime)
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlayerState(
        avatarGuid,
        player.Position,
        player.Velocity,
        yaw,
        pitch,
        yawUpper,
        seqTime,
        isCrouching,
        isJumping,
        jumpThrust,
        isCloaking,
        isNotVisible,
        eagleEye
      )
    )
    sessionLogic.squad.updateSquad()
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
    player.zoneInteractions()
  }

  def handleChat(pkt: ChatMsg): Unit = {
    chatActor ! ChatActor.Message(pkt)
  }

  def handleChatFilter(pkt: SetChatFilterMessage): Unit = {
    val SetChatFilterMessage(_, _, _) = pkt
  }

  def handleVoiceHostRequest(pkt: VoiceHostRequest): Unit = {
    log.debug(s"$pkt")
    sendResponse(VoiceHostKill())
    sendResponse(
      ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, "", "Try our Discord at https://discord.gg/0nRe5TNbTYoUruA4", None)
    )
  }

  def handleVoiceHostInfo(pkt: VoiceHostInfo): Unit = {
    log.debug(s"$pkt")
    sendResponse(VoiceHostKill())
    sendResponse(
      ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, "", "Try our Discord at https://discord.gg/0nRe5TNbTYoUruA4", None)
    )
  }

  def handleEmote(pkt: EmoteMsg): Unit = {
    val EmoteMsg(avatarGuid, emote) = pkt
    sendResponse(EmoteMsg(avatarGuid, emote))
  }

  def handleDropItem(pkt: DropItemMessage): Unit = {
    val DropItemMessage(itemGuid) = pkt
    (sessionLogic.validObject(itemGuid, decorator = "DropItem"), player.FreeHand.Equipment) match {
      case (Some(anItem: Equipment), Some(heldItem))
        if (anItem eq heldItem) && continent.GUID(player.VehicleSeated).nonEmpty =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        RemoveOldEquipmentFromInventory(player)(heldItem)
      case (Some(anItem: Equipment), Some(heldItem))
        if anItem eq heldItem =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        DropEquipmentFromInventory(player)(heldItem)
      case (Some(anItem: Equipment), _)
        if continent.GUID(player.VehicleSeated).isEmpty =>
        //suppress the warning message if in a vehicle
        log.warn(s"DropItem: ${player.Name} wanted to drop a ${anItem.Definition.Name}, but it wasn't at hand")
      case (Some(obj), _) =>
        log.warn(s"DropItem: ${player.Name} wanted to drop a ${obj.Definition.Name}, but it was not equipment")
      case _ => ()
    }
  }

  def handlePickupItem(pkt: PickupItemMessage): Unit = {
    val PickupItemMessage(itemGuid, _, _, _) = pkt
    sessionLogic.validObject(itemGuid, decorator = "PickupItem").collect {
      case item: Equipment if player.Fit(item).nonEmpty =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        PickUpEquipmentFromGround(player)(item)
      case _: Equipment =>
        sendResponse(ActionResultMessage.Fail(16)) //error code?
    }
  }

  def handleObjectHeld(pkt: ObjectHeldMessage): Unit = {
    val ObjectHeldMessage(_, heldHolsters, _) = pkt
    player.Actor ! PlayerControl.ObjectHeld(heldHolsters)
  }

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit = {
    val AvatarJumpMessage(_) = pkt
    avatarActor ! AvatarActor.ConsumeStamina(10)
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(2.5 seconds)
  }

  def handleZipLine(pkt: ZipLineMessage): Unit = {
    val ZipLineMessage(playerGuid, forwards, action, pathId, pos) = pkt
    continent.zipLinePaths.find(x => x.PathId == pathId) match {
      case Some(path) if path.IsTeleporter =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
        val endPoint = path.ZipLinePoints.last
        sendResponse(ZipLineMessage(PlanetSideGUID(0), forwards, 0, pathId, pos))
        //todo: send to zone to show teleport animation to all clients
        sendResponse(PlayerStateShiftMessage(ShiftState(0, endPoint, (player.Orientation.z + player.FacingYawUpper) % 360f, None)))
      case Some(_) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_motion")
        action match {
          case 0 =>
            //travel along the zipline in the direction specified
            sendResponse(ZipLineMessage(playerGuid, forwards, action, pathId, pos))
          case 1 =>
            //disembark from zipline at destination!
            sendResponse(ZipLineMessage(playerGuid, forwards, action, 0, pos))
          case 2 =>
            //get off by force
            sendResponse(ZipLineMessage(playerGuid, forwards, action, 0, pos))
          case _ =>
            log.warn(
              s"${player.Name} tried to do something with a zipline but can't handle it. forwards: $forwards action: $action pathId: $pathId zone: ${continent.Number} / ${continent.id}"
            )
        }
      case _ =>
        log.warn(s"${player.Name} couldn't find a zipline path $pathId in zone ${continent.id}")
    }
  }

  def handleRequestDestroy(pkt: RequestDestroyMessage): Unit = {
    val RequestDestroyMessage(objectGuid) = pkt
    //make sure this is the correct response for all cases
    sessionLogic.validObject(objectGuid, decorator = "RequestDestroy") match {
      case Some(vehicle: Vehicle) =>
        /* line 1a: player is admin (and overrules other access requirements) */
        /* line 1b: vehicle and player (as the owner) acknowledge each other */
        /* line 1c: vehicle is the same faction as player, is ownable, and either the owner is absent or the vehicle is destroyed */
        /* line 2: vehicle is not mounted in anything or, if it is, its seats are empty */
        if (
          (session.account.gm ||
            (player.avatar.vehicle.contains(objectGuid) && vehicle.OwnerGuid.contains(player.GUID)) ||
            (player.Faction == vehicle.Faction &&
              (vehicle.Definition.CanBeOwned.nonEmpty &&
                (vehicle.OwnerGuid.isEmpty || continent.GUID(vehicle.OwnerGuid.get).isEmpty) || vehicle.Destroyed))) &&
            (vehicle.MountedIn.isEmpty || !vehicle.Seats.values.exists(_.isOccupied))
        ) {
          vehicle.Actor ! Vehicle.Deconstruct()
          //log.info(s"RequestDestroy: vehicle $vehicle")
        } else {
          log.warn(s"RequestDestroy: ${player.Name} must own vehicle in order to deconstruct it")
        }

      case Some(obj: Projectile) =>
        if (!obj.isResolved) {
          obj.Miss()
        }
        continent.Projectile ! ZoneProjectile.Remove(objectGuid)

      case Some(obj: BoomerTrigger) =>
        if (findEquipmentToDelete(objectGuid, obj)) {
          continent.GUID(obj.Companion) match {
            case Some(boomer: BoomerDeployable) =>
              boomer.Trigger = None
              boomer.Actor ! Deployable.Deconstruct()
            case Some(thing) =>
              log.warn(s"RequestDestroy: BoomerTrigger object connected to wrong object - $thing")
            case None => ()
          }
        }

      case Some(obj: Deployable) =>
        if (session.account.gm || obj.OwnerGuid.isEmpty || obj.OwnerGuid.contains(player.GUID) || obj.Destroyed) {
          obj.Actor ! Deployable.Deconstruct()
        } else {
          log.warn(s"RequestDestroy: ${player.Name} must own the deployable in order to deconstruct it")
        }

      case Some(obj: Equipment) =>
        findEquipmentToDelete(objectGuid, obj)

      case Some(thing) =>
        log.warn(s"RequestDestroy: not allowed to delete this ${thing.Definition.Name}")

      case None => ()
    }
  }

  def handleMoveItem(pkt: MoveItemMessage): Unit = {
    val MoveItemMessage(itemGuid, sourceGuid, destinationGuid, dest, _) = pkt
    (
      continent.GUID(sourceGuid),
      continent.GUID(destinationGuid),
      sessionLogic.validObject(itemGuid, decorator = "MoveItem")
    ) match {
      case (
        Some(source: PlanetSideServerObject with Container),
        Some(destination: PlanetSideServerObject with Container),
        Some(item: Equipment)
        ) =>
        ContainableMoveItem(player.Name, source, destination, item, destination.SlotMapResolution(dest))
      case (None, _, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid from $sourceGuid, but could not find source object"
        )
      case (_, None, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid to $destinationGuid, but could not find destination object"
        )
      case (_, _, None) => ()
      case _ =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid from $sourceGuid to $destinationGuid, but multiple problems were encountered"
        )
    }
  }

  def handleLootItem(pkt: LootItemMessage): Unit = {
    val LootItemMessage(itemGuid, targetGuid) = pkt
    (sessionLogic.validObject(itemGuid, decorator = "LootItem"), continent.GUID(targetGuid)) match {
      case (Some(item: Equipment), Some(destination: PlanetSideServerObject with Container)) =>
        //figure out the source
        (
          {
            val findFunc: PlanetSideServerObject with Container => Option[
              (PlanetSideServerObject with Container, Option[Int])
            ] = findInLocalContainer(itemGuid)
            findFunc(player.avatar.locker)
              .orElse(findFunc(player))
              .orElse(accessedContainer match {
                case Some(parent: PlanetSideServerObject) =>
                  findFunc(parent)
                case _ =>
                  None
              })
          },
          destination.Fit(item)
        ) match {
          case (Some((source, Some(_))), Some(dest)) =>
            ContainableMoveItem(player.Name, source, destination, item, dest)
          case (None, _) =>
            log.error(s"LootItem: ${player.Name} can not find where $item is put currently")
          case (_, None) =>
            log.error(s"LootItem: ${player.Name} can not find anywhere to put $item in $destination")
          case _ =>
            log.error(
              s"LootItem: ${player.Name}wanted to move $itemGuid to $targetGuid, but multiple problems were encountered"
            )
        }
      case (Some(obj), _) =>
        log.error(s"LootItem: item $obj is (probably) not lootable to ${player.Name}")
      case (None, _) => ()
      case (_, None) =>
        log.error(s"LootItem: ${player.Name} can not find where to put $itemGuid")
    }
  }

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit = {
    val AvatarImplantMessage(_, action, slot, status) = pkt
    if (action == ImplantAction.Activation) {
      if (sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing) {
        //do not activate; play deactivation sound instead
        sessionLogic.zoning.spawn.stopDeconstructing()
        avatar.implants(slot).collect {
          case implant if implant.active =>
            avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
          case implant =>
            sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2))
        }
      } else {
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_implant")
        avatar.implants(slot) match {
          case Some(implant) =>
            if (status == 1) {
              avatarActor ! AvatarActor.ActivateImplant(implant.definition.implantType)
            } else {
              avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
            }
          case _ =>
            log.error(s"AvatarImplantMessage: ${player.Name} has an unknown implant in $slot")
        }
      }
    }
  }

  def handleUseItem(pkt: UseItemMessage): Unit = {
    val equipment = findContainedEquipment(pkt.item_used_guid) match {
      case (o @ Some(_), a) if a.exists(_.isInstanceOf[Tool]) =>
        sessionLogic.shooting.FindEnabledWeaponsToHandleWeaponFireAccountability(o, a.collect { case w: Tool => w })._2.headOption
      case (Some(_), a) =>
        a.headOption
      case _ =>
        None
    }
    sessionLogic.validObject(pkt.object_guid, decorator = "UseItem") match {
      case Some(door: Door) =>
        handleUseDoor(door, equipment)
      case Some(resourceSilo: ResourceSilo) =>
        handleUseResourceSilo(resourceSilo, equipment)
      case Some(panel: IFFLock) =>
        handleUseGeneralEntity(panel, equipment)
      case Some(obj: Player) =>
        handleUsePlayer(obj, equipment, pkt)
      case Some(locker: Locker) =>
        handleUseLocker(locker, equipment, pkt)
      case Some(gen: Generator) =>
        handleUseGeneralEntity(gen, equipment)
      case Some(mech: ImplantTerminalMech) =>
        handleUseGeneralEntity(mech, equipment)
      case Some(captureTerminal: CaptureTerminal) =>
        handleUseCaptureTerminal(captureTerminal, equipment)
      case Some(obj: FacilityTurret) =>
        handleUseFacilityTurret(obj, equipment, pkt)
      case Some(obj: Vehicle) =>
        handleUseVehicle(obj, equipment, pkt)
      case Some(terminal: Terminal) =>
        handleUseTerminal(terminal, equipment, pkt)
      case Some(obj: SpawnTube) =>
        handleUseSpawnTube(obj, equipment)
      case Some(obj: SensorDeployable) =>
        handleUseGeneralEntity(obj, equipment)
      case Some(obj: TurretDeployable) =>
        handleUseGeneralEntity(obj, equipment)
      case Some(obj: TrapDeployable) =>
        handleUseGeneralEntity(obj, equipment)
      case Some(obj: ShieldGeneratorDeployable) =>
        handleUseGeneralEntity(obj, equipment)
      case Some(obj: TelepadDeployable) =>
        handleUseTelepadDeployable(obj, equipment, pkt)
      case Some(obj: Utility.InternalTelepad) =>
        handleUseInternalTelepad(obj, pkt)
      case Some(obj: CaptureFlag) =>
        handleUseCaptureFlag(obj)
      case Some(_: WarpGate) =>
        handleUseWarpGate(equipment)
      case Some(obj) =>
        handleUseDefaultEntity(obj, equipment)
      case None => ()
    }
  }

  def handleUnuseItem(pkt: UnuseItemMessage): Unit = {
    val UnuseItemMessage(_, objectGuid) = pkt
    sessionLogic.validObject(objectGuid, decorator = "UnuseItem") match {
      case Some(obj: Player) =>
        unaccessContainer(obj)
        sessionLogic.zoning.spawn.TryDisposeOfLootedCorpse(obj)
      case Some(obj: Container) =>
        // Make sure we don't unload the contents of the vehicle the player is seated in
        // An example scenario of this would be closing the trunk contents when rearming at a landing pad
        if (player.VehicleSeated.isEmpty || player.VehicleSeated.get != obj.GUID) {
          unaccessContainer(obj)
        }
      case _ => ()
    }
  }

  def handleDeployObject(pkt: DeployObjectMessage): Unit = {
    val DeployObjectMessage(guid, _, pos, orient, _) = pkt
    player.Holsters().find(slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid).flatMap { slot => slot.Equipment } match {
      case Some(obj: ConstructionItem) =>
        val ammoType = obj.AmmoType match {
          case DeployedItem.portable_manned_turret => GlobalDefinitions.PortableMannedTurret(player.Faction).Item
          case dtype                               => dtype
        }
        log.info(s"${player.Name} is constructing a $ammoType deployable")
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        val dObj: Deployable = Deployables.Make(ammoType)()
        dObj.Position = pos
        dObj.Orientation = orient
        dObj.WhichSide = player.WhichSide
        dObj.Faction = player.Faction
        dObj.AssignOwnership(player)
        val tasking: TaskBundle = dObj match {
          case turret: TurretDeployable =>
            GUIDTask.registerDeployableTurret(continent.GUID, turret)
          case _ =>
            GUIDTask.registerObject(continent.GUID, dObj)
        }
        TaskWorkflow.execute(CallBackForTask(tasking, continent.Deployables, Zone.Deployable.BuildByOwner(dObj, player, obj)))
      case Some(obj) =>
        log.warn(s"DeployObject: what is $obj, ${player.Name}?  It's not a construction tool!")
      case None =>
        log.error(s"DeployObject: nothing, ${player.Name}?  It's not a construction tool!")
    }
  }

  def handlePlanetsideAttribute(pkt: PlanetsideAttributeMessage): Unit = {
    val PlanetsideAttributeMessage(objectGuid, attributeType, attributeValue) = pkt
    sessionLogic.validObject(objectGuid, decorator = "PlanetsideAttribute") match {
      case Some(vehicle: Vehicle) if player.avatar.vehicle.contains(vehicle.GUID) =>
        vehicle.Actor ! ServerObject.AttributeMsg(attributeType, attributeValue)
      case Some(vehicle: Vehicle) =>
        log.warn(s"PlanetsideAttribute: ${player.Name} does not own vehicle ${vehicle.GUID} and can not change it")
      // Cosmetics options
      case Some(_: Player) if attributeType == 106 =>
        avatarActor ! AvatarActor.SetCosmetics(Cosmetic.valuesFromAttributeValue(attributeValue))
      case Some(obj) =>
        log.trace(s"PlanetsideAttribute: ${player.Name} does not know how to apply unknown attributes behavior $attributeType to ${obj.Definition.Name}")
      case _ => ()
    }
  }

  def handleGenericObjectAction(pkt: GenericObjectActionMessage): Unit = {
    val GenericObjectActionMessage(objectGuid, code) = pkt
    sessionLogic.validObject(objectGuid, decorator = "GenericObjectAction") match {
      case Some(vehicle: Vehicle)
        if vehicle.OwnerName.contains(player.Name) =>
        vehicle.Actor ! ServerObject.GenericObjectAction(objectGuid, code, Some(player.GUID))

      case Some(tool: Tool) =>
        if (code == 35 &&
          (tool.Definition == GlobalDefinitions.maelstrom || tool.Definition.Name.startsWith("aphelion_laser"))
        ) {
          //maelstrom primary fire mode discharge (no target)
          //aphelion_laser discharge (no target)
          sessionLogic.shooting.HandleWeaponFireAccountability(objectGuid, PlanetSideGUID(Projectile.baseUID))
        } else {
          sessionLogic.validObject(player.VehicleSeated, decorator = "GenericObjectAction/Vehicle") match {
            case Some(vehicle: Vehicle)
              if vehicle.OwnerName.contains(player.Name) =>
              vehicle.Actor ! ServerObject.GenericObjectAction(objectGuid, code, Some(tool))
            case _ =>
          }
        }
      case _ =>
        log.info(s"${player.Name} - $pkt")
    }
  }

  def handleGenericObjectActionAtPosition(pkt: GenericObjectActionAtPositionMessage): Unit = {
    val GenericObjectActionAtPositionMessage(objectGuid, _, _) = pkt
    sessionLogic.validObject(objectGuid, decorator = "GenericObjectActionAtPosition") match {
      case Some(tool: Tool) if GlobalDefinitions.isBattleFrameNTUSiphon(tool.Definition) =>
        sessionLogic.shooting.FindContainedWeapon match {
          case (Some(vehicle: Vehicle), weps) if weps.exists(_.GUID == objectGuid) =>
            vehicle.Actor ! SpecialEmp.Burst()
          case _ => ()
        }
      case _ =>
        log.info(s"${player.Name} - $pkt")
    }
  }

  def handleGenericObjectState(pkt: GenericObjectStateMsg): Unit = {
    val GenericObjectStateMsg(_, _) = pkt
    log.info(s"${player.Name} - $pkt")
  }

  def handleGenericAction(pkt: GenericActionMessage): Unit = {
    val GenericActionMessage(action) = pkt
    if (player == null) {
      if (action == GenericAction.AwayFromKeyboard_RCV) {
        log.debug("GenericObjectState: AFK state reported during login")
      }
    } else {
      val (toolOpt, definition) = player.Slot(0).Equipment match {
        case Some(tool: Tool) =>
          (Some(tool), tool.Definition)
        case _ =>
          (None, GlobalDefinitions.bullet_9mm)
      }
      action match {
        case GenericAction.DropSpecialItem =>
          dropSpecialSlotItem()
        case GenericAction.MaxAnchorsExtend_RCV =>
          log.info(s"${player.Name} has anchored ${player.Sex.pronounObject}self to the ground")
          player.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttribute(player.GUID, 19, 1)
          )
          definition match {
            case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.trhev_burster =>
              val tool = toolOpt.get
              tool.ToFireMode = 1
              sendResponse(ChangeFireModeMessage(tool.GUID, 1))
            case GlobalDefinitions.trhev_pounder =>
              val tool = toolOpt.get
              val convertFireModeIndex = if (tool.FireModeIndex == 0) { 1 }
              else { 4 }
              tool.ToFireMode = convertFireModeIndex
              sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
            case _ =>
              log.warn(s"GenericObject: ${player.Name} is a MAX with an unexpected attachment - ${definition.Name}")
          }
        case GenericAction.MaxAnchorsRelease_RCV =>
          log.info(s"${player.Name} has released the anchors")
          player.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttribute(player.GUID, 19, 0)
          )
          definition match {
            case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.trhev_burster =>
              val tool = toolOpt.get
              tool.ToFireMode = 0
              sendResponse(ChangeFireModeMessage(tool.GUID, 0))
            case GlobalDefinitions.trhev_pounder =>
              val tool = toolOpt.get
              val convertFireModeIndex = if (tool.FireModeIndex == 1) { 0 } else { 3 }
              tool.ToFireMode = convertFireModeIndex
              sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
            case _ =>
              log.warn(s"GenericObject: $player is MAX with an unexpected attachment - ${definition.Name}")
          }
        case GenericAction.MaxSpecialEffect_RCV =>
          if (player.ExoSuit == ExoSuitType.MAX) {
            toggleMaxSpecialState(enable = true)
          } else {
            log.warn(s"GenericActionMessage: ${player.Name} can't handle MAX special effect")
          }
        case GenericAction.StopMaxSpecialEffect_RCV =>
          if (player.ExoSuit == ExoSuitType.MAX) {
            player.Faction match {
              case PlanetSideEmpire.NC =>
                toggleMaxSpecialState(enable = false)
              case _ =>
                log.warn(s"GenericActionMessage: ${player.Name} tried to cancel an uncancellable MAX special ability")
            }
          } else {
            log.warn(s"GenericActionMessage: ${player.Name} can't stop MAX special effect")
          }
        case GenericAction.AwayFromKeyboard_RCV =>
          log.info(s"${player.Name} is AFK")
          AvatarActor.savePlayerLocation(player)
          displayCharSavedMsgThenRenewTimer(fixedLen=1800L, varLen=0L) //~30min
          player.AwayFromKeyboard = true
        case GenericAction.BackInGame_RCV =>
          log.info(s"${player.Name} is back")
          player.AwayFromKeyboard = false
          renewCharSavedTimer(
            Config.app.game.savedMsg.renewal.fixed,
            Config.app.game.savedMsg.renewal.variable
          )
        case GenericAction.LookingForSquad_RCV => //Looking For Squad ON
          if (!avatar.lookingForSquad && (sessionLogic.squad.squadUI.isEmpty || sessionLogic.squad.squadUI(player.CharId).index == 0)) {
            avatarActor ! AvatarActor.SetLookingForSquad(true)
          }
        case GenericAction.NotLookingForSquad_RCV => //Looking For Squad OFF
          if (avatar.lookingForSquad && (sessionLogic.squad.squadUI.isEmpty || sessionLogic.squad.squadUI(player.CharId).index == 0)) {
            avatarActor ! AvatarActor.SetLookingForSquad(false)
          }
        case _ =>
          log.warn(s"GenericActionMessage: ${player.Name} can't handle $action")
      }
    }
  }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = {
    val FavoritesRequest(_, loadoutType, action, line, label) = pkt
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    action match {
      case FavoritesAction.Save   =>
        avatarActor ! AvatarActor.SaveLoadout(player, loadoutType, label, line)
      case FavoritesAction.Delete =>
        avatarActor ! AvatarActor.DeleteLoadout(player, loadoutType, line)
      case FavoritesAction.Unknown =>
        log.warn(s"FavoritesRequest: ${player.Name} requested an unknown favorites action")
    }
  }

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit = {
    val GenericCollisionMsg(ctype, p, _, ppos, pv, t, _, tpos, tv, _, _, _) = pkt
    val fallHeight = {
      if (pv.z * pv.z >= (pv.x * pv.x + pv.y * pv.y) * 0.5f) {
        if (heightTrend) {
          val fall = heightLast - heightHistory
          heightHistory = heightLast
          fall
        }
        else {
          val fall = heightHistory - heightLast
          heightLast = heightHistory
          fall
        }
      } else {
        0f
      }
    }
    val (target1, target2, bailProtectStatus, velocity) = (ctype, sessionLogic.validObject(p, decorator = "GenericCollision/Primary")) match {
      case (CollisionIs.OfInfantry, out @ Some(user: Player))
        if user == player =>
        val bailStatus = session.flying || player.spectator || session.speed > 1f || player.BailProtection
        player.BailProtection = false
        val v = if (player.avatar.implants.exists {
          case Some(implant) => implant.definition.implantType == ImplantType.Surge && implant.active
          case _             => false
        }) {
          Vector3.Zero
        } else {
          pv
        }
        (out, None, bailStatus, v)
      case (CollisionIs.OfGroundVehicle, out @ Some(v: Vehicle))
        if v.Seats(0).occupant.contains(player) =>
        val bailStatus = v.BailProtection
        v.BailProtection = false
        (out, sessionLogic.validObject(t, decorator = "GenericCollision/GroundVehicle"), bailStatus, pv)
      case (CollisionIs.OfAircraft, out @ Some(v: Vehicle))
        if v.Definition.CanFly && v.Seats(0).occupant.contains(player) =>
        (out, sessionLogic.validObject(t, decorator = "GenericCollision/Aircraft"), false, pv)
      case (CollisionIs.BetweenThings, _) =>
        log.warn("GenericCollision: CollisionIs.BetweenThings detected - no handling case")
        (None, None, false, Vector3.Zero)
      case _ =>
        (None, None, false, Vector3.Zero)
    }
    val curr = System.currentTimeMillis()
    (target1, t, target2) match {
      case (None, _, _) => ()

      case (Some(us: PlanetSideServerObject with Vitality with FactionAffinity), PlanetSideGUID(0), _) =>
        if (updateCollisionHistoryForTarget(us, curr)) {
          if (!bailProtectStatus) {
            sessionLogic.handleDealingDamage(
              us,
              DamageInteraction(
                SourceEntry(us),
                CollisionReason(velocity, fallHeight, us.DamageModel),
                ppos
              )
            )
          }
        }

      case (Some(us: Vehicle), _, Some(victim: SensorDeployable)) =>
        collisionBetweenVehicleAndFragileDeployable(us, ppos, victim, tpos, velocity - tv, fallHeight, curr)

      case (Some(us: Vehicle), _, Some(victim: TurretDeployable)) if victim.Seats.isEmpty =>
        collisionBetweenVehicleAndFragileDeployable(us, ppos, victim, tpos, velocity - tv, fallHeight, curr)

      case (
        Some(us: PlanetSideServerObject with Vitality with FactionAffinity), _,
        Some(victim: PlanetSideServerObject with Vitality with FactionAffinity)
        ) =>
        if (updateCollisionHistoryForTarget(victim, curr)) {
          val usSource = SourceEntry(us)
          val victimSource = SourceEntry(victim)
          //we take damage from the collision
          if (!bailProtectStatus) {
            performCollisionWithSomethingDamage(us, usSource, ppos, victimSource, fallHeight, velocity - tv)
          }
          //get dealt damage from our own collision (no protection)
          collisionHistory.put(us.Actor, curr)
          performCollisionWithSomethingDamage(victim, victimSource, tpos, usSource, fallHeight = 0f, tv - velocity)
        }

      case _ => ()
    }
  }

  def handleAvatarFirstTimeEvent(pkt: AvatarFirstTimeEventMessage): Unit = {
    val AvatarFirstTimeEventMessage(_, _, _, eventName) = pkt
    avatarActor ! AvatarActor.AddFirstTimeEvent(eventName)
  }

  def handleBugReport(pkt: PlanetSideGamePacket): Unit = {
    val BugReportMessage(
    _/*versionMajor*/,
    _/*versionMinor*/,
    _/*versionDate*/,
    _/*bugType*/,
    _/*repeatable*/,
    _/*location*/,
    _/*zone*/,
    _/*pos*/,
    _/*summary*/,
    _/*desc*/
    ) = pkt
    log.warn(s"${player.Name} filed a bug report - it might be something important")
    log.debug(s"$pkt")
  }

  def handleFacilityBenefitShieldChargeRequest(pkt: FacilityBenefitShieldChargeRequestMessage): Unit = {
    val FacilityBenefitShieldChargeRequestMessage(_) = pkt
    val vehicleGuid = player.VehicleSeated
    continent
      .GUID(vehicleGuid)
      .foreach {
        case obj: Vehicle if !obj.Destroyed && obj.MountedIn.isEmpty => // vehicle will try to charge even if destroyed & cargo vehicles need to be excluded
          obj.Actor ! CommonMessages.ChargeShields(
            15,
            Some(continent.blockMap.sector(obj).buildingList.maxBy(_.Definition.SOIRadius))
          )
        case obj: Vehicle if obj.MountedIn.nonEmpty =>
          false
        case _ if vehicleGuid.nonEmpty =>
          log.warn(
            s"FacilityBenefitShieldChargeRequest: ${player.Name} can not find vehicle ${vehicleGuid.get.guid} in zone ${continent.id}"
          )
        case _ =>
          log.warn(s"FacilityBenefitShieldChargeRequest: ${player.Name} is not seated in a vehicle")
      }
  }

  def handleBattleplan(pkt: BattleplanMessage): Unit = {
    val BattleplanMessage(_, name, _, _) = pkt
    val lament: String = s"$name has a brilliant idea that no one will ever see"
    log.info(lament)
    log.debug(s"Battleplan: $lament - $pkt")
  }

  def handleBindPlayer(pkt: BindPlayerMessage): Unit = {
    val BindPlayerMessage(_, _, _, _, _, _, _, _) = pkt
  }

  def handleCreateShortcut(pkt: CreateShortcutMessage): Unit = {
    val CreateShortcutMessage(_, slot, shortcutOpt) = pkt
    shortcutOpt match {
      case Some(shortcut) =>
        avatarActor ! AvatarActor.AddShortcut(slot - 1, shortcut)
      case None =>
        avatarActor ! AvatarActor.RemoveShortcut(slot - 1)
    }
  }

  def handleChangeShortcutBank(pkt: ChangeShortcutBankMessage): Unit = {
    val ChangeShortcutBankMessage(_, _) = pkt
  }

  def handleFriendRequest(pkt: FriendsRequest): Unit = {
    val FriendsRequest(action, name) = pkt
    avatarActor ! AvatarActor.MemberListRequest(action, name)
  }

  def handleInvalidTerrain(pkt: InvalidTerrainMessage): Unit = {
    val InvalidTerrainMessage(_, vehicleGuid, alert, _) = pkt
    (continent.GUID(vehicleGuid), continent.GUID(player.VehicleSeated)) match {
      case (Some(packetVehicle: Vehicle), Some(playerVehicle: Vehicle)) if packetVehicle eq playerVehicle =>
        if (alert == TerrainCondition.Unsafe) {
          log.info(s"${player.Name}'s ${packetVehicle.Definition.Name} is approaching terrain unsuitable for idling")
        }
      case (Some(packetVehicle: Vehicle), Some(_: Vehicle)) =>
        if (alert == TerrainCondition.Unsafe) {
          log.info(s"${packetVehicle.Definition.Name}@${packetVehicle.GUID} is approaching terrain unsuitable for idling, but is not ${player.Name}'s vehicle")
        }
      case (Some(_: Vehicle), _) =>
        log.warn(s"InvalidTerrain: ${player.Name} is not seated in a(ny) vehicle near unsuitable terrain")
      case (Some(packetThing), _) =>
        log.warn(s"InvalidTerrain: ${player.Name} thinks that ${packetThing.Definition.Name}@${packetThing.GUID} is near unsuitable terrain")
      case _ =>
        log.error(s"InvalidTerrain: ${player.Name} is complaining about a thing@$vehicleGuid that can not be found")
    }
  }

  def handleActionCancel(pkt: ActionCancelMessage): Unit = {
    val ActionCancelMessage(_, _, _) = pkt
    progressBarUpdate.cancel()
    progressBarValue = None
  }

  def handleTrade(pkt: TradeMessage): Unit = {
    val TradeMessage(trade) = pkt
    log.trace(s"${player.Name} wants to trade for some reason - $trade")
  }

  def handleDisplayedAward(pkt: DisplayedAwardMessage): Unit = {
    val DisplayedAwardMessage(_, ribbon, bar) = pkt
    log.trace(s"${player.Name} changed the $bar displayed award ribbon to $ribbon")
    avatarActor ! AvatarActor.SetRibbon(ribbon, bar)
  }

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit = {
    val ObjectDetectedMessage(_, _, _, targets) = pkt
    sessionLogic.shooting.FindWeapon.foreach {
      case weapon if weapon.Projectile.AutoLock =>
        //projectile with auto-lock instigates a warning on the target
        val detectedTargets = sessionLogic.shooting.FindDetectedProjectileTargets(targets)
        val mode = 7 + (weapon.Projectile == GlobalDefinitions.wasp_rocket_projectile)
        detectedTargets.foreach { target =>
          continent.AvatarEvents ! AvatarServiceMessage(target, AvatarAction.ProjectileAutoLockAwareness(mode))
        }
      case _ => ()
    }
  }

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit = {
    val TargetingImplantRequest(list) = pkt
    val targetInfo: List[TargetInfo] = list.flatMap { x =>
      continent.GUID(x.target_guid) match {
        case Some(player: Player) =>
          val health = player.Health.toFloat / player.MaxHealth
          val armor = if (player.MaxArmor > 0) {
            player.Armor.toFloat / player.MaxArmor
          } else {
            0
          }
          Some(TargetInfo(player.GUID, health, armor))
        case _ =>
          log.warn(
            s"TargetingImplantRequest: the info that ${player.Name} requested for target ${x.target_guid} is not for a player"
          )
          None
      }
    }
    sendResponse(TargetingInfoMessage(targetInfo))
  }

  def handleHitHint(pkt: HitHint): Unit = {
    val HitHint(_, _) = pkt
  }

  /* messages */

  def handleSetAvatar(avatar: Avatar): Unit = {
    session = session.copy(avatar = avatar)
    if (session.player != null) {
      session.player.avatar = avatar
    }
    LivePlayerList.Update(avatar.id, avatar)
  }

  def handleReceiveAccountData(account: Account): Unit = {
    log.trace(s"ReceiveAccountData $account")
    session = session.copy(account = account)
    avatarActor ! AvatarActor.SetAccount(account)
  }

  def handleUseCooldownRenew: BasicDefinition => Unit = {
    case _: KitDefinition => kitToBeUsed = None
    case _ => ()
  }

  def handleAvatarResponse(avatar: Avatar): Unit = {
    session = session.copy(avatar = avatar)
    sessionLogic.accountPersistence ! AccountPersistenceService.Login(avatar.name, avatar.id)
  }

  def handleSetSpeed(speed: Float): Unit = {
    session = session.copy(speed = speed)
  }

  def handleSetFlying(flying: Boolean): Unit = {
    session = session.copy(flying = flying)
  }

  def handleSetSpectator(spectator: Boolean): Unit = {
    session.player.spectator = spectator
  }

  def handleKick(player: Player, time: Option[Long]): Unit = {
    administrativeKick(player)
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(player.Name, time)
  }

  def handleSilenced(isSilenced: Boolean): Unit = {
    player.silenced = isSilenced
  }

  /* supporting functions */

  /**
   * Enforce constraints on bulk purchases as determined by a given player's previous purchase times and hard acquisition delays.
   * Intended to assist in sanitizing loadout information from the perspective of the player, or target owner.
   * The equipment is expected to be unregistered and already fitted to their ultimate slot in the target container.
   * @param player the player whose purchasing constraints are to be tested
   * @param target the location in which the equipment will be stowed
   * @param slots  the equipment, in the standard object-slot format container
   */
  def applyPurchaseTimersBeforePackingLoadout(
                                               player: Player,
                                               target: PlanetSideServerObject with Container,
                                               slots: List[InventoryItem]
                                             ): Unit = {
    slots.foreach { item =>
      player.avatar.purchaseCooldown(item.obj.Definition) match {
        case Some(_) => ()
        case None if Avatar.purchaseCooldowns.contains(item.obj.Definition) =>
          avatarActor ! AvatarActor.UpdatePurchaseTime(item.obj.Definition)
          TaskWorkflow.execute(PutLoadoutEquipmentInInventory(target)(item.obj, item.start))
        case None =>
          TaskWorkflow.execute(PutLoadoutEquipmentInInventory(target)(item.obj, item.start))
      }
    }
  }

  private def handleUseDoor(door: Door, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
        val distance: Float = math.max(
          Config.app.game.doorsCanBeOpenedByMedAppFromThisDistance,
          door.Definition.initialOpeningDistance
        )
        door.Actor ! CommonMessages.Use(player, Some(distance))
      case _ =>
        door.Actor ! CommonMessages.Use(player)
    }
  }

  private def handleUseResourceSilo(resourceSilo: ResourceSilo, equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    (continent.GUID(player.VehicleSeated), equipment) match {
      case (Some(vehicle: Vehicle), Some(item))
        if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
        resourceSilo.Actor ! CommonMessages.Use(player, equipment)
      case _ =>
        resourceSilo.Actor ! CommonMessages.Use(player)
    }
  }

  private def handleUsePlayer(obj: Player, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    if (obj.isBackpack) {
      if (equipment.isEmpty) {
        log.info(s"${player.Name} is looting the corpse of ${obj.Name}")
        sendResponse(msg)
        accessContainer(obj)
      }
    } else if (!msg.unk3 && player.isAlive) { //potential kit use
      (continent.GUID(msg.item_used_guid), kitToBeUsed) match {
        case (Some(kit: Kit), None) =>
          kitToBeUsed = Some(msg.item_used_guid)
          player.Actor ! CommonMessages.Use(player, Some(kit))
        case (Some(_: Kit), Some(_)) | (None, Some(_)) =>
          //a kit is already queued to be used; ignore this request
          sendResponse(ChatMsg(ChatMessageType.UNK_225, wideContents=false, "", "Please wait ...", None))
        case (Some(item), _) =>
          log.error(s"UseItem: ${player.Name} looking for Kit to use, but found $item instead")
        case (None, None) =>
          log.warn(s"UseItem: anticipated a Kit ${msg.item_used_guid} for ${player.Name}, but can't find it")              }
    } else if (msg.object_id == ObjectClass.avatar && msg.unk3) {
      equipment match {
        case Some(tool: Tool) if tool.Definition == GlobalDefinitions.bank =>
          obj.Actor ! CommonMessages.Use(player, equipment)

        case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
          obj.Actor ! CommonMessages.Use(player, equipment)
        case _ => ()
      }
    }
  }

  private def handleUseLocker(locker: Locker, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(locker, item)
      case None if locker.Faction == player.Faction || locker.HackedBy.nonEmpty =>
        log.info(s"${player.Name} is accessing a locker")
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        val playerLocker = player.avatar.locker
        sendResponse(msg.copy(object_guid = playerLocker.GUID, object_id = 456))
        accessContainer(playerLocker)
      case _ => ()
    }
  }

  private def handleUseCaptureTerminal(captureTerminal: CaptureTerminal, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(captureTerminal, item)
      case _ if specialItemSlotGuid.nonEmpty =>
        continent.GUID(specialItemSlotGuid) match {
          case Some(llu: CaptureFlag) =>
            if (llu.Target.GUID == captureTerminal.Owner.GUID) {
              continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.LluCaptured(llu))
            } else {
              log.info(
                s"LLU target is not this base. Target GUID: ${llu.Target.GUID} This base: ${captureTerminal.Owner.GUID}"
              )
            }
          case _ => log.warn("Item in specialItemSlotGuid is not registered with continent or is not a LLU")
        }
      case _ => ()
    }
  }

  private def handleUseFacilityTurret(obj: FacilityTurret, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment.foreach { item =>
      sendUseGeneralEntityMessage(obj, item)
      obj.Actor ! CommonMessages.Use(player, Some((item, msg.unk2.toInt))) //try upgrade path
    }
  }

  private def handleUseVehicle(obj: Vehicle, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(obj, item)
      case None if player.Faction == obj.Faction =>
        //access to trunk
        if (
          obj.AccessingTrunk.isEmpty &&
            (!obj.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked) || obj.OwnerGuid
              .contains(player.GUID))
        ) {
          log.info(s"${player.Name} is looking in the ${obj.Definition.Name}'s trunk")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          obj.AccessingTrunk = player.GUID
          accessContainer(obj)
          sendResponse(msg)
        }
      case _ => ()
    }
  }

  private def handleUseTerminal(terminal: Terminal, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(terminal, item)
      case None
        if terminal.Owner == Building.NoBuilding || terminal.Faction == player.Faction ||
          terminal.HackedBy.nonEmpty || terminal.Faction == PlanetSideEmpire.NEUTRAL =>
        val tdef = terminal.Definition
        if (tdef.isInstanceOf[MatrixTerminalDefinition]) {
          //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          sendResponse(
            BindPlayerMessage(BindStatus.Bind, "", display_icon=true, logging=true, SpawnGroup.Sanctuary, 0, 0, terminal.Position)
          )
        } else if (
          tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
            tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal
        ) {
          findLocalVehicle match {
            case Some(vehicle) =>
              log.info(
                s"${player.Name} is accessing a ${terminal.Definition.Name} for ${player.Sex.possessive} ${vehicle.Definition.Name}"
              )
              sendResponse(msg)
              sendResponse(msg.copy(object_guid = vehicle.GUID, object_id = vehicle.Definition.ObjectId))
            case None =>
              log.error(s"UseItem: Expecting a seated vehicle, ${player.Name} found none")
          }
        } else if (tdef == GlobalDefinitions.teleportpad_terminal) {
          //explicit request
          log.info(s"${player.Name} is purchasing a router telepad")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          terminal.Actor ! Terminal.Request(
            player,
            ItemTransactionMessage(msg.object_guid, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
          )
        } else if (tdef == GlobalDefinitions.targeting_laser_dispenser) {
          //explicit request
          log.info(s"${player.Name} is purchasing a targeting laser")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          terminal.Actor ! Terminal.Request(
            player,
            ItemTransactionMessage(msg.object_guid, TransactionType.Buy, 0, "flail_targeting_laser", 0, PlanetSideGUID(0))
          )
        } else {
          log.info(s"${player.Name} is accessing a ${terminal.Definition.Name}")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          sendResponse(msg)
        }
      case _ => ()
    }
  }

  private def handleUseSpawnTube(obj: SpawnTube, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(obj, item)
      case None if player.Faction == obj.Faction =>
        //deconstruction
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        sessionLogic.actionsToCancel()
        sessionLogic.terminals.CancelAllProximityUnits()
        sessionLogic.zoning.spawn.startDeconstructing(obj)
      case _ => ()
    }
  }

  private def handleUseTelepadDeployable(obj: TelepadDeployable, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    if (equipment.isEmpty) {
      (continent.GUID(obj.Router) match {
        case Some(vehicle: Vehicle) => Some((vehicle, vehicle.Utility(UtilityType.internal_router_telepad_deployable)))
        case Some(vehicle) => Some(vehicle, None)
        case None => None
      }) match {
        case Some((vehicle: Vehicle, Some(util: Utility.InternalTelepad))) =>
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
          player.WhichSide = vehicle.WhichSide
          useRouterTelepadSystem(
            router = vehicle,
            internalTelepad = util,
            remoteTelepad = obj,
            src = obj,
            dest = util
          )
        case Some((vehicle: Vehicle, None)) =>
          log.error(
            s"telepad@${msg.object_guid.guid} is not linked to a router - ${vehicle.Definition.Name}"
          )
        case Some((o, _)) =>
          log.error(
            s"telepad@${msg.object_guid.guid} is linked to wrong kind of object - ${o.Definition.Name}, ${obj.Router}"
          )
          obj.Actor ! Deployable.Deconstruct()
        case _ => ()
      }
    }
  }

  private def handleUseInternalTelepad(obj: InternalTelepad, msg: UseItemMessage): Unit = {
    continent.GUID(obj.Telepad) match {
      case Some(pad: TelepadDeployable) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
        player.WhichSide = pad.WhichSide
        useRouterTelepadSystem(
          router = obj.Owner.asInstanceOf[Vehicle],
          internalTelepad = obj,
          remoteTelepad = pad,
          src = obj,
          dest = pad
        )
      case Some(o) =>
        log.error(
          s"internal telepad@${msg.object_guid.guid} is not linked to a remote telepad - ${o.Definition.Name}@${o.GUID.guid}"
        )
      case None => ()
    }
  }

  private def handleUseCaptureFlag(obj: CaptureFlag): Unit = {
    // LLU can normally only be picked up the faction that owns it
    specialItemSlotGuid match {
      case None if obj.Faction == player.Faction =>
        specialItemSlotGuid = Some(obj.GUID)
        player.Carrying = SpecialCarry.CaptureFlag
        continent.LocalEvents ! CaptureFlagManager.PickupFlag(obj, player)
      case None =>
        log.warn(s"${player.Faction} player ${player.toString} tried to pick up a ${obj.Faction} LLU -  ${obj.GUID}")
      case Some(guid) if guid != obj.GUID =>
        // Ignore duplicate pickup requests
        log.warn(
          s"${player.Faction} player ${player.toString} tried to pick up a ${obj.Faction} LLU, but their special slot already contains $guid"
        )
      case _ => ()
    }
  }

  private def handleUseWarpGate(equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    (continent.GUID(player.VehicleSeated), equipment) match {
      case (Some(vehicle: Vehicle), Some(item))
        if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
        vehicle.Actor ! CommonMessages.Use(player, equipment)
      case _ => ()
    }
  }

  private def handleUseGeneralEntity(obj: PlanetSideServerObject, equipment: Option[Equipment]): Unit = {
    equipment.foreach { item =>
      sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
      obj.Actor ! CommonMessages.Use(player, Some(item))
    }
  }

  private def sendUseGeneralEntityMessage(obj: PlanetSideServerObject, equipment: Equipment): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    obj.Actor ! CommonMessages.Use(player, Some(equipment))
  }

  private def handleUseDefaultEntity(obj: PlanetSideGameObject, equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    equipment match {
      case Some(item)
        if GlobalDefinitions.isBattleFrameArmorSiphon(item.Definition) ||
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) => ()
      case _ =>
        log.warn(s"UseItem: ${player.Name} does not know how to handle $obj")
    }
  }

  def dropSpecialSlotItem(): Unit = {
    specialItemSlotGuid.foreach { guid =>
      specialItemSlotGuid = None
      player.Carrying = None
      (continent.GUID(guid) match {
        case Some(llu: CaptureFlag) => Some((llu, llu.Carrier))
        case _ => None
      }) match {
        case Some((llu, Some(carrier: Player)))
          if carrier.GUID == player.GUID && !player.isAlive =>
          player.LastDamage.foreach { damage =>
            damage
              .interaction
              .adversarial
              .map { _.attacker }
              .collect {
                case attacker
                  if attacker.Faction != player.Faction &&
                    System.currentTimeMillis() - llu.LastCollectionTime >= Config.app.game.experience.cep.lluSlayerCreditDuration.toMillis =>
                  continent.AvatarEvents ! AvatarServiceMessage(
                    attacker.Name,
                    AvatarAction.AwardCep(attacker.CharId, Config.app.game.experience.cep.lluSlayerCredit)
                  )
              }
          }
          continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
        case Some((llu, Some(carrier: Player))) if carrier.GUID == player.GUID =>
          continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
        case Some((_, Some(carrier: Player))) =>
          log.warn(s"${player.toString} tried to drop LLU, but it is currently held by ${carrier.toString}")
        case Some((_, None)) =>
          log.warn(s"${player.toString} tried to drop LLU, but nobody is holding it.")
        case None =>
          log.warn(s"${player.toString} tried to drop a special item that wasn't recognized. GUID: $guid")
      }
    }
  }

  def setupProgressChange(rate: Float, finishedAction: () => Unit, stepAction: Float => Boolean): Unit = {
    if (progressBarValue.isEmpty) {
      progressBarValue = Some(-rate)
      context.self ! CommonMessages.ProgressEvent(rate, finishedAction, stepAction)
    }
  }

  /**
   * Handle the message that indicates the level of completion of a process.
   * The process is any form of user-driven activity with a certain eventual outcome
   * but indeterminate progress feedback per cycle.<br>
   * <br>
   * This task is broken down into the "progression" from its initial state to the eventual outcome
   * as is reported back to the player through some means of messaging window feedback.
   * Though common in practice, this is not a requirement
   * and the progress can accumulate without a user reportable method.
   * To ensure that completion is reported properly,
   * an exception is made that 99% completion is accounted uniquely
   * before the final 100% is achieved.
   * If the background process recording value is never set before running the initial operation
   * or gets unset by failing a `tickAction` check
   * the process is stopped.
   * @see `progressBarUpdate`
   * @see `progressBarValue`
   * @see `essionActor.Progress`
   * @param delta            how much the progress changes each tick
   * @param completionAction a custom action performed once the process is completed
   * @param tickAction       an optional action is is performed for each tick of progress;
   *                         also performs a continuity check to determine if the process has been disrupted
   */
  def handleProgressChange(
                            delta: Float,
                            completionAction: () => Unit,
                            tickAction: Float => Boolean,
                            tick: Long
                          ): Unit = {
    progressBarUpdate.cancel()
    progressBarValue.foreach { value =>
      val next = value + delta
      if (value >= 100f) {
        //complete
        progressBarValue = None
        tickAction(100)
        completionAction()
      } else if (value < 100f && next >= 100f) {
        if (tickAction(99)) {
          //will complete after this turn
          progressBarValue = Some(next)
          import scala.concurrent.ExecutionContext.Implicits.global
          progressBarUpdate = context.system.scheduler.scheduleOnce(
            delay = 100 milliseconds,
            context.self,
            CommonMessages.ProgressEvent(delta, completionAction, tickAction)
          )
        } else {
          progressBarValue = None
        }
      } else {
        if (tickAction(next)) {
          //normal progress activity
          progressBarValue = Some(next)
          import scala.concurrent.ExecutionContext.Implicits.global
          progressBarUpdate = context.system.scheduler.scheduleOnce(
            tick.milliseconds,
            context.self,
            CommonMessages.ProgressEvent(delta, completionAction, tickAction, tick)
          )
        } else {
          progressBarValue = None
        }
      }
    }
  }

  def accessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        accessVehicleContents(v)
      case o: LockerContainer =>
        accessGenericContainer(o)
      case p: Player if p.isBackpack =>
        accessCorpseContents(p)
      case p: PlanetSideServerObject with Container =>
        accessedContainer = Some(p)
      case _ => ()
    }
  }

  private def accessGenericContainer(container: PlanetSideServerObject with Container): Unit = {
    accessedContainer = Some(container)
    displayContainerContents(container.GUID, container.Inventory.Items)
  }

  /**
   * Common preparation for interfacing with a vehicle trunk.
   * Join a vehicle-specific group for shared updates.
   * Construct every object in the vehicle's inventory for shared manipulation updates.
   * @see `Container.Inventory`
   * @see `GridInventory.Items`
   * @param vehicle the vehicle
   */
  private def accessVehicleContents(vehicle: Vehicle): Unit = {
    accessedContainer = Some(vehicle)
    accessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    displayContainerContents(vehicle.GUID, vehicle.Inventory.Items)
  }

  /**
   * Common preparation for interfacing with a corpse (former player's backpack).
   * Join a corpse-specific group for shared updates.
   * Construct every object in the player's hands and inventory for shared manipulation updates.
   * @see `Container.Inventory`
   * @see `GridInventory.Items`
   * @see `Player.HolsterItems`
   * @param tplayer the corpse
   */
  private def accessCorpseContents(tplayer: Player): Unit = {
    accessedContainer = Some(tplayer)
    accessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    displayContainerContents(tplayer.GUID, tplayer.HolsterItems())
    displayContainerContents(tplayer.GUID, tplayer.Inventory.Items)
  }

  /**
   * Join an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  private def accessContainerChannel(events: ActorRef, channel: String): Unit = {
    events ! Service.Join(channel)
  }

  /**
   * Depict the contents of a container by building them in the local client
   * in their container as a group of detailed entities.
   * @see `ObjectCreateDetailedMessage`
   * @see `ObjectCreateMessageParent`
   * @see `PacketConverter.DetailedConstructorData`
   * @param containerId the container's unique identifier
   * @param items a list of the entities to be depicted
   */
  private def displayContainerContents(containerId: PlanetSideGUID, items: Iterable[InventoryItem]): Unit = {
    items.foreach(entry => {
      val obj    = entry.obj
      val objDef = obj.Definition
      sendResponse(
        ObjectCreateDetailedMessage(
          objDef.ObjectId,
          obj.GUID,
          ObjectCreateMessageParent(containerId, entry.start),
          objDef.Packet.DetailedConstructorData(obj).get
        )
      )
    })
  }

  /**
   * For whatever conatiner the character considers itself accessing,
   * initiate protocol to release it from "access".
   */
  def unaccessContainer(): Unit = {
    accessedContainer.foreach { container => unaccessContainer(container) }
  }

  /**
   * For the target container, initiate protocol to release it from "access".
   */
  def unaccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        unaccessVehicleContainer(v)
      case o: LockerContainer =>
        unaccessGenericContainer(o)
        avatarActor ! AvatarActor.SaveLocker()
      case p: Player if p.isBackpack =>
        unaccessCorpseContainer(p)
      case _: PlanetSideServerObject with Container =>
        accessedContainer = None
      case _ => ()
    }
  }

  private def unaccessGenericContainer(container: Container): Unit = {
    accessedContainer = None
    hideContainerContents(container.Inventory.Items)
  }

  /**
   * Common preparation for disengaging from a vehicle.
   * Leave the vehicle-specific group that was used for shared updates.
   * Deconstruct every object in the vehicle's inventory.
   * @param vehicle the vehicle
   */
  private def unaccessVehicleContainer(vehicle: Vehicle): Unit = {
    accessedContainer = None
    if (vehicle.AccessingTrunk.contains(player.GUID)) {
      vehicle.AccessingTrunk = None
    }
    unaccessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    hideContainerContents(vehicle.Inventory.Items)
  }

  /**
   * Common preparation for disengaging from a corpse.
   * Leave the corpse-specific group that was used for shared updates.
   * Deconstruct every object in the backpack's inventory.
   * @param tplayer the corpse
   */
  private def unaccessCorpseContainer(tplayer: Player): Unit = {
    accessedContainer = None
    unaccessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    hideContainerContents(tplayer.HolsterItems())
    hideContainerContents(tplayer.Inventory.Items)
  }

  /**
   * Leave an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  private def unaccessContainerChannel(events: ActorRef, channel: String): Unit = {
    events ! Service.Leave(Some(channel))
  }

  /**
   * Forget the contents of a container by deleting that content from the local client.
   * @see `InventoryItem`
   * @see `ObjectDeleteMessage`
   * @param items a list of the entities to be depicted
   */
  private def hideContainerContents(items: List[InventoryItem]): Unit = {
    items.foreach { entry =>
      sendResponse(ObjectDeleteMessage(entry.obj.GUID, 0))
    }
  }

  /**
   * Check two locations for a controlled piece of equipment that is associated with the `player`.<br>
   * <br>
   * The first location is dependent on whether the avatar is in a vehicle.
   * Some vehicle seats may have a "controlled weapon" which counts as the first location to be checked.
   * The second location is dependent on whether the avatar has a raised hand.
   * That is only possible if the player has something in their hand at the moment, hence the second location.
   * Players do have a concept called a "last drawn slot" (hand) but that former location is not eligible.<br>
   * <br>
   * Along with any discovered item, a containing object such that the statement:<br>
   *   `container.Find(object) = Some(slot)`<br>
   * ... will return a proper result.
   * For a mount controlled weapon, the vehicle is returned.
   * For the player's hand, the player is returned.
   * @return a `Tuple` of the returned values;
   *         the first value is a `Container` object;
   *         the second value is an `Equipment` object in the former
   */
  def findContainedEquipment(): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    continent.GUID(player.VehicleSeated) match {
      case Some(vehicle: Mountable with MountableWeapons with Container) =>
        vehicle.PassengerInSeat(player) match {
          case Some(seatNum) =>
            (Some(vehicle), vehicle.WeaponControlledFromSeat(seatNum))
          case None =>
            (None, Set.empty)
        }
      case _ =>
        player.Slot(player.DrawnSlot).Equipment match {
          case Some(a) =>
            (Some(player), Set(a))
          case _ =>
            (None, Set.empty)
        }
    }
  }

  /**
   * Check two locations for a controlled piece of equipment that is associated with the `player`
   * and has the specified global unique identifier number.
   */
  def findContainedEquipment(
                              guid: PlanetSideGUID
                            ): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    val (o, equipment) = findContainedEquipment()
    equipment.find { _.GUID == guid } match {
      case Some(equip) => (o, Set(equip))
      case None        => (None, Set.empty)
    }
  }

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output.
   * @return an `Equipment` object
   */
  def findEquipment(): Set[Equipment] = findContainedEquipment()._2

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output
   * and only discovers `Equipment` with the specified global unique identifier number.
   * @return an `Equipment` object
   */
  def findEquipment(guid: PlanetSideGUID): Option[Equipment] = findEquipment().find { _.GUID == guid }

  /**
   * Get the current `Vehicle` object that the player is riding/driving.
   * The vehicle must be found solely through use of `player.VehicleSeated`.
   * @return the vehicle
   */
  def findLocalVehicle: Option[Vehicle] = {
    continent.GUID(player.VehicleSeated) match {
      case Some(obj: Vehicle) => Some(obj)
      case _ => None
    }
  }

  /**
   * Drop an `Equipment` item onto the ground.
   * Specifically, instruct the item where it will appear,
   * add it to the list of items that are visible to multiple users,
   * and then inform others that the item has been dropped.
   * @param obj a `Container` object that represents where the item will be dropped;
   *            curried for callback
   * @param zone the continent in which the item is being dropped;
   *             curried for callback
   * @param item the item
   */
  def normalItemDrop(obj: PlanetSideServerObject with Container, zone: Zone)(item: Equipment): Unit = {
    zone.Ground.tell(Zone.Ground.DropItem(item, obj.Position, Vector3.z(obj.Orientation.z)), obj.Actor)
  }

  /**
   * Given an object globally unique identifier, search in a given location for it.
   * @param objectGuid the object
   * @param parent a `Container` object wherein to search
   * @return an optional tuple that contains two values;
   *         the first value is the container that matched correctly with the object's GUID;
   *         the second value is the slot position of the object
   */
  def findInLocalContainer(
                            objectGuid: PlanetSideGUID
                          )(parent: PlanetSideServerObject with Container): Option[(PlanetSideServerObject with Container, Option[Int])] = {
    parent.Find(objectGuid).flatMap { slot => Some((parent, Some(slot))) }
  }

  /**
   * na
   * @param targetGuid na
   * @param unk1 na
   * @param unk2 na
   */
  def hackObject(targetGuid: PlanetSideGUID, unk1: Long, unk2: Long): Unit = {
    sendResponse(HackMessage(unk1=0, targetGuid, player_guid=Service.defaultPlayerGUID, progress=100, unk1, HackState.Hacked, unk2))
  }

  /**
   * Send a PlanetsideAttributeMessage packet to the client
   * @param targetGuid The target of the attribute
   * @param attributeNumber The attribute number
   * @param attributeValue The attribute value
   */
  def sendPlanetsideAttributeMessage(
                                      targetGuid: PlanetSideGUID,
                                      attributeNumber: PlanetsideAttributeEnum,
                                      attributeValue: Long
                                    ): Unit = {
    sendResponse(PlanetsideAttributeMessage(targetGuid, attributeNumber, attributeValue))
  }

  /**
   * The player has lost the will to live and must be killed.
   * @see `Vitality`<br>
   *       `PlayerSuicide`
   * @param tplayer the player to be killed
   */
  def suicide(tplayer: Player): Unit = {
    tplayer.LogActivity(PlayerSuicide(PlayerSource(tplayer)))
    tplayer.Actor ! Player.Die()
  }

  /**
   * Initialize the deployables user interface elements.<br>
   * <br>
   * All element initializations require both the maximum deployable amount and the current deployables active counts.
   * Until initialized, all elements will be RED 0/0 as if the corresponding certification were not `learn`ed.
   * The respective element will become a pair of numbers, the second always being non-zero, when properly initialized.
   * The numbers will appear GREEN when more deployables of that type can be placed.
   * The numbers will appear RED if the player can not place any more of that type of deployable.
   * The numbers will appear YELLOW if the current deployable count is greater than the maximum count of that type
   * such as may be the case when a player `forget`s a certification.
   * @param list a tuple of each UI element with four numbers;
   *             even numbers are attribute ids;
   *             odd numbers are quantities;
   *             first pair is current quantity;
   *             second pair is maximum quantity
   */
  def updateDeployableUIElements(list: List[(Int, Int, Int, Int)]): Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach {
      case (currElem, curr, maxElem, max) =>
        //fields must update in ordered pairs: max, curr
        sendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    }
  }

  /**
   * A simple object searching algorithm that is limited to containers currently known and accessible by the player.
   * If all relatively local containers are checked and the object is not found,
   * the player's locker inventory will be checked, and then
   * the game environment (items on the ground) will be checked too.
   * If the target object is discovered, it is removed from its current location and is completely destroyed.
   * @see `RequestDestroyMessage`
   * @see `Zone.ItemIs.Where`
   * @param objectGuid the target object's globally unique identifier;
   *                    it is not expected that the object will be unregistered, but it is also not gauranteed
   * @param obj the target object
   * @return `true`, if the target object was discovered and removed;
   *        `false`, otherwise
   */
  def findEquipmentToDelete(objectGuid: PlanetSideGUID, obj: Equipment): Boolean = {
    val findFunc
    : PlanetSideServerObject with Container => Option[(PlanetSideServerObject with Container, Option[Int])] =
      findInLocalContainer(objectGuid)

    findFunc(player)
      .orElse(accessedContainer match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      })
      .orElse(findLocalVehicle match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      }) match {
      case Some((parent, Some(_))) =>
        obj.Position = Vector3.Zero
        RemoveOldEquipmentFromInventory(parent)(obj)
        true
      case _ if player.avatar.locker.Inventory.Remove(objectGuid) =>
        sendResponse(ObjectDeleteMessage(objectGuid, 0))
        true
      case _ if continent.EquipmentOnGround.contains(obj) =>
        obj.Position = Vector3.Zero
        continent.Ground ! Zone.Ground.RemoveItem(objectGuid)
        continent.AvatarEvents ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
        true
      case _ =>
        Zone.EquipmentIs.Where(obj, objectGuid, continent) match {
          case None =>
            true
          case Some(Zone.EquipmentIs.Orphaned()) if obj.HasGUID =>
            TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
            true
          case Some(Zone.EquipmentIs.Orphaned()) =>
            true
          case _ =>
            log.warn(s"RequestDestroy: equipment $obj exists, but ${player.Name} can not reach it to dispose of it")
            false
        }
    }
  }

  /**
   * Attempt to link the router teleport system using the provided terminal information.
   * Although additional states are necessary to properly use the teleportation system,
   * e.g., deployment state, active state of the endpoints, etc.,
   * this decision is not made factoring those other conditions.
   * @param router the vehicle that houses one end of the teleportation system (the `InternalTelepad` object)
   * @param systemPlan specific object identification of the two endpoints of the teleportation system;
   *                   if absent, the knowable endpoint is deleted from the client reflexively
   */
  def toggleTeleportSystem(router: Vehicle, systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]): Unit = {
    systemPlan match {
      case Some((internalTelepad, remoteTelepad)) =>
        internalTelepad.Telepad = remoteTelepad.GUID //necessary; backwards link to the (new) telepad
        TelepadLike.StartRouterInternalTelepad(continent, router.GUID, internalTelepad)
        TelepadLike.LinkTelepad(continent, remoteTelepad.GUID)
      case _ =>
        router.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) =>
            sendResponse(ObjectDeleteMessage(util.GUID, 0))
          case _ => ()
        }
    }
  }

  /**
   * A player uses a fully-linked Router teleportation system.
   * @param router the Router vehicle
   * @param internalTelepad the internal telepad within the Router vehicle
   * @param remoteTelepad the remote telepad that is currently associated with this Router
   * @param src the origin of the teleportation (where the player starts)
   * @param dest the destination of the teleportation (where the player is going)
   */
  def useRouterTelepadSystem(
                              router: Vehicle,
                              internalTelepad: InternalTelepad,
                              remoteTelepad: TelepadDeployable,
                              src: PlanetSideGameObject with TelepadLike,
                              dest: PlanetSideGameObject with TelepadLike
                            ): Unit = {
    val time = System.currentTimeMillis()
    if (
      time - recentTeleportAttempt > 2000L && router.DeploymentState == DriveState.Deployed &&
        internalTelepad.Active &&
        remoteTelepad.Active
    ) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      useRouterTelepadEffect(pguid, sguid, dguid)
      continent.LocalEvents ! LocalServiceMessage(
        continent.id,
        LocalAction.RouterTelepadTransport(pguid, pguid, sguid, dguid)
      )
      val vSource = VehicleSource(router)
      val zoneNumber = continent.Number
      player.LogActivity(VehicleMountActivity(vSource, PlayerSource(player), zoneNumber))
      player.Position = dest.Position
      player.LogActivity(VehicleDismountActivity(vSource, PlayerSource(player), zoneNumber))
    } else {
      log.warn(s"UseRouterTelepadSystem: ${player.Name} can not teleport")
    }
    recentTeleportAttempt = time
  }

  /**
   * Animate(?) a player using a fully-linked Router teleportation system.
   * In reality, this seems to do nothing visually?
   * @param playerGUID the player being teleported
   * @param srcGUID the origin of the teleportation
   * @param destGUID the destination of the teleportation
   */
  def useRouterTelepadEffect(playerGUID: PlanetSideGUID, srcGUID: PlanetSideGUID, destGUID: PlanetSideGUID): Unit = {
    sendResponse(PlanetsideAttributeMessage(playerGUID, 64, 1)) //what does this do?
    sendResponse(GenericObjectActionMessage(srcGUID, 31))
    sendResponse(GenericObjectActionMessage(destGUID, 32))
  }

  def maxCapacitorTick(jumpThrust: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      val activate = (jumpThrust || player.isOverdrived || player.isShielded) && player.Capacitor > 0
      player.CapacitorState match {
        case CapacitorStateType.Idle        => maxCapacitorTickIdle(activate)
        case CapacitorStateType.Discharging => maxCapacitorTickDischarging(activate)
        case CapacitorStateType.ChargeDelay => maxCapacitorTickChargeDelay(activate)
        case CapacitorStateType.Charging    => maxCapacitorTickCharging(activate)
      }
    } else if (player.CapacitorState != CapacitorStateType.Idle) {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  private def maxCapacitorTickIdle(activate: Boolean): Unit = {
    if (activate) {
      player.CapacitorState = CapacitorStateType.Discharging
      //maxCapacitorTickDischarging(activate)
    } else if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      player.CapacitorState = CapacitorStateType.ChargeDelay
      maxCapacitorTickChargeDelay(activate)
    }
  }

  private def maxCapacitorTickDischarging(activate: Boolean): Unit = {
    if (activate) {
      val timeDiff    = (System.currentTimeMillis() - player.CapacitorLastUsedMillis).toFloat / 1000
      val drainAmount = player.ExoSuitDef.CapacitorDrainPerSecond.toFloat * timeDiff
      player.Capacitor -= drainAmount
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
    } else if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      if (player.Faction != PlanetSideEmpire.VS) {
        toggleMaxSpecialState(enable = false)
      }
      player.CapacitorState = CapacitorStateType.ChargeDelay
      maxCapacitorTickChargeDelay(activate)
    } else {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  private def maxCapacitorTickChargeDelay(activate: Boolean): Unit = {
    if (activate) {
      player.CapacitorState = CapacitorStateType.Discharging
      //maxCapacitorTickDischarging(activate)
    } else if (player.Capacitor == player.ExoSuitDef.MaxCapacitor) {
      player.CapacitorState = CapacitorStateType.Idle
    } else if (System.currentTimeMillis() - player.CapacitorLastUsedMillis > player.ExoSuitDef.CapacitorRechargeDelayMillis) {
      player.CapacitorState = CapacitorStateType.Charging
      //maxCapacitorTickCharging(activate)
    }
  }

  private def maxCapacitorTickCharging(activate: Boolean): Unit = {
    if (activate) {
      player.CapacitorState = CapacitorStateType.Discharging
      //maxCapacitorTickDischarging(activate)
    } else if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      val timeDiff = (System.currentTimeMillis() - player.CapacitorLastChargedMillis).toFloat / 1000
      val chargeAmount = player.ExoSuitDef.CapacitorRechargePerSecond * timeDiff
      player.Capacitor += chargeAmount
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
    } else {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  def toggleMaxSpecialState(enable: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      if (enable && player.UsingSpecial == SpecialExoSuitDefinition.Mode.Normal) {
        player.Faction match {
          case PlanetSideEmpire.TR if player.Capacitor == player.ExoSuitDef.MaxCapacitor =>
            player.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
            activateMaxSpecialStateMessage()
          case PlanetSideEmpire.NC if player.Capacitor > 0 =>
            player.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
            activateMaxSpecialStateMessage()
          case PlanetSideEmpire.VS =>
            log.warn(s"${player.Name} tried to use a MAX special ability but their faction doesn't have one")
          case _ => ()
        }
      } else {
        player.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.PlanetsideAttributeToAll(player.GUID, 8, 0)
        )
      }
    }
  }

  private def activateMaxSpecialStateMessage(): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlanetsideAttributeToAll(player.GUID, 8, 1)
    )
  }

  def administrativeKick(tplayer: Player): Unit = {
    log.warn(s"${tplayer.Name} has been kicked by ${player.Name}")
    tplayer.death_by = -1
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(tplayer.Name)
    //get out of that vehicle
    sessionLogic.vehicles.GetMountableAndSeat(None, tplayer, continent) match {
      case (Some(obj), Some(seatNum)) =>
        tplayer.VehicleSeated = None
        obj.Seats(seatNum).unmount(tplayer)
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seatNum, unk2=false, obj.GUID)
        )
      case _ => ()
    }
  }

  def fallHeightTracker(zHeight: Float): Unit = {
    if ((heightTrend && heightLast - zHeight >= 0.5f) ||
      (!heightTrend && zHeight - heightLast >= 0.5f)) {
      heightTrend = !heightTrend
      heightHistory = zHeight
    }
    heightLast = zHeight
  }

  def canSeeReallyFar: Boolean = {
    sessionLogic.shooting.FindContainedWeapon match {
      case (Some(_: Vehicle), weapons) if weapons.nonEmpty =>
        player.avatar
          .implants
          .exists { p =>
            p.collect { implant => implant.definition.implantType == ImplantType.RangeMagnifier && implant.active }.nonEmpty
          }
      case (Some(_: Player), weapons) if weapons.nonEmpty =>
        val wep = weapons.head
        wep.Definition == GlobalDefinitions.bolt_driver ||
          wep.Definition == GlobalDefinitions.heavy_sniper ||
          (
            (wep.Projectile ne GlobalDefinitions.no_projectile) &&
              player.Crouching &&
              player.avatar
                .implants
                .exists { p =>
                  p.collect { implant => implant.definition.implantType == ImplantType.RangeMagnifier && implant.active }.nonEmpty
                }
            )
      case _ =>
        false
    }
  }

  def displayCharSavedMsgThenRenewTimer(fixedLen: Long, varLen: Long): Unit = {
    charSaved()
    renewCharSavedTimer(fixedLen, varLen)
  }

  def renewCharSavedTimer(fixedLen: Long, varLen: Long): Unit = {
    charSavedTimer.cancel()
    val delay = (fixedLen + (varLen * scala.math.random()).toInt).seconds
    charSavedTimer = context.system.scheduler.scheduleOnce(delay, context.self, SessionActor.CharSavedMsg)
  }

  def charSaved(): Unit = {
    sendResponse(ChatMsg(ChatMessageType.UNK_227, wideContents=false, "", "@charsaved", None))
  }

  private def updateCollisionHistoryForTarget(
                                               target: PlanetSideServerObject with Vitality with FactionAffinity,
                                               curr: Long
                                             ): Boolean = {
    collisionHistory.get(target.Actor) match {
      case Some(lastCollision) if curr - lastCollision <= 1000L =>
        false
      case _ =>
        collisionHistory.put(target.Actor, curr)
        true
    }
  }

  private def collisionBetweenVehicleAndFragileDeployable(
                                                           vehicle: Vehicle,
                                                           vehiclePosition: Vector3,
                                                           smallDeployable: Deployable,
                                                           smallDeployablePosition: Vector3,
                                                           velocity: Vector3,
                                                           fallHeight: Float,
                                                           collisionTime: Long
                                                         ): Unit = {
    if (updateCollisionHistoryForTarget(smallDeployable, collisionTime)) {
      val smallDeployableSource = SourceEntry(smallDeployable)
      //vehicle takes damage from the collision (ignore bail protection in this case)
      performCollisionWithSomethingDamage(vehicle, SourceEntry(vehicle), vehiclePosition, smallDeployableSource, fallHeight, velocity)
      //deployable gets absolutely destroyed
      collisionHistory.put(vehicle.Actor, collisionTime)
      sessionLogic.handleDealingDamage(
        smallDeployable,
        DamageInteraction(smallDeployableSource, SuicideReason(), smallDeployablePosition)
      )
    }
  }

  private def performCollisionWithSomethingDamage(
                                                   target: PlanetSideServerObject with Vitality with FactionAffinity,
                                                   targetSource: SourceEntry,
                                                   targetPosition: Vector3,
                                                   victimSource: SourceEntry,
                                                   fallHeight: Float,
                                                   velocity: Vector3
                                                 ): Unit = {
    sessionLogic.handleDealingDamage(
      target,
      DamageInteraction(
        targetSource,
        CollisionWithReason(CollisionReason(velocity, fallHeight, target.DamageModel), victimSource),
        targetPosition
      )
    )
  }

  override protected[support] def actionsToCancel(): Unit = {
    progressBarValue = None
    kitToBeUsed = None
    collisionHistory.clear()
    accessedContainer match {
      case Some(v: Vehicle) =>
        val vguid = v.GUID
        sessionLogic.vehicles.ConditionalDriverVehicleControl(v)
        if (v.AccessingTrunk.contains(player.GUID)) {
          if (player.VehicleSeated.contains(vguid)) {
            v.AccessingTrunk = None //player is seated; just stop accessing trunk
            if (player.isAlive) {
              sendResponse(UnuseItemMessage(player.GUID, vguid))
            }
          } else {
            unaccessContainer(v)
          }
        }

      case Some(o) =>
        unaccessContainer(o)
        if (player.isAlive) {
          sendResponse(UnuseItemMessage(player.GUID, o.GUID))
        }

      case None => ()
    }
  }

  override protected[support] def stop(): Unit = {
    progressBarUpdate.cancel()
    charSavedTimer.cancel()
  }
}
