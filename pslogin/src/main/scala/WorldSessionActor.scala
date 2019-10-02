// Copyright (c) 2017 PSForever
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet._
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.{Logger, MDC}
import MDCContextAware.Implicits._
import csr.{CSRWarp, CSRZone, Traveler}
import net.psforever.objects.GlobalDefinitions._
import services.ServiceManager.Lookup
import net.psforever.objects._
import net.psforever.objects.avatar.{Certification, DeployableToolbox}
import net.psforever.objects.ballistics._
import net.psforever.objects.ce._
import net.psforever.objects.definition._
import net.psforever.objects.definition.converter.{CorpseConverter, DestroyedVehicleConverter}
import net.psforever.objects.equipment.{CItem, _}
import net.psforever.objects.loadouts._
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.pad.process.{AutoDriveControls, VehicleSpawnControlGuided}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.{FacilityTurret, TurretUpgrade, WeaponTurret}
import net.psforever.objects.teamwork.Squad
import net.psforever.objects.vehicles.{AccessPermissionGroup, Cargo, Utility, VehicleLockState, _}
import net.psforever.objects.vital._
import net.psforever.objects.zones.{InterstellarCluster, Zone, ZoneHotSpotProjector}
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.{HotSpotInfo => PacketHotSpotInfo}
import net.psforever.types._
import services.{RemoverActor, vehicle, _}
import services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage, AvatarServiceResponse}
import services.galaxy.{GalaxyResponse, GalaxyServiceResponse}
import services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import services.vehicle.support.TurretUpgrader
import services.vehicle.{VehicleAction, VehicleResponse, VehicleServiceMessage, VehicleServiceResponse}
import services.teamwork.{SquadAction => SquadServiceAction, SquadServiceMessage, SquadServiceResponse, SquadResponse, SquadService}

import scala.collection.mutable.LongMap
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success
import akka.pattern.ask
import net.psforever.objects.vehicles.Utility.InternalTelepad
import services.local.support.{HackCaptureActor, RouterTelepadActivation}
import services.support.SupportActor

class WorldSessionActor extends Actor with MDCContextAware {
  import WorldSessionActor._

  private[this] val log = org.log4s.getLogger
  private[this] val damageLog = org.log4s.getLogger("DamageResolution")
  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var avatarService : ActorRef = ActorRef.noSender
  var localService : ActorRef = ActorRef.noSender
  var vehicleService : ActorRef = ActorRef.noSender
  var galaxyService : ActorRef = ActorRef.noSender
  var squadService : ActorRef = ActorRef.noSender
  var taskResolver : ActorRef = Actor.noSender
  var cluster : ActorRef = Actor.noSender
  var continent : Zone = Zone.Nowhere
  var player : Player = null
  var avatar : Avatar = null
  var progressBarValue : Option[Float] = None
  var shooting : Option[PlanetSideGUID] = None //ChangeFireStateMessage_Start
  var prefire : Option[PlanetSideGUID] = None //if WeaponFireMessage precedes ChangeFireStateMessage_Start
  var accessedContainer : Option[PlanetSideGameObject with Container] = None
  var flying : Boolean = false
  var speed : Float = 1.0f
  var spectator : Boolean = false
  var admin : Boolean = false
  var usingMedicalTerminal : Option[PlanetSideGUID] = None
  var controlled : Option[Int] = None
  //keep track of avatar's ServerVehicleOverride state
  var traveler : Traveler = null
  var deadState : DeadState.Value = DeadState.Dead
  var whenUsedLastKit : Long = 0
  val projectiles : Array[Option[Projectile]] = Array.fill[Option[Projectile]](Projectile.RangeUID - Projectile.BaseUID)(None)
  var drawDeloyableIcon : PlanetSideGameObject with Deployable => Unit = RedrawDeployableIcons
  var updateSquad : () => Unit = NoSquadUpdates
  var recentTeleportAttempt : Long = 0
  var lastTerminalOrderFulfillment : Boolean = true  /**
    * used during zone transfers to maintain reference to seated vehicle (which does not yet exist in the new zone)
    * used during intrazone gate transfers, but not in a way distinct from prior zone transfer procedures
    * should only be set during the transient period when moving between one spawn point and the next
    * leaving set prior to a subsequent transfers may cause unstable vehicle associations, with memory leak potential
    */
  var interstellarFerry : Option[Vehicle] = None
  /**
    * used during zone transfers for cleanup to refer to the vehicle that instigated a transfer
    * "top level" is the carrier in a carrier/ferried association or a projected carrier/(ferried carrier)/ferried association
    * inherited from parent (carrier) to child (ferried) through the `TransferPassenger` message
    * the old-zone unique identifier for the carrier
    * no harm should come from leaving the field set to an old unique identifier value after the transfer period
    */
  var interstellarFerryTopLevelGUID : Option[PlanetSideGUID] = None
  val squadUI : LongMap[SquadUIElement] = new LongMap[SquadUIElement]()
  val squad_supplement_id : Int = 11
  /**
    * `AvatarConverter` can only rely on the `Avatar`-local Looking For Squad variable.
    * When joining or creating a squad, the original state of the avatar's local LFS variable is blanked.
    * This `WSA`-local variable is then used to indicate the ongoing state of the LFS UI component,
    * now called "Looking for Squad Member."
    * Upon leaving or disbanding a squad, this value is made false.
    * Control switching between the `Avatar`-local and the `WSA`-local variable is contingent on `squadUI` being populated.
    */
  var lfs : Boolean = false
  var squadChannel : Option[String] = None
  var squadSetup : () => Unit = FirstTimeSquadSetup

  var amsSpawnPoints : List[SpawnPoint] = Nil
  var clientKeepAlive : Cancellable = DefaultCancellable.obj
  var progressBarUpdate : Cancellable = DefaultCancellable.obj
  var reviveTimer : Cancellable = DefaultCancellable.obj
  var respawnTimer : Cancellable = DefaultCancellable.obj
  var cargoMountTimer : Cancellable = DefaultCancellable.obj
  var cargoDismountTimer : Cancellable = DefaultCancellable.obj
  var antChargingTick : Cancellable = DefaultCancellable.obj
  var antDischargingTick : Cancellable = DefaultCancellable.obj

  /**
    * Convert a boolean value into an integer value.
    * Use: `true:Int` or `false:Int`
    * @param b `true` or `false` (or `null`)
    * @return 1 for `true`; 0 for `false`
    */
  implicit def boolToInt(b : Boolean) : Int = if(b) 1 else 0

  override def postStop() : Unit = {
    //TODO normally, player avatar persists a minute or so after disconnect; we are subject to the SessionReaper
    clientKeepAlive.cancel
    reviveTimer.cancel
    respawnTimer.cancel
    PlayerActionsToCancel()
    localService ! Service.Leave()
    vehicleService ! Service.Leave()
    avatarService ! Service.Leave()
    galaxyService ! Service.Leave()
    LivePlayerList.Remove(sessionId)
    if(player != null && player.HasGUID) {
      squadService ! Service.Leave(Some(player.CharId.toString))
      val player_guid = player.GUID
      //handle orphaned deployables
      DisownDeployables()
      //clean up boomer triggers and telepads
      val equipment = (
        (player.Holsters()
          .zipWithIndex
          .map({ case ((slot, index)) => (index, slot.Equipment) })
          .collect { case ((index, Some(obj))) => InventoryItem(obj, index) }
          ) ++ player.Inventory.Items)
        .filterNot({ case InventoryItem(obj, _) => obj.isInstanceOf[BoomerTrigger] || obj.isInstanceOf[Telepad] })
      //put any temporary value back into the avatar
      //TODO final character save before doing any of this (use equipment)
      continent.Population ! Zone.Population.Release(avatar)
      if(player.isAlive) {
        //actually being alive or manually deconstructing
        player.Position = Vector3.Zero
        //if seated, dismount
        player.VehicleSeated match {
          case Some(_) =>
            //quickly and briefly kill player to avoid disembark animation?
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 0, 0))
            DismountVehicleOnLogOut()
          case _ => ;
        }
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
        taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
        //TODO normally, the actual player avatar persists a minute or so after the user disconnects
      }
      else if(continent.LivePlayers.contains(player) && !continent.Corpses.contains(player)) {
        //player disconnected while waiting for a revive, maybe
        //similar to handling ReleaseAvatarRequestMessage
        player.Release
        player.VehicleSeated match {
          case None =>
            FriskCorpse(player) //TODO eliminate dead letters
            if(!WellLootedCorpse(player)) {
              continent.Population ! Zone.Corpse.Add(player)
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.Release(player, continent))
              taskResolver ! GUIDTask.UnregisterLocker(player.Locker)(continent.GUID) //rest of player will be cleaned up with corpses
            }
            else {
              //no items in inventory; leave no corpse
              val player_guid = player.GUID
              player.Position = Vector3.Zero //save character before doing this
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
              taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
            }

          case Some(_) =>
            val player_guid = player.GUID
            player.Position = Vector3.Zero //save character before doing this
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
            taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
            DismountVehicleOnLogOut()
        }
      }
      //disassociate and start the deconstruction timer for any currently owned vehicle
      SpecialCaseDisownVehicle()
      continent.Population ! Zone.Population.Leave(avatar)
    }
  }

  /**
    * Vehicle cleanup that is specific to log out behavior.
    */
  def DismountVehicleOnLogOut() : Unit = {
    (continent.GUID(player.VehicleSeated) match {
      case Some(obj : Mountable) =>
        (Some(obj), obj.PassengerInSeat(player))
      case _ =>
        (None, None)
    }) match {
      case (Some(mountObj), Some(seatIndex)) =>
        mountObj.Seats(seatIndex).Occupant = None

      case _ => ;
    }
  }

  /**
    * If a vehicle is owned by a character, disassociate the vehicle, then schedule it for deconstruction.
    * @see `DisownVehicle()`
    * @return the vehicle previously owned, if any
    */
  def SpecialCaseDisownVehicle() : Option[Vehicle] = {
    DisownVehicle() match {
      case out @ Some(vehicle) =>
        vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(vehicle), continent))
        vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, continent,
          if(vehicle.Flying) {
            Some(0 seconds) //immediate deconstruction
          }
          else {
            vehicle.Definition.DeconstructionTime //normal deconstruction
          }
        ))
        out
      case None =>
        None
    }
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(inSessionId, pipe) =>
      this.sessionId = inSessionId
      leftRef = sender()
      if(pipe.hasNext) {
        rightRef = pipe.next
        rightRef !> HelloFriend(sessionId, pipe)
      }
      else {
        rightRef = sender()
      }
      context.become(Started)
      ServiceManager.serviceManager ! Lookup("avatar")
      ServiceManager.serviceManager ! Lookup("local")
      ServiceManager.serviceManager ! Lookup("vehicle")
      ServiceManager.serviceManager ! Lookup("taskResolver")
      ServiceManager.serviceManager ! Lookup("cluster")
      ServiceManager.serviceManager ! Lookup("galaxy")
      ServiceManager.serviceManager ! Lookup("squad")

    case _ =>
      log.error("Unknown message")
      context.stop(self)
  }

  def Started : Receive = {
    case ServiceManager.LookupResult("avatar", endpoint) =>
      avatarService = endpoint
      log.info("ID: " + sessionId + " Got avatar service " + endpoint)
    case ServiceManager.LookupResult("local", endpoint) =>
      localService = endpoint
      log.info("ID: " + sessionId + " Got local service " + endpoint)
    case ServiceManager.LookupResult("vehicle", endpoint) =>
      vehicleService = endpoint
      log.info("ID: " + sessionId + " Got vehicle service " + endpoint)
    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      log.info("ID: " + sessionId + " Got task resolver service " + endpoint)
    case ServiceManager.LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.info("ID: " + sessionId + " Got galaxy service " + endpoint)
    case ServiceManager.LookupResult("cluster", endpoint) =>
      cluster = endpoint
      log.info("ID: " + sessionId + " Got cluster service " + endpoint)
    case ServiceManager.LookupResult("squad", endpoint) =>
      squadService = endpoint
      log.info("ID: " + sessionId + " Got squad service " + endpoint)

    case ControlPacket(_, ctrl) =>
      handleControlPkt(ctrl)
    case GamePacket(_, _, pkt) =>
      handleGamePkt(pkt)
    // temporary hack to keep the client from disconnecting
    //it's been a "temporary hack" since 2016 :P
    case PokeClient() =>
      sendResponse(KeepAliveMessage())

    case AvatarServiceResponse(toChannel, guid, reply) =>
      HandleAvatarServiceResponse(toChannel, guid, reply)

    case Door.DoorMessage(tplayer, msg, order) =>
      HandleDoorMessage(tplayer, msg, order)

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
      if(!excluded.exists(_ == avatar.CharId)) {
        response match {
          case SquadResponse.ListSquadFavorite(line, task) =>
            sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), line, SquadAction.ListSquadFavorite(task)))

          case SquadResponse.InitList(infos) =>
            sendResponse(ReplicationStreamMessage(infos))

          case SquadResponse.UpdateList(infos) if infos.nonEmpty =>
            sendResponse(
              ReplicationStreamMessage(6, None,
                infos.map { case (index, squadInfo) =>
                  SquadListing(index, squadInfo)
                }.toVector
              )
            )

          case SquadResponse.RemoveFromList(infos) if infos.nonEmpty =>
            sendResponse(
              ReplicationStreamMessage(1, None,
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

          case SquadResponse.Unknown17(squad, char_id) =>
            sendResponse(
              SquadDefinitionActionMessage(squad.GUID, 0, SquadAction.Unknown(33))
            )

          case SquadResponse.Membership(request_type, unk1, unk2, char_id, opt_char_id, player_name, unk5, unk6) =>
            val name = request_type match {
              case SquadResponseType.Invite if unk5 =>
                //player_name is our name; the name of the player indicated by unk3 is needed
                LivePlayerList.WorldPopulation({ case (_, a : Avatar) => char_id == a.CharId }).headOption match {
                  case Some(player) =>
                    player.name
                  case None =>
                    player_name
                }
              case _ =>
                player_name
            }
            sendResponse(SquadMembershipResponse(request_type, unk1, unk2, char_id, opt_char_id, name, unk5, unk6))

          case SquadResponse.Invite(from_char_id, to_char_id, name) =>
            sendResponse(SquadMembershipResponse(SquadResponseType.Invite, 0, 0, from_char_id, Some(to_char_id), s"$name", false, Some(None)))

          case SquadResponse.WantsSquadPosition(_, name) =>
            sendResponse(
              ChatMsg(
                ChatMessageType.CMT_SQUAD, true, name,
                s"\\#6 would like to join your squad. (respond with \\#3/accept\\#6 or \\#3/reject\\#6)",
                None
              )
            )

          case SquadResponse.Join(squad, positionsToUpdate, toChannel) =>
            val leader = squad.Leader
            val membershipPositions = squad.Membership
              .zipWithIndex
              .filter { case (_, index ) => positionsToUpdate.contains(index) }
            membershipPositions.find({ case(member, _) => member.CharId == avatar.CharId }) match {
              case Some((ourMember, ourIndex)) =>
                //we are joining the squad
                //load each member's entry (our own too)
                membershipPositions.foreach { case(member, index) =>
                  sendResponse(SquadMemberEvent.Add(squad_supplement_id, member.CharId, index, member.Name, member.ZoneId, unk7 = 0))
                  squadUI(member.CharId) = SquadUIElement(member.Name, index, member.ZoneId, member.Health, member.Armor, member.Position)
                }
                //repeat our entry
                sendResponse(SquadMemberEvent.Add(squad_supplement_id, ourMember.CharId, ourIndex, ourMember.Name, ourMember.ZoneId, unk7 = 0)) //repeat of our entry
                //turn lfs off
                val factionOnContinentChannel = s"${continent.Id}/${player.Faction}"
                if(avatar.LFS) {
                  avatar.LFS = false
                  sendResponse(PlanetsideAttributeMessage(player.GUID, 53, 0))
                  avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.PlanetsideAttribute(player.GUID, 53, 0))
                }
                //squad colors
                sendResponse(PlanetsideAttributeMessage(player.GUID, 31, squad_supplement_id))
                avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.PlanetsideAttribute(player.GUID, 31, squad_supplement_id))
                sendResponse(PlanetsideAttributeMessage(player.GUID, 32, ourIndex)) //associate with member position in squad
                //a finalization? what does this do?
                sendResponse(SquadDefinitionActionMessage(squad.GUID, 0, SquadAction.Unknown(18)))
                updateSquad = UpdatesWhenEnrolledInSquad
                squadChannel = Some(toChannel)
              case _ =>
                //other player is joining our squad
                //load each member's entry
                membershipPositions.foreach { case(member, index) =>
                  sendResponse(SquadMemberEvent.Add(squad_supplement_id, member.CharId, index, member.Name, member.ZoneId, unk7 = 0))
                  squadUI(member.CharId) = SquadUIElement(member.Name, index, member.ZoneId, member.Health, member.Armor, member.Position)
                }
            }
            //send an initial dummy update for map icon(s)
            sendResponse(SquadState(PlanetSideGUID(squad_supplement_id),
              membershipPositions
                .filterNot { case (member, _) => member.CharId == avatar.CharId }
                .map{ case (member, _) => SquadStateInfo(member.CharId, member.Health, member.Armor, member.Position, 2,2, false, 429, None,None) }
                .toList
            ))

          case SquadResponse.Leave(squad, positionsToUpdate) =>
            positionsToUpdate.find({ case(member, _) => member == avatar.CharId }) match {
              case Some((ourMember, ourIndex)) =>
                //we are leaving the squad
                //remove each member's entry (our own too)
                positionsToUpdate.foreach { case(member, index) =>
                  sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                  squadUI.remove(member)
                }
                //uninitialize
                sendResponse(SquadMemberEvent.Remove(squad_supplement_id, ourMember, ourIndex)) //repeat of our entry
                sendResponse(PlanetsideAttributeMessage(player.GUID, 31, 0)) //disassociate with squad?
                sendResponse(PlanetsideAttributeMessage(player.GUID, 32, 0)) //disassociate with member position in squad?
                sendResponse(PlanetsideAttributeMessage(player.GUID, 34, 4294967295L)) //unknown, perhaps unrelated?
                lfs = false
                //a finalization? what does this do?
                sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
                updateSquad = NoSquadUpdates
                squadChannel = None
              case _ =>
                //remove each member's entry
                positionsToUpdate.foreach { case(member, index) =>
                  sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                  squadUI.remove(member)
                }
            }

          case SquadResponse.AssignMember(squad, from_index, to_index) =>
            //we've already swapped position internally; now we swap the cards
            SwapSquadUIElements(squad, from_index, to_index)

          case SquadResponse.PromoteMember(squad, char_id, from_index, to_index) =>
            val charId = player.CharId
            val guid = player.GUID
            lazy val factionOnContinentChannel = s"${continent.Id}/${player.Faction}"
            //are we being demoted?
            if(squadUI(charId).index == 0) {
              //lfsm -> lfs
              if(lfs) {
                sendResponse(PlanetsideAttributeMessage(guid, 53, 0))
                avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.PlanetsideAttribute(guid, 53, 0))
              }
              lfs = false
              sendResponse(PlanetsideAttributeMessage(guid, 32, from_index)) //associate with member position in squad
            }
            //are we being promoted?
            else if(charId == char_id) {
              sendResponse(PlanetsideAttributeMessage(guid, 32, 0)) //associate with member position in squad
            }
            avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.PlanetsideAttribute(guid, 31, squad_supplement_id))
            //we must fix the squad cards backend
            SwapSquadUIElements(squad, from_index, to_index)

          case SquadResponse.UpdateMembers(squad, positions) =>
            import services.teamwork.SquadAction.{Update => SAUpdate}
            val pairedEntries = positions.collect {
              case entry if squadUI.contains(entry.char_id) =>
                (entry, squadUI(entry.char_id))
            }
            //prune entries
            val updatedEntries = pairedEntries
              .collect({
                case (entry, element) if entry.zone_number != element.zone =>
                  //zone gets updated for these entries
                  sendResponse(SquadMemberEvent.UpdateZone(squad_supplement_id, entry.char_id, element.index, entry.zone_number))
                  squadUI(entry.char_id) = SquadUIElement(element.name, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                  entry
                case (entry, element) if entry.health != element.health || entry.armor != element.armor || entry.pos != element.position =>
                  //other elements that need to be updated
                  squadUI(entry.char_id) = SquadUIElement(element.name, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                  entry
              })
              .filterNot(_.char_id == avatar.CharId) //we want to update our backend, but not our frontend
            if(updatedEntries.nonEmpty) {
              sendResponse(
                SquadState(
                  PlanetSideGUID(squad_supplement_id),
                  updatedEntries.map { entry => SquadStateInfo(entry.char_id, entry.health, entry.armor, entry.pos, 2,2, false, 429, None,None)}
                )
              )
            }

          case SquadResponse.SquadSearchResults() =>
            //I don't actually know how to return search results
            sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.NoSquadSearchResults()))

          case SquadResponse.InitWaypoints(char_id, waypoints) =>
            StartBundlingPackets()
            waypoints.foreach { case (waypoint_type, info, unk) =>
              sendResponse(SquadWaypointEvent.Add(squad_supplement_id, char_id, waypoint_type, WaypointEvent(info.zone_number, info.pos, unk)))
            }
            StopBundlingPackets()

          case SquadResponse.WaypointEvent(WaypointEventAction.Add, char_id, waypoint_type, _, Some(info), unk) =>
            sendResponse(SquadWaypointEvent.Add(squad_supplement_id, char_id, waypoint_type, WaypointEvent(info.zone_number, info.pos, unk)))

          case SquadResponse.WaypointEvent(WaypointEventAction.Remove, char_id, waypoint_type, _, _, _) =>
            sendResponse(SquadWaypointEvent.Remove(squad_supplement_id, char_id, waypoint_type))

          case _ => ;
        }
      }

    case Deployment.CanDeploy(obj, state) =>
      val vehicle_guid = obj.GUID
      //TODO remove this arbitrary allowance angle when no longer helpful
      if(obj.Orientation.x > 30 && obj.Orientation.x < 330) {
        obj.DeploymentState = DriveState.Mobile
        CanNotChangeDeployment(obj, state, "ground too steep")
      }
      else if(state == DriveState.Deploying) {
        log.info(s"DeployRequest: $obj transitioning to deploy state")
        obj.Velocity = Some(Vector3.Zero) //no velocity
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        DeploymentActivities(obj)
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(obj.DeployTime milliseconds, obj.Actor, Deployment.TryDeploy(DriveState.Deployed))
      }
      else if(state == DriveState.Deployed) {
        log.info(s"DeployRequest: $obj has been Deployed")
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        DeploymentActivities(obj)
        //...
      }
      else {
        CanNotChangeDeployment(obj, state, "incorrect deploy state")
      }

    case Deployment.CanUndeploy(obj, state) =>
      val vehicle_guid = obj.GUID
      if(state == DriveState.Undeploying) {
        log.info(s"DeployRequest: $obj transitioning to undeploy state")
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        DeploymentActivities(obj)
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(obj.UndeployTime milliseconds, obj.Actor, Deployment.TryUndeploy(DriveState.Mobile))
      }
      else if(state == DriveState.Mobile) {
        log.info(s"DeployRequest: $obj is Mobile")
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        DeploymentActivities(obj)
        //...
      }
      else {
        CanNotChangeDeployment(obj, state, "incorrect undeploy state")
      }

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      CanNotChangeDeployment(obj, state, reason)

    case ResourceSilo.ResourceSiloMessage(tplayer, msg, order) =>
      val vehicle_guid = msg.avatar_guid
      val silo_guid = msg.object_guid
      order match {
        case ResourceSilo.ChargeEvent() =>
          antChargingTick.cancel() // If an ANT is refilling an NTU silo it isn't in a warpgate, so disable NTU regeneration
          antDischargingTick.cancel()
          antDischargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuDischarging(player, continent.GUID(vehicle_guid).get.asInstanceOf[Vehicle], silo_guid))
      }

    case VehicleSpawnPad.StartPlayerSeatedInVehicle(vehicle, pad) =>
      val vehicle_guid = vehicle.GUID
      PlayerActionsToCancel()
      if(player.VisibleSlots.contains(player.DrawnSlot)) {
        player.DrawnSlot = Player.HandsDownSlot
        sendResponse(ObjectHeldMessage(player.GUID, Player.HandsDownSlot, true))
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectHeld(player.GUID, player.LastDrawnSlot))
      }
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 1L)) //mount points off
      sendResponse(PlanetsideAttributeMessage(player.GUID, 21, vehicle_guid)) //ownership

    case VehicleSpawnPad.PlayerSeatedInVehicle(vehicle, pad) =>
      val vehicle_guid = vehicle.GUID
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 0L)) //mount points on
      ReloadVehicleAccessPermissions(vehicle)
      ServerVehicleLock(vehicle)

    case VehicleSpawnPad.ServerVehicleOverrideStart(vehicle, pad) =>
      val vdef = vehicle.Definition
      if(vehicle.Seats(0).isOccupied) {
        // todo: shouldn't the vehicle already be detached by this point? I'm going to disable the below for now as it's causing duplicate ODM packets
//        sendResponse(ObjectDetachMessage(pad.GUID, vehicle.GUID, pad.Position + Vector3.z(0.5f), pad.Orientation.z))
      }
      ServerVehicleOverride(vehicle, vdef.AutoPilotSpeed1, GlobalDefinitions.isFlightVehicle(vdef) : Int)

    case VehicleSpawnControlGuided.GuidedControl(cmd, vehicle, data) =>
      cmd match {
        case AutoDriveControls.State.Drive =>
          val speed : Int = data.getOrElse({
            vehicle.Definition.AutoPilotSpeed1
          }).asInstanceOf[Int]
          ServerVehicleOverride(vehicle, speed)

        case AutoDriveControls.State.Climb =>
          ServerVehicleOverride(vehicle, controlled.getOrElse(0), GlobalDefinitions.isFlightVehicle(vehicle.Definition) : Int)

        case AutoDriveControls.State.Turn =>
          //TODO how to turn hovering/flying vehicle?
          val direction = data.getOrElse(15).asInstanceOf[Int]
          sendResponse(VehicleStateMessage(vehicle.GUID, 0, vehicle.Position, vehicle.Orientation, vehicle.Velocity, None, 0, 0, direction, false, false))

        case AutoDriveControls.State.Stop =>
          ServerVehicleOverride(vehicle, 0)

        case _ => ;
      }

    case VehicleSpawnPad.ServerVehicleOverrideEnd(vehicle, pad) =>
      sendResponse(GenericObjectActionMessage(pad.GUID, 92)) //reset spawn pad
      DriverVehicleControl(vehicle, vehicle.Definition.AutoPilotSpeed2)

    case VehicleSpawnPad.PeriodicReminder(cause, data) =>
      val msg : String = (cause match {
        case VehicleSpawnPad.Reminders.Blocked =>
          s"The vehicle spawn where you placed your order is blocked. ${data.getOrElse("")}"
        case VehicleSpawnPad.Reminders.Queue =>
          s"Your position in the vehicle spawn queue is ${data.getOrElse("last")}."
        case VehicleSpawnPad.Reminders.Cancelled =>
          "Your vehicle order has been cancelled."
      })
      sendResponse(ChatMsg(ChatMessageType.CMT_OPEN, true, "", msg, None))

    case CheckCargoDismount(cargo_guid, carrier_guid, mountPoint, iteration) =>
      HandleCheckCargoDismounting(cargo_guid, carrier_guid, mountPoint, iteration)

    case CheckCargoMounting(cargo_guid, carrier_guid, mountPoint, iteration) =>
      HandleCheckCargoMounting(cargo_guid, carrier_guid, mountPoint, iteration)

    case ListAccountCharacters =>
      import net.psforever.objects.definition.converter.CharacterSelectConverter
      val gen : AtomicInteger = new AtomicInteger(1)
      val converter : CharacterSelectConverter = new CharacterSelectConverter
      //load characters
      SetCharacterSelectScreenGUID(player, gen)
      val health = player.Health
      val stamina = player.Stamina
      val armor = player.Armor
      player.Spawn
      sendResponse(
        ObjectCreateDetailedMessage(ObjectClass.avatar, player.GUID, converter.DetailedConstructorData(player).get)
      )
      if(health > 0) {
        //player can not be dead; stay spawned as alive
        player.Health = health
        player.Stamina = stamina
        player.Armor = armor
      }
      sendResponse(CharacterInfoMessage(15, PlanetSideZoneID(10000), avatar.CharId, player.GUID, false, 6404428))
      RemoveCharacterSelectScreenGUID(player)
      sendResponse(CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0))
      sendResponse(CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0))

    case VehicleLoaded(_ /*vehicle*/) => ;
    //currently being handled by VehicleSpawnPad.LoadVehicle during testing phase

    case Zone.ClientInitialization(zone) =>
      val continentNumber = zone.Number
      val poplist = zone.Players
      val popBO = 0
      //TODO black ops test (partition)
      val popTR = poplist.count(_.faction == PlanetSideEmpire.TR)
      val popNC = poplist.count(_.faction == PlanetSideEmpire.NC)
      val popVS = poplist.count(_.faction == PlanetSideEmpire.VS)
      // StopBundlingPackets() is called on ClientInitializationComplete
      StartBundlingPackets()
      zone.Buildings.foreach({ case (id, building) => initBuilding(continentNumber, building.MapId, building) })
      sendResponse(ZonePopulationUpdateMessage(continentNumber, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))
      sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NEUTRAL))
      //CaptureFlagUpdateMessage()
      //VanuModuleUpdateMessage()
      //ModuleLimitsMessage()
      sendResponse(ZoneInfoMessage(continentNumber, true, 0))
      sendResponse(ZoneLockInfoMessage(continentNumber, false, true))
      sendResponse(ZoneForcedCavernConnectionsMessage(continentNumber, 0))
      sendResponse(HotSpotUpdateMessage(
        continentNumber,
        1,
        ZoneHotSpotProjector.SpecificHotSpotInfo(player.Faction, zone.HotSpots)
          .map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
      )) //normally set for all zones in bulk; should be fine manually updating per zone like this

    case Zone.Population.PlayerHasLeft(zone, None) =>
      log.info(s"$avatar does not have a body on ${zone.Id}")

    case Zone.Population.PlayerHasLeft(zone, Some(tplayer)) =>
      if(tplayer.isAlive) {
        log.info(s"$tplayer has left zone ${zone.Id}")
      }

    case Zone.Population.PlayerCanNotSpawn(zone, tplayer) =>
      log.warn(s"$tplayer can not spawn in zone ${zone.Id}; why?")

    case Zone.Population.PlayerAlreadySpawned(zone, tplayer) =>
      log.warn(s"$tplayer is already spawned on zone ${zone.Id}; a clerical error?")

    case Zone.Lattice.SpawnPoint(zone_id, spawn_tube) =>
      var (pos, ori) = spawn_tube.SpecificPoint(continent.GUID(player.VehicleSeated) match {
        case Some(obj : Vehicle) if obj.Health > 0 =>
          obj
        case _ =>
          player
      })
      spawn_tube.Owner match {
        case building : Building =>
          log.info(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id in building ${building.MapId} selected")
        case vehicle : Vehicle =>
          log.info(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id at ams ${vehicle.GUID.guid} selected")
        case owner =>
          log.warn(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id at ${spawn_tube.Position} has unexpected owner $owner")
      }
      LoadZonePhysicalSpawnPoint(zone_id, pos, ori, CountSpawnDelay(zone_id, spawn_tube, continent.Id))

    case Zone.Lattice.NoValidSpawnPoint(zone_number, None) =>
      log.warn(s"Zone.Lattice.SpawnPoint: zone $zone_number could not be accessed as requested")
      reviveTimer.cancel
      RequestSanctuaryZoneSpawn(player, zone_number)

    case Zone.Lattice.NoValidSpawnPoint(zone_number, Some(spawn_group)) =>
      log.warn(s"Zone.Lattice.SpawnPoint: zone $zone_number has no available ${player.Faction} targets in spawn group $spawn_group")
      reviveTimer.cancel
      if(spawn_group == 2) {
        sendResponse(ChatMsg(ChatMessageType.CMT_OPEN, false, "", "No friendly AMS is deployed in this region.", None))
        cluster ! Zone.Lattice.RequestSpawnPoint(zone_number, player, 0)
      }
      else {
        RequestSanctuaryZoneSpawn(player, zone_number)
      }

    case msg @ Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      log.warn(s"$msg")

    case msg @ Zone.Vehicle.CanNotDespawn(zone, vehicle, reason) =>
      log.warn(s"$msg")

    case Zone.Ground.ItemOnGround(item : BoomerTrigger, pos, orient) =>
      //dropped the trigger, no longer own the boomer; make certain whole faction is aware of that
      val playerGUID = player.GUID
      continent.GUID(item.Companion) match {
        case Some(obj : BoomerDeployable) =>
          val guid = obj.GUID
          val factionOnContinentChannel = s"${continent.Id}/${player.Faction}"
          obj.AssignOwnership(None)
          avatar.Deployables.Remove(obj)
          UpdateDeployableUIElements(avatar.Deployables.UpdateUIElement(obj.Definition.Item))
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent))
          sendResponse(SetEmpireMessage(guid, PlanetSideEmpire.NEUTRAL))
          avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.SetEmpire(playerGUID, guid, PlanetSideEmpire.NEUTRAL))
          val info = DeployableInfo(guid, DeployableIcon.Boomer, obj.Position, PlanetSideGUID(0))
          sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Dismiss, info))
          localService ! LocalServiceMessage(factionOnContinentChannel, LocalAction.DeployableMapIcon(playerGUID, DeploymentAction.Dismiss, info))
          PutItemOnGround(item, pos, orient)
        case Some(_) | None =>
          //pointless trigger
          val guid = item.GUID
          continent.Ground ! Zone.Ground.RemoveItem(guid) //undo; no callback
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(PlanetSideGUID(0), guid))
          taskResolver ! GUIDTask.UnregisterObjectTask(item)(continent.GUID)
      }

    case Zone.Ground.ItemOnGround(item : ConstructionItem, pos, orient) =>
      //defensively, reset CItem configuration
      item.FireModeIndex = 0
      item.AmmoTypeIndex = 0
      PutItemOnGround(item, pos, orient)

    case Zone.Ground.ItemOnGround(item : PlanetSideGameObject, pos, orient) =>
      PutItemOnGround(item, pos, orient)

    case Zone.Ground.CanNotDropItem(zone, item, reason) =>
      log.warn(s"DropItem: $player tried to drop a $item on the ground, but $reason")

    case Zone.Ground.ItemInHand(item : BoomerTrigger) =>
      if(PutItemInHand(item)) {
        //pick up the trigger, own the boomer; make certain whole faction is aware of that
        continent.GUID(item.Companion) match {
          case Some(obj : BoomerDeployable) =>
            val guid = obj.GUID
            val playerGUID = player.GUID
            val faction = player.Faction
            val factionOnContinentChannel = s"${continent.Id}/${faction}"
            obj.AssignOwnership(player)
            obj.Faction = faction
            avatar.Deployables.Add(obj)
            UpdateDeployableUIElements(avatar.Deployables.UpdateUIElement(obj.Definition.Item))
            localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(obj), continent))
            sendResponse(SetEmpireMessage(guid, faction))
            avatarService ! AvatarServiceMessage(factionOnContinentChannel, AvatarAction.SetEmpire(playerGUID, guid, faction))
            val info = DeployableInfo(obj.GUID, DeployableIcon.Boomer, obj.Position, obj.Owner.get)
            sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, info))
            localService ! LocalServiceMessage(factionOnContinentChannel, LocalAction.DeployableMapIcon(playerGUID, DeploymentAction.Build, info))
          case Some(_) | None => ; //pointless trigger; see Zone.Ground.ItemOnGround(BoomerTrigger, ...)
        }
      }

    case Zone.Ground.ItemInHand(item : Equipment) =>
      PutItemInHand(item)

    case Zone.Ground.CanNotPickupItem(zone, item_guid, _) =>
      zone.GUID(item_guid) match {
        case Some(item) =>
          log.warn(s"DropItem: finding a $item on the ground was suggested, but $player can not reach it")
        case None =>
          log.warn(s"DropItem: finding an item ($item_guid) on the ground was suggested, but $player can not see it")
      }

    case Zone.Deployable.DeployableIsBuilt(obj, tool) =>
      val index = player.Find(tool) match {
        case Some(x) =>
          x
        case None =>
          player.LastDrawnSlot
      }
      if(avatar.Deployables.Accept(obj) || (avatar.Deployables.Valid(obj) && !avatar.Deployables.Contains(obj))) {
        tool.Definition match {
          case GlobalDefinitions.ace =>
            localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerEffectLocation(player.GUID, "spawn_object_effect", obj.Position, obj.Orientation))
          case GlobalDefinitions.advanced_ace =>
            sendResponse(GenericObjectActionMessage(player.GUID, 212)) //put fdu down; it will be removed from the client's holster
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PutDownFDU(player.GUID))
          case GlobalDefinitions.router_telepad => ;
          case _ =>
            log.warn(s"Zone.Deployable.DeployableIsBuilt: not sure what kind of construction item to animate - ${tool.Definition}")
        }
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(
          obj.Definition.DeployTime milliseconds,
          self,
          WorldSessionActor.FinalizeDeployable(obj, tool, index)
        )
      }
      else {
        TryDropConstructionTool(tool, index, obj.Position)
        sendResponse(ObjectDeployedMessage.Failure(obj.Definition.Name))
        obj.Position = Vector3.Zero
        obj.AssignOwnership(None)
        continent.Deployables ! Zone.Deployable.Dismiss(obj)
      }

    case WorldSessionActor.FinalizeDeployable(obj : TurretDeployable, tool, index) =>
      //spitfires and deployable field turrets
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : ComplexDeployable, tool, index) =>
      //deployable_shield_generator
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : BoomerDeployable, tool, index) =>
      //boomers
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      //TODO sufficiently delete the tool
      sendResponse(ObjectDeleteMessage(tool.GUID, 0))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, tool.GUID))
      taskResolver ! GUIDTask.UnregisterEquipment(tool)(continent.GUID)
      val trigger = new BoomerTrigger
      trigger.Companion = obj.GUID
      obj.Trigger = trigger
      val holster = player.Slot(index)
      if(holster.Equipment.contains(tool)) {
        holster.Equipment = None
        taskResolver ! DelayedObjectHeld(player, index, List(PutEquipmentInSlot(player, trigger, index)))
      }
      else {
        //don't know where boomer trigger should go; drop it on the ground
        taskResolver ! NewItemDrop(player, continent, avatarService)(trigger)
      }
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : ExplosiveDeployable, tool, index) =>
      //mines
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : SensorDeployable, tool, index) =>
      //motion alarm sensor and sensor disruptor
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerEffectInfo(player.GUID, "on", obj.GUID, true, 1000))
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : TelepadDeployable, tool, index) =>
      StartBundlingPackets()
      if(obj.Health > 0) {
        val guid = obj.GUID
        //router telepad deployable
        val router = tool.asInstanceOf[Telepad].Router
        //router must exist and be deployed
        continent.GUID(router) match {
          case Some(vehicle : Vehicle) =>
            val routerGUID = router.get
            if(vehicle.Health == 0) {
              //the Telepad was successfully deployed; but, before it could configure, its Router was destroyed
              sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", "@Telepad_NoDeploy_RouterLost", None))
              localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds)))
            }
            else {
              log.info(s"FinalizeDeployable: setup for telepad #${guid.guid} in zone ${continent.Id}")
              obj.Router = routerGUID //necessary; forwards link to the router
              DeployableBuildActivity(obj)
              CommonDestroyConstructionItem(tool, index)
              StopBundlingPackets()
              //it takes 60s for the telepad to become properly active
              localService ! LocalServiceMessage.Telepads(RouterTelepadActivation.AddTask(obj, continent))
            }

          case _ =>
            //the Telepad was successfully deployed; but, before it could configure, its Router was deconstructed
            sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", "@Telepad_NoDeploy_RouterLost", None))
            localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds)))
        }
      }
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : SimpleDeployable, tool, index) =>
      //tank_trap
      StartBundlingPackets()
      DeployableBuildActivity(obj)
      CommonDestroyConstructionItem(tool, index)
      FindReplacementConstructionItem(tool, index)
      StopBundlingPackets()

    case WorldSessionActor.FinalizeDeployable(obj : PlanetSideGameObject with Deployable, tool, index) =>
      val guid = obj.GUID
      val definition = obj.Definition
      StartBundlingPackets()
      sendResponse(GenericObjectActionMessage(guid, 84)) //reset build cooldown
      sendResponse(ObjectDeployedMessage.Failure(definition.Name))
      log.warn(s"FinalizeDeployable: deployable ${definition.asInstanceOf[DeployableDefinition].Item}@$guid not handled by specific case")
      log.warn(s"FinalizeDeployable: deployable will be cleaned up, but may not get unregistered properly")
      TryDropConstructionTool(tool, index, obj.Position)
      obj.Position = Vector3.Zero
      continent.Deployables ! Zone.Deployable.Dismiss(obj)
      StopBundlingPackets()

    //!!only dispatch Zone.Deployable.Dismiss from WSA as cleanup if the target deployable was never fully introduced
    case Zone.Deployable.DeployableIsDismissed(obj : TurretDeployable) =>
      taskResolver ! GUIDTask.UnregisterDeployableTurret(obj)(continent.GUID)

    //!!only dispatch Zone.Deployable.Dismiss from WSA as cleanup if the target deployable was never fully introduced
    case Zone.Deployable.DeployableIsDismissed(obj) =>
      taskResolver ! GUIDTask.UnregisterObjectTask(obj)(continent.GUID)

    case InterstellarCluster.ClientInitializationComplete() =>
      StopBundlingPackets()
      LivePlayerList.Add(sessionId, avatar)
      traveler = new Traveler(self, continent.Id)
      //PropertyOverrideMessage
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
      sendResponse(FriendsResponse(FriendAction.InitializeFriendList, 0, true, true, Nil))
      sendResponse(FriendsResponse(FriendAction.InitializeIgnoreList, 0, true, true, Nil))
      avatarService ! Service.Join(avatar.name) //channel will be player.Name
      localService ! Service.Join(avatar.name) //channel will be player.Name
      vehicleService ! Service.Join(avatar.name) //channel will be player.Name
      galaxyService ! Service.Join("galaxy") //for galaxy-wide messages
      galaxyService ! Service.Join(s"${avatar.faction}") //for hotspots
      squadService ! Service.Join(s"${avatar.faction}") //channel will be player.Faction
      squadService ! Service.Join(s"${avatar.CharId}") //channel will be player.CharId (in order to work with packets)
      squadService ! Service.Join(s"${avatar.faction}") //channel will be player.Faction
      cluster ! InterstellarCluster.GetWorld("home3")

    case InterstellarCluster.GiveWorld(zoneId, zone) =>
      log.info(s"Zone $zoneId will now load")
      val continentId = continent.Id
      val factionOnContinentChannel = s"$continentId/${avatar.faction}"
      avatarService ! Service.Leave(Some(continentId))
      avatarService ! Service.Leave(Some(factionOnContinentChannel))
      localService ! Service.Leave(Some(continentId))
      localService ! Service.Leave(Some(factionOnContinentChannel))
      vehicleService ! Service.Leave(Some(continentId))
      vehicleService ! Service.Leave(Some(factionOnContinentChannel))
      player.Continent = zoneId
      continent = zone
      continent.Population ! Zone.Population.Join(avatar)
      interstellarFerry match {
        case Some(vehicle) if vehicle.PassengerInSeat(player).contains(0) =>
          taskResolver ! RegisterDrivenVehicle(vehicle, player)
        case _ =>
          taskResolver ! RegisterNewAvatar(player)
      }

    case NewPlayerLoaded(tplayer) =>
      //new zone
      log.info(s"Player ${tplayer.Name} has been loaded")
      player = tplayer
      //LoadMapMessage causes the client to send BeginZoningMessage, eventually leading to SetCurrentAvatar
      sendResponse(LoadMapMessage(continent.Map.Name, continent.Id, 40100, 25, true, 3770441820L))
      AvatarCreate() //important! the LoadMapMessage must be processed by the client before the avatar is created

    case PlayerLoaded(tplayer) =>
      //same zone
      log.info(s"Player ${tplayer.Name} will respawn")
      player = tplayer
      AvatarCreate()
      self ! SetCurrentAvatar(tplayer)

    case PlayerFailedToLoad(tplayer) =>
      player.Continent match {
        case _ =>
          failWithError(s"${tplayer.Name} failed to load anywhere")
      }

    case UnregisterCorpseOnVehicleDisembark(corpse) =>
      if(!corpse.isAlive && corpse.HasGUID) {
        corpse.VehicleSeated match {
          case Some(_) =>
            import scala.concurrent.ExecutionContext.Implicits.global
            context.system.scheduler.scheduleOnce(50 milliseconds, self, UnregisterCorpseOnVehicleDisembark(corpse))
          case None =>
            taskResolver ! GUIDTask.UnregisterPlayer(corpse)(continent.GUID)
        }
      }

    case SetCurrentAvatar(tplayer) =>
      HandleSetCurrentAvatar(tplayer)

    case NtuCharging(tplayer, vehicle) =>
      HandleNtuCharging(tplayer, vehicle)

    case NtuDischarging(tplayer, vehicle, silo_guid) =>
      HandleNtuDischarging(tplayer, vehicle, silo_guid)

    case HackingProgress(progressType, tplayer, target, tool_guid, delta, completeAction, tickAction) =>
      HandleHackingProgress(progressType, tplayer, target, tool_guid, delta, completeAction, tickAction)

    case Vitality.DamageResolution(target : Vehicle) =>
      HandleVehicleDamageResolution(target)

    case Vitality.DamageResolution(target : TrapDeployable) =>
      //tank_traps
      val guid = target.GUID
      val health = target.Health
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(guid, 0, health))
      if(health <= 0) {
        AnnounceDestroyDeployable(target, None)
      }

    case Vitality.DamageResolution(target : SensorDeployable) =>
      //sensors
      val guid = target.GUID
      val health = target.Health
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(guid, 0, health))
      if(health <= 0) {
        AnnounceDestroyDeployable(target, Some(0 seconds))
      }

    case Vitality.DamageResolution(target : SimpleDeployable) =>
      //boomers, mines
      if(target.Health <= 0) {
        //update if destroyed
        val guid = target.GUID
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, guid))
        AnnounceDestroyDeployable(target, Some(0 seconds))
      }

    case Vitality.DamageResolution(target : TurretDeployable) =>
      HandleTurretDeployableDamageResolution(target)

    case Vitality.DamageResolution(target : ComplexDeployable) =>
      //shield_generators
      val health = target.Health
      val guid = target.GUID
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(guid, 0, health))
      if(health <= 0) {
        AnnounceDestroyDeployable(target, None)
      }

    case Vitality.DamageResolution(target : FacilityTurret) =>
      HandleFacilityTurretDamageResolution(target)

    case Vitality.DamageResolution(target : PlanetSideGameObject) =>
      log.warn(s"Vital target ${target.Definition.Name} damage resolution not supported using this method")

    case Vehicle.UpdateShieldsCharge(vehicle) =>
      vehicleService ! VehicleServiceMessage(s"${vehicle.Actor}", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, 68, vehicle.Shields))

    case ResponseToSelf(pkt) =>
      log.info(s"Received a direct message: $pkt")
      sendResponse(pkt)

    case default =>
      log.warn(s"Invalid packet class received: $default from $sender")
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleAvatarServiceResponse(toChannel : String, guid : PlanetSideGUID, reply : AvatarResponse.Response) : Unit = {
    val tplayer_guid = if(player.HasGUID) player.GUID
    else PlanetSideGUID(0)
    reply match {
      case AvatarResponse.SendResponse(msg) =>
        sendResponse(msg)

      case AvatarResponse.ArmorChanged(suit, subtype) =>
        if(tplayer_guid != guid) {
          sendResponse(ArmorChangedMessage(guid, suit, subtype))
        }

      case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) =>
        if(tplayer_guid != guid) {
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
        if(tplayer_guid != guid) {
          sendResponse(ChangeFireModeMessage(item_guid, mode))
        }

      case AvatarResponse.ChangeFireState_Start(weapon_guid) =>
        if(tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Start(weapon_guid))
        }

      case AvatarResponse.ChangeFireState_Stop(weapon_guid) =>
        if(tplayer_guid != guid) {
          sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
        }

      case AvatarResponse.ConcealPlayer() =>
        if(tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(guid, 36))
        }

      case AvatarResponse.DamageResolution(target, resolution_function) =>
        if(player.isAlive) {
          val originalHealth = player.Health
          val originalArmor = player.Armor
          resolution_function(target)
          val health = player.Health
          val armor = player.Armor
          val damageToHealth = originalHealth - health
          val damageToArmor = originalArmor - armor
          damageLog.info(s"${player.Name}-infantry: BEFORE=$originalHealth/$originalArmor, AFTER=$health/$armor, CHANGE=$damageToHealth/$damageToArmor")
          if(damageToHealth != 0 || damageToArmor != 0) {
            val playerGUID = player.GUID
            sendResponse(PlanetsideAttributeMessage(playerGUID, 0, health))
            sendResponse(PlanetsideAttributeMessage(playerGUID, 4, armor))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(playerGUID, 0, health))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(playerGUID, 4, armor))
            if(health == 0 && player.isAlive) {
              KillPlayer(player)
            }
            else {
              //first damage entry -> most recent damage source -> killing blow
              target.History.find(p => p.isInstanceOf[DamagingActivity]) match {
                case Some(data : DamageFromProjectile) =>
                  val owner = data.data.projectile.owner
                  owner match {
                    case pSource : PlayerSource =>
                      continent.LivePlayers.find(_.Name == pSource.Name) match {
                        case Some(tplayer) =>
                          sendResponse(HitHint(tplayer.GUID, player.GUID))
                        case None => ;
                      }
                    case vSource : SourceEntry =>
                      sendResponse(DamageWithPositionMessage(damageToHealth + damageToArmor, vSource.Position))
                    case _ => ;
                  }
                  continent.Activity ! Zone.HotSpot.Activity(owner, data.Target, target.Position)
                case _ => ;
              }
            }
          }
        }

      case AvatarResponse.Destroy(victim, killer, weapon, pos) =>
        // guid = victim // killer = killer ;)
        sendResponse(DestroyMessage(victim, killer, weapon, pos))

      case AvatarResponse.DestroyDisplay(killer, victim, method, unk) =>
        sendResponse(DestroyDisplayMessage(killer, victim, method, unk))

      case AvatarResponse.DropItem(pkt) =>
        if(tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.EquipmentInHand(pkt) =>
        if(tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.HitHint(source_guid) =>
        if(player.isAlive) {
          sendResponse(HitHint(source_guid, guid))
        }

      case AvatarResponse.KilledWhileInVehicle() =>
        if(player.isAlive && player.VehicleSeated.nonEmpty) {
          (continent.GUID(player.VehicleSeated) match {
            case Some(obj : Vehicle) =>
              if(obj.Health == 0) Some(obj)
              else None
            case Some(obj : TurretDeployable) =>
              if(obj.Health == 0) Some(obj)
              else None
            case Some(obj : FacilityTurret) =>
              if(obj.Health == 1) Some(obj) //TODO proper turret death at 0 health
              else None
            case _ =>
              None
          }) match {
            case Some(obj : PlanetSideGameObject with Vitality) =>
              obj.LastShot match {
                case Some(cause) =>
                  player.History(cause)
                  KillPlayer(player)
                case None => ;
              }
            case _ =>
              log.warn(s"${player.Name} was seated in a vehicle and should have been killed, but was not; suicidal fallback")
              Suicide(player)
          }
        }

      case AvatarResponse.LoadPlayer(pkt) =>
        if(tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case AvatarResponse.ObjectDelete(item_guid, unk) =>
        if(tplayer_guid != guid) {
          sendResponse(ObjectDeleteMessage(item_guid, unk))
        }

      case AvatarResponse.ObjectHeld(slot) =>
        if(tplayer_guid != guid) {
          sendResponse(ObjectHeldMessage(guid, slot, false))
        }

      case AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value) =>
        if(tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
        }

      case AvatarResponse.PlayerState(msg, spectating, weaponInHand) =>
        if(tplayer_guid != guid) {
          val now = System.currentTimeMillis()
          val (location, time, distanceSq) : (Vector3, Long, Float) = if(spectating) {
            (Vector3(2, 2, 2), 0L, 0f)
          }
          else {
            val before = player.lastSeenStreamMessage(guid.guid)
            val dist = Vector3.DistanceSquared(player.Position, msg.pos)
            (msg.pos, now - before, dist)
          }
          if(spectating ||
            ((distanceSq < 900 || weaponInHand) && time > 200) ||
            (distanceSq < 10000 && time > 500) ||
            (distanceSq < 160000 && (
              (msg.is_jumping || time < 200)) ||
              ((msg.vel.isEmpty || Vector3.MagnitudeSquared(msg.vel.get).toInt == 0) && time > 2000) ||
              (time > 1000)) ||
            (distanceSq > 160000 && time > 5000)) {
            sendResponse(
              PlayerStateMessage(
                guid,
                location,
                msg.vel,
                msg.facingYaw,
                msg.facingPitch,
                msg.facingYawUpper,
                timestamp = 0,
                msg.is_crouching,
                msg.is_jumping,
                msg.jump_thrust,
                msg.is_cloaked
              )
            )
            player.lastSeenStreamMessage(guid.guid) = now
          }
        }

      case AvatarResponse.PutDownFDU(target) =>
        if(tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(target, 212))
        }

      case AvatarResponse.Release(tplayer) =>
        if(tplayer_guid != guid) {
          TurnPlayerIntoCorpse(tplayer)
        }

      case AvatarResponse.Reload(item_guid) =>
        if(tplayer_guid != guid) {
          sendResponse(ReloadMessage(item_guid, 1, 0))
        }

      case AvatarResponse.SetEmpire(object_guid, faction) =>
        if(tplayer_guid != guid) {
          sendResponse(SetEmpireMessage(object_guid, faction))
        }

      case AvatarResponse.StowEquipment(target, slot, item) =>
        if(tplayer_guid != guid) {
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
        if(tplayer_guid != guid) {
          sendResponse(WeaponDryFireMessage(weapon_guid))
        }

      case _ => ;
    }
  }

  /**
    * na
    * @param tplayer na
    * @param msg na
    * @param order na
    */
  def HandleDoorMessage(tplayer : Player, msg : UseItemMessage, order : Door.Exchange) : Unit = {
    val door_guid = msg.object_guid
    order match {
      case Door.OpenEvent() =>
        continent.GUID(door_guid) match {
          case Some(door : Door) =>
            sendResponse(GenericObjectStateMsg(door_guid, 16))
            localService ! LocalServiceMessage(continent.Id, LocalAction.DoorOpens(tplayer.GUID, continent, door))

          case _ =>
            log.warn(s"door $door_guid wanted to be opened but could not be found")
        }

      case Door.CloseEvent() =>
        sendResponse(GenericObjectStateMsg(door_guid, 17))
        localService ! LocalServiceMessage(continent.Id, LocalAction.DoorCloses(tplayer.GUID, door_guid))

      case Door.NoEvent() => ;
    }
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleLocalServiceResponse(toChannel : String, guid : PlanetSideGUID, reply : LocalResponse.Response) : Unit = {
    val tplayer_guid = if(player.HasGUID) player.GUID
    else PlanetSideGUID(0)
    reply match {
      case LocalResponse.AlertDestroyDeployable(obj) =>
        //the (former) owner (obj.OwnerName) should process this message
        avatar.Deployables.Remove(obj)
        UpdateDeployableUIElements(avatar.Deployables.UpdateUIElement(obj.Definition.Item))

      case LocalResponse.DeployableMapIcon(behavior, deployInfo) =>
        if(tplayer_guid != guid) {
          sendResponse(DeployableObjectsInfoMessage(behavior, deployInfo))
        }

      case LocalResponse.DoorOpens(door_guid) =>
        if(tplayer_guid != guid) {
          sendResponse(GenericObjectStateMsg(door_guid, 16))
        }

      case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
        sendResponse(GenericObjectStateMsg(door_guid, 17))

      case LocalResponse.EliminateDeployable(obj : TurretDeployable, guid, pos) =>
        if(obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        }
        else {
          DeconstructDeployable(obj, guid, pos, obj.Orientation, if(obj.MountPoints.isEmpty) 2 else 1)
        }

      case LocalResponse.EliminateDeployable(obj : ComplexDeployable, guid, pos) =>
        if(obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        }
        else {
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 1)
        }

      case LocalResponse.EliminateDeployable(obj : ExplosiveDeployable, guid, pos) =>
        if(obj.Exploded || obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        }
        else {
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.EliminateDeployable(obj : TelepadDeployable, guid, pos) =>
        //if active, deactivate
        if(obj.Active) {
          obj.Active = false
          sendResponse(GenericObjectActionMessage(guid, 116))
          sendResponse(GenericObjectActionMessage(guid, 120))
        }
        //determine if no replacement teleport system exists
        continent.GUID(obj.Router) match {
          case Some(router : Vehicle) =>
            //if the telepad was replaced, the new system is physically in place but not yet functional
            if(router.Utility(UtilityType.internal_router_telepad_deployable) match {
              case Some(internalTelepad : Utility.InternalTelepad) => internalTelepad.Telepad.contains(guid) //same telepad
              case _ => true
            }) {
              //there is no replacement telepad; shut down the system
              ToggleTeleportSystem(router, None)
            }
          case _ => ;
        }
        //standard deployable elimination behavior
        if(obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        }
        else {
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.EliminateDeployable(obj, guid, pos) =>
        if(obj.Health == 0) {
          DeconstructDeployable(obj, guid, pos)
        }
        else {
          DeconstructDeployable(obj, guid, pos, obj.Orientation, 2)
        }

      case LocalResponse.HackClear(target_guid, unk1, unk2) =>
        log.trace(s"Clearing hack for ${target_guid}")
        // Reset hack state for all players
        sendResponse(HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2))
      case LocalResponse.HackObject(target_guid, unk1, unk2) =>
          sendResponse(HackMessage(0, target_guid, guid, 100, unk1, HackState.Hacked, unk2))
      case LocalResponse.HackCaptureTerminal(target_guid, unk1, unk2, isResecured) =>
        var value = 0L
        if(isResecured) {
          value = 17039360L
        }
        else {
          continent.GUID(target_guid) match {
            case Some(capture_terminal: Hackable) =>
              capture_terminal.HackedBy match {
                case Some(Hackable.HackInfo(_, _, hfaction, _, start, length)) =>
                  val hack_time_remaining_ms = TimeUnit.MILLISECONDS.convert(math.max(0, start + length - System.nanoTime), TimeUnit.NANOSECONDS)
                  val deciseconds_remaining = (hack_time_remaining_ms / 100)

                  // See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
                  val start_num = hfaction match {
                    case PlanetSideEmpire.TR => 65536L
                    case PlanetSideEmpire.NC => 131072L
                    case PlanetSideEmpire.VS => 196608L
                  }
                  value = start_num + deciseconds_remaining

                  sendResponse(PlanetsideAttributeMessage(target_guid, 20, value))
                case _ => log.warn("LocalResponse.HackCaptureTerminal: HackedBy not defined")
              }
            case _ => log.warn(s"LocalResponse.HackCaptureTerminal: Couldn't find capture terminal with GUID ${target_guid} in zone ${continent.Id}")
          }
        }
      case LocalResponse.ObjectDelete(object_guid, unk) =>
        if(tplayer_guid != guid) {
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
        StartBundlingPackets()
        UseRouterTelepadEffect(passenger_guid, src_guid, dest_guid)
        StopBundlingPackets()

      case LocalResponse.SetEmpire(object_guid, empire) =>
        sendResponse(SetEmpireMessage(object_guid, empire))

      case LocalResponse.ToggleTeleportSystem(router, system_plan) =>
        ToggleTeleportSystem(router, system_plan)

      case LocalResponse.TriggerEffect(target_guid, effect, effectInfo, triggerLocation) =>
        sendResponse(TriggerEffectMessage(target_guid, effect, effectInfo, triggerLocation))

      case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
        sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

      case _ => ;
    }
  }

  /**
    * na
    * @param tplayer na
    * @param reply   na
    */
  def HandleMountMessages(tplayer : Player, reply : Mountable.Exchange) : Unit = {
    reply match {
      case Mountable.CanMount(obj : ImplantTerminalMech, seat_num) =>
        MountingAction(tplayer, obj, seat_num)
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, 1000L)) //health of mech

      case Mountable.CanMount(obj : Vehicle, seat_num) =>
        val obj_guid : PlanetSideGUID = obj.GUID
        val player_guid : PlanetSideGUID = tplayer.GUID
        log.info(s"MountVehicleMsg: $player_guid mounts $obj_guid @ $seat_num")
        PlayerActionsToCancel()
        sendResponse(PlanetsideAttributeMessage(obj_guid, 0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, 68, 0)) //shield health
        sendResponse(PlanetsideAttributeMessage(obj_guid, 113, 0)) //capacitor
        if(seat_num == 0) {
          vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(obj), continent)) //clear timer
          //simplistic vehicle ownership management
          obj.Owner match {
            case Some(owner_guid) =>
              continent.GUID(owner_guid) match {
                case Some(previous_owner : Player) =>
                  if(previous_owner.VehicleOwned.contains(obj_guid)) {
                    previous_owner.VehicleOwned = None //simplistic ownership management, player loses vehicle ownership
                  }
                case _ => ;
              }
            case None => ;
          }
          OwnVehicle(obj, tplayer)
        }
        AccessContents(obj)
        UpdateWeaponAtSeatPosition(obj, seat_num)
        MountingAction(tplayer, obj, seat_num)

      case Mountable.CanMount(obj : PlanetSideGameObject with WeaponTurret, seat_num) =>
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
        UpdateWeaponAtSeatPosition(obj, seat_num)
        MountingAction(tplayer, obj, seat_num)

      case Mountable.CanMount(obj : Mountable, _) =>
        log.warn(s"MountVehicleMsg: $obj is some generic mountable object and nothing will happen")

      case Mountable.CanDismount(obj : ImplantTerminalMech, seat_num) =>
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj : Vehicle, seat_num) =>
        val player_guid : PlanetSideGUID = tplayer.GUID
        if(player_guid == player.GUID) {
          //disembarking self
          TotalDriverVehicleControl(obj)
          UnAccessContents(obj)
          DismountAction(tplayer, obj, seat_num)
        }
        else {
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(player_guid, seat_num, true, obj.GUID))
        }

      case Mountable.CanDismount(obj : PlanetSideGameObject with WeaponTurret, seat_num) =>
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj : Mountable, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some generic mountable object and nothing will happen")

      case Mountable.CanNotMount(obj : Vehicle, seat_num) =>
        log.warn(s"MountVehicleMsg: $tplayer attempted to mount $obj's seat $seat_num, but was not allowed")
        if(obj.SeatPermissionGroup(seat_num).contains(AccessPermissionGroup.Driver)) {
          sendResponse(ChatMsg(ChatMessageType.CMT_OPEN, false, "", "You are not the driver of this vehicle.", None))
        }

      case Mountable.CanNotMount(obj : Mountable, seat_num) =>
        log.warn(s"MountVehicleMsg: $tplayer attempted to mount $obj's seat $seat_num, but was not allowed")

      case Mountable.CanNotDismount(obj, seat_num) =>
        log.warn(s"DismountVehicleMsg: $tplayer attempted to dismount $obj's seat $seat_num, but was not allowed")
    }
  }

  /**
    * na
    * @param tplayer na
    * @param msg     na
    * @param order   na
    */
  def HandleTerminalMessage(tplayer : Player, msg : ItemTransactionMessage, order : Terminal.Exchange) : Unit = {
    order match {
      case Terminal.BuyExosuit(exosuit, subtype) =>
        //TODO check exo-suit permissions
        val originalSuit = tplayer.ExoSuit
        val originalSubtype = Loadout.DetermineSubtype(tplayer)
        if(originalSuit != exosuit || originalSubtype != subtype) {
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
          //prepare lists of valid objects
          val beforeInventory = tplayer.Inventory.Clear()
          val beforeHolsters = clearHolsters(tplayer.Holsters().iterator)
          //change suit (clear inventory and change holster sizes; holsters must be empty before this point)
          val originalArmor = tplayer.Armor
          tplayer.ExoSuit = exosuit //changes the value of MaxArmor to reflect the new exo-suit
          val toMaxArmor = tplayer.MaxArmor
          if(originalSuit != exosuit || originalSubtype != subtype || originalArmor > toMaxArmor) {
            tplayer.History(HealFromExoSuitChange(PlayerSource(tplayer), exosuit))
            tplayer.Armor = toMaxArmor
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, toMaxArmor))
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, toMaxArmor))
          }
          else {
            tplayer.Armor = originalArmor
          }
          //ensure arm is down, even if it needs to go back up
          if(tplayer.DrawnSlot != Player.HandsDownSlot) {
            tplayer.DrawnSlot = Player.HandsDownSlot
            sendResponse(ObjectHeldMessage(tplayer.GUID, Player.HandsDownSlot, true))
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectHeld(tplayer.GUID, tplayer.LastDrawnSlot))
          }
          //delete everything not dropped
          (beforeHolsters ++ beforeInventory).foreach({ elem =>
            sendResponse(ObjectDeleteMessage(elem.obj.GUID, 0))
          })
          beforeHolsters.foreach({ elem =>
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(tplayer.GUID, elem.obj.GUID))
          })
          //report change
          sendResponse(ArmorChangedMessage(tplayer.GUID, exosuit, subtype))
          avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
          //sterilize holsters
          val normalHolsters = if(originalSuit == ExoSuitType.MAX) {
            val (maxWeapons, normalWeapons) = beforeHolsters.partition(elem => elem.obj.Size == EquipmentSize.Max)
            maxWeapons.foreach(entry => { taskResolver ! GUIDTask.UnregisterEquipment(entry.obj)(continent.GUID) })
            normalWeapons
          }
          else {
            beforeHolsters
          }
          //populate holsters
          val finalInventory = if(exosuit == ExoSuitType.MAX) {
            taskResolver ! DelayedObjectHeld(tplayer, 0, List(PutEquipmentInSlot(tplayer, Tool(GlobalDefinitions.MAXArms(subtype, tplayer.Faction)), 0)))
            fillEmptyHolsters(List(tplayer.Slot(4)).iterator, normalHolsters) ++ beforeInventory
          }
          else if(originalSuit == exosuit) { //note - this will rarely be the situation
            fillEmptyHolsters(tplayer.Holsters().iterator, normalHolsters)
          }
          else {
            val (afterHolsters, toInventory) = normalHolsters.partition(elem => elem.obj.Size == tplayer.Slot(elem.start).Size)
            afterHolsters.foreach({ elem => tplayer.Slot(elem.start).Equipment = elem.obj })
            fillEmptyHolsters(tplayer.Holsters().iterator, toInventory ++ beforeInventory)
          }
          //draw holsters
          tplayer.VisibleSlots.foreach({ index =>
            tplayer.Slot(index).Equipment match {
              case Some(obj) =>
                val definition = obj.Definition
                sendResponse(
                  ObjectCreateDetailedMessage(
                    definition.ObjectId,
                    obj.GUID,
                    ObjectCreateMessageParent(tplayer.GUID, index),
                    definition.Packet.DetailedConstructorData(obj).get
                  )
                )
                avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(player.GUID, player.GUID, index, obj))
              case None => ;
            }
          })
          //re-draw equipment held in free hand
          tplayer.FreeHand.Equipment match {
            case Some(item) =>
              val definition = item.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  definition.ObjectId,
                  item.GUID,
                  ObjectCreateMessageParent(tplayer.GUID, Player.FreeHandSlot),
                  definition.Packet.DetailedConstructorData(item).get
                )
              )
            case None => ;
          }
          //put items back into inventory
          val (stow, drop) = if(originalSuit == exosuit) {
            (finalInventory, Nil)
          }
          else {
            GridInventory.recoverInventory(finalInventory, tplayer.Inventory)
          }
          stow.foreach(elem => {
            tplayer.Inventory.Insert(elem.start, elem.obj)
            val obj = elem.obj
            val definition = obj.Definition
            sendResponse(
              ObjectCreateDetailedMessage(
                definition.ObjectId,
                obj.GUID,
                ObjectCreateMessageParent(tplayer.GUID, elem.start),
                definition.Packet.DetailedConstructorData(obj).get
              )
            )
          })
          val (finalDroppedItems, retiredItems) = drop.map(item => InventoryItem(item, -1)).partition(DropPredicate(tplayer))
          //drop special items on ground
          val pos = tplayer.Position
          val orient = Vector3.z(tplayer.Orientation.z)
          finalDroppedItems.foreach(entry => {
            //TODO make a sound when dropping stuff
            continent.Ground ! Zone.Ground.DropItem(entry.obj, pos, orient)
          })
          //deconstruct normal items
          retiredItems.foreach({ entry =>
            taskResolver ! GUIDTask.UnregisterEquipment(entry.obj)(continent.GUID)
          })
        }
        else {
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.BuyEquipment(item) =>
        continent.GUID(tplayer.VehicleSeated) match {
          //vehicle trunk
          case Some(vehicle : Vehicle) =>
            vehicle.Fit(item) match {
              case Some(index) =>
                item.Faction = tplayer.Faction
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
                taskResolver ! StowNewEquipmentInVehicle(vehicle)(index, item)
              case None => //player free hand?
                tplayer.FreeHand.Equipment match {
                  case None =>
                    item.Faction = tplayer.Faction
                    sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
                    taskResolver ! PutEquipmentInSlot(tplayer, item, Player.FreeHandSlot)
                  case Some(_) =>
                    sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, false))
                }
            }
          //player backpack or free hand
          case _ =>
            tplayer.Fit(item) match {
              case Some(index) =>
                item.Faction = tplayer.Faction
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
                taskResolver ! PutEquipmentInSlot(tplayer, item, index)
              case None =>
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, false))
            }
        }
        lastTerminalOrderFulfillment = true

      case Terminal.SellEquipment() =>
        tplayer.FreeHand.Equipment match {
          case Some(item) =>
            if(item.GUID == msg.item_guid) {
              sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Sell, true))
              taskResolver ! RemoveEquipmentFromSlot(tplayer, item, Player.FreeHandSlot)
            }
          case None =>
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Sell, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.InfantryLoadout(exosuit, subtype, holsters, inventory) =>
        log.info(s"$tplayer wants to change equipment loadout to their option #${msg.unk1 + 1}")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Loadout, true))
        //prepare lists of valid objects
        val beforeFreeHand = tplayer.FreeHand.Equipment
        val dropPred = DropPredicate(tplayer)
        val (dropHolsters, beforeHolsters) = clearHolsters(tplayer.Holsters().iterator).partition(dropPred)
        val (dropInventory, beforeInventory) = tplayer.Inventory.Clear().partition(dropPred)
        tplayer.FreeHand.Equipment = None //terminal and inventory will close, so prematurely dropping should be fine
        //sanitize exo-suit for change
        val originalSuit = player.ExoSuit
        val originalSubtype = Loadout.DetermineSubtype(tplayer)
        val fallbackSuit = ExoSuitType.Standard
        val fallbackSubtype = 0
        //a loadout with a prohibited exo-suit type will result in a fallback exo-suit type
        val (nextSuit : ExoSuitType.Value, nextSubtype : Int) =
          if(ExoSuitDefinition.Select(exosuit).Permissions match {
            case Nil =>
              true
            case permissions if subtype != 0 =>
              val certs = tplayer.Certifications
              certs.intersect(permissions.toSet).nonEmpty &&
                certs.intersect(InfantryLoadout.DetermineSubtypeC(subtype)).nonEmpty
            case permissions =>
              tplayer.Certifications.intersect(permissions.toSet).nonEmpty
          }) {
          (exosuit, subtype)
        }
        else {
          log.warn(s"$tplayer no longer has permission to wear the exo-suit type $exosuit; will wear $fallbackSuit instead")
          (fallbackSuit, fallbackSubtype)
        }
        //update suit interally (holsters must be empty before this point)
        val originalArmor = player.Armor
        tplayer.ExoSuit = nextSuit
        val toMaxArmor = tplayer.MaxArmor
        if(originalSuit != nextSuit || originalSubtype != nextSubtype || originalArmor > toMaxArmor) {
          tplayer.History(HealFromExoSuitChange(PlayerSource(tplayer), nextSuit))
          tplayer.Armor = toMaxArmor
          sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, toMaxArmor))
          avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, toMaxArmor))
        }
        else {
          tplayer.Armor = originalArmor
        }
        //ensure arm is down, even if it needs to go back up
        if(tplayer.DrawnSlot != Player.HandsDownSlot) {
          tplayer.DrawnSlot = Player.HandsDownSlot
          sendResponse(ObjectHeldMessage(tplayer.GUID, Player.HandsDownSlot, true))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectHeld(tplayer.GUID, tplayer.LastDrawnSlot))
        }
        //a change due to exo-suit permissions mismatch will result in (more) items being re-arranged and/or dropped
        //dropped items can be forgotten safely
        val (afterHolsters, afterInventory) = if(nextSuit == exosuit) {
          (
            holsters.filterNot(dropPred),
            inventory.filterNot(dropPred)
          )
        }
        else {
          val newSuitDef = ExoSuitDefinition.Select(nextSuit)
          val (afterInventory, extra) = GridInventory.recoverInventory(
            inventory.filterNot(dropPred),
            tplayer.Inventory
          )
          val afterHolsters = {
            val preservedHolsters = if(exosuit == ExoSuitType.MAX) {
              holsters.filter(_.start == 4) //melee slot perservation
            }
            else {
              holsters
                .filterNot(dropPred)
                .collect {
                  case item @ InventoryItem(obj, index) if newSuitDef.Holster(index) == obj.Size => item
                }
            }
            val size = newSuitDef.Holsters.size
            val indexMap = preservedHolsters.map { entry => entry.start }
            preservedHolsters ++ (extra.map { obj =>
              tplayer.Fit(obj) match {
                case Some(index : Int) if index < size && !indexMap.contains(index) =>
                  InventoryItem(obj, index)
                case _ =>
                  InventoryItem(obj, -1)
              }
            }).filterNot(entry => entry.start == -1)
          }
          (afterHolsters, afterInventory)
        }
        //delete everything (not dropped)
        beforeHolsters.foreach({ elem =>
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(tplayer.GUID, elem.obj.GUID))
        })
        (beforeHolsters ++ beforeInventory).foreach({ elem =>
          sendResponse(ObjectDeleteMessage(elem.obj.GUID, 0))
          taskResolver ! GUIDTask.UnregisterEquipment(elem.obj)(continent.GUID)
        })
        //report change
        sendResponse(ArmorChangedMessage(tplayer.GUID, nextSuit, nextSubtype))
        avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ArmorChanged(tplayer.GUID, nextSuit, nextSubtype))
        if(nextSuit == ExoSuitType.MAX) {
          val (maxWeapons, otherWeapons) = afterHolsters.partition(entry => { entry.obj.Size == EquipmentSize.Max })
          taskResolver ! DelayedObjectHeld(tplayer, 0, List(PutEquipmentInSlot(tplayer, maxWeapons.head.obj, 0)))
          otherWeapons
        }
        else {
          afterHolsters
        }.foreach(entry => {
          entry.obj.Faction = tplayer.Faction
          taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
        })
        //put items into inventory
        afterInventory.foreach(entry => {
          entry.obj.Faction = tplayer.Faction
          taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
        })
        //drop stuff on ground
        val pos = tplayer.Position
        val orient = Vector3.z(tplayer.Orientation.z)
        ((beforeFreeHand match {
          case Some(item) => List(InventoryItem(item, -1)) //add the item previously in free hand, if any
          case None => Nil
        }) ++ dropHolsters ++ dropInventory).foreach(entry => {
          entry.obj.Faction = PlanetSideEmpire.NEUTRAL
          continent.Ground ! Zone.Ground.DropItem(entry.obj, pos, orient)
        })
        lastTerminalOrderFulfillment = true

      case Terminal.VehicleLoadout(definition, weapons, inventory) =>
        log.info(s"$tplayer wants to change their vehicle equipment loadout to their option #${msg.unk1 + 1}")
        FindLocalVehicle match {
          case Some(vehicle) =>
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Loadout, true))
            val (_, afterInventory) = inventory.partition(DropPredicate(tplayer))
            //dropped items are lost
            //remove old inventory
            val deleteEquipment : (Int, Equipment) => Unit = DeleteEquipmentFromVehicle(vehicle)
            vehicle.Inventory.Clear().foreach({ case InventoryItem(obj, index) => deleteEquipment(index, obj) })
            val stowEquipment : (Int, Equipment) => TaskResolver.GiveTask = StowNewEquipmentInVehicle(vehicle)
            (if(vehicle.Definition == definition) {
              //vehicles are the same type; transfer over weapon ammo
              //TODO ammo switching? no vehicle weapon does that currently but ...
              //TODO want to completely swap weapons, but holster icon vanishes temporarily after swap
              //TODO BFR arms must be swapped properly
              val channel = s"${vehicle.Actor}"
              weapons.foreach({ case InventoryItem(obj, index) =>
                val savedWeapon = obj.asInstanceOf[Tool]
                val existingWeapon = vehicle.Weapons(index).Equipment.get.asInstanceOf[Tool]
                (0 until existingWeapon.MaxAmmoSlot).foreach({ index =>
                  val existingBox = existingWeapon.AmmoSlots(index).Box
                  existingBox.Capacity = savedWeapon.AmmoSlots(index).Box.Capacity
                  //use VehicleAction.InventoryState2; VehicleAction.InventoryState temporarily glitches ammo count in ui
                  vehicleService ! VehicleServiceMessage(channel, VehicleAction.InventoryState2(PlanetSideGUID(0), existingBox.GUID, existingWeapon.GUID, existingBox.Capacity))
                })
              })
              afterInventory
            }
            else {
              //do not transfer over weapon ammo
              if(vehicle.Definition.TrunkSize == definition.TrunkSize && vehicle.Definition.TrunkOffset == definition.TrunkOffset) {
                afterInventory
              }
              else {
                //accommodate as much of inventory as possible
                val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory) //dropped items can be forgotten
                stow
              }
            }).foreach({ case InventoryItem(obj, index) =>
              obj.Faction = tplayer.Faction
              taskResolver ! stowEquipment(index, obj)
            })
          case None =>
            log.error(s"can not apply the loadout - can not find a vehicle")
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Loadout, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.LearnCertification(cert) =>
        val name = tplayer.Name
        if(!tplayer.Certifications.contains(cert)) {
          val guid = tplayer.GUID
          log.info(s"$name is learning the $cert certification for ${Certification.Cost.Of(cert)} points")
          avatar.Certifications += cert
          StartBundlingPackets()
          AddToDeployableQuantities(cert, player.Certifications)
          sendResponse(PlanetsideAttributeMessage(guid, 24, cert.id))
          tplayer.Certifications.intersect(Certification.Dependencies.Like(cert)).foreach(entry => {
            log.info(s"$cert replaces the learned certification $entry that cost ${Certification.Cost.Of(entry)} points")
            avatar.Certifications -= entry
            sendResponse(PlanetsideAttributeMessage(guid, 25, entry.id))
          })
          StopBundlingPackets()
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, true))
        }
        else {
          log.warn(s"$name already knows the $cert certification, so he can't learn it")
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.SellCertification(cert) =>
        val name = tplayer.Name
        if(tplayer.Certifications.contains(cert)) {
          val guid = tplayer.GUID
          log.info(s"$name is forgetting the $cert certification for ${Certification.Cost.Of(cert)} points")
          avatar.Certifications -= cert
          StartBundlingPackets()
          RemoveFromDeployablesQuantities(cert, player.Certifications)
          sendResponse(PlanetsideAttributeMessage(guid, 25, cert.id))
          tplayer.Certifications.intersect(Certification.Dependencies.FromAll(cert)).foreach(entry => {
            log.info(s"$name is also forgetting the ${Certification.Cost.Of(entry)}-point $entry certification which depends on $cert")
            avatar.Certifications -= entry
            RemoveFromDeployablesQuantities(entry, player.Certifications)
            sendResponse(PlanetsideAttributeMessage(guid, 25, entry.id))
          })
          StopBundlingPackets()
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Sell, true))
        }
        else {
          log.warn(s"$name doesn't know what a $cert certification is, so he can't forget it")
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.LearnImplant(implant) =>
        val terminal_guid = msg.terminal_guid
        val implant_type = implant.Type
        val message = s"Implants: $tplayer wants to learn $implant_type"
        val (interface, slotNumber) = tplayer.VehicleSeated match {
          case Some(mech_guid) =>
            (
              continent.Map.TerminalToInterface.get(mech_guid.guid),
              if(!avatar.Implants.exists({ slot => slot.Implant == implant_type })) {
                //no duplicates
                avatar.InstallImplant(implant)
              }
              else {
                None
              }
            )
          case _ =>
            (None, None)
        }
        if(interface.contains(terminal_guid.guid) && slotNumber.isDefined) {
          val slot = slotNumber.get
          log.info(s"$message - put in slot $slot")
          sendResponse(AvatarImplantMessage(tplayer.GUID, ImplantAction.Add, slot, implant_type.id))
          sendResponse(ItemTransactionResultMessage(terminal_guid, TransactionType.Learn, true))
        }
        else {
          if(interface.isEmpty) {
            log.warn(s"$message - not interacting with a terminal")
          }
          else if(!interface.contains(terminal_guid.guid)) {
            log.warn(s"$message - interacting with the wrong terminal, ${interface.get}")
          }
          else if(slotNumber.isEmpty) {
            log.warn(s"$message - already knows that implant")
          }
          else {
            log.warn(s"$message - forgot to sit at a terminal")
          }
          sendResponse(ItemTransactionResultMessage(terminal_guid, TransactionType.Learn, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.SellImplant(implant) =>
        val terminal_guid = msg.terminal_guid
        val implant_type = implant.Type
        val (interface, slotNumber) = tplayer.VehicleSeated match {
          case Some(mech_guid) =>
            (
              continent.Map.TerminalToInterface.get(mech_guid.guid),
              avatar.UninstallImplant(implant_type)
            )
          case None =>
            (None, None)
        }
        if(interface.contains(terminal_guid.guid) && slotNumber.isDefined) {
          val slot = slotNumber.get
          log.info(s"$tplayer is selling $implant_type - take from slot $slot")
          sendResponse(AvatarImplantMessage(tplayer.GUID, ImplantAction.Remove, slot, 0))
          sendResponse(ItemTransactionResultMessage(terminal_guid, TransactionType.Sell, true))
        }
        else {
          val message = s"$tplayer can not sell $implant_type"
          if(interface.isEmpty) {
            log.warn(s"$message - not interacting with a terminal")
          }
          else if(!interface.contains(terminal_guid.guid)) {
            log.warn(s"$message - interacting with the wrong terminal, ${interface.get}")
          }
          else if(slotNumber.isEmpty) {
            log.warn(s"$message - does not know that implant")
          }
          else {
            log.warn(s"$message - forgot to sit at a terminal")
          }
          sendResponse(ItemTransactionResultMessage(terminal_guid, TransactionType.Sell, false))
        }
        lastTerminalOrderFulfillment = true

      case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
        continent.Map.TerminalToSpawnPad.get(msg.terminal_guid.guid) match {
          case Some(pad_guid) =>
            val toFaction = tplayer.Faction
            val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad]
            vehicle.Faction = toFaction
            vehicle.Continent = continent.Id
            vehicle.Position = pad.Position
            vehicle.Orientation = pad.Orientation
            //default loadout, weapons
            val vWeapons = vehicle.Weapons
            weapons.foreach(entry => {
              val index = entry.start
              vWeapons.get(index) match {
                case Some(slot) =>
                  entry.obj.Faction = toFaction
                  slot.Equipment = None
                  slot.Equipment = entry.obj
                case None =>
                  log.warn(s"applying default loadout to $vehicle on spawn, but can not find a mounted weapon @ $index")
              }
            })
            //default loadout, trunk
            val vTrunk = vehicle.Trunk
            vTrunk.Clear()
            trunk.foreach(entry => {
              entry.obj.Faction = toFaction
              vTrunk += entry.start -> entry.obj
            })
            taskResolver ! RegisterNewVehicle(vehicle, pad)
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))

          case None =>
            log.error(s"$tplayer wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it")
        }
        lastTerminalOrderFulfillment = true

      case _ =>
        val order : String = if(msg == null) {
          s"order $msg"
        }
        else {
          "missing order"
        }
        log.warn(s"${tplayer.Name} made a request but the terminal rejected the $order")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false))
        lastTerminalOrderFulfillment = true
    }
  }

  /**
    * na
    * @param toChannel na
    * @param guid      na
    * @param reply     na
    */
  def HandleVehicleServiceResponse(toChannel : String, guid : PlanetSideGUID, reply : VehicleResponse.Response) : Unit = {
    val tplayer_guid = if(player.HasGUID) player.GUID else PlanetSideGUID(0)

    reply match {
      case VehicleResponse.AttachToRails(vehicle_guid, pad_guid) =>
        sendResponse(ObjectAttachMessage(pad_guid, vehicle_guid, 3))

      case VehicleResponse.ChildObjectState(object_guid, pitch, yaw) =>
        if(tplayer_guid != guid) {
          sendResponse(ChildObjectStateMessage(object_guid, pitch, yaw))
        }

      case VehicleResponse.ConcealPlayer(player_guid) =>
        //TODO this is the correct message; but, I don't know how to undo the effects of it
        //sendResponse(GenericObjectActionMessage(player_guid, 36))
        sendResponse(PlanetsideAttributeMessage(player_guid, 29, 1))

      case VehicleResponse.DismountVehicle(bailType, wasKickedByDriver) =>
        if(tplayer_guid != guid) {
          sendResponse(DismountVehicleMsg(guid, bailType, wasKickedByDriver))
        }

      case VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos) =>
        if(tplayer_guid != guid) {
          sendResponse(DeployRequestMessage(guid, object_guid, state, unk1, unk2, pos))
        }

      case VehicleResponse.DetachFromRails(vehicle_guid, pad_guid, pad_position, pad_orientation_z) =>
        val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad].Definition
        sendResponse(ObjectDetachMessage(pad_guid, vehicle_guid, pad_position + Vector3(0, 0, pad.VehicleCreationZOffset), pad_orientation_z + pad.VehicleCreationZOrientOffset))

      case VehicleResponse.EquipmentInSlot(pkt) =>
        if(tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case VehicleResponse.HitHint(source_guid) =>
        if(player.isAlive) {
          sendResponse(HitHint(source_guid, player.GUID))
        }

      case VehicleResponse.InventoryState(obj, parent_guid, start, con_data) =>
        if(tplayer_guid != guid) {
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

      case msg@VehicleResponse.KickPassenger(seat_num, wasKickedByDriver, vehicle_guid) =>
        // seat_num seems to be correct if passenger is kicked manually by driver, but always seems to return 4 if user is kicked by seat permissions
        log.info(s"$msg")
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))
        if(tplayer_guid == guid) {
          continent.GUID(vehicle_guid) match {
            case Some(obj : Vehicle) =>
              UnAccessContents(obj)
            case _ => ;
          }
        }

      case VehicleResponse.InventoryState2(obj_guid, parent_guid, value) =>
        if(tplayer_guid != guid) {
          sendResponse(InventoryStateMessage(obj_guid, 0, parent_guid, value))
        }

      case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) =>
        //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
        if(tplayer_guid != guid) {
          sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
          ReloadVehicleAccessPermissions(vehicle)
        }

      case VehicleResponse.MountVehicle(vehicle_guid, seat) =>
        if(tplayer_guid != guid) {
          sendResponse(ObjectAttachMessage(vehicle_guid, guid, seat))
        }

      case VehicleResponse.Ownership(vehicle_guid) =>
        if(tplayer_guid == guid) { // Only the player that owns this vehicle needs the ownership packet
          sendResponse(PlanetsideAttributeMessage(tplayer_guid, 21, vehicle_guid))
        }

      case VehicleResponse.PlanetsideAttribute(vehicle_guid, attribute_type, attribute_value) =>
        if(tplayer_guid != guid) {
          player.VehicleOwned = Some(vehicle_guid)
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, attribute_type, attribute_value))
        }

      case VehicleResponse.ResetSpawnPad(pad_guid) =>
        sendResponse(GenericObjectActionMessage(pad_guid, 92))

      case VehicleResponse.RevealPlayer(player_guid) =>
        //TODO see note in ConcealPlayer
        sendResponse(PlanetsideAttributeMessage(player_guid, 29, 0))

      case VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission) =>
        if(tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, seat_group, permission))
        }

      case VehicleResponse.StowEquipment(vehicle_guid, slot, item_type, item_guid, item_data) =>
        if(tplayer_guid != guid) {
          //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
          sendResponse(
            ObjectCreateDetailedMessage(item_type, item_guid, ObjectCreateMessageParent(vehicle_guid, slot), item_data)
          )
        }

      case VehicleResponse.UnloadVehicle(vehicle, vehicle_guid) =>
        BeforeUnloadVehicle(vehicle)
        sendResponse(ObjectDeleteMessage(vehicle_guid, 0))

      case VehicleResponse.UnstowEquipment(item_guid) =>
        if(tplayer_guid != guid) {
          //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
          sendResponse(ObjectDeleteMessage(item_guid, 0))
        }

      case VehicleResponse.VehicleState(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6) =>
        if(tplayer_guid != guid) {
          sendResponse(VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6))
          if(player.VehicleSeated.contains(vehicle_guid)) {
            player.Position = pos
          }
        }
      case VehicleResponse.SendResponse(msg) =>
        sendResponse(msg)

      case VehicleResponse.UpdateAmsSpawnPoint(list) =>
        amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
        DrawCurrentAmsSpawnPoint()

      case VehicleResponse.TransferPassengerChannel(old_channel, temp_channel, vehicle) =>
        if(tplayer_guid != guid) {
          interstellarFerry = Some(vehicle)
          vehicleService ! Service.Leave(Some(old_channel)) //old vehicle-specific channel (was s"${vehicle.Actor}")
          vehicleService ! Service.Join(temp_channel) //temporary vehicle-specific channel (driver name, plus extra)
        }
      case VehicleResponse.TransferPassenger(temp_channel, vehicle, vehicle_to_delete) =>
        vehicle.PassengerInSeat(player) match {
          case Some(_) =>
            vehicleService ! Service.Leave(Some(temp_channel)) //temporary vehicle-specific channel (see above)
            deadState = DeadState.Release
            sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
            interstellarFerry = Some(vehicle) //on the other continent and registered to that continent's GUID system
            interstellarFerryTopLevelGUID = Some(vehicle_to_delete) //vehicle.GUID, or previously a higher level parent
            LoadZonePhysicalSpawnPoint(vehicle.Continent, vehicle.Position, vehicle.Orientation, 1)
          case None =>
            interstellarFerry match {
              case None =>
                vehicleService ! Service.Leave(Some(temp_channel)) //no longer being transferred between zones
              case Some(_) => ;
              //wait patiently
            }
        }

      case VehicleResponse.ForceDismountVehicleCargo(cargo_guid, bailed, requestedByPassenger, kicked) =>
        DismountVehicleCargo(tplayer_guid, cargo_guid, bailed, requestedByPassenger, kicked)
      case VehicleResponse.KickCargo(vehicle, speed, delay) =>
        if(player.VehicleSeated.nonEmpty && deadState == DeadState.Alive) {
          if(speed > 0) {
            val strafe = if(CargoOrientation(vehicle) == 1) 2 else 1
            val reverseSpeed = if(strafe > 1) 0 else speed
            //strafe or reverse, not both
            controlled = Some(reverseSpeed)
            sendResponse(ServerVehicleOverrideMsg(true, true, true, false, 0, strafe, reverseSpeed, Some(0)))
            import scala.concurrent.ExecutionContext.Implicits.global
            context.system.scheduler.scheduleOnce(delay milliseconds, self, VehicleServiceResponse(toChannel, tplayer_guid, VehicleResponse.KickCargo(vehicle, 0, delay)))
          }
          else {
            controlled = None
            sendResponse(ServerVehicleOverrideMsg(false, false, false, false, 0, 0, 0, None))
          }
        }

      case _ => ;
    }
  }

  /**
    * na
    * @param decorator custom text for these messages in the log
    * @param target an optional the target object
    * @param targetGUID the expected globally unique identifier of the target object
    */
  def LogCargoEventMissingVehicleError(decorator : String, target : Option[PlanetSideGameObject], targetGUID : PlanetSideGUID) : Unit = {
    target match {
      case Some(_ : Vehicle) => ;
      case Some(_) => log.error(s"$decorator target $targetGUID no longer identifies as a vehicle")
      case None => log.error(s"$decorator target $targetGUID has gone missing")
    }
  }

  /**
    * na
    * @param cargoGUID  na
    * @param carrierGUID na
    * @param mountPoint na
    * @param iteration na
    */
  def HandleCheckCargoDismounting(cargoGUID : PlanetSideGUID, carrierGUID : PlanetSideGUID, mountPoint : Int, iteration : Int) : Unit = {
    (continent.GUID(cargoGUID), continent.GUID(carrierGUID)) match {
      case ((Some(vehicle : Vehicle), Some(cargo_vehicle : Vehicle))) =>
        HandleCheckCargoDismounting(cargoGUID, vehicle, carrierGUID, cargo_vehicle, mountPoint, iteration)
      case (cargo, carrier) if iteration > 0 =>
        log.error(s"HandleCheckCargoDismounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoDismounting: cargo", cargo, cargoGUID)
        LogCargoEventMissingVehicleError("HandleCheckCargoDismounting: carrier", carrier, carrierGUID)
      case _ =>
    }
  }

  /**
    * na
    * @param cargoGUID na
    * @param cargo na
    * @param carrierGUID na
    * @param carrier na
    * @param mountPoint na
    * @param iteration na
    */
  def HandleCheckCargoDismounting(cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, mountPoint : Int, iteration : Int) : Unit = {
    carrier.CargoHold(mountPoint) match {
      case Some(hold) if !hold.isOccupied =>
        val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
        log.debug(s"HandleCheckCargoDismounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=225")
        if(distance > 225) {
          //cargo vehicle has moved far enough away; close the carrier's hold door
          log.info(s"HandleCheckCargoDismounting: dismount of cargo vehicle from carrier complete at distance of $distance")
          vehicleService ! VehicleServiceMessage(
            continent.Id,
            VehicleAction.SendResponse(
              player.GUID,
              CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
            )
          )
          //sending packet to the cargo vehicle's client results in player locking himself in his vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        }
        else if(iteration > 40) {
          //cargo vehicle has spent too long not getting far enough away; restore the cargo's mount in the carrier hold
          cargo.MountedIn = carrierGUID
          hold.Occupant = cargo
          StartBundlingPackets()
          CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          StopBundlingPackets()
        }
        else {
          //cargo vehicle did not move far away enough yet and there is more time to wait; reschedule check
          import scala.concurrent.ExecutionContext.Implicits.global
          cargoDismountTimer = context.system.scheduler.scheduleOnce(250 milliseconds, self, CheckCargoDismount(cargoGUID, carrierGUID, mountPoint, iteration + 1))
        }
      case None =>
        log.warn(s"HandleCheckCargoDismounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
      case _ =>
        if(iteration == 0) {
          log.warn(s"HandleCheckCargoDismounting: carrier vehicle $carrier will not discharge the cargo of hold #$mountPoint; this operation was initiated incorrectly")
        }
        else {
          log.error(s"HandleCheckCargoDismounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40")
        }
    }
  }

  /**
    * na
    * @param cargoGUID the vehicle being ferried as cargo
    * @param carrierGUID the ferrying carrier vehicle
    * @param mountPoint the cargo hold to which the cargo vehicle is stowed
    * @param iteration number of times a proper mounting for this combination has been queried
    */
  def HandleCheckCargoMounting(cargoGUID : PlanetSideGUID, carrierGUID : PlanetSideGUID, mountPoint : Int, iteration : Int) : Unit = {
    (continent.GUID(cargoGUID), continent.GUID(carrierGUID)) match {
      case ((Some(cargo : Vehicle), Some(carrier : Vehicle))) =>
        HandleCheckCargoMounting(cargoGUID, cargo, carrierGUID, carrier, mountPoint, iteration)
      case (cargo, carrier) if iteration > 0 =>
        log.error(s"HandleCheckCargoMounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoMounting: cargo", cargo, cargoGUID)
        LogCargoEventMissingVehicleError("HandleCheckCargoMounting: carrier", carrier, carrierGUID)
      case _ => ;
    }
  }

  /**
    * na
    * @param cargoGUID the vehicle being ferried as cargo
    * @param cargo the vehicle being ferried as cargo
    * @param carrierGUID the ferrying carrier vehicle
    * @param carrier the ferrying carrier vehicle
    * @param mountPoint the cargo hold to which the cargo vehicle is stowed
    * @param iteration number of times a proper mounting for this combination has been queried
    */
  def HandleCheckCargoMounting(cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, mountPoint : Int, iteration : Int) : Unit = {
    val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
    carrier.CargoHold(mountPoint) match {
      case Some(hold) if !hold.isOccupied =>
        log.debug(s"HandleCheckCargoMounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=64")
        if(distance <= 64) {
          //cargo vehicle is close enough to assume to be physically within the carrier's hold; mount it
          log.info(s"HandleCheckCargoMounting: mounting cargo vehicle in carrier at distance of $distance")
          cargo.MountedIn = carrierGUID
          hold.Occupant = cargo
          cargo.Velocity = None
          vehicleService ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health)))
          vehicleService ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 68, cargo.Shields)))
          StartBundlingPackets()
          val (attachMsg, mountPointMsg) = CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          StopBundlingPackets()
          log.info(s"HandleCheckCargoMounting: $attachMsg")
          log.info(s"HandleCheckCargoMounting: $mountPointMsg")
        }
        else if(distance > 625 || iteration >= 40) {
          //vehicles moved too far away or took too long to get into proper position; abort mounting
          log.info("HandleCheckCargoMounting: cargo vehicle is too far away or didn't mount within allocated time - aborting")
          vehicleService ! VehicleServiceMessage(
            continent.Id,
            VehicleAction.SendResponse(
              player.GUID,
              CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
            )
          )
          //sending packet to the cargo vehicle's client results in player locking himself in his vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        }
        else {
          //cargo vehicle still not in position but there is more time to wait; reschedule check
          import scala.concurrent.ExecutionContext.Implicits.global
          cargoMountTimer = context.system.scheduler.scheduleOnce(250 milliseconds, self, CheckCargoMounting(cargoGUID, carrierGUID, mountPoint, iteration = iteration + 1))
        }
      case None => ;
        log.warn(s"HandleCheckCargoMounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
      case _ =>
        if(iteration == 0) {
          log.warn(s"HandleCheckCargoMounting: carrier vehicle $carrier already possesses cargo in hold #$mountPoint; this operation was initiated incorrectly")
        }
        else {
          log.error(s"HandleCheckCargoMounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40")
        }
    }
  }

  /**
    * Produce an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    * that will set up a realized parent-child association between a ferrying vehicle and a ferried vehicle.
    * @see `CargoMountPointStatusMessage`
    * @see `CargoOrientation`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached;
    *                   also known as a "cargo hold"
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountMessages(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    CargoMountMessages(carrier.GUID, cargo.GUID, mountPoint, CargoOrientation(cargo))
  }

  /**
    * Produce an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    * that will set up a realized parent-child association between a ferrying vehicle and a ferried vehicle.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountMessages(carrierGUID : PlanetSideGUID, cargoGUID : PlanetSideGUID, mountPoint : Int, orientation : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    (
      ObjectAttachMessage(carrierGUID, cargoGUID, mountPoint),
      CargoMountPointStatusMessage(carrierGUID, cargoGUID, cargoGUID, PlanetSideGUID(0), mountPoint, CargoStatus.Occupied, orientation)
    )
  }

  /**
    * The orientation of a cargo vehicle as it is being loaded into and contained by a carrier vehicle.
    * The type of carrier is not an important consideration in determining the orientation, oddly enough.
    * @param vehicle the cargo vehicle
    * @return the orientation as an `Integer` value;
    *         `0` for almost all cases
    */
  def CargoOrientation(vehicle : Vehicle) : Int = {
    if(vehicle.Definition == GlobalDefinitions.router) {
      1
    }
    else {
      0
    }
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForUs(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForUs(attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet only to this client.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param attachMessage an `ObjectAttachMessage` packet suitable for initializing cargo operations
    * @param mountPointStatusMessage a `CargoMountPointStatusMessage` packet suitable for initializing cargo operations
    */
  def CargoMountMessagesForUs(attachMessage : ObjectAttachMessage, mountPointStatusMessage : CargoMountPointStatusMessage) : Unit = {
    sendResponse(attachMessage)
    sendResponse(mountPointStatusMessage)
  }



  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to all other clients, not this one.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForOthers(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForOthers(attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to all other clients, not this one.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param attachMessage an `ObjectAttachMessage` packet suitable for initializing cargo operations
    * @param mountPointStatusMessage a `CargoMountPointStatusMessage` packet suitable for initializing cargo operations
    */
  def CargoMountMessagesForOthers(attachMessage : ObjectAttachMessage, mountPointStatusMessage : CargoMountPointStatusMessage) : Unit = {
    val pguid = player.GUID
    vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(pguid, attachMessage))
    vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(pguid, mountPointStatusMessage))
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to everyone.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForAll(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForUs(attachMessage, mountPointStatusMessage)
    CargoMountMessagesForOthers(attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
    * na
    * @param target na
    */
  def HandleVehicleDamageResolution(target : Vehicle) : Unit = {
    val targetGUID = target.GUID
    val playerGUID = player.GUID
    val players = target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    })
    target.LastShot match { //TODO: collision damage from/in history
      case Some(shot) =>
        if(target.Health > 0) {
          //activity on map
          continent.Activity ! Zone.HotSpot.Activity(shot.target, shot.projectile.owner, shot.hit_pos)
          //alert occupants to damage source
          HandleVehicleDamageAwareness(target, playerGUID, shot)
        }
        else {
          //alert to vehicle death (hence, occupants' deaths)
          HandleVehicleDestructionAwareness(target, shot)
        }
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
        vehicleService ! VehicleServiceMessage(s"${target.Actor}", VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 68, target.Shields))
      case None => ;
    }
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleVehicleDamageAwareness(target : Vehicle, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    //alert occupants to damage source
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      avatarService ! AvatarServiceMessage(tplayer.Name, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.History(lastShot)
          HandleVehicleDamageAwareness(cargo, attribution, lastShot)
        case None => ;
      }
    })
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleVehicleDestructionAwareness(target : Vehicle, lastShot : ResolvedProjectile) : Unit = {
    val playerGUID = player.GUID
    val continentId = continent.Id
    //alert to vehicle death (hence, occupants' deaths)
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      val tplayerGUID = tplayer.GUID
      avatarService ! AvatarServiceMessage(tplayer.Name, AvatarAction.KilledWhileInVehicle(tplayerGUID))
      avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(tplayerGUID, tplayerGUID)) //dead player still sees self
    })
    //vehicle wreckage has no weapons
    target.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.Position += Vector3.z(1)
          cargo.History(lastShot) //necessary to kill cargo vehicle occupants //TODO: collision damage
          HandleVehicleDestructionAwareness(cargo, lastShot) //might cause redundant packets
        case None => ;
      }
    })
    target.Definition match {
      case GlobalDefinitions.ams =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
        BeforeUnloadVehicle(target)
        localService ! LocalServiceMessage(continent.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), target, None))
      case _ => ;
    }
    avatarService ! AvatarServiceMessage(continentId, AvatarAction.Destroy(target.GUID, playerGUID, playerGUID, target.Position))
    vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), continent))
    vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, continent, Some(1 minute)))
  }

  /**
    * na
    * @param target na
    */
  def HandleTurretDeployableDamageResolution(target : TurretDeployable) : Unit = {
    //spitfires and field turrets
    val health = target.Health
    val guid = target.GUID
    val continentId = continent.Id
    if(health <= 0) {
      //if occupants, kill them
      target.Seats.values
        .filter(seat => {
          seat.isOccupied && seat.Occupant.get.isAlive
        })
        .foreach(seat => {
          val tplayer = seat.Occupant.get
          val tplayerGUID = tplayer.GUID
          avatarService ! AvatarServiceMessage(tplayer.Name, AvatarAction.KilledWhileInVehicle(tplayerGUID))
          avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(tplayerGUID, tplayerGUID)) //dead player still sees self
        })
      //destroy weapons
      target.Weapons.values
        .map(slot => slot.Equipment)
        .collect { case Some(weapon) =>
          val wguid = weapon.GUID
          sendResponse(ObjectDeleteMessage(wguid, 0))
          avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(player.GUID, wguid))
        }
      AnnounceDestroyDeployable(target, None)
    }
    avatarService ! AvatarServiceMessage(continentId, AvatarAction.PlanetsideAttribute(guid, 0, health))
  }

  def HandleFacilityTurretDamageResolution(target : FacilityTurret) : Unit = {
    val targetGUID = target.GUID
    val playerGUID = player.GUID
    val continentId = continent.Id
    val players = target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    })
    if(target.Health > 1) { //TODO turret "death" at 0, as is proper
      //alert occupants to damage source
      players.foreach(seat => {
        val tplayer = seat.Occupant.get
        avatarService ! AvatarServiceMessage(tplayer.Name, AvatarAction.HitHint(playerGUID, tplayer.GUID))
      })
    }
    else {
      //alert to vehicle death (hence, occupants' deaths)
      players.foreach(seat => {
        val tplayer = seat.Occupant.get
        val tplayerGUID = tplayer.GUID
        avatarService ! AvatarServiceMessage(tplayer.Name, AvatarAction.KilledWhileInVehicle(tplayerGUID))
        avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(tplayerGUID, tplayerGUID)) //dead player still sees self
      })
      //turret wreckage has no weapons
//      target.Weapons.values
//        .filter {
//          _.Equipment.nonEmpty
//        }
//        .foreach(slot => {
//          val wep = slot.Equipment.get
//          avatarService ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
//        })
//      avatarService ! AvatarServiceMessage(continentId, AvatarAction.Destroy(targetGUID, playerGUID, playerGUID, player.Position))
      target.Health = 1
      vehicleService ! VehicleServiceMessage(continentId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.MaxHealth)) //TODO not necessary
      if(target.Upgrade != TurretUpgrade.None) {
        vehicleService ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(target), continent))
        vehicleService ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(target, continent, TurretUpgrade.None))
      }
    }
    vehicleService ! VehicleServiceMessage(continentId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
  }

  /**
    * na
    * @param tplayer na
    * @param vehicle na
    */
  def HandleNtuCharging(tplayer : Player, vehicle : Vehicle) : Unit = {
    log.trace(s"NtuCharging: Vehicle ${vehicle.GUID} is charging NTU capacitor.")
    if(vehicle.Capacitor < vehicle.Definition.MaximumCapacitor) {
      // Charging
      vehicle.Capacitor += 100
      sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.ceil((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 1L)) // panel glow on
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 49, 1L)) // orb particle effect on

      antChargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuCharging(player, vehicle)) // Repeat until fully charged
    }
    else {
      // Fully charged
      sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.ceil((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI

      // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
      context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))
    }
  }

  /**
    * na
    * @param tplayer na
    * @param vehicle na
    * @param silo_guid na
    */
  def HandleNtuDischarging(tplayer : Player, vehicle : Vehicle, silo_guid : PlanetSideGUID) : Unit = {
    log.trace(s"NtuDischarging: Vehicle ${vehicle.GUID} is discharging NTU into silo $silo_guid")
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 49, 0L)) // orb particle effect off
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 1L)) // panel glow on

    var silo = continent.GUID(silo_guid).get.asInstanceOf[ResourceSilo]
    // Check vehicle is still deployed before continuing. User can undeploy manually or vehicle may not longer be present.
    if(vehicle.DeploymentState == DriveState.Deployed) {
      if(vehicle.Capacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {

        // Make sure we don't exceed the silo maximum charge or remove much NTU from ANT if maximum is reached, or try to make ANT go below 0 NTU
        var chargeToDeposit = Math.min(Math.min(vehicle.Capacitor, 100), (silo.MaximumCharge - silo.ChargeLevel))
        vehicle.Capacitor -= chargeToDeposit
        silo.Actor ! ResourceSilo.UpdateChargeLevel(chargeToDeposit)
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 1L)) // panel glow on & orb particles on
        sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.ceil((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI

        //todo: grant BEP to user
        //todo: grant BEP to squad in range
        //todo: notify map service to update ntu % on map for all users

        //todo: handle silo orb / panel glow properly if more than one person is refilling silo and one player stops. effects should stay on until all players stop

        if(vehicle.Capacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {
          log.trace(s"NtuDischarging: ANT not empty and Silo not full. Scheduling another discharge")
          // Silo still not full and ant still has charge left - keep rescheduling ticks
          antDischargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuDischarging(player, vehicle, silo_guid))
        }
        else {
          log.trace(s"NtuDischarging: ANT NTU empty or Silo NTU full.")
          // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
          context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
          antDischargingTick.cancel()
        }
      }
      else {
        // This shouldn't normally be run, only if the client thinks the ANT has capacitor charge when it doesn't, or thinks the silo isn't full when it is.
        log.warn(s"NtuDischarging: Invalid discharge state. ANT Capacitor: ${vehicle.Capacitor} Silo Capacitor: ${silo.ChargeLevel}")
        // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
        context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
        antDischargingTick.cancel()
      }
    }
    else {
      log.trace(s"NtuDischarging: Vehicle is no longer deployed. Removing effects")
      // Vehicle has changed from deployed and this should be the last timer tick sent
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 0L)) // panel glow off
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
      antDischargingTick.cancel()
    }
  }

  /**
    * na
    * @param progressType na
    * @param tplayer na
    * @param target na
    * @param tool_guid na
    * @param delta na
    * @param completeAction na
    * @param tickAction na
    */
  def HandleHackingProgress(progressType : Int, tplayer : Player, target : PlanetSideServerObject, tool_guid : PlanetSideGUID, delta : Float, completeAction : ()=>Unit, tickAction : Option[()=>Unit]) : Unit = {
    progressBarUpdate.cancel
    if(progressBarValue.isDefined) {
      val progressBarVal : Float = progressBarValue.get + delta
      val vis = if(progressBarVal == 0L) {
        //hack state for progress bar visibility
        HackState.Start
      }
      else if(progressBarVal > 100L) {
        HackState.Finished
      }
      else {
        HackState.Ongoing
      }
      sendResponse(HackMessage(progressType, target.GUID, player.GUID, progressBarVal.toInt, 0L, vis, 8L))
      if(progressBarVal > 100) {
        //done
        progressBarValue = None
        //          sendResponse(HackMessage(0, target.GUID, player.GUID, 100, 1114636288L, HackState.Hacked, 8L))
        completeAction()
      }
      else {
        //continue next tick
        tickAction.getOrElse(() => Unit)()
        progressBarValue = Some(progressBarVal)
        import scala.concurrent.ExecutionContext.Implicits.global
        progressBarUpdate = context.system.scheduler.scheduleOnce(250 milliseconds, self, HackingProgress(progressType, tplayer, target, tool_guid, delta, completeAction))
      }
    }
  }

  /**
    * na
    * @param tplayer na
    */
  def HandleSetCurrentAvatar(tplayer : Player) : Unit = {
    player = tplayer
    val guid = tplayer.GUID
    StartBundlingPackets()
    InitializeDeployableUIElements(avatar)
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 75, 0))
    sendResponse(SetCurrentAvatarMessage(guid, 0, 0))
    sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, true, "", "1 on", None)) //CC on //TODO once per respawn?
    sendResponse(PlayerStateShiftMessage(ShiftState(1, tplayer.Position, tplayer.Orientation.z)))
    if(spectator) {
      sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, false, "", "on", None))
    }
    (0 until DetailedCharacterData.numberOfImplantSlots(tplayer.BEP)).foreach(slot => {
      sendResponse(AvatarImplantMessage(guid, ImplantAction.Initialization, slot, 1)) //init implant slot
      sendResponse(AvatarImplantMessage(guid, ImplantAction.Activation, slot, 0)) //deactivate implant
      //TODO if this implant is Installed but does not have shortcut, add to a free slot or write over slot 61/62/63
    })
    sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 82, 0))
    //TODO if Medkit does not have shortcut, add to a free slot or write over slot 64
    sendResponse(CreateShortcutMessage(guid, 1, 0, true, Shortcut.MEDKIT))
    sendResponse(ChangeShortcutBankMessage(guid, 0))
    //Favorites lists
    val (inf, veh) = avatar.EquipmentLoadouts.Loadouts.partition { case (index, _) => index < 10 }
    inf.foreach {
      case (index, loadout : InfantryLoadout) =>
        sendResponse(FavoritesMessage(LoadoutType.Infantry, guid, index, loadout.label, InfantryLoadout.DetermineSubtypeB(loadout.exosuit, loadout.subtype)))
    }
    veh.foreach {
      case (index, loadout : VehicleLoadout) =>
        sendResponse(FavoritesMessage(LoadoutType.Vehicle, guid, index - 10, loadout.label))
    }
    sendResponse(SetChatFilterMessage(ChatChannel.Local, false, ChatChannel.values.toList)) //TODO will not always be "on" like this
    deadState = DeadState.Alive
    sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, tplayer.Position, player.Faction, true))
    //looking for squad (members)
    if(squadUI.nonEmpty && squadUI(avatar.CharId).index == 0) {
      sendResponse(PlanetsideAttributeMessage(guid, 31, 1))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(guid, 31, 1))
    }
    if(tplayer.LFS || lfs) {
      sendResponse(PlanetsideAttributeMessage(guid, 53, 1))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(guid, 53, 1))
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
    val faction = tplayer.Faction
    continent.DeployableList
      .filter(_.OwnerName.contains(name))
      .foreach(obj => {
        obj.Owner = guid
        drawDeloyableIcon(obj)
      })
    StopBundlingPackets()
    drawDeloyableIcon = DontRedrawIcons
    //assert or transfer vehicle ownership
    continent.GUID(player.VehicleOwned) match {
      case Some(vehicle : Vehicle) if vehicle.OwnerName.contains(tplayer.Name) =>
        vehicle.Owner = guid
        vehicleService ! VehicleServiceMessage(s"${continent.Id}/${tplayer.Faction}", VehicleAction.Ownership(guid, vehicle.GUID))
      case _ =>
        player.VehicleOwned = None
    }
    //if driver of a vehicle, summon any passengers and cargo vehicles left behind on previous continent
    GetVehicleAndSeat() match {
      case (Some(vehicle), Some(0)) =>
        LoadZoneTransferPassengerMessages(
          guid,
          continent.Id,
          TransportVehicleChannelName(vehicle),
          vehicle,
          interstellarFerryTopLevelGUID.getOrElse(vehicle.GUID)
        )
        interstellarFerryTopLevelGUID = None
      case _ => ;
    }
  }

  def FirstTimeSquadSetup() : Unit = {
    sendResponse(SquadDetailDefinitionUpdateMessage.Init)
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(6)))
    //only need to load these once
    avatar.SquadLoadouts.Loadouts.foreach {
      case (index, loadout : SquadLoadout) =>
        sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), index, SquadAction.ListSquadFavorite(loadout.task)))
    }
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.AssociateWithSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.SetListSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitSquadList())
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitCharId())
    squadSetup = SubsequentSpawnSquadSetup
  }

  def SubsequentSpawnSquadSetup() : Unit = {
    if(squadUI.nonEmpty) {
      //sendResponse(PlanetsideAttributeMessage(player.GUID, 31, squad_supplement_id))
      val (_, ourCard) = squadUI.find { case (id, card) => id == player.CharId }.get
      sendResponse(PlanetsideAttributeMessage(player.GUID, 32, ourCard.index))
    }
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case sync @ ControlSync(diff, _, _, _, _, _, fa, fb) =>
        log.trace(s"SYNC: $sync")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(ControlSyncResp(diff, serverTick, fa, fb, fb, fa))

      case TeardownConnection(_) =>
        log.info("Good bye")

      case default =>
        log.warn(s"Unhandled ControlPacket $default")
    }
  }

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>
      val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
      log.info(s"New world login to $server with Token:$token. $clientVersion")
      //TODO begin temp player character auto-loading; remove later
      import net.psforever.objects.GlobalDefinitions._
      import net.psforever.types.CertificationType._
      val faction = PlanetSideEmpire.VS
      val avatar = new Avatar(41605313L+sessionId, s"TestCharacter$sessionId", faction, CharacterGender.Female, 41, CharacterVoice.Voice1)
      avatar.Certifications += StandardAssault
      avatar.Certifications += MediumAssault
      avatar.Certifications += StandardExoSuit
      avatar.Certifications += AgileExoSuit
      avatar.Certifications += ReinforcedExoSuit
      avatar.Certifications += ATV
      avatar.Certifications += Harasser
      //
      avatar.Certifications += InfiltrationSuit
      avatar.Certifications += Sniping
      avatar.Certifications += AntiVehicular
      avatar.Certifications += HeavyAssault
      avatar.Certifications += SpecialAssault
      avatar.Certifications += EliteAssault
      avatar.Certifications += GroundSupport
      avatar.Certifications += GroundTransport
      avatar.Certifications += Flail
      avatar.Certifications += Switchblade
      avatar.Certifications += AssaultBuggy
      avatar.Certifications += ArmoredAssault1
      avatar.Certifications += ArmoredAssault2
      avatar.Certifications += AirCavalryScout
      avatar.Certifications += AirCavalryAssault
      avatar.Certifications += AirCavalryInterceptor
      avatar.Certifications += AirSupport
      avatar.Certifications += GalaxyGunship
      avatar.Certifications += Phantasm
      avatar.Certifications += UniMAX
      avatar.Certifications += Engineering
      avatar.Certifications += CombatEngineering
      avatar.Certifications += FortificationEngineering
      avatar.Certifications += AssaultEngineering
      avatar.Certifications += Hacking
      avatar.Certifications += AdvancedHacking
      avatar.CEP = 6000001
      this.avatar = avatar

      InitializeDeployableQuantities(avatar) //set deployables ui elements
      AwardBattleExperiencePoints(avatar, 1000000L)
      player = new Player(avatar)
      player.Position = Vector3(3561.0f, 2854.0f, 90.859375f) //home3, HART C
//      player.Position = Vector3(3940.3984f, 4343.625f, 266.45312f) //z6, Anguta
//      player.Position = Vector3(3571.2266f, 3278.0938f, 119.0f) //ce test
      player.Orientation = Vector3(0f, 0f, 90f)
      //player.Position = Vector3(4262.211f ,4067.0625f ,262.35938f) //z6, Akna.tower
      //player.Orientation = Vector3(0f, 0f, 132.1875f)
//      player.ExoSuit = ExoSuitType.MAX //TODO strange issue; divide number above by 10 when uncommenting
      player.Slot(0).Equipment = Tool(GlobalDefinitions.StandardPistol(player.Faction))
      player.Slot(2).Equipment = Tool(suppressor)
      player.Slot(4).Equipment = Tool(GlobalDefinitions.StandardMelee(player.Faction))
      player.Slot(6).Equipment = AmmoBox(bullet_9mm)
      player.Slot(9).Equipment = AmmoBox(bullet_9mm)
      player.Slot(12).Equipment = AmmoBox(bullet_9mm)
      player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
      player.Slot(36).Equipment = AmmoBox(GlobalDefinitions.StandardPistolAmmo(player.Faction))
      player.Slot(39).Equipment = SimpleItem(remote_electronics_kit)
      player.Locker.Inventory += 0 -> SimpleItem(remote_electronics_kit)
      player.Inventory.Items.foreach { _.obj.Faction = faction }
      //TODO end temp player character auto-loading
      self ! ListAccountCharacters
      import scala.concurrent.ExecutionContext.Implicits.global
      clientKeepAlive.cancel
      clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())

    case msg @ MountVehicleCargoMsg(player_guid, vehicle_guid, cargo_vehicle_guid, unk4) =>
      log.info(msg.toString)
      (continent.GUID(vehicle_guid), continent.GUID(cargo_vehicle_guid)) match {
        case (Some(_ : Vehicle), Some(carrier : Vehicle)) =>
          carrier.Definition.Cargo.headOption match {
            case Some((mountPoint, _)) => //begin the mount process - open the cargo door
              val reply = CargoMountPointStatusMessage(cargo_vehicle_guid, PlanetSideGUID(0), vehicle_guid, PlanetSideGUID(0), mountPoint, CargoStatus.InProgress, 0)
              log.debug(reply.toString)
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.SendResponse(player.GUID, reply))
              sendResponse(reply)

              import scala.concurrent.duration._
              import scala.concurrent.ExecutionContext.Implicits.global
              // Start timer to check every second if the vehicle is close enough to mount, or far enough away to cancel the mounting
              cargoMountTimer.cancel
              cargoMountTimer = context.system.scheduler.scheduleOnce(1 second, self, CheckCargoMounting(vehicle_guid, cargo_vehicle_guid, mountPoint, iteration = 0))
            case None =>
              log.warn(s"MountVehicleCargoMsg: target carrier vehicle (${carrier.Definition.Name}) does not have a cargo hold")
          }
        case (None, _) | (Some(_), None) =>
          log.warn(s"MountVehicleCargoMsg: one or more of the target vehicles do not exist - $cargo_vehicle_guid or $vehicle_guid")
        case _ => ;
      }

    case msg @ DismountVehicleCargoMsg(player_guid, cargo_guid, bailed, requestedByPassenger, kicked) =>
      log.info(msg.toString)
      if(!requestedByPassenger) {
        DismountVehicleCargo(player_guid, cargo_guid, bailed, requestedByPassenger, kicked)
      }

    case msg @ CharacterCreateRequestMessage(name, head, voice, gender, empire) =>
      log.info("Handling " + msg)
      sendResponse(ActionResultMessage.Pass)
      self ! ListAccountCharacters

    case msg @ CharacterRequestMessage(charId, action) =>
      log.info("Handling " + msg)
      action match {
        case CharacterRequestAction.Delete =>
          sendResponse(ActionResultMessage.Fail(1))
        case CharacterRequestAction.Select =>
          //TODO check if can spawn on last continent/location from player?
          //TODO if yes, get continent guid accessors
          //TODO if no, get sanctuary guid accessors and reset the player's expectations
          cluster ! InterstellarCluster.RequestClientInitialization()
        case default =>
          log.error("Unsupported " + default + " in " + msg)
      }

    case KeepAliveMessage(code) =>
      sendResponse(KeepAliveMessage())

    case msg @ BeginZoningMessage() =>
      log.info("Reticulating splines ...")
      val continentId = continent.Id
      traveler.zone = continentId
      val faction = player.Faction
      val factionOnContinentChannel = s"$continentId/$faction"
      avatarService ! Service.Join(continentId)
      avatarService ! Service.Join(factionOnContinentChannel)
      localService ! Service.Join(continentId)
      localService ! Service.Join(factionOnContinentChannel)
      vehicleService ! Service.Join(continentId)
      vehicleService ! Service.Join(factionOnContinentChannel)
      configZone(continent)
      sendResponse(TimeOfDayMessage(1191182336))
      //custom
      sendResponse(ContinentalLockUpdateMessage(13, PlanetSideEmpire.VS)) // "The VS have captured the VS Sanctuary."
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 0)) // disable festive backpacks
      //(0 to 255).foreach(i => { sendResponse(SetEmpireMessage(PlanetSideGUID(i), PlanetSideEmpire.VS)) })

      //find and reclaim own deployables, if any
      val guid = player.GUID
      val foundDeployables = continent.DeployableList.filter(obj => obj.OwnerName.contains(player.Name) && obj.Health > 0)
      localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(foundDeployables, continent))
      foundDeployables.foreach(obj => {
        if(avatar.Deployables.Add(obj)) {
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
        val objGUID = obj.GUID
        val definition = obj.Definition
        sendResponse(
          ObjectCreateMessage(
            definition.ObjectId,
            objGUID,
            definition.Packet.ConstructorData(obj).get
          )
        )
        //seated players
        obj.asInstanceOf[Mountable].Seats.values
          .map(_.Occupant)
          .collect {
            case Some(occupant) =>
              if(occupant.isAlive) {
                val tdefintion = occupant.Definition
                sendResponse(
                  ObjectCreateMessage(
                    tdefintion.ObjectId,
                    occupant.GUID,
                    ObjectCreateMessageParent(objGUID, 0),
                    tdefintion.Packet.ConstructorData(occupant).get
                  )
                )
              }
          }
      })
      normal
        .filter(_.Definition.DeployCategory == DeployableCategory.Sensors)
        .foreach(obj => { sendResponse(TriggerEffectMessage(obj.GUID, "on", true, 1000)) })
      //draw our faction's deployables on the map
      continent.DeployableList
        .filter(obj => obj.Faction == faction && obj.Health > 0)
        .foreach(obj => {
          val deployInfo = DeployableInfo(obj.GUID, Deployable.Icon(obj.Definition.Item), obj.Position, obj.Owner.getOrElse(PlanetSideGUID(0)))
          sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
        })
      //render Equipment that was dropped into zone before the player arrived
      continent.EquipmentOnGround.foreach(item => {
        val definition = item.Definition
        sendResponse(
          ObjectCreateMessage(
            definition.ObjectId,
            item.GUID,
            DroppedItemData(PlacementData(item.Position, item.Orientation), definition.Packet.ConstructorData(item).get)
          )
        )
      })
      //load active players in zone (excepting players who are seated or players who are us)
      val live = continent.LivePlayers
      live.filterNot(tplayer => { tplayer.GUID == player.GUID || tplayer.VehicleSeated.nonEmpty })
        .foreach(char => {
          val tdefintion = char.Definition
          sendResponse(ObjectCreateMessage(tdefintion.ObjectId, char.GUID, char.Definition.Packet.ConstructorData(char).get))
          if(char.UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored) {
            sendResponse(PlanetsideAttributeMessage(char.GUID, 19, 1))
          }
        })
      //load corpses in zone
      continent.Corpses.foreach {
        TurnPlayerIntoCorpse
      }
      //load vehicles in zone (put separate the one we may be using)
      val (wreckages, (vehicles, usedVehicle)) = {
        val (a, b) = continent.Vehicles.partition(vehicle => { vehicle.Health == 0 && vehicle.Definition.DestroyedModel.nonEmpty })
        (a, (continent.GUID(player.VehicleSeated) match {
          case Some(vehicle : Vehicle) if vehicle.PassengerInSeat(player).isDefined =>
            b.partition { _.GUID != vehicle.GUID }
          case None =>
            (b, List.empty[Vehicle])
          case _ =>
            //throw error since VehicleSeated didn't point to a vehicle?
            player.VehicleSeated = None
            (b, List.empty[Vehicle])
        }))
      }
      //active vehicles (and some wreckage)
      vehicles.foreach(vehicle => {
        val vguid = vehicle.GUID
        val vdefinition = vehicle.Definition
        sendResponse(ObjectCreateMessage(vdefinition.ObjectId, vguid, vdefinition.Packet.ConstructorData(vehicle).get))
        //occupants other than driver
        vehicle.Seats
          .filter({ case(index, seat) => seat.isOccupied && live.contains(seat.Occupant.get) && index > 0 })
          .foreach({ case(index, seat) =>
            val tplayer = seat.Occupant.get
            val tdefintion = tplayer.Definition
            sendResponse(
              ObjectCreateMessage(
                tdefintion.ObjectId,
                tplayer.GUID,
                ObjectCreateMessageParent(vguid, index),
                tdefintion.Packet.ConstructorData(tplayer).get
              )
            )
          })
        ReloadVehicleAccessPermissions(vehicle)
      })
      //our vehicle would have already been loaded; see NewPlayerLoaded/AvatarCreate
      usedVehicle.headOption match {
        case Some(vehicle) if !vehicle.PassengerInSeat(player).contains(0) =>
          //if passenger, attempt to depict any other passengers already in this zone
          val vguid = vehicle.GUID
          vehicle.Seats
            .filter({ case(index, seat) => seat.isOccupied && !seat.Occupant.contains(player) && live.contains(seat.Occupant.get) && index > 0 })
            .foreach({ case(index, seat) =>
              val tplayer = seat.Occupant.get
              val tdefintion = tplayer.Definition
              sendResponse(
                ObjectCreateMessage(
                  tdefintion.ObjectId,
                  tplayer.GUID,
                  ObjectCreateMessageParent(vguid, index),
                  tdefintion.Packet.ConstructorData(tplayer).get
                )
              )
            })
        case _ => ; //driver, or no vehicle
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
      vehicles.collect { case vehicle if vehicle.CargoHolds.nonEmpty =>
        vehicle.CargoHolds.collect({ case (index, hold) if hold.isOccupied => {
          CargoMountBehaviorForAll(vehicle, hold.Occupant.get, index) //CargoMountBehaviorForUs can fail to attach the cargo vehicle on some clients
        }})
      }
      //special deploy states
      val deployedVehicles = vehicles.filter(_.DeploymentState == DriveState.Deployed)
      deployedVehicles.filter(_.Definition == GlobalDefinitions.ams).foreach(obj => {
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 81, 1))
      })
      deployedVehicles.filter(_.Definition == GlobalDefinitions.router).foreach(obj => {
        sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deploying, 0, false, Vector3.Zero))
        sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Deployed, 0, false, Vector3.Zero))
        ToggleTeleportSystem(obj, TelepadLike.AppraiseTeleportationSystem(obj, continent))
      })

      //implant terminals
      continent.Map.TerminalToInterface.foreach({ case ((terminal_guid, interface_guid)) =>
        val parent_guid = PlanetSideGUID(terminal_guid)
        continent.GUID(interface_guid) match {
          case Some(obj : Terminal) =>
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
          case Some(obj : Mountable) =>
            obj.Seats(0).Occupant match {
              case Some(tplayer) =>
                val tdefintion = tplayer.Definition
                sendResponse(
                  ObjectCreateMessage(
                    tdefintion.ObjectId,
                    tplayer.GUID,
                    ObjectCreateMessageParent(parent_guid, 0),
                    tdefintion.Packet.ConstructorData(tplayer).get
                  )
                )
              case None => ;
            }
          case _ => ;
        }
      })

      //base turrets
      continent.Map.TurretToWeapon.foreach({ case((turret_guid, weapon_guid)) =>
        val parent_guid = PlanetSideGUID(turret_guid)
        continent.GUID(turret_guid) match {
          case Some(turret : FacilityTurret) =>
            //attached weapon
            turret.ControlledWeapon(1) match {
              case Some(obj : Tool) =>
                val objDef = obj.Definition
                sendResponse(
                  ObjectCreateMessage(
                    objDef.ObjectId,
                    obj.GUID,
                    ObjectCreateMessageParent(parent_guid, 1),
                    objDef.Packet.ConstructorData(obj).get
                  )
                )
              case _ => ;
            }
            //reserved ammunition?
            //TODO need to register if it exists
            //seat turret occupant
            turret.Seats(0).Occupant match {
              case Some(tplayer) =>
                val tdefintion = tplayer.Definition
                sendResponse(
                  ObjectCreateMessage(
                    tdefintion.ObjectId,
                    tplayer.GUID,
                    ObjectCreateMessageParent(parent_guid, 0),
                    tdefintion.Packet.ConstructorData(tplayer).get
                  )
                )
              case None => ;
            }
          case _ => ;
        }
      })
      vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.UpdateAmsSpawnPoint(continent))
      self ! SetCurrentAvatar(player)

    case msg @ PlayerStateMessageUpstream(avatar_guid, pos, vel, yaw, pitch, yaw_upper, seq_time, unk3, is_crouching, is_jumping, unk4, is_cloaking, unk5, unk6) =>
      if(deadState == DeadState.Alive) {
        if(!player.Crouching && is_crouching) { //SQUAD TESTING CODE
          //...
        }
        player.Position = pos
        player.Velocity = vel
        player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
        player.FacingYawUpper = yaw_upper
        player.Crouching = is_crouching
        player.Jumping = is_jumping

        if(vel.isDefined && usingMedicalTerminal.isDefined) {
          continent.GUID(usingMedicalTerminal) match {
            case Some(term : Terminal with ProximityUnit) =>
              StopUsingProximityUnit(term)
            case _ => ;
          }
        }
        accessedContainer match {
          case Some(veh : Vehicle) =>
            if(vel.isDefined || Vector3.DistanceSquared(player.Position, veh.Position) > 100) {
              val guid = player.GUID
              sendResponse(UnuseItemMessage(guid, veh.GUID))
              sendResponse(UnuseItemMessage(guid, guid))
              veh.AccessingTrunk = None
              UnAccessContents(veh)
              accessedContainer = None
            }
          case Some(container) => //just in case
            if(vel.isDefined) {
              val guid = player.GUID
              // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
              if(container.HasGUID) {
                sendResponse(UnuseItemMessage(guid, container.GUID))
              }
              sendResponse(UnuseItemMessage(guid, guid))
              accessedContainer = None
            }
          case None => ;
        }
        val wepInHand : Boolean = player.Slot(player.DrawnSlot).Equipment match {
          case Some(item) => item.Definition == GlobalDefinitions.bolt_driver
          case None => false
        }
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlayerState(avatar_guid, msg, spectator, wepInHand))
        updateSquad()
      }

    case msg @ ChildObjectStateMessage(object_guid, pitch, yaw) =>
      //the majority of the following check retrieves information to determine if we are in control of the child
      FindContainedWeapon match {
        case (Some(_), Some(tool)) =>
          if(tool.GUID == object_guid) {
            //TODO set tool orientation?
            player.Orientation = Vector3(0f, pitch, yaw)
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.ChildObjectState(player.GUID, object_guid, pitch, yaw))
          }
          else {
            log.warn(s"ChildObjectState: ${player.Name} is using a different controllable agent than #${object_guid.guid}")
          }
        case (Some(obj), None) =>
          log.warn(s"ChildObjectState: ${player.Name} can not find any controllable agent, let alone #${object_guid.guid}")
        case (None, _) => ;
          //TODO status condition of "playing getting out of vehicle to allow for late packets without warning
          //log.warn(s"ChildObjectState: player $player not related to anything with a controllable agent")
      }

    case msg @ VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, flight, unk6, unk7, wheels, unk9, unkA) =>
      if(deadState == DeadState.Alive) {
        GetVehicleAndSeat() match {
          case (Some(obj), Some(0)) =>
            val seat = obj.Seats(0)
            //we're driving the vehicle
            player.Position = pos //convenient
            if(seat.ControlledWeapon.isEmpty) {
              player.Orientation = Vector3.z(ang.z) //convenient
            }
            obj.Position = pos
            obj.Orientation = ang
            if(obj.MountedIn.isEmpty) {
              obj.Velocity = vel
              if(obj.Definition.CanFly) {
                obj.Flying = flight.nonEmpty //usually Some(7)
              }
              vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.VehicleState(player.GUID, vehicle_guid, unk1, pos, ang, vel, flight, unk6, unk7, wheels, unk9, unkA))
            }
            //vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.VehicleState(player.GUID, vehicle_guid, unk1, pos, ang, vel, flight, unk6, unk7, wheels, unk9, unkA))
            updateSquad()
          case (None, _) =>
            //log.error(s"VehicleState: no vehicle $vehicle_guid found in zone")
            //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
          case (_, Some(index)) =>
            log.error(s"VehicleState: player should not be dispatching this kind of packet from vehicle#$vehicle_guid  when not the driver ($index)")
          case _ => ;
        }
      }
      //log.info(s"VehicleState: $msg")

    case msg @ VehicleSubStateMessage(vehicle_guid, player_guid, vehicle_pos, vehicle_ang, vel, unk1, unk2) =>
    //log.info(s"VehicleSubState: $vehicle_guid, $player_guid, $vehicle_pos, $vehicle_ang, $vel, $unk1, $unk2")

    case msg @ ProjectileStateMessage(projectile_guid, shot_pos, shot_vector, unk1, unk2, unk3, unk4, time_alive) =>
    //log.info("ProjectileState: " + msg)

    case msg @ ReleaseAvatarRequestMessage() =>
      log.info(s"ReleaseAvatarRequest: ${player.GUID} on ${continent.Id} has released")
      reviveTimer.cancel
      continent.Population ! Zone.Population.Release(avatar)
      GoToDeploymentMap()
      player.VehicleSeated match {
        case None =>
          FriskCorpse(player)
          if(!WellLootedCorpse(player)) {
            TurnPlayerIntoCorpse(player)
            continent.Population ! Zone.Corpse.Add(player) //TODO move back out of this match case when changing below issue
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.Release(player, continent))
          }
          else {
            //no items in inventory; leave no corpse
            val player_guid = player.GUID
            sendResponse(ObjectDeleteMessage(player_guid, 0))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid, 0))
            taskResolver ! GUIDTask.UnregisterPlayer(player)(continent.GUID)
          }

        case Some(_) =>
          val player_guid = player.GUID
          sendResponse(ObjectDeleteMessage(player_guid, 0))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid, 0))
          self ! PacketCoding.CreateGamePacket(0, DismountVehicleMsg(player_guid, BailType.Normal, true)) //let vehicle try to clean up its fields

          import scala.concurrent.ExecutionContext.Implicits.global
          context.system.scheduler.scheduleOnce(50 milliseconds, self, UnregisterCorpseOnVehicleDisembark(player))
      }

    case msg @ SpawnRequestMessage(u1, spawn_type, u3, u4, zone_number) =>
      log.info(s"SpawnRequestMessage: $msg")
      if(deadState != DeadState.RespawnTime) {
        deadState = DeadState.RespawnTime
        cluster ! Zone.Lattice.RequestSpawnPoint(zone_number.toInt, player, spawn_type.id.toInt)
      }
      else {
        log.warn("SpawnRequestMessage: request consumed; already respawning ...")
      }

    case msg @ SetChatFilterMessage(send_channel, origin, whitelist) =>
      //log.info("SetChatFilters: " + msg)

    case msg @ ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
      var makeReply : Boolean = true
      var echoContents : String = contents
      val trimContents = contents.trim
      //TODO messy on/off strings may work
      if(messagetype == ChatMessageType.CMT_FLY) {
        if(trimContents.equals("on")) {
          flying = true
        }
        else if(trimContents.equals("off")) {
          flying = false
        }
      }
      else if(messagetype == ChatMessageType.CMT_SPEED) {
        speed = {
          try {
            trimContents.toFloat
          }
          catch {
            case _ : Exception =>
              echoContents = "1.000"
              1f
          }
        }
      }
      else if(messagetype == ChatMessageType.CMT_TOGGLESPECTATORMODE) {
        if(trimContents.equals("on")) {
          spectator = true
        }
        else if(contents.trim.equals("off")) {
          spectator = false
        }
      }

      CSRZone.read(traveler, msg) match {
        case (true, zone, pos) if player.isAlive =>
          deadState = DeadState.Release //cancel movement updates
          PlayerActionsToCancel()
          continent.GUID(player.VehicleSeated) match {
            case Some(vehicle : Vehicle) if vehicle.MountedIn.isEmpty =>
              vehicle.PassengerInSeat(player) match {
                case Some(0) =>
                  vehicle.Position = pos
                  LoadZonePhysicalSpawnPoint(zone, pos, Vector3.Zero, 0)
                case _ => //not seated as the driver, in which case we can't move
                  deadState = DeadState.Alive
              }
            case None =>
              player.Position = pos
              //avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, player.GUID))
              LoadZonePhysicalSpawnPoint(zone, pos, Vector3.Zero, 0)
            case _ => //seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
              deadState = DeadState.Alive
          }

        case (_, _, _) => ;
      }

      CSRWarp.read(traveler, msg) match {
        case (true, pos) if player.isAlive =>
          deadState = DeadState.Release //cancel movement updates
          PlayerActionsToCancel()
          continent.GUID(player.VehicleSeated) match {
            case Some(vehicle : Vehicle) if vehicle.MountedIn.isEmpty =>
              vehicle.PassengerInSeat(player) match {
                case Some(0) =>
                  vehicle.Position = pos
                  LoadZonePhysicalSpawnPoint(continent.Id, pos, Vector3.z(vehicle.Orientation.z), 0)
                case _ => //not seated as the driver, in which case we can't move
                  deadState = DeadState.Alive
              }
            case None =>
              player.Position = pos
              sendResponse(PlayerStateShiftMessage(ShiftState(0, pos, player.Orientation.z, None)))
              deadState = DeadState.Alive //must be set here
            case _ => //seated in something that is not a vehicle or the vehicle is cargo, in which case we can't move
              deadState = DeadState.Alive
          }

        case (_, _) => ;
      }

      // TODO: Prevents log spam, but should be handled correctly
      if(messagetype != ChatMessageType.CMT_TOGGLE_GM) {
        log.info("Chat: " + msg)
      }
      else {
        makeReply = false
      }
      if(messagetype == ChatMessageType.CMT_SUICIDE) {
        if(player.isAlive && deadState != DeadState.Release) {
          Suicide(player)
        }
      }
      if(messagetype == ChatMessageType.CMT_DESTROY) {
        val guid = contents.toInt
        continent.Map.TerminalToSpawnPad.get(guid) match {
          case Some(padGUID) =>
            continent.GUID(padGUID).get.asInstanceOf[VehicleSpawnPad].Actor ! VehicleSpawnControl.ProcessControl.Flush
          case None =>
            self ! PacketCoding.CreateGamePacket(0, RequestDestroyMessage(PlanetSideGUID(guid)))
        }
      }
      if(messagetype == ChatMessageType.CMT_VOICE) {
        sendResponse(ChatMsg(ChatMessageType.CMT_VOICE, false, player.Name, contents, None))
      }
      // TODO: handle this appropriately
      if(messagetype == ChatMessageType.CMT_QUIT) {
        sendResponse(DropCryptoSession())
        sendResponse(DropSession(sessionId, "user quit"))
      }
      //dev hack; consider bang-commands to complement slash-commands in future
      if(trimContents.equals("!loc")) {
        echoContents = s"zone=${continent.Id} pos=${player.Position.x},${player.Position.y},${player.Position.z}; ori=${player.Orientation.x},${player.Orientation.y},${player.Orientation.z}"
        log.info(echoContents)
      }
      else if(trimContents.equals("!ams")) {
        makeReply = false
        if(deadState == DeadState.Release) { //player is on deployment screen (either dead or deconstructed)
          cluster ! Zone.Lattice.RequestSpawnPoint(continent.Number, player, 2)
        }
      }
      // TODO: Depending on messagetype, may need to prepend sender's name to contents with proper spacing
      // TODO: Just replays the packet straight back to sender; actually needs to be routed to recipients!
      if(makeReply) {
        sendResponse(ChatMsg(messagetype, has_wide_contents, recipient, echoContents, note_contents))
      }

    case msg @ VoiceHostRequest(unk, PlanetSideGUID(player_guid), data) =>
      log.info("Player "+player_guid+" requested in-game voice chat.")
      sendResponse(VoiceHostKill())

    case msg @ VoiceHostInfo(player_guid, data) =>
      sendResponse(VoiceHostKill())

    case msg @ ChangeAmmoMessage(item_guid, unk1) =>
      log.info("ChangeAmmo: " + msg)
      FindContainedEquipment match {
        case(Some(_), Some(obj : ConstructionItem)) =>
          PerformConstructionItemAmmoChange(obj, obj.AmmoTypeIndex)
        case (Some(obj), Some(tool : Tool)) =>
          PerformToolAmmoChange(tool, obj)
        case (_, Some(obj)) =>
          log.error(s"ChangeAmmo: the object ${obj.Definition.Name} is not a valid type")
        case (_, None) =>
          log.error(s"ChangeAmmo: can not find $item_guid")
      }

    case msg @ ChangeFireModeMessage(item_guid, fire_mode) =>
      log.info("ChangeFireMode: " + msg)
      FindEquipment match {
        case Some(obj : PlanetSideGameObject with FireModeSwitch[_]) =>
          val originalModeIndex = obj.FireModeIndex
          obj match {
            case cItem : ConstructionItem =>
              NextConstructionItemFireMode(cItem, originalModeIndex)
            case _ =>
              obj.NextFireMode
          }
          val modeIndex = obj.FireModeIndex
          val tool_guid = obj.GUID
          if(originalModeIndex == modeIndex) {
            obj.FireModeIndex = originalModeIndex
            sendResponse(ChangeFireModeMessage(tool_guid, originalModeIndex)) //reinforcement
          }
          else {
            log.info(s"ChangeFireMode: changing $tool_guid to fire mode $modeIndex")
            sendResponse(ChangeFireModeMessage(tool_guid, modeIndex))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireMode(player.GUID, tool_guid, modeIndex))
          }
        case Some(_) =>
          log.error(s"ChangeFireMode: the object that was found for $item_guid does not possess fire modes")
        case None =>
          log.error(s"ChangeFireMode: can not find $item_guid")
      }

    case msg @ ChangeFireStateMessage_Start(item_guid) =>
      log.info("ChangeFireState_Start: " + msg)
      if(shooting.isEmpty) {
        FindEquipment match {
          case Some(tool : Tool) =>
            if(tool.Magazine > 0 || prefire.contains(item_guid)) {
              prefire = None
              shooting = Some(item_guid)
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(player.GUID, item_guid))
            }
            else {
              log.warn(s"ChangeFireState_Start: ${tool.Definition.Name} magazine is empty before trying to shoot bullet")
              EmptyMagazine(item_guid, tool)
            }
          case Some(_) => //permissible, for now
            prefire = None
            shooting = Some(item_guid)
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(player.GUID, item_guid))
          case None =>
            log.error(s"ChangeFireState_Start: can not find $item_guid")
        }
      }

    case msg @ ChangeFireStateMessage_Stop(item_guid) =>
      log.info("ChangeFireState_Stop: " + msg)
      prefire = None
      val weapon : Option[Equipment] = if(shooting.contains(item_guid)) {
        shooting = None
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Stop(player.GUID, item_guid))
        FindEquipment
      }
      else {
        //some weapons, e.g., the decimator, do not send a ChangeFireState_Start on the last shot
        FindEquipment match {
          case Some(tool) =>
            if(tool.Definition == GlobalDefinitions.phoenix) {
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(player.GUID, item_guid))
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Stop(player.GUID, item_guid))
            }
            Some(tool)
          case _ =>
            log.warn(s"ChangeFireState_Stop: received an unexpected message about $item_guid")
            None
        }
      }
      weapon match {
        case Some(tool : Tool) =>
          if(tool.Magazine == 0) {
            FireCycleCleanup(tool)
          }
        case Some(trigger : BoomerTrigger) =>
          val playerGUID = player.GUID
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(playerGUID, item_guid))
          continent.GUID(trigger.Companion) match {
            case Some(boomer : BoomerDeployable) =>
              val boomerGUID = boomer.GUID
              boomer.Exploded = true
              sendResponse(TriggerEffectMessage(boomerGUID, "detonate_boomer"))
              sendResponse(PlanetsideAttributeMessage(boomerGUID, 29, 1))
              sendResponse(ObjectDeleteMessage(boomerGUID, 0))
              localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerEffect(playerGUID, "detonate_boomer", boomerGUID))
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(boomerGUID, 29, 1))
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(playerGUID, boomerGUID))
              localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(boomer, continent, Some(0 seconds)))
            case Some(_) | None => ;
          }
          FindEquipmentToDelete(item_guid, trigger)
          trigger.Companion = None
        case _ => ;
      }
      progressBarUpdate.cancel //TODO independent action?

    case msg @ EmoteMsg(avatar_guid, emote) =>
      log.info("Emote: " + msg)
      sendResponse(EmoteMsg(avatar_guid, emote))

    case msg @ DropItemMessage(item_guid) =>
      log.info(s"DropItem: $msg")
      continent.GUID(item_guid) match {
        case Some(item : Equipment) =>
          player.FreeHand.Equipment match {
            case Some(_) =>
              if(item.GUID == item_guid) {
                continent.Ground ! Zone.Ground.DropItem(item, player.Position, player.Orientation)
              }
            case None =>
              log.warn(s"DropItem: $player wanted to drop a $item, but it wasn't at hand")
          }
        case Some(obj) => //TODO LLU
          log.warn(s"DropItem: $player wanted to drop a $obj, but that isn't possible")
        case None =>
          log.warn(s"DropItem: $player wanted to drop an item ($item_guid), but it was nowhere to be found")
      }

    case msg @ PickupItemMessage(item_guid, player_guid, unk1, unk2) =>
      log.info(s"PickupItem: $msg")
      continent.GUID(item_guid) match {
        case Some(item : Equipment) =>
          player.Fit(item) match {
            case Some(_) =>
              continent.Ground ! Zone.Ground.PickupItem(item_guid)
            case None => //skip
              sendResponse(ActionResultMessage.Fail(16)) //error code?
          }
        case _ =>
          log.warn(s"PickupItem: $player requested an item that doesn't exist in this zone; assume client-side garbage data")
          sendResponse(ObjectDeleteMessage(item_guid, 0))
      }

    case msg @ ReloadMessage(item_guid, ammo_clip, unk1) =>
      log.info("Reload: " + msg)
      FindContainedWeapon match {
        case (Some(obj), Some(tool : Tool)) =>
          val currentMagazine : Int = tool.Magazine
          val magazineSize : Int = tool.MaxMagazine
          val reloadValue : Int = magazineSize - currentMagazine
          if(magazineSize > 0 && reloadValue > 0) {
            FindEquipmentStock(obj, FindAmmoBoxThatUses(tool.AmmoType), reloadValue, CountAmmunition).reverse match {
              case Nil =>
                log.warn(s"ReloadMessage: no ammunition could be found for $item_guid")
              case x :: xs =>
                val (deleteFunc, modifyFunc) : ((Int, AmmoBox)=>Unit, (AmmoBox, Int)=>Unit) = obj match {
                  case (veh : Vehicle) =>
                    (DeleteEquipmentFromVehicle(veh), ModifyAmmunitionInVehicle(veh))
                  case _ =>
                    (DeleteEquipment(obj), ModifyAmmunition(obj))
                }
                xs.foreach(item => {
                  deleteFunc(item.start, item.obj.asInstanceOf[AmmoBox])
                })
                val box = x.obj.asInstanceOf[AmmoBox]
                val tailReloadValue : Int = if(xs.isEmpty) { 0 } else { xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).reduceLeft(_ + _) }
                val sumReloadValue : Int = box.Capacity + tailReloadValue
                val actualReloadValue = (if(sumReloadValue <= reloadValue) {
                  deleteFunc(x.start, box)
                  sumReloadValue
                }
                else {
                  modifyFunc(box, reloadValue - tailReloadValue)
                  reloadValue
                }) + currentMagazine
                log.info(s"ReloadMessage: success, $tool <- $actualReloadValue ${tool.AmmoType}")
                tool.Magazine = actualReloadValue
                sendResponse(ReloadMessage(item_guid, actualReloadValue, unk1))
                avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.Reload(player.GUID, item_guid))
            }
          }
          else {
            log.warn(s"ReloadMessage: item $item_guid can not reload (full=$magazineSize, want=$reloadValue)")
          }
        case (_, Some(_)) =>
          log.error(s"ReloadMessage: the object that was found for $item_guid was not a Tool")
        case (_, None) =>
          log.error(s"ReloadMessage: can not find $item_guid")
      }

    case msg @ ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
      log.info(s"ObjectHeld: $msg")
      val before = player.DrawnSlot
      if(before != held_holsters) {
        if(player.ExoSuit == ExoSuitType.MAX && held_holsters != 0) {
          log.info(s"ObjectHeld: $player is denied changing hands to $held_holsters as a MAX")
          player.DrawnSlot = 0
          sendResponse(ObjectHeldMessage(avatar_guid, 0, true))
        }
        else if((player.DrawnSlot = held_holsters) != before) {
          avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ObjectHeld(player.GUID, player.LastDrawnSlot))

          // Ignore non-equipment holsters
          //todo: check current suit holster slots?
          if(held_holsters >= 0 && held_holsters < 5) {
            player.Holsters()(held_holsters).Equipment match {
              case Some(unholsteredItem : Equipment) =>
                if(unholsteredItem.Definition == GlobalDefinitions.remote_electronics_kit) {
                  // Player has ulholstered a REK - we need to set an atttribute on the REK itself to change the beam/icon colour to the correct one for the player's hack level
                  avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlanetsideAttribute(unholsteredItem.GUID, 116, GetPlayerHackLevel()))
                }
              case None => ;
            }
          }

          // Stop using proximity terminals if player unholsters a weapon (which should re-trigger the proximity effect and re-holster the weapon)
          if(player.VisibleSlots.contains(held_holsters)) {
            continent.GUID(usingMedicalTerminal) match {
              case Some(term : Terminal with ProximityUnit) =>
                StopUsingProximityUnit(term)
              case _ => ;
            }
          }
        }
      }

    case msg @ AvatarJumpMessage(state) =>
      //log.info("AvatarJump: " + msg)

    case msg @ ZipLineMessage(player_guid,origin_side,action,id,pos) =>
      log.info("ZipLineMessage: " + msg)
      if (!origin_side && action == 0) {
        //doing this lets you use the zip line in one direction, cant come back
        sendResponse(ZipLineMessage(player_guid, origin_side, action, id, pos))
      }
      else if (!origin_side && action == 1) {
        //disembark from zipline at destination !
        sendResponse(ZipLineMessage(player_guid, origin_side, action, 0, pos))
      }
      else if (!origin_side && action == 2) {
        //get off by force
        sendResponse(ZipLineMessage(player_guid, origin_side, action, 0, pos))
      }
      else if (origin_side && action == 0) {
        // for teleporters & the other zipline direction
      }

    case msg @ RequestDestroyMessage(object_guid) =>
      // TODO: Make sure this is the correct response for all cases
      continent.GUID(object_guid) match {
        case Some(vehicle : Vehicle) =>
          if((player.VehicleOwned.contains(object_guid) && vehicle.Owner.contains(player.GUID))
            || (player.Faction == vehicle.Faction
            && ((vehicle.Owner.isEmpty || continent.GUID(vehicle.Owner.get).isEmpty) || vehicle.Health == 0))) {
            vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(vehicle), continent))
            vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, continent, Some(0 seconds)))
            log.info(s"RequestDestroy: vehicle $vehicle")
          }
          else {
            log.info(s"RequestDestroy: must own vehicle in order to deconstruct it")
          }

        case Some(obj : BoomerTrigger) =>
          if(FindEquipmentToDelete(object_guid, obj)) {
            continent.GUID(obj.Companion) match {
              case Some(boomer : BoomerDeployable) =>
                boomer.Trigger = None
                localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(boomer, continent, Some(0 seconds)))
                //continent.Deployables ! Zone.Deployable.Dismiss(boomer)
              case Some(thing) =>
                log.info(s"RequestDestroy: BoomerTrigger object connected to wrong object - $thing")
              case None => ;
            }
          }

        case Some(obj : Equipment) =>
          FindEquipmentToDelete(object_guid, obj)

        case Some(_ : LocalProjectile) =>
          FindProjectileEntry(object_guid) match {
            case Some(projectile) =>
              if(projectile.isResolved) {
                log.warn(s"RequestDestroy: tried to clean up projectile ${object_guid.guid} but it was already resolved")
              }
              else {
                projectile.Miss()
              }
            case None =>
              log.warn(s"RequestDestroy: projectile ${object_guid.guid} has never been fired")
          }

        case Some(obj : BoomerDeployable) =>
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds)))
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
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(PlanetSideGUID(0), guid))
              GUIDTask.UnregisterObjectTask(trigger)(continent.GUID)

            case None => ;
          }

        case Some(obj : TelepadDeployable) =>
          localService ! LocalServiceMessage.Telepads(SupportActor.ClearSpecific(List(obj), continent))
          localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(obj), continent))
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds)))

        case Some(obj : PlanetSideGameObject with Deployable) =>
          localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(obj), continent))
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds)))

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
      (continent.GUID(source_guid), continent.GUID(destination_guid), continent.GUID(item_guid)) match {
        case (Some(source : Container), Some(destination : Container), Some(item : Equipment)) =>
          source.Find(item_guid) match {
            case Some(index) =>
              val indexSlot = source.Slot(index)
              val tile = item.Definition.Tile
              val destinationCollisionTest = destination.Collisions(dest, tile.Width, tile.Height)
              val destItemEntry = destinationCollisionTest match {
                case Success(entry :: Nil) =>
                  Some(entry)
                case _ =>
                  None
              }
              if( {
                destinationCollisionTest match {
                  case Success(Nil) | Success(_ :: Nil) =>
                    true //no item or one item to swap
                  case _ =>
                    false //abort when too many items at destination or other failure case
                }
              } && indexSlot.Equipment.contains(item)) {
                if(PermitEquipmentStow(item, destination)) {
                  PerformMoveItem(item, source, index, destination, dest, destItemEntry)
                }
                else {
                  log.error(s"MoveItem: $item disallowed storage in $destination")
                }
              }
              else if(!indexSlot.Equipment.contains(item)) {
                log.error(s"MoveItem: wanted to move $item_guid, but found unexpected ${indexSlot.Equipment.get} at source location")
              }
              else {
                destinationCollisionTest match {
                  case Success(_) =>
                    log.error(s"MoveItem: wanted to move $item_guid, but multiple unexpected items at destination blocked progress")
                  case scala.util.Failure(err) =>
                    log.error(s"MoveItem: wanted to move $item_guid, but $err")
                }
              }
            case _ =>
              log.error(s"MoveItem: wanted to move $item_guid, but could not find it")
          }
        case (None, _, _) =>
          log.error(s"MoveItem: wanted to move $item_guid from $source_guid, but could not find source object")
        case (_, None, _) =>
          log.error(s"MoveItem: wanted to move $item_guid to $destination_guid, but could not find destination object")
        case (_, _, None) =>
          log.error(s"MoveItem: wanted to move $item_guid, but could not find it")
        case _ =>
          log.error(s"MoveItem: wanted to move $item_guid from $source_guid to $destination_guid, but multiple problems were encountered")
      }

    case msg @ LootItemMessage(item_guid, target_guid) =>
      log.info(s"LootItem: $msg")
      (continent.GUID(item_guid), continent.GUID(target_guid)) match {
        case (Some(item : Equipment), Some(target : Container)) =>
          //figure out the source
          (
            {
              val findFunc : PlanetSideGameObject with Container => Option[(PlanetSideGameObject with Container, Option[Int])] = FindInLocalContainer(item_guid)
              findFunc(player.Locker)
                .orElse(findFunc(player))
                .orElse(accessedContainer match {
                  case Some(parent) =>
                    findFunc(parent)
                  case None =>
                    None
                }
              )
            }, target.Fit(item)) match {
            case (Some((source, Some(index))), Some(dest)) =>
              if(PermitEquipmentStow(item, target)) {
                PerformMoveItem(item, source, index, target, dest, None)
              }
              else {
                log.error(s"LootItem: $item disallowed storage in $target")
              }
            case (None, _) =>
              log.error(s"LootItem: can not find where $item is put currently")
            case (_, None) =>
              log.error(s"LootItem: can not find somwhere to put $item in $target")
            case _ =>
              log.error(s"LootItem: wanted to move $item_guid to $target_guid, but multiple problems were encountered")
          }
        case (Some(obj), _) =>
          log.warn(s"LootItem: item $obj is (probably) not lootable")
        case (None, _) =>
          log.warn(s"LootItem: can not find $item_guid")
        case (_, None) =>
          log.warn(s"LootItem: can not find where to put $item_guid")
      }

    case msg @ AvatarImplantMessage(_, _, _, _) => //(player_guid, unk1, unk2, implant) =>
      log.info("AvatarImplantMessage: " + msg)

    case msg @ UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType) =>
      log.info("UseItem: " + msg)
      // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
      // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
      continent.GUID(object_guid) match {
        case Some(door : Door) =>
          if(player.Faction == door.Faction || (continent.Map.DoorToLock.get(object_guid.guid) match {
            case Some(lock_guid) =>
              val lock = continent.GUID(lock_guid).get.asInstanceOf[IFFLock]

              val playerIsOnInside = Vector3.ScalarProjection(lock.Outwards, player.Position - door.Position) < 0f

              // If an IFF lock exists and the IFF lock faction doesn't match the current player and one of the following conditions are met open the door:
              // A base is neutral
              // A base is hacked
              // The lock is hacked
              // The player is on the inside of the door, determined by the lock orientation
              lock.HackedBy.isDefined || lock.Owner.asInstanceOf[Building].CaptureConsoleIsHacked || lock.Faction == PlanetSideEmpire.NEUTRAL || playerIsOnInside
            case None => !door.isOpen // If there's no linked IFF lock just open the door if it's closed.
          })) {
            door.Actor ! Door.Use(player, msg)
          }
          else if(door.isOpen) {
            //the door is open globally ... except on our screen
            sendResponse(GenericObjectStateMsg(object_guid, 16))
          }

        case Some(resourceSilo : ResourceSilo) =>
          log.info(s"UseItem: Vehicle $avatar_guid is refilling resource silo $object_guid")
          val vehicle = continent.GUID(avatar_guid).get.asInstanceOf[Vehicle]

          if(resourceSilo.Faction == PlanetSideEmpire.NEUTRAL || player.Faction == resourceSilo.Faction) {
            if(vehicle.Seat(0).get.Occupant.contains(player)) {
              log.trace("UseItem: Player matches vehicle driver. Calling ResourceSilo.Use")
              resourceSilo.Actor ! ResourceSilo.Use(player, msg)
            }
          } else {
            log.warn(s"Player ${player.GUID} - ${player.Faction} tried to refill silo ${resourceSilo.GUID} - ${resourceSilo.Faction} belonging to another empire")
          }

        case Some(panel : IFFLock) =>
          if((panel.Faction != player.Faction && panel.HackedBy.isEmpty) || (panel.Faction == player.Faction && panel.HackedBy.isDefined)) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool : SimpleItem) =>
                if(tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  val hackSpeed = GetPlayerHackSpeed(panel)

                  if(hackSpeed > 0) {
                    progressBarValue = Some(-hackSpeed)
                    if(panel.Faction != player.Faction) {
                      // Enemy faction is hacking this IFF lock
                      self ! WorldSessionActor.HackingProgress(progressType = 1, player, panel, tool.GUID, hackSpeed, FinishHacking(panel, 1114636288L))
                      log.info("Hacking an IFF lock")
                    } else {
                      // IFF Lock is being resecured by it's owner faction
                      self ! WorldSessionActor.HackingProgress(progressType = 1, player, panel, tool.GUID, hackSpeed, FinishResecuringIFFLock(panel))
                      log.info("Resecuring an IFF lock")
                    }
                  }
                }
              case _ => ;
            }
          } else {
            log.warn("IFF lock is being hacked, but don't know how to handle this state")
            log.warn(s"Lock - HackedBy.isDefined: ${panel.HackedBy.isDefined} Faction: ${panel.Faction} HackedBy.isEmpty: ${panel.HackedBy.isEmpty}")
            log.warn(s"Hacking player - Faction: ${player.Faction}")
          }

        case Some(obj : Player) =>
          if(obj.isBackpack) {
            log.info(s"UseItem: $player looting the corpse of $obj")
            sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
            accessedContainer = Some(obj)
          }
          else if(!unk3) { //potential kit use
            continent.GUID(item_used_guid) match {
              case Some(kit : Kit) =>
                player.Find(kit) match {
                  case Some(index) =>
                    if(kit.Definition == GlobalDefinitions.medkit) {
                      if(player.Health == player.MaxHealth) {
                        sendResponse(ChatMsg(ChatMessageType.UNK_225, false, "", "@HealComplete", None))
                      }
                      else if(System.currentTimeMillis - whenUsedLastKit < 5000) {
                        sendResponse(ChatMsg(ChatMessageType.UNK_225, false, "", s"@TimeUntilNextUse^${5 - (System.currentTimeMillis - whenUsedLastKit) / 1000}~", None))
                      }
                      else {
                        player.Find(kit) match {
                          case Some(index) =>
                            whenUsedLastKit = System.currentTimeMillis
                            player.Slot(index).Equipment = None //remove from slot immediately; must exist on client for next packet
                            sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, 0, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
                            sendResponse(ObjectDeleteMessage(kit.GUID, 0))
                            taskResolver ! GUIDTask.UnregisterEquipment(kit)(continent.GUID)
                            player.History(HealFromKit(PlayerSource(player), 25, kit.Definition))
                            player.Health = player.Health + 25
                            sendResponse(PlanetsideAttributeMessage(avatar_guid, 0, player.Health))
                            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(avatar_guid, 0, player.Health))
                          case None =>
                            log.error(s"UseItem: anticipated a $kit, but can't find it")
                        }
                      }
                    }
                    else {
                      log.warn(s"UseItem: $kit behavior not supported")
                    }

                  case None =>
                    log.error(s"UseItem: anticipated a $kit, but can't find it")
                }
              case Some(item) =>
                log.warn(s"UseItem: looking for Kit to use, but found $item instead")
              case None =>
                log.warn(s"UseItem: anticipated a Kit $item_used_guid, but can't find it")
            }
          }

        case Some(locker : Locker) =>
          if(locker.Faction != player.Faction && locker.HackedBy.isEmpty) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool: SimpleItem) =>
                if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  val hackSpeed = GetPlayerHackSpeed(locker)

                  if(hackSpeed > 0)  {
                    progressBarValue = Some(-hackSpeed)
                    self ! WorldSessionActor.HackingProgress(progressType = 1, player, locker, tool.GUID, hackSpeed, FinishHacking(locker, 3212836864L))
                    log.info("Hacking a locker")
                  }
                }
              case _ => ;
            }
          } else if(player.Faction == locker.Faction || !locker.HackedBy.isEmpty) {
            log.info(s"UseItem: $player accessing a locker")
            val container = player.Locker
            accessedContainer = Some(container)
            sendResponse(UseItemMessage(avatar_guid, item_used_guid, container.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, 456))
          }
          else {
            log.info(s"UseItem: not $player's locker")
          }

        case Some(implant_terminal : ImplantTerminalMech) =>
          if(implant_terminal.Faction != player.Faction && implant_terminal.HackedBy.isEmpty) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool: SimpleItem) =>
                if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  val hackSpeed = GetPlayerHackSpeed(implant_terminal)

                  if(hackSpeed > 0)  {
                    progressBarValue = Some(-hackSpeed)
                    self ! WorldSessionActor.HackingProgress(progressType = 1, player, implant_terminal, tool.GUID, hackSpeed, FinishHacking(implant_terminal, 3212836864L))
                    log.info("Hacking an implant terminal")
                  }
                }
              case _ => ;
            }
          }

        case Some(captureTerminal : CaptureTerminal) =>
          val hackedByCurrentFaction = (captureTerminal.Faction != player.Faction && !captureTerminal.HackedBy.isEmpty && captureTerminal.HackedBy.get.hackerFaction == player.Faction)
          val ownedByPlayerFactionAndHackedByEnemyFaction = (captureTerminal.Faction == player.Faction && !captureTerminal.HackedBy.isEmpty)
          if(!hackedByCurrentFaction || ownedByPlayerFactionAndHackedByEnemyFaction) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool: SimpleItem) =>
                if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  val hackSpeed = GetPlayerHackSpeed(captureTerminal)

                  if(hackSpeed > 0) {
                    progressBarValue = Some(-hackSpeed)
                    self ! WorldSessionActor.HackingProgress(progressType = 1, player, captureTerminal, tool.GUID, hackSpeed, FinishHacking(captureTerminal, 3212836864L))
                    log.info("Hacking a capture terminal")
                  }
                }
              case _ => ;
            }
          }

        case Some(obj : FacilityTurret) =>
          player.Slot(player.DrawnSlot).Equipment match {
            case Some(tool : Tool) =>
              if(tool.Definition == GlobalDefinitions.nano_dispenser && tool.Magazine > 0) {
                val ammo = tool.AmmoType
                if(ammo == Ammo.upgrade_canister && obj.Seats.values.count(_.isOccupied) == 0) {
                  progressBarValue = Some(-1.25f)
                  self ! WorldSessionActor.HackingProgress(
                    progressType = 2,
                    player,
                    obj,
                    tool.GUID,
                    delta = 1.25f,
                    FinishUpgradingMannedTurret(obj, tool, TurretUpgrade(unk2.toInt))
                  )
                }
                else if(ammo == Ammo.armor_canister && obj.Health < obj.MaxHealth) {
                  //repair turret
                }
              }
              else if(tool.Definition == GlobalDefinitions.trek) {
                //infect turret with virus
              }
            case _ => ;
          }

        case Some(obj : Vehicle) =>
          val equipment = player.Slot(player.DrawnSlot).Equipment
          if(player.Faction == obj.Faction) {
            if(equipment match {
              case Some(tool : Tool) =>
                tool.Definition match {
                  case GlobalDefinitions.nano_dispenser => false
                  case _ => true
                }
              case _ => true
            }) {
              //access to trunk
              if(obj.AccessingTrunk.isEmpty &&
                (!obj.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked) || obj.Owner.contains(player.GUID))) {
                obj.AccessingTrunk = player.GUID
                accessedContainer = Some(obj)
                AccessContents(obj)
                sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
              }
              else {
                log.info(s"UseItem: $obj's trunk is not currently accessible for $player")
              }
            }
            else if(equipment.isDefined) {
              equipment.get.Definition match {
                case GlobalDefinitions.nano_dispenser =>
                  //TODO repairing behavior

                case _ => ;
              }
            }
          }
          //enemy player interactions
          else if(equipment.isDefined) {
            equipment.get.Definition match {
              case GlobalDefinitions.remote_electronics_kit =>
                val hackSpeed = GetPlayerHackSpeed(obj)

                if(hackSpeed > 0) {
                  progressBarValue = Some(-hackSpeed)
                  self ! WorldSessionActor.HackingProgress(progressType = 1, player, obj, equipment.get.GUID, hackSpeed, FinishHackingVehicle(obj, 3212836864L))
                  log.info("Hacking a vehicle")
                }
              case _ => ;
            }
          }

        case Some(terminal : Terminal) =>
          val tdef = terminal.Definition

          // If the base this terminal belongs to has been hacked the owning faction needs to be able to hack it to gain access
          val ownerIsHacked = terminal.Owner match {
            case b: Building => b.CaptureConsoleIsHacked
            case _ => false
          }
          var playerIsHacking = false

          player.Slot(player.DrawnSlot).Equipment match {
            case Some(tool: SimpleItem) =>
              if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                if (!terminal.HackedBy.isEmpty) {
                  log.warn("Player tried to hack a terminal that is already hacked")
                  log.warn(s"Player faction ${player.Faction} terminal faction: ${terminal.Faction} terminal hacked: ${terminal.HackedBy.isDefined} owner hacked: ${ownerIsHacked}")
                }
                else if (terminal.Faction != player.Faction || ownerIsHacked) {
                  val hackSpeed = GetPlayerHackSpeed(terminal)

                  if (hackSpeed > 0) {
                    progressBarValue = Some(-hackSpeed)
                    self ! WorldSessionActor.HackingProgress(progressType = 1, player, terminal, tool.GUID, hackSpeed, FinishHacking(terminal, 3212836864L))
                    playerIsHacking = true
                    log.info("Hacking a terminal")
                  }
                }
              }
            case _ => ;
          }

          if(!playerIsHacking) {
            if (terminal.Faction == player.Faction) {
              if (tdef.isInstanceOf[MatrixTerminalDefinition]) {
                //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
                sendResponse(BindPlayerMessage(BindStatus.Bind, "", true, true, SpawnGroup.Sanctuary, 0, 0, terminal.Position))
              }
              else if (tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
                tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal) {
                FindLocalVehicle match {
                  case Some(vehicle) =>
                    sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
                    sendResponse(UseItemMessage(avatar_guid, item_used_guid, vehicle.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, vehicle.Definition.ObjectId))
                  case None =>
                    log.error("UseItem: expected seated vehicle, but found none")
                }
              }
              else if (tdef == GlobalDefinitions.teleportpad_terminal) {
                //explicit request
                terminal.Actor ! Terminal.Request(
                  player,
                  ItemTransactionMessage(object_guid, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
                )
              }
              else if (!ownerIsHacked || (ownerIsHacked && terminal.HackedBy.isDefined)) {
                sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
              }
              else {
                log.warn("Tried to use a terminal, but can't handle this case")
                log.warn(s"Terminal - isHacked ${terminal.HackedBy.isDefined} ownerIsHacked ${ownerIsHacked}")
              }
            }
            else if (terminal.HackedBy.isDefined) {
              sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
            } else {
              log.warn("Tried to use a terminal that doesn't belong to this faction and isn't hacked")
              log.warn(s"Player faction ${player.Faction} terminal faction: ${terminal.Faction} terminal hacked: ${terminal.HackedBy.isDefined} owner hacked: ${ownerIsHacked}")
            }
          }
        case Some(obj : SpawnTube) =>
          //deconstruction
          PlayerActionsToCancel()
          CancelAllProximityUnits()
          continent.Population ! Zone.Population.Release(avatar)
          GoToDeploymentMap()

        case Some(obj : TelepadDeployable) =>
          continent.GUID(obj.Router) match {
            case Some(vehicle : Vehicle) =>
              vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(util : Utility.InternalTelepad) =>
                  UseRouterTelepadSystem(router = vehicle, internalTelepad = util, remoteTelepad = obj, src = obj, dest = util)
                case _ =>
                  log.error(s"telepad@${object_guid.guid} is not linked to a router - ${vehicle.Definition.Name}@${obj.Router.get.guid}")
              }
            case Some(o) =>
              log.error(s"telepad@${object_guid.guid} is linked to wrong kind of object - ${o.Definition.Name}@${obj.Router.get.guid}")
            case None => ;
          }

        case Some(obj : Utility.InternalTelepad) =>
          continent.GUID(obj.Telepad) match {
            case Some(pad : TelepadDeployable) =>
              UseRouterTelepadSystem(router = obj.Owner.asInstanceOf[Vehicle], internalTelepad = obj, remoteTelepad = pad, src = obj, dest = pad)
            case Some(o) =>
              log.error(s"internal telepad@${object_guid.guid} is not linked to a remote telepad - ${o.Definition.Name}@${o.GUID.guid}")
            case None => ;
          }

        case Some(obj) =>
          log.warn(s"UseItem: don't know how to handle $obj; taking a shot in the dark")
          sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))

        case None =>
          log.error(s"UseItem: can not find object $object_guid")
      }

    case msg @ ProximityTerminalUseMessage(player_guid, object_guid, _) =>
      log.info(s"ProximityTerminalUse: $msg")
      continent.GUID(object_guid) match {
        case Some(obj : Terminal with ProximityUnit) =>
          HandleProximityTerminalUse(obj)
        case Some(obj) => ;
          log.warn(s"ProximityTerminalUse: object does not have proximity effects - $obj")
        case None =>
          log.warn(s"ProximityTerminalUse: no object with guid $object_guid found")
      }

    case msg @ UnuseItemMessage(player_guid, object_guid) =>
      log.info(s"UnuseItem: $msg")
      //TODO check for existing accessedContainer value?
      continent.GUID(object_guid) match {
        case Some(obj : Vehicle) =>
          if(obj.AccessingTrunk.contains(player.GUID)) {
            obj.AccessingTrunk = None
            UnAccessContents(obj)
          }
        case Some(obj : Player) =>
          TryDisposeOfLootedCorpse(obj)

        case _ =>;
      }
      accessedContainer = None

    case msg @ DeployObjectMessage(guid, unk1, pos, orient, unk2) =>
      log.info(s"DeployObject: $msg")
      //the hand with the construction item is no longer drawn
      //TODO consider player.Slot(player.LastDrawnSlot)
      (player.Holsters.find(slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid) match {
        case Some(slot) =>
          slot.Equipment
        case None =>
          None
      }) match {
        case Some(obj : ConstructionItem) =>
          val ammoType = obj.AmmoType match {
            case DeployedItem.portable_manned_turret =>
              GlobalDefinitions.PortableMannedTurret(player.Faction).Item //faction-specific turret
            case turret =>
              turret
          }
          log.info(s"DeployObject: Constructing a ${ammoType}")
          val dObj : PlanetSideGameObject with Deployable = Deployables.Make(ammoType)()
          dObj.Position = pos
          dObj.Orientation = orient
          dObj.Faction = player.Faction
          dObj.AssignOwnership(player)
          val tasking : TaskResolver.GiveTask = dObj match {
            case turret : TurretDeployable =>
              GUIDTask.RegisterDeployableTurret(turret)(continent.GUID)
            case _ =>
              GUIDTask.RegisterObjectTask(dObj)(continent.GUID)
          }
          taskResolver ! CallBackForTask(tasking, continent.Deployables, Zone.Deployable.Build(dObj, obj))

        case Some(obj) =>
          log.warn(s"DeployObject: $obj is something?")
        case None =>
          log.warn("DeployObject: nothing?")

      }

    case msg @ GenericObjectStateMsg(object_guid, unk1) =>
      log.info("GenericObjectState: " + msg)

    case msg @ GenericActionMessage(action) =>
      log.info(s"GenericAction: $msg")
      val (toolOpt, definition) = player.Slot(0).Equipment match {
        case Some(tool : Tool) =>
          (Some(tool), tool.Definition)
        case _ =>
          (None, GlobalDefinitions.bullet_9mm)
      }
      if(action == 15) { //max deployment
        log.info(s"GenericObject: $player is anchored")
        player.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 19, 1))
        definition match {
          case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.trhev_burster =>
            val tool = toolOpt.get
            tool.ToFireMode = 1
            sendResponse(ChangeFireModeMessage(tool.GUID, 1))
          case GlobalDefinitions.trhev_pounder =>
            val tool = toolOpt.get
            val convertFireModeIndex = if(tool.FireModeIndex == 0) { 1 } else { 4 }
            tool.ToFireMode = convertFireModeIndex
            sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
          case _ =>
            log.info(s"GenericObject: $player is MAX with an unexpected weapon - ${definition.Name}")
        }
      }
      else if(action == 16) { //max deployment
        log.info(s"GenericObject: $player has released the anchors")
        player.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 19, 0))
        definition match {
          case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.trhev_burster =>
            val tool = toolOpt.get
            tool.ToFireMode = 0
            sendResponse(ChangeFireModeMessage(tool.GUID, 0))
          case GlobalDefinitions.trhev_pounder =>
            val tool = toolOpt.get
            val convertFireModeIndex = if(tool.FireModeIndex == 1) { 0 } else { 3 }
            tool.ToFireMode = convertFireModeIndex
            sendResponse(ChangeFireModeMessage(tool.GUID, convertFireModeIndex))
          case _ =>
            log.info(s"GenericObject: $player is MAX with an unexpected weapon - ${definition.Name}")
        }
      }
      else if(action == 36) { //Looking For Squad ON
        if(squadUI.nonEmpty) {
          if(!lfs && squadUI(player.CharId).index == 0) {
            lfs = true
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 53, 1))
          }
        }
        else if(!avatar.LFS) {
          avatar.LFS = true
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 53, 1))
        }
      }
      else if(action == 37) { //Looking For Squad OFF
        if(squadUI.nonEmpty) {
          if(lfs && squadUI(player.CharId).index == 0) {
            lfs = false
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 53, 0))
          }
        }
        else if(avatar.LFS) {
          avatar.LFS = false
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player.GUID, 53, 0))
        }
      }

    case msg @ ItemTransactionMessage(terminal_guid, transaction_type, _, _, _, _) =>
      log.info("ItemTransaction: " + msg)
      continent.GUID(terminal_guid) match {
        case Some(term : Terminal) =>
          log.info(s"ItemTransaction: ${term.Definition.Name} found")
          if(lastTerminalOrderFulfillment) {
            lastTerminalOrderFulfillment = false
            term.Actor ! Terminal.Request(player, msg)
          }
        case Some(obj : PlanetSideGameObject) =>
          log.error(s"ItemTransaction: $obj is not a terminal")
        case _ =>
          log.error(s"ItemTransaction: $terminal_guid does not exist")
      }

    case msg @ FavoritesRequest(player_guid, list, action, line, label) =>
      log.info(s"FavoritesRequest: $msg")
      if(player.GUID == player_guid) {
        val lineno = if(list == LoadoutType.Vehicle) { line + 10 } else { line }
        val name = label.getOrElse(s"missing_loadout_${line+1}")
        action match {
          case FavoritesAction.Save =>
            (if(list == LoadoutType.Infantry) {
              Some(player)
            }
            else if(list == LoadoutType.Vehicle) {
              player.VehicleSeated match {
                case Some(vehicle_guid) =>
                  continent.GUID(vehicle_guid)
                case None =>
                  None
              }
            }
            else {
              None
            }) match {
              case Some(owner : Player) => //InfantryLoadout
                avatar.EquipmentLoadouts.SaveLoadout(owner, name, lineno)
                import InfantryLoadout._
                sendResponse(FavoritesMessage(list, player_guid, line, name, DetermineSubtypeB(player.ExoSuit, DetermineSubtype(player))))
              case Some(owner : Vehicle) => //VehicleLoadout
                avatar.EquipmentLoadouts.SaveLoadout(owner, name, lineno)
                sendResponse(FavoritesMessage(list, player_guid, line, name))
              case Some(_) | None =>
                log.error("FavoritesRequest: unexpected owner for favorites")
            }

          case FavoritesAction.Delete =>
            avatar.EquipmentLoadouts.DeleteLoadout(lineno)
            sendResponse(FavoritesMessage(list, player_guid, line, ""))

          case FavoritesAction.Unknown =>
            log.warn("FavoritesRequest: unknown favorites action")
        }
      }

    case msg @ WeaponDelayFireMessage(seq_time, weapon_guid) =>
      log.info("WeaponDelayFire: " + msg)

    case msg @ WeaponDryFireMessage(weapon_guid) =>
      log.info("WeaponDryFireMessage: "+msg)
      FindWeapon match {
        case Some(tool : Tool) =>
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.WeaponDryFire(player.GUID, weapon_guid))
        case _ => ;
      }

    case msg @ WeaponFireMessage(seq_time, weapon_guid, projectile_guid, shot_origin, unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
      log.info("WeaponFire: " + msg)
      FindContainedWeapon match {
        case (Some(obj), Some(tool : Tool)) =>
          if(tool.Magazine <= 0) { //safety: enforce ammunition depletion
            prefire = None
            EmptyMagazine(weapon_guid, tool)
          }
          else { //shooting
            prefire = shooting.orElse(Some(weapon_guid))
            tool.Discharge
            val projectileIndex = projectile_guid.guid - Projectile.BaseUID
            val projectilePlace = projectiles(projectileIndex)
            if(projectilePlace match {
              case Some(projectile) => !projectile.isResolved
              case None => false
            }) {
              log.trace(s"WeaponFireMessage: overwriting unresolved projectile ${projectile_guid.guid}")
            }
            val (angle, attribution, acceptableDistanceToOwner) = obj match {
              case p : Player =>
                (p.Orientation, tool.Definition.ObjectId, 10f) //TODO upper body facing
              case v : Vehicle if v.Definition.CanFly =>
                (tool.Orientation, obj.Definition.ObjectId, 1000f) //TODO this is too simplistic to find proper angle
              case _ : Vehicle =>
                (tool.Orientation, obj.Definition.ObjectId, 225f) //TODO this is too simplistic to find proper angle
              case _ =>
                (obj.Orientation, obj.Definition.ObjectId, 300f)
            }
            val distanceToOwner = Vector3.DistanceSquared(shot_origin, player.Position)
            if(distanceToOwner <= acceptableDistanceToOwner) {
              projectiles(projectileIndex) =
                Some(Projectile(tool.Projectile, tool.Definition, tool.FireMode, player, attribution, shot_origin, angle))
            }
            else {
              log.warn(s"WeaponFireMessage: $player's ${tool.Definition.Name} projectile is too far from owner position at time of discharge ($distanceToOwner > $acceptableDistanceToOwner); suspect")
            }
          }
        case _ => ;
      }

    case msg @ WeaponLazeTargetPositionMessage(weapon, pos1, pos2) =>
      log.info("Lazing position: " + pos2.toString)

    case msg @ HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
      log.info(s"Hit: $msg")
      (hit_info match {
        case Some(hitInfo) =>
          continent.GUID(hitInfo.hitobject_guid) match {
            case Some(target : PlanetSideGameObject with FactionAffinity with Vitality) =>
              Some((target, hitInfo.shot_origin, hitInfo.hit_pos))
            case _ =>
              None
          }
        case None => ;
          None
      }) match {
        case Some((target, shotOrigin, hitPos)) =>
          ResolveProjectileEntry(projectile_guid, ProjectileResolution.Hit, target, hitPos) match {
            case Some(projectile) =>
              HandleDealingDamage(target, projectile)
            case None => ;
          }
        case None => ;
      }

    case msg @ SplashHitMessage(seq_time, projectile_guid, explosion_pos, direct_victim_uid, unk3, projectile_vel, unk4, targets) =>
      log.info(s"Splash: $msg")
      continent.GUID(direct_victim_uid) match {
        case Some(target : PlanetSideGameObject with FactionAffinity with Vitality) =>
          ResolveProjectileEntry(projectile_guid, ProjectileResolution.Splash, target, target.Position) match {
            case Some(projectile) =>
              HandleDealingDamage(target, projectile)
            case None => ;
          }
        case _ => ;
      }
      targets.foreach(elem => {
        continent.GUID(elem.uid) match {
          case Some(target : PlanetSideGameObject with FactionAffinity with Vitality) =>
            ResolveProjectileEntry(projectile_guid, ProjectileResolution.Splash, target, explosion_pos) match {
              case Some(projectile) =>
                HandleDealingDamage(target, projectile)
              case None => ;
            }
          case _ => ;
        }
      })

    case msg @ LashMessage(seq_time, killer_guid, victim_guid, projectile_guid, pos, unk1) =>
      log.info(s"Lash: $msg")
      continent.GUID(victim_guid) match {
        case Some(target : PlanetSideGameObject with FactionAffinity with Vitality) =>
          ResolveProjectileEntry(projectile_guid, ProjectileResolution.Lash, target, pos) match {
            case Some(projectile) =>
              HandleDealingDamage(target, projectile)
            case None => ;
          }
        case _ => ;
      }

    case msg @ AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, event_name) =>
      log.info("AvatarFirstTimeEvent: " + msg)

    case msg @ WarpgateRequest(continent_guid, building_guid, dest_building_guid, dest_continent_guid, unk1, unk2) =>
      log.info(s"WarpgateRequest: $msg")
      if(deadState != DeadState.RespawnTime) {
        deadState = DeadState.RespawnTime
        continent.Buildings.values.find(building => building.GUID == building_guid) match {
          case Some(wg : WarpGate) if (wg.Active && (GetKnownVehicleAndSeat() match {
            case (Some(vehicle), _) =>
              wg.Definition.VehicleAllowance && !wg.Definition.NoWarp.contains(vehicle.Definition)
            case _ =>
              true
          })) =>
            cluster ! Zone.Lattice.RequestSpecificSpawnPoint(dest_continent_guid.guid, player, dest_building_guid)

          case _ =>
            RequestSanctuaryZoneSpawn(player, continent.Number)
        }
      }
      else {
        log.warn("WarpgateRequest: request consumed; already respawning ...")
      }

    case msg @ MountVehicleMsg(player_guid, mountable_guid, entry_point) =>
      log.info("MountVehicleMsg: "+msg)
      continent.GUID(mountable_guid) match {
        case Some(obj : Mountable) =>
          obj.GetSeatFromMountPoint(entry_point) match {
            case Some(seat_num) =>
              obj.Actor ! Mountable.TryMount(player, seat_num)
            case None =>
              log.warn(s"MountVehicleMsg: attempted to board mountable $mountable_guid's seat $entry_point, but no seat exists there")
          }
        case None | Some(_) =>
          log.warn(s"MountVehicleMsg: not a mountable thing")
      }

    case msg @ DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) =>
      //TODO optimize this later
      log.info(s"DismountVehicleMsg: $msg")
      //common warning for this section
      def dismountWarning(msg : String) : Unit = {
        log.warn(s"$msg; some vehicle might not know that a player is no longer sitting in it")
      }
      if(player.GUID == player_guid) {
        //normally disembarking from a seat
        player.VehicleSeated match {
          case Some(obj_guid) =>
            interstellarFerry.orElse(continent.GUID(obj_guid)) match {
              case Some(obj : Mountable) =>
                obj.PassengerInSeat(player) match {
                  case Some(0) if controlled.nonEmpty =>
                    log.warn(s"DismountVehicleMsg: can not dismount from vehicle as driver while server has asserted control; please wait ...")
                  case Some(seat_num : Int) =>
                    obj.Actor ! Mountable.TryDismount(player, seat_num)
                    if(interstellarFerry.isDefined) {
                      //short-circuit the temporary channel for transferring between zones, the player is no longer doing that
                      //see above in VehicleResponse.TransferPassenger case
                      interstellarFerry = None
                    }
                    // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
                    //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
                    //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
                    //todo: kick cargo passengers out. To be added after PR #216 is merged
                    obj match {
                      case v : Vehicle if bailType == BailType.Bailed && seat_num == 0 && v.Flying =>
                        vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(obj), continent))
                        vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(obj, continent, Some(0 seconds))) // Immediately deconstruct vehicle
                      case _ => ;
                    }

                  case None =>
                    dismountWarning(s"DismountVehicleMsg: can not find where player $player_guid is seated in mountable $obj_guid")
                }
              case _ =>
                dismountWarning(s"DismountVehicleMsg: can not find mountable entity $obj_guid")
            }
          case None =>
            dismountWarning(s"DismountVehicleMsg: player $player_guid not considered seated in a mountable entity")
        }
      }
      else {
        //kicking someone else out of a seat; need to own that seat/mountable
        player.VehicleOwned match {
          case Some(obj_guid) =>
            (continent.GUID(obj_guid), continent.GUID(player_guid)) match {
              case (Some(obj : Mountable), Some(tplayer : Player)) =>
                obj.PassengerInSeat(tplayer) match {
                  case Some(seat_num : Int) =>
                    obj.Actor ! Mountable.TryDismount(tplayer, seat_num)
                  case None =>
                    dismountWarning(s"DismountVehicleMsg: can not find where other player $player_guid is seated in mountable $obj_guid")
                }
              case (None, _) => ;
                log.warn(s"DismountVehicleMsg: $player can not find his vehicle")
              case (_, None) => ;
                log.warn(s"DismountVehicleMsg: player $player_guid could not be found to kick")
              case _ =>
                log.warn(s"DismountVehicleMsg: object is either not a Mountable or not a Player")
            }
          case None =>
            log.warn(s"DismountVehicleMsg: $player does not own a vehicle")
        }
      }

    case msg @ DeployRequestMessage(player_guid, vehicle_guid, deploy_state, unk2, unk3, pos) =>
      log.info(s"DeployRequest: $msg")
      if(player.VehicleOwned.contains(vehicle_guid) && player.VehicleOwned == player.VehicleSeated) {
        continent.GUID(vehicle_guid) match {
          case Some(obj : Vehicle) =>
            obj.Actor ! Deployment.TryDeploymentChange(deploy_state)

          case _ =>
            log.error(s"DeployRequest: can not find $vehicle_guid in scope")
            player.VehicleOwned = None
        }
      }
      else {
        log.warn(s"DeployRequest: $player does not own the deploying $vehicle_guid object")
      }

    case msg @ AvatarGrenadeStateMessage(player_guid, state) =>
      log.info("AvatarGrenadeStateMessage: " + msg)

    case msg @ SquadDefinitionActionMessage(u1, u2, action) =>
      log.info(s"SquadDefinitionAction: $msg")
        squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Definition(u1, u2, action))

    case msg @ SquadMembershipRequest(request_type, unk2, unk3, player_name, unk5) =>
      log.info(s"$msg")
      squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Membership(request_type, unk2, unk3, player_name, unk5))

    case msg @ SquadWaypointRequest(request, _, wtype, unk, info) =>
      log.info(s"Waypoint Request: $msg")
      squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Waypoint(request, wtype, unk, info))

    case msg @ GenericCollisionMsg(u1, p, t, php, thp, pv, tv, ppos, tpos, u2, u3, u4) =>
      log.info("Ouch! " + msg)

    case msg @ BugReportMessage(version_major,version_minor,version_date,bug_type,repeatable,location,zone,pos,summary,desc) =>
      log.info("BugReportMessage: " + msg)

    case msg @ BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
      log.info("BindPlayerMessage: " + msg)

    case msg @ PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value) =>
      log.info("PlanetsideAttributeMessage: "+msg)
      continent.GUID(object_guid) match {
        case Some(vehicle : Vehicle) =>
          if(player.VehicleOwned.contains(vehicle.GUID)) {
            if(9 < attribute_type && attribute_type < 14) {
              vehicle.PermissionGroup(attribute_type, attribute_value) match {
                case Some(allow) =>
                  val group = AccessPermissionGroup(attribute_type - 10)
                  log.info(s"Vehicle attributes: vehicle ${vehicle.GUID} access permission $group changed to $allow")
                  vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SeatPermissions(player.GUID, vehicle.GUID, attribute_type, attribute_value))
                  //kick players who should not be seated in the vehicle due to permission changes
                  if(allow == VehicleLockState.Locked) { //TODO only important permission atm
                    vehicle.Definition.MountPoints.values.foreach(mountpoint_num => {
                      vehicle.Seat(mountpoint_num) match {
                        case Some(seat) =>
                          seat.Occupant match {
                            case Some(tplayer) =>
                              if(vehicle.SeatPermissionGroup(mountpoint_num).contains(group) && tplayer != player) { //can not kick self
                                seat.Occupant = None
                                tplayer.VehicleSeated = None
                                vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(tplayer.GUID, 4, false, object_guid))
                              }
                            case None => ; // No player seated
                          }
                        case None => ; // Not a seat mounting point
                      }
                      vehicle.CargoHold(mountpoint_num) match {
                        case Some(cargo) =>
                          cargo.Occupant match {
                            case Some(vehicle) =>
                              if(vehicle.SeatPermissionGroup(mountpoint_num).contains(group)) {
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
            }
            else {
              log.warn(s"Vehicle attributes: unsupported change on vehicle $object_guid - $attribute_type")
            }
          }
          else {
            log.warn(s"Vehicle attributes: $player does not own vehicle ${vehicle.GUID} and can not change it")
          }
        case _ =>
          log.warn(s"echo unknown attributes behavior")
          sendResponse(PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value))
      }

    case msg @ FacilityBenefitShieldChargeRequestMessage(guid) =>
      player.VehicleSeated match {
        case Some(vehicleGUID) =>
          continent.GUID(vehicleGUID) match {
            case Some(obj : Vehicle) =>
              if(obj.Health > 0) { //vehicle will try to charge even if destroyed
                obj.Actor ! Vehicle.ChargeShields(15)
              }
            case _ =>
              log.warn(s"FacilityBenefitShieldChargeRequest: can not find vehicle ${vehicleGUID.guid} in zone ${continent.Id}")
          }
        case None =>
          log.warn(s"FacilityBenefitShieldChargeRequest: player ${player.Name} is not seated in a vehicle")
      }

    case msg @ BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
      log.info("Battleplan: "+msg)

    case msg @ CreateShortcutMessage(player_guid, slot, unk, add, shortcut) =>
      log.info("CreateShortcutMessage: "+msg)

    case msg @ FriendsRequest(action, friend) =>
      log.info("FriendsRequest: "+msg)

    case msg @ HitHint(source_guid, player_guid) =>
      log.trace(s"HitHint: $msg") //HitHint is manually distributed for proper operation

    case msg @ TargetingImplantRequest(list) =>
      log.info("TargetingImplantRequest: "+msg)

    case msg @ ActionCancelMessage(u1, u2, u3) =>
      log.info("Cancelled: "+msg)

    case default => log.error(s"Unhandled GamePacket $pkt")
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Remove any encountered items and add them to an output `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param index a number that equals the "current" holster slot (`EquipmentSlot`)
    * @param list a persistent `List` of `Equipment` in the holster slots
    * @return a `List` of `Equipment` in the holster slots
    */
  @tailrec private def clearHolsters(iter : Iterator[EquipmentSlot], index : Int = 0, list : List[InventoryItem] = Nil) : List[InventoryItem] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val slot = iter.next
      slot.Equipment match {
        case Some(equipment) =>
          slot.Equipment = None
          clearHolsters(iter, index + 1, InventoryItem(equipment, index) +: list)
        case None =>
          clearHolsters(iter, index + 1, list)
      }
    }
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * For any slots that are not yet occupied by an item, search through the `List` and find an item that fits in that slot.
    * Add that item to the slot and remove it from the list.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param list a `List` of all `Equipment` that is not yet assigned to a holster slot or an inventory slot
    * @return the `List` of all `Equipment` not yet assigned to a holster slot or an inventory slot
    */
  @tailrec private def fillEmptyHolsters(iter : Iterator[EquipmentSlot], list : List[InventoryItem]) : List[InventoryItem] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val slot = iter.next
      if(slot.Equipment.isEmpty) {
        list.find(item => item.obj.Size == slot.Size) match {
          case Some(obj) =>
            val index = list.indexOf(obj)
            slot.Equipment = obj.obj
            fillEmptyHolsters(iter, list.take(index) ++ list.drop(index + 1))
          case None =>
            fillEmptyHolsters(iter, list)
        }
      }
      else {
        fillEmptyHolsters(iter, list)
      }
    }
  }

  /**
    * Construct tasking that coordinates the following:<br>
    * 1) Accept a new piece of `Equipment` and register it with a globally unique identifier.<br>
    * 2) Once it is registered, give the `Equipment` to `target`.
    * @param target what object will accept the new `Equipment`
    * @param obj the new `Equipment`
    * @param index the slot where the new `Equipment` will be placed
    * @see `GUIDTask.RegisterEquipment`
    * @see `PutInSlot`
    * @return a `TaskResolver.GiveTask` message
    */
  private def PutEquipmentInSlot(target : PlanetSideGameObject with Container, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
    val regTask = GUIDTask.RegisterEquipment(obj)(continent.GUID)
    obj match {
      case tool : Tool =>
        val linearToolTask = TaskResolver.GiveTask(regTask.task) +: regTask.subs
        TaskResolver.GiveTask(PutInSlot(target, tool, index).task, linearToolTask)
      case _ =>
        TaskResolver.GiveTask(PutInSlot(target, obj, index).task, List(regTask))
    }
  }

  /**
    * Construct tasking that coordinates the following:<br>
    * 1) Remove a new piece of `Equipment` from where it is currently stored.<br>
    * 2) Once it is removed, un-register the `Equipment`'s globally unique identifier.
    * @param target the object that currently possesses the `Equipment`
    * @param obj the `Equipment`
    * @param index the slot from where the `Equipment` will be removed
    * @see `GUIDTask.UnregisterEquipment`
    * @see `RemoveFromSlot`
    * @return a `TaskResolver.GiveTask` message
    */
  private def RemoveEquipmentFromSlot(target : PlanetSideGameObject with Container, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
    val regTask = GUIDTask.UnregisterEquipment(obj)(continent.GUID)
    //to avoid an error from a GUID-less object from being searchable, it is removed from the inventory first
    obj match {
      case _ : Tool =>
        TaskResolver.GiveTask(regTask.task, RemoveFromSlot(target, obj, index) +: regTask.subs)
      case _ =>
        TaskResolver.GiveTask(regTask.task, List(RemoveFromSlot(target, obj, index)))
    }
  }

  /**
    * Construct tasking that gives the `Equipment` to `target`.
    * @param target what object will accept the new `Equipment`
    * @param obj the new `Equipment`
    * @param index the slot where the new `Equipment` will be placed
    * @return a `TaskResolver.GiveTask` message
    */
  private def PutInSlot(target : PlanetSideGameObject with Container, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localTarget = target
        private val localIndex = index
        private val localObject = obj
        private val localAnnounce = self
        private val localService = avatarService

        override def isComplete : Task.Resolution.Value = {
          if(localTarget.Slot(localIndex).Equipment.contains(localObject)) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localTarget.Slot(localIndex).Equipment = localObject
          resolver ! scala.util.Success(this)
        }

        override def onSuccess() : Unit = {
          val definition = localObject.Definition
          localAnnounce ! ResponseToSelf(
            ObjectCreateDetailedMessage(
              definition.ObjectId,
              localObject.GUID,
              ObjectCreateMessageParent(localTarget.GUID, localIndex),
              definition.Packet.DetailedConstructorData(localObject).get
            )
          )
          if(localTarget.VisibleSlots.contains(localIndex)) {
            localService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(localTarget.GUID, localTarget.GUID, localIndex, localObject))
          }
        }
      })
  }

  /**
    * Construct tasking that registers all aspects of a `Player` avatar.
    * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
    * @param tplayer the avatar `Player`
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterNewAvatar(tplayer : Player) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localPlayer = tplayer
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = {
          if(localPlayer.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          log.info(s"Player $localPlayer is registered")
          resolver ! scala.util.Success(this)
          localAnnounce ! NewPlayerLoaded(localPlayer) //alerts WSA
        }

        override def onFailure(ex : Throwable) : Unit = {
          localAnnounce ! PlayerFailedToLoad(localPlayer) //alerts WSA
        }
      }, List(GUIDTask.RegisterAvatar(tplayer)(continent.GUID))
    )
  }

  /**
    * Construct tasking that registers all aspects of a `Player` avatar.
    * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
    * @param tplayer the avatar `Player`
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterAvatar(tplayer : Player) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localPlayer = tplayer
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = {
          if(localPlayer.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          log.info(s"Player $localPlayer is registered")
          resolver ! scala.util.Success(this)
          localAnnounce ! PlayerLoaded(localPlayer) //alerts WSA
        }

        override def onFailure(ex : Throwable) : Unit = {
          localAnnounce ! PlayerFailedToLoad(localPlayer) //alerts WSA
        }
      }, List(GUIDTask.RegisterPlayer(tplayer)(continent.GUID))
    )
  }

  /**
    * Construct tasking that adds a completed and registered vehicle into the scene.
    * Use this function to renew the globally unique identifiers on a vehicle that has already been added to the scene once.
    * @param vehicle the `Vehicle` object
    * @see `RegisterNewVehicle`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterVehicle(vehicle : Vehicle) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = vehicle
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = {
          if(localVehicle.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          log.info(s"Vehicle $localVehicle is registered")
          resolver ! scala.util.Success(this)
          localAnnounce ! VehicleLoaded(localVehicle) //alerts WSA
        }
      }, List(GUIDTask.RegisterVehicle(vehicle)(continent.GUID))
    )
  }

  /**
    * Construct tasking that adds a completed and registered vehicle into the scene.
    * The major difference between `RegisterVehicle` and `RegisterNewVehicle` is the assumption that this vehicle lacks an internal `Actor`.
    * Before being finished, that vehicle is supplied an `Actor` such that it may function properly.
    * This function wraps around `RegisterVehicle` and is used in case, prior to this event,
    * the vehicle is being brought into existence from scratch and was never a member of any `Zone`.
    * @param obj the `Vehicle` object
    * @see `RegisterVehicle`
    * @return a `TaskResolver.GiveTask` message
    */
  def RegisterNewVehicle(obj : Vehicle, pad : VehicleSpawnPad) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localPad = pad.Actor
        private val localSession : String = sessionId.toString
        private val localPlayer = player
        private val localVehicleService = vehicleService
        private val localZone = continent

        override def isComplete : Task.Resolution.Value = {
          if(localVehicle.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localPad ! VehicleSpawnPad.VehicleOrder(localPlayer, localVehicle)
          resolver ! scala.util.Success(this)
        }
      }, List(RegisterVehicle(obj)))
  }

  def RegisterDrivenVehicle(obj : Vehicle, driver : Player) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localDriver = driver
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = {
          if(localVehicle.HasGUID && localDriver.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localDriver.VehicleSeated = localVehicle.GUID
          OwnVehicle(localVehicle, localDriver)
          localAnnounce ! NewPlayerLoaded(localDriver) //alerts WSA
          resolver ! scala.util.Success(this)
        }

        override def onFailure(ex : Throwable) : Unit = {
          localAnnounce ! PlayerFailedToLoad(localDriver) //alerts WSA
        }
      }, List(GUIDTask.RegisterAvatar(driver)(continent.GUID), GUIDTask.RegisterVehicle(obj)(continent.GUID)))
  }

  def UnregisterDrivenVehicle(obj : Vehicle, driver : Player) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localDriver = driver

        override def isComplete : Task.Resolution.Value = {
          if(!localVehicle.HasGUID && !localDriver.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }
      }, List(GUIDTask.UnregisterAvatar(driver)(continent.GUID), GUIDTask.UnregisterVehicle(obj)(continent.GUID)))
  }

  /**
    * Construct tasking that removes the `Equipment` to `target`.
    * @param target what object that contains the `Equipment`
    * @param obj the `Equipment`
    * @param index the slot where the `Equipment` is stored
    * @return a `TaskResolver.GiveTask` message
    */
  private def RemoveFromSlot(target : PlanetSideGameObject with Container, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localTarget = target
        private val localIndex = index
        private val localObject = obj
        private val localObjectGUID = obj.GUID
        private val localAnnounce = self //self may not be the same when it executes
        private val localService = avatarService
        private val localContinent = continent.Id

        override def isComplete : Task.Resolution.Value = {
          if(localTarget.Slot(localIndex).Equipment.contains(localObject)) {
            Task.Resolution.Incomplete
          }
          else {
            Task.Resolution.Success
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localTarget.Slot(localIndex).Equipment = None
          resolver ! scala.util.Success(this)
        }

        override def onSuccess() : Unit = {
          localAnnounce ! ResponseToSelf( ObjectDeleteMessage(localObjectGUID, 0))
          if(localTarget.VisibleSlots.contains(localIndex)) {
            localService ! AvatarServiceMessage(localContinent, AvatarAction.ObjectDelete(localTarget.GUID, localObjectGUID))
          }
        }
      }
    )
  }

  /**
    * After some subtasking is completed, draw a particular slot, as if an `ObjectHeldMessage` packet was sent/received.<br>
    * <br>
    * The resulting `Task` is most useful for sequencing MAX weaponry when combined with the proper subtasks.
    * @param player the player
    * @param index the slot to be drawn
    * @param priorTasking subtasks that needs to be accomplished first
    * @return a `TaskResolver.GiveTask` message
    */
  private def DelayedObjectHeld(player : Player, index : Int, priorTasking : List[TaskResolver.GiveTask]) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localPlayer = player
        private val localSlot = index
        private val localAnnounce = self
        private val localService = avatarService

        override def isComplete : Task.Resolution.Value = {
          if(localPlayer.DrawnSlot == localSlot) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localPlayer.DrawnSlot = localSlot
          resolver ! scala.util.Success(this)
        }

        override def onSuccess() : Unit = {
          localAnnounce ! ResponseToSelf( ObjectHeldMessage(localPlayer.GUID, localSlot, true))
          localService ! AvatarServiceMessage(localPlayer.Continent, AvatarAction.ObjectHeld(localPlayer.GUID, localSlot))
        }
      }, priorTasking
    )
  }

  /**
    * Before calling `Interstellar.GetWorld` to change zones, perform the following task (which can be a nesting of subtasks).
    * @param priorTask the tasks to perform
    * @param zoneId the zone to load afterwards
    * @return a `TaskResolver.GiveTask` message
    */
  def TaskBeforeZoneChange(priorTask : TaskResolver.GiveTask, zoneId : String) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localService = cluster
        private val localMsg = InterstellarCluster.GetWorld(zoneId)

        override def isComplete : Task.Resolution.Value = priorTask.task.isComplete

        def Execute(resolver : ActorRef) : Unit = {
          localService ! localMsg
          resolver ! scala.util.Success(this)
        }
      }, List(priorTask)
    )
  }

  def CallBackForTask(task : TaskResolver.GiveTask, sendTo : ActorRef, pass : Any) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val destination = sendTo
        private val passMsg = pass

        def Execute(resolver : ActorRef) : Unit = {
          destination ! passMsg
          resolver ! scala.util.Success(this)
        }
      }, List(task)
    )
  }

  /**
    * After a client has connected to the server, their account is used to generate a list of characters.
    * On the character selection screen, each of these characters is made to exist temporarily when one is selected.
    * This "character select screen" is an isolated portion of the client, so it does not have any external constraints.
    * Temporary global unique identifiers are assigned to the underlying `Player` objects so that they can be turned into packets.
    * @param tplayer the `Player` object
    * @param gen a constant source of incremental unique numbers
    */
  private def SetCharacterSelectScreenGUID(tplayer : Player, gen : AtomicInteger) : Unit = {
    tplayer.Holsters().foreach(holster => {
      SetCharacterSelectScreenGUID_SelectEquipment(holster.Equipment, gen)
    })
    tplayer.GUID = PlanetSideGUID(gen.getAndIncrement)
  }

  /**
    * Assists in assigning temporary global unique identifiers.
    * If the item is a `Tool`, handle the embedded `AmmoBox` objects in each ammunition slot.
    * Whether or not, give the object itself a GUID as well.
    * @param item the piece of `Equipment`
    * @param gen a constant source of incremental unique numbers
    */
  private def SetCharacterSelectScreenGUID_SelectEquipment(item : Option[Equipment], gen : AtomicInteger) : Unit = {
    item match {
      case Some(tool : Tool) =>
        tool.AmmoSlots.foreach(slot => { slot.Box.GUID = PlanetSideGUID(gen.getAndIncrement) })
        tool.GUID = PlanetSideGUID(gen.getAndIncrement)
      case Some(item : Equipment) =>
        item.GUID = PlanetSideGUID(gen.getAndIncrement)
      case None => ;
    }
  }

  /**
    * After the user has selected a character to load from the "character select screen,"
    * the temporary global unique identifiers used for that screen are stripped from the underlying `Player` object that was selected.
    * Characters that were not selected may  be destroyed along with their temporary GUIDs.
    * @param tplayer the `Player` object
    */
  private def RemoveCharacterSelectScreenGUID(tplayer : Player) : Unit = {
    tplayer.Holsters().foreach(holster => {
      RemoveCharacterSelectScreenGUID_SelectEquipment(holster.Equipment)
    })
    tplayer.Invalidate()
  }

  /**
    * Assists in stripping temporary global unique identifiers.
    * If the item is a `Tool`, handle the embedded `AmmoBox` objects in each ammunition slot.
    * Whether or not, remove the GUID from the object itself.
    * @param item the piece of `Equipment`
    */
  private def RemoveCharacterSelectScreenGUID_SelectEquipment(item : Option[Equipment]) : Unit = {
    item match {
      case Some(item : Tool) =>
        item.AmmoSlots.foreach(slot => { slot.Box.Invalidate() })
        item.Invalidate()
      case Some(item : Equipment) =>
        item.Invalidate()
      case None => ;
    }
  }

  /**
    * The process of hacking an object is completed.
    * Pass the message onto the hackable object and onto the local events system.
    * @param target the `Hackable` object that has been hacked
    * @param unk na;
    *            used by `HackMessage` as `unk5`
    * @see `HackMessage`
    */
  //TODO add params here depending on which params in HackMessage are important
  private def FinishHacking(target : PlanetSideServerObject with Hackable, unk : Long)() : Unit = {
    log.info(s"Hacked a $target")
    // Wait for the target actor to set the HackedBy property, otherwise LocalAction.HackTemporarily will not complete properly
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(target.Actor, CommonMessages.Hack(player))(1 second).mapTo[Boolean].onComplete {
      case Success(_) =>
        localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerSound(player.GUID, target.HackSound, player.Position, 30, 0.49803925f))
        target match {
          case term: CaptureTerminal =>
            val isResecured = player.Faction == target.Faction
            localService ! LocalServiceMessage(continent.Id, LocalAction.HackCaptureTerminal(player.GUID, continent, term, unk, 8L, isResecured))
          case _ => localService ! LocalServiceMessage(continent.Id, LocalAction.HackTemporarily(player.GUID, continent, target, unk, target.HackEffectDuration(GetPlayerHackLevel())))
        }
      case scala.util.Failure(_) => log.warn(s"Hack message failed on target guid: ${target.GUID}")
    }
  }

  /**
    * The process of hacking/jacking a vehicle is complete.
    * Change the faction of the vehicle to the hacker's faction and remove all occupants.
    *
    * @param target The `Vehicle` object that has been hacked/jacked
    * @param unk na; used by HackMessage` as `unk5`
    */
  private def FinishHackingVehicle(target : Vehicle, unk : Long)(): Unit = {
    log.info(s"Vehicle guid: ${target.GUID} has been jacked")


    // Forcefully dismount any cargo
    target.CargoHolds.values.foreach(cargoHold =>  {
      cargoHold.Occupant match {
        case Some(cargo : Vehicle) => {
          cargo.Seats(0).Occupant match {
            case Some(cargoDriver: Player) =>
              DismountVehicleCargo(cargoDriver.GUID, cargo.GUID, bailed = target.Flying, requestedByPassenger = false, kicked = true )
            case None =>
              log.error("FinishHackingVehicle: vehicle in cargo hold missing driver")
              HandleDismountVehicleCargo(player.GUID, cargo.GUID, cargo, target.GUID, target, false, false, true)
          }
        }
        case None => ;
      }
    })

    // Forcefully dismount all seated occupants from the vehicle
    target.Seats.values.foreach(seat => {
      seat.Occupant match {
        case Some(tplayer) =>
          seat.Occupant = None
          tplayer.VehicleSeated = None
          if(tplayer.HasGUID) {
            vehicleService ! VehicleServiceMessage(tplayer.Continent, VehicleAction.KickPassenger(tplayer.GUID, 4, unk2 = false, target.GUID))
          }
        case None => ;
      }
    })

    // If the vehicle can fly and is flying deconstruct it, and well played to whomever managed to hack a plane in mid air. I'm impressed.
    if(target.Definition.CanFly && target.Flying) {
      // todo: Should this force the vehicle to land in the same way as when a pilot bails with passengers on board?
      vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), continent))
      vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, continent, Some(0 seconds)))
    } else { // Otherwise handle ownership transfer as normal
      // Remove ownership of our current vehicle, if we have one
      player.VehicleOwned match {
        case Some(guid : PlanetSideGUID) =>
          continent.GUID(guid) match {
            case Some(vehicle: Vehicle) =>
              DisownVehicle(player, vehicle)
            case _ => ;
          }
        case _ => ;
      }

      target.Owner match {
        case Some(previousOwnerGuid: PlanetSideGUID) =>
          // Remove ownership of the vehicle from the previous player
          continent.GUID(previousOwnerGuid) match {
            case Some(player: Player) =>
              DisownVehicle(player, target)
            case _ => ; // Vehicle already has no owner
          }
        case _ => ;
      }

      // Now take ownership of the jacked vehicle
      target.Faction = player.Faction
      OwnVehicle(target, player)

      //todo: Send HackMessage -> HackCleared to vehicle? can be found in packet captures. Not sure if necessary.

      // And broadcast the faction change to other clients
      sendResponse(SetEmpireMessage(target.GUID, player.Faction))
      avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.SetEmpire(player.GUID, target.GUID, player.Faction))
    }

    localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerSound(player.GUID, TriggeredSound.HackVehicle, target.Position, 30, 0.49803925f))

    // Clean up after specific vehicles, e.g. remove router telepads
    // If AMS is deployed, swap it to the new faction
    target.Definition match {
      case GlobalDefinitions.router =>
        log.info("FinishHackingVehicle: cleaning up after a router ...")
        RemoveTelepads(target)
      case GlobalDefinitions.ams
        if(target.DeploymentState == DriveState.Deployed) =>
        vehicleService ! VehicleServiceMessage.AMSDeploymentChange(continent)
      case _ => ;
    }
  }

  /**
    * The process of resecuring an IFF lock is finished
    * Clear the hack state and send to clients
    * @param lock the `IFFLock` object that has been resecured
    */
  private def FinishResecuringIFFLock(lock: IFFLock)() : Unit = {
    localService ! LocalServiceMessage(continent.Id, LocalAction.ClearTemporaryHack(player.GUID, lock))
  }

  /**
    * The process of upgrading a turret's weapon(s) is completed.
    * Pass the message onto the turret and onto the vehicle events system.
    * Additionally, force-deplete the ammunition count of the nano-dispenser used to perform the upgrade.
    * @param target the turret
    * @param tool the nano-dispenser that was used to perform this upgrade
    * @param upgrade the new upgrade state
    */
  private def FinishUpgradingMannedTurret(target : FacilityTurret, tool : Tool, upgrade : TurretUpgrade.Value)() : Unit = {
    log.info(s"Converting manned wall turret weapon to $upgrade")
    tool.Magazine = 0
    sendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, tool.GUID, 0))
    vehicleService ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(target), continent))
    vehicleService ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(target, continent, upgrade))
  }

  /**
    * Temporary function that iterates over vehicle permissions and turns them into `PlanetsideAttributeMessage` packets.<br>
    * <br>
    * 2 November 2017:<br>
    * Unexpected behavior causes seat mount points to become blocked when a new driver claims the vehicle.
    * For the purposes of ensuring that other players are always aware of the proper permission state of the trunk and seats,
    * packets are intentionally dispatched to the current client to update the states.
    * Perform this action just after any instance where the client would initially gain awareness of the vehicle.
    * The most important examples include either the player or the vehicle itself spawning in for the first time.<br>
    * <br>
    * 20 February 2018:<br>
    * Occasionally, during deployment, local(?) vehicle seat access permissions may change.
    * This results in players being locked into their own vehicle.
    * Reloading vehicle permissions supposedly ensures the seats will be properly available.
    * This is considered a client issue; but, somehow, it also impacts server operation somehow.<br>
    * <br>
    * 22 June 2018:<br>
    * I think vehicle ownership works properly now.
    * @param vehicle the `Vehicle`
    */
  def ReloadVehicleAccessPermissions(vehicle : Vehicle) : Unit = {
    if(vehicle.Faction == player.Faction) {
      val vehicle_guid = vehicle.GUID
      (0 to 3).foreach(group => {
        sendResponse(
          PlanetsideAttributeMessage(vehicle_guid, group + 10, vehicle.PermissionGroup(group).get.id)
        )
      })
    }
  }

  /**
    * na
    * @param vehicle na
    * @param tplayer na
    * @return na
    */
  def OwnVehicle(vehicle : Vehicle, tplayer : Player) : Option[Vehicle] = OwnVehicle(vehicle, Some(tplayer))

  /**
    * na
    * @param vehicle na
    * @param playerOpt na
    * @return na
    */
  def OwnVehicle(vehicle : Vehicle, playerOpt : Option[Player]) : Option[Vehicle] = {
    playerOpt match {
      case Some(tplayer) =>
        tplayer.VehicleOwned = vehicle.GUID
        vehicle.AssignOwnership(playerOpt)

        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.Ownership(player.GUID, vehicle.GUID))
        ReloadVehicleAccessPermissions(vehicle)
        Some(vehicle)
      case None =>
        None
    }
  }

  /**
    * Disassociate this client's player (oneself) from a vehicle that he owns.
    */
  def DisownVehicle() : Option[Vehicle] = DisownVehicle(player)

  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the current continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver seat.
    * This is the player side of vehicle ownership removal.
    * @param tplayer the player
    */
  def DisownVehicle(tplayer : Player) : Option[Vehicle] = {
    tplayer.VehicleOwned match {
      case Some(vehicle_guid) =>
        tplayer.VehicleOwned = None
        continent.GUID(vehicle_guid) match {
          case Some(vehicle : Vehicle) =>
            DisownVehicle(tplayer, vehicle)
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  /**
    * Disassociate a player from a vehicle that he owns without associating a different player as the owner.
    * Set the vehicle's driver seat permissions and passenger and gunner seat permissions to "allow empire,"
    * then reload them for all clients.
    * This is the vehicle side of vehicle ownership removal.
    * @param tplayer the player
    */
  private def DisownVehicle(tplayer : Player, vehicle : Vehicle) : Option[Vehicle] = {
    val pguid = tplayer.GUID
    if(vehicle.Owner.contains(pguid)) {
      vehicle.AssignOwnership(None)
      val factionOnContinent = s"${continent.Id}/${vehicle.Faction}"
      vehicleService ! VehicleServiceMessage(factionOnContinent, VehicleAction.Ownership(pguid, PlanetSideGUID(0)))
      val vguid = vehicle.GUID
      val empire = VehicleLockState.Empire.id
      (0 to 2).foreach(group => {
        vehicle.PermissionGroup(group, empire)
        vehicleService ! VehicleServiceMessage(factionOnContinent, VehicleAction.SeatPermissions(pguid, vguid, group, empire))
      })
      ReloadVehicleAccessPermissions(vehicle)
      Some(vehicle)
    }
    else {
      None
    }
  }

  /**
    * Gives a target player positive battle experience points only.
    * If the player has access to more implant slots as a result of changing battle experience points, unlock those slots.
    * @param avatar the player
    * @param bep the change in experience points, positive by assertion
    * @return the player's current battle experience points
    */
  def AwardBattleExperiencePoints(avatar : Avatar, bep : Long) : Long = {
    val oldBep = avatar.BEP
    if(bep <= 0) {
      log.error(s"trying to set $bep battle experience points on $avatar; value can not be negative")
      oldBep
    }
    else {
      val oldSlots = DetailedCharacterData.numberOfImplantSlots(oldBep)
      val newBep = oldBep + bep
      val newSlots = DetailedCharacterData.numberOfImplantSlots(newBep)
      avatar.BEP = newBep
      if(newSlots > oldSlots) {
        (oldSlots until newSlots).foreach(slotNumber => {
          avatar.Implants(slotNumber).Unlocked = true
          log.info(s"unlocking implant slot $slotNumber for $avatar")
        })
      }
      newBep
    }
  }

  /**
    * Common preparation for interfacing with a vehicle.
    * Join a vehicle-specific group for shared updates.
    * Construct every object in the vehicle's inventory fpr shared manipulation updates.
    * @param vehicle the vehicle
    */
  def AccessContents(vehicle : Vehicle) : Unit = {
    vehicleService ! Service.Join(s"${vehicle.Actor}")
    val parent_guid = vehicle.GUID
    vehicle.Trunk.Items.foreach(entry => {
      val obj = entry.obj
      val objDef = obj.Definition
      sendResponse(
        ObjectCreateDetailedMessage(
          objDef.ObjectId,
          obj.GUID,
          ObjectCreateMessageParent(parent_guid, entry.start),
          objDef.Packet.DetailedConstructorData(obj).get
        )
      )
    })
  }

  /**
    * Common preparation for disengaging from a vehicle.
    * Leave the vehicle-specific group that was used for shared updates.
    * Deconstruct every object in the vehicle's inventory.
    * @param vehicle the vehicle
    */
  def UnAccessContents(vehicle : Vehicle) : Unit = {
    vehicleService ! Service.Leave(Some(s"${vehicle.Actor}"))
    vehicle.Trunk.Items.foreach(entry =>{
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
  def FindContainedEquipment : (Option[PlanetSideGameObject with Container], Option[Equipment]) = {
    player.VehicleSeated match {
      case Some(vehicle_guid) => //weapon is vehicle turret?
        continent.GUID(vehicle_guid) match {
          case Some(vehicle : Mountable with MountedWeapons with Container) =>
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
  def FindEquipment : Option[Equipment] = FindContainedEquipment._2

  /**
    * Check two locations for a controlled piece of equipment that is associated with the `player`.
    * Filter for discovered `Tool`-type `Equipment`.
    * @return a `Tuple` of the returned values;
    *         the first value is a `Container` object;
    *         the second value is an `Tool` object in the former
    */
  def FindContainedWeapon : (Option[PlanetSideGameObject with Container], Option[Tool]) = {
    FindContainedEquipment match {
      case (container, Some(tool : Tool)) =>
        (container, Some(tool))
      case _ =>
        (None, None)
    }
  }

  /**
    * Runs `FindContainedWeapon` but ignores the `Container` object output.
    * @return a `Tool` object
    */
  def FindWeapon : Option[Tool] = FindContainedWeapon._2

  /**
    * Within a specified `Container`, find the smallest number of `Equipment` objects of a certain qualifying type
    * whose sum count is greater than, or equal to, a `desiredAmount` based on an accumulator method.<br>
    * <br>
    * In an occupied `List` of returned `Inventory` entries, all but the last entry is typically considered "emptied."
    * For objects with contained quantities, the last entry may require having that quantity be set to a non-zero number.
    * @param obj the `Container` to search
    * @param filterTest test used to determine inclusivity of `Equipment` collection
    * @param desiredAmount how much is requested
    * @param counting test used to determine value of found `Equipment`;
    *                 defaults to one per entry
    * @return a `List` of all discovered entries totaling approximately the amount requested
    */
  def FindEquipmentStock(obj : Container,
                         filterTest : (Equipment)=>Boolean,
                         desiredAmount : Int,
                         counting : (Equipment)=>Int = DefaultCount) : List[InventoryItem] = {
    var currentAmount : Int = 0
    obj.Inventory.Items
      .filter(item => filterTest(item.obj))
      .toList
      .sortBy(_.start)
      .takeWhile(entry => {
        val previousAmount = currentAmount
        currentAmount += counting(entry.obj)
        previousAmount < desiredAmount
      })
  }

  /**
    * The default counting function for an item.
    * Counts the number of item(s).
    * @param e the `Equipment` object
    * @return the quantity;
    *         always one
    */
  def DefaultCount(e : Equipment) : Int = 1

  /**
    * The counting function for an item of `AmmoBox`.
    * Counts the `Capacity` of the ammunition.
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountAmmunition(e : Equipment) : Int = {
    e match {
      case a : AmmoBox =>
        a.Capacity
      case _ =>
        0
    }
  }

  /**
    * The counting function for an item of `Tool` where the item is also a grenade.
    * Counts the number of grenades.
    * @see `GlobalDefinitions.isGrenade`
    * @param e the `Equipment` object
    * @return the quantity
    */
  def CountGrenades(e : Equipment) : Int = {
    e match {
      case t : Tool =>
        (GlobalDefinitions.isGrenade(t.Definition):Int) * t.Magazine
      case _ =>
        0
    }
  }

  /**
    * Flag an `AmmoBox` object that matches for the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is an `AmmoBox` of the correct ammunition type; `false`, otherwise
    */
  def FindAmmoBoxThatUses(ammo : Ammo.Value)(e : Equipment) : Boolean = {
    e match {
      case t : AmmoBox =>
        t.AmmoType == ammo
      case _ =>
        false
    }
  }

  /**
    * Flag a `Tool` object that matches for loading the given ammunition type.
    * @param ammo the type of `Ammo` to check
    * @param e the `Equipment` object
    * @return `true`, if the object is a `Tool` that loads the correct ammunition type; `false`, otherwise
    */
  def FindToolThatUses(ammo : Ammo.Value)(e : Equipment) : Boolean = {
    e match {
      case t : Tool =>
        t.Definition.AmmoTypes.map { _.AmmoType }.contains(ammo)
      case _ =>
        false
    }
  }

  /**
    * Get the current `Vehicle` object that the player is riding/driving.
    * The vehicle must be found solely through use of `player.VehicleSeated`.
    * @return the vehicle
    */
  def FindLocalVehicle : Option[Vehicle] = {
    player.VehicleSeated match {
      case Some(vehicle_guid) =>
        continent.GUID(vehicle_guid) match {
          case Some(obj : Vehicle) =>
            Some(obj)
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  /**
    * Given an object that contains an item (`Equipment`) in its `Inventory` at a certain location,
    * remove it permanently.
    * @param obj the `Container`
    * @param start where the item can be found
    * @param item an object to unregister;
    *             not explicitly checked
    */
  private def DeleteEquipment(obj : PlanetSideGameObject with Container)(start : Int, item : Equipment) : Unit = {
    val item_guid = item.GUID
    obj.Slot(start).Equipment = None
    //obj.Inventory -= start
    taskResolver ! GUIDTask.UnregisterEquipment(item)(continent.GUID)
    sendResponse(ObjectDeleteMessage(item_guid, 0))
  }

  /**
    * Given a vehicle that contains an item (`Equipment`) in its `Trunk` at a certain location,
    * remove it permanently.
    * @see `DeleteEquipment`
    * @param obj the `Vehicle`
    * @param start where the item can be found
    * @param item an object to unregister;
    *             not explicitly checked
    */
  private def DeleteEquipmentFromVehicle(obj : Vehicle)(start : Int, item : Equipment) : Unit = {
    val item_guid = item.GUID
    DeleteEquipment(obj)(start, item)
    vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player.GUID, item_guid))
  }

  /**
    * Given an object that contains a box of amunition in its `Inventory` at a certain location,
    * change the amount of ammunition within that box.
    * @param obj the `Container`
    * @param box an `AmmoBox` to modify
    * @param reloadValue the value to modify the `AmmoBox`;
    *                    subtracted from the current `Capacity` of `Box`
    */
  private def ModifyAmmunition(obj : PlanetSideGameObject with Container)(box : AmmoBox, reloadValue : Int) : Unit = {
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
  private def ModifyAmmunitionInVehicle(obj : Vehicle)(box : AmmoBox, reloadValue : Int) : Unit = {
    val capacity = ModifyAmmunition(obj)(box, reloadValue)
    vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.InventoryState(player.GUID, box, obj.GUID, obj.Find(box).get, box.Definition.Packet.DetailedConstructorData(box).get))
  }

  /**
    * Announce that an already-registered `AmmoBox` object exists in a given position in some `Container` object's inventory.
    * @see `StowEquipmentInVehicles`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    */
  def StowEquipment(obj : PlanetSideGameObject with Container)(index : Int, item : AmmoBox) : Unit = {
    obj.Inventory += index -> item
    sendResponse(ObjectAttachMessage(obj.GUID, item.GUID, index))
  }

  /**
    * Announce that an already-registered `AmmoBox` object exists in a given position in some vehicle's inventory.
    * @see `StowEquipment`
    * @see `ChangeAmmoMessage`
    * @param obj the `Vehicle` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    */
  def StowEquipmentInVehicles(obj : Vehicle)(index : Int, item : AmmoBox) : Unit = {
    StowEquipment(obj)(index, item)
    vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player.GUID, obj.GUID, index, item))
  }

  /**
    * Prepare tasking that registers an `AmmoBox` object
    * and announces that it exists in a given position in some `Container` object's inventory.
    * `PutEquipmentInSlot` is the fastest way to achieve these goals.
    * @see `StowNewEquipmentInVehicle`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item the `Equipment` item
    * @return a `TaskResolver.GiveTask` chain that executes the action
    */
  def StowNewEquipment(obj : PlanetSideGameObject with Container)(index : Int, item : Equipment) : TaskResolver.GiveTask = {
    PutEquipmentInSlot(obj, item, index)
  }

  /**
    * Prepare tasking that registers an `AmmoBox` object
    * and announces that it exists in a given position in some vehicle's inventory.
    * `PutEquipmentInSlot` is the fastest way to achieve these goals.
    * @see `StowNewEquipment`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item the `Equipment` item
    * @return a `TaskResolver.GiveTask` chain that executes the action
    */
  def StowNewEquipmentInVehicle(obj : Vehicle)(index : Int, item : Equipment) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localService = vehicleService
        private val localPlayer = player
        private val localVehicle = obj
        private val localIndex = index
        private val localItem = item

        override def isComplete : Task.Resolution.Value = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          localService ! VehicleServiceMessage(
            s"${localVehicle.Actor}",
            VehicleAction.StowEquipment(localPlayer.GUID, localVehicle.GUID, localIndex, localItem)
          )
          resolver ! scala.util.Success(this)
        }
      },
      List(StowNewEquipment(obj)(index, item))
    )
  }

  /**
    * Given an item, and two places, one where the item currently is and one where the item will be moved,
    * perform a controlled transfer of the item.
    * If something exists at the `destination` side of the transfer in the position that `item` will occupy,
    * resolve its location as well by swapping it with where `item` originally was positioned.<br>
    * <br>
    * Parameter checks will not be performed.
    * Do perform checks before sending data to this function.
    * Do not call with incorrect or unverified data, e.g., `item` not actually being at `source` @ `index`.
    * @param item the item being moved
    * @param source the container in which `item` is currently located
    * @param index the index position in `source` where `item` is currently located
    * @param destination the container where `item` is being moved
    * @param dest the index position in `destination` where `item` is being moved
    * @param destinationCollisionEntry information about the contents in an area of `destination` starting at index `dest`
    */
  private def PerformMoveItem(item : Equipment,
                              source : PlanetSideGameObject with Container,
                              index : Int,
                              destination : PlanetSideGameObject with Container,
                              dest : Int,
                              destinationCollisionEntry : Option[InventoryItem]) : Unit = {
    val item_guid = item.GUID
    val source_guid = source.GUID
    val destination_guid = destination.GUID
    val player_guid = player.GUID
    val indexSlot = source.Slot(index)
    val sourceIsNotDestination : Boolean = source != destination //if source is destination, explicit OCDM is not required
    if(sourceIsNotDestination) {
      log.info(s"MoveItem: $item moved from $source @ $index to $destination @ $dest")
    }
    else {
      log.info(s"MoveItem: $item moved from $index to $dest in $source")
    }
    //remove item from source
    indexSlot.Equipment = None
    source match {
      case obj : Vehicle =>
        vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player_guid, item_guid))
      case obj : Player =>
        if(obj.isBackpack || source.VisibleSlots.contains(index)) { //corpse being looted, or item was in hands
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, item_guid))
        }
      case _ => ;
    }

    destinationCollisionEntry match { //do we have a swap item in the destination slot?
      case Some(InventoryItem(item2, destIndex)) => //yes, swap
        //cleanly shuffle items around to avoid losing icons
        //the next ObjectDetachMessage is necessary to avoid icons being lost, but only as part of this swap
        sendResponse(ObjectDetachMessage(source_guid, item_guid, Vector3.Zero, 0f))
        val item2_guid = item2.GUID
        destination.Slot(destIndex).Equipment = None //remove the swap item from destination
        (indexSlot.Equipment = item2) match {
          case Some(_) => //item and item2 swapped places successfully
            log.info(s"MoveItem: $item2 swapped to $source @ $index")
            //remove item2 from destination
            sendResponse(ObjectDetachMessage(destination_guid, item2_guid, Vector3.Zero, 0f))
            destination match {
              case obj : Vehicle =>
                vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player_guid, item2_guid))
              case obj : Player =>
                if(obj.isBackpack || destination.VisibleSlots.contains(dest)) { //corpse being looted, or item was accessible
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, item2_guid))
                  //put hand down locally
                  if(dest == player.DrawnSlot) {
                    player.DrawnSlot = Player.HandsDownSlot
                  }
                }
              case _ => ;
            }
            //display item2 in source
            if(sourceIsNotDestination && player == source) {
              val objDef = item2.Definition
              sendResponse(
                ObjectCreateDetailedMessage(
                  objDef.ObjectId,
                  item2_guid,
                  ObjectCreateMessageParent(source_guid, index),
                  objDef.Packet.DetailedConstructorData(item2).get
                )
              )
            }
            else {
              sendResponse(ObjectAttachMessage(source_guid, item2_guid, index))
            }
            source match {
              case obj : Vehicle =>
                item2.Faction = PlanetSideEmpire.NEUTRAL
                vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player_guid, source_guid, index, item2))
              case obj : Player =>
                item2.Faction = obj.Faction
                if(source.VisibleSlots.contains(index)) { //item is put in hands
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(player_guid, source_guid, index, item2))
                }
                else if(obj.isBackpack) { //corpse being given item
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.StowEquipment(player_guid, source_guid, index, item2))
                }
              case _ =>
                item2.Faction = PlanetSideEmpire.NEUTRAL
            }

          case None => //item2 does not fit; drop on ground
            log.info(s"MoveItem: $item2 can not fit in swap location; dropping on ground @ ${source.Position}")
            val pos = source.Position
            val sourceOrientZ = source.Orientation.z
            val orient : Vector3 = Vector3(0f, 0f, sourceOrientZ)
            continent.Ground ! Zone.Ground.DropItem(item2, pos, orient)
            sendResponse(ObjectDetachMessage(destination_guid, item2_guid, pos, sourceOrientZ)) //ground
          val objDef = item2.Definition
            destination match {
              case obj : Vehicle =>
                vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player_guid, item2_guid))
              case _ => ;
              //Player does not require special case; the act of dropping forces the item and icon to change
            }
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DropItem(player_guid, item2, continent))
        }

      case None => ;
    }
    //move item into destination slot
    destination.Slot(dest).Equipment = item
    if(sourceIsNotDestination && player == destination) {
      val objDef = item.Definition
      sendResponse(
        ObjectCreateDetailedMessage(
          objDef.ObjectId,
          item_guid,
          ObjectCreateMessageParent(destination_guid, dest),
          objDef.Packet.DetailedConstructorData(item).get
        )
      )
    }
    else {
      sendResponse(ObjectAttachMessage(destination_guid, item_guid, dest))
    }
    destination match {
      case obj : Vehicle =>
        item.Faction = PlanetSideEmpire.NEUTRAL
        vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player_guid, destination_guid, dest, item))
      case obj : Player =>
        if(destination.VisibleSlots.contains(dest)) { //item is put in hands
          item.Faction = obj.Faction
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(player_guid, destination_guid, dest, item))
        }
        else if(obj.isBackpack) { //corpse being given item
          item.Faction = PlanetSideEmpire.NEUTRAL
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.StowEquipment(player_guid, destination_guid, dest, item))
        }
      case _ =>
        item.Faction = PlanetSideEmpire.NEUTRAL
    }
  }

  /**
    * na
    * @param equipment na
    * @param obj na
    * @return `true`, if the object is allowed to contain the type of equipment object
    */
  def PermitEquipmentStow(equipment : Equipment, obj : PlanetSideGameObject with Container) : Boolean = {
    equipment match {
      case _ : BoomerTrigger =>
        obj.isInstanceOf[Player] //a BoomerTrigger can only be stowed in a player's holsters or inventory
      case _ =>
        true
    }
  }

  /**
    * na
    * @param tool na
    * @param obj na
    */
  def PerformToolAmmoChange(tool : Tool, obj : PlanetSideGameObject with Container) : Unit = {
    val originalAmmoType = tool.AmmoType
    do {
      val requestedAmmoType = tool.NextAmmoType
      val fullMagazine = tool.MaxMagazine
      if(requestedAmmoType != tool.AmmoSlot.Box.AmmoType) {
        FindEquipmentStock(obj, FindAmmoBoxThatUses(requestedAmmoType), fullMagazine, CountAmmunition).reverse match {
          case Nil => ;
          case x :: xs =>
            val (deleteFunc, modifyFunc) : ((Int, AmmoBox)=>Unit, (AmmoBox, Int)=>Unit) = obj match {
              case (veh : Vehicle) =>
                (DeleteEquipmentFromVehicle(veh), ModifyAmmunitionInVehicle(veh))
              case _ =>
                (DeleteEquipment(obj), ModifyAmmunition(obj))
            }
            val (stowFuncTask, stowFunc) : ((Int, AmmoBox)=>TaskResolver.GiveTask, (Int, AmmoBox)=>Unit) = obj match {
              case (veh : Vehicle) =>
                (StowNewEquipmentInVehicle(veh), StowEquipmentInVehicles(veh))
              case _ =>
                (StowNewEquipment(obj), StowEquipment(obj))
            }
            xs.foreach(item => {
              obj.Inventory -= x.start
              deleteFunc(item.start, item.obj.asInstanceOf[AmmoBox])
            })

            //box will be the replacement ammo; give it the discovered magazine and load it into the weapon @ 0
            val box = x.obj.asInstanceOf[AmmoBox]
            val originalBoxCapacity = box.Capacity
            val tailReloadValue : Int = if(xs.isEmpty) { 0 } else { xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).reduceLeft(_ + _) }
            val sumReloadValue : Int = originalBoxCapacity + tailReloadValue
            val previousBox = tool.AmmoSlot.Box //current magazine in tool
            sendResponse(ObjectDetachMessage(tool.GUID, previousBox.GUID, Vector3.Zero, 0f))
            sendResponse(ObjectDetachMessage(player.GUID, box.GUID, Vector3.Zero, 0f))
            obj.Inventory -= x.start //remove replacement ammo from inventory
          val ammoSlotIndex = tool.FireMode.AmmoSlotIndex
            tool.AmmoSlots(ammoSlotIndex).Box = box //put replacement ammo in tool
            sendResponse(ObjectAttachMessage(tool.GUID, box.GUID, ammoSlotIndex))

            //announce swapped ammunition box in weapon
            val previous_box_guid = previousBox.GUID
            val boxDef = box.Definition
            val box_guid = box.GUID
            val tool_guid = tool.GUID
            sendResponse(ChangeAmmoMessage(tool_guid, box.Capacity))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeAmmo(player.GUID, tool_guid, ammoSlotIndex,previous_box_guid, boxDef.ObjectId, box.GUID, boxDef.Packet.ConstructorData(box).get))

            //handle inventory contents
            box.Capacity = (if(sumReloadValue <= fullMagazine) {
              sumReloadValue
            }
            else {
              val splitReloadAmmo : Int = sumReloadValue - fullMagazine
              log.info(s"ChangeAmmo: taking ${originalBoxCapacity - splitReloadAmmo} from a box of ${originalBoxCapacity} $requestedAmmoType")
              val boxForInventory = AmmoBox(box.Definition, splitReloadAmmo)
              obj.Inventory += x.start -> boxForInventory //block early; assumption warning: swappable ammo types have the same icon size
              taskResolver ! stowFuncTask(x.start, boxForInventory)
              fullMagazine
            })
            sendResponse(InventoryStateMessage(box.GUID, tool.GUID, box.Capacity)) //should work for both players and vehicles
            log.info(s"ChangeAmmo: loading ${box.Capacity} $requestedAmmoType into ${tool.GUID} @ $ammoSlotIndex")
            if(previousBox.Capacity > 0) {
              //divide capacity across other existing and not full boxes of that ammo type
              var capacity = previousBox.Capacity
              val iter = obj.Inventory.Items
                .filter(entry => {
                  entry.obj match {
                    case (item : AmmoBox) =>
                      item.AmmoType == originalAmmoType && item.FullCapacity != item.Capacity
                    case _ =>
                      false
                  }
                })
                .toList
                .sortBy(_.start)
                .iterator
              while(capacity > 0 && iter.hasNext) {
                val entry = iter.next
                val item : AmmoBox = entry.obj.asInstanceOf[AmmoBox]
                val ammoAllocated = math.min(item.FullCapacity - item.Capacity, capacity)
                log.info(s"ChangeAmmo: putting $ammoAllocated back into a box of ${item.Capacity} $originalAmmoType")
                capacity -= ammoAllocated
                modifyFunc(item, -ammoAllocated)
              }
              previousBox.Capacity = capacity
            }

            if(previousBox.Capacity > 0) {
              //split previousBox into AmmoBox objects of appropriate max capacity, e.g., 100 9mm -> 2 x 50 9mm
              obj.Inventory.Fit(previousBox) match {
                case Some(index) =>
                  stowFunc(index, previousBox)
                case None =>
                  NormalItemDrop(player, continent, avatarService)(previousBox)
              }
              val dropFunc : (Equipment)=>TaskResolver.GiveTask = NewItemDrop(player, continent, avatarService)
              AmmoBox.Split(previousBox) match {
                case Nil  | _ :: Nil => ; //done (the former case is technically not possible)
                case _ :: xs =>
                  modifyFunc(previousBox, 0) //update to changed capacity value
                  xs.foreach(box => {
                    obj.Inventory.Fit(box) match {
                      case Some(index) =>
                        obj.Inventory += index -> box //block early, for purposes of Fit
                        taskResolver ! stowFuncTask(index, box)
                      case None =>
                        taskResolver ! dropFunc(box)
                    }
                  })
              }
            }
            else {
              taskResolver ! GUIDTask.UnregisterObjectTask(previousBox)(continent.GUID)
            }
        }
      }
    }
    while(tool.AmmoType != originalAmmoType && tool.AmmoType != tool.AmmoSlot.Box.AmmoType)
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
    * @param service a reference to the event system that announces that the item has been dropped on the ground;
    *                "AvatarService";
    *                curried for callback
    * @param item the item
    */
  def NormalItemDrop(obj : PlanetSideGameObject with Container, zone : Zone, service : ActorRef)(item : Equipment) : Unit = {
    continent.Ground ! Zone.Ground.DropItem(item, obj.Position, Vector3.z(obj.Orientation.z))
  }

  /**
    * Register an `Equipment` item and then drop it on the ground.
    * @see `NormalItemDrop`
    * @param obj a `Container` object that represents where the item will be dropped;
    *            curried for callback
    * @param zone the continent in which the item is being dropped;
    *             curried for callback
    * @param service a reference to the event system that announces that the item has been dropped on the ground;
    *                "AvatarService";
    *                curried for callback
    * @param item the item
    */
  def NewItemDrop(obj : PlanetSideGameObject with Container, zone : Zone, service : ActorRef)(item : Equipment) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localItem = item
        private val localFunc : (Equipment)=>Unit = NormalItemDrop(obj, zone, service)

        def Execute(resolver : ActorRef) : Unit = {
          localFunc(localItem)
          resolver ! scala.util.Success(this)
        }
      }, List(GUIDTask.RegisterEquipment(item)(zone.GUID))
    )
  }

  /**
    * After a weapon has finished shooting, determine if it needs to be sorted in a special way.
    * @param tool a weapon
    */
  def FireCycleCleanup(tool : Tool) : Unit = {
    //TODO this is temporary and will be replaced by more appropriate functionality in the future.
    val tdef = tool.Definition
    if(GlobalDefinitions.isGrenade(tdef)) {
      val ammoType = tool.AmmoType
      FindEquipmentStock(player, FindToolThatUses(ammoType), 3, CountGrenades).reverse match { //do not search sidearm holsters
        case Nil =>
          log.info(s"no more $ammoType grenades")
          taskResolver ! RemoveEquipmentFromSlot(player, tool, player.Find(tool).get)

        case x :: xs => //this is similar to ReloadMessage
          val box = x.obj.asInstanceOf[Tool]
          val tailReloadValue : Int = if(xs.isEmpty) { 0 } else { xs.map(_.obj.asInstanceOf[Tool].Magazine).reduce(_ + _) }
          val sumReloadValue : Int = box.Magazine + tailReloadValue
          val actualReloadValue = (if(sumReloadValue <= 3) {
            taskResolver ! RemoveEquipmentFromSlot(player, x.obj, x.start)
            sumReloadValue
          }
          else {
            ModifyAmmunition(player)(box.AmmoSlot.Box, 3 - tailReloadValue)
            3
          })
          log.info(s"found $actualReloadValue more $ammoType grenades to throw")
          ModifyAmmunition(player)(tool.AmmoSlot.Box, -actualReloadValue) //grenade item already in holster (negative because empty)
          xs.foreach(item => {
            taskResolver ! RemoveEquipmentFromSlot(player, item.obj, item.start)
          })
      }
    }
    else if(tdef == GlobalDefinitions.phoenix) {
      taskResolver ! RemoveEquipmentFromSlot(player, tool, player.Find(tool).get)
    }
  }

  /**
    * A predicate used to determine if an `InventoryItem` object contains `Equipment` that should be dropped.
    * Used to filter through lists of object data before it is placed into a player's inventory.
    * Drop the item if:<br>
    * - the item is cavern equipment<br>
    * - the item is a `BoomerTrigger` type object<br>
    * - the item is a `router_telepad` type object<br>
    * - the item is another faction's exclusive equipment
    * @param tplayer the player
    * @return true if the item is to be dropped; false, otherwise
    */
  def DropPredicate(tplayer : Player) : (InventoryItem => Boolean) = entry => {
    val objDef = entry.obj.Definition
    val faction = GlobalDefinitions.isFactionEquipment(objDef)
    GlobalDefinitions.isCavernEquipment(objDef) ||
      objDef == GlobalDefinitions.router_telepad ||
      entry.obj.isInstanceOf[BoomerTrigger] ||
      (faction != tplayer.Faction && faction != PlanetSideEmpire.NEUTRAL)
  }

  /**
    * Given an object globally unique identifier, search in a given location for it.
    * @param object_guid the object
    * @param parent a `Container` object wherein to search
    * @return an optional tuple that contains two values;
    *         the first value is the container that matched correctly with the object's GUID;
    *         the second value is the slot position of the object
    */
  def FindInLocalContainer(object_guid : PlanetSideGUID)(parent : PlanetSideGameObject with Container) : Option[(PlanetSideGameObject with Container, Option[Int])] = {
    val slot : Option[Int] = parent.Find(object_guid)
    slot match {
      case place @ Some(_) =>
        Some(parent, slot)
      case None =>
        None
    }
  }

  /**
    * Perform specific operations depending on the target of deployment.
    * @param obj the object that has had its deployment state changed
    */
  def DeploymentActivities(obj : Deployment.DeploymentObject) : Unit = {
    DeploymentActivities(obj, obj.DeploymentState)
  }

  /**
    * Perform specific operations depending on the target of deployment.
    * @param obj the object that has had its deployment state changed
    * @param state the new deployment state
    */
  def DeploymentActivities(obj : Deployment.DeploymentObject, state : DriveState.Value) : Unit = {
    obj match {
      case vehicle : Vehicle =>
        ReloadVehicleAccessPermissions(vehicle) //TODO we should not have to do this imho
        //ams
        if(vehicle.Definition == GlobalDefinitions.ams) {
          state match {
            case DriveState.Deployed =>
              vehicleService ! VehicleServiceMessage.AMSDeploymentChange(continent)
              sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 81, 1))
            case DriveState.Undeploying =>
              vehicleService ! VehicleServiceMessage.AMSDeploymentChange(continent)
              sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 81, 0))
            case DriveState.Mobile | DriveState.State7 =>
            case _ => ;
          }
        }
        //ant
        else if(vehicle.Definition == GlobalDefinitions.ant) {
          state match {
            case DriveState.Deployed =>
              // We only want this WSA (not other player's WSA) to manage timers
              if(vehicle.Seat(0).get.Occupant.contains(player)){
                // Start ntu regeneration
                // If vehicle sends UseItemMessage with silo as target NTU regeneration will be disabled and orb particles will be disabled
                antChargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuCharging(player, vehicle))
              }
            case DriveState.Undeploying =>
              // We only want this WSA (not other player's WSA) to manage timers
              if(vehicle.Seat(0).get.Occupant.contains(player)){
                antChargingTick.cancel() // Stop charging NTU if charging
              }

              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 0L)) // panel glow off
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 49, 0L)) // orb particles off
            case DriveState.Mobile | DriveState.State7 | DriveState.Deploying =>
            case _ => ;
          }
        }
        //router
        else if(vehicle.Definition == GlobalDefinitions.router) {
          state match {
            case DriveState.Deploying =>
              vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(util : Utility.InternalTelepad) =>
                  util.Active = true
                case _ =>
                  log.warn(s"DeploymentActivities: could not find internal telepad in router@${vehicle.GUID.guid} while $state")
              }
            case DriveState.Deployed =>
              //let the timer do all the work
              localService ! LocalServiceMessage(continent.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), vehicle, TelepadLike.AppraiseTeleportationSystem(vehicle, continent)))
            case DriveState.Undeploying =>
              //deactivate internal router before trying to reset the system
              vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(util : Utility.InternalTelepad) =>
                  //any telepads linked with internal mechanism must be deconstructed
                  continent.GUID(util.Telepad) match {
                    case Some(telepad : TelepadDeployable) =>
                      localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(telepad), continent))
                      localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(telepad, continent, Some(0 milliseconds)))
                    case Some(_) | None => ;
                  }
                  util.Active = false
                  localService ! LocalServiceMessage(continent.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), vehicle, None))
                case _ =>
                  log.warn(s"DeploymentActivities: could not find internal telepad in router@${vehicle.GUID.guid} while $state")
              }
            case _ => ;
          }
        }
      case _ => ;
    }
  }

  /**
    * Common reporting behavior when a `Deployment` object fails to properly transition between states.
    * @param obj the game object that could not
    * @param state the `DriveState` that could not be promoted
    * @param reason a string explaining why the state can not or will not change
    */
  def CanNotChangeDeployment(obj : PlanetSideServerObject with Deployment, state : DriveState.Value, reason : String) : Unit = {
    val mobileShift : String = if(obj.DeploymentState != DriveState.Mobile) {
      obj.DeploymentState = DriveState.Mobile
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Mobile, 0, false, Vector3.Zero))
      vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, obj.GUID, DriveState.Mobile, 0, false, Vector3.Zero))
      "; enforcing Mobile deployment state"
    }
    else {
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
  def initBuilding(continentNumber : Int, buildingNumber : Int, building : Building) : Unit = {
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
  def initFacility(continentNumber : Int, buildingNumber : Int, building : Building) : Unit = {
    val (
      ntuLevel,
      isHacked, empireHack, hackTimeRemaining, controllingEmpire,
      unk1, unk1x,
      generatorState, spawnTubesNormal, forceDomeActive,
      latticeBenefit, cavernBenefit,
      unk4, unk5, unk6,
      unk7, unk7x,
      boostSpawnPain, boostGeneratorPain
      ) = building.Info
    sendResponse(
      BuildingInfoUpdateMessage(
        continentNumber,
        buildingNumber,
        ntuLevel,
        isHacked, empireHack, hackTimeRemaining, controllingEmpire,
        unk1, unk1x,
        generatorState, spawnTubesNormal, forceDomeActive,
        latticeBenefit, cavernBenefit,
        unk4, unk5, unk6,
        unk7, unk7x,
        boostSpawnPain, boostGeneratorPain
      )
    )
    sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0,0, 0,0, 0,0, 0,0)))
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
  def initGate(continentNumber : Int, buildingNumber : Int, building : Building) : Unit = {
    building match {
      case wg : WarpGate =>
        sendResponse(
          BuildingInfoUpdateMessage(
            continentNumber,
            buildingNumber,
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
        sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0,0, 0,0, 0,0, 0,0)))
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
  def configZone(zone : Zone) : Unit = {
    zone.Buildings.values.foreach(building => {
      sendResponse(SetEmpireMessage(building.GUID, building.Faction))
      building.Amenities.foreach(amenity => {
        val amenityId = amenity.GUID
        sendResponse(PlanetsideAttributeMessage(amenityId, 50, 0))
        sendResponse(PlanetsideAttributeMessage(amenityId, 51, 0))

        amenity.Definition match {
          case GlobalDefinitions.resource_silo =>
            // Synchronise warning light & silo capacity
            val silo = amenity.asInstanceOf[ResourceSilo]
            sendResponse(PlanetsideAttributeMessage(amenityId, 45, silo.CapacitorDisplay))
            sendResponse(PlanetsideAttributeMessage(amenityId, 47, if(silo.LowNtuWarningOn) 1 else 0))

            if(silo.ChargeLevel == 0) {
              // temporarily disabled until warpgates can bring ANTs from sanctuary, otherwise we'd be stuck in a situation with an unpowered base and no way to get an ANT to refill it.
              //              sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(silo.Owner.asInstanceOf[Building].ModelId), 48, 1))
            }
          case _ => ;
        }

        // Synchronise hack states to clients joining the zone.
        // We'll have to fake LocalServiceResponse messages to self, otherwise it means duplicating the same hack handling code twice
        if(amenity.isInstanceOf[Hackable]) {
          val hackable = amenity.asInstanceOf[Hackable]

          if(hackable.HackedBy.isDefined) {
            amenity.Definition match {
              case GlobalDefinitions.capture_terminal =>
                self ! LocalServiceResponse("", PlanetSideGUID(0), LocalResponse.HackCaptureTerminal(amenity.GUID, 0L, 0L, false))
              case _ =>
                // Generic hackable object
                self ! LocalServiceResponse("", PlanetSideGUID(0), LocalResponse.HackObject(amenity.GUID, 1114636288L, 8L))
            }
          }
        }
      })

//      sendResponse(HackMessage(3, PlanetSideGUID(building.ModelId), PlanetSideGUID(0), 0, 3212836864L, HackState.HackCleared, 8))
    })
  }

  /**
    * The player has lost the will to live and must be killed.
    * @see `Vitality`<br>
    *       `PlayerSuicide`
    * @param tplayer the player to be killed
    */
  def Suicide(tplayer : Player) : Unit = {
    tplayer.History(PlayerSuicide(PlayerSource(tplayer)))
    KillPlayer(tplayer)
  }

  /**
    * The player has lost all his vitality and must be killed.<br>
    * <br>
    * Shift directly into a state of being dead on the client by setting health to zero points,
    * whereupon the player will perform a dramatic death animation.
    * Stamina is also set to zero points.
    * If the player was in a vehicle at the time of demise, special conditions apply and
    * the model must be manipulated so it behaves correctly.
    * Do not move or completely destroy the `Player` object as its coordinates of death will be important.<br>
    * <br>
    * A maximum revive waiting timer is started.
    * When this timer reaches zero, the avatar will attempt to spawn back on its faction-specific sanctuary continent.
    * @param tplayer the player to be killed
    */
  def KillPlayer(tplayer : Player) : Unit = {
    val player_guid = tplayer.GUID
    val pos = tplayer.Position
    val respawnTimer = 300000 //milliseconds
    tplayer.Die
    deadState = DeadState.Dead
    sendResponse(PlanetsideAttributeMessage(player_guid, 0, 0))
    sendResponse(PlanetsideAttributeMessage(player_guid, 2, 0))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 0, 0))
    sendResponse(DestroyMessage(player_guid, player_guid, PlanetSideGUID(0), pos)) //how many players get this message?
    sendResponse(AvatarDeadStateMessage(DeadState.Dead, respawnTimer, respawnTimer, pos, player.Faction, true))
    if(tplayer.VehicleSeated.nonEmpty) {
      continent.GUID(tplayer.VehicleSeated.get) match {
        case Some(obj : Vehicle) =>
          TotalDriverVehicleControl(obj)
          UnAccessContents(obj)
        case _ => ;
      }
      //make player invisible (if not, the cadaver sticks out the side in a seated position)
      sendResponse(PlanetsideAttributeMessage(player_guid, 29, 1))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 29, 1))
    }
    PlayerActionsToCancel()
    //TODO other methods of death?
    val pentry = PlayerSource(tplayer)
    (tplayer.History.find({p => p.isInstanceOf[PlayerSuicide]}) match {
      case Some(PlayerSuicide(_)) =>
        None
      case _ =>
        tplayer.LastShot match {
          case Some(shot) =>
            if(System.nanoTime - shot.hit_time < (10 seconds).toNanos) {
              Some(shot)
            }
            else {
              None //suicide
            }
          case None =>
            None //suicide
        }
    }) match {
      case Some(shot) =>
        continent.Activity ! Zone.HotSpot.Activity(pentry, shot.projectile.owner, shot.hit_pos)
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DestroyDisplay(shot.projectile.owner, pentry, shot.projectile.attribute_to))
      case None =>
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DestroyDisplay(pentry, pentry, 0))
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    reviveTimer = context.system.scheduler.scheduleOnce(respawnTimer milliseconds, cluster, Zone.Lattice.RequestSpawnPoint(Zones.SanctuaryZoneNumber(tplayer.Faction), tplayer, 7))
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
  def PlayerActionsToCancel() : Unit = {
    progressBarUpdate.cancel
    progressBarValue = None
    lastTerminalOrderFulfillment = true
    accessedContainer match {
      case Some(obj : Vehicle) =>
        if(obj.AccessingTrunk.contains(player.GUID)) {
          obj.AccessingTrunk = None
          UnAccessContents(obj)
        }
        accessedContainer = None

      case Some(_) =>
        accessedContainer = None

      case None => ;
    }
    shooting match {
      case Some(guid) =>
        sendResponse(ChangeFireStateMessage_Stop(guid))
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Stop(player.GUID, guid))
        prefire = None
        shooting = None
      case None => ;
    }
    if(flying) {
      sendResponse(ChatMsg(ChatMessageType.CMT_FLY, false, "", "off", None))
      flying = false
    }
    if(speed > 1) {
      sendResponse(ChatMsg(ChatMessageType.CMT_SPEED, false, "", "1.000", None))
      speed = 1f
    }
  }

  /**
    * A part of the process of spawning the player into the game world.
    * The function should work regardless of whether the player is alive or dead - it will make them alive.
    * It adds the `WSA`-current `Player` to the current zone and sends out the expected packets.<br>
    * <br>
    * If that player is the driver of a vehicle, it will construct the vehicle.
    * If that player is the occupant of a vehicle, it will place them inside that vehicle.
    * These two previous statements operate through similar though distinct mechanisms and actually imply different conditions.
    * The vehicle will not be created unless the player is a living driver;
    * but, the second statement will always trigger so long as the player is in a vehicle.
    * The first produces a version of the player more suitable to be "another player in the game," and not "the avatar."
    * The second would write over the product of the first to produce "the avatar."
    * The vehicle should only be constructed once as, if it created a second time, that distinction will become lost.
    * @see `BeginZoningMessage`
    * @see `CargoMountBehaviorForOthers`
    * @see `AvatarCreateInVehicle`
    * @see `GetKnownVehicleAndSeat`
    * @see `LoadZoneTransferPassengerMessages`
    * @see `Player.Spawn`
    * @see `ReloadVehicleAccessPermissions`
    * @see `TransportVehicleChannelName`
    */
  def AvatarCreate() : Unit = {
    val health = player.Health
    val armor = player.Armor
    val stamina = player.Stamina
    player.Spawn
    if(health != 0) {
      player.Health = health
      player.Armor = armor
      player.Stamina = stamina
    }
    GetKnownVehicleAndSeat() match {
      case (Some(vehicle : Vehicle), Some(seat : Int)) =>
        //vehicle and driver/passenger
        interstellarFerry = None
        val vdef = vehicle.Definition
        val data = vdef.Packet.ConstructorData(vehicle).get
        val guid = vehicle.GUID
        sendResponse(ObjectCreateMessage(vehicle.Definition.ObjectId, guid, data))
        ReloadVehicleAccessPermissions(vehicle)
        //if the vehicle is the cargo of another vehicle in this zone
        val carrierInfo = continent.GUID(vehicle.MountedIn) match {
          case Some(carrier : Vehicle) =>
            (Some(carrier), carrier.CargoHolds.find({ case (index, hold) => hold.Occupant.contains(vehicle)}))
          case _ =>
            (None, None)
        }
        player.VehicleSeated = guid
        if(seat == 0) {
          //if driver
          OwnVehicle(vehicle, player)
          vehicle.CargoHolds.values
            .collect { case hold if hold.isOccupied => hold.Occupant.get }
            .foreach { _.MountedIn = guid }
          continent.Transport ! Zone.Vehicle.Spawn(vehicle)
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.LoadVehicle(player.GUID, vehicle, vdef.ObjectId, guid, data))
          carrierInfo match {
            case (Some(carrier), Some((index, _))) =>
              CargoMountBehaviorForOthers(carrier, vehicle, index)
            case _ =>
              vehicle.MountedIn = None
          }
        }
        else {
          //if passenger;
          //meant for same-zone warping; when player changes zones, redundant information will be sent
          carrierInfo match {
            case (Some(carrier), Some((index, _))) =>
              CargoMountBehaviorForUs(carrier, vehicle, index)
            case _ => ;
          }
        }
        log.info(s"AvatarCreate (vehicle): $guid -> $data")
        //player, passenger
        AvatarCreateInVehicle(player, vehicle, seat)

      case _ =>
        player.VehicleSeated = None
        val packet = player.Definition.Packet
        val data = packet.DetailedConstructorData(player).get
        val guid = player.GUID
        sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, guid, data))
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.LoadPlayer(guid, ObjectClass.avatar, guid, packet.ConstructorData(player).get, None))
        log.info(s"AvatarCreate: $guid -> $data")
    }
    continent.Population ! Zone.Population.Spawn(avatar, player)
    //cautious redundancy
    deadState = DeadState.Alive
  }

  /**
    * If the player is seated in a vehicle, find that vehicle and get the seat index number at which the player is sat.<br>
    * <br>
    * For special purposes involved in zone transfers,
    * where the vehicle may or may not exist in either of the zones (yet),
    * the value of `interstellarFerry` is also polled.
    * Making certain this field is blanked after the transfer is completed is important
    * to avoid inspecting the wrong vehicle and failing simple vehicle checks where this function may be employed.
    * @see `interstellarFerry`
    * @return a tuple consisting of a vehicle reference and a seat index
    *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
    *         `(None, None)`, otherwise (even if the vehicle can be determined)
    */
  def GetKnownVehicleAndSeat() : (Option[Vehicle], Option[Int]) =
    interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
      case Some(vehicle : Vehicle) =>
        vehicle.PassengerInSeat(player) match {
          case index @ Some(_) =>
            (Some(vehicle), index)
          case None =>
            (None, None)
        }
      case _ =>
        (None, None)
    }

  /**
    * If the player is seated in a vehicle, find that vehicle and get the seat index number at which the player is sat.
    * @return a tuple consisting of a vehicle reference and a seat index
    *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
    *         `(None, None)`, otherwise (even if the vehicle can be determined)
    */
  def GetVehicleAndSeat() : (Option[Vehicle], Option[Int]) =
    continent.GUID(player.VehicleSeated) match {
      case Some(vehicle : Vehicle) =>
        vehicle.PassengerInSeat(player) match {
          case index @ Some(_) =>
            (Some(vehicle), index)
          case None =>
            (None, None)
        }
      case _ =>
        (None, None)
    }

  /**
    * Create an avatar character as if that avatar's player is mounted in a vehicle object's seat.<br>
    * <br>
    * This is a very specific configuration of the player character that is not visited very often.
    * The value of `player.VehicleSeated` should be set to accommodate `Packet.DetailedConstructorData` and,
    * though not explicitly checked,
    * should be the same as the globally unique identifier that is assigned to the `vehicle` parameter for the current zone.
    * The priority of this function is consider "initial" so it introduces the avatar to the game world in this state
    * and is permitted to introduce the avatar to the vehicle's internal settings in a similar way.
    * @see `AccessContents`
    * @see `UpdateWeaponAtSeatPosition`
    * @param tplayer the player avatar seated in the vehicle's seat
    * @param vehicle the vehicle the player is driving
    * @param seat the seat index
    */
  def AvatarCreateInVehicle(tplayer : Player, vehicle : Vehicle, seat : Int) : Unit = {
    val pdef = tplayer.Definition
    val guid = player.GUID
    val parent = ObjectCreateMessageParent(vehicle.GUID, seat)
    val data = pdef.Packet.DetailedConstructorData(player).get
    sendResponse(
      ObjectCreateDetailedMessage(
        pdef.ObjectId,
        guid,
        parent,
        data
      )
    )
    avatarService ! AvatarServiceMessage(vehicle.Continent, AvatarAction.LoadPlayer(guid, pdef.ObjectId, guid, pdef.Packet.ConstructorData(player).get, Some(parent)))
    AccessContents(vehicle)
    UpdateWeaponAtSeatPosition(vehicle, seat)
    log.info(s"AvatarCreateInVehicle: $guid -> $data")
  }

  /**
    * Produce a clone of the player that is equipped with the default infantry loadout.
    * The loadout is hardcoded.
    * The player is expected to be in a Standard Exo-Suit.
    * @param tplayer the original player
    * @return the duplication of the player, in Standard Exo-Suit and with default equipment loadout
    */
  def RespawnClone(tplayer : Player) : Player = {
    val faction = tplayer.Faction
    val obj = Player.Respawn(tplayer)
    obj.Slot(0).Equipment = Tool(StandardPistol(faction))
    obj.Slot(2).Equipment = Tool(suppressor)
    obj.Slot(4).Equipment = Tool(StandardMelee(faction))
    obj.Slot(6).Equipment = AmmoBox(bullet_9mm)
    obj.Slot(9).Equipment = AmmoBox(bullet_9mm)
    obj.Slot(12).Equipment = AmmoBox(bullet_9mm)
    obj.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
    obj.Slot(36).Equipment = AmmoBox(StandardPistolAmmo(faction))
    obj.Slot(39).Equipment = SimpleItem(remote_electronics_kit)
    obj.Inventory.Items.foreach { _.obj.Faction = faction }
    obj
  }

  /**
    * Remove items from a deceased player that are not expected to be found on a corpse.
    * Most all players have their melee slot knife (which can not be un-equipped normally) removed.
    * MAX's have their primary weapon in the designated slot removed.
    * @param obj the player to be turned into a corpse
    */
  def FriskCorpse(obj : Player) : Unit = {
    if(obj.isBackpack) {
      obj.Slot(4).Equipment match {
        case None => ;
        case Some(knife) =>
          obj.Slot(4).Equipment = None
          taskResolver ! RemoveEquipmentFromSlot(obj, knife, 4)
      }
      obj.Slot(0).Equipment match {
        case Some(arms : Tool) =>
          if(GlobalDefinitions.isMaxArms(arms.Definition)) {
            obj.Slot(0).Equipment = None
            taskResolver ! RemoveEquipmentFromSlot(obj, arms, 0)
          }
        case _ => ;
      }
      //disown boomers and drop triggers
      val boomers = avatar.Deployables.ClearDeployable(DeployedItem.boomer)
      boomers.foreach(boomer => {
        continent.GUID(boomer) match {
          case Some(obj : BoomerDeployable) =>
            obj.OwnerName = None
            localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent))
          case Some(_) | None => ;
        }
      })
      val triggers = RemoveBoomerTriggersFromInventory()
      triggers.foreach(trigger => { NormalItemDrop(obj, continent, avatarService)(trigger) })
    }
  }

  /**
    * Creates a player that has the characteristics of a corpse.
    * To the game, that is a backpack (or some pastry, festive graphical modification allowing).
    * @see `CorpseConverter.converter`
    * @param tplayer the player
    */
  def TurnPlayerIntoCorpse(tplayer : Player) : Unit = {
    val guid = tplayer.GUID
    sendResponse(
      ObjectCreateDetailedMessage(ObjectClass.avatar, guid, CorpseConverter.converter.DetailedConstructorData(tplayer).get)
    )
  }

  /**
    * If the corpse has been well-looted, it has no items in its primary holsters nor any items in its inventory.
    * @param obj the corpse
    * @return `true`, if the `obj` is actually a corpse and has no objects in its holsters or backpack;
    *        `false`, otherwise
    */
  def WellLootedCorpse(obj : Player) : Boolean = {
    obj.isBackpack && obj.Holsters().count(_.Equipment.nonEmpty) == 0 && obj.Inventory.Size == 0
  }

  /**
    * If the corpse has been well-looted, remove it from the ground.
    * @param obj the corpse
    * @return `true`, if the `obj` is actually a corpse and has no objects in its holsters or backpack;
    *        `false`, otherwise
    */
  def TryDisposeOfLootedCorpse(obj : Player) : Boolean = {
    if(WellLootedCorpse(obj)) {
      avatarService ! AvatarServiceMessage.Corpse(RemoverActor.HurrySpecific(List(obj), continent))
      true
    }
    else {
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
  def RequestSanctuaryZoneSpawn(tplayer : Player, currentZone : Int) : Unit = {
    val sanctNumber = Zones.SanctuaryZoneNumber(tplayer.Faction)
    if(currentZone == sanctNumber) {
      if(!player.isAlive) {
        sendResponse(DisconnectMessage("Player failed to load on faction's sanctuary continent.  Please relog."))
      }
      //we are already on sanctuary, alive; what more is there to do?
    }
    else {
      continent.GUID(player.VehicleSeated) match {
        case Some(obj : Vehicle) if obj.Health > 0 =>
          cluster ! Zone.Lattice.RequestSpawnPoint(sanctNumber, tplayer, 12) //warp gates for functioning vehicles
        case None =>
          cluster ! Zone.Lattice.RequestSpawnPoint(sanctNumber, tplayer, 7) //player character spawns
        case _ =>
      }
    }
  }

  /**
    * na
    * @param terminal na
    */
  def HandleProximityTerminalUse(terminal : Terminal with ProximityUnit) : Unit = {
    val term_guid = terminal.GUID
    val targets = FindProximityUnitTargetsInScope(terminal)
    val currentTargets = terminal.Targets
    targets.foreach(target => {
      if(!currentTargets.contains(target)) {
        StartUsingProximityUnit(terminal, target)
      }
      else if(targets.isEmpty) {
        log.warn(s"HandleProximityTerminalUse: ${player.Name} could not find valid targets to give to proximity unit ${terminal.Definition.Name}@${term_guid.guid}")
      }
    })
  }

  /**
    * na
    * @param terminal na
    * @return na
    */
  def FindProximityUnitTargetsInScope(terminal : Terminal with ProximityUnit) : Seq[PlanetSideGameObject] = {
    terminal.Definition.asInstanceOf[ProximityDefinition].TargetValidation.keySet collect {
      case ProximityTarget.Player => Some(player)
      case ProximityTarget.Vehicle | ProximityTarget.Aircraft => continent.GUID(player.VehicleSeated)
    } collect {
      case Some(a) => a
    } toSeq
  }

  /**
    * Queue a proximity-base service.
    * @param terminal the proximity-based unit
    * @param target the entity that is being considered for terminal operation
    */
  def StartUsingProximityUnit(terminal : Terminal with ProximityUnit, target : PlanetSideGameObject) : Unit = {
    val term_guid = terminal.GUID
    if(player.isAlive) {
      log.info(s"StartUsingProximityUnit: ${player.Name} wants to use ${terminal.Definition.Name}@${term_guid.guid} on $target")
      target match {
        case _ : Player =>
          terminal.Actor ! CommonMessages.Use(player, Some(target))
        case _ : Vehicle =>
          terminal.Actor ! CommonMessages.Use(player, Some((target, vehicleService)))
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
  def SelectProximityUnitBehavior(terminal : Terminal with ProximityUnit, target : PlanetSideGameObject) : Unit = {
    target match {
      case o : Player =>
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
  def StopUsingProximityUnit(terminal : Terminal with ProximityUnit) : Unit = {
    val term_guid = terminal.GUID
    log.info(s"StopUsingProximityUnit: attempting to stop using proximity unit ${terminal.Definition.Name}@${term_guid.guid}")
    val targets = FindProximityUnitTargetsInScope(terminal)
    if(targets.nonEmpty) {
      if(usingMedicalTerminal.contains(term_guid)) {
        usingMedicalTerminal = None
      }
      targets.foreach(target =>
        terminal.Actor ! CommonMessages.Unuse(player, Some(target))
      )
    }
    else {
      log.warn(s"StopUsingProximityUnit: ${player.Name} could not find valid targets for proximity unit ${terminal.Definition.Name}@${term_guid.guid}")
    }
  }

  /**
    * na
    */
  def ForgetAllProximityTerminals(term_guid : PlanetSideGUID) : Unit = {
    if(usingMedicalTerminal.contains(term_guid)) {
      usingMedicalTerminal = None
    }
  }

  /**
    * Cease all current interactions with proximity-based units.
    * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
    * This operations may invoke callback messages.
    * @see `postStop`
    */
  def CancelAllProximityUnits() : Unit = {
    continent.GUID(usingMedicalTerminal) match {
      case Some(terminal : Terminal with ProximityUnit) =>
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
  def HealthAndArmorTerminal(unit : Terminal with ProximityUnit, target : Player) : Unit = {
    val medDef = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val healAmount = medDef.HealAmount
    val healthFull : Boolean = if(healAmount != 0 && target.Health < target.MaxHealth) {
      target.History(HealFromTerm(PlayerSource(target), healAmount, 0, medDef))
      HealAction(target, healAmount)
    }
    else {
      true
    }
    val repairAmount = medDef.ArmorAmount
    val armorFull : Boolean = if(repairAmount != 0 && target.Armor < target.MaxArmor) {
      target.History(HealFromTerm(PlayerSource(target), 0, repairAmount, medDef))
      ArmorRepairAction(target, repairAmount)
    }
    else {
      true
    }
    if(healthFull && armorFull) {
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
  def HealAction(tplayer : Player, healValue : Int = 10) : Boolean = {
    val player_guid = tplayer.GUID
    tplayer.Health = tplayer.Health + healValue
    sendResponse(PlanetsideAttributeMessage(player_guid, 0, tplayer.Health))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 0, tplayer.Health))
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
  def ArmorRepairAction(tplayer : Player, repairValue : Int = 10) : Boolean = {
    val player_guid = tplayer.GUID
    tplayer.Armor = tplayer.Armor + repairValue
    sendResponse(PlanetsideAttributeMessage(player_guid, 4, tplayer.Armor))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 4, tplayer.Armor))
    tplayer.Armor == tplayer.MaxArmor
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to auto reverse them out
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to move in reverse
    */
  def ServerVehicleLockReverse() : Unit = {
    controlled = Some(0)
    sendResponse(ServerVehicleOverrideMsg(lock_accelerator = true, lock_wheel = true, reverse = true, unk4 = true, lock_vthrust = 0, lock_strafe = 1, movement_speed = 2, unk8 = Some(0)))
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to strafe right out of the cargo hold for vehicles that are mounted sideways e.g. router/BFR
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to strafe right
    */
  def ServerVehicleLockStrafeRight() : Unit = {
    controlled = Some(0)
    sendResponse(ServerVehicleOverrideMsg(lock_accelerator = true, lock_wheel = true, reverse = false, unk4 = true, lock_vthrust = 0, lock_strafe = 3, movement_speed = 0, unk8 = Some(0)))
  }

  /**
    * This function is applied to vehicles that are leaving a cargo vehicle's cargo hold to strafe left out of the cargo hold for vehicles that are mounted sideways e.g. router/BFR
    * Lock all applicable controls of the current vehicle
    * Set the vehicle to strafe left
    */
  def ServerVehicleLockStrafeLeft() : Unit = {
    controlled = Some(0)
    sendResponse(ServerVehicleOverrideMsg(lock_accelerator = true, lock_wheel = true, reverse = false, unk4 = true, lock_vthrust = 0, lock_strafe = 2, movement_speed = 0, unk8 = Some(0)))
  }

  /**
    * Lock all applicable controls of the current vehicle.
    * This includes forward motion, turning, and, if applicable, strafing.
    * @param vehicle the vehicle being controlled
    */
  def ServerVehicleLock(vehicle : Vehicle) : Unit = {
    controlled = Some(0)
    sendResponse(ServerVehicleOverrideMsg(true, true, false, false, 0, 1, 0, Some(0)))
  }

  /**
    * Place the current vehicle under the control of the server's commands.
    * @param vehicle the vehicle
    * @param speed how fast the vehicle is moving forward
    * @param flight whether the vehicle is ascending or not, if the vehicle is an applicable type
    */
  def ServerVehicleOverride(vehicle : Vehicle, speed : Int = 0, flight : Int = 0) : Unit = {
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
  def DriverVehicleControl(vehicle : Vehicle, speed : Int = 0, flight : Int = 0) : Unit = {
    if(controlled.nonEmpty) {
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
  def TotalDriverVehicleControl(vehicle : Vehicle) : Unit = {
    if(controlled.nonEmpty) {
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
  def FindProjectileEntry(projectile_guid : PlanetSideGUID) : Option[Projectile] = {
    val index = projectile_guid.guid - Projectile.BaseUID
    if(0 <= index && index < projectiles.length) {
      projectiles(index)
    }
    else {
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
  def ResolveProjectileEntry(projectile_guid : PlanetSideGUID, resolution : ProjectileResolution.Value, target : PlanetSideGameObject with FactionAffinity with Vitality, pos : Vector3) : Option[ResolvedProjectile] = {
    FindProjectileEntry(projectile_guid) match {
      case Some(projectile) =>
        val index =  projectile_guid.guid - Projectile.BaseUID
        ResolveProjectileEntry(projectile, index, resolution, target, pos)
      case None =>
        log.warn(s"ResolveProjectile: expected projectile, but ${projectile_guid.guid} not found")
        None
    }
  }

  /**
    * Find a projectile with the given globally unique identifier and mark it as a resolved shot.
    * A `Resolved` shot has either encountered an obstacle or is being cleaned up for not finding an obstacle.
    * The internal copy of the projectile is retained as merely `Resolved`
    * while the observed projectile is promoted to the suggested resolution status.
    * @param projectile the projectile object
    * @param index where the projectile was found
    * @param resolution the resolution status to promote the projectile
    * @return a copy of the projectile
    */
  def ResolveProjectileEntry(projectile : Projectile, index : Int, resolution : ProjectileResolution.Value, target : PlanetSideGameObject with FactionAffinity with Vitality, pos : Vector3) : Option[ResolvedProjectile] = {
    if(!projectiles(index).contains(projectile)) {
      log.error(s"expected projectile could not be found at $index; can not resolve")
      None
    }
    else if(projectile.isMiss) {
      log.error(s"expected projectile at $index was already counted as a missed shot; can not resolve any further")
      None
    }
    else {
      projectile.Resolve()
      Some(ResolvedProjectile(resolution, projectile, SourceEntry(target), target.DamageModel, pos))
    }
  }

  /**
    * Common activities/procedure when a player mounts a valid object.
    * @param tplayer the player
    * @param obj the mountable object
    * @param seatNum the seat into which the player is mounting
    */
  def MountingAction(tplayer : Player, obj : PlanetSideGameObject with Mountable, seatNum : Int) : Unit = {
    val player_guid : PlanetSideGUID = tplayer.GUID
    val obj_guid : PlanetSideGUID = obj.GUID
    PlayerActionsToCancel()
    log.info(s"MountVehicleMsg: $player_guid mounts $obj @ $seatNum")
    sendResponse(ObjectAttachMessage(obj_guid, player_guid, seatNum))
    vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seatNum))
  }

  /**
    * Common activities/procedure when a player dismounts a valid object.
    * @param tplayer the player
    * @param obj the mountable object
    * @param seatNum the seat out of which which the player is disembarking
    */
  def DismountAction(tplayer : Player, obj : PlanetSideGameObject with Mountable, seatNum : Int) : Unit = {
    val player_guid : PlanetSideGUID = tplayer.GUID
    log.info(s"DismountVehicleMsg: ${tplayer.Name} dismounts $obj from $seatNum")
    sendResponse(DismountVehicleMsg(player_guid, BailType.Normal, false))
    vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, BailType.Normal, false))
  }

  /**
    * Calculate the amount of damage to be dealt to an active `target`
    * using the information reconstructed from a `Resolvedprojectile`
    * and affect the `target` in a synchronized manner.
    * The active `target` and the target of the `ResolvedProjectile` do not have be the same.
    * @see `DamageResistanceModel`<br>
    *       `Vitality`
    * @param target a valid game object that is known to the server
    * @param data a projectile that will affect the target
    */
  def HandleDealingDamage(target : PlanetSideGameObject with Vitality, data : ResolvedProjectile) : Unit = {
    val func = data.damage_model.Calculate(data)
    target match {
      case obj : Player =>
        //damage is synchronized on the target player's `WSA` (results distributed from there)
        avatarService ! AvatarServiceMessage(obj.Name, AvatarAction.Damage(player.GUID, obj, func))
      case obj : Vehicle =>
        //damage is synchronized on the vehicle actor (results returned to and distributed from this `WSA`)
        obj.Actor ! Vitality.Damage(func)
      case obj : Deployable =>
        //damage is synchronized on `LSA` (results returned to and distributed from this `WSA`)
        localService ! Vitality.DamageOn(obj, func)
      case obj : FacilityTurret =>
        //damage is synchronized on the turret actor (results returned to and distributed from this `WSA`)
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
  def DestroyDisplayMessage(killer : SourceEntry, victim : SourceEntry, method : Int, unk : Int = 121) : DestroyDisplayMessage = {
    val killer_seated = killer match {
      case obj : PlayerSource => obj.Seated
      case _ => false
    }
    val victim_seated = victim match {
      case obj : PlayerSource => obj.Seated
      case _ => false
    }
    new DestroyDisplayMessage(
      killer.Name, killer.CharId, killer.Faction, killer_seated,
      unk, method,
      victim.Name, victim.CharId, victim.Faction, victim_seated
    )
  }

  /**
    * Initialize the deployables backend information.
    * @param avatar the player's core
    */
  def InitializeDeployableQuantities(avatar : Avatar) : Unit = {
    log.info("Setting up combat engineering ...")
    avatar.Deployables.Initialize(avatar.Certifications.toSet)
  }

  /**
    * Initialize the UI elements for deployables.
    * @param avatar the player's core
    */
  def InitializeDeployableUIElements(avatar : Avatar) : Unit = {
    log.info("Setting up combat engineering UI ...")
    UpdateDeployableUIElements(avatar.Deployables.UpdateUI())
  }

  /**
    * The player learned a new certification.
    * Update the deployables user interface elements if it was an "Engineering" certification.
    * The certification "Advanced Hacking" also relates to an element.
    * @param certification the certification that was added
    * @param certificationSet all applicable certifications
    */
  def AddToDeployableQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    avatar.Deployables.AddToDeployableQuantities(certification, certificationSet)
    UpdateDeployableUIElements(avatar.Deployables.UpdateUI(certification))
  }

  /**
    * The player forgot a certification he previously knew.
    * Update the deployables user interface elements if it was an "Engineering" certification.
    * The certification "Advanced Hacking" also relates to an element.
    * @param certification the certification that was added
    * @param certificationSet all applicable certifications
    */
  def RemoveFromDeployablesQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    avatar.Deployables.RemoveFromDeployableQuantities(certification, certificationSet)
    UpdateDeployableUIElements(avatar.Deployables.UpdateUI(certification))
  }

  /**
    * Initialize the deployables user interface elements.<br>
    * <br>
    * All element initializations require both the maximum deployable amount and the current deployables active counts.
    * Until initialized, all elements will be RED 0/0 as if the cooresponding certification were not `learn`ed.
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
  def UpdateDeployableUIElements(list : List[(Int,Int,Int,Int)]) : Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach({ case((currElem, curr, maxElem, max)) =>
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
    * @see `SetCurrentAvatar`<br>
    *       `DontRedrawIcons`
    * @param obj a `Deployable` object
    */
  def RedrawDeployableIcons(obj : PlanetSideGameObject with Deployable) : Unit = {
    val deployInfo = DeployableInfo(obj.GUID, Deployable.Icon(obj.Definition.Item), obj.Position, obj.Owner.get)
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
    * @see `SetCurrentAvatar`<br>
    *       `RedrawDeployableIcons`
    * @param obj a `Deployable` object
    */
  def DontRedrawIcons(obj : PlanetSideGameObject with Deployable) : Unit = { }

  /**
    * The custom behavior responding to the message `ChangeFireModeMessage` for `ConstructionItem` game objects.
    * Each fire mode has sub-modes corresponding to a type of "deployable" as ammunition
    * and each of these sub-modes have certification requirements that must be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given mode and given sub-mode are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @see `PerformConstructionItemAmmoChange`<br>
    *       `FireModeSwitch.NextFireMode`
    * @param obj the `ConstructionItem` object
    * @param originalModeIndex the starting point fire mode index
    * @return the changed fire mode
    */
  def NextConstructionItemFireMode(obj : ConstructionItem, originalModeIndex : Int) : ConstructionFireMode = {
    val certifications = player.Certifications
    do {
      obj.NextFireMode
      if(!ConstructionItemPermissionComparison(certifications, obj.ModePermissions)) {
        PerformConstructionItemAmmoChange(obj, obj.AmmoTypeIndex)
      }
      sendResponse(ChangeFireModeMessage(obj.GUID, obj.FireModeIndex))
    }
    while(!ConstructionItemPermissionComparison(certifications, obj.ModePermissions) && originalModeIndex != obj.FireModeIndex)
    obj.FireMode
  }

  /**
    * The custom behavior responding to the message `ChangeAmmoMessage` for `ConstructionItem` game objects.
    * Iterate through sub-modes corresponding to a type of "deployable" as ammunition for this fire mode
    * and check each of these sub-modes for their certification requirements to be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given ammunition are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @param obj the `ConstructionItem` object
    * @param originalModeIndex the starting point ammunition type mode index
    */
  def PerformConstructionItemAmmoChange(obj : ConstructionItem, originalAmmoIndex : Int) : Unit = {
    val certifications = player.Certifications
    do {
      obj.NextAmmoType
    }
    while(!ConstructionItemPermissionComparison(certifications, obj.ModePermissions) && originalAmmoIndex != obj.AmmoTypeIndex)
    log.info(s"ChangeFireMode: construction object ${obj.Definition.Name} changed to ${obj.AmmoType} (mode ${obj.FireModeIndex})")
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
  def ConstructionItemPermissionComparison(sample : Set[CertificationType.Value], test : Set[CertificationType.Value]) : Boolean = {
    import CertificationType._
    val engineeringCerts : Set[CertificationType.Value] = Set(AssaultEngineering, FortificationEngineering)
    val testDiff : Set[CertificationType.Value] = test diff (engineeringCerts ++ Set(AdvancedEngineering))
    //substitute `AssaultEngineering` and `FortificationEngineering` for `AdvancedEngineering`
    val sampleIntersect = if(sample contains AdvancedEngineering) {
      engineeringCerts
    }
    else {
      sample intersect engineeringCerts
    }
    val testIntersect = if(test contains AdvancedEngineering) {
      engineeringCerts
    }
    else {
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
  def DeployableBuildActivity(obj : PlanetSideGameObject with Deployable) : Unit = {
    val guid = obj.GUID
    val definition = obj.Definition
    val item = definition.Item
    val deployables = avatar.Deployables
    val (curr, max) = deployables.CountDeployable(item)
    log.info(s"DeployableBuildActivity: ${definition.Name}")
    //two potential messages related to numerical limitations of deployables
    if(!avatar.Deployables.Available(obj)) {
      val (removed, msg) = {
        if(curr == max) { //too many of a specific type of deployable
          (deployables.DisplaceFirst(obj), max > 1)
        }
        else { //make room by eliminating a different type of deployable
          (deployables.DisplaceFirst(obj, { d => d.Definition.Item != item }), true)
        }
      }
      removed match {
        case Some(telepad : TelepadDeployable) =>
          telepad.AssignOwnership(None)
          localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(telepad), continent))
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(telepad, continent, Some(0 seconds))) //normal decay
        case Some(old) =>
          old.AssignOwnership(None)
          localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(old), continent))
          localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(old, continent, Some(0 seconds)))
          if(msg) { //max test
            sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}OldestDestroyed", None))
          }
        case None => ; //should be an invalid case
          log.warn(s"DeployableBuildActivity: how awkward: we probably shouldn't be allowed to build this deployable right now")
      }
    }
    else if(obj.isInstanceOf[TelepadDeployable]) {
      //always treat the telepad we are putting down as the first and only one
      sendResponse(ObjectDeployedMessage.Success(definition.Name, 1, 1))
    }
    else {
      sendResponse(ObjectDeployedMessage.Success(definition.Name, curr + 1, max))
      val (catCurr, catMax) = deployables.CountCategory(item)
      if((max > 1 && curr + 1 == max) || (catMax > 1 && catCurr + 1 == catMax)) {
        sendResponse(ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}LimitReached", None))
      }
    }
    avatar.Deployables.Add(obj)
    UpdateDeployableUIElements(avatar.Deployables.UpdateUIElement(item))
    sendResponse(GenericObjectActionMessage(guid, 84)) //reset build cooldown
    sendResponse(ObjectCreateMessage(definition.ObjectId, guid, definition.Packet.ConstructorData(obj).get))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DeployItem(player.GUID, obj))
    //map icon
    val deployInfo = DeployableInfo(guid, Deployable.Icon(item), obj.Position, obj.Owner.getOrElse(PlanetSideGUID(0)))
    sendResponse(DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
    localService ! LocalServiceMessage(s"${continent.Id}/${player.Faction}", LocalAction.DeployableMapIcon(player.GUID, DeploymentAction.Build, deployInfo))
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
  def TryDropConstructionTool(tool : ConstructionItem, index : Int, pos : Vector3) : Unit = {
    if(tool.Definition == GlobalDefinitions.advanced_ace &&
      SafelyRemoveConstructionItemFromSlot(tool, index, "TryDropConstructionTool")) {
      continent.Ground ! Zone.Ground.DropItem(tool, pos, Vector3.Zero)
    }
  }

  /**
    * Destroy a `ConstructionItem` object that can be found in the indexed slot.
    * @see `Player.Find`
    * @param tool the `ConstructionItem` object currently in the slot (checked)
    * @param index the slot index
    */
  def CommonDestroyConstructionItem(tool : ConstructionItem, index : Int) : Unit = {
    if(SafelyRemoveConstructionItemFromSlot(tool, index, "CommonDestroyConstructionItem")) {
      taskResolver ! GUIDTask.UnregisterEquipment(tool)(continent.GUID)
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
  def SafelyRemoveConstructionItemFromSlot(tool : ConstructionItem, index : Int, logDecorator : String = "SafelyRemoveConstructionItemFromSlot") : Boolean = {
    if({
      val holster = player.Slot(index)
      if(holster.Equipment.contains(tool)) {
        holster.Equipment = None
        true
      }
      else {
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
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, tool.GUID))
      true
    }
    else {
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
  def FindReplacementConstructionItem(tool : ConstructionItem, index : Int) : Unit = {
    val fireMode = tool.FireModeIndex
    val ammoType = tool.AmmoTypeIndex
    val definition = tool.Definition

    if(player.Slot(index).Equipment.isEmpty) {
      FindEquipmentStock(player, { (e) => e.Definition == definition }, 1) match {
        case x :: _ =>
          val guid = player.GUID
          val obj = x.obj.asInstanceOf[ConstructionItem]
          if((player.Slot(index).Equipment = obj).contains(obj)) {
            player.Inventory -= x.start
            sendResponse(ObjectAttachMessage(guid, obj.GUID, index))

            if(obj.FireModeIndex != fireMode) {
              obj.FireModeIndex = fireMode
              sendResponse(ChangeFireModeMessage(obj.GUID, fireMode))
            }
            if(obj.AmmoTypeIndex != ammoType) {
              obj.AmmoTypeIndex = ammoType
              sendResponse(ChangeAmmoMessage(obj.GUID, ammoType))
            }
            if(player.VisibleSlots.contains(index)) {
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(guid, guid, index, obj))
              if(player.DrawnSlot == Player.HandsDownSlot) {
                player.DrawnSlot = index
                sendResponse(ObjectHeldMessage(guid, index, false))
                avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectHeld(guid, index))
              }
            }
          }
        case Nil => ; //no replacements found
      }
    }
    else {
      log.warn(s"FindReplacementConstructionItem: slot $index needs to be empty before a replacement ${definition.Name} can be installed")
    }
  }

  /**
    * A simple object searching algorithm that is limited to containers currently known and accessible by the player.
    * If all relatively local containers are checked and the object is not found,
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
  def FindEquipmentToDelete(object_guid : PlanetSideGUID, obj : Equipment) : Boolean = {
    val findFunc : PlanetSideGameObject with Container => Option[(PlanetSideGameObject with Container, Option[Int])] =
      FindInLocalContainer(object_guid)

    findFunc(player.Locker)
      .orElse(findFunc(player))
      .orElse(accessedContainer match {
        case Some(parent) =>
          findFunc(parent)
        case None =>
          None
      })
      .orElse(FindLocalVehicle match {
        case Some(parent) =>
          findFunc(parent)
        case None =>
          None
      })
    match {
      case Some((parent, Some(slot))) =>
        obj.Position = Vector3.Zero
        taskResolver ! RemoveEquipmentFromSlot(parent, obj, slot)
        log.info(s"RequestDestroy: equipment $obj")
        true

      case _ =>
        if(continent.EquipmentOnGround.contains(obj)) {
          obj.Position = Vector3.Zero
          continent.Ground ! Zone.Ground.RemoveItem(object_guid)
          avatarService ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(PlanetSideGUID(0), object_guid))
          log.info(s"RequestDestroy: equipment $obj on ground")
          true
        }
        else {
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
  def DeconstructDeployable(obj : PlanetSideGameObject with Deployable, guid : PlanetSideGUID, pos : Vector3) : Unit = {
    StartBundlingPackets()
    sendResponse(SetEmpireMessage(guid, PlanetSideEmpire.NEUTRAL)) //for some, removes the green marker circle
    sendResponse(ObjectDeleteMessage(guid, 0))
    if(player.Faction == obj.Faction) {
      sendResponse(
        DeployableObjectsInfoMessage(
          DeploymentAction.Dismiss,
          DeployableInfo(guid, Deployable.Icon(obj.Definition.Item), pos, obj.Owner.getOrElse(PlanetSideGUID(0)))
        )
      )
    }
    StopBundlingPackets()
  }

  /**
    * Common behavior for deconstructing deployables in the game environment.
    * @param obj the deployable
    * @param guid the globally unique identifier for the deployable
    * @param pos the previous position of the deployable
    * @param orient the previous orientation of the deployable
    * @param deletionType the value passed to `ObjectDeleteMessage` concerning the deconstruction animation
    */
  def DeconstructDeployable(obj : PlanetSideGameObject with Deployable, guid : PlanetSideGUID, pos : Vector3, orient : Vector3, deletionType : Int) : Unit = {
    StartBundlingPackets()
    sendResponse(SetEmpireMessage(guid, PlanetSideEmpire.NEUTRAL)) //for some, removes the green marker circle
    sendResponse(TriggerEffectMessage("spawn_object_failed_effect", pos, orient))
    sendResponse(PlanetsideAttributeMessage(guid, 29, 1)) //make deployable vanish
    sendResponse(ObjectDeleteMessage(guid, deletionType))
    if(player.Faction == obj.Faction) {
      sendResponse(
        DeployableObjectsInfoMessage(
          DeploymentAction.Dismiss,
          DeployableInfo(guid, Deployable.Icon(obj.Definition.Item), pos, obj.Owner.getOrElse(PlanetSideGUID(0)))
        )
      )
    }
    StopBundlingPackets()
  }

  /**
    * Distribute information that a deployable has been destroyed.
    * The deployable may not have yet been eliminated from the game world (client or server),
    * but its health is zero and it has entered the conditions where it is nearly irrelevant.<br>
    * <br>
    * The typical use case of this function involves destruction via weapon fire, attributed to a particular player.
    * Contrast this to simply destroying a deployable by being the deployable's owner and using the map icon controls.
    * This function eventually invokes the same routine
    * but mainly goes into effect when the deployable has been destroyed
    * and may still leave a physical component in the game world to be cleaned up later.
    * That is the task `EliminateDeployable` performs.
    * Additionally, since the player who destroyed the deployable isn't necessarily the owner,
    * and the real owner will still be aware of the existence of the deployable,
    * that player must be informed of the loss of the deployable directly.
    * @see `DeployableRemover`
    * @see `Vitality.DamageResolution`
    * @see `LocalResponse.EliminateDeployable`
    * @see `DeconstructDeployable`
    * @param target the deployable that is destroyed
    * @param time length of time that the deployable is allowed to exist in the game world;
    *             `None` indicates the normal un-owned existence time (180 seconds)
    */
  def AnnounceDestroyDeployable(target : PlanetSideGameObject with Deployable, time : Option[FiniteDuration]) : Unit = {
    target.OwnerName match {
      case Some(owner) =>
        target.OwnerName = None
        localService ! LocalServiceMessage(owner, LocalAction.AlertDestroyDeployable(PlanetSideGUID(0), target))
      case None => ;
    }
    localService ! LocalServiceMessage(s"${continent.Id}/${target.Faction}", LocalAction.DeployableMapIcon(
      PlanetSideGUID(0),
      DeploymentAction.Dismiss,
      DeployableInfo(target.GUID, Deployable.Icon(target.Definition.Item), target.Position, PlanetSideGUID(0)))
    )
    localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(target), continent))
    localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(target, continent, time))
  }

  /**
    * Search through the player's holsters and their inventory space
    * and remove all `BoomerTrigger` objects, both functionally and visually.
    * @return all discovered `BoomTrigger` objects
    */
  def RemoveBoomerTriggersFromInventory() : List[BoomerTrigger] = {
    val player_guid = player.GUID
    val holstersWithIndex = player.Holsters().zipWithIndex
    ((player.Inventory.Items.collect({ case InventoryItem(obj : BoomerTrigger, index) => (obj, index) })) ++
      (holstersWithIndex
        .map({ case ((slot, index)) => (slot.Equipment, index) })
        .collect { case ((Some(obj : BoomerTrigger), index)) => (obj, index) }
        )
      )
      .map({ case ((obj, index)) =>
        player.Slot(index).Equipment = None
        sendResponse(ObjectDeleteMessage(obj.GUID, 0))
        if(player.VisibleSlots.contains(index)) {
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, obj.GUID))
        }
        obj
      })
  }

  /**
    * Collect all deployables previously owned by the player,
    * dissociate the avatar's globally unique identifier to remove turnover ownership,
    * and, on top of performing the above manipulations, dispose of any boomers discovered.
    * (`BoomerTrigger` objects, the companions of the boomers, should be handled by an external implementation
    * if they had not already been handled by the time this function is executed.)
    * @return all previously-owned deployables after they have been processed;
    *         boomers are listed before all other deployable types
    */
  def DisownDeployables() : List[PlanetSideGameObject with Deployable] = {
    val (boomers, deployables) =
      avatar.Deployables.Clear()
        .map(continent.GUID(_))
        .collect { case Some(obj) => obj.asInstanceOf[PlanetSideGameObject with Deployable] }
        .partition(_.isInstanceOf[BoomerDeployable])
    //do not change the OwnerName field at this time
    boomers.collect({ case obj : BoomerDeployable =>
      localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent, Some(0 seconds))) //near-instant
      obj.Owner = None
      obj.Trigger = None
    })
    deployables.foreach(obj => {
      localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, continent)) //normal decay
      obj.Owner = None
    })
    boomers ++ deployables
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
    * @see `AvatarDeadStateMessage`
    * @see `interstellarFerry`
    * @see `LoadZoneAsPlayer`
    * @see `LoadZoneInVehicle`
    * @param zone_id the zone in which the player will be placed
    * @param pos the game world coordinates where the player will be positioned
    * @param ori the direction in which the player will be oriented
    * @param respawnTime the character downtime spent respawning, as clocked on the redeployment screen;
    *                    does not factor in any time required for loading zone or game objects
    */
  def LoadZonePhysicalSpawnPoint(zone_id : String, pos : Vector3, ori : Vector3, respawnTime : Long) : Unit = {
    log.info(s"Load in zone $zone_id at position $pos in $respawnTime seconds")
    respawnTimer.cancel
    reviveTimer.cancel
    val backpack = player.isBackpack
    val respawnTimeMillis = respawnTime * 1000 //ms
    deadState = DeadState.RespawnTime
    sendResponse(AvatarDeadStateMessage(DeadState.RespawnTime, respawnTimeMillis, respawnTimeMillis, Vector3.Zero, player.Faction, true))
    val (target, msg) = if(backpack) { //if the player is dead, he is handled as dead infantry, even if he died in a vehicle
      //new player is spawning
      val newPlayer = RespawnClone(player)
      newPlayer.Position = pos
      newPlayer.Orientation = ori
      LoadZoneAsPlayer(newPlayer, zone_id)
    }
    else {
      interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
        case Some(vehicle : Vehicle) => //driver or passenger in vehicle using a warp gate
          LoadZoneInVehicle(vehicle, pos, ori, zone_id)

        case _ => //player is deconstructing self
          val player_guid = player.GUID
          sendResponse(ObjectDeleteMessage(player_guid, 4))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid, 4))
          player.Position = pos
          player.Orientation = ori
          LoadZoneAsPlayer(player, zone_id)
      }
    }
    import scala.concurrent.ExecutionContext.Implicits.global
    respawnTimer = context.system.scheduler.scheduleOnce(respawnTime seconds, target, msg)
  }

  /**
    * Deal with a target player as free-standing infantry in the course of a redeployment action to a target continent
    * whether that action is the result of a deconstruction (reconstruction), a death (respawning),
    * or other position shifting action handled directly by the server.<br>
    * <br>
    * The two important vectors are still whether the zone being transported to is the same or is different
    * and whether the target player is alive or released (note: not just "dead" ...).
    * @see `LoadZoneCommonTransferActivity`
    * @see `GUIDTask.UnregisterAvatar`
    * @see `GUIDTask.UnregisterLocker`
    * @see `PlayerLoaded`
    * @see `Player.isBackpack`
    * @see `RegisterAvatar`
    * @see `TaskBeforeZoneChange`
    * @param tplayer the target player being moved around;
    *                not necessarily the same player as the `WorldSessionActor`-global `player`
    * @param zone_id the zone in which the player will be placed
    * @return a tuple composed of an `ActorRef` destination and a message to send to that destination
    */
  def LoadZoneAsPlayer(tplayer : Player, zone_id : String) : (ActorRef, Any) = {
    if(zone_id == continent.Id) {
      if(player.isBackpack) { //important! test the actor-wide player ref, not the parameter
        //respawning from unregistered player
        (taskResolver, RegisterAvatar(tplayer))
      }
      else {
        //move existing player; this is the one case where the original GUID is retained by the player
        (self, PlayerLoaded(tplayer))
      }
    }
    else {
      LoadZoneCommonTransferActivity()
      val original = player
      if(player.isBackpack) {
        //unregister avatar locker + GiveWorld
        player = tplayer
        (taskResolver, TaskBeforeZoneChange(GUIDTask.UnregisterLocker(original.Locker)(continent.GUID), zone_id))
      }
      else {
        //unregister avatar whole + GiveWorld
        (taskResolver, TaskBeforeZoneChange(GUIDTask.UnregisterAvatar(original)(continent.GUID), zone_id))
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
    * @see `interstellarFerry`
    * @see `LoadZoneInVehicleAsDriver`
    * @see `LoadZoneInVehicleAsPassenger`
    * @see `Vehicle.PassengerInSeat`
    * @param vehicle the target vehicle being moved around;
    *                WILL necessarily be the same vehicles as is controlled by the `WorldSessionActor`-global `player`
    * @param pos     the game world coordinates where the vehicle will be positioned
    * @param ori     the direction in which the vehicle will be oriented
    * @param zone_id the zone in which the vehicle and driver will be placed,
    *                or in which the vehicle has already been placed
    * @return a tuple composed of an ActorRef` destination and a message to send to that destination
    **/
  def LoadZoneInVehicle(vehicle : Vehicle, pos : Vector3, ori : Vector3, zone_id : String) : (ActorRef, Any) = {
    interstellarFerry = Some(vehicle)
    if(vehicle.PassengerInSeat(player).contains(0)) {
      vehicle.Position = pos
      vehicle.Orientation = ori
      LoadZoneInVehicleAsDriver(vehicle, zone_id)
    }
    else {
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
    * @see `interstellarFerryTopLevelGUID`
    * @see `LoadZoneCommonTransferActivity`
    * @see `PlayerLoaded`
    * @see `TaskBeforeZoneChange`
    * @see `UnAccessContents`
    * @see `UnregisterDrivenVehicle`
    * @param vehicle the target vehicle being moved around;
    *                WILL necessarily be the same vehicles as is controlled by the `WorldSessionActor`-global `player`
    * @param zone_id the zone in which the vehicle and driver will be placed,
    *                or in which the vehicle has already been placed
    * @return a tuple composed of an ActorRef` destination and a message to send to that destination
    **/
  def LoadZoneInVehicleAsDriver(vehicle : Vehicle, zone_id : String) : (ActorRef, Any) = {
    log.info(s"LoadZoneInVehicleAsDriver: ${player.Name} is driving a ${vehicle.Definition.Name}")
    val pguid = player.GUID
    val toChannel = TransportVehicleChannelName(vehicle)
    //standard passengers
    vehicleService ! VehicleServiceMessage(s"${vehicle.Actor}", VehicleAction.TransferPassengerChannel(pguid, s"${vehicle.Actor}", toChannel, vehicle))
    //cargo
    val occupiedCargoHolds = vehicle.CargoHolds.values.collect {
      case hold if hold.isOccupied =>
        hold.Occupant.get
    }
    occupiedCargoHolds.foreach{ cargo =>
      cargo.Seats(0).Occupant match {
        case Some(occupant) =>
          vehicleService ! VehicleServiceMessage(s"${occupant.Name}", VehicleAction.TransferPassengerChannel(pguid, s"${cargo.Actor}", toChannel, cargo))
        case _ =>
          log.error("LoadZoneInVehicleAsDriver: abort; vehicle in cargo hold missing driver")
          HandleDismountVehicleCargo(player.GUID, cargo.GUID, cargo, vehicle.GUID, vehicle, false, false, true)
      }
    }
    //
    if(zone_id == continent.Id) {
      //transferring a vehicle between spawn points (warp gates) in the same zone
      //LoadZoneTransferPassengerMessages(pguid, zone_id, toChannel, vehicle, PlanetSideGUID(0))
      (self, PlayerLoaded(player))
    }
    else {
      UnAccessContents(vehicle)
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zone_id //forward-set the continent id to perform a test
      interstellarFerryTopLevelGUID = (if(vehicle.Seats.values.count(_.isOccupied) == 1 && occupiedCargoHolds.size == 0) {
        //do not delete if vehicle has passengers or cargo
        val vehicleToDelete = interstellarFerryTopLevelGUID.orElse(player.VehicleSeated).getOrElse(PlanetSideGUID(0))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.UnloadVehicle(pguid, continent, vehicle, vehicleToDelete))
        None
      }
      else {
        interstellarFerryTopLevelGUID.orElse(Some(vehicle.GUID))
      })
      //unregister vehicle and driver whole + GiveWorld
      continent.Transport ! Zone.Vehicle.Despawn(vehicle)
      (taskResolver, TaskBeforeZoneChange(UnregisterDrivenVehicle(vehicle, player), zone_id))
    }
  }

  /**
    * The channel name for summoning passengers to the vehicle
    * after it has been loaded to a new location or to a new zone.
    * This channel name should be unique to the vehicle for at least the duration of the transition.
    * The vehicle-specific channel with which all passengers are coordinated back to the original vehicle.
    * @param vehicle the vehicle being moved (or having been moved)
    * @return the channel name
    */
  def TransportVehicleChannelName(vehicle : Vehicle) : String = {
    s"transport-vehicle-channel-${interstellarFerryTopLevelGUID.getOrElse(vehicle.GUID).guid}"
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
    * @see `GUIDTask.UnregisterAvatar`
    * @see `LoadZoneCommonTransferActivity`
    * @see `NoVehicleOccupantsInZone`
    * @see `PlayerLoaded`
    * @see `TaskBeforeZoneChange`
    * @see `UnAccessContents`
    * @param vehicle the target vehicle being moved around
    * @param zone_id the zone in which the vehicle and driver will be placed
    * @return a tuple composed of an ActorRef` destination and a message to send to that destination
    **/
  def LoadZoneInVehicleAsPassenger(vehicle : Vehicle, zone_id : String) : (ActorRef, Any) = {
    log.info(s"LoadZoneInVehicleAsPassenger: ${player.Name} is the passenger of a ${vehicle.Definition.Name}")
    if(zone_id == continent.Id) {
      //transferring a vehicle between spawn points (warp gates) in the same zone
      (self, PlayerLoaded(player))
    }
    else {
      LoadZoneCommonTransferActivity()
      player.VehicleSeated = vehicle.GUID
      player.Continent = zone_id //forward-set the continent id to perform a test
      val continentId = continent.Id
      if(NoVehicleOccupantsInZone(vehicle, continentId)) {
        //do not dispatch delete action if any hierarchical occupant has not gotten this far through the summoning process
        val vehicleToDelete = interstellarFerryTopLevelGUID.orElse(player.VehicleSeated).getOrElse(PlanetSideGUID(0))
        vehicleService ! VehicleServiceMessage(continentId, VehicleAction.UnloadVehicle(player.GUID, continent, vehicle, vehicleToDelete))
      }
      interstellarFerryTopLevelGUID = None
      //unregister avatar + GiveWorld
      (taskResolver, TaskBeforeZoneChange(GUIDTask.UnregisterAvatar(player)(continent.GUID), zone_id))
    }
  }

  /**
    * A recursive test that explores all the seats of a target vehicle
    * and all the seats of any discovered cargo vehicles
    * and then the same criteria in those cargo vehicles
    * to determine if any of their combined passenger roster remains in a given zone.<br>
    * <br>
    * While it should be possible to recursively explore up a parent-child relationship -
    * testing the ferrying vehicle to which of the current tested vehicle in consider a cargo vehicle -
    * the relationship expressed is one of globally unique refertences and not one of object references -
    * that suggested super-ferrying vehicle may not exist in the zone unless special considerations are imposed.
    * For the purpose of these special considerations,
    * implemented by enforcing a strictly downwards order of vehicular zone transportation,
    * where drivers move vehicles and call passengers and immediate cargo vehicle drivers,
    * it becomes unnecessary to test any vehicle that might be ferrying the target vehicle.
    * @see `ZoneAware`
    * @param vehicle the target vehicle being moved around
    * @param zone_id the zone in which the vehicle and its passengers should not be located
    * @return `true`, if all passengers of the vehicle, and its cargo vehicles, etc., have reported being moved from the zone;
    *        `false`, otherwise
    */
  def NoVehicleOccupantsInZone(vehicle : Vehicle, zoneId : String) : Boolean = {
    (vehicle.Seats.values
      .collect { case seat if seat.isOccupied && seat.Occupant.get.Continent == zoneId => true }
      .isEmpty) &&
      {
        vehicle.CargoHolds.values
          .collect {
            case hold if hold.isOccupied =>
              hold.Occupant.get
          }
          .foldLeft(true)(_ && NoVehicleOccupantsInZone(_, zoneId))
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
    * @param toChannel the vehicle-specific channel with which all passengers are coordinated to the vehicle
    * @param vehicle the vehicle (object)
    * @param vehicleToDelete the vehicle as it was identified in the zone that it is being moved from
    */
  def LoadZoneTransferPassengerMessages(player_guid : PlanetSideGUID, toZoneId : String, toChannel : String, vehicle : Vehicle, vehicleToDelete : PlanetSideGUID) : Unit = {
    vehicleService ! VehicleServiceMessage(toChannel, VehicleAction.TransferPassenger(player_guid, toChannel, vehicle, vehicleToDelete))
    vehicle.CargoHolds.values
      .collect {
        case hold if hold.isOccupied =>
          val cargo = hold.Occupant.get
          cargo.Continent = toZoneId
          //point to the cargo vehicle to instigate cargo vehicle driver transportation
          vehicleService ! VehicleServiceMessage(toChannel, VehicleAction.TransferPassenger(player_guid, toChannel, cargo, vehicleToDelete))
      }
  }

  /**
    * Common behavior when transferring between zones
    * encompassing actions that disassociate the player with entities they left (will leave) in the previous zone.
    * It also sets up actions for the new zone loading process.
    */
  def LoadZoneCommonTransferActivity() : Unit = {
    RemoveBoomerTriggersFromInventory().foreach(obj => {
      taskResolver ! GUIDTask.UnregisterObjectTask(obj)(continent.GUID)
    })
    DisownDeployables()
    drawDeloyableIcon = RedrawDeployableIcons //important for when SetCurrentAvatar initializes the UI next zone
    continent.Population ! Zone.Population.Leave(avatar)
  }

  /**
    * Primary functionality for tranferring a piece of equipment from a player's hands or his inventory to the ground.
    * Items are always dropped at player's feet because for simplicity's sake
    * because, by virtue of already standing there, the stability of the surface has been proven.
    * The only exception to this is dropping items while falling.
    * @see `Player.Find`<br>
    *       `ObjectDetachMessage`
    * @param item the `Equipment` object in the player's hand
    * @param pos the game world coordinates where the object will be dropped
    * @param orient a suggested orientation in which the object will settle when on the ground;
    *               as indicated, the simulation is only concerned with certain angles
    */
  def PutItemOnGround(item : Equipment, pos : Vector3, orient : Vector3) : Unit = {
    //TODO delay or reverse dropping item when player is falling down
    item.Position = pos
    item.Orientation = Vector3.z(orient.z)
    item.Faction = PlanetSideEmpire.NEUTRAL
    //dropped items rotate towards the user's standing direction
    val exclusionId = player.Find(item) match {
      //if the item is in our hands ...
      case Some(slotNum) =>
        player.Slot(slotNum).Equipment = None
        sendResponse(ObjectDetachMessage(player.GUID, item.GUID, pos, orient.z))
        sendResponse(ActionResultMessage.Pass)
        player.GUID //we're dropping the item; don't need to see it dropped again
      case None =>
        PlanetSideGUID(0) //item is being introduced into the world upon drop
    }
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DropItem(exclusionId, item, continent))
  }

  /**
    * Primary functionality for tranferring a piece of equipment from the ground in a player's hands or his inventory.
    * The final destination of the item in terms of slot position is not determined until the attempt is made.
    * If it can not be placed in a slot correctly, the item will be returned to the ground in the same place.
    * @see `Player.Fit`
    * @param item the `Equipment` object on the ground
    * @return `true`, if the object was properly picked up;
    *        `false` if it was returned to the ground
    */
  def PutItemInHand(item : Equipment) : Boolean = {
    player.Fit(item) match {
      case Some(slotNum) =>
        item.Faction = player.Faction
        val item_guid = item.GUID
        val player_guid = player.GUID
        player.Slot(slotNum).Equipment = item
        val definition = item.Definition
        sendResponse(
          ObjectCreateDetailedMessage(
            definition.ObjectId,
            item_guid,
            ObjectCreateMessageParent(player_guid, slotNum),
            definition.Packet.DetailedConstructorData(item).get
          )
        )
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PickupItem(player_guid, continent, player, slotNum, item))
        true
      case None =>
        continent.Ground ! Zone.Ground.DropItem(item, item.Position, item.Orientation) //restore previous state
        false
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
  def ToggleTeleportSystem(router : Vehicle, systemPlan : Option[(Utility.InternalTelepad, TelepadDeployable)]) : Unit = {
    StartBundlingPackets()
    systemPlan match {
      case Some((internalTelepad, remoteTelepad)) =>
        LinkRouterToRemoteTelepad(router, internalTelepad, remoteTelepad)
      case _ =>
        router.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util : Utility.InternalTelepad) =>
            sendResponse(ObjectDeleteMessage(util.GUID, 0))
          case _ => ;
        }
    }
    StopBundlingPackets()
  }

  /**
    * Link the router teleport system using the provided terminal information.
    * The internal telepad is made known of the remote telepad, creating the link.
    * @param router the vehicle that houses one end of the teleportation system (the `internalTelepad`)
    * @param internalTelepad the endpoint of the teleportation system housed by the router
    * @param remoteTelepad the endpoint of the teleportation system that exists in the environment
    */
  def LinkRouterToRemoteTelepad(router : Vehicle, internalTelepad : Utility.InternalTelepad, remoteTelepad : TelepadDeployable) : Unit = {
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
  def CreateRouterInternalTelepad(router : Vehicle, internalTelepad : PlanetSideGameObject with TelepadLike) : Unit = {
    //create the interal telepad each time the link is made
    val rguid = router.GUID
    val uguid = internalTelepad.GUID
    val udef = internalTelepad.Definition
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
    sendResponse(GenericObjectActionMessage(uguid, 108))
    sendResponse(GenericObjectActionMessage(uguid, 120))
    /*
    the following configurations create the interactive beam underneath the Deployed Router
    normally dispatched after the warm-up timer has completed
     */
    sendResponse(GenericObjectActionMessage(uguid, 108))
    sendResponse(GenericObjectActionMessage(uguid, 112))
  }

  /**
    * na
    * @param telepadGUID na
    */
  def LinkRemoteTelepad(telepadGUID: PlanetSideGUID) : Unit = {
    sendResponse(GenericObjectActionMessage(telepadGUID, 108))
    sendResponse(GenericObjectActionMessage(telepadGUID, 112))
  }

  /**
    * A player uses a fully-linked Router teleportation system.
    * @param router the Router vehicle
    * @param internalTelepad the internal telepad within the Router vehicle
    * @param remoteTelepad the remote telepad that is currently associated with this Router
    * @param src the origin of the teleportation (where the player starts)
    * @param dest the destination of the teleportation (where the player is going)
    */
  def UseRouterTelepadSystem(router: Vehicle, internalTelepad: InternalTelepad, remoteTelepad: TelepadDeployable, src: PlanetSideGameObject with TelepadLike, dest: PlanetSideGameObject with TelepadLike) = {
    val time = System.nanoTime
    if(time - recentTeleportAttempt > (2 seconds).toNanos && router.DeploymentState == DriveState.Deployed && internalTelepad.Active && remoteTelepad.Active) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      StartBundlingPackets()
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      UseRouterTelepadEffect(pguid, sguid, dguid)
      StopBundlingPackets()
//      vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(router), continent))
//      vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(router, continent, router.Definition.DeconstructionTime))
      localService ! LocalServiceMessage(continent.Id, LocalAction.RouterTelepadTransport(pguid, pguid, sguid, dguid))
    }
    else {
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
  def UseRouterTelepadEffect(playerGUID : PlanetSideGUID, srcGUID : PlanetSideGUID, destGUID : PlanetSideGUID) : Unit = {
    sendResponse(PlanetsideAttributeMessage(playerGUID, 64, 1)) //what does this do?
    sendResponse(GenericObjectActionMessage(srcGUID, 124))
    sendResponse(GenericObjectActionMessage(destGUID, 128))
  }

  /**
    * Before a vehicle is removed from the game world, the following actions must be performed.
    * @param vehicle the vehicle
    */
  def BeforeUnloadVehicle(vehicle : Vehicle) : Unit = {
    vehicle.Definition match {
      case GlobalDefinitions.ams if vehicle.Faction == player.Faction =>
        log.info("BeforeUnload: cleaning up after a mobile spawn vehicle ...")
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.UpdateAmsSpawnPoint(continent))
        None
      case GlobalDefinitions.router =>
        //this may repeat for multiple players on the same continent but that's okay(?)
        log.info("BeforeUnload: cleaning up after a router ...")
        RemoveTelepads(vehicle)
      case _ => ;
    }
  }

  def RemoveTelepads(vehicle: Vehicle) : Unit = {
    (vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
      case Some(util : Utility.InternalTelepad) =>
        val telepad = util.Telepad
        util.Telepad = None
        continent.GUID(telepad)
      case _ =>
        None
    }) match {
      case Some(telepad : TelepadDeployable) =>
        log.info(s"BeforeUnload: deconstructing telepad $telepad that was linked to router $vehicle ...")
        telepad.Active = false
        localService ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(telepad), continent))
        localService ! LocalServiceMessage.Deployables(RemoverActor.AddTask(telepad, continent, Some(0 seconds)))
      case _ => ;
    }
  }

  /**
    * For a certain weapon that cna load ammunition, enforce that its magazine is empty.
    * @param weapon_guid the weapon
    */
  def EmptyMagazine(weapon_guid : PlanetSideGUID) : Unit = {
    continent.GUID(weapon_guid) match {
      case Some(tool : Tool) =>
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
  def EmptyMagazine(weapon_guid : PlanetSideGUID, tool : Tool) : Unit = {
    tool.Magazine = 0
    sendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, weapon_guid, 0))
    sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Stop(player.GUID, weapon_guid))
    sendResponse(WeaponDryFireMessage(weapon_guid))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.WeaponDryFire(player.GUID, weapon_guid))
  }

  /**
    * na
    * @param player_guid the target player
    * @param cargoGUID the globally unique number for the vehicle being ferried
    * @param cargo the vehicle being ferried
    * @param carrierGUID the globally unique number for the vehicle doing the ferrying
    * @param carrier the vehicle doing the ferrying
    * @param bailed the ferried vehicle is bailing from the cargo hold
    * @param requestedByPassenger the ferried vehicle is being politely disembarked from the cargo hold
    * @param kicked the ferried vehicle is being kicked out of the cargo hold
    */
  def HandleDismountVehicleCargo(player_guid : PlanetSideGUID, cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, bailed : Boolean, requestedByPassenger : Boolean, kicked : Boolean) : Unit = {
    carrier.CargoHolds.find({case((_, hold)) => hold.Occupant.contains(cargo)}) match {
      case Some((mountPoint, hold)) =>
        cargo.MountedIn = None
        hold.Occupant = None
        val driverOpt = cargo.Seats(0).Occupant
        val rotation : Vector3 = if(CargoOrientation(cargo) == 1) { //TODO: BFRs will likely also need this set
          //dismount router "sideways" in a lodestar
          carrier.Orientation.xy + Vector3.z((carrier.Orientation.z - 90) % 360)
        }
        else {
          carrier.Orientation
        }
        val cargoHoldPosition : Vector3 = if(carrier.Definition == GlobalDefinitions.dropship) {
          //the galaxy cargo bay is offset backwards from the center of the vehicle
          carrier.Position + Vector3.Rz(Vector3(0, 7, 0), math.toRadians(carrier.Orientation.z))
        }
        else {
          //the lodestar's cargo hold is almost the center of the vehicle
          carrier.Position
        }
        StartBundlingPackets()
        vehicleService ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health)))
        vehicleService ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 68, cargo.Shields)))
        if(carrier.Flying) {
          //the carrier vehicle is flying; eject the cargo vehicle
          val ejectCargoMsg = CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.InProgress, 0)
          val detachCargoMsg = ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition - Vector3.z(1), rotation)
          val resetCargoMsg = CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
          sendResponse(ejectCargoMsg) //dismount vehicle on UI and disable "shield" effect on lodestar
          sendResponse(detachCargoMsg)
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(player_guid, ejectCargoMsg))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(player_guid, detachCargoMsg))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(PlanetSideGUID(0), resetCargoMsg)) //lazy
          log.debug(ejectCargoMsg.toString)
          log.debug(detachCargoMsg.toString)
          if(driverOpt.isEmpty) {
            //TODO cargo should drop like a rock like normal; until then, deconstruct it
            vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(cargo), continent))
            vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(cargo, continent, Some(0 seconds)))
          }
        }
        else {
          //the carrier vehicle is not flying; just open the door and let the cargo vehicle back out; force it out if necessary
          val cargoStatusMessage = CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), cargoGUID, PlanetSideGUID(0), mountPoint, CargoStatus.InProgress, 0)
          val cargoDetachMessage = ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition + Vector3.z(1f), rotation)
          sendResponse(cargoStatusMessage)
          sendResponse(cargoDetachMessage)
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.SendResponse(player_guid, cargoStatusMessage))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.SendResponse(player_guid, cargoDetachMessage))
          driverOpt match {
            case Some(driver) =>
              vehicleService ! VehicleServiceMessage(s"${driver.Name}", VehicleAction.KickCargo(player_guid, cargo, cargo.Definition.AutoPilotSpeed2, 2500))

              import scala.concurrent.duration._
              import scala.concurrent.ExecutionContext.Implicits.global
              //check every quarter second if the vehicle has moved far enough away to be considered dismounted
              cargoDismountTimer.cancel
              cargoDismountTimer = context.system.scheduler.scheduleOnce(250 milliseconds, self, CheckCargoDismount(cargoGUID, carrierGUID, mountPoint, iteration = 0))
            case None =>
              val resetCargoMsg = CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
              vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SendResponse(PlanetSideGUID(0), resetCargoMsg)) //lazy
              //TODO cargo should back out like normal; until then, deconstruct it
              vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(cargo), continent))
              vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(cargo, continent, Some(0 seconds)))
          }
        }
        StopBundlingPackets()

      case None =>
        log.warn(s"HandleDismountVehicleCargo: can not locate cargo $cargo in any hold of the carrier vehicle $carrier")
    }
  }

  /**
    * na
    * @param player_guid na
    * @param cargo_guid na
    * @param bailed na
    * @param requestedByPassenger na
    * @param kicked na
    */
  def DismountVehicleCargo(player_guid : PlanetSideGUID, cargo_guid : PlanetSideGUID, bailed : Boolean, requestedByPassenger : Boolean, kicked : Boolean) : Unit = {
    continent.GUID(cargo_guid) match {
      case Some(cargo : Vehicle) =>
        continent.GUID(cargo.MountedIn) match {
          case Some(ferry : Vehicle) =>
            HandleDismountVehicleCargo(player_guid, cargo_guid, cargo, ferry.GUID, ferry, bailed, requestedByPassenger, kicked)
          case _ =>
            log.warn(s"DismountVehicleCargo: target ${cargo.Definition.Name}@$cargo_guid does not know what treats it as cargo")
        }
      case _ =>
        log.warn(s"DismountVehicleCargo: target $cargo_guid either is not a vehicle in ${continent.Id} or does not exist")
    }
  }

  def GetPlayerHackSpeed(obj: PlanetSideServerObject): Float = {
    val playerHackLevel = GetPlayerHackLevel()

    val timeToHack = obj match {
        case (hackable: Hackable) => hackable.HackDuration(playerHackLevel)
        case (vehicle: Vehicle) => vehicle.JackingDuration(playerHackLevel)
        case _ =>
          log.warn(s"Player tried to hack an object that has no hack time defined ${obj.GUID} - ${obj.Definition.Name}")
        0
    }

    if(timeToHack == 0) {
      log.warn(s"Player ${player.GUID} tried to hack an object ${obj.GUID} - ${obj.Definition.Name} that they don't have the correct hacking level for")
      0f
    } else {
      // 250 ms per tick on the hacking progress bar
      val ticks = (timeToHack * 1000) / 250
      100f / ticks
    }
  }

  def GetPlayerHackLevel(): Int = {
    if(player.Certifications.contains(CertificationType.ExpertHacking) || player.Certifications.contains(CertificationType.ElectronicsExpert)) {
      3
    } else if(player.Certifications.contains(CertificationType.AdvancedHacking)) {
      2
    } else if (player.Certifications.contains(CertificationType.Hacking)) {
      1
    } else {
      0
    }
  }

  /**
    * Make this client display the deployment map, and all its available destination spawn points.
    * @see `AvatarDeadStateMessage`
    * @see `DeadState.Release`
    * @see `Player.Release`
    */
  def GoToDeploymentMap() : Unit = {
    player.Release
    deadState = DeadState.Release
    sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
    DrawCurrentAmsSpawnPoint()
  }

  /**
    * From a seat, find the weapon controlled from it, and update the ammunition counts for that weapon's magazines.
    * @param objWithSeat the object that owns seats (and weaponry)
    * @param seatNum the seat
    */
  def UpdateWeaponAtSeatPosition(objWithSeat : MountedWeapons, seatNum : Int) : Unit = {
    objWithSeat.WeaponControlledFromSeat(seatNum) match {
      case Some(weapon : Tool) =>
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
    * @param toZoneId the zone where the target is headed
    * @param toSpawnPoint the unit the target is using as a destination
    * @param fromZoneId the zone where the target current is located
    * @return how long in seconds the spawning process will take
    */
  def CountSpawnDelay(toZoneId : String, toSpawnPoint : SpawnPoint, fromZoneId : String) : Long = {
    val sanctuaryZoneId = Zones.SanctuaryZoneId(player.Faction)
    if(sanctuaryZoneId.equals(toZoneId)) { //to sanctuary
      0L
    }
    else if(!player.isAlive) {
      toSpawnPoint.Definition.Delay //TODO +cumulative death penalty
    }
    else {
      toSpawnPoint.Definition.Delay
    }
  }

  /**
    * In the background, a list of advanced mobile spawn vehicles that are deployed in the zone is being updated constantly.
    * Select, from this list, the AMS that is closest to the player's current or last position
    * and draw its spawn selection icon onto the deployment map.
    * @see `BindPlayerMessage`
    * @see `DeadState.Release`
    */
  def DrawCurrentAmsSpawnPoint() : Unit = {
    if(deadState == DeadState.Release) {
      amsSpawnPoints
        .sortBy(tube => Vector3.DistanceSquared(tube.Position, player.Position))
        .headOption match {
        case Some(tube) =>
          log.info("DrawCurrentAmsSpawnPoint - new @ams spawn point drawn")
          sendResponse(BindPlayerMessage(BindStatus.Available, "@ams", true, false, SpawnGroup.AMS, continent.Number, 5, tube.Position))
        case None =>
          log.info("DrawCurrentAmsSpawnPoint - no @ams spawn point drawn")
          sendResponse(BindPlayerMessage(BindStatus.Unavailable, "@ams", false, false, SpawnGroup.AMS, continent.Number, 0, Vector3.Zero))
      }
    }
  }

  def SwapSquadUIElements(squad : Squad, fromIndex : Int, toIndex : Int) : Unit = {
    if(squadUI.nonEmpty) {
      val fromMember = squad.Membership(toIndex) //the players have already been swapped in the backend object
      val fromCharId = fromMember.CharId
      val toMember = squad.Membership(fromIndex) //the players have already been swapped in the backend object
      val toCharId = toMember.CharId
      val id = 11
      if(toCharId > 0) {
        //toMember and fromMember have swapped places
        val fromElem = squadUI(fromCharId)
        val toElem = squadUI(toCharId)
        squadUI(toCharId) = SquadUIElement(fromElem.name, toIndex, fromElem.zone, fromElem.health, fromElem.armor, fromElem.position)
        squadUI(fromCharId) = SquadUIElement(toElem.name, fromIndex, toElem.zone, toElem.health, toElem.armor, toElem.position)
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
      }
      else {
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
      val charId = avatar.CharId
      if(toCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, toIndex))
      }
      else if(fromCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, fromIndex))
      }
    }
  }

  def NoSquadUpdates() : Unit = { }

  def UpdatesWhenEnrolledInSquad() : Unit = {
    squadService ! SquadServiceMessage(
      player,
      continent,
      continent.GUID(player.VehicleSeated) match {
        case Some(vehicle : Vehicle) =>
          SquadServiceAction.Update(player.CharId, vehicle.Health, vehicle.MaxHealth, vehicle.Shields, vehicle.MaxShields, vehicle.Position, continent.Number)
        case Some(obj : PlanetSideGameObject with WeaponTurret) =>
          SquadServiceAction.Update(player.CharId, obj.Health, obj.MaxHealth, 0, 0, obj.Position, continent.Number)
        case _ =>
          SquadServiceAction.Update(player.CharId, player.Health, player.MaxHealth, player.Armor, player.MaxArmor, player.Position, continent.Number)
      }
    )
  }

  def failWithError(error : String) = {
    log.error(error)
    sendResponse(ConnectionClose())
  }

  /**
    * Persistent collector that intercepts `GamePacket` and `ControlPacket` messages that are being sent towards the network.
    */
  private val packetBundlingCollector : MultiPacketCollector = new MultiPacketCollector()
  /**
    * Re-assigned function used to direct/intercept packets being sent towards the network.
    * Defaults to directing the packets.
    */
  private var packetBundlingFunc : (PlanetSidePacket)=>Option[PlanetSidePacket] = NoBundlingAction

  /**
    * Start packet bundling by assigning the appropriate function.
    * @see `sendResponse(PlanetSidePacket) : Unit`
    */
  def StartBundlingPackets() : Unit = {
    log.trace("WORLD SEND: STARTED BUNDLING PACKETS")
    packetBundlingFunc = PerformBundlingAction
  }

  /**
    * Stop packet bundling by assigning the appropriate function.
    * If any bundles are in the collector's buffer, push that bundle out towards the network.
    * @see `sendResponse(PlanetSidePacket) : Unit`
    */
  def StopBundlingPackets() : Unit = {
    log.trace("WORLD SEND: PACKET BUNDLING SUSPENDED")
    packetBundlingFunc = NoBundlingAction
    packetBundlingCollector.BundleOption match {
      case Some(bundle) =>
        sendResponse(bundle)
      case None => ;
    }
  }

  /**
    * Transform the packet into either a `PlanetSideGamePacket` or a `PlanetSideControlPacket` and push it towards the network.
    * @param cont the packet
    * @return the same packet, to indicate it was sent
    */
  private def NoBundlingAction(cont : PlanetSidePacket) : Option[PlanetSidePacket] = {
    cont match {
      case game : PlanetSideGamePacket =>
        sendResponse(PacketCoding.CreateGamePacket(0, game))
      case control : PlanetSideControlPacket =>
        sendResponse(PacketCoding.CreateControlPacket(control))
      case _ => ;
    }
    Some(cont)
  }

  /**
    * Intercept the packet being sent towards the network and
    * add it to a bundle that will eventually be sent to the network itself.
    * @param cont the packet
    * @return always `None`, to indicate the packet was not sent
    */
  private def PerformBundlingAction(cont : PlanetSidePacket) : Option[PlanetSidePacket] = {
    log.trace("WORLD SEND, BUNDLED: " + cont)
    packetBundlingCollector.Add(cont)
    None
  }

  /**
    * Common entry point for transmitting packets to the network.
    * Alternately, catch those packets and retain them to send out a bundled message.
    * @param cont the packet
    */
  def sendResponse(cont : PlanetSidePacket) : Unit = packetBundlingFunc(cont)

  /**
    * `KeepAliveMessage` is a special `PlanetSideGamePacket` that is excluded from being bundled when it is sent to the network.<br>
    * <br>
    * The risk of the server getting caught in a state where the packets dispatched to the client are always bundled is posible.
    * Starting the bundling functionality but forgetting to transition into a state where it is deactivated can lead to this problem.
    * No packets except for `KeepAliveMessage` will ever be sent until the ever-accumulating packets overflow.
    * To avoid this state, whenever a `KeepAliveMessage` is sent, the packet collector empties its current contents to the network.
    * @see `StartBundlingPackets`
    * @see `StopBundlingPackets`
    * @see `clientKeepAlive`
    * @param cont a `KeepAliveMessage` packet
    */
  def sendResponse(cont : KeepAliveMessage) : Unit = {
    sendResponse(PacketCoding.CreateGamePacket(0, cont))
    packetBundlingCollector.BundleOption match {
      case Some(bundle) =>
        log.trace("WORLD SEND: INTERMITTENT PACKET BUNDLE")
        sendResponse(bundle)
      case None => ;
    }
  }

  def sendResponse(cont : PlanetSidePacketContainer) : Unit = {
    log.trace("WORLD SEND: " + cont)
    sendResponse(cont.asInstanceOf[Any])
  }

  def sendResponse(cont : MultiPacketBundle) : Unit = {
    sendResponse(cont.asInstanceOf[Any])
  }

  def sendResponse(msg : Any) : Unit = {
    MDC("sessionId") = sessionId.toString
    rightRef !> msg
  }

  def sendRawResponse(pkt : ByteVector) = {
    log.trace("WORLD SEND RAW: " + pkt)
    sendResponse(RawPacket(pkt))
  }
}

object WorldSessionActor {
  final case class ResponseToSelf(pkt : PlanetSideGamePacket)

  private final case class PokeClient()
  private final case class ServerLoaded()
  private final case class NewPlayerLoaded(tplayer : Player)
  private final case class PlayerLoaded(tplayer : Player)
  private final case class PlayerFailedToLoad(tplayer : Player)
  private final case class ListAccountCharacters()
  private final case class SetCurrentAvatar(tplayer : Player)
  private final case class VehicleLoaded(vehicle : Vehicle)
  private final case class UnregisterCorpseOnVehicleDisembark(corpse : Player)
  private final case class CheckCargoMounting(vehicle_guid : PlanetSideGUID, cargo_vehicle_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int)
  private final case class CheckCargoDismount(vehicle_guid : PlanetSideGUID, cargo_vehicle_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int)


  /**
    * A message that indicates the user is using a remote electronics kit to hack some server object.<br>
    * <br>
    * Each time this message is sent for a given hack attempt counts as a single "tick" of progress.
    * The process of "making progress" with a hack involves sending this message repeatedly until the progress is 100 or more.
    * To calculate the actual amount of change in the progress `delta`,
    * start with 100, divide by the length of time in seconds, then divide once more by 4.
    * @param progressType 1 - REK hack
    *                     2 - Turret upgrade with glue gun + upgrade cannister
    * @param tplayer the player
    * @param target the object being hacked
    * @param tool_guid the REK
    * @param delta how much the progress bar value changes each tick
    * @param completeAction a custom action performed once the hack is completed
    * @param tickAction an optional action is is performed for each tick of progress
    */
  private final case class HackingProgress(progressType : Int,
                                           tplayer : Player,
                                           target : PlanetSideServerObject,
                                           tool_guid : PlanetSideGUID,
                                           delta : Float,
                                           completeAction : () => Unit,
                                           tickAction : Option[() => Unit] = None)

  protected final case class SquadUIElement(name : String, index : Int, zone : Int, health : Int, armor : Int, position : Vector3)

  private final case class NtuCharging(tplayer: Player,
                                       vehicle: Vehicle)
  private final case class NtuDischarging(tplayer: Player, vehicle: Vehicle, silo_guid: PlanetSideGUID)

  private final case class FinalizeDeployable(obj : PlanetSideGameObject with Deployable, tool : ConstructionItem, index : Int)
}
