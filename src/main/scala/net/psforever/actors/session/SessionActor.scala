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
    case msg =>
      if (!sessionFuncs.assignEventBus(msg)) {
        buffer.addOne(msg)
      } else if (sessionFuncs.whenAllEventBusesLoaded()) {
        context.become(inTheGame)
        buffer.foreach { self.tell(_, self) }
        buffer.clear()
      }
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
      sessionFuncs.squadResponseHandlers.handle(response, excluded)

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

    /* common messages (once every respawn) */
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
      sessionFuncs.SetupProgressChange(rate, finishedAction, stepAction)

    case SessionActor.ProgressEvent(delta, finishedAction, stepAction, tick) =>
      sessionFuncs.HandleProgressChange(delta, finishedAction, stepAction, tick)

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
    case ProximityUnit.StopAction(term, target) =>
      sessionFuncs.terminals.LocalStopUsingProximityUnit(term, target)

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

  def handleGamePkt(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case _: ConnectToWorldRequestMessage =>
        sessionFuncs.handleConnectToWorldRequest(pkt)

      case _: MountVehicleCargoMsg =>
        sessionFuncs.vehicles.handleDismountVehicleCargo(pkt)

      case _: DismountVehicleCargoMsg =>
        sessionFuncs.vehicles.handleDismountVehicleCargo(pkt)

      case _: CharacterCreateRequestMessage =>
       sessionFuncs.handleCharacterCreateRequest(pkt)

      case _: CharacterRequestMessage =>
        sessionFuncs.handleCharacterRequest(pkt)

      case _: KeepAliveMessage =>
        sessionFuncs.keepAliveFunc()

      case _: BeginZoningMessage =>
        sessionFuncs.zoning.handleBeginZoning(pkt)

      case _: PlayerStateMessageUpstream =>
        sessionFuncs.handlePlayerStateUpstream(pkt)

      case _: ChildObjectStateMessage =>
        sessionFuncs.vehicles.handleChildObjectState(pkt)

      case _: VehicleStateMessage =>
        sessionFuncs.vehicles.handleVehicleState(pkt)

      case _: VehicleSubStateMessage =>
        sessionFuncs.vehicles.handleVehicleSubState(pkt)

      case _: FrameVehicleStateMessage =>
        sessionFuncs.vehicles.handleFrameVehicleState(pkt)

      case _: ProjectileStateMessage =>
        sessionFuncs.shooting.handleProjectileState(pkt)

      case _: LongRangeProjectileInfoMessage =>
        sessionFuncs.shooting.handleLongRangeProjectileState(pkt)

      case _: ReleaseAvatarRequestMessage =>
        sessionFuncs.zoning.spawn.handleReleaseAvatarRequest(pkt)

      case _: SpawnRequestMessage =>
        sessionFuncs.zoning.spawn.handleSpawnRequest(pkt)

      case _: ChatMsg =>
        sessionFuncs.handleChat(pkt)

      case _: SetChatFilterMessage =>
        sessionFuncs.handleChatFilter(pkt)

      case _: VoiceHostRequest =>
        sessionFuncs.handleVoiceHostRequest(pkt)

      case _: VoiceHostInfo =>
        sessionFuncs.handleVoiceHostInfo(pkt)

      case _: ChangeAmmoMessage =>
        sessionFuncs.shooting.handleChangeAmmo(pkt)

      case _: ChangeFireModeMessage =>
        sessionFuncs.shooting.handleChangeFireMode(pkt)

      case _: ChangeFireStateMessage_Start =>
        sessionFuncs.shooting.handleChangeFireStateStart(pkt)

      case _: ChangeFireStateMessage_Stop =>
        sessionFuncs.shooting.handleChangeFireStateStop(pkt)

      case _: EmoteMsg =>
        sessionFuncs.handleEmote(pkt)

      case _: DropItemMessage =>
        sessionFuncs.handleDropItem(pkt)

      case _: PickupItemMessage =>
        sessionFuncs.handlePickupItem(pkt)

      case _: ReloadMessage =>
        sessionFuncs.shooting.handleReload(pkt)

      case _: ObjectHeldMessage =>
        sessionFuncs.handleObjectHeld(pkt)

      case _: AvatarJumpMessage =>
        sessionFuncs.handleAvatarJump(pkt)

      case _: ZipLineMessage =>
        sessionFuncs.handleZipLine(pkt)

      case _: RequestDestroyMessage =>
        sessionFuncs.handleRequestDestroy(pkt)

      case _: MoveItemMessage =>
        sessionFuncs.handleMoveItem(pkt)

      case _: LootItemMessage =>
        sessionFuncs.handleLootItem(pkt)

      case _: AvatarImplantMessage =>
        sessionFuncs.handleAvatarImplant(pkt)

      case _: UseItemMessage =>
        sessionFuncs.handleUseItem(pkt)

      case _: UnuseItemMessage =>
        sessionFuncs.handleUnuseItem(pkt)

      case _: ProximityTerminalUseMessage =>
        sessionFuncs.terminals.handleProximityTerminalUse(pkt)

      case _: DeployObjectMessage =>
        sessionFuncs.handleDeployObject(pkt)

      case _: GenericObjectActionMessage =>
        sessionFuncs.handleGenericObjectAction(pkt)

      case _: GenericObjectActionAtPositionMessage =>
        sessionFuncs.handleGenericObjectActionAtPosition(pkt)

      case _: GenericObjectStateMsg =>
        sessionFuncs.handleGenericObjectState(pkt)

      case _: GenericActionMessage =>
        sessionFuncs.handleGenericAction(pkt)

      case _: ItemTransactionMessage =>
        sessionFuncs.terminals.handleItemTransaction(pkt)

      case _: FavoritesRequest =>
        sessionFuncs.handleFavoritesRequest(pkt)

      case _: WeaponDelayFireMessage =>
        sessionFuncs.shooting.handleWeaponDelayFire(pkt)

      case _: WeaponDryFireMessage =>
        sessionFuncs.shooting.handleWeaponDryFire(pkt)

      case _: WeaponFireMessage =>
        sessionFuncs.shooting.handleWeaponFire(pkt)

      case _: WeaponLazeTargetPositionMessage =>
        sessionFuncs.shooting.handleWeaponLazeTargetPosition(pkt)

      case _: HitMessage =>
        sessionFuncs.shooting.handleDirectHit(pkt)

      case _: SplashHitMessage =>
        sessionFuncs.shooting.handleSplashHit(pkt)

      case _: LashMessage =>
        sessionFuncs.shooting.handleLashHit(pkt)

      case _: AvatarFirstTimeEventMessage =>
        sessionFuncs.handleAvatarFirstTimeEvent(pkt)

      case _: WarpgateRequest =>
        sessionFuncs.zoning.handleWarpgateRequest(pkt)

      case _: MountVehicleMsg =>
        sessionFuncs.vehicles.handleMountVehicle(pkt)

      case _: DismountVehicleMsg =>
        sessionFuncs.vehicles.handleDismountVehicle(pkt)

      case _: DeployRequestMessage =>
        sessionFuncs.vehicles.handleDeployRequest(pkt)

      case _: AvatarGrenadeStateMessage =>
        sessionFuncs.shooting.handleAvatarGrenadeState(pkt)

      case _: SquadDefinitionActionMessage =>
        sessionFuncs.handleSquadDefinitionAction(pkt)

      case _: SquadMembershipRequest =>
        sessionFuncs.handleSquadMemberRequest(pkt)

      case _: SquadWaypointRequest =>
        sessionFuncs.handleSquadWaypointRequest(pkt)

      case _: GenericCollisionMsg =>
        sessionFuncs.handleGenericCollision(pkt)

      case _: BugReportMessage =>
        sessionFuncs.handleBugReport(pkt)

      case _: BindPlayerMessage =>
        sessionFuncs.handleBindPlayer(pkt)

      case _: PlanetsideAttributeMessage =>
        sessionFuncs.handlePlanetsideAttribute(pkt)

      case _: FacilityBenefitShieldChargeRequestMessage =>
        sessionFuncs.handleFacilityBenefitShieldChargeRequest(pkt)

      case _: BattleplanMessage =>
        sessionFuncs.handleBattleplan(pkt)

      case msg: CreateShortcutMessage =>
        if (msg.shortcut.nonEmpty) {
          sessionFuncs.handleAddShortcut(pkt)
        } else {
          sessionFuncs.handleRemoveShortcut(pkt)
        }

      case _: ChangeShortcutBankMessage =>
        sessionFuncs.handleChangeShortcutBank(pkt)

      case _: FriendsRequest =>
        sessionFuncs.handleFriendRequest(pkt)

      case _: DroppodLaunchRequestMessage =>
        sessionFuncs.zoning.handleDroppodLaunchRequest(pkt)

      case _: InvalidTerrainMessage =>
        sessionFuncs.handleInvalidTerrain(pkt)

      case _: ActionCancelMessage =>
        sessionFuncs.handleActionCancel(pkt)

      case _: TradeMessage =>
        sessionFuncs.handleTrade(pkt)

      case _: DisplayedAwardMessage =>
        sessionFuncs.handleDisplayedAward(pkt)

      case _: ObjectDetectedMessage =>
        sessionFuncs.handleObjectDetected(pkt)

      case _: TargetingImplantRequest =>
        sessionFuncs.handleTargetingImplantRequest(pkt)

      case _: HitHint =>
        sessionFuncs.handleHitHint(pkt)

      case _ =>
        log.warn(s"Unhandled GamePacket $pkt")
    }
  }
}
