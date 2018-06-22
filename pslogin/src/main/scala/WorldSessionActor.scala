// Copyright (c) 2017 PSForever
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet._
import net.psforever.packet.control._
import net.psforever.packet.game.{BattleDiagramAction, _}
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import csr.{CSRWarp, CSRZone, Traveler}
import net.psforever.objects.GlobalDefinitions._
import services.ServiceManager.Lookup
import net.psforever.objects._
import net.psforever.objects.definition.ToolDefinition
import net.psforever.objects.definition.converter.{CharacterSelectConverter, CorpseConverter}
import net.psforever.objects.equipment._
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
import net.psforever.objects.serverobject.structures.{Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.vehicles.{AccessPermissionGroup, Utility, VehicleLockState}
import net.psforever.objects.zones.{InterstellarCluster, Zone}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import services.{RemoverActor, _}
import services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage, AvatarServiceResponse}
import services.galaxy.{GalaxyResponse, GalaxyServiceResponse}
import services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import services.vehicle.VehicleAction.UnstowEquipment
import services.vehicle.{VehicleAction, VehicleResponse, VehicleServiceMessage, VehicleServiceResponse}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success
import akka.pattern.ask
import services.local.support.HackCaptureActor

class WorldSessionActor extends Actor with MDCContextAware {
  import WorldSessionActor._
  private[this] val log = org.log4s.getLogger

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var avatarService : ActorRef = ActorRef.noSender
  var localService : ActorRef = ActorRef.noSender
  var vehicleService : ActorRef = ActorRef.noSender
  var galaxyService : ActorRef = ActorRef.noSender
  var taskResolver : ActorRef = Actor.noSender
  var cluster : ActorRef = Actor.noSender
  var continent : Zone = Zone.Nowhere
  var player : Player = null
  var avatar : Avatar = null
  var progressBarValue : Option[Float] = None
  var shooting : Option[PlanetSideGUID] = None
  var accessedContainer : Option[PlanetSideGameObject with Container] = None
  var flying : Boolean = false
  var speed : Float = 1.0f
  var spectator : Boolean = false
  var admin : Boolean = false
  var usingMedicalTerminal : Option[PlanetSideGUID] = None
  var usingProximityTerminal : Set[PlanetSideGUID] = Set.empty
  var delayedProximityTerminalResets : Map[PlanetSideGUID, Cancellable] = Map.empty
  var controlled : Option[Int] = None //keep track of avatar's ServerVehicleOverride state
  var traveler : Traveler = null
  var deadState : DeadState.Value = DeadState.Dead
  var whenUsedLastKit : Long = 0

  var amsSpawnPoint : Option[SpawnTube] = None

  var clientKeepAlive : Cancellable = DefaultCancellable.obj
  var progressBarUpdate : Cancellable = DefaultCancellable.obj
  var reviveTimer : Cancellable = DefaultCancellable.obj
  var respawnTimer : Cancellable = DefaultCancellable.obj
  var antChargingTick : Cancellable = DefaultCancellable.obj
  var antDischargingTick : Cancellable = DefaultCancellable.obj

  /**
    * Convert a boolean value into an integer value.
    * Use: `true:Int` or `false:Int`
    * @param b `true` or `false` (or `null`)
    * @return 1 for `true`; 0 for `false`
    */
  implicit def boolToInt(b : Boolean) : Int = if(b) 1 else 0

  override def postStop() = {
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
      val player_guid = player.GUID
      //proximity vehicle terminals must be considered too
      delayedProximityTerminalResets.foreach({case(_, task) => task.cancel})
      usingProximityTerminal.foreach(term_guid => {
        continent.GUID(term_guid) match {
          case Some(obj : ProximityTerminal) =>
            if(obj.NumberUsers > 0 && obj.RemoveUser(player_guid) == 0) { //refer to ProximityTerminalControl when modernizng
              localService ! LocalServiceMessage(continent.Id, LocalAction.ProximityTerminalEffect(player_guid, term_guid, false))
            }
          case _ => ;
        }
      })

      if(player.isAlive) {
        //actually being alive or manually deconstructing
        DismountVehicleOnLogOut()
        continent.Population ! Zone.Population.Release(avatar)
        player.Position = Vector3.Zero //save character before doing this
        avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ObjectDelete(player_guid, player_guid))
        taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
        //TODO normally, the actual player avatar persists a minute or so after the user disconnects
      }
      else if(continent.LivePlayers.contains(player) && !continent.Corpses.contains(player)) {
        //player disconnected while waiting for a revive
        //similar to handling ReleaseAvatarRequestMessage
        player.Release
        continent.Population ! Zone.Population.Release(avatar)
        player.VehicleSeated match {
          case None =>
            FriskCorpse(player) //TODO eliminate dead letters
            if(!WellLootedCorpse(player)) {
              continent.Population ! Zone.Corpse.Add(player)
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.Release(player, continent))
              taskResolver ! GUIDTask.UnregisterLocker(player.Locker)(continent.GUID) //rest of player will be cleaned up with corpses
            }
            else { //no items in inventory; leave no corpse
              val player_guid = player.GUID
              player.Position = Vector3.Zero //save character before doing this
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
              taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
            }

          case Some(vehicle_guid) =>
            val player_guid = player.GUID
            player.Position = Vector3.Zero //save character before doing this
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
            taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
            DismountVehicleOnLogOut()
        }
      }

      DisownVehicle()
      continent.Population ! Zone.Population.Leave(avatar)
    }
  }

  /**
    * Vehicle cleanup that is specific to log out behavior.
    */
  def DismountVehicleOnLogOut() : Unit = {
    //TODO Will base guns implement Vehicle type? Don't want those to deconstruct
    (player.VehicleSeated match {
      case Some(vehicle_guid) =>
        continent.GUID(vehicle_guid)
      case None =>
        None
    }) match {
      case Some(vehicle : Vehicle) =>
        vehicle.Seat(vehicle.PassengerInSeat(player).get).get.Occupant = None
        if(vehicle.Seats.values.count(_.isOccupied) == 0) {
          vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, continent), vehicle.Definition.DeconstructionTime) //start vehicle decay
        }
        vehicleService ! Service.Leave(Some(s"${vehicle.Actor}"))

      case Some(mobj : Mountable) =>
        mobj.Seat(mobj.PassengerInSeat(player).get).get.Occupant = None

      case _ => ;
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

    case ControlPacket(_, ctrl) =>
      handleControlPkt(ctrl)
    case GamePacket(_, _, pkt) =>
      handleGamePkt(pkt)
      // temporary hack to keep the client from disconnecting
      //it's been a "temporary hack" since 2016 :P
    case PokeClient() =>
      sendResponse(KeepAliveMessage())

    case AvatarServiceResponse(_, guid, reply) =>
      val tplayer_guid = if(player.HasGUID) { player.GUID} else { PlanetSideGUID(-1) }
      reply match {
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

        case msg @ AvatarResponse.DropItem(pkt) =>
          if(tplayer_guid != guid) {
            sendResponse(pkt)
          }

        case AvatarResponse.EquipmentInHand(pkt) =>
          if(tplayer_guid != guid) {
            sendResponse(pkt)
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
              (distanceSq > 160000 && time > 5000))
            {
              sendResponse(
                PlayerStateMessage(
                  guid,
                  location,
                  msg.vel,
                  msg.facingYaw,
                  msg.facingPitch,
                  msg.facingYawUpper,
                  unk1 = 0,
                  msg.is_crouching,
                  msg.is_jumping,
                  msg.jump_thrust,
                  msg.is_cloaked
                )
              )
              player.lastSeenStreamMessage(guid.guid) = now
            }
          }

        case AvatarResponse.Release(tplayer) =>
          if(tplayer_guid != guid) {
            TurnPlayerIntoCorpse(tplayer)
          }

        case AvatarResponse.Reload(item_guid) =>
          if(tplayer_guid != guid) {
            sendResponse(ReloadMessage(item_guid, 1, 0))
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

    case GalaxyServiceResponse(_, reply) =>
      reply match {
        case GalaxyResponse.MapUpdate(msg) =>
          sendResponse(msg)
      }

    case LocalServiceResponse(_, guid, reply) =>
      val tplayer_guid = if(player.HasGUID) { player.GUID} else { PlanetSideGUID(-1) }
      reply match {
        case LocalResponse.DoorOpens(door_guid) =>
          if(tplayer_guid != guid) {
            sendResponse(GenericObjectStateMsg(door_guid, 16))
          }

        case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
          sendResponse(GenericObjectStateMsg(door_guid, 17))

        case LocalResponse.HackClear(target_guid, unk1, unk2) =>
          log.trace(s"Clearing hack for ${target_guid}")
          // Reset hack state for all players
          sendResponse(HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2))
          // Set the object faction displayed back to it's original owner faction

          continent.GUID(target_guid) match {
            case Some(obj) =>
              sendResponse(SetEmpireMessage(target_guid, obj.asInstanceOf[FactionAffinity].Faction))
            case None => ;
          }

        case LocalResponse.HackObject(target_guid, unk1, unk2) =>
          if(tplayer_guid != guid && continent.GUID(target_guid).get.asInstanceOf[Hackable].HackedBy.get._1.Faction != player.Faction) {
            // If the player is not in the faction that hacked this object then send the packet that it's been hacked, so they can either unhack it or use the hacked object
            // Don't send this to the faction that hacked the object, otherwise it will interfere with the new SetEmpireMessage QoL change that changes the object colour to their faction (but only visible to that faction)
            sendResponse(HackMessage(0, target_guid, guid, 100, unk1, HackState.Hacked, unk2))
          }

          if(continent.GUID(target_guid).get.asInstanceOf[Hackable].HackedBy.get._1.Faction == player.Faction){
            // Make the hacked object look like it belongs to the hacking empire, but only for that empire's players (so that infiltrators on stealth missions won't be given away to opposing factions)
            sendResponse(SetEmpireMessage(target_guid, player.Faction))
          }
        case LocalResponse.HackCaptureTerminal(target_guid, unk1, unk2, isResecured) =>
          var value = 0L

          if(isResecured) {
            value = 17039360L
          } else {
            import scala.concurrent.ExecutionContext.Implicits.global
            val future = ask(localService, HackCaptureActor.GetHackTimeRemainingNanos(target_guid))(1 second)
            val time = Await.result(future, 1 second).asInstanceOf[Long] // todo: blocking call. Not good.
            val hack_time_remaining_ms = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS)
            val deciseconds_remaining = (hack_time_remaining_ms / 100)

            val hacking_faction = continent.GUID(target_guid).get.asInstanceOf[Hackable].HackedBy.get._1.Faction

            // See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
            val start_num = hacking_faction match {
              case PlanetSideEmpire.TR => 65536L
              case PlanetSideEmpire.NC => 131072L
              case PlanetSideEmpire.VS => 196608L
            }

            value = start_num + deciseconds_remaining
          }

          sendResponse(PlanetsideAttributeMessage(target_guid, 20, value))
        case LocalResponse.ProximityTerminalEffect(object_guid, effectState) =>
          if(tplayer_guid != guid) {
            sendResponse(ProximityTerminalUseMessage(PlanetSideGUID(0), object_guid, effectState))
          }

        case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
          sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

        case LocalResponse.SetEmpire(object_guid, empire) =>
          sendResponse(SetEmpireMessage(object_guid, empire))
        case _ => ;
      }

    case VehicleServiceResponse(_, guid, reply) =>
      val tplayer_guid = if(player.HasGUID) { player.GUID} else { PlanetSideGUID(0) }
      reply match {
        case VehicleResponse.Ownership(vehicle_guid) =>
          sendResponse(PlanetsideAttributeMessage(guid, 21, vehicle_guid.guid.toLong))

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
          sendResponse(ObjectDetachMessage(pad_guid, vehicle_guid, pad_position + Vector3(0,0,0.5f), pad_orientation_z))

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

        case msg @ VehicleResponse.KickPassenger(seat_num, wasKickedByDriver, vehicle_guid) =>
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

        case VehicleResponse.UnloadVehicle(vehicle_guid) =>
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

        case VehicleResponse.UpdateAmsSpawnPoint(list) =>
          if(player.isBackpack) {
            //dismiss old ams spawn point
            ClearCurrentAmsSpawnPoint()
            //draw new ams spawn point
            list
              .filter(tube => tube.Faction == player.Faction)
              .sortBy(tube => Vector3.DistanceSquared(tube.Position, player.Position))
              .headOption match {
              case Some(tube) =>
                sendResponse(
                  BattleplanMessage(41378949, "ams", continent.Number, List(BattleDiagramAction(DiagramActionCode.StartDrawing)))
                )
                sendResponse(
                  BattleplanMessage(41378949, "ams", continent.Number, List(BattleDiagramAction.drawString(tube.Position.x, tube.Position.y, 3, 0, "AMS")))
                )
                amsSpawnPoint = Some(tube)
              case None => ;
            }
          }

        case _ => ;
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
          antChargingTick.cancel() // If an ANT is refilling a NTU silo it isn't in a warpgate, so disable NTU regeneration
          antDischargingTick.cancel()

          antDischargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuDischarging(player, continent.GUID(vehicle_guid).get.asInstanceOf[Vehicle], silo_guid))
      }

    case Door.DoorMessage(tplayer, msg, order) =>
      val door_guid = msg.object_guid
      order match {
        case Door.OpenEvent() =>
          continent.GUID(door_guid) match {
            case Some(door : Door) =>
              sendResponse(GenericObjectStateMsg(door_guid, 16))
              localService ! LocalServiceMessage(continent.Id, LocalAction.DoorOpens (tplayer.GUID, continent, door) )

            case _ =>
              log.warn(s"door $door_guid wanted to be opened but could not be found")
          }

        case Door.CloseEvent() =>
          sendResponse(GenericObjectStateMsg(door_guid, 17))
          localService ! LocalServiceMessage(continent.Id, LocalAction.DoorCloses(tplayer.GUID, door_guid))

        case Door.NoEvent() => ;
      }

    case Mountable.MountMessages(tplayer, reply) =>
      reply match {
        case Mountable.CanMount(obj : ImplantTerminalMech, seat_num) =>
          val player_guid : PlanetSideGUID = tplayer.GUID
          val obj_guid : PlanetSideGUID = obj.GUID
          log.info(s"MountVehicleMsg: $player_guid mounts $obj @ $seat_num")
          PlayerActionsToCancel()
          sendResponse(PlanetsideAttributeMessage(obj_guid, 0, 1000L)) //health of mech
          sendResponse(ObjectAttachMessage(obj_guid, player_guid, seat_num))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seat_num))

        case Mountable.CanMount(obj : Vehicle, seat_num) =>
          val obj_guid : PlanetSideGUID = obj.GUID
          val player_guid : PlanetSideGUID = tplayer.GUID
          log.info(s"MountVehicleMsg: $player_guid mounts $obj_guid @ $seat_num")
          vehicleService ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(obj), continent)) //clear timer
          PlayerActionsToCancel()
          if(seat_num == 0) { //simplistic vehicle ownership management
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
            tplayer.VehicleOwned = Some(obj_guid)
            obj.Owner = Some(player_guid)
          }
          obj.WeaponControlledFromSeat(seat_num) match {
            case Some(weapon : Tool) =>
              //update mounted weapon belonging to seat
              weapon.AmmoSlots.foreach(slot => { //update the magazine(s) in the weapon, specifically
                val magazine = slot.Box
                sendResponse(InventoryStateMessage(magazine.GUID, weapon.GUID, magazine.Capacity.toLong))
              })
            case _ => ; //no weapons to update
          }
          sendResponse(ObjectAttachMessage(obj_guid, player_guid, seat_num))
          AccessContents(obj)
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seat_num))

        case Mountable.CanMount(obj : Mountable, _) =>
          log.warn(s"MountVehicleMsg: $obj is some generic mountable object and nothing will happen")

        case Mountable.CanDismount(obj : ImplantTerminalMech, seat_num) =>
          val obj_guid : PlanetSideGUID = obj.GUID
          val player_guid : PlanetSideGUID = tplayer.GUID
          log.info(s"DismountVehicleMsg: $player_guid dismounts $obj @ $seat_num")
          sendResponse(DismountVehicleMsg(player_guid, BailType.Normal, false))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, BailType.Normal, false))

        case Mountable.CanDismount(obj : Vehicle, seat_num) =>
          val player_guid : PlanetSideGUID = tplayer.GUID
          if(player_guid == player.GUID) {
            //disembarking self
            log.info(s"DismountVehicleMsg: $player_guid dismounts $obj @ $seat_num")
            TotalDriverVehicleControl(obj)
            sendResponse(DismountVehicleMsg(player_guid, BailType.Normal, false))
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, BailType.Normal, false))
            UnAccessContents(obj)
          }
          else {
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(player_guid, seat_num, true, obj.GUID))
          }
          if(obj.Seats.values.count(_.isOccupied) == 0) {
            vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(obj, continent, obj.Definition.DeconstructionTime)) //start vehicle decay
          }

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

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      order match {
        case Terminal.BuyExosuit(exosuit, subtype) => //refresh armor points
          if(tplayer.ExoSuit == exosuit) {
            if(exosuit == ExoSuitType.MAX) {
              //special MAX case - clear any special state
              player.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
              player.ExoSuit = exosuit
              if(Loadout.DetermineSubtype(tplayer) != subtype) {
                //special MAX case - suit switching to a different MAX suit; we need to change the main weapon
                sendResponse(ArmorChangedMessage(tplayer.GUID, exosuit, subtype))
                avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
                val arms = tplayer.Slot(0).Equipment.get
                val putTask = PutEquipmentInSlot(tplayer, Tool(GlobalDefinitions.MAXArms(subtype, tplayer.Faction)), 0)
                taskResolver ! DelayedObjectHeld(tplayer, 0, List(TaskResolver.GiveTask(putTask.task, putTask.subs :+ RemoveEquipmentFromSlot(tplayer, arms, 0))))
              }
            }
            //outside of the MAX condition above, we should seldom reach this point through conventional methods
            tplayer.Armor = tplayer.MaxArmor
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor))
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
          }
          else {
            //load a complete new exo-suit and shuffle the inventory around
            val originalSuit = tplayer.ExoSuit
            //save inventory before it gets cleared (empty holsters)
            val dropPred = DropPredicate(tplayer)
            val (dropHolsters, beforeHolsters) = clearHolsters(tplayer.Holsters().iterator).partition(dropPred)
            val (dropInventory, beforeInventory) = tplayer.Inventory.Clear().partition(dropPred)
            //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
            tplayer.ExoSuit = exosuit
            tplayer.Armor = tplayer.MaxArmor
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
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor))
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
            val finalInventory = if(exosuit == ExoSuitType.MAX) {
              //MAX weapon to be placed in first pistol slot; slot to be drawn
              taskResolver ! DelayedObjectHeld(tplayer, 0, List(PutEquipmentInSlot(tplayer, Tool(GlobalDefinitions.MAXArms(subtype, tplayer.Faction)), 0)))
              //fill melee slot
              fillEmptyHolsters(List(tplayer.Slot(4)).iterator, beforeHolsters) ++ beforeInventory
            }
            else {
              //remove potential MAX weapon
              val normalWeapons = if(originalSuit == ExoSuitType.MAX) {
                val (maxWeapons, normalWeapons) = beforeHolsters.partition(elem => elem.obj.Size == EquipmentSize.Max)
                maxWeapons.foreach(entry => { taskResolver ! GUIDTask.UnregisterEquipment(entry.obj)(continent.GUID) })
                normalWeapons
              }
              else {
                tplayer.DrawnSlot = Player.HandsDownSlot
                sendResponse(ObjectHeldMessage(tplayer.GUID, Player.HandsDownSlot, true))
                avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectHeld(tplayer.GUID, Player.HandsDownSlot))
                beforeHolsters
              }
              //fill holsters
              val (afterHolsters, toInventory) = normalWeapons.partition(elem => elem.obj.Size == tplayer.Slot(elem.start).Size)
              afterHolsters.foreach({elem => tplayer.Slot(elem.start).Equipment = elem.obj })
              fillEmptyHolsters(tplayer.Holsters().iterator, toInventory ++ beforeInventory)
            }
            //draw holsters
            tplayer.VisibleSlots.foreach({index =>
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
            val (stow, drop) = GridInventory.recoverInventory(finalInventory, tplayer.Inventory)
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
            //drop items on ground
            val pos = tplayer.Position
            val orient = Vector3(0,0, tplayer.Orientation.z)
            ((dropHolsters ++ dropInventory).map(_.obj) ++ drop).foreach(obj => {
              //TODO make a sound when dropping stuff
              continent.Ground ! Zone.Ground.DropItem(obj, pos, orient)
            })
            sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true))
          }

        case Terminal.BuyEquipment(item) => ;
          tplayer.Fit(item) match {
            case Some(index) =>
              sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true))
              taskResolver ! PutEquipmentInSlot(tplayer, item, index)
            case None =>
              sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, false))
          }

        case Terminal.SellEquipment() =>
          tplayer.FreeHand.Equipment match {
            case Some(item) =>
              if(item.GUID == msg.item_guid) {
                sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Sell, true))
                taskResolver ! RemoveEquipmentFromSlot(tplayer, item, Player.FreeHandSlot)
              }
            case None =>
              sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Sell, false))
          }

        case Terminal.InfantryLoadout(exosuit, subtype, holsters, inventory) =>
          //TODO optimizations against replacing Equipment with the exact same Equipment and potentially for recycling existing Equipment
          log.info(s"$tplayer wants to change equipment loadout to their option #${msg.unk1 + 1}")
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Loadout, true))
          //ensure arm is down
          tplayer.DrawnSlot = Player.HandsDownSlot
          sendResponse(ObjectHeldMessage(tplayer.GUID, Player.HandsDownSlot, true))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectHeld(tplayer.GUID, Player.HandsDownSlot))
          //load
          val dropPred = DropPredicate(tplayer)
          val (dropHolsters, beforeHolsters) = clearHolsters(tplayer.Holsters().iterator).partition(dropPred)
          val (dropInventory, beforeInventory) = tplayer.Inventory.Clear().partition(dropPred)
          val (_, afterHolsters) = holsters.partition(dropPred) //dropped items are lost
          val (_, afterInventory) = inventory.partition(dropPred) //dropped items are lost
          val beforeFreeHand = tplayer.FreeHand.Equipment
          //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
          tplayer.ExoSuit = exosuit
          tplayer.Armor = tplayer.MaxArmor
          //delete everything (not dropped)
          beforeHolsters.foreach({ elem =>
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(tplayer.GUID, elem.obj.GUID))
          })
          (beforeHolsters ++ beforeInventory).foreach({ elem =>
            sendResponse(ObjectDeleteMessage(elem.obj.GUID, 0))
            taskResolver ! GUIDTask.UnregisterEquipment(elem.obj)(continent.GUID)
          })
          //report change
          sendResponse(ArmorChangedMessage(tplayer.GUID, exosuit, subtype))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
          sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
          //re-draw equipment held in free hand
          beforeFreeHand match {
            case Some(item) =>
              tplayer.FreeHand.Equipment = beforeFreeHand
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
          //draw holsters
          if(exosuit == ExoSuitType.MAX) {
            tplayer.DrawnSlot = 0
            val (maxWeapons, otherWeapons) = afterHolsters.partition(entry => { entry.obj.Size == EquipmentSize.Max })
            taskResolver ! DelayedObjectHeld(tplayer, 0, List(PutEquipmentInSlot(tplayer, maxWeapons.head.obj, 0)))
            otherWeapons
          }
          else {
            afterHolsters
          }.foreach(entry => {
            taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //put items into inventory
          afterInventory.foreach(entry => {
            taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //drop stuff on ground
          val pos = tplayer.Position
          val orient = Vector3(0,0, tplayer.Orientation.z)
          ((dropHolsters ++ dropInventory).map(_.obj)).foreach(obj => {
            continent.Ground ! Zone.Ground.DropItem(obj, pos, orient)
          })

        case Terminal.VehicleLoadout(definition, weapons, inventory) =>
          log.info(s"$tplayer wants to change their vehicle equipment loadout to their option #${msg.unk1 + 1}")
          FindLocalVehicle match {
            case Some(vehicle) =>
              sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Loadout, true))
              val (_, afterInventory) = inventory.partition( DropPredicate(tplayer) ) //dropped items are lost
              //remove old inventory
              val deleteEquipment : (Int,Equipment)=>Unit = DeleteEquipmentFromVehicle(vehicle)
              vehicle.Inventory.Clear().foreach({ case InventoryItem(obj, index) => deleteEquipment(index, obj) })
              val stowEquipment : (Int,Equipment)=>TaskResolver.GiveTask = StowNewEquipmentInVehicle(vehicle)
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
                  //TODO map x,y -> x,y rather than reorganize items
                  val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory) //dropped items can be forgotten
                  stow
                }
              }).foreach({ case InventoryItem(obj, index) =>
                taskResolver ! stowEquipment(index, obj)
              })
            case None =>
              log.error(s"can not apply the loadout - can not find a vehicle")
              sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Loadout, false))
          }

        case Terminal.LearnCertification(cert, cost) =>
          if(!tplayer.Certifications.contains(cert)) {
            log.info(s"$tplayer is learning the $cert certification for $cost points")
            avatar.Certifications += cert
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 24, cert.id.toLong))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, true))
          }
          else {
            log.warn(s"$tplayer already knows the $cert certification, so he can't learn it")
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false))
          }

        case Terminal.SellCertification(cert, cost) =>
          if(tplayer.Certifications.contains(cert)) {
            log.info(s"$tplayer is forgetting the $cert certification for $cost points")
            avatar.Certifications -= cert
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 25, cert.id.toLong))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Sell, true))
          }
          else {
            log.warn(s"$tplayer doesn't know what a $cert certification is, so he can't forget it")
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false))
          }

        case Terminal.LearnImplant(implant) =>
          val terminal_guid = msg.terminal_guid
          val implant_type = implant.Type
          val message = s"Implants: $tplayer wants to learn $implant_type"
          val (interface, slotNumber) = tplayer.VehicleSeated match {
            case Some(mech_guid) =>
              (
                continent.Map.TerminalToInterface.get(mech_guid.guid),
                if(!avatar.Implants.exists({slot => slot.Implant == implant_type})) { //no duplicates
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

        case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
          continent.Map.TerminalToSpawnPad.get(msg.terminal_guid.guid) match {
            case Some(pad_guid) =>
              val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad]
              vehicle.Faction = tplayer.Faction
              vehicle.Position = pad.Position
              vehicle.Orientation = pad.Orientation
              //default loadout, weapons
              log.info(s"default weapons: ${weapons.size}")
              val vWeapons = vehicle.Weapons
              weapons.foreach(entry => {
                val index = entry.start
                vWeapons.get(index) match {
                  case Some(slot) =>
                    slot.Equipment = None
                    slot.Equipment = entry.obj
                  case None =>
                    log.warn(s"applying default loadout to $vehicle, can not find a mounted weapon @ $index")
                }
              })
              //default loadout, trunk
              log.info(s"default trunk: ${trunk.size}")
              val vTrunk = vehicle.Trunk
              vTrunk.Clear()
              trunk.foreach(entry => { vTrunk += entry.start -> entry.obj })
              taskResolver ! RegisterNewVehicle(vehicle, pad)
              sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))

            case None =>
              log.error(s"$tplayer wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it")
          }

        case Terminal.StartProximityEffect(term) =>
          val player_guid = player.GUID
          val term_guid = term.GUID
          StartUsingProximityUnit(term) //redundant but cautious
          sendResponse(ProximityTerminalUseMessage(player_guid, term_guid, true))
          localService ! LocalServiceMessage(continent.Id, LocalAction.ProximityTerminalEffect(player_guid, term_guid, true))

        case Terminal.StopProximityEffect(term) =>
          val player_guid = player.GUID
          val term_guid = term.GUID
          StopUsingProximityUnit(term) //redundant but cautious
          sendResponse(ProximityTerminalUseMessage(player_guid, term_guid, false))
          localService ! LocalServiceMessage(continent.Id, LocalAction.ProximityTerminalEffect(player_guid, term_guid, false))

        case Terminal.NoDeal() =>
          val order : String = if(msg == null) {
            s"order $msg"
          }
          else {
            "missing order"
          }
          log.warn(s"${tplayer.Name} made a request but the terminal rejected the $order")
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false))
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
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 21, player.GUID.guid)) //ownership

    case VehicleSpawnPad.PlayerSeatedInVehicle(vehicle, pad) =>
      val vehicle_guid = vehicle.GUID
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 0L)) //mount points on
      //sendResponse(PlanetsideAttributeMessage(vehicle_guid, 0, 10))//vehicle.Definition.MaxHealth))
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 68, 0L)) // Shield health
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 113, 0L)) // Capacitor (EMP)
      ReloadVehicleAccessPermissions(vehicle)
      ServerVehicleLock(vehicle)

    case VehicleSpawnPad.ServerVehicleOverrideStart(vehicle, pad) =>
      val vdef = vehicle.Definition
      if(vehicle.Seats(0).isOccupied) {
        sendResponse(ObjectDetachMessage(pad.GUID, vehicle.GUID, pad.Position + Vector3(0, 0, 0.5f), pad.Orientation.z))
      }
      ServerVehicleOverride(vehicle, vdef.AutoPilotSpeed1, GlobalDefinitions.isFlightVehicle(vdef):Int)

    case VehicleSpawnControlGuided.GuidedControl(cmd, vehicle, data) =>
      cmd match {
        case AutoDriveControls.State.Drive =>
          val speed : Int = data.getOrElse({ vehicle.Definition.AutoPilotSpeed1 }).asInstanceOf[Int]
          ServerVehicleOverride(vehicle, speed)

        case AutoDriveControls.State.Climb =>
          ServerVehicleOverride(vehicle, controlled.getOrElse(0), GlobalDefinitions.isFlightVehicle(vehicle.Definition):Int)

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
      if(health > 0) { //player can not be dead; stay spawned as alive
        player.Health = health
        player.Stamina = stamina
        player.Armor = armor
      }
      sendResponse(CharacterInfoMessage(15,PlanetSideZoneID(10000), 41605313, player.GUID, false, 6404428))
      RemoveCharacterSelectScreenGUID(player)

      sendResponse(CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0))
      sendResponse(CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0))

    case VehicleLoaded(_/*vehicle*/) => ;
      //currently being handled by VehicleSpawnPad.LoadVehicle during testing phase

    case Zone.ClientInitialization(zone) =>
      val continentNumber = zone.Number
      val poplist = zone.Players
      val popBO = 0 //TODO black ops test (partition)
      val popTR = poplist.count(_.faction == PlanetSideEmpire.TR)
      val popNC = poplist.count(_.faction == PlanetSideEmpire.NC)
      val popVS = poplist.count(_.faction == PlanetSideEmpire.VS)

      // StopBundlingPackets() is called on ClientInitializationComplete
      StartBundlingPackets()
      zone.Buildings.foreach({ case(id, building) => initBuilding(continentNumber, id, building) })
      sendResponse(ZonePopulationUpdateMessage(continentNumber, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))
      sendResponse(ContinentalLockUpdateMessage(continentNumber, PlanetSideEmpire.NEUTRAL))
      //CaptureFlagUpdateMessage()
      //VanuModuleUpdateMessage()
      //ModuleLimitsMessage()
      sendResponse(ZoneInfoMessage(continentNumber, true, 0))
      sendResponse(ZoneLockInfoMessage(continentNumber, false, true))
      sendResponse(ZoneForcedCavernConnectionsMessage(continentNumber, 0))
      sendResponse(HotSpotUpdateMessage(continentNumber, 1, Nil)) //normally set in bulk; should be fine doing per continent

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
      var pos = spawn_tube.Position
      var ori = spawn_tube.Orientation
      spawn_tube.Owner match {
        case building : Building =>
          log.info(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id in building ${building.Id} selected")
        case vehicle : Vehicle =>
          //TODO replace this bad math with good math or no math
          //position the player alongside either of the AMS's terminals, facing away from it
          val side = if(System.currentTimeMillis() % 2 == 0) 1 else -1 //right | left
          val z = spawn_tube.Orientation.z
          val zrot = (z + 90) % 360
          val x = spawn_tube.Orientation.x
          val xsin = 3 * side * math.abs(math.sin(math.toRadians(x))).toFloat + 0.5f //sin because 0-degrees is up
          val zrad = math.toRadians(zrot)
          pos = pos + (Vector3(math.sin(zrad).toFloat, math.cos(zrad).toFloat, 0) * (3 * side)) //x=sin, y=cos because compass-0 is East, not North
          ori = if(side == 1) {
            Vector3(0, 0, zrot)
          }
          else {
            Vector3(0, 0, (z - 90) % 360)
          }
          pos = if(x >= 330) { //leaning to the left
            pos + Vector3(0, 0, xsin)
          }
          else {
            pos - Vector3(0, 0, xsin)
          }
          log.info(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id at ams ${vehicle.GUID.guid} selected")
        case owner =>
          log.warn(s"Zone.Lattice.SpawnPoint: spawn point on $zone_id at ${spawn_tube.Position} has unexpected owner $owner")
      }
      respawnTimer.cancel
      reviveTimer.cancel
      ClearCurrentAmsSpawnPoint()
      val sameZone = zone_id == continent.Id
      val backpack = player.isBackpack
      val respawnTime : Long = if(sameZone) { 10 } else { 0 } //s
      val respawnTimeMillis = respawnTime * 1000 //ms
      deadState = DeadState.RespawnTime
      sendResponse(AvatarDeadStateMessage(DeadState.RespawnTime, respawnTimeMillis, respawnTimeMillis, Vector3.Zero, player.Faction, true))
      val tplayer = if(backpack) {
        RespawnClone(player) //new player
      }
      else {
        val player_guid = player.GUID
        sendResponse(ObjectDeleteMessage(player_guid, 4))
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid, 4))
        player //player is deconstructing self
      }

      tplayer.Position = pos
      tplayer.Orientation = ori
      val (target, msg) : (ActorRef, Any) = if(sameZone) {
        if(backpack) {
          //respawning from unregistered player
          (taskResolver, RegisterAvatar(tplayer))
        }
        else {
          //move existing player
          (self, PlayerLoaded(tplayer))
        }
      }
      else {
        DisownVehicle()
        continent.Population ! Zone.Population.Leave(avatar)
        val original = player
        //TODO check player orientation upon spawn not polluted
        if(backpack) {
          //unregister avatar locker + GiveWorld
          player = tplayer
          (taskResolver, TaskBeforeZoneChange(GUIDTask.UnregisterLocker(original.Locker)(continent.GUID), zone_id))
        }
        else {
          //unregister avatar whole + GiveWorld
          (taskResolver, TaskBeforeZoneChange(GUIDTask.UnregisterAvatar(original)(continent.GUID), zone_id))
        }
      }
      import scala.concurrent.ExecutionContext.Implicits.global
      respawnTimer = context.system.scheduler.scheduleOnce(respawnTime seconds, target, msg)

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

    case Zone.Ground.ItemOnGround(item, pos, orient) =>
      item.Position = pos
      item.Orientation = Vector3(0,0, orient.z) //dropped items rotate towards the user's standing direction
      val exclusionId = player.Find(item) match {
        case Some(slotNum) =>
          player.Slot(slotNum).Equipment = None
          sendResponse(ObjectDetachMessage(player.GUID, item.GUID, pos, orient.z))
          sendResponse(ActionResultMessage.Pass)
          player.GUID //we're dropping it; don't need to see it dropped again
        case None =>
          PlanetSideGUID(0) //object is being introduced into the world upon drop
      }
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.DropItem(exclusionId, item, continent))

    case Zone.Ground.CanNotDropItem(zone, item, reason) =>
      log.warn(s"DropItem: $player tried to drop a $item on the ground, but $reason")

    case Zone.Ground.ItemInHand(item) =>
      player.Fit(item) match {
        case Some(slotNum) =>
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
        case None =>
          continent.Ground ! Zone.Ground.DropItem(item, item.Position, item.Orientation) //restore previous state
      }

    case Zone.Ground.CanNotPickupItem(zone, item_guid, _) =>
      zone.GUID(item_guid) match {
        case Some(item) =>
          log.warn(s"DropItem: finding a $item on the ground was suggested, but $player can not reach it")
        case None =>
          log.warn(s"DropItem: finding an item ($item_guid) on the ground was suggested, but $player can not see it")
      }

    case InterstellarCluster.ClientInitializationComplete() =>
      StopBundlingPackets()
      LivePlayerList.Add(sessionId, avatar)
      traveler = new Traveler(self, continent.Id)
      //PropertyOverrideMessage
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 1))
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector(SquadListing()))) //clear squad list
      sendResponse(FriendsResponse(FriendAction.InitializeFriendList, 0, true, true, Nil))
      sendResponse(FriendsResponse(FriendAction.InitializeIgnoreList, 0, true, true, Nil))
      cluster ! InterstellarCluster.GetWorld("z6")

    case InterstellarCluster.GiveWorld(zoneId, zone) =>
      log.info(s"Zone $zoneId will now load")
      avatarService ! Service.Leave(Some(continent.Id))
      localService ! Service.Leave(Some(continent.Id))
      vehicleService ! Service.Leave(Some(continent.Id))
      player.Continent = zoneId
      continent = zone
      continent.Population ! Zone.Population.Join(avatar)
      taskResolver ! RegisterNewAvatar(player)

    case NewPlayerLoaded(tplayer) =>
      log.info(s"Player ${tplayer.Name} has been loaded")
      player = tplayer
      //LoadMapMessage will cause the client to send back a BeginZoningMessage packet (see below)
      sendResponse(LoadMapMessage(continent.Map.Name, continent.Id, 40100,25,true,3770441820L))
      AvatarCreate() //important! the LoadMapMessage must be processed by the client before the avatar is created

    case PlayerLoaded(tplayer) =>
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
      player = tplayer
      val guid = tplayer.GUID
      StartBundlingPackets()
      sendResponse(SetCurrentAvatarMessage(guid, 0, 0))
      sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, true, "", "1 on", None)) //CC on //TODO once per respawn?
      sendResponse(PlayerStateShiftMessage(ShiftState(1, tplayer.Position, tplayer.Orientation.z)))
      //transfer vehicle ownership
      player.VehicleOwned match {
        case Some(vehicle_guid) =>
          continent.GUID(vehicle_guid) match {
            case Some(vehicle : Vehicle) =>
              vehicle.Owner = player
              vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.Ownership(guid, vehicle_guid))
            case _ =>
              player.VehicleOwned = None
          }
        case None => ;
      }
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
      //FavoritesMessage
      sendResponse(SetChatFilterMessage(ChatChannel.Local, false, ChatChannel.values.toList)) //TODO will not always be "on" like this
      deadState = DeadState.Alive
      sendResponse(AvatarDeadStateMessage(DeadState.Alive, 0, 0, tplayer.Position, player.Faction, true))
      sendResponse(PlanetsideAttributeMessage(guid, 53, 1))
      sendResponse(AvatarSearchCriteriaMessage(guid, List(0, 0, 0, 0, 0, 0)))
      (1 to 73).foreach(i => {
        sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(i), 67, 0))
      })
      (0 to 30).foreach(i => {
        //TODO 30 for a new character only?
        sendResponse(AvatarStatisticsMessage(2, Statistics(0L)))
      })
      //AvatarAwardMessage
      //DisplayAwardMessage
      //SquadDefinitionActionMessage and SquadDetailDefinitionUpdateMessage
      //MapObjectStateBlockMessage and ObjectCreateMessage?
      //TacticsMessage?
      StopBundlingPackets()

    case NtuCharging(tplayer, vehicle) =>
      log.trace(s"NtuCharging: Vehicle ${vehicle.GUID} is charging NTU capacitor.")

      if(vehicle.Capacitor < vehicle.Definition.MaximumCapacitor) {
        // Charging
        vehicle.Capacitor += 100

        sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.round((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10) )) // set ntu on vehicle UI
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 1L)) // panel glow on
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 49, 1L)) // orb particle effect on

        antChargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuCharging(player, vehicle)) // Repeat until fully charged
      } else {
        // Fully charged
        sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.round((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10).toInt)) // set ntu on vehicle UI

        // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
        context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))
      }

    case NtuDischarging(tplayer, vehicle, silo_guid) =>
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
          sendResponse(PlanetsideAttributeMessage(vehicle.GUID, 45, scala.math.round((vehicle.Capacitor.toFloat / vehicle.Definition.MaximumCapacitor.toFloat) * 10))) // set ntu on vehicle UI

          //todo: grant BEP to user
          //todo: grant BEP to squad in range
          //todo: notify map service to update ntu % on map for all users

          //todo: handle silo orb / panel glow properly if more than one person is refilling silo and one player stops. effects should stay on until all players stop

          if(vehicle.Capacitor > 0 && silo.ChargeLevel < silo.MaximumCharge) {
            log.trace(s"NtuDischarging: ANT not empty and Silo not full. Scheduling another discharge")
            // Silo still not full and ant still has charge left - keep rescheduling ticks
            antDischargingTick = context.system.scheduler.scheduleOnce(1000 milliseconds, self, NtuDischarging(player, vehicle, silo_guid))
          } else {
            log.trace(s"NtuDischarging: ANT NTU empty or Silo NTU full.")

            // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
            context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))

            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
            antDischargingTick.cancel()
          }
        } else {
          // This shouldn't normally be run, only if the client thinks the ANT has capacitor charge when it doesn't, or thinks the silo isn't full when it is.
          log.warn(s"NtuDischarging: Invalid discharge state. ANT Capacitor: ${vehicle.Capacitor} Silo Capacitor: ${silo.ChargeLevel}")

          // Turning off glow/orb effects on ANT doesn't seem to work when deployed. Try to undeploy ANT from server side
          context.system.scheduler.scheduleOnce(vehicle.UndeployTime milliseconds, vehicle.Actor, Deployment.TryUndeploy(DriveState.Undeploying))

          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
          antDischargingTick.cancel()
        }
      } else {
        log.trace(s"NtuDischarging: Vehicle is no longer deployed. Removing effects")
        // Vehicle has changed from deployed and this should be the last timer tick sent
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(vehicle.GUID, 52, 0L)) // panel glow off
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(silo_guid, 49, 0L)) // panel glow off & orb particles off
        antDischargingTick.cancel()
      }

    case ItemHacking(tplayer, target, tool_guid, delta, completeAction, tickAction) =>
      progressBarUpdate.cancel
      if(progressBarValue.isDefined) {
        val progressBarVal : Float = progressBarValue.get + delta
        val vis = if(progressBarVal == 0L) { //hack state for progress bar visibility
          HackState.Start
        }
        else if(progressBarVal > 100L) {
          HackState.Finished
        }
        else {
          HackState.Ongoing
        }
        sendResponse(HackMessage(1, target.GUID, player.GUID, progressBarVal.toInt, 0L, vis, 8L))
        if(progressBarVal > 100) { //done
          progressBarValue = None
          log.info(s"Hacked a $target")
//          sendResponse(HackMessage(0, target.GUID, player.GUID, 100, 1114636288L, HackState.Hacked, 8L))
          completeAction()
        }
        else { //continue next tick
          tickAction.getOrElse(() => Unit)()
          progressBarValue = Some(progressBarVal)
          import scala.concurrent.ExecutionContext.Implicits.global
          progressBarUpdate = context.system.scheduler.scheduleOnce(250 milliseconds, self, ItemHacking(tplayer, target, tool_guid, delta, completeAction))
        }
      }

    case DelayedProximityUnitStop(terminal) =>
      StopUsingProximityUnit(terminal)

    case ResponseToSelf(pkt) =>
      log.info(s"Received a direct message: $pkt")
      sendResponse(pkt)

    case default =>
     log.warn(s"Invalid packet class received: $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case sync @ ControlSync(diff, _, _, _, _, _, fa, fb) =>
        log.debug(s"SYNC: $sync")
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
      avatar = Avatar("TestCharacter" + sessionId.toString, PlanetSideEmpire.VS, CharacterGender.Female, 41, CharacterVoice.Voice1)
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
      AwardBattleExperiencePoints(avatar, 1000000L)
      player = new Player(avatar)
      //player.Position = Vector3(3561.0f, 2854.0f, 90.859375f) //home3, HART C
      player.Position = Vector3(3940.3984f, 4343.625f, 266.45312f)
      player.Orientation = Vector3(0f, 0f, 90f)
      //player.Position = Vector3(4262.211f ,4067.0625f ,262.35938f) //z6, Akna.tower
      //player.Orientation = Vector3(0f, 0f, 132.1875f)
//      player.ExoSuit = ExoSuitType.MAX //TODO strange issue; divide number above by 10 when uncommenting
      player.Slot(0).Equipment = SimpleItem(remote_electronics_kit) //Tool(GlobalDefinitions.StandardPistol(player.Faction))
      player.Slot(2).Equipment = Tool(mini_chaingun) //punisher //suppressor
      player.Slot(4).Equipment = Tool(GlobalDefinitions.StandardMelee(player.Faction))
      player.Slot(6).Equipment = AmmoBox(bullet_9mm, 20) //bullet_9mm
      player.Slot(9).Equipment = AmmoBox(rocket, 11) //bullet_9mm
      player.Slot(12).Equipment = AmmoBox(frag_cartridge) //bullet_9mm
      player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
      player.Slot(36).Equipment = AmmoBox(GlobalDefinitions.StandardPistolAmmo(player.Faction))
      player.Slot(39).Equipment = AmmoBox(plasma_cartridge) //SimpleItem(remote_electronics_kit)
      player.Locker.Inventory += 0 -> SimpleItem(remote_electronics_kit)
      //TODO end temp player character auto-loading
      self ! ListAccountCharacters
      import scala.concurrent.ExecutionContext.Implicits.global
      clientKeepAlive.cancel
      clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())

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
      traveler.zone = continent.Id
      StartBundlingPackets()
      avatarService ! Service.Join(continent.Id)
      localService ! Service.Join(continent.Id)
      vehicleService ! Service.Join(continent.Id)
      galaxyService ! Service.Join("galaxy")
      configZone(continent)
      sendResponse(TimeOfDayMessage(1191182336))
      //custom
      sendResponse(ContinentalLockUpdateMessage(13, PlanetSideEmpire.VS)) // "The VS have captured the VS Sanctuary."
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector(SquadListing()))) //clear squad list
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 1)) //common
      //(0 to 255).foreach(i => { sendResponse(SetEmpireMessage(PlanetSideGUID(i), PlanetSideEmpire.VS)) })

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
      //load active players in zone
      continent.LivePlayers
        .filterNot(tplayer => { tplayer.GUID == player.GUID || tplayer.VehicleSeated.nonEmpty })
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
      //load active vehicles in zone
      continent.Vehicles.foreach(vehicle => {
        val vehicle_guid = vehicle.GUID
        val vdefinition = vehicle.Definition
        sendResponse(ObjectCreateMessage(vdefinition.ObjectId, vehicle_guid, vdefinition.Packet.ConstructorData(vehicle).get))
        //occupants other than driver
        vehicle.Seats
          .filter({ case(index, seat) => seat.isOccupied && index > 0 })
          .foreach({ case(index, seat) =>
            val tplayer = seat.Occupant.get
            val tdefintion = tplayer.Definition
            sendResponse(
              ObjectCreateMessage(
                tdefintion.ObjectId,
                tplayer.GUID,
                ObjectCreateMessageParent(vehicle_guid, index),
                tdefintion.Packet.ConstructorData(tplayer).get
              )
            )
          })
        ReloadVehicleAccessPermissions(vehicle)
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
      StopBundlingPackets()
      self ! SetCurrentAvatar(player)

    case msg @ PlayerStateMessageUpstream(avatar_guid, pos, vel, yaw, pitch, yaw_upper, seq_time, unk3, is_crouching, is_jumping, unk4, is_cloaking, unk5, unk6) =>
      if(player.isAlive) {
        player.Position = pos
        player.Velocity = vel
        player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
        player.FacingYawUpper = yaw_upper
        player.Crouching = is_crouching
        player.Jumping = is_jumping
        if(vel.isDefined && usingMedicalTerminal.isDefined) {
          StopUsingProximityUnit(continent.GUID(usingMedicalTerminal.get).get.asInstanceOf[ProximityTerminal])
        }
        val wepInHand : Boolean = player.Slot(player.DrawnSlot).Equipment match {
          case Some(item) => item.Definition == GlobalDefinitions.bolt_driver
          case None => false
        }
        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlayerState(avatar_guid, msg, spectator, wepInHand))
      }

    case msg @ ChildObjectStateMessage(object_guid, pitch, yaw) =>
      //the majority of the following check retrieves information to determine if we are in control of the child
      player.VehicleSeated match {
        case Some(vehicle_guid) =>
          continent.GUID(vehicle_guid) match {
            case Some(obj : Vehicle) =>
              obj.PassengerInSeat(player) match {
                case Some(seat_num) =>
                  obj.WeaponControlledFromSeat(seat_num) match {
                    case Some(tool) =>
                      if(tool.GUID == object_guid) {
                        //TODO set tool orientation?
                        player.Orientation = Vector3(0f, pitch, yaw)
                        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.ChildObjectState(player.GUID, object_guid, pitch, yaw))
                      }
                    case None =>
                      log.warn(s"ChildObjectState: player $player is not using stated controllable agent")
                  }
                case None =>
                  log.warn(s"ChildObjectState: player ${player.GUID} is not in a position to use controllable agent")
              }
            case _ =>
              log.warn(s"ChildObjectState: player $player's controllable agent not available in scope")
          }
        case None =>
        //TODO status condition of "playing getting out of vehicle to allow for late packets without warning
        //log.warn(s"ChildObjectState: player $player not related to anything with a controllable agent")
      }
    //log.info("ChildObjectState: " + msg)

    case msg @ VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk5, unk6, unk7, wheels, unk9, unkA) =>
      continent.GUID(vehicle_guid) match {
        case Some(obj : Vehicle) =>
          val seat = obj.Seat(0).get
          if(seat.Occupant.contains(player)) {
            //we're driving the vehicle
            player.Position = pos //convenient
            if(seat.ControlledWeapon.isEmpty) {
              player.Orientation = Vector3(0f, 0f, ang.z) //convenient
            }
            obj.Position = pos
            obj.Orientation = ang
            obj.Velocity = vel
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.VehicleState(player.GUID, vehicle_guid, unk1, pos, ang, vel, unk5, unk6, unk7, wheels, unk9, unkA))
          }
        //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
        case _ =>
          log.warn(s"VehicleState: no vehicle $vehicle_guid found in zone")
      }
      //log.info(s"VehicleState: $msg")

    case msg @ VehicleSubStateMessage(vehicle_guid, player_guid, vehicle_pos, vehicle_ang, vel, unk1, unk2) =>
    //log.info(s"VehicleSubState: $vehicle_guid, $player_guid, $vehicle_pos, $vehicle_ang, $vel, $unk1, $unk2")

    case msg @ ProjectileStateMessage(projectile_guid, shot_pos, shot_vector, unk1, unk2, unk3, unk4, time_alive) =>
    //log.info("ProjectileState: " + msg)

    case msg @ ReleaseAvatarRequestMessage() =>
      log.info(s"ReleaseAvatarRequest: ${player.GUID} on ${continent.Id} has released")
      reviveTimer.cancel
      player.Release
      deadState = DeadState.Release
      sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
      continent.Population ! Zone.Population.Release(avatar)
      vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.UpdateAmsSpawnPoint(continent))
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
          //TODO we do not want to delete the player if he is seated in a vehicle when releasing
          //TODO it is necessary for now until we know how to juggle ownership properly
          val player_guid = player.GUID
          sendResponse(ObjectDeleteMessage(player_guid, 0))
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid, 0))
          self ! PacketCoding.CreateGamePacket(0, DismountVehicleMsg(player_guid, BailType.Normal, true)) //let vehicle try to clean up its fields

          import scala.concurrent.ExecutionContext.Implicits.global
          context.system.scheduler.scheduleOnce(50 milliseconds, self, UnregisterCorpseOnVehicleDisembark(player))
          //sendResponse(ObjectDetachMessage(vehicle_guid, player.GUID, Vector3.Zero, 0))
          //sendResponse(PlayerStateShiftMessage(ShiftState(1, Vector3.Zero, 0)))
      }

    case msg @ SpawnRequestMessage(u1, u2, u3, u4, u5) =>
      log.info(s"SpawnRequestMessage: $msg")
      //TODO just focus on u5 and u2 for now
      cluster ! Zone.Lattice.RequestSpawnPoint(u5.toInt, player, u2.toInt)

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
        case (true, zone, pos) =>
          if(player.isAlive) {
            player.Die //die to suspend client-driven position change updates
            PlayerActionsToCancel()
            player.Position = pos
            traveler.zone = zone
            continent.Population ! Zone.Population.Release(avatar)
            continent.Population ! Zone.Population.Leave(avatar)
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player.GUID, player.GUID))
            taskResolver ! TaskBeforeZoneChange(GUIDTask.UnregisterAvatar(player)(continent.GUID), zone)
          }

        case (false, _, _) => ;
      }

      CSRWarp.read(traveler, msg) match {
        case (true, pos) =>
          if(player.isAlive) {
            PlayerActionsToCancel()
            sendResponse(PlayerStateShiftMessage(ShiftState(0, pos, player.Orientation.z, None)))
            player.Position = pos
          }

        case (false, _) => ;
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
          KillPlayer(player)
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
        if(player.isBackpack) { //player is on deployment screen (either dead or deconstructed)
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
      FindContainedWeapon match {
        case (Some(obj), Some(tool : Tool)) =>
          val originalAmmoType = tool.AmmoType
          val fullMagazine = tool.MaxMagazine
          do {
            val requestedAmmoType = tool.NextAmmoType
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
                    val dropFunc : (Equipment)=>Unit = NewItemDrop(player, continent, avatarService)
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
                              dropFunc(box)
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

        case (_, Some(_)) =>
          log.error(s"ChangeAmmo: the object that was found for $item_guid was not a Tool")
        case (_, None) =>
          log.error(s"ChangeAmmo: can not find $item_guid")
      }

    case msg @ ChangeFireModeMessage(item_guid, fire_mode) =>
      log.info("ChangeFireMode: " + msg)
      FindWeapon match {
        case Some(tool : Tool) =>
          val originalModeIndex = tool.FireModeIndex
          tool.NextFireMode
          val modeIndex = tool.FireModeIndex
          val tool_guid = tool.GUID
          if(originalModeIndex != modeIndex) {
            log.info(s"ChangeFireMode: changing $tool_guid to fire mode $modeIndex")
            sendResponse(ChangeFireModeMessage(tool_guid, modeIndex))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireMode(player.GUID, tool_guid, modeIndex))
          }
          else {
            tool.FireModeIndex = originalModeIndex
            sendResponse(ChangeFireModeMessage(tool_guid, originalModeIndex))
          }
        case Some(_) =>
          log.error(s"ChangeFireMode: the object that was found for $item_guid was not a Tool")
        case None =>
          log.error(s"ChangeFireMode: can not find $item_guid")
      }

    case msg @ ChangeFireStateMessage_Start(item_guid) =>
      log.info("ChangeFireState_Start: " + msg)
      if(shooting.isEmpty) {
        FindEquipment match {
          case Some(tool : Tool) =>
            if(tool.GUID == item_guid && tool.Magazine > 0) {
              shooting = Some(item_guid)
              avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(player.GUID, item_guid))
            }
          case Some(_) => //permissible, for now
            shooting = Some(item_guid)
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Start(player.GUID, item_guid))
          case None =>
            log.error(s"ChangeFireState_Start: can not find $item_guid")
        }
      }

    case msg @ ChangeFireStateMessage_Stop(item_guid) =>
      log.info("ChangeFireState_Stop: " + msg)
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
            usingMedicalTerminal match {
              case Some(term_guid) =>
                StopUsingProximityUnit(continent.GUID(term_guid).get.asInstanceOf[ProximityTerminal])
              case None => ;
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

        case Some(obj : Equipment) =>
          val findFunc : PlanetSideGameObject with Container => Option[(PlanetSideGameObject with Container, Option[Int])] = FindInLocalContainer(object_guid)

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

            case _ =>
              if(continent.EquipmentOnGround.contains(obj)) {
                obj.Position = Vector3.Zero
                continent.Ground ! Zone.Ground.RemoveItem(object_guid)
                avatarService ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
                avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(PlanetSideGUID(0), object_guid))
                log.info(s"RequestDestroy: equipment $obj on ground")
              }
              else {
                log.warn(s"RequestDestroy: equipment $obj exists, but can not be reached")
              }
          }

        case Some(thing) =>
          log.warn(s"RequestDestroy: not allowed to delete object $thing")

        case None =>
          log.warn(s"RequestDestroy: object $object_guid not found")
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
                PerformMoveItem(item, source, index, destination, dest, destItemEntry)
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
              PerformMoveItem(item, source, index, target, dest, None)
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
          if(player.Faction == door.Faction || ((continent.Map.DoorToLock.get(object_guid.guid) match {
            case Some(lock_guid) =>
              val lock = continent.GUID(lock_guid).get.asInstanceOf[IFFLock]

              var baseIsHacked = false
              lock.Owner.asInstanceOf[Building].Amenities.filter(x => x.Definition == GlobalDefinitions.capture_terminal).headOption.asInstanceOf[Option[CaptureTerminal]] match {
                case Some(obj: CaptureTerminal) =>
                  baseIsHacked = obj.HackedBy.isDefined
                case None => ;
              }

              // If the IFF lock has been hacked OR the base is neutral OR the base linked to the lock is hacked then open the door
              lock.HackedBy.isDefined || baseIsHacked || lock.Faction == PlanetSideEmpire.NEUTRAL
            case None => !door.isOpen
          }) || Vector3.ScalarProjection(door.Outwards, player.Position - door.Position) < 0f)) {
            // We're on the inside of the door - open the door
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
                      self ! WorldSessionActor.ItemHacking(player, panel, tool.GUID, hackSpeed, FinishHacking(panel, 1114636288L))
                      log.info("Hacking an IFF lock")
                    } else {
                      // IFF Lock is being resecured by it's owner faction
                      self ! WorldSessionActor.ItemHacking(player, panel, tool.GUID, hackSpeed, FinishResecuringIFFLock(panel))
                      log.info("Resecuring an IFF lock")
                    }

                  }
                }
              case _ => ;
            }
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
                            //TODO better health/damage control workflow
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
                    self ! WorldSessionActor.ItemHacking(player, locker, tool.GUID, hackSpeed, FinishHacking(locker, 3212836864L))
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

        case Some(captureTerminal : CaptureTerminal) =>
          val hackedByCurrentFaction = (captureTerminal.Faction != player.Faction && !captureTerminal.HackedBy.isEmpty && captureTerminal.HackedBy.head._1.Faction == player.Faction)
          val ownedByPlayerFactionAndHackedByEnemyFaction = (captureTerminal.Faction == player.Faction && !captureTerminal.HackedBy.isEmpty)
          if(!hackedByCurrentFaction || ownedByPlayerFactionAndHackedByEnemyFaction) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool: SimpleItem) =>
                if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  val hackSpeed = GetPlayerHackSpeed(captureTerminal)

                  if(hackSpeed > 0) {
                    progressBarValue = Some(-hackSpeed)
                    self ! WorldSessionActor.ItemHacking(player, captureTerminal, tool.GUID, hackSpeed, FinishHacking(captureTerminal, 3212836864L))
                    log.info("Hacking a capture terminal")
                  }
                }
              case _ => ;
            }
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
                //TODO hacking behavior

              case _ => ;
            }
          }

        case Some(terminal : Terminal) =>
          if(terminal.Definition.isInstanceOf[MatrixTerminalDefinition]) {
            //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
            sendResponse(BindPlayerMessage(1, "@ams", true, true, 0, 0, 0, terminal.Position))
          }
          else if(terminal.Definition.isInstanceOf[RepairRearmSiloDefinition]) {
            FindLocalVehicle match {
              case Some(vehicle) =>
                sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
                sendResponse(UseItemMessage(avatar_guid, item_used_guid, vehicle.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, vehicle.Definition.ObjectId))
              case None =>
                log.error("UseItem: expected seated vehicle, but found none")
            }
          }
          else {
            if(terminal.Faction != player.Faction && terminal.HackedBy.isEmpty) {
              player.Slot(player.DrawnSlot).Equipment match {
                case Some(tool: SimpleItem) =>
                  if (tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                    val hackSpeed = GetPlayerHackSpeed(terminal)

                    if(hackSpeed > 0) {
                      progressBarValue = Some(-hackSpeed)
                      self ! WorldSessionActor.ItemHacking(player, terminal, tool.GUID, hackSpeed, FinishHacking(terminal, 3212836864L))
                      log.info("Hacking a terminal")
                    }
                  }
                case _ => ;
              }
            } else if (terminal.Faction == player.Faction || !terminal.HackedBy.isEmpty) {
              // If hacked only allow access to the faction that hacked it
              // Otherwise allow the faction that owns the terminal to use it
              sendResponse(UseItemMessage(avatar_guid, item_used_guid, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
            }

          }

        case Some(obj : SpawnTube) =>
          //deconstruction
          PlayerActionsToCancel()
          CancelAllProximityUnits()
          player.Release
          deadState = DeadState.Release
          sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, true))
          continent.Population ! Zone.Population.Release(avatar)

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
          if(usingProximityTerminal.contains(object_guid)) {
            SelectProximityUnit(obj)
          }
          else {
            StartUsingProximityUnit(obj)
          }
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

    case msg @ DeployObjectMessage(guid, unk1, pos, roll, pitch, yaw, unk2) =>
      log.info("DeployObject: " + msg)

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
      else if(action == 16) {
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

    case msg @ ItemTransactionMessage(terminal_guid, _, _, _, _, _) =>
      log.info("ItemTransaction: " + msg)
      continent.GUID(terminal_guid) match {
        case Some(term : Terminal) =>
          log.info(s"ItemTransaction: ${term.Definition.Name} found")
          term.Actor ! Terminal.Request(player, msg)
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
                avatar.SaveLoadout(owner, name, lineno)
                import InfantryLoadout._
                sendResponse(FavoritesMessage(list, player_guid, line, name, DetermineSubtypeB(player.ExoSuit, DetermineSubtype(player))))
              case Some(owner : Vehicle) => //VehicleLoadout
                avatar.SaveLoadout(owner, name, lineno)
                sendResponse(FavoritesMessage(list, player_guid, line, name))
              case Some(_) | None =>
                log.error("FavoritesRequest: unexpected owner for favorites")
            }

          case FavoritesAction.Delete =>
            avatar.DeleteLoadout(lineno)
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
      FindWeapon match {
        case Some(tool : Tool) =>
          if(tool.Magazine <= 0) { //safety: enforce ammunition depletion
            tool.Magazine = 0
            sendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, weapon_guid, 0))
            sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ChangeFireState_Stop(player.GUID, weapon_guid))
            sendResponse(WeaponDryFireMessage(weapon_guid))
            avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.WeaponDryFire(player.GUID, weapon_guid))
          }
          else { //shooting
            tool.Discharge
            //TODO other stuff?
          }
        case _ => ;
      }

    case msg @ WeaponLazeTargetPositionMessage(weapon, pos1, pos2) =>
      log.info("Lazing position: " + pos2.toString)

    case msg @ HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
      log.info("Hit: " + msg)

    case msg @ SplashHitMessage(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
      log.info("SplashHitMessage: " + msg)

    case msg @ AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, event_name) =>
      log.info("AvatarFirstTimeEvent: " + msg)

    case msg @ WarpgateRequest(continent_guid, building_guid, dest_building_guid, dest_continent_guid, unk1, unk2) =>
      log.info("WarpgateRequest: " + msg)

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
      if(player.HasGUID && player.GUID == player_guid) {
        //normally disembarking from a seat
        player.VehicleSeated match {
          case Some(obj_guid) =>
            continent.GUID(obj_guid) match {
              case Some(obj : Mountable) =>
                obj.PassengerInSeat(player) match {
                  case Some(seat_num : Int) =>
                    obj.Actor ! Mountable.TryDismount(player, seat_num)

                    // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
                    //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
                    //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
                    if(bailType == BailType.Bailed && seat_num == 0 && GlobalDefinitions.isFlightVehicle(obj.asInstanceOf[Vehicle].Definition)) {
                      vehicleService ! VehicleServiceMessage.Decon(RemoverActor.AddTask(obj, continent, Some(0 seconds))) // Immediately deconstruct vehicle
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
      if(player.VehicleOwned == Some(vehicle_guid) && player.VehicleOwned == player.VehicleSeated) {
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

    case msg @ SquadDefinitionActionMessage(a, b, c, d, e, f, g, h, i) =>
      log.info("SquadDefinitionAction: " + msg)

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
                    vehicle.Seats.foreach({ case (seat_num, seat) =>
                      seat.Occupant match {
                        case Some(tplayer) =>
                          if(vehicle.SeatPermissionGroup(seat_num).contains(group) && tplayer != player) { //can not kick self
                            seat.Occupant = None
                            tplayer.VehicleSeated = None
                            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(tplayer.GUID, 4, false, object_guid))
                          }
                        case None => ;
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
      //log.info(s"ShieldChargeRequest: $msg")

    case msg @ BattleplanMessage(char_id, player_name, zonr_id, diagrams) =>
      log.info("Battleplan: "+msg)

    case msg @ CreateShortcutMessage(player_guid, slot, unk, add, shortcut) =>
      log.info("CreateShortcutMessage: "+msg)

    case msg @ FriendsRequest(action, friend) =>
      log.info("FriendsRequest: "+msg)

    case msg @ HitHint(source, player_guid) =>
      log.info("HitHint: "+msg)

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

  //TODO this may be useful for vehicle gating
  def RegisterDrivenVehicle(obj : Vehicle, driver : Player) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localVehicle = obj
        private val localDriver = driver

        override def isComplete : Task.Resolution.Value = {
          if(localVehicle.HasGUID && localDriver.HasGUID) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          //TODO some kind of callback ...
          resolver ! scala.util.Success(this)
        }
      }, List(RegisterAvatar(driver), RegisterVehicle(obj)))
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
    * The process of hacking an object is completed
    * Pass the message onto the hackable object and onto the local events system.
    * @param target the `Hackable` object that has been hacked
    * @param unk na;
    *            used by `HackMessage` as `unk5`
    * @see `HackMessage`
    */
  //TODO add params here depending on which params in HackMessage are important
  private def FinishHacking(target : PlanetSideServerObject with Hackable, unk : Long)() : Unit = {
    // Wait for the target actor to set the HackedBy property, otherwise LocalAction.HackTemporarily will not complete properly
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(target.Actor, CommonMessages.Hack(player))(1 second).mapTo[Boolean].onComplete {
      case Success(_) =>
        localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerSound(player.GUID, target.HackSound, player.Position, 30, 0.49803925f))
        target match {
          case term : CaptureTerminal =>
            val isResecured = player.Faction == target.Faction
            localService ! LocalServiceMessage(continent.Id, LocalAction.HackCaptureTerminal(player.GUID, continent, term, unk, 8L, isResecured))
          case _ =>
            localService ! LocalServiceMessage(continent.Id, LocalAction.HackTemporarily(player.GUID, continent, target, unk, target.HackEffectDuration(GetPlayerHackLevel())))
        }

      case scala.util.Failure(_) => log.warn(s"Hack message failed on target guid: ${target.GUID}")
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
    * This is considered a client issue; but, somehow, it also impacts server operation somehow.
    * @param vehicle the `Vehicle`
    */
  def ReloadVehicleAccessPermissions(vehicle : Vehicle) : Unit = {
    val vehicle_guid = vehicle.GUID
    (0 to 3).foreach(group => {
      sendResponse(
        PlanetsideAttributeMessage(vehicle_guid, group + 10, vehicle.PermissionGroup(group).get.id.toLong)
      )
    })
  }

  /**
    * Disassociate this client's player (oneself) from a vehicle that he owns.
    */
  def DisownVehicle() : Unit = DisownVehicle(player)

  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the current continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver seat.
    * This is the player side of vehicle ownership removal.
    * @see `DisownVehicle(Player, Vehicle)`
    * @param tplayer the player
    */
  def DisownVehicle(tplayer : Player) : Unit = {
    tplayer.VehicleOwned match {
      case Some(vehicle_guid) =>
        continent.GUID(vehicle_guid) match {
          case Some(vehicle : Vehicle) =>
            DisownVehicle(tplayer, vehicle)
          case _ => ;
        }
        tplayer.VehicleOwned = None
      case None => ;
    }
  }

  /**
    * Disassociate a vehicle from the player that owns it.
    * When a vehicle is disowned
    * This is the vehicle side of vehicle ownership removal.
    * @see `DisownVehicle(Player)`
    * @param tplayer the player
    * @param vehicle the discovered vehicle
    */
  private def DisownVehicle(tplayer : Player, vehicle : Vehicle) : Unit = {
    if(vehicle.Owner.contains(tplayer.GUID)) {
      vehicle.Owner = None
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
          case Some(vehicle : Vehicle) =>
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
    * Given an object that contains a box of amunition in its `Inventry` at a certain location,
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
    * @param item an `AmmoBox`
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
    * @param item an `AmmoBox`
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
                if(obj.isBackpack || destination.VisibleSlots.contains(dest)) { //corpse being looted, or item was in hands
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, item2_guid))
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
                vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player_guid, source_guid, index, item2))
              case obj : Player =>
                if(source.VisibleSlots.contains(index)) { //item is put in hands
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(player_guid, source_guid, index, item2))
                }
                else if(obj.isBackpack) { //corpse being given item
                  avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.StowEquipment(player_guid, source_guid, index, item2))
                }
              case _ => ;
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
        vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player_guid, destination_guid, dest, item))
      case obj : Player =>
        if(destination.VisibleSlots.contains(dest)) { //item is put in hands
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(player_guid, destination_guid, dest, item))
        }
        else if(obj.isBackpack) { //corpse being given item
          avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.StowEquipment(player_guid, destination_guid, dest, item))
        }
      case _ => ;
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
    * @param service a reference to the event system that announces that the item has been dropped on the ground;
    *                "AvatarService";
    *                curried for callback
    * @param item the item
    */
  def NormalItemDrop(obj : PlanetSideGameObject with Container, zone : Zone, service : ActorRef)(item : Equipment) : Unit = {
    continent.Ground ! Zone.Ground.DropItem(item, obj.Position, Vector3(0f, 0f, obj.Orientation.z))
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
  def NewItemDrop(obj : PlanetSideGameObject with Container, zone : Zone, service : ActorRef)(item : Equipment) : Unit = {
    taskResolver ! TaskResolver.GiveTask(
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
    * @param tplayer the player
    * @return true if the item is to be dropped; false, otherwise
    */
  def DropPredicate(tplayer : Player) : (InventoryItem => Boolean) = entry => { //drop if Cavern equipment, or is another faction's exclusive equipment
    val objDef = entry.obj.Definition
    val faction = GlobalDefinitions.isFactionEquipment(objDef)
    GlobalDefinitions.isCavernEquipment(objDef) || (faction != tplayer.Faction && faction != PlanetSideEmpire.NEUTRAL)
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
    * @param obj the object that has deployed
    */
  def DeploymentActivities(obj : Deployment.DeploymentObject) : Unit = {
    obj match {
      case vehicle : Vehicle =>
        ReloadVehicleAccessPermissions(vehicle) //TODO we should not have to do this imho

        if(obj.Definition == GlobalDefinitions.ams) {
          obj.DeploymentState match {
            case DriveState.Deployed =>
              vehicleService ! VehicleServiceMessage.AMSDeploymentChange(continent)
              sendResponse(PlanetsideAttributeMessage(obj.GUID, 81, 1))
            case DriveState.Undeploying =>
              vehicleService ! VehicleServiceMessage.AMSDeploymentChange(continent)
              sendResponse(PlanetsideAttributeMessage(obj.GUID, 81, 0))
            case DriveState.Mobile | DriveState.State7 =>
            case _ => ;
          }
        }
        if(obj.Definition == GlobalDefinitions.ant) {
            obj.DeploymentState match {
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

                avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(obj.GUID, 52, 0L)) // panel glow off
                avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(obj.GUID, 49, 0L)) // orb particles off
              case DriveState.Mobile | DriveState.State7 | DriveState.Deploying =>
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

  def ClearCurrentAmsSpawnPoint() : Unit = {
    amsSpawnPoint match {
      case Some(_) =>
        sendResponse(
          BattleplanMessage(41378949, "ams", continent.Number, List(BattleDiagramAction(DiagramActionCode.StopDrawing)))
        )
        amsSpawnPoint = None
      case None => ;
    }
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
    * Pay special attention to the details of `BuildingInfoUpdateMessage` when preparing this packet.
    * @see `BuildingInfoUpdateMessage`
    * @see `DensityLevelUpdateMessage`
    * @param continentNumber the zone id
    * @param buildingNumber the building id
    * @param building the building object
    */
  def initFacility(continentNumber : Int, buildingNumber : Int, building : Building) : Unit = {
    building.Actor ! Building.SendMapUpdate(all_clients = false)
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
    sendResponse(BroadcastWarpgateUpdateMessage(continentNumber, buildingNumber, false, false, true))
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
      sendResponse(SetEmpireMessage(PlanetSideGUID(building.ModelId), building.Faction))
      building.Amenities.foreach(amenity => {
        val amenityId = amenity.GUID
        sendResponse(PlanetsideAttributeMessage(amenityId, 50, 0))
        sendResponse(PlanetsideAttributeMessage(amenityId, 51, 0))

        amenity.Definition match {
          case GlobalDefinitions.resource_silo =>
            // Synchronise warning light & silo capacity
            val silo = amenity.asInstanceOf[ResourceSilo]
            sendResponse(PlanetsideAttributeMessage(amenityId, 45, silo.CapacitorDisplay))
            sendResponse(PlanetsideAttributeMessage(amenityId, 47, silo.LowNtuWarningOn))

            if(silo.ChargeLevel == 0) {
              //todo: temporarily disabled until warpgates can bring ANTs from sanctuary, otherwise we'd be stuck in a situation with an unpowered base and no way to get an ANT to refill it.
              //sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(silo.Owner.asInstanceOf[Building].ModelId), 48, 1))
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
        case _ => ;
      }
      //make player invisible (if not, the cadaver sticks out the side in a seated position)
      sendResponse(PlanetsideAttributeMessage(player_guid, 29, 1))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 29, 1))
    }
    PlayerActionsToCancel()
    CancelAllProximityUnits()

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
    * - if anchored
    */
  def PlayerActionsToCancel() : Unit = {
    progressBarUpdate.cancel
    progressBarValue = None
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
    * It adds the `WSA`-current `Player` to the current zone and sends out the expected packets.
    */
  def AvatarCreate() : Unit = {
    player.VehicleSeated = None //TODO temp, until vehicle gating; unseat player else constructor data is messed up
    player.Spawn
    player.Health = 50 //TODO temp
    player.Armor = 25
    val packet = player.Definition.Packet
    val dcdata = packet.DetailedConstructorData(player).get
    val player_guid = player.GUID
    sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, player_guid, dcdata))
    continent.Population ! Zone.Population.Spawn(avatar, player)
    avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.LoadPlayer(player_guid, ObjectClass.avatar, player_guid, packet.ConstructorData(player).get, None))
    log.debug(s"ObjectCreateDetailedMessage: $dcdata")
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
    obj
  }

  /**
    * Remove items from a deceased player that is not expected to be found on a corpse.
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
    * If the server thinks the player is already on his sanctuary continent,
    * it will disconnect the player under the assumption that an error has occurred.
    * Eventually, this functionality should support better error-handling before it jumps to the conclusion:
    * "Disconnecting the client is the safest option."
    * @see `Zones.SanctuaryZoneNumber`
    * @param tplayer the player
    * @param currentZone the current cone number
    */
  def RequestSanctuaryZoneSpawn(tplayer : Player, currentZone : Int) : Unit = {
    val sanctNumber = Zones.SanctuaryZoneNumber(tplayer.Faction)
    if(currentZone == sanctNumber) {
      sendResponse(DisconnectMessage("Player failed to load on faction's sanctuary continent.  Please relog."))
    }
    else {
      cluster ! Zone.Lattice.RequestSpawnPoint(sanctNumber, tplayer, 7)
    }
  }

  /**
    * Start using a proximity-base service.
    * Special note is warranted in the case of a medical terminal or an advanced medical terminal.
    * @param terminal the proximity-based unit
    */
  def StartUsingProximityUnit(terminal : Terminal with ProximityUnit) : Unit = {
    val term_guid = terminal.GUID
    if(!usingProximityTerminal.contains(term_guid)) {
      usingProximityTerminal += term_guid
      terminal.Definition match {
        case GlobalDefinitions.adv_med_terminal | GlobalDefinitions.medical_terminal =>
          usingMedicalTerminal = Some(term_guid)
        case _ =>
          SetDelayedProximityUnitReset(terminal)
      }
      terminal.Actor ! CommonMessages.Use(player)
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
    if(usingProximityTerminal.contains(term_guid)) {
      usingProximityTerminal -= term_guid
      ClearDelayedProximityUnitReset(term_guid)
      if(usingMedicalTerminal.contains(term_guid)) {
        usingMedicalTerminal = None
      }
      terminal.Actor ! CommonMessages.Unuse(player)
    }
  }

  /**
    * For pure proximity-based units and services, a manual attempt at cutting off the functionality.
    * First, if an existing timer can be found, cancel it.
    * Then, create a new timer.
    * If this timer completes, a message will be sent that will attempt to disassociate from the target proximity unit.
    * @param terminal the proximity-based unit
    */
  def SetDelayedProximityUnitReset(terminal : Terminal with ProximityUnit) : Unit = {
    val terminal_guid = terminal.GUID
    ClearDelayedProximityUnitReset(terminal_guid)
    import scala.concurrent.ExecutionContext.Implicits.global
    delayedProximityTerminalResets += terminal_guid ->
      context.system.scheduler.scheduleOnce(3000 milliseconds, self, DelayedProximityUnitStop(terminal))
  }

  /**
    * For pure proximity-based units and services, disable any manual attempt at cutting off the functionality.
    * If an existing timer can be found, cancel it.
    * @param terminal_guid the proximity-based unit
    */
  def ClearDelayedProximityUnitReset(terminal_guid : PlanetSideGUID) : Unit = {
    delayedProximityTerminalResets.get(terminal_guid) match {
      case Some(task) =>
        task.cancel
        delayedProximityTerminalResets -= terminal_guid
      case None => ;
    }
  }

  /**
    * Cease all current interactions with proximity-based units.
    * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
    * This operations may invoke callback messages.
    * @see `postStop`<br>
    *       `Terminal.StopProximityEffects`
    */
  def CancelAllProximityUnits() : Unit = {
    delayedProximityTerminalResets.foreach({case(term_guid, task) =>
      task.cancel
      delayedProximityTerminalResets -= term_guid
    })
    usingProximityTerminal.foreach(term_guid => {
      StopUsingProximityUnit(continent.GUID(term_guid).get.asInstanceOf[ProximityTerminal])
    })
  }

  /**
    * Determine which functionality to pursue, by being given a generic proximity-functional unit
    * and determinig which kind of unit is being utilized.
    * @param terminal the proximity-based unit
    */
  def SelectProximityUnit(terminal : Terminal with ProximityUnit) : Unit = {
    terminal.Definition match {
      case GlobalDefinitions.adv_med_terminal | GlobalDefinitions.medical_terminal =>
        ProximityMedicalTerminal(terminal)

      case GlobalDefinitions.crystals_health_a | GlobalDefinitions.crystals_health_b =>
        SetDelayedProximityUnitReset(terminal)
        ProximityHealCrystal(terminal)

      case GlobalDefinitions.repair_silo =>
        SetDelayedProximityUnitReset(terminal)
        //TODO insert vehicle repair here; see ProximityMedicalTerminal for example

      case _ => ;
    }
  }

  /**
    * When standing on the platform of a(n advanced) medical terminal,
    * resotre the player's health and armor points (when they need their health and armor points restored).
    * If the player is both fully healed and fully repaired, stop using the terminal.
    * @param unit the medical terminal
    */
  def ProximityMedicalTerminal(unit : Terminal with ProximityUnit) : Unit = {
    val healthFull : Boolean = if(player.Health < player.MaxHealth) {
      HealAction(player)
    }
    else {
      true
    }
    val armorFull : Boolean = if(player.Armor < player.MaxArmor) {
      ArmorRepairAction(player)
    }
    else {
      true
    }
    if(healthFull && armorFull) {
      log.info(s"${player.Name} is all fixed up")
      StopUsingProximityUnit(unit)
    }
  }

  /**
    * When near a red cavern crystal, resotre the player's health (when they need their health restored).
    * If the player is fully healed, stop using the crystal.
    * @param unit the healing crystal
    */
  def ProximityHealCrystal(unit : Terminal with ProximityUnit) : Unit = {
    val healthFull : Boolean = if(player.Health < player.MaxHealth) {
      HealAction(player)
    }
    else {
      true
    }
    if(healthFull) {
      log.info(s"${player.Name} is all healed up")
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
    log.info(s"Dispensing health to ${tplayer.Name} - <3")
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
    log.info(s"Dispensing armor to ${tplayer.Name} - c[=")
    val player_guid = tplayer.GUID
    tplayer.Armor = tplayer.Armor + repairValue
    sendResponse(PlanetsideAttributeMessage(player_guid, 4, tplayer.Armor))
    avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 4, tplayer.Armor))
    tplayer.Armor == tplayer.MaxArmor
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
    * The risk of the server getting caught in a state where the packets dispatched to the client are alwaysd bundled is posible.
    * Starting the bundling functionality but forgetting to transition into a state where it is deactivated can lead to this problem.
    * No packets except for `KeepAliveMessage` will ever be sent until the ever-accumulating packets overflow.
    * To avoid this state, whenever a `KeepAliveMessage` is sent, the packet collector empties its current contents to the network.
    * @see `StartBundlingPackets`<br>
    *       `StopBundlingPackets`<br>
  *         `clientKeepAlive`
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

  def GetPlayerHackSpeed(obj: PlanetSideServerObject with Hackable): Float = {
    val playerHackLevel = GetPlayerHackLevel()
    val timeToHack = obj.HackDuration(playerHackLevel)

    if(timeToHack == 0) {
      log.warn(s"Player ${player.GUID} tried to hack an object ${obj.GUID} - ${obj.Definition.Name} that they don't have the correct hacking level for")
      0f
    }

    // 250 ms per tick on the hacking progress bar
    val ticks = (timeToHack * 1000) / 250
    100f / ticks
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
  private final case class DelayedProximityUnitStop(unit : Terminal with ProximityUnit)
  private final case class UnregisterCorpseOnVehicleDisembark(corpse : Player)

  /**
    * A message that indicates the user is using a remote electronics kit to hack some server object.
    * Each time this message is sent for a given hack attempt counts as a single "tick" of progress.
    * The process of "making progress" with a hack involves sending this message repeatedly until the progress is 100 or more.
    * @param tplayer the player
    * @param target the object being hacked
    * @param tool_guid the REK
    * @param delta how much the progress bar value changes each tick
    * @param completeAction a custom action performed once the hack is completed
    * @param tickAction an optional action is is performed for each tick of progress
    */
  private final case class ItemHacking(tplayer : Player,
                                       target : PlanetSideServerObject,
                                       tool_guid : PlanetSideGUID,
                                       delta : Float,
                                       completeAction : () => Unit,
                                       tickAction : Option[() => Unit] = None)

  private final case class NtuCharging(tplayer: Player,
                                       vehicle: Vehicle)
  private final case class NtuDischarging(tplayer: Player, vehicle: Vehicle, silo_guid: PlanetSideGUID)
}
