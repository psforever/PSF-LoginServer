// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData}
import net.psforever.login.WorldSession.RemoveOldEquipmentFromInventory
import net.psforever.objects.{Account, BoomerDeployable, BoomerTrigger, GlobalDefinitions, LivePlayerList, PlanetSideGameObject, Player, TelepadDeployable, Tool, Vehicle}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.{Deployable, TelepadLike}
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.sourcing.{PlayerSource, VehicleSource}
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vital.{VehicleDismountActivity, VehicleMountActivity}
import net.psforever.objects.zones.{Zone, ZoneProjectile}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, ChatMsg, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, ImplantAction, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDeleteMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, PlayerStateShiftMessage, RequestDestroyMessage, ShiftState, TargetInfo, TargetingImplantRequest, TargetingInfoMessage, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostKill, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.RemoverActor
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{ChatMessageType, DriveState, ExoSuitType, PlanetSideGUID, Vector3}
import net.psforever.util.Config

object GeneralLogic {
  def apply(ops: GeneralOperations): GeneralLogic = {
    new GeneralLogic(ops, ops.context)
  }
}

class GeneralLogic(val ops: GeneralOperations, implicit val context: ActorContext) extends GeneralFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  private var customImplants = SpectatorModeLogic.SpectatorImplants.map(_.get)

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
    _/*seqTime*/,
    _,
    isCrouching,
    isJumping,
    _/*jumpThrust*/,
    isCloaking,
    _,
    _
    )= pkt
    sessionLogic.persist()
    sessionLogic.turnCounterFunc(avatarGuid)
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
    player.Cloaked = player.ExoSuit == ExoSuitType.Infiltration && isCloaking
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
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

  def handleDropItem(pkt: DropItemMessage): Unit = { /* intentionally blank */ }

  def handlePickupItem(pkt: PickupItemMessage): Unit = { /* intentionally blank */ }

  def handleObjectHeld(pkt: ObjectHeldMessage): Unit = {
    val ObjectHeldMessage(_, heldHolsters, _) = pkt
    if (heldHolsters != Player.HandsDownSlot && heldHolsters != 4) {
      sendResponse(ObjectHeldMessage(player.GUID, Player.HandsDownSlot, unk1=true))
    }
  }

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit = { /* intentionally blank */ }

  def handleZipLine(pkt: ZipLineMessage): Unit = {
    val ZipLineMessage(playerGuid, forwards, action, pathId, pos) = pkt
    continent.zipLinePaths.find(x => x.PathId == pathId) match {
      case Some(path) if path.IsTeleporter =>
        val endPoint = path.ZipLinePoints.last
        sendResponse(ZipLineMessage(PlanetSideGUID(0), forwards, 0, pathId, pos))
        //todo: send to zone to show teleport animation to all clients
        sendResponse(PlayerStateShiftMessage(ShiftState(0, endPoint, (player.Orientation.z + player.FacingYawUpper) % 360f, None)))
      case Some(_) =>
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

      case _ => ()
    }
  }

  def handleMoveItem(pkt: MoveItemMessage): Unit = { /* intentionally blank */ }

  def handleLootItem(pkt: LootItemMessage): Unit = { /* intentionally blank */ }

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit = {
    val AvatarImplantMessage(_, _, slot, _) = pkt
    customImplants.lift(slot)
      .collect {
        case implant if implant.active =>
          customImplants = customImplants.updated(slot, implant.copy(active = false))
          sendResponse(AvatarImplantMessage(player.GUID, ImplantAction.Activation, slot, 0))
          sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2))
        case implant =>
          customImplants = customImplants.updated(slot, implant.copy(active = true))
          sendResponse(AvatarImplantMessage(player.GUID, ImplantAction.Activation, slot, 1))
          sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2 + 1))
      }
  }

  def handleUseItem(pkt: UseItemMessage): Unit = {
    sessionLogic.validObject(pkt.object_guid, decorator = "UseItem") match {
      case Some(door: Door) =>
        handleUseDoor(door, None)
      case Some(obj: TelepadDeployable) =>
        handleUseTelepadDeployable(obj, None, pkt)
      case Some(obj: Utility.InternalTelepad) =>
        handleUseInternalTelepad(obj, pkt)
      case _ => ()
    }
  }

  def handleUnuseItem(pkt: UnuseItemMessage): Unit = { /* intentionally blank */ }

  def handleDeployObject(pkt: DeployObjectMessage): Unit = { /* intentionally blank */ }

  def handlePlanetsideAttribute(pkt: PlanetsideAttributeMessage): Unit = {
    val PlanetsideAttributeMessage(objectGuid, attributeType, _/*attributeValue*/) = pkt
    sessionLogic.validObject(objectGuid, decorator = "PlanetsideAttribute") match {
      case Some(_: Vehicle) => ()
      case Some(_: Player) if attributeType == 106 => ()
      case Some(obj) =>
        log.trace(s"PlanetsideAttribute: ${player.Name} does not know how to apply unknown attributes behavior $attributeType to ${obj.Definition.Name}")
      case _ => ()
    }
  }

  def handleGenericObjectAction(pkt: GenericObjectActionMessage): Unit = {
    val GenericObjectActionMessage(objectGuid, _/*code*/) = pkt
    sessionLogic.validObject(objectGuid, decorator = "GenericObjectAction") match {
      case Some(_: Vehicle) => ()
      case Some(_: Tool) => ()
      case _ => log.info(s"${player.Name} - $pkt")
    }
  }

  def handleGenericObjectActionAtPosition(pkt: GenericObjectActionAtPositionMessage): Unit = {
    val GenericObjectActionAtPositionMessage(objectGuid, _, _) = pkt
    sessionLogic.validObject(objectGuid, decorator = "GenericObjectActionAtPosition") match {
      case Some(tool: Tool) if GlobalDefinitions.isBattleFrameNTUSiphon(tool.Definition) => ()
      case _ => log.info(s"${player.Name} - $pkt")
    }
  }

  def handleGenericObjectState(pkt: GenericObjectStateMsg): Unit = {
    val GenericObjectStateMsg(_, _) = pkt
    log.info(s"${player.Name} - $pkt")
  }

  def handleGenericAction(pkt: GenericActionMessage): Unit = {
    val GenericActionMessage(action) = pkt
    val (toolOpt, definition) = player.Slot(0).Equipment match {
      case Some(tool: Tool) =>
        (Some(tool), tool.Definition)
      case _ =>
        (None, GlobalDefinitions.bullet_9mm)
    }
    action match {
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

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit = { /* intentionally blank */ }

  def handleAvatarFirstTimeEvent(pkt: AvatarFirstTimeEventMessage): Unit = { /* intentionally blank */ }

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

  def handleFacilityBenefitShieldChargeRequest(pkt: FacilityBenefitShieldChargeRequestMessage): Unit = { /* intentionally blank */ }

  def handleBattleplan(pkt: BattleplanMessage): Unit = {
    val BattleplanMessage(_, name, _, _) = pkt
    val lament: String = s"$name has a brilliant idea that no one will ever see"
    log.info(lament)
    log.debug(s"Battleplan: $lament - $pkt")
  }

  def handleBindPlayer(pkt: BindPlayerMessage): Unit = {
    val BindPlayerMessage(_, _, _, _, _, _, _, _) = pkt
  }

  def handleCreateShortcut(pkt: CreateShortcutMessage): Unit = { /* intentionally blank */ }

  def handleChangeShortcutBank(pkt: ChangeShortcutBankMessage): Unit = { /* intentionally blank */ }

  def handleFriendRequest(pkt: FriendsRequest): Unit = {
    val FriendsRequest(action, name) = pkt
    avatarActor ! AvatarActor.MemberListRequest(action, name)
  }

  def handleInvalidTerrain(pkt: InvalidTerrainMessage): Unit = { /* intentionally blank */ }

  def handleActionCancel(pkt: ActionCancelMessage): Unit = {
    val ActionCancelMessage(_, _, _) = pkt
    ops.progressBarUpdate.cancel()
    ops.progressBarValue = None
  }

  def handleTrade(pkt: TradeMessage): Unit = { /* intentionally blank */ }

  def handleDisplayedAward(pkt: DisplayedAwardMessage): Unit = {
    val DisplayedAwardMessage(_, ribbon, bar) = pkt
    log.trace(s"${player.Name} changed the $bar displayed award ribbon to $ribbon")
    avatarActor ! AvatarActor.SetRibbon(ribbon, bar)
  }

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit = { /* intentionally blank */ }

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

  def handleHitHint(pkt: HitHint): Unit = { /* intentionally blank */ }

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
    administrativeKick(player)
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(player.Name, time)
  }

  def handleSilenced(isSilenced: Boolean): Unit = {
    player.silenced = isSilenced
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

  private def handleUseTelepadDeployable(obj: TelepadDeployable, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    if (equipment.isEmpty) {
      (continent.GUID(obj.Router) match {
        case Some(vehicle: Vehicle) => Some((vehicle, vehicle.Utility(UtilityType.internal_router_telepad_deployable)))
        case Some(vehicle) => Some(vehicle, None)
        case None => None
      }) match {
        case Some((vehicle: Vehicle, Some(util: Utility.InternalTelepad))) =>
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

  private def administrativeKick(tplayer: Player): Unit = {
    log.warn(s"${tplayer.Name} has been kicked by ${player.Name}")
    tplayer.death_by = -1
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(tplayer.Name)
  }
}
