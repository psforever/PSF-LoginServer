// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GeneralFunctions, GeneralOperations, SessionData}
import net.psforever.objects.{Account, GlobalDefinitions, LivePlayerList, Player, TelepadDeployable, Tool, Vehicle}
import net.psforever.objects.avatar.{Avatar, Implant}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.definition.{BasicDefinition, KitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.dome.ForceDomePhysics
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.ZoneProjectile
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeFireModeMessage, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, CollisionIs, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DisplayedAwardMessage, DropItemMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericAction, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, ImplantAction, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, OutfitMembershipRequest, OutfitMembershipResponse, OutfitRequest, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UseItemMessage, VoiceHostInfo, VoiceHostRequest, ZipLineMessage}
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{ExoSuitType, Vector3}

import scala.concurrent.duration.DurationInt

object GeneralLogic {
  def apply(ops: GeneralOperations): GeneralLogic = {
    new GeneralLogic(ops, ops.context)
  }
}

class GeneralLogic(val ops: GeneralOperations, implicit val context: ActorContext) extends GeneralFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  private var customImplants = SpectatorModeLogic.SpectatorImplants.map(_.get)

  private var additionalImplants: Seq[CreateShortcutMessage] = Seq()

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
    _/*jumpThrust*/,
    isCloaking,
    _,
    _
    )= pkt
    sessionLogic.zoning.spawn.tryQueuedActivity(vel)
    sessionLogic.persist()
    sessionLogic.turnCounterFunc(avatarGuid)
    sessionLogic.updateBlockMap(player, pos)
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
    continent.AvatarEvents ! AvatarServiceMessage(
      "spectator",
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
        jump_thrust = false,
        is_cloaked = isCloaking,
        spectator = false,
        weaponInHand = false
      )
    )
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
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
    ops.handleZipLine(pkt) match {
      case GeneralOperations.ZiplineBehavior.Teleporter | GeneralOperations.ZiplineBehavior.Zipline =>
        sessionLogic.zoning.CancelZoningProcess()
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
      case Some(obj: Projectile) =>
        if (!obj.isResolved) {
          obj.Miss()
        }
        continent.Projectile ! ZoneProjectile.Remove(objectGuid)

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
          customImplantOff(slot, implant)
        case implant =>
          customImplants = customImplants.updated(slot, implant.copy(active = true))
          sendResponse(AvatarImplantMessage(player.GUID, ImplantAction.Activation, slot, 1))
          sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2 + 1))
      }
  }

  def handleUseItem(pkt: UseItemMessage): Unit = {
    sessionLogic.validObject(pkt.object_guid, decorator = "UseItem") match {
      case Some(door: Door) =>
        ops.handleUseDoor(door, None)
      case Some(obj: TelepadDeployable) =>
        ops.handleUseTelepadDeployable(obj, None, pkt, ops.useRouterTelepadSystemSecretly)
      case Some(obj: Utility.InternalTelepad) =>
        ops.handleUseInternalTelepad(obj, pkt, ops.useRouterTelepadSystemSecretly)
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
        player.AwayFromKeyboard = true
      case GenericAction.BackInGame_RCV =>
        log.info(s"${player.Name} is back")
        player.AwayFromKeyboard = false
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
      case _ => ()
    }
  }

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

  def handleCreateShortcut(pkt: CreateShortcutMessage): Unit = {
    val CreateShortcutMessage(_, slot, wouldBeImplant) = pkt
    val pguid = player.GUID
    if (slot > 1 && slot < 5) {
      //protected
      customImplants
        .zipWithIndex
        .find { case (_, index) => index + 2 == slot}
        .foreach {
          case (implant, _) if wouldBeImplant.contains(implant.definition.implantType.shortcut) => ()
          case (implant, _) if implant.active =>
            sendResponse(CreateShortcutMessage(pguid, slot, Some(implant.definition.implantType.shortcut)))
            customImplantOff(slot, implant)
          case (implant, _) =>
            sendResponse(CreateShortcutMessage(pguid, slot, Some(implant.definition.implantType.shortcut)))
        }
    } else {
      additionalImplants.indexWhere(_.slot == slot) match {
        case -1 => ()
        case index =>
          additionalImplants = additionalImplants.take(index) ++ additionalImplants.drop(index + 1)
      }
      wouldBeImplant.collect {
        case _ =>
          additionalImplants = additionalImplants :+ pkt
      }
    }
  }

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

  def handleDisplayedAward(pkt: DisplayedAwardMessage): Unit = { /* intentionally blank */ }

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit = {
    ops.handleObjectDetected(pkt)
  }

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit = {
    ops.handleTargetingImplantRequest(pkt)
  }

  def handleHitHint(pkt: HitHint): Unit = { /* intentionally blank */ }

  def handleOutfitMembershipRequest(pkt: OutfitMembershipRequest): Unit = {}

  def handleOutfitMembershipResponse(pkt: OutfitMembershipResponse): Unit = {}

  def handleOutfitRequest(pkt: OutfitRequest): Unit = {}

  /* messages */

  def handleRenewCharSavedTimer(): Unit = { /* intentionally blank */ }

  def handleRenewCharSavedTimerMsg(): Unit = { /* intentionally blank */ }

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
    ops.administrativeKick(player, None)
  }

  def handleSilenced(isSilenced: Boolean): Unit = {
    player.silenced = isSilenced
  }

  def handleItemPutInSlot(msg: Containable.ItemPutInSlot): Unit = { /* intentionally blank */ }

  def handleCanNotPutItemInSlot(msg: Containable.CanNotPutItemInSlot): Unit = { /* intentionally blank */ }

  def handleReceiveDefaultMessage(default: Any, sender: ActorRef): Unit = { /* intentionally blank */ }

  /* supporting functions */

  private def customImplantOff(slot: Int, implant: Implant): Unit = {
    customImplants = customImplants.updated(slot, implant.copy(active = false))
    sendResponse(AvatarImplantMessage(player.GUID, ImplantAction.Activation, slot, 0))
    sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2))
  }

  override protected[session] def stop(): Unit = {
    val pguid = player.GUID
    //set only originally blank slots blank again; rest will be overwrote later
    val originalBlankSlots = ((player.avatar.shortcuts.head, 1) +:
      player.avatar.shortcuts.drop(4).zipWithIndex.map { case (scut, slot) => (scut, slot + 4) })
        .collect { case (None, slot) => slot }
    additionalImplants
      .map(_.slot)
      .filter(originalBlankSlots.contains)
      .map(slot => CreateShortcutMessage(pguid, slot, None))
      .foreach(sendResponse)
  }
}
