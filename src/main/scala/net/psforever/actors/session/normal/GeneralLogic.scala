// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData}
import net.psforever.login.WorldSession.{CallBackForTask, ContainableMoveItem, DropEquipmentFromInventory, PickUpEquipmentFromGround, RemoveOldEquipmentFromInventory}
import net.psforever.objects.{Account, BoomerDeployable, BoomerTrigger, ConstructionItem, Deployables, GlobalDefinitions, Kit, LivePlayerList, PlanetSideGameObject, Player, SensorDeployable, ShieldGeneratorDeployable, SpecialEmp, TelepadDeployable, Tool, TrapDeployable, TurretDeployable, Vehicle}
import net.psforever.objects.avatar.{Avatar, PlayerControl, SpecialCarry}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.{Deployable, DeployedItem, TelepadLike}
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObject}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.{MatrixTerminalDefinition, ProximityUnit, Terminal}
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vehicles.{AccessPermissionGroup, Utility, UtilityType, VehicleLockState}
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vital.{VehicleDismountActivity, VehicleMountActivity, Vitality}
import net.psforever.objects.vital.collision.{CollisionReason, CollisionWithReason}
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.objects.zones.{Zone, ZoneProjectile, Zoning}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.packet.game.{ActionCancelMessage, ActionResultMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BindStatus, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestAction, CharacterRequestMessage, ChatMsg, CollisionIs, ConnectToWorldRequestMessage, CreateShortcutMessage, DeadState, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, ImplantAction, InvalidTerrainMessage, ItemTransactionMessage, LootItemMessage, MoveItemMessage, ObjectDeleteMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, PlayerStateShiftMessage, RequestDestroyMessage, ShiftState, TargetInfo, TargetingImplantRequest, TargetingInfoMessage, TerrainCondition, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.RemoverActor
import net.psforever.services.account.{AccountPersistenceService, RetrieveAccountData}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.types.{CapacitorStateType, ChatMessageType, Cosmetic, DriveState, ExoSuitType, ImplantType, PlanetSideEmpire, PlanetSideGUID, SpawnGroup, TransactionType, Vector3}
import net.psforever.util.Config

import scala.concurrent.duration._

object GeneralLogic {
  def apply(ops: GeneralOperations): GeneralLogic = {
    new GeneralLogic(ops, ops.context)
  }
}

class GeneralLogic(val ops: GeneralOperations, implicit val context: ActorContext) extends GeneralFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  def handleConnectToWorldRequest(pkt: ConnectToWorldRequestMessage): Unit = {
    val ConnectToWorldRequestMessage(_, token, majorVersion, minorVersion, revision, buildDate, _, _) = pkt
    log.trace(
      s"ConnectToWorldRequestMessage: client with versioning $majorVersion.$minorVersion.$revision, $buildDate has sent a token to the server"
    )
    sendResponse(ChatMsg(ChatMessageType.CMT_CULLWATERMARK, wideContents=false, "", "", None))
    context.self ! SessionActor.StartHeartbeat
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
    ops.fallHeightTracker(pos.z)
    if (isCrouching && !player.Crouching) {
      //dev stuff goes here
      sendResponse(ChatMsg(ChatMessageType.UNK_228, "@login_reposition_to_sanctuary"))
      sendResponse(ChatMsg(ChatMessageType.UNK_229, "@PadDeconstruct_Done"))
      //sendResponse(ChatMsg(ChatMessageType.UNK_227, "@NoMount_Permission"))
      //sendResponse(ChatMsg(ChatMessageType.UNK_227, "@ArmorShieldOff"))
      //sendResponse(ChatMsg(ChatMessageType.UNK_227, "@ArmorShieldOverride"))
    }
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
    ops.accessedContainer match {
      // Ensure we don't unload the contents of the vehicle trunk for players seated in the vehicle.
      // This can happen if PSUM arrives during the mounting process
      case Some(veh: Vehicle) if player.VehicleSeated.isEmpty || player.VehicleSeated.get != veh.GUID =>
        if (isMoving || veh.isMoving(test = 1) || Vector3.DistanceSquared(player.Position, veh.TrunkLocation) > 9) {
          val guid = player.GUID
          sendResponse(UnuseItemMessage(guid, veh.GUID))
          sendResponse(UnuseItemMessage(guid, guid))
          ops.unaccessContainer(veh)
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
          ops.unaccessContainer(container)
        }
      case None => ()
    }
    //llu destruction check
    sessionLogic.localResponse.loseFlagViolently(ops.specialItemSlotGuid, player)
    //
    val eagleEye: Boolean = ops.canSeeReallyFar
    val isNotVisible: Boolean = sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing ||
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

  def handleVoiceHostRequest(pkt: VoiceHostRequest): Unit = {
    ops.noVoicedChat(pkt)
  }

  def handleVoiceHostInfo(pkt: VoiceHostInfo): Unit = {
    ops.noVoicedChat(pkt)
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
            //disembark from zipline at destination
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
            ] = ops.findInLocalContainer(itemGuid)
            findFunc(player.avatar.locker)
              .orElse(findFunc(player))
              .orElse(ops.accessedContainer match {
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
    val equipment = ops.findContainedEquipment(pkt.item_used_guid) match {
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
        ops.unaccessContainer(obj)
        sessionLogic.zoning.spawn.TryDisposeOfLootedCorpse(obj)
      case Some(obj: Container) =>
        // Make sure we don't unload the contents of the vehicle the player is seated in
        // An example scenario of this would be closing the trunk contents when rearming at a landing pad
        if (player.VehicleSeated.isEmpty || player.VehicleSeated.get != obj.GUID) {
          ops.unaccessContainer(obj)
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
        TaskWorkflow.execute(CallBackForTask(tasking, continent.Deployables, Zone.Deployable.BuildByOwner(dObj, player, obj), context.self))
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
          sessionLogic.validObject(player.VehicleSeated, decorator = "GenericObjectAction/Vehicle") collect {
            case vehicle: Vehicle
              if vehicle.OwnerName.contains(player.Name) =>
              vehicle.Actor ! ServerObject.GenericObjectAction(objectGuid, code, Some(tool))
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
          ops.dropSpecialSlotItem()
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
            ops.toggleMaxSpecialState(enable = true)
          } else {
            log.warn(s"GenericActionMessage: ${player.Name} can't handle MAX special effect")
          }
        case GenericAction.StopMaxSpecialEffect_RCV =>
          if (player.ExoSuit == ExoSuitType.MAX) {
            player.Faction match {
              case PlanetSideEmpire.NC =>
                ops.toggleMaxSpecialState(enable = false)
              case _ =>
                log.warn(s"GenericActionMessage: ${player.Name} tried to cancel an uncancellable MAX special ability")
            }
          } else {
            log.warn(s"GenericActionMessage: ${player.Name} can't stop MAX special effect")
          }
        case GenericAction.AwayFromKeyboard_RCV =>
          log.info(s"${player.Name} is AFK")
          AvatarActor.savePlayerLocation(player)
          ops.displayCharSavedMsgThenRenewTimer(fixedLen=1800L, varLen=0L) //~30min
          player.AwayFromKeyboard = true
        case GenericAction.BackInGame_RCV =>
          log.info(s"${player.Name} is back")
          player.AwayFromKeyboard = false
          ops.renewCharSavedTimer(
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

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit = {
    val GenericCollisionMsg(ctype, p, _, ppos, pv, t, _, tpos, tv, _, _, _) = pkt
    val fallHeight = {
      if (pv.z * pv.z >= (pv.x * pv.x + pv.y * pv.y) * 0.5f) {
        if (ops.heightTrend) {
          val fall = ops.heightLast - ops.heightHistory
          ops.heightHistory = ops.heightLast
          fall
        }
        else {
          val fall = ops.heightHistory - ops.heightLast
          ops.heightLast = ops.heightHistory
          fall
        }
      } else {
        0f
      }
    }
    val (target1, target2, bailProtectStatus, velocity) = (ctype, sessionLogic.validObject(p, decorator = "GenericCollision/Primary")) match {
      case (CollisionIs.OfInfantry, out @ Some(user: Player))
        if user == player =>
        val bailStatus = session.flying || session.speed > 1f || player.BailProtection
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
          ops.collisionHistory.put(us.Actor, curr)
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
        case obj: Vitality if obj.Destroyed => () //some entities will try to charge even if destroyed
        case obj: Vehicle if obj.MountedIn.nonEmpty => () //cargo vehicles need to be excluded
        case obj: Vehicle =>
          commonFacilityShieldCharging(obj)
        case obj: TurretDeployable =>
          commonFacilityShieldCharging(obj)
        case _ if vehicleGuid.nonEmpty =>
          log.warn(
            s"FacilityBenefitShieldChargeRequest: ${player.Name} can not find chargeable entity ${vehicleGuid.get.guid} in ${continent.id}"
          )
        case _ =>
          log.warn(s"FacilityBenefitShieldChargeRequest: ${player.Name} is not seated in anything")
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
    ops.progressBarUpdate.cancel()
    ops.progressBarValue = None
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
        val mode = 7 + (if (weapon.Projectile == GlobalDefinitions.wasp_rocket_projectile) 1 else 0)
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

  def handleRenewCharSavedTimer(): Unit = {
    ops.renewCharSavedTimer(
      Config.app.game.savedMsg.interruptedByAction.fixed,
      Config.app.game.savedMsg.interruptedByAction.variable
    )
  }

  def handleRenewCharSavedTimerMsg(): Unit = {
    ops.displayCharSavedMsgThenRenewTimer(
      Config.app.game.savedMsg.interruptedByAction.fixed,
      Config.app.game.savedMsg.interruptedByAction.variable
    )
  }

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
    case _: KitDefinition => ops.kitToBeUsed = None
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
    ops.administrativeKick(player)
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(player.Name, time)
  }

  def handleSilenced(isSilenced: Boolean): Unit = {
    player.silenced = isSilenced
  }

  def handleItemPutInSlot(msg: Containable.ItemPutInSlot): Unit = {
    log.debug(s"ItemPutInSlot: $msg")
  }

  def handleCanNotPutItemInSlot(msg: Containable.CanNotPutItemInSlot): Unit = {
    log.debug(s"CanNotPutItemInSlot: $msg")
  }

  def handleReceiveDefaultMessage(default: Any, sender: ActorRef): Unit = {
    log.warn(s"Invalid packet class received: $default from $sender")
  }

  /* supporting functions */

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
    val vehicleOpt = continent.GUID(player.avatar.vehicle)
    (vehicleOpt, equipment) match {
      case (Some(vehicle: Vehicle), Some(item))
        if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
        resourceSilo.Actor ! CommonMessages.Use(player, Some(vehicle))
      case (Some(vehicle: Vehicle), _)
        if vehicle.Definition == GlobalDefinitions.ant &&
          vehicle.DeploymentState == DriveState.Deployed &&
          Vector3.DistanceSquared(resourceSilo.Position.xy, vehicle.Position.xy) < math.pow(resourceSilo.Definition.UseRadius, 2) =>
        resourceSilo.Actor ! CommonMessages.Use(player, Some(vehicle))
      case _ => ()
    }
  }

  private def handleUsePlayer(obj: Player, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    if (obj.isBackpack) {
      if (equipment.isEmpty) {
        log.info(s"${player.Name} is looting the corpse of ${obj.Name}")
        sendResponse(msg)
        ops.accessContainer(obj)
      }
    } else if (!msg.unk3 && player.isAlive) { //potential kit use
      (continent.GUID(msg.item_used_guid), ops.kitToBeUsed) match {
        case (Some(kit: Kit), None) =>
          ops.kitToBeUsed = Some(msg.item_used_guid)
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
        ops.accessContainer(playerLocker)
      case _ => ()
    }
  }

  private def handleUseCaptureTerminal(captureTerminal: CaptureTerminal, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(captureTerminal, item)
      case _ if ops.specialItemSlotGuid.nonEmpty =>
        continent.GUID(ops.specialItemSlotGuid) match {
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
          ops.accessContainer(obj)
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
    ops.specialItemSlotGuid match {
      case None if obj.Faction == player.Faction =>
        ops.specialItemSlotGuid = Some(obj.GUID)
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

  /**
   * Get the current `Vehicle` object that the player is riding/driving.
   * The vehicle must be found solely through use of `player.VehicleSeated`.
   * @return the vehicle
   */
  private def findLocalVehicle: Option[Vehicle] = {
    continent.GUID(player.VehicleSeated) match {
      case Some(obj: Vehicle) => Some(obj)
      case _ => None
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
  private def findEquipmentToDelete(objectGuid: PlanetSideGUID, obj: Equipment): Boolean = {
    val findFunc
    : PlanetSideServerObject with Container => Option[(PlanetSideServerObject with Container, Option[Int])] =
      ops.findInLocalContainer(objectGuid)

    findFunc(player)
      .orElse(ops.accessedContainer match {
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
   * A player uses a fully-linked Router teleportation system.
   * @param router the Router vehicle
   * @param internalTelepad the internal telepad within the Router vehicle
   * @param remoteTelepad the remote telepad that is currently associated with this Router
   * @param src the origin of the teleportation (where the player starts)
   * @param dest the destination of the teleportation (where the player is going)
   */
  private def useRouterTelepadSystem(
                                      router: Vehicle,
                                      internalTelepad: InternalTelepad,
                                      remoteTelepad: TelepadDeployable,
                                      src: PlanetSideGameObject with TelepadLike,
                                      dest: PlanetSideGameObject with TelepadLike
                                    ): Unit = {
    val time = System.currentTimeMillis()
    if (
      time - ops.recentTeleportAttempt > 2000L && router.DeploymentState == DriveState.Deployed &&
        internalTelepad.Active &&
        remoteTelepad.Active
    ) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      ops.useRouterTelepadEffect(pguid, sguid, dguid)
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
    ops.recentTeleportAttempt = time
  }

  private def maxCapacitorTick(jumpThrust: Boolean): Unit = {
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
        ops.toggleMaxSpecialState(enable = false)
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

  private def updateCollisionHistoryForTarget(
                                               target: PlanetSideServerObject with Vitality with FactionAffinity,
                                               curr: Long
                                             ): Boolean = {
    ops.collisionHistory.get(target.Actor) match {
      case Some(lastCollision) if curr - lastCollision <= 1000L =>
        false
      case _ =>
        ops.collisionHistory.put(target.Actor, curr)
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
      ops.collisionHistory.put(vehicle.Actor, collisionTime)
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

  private def commonFacilityShieldCharging(obj: PlanetSideServerObject with BlockMapEntity): Unit = {
    obj.Actor ! CommonMessages.ChargeShields(
      15,
      Some(continent.blockMap.sector(obj).buildingList.maxBy(_.Definition.SOIRadius))
    )
  }
}
