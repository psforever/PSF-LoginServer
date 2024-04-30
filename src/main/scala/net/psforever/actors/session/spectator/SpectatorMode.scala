// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import net.psforever.actors.session.ChatActor
import net.psforever.actors.session.support.{AvatarHandlerFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{BattleRank, CommandRank, DeployableToolbox, FirstTimeEvents, Implant, ProgressDecoration, Shortcut => AvatarShortcut}
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.{GlobalDefinitions, Player, Session, SimpleItem, Vehicle}
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.{ObjectCreateDetailedMessage, ObjectDeleteMessage}
import net.psforever.packet.game.objectcreate.{ObjectClass, ObjectCreateMessageParent, RibbonBars}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.teamwork.{SquadAction, SquadServiceMessage}
import net.psforever.types.{CapacitorStateType, ChatMessageType, ExoSuitType, MeritCommendation, SquadRequestType}
//
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData, ZoningOperations}
import net.psforever.objects.TurretDeployable
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{AIDamage, ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarGrenadeStateMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BeginZoningMessage, BindPlayerMessage, BugReportMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, ChatMsg, ChildObjectStateMessage, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DeployRequestMessage, DismountVehicleCargoMsg, DismountVehicleMsg, DisplayedAwardMessage, DropItemMessage, DroppodLaunchRequestMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FavoritesRequest, FrameVehicleStateMessage, FriendsRequest, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, HitMessage, InvalidTerrainMessage, ItemTransactionMessage, KeepAliveMessage, LashMessage, LongRangeProjectileInfoMessage, LootItemMessage, MountVehicleCargoMsg, MountVehicleMsg, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, OutfitRequest, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, ProjectileStateMessage, ProximityTerminalUseMessage, ReleaseAvatarRequestMessage, ReloadMessage, RequestDestroyMessage, SetChatFilterMessage, SpawnRequestMessage, SplashHitMessage, SquadDefinitionActionMessage, SquadMembershipRequest, SquadWaypointRequest, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UplinkRequest, UseItemMessage, VehicleStateMessage, VehicleSubStateMessage, VoiceHostInfo, VoiceHostRequest, WarpgateRequest, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage, ZipLineMessage}
import net.psforever.services.{InterstellarClusterService => ICS}
import net.psforever.services.CavernRotationService
import net.psforever.services.CavernRotationService.SendCavernRotationUpdates
import net.psforever.services.ServiceManager.LookupResult
import net.psforever.services.account.{PlayerToken, ReceiveAccountData}
import net.psforever.services.avatar.AvatarServiceResponse
import net.psforever.services.galaxy.GalaxyServiceResponse
import net.psforever.services.local.LocalServiceResponse
import net.psforever.services.teamwork.SquadServiceResponse
import net.psforever.services.vehicle.VehicleServiceResponse

class SpectatorModeLogic(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerFunctions = AvatarHandlerLogic(data.avatarResponse)
  val galaxy: GalaxyHandlerFunctions = GalaxyHandlerLogic(data.galaxyResponseHandlers)
  val general: GeneralFunctions = GeneralLogic(data.general)
  val local: LocalHandlerFunctions = LocalHandlerLogic(data.localResponse)
  val mountResponse: MountHandlerFunctions = MountHandlerLogic(data.mountResponse)
  val shooting: WeaponAndProjectileFunctions = WeaponAndProjectileLogic(data.shooting)
  val squad: SquadHandlerFunctions = SquadHandlerLogic(data.squad)
  val terminals: TerminalHandlerFunctions = TerminalHandlerLogic(data.terminals)
  val vehicles: VehicleFunctions = VehicleLogic(data.vehicles)
  val vehicleResponse: VehicleHandlerFunctions = VehicleHandlerLogic(data.vehicleResponseOperations)

  override def switchTo(session: Session): Unit = {
    val player = session.player
    val continent = session.zone
    val pguid = player.GUID
    val sendResponse: PlanetSidePacket=>Unit = data.sendResponse
    //
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "on"))
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SpectatorEnabled"))
    continent.actor ! ZoneActor.RemoveFromBlockMap(player)
    data.general.avatarActor ! AvatarActor.DeactivateActiveImplants()
    continent
      .GUID(data.terminals.usingMedicalTerminal)
      .foreach { case term: Terminal with ProximityUnit =>
        data.terminals.StopUsingProximityUnit(term)
      }
    data.general.accessedContainer
      .collect {
        case veh: Vehicle if player.VehicleSeated.isEmpty || player.VehicleSeated.get != veh.GUID =>
          sendResponse(UnuseItemMessage(pguid, veh.GUID))
          sendResponse(UnuseItemMessage(pguid, pguid))
          data.general.unaccessContainer(veh)
        case container => //just in case
          if (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID) {
            // Ensure we don't close the container if the player is seated in it
            // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
            if (container.HasGUID) {
              sendResponse(UnuseItemMessage(pguid, container.GUID))
            }
            sendResponse(UnuseItemMessage(pguid, pguid))
            data.general.unaccessContainer(container)
          }
      }
    player.CapacitorState = CapacitorStateType.Idle
    player.Capacitor = 0f
    player.Inventory.Items
      .foreach { entry => sendResponse(ObjectDeleteMessage(entry.GUID, 0)) }
    sendResponse(ObjectDeleteMessage(player.avatar.locker.GUID, 0))
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(pguid, pguid))
    player.Holsters()
      .collect { case slot if slot.Equipment.nonEmpty => sendResponse(ObjectDeleteMessage(slot.Equipment.get.GUID, 0)) }
    val vehicleAndSeat = data.vehicles.GetMountableAndSeat(None, player, continent) match {
      case (Some(obj: Vehicle), Some(seatNum)) if seatNum == 0 =>
        data.vehicles.ServerVehicleOverrideStop(obj)
        obj.Actor ! ServerObject.AttributeMsg(10, 3) //faction-accessible driver seat
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case (Some(obj), Some(seatNum)) =>
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case _ =>
        None
    }
    data.general.dropSpecialSlotItem()
    data.general.toggleMaxSpecialState(enable = false)
    data.terminals.CancelAllProximityUnits()
    data.terminals.lastTerminalOrderFulfillment = true
    data.squadService ! SquadServiceMessage(
      player,
      continent,
      SquadAction.Membership(SquadRequestType.Leave, player.CharId, Some(player.CharId), player.Name, None)
    )
    val originalEvent = player.History.headOption
    player.ClearHistory()
    player.LogActivity(originalEvent)
    player.spectator = true
    //
    data.general.chatActor ! ChatActor.SetMode("spectator")
    val newPlayer = SpectatorModeLogic.spectatorCharacter(player)
    val cud = new SimpleItem(GlobalDefinitions.command_detonater)
    cud.GUID = player.avatar.locker.GUID
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      ObjectClass.avatar,
      pguid,
      vehicleAndSeat,
      newPlayer.Definition.Packet.DetailedConstructorData(newPlayer).get
    ))
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      ObjectClass.command_detonater,
      cud.GUID,
      Some(ObjectCreateMessageParent(pguid, 4)),
      cud.Definition.Packet.DetailedConstructorData(cud).get
    ))
    data.zoning.spawn.HandleSetCurrentAvatar(newPlayer)
    data.session = session.copy(player = player)
  }

  override def switchFrom(session: Session): Unit = {
    import scala.concurrent.duration._
    val player = data.player
    val zoning = data.zoning
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    player.spectator = false
    sendResponse(ObjectDeleteMessage(player.avatar.locker.GUID, 0)) //free up the slot (from cud)
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "off"))
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SpectatorDisabled"))
    zoning.spawn.randomRespawn(0.seconds) //to sanctuary
  }

  def parse(sender: ActorRef): Receive = {
    /* really common messages (very frequently, every life) */
    case packet: PlanetSideGamePacket =>
      handleGamePkt(packet)

    case AvatarServiceResponse(toChannel, guid, reply) =>
      avatarResponse.handle(toChannel, guid, reply)

    case GalaxyServiceResponse(_, reply) =>
      galaxy.handle(reply)

    case LocalServiceResponse(toChannel, guid, reply) =>
      local.handle(toChannel, guid, reply)

    case Mountable.MountMessages(tplayer, reply) =>
      mountResponse.handle(tplayer, reply)

    case SquadServiceResponse(_, excluded, response) =>
      squad.handle(response, excluded)

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      terminals.handle(tplayer, msg, order)

    case VehicleServiceResponse(toChannel, guid, reply) =>
      vehicleResponse.handle(toChannel, guid, reply)

    case SessionActor.SendResponse(packet) =>
      data.sendResponse(packet)

    case SessionActor.CharSaved => ()

    case SessionActor.CharSavedMsg => ()

    /* common messages (maybe once every respawn) */
    case ICS.SpawnPointResponse(response) =>
      data.zoning.handleSpawnPointResponse(response)

    case SessionActor.NewPlayerLoaded(tplayer) =>
      data.zoning.spawn.handleNewPlayerLoaded(tplayer)

    case SessionActor.PlayerLoaded(tplayer) =>
      data.zoning.spawn.handlePlayerLoaded(tplayer)

    case Zone.Population.PlayerHasLeft(zone, None) =>
      data.log.debug(s"PlayerHasLeft: ${data.player.Name} does not have a body on ${zone.id}")

    case Zone.Population.PlayerHasLeft(zone, Some(tplayer)) =>
      if (tplayer.isAlive) {
        data.log.info(s"${tplayer.Name} has left zone ${zone.id}")
      }

    case Zone.Population.PlayerCanNotSpawn(zone, tplayer) =>
      data.log.warn(s"${tplayer.Name} can not spawn in zone ${zone.id}; why?")

    case Zone.Population.PlayerAlreadySpawned(zone, tplayer) =>
      data.log.warn(s"${tplayer.Name} is already spawned on zone ${zone.id}; is this a clerical error?")

    case Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      data.log.warn(
        s"${data.player.Name}'s ${vehicle.Definition.Name} can not spawn in ${zone.id} because $reason"
      )

    case Zone.Vehicle.CanNotDespawn(zone, vehicle, reason) =>
      data.log.warn(
        s"${data.player.Name}'s ${vehicle.Definition.Name} can not deconstruct in ${zone.id} because $reason"
      )

    case ICS.ZoneResponse(Some(zone)) =>
      data.zoning.handleZoneResponse(zone)

    /* uncommon messages (once a session) */
    case ICS.ZonesResponse(zones) =>
      data.zoning.handleZonesResponse(zones)

    case SessionActor.SetAvatar(avatar) =>
      general.handleSetAvatar(avatar)

    case PlayerToken.LoginInfo(name, Zone.Nowhere, _) =>
      data.zoning.spawn.handleLoginInfoNowhere(name, sender)

    case PlayerToken.LoginInfo(name, inZone, optionalSavedData) =>
      data.zoning.spawn.handleLoginInfoSomewhere(name, inZone, optionalSavedData, sender)

    case PlayerToken.RestoreInfo(playerName, inZone, pos) =>
      data.zoning.spawn.handleLoginInfoRestore(playerName, inZone, pos, sender)

    case PlayerToken.CanNotLogin(playerName, reason) =>
      data.zoning.spawn.handleLoginCanNot(playerName, reason)

    case ReceiveAccountData(account) =>
      general.handleReceiveAccountData(account)

    case AvatarActor.AvatarResponse(avatar) =>
      general.handleAvatarResponse(avatar)

    case AvatarActor.AvatarLoginResponse(avatar) =>
      data.zoning.spawn.avatarLoginResponse(avatar)

    case SessionActor.SetCurrentAvatar(tplayer, max_attempts, attempt) =>
      data.zoning.spawn.ReadyToSetCurrentAvatar(tplayer, max_attempts, attempt)

    case SessionActor.SetConnectionState(state) =>
      data.connectionState = state

    case SessionActor.AvatarLoadingSync(state) =>
      data.zoning.spawn.handleAvatarLoadingSync(state)

    /* uncommon messages (utility, or once in a while) */
    case ZoningOperations.AvatarAwardMessageBundle(pkts, delay) =>
      data.zoning.spawn.performAvatarAwardMessageDelivery(pkts, delay)

    case CommonMessages.ProgressEvent(delta, finishedAction, stepAction, tick) =>
      general.ops.handleProgressChange(delta, finishedAction, stepAction, tick)

    case CommonMessages.Progress(rate, finishedAction, stepAction) =>
      general.ops.setupProgressChange(rate, finishedAction, stepAction)

    case CavernRotationService.CavernRotationServiceKey.Listing(listings) =>
      listings.head ! SendCavernRotationUpdates(data.context.self)

    case LookupResult("propertyOverrideManager", endpoint) =>
      data.zoning.propertyOverrideManagerLoadOverrides(endpoint)

    case SessionActor.UpdateIgnoredPlayers(msg) =>
      galaxy.handleUpdateIgnoredPlayers(msg)

    case SessionActor.UseCooldownRenewed(definition, _) =>
      general.handleUseCooldownRenew(definition)

    case Deployment.CanDeploy(obj, state) =>
      vehicles.handleCanDeploy(obj, state)

    case Deployment.CanUndeploy(obj, state) =>
      vehicles.handleCanUndeploy(obj, state)

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      vehicles.handleCanNotChangeDeployment(obj, state, reason)

    /* rare messages */
    case ProximityUnit.StopAction(term, _) =>
      terminals.ops.LocalStopUsingProximityUnit(term)

    case SessionActor.Suicide() =>
      general.ops.suicide(data.player)

    case SessionActor.Recall() =>
      data.zoning.handleRecall()

    case SessionActor.InstantAction() =>
      data.zoning.handleInstantAction()

    case SessionActor.Quit() =>
      data.zoning.handleQuit()

    case ICS.DroppodLaunchDenial(errorCode, _) =>
      data.zoning.handleDroppodLaunchDenial(errorCode)

    case ICS.DroppodLaunchConfirmation(zone, position) =>
      data.zoning.LoadZoneLaunchDroppod(zone, position)

    case SessionActor.PlayerFailedToLoad(tplayer) =>
      data.failWithError(s"${tplayer.Name} failed to load anywhere")

    /* csr only */
    case SessionActor.SetSpeed(speed) =>
      general.handleSetSpeed(speed)

    case SessionActor.SetFlying(isFlying) =>
      general.handleSetFlying(isFlying)

    case SessionActor.SetSpectator(isSpectator) =>
      general.handleSetSpectator(isSpectator)

    case SessionActor.Kick(player, time) =>
      general.handleKick(player, time)

    case SessionActor.SetZone(zoneId, position) =>
      data.zoning.handleSetZone(zoneId, position)

    case SessionActor.SetPosition(position) =>
      data.zoning.spawn.handleSetPosition(position)

    case SessionActor.SetSilenced(silenced) =>
      general.handleSilenced(silenced)

    /* catch these messages */
    case _: ProximityUnit.Action => ;

    case _: Zone.Vehicle.HasSpawned => ;

    case _: Zone.Vehicle.HasDespawned => ;

    case Zone.Deployable.IsDismissed(obj: TurretDeployable) => //only if target deployable was never fully introduced
      TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(data.continent.GUID, obj))

    case Zone.Deployable.IsDismissed(obj) => //only if target deployable was never fully introduced
      TaskWorkflow.execute(GUIDTask.unregisterObject(data.continent.GUID, obj))

    case msg: Containable.ItemPutInSlot =>
      data.log.debug(s"ItemPutInSlot: $msg")

    case msg: Containable.CanNotPutItemInSlot =>
      data.log.debug(s"CanNotPutItemInSlot: $msg")

    case _ => ()
  }

  private def handleGamePkt: PlanetSideGamePacket => Unit = {
    case packet: ConnectToWorldRequestMessage =>
      general.handleConnectToWorldRequest(packet)

    case packet: MountVehicleCargoMsg =>
      mountResponse.handleMountVehicleCargo(packet)

    case packet: DismountVehicleCargoMsg =>
      mountResponse.handleDismountVehicleCargo(packet)

    case packet: CharacterCreateRequestMessage =>
      general.handleCharacterCreateRequest(packet)

    case packet: CharacterRequestMessage =>
      general.handleCharacterRequest(packet)

    case _: KeepAliveMessage =>
      data.keepAliveFunc()

    case packet: BeginZoningMessage =>
      data.zoning.handleBeginZoning(packet)

    case packet: PlayerStateMessageUpstream =>
      general.handlePlayerStateUpstream(packet)

    case packet: ChildObjectStateMessage =>
      vehicles.handleChildObjectState(packet)

    case packet: VehicleStateMessage =>
      vehicles.handleVehicleState(packet)

    case packet: VehicleSubStateMessage =>
      vehicles.handleVehicleSubState(packet)

    case packet: FrameVehicleStateMessage =>
      vehicles.handleFrameVehicleState(packet)

    case packet: ProjectileStateMessage =>
      shooting.handleProjectileState(packet)

    case packet: LongRangeProjectileInfoMessage =>
      shooting.handleLongRangeProjectileState(packet)

    case packet: ReleaseAvatarRequestMessage =>
      data.zoning.spawn.handleReleaseAvatarRequest(packet)

    case packet: SpawnRequestMessage =>
      data.zoning.spawn.handleSpawnRequest(packet)

    case packet: ChatMsg =>
      general.handleChat(packet)

    case packet: SetChatFilterMessage =>
      general.handleChatFilter(packet)

    case packet: VoiceHostRequest =>
      general.handleVoiceHostRequest(packet)

    case packet: VoiceHostInfo =>
      general.handleVoiceHostInfo(packet)

    case packet: ChangeAmmoMessage =>
      shooting.handleChangeAmmo(packet)

    case packet: ChangeFireModeMessage =>
      shooting.handleChangeFireMode(packet)

    case packet: ChangeFireStateMessage_Start =>
      shooting.handleChangeFireStateStart(packet)

    case packet: ChangeFireStateMessage_Stop =>
      shooting.handleChangeFireStateStop(packet)

    case packet: EmoteMsg =>
      general.handleEmote(packet)

    case packet: DropItemMessage =>
      general.handleDropItem(packet)

    case packet: PickupItemMessage =>
      general.handlePickupItem(packet)

    case packet: ReloadMessage =>
      shooting.handleReload(packet)

    case packet: ObjectHeldMessage =>
      general.handleObjectHeld(packet)

    case packet: AvatarJumpMessage =>
      general.handleAvatarJump(packet)

    case packet: ZipLineMessage =>
      general.handleZipLine(packet)

    case packet: RequestDestroyMessage =>
      general.handleRequestDestroy(packet)

    case packet: MoveItemMessage =>
      general.handleMoveItem(packet)

    case packet: LootItemMessage =>
      general.handleLootItem(packet)

    case packet: AvatarImplantMessage =>
      general.handleAvatarImplant(packet)

    case packet: UseItemMessage =>
      general.handleUseItem(packet)

    case packet: UnuseItemMessage =>
      general.handleUnuseItem(packet)

    case packet: ProximityTerminalUseMessage =>
      terminals.handleProximityTerminalUse(packet)

    case packet: DeployObjectMessage =>
      general.handleDeployObject(packet)

    case packet: GenericObjectActionMessage =>
      general.handleGenericObjectAction(packet)

    case packet: GenericObjectActionAtPositionMessage =>
      general.handleGenericObjectActionAtPosition(packet)

    case packet: GenericObjectStateMsg =>
      general.handleGenericObjectState(packet)

    case packet: GenericActionMessage =>
      general.handleGenericAction(packet)

    case packet: ItemTransactionMessage =>
      terminals.handleItemTransaction(packet)

    case packet: FavoritesRequest =>
      terminals.handleFavoritesRequest(packet)

    case packet: WeaponDelayFireMessage =>
      shooting.handleWeaponDelayFire(packet)

    case packet: WeaponDryFireMessage =>
      shooting.handleWeaponDryFire(packet)

    case packet: WeaponFireMessage =>
      shooting.handleWeaponFire(packet)

    case packet: WeaponLazeTargetPositionMessage =>
      shooting.handleWeaponLazeTargetPosition(packet)

    case packet: UplinkRequest =>
      shooting.handleUplinkRequest(packet)

    case packet: HitMessage =>
      shooting.handleDirectHit(packet)

    case packet: SplashHitMessage =>
      shooting.handleSplashHit(packet)

    case packet: LashMessage =>
      shooting.handleLashHit(packet)

    case packet: AIDamage =>
      shooting.handleAIDamage(packet)

    case packet: AvatarFirstTimeEventMessage =>
      general.handleAvatarFirstTimeEvent(packet)

    case packet: WarpgateRequest =>
      data.zoning.handleWarpgateRequest(packet)

    case packet: MountVehicleMsg =>
      mountResponse.handleMountVehicle(packet)

    case packet: DismountVehicleMsg =>
      mountResponse.handleDismountVehicle(packet)

    case packet: DeployRequestMessage =>
      vehicles.handleDeployRequest(packet)

    case packet: AvatarGrenadeStateMessage =>
      shooting.handleAvatarGrenadeState(packet)

    case packet: SquadDefinitionActionMessage =>
      squad.handleSquadDefinitionAction(packet)

    case packet: SquadMembershipRequest =>
      squad.handleSquadMemberRequest(packet)

    case packet: SquadWaypointRequest =>
      squad.handleSquadWaypointRequest(packet)

    case packet: GenericCollisionMsg =>
      general.handleGenericCollision(packet)

    case packet: BugReportMessage =>
      general.handleBugReport(packet)

    case packet: BindPlayerMessage =>
      general.handleBindPlayer(packet)

    case packet: PlanetsideAttributeMessage =>
      general.handlePlanetsideAttribute(packet)

    case packet: FacilityBenefitShieldChargeRequestMessage =>
      general.handleFacilityBenefitShieldChargeRequest(packet)

    case packet: BattleplanMessage =>
      general.handleBattleplan(packet)

    case packet: CreateShortcutMessage =>
      general.handleCreateShortcut(packet)

    case packet: ChangeShortcutBankMessage =>
      general.handleChangeShortcutBank(packet)

    case packet: FriendsRequest =>
      general.handleFriendRequest(packet)

    case packet: DroppodLaunchRequestMessage =>
      data.zoning.handleDroppodLaunchRequest(packet)

    case packet: InvalidTerrainMessage =>
      general.handleInvalidTerrain(packet)

    case packet: ActionCancelMessage =>
      general.handleActionCancel(packet)

    case packet: TradeMessage =>
      general.handleTrade(packet)

    case packet: DisplayedAwardMessage =>
      general.handleDisplayedAward(packet)

    case packet: ObjectDetectedMessage =>
      general.handleObjectDetected(packet)

    case packet: TargetingImplantRequest =>
      general.handleTargetingImplantRequest(packet)

    case packet: HitHint =>
      general.handleHitHint(packet)

    case _: OutfitRequest => ()

    case pkt =>
      data.log.warn(s"Unhandled GamePacket $pkt")
  }
}

object SpectatorModeLogic {
  final val SpectatorImplants: Seq[Option[Implant]] = Seq(
    Some(Implant(GlobalDefinitions.targeting, initialized = true)),
    Some(Implant(GlobalDefinitions.darklight_vision, initialized = true)),
    Some(Implant(GlobalDefinitions.range_magnifier, initialized = true))
  )

  private def spectatorCharacter(player: Player): Player = {
    val avatar = player.avatar
    val newAvatar = avatar.copy(
      basic = avatar.basic.copy(name = "spectator"),
      bep = BattleRank.BR18.experience,
      cep = CommandRank.CR5.experience,
      certifications = Set(),
      decoration = ProgressDecoration(
        ribbonBars = RibbonBars(
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor
        ),
        firstTimeEvents = FirstTimeEvents.All
      ),
      deployables = {
        val dt = new DeployableToolbox()
        dt.Initialize(Set())
        dt
      },
      implants = SpectatorImplants,
      lookingForSquad = false,
      shortcuts = {
        val allShortcuts: Array[Option[AvatarShortcut]] = Array.fill[Option[AvatarShortcut]](64)(None)
        SpectatorImplants.zipWithIndex.collect { case (Some(implant), slot) =>
          allShortcuts.update(slot + 1, Some(AvatarShortcut(2, implant.definition.Name)))
        }
        allShortcuts
      }
    )
    val newPlayer = Player(newAvatar)
    newPlayer.GUID = player.GUID
    newPlayer.ExoSuit = ExoSuitType.Infiltration
    newPlayer.Position = player.Position
    newPlayer.Orientation = player.Orientation
    newPlayer.spectator = true
    newPlayer.Spawn()
    newPlayer
  }
}

case object SpectatorMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new SpectatorModeLogic(data)
  }
}
