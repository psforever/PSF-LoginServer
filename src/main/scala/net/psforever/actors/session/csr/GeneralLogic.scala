// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData}
import net.psforever.login.WorldSession.{ContainableMoveItem, DropEquipmentFromInventory, PickUpEquipmentFromGround, RemoveOldEquipmentFromInventory}
import net.psforever.objects.{Account, BoomerDeployable, BoomerTrigger, ConstructionItem, GlobalDefinitions, LivePlayerList, Player, SensorDeployable, ShieldGeneratorDeployable, SpecialEmp, TelepadDeployable, Tool, TrapDeployable, TurretDeployable, Vehicle}
import net.psforever.objects.avatar.{Avatar, PlayerControl}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObject}
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{ZoneProjectile, Zoning}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ActionCancelMessage, ActionResultMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, CollisionIs, ConnectToWorldRequestMessage, CreateShortcutMessage, DeadState, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, ImplantAction, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, PlayerStateShiftMessage, RequestDestroyMessage, ShiftState, TargetInfo, TargetingImplantRequest, TargetingInfoMessage, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.RemoverActor
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{CapacitorStateType, Cosmetic, ExoSuitType, PlanetSideEmpire, PlanetSideGUID, Vector3}

object GeneralLogic {
  def apply(ops: GeneralOperations): GeneralLogic = {
    new GeneralLogic(ops, ops.context)
  }
}

class GeneralLogic(val ops: GeneralOperations, implicit val context: ActorContext) extends GeneralFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  def handleConnectToWorldRequest(pkt: ConnectToWorldRequestMessage): Unit = { /* intentionally blank */ }

  def handleCharacterCreateRequest(pkt: CharacterCreateRequestMessage): Unit = { /* intentionally blank */ }

  def handleCharacterRequest(pkt: CharacterRequestMessage): Unit = { /* intentionally blank */ }

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
    ) = pkt
    sessionLogic.persist()
    sessionLogic.turnCounterFunc(avatarGuid)
    //below half health, full heal
    val maxHealth = player.MaxHealth.toLong
    if (player.Health < maxHealth * 0.5f) {
      player.Health = maxHealth.toInt
      player.LogActivity(player.ClearHistory().head)
      sendResponse(PlanetsideAttributeMessage(avatarGuid, 0, maxHealth))
      continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.PlanetsideAttribute(avatarGuid, 0, maxHealth))
    }
    //below half stamina, full stamina
    val avatar = player.avatar
    val maxStamina = avatar.maxStamina
    if (avatar.stamina < maxStamina * 0.5f) {
      session = session.copy(avatar = avatar.copy(stamina = maxStamina))
      sendResponse(PlanetsideAttributeMessage(player.GUID, 2, maxStamina.toLong))
    }
    //expected
    val isMoving     = WorldEntity.isMoving(vel)
    val isMovingPlus = isMoving || isJumping || jumpThrust
    if (isMovingPlus) {
      if (sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing) {
        sessionLogic.zoning.spawn.stopDeconstructing()
      } else if (sessionLogic.zoning.zoningStatus != Zoning.Status.None) {
        sessionLogic.zoning.CancelZoningProcess()
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
      sessionLogic.zoning.CancelZoningProcess()
    }
    player.Cloaked = player.ExoSuit == ExoSuitType.Infiltration && isCloaking
    maxCapacitorTick()
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
      case Some(container)
        if !container.HasGUID && (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID) => //just in case
          // Ensure we don't close the container if the player is seated in it
          val guid = player.GUID
          // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
          sendResponse(UnuseItemMessage(guid, guid))
          ops.unaccessContainer(container)
      case None => ()
    }
    if (!player.spectator) {
      sessionLogic.updateBlockMap(player, pos)
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
    }
    sessionLogic.squad.updateSquad()
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
        sessionLogic.zoning.CancelZoningProcess()
        RemoveOldEquipmentFromInventory(player)(heldItem)
      case (Some(anItem: Equipment), Some(heldItem))
        if anItem eq heldItem =>
        sessionLogic.zoning.CancelZoningProcess()
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

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit = { /* no stamina loss */ }

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
        vehicle.Actor ! Vehicle.Deconstruct()

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
        obj.Actor ! Deployable.Deconstruct()

      case Some(obj: Equipment) =>
        ops.findEquipmentToDelete(objectGuid, obj)

      case Some(obj: Player) if obj.isBackpack =>
        obj.Position = Vector3.Zero
        continent.AvatarEvents ! AvatarServiceMessage.Corpse(RemoverActor.ClearSpecific(List(obj), continent))

      case Some(obj: Player) =>
        sessionLogic.general.suicide(obj)

      case Some(_) => ()

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
        sessionLogic.zoning.CancelZoningProcess()
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
      case Some(obj: TelepadDeployable) if player.spectator =>
        ops.handleUseTelepadDeployable(obj, equipment, pkt, ops.useRouterTelepadSystemSecretly)
      case Some(obj: Utility.InternalTelepad) if player.spectator =>
        ops.handleUseInternalTelepad (obj, pkt, ops.useRouterTelepadSystemSecretly)
      case Some(obj: TelepadDeployable) =>
        ops.handleUseTelepadDeployable(obj, equipment, pkt, ops.useRouterTelepadSystem)
      case Some(obj: Utility.InternalTelepad) =>
        ops.handleUseInternalTelepad (obj, pkt, ops.useRouterTelepadSystem)
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
        sessionLogic.zoning.CancelZoningProcess()
        ops.handleDeployObject(continent, ammoType, pos, orient, player.WhichSide, PlanetSideEmpire.NEUTRAL, None)
      case Some(obj) =>
        log.warn(s"DeployObject: what is $obj, ${player.Name}?  It's not a construction tool!")
      case None =>
        log.error(s"DeployObject: nothing, ${player.Name}?  It's not a construction tool!")
    }
  }

  def handlePlanetsideAttribute(pkt: PlanetsideAttributeMessage): Unit = {
    val PlanetsideAttributeMessage(objectGuid, attributeType, attributeValue) = pkt
    sessionLogic.validObject(objectGuid, decorator = "PlanetsideAttribute") match {
      case Some(vehicle: Vehicle) =>
        vehicle.Actor ! ServerObject.AttributeMsg(attributeType, attributeValue)
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
    if (player != null) {
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
          AvatarActor.savePlayerLocation(player)
          player.AwayFromKeyboard = true
        case GenericAction.BackInGame_RCV =>
          player.AwayFromKeyboard = false
        case GenericAction.LookingForSquad_RCV => //Looking For Squad ON
          if (!avatar.lookingForSquad) {
            avatarActor ! AvatarActor.SetLookingForSquad(false)
          }
        case GenericAction.NotLookingForSquad_RCV => //Looking For Squad OFF
          if (avatar.lookingForSquad) {
            avatarActor ! AvatarActor.SetLookingForSquad(false)
          }
        case _ =>
          log.warn(s"GenericActionMessage: ${player.Name} can't handle $action")
      }
    }
  }

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit = {
    val GenericCollisionMsg(ctype, p, _, _, pv, _, _, _, _, _, _, _) = pkt
    if (pv.z * pv.z >= (pv.x * pv.x + pv.y * pv.y) * 0.5f) {
      if (ops.heightTrend) {
        ops.heightHistory = ops.heightLast
      }
      else {
        ops.heightLast = ops.heightHistory
      }
    }
    (ctype, sessionLogic.validObject(p, decorator = "GenericCollision/Primary")) match {
      case (CollisionIs.OfInfantry, Some(user: Player))
        if user == player =>
        player.BailProtection = false
      case (CollisionIs.OfGroundVehicle, Some(v: Vehicle))
        if v.Seats(0).occupant.contains(player) =>
        v.BailProtection = false
      case (CollisionIs.OfAircraft, Some(v: Vehicle))
        if v.Definition.CanFly && v.Seats(0).occupant.contains(player) => ()
      case (CollisionIs.BetweenThings, _) =>
        log.warn("GenericCollision: CollisionIs.BetweenThings detected - no handling case")
      case _ => ()
    }
  }

  def handleAvatarFirstTimeEvent(pkt: AvatarFirstTimeEventMessage): Unit = { /* no speedrunning fte's */ }

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
        case _ if vehicleGuid.nonEmpty => ()
        case _ => ()
      }
  }

  def handleBattleplan(pkt: BattleplanMessage): Unit = {
    /* can not draw battleplan */
    //todo csr exclusive battleplan channel
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

  def handleInvalidTerrain(pkt: InvalidTerrainMessage): Unit = { /* csr does not have to worry about invalid terrain */ }

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

  def handleRenewCharSavedTimer(): Unit = { /* */ }

  def handleRenewCharSavedTimerMsg(): Unit =  { /* */ }

  def handleSetAvatar(avatar: Avatar): Unit = {
    session = session.copy(avatar = avatar)
    if (session.player != null) {
      session.player.avatar = avatar
    }
    LivePlayerList.Update(avatar.id, avatar)
  }

  def handleReceiveAccountData(account: Account): Unit = { /* no need */ }

  def handleUseCooldownRenew: BasicDefinition => Unit = {
    case _: KitDefinition => ops.kitToBeUsed = None
    case _ => ()
  }

  def handleAvatarResponse(avatar: Avatar): Unit = { /* no need */ }

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

  def handleSilenced(isSilenced: Boolean): Unit = { /* can not be silenced */ }

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

  def handleUseDoor(door: Door, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
        door.Actor ! CommonMessages.Use(player, Some(Float.MaxValue))
      case _ =>
        door.Actor ! CommonMessages.Use(player)
    }
  }

  private def maxCapacitorTick(): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      player.CapacitorState match {
        case CapacitorStateType.ChargeDelay => maxCapacitorTickChargeDelay()
        case CapacitorStateType.Charging    => maxCapacitorTickCharging()
        case _                              => maxCapacitorTickIdle()
      }
    } else if (player.CapacitorState != CapacitorStateType.Idle) {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  private def maxCapacitorTickIdle(): Unit = {
    if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      player.CapacitorState = CapacitorStateType.ChargeDelay
      maxCapacitorTickChargeDelay()
    }
  }

  private def maxCapacitorTickChargeDelay(): Unit = {
    if (player.Capacitor == player.ExoSuitDef.MaxCapacitor) {
      player.CapacitorState = CapacitorStateType.Idle
    } else if (System.currentTimeMillis() - player.CapacitorLastUsedMillis > player.ExoSuitDef.CapacitorRechargeDelayMillis) {
      player.CapacitorState = CapacitorStateType.Charging
    }
  }

  private def maxCapacitorTickCharging(): Unit = {
    if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      val timeDiff = (System.currentTimeMillis() - player.CapacitorLastChargedMillis).toFloat / 1000
      val chargeAmount = player.ExoSuitDef.CapacitorRechargePerSecond * timeDiff
      player.Capacitor += chargeAmount
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
    } else {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }
}
