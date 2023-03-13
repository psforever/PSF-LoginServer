// Copyright (c) 2016, 2020 PSForever
package net.psforever.actors.session

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, MDCContextAware, SupervisorStrategy, typed}
import org.joda.time.LocalDateTime
import org.log4s.MDC
import scala.collection.mutable
//
import net.psforever.actors.net.MiddlewareActor
import net.psforever.actors.session.support.SessionData
import net.psforever.objects._
import net.psforever.objects.avatar._
import net.psforever.objects.definition._
import net.psforever.objects.guid._
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.zones._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.services.CavernRotationService.SendCavernRotationUpdates
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.account.{PlayerToken, ReceiveAccountData}
import net.psforever.services.avatar.AvatarServiceResponse
import net.psforever.services.galaxy.GalaxyServiceResponse
import net.psforever.services.local.LocalServiceResponse
import net.psforever.services.teamwork.SquadServiceResponse
import net.psforever.services.vehicle.VehicleServiceResponse
import net.psforever.services.{CavernRotationService, ServiceManager, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.Config

object SessionActor {
  sealed trait Command

  private[session] final case class PokeClient()

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

  final case object CharSaved extends Command

  private[session] case object CharSavedMsg extends Command

  /**
    * The message that progresses some form of user-driven activity with a certain eventual outcome
    * and potential feedback per cycle.
    * @param delta how much the progress value changes each tick, which will be treated as a percentage;
    *              must be a positive value
    * @param completionAction a finalizing action performed once the progress reaches 100(%)
    * @param tickAction an action that is performed for each increase of progress
    * @param tickTime how long between each `tickAction` (ms);
    *                 defaults to 250 milliseconds
    */
  private[session] final case class ProgressEvent(
      delta: Float,
      completionAction: () => Unit,
      tickAction: Float => Boolean,
      tickTime: Long = 250L
  )

  private[session] final case class AvatarAwardMessageBundle(bundle: Iterable[Iterable[PlanetSideGamePacket]], delay: Long)
}

class SessionActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String, sessionId: Long)
    extends Actor
    with MDCContextAware {
  MDC("connectionId") = connectionId

  private[this] val log = org.log4s.getLogger
  private[this] val buffer: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]()
  private[this] val sessionFuncs = new SessionData(middlewareActor, context)

  override val supervisorStrategy: SupervisorStrategy = sessionFuncs.sessionSupervisorStrategy

  ServiceManager.serviceManager ! Lookup("accountIntermediary")
  ServiceManager.serviceManager ! Lookup("accountPersistence")
  ServiceManager.serviceManager ! Lookup("galaxy")
  ServiceManager.serviceManager ! Lookup("squad")
  ServiceManager.receptionist ! Receptionist.Find(ICS.InterstellarClusterServiceKey, context.self)

  override def postStop(): Unit = {
    //normally, the player avatar persists a minute or so after disconnect; we are subject to the SessionReaper
    //TODO put any temporary values back into the avatar
    sessionFuncs.stop()
  }

  def receive: Receive = startup

  def startup: Receive = {
    case msg if !sessionFuncs.assignEventBus(msg) =>
      buffer.addOne(msg)
    case _ if sessionFuncs.whenAllEventBusesLoaded() =>
      context.become(inTheGame)
      buffer.foreach { self.tell(_, self) } //we forget the original sender, shouldn't be doing callbacks at this point
      buffer.clear()
    case _ => ()
  }

  def inTheGame: Receive = {
    /* really common messages (very frequently, every life) */
    case packet: PlanetSideGamePacket =>
      handleGamePkt(packet)

    case AvatarServiceResponse(toChannel, guid, reply) =>
      sessionFuncs.avatarResponse.handle(toChannel, guid, reply)

    case GalaxyServiceResponse(_, reply) =>
      sessionFuncs.galaxyResponseHanders.handle(reply)

    case LocalServiceResponse(toChannel, guid, reply) =>
      sessionFuncs.localResponse.handle(toChannel, guid, reply)

    case Mountable.MountMessages(tplayer, reply) =>
      sessionFuncs.mountResponse.handle(tplayer, reply)

    case SquadServiceResponse(_, excluded, response) =>
      sessionFuncs.squad.handle(response, excluded)

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      sessionFuncs.terminals.handle(tplayer, msg, order)

    case VehicleServiceResponse(toChannel, guid, reply) =>
      sessionFuncs.vehicleResponseOperations.handle(toChannel, guid, reply)

    case SessionActor.PokeClient() =>
      sessionFuncs.sendResponse(KeepAliveMessage())

    case SessionActor.SendResponse(packet) =>
      sessionFuncs.sendResponse(packet)

    case SessionActor.CharSaved =>
      sessionFuncs.renewCharSavedTimer(
        Config.app.game.savedMsg.interruptedByAction.fixed,
        Config.app.game.savedMsg.interruptedByAction.variable
      )

    case SessionActor.CharSavedMsg =>
      sessionFuncs.displayCharSavedMsgThenRenewTimer(
        Config.app.game.savedMsg.renewal.fixed,
        Config.app.game.savedMsg.renewal.variable
      )

    /* common messages (maybe once every respawn) */
    case ICS.SpawnPointResponse(response) =>
      sessionFuncs.zoning.handleSpawnPointResponse(response)

    case SessionActor.NewPlayerLoaded(tplayer) =>
      sessionFuncs.zoning.spawn.handleNewPlayerLoaded(tplayer)

    case SessionActor.PlayerLoaded(tplayer) =>
      sessionFuncs.zoning.spawn.handlePlayerLoaded(tplayer)

    case Zone.Population.PlayerHasLeft(zone, None) =>
      log.trace(s"PlayerHasLeft: ${sessionFuncs.player.Name} does not have a body on ${zone.id}")

    case Zone.Population.PlayerHasLeft(zone, Some(tplayer)) =>
      if (tplayer.isAlive) {
        log.info(s"${tplayer.Name} has left zone ${zone.id}")
      }

    case Zone.Population.PlayerCanNotSpawn(zone, tplayer) =>
      log.warn(s"${tplayer.Name} can not spawn in zone ${zone.id}; why?")

    case Zone.Population.PlayerAlreadySpawned(zone, tplayer) =>
      log.warn(s"${tplayer.Name} is already spawned on zone ${zone.id}; is this a clerical error?")

    case Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      log.warn(s"${sessionFuncs.player.Name}'s ${vehicle.Definition.Name} can not spawn in ${zone.id} because $reason")

    case Zone.Vehicle.CanNotDespawn(zone, vehicle, reason) =>
      log.warn(s"${sessionFuncs.player.Name}'s ${vehicle.Definition.Name} can not deconstruct in ${zone.id} because $reason")

    case ICS.ZoneResponse(Some(zone)) =>
      sessionFuncs.zoning.handleZoneResponse(zone)

    /* uncommon messages (once a session) */
    case ICS.ZonesResponse(zones) =>
      sessionFuncs.zoning.handleZonesResponse(zones)

    case SessionActor.SetAvatar(avatar) =>
      sessionFuncs.handleSetAvatar(avatar)

    case PlayerToken.LoginInfo(name, Zone.Nowhere, _) =>
      sessionFuncs.zoning.spawn.handleLoginInfoNowhere(name, sender())

    case PlayerToken.LoginInfo(name, inZone, optionalSavedData) =>
      sessionFuncs.zoning.spawn.handleLoginInfoSomewhere(name, inZone, optionalSavedData, sender())

    case PlayerToken.RestoreInfo(playerName, inZone, pos) =>
      sessionFuncs.zoning.spawn.handleLoginInfoRestore(playerName, inZone, pos, sender())

    case PlayerToken.CanNotLogin(playerName, reason) =>
      sessionFuncs.zoning.spawn.handleLoginCanNot(playerName, reason)

    case ReceiveAccountData(account) =>
      sessionFuncs.handleReceiveAccountData(account)

    case AvatarActor.AvatarResponse(avatar) =>
      sessionFuncs.handleAvatarResponse(avatar)

    case AvatarActor.AvatarLoginResponse(avatar) =>
      sessionFuncs.zoning.spawn.avatarLoginResponse(avatar)

    case SessionActor.SetCurrentAvatar(tplayer, max_attempts, attempt) =>
      sessionFuncs.zoning.spawn.ReadyToSetCurrentAvatar(tplayer, max_attempts, attempt)

    case SessionActor.SetConnectionState(state) =>
      sessionFuncs.connectionState = state

    /* uncommon messages (utility, or once in a while) */
    case SessionActor.AvatarAwardMessageBundle(pkts, delay) =>
      sessionFuncs.zoning.spawn.performAvatarAwardMessageDelivery(pkts, delay)

    case CommonMessages.Progress(rate, finishedAction, stepAction) =>
      sessionFuncs.setupProgressChange(rate, finishedAction, stepAction)

    case SessionActor.ProgressEvent(delta, finishedAction, stepAction, tick) =>
      sessionFuncs.handleProgressChange(delta, finishedAction, stepAction, tick)

    case CavernRotationService.CavernRotationServiceKey.Listing(listings) =>
      listings.head ! SendCavernRotationUpdates(context.self)

    case LookupResult("propertyOverrideManager", endpoint) =>
      sessionFuncs.zoning.propertyOverrideManagerLoadOverrides(endpoint)

    case SessionActor.UpdateIgnoredPlayers(msg) =>
      sessionFuncs.handleUpdateIgnoredPlayers(msg)

    case SessionActor.UseCooldownRenewed(definition, _) =>
      sessionFuncs.handleUseCooldownRenew(definition)

    case Deployment.CanDeploy(obj, state) =>
      sessionFuncs.vehicles.handleCanDeploy(obj, state)

    case Deployment.CanUndeploy(obj, state) =>
      sessionFuncs.vehicles.handleCanUndeploy(obj, state)

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      sessionFuncs.vehicles.handleCanNotChangeDeployment(obj, state, reason)

    /* rare messages */
    case ProximityUnit.StopAction(term, _) =>
      sessionFuncs.terminals.LocalStopUsingProximityUnit(term)

    case SessionActor.Suicide() =>
      sessionFuncs.suicide(sessionFuncs.player)

    case SessionActor.Recall() =>
      sessionFuncs.zoning.handleRecall()

    case SessionActor.InstantAction() =>
      sessionFuncs.zoning.handleInstantAction()

    case SessionActor.Quit() =>
      sessionFuncs.zoning.handleQuit()

    case ICS.DroppodLaunchDenial(errorCode, _) =>
      sessionFuncs.zoning.handleDroppodLaunchDenial(errorCode)

    case ICS.DroppodLaunchConfirmation(zone, position) =>
      sessionFuncs.zoning.LoadZoneLaunchDroppod(zone, position)

    case SessionActor.PlayerFailedToLoad(tplayer) =>
      sessionFuncs.failWithError(s"${tplayer.Name} failed to load anywhere")

    /* csr only */
    case SessionActor.SetSpeed(speed) =>
      sessionFuncs.handleSetSpeed(speed)

    case SessionActor.SetFlying(isFlying) =>
      sessionFuncs.handleSetFlying(isFlying)

    case SessionActor.SetSpectator(isSpectator) =>
      sessionFuncs.handleSetSpectator(isSpectator)

    case SessionActor.Kick(player, time) =>
      sessionFuncs.handleKick(player, time)

    case SessionActor.SetZone(zoneId, position) =>
      sessionFuncs.zoning.handleSetZone(zoneId, position)

    case SessionActor.SetPosition(position) =>
      sessionFuncs.zoning.spawn.handleSetPosition(position)

    case SessionActor.SetSilenced(silenced) =>
      sessionFuncs.handleSilenced(silenced)

    /* catch these messages */
    case _: ProximityUnit.Action => ;

    case _: Zone.Vehicle.HasSpawned => ;

    case _: Zone.Vehicle.HasDespawned => ;

    case Zone.Deployable.IsDismissed(obj: TurretDeployable) => //only if target deployable was never fully introduced
      TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(sessionFuncs.continent.GUID, obj))

    case Zone.Deployable.IsDismissed(obj) => //only if target deployable was never fully introduced
      TaskWorkflow.execute(GUIDTask.unregisterObject(sessionFuncs.continent.GUID, obj))

    case msg: Containable.ItemPutInSlot =>
      log.debug(s"ItemPutInSlot: $msg")

    case msg: Containable.CanNotPutItemInSlot =>
      log.debug(s"CanNotPutItemInSlot: $msg")

    case default =>
      log.warn(s"Invalid packet class received: $default from ${sender()}")
  }

  private def handleGamePkt: PlanetSideGamePacket => Unit = {
    case packet: ConnectToWorldRequestMessage =>
      sessionFuncs.handleConnectToWorldRequest(packet)

    case packet: MountVehicleCargoMsg =>
      sessionFuncs.vehicles.handleMountVehicleCargo(packet)

    case packet: DismountVehicleCargoMsg =>
      sessionFuncs.vehicles.handleDismountVehicleCargo(packet)

    case packet: CharacterCreateRequestMessage =>
     sessionFuncs.handleCharacterCreateRequest(packet)

    case packet: CharacterRequestMessage =>
      sessionFuncs.handleCharacterRequest(packet)

    case _: KeepAliveMessage =>
      sessionFuncs.keepAliveFunc()

    case packet: BeginZoningMessage =>
      sessionFuncs.zoning.handleBeginZoning(packet)

    case packet: PlayerStateMessageUpstream =>
      sessionFuncs.handlePlayerStateUpstream(packet)

    case packet: ChildObjectStateMessage =>
      sessionFuncs.vehicles.handleChildObjectState(packet)

    case packet: VehicleStateMessage =>
      sessionFuncs.vehicles.handleVehicleState(packet)

    case packet: VehicleSubStateMessage =>
      sessionFuncs.vehicles.handleVehicleSubState(packet)

    case packet: FrameVehicleStateMessage =>
      sessionFuncs.vehicles.handleFrameVehicleState(packet)

    case packet: ProjectileStateMessage =>
      sessionFuncs.shooting.handleProjectileState(packet)

    case packet: LongRangeProjectileInfoMessage =>
      sessionFuncs.shooting.handleLongRangeProjectileState(packet)

    case packet: ReleaseAvatarRequestMessage =>
      sessionFuncs.zoning.spawn.handleReleaseAvatarRequest(packet)

    case packet: SpawnRequestMessage =>
      sessionFuncs.zoning.spawn.handleSpawnRequest(packet)

    case packet: ChatMsg =>
      sessionFuncs.handleChat(packet)

    case packet: SetChatFilterMessage =>
      sessionFuncs.handleChatFilter(packet)

    case packet: VoiceHostRequest =>
      sessionFuncs.handleVoiceHostRequest(packet)

    case packet: VoiceHostInfo =>
      sessionFuncs.handleVoiceHostInfo(packet)

    case packet: ChangeAmmoMessage =>
      sessionFuncs.shooting.handleChangeAmmo(packet)

    case packet: ChangeFireModeMessage =>
      sessionFuncs.shooting.handleChangeFireMode(packet)

    case packet: ChangeFireStateMessage_Start =>
      sessionFuncs.shooting.handleChangeFireStateStart(packet)

    case packet: ChangeFireStateMessage_Stop =>
      sessionFuncs.shooting.handleChangeFireStateStop(packet)

    case packet: EmoteMsg =>
      sessionFuncs.handleEmote(packet)

    case packet: DropItemMessage =>
      sessionFuncs.handleDropItem(packet)

    case packet: PickupItemMessage =>
      sessionFuncs.handlePickupItem(packet)

    case packet: ReloadMessage =>
      sessionFuncs.shooting.handleReload(packet)

    case packet: ObjectHeldMessage =>
      sessionFuncs.handleObjectHeld(packet)

    case packet: AvatarJumpMessage =>
      sessionFuncs.handleAvatarJump(packet)

    case packet: ZipLineMessage =>
      sessionFuncs.handleZipLine(packet)

    case packet: RequestDestroyMessage =>
      sessionFuncs.handleRequestDestroy(packet)

    case packet: MoveItemMessage =>
      sessionFuncs.handleMoveItem(packet)

    case packet: LootItemMessage =>
      sessionFuncs.handleLootItem(packet)

    case packet: AvatarImplantMessage =>
      sessionFuncs.handleAvatarImplant(packet)

    case packet: UseItemMessage =>
      sessionFuncs.handleUseItem(packet)

    case packet: UnuseItemMessage =>
      sessionFuncs.handleUnuseItem(packet)

    case packet: ProximityTerminalUseMessage =>
      sessionFuncs.terminals.handleProximityTerminalUse(packet)

    case packet: DeployObjectMessage =>
      sessionFuncs.handleDeployObject(packet)

    case packet: GenericObjectActionMessage =>
      sessionFuncs.handleGenericObjectAction(packet)

    case packet: GenericObjectActionAtPositionMessage =>
      sessionFuncs.handleGenericObjectActionAtPosition(packet)

    case packet: GenericObjectStateMsg =>
      sessionFuncs.handleGenericObjectState(packet)

    case packet: GenericActionMessage =>
      sessionFuncs.handleGenericAction(packet)

    case packet: ItemTransactionMessage =>
      sessionFuncs.terminals.handleItemTransaction(packet)

    case packet: FavoritesRequest =>
      sessionFuncs.handleFavoritesRequest(packet)

    case packet: WeaponDelayFireMessage =>
      sessionFuncs.shooting.handleWeaponDelayFire(packet)

    case packet: WeaponDryFireMessage =>
      sessionFuncs.shooting.handleWeaponDryFire(packet)

    case packet: WeaponFireMessage =>
      sessionFuncs.shooting.handleWeaponFire(packet)

    case packet: WeaponLazeTargetPositionMessage =>
      sessionFuncs.shooting.handleWeaponLazeTargetPosition(packet)

    case packet: HitMessage =>
      sessionFuncs.shooting.handleDirectHit(packet)

    case packet: SplashHitMessage =>
      sessionFuncs.shooting.handleSplashHit(packet)

    case packet: LashMessage =>
      sessionFuncs.shooting.handleLashHit(packet)

    case packet: AvatarFirstTimeEventMessage =>
      sessionFuncs.handleAvatarFirstTimeEvent(packet)

    case packet: WarpgateRequest =>
      sessionFuncs.zoning.handleWarpgateRequest(packet)

    case packet: MountVehicleMsg =>
      sessionFuncs.vehicles.handleMountVehicle(packet)

    case packet: DismountVehicleMsg =>
      sessionFuncs.vehicles.handleDismountVehicle(packet)

    case packet: DeployRequestMessage =>
      sessionFuncs.vehicles.handleDeployRequest(packet)

    case packet: AvatarGrenadeStateMessage =>
      sessionFuncs.shooting.handleAvatarGrenadeState(packet)

    case packet: SquadDefinitionActionMessage =>
      sessionFuncs.squad.handleSquadDefinitionAction(packet)

    case packet: SquadMembershipRequest =>
      sessionFuncs.squad.handleSquadMemberRequest(packet)

    case packet: SquadWaypointRequest =>
      sessionFuncs.squad.handleSquadWaypointRequest(packet)

    case packet: GenericCollisionMsg =>
      sessionFuncs.handleGenericCollision(packet)

    case packet: BugReportMessage =>
      sessionFuncs.handleBugReport(packet)

    case packet: BindPlayerMessage =>
      sessionFuncs.handleBindPlayer(packet)

    case packet: PlanetsideAttributeMessage =>
      sessionFuncs.handlePlanetsideAttribute(packet)

    case packet: FacilityBenefitShieldChargeRequestMessage =>
      sessionFuncs.handleFacilityBenefitShieldChargeRequest(packet)

    case packet: BattleplanMessage =>
      sessionFuncs.handleBattleplan(packet)

    case packet: CreateShortcutMessage =>
      sessionFuncs.handleCreateShortcut(packet)

    case packet: ChangeShortcutBankMessage =>
      sessionFuncs.handleChangeShortcutBank(packet)

    case packet: FriendsRequest =>
      sessionFuncs.handleFriendRequest(packet)

    case packet: DroppodLaunchRequestMessage =>
      sessionFuncs.zoning.handleDroppodLaunchRequest(packet)

    case packet: InvalidTerrainMessage =>
      sessionFuncs.handleInvalidTerrain(packet)

    case packet: ActionCancelMessage =>
      sessionFuncs.handleActionCancel(packet)

    case packet: TradeMessage =>
      sessionFuncs.handleTrade(packet)

    case packet: DisplayedAwardMessage =>
      sessionFuncs.handleDisplayedAward(packet)

    case packet: ObjectDetectedMessage =>
      sessionFuncs.handleObjectDetected(packet)

    case packet: TargetingImplantRequest =>
      sessionFuncs.handleTargetingImplantRequest(packet)

    case packet: HitHint =>
      sessionFuncs.handleHitHint(packet)

    case pkt =>
      log.warn(s"Unhandled GamePacket $pkt")
  }
}
