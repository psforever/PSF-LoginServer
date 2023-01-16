// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Success
//
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.objects.avatar.{Avatar, DeployableToolbox}
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem, TelepadLike}
import net.psforever.objects.definition.SpecialExoSuitDefinition
import net.psforever.objects.definition.converter.DestroyedVehicleConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.guid.{GUIDTask, StraightforwardTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.shuttle.OrbitalShuttlePad
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vehicles._
import net.psforever.objects.zones.{Zone, ZoneHotSpotProjector, Zoning}
import net.psforever.objects._
import net.psforever.packet.game.objectcreate.{DroppedItemData, ObjectCreateMessageParent, PlacementData}
import net.psforever.packet.game.{AvatarDeadStateMessage, BeginZoningMessage, BroadcastWarpgateUpdateMessage, ChatMsg, ContinentalLockUpdateMessage, DeadState, DensityLevelUpdateMessage, DeployRequestMessage, DeployableInfo, DeployableObjectsInfoMessage, DeploymentAction, DisconnectMessage, DroppodError, DroppodLaunchResponseMessage, FriendsResponse, GenericObjectActionMessage, GenericObjectStateMsg, HotSpotUpdateMessage, ObjectAttachMessage, ObjectCreateMessage, PlanetsideAttributeEnum, PlanetsideAttributeMessage, PropertyOverrideMessage, ReplicationStreamMessage, SetEmpireMessage, TimeOfDayMessage, TriggerEffectMessage, WarpgateRequest, ZoneForcedCavernConnectionsMessage, ZoneInfoMessage, ZoneLockInfoMessage, ZonePopulationUpdateMessage, HotSpotInfo => PacketHotSpotInfo}
import net.psforever.packet.{PlanetSideGamePacket, game}
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.hart.HartTimer
import net.psforever.services.local.support.HackCaptureActor
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.properties.PropertyOverrideManager
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{CavernRotationService, Service, ServiceManager, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.zones.Zones

object ZoningOperations {
  private final val zoningCountdownMessages: Seq[Int] = Seq(5, 10, 20)
}

class ZoningOperations(
                        val sessionData: SessionData,
                        avatarActor: typed.ActorRef[AvatarActor.Command],
                        galaxyService: ActorRef,
                        cluster: typed.ActorRef[ICS.Command],
                        implicit val context: ActorContext
                      ) extends CommonSessionInterfacingFuncs {
  var zoningType: Zoning.Method = Zoning.Method.None
  var zoningChatMessageType: ChatMessageType = ChatMessageType.CMT_QUIT
  var zoningStatus: Zoning.Status = Zoning.Status.None
  var zoningCounter: Int = 0
  var instantActionFallbackDestination: Option[Zoning.InstantAction.Located] = None
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
  var loadConfZone: Boolean = false
  /** a flag for the zone having finished loading during zoning
   * `None` when no zone is loaded
   * `Some(true)` when a zone has successfully loaded
   * `Some(false)` when the loading process has failed or was executed but did not complete for some reason
   */
  var zoneLoaded: Option[Boolean] = None
  /** a flag that forces the current zone to reload itself during a zoning operation */
  var zoneReload: Boolean = false
  var zoningTimer: Cancellable = Default.Cancellable

  /* */

  def handleWarpgateRequest(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case WarpgateRequest(_, building_guid, destinationBuildingGuid, destinationZoneGuid, _, _) =>
        CancelZoningProcessWithDescriptiveReason("cancel_use")
        if (sessionData.spawn.deadState != DeadState.RespawnTime) {
          continent.Buildings.values.find(_.GUID == building_guid) match {
            case Some(wg: WarpGate) if wg.Active && (sessionData.vehicles.GetKnownVehicleAndSeat() match {
              case (Some(vehicle), _) =>
                wg.Definition.VehicleAllowance && !wg.Definition.NoWarp.contains(vehicle.Definition)
              case _ =>
                true
            }) =>
              sessionData.spawn.deadState = DeadState.RespawnTime
              cluster ! ICS.GetSpawnPoint(
                destinationZoneGuid.guid,
                player,
                destinationBuildingGuid,
                continent.Number,
                building_guid,
                context.self
              )
              log.info(s"${player.Name} wants to use a warp gate")

            case Some(wg: WarpGate) if !wg.Active =>
              log.warn(s"WarpgateRequest: ${player.Name} is knocking on an inactive warp gate")

            case _ =>
              sessionData.spawn.deadState = DeadState.RespawnTime
              RequestSanctuaryZoneSpawn(player, continent.Number)
          }
        } else {
          log.debug(s"WarpgateRequest: your request was already consumed, ${player.Name}; already working on it ...")
        }
      case _ => ;
    }
  }

  def handleZonesResponse(zones: Iterable[Zone], propertyOverrideManager: ActorRef): Unit = {
    zones.foreach { zone =>
      val continentNumber = zone.Number
      val popBO = 0
      //TODO black ops test (partition)
      val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
      val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
      val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)

      zone.Buildings.foreach({ case (_, building) => initBuilding(continentNumber, building.MapId, building) })
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
      val isCavern = continent.map.cavern
      sendResponse(ZoneInfoMessage(continentNumber, empire_status=true, if (isCavern) {
        Int.MaxValue.toLong
      } else {
        0L
      }))
      sendResponse(ZoneLockInfoMessage(continentNumber, lock_status=false, unk=true))
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
    ServiceManager.receptionist ! Receptionist.Find(
      CavernRotationService.CavernRotationServiceKey,
      context.self
    )
    LivePlayerList.Add(avatar.id, avatar)
    galaxyService.tell(GalaxyServiceMessage(GalaxyAction.LogStatusChange(avatar.name)), context.parent)
    //PropertyOverrideMessage

    implicit val timeout: Timeout = Timeout(1 seconds)
    val future = ask(propertyOverrideManager, PropertyOverrideManager.GetOverridesMessage)
      .mapTo[List[PropertyOverrideMessage.GamePropertyScope]]
    val overrides = Await.result(future, 1 second)

    sendResponse(PropertyOverrideMessage(overrides))

    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks
    sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
    (
      FriendsResponse.packetSequence(
        MemberAction.InitializeFriendList,
        avatar.people.friend
          .map { f =>
            game.Friend(f.name, AvatarActor.onlineIfNotIgnoredEitherWay(avatar, f.name))
          }
      ) ++
        //ignored list (no one ever online)
        FriendsResponse.packetSequence(
          MemberAction.InitializeIgnoreList,
          avatar.people.ignored.map { f => game.Friend(f.name) }
        )
      ).foreach {
      sendResponse
    }
    //the following subscriptions last until character switch/logout
    galaxyService ! Service.Join("galaxy") //for galaxy-wide messages
    galaxyService ! Service.Join(s"${avatar.faction}") //for hotspots, etc.
    sessionData.squadService ! Service.Join(s"${avatar.faction}") //channel will be player.Faction
    sessionData.squadService ! Service.Join(s"${avatar.id}") //channel will be player.CharId (in order to work with packets)
    player.Zone match {
      case Zone.Nowhere =>
        RandomSanctuarySpawnPosition(player)
        RequestSanctuaryZoneSpawn(player, currentZone = 0)
      case zone =>
        log.trace(s"ZoneResponse: zone ${zone.id} will now load for ${player.Name}")
        loadConfZone = true
        val oldZone = continent
        session = session.copy(zone = zone)
        //the only zone-level event system subscription necessary before BeginZoningMessage (for persistence purposes)
        zone.AvatarEvents ! Service.Join(player.Name)
        sessionData.persist()
        oldZone.AvatarEvents ! Service.Leave()
        oldZone.LocalEvents ! Service.Leave()
        oldZone.VehicleEvents ! Service.Leave()

        if (player.isAlive && zoningType != Zoning.Method.Reset) {
          if (player.HasGUID) {
            sessionData.spawn.handleNewPlayerLoaded(player)
          } else {
            //alive but doesn't have a GUID; probably logging in?
            session = session.copy(zone = Zone.Nowhere)
            context.self ! ICS.ZoneResponse(Some(player.Zone))
          }
        } else {
          zoneReload = true
          cluster ! ICS.GetNearbySpawnPoint(
            zone.Number,
            player,
            Seq(SpawnGroup.Facility, SpawnGroup.Tower),
            context.self
          )
        }
    }
  }

  def handleZoneResponse(foundZone: Zone): Unit = {
    log.trace(s"ZoneResponse: zone ${foundZone.id} will now load for ${player.Name}")
    loadConfZone = true
    val oldZone = session.zone
    session = session.copy(zone = foundZone)
    sessionData.persist()
    oldZone.AvatarEvents ! Service.Leave()
    oldZone.LocalEvents ! Service.Leave()
    oldZone.VehicleEvents ! Service.Leave()
    //the only zone-level event system subscription necessary before BeginZoningMessage (for persistence purposes)
    foundZone.AvatarEvents ! Service.Join(player.Name)
    foundZone.Population ! Zone.Population.Join(avatar)
    player.avatar = avatar
    interstellarFerry match {
      case Some(vehicle) if vehicle.PassengerInSeat(player).contains(0) =>
        TaskWorkflow.execute(sessionData.registerDrivenVehicle(vehicle, player))
      case _ =>
        TaskWorkflow.execute(sessionData.registerNewAvatar(player))
    }
  }

  def handleSpawnPointResponse(response: Option[(Zone, SpawnPoint)]): Unit = {
    zoningType match {
      case Zoning.Method.InstantAction if response.isEmpty =>
        CancelZoningProcessWithReason("@InstantActionNoHotspotsAvailable")

      case Zoning.Method.InstantAction if zoningStatus == Zoning.Status.Request =>
        beginZoningCountdown(() => {
          cluster ! ICS.GetInstantActionSpawnPoint(player.Faction, context.self)
        })

      case Zoning.Method.Reset =>
        player.ZoningRequest = Zoning.Method.Login
        zoningType = Zoning.Method.Login
        response match {
          case Some((zone, spawnPoint)) =>
            sessionData.spawn.loginChatMessage = "@login_reposition_to_friendly_facility" //Your previous location was held by the enemy. You have been moved to the nearest friendly facility.
            val (pos, ori) = spawnPoint.SpecificPoint(player)
            sessionData.spawn.LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
          case _ =>
            sessionData.spawn.loginChatMessage = "@login_reposition_to_sanctuary" //Your previous location was held by the enemy.  As there were no operational friendly facilities on that continent, you have been brought back to your Sanctuary.
            RequestSanctuaryZoneSpawn(player, player.Zone.Number)
        }

      case Zoning.Method.Login =>
        sessionData.spawn.resolveZoningSpawnPointLoad(response, Zoning.Method.Login)

      case ztype =>
        if (ztype != Zoning.Method.None) {
          log.warn(
            s"SpawnPointResponse: ${player.Name}'s zoning was not in order at the time a response was received; attempting to guess what ${player.Sex.pronounSubject} wants to do"
          )
        }
        val previousZoningType = ztype
        CancelZoningProcess()
        sessionData.PlayerActionsToCancel()
        sessionData.terminals.CancelAllProximityUnits()
        sessionData.DropSpecialSlotItem()
        continent.Population ! Zone.Population.Release(avatar)
        sessionData.spawn.resolveZoningSpawnPointLoad(response, previousZoningType)
    }
  }

  def handleTransferPassenger(
                               temp_channel: String,
                               vehicle: Vehicle,
                               manifest: VehicleManifest
                             ): Unit = {
    val playerName = player.Name
    log.debug(s"TransferPassenger: $playerName received the summons to transfer to ${vehicle.Zone.id} ...")
    (manifest.passengers.find {
      _.name.equals(playerName)
    } match {
      case Some(entry) if vehicle.Seats(entry.mount).occupant.isEmpty =>
        player.VehicleSeated = None
        vehicle.Seats(entry.mount).mount(player)
        player.VehicleSeated = vehicle.GUID
        Some(vehicle)
      case Some(entry) if vehicle.Seats(entry.mount).occupant.contains(player) =>
        Some(vehicle)
      case Some(entry) =>
        log.warn(
          s"TransferPassenger: $playerName tried to mount seat ${entry.mount} during summoning, but it was already occupied, and ${player.Sex.pronounSubject} was rebuked"
        )
        None
      case None =>
        //log.warn(s"TransferPassenger: $playerName is missing from the manifest of a summoning ${vehicle.Definition.Name} from ${vehicle.Zone.id}")
        None
    }).orElse {
      manifest.cargo.find {
        _.name.equals(playerName)
      } match {
        case Some(entry) =>
          vehicle.CargoHolds(entry.mount).occupant match {
            case out@Some(cargo) if cargo.Seats(0).occupants.exists(_.Name.equals(playerName)) =>
              out
            case _ =>
              None
          }
        case None =>
          None
      }
    } match {
      case Some(v: Vehicle) =>
        galaxyService ! Service.Leave(Some(temp_channel)) //temporary vehicle-specific channel (see above)
        sessionData.spawn.deadState = DeadState.Release
        sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, unk5=true))
        interstellarFerry = Some(v) //on the other continent and registered to that continent's GUID system
        sessionData.spawn.LoadZonePhysicalSpawnPoint(v.Continent, v.Position, v.Orientation, 1 seconds, None)
      case _ =>
        interstellarFerry match {
          case None =>
            galaxyService ! Service.Leave(Some(temp_channel)) //no longer being transferred between zones
            interstellarFerryTopLevelGUID = None
          case Some(_) => ;
          //wait patiently
        }
    }
  }

  def handleDroppodLaunchDenial(errorCode: DroppodError): Unit = {
    sendResponse(DroppodLaunchResponseMessage(errorCode, player.GUID))
  }

  def handleBeginZoning(pkt: PlanetSideGamePacket)(implicit serviceManager: ActorRef): Unit = {
    pkt match {
      case BeginZoningMessage() =>
        log.trace(s"BeginZoningMessage: ${player.Name} is reticulating ${continent.id}'s splines ...")
        zoneLoaded = None
        val name = avatar.name
        val continentId    = continent.id
        val faction        = player.Faction
        val factionChannel = s"$faction"
        continent.AvatarEvents ! Service.Join(continentId)
        continent.AvatarEvents ! Service.Join(factionChannel)
        continent.LocalEvents ! Service.Join(name)
        continent.LocalEvents ! Service.Join(continentId)
        continent.LocalEvents ! Service.Join(factionChannel)
        continent.VehicleEvents ! Service.Join(name)
        continent.VehicleEvents ! Service.Join(continentId)
        continent.VehicleEvents ! Service.Join(factionChannel)
        if (sessionData.connectionState != 100) configZone(continent)
        sendResponse(TimeOfDayMessage(1191182336))
        //custom
        sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty))    //clear squad list
        sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks

        //find and reclaim own deployables, if any
        val foundDeployables =
          continent.DeployableList.filter(obj => obj.OwnerName.contains(player.Name) && obj.Health > 0)
        foundDeployables.foreach(obj => {
          if (avatar.deployables.AddOverLimit(obj)) {
            obj.Actor ! Deployable.Ownership(player)
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
            .map(_.occupant)
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
            sendResponse(TriggerEffectMessage(obj.GUID, "on", unk1=true, 1000))
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
          sessionData.spawn.DepictPlayerAsCorpse
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
                log.warn(
                  s"BeginZoningMessage: ${player.Name} thought ${player.Sex.pronounSubject} was sitting in a vehicle, but it just evaporated around ${player.Sex.pronounObject}"
                )
                player.VehicleSeated = None
                (b, List.empty[Vehicle])
              case None =>
                player.VehicleSeated = None
                (b, List.empty[Vehicle])
            }
          )
        }
        val allActiveVehicles = vehicles ++ usedVehicle
        //active vehicles (and some wreckage)
        vehicles.foreach { vehicle =>
          val vguid       = vehicle.GUID
          val vdefinition = vehicle.Definition
          sendResponse(
            ObjectCreateMessage(vdefinition.ObjectId, vguid, vdefinition.Packet.ConstructorData(vehicle).get)
          )
          //occupants other than driver (with exceptions)
          vehicle.Seats
            .filter {
              case (index, seat) =>
                seat.isOccupied &&
                  live.contains(seat.occupant.get) &&
                  (vehicle.Definition match {
                    case GlobalDefinitions.orbital_shuttle | GlobalDefinitions.droppod => true
                    case _                                                             => index > 0
                  })
            }
            .foreach {
              case (index, seat) =>
                val targetPlayer    = seat.occupant.get
                val targetDefiniton = targetPlayer.avatar.definition
                sendResponse(
                  ObjectCreateMessage(
                    targetDefiniton.ObjectId,
                    targetPlayer.GUID,
                    ObjectCreateMessageParent(vguid, index),
                    targetDefiniton.Packet.ConstructorData(targetPlayer).get
                  )
                )
            }
          vehicle.SubsystemMessages().foreach { sendResponse }
        }
        vehicles.collect {
          case vehicle if vehicle.Faction == faction =>
            Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        }
        //our vehicle would have already been loaded; see NewPlayerLoaded/AvatarCreate
        usedVehicle.headOption match {
          case Some(vehicle) =>
            //subsystems
            vehicle.Actor ! Vehicle.UpdateSubsystemStates(player.Name, Some(false))
            //depict any other passengers already in this zone
            val vguid = vehicle.GUID
            vehicle.Seats
              .filter {
                case (index, seat) =>
                  seat.isOccupied &&
                    !seat.occupant.contains(player) &&
                    live.contains(seat.occupant.get) &&
                    (vehicle.Definition match {
                      case GlobalDefinitions.orbital_shuttle => true
                      case _                                 => index > 0
                    })
              }
              .foreach {
                case (index, seat) =>
                  val targetPlayer     = seat.occupant.get
                  val targetDefinition = targetPlayer.avatar.definition
                  sendResponse(
                    ObjectCreateMessage(
                      targetDefinition.ObjectId,
                      targetPlayer.GUID,
                      ObjectCreateMessageParent(vguid, index),
                      targetDefinition.Packet.ConstructorData(targetPlayer).get
                    )
                  )
              }
            //since we would have only subscribed recently, we need to reload mount access states
            (0 to 3).foreach { group =>
              sendResponse(PlanetsideAttributeMessage(vguid, group + 10, vehicle.PermissionGroup(group).get.id))
            }
            //positive shield strength
            if (vehicle.Shields > 0) {
              sendResponse(PlanetsideAttributeMessage(vguid, vehicle.Definition.shieldUiAttribute, vehicle.Shields))
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
            vehicle.CargoHolds.collect {
              case (_index, hold: Cargo) if hold.isOccupied =>
                CarrierBehavior.CargoMountBehaviorForAll(
                  vehicle,
                  hold.occupant.get,
                  _index
                ) //CargoMountBehaviorForUs can fail to attach the cargo vehicle on some clients
            }
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
          sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deploying, 0, unk3=false, Vector3.Zero))
          sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deployed, 0, unk3=false, Vector3.Zero))
          sessionData.ToggleTeleportSystem(obj, TelepadLike.AppraiseTeleportationSystem(obj, continent))
        }
        serviceManager
          .ask(Lookup("hart"))(Timeout(2 seconds))
          .onComplete {
            case Success(LookupResult("hart", ref)) =>
              ref ! HartTimer.Update(continentId, name)
            case _ =>
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
            //mount terminal occupants
            continent.GUID(terminal_guid) match {
              case Some(obj: Mountable) =>
                obj.Seats(0).occupant match {
                  case Some(targetPlayer: Player) =>
                    val targetDefinition = targetPlayer.avatar.definition
                    sendResponse(
                      ObjectCreateMessage(
                        targetDefinition.ObjectId,
                        targetPlayer.GUID,
                        ObjectCreateMessageParent(parent_guid, 0),
                        targetDefinition.Packet.ConstructorData(targetPlayer).get
                      )
                    )
                  case _ => ;
                }
              case _ => ;
            }
        })
        //facility turrets
        continent.map.turretToWeapon
          .map { case (turret_guid: Int, _) => continent.GUID(turret_guid) }
          .collect {
            case Some(turret: FacilityTurret) =>
              val pguid = turret.GUID
              //attached weapon
              if (!turret.isUpgrading) {
                turret.ControlledWeapon(wepNumber = 1).foreach {
                  case obj: Tool =>
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
              //mount turret occupant
              turret.Seats(0).occupant match {
                case Some(targetPlayer: Player) =>
                  val targetDefinition = targetPlayer.avatar.definition
                  sendResponse(
                    ObjectCreateMessage(
                      targetDefinition.ObjectId,
                      targetPlayer.GUID,
                      ObjectCreateMessageParent(pguid, 0),
                      targetDefinition.Packet.ConstructorData(targetPlayer).get
                    )
                  )
                case _ => ;
              }
          }
        //remote projectiles and radiation clouds
        continent.Projectiles.foreach { projectile =>
          val definition = projectile.Definition
          sendResponse(
            ObjectCreateMessage(
              definition.ObjectId,
              projectile.GUID,
              definition.Packet.ConstructorData(projectile).get
            )
          )
        }
        //spawn point update request
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.UpdateAmsSpawnPoint(continent)
        )
        sessionData.spawn.upstreamMessageCount = 0
        zoneLoaded = Some(true)
      case _ => ;
    }
  }

  def handleRecall(): Unit = {
    player.ZoningRequest = Zoning.Method.Recall
    zoningType = Zoning.Method.Recall
    zoningChatMessageType = ChatMessageType.CMT_RECALL
    zoningStatus = Zoning.Status.Request
    beginZoningCountdown(() => {
      cluster ! ICS.GetRandomSpawnPoint(
        Zones.sanctuaryZoneNumber(player.Faction),
        player.Faction,
        Seq(SpawnGroup.Sanctuary),
        context.self
      )
    })
  }

  def handleInstantAction(): Unit = {
    player.ZoningRequest = Zoning.Method.InstantAction
    zoningType = Zoning.Method.InstantAction
    zoningChatMessageType = ChatMessageType.CMT_INSTANTACTION
    zoningStatus = Zoning.Status.Request
    cluster ! ICS.GetInstantActionSpawnPoint(player.Faction, context.self)
  }

  def handleQuit(): Unit = {
    //priority is given to quit over other zoning methods
    if (session.zoningType == Zoning.Method.InstantAction || session.zoningType == Zoning.Method.Recall) {
      CancelZoningProcessWithDescriptiveReason("cancel")
    }
    player.ZoningRequest = Zoning.Method.Quit
    zoningType = Zoning.Method.Quit
    zoningChatMessageType = ChatMessageType.CMT_QUIT
    zoningStatus = Zoning.Status.Request
    beginZoningCountdown(() => {
      log.info(s"Good-bye, ${player.Name}")
      sessionData.ImmediateDisconnect()
    })
  }

  def handleSetZone(zoneId: String, position: Vector3): Unit = {
    if (sessionData.vehicles.serverVehicleControlVelocity.isEmpty) {
      sessionData.PlayerActionsToCancel()
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
          vehicle.PassengerInSeat(player) match {
            case Some(0) =>
              sessionData.spawn.deadState = DeadState.Release // cancel movement updates
              vehicle.Position = position
              sessionData.spawn.LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds, None)
            case _ => // not seated as the driver, in which case we can't move
          }
        case None =>
          sessionData.spawn.deadState = DeadState.Release // cancel movement updates
          player.Position = position
          // continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, player.GUID))
          sessionData.spawn.LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds, None)
        case _ => // seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
      }
    }
  }

  /* support code */

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
      sendResponse(ChatMsg(ChatMessageType.CMT_QUIT, wideContents=false, "", s"@${descriptor}_$origin", None))
      zoningTimer.cancel()
      zoningTimer = context.system.scheduler.scheduleOnce(5 seconds) {
        beginZoningCountdown(runnable)
      }
    } else if (zoningStatus == Zoning.Status.Countdown) {
      zoningCounter -= 5
      zoningTimer.cancel()
      if (zoningCounter > 0) {
        if (ZoningOperations.zoningCountdownMessages.contains(zoningCounter)) {
          sendResponse(ChatMsg(zoningChatMessageType, wideContents=false, "", s"@${descriptor}_$zoningCounter", None))
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
   *
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
   * The user no longer expects to perform a zoning event for this reason.
   *
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
   *                normally, this message uses the same value as `zoningChatMessageType`;
   *                defaults to `None`
   */
  def CancelZoningProcessWithReason(msg: String, msgType: Option[ChatMessageType] = None): Unit = {
    if (zoningStatus != Zoning.Status.None) {
      sendResponse(ChatMsg(msgType.getOrElse(zoningChatMessageType), wideContents=false, "", msg, None))
    }
    CancelZoningProcess()
  }

  /**
   * The user no longer expects to perform a zoning event,
   * or the process is merely resetting its internal state.
   */
  def CancelZoningProcess(): Unit = {
    zoningTimer.cancel()
    player.ZoningRequest = Zoning.Method.None
    zoningType = Zoning.Method.None
    zoningStatus = Zoning.Status.None
    zoningCounter = 0
    //instant action exclusive field
    instantActionFallbackDestination = None
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
        sendResponse(building.infoUpdateMessage())
        sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0, 0, 0, 0, 0, 0, 0, 0)))
        if (wg.Broadcast(player.Faction)) {
          sendResponse(
            BroadcastWarpgateUpdateMessage(
              continentNumber,
              buildingNumber,
              player.Faction
            )
          )
        }
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
        val warning = net.psforever.login.WorldSession.boolToInt(silo.LowNtuWarningOn)
        sendResponse(PlanetsideAttributeMessage(silo.Owner.GUID, 47, warning))
        if (silo.NtuCapacitor == 0) {
          sendResponse(PlanetsideAttributeMessage(silo.Owner.GUID, 48, 1))
        }
      case door: Door if door.isOpen =>
        sendResponse(GenericObjectStateMsg(amenityId, 16))

      case obj: Hackable if obj.HackedBy.nonEmpty =>
        //sync hack state
        amenity.Definition match {
          case GlobalDefinitions.capture_terminal =>
            sessionData.SendPlanetsideAttributeMessage(
              amenity.GUID,
              PlanetsideAttributeEnum.ControlConsoleHackUpdate,
              HackCaptureActor.GetHackUpdateAttributeValue(amenity.asInstanceOf[CaptureTerminal], isResecured = false)
            )
          case _ =>
            sessionData.HackObject(amenity.GUID, 1114636288L, 8L) //generic hackable object
        }

      // sync capture flags
      case llu: CaptureFlag =>
        // Create LLU
        sendResponse(
          ObjectCreateMessage(
            llu.Definition.ObjectId,
            llu.GUID,
            llu.Definition.Packet.ConstructorData(llu).get
          )
        )

        // Attach it to a player if it has a carrier
        if (llu.Carrier.nonEmpty) {
          continent.LocalEvents ! LocalServiceMessage(
            continent.id,
            LocalAction.SendPacket(ObjectAttachMessage(llu.Carrier.get.GUID, llu.GUID, 252))
          )
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
    log.debug(s"LoadZoneAsPlayer: ${targetPlayer.avatar.name} loading into $zoneId")
    if (!zoneReload && zoneId == continent.id) {
      if (player.isBackpack) { // important! test the actor-wide player ref, not the parameter
        // respawning from unregistered player
        TaskWorkflow.execute(sessionData.registerAvatar(targetPlayer))
      } else {
        // move existing player; this is the one case where the original GUID is retained by the player
        context.self ! SessionActor.PlayerLoaded(targetPlayer)
      }
    } else {
      LoadZoneCommonTransferActivity()
      val original = player
      if (player.isBackpack) {
        session = session.copy(player = targetPlayer)
        TaskWorkflow.execute(taskThenZoneChange(
          GUIDTask.unregisterObject(continent.GUID, original.avatar.locker),
          ICS.FindZone(_.id.equals(zoneId), context.self)
        ))
      } else if (player.HasGUID) {
        TaskWorkflow.execute(taskThenZoneChange(
          GUIDTask.unregisterAvatar(continent.GUID, original),
          ICS.FindZone(_.id.equals(zoneId), context.self)
        ))
      } else {
        cluster ! ICS.FindZone(_.id.equals(zoneId), context.self)
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
    val msg: String = s"${player.Name} is driving a ${vehicle.Definition.Name}"
    log.info(msg)
    log.debug(s"LoadZoneInVehicleAsDriver: $msg")
    val manifest  = vehicle.PrepareGatingManifest()
    val pguid     = player.GUID
    val toChannel = manifest.file
    val topLevel  = interstellarFerryTopLevelGUID.getOrElse(vehicle.GUID)
    continent.VehicleEvents ! VehicleServiceMessage(
      s"${vehicle.Actor}",
      VehicleAction.TransferPassengerChannel(pguid, s"${vehicle.Actor}", toChannel, vehicle, topLevel)
    )
    manifest.cargo.foreach {
      case ManifestPassengerEntry("MISSING_DRIVER", index) =>
        val cargo = vehicle.CargoHolds(index).occupant.get
        log.warn(
          s"LoadZoneInVehicleAsDriver: ${player.Name} must eject cargo in hold $index; vehicle is missing driver"
        )
        cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed = false)
      case entry =>
        val cargo = vehicle.CargoHolds(entry.mount).occupant.get
        continent.VehicleEvents ! VehicleServiceMessage(
          entry.name,
          VehicleAction.TransferPassengerChannel(pguid, s"${cargo.Actor}", toChannel, cargo, topLevel)
        )
    }
    //
    vehicle.allowInteraction = false
    if (!zoneReload && zoneId == continent.id) {
      if (vehicle.Definition == GlobalDefinitions.droppod) {
        //instant action droppod in the same zone
        TaskWorkflow.execute(registerDroppod(vehicle, player))
      } else {
        //transferring a vehicle between spawn points (warp gates) in the same zone
        context.self ! SessionActor.PlayerLoaded(player)
      }
    } else if (vehicle.Definition == GlobalDefinitions.droppod) {
      LoadZoneCommonTransferActivity()
      player.Continent = zoneId //forward-set the continent id to perform a test
      TaskWorkflow.execute(taskThenZoneChange(
        GUIDTask.unregisterAvatar(continent.GUID, player),
        ICS.FindZone(_.id == zoneId, context.self)
      ))
    } else {
      sessionData.UnaccessContainer(vehicle)
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zoneId //forward-set the continent id to perform a test
      interstellarFerryTopLevelGUID =
        if (manifest.passengers.isEmpty && manifest.cargo.count { !_.name.equals("MISSING_DRIVER") } == 0) {
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
      TaskWorkflow.execute(taskThenZoneChange(
        sessionData.unregisterDrivenVehicle(vehicle, player),
        ICS.FindZone(_.id == zoneId, context.self)
      ))
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
   * @see `GUIDTask.unregisterPlayer`
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
    val msg: String = s"${player.Name} is the passenger of a ${vehicle.Definition.Name}"
    log.info(msg)
    log.debug(s"LoadZoneInVehicleAsPassenger: $msg")
    if (!zoneReload && zoneId == continent.id) {
      //transferring a vehicle between spawn points (warp gates) in the same zone
      context.self ! SessionActor.PlayerLoaded(player)
    } else {
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zoneId //forward-set the continent id to perform a test
      interstellarFerryTopLevelGUID = None

      TaskWorkflow.execute(taskThenZoneChange(
        GUIDTask.unregisterAvatar(continent.GUID, player),
        ICS.FindZone(_.id == zoneId, context.self)
      ))
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
              val cargo = hold.occupant.get
              cargo.Continent = toZoneId
            //point to the cargo vehicle to instigate cargo vehicle driver transportation
            //              galaxyService ! GalaxyServiceMessage(
            //                toChannel,
            //                GalaxyAction.TransferPassenger(player_guid, toChannel, vehicle, topLevel, manifest)
            //              )
          }
      case None =>
        log.error(
          s"LoadZoneTransferPassengerMessages: ${player.Name} expected a manifest for zone transfer; got nothing"
        )
    }
  }

  /** Before changing zones, perform the following task (which can be a nesting of subtasks). */
  def taskThenZoneChange(
                          task: TaskBundle,
                          zoneMessage: ICS.FindZone
                        ): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        val localAvatar: Avatar = avatar
        val localZone: Zone = continent
        val localCluster: typed.ActorRef[ICS.Command] = cluster

        override def description() : String = s"doing ${task.description()} before transferring zones"

        def action(): Future[Any] = {
          localZone.Population ! Zone.Population.Leave(localAvatar)
          localCluster ! zoneMessage
          Future(true)
        }
      },
      task
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
          sessionData.vehicles.TotalDriverVehicleControl(vehicle)
          vehicle.Actor ! Vehicle.Ownership(None)
        case _ => ;
      }
      avatarActor ! AvatarActor.SetVehicle(None)
    }
    sessionData.RemoveBoomerTriggersFromInventory().foreach(obj => {
      TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, obj))
    })
    Deployables.Disown(continent, avatar, context.self)
    sessionData.spawn.drawDeloyableIcon = sessionData.spawn.RedrawDeployableIcons //important for when SetCurrentAvatar initializes the UI next zone
    sessionData.squadResponseHandlers.squadSetup = sessionData.squadResponseHandlers.ZoneChangeSquadSetup
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
      log.error(s"RequestSanctuaryZoneSpawn: ${player.Name} is already in faction sanctuary zone.")
      sendResponse(DisconnectMessage("RequestSanctuaryZoneSpawn: player is already in sanctuary."))
      sessionData.ImmediateDisconnect()
    } else {
      continent.GUID(player.VehicleSeated) match {
        case Some(obj: Vehicle) if !obj.Destroyed =>
          cluster ! ICS.GetRandomSpawnPoint(
            Zones.sanctuaryZoneNumber(player.Faction),
            player.Faction,
            Seq(SpawnGroup.WarpGate),
            context.self
          )
        case _ =>
          cluster ! ICS.GetRandomSpawnPoint(
            Zones.sanctuaryZoneNumber(player.Faction),
            player.Faction,
            Seq(SpawnGroup.Sanctuary),
            context.self
          )
      }
    }
  }

  /**
   * Attach the player to a droppod vehicle and hurtle them through the stratosphere in some far off world.
   * Perform all normal operation standardization (state cancels) as if any of form of zoning was being performed,
   * then assemble the vehicle and work around some inconvenient setup requirements for vehicle gating.
   * You can't instant action to respond to some activity using a droppod.
   *
   * @param zone          the destination zone
   * @param spawnPosition the destination drop position
   */
  def LoadZoneLaunchDroppod(zone: Zone, spawnPosition: Vector3): Unit = {
    log.info(s"${player.Name} is launching  to ${zone.id} in ${player.Sex.possessive} droppod")
    CancelZoningProcess()
    sessionData.PlayerActionsToCancel()
    sessionData.terminals.CancelAllProximityUnits()
    //droppod action
    val droppod = Vehicle(GlobalDefinitions.droppod)
    droppod.GUID = PlanetSideGUID(0) //droppod is not registered, we must jury-rig this
    droppod.Faction = player.Faction
    droppod.Position = spawnPosition.xy + Vector3.z(1024)
    droppod.Orientation = Vector3.z(180) //you always seems to land looking south; don't know why
    droppod.Seats(0).mount(player)
    droppod.Invalidate() //now, we must short-circuit the jury-rig
    interstellarFerry = Some(droppod) //leverage vehicle gating
    player.Position = droppod.Position
    player.VehicleSeated = PlanetSideGUID(0)
    sessionData.spawn.LoadZonePhysicalSpawnPoint(zone.id, droppod.Position, Vector3.Zero, 0 seconds, None)
  }

  def RandomSanctuarySpawnPosition(target: Player): Unit = {
    //xy-coordinates indicate spawn bias:
    val sanctuaryNum = Zones.sanctuaryZoneNumber(target.Faction)
    val harts = Zones.zones.find(zone => zone.Number == sanctuaryNum) match {
      case Some(zone) => zone.Buildings
        .values
        .filter(b => b.Amenities.exists { a: Amenity => a.isInstanceOf[OrbitalShuttlePad] })
        .toSeq
      case None =>
        Nil
    }
    //compass directions to modify spawn destination
    val directionBias = math.abs(scala.util.Random.nextInt() % avatar.name.hashCode % 8) match {
      case 0 => Vector3(-1, 1, 0) //NW
      case 1 => Vector3(0, 1, 0) //N
      case 2 => Vector3(1, 1, 0) //NE
      case 3 => Vector3(1, 0, 0) //E
      case 4 => Vector3(1, -1, 0) //SE
      case 5 => Vector3(0, -1, 0) //S
      case 6 => Vector3(-1, -1, 0) //SW
      case 7 => Vector3(-1, 0, 0) //W
    }
    if (harts.nonEmpty) {
      //get a hart building and select one of the spawn facilities surrounding it
      val campusLocation = harts(math.floor(math.abs(math.random()) * harts.size).toInt).Position
      target.Position = campusLocation + directionBias
    } else {
      //weird issue here; should we log?
      //select closest spawn point based on global cardinal or ordinal direction bias
      target.Position = directionBias * 8192f
    }
  }

  /**
   * Use this function to facilitate registering a droppod for a globally unique identifier
   * in the event that the user has instigated an instant action event to a destination within the current zone.<br>
   * <br>
   * If going to another zone instead,
   * this is unneccessary as the normal vehicle gating protocol is partially intersected for droppod operation,
   * and will properly register the droppod before introducing it into the new zone without additional concern.
   * The droppod should actually not be completely unregistered.
   * If inquired, it will act like a GUID had already been assigned to it, but it was invalidated.
   * This condition is artificial, but it necessary to pass certain operations related to vehicle gating.
   * Additionally, the driver is only partially associated with the vehicle at this time.
   * `interstellarFerry` is properly keeping track of the vehicle during the transition
   * and the user who is the driver (second param) is properly seated
   * but the said driver does not know about the vehicle through his usual convention - `VehicleSeated` - yet.
   * @see `GlobalDefinitions.droppod`
   * @see `GUIDTask.registerObject`
   * @see `interstellarFerry`
   * @see `Player.VehicleSeated`
   * @see `PlayerLoaded`
   * @see `TaskBundle`
   * @see `Vehicles.Own`
   * @param vehicle the unregistered droppod
   * @param tplayer the player using the droppod for instant action;
   *                should already be the driver of the droppod
   * @return a `TaskBundle` message
   */
  def registerDroppod(vehicle: Vehicle, tplayer: Player): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localDriver   = tplayer
        private val localVehicle  = vehicle
        private val localAnnounce = context.self

        override def description(): String = s"register a ${localVehicle.Definition.Name} manned by ${localDriver.Name}"

        def action(): Future[Any] = {
          localDriver.VehicleSeated = localVehicle.GUID
          Vehicles.Own(localVehicle, localDriver)
          localAnnounce ! SessionActor.PlayerLoaded(localDriver)
          Future(true)
        }
      },
      List(GUIDTask.registerObject(continent.GUID, vehicle))
    )
  }
}
