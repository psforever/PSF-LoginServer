// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData, SessionOutfitHandlers}
import net.psforever.objects.{Account, BoomerDeployable, BoomerTrigger, ConstructionItem, GlobalDefinitions, LivePlayerList, Player, SensorDeployable, ShieldGeneratorDeployable, SpecialEmp, TelepadDeployable, Tool, TrapDeployable, TurretDeployable, Vehicle}
import net.psforever.objects.avatar.{Avatar, PlayerControl, SpecialCarry}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{PlanetSideServerObject, ServerObject}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.dome.ForceDomePhysics
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.interior.Sidedness.OutsideOf
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.mount.MountableEntity
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.collision.{CollisionReason, CollisionWithReason}
import net.psforever.objects.vital.etc.{ForceDomeExposure, SuicideReason}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.{ZoneProjectile, Zoning}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ActionCancelMessage, ActionResultMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestAction, CharacterRequestMessage, ChatMsg, CollisionIs, ConnectToWorldRequestMessage, CreateShortcutMessage, DeadState, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, OutfitMembershipRequest, OutfitMembershipRequestAction, OutfitMembershipResponse, OutfitRequest, OutfitRequestAction, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, TargetingImplantRequest, TerrainCondition, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.account.{AccountPersistenceService, RetrieveAccountData}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.types.{CapacitorStateType, ChatMessageType, Cosmetic, ExoSuitType, ImplantType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.util.Config
import net.psforever.zones.Zones.zones

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
    sessionLogic.zoning.spawn.tryQueuedActivity(vel)
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
    if (player.Carrying.contains(SpecialCarry.CaptureFlag)) {
      CaptureFlagManager.ReasonToLoseFlagViolently(continent, sessionLogic.general.specialItemSlotGuid, player)
    }
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
    val pZone = player.Zone
    sendResponse(EmoteMsg(avatarGuid, emote))
    pZone.blockMap.sector(player).livePlayerList.collect { case t if t.GUID != player.GUID =>
      pZone.LocalEvents ! LocalServiceMessage(t.Name, LocalAction.SendResponse(EmoteMsg(avatarGuid, emote)))
    }
    pZone.AllPlayers.collect { case t if t.GUID != player.GUID && !t.allowInteraction =>
      pZone.LocalEvents ! LocalServiceMessage(t.Name, LocalAction.SendResponse(EmoteMsg(avatarGuid, emote)))
    }
  }

  def handleDropItem(pkt: DropItemMessage): Unit = {
    ops.handleDropItem(pkt) match {
      case GeneralOperations.ItemDropState.Dropped =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
      case GeneralOperations.ItemDropState.NotDropped
        if continent.GUID(player.VehicleSeated).isEmpty =>
        log.warn(s"DropItem: ${player.Name} wanted to drop an item, but it wasn't at hand")
      case GeneralOperations.ItemDropState.NotDropped =>
        log.warn(s"DropItem: ${player.Name} wanted to drop an item, but it was not equipment")
      case _ => ()
    }
  }

  def handlePickupItem(pkt: PickupItemMessage): Unit = {
    ops.handlePickupItem(pkt) match {
      case GeneralOperations.ItemPickupState.PickedUp =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
      case GeneralOperations.ItemPickupState.Dropped =>
        sendResponse(ActionResultMessage.Fail(16)) //error code?
      case _ => ()
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
    ops.handleZipLine(pkt) match {
      case GeneralOperations.ZiplineBehavior.Teleporter =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
      case GeneralOperations.ZiplineBehavior.Zipline =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_motion")
      case GeneralOperations.ZiplineBehavior.Unsupported =>
        log.warn(
          s"${player.Name} tried to do something with a zipline but can't handle it. action: ${pkt.action}, pathId: ${pkt.path_id}, zone: ${continent.id}"
        )
      case GeneralOperations.ZiplineBehavior.NotFound =>
        log.warn(s"${player.Name} couldn't find a zipline path ${pkt.path_id} in zone ${continent.id}")
    }
  }

  def handleRequestDestroy(pkt: RequestDestroyMessage): Unit = {
    val RequestDestroyMessage(objectGuid) = pkt
    //make sure this is the correct response for all cases
    sessionLogic.validObject(objectGuid, decorator = "RequestDestroy") match {
      case Some(vehicle: Vehicle) =>
        /* line 1a: vehicle and player (as the owner) acknowledge each other */
        /* line 1b: vehicle is the same faction as player, is ownable, and either the owner is absent or the vehicle is destroyed */
        /* line 2: vehicle is not mounted in anything or, if it is, its seats are empty */
        if (
          ((avatar.vehicle.contains(objectGuid) && vehicle.OwnerGuid.contains(player.GUID)) ||
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
        if (ops.findEquipmentToDelete(objectGuid, obj)) {
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
        if (obj.OwnerGuid.isEmpty || obj.OwnerGuid.contains(player.GUID) || obj.Destroyed) {
          obj.Actor ! Deployable.Deconstruct()
        } else {
          log.warn(s"RequestDestroy: ${player.Name} must own the deployable in order to deconstruct it")
        }

      case Some(obj: Equipment) =>
        ops.findEquipmentToDelete(objectGuid, obj)

      case Some(thing) =>
        log.warn(s"RequestDestroy: not allowed to delete this ${thing.Definition.Name}")

      case None => ()
    }
  }

  def handleMoveItem(pkt: MoveItemMessage): Unit = {
    ops.handleMoveItem(pkt)
  }

  def handleLootItem(pkt: LootItemMessage): Unit = {
    ops.handleLootItem(pkt)
  }

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit = {
    ops.handleAvatarImplant(pkt) match {
      case GeneralOperations.ImplantActivationBehavior.Activate | GeneralOperations.ImplantActivationBehavior.Deactivate =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_implant")
      case GeneralOperations.ImplantActivationBehavior.NotFound =>
        log.error(s"AvatarImplantMessage: ${player.Name} has an unknown implant in ${pkt.implantSlot}")
      case _ => ()
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
        ops.handleUseDoor(door, equipment)
      case Some(resourceSilo: ResourceSilo) =>
        ops.handleUseResourceSilo(resourceSilo, equipment)
      case Some(panel: IFFLock) =>
        ops.handleUseGeneralEntity(panel, equipment)
      case Some(obj: Player) =>
        ops.handleUsePlayer(obj, equipment, pkt)
      case Some(locker: Locker) =>
        ops.handleUseLocker(locker, equipment, pkt)
      case Some(gen: Generator) =>
        ops.handleUseGeneralEntity(gen, equipment)
      case Some(mech: ImplantTerminalMech) =>
        ops.handleUseGeneralEntity(mech, equipment)
      case Some(captureTerminal: CaptureTerminal) =>
        ops.handleUseCaptureTerminal(captureTerminal, equipment)
      case Some(obj: FacilityTurret) =>
        ops.handleUseFacilityTurret(obj, equipment, pkt)
      case Some(obj: Vehicle) =>
        ops.handleUseVehicle(obj, equipment, pkt)
      case Some(terminal: Terminal) =>
        ops.handleUseTerminal(terminal, equipment, pkt)
      case Some(obj: SpawnTube) =>
        ops.handleUseSpawnTube(obj, equipment)
      case Some(obj: SensorDeployable) =>
        ops.handleUseGeneralEntity(obj, equipment)
      case Some(obj: TurretDeployable) =>
        ops.handleUseGeneralEntity(obj, equipment)
      case Some(obj: TrapDeployable) =>
        ops.handleUseGeneralEntity(obj, equipment)
      case Some(obj: ShieldGeneratorDeployable) =>
        ops.handleUseGeneralEntity(obj, equipment)
      case Some(obj: TelepadDeployable) =>
        ops.handleUseTelepadDeployable(obj, equipment, pkt, ops.useRouterTelepadSystem)
      case Some(obj: Utility.InternalTelepad) =>
        ops.handleUseInternalTelepad(obj, pkt, ops.useRouterTelepadSystem)
      case Some(obj: CaptureFlag) =>
        ops.handleUseCaptureFlag(obj)
      case Some(_: WarpGate) =>
        ops.handleUseWarpGate(equipment)
      case Some(obj) =>
        ops.handleUseDefaultEntity(obj, equipment)
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
        if (ammoType == DeployedItem.spitfire_turret || ammoType == DeployedItem.spitfire_cloaked ||
          ammoType == DeployedItem.spitfire_aa) {
          ops.handleDeployObject(continent, ammoType, pos, orient, OutsideOf, player.Faction, player, obj)
        }
        else {
          ops.handleDeployObject(continent, ammoType, pos, orient, player.WhichSide, player.Faction, player, obj)
        }
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
          sessionLogic.shooting.handleWeaponFireAccountability(objectGuid, PlanetSideGUID(Projectile.baseUID))
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
        case GenericAction.MaxEnableAutoRun =>
          player.maxAutoRunEnabled = true
        case GenericAction.MaxDisableAutoRun =>
          player.maxAutoRunEnabled = false
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
      case (CollisionIs.BetweenThings, out @ Some(target: PlanetSideServerObject with MountableEntity)) =>
        target.BailProtection = false
        player.BailProtection = false
        (out, sessionLogic.validObject(t, decorator = "GenericCollision/Surface"), false, pv)
      case (_, Some(obj)) =>
        log.error(s"GenericCollision: $ctype detected: no handling case for ${obj.Definition.Name}")
        (None, None, false, Vector3.Zero)
      case (_, None) =>
        log.error(s"GenericCollision: $ctype detected: no entity detected as 'Primary'")
        (None, None, false, Vector3.Zero)
    }
    val curr = System.currentTimeMillis()
    (target1, t, target2) match {
      case (None, _, _) =>
        ()

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

      case (Some(us: PlanetSideServerObject with Vitality with FactionAffinity), _, Some(field: ForceDomePhysics)) =>
        us.Actor ! Damageable.MakeVulnerable
        us.Actor ! Vitality.Damage(
          DamageInteraction(
            PlayerSource(player),
            ForceDomeExposure(SourceEntry(field)),
            player.Position
          ).calculate()
        )

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
        case obj: Vehicle => ops.commonFacilityShieldCharging(obj)
        case obj: TurretDeployable => ops.commonFacilityShieldCharging(obj)
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
    ops.handleCreateShortcut(pkt)
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
    ops.handleObjectDetected(pkt)
  }

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit = {
    ops.handleTargetingImplantRequest(pkt)
  }

  def handleHitHint(pkt: HitHint): Unit = {
    val HitHint(_, _) = pkt
  }

  def handleOutfitMembershipRequest(pkt: OutfitMembershipRequest): Unit = {
    pkt match {
      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.Form(_, outfitName)) =>
        if (player.outfit_id == 0) {
          SessionOutfitHandlers.HandleOutfitForm(outfitName, player, sessionLogic)
        }

      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.Invite(_, invitedName)) =>
        if (player.outfit_id != 0) {
          SessionOutfitHandlers.HandleOutfitInvite(zones, invitedName, player)
        }

      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.AcceptInvite(_)) =>
        SessionOutfitHandlers.HandleOutfitInviteAccept(player, sessionLogic)

      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.RejectInvite(_)) =>
        SessionOutfitHandlers.HandleOutfitInviteReject(player)

      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.Kick(memberId, _)) =>
        if (player.outfit_id != 0) {
          SessionOutfitHandlers.HandleOutfitKick(zones, memberId, player, sessionLogic)
        }

      case OutfitMembershipRequest(_, OutfitMembershipRequestAction.SetRank(memberId, newRank, _)) =>
        SessionOutfitHandlers.HandleOutfitPromote(zones, memberId, newRank, player)

      case _ =>
    }
  }

  def handleOutfitMembershipResponse(pkt: OutfitMembershipResponse): Unit = {}

  def handleOutfitRequest(pkt: OutfitRequest): Unit = {
    pkt match {

      case OutfitRequest(_, OutfitRequestAction.Motd(message)) =>
        //SessionOutfitHandlers.HandleOutfitMotd(zones, message, player)

      case OutfitRequest(_, OutfitRequestAction.Ranks(List(r1, r2, r3, r4, r5, r6, r7, r8))) =>
        SessionOutfitHandlers.HandleOutfitRank(zones, List(r1, r2, r3, r4, r5, r6, r7, r8), player)

      case OutfitRequest(_, OutfitRequestAction.OutfitWindowOpen(true)) =>
        player.outfit_window_open = true
        SessionOutfitHandlers.HandleViewOutfitWindow(zones, player, player.outfit_id)

      case OutfitRequest(_, OutfitRequestAction.OutfitWindowOpen(false)) =>
        player.outfit_window_open = false

      case OutfitRequest(_, OutfitRequestAction.OutfitListWindowOpen(true)) =>
        player.outfit_list_open = true
        SessionOutfitHandlers.HandleGetOutfitList(player)

      case OutfitRequest(_, OutfitRequestAction.OutfitListWindowOpen(false)) =>
        player.outfit_list_open = false

      case _ =>
    }
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

  def handleSetSpectator(spectator: Boolean): Unit = { /* normal players can not flag spectate */ }

  def handleKick(player: Player, time: Option[Long]): Unit = {
    ops.administrativeKick(player, time)
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
}
