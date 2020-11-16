package net.psforever.actors.session

import akka.actor.typed
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import net.psforever.actors.net.MiddlewareActor
import net.psforever.services.ServiceManager.Lookup
import net.psforever.objects.locker.LockerContainer
import org.log4s.MDC
import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import net.psforever.login.WorldSession._
import net.psforever.objects._
import net.psforever.objects.avatar.{Avatar, Certification, Cosmetic, DeployableToolbox}
import net.psforever.objects.ballistics._
import net.psforever.objects.ce._
import net.psforever.objects.definition._
import net.psforever.objects.definition.converter.{CorpseConverter, DestroyedVehicleConverter}
import net.psforever.objects.entity.{SimpleWorldEntity, WorldEntity}
import net.psforever.objects.equipment.{ChargeFireModeDefinition, EffectTarget, Equipment, FireModeSwitch, JammableUnit}
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.{FacilityTurret, WeaponTurret}
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.objects.teamwork.Squad
import net.psforever.objects.vehicles._
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vital._
import net.psforever.objects.zones.{Zone, ZoneHotSpotProjector, Zoning}
import net.psforever.packet._
import net.psforever.packet.game.{HotSpotInfo => PacketHotSpotInfo, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.services.ServiceManager.LookupResult
import net.psforever.services.account.{AccountPersistenceService, PlayerToken, ReceiveAccountData, RetrieveAccountData}
import net.psforever.services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage, AvatarServiceResponse}
import net.psforever.services.chat.ChatService
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
import net.psforever.services.local.support.RouterTelepadActivation
import net.psforever.services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import net.psforever.services.properties.PropertyOverrideManager
import net.psforever.services.support.SupportActor
import net.psforever.services.teamwork.{
  SquadResponse,
  SquadServiceMessage,
  SquadServiceResponse,
  SquadAction => SquadServiceAction
}
import net.psforever.services.vehicle.{VehicleAction, VehicleResponse, VehicleServiceMessage, VehicleServiceResponse}
import net.psforever.services.{InterstellarClusterService, RemoverActor, Service, ServiceManager}
import net.psforever.types._
import net.psforever.util.{Config, DefinitionUtil}
import net.psforever.zones.Zones

object SessionActor {
  sealed trait Command

  final case class ResponseToSelf(pkt: PlanetSideGamePacket)

  private final case class PokeClient()
  private final case class ServerLoaded()
  private final case class NewPlayerLoaded(tplayer: Player)
  private final case class PlayerLoaded(tplayer: Player)
  private final case class PlayerFailedToLoad(tplayer: Player)

  private final case class SetCurrentAvatar(tplayer: Player, max_attempts: Int, attempt: Int = 0)

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
  final case class ProgressEvent(
      delta: Float,
      completionAction: () => Unit,
      tickAction: Float => Boolean,
      tickTime: Long = 250
  )

  private final val zoningCountdownMessages: Seq[Int] = Seq(5, 10, 20)

  protected final case class SquadUIElement(
      name: String,
      index: Int,
      zone: Int,
      health: Int,
      armor: Int,
      position: Vector3
  )

  private final case class NtuCharging(tplayer: Player, vehicle: Vehicle)

  private final case class NtuDischarging(tplayer: Player, vehicle: Vehicle, silo_guid: PlanetSideGUID)

  private final case class FinalizeDeployable(
      obj: PlanetSideGameObject with Deployable,
      tool: ConstructionItem,
      index: Int
  )

  private final case class LoadedRemoteProjectile(projectile_guid: PlanetSideGUID, projectile: Option[Projectile])
}

class SessionActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String)
    extends Actor
    with MDCContextAware {

  import SessionActor._

  MDC("connectionId") = connectionId

  private[this] val log                                              = org.log4s.getLogger
  private[this] val damageLog                                        = org.log4s.getLogger(Damageable.LogChannel)
  var avatarActor: typed.ActorRef[AvatarActor.Command]               = context.spawnAnonymous(AvatarActor(context.self))
  var chatActor: typed.ActorRef[ChatActor.Command]                   = context.spawnAnonymous(ChatActor(context.self, avatarActor))
  var accountIntermediary: ActorRef                                  = ActorRef.noSender
  var accountPersistence: ActorRef                                   = ActorRef.noSender
  var galaxyService: ActorRef                                        = ActorRef.noSender
  var squadService: ActorRef                                         = ActorRef.noSender
  var propertyOverrideManager: ActorRef                              = Actor.noSender
  var cluster: typed.ActorRef[InterstellarClusterService.Command]    = Actor.noSender
  var _session: Session                                              = Session()
  var progressBarValue: Option[Float]                                = None
  var shooting: Option[PlanetSideGUID]                               = None //ChangeFireStateMessage_Start
  var prefire: Option[PlanetSideGUID]                                = None //if WeaponFireMessage precedes ChangeFireStateMessage_Start
  var shootingStart: Long                                            = 0
  var shootingStop: Long                                             = 0
  var shotsWhileDead: Int                                            = 0
  var accessedContainer: Option[PlanetSideGameObject with Container] = None
  var connectionState: Int                                           = 25
  var flying: Boolean                                                = false
  var loadConfZone: Boolean                                          = false
  var noSpawnPointHere: Boolean                                      = false
  var usingMedicalTerminal: Option[PlanetSideGUID]                   = None
  var controlled: Option[Int]                                        = None
  var deadState: DeadState.Value                                     = DeadState.Dead
  val projectiles: Array[Option[Projectile]] =
    Array.fill[Option[Projectile]](Projectile.rangeUID - Projectile.baseUID)(None)
  val projectilesToCleanUp: Array[Boolean]                            = Array.fill[Boolean](Projectile.rangeUID - Projectile.baseUID)(false)
  var drawDeloyableIcon: PlanetSideGameObject with Deployable => Unit = RedrawDeployableIcons
  var updateSquad: () => Unit                                         = NoSquadUpdates
  var recentTeleportAttempt: Long                                     = 0
  var lastTerminalOrderFulfillment: Boolean                           = true
  var shiftPosition: Option[Vector3]                                  = None
  var shiftOrientation: Option[Vector3]                               = None
  var setupAvatarFunc: () => Unit                                     = AvatarCreate
  var setCurrentAvatarFunc: Player => Unit                            = SetCurrentAvatarNormally
  var persist: () => Unit                                             = NoPersistence

  /**
    * used during zone transfers to maintain reference to seated vehicle (which does not yet exist in the new zone)
    * used during intrazone gate transfers, but not in a way distinct from prior zone transfer procedures
    * should only be set during the transient period when moving between one spawn point and the next
    * leaving set prior to a subsequent transfers may cause unstable vehicle associations, with memory leak potential
    */
  var interstellarFerry: Option[Vehicle] = None

  /**
    * used during zone transfers for cleanup to refer to the vehicle that instigated a transfer
    * "top level" is the carrier in a carrier/ferried association or a projected carrier/(ferried carrier)/ferried association
    * inherited from parent (carrier) to child (ferried) through the `TransferPassenger` message
    * the old-zone unique identifier for the carrier
    * no harm should come from leaving the field set to an old unique identifier value after the transfer period
    */
  var interstellarFerryTopLevelGUID: Option[PlanetSideGUID] = None
  val squadUI: mutable.LongMap[SquadUIElement]              = new mutable.LongMap[SquadUIElement]()
  var squad_supplement_id: Int                              = 0

  /**
    * When joining or creating a squad, the original state of the avatar's internal LFS variable is blanked.
    * This `WorldSessionActor`-local variable is then used to indicate the ongoing state of the LFS UI component,
    * now called "Looking for Squad Member."
    * Only the squad leader may toggle the LFSM marquee.
    * Upon leaving or disbanding a squad, this value is made false.
    * Control switching between the `Avatar`-local and the `WorldSessionActor`-local variable is contingent on `squadUI` being populated.
    */
  var lfsm: Boolean                       = false
  var squadSetup: () => Unit              = FirstTimeSquadSetup
  var squadUpdateCounter: Int             = 0
  val queuedSquadActions: Seq[() => Unit] = Seq(SquadUpdates, NoSquadUpdates, NoSquadUpdates, NoSquadUpdates)

  /** Upstream message counter<br>
    * Checks for server acknowledgement of the following messages in the following conditions:<br>
    *   `PlayerStateMessageUpstream` (infantry)<br>
    *   `VehicleStateMessage` (driver seat only)<br>
    *   `ChildObjectStateMessage` (any gunner seat that is not the driver)<br>
    *   `KeepAliveMessage` (any passenger seat that is not the driver)<br>
    * As they should arrive roughly every 250 milliseconds this allows for a very crude method of scheduling tasks up to four times per second
    */
  var upstreamMessageCount: Int                                              = 0
  var zoningType: Zoning.Method.Value                                        = Zoning.Method.None
  var zoningChatMessageType: ChatMessageType                                 = ChatMessageType.CMT_QUIT
  var zoningStatus: Zoning.Status.Value                                      = Zoning.Status.None
  var zoningCounter: Int                                                     = 0
  var instantActionFallbackDestination: Option[Zoning.InstantAction.Located] = None
  lazy val unsignedIntMaxValue: Long                                         = Int.MaxValue.toLong * 2L + 1L
  var serverTime: Long                                                       = 0
  var amsSpawnPoints: List[SpawnPoint]                                       = Nil

  /** a flag for the zone having finished loading during zoning
    * `None` when no zone is loaded
    * `Some(true)` when a zone has successfully loaded
    * `Some(false)` when the loading process has failed or was executed but did not complete for some reason
    */
  var zoneLoaded: Option[Boolean] = None

  /** a flag that forces the current zone to reload itself during a zoning operation */
  var zoneReload: Boolean                            = false
  var interimUngunnedVehicle: Option[PlanetSideGUID] = None
  var interimUngunnedVehicleSeat: Option[Int]        = None
  var keepAliveFunc: () => Unit                      = NormalKeepAlive
  var setAvatar: Boolean                             = false
  var turnCounterFunc: PlanetSideGUID => Unit        = TurnCounterDuringInterim

  var clientKeepAlive: Cancellable   = Default.Cancellable
  var progressBarUpdate: Cancellable = Default.Cancellable
  var reviveTimer: Cancellable       = Default.Cancellable
  var respawnTimer: Cancellable      = Default.Cancellable
  var zoningTimer: Cancellable       = Default.Cancellable

  def session: Session = _session

  def session_=(session: Session): Unit = {
    // TODO use Topic
    chatActor ! ChatActor.SetSession(session)
    avatarActor ! AvatarActor.SetSession(session)
    _session = session
  }

  def account: Account = _session.account

  def continent: Zone = _session.zone // TODO continent -> zone
  def player: Player  = _session.player

  def avatar: Avatar = _session.avatar

  val serviceManager = ServiceManager.serviceManager
  serviceManager ! Lookup("accountIntermediary")
  serviceManager ! Lookup("accountPersistence")
  serviceManager ! Lookup("galaxy")
  serviceManager ! Lookup("squad")
  serviceManager ! Lookup("propertyOverrideManager")

  ServiceManager.receptionist ! Receptionist.Find(
    InterstellarClusterService.InterstellarClusterServiceKey,
    context.self
  )

  override def postStop(): Unit = {
    //normally, the player avatar persists a minute or so after disconnect; we are subject to the SessionReaper
    clientKeepAlive.cancel()
    progressBarUpdate.cancel()
    reviveTimer.cancel()
    respawnTimer.cancel()
    galaxyService ! Service.Leave()
    continent.AvatarEvents ! Service.Leave()
    continent.LocalEvents ! Service.Leave()
    continent.VehicleEvents ! Service.Leave()

    // when going from classic -> typed this seems necessary
    context.stop(avatarActor)
    context.stop(chatActor)

    if (avatar != null) {
      //TODO put any temporary values back into the avatar
      squadService ! Service.Leave(Some(s"${avatar.faction}"))
      if (player != null && player.HasGUID) {
        prefire.orElse(shooting) match {
          case Some(guid) =>
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ChangeFireState_Stop(player.GUID, guid)
            )
          case None => ;
        }
      }
    }
  }

  def ValidObject(id: Int): Option[PlanetSideGameObject] = ValidObject(Some(PlanetSideGUID(id)))

  def ValidObject(id: PlanetSideGUID): Option[PlanetSideGameObject] = ValidObject(Some(id))

  def ValidObject(id: Option[PlanetSideGUID]): Option[PlanetSideGameObject] =
    continent.GUID(id) match {
      case Some(obj: LocalProjectile) =>
        FindProjectileEntry(id.get)

      case Some(_: LocalLockerItem) =>
        player.avatar.locker.Inventory.hasItem(id.get) match {
          case out @ Some(_) =>
            out
          case None =>
            //delete stale entity reference from client
            log.warn(s"${player.Name} has an invalid reference to GUID ${id.get} in zone ${continent.id}")
            sendResponse(ObjectDeleteMessage(id.get, 0))
            None
        }

      case out @ Some(obj) if obj.HasGUID =>
        out

      case None if id.nonEmpty && id.get != PlanetSideGUID(0) =>
        //delete stale entity reference from client
        log.warn(s"${player.Name} has an invalid reference to GUID ${id.get} in zone ${continent.id}")
        sendResponse(ObjectDeleteMessage(id.get, 0))
        None

      case _ =>
        None
    }

  def receive: Receive = {
    case LookupResult("accountIntermediary", endpoint) =>
      accountIntermediary = endpoint
      log.info("ID: " + session.id + " Got account intermediary service " + endpoint)
    case LookupResult("accountPersistence", endpoint) =>
      accountPersistence = endpoint
      log.info("ID: " + session.id + " Got account persistence service " + endpoint)
    case LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.info("ID: " + session.id + " Got galaxy service " + endpoint)
    case LookupResult("squad", endpoint) =>
      squadService = endpoint
      log.info("ID: " + session.id + " Got squad service " + endpoint)
    case LookupResult("propertyOverrideManager", endpoint) =>
      propertyOverrideManager = endpoint
      log.info("ID: " + session.id + " Got propertyOverrideManager service " + endpoint)

    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      cluster = listings.head

    // Avatar subscription update
    case avatar: Avatar =>
    /*
      log.info(s"new Avatar ${avatar.id}")
      if (session.player != null) session.player.avatar = avatar
      session = session.copy(avatar = avatar)
     */

    case SetAvatar(avatar) =>
      if (session.player != null) session.player.avatar = avatar
      session = session.copy(avatar = avatar)
      LivePlayerList.Update(avatar.id, avatar)

    case AvatarActor.AvatarResponse(avatar) =>
      session = session.copy(avatar = avatar)
      accountPersistence ! AccountPersistenceService.Login(avatar.name)

    case AvatarActor.AvatarLoginResponse(avatar) =>
      session = session.copy(avatar = avatar)
      Deployables.InitializeDeployableQuantities(avatar)
      cluster ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case packet: PlanetSideGamePacket =>
      handleGamePkt(packet)

    case PokeClient() =>
      sendResponse(KeepAliveMessage())

    case AvatarServiceResponse(toChannel, guid, reply) =>
      HandleAvatarServiceResponse(toChannel, guid, reply)

    case SendResponse(packet) =>
      sendResponse(packet)

    case SetSpeed(speed) =>
      session = session.copy(speed = speed)

    case SetFlying(flying) =>
      session = session.copy(flying = flying)

    case SetSpectator(spectator) =>
      session.player.spectator = spectator

    case Recall() =>
      zoningType = Zoning.Method.Recall
      zoningChatMessageType = ChatMessageType.CMT_RECALL
      zoningStatus = Zoning.Status.Request
      beginZoningCountdown(() => {
        cluster ! InterstellarClusterService.GetRandomSpawnPoint(
          Zones.sanctuaryZoneNumber(player.Faction),
          player.Faction,
          Seq(SpawnGroup.Sanctuary),
          context.self
        )
      })

    case InstantAction() =>
      zoningType = Zoning.Method.InstantAction
      zoningChatMessageType = ChatMessageType.CMT_INSTANTACTION
      zoningStatus = Zoning.Status.Request
      /* TODO no ask or adapters from classic to typed so this logic is happening in SpawnPointResponse
      implicit val timeout = Timeout(1 seconds)
      val future =
        ask(cluster.toClassic, InterstellarClusterService.GetInstantActionSpawnPoint(player.Faction, context.self))
          .mapTo[InterstellarClusterService.SpawnPointResponse]
      Await.result(future, 2 second) match {
        case InterstellarClusterService.SpawnPointResponse(None) =>
          sendResponse(
            ChatMsg(ChatMessageType.CMT_INSTANTACTION, false, "", "@InstantActionNoHotspotsAvailable", None)
          )
        case InterstellarClusterService.SpawnPointResponse(Some(_)) =>
          beginZoningCountdown(() => {
            cluster ! InterstellarClusterService.GetInstantActionSpawnPoint(player.Faction, context.self)
          })
      }

      beginZoningCountdown(() => {
        cluster ! InterstellarClusterService.GetInstantActionSpawnPoint(player.Faction, context.self)
      })
       */
      cluster ! InterstellarClusterService.GetInstantActionSpawnPoint(player.Faction, context.self)

    case Quit() =>
      //priority to quitting is given to quit over other zoning methods
      if (session.zoningType == Zoning.Method.InstantAction || session.zoningType == Zoning.Method.Recall) {
        CancelZoningProcessWithDescriptiveReason("cancel")
      }
      zoningType = Zoning.Method.Quit
      zoningChatMessageType = ChatMessageType.CMT_QUIT
      zoningStatus = Zoning.Status.Request
      beginZoningCountdown(() => {
        log.info("Good-bye")
        ImmediateDisconnect()
      })

    case Suicide() =>
      suicide(player)

    case Kick(player, time) =>
      AdministrativeKick(player)
      accountPersistence ! AccountPersistenceService.Kick(player.Name, time)

    case SetZone(zoneId, position) =>
      PlayerActionsToCancel()
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
          vehicle.PassengerInSeat(player) match {
            case Some(0) =>
              deadState = DeadState.Release // cancel movement updates
              vehicle.Position = position
              LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds)
            case _ => // not seated as the driver, in which case we can't move
          }
        case None =>
          deadState = DeadState.Release // cancel movement updates
          player.Position = position
          // continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, player.GUID))
          LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds)
        case _ => // seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
      }

    case SetPosition(position) =>
      PlayerActionsToCancel()
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
          vehicle.PassengerInSeat(player) match {
            case Some(0) =>
              deadState = DeadState.Release // cancel movement updates
              vehicle.Position = position
              LoadZonePhysicalSpawnPoint(continent.id, position, Vector3.z(vehicle.Orientation.z), 0 seconds)
            case _ => // not seated as the driver, in which case we can't move
          }
        case None =>
          deadState = DeadState.Release // cancel movement updates
          player.Position = position
          sendResponse(PlayerStateShiftMessage(ShiftState(0, position, player.Orientation.z, None)))
          deadState = DeadState.Alive // must be set here
        case _ => // seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
      }

    case SetConnectionState(state) =>
      connectionState = state

    case SetSilenced(silenced) =>
      player.silenced = silenced

    case CommonMessages.Progress(rate, finishedAction, stepAction) =>
      if (progressBarValue.isEmpty) {
        progressBarValue = Some(-rate)
        self ! ProgressEvent(rate, finishedAction, stepAction)
      }

    case ProgressEvent(delta, finishedAction, stepAction, tick) =>
      HandleProgressChange(delta, finishedAction, stepAction, tick)

    case GalaxyServiceResponse(_, reply) =>
      reply match {
        case GalaxyResponse.HotSpotUpdate(zone_index, priority, hot_spot_info) =>
          sendResponse(
            HotSpotUpdateMessage(
              zone_index,
              priority,
              hot_spot_info.map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
            )
          )
        case GalaxyResponse.MapUpdate(msg) =>
          sendResponse(msg)

        case GalaxyResponse.TransferPassenger(temp_channel, vehicle, vehicle_to_delete, manifest) =>
          (manifest.passengers.find { case (name, _) => player.Name.equals(name) } match {
            case Some((name, index)) if vehicle.Seats(index).Occupant.isEmpty =>
              vehicle.Seats(index).Occupant = player
              Some(vehicle)
            case Some((name, index)) =>
              log.warn(s"TransferPassenger: seat $index is already occupied")
              None
            case None =>
              None
          }).orElse(manifest.cargo.find { case (name, _) => player.Name.equals(name) } match {
            case Some((name, index)) =>
              vehicle.CargoHolds(index).Occupant match {
                case Some(cargo) =>
                  cargo.Seats(0).Occupant match {
                    case Some(driver) if driver.Name.equals(name) =>
                      Some(cargo)
                    case _ =>
                      None
                  }
                case None =>
                  None
              }
            case None =>
              None
          }) match {
            case Some(v) =>
              galaxyService ! Service.Leave(Some(temp_channel)) //temporary vehicle-specific channel (see above)
              deadState = DeadState.Release
              sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
              interstellarFerry = Some(v) //on the other continent and registered to that continent's GUID system
              LoadZonePhysicalSpawnPoint(v.Continent, v.Position, v.Orientation, 1 seconds)
            case None =>
              interstellarFerry match {
                case None =>
                  galaxyService ! Service.Leave(Some(temp_channel)) //no longer being transferred between zones
                  interstellarFerryTopLevelGUID = None
                case Some(_) => ;
                //wait patiently
              }
          }
      }

    case LocalServiceResponse(toChannel, guid, reply) =>
      HandleLocalServiceResponse(toChannel, guid, reply)

    case Mountable.MountMessages(tplayer, reply) =>
      HandleMountMessages(tplayer, reply)

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      HandleTerminalMessage(tplayer, msg, order)

    case ProximityUnit.Action(term, target) =>
      SelectProximityUnitBehavior(term, target)

    case VehicleServiceResponse(toChannel, guid, reply) =>
      HandleVehicleServiceResponse(toChannel, guid, reply)

    case SquadServiceResponse(_, excluded, response) =>
      if (!excluded.exists(_ == avatar.id)) {
        response match {
          case SquadResponse.ListSquadFavorite(line, task) =>
            sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), line, SquadAction.ListSquadFavorite(task)))

          case SquadResponse.InitList(infos) =>
            sendResponse(ReplicationStreamMessage(infos))

          case SquadResponse.UpdateList(infos) if infos.nonEmpty =>
            sendResponse(
              ReplicationStreamMessage(
                6,
                None,
                infos.map {
                  case (index, squadInfo) =>
                    SquadListing(index, squadInfo)
                }.toVector
              )
            )

          case SquadResponse.RemoveFromList(infos) if infos.nonEmpty =>
            sendResponse(
              ReplicationStreamMessage(
                1,
                None,
                infos.map { index =>
                  SquadListing(index, None)
                }.toVector
              )
            )

          case SquadResponse.Detail(guid, detail) =>
            sendResponse(SquadDetailDefinitionUpdateMessage(guid, detail))

          case SquadResponse.AssociateWithSquad(squad_guid) =>
            sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.AssociateWithSquad()))

          case SquadResponse.SetListSquad(squad_guid) =>
            sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.SetListSquad()))

          case SquadResponse.Membership(request_type, unk1, unk2, charId, opt_char_id, player_name, unk5, unk6) =>
            val name = request_type match {
              case SquadResponseType.Invite if unk5 =>
                //player_name is our name; the name of the player indicated by unk3 is needed
                LivePlayerList.WorldPopulation({ case (_, a: Avatar) => charId == a.id }).headOption match {
                  case Some(player) =>
                    player.name
                  case None =>
                    player_name
                }
              case _ =>
                player_name
            }
            sendResponse(SquadMembershipResponse(request_type, unk1, unk2, charId, opt_char_id, name, unk5, unk6))

          case SquadResponse.WantsSquadPosition(_, name) =>
            sendResponse(
              ChatMsg(
                ChatMessageType.CMT_SQUAD,
                true,
                name,
                s"\\#6 would like to join your squad. (respond with \\#3/accept\\#6 or \\#3/reject\\#6)",
                None
              )
            )

          case SquadResponse.Join(squad, positionsToUpdate, toChannel) =>
            val leader              = squad.Leader
            val membershipPositions = positionsToUpdate map squad.Membership.zipWithIndex
            membershipPositions.find({ case (member, _) => member.CharId == avatar.id }) match {
              case Some((ourMember, ourIndex)) =>
                //we are joining the squad
                //load each member's entry (our own too)
                squad_supplement_id = squad.GUID.guid + 1
                membershipPositions.foreach {
                  case (member, index) =>
                    sendResponse(
                      SquadMemberEvent.Add(
                        squad_supplement_id,
                        member.CharId,
                        index,
                        member.Name,
                        member.ZoneId,
                        unk7 = 0
                      )
                    )
                    squadUI(member.CharId) =
                      SquadUIElement(member.Name, index, member.ZoneId, member.Health, member.Armor, member.Position)
                }
                //repeat our entry
                sendResponse(
                  SquadMemberEvent.Add(
                    squad_supplement_id,
                    ourMember.CharId,
                    ourIndex,
                    ourMember.Name,
                    ourMember.ZoneId,
                    unk7 = 0
                  )
                ) //repeat of our entry
                val playerGuid = player.GUID
                //turn lfs off
                val factionChannel = s"${player.Faction}"
                if (avatar.lookingForSquad) {
                  avatarActor ! AvatarActor.SetLookingForSquad(false)
                }
                //squad colors
                GiveSquadColorsInZone()
                continent.AvatarEvents ! AvatarServiceMessage(
                  factionChannel,
                  AvatarAction.PlanetsideAttribute(playerGuid, 31, squad_supplement_id)
                )
                //associate with member position in squad
                sendResponse(PlanetsideAttributeMessage(playerGuid, 32, ourIndex))
                //a finalization? what does this do?
                sendResponse(SquadDefinitionActionMessage(squad.GUID, 0, SquadAction.Unknown(18)))
                updateSquad = PeriodicUpdatesWhenEnrolledInSquad
                chatActor ! ChatActor.JoinChannel(ChatService.ChatChannel.Squad(squad.GUID))
              case _ =>
                //other player is joining our squad
                //load each member's entry
                GiveSquadColorsInZone(
                  membershipPositions.map {
                    case (member, index) =>
                      val charId = member.CharId
                      sendResponse(
                        SquadMemberEvent.Add(squad_supplement_id, charId, index, member.Name, member.ZoneId, unk7 = 0)
                      )
                      squadUI(charId) =
                        SquadUIElement(member.Name, index, member.ZoneId, member.Health, member.Armor, member.Position)
                      charId
                  }
                )
            }
            //send an initial dummy update for map icon(s)
            sendResponse(
              SquadState(
                PlanetSideGUID(squad_supplement_id),
                membershipPositions
                  .filterNot { case (member, _) => member.CharId == avatar.id }
                  .map {
                    case (member, _) =>
                      SquadStateInfo(
                        member.CharId,
                        member.Health,
                        member.Armor,
                        member.Position,
                        2,
                        2,
                        false,
                        429,
                        None,
                        None
                      )
                  }
                  .toList
              )
            )

          case SquadResponse.Leave(squad, positionsToUpdate) =>
            positionsToUpdate.find({ case (member, _) => member == avatar.id }) match {
              case Some((ourMember, ourIndex)) =>
                //we are leaving the squad
                //remove each member's entry (our own too)
                positionsToUpdate.foreach {
                  case (member, index) =>
                    sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                    squadUI.remove(member)
                }
                //uninitialize
                val playerGuid = player.GUID
                sendResponse(SquadMemberEvent.Remove(squad_supplement_id, ourMember, ourIndex)) //repeat of our entry
                sendResponse(PlanetsideAttributeMessage(playerGuid, 31, 0))                     //disassociate with squad?
                continent.AvatarEvents ! AvatarServiceMessage(
                  s"${player.Faction}",
                  AvatarAction.PlanetsideAttribute(playerGuid, 31, 0)
                )
                sendResponse(
                  PlanetsideAttributeMessage(playerGuid, 32, 0)
                )                                                                     //disassociate with member position in squad?
                sendResponse(PlanetsideAttributeMessage(playerGuid, 34, 4294967295L)) //unknown, perhaps unrelated?
                lfsm = false
                //a finalization? what does this do?
                sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
                squad_supplement_id = 0
                squadUpdateCounter = 0
                updateSquad = NoSquadUpdates
                chatActor ! ChatActor.LeaveChannel(ChatService.ChatChannel.Squad(squad.GUID))
              case _ =>
                //remove each member's entry
                GiveSquadColorsInZone(
                  positionsToUpdate.map {
                    case (member, index) =>
                      sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                      squadUI.remove(member)
                      member
                  },
                  value = 0
                )
            }

          case SquadResponse.AssignMember(squad, from_index, to_index) =>
            //we've already swapped position internally; now we swap the cards
            SwapSquadUIElements(squad, from_index, to_index)

          case SquadResponse.PromoteMember(squad, char_id, from_index, to_index) =>
            val charId              = player.CharId
            val guid                = player.GUID
            lazy val factionChannel = s"${player.Faction}"
            //are we being demoted?
            if (squadUI(charId).index == 0) {
              //lfsm -> lfs
              if (lfsm) {
                sendResponse(PlanetsideAttributeMessage(guid, 53, 0))
                continent.AvatarEvents ! AvatarServiceMessage(
                  factionChannel,
                  AvatarAction.PlanetsideAttribute(guid, 53, 0)
                )
              }
              lfsm = false
              sendResponse(PlanetsideAttributeMessage(guid, 32, from_index)) //associate with member position in squad
            }
            //are we being promoted?
            else if (charId == char_id) {
              sendResponse(PlanetsideAttributeMessage(guid, 32, 0)) //associate with member position in squad
            }
            continent.AvatarEvents ! AvatarServiceMessage(
              factionChannel,
              AvatarAction.PlanetsideAttribute(guid, 31, squad_supplement_id)
            )
            //we must fix the squad cards backend
            SwapSquadUIElements(squad, from_index, to_index)

          case SquadResponse.UpdateMembers(squad, positions) =>
            val pairedEntries = positions.collect {
              case entry if squadUI.contains(entry.char_id) =>
                (entry, squadUI(entry.char_id))
            }
            //prune entries
            val updatedEntries = pairedEntries
              .collect({
                case (entry, element) if entry.zone_number != element.zone =>
                  //zone gets updated for these entries
                  sendResponse(
                    SquadMemberEvent.UpdateZone(squad_supplement_id, entry.char_id, element.index, entry.zone_number)
                  )
                  squadUI(entry.char_id) =
                    SquadUIElement(element.name, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                  entry
                case (entry, element)
                    if entry.health != element.health || entry.armor != element.armor || entry.pos != element.position =>
                  //other elements that need to be updated
                  squadUI(entry.char_id) =
                    SquadUIElement(element.name, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                  entry
              })
              .filterNot(_.char_id == avatar.id) //we want to update our backend, but not our frontend
            if (updatedEntries.nonEmpty) {
              sendResponse(
                SquadState(
                  PlanetSideGUID(squad_supplement_id),
                  updatedEntries.map { entry =>
                    SquadStateInfo(entry.char_id, entry.health, entry.armor, entry.pos, 2, 2, false, 429, None, None)
                  }
                )
              )
            }

          case SquadResponse.SquadSearchResults() =>
            //I don't actually know how to return search results
            sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.NoSquadSearchResults()))

          case SquadResponse.InitWaypoints(char_id, waypoints) =>
            waypoints.foreach {
              case (waypoint_type, info, unk) =>
                sendResponse(
                  SquadWaypointEvent.Add(
                    squad_supplement_id,
                    char_id,
                    waypoint_type,
                    WaypointEvent(info.zone_number, info.pos, unk)
                  )
                )
            }

          case SquadResponse.WaypointEvent(WaypointEventAction.Add, char_id, waypoint_type, _, Some(info), unk) =>
            sendResponse(
              SquadWaypointEvent.Add(
                squad_supplement_id,
                char_id,
                waypoint_type,
                WaypointEvent(info.zone_number, info.pos, unk)
              )
            )

          case SquadResponse.WaypointEvent(WaypointEventAction.Remove, char_id, waypoint_type, _, _, _) =>
            sendResponse(SquadWaypointEvent.Remove(squad_supplement_id, char_id, waypoint_type))

          case _ => ;
        }
      }

    case Deployment.CanDeploy(obj, state) =>
      if (state == DriveState.Deploying) {
        log.info(s"DeployRequest: $obj transitioning to deploy state")
      } else if (state == DriveState.Deployed) {
        log.info(s"DeployRequest: $obj has been Deployed")
      } else {
        CanNotChangeDeployment(obj, state, "incorrect deploy state")
      }

    case Deployment.CanUndeploy(obj, state) =>
      if (state == DriveState.Undeploying) {
        log.info(s"DeployRequest: $obj transitioning to undeploy state")
      } else if (state == DriveState.Mobile) {
        log.info(s"DeployRequest: $obj is Mobile")
      } else {
        CanNotChangeDeployment(obj, state, "incorrect undeploy state")
      }

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      if (Deployment.CheckForDeployState(state) && !VehicleControl.DeploymentAngleCheck(obj)) {
        CanNotChangeDeployment(obj, state, "ground too steep")
      } else {
        CanNotChangeDeployment(obj, state, reason)
      }

    case Zone.Population.PlayerHasLeft(zone, None) =>
      log.info(s"${avatar.name} does not have a body on ${zone.id}")

    case Zone.Population.PlayerHasLeft(zone, Some(tplayer)) =>
      if (tplayer.isAlive) {
        log.info(s"${tplayer.Name} has left zone ${zone.id}")
      }

    case Zone.Population.PlayerCanNotSpawn(zone, tplayer) =>
      log.warn(s"${tplayer.Name} can not spawn in zone ${zone.id}; why?")

    case Zone.Population.PlayerAlreadySpawned(zone, tplayer) =>
      log.warn(s"${tplayer.Name} is already spawned on zone ${zone.id}; a clerical error?")

    case InterstellarClusterService.SpawnPointResponse(response) =>
      zoningType match {
        case Zoning.Method.InstantAction if response.isEmpty =>
          CancelZoningProcessWithReason("@InstantActionNoHotspotsAvailable")

        case Zoning.Method.InstantAction if zoningStatus == Zoning.Status.Request =>
          beginZoningCountdown(() => {
            cluster ! InterstellarClusterService.GetInstantActionSpawnPoint(player.Faction, context.self)
          })

        case zoningType =>
          val currentZoningType = zoningType
          CancelZoningProcess()
          PlayerActionsToCancel()
          CancelAllProximityUnits()
          continent.Population ! Zone.Population.Release(avatar)
          response match {
            case Some((zone, spawnPoint)) =>
              val obj = continent.GUID(player.VehicleSeated) match {
                case Some(obj: Vehicle) if !obj.Destroyed =>
                  obj
                case _ =>
                  player
              }
              val (pos, ori) = spawnPoint.SpecificPoint(obj)
              if (zoningType == Zoning.Method.InstantAction)
                LoadZonePhysicalSpawnPoint(zone.id, pos, ori, 0 seconds)
              else
                LoadZonePhysicalSpawnPoint(zone.id, pos, ori, CountSpawnDelay(zone.id, spawnPoint, continent.id))
            case None =>
              log.error("got None spawn point response from InterstellarClusterService")
              Thread.sleep(1000) // throttle in case of infinite loop
              RequestSanctuaryZoneSpawn(player, 0)
          }
      }

    case msg @ Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      log.warn(s"$msg")

    case msg @ Zone.Vehicle.CanNotDespawn(zone, vehicle, reason) =>
      log.warn(s"$msg")

    case Zone.Deployable.DeployableIsBuilt(obj, tool) =>
      val index = player.Find(tool) match {
        case Some(x) =>
          x
        case None =>
          player.LastDrawnSlot
      }
      if (avatar.deployables.Accept(obj) || (avatar.deployables.Valid(obj) && !avatar.deployables.Contains(obj))) {
        tool.Definition match {
          case GlobalDefinitions.ace =>
            continent.LocalEvents ! LocalServiceMessage(
              continent.id,
              LocalAction.TriggerEffectLocation(player.GUID, "spawn_object_effect", obj.Position, obj.Orientation)
            )
          case GlobalDefinitions.advanced_ace =>
            sendResponse(
              GenericObjectActionMessage(player.GUID, 53)
            ) //put fdu down; it will be removed from the client's holster
            continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.PutDownFDU(player.GUID))
          case GlobalDefinitions.router_telepad => ;
          case _ =>
            log.warn(
              s"Zone.Deployable.DeployableIsBuilt: not sure what kind of construction item to animate - ${tool.Definition}"
            )
        }
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(
          obj.Definition.DeployTime milliseconds,
          self,
          SessionActor.FinalizeDeployable(obj, tool, index)
        )
      } else {
        TryDropFDU(tool, index, obj.Position)
        sendResponse(ObjectDeployedMessage.Failure(obj.Definition.Name))
        obj.Position = Vector3.Zero
        obj.AssignOwnership(None)
        continent.Deployables ! Zone.Deployable.Dismiss(obj)
      }

    case SessionActor.FinalizeDeployable(obj: SensorDeployable, tool, index) =>
      //motion alarm sensor and sensor disruptor
      DeployableBuildActivity(obj)
      continent.LocalEvents ! LocalServiceMessage(
        continent.id,
        LocalAction.TriggerEffectInfo(player.GUID, "on", obj.GUID, true, 1000)
      )
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)

    case SessionActor.FinalizeDeployable(obj: BoomerDeployable, tool, index) =>
      //boomers
      DeployableBuildActivity(obj)
      //TODO sufficiently delete the tool
      sendResponse(ObjectDeleteMessage(tool.GUID, 0))
      continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(player.GUID, tool.GUID))
      continent.tasks ! GUIDTask.UnregisterEquipment(tool)(continent.GUID)
      val trigger = new BoomerTrigger
      trigger.Companion = obj.GUID
      obj.Trigger = trigger
      val holster = player.Slot(index)
      if (holster.Equipment.contains(tool)) {
        holster.Equipment = None
        continent.tasks ! HoldNewEquipmentUp(player)(trigger, index)
      } else {
        //don't know where boomer trigger should go; drop it on the ground
        continent.tasks ! NewItemDrop(player, continent)(trigger)
      }

    case SessionActor.FinalizeDeployable(obj: ExplosiveDeployable, tool, index) =>
      //mines
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)

    case SessionActor.FinalizeDeployable(obj: ComplexDeployable, tool, index) =>
      //tank_traps, spitfires, deployable field turrets and the deployable_shield_generator
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)

    case SessionActor.FinalizeDeployable(obj: TelepadDeployable, tool, index) =>
      if (obj.Health > 0) {
        val guid = obj.GUID
        //router telepad deployable
        val router = tool.asInstanceOf[Telepad].Router
        //router must exist and be deployed
        continent.GUID(router) match {
          case Some(vehicle: Vehicle) =>
            val routerGUID = router.get
            if (vehicle.Destroyed) {
              //the Telepad was successfully deployed; but, before it could configure, its Router was destroyed
              sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", "@Telepad_NoDeploy_RouterLost", None))
              continent.LocalEvents ! LocalServiceMessage.Deployables(
                RemoverActor.AddTask(obj, continent, Some(0 seconds))
              )
            } else {
              log.info(s"FinalizeDeployable: setup for telepad #${guid.guid} in zone ${continent.id}")
              obj.Router = routerGUID //necessary; forwards link to the router
              DeployableBuildActivity(obj)
              RemoveOldEquipmentFromInventory(player)(tool)
              //it takes 60s for the telepad to become properly active
              continent.LocalEvents ! LocalServiceMessage.Telepads(RouterTelepadActivation.AddTask(obj, continent))
            }

          case _ =>
            //the Telepad was successfully deployed; but, before it could configure, its Router was deconstructed
            sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", "@Telepad_NoDeploy_RouterLost", None))
            continent.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(obj, continent, Some(0 seconds))
            )
        }
      }

    case SessionActor.FinalizeDeployable(obj: PlanetSideGameObject with Deployable, tool, index) =>
      val guid       = obj.GUID
      val definition = obj.Definition
      sendResponse(GenericObjectActionMessage(guid, 21)) //reset build cooldown
      sendResponse(ObjectDeployedMessage.Failure(definition.Name))
      log.warn(
        s"FinalizeDeployable: deployable ${definition.asInstanceOf[BaseDeployableDefinition].Item}@$guid not handled by specific case"
      )
      log.warn(s"FinalizeDeployable: deployable will be cleaned up, but may not get unregistered properly")
      TryDropFDU(tool, index, obj.Position)
      obj.Position = Vector3.Zero
      continent.Deployables ! Zone.Deployable.Dismiss(obj)

    //!!only dispatch Zone.Deployable.Dismiss from WorldSessionActor as cleanup if the target deployable was never fully introduced
    case Zone.Deployable.DeployableIsDismissed(obj: TurretDeployable) =>
      continent.tasks ! GUIDTask.UnregisterDeployableTurret(obj)(continent.GUID)

    //!!only dispatch Zone.Deployable.Dismiss from WorldSessionActor as cleanup if the target deployable was never fully introduced
    case Zone.Deployable.DeployableIsDismissed(obj) =>
      continent.tasks ! GUIDTask.UnregisterObjectTask(obj)(continent.GUID)

    case InterstellarClusterService.ZonesResponse(zones) =>
      zones.foreach { zone =>
        val continentNumber = zone.Number
        val popBO           = 0
        //TODO black ops test (partition)
        val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
        val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
        val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)

        zone.Buildings.foreach({ case (id, building) => initBuilding(continentNumber, building.MapId, building) })
        sendResponse(ZonePopulationUpdateMessage(continentNumber, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))
        if (continentNumber == 11)
          sendResponse(
            ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NC)
          ) // "The NC have captured the NC Sanctuary."
        else if (continentNumber == 12)
          sendResponse(
            ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.TR)
          ) // "The TR have captured the TR Sanctuary."
        else if (continentNumber == 13)
          sendResponse(
            ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.VS)
          ) // "The VS have captured the VS Sanctuary."
        else sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NEUTRAL))
        //CaptureFlagUpdateMessage()
        //VanuModuleUpdateMessage()
        //ModuleLimitsMessage()
        sendResponse(ZoneInfoMessage(continentNumber, true, 0))
        sendResponse(ZoneLockInfoMessage(continentNumber, false, true))
        sendResponse(ZoneForcedCavernConnectionsMessage(continentNumber, 0))
        sendResponse(
          HotSpotUpdateMessage(
            continentNumber,
            1,
            ZoneHotSpotProjector
              .SpecificHotSpotInfo(player.Faction, zone.HotSpots)
              .map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
          )
        ) //normally set for all zones in bulk; should be fine manually updating per zone like this
      }
      LivePlayerList.Add(avatar.id, avatar)
      //PropertyOverrideMessage

      implicit val timeout = Timeout(1 seconds)
      val future = ask(propertyOverrideManager, PropertyOverrideManager.GetOverridesMessage)
        .mapTo[List[PropertyOverrideMessage.GamePropertyScope]]
      val overrides = Await.result(future, 1 second)

      sendResponse(PropertyOverrideMessage(overrides))

      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty))    //clear squad list
      sendResponse(FriendsResponse(FriendAction.InitializeFriendList, 0, true, true, Nil))
      sendResponse(FriendsResponse(FriendAction.InitializeIgnoreList, 0, true, true, Nil))
      //the following subscriptions last until character switch/logout
      galaxyService ! Service.Join("galaxy")             //for galaxy-wide messages
      galaxyService ! Service.Join(s"${avatar.faction}") //for hotspots
      squadService ! Service.Join(s"${avatar.faction}")  //channel will be player.Faction
      squadService ! Service.Join(s"${avatar.id}")       //channel will be player.CharId (in order to work with packets)
      player.Zone match {
        case Zone.Nowhere =>
          RequestSanctuaryZoneSpawn(player, currentZone = 0)
        case zone =>
          log.info(s"Zone ${zone.id} will now load")
          loadConfZone = true
          val oldZone = continent
          session = session.copy(zone = zone)
          //the only zone-level event system subscription necessary before BeginZoningMessage (for persistence purposes)
          continent.AvatarEvents ! Service.Join(player.Name)
          persist()
          oldZone.AvatarEvents ! Service.Leave()
          oldZone.LocalEvents ! Service.Leave()
          oldZone.VehicleEvents ! Service.Leave()
          if (player.isAlive) {
            self ! NewPlayerLoaded(player)
          } else {
            zoneReload = true
            cluster ! InterstellarClusterService.GetNearbySpawnPoint(
              continent.Number,
              player,
              Seq(SpawnGroup.Facility, SpawnGroup.Tower),
              context.self
            )
          }
      }

    case InterstellarClusterService.ZoneResponse(zone) =>
      log.info(s"Zone ${zone.get.id} will now load")
      loadConfZone = true
      val oldZone = session.zone
      session = session.copy(zone = zone.get)
      //the only zone-level event system subscription necessary before BeginZoningMessage (for persistence purposes)
      continent.AvatarEvents ! Service.Join(player.Name)
      persist()
      oldZone.AvatarEvents ! Service.Leave()
      oldZone.LocalEvents ! Service.Leave()
      oldZone.VehicleEvents ! Service.Leave()
      continent.Population ! Zone.Population.Join(avatar)
      player.avatar = avatar
      interstellarFerry match {
        case Some(vehicle) if vehicle.PassengerInSeat(player).contains(0) =>
          continent.tasks ! RegisterDrivenVehicle(vehicle, player)
        case _ =>
          continent.tasks ! RegisterNewAvatar(player)
      }

    case NewPlayerLoaded(tplayer) =>
      //new zone
      log.info(s"Player ${tplayer.Name} has been loaded")
      tplayer.avatar = avatar
      session = session.copy(player = tplayer)
      avatarActor ! AvatarActor.CreateImplants()
      avatarActor ! AvatarActor.InitializeImplants()
      //LoadMapMessage causes the client to send BeginZoningMessage, eventually leading to SetCurrentAvatar
      val weaponsEnabled =
        session.zone.map.name != "map11" && session.zone.map.name != "map12" && session.zone.map.name != "map13"
      sendResponse(
        LoadMapMessage(
          session.zone.map.name,
          session.zone.id,
          40100,
          25,
          weaponsEnabled,
          session.zone.map.checksum
        )
      )
      //important! the LoadMapMessage must be processed by the client before the avatar is created
      setupAvatarFunc()
      //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
      turnCounterFunc = interimUngunnedVehicle match {
        case Some(_) =>
          TurnCounterDuringInterimWhileInPassengerSeat
        case None =>
          TurnCounterDuringInterim
      }
      keepAliveFunc = NormalKeepAlive
      upstreamMessageCount = 0
      setAvatar = false
      persist()

    case PlayerLoaded(tplayer) =>
      //same zone
      log.info(s"Player ${tplayer.Name} will respawn")
      tplayer.avatar = avatar
      session = session.copy(player = tplayer)
      setupAvatarFunc()
      //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
      turnCounterFunc = interimUngunnedVehicle match {
        case Some(_) =>
          TurnCounterDuringInterimWhileInPassengerSeat
        case None =>
          TurnCounterDuringInterim
      }
      keepAliveFunc = NormalKeepAlive
      upstreamMessageCount = 0
      setAvatar = false
      persist()

    case PlayerFailedToLoad(tplayer) =>
      player.Continent match {
        case _ =>
          failWithError(s"${tplayer.Name} failed to load anywhere")
      }

    /**
      * The user is either already in the current zone and merely transporting himself from one location to another,
      * also called "dying", or occasionally "deconstructing,"
      * or is completely switching in between zones.
      * These correspond to the message NewPlayerLoaded for the case of "dying" or the latter zone switching case,
      * and PlayerLoaded for "deconstruction."
      * In the latter case, the user must wait for the zone to be recognized as loaded for the server
      * and this is performed through the send LoadMapMessage, receive BeginZoningMessage exchange
      * The user's player should have already been registered into the new zone
      * and is at some stage of being added to the zone in which they will have control agency in that zone.
      * Whether or not the zone is loaded in the earlier case depends on the destination with respect to the current location.
      * Once all of the following is (assumed) accomplished,
      * the server will attempt to declare that user's player the avatar of the user's client.
      * Reception of certain packets that represent "reported user activity" after that marks the end of avatar loading.
      * If the maximum number of unsuccessful attempts is reached, some course of action is taken.
      * If the player dies, the process does not need to continue.
      * He may or may not be accompanied by a vehicle at any stage of this process.
      */
    case SetCurrentAvatar(tplayer, max_attempts, attempt) =>
      respawnTimer.cancel()
      val waitingOnUpstream = upstreamMessageCount == 0
      if (attempt >= max_attempts && waitingOnUpstream) {
        log.warn(
          s"SetCurrentAvatar-max attempt failure: " +
            s"zone=${if (zoneLoaded.contains(true)) "loaded"
            else if (zoneLoaded.contains(false)) "failed"
            else "unloaded"}," +
            s"guid=${tplayer.HasGUID}, control=${(tplayer.Actor != Default.Actor)}, avatar=$waitingOnUpstream"
        )
        zoneLoaded match {
          case None | Some(false) =>
            log.warn(
              "SetCurrentAvatar-max attempt failure: failed to load intended destination zone; routing to faction sanctuary"
            )
            RequestSanctuaryZoneSpawn(tplayer, continent.Number)
          case _ =>
            log.warn(
              "SetCurrentAvatar-max attempt failure: the zone loaded but elements remain unready; restarting the process ..."
            )
            val pos    = shiftPosition.getOrElse(player.Position)
            val orient = shiftOrientation.getOrElse(player.Orientation)
            deadState = DeadState.Release
            sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, pos, player.Faction, true))
            val toZoneId = continent.id
            tplayer.Die
            continent.Population ! Zone.Population.Leave(avatar) //does not matter if it doesn't work
            zoneLoaded = None
            zoneReload = true
            LoadZonePhysicalSpawnPoint(toZoneId, pos, orient, 0 seconds)
        }
      } else if (tplayer.isAlive) {
        if (
          zoneLoaded.contains(true) &&
          tplayer.HasGUID && tplayer.Actor != Default.Actor && (continent.GUID(tplayer.VehicleSeated) match {
            case Some(o: Vehicle) => o.HasGUID && o.Actor != Default.Actor && !o.Destroyed
            case _                => true
          })
        ) {
          if (!setAvatar || waitingOnUpstream) {
            setCurrentAvatarFunc(tplayer)
            respawnTimer = context.system.scheduler.scheduleOnce(
              delay = (if (attempt <= max_attempts / 2) 10 else 5) seconds,
              self,
              SetCurrentAvatar(tplayer, max_attempts, attempt + max_attempts / 3)
            )
          }
          //if not the condition above, player has started playing normally
        } else {
          respawnTimer = context.system.scheduler.scheduleOnce(
            500 milliseconds,
            self,
            SetCurrentAvatar(tplayer, max_attempts, attempt + 1)
          )
        }
      }

    case Vitality.DamageResolution(target: TelepadDeployable, _) =>
      //telepads
      if (target.Health <= 0) {
        //update if destroyed
        target.Destroyed = true
        val guid = target.GUID
        continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(player.GUID, guid))
        Deployables.AnnounceDestroyDeployable(target, Some(0 seconds))
      }

    case Vitality.DamageResolution(target: PlanetSideGameObject, _) =>
      log.warn(s"Vital target ${target.Definition.Name} damage resolution not supported using this method")

    case ResponseToSelf(pkt) =>
      //log.info(s"Received a direct message: $pkt")
      sendResponse(pkt)

    case ReceiveAccountData(account) =>
      log.info(s"ReceiveAccountData ${account}")
      session = session.copy(account = account)
      avatarActor ! AvatarActor.SetAccount(account)

    case LoadedRemoteProjectile(projectile_guid, Some(projectile)) =>
      if (projectile.profile.ExistsOnRemoteClients) {
        //spawn projectile on other clients
        val definition = projectile.Definition
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.LoadProjectile(
            player.GUID,
            definition.ObjectId,
            projectile_guid,
            definition.Packet.ConstructorData(projectile).get
          )
        )
      }
      //immediately slated for deletion?
      CleanUpRemoteProjectile(projectile.GUID, projectile)

    case LoadedRemoteProjectile(projectile_guid, None) =>
      continent.GUID(projectile_guid) match {
        case Some(obj: Projectile) if obj.profile.ExistsOnRemoteClients =>
          //spawn projectile on other clients
          val definition = obj.Definition
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.LoadProjectile(
              player.GUID,
              definition.ObjectId,
              projectile_guid,
              definition.Packet.ConstructorData(obj).get
            )
          )
        case _ => ;
      }

    case PlayerToken.LoginInfo(name, Zone.Nowhere, _) =>
      log.info(s"LoginInfo: player $name is considered a new character")
      //TODO poll the database for saved zone and coordinates?
      persist = UpdatePersistence(sender())
      deadState = DeadState.RespawnTime

      session = session.copy(player = new Player(avatar))
      //xy-coordinates indicate sanctuary spawn bias:
      player.Position = math.abs(scala.util.Random.nextInt() % avatar.name.hashCode % 4) match {
        case 0 => Vector3(8192, 8192, 0) //NE
        case 1 => Vector3(8192, 0, 0)    //SE
        case 2 => Vector3(0, 0, 0)       //SW
        case 3 => Vector3(0, 8192, 0)    //NW
      }
      DefinitionUtil.applyDefaultLoadout(player)
      avatarActor ! AvatarActor.LoginAvatar(context.self)

    case PlayerToken.LoginInfo(playerName, inZone, pos) =>
      log.info(s"LoginInfo: player $playerName is already logged in zone ${inZone.id}; rejoining that character")
      persist = UpdatePersistence(sender())
      //tell the old WorldSessionActor to kill itself by using its own subscriptions against itself
      inZone.AvatarEvents ! AvatarServiceMessage(playerName, AvatarAction.TeardownConnection())
      //find and reload previous player
      (
        inZone.Players.find(p => p.name.equals(playerName)),
        inZone.LivePlayers.find(p => p.Name.equals(playerName))
      ) match {
        case (_, Some(p)) if p.death_by == -1 =>
          //player is not allowed
          KickedByAdministration()

        case (Some(a), Some(p)) if p.isAlive =>
          //rejoin current avatar/player
          log.info(s"LoginInfo: player $playerName is alive")
          deadState = DeadState.Alive
          session = session.copy(player = p)
          persist()
          setupAvatarFunc = AvatarRejoin
          avatarActor ! AvatarActor.ReplaceAvatar(a)
          avatarActor ! AvatarActor.LoginAvatar(context.self)

        case (Some(a), Some(p)) =>
          //convert player to a corpse (unless in vehicle); automatic recall to closest spawn point
          log.info(s"LoginInfo: player $playerName is dead")
          deadState = DeadState.Dead
          session = session.copy(player = p)
          persist()
          player.Zone = inZone
          HandleReleaseAvatar(p, inZone)
          avatarActor ! AvatarActor.ReplaceAvatar(a)
          avatarActor ! AvatarActor.LoginAvatar(context.self)

        case (Some(a), None) =>
          //respawn avatar as a new player; automatic recall to closest spawn point
          log.info(s"LoginInfo: player $playerName had released recently")
          deadState = DeadState.RespawnTime
          session = session.copy(
            player = inZone.Corpses.findLast(c => c.Name == playerName) match {
              case Some(c) =>
                c //the last corpse of this user should be where they died
              case None =>
                val tplayer = Player(a) //throwaway
                tplayer.Position = pos
                tplayer.Release //for proper respawn
                tplayer.Zone = inZone
                tplayer
            }
          )
          avatarActor ! AvatarActor.ReplaceAvatar(a)
          avatarActor ! AvatarActor.LoginAvatar(context.self)

        case _ =>
          //fall back to sanctuary/prior?
          log.error(s"LoginInfo: player $playerName could not be found in game world")
          self.forward(PlayerToken.LoginInfo(playerName, Zone.Nowhere, pos))
      }

    case PlayerToken.CanNotLogin(playerName, reason) =>
      log.warn(s"LoginInfo: player $playerName is denied login for reason: $reason")
      reason match {
        case PlayerToken.DeniedLoginReason.Kicked => KickedByAdministration()
        case _                                    => sendResponse(DisconnectMessage("You will be logged out."))
      }

    case msg @ Containable.ItemPutInSlot(
          _: PlanetSideServerObject with Container,
          _: Equipment,
          _: Int,
          _: Option[Equipment]
        ) =>
      log.info(s"$msg")

    case msg @ Containable.CanNotPutItemInSlot(_: PlanetSideServerObject with Container, _: Equipment, _: Int) =>
      log.info(s"$msg")

    case default =>
      log.warn(s"Invalid packet class received: $default from ${sender()}")
  }

  /**
    * Update this player avatar for persistence.
    * @param persistRef reference to the persistence monitor
    */
  def UpdatePersistence(persistRef: ActorRef)(): Unit = {
    persistRef ! AccountPersistenceService.Update(player.Name, continent, player.Position)
  }

  /**
    * Do not update this player avatar for persistence.
    */
  def NoPersistence(): Unit = {}

  /**
    * A zoning message was received.
    * That doesn't matter.
    * In what stage of the zoning determination process is the client, and what is the next stage.<br>
    * <br>
    * To perform any actions involving zoning, an initial request must have been dispatched and marked as dispatched.
    * When invoked after, the process will switch over to a countdown of time until the zoning actually occurs.
    * The origin will be evaluated based on comparison of faction affinity with the client's player
    * and from that an initial time and a message will be generated.
    * Afterwards, the process will queue another inquiry for another zoning response.
    * Each time 5s of the countdown passes, another message will be sent and received;
    * and, this is another pass of the countdown.<br>
    * <br>
    * Once the countdown reaches 0, the transportation that has been promised by the zoning attempt may begin.
    *
    * @param runnable execute for the next step of the zoning process
    */
  def beginZoningCountdown(runnable: Runnable): Unit = {
    val descriptor = zoningType.toString.toLowerCase
    if (zoningStatus == Zoning.Status.Request) {
      avatarActor ! AvatarActor.DeinitializeImplants()
      zoningStatus = Zoning.Status.Countdown
      val (time, origin) = ZoningStartInitialMessageAndTimer()
      zoningCounter = time
      sendResponse(ChatMsg(ChatMessageType.CMT_QUIT, false, "", s"@${descriptor}_$origin", None))
      zoningTimer.cancel()
      zoningTimer = context.system.scheduler.scheduleOnce(5 seconds) {
        beginZoningCountdown(runnable)
      }
    } else if (zoningStatus == Zoning.Status.Countdown) {
      zoningCounter -= 5
      zoningTimer.cancel()
      if (zoningCounter > 0) {
        if (zoningCountdownMessages.contains(zoningCounter)) {
          sendResponse(ChatMsg(zoningChatMessageType, false, "", s"@${descriptor}_$zoningCounter", None))
        }
        zoningTimer = context.system.scheduler.scheduleOnce(5 seconds) {
          beginZoningCountdown(runnable)
        }
      } else {
        zoningCounter = 0
        //zoning deployment
        runnable.run()
      }
    }
  }

  /**
    * The primary method of determination involves the faction affinity of the most favorable available region subset,
    * e.g., in the overlapping sphere of influences of a friendly field tower and an enemy major facility,
    * the time representative of the the tower has priority.
    * When no spheres of influence are being encroached, one is considered "in the wilderness".
    * The messaging is different but the location is normally treated the same as if in a neutral sphere of influence.
    * Being anywhere in one's faction's own sanctuary is a special case.
    * @return a `Tuple` composed of the initial countdown time and the descriptor for message composition
    */
  def ZoningStartInitialMessageAndTimer(): (Int, String) = {
    val location = if (Zones.sanctuaryZoneNumber(player.Faction) == continent.Number) {
      Zoning.Time.Sanctuary
    } else {
      val playerPosition = player.Position.xy
      continent.Buildings.values
        .filter { building =>
          val radius = building.Definition.SOIRadius
          Vector3.DistanceSquared(building.Position.xy, playerPosition) < radius * radius
        } match {
        case Nil =>
          Zoning.Time.None
        case List(building: FactionAffinity) =>
          if (building.Faction == player.Faction) Zoning.Time.Friendly
          else if (building.Faction == PlanetSideEmpire.NEUTRAL) Zoning.Time.Neutral
          else Zoning.Time.Enemy
        case buildings =>
          if (buildings.exists(_.Faction == player.Faction)) Zoning.Time.Friendly
          else if (buildings.exists(_.Faction == PlanetSideEmpire.NEUTRAL)) Zoning.Time.Neutral
          else Zoning.Time.Enemy
      }
    }
    (location.id, location.descriptor.toLowerCase)
  }

  /**
    * You can't instant action to respond to some activity using a droppod!
    * You can't.
    * You just can't.
    * @param zone            the destination zone
    * @param hotspotPosition where is the hotspot that is being addressed
    * @param spawnPosition   the destination spawn position (may not be related to a literal `SpawnPoint` entity)
    */
  def YouCantInstantActionUsingDroppod(zone: Zone, hotspotPosition: Vector3, spawnPosition: Vector3): Unit = {
    CancelZoningProcess()
    PlayerActionsToCancel()
    CancelAllProximityUnits()
    //find a safe drop point
    var targetBuildings = zone.Buildings.values
    var whereToDroppod  = spawnPosition.xy
    while (targetBuildings.nonEmpty) {
      (targetBuildings
        .filter { building =>
          val radius = building.Definition.SOIRadius
          Vector3.DistanceSquared(building.Position.xy, whereToDroppod) < radius * radius
        }) match {
        case Nil =>
          //no soi interference
          targetBuildings = Nil
        case List(building: Building) =>
          //blocked by a single soi; find space just outside of this soi and confirm no new overlap
          val radius = Vector3(0, building.Definition.SOIRadius.toFloat + 5f, 0)
          whereToDroppod =
            building.Position.xy + Vector3.Rz(radius, math.abs(scala.util.Random.nextInt() % 360).toFloat)
        case buildings =>
          //probably blocked by a facility and its tower (maximum overlap potential is 2?); find space outside of largest soi
          val largestBuilding = buildings.maxBy(_.Definition.SOIRadius)
          val radius          = Vector3(0, largestBuilding.Definition.SOIRadius.toFloat + 5f, 0)
          whereToDroppod =
            largestBuilding.Position.xy + Vector3.Rz(radius, math.abs(scala.util.Random.nextInt() % 360).toFloat)
          targetBuildings = buildings
      }
    }
    //droppod action
    val droppod = Vehicle(GlobalDefinitions.droppod)
    droppod.Faction = player.Faction
    droppod.Position = whereToDroppod.xy + Vector3.z(1024)
    droppod.Orientation = Vector3.z(180) //you always seems to land looking south; don't know why
    droppod.Seats(0).Occupant = player
    droppod.GUID = PlanetSideGUID(0)  //droppod is not registered, we must jury-rig this
    droppod.Invalidate()              //now, we must short-circuit the jury-rig
    interstellarFerry = Some(droppod) //leverage vehicle gating
    player.Position = droppod.Position
    LoadZonePhysicalSpawnPoint(zone.id, droppod.Position, Vector3.Zero, 0 seconds)
    /* Don't even think about it. */
  }

  /**
    * The user no longer expects to perform a zoning event for this reason.
    * @param msg the message to the user
    */
  def CancelZoningProcessWithDescriptiveReason(msg: String): Unit = {
    CancelZoningProcessWithReason(s"@${zoningType.toString.toLowerCase}_$msg", Some(zoningChatMessageType))
  }

  /**
    * The user no longer expects to perform a zoning event for this reason.
    *
    * @param msg     the message to the user
    * @param msgType the type of message, influencing how it is presented to the user;
    *                normally, this message uses the same value as `zoningChatMessageType`s
    *                defaults to `None`
    */
  def CancelZoningProcessWithReason(msg: String, msgType: Option[ChatMessageType] = None): Unit = {
    if (zoningStatus > Zoning.Status.None) {
      sendResponse(ChatMsg(msgType.getOrElse(zoningChatMessageType), false, "", msg, None))
    }
    CancelZoningProcess()
  }

  /**
    * The user no longer expects to perform a zoning event,
    * or the process is merely resetting its internal state.
    */
  def CancelZoningProcess(): Unit = {
    zoningTimer.cancel()
    zoningType = Zoning.Method.None
    zoningStatus = Zoning.Status.None
    zoningCounter = 0
    //instant action exclusive field
    instantActionFallbackDestination = None
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleAvatarServiceResponse(toChannel: String, guid: PlanetSideGUID, reply: AvatarResponse.Response): Unit = {
    val tplayer_guid =
      if (player.HasGUID) player.GUID
      else PlanetSideGUID(0)
    reply match {
      case AvatarResponse.TeardownConnection() =>
        log.info("ending session by event system request (relog)")
        context.stop(self)

      case AvatarResponse.SendResponse(msg) =>
        sendResponse(msg)

      case AvatarResponse.SendResponseTargeted(target_guid, msg) =>
        if (tplayer_guid == target_guid) {
          sendResponse(msg)
        }

      case AvatarResponse.Revive(target_guid) =>
        if (tplayer_guid == target_guid) {
          reviveTimer.cancel()
          deadState = DeadState.Alive
          player.Revive
          val health = player.Health
          sendResponse(PlanetsideAttributeMessage(target_guid, 0, health))
          sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, player.Position, player.Faction, true))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttributeToAll(target_guid, 0, health)
          )
        }

      case AvatarResponse.ArmorChanged(suit, subtype) =>
        if (tplayer_guid != guid) {
          sendResponse(ArmorChangedMessage(guid, suit, subtype))
        }

      case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDetachMessage(weapon_guid, previous_guid, Vector3.Zero, 0))
          sendResponse(
            ObjectCreateMessage(
              ammo_id,
              ammo_guid,
              ObjectCreateMessageParent(weapon_guid, weapon_slot),
              ammo_data
            )
          )
          sendResponse(ChangeAmmoMessage(weapon_guid, 1))
        }

      case AvatarResponse.ChangeFireMode(item_guid, mode) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireModeMessage(item_guid, mode))
        }

      case AvatarResponse.ChangeFireState_Start(weapon_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Start(weapon_guid))
        }

      case AvatarResponse.ChangeFireState_Stop(weapon_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
        }

      case AvatarResponse.ConcealPlayer() =>
        sendResponse(GenericObjectActionMessage(guid, 9))

      case AvatarResponse.EnvironmentalDamage(target, source, amount) =>
        if (player.isAlive && amount > 0) {
          val playerGUID     = player.GUID
          val armor          = player.Armor
          val capacitor      = player.Capacitor
          val originalHealth = player.Health
          //history
          continent.GUID(source) match {
            case Some(obj: Painbox) =>
              player.History(DamageFromPainbox(PlayerSource(player), obj, amount))
            case _ => ;
          }
          CancelZoningProcessWithDescriptiveReason("cancel_dmg")
          player.Health = originalHealth - amount
          sendResponse(PlanetsideAttributeMessage(target, 0, player.Health))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttribute(target, 0, player.Health)
          )
          damageLog.info(
            s"${player.Name}-infantry: BEFORE=$originalHealth/$armor/$capacitor, AFTER=${player.Health}/$armor/$capacitor, CHANGE=$amount/0/0"
          )
          if (player.Health == 0 && player.isAlive) {
            player.Actor ! Player.Die()
          }
        }

      case AvatarResponse.Destroy(victim, killer, weapon, pos) =>
        // guid = victim // killer = killer ;)
        sendResponse(DestroyMessage(victim, killer, weapon, pos))

      case AvatarResponse.DestroyDisplay(killer, victim, method, unk) =>
        sendResponse(DestroyDisplayMessage(killer, victim, method, unk))
        // TODO Temporary thing that should go somewhere else and use proper xp values
        if (killer.CharId == avatar.id && killer.Faction != victim.Faction) {
          avatarActor ! AvatarActor.AwardBep((1000 * Config.app.game.bepRate).toLong)
          avatarActor ! AvatarActor.AwardCep((100 * Config.app.game.cepRate).toLong)
        }

      case AvatarResponse.DropItem(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.EquipmentInHand(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.GenericObjectAction(object_guid, action_code) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(object_guid, action_code))
        }

      case AvatarResponse.HitHint(source_guid) =>
        if (player.isAlive) {
          sendResponse(HitHint(source_guid, guid))
          CancelZoningProcessWithDescriptiveReason("cancel_dmg")
        }

      case AvatarResponse.Killed(mount) =>
        val respawnTimer = 300.seconds
        //drop free hand item
        player.FreeHand.Equipment match {
          case Some(item) =>
            DropEquipmentFromInventory(player)(item)
          case None => ;
        }
        ToggleMaxSpecialState(enable = false)
        keepAliveFunc = NormalKeepAlive
        zoningStatus = Zoning.Status.None
        deadState = DeadState.Dead
        continent.GUID(mount) match {
          case Some(obj: Vehicle) =>
            TotalDriverVehicleControl(obj)
            UnaccessContainer(obj)
          case _ => ;
        }
        PlayerActionsToCancel()
        CancelAllProximityUnits()
        CancelZoningProcessWithDescriptiveReason("cancel")
        if (shotsWhileDead > 0) {
          log.warn(
            s"KillPlayer/SHOTS_WHILE_DEAD: client of ${avatar.name} fired $shotsWhileDead rounds while character was dead on server"
          )
          shotsWhileDead = 0
        }
        reviveTimer.cancel()
        if (player.death_by == 0) {
          reviveTimer = context.system.scheduler.scheduleOnce(respawnTimer) {
            cluster ! InterstellarClusterService.GetRandomSpawnPoint(
              Zones.sanctuaryZoneNumber(player.Faction),
              player.Faction,
              Seq(SpawnGroup.Sanctuary),
              context.self
            )
          }

        } else {
          HandleReleaseAvatar(player, continent)
        }

      case AvatarResponse.LoadPlayer(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.LoadProjectile(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.ObjectDelete(item_guid, unk) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDeleteMessage(item_guid, unk))
        }

      case AvatarResponse.ObjectHeld(slot) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectHeldMessage(guid, slot, false))
        }

      case AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
        }

      case AvatarResponse.PlanetsideAttributeToAll(attribute_type, attribute_value) =>
        sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))

      case AvatarResponse.PlanetsideAttributeSelf(attribute_type, attribute_value) =>
        if (tplayer_guid == guid) {
          sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
        }

      case AvatarResponse.PlayerState(
            pos,
            vel,
            yaw,
            pitch,
            yaw_upper,
            seq_time,
            is_crouching,
            is_jumping,
            jump_thrust,
            is_cloaking,
            spectating,
            weaponInHand
          ) =>
        if (tplayer_guid != guid) {
          val now = System.currentTimeMillis()
          val (location, time, distanceSq): (Vector3, Long, Float) = if (spectating) {
            val r  = new scala.util.Random
            val r1 = 2 + r.nextInt(30).toFloat
            val r2 = 2 + r.nextInt(4000).toFloat
            (Vector3(r2, r2, r1), 0L, 0f)
          } else {
            val before = player.lastSeenStreamMessage(guid.guid)
            val dist   = Vector3.DistanceSquared(player.Position, pos)
            (pos, now - before, dist)
          }
          if (distanceSq < 302500 || time > 5000) { // Render distance seems to be approx 525m. Reduce update rate at ~550m to be safe
            sendResponse(
              PlayerStateMessage(
                guid,
                location,
                vel,
                yaw,
                pitch,
                yaw_upper,
                timestamp = 0,
                is_crouching,
                is_jumping,
                jump_thrust,
                is_cloaking
              )
            )
            player.lastSeenStreamMessage(guid.guid) = now
          }
        }

      case AvatarResponse.ProjectileExplodes(projectile_guid, projectile) =>
        sendResponse(
          ProjectileStateMessage(
            projectile_guid,
            projectile.Position,
            Vector3.Zero,
            projectile.Orientation,
            0,
            true,
            PlanetSideGUID(0)
          )
        )
        sendResponse(ObjectDeleteMessage(projectile_guid, 2))

      case AvatarResponse.ProjectileAutoLockAwareness(mode) =>
        sendResponse(GenericActionMessage(mode))

      case AvatarResponse.ProjectileState(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ProjectileStateMessage(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid))
        }

      case AvatarResponse.PutDownFDU(target) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(target, 53))
        }

      case AvatarResponse.Release(tplayer) =>
        if (tplayer_guid != guid) {
          DepictPlayerAsCorpse(tplayer)
        }

      case AvatarResponse.Reload(item_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(ReloadMessage(item_guid, 1, 0))
        }

      case AvatarResponse.SetEmpire(object_guid, faction) =>
        if (tplayer_guid != guid) {
          sendResponse(SetEmpireMessage(object_guid, faction))
        }

      case AvatarResponse.StowEquipment(target, slot, item) =>
        if (tplayer_guid != guid) {
          val definition = item.Definition
          sendResponse(
            ObjectCreateDetailedMessage(
              definition.ObjectId,
              item.GUID,
              ObjectCreateMessageParent(target, slot),
              definition.Packet.DetailedConstructorData(item).get
            )
          )
        }

      case AvatarResponse.WeaponDryFire(weapon_guid) =>
        if (tplayer_guid != guid) {
          // Check that the magazine is still empty before sending WeaponDryFireMessage
          // As it could have been reloaded since the packet was dispatched, which would make other clients not see it firing
          continent.GUID(weapon_guid) match {
            case Some(tool: Tool) => {
              if (tool.Magazine == 0) {
                sendResponse(WeaponDryFireMessage(weapon_guid))
              }
            }
            case _ => log.warn(s"Tried to send WeaponDryFire but GUID ${weapon_guid} does not seem to be a Tool")
          }
        }

      case AvatarResponse.TerminalOrderResult(terminal_guid, action, result) =>
        sendResponse(ItemTransactionResultMessage(terminal_guid, action, result))
        lastTerminalOrderFulfillment = true

      case AvatarResponse.ChangeExosuit(
            target,
            armor,
            exosuit,
            subtype,
            slot,
            maxhand,
            old_holsters,
            holsters,
            old_inventory,
            inventory,
            drop,
            delete
          ) =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, 4, armor))
        if (tplayer_guid == target) {
          //happening to this player
          //cleanup
          sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, false))
          (old_holsters ++ old_inventory ++ delete).foreach {
            case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0))
          }
          //functionally delete
          delete.foreach { case (obj, _) => continent.tasks ! GUIDTask.UnregisterEquipment(obj)(continent.GUID) }
          //redraw
          if (maxhand) {
            continent.tasks ! HoldNewEquipmentUp(player)(
              Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
              0
            )
          }
          //draw free hand
          player.FreeHand.Equipment match {
            case Some(obj) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, Player.FreeHandSlot),
                  definition.Packet.DetailedConstructorData(obj).get
                )
              )
            case None => ;
          }
          //draw holsters and inventory
          (holsters ++ inventory).foreach {
            case InventoryItem(obj, index) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, index),
                  definition.Packet.DetailedConstructorData(obj).get
                )
              )
          }
          DropLeftovers(player)(drop)
        } else {
          //happening to some other player
          sendResponse(ObjectHeldMessage(target, slot, false))
          //cleanup
          (old_holsters ++ delete).foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
          //draw holsters
          holsters.foreach {
            case InventoryItem(obj, index) =>
              val definition = obj.Definition
              sendResponse(
                ObjectCreateMessage(
                  definition.ObjectId,
                  obj.GUID,
                  ObjectCreateMessageParent(target, index),
                  definition.Packet.ConstructorData(obj).get
                )
              )
          }
        }

      case AvatarResponse.ChangeLoadout(
            target,
            armor,
            exosuit,
            subtype,
            slot,
            maxhand,
            old_holsters,
            holsters,
            old_inventory,
            inventory,
            drops
          ) =>
        sendResponse(ArmorChangedMessage(target, exosuit, subtype))
        sendResponse(PlanetsideAttributeMessage(target, 4, armor))
        if (tplayer_guid == target) {
          //happening to this player
          sendResponse(ObjectHeldMessage(target, Player.HandsDownSlot, false))
          //cleanup
          (old_holsters ++ old_inventory).foreach {
            case (obj, guid) =>
              sendResponse(ObjectDeleteMessage(guid, 0))
              continent.tasks ! GUIDTask.UnregisterEquipment(obj)(continent.GUID)
          }
          //redraw
          if (maxhand) {
            continent.tasks ! HoldNewEquipmentUp(player)(
              Tool(GlobalDefinitions.MAXArms(subtype, player.Faction)),
              0
            )
          }
          ApplyPurchaseTimersBeforePackingLoadout(player, player, holsters ++ inventory)
          DropLeftovers(player)(drops)
        } else {
          //happening to some other player
          sendResponse(ObjectHeldMessage(target, slot, false))
          //cleanup
          old_holsters.foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
          //redraw handled by callback
        }

      case _ => ;
    }
  }

  /**
    * Enforce constraints on bulk purchases as determined by a given player's previous purchase times and hard acquisition delays.
    * Intended to assist in sanitizing loadout information from the perspectvie of the player, or target owner.
    * The equipment is expected to be unregistered and already fitted to their ultimate slot in the target container.
    * @param player the player whose purchasing constraints are to be tested
    * @param target the location in which the equipment will be stowed
    * @param slots the equipment, in the standard object-slot format container
    */
  def ApplyPurchaseTimersBeforePackingLoadout(
      player: Player,
      target: PlanetSideServerObject with Container,
      slots: List[InventoryItem]
  ): Unit = {
    slots.foreach { item =>
      player.avatar.purchaseCooldown(item.obj.Definition) match {
        case Some(_) => ;
        case None =>
          if (Avatar.purchaseCooldowns.contains(item.obj.Definition)) {
            avatarActor ! AvatarActor.UpdatePurchaseTime(item.obj.Definition)
          }
          continent.tasks ! PutLoadoutEquipmentInInventory(target)(item.obj, item.start)
      }
    }
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleLocalServiceResponse(toChannel: String, guid: PlanetSideGUID, reply: LocalResponse.Response): Unit = {
    val tplayer_guid =
      if (player.HasGUID) player.GUID
      else PlanetSideGUID(0)
    reply match {
      case LocalResponse.AlertDestroyDeployable(obj) =>
        //the (former) owner (obj.OwnerName) should process this message
        avatar.deployables.Remove(obj)
        UpdateDeployableUIElements(avatar.deployables.UpdateUIElement(obj.Definition.Item))

      case LocalResponse.DeployableMapIcon(behavior, deployInfo) =>
        if (tplayer_guid != guid) {
          sendResponse(DeployableObjectsInfoMessage(behavior, deployInfo))
        }

      case LocalResponse.Detonate(guid, obj: BoomerDeployable) =>
        sendResponse(TriggerEffectMessage(guid, "detonate_boomer"))
        sendResponse(PlanetsideAttributeMessage(guid, 29, 1))
        sendResponse(ObjectDeleteMessage(guid, 0))

      case LocalResponse.Detonate(guid, obj: ExplosiveDeployable) =>
        sendResponse(GenericObjectActionMessage(guid, 19))
        sendResponse(PlanetsideAttributeMessage(guid, 29, 1))
        sendResponse(ObjectDeleteMessage(guid, 0))

      case LocalResponse.Detonate(guid, obj) =>
        log.warn(s"LocalResponse.Detonate: ${obj.Definition.Name} not configured to explode correctly")

      case LocalResponse.DoorOpens(door_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectStateMsg(door_guid, 16))
        }

      case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
        sendResponse(GenericObjectStateMsg(door_guid, 17))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, guid, pos) =>
        if (obj.Destroyed) {
          DeconstructDeployable(obj, guid, pos)
        } else {
          obj.Destroyed = true
          DeconstructDeployable(
            obj,
            guid,
            pos,
            obj.Orientation,
            if (obj.MountPoints.isEmpty) 2
            else 1
          )
        }

      case LocalResponse.EliminateDeployable(obj: ExplosiveDeployable, guid, pos) =>
        if (obj.Destroyed || obj.Jammed || obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.EliminateDeployable(obj: ComplexDeployable, guid, pos) =>
        if (obj.Destroyed) {
          DeconstructDeployable(obj, guid, pos)
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 1)
        }

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, guid, pos) =>
        //if active, deactivate
        if (obj.Active) {
          obj.Active = false
          sendResponse(GenericObjectActionMessage(guid, 29))
          sendResponse(GenericObjectActionMessage(guid, 30))
        }
        //determine if no replacement teleport system exists
        continent.GUID(obj.Router) match {
          case Some(router: Vehicle) =>
            //if the telepad was replaced, the new system is physically in place but not yet functional
            if (
              router.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(internalTelepad: Utility.InternalTelepad) =>
                  internalTelepad.Telepad.contains(guid) //same telepad
                case _ => true
              }
            ) {
              //there is no replacement telepad; shut down the system
              ToggleTeleportSystem(router, None)
            }
          case _ => ;
        }
        //standard deployable elimination behavior
        if (obj.Destroyed) {
          DeconstructDeployable(obj, guid, pos)
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.EliminateDeployable(obj, guid, pos) =>
        if (obj.Destroyed) {
          DeconstructDeployable(obj, guid, pos)
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.HackClear(target_guid, unk1, unk2) =>
        log.trace(s"Clearing hack for ${target_guid}")
        // Reset hack state for all players
        sendResponse(HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2))

      case LocalResponse.HackObject(target_guid, unk1, unk2) =>
        HackObject(target_guid, unk1, unk2)

      case LocalResponse.HackCaptureTerminal(target_guid, unk1, unk2, isResecured) =>
        HackCaptureTerminal(target_guid, unk1, unk2, isResecured)

      case LocalResponse.ObjectDelete(object_guid, unk) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDeleteMessage(object_guid, unk))
        }

      case LocalResponse.ProximityTerminalEffect(object_guid, true) =>
        sendResponse(ProximityTerminalUseMessage(PlanetSideGUID(0), object_guid, true))

      case LocalResponse.ProximityTerminalEffect(object_guid, false) =>
        sendResponse(ProximityTerminalUseMessage(PlanetSideGUID(0), object_guid, false))
        ForgetAllProximityTerminals(object_guid)

      case LocalResponse.RouterTelepadMessage(msg) =>
        sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", msg, None))

      case LocalResponse.RouterTelepadTransport(passenger_guid, src_guid, dest_guid) =>
        UseRouterTelepadEffect(passenger_guid, src_guid, dest_guid)

      case LocalResponse.SetEmpire(object_guid, empire) =>
        sendResponse(SetEmpireMessage(object_guid, empire))

      case LocalResponse.ToggleTeleportSystem(router, system_plan) =>
        ToggleTeleportSystem(router, system_plan)

      case LocalResponse.TriggerEffect(target_guid, effect, effectInfo, triggerLocation) =>
        sendResponse(TriggerEffectMessage(target_guid, effect, effectInfo, triggerLocation))

      case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
        sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

      case LocalResponse.UpdateForceDomeStatus(building_guid, activated) => {
        if (activated) {
          sendResponse(GenericObjectActionMessage(building_guid, 11))
        } else {
          sendResponse(GenericObjectActionMessage(building_guid, 12))
        }
      }

      case LocalResponse.RechargeVehicleWeapon(vehicle_guid, weapon_guid) => {
        if (tplayer_guid == guid) {
          continent.GUID(vehicle_guid) match {
            case Some(vehicle: Mountable with MountedWeapons) =>
              vehicle.PassengerInSeat(player) match {
                case Some(seat_num: Int) =>
                  vehicle.WeaponControlledFromSeat(seat_num) match {
                    case Some(equipment) if equipment.GUID == weapon_guid =>
                      val weapon = equipment.asInstanceOf[Tool]
                      sendResponse(InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, weapon.Magazine))
                    case _ => ;
                  }
                case _ => ;
              }
            case _ => ;
          }
        }
      }

      case _ => ;
    }
  }

  /**
    * na
    * @param tplayer na
    * @param reply   na
    */
  def HandleMountMessages(tplayer: Player, reply: Mountable.Exchange): Unit = {
    reply match {
      case Mountable.CanMount(obj: ImplantTerminalMech, seat_num) =>
        CancelZoningProcessWithDescriptiveReason("cancel_use")
        CancelAllProximityUnits()
        MountingAction(tplayer, obj, seat_num)
        // the player will receive no messages consistently except the KeepAliveMessage echo
        keepAliveFunc = KeepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seat_num) =>
        CancelZoningProcessWithDescriptiveReason("cancel_mount")
        val obj_guid: PlanetSideGUID = obj.GUID
        log.info(s"MountVehicleMsg: ${player.Name}_guid mounts $obj_guid @ $seat_num")
        CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, 0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, 68, obj.Shields)) //shield health
        if (obj.Definition == GlobalDefinitions.ant) {
          sendResponse(PlanetsideAttributeMessage(obj_guid, 45, obj.NtuCapacitorScaled))
        }
        if (obj.Definition.MaxCapacitor > 0) {
          val capacitor = scala.math.ceil((obj.Capacitor.toFloat / obj.Definition.MaxCapacitor.toFloat) * 10).toInt
          sendResponse(PlanetsideAttributeMessage(obj_guid, 113, capacitor))
        }
        if (seat_num == 0) {
          if (obj.Definition == GlobalDefinitions.quadstealth) {
            //wraith cloak state matches the cloak state of the driver
            //phantasm doesn't uncloak if the driver is uncloaked and no other vehicle cloaks
            obj.Cloaked = tplayer.Cloaked
          }
        } else if (obj.Seats(seat_num).ControlledWeapon.isEmpty) {
          // the player will receive no messages consistently except the KeepAliveMessage echo
          keepAliveFunc = KeepAlivePersistence
        }
        AccessContainer(obj)
        UpdateWeaponAtSeatPosition(obj, seat_num)
        MountingAction(tplayer, obj, seat_num)

      case Mountable.CanMount(obj: FacilityTurret, seat_num) =>
        CancelZoningProcessWithDescriptiveReason("cancel_mount")
        if (!obj.isUpgrading) {
          if (obj.Definition == GlobalDefinitions.vanu_sentry_turret) {
            obj.Zone.LocalEvents ! LocalServiceMessage(obj.Zone.id, LocalAction.SetEmpire(obj.GUID, player.Faction))
          }
          sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
          UpdateWeaponAtSeatPosition(obj, seat_num)
          MountingAction(tplayer, obj, seat_num)
          // the player will receive no messages consistently except the KeepAliveMessage echo
          keepAliveFunc = KeepAlivePersistence
        } else {
          log.warn(
            s"MountVehicleMsg: ${tplayer.Name} wants to mount turret ${obj.GUID.guid}, but needs to wait until it finishes updating"
          )
        }

      case Mountable.CanMount(obj: PlanetSideGameObject with WeaponTurret, seat_num) =>
        CancelZoningProcessWithDescriptiveReason("cancel_mount")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
        UpdateWeaponAtSeatPosition(obj, seat_num)
        MountingAction(tplayer, obj, seat_num)
        // the player will receive no messages consistently except the KeepAliveMessage echo
        keepAliveFunc = KeepAlivePersistence

      case Mountable.CanMount(obj: Mountable, _) =>
        log.warn(s"MountVehicleMsg: $obj is some generic mountable object and nothing will happen")

      case Mountable.CanDismount(obj: ImplantTerminalMech, seat_num) =>
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj: Vehicle, seat_num) if obj.Definition == GlobalDefinitions.droppod =>
        UnaccessContainer(obj)
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj: Vehicle, seat_num) =>
        val player_guid: PlanetSideGUID = tplayer.GUID
        if (player_guid == player.GUID) {
          //disembarking self
          TotalDriverVehicleControl(obj)
          UnaccessContainer(obj)
          DismountAction(tplayer, obj, seat_num)
        } else {
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.KickPassenger(player_guid, seat_num, true, obj.GUID)
          )
        }

      case Mountable.CanDismount(obj: PlanetSideGameObject with WeaponTurret, seat_num) =>
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj: Mountable, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some generic mountable object and nothing will happen")

      case Mountable.CanNotMount(obj: Vehicle, seat_num) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seat_num, but was not allowed")
        if (obj.SeatPermissionGroup(seat_num).contains(AccessPermissionGroup.Driver)) {
          sendResponse(
            ChatMsg(ChatMessageType.CMT_OPEN, false, "", "You are not the driver of this vehicle.", None)
          )
        }

      case Mountable.CanNotMount(obj: Mountable, seat_num) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seat_num, but was not allowed")

      case Mountable.CanNotDismount(obj, seat_num) =>
        log.warn(
          s"DismountVehicleMsg: ${tplayer.Name} attempted to dismount $obj's seat $seat_num, but was not allowed"
        )
    }
  }

  /**
    * na
    * @param tplayer na
    * @param msg     na
    * @param order   na
    */
  def HandleTerminalMessage(tplayer: Player, msg: ItemTransactionMessage, order: Terminal.Exchange): Unit = {
    order match {
      case Terminal.BuyEquipment(item) =>
        tplayer.avatar.purchaseCooldown(item.Definition) match {
          case Some(_) =>
            lastTerminalOrderFulfillment = true
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, false))
          case None =>
            avatarActor ! AvatarActor.UpdatePurchaseTime(item.Definition)
            continent.tasks ! BuyNewEquipmentPutInInventory(
              continent.GUID(tplayer.VehicleSeated) match { case Some(v: Vehicle) => v; case _ => player },
              tplayer,
              msg.terminal_guid
            )(item)
        }

      case Terminal.SellEquipment() =>
        SellEquipmentFromInventory(tplayer, tplayer, msg.terminal_guid)(Player.FreeHandSlot)

      case Terminal.LearnCertification(cert) =>
        avatarActor ! AvatarActor.LearnCertification(msg.terminal_guid, cert)
        lastTerminalOrderFulfillment = true

      case Terminal.SellCertification(cert) =>
        avatarActor ! AvatarActor.SellCertification(msg.terminal_guid, cert)
        lastTerminalOrderFulfillment = true

      case Terminal.LearnImplant(implant) =>
        avatarActor ! AvatarActor.LearnImplant(msg.terminal_guid, implant)
        lastTerminalOrderFulfillment = true

      case Terminal.SellImplant(implant) =>
        avatarActor ! AvatarActor.SellImplant(msg.terminal_guid, implant)
        lastTerminalOrderFulfillment = true

      case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
        continent.map.terminalToSpawnPad.get(msg.terminal_guid.guid) match {
          case Some(padGuid) =>
            tplayer.avatar.purchaseCooldown(vehicle.Definition) match {
              case Some(_) =>
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
              case None =>
                avatarActor ! AvatarActor.UpdatePurchaseTime(vehicle.Definition)
                Avatar.purchaseCooldowns.get(vehicle.Definition) match {
                  case Some(cooldown) =>
                    sendResponse(
                      AvatarVehicleTimerMessage(tplayer.GUID, vehicle.Definition.Name, cooldown.toSeconds, unk1 = true)
                    )
                  case None => ;
                }
                val pad = continent.GUID(padGuid).get.asInstanceOf[VehicleSpawnPad]
                vehicle.Faction = tplayer.Faction
                vehicle.Position = pad.Position
                vehicle.Orientation = pad.Orientation + Vector3.z(pad.Definition.VehicleCreationZOrientOffset)
                //default loadout, weapons
                val vWeapons = vehicle.Weapons
                weapons.foreach(entry => {
                  vWeapons.get(entry.start) match {
                    case Some(slot) =>
                      entry.obj.Faction = tplayer.Faction
                      slot.Equipment = None
                      slot.Equipment = entry.obj
                    case None =>
                      log.warn(
                        s"applying default loadout to $vehicle on spawn, but can not find a mounted weapon @ ${entry.start}"
                      )
                  }
                })
                //default loadout, trunk
                val vTrunk = vehicle.Trunk
                vTrunk.Clear()
                trunk.foreach(entry => {
                  entry.obj.Faction = tplayer.Faction
                  vTrunk.InsertQuickly(entry.start, entry.obj)
                })
                continent.tasks ! RegisterVehicleFromSpawnPad(vehicle, pad)
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
            }
          case None =>
            log.error(
              s"${tplayer.Name} wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it"
            )
        }
        lastTerminalOrderFulfillment = true

      case Terminal.NoDeal() =>
        val order: String = if (msg == null) {
          s"order $msg"
        } else {
          "missing order"
        }
        log.warn(s"${tplayer.Name} made a request but the terminal rejected the $order")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false))
        lastTerminalOrderFulfillment = true

      case _ => ;
    }
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleVehicleServiceResponse(toChannel: String, guid: PlanetSideGUID, reply: VehicleResponse.Response): Unit = {
    val tplayer_guid = if (player.HasGUID) player.GUID else PlanetSideGUID(0)
    reply match {
      case VehicleResponse.AttachToRails(vehicle_guid, pad_guid) =>
        sendResponse(ObjectAttachMessage(pad_guid, vehicle_guid, 3))

      case VehicleResponse.ChildObjectState(object_guid, pitch, yaw) =>
        if (tplayer_guid != guid) {
          sendResponse(ChildObjectStateMessage(object_guid, pitch, yaw))
        }

      case VehicleResponse.ConcealPlayer(player_guid) =>
        sendResponse(GenericObjectActionMessage(player_guid, 9))

      case VehicleResponse.DismountVehicle(bailType, wasKickedByDriver) =>
        if (tplayer_guid != guid) {
          sendResponse(DismountVehicleMsg(guid, bailType, wasKickedByDriver))
        }

      case VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos) =>
        if (tplayer_guid != guid) {
          sendResponse(DeployRequestMessage(guid, object_guid, state, unk1, unk2, pos))
        }

      case VehicleResponse.DetachFromRails(vehicle_guid, pad_guid, pad_position, pad_orientation_z) =>
        val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad].Definition
        sendResponse(
          ObjectDetachMessage(
            pad_guid,
            vehicle_guid,
            pad_position + Vector3(0, 0, pad.VehicleCreationZOffset),
            pad_orientation_z + pad.VehicleCreationZOrientOffset
          )
        )

      case VehicleResponse.EquipmentInSlot(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case VehicleResponse.HitHint(source_guid) =>
        if (player.isAlive) {
          sendResponse(HitHint(source_guid, player.GUID))
        }

      case VehicleResponse.InventoryState(obj, parent_guid, start, con_data) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
          val obj_guid = obj.GUID
          sendResponse(ObjectDeleteMessage(obj_guid, 0))
          sendResponse(
            ObjectCreateDetailedMessage(
              obj.Definition.ObjectId,
              obj_guid,
              ObjectCreateMessageParent(parent_guid, start),
              con_data
            )
          )
        }

      case VehicleResponse.KickPassenger(seat_num, wasKickedByDriver, vehicle_guid) =>
        // seat_num seems to be correct if passenger is kicked manually by driver, but always seems to return 4 if user is kicked by seat permissions
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))
        if (tplayer_guid == guid) {
          continent.GUID(vehicle_guid) match {
            case Some(obj: Vehicle) =>
              UnaccessContainer(obj)
            case _ => ;
          }
        }

      case VehicleResponse.InventoryState2(obj_guid, parent_guid, value) =>
        if (tplayer_guid != guid) {
          sendResponse(InventoryStateMessage(obj_guid, 0, parent_guid, value))
        }

      case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) =>
        //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
        if (tplayer_guid != guid) {
          sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
          Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        }

      case VehicleResponse.MountVehicle(vehicle_guid, seat) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectAttachMessage(vehicle_guid, guid, seat))
        }

      case VehicleResponse.Ownership(vehicleGuid) =>
        if (tplayer_guid == guid) { // Only the player that owns this vehicle needs the ownership packet
          avatarActor ! AvatarActor.SetVehicle(Some(vehicleGuid))
          sendResponse(PlanetsideAttributeMessage(tplayer_guid, 21, vehicleGuid))
        }

      case VehicleResponse.PlanetsideAttribute(vehicle_guid, attribute_type, attribute_value) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, attribute_type, attribute_value))
        }

      case VehicleResponse.ResetSpawnPad(pad_guid) =>
        sendResponse(GenericObjectActionMessage(pad_guid, 23))

      case VehicleResponse.RevealPlayer(player_guid) =>
        sendResponse(GenericObjectActionMessage(player_guid, 10))

      case VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, seat_group, permission))
        }

      case VehicleResponse.StowEquipment(vehicle_guid, slot, item_type, item_guid, item_data) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
          sendResponse(
            ObjectCreateDetailedMessage(item_type, item_guid, ObjectCreateMessageParent(vehicle_guid, slot), item_data)
          )
        }

      case VehicleResponse.UnloadVehicle(vehicle, vehicle_guid) =>
        sendResponse(ObjectDeleteMessage(vehicle_guid, 0))

      case VehicleResponse.UnstowEquipment(item_guid) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
          sendResponse(ObjectDeleteMessage(item_guid, 0))
        }

      case VehicleResponse.VehicleState(
            vehicle_guid,
            unk1,
            pos,
            ang,
            vel,
            unk2,
            unk3,
            unk4,
            wheel_direction,
            unk5,
            unk6
          ) =>
        if (tplayer_guid != guid) {
          sendResponse(
            VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6)
          )
          if (player.VehicleSeated.contains(vehicle_guid)) {
            player.Position = pos
          }
        }
      case VehicleResponse.SendResponse(msg) =>
        sendResponse(msg)

      case VehicleResponse.UpdateAmsSpawnPoint(list) =>
        amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
        DrawCurrentAmsSpawnPoint()

      case VehicleResponse.TransferPassengerChannel(old_channel, temp_channel, vehicle, vehicle_to_delete) =>
        if (tplayer_guid != guid) {
          interstellarFerry = Some(vehicle)
          interstellarFerryTopLevelGUID = Some(vehicle_to_delete)
          continent.VehicleEvents ! Service.Leave(
            Some(old_channel)
          )                                          //old vehicle-specific channel (was s"${vehicle.Actor}")
          galaxyService ! Service.Join(temp_channel) //temporary vehicle-specific channel
        }

      case VehicleResponse.KickCargo(vehicle, speed, delay) =>
        if (player.VehicleSeated.nonEmpty && deadState == DeadState.Alive) {
          if (speed > 0) {
            val strafe =
              if (Vehicles.CargoOrientation(vehicle) == 1) 2
              else 1
            val reverseSpeed =
              if (strafe > 1) 0
              else speed
            //strafe or reverse, not both
            controlled = Some(reverseSpeed)
            sendResponse(ServerVehicleOverrideMsg(true, true, true, false, 0, strafe, reverseSpeed, Some(0)))
            import scala.concurrent.ExecutionContext.Implicits.global
            context.system.scheduler.scheduleOnce(
              delay milliseconds,
              self,
              VehicleServiceResponse(toChannel, PlanetSideGUID(0), VehicleResponse.KickCargo(vehicle, 0, delay))
            )
          } else {
            controlled = None
            sendResponse(ServerVehicleOverrideMsg(false, false, false, false, 0, 0, 0, None))
          }
        }

      case VehicleResponse.StartPlayerSeatedInVehicle(vehicle, pad) =>
        val vehicle_guid = vehicle.GUID
        PlayerActionsToCancel()
        CancelAllProximityUnits()
        if (player.VisibleSlots.contains(player.DrawnSlot)) {
          player.DrawnSlot = Player.HandsDownSlot
          sendResponse(ObjectHeldMessage(player.GUID, Player.HandsDownSlot, true))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.ObjectHeld(player.GUID, player.LastDrawnSlot)
          )
        }
        sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 1L))          //mount points off
        sendResponse(PlanetsideAttributeMessage(player.GUID, 21, vehicle_guid)) //ownership
        vehicle.Actor ! Mountable.TryMount(player, 0)

      case VehicleResponse.PlayerSeatedInVehicle(vehicle, pad) =>
        val vehicle_guid = vehicle.GUID
        sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 0L)) //mount points on
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        ServerVehicleLock(vehicle)

      case VehicleResponse.ServerVehicleOverrideStart(vehicle, pad) =>
        val vdef = vehicle.Definition
        ServerVehicleOverride(vehicle, vdef.AutoPilotSpeed1, if (GlobalDefinitions.isFlightVehicle(vdef)) 1 else 0)

      case VehicleResponse.ServerVehicleOverrideEnd(vehicle, pad) =>
        DriverVehicleControl(vehicle, vehicle.Definition.AutoPilotSpeed2)

      case VehicleResponse.PeriodicReminder(cause, data) =>
        val msg: String = (cause match {
          case VehicleSpawnPad.Reminders.Blocked =>
            s"The vehicle spawn where you placed your order is blocked. ${data.getOrElse("")}"
          case VehicleSpawnPad.Reminders.Queue =>
            s"Your position in the vehicle spawn queue is ${data.getOrElse("last")}."
          case VehicleSpawnPad.Reminders.Cancelled =>
            "Your vehicle order has been cancelled."
        })
        sendResponse(ChatMsg(ChatMessageType.CMT_OPEN, true, "", msg, None))

      case VehicleResponse.ChangeLoadout(target, old_weapons, added_weapons, old_inventory, new_inventory) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target) match {
          case Some(vehicle: Vehicle) =>
            if (player.avatar.vehicle.contains(target)) {
              //owner: must unregister old equipment, and register and install new equipment
              (old_weapons ++ old_inventory).foreach {
                case (obj, guid) =>
                  sendResponse(ObjectDeleteMessage(guid, 0))
                  continent.tasks ! GUIDTask.UnregisterEquipment(obj)(continent.GUID)
              }
              ApplyPurchaseTimersBeforePackingLoadout(player, vehicle, added_weapons ++ new_inventory)
            } else if (accessedContainer.contains(target)) {
              //external participant: observe changes to equipment
              (old_weapons ++ old_inventory).foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
            }
            vehicle.PassengerInSeat(player) match {
              case Some(seatNum) =>
                //participant: observe changes to equipment
                (old_weapons ++ old_inventory).foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
                UpdateWeaponAtSeatPosition(vehicle, seatNum)
              case None =>
                //observer: observe changes to external equipment
                old_weapons.foreach { case (_, guid) => sendResponse(ObjectDeleteMessage(guid, 0)) }
            }
          case _ => ;
        }

      case _ => ;
    }
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier    the ferrying vehicle
    * @param cargo      the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForUs(
      carrier: Vehicle,
      cargo: Vehicle,
      mountPoint: Int
  ): (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoBehavior.CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForUs(attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param attachMessage           an `ObjectAttachMessage` packet suitable for initializing cargo operations
    * @param mountPointStatusMessage a `CargoMountPointStatusMessage` packet suitable for initializing cargo operations
    */
  def CargoMountMessagesForUs(
      attachMessage: ObjectAttachMessage,
      mountPointStatusMessage: CargoMountPointStatusMessage
  ): Unit = {
    sendResponse(attachMessage)
    sendResponse(mountPointStatusMessage)
  }

  /**
    * Handle the message that indicates the level of completion of a process.
    * The process is any form of user-driven activity with a certain eventual outcome
    * but indeterminate progress feedback per cycle.<br>
    * <br>
    * This task is broken down into the "progression" from its initial state to the eventual outcome
    * as is reported back to the player through some means of messaging window feedback.
    * Though common in practice, this is not a requirement
    * and the progress can accumulate without a user reportable method.
    * To ensure that completion is reported properly,
    * an exception is made that 99% completion is accounted uniquely
    * before the final 100% is achieved.
    * If the background process recording value is never set before running the initial operation
    * or gets unset by failing a `tickAction` check
    * the process is stopped.
    *
    * @see `progressBarUpdate`
    * @see `progressBarValue`
    * @see `WorldSessionActor.Progress`
    * @param delta            how much the progress changes each tick
    * @param completionAction a custom action performed once the process is completed
    * @param tickAction       an optional action is is performed for each tick of progress;
    *                         also performs a continuity check to determine if the process has been disrupted
    */
  def HandleProgressChange(
      delta: Float,
      completionAction: () => Unit,
      tickAction: Float => Boolean,
      tick: Long
  ): Unit = {
    progressBarUpdate.cancel()
    progressBarValue match {
      case Some(value) =>
        val next = value + delta
        if (value >= 100f) {
          //complete
          progressBarValue = None
          tickAction(100)
          completionAction()
        } else if (value < 100f && next >= 100f) {
          if (tickAction(99)) {
            //will complete after this turn
            progressBarValue = Some(next)
            import scala.concurrent.ExecutionContext.Implicits.global
            progressBarUpdate = context.system.scheduler.scheduleOnce(
              100 milliseconds,
              self,
              ProgressEvent(delta, completionAction, tickAction)
            )
          } else {
            progressBarValue = None
          }
        } else {
          if (tickAction(next)) {
            //normal progress activity
            progressBarValue = Some(next)
            import scala.concurrent.ExecutionContext.Implicits.global
            progressBarUpdate = context.system.scheduler.scheduleOnce(
              tick milliseconds,
              self,
              ProgressEvent(delta, completionAction, tickAction, tick)
            )
          } else {
            progressBarValue = None
          }
        }
      case None => ;
    }
  }

  /**
    * Instruct the client to treat this player as the avatar.
    * Initialize all client-specific data that is dependent on some player being declared the "avatar".
    * @param tplayer the target player
    */
  def HandleSetCurrentAvatar(tplayer: Player): Unit = {
    log.info(s"HandleSetCurrentAvatar - ${tplayer.Name}")
    session = session.copy(player = tplayer)
    val guid = tplayer.GUID
    UpdateDeployableUIElements(Deployables.InitializeDeployableUIElements(avatar))
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 75, 0))
    sendResponse(SetCurrentAvatarMessage(guid, 0, 0))
    sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, true, "", "1 on", None)) //CC on //TODO once per respawn?
    val pos    = player.Position = shiftPosition.getOrElse(tplayer.Position)
    val orient = player.Orientation = shiftOrientation.getOrElse(tplayer.Orientation)
    sendResponse(PlayerStateShiftMessage(ShiftState(1, pos, orient.z)))
    shiftPosition = None
    shiftOrientation = None
    if (player.spectator) {
      sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, false, "", "on", None))
    }
    if (player.Jammed) {
      //TODO something better than just canceling?
      player.Actor ! JammableUnit.ClearJammeredStatus()
      player.Actor ! JammableUnit.ClearJammeredSound()
    }
    // TODO only when respawning after death
    avatarActor ! AvatarActor.ResetImplants()

    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 82, 0))
    //TODO if Medkit does not have shortcut, add to a free slot or write over slot 64
    sendResponse(CreateShortcutMessage(guid, 1, 0, true, Shortcut.Medkit))
    sendResponse(ChangeShortcutBankMessage(guid, 0))
    //Favorites lists
    avatarActor ! AvatarActor.RefreshLoadouts()

    sendResponse(
      SetChatFilterMessage(ChatChannel.Platoon, false, ChatChannel.values.toList)
    ) //TODO will not always be "on" like this
    val originalDeadState = deadState
    deadState = DeadState.Alive
    sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, tplayer.Position, player.Faction, true))
    //looking for squad (members)
    if (tplayer.avatar.lookingForSquad || lfsm) {
      sendResponse(PlanetsideAttributeMessage(guid, 53, 1))
      continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.PlanetsideAttribute(guid, 53, 1))
    }
    sendResponse(AvatarSearchCriteriaMessage(guid, List(0, 0, 0, 0, 0, 0)))
    (1 to 73).foreach(i => {
      // not all GUID's are set, and not all of the set ones will always be zero; what does this section do?
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(i), 67, 0))
    })
    (0 to 30).foreach(i => {
      //TODO 30 for a new character only?
      sendResponse(AvatarStatisticsMessage(2, Statistics(0L)))
    })
    //AvatarAwardMessage
    //DisplayAwardMessage
    sendResponse(PlanetsideStringAttributeMessage(guid, 0, "Outfit Name"))
    //squad stuff (loadouts, assignment)
    squadSetup()
    //MapObjectStateBlockMessage and ObjectCreateMessage?
    //TacticsMessage?
    //change the owner on our deployables (re-draw the icons for our deployables too)
    val name = tplayer.Name
    continent.DeployableList
      .filter(_.OwnerName.contains(name))
      .foreach(obj => {
        obj.Owner = guid
        drawDeloyableIcon(obj)
      })
    drawDeloyableIcon = DontRedrawIcons

    //assert or transfer vehicle ownership
    continent.GUID(player.avatar.vehicle) match {
      case Some(vehicle: Vehicle) if vehicle.OwnerName.contains(tplayer.Name) =>
        vehicle.Owner = guid
        continent.VehicleEvents ! VehicleServiceMessage(
          s"${tplayer.Faction}",
          VehicleAction.Ownership(guid, vehicle.GUID)
        )
      case _ =>
        avatarActor ! AvatarActor.SetVehicle(None)
    }
    GetVehicleAndSeat() match {
      //we're falling
      case (Some(vehicle), _) if vehicle.Definition == GlobalDefinitions.droppod =>
        sendResponse(
          DroppodFreefallingMessage(
            vehicle.GUID,
            vehicle.Position + Vector3.z(50),
            Vector3.z(-999),
            vehicle.Position + Vector3.z(25),
            Vector3(0, 70.3125f, 90),
            Vector3(0, 0, 90)
          )
        )
      case (Some(vehicle), Some(0)) =>
        //summon any passengers and cargo vehicles left behind on previous continent
        if (vehicle.Jammed) {
          //TODO something better than just canceling?
          vehicle.Actor ! JammableUnit.ClearJammeredStatus()
          vehicle.Actor ! JammableUnit.ClearJammeredSound()
        }
        //positive shield strength
        if (vehicle.Shields > 0) {
          sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 68, vehicle.Shields))
        }
        // ANT capacitor
        if (vehicle.Definition == GlobalDefinitions.ant) {
          sendResponse(
            PlanetsideAttributeMessage(vehicle.GUID, 45, vehicle.NtuCapacitorScaled)
          ) // set ntu on vehicle UI
        }
        LoadZoneTransferPassengerMessages(
          guid,
          continent.id,
          vehicle
        )
      case _ => ;
    }
    interstellarFerryTopLevelGUID = None
    if (loadConfZone && connectionState == 100) {
      configZone(continent)
      loadConfZone = false
    }
    if (noSpawnPointHere) {
      RequestSanctuaryZoneSpawn(player, continent.Number)
    } else if (originalDeadState == DeadState.Dead || player.Health == 0) {
      //killed during spawn setup or possibly a relog into a corpse (by accident?)
      player.Actor ! Player.Die()
    }
    upstreamMessageCount = 0
    setAvatar = true
  }

  /**
    * Instruct the client to treat this player as the avatar.
    * @see `SetCurrentAvatar`
    * @param tplayer the target player
    */
  def SetCurrentAvatarNormally(tplayer: Player): Unit = {
    HandleSetCurrentAvatar(tplayer)
  }

  /**
    * These messages are dispatched when first starting up the client and connecting to the server for the first time.
    * While many of thee messages will be reused for other situations, they appear in this order only during startup.
    */
  def FirstTimeSquadSetup(): Unit = {
    sendResponse(SquadDetailDefinitionUpdateMessage.Init)
    sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(6)))
    //only need to load these once - they persist between zone transfers and respawns
    avatar.squadLoadouts.zipWithIndex.foreach {
      case (Some(loadout), index) =>
        sendResponse(
          SquadDefinitionActionMessage(PlanetSideGUID(0), index, SquadAction.ListSquadFavorite(loadout.task))
        )
      case (None, _) => ;
    }
    //non-squad GUID-0 counts as the settings when not joined with a squad
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.AssociateWithSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.SetListSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitSquadList())
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitCharId())
    squadSetup = RespawnSquadSetup
  }

  /**
    * These messages are used during each subsequent respawn to reset the squad colors on player nameplates and marquees.
    * By using `squadUI` to maintain relevant information about squad members,
    * especially the unique character identifier number,
    * only the zone-specific squad members will receive the important messages about their squad member's spawn.
    */
  def RespawnSquadSetup(): Unit = {
    if (squadUI.nonEmpty) {
      sendResponse(PlanetsideAttributeMessage(player.GUID, 31, squad_supplement_id))
      continent.AvatarEvents ! AvatarServiceMessage(
        s"${player.Faction}",
        AvatarAction.PlanetsideAttribute(player.GUID, 31, squad_supplement_id)
      )
      sendResponse(PlanetsideAttributeMessage(player.GUID, 32, squadUI(player.CharId).index))
    }
  }

  /**
    * These messages are used during each subsequent respawn to reset the squad colors on player nameplates and marquees.
    * During a zone change,
    * on top of other squad mates in the zone needing to have their knowledge of this player's squad colors changed,
    * the player must also set squad colors for each other squad members.
    * Default respawn functionality may resume afterwards.
    */
  def ZoneChangeSquadSetup(): Unit = {
    RespawnSquadSetup()
    GiveSquadColorsInZone()
    squadSetup = RespawnSquadSetup
  }

  /**
    * Allocate all squad members in zone and give their nameplates and their marquees the appropriate squad color.
    */
  def GiveSquadColorsInZone(): Unit = {
    GiveSquadColorsInZone(squadUI.keys, squad_supplement_id)
  }

  /**
    * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
    * @param members members of the squad to target
    */
  def GiveSquadColorsInZone(members: Iterable[Long]): Unit = {
    GiveSquadColorsInZone(members, squad_supplement_id)
  }

  /**
    * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
    * @see `PlanetsideAttributeMessage`
    * @param members members of the squad to target
    * @param value   the assignment value
    */
  def GiveSquadColorsInZone(members: Iterable[Long], value: Long): Unit = {
    SquadMembersInZone(members).foreach { members =>
      sendResponse(PlanetsideAttributeMessage(members.GUID, 31, value))
    }
  }

  /**
    * For the listed squad member unique character identifier numbers,
    * find and return all squad members in the current zone.
    * @param members members of the squad to target
    * @return a list of `Player` objects
    */
  def SquadMembersInZone(members: Iterable[Long]): Iterable[Player] = {
    val players = continent.LivePlayers
    for {
      charId <- members
      player = players.find {
        _.CharId == charId
      }
      if player.nonEmpty
    } yield player.get
  }

  def handleGamePkt(pkt: PlanetSideGamePacket) =
    pkt match {
      case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>
        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
        log.info(s"New world login to $server with Token:$token. $clientVersion")
        sendResponse(ChatMsg(ChatMessageType.CMT_CULLWATERMARK, false, "", "", None))
        import scala.concurrent.ExecutionContext.Implicits.global
        clientKeepAlive.cancel()
        clientKeepAlive =
          context.system.scheduler.scheduleWithFixedDelay(0 seconds, 500 milliseconds, self, PokeClient())
        accountIntermediary ! RetrieveAccountData(token)

      case msg @ MountVehicleCargoMsg(player_guid, cargo_guid, carrier_guid, unk4) =>
        log.info(msg.toString)
        (continent.GUID(cargo_guid), continent.GUID(carrier_guid)) match {
          case (Some(cargo: Vehicle), Some(carrier: Vehicle)) =>
            carrier.CargoHolds.find({ case (_, hold) => !hold.isOccupied }) match {
              case Some((mountPoint, _)) => //try begin the mount process
                cargo.Actor ! CargoBehavior.CheckCargoMounting(carrier_guid, mountPoint, 0)
              case _ =>
                log.warn(
                  s"MountVehicleCargoMsg: target carrier vehicle (${carrier.Definition.Name}) does not have a cargo hold"
                )
            }
          case (None, _) | (Some(_), None) =>
            log.warn(
              s"MountVehicleCargoMsg: one or more of the target vehicles do not exist - $carrier_guid or $cargo_guid"
            )
          case _ => ;
        }

      case msg @ DismountVehicleCargoMsg(player_guid, cargo_guid, bailed, requestedByPassenger, kicked) =>
        log.info(msg.toString)
        //when kicked by carrier driver, player_guid will be PlanetSideGUID(0)
        //when exiting of the cargo vehicle driver's own accord, player_guid will be the cargo vehicle driver
        continent.GUID(cargo_guid) match {
          case Some(cargo: Vehicle) if !requestedByPassenger =>
            continent.GUID(cargo.MountedIn) match {
              case Some(carrier: Vehicle) =>
                CargoBehavior.HandleVehicleCargoDismount(continent, cargo_guid, bailed, requestedByPassenger, kicked)
              case _ => ;
            }
          case _ => ;
        }

      case msg @ CharacterCreateRequestMessage(name, head, voice, gender, empire) =>
        avatarActor ! AvatarActor.CreateAvatar(name, head, voice, gender, empire)

      case msg @ CharacterRequestMessage(charId, action) =>
        action match {
          case CharacterRequestAction.Delete =>
            avatarActor ! AvatarActor.DeleteAvatar(charId.toInt)
          case CharacterRequestAction.Select =>
            avatarActor ! AvatarActor.SelectAvatar(charId.toInt, context.self)
        }

      case KeepAliveMessage(_) =>
        keepAliveFunc()

      case msg @ BeginZoningMessage() =>
        log.info("Reticulating splines ...")
        zoneLoaded = None
        val continentId    = continent.id
        val faction        = player.Faction
        val factionChannel = s"$faction"
        continent.AvatarEvents ! Service.Join(continentId)
        continent.AvatarEvents ! Service.Join(factionChannel)
        continent.LocalEvents ! Service.Join(avatar.name)
        continent.LocalEvents ! Service.Join(continentId)
        continent.LocalEvents ! Service.Join(factionChannel)
        continent.VehicleEvents ! Service.Join(avatar.name)
        continent.VehicleEvents ! Service.Join(continentId)
        continent.VehicleEvents ! Service.Join(factionChannel)
        if (connectionState != 100) configZone(continent)
        sendResponse(TimeOfDayMessage(1191182336))
        //custom
        sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty))    //clear squad list
        sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks

        //find and reclaim own deployables, if any
        val guid = player.GUID
        val foundDeployables =
          continent.DeployableList.filter(obj => obj.OwnerName.contains(player.Name) && obj.Health > 0)
        continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(foundDeployables, continent))
        foundDeployables.foreach(obj => {
          if (avatar.deployables.Add(obj)) {
            obj.Owner = guid
            log.info(s"Found a ${obj.Definition.Name} of ours while loading the zone")
          }
        })
        //render deployable objects
        val (turrets, normal) = continent.DeployableList.partition(obj =>
          DeployableToolbox.UnifiedType(obj.Definition.Item) == DeployedItem.portable_manned_turret
        )
        normal.foreach(obj => {
          val definition = obj.Definition
          sendResponse(
            ObjectCreateMessage(
              definition.ObjectId,
              obj.GUID,
              definition.Packet.ConstructorData(obj).get
            )
          )
        })
        turrets.foreach(obj => {
          val objGUID    = obj.GUID
          val definition = obj.Definition
          sendResponse(
            ObjectCreateMessage(
              definition.ObjectId,
              objGUID,
              definition.Packet.ConstructorData(obj).get
            )
          )
          //seated players
          obj
            .asInstanceOf[Mountable]
            .Seats
            .values
            .map(_.Occupant)
            .collect {
              case Some(occupant) =>
                if (occupant.isAlive) {
                  val targetDefinition = occupant.avatar.definition
                  sendResponse(
                    ObjectCreateMessage(
                      targetDefinition.ObjectId,
                      occupant.GUID,
                      ObjectCreateMessageParent(objGUID, 0),
                      targetDefinition.Packet.ConstructorData(occupant).get
                    )
                  )
                }
            }
        })
        //sensor animation
        normal
          .filter(obj =>
            obj.Definition.DeployCategory == DeployableCategory.Sensors &&
              !obj.Destroyed &&
              (obj match {
                case jObj: JammableUnit => !jObj.Jammed;
                case _                  => true
              })
          )
          .foreach(obj => {
            sendResponse(TriggerEffectMessage(obj.GUID, "on", true, 1000))
          })
        //update the health of our faction's deployables (if necessary)
        //draw our faction's deployables on the map
        continent.DeployableList
          .filter(obj => obj.Faction == faction && !obj.Destroyed)
          .foreach(obj => {
            if (obj.Health != obj.DefaultHealth) {
              sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
            }
            val deployInfo = DeployableInfo(
              obj.GUID,
              Deployable.Icon(obj.Definition.Item),
              obj.Position,
              obj.Owner.getOrElse(PlanetSideGUID(0))
            )
            sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
          })
        //render Equipment that was dropped into zone before the player arrived
        continent.EquipmentOnGround.foreach(item => {
          val definition = item.Definition
          sendResponse(
            ObjectCreateMessage(
              definition.ObjectId,
              item.GUID,
              DroppedItemData(
                PlacementData(item.Position, item.Orientation),
                definition.Packet.ConstructorData(item).get
              )
            )
          )
        })
        //load active players in zone (excepting players who are seated or players who are us)
        val live = continent.LivePlayers
        log.info(s"loading players $live")
        live
          .filterNot(tplayer => {
            tplayer.GUID == player.GUID || tplayer.VehicleSeated.nonEmpty
          })
          .foreach(targetPlayer => {
            val targetDefinition = player.avatar.definition
            sendResponse(
              ObjectCreateMessage(
                targetDefinition.ObjectId,
                targetPlayer.GUID,
                targetDefinition.Packet.ConstructorData(targetPlayer).get
              )
            )
            if (targetPlayer.UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored) {
              sendResponse(PlanetsideAttributeMessage(targetPlayer.GUID, 19, 1))
            }
          })
        //load corpses in zone
        continent.Corpses.foreach {
          DepictPlayerAsCorpse
        }
        //load vehicles in zone (put separate the one we may be using)
        val (wreckages, (vehicles, usedVehicle)) = {
          val (a, b) = continent.Vehicles.partition(vehicle => {
            vehicle.Destroyed && vehicle.Definition.DestroyedModel.nonEmpty
          })
          (
            a,
            continent.GUID(player.VehicleSeated) match {
              case Some(vehicle: Vehicle) if vehicle.PassengerInSeat(player).isDefined =>
                b.partition {
                  _.GUID != vehicle.GUID
                }
              case Some(_) =>
                //vehicle, but we're not seated in it
                player.VehicleSeated = None
                (b, List.empty[Vehicle])
              case None =>
                //throw error since VehicleSeated didn't point to a vehicle?
                player.VehicleSeated = None
                (b, List.empty[Vehicle])
            }
          )
        }
        val allActiveVehicles = vehicles ++ usedVehicle
        //active vehicles (and some wreckage)
        vehicles.foreach(vehicle => {
          val vguid       = vehicle.GUID
          val vdefinition = vehicle.Definition
          sendResponse(
            ObjectCreateMessage(vdefinition.ObjectId, vguid, vdefinition.Packet.ConstructorData(vehicle).get)
          )
          //occupants other than driver
          vehicle.Seats
            .filter({ case (index, seat) => seat.isOccupied && live.contains(seat.Occupant.get) && index > 0 })
            .foreach({
              case (index, seat) =>
                val targetPlayer    = seat.Occupant.get
                val targetDefiniton = targetPlayer.avatar.definition
                sendResponse(
                  ObjectCreateMessage(
                    targetDefiniton.ObjectId,
                    targetPlayer.GUID,
                    ObjectCreateMessageParent(vguid, index),
                    targetDefiniton.Packet.ConstructorData(targetPlayer).get
                  )
                )
            })
        })
        vehicles.collect {
          case vehicle if vehicle.Faction == faction =>
            Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        }
        //our vehicle would have already been loaded; see NewPlayerLoaded/AvatarCreate
        usedVehicle.headOption match {
          case Some(vehicle) =>
            //depict any other passengers already in this zone
            val vguid = vehicle.GUID
            vehicle.Seats
              .filter({
                case (index, seat) =>
                  seat.isOccupied && !seat.Occupant.contains(player) && live.contains(seat.Occupant.get) && index > 0
              })
              .foreach({
                case (index, seat) =>
                  val targetPlayer     = seat.Occupant.get
                  val targetDefinition = targetPlayer.avatar.definition
                  sendResponse(
                    ObjectCreateMessage(
                      targetDefinition.ObjectId,
                      targetPlayer.GUID,
                      ObjectCreateMessageParent(vguid, index),
                      targetDefinition.Packet.ConstructorData(targetPlayer).get
                    )
                  )
              })
            //since we would have only subscribed recently, we need to reload seat access states
            (0 to 3).foreach { group =>
              sendResponse(PlanetsideAttributeMessage(vguid, group + 10, vehicle.PermissionGroup(group).get.id))
            }
            //positive shield strength
            if (vehicle.Shields > 0) {
              sendResponse(PlanetsideAttributeMessage(vguid, 68, vehicle.Shields))
            }
          case _ => ; //no vehicle
        }
        //vehicle wreckages
        wreckages.foreach(vehicle => {
          sendResponse(
            ObjectCreateMessage(
              vehicle.Definition.DestroyedModel.get.id,
              vehicle.GUID,
              DestroyedVehicleConverter.converter.ConstructorData(vehicle).get
            )
          )
        })
        //cargo occupants (including our own vehicle as cargo)
        allActiveVehicles.collect {
          case vehicle if vehicle.CargoHolds.nonEmpty =>
            vehicle.CargoHolds.collect({
              case (index, hold: Cargo) if hold.isOccupied => {
                CargoBehavior.CargoMountBehaviorForAll(
                  vehicle,
                  hold.Occupant.get,
                  index
                ) //CargoMountBehaviorForUs can fail to attach the cargo vehicle on some clients
              }
            })
        }
        //special deploy states
        val deployedVehicles = allActiveVehicles.filter(_.DeploymentState == DriveState.Deployed)
        deployedVehicles.filter(_.Definition == GlobalDefinitions.ams).foreach { obj =>
          //???
          sendResponse(PlanetsideAttributeMessage(obj.GUID, 81, 1))
        }
        deployedVehicles.filter(_.Definition == GlobalDefinitions.ant).foreach { obj =>
          //special effects
          sendResponse(PlanetsideAttributeMessage(obj.GUID, 52, 1)) // ant panel glow
          Vehicles.FindANTChargingSource(obj, None).orElse(Vehicles.FindANTDischargingTarget(obj, None)) match {
            case Some(silo: ResourceSilo) =>
              sendResponse(PlanetsideAttributeMessage(silo.GUID, 49, 1)) // silo orb particle effect
            case Some(_: WarpGate) =>
              sendResponse(PlanetsideAttributeMessage(obj.GUID, 49, 1)) // ant orb particle effect
            case _ => ;
          }
        }
        deployedVehicles.filter(_.Definition == GlobalDefinitions.router).foreach { obj =>
          //the router won't work if it doesn't completely deploy
          sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deploying, 0, false, Vector3.Zero))
          sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deployed, 0, false, Vector3.Zero))
          ToggleTeleportSystem(obj, TelepadLike.AppraiseTeleportationSystem(obj, continent))
        }

        //implant terminals
        continent.map.terminalToInterface.foreach({
          case (terminal_guid, interface_guid) =>
            val parent_guid = PlanetSideGUID(terminal_guid)
            continent.GUID(interface_guid) match {
              case Some(obj: Terminal) =>
                val objDef = obj.Definition
                sendResponse(
                  ObjectCreateMessage(
                    objDef.ObjectId,
                    PlanetSideGUID(interface_guid),
                    ObjectCreateMessageParent(parent_guid, 1),
                    objDef.Packet.ConstructorData(obj).get
                  )
                )
              case _ => ;
            }
            //seat terminal occupants
            continent.GUID(terminal_guid) match {
              case Some(obj: Mountable) =>
                obj.Seats(0).Occupant match {
                  case Some(targetPlayer) =>
                    val targetDefinition = targetPlayer.avatar.definition
                    sendResponse(
                      ObjectCreateMessage(
                        targetDefinition.ObjectId,
                        targetPlayer.GUID,
                        ObjectCreateMessageParent(parent_guid, 0),
                        targetDefinition.Packet.ConstructorData(targetPlayer).get
                      )
                    )
                  case None => ;
                }
              case _ => ;
            }
        })

        //base turrets
        continent.map.turretToWeapon
          .map { case (turret_guid: Int, _) => continent.GUID(turret_guid) }
          .collect {
            case Some(turret: FacilityTurret) =>
              val pguid = turret.GUID
              //attached weapon
              if (!turret.isUpgrading) {
                turret.ControlledWeapon(wepNumber = 1) match {
                  case Some(obj: Tool) =>
                    val objDef = obj.Definition
                    sendResponse(
                      ObjectCreateMessage(
                        objDef.ObjectId,
                        obj.GUID,
                        ObjectCreateMessageParent(pguid, 1),
                        objDef.Packet.ConstructorData(obj).get
                      )
                    )
                  case _ => ;
                }
              }
              //reserved ammunition?
              //TODO need to register if it exists
              //seat turret occupant
              turret.Seats(0).Occupant match {
                case Some(targetPlayer) =>
                  val targetDefinition = targetPlayer.avatar.definition
                  sendResponse(
                    ObjectCreateMessage(
                      targetDefinition.ObjectId,
                      targetPlayer.GUID,
                      ObjectCreateMessageParent(pguid, 0),
                      targetDefinition.Packet.ConstructorData(targetPlayer).get
                    )
                  )
                case None => ;
              }
          }
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.UpdateAmsSpawnPoint(continent)
        )
        upstreamMessageCount = 0
        zoneLoaded = Some(true)

      case msg @ PlayerStateMessageUpstream(
            avatar_guid,
            pos,
            vel,
            yaw,
            pitch,
            yaw_upper,
            seq_time,
            unk3,
            is_crouching,
            is_jumping,
            jump_thrust,
            is_cloaking,
            unk5,
            unk6
          ) =>
        //log.info(s"$msg")
        persist()
        turnCounterFunc(avatar_guid)
        val isMoving     = WorldEntity.isMoving(vel)
        val isMovingPlus = isMoving || is_jumping || jump_thrust
        if (isMovingPlus) {
          CancelZoningProcessWithDescriptiveReason("cancel_motion")
        }
        player.Position = pos
        player.Velocity = vel
        player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
        player.FacingYawUpper = yaw_upper
        player.Crouching = is_crouching
        player.Jumping = is_jumping
        if (is_cloaking && !player.Cloaked) {
          CancelZoningProcessWithDescriptiveReason("cancel_cloak")
        }
        player.Cloaked = player.ExoSuit == ExoSuitType.Infiltration && is_cloaking
        CapacitorTick(jump_thrust)
        if (isMovingPlus && usingMedicalTerminal.isDefined) {
          continent.GUID(usingMedicalTerminal) match {
            case Some(term: Terminal with ProximityUnit) =>
              StopUsingProximityUnit(term)
            case _ => ;
          }
        }
        accessedContainer match {
          case Some(veh: Vehicle) =>
            if (
              isMoving || veh.isMoving(1) || Vector3.DistanceSquared(
                player.Position,
                veh.TrunkLocation
              ) > 9
            ) {
              val guid = player.GUID
              sendResponse(UnuseItemMessage(guid, veh.GUID))
              sendResponse(UnuseItemMessage(guid, guid))
              UnaccessContainer(veh)
            }
          case Some(container) => //just in case
            if (isMovingPlus) {
              val guid = player.GUID
              // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
              if (container.HasGUID) {
                sendResponse(UnuseItemMessage(guid, container.GUID))
              }
              sendResponse(UnuseItemMessage(guid, guid))
              UnaccessContainer(container)
            }
          case None => ;
        }
        val wepInHand: Boolean = player.Slot(player.DrawnSlot).Equipment match {
          case Some(item) => item.Definition == GlobalDefinitions.bolt_driver
          case None       => false
        }
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.PlayerState(
            avatar_guid,
            player.Position,
            player.Velocity,
            yaw,
            pitch,
            yaw_upper,
            seq_time,
            is_crouching,
            is_jumping,
            jump_thrust,
            is_cloaking,
            player.spectator,
            wepInHand
          )
        )
        updateSquad()
        if (player.death_by == -1) {
          KickedByAdministration()
        }

      case msg @ ChildObjectStateMessage(object_guid, pitch, yaw) =>
        //log.info(s"$msg")
        //the majority of the following check retrieves information to determine if we are in control of the child
        FindContainedWeapon match {
          case (Some(o), Some(tool)) =>
            (o match {
              case mount: Mountable => mount.PassengerInSeat(player)
              case _                => None
            }) match {
              case None | Some(0) => ;
              case Some(_) =>
                persist()
                turnCounterFunc(player.GUID)
            }
            if (tool.GUID == object_guid) {
              //TODO set tool orientation?
              player.Orientation = Vector3(0f, pitch, yaw)
              continent.VehicleEvents ! VehicleServiceMessage(
                continent.id,
                VehicleAction.ChildObjectState(player.GUID, object_guid, pitch, yaw)
              )
            } else {
              log.warn(
                s"ChildObjectState: ${player.Name} is using a different controllable agent than #${object_guid.guid}"
              )
            }
          case (Some(obj), None) =>
            log.warn(
              s"ChildObjectState: ${player.Name} can not find any controllable agent, let alone #${object_guid.guid}"
            )
          case (None, _) => ;
          //TODO status condition of "playing getting out of vehicle to allow for late packets without warning
          //log.warn(s"ChildObjectState: player ${player.Name} not related to anything with a controllable agent")
        }
        if (player.death_by == -1) {
          KickedByAdministration()
        }

      case msg @ VehicleStateMessage(
            vehicle_guid,
            unk1,
            pos,
            ang,
            vel,
            flying,
            unk6,
            unk7,
            wheels,
            is_decelerating,
            is_cloaked
          ) =>
        //log.info(s"$msg")
        GetVehicleAndSeat() match {
          case (Some(obj), Some(0)) =>
            //we're driving the vehicle
            persist()
            turnCounterFunc(player.GUID)
            val seat = obj.Seats(0)
            player.Position = pos //convenient
            if (seat.ControlledWeapon.isEmpty) {
              player.Orientation = Vector3.z(ang.z) //convenient
            }
            obj.Position = pos
            obj.Orientation = ang
            if (obj.MountedIn.isEmpty) {
              if (obj.DeploymentState != DriveState.Deployed) {
                obj.Velocity = vel
              } else {
                obj.Velocity = Some(Vector3.Zero)
              }
              if (obj.Definition.CanFly) {
                obj.Flying = flying.nonEmpty //usually Some(7)
              }
              obj.Cloaked = obj.Definition.CanCloak && is_cloaked
            } else {
              obj.Velocity = None
              obj.Flying = false
            }
            continent.VehicleEvents ! VehicleServiceMessage(
              continent.id,
              VehicleAction.VehicleState(
                player.GUID,
                vehicle_guid,
                unk1,
                obj.Position,
                ang,
                obj.Velocity,
                if (obj.Flying) {
                  flying
                } else {
                  None
                },
                unk6,
                unk7,
                wheels,
                is_decelerating,
                obj.Cloaked
              )
            )
            updateSquad()
          case (None, _) =>
          //log.error(s"VehicleState: no vehicle $vehicle_guid found in zone")
          //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
          case (_, Some(index)) =>
            log.error(
              s"VehicleState: player should not be dispatching this kind of packet from vehicle#$vehicle_guid  when not the driver ($index)"
            )
          case _ => ;
        }
        if (player.death_by == -1) {
          KickedByAdministration()
        }

      case msg @ VehicleSubStateMessage(vehicle_guid, player_guid, vehicle_pos, vehicle_ang, vel, unk1, unk2) =>
      //log.info(s"VehicleSubState: $vehicle_guid, ${player.Name}_guid, $vehicle_pos, $vehicle_ang, $vel, $unk1, $unk2")

      case msg @ ProjectileStateMessage(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid) =>
        //log.trace(s"ProjectileState: $msg")
        val index = projectile_guid.guid - Projectile.baseUID
        projectiles(index) match {
          case Some(projectile) if projectile.HasGUID =>
            val projectileGlobalUID = projectile.GUID
            projectile.Position = shot_pos
            projectile.Orientation = shot_orient
            projectile.Velocity = shot_vel
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ProjectileState(
                player.GUID,
                projectileGlobalUID,
                shot_pos,
                shot_vel,
                shot_orient,
                seq,
                end,
                target_guid
              )
            )
          case _ if seq == 0 =>
          /* missing the first packet in the sequence is permissible  */
          case _ =>
            log.warn(s"ProjectileState: constructed projectile ${projectile_guid.guid} can not be found")
        }

      case msg @ ReleaseAvatarRequestMessage() =>
        log.info(s"ReleaseAvatarRequest: ${player.GUID} on ${continent.id} has released")
        reviveTimer.cancel()
        GoToDeploymentMap()
        HandleReleaseAvatar(player, continent)

      case msg @ SpawnRequestMessage(u1, spawnGroup, u3, u4, zoneNumber) =>
        log.info(s"SpawnRequestMessage: $msg")
        if (deadState != DeadState.RespawnTime) {
          deadState = DeadState.RespawnTime
          cluster ! InterstellarClusterService.GetNearbySpawnPoint(
            spawnGroup match {
              case SpawnGroup.Sanctuary =>
                Zones.sanctuaryZoneNumber(player.Faction)
              case _ =>
                zoneNumber
            },
            player,
            Seq(spawnGroup),
            context.self
          )
        } else {
          log.warn("SpawnRequestMessage: request consumed; already respawning ...")
        }

      case msg @ SetChatFilterMessage(send_channel, origin, whitelist) =>
      //log.info("SetChatFilters: " + msg)

      case msg: ChatMsg =>
        chatActor ! ChatActor.Message(msg)

      case msg @ VoiceHostRequest(unk, PlanetSideGUID(player_guid), data) =>
        log.info("Player " + player_guid + " requested in-game voice chat.")
        sendResponse(VoiceHostKill())

      case msg @ VoiceHostInfo(player_guid, data) =>
        sendResponse(VoiceHostKill())

      case msg @ ChangeAmmoMessage(item_guid, unk1) =>
        log.info("ChangeAmmo: " + msg)
        FindContainedEquipment match {
          case (Some(_), Some(obj: ConstructionItem)) =>
            PerformConstructionItemAmmoChange(obj, obj.AmmoTypeIndex)
          case (Some(obj), Some(tool: Tool)) =>
            PerformToolAmmoChange(tool, obj)
          case (_, Some(obj)) =>
            log.error(s"ChangeAmmo: the object ${obj.Definition.Name} is not a valid type")
          case (_, None) =>
            log.error(s"ChangeAmmo: can not find $item_guid")
        }

      case msg @ ChangeFireModeMessage(item_guid, fire_mode) =>
        log.info("ChangeFireMode: " + msg)
        FindEquipment match {
          case Some(obj: PlanetSideGameObject with FireModeSwitch[_]) =>
            val originalModeIndex = obj.FireModeIndex
            obj match {
              case cItem: ConstructionItem =>
                NextConstructionItemFireMode(cItem, originalModeIndex)
              case _ =>
                obj.NextFireMode
            }
            val modeIndex = obj.FireModeIndex
            val tool_guid = obj.GUID
            if (originalModeIndex == modeIndex) {
              obj.FireModeIndex = originalModeIndex
              sendResponse(ChangeFireModeMessage(tool_guid, originalModeIndex)) //reinforcement
            } else {
              log.info(s"ChangeFireMode: changing $tool_guid to fire mode $modeIndex")
              sendResponse(ChangeFireModeMessage(tool_guid, modeIndex))
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.ChangeFireMode(player.GUID, tool_guid, modeIndex)
              )
            }
          case Some(_) =>
            log.error(s"ChangeFireMode: the object that was found for $item_guid does not possess fire modes")
          case None =>
            log.error(s"ChangeFireMode: can not find $item_guid")
        }

      case msg @ ChangeFireStateMessage_Start(item_guid) =>
        log.trace("ChangeFireState_Start: " + msg)
        if (shooting.isEmpty) {
          FindEquipment match {
            case Some(tool: Tool) =>
              if (tool.Magazine > 0 || prefire.contains(item_guid)) {
                prefire = None
                shooting = Some(item_guid)
                shootingStart = System.currentTimeMillis()
                //special case - suppress the decimator's alternate fire mode, by projectile
                if (tool.Projectile != GlobalDefinitions.phoenix_missile_guided_projectile) {
                  continent.AvatarEvents ! AvatarServiceMessage(
                    continent.id,
                    AvatarAction.ChangeFireState_Start(player.GUID, item_guid)
                  )
                }
                //charge ammunition drain
                tool.FireMode match {
                  case mode: ChargeFireModeDefinition =>
                    progressBarValue = Some(0f)
                    progressBarUpdate = context.system.scheduler.scheduleOnce(
                      (mode.Time + mode.DrainInterval) milliseconds,
                      self,
                      ProgressEvent(1f, () => {}, Tools.ChargeFireMode(player, tool), mode.DrainInterval)
                    )
                  case _ => ;
                }
              } else {
                log.warn(
                  s"ChangeFireState_Start: ${tool.Definition.Name} magazine is empty before trying to shoot bullet"
                )
                EmptyMagazine(item_guid, tool)
              }
            case Some(_) => //permissible, for now
              prefire = None
              shooting = Some(item_guid)
              shootingStart = System.currentTimeMillis()
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.ChangeFireState_Start(player.GUID, item_guid)
              )
            case None =>
              log.error(s"ChangeFireState_Start: can not find $item_guid")
          }
        }

      case msg @ ChangeFireStateMessage_Stop(item_guid) =>
        log.trace("ChangeFireState_Stop: " + msg)
        prefire = None
        shootingStop = System.currentTimeMillis()
        val weapon: Option[Equipment] = if (shooting.contains(item_guid)) {
          shooting = None
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.ChangeFireState_Stop(player.GUID, item_guid)
          )
          FindEquipment
        } else {
          FindEquipment match {
            case Some(tool: Tool) =>
              //the decimator does not send a ChangeFireState_Start on the last shot
              if (
                tool.Definition == GlobalDefinitions.phoenix &&
                tool.Projectile != GlobalDefinitions.phoenix_missile_guided_projectile
              ) {
                //suppress the decimator's alternate fire mode, however
                continent.AvatarEvents ! AvatarServiceMessage(
                  continent.id,
                  AvatarAction.ChangeFireState_Start(player.GUID, item_guid)
                )
                shootingStart = System.currentTimeMillis() - 1L
              }
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.ChangeFireState_Stop(player.GUID, item_guid)
              )
              Some(tool)
            case Some(tool) => //permissible, for now
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.ChangeFireState_Stop(player.GUID, item_guid)
              )
              Some(tool)
            case _ =>
              log.warn(s"ChangeFireState_Stop: received an unexpected message about $item_guid")
              None
          }
        }
        weapon match {
          case Some(tool: Tool) =>
            tool.FireMode match {
              case mode: ChargeFireModeDefinition =>
                sendResponse(QuantityUpdateMessage(tool.AmmoSlot.Box.GUID, tool.Magazine))
              case _ => ;
            }
            if (tool.Magazine == 0) {
              FireCycleCleanup(tool)
            }
          case Some(trigger: BoomerTrigger) =>
            val playerGUID = player.GUID
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ChangeFireState_Start(playerGUID, item_guid)
            )
            continent.GUID(trigger.Companion) match {
              case Some(boomer: BoomerDeployable) =>
                boomer.Destroyed = true
                continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.Detonate(boomer.GUID, boomer))
                Deployables.AnnounceDestroyDeployable(boomer, Some(500 milliseconds))
              case Some(_) | None => ;
            }
            FindEquipmentToDelete(item_guid, trigger)
            trigger.Companion = None
          case _ => ;
        }
        progressBarUpdate.cancel()
        progressBarValue = None

      case msg @ EmoteMsg(avatar_guid, emote) =>
        log.info("Emote: " + msg)
        sendResponse(EmoteMsg(avatar_guid, emote))

      case msg @ DropItemMessage(item_guid) =>
        log.info(s"DropItem: $msg")
        ValidObject(item_guid) match {
          case Some(anItem: Equipment) =>
            player.FreeHand.Equipment match {
              case Some(item) =>
                if (item.GUID == item_guid) {
                  CancelZoningProcessWithDescriptiveReason("cancel_use")
                  continent.GUID(player.VehicleSeated) match {
                    case Some(_) =>
                      RemoveOldEquipmentFromInventory(player)(item)
                    case None =>
                      DropEquipmentFromInventory(player)(item)
                  }
                }
              case None =>
                log.warn(s"DropItem: ${player.Name} wanted to drop a $anItem, but it wasn't at hand")
            }
          case Some(obj) => //TODO LLU
            log.warn(s"DropItem: ${player.Name} wanted to drop a $obj, but that isn't possible")
          case None =>
            sendResponse(ObjectDeleteMessage(item_guid, 0)) //this is fine; item doesn't exist to the server anyway
            log.warn(s"DropItem: ${player.Name} wanted to drop an item ($item_guid), but it was nowhere to be found")
        }

      case msg @ PickupItemMessage(item_guid, player_guid, unk1, unk2) =>
        log.info(s"PickupItem: $msg")
        ValidObject(item_guid) match {
          case Some(item: Equipment) =>
            player.Fit(item) match {
              case Some(_) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                PickUpEquipmentFromGround(player)(item)
              case None => //skip
                sendResponse(ActionResultMessage.Fail(16)) //error code?
            }
          case _ =>
            log.warn(
              s"PickupItem: ${player.Name} requested an item that doesn't exist in this zone; assume client-side garbage data"
            )
            sendResponse(ObjectDeleteMessage(item_guid, 0))
        }

      case msg @ ReloadMessage(item_guid, ammo_clip, unk1) =>
        log.info("Reload: " + msg)
        FindContainedWeapon match {
          case (Some(obj), Some(tool: Tool)) =>
            val currentMagazine: Int = tool.Magazine
            val magazineSize: Int    = tool.MaxMagazine
            val reloadValue: Int     = magazineSize - currentMagazine
            if (magazineSize > 0 && reloadValue > 0) {
              FindEquipmentStock(obj, FindAmmoBoxThatUses(tool.AmmoType), reloadValue, CountAmmunition).reverse match {
                case Nil =>
                  log.warn(s"ReloadMessage: no ammunition could be found for $item_guid")
                case x :: xs =>
                  val (deleteFunc, modifyFunc): (Equipment => Future[Any], (AmmoBox, Int) => Unit) = obj match {
                    case veh: Vehicle =>
                      (RemoveOldEquipmentFromInventory(veh), ModifyAmmunitionInVehicle(veh))
                    case o: PlanetSideServerObject with Container =>
                      (RemoveOldEquipmentFromInventory(o), ModifyAmmunition(o))
                    case _ =>
                      throw new Exception("ReloadMessage: should be a server object, not a regular game object")
                  }
                  xs.foreach { item => deleteFunc(item.obj) }
                  val box = x.obj.asInstanceOf[AmmoBox]
                  val tailReloadValue: Int = if (xs.isEmpty) {
                    0
                  } else {
                    xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).sum
                  }
                  val sumReloadValue: Int = box.Capacity + tailReloadValue
                  val actualReloadValue = (if (sumReloadValue <= reloadValue) {
                                             deleteFunc(box)
                                             sumReloadValue
                                           } else {
                                             modifyFunc(box, reloadValue - tailReloadValue)
                                             reloadValue
                                           }) + currentMagazine
                  log.info(s"ReloadMessage: success, $tool <- $actualReloadValue ${tool.AmmoType}")
                  tool.Magazine = actualReloadValue
                  sendResponse(ReloadMessage(item_guid, actualReloadValue, unk1))
                  continent.AvatarEvents ! AvatarServiceMessage(
                    continent.id,
                    AvatarAction.Reload(player.GUID, item_guid)
                  )
              }
            } else {
              log.warn(s"ReloadMessage: item $item_guid can not reload (full=$magazineSize, want=$reloadValue)")
            }
          case (_, Some(_)) =>
            log.error(s"ReloadMessage: the object that was found for $item_guid was not a Tool")
          case (_, None) =>
            log.error(s"ReloadMessage: can not find $item_guid")
        }

      case msg @ ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
        log.debug(s"ObjectHeld: $msg")
        val before = player.DrawnSlot
        if (before != held_holsters) {
          if (player.ExoSuit == ExoSuitType.MAX && held_holsters != 0) {
            log.info(s"ObjectHeld: ${player.Name} is denied changing hands to $held_holsters as a MAX")
            player.DrawnSlot = 0
            sendResponse(ObjectHeldMessage(avatar_guid, 0, true))
          } else if ((player.DrawnSlot = held_holsters) != before) {
            continent.AvatarEvents ! AvatarServiceMessage(
              player.Continent,
              AvatarAction.ObjectHeld(player.GUID, player.LastDrawnSlot)
            )

            // Ignore non-equipment holsters
            //todo: check current suit holster slots?
            if (held_holsters >= 0 && held_holsters < 5) {
              player.Holsters()(held_holsters).Equipment match {
                case Some(unholsteredItem: Equipment) =>
                  if (unholsteredItem.Definition == GlobalDefinitions.remote_electronics_kit) {
                    // Player has unholstered a REK - we need to set an atttribute on the REK itself to change the beam/icon colour to the correct one for the player's hack level
                    continent.AvatarEvents ! AvatarServiceMessage(
                      player.Continent,
                      AvatarAction.PlanetsideAttribute(unholsteredItem.GUID, 116, player.avatar.hackingSkillLevel())
                    )
                  }
                case None => ;
              }
            }

            // Stop using proximity terminals if player unholsters a weapon (which should re-trigger the proximity effect and re-holster the weapon)
            if (player.VisibleSlots.contains(held_holsters)) {
              continent.GUID(usingMedicalTerminal) match {
                case Some(term: Terminal with ProximityUnit) =>
                  StopUsingProximityUnit(term)
                case _ => ;
              }
            }
          }
        }

      case msg @ AvatarJumpMessage(state) =>
        avatarActor ! AvatarActor.ConsumeStamina(10)
        avatarActor ! AvatarActor.SuspendStaminaRegeneration(2.5 seconds)

      case msg @ ZipLineMessage(player_guid, forwards, action, path_id, pos) =>
        log.info("ZipLineMessage: " + msg)
        val (isTeleporter: Boolean, path: Option[ZipLinePath]) =
          continent.zipLinePaths.find(x => x.PathId == path_id) match {
            case Some(x) => (x.IsTeleporter, Some(x))
            case _ =>
              log.warn(s"Couldn't find zipline path ${path_id} in zone ${continent.Number} / ${continent.id}")
              (false, None)
          }
        if (isTeleporter) {
          CancelZoningProcessWithDescriptiveReason("cancel")
          val endPoint = path.get.ZipLinePoints.last
          sendResponse(
            ZipLineMessage(PlanetSideGUID(0), forwards, 0, path_id, pos)
          ) // todo: send to zone to show teleport animation to all clients
          sendResponse(PlayerStateShiftMessage(ShiftState(0, endPoint, player.Orientation.z, None)))
        } else {
          CancelZoningProcessWithDescriptiveReason("cancel_motion")
          action match {
            case 0 =>
              // Travel along the zipline in the direction specified
              sendResponse(ZipLineMessage(player_guid, forwards, action, path_id, pos))
            case 1 =>
              //disembark from zipline at destination !
              sendResponse(ZipLineMessage(player_guid, forwards, action, 0, pos))
            case 2 =>
              //get off by force
              sendResponse(ZipLineMessage(player_guid, forwards, action, 0, pos))
            case _ =>
              log.warn(
                s"Tried to do something with a zipline but can't handle it. forwards: ${forwards} action: ${action} path_id: ${path_id} zone: ${continent.Number} / ${continent.id}"
              )
          }
        }

      case msg @ RequestDestroyMessage(object_guid) =>
        // TODO: Make sure this is the correct response for all cases
        ValidObject(object_guid) match {
          case Some(vehicle: Vehicle) =>
            /* line 1a: player is admin (and overrules other access requirements) */
            /* line 1b: vehicle and player (as the owner) acknowledge each other */
            /* line 1c: vehicle is the same faction as player and either the owner is absent or the vehicle is destroyed */
            /* line 2: vehicle is not mounted in anything or, if it is, its seats are empty */
            if (
              (session.account.gm ||
              (player.avatar.vehicle.contains(object_guid) && vehicle.Owner.contains(player.GUID)) ||
              (player.Faction == vehicle.Faction && ((vehicle.Owner.isEmpty || continent
                .GUID(vehicle.Owner.get)
                .isEmpty) || vehicle.Destroyed))) &&
              (vehicle.MountedIn.isEmpty || !vehicle.Seats.values.exists(_.isOccupied))
            ) {
              vehicle.Actor ! Vehicle.Deconstruct()
              log.info(s"RequestDestroy: vehicle $vehicle")
            } else {
              log.info(s"RequestDestroy: must own vehicle in order to deconstruct it")
            }

          case Some(obj: BoomerTrigger) =>
            if (FindEquipmentToDelete(object_guid, obj)) {
              continent.GUID(obj.Companion) match {
                case Some(boomer: BoomerDeployable) =>
                  boomer.Trigger = None
                  continent.LocalEvents ! LocalServiceMessage.Deployables(
                    RemoverActor.AddTask(boomer, continent, Some(0 seconds))
                  )
                //continent.Deployables ! Zone.Deployable.Dismiss(boomer)
                case Some(thing) =>
                  log.info(s"RequestDestroy: BoomerTrigger object connected to wrong object - $thing")
                case None => ;
              }
            }

          case Some(obj: Equipment) =>
            FindEquipmentToDelete(object_guid, obj)

          case Some(obj: Projectile) =>
            if (obj.isResolved) {
              log.warn(
                s"RequestDestroy: tried to clean up projectile ${object_guid.guid} but it was already resolved"
              )
            } else {
              obj.Miss()
              if (obj.profile.ExistsOnRemoteClients && obj.HasGUID) {
                continent.AvatarEvents ! AvatarServiceMessage(
                  continent.id,
                  AvatarAction.ProjectileExplodes(player.GUID, obj.GUID, obj)
                )
                continent.tasks ! UnregisterProjectile(obj)
              }
            }

          case Some(obj: BoomerDeployable) =>
            continent.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(obj, continent, Some(0 seconds))
            )
            obj.Trigger match {
              case Some(trigger) =>
                obj.Trigger = None
                val guid = trigger.GUID
                Zone.EquipmentIs.Where(trigger, guid, continent) match {
                  case Some(Zone.EquipmentIs.InContainer(container, index)) =>
                    container.Slot(index).Equipment = None
                  case Some(Zone.EquipmentIs.OnGround()) =>
                    continent.Ground ! Zone.Ground.RemoveItem(guid)
                  case Some(Zone.EquipmentIs.Orphaned()) =>
                    log.warn(s"RequestDestroy: boomer_trigger@$guid has been found but it seems to be orphaned")
                  case _ => ;
                }
                continent.AvatarEvents ! AvatarServiceMessage(
                  continent.id,
                  AvatarAction.ObjectDelete(PlanetSideGUID(0), guid)
                )
                GUIDTask.UnregisterObjectTask(trigger)(continent.GUID)

              case None => ;
            }

          case Some(obj: TelepadDeployable) =>
            continent.LocalEvents ! LocalServiceMessage.Telepads(SupportActor.ClearSpecific(List(obj), continent))
            continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(obj), continent))
            continent.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(obj, continent, Some(0 seconds))
            )

          case Some(obj: PlanetSideGameObject with Deployable) =>
            continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(obj), continent))
            continent.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(obj, continent, Some(0 seconds))
            )

          case Some(thing) =>
            log.warn(s"RequestDestroy: not allowed to delete object $thing")

          case None =>
            log.warn(s"RequestDestroy: object ${object_guid.guid} not found")
        }

      case msg @ ObjectDeleteMessage(object_guid, unk1) =>
        sendResponse(ObjectDeleteMessage(object_guid, 0))
        log.info("ObjectDelete: " + msg)

      case msg @ MoveItemMessage(item_guid, source_guid, destination_guid, dest, _) =>
        log.info(s"MoveItem: $msg")
        (continent.GUID(source_guid), continent.GUID(destination_guid), ValidObject(item_guid)) match {
          case (
                Some(source: PlanetSideServerObject with Container),
                Some(destination: PlanetSideServerObject with Container),
                Some(item: Equipment)
              ) =>
            ContainableMoveItem(player.Name, source, destination, item, dest)
          case (None, _, _) =>
            log.error(s"MoveItem: wanted to move $item_guid from $source_guid, but could not find source object")
          case (_, None, _) =>
            log.error(
              s"MoveItem: wanted to move $item_guid to $destination_guid, but could not find destination object"
            )
          case (_, _, None) =>
            log.error(s"MoveItem: wanted to move $item_guid, but could not find it")
          case _ =>
            log.error(
              s"MoveItem: wanted to move $item_guid from $source_guid to $destination_guid, but multiple problems were encountered"
            )
        }

      case msg @ LootItemMessage(item_guid, target_guid) =>
        log.info(s"LootItem: $msg")
        (ValidObject(item_guid), continent.GUID(target_guid)) match {
          case (Some(item: Equipment), Some(destination: PlanetSideServerObject with Container)) =>
            //figure out the source
            (
              {
                val findFunc: PlanetSideServerObject with Container => Option[
                  (PlanetSideServerObject with Container, Option[Int])
                ] = FindInLocalContainer(item_guid)
                findFunc(player.avatar.locker)
                  .orElse(findFunc(player))
                  .orElse(accessedContainer match {
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
                log.error(s"LootItem: can not find where $item is put currently")
              case (_, None) =>
                log.error(s"LootItem: can not find somwhere to put $item in $destination")
              case _ =>
                log.error(
                  s"LootItem: wanted to move $item_guid to $target_guid, but multiple problems were encountered"
                )
            }
          case (Some(obj), _) =>
            log.warn(s"LootItem: item $obj is (probably) not lootable")
          case (None, _) =>
            log.warn(s"LootItem: can not find $item_guid")
          case (_, None) =>
            log.warn(s"LootItem: can not find where to put $item_guid")
        }

      case msg @ AvatarImplantMessage(player_guid, action, slot, status) =>
        if (action == ImplantAction.Activation) {
          CancelZoningProcessWithDescriptiveReason("cancel_implant")
          avatar.implants(slot) match {
            case Some(implant) if implant.initialized =>
              if (!implant.active) {
                avatarActor ! AvatarActor.ActivateImplant(implant.definition.implantType)
              } else {
                avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
              }
            case Some(implant) if !implant.initialized => ()
            case _                                     => log.error(s"AvatarImplantMessage for unknown implant ${msg}")
          }
        }

      case msg @ UseItemMessage(
            avatar_guid,
            item_used_guid,
            object_guid,
            unk2,
            unk3,
            unk4,
            unk5,
            unk6,
            unk7,
            unk8,
            itemType
          ) =>
        //log.info("UseItem: " + msg)
        // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
        // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
        val equipment = player.Slot(player.DrawnSlot).Equipment match {
          case out @ Some(item) if item.GUID == item_used_guid => out
          case _                                               => None
        }
        ValidObject(object_guid) match {
          case Some(door: Door) =>
            door.Actor ! CommonMessages.Use(player)

          case Some(resourceSilo: ResourceSilo) =>
            resourceSilo.Actor ! CommonMessages.Use(player)

          case Some(panel: IFFLock) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                panel.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: Player) =>
            CancelZoningProcessWithDescriptiveReason("cancel_use")
            if (obj.isBackpack) {
              if (equipment.isEmpty) {
                log.info(s"UseItem: ${player.Name} looting the corpse of $obj")
                sendResponse(
                  UseItemMessage(
                    avatar_guid,
                    item_used_guid,
                    object_guid,
                    unk2,
                    unk3,
                    unk4,
                    unk5,
                    unk6,
                    unk7,
                    unk8,
                    itemType
                  )
                )
                AccessContainer(obj)
              }
            } else if (!unk3 && player.isAlive) { //potential kit use
              ValidObject(item_used_guid) match {
                case Some(kit: Kit) =>
                  player.avatar.useCooldown(kit.Definition) match {
                    case Some(cooldown) =>
                      sendResponse(
                        ChatMsg(
                          ChatMessageType.UNK_225,
                          false,
                          "",
                          s"@TimeUntilNextUse^${cooldown.getStandardSeconds}",
                          None
                        )
                      )

                    case None =>
                      val indexOpt = player.Find(kit)
                      val kitIsUsed = indexOpt match {
                        case Some(index) =>
                          if (kit.Definition == GlobalDefinitions.medkit) {
                            if (player.Health == player.MaxHealth) {
                              sendResponse(ChatMsg(ChatMessageType.UNK_225, false, "", "@HealComplete", None))
                              false
                            } else {
                              player.History(HealFromKit(PlayerSource(player), 25, kit.Definition))
                              player.Health = player.Health + 25
                              sendResponse(PlanetsideAttributeMessage(avatar_guid, 0, player.Health))
                              continent.AvatarEvents ! AvatarServiceMessage(
                                continent.id,
                                AvatarAction.PlanetsideAttribute(avatar_guid, 0, player.Health)
                              )
                              true
                            }
                          } else if (kit.Definition == GlobalDefinitions.super_medkit) {
                            if (player.Health == player.MaxHealth) {
                              sendResponse(ChatMsg(ChatMessageType.UNK_225, false, "", "@HealComplete", None))
                              false
                            } else {
                              player.History(HealFromKit(PlayerSource(player), 100, kit.Definition))
                              player.Health = player.Health + 100
                              sendResponse(PlanetsideAttributeMessage(avatar_guid, 0, player.Health))
                              continent.AvatarEvents ! AvatarServiceMessage(
                                continent.id,
                                AvatarAction.PlanetsideAttribute(avatar_guid, 0, player.Health)
                              )
                              true
                            }
                          } else if (kit.Definition == GlobalDefinitions.super_armorkit) {
                            if (player.Armor == player.MaxArmor) {
                              sendResponse(
                                ChatMsg(
                                  ChatMessageType.UNK_225,
                                  false,
                                  "",
                                  "Armor at maximum - No repairing required.",
                                  None
                                )
                              )
                              false
                            } else {
                              player.History(RepairFromKit(PlayerSource(player), 200, kit.Definition))
                              player.Armor = player.Armor + 200
                              sendResponse(PlanetsideAttributeMessage(avatar_guid, 4, player.Armor))
                              continent.AvatarEvents ! AvatarServiceMessage(
                                continent.id,
                                AvatarAction.PlanetsideAttribute(avatar_guid, 4, player.Armor)
                              )
                              true
                            }
                          } else if (kit.Definition == GlobalDefinitions.super_staminakit) {
                            if (player.avatar.staminaFull) {
                              sendResponse(
                                ChatMsg(
                                  ChatMessageType.UNK_225,
                                  false,
                                  "",
                                  "Stamina at maximum - No recharge required.",
                                  None
                                )
                              )
                              false
                            } else {
                              avatarActor ! AvatarActor.RestoreStamina(100)
                              // TODO do we need this? this used to always send the old stamina amount...
                              /*
                              sendResponse(PlanetsideAttributeMessage(avatar_guid, 2, player.Stamina))
                               */
                              true
                            }
                          } else {
                            log.warn(s"UseItem: $kit behavior not supported")
                            false
                          }

                        case None =>
                          log.error(s"UseItem: anticipated a $kit, but can't find it")
                          false
                      }
                      if (kitIsUsed) {
                        //kit was found belonging to player and was used
                        avatarActor ! AvatarActor.UpdateUseTime(kit.Definition)
                        player.Slot(indexOpt.get).Equipment =
                          None //remove from slot immediately; must exist on client for next packet
                        sendResponse(
                          UseItemMessage(
                            avatar_guid,
                            item_used_guid,
                            object_guid,
                            0,
                            unk3,
                            unk4,
                            unk5,
                            unk6,
                            unk7,
                            unk8,
                            itemType
                          )
                        )
                        sendResponse(ObjectDeleteMessage(kit.GUID, 0))
                        continent.tasks ! GUIDTask.UnregisterEquipment(kit)(continent.GUID)
                      }
                  }

                case Some(item) =>
                  log.warn(s"UseItem: looking for Kit to use, but found $item instead")
                case None =>
                  log.warn(s"UseItem: anticipated a Kit $item_used_guid, but can't find it")
              }
            } else if (itemType == ObjectClass.avatar && unk3) {
              equipment match {
                case Some(tool: Tool) if tool.Definition == GlobalDefinitions.bank =>
                  obj.Actor ! CommonMessages.Use(player, equipment)

                case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
                  obj.Actor ! CommonMessages.Use(player, equipment)
                case _ => ;
              }
            }

          case Some(locker: Locker) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                locker.Actor ! CommonMessages.Use(player, Some(item))
              case None if locker.Faction == player.Faction || !locker.HackedBy.isEmpty =>
                log.trace(s"UseItem: ${player.Name} accessing a locker")
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                val playerLocker = player.avatar.locker
                sendResponse(
                  UseItemMessage(
                    avatar_guid,
                    item_used_guid,
                    playerLocker.GUID,
                    unk2,
                    unk3,
                    unk4,
                    unk5,
                    unk6,
                    unk7,
                    unk8,
                    456
                  )
                )
                AccessContainer(playerLocker)
              case _ => ;
            }

          case Some(gen: Generator) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                gen.Actor ! CommonMessages.Use(player, Some(item))
              case None => ;
            }

          case Some(mech: ImplantTerminalMech) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                mech.Actor ! CommonMessages.Use(player, Some(item))
              case None => ;
            }

          case Some(captureTerminal: CaptureTerminal) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                captureTerminal.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: FacilityTurret) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))               //try generic
                obj.Actor ! CommonMessages.Use(player, Some((item, unk2.toInt))) //try upgrade path
              case _ => ;
            }

          case Some(obj: Vehicle) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))

              case None if player.Faction == obj.Faction =>
                //access to trunk
                if (
                  obj.AccessingTrunk.isEmpty &&
                  (!obj.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked) || obj.Owner
                    .contains(player.GUID))
                ) {
                  CancelZoningProcessWithDescriptiveReason("cancel_use")
                  obj.AccessingTrunk = player.GUID
                  AccessContainer(obj)
                  sendResponse(
                    UseItemMessage(
                      avatar_guid,
                      item_used_guid,
                      object_guid,
                      unk2,
                      unk3,
                      unk4,
                      unk5,
                      unk6,
                      unk7,
                      unk8,
                      itemType
                    )
                  )
                }
              case _ => ;
            }

          case Some(terminal: Terminal) =>
            log.info(s"$msg")
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                terminal.Actor ! CommonMessages.Use(player, Some(item))

              case None
                  if terminal.Owner == Building.NoBuilding || terminal.Faction == player.Faction || terminal.HackedBy.nonEmpty =>
                val tdef = terminal.Definition
                if (tdef.isInstanceOf[MatrixTerminalDefinition]) {
                  //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
                  CancelZoningProcessWithDescriptiveReason("cancel_use")
                  sendResponse(
                    BindPlayerMessage(BindStatus.Bind, "", true, true, SpawnGroup.Sanctuary, 0, 0, terminal.Position)
                  )
                } else if (
                  tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
                  tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal
                ) {
                  FindLocalVehicle match {
                    case Some(vehicle) =>
                      sendResponse(
                        UseItemMessage(
                          avatar_guid,
                          item_used_guid,
                          object_guid,
                          unk2,
                          unk3,
                          unk4,
                          unk5,
                          unk6,
                          unk7,
                          unk8,
                          itemType
                        )
                      )
                      sendResponse(
                        UseItemMessage(
                          avatar_guid,
                          item_used_guid,
                          vehicle.GUID,
                          unk2,
                          unk3,
                          unk4,
                          unk5,
                          unk6,
                          unk7,
                          unk8,
                          vehicle.Definition.ObjectId
                        )
                      )
                    case None =>
                      log.error("UseItem: expected seated vehicle, but found none")
                  }
                } else if (tdef == GlobalDefinitions.teleportpad_terminal) {
                  //explicit request
                  CancelZoningProcessWithDescriptiveReason("cancel_use")
                  terminal.Actor ! Terminal.Request(
                    player,
                    ItemTransactionMessage(object_guid, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
                  )
                } else {
                  CancelZoningProcessWithDescriptiveReason("cancel_use")
                  sendResponse(
                    UseItemMessage(
                      avatar_guid,
                      item_used_guid,
                      object_guid,
                      unk2,
                      unk3,
                      unk4,
                      unk5,
                      unk6,
                      unk7,
                      unk8,
                      itemType
                    )
                  )
                }

              case _ => ;
            }

          case Some(obj: SpawnTube) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))
              case None if player.Faction == obj.Faction =>
                //deconstruction
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                PlayerActionsToCancel()
                CancelAllProximityUnits()
                GoToDeploymentMap()
              case _ => ;
            }

          case Some(obj: SensorDeployable) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: TurretDeployable) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: TrapDeployable) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: ShieldGeneratorDeployable) =>
            equipment match {
              case Some(item) =>
                CancelZoningProcessWithDescriptiveReason("cancel_use")
                obj.Actor ! CommonMessages.Use(player, Some(item))
              case _ => ;
            }

          case Some(obj: TelepadDeployable) =>
            if (equipment.isEmpty) {
              continent.GUID(obj.Router) match {
                case Some(vehicle: Vehicle) =>
                  vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                    case Some(util: Utility.InternalTelepad) =>
                      CancelZoningProcessWithDescriptiveReason("cancel")
                      UseRouterTelepadSystem(
                        router = vehicle,
                        internalTelepad = util,
                        remoteTelepad = obj,
                        src = obj,
                        dest = util
                      )
                    case _ =>
                      log.error(
                        s"telepad@${object_guid.guid} is not linked to a router - ${vehicle.Definition.Name}, ${obj.Router}"
                      )
                  }
                case Some(o) =>
                  log.error(
                    s"telepad@${object_guid.guid} is linked to wrong kind of object - ${o.Definition.Name}, ${obj.Router}"
                  )
                case None => ;
              }
            }

          case Some(obj: Utility.InternalTelepad) =>
            continent.GUID(obj.Telepad) match {
              case Some(pad: TelepadDeployable) =>
                CancelZoningProcessWithDescriptiveReason("cancel")
                UseRouterTelepadSystem(
                  router = obj.Owner.asInstanceOf[Vehicle],
                  internalTelepad = obj,
                  remoteTelepad = pad,
                  src = obj,
                  dest = pad
                )
              case Some(o) =>
                log.error(
                  s"internal telepad@${object_guid.guid} is not linked to a remote telepad - ${o.Definition.Name}@${o.GUID.guid}"
                )
              case None => ;
            }

          case Some(obj) =>
            CancelZoningProcessWithDescriptiveReason("cancel_use")
            log.warn(s"UseItem: don't know how to handle $obj")

          case None =>
            log.error(s"UseItem: can not find object $object_guid")
        }

      case msg @ ProximityTerminalUseMessage(player_guid, object_guid, _) =>
        log.trace(s"ProximityTerminalUse: $msg")
        continent.GUID(object_guid) match {
          case Some(obj: Terminal with ProximityUnit) =>
            HandleProximityTerminalUse(obj)
          case Some(obj) => ;
            log.warn(s"ProximityTerminalUse: object does not have proximity effects - $obj")
          case None =>
            log.warn(s"ProximityTerminalUse: no object with guid $object_guid found")
        }

      case msg @ UnuseItemMessage(player_guid, object_guid) =>
        log.info(s"UnuseItem: $msg")
        ValidObject(object_guid) match {
          case Some(obj: Player) =>
            UnaccessContainer(obj)
            TryDisposeOfLootedCorpse(obj)

          case Some(obj: Container) =>
            UnaccessContainer(obj)

          case _ => ;
        }

      case msg @ DeployObjectMessage(guid, unk1, pos, orient, unk2) =>
        log.info(s"DeployObject: $msg")
        //the hand with the construction item is no longer drawn
        //TODO consider player.Slot(player.LastDrawnSlot)
        (player.Holsters().find(slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid) match {
          case Some(slot) =>
            slot.Equipment
          case None =>
            None
        }) match {
          case Some(obj: ConstructionItem) =>
            val ammoType = obj.AmmoType match {
              case DeployedItem.portable_manned_turret =>
                GlobalDefinitions.PortableMannedTurret(player.Faction).Item //faction-specific turret
              case turret =>
                turret
            }
            log.info(s"DeployObject: Constructing a ${ammoType}")
            CancelZoningProcessWithDescriptiveReason("cancel_use")
            val dObj: PlanetSideGameObject with Deployable = Deployables.Make(ammoType)()
            dObj.Position = pos
            dObj.Orientation = orient
            dObj.Faction = player.Faction
            dObj.AssignOwnership(player)
            val tasking: TaskResolver.GiveTask = dObj match {
              case turret: TurretDeployable =>
                GUIDTask.RegisterDeployableTurret(turret)(continent.GUID)
              case _ =>
                GUIDTask.RegisterObjectTask(dObj)(continent.GUID)
            }
            continent.tasks ! CallBackForTask(tasking, continent.Deployables, Zone.Deployable.Build(dObj, obj))

          case Some(obj) =>
            log.warn(s"DeployObject: $obj is something?")
          case None =>
            log.warn("DeployObject: nothing?")
        }

      case msg @ GenericObjectActionMessage(object_guid, code) =>
        //log.info(s"$msg")
        ValidObject(object_guid) match {
          case Some(tool: Tool) =>
            if (tool.Definition == GlobalDefinitions.maelstrom && code == 35) {
              //maelstrom primary fire mode effect (no target)
              HandleWeaponFireAccountability(object_guid, PlanetSideGUID(Projectile.baseUID))
            }
          case _ =>
            log.info(s"$msg")
        }

      case msg @ GenericObjectStateMsg(object_guid, unk1) =>
        log.info("GenericObjectState: " + msg)

      case msg @ GenericActionMessage(action) =>
        log.info(s"GenericAction: $msg")
        if (player == null) {
          if (action == 29) {
            log.info("AFK state reported during login")
          }
        } else {
          val (toolOpt, definition) = player.Slot(0).Equipment match {
            case Some(tool: Tool) =>
              (Some(tool), tool.Definition)
            case _ =>
              (None, GlobalDefinitions.bullet_9mm)
          }
          if (action == 29) {
            log.info(s"${player.Name} is AFK")
            player.AwayFromKeyboard = true
          } else if (action == 30) {
            log.info(s"${player.Name} is back")
            player.AwayFromKeyboard = false
          }
          if (action == 15) { //max deployment
            log.info(s"GenericObject: $player is anchored")
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
                log.warn(s"GenericObject: $player is MAX with an unexpected weapon - ${definition.Name}")
            }
          } else if (action == 16) { //max deployment
            log.info(s"GenericObject: $player has released the anchors")
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
                val convertFireModeIndex = if (tool.FireModeIndex == 1) { 0 }
                else { 3 }
                tool.ToFireMode = convertFireModeIndex
                sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
              case _ =>
                log.warn(s"GenericObject: $player is MAX with an unexpected weapon - ${definition.Name}")
            }
          } else if (action == 20) {
            if (player.ExoSuit == ExoSuitType.MAX) {
              ToggleMaxSpecialState(enable = true)
            } else {
              log.warn("Got GenericActionMessage 20 but can't handle it")
            }
          } else if (action == 21) {
            if (player.ExoSuit == ExoSuitType.MAX) {
              player.Faction match {
                case PlanetSideEmpire.NC =>
                  ToggleMaxSpecialState(enable = false)
                case _ => log.warn(s"Player ${player.Name} tried to cancel an uncancellable MAX special ability")
              }
            } else {
              log.warn("Got GenericActionMessage 21 but can't handle it")
            }
          } else if (action == 36) { //Looking For Squad ON
            if (squadUI.nonEmpty) {
              if (!lfsm && squadUI(player.CharId).index == 0) {
                lfsm = true
                continent.AvatarEvents ! AvatarServiceMessage(
                  s"${player.Faction}",
                  AvatarAction.PlanetsideAttribute(player.GUID, 53, 1)
                )
              }
            } else if (!avatar.lookingForSquad) {
              avatarActor ! AvatarActor.SetLookingForSquad(true)
            }
          } else if (action == 37) { //Looking For Squad OFF
            if (squadUI.nonEmpty) {
              if (lfsm && squadUI(player.CharId).index == 0) {
                lfsm = false
                continent.AvatarEvents ! AvatarServiceMessage(
                  s"${player.Faction}",
                  AvatarAction.PlanetsideAttribute(player.GUID, 53, 0)
                )
              }
            } else if (avatar.lookingForSquad) {
              avatarActor ! AvatarActor.SetLookingForSquad(false)
            }
          }
        }

      case msg @ ItemTransactionMessage(terminal_guid, transaction_type, _, _, _, _) =>
        log.info("ItemTransaction: " + msg)
        continent.GUID(terminal_guid) match {
          case Some(term: Terminal) =>
            log.info(s"ItemTransaction: ${term.Definition.Name} found")
            if (lastTerminalOrderFulfillment) {
              lastTerminalOrderFulfillment = false
              CancelZoningProcessWithDescriptiveReason("cancel_use")
              term.Actor ! Terminal.Request(player, msg)
            }
          case Some(obj: PlanetSideGameObject) =>
            log.error(s"ItemTransaction: $obj is not a terminal")
          case _ =>
            log.error(s"ItemTransaction: $terminal_guid does not exist")
        }

      case msg @ FavoritesRequest(player_guid, loadoutType, action, line, label) =>
        CancelZoningProcessWithDescriptiveReason("cancel_use")
        log.info(s"FavoritesRequest: $msg")
        action match {
          case FavoritesAction.Save    => avatarActor ! AvatarActor.SaveLoadout(player, loadoutType, label, line)
          case FavoritesAction.Delete  => avatarActor ! AvatarActor.DeleteLoadout(player, loadoutType, line)
          case FavoritesAction.Unknown => log.warn("FavoritesRequest: unknown favorites action")
        }

      case msg @ WeaponDelayFireMessage(seq_time, weapon_guid) =>
        log.info("WeaponDelayFire: " + msg)

      case msg @ WeaponDryFireMessage(weapon_guid) =>
        log.debug("WeaponDryFireMessage: " + msg)
        FindWeapon match {
          case Some(tool: Tool) =>
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.WeaponDryFire(player.GUID, weapon_guid)
            )
          case _ => ;
        }

      case msg @ WeaponFireMessage(
            seq_time,
            weapon_guid,
            projectile_guid,
            shot_origin,
            unk1,
            unk2,
            unk3,
            unk4,
            unk5,
            unk6,
            unk7
          ) =>
        log.debug(s"WeaponFire: $msg")
        HandleWeaponFire(weapon_guid, projectile_guid, shot_origin)

      case msg @ WeaponLazeTargetPositionMessage(weapon, pos1, pos2) =>
        log.info("Lazing position: " + pos2.toString)

      case msg @ ObjectDetectedMessage(guid1, guid2, unk, targets) =>
        //log.info(s"Detection: $msg")
        FindWeapon match {
          case Some(weapon) if weapon.Projectile.AutoLock =>
            //projectile with auto-lock instigates a warning on the target
            val detectedTargets = FindDetectedProjectileTargets(targets)
            if (detectedTargets.nonEmpty) {
              val mode = 7 + (weapon.Projectile == GlobalDefinitions.wasp_rocket_projectile)
              detectedTargets.foreach { target =>
                continent.AvatarEvents ! AvatarServiceMessage(target, AvatarAction.ProjectileAutoLockAwareness(mode))
              }
            }
          case _ => ;
        }

      case msg @ HitMessage(
            seq_time,
            projectile_guid,
            unk1,
            hit_info,
            unk2,
            unk3,
            unk4
          ) =>
        log.info(s"Hit: $msg")
        //find defined projectile
        FindProjectileEntry(projectile_guid) match {
          case Some(projectile) =>
            //find target(s)
            (hit_info match {
              case Some(hitInfo) =>
                ValidObject(hitInfo.hitobject_guid) match {
                  case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
                    CheckForHitPositionDiscrepancy(projectile_guid, hitInfo.hit_pos, target)
                    List((target, hitInfo.shot_origin, hitInfo.hit_pos))

                  case None if projectile.profile.DamageProxy.getOrElse(0) > 0 =>
                    //server-side maelstrom grenade target selection
                    if (projectile.tool_def == GlobalDefinitions.maelstrom) {
                      val hitPos     = hitInfo.hit_pos
                      val shotOrigin = hitInfo.shot_origin
                      val radius     = projectile.profile.LashRadius * projectile.profile.LashRadius
                      val targets = continent.LivePlayers.filter { target =>
                        Vector3.DistanceSquared(target.Position, hitPos) <= radius
                      }
                      //chainlash is separated from the actual damage application for convenience
                      continent.AvatarEvents ! AvatarServiceMessage(
                        continent.id,
                        AvatarAction.SendResponse(
                          PlanetSideGUID(0),
                          ChainLashMessage(
                            hitPos,
                            projectile.profile.ObjectId,
                            targets.map {
                              _.GUID
                            }.toList
                          )
                        )
                      )
                      targets.map { target =>
                        CheckForHitPositionDiscrepancy(projectile_guid, hitPos, target)
                        (target, hitPos, target.Position)
                      }
                    } else {
                      Nil
                    }
                  case _ =>
                    Nil
                }
              case None =>
                Nil
            })
              .foreach({
                case (
                      target: PlanetSideGameObject with FactionAffinity with Vitality,
                      shotOrigin: Vector3,
                      hitPos: Vector3
                    ) =>
                  ResolveProjectileEntry(projectile, ProjectileResolution.Hit, target, hitPos) match {
                    case Some(resprojectile) =>
                      HandleDealingDamage(target, resprojectile)
                    case None => ;
                  }
                case _ => ;
              })
          case None =>
            log.warn(s"ResolveProjectile: expected projectile, but ${projectile_guid.guid} not found")
        }

      case msg @ SplashHitMessage(
            seq_time,
            projectile_guid,
            explosion_pos,
            direct_victim_uid,
            unk3,
            projectile_vel,
            unk4,
            targets
          ) =>
        log.info(s"Splash: $msg")
        FindProjectileEntry(projectile_guid) match {
          case Some(projectile) =>
            val profile = projectile.profile
            projectile.Position = explosion_pos
            projectile.Velocity = projectile_vel
            val (resolution1, resolution2) = profile.Aggravated match {
              case Some(_) if profile.ProjectileDamageTypes.contains(DamageType.Aggravated) =>
                (ProjectileResolution.AggravatedDirect, ProjectileResolution.AggravatedSplash)
              case _ =>
                (ProjectileResolution.Splash, ProjectileResolution.Splash)
            }
            //direct_victim_uid
            ValidObject(direct_victim_uid) match {
              case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
                CheckForHitPositionDiscrepancy(projectile_guid, target.Position, target)
                ResolveProjectileEntry(projectile, resolution1, target, target.Position) match {
                  case Some(projectile) =>
                    HandleDealingDamage(target, projectile)
                  case None => ;
                }
              case _ => ;
            }
            //other victims
            targets.foreach(elem => {
              ValidObject(elem.uid) match {
                case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
                  CheckForHitPositionDiscrepancy(projectile_guid, explosion_pos, target)
                  ResolveProjectileEntry(projectile, resolution2, target, explosion_pos) match {
                    case Some(projectile) =>
                      HandleDealingDamage(target, projectile)
                    case None => ;
                  }
                case _ => ;
              }
            })
            if (profile.ExistsOnRemoteClients && projectile.HasGUID) {
              //cleanup
              val localIndex = projectile_guid.guid - Projectile.baseUID
              if (projectile.HasGUID) {
                CleanUpRemoteProjectile(projectile.GUID, projectile, localIndex)
              } else {
                projectilesToCleanUp(localIndex) = true
              }
            }
          case None => ;
        }

      case msg @ LashMessage(seq_time, killer_guid, victim_guid, projectile_guid, hit_pos, unk1) =>
        log.info(s"Lash: $msg")
        ValidObject(victim_guid) match {
          case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
            CheckForHitPositionDiscrepancy(projectile_guid, hit_pos, target)
            ResolveProjectileEntry(projectile_guid, ProjectileResolution.Lash, target, hit_pos) match {
              case Some(projectile) =>
                HandleDealingDamage(target, projectile)
              case None => ;
            }
          case _ => ;
        }

      case msg @ AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, eventName) =>
        avatarActor ! AvatarActor.AddFirstTimeEvent(eventName)

      case msg @ WarpgateRequest(
            continent_guid,
            building_guid,
            destinationBuildingGuid,
            destinationZoneGuid,
            unk1,
            unk2
          ) =>
        log.info(s"WarpgateRequest: $msg")
        CancelZoningProcessWithDescriptiveReason("cancel_use")
        if (deadState != DeadState.RespawnTime) {
          continent.Buildings.values.find(building => building.GUID == building_guid) match {
            case Some(wg: WarpGate) if (wg.Active && (GetKnownVehicleAndSeat() match {
                  case (Some(vehicle), _) =>
                    wg.Definition.VehicleAllowance && !wg.Definition.NoWarp.contains(vehicle.Definition)
                  case _ =>
                    true
                })) =>
              deadState = DeadState.RespawnTime
              cluster ! InterstellarClusterService.GetSpawnPoint(
                destinationZoneGuid.guid,
                player,
                destinationBuildingGuid,
                context.self
              )

            case Some(wg: WarpGate) if (!wg.Active) =>
              log.info(s"WarpgateRequest: inactive WarpGate")

            case _ =>
              deadState = DeadState.RespawnTime
              RequestSanctuaryZoneSpawn(player, continent.Number)
          }
        } else {
          log.warn("WarpgateRequest: request consumed; already respawning ...")
        }

      case msg @ MountVehicleMsg(player_guid, mountable_guid, entry_point) =>
        log.info("MountVehicleMsg: " + msg)
        ValidObject(mountable_guid) match {
          case Some(obj: Mountable) =>
            obj.GetSeatFromMountPoint(entry_point) match {
              case Some(seat_num) =>
                obj.Actor ! Mountable.TryMount(player, seat_num)
              case None =>
                log.warn(
                  s"MountVehicleMsg: attempted to board mountable $mountable_guid's seat $entry_point, but no seat exists there"
                )
            }
          case None | Some(_) =>
            log.warn(s"MountVehicleMsg: not a mountable thing")
        }

      case msg @ DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) =>
        //TODO optimize this later
        log.info(s"DismountVehicleMsg: $msg")
        //common warning for this section
        def dismountWarning(msg: String): Unit = {
          log.warn(s"$msg; some vehicle might not know that a player is no longer sitting in it")
        }
        if (player.GUID == player_guid) {
          //normally disembarking from a seat
          (interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
            case out @ Some(obj: Vehicle) =>
              if (obj.MountedIn.isEmpty) out else None
            case out @ Some(_: Mountable) =>
              out
            case _ =>
              dismountWarning(
                s"DismountVehicleMsg: player ${player.Name}_guid not considered seated in a mountable entity"
              )
              None
          }) match {
            case Some(obj: Mountable) =>
              obj.PassengerInSeat(player) match {
                case Some(0) if controlled.nonEmpty =>
                  log.warn(
                    s"DismountVehicleMsg: can not dismount from vehicle as driver while server has asserted control; please wait ..."
                  )
                case Some(seat_num) =>
                  obj.Actor ! Mountable.TryDismount(player, seat_num)
                  if (interstellarFerry.isDefined) {
                    //short-circuit the temporary channel for transferring between zones, the player is no longer doing that
                    //see above in VehicleResponse.TransferPassenger case
                    interstellarFerry = None
                  }
                  // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
                  //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
                  //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
                  //todo: kick cargo passengers out. To be added after PR #216 is merged
                  obj match {
                    case v: Vehicle if bailType == BailType.Bailed && seat_num == 0 && v.Flying =>
                      v.Actor ! Vehicle.Deconstruct(None) //immediate deconstruction
                    case _ => ;
                  }

                case None =>
                  dismountWarning(
                    s"DismountVehicleMsg: can not find where player ${player.Name}_guid is seated in mountable ${player.VehicleSeated}"
                  )
              }
            case _ =>
              dismountWarning(s"DismountVehicleMsg: can not find mountable entity ${player.VehicleSeated}")
          }
        } else {
          //kicking someone else out of a seat; need to own that seat/mountable
          player.avatar.vehicle match {
            case Some(obj_guid) =>
              ((ValidObject(obj_guid), ValidObject(player_guid)) match {
                case (vehicle @ Some(obj: Vehicle), tplayer) =>
                  if (obj.MountedIn.isEmpty) (vehicle, tplayer) else (None, None)
                case (mount @ Some(obj: Mountable), tplayer) =>
                  (mount, tplayer)
                case _ =>
                  (None, None)
              }) match {
                case (Some(obj: Mountable), Some(tplayer: Player)) =>
                  obj.PassengerInSeat(tplayer) match {
                    case Some(seat_num) =>
                      obj.Actor ! Mountable.TryDismount(tplayer, seat_num)
                    case None =>
                      dismountWarning(
                        s"DismountVehicleMsg: can not find where other player ${player.Name}_guid is seated in mountable $obj_guid"
                      )
                  }
                case (None, _) => ;
                  log.warn(s"DismountVehicleMsg: ${player.Name} can not find his vehicle")
                case (_, None) => ;
                  log.warn(s"DismountVehicleMsg: player $player_guid could not be found to kick")
                case _ =>
                  log.warn(s"DismountVehicleMsg: object is either not a Mountable or not a Player")
              }
            case None =>
              log.warn(s"DismountVehicleMsg: ${player.Name} does not own a vehicle")
          }
        }

      case msg @ DeployRequestMessage(player_guid, vehicle_guid, deploy_state, unk2, unk3, pos) =>
        log.info(s"DeployRequest: $msg")
        if (player.avatar.vehicle.contains(vehicle_guid) && player.avatar.vehicle == player.VehicleSeated) {
          continent.GUID(vehicle_guid) match {
            case Some(obj: Vehicle) =>
              obj.Actor ! Deployment.TryDeploymentChange(deploy_state)

            case _ =>
              log.error(s"DeployRequest: can not find $vehicle_guid in scope")
              avatarActor ! AvatarActor.SetVehicle(None)
          }
        } else {
          log.warn(s"DeployRequest: ${player.Name} does not own the deploying $vehicle_guid object")
        }

      case msg @ AvatarGrenadeStateMessage(player_guid, state) =>
        log.info("AvatarGrenadeStateMessage: " + msg)

      case msg @ SquadDefinitionActionMessage(u1, u2, action) =>
        log.info(s"SquadDefinitionAction: $msg")
        squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Definition(u1, u2, action))

      case msg @ SquadMembershipRequest(request_type, char_id, unk3, player_name, unk5) =>
        log.info(s"$msg")
        squadService ! SquadServiceMessage(
          player,
          continent,
          SquadServiceAction.Membership(request_type, char_id, unk3, player_name, unk5)
        )

      case msg @ SquadWaypointRequest(request, _, wtype, unk, info) =>
        log.info(s"Waypoint Request: $msg")
        squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Waypoint(request, wtype, unk, info))

      case msg @ GenericCollisionMsg(u1, p, t, php, thp, pv, tv, ppos, tpos, u2, u3, u4) =>
        log.debug("Ouch! " + msg)

      case msg @ BugReportMessage(
            version_major,
            version_minor,
            version_date,
            bug_type,
            repeatable,
            location,
            zone,
            pos,
            summary,
            desc
          ) =>
        log.info("BugReportMessage: " + msg)

      case msg @ BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        log.info("BindPlayerMessage: " + msg)

      case msg @ PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value) =>
        log.info("PlanetsideAttributeMessage: " + msg)
        ValidObject(object_guid) match {
          case Some(vehicle: Vehicle) =>
            if (player.avatar.vehicle.contains(vehicle.GUID)) {
              if (9 < attribute_type && attribute_type < 14) {
                vehicle.PermissionGroup(attribute_type, attribute_value) match {
                  case Some(allow) =>
                    val group = AccessPermissionGroup(attribute_type - 10)
                    log.info(s"Vehicle attributes: vehicle ${vehicle.GUID} access permission $group changed to $allow")
                    continent.VehicleEvents ! VehicleServiceMessage(
                      continent.id,
                      VehicleAction.SeatPermissions(player.GUID, vehicle.GUID, attribute_type, attribute_value)
                    )
                    //kick players who should not be seated in the vehicle due to permission changes
                    if (allow == VehicleLockState.Locked) { //TODO only important permission atm
                      vehicle.Definition.MountPoints.values
                        .foreach(mountpoint_num => {
                          vehicle.Seat(mountpoint_num) match {
                            case Some(seat) =>
                              seat.Occupant match {
                                case Some(tplayer) =>
                                  if (
                                    vehicle.SeatPermissionGroup(mountpoint_num).contains(group) && tplayer != player
                                  ) { //can not kick self
                                    seat.Occupant = None
                                    tplayer.VehicleSeated = None
                                    continent.VehicleEvents ! VehicleServiceMessage(
                                      continent.id,
                                      VehicleAction.KickPassenger(tplayer.GUID, 4, false, object_guid)
                                    )
                                  }
                                case None => ; // No player seated
                              }
                            case None => ; // Not a seat mounting point
                          }
                          vehicle.CargoHold(mountpoint_num) match {
                            case Some(cargo) =>
                              cargo.Occupant match {
                                case Some(vehicle) =>
                                  if (vehicle.SeatPermissionGroup(mountpoint_num).contains(group)) {
                                    //todo: this probably doesn't work for passengers within the cargo vehicle
                                    // Instruct client to start bail dismount procedure
                                    self ! DismountVehicleCargoMsg(player.GUID, vehicle.GUID, true, false, false)
                                  }
                                case None => ; // No vehicle in cargo
                              }
                            case None => ; // Not a cargo mounting point
                          }

                        })
                    }
                  case None => ;
                }
              } else {
                log.warn(s"Vehicle attributes: unsupported change on vehicle $object_guid - $attribute_type")
              }
            } else {
              log.warn(s"Vehicle attributes: ${player.Name} does not own vehicle ${vehicle.GUID} and can not change it")
            }

          // Cosmetics options
          case Some(player: Player) if attribute_type == 106 =>
            avatarActor ! AvatarActor.SetCosmetics(Cosmetic.valuesFromAttributeValue(attribute_value))

          case _ =>
            log.warn(s"echo unknown attributes behavior")
            sendResponse(PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value))
        }

      case msg @ FacilityBenefitShieldChargeRequestMessage(guid) =>
        player.VehicleSeated match {
          case Some(vehicleGUID) =>
            continent.GUID(vehicleGUID) match {
              case Some(obj: Vehicle) =>
                if (!obj.Destroyed) { //vehicle will try to charge even if destroyed
                  obj.Actor ! Vehicle.ChargeShields(15)
                }
              case _ =>
                log.warn(
                  s"FacilityBenefitShieldChargeRequest: can not find vehicle ${vehicleGUID.guid} in zone ${continent.id}"
                )
            }
          case None =>
            log.warn(s"FacilityBenefitShieldChargeRequest: player ${player.Name} is not seated in a vehicle")
        }

      case msg @ BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        log.info("Battleplan: " + msg)

      case msg @ CreateShortcutMessage(player_guid, slot, unk, add, shortcut) =>
        log.debug("CreateShortcutMessage: " + msg)

      case msg @ FriendsRequest(action, friend) =>
        log.info("FriendsRequest: " + msg)

      case msg @ HitHint(source_guid, player_guid) =>
        log.trace(s"HitHint: $msg") //HitHint is manually distributed for proper operation

      case msg @ TargetingImplantRequest(list) =>
        log.info("TargetingImplantRequest: " + msg)
        val targetInfo: List[TargetInfo] = list.flatMap(x => {
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
              log.warn(s"Target info requested for guid ${x.target_guid} but is not a player")
              None
          }
        })
        sendResponse(TargetingInfoMessage(targetInfo))

      case msg @ ActionCancelMessage(u1, u2, u3) =>
        log.info("Cancelled: " + msg)
        progressBarUpdate.cancel()
        progressBarValue = None

      case default => log.error(s"Unhandled GamePacket $pkt")
    }

  /**
    * Construct tasking that registers all aspects of a `Player` avatar
    * as if that player is only just being introduced.
    * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
    * @param tplayer the avatar `Player`
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterNewAvatar(tplayer: Player): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localPlayer   = tplayer
        private val localAnnounce = self

        override def Description: String = s"register new player avatar ${localPlayer.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (localPlayer.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          log.info(s"Player $localPlayer is registered")
          resolver ! Success(this)
          localAnnounce ! NewPlayerLoaded(localPlayer) //alerts WorldSessionActor
        }

        override def onFailure(ex: Throwable): Unit = {
          localAnnounce ! PlayerFailedToLoad(localPlayer) //alerts SessionActor
        }
      },
      List(GUIDTask.RegisterAvatar(tplayer)(continent.GUID))
    )
  }

  /**
    * Construct tasking that registers all aspects of a `Player` avatar
    * as if that player was already introduced and is just being renewed.
    * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
    * @param tplayer the avatar `Player`
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterAvatar(tplayer: Player): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localPlayer   = tplayer
        private val localAnnounce = self

        override def Description: String = s"register player avatar ${localPlayer.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (localPlayer.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          log.info(s"Player $localPlayer is registered")
          resolver ! Success(this)
          localAnnounce ! PlayerLoaded(localPlayer) //alerts WorldSessionActor
        }

        override def onFailure(ex: Throwable): Unit = {
          localAnnounce ! PlayerFailedToLoad(localPlayer) //alerts WorldSessionActor
        }
      },
      List(GUIDTask.RegisterPlayer(tplayer)(continent.GUID))
    )
  }

  /**
    * Construct tasking that adds a completed and registered vehicle into the scene.
    * Use this function to renew the globally unique identifiers on a vehicle that has already been added to the scene once.
    * @param vehicle the `Vehicle` object
    * @see `RegisterVehicleFromSpawnPad`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterVehicle(vehicle: Vehicle): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = vehicle

        override def Description: String = s"register a ${localVehicle.Definition.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (localVehicle.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          log.info(s"Vehicle $localVehicle is registered")
          resolver ! Success(this)
        }
      },
      List(GUIDTask.RegisterVehicle(vehicle)(continent.GUID))
    )
  }

  /**
    * Use this function to facilitate registering a droppod for a globally unique identifier
    * in the event that the user has instigated an instant action event to a destination within the current zone.<br>
    * <br>
    * If going to another zone instead,
    * this is uneccessary as the normal vehicle gating protocol is partially intersected for droppod operation,
    * and will properly register the droppod before introducing it into the new zone without additional concern.
    * The droppod should actually not be completely unregistered.
    * If inquired, it will act like a GUID had already been assigned to it, but it was invalidated.
    * This condition is artificial, but it necessary to pass certain operations related to vehicle gating.
    * Additionally, the driver is only partially associated with the vehicle at this time.
    * `interstellarFerry` is properly keeping track of the vehicle during the transition
    * and the user who is the driver (second param) is properly seated
    * but the said driver does not know about the vehicle through his usual convention - `VehicleSeated` - yet.
    * @see `GlobalDefinitions.droppod`
    * @see `GUIDTask.RegisterObjectTask`
    * @see `interstellarFerry`
    * @see `Player.VehicleSeated`
    * @see `PlayerLoaded`
    * @see `TaskResolver.GiveTask`
    * @see `Vehicles.Own`
    * @param vehicle the unregistered droppod
    * @param tplayer the player using the droppod for instant action;
    *                should already be the driver of the droppod
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterDroppod(vehicle: Vehicle, tplayer: Player): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localDriver   = tplayer
        private val localVehicle  = vehicle
        private val localAnnounce = self

        override def Description: String = s"register a ${localVehicle.Definition.Name} manned by ${localDriver.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (localVehicle.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          log.info(s"Vehicle $localVehicle is registered")
          localDriver.VehicleSeated = localVehicle.GUID
          Vehicles.Own(localVehicle, localDriver)
          localAnnounce ! PlayerLoaded(localDriver)
          resolver ! Success(this)
        }
      },
      List(GUIDTask.RegisterObjectTask(vehicle)(continent.GUID))
    )
  }

  /**
    * Construct tasking that adds a completed and registered vehicle into the scene.
    * The major difference between `RegisterVehicle` and `RegisterVehicleFromSpawnPad` is the assumption that this vehicle lacks an internal `Actor`.
    * Before being finished, that vehicle is supplied an `Actor` such that it may function properly.
    * This function wraps around `RegisterVehicle` and is used in case, prior to this event,
    * the vehicle is being brought into existence from scratch and was never a member of any `Zone`.
    * @param obj the `Vehicle` object
    * @see `RegisterVehicle`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterVehicleFromSpawnPad(obj: Vehicle, pad: VehicleSpawnPad): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localPad     = pad.Actor
        private val localPlayer  = player

        override def Description: String = s"register a ${localVehicle.Definition.Name} for spawn pad"

        override def isComplete: Task.Resolution.Value = {
          if (localVehicle.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          localPad ! VehicleSpawnPad.VehicleOrder(localPlayer, localVehicle)
          resolver ! Success(this)
        }
      },
      List(RegisterVehicle(obj))
    )
  }

  def RegisterDrivenVehicle(obj: Vehicle, driver: Player): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle  = obj
        private val localDriver   = driver
        private val localAnnounce = self

        override def Description: String = s"register a ${localVehicle.Definition.Name} driven by ${localDriver.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (localVehicle.HasGUID && localDriver.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          localDriver.VehicleSeated = localVehicle.GUID
          Vehicles.Own(localVehicle, localDriver)
          localAnnounce ! NewPlayerLoaded(localDriver) //alerts WorldSessionActor
          resolver ! Success(this)
        }

        override def onFailure(ex: Throwable): Unit = {
          localAnnounce ! PlayerFailedToLoad(localDriver) //alerts SessionActor
        }
      },
      List(RegisterNewAvatar(driver), GUIDTask.RegisterVehicle(obj)(continent.GUID))
    )
  }

  /**
    * Construct tasking that adds a completed but unregistered projectile into the scene.
    * After the projectile is registered to the curent zone's global unique identifier system,
    * all connected clients save for the one that registered it will be informed about the projectile's "creation."
    * @param obj the projectile to be registered
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterProjectile(obj: Projectile): TaskResolver.GiveTask = {
    val definition = obj.Definition
    TaskResolver.GiveTask(
      new Task() {
        private val globalProjectile = obj
        private val localAnnounce    = self

        override def Description: String = s"register a ${globalProjectile.profile.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (globalProjectile.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          localAnnounce ! LoadedRemoteProjectile(globalProjectile.GUID, Some(globalProjectile))
          resolver ! Success(this)
        }
      },
      List(GUIDTask.RegisterObjectTask(obj)(continent.GUID))
    )
  }

  def UnregisterDrivenVehicle(obj: Vehicle, driver: Player): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localDriver  = driver

        override def Description: String = s"unregister a ${localVehicle.Definition.Name} driven by ${localDriver.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (!localVehicle.HasGUID && !localDriver.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          resolver ! Success(this)
        }
      },
      List(GUIDTask.UnregisterAvatar(driver)(continent.GUID), GUIDTask.UnregisterVehicle(obj)(continent.GUID))
    )
  }

  /**
    * Construct tasking that removes a formerly complete and currently registered projectile from the scene.
    * After the projectile is unregistered from the curent zone's global unique identifier system,
    * all connected clients save for the one that registered it will be informed about the projectile's "destruction."
    * @param obj the projectile to be unregistered
    * @return a `TaskResolver.GiveTask` message
    */
  def UnregisterProjectile(obj: Projectile): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val globalProjectile = obj
        private val localAnnounce    = continent.AvatarEvents
        private val localMsg         = AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(player.GUID, obj.GUID, 2))

        override def Description: String = s"unregister a ${globalProjectile.profile.Name}"

        override def isComplete: Task.Resolution.Value = {
          if (!globalProjectile.HasGUID) {
            Task.Resolution.Success
          } else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver: ActorRef): Unit = {
          localAnnounce ! localMsg
          resolver ! Success(this)
        }
      },
      List(GUIDTask.UnregisterObjectTask(obj)(continent.GUID))
    )
  }

  /**
    * If the projectile object is unregistered, register it.
    * If the projectile object is already registered, unregister it and then register it again.
    * @see `RegisterProjectile(Projectile)`
    * @see `UnregisterProjectile(Projectile)`
    * @param obj the projectile to be registered (a second time?)
    * @return a `TaskResolver.GiveTask` message
    */
  def ReregisterProjectile(obj: Projectile): TaskResolver.GiveTask = {
    val reg = RegisterProjectile(obj)
    if (obj.HasGUID) {
      TaskResolver.GiveTask(
        reg.task,
        List(
          TaskResolver.GiveTask(
            reg.subs(0).task,
            List(UnregisterProjectile(obj))
          )
        )
      )
    } else {
      reg
    }
  }

  def CallBackForTask(task: TaskResolver.GiveTask, sendTo: ActorRef, pass: Any): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localDesc   = task.task.Description
        private val destination = sendTo
        private val passMsg     = pass

        override def Description: String = s"callback for tasking $localDesc"

        def Execute(resolver: ActorRef): Unit = {
          destination ! passMsg
          resolver ! Success(this)
        }
      },
      List(task)
    )
  }

  def AccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        AccessVehicleContents(v)
      case o: LockerContainer =>
        AccessGenericContainer(o)
      case p: PlanetSideServerObject with Container =>
        accessedContainer = Some(p)
      case _ => ;
    }
  }

  def AccessGenericContainer(container: PlanetSideServerObject with Container): Unit = {
    accessedContainer = Some(container)
    DisplayContainerContents(container.GUID, container)
  }

  /**
    * Common preparation for interfacing with a vehicle.
    * Join a vehicle-specific group for shared updates.
    * Construct every object in the vehicle's inventory for shared manipulation updates.
    * @param vehicle the vehicle
    */
  def AccessVehicleContents(vehicle: Vehicle): Unit = {
    accessedContainer = Some(vehicle)
    AccessVehicleContainerChannel(vehicle)
    DisplayContainerContents(vehicle.GUID, vehicle)
  }

  def AccessVehicleContainerChannel(container: PlanetSideServerObject): Unit = {
    continent.VehicleEvents ! Service.Join(s"${container.Actor}")
  }

  def DisplayContainerContents(containerId: PlanetSideGUID, container: Container): Unit = {
    container.Inventory.Items.foreach(entry => {
      val obj    = entry.obj
      val objDef = obj.Definition
      sendResponse(
        ObjectCreateDetailedMessage(
          objDef.ObjectId,
          obj.GUID,
          ObjectCreateMessageParent(containerId, entry.start),
          objDef.Packet.DetailedConstructorData(obj).get
        )
      )
    })
  }

  def UnaccessContainer(): Unit = {
    accessedContainer match {
      case Some(container) => UnaccessContainer(container)
      case _               => ;
    }
  }

  def UnaccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        UnaccessVehicleContainer(v)
      case o: LockerContainer =>
        UnaccessGenericContainer(o)
      case _: PlanetSideServerObject with Container =>
        accessedContainer = None
      case _ => ;
    }
  }

  def UnaccessGenericContainer(container: Container): Unit = {
    accessedContainer = None
    HideContainerContents(container)
  }

  /**
    * Common preparation for disengaging from a vehicle.
    * Leave the vehicle-specific group that was used for shared updates.
    * Deconstruct every object in the vehicle's inventory.
    * @param vehicle the vehicle
    */
  def UnaccessVehicleContainer(vehicle: Vehicle): Unit = {
    accessedContainer = None
    if (vehicle.AccessingTrunk.contains(player.GUID)) {
      vehicle.AccessingTrunk = None
    }
    UnaccessVehicleContainerChannel(vehicle)
    HideContainerContents(vehicle)
  }

  def UnaccessVehicleContainerChannel(container: PlanetSideServerObject): Unit = {
    continent.VehicleEvents ! Service.Leave(Some(s"${container.Actor}"))
  }

  def HideContainerContents(container: Container): Unit = {
    container.Inventory.Items.foreach(entry => {
      sendResponse(ObjectDeleteMessage(entry.obj.GUID, 0))
    })
  }

  /**
    * Check two locations for a controlled piece of equipment that is associated with the `player`.<br>
    * <br>
    * The first location is dependent on whether the avatar is in a vehicle.
    * Some vehicle seats may have a "controlled weapon" which counts as the first location to be checked.
    * The second location is dependent on whether the avatar has a raised hand.
    * That is only possible if the player has something in their hand at the moment, hence the second location.
    * Players do have a concept called a "last drawn slot" (hand) but that former location is not eligible.<br>
    * <br>
    * Along with any discovered item, a containing object such that the statement:<br>
    *   `container.Find(object) = Some(slot)`<br>
    * ... will return a proper result.
    * For a seat controlled weapon, the vehicle is returned.
    * For the player's hand, the player is returned.
    * @return a `Tuple` of the returned values;
    *         the first value is a `Container` object;
    *         the second value is an `Equipment` object in the former
    */
  def FindContainedEquipment: (Option[PlanetSideGameObject with Container], Option[Equipment]) = {
    player.VehicleSeated match {
      case Some(vehicle_guid) => //weapon is vehicle turret?
        continent.GUID(vehicle_guid) match {
          case Some(vehicle: Mountable with MountedWeapons with Container) =>
            vehicle.PassengerInSeat(player) match {
              case Some(seat_num) =>
                (Some(vehicle), vehicle.WeaponControlledFromSeat(seat_num))
              case None => ;
                (None, None)
            }
          case _ => ;
            (None, None)
        }
      case None => //not in vehicle; weapon in hand?
        (Some(player), player.Slot(player.DrawnSlot).Equipment)
    }
  }

  /**
    * Runs `FindContainedEquipment` but ignores the `Container` object output.
    * @return an `Equipment` object
    */
  def FindEquipment: Option[Equipment] = FindContainedEquipment._2

  /**
    * Check two locations for a controlled piece of equipment that is associated with the `player`.
    * Filter for discovered `Tool`-type `Equipment`.
    * @return a `Tuple` of the returned values;
    *         the first value is a `Container` object;
    *         the second value is an `Tool` object in the former
    */
  def FindContainedWeapon: (Option[PlanetSideGameObject with Container], Option[Tool]) = {
    FindContainedEquipment match {
      case (container, Some(tool: Tool)) =>
        (container, Some(tool))
      case _ =>
        (None, None)
    }
  }

  /**
    * Runs `FindContainedWeapon` but ignores the `Container` object output.
    * @return a `Tool` object
    */
  def FindWeapon: Option[Tool] = FindContainedWeapon._2

  /**
    * Get the current `Vehicle` object that the player is riding/driving.
    * The vehicle must be found solely through use of `player.VehicleSeated`.
    * @return the vehicle
    */
  def FindLocalVehicle: Option[Vehicle] = {
    player.VehicleSeated match {
      case Some(vehicle_guid) =>
        continent.GUID(vehicle_guid) match {
          case Some(obj: Vehicle) =>
            Some(obj)
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  /**
    * Given an object that contains a box of amunition in its `Inventory` at a certain location,
    * change the amount of ammunition within that box.
    * @param obj the `Container`
    * @param box an `AmmoBox` to modify
    * @param reloadValue the value to modify the `AmmoBox`;
    *                    subtracted from the current `Capacity` of `Box`
    */
  private def ModifyAmmunition(obj: PlanetSideGameObject with Container)(box: AmmoBox, reloadValue: Int): Unit = {
    val capacity = box.Capacity - reloadValue
    box.Capacity = capacity
    sendResponse(InventoryStateMessage(box.GUID, obj.GUID, capacity))
  }

  /**
    * Given a vehicle that contains a box of amunition in its `Trunk` at a certain location,
    * change the amount of ammunition within that box.
    * @param obj the `Container`
    * @param box an `AmmoBox` to modify
    * @param reloadValue the value to modify the `AmmoBox`;
    *                    subtracted from the current `Capacity` of `Box`
    */
  private def ModifyAmmunitionInVehicle(obj: Vehicle)(box: AmmoBox, reloadValue: Int): Unit = {
    val capacity = ModifyAmmunition(obj)(box, reloadValue)
    obj.Find(box) match {
      case Some(index) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          s"${obj.Actor}",
          VehicleAction.InventoryState(
            player.GUID,
            box,
            obj.GUID,
            index,
            box.Definition.Packet.DetailedConstructorData(box).get
          )
        )
      case None => ;
    }
  }

  /**
    * na
    * @param tool na
    * @param obj na
    */
  def PerformToolAmmoChange(tool: Tool, obj: PlanetSideGameObject with Container): Unit = {
    val originalAmmoType = tool.AmmoType
    do {
      val requestedAmmoType = tool.NextAmmoType
      val fullMagazine      = tool.MaxMagazine
      if (requestedAmmoType != tool.AmmoSlot.Box.AmmoType) {
        FindEquipmentStock(obj, FindAmmoBoxThatUses(requestedAmmoType), fullMagazine, CountAmmunition).reverse match {
          case Nil => ;
          case x :: xs =>
            val (deleteFunc, modifyFunc): (Equipment => Future[Any], (AmmoBox, Int) => Unit) = obj match {
              case (veh: Vehicle) =>
                (RemoveOldEquipmentFromInventory(veh), ModifyAmmunitionInVehicle(veh))
              case o: PlanetSideServerObject with Container =>
                (RemoveOldEquipmentFromInventory(o), ModifyAmmunition(o))
              case _ =>
                throw new Exception(
                  "PerformToolAmmoChange: (remove/modify) should be a server object, not a regular game object"
                )
            }
            val (stowNewFunc, stowFunc): (Equipment => TaskResolver.GiveTask, Equipment => Future[Any]) = obj match {
              case o: PlanetSideServerObject with Container =>
                (PutNewEquipmentInInventoryOrDrop(o), PutEquipmentInInventoryOrDrop(o))
              case _ =>
                throw new Exception(
                  "PerformToolAmmoChange: (new/put) should be a server object, not a regular game object"
                )
            }
            xs.foreach(item => {
              obj.Inventory -= x.start
              deleteFunc(item.obj)
            })

            //box will be the replacement ammo; give it the discovered magazine and load it into the weapon @ 0
            val box                 = x.obj.asInstanceOf[AmmoBox]
            val originalBoxCapacity = box.Capacity
            val tailReloadValue: Int = if (xs.isEmpty) { 0 }
            else { xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).reduceLeft(_ + _) }
            val sumReloadValue: Int = originalBoxCapacity + tailReloadValue
            val previousBox         = tool.AmmoSlot.Box //current magazine in tool
            sendResponse(ObjectDetachMessage(tool.GUID, previousBox.GUID, Vector3.Zero, 0f))
            sendResponse(ObjectDetachMessage(player.GUID, box.GUID, Vector3.Zero, 0f))
            obj.Inventory -= x.start //remove replacement ammo from inventory
            val ammoSlotIndex = tool.FireMode.AmmoSlotIndex
            tool.AmmoSlots(ammoSlotIndex).Box = box //put replacement ammo in tool
            sendResponse(ObjectAttachMessage(tool.GUID, box.GUID, ammoSlotIndex))

            //announce swapped ammunition box in weapon
            val previous_box_guid = previousBox.GUID
            val boxDef            = box.Definition
            val box_guid          = box.GUID
            val tool_guid         = tool.GUID
            sendResponse(ChangeAmmoMessage(tool_guid, box.Capacity))
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ChangeAmmo(
                player.GUID,
                tool_guid,
                ammoSlotIndex,
                previous_box_guid,
                boxDef.ObjectId,
                box.GUID,
                boxDef.Packet.ConstructorData(box).get
              )
            )

            //handle inventory contents
            box.Capacity = (if (sumReloadValue <= fullMagazine) {
                              sumReloadValue
                            } else {
                              val splitReloadAmmo: Int = sumReloadValue - fullMagazine
                              log.info(
                                s"ChangeAmmo: taking ${originalBoxCapacity - splitReloadAmmo} from a box of ${originalBoxCapacity} $requestedAmmoType"
                              )
                              val boxForInventory = AmmoBox(box.Definition, splitReloadAmmo)
                              continent.tasks ! stowNewFunc(boxForInventory)
                              fullMagazine
                            })
            sendResponse(
              InventoryStateMessage(box.GUID, tool.GUID, box.Capacity)
            ) //should work for both players and vehicles
            log.info(s"ChangeAmmo: loading ${box.Capacity} $requestedAmmoType into ${tool.GUID} @ $ammoSlotIndex")
            if (previousBox.Capacity > 0) {
              //divide capacity across other existing and not full boxes of that ammo type
              var capacity = previousBox.Capacity
              val iter = obj.Inventory.Items
                .filter(entry => {
                  entry.obj match {
                    case (item: AmmoBox) =>
                      item.AmmoType == originalAmmoType && item.FullCapacity != item.Capacity
                    case _ =>
                      false
                  }
                })
                .toList
                .sortBy(_.start)
                .iterator
              while (capacity > 0 && iter.hasNext) {
                val entry         = iter.next()
                val item: AmmoBox = entry.obj.asInstanceOf[AmmoBox]
                val ammoAllocated = math.min(item.FullCapacity - item.Capacity, capacity)
                log.info(s"ChangeAmmo: putting $ammoAllocated back into a box of ${item.Capacity} $originalAmmoType")
                capacity -= ammoAllocated
                modifyFunc(item, -ammoAllocated)
              }
              previousBox.Capacity = capacity
            }

            if (previousBox.Capacity > 0) {
              //split previousBox into AmmoBox objects of appropriate max capacity, e.g., 100 9mm -> 2 x 50 9mm
              obj.Inventory.Fit(previousBox) match {
                case Some(_) =>
                  stowFunc(previousBox)
                case None =>
                  NormalItemDrop(player, continent)(previousBox)
              }
              AmmoBox.Split(previousBox) match {
                case Nil | List(_) => ; //done (the former case is technically not possible)
                case _ :: xs =>
                  modifyFunc(previousBox, 0) //update to changed capacity value
                  xs.foreach(box => { continent.tasks ! stowNewFunc(box) })
              }
            } else {
              continent.tasks ! GUIDTask.UnregisterObjectTask(previousBox)(continent.GUID)
            }
        }
      }
    } while (tool.AmmoType != originalAmmoType && tool.AmmoType != tool.AmmoSlot.Box.AmmoType)
  }

  /**
    * Drop an `Equipment` item onto the ground.
    * Specifically, instruct the item where it will appear,
    * add it to the list of items that are visible to multiple users,
    * and then inform others that the item has been dropped.
    * @param obj a `Container` object that represents where the item will be dropped;
    *            curried for callback
    * @param zone the continent in which the item is being dropped;
    *             curried for callback
    * @param item the item
    */
  def NormalItemDrop(obj: PlanetSideServerObject with Container, zone: Zone)(item: Equipment): Unit = {
    zone.Ground.tell(Zone.Ground.DropItem(item, obj.Position, Vector3.z(obj.Orientation.z)), obj.Actor)
  }

  /**
    * Register an `Equipment` item and then drop it on the ground.
    * @see `NormalItemDrop`
    * @param obj a `Container` object that represents where the item will be dropped;
    *            curried for callback
    * @param zone the continent in which the item is being dropped;
    *             curried for callback
    * @param item the item
    */
  def NewItemDrop(obj: PlanetSideServerObject with Container, zone: Zone)(item: Equipment): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localItem                      = item
        private val localFunc: (Equipment) => Unit = NormalItemDrop(obj, zone)

        override def Description: String = s"dropping a new ${localItem.Definition.Name} on the ground"

        def Execute(resolver: ActorRef): Unit = {
          localFunc(localItem)
          resolver ! Success(this)
        }
      },
      List(GUIDTask.RegisterEquipment(item)(zone.GUID))
    )
  }

  /**
    * After a weapon has finished shooting, determine if it needs to be sorted in a special way.
    * @param tool a weapon
    */
  def FireCycleCleanup(tool: Tool): Unit = {
    //TODO replaced by more appropriate functionality in the future
    val tdef = tool.Definition
    if (GlobalDefinitions.isGrenade(tdef)) {
      val ammoType = tool.AmmoType
      FindEquipmentStock(player, FindToolThatUses(ammoType), 3, CountGrenades).reverse match { //do not search sidearm holsters
        case Nil =>
          log.info(s"no more $ammoType grenades")
          RemoveOldEquipmentFromInventory(player)(tool)

        case x :: xs => //this is similar to ReloadMessage
          val box = x.obj.asInstanceOf[Tool]
          val tailReloadValue: Int = if (xs.isEmpty) { 0 }
          else { xs.map(_.obj.asInstanceOf[Tool].Magazine).reduce(_ + _) }
          val sumReloadValue: Int = box.Magazine + tailReloadValue
          val actualReloadValue = if (sumReloadValue <= 3) {
                                     RemoveOldEquipmentFromInventory(player)(x.obj)
                                     sumReloadValue
                                   } else {
                                     ModifyAmmunition(player)(box.AmmoSlot.Box, 3 - tailReloadValue)
                                     3
                                   }
          log.info(s"found $actualReloadValue more $ammoType grenades to throw")
          ModifyAmmunition(player)(
            tool.AmmoSlot.Box,
            -actualReloadValue
          ) //grenade item already in holster (negative because empty)
          xs.foreach(item => { RemoveOldEquipmentFromInventory(player)(item.obj) })
      }
    } else if (tdef == GlobalDefinitions.phoenix) {
      RemoveOldEquipmentFromInventory(player)(tool)
    }
  }

  /**
    * Given an object globally unique identifier, search in a given location for it.
    * @param object_guid the object
    * @param parent a `Container` object wherein to search
    * @return an optional tuple that contains two values;
    *         the first value is the container that matched correctly with the object's GUID;
    *         the second value is the slot position of the object
    */
  def FindInLocalContainer(
      object_guid: PlanetSideGUID
  )(parent: PlanetSideServerObject with Container): Option[(PlanetSideServerObject with Container, Option[Int])] = {
    val slot: Option[Int] = parent.Find(object_guid)
    slot match {
      case place @ Some(_) =>
        Some(parent, slot)
      case None =>
        None
    }
  }

  /**
    * Common reporting behavior when a `Deployment` object fails to properly transition between states.
    * @param obj the game object that could not
    * @param state the `DriveState` that could not be promoted
    * @param reason a string explaining why the state can not or will not change
    */
  def CanNotChangeDeployment(
      obj: PlanetSideServerObject with Deployment,
      state: DriveState.Value,
      reason: String
  ): Unit = {
    val mobileShift: String = if (obj.DeploymentState != DriveState.Mobile) {
      obj.DeploymentState = DriveState.Mobile
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Mobile, 0, false, Vector3.Zero))
      continent.VehicleEvents ! VehicleServiceMessage(
        continent.id,
        VehicleAction.DeployRequest(player.GUID, obj.GUID, DriveState.Mobile, 0, false, Vector3.Zero)
      )
      "; enforcing Mobile deployment state"
    } else {
      ""
    }
    log.error(s"DeployRequest: $obj can not transition to $state - $reason$mobileShift")
  }

  /**
    * For a given continental structure, determine the method of generating server-join client configuration packets.
    * @param continentNumber the zone id
    * @param buildingNumber the building id
    * @param building the building object
    */
  def initBuilding(continentNumber: Int, buildingNumber: Int, building: Building): Unit = {
    building.BuildingType match {
      case StructureType.WarpGate =>
        initGate(continentNumber, buildingNumber, building)
      case _ =>
        initFacility(continentNumber, buildingNumber, building)
    }
  }

  /**
    * For a given facility structure, configure a client by dispatching the appropriate packets.
    * Pay special attention to the details of `BuildingInfoUpdateMessage` when preparing this packet.<br>
    * <br>
    * 24 Janurtay 2019:<br>
    * Manual `BIUM` construction to alleviate player login.
    * @see `BuildingInfoUpdateMessage`
    * @see `DensityLevelUpdateMessage`
    * @param continentNumber the zone id
    * @param buildingNumber the building id
    * @param building the building object
    */
  def initFacility(continentNumber: Int, buildingNumber: Int, building: Building): Unit = {
    sendResponse(building.infoUpdateMessage())
    sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0, 0, 0, 0, 0, 0, 0, 0)))
  }

  /**
    * For a given lattice warp gate structure, configure a client by dispatching the appropriate packets.
    * Unlike other facilities, gates do not have complicated `BuildingInfoUpdateMessage` packets.
    * Also unlike facilities, gates have an additional packet.
    * @see `BuildingInfoUpdateMessage`
    * @see `DensityLevelUpdateMessage`
    * @see `BroadcastWarpgateUpdateMessage`
    * @param continentNumber the zone id
    * @param buildingNumber the building id
    * @param building the building object
    */
  def initGate(continentNumber: Int, buildingNumber: Int, building: Building): Unit = {
    building match {
      case wg: WarpGate =>
        sendResponse(
          BuildingInfoUpdateMessage(
            building.Zone.Number,
            building.MapId,
            ntu_level = 0,
            is_hacked = false,
            empire_hack = PlanetSideEmpire.NEUTRAL,
            hack_time_remaining = 0,
            building.Faction,
            unk1 = 0,
            unk1x = None,
            PlanetSideGeneratorState.Normal,
            spawn_tubes_normal = true,
            force_dome_active = false,
            lattice_benefit = 0,
            cavern_benefit = 0,
            unk4 = Nil,
            unk5 = 0,
            unk6 = false,
            unk7 = 8,
            unk7x = None,
            boost_spawn_pain = false,
            boost_generator_pain = false
          )
        )
        sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0, 0, 0, 0, 0, 0, 0, 0)))
        //TODO one faction knows which gates are broadcast for another faction?
        sendResponse(
          BroadcastWarpgateUpdateMessage(
            continentNumber,
            buildingNumber,
            wg.Broadcast(PlanetSideEmpire.TR),
            wg.Broadcast(PlanetSideEmpire.NC),
            wg.Broadcast(PlanetSideEmpire.VS)
          )
        )
      case _ => ;
    }
  }

  /**
    * Configure the buildings and each specific amenity for that building in a given zone by sending the client packets.
    * These actions are performed during the loading of a zone.
    * @see `SetEmpireMessage`<br>
    *     `PlanetsideAttributeMessage`<br>
    *     `HackMessage`
    * @param zone the zone being loaded
    */
  def configZone(zone: Zone): Unit = {
    zone.Buildings.values.foreach(building => {
      val guid = building.GUID
      sendResponse(SetEmpireMessage(guid, building.Faction))
      // power
      building.Generator match {
        case Some(obj) if obj.Condition == PlanetSideGeneratorState.Destroyed || building.NtuLevel == 0 =>
          sendResponse(PlanetsideAttributeMessage(guid, 48, 1)) //amenities disabled; red warning lights
          sendResponse(PlanetsideAttributeMessage(guid, 38, 0)) //disable spawn target on deployment map
        case _ => ;
      }
      // capitol force dome state
      if (building.IsCapitol && building.ForceDomeActive) {
        sendResponse(GenericObjectActionMessage(guid, 13))
      }
      // amenities
      building.Amenities.collect {
        case obj if obj.Destroyed => configAmenityAsDestroyed(obj)
        case obj                  => configAmenityAsWorking(obj)
      }
    })
  }

  /**
    * Configure the specific working amenity by sending the client packets.
    * Amenities that are not `Damageable` are also included.
    * These actions are performed during the loading of a zone.
    * @see `Door`
    * @see `GenericObjectStateMsg`
    * @see `Hackable`
    * @see `HackCaptureTerminal`
    * @see `HackObject`
    * @see `PlanetsideAttributeMessage`
    * @see `ResourceSilo`
    * @see `SetEmpireMessage`
    * @see `VitalityDefinition.Damageable`
    * @param amenity the facility object
    */
  def configAmenityAsWorking(amenity: Amenity): Unit = {
    val amenityId = amenity.GUID
    //sync model access state
    sendResponse(PlanetsideAttributeMessage(amenityId, 50, 0))
    sendResponse(PlanetsideAttributeMessage(amenityId, 51, 0))
    //sync damageable, if
    val health = amenity.Health
    if (amenity.Definition.Damageable && health < amenity.MaxHealth) {
      sendResponse(PlanetsideAttributeMessage(amenityId, 0, health))
    }
    //sync special object type cases
    amenity match {
      case silo: ResourceSilo =>
        //silo capacity
        sendResponse(PlanetsideAttributeMessage(amenityId, 45, silo.CapacitorDisplay))
        //warning lights
        sendResponse(PlanetsideAttributeMessage(silo.Owner.GUID, 47, if (silo.LowNtuWarningOn) 1 else 0))
        if (silo.NtuCapacitor == 0) {
          sendResponse(PlanetsideAttributeMessage(silo.Owner.GUID, 48, 1))
        }
      case door: Door if door.isOpen =>
        sendResponse(GenericObjectStateMsg(amenityId, 16))

      case _ => ;
    }
    //sync hack state
    amenity match {
      case obj: Hackable if obj.HackedBy.nonEmpty =>
        amenity.Definition match {
          case GlobalDefinitions.capture_terminal =>
            HackCaptureTerminal(amenity.GUID, 0L, 0L, false)
          case _ =>
            HackObject(amenity.GUID, 1114636288L, 8L) //generic hackable object
        }
      case _ => ;
    }
  }

  /**
    * Configure the specific destroyed amenity by sending the client packets.
    * These actions are performed during the loading of a zone.
    * @see `Generator`
    * @see `ImplantTerminalMech`
    * @see `PlanetsideAttributeMessage`
    * @see `PlanetSideGameObject.Destroyed`
    * @param amenity the facility object
    */
  def configAmenityAsDestroyed(amenity: Amenity): Unit = {
    val amenityId = amenity.GUID
    val configValue = amenity match {
      case _: ImplantTerminalMech => 0
      case _: Generator           => 0
      case _                      => 1
    }
    //sync model access state
    sendResponse(PlanetsideAttributeMessage(amenityId, 50, configValue))
    sendResponse(PlanetsideAttributeMessage(amenityId, 51, configValue))
    //sync damageable, if
    if (amenity.Definition.Damageable) {
      sendResponse(PlanetsideAttributeMessage(amenityId, 0, 0))
    }
  }

  /**
    * na
    * @param target_guid na
    * @param unk1 na
    * @param unk2 na
    */
  def HackObject(target_guid: PlanetSideGUID, unk1: Long, unk2: Long): Unit = {
    sendResponse(HackMessage(0, target_guid, PlanetSideGUID(0), 100, unk1, HackState.Hacked, unk2))
  }

  /**
    * na
    * @param target_guid na
    * @param unk1 na
    * @param unk2 na
    * @param isResecured na
    */
  def HackCaptureTerminal(target_guid: PlanetSideGUID, unk1: Long, unk2: Long, isResecured: Boolean): Unit = {
    var value = 0L
    if (isResecured) {
      value = 17039360L
      sendResponse(PlanetsideAttributeMessage(target_guid, 20, value))
    } else {
      continent.GUID(target_guid) match {
        case Some(capture_terminal: Amenity with Hackable) =>
          capture_terminal.HackedBy match {
            case Some(Hackable.HackInfo(_, _, hfaction, _, start, length)) =>
              val hack_time_remaining_ms =
                TimeUnit.MILLISECONDS.convert(math.max(0, start + length - System.nanoTime), TimeUnit.NANOSECONDS)
              val deciseconds_remaining = (hack_time_remaining_ms / 100)
              //See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
              val start_num = hfaction match {
                case PlanetSideEmpire.TR => 65536L
                case PlanetSideEmpire.NC => 131072L
                case PlanetSideEmpire.VS => 196608L
              }
              value = start_num + deciseconds_remaining
              sendResponse(PlanetsideAttributeMessage(target_guid, 20, value))
              GetMountableAndSeat(None, player, continent) match {
                case (Some(mountable: Amenity), Some(seat)) if mountable.Owner.GUID == capture_terminal.Owner.GUID =>
                  mountable.Seats(seat).Occupant = None
                  player.VehicleSeated = None
                  continent.VehicleEvents ! VehicleServiceMessage(
                    continent.id,
                    VehicleAction.KickPassenger(player.GUID, seat, true, mountable.GUID)
                  )
                case _ => ;
              }
            case _ => log.warn("HackCaptureTerminal: hack state monitor not defined")
          }
        case _ =>
          log.warn(
            s"HackCaptureTerminal: couldn't find capture terminal with GUID ${target_guid} in zone ${continent.id}"
          )
      }
    }
  }

  /**
    * The player has lost the will to live and must be killed.
    * @see `Vitality`<br>
    *       `PlayerSuicide`
    * @param tplayer the player to be killed
    */
  def suicide(tplayer: Player): Unit = {
    tplayer.History(PlayerSuicide(PlayerSource(tplayer)))
    tplayer.Actor ! Player.Die()
  }

  /**
    * An event has occurred that would cause the player character to stop certain stateful activities.
    * These activities include shooting, the weapon being drawn, hacking, accessing (a container), flying, and running.
    * Other players in the same zone must be made aware that the player has stopped as well.<br>
    * <br>
    * Things whose configuration should not be changed:<br>
    * - if the player is seated<br>
    * - if the player is anchored<br>
    * This is not a complete list but, for the purpose of enforcement, some pointers will be documented here.
    */
  def PlayerActionsToCancel(): Unit = {
    progressBarUpdate.cancel()
    progressBarValue = None
    lastTerminalOrderFulfillment = true
    accessedContainer match {
      case Some(v: Vehicle) =>
        val vguid = v.GUID
        if (v.AccessingTrunk.contains(player.GUID)) {
          if (player.VehicleSeated.contains(vguid)) {
            v.AccessingTrunk = None //player is seated; just stop accessing trunk
            if (player.isAlive) {
              sendResponse(UnuseItemMessage(player.GUID, vguid))
            }
          } else {
            UnaccessContainer(v)
          }
        }

      case Some(o) =>
        UnaccessContainer(o)
        if (player.isAlive) {
          sendResponse(UnuseItemMessage(player.GUID, o.GUID))
        }

      case None => ;
    }
    prefire.orElse(shooting) match {
      case Some(guid) =>
        sendResponse(ChangeFireStateMessage_Stop(guid))
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.ChangeFireState_Stop(player.GUID, guid)
        )
        prefire = None
        shooting = None
        shootingStop = System.currentTimeMillis()
      case None => ;
    }
    if (session.flying) {
      chatActor ! ChatActor.Message(ChatMsg(ChatMessageType.CMT_FLY, false, "", "off", None))
    }
    if (session.speed > 1) {
      chatActor ! ChatActor.Message(ChatMsg(ChatMessageType.CMT_SPEED, false, "", "1.000", None))
    }
  }

  /**
    * A part of the process of spawning the player into the game world.
    * The function should work regardless of whether the player is alive or dead - it will make them alive.
    * It adds the `WorldSessionActor`-current `Player` to the current zone and sends out the expected packets.<br>
    * <br>
    * If that player is in a vehicle, it will construct that vehicle.
    * If the player is the driver of the vehicle,
    * they must temporarily be removed from the driver seat in order for the vehicle to be constructed properly.
    * These two previous statements operate through similar though distinct mechanisms and imply different conditions.
    * In reality, they produce the same output but enforce different relationships between the components.
    * The vehicle without a rendered player will always be created if that vehicle exists.
    * The vehicle should only be constructed once.
    */
  def AvatarCreate(): Unit = {
    val health = player.Health
    val armor  = player.Armor
    avatarActor ! AvatarActor.ResetImplants()
    player.Spawn()
    if (health != 0) {
      player.Health = health
      player.Armor = armor
    }
    GetKnownVehicleAndSeat() match {
      case (Some(vehicle: Vehicle), Some(seat: Int)) =>
        //if the vehicle is the cargo of another vehicle in this zone
        val carrierInfo = continent.GUID(vehicle.MountedIn) match {
          case Some(carrier: Vehicle) =>
            (Some(carrier), carrier.CargoHolds.find({ case (index, hold) => hold.Occupant.contains(vehicle) }))
          case _ =>
            (None, None)
        }
        //vehicle and driver/passenger
        interstellarFerry = None
        val vdef  = vehicle.Definition
        val vguid = vehicle.GUID
        vehicle.Position = shiftPosition.getOrElse(vehicle.Position)
        vehicle.Orientation = shiftOrientation.getOrElse(vehicle.Orientation)
        val vdata = if (seat == 0) {
          //driver
          continent.Transport ! Zone.Vehicle.Spawn(vehicle)
          //as the driver, we must temporarily exclude ourselves from being in the vehicle during its creation
          val seat = vehicle.Seats(0)
          seat.Occupant = None
          val data = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, data))
          seat.Occupant = player
          Vehicles.Own(vehicle, player)
          vehicle.CargoHolds.values
            .collect { case hold if hold.isOccupied => hold.Occupant.get }
            .foreach { _.MountedIn = vguid }
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.LoadVehicle(player.GUID, vehicle, vdef.ObjectId, vguid, data)
          )
          carrierInfo match {
            case (Some(carrier), Some((index, _))) =>
              CargoBehavior.CargoMountBehaviorForOthers(carrier, vehicle, index, player.GUID)
            case _ =>
              vehicle.MountedIn = None
          }
          data
        } else {
          //passenger
          //non-drivers are not rendered in the vehicle at this time
          val data = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, data))
          carrierInfo match {
            case (Some(carrier), Some((index, _))) =>
              CargoMountBehaviorForUs(carrier, vehicle, index)
            case _ => ;
          }
          data
        }
        val originalSeated = player.VehicleSeated
        player.VehicleSeated = vguid
        if (Vehicles.AllGatedOccupantsInSameZone(vehicle)) {
          //do not dispatch delete action if any hierarchical occupant has not gotten this far through the summoning process
          val vehicleToDelete = interstellarFerryTopLevelGUID.orElse(originalSeated).getOrElse(PlanetSideGUID(0))
          val zone            = vehicle.PreviousGatingManifest().get.origin
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.UnloadVehicle(player.GUID, vehicle, vehicleToDelete)
          )
          log.info(
            s"AvatarCreate: cleaning up ghost of transitioning vehicle ${vehicle.Definition.Name}@${vehicleToDelete.guid} in zone ${zone.id}"
          )
        }
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        //log.info(s"AvatarCreate (vehicle): $guid -> $data")
        AvatarCreateInVehicle(player, vehicle, seat)

      case _ =>
        player.VehicleSeated = None
        val packet = player.avatar.definition.Packet
        val data   = packet.DetailedConstructorData(player).get
        val guid   = player.GUID
        sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, guid, data))
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.LoadPlayer(guid, ObjectClass.avatar, guid, packet.ConstructorData(player).get, None)
        )
        //log.info(s"AvatarCreate: $guid -> $data")
        log.trace(s"AvatarCreate: ${player.Name}")
    }
    continent.Population ! Zone.Population.Spawn(avatar, player, avatarActor)
    //cautious redundancy
    deadState = DeadState.Alive
    avatarActor ! AvatarActor.RefreshPurchaseTimes()
    //begin looking for conditions to set the avatar
    context.system.scheduler.scheduleOnce(delay = 250 millisecond, self, SetCurrentAvatar(player, 200))
  }

  /**
    * If the player is mounted in some entity, find that entity and get the seat index number at which the player is sat.
    * The priority of object confirmation is `direct` then `occupant.VehicleSeated`.
    * Once an object is found, the remainder are ignored.
    * @param direct a game object in which the player may be sat
    * @param occupant the player who is sat and may have specified the game object in which mounted
    * @return a tuple consisting of a vehicle reference and a seat index
    *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
    *         `(None, None)`, otherwise (even if the vehicle can be determined)
    */
  def GetMountableAndSeat(
      direct: Option[PlanetSideGameObject with Mountable],
      occupant: Player,
      zone: Zone
  ): (Option[PlanetSideGameObject with Mountable], Option[Int]) =
    direct.orElse(zone.GUID(occupant.VehicleSeated)) match {
      case Some(obj: PlanetSideGameObject with Mountable) =>
        obj.PassengerInSeat(occupant) match {
          case index @ Some(_) =>
            (Some(obj), index)
          case None =>
            (None, None)
        }
      case _ =>
        (None, None)
    }

  /**
    * If the player is seated in a vehicle, find that vehicle and get the seat index number at which the player is sat.<br>
    * <br>
    * For special purposes involved in zone transfers,
    * where the vehicle may or may not exist in either of the zones (yet),
    * the value of `interstellarFerry` is also polled.
    * Making certain this field is blanked after the transfer is completed is important
    * to avoid inspecting the wrong vehicle and failing simple vehicle checks where this function may be employed.
    * @see `GetMountableAndSeat`
    * @see `interstellarFerry`
    * @return a tuple consisting of a vehicle reference and a seat index
    *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
    *         `(None, None)`, otherwise (even if the vehicle can be determined)
    */
  def GetKnownVehicleAndSeat(): (Option[Vehicle], Option[Int]) =
    GetMountableAndSeat(interstellarFerry, player, continent) match {
      case (Some(v: Vehicle), Some(seat)) => (Some(v), Some(seat))
      case _                              => (None, None)
    }

  /**
    * If the player is seated in a vehicle, find that vehicle and get the seat index number at which the player is sat.
    * @see `GetMountableAndSeat`
    * @return a tuple consisting of a vehicle reference and a seat index
    *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
    *         `(None, None)`, otherwise (even if the vehicle can be determined)
    */
  def GetVehicleAndSeat(): (Option[Vehicle], Option[Int]) =
    GetMountableAndSeat(None, player, continent) match {
      case (Some(v: Vehicle), Some(seat)) => (Some(v), Some(seat))
      case _                              => (None, None)
    }

  /**
    * Create an avatar character so that avatar's player is mounted in a vehicle's seat.
    * A part of the process of spawning the player into the game world.<br>
    * <br>
    * This is a very specific configuration of the player character that is not visited very often.
    * The value of `player.VehicleSeated` should be set to accommodate `Packet.DetailedConstructorData` and,
    * though not explicitly checked,
    * should be the same as the globally unique identifier that is assigned to the `vehicle` parameter for the current zone.
    * The priority of this function is consider "initial" so it introduces the avatar to the game world in this state
    * and is permitted to introduce the avatar to the vehicle's internal settings in a similar way.
    * Neither the player avatar nor the vehicle should be reconstructed before the next zone load operation
    * to avoid damaging the critical setup of this function.
    * @see `AccessContainer`
    * @see `UpdateWeaponAtSeatPosition`
    * @param tplayer the player avatar seated in the vehicle's seat
    * @param vehicle the vehicle the player is riding
    * @param seat the seat index
    */
  def AvatarCreateInVehicle(tplayer: Player, vehicle: Vehicle, seat: Int): Unit = {
    val pdef  = tplayer.avatar.definition
    val pguid = tplayer.GUID
    val vguid = vehicle.GUID
    tplayer.VehicleSeated = None
    val pdata = pdef.Packet.DetailedConstructorData(tplayer).get
    tplayer.VehicleSeated = vguid
    sendResponse(ObjectCreateDetailedMessage(pdef.ObjectId, pguid, pdata))
    if (seat == 0 || vehicle.Seats(seat).ControlledWeapon.nonEmpty) {
      sendResponse(ObjectAttachMessage(vguid, pguid, seat))
      AccessContainer(vehicle)
      UpdateWeaponAtSeatPosition(vehicle, seat)
    } else {
      interimUngunnedVehicle = Some(vguid)
      interimUngunnedVehicleSeat = Some(seat)
    }
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.LoadPlayer(
        pguid,
        pdef.ObjectId,
        pguid,
        pdef.Packet.ConstructorData(tplayer).get,
        Some(ObjectCreateMessageParent(vguid, seat))
      )
    )
    //log.info(s"AvatarCreateInVehicle: $pguid -> pdata")
  }

  /**
    * A part of the process of spawning the player into the game world
    * in the case of a restored game connection (relogging).<br>
    * <br>
    * A login protocol that substitutes the first call to `avatarSetupFunc` (replacing `AvatarCreate`)
    * in consideration of a user re-logging into the game
    * before the period of time where an avatar/player instance would decay and be cleaned-up.
    * Large portions of this function operate as a combination of the mechanics
    * for normal `AvatarCreate` and for `AvatarCreateInVehicle`.
    * Unlike either of the previous, this functionlality is disinterested in updating other clients
    * as the target player and potential vehicle already exist as far as other clients are concerned.<br>
    * <br>
    * If that player is in a vehicle, it will construct that vehicle.
    * If the player is the driver of the vehicle,
    * they must temporarily be removed from the driver seat in order for the vehicle to be constructed properly.
    * These two previous statements operate through similar though distinct mechanisms and imply different conditions.
    * In reality, they produce the same output but enforce different relationships between the components.
    * The vehicle without a rendered player will always be created if that vehicle exists.<br>
    * <br>
    * The value of `player.VehicleSeated` should be set to accommodate `Packet.DetailedConstructorData` and,
    * though not explicitly checked,
    * should be the same as the globally unique identifier that is assigned to the `vehicle` parameter for the current zone.
    * The priority of this function is consider "initial" so it introduces the avatar to the game world in this state
    * and is permitted to introduce the avatar to the vehicle's internal settings in a similar way.
    * Neither the player avatar nor the vehicle should be reconstructed before the next zone load operation
    * to avoid damaging the critical setup of this function.
    */
  def AvatarRejoin(): Unit = {
    GetKnownVehicleAndSeat() match {
      case (Some(vehicle: Vehicle), Some(seat: Int)) =>
        //vehicle and driver/passenger
        val vdef  = vehicle.Definition
        val vguid = vehicle.GUID
        if (seat == 0) {
          val seat = vehicle.Seats(0)
          seat.Occupant = None
          val vdata = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, vdata))
          seat.Occupant = player
        } else {
          val vdata = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, vdata))
        }
        Vehicles.ReloadAccessPermissions(vehicle, continent.id)
        //log.info(s"AvatarCreate (vehicle): $vguid -> $vdata")
        val pdef   = player.avatar.definition
        val pguid  = player.GUID
        val parent = ObjectCreateMessageParent(vguid, seat)
        player.VehicleSeated = None
        val pdata = pdef.Packet.DetailedConstructorData(player).get
        player.VehicleSeated = vguid
        sendResponse(ObjectCreateDetailedMessage(pdef.ObjectId, pguid, pdata))
        //log.info(s"AvatarRejoin: $vguid -> $vdata")
        if (seat == 0 || vehicle.Seats(seat).ControlledWeapon.nonEmpty) {
          sendResponse(ObjectAttachMessage(vguid, pguid, seat))
          AccessContainer(vehicle)
          UpdateWeaponAtSeatPosition(vehicle, seat)
        } else {
          interimUngunnedVehicle = Some(vguid)
          interimUngunnedVehicleSeat = Some(seat)
        }
        log.info(s"AvatarRejoin: ${player.Name} in ${vehicle.Definition.Name}")

      case _ =>
        player.VehicleSeated = None
        val packet = player.avatar.definition.Packet
        val data   = packet.DetailedConstructorData(player).get
        val guid   = player.GUID
        sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, guid, data))
        //log.info(s"AvatarRejoin: $guid -> $data")
        log.trace(s"AvatarRejoin: ${player.Name}")
    }
    //cautious redundancy
    deadState = DeadState.Alive
    avatarActor ! AvatarActor.RefreshPurchaseTimes()
    setupAvatarFunc = AvatarCreate
    //begin looking for conditions to set the avatar
    context.system.scheduler.scheduleOnce(delay = 750 millisecond, self, SetCurrentAvatar(player, 200))
  }

  /**
    * Produce a clone of the player that is equipped with the default infantry loadout.
    * The loadout is hardcoded.
    * The player is expected to be in a Standard Exo-Suit.
    * @param tplayer the original player
    * @return the duplication of the player, in Standard Exo-Suit and with default equipment loadout
    */
  def RespawnClone(tplayer: Player): Player = {
    // workaround to make sure player is spawned with full stamina
    player.avatar = player.avatar.copy(stamina = avatar.maxStamina)
    avatarActor ! AvatarActor.RestoreStamina(avatar.maxStamina)
    avatarActor ! AvatarActor.ResetImplants()
    val obj = Player.Respawn(tplayer)
    DefinitionUtil.applyDefaultLoadout(obj)
    obj.death_by = tplayer.death_by
    obj
  }

  /**
    * Remove items from a deceased player that are not expected to be found on a corpse.
    * Most all players have their melee slot knife (which can not be un-equipped normally) removed.
    * MAX's have their primary weapon in the designated slot removed.
    * @param obj the player to be turned into a corpse
    */
  def FriskDeadBody(obj: Player): Unit = {
    if (!obj.isAlive) {
      obj.Slot(4).Equipment match {
        case None => ;
        case Some(knife) =>
          RemoveOldEquipmentFromInventory(obj)(knife)
      }
      obj.Slot(0).Equipment match {
        case Some(arms: Tool) =>
          if (GlobalDefinitions.isMaxArms(arms.Definition)) {
            RemoveOldEquipmentFromInventory(obj)(arms)
          }
        case _ => ;
      }
      //disown boomers and drop triggers
      val boomers = avatar.deployables.ClearDeployable(DeployedItem.boomer)
      boomers.foreach(boomer => {
        continent.GUID(boomer) match {
          case Some(obj: BoomerDeployable) =>
            obj.OwnerName = None
            continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent))
          case Some(_) | None => ;
        }
      })
      val triggers = RemoveBoomerTriggersFromInventory()
      triggers.foreach(trigger => { NormalItemDrop(obj, continent)(trigger) })
    }
  }

  /**
    * Creates a player that has the characteristics of a corpse
    * so long as the player has items in their knapsack or their holsters.
    * If the player has no items stored, the clean solution is to remove the player from the game.
    * To the game, that is a backpack (or some pastry, festive graphical modification allowing).
    * @see `AvatarAction.ObjectDelete`
    * @see `AvatarAction.Release`
    * @see `AvatarServiceMessage`
    * @see `FriskDeadBody`
    * @see `GUIDTask.UnregisterPlayer`
    * @see `ObjectDeleteMessage`
    * @see `WellLootedDeadBody`
    * @see `Zone.Corpse.Add`
    * @param tplayer the player
    */
  def PrepareToTurnPlayerIntoCorpse(tplayer: Player, zone: Zone): Unit = {
    tplayer.Release
    FriskDeadBody(tplayer)
    if (!WellLootedDeadBody(tplayer)) {
      TurnPlayerIntoCorpse(tplayer, zone)
    } else {
      //no items in inventory; leave no corpse
      val pguid = tplayer.GUID
      zone.Population ! Zone.Population.Release(avatar)
      sendResponse(ObjectDeleteMessage(pguid, 0))
      zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(pguid, pguid, 0))
      zone.tasks ! GUIDTask.UnregisterPlayer(tplayer)(zone.GUID)
    }
  }

  /**
    * Creates a player that has the characteristics of a corpse.
    * To the game, that is a backpack (or some pastry, festive graphical modification allowing).
    * A player who has been kicked may not turn into a corpse.
    * @see `AvatarAction.Release`
    * @see `AvatarServiceMessage`
    * @see `CorpseConverter.converter`
    * @see `DepictPlayerAsCorpse`
    * @see `Player.Release`
    * @see `Zone.AvatarEvents`
    * @see `Zone.Corpse.Add`
    * @see `Zone.Population`
    * @param tplayer the player
    */
  def TurnPlayerIntoCorpse(tplayer: Player, zone: Zone): Unit = {
    tplayer.Release
    DepictPlayerAsCorpse(tplayer)
    zone.Population ! Zone.Corpse.Add(tplayer)
    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.Release(tplayer, zone))
  }

  /**
    * Creates a player that has the characteristics of a corpse.
    * To the game, that is a backpack (or some pastry, festive graphical modification allowing).
    * @see `CorpseConverter.converter`
    * @param tplayer the player
    */
  def DepictPlayerAsCorpse(tplayer: Player): Unit = {
    val guid = tplayer.GUID
    sendResponse(
      ObjectCreateDetailedMessage(
        ObjectClass.avatar,
        guid,
        CorpseConverter.converter.DetailedConstructorData(tplayer).get
      )
    )
  }

  /**
    * If the corpse has been well-looted, it has no items in its primary holsters nor any items in its inventory.
    * @param obj the corpse
    * @return `true`, if the `obj` is actually a corpse and has no objects in its holsters or backpack;
    *        `false`, otherwise
    */
  def WellLootedDeadBody(obj: Player): Boolean = {
    !obj.isAlive && obj.Holsters().count(_.Equipment.nonEmpty) == 0 && obj.Inventory.Size == 0
  }

  /**
    * If the corpse has been well-looted, remove it from the ground.
    * @param obj the corpse
    * @return `true`, if the `obj` is actually a corpse and has no objects in its holsters or backpack;
    *        `false`, otherwise
    */
  def TryDisposeOfLootedCorpse(obj: Player): Boolean = {
    if (obj.isBackpack && WellLootedDeadBody(obj)) {
      obj.Zone.AvatarEvents ! AvatarServiceMessage.Corpse(RemoverActor.HurrySpecific(List(obj), obj.Zone))
      true
    } else {
      false
    }
  }

  /**
    * Attempt to tranfer to the player's faction-specific sanctuary continent.
    * If the server thinks the player is already on his sanctuary continent, and dead,
    * it will disconnect the player under the assumption that an error has occurred.
    * Eventually, this functionality should support better error-handling before it jumps to the conclusion:
    * "Disconnecting the client is the safest option."
    * @see `Zones.SanctuaryZoneNumber`
    * @param tplayer the player
    * @param currentZone the current zone number
    */
  def RequestSanctuaryZoneSpawn(tplayer: Player, currentZone: Int): Unit = {
    if (currentZone == Zones.sanctuaryZoneNumber(tplayer.Faction)) {
      log.error("RequestSanctuaryZoneSpawn called for player already in sanctuary.")
      sendResponse(DisconnectMessage("RequestSanctuaryZoneSpawn called for player already in sanctuary."))
      return
    }

    continent.GUID(player.VehicleSeated) match {
      case Some(obj: Vehicle) if !obj.Destroyed =>
        cluster ! InterstellarClusterService.GetRandomSpawnPoint(
          Zones.sanctuaryZoneNumber(player.Faction),
          player.Faction,
          Seq(SpawnGroup.WarpGate),
          context.self
        )
      case _ =>
        cluster ! InterstellarClusterService.GetRandomSpawnPoint(
          Zones.sanctuaryZoneNumber(player.Faction),
          player.Faction,
          Seq(SpawnGroup.Sanctuary),
          context.self
        )
    }
  }

  /**
    * na
    * @param terminal na
    */
  def HandleProximityTerminalUse(terminal: Terminal with ProximityUnit): Unit = {
    val term_guid      = terminal.GUID
    val targets        = FindProximityUnitTargetsInScope(terminal)
    val currentTargets = terminal.Targets
    targets.foreach(target => {
      if (!currentTargets.contains(target)) {
        StartUsingProximityUnit(terminal, target)
      } else if (targets.isEmpty) {
        log.warn(
          s"HandleProximityTerminalUse: ${player.Name} could not find valid targets to give to proximity unit ${terminal.Definition.Name}@${term_guid.guid}"
        )
      }
    })
  }

  /**
    * na
    * @param terminal na
    * @return na
    */
  def FindProximityUnitTargetsInScope(terminal: Terminal with ProximityUnit): Seq[PlanetSideGameObject] = {
    terminal.Definition.asInstanceOf[ProximityDefinition].TargetValidation.keySet collect {
      case EffectTarget.Category.Player                                   => Some(player)
      case EffectTarget.Category.Vehicle | EffectTarget.Category.Aircraft => continent.GUID(player.VehicleSeated)
    } collect {
      case Some(a) => a
    } toSeq
  }

  /**
    * Queue a proximity-base service.
    * @param terminal the proximity-based unit
    * @param target the entity that is being considered for terminal operation
    */
  def StartUsingProximityUnit(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject): Unit = {
    val term_guid = terminal.GUID
    //log.trace(s"StartUsingProximityUnit: ${player.Name} wants to use ${terminal.Definition.Name}@${term_guid.guid} on $target")
    if (player.isAlive) {
      target match {
        case _: Player =>
          terminal.Actor ! CommonMessages.Use(player, Some(target))
        case _: Vehicle =>
          terminal.Actor ! CommonMessages.Use(player, Some((target, continent.VehicleEvents)))
        case _ =>
          log.error(s"StartUsingProximityUnit: can not deal with target $target")
      }
      terminal.Definition match {
        case GlobalDefinitions.adv_med_terminal | GlobalDefinitions.medical_terminal =>
          usingMedicalTerminal = Some(term_guid)
        case _ => ;
      }
    }
  }

  /**
    * Determine which functionality to pursue by a generic proximity-functional unit given the target for its activity.
    * @see `VehicleService:receive, ProximityUnit.Action`
    * @param terminal the proximity-based unit
    * @param target the object being affected by the unit
    */
  def SelectProximityUnitBehavior(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject): Unit = {
    target match {
      case o: Player =>
        HealthAndArmorTerminal(terminal, o)
      case _ => ;
    }
  }

  /**
    * Stop using a proximity-base service.
    * Special note is warranted when determining the identity of the proximity terminal.
    * Medical terminals of both varieties can be cancelled by movement.
    * Other sorts of proximity-based units are put on a timer.
    * @param terminal the proximity-based unit
    */
  def StopUsingProximityUnit(terminal: Terminal with ProximityUnit): Unit = {
    val term_guid = terminal.GUID
    //log.trace(s"StopUsingProximityUnit: attempting to stop using proximity unit ${terminal.Definition.Name}@${term_guid.guid}")
    val targets = FindProximityUnitTargetsInScope(terminal)
    if (targets.nonEmpty) {
      if (usingMedicalTerminal.contains(term_guid)) {
        usingMedicalTerminal = None
      }
      targets.foreach(target => terminal.Actor ! CommonMessages.Unuse(player, Some(target)))
    } else {
      log.warn(
        s"StopUsingProximityUnit: ${player.Name} could not find valid targets for proximity unit ${terminal.Definition.Name}@${term_guid.guid}"
      )
    }
  }

  /**
    * na
    */
  def ForgetAllProximityTerminals(term_guid: PlanetSideGUID): Unit = {
    if (usingMedicalTerminal.contains(term_guid)) {
      usingMedicalTerminal = None
    }
  }

  /**
    * Cease all current interactions with proximity-based units.
    * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
    * This operations may invoke callback messages.
    * @see `postStop`
    */
  def CancelAllProximityUnits(): Unit = {
    continent.GUID(usingMedicalTerminal) match {
      case Some(terminal: Terminal with ProximityUnit) =>
        FindProximityUnitTargetsInScope(terminal).foreach(target =>
          terminal.Actor ! CommonMessages.Unuse(player, Some(target))
        )
        ForgetAllProximityTerminals(usingMedicalTerminal.get)
      case _ => ;
    }
  }

  /**
    * When standing on the platform of a(n advanced) medical terminal,
    * resotre the player's health and armor points (when they need their health and armor points restored).
    * If the player is both fully healed and fully repaired, stop using the terminal.
    * @param unit the medical terminal
    * @param target the player being healed
    */
  def HealthAndArmorTerminal(unit: Terminal with ProximityUnit, target: Player): Unit = {
    val medDef     = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val healAmount = medDef.HealAmount
    val healthFull: Boolean = if (healAmount != 0 && target.Health < target.MaxHealth) {
      target.History(HealFromTerm(PlayerSource(target), healAmount, 0, medDef))
      HealAction(target, healAmount)
    } else {
      true
    }
    val repairAmount = medDef.ArmorAmount
    val armorFull: Boolean = if (repairAmount != 0 && target.Armor < target.MaxArmor) {
      target.History(HealFromTerm(PlayerSource(target), 0, repairAmount, medDef))
      ArmorRepairAction(target, repairAmount)
    } else {
      true
    }
    if (healthFull && armorFull) {
      StopUsingProximityUnit(unit)
    }
  }

  /**
    * Restore, at most, a specific amount of health points on a player.
    * Send messages to connected client and to events system.
    * @param tplayer the player
    * @param healValue the amount to heal;
    *                    10 by default
    * @return whether the player can be repaired for any more health points
    */
  def HealAction(tplayer: Player, healValue: Int = 10): Boolean = {
    val player_guid = tplayer.GUID
    tplayer.Health = tplayer.Health + healValue
    sendResponse(PlanetsideAttributeMessage(player_guid, 0, tplayer.Health))
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlanetsideAttribute(player_guid, 0, tplayer.Health)
    )
    tplayer.Health == tplayer.MaxHealth
  }

  /**
    * Restore, at most, a specific amount of personal armor points on a player.
    * Send messages to connected client and to events system.
    * @param tplayer the player
    * @param repairValue the amount to repair;
    *                    10 by default
    * @return whether the player can be repaired for any more armor points
    */
  def ArmorRepairAction(tplayer: Player, repairValue: Int = 10): Boolean = {
    val player_guid = tplayer.GUID
    tplayer.Armor = tplayer.Armor + repairValue
    sendResponse(PlanetsideAttributeMessage(player_guid, 4, tplayer.Armor))
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlanetsideAttribute(player_guid, 4, tplayer.Armor)
    )
    tplayer.Armor == tplayer.MaxArmor
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to auto reverse them out
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to move in reverse
    */
  def ServerVehicleLockReverse(): Unit = {
    controlled = Some(0)
    sendResponse(
      ServerVehicleOverrideMsg(
        lock_accelerator = true,
        lock_wheel = true,
        reverse = true,
        unk4 = true,
        lock_vthrust = 0,
        lock_strafe = 1,
        movement_speed = 2,
        unk8 = Some(0)
      )
    )
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to strafe right out of the cargo hold for vehicles that are mounted sideways e.g. router/BFR
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to strafe right
    */
  def ServerVehicleLockStrafeRight(): Unit = {
    controlled = Some(0)
    sendResponse(
      ServerVehicleOverrideMsg(
        lock_accelerator = true,
        lock_wheel = true,
        reverse = false,
        unk4 = true,
        lock_vthrust = 0,
        lock_strafe = 3,
        movement_speed = 0,
        unk8 = Some(0)
      )
    )
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to strafe left out of the cargo hold for vehicles that are mounted sideways e.g. router/BFR
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to strafe left
    */
  def ServerVehicleLockStrafeLeft(): Unit = {
    controlled = Some(0)
    sendResponse(
      ServerVehicleOverrideMsg(
        lock_accelerator = true,
        lock_wheel = true,
        reverse = false,
        unk4 = true,
        lock_vthrust = 0,
        lock_strafe = 2,
        movement_speed = 0,
        unk8 = Some(0)
      )
    )
  }

  /**
    * Lock all applicable controls of the current vehicle.
    * This includes forward motion, turning, and, if applicable, strafing.
    * @param vehicle the vehicle being controlled
    */
  def ServerVehicleLock(vehicle: Vehicle): Unit = {
    controlled = Some(0)
    sendResponse(ServerVehicleOverrideMsg(true, true, false, false, 0, 1, 0, Some(0)))
  }

  /**
    * Place the current vehicle under the control of the server's commands.
    * @param vehicle the vehicle
    * @param speed how fast the vehicle is moving forward
    * @param flight whether the vehicle is ascending or not, if the vehicle is an applicable type
    */
  def ServerVehicleOverride(vehicle: Vehicle, speed: Int = 0, flight: Int = 0): Unit = {
    controlled = Some(speed)
    sendResponse(ServerVehicleOverrideMsg(true, true, false, false, flight, 0, speed, Some(0)))
  }

  /**
    * Place the current vehicle under the control of the driver's commands,
    * but leave it in a cancellable auto-drive.
    * @param vehicle the vehicle
    * @param speed how fast the vehicle is moving forward
    * @param flight whether the vehicle is ascending or not, if the vehicle is an applicable type
    */
  def DriverVehicleControl(vehicle: Vehicle, speed: Int = 0, flight: Int = 0): Unit = {
    if (controlled.nonEmpty) {
      controlled = None
      sendResponse(ServerVehicleOverrideMsg(false, false, false, true, flight, 0, speed, None))
    }
  }

  /**
    * Place the current vehicle under the control of the driver's commands,
    * but leave it in a cancellable auto-drive.
    * Stop all movement entirely.
    * @param vehicle the vehicle
    */
  def TotalDriverVehicleControl(vehicle: Vehicle): Unit = {
    if (controlled.nonEmpty) {
      controlled = None
      sendResponse(ServerVehicleOverrideMsg(false, false, false, false, 0, 0, 0, None))
    }
  }

  /**
    * Given a globally unique identifier in the 40100 to 40124 range
    * (with an optional 25 as buffer),
    * find a projectile.
    * @param projectile_guid the projectile's GUID
    * @return the discovered projectile
    */
  def FindProjectileEntry(projectile_guid: PlanetSideGUID): Option[Projectile] = {
    val index = projectile_guid.guid - Projectile.baseUID
    if (0 <= index && index < projectiles.length) {
      projectiles(index)
    } else {
      log.warn(s"ResolveProjectile: expected projectile, but ${projectile_guid.guid} not found")
      None
    }
  }

  /**
    * Find a projectile with the given globally unique identifier and mark it as a resolved shot.
    * A `Resolved` shot has either encountered an obstacle or is being cleaned up for not finding an obstacle.
    * @param projectile_guid the projectile GUID
    * @param resolution the resolution status to promote the projectile
    * @return the projectile
    */
  def ResolveProjectileEntry(
      projectile_guid: PlanetSideGUID,
      resolution: ProjectileResolution.Value,
      target: PlanetSideGameObject with FactionAffinity with Vitality,
      pos: Vector3
  ): Option[ResolvedProjectile] = {
    FindProjectileEntry(projectile_guid) match {
      case Some(projectile) =>
        ResolveProjectileEntry(projectile, resolution, target, pos)
      case None =>
        log.warn(s"ResolveProjectile: expected projectile, but ${projectile_guid.guid} not found")
        None
    }
  }

  /**
    * Find a projectile with the given globally unique identifier and mark it as a resolved shot.
    * @param projectile the projectile object
    * @param index where the projectile was found
    * @param resolution the resolution status to promote the projectile
    * @return a copy of the projectile
    */
  def ResolveProjectileEntry(
      projectile: Projectile,
      index: Int,
      resolution: ProjectileResolution.Value,
      target: PlanetSideGameObject with FactionAffinity with Vitality,
      pos: Vector3
  ): Option[ResolvedProjectile] = {
    if (!projectiles(index).contains(projectile)) {
      log.error(s"expected projectile could not be found at $index; can not resolve")
      None
    } else {
      ResolveProjectileEntry(projectile, resolution, target, pos)
    }
  }

  /**
    * na
    * @param projectile the projectile object
    * @param resolution the resolution status to promote the projectile
    * @return a copy of the projectile
    */
  def ResolveProjectileEntry(
      projectile: Projectile,
      resolution: ProjectileResolution.Value,
      target: PlanetSideGameObject with FactionAffinity with Vitality,
      pos: Vector3
  ): Option[ResolvedProjectile] = {
    if (projectile.isMiss) {
      log.error("expected projectile was already counted as a missed shot; can not resolve any further")
      None
    } else {
      projectile.Resolve()
      val outProjectile = if (projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated)) {
        val quality = projectile.profile.Aggravated match {
          case Some(aggravation)
              if aggravation.targets.exists(validation => validation.test(target)) &&
                aggravation.info.exists(_.damage_type == AggravatedDamage.basicDamageType(resolution)) =>
            ProjectileQuality.AggravatesTarget
          case _ =>
            ProjectileQuality.Normal
        }
        projectile.quality(quality)
      } else {
        projectile
      }
      Some(ResolvedProjectile(resolution, outProjectile, SourceEntry(target), target.DamageModel, pos))
    }
  }

  /**
    * Common activities/procedure when a player mounts a valid object.
    * @param tplayer the player
    * @param obj the mountable object
    * @param seatNum the seat into which the player is mounting
    */
  def MountingAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val player_guid: PlanetSideGUID = tplayer.GUID
    val obj_guid: PlanetSideGUID    = obj.GUID
    PlayerActionsToCancel()
    avatarActor ! AvatarActor.DeactivateActiveImplants()
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(3 seconds)
    log.info(s"MountVehicleMsg: ${player.Name}_guid mounts $obj @ $seatNum")
    sendResponse(ObjectAttachMessage(obj_guid, player_guid, seatNum))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.MountVehicle(player_guid, obj_guid, seatNum)
    )
  }

  /**
    * Common activities/procedure when a player dismounts a valid object.
    * @param tplayer the player
    * @param obj the mountable object
    * @param seatNum the seat out of which which the player is disembarking
    */
  def DismountAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val player_guid: PlanetSideGUID = tplayer.GUID
    log.info(s"DismountVehicleMsg: ${tplayer.Name} dismounts $obj from $seatNum")
    keepAliveFunc = NormalKeepAlive
    sendResponse(DismountVehicleMsg(player_guid, BailType.Normal, false))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.DismountVehicle(player_guid, BailType.Normal, false)
    )
  }

  /**
    * Calculate the amount of damage to be dealt to an active `target`
    * using the information reconstructed from a `Resolvedprojectile`
    * and affect the `target` in a synchronized manner.
    * The active `target` and the target of the `ResolvedProjectile` do not have be the same.
    * While the "tell" for being able to sustain damage is an entity of type `Vitality`,
    * only specific `Vitality` entity types are being screened for sustaining damage.
    * @see `DamageResistanceModel`
    * @see `Vitality`
    * @param target a valid game object that is known to the server
    * @param data a projectile that will affect the target
    */
  def HandleDealingDamage(target: PlanetSideGameObject with Vitality, data: ResolvedProjectile): Unit = {
    val func = data.damage_model.Calculate(data)
    target match {
      case obj: Player if obj.CanDamage && obj.Actor != Default.Actor =>
        // auto kick players damaging spectators
        if (obj.spectator && obj != player) {
          AdministrativeKick(player)
        } else {
          obj.Actor ! Vitality.Damage(func)
        }
      case obj: Vehicle if obj.CanDamage           => obj.Actor ! Vitality.Damage(func)
      case obj: Amenity if obj.CanDamage           => obj.Actor ! Vitality.Damage(func)
      case obj: ComplexDeployable if obj.CanDamage => obj.Actor ! Vitality.Damage(func)

      case obj: SimpleDeployable if obj.CanDamage =>
        //damage is synchronized on `LSA` (results returned to and distributed from this `WorldSessionActor`)
        continent.LocalEvents ! Vitality.DamageOn(obj, func)
      case _ => ;
    }
  }

  /**
    * Properly format a `DestroyDisplayMessage` packet
    * given sufficient information about a target (victim) and an actor (killer).
    * For the packet, the `charId` field is important for determining distinction between players.
    * @param killer the killer's entry
    * @param victim the victim's entry
    * @param method the manner of death
    * @param unk na;
    *            defaults to 121, the object id of `avatar`
    * @return a `DestroyDisplayMessage` packet that is properly formatted
    */
  def DestroyDisplayMessage(
      killer: SourceEntry,
      victim: SourceEntry,
      method: Int,
      unk: Int = 121
  ): DestroyDisplayMessage = {
    val killer_seated = killer match {
      case obj: PlayerSource => obj.Seated
      case _                 => false
    }
    val victim_seated = victim match {
      case obj: PlayerSource => obj.Seated
      case _                 => false
    }
    new DestroyDisplayMessage(
      killer.Name,
      killer.CharId,
      killer.Faction,
      killer_seated,
      unk,
      method,
      victim.Name,
      victim.CharId,
      victim.Faction,
      victim_seated
    )
  }

  /**
    * Initialize the deployables user interface elements.<br>
    * <br>
    * All element initializations require both the maximum deployable amount and the current deployables active counts.
    * Until initialized, all elements will be RED 0/0 as if the corresponding certification were not `learn`ed.
    * The respective element will become a pair of numbers, the second always being non-zero, when properly initialized.
    * The numbers will appear GREEN when more deployables of that type can be placed.
    * The numbers will appear RED if the player can not place any more of that type of deployable.
    * The numbers will appear YELLOW if the current deployable count is greater than the maximum count of that type
    * such as may be the case when a player `forget`s a certification.
    * @param list a tuple of each UI element with four numbers;
    *             even numbers are attribute ids;
    *             odd numbers are quantities;
    *             first pair is current quantity;
    *             second pair is maximum quantity
    */
  def UpdateDeployableUIElements(list: List[(Int, Int, Int, Int)]): Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach({
      case ((currElem, curr, maxElem, max)) =>
        //fields must update in ordered pairs: max, curr
        sendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    })
  }

  /**
    * Draw the icon for this deployable object.<br>
    * <br>
    * When a client first joins a zone, all deployables are drawn on the continent map once.
    * Should the player place any deployables, those deployables belong to that player.
    * Ownership causes icon to be drawn in yellow to the player (as opposed to a white icon)
    * and that signifies a certain level of control over the deployable, at least the ability to quietly deconstruct it.
    * Under normal death/respawn cycles while the player is in a given zone,
    * the map icons for owned deployables ramin manipulable to that given user.
    * They do not havwe to be redrawn to stay accurate.
    * Upon leaving a zone, where the icons are erased, and returning back to the zone, where they are drawn again,
    * the deployables that a player owned should be restored in terms of their map icon visibility.
    * This control can not be recovered, however, until they are updated with the player's globally unique identifier.
    * Since the player does not need to redraw his own deployable icons each time he respawns,
    * but will not possess a valid GUID for that zone until he spawns in it at least once,
    * this function is swapped with another after the first spawn in any given zone.
    * This function is restored upon transferring zones.
    * @see `DontRedrawIcons`
    * @see `SetCurrentAvatar`
    * @param obj a `Deployable` object
    */
  def RedrawDeployableIcons(obj: PlanetSideGameObject with Deployable): Unit = {
    val deployInfo = DeployableInfo(
      obj.GUID,
      Deployable.Icon(obj.Definition.Item),
      obj.Position,
      obj.Owner.getOrElse(PlanetSideGUID(0))
    )
    sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
  }

  /**
    * Do not draw any icon for this deployable object.<br>
    * <br>
    * When a client first joins a zone, all deployables are drawn on the continent map once.
    * Should the player place any deployables, those deployables belong to that player.
    * Ownership causes icon to be drawn in yellow to the player (as opposed to a white icon)
    * and that signifies a certain level of control over the deployable, at least the ability to quietly deconstruct it.
    * Under normal death/respawn cycles while the player is in a given zone,
    * the map icons for owned deployables remain manipulable by that given user.
    * They do not have to be redrawn to stay accurate.
    * Upon leaving a zone, where the icons are erased, and returning back to the zone, where they are drawn again,
    * the deployables that a player owned should be restored in terms of their map icon visibility.
    * This control can not be recovered, however, until they are updated with the player's globally unique identifier.
    * Since the player does not need to redraw his own deployable icons each time he respawns,
    * but will not possess a valid GUID for that zone until he spawns in it at least once,
    * this function swaps out with another after the first spawn in any given zone.
    * It stays swapped in until the player changes zones.
    * @see `RedrawDeployableIcons`
    * @see `SetCurrentAvatar`
    * @param obj a `Deployable` object
    */
  def DontRedrawIcons(obj: PlanetSideGameObject with Deployable): Unit = {}

  /**
    * The custom behavior responding to the message `ChangeFireModeMessage` for `ConstructionItem` game objects.
    * Each fire mode has sub-modes corresponding to a type of "deployable" as ammunition
    * and each of these sub-modes have certification requirements that must be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given mode and given sub-mode are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @see `FireModeSwitch.NextFireMode`
    * @see `PerformConstructionItemAmmoChange`
    * @param obj the `ConstructionItem` object
    * @param originalModeIndex the starting point fire mode index
    * @return the changed fire mode
    */
  def NextConstructionItemFireMode(obj: ConstructionItem, originalModeIndex: Int): ConstructionFireMode = {
    do {
      obj.NextFireMode
      if (!ConstructionItemPermissionComparison(player.avatar.certifications, obj.ModePermissions)) {
        PerformConstructionItemAmmoChange(obj, obj.AmmoTypeIndex)
      }
      sendResponse(ChangeFireModeMessage(obj.GUID, obj.FireModeIndex))
    } while (!ConstructionItemPermissionComparison(
      player.avatar.certifications,
      obj.ModePermissions
    ) && originalModeIndex != obj.FireModeIndex)
    obj.FireMode
  }

  /**
    * The custom behavior responding to the message `ChangeAmmoMessage` for `ConstructionItem` game objects.
    * Iterate through sub-modes corresponding to a type of "deployable" as ammunition for this fire mode
    * and check each of these sub-modes for their certification requirements to be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given ammunition are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @param obj the `ConstructionItem` object
    * @param originalAmmoIndex the starting point ammunition type mode index
    */
  def PerformConstructionItemAmmoChange(obj: ConstructionItem, originalAmmoIndex: Int): Unit = {
    do {
      obj.NextAmmoType
    } while (!ConstructionItemPermissionComparison(
      player.avatar.certifications,
      obj.ModePermissions
    ) && originalAmmoIndex != obj.AmmoTypeIndex)
    log.info(
      s"ChangeFireMode: construction object ${obj.Definition.Name} changed to ${obj.AmmoType} (mode ${obj.FireModeIndex})"
    )
    sendResponse(ChangeAmmoMessage(obj.GUID, obj.AmmoTypeIndex))
  }

  /**
    * Compare sets of certifications to determine if
    * the requested `Engineering`-like certification requirements of the one group can be found in a another group.
    * @see `CertificationType`
    * @param sample the certifications to be compared against
    * @param test the desired certifications
    * @return `true`, if the desired certification requirements are met; `false`, otherwise
    */
  def ConstructionItemPermissionComparison(
      sample: Set[Certification],
      test: Set[Certification]
  ): Boolean = {
    import Certification._
    val engineeringCerts: Set[Certification] = Set(AssaultEngineering, FortificationEngineering)
    val testDiff: Set[Certification]         = test diff (engineeringCerts ++ Set(AdvancedEngineering))
    //substitute `AssaultEngineering` and `FortificationEngineering` for `AdvancedEngineering`
    val sampleIntersect = if (sample contains AdvancedEngineering) {
      engineeringCerts
    } else {
      sample intersect engineeringCerts
    }
    val testIntersect = if (test contains AdvancedEngineering) {
      engineeringCerts
    } else {
      test intersect engineeringCerts
    }
    (sample intersect testDiff equals testDiff) && (sampleIntersect intersect testIntersect equals testIntersect)
  }

  /**
    * Common actions related to constructing a new `Deployable` object in the game environment.<br>
    * <br>
    * Besides the standard `ObjectCreateMessage` packet that produces the model and game object on the client,
    * two messages are dispatched in accordance with enforced deployable limits.
    * The first limit of note is the actual number of a specific type of deployable can be placed.
    * The second limit of note is the actual number of a specific group (category) of deployables that can be placed.
    * For example, the player can place 25 mines but that count adds up all types of mines;
    * specific mines have individual limits such as 25 and 5 and only that many of that type can be placed at once.
    * Depending on which limit is encountered, an "oldest entry" is struck from the list to make space.
    * This generates the first message - "@*OldestDestroyed."
    * The other message is generated if the number of that specific type of deployable
    * or the number of deployables available in its category
    * matches against the maximum count allowed.
    * This generates the second message - "@*LimitReached."
    * These messages are mutually exclusive, with "@*OldestDestroyed" taking priority over "@*LimitReached."<br>
    * <br>
    * The map icon for the deployable just introduced is also created on the clients of all faction-affiliated players.
    * This icon is important as, short of destroying it,
    * the owner has no other means of controlling the created object that it is associated with.
    * @param obj the `Deployable` object to be built
    */
  def DeployableBuildActivity(obj: PlanetSideGameObject with Deployable): Unit = {
    val guid        = obj.GUID
    val definition  = obj.Definition
    val item        = definition.Item
    val deployables = avatar.deployables
    val (curr, max) = deployables.CountDeployable(item)
    log.info(s"DeployableBuildActivity: ${definition.Name}")
    //two potential messages related to numerical limitations of deployables
    if (!avatar.deployables.Available(obj)) {
      val (removed, msg) = {
        if (curr == max) { //too many of a specific type of deployable
          (deployables.DisplaceFirst(obj), max > 1)
        } else { //make room by eliminating a different type of deployable
          (deployables.DisplaceFirst(obj, { d => d.Definition.Item != item }), true)
        }
      }
      removed match {
        case Some(telepad: TelepadDeployable) =>
          telepad.AssignOwnership(None)
          continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(telepad), continent))
          continent.LocalEvents ! LocalServiceMessage.Deployables(
            RemoverActor.AddTask(telepad, continent, Some(0 seconds))
          ) //normal decay
        case Some(old) =>
          old.AssignOwnership(None)
          continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(old), continent))
          continent.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.AddTask(old, continent, Some(0 seconds)))
          if (msg) { //max test
            sendResponse(
              ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}OldestDestroyed", None)
            )
          }
        case None => ; //should be an invalid case
          log.warn(
            s"DeployableBuildActivity: how awkward: we probably shouldn't be allowed to build this deployable right now"
          )
      }
    } else if (obj.isInstanceOf[TelepadDeployable]) {
      //always treat the telepad we are putting down as the first and only one
      sendResponse(ObjectDeployedMessage.Success(definition.Name, 1, 1))
    } else {
      sendResponse(ObjectDeployedMessage.Success(definition.Name, curr + 1, max))
      val (catCurr, catMax) = deployables.CountCategory(item)
      if ((max > 1 && curr + 1 == max) || (catMax > 1 && catCurr + 1 == catMax)) {
        sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}LimitReached", None))
      }
    }
    avatar.deployables.Add(obj)
    UpdateDeployableUIElements(avatar.deployables.UpdateUIElement(item))
    sendResponse(GenericObjectActionMessage(guid, 21)) //reset build cooldown
    sendResponse(ObjectCreateMessage(definition.ObjectId, guid, definition.Packet.ConstructorData(obj).get))
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.DeployItem(player.GUID, obj))
    //map icon
    val deployInfo = DeployableInfo(guid, Deployable.Icon(item), obj.Position, obj.Owner.getOrElse(PlanetSideGUID(0)))
    sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
    continent.LocalEvents ! LocalServiceMessage(
      s"${player.Faction}",
      LocalAction.DeployableMapIcon(player.GUID, DeploymentAction.Build, deployInfo)
    )
  }

  /**
    * If the tool is a form of field deployment unit (FDU, also called an `advanced_ace`),
    * completely remove the object from its current position and place it on the ground.
    * In the case of a botched deployable construction, dropping the FDU is visually consistent
    * as it should already be depicted as on the ground as a part of its animation cycle.
    * @param tool the `ConstructionItem` object currently in the slot (checked)
    * @param index the slot index
    * @param pos where to drop the object in the game world
    */
  def TryDropFDU(tool: ConstructionItem, index: Int, pos: Vector3): Unit = {
    if (tool.Definition == GlobalDefinitions.advanced_ace) {
      DropEquipmentFromInventory(player)(tool, Some(pos))
    }
  }

  /**
    * Destroy a `ConstructionItem` object that can be found in the indexed slot.
    * @see `Player.Find`
    * @param tool the `ConstructionItem` object currently in the slot (checked)
    * @param index the slot index
    */
  def CommonDestroyConstructionItem(tool: ConstructionItem, index: Int): Unit = {
    if (SafelyRemoveConstructionItemFromSlot(tool, index, "CommonDestroyConstructionItem")) {
      continent.tasks ! GUIDTask.UnregisterEquipment(tool)(continent.GUID)
    }
  }

  /**
    * Find the target `ConstructionTool` object, either at the suggested slot or wherever it is on the `player`,
    * and remove it from the game world visually.<br>
    * <br>
    * Not finding the target object at its intended slot is an entirely recoverable situation
    * as long as the target object is discovered to be somewhere else in the player's holsters or inventory space.
    * If found after a more thorough search, merely log the discrepancy as a warning.
    * If the discrepancy becomes common, the developer messed up the function call
    * or he should not be using this function.
    * @param tool the `ConstructionItem` object currently in the slot (checked)
    * @param index the slot index
    * @param logDecorator what kind of designation to give any log entires originating from this function;
    *                     defaults to its own function name
    * @return `true`, if the target object was found and removed;
    *        `false`, otherwise
    */
  def SafelyRemoveConstructionItemFromSlot(
      tool: ConstructionItem,
      index: Int,
      logDecorator: String = "SafelyRemoveConstructionItemFromSlot"
  ): Boolean = {
    if ({
      val holster = player.Slot(index)
      if (holster.Equipment.contains(tool)) {
        holster.Equipment = None
        true
      } else {
        player.Find(tool) match {
          case Some(newIndex) =>
            log.warn(s"$logDecorator: looking for item in $index, but item was found at $newIndex instead")
            player.Slot(newIndex).Equipment = None
            true
          case None =>
            log.error(s"$logDecorator: could not find the target ${tool.Definition.Name}")
            false
        }
      }
    }) {
      sendResponse(ObjectDeleteMessage(tool.GUID, 0))
      continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(player.GUID, tool.GUID))
      true
    } else {
      false
    }
  }

  /**
    * Find a `ConstructionItem` object in player's inventory
    * that is the same type as a target `ConstructionItem` object and
    * transfer it into the designated slot index, usually a holster.
    * Draw that holster.
    * After being transferred, the replacement should be reconfigured to match the fire mode of the original.
    * The primary use of this operation is following the successful manifestation of a deployable in the game world.<br>
    * <br>
    * As this function should be used in response to some other action such as actually placing a deployable,
    * do not instigate bundling from within the function's scope.
    * @see `WorldSessionActor.FinalizeDeployable`<br>
    *       `FindEquipmentStock`
    * @param tool the `ConstructionItem` object to match
    * @param index where to put the discovered replacement
    */
  def FindReplacementConstructionItem(tool: ConstructionItem, index: Int): Unit = {
    val fireMode   = tool.FireModeIndex
    val ammoType   = tool.AmmoTypeIndex
    val definition = tool.Definition

    if (player.Slot(index).Equipment.isEmpty) {
      FindEquipmentStock(player, { (e) => e.Definition == definition }, 1) match {
        case x :: _ =>
          val guid = player.GUID
          val obj  = x.obj.asInstanceOf[ConstructionItem]
          if ((player.Slot(index).Equipment = obj).contains(obj)) {
            player.Inventory -= x.start
            sendResponse(ObjectAttachMessage(guid, obj.GUID, index))

            if (obj.FireModeIndex != fireMode) {
              obj.FireModeIndex = fireMode
              sendResponse(ChangeFireModeMessage(obj.GUID, fireMode))
            }
            if (obj.AmmoTypeIndex != ammoType) {
              obj.AmmoTypeIndex = ammoType
              sendResponse(ChangeAmmoMessage(obj.GUID, ammoType))
            }
            if (player.VisibleSlots.contains(index)) {
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.EquipmentInHand(guid, guid, index, obj)
              )
              if (player.DrawnSlot == Player.HandsDownSlot) {
                player.DrawnSlot = index
                sendResponse(ObjectHeldMessage(guid, index, false))
                continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectHeld(guid, index))
              }
            }
          }
        case Nil => ; //no replacements found
      }
    } else {
      log.warn(
        s"FindReplacementConstructionItem: slot $index needs to be empty before a replacement ${definition.Name} can be installed"
      )
    }
  }

  /**
    * A simple object searching algorithm that is limited to containers currently known and accessible by the player.
    * If all relatively local containers are checked and the object is not found,
    * the player's locker inventory will be checked, and then
    * the game environment (items on the ground) will be checked too.
    * If the target object is discovered, it is removed from its current location and is completely destroyed.
    * @see `RequestDestroyMessage`<br>
    *       `Zone.ItemIs.Where`
    * @param object_guid the target object's globally unique identifier;
    *                    it is not expected that the object will be unregistered, but it is also not gauranteed
    * @param obj the target object
    * @return `true`, if the target object was discovered and removed;
    *        `false`, otherwise
    */
  def FindEquipmentToDelete(object_guid: PlanetSideGUID, obj: Equipment): Boolean = {
    val findFunc
        : PlanetSideServerObject with Container => Option[(PlanetSideServerObject with Container, Option[Int])] =
      FindInLocalContainer(object_guid)

    findFunc(player)
      .orElse(accessedContainer match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      })
      .orElse(FindLocalVehicle match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      }) match {
      case Some((parent, Some(slot))) =>
        obj.Position = Vector3.Zero
        RemoveOldEquipmentFromInventory(parent)(obj)
        log.info(s"RequestDestroy: equipment $obj")
        true

      case _ =>
        if (player.avatar.locker.Inventory.Remove(object_guid)) {
          sendResponse(ObjectDeleteMessage(object_guid, 0))
          log.info(s"RequestDestroy: equipment $obj")
          true
        } else if (continent.EquipmentOnGround.contains(obj)) {
          obj.Position = Vector3.Zero
          continent.Ground ! Zone.Ground.RemoveItem(object_guid)
          continent.AvatarEvents ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
          log.info(s"RequestDestroy: equipment $obj on ground")
          true
        } else {
          log.warn(s"RequestDestroy: equipment $obj exists, but can not be reached")
          false
        }
    }
  }

  /**
    * Common behavior for deconstructing expended explosive deployables in the game environment.
    * @param obj the deployable
    * @param guid the globally unique identifier for the deployable
    * @param pos the previous position of the deployable
    */
  def DeconstructDeployable(obj: PlanetSideGameObject with Deployable, guid: PlanetSideGUID, pos: Vector3): Unit = {
    sendResponse(SetEmpireMessage(guid, PlanetSideEmpire.NEUTRAL)) //for some, removes the green marker circle
    sendResponse(ObjectDeleteMessage(guid, 0))
    if (player.Faction == obj.Faction) {
      sendResponse(
        DeployableObjectsInfoMessage(
          DeploymentAction.Dismiss,
          DeployableInfo(guid, Deployable.Icon(obj.Definition.Item), pos, obj.Owner.getOrElse(PlanetSideGUID(0)))
        )
      )
    }
  }

  /**
    * Common behavior for deconstructing deployables in the game environment.
    * @param obj the deployable
    * @param guid the globally unique identifier for the deployable
    * @param pos the previous position of the deployable
    * @param orient the previous orientation of the deployable
    * @param deletionType the value passed to `ObjectDeleteMessage` concerning the deconstruction animation
    */
  def DeconstructDeployable(
      obj: PlanetSideGameObject with Deployable,
      guid: PlanetSideGUID,
      pos: Vector3,
      orient: Vector3,
      deletionType: Int
  ): Unit = {
    sendResponse(SetEmpireMessage(guid, PlanetSideEmpire.NEUTRAL)) //for some, removes the green marker circle
    sendResponse(TriggerEffectMessage("spawn_object_failed_effect", pos, orient))
    sendResponse(PlanetsideAttributeMessage(guid, 29, 1)) //make deployable vanish
    sendResponse(ObjectDeleteMessage(guid, deletionType))
    if (player.Faction == obj.Faction) {
      sendResponse(
        DeployableObjectsInfoMessage(
          DeploymentAction.Dismiss,
          DeployableInfo(guid, Deployable.Icon(obj.Definition.Item), pos, obj.Owner.getOrElse(PlanetSideGUID(0)))
        )
      )
    }
  }

  /**
    * Search through the player's holsters and their inventory space
    * and remove all `BoomerTrigger` objects, both functionally and visually.
    * @return all discovered `BoomTrigger` objects
    */
  def RemoveBoomerTriggersFromInventory(): List[BoomerTrigger] = {
    val holstersWithIndex = player.Holsters().zipWithIndex
    ((player.Inventory.Items.collect({ case InventoryItem(obj: BoomerTrigger, index) => (obj, index) })) ++
      (holstersWithIndex
        .map({ case ((slot, index)) => (slot.Equipment, index) })
        .collect { case ((Some(obj: BoomerTrigger), index)) => (obj, index) }))
      .map({
        case ((obj, index)) =>
          player.Slot(index).Equipment = None
          sendResponse(ObjectDeleteMessage(obj.GUID, 0))
          if (player.VisibleSlots.contains(index) && player.HasGUID) {
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ObjectDelete(player.GUID, obj.GUID)
            )
          }
          obj
      })
  }

  /**
    * The starting point of behavior for a player who:
    * is dead and is respawning;
    * is deconstructing at a spawn tube and is respawning;
    * is using a warp gate; or,
    * any or none of the previous conditions, but the final result involves changing what zone the player occupies.
    * This route is not taken when first spawning in the game world, unless special conditions need to be satisfied.
    * The visible result will be apparent by the respawn timer being displayed to the client over the deployment map.<br>
    * <br>
    * Two choices must be independently made to complete this part of the process.
    * The first choice ivolves the state of the player who is spawning
    * as the known entry state involve either being alive or being dead.
    * A dead player (technically, a "corpse" that can no longer be revived) is embodied
    * in a completely new player with a new globally unique identifier and a whole new inventory.
    * A player who is transferring continents also satisfies the requirements
    * for obtaining a completely new globally unique identifier,
    * though the new identifier belongs to the new zone rather than the previous (still current) one.
    * The second choice is satisfied by respawning in the same zone while still in a state of still being alive.
    * In this singular case, the player retains his previous globally unique identifier.
    * In all other cases, as indicated, a new globally unique identifier is selected.<br>
    * <br>
    * If the player is alive and mounted in a vehicle, a different can of worms is produced.
    * The ramifications of these conditions are not fully satisfied until the player loads into the new zone.
    * Even then, the conclusion becomes delayed while a slightly lagged mechanism hoists players between zones.
    *
    * @param zoneId      the zone in which the player will be placed
    * @param pos         the game world coordinates where the player will be positioned
    * @param ori         the direction in which the player will be oriented
    * @param respawnTime the character downtime spent respawning, as clocked on the redeployment screen;
    *                    does not factor in any time required for loading zone or game objects
    */
  def LoadZonePhysicalSpawnPoint(zoneId: String, pos: Vector3, ori: Vector3, respawnTime: FiniteDuration): Unit = {
    log.info(s"Load in zone $zoneId at position $pos in $respawnTime")
    respawnTimer.cancel()
    reviveTimer.cancel()
    deadState = DeadState.RespawnTime
    sendResponse(
      AvatarDeadStateMessage(
        DeadState.RespawnTime,
        respawnTime.toMillis,
        respawnTime.toMillis,
        Vector3.Zero,
        player.Faction,
        unk5 = true
      )
    )
    shiftPosition = Some(pos)
    shiftOrientation = Some(ori)

    respawnTimer = context.system.scheduler.scheduleOnce(respawnTime) {
      if (player.isBackpack) { // if the player is dead, he is handled as dead infantry, even if he died in a vehicle
        // new player is spawning
        val newPlayer = RespawnClone(player)
        newPlayer.Position = pos
        newPlayer.Orientation = ori
        LoadZoneAsPlayer(newPlayer, zoneId)
      } else {
        avatarActor ! AvatarActor.DeactivateActiveImplants()
        interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
          case Some(vehicle: Vehicle) => // driver or passenger in vehicle using a warp gate, or a droppod
            LoadZoneInVehicle(vehicle, pos, ori, zoneId)

          case _ if player.HasGUID => // player is deconstructing self or instant action
            val player_guid = player.GUID
            sendResponse(ObjectDeleteMessage(player_guid, 4))
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ObjectDelete(player_guid, player_guid, 4)
            )
            player.Position = pos
            player.Orientation = ori
            LoadZoneAsPlayer(player, zoneId)

          case _ => //player is logging in
            player.Position = pos
            player.Orientation = ori
            LoadZoneAsPlayer(player, zoneId)
        }
      }
    }

  }

  /**
    * Deal with a target player as free-standing infantry in the course of a redeployment action to a target continent
    * whether that action is the result of a deconstruction (reconstruction), a death (respawning),
    * or other position shifting action handled directly by the server.
    *
    * The two important vectors are still whether the zone being transported to is the same or is different
    * and whether the target player is alive or released (note: not just "dead" ...).
    *
    * @param targetPlayer the target player being moved around;
    *                     not necessarily the same player as the `WorldSessionActor`-global `player`
    * @param zoneId       the zone in which the player will be placed
    */
  def LoadZoneAsPlayer(targetPlayer: Player, zoneId: String): Unit = {
    log.debug(s"loadZoneAsPlayer ${targetPlayer.avatar.name} $zoneId")
    if (!zoneReload && zoneId == continent.id) {
      if (player.isBackpack) { // important! test the actor-wide player ref, not the parameter
        // respawning from unregistered player
        continent.tasks ! RegisterAvatar(targetPlayer)
      } else {
        // move existing player; this is the one case where the original GUID is retained by the player
        self ! PlayerLoaded(targetPlayer)
      }
    } else {
      LoadZoneCommonTransferActivity()
      val original = player
      if (player.isBackpack) {
        session = session.copy(player = targetPlayer)
        taskThenZoneChange(
          GUIDTask.UnregisterObjectTask(original.avatar.locker)(continent.GUID),
          InterstellarClusterService.FindZone(_.id == zoneId, context.self)
        )
      } else if (player.HasGUID) {
        taskThenZoneChange(
          GUIDTask.UnregisterAvatar(original)(continent.GUID),
          InterstellarClusterService.FindZone(_.id == zoneId, context.self)
        )
      } else {
        cluster ! InterstellarClusterService.FindZone(_.id == zoneId, context.self)
      }

    }
  }

  /**
    * Deal with a target player as a vehicle occupant in the course of a redeployment action to a target continent
    * whether that action is the result of a deconstruction (reconstruction)
    * or other position shifting action handled directly by the server.<br>
    * <br>
    * The original target player must be alive and the only consideration is in what position the player is mounted in the vehicle.
    * Any seated position that isn't the driver is a passenger.
    * The most important role performed in this function is to declare a reference to the vehicle itsself
    * since no other connection from the player to the vehicle is guaranteed to persist in a meaningful way during the transfer.
    *
    * @param vehicle the target vehicle being moved around;
    *                WILL necessarily be the same vehicles as is controlled by the `WorldSessionActor`-global `player`
    * @param pos     the game world coordinates where the vehicle will be positioned
    * @param ori     the direction in which the vehicle will be oriented
    * @param zone_id the zone in which the vehicle and driver will be placed,
    *                or in which the vehicle has already been placed
    * @return a tuple composed of an `ActorRef` destination and a message to send to that destination
    */
  def LoadZoneInVehicle(vehicle: Vehicle, pos: Vector3, ori: Vector3, zone_id: String): Unit = {
    interstellarFerry = Some(vehicle)
    if (vehicle.PassengerInSeat(player).contains(0)) {
      vehicle.Position = pos
      vehicle.Orientation = ori
      LoadZoneInVehicleAsDriver(vehicle, zone_id)
    } else {
      LoadZoneInVehicleAsPassenger(vehicle, zone_id)
    }
  }

  /**
    * Deal with a target player as a vehicle driver in the course of a redeployment action to a target continent
    * whether that action is the result of a deconstruction (reconstruction)
    * or other position shifting action handled directly by the server.<br>
    * <br>
    * During a vehicle transfer, whether to the same zone or to a different zone,
    * the driver has the important task of ensuring the certain safety of his passengers during transport.
    * The driver must modify the conditions of the vehicle's passengers common communication channel
    * originally determined entirely by the vehicle's soon-to-be blanked internal `Actor` object.
    * Any cargo vehicles under the control of the target vehicle must also be made aware of the current state of the process.
    * In the case of a series of ferrying vehicles and cargo vehicles,
    * the vehicle to be deleted might not be the one immediately mounted.
    * A reference to the top-level ferrying vehicle's former globally unique identifier has been retained for this purpose.
    * This vehicle can be deleted for everyone if no more work can be detected.
    *
    * @param vehicle the target vehicle being moved around;
    *                WILL necessarily be the same vehicles as is controlled by the `WorldSessionActor`-global `player`
    * @param zoneId  the zone in which the vehicle and driver will be placed,
    *                or in which the vehicle has already been placed
    * @return a tuple composed of an `ActorRef` destination and a message to send to that destination
    */
  def LoadZoneInVehicleAsDriver(vehicle: Vehicle, zoneId: String): Unit = {
    log.info(s"LoadZoneInVehicleAsDriver: ${player.Name} is driving a ${vehicle.Definition.Name}")
    val manifest = vehicle.PrepareGatingManifest()
    log.info(s"$manifest")
    val pguid     = player.GUID
    val toChannel = manifest.file
    val topLevel  = interstellarFerryTopLevelGUID.getOrElse(vehicle.GUID)
    continent.VehicleEvents ! VehicleServiceMessage(
      s"${vehicle.Actor}",
      VehicleAction.TransferPassengerChannel(pguid, s"${vehicle.Actor}", toChannel, vehicle, topLevel)
    )
    manifest.cargo.foreach {
      case ("MISSING_DRIVER", index) =>
        val cargo = vehicle.CargoHolds(index).Occupant.get
        log.error(s"LoadZoneInVehicleAsDriver: eject cargo in hold $index; vehicle missing driver")
        CargoBehavior.HandleVehicleCargoDismount(cargo.GUID, cargo, vehicle.GUID, vehicle, false, false, true)
      case (name, index) =>
        val cargo = vehicle.CargoHolds(index).Occupant.get
        continent.VehicleEvents ! VehicleServiceMessage(
          name,
          VehicleAction.TransferPassengerChannel(pguid, s"${cargo.Actor}", toChannel, cargo, topLevel)
        )
    }
    //
    if (!zoneReload && zoneId == continent.id) {
      if (vehicle.Definition == GlobalDefinitions.droppod) {
        //instant action droppod in the same zone
        continent.tasks ! RegisterDroppod(vehicle, player)
      } else {
        //transferring a vehicle between spawn points (warp gates) in the same zone
        self ! PlayerLoaded(player)
      }
    } else if (vehicle.Definition == GlobalDefinitions.droppod) {
      LoadZoneCommonTransferActivity()
      player.Continent = zoneId //forward-set the continent id to perform a test
      taskThenZoneChange(
        GUIDTask.UnregisterAvatar(player)(continent.GUID),
        InterstellarClusterService.FindZone(_.id == zoneId, context.self)
      )
    } else {
      UnaccessContainer(vehicle)
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zoneId //forward-set the continent id to perform a test
      interstellarFerryTopLevelGUID =
        if (
          manifest.passengers.isEmpty && manifest.cargo.count { case (name, _) => !name.equals("MISSING_DRIVER") } == 0
        ) {
          //do not delete if vehicle has passengers or cargo
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.UnloadVehicle(pguid, vehicle, topLevel)
          )
          None
        } else {
          Some(topLevel)
        }
      //unregister vehicle and driver whole + GiveWorld
      continent.Transport ! Zone.Vehicle.Despawn(vehicle)
      taskThenZoneChange(
        UnregisterDrivenVehicle(vehicle, player),
        InterstellarClusterService.FindZone(_.id == zoneId, context.self)
      )
    }
  }

  /**
    * Deal with a target player as a vehicle passenger in the course of a redeployment action to a target continent
    * whether that action is the result of a deconstruction (reconstruction)
    * or other position shifting action handled directly by the server.<br>
    * <br>
    * The way a vehicle is handled in reference to being a passenger
    * is very similar to how an infantry player is handled in the same process.
    * If this player is the last person who requires a zone change
    * which is the concluding zone transfer of what might have been a long chain of vehicle and passengers
    * then that player is responsible for deleting the vehicle for other players of the previous zone.
    * In the case of a series of ferrying vehicles and cargo vehicles,
    * the vehicle to be deleted might not be the one immediately mounted.
    * A reference to the top-level ferrying vehicle's former globally unique identifier has been retained for this purpose.
    * This vehicle can be deleted for everyone if no more work can be detected.
    *
    * @see `GUIDTask.UnregisterPlayer`
    * @see `LoadZoneCommonTransferActivity`
    * @see `Vehicles.AllGatedOccupantsInSameZone`
    * @see `PlayerLoaded`
    * @see `TaskBeforeZoneChange`
    * @see `UnaccessContainer`
    * @param vehicle the target vehicle being moved around
    * @param zoneId  the zone in which the vehicle and driver will be placed
    * @return a tuple composed of an `ActorRef` destination and a message to send to that destination
    */
  def LoadZoneInVehicleAsPassenger(vehicle: Vehicle, zoneId: String): Unit = {
    log.info(s"LoadZoneInVehicleAsPassenger: ${player.Name} is the passenger of a ${vehicle.Definition.Name}")
    if (!zoneReload && zoneId == continent.id) {
      //transferring a vehicle between spawn points (warp gates) in the same zone
      self ! PlayerLoaded(player)
    } else {
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zoneId //forward-set the continent id to perform a test
      val continentId = continent.id
      interstellarFerryTopLevelGUID = None

      taskThenZoneChange(
        GUIDTask.UnregisterAvatar(player)(continent.GUID),
        InterstellarClusterService.FindZone(_.id == zoneId, context.self)
      )
    }
  }

  /**
    * Dispatch messages to all target players in immediate passenger and gunner seats
    * and to the driver of all vehicles in cargo holds
    * that their current ferrying vehicle is being transported from one zone to the next
    * and that they should follow after it.
    * The messages address the avatar of their recipient `WorldSessionActor` objects.
    * @param player_guid the driver of the target vehicle
    * @param toZoneId the zone where the target vehicle will be moved
    * @param vehicle the vehicle (object)
    */
  def LoadZoneTransferPassengerMessages(player_guid: PlanetSideGUID, toZoneId: String, vehicle: Vehicle): Unit = {
    vehicle.PublishGatingManifest() match {
      case Some(manifest) =>
        val toChannel = manifest.file
        val topLevel  = interstellarFerryTopLevelGUID.getOrElse(vehicle.GUID)
        galaxyService ! GalaxyServiceMessage(
          toChannel,
          GalaxyAction.TransferPassenger(player_guid, toChannel, vehicle, topLevel, manifest)
        )
        vehicle.CargoHolds.values
          .collect {
            case hold if hold.isOccupied =>
              val cargo = hold.Occupant.get
              cargo.Continent = toZoneId
              //point to the cargo vehicle to instigate cargo vehicle driver transportation
              galaxyService ! GalaxyServiceMessage(
                toChannel,
                GalaxyAction.TransferPassenger(player_guid, toChannel, vehicle, topLevel, manifest)
              )
          }
      case None =>
        log.error("LoadZoneTransferPassengerMessages: expected a manifest for zone transfer; got nothing")
    }
  }

  /** Before changing zones, perform the following task (which can be a nesting of subtasks). */
  def taskThenZoneChange(
      task: TaskResolver.GiveTask,
      zoneMessage: InterstellarClusterService.FindZone
  ): Unit = {
    continent.tasks ! TaskResolver.GiveTask(
      new Task() {
        override def isComplete: Task.Resolution.Value = task.task.isComplete

        def Execute(resolver: ActorRef): Unit = {
          continent.Population ! Zone.Population.Leave(avatar)
          cluster ! zoneMessage
          resolver ! Success(this)
        }
      },
      List(task)
    )
  }

  /**
    * Common behavior when transferring between zones
    * encompassing actions that disassociate the player with entities they left (will leave) in the previous zone.
    * It also sets up actions for the new zone loading process.
    */
  def LoadZoneCommonTransferActivity(): Unit = {
    zoneLoaded = None
    zoneReload = false
    if (player.avatar.vehicle.nonEmpty && player.VehicleSeated != player.avatar.vehicle) {
      continent.GUID(player.avatar.vehicle) match {
        case Some(vehicle: Vehicle) if vehicle.Actor != Default.Actor =>
          vehicle.Actor ! Vehicle.Ownership(None)
        case _ => ;
      }
      avatarActor ! AvatarActor.SetVehicle(None)
    }
    RemoveBoomerTriggersFromInventory().foreach(obj => {
      continent.tasks ! GUIDTask.UnregisterObjectTask(obj)(continent.GUID)
    })
    Deployables.Disown(continent, avatar, self)
    drawDeloyableIcon = RedrawDeployableIcons //important for when SetCurrentAvatar initializes the UI next zone
    squadSetup = ZoneChangeSquadSetup
  }

  /**
    * Attempt to link the router teleport system using the provided terminal information.
    * Although additional states are necessary to properly use the teleportation system,
    * e.g., deployment state, active state of the endpoints, etc.,
    * this decision is not made factoring those other conditions.
    * @param router the vehicle that houses one end of the teleportation system (the `InternalTelepad` object)
    * @param systemPlan specific object identification of the two endpoints of the teleportation system;
    *                   if absent, the knowable endpoint is deleted from the client reflexively
    */
  def ToggleTeleportSystem(router: Vehicle, systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]): Unit = {
    systemPlan match {
      case Some((internalTelepad, remoteTelepad)) =>
        LinkRouterToRemoteTelepad(router, internalTelepad, remoteTelepad)
      case _ =>
        router.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) =>
            sendResponse(ObjectDeleteMessage(util.GUID, 0))
          case _ => ;
        }
    }
  }

  /**
    * Link the router teleport system using the provided terminal information.
    * The internal telepad is made known of the remote telepad, creating the link.
    * @param router the vehicle that houses one end of the teleportation system (the `internalTelepad`)
    * @param internalTelepad the endpoint of the teleportation system housed by the router
    * @param remoteTelepad the endpoint of the teleportation system that exists in the environment
    */
  def LinkRouterToRemoteTelepad(
      router: Vehicle,
      internalTelepad: Utility.InternalTelepad,
      remoteTelepad: TelepadDeployable
  ): Unit = {
    internalTelepad.Telepad = remoteTelepad.GUID //necessary; backwards link to the (new) telepad
    CreateRouterInternalTelepad(router, internalTelepad)
    LinkRemoteTelepad(remoteTelepad.GUID)
  }

  /**
    * Create the mechanism that serves as one endpoint of the linked router teleportation system.<br>
    * <br>
    * Technically, the mechanism - an `InternalTelepad` object - is always made to exist
    * due to how the Router vehicle object is encoded into an `ObjectCreateMessage` packet.
    * Regardless, that internal mechanism is created anew each time the system links a new remote telepad.
    * @param router the vehicle that houses one end of the teleportation system (the `internalTelepad`)
    * @param internalTelepad the endpoint of the teleportation system housed by the router
    */
  def CreateRouterInternalTelepad(router: Vehicle, internalTelepad: PlanetSideGameObject with TelepadLike): Unit = {
    //create the interal telepad each time the link is made
    val rguid = router.GUID
    val uguid = internalTelepad.GUID
    val udef  = internalTelepad.Definition
    /*
    the following instantiation and configuration creates the internal Router component
    normally dispatched while the Router is transitioned into its Deploying state
    it is safe, however, to perform these actions at any time during and after the Deploying state
     */
    sendResponse(
      ObjectCreateMessage(
        udef.ObjectId,
        uguid,
        ObjectCreateMessageParent(rguid, 2), //TODO stop assuming slot number
        udef.Packet.ConstructorData(internalTelepad).get
      )
    )
    sendResponse(GenericObjectActionMessage(uguid, 27))
    sendResponse(GenericObjectActionMessage(uguid, 30))
    /*
    the following configurations create the interactive beam underneath the Deployed Router
    normally dispatched after the warm-up timer has completed
     */
    sendResponse(GenericObjectActionMessage(uguid, 27))
    sendResponse(GenericObjectActionMessage(uguid, 28))
  }

  /**
    * na
    * @param telepadGUID na
    */
  def LinkRemoteTelepad(telepadGUID: PlanetSideGUID): Unit = {
    sendResponse(GenericObjectActionMessage(telepadGUID, 27))
    sendResponse(GenericObjectActionMessage(telepadGUID, 28))
  }

  /**
    * A player uses a fully-linked Router teleportation system.
    * @param router the Router vehicle
    * @param internalTelepad the internal telepad within the Router vehicle
    * @param remoteTelepad the remote telepad that is currently associated with this Router
    * @param src the origin of the teleportation (where the player starts)
    * @param dest the destination of the teleportation (where the player is going)
    */
  def UseRouterTelepadSystem(
      router: Vehicle,
      internalTelepad: InternalTelepad,
      remoteTelepad: TelepadDeployable,
      src: PlanetSideGameObject with TelepadLike,
      dest: PlanetSideGameObject with TelepadLike
  ) = {
    val time = System.nanoTime
    if (
      time - recentTeleportAttempt > (2 seconds).toNanos && router.DeploymentState == DriveState.Deployed && internalTelepad.Active && remoteTelepad.Active
    ) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      UseRouterTelepadEffect(pguid, sguid, dguid)
      continent.LocalEvents ! LocalServiceMessage(
        continent.id,
        LocalAction.RouterTelepadTransport(pguid, pguid, sguid, dguid)
      )
    } else {
      log.warn(s"UseRouterTelepadSystem: can not teleport")
    }
    recentTeleportAttempt = time
  }

  /**
    * Animate(?) a player using a fully-linked Router teleportation system.
    * In reality, this seems to do nothing visually?
    * @param playerGUID the player being teleported
    * @param srcGUID the origin of the teleportation
    * @param destGUID the destination of the teleportation
    */
  def UseRouterTelepadEffect(playerGUID: PlanetSideGUID, srcGUID: PlanetSideGUID, destGUID: PlanetSideGUID): Unit = {
    sendResponse(PlanetsideAttributeMessage(playerGUID, 64, 1)) //what does this do?
    sendResponse(GenericObjectActionMessage(srcGUID, 31))
    sendResponse(GenericObjectActionMessage(destGUID, 32))
  }

  /**
    * For a certain weapon that cna load ammunition, enforce that its magazine is empty.
    * @param weapon_guid the weapon
    */
  def EmptyMagazine(weapon_guid: PlanetSideGUID): Unit = {
    continent.GUID(weapon_guid) match {
      case Some(tool: Tool) =>
        EmptyMagazine(weapon_guid, tool)
      case _ => ;
    }
  }

  /**
    * For a certain weapon that can load ammunition, enforce that its magazine is empty.
    * Punctuate that emptiness with a ceasation of weapons fire and a dry fire sound effect.
    * @param weapon_guid the weapon (GUID)
    * @param tool the weapon (object)
    */
  def EmptyMagazine(weapon_guid: PlanetSideGUID, tool: Tool): Unit = {
    tool.Magazine = 0
    sendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, weapon_guid, 0))
    sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Stop(player.GUID, weapon_guid)
    )
    sendResponse(WeaponDryFireMessage(weapon_guid))
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.WeaponDryFire(player.GUID, weapon_guid))
  }

  /**
    * Make this client display the deployment map, and all its available destination spawn points.
    * @see `AvatarDeadStateMessage`
    * @see `DeadState.Release`
    * @see `Player.Release`
    */
  def GoToDeploymentMap(): Unit = {
    deadState = DeadState.Release //we may be alive or dead, may or may not be a corpse
    sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
    DrawCurrentAmsSpawnPoint()
  }

  /**
    * From a seat, find the weapon controlled from it, and update the ammunition counts for that weapon's magazines.
    * @param objWithSeat the object that owns seats (and weaponry)
    * @param seatNum the seat
    */
  def UpdateWeaponAtSeatPosition(objWithSeat: MountedWeapons, seatNum: Int): Unit = {
    objWithSeat.WeaponControlledFromSeat(seatNum) match {
      case Some(weapon: Tool) =>
        //update mounted weapon belonging to seat
        weapon.AmmoSlots.foreach(slot => {
          //update the magazine(s) in the weapon, specifically
          val magazine = slot.Box
          sendResponse(InventoryStateMessage(magazine.GUID, weapon.GUID, magazine.Capacity.toLong))
        })
      case _ => ; //no weapons to update
    }
  }

  /**
    * Given an origin and a destination, determine how long the process of traveling should take in reconstruction time.
    * For most destinations, the unit of receiving ("spawn point") determines the reconstruction time.
    * In a special consideration, travel to any sanctuary or sanctuary-special zone should be as immediate as zone loading.
    *
    * @param toZoneId     the zone where the target is headed
    * @param toSpawnPoint the unit the target is using as a destination
    * @param fromZoneId   the zone where the target current is located
    * @return how long the spawning process will take
    */
  def CountSpawnDelay(toZoneId: String, toSpawnPoint: SpawnPoint, fromZoneId: String): FiniteDuration = {
    val sanctuaryZoneId = Zones.sanctuaryZoneId(player.Faction)
    if (fromZoneId.equals("Nowhere") || sanctuaryZoneId.equals(toZoneId)) { //to sanctuary
      0 seconds
    } else if (!player.isAlive) {
      toSpawnPoint.Definition.Delay seconds //TODO +cumulative death penalty
    } else {
      toSpawnPoint.Definition.Delay seconds
    }
  }

  /**
    * In the background, a list of advanced mobile spawn vehicles that are deployed in the zone is being updated constantly.
    * Select, from this list, the AMS that is closest to the player's current or last position
    * and draw its spawn selection icon onto the deployment map.
    * @see `BindPlayerMessage`
    * @see `DeadState.Release`
    */
  def DrawCurrentAmsSpawnPoint(): Unit = {
    if (deadState == DeadState.Release) {
      amsSpawnPoints
        .sortBy(tube => Vector3.DistanceSquared(tube.Position, player.Position))
        .headOption match {
        case Some(tube) =>
          log.info("DrawCurrentAmsSpawnPoint - new @ams spawn point drawn")
          sendResponse(
            BindPlayerMessage(
              BindStatus.Available,
              "@ams",
              true,
              false,
              SpawnGroup.AMS,
              continent.Number,
              5,
              tube.Position
            )
          )
        case None =>
          log.info("DrawCurrentAmsSpawnPoint - no @ams spawn point drawn")
          sendResponse(
            BindPlayerMessage(
              BindStatus.Unavailable,
              "@ams",
              false,
              false,
              SpawnGroup.AMS,
              continent.Number,
              0,
              Vector3.Zero
            )
          )
      }
    }
  }

  def SwapSquadUIElements(squad: Squad, fromIndex: Int, toIndex: Int): Unit = {
    if (squadUI.nonEmpty) {
      val fromMember = squad.Membership(toIndex)   //the players have already been swapped in the backend object
      val fromCharId = fromMember.CharId
      val toMember   = squad.Membership(fromIndex) //the players have already been swapped in the backend object
      val toCharId   = toMember.CharId
      val id         = 11
      if (toCharId > 0) {
        //toMember and fromMember have swapped places
        val fromElem = squadUI(fromCharId)
        val toElem   = squadUI(toCharId)
        squadUI(toCharId) =
          SquadUIElement(fromElem.name, toIndex, fromElem.zone, fromElem.health, fromElem.armor, fromElem.position)
        squadUI(fromCharId) =
          SquadUIElement(toElem.name, fromIndex, toElem.zone, toElem.health, toElem.armor, toElem.position)
        sendResponse(SquadMemberEvent.Add(id, toCharId, toIndex, fromElem.name, fromElem.zone, unk7 = 0))
        sendResponse(SquadMemberEvent.Add(id, fromCharId, fromIndex, toElem.name, toElem.zone, unk7 = 0))
        sendResponse(
          SquadState(
            PlanetSideGUID(id),
            List(
              SquadStateInfo(fromCharId, toElem.health, toElem.armor, toElem.position, 2, 2, false, 429, None, None),
              SquadStateInfo(toCharId, fromElem.health, fromElem.armor, fromElem.position, 2, 2, false, 429, None, None)
            )
          )
        )
      } else {
        //previous fromMember has moved toMember
        val elem = squadUI(fromCharId)
        squadUI(fromCharId) = SquadUIElement(elem.name, toIndex, elem.zone, elem.health, elem.armor, elem.position)
        sendResponse(SquadMemberEvent.Remove(id, fromCharId, fromIndex))
        sendResponse(SquadMemberEvent.Add(id, fromCharId, toIndex, elem.name, elem.zone, unk7 = 0))
        sendResponse(
          SquadState(
            PlanetSideGUID(id),
            List(SquadStateInfo(fromCharId, elem.health, elem.armor, elem.position, 2, 2, false, 429, None, None))
          )
        )
      }
      val charId = avatar.id
      if (toCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, toIndex))
      } else if (fromCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, fromIndex))
      }
    }
  }

  def NoSquadUpdates(): Unit = {}

  def SquadUpdates(): Unit = {
    squadService ! SquadServiceMessage(
      player,
      continent,
      SquadServiceAction.Update(
        player.CharId,
        player.Health,
        player.MaxHealth,
        player.Armor,
        player.MaxArmor,
        player.Position,
        continent.Number
      )
    )
  }

  def PeriodicUpdatesWhenEnrolledInSquad(): Unit = {
    queuedSquadActions(squadUpdateCounter)()
    squadUpdateCounter = (squadUpdateCounter + 1) % queuedSquadActions.length
  }

  def CapacitorTick(jump_thrust: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      //Discharge
      if (jump_thrust || player.isOverdrived || player.isShielded) {
        if (player.CapacitorState == CapacitorStateType.Discharging) {
          // Previous tick was already discharging, calculate how much energy to drain from time between the two ticks
          val timeDiff    = (System.currentTimeMillis() - player.CapacitorLastUsedMillis).toFloat / 1000
          val drainAmount = player.ExoSuitDef.CapacitorDrainPerSecond.toFloat * timeDiff
          player.Capacitor -= drainAmount
          sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
        } else {
          // Start discharging
          player.CapacitorState = CapacitorStateType.Discharging
        }
      }
      // Charge
      else if (
        player.Capacitor < player.ExoSuitDef.MaxCapacitor
        && (player.CapacitorState == CapacitorStateType.Idle || player.CapacitorState == CapacitorStateType.Charging || (player.CapacitorState == CapacitorStateType.ChargeDelay && System
          .currentTimeMillis() - player.CapacitorLastUsedMillis > player.ExoSuitDef.CapacitorRechargeDelayMillis))
      ) {
        if (player.CapacitorState == CapacitorStateType.Charging) {
          val timeDiff     = (System.currentTimeMillis() - player.CapacitorLastChargedMillis).toFloat / 1000
          val chargeAmount = player.ExoSuitDef.CapacitorRechargePerSecond * timeDiff
          player.Capacitor += chargeAmount
          sendResponse(PlanetsideAttributeMessage(player.GUID, 7, player.Capacitor.toInt))
        } else {
          player.CapacitorState = CapacitorStateType.Charging
        }
      }

      if (player.Faction == PlanetSideEmpire.VS) {
        // Start charge delay for VS when not boosting
        if (!jump_thrust && player.CapacitorState == CapacitorStateType.Discharging) {
          player.CapacitorState = CapacitorStateType.ChargeDelay
        }
      } else {
        // Start charge delay for other factions if capacitor is empty or special ability is off
        if (
          player.CapacitorState == CapacitorStateType.Discharging && (player.Capacitor == 0 || (!player.isOverdrived && !player.isShielded))
        ) {
          player.CapacitorState = CapacitorStateType.ChargeDelay
          ToggleMaxSpecialState(enable = false)
        }
      }
    } else {
      if (player.CapacitorState != CapacitorStateType.Idle) {
        player.CapacitorState = CapacitorStateType.Idle
      }
    }
  }

  def ToggleMaxSpecialState(enable: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      if (enable) {
        player.Faction match {
          case PlanetSideEmpire.TR =>
            if (player.Capacitor == player.ExoSuitDef.MaxCapacitor)
              player.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
          case PlanetSideEmpire.NC =>
            if (player.Capacitor > 0) player.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
          case _ =>
            log.warn(s"Player ${player.Name} tried to use a MAX special ability but their faction doesn't have one")
        }
        if (
          player.UsingSpecial == SpecialExoSuitDefinition.Mode.Overdrive || player.UsingSpecial == SpecialExoSuitDefinition.Mode.Shielded
        ) {
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.PlanetsideAttributeToAll(player.GUID, 8, 1)
          )
        }
      } else {
        player.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.PlanetsideAttributeToAll(player.GUID, 8, 0)
        )
      }
    }
  }

  /**
    * The main purpose of this method is to determine which targets will receive "locked on" warnings from remote projectiles.
    * For a given series of globally unique identifiers, indicating targets,
    * and that may include mounted elements (players),
    * estimate a series of channel names for communication with the vulnerable targets.
    * @param targets the globally unique identifiers of the immediate detected targets
    * @return channels names that allow direct communication to specific realized targets
    */
  def FindDetectedProjectileTargets(targets: Iterable[PlanetSideGUID]): Iterable[String] = {
    targets
      .map { ValidObject }
      .flatMap {
        case Some(obj: Vehicle) if !obj.Cloaked =>
          //TODO hint: vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.ProjectileAutoLockAwareness(mode))
          obj.Seats.values.collect { case seat if seat.isOccupied => seat.Occupant.get.Name }
        case Some(obj: Mountable) =>
          obj.Seats.values.collect { case seat if seat.isOccupied => seat.Occupant.get.Name }
        case Some(obj: Player) if obj.ExoSuit == ExoSuitType.MAX =>
          Seq(obj.Name)
        case _ =>
          Seq.empty[String]
      }
  }

  /**
    * For a given registered remote projectile, perform all the actions necessary to properly dispose of it.
    * Those actions involve:
    * informing that the projectile should explode,
    * unregistering the projectile's globally unique identifier,
    * and managing the projectiles's local status information.
    * @see `CleanUpRemoteProjectile(PlanetSideGUID, Projectile, Int)`
    * @param projectile_guid the globally unique identifier of the projectile
    * @param projectile the projectile
    */
  def CleanUpRemoteProjectile(projectile_guid: PlanetSideGUID, projectile: Projectile): Unit = {
    projectiles.indexWhere({
      case Some(p) => p eq projectile
      case None    => false
    }) match {
      case -1 => ; //required catch
      case index if projectilesToCleanUp(index) =>
        CleanUpRemoteProjectile(projectile_guid, projectile, index)
      case _ => ;
    }
  }

  /**
    * For a given registered remote projectile, perform all the actions necessary to properly dispose of it.
    * Those actions involve:
    * informing that the projectile should explode,
    * unregistering the projectile's globally unique identifier,
    * and managing the projectiles's local status information.
    * @param projectile_guid the globally unique identifier of the projectile
    * @param projectile the projectile
    * @param local_index an index of the absolute sequence of the projectile, for internal lists
    */
  def CleanUpRemoteProjectile(projectile_guid: PlanetSideGUID, projectile: Projectile, local_index: Int): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ProjectileExplodes(player.GUID, projectile_guid, projectile)
    )
    continent.tasks ! UnregisterProjectile(projectile)
    projectiles(local_index) match {
      case Some(obj) if !obj.isResolved => obj.Miss()
      case _                            => ;
    }
    projectilesToCleanUp(local_index) = false
  }

  def CheckForHitPositionDiscrepancy(
      projectile_guid: PlanetSideGUID,
      hitPos: Vector3,
      target: PlanetSideGameObject with FactionAffinity with Vitality
  ): Unit = {
    val hitPositionDiscrepancy = Vector3.DistanceSquared(hitPos, target.Position)
    if (hitPositionDiscrepancy > Config.app.antiCheat.hitPositionDiscrepancyThreshold) {
      // If the target position on the server does not match the position where the projectile landed within reason there may be foul play
      log.warn(
        s"Shot guid ${projectile_guid} has hit location discrepancy with target location. Target: ${target.Position} Reported: ${hitPos}, Distance: ${hitPositionDiscrepancy} / ${math.sqrt(hitPositionDiscrepancy).toFloat}; suspect"
      )
    }
  }

  /**
    * na
    * @param tplayer na
    * @param zone na
    */
  def HandleReleaseAvatar(tplayer: Player, zone: Zone): Unit = {
    tplayer.Release
    tplayer.VehicleSeated match {
      case None =>
        PrepareToTurnPlayerIntoCorpse(tplayer, zone)
      case Some(_) =>
        tplayer.VehicleSeated = None
        zone.Population ! Zone.Population.Release(avatar)
        sendResponse(ObjectDeleteMessage(tplayer.GUID, 0))
        zone.tasks ! GUIDTask.UnregisterPlayer(tplayer)(zone.GUID)
    }
  }

  /**
    * The upstream counter accumulates when the server receives specific messages from the client.
    * It counts upwards until it reach maximum value, and then starts over.
    * When it starts over, which should take an exceptionally long time to achieve,
    * it starts counting at one rather than zero.
    * @param p the player's globally unique identifier number
    */
  def NormalTurnCounter(p: PlanetSideGUID): Unit = {
    upstreamMessageCount = 1 + upstreamMessageCount % Int.MaxValue
  }

  /**
    * During the interim period between the avatar being in one place/zone
    * and completing the process of transitioning to another place/zone,
    * the upstream message counter is zero'd
    * awaiting new activity from the client.
    * Until new upstream messages that pass some tests against their data start being reported,
    * the counter does not accumulate properly.
    * @param guid the player's globally unique identifier number
    */
  def TurnCounterDuringInterim(guid: PlanetSideGUID): Unit = {
    upstreamMessageCount = 0
    if (player.GUID == guid && player.Zone == continent) {
      turnCounterFunc = NormalTurnCounter
    }
  }

  /**
    * During the interim period between the avatar being in one place/zone
    * and completing the process of transitioning to another place/zone,
    * the upstream message counter is zero'd
    * awaiting new activity from the client.
    * Until new upstream messages that pass some tests against their data start being reported,
    * the counter does not accumulate properly.<br>
    * <br>
    * In the case that the transitioning player is seated in a vehicle seat
    * that is not the driver and does not have a mounted weapon under its control,
    * no obvious feedback will be provided by the client.
    * For example, when as infantry, a `PlayerStateMessageUpstream` packet is dispatched by the client.
    * For example, when in the driver seat, a `VehicleStateMessage` is dispatched by the client.
    * In the given case, the only packet that indicates the player is seated is a `KeepAliveMessage`.
    * Detection of this `KeepALiveMessage`, for the purpose of transitioning logic,
    * can not be instantaneous to the zoning process or other checks for proper zoning conditions that will be disrupted.
    * To avoid complications, the player in such a seat is initially spawned as infantry on their own client,
    * realizes the state transition confirmation for infantry (turn counter),
    * and is forced to transition into being seated,
    * and only at that time will begin registering `KeepAliveMessage` to mark the end of their interim period.
    * @param guid the player's globally unique identifier number
    */
  def TurnCounterDuringInterimWhileInPassengerSeat(guid: PlanetSideGUID): Unit = {
    upstreamMessageCount = 0
    val pguid = player.GUID
    if (pguid == guid && player.Zone == continent) {
      (continent.GUID(interimUngunnedVehicle), interimUngunnedVehicle, interimUngunnedVehicleSeat) match {
        case (Some(vehicle: Vehicle), Some(vguid), Some(seat)) =>
          //sit down
          sendResponse(ObjectAttachMessage(vguid, pguid, seat))
          AccessContainer(vehicle)
          keepAliveFunc = KeepAlivePersistence
        case _ => ;
          //we can't find a vehicle? and we're still here? that's bad
          player.VehicleSeated = None
      }
      interimUngunnedVehicle = None
      interimUngunnedVehicleSeat = None
      turnCounterFunc = NormalTurnCounter
    }
  }

  /**
    * The normal response to receiving a `KeepAliveMessage` packet from the client.<br>
    * <br>
    * Even though receiving a `KeepAliveMessage` outside of zoning is uncommon,
    * the behavior should be configured to maintain a neutral action.
    *
    * @see `KeepAliveMessage`
    * @see `keepAliveFunc`
    */
  def NormalKeepAlive(): Unit = {}

  /**
    * The atypical response to receiving a `KeepAliveMessage` packet from the client.<br>
    * <br>
    * `KeepAliveMessage` packets are the primary vehicle for persistence due to client reporting
    * in the case where the player's avatar is riding in a vehicle in a seat with no vehicle.
    * @see `KeepAliveMessage`
    * @see `keepAliveFunc`
    * @see `turnCounterFunc`
    * @see `persist`
    */
  def KeepAlivePersistence(): Unit = {
    //log.info(s"KeepAlive in a vehicle - $upstreamMessageCount")
    interimUngunnedVehicle = None
    persist()
    turnCounterFunc(player.GUID)
  }

  def AdministrativeKick(tplayer: Player) = {
    tplayer.death_by = -1
    accountPersistence ! AccountPersistenceService.Kick(tplayer.Name)
    //get out of that vehicle
    GetMountableAndSeat(None, tplayer, continent) match {
      case (Some(obj), Some(seatNum)) =>
        tplayer.VehicleSeated = None
        obj.Seats(seatNum).Occupant = None
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seatNum, false, obj.GUID)
        )
      case _ => ;
    }
  }

  def KickedByAdministration(): Unit = {
    sendResponse(DisconnectMessage("@kick_w"))
    Thread.sleep(300)
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def ImmediateDisconnect(): Unit = {
    if (avatar != null) {
      accountPersistence ! AccountPersistenceService.Logout(avatar.name)
    }
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def HandleWeaponFire(weaponGUID: PlanetSideGUID, projectileGUID: PlanetSideGUID, shotOrigin: Vector3): Unit = {
    HandleWeaponFireAccountability(weaponGUID, projectileGUID) match {
      case (Some(obj), Some(tool)) =>
        val projectileIndex = projectileGUID.guid - Projectile.baseUID
        val projectilePlace = projectiles(projectileIndex)
        if (
          projectilePlace match {
            case Some(projectile) => !projectile.isResolved
            case None             => false
          }
        ) {
          log.trace(
            s"WeaponFireMessage: overwriting unresolved projectile ${projectileGUID.guid}"
          )
        }
        val (angle, attribution, acceptableDistanceToOwner) = obj match {
          case p: Player =>
            (
              SimpleWorldEntity.validateOrientationEntry(
                p.Orientation + Vector3.z(p.FacingYawUpper)
              ),
              tool.Definition.ObjectId,
              10f + (if (p.Velocity.nonEmpty) {
                       5f
                     } else {
                       0f
                     })
            )
          case v: Vehicle if v.Definition.CanFly =>
            (tool.Orientation, obj.Definition.ObjectId, 1000f) //TODO this is too simplistic to find proper angle
          case _: Vehicle =>
            (tool.Orientation, obj.Definition.ObjectId, 225f) //TODO this is too simplistic to find proper angle
          case _ =>
            (obj.Orientation, obj.Definition.ObjectId, 300f)
        }
        val distanceToOwner = Vector3.DistanceSquared(shotOrigin, player.Position)
        if (distanceToOwner <= acceptableDistanceToOwner) {
          val projectile_info = tool.Projectile
          val projectile =
            Projectile(
              projectile_info,
              tool.Definition,
              tool.FireMode,
              player,
              attribution,
              shotOrigin,
              angle
            )
          val initialQuality = tool.FireMode match {
            case mode: ChargeFireModeDefinition =>
              ProjectileQuality.Modified((projectile.fire_time - shootingStart) / mode.Time.toFloat)
            case _ =>
              ProjectileQuality.Normal
          }
          projectiles(projectileIndex) = Some(projectile.quality(initialQuality))
          if (projectile_info.ExistsOnRemoteClients) {
            log.trace(
              s"WeaponFireMessage: ${projectile_info.Name} is a remote projectile"
            )
            continent.tasks ! (if (projectile.HasGUID) {
                              continent.AvatarEvents ! AvatarServiceMessage(
                                continent.id,
                                AvatarAction.ProjectileExplodes(
                                  player.GUID,
                                  projectile.GUID,
                                  projectile
                                )
                              )
                              ReregisterProjectile(projectile)
                            } else {
                              RegisterProjectile(projectile)
                            })
          }
          projectilesToCleanUp(projectileIndex) = false

          obj match {
            case turret: FacilityTurret if turret.Definition == GlobalDefinitions.vanu_sentry_turret =>
              turret.Actor ! FacilityTurret.WeaponDischarged()
            case _ => ;
          }
        } else {
          log.warn(
            s"WeaponFireMessage: ${player.Name}'s ${tool.Definition.Name} projectile is too far from owner position at time of discharge ($distanceToOwner > $acceptableDistanceToOwner); suspect"
          )
        }

      case _ => ;
    }
  }

  def HandleWeaponFireAccountability(
      weaponGUID: PlanetSideGUID,
      projectileGUID: PlanetSideGUID
  ): (Option[PlanetSideGameObject with Container], Option[Tool]) = {
    CancelZoningProcessWithDescriptiveReason("cancel_fire")
    if (player.isShielded) {
      // Cancel NC MAX shield if it's active
      ToggleMaxSpecialState(enable = false)
    }
    FindContainedWeapon match {
      case out @ (Some(_), Some(tool: Tool)) =>
        if (tool.Magazine <= 0) { //safety: enforce ammunition depletion
          prefire = None
          EmptyMagazine(weaponGUID, tool)
        } else if (!player.isAlive) { //proper internal accounting, but no projectile
          prefire = shooting.orElse(Some(weaponGUID))
          tool.Discharge()
          projectiles(projectileGUID.guid - Projectile.baseUID) = None
          shotsWhileDead += 1
        } else { //shooting
          if (
            avatar.stamina > 0 &&
            tool.FireModeIndex == 1 &&
            (tool.Definition.Name == "anniversary_guna"
            || tool.Definition.Name == "anniversary_gun"
            || tool.Definition.Name == "anniversary_gunb")
          ) {
            avatarActor ! AvatarActor.ConsumeStamina(avatar.stamina)
          }
          avatarActor ! AvatarActor.SuspendStaminaRegeneration(3.seconds)
          prefire = shooting.orElse(Some(weaponGUID))
          tool.Discharge()
        }
        out
      case _ =>
        (None, None)
    }
  }

  def failWithError(error: String) = {
    log.error(error)
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def sendResponse(packet: PlanetSidePacket): Unit = {
    middlewareActor ! MiddlewareActor.Send(packet)
  }
}
