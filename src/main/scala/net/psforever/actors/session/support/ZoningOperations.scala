// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.login.WorldSession
import net.psforever.objects.avatar.{BattleRank, DeployableToolbox}
import net.psforever.objects.avatar.scoring.{CampaignStatistics, ScoreCard, SessionStatistics}
import net.psforever.objects.definition.converter.OCM
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.mount.Seat
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.auto.AutomatedTurret
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital.{InGameHistory, IncarnationActivity, ReconstructionActivity, SpawningActivity}
import net.psforever.packet.game.{CampaignStatistic, ChangeFireStateMessage_Start, HackState7, MailMessage, ObjectDetectedMessage, SessionStatistic, TriggeredSound}
import net.psforever.services.chat.DefaultChannel

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success
//
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.login.WorldSession.RemoveOldEquipmentFromInventory
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.avatar.{Award, AwardCategory, PlayerControl, Shortcut => AvatarShortcut}
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem, TelepadLike}
import net.psforever.objects.definition.SpecialExoSuitDefinition
import net.psforever.objects.definition.converter.{CorpseConverter, DestroyedVehicleConverter}
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
import net.psforever.packet.game.{AvatarAwardMessage, AvatarSearchCriteriaMessage, AvatarStatisticsMessage, AwardCompletion, BindPlayerMessage, BindStatus, CargoMountPointStatusMessage, ChangeShortcutBankMessage, ChatChannel, CreateShortcutMessage, DroppodFreefallingMessage, LoadMapMessage, ObjectCreateDetailedMessage, ObjectDeleteMessage, PlanetsideStringAttributeMessage, PlayerStateShiftMessage, SetChatFilterMessage, SetCurrentAvatarMessage, ShiftState}
import net.psforever.packet.game.{AvatarDeadStateMessage, BroadcastWarpgateUpdateMessage, ChatMsg, ContinentalLockUpdateMessage, DeadState, DensityLevelUpdateMessage, DeployRequestMessage, DeployableInfo, DeployableObjectsInfoMessage, DeploymentAction, DisconnectMessage, DroppodError, DroppodLaunchResponseMessage, FriendsResponse, GenericObjectActionMessage, GenericObjectStateMsg, HotSpotUpdateMessage, ObjectAttachMessage, ObjectCreateMessage, PlanetsideAttributeEnum, PlanetsideAttributeMessage, PropertyOverrideMessage, ReplicationStreamMessage, SetEmpireMessage, TimeOfDayMessage, TriggerEffectMessage, ZoneForcedCavernConnectionsMessage, ZoneInfoMessage, ZoneLockInfoMessage, ZonePopulationUpdateMessage, HotSpotInfo => PacketHotSpotInfo}
import net.psforever.packet.game.{BeginZoningMessage, DroppodLaunchRequestMessage, ReleaseAvatarRequestMessage, SpawnRequestMessage, WarpgateRequest}
import net.psforever.packet.game.DeathStatistic
import net.psforever.packet.game.objectcreate.{DroppedItemData, ObjectCreateMessageParent, PlacementData}
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.packet.{PlanetSideGamePacket, game}
import net.psforever.persistence.Savedplayer
import net.psforever.services.RemoverActor
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.account.{AccountPersistenceService, PlayerToken}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.hart.HartTimer
import net.psforever.services.local.support.HackCaptureActor
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.properties.PropertyOverrideManager
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{CavernRotationService, Service, ServiceManager, InterstellarClusterService => ICS}
import net.psforever.types._
import net.psforever.util.{Config, DefinitionUtil}
import net.psforever.zones.Zones

object ZoningOperations {
  private[session] final case class AvatarAwardMessageBundle(
                                                              bundle: Iterable[Iterable[PlanetSideGamePacket]],
                                                              delay: Long
                                                            )

  private final val zoningCountdownMessages: Seq[Int] = Seq(5, 10, 20)

  def reportProgressionSystem(sessionActor: ActorRef): Unit = {
    sessionActor ! SessionActor.SendResponse(
      MailMessage(
        "High Command",
        "Progress versus Promotion",
        "If you consider yourself as a veteran soldier, despite looking so green, please read this.\n" ++
          s"You only have this opportunity while you are less than or equal to battle rank ${Config.app.game.promotion.broadcastBattleRank}." ++
          "\n\n" ++
          "The normal method of rank advancement comes from the battlefield - fighting enemies, helping allies, and capturing facilities. " ++
          "\n\n" ++
          s"You may, however, rapidly promote yourself to at most battle rank ${Config.app.game.promotion.maxBattleRank}. " ++
          "You have access to all of the normal benefits, certification points, implants, etc., of your chosen rank. " ++
          "However, that experience that you have skipped will count as PROMOTION DEBT. " ++
          "You will not advance any further until you earn that experience back through support activity and engaging in facility capture. " ++
          "The amount of experience required and your own effort will determine how long it takes. " ++
          "In addition, you will be ineligible of having your command experience be recognized during this time." ++
          "\n\n" ++
          "If you wish to continue, set your desired battle rank now - use '!progress' followed by a battle rank index. " ++
          s"If you accept, but it becomes too much of burden, you may ask to revert to battle rank ${Config.app.game.promotion.resetBattleRank} at any time. " ++
          "Your normal sense of progress will be restored."
      )
    )
  }

  private def usingVehicleSpawnTubeAnimation(
                                              zone: Zone,
                                              side: Sidedness,
                                              faction: PlanetSideEmpire.Value,
                                              position: Vector3,
                                              orientation: Vector3,
                                              additionalChannels: List[String]
                                            ): Unit = {
    val events = zone.LocalEvents
    val effectMessage = LocalAction.TriggerEffectLocation(Service.defaultPlayerGUID, s"respawn_$faction", position, orientation)
    (zone
      .blockMap
      .sector(position, range = 100f)
      .livePlayerList
      .filter(p => Sidedness.equals(side, p.WhichSide))
      .map(_.Name) ++ additionalChannels)
      .foreach { target =>
        events ! LocalServiceMessage(target, effectMessage)
      }
  }

  private def usingFacilitySpawnTubeAnimation(
                                               zone: Zone,
                                               side: Sidedness,
                                               faction: PlanetSideEmpire.Value,
                                               position: Vector3,
                                               orientation: Vector3,
                                               additionalChannels: List[String]
                                             ): Unit = {
    val posxy = position.xy
    val posz = position.z
    val events = zone.LocalEvents
    val soundTargets = zone
      .blockMap
      .sector(position, range = 50f)
      .livePlayerList
    val effectTargets = soundTargets
      .filter { t =>
        val heightDiff = t.Position.z - posz
        Sidedness.equals(t.WhichSide, side) &&
          Vector3.DistanceSquared(t.Position.xy, posxy) < 2500f && /* literal 50m */
          heightDiff < 5f && heightDiff > -1f
      }
    val effectMessage = LocalAction.TriggerEffectLocation(Service.defaultPlayerGUID, s"respawn_$faction", position, orientation)
    (effectTargets.map(_.Name) ++ additionalChannels).foreach { target =>
      events ! LocalServiceMessage(target, effectMessage)
    }
    val soundMessage = LocalAction.TriggerSound(Service.defaultPlayerGUID, TriggeredSound.SpawnInTube, position, 50, 0.69803923f)
    (soundTargets.map(_.Name) ++ additionalChannels).foreach { target =>
      events ! LocalServiceMessage(target, soundMessage)
    }
  }

  def findBuildingsBySoiOccupancy(zone: Zone, position: Vector3): List[Building] = {
    val positionxy = position.xy
    zone
      .blockMap
      .sector(positionxy, range=5)
      .buildingList
      .filter { building =>
        val radius = building.Definition.SOIRadius
        Vector3.DistanceSquared(building.Position.xy, positionxy) < radius * radius
      }
  }
}

class ZoningOperations(
                        val sessionLogic: SessionData,
                        avatarActor: typed.ActorRef[AvatarActor.Command],
                        galaxyService: ActorRef,
                        cluster: typed.ActorRef[ICS.Command],
                        implicit val context: ActorContext
                      ) extends CommonSessionInterfacingFunctionality {
  private[session] var zoningStatus: Zoning.Status = Zoning.Status.None
  /** a flag for the zone having finished loading during zoning
   * `None` when no zone is loaded
   * `Some(true)` when a zone has successfully loaded
   * `Some(false)` when the loading process has failed or was executed but did not complete for some reason
   */
  private[session] var zoneLoaded: Option[Boolean] = None
  /**
   * used during zone transfers to maintain reference to seated vehicle (which does not yet exist in the new zone)
   * used during intrazone gate transfers, but not in a way distinct from prior zone transfer procedures
   * should only be set during the transient period when moving between one spawn point and the next
   * leaving set prior to a subsequent transfers may cause unstable vehicle associations, with memory leak potential
   */
  private[session] var interstellarFerry: Option[Vehicle] = None
  /**
   * used during zone transfers for cleanup to refer to the vehicle that instigated a transfer
   * "top level" is the carrier in a carrier/ferried association or a projected carrier/(ferried carrier)/ferried association
   * inherited from parent (carrier) to child (ferried) through the `TransferPassenger` message
   * the old-zone unique identifier for the carrier
   * no harm should come from leaving the field set to an old unique identifier value after the transfer period
   */
  private[session] var interstellarFerryTopLevelGUID: Option[PlanetSideGUID] = None
  /** a flag that forces the current zone to reload itself during a zoning operation */
  private[session] var zoneReload: Boolean = false
  private[session] val spawn: SpawnOperations = new SpawnOperations()
  private[session] var maintainInitialGmState: Boolean = false

  private[session] var zoneChannel: String = Zone.Nowhere.id

  private var loadConfZone: Boolean = false
  private var instantActionFallbackDestination: Option[Zoning.InstantAction.Located] = None
  private var zoningType: Zoning.Method = Zoning.Method.None
  private var zoningChatMessageType: ChatMessageType = ChatMessageType.CMT_QUIT
  private var zoningCounter: Int = 0
  private var zoningTimer: Cancellable = Default.Cancellable
  var displayZoningMessageWhenCancelled: Boolean = true

  /* packets */

  def handleWarpgateRequest(pkt: WarpgateRequest): Unit = {
    val WarpgateRequest(_, building_guid, destinationBuildingGuid, destinationZoneGuid, _, _) = pkt
    CancelZoningProcessWithDescriptiveReason("cancel_use")
    if (spawn.deadState != DeadState.RespawnTime) {
      continent.Buildings.values.find(_.GUID == building_guid) match {
        case Some(wg: WarpGate) if wg.Active && (sessionLogic.vehicles.GetKnownVehicleAndSeat() match {
          case (Some(vehicle), _) =>
            wg.Definition.VehicleAllowance && !wg.Definition.NoWarp.contains(vehicle.Definition)
          case _ =>
            true
        }) =>
          spawn.deadState = DeadState.RespawnTime
          cluster ! ICS.GetSpawnPoint(
            destinationZoneGuid.guid,
            player.Faction,
            destinationBuildingGuid,
            continent.Number,
            building_guid,
            context.self
          )
          log.info(s"${player.Name} wants to use a warp gate")

        case Some(wg: WarpGate) if !wg.Active =>
          log.warn(s"WarpgateRequest: ${player.Name} is knocking on an inactive warp gate")

        case _ =>
          spawn.deadState = DeadState.RespawnTime
          RequestSanctuaryZoneSpawn(player, continent.Number)
      }
    } else {
      log.debug(s"WarpgateRequest: your request was already consumed, ${player.Name}; already working on it ...")
    }
  }

  def handleDroppodLaunchRequest(pkt: DroppodLaunchRequestMessage): Unit = {
    val DroppodLaunchRequestMessage(info, _) = pkt
    cluster ! ICS.DroppodLaunchRequest(
      info.zone_number,
      info.xypos,
      player.Faction,
      context.self.toTyped[ICS.DroppodLaunchExchange]
    )
  }

  def handleBeginZoning(pkt: BeginZoningMessage): Unit = {
    val BeginZoningMessage() = pkt
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
    if (sessionLogic.connectionState != 100) configZone(continent)
    sendResponse(TimeOfDayMessage(1191182336))
    //custom
    sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty))    //clear squad list
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks

    val deployables = continent.DeployableList
    reclaimOurDeployables(deployables, name, manageDeployablesWith(player.GUID, avatar.deployables))
    drawDeployableIconsOnMap(
      depictDeployables(deployables).filter(_.Faction == faction)
    )
    //render Equipment that was dropped into zone before the player arrived
    continent.EquipmentOnGround.foreach { item =>
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
    }
    //load active players in zone (excepting players who are seated or players who are us)
    val live = continent.LivePlayers
    live
      .filterNot { tplayer =>
        tplayer.GUID == player.GUID || tplayer.VehicleSeated.nonEmpty
      }
      .foreach { targetPlayer =>
        sendResponse(OCM.apply(targetPlayer))
        if (targetPlayer.UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored) {
          sendResponse(PlanetsideAttributeMessage(targetPlayer.GUID, 19, 1))
        }
      }
    //load corpses in zone
    continent.Corpses.foreach {
      spawn.DepictPlayerAsCorpse
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
      sendResponse(OCM.apply(vehicle))
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
            sendResponse(
              OCM.apply(seat.occupant.get)
                .asInstanceOf[ObjectCreateMessage]
                .copy(parentInfo = Some(ObjectCreateMessageParent(vguid, index)))
            )
        }
      vehicle.SubsystemMessages().foreach { sendResponse }
    }
    vehicles.collect {
      case vehicle if vehicle.Faction == faction =>
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
    }
    //our vehicle would have already been loaded; see NewPlayerLoaded/AvatarCreate
    usedVehicle.headOption.collect {
      case vehicle =>
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
              sendResponse(
                OCM.apply(seat.occupant.get)
                  .asInstanceOf[ObjectCreateMessage]
                  .copy(parentInfo = Some(ObjectCreateMessageParent(vguid, index)))
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
    }
    //vehicle wreckages
    wreckages.foreach { vehicle =>
      sendResponse(
        ObjectCreateMessage(
          vehicle.Definition.DestroyedModel.get.id,
          vehicle.GUID,
          DestroyedVehicleConverter.converter.ConstructorData(vehicle).get
        )
      )
    }
    //cargo occupants (including our own vehicle as cargo)
    allActiveVehicles.collect {
      case vehicle if vehicle.CargoHolds.nonEmpty =>
        vehicle.CargoHolds.collect {
          case (_index, hold: Cargo) if hold.isOccupied =>
            //CargoMountBehaviorForUs can fail to attach the cargo vehicle on some clients
            CarrierBehavior.CargoMountBehaviorForAll(vehicle, hold.occupant.get, _index)
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
        case _ => ()
      }
    }
    deployedVehicles.filter(_.Definition == GlobalDefinitions.router).foreach { obj =>
      //the router won't work if it doesn't completely deploy
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deploying, 0, unk3 = false, Vector3.Zero))
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deployed, 0, unk3 = false, Vector3.Zero))
      sessionLogic.general.toggleTeleportSystem(obj, TelepadLike.AppraiseTeleportationSystem(obj, continent))
    }
    ServiceManager.serviceManager
      .ask(Lookup("hart"))(Timeout(2 seconds))
      .onComplete {
        case Success(LookupResult("hart", ref)) =>
          ref ! HartTimer.Update(continentId, name)
        case _ =>
      }
    //implant terminals
    continent.map.terminalToInterface.foreach {
      case (terminal_guid, interface_guid) =>
        val parent_guid = PlanetSideGUID(terminal_guid)
        continent.GUID(interface_guid).collect {
          case obj: Terminal =>
            sendResponse(
              OCM.apply(obj)
                .asInstanceOf[ObjectCreateMessage]
                .copy(parentInfo = Some(ObjectCreateMessageParent(parent_guid, 1)))
            )
        }
        //mount terminal occupants
        continent.GUID(terminal_guid).collect {
          case obj: Mountable =>
            obj.Seats(0).occupant.collect {
              case occupant: Player =>
                sendResponse(
                  OCM.apply(occupant)
                    .asInstanceOf[ObjectCreateMessage]
                    .copy(parentInfo = Some(ObjectCreateMessageParent(parent_guid, 0)))
                )
            }
        }
    }
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
                sendResponse(
                  OCM.apply(obj)
                    .asInstanceOf[ObjectCreateMessage]
                    .copy(parentInfo = Some(ObjectCreateMessageParent(pguid, 1)))
                )
              case _ => ()
            }
          }
          //reserved ammunition?
          //TODO need to register if it exists
          //mount turret occupant
          turret.Seats(0).occupant.collect {
            case occupant: Player =>
              sendResponse(
                OCM.apply(occupant)
                  .asInstanceOf[ObjectCreateMessage]
                  .copy(parentInfo = Some(ObjectCreateMessageParent(pguid, 0)))
              )
          }
          triggerAutomatedTurretFire(turret)
      }
    //remote projectiles and radiation clouds
    continent.Projectiles.foreach { projectile =>
      sendResponse(OCM.apply(projectile))
    }
    //spawn point update request
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.UpdateAmsSpawnPoint(continent)
    )
    spawn.upstreamMessageCount = 0
    zoneLoaded = Some(true)
  }

  /* messages */

  def handleZonesResponse(zones: Iterable[Zone]): Unit = {
    zones.foreach { zone =>
      val continentNumber = zone.Number
      val popBO = 0
      //TODO black ops test (partition)
      val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
      val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
      val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)

      zone.Buildings.foreach({ case (_, building) => initBuilding(continentNumber, building.MapId, building) })
      sendResponse(ZonePopulationUpdateMessage(continentNumber, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))
      //TODO should actually not claim that the sanctuary or VR zones are locked by their respective empire
      if (continentNumber == 11)
        sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NC))
      else if (continentNumber == 12)
        sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.TR))
      else if (continentNumber == 13)
        sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.VS))
      else
        sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NEUTRAL))
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
    ServiceManager.serviceManager ! Lookup("propertyOverrideManager")
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks
    sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
    spawn.initializeFriendsAndIgnoredLists()
    //the following subscriptions last until character switch/logout
    galaxyService ! Service.Join("galaxy") //for galaxy-wide messages
    galaxyService ! Service.Join(s"${avatar.faction}") //for hotspots, etc.
    sessionLogic.squadService ! Service.Join(s"${avatar.faction}") //channel will be player.Faction
    sessionLogic.squadService ! Service.Join(s"${avatar.id}") //channel will be player.CharId (in order to work with packets)
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
        sessionLogic.persist()
        oldZone.AvatarEvents ! Service.Leave()
        oldZone.LocalEvents ! Service.Leave()
        oldZone.VehicleEvents ! Service.Leave()

        if (player.isAlive && zoningType != Zoning.Method.Reset) {
          if (player.HasGUID) {
            spawn.handleNewPlayerLoaded(player)
          } else {
            //alive but doesn't have a GUID; probably logging in?
            context.self ! ICS.ZoneResponse(Some(player.Zone))
          }
        } else {
          zoneReload = true
          cluster ! ICS.GetNearbySpawnPoint(
            zone.Number,
            player.Faction,
            player.Position,
            Seq(SpawnGroup.Facility, SpawnGroup.Tower),
            context.self
          )
        }
    }
  }

  def handleZoneResponse(foundZone: Zone): Unit = {
    log.trace(s"ZoneResponse: zone ${foundZone.id} will now load for ${player.Name}")
    loadConfZone = true
    maintainInitialGmState = true
    val oldZone = session.zone
    session = session.copy(zone = foundZone)
    sessionLogic.persist()
    oldZone.AvatarEvents ! Service.Leave()
    oldZone.LocalEvents ! Service.Leave()
    oldZone.VehicleEvents ! Service.Leave()
    //the only zone-level event system subscription necessary before BeginZoningMessage (for persistence purposes)
    foundZone.AvatarEvents ! Service.Join(player.Name)
    foundZone.Population ! Zone.Population.Join(avatar)
    player.avatar = avatar
    interstellarFerry match {
      case Some(vehicle) if vehicle.PassengerInSeat(player).contains(0) =>
        TaskWorkflow.execute(registerDrivenVehicle(vehicle, player))
      case _ =>
        TaskWorkflow.execute(registerNewAvatar(player))
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
            spawn.loginChatMessage.addOne("@login_reposition_to_friendly_facility") //Your previous location was held by the enemy. You have been moved to the nearest friendly facility.
            val (pos, ori) = spawnPoint.SpecificPoint(player)
            spawn.LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
          case _ =>
            spawn.loginChatMessage.addOne("@login_reposition_to_sanctuary") //Your previous location was held by the enemy.  As there were no operational friendly facilities on that continent, you have been brought back to your Sanctuary.
            RequestSanctuaryZoneSpawn(player, player.Zone.Number)
        }

      case Zoning.Method.Login =>
        spawn.resolveZoningSpawnPointLoad(response, Zoning.Method.Login)

      case ztype =>
        if (ztype != Zoning.Method.None) {
          log.warn(
            s"SpawnPointResponse: ${player.Name}'s zoning was not in order at the time a response was received; attempting to guess what ${player.Sex.pronounSubject} wants to do"
          )
        }
        val previousZoningType = ztype
        CancelZoningProcess()
        sessionLogic.actionsToCancel()
        sessionLogic.terminals.CancelAllProximityUnits()
        sessionLogic.general.dropSpecialSlotItem()
        continent.Population ! Zone.Population.Release(avatar)
        spawn.resolveZoningSpawnPointLoad(response, previousZoningType)
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
      case Some(entry) if vehicle.Seat(entry.mount).flatMap { _.occupant }.isEmpty =>
        player.VehicleSeated = None
        vehicle.Seats(entry.mount).mount(player)
        player.VehicleSeated = vehicle.GUID
        Some((None, Some(vehicle)))
      case Some(entry) if vehicle.Seat(entry.mount).flatMap { _.occupant }.contains(player) =>
        player.VehicleSeated = vehicle.GUID
        Some((None, Some(vehicle)))
      case Some(entry) =>
        log.warn(
          s"TransferPassenger: $playerName tried to mount seat ${entry.mount} during summoning, but it was already occupied, and ${player.Sex.pronounSubject} was rebuked"
        )
        Some((None, None))
      case None =>
        //log.warn(s"TransferPassenger: $playerName is missing from the manifest of a summoning ${vehicle.Definition.Name} from ${vehicle.Zone.id}")
        None
    }).orElse {
      manifest.cargo.find {
        _.name.equals(playerName)
      } match {
        case Some(entry) =>
          vehicle.CargoHolds(entry.mount).occupant match {
            case out @ Some(cargo) if cargo.Seats(0).occupants.exists(_.Name.equals(playerName)) =>
              Some((Some(vehicle), out))
            case _ =>
              None
          }
        case None =>
          None
      }
    } match {
      case Some((Some(ferry), Some(cargo))) =>
        cargo.MountedIn = ferry.GUID
        handleTransferPassengerVehicle(cargo, temp_channel)
      case Some((None, Some(_: Vehicle))) =>
        handleTransferPassengerVehicle(vehicle, temp_channel)
      case _ =>
        interstellarFerry match {
          case None =>
            galaxyService ! Service.Leave(Some(temp_channel)) //no longer being transferred between zones
            interstellarFerryTopLevelGUID = None
          case Some(_) => () //wait patiently
        }
    }
  }

  private def handleTransferPassengerVehicle(vehicle: Vehicle, temporaryChannel: String): Unit = {
    galaxyService ! Service.Leave(Some(temporaryChannel)) //temporary vehicle-specific channel (see above)
    spawn.deadState = DeadState.Release
    sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, unk5=true))
    interstellarFerry = Some(vehicle) //on the other continent and registered to that continent's GUID system
    spawn.LoadZonePhysicalSpawnPoint(vehicle.Continent, vehicle.Position, vehicle.Orientation, respawnTime = 1 seconds, None)
  }

  def handleDroppodLaunchDenial(errorCode: DroppodError): Unit = {
    sendResponse(DroppodLaunchResponseMessage(errorCode, player.GUID))
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
      sessionLogic.immediateDisconnect()
    })
  }

  def handleSetZone(zoneId: String, position: Vector3): Unit = {
    if (sessionLogic.vehicles.serverVehicleControlVelocity.isEmpty) {
      sessionLogic.actionsToCancel()
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
          vehicle.PassengerInSeat(player) match {
            case Some(0) =>
              spawn.deadState = DeadState.Release // cancel movement updates
              vehicle.Position = position
              spawn.LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds, None)
            case _ => // not seated as the driver, in which case we can't move
          }
        case _ if !player.isAlive =>
          Player.Respawn(player)
          player.Health = 1
          spawn.LoadZonePhysicalSpawnPoint(continent.id, position, Vector3.z(player.Orientation.z), 0.seconds, None)
        case None =>
          spawn.deadState = DeadState.Release // cancel movement updates
          player.Position = position
          // continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, player.GUID))
          spawn.LoadZonePhysicalSpawnPoint(zoneId, position, Vector3.Zero, 0 seconds, None)
        case _ => // seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
      }
    }
  }

  /* support functions */

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
      avatarActor ! AvatarActor.DeactivateActiveImplants
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
      ZoningOperations.findBuildingsBySoiOccupancy(continent, player.Position) match {
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
    if (displayZoningMessageWhenCancelled && zoningStatus != Zoning.Status.None) {
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
      case _ => ()
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
    zone
      .Buildings
      .values
      .foreach { building =>
        val guid = building.GUID
        sendResponse(SetEmpireMessage(guid, building.Faction))
        // power
        building.Generator match {
          case Some(obj) if obj.Condition == PlanetSideGeneratorState.Destroyed || building.NtuLevel == 0 =>
            sendResponse(PlanetsideAttributeMessage(guid, 48, 1)) //amenities disabled; red warning lights
            sendResponse(PlanetsideAttributeMessage(guid, 38, 0)) //disable spawn target on deployment map
          case _ => ()
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
        //sendResponse(HackMessage(HackState1.Unk3, guid, Service.defaultPlayerGUID, progress=0, -1f, HackState.HackCleared, HackState7.Unk8))
      }
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
            sessionLogic.general.sendPlanetsideAttributeMessage(
              amenity.GUID,
              PlanetsideAttributeEnum.ControlConsoleHackUpdate,
              HackCaptureActor.GetHackUpdateAttributeValue(amenity.asInstanceOf[CaptureTerminal], isResecured = false)
            )
          case _ =>
            sessionLogic.general.hackObject(amenity.GUID, unk1 = 1114636288L, HackState7.Unk8) //generic hackable object
        }

      // sync capture flags
      case llu: CaptureFlag =>
        // Create LLU
        sendResponse(OCM.apply(llu))
        // Attach it to a player if it has a carrier
        if (llu.Carrier.nonEmpty) {
          continent.LocalEvents ! LocalServiceMessage(
            continent.id,
            LocalAction.SendPacket(ObjectAttachMessage(llu.Carrier.get.GUID, llu.GUID, 252))
          )
        }
      case _ => ()
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
    if (!zoneReload && zoneId.equals(continent.id)) {
      if (player.isBackpack) { // important! test the actor-wide player ref, not the parameter
        // respawning from unregistered player
        TaskWorkflow.execute(registerAvatar(targetPlayer))
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
    vehicle.Seats.collect { case (index: Int, seat: Seat) if index > 0 => seat.unmount(seat.occupant) }
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
      sessionLogic.general.unaccessContainer(vehicle)
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
        unregisterDrivenVehicle(vehicle, player),
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

          // allow AMS, ANT and Router to remain deployed when owner leaves the zone
          vehicle.Definition match {
            case GlobalDefinitions.ams | GlobalDefinitions.ant | GlobalDefinitions.router
              => sessionLogic.vehicles.ConditionalDriverVehicleControl(vehicle)

            case _ => sessionLogic.vehicles.TotalDriverVehicleControl(vehicle)
          }

          // remove owner
          vehicle.Actor ! Vehicle.Ownership(None)

        case _ => ()
      }
      avatarActor ! AvatarActor.SetVehicle(None)
    }
    spawn.removeBoomerTriggersFromInventory().foreach(obj => {
      TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, obj))
    })
    Deployables.Disown(continent, avatar, context.self)
    spawn.drawDeloyableIcon = spawn.RedrawDeployableIcons //important for when SetCurrentAvatar initializes the UI next zone
    sessionLogic.squad.squadSetup = sessionLogic.squad.ZoneChangeSquadSetup
  }

  /**
   * Attempt to transfer to the player's faction-specific sanctuary continent.
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
      sessionLogic.immediateDisconnect()
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
    sessionLogic.actionsToCancel()
    sessionLogic.terminals.CancelAllProximityUnits()
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
    spawn.LoadZonePhysicalSpawnPoint(zone.id, droppod.Position, Vector3.Zero, 0 seconds, None)
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

        def action(): Future[Any] = Future(true)
      },
      List(GUIDTask.unregisterAvatar(continent.GUID, driver), GUIDTask.unregisterVehicle(continent.GUID, vehicle))
    )
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

  def propertyOverrideManagerLoadOverrides(manager: ActorRef): Unit = {
    ask(manager, PropertyOverrideManager.GetOverridesMessage)(Timeout(2.seconds)).onComplete {
      case Success(overrides: List[Any]) =>
        //safe to cast like this
        sendResponse(PropertyOverrideMessage(overrides.map { _.asInstanceOf[PropertyOverrideMessage.GamePropertyScope] }))
      case _ => ()
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
   * Find all deployables that internally keep track of a player's name as its owner
   * and change the GUID associated with that owner (reclaim it).
   * @param deployables list of deployables
   * @param name owner name
   * @param reclamationStrategy what happens to deployables to re-assign ownership
   * @return the list of deployables whose owenrship has been re-assigned
   */
  def reclaimOurDeployables(
                             deployables: List[Deployable],
                             name: String,
                             reclamationStrategy: Deployable => Option[Deployable]
                           ): List[Deployable] = {
    deployables
      .filter {
        case _: BoomerDeployable => false //always ignore
        case obj                 => obj.OwnerName.contains(name) && !obj.Destroyed && obj.Health > 0
      }
      .flatMap(reclamationStrategy)
  }

  /**
   * If the deployable was added to a management collection, reassign its internal owner GUID.
   * Hence, it can fail.
   * @param toGuid anticipated ownership GUID
   * @param managedDeployables collection for logically organizing deployables
   * @param obj deployable designated for ownership
   * @return the deployable, if it was capable of being managed and ownership was re-assigned
   */
  def manageDeployablesWith(toGuid: PlanetSideGUID, managedDeployables: DeployableToolbox)(obj: Deployable): Option[Deployable] = {
    if (managedDeployables.AddOverLimit(obj)) {
      reassignDeployablesTo(toGuid)(obj)
    } else {
      None
    }
  }

  /**
   * Reassign the internal owner GUID on this deployable.
   * @param toGuid anticipated ownership GUID
   * @param obj deployable designated for ownership
   * @return the deployable, but now assigned to the GUID
   */
  def reassignDeployablesTo(toGuid: PlanetSideGUID)(obj: Deployable): Option[Deployable] = {
    obj.OwnerGuid = toGuid
    Some(obj)
  }

  /**
   * Render and animate all provided deployables.
   * Animation includes occupants for mountable deployables and ongoing behaviors for automated deployables.
   * @param deployables list of deployables
   * @return list of working deployables
   * @see `OCM.apply`
   */
  def depictDeployables(deployables: List[Deployable]): List[Deployable] = {
    val (smallTurrets, largeTurrets, sensors, normal, brokenThings) = {
      val (broken, working) = deployables.partition { obj =>
        obj.Destroyed || obj.Health == 0 || (obj match {
          case jammable: JammableUnit => jammable.Jammed
          case _ => false
        })
      }
      val (small, remainder1) = working.partition { obj => obj.Definition.DeployCategory == DeployableCategory.SmallTurrets }
      val (large, remainder2) = remainder1.partition { obj => obj.Definition.DeployCategory == DeployableCategory.FieldTurrets }
      val (sensor, remainder3) = remainder2.partition { obj => obj.Definition.DeployCategory == DeployableCategory.Sensors }
      (small, large, sensor, remainder3, broken)
    }
    val miscThings = normal ++ sensors ++ smallTurrets
    (brokenThings ++ miscThings).foreach { obj =>
      sendResponse(OCM.apply(obj))
    }
    largeTurrets.foreach { obj =>
      sendResponse(OCM.apply(obj))
      //seated players
      obj
        .asInstanceOf[Mountable]
        .Seats
        .values
        .map(_.occupant)
        .collect {
          case Some(occupant) if occupant.isAlive =>
            sendResponse(
              OCM.apply(occupant)
                .asInstanceOf[ObjectCreateMessage]
                .copy(parentInfo = Some(ObjectCreateMessageParent(obj.GUID, 0)))
            )
        }
    }
    triggerAutomatedTurretFire(smallTurrets)
    triggerSensorDeployables(sensors)
    miscThings ++ largeTurrets
  }

  /**
   * Render and animate all provided deployables.
   * Animation includes ongoing behaviors for automated deployables.
   * @param deployables list of deployables
   * @return list of working deployables
   * @see `OCM.apply`
   */
  def depictDeployablesUponRevival(deployables: List[Deployable]): List[Deployable] = {
    val (smallTurrets, sensors, normal) = {
      val (_, working) = deployables.partition { obj =>
        obj.Destroyed || obj.Health == 0 || (obj match {
          case jammable: JammableUnit => jammable.Jammed
          case _ => false
        })
      }
      val (small, remainder1) = working.partition { obj => obj.Definition.DeployCategory == DeployableCategory.SmallTurrets }
      val (sensor, remainder2) = remainder1.partition { obj => obj.Definition.DeployCategory == DeployableCategory.Sensors }
      (small, sensor, remainder2)
    }
    val miscThings = normal ++ sensors ++ smallTurrets
    miscThings.foreach { obj =>
      sendResponse(OCM.apply(obj))
    }
    triggerAutomatedTurretFire(smallTurrets)
    triggerSensorDeployables(sensors)
    miscThings
  }

  /**
   * Treat the deployables as sensor-types and provide appropriate animation.
   * This animation is the glowing halo-ing effect on its sensor bulb.
   * @param sensors list of deployables
   * @see `TriggerEffectMessage`
   */
  def triggerSensorDeployables(sensors: List[Deployable]): Unit = {
    sensors.foreach { obj =>
      sendResponse(TriggerEffectMessage(obj.GUID, effect = "on", unk1 = true, unk2 = 1000))
    }
  }

  /**
   * Treat the deployables as small turret-types and provide appropriate animation.
   * This animation is related to its automation - tracking and shooting.
   * @param turrets list of deployables
   */
  def triggerAutomatedTurretFire(turrets: List[Deployable]): Unit = {
    turrets.collect { case turret: AutomatedTurret =>
      triggerAutomatedTurretFire(turret)
    }
  }
  /**
   * Provide appropriate animation to the small turret deployable.
   * This animation is related to its automation - tracking and shooting.
   * @param turret small turret deployable
   * @see `ChangeFireStateMessage_Start`
   * @see `ObjectDetectedMessage`
   */
  def triggerAutomatedTurretFire(turret: AutomatedTurret): Unit = {
    turret.Target.foreach { target =>
      val guid = turret.GUID
      sendResponse(ObjectDetectedMessage(guid, guid, 0, List(target.GUID)))
      sendResponse(ChangeFireStateMessage_Start(turret.Weapons.values.head.Equipment.get.GUID))
    }
  }

  /**
   * Draw or redraw deployment map icons related to deployable presence and deployable management (if the owner).
   * Assert deployable health as a precaution.
   * @param deployables list of deployables
   * @see `DeployableObjectsInfoMessage`
   */
  def drawDeployableIconsOnMap(deployables: List[Deployable]): Unit = {
    deployables
      .foreach { obj =>
        val guid = obj.GUID
        val health = obj.Health
        if (health != obj.DefaultHealth) {
          sendResponse(PlanetsideAttributeMessage(guid, 0, health))
        }
        sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, DeployableInfo(
          guid,
          Deployable.Icon(obj.Definition.Item),
          obj.Position,
          obj.OwnerGuid.getOrElse(Service.defaultPlayerGUID)
        )))
      }
  }

  /* nested class - spawn operations */

  class SpawnOperations() {
    private[session] var deadState: DeadState.Value = DeadState.Dead
    private[session] var loginChatMessage: mutable.ListBuffer[String] = new mutable.ListBuffer[String]()
    private[session] var amsSpawnPoints: List[SpawnPoint] = Nil
    private[session] var noSpawnPointHere: Boolean = false
    private[session] var setupAvatarFunc: () => Unit = AvatarCreate
    private[session] var setCurrentAvatarFunc: Player => Unit = SetCurrentAvatarNormally
    private[session] var nextSpawnPoint: Option[SpawnPoint] = None
    private[session] var prevSpawnPoint: Option[SpawnPoint] = None
    private[session] var interimUngunnedVehicle: Option[PlanetSideGUID] = None
    private[session] var interimUngunnedVehicleSeat: Option[Int] = None
    /** Upstream message counter<br>
     * Checks for server acknowledgement of the following messages in the following conditions:<br>
     * `PlayerStateMessageUpstream` (infantry)<br>
     * `VehicleStateMessage` (driver mount only)<br>
     * `ChildObjectStateMessage` (any gunner mount that is not the driver)<br>
     * `KeepAliveMessage` (any passenger mount that is not the driver)<br>
     * As they should arrive roughly every 250 milliseconds this allows for a very crude method of scheduling tasks up to four times per second
     */
    private[session] var upstreamMessageCount: Int = 0
    private[session] var shiftPosition: Option[Vector3] = None
    private[session] var shiftOrientation: Option[Vector3] = None
    private[session] var drawDeloyableIcon: PlanetSideGameObject with Deployable => Unit = RedrawDeployableIcons
    private[session] var populateAvatarAwardRibbonsFunc: (Int, Long) => Unit = setupAvatarAwardMessageDelivery
    private[session] var setAvatar: Boolean = false
    private[session] var reviveTimer: Cancellable = Default.Cancellable
    private[session] var respawnTimer: Cancellable = Default.Cancellable

    private var statisticsPacketFunc: () => Unit = loginAvatarStatisticsFields

    /* packets */

    def handleReleaseAvatarRequest(pkt: ReleaseAvatarRequestMessage): Unit = {
      val ReleaseAvatarRequestMessage() = pkt
      log.info(s"${player.Name} on ${continent.id} has released")
      reviveTimer.cancel()
      GoToDeploymentMap()
      HandleReleaseAvatar(player, continent)
    }

    def handleSpawnRequest(pkt: SpawnRequestMessage): Unit = {
      val SpawnRequestMessage(_, spawnGroup, _, _, zoneNumber) = pkt
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
          player.Faction,
          shiftPosition.getOrElse(player.Position),
          Seq(spawnGroup),
          context.self
        )
        shiftPosition = None
      } else {
        log.warn(s"SpawnRequestMessage: request consumed because ${player.Name} is already respawning ...")
      }
    }

    /* messages */

    def handleLoginInfoNowhere(name: String, from: ActorRef): Unit = {
      log.info(s"LoginInfo: player $name is considered a fresh character")
      sessionLogic.persistFunc = UpdatePersistence(from)
      deadState = DeadState.RespawnTime
      val tplayer = new Player(avatar)
      session = session.copy(player = tplayer)
      //actual zone is undefined; going to our sanctuary
      RandomSanctuarySpawnPosition(tplayer)
      DefinitionUtil.applyDefaultLoadout(tplayer)
      avatarActor ! AvatarActor.LoginAvatar(context.self)
    }

    def handleLoginInfoSomewhere(name: String, inZone: Zone, optionalSavedData: Option[Savedplayer], from: ActorRef): Unit = {
      log.info(s"LoginInfo: player $name is considered a fresh character")
      sessionLogic.persistFunc = UpdatePersistence(from)
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
            player.ExoSuit = ExoSuitType(results.exosuitNum)
            player.Armor = results.armor
            AvatarActor.buildContainedEquipmentFromClob(player, results.loadout, log, restoreAmmo = true)
            if (player.ExoSuit == ExoSuitType.MAX) {
              player.DrawnSlot = 0
              player.ResistArmMotion(PlayerControl.maxRestriction)
            }
          } else {
            player.ExoSuit = ExoSuitType.Standard
            DefinitionUtil.applyDefaultLoadout(player)
          }
          if (player.isAlive) {
            zoningType = Zoning.Method.Login
            player.ZoningRequest = Zoning.Method.Login
            zoningChatMessageType = ChatMessageType.UNK_227
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
                loginChatMessage.addOne("@reset_sanctuary_locked")
                //You have been returned to the sanctuary because the location you logged out is not available.
                player.Zone = Zone.Nowhere
              } else if (ourBuildings.isEmpty && (amsSpawnPoints.isEmpty || noFriendlyPlayersInZone)) {
                loginChatMessage.addOne("@reset_sanctuary_locked")
                //You have been returned to the sanctuary because the location you logged out is not available.
                player.Zone = Zone.Nowhere
              } else if (friendlyPlayersInZone > 137 || playersInZone.size > 413) {
                loginChatMessage.addOne("@reset_sanctuary_full")
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
                    zoningType = Zoning.Method.Reset
                    player.ZoningRequest = Zoning.Method.Reset
                    zoningChatMessageType = ChatMessageType.UNK_228
                  }
                } else {
                  if (noFriendlyPlayersInZone) {
                    loginChatMessage.addOne("@reset_sanctuary_inactive")
                    //You have been returned to the sanctuary because the location you logged out is not available.
                    player.Zone = Zone.Nowhere
                  }
                }
              }
            }
          } else {
            //player is dead; go back to sanctuary
            loginChatMessage.addOne("@reset_sanctuary_inactive")
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
      sessionLogic.persistFunc = UpdatePersistence(from)
      //tell the old WorldSessionActor to kill itself by using its own subscriptions against itself
      inZone.AvatarEvents ! AvatarServiceMessage(name, AvatarAction.TeardownConnection())
      spawn.switchAvatarStatisticsFieldToRefreshAfterRespawn()
      //find and reload previous player
      (
        inZone.Players.find(p => p.name.equals(name)),
        inZone.AllPlayers.find(p => p.Name.equals(name))
      ) match {
        case (_, Some(p)) if p.death_by == -1 =>
          //player is not allowed
          sessionLogic.kickedByAdministration()

        case (Some(a), Some(p)) if p.isAlive =>
          //rejoin current avatar/player
          log.info(s"RestoreInfo: player $name is alive")
          deadState = DeadState.Alive
          session = session.copy(player = p, avatar = a)
          sessionLogic.persist()
          setupAvatarFunc = AvatarRejoin
          dropMedicalApplicators(p)
          avatarActor ! AvatarActor.ReplaceAvatar(a)
          avatarLoginResponse(a)

        case (Some(a), Some(p)) =>
          //convert player to a corpse (unless in vehicle); automatic recall to closest spawn point
          log.info(s"RestoreInfo: player $name is dead")
          deadState = DeadState.Dead
          session = session.copy(player = p, avatar = a)
          sessionLogic.persist()
          dropMedicalApplicators(p)
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
        case PlayerToken.DeniedLoginReason.Kicked => sessionLogic.kickedByAdministration()
        case _ => sendResponse(DisconnectMessage("You will be logged out."))
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
              loginChatMessage.addOne("@login_reposition_to_friendly_facility") //Your previous location was held by the enemy. You have been moved to the nearest friendly facility.
              val (pos, ori) = spawnPoint.SpecificPoint(player)
              LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
            case _ =>
              loginChatMessage.addOne("@login_reposition_to_sanctuary") //Your previous location was held by the enemy.  As there were no operational friendly facilities on that continent, you have been brought back to your Sanctuary.
              RequestSanctuaryZoneSpawn(player, player.Zone.Number)
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
          CancelZoningProcess()
          sessionLogic.actionsToCancel()
          sessionLogic.terminals.CancelAllProximityUnits()
          sessionLogic.general.dropSpecialSlotItem()
          continent.Population ! Zone.Population.Release(avatar)
          resolveZoningSpawnPointLoad(response, previousZoningType)
      }
    }

    def handleNewPlayerLoaded(tplayer: Player): Unit = {
      /* new zone, might be on `tplayer.Zone` but should definitely be on `session` */
      val zone = session.zone
      val id = zone.id
      val map = zone.map
      val mapName = map.name
      log.info(s"${tplayer.Name} has spawned into $id")
      sessionLogic.zoning.zoneChannel = Players.ZoneChannelIfSpectating(tplayer, zone.id)
      sessionLogic.oldRefsMap.clear()
      sessionLogic.persist = UpdatePersistenceAndRefs
      tplayer.avatar = avatar
      session = session.copy(player = tplayer)
      //LoadMapMessage causes the client to send BeginZoningMessage, eventually leading to SetCurrentAvatar
      val weaponsEnabled = !(mapName.equals("map11") || mapName.equals("map12") || mapName.equals("map13"))
      sendResponse(LoadMapMessage(mapName, id, 40100, 25, weaponsEnabled, map.checksum))
      if (isAcceptableNextSpawnPoint) {
        //important! the LoadMapMessage must be processed by the client before the avatar is created
        player.allowInteraction = true
        setupAvatarFunc()
        //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
        sessionLogic.turnCounterFunc = interimUngunnedVehicle match {
          case Some(_) =>
            TurnCounterDuringInterimWhileInPassengerSeat
          case None if zoningType == Zoning.Method.Login || zoningType == Zoning.Method.Reset =>
            TurnCounterLogin
          case None =>
            TurnCounterDuringInterim
        }
        sessionLogic.keepAliveFunc = NormalKeepAlive
        if (zoningStatus == Zoning.Status.Deconstructing) {
          stopDeconstructing()
        }
        sessionLogic.avatarResponse.lastSeenStreamMessage.clear()
        upstreamMessageCount = 0
        setAvatar = false
        sessionLogic.persist()
      } else {
        //look for different spawn point in same zone
        cluster ! ICS.GetNearbySpawnPoint(
          zone.Number,
          tplayer.Faction,
          tplayer.Position,
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
        player.allowInteraction = true
        setupAvatarFunc()
        //interimUngunnedVehicle should have been setup by setupAvatarFunc, if it is applicable
        sessionLogic.turnCounterFunc = interimUngunnedVehicle match {
          case Some(_) =>
            TurnCounterDuringInterimWhileInPassengerSeat
          case None =>
            TurnCounterDuringInterim
        }
        sessionLogic.keepAliveFunc = NormalKeepAlive
        if (zoningStatus == Zoning.Status.Deconstructing) {
          stopDeconstructing()
        }
        sessionLogic.avatarResponse.lastSeenStreamMessage.clear()
        upstreamMessageCount = 0
        setAvatar = false
        sessionLogic.persist()
      } else {
        //look for different spawn point in same zone
        cluster ! ICS.GetNearbySpawnPoint(
          continent.Number,
          tplayer.Faction,
          tplayer.Position,
          Seq(SpawnGroup.Facility, SpawnGroup.Tower, SpawnGroup.AMS),
          context.self
        )
      }
    }

    def handlePlayerHasLeft(zone: Zone, playerOpt: Option[Player]): Unit = {
      playerOpt match {
        case None =>
          log.debug(s"PlayerHasLeft: ${player.Name} does not have a body on ${zone.id}")
        case Some(tplayer) if tplayer.isAlive =>
          log.info(s"${tplayer.Name} has left zone ${zone.id}")
        case _ => ()
      }
    }

    def handlePlayerCanNotSpawn(zone: Zone, tplayer: Player): Unit = {
      log.warn(s"${tplayer.Name} can not spawn in zone ${zone.id}; why?")
    }

    def handlePlayerAlreadySpawned(zone: Zone, tplayer: Player): Unit = {
      log.warn(s"${tplayer.Name} is already spawned on zone ${zone.id}; is this a clerical error?")
    }

    def handleCanNotSpawn(zone: Zone, vehicle: Vehicle, reason: String): Unit = {
      log.warn(
        s"${player.Name}'s ${vehicle.Definition.Name} can not spawn in ${zone.id} because $reason"
      )
    }

    def handleCanNotDespawn(zone: Zone, vehicle: Vehicle, reason: String): Unit = {
      log.warn(
        s"${player.Name}'s ${vehicle.Definition.Name} can not deconstruct in ${zone.id} because $reason"
      )
    }

    def handlePlayerFailedToLoad(tplayer: Player): Unit = {
      sessionLogic.failWithError(s"${tplayer.Name} failed to load anywhere")
    }

    /* support functions */

    private def dropMedicalApplicators(p: Player): Unit = {
      WorldSession.DropLeftovers(p)(
        (p.Holsters().zipWithIndex.collect { case (slot, index) if slot.Equipment.nonEmpty => InventoryItem(slot.Equipment.get, index) } ++
          p.Inventory.Items ++
          p.FreeHand.Equipment.flatMap { item => Some(InventoryItem(item, Player.FreeHandSlot)) }.toList)
          .collect {
            case entry @ InventoryItem(equipment, index)
              if equipment.Definition == GlobalDefinitions.medicalapplicator && p.DrawnSlot == index =>
              p.Slot(index).Equipment = None
              p.DrawnSlot = Player.HandsDownSlot
              entry
            case entry @ InventoryItem(equipment, index)
              if equipment.Definition == GlobalDefinitions.medicalapplicator =>
              p.Slot(index).Equipment = None
              entry
          }
      )
    }

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
      sessionLogic.chat.JoinChannel(DefaultChannel)
      Deployables.InitializeDeployableQuantities(avatar)
      cluster ! ICS.FilterZones(_ => true, context.self)
    }

    /**
     * na
     * @param tplayer na
     * @param zone na
     */
    def HandleReleaseAvatar(tplayer: Player, zone: Zone): Unit = {
      sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc
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
      if (sessionLogic.vehicles.serverVehicleControlVelocity.isEmpty) {
        sessionLogic.actionsToCancel()
        continent.GUID(player.VehicleSeated) match {
          case Some(vehicle: Vehicle) if vehicle.MountedIn.isEmpty =>
            vehicle
              .PassengerInSeat(player)
              .collect {
                case 0 => //driver of the vehicle carries the vehicle and its passengers
                  deadState = DeadState.Release //cancel movement updates
                  vehicle.Position = position
                  doorsThatShouldBeClosedOrBeOpenedByRange(
                    player.Position,
                    sessionLogic.localResponse.doorLoadRange(),
                    position,
                    openRange = 100f
                  )
                  LoadZonePhysicalSpawnPoint(continent.id, position, Vector3.z(vehicle.Orientation.z), 0 seconds, None)
            }
          case None =>
            deadState = DeadState.Release // cancel movement updates
            doorsThatShouldBeClosedOrBeOpenedByRange(
              player.Position,
              sessionLogic.localResponse.doorLoadRange(),
              position,
              openRange = 100f
            )
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
      avatarActor ! AvatarActor.SoftResetImplants
      player.Spawn()
      if (health != 0) {
        player.Health = health
        player.Armor = armor
      }
      player.death_by = math.min(player.death_by, 0)
      sessionLogic.vehicles.GetKnownVehicleAndSeat() match {
        case (Some(vehicle: Vehicle), Some(seat: Int)) =>
          //if the vehicle is the cargo of another vehicle in this zone
          val carrierInfo = continent.GUID(vehicle.MountedIn) match {
            case Some(carrier: Vehicle) =>
              (Some(carrier), carrier.CargoHolds.find({ case (_, hold) => hold.occupant.contains(vehicle) }))
            case _ =>
              (None, None)
          }
          //vehicle and driver/passenger
          interstellarFerry = None
          val vdef  = vehicle.Definition
          val vObjectId  = vdef.ObjectId
          val vguid = vehicle.GUID
          vehicle.Position = shiftPosition.getOrElse(vehicle.Position)
          vehicle.Orientation = shiftOrientation.getOrElse(vehicle.Orientation)
          if (seat == 0) {
            //driver
            if (vehicle.Zone ne continent) {
              continent.Transport ! Zone.Vehicle.Spawn(vehicle)
            }
            //as the driver, we must temporarily exclude ourselves from being in the vehicle during its creation
            val mount = vehicle.Seats(0)
            mount.unmount(player)
            player.VehicleSeated = None
            val data = vdef.Packet.ConstructorData(vehicle).get
            sendResponse(ObjectCreateMessage(vObjectId, vguid, data))
            mount.mount(player)
            player.VehicleSeated = vguid
            Vehicles.Own(vehicle, player)
            vehicle.CargoHolds.values
              .collect { case hold if hold.isOccupied => hold.occupant.get }
              .foreach { _.MountedIn = vguid }
            events ! VehicleServiceMessage(
              zoneid,
              VehicleAction.LoadVehicle(player.GUID, vehicle, vObjectId, vguid, data)
            )
            carrierInfo match {
              case (Some(carrier), Some((index, _))) =>
                CarrierBehavior.CargoMountBehaviorForOthers(carrier, vehicle, index, player.GUID)
              case _ =>
                vehicle.MountedIn = None
            }
            vehicle.allowInteraction = true
          } else {
            //passenger
            //non-drivers are not rendered in the vehicle at this time
            sendResponse(OCM.apply(vehicle))
            carrierInfo match {
              case (Some(carrier), Some((index, _))) =>
                CargoMountBehaviorForUs(carrier, vehicle, index)
              case _ => ()
            }
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
            log.debug(
              s"AvatarCreate: cleaning up ghost of transitioning vehicle ${vehicle.Definition.Name}@${vehicleToDelete.guid} in zone ${zone.id}"
            )
          }
          Vehicles.ReloadAccessPermissions(vehicle, player.Name)
          log.debug(s"AvatarCreate (vehicle): ${player.Name}'s ${vdef.Name}")
          AvatarCreateInVehicle(player, vehicle, seat)

        case _ if player.spectator =>
          player.VehicleSeated = None
          val definition = player.avatar.definition
          val guid = player.GUID
          sendResponse(OCM.detailed(player))
          continent.AvatarEvents ! AvatarServiceMessage(
            s"spectator",
            AvatarAction.LoadPlayer(guid, definition.ObjectId, guid, definition.Packet.ConstructorData(player).get, None)
          )

        case _ =>
          player.VehicleSeated = None
          val definition = player.avatar.definition
          val guid = player.GUID
          usingSpawnTubeAnimation()
          sendResponse(OCM.detailed(player))
          continent.AvatarEvents ! AvatarServiceMessage(
            zoneid,
            AvatarAction.LoadPlayer(guid, definition.ObjectId, guid, definition.Packet.ConstructorData(player).get, None)
          )
      }
      continent.Population ! Zone.Population.Spawn(avatar, player, avatarActor)
      avatarActor ! AvatarActor.RefreshPurchaseTimes()
      drawDeployableIconsOnMap(
        depictDeployablesUponRevival(
          reclaimOurDeployables(continent.DeployableList, player.Name, reassignDeployablesTo(player.GUID))
        )
      )
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
     * @see `SessionMountHandlers.updateWeaponAtSeatPosition`
     * @param tplayer the player avatar seated in the vehicle's mount
     * @param vehicle the vehicle the player is riding
     * @param seat the mount index
     */
    def AvatarCreateInVehicle(tplayer: Player, vehicle: Vehicle, seat: Int): Unit = {
      log.debug(s"AvatarCreateInVehicle: ${tplayer.Name}")
      val pdef  = tplayer.avatar.definition
      val pguid = tplayer.GUID
      val vguid = vehicle.GUID
      tplayer.VehicleSeated = None
      sendResponse(OCM.detailed(tplayer))
      tplayer.VehicleSeated = vguid
      if (seat == 0 || vehicle.WeaponControlledFromSeat(seat).nonEmpty) {
        sendResponse(ObjectAttachMessage(vguid, pguid, seat))
        sessionLogic.general.accessContainer(vehicle)
        sessionLogic.mountResponse.updateWeaponAtSeatPosition(vehicle, seat)
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
      sessionLogic.vehicles.GetKnownVehicleAndSeat() match {
        case (Some(vehicle: Vehicle), Some(seat: Int)) =>
          //vehicle and driver/passenger
          val vguid = vehicle.GUID
          sendResponse(OCM.apply(vehicle))
          Vehicles.ReloadAccessPermissions(vehicle, continent.id)
          log.debug(s"AvatarCreate (vehicle): ${player.Name}'s ${vehicle.Definition.Name}")
          val pdef   = player.avatar.definition
          val pguid  = player.GUID
          player.VehicleSeated = None
          val pdata = pdef.Packet.DetailedConstructorData(player).get
          player.VehicleSeated = vguid
          sendResponse(ObjectCreateDetailedMessage(pdef.ObjectId, pguid, pdata))
          log.debug(s"AvatarRejoin: ${player.Name} - $pguid -> $pdata")
          if (seat == 0 || vehicle.WeaponControlledFromSeat(seat).nonEmpty) {
            sendResponse(ObjectAttachMessage(vguid, pguid, seat))
            sessionLogic.general.accessContainer(vehicle)
            sessionLogic.mountResponse.updateWeaponAtSeatPosition(vehicle, seat)
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
      avatarActor ! AvatarActor.DeinitializeImplants
      zones.exp.ToDatabase.reportRespawns(tplayer.CharId, ScoreCard.reviveCount(player.avatar.scorecard.CurrentLife))
      val obj = Player.Respawn(tplayer)
      DefinitionUtil.applyDefaultLoadout(obj)
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
          case None => ()
          case Some(knife) =>
            RemoveOldEquipmentFromInventory(obj)(knife)
        }
        obj.Slot(0).Equipment match {
          case Some(arms: Tool) =>
            if (GlobalDefinitions.isMaxArms(arms.Definition)) {
              RemoveOldEquipmentFromInventory(obj)(arms)
            }
          case _ => ()
        }
        //disown boomers and drop triggers
        val boomers = avatar.deployables.ClearDeployable(DeployedItem.boomer)
        boomers.foreach(boomer => {
          continent.GUID(boomer) match {
            case Some(obj: BoomerDeployable) =>
              obj.Actor ! Deployable.Ownership(None)
            case Some(_) | None => ()
          }
        })
        removeBoomerTriggersFromInventory().foreach(trigger => { sessionLogic.general.normalItemDrop(obj, continent)(trigger) })
      }
    }

    /**
     * Search through the player's holsters and their inventory space
     * and remove all `BoomerTrigger` objects, both functionally and visually.
     * @return all discovered `BoomTrigger` objects
     */
    def removeBoomerTriggersFromInventory(): List[BoomerTrigger] = {
      val events = continent.AvatarEvents
      val zoneId = continent.id
      (player.Inventory.Items ++ player.HolsterItems())
        .collect { case InventoryItem(obj: BoomerTrigger, index) =>
          player.Slot(index).Equipment = None
          continent.GUID(obj.Companion) match {
            case Some(mine: BoomerDeployable) => mine.Actor ! Deployable.Ownership(None)
            case _ => ()
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
          if (zoningType == Zoning.Method.InstantAction)
            LoadZonePhysicalSpawnPoint(zone.id, pos, ori, respawnTime = 0 seconds, Some(spawnPoint))
          else
            LoadZonePhysicalSpawnPoint(zone.id, pos, ori, CountSpawnDelay(zone.id, spawnPoint, continent.id), Some(spawnPoint))
        case None =>
          log.warn(
            s"SpawnPointResponse: ${player.Name} received no spawn point response when asking InterstellarClusterService"
          )
          if (Config.app.game.warpGates.defaultToSanctuaryDestination) {
            log.warn(s"SpawnPointResponse: sending ${player.Name} home")
            RequestSanctuaryZoneSpawn(player, currentZone = 0)
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
      val toZoneNumber = if (continent.id.equals(zoneId)) {
        continent.Number
      } else {
        Zones.zones.find { _.id.equals(zoneId) }.orElse(Some(Zone.Nowhere)).get.Number
      }
      val toSide = physSpawnPoint.map(_.Owner) match {
        case Some(_: WarpGate) =>
          Sidedness.OutsideOf
        case Some(_: Building) =>
          Sidedness.InsideOf
        case Some(v: Vehicle) =>
          v.WhichSide //though usually OutsideOf
        case _ =>
          Sidedness.StrictlyBetweenSides //todo needs better determination
      }
      val toSpawnPoint = physSpawnPoint.collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) }
      respawnTimer = context.system.scheduler.scheduleOnce(respawnTime) {
        if (player.isBackpack) { // if the player is dead, he is handled as dead infantry, even if he died in a vehicle
          // new player is spawning
          val newPlayer = RespawnClone(player)
          newPlayer.LogActivity(SpawningActivity(PlayerSource(newPlayer), toZoneNumber, toSpawnPoint))
          LoadZoneAsPlayerUsing(newPlayer, pos, ori, toSide, zoneId)
        } else {
          avatarActor ! AvatarActor.DeactivateActiveImplants
          val betterSpawnPoint = physSpawnPoint.collect { case o: PlanetSideGameObject with FactionAffinity with InGameHistory => o }
          interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
            case Some(vehicle: Vehicle) => // driver or passenger in vehicle using a warp gate, or a droppod
              InGameHistory.SpawnReconstructionActivity(vehicle, toZoneNumber, betterSpawnPoint)
              InGameHistory.SpawnReconstructionActivity(player, toZoneNumber, betterSpawnPoint)
              vehicle.WhichSide = toSide
              LoadZoneInVehicle(vehicle, pos, ori, zoneId)

            case _ if player.HasGUID => // player is deconstructing self or instant action
              val player_guid = player.GUID
              sendResponse(ObjectDeleteMessage(player_guid, 4))
              continent.AvatarEvents ! AvatarServiceMessage(
                continent.id,
                AvatarAction.ObjectDelete(player_guid, player_guid, 4)
              )
              InGameHistory.SpawnReconstructionActivity(player, toZoneNumber, betterSpawnPoint)
              LoadZoneAsPlayerUsing(player, pos, ori, toSide, zoneId)

            case _ => //player is logging in
              InGameHistory.SpawnReconstructionActivity(player, toZoneNumber, betterSpawnPoint)
              LoadZoneAsPlayerUsing(player, pos, ori, toSide, zoneId)
          }
        }
      }
    }

    /**
     * na
     * @param target player being spawned
     * @param position where player is being placed in the game wqrld
     * @param orientation in what direction the player is facing in the game world
     * @param onThisSide description of the containing environment
     * @param goingToZone common designation for the zone
     */
    private def LoadZoneAsPlayerUsing(
                                     target: Player,
                                     position: Vector3,
                                     orientation: Vector3,
                                     onThisSide: Sidedness,
                                     goingToZone: String
                                   ): Unit = {
      target.Position = position
      target.Orientation = orientation
      target.WhichSide = onThisSide
      LoadZoneAsPlayer(target, goingToZone)
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
            RequestSanctuaryZoneSpawn(player, continent.Number)
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
            zoneLoaded = None
            zoneReload = true
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
            sessionLogic.keepAliveFunc = sessionLogic.vehicles.GetMountableAndSeat(None, player, continent) match {
              case (Some(v: Vehicle), Some(seatNumber))
                if seatNumber > 0 && v.WeaponControlledFromSeat(seatNumber).isEmpty =>
                sessionLogic.keepAlivePersistenceFunc
              case _ =>
                NormalKeepAlive
            }
            prevSpawnPoint = nextSpawnPoint
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
      val tavatar = tplayer.avatar
      val guid = tplayer.GUID
      sessionLogic.general.updateDeployableUIElements(Deployables.InitializeDeployableUIElements(tavatar))
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 75, 0))
      sendResponse(SetCurrentAvatarMessage(guid, 0, 0))
      sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, wideContents=true, "", "1 on", None)) //CC on //TODO once per respawn?
      val pos = tplayer.Position = shiftPosition.getOrElse(tplayer.Position)
      val orient = tplayer.Orientation = shiftOrientation.getOrElse(tplayer.Orientation)
      sendResponse(PlayerStateShiftMessage(ShiftState(1, pos, orient.z)))
      shiftPosition = None
      shiftOrientation = None
      if (tplayer.Jammed) {
        //TODO something better than just canceling?
        tplayer.Actor ! JammableUnit.ClearJammeredStatus()
        tplayer.Actor ! JammableUnit.ClearJammeredSound()
      }
      avatarActor ! AvatarActor.SoftResetImplants
      val originalDeadState = deadState
      deadState = DeadState.Alive
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 82, 0))
      initializeShortcutsAndBank(guid, tavatar.shortcuts)
      //Favorites lists
      avatarActor ! AvatarActor.InitialRefreshLoadouts()

      sendResponse(
        SetChatFilterMessage(ChatChannel.Platoon, origin = false, ChatChannel.values.toList)
      ) //TODO will not always be "on" like this
      sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, tplayer.Position, tplayer.Faction, unk5 = true))
      //looking for squad (members)
      if (tplayer.avatar.lookingForSquad) {
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
      statisticsPacketFunc()
      if (tplayer.ExoSuit == ExoSuitType.MAX) {
        sendResponse(PlanetsideAttributeMessage(guid, 7, tplayer.Capacitor.toLong))
      }
      // AvatarAwardMessage
      //populateAvatarAwardRibbonsFunc(1, 20L)

      sendResponse(PlanetsideStringAttributeMessage(guid, 0, "Outfit Name"))
      //squad stuff (loadouts, assignment)
      sessionLogic.squad.squadSetup()
      //MapObjectStateBlockMessage and ObjectCreateMessage?
      //TacticsMessage?
      //change the owner on our deployables (re-draw the icons for our deployables too)
      val name = tplayer.Name
      continent.DeployableList
        .filter(_.OwnerName.contains(name))
        .foreach(obj => {
          obj.OwnerGuid = guid
          drawDeloyableIcon(obj)
        })
      drawDeloyableIcon = DontRedrawIcons

      //assert or transfer vehicle ownership
      continent.GUID(tplayer.avatar.vehicle) match {
        case Some(vehicle: Vehicle) if vehicle.OwnerName.contains(tplayer.Name) =>
          vehicle.OwnerGuid = guid
          continent.VehicleEvents ! VehicleServiceMessage(
            s"${tplayer.Faction}",
            VehicleAction.Ownership(guid, vehicle.GUID)
          )
        case _ =>
          avatarActor ! AvatarActor.SetVehicle(None)
      }
      sessionLogic.vehicles.GetVehicleAndSeat() match {
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
          //driver of vehicle
          if (vehicle.Jammed) {
            //TODO something better than just canceling?
            vehicle.Actor ! JammableUnit.ClearJammeredStatus()
            vehicle.Actor ! JammableUnit.ClearJammeredSound()
          }
          // positive shield strength
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
          // vehicle entering zone
          if (vehicle.History.headOption.exists { _.isInstanceOf[SpawningActivity] }) {
            vehicle.LogActivity(ReconstructionActivity(VehicleSource(vehicle), continent.Number, None))
          }
          // summon any passengers and cargo vehicles left behind on previous continent
          LoadZoneTransferPassengerMessages(
            guid,
            continent.id,
            vehicle
          )
        case (Some(vehicle), _) =>
          //passenger
          vehicle.Actor ! Vehicle.UpdateZoneInteractionProgressUI(tplayer)
        case _ => ()
      }
      interstellarFerryTopLevelGUID = None
      if (loadConfZone && sessionLogic.connectionState == 100) {
        configZone(continent)
        loadConfZone = false
      }
      if (noSpawnPointHere) {
        RequestSanctuaryZoneSpawn(tplayer, continent.Number)
      } else if (originalDeadState == DeadState.Dead || tplayer.Health == 0) {
        //killed during spawn setup or possibly a relog into a corpse (by accident?)
        tplayer.Actor ! Player.Die()
      } else {
        //properly logged in
        AvatarActor.savePlayerData(tplayer)
        sessionLogic.general.displayCharSavedMsgThenRenewTimer(
          Config.app.game.savedMsg.short.fixed,
          Config.app.game.savedMsg.short.variable
        )
        val effortBy = getSpawnTubeOwner
          .collect {
            case (_, Some(v: Vehicle)) =>
              sessionLogic.vehicleResponseOperations.announceAmsDecay(
                v.GUID,
                msg = "The AMS you were bound to has lost its owner.  It will auto-deconstruct soon."
              )
              continent.GUID(v.OwnerGuid)
            case (sp, Some(_: Building)) =>
              Some(sp)
          }
          .collect { case Some(thing: PlanetSideGameObject with FactionAffinity) => Some(SourceEntry(thing)) }
          .flatten
        val lastEntryOpt = tplayer.History.lastOption
        if (lastEntryOpt.exists { !_.isInstanceOf[IncarnationActivity] }) {
          tplayer.LogActivity({
            lastEntryOpt match {
              case Some(_) =>
                ReconstructionActivity(PlayerSource(tplayer), continent.Number, effortBy)
              case None =>
                SpawningActivity(PlayerSource(tplayer), continent.Number, effortBy)
            }
          })
        }
        doorsThatShouldBeOpenInRange(pos, range = 100f)
        setAvatar = true
        player.allowInteraction = true
        upstreamMessageCount = 0
        if (tplayer.spectator) {
          if (!setAvatar) {
            context.self ! SessionActor.SetMode(sessionLogic.chat.CurrentSpectatorMode) //should reload spectator status
          }
        } else if (
          !avatar.permissions.canGM && /* gm's are excluded */
            Config.app.game.promotion.active && /* play versus progress system must be active */
            BattleRank.withExperience(tplayer.avatar.bep).value <= Config.app.game.promotion.broadcastBattleRank && /* must be below a certain battle rank */
            tavatar.scorecard.Lives.isEmpty && /* first life after login */
            tavatar.scorecard.CurrentLife.prior.isEmpty && /* no revives */
            tplayer.History.size == 1 /* did nothing but come into existence */
        ) {
          ZoningOperations.reportProgressionSystem(context.self)
        }
      }
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
     * Respond to feedback of how the avatar's data is being handled
     * in a way that properly reflects the state of the server at the moment.
     * @param state indicator for the progress of the avatar
     */
    def handleAvatarLoadingSync(state: Int): Unit = {
      if (state == 2 && zoneLoaded.contains(true)) {
        initializeFriendsAndIgnoredLists()
        initializeShortcutsAndBank(player.GUID)
        avatarActor ! AvatarActor.RefreshPurchaseTimes()
        loginAvatarStatisticsFields()
        avatarActor ! AvatarActor.InitialRefreshLoadouts()
      }
    }

    /**
     * Set up and dispatch a list of `FriendsResponse` packets related to both formal friends and ignored players.
     */
    def initializeFriendsAndIgnoredLists(): Unit = {
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
    }

    /**
     * Set up and dispatch a list of `CreateShortcutMessage` packets and a single `ChangeShortcutBankMessage` packet.
     */
    def initializeShortcutsAndBank(guid: PlanetSideGUID): Unit = {
      initializeShortcutsAndBank(guid, avatar.shortcuts)
    }
    /**
     * Set up and dispatch a list of `CreateShortcutMessage` packets and a single `ChangeShortcutBankMessage` packet.
     */
    def initializeShortcutsAndBank(guid: PlanetSideGUID, shortcuts: Array[Option[AvatarShortcut]]): Unit = {
      shortcuts
        .zipWithIndex
        .collect { case (Some(shortcut), index) =>
          sendResponse(CreateShortcutMessage(
            guid,
            index + 1,
            Some(AvatarShortcut.convert(shortcut))
          ))
        }
      sendResponse(ChangeShortcutBankMessage(guid, 0))
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
        obj.OwnerGuid.getOrElse(PlanetSideGUID(0))
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
        sessionLogic.turnCounterFunc = NormalTurnCounter
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
            sessionLogic.general.accessContainer(vehicle)
            sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc
          case _ => ()
            //we can't find a vehicle? and we're still here? that's bad
            player.VehicleSeated = None
        }
        interimUngunnedVehicle = None
        interimUngunnedVehicleSeat = None
        sessionLogic.turnCounterFunc = NormalTurnCounter
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
      loginChatMessage.foreach { msg => sendResponse(ChatMsg(zoningChatMessageType, wideContents=false, "", msg, None)) }
      loginChatMessage.clear()
      CancelZoningProcess()
      sessionLogic.turnCounterFunc = NormalTurnCounter
    }

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

    /**
     * Populate the Character Info window's statistics page during login.
     * Always send campaign (historical, total) statistics.
     * Set to refresh the statistics fields after each respawn from now on.
     */
    private def loginAvatarStatisticsFields(): Unit = {
      avatar.scorecard.KillStatistics.foreach { case (id, stat) =>
        val campaign = CampaignStatistics(stat)
        val elem = StatisticalElement.fromId(id)
        sendResponse(AvatarStatisticsMessage(
          CampaignStatistic(StatisticalCategory.Destroyed, elem, campaign.tr, campaign.nc, campaign.vs, campaign.ps)
        ))
      }
      //originally the client sent a death statistic update in between each change of statistic categories, about 30 times
      sendResponse(AvatarStatisticsMessage(DeathStatistic(ScoreCard.deathCount(avatar.scorecard))))
      statisticsPacketFunc = respawnAvatarStatisticsFields
    }

    /**
     * Populate the Character Info window's statistics page after each respawn.
     * Check whether to send session-related data, or campaign-related data, or both.
     */
    private def respawnAvatarStatisticsFields(): Unit = {
      avatar
        .scorecard
        .KillStatistics
        .flatMap { case (id, stat) =>
          val campaign = CampaignStatistics(stat)
          val session = SessionStatistics(stat)
          (StatisticalElement.fromId(id), campaign.total, campaign, session.total, session) match {
            case (elem, 0, _, _, session) =>
              Seq(SessionStatistic(StatisticalCategory.Destroyed, elem, session.tr, session.nc, session.vs, session.ps))
            case (elem, _, campaign, 0, _) =>
              Seq(CampaignStatistic(StatisticalCategory.Destroyed, elem, campaign.tr, campaign.nc, campaign.vs, campaign.ps))
            case (elem, _, campaign, _, session) =>
              Seq(
                CampaignStatistic(StatisticalCategory.Destroyed, elem, campaign.tr, campaign.nc, campaign.vs, campaign.ps),
                SessionStatistic(StatisticalCategory.Destroyed, elem, session.tr, session.nc, session.vs, session.ps)
              )
          }
        }
        .foreach { statistics =>
          sendResponse(AvatarStatisticsMessage(statistics))
        }
      //originally the client sent a death statistic update in between each change of statistic categories, about 30 times
      sendResponse(AvatarStatisticsMessage(DeathStatistic(ScoreCard.deathCount(avatar.scorecard))))
    }

    /**
     * Accessible method to switch population of the Character Info window's statistics page
     * from whatever it currently is to after each respawn.
     * At the time of "login", only campaign (total, historical) deaths are reported for convenience.
     * At the time of "respawn", all fields - campaign and session - should be reported if applicable.
     */
    def switchAvatarStatisticsFieldToRefreshAfterRespawn(): Unit = {
      statisticsPacketFunc = respawnAvatarStatisticsFields
    }

    /**
     * Don't extract the award advancement information from a player character upon respawning or zoning.
     * You only need to perform that population once at login.
     *
     * @param bundleSize it doesn't matter
     * @param delay      it doesn't matter
     */
    def skipAvatarAwardMessageDelivery(bundleSize: Int, delay: Long): Unit = {}

    /**
     * Extract the award advancement information from a player character, and
     * coordinate timed dispatches of groups of packets.
     *
     * @param bundleSize divide packets into groups of this size
     * @param delay      dispatch packet divisions in intervals
     */
    def setupAvatarAwardMessageDelivery(bundleSize: Int, delay: Long): Unit = {
      setupAvatarAwardMessageDelivery(player, bundleSize, delay)
      populateAvatarAwardRibbonsFunc = skipAvatarAwardMessageDelivery
    }

    /**
     * Extract the merit commendation advancement information from a player character,
     * filter unnecessary or not applicable statistics,
     * translate the information into packet data, and
     * coordinate timed dispatches of groups of packets.
     *
     * @param tplayer    the player character
     * @param bundleSize divide packets into groups of this size
     * @param delay      dispatch packet divisions in intervals
     */
    def setupAvatarAwardMessageDelivery(tplayer: Player, bundleSize: Int, delay: Long): Unit = {
      val date: Int = (System.currentTimeMillis() / 1000L).toInt - 604800 //last week, in seconds
      performAvatarAwardMessageDelivery(
        Award
          .values
          .filter { merit =>
            val label = merit.value
            val alignment = merit.alignment
            if (merit.category == AwardCategory.Exclusive) false
            else if (alignment != PlanetSideEmpire.NEUTRAL && alignment != player.Faction) false
            else if (label.contains("Male") && player.Sex != CharacterSex.Male) false
            else if (label.contains("Female") && player.Sex != CharacterSex.Female) false
            else true
          }
          .flatMap { merit =>
            merit.progression.map { level =>
              AvatarAwardMessage(level.commendation, AwardCompletion(date))
            }
          }
          .grouped(bundleSize)
          .iterator
          .to(Iterable),
        delay
      )
    }

    /**
     * Coordinate timed dispatches of groups of packets.
     *
     * @param messageBundles groups of packets to be dispatched
     * @param delay          dispatch packet divisions in intervals
     */
    def performAvatarAwardMessageDelivery(
                                           messageBundles: Iterable[Iterable[PlanetSideGamePacket]],
                                           delay: Long
                                         ): Unit = {
      messageBundles match {
        case Nil => ()
        case x :: Nil =>
          x.foreach {
            sendResponse
          }
        case x :: xs =>
          x.foreach {
            sendResponse
          }
          context.system.scheduler.scheduleOnce(
            delay.milliseconds,
            context.self,
            ZoningOperations.AvatarAwardMessageBundle(xs, delay)
          )
      }
    }

    /**
     * Update this player avatar for persistence.
     * Set to `persist` when (new) player is loaded.
     */
    def UpdatePersistenceAndRefs(): Unit = {
      sessionLogic.persistFunc()
      sessionLogic.updateOldRefsMap()
    }

    /**
     * Update this player avatar for persistence.
     * Set this to `persistFunc` when persistence is ready.
     * @param persistRef reference to the persistence monitor
     */
    def UpdatePersistence(persistRef: ActorRef)(): Unit = {
      persistRef ! AccountPersistenceService.Update(player.Name, continent, player.Position)
    }

    def startDeconstructing(obj: SpawnTube): Unit = {
      log.info(s"${player.Name} is deconstructing at the ${obj.Owner.Definition.Name}'s spawns")
      avatarActor ! AvatarActor.DeactivateActiveImplants
      if (player.ExoSuit != ExoSuitType.MAX) {
        player.Actor ! PlayerControl.ObjectHeld(Player.HandsDownSlot, updateMyHolsterArm = true)
      }
      nextSpawnPoint = Some(obj) //set fallback
      zoningStatus = Zoning.Status.Deconstructing
      player.allowInteraction = false
      if (player.death_by == 0) {
        player.death_by = 1
      }
      GoToDeploymentMap()
    }

    def stopDeconstructing(): Unit = {
      zoningStatus = Zoning.Status.None
      player.death_by = math.min(player.death_by, 0)
      player.allowInteraction = true
      nextSpawnPoint.foreach { tube =>
        sendResponse(PlayerStateShiftMessage(ShiftState(0, tube.Position, tube.Orientation.z)))
        nextSpawnPoint = None
      }
    }

    def randomRespawn(time: FiniteDuration = 300.seconds): Unit = {
      val faction = player.Faction
      reviveTimer = context.system.scheduler.scheduleOnce(time) {
        cluster ! ICS.GetRandomSpawnPoint(
          Zones.sanctuaryZoneNumber(faction),
          faction,
          Seq(SpawnGroup.Sanctuary),
          context.self
        )
      }
    }

    private def getSpawnTubeOwner: Option[(SpawnTube, Option[PlanetSideGameObject])] = {
      prevSpawnPoint.orElse(nextSpawnPoint).collect { case sp: SpawnTube => (sp, continent.GUID(sp.Owner.GUID)) }
    }

    private def usingSpawnTubeAnimation(): Unit = {
      getSpawnTubeOwner
        .collect { case (sp, owner @ Some(_)) => (sp, owner) }
        .collect {
          case (sp, Some(_: Vehicle)) =>
            ZoningOperations.usingVehicleSpawnTubeAnimation(sp.Zone, sp.WhichSide, sp.Faction, player.Position, sp.Orientation, List(player.Name))
          case (sp, Some(_: Building)) =>
            ZoningOperations.usingFacilitySpawnTubeAnimation(sp.Zone, sp.WhichSide, sp.Faction, player.Position, sp.Orientation, List(player.Name))
        }
    }
  }

  def doorsThatShouldBeClosedOrBeOpenedByRange(
                                                closedPosition: Vector3,
                                                closedRange: Float,
                                                openPosition: Vector3,
                                                openRange: Float
                                              ): Unit = {
    continent
      .blockMap
      .sector(closedPosition, closedRange)
      .amenityList
      .collect { case door: Door if door.isOpen =>
        sendResponse(GenericObjectStateMsg(door.GUID, state=17))
      }
    doorsThatShouldBeOpenInRange(openPosition, openRange)
  }

  def doorsThatShouldBeOpenInRange(position: Vector3, range: Float): Unit = {
    continent
      .blockMap
      .sector(position.xy, range)
      .amenityList
      .collect { case door: Door if door.isOpen =>
        sendResponse(GenericObjectStateMsg(door.GUID, state=16))
      }
  }

  override protected[session] def stop(): Unit = {
    zoningTimer.cancel()
    spawn.respawnTimer.cancel()
    spawn.reviveTimer.cancel()
  }
}
