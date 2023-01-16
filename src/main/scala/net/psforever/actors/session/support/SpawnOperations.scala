// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
//
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.login.WorldSession.RemoveOldEquipmentFromInventory
import net.psforever.objects.avatar.{Avatar, PlayerControl, Shortcut => AvatarShortcut}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.converter.CorpseConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.structures.{Building, StructureType, WarpGate}
import net.psforever.objects.vehicles.CarrierBehavior
import net.psforever.objects.zones.{Zone, Zoning}
import net.psforever.objects._
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.objectcreate.{ObjectClass, ObjectCreateMessageParent}
import net.psforever.packet.game._
import net.psforever.persistence.Savedplayer
import net.psforever.services.account.PlayerToken
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{RemoverActor, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.{Config, DefinitionUtil}
import net.psforever.zones.Zones

class SpawnOperations(
                                 val sessionData: SessionData,
                                 avatarActor: typed.ActorRef[AvatarActor.Command],
                                 cluster: typed.ActorRef[ICS.Command],
                                 implicit val context: ActorContext
                               ) extends CommonSessionInterfacingFuncs {
  var deadState: DeadState.Value = DeadState.Dead
  var loginChatMessage: String = ""
  var amsSpawnPoints: List[SpawnPoint] = Nil
  var noSpawnPointHere: Boolean = false
  var setupAvatarFunc: () => Unit = AvatarCreate
  var setCurrentAvatarFunc: Player => Unit = SetCurrentAvatarNormally
  var nextSpawnPoint: Option[SpawnPoint] = None
  var interimUngunnedVehicle: Option[PlanetSideGUID] = None
  var interimUngunnedVehicleSeat: Option[Int] = None
  /** Upstream message counter<br>
   * Checks for server acknowledgement of the following messages in the following conditions:<br>
   * `PlayerStateMessageUpstream` (infantry)<br>
   * `VehicleStateMessage` (driver mount only)<br>
   * `ChildObjectStateMessage` (any gunner mount that is not the driver)<br>
   * `KeepAliveMessage` (any passenger mount that is not the driver)<br>
   * As they should arrive roughly every 250 milliseconds this allows for a very crude method of scheduling tasks up to four times per second
   */
  var upstreamMessageCount: Int = 0
  var shiftPosition: Option[Vector3] = None
  var shiftOrientation: Option[Vector3] = None
  var drawDeloyableIcon: PlanetSideGameObject with Deployable => Unit = RedrawDeployableIcons
  var setAvatar: Boolean = false
  var turnCounterFunc: PlanetSideGUID => Unit = TurnCounterDuringInterim
  var reviveTimer: Cancellable = Default.Cancellable
  var respawnTimer: Cancellable = Default.Cancellable

  /* */

  def handleLoginInfoNowhere(name: String, from: ActorRef): Unit = {
    log.info(s"LoginInfo: player $name is considered a fresh character")
    sessionData.persistFunc = sessionData.UpdatePersistence(from)
    deadState = DeadState.RespawnTime
    val tplayer = new Player(avatar)
    session = session.copy(player = tplayer)
    //actual zone is undefined; going to our sanctuary
    sessionData.zoning.RandomSanctuarySpawnPosition(tplayer)
    DefinitionUtil.applyDefaultLoadout(tplayer)
    avatarActor ! AvatarActor.LoginAvatar(context.self)
  }

  def handleLoginInfoSomewhere(name: String, inZone: Zone, optionalSavedData: Option[Savedplayer], from: ActorRef): Unit = {
    log.info(s"LoginInfo: player $name is considered a fresh character")
    sessionData.persistFunc = sessionData.UpdatePersistence(from)
    deadState = DeadState.RespawnTime
    session = session.copy(player = new Player(avatar))
    player.Zone = inZone
    optionalSavedData match {
      case Some(results) =>
        val health = results.health
        val hasHealthUponLogin = health > 0
        val position = Vector3(results.px * 0.001f, results.py * 0.001f, results.pz * 0.001f)
        player.Position = position
        player.Orientation = Vector3(0f, 0f, results.orientation * 0.001f)
        /*
        @reset_sanctuary=You have been returned to the sanctuary because you played another character.
         */
        if (hasHealthUponLogin) {
          player.Spawn()
          player.Health = health
          player.Armor = results.armor
          player.ExoSuit = ExoSuitType(results.exosuitNum)
          AvatarActor.buildContainedEquipmentFromClob(player, results.loadout, log)
          if (player.ExoSuit == ExoSuitType.MAX) {
            player.DrawnSlot = 0
            player.ResistArmMotion(PlayerControl.maxRestriction)
          }
        } else {
          player.ExoSuit = ExoSuitType.Standard
          DefinitionUtil.applyDefaultLoadout(player)
        }
        if (player.isAlive) {
          sessionData.zoning.zoningType = Zoning.Method.Login
          player.ZoningRequest = Zoning.Method.Login
          sessionData.zoning.zoningChatMessageType = ChatMessageType.UNK_227
          if (Zones.sanctuaryZoneNumber(player.Faction) != inZone.Number) {
            val pfaction = player.Faction
            val buildings = inZone.Buildings.values
            val ourBuildings = buildings.filter {
              _.Faction == pfaction
            }.toSeq
            val playersInZone = inZone.Players
            val friendlyPlayersInZone = playersInZone.count {
              _.faction == pfaction
            }
            val noFriendlyPlayersInZone = friendlyPlayersInZone == 0
            if (inZone.map.cavern) {
              loginChatMessage = "@reset_sanctuary_locked"
              //You have been returned to the sanctuary because the location you logged out is not available.
              player.Zone = Zone.Nowhere
            } else if (ourBuildings.isEmpty && (amsSpawnPoints.isEmpty || noFriendlyPlayersInZone)) {
              loginChatMessage = "@reset_sanctuary_locked"
              //You have been returned to the sanctuary because the location you logged out is not available.
              player.Zone = Zone.Nowhere
            } else if (friendlyPlayersInZone > 137 || playersInZone.size > 413) {
              loginChatMessage = "@reset_sanctuary_full"
              //You have been returned to the sanctuary because the zone you logged out on is full.
              player.Zone = Zone.Nowhere
            } else {
              val inBuildingSOI = buildings.filter { b =>
                val soi2 = b.Definition.SOIRadius * b.Definition.SOIRadius
                Vector3.DistanceSquared(b.Position, position) < soi2
              }
              if (inBuildingSOI.nonEmpty) {
                if (!inBuildingSOI.exists {
                  ourBuildings.contains
                }) {
                  sessionData.zoning.zoningType = Zoning.Method.Reset
                  player.ZoningRequest = Zoning.Method.Reset
                  sessionData.zoning.zoningChatMessageType = ChatMessageType.UNK_228
                }
              } else {
                if (noFriendlyPlayersInZone) {
                  loginChatMessage = "@reset_sanctuary_inactive"
                  //You have been returned to the sanctuary because the location you logged out is not available.
                  player.Zone = Zone.Nowhere
                }
              }
            }
          }
        } else {
          //player is dead; go back to sanctuary
          loginChatMessage = "@reset_sanctuary_inactive"
          //You have been returned to the sanctuary because the location you logged out is not available.
          player.Zone = Zone.Nowhere
        }

      case None =>
        player.Spawn()
        player.ExoSuit = ExoSuitType.Standard
        DefinitionUtil.applyDefaultLoadout(player)
    }
    avatarActor ! AvatarActor.LoginAvatar(context.self)
  }

  def handleLoginInfoRestore(name: String, inZone: Zone, pos: Vector3, from: ActorRef): Unit = {
    log.info(s"RestoreInfo: player $name is already logged in zone ${inZone.id}; rejoining that character")
    sessionData.persistFunc = sessionData.UpdatePersistence(from)
    //tell the old WorldSessionActor to kill itself by using its own subscriptions against itself
    inZone.AvatarEvents ! AvatarServiceMessage(name, AvatarAction.TeardownConnection())
    //find and reload previous player
    (
      inZone.Players.find(p => p.name.equals(name)),
      inZone.LivePlayers.find(p => p.Name.equals(name))
    ) match {
      case (_, Some(p)) if p.death_by == -1 =>
        //player is not allowed
        sessionData.KickedByAdministration()

      case (Some(a), Some(p)) if p.isAlive =>
        //rejoin current avatar/player
        log.info(s"RestoreInfo: player $name is alive")
        deadState = DeadState.Alive
        session = session.copy(player = p, avatar = a)
        sessionData.persist()
        setupAvatarFunc = AvatarRejoin
        avatarActor ! AvatarActor.ReplaceAvatar(a)
        avatarLoginResponse(a)

      case (Some(a), Some(p)) =>
        //convert player to a corpse (unless in vehicle); automatic recall to closest spawn point
        log.info(s"RestoreInfo: player $name is dead")
        deadState = DeadState.Dead
        session = session.copy(player = p, avatar = a)
        sessionData.persist()
        HandleReleaseAvatar(p, inZone)
        avatarActor ! AvatarActor.ReplaceAvatar(a)
        avatarLoginResponse(a)

      case (Some(a), None) =>
        //respawn avatar as a new player; automatic recall to closest spawn point
        log.info(s"RestoreInfo: player $name had released recently")
        deadState = DeadState.RespawnTime
        session = session.copy(
          player = inZone.Corpses.findLast(c => c.Name.equals(name)) match {
            case Some(c) =>
              c //the last corpse of this user should be where they died
            case None =>
              val tplayer = Player(a) //throwaway
              tplayer.Position = pos
              tplayer.Release //for proper respawn
              tplayer.Zone = inZone
              tplayer
          }, avatar = a
        )
        avatarActor ! AvatarActor.ReplaceAvatar(a)
        avatarLoginResponse(a)

      case _ =>
        //fall back to sanctuary/prior?
        log.info(s"RestoreInfo: player $name could not be found in game world")
        context.self.forward(PlayerToken.LoginInfo(name, Zone.Nowhere, None))
    }
  }

  def handleLoginCanNot(name: String, reason: PlayerToken.DeniedLoginReason.Value): Unit = {
    log.warn(s"LoginInfo: $name is denied login for reason - $reason")
    reason match {
      case PlayerToken.DeniedLoginReason.Kicked => sessionData.KickedByAdministration()
      case _ => sendResponse(DisconnectMessage("You will be logged out."))
    }
  }

  def handleReleaseAvatarRequest(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case ReleaseAvatarRequestMessage() =>
        log.info(s"${player.Name} on ${continent.id} has released")
        reviveTimer.cancel()
        GoToDeploymentMap()
        HandleReleaseAvatar(player, continent)
      case _ => ;
    }
  }

  def handleSpawnRequest(pkt: PlanetSideGamePacket)(implicit context: ActorContext): Unit = {
    pkt match {
      case SpawnRequestMessage(_, spawnGroup, _, _, zoneNumber) =>
        log.info(s"${player.Name} on ${continent.id} wants to respawn in zone #$zoneNumber")
        if (deadState != DeadState.RespawnTime) {
          deadState = DeadState.RespawnTime
          cluster ! ICS.GetNearbySpawnPoint(
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
          log.warn(s"SpawnRequestMessage: request consumed because ${player.Name} is already respawning ...")
        }
      case _ => ;
    }
  }

  def handleSpawnPointResponse(response: Option[(Zone, SpawnPoint)]): Unit = {
    sessionData.zoning.zoningType match {
      case Zoning.Method.InstantAction if response.isEmpty =>
        sessionData.zoning.CancelZoningProcessWithReason("@InstantActionNoHotspotsAvailable")

      case Zoning.Method.InstantAction if sessionData.zoning.zoningStatus == Zoning.Status.Request =>
        sessionData.zoning.beginZoningCountdown(() => {
          cluster ! ICS.GetInstantActionSpawnPoint(player.Faction, context.self)
        })

      case Zoning.Method.Reset =>
        player.ZoningRequest = Zoning.Method.Login
        sessionData.zoning.zoningType = Zoning.Method.Login
        response match {
          case Some((zone, spawnPoint)) =>
            loginChatMessage = "@login_reposition_to_friendly_facility" //Your previous location was held by the enemy. You have been moved to the nearest friendly facility.
            val (pos, ori) = spawnPoint.SpecificPoint(player)
            LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
          case _ =>
            loginChatMessage = "@login_reposition_to_sanctuary" //Your previous location was held by the enemy.  As there were no operational friendly facilities on that continent, you have been brought back to your Sanctuary.
            sessionData.zoning.RequestSanctuaryZoneSpawn(player, player.Zone.Number)
        }

      case Zoning.Method.Login =>
        resolveZoningSpawnPointLoad(response, Zoning.Method.Login)

      case ztype =>
        if (ztype != Zoning.Method.None) {
          log.warn(
            s"SpawnPointResponse: ${player.Name}'s zoning was not in order at the time a response was received; attempting to guess what ${player.Sex.pronounSubject} wants to do"
          )
        }
        val previousZoningType = ztype
        sessionData.zoning.CancelZoningProcess()
        sessionData.PlayerActionsToCancel()
        sessionData.terminals.CancelAllProximityUnits()
        sessionData.DropSpecialSlotItem()
        continent.Population ! Zone.Population.Release(avatar)
        resolveZoningSpawnPointLoad(response, previousZoningType)
    }
  }

  def handleNewPlayerLoaded(tplayer: Player): Unit = {
    //new zone
    log.info(s"${tplayer.Name} has spawned into ${session.zone.id}")
    sessionData.oldRefsMap.clear()
    sessionData.persist = sessionData.UpdatePersistenceAndRefs
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
    if (isAcceptableNextSpawnPoint) {
      //important! the LoadMapMessage must be processed by the client before the avatar is created
      setupAvatarFunc()
      //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
      turnCounterFunc = interimUngunnedVehicle match {
        case Some(_) =>
          TurnCounterDuringInterimWhileInPassengerSeat
        case None if sessionData.zoning.zoningType == Zoning.Method.Login || sessionData.zoning.zoningType == Zoning.Method.Reset =>
          TurnCounterLogin
        case None =>
          TurnCounterDuringInterim
      }
      sessionData.keepAliveFunc = NormalKeepAlive
      upstreamMessageCount = 0
      setAvatar = false
      sessionData.persist()
    } else {
      //look for different spawn point in same zone
      cluster ! ICS.GetNearbySpawnPoint(
        session.zone.Number,
        tplayer,
        Seq(SpawnGroup.Facility, SpawnGroup.Tower, SpawnGroup.AMS),
        context.self
      )
    }
  }

  def handlePlayerLoaded(tplayer: Player): Unit = {
    //same zone
    log.info(s"${tplayer.Name} will respawn")
    tplayer.avatar = avatar
    session = session.copy(player = tplayer)
    if (isAcceptableNextSpawnPoint) {
      //try this spawn point
      setupAvatarFunc()
      //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
      turnCounterFunc = interimUngunnedVehicle match {
        case Some(_) =>
          TurnCounterDuringInterimWhileInPassengerSeat
        case None =>
          TurnCounterDuringInterim
      }
      sessionData.keepAliveFunc = NormalKeepAlive
      upstreamMessageCount = 0
      setAvatar = false
      sessionData.persist()
    } else {
      //look for different spawn point in same zone
      cluster ! ICS.GetNearbySpawnPoint(
        continent.Number,
        tplayer,
        Seq(SpawnGroup.Facility, SpawnGroup.Tower, SpawnGroup.AMS),
        context.self
      )
    }
  }

  /* */

  def isAcceptableNextSpawnPoint: Boolean = isAcceptableSpawnPoint(nextSpawnPoint)

  def isAcceptableSpawnPoint(spawnPoint: SpawnPoint): Boolean = isAcceptableSpawnPoint(Some(spawnPoint))

  def isAcceptableSpawnPoint(spawnPoint: Option[SpawnPoint]): Boolean = {
    spawnPoint match {
      case Some(aSpawnPoint) =>
        !aSpawnPoint.isOffline &&
          (aSpawnPoint.Owner match {
            case w: WarpGate => w.Active
            case b: Building => b.Faction == player.Faction
            case v: Vehicle  => v.Faction == player.Faction && !v.Destroyed && v.DeploymentState == DriveState.Deployed
            case _           => true
          })
      case None            => true
    }
  }

  /**
   * During login, when the avatar is set, the response code sets up session and deployable toolbox stats.
   * Immediately contact the interstellar cluster to deal with zoning conditions.
   * Only call this once during login and never any time after that.
   * @param avatar the avatar being set as the current one belonging to this session
   */
  def avatarLoginResponse(avatar: Avatar): Unit = {
    session = session.copy(avatar = avatar)
    Deployables.InitializeDeployableQuantities(avatar)
    cluster ! ICS.FilterZones(_ => true, context.self)
  }

  /**
   * na
   * @param tplayer na
   * @param zone na
   */
  def HandleReleaseAvatar(tplayer: Player, zone: Zone): Unit = {
    sessionData.keepAliveFunc = sessionData.KeepAlivePersistence
    tplayer.Release
    tplayer.VehicleSeated match {
      case None =>
        PrepareToTurnPlayerIntoCorpse(tplayer, zone)
      case Some(_) =>
        tplayer.VehicleSeated = None
        zone.Population ! Zone.Population.Release(avatar)
        sendResponse(ObjectDeleteMessage(tplayer.GUID, 0))
        TaskWorkflow.execute(GUIDTask.unregisterPlayer(zone.GUID, tplayer))
    }
  }

  def handleSetPosition(position: Vector3): Unit = {
    if (sessionData.vehicles.serverVehicleControlVelocity.isEmpty) {
      sessionData.PlayerActionsToCancel()
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
          vehicle.PassengerInSeat(player) match {
            case Some(0) =>
              deadState = DeadState.Release // cancel movement updates
              vehicle.Position = position
              LoadZonePhysicalSpawnPoint(continent.id, position, Vector3.z(vehicle.Orientation.z), 0 seconds, None)
            case _ => // not seated as the driver, in which case we can't move
          }
        case None =>
          deadState = DeadState.Release // cancel movement updates
          player.Position = position
          sendResponse(PlayerStateShiftMessage(ShiftState(0, position, player.Orientation.z, None)))
          deadState = DeadState.Alive // must be set here
        case _ => // seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
      }
    }
  }

  /**
   * A part of the process of spawning the player into the game world.
   * The function should work regardless of whether the player is alive or dead - it will make them alive.
   * It adds the `WorldSessionActor`-current `Player` to the current zone and sends out the expected packets.<br>
   * <br>
   * If that player is in a vehicle, it will construct that vehicle.
   * If the player is the driver of the vehicle,
   * they must temporarily be removed from the driver mount in order for the vehicle to be constructed properly.
   * These two previous statements operate through similar though distinct mechanisms and imply different conditions.
   * In reality, they produce the same output but enforce different relationships between the components.
   * The vehicle without a rendered player will always be created if that vehicle exists.
   * The vehicle should only be constructed once.
   */
  def AvatarCreate(): Unit = {
    val health = player.Health
    val armor  = player.Armor
    val events = continent.VehicleEvents
    val zoneid = continent.id
    avatarActor ! AvatarActor.ResetImplants()
    player.Spawn()
    if (health != 0) {
      player.Health = health
      player.Armor = armor
    }
    sessionData.vehicles.GetKnownVehicleAndSeat() match {
      case (Some(vehicle: Vehicle), Some(seat: Int)) =>
        //if the vehicle is the cargo of another vehicle in this zone
        val carrierInfo = continent.GUID(vehicle.MountedIn) match {
          case Some(carrier: Vehicle) =>
            (Some(carrier), carrier.CargoHolds.find({ case (_, hold) => hold.occupant.contains(vehicle) }))
          case _ =>
            (None, None)
        }
        //vehicle and driver/passenger
        sessionData.zoning.interstellarFerry = None
        val vdef  = vehicle.Definition
        val vguid = vehicle.GUID
        vehicle.Position = shiftPosition.getOrElse(vehicle.Position)
        vehicle.Orientation = shiftOrientation.getOrElse(vehicle.Orientation)
        val vdata = if (seat == 0) {
          //driver
          if (vehicle.Zone ne continent) {
            continent.Transport ! Zone.Vehicle.Spawn(vehicle)
          }
          //as the driver, we must temporarily exclude ourselves from being in the vehicle during its creation
          val mount = vehicle.Seats(0)
          mount.unmount(player)
          player.VehicleSeated = None
          val data = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, data))
          mount.mount(player)
          player.VehicleSeated = vguid
          Vehicles.Own(vehicle, player)
          vehicle.CargoHolds.values
            .collect { case hold if hold.isOccupied => hold.occupant.get }
            .foreach { _.MountedIn = vguid }
          events ! VehicleServiceMessage(
            zoneid,
            VehicleAction.LoadVehicle(player.GUID, vehicle, vdef.ObjectId, vguid, data)
          )
          carrierInfo match {
            case (Some(carrier), Some((index, _))) =>
              CarrierBehavior.CargoMountBehaviorForOthers(carrier, vehicle, index, player.GUID)
            case _ =>
              vehicle.MountedIn = None
          }
          vehicle.allowInteraction = true
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
          val vehicleToDelete = sessionData.zoning.interstellarFerryTopLevelGUID.orElse(originalSeated).getOrElse(PlanetSideGUID(0))
          val zone            = vehicle.PreviousGatingManifest().get.origin
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.UnloadVehicle(player.GUID, vehicle, vehicleToDelete)
          )
          log.debug(
            s"AvatarCreate: cleaning up ghost of transitioning vehicle ${vehicle.Definition.Name}@${vehicleToDelete.guid} in zone ${zone.id}"
          )
        }
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        log.debug(s"AvatarCreate (vehicle): ${player.Name}'s ${vehicle.Definition.Name}")
        log.trace(s"AvatarCreate (vehicle): ${player.Name}'s ${vehicle.Definition.Name} - $vguid -> $vdata")
        AvatarCreateInVehicle(player, vehicle, seat)

      case _ =>
        player.VehicleSeated = None
        val packet = player.avatar.definition.Packet
        val data   = packet.DetailedConstructorData(player).get
        val guid   = player.GUID
        sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, guid, data))
        continent.AvatarEvents ! AvatarServiceMessage(
          zoneid,
          AvatarAction.LoadPlayer(guid, ObjectClass.avatar, guid, packet.ConstructorData(player).get, None)
        )
        log.debug(s"AvatarCreate: ${player.Name}")
        log.trace(s"AvatarCreate: ${player.Name} - $guid -> $data")
    }
    continent.Population ! Zone.Population.Spawn(avatar, player, avatarActor)
    avatarActor ! AvatarActor.RefreshPurchaseTimes()
    //begin looking for conditions to set the avatar
    context.system.scheduler.scheduleOnce(delay = 250 millisecond, context.self, SessionActor.SetCurrentAvatar(player, 200))
  }

  /**
   * Create an avatar character so that avatar's player is mounted in a vehicle's mount.
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
   * @param tplayer the player avatar seated in the vehicle's mount
   * @param vehicle the vehicle the player is riding
   * @param seat the mount index
   */
  def AvatarCreateInVehicle(tplayer: Player, vehicle: Vehicle, seat: Int): Unit = {
    val pdef  = tplayer.avatar.definition
    val pguid = tplayer.GUID
    val vguid = vehicle.GUID
    tplayer.VehicleSeated = None
    val pdata = pdef.Packet.DetailedConstructorData(tplayer).get
    tplayer.VehicleSeated = vguid
    log.debug(s"AvatarCreateInVehicle: ${player.Name}")
    log.trace(s"AvatarCreateInVehicle: ${player.Name} - $pguid -> $pdata")
    sendResponse(ObjectCreateDetailedMessage(pdef.ObjectId, pguid, pdata))
    if (seat == 0 || vehicle.WeaponControlledFromSeat(seat).nonEmpty) {
      sendResponse(ObjectAttachMessage(vguid, pguid, seat))
      sessionData.AccessContainer(vehicle)
      sessionData.UpdateWeaponAtSeatPosition(vehicle, seat)
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
   * they must temporarily be removed from the driver mount in order for the vehicle to be constructed properly.
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
    sessionData.vehicles.GetKnownVehicleAndSeat() match {
      case (Some(vehicle: Vehicle), Some(seat: Int)) =>
        //vehicle and driver/passenger
        val vdef  = vehicle.Definition
        val vguid = vehicle.GUID
        val vdata = if (seat == 0) {
          val seat = vehicle.Seats(0)
          seat.unmount(player)
          val _vdata = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, _vdata))
          seat.mount(player)
          _vdata
        } else {
          val _vdata = vdef.Packet.ConstructorData(vehicle).get
          sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, vguid, _vdata))
          _vdata
        }
        Vehicles.ReloadAccessPermissions(vehicle, continent.id)
        log.debug(s"AvatarCreate (vehicle): ${player.Name}'s ${vehicle.Definition.Name}")
        log.trace(s"AvatarCreate (vehicle): ${player.Name}'s ${vehicle.Definition.Name} - $vguid -> $vdata")
        val pdef   = player.avatar.definition
        val pguid  = player.GUID
        player.VehicleSeated = None
        val pdata = pdef.Packet.DetailedConstructorData(player).get
        player.VehicleSeated = vguid
        sendResponse(ObjectCreateDetailedMessage(pdef.ObjectId, pguid, pdata))
        log.debug(s"AvatarRejoin: ${player.Name} - $pguid -> $pdata")
        if (seat == 0 || vehicle.WeaponControlledFromSeat(seat).nonEmpty) {
          sendResponse(ObjectAttachMessage(vguid, pguid, seat))
          sessionData.AccessContainer(vehicle)
          sessionData.UpdateWeaponAtSeatPosition(vehicle, seat)
        } else {
          interimUngunnedVehicle = Some(vguid)
          interimUngunnedVehicleSeat = Some(seat)
        }

      case _ =>
        player.VehicleSeated = None
        val packet = player.avatar.definition.Packet
        val data   = packet.DetailedConstructorData(player).get
        val guid   = player.GUID
        sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, guid, data))
        log.debug(s"AvatarRejoin: ${player.Name} - $guid -> $data")
    }
    avatarActor ! AvatarActor.RefreshPurchaseTimes()
    setupAvatarFunc = AvatarCreate
    //begin looking for conditions to set the avatar
    context.system.scheduler.scheduleOnce(delay = 750 millisecond, context.self, SessionActor.SetCurrentAvatar(player, 200))
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
    obj.silenced = tplayer.silenced
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
            obj.Actor ! Deployable.Ownership(None)
          case Some(_) | None => ;
        }
      })
      sessionData.RemoveBoomerTriggersFromInventory().foreach(trigger => { sessionData.NormalItemDrop(obj, continent)(trigger) })
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
   * @see `GUIDTask.unregisterPlayer`
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
      zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(pguid, pguid))
      TaskWorkflow.execute(GUIDTask.unregisterPlayer(zone.GUID, tplayer))
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
    //the corpse as a receptacle
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
   * Process recovered spawn request information to start the process of spawning an avatar player entity
   * in a specific zone in a specific place in that zone after a certain amount of time has elapsed.<br>
   * <br>
   * To load: a zone, a spawn point, a spawning target entity, and the time it takes to spawn are required.
   * Everything but the spawn point can be determined from the information already available to the context
   * (does not need to be passed in as a parameter).
   * The zone is more reliable when passed in as a parameter since local references may have changed.
   * The spawn point defines the spawn position as well as the spawn orientation.
   * Any of information provided can be used to calculate the time to spawn.
   * The session's knowledge of the zoning event is also used to assist with the spawning event.<br>
   * <br>
   * If no spawn information has been provided, abort the whole process (unsafe!).
   *
   * @param spawnPointTarget an optional paired zone entity and a spawn point within the zone
   * @param zoningType       a token that references the manner of zone transfer
   */
  def resolveZoningSpawnPointLoad(spawnPointTarget: Option[(Zone, SpawnPoint)], zoningType: Zoning.Method): Unit = {
    spawnPointTarget match {
      case Some((zone, spawnPoint)) =>
        val obj = continent.GUID(player.VehicleSeated) match {
          case Some(obj: Vehicle) if !obj.Destroyed => obj
          case _ => player
        }
        val (pos, ori) = spawnPoint.SpecificPoint(obj)
        if (sessionData.zoning.zoningType == Zoning.Method.InstantAction)
          LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
        else
          LoadZonePhysicalSpawnPoint(zone.id, pos, ori, CountSpawnDelay(zone.id, spawnPoint, continent.id), Some(spawnPoint))
      case None =>
        log.warn(
          s"SpawnPointResponse: ${player.Name} received no spawn point response when asking InterstellarClusterService"
        )
        if (Config.app.game.warpGates.defaultToSanctuaryDestination) {
          log.warn(s"SpawnPointResponse: sending ${player.Name} home")
          sessionData.zoning.RequestSanctuaryZoneSpawn(player, currentZone = 0)
        }
    }
  }

  /**
   * Given an origin and a destination, determine how long the process of traveling should take in reconstruction time.
   * For most destinations, the unit of receiving ("spawn point") determines the reconstruction time.
   * Possession of a lattice-linked friendly Bio Laboratory halves the time of spawning at facilities.
   * In a special consideration, travel to any sanctuary or sanctuary-special zone should be as immediate as zone loading.
   *
   * @param toZoneId     the zone where the target is headed
   * @param toSpawnPoint the unit the target is using as a destination
   * @param fromZoneId   the zone where the target current is located
   * @return how long the spawning process will take
   */
  def CountSpawnDelay(toZoneId: String, toSpawnPoint: SpawnPoint, fromZoneId: String): FiniteDuration = {
    val sanctuaryZoneId = Zones.sanctuaryZoneId(player.Faction)
    if (fromZoneId.equals("Nowhere") || sanctuaryZoneId.equals(toZoneId) || !isAcceptableNextSpawnPoint) {
      //first login, to sanctuary, resolution of invalid spawn point
      0 seconds
    } else {
      //for other zones ...
      //biolabs have/grant benefits
      val cryoBenefit: Float = toSpawnPoint.Owner match {
        case b: Building if b.hasLatticeBenefit(LatticeBenefit.BioLaboratory) => 0.5f
        case _                                                                => 1f
      }
      //TODO cumulative death penalty
      (toSpawnPoint.Definition.Delay.toFloat * cryoBenefit).seconds
    }
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
   * @param zoneId      the zone in which the player will be placed
   * @param pos         the game world coordinates where the player will be positioned
   * @param ori         the direction in which the player will be oriented
   * @param respawnTime the character downtime spent respawning, as clocked on the redeployment screen;
   *                    does not factor in any time required for loading zone or game objects
   */
  def LoadZonePhysicalSpawnPoint(
                                  zoneId: String,
                                  pos: Vector3,
                                  ori: Vector3,
                                  respawnTime: FiniteDuration,
                                  physSpawnPoint: Option[SpawnPoint]
                                ): Unit = {
    log.info(s"${player.Name} will load in zone $zoneId at position $pos in $respawnTime")
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
    nextSpawnPoint = physSpawnPoint
    shiftPosition = Some(pos)
    shiftOrientation = Some(ori)
    respawnTimer = context.system.scheduler.scheduleOnce(respawnTime) {
      if (player.isBackpack) { // if the player is dead, he is handled as dead infantry, even if he died in a vehicle
        // new player is spawning
        val newPlayer = RespawnClone(player)
        newPlayer.Position = pos
        newPlayer.Orientation = ori
        sessionData.zoning.LoadZoneAsPlayer(newPlayer, zoneId)
      } else {
        avatarActor ! AvatarActor.DeactivateActiveImplants()
        sessionData.zoning.interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
          case Some(vehicle: Vehicle) => // driver or passenger in vehicle using a warp gate, or a droppod
            sessionData.zoning.LoadZoneInVehicle(vehicle, pos, ori, zoneId)

          case _ if player.HasGUID => // player is deconstructing self or instant action
            val player_guid = player.GUID
            sendResponse(ObjectDeleteMessage(player_guid, 4))
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ObjectDelete(player_guid, player_guid, 4)
            )
            player.Position = pos
            player.Orientation = ori
            sessionData.zoning.LoadZoneAsPlayer(player, zoneId)

          case _ => //player is logging in
            player.Position = pos
            player.Orientation = ori
            sessionData.zoning.LoadZoneAsPlayer(player, zoneId)
        }
      }
    }
  }

  /**
   * The user is either already in the current zone and merely transporting from one location to another,
   * also called "dying", or occasionally "deconstructing,"
   * or is completely switching in between zones.
   * These correspond to the message `NewPlayerLoaded` for the case of "dying" or the latter zone switching case,
   * and `PlayerLoaded` for "deconstruction."
   * In the latter case, the user must wait for the zone to be recognized as loaded for the server
   * and this is performed through the send `LoadMapMessage`, receive `BeginZoningMessage` exchange.
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
  def ReadyToSetCurrentAvatar(player: Player, max_attempts: Int, attempt: Int): Unit = {
    respawnTimer.cancel()
    val zoneLoaded = sessionData.zoning.zoneLoaded
    val waitingOnUpstream = upstreamMessageCount == 0
    if (attempt >= max_attempts && waitingOnUpstream) {
      log.warn(
        s"SetCurrentAvatar/${player.Name}: max attempt failure: " +
          s"zone=${
            if (zoneLoaded.contains(true)) "loaded"
            else if (zoneLoaded.contains(false)) "failed"
            else "unloaded"
          }, " +
          s"guid=${player.HasGUID}, control=${player.Actor != Default.Actor}, no upstream messaging"
      )
      zoneLoaded match {
        case None | Some(false) =>
          log.warn(
            s"SetCurrentAvatar/${player.Name}: max attempt failure: failed to load intended destination zone; routing to faction sanctuary"
          )
          sessionData.zoning.RequestSanctuaryZoneSpawn(player, continent.Number)
        case _ =>
          log.warn(
            s"SetCurrentAvatar/${player.Name}: max attempt failure: the zone loaded but elements remain unready; restarting the process ..."
          )
          val pos = shiftPosition.getOrElse(player.Position)
          val orient = shiftOrientation.getOrElse(player.Orientation)
          deadState = DeadState.Release
          sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, pos, player.Faction, unk5=true))
          val toZoneId = continent.id
          player.Die
          continent.Population ! Zone.Population.Leave(avatar) //does not matter if it doesn't work
          sessionData.zoning.zoneLoaded = None
          sessionData.zoning.zoneReload = true
          LoadZonePhysicalSpawnPoint(toZoneId, pos, orient, respawnTime = 0 seconds, None)
      }
    } else if (player.isAlive) {
      if (
        zoneLoaded.contains(true) &&
          player.HasGUID && player.Actor != Default.Actor && (continent.GUID(player.VehicleSeated) match {
          case Some(o: Vehicle) => o.HasGUID && o.Actor != Default.Actor && !o.Destroyed
          case _ => true
        })
      ) {
        if (!setAvatar || waitingOnUpstream) {
          setCurrentAvatarFunc(player)
          respawnTimer = context.system.scheduler.scheduleOnce(
            delay = (if (attempt <= max_attempts / 2) 10
            else 5) seconds,
            context.self,
            SessionActor.SetCurrentAvatar(player, max_attempts, attempt + max_attempts / 3)
          )
        } else {
          sessionData.keepAliveFunc = sessionData.vehicles.GetMountableAndSeat(None, player, continent) match {
            case (Some(v: Vehicle), Some(seatNumber))
              if seatNumber > 0 && v.WeaponControlledFromSeat(seatNumber).isEmpty =>
              sessionData.KeepAlivePersistence
            case _ =>
              NormalKeepAlive
          }
          nextSpawnPoint = None
        }
        //if not the condition above, player has started playing normally
      } else {
        respawnTimer = context.system.scheduler.scheduleOnce(
          500 milliseconds,
          context.self,
          SessionActor.SetCurrentAvatar(player, max_attempts, attempt + 1)
        )
      }
    }
  }

  /**
   * Instruct the client to treat this player as the avatar.
   * Initialize all client-specific data that is dependent on some player being declared the "avatar".
   *
   * @param tplayer the target player
   */
  def HandleSetCurrentAvatar(tplayer: Player): Unit = {
    log.trace(s"HandleSetCurrentAvatar - ${tplayer.Name}")
    session = session.copy(player = tplayer)
    val guid = tplayer.GUID
    sessionData.UpdateDeployableUIElements(Deployables.InitializeDeployableUIElements(avatar))
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 75, 0))
    sendResponse(SetCurrentAvatarMessage(guid, 0, 0))
    sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, wideContents=true, "", "1 on", None)) //CC on //TODO once per respawn?
    val pos = player.Position = shiftPosition.getOrElse(tplayer.Position)
    val orient = player.Orientation = shiftOrientation.getOrElse(tplayer.Orientation)
    sendResponse(PlayerStateShiftMessage(ShiftState(1, pos, orient.z)))
    shiftPosition = None
    shiftOrientation = None
    if (player.spectator) {
      sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, wideContents=false, "", "on", None))
    }
    if (player.Jammed) {
      //TODO something better than just canceling?
      player.Actor ! JammableUnit.ClearJammeredStatus()
      player.Actor ! JammableUnit.ClearJammeredSound()
    }
    val originalDeadState = deadState
    deadState = DeadState.Alive
    if (originalDeadState != DeadState.Alive) {
      avatarActor ! AvatarActor.ResetImplants()
    }

    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 82, 0))
    //TODO if Medkit does not have shortcut, add to a free slot or write over slot 64
    avatar.shortcuts
      .zipWithIndex
      .collect { case (Some(shortcut), index) =>
        sendResponse(CreateShortcutMessage(
          guid,
          index + 1,
          Some(AvatarShortcut.convert(shortcut))
        ))
      }
    sendResponse(ChangeShortcutBankMessage(guid, 0))
    //Favorites lists
    avatarActor ! AvatarActor.InitialRefreshLoadouts()

    sendResponse(
      SetChatFilterMessage(ChatChannel.Platoon, origin = false, ChatChannel.values.toList)
    ) //TODO will not always be "on" like this
    sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, tplayer.Position, player.Faction, unk5 = true))
    //looking for squad (members)
    if (tplayer.avatar.lookingForSquad || sessionData.lfsm) {
      sendResponse(PlanetsideAttributeMessage(guid, 53, 1))
      continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.PlanetsideAttribute(guid, 53, 1))
    }
    sendResponse(AvatarSearchCriteriaMessage(guid, List(0, 0, 0, 0, 0, 0)))
    //these are facilities and towers and bunkers in the zone, but not necessarily all of them for some reason
    //for standard zones, facilities are 1, towers and bunkers are 0
    //for standard zone facilities in a position for valid vehicle gate shield benefits, 1 activates that shield
    //for caverns, who knows what this does
    //why is this all set in bulk?
    continent.Buildings
      .filter { case (_, building) =>
        val buildingType = building.BuildingType
        buildingType == StructureType.Facility ||
          buildingType == StructureType.Tower ||
          buildingType == StructureType.Bunker
      }
      .foreach { case (_, building) =>
        sendResponse(PlanetsideAttributeMessage(building.GUID, 67, 0 /*building.BuildingType == StructureType.Facility*/))
      }
    (0 to 30).foreach(_ => {
      //TODO 30 for a new character only?
      sendResponse(AvatarStatisticsMessage(2, Statistics(0L)))
    })
    if (tplayer.ExoSuit == ExoSuitType.MAX) {
      sendResponse(PlanetsideAttributeMessage(guid, 7, tplayer.Capacitor.toLong))
    }
    // AvatarAwardMessage
    //populateAvatarAwardRibbonsFunc(1, 20L)

    sendResponse(PlanetsideStringAttributeMessage(guid, 0, "Outfit Name"))
    //squad stuff (loadouts, assignment)
    sessionData.squadResponseHandlers.squadSetup()
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
    sessionData.vehicles.GetVehicleAndSeat() match {
      case (Some(vehicle), _) if vehicle.Definition == GlobalDefinitions.droppod =>
        //we're falling
        sendResponse(
          DroppodFreefallingMessage(
            vehicle.GUID,
            vehicle.Position,
            Vector3.z(value = -999),
            vehicle.Position + Vector3(-20, 1.156f, -50),
            Vector3(0, 70.3125f, 90),
            Vector3(0, 0, 90)
          )
        )
      case (Some(vehicle), Some(0)) =>
        //driver; summon any passengers and cargo vehicles left behind on previous continent
        if (vehicle.Jammed) {
          //TODO something better than just canceling?
          vehicle.Actor ! JammableUnit.ClearJammeredStatus()
          vehicle.Actor ! JammableUnit.ClearJammeredSound()
        }
        //positive shield strength
        if (vehicle.Definition.MaxShields > 0) {
          sendResponse(PlanetsideAttributeMessage(vehicle.GUID, vehicle.Definition.shieldUiAttribute, vehicle.Shields))
        }
        // ANT capacitor
        if (vehicle.Definition == GlobalDefinitions.ant) {
          sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, vehicle.NtuCapacitorScaled)) // set ntu on vehicle UI
        }
        // vehicle capacitor
        if (vehicle.Definition.MaxCapacitor > 0) {
          sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 113, vehicle.Capacitor))
        }
        sessionData.zoning.LoadZoneTransferPassengerMessages(
          guid,
          continent.id,
          vehicle
        )
      case (Some(vehicle), _) =>
        //passenger
        vehicle.Actor ! Vehicle.UpdateZoneInteractionProgressUI(player)
      case _ => ;
    }
    sessionData.zoning.interstellarFerryTopLevelGUID = None
    if (sessionData.zoning.loadConfZone && sessionData.connectionState == 100) {
      sessionData.zoning.configZone(continent)
      sessionData.zoning.loadConfZone = false
    }
    if (noSpawnPointHere) {
      sessionData.zoning.RequestSanctuaryZoneSpawn(player, continent.Number)
    } else if (originalDeadState == DeadState.Dead || player.Health == 0) {
      //killed during spawn setup or possibly a relog into a corpse (by accident?)
      player.Actor ! Player.Die()
    }
    AvatarActor.savePlayerData(player)
    sessionData.displayCharSavedMsgThenRenewTimer(
      Config.app.game.savedMsg.short.fixed,
      Config.app.game.savedMsg.short.variable
    )
    upstreamMessageCount = 0
    setAvatar = true
  }

  /**
   * Instruct the client to treat this player as the avatar.
   *
   * @see `SetCurrentAvatar`
   * @param tplayer the target player
   */
  def SetCurrentAvatarNormally(tplayer: Player): Unit = {
    HandleSetCurrentAvatar(tplayer)
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
  def RedrawDeployableIcons(obj: Deployable): Unit = {
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
  def DontRedrawIcons(obj: Deployable): Unit = {}

  /**
   * Make this client display the deployment map, and all its available destination spawn points.
   * @see `AvatarDeadStateMessage`
   * @see `DeadState.Release`
   * @see `Player.Release`
   */
  def GoToDeploymentMap(): Unit = {
    deadState = DeadState.Release //we may be alive or dead, may or may not be a corpse
    sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, unk5=true))
    DrawCurrentAmsSpawnPoint()
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
          sendResponse(
            BindPlayerMessage(
              BindStatus.Available,
              "@ams",
              display_icon=true,
              logging=false,
              SpawnGroup.AMS,
              continent.Number,
              5,
              tube.Position
            )
          )
        case None =>
          sendResponse(
            BindPlayerMessage(
              BindStatus.Unavailable,
              "@ams",
              display_icon=false,
              logging=false,
              SpawnGroup.AMS,
              continent.Number,
              0,
              Vector3.Zero
            )
          )
      }
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
    if (player != null && player.HasGUID && player.GUID == guid && player.Zone == continent) {
      sessionData.turnCounterFunc = NormalTurnCounter
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
   * In the case that the transitioning player is seated in a vehicle mount
   * that is not the driver and does not have a mounted weapon under its control,
   * no obvious feedback will be provided by the client.
   * For example, when as infantry, a `PlayerStateMessageUpstream` packet is dispatched by the client.
   * For example, when in the driver mount, a `VehicleStateMessage` is dispatched by the client.
   * In the given case, the only packet that indicates the player is seated is a `KeepAliveMessage`.
   * Detection of this `KeepALiveMessage`, for the purpose of transitioning logic,
   * can not be instantaneous to the zoning process or other checks for proper zoning conditions that will be disrupted.
   * To avoid complications, the player in such a mount is initially spawned as infantry on their own client,
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
          sessionData.AccessContainer(vehicle)
          sessionData.keepAliveFunc = sessionData.KeepAlivePersistence
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
   * The upstream counter accumulates when the server receives specific messages from the client.<br>
   * <br>
   * This accumulator is assigned after a login event.
   * The main purpose is to display any messages to the client regarding
   * if their previous log-out location and their current log-in location are different.
   * Hereafter, the normal accumulator will be referenced.
   * @param guid the player's globally unique identifier number
   */
  def TurnCounterLogin(guid: PlanetSideGUID): Unit = {
    NormalTurnCounter(guid)
    sendResponse(ChatMsg(sessionData.zoning.zoningChatMessageType, wideContents=false, "", loginChatMessage, None))
    sessionData.zoning.CancelZoningProcess()
    loginChatMessage = ""
    turnCounterFunc = NormalTurnCounter
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
   * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
   *
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
    val msgs@(attachMessage, mountPointStatusMessage) = CarrierBehavior.CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForUs(attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
   * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
   *
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
}
