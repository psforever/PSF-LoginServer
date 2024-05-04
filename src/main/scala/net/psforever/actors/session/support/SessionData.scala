// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.services.chat.ChatService

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
//
import net.psforever.actors.net.MiddlewareActor
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects._
import net.psforever.objects.avatar._
import net.psforever.objects.ce._
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.vehicles._
import net.psforever.objects.vital._
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones._
import net.psforever.objects.zones.blockmap.{BlockMap, BlockMapEntity, SectorGroup, SectorPopulation}
import net.psforever.services.ServiceManager
import net.psforever.services.ServiceManager.Lookup
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.ServiceManager.LookupResult
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{Service, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.Config

object SessionData {
  //noinspection ScalaUnusedSymbol
  private def NoTurnCounterYet(guid: PlanetSideGUID): Unit = { }

  private def updateOldRefsMap(inventory: net.psforever.objects.inventory.GridInventory): IterableOnce[(PlanetSideGUID, String)] = {
    inventory.Items.flatMap {
      case InventoryItem(o, _) => updateOldRefsMap(o)
    }
  }

  private def updateOldRefsMap(item: PlanetSideGameObject): IterableOnce[(PlanetSideGUID, String)] = {
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
}

class SessionData(
                   val middlewareActor: typed.ActorRef[MiddlewareActor.Command],
                   implicit val context: ActorContext
                 ) extends SessionSource {
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

  private[session] val log = org.log4s.getLogger
  private[session] var theSession: Session = Session()
  private[session] var accountIntermediary: ActorRef = Default.Actor
  private[session] var accountPersistence: ActorRef = Default.Actor
  private[session] var galaxyService: ActorRef = Default.Actor
  private[session] var squadService: ActorRef = Default.Actor
  private[session] var cluster: typed.ActorRef[ICS.Command] = Default.typed.Actor
  private[session] var chatService: typed.ActorRef[ChatService.Command] = Default.typed.Actor
  private[session] var connectionState: Int = 25
  private[session] var persistFunc: () => Unit = noPersistence
  private[session] var persist: () => Unit = updatePersistenceOnly
  private[session] var keepAliveFunc: () => Unit = keepAlivePersistenceInitial
  private[session] var turnCounterFunc: PlanetSideGUID => Unit = SessionData.NoTurnCounterYet
  private[session] val oldRefsMap: mutable.HashMap[PlanetSideGUID, String] = new mutable.HashMap[PlanetSideGUID, String]()
  private var contextSafeEntity: PlanetSideGUID = PlanetSideGUID(0)

  val general: GeneralOperations =
    new GeneralOperations(sessionLogic=this, avatarActor, context)
  val shooting: WeaponAndProjectileOperations =
    new WeaponAndProjectileOperations(sessionLogic=this, avatarActor, context)
  val vehicles: VehicleOperations =
    new VehicleOperations(sessionLogic=this, avatarActor, context)
  val avatarResponse: SessionAvatarHandlers =
    new SessionAvatarHandlers(sessionLogic=this, avatarActor, context)
  val localResponse: SessionLocalHandlers =
    new SessionLocalHandlers(sessionLogic=this, context)
  val mountResponse: SessionMountHandlers =
    new SessionMountHandlers(sessionLogic=this, avatarActor, context)
  val terminals: SessionTerminalHandlers =
    new SessionTerminalHandlers(sessionLogic=this, avatarActor, context)
  private var vehicleResponseOpt: Option[SessionVehicleHandlers] = None
  private var galaxyResponseOpt: Option[SessionGalaxyHandlers] = None
  private var squadResponseOpt: Option[SessionSquadHandlers] = None
  private var zoningOpt: Option[ZoningOperations] = None
  private var chatOpt: Option[ChatOperations] = None
  def vehicleResponseOperations: SessionVehicleHandlers = vehicleResponseOpt.orNull
  def galaxyResponseHandlers: SessionGalaxyHandlers = galaxyResponseOpt.orNull
  def squad: SessionSquadHandlers = squadResponseOpt.orNull
  def zoning: ZoningOperations = zoningOpt.orNull
  def chat: ChatOperations = chatOpt.orNull

  ServiceManager.serviceManager ! Lookup("accountIntermediary")
  ServiceManager.serviceManager ! Lookup("accountPersistence")
  ServiceManager.serviceManager ! Lookup("galaxy")
  ServiceManager.serviceManager ! Lookup("squad")
  ServiceManager.receptionist ! Receptionist.Find(ICS.InterstellarClusterServiceKey, context.self)
  ServiceManager.receptionist ! Receptionist.Find(ChatService.ChatServiceKey, context.self)

  /**
   * updated when an upstream packet arrives;
   * allow to be a little stale for a short while
   */
  private[session] var localSector: SectorPopulation = SectorGroup(Nil)

  def session: Session = theSession

  def session_=(session: Session): Unit = {
    avatarActor ! AvatarActor.SetSession(session)
    theSession = session
  }

  def account: Account = theSession.account

  def continent: Zone = theSession.zone

  def player: Player = theSession.player

  def avatar: Avatar = theSession.avatar

  /* setup functions */

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
        buildDependentOperationsForZoning(endpoint, cluster)
        true
      case LookupResult("squad", endpoint) =>
        squadService = endpoint
        buildDependentOperationsForSquad(endpoint)
        true
      case ICS.InterstellarClusterServiceKey.Listing(listings) =>
        cluster = listings.head
        buildDependentOperationsForZoning(galaxyService, cluster)
        buildDependentOperationsForChat(chatService, cluster)
        true
      case ChatService.ChatServiceKey.Listing(listings) =>
        chatService = listings.head
        buildDependentOperationsForChat(chatService, cluster)
        true

      case _ =>
        false
    }
  }

  def buildDependentOperationsForGalaxy(galaxyActor: ActorRef): Unit = {
    if (vehicleResponseOpt.isEmpty && galaxyActor != Default.Actor) {
      galaxyResponseOpt = Some(new SessionGalaxyHandlers(sessionLogic=this, avatarActor, galaxyActor, context))
      vehicleResponseOpt = Some(new SessionVehicleHandlers(sessionLogic=this, avatarActor, galaxyActor, context))
    }
  }

  def buildDependentOperationsForZoning(galaxyActor: ActorRef, clusterActor: typed.ActorRef[ICS.Command]): Unit = {
    if (zoningOpt.isEmpty && galaxyActor != Default.Actor && clusterActor != Default.typed.Actor) {
      zoningOpt = Some(new ZoningOperations(sessionLogic=this, avatarActor, galaxyActor, clusterActor, context))
    }
  }

  def buildDependentOperationsForSquad(squadActor: ActorRef): Unit = {
    if (squadResponseOpt.isEmpty && squadActor != Default.Actor) {
      squadResponseOpt = Some(new SessionSquadHandlers(sessionLogic=this, avatarActor, squadActor, context))
    }
  }

  def buildDependentOperationsForChat(chatService: typed.ActorRef[ChatService.Command], clusterActor: typed.ActorRef[ICS.Command]): Unit = {
    if (chatOpt.isEmpty && chatService != Default.typed.Actor && clusterActor != Default.typed.Actor) {
      chatOpt = Some(new ChatOperations(sessionLogic=this, avatarActor, chatService, clusterActor, context))
    }
  }

  def whenAllEventBusesLoaded(): Boolean = {
    accountIntermediary != Default.Actor &&
      accountPersistence != Default.Actor &&
      vehicleResponseOpt.nonEmpty &&
      galaxyResponseOpt.nonEmpty &&
      squadResponseOpt.nonEmpty &&
      zoningOpt.nonEmpty &&
      chatOpt.nonEmpty
  }

  /* support functions */

  def validObject(id: Int): Option[PlanetSideGameObject] = validObject(Some(PlanetSideGUID(id)), decorator = "")

  def validObject(id: Int, decorator: String): Option[PlanetSideGameObject] = validObject(Some(PlanetSideGUID(id)), decorator)

  def validObject(id: PlanetSideGUID): Option[PlanetSideGameObject] = validObject(Some(id), decorator = "")

  def validObject(id: PlanetSideGUID, decorator: String): Option[PlanetSideGameObject] = validObject(Some(id), decorator)

  def validObject(id: Option[PlanetSideGUID]): Option[PlanetSideGameObject] = validObject(id, decorator = "")

  def validObject(id: Option[PlanetSideGUID], decorator: String): Option[PlanetSideGameObject] = {
    val elevatedDecorator = if (decorator.nonEmpty) decorator else "ValidObject"
    id match {
      case Some(guid) =>
        val hint = oldRefsMap.getOrElse(guid, "thing")
        continent.GUID(guid) match {
          case Some(_: LocalProjectile) =>
            shooting.FindProjectileEntry(guid)

          case Some(_: LocalLockerItem) =>
            player.avatar.locker.Inventory.hasItem(guid) match {
              case out @ Some(_) =>
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

          case out @ Some(obj) if obj.HasGUID =>
            out

          case None if !id.contains(PlanetSideGUID(0)) =>
            //delete stale entity reference from client
            //deleting guid=0 will cause BAD things to happen
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

  /**
   * Update this player avatar for persistence.
   * Set to `persist` initially.
   */
  def updatePersistenceOnly(): Unit = {
    persistFunc()
  }

  /**
   * Do not update this player avatar for persistence.
   * Set to `persistFunc` initially.
   */
  def noPersistence(): Unit = { }

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
  def findContainedEquipment(): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    continent.GUID(player.VehicleSeated) match {
      case Some(vehicle: Mountable with MountableWeapons with Container) =>
        vehicle.PassengerInSeat(player) match {
          case Some(seatNum) =>
            (Some(vehicle), vehicle.WeaponControlledFromSeat(seatNum))
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
  def findContainedEquipment(
                              guid: PlanetSideGUID
                            ): (Option[PlanetSideGameObject with Container], Set[Equipment]) = {
    val (o, equipment) = findContainedEquipment()
    equipment.find { _.GUID == guid } match {
      case Some(equip) => (o, Set(equip))
      case None        => (None, Set.empty)
    }
  }

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output.
   * @return an `Equipment` object
   */
  def findEquipment(): Set[Equipment] = findContainedEquipment()._2

  /**
   * Runs `FindContainedEquipment` but ignores the `Container` object output
   * and only discovers `Equipment` with the specified global unique identifier number.
   * @return an `Equipment` object
   */
  def findEquipment(guid: PlanetSideGUID): Option[Equipment] = findEquipment().find { _.GUID == guid }

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
  def actionsToCancel(): Unit = {
    general.actionsToCancel()
    shooting.actionsToCancel()
    terminals.actionsToCancel()
    if (session.flying) {
      session = session.copy(flying = false)
      chat.commandFly(contents = "off", recipient = "")
    }
    if (session.speed > 1) {
      session = session.copy(speed = 1)
      chat.commandSpeed(ChatMsg(ChatMessageType.CMT_SPEED, "1.000"), contents = "1.000")
    }
  }

  /**
   * Calculate the amount of damage to be dealt to an active `target`
   * using the information reconstructed from a `ResolvedProjectile`
   * and affect the `target` in a synchronized manner.
   * The active `target` and the target of the `DamageResult` do not have be the same.
   * While the "tell" for being able to sustain damage is an entity of type `Vitality`,
   * only specific `Vitality` entity types are being screened for sustaining damage.
   * @see `DamageResistanceModel`
   * @see `Vitality`
   * @param target a valid game object that is known to the server
   * @param data a projectile that will affect the target
   */
  def handleDealingDamage(target: PlanetSideGameObject with Vitality, data: DamageInteraction): Unit = {
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
          administrativeKick(player)
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

      case _ => ()
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
  def keepAlivePersistence(): Unit = {
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
  def keepAlivePersistenceInitial(): Unit = {
    persist()
    if (player != null && player.HasGUID) {
      keepAliveFunc = keepAlivePersistence
    }
  }

  def updateBlockMap(target: BlockMapEntity, newCoords: Vector3): Unit = {
    target.blockMapEntry.foreach { entry =>
      val sectorIndices = BlockMap.findSectorIndices(continent.blockMap, newCoords, entry.rangeX, entry.rangeY).toSet
      if (sectorIndices.equals(entry.sectors)) {
        target.updateBlockMapEntry(newCoords) //soft update
        localSector = continent.blockMap.sector(sectorIndices, Config.app.game.playerDraw.rangeMax.toFloat)
      } else {
        continent.actor ! ZoneActor.UpdateBlockMap(target, newCoords) //hard update
      }
    }
  }

  def updateLocalBlockMap(pos: Vector3): Unit = {
    localSector = continent.blockMap.sector(pos, Config.app.game.playerDraw.rangeMax.toFloat)
  }

  def updateOldRefsMap(): Unit = {
    if(player.HasGUID) {
      oldRefsMap.addAll(
        (continent.GUID(player.VehicleSeated) match {
          case Some(v : Vehicle) =>
            v.Weapons.toList.collect {
              case (_, slot : EquipmentSlot) if slot.Equipment.nonEmpty => SessionData.updateOldRefsMap(slot.Equipment.get)
            }.flatten ++
              SessionData.updateOldRefsMap(v.Inventory)
          case _ =>
            Map.empty[PlanetSideGUID, String]
        }) ++
          (general.accessedContainer match {
            case Some(cont) => SessionData.updateOldRefsMap(cont.Inventory)
            case None => Map.empty[PlanetSideGUID, String]
          }) ++
          player.Holsters().toList.collect {
            case slot if slot.Equipment.nonEmpty => SessionData.updateOldRefsMap(slot.Equipment.get)
          }.flatten ++
          SessionData.updateOldRefsMap(player.Inventory) ++
          SessionData.updateOldRefsMap(player.avatar.locker.Inventory)
      )
    }
  }

  def administrativeKick(tplayer: Player): Unit = {
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
      case _ => ()
    }
  }

  def kickedByAdministration(): Unit = {
    sendResponse(DisconnectMessage("@kick_w"))
    context.system.scheduler.scheduleOnce(
      delay = 300.milliseconds,
      middlewareActor.toClassic,
      MiddlewareActor.Teardown()
    )
  }

  def immediateDisconnect(): Unit = {
    if (avatar != null) {
      accountPersistence ! AccountPersistenceService.Logout(avatar.name)
    }
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def failWithError(error: String): Unit = {
    log.error(error)
    middlewareActor ! MiddlewareActor.Teardown()
  }

  def sendResponse(packet: PlanetSidePacket): Unit = {
    middlewareActor ! MiddlewareActor.Send(packet)
  }

  def stop(): Unit = {
    context.stop(avatarActor)
    general.stop()
    shooting.stop()
    vehicles.stop()
    avatarResponse.stop()
    localResponse.stop()
    mountResponse.stop()
    terminals.stop()
    vehicleResponseOpt.foreach(_.stop())
    galaxyResponseOpt.foreach(_.stop())
    squadResponseOpt.foreach(_.stop())
    zoningOpt.foreach(_.stop())
    chatOpt.foreach(_.stop())
    continent.AvatarEvents ! Service.Leave()
    continent.LocalEvents ! Service.Leave()
    continent.VehicleEvents ! Service.Leave()
    galaxyService ! Service.Leave()
    if (avatar != null && squadService != Default.Actor) {
      squadService ! Service.Leave()
    }
  }
}
