// Copyright (c) 2016, 2020, 2024 PSForever
package net.psforever.actors.session

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware, typed}
import net.psforever.actors.session.normal.NormalMode
import net.psforever.actors.session.support.ZoningOperations
import net.psforever.objects.TurretDeployable
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{AIDamage, ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarGrenadeStateMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BeginZoningMessage, BindPlayerMessage, BugReportMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ChangeShortcutBankMessage, CharacterCreateRequestMessage, CharacterRequestMessage, ChatMsg, ChildObjectStateMessage, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DeployRequestMessage, DismountVehicleCargoMsg, DismountVehicleMsg, DisplayedAwardMessage, DropItemMessage, DroppodLaunchRequestMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FavoritesRequest, FrameVehicleStateMessage, FriendsRequest, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, HitMessage, InvalidTerrainMessage, ItemTransactionMessage, LashMessage, LongRangeProjectileInfoMessage, LootItemMessage, MountVehicleCargoMsg, MountVehicleMsg, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, OutfitRequest, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, ProjectileStateMessage, ProximityTerminalUseMessage, ReleaseAvatarRequestMessage, ReloadMessage, RequestDestroyMessage, SetChatFilterMessage, SpawnRequestMessage, SplashHitMessage, SquadDefinitionActionMessage, SquadMembershipRequest, SquadWaypointRequest, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UplinkRequest, UseItemMessage, VehicleStateMessage, VehicleSubStateMessage, VoiceHostInfo, VoiceHostRequest, WarpgateRequest, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage, ZipLineMessage}
import net.psforever.services.{InterstellarClusterService => ICS}
import net.psforever.services.CavernRotationService
import net.psforever.services.CavernRotationService.SendCavernRotationUpdates
import net.psforever.services.ServiceManager.LookupResult
import net.psforever.services.account.{PlayerToken, ReceiveAccountData}
import net.psforever.services.avatar.AvatarServiceResponse
import net.psforever.services.chat.ChatService
import net.psforever.services.galaxy.GalaxyServiceResponse
import net.psforever.services.local.LocalServiceResponse
import net.psforever.services.teamwork.SquadServiceResponse
import net.psforever.services.vehicle.VehicleServiceResponse
import org.joda.time.LocalDateTime
import org.log4s.MDC

import scala.collection.mutable
//
import net.psforever.actors.net.MiddlewareActor
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}
import net.psforever.objects.{Default, Player}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.BasicDefinition
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.{FriendsResponse, KeepAliveMessage}
import net.psforever.types.Vector3

object SessionActor {
  sealed trait Command

  private[session] final case class ServerLoaded()

  private[session] final case class NewPlayerLoaded(tplayer: Player)

  private[session] final case class PlayerLoaded(tplayer: Player)

  private[session] final case class PlayerFailedToLoad(tplayer: Player)

  private[session] final case class SetCurrentAvatar(tplayer: Player, max_attempts: Int, attempt: Int = 0)

  final case class SendResponse(packet: PlanetSidePacket) extends Command

  final case class SetSpeed(speed: Float) extends Command

  final case class SetFlying(flying: Boolean) extends Command

  final case class SetSpectator(spectator: Boolean) extends Command

  final case class SetZone(zoneId: String, position: Vector3) extends Command

  final case class SetPosition(position: Vector3) extends Command

  final case class SetConnectionState(connectionState: Int) extends Command

  final case class SetSilenced(silenced: Boolean) extends Command

  final case class SetAvatar(avatar: Avatar) extends Command

  final case class Recall() extends Command

  final case class InstantAction() extends Command

  final case class Quit() extends Command

  final case class Suicide() extends Command

  final case class Kick(player: Player, time: Option[Long] = None) extends Command

  final case class UseCooldownRenewed(definition: BasicDefinition, time: LocalDateTime) extends Command

  final case class UpdateIgnoredPlayers(msg: FriendsResponse) extends Command

  final case class AvatarLoadingSync(step: Int) extends Command

  final case object CharSaved extends Command

  private[session] case object CharSavedMsg extends Command

  final case object StartHeartbeat extends Command

  private final case object PokeClient extends Command

  final case class SetMode(mode: PlayerMode) extends Command
}

class SessionActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String, sessionId: Long)
  extends Actor
    with MDCContextAware {
  MDC("connectionId") = connectionId

  private var clientKeepAlive: Cancellable = Default.Cancellable
  private[this] val buffer: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]()
  private[this] val data = new SessionData(middlewareActor, context)
  private[this] var mode: PlayerMode = NormalMode
  private[this] var logic: ModeLogic = _

  override def postStop(): Unit = {
    clientKeepAlive.cancel()
    data.stop()
  }

  def receive: Receive = startup

  private def startup: Receive = {
    case msg if !data.assignEventBus(msg) =>
      buffer.addOne(msg)
    case _ if data.whenAllEventBusesLoaded() =>
      context.become(inTheGame)
      logic = mode.setup(data)
      buffer.foreach { self.tell(_, self) } //we forget the original sender, shouldn't be doing callbacks at this point
      buffer.clear()
    case _ => ()
  }

  private def inTheGame: Receive = {
    case SessionActor.StartHeartbeat =>
      //used for the game's heartbeat
      startHeartbeat()

    case SessionActor.PokeClient =>
      pokeClient()

    case SessionActor.SetMode(newMode) =>
      changeMode(newMode)

    case packet =>
      parse(sender())(packet)
  }

  private def startHeartbeat(): Unit = {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    clientKeepAlive.cancel()
    clientKeepAlive = context.system.scheduler.scheduleWithFixedDelay(
      initialDelay = 0.seconds,
      delay = 500.milliseconds,
      context.self,
      SessionActor.PokeClient
    )
  }

  private def pokeClient(): Unit = {
    middlewareActor ! MiddlewareActor.Send(KeepAliveMessage())
  }

  private def changeMode(newMode: PlayerMode): Unit = {
    if (mode != newMode) {
      logic.switchFrom(data.session)
      mode = newMode
      logic = newMode.setup(data)
    }
    logic.switchTo(data.session)
  }

  private def parse(sender: ActorRef): Receive = {
    /* really common messages (very frequently, every life) */
    case packet: PlanetSideGamePacket =>
      handleGamePkt(packet)

    case AvatarServiceResponse(toChannel, guid, reply) =>
      logic.avatarResponse.handle(toChannel, guid, reply)

    case GalaxyServiceResponse(_, reply) =>
      logic.galaxy.handle(reply)

    case LocalServiceResponse(toChannel, guid, reply) =>
      logic.local.handle(toChannel, guid, reply)

    case Mountable.MountMessages(tplayer, reply) =>
      logic.mountResponse.handle(tplayer, reply)

    case SquadServiceResponse(_, excluded, response) =>
      logic.squad.handle(response, excluded)

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      logic.terminals.handle(tplayer, msg, order)

    case VehicleServiceResponse(toChannel, guid, reply) =>
      logic.vehicleResponse.handle(toChannel, guid, reply)

    case ChatService.MessageResponse(fromSession, message, _) =>
      logic.chat.handleIncomingMessage(message, fromSession)

    case SessionActor.SendResponse(packet) =>
      data.sendResponse(packet)

    case SessionActor.CharSaved =>
      logic.general.handleRenewCharSavedTimer()

    case SessionActor.CharSavedMsg =>
      logic.general.handleRenewCharSavedTimerMsg()

    /* common messages (maybe once every respawn) */
    case ICS.SpawnPointResponse(response) =>
      data.zoning.handleSpawnPointResponse(response)

    case SessionActor.NewPlayerLoaded(tplayer) =>
      data.zoning.spawn.handleNewPlayerLoaded(tplayer)

    case SessionActor.PlayerLoaded(tplayer) =>
      data.zoning.spawn.handlePlayerLoaded(tplayer)

    case Zone.Population.PlayerHasLeft(zone, playerOpt) =>
      data.zoning.spawn.handlePlayerHasLeft(zone, playerOpt)

    case Zone.Population.PlayerCanNotSpawn(zone, tplayer) =>
      data.zoning.spawn.handlePlayerCanNotSpawn(zone, tplayer)

    case Zone.Population.PlayerAlreadySpawned(zone, tplayer) =>
      data.zoning.spawn.handlePlayerAlreadySpawned(zone, tplayer)

    case Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      data.zoning.spawn.handleCanNotSpawn(zone, vehicle, reason)

    case Zone.Vehicle.CanNotDespawn(zone, vehicle, reason) =>
      data.zoning.spawn.handleCanNotDespawn(zone, vehicle, reason)

    case ICS.ZoneResponse(Some(zone)) =>
      data.zoning.handleZoneResponse(zone)

    /* uncommon messages (once a session) */
    case ICS.ZonesResponse(zones) =>
      data.zoning.handleZonesResponse(zones)

    case SessionActor.SetAvatar(avatar) =>
      logic.general.handleSetAvatar(avatar)

    case PlayerToken.LoginInfo(name, Zone.Nowhere, _) =>
      data.zoning.spawn.handleLoginInfoNowhere(name, sender)

    case PlayerToken.LoginInfo(name, inZone, optionalSavedData) =>
      data.zoning.spawn.handleLoginInfoSomewhere(name, inZone, optionalSavedData, sender)

    case PlayerToken.RestoreInfo(playerName, inZone, pos) =>
      data.zoning.spawn.handleLoginInfoRestore(playerName, inZone, pos, sender)

    case PlayerToken.CanNotLogin(playerName, reason) =>
      data.zoning.spawn.handleLoginCanNot(playerName, reason)

    case ReceiveAccountData(account) =>
      logic.general.handleReceiveAccountData(account)

    case AvatarActor.AvatarResponse(avatar) =>
      logic.general.handleAvatarResponse(avatar)

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
      data.general.handleProgressChange(delta, finishedAction, stepAction, tick)

    case CommonMessages.Progress(rate, finishedAction, stepAction) =>
      data.general.setupProgressChange(rate, finishedAction, stepAction)

    case CavernRotationService.CavernRotationServiceKey.Listing(listings) =>
      listings.head ! SendCavernRotationUpdates(data.context.self)

    case LookupResult("propertyOverrideManager", endpoint) =>
      data.zoning.propertyOverrideManagerLoadOverrides(endpoint)

    case SessionActor.UpdateIgnoredPlayers(msg) =>
      logic.galaxy.handleUpdateIgnoredPlayers(msg)

    case SessionActor.UseCooldownRenewed(definition, _) =>
      logic.general.handleUseCooldownRenew(definition)

    case Deployment.CanDeploy(obj, state) =>
      logic.vehicles.handleCanDeploy(obj, state)

    case Deployment.CanUndeploy(obj, state) =>
      logic.vehicles.handleCanUndeploy(obj, state)

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      logic.vehicles.handleCanNotChangeDeployment(obj, state, reason)

    /* rare messages */
    case ProximityUnit.StopAction(term, _) =>
      logic.terminals.ops.LocalStopUsingProximityUnit(term)

    case SessionActor.Suicide() =>
      data.general.suicide(data.player)

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
      data.zoning.spawn.handlePlayerFailedToLoad(tplayer)

    /* csr only */
    case SessionActor.SetSpeed(speed) =>
      logic.general.handleSetSpeed(speed)

    case SessionActor.SetFlying(isFlying) =>
      logic.general.handleSetFlying(isFlying)

    case SessionActor.SetSpectator(isSpectator) =>
      logic.general.handleSetSpectator(isSpectator)

    case SessionActor.Kick(player, time) =>
      logic.general.handleKick(player, time)

    case SessionActor.SetZone(zoneId, position) =>
      data.zoning.handleSetZone(zoneId, position)

    case SessionActor.SetPosition(position) =>
      data.zoning.spawn.handleSetPosition(position)

    case SessionActor.SetSilenced(silenced) =>
      logic.general.handleSilenced(silenced)

    /* catch these messages */
    case _: ProximityUnit.Action => ()

    case _: Zone.Vehicle.HasSpawned => ()

    case _: Zone.Vehicle.HasDespawned => ()

    case Zone.Deployable.IsDismissed(obj: TurretDeployable) => //only if target deployable was never fully introduced
      logic.local.handleTurretDeployableIsDismissed(obj)

    case Zone.Deployable.IsDismissed(obj) => //only if target deployable was never fully introduced
      logic.local.handleDeployableIsDismissed(obj)

    case msg: Containable.ItemPutInSlot =>
      logic.general.handleItemPutInSlot(msg)

    case msg: Containable.CanNotPutItemInSlot =>
      logic.general.handleCanNotPutItemInSlot(msg)

    case default =>
      logic.general.handleReceiveDefaultMessage(default, sender)
  }

  private def handleGamePkt: PlanetSideGamePacket => Unit = {
    case packet: ConnectToWorldRequestMessage =>
      logic.general.handleConnectToWorldRequest(packet)

    case packet: MountVehicleCargoMsg =>
      logic.mountResponse.handleMountVehicleCargo(packet)

    case packet: DismountVehicleCargoMsg =>
      logic.mountResponse.handleDismountVehicleCargo(packet)

    case packet: CharacterCreateRequestMessage =>
      logic.general.handleCharacterCreateRequest(packet)

    case packet: CharacterRequestMessage =>
      logic.general.handleCharacterRequest(packet)

    case _: KeepAliveMessage =>
      data.keepAliveFunc()

    case packet: BeginZoningMessage =>
      data.zoning.handleBeginZoning(packet)

    case packet: PlayerStateMessageUpstream =>
      logic.general.handlePlayerStateUpstream(packet)

    case packet: ChildObjectStateMessage =>
      logic.vehicles.handleChildObjectState(packet)

    case packet: VehicleStateMessage =>
      logic.vehicles.handleVehicleState(packet)

    case packet: VehicleSubStateMessage =>
      logic.vehicles.handleVehicleSubState(packet)

    case packet: FrameVehicleStateMessage =>
      logic.vehicles.handleFrameVehicleState(packet)

    case packet: ProjectileStateMessage =>
      logic.shooting.handleProjectileState(packet)

    case packet: LongRangeProjectileInfoMessage =>
      logic.shooting.handleLongRangeProjectileState(packet)

    case packet: ReleaseAvatarRequestMessage =>
      data.zoning.spawn.handleReleaseAvatarRequest(packet)

    case packet: SpawnRequestMessage =>
      data.zoning.spawn.handleSpawnRequest(packet)

    case packet: ChatMsg =>
      logic.chat.handleChatMsg(packet)

    case packet: SetChatFilterMessage =>
      logic.chat.handleChatFilter(packet)

    case packet: VoiceHostRequest =>
      logic.general.handleVoiceHostRequest(packet)

    case packet: VoiceHostInfo =>
      logic.general.handleVoiceHostInfo(packet)

    case packet: ChangeAmmoMessage =>
      logic.shooting.handleChangeAmmo(packet)

    case packet: ChangeFireModeMessage =>
      logic.shooting.handleChangeFireMode(packet)

    case packet: ChangeFireStateMessage_Start =>
      logic.shooting.handleChangeFireStateStart(packet)

    case packet: ChangeFireStateMessage_Stop =>
      logic.shooting.handleChangeFireStateStop(packet)

    case packet: EmoteMsg =>
      logic.general.handleEmote(packet)

    case packet: DropItemMessage =>
      logic.general.handleDropItem(packet)

    case packet: PickupItemMessage =>
      logic.general.handlePickupItem(packet)

    case packet: ReloadMessage =>
      logic.shooting.handleReload(packet)

    case packet: ObjectHeldMessage =>
      logic.general.handleObjectHeld(packet)

    case packet: AvatarJumpMessage =>
      logic.general.handleAvatarJump(packet)

    case packet: ZipLineMessage =>
      logic.general.handleZipLine(packet)

    case packet: RequestDestroyMessage =>
      logic.general.handleRequestDestroy(packet)

    case packet: MoveItemMessage =>
      logic.general.handleMoveItem(packet)

    case packet: LootItemMessage =>
      logic.general.handleLootItem(packet)

    case packet: AvatarImplantMessage =>
      logic.general.handleAvatarImplant(packet)

    case packet: UseItemMessage =>
      logic.general.handleUseItem(packet)

    case packet: UnuseItemMessage =>
      logic.general.handleUnuseItem(packet)

    case packet: ProximityTerminalUseMessage =>
      logic.terminals.handleProximityTerminalUse(packet)

    case packet: DeployObjectMessage =>
      logic.general.handleDeployObject(packet)

    case packet: GenericObjectActionMessage =>
      logic.general.handleGenericObjectAction(packet)

    case packet: GenericObjectActionAtPositionMessage =>
      logic.general.handleGenericObjectActionAtPosition(packet)

    case packet: GenericObjectStateMsg =>
      logic.general.handleGenericObjectState(packet)

    case packet: GenericActionMessage =>
      logic.general.handleGenericAction(packet)

    case packet: ItemTransactionMessage =>
      logic.terminals.handleItemTransaction(packet)

    case packet: FavoritesRequest =>
      logic.terminals.handleFavoritesRequest(packet)

    case packet: WeaponDelayFireMessage =>
      logic.shooting.handleWeaponDelayFire(packet)

    case packet: WeaponDryFireMessage =>
      logic.shooting.handleWeaponDryFire(packet)

    case packet: WeaponFireMessage =>
      logic.shooting.handleWeaponFire(packet)

    case packet: WeaponLazeTargetPositionMessage =>
      logic.shooting.handleWeaponLazeTargetPosition(packet)

    case packet: UplinkRequest =>
      logic.shooting.handleUplinkRequest(packet)

    case packet: HitMessage =>
      logic.shooting.handleDirectHit(packet)

    case packet: SplashHitMessage =>
      logic.shooting.handleSplashHit(packet)

    case packet: LashMessage =>
      logic.shooting.handleLashHit(packet)

    case packet: AIDamage =>
      logic.shooting.handleAIDamage(packet)

    case packet: AvatarFirstTimeEventMessage =>
      logic.general.handleAvatarFirstTimeEvent(packet)

    case packet: WarpgateRequest =>
      data.zoning.handleWarpgateRequest(packet)

    case packet: MountVehicleMsg =>
      logic.mountResponse.handleMountVehicle(packet)

    case packet: DismountVehicleMsg =>
      logic.mountResponse.handleDismountVehicle(packet)

    case packet: DeployRequestMessage =>
      logic.vehicles.handleDeployRequest(packet)

    case packet: AvatarGrenadeStateMessage =>
      logic.shooting.handleAvatarGrenadeState(packet)

    case packet: SquadDefinitionActionMessage =>
      logic.squad.handleSquadDefinitionAction(packet)

    case packet: SquadMembershipRequest =>
      logic.squad.handleSquadMemberRequest(packet)

    case packet: SquadWaypointRequest =>
      logic.squad.handleSquadWaypointRequest(packet)

    case packet: GenericCollisionMsg =>
      logic.general.handleGenericCollision(packet)

    case packet: BugReportMessage =>
      logic.general.handleBugReport(packet)

    case packet: BindPlayerMessage =>
      logic.general.handleBindPlayer(packet)

    case packet: PlanetsideAttributeMessage =>
      logic.general.handlePlanetsideAttribute(packet)

    case packet: FacilityBenefitShieldChargeRequestMessage =>
      logic.general.handleFacilityBenefitShieldChargeRequest(packet)

    case packet: BattleplanMessage =>
      logic.general.handleBattleplan(packet)

    case packet: CreateShortcutMessage =>
      logic.general.handleCreateShortcut(packet)

    case packet: ChangeShortcutBankMessage =>
      logic.general.handleChangeShortcutBank(packet)

    case packet: FriendsRequest =>
      logic.general.handleFriendRequest(packet)

    case packet: DroppodLaunchRequestMessage =>
      data.zoning.handleDroppodLaunchRequest(packet)

    case packet: InvalidTerrainMessage =>
      logic.general.handleInvalidTerrain(packet)

    case packet: ActionCancelMessage =>
      logic.general.handleActionCancel(packet)

    case packet: TradeMessage =>
      logic.general.handleTrade(packet)

    case packet: DisplayedAwardMessage =>
      logic.general.handleDisplayedAward(packet)

    case packet: ObjectDetectedMessage =>
      logic.general.handleObjectDetected(packet)

    case packet: TargetingImplantRequest =>
      logic.general.handleTargetingImplantRequest(packet)

    case packet: HitHint =>
      logic.general.handleHitHint(packet)

    case _: OutfitRequest => ()

    case pkt =>
      data.log.warn(s"Unhandled GamePacket $pkt")
  }
}
