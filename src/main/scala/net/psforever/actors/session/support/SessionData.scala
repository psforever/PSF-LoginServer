// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, Cancellable, OneForOneStrategy, SupervisorStrategy, typed}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
//
import net.psforever.actors.net.MiddlewareActor
import net.psforever.actors.session.{AvatarActor, ChatActor, SessionActor}
import net.psforever.actors.zone.ZoneActor
import net.psforever.login.WorldSession._
import net.psforever.objects._
import net.psforever.objects.avatar._
import net.psforever.objects.ballistics._
import net.psforever.objects.ce._
import net.psforever.objects.definition._
import net.psforever.objects.entity.{NoGUIDException, WorldEntity}
import net.psforever.objects.equipment._
import net.psforever.objects.guid._
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.loadouts.InfantryLoadout
import net.psforever.objects.locker.LockerContainer
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building, WarpGate}
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObject}
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vehicles._
import net.psforever.objects.vital._
import net.psforever.objects.vital.collision.{CollisionReason, CollisionWithReason}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones._
import net.psforever.objects.zones.blockmap.{BlockMap, BlockMapEntity}
import net.psforever.packet._
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, BugReportMessage, ChangeShortcutBankMessage, CharacterRequestMessage, CreateShortcutMessage, DeployObjectMessage, DisplayedAwardMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FavoritesRequest, FriendsRequest, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, SetChatFilterMessage, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UseItemMessage, ZipLineMessage}
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game._
import net.psforever.services.account.{AccountPersistenceService, RetrieveAccountData}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.ServiceManager.LookupResult
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{RemoverActor, Service, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.Config

object SessionData {
  private def NoTurnCounterYet(guid: PlanetSideGUID): Unit = { }
}

class SessionData(
                   middlewareActor: typed.ActorRef[MiddlewareActor.Command],
                   implicit val context: ActorContext
                 ) {
  /**
   * Hardwire an implicit `sender` to be the same as `context.self` of the `SessionActor` actor class
   * for which this support class was initialized.
   * Allows for proper use for `ActorRef.tell` or an actor's `!` in the support class,
   * one where the result is always directed back to the same `SessionActor` instance.
   * If there is a different packet "sender" that has to be respected by a given method,
   * pass that `ActorRef` into the method as a parameter.
   * @see `ActorRef.!(Any)(ActorRef)`
   * @see `ActorRef.tell(Any)(ActorRef)`
   */
  private[this] implicit val sender: ActorRef = context.self

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = context.spawnAnonymous(AvatarActor(context.self))
  private val chatActor: typed.ActorRef[ChatActor.Command] = context.spawnAnonymous(ChatActor(context.self, avatarActor))

  private[support] val log = org.log4s.getLogger
  private[support] var _session: Session = Session()
  private[support] var accountIntermediary: ActorRef = Default.Actor
  private[support] var accountPersistence: ActorRef = Default.Actor
  private[support] var galaxyService: ActorRef = Default.Actor
  private[support] var squadService: ActorRef = Default.Actor
  private[support] var cluster: typed.ActorRef[ICS.Command] = Default.typed.Actor
  private[support] var progressBarValue: Option[Float] = None
  private[support] var accessedContainer: Option[PlanetSideGameObject with Container] = None
  private[session] var connectionState: Int = 25
  private var recentTeleportAttempt: Long = 0
  private[support] var kitToBeUsed: Option[PlanetSideGUID] = None
  private[support] var persistFunc: () => Unit = NoPersistence
  private[support] var persist: () => Unit = UpdatePersistenceOnly
  private[support] var specialItemSlotGuid: Option[PlanetSideGUID] =
    None // If a special item (e.g. LLU) has been attached to the player the GUID should be stored here, or cleared when dropped, since the drop hotkey doesn't send the GUID of the object to be dropped.
  private[support] var serverTime: Long = 0 //unused?
  private[session] var keepAliveFunc: () => Unit = KeepAlivePersistenceInitial
  private[support] var turnCounterFunc: PlanetSideGUID => Unit = SessionData.NoTurnCounterYet
  private var heightLast: Float = 0f
  private var heightTrend: Boolean = false //up = true, down = false
  private var heightHistory: Float = 0f
  private var contextSafeEntity: PlanetSideGUID = PlanetSideGUID(0)
  private val collisionHistory: mutable.HashMap[ActorRef, Long] = mutable.HashMap()

  private var clientKeepAlive: Cancellable = Default.Cancellable
  private[support] var progressBarUpdate: Cancellable = Default.Cancellable
  private var charSavedTimer: Cancellable = Default.Cancellable

  val shooting: WeaponAndProjectileOperations =
    new WeaponAndProjectileOperations(sessionData=this, avatarActor, chatActor, context)
  val vehicles: VehicleOperations =
    new VehicleOperations(sessionData=this, avatarActor, context)
  val avatarResponse: SessionAvatarHandlers =
    new SessionAvatarHandlers(sessionData=this, avatarActor, chatActor, context)
  val localResponse: SessionLocalHandlers =
    new SessionLocalHandlers(sessionData=this, context)
  val mountResponse: SessionMountHandlers =
    new SessionMountHandlers(sessionData=this, avatarActor, context)
  val terminals: SessionTerminalHandlers =
    new SessionTerminalHandlers(sessionData=this, avatarActor, context)
  private var _vehicleResponse: Option[SessionVehicleHandlers] = None
  private var _galaxyResponse: Option[SessionGalaxyHandlers] = None
  private var _squadResponse: Option[SessionSquadHandlers] = None
  private var _zoning: Option[ZoningOperations] = None
  def vehicleResponseOperations: SessionVehicleHandlers = _vehicleResponse.orNull
  def galaxyResponseHanders: SessionGalaxyHandlers = _galaxyResponse.orNull
  def squad: SessionSquadHandlers = _squadResponse.orNull
  def zoning: ZoningOperations = _zoning.orNull

  val sessionSupervisorStrategy: SupervisorStrategy = {
    import net.psforever.objects.inventory.InventoryDisarrayException
    OneForOneStrategy(maxNrOfRetries = -1, withinTimeRange = 1 minute) {
      case nge: NoGUIDException =>
        nge.getEntity match {
          case p: Player =>
            continent.GUID(p.VehicleSeated) match {
              case Some(v: Vehicle) =>
                attemptRecoveryFromNoGuidExceptionAsVehicle(v, nge)
              case _ =>
                attemptRecoveryFromNoGuidExceptionAsPlayer(p, nge)
            }

          case v: Vehicle =>
            attemptRecoveryFromNoGuidExceptionAsVehicle(v, nge)

          case e: Equipment =>
            (
              player.Holsters().zipWithIndex.flatMap { case (o, i) =>
                o.Equipment match {
                  case Some(e) => Some((player, InventoryItem(e, i)))
                  case None => None
                }
              }.toList ++
                player.Inventory.Items.map { o => (player, o) } ++ {
                player.FreeHand.Equipment match {
                  case Some(_) => List((player, InventoryItem(e, Player.FreeHandSlot)))
                  case _ => Nil
                }
              } ++
                (ValidObject(player.VehicleSeated) match {
                  case Some(v: Vehicle) => v.Trunk.Items.map { o => (v, o) }
                  case _ => Nil
                })
              )
              .find { case (_, InventoryItem(o, _)) => o eq e } match {
              case Some((c: Container, InventoryItem(obj, index))) =>
                if (!obj.HasGUID) {
                  c.Slot(index).Equipment = None
                }
                c match {
                  case _: Player =>
                    attemptRecoveryFromNoGuidExceptionAsPlayer(player, nge)
                  case v: Vehicle =>
                    if (v.PassengerInSeat(player).contains(0)) {
                      attemptRecoveryFromNoGuidExceptionAsPlayer(player, nge)
                    }
                    SupervisorStrategy.resume
                  case _ =>
                    writeLogExceptionAndStop(nge)
                }
              case _ =>
                //did not discover or resolve the situation
                writeLogExceptionAndStop(nge)
            }

          case _ =>
            SupervisorStrategy.resume
        }

      case ide: InventoryDisarrayException =>
        attemptRecoveryFromInventoryDisarrayException(ide.inventory)
        //re-evaluate results
        if (ide.inventory.ElementsOnGridMatchList() > 0) {
          writeLogExceptionAndStop(ide)
        } else {
          SupervisorStrategy.resume
        }

      case e =>
        writeLogExceptionAndStop(e)
    }
  }

  def attemptRecoveryFromNoGuidExceptionAsVehicle(v: Vehicle, e: Throwable): SupervisorStrategy.Directive = {
    val entry = v.Seats.find { case (_, s) => s.occupants.contains(player) }
    entry match {
      case Some(_) =>
        player.VehicleSeated = None
        v.Seats(0).unmount(player)
        player.Position = v.Position
        player.Orientation = v.Orientation
        zoning.interstellarFerry = None
        zoning.interstellarFerryTopLevelGUID = None
        attemptRecoveryFromNoGuidExceptionAsPlayer(player, e)
      case None =>
        writeLogException(e)
    }
  }

  def attemptRecoveryFromNoGuidExceptionAsPlayer(p: Player, e: Throwable): SupervisorStrategy.Directive = {
    if (p eq player) {
      val hasGUID = p.HasGUID
      zoning.zoneLoaded match {
        case Some(true) if hasGUID =>
          zoning.spawn.AvatarCreate() //this will probably work?
          SupervisorStrategy.resume
        case Some(false) =>
          zoning.RequestSanctuaryZoneSpawn(p, continent.Number)
          SupervisorStrategy.resume
        case None =>
          if (player.Zone eq Zone.Nowhere) {
            zoning.RequestSanctuaryZoneSpawn(p, continent.Number)
          } else {
            zoning.zoneReload = true
            zoning.LoadZoneAsPlayer(player, player.Zone.id)
          }
          SupervisorStrategy.resume
        case _ =>
          writeLogExceptionAndStop(e)
      }
    } else {
      SupervisorStrategy.resume
    }
  }

  def attemptRecoveryFromInventoryDisarrayException(inv: GridInventory): Unit = {
    inv.ElementsInListCollideInGrid() match {
      case Nil => ;
      case overlaps =>
        val previousItems = inv.Clear()
        val allOverlaps = overlaps.flatten.sortBy { entry =>
          val tile = entry.obj.Definition.Tile
          tile.Width * tile.Height
        }.toSet
        val notCollidingRemainder = previousItems.filterNot(allOverlaps.contains)
        notCollidingRemainder.foreach { entry =>
          inv.InsertQuickly(entry.start, entry.obj)
        }
        var didNotFit: List[Equipment] = Nil
        allOverlaps.foreach { entry =>
          inv.Fit(entry.obj.Definition.Tile) match {
            case Some(newStart) =>
              inv.InsertQuickly(newStart, entry.obj)
            case None =>
              didNotFit = didNotFit :+ entry.obj
          }
        }
        //completely clear the inventory
        val pguid = player.GUID
        val equipmentInHand = player.Slot(player.DrawnSlot).Equipment
        //redraw suit
        sendResponse(ArmorChangedMessage(
          pguid,
          player.ExoSuit,
          InfantryLoadout.DetermineSubtypeA(player.ExoSuit, equipmentInHand)
        ))
        //redraw item in free hand (if)
        player.FreeHand.Equipment match {
          case Some(item) =>
            sendResponse(ObjectCreateDetailedMessage(
              item.Definition.ObjectId,
              item.GUID,
              ObjectCreateMessageParent(pguid, Player.FreeHandSlot),
              item.Definition.Packet.DetailedConstructorData(item).get
            ))
          case _ => ;
        }
        //redraw items in holsters
        player.Holsters().zipWithIndex.foreach { case (slot, _) =>
          slot.Equipment match {
            case Some(item) =>
              sendResponse(ObjectCreateDetailedMessage(
                item.Definition.ObjectId,
                item.GUID,
                item.Definition.Packet.DetailedConstructorData(item).get
              ))
            case _ => ;
          }
        }
        //redraw raised hand (if)
        equipmentInHand match {
          case Some(_) =>
            sendResponse(ObjectHeldMessage(pguid, player.DrawnSlot, unk1 = true))
          case _ => ;
        }
        //redraw inventory items
        val recoveredItems = inv.Items
        recoveredItems.foreach { entry =>
          val item = entry.obj
          sendResponse(ObjectCreateDetailedMessage(
            item.Definition.ObjectId,
            item.GUID,
            ObjectCreateMessageParent(pguid, entry.start),
            item.Definition.Packet.DetailedConstructorData(item).get
          ))
        }
        //drop items that did not fit
        val placementData = PlacementData(player.Position, Vector3.z(player.Orientation.z))
        didNotFit.foreach { item =>
          sendResponse(ObjectCreateMessage(
            item.Definition.ObjectId,
            item.GUID,
            DroppedItemData(
              placementData,
              item.Definition.Packet.ConstructorData(item).get
            )
          ))
        }
    }
  }

  def writeLogException(e: Throwable): SupervisorStrategy.Directive = {
    import java.io.{PrintWriter, StringWriter}
    val sw = new StringWriter
    e.printStackTrace(new PrintWriter(sw))
    log.error(sw.toString)
    SupervisorStrategy.Resume
  }

  def writeLogExceptionAndStop(e: Throwable): SupervisorStrategy.Directive = {
    writeLogException(e)
    ImmediateDisconnect()
    SupervisorStrategy.stop
  }

  def session: Session = _session

  def session_=(session: Session): Unit = {
    chatActor ! ChatActor.SetSession(session)
    avatarActor ! AvatarActor.SetSession(session)
    _session = session
  }

  def account: Account = _session.account

  def continent: Zone = _session.zone

  def player: Player = _session.player

  def avatar: Avatar = _session.avatar

  /* packets */

  def handleConnectToWorldRequest(pkt: PlanetSideGamePacket)(implicit context: ActorContext): Unit = {
    pkt match {
      case ConnectToWorldRequestMessage(_, token, majorVersion, minorVersion, revision, buildDate, _) =>
        log.trace(
          s"ConnectToWorldRequestMessage: client with versioning $majorVersion.$minorVersion.$revision, $buildDate has sent a token to the server"
        )
        sendResponse(ChatMsg(ChatMessageType.CMT_CULLWATERMARK, wideContents=false, "", "", None))
        import scala.concurrent.ExecutionContext.Implicits.global
        clientKeepAlive.cancel()
        clientKeepAlive = context.system.scheduler.scheduleWithFixedDelay(
          initialDelay = 0.seconds,
          delay = 500.milliseconds,
          context.self,
          SessionActor.PokeClient()
        )
        accountIntermediary ! RetrieveAccountData(token)
      case _ => ;
    }
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
    avatar_guid,
    pos,
    vel,
    yaw,
    pitch,
    yaw_upper,
    seq_time,
    _,
    is_crouching,
    is_jumping,
    jump_thrust,
    is_cloaking,
    _,
    _
    )= pkt
    persist()
    turnCounterFunc(avatar_guid)
    updateBlockMap(player, continent, pos)
    val isMoving     = WorldEntity.isMoving(vel)
    val isMovingPlus = isMoving || is_jumping || jump_thrust
    if (isMovingPlus) {
      zoning.CancelZoningProcessWithDescriptiveReason("cancel_motion")
    }
    fallHeightTracker(pos.z)
    //        if (is_crouching && !player.Crouching) {
    //          //dev stuff goes here
    //        }
    player.Position = pos
    player.Velocity = vel
    player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
    player.FacingYawUpper = yaw_upper
    player.Crouching = is_crouching
    player.Jumping = is_jumping
    if (is_cloaking && !player.Cloaked) {
      zoning.CancelZoningProcessWithDescriptiveReason("cancel_cloak")
    }
    player.Cloaked = player.ExoSuit == ExoSuitType.Infiltration && is_cloaking
    CapacitorTick(jump_thrust)
    if (isMovingPlus && terminals.usingMedicalTerminal.isDefined) {
      continent.GUID(terminals.usingMedicalTerminal) match {
        case Some(term: Terminal with ProximityUnit) =>
          terminals.StopUsingProximityUnit(term)
        case _ => ;
      }
    }
    accessedContainer match {
      // Ensure we don't unload the contents of the vehicle trunk for players seated in the vehicle.
      // This can happen if PSUM arrives during the mounting process
      case Some(veh: Vehicle) if player.VehicleSeated.isEmpty || player.VehicleSeated.get != veh.GUID =>
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
        if (isMovingPlus && (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID)) { // Ensure we don't close the container if the player is seated in it
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
    squad.updateSquad()
    if (player.death_by == -1) {
      KickedByAdministration()
    }
    player.zoneInteractions()
  }

  def handleChat(pkt: ChatMsg): Unit = {
    chatActor ! ChatActor.Message(pkt)
  }

  def handleChatFilter(pkt: SetChatFilterMessage): Unit = {
    val SetChatFilterMessage(_, _, _) = pkt
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
    val EmoteMsg(avatar_guid, emote) = pkt
    sendResponse(EmoteMsg(avatar_guid, emote))
  }

  def handleDropItem(pkt: DropItemMessage): Unit = {
    val DropItemMessage(item_guid) = pkt
    ValidObject(item_guid, decorator = "DropItem") match {
      case Some(anItem: Equipment) =>
        player.FreeHand.Equipment match {
          case Some(item) =>
            if (item.GUID == item_guid) {
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
              continent.GUID(player.VehicleSeated) match {
                case Some(_) =>
                  RemoveOldEquipmentFromInventory(player)(item)
                case None =>
                  DropEquipmentFromInventory(player)(item)
              }
            }
          case None =>
            continent.GUID(player.VehicleSeated) match {
              case Some(_) => ; //in a vehicle, suppress the warning message
              case None =>
                log.warn(s"DropItem: ${player.Name} wanted to drop a $anItem, but it wasn't at hand")
            }
        }
      case Some(obj) =>
        log.warn(s"DropItem: ${player.Name} wanted to drop a $obj, but that isn't possible")
      case None => ;
    }
  }

  def handlePickupItem(pkt: PickupItemMessage): Unit = {
    val PickupItemMessage(item_guid, _, _, _) = pkt
    ValidObject(item_guid, decorator = "PickupItem") match {
      case Some(item: Equipment) =>
        player.Fit(item) match {
          case Some(_) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            PickUpEquipmentFromGround(player)(item)
          case None => //skip
            sendResponse(ActionResultMessage.Fail(16)) //error code?
        }
      case _ => ;
    }
  }

  def handleObjectHeld(pkt: ObjectHeldMessage): Unit = {
    val ObjectHeldMessage(_, held_holsters, _) = pkt
    player.Actor ! PlayerControl.ObjectHeld(held_holsters)
  }

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit = {
    val AvatarJumpMessage(_) = pkt
    avatarActor ! AvatarActor.ConsumeStamina(10)
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(2.5 seconds)
  }

  def handleZipLine(pkt: ZipLineMessage): Unit = {
    val ZipLineMessage(player_guid, forwards, action, path_id, pos) = pkt
    val (isTeleporter: Boolean, path: Option[ZipLinePath]) = continent.zipLinePaths.find(x => x.PathId == path_id) match {
      case Some(x) =>
        (x.IsTeleporter, Some(x))
      case _ =>
        log.warn(s"${player.Name} couldn't find a zipline path $path_id in zone ${continent.id}")
        (false, None)
    }
    if (isTeleporter) {
      zoning.CancelZoningProcessWithDescriptiveReason("cancel")
      val endPoint = path.get.ZipLinePoints.last
      sendResponse(
        ZipLineMessage(PlanetSideGUID(0), forwards, 0, path_id, pos)
      ) // todo: send to zone to show teleport animation to all clients
      sendResponse(PlayerStateShiftMessage(ShiftState(0, endPoint, player.Orientation.z, None)))
    } else {
      zoning.CancelZoningProcessWithDescriptiveReason("cancel_motion")
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
            s"${player.Name} tried to do something with a zipline but can't handle it. forwards: $forwards action: $action path_id: $path_id zone: ${continent.Number} / ${continent.id}"
          )
      }
    }
  }

  def handleRequestDestroy(pkt: RequestDestroyMessage): Unit = {
    val RequestDestroyMessage(object_guid) = pkt
    //make sure this is the correct response for all cases
    ValidObject(object_guid, decorator = "RequestDestroy") match {
      case Some(vehicle: Vehicle) =>
        /* line 1a: player is admin (and overrules other access requirements) */
        /* line 1b: vehicle and player (as the owner) acknowledge each other */
        /* line 1c: vehicle is the same faction as player, is ownable, and either the owner is absent or the vehicle is destroyed */
        /* line 2: vehicle is not mounted in anything or, if it is, its seats are empty */
        if (
          (session.account.gm ||
            (player.avatar.vehicle.contains(object_guid) && vehicle.Owner.contains(player.GUID)) ||
            (player.Faction == vehicle.Faction &&
              (vehicle.Definition.CanBeOwned.nonEmpty &&
                (vehicle.Owner.isEmpty || continent.GUID(vehicle.Owner.get).isEmpty) || vehicle.Destroyed))) &&
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
        continent.Projectile ! ZoneProjectile.Remove(object_guid)

      case Some(obj: BoomerTrigger) =>
        if (FindEquipmentToDelete(object_guid, obj)) {
          continent.GUID(obj.Companion) match {
            case Some(boomer: BoomerDeployable) =>
              boomer.Trigger = None
              boomer.Actor ! Deployable.Deconstruct()
            case Some(thing) =>
              log.warn(s"RequestDestroy: BoomerTrigger object connected to wrong object - $thing")
            case None => ;
          }
        }

      case Some(obj: Deployable) =>
        if (session.account.gm || obj.Owner.isEmpty || obj.Owner.contains(player.GUID) || obj.Destroyed) {
          obj.Actor ! Deployable.Deconstruct()
        } else {
          log.warn(s"RequestDestroy: ${player.Name} must own the deployable in order to deconstruct it")
        }

      case Some(obj: Equipment) =>
        FindEquipmentToDelete(object_guid, obj)

      case Some(thing) =>
        log.warn(s"RequestDestroy: not allowed to delete this ${thing.Definition.Name}")

      case None => ;
    }
  }

  def handleMoveItem(pkt: MoveItemMessage): Unit = {
    val MoveItemMessage(item_guid, source_guid, destination_guid, dest, _) = pkt
    (
      continent.GUID(source_guid),
      continent.GUID(destination_guid),
      ValidObject(item_guid, decorator = "MoveItem")
    ) match {
      case (
        Some(source: PlanetSideServerObject with Container),
        Some(destination: PlanetSideServerObject with Container),
        Some(item: Equipment)
        ) =>
        ContainableMoveItem(player.Name, source, destination, item, destination.SlotMapResolution(dest))
      case (None, _, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $item_guid from $source_guid, but could not find source object"
        )
      case (_, None, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $item_guid to $destination_guid, but could not find destination object"
        )
      case (_, _, None) => ;
      case _ =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $item_guid from $source_guid to $destination_guid, but multiple problems were encountered"
        )
    }
  }

  def handleLootItem(pkt: LootItemMessage): Unit = {
    val LootItemMessage(item_guid, target_guid) = pkt
    (ValidObject(item_guid, decorator = "LootItem"), continent.GUID(target_guid)) match {
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
            log.error(s"LootItem: ${player.Name} can not find where $item is put currently")
          case (_, None) =>
            log.error(s"LootItem: ${player.Name} can not find anywhere to put $item in $destination")
          case _ =>
            log.error(
              s"LootItem: ${player.Name}wanted to move $item_guid to $target_guid, but multiple problems were encountered"
            )
        }
      case (Some(obj), _) =>
        log.error(s"LootItem: item $obj is (probably) not lootable to ${player.Name}")
      case (None, _) => ;
      case (_, None) =>
        log.error(s"LootItem: ${player.Name} can not find where to put $item_guid")
    }
  }

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit = {
    val AvatarImplantMessage(_, action, slot, status) = pkt
    if (action == ImplantAction.Activation) {
      zoning.CancelZoningProcessWithDescriptiveReason("cancel_implant")
      avatar.implants(slot) match {
        case Some(implant) =>
          if (status == 1) {
            avatarActor ! AvatarActor.ActivateImplant(implant.definition.implantType)
          } else {
            avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
          }
        case _ => log.error(s"AvatarImplantMessage: ${player.Name} has an unknown implant in $slot")
      }
    }
  }

  def handleUseItem(pkt: UseItemMessage): Unit = {
    val UseItemMessage(
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
    ) = pkt
    // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
    val equipment = FindContainedEquipment(item_used_guid) match {
      case (o @ Some(_), a)
        if a.exists(_.isInstanceOf[Tool]) =>
        shooting.FindEnabledWeaponsToHandleWeaponFireAccountability(o, a.collect { case w: Tool => w })._2.headOption
      case (Some(_), a) =>
        a.headOption
      case _ =>
        None
    }
    ValidObject(object_guid, decorator = "UseItem") match {
      case Some(door: Door) =>
        door.Actor ! CommonMessages.Use(player)

      case Some(resourceSilo: ResourceSilo) =>
        zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        (continent.GUID(player.VehicleSeated), equipment) match {
          case (Some(vehicle: Vehicle), Some(item))
            if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
              GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
            resourceSilo.Actor ! CommonMessages.Use(player, equipment)
          case _ =>
            resourceSilo.Actor ! CommonMessages.Use(player)
        }

      case Some(panel: IFFLock) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            panel.Actor ! CommonMessages.Use(player, Some(item))
          case _ => ;
        }

      case Some(obj: Player) =>
        zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        if (obj.isBackpack) {
          if (equipment.isEmpty) {
            log.info(s"${player.Name} is looting the corpse of ${obj.Name}")
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
          (continent.GUID(item_used_guid), kitToBeUsed) match {
            case (Some(kit: Kit), None) =>
              kitToBeUsed = Some(item_used_guid)
              player.Actor ! CommonMessages.Use(player, Some(kit))
            case (Some(_: Kit), Some(_)) | (None, Some(_)) =>
              //a kit is already queued to be used; ignore this request
              sendResponse(ChatMsg(ChatMessageType.UNK_225, wideContents=false, "", "Please wait ...", None))
            case (Some(item), _) =>
              log.error(s"UseItem: ${player.Name} looking for Kit to use, but found $item instead")
            case (None, None) =>
              log.warn(s"UseItem: anticipated a Kit $item_used_guid for ${player.Name}, but can't find it")              }
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
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            locker.Actor ! CommonMessages.Use(player, Some(item))
          case None if locker.Faction == player.Faction || locker.HackedBy.nonEmpty =>
            log.info(s"${player.Name} is accessing a locker")
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
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
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            gen.Actor ! CommonMessages.Use(player, Some(item))
          case None => ;
        }

      case Some(mech: ImplantTerminalMech) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            mech.Actor ! CommonMessages.Use(player, Some(item))
          case None => ;
        }

      case Some(captureTerminal: CaptureTerminal) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            captureTerminal.Actor ! CommonMessages.Use(player, Some(item))
          case _ if specialItemSlotGuid.nonEmpty =>
            continent.GUID(specialItemSlotGuid) match {
              case Some(llu: CaptureFlag) =>
                if (llu.Target.GUID == captureTerminal.Owner.GUID) {
                  continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.LluCaptured(llu))
                } else {
                  log.info(
                    s"LLU target is not this base. Target GUID: ${llu.Target.GUID} This base: ${captureTerminal.Owner.GUID}"
                  )
                }
              case _ => log.warn("Item in specialItemSlotGuid is not registered with continent or is not a LLU")
            }
          case _ => ;
        }

      case Some(obj: FacilityTurret) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))               //try generic
            obj.Actor ! CommonMessages.Use(player, Some((item, unk2.toInt))) //try upgrade path
          case _ => ;
        }

      case Some(obj: Vehicle) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))

          case None if player.Faction == obj.Faction =>
            //access to trunk
            if (
              obj.AccessingTrunk.isEmpty &&
                (!obj.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked) || obj.Owner
                  .contains(player.GUID))
            ) {
              log.info(s"${player.Name} is looking in the ${obj.Definition.Name}'s trunk")
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
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
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            terminal.Actor ! CommonMessages.Use(player, Some(item))

          case None
            if terminal.Owner == Building.NoBuilding || terminal.Faction == player.Faction || terminal.HackedBy.nonEmpty =>
            val tdef = terminal.Definition
            if (tdef.isInstanceOf[MatrixTerminalDefinition]) {
              //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
              sendResponse(
                BindPlayerMessage(BindStatus.Bind, "", display_icon=true, logging=true, SpawnGroup.Sanctuary, 0, 0, terminal.Position)
              )
            } else if (
              tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
                tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal
            ) {
              FindLocalVehicle match {
                case Some(vehicle) =>
                  log.info(
                    s"${player.Name} is accessing a ${terminal.Definition.Name} for ${player.Sex.possessive} ${vehicle.Definition.Name}"
                  )
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
                  log.error(s"UseItem: Expecting a seated vehicle, ${player.Name} found none")
              }
            } else if (tdef == GlobalDefinitions.teleportpad_terminal) {
              //explicit request
              log.info(s"${player.Name} is purchasing a router telepad")
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
              terminal.Actor ! Terminal.Request(
                player,
                ItemTransactionMessage(object_guid, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
              )
            } else if (tdef == GlobalDefinitions.targeting_laser_dispenser) {
              //explicit request
              log.info(s"${player.Name} is purchasing a targeting laser")
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
              terminal.Actor ! Terminal.Request(
                player,
                ItemTransactionMessage(object_guid, TransactionType.Buy, 0, "flail_targeting_laser", 0, PlanetSideGUID(0))
              )
            } else {
              log.info(s"${player.Name} is accessing a ${terminal.Definition.Name}")
              zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
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
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))
          case None if player.Faction == obj.Faction =>
            //deconstruction
            log.info(s"${player.Name} is deconstructing at the ${obj.Owner.Definition.Name}'s spawns")
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            PlayerActionsToCancel()
            terminals.CancelAllProximityUnits()
            zoning.spawn.GoToDeploymentMap()
          case _ => ;
        }

      case Some(obj: SensorDeployable) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))
          case _ => ;
        }

      case Some(obj: TurretDeployable) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))
          case _ => ;
        }

      case Some(obj: TrapDeployable) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))
          case _ => ;
        }

      case Some(obj: ShieldGeneratorDeployable) =>
        equipment match {
          case Some(item) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
            obj.Actor ! CommonMessages.Use(player, Some(item))
          case _ => ;
        }

      case Some(obj: TelepadDeployable) =>
        if (equipment.isEmpty) {
          continent.GUID(obj.Router) match {
            case Some(vehicle: Vehicle) =>
              vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(util: Utility.InternalTelepad) =>
                  zoning.CancelZoningProcessWithDescriptiveReason("cancel")
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
              obj.Actor ! Deployable.Deconstruct()
            case None => ;
          }
        }

      case Some(obj: Utility.InternalTelepad) =>
        continent.GUID(obj.Telepad) match {
          case Some(pad: TelepadDeployable) =>
            zoning.CancelZoningProcessWithDescriptiveReason("cancel")
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

      case Some(obj: CaptureFlag) =>
        // LLU can normally only be picked up the faction that owns it
        if (specialItemSlotGuid.isEmpty) {
          if (obj.Faction == player.Faction) {
            specialItemSlotGuid = Some(obj.GUID)
            player.Carrying = SpecialCarry.CaptureFlag
            continent.LocalEvents ! CaptureFlagManager.PickupFlag(obj, player)
          } else {
            log.warn(
              s"Player ${player.toString} tried to pick up LLU ${obj.GUID} - ${obj.Faction} that doesn't belong to their faction"
            )
          }
        } else if (specialItemSlotGuid.get != obj.GUID) { // Ignore duplicate pickup requests
          log.warn(
            s"Player ${player.toString} tried to pick up LLU ${obj.GUID} - ${obj.Faction} but their special slot already contains $specialItemSlotGuid"
          )
        }

      case Some(_: WarpGate) =>
        zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        (continent.GUID(player.VehicleSeated), equipment) match {
          case (Some(vehicle: Vehicle), Some(item))
            if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
              GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
            vehicle.Actor ! CommonMessages.Use(player, equipment)
          case _ => ;
        }

      case Some(obj) =>
        zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        equipment match {
          case Some(item)
            if GlobalDefinitions.isBattleFrameArmorSiphon(item.Definition) ||
              GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) => ;

          case _ =>
            log.warn(s"UseItem: ${player.Name} does not know how to handle $obj")

        }

      case None => ;
    }
  }

  def handleUnuseItem(pkt: UnuseItemMessage): Unit = {
    val UnuseItemMessage(_, object_guid) = pkt
    ValidObject(object_guid, decorator = "UnuseItem") match {
      case Some(obj: Player) =>
        UnaccessContainer(obj)
        zoning.spawn.TryDisposeOfLootedCorpse(obj)

      case Some(obj: Container) =>
        // Make sure we don't unload the contents of the vehicle the player is seated in
        // An example scenario of this would be closing the trunk contents when rearming at a landing pad
        if (player.VehicleSeated.isEmpty || player.VehicleSeated.get != obj.GUID) {
          UnaccessContainer(obj)
        }

      case _ => ;
    }
  }

  def handleDeployObject(pkt: DeployObjectMessage): Unit = {
    val DeployObjectMessage(guid, _, pos, orient, _) = pkt
    (player.Holsters().find(slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid) match {
      case Some(slot) => slot.Equipment
      case None       => None
    }) match {
      case Some(obj: ConstructionItem) =>
        val ammoType = obj.AmmoType match {
          case DeployedItem.portable_manned_turret => GlobalDefinitions.PortableMannedTurret(player.Faction).Item
          case dtype                               => dtype
        }
        log.info(s"${player.Name} is constructing a $ammoType deployable")
        zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        val dObj: Deployable = Deployables.Make(ammoType)()
        dObj.Position = pos
        dObj.Orientation = orient
        dObj.Faction = player.Faction
        dObj.AssignOwnership(player)
        val tasking: TaskBundle = dObj match {
          case turret: TurretDeployable =>
            GUIDTask.registerDeployableTurret(continent.GUID, turret)
          case _ =>
            GUIDTask.registerObject(continent.GUID, dObj)
        }
        TaskWorkflow.execute(CallBackForTask(tasking, continent.Deployables, Zone.Deployable.BuildByOwner(dObj, player, obj)))

      case Some(obj) =>
        log.warn(s"DeployObject: what is $obj, ${player.Name}?  It's not a construction tool!")
      case None =>
        log.error(s"DeployObject: nothing, ${player.Name}?  It's not a construction tool!")
    }
  }

  def handlePlanetsideAttribute(pkt: PlanetsideAttributeMessage): Unit = {
    val PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value) = pkt
    ValidObject(object_guid, decorator = "PlanetsideAttribute") match {
      case Some(vehicle: Vehicle) if player.avatar.vehicle.contains(vehicle.GUID) =>
        vehicle.Actor ! ServerObject.AttributeMsg(attribute_type, attribute_value)
      case Some(vehicle: Vehicle) =>
        log.warn(
          s"PlanetsideAttribute: ${player.Name} does not own vehicle ${vehicle.GUID} and can not change it"
        )
      // Cosmetics options
      case Some(_: Player) if attribute_type == 106 =>
        avatarActor ! AvatarActor.SetCosmetics(Cosmetic.valuesFromAttributeValue(attribute_value))

      case Some(obj) =>
        log.trace(s"PlanetsideAttribute: ${player.Name} does not know how to apply unknown attributes behavior $attribute_type to ${obj.Definition.Name}")

      case _ => ;
    }
  }

  def handleGenericObjectAction(pkt: GenericObjectActionMessage): Unit = {
    val GenericObjectActionMessage(object_guid, code) = pkt
    ValidObject(object_guid, decorator = "GenericObjectAction") match {
      case Some(vehicle: Vehicle)
        if vehicle.OwnerName.contains(player.Name) =>
        vehicle.Actor ! ServerObject.GenericObjectAction(object_guid, code, Some(player.GUID))

      case Some(tool: Tool) =>
        if (code == 35 &&
          (tool.Definition == GlobalDefinitions.maelstrom || tool.Definition.Name.startsWith("aphelion_laser"))
        ) {
          //maelstrom primary fire mode discharge (no target)
          //aphelion_laser discharge (no target)
          shooting.HandleWeaponFireAccountability(object_guid, PlanetSideGUID(Projectile.baseUID))
        } else {
          ValidObject(player.VehicleSeated, decorator = "GenericObjectAction/Vehicle") match {
            case Some(vehicle: Vehicle)
              if vehicle.OwnerName.contains(player.Name) =>
              vehicle.Actor ! ServerObject.GenericObjectAction(object_guid, code, Some(tool))
            case _ =>
          }
        }
      case _ =>
        log.info(s"${player.Name} - $pkt")
    }
  }

  def handleGenericObjectActionAtPosition(pkt: GenericObjectActionAtPositionMessage): Unit = {
    val GenericObjectActionAtPositionMessage(object_guid, _, _) = pkt
    ValidObject(object_guid, decorator = "GenericObjectActionAtPosition") match {
      case Some(tool: Tool) if GlobalDefinitions.isBattleFrameNTUSiphon(tool.Definition) =>
        shooting.FindContainedWeapon match {
          case (Some(vehicle: Vehicle), weps) if weps.exists(_.GUID == object_guid) =>
            vehicle.Actor ! SpecialEmp.Burst()
          case _ => ;
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
      if (action == 29) {
        log.debug("GenericObjectState: AFK state reported during login")
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
        AvatarActor.savePlayerLocation(player)
        displayCharSavedMsgThenRenewTimer(fixedLen=1800L, varLen=0L) //~30min
        player.AwayFromKeyboard = true
      } else if (action == 30) {
        log.info(s"${player.Name} is back")
        player.AwayFromKeyboard = false
        renewCharSavedTimer(
          Config.app.game.savedMsg.renewal.fixed,
          Config.app.game.savedMsg.renewal.variable
        )
      }
      if (action == GenericActionEnum.DropSpecialItem.id) {
        DropSpecialSlotItem()
      }
      if (action == 15) { //max deployment
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
      } else if (action == 16) { //max deployment
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
            val convertFireModeIndex = if (tool.FireModeIndex == 1) { 0 }
            else { 3 }
            tool.ToFireMode = convertFireModeIndex
            sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
          case _ =>
            log.warn(s"GenericObject: $player is MAX with an unexpected attachment - ${definition.Name}")
        }
      } else if (action == 20) {
        if (player.ExoSuit == ExoSuitType.MAX) {
          ToggleMaxSpecialState(enable = true)
        } else {
          log.warn(s"GenericActionMessage: ${player.Name} can't handle action code 20")
        }
      } else if (action == 21) {
        if (player.ExoSuit == ExoSuitType.MAX) {
          player.Faction match {
            case PlanetSideEmpire.NC =>
              ToggleMaxSpecialState(enable = false)
            case _ =>
              log.warn(s"GenericActionMessage: ${player.Name} tried to cancel an uncancellable MAX special ability")
          }
        } else {
          log.warn(s"GenericActionMessage: ${player.Name} can't handle action code 21")
        }
      } else if (action == 36) { //Looking For Squad ON
        if (squad.squadUI.nonEmpty) {
          if (!squad.lfsm && squad.squadUI(player.CharId).index == 0) {
            squad.lfsm = true
            continent.AvatarEvents ! AvatarServiceMessage(
              s"${player.Faction}",
              AvatarAction.PlanetsideAttribute(player.GUID, 53, 1)
            )
          }
        } else if (!avatar.lookingForSquad) {
          avatarActor ! AvatarActor.SetLookingForSquad(true)
        }
      } else if (action == 37) { //Looking For Squad OFF
        if (squad.squadUI.nonEmpty) {
          if (squad.lfsm && squad.squadUI(player.CharId).index == 0) {
            squad.lfsm = false
            continent.AvatarEvents ! AvatarServiceMessage(
              s"${player.Faction}",
              AvatarAction.PlanetsideAttribute(player.GUID, 53, 0)
            )
          }
        } else if (avatar.lookingForSquad) {
          avatarActor ! AvatarActor.SetLookingForSquad(false)
        }
      } else {
        log.info(s"${player.Name} - $pkt")
      }
    }
  }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = {
    val FavoritesRequest(_, loadoutType, action, line, label) = pkt
    zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    action match {
      case FavoritesAction.Save   =>
        avatarActor ! AvatarActor.SaveLoadout(player, loadoutType, label, line)
      case FavoritesAction.Delete =>
        avatarActor ! AvatarActor.DeleteLoadout(player, loadoutType, line)
      case FavoritesAction.Unknown =>
        log.warn(s"FavoritesRequest: ${player.Name} requested an unknown favorites action")
    }
  }

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit = {
    val GenericCollisionMsg(ctype, p, _, ppos, pv, t, _, tpos, tv, _, _, _) = pkt
    val fallHeight = {
      if (pv.z * pv.z >= (pv.x * pv.x + pv.y * pv.y) * 0.5f) {
        if (heightTrend) {
          val fall = heightLast - heightHistory
          heightHistory = heightLast
          fall
        }
        else {
          val fall = heightHistory - heightLast
          heightLast = heightHistory
          fall
        }
      } else {
        0f
      }
    }
    val (target1, target2, bailProtectStatus, velocity) = (ctype, ValidObject(p, decorator = "GenericCollision/Primary")) match {
      case (CollisionIs.OfInfantry, out @ Some(user: Player))
        if user == player =>
        val bailStatus = session.flying || player.spectator || session.speed > 1f || player.BailProtection
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
        (out, ValidObject(t, decorator = "GenericCollision/GroundVehicle"), bailStatus, pv)
      case (CollisionIs.OfAircraft, out @ Some(v: Vehicle))
        if v.Definition.CanFly && v.Seats(0).occupant.contains(player) =>
        (out, ValidObject(t, decorator = "GenericCollision/Aircraft"), false, pv)
      case (CollisionIs.BetweenThings, _) =>
        log.warn("GenericCollision: CollisionIs.BetweenThings detected - no handling case")
        (None, None, false, Vector3.Zero)
      case _ =>
        (None, None, false, Vector3.Zero)
    }
    val curr = System.currentTimeMillis()
    (target1, t, target2) match {
      case (None, _, _) => ;

      case (Some(us: PlanetSideServerObject with Vitality with FactionAffinity), PlanetSideGUID(0), _) =>
        if (collisionHistory.get(us.Actor) match {
          case Some(lastCollision) if curr - lastCollision <= 1000L =>
            false
          case _ =>
            collisionHistory.put(us.Actor, curr)
            true
        }) {
          if (!bailProtectStatus) {
            HandleDealingDamage(
              us,
              DamageInteraction(
                SourceEntry(us),
                CollisionReason(velocity, fallHeight, us.DamageModel),
                ppos
              )
            )
          }
        }

      case (
        Some(us: PlanetSideServerObject with Vitality with FactionAffinity), _,
        Some(victim: PlanetSideServerObject with Vitality with FactionAffinity)
        ) =>
        if (collisionHistory.get(victim.Actor) match {
          case Some(lastCollision) if curr - lastCollision <= 1000L =>
            false
          case _ =>
            collisionHistory.put(victim.Actor, curr)
            true
        }) {
          val usSource = SourceEntry(us)
          val victimSource = SourceEntry(victim)
          //we take damage from the collision
          if (!bailProtectStatus) {
            HandleDealingDamage(
              us,
              DamageInteraction(
                usSource,
                CollisionWithReason(CollisionReason(velocity - tv, fallHeight, us.DamageModel), victimSource),
                ppos
              )
            )
          }
          //get dealt damage from our own collision (no protection)
          collisionHistory.put(us.Actor, curr)
          HandleDealingDamage(
            victim,
            DamageInteraction(
              victimSource,
              CollisionWithReason(CollisionReason(tv - velocity, 0, victim.DamageModel), usSource),
              tpos
            )
          )
        }

      case _ => ;
    }
  }

  def handleAvatarFirstTimeEvent(pkt: AvatarFirstTimeEventMessage): Unit = {
    val AvatarFirstTimeEventMessage(_, _, _, eventName) = pkt
    avatarActor ! AvatarActor.AddFirstTimeEvent(eventName)
  }

  def handleBugReport(pkt: PlanetSideGamePacket): Unit = {
    val BugReportMessage(
    _/*version_major*/,
    _/*version_minor*/,
    _/*version_date*/,
    _/*bug_type*/,
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
    player.VehicleSeated match {
      case Some(vehicleGUID) =>
        continent.GUID(vehicleGUID) match {
          case Some(obj: Vehicle) =>
            if (!obj.Destroyed) { //vehicle will try to charge even if destroyed
              obj.Actor ! Vehicle.ChargeShields(15)
            }
          case _ =>
            log.warn(
              s"FacilityBenefitShieldChargeRequest: ${player.Name} can not find vehicle ${vehicleGUID.guid} in zone ${continent.id}"
            )
        }
      case None =>
        log.warn(s"FacilityBenefitShieldChargeRequest: ${player.Name} is not seated in a vehicle")
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
    val InvalidTerrainMessage(_, vehicle_guid, alert, _) = pkt
    (continent.GUID(vehicle_guid), continent.GUID(player.VehicleSeated)) match {
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
        log.error(s"InvalidTerrain: ${player.Name} is complaining about a thing@$vehicle_guid that can not be found")
    }
  }

  def handleActionCancel(pkt: ActionCancelMessage): Unit = {
    val ActionCancelMessage(_, _, _) = pkt
    progressBarUpdate.cancel()
    progressBarValue = None
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
    shooting.FindWeapon.foreach {
      case weapon if weapon.Projectile.AutoLock =>
        //projectile with auto-lock instigates a warning on the target
        val detectedTargets = shooting.FindDetectedProjectileTargets(targets)
        if (detectedTargets.nonEmpty) {
          val mode = 7 + (weapon.Projectile == GlobalDefinitions.wasp_rocket_projectile)
          detectedTargets.foreach { target =>
            continent.AvatarEvents ! AvatarServiceMessage(target, AvatarAction.ProjectileAutoLockAwareness(mode))
          }
        }
      case _ => ;
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

  /* supporting functions */

  def ValidObject(id: Int): Option[PlanetSideGameObject] = ValidObject(Some(PlanetSideGUID(id)), decorator = "")

  def ValidObject(id: Int, decorator: String): Option[PlanetSideGameObject] = ValidObject(Some(PlanetSideGUID(id)), decorator)

  def ValidObject(id: PlanetSideGUID): Option[PlanetSideGameObject] = ValidObject(Some(id), decorator = "")

  def ValidObject(id: PlanetSideGUID, decorator: String): Option[PlanetSideGameObject] = ValidObject(Some(id), decorator)

  def ValidObject(id: Option[PlanetSideGUID]): Option[PlanetSideGameObject] = ValidObject(id, decorator = "")

  def ValidObject(id: Option[PlanetSideGUID], decorator: String): Option[PlanetSideGameObject] = {
    val elevatedDecorator = if (decorator.nonEmpty) decorator else "ValidObject"
    id match {
      case Some(guid) =>
        val hint = oldRefsMap.getOrElse(guid, "thing")
        continent.GUID(guid) match {
          case Some(_: LocalProjectile) =>
            shooting.FindProjectileEntry(guid)

          case Some(_: LocalLockerItem) =>
            player.avatar.locker.Inventory.hasItem(guid) match {
              case out@Some(_) =>
                contextSafeEntity = guid
                out
              case None if contextSafeEntity == guid =>
                //safeguard
                None
              case None =>
                //delete stale entity reference from client
                log.warn(
                  s"$elevatedDecorator: ${player.Name} is looking for an invalid GUID $guid, believing it a $hint in ${player.Sex.possessive} locker"
                )
                sendResponse(ObjectDeleteMessage(guid, 0))
                None
            }

          case Some(obj) if obj.HasGUID && obj.GUID != guid =>
            log.error(
              s"$elevatedDecorator: ${player.Name} found a ${obj.Definition.Name} that isn't the $hint ${player.Sex.pronounSubject} thought it was in zone ${continent.id}"
            )
            log.debug(
              s"$elevatedDecorator: potentially fatal error in ${continent.id} - requested $hint with $guid, got ${obj.Definition.Name} with ${obj.GUID}; mismatch"
            )
            None

          case out@Some(obj) if obj.HasGUID =>
            out

          case None if !id.contains(PlanetSideGUID(0)) =>
            //delete stale entity reference from client
            log.error(s"$elevatedDecorator: ${player.Name} has an invalid reference to $hint with GUID $guid in zone ${continent.id}")
            sendResponse(ObjectDeleteMessage(guid, 0))
            None

          case None if contextSafeEntity == guid =>
            //safeguard
            None

          case _ =>
            None
        }

      case None =>
        None
    }
  }

  def buildDependentOperationsForGalaxy(galaxyActor: ActorRef): Unit = {
    if (_vehicleResponse.isEmpty && galaxyActor != Default.Actor) {
      _galaxyResponse = Some(new SessionGalaxyHandlers(sessionData=this, avatarActor, galaxyActor, context))
      _vehicleResponse = Some(new SessionVehicleHandlers(sessionData=this, avatarActor, galaxyActor, context))
    }
  }

  def buildDependentOperations(galaxyActor: ActorRef, clusterActor: typed.ActorRef[ICS.Command]): Unit = {
    if (_zoning.isEmpty && galaxyActor != Default.Actor && clusterActor != Default.typed.Actor) {
      _zoning = Some(new ZoningOperations(sessionData=this, avatarActor, galaxyActor, clusterActor, context))
    }
  }

  def buildDependentOperationsForSquad(squadActor: ActorRef): Unit = {
    if (_squadResponse.isEmpty && squadActor != Default.Actor) {
      _squadResponse = Some(new SessionSquadHandlers(sessionData=this, avatarActor, chatActor, squadActor, context))
    }
  }

  /**
   * Update this player avatar for persistence.
   * Set to `persist` initially.
   */
  def UpdatePersistenceOnly(): Unit = {
    persistFunc()
  }

  /**
   * Do not update this player avatar for persistence.
   * Set to `persistFunc` initially.
   */
  def NoPersistence(): Unit = { }

  def DropSpecialSlotItem(): Unit = {
    specialItemSlotGuid match {
      case Some(guid: PlanetSideGUID) =>
        specialItemSlotGuid = None
        player.Carrying = None
        continent.GUID(guid) match {
          case Some(llu: CaptureFlag) =>
            llu.Carrier match {
              case Some(carrier: Player) if carrier.GUID == player.GUID =>
                continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
              case Some(carrier: Player) =>
                log.warn(s"${player.toString} tried to drop LLU, but it is currently held by ${carrier.toString}")
              case _ =>
                log.warn(s"${player.toString} tried to drop LLU, but nobody is holding it.")
            }
          case _ =>
            log.warn(s"${player.toString} Tried to drop a special item that wasn't recognized. GUID: $guid")
        }
      case _ => ; // Nothing to drop, do nothing.
    }
  }

  /**
   * Enforce constraints on bulk purchases as determined by a given player's previous purchase times and hard acquisition delays.
   * Intended to assist in sanitizing loadout information from the perspective of the player, or target owner.
   * The equipment is expected to be unregistered and already fitted to their ultimate slot in the target container.
   * @param player the player whose purchasing constraints are to be tested
   * @param target the location in which the equipment will be stowed
   * @param slots  the equipment, in the standard object-slot format container
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
          TaskWorkflow.execute(PutLoadoutEquipmentInInventory(target)(item.obj, item.start))
      }
    }
  }

  def SetupProgressChange(rate: Float, finishedAction: () => Unit, stepAction: Float => Boolean): Unit = {
    if (progressBarValue.isEmpty) {
      progressBarValue = Some(-rate)
      context.self ! SessionActor.ProgressEvent(rate, finishedAction, stepAction)
    }
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
   * @see `progressBarUpdate`
   * @see `progressBarValue`
   * @see `essionActor.Progress`
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
              context.self,
              SessionActor.ProgressEvent(delta, completionAction, tickAction)
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
              context.self,
              SessionActor.ProgressEvent(delta, completionAction, tickAction, tick)
            )
          } else {
            progressBarValue = None
          }
        }
      case None => ;
    }
  }

  /* */

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

  def handleUpdateIgnoredPlayers(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case msg: FriendsResponse =>
        sendResponse(msg)
        msg.friends.foreach { f =>
          galaxyService ! GalaxyServiceMessage(GalaxyAction.LogStatusChange(f.name))
        }
      case _ => ;
    }
  }

  def handleUseCooldownRenew(definition: BasicDefinition): Unit = {
    definition match {
      case _: KitDefinition => kitToBeUsed = None
      case _ => ;
    }
  }

  def handleAvatarResponse(avatar: Avatar): Unit = {
    session = session.copy(avatar = avatar)
    accountPersistence ! AccountPersistenceService.Login(avatar.name, avatar.id)
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
    AdministrativeKick(player)
    accountPersistence ! AccountPersistenceService.Kick(player.Name, time)
  }

  def handleSilenced(isSilenced: Boolean): Unit = {
    player.silenced = isSilenced
  }

  /**
   * Construct tasking that registers all aspects of a `Player` avatar
   * as if that player is only just being introduced.
   * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
   * @param tplayer the avatar `Player`
   * @return a `TaskBundle` message
   */
  private[session] def registerNewAvatar(tplayer: Player): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localPlayer   = tplayer
        private val localAnnounce = context.self

        override def description(): String = s"register new player avatar ${localPlayer.Name}"

        def action(): Future[Any] = {
          localAnnounce ! SessionActor.NewPlayerLoaded(localPlayer)
          Future(true)
        }
      },
      List(GUIDTask.registerAvatar(continent.GUID, tplayer))
    )
  }

  /**
   * Construct tasking that registers all aspects of a `Player` avatar
   * as if that player was already introduced and is just being renewed.
   * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
   * @param tplayer the avatar `Player`
   * @return a `TaskBundle` message
   */
  private[session] def registerAvatar(tplayer: Player): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localPlayer   = tplayer
        private val localAnnounce = context.self

        override def description(): String = s"register player avatar ${localPlayer.Name}"

        def action(): Future[Any] = {
          localAnnounce ! SessionActor.PlayerLoaded(localPlayer)
          Future(true)
        }
      },
      List(GUIDTask.registerPlayer(continent.GUID, tplayer))
    )
  }

  /**
   * Construct tasking that adds a completed and registered vehicle into the scene.
   * Use this function to renew the globally unique identifiers on a vehicle that has already been added to the scene once.
   * @param vehicle the `Vehicle` object
   * @see `RegisterVehicleFromSpawnPad`
   * @return a `TaskBundle` message
   */
  private[session] def registerVehicle(vehicle: Vehicle): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle = vehicle

        override def description(): String = s"register a ${localVehicle.Definition.Name}"

        def action(): Future[Any] = {
          Future(true)
        }
      },
      List(GUIDTask.registerVehicle(continent.GUID, vehicle))
    )
  }

  private[session] def registerDrivenVehicle(vehicle: Vehicle, driver: Player): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle  = vehicle
        private val localDriver   = driver
        private val localAnnounce = context.self

        override def description(): String = s"register a ${localVehicle.Definition.Name} driven by ${localDriver.Name}"

        def action(): Future[Any] = {
          localDriver.VehicleSeated = localVehicle.GUID
          Vehicles.Own(localVehicle, localDriver)
          localAnnounce ! SessionActor.NewPlayerLoaded(localDriver)
          Future(true)
        }
      },
      List(GUIDTask.registerAvatar(continent.GUID, driver), GUIDTask.registerVehicle(continent.GUID, vehicle))
    )
  }

  private[session] def unregisterDrivenVehicle(vehicle: Vehicle, driver: Player): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle  = vehicle
        private val localDriver   = driver

        override def description(): String = s"unregister a ${localVehicle.Definition.Name} driven by ${localDriver.Name}"

        def action(): Future[Any] = {
          Future(true)
        }
      },
      List(GUIDTask.unregisterAvatar(continent.GUID, driver), GUIDTask.unregisterVehicle(continent.GUID, vehicle))
    )
  }

  def AccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        AccessVehicleContents(v)
      case o: LockerContainer =>
        AccessGenericContainer(o)
      case p: Player if p.isBackpack =>
        AccessCorpseContents(p)
      case p: PlanetSideServerObject with Container =>
        accessedContainer = Some(p)
      case _ => ;
    }
  }

  def AccessGenericContainer(container: PlanetSideServerObject with Container): Unit = {
    accessedContainer = Some(container)
    DisplayContainerContents(container.GUID, container.Inventory.Items)
  }

  /**
   * Common preparation for interfacing with a vehicle trunk.
   * Join a vehicle-specific group for shared updates.
   * Construct every object in the vehicle's inventory for shared manipulation updates.
   * @see `Container.Inventory`
   * @see `GridInventory.Items`
   * @param vehicle the vehicle
   */
  def AccessVehicleContents(vehicle: Vehicle): Unit = {
    accessedContainer = Some(vehicle)
    AccessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    DisplayContainerContents(vehicle.GUID, vehicle.Inventory.Items)
  }

  /**
   * Common preparation for interfacing with a corpse (former player's backpack).
   * Join a corpse-specific group for shared updates.
   * Construct every object in the player's hands and inventory for shared manipulation updates.
   * @see `Container.Inventory`
   * @see `GridInventory.Items`
   * @see `Player.HolsterItems`
   * @param tplayer the corpse
   */
  def AccessCorpseContents(tplayer: Player): Unit = {
    accessedContainer = Some(tplayer)
    AccessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    DisplayContainerContents(tplayer.GUID, tplayer.HolsterItems())
    DisplayContainerContents(tplayer.GUID, tplayer.Inventory.Items)
  }

  /**
   * Join an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  def AccessContainerChannel(events: ActorRef, channel: String): Unit = {
    events ! Service.Join(channel)
  }

  /**
   * Depict the contents of a container by building them in the local client
   * in their container as a group of detailed entities.
   * @see `ObjectCreateDetailedMessage`
   * @see `ObjectCreateMessageParent`
   * @see `PacketConverter.DetailedConstructorData`
   * @param containerId the container's unique identifier
   * @param items a list of the entities to be depicted
   */
  def DisplayContainerContents(containerId: PlanetSideGUID, items: Iterable[InventoryItem]): Unit = {
    items.foreach(entry => {
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

  /**
   * For whatever conatiner the character considers itself accessing,
   * initiate protocol to release it from "access".
   */
  def UnaccessContainer(): Unit = {
    accessedContainer match {
      case Some(container) => UnaccessContainer(container)
      case _               => ;
    }
  }

  /**
   * For the target container, initiate protocol to release it from "access".
   */
  def UnaccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        UnaccessVehicleContainer(v)
      case o: LockerContainer =>
        UnaccessGenericContainer(o)
        avatarActor ! AvatarActor.SaveLocker()
      case p: Player if p.isBackpack =>
        UnaccessCorpseContainer(p)
      case _: PlanetSideServerObject with Container =>
        accessedContainer = None
      case _ => ;
    }
  }

  def UnaccessGenericContainer(container: Container): Unit = {
    accessedContainer = None
    HideContainerContents(container.Inventory.Items)
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
    UnaccessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    HideContainerContents(vehicle.Inventory.Items)
  }

  /**
   * Common preparation for disengaging from a corpse.
   * Leave the corpse-specific group that was used for shared updates.
   * Deconstruct every object in the backpack's inventory.
   * @param tplayer the corpse
   */
  def UnaccessCorpseContainer(tplayer: Player): Unit = {
    accessedContainer = None
    UnaccessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    HideContainerContents(tplayer.HolsterItems())
    HideContainerContents(tplayer.Inventory.Items)
  }

  /**
   * Leave an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  def UnaccessContainerChannel(events: ActorRef, channel: String): Unit = {
    events ! Service.Leave(Some(channel))
  }

  /**
   * Forget the contents of a container by deleting that content from the local client.
   * @see `InventoryItem`
   * @see `ObjectDeleteMessage`
   * @param items a list of the entities to be depicted
   */
  def HideContainerContents(items: List[InventoryItem]): Unit = {
    items.foreach { entry =>
      sendResponse(ObjectDeleteMessage(entry.obj.GUID, 0))
    }
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
   * For a mount controlled weapon, the vehicle is returned.
   * For the player's hand, the player is returned.
   * @return a `Tuple` of the returned values;
   *         the first value is a `Container` object;
   *         the second value is an `Equipment` object in the former
   */
  def FindContainedEquipment(): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    continent.GUID(player.VehicleSeated) match {
      case Some(vehicle: Mountable with MountableWeapons with Container) =>
        vehicle.PassengerInSeat(player) match {
          case Some(seat_num) =>
            (Some(vehicle), vehicle.WeaponControlledFromSeat(seat_num))
          case None =>
            (None, Set.empty)
        }
      case _ =>
        player.Slot(player.DrawnSlot).Equipment match {
          case Some(a) =>
            (Some(player), Set(a))
          case _ =>
            (None, Set.empty)
        }
    }
  }

  /**
   * Check two locations for a controlled piece of equipment that is associated with the `player`
   * and has the specified global unique identifier number.
   */
  def FindContainedEquipment(
                              guid: PlanetSideGUID
                            ): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    val (o, equipment) = FindContainedEquipment()
    equipment.find { _.GUID == guid } match {
      case Some(equip) => (o, Set(equip))
      case None        => (None, Set.empty)
    }
  }

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output.
   * @return an `Equipment` object
   */
  def FindEquipment(): Set[Equipment] = FindContainedEquipment()._2

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output
   * and only discovers `Equipment` with the specified global unique identifier number.
   * @return an `Equipment` object
   */
  def FindEquipment(guid: PlanetSideGUID): Option[Equipment] = FindEquipment().find { _.GUID == guid }

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
      case Some(_) =>
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
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Mobile, 0, unk3=false, Vector3.Zero))
      continent.VehicleEvents ! VehicleServiceMessage(
        continent.id,
        VehicleAction.DeployRequest(player.GUID, obj.GUID, DriveState.Mobile, 0, unk2=false, Vector3.Zero)
      )
      "; enforcing Mobile deployment state"
    } else {
      ""
    }
    log.error(s"DeployRequest: ${player.Name} can not transition $obj to $state - $reason$mobileShift")
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
   * Send a PlanetsideAttributeMessage packet to the client
   * @param target_guid The target of the attribute
   * @param attribute_number The attribute number
   * @param attribute_value The attribute value
   */
  def SendPlanetsideAttributeMessage(
                                      target_guid: PlanetSideGUID,
                                      attribute_number: PlanetsideAttributeEnum,
                                      attribute_value: Long
                                    ): Unit = {
    sendResponse(PlanetsideAttributeMessage(target_guid, attribute_number, attribute_value))
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
    shooting.shootingStart.clear()
    shooting.shootingStop.clear()
    progressBarUpdate.cancel()
    progressBarValue = None
    terminals.lastTerminalOrderFulfillment = true
    kitToBeUsed = None
    collisionHistory.clear()
    accessedContainer match {
      case Some(v: Vehicle) =>
        val vguid = v.GUID
        vehicles.ConditionalDriverVehicleControl(v)
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
    (shooting.prefire ++ shooting.shooting).foreach { guid =>
      sendResponse(ChangeFireStateMessage_Stop(guid))
      continent.AvatarEvents ! AvatarServiceMessage(
        continent.id,
        AvatarAction.ChangeFireState_Stop(player.GUID, guid)
      )
    }
    shooting.prefire.clear()
    shooting.shooting.clear()
    if (session.flying) {
      chatActor ! ChatActor.Message(ChatMsg(ChatMessageType.CMT_FLY, wideContents=false, "", "off", None))
    }
    if (session.speed > 1) {
      chatActor ! ChatActor.Message(ChatMsg(ChatMessageType.CMT_SPEED, wideContents=false, "", "1.000", None))
    }
  }

  /**
   * Calculate the amount of damage to be dealt to an active `target`
   * using the information reconstructed from a `Resolvedprojectile`
   * and affect the `target` in a synchronized manner.
   * The active `target` and the target of the `DamageResult` do not have be the same.
   * While the "tell" for being able to sustain damage is an entity of type `Vitality`,
   * only specific `Vitality` entity types are being screened for sustaining damage.
   * @see `DamageResistanceModel`
   * @see `Vitality`
   * @param target a valid game object that is known to the server
   * @param data a projectile that will affect the target
   */
  def HandleDealingDamage(target: PlanetSideGameObject with Vitality, data: DamageInteraction): Unit = {
    val func = data.calculate()
    target match {
      case obj: Player if obj.CanDamage && obj.Actor != Default.Actor =>
        if (obj.CharId != player.CharId) {
          log.info(s"${player.Name} is attacking ${obj.Name}")
        } else {
          log.info(s"${player.Name} hurt ${player.Sex.pronounObject}self")
        }
        // auto kick players damaging spectators
        if (obj.spectator && obj != player) {
          AdministrativeKick(player)
        } else {
          obj.Actor ! Vitality.Damage(func)
        }

      case obj: Vehicle if obj.CanDamage =>
        val name = player.Name
        val ownerName = obj.OwnerName.getOrElse("someone")
        if (ownerName.equals(name)) {
          log.info(s"$name is damaging ${player.Sex.possessive} own ${obj.Definition.Name}")
        } else {
          log.info(s"$name is attacking $ownerName's ${obj.Definition.Name}")
        }
        obj.Actor ! Vitality.Damage(func)

      case obj: Amenity if obj.CanDamage =>
        obj.Actor ! Vitality.Damage(func)

      case obj: Deployable if obj.CanDamage =>
        val name = player.Name
        val ownerName = obj.OwnerName.getOrElse("someone")
        if (ownerName.equals(name)) {
          log.info(s"$name is damaging ${player.Sex.possessive} own ${obj.Definition.Name}")
        } else {
          log.info(s"$name is attacking $ownerName's ${obj.Definition.Name}")
        }
        obj.Actor ! Vitality.Damage(func)

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
      case (currElem, curr, maxElem, max) =>
        //fields must update in ordered pairs: max, curr
        sendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    })
  }

  /**
   * Common actions related to constructing a new `Deployable` object in the game environment.<br>
   * <br>
   * The map icon for the deployable just introduced is also created on the clients of all faction-affiliated players.
   * This icon is important as, short of destroying it,
   * the owner has no other means of controlling the created object that it is associated with.
   * @param obj the `Deployable` object to be built
   */
  def DeployableBuildActivity(obj: Deployable): Unit = {
    sendResponse(GenericObjectActionMessage(obj.GUID, 21)) //reset build cooldown
    UpdateDeployableUIElements(avatar.deployables.UpdateUIElement(obj.Definition.Item))
  }

  /**
   * A simple object searching algorithm that is limited to containers currently known and accessible by the player.
   * If all relatively local containers are checked and the object is not found,
   * the player's locker inventory will be checked, and then
   * the game environment (items on the ground) will be checked too.
   * If the target object is discovered, it is removed from its current location and is completely destroyed.
   * @see `RequestDestroyMessage`
   * @see `Zone.ItemIs.Where`
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
      case Some((parent, Some(_))) =>
        obj.Position = Vector3.Zero
        RemoveOldEquipmentFromInventory(parent)(obj)
        true

      case _ =>
        if (player.avatar.locker.Inventory.Remove(object_guid)) {
          sendResponse(ObjectDeleteMessage(object_guid, 0))
          true
        } else if (continent.EquipmentOnGround.contains(obj)) {
          obj.Position = Vector3.Zero
          continent.Ground ! Zone.Ground.RemoveItem(object_guid)
          continent.AvatarEvents ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
          true
        } else {
          Zone.EquipmentIs.Where(obj, object_guid, continent) match {
            case None =>
              true
            case Some(Zone.EquipmentIs.Orphaned()) =>
              if (obj.HasGUID) {
                TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
              }
              true
            case _ =>
              log.warn(s"RequestDestroy: equipment $obj exists, but ${player.Name} can not reach it to dispose of it")
              false
          }
        }
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
                             obj: Deployable,
                             guid: PlanetSideGUID,
                             pos: Vector3,
                             orient: Vector3,
                             deletionType: Int
                           ): Unit = {
    sendResponse(TriggerEffectMessage("spawn_object_failed_effect", pos, orient))
    sendResponse(PlanetsideAttributeMessage(guid, 29, 1)) //make deployable vanish
    sendResponse(ObjectDeleteMessage(guid, deletionType))
  }

  /**
   * Search through the player's holsters and their inventory space
   * and remove all `BoomerTrigger` objects, both functionally and visually.
   * @return all discovered `BoomTrigger` objects
   */
  def RemoveBoomerTriggersFromInventory(): List[BoomerTrigger] = {
    val events = continent.AvatarEvents
    val zoneId = continent.id
    (player.Inventory.Items ++ player.HolsterItems())
      .collect { case InventoryItem(obj: BoomerTrigger, index) =>
        player.Slot(index).Equipment = None
        continent.GUID(obj.Companion) match {
          case Some(mine: BoomerDeployable) => mine.Actor ! Deployable.Ownership(None)
          case _ => ;
        }
        if (player.VisibleSlots.contains(index)) {
          events ! AvatarServiceMessage(
            zoneId,
            AvatarAction.ObjectDelete(Service.defaultPlayerGUID, obj.GUID)
          )
        } else {
          sendResponse(ObjectDeleteMessage(obj.GUID, 0))
        }
        obj
      }
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
        internalTelepad.Telepad = remoteTelepad.GUID //necessary; backwards link to the (new) telepad
        TelepadLike.StartRouterInternalTelepad(continent, router.GUID, internalTelepad)
        TelepadLike.LinkTelepad(continent, remoteTelepad.GUID)
      case _ =>
        router.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) =>
            sendResponse(ObjectDeleteMessage(util.GUID, 0))
          case _ => ;
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
  def UseRouterTelepadSystem(
                              router: Vehicle,
                              internalTelepad: InternalTelepad,
                              remoteTelepad: TelepadDeployable,
                              src: PlanetSideGameObject with TelepadLike,
                              dest: PlanetSideGameObject with TelepadLike
                            ): Unit = {
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
      log.warn(s"UseRouterTelepadSystem: ${player.Name} can not teleport")
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
   * From a mount, find the weapon controlled from it, and update the ammunition counts for that weapon's magazines.
   * @param objWithSeat the object that owns seats (and weaponry)
   * @param seatNum the mount
   */
  def UpdateWeaponAtSeatPosition(objWithSeat: MountableWeapons, seatNum: Int): Unit = {
    objWithSeat.WeaponControlledFromSeat(seatNum) foreach {
      case weapon: Tool =>
        //update mounted weapon belonging to mount
        weapon.AmmoSlots.foreach(slot => {
          //update the magazine(s) in the weapon, specifically
          val magazine = slot.Box
          sendResponse(InventoryStateMessage(magazine.GUID, weapon.GUID, magazine.Capacity.toLong))
        })
      case _ => ; //no weapons to update
    }
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
            log.warn(s"${player.Name} tried to use a MAX special ability but their faction doesn't have one")
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
   * The atypical response to receiving a `KeepAliveMessage` packet from the client.<br>
   * <br>
   * `KeepAliveMessage` packets are the primary vehicle for persistence due to client reporting
   * in the case where the player's avatar is riding in a vehicle in a mount with no weapon to control.
   * @see `KeepAliveMessage`
   * @see `keepAliveFunc`
   * @see `turnCounterFunc`
   * @see `persist`
   */
  def KeepAlivePersistence(): Unit = {
    zoning.spawn.interimUngunnedVehicle = None
    persist()
    if (player.HasGUID) {
      turnCounterFunc(player.GUID)
    } else {
      turnCounterFunc(PlanetSideGUID(0))
    }
  }

  /**
   *  A really atypical response to receiving a `KeepAliveMessage` packet from the client
   *  that applies only during the character select portion and part of the first zone load activity.
   */
  def KeepAlivePersistenceInitial(): Unit = {
    persist()
    if (player != null && player.HasGUID) {
      keepAliveFunc = KeepAlivePersistence
    }
  }

  def AdministrativeKick(tplayer: Player): Unit = {
    log.warn(s"${tplayer.Name} has been kicked by ${player.Name}")
    tplayer.death_by = -1
    accountPersistence ! AccountPersistenceService.Kick(tplayer.Name)
    //get out of that vehicle
    vehicles.GetMountableAndSeat(None, tplayer, continent) match {
      case (Some(obj), Some(seatNum)) =>
        tplayer.VehicleSeated = None
        obj.Seats(seatNum).unmount(tplayer)
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seatNum, unk2=false, obj.GUID)
        )
      case _ => ;
    }
  }

  def KickedByAdministration(): Unit = {
    sendResponse(DisconnectMessage("@kick_w"))
    context.system.scheduler.scheduleOnce(
      delay = 300 milliseconds,
      middlewareActor.toClassic,
      MiddlewareActor.Teardown()
    )
  }

  def ImmediateDisconnect(): Unit = {
    if (avatar != null) {
      accountPersistence ! AccountPersistenceService.Logout(avatar.name)
    }
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def updateBlockMap(target: BlockMapEntity, zone: Zone, newCoords: Vector3): Unit = {
    target.blockMapEntry match {
      case Some(entry) =>
        if (BlockMap.findSectorIndices(continent.blockMap, newCoords, entry.rangeX, entry.rangeY).toSet.equals(entry.sectors)) {
          target.updateBlockMapEntry(newCoords) //soft update
        } else {
          zone.actor ! ZoneActor.UpdateBlockMap(target, newCoords) //hard update
        }
      case None        => ;
    }
  }

  private[support] var oldRefsMap: mutable.HashMap[PlanetSideGUID, String] = new mutable.HashMap[PlanetSideGUID, String]()
  def updateOldRefsMap(): Unit = {
    if(player.HasGUID) {
      oldRefsMap.addAll(
        (continent.GUID(player.VehicleSeated) match {
          case Some(v : Vehicle) =>
            v.Weapons.toList.collect {
              case (_, slot : EquipmentSlot) if slot.Equipment.nonEmpty => updateOldRefsMap(slot.Equipment.get)
            }.flatten ++
              updateOldRefsMap(v.Inventory)
          case _ =>
            Map.empty[PlanetSideGUID, String]
        }) ++
          (accessedContainer match {
            case Some(cont) => updateOldRefsMap(cont.Inventory)
            case None => Map.empty[PlanetSideGUID, String]
          }) ++
          player.Holsters().toList.collect {
            case slot if slot.Equipment.nonEmpty => updateOldRefsMap(slot.Equipment.get)
          }.flatten ++
          updateOldRefsMap(player.Inventory) ++
          updateOldRefsMap(player.avatar.locker.Inventory)
      )
    }
  }

  def updateOldRefsMap(inventory: net.psforever.objects.inventory.GridInventory): IterableOnce[(PlanetSideGUID, String)] = {
    inventory.Items.flatMap {
      case InventoryItem(o, _) => updateOldRefsMap(o)
    }
  }

  def updateOldRefsMap(item: PlanetSideGameObject): IterableOnce[(PlanetSideGUID, String)] = {
    item match {
      case t: Tool =>
        t.AmmoSlots.map { slot =>
          val box = slot.Box
          box.GUID -> box.Definition.Name
        } :+ (t.GUID -> t.Definition.Name)
      case _ =>
        Seq(item.GUID -> item.Definition.Name)
    }
  }

  def fallHeightTracker(zHeight: Float): Unit = {
    if ((heightTrend && heightLast - zHeight >= 0.5f) ||
      (!heightTrend && zHeight - heightLast >= 0.5f)) {
      heightTrend = !heightTrend
      //      if (heightTrend) {
      //        GetMountableAndSeat(None, player, continent) match {
      //          case (Some(v: Vehicle), _)  => v.BailProtection = false
      //          case _                      => player.BailProtection = false
      //        }
      //      }
      heightHistory = zHeight
    }
    heightLast = zHeight
  }

  def displayCharSavedMsgThenRenewTimer(fixedLen: Long, varLen: Long): Unit = {
    charSaved()
    renewCharSavedTimer(fixedLen, varLen)
  }

  def renewCharSavedTimer(fixedLen: Long, varLen: Long): Unit = {
    charSavedTimer.cancel()
    val delay = (fixedLen + (varLen * scala.math.random()).toInt).seconds
    charSavedTimer = context.system.scheduler.scheduleOnce(delay, context.self, SessionActor.CharSavedMsg)
  }

  def charSaved(): Unit = {
    sendResponse(ChatMsg(ChatMessageType.UNK_227, wideContents=false, "", "@charsaved", None))
  }

  def failWithError(error: String): Unit = {
    log.error(error)
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def sendResponse(packet: PlanetSidePacket): Unit = {
    middlewareActor ! MiddlewareActor.Send(packet)
  }

  def assignEventBus(msg: Any): Boolean = {
    msg match {
      case LookupResult("accountIntermediary", endpoint) =>
        accountIntermediary = endpoint
        true
      case LookupResult("accountPersistence", endpoint) =>
        accountPersistence = endpoint
        true
      case LookupResult("galaxy", endpoint) =>
        galaxyService = endpoint
        buildDependentOperationsForGalaxy(endpoint)
        buildDependentOperations(endpoint, cluster)
        true
      case LookupResult("squad", endpoint) =>
        squadService = endpoint
        buildDependentOperationsForSquad(endpoint)
        true
      case ICS.InterstellarClusterServiceKey.Listing(listings) =>
        cluster = listings.head
        buildDependentOperations(galaxyService, cluster)
        true

      case _ =>
        false
    }
  }

  def whenAllEventBusesLoaded(): Boolean = {
    accountIntermediary != Default.Actor &&
      accountPersistence != Default.Actor &&
      _vehicleResponse.nonEmpty &&
      _galaxyResponse.nonEmpty &&
      _squadResponse.nonEmpty &&
      _zoning.nonEmpty
  }

  def stop(): Unit = {
    continent.AvatarEvents ! Service.Leave()
    continent.LocalEvents ! Service.Leave()
    continent.VehicleEvents ! Service.Leave()
    context.stop(avatarActor)
    context.stop(chatActor)
    galaxyService ! Service.Leave()
    if (avatar != null && squadService != Default.Actor) {
      squadService ! Service.Leave(Some(s"${avatar.faction}"))
    }
    clientKeepAlive.cancel()
    progressBarUpdate.cancel()
    charSavedTimer.cancel()
    shooting.stop()
    vehicles.stop()
    avatarResponse.stop()
    localResponse.stop()
    mountResponse.stop()
    terminals.stop()
    _vehicleResponse.foreach { _.stop() }
    _galaxyResponse.foreach { _.stop() }
    _squadResponse.foreach { _.stop() }
    _zoning.foreach { _.stop() }
  }
}
