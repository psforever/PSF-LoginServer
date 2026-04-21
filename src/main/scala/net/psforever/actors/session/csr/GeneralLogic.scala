// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData}
import net.psforever.objects.{Account, BoomerDeployable, BoomerTrigger, ConstructionItem, GlobalDefinitions, LivePlayerList, Player, SensorDeployable, ShieldGeneratorDeployable, SpecialEmp, TelepadDeployable, Tool, TrapDeployable, TurretDeployable, Vehicle}
import net.psforever.objects.avatar.{Avatar, PlayerControl}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, ServerObject}
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.dome.ForceDomePhysics
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
import net.psforever.packet.game.OutfitEventAction.{Initial, OutfitInfo, OutfitRankNames, Unk1}
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, ChatMsg, CollisionIs, ConnectToWorldRequestMessage, CreateShortcutMessage, DeadState, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, OutfitEvent, OutfitMemberEvent, OutfitMembershipRequest, OutfitMembershipResponse, OutfitRequest, OutfitRequestAction, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, TargetingImplantRequest, TerrainCondition, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.RemoverActor
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{CapacitorStateType, ChatMessageType, Cosmetic, ExoSuitType, PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.concurrent.duration._
import scala.util.Success

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
    sessionLogic.updateBlockMap(player, pos)
    topOffHealthOfPlayer()
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
      case Some(container)
        if !container.HasGUID && (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID) => //just in case
          // Ensure we don't close the container if the player is seated in it
          val guid = player.GUID
          // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
          sendResponse(UnuseItemMessage(guid, guid))
          ops.unaccessContainer(container)
      case _ => ()
    }
    val channel = if (!player.spectator) {
      sessionLogic.updateBlockMap(player, pos)
      continent.id
    } else {
      "spectator"
    }
    val eagleEye: Boolean = ops.canSeeReallyFar
    val isNotVisible: Boolean = sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing ||
      (player.isAlive && sessionLogic.zoning.spawn.deadState == DeadState.RespawnTime)
    continent.AvatarEvents ! AvatarServiceMessage(
      channel,
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
    player.allowInteraction = false
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
        sessionLogic.zoning.CancelZoningProcess()
      case _ => ()
    }
  }

  def handlePickupItem(pkt: PickupItemMessage): Unit = {
    ops.handlePickupItem(pkt) match {
      case GeneralOperations.ItemPickupState.PickedUp =>
        sessionLogic.zoning.CancelZoningProcess()
      case _ => ()
    }
  }

  def handleObjectHeld(pkt: ObjectHeldMessage): Unit = {
    val ObjectHeldMessage(_, heldHolsters, _) = pkt
    player.Actor ! PlayerControl.ObjectHeld(heldHolsters)
  }

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit = { /* no stamina loss */ }

  def handleZipLine(pkt: ZipLineMessage): Unit = {
    ops.handleZipLine(pkt) match {
      case GeneralOperations.ZiplineBehavior.Teleporter | GeneralOperations.ZiplineBehavior.Zipline =>
        sessionLogic.zoning.CancelZoningProcess()
      case _ =>
        ()
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
    ops.handleMoveItem(pkt)
  }

  def handleLootItem(pkt: LootItemMessage): Unit = {
    ops.handleLootItem(pkt)
  }

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit = {
    ops.handleAvatarImplant(pkt) match {
      case GeneralOperations.ImplantActivationBehavior.Activate | GeneralOperations.ImplantActivationBehavior.Deactivate =>
        sessionLogic.zoning.CancelZoningProcess()
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
        ops.handleUseInternalTelepad(obj, pkt, ops.useRouterTelepadSystemSecretly)
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
    if (!player.spectator) {
      import scala.concurrent.ExecutionContext.Implicits.global
      val DeployObjectMessage(guid, _, pos, orient, _) = pkt
      player.Holsters().find(slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid).flatMap { slot => slot.Equipment } match {
        case Some(obj: ConstructionItem) =>
          sessionLogic.zoning.CancelZoningProcess()
          val result = ops.handleDeployObject(continent, obj.AmmoType, pos, orient, player.WhichSide, PlanetSideEmpire.NEUTRAL)
          result.onComplete {
            case Success(obj) => sendResponse(ChatMsg(ChatMessageType.UNK_227, s"${obj.GUID.guid}"))
            case _ => ()
          }
        case Some(obj) =>
          log.warn(s"DeployObject: what is $obj, ${player.Name}?  It's not a construction tool!")
        case None =>
          log.error(s"DeployObject: nothing, ${player.Name}?  It's not a construction tool!")
      }
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
    player.BailProtection = false
    val GenericCollisionMsg(ctype, p, _, _, pv, t, _, _, _, _, _, _) = pkt
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
        if user == player => ()
      case (CollisionIs.OfGroundVehicle, Some(v: Vehicle))
        if v.Seats(0).occupant.contains(player) =>
        v.BailProtection = false
      case (CollisionIs.OfAircraft, Some(v: Vehicle))
        if v.Definition.CanFly && v.Seats(0).occupant.contains(player) => ()
      case (CollisionIs.BetweenThings, Some(v: Vehicle)) =>
        v.Actor ! Vehicle.Deconstruct(Some(1 millisecond))
        continent.GUID(t) match {
          case Some(_: ForceDomePhysics) =>
            player.Actor ! Player.Die()
          case _ => ()
        }
      case (CollisionIs.BetweenThings, Some(_: Player)) =>
        continent.GUID(t) match {
          case Some(_: ForceDomePhysics) =>
            player.Actor ! Player.Die()
          case _ => ()
        }
      case (CollisionIs.BetweenThings, _) =>
        log.warn(s"GenericCollision: CollisionIs.BetweenThings detected - no handling case for obj id:${t.guid}")
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

  def handleDisplayedAward(pkt: DisplayedAwardMessage): Unit = { /* intentionally blank */ }

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit = {
    ops.handleObjectDetected(pkt)
  }

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit = {
    ops.handleTargetingImplantRequest(pkt)
  }

  def handleHitHint(pkt: HitHint): Unit = {
    val HitHint(_, _) = pkt
  }

  def handleOutfitMembershipRequest(pkt: OutfitMembershipRequest): Unit = {}

  def handleOutfitMembershipResponse(pkt: OutfitMembershipResponse): Unit = {}

  def handleOutfitRequest(pkt: OutfitRequest): Unit = {
    pkt match {
      case OutfitRequest(_, OutfitRequestAction.OutfitWindowOpen(true)) =>

      case OutfitRequest(_, OutfitRequestAction.OutfitWindowOpen(false)) =>

      case _ =>
    }
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
    ops.administrativeKick(player, time)
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
    if (!player.spectator) {
      //opens for everyone
      equipment match {
        case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
          door.Actor ! CommonMessages.Use(player, Some(Float.MaxValue))
        case _ =>
          door.Actor ! CommonMessages.Use(player)
      }
    }
  }

  private def maxCapacitorTick(jumpThrust: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      val activate = (jumpThrust || player.isOverdrived || player.isShielded) && player.Capacitor > 0
      player.CapacitorState match {
        case CapacitorStateType.Discharging => maxCapacitorTickDischarging(activate)
        case CapacitorStateType.Charging    => maxCapacitorTickCharging(activate)
        case _                              => maxCapacitorTickIdle(activate)
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
      player.CapacitorState = CapacitorStateType.Charging
    }
  }

  private def maxCapacitorTickDischarging(activate: Boolean): Unit = {
    if (activate) {
      val timeDiff    = (System.currentTimeMillis() - player.CapacitorLastUsedMillis).toFloat / 1000
      val drainAmount = player.ExoSuitDef.CapacitorDrainPerSecond.toFloat * timeDiff
      player.Capacitor -= drainAmount
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
    } else if (player.Capacitor == 0) {
      if (player.Faction == PlanetSideEmpire.TR) {
        ops.toggleMaxSpecialState(enable = false)
      }
      player.Capacitor = player.ExoSuitDef.MaxCapacitor.toFloat
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
      player.CapacitorState = CapacitorStateType.Idle
    } else if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      player.CapacitorState = CapacitorStateType.Charging
    } else {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  private def maxCapacitorTickCharging(activate: Boolean): Unit = {
    val maxCapacitor = player.ExoSuitDef.MaxCapacitor
    if (activate) {
      player.CapacitorState = CapacitorStateType.Discharging
      //maxCapacitorTickDischarging(activate)
    } else if (player.Capacitor < player.ExoSuitDef.MaxCapacitor) {
      player.Capacitor = maxCapacitor.toFloat
      sendResponse(PlanetsideAttributeMessage(player.GUID, 7, maxCapacitor))
    } else {
      player.CapacitorState = CapacitorStateType.Idle
    }
  }

  def topOffHealthOfPlayer(): Unit = {
    //below half health, full heal
    CustomerServiceRepresentativeMode.topOffHealthOfPlayer(sessionLogic, player)
    //below half stamina, full stamina
    val avatar = player.avatar
    val maxStamina = avatar.maxStamina
    if (avatar.stamina < maxStamina) {
      avatarActor ! AvatarActor.RestoreStamina(maxStamina)
      sendResponse(PlanetsideAttributeMessage(player.GUID, 2, maxStamina.toLong))
    }
  }
}
