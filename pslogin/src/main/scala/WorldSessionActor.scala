// Copyright (c) 2017 PSForever
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet._
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import services.ServiceManager.Lookup
import net.psforever.objects._
import net.psforever.objects.equipment._
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{MatrixTerminalDefinition, Terminal}
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.objects.vehicles.{AccessPermissionGroup, Utility, VehicleLockState}
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles.{AccessPermissionGroup, VehicleLockState}
import net.psforever.objects.zones.{InterstellarCluster, Zone}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import services._
import services.avatar.{AvatarAction, AvatarResponse, AvatarServiceMessage, AvatarServiceResponse}
import services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import services.vehicle.{VehicleAction, VehicleResponse, VehicleServiceMessage, VehicleServiceResponse}

import scala.annotation.tailrec
import scala.util.Success

class WorldSessionActor extends Actor with MDCContextAware {
  import WorldSessionActor._
  private[this] val log = org.log4s.getLogger

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var avatarService : ActorRef = ActorRef.noSender
  var localService : ActorRef = ActorRef.noSender
  var vehicleService : ActorRef = ActorRef.noSender
  var taskResolver : ActorRef = Actor.noSender
  var galaxy : ActorRef = Actor.noSender
  var continent : Zone = null
  var progressBarValue : Option[Float] = None
  var shooting : Option[PlanetSideGUID] = None
  var accessedContainer : Option[PlanetSideGameObject with Container] = None

  var clientKeepAlive : Cancellable = DefaultCancellable.obj
  var progressBarUpdate : Cancellable = DefaultCancellable.obj

  override def postStop() = {
    if(clientKeepAlive != null)
      clientKeepAlive.cancel()
      localService ! Service.Leave()
      vehicleService ! Service.Leave()
      avatarService ! Service.Leave()
      LivePlayerList.Remove(sessionId) match {
        case Some(tplayer) =>
          tplayer.VehicleSeated match {
            case Some(vehicle_guid) =>
              //TODO do this at some other time
              vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(tplayer.GUID, 0, true, vehicle_guid))
            case None => ;
          }
          tplayer.VehicleOwned match {
            case Some(vehicle_guid) =>
              continent.GUID(vehicle_guid) match {
                case Some(vehicle : Vehicle) =>
                  vehicle.Owner = None
                  //TODO temporary solution; to un-own, permit driver seat to Empire access level
                  vehicle.PermissionGroup(10, VehicleLockState.Empire.id)
                  vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.SeatPermissions(tplayer.GUID, vehicle_guid, 10, VehicleLockState.Empire.id))
                case _ => ;
              }
            case None => ;
          }

          if(tplayer.HasGUID) {
            val guid = tplayer.GUID
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(guid, guid))
            taskResolver ! GUIDTask.UnregisterAvatar(tplayer)(continent.GUID)
            //TODO normally, the actual player avatar persists a minute or so after the user disconnects
          }

        case None => ;
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
      galaxy = endpoint
      log.info("ID: " + sessionId + " Got galaxy service " + endpoint)

    case ctrl @ ControlPacket(_, _) =>
      handlePktContainer(ctrl)
    case game @ GamePacket(_, _, _) =>
      handlePktContainer(game)
      // temporary hack to keep the client from disconnecting
    case PokeClient() =>
      sendResponse(KeepAliveMessage())

    case AvatarServiceResponse(_, guid, reply) =>
      reply match {
        case AvatarResponse.ArmorChanged(suit, subtype) =>
          if(player.GUID != guid) {
            sendResponse(ArmorChangedMessage(guid, suit, subtype))
          }

        case AvatarResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) =>
          if(player.GUID != guid) {
            sendResponse(ObjectDetachMessage(weapon_guid, previous_guid, Vector3(0,0,0), 0f, 0f, 0f))
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
          if(player.GUID != guid) {
            sendResponse(ChangeFireModeMessage(item_guid, mode))
          }

        case AvatarResponse.ChangeFireState_Start(weapon_guid) =>
          if(player.GUID != guid) {
            sendResponse(ChangeFireStateMessage_Start(weapon_guid))
          }

        case AvatarResponse.ChangeFireState_Stop(weapon_guid) =>
          if(player.GUID != guid) {
            sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
          }

        case AvatarResponse.ConcealPlayer() =>
          if(player.GUID != guid) {
            sendResponse(GenericObjectActionMessage(guid, 36))
          }

        case AvatarResponse.EquipmentInHand(slot, item) =>
          if(player.GUID != guid) {
            val definition = item.Definition
            sendResponse(
              ObjectCreateMessage(
                definition.ObjectId,
                item.GUID,
                ObjectCreateMessageParent(guid, slot),
                definition.Packet.ConstructorData(item).get
              )
            )
          }

        case AvatarResponse.EquipmentOnGround(pos, orient, item_id, item_guid, item_data) =>
          if(player.GUID != guid) {
            sendResponse(
              ObjectCreateMessage(
                item_id,
                item_guid,
                DroppedItemData(PlacementData(pos, Vector3(0f, 0f, orient.z)), item_data)
              )
            )
          }

        case AvatarResponse.LoadPlayer(pdata) =>
          if(player.GUID != guid) {
            sendResponse(ObjectCreateMessage(ObjectClass.avatar, guid, pdata))
          }

        case AvatarResponse.ObjectDelete(item_guid, unk) =>
          if(player.GUID != guid) {
            sendResponse(ObjectDeleteMessage(item_guid, unk))
          }

        case AvatarResponse.ObjectHeld(slot) =>
          if(player.GUID != guid) {
            sendResponse(ObjectHeldMessage(guid, slot, false))
          }

        case AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value) =>
          if(player.GUID != guid) {
            sendResponse(PlanetsideAttributeMessage(guid, attribute_type, attribute_value))
          }

        case AvatarResponse.PlayerState(msg, spectating, weaponInHand) =>
          if(player.GUID != guid) {
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
                  0,
                  msg.is_crouching,
                  msg.is_jumping,
                  msg.jump_thrust,
                  msg.is_cloaked
                )
              )
              player.lastSeenStreamMessage(guid.guid) = now
            }
          }

        case AvatarResponse.Reload(item_guid) =>
          if(player.GUID != guid) {
            sendResponse(ReloadMessage(item_guid, 1, 0))
          }

        case AvatarResponse.WeaponDryFire(weapon_guid) =>
          if(player.GUID != guid) {
            sendResponse(WeaponDryFireMessage(weapon_guid))
          }

        case _ => ;
      }

    case LocalServiceResponse(_, guid, reply) =>
      reply match {
        case LocalResponse.DoorOpens(door_guid) =>
          if(player.GUID != guid) {
            sendResponse(GenericObjectStateMsg(door_guid, 16))
          }

        case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
          sendResponse(GenericObjectStateMsg(door_guid, 17))

        case LocalResponse.HackClear(target_guid, unk1, unk2) =>
          sendResponse(HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2))

        case LocalResponse.HackObject(target_guid, unk1, unk2) =>
          if(player.GUID != guid) {
            sendResponse(HackMessage(0, target_guid, guid, 100, unk1, HackState.Hacked, unk2))
          }

        case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
          sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

        case _ => ;
      }

    case VehicleServiceResponse(_, guid, reply) =>
      reply match {
        case VehicleResponse.Awareness(vehicle_guid) =>
          //resets exclamation point fte marker (once)
          sendResponse(PlanetsideAttributeMessage(guid, 21, vehicle_guid.guid.toLong))

        case VehicleResponse.ChildObjectState(object_guid, pitch, yaw) =>
          if(player.GUID != guid) {
            sendResponse(ChildObjectStateMessage(object_guid, pitch, yaw))
          }

        case VehicleResponse.DismountVehicle(unk1, unk2) =>
          if(player.GUID != guid) {
            sendResponse(DismountVehicleMsg(guid, unk1, unk2))
          }

        case VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos) =>
          if(player.GUID != guid) {
            sendResponse(DeployRequestMessage(guid, object_guid, state, unk1, unk2, pos))
          }

        case VehicleResponse.InventoryState(obj, parent_guid, start, con_data) =>
          if(player.GUID != guid) {
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

        case VehicleResponse.KickPassenger(unk1, unk2, vehicle_guid) =>
          sendResponse(DismountVehicleMsg(guid, unk1, unk2))
          if(guid == player.GUID) {
            continent.GUID(vehicle_guid) match {
              case Some(obj : Vehicle) =>
                UnAccessContents(obj)
              case _ => ;
            }
          }

        case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) =>
          //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
          if(player.GUID != guid) {
            sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
            ReloadVehicleAccessPermissions(vehicle)
          }

        case VehicleResponse.MountVehicle(vehicle_guid, seat) =>
          if(player.GUID != guid) {
            sendResponse(ObjectAttachMessage(vehicle_guid, guid, seat))
          }

        case VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission) =>
          if(player.GUID != guid) {
            sendResponse(PlanetsideAttributeMessage(vehicle_guid, seat_group, permission))
          }

        case VehicleResponse.StowEquipment(vehicle_guid, slot, item_type, item_guid, item_data) =>
          if(player.GUID != guid) {
            //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
            sendResponse(
              ObjectCreateDetailedMessage(item_type, item_guid, ObjectCreateMessageParent(vehicle_guid, slot), item_data)
            )
          }

        case VehicleResponse.UnloadVehicle(vehicle_guid) =>
          sendResponse(ObjectDeleteMessage(vehicle_guid, 0))

        case VehicleResponse.UnstowEquipment(item_guid) =>
          if(player.GUID != guid) {
            //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
            sendResponse(ObjectDeleteMessage(item_guid, 0))
          }

        case VehicleResponse.VehicleState(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6) =>
          if(player.GUID != guid) {
            sendResponse(VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6))
            if(player.VehicleSeated.contains(vehicle_guid)) {
              player.Position = pos
            }
          }

        case _ => ;
      }

    case Deployment.CanDeploy(obj, state) =>
      val vehicle_guid = obj.GUID
      if(state == DriveState.Deploying) {
        log.info(s"DeployRequest: $obj transitioning to deploy state")
        obj.Velocity = Some(Vector3.Zero) //no velocity
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        import scala.concurrent.duration._
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
        import scala.concurrent.duration._
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(obj.UndeployTime milliseconds, obj.Actor, Deployment.TryUndeploy(DriveState.Mobile))
      }
      else if(state == DriveState.Mobile) {
        log.info(s"DeployRequest: $obj is Mobile")
        sendResponse(DeployRequestMessage(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DeployRequest(player.GUID, vehicle_guid, state, 0, false, Vector3.Zero))
        //...
      }
      else {
        CanNotChangeDeployment(obj, state, "incorrect undeploy state")
      }

    case Deployment.CanNotChangeDeployment(obj, state, reason) =>
      CanNotChangeDeployment(obj, state, reason)

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
          //tplayer.VehicleSeated = Some(obj_guid)
          sendResponse(PlanetsideAttributeMessage(obj_guid, 0, 1000L)) //health of mech
          sendResponse(ObjectAttachMessage(obj_guid, player_guid, seat_num))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seat_num))

        case Mountable.CanMount(obj : Vehicle, seat_num) =>
          val obj_guid : PlanetSideGUID = obj.GUID
          val player_guid : PlanetSideGUID = tplayer.GUID
          log.info(s"MountVehicleMsg: $player_guid mounts $obj_guid @ $seat_num")
          vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(obj_guid) //clear all deconstruction timers
          //tplayer.VehicleSeated = Some(obj_guid)
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
          sendResponse(DismountVehicleMsg(player_guid, seat_num, false))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, seat_num, false))

        case Mountable.CanDismount(obj : Vehicle, seat_num) =>
          val player_guid : PlanetSideGUID = tplayer.GUID
          if(player_guid == player.GUID) {
            //disembarking self
            log.info(s"DismountVehicleMsg: $player_guid dismounts $obj @ $seat_num")
            sendResponse(DismountVehicleMsg(player_guid, seat_num, false))
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, seat_num, false))
            UnAccessContents(obj)
          }
          else {
            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(player_guid, seat_num, true, obj.GUID))
          }
          if(obj.Seats.values.count(seat => seat.isOccupied) == 0) {
            vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(obj, continent, 600L) //start vehicle decay (10m)
          }

        case Mountable.CanDismount(obj : Mountable, _) =>
          log.warn(s"DismountVehicleMsg: $obj is some generic mountable object and nothing will happen")

        case Mountable.CanNotMount(obj, seat_num) =>
          log.warn(s"MountVehicleMsg: $tplayer attempted to mount $obj's seat $seat_num, but was not allowed")

        case Mountable.CanNotDismount(obj, seat_num) =>
          log.warn(s"DismountVehicleMsg: $tplayer attempted to dismount $obj's seat $seat_num, but was not allowed")
      }

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      order match {
        case Terminal.BuyExosuit(exosuit, subtype) => //refresh armor points
          if(tplayer.ExoSuit == exosuit) {
            if(Loadout.DetermineSubtype(tplayer) != subtype) {
              //special case: MAX suit switching to a different MAX suit; we need to change the main weapon
              sendResponse(ArmorChangedMessage(tplayer.GUID, exosuit, subtype))
              avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
              val arms = tplayer.Slot(0).Equipment.get
              val putTask = PutEquipmentInSlot(tplayer, Tool(GlobalDefinitions.MAXArms(subtype, tplayer.Faction)), 0)
              taskResolver ! DelayedObjectHeld(tplayer, 0, List(TaskResolver.GiveTask(putTask.task, putTask.subs :+ RemoveEquipmentFromSlot(tplayer, arms, 0))))
            }
            //outside of the MAX condition above, we should seldom reach this point through conventional methods
            tplayer.Armor = tplayer.MaxArmor
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor))
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, true))
          }
          else { //load a complete new exo-suit and shuffle the inventory around
            val originalSuit = tplayer.ExoSuit
            //save inventory before it gets cleared (empty holsters)
            val dropPred = DropPredicate(tplayer)
            val (dropHolsters, beforeHolsters) = clearHolsters(tplayer.Holsters().iterator).partition(dropPred)
            val (dropInventory, beforeInventory) = tplayer.Inventory.Clear().partition(dropPred)
            //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
            Player.SuitSetup(tplayer, exosuit)
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
                  avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(player.GUID, index, obj))
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
            val orient = tplayer.Orientation
            ((dropHolsters ++ dropInventory).map(_.obj) ++ drop).foreach(obj => {
              continent.Ground ! Zone.DropItemOnGround(obj, pos, Vector3(0f, 0f, orient.z))
              sendResponse(ObjectDetachMessage(tplayer.GUID, obj.GUID, pos, 0f, 0f, orient.z))
              val objDef = obj.Definition
              avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.EquipmentOnGround(tplayer.GUID, pos, orient, objDef.ObjectId, obj.GUID, objDef.Packet.ConstructorData(obj).get))
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
          sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.InfantryLoadout, true))
          val dropPred = DropPredicate(tplayer)
          val (dropHolsters, beforeHolsters) = clearHolsters(tplayer.Holsters().iterator).partition(dropPred)
          val (dropInventory, beforeInventory) = tplayer.Inventory.Clear().partition(dropPred)
          val (_, afterHolsters) = holsters.partition(dropPred) //dropped items are lost
          val (_, afterInventory) = inventory.partition(dropPred) //dropped items are lost
          val beforeFreeHand = tplayer.FreeHand.Equipment
          //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
          Player.SuitSetup(tplayer, exosuit)
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
          sendResponse(ArmorChangedMessage(tplayer.GUID, exosuit, 0))
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
          val orient = tplayer.Orientation
          ((dropHolsters ++ dropInventory).map(_.obj)).foreach(obj => {
            continent.Ground ! Zone.DropItemOnGround(obj, pos, Vector3(0f, 0f, orient.z))
            sendResponse(ObjectDetachMessage(tplayer.GUID, obj.GUID, pos, 0f, 0f, orient.z))
            val objDef = obj.Definition
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.EquipmentOnGround(tplayer.GUID, pos, orient, objDef.ObjectId, obj.GUID, objDef.Packet.ConstructorData(obj).get))
          })
          sendResponse(ItemTransactionResultMessage (msg.terminal_guid, TransactionType.InfantryLoadout, true))

        case Terminal.LearnCertification(cert, cost) =>
          if(!player.Certifications.contains(cert)) {
            log.info(s"$tplayer is learning the $cert certification for $cost points")
            tplayer.Certifications += cert
            sendResponse(PlanetsideAttributeMessage(tplayer.GUID, 24, cert.id.toLong))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, true))
          }
          else {
            log.warn(s"$tplayer already knows the $cert certification, so he can't learn it")
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false))
          }

        case Terminal.SellCertification(cert, cost) =>
          if(player.Certifications.contains(cert)) {
            log.info(s"$tplayer is forgetting the $cert certification for $cost points")
            tplayer.Certifications -= cert
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
                if(!tplayer.Implants.exists({slot => slot.Implant == implant_type})) { //no duplicates
                  tplayer.InstallImplant(implant)
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
            sendResponse(AvatarImplantMessage(tplayer.GUID, 0, slot, implant_type.id))
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
                tplayer.UninstallImplant(implant_type)
              )
            case None =>
              (None, None)
          }

          if(interface.contains(terminal_guid.guid) && slotNumber.isDefined) {
            val slot = slotNumber.get
            log.info(s"$tplayer is selling $implant_type - take from slot $slot")
            sendResponse(AvatarImplantMessage(tplayer.GUID, 1, slot, 0))
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

        case Terminal.NoDeal() =>
          log.warn(s"$tplayer made a request but the terminal rejected the order $msg")
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false))
      }

    case VehicleSpawnPad.ConcealPlayer =>
      sendResponse(GenericObjectActionMessage(player.GUID, 36))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ConcealPlayer(player.GUID))

    case VehicleSpawnPad.LoadVehicle(vehicle, _/*pad*/) =>
      val player_guid = player.GUID
      val definition = vehicle.Definition
      val objedtId = definition.ObjectId
      val vehicle_guid = vehicle.GUID
      val vdata = definition.Packet.ConstructorData(vehicle).get
      sendResponse(ObjectCreateMessage(objedtId, vehicle_guid, vdata))
      continent.Transport ! Zone.SpawnVehicle(vehicle)
      vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.LoadVehicle(player_guid, vehicle, objedtId, vehicle_guid, vdata))
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 1L)) //mount points off?
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 1L)) //mount points off?
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 21, player_guid.guid)) //fte and ownership?
      //sendResponse(ObjectAttachMessage(vehicle_guid, player_guid, 0))
      vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(vehicle_guid) //cancel queue timeout delay
      vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(vehicle, continent, 21L) //temporary drive away from pad delay
      vehicle.Actor ! Mountable.TryMount(player, 0)

    case VehicleSpawnPad.PlayerSeatedInVehicle(vehicle) =>
      vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(vehicle, continent, 21L) //sitting in the vehicle clears the drive away delay
      val vehicle_guid = vehicle.GUID
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 0L)) //mount points on?
      //sendResponse(PlanetsideAttributeMessage(vehicle_guid, 0, vehicle.Definition.MaxHealth)))
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 68, 0L)) //???
      sendResponse(PlanetsideAttributeMessage(vehicle_guid, 113, 0L)) //???
      ReloadVehicleAccessPermissions(vehicle)

    case VehicleSpawnPad.SpawnPadBlockedWarning(vehicle, warning_count) =>
      if(warning_count > 2) {
        sendResponse(TriggerSoundMessage(TriggeredSound.Unknown14, vehicle.Position, 20, 1f))
        sendResponse(
          ChatMsg(ChatMessageType.CMT_TELL, true, "", "\\#FYour vehicle is blocking the spawn pad, and will be deconstructed if not moved.", None)
        )
      }

    case VehicleSpawnPad.SpawnPadUnblocked(vehicle_guid) =>
      //vehicle has moved away from spawn pad after initial spawn
      vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(vehicle_guid) //cancel temporary drive away from pad delay

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

    case InterstellarCluster.GiveWorld(zoneId, zone) =>
      log.info(s"Zone $zoneId has been loaded")
      player.Continent = zoneId
      continent = zone
      taskResolver ! RegisterAvatar(player)

    case PlayerLoaded(tplayer) =>
      log.info(s"Player $tplayer has been loaded")
      //init for whole server
      galaxy ! InterstellarCluster.RequestClientInitialization(tplayer)

    case PlayerFailedToLoad(tplayer) =>
      player.Continent match {
        case _ =>
          failWithError(s"$tplayer failed to load anywhere")
      }

    case VehicleLoaded(_/*vehicle*/) => ;
      //currently being handled by VehicleSpawnPad.LoadVehicle during testing phase

    case Zone.ClientInitialization(zone) =>
      val continentNumber = zone.Number
      val poplist = LivePlayerList.ZonePopulation(continentNumber, _ => true)
      val popTR = poplist.count(_.Faction == PlanetSideEmpire.TR)
      val popNC = poplist.count(_.Faction == PlanetSideEmpire.NC)
      val popVS = poplist.count(_.Faction == PlanetSideEmpire.VS)
      val popBO = poplist.size - popTR - popNC - popVS

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

    case InterstellarCluster.ClientInitializationComplete(tplayer)=>
      //PropertyOverrideMessage
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 112, 1))
      sendResponse(ReplicationStreamMessage(5, Some(6), Vector(SquadListing()))) //clear squad list
      sendResponse(FriendsResponse(FriendAction.InitializeFriendList, 0, true, true, Nil))
      sendResponse(FriendsResponse(FriendAction.InitializeIgnoreList, 0, true, true, Nil))

      //LoadMapMessage will cause the client to send back a BeginZoningMessage packet (see below)
      sendResponse(LoadMapMessage(continent.Map.Name, continent.Id, 40100,25,true,3770441820L))
      log.info("Load the now-registered player")
      //load the now-registered player
      tplayer.Spawn
      tplayer.Health = 50
      val dcdata = tplayer.Definition.Packet.DetailedConstructorData(tplayer).get
      sendResponse(ObjectCreateDetailedMessage(ObjectClass.avatar, tplayer.GUID, dcdata))
      avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.LoadPlayer(tplayer.GUID, tplayer.Definition.Packet.ConstructorData(tplayer).get))
      log.debug(s"ObjectCreateDetailedMessage: $dcdata")

    case SetCurrentAvatar(tplayer) =>
      val guid = tplayer.GUID
      LivePlayerList.Assign(continent.Number, sessionId, guid)
      sendResponse(SetCurrentAvatarMessage(guid,0,0))
      sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(0), 82, 0))
      sendResponse(CreateShortcutMessage(guid, 1, 0, true, Shortcut.MEDKIT))
      sendResponse(ChangeShortcutBankMessage(guid, 0))
      //FavoritesMessage
      sendResponse(SetChatFilterMessage(ChatChannel.Local, false, ChatChannel.values.toList)) //TODO will not always be "on"
      sendResponse(AvatarDeadStateMessage(DeadState.Nothing, 0,0, tplayer.Position, 0, true))
      sendResponse(PlanetsideAttributeMessage(guid, 53, 1))
      sendResponse(AvatarSearchCriteriaMessage(guid, List(0,0,0,0,0,0)))
      (1 to 73).foreach( i => {
        sendResponse(PlanetsideAttributeMessage(PlanetSideGUID(i), 67, 0))
      })
      //AvatarStatisticsMessage
      //SquadDefinitionActionMessage and SquadDetailDefinitionUpdateMessage
      //MapObjectStateBlockMessage and ObjectCreateMessage
      //TacticsMessage

      sendResponse(ChatMsg(ChatMessageType.CMT_EXPANSIONS, true, "", "1 on", None)) //CC on

    case Zone.ItemFromGround(tplayer, item) =>
      val obj_guid = item.GUID
      val player_guid = tplayer.GUID
      tplayer.Fit(item) match {
        case Some(slot) =>
          tplayer.Slot(slot).Equipment = item
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(player_guid, obj_guid))
          val definition = item.Definition
          sendResponse(
            ObjectCreateDetailedMessage(
              definition.ObjectId,
              obj_guid,
              ObjectCreateMessageParent(player_guid, slot),
              definition.Packet.DetailedConstructorData(item).get
            )
          )
          if(tplayer.VisibleSlots.contains(slot)) {
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.EquipmentInHand(player_guid, slot, item))
          }
        case None =>
          continent.Ground ! Zone.DropItemOnGround(item, item.Position, item.Orientation) //restore
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
          sendResponse(HackMessage(0, target.GUID, player.GUID, 100, 1114636288L, HackState.Hacked, 8L))
          completeAction()
        }
        else { //continue next tick
          tickAction.getOrElse(() => Unit)()
          progressBarValue = Some(progressBarVal)
          import scala.concurrent.duration._
          import scala.concurrent.ExecutionContext.Implicits.global
          progressBarUpdate = context.system.scheduler.scheduleOnce(250 milliseconds, self, ItemHacking(tplayer, target, tool_guid, delta, completeAction))
        }
      }

    case ResponseToSelf(pkt) =>
      log.info(s"Received a direct message: $pkt")
      sendResponse(pkt)

    case default =>
     log.warn(s"Invalid packet class received: $default")
  }

  def handlePkt(pkt : PlanetSidePacket) : Unit = pkt match {
    case ctrl : PlanetSideControlPacket =>
      handleControlPkt(ctrl)
    case game : PlanetSideGamePacket =>
      handleGamePkt(game)
    case default => log.error(s"Invalid packet class received: $default")
  }

  def handlePktContainer(pkt : PlanetSidePacketContainer) : Unit = pkt match {
    case ctrl @ ControlPacket(opcode, ctrlPkt) =>
      handleControlPkt(ctrlPkt)
    case game @ GamePacket(opcode, seq, gamePkt) =>
      handleGamePkt(gamePkt)
    case default => log.warn(s"Invalid packet container class received: $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case SlottedMetaPacket(slot, subslot, innerPacket) =>
        sendResponse(SlottedMetaAck(slot, subslot))

        PacketCoding.DecodePacket(innerPacket) match {
          case Failure(e) =>
            log.error(s"Failed to decode inner packet of SlottedMetaPacket: $e")
          case Successful(v) =>
            handlePkt(v)
        }
      case sync @ ControlSync(diff, unk, f1, f2, f3, f4, fa, fb) =>
        log.debug(s"SYNC: $sync")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(ControlSyncResp(diff, serverTick, fa, fb, fb, fa))
      case MultiPacket(packets) =>
        packets.foreach { pkt =>
          PacketCoding.DecodePacket(pkt) match {
            case Failure(e) =>
              log.error(s"Failed to decode inner packet of MultiPacket: $e")
            case Successful(v) =>
              handlePkt(v)
          }
        }
      case MultiPacketEx(packets) =>
        packets.foreach { pkt =>
          PacketCoding.DecodePacket(pkt) match {
            case Failure(e) =>
              log.error(s"Failed to decode inner packet of MultiPacketEx: $e")
            case Successful(v) =>
              handlePkt(v)
          }
        }

      case RelatedA0(subslot) =>
        log.error(s"Client not ready for last control packet with subslot $subslot; potential system disarray")

      case RelatedB0(subslot) =>
        log.trace(s"Good control packet received $subslot")

      case TeardownConnection(_) =>
        log.info("Good bye")

      case default =>
        log.warn(s"Unhandled ControlPacket $default")
    }
  }

  var player : Player = null

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>
      val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
      log.info(s"New world login to $server with Token:$token. $clientVersion")
      //TODO begin temp player character auto-loading; remove later
      import net.psforever.objects.GlobalDefinitions._
      player = Player("TestCharacter"+sessionId.toString, PlanetSideEmpire.VS, CharacterGender.Female, 41, 1)
      //player.Position = Vector3(3674.8438f, 2726.789f, 91.15625f)
      //player.Position = Vector3(3523.039f, 2855.5078f, 90.859375f)
      player.Position = Vector3(3561.0f, 2854.0f, 90.859375f)
      player.Orientation = Vector3(0f, 0f, 90f)
      player.Certifications += CertificationType.StandardAssault
      player.Certifications += CertificationType.MediumAssault
      player.Certifications += CertificationType.StandardExoSuit
      player.Certifications += CertificationType.AgileExoSuit
      player.Certifications += CertificationType.ReinforcedExoSuit
      player.Certifications += CertificationType.ATV
      player.Certifications += CertificationType.Harasser
      //
      player.Certifications += CertificationType.InfiltrationSuit
      player.Certifications += CertificationType.Sniping
      player.Certifications += CertificationType.AntiVehicular
      player.Certifications += CertificationType.HeavyAssault
      player.Certifications += CertificationType.SpecialAssault
      player.Certifications += CertificationType.EliteAssault
      player.Certifications += CertificationType.GroundSupport
      player.Certifications += CertificationType.GroundTransport
      player.Certifications += CertificationType.Flail
      player.Certifications += CertificationType.Switchblade
      player.Certifications += CertificationType.AssaultBuggy
      player.Certifications += CertificationType.ArmoredAssault1
      player.Certifications += CertificationType.ArmoredAssault2
      player.Certifications += CertificationType.AirCavalryScout
      player.Certifications += CertificationType.AirCavalryAssault
      player.Certifications += CertificationType.AirCavalryInterceptor
      player.Certifications += CertificationType.AirSupport
      player.Certifications += CertificationType.GalaxyGunship
      player.Certifications += CertificationType.Phantasm
      player.Certifications += CertificationType.UniMAX
      AwardBattleExperiencePoints(player, 1000000L)
//      player.ExoSuit = ExoSuitType.MAX //TODO strange issue; divide number above by 10 when uncommenting
      player.Slot(0).Equipment = SimpleItem(remote_electronics_kit) //Tool(GlobalDefinitions.StandardPistol(player.Faction))
      player.Slot(2).Equipment = Tool(punisher) //suppressor
      player.Slot(4).Equipment = Tool(GlobalDefinitions.StandardMelee(player.Faction))
      player.Slot(6).Equipment = AmmoBox(bullet_9mm, 20) //bullet_9mm
      player.Slot(9).Equipment = AmmoBox(rocket, 11) //bullet_9mm
      player.Slot(12).Equipment = AmmoBox(frag_cartridge) //bullet_9mm
      player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
      player.Slot(36).Equipment = AmmoBox(GlobalDefinitions.StandardPistolAmmo(player.Faction))
      player.Slot(39).Equipment = AmmoBox(plasma_cartridge) //SimpleItem(remote_electronics_kit)
      player.Slot(5).Equipment.get.asInstanceOf[LockerContainer].Inventory += 0 -> SimpleItem(remote_electronics_kit)
      //TODO end temp player character auto-loading
      self ! ListAccountCharacters
      import scala.concurrent.duration._
      import scala.concurrent.ExecutionContext.Implicits.global
      clientKeepAlive.cancel
      clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())

    case msg @ CharacterCreateRequestMessage(name, head, voice, gender, empire) =>
      log.info("Handling " + msg)
      sendResponse(ActionResultMessage(true, None))
      self ! ListAccountCharacters

    case msg @ CharacterRequestMessage(charId, action) =>
      log.info("Handling " + msg)
      action match {
        case CharacterRequestAction.Delete =>
          sendResponse(ActionResultMessage(false, Some(1)))
        case CharacterRequestAction.Select =>
          LivePlayerList.Add(sessionId, player)
          //TODO check if can spawn on last continent/location from player?
          //TODO if yes, get continent guid accessors
          //TODO if no, get sanctuary guid accessors and reset the player's expectations
          galaxy ! InterstellarCluster.GetWorld("home3")
        case default =>
          log.error("Unsupported " + default + " in " + msg)
      }

    case KeepAliveMessage(code) =>
      sendResponse(KeepAliveMessage())

    case msg @ BeginZoningMessage() =>
      log.info("Reticulating splines ...")
      //map-specific initializations
      configZone(continent) //todo density
      sendResponse(TimeOfDayMessage(1191182336))
      //custom
      sendResponse(ContinentalLockUpdateMessage(13, PlanetSideEmpire.VS)) // "The VS have captured the VS Sanctuary."
      (1 to 255).foreach(i => { sendResponse(SetEmpireMessage(PlanetSideGUID(i), PlanetSideEmpire.VS)) })

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
      LivePlayerList.ZonePopulation(continent.Number, _ => true).foreach(char => {
        sendResponse(
          ObjectCreateMessage(ObjectClass.avatar, char.GUID, char.Definition.Packet.ConstructorData(char).get)
        )
      })
      //load active vehicles in zone
      continent.Vehicles.foreach(vehicle => {
        val definition = vehicle.Definition
        sendResponse(
          ObjectCreateMessage(
            definition.ObjectId,
            vehicle.GUID,
            definition.Packet.ConstructorData(vehicle).get
          )
        )
        //seat vehicle occupants
        vehicle.Definition.MountPoints.values.foreach(seat_num => {
          vehicle.Seat(seat_num).get.Occupant match {
            case Some(tplayer) =>
              if(tplayer.HasGUID) {
                sendResponse(ObjectAttachMessage(vehicle.GUID, tplayer.GUID, seat_num))
              }
            case None => ;
          }
        })
        ReloadVehicleAccessPermissions(vehicle)
      })
      //implant terminals
      continent.Map.TerminalToInterface.foreach({ case((terminal_guid, interface_guid)) =>
        val parent_guid = PlanetSideGUID(terminal_guid)
        continent.GUID(interface_guid) match {
          case Some(obj : Terminal) =>
            val objDef = obj.Definition
            val obj_uid = objDef.ObjectId
            val obj_data = objDef.Packet.ConstructorData(obj).get
            sendResponse(
              ObjectCreateMessage(
                obj_uid,
                PlanetSideGUID(interface_guid),
                ObjectCreateMessageParent(parent_guid, 1),
                obj_data
              )
            )
          case _ => ;
        }
        //seat terminal occupants
        continent.GUID(terminal_guid) match {
          case Some(obj : Mountable) =>
            obj.MountPoints.foreach({ case((_, seat_num)) =>
              obj.Seat(seat_num).get.Occupant match {
                case Some(tplayer) =>
                  if(tplayer.HasGUID) {
                    sendResponse(ObjectAttachMessage(parent_guid, tplayer.GUID, seat_num))
                  }
                case None => ;
              }
            })
          case _ => ;
        }
      })
      avatarService ! Service.Join(player.Continent)
      localService ! Service.Join(player.Continent)
      vehicleService ! Service.Join(player.Continent)
      self ! SetCurrentAvatar(player)

    case msg @ PlayerStateMessageUpstream(avatar_guid, pos, vel, yaw, pitch, yaw_upper, seq_time, unk3, is_crouching, is_jumping, unk4, is_cloaking, unk5, unk6) =>
      player.Position = pos
      player.Velocity = vel
      player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
      player.FacingYawUpper = yaw_upper
      player.Crouching = is_crouching
      player.Jumping = is_jumping

      val wepInHand : Boolean = player.Slot(player.DrawnSlot).Equipment match {
        case Some(item) => item.Definition == GlobalDefinitions.bolt_driver
        case None => false
      }
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.PlayerState(avatar_guid, msg, player.Spectator, wepInHand))

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
          if(seat.Occupant.contains(player)) { //we're driving the vehicle
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
      //log.info("VehicleState: " + msg)

    case msg @ VehicleSubStateMessage(vehicle_guid, player_guid, vehicle_pos, vehicle_ang, vel, unk1, unk2) =>
      //log.info(s"VehicleSubState: $vehicle_guid, $player_guid, $vehicle_pos, $vehicle_ang, $vel, $unk1, $unk2")

    case msg @ ProjectileStateMessage(projectile_guid, shot_pos, shot_vector, unk1, unk2, unk3, unk4, time_alive) =>
      //log.info("ProjectileState: " + msg)

    case msg @ ReleaseAvatarRequestMessage() =>
      log.info(s"ReleaseAvatarRequest: ${player.GUID} on ${continent.Id} has released")
      sendResponse(PlanetsideAttributeMessage(player.GUID, 6, 1))
      sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, 2, true))

    case msg @ SpawnRequestMessage(u1, u2, u3, u4, u5) =>
      log.info(s"SpawnRequestMessage: $msg")

    case msg @ SetChatFilterMessage(send_channel, origin, whitelist) =>
      log.info("SetChatFilters: " + msg)

    case msg @ ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
      // TODO: Prevents log spam, but should be handled correctly
      if (messagetype != ChatMessageType.CMT_TOGGLE_GM) {
        log.info("Chat: " + msg)
      }

      if(messagetype == ChatMessageType.CMT_SUICIDE) {
        val player_guid = player.GUID
        val pos = player.Position
        sendResponse(PlanetsideAttributeMessage(player_guid, 0, 0))
        sendResponse(PlanetsideAttributeMessage(player_guid, 2, 0))
        sendResponse(DestroyMessage(player_guid, player_guid, PlanetSideGUID(0), pos))
        sendResponse(AvatarDeadStateMessage(DeadState.Dead, 300000, 300000, pos, 2, true))
      }

      if (messagetype == ChatMessageType.CMT_VOICE) {
        sendResponse(ChatMsg(ChatMessageType.CMT_VOICE, false, player.Name, contents, None))
      }

      // TODO: handle this appropriately
      if(messagetype == ChatMessageType.CMT_QUIT) {
        sendResponse(DropCryptoSession())
        sendResponse(DropSession(sessionId, "user quit"))
      }

      // TODO: Depending on messagetype, may need to prepend sender's name to contents with proper spacing
      // TODO: Just replays the packet straight back to sender; actually needs to be routed to recipients!
      sendResponse(ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents))

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
              FindReloadAmmunition(obj, requestedAmmoType, fullMagazine).reverse match {
                case Nil => ;
                case x :: xs =>
                  val (deleteFunc, modifyFunc) : ((Int, AmmoBox)=>Unit, (AmmoBox, Int)=>Unit) = obj match {
                    case (veh : Vehicle) =>
                      (DeleteAmmunitionInVehicle(veh), ModifyAmmunitionInVehicle(veh))
                    case _ =>
                      (DeleteAmmunition(obj), ModifyAmmunition(obj))
                  }
                  val (stowFuncTask, stowFunc) : ((Int, AmmoBox)=>TaskResolver.GiveTask, (Int, AmmoBox)=>Unit) = obj match {
                    case (veh : Vehicle) =>
                      (StowNewAmmunitionInVehicles(veh), StowAmmunitionInVehicles(veh))
                    case _ =>
                      (StowNewAmmunition(obj), StowAmmunition(obj))
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
                  sendResponse(ObjectDetachMessage(tool.GUID, previousBox.GUID, Vector3(0f, 0f, 0f), 0f, 0f, 0f))
                  sendResponse(ObjectDetachMessage(player.GUID, box.GUID, Vector3(0f, 0f, 0f), 0f, 0f, 0f))
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
                        .map({case(_, entry) => entry })
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
                    //TODO split previousBox into AmmoBox objects of appropriate max capacity, e.g., 100 9mm -> 2 x 50 9mm
                    obj.Inventory.Fit(previousBox.Definition.Tile) match {
                      case Some(index) => //put retained magazine in inventory
                        stowFunc(index, previousBox)
                      case None => //drop
                        log.info(s"ChangeAmmo: dropping ammo box $previousBox")
                        val pos = player.Position
                        val orient = player.Orientation
                        sendResponse(
                          ObjectDetachMessage(Service.defaultPlayerGUID, previous_box_guid, pos, 0f, 0f, orient.z)
                        )
                        val orient2 = Vector3(0f, 0f, orient.z)
                        continent.Ground ! Zone.DropItemOnGround(previousBox, pos, orient2)
                        val objDef = previousBox.Definition
                        avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentOnGround(player.GUID, pos, orient2, objDef.ObjectId, previousBox.GUID, objDef.Packet.ConstructorData(previousBox).get))
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
      log.info("DropItem: " + msg)
      player.FreeHand.Equipment match {
        case Some(item) =>
          if(item.GUID == item_guid) {
            val orient : Vector3 = Vector3(0f, 0f, player.Orientation.z)
            player.FreeHand.Equipment = None
            continent.Ground ! Zone.DropItemOnGround(item, player.Position, orient)
            sendResponse(ObjectDetachMessage(player.GUID, item.GUID, player.Position, 0f, 0f, player.Orientation.z))
            val objDef = item.Definition
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentOnGround(player.GUID, player.Position, orient, objDef.ObjectId, item.GUID, objDef.Packet.ConstructorData(item).get))
          }
          else {
            log.warn(s"item in hand was ${item.GUID} but trying to drop $item_guid; nothing will be dropped")
          }
        case None =>
          log.error(s"$player wanted to drop an item, but it was not in hand")
      }

    case msg @ PickupItemMessage(item_guid, player_guid, unk1, unk2) =>
      log.info("PickupItem: " + msg)
      continent.Ground ! Zone.GetItemOnGround(player, item_guid)

    case msg @ ReloadMessage(item_guid, ammo_clip, unk1) =>
      log.info("Reload: " + msg)
      FindContainedWeapon match {
        case (Some(obj), Some(tool : Tool)) =>
          val currentMagazine : Int = tool.Magazine
          val magazineSize : Int = tool.MaxMagazine
          val reloadValue : Int = magazineSize - currentMagazine
          if(magazineSize > 0 && reloadValue > 0) {
            FindReloadAmmunition(obj, tool.AmmoType, reloadValue).reverse match {
              case Nil =>
                log.warn(s"ReloadMessage: no ammunition could be found for $item_guid")
              case list @ x :: xs =>
                val (deleteFunc, modifyFunc) : ((Int, AmmoBox)=>Unit, (AmmoBox, Int)=>Unit) = obj match {
                  case (veh : Vehicle) =>
                    (DeleteAmmunitionInVehicle(veh), ModifyAmmunitionInVehicle(veh))
                  case _ =>
                    (DeleteAmmunition(obj), ModifyAmmunition(obj))
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
      log.info("ObjectHeld: " + msg)
      val before = player.DrawnSlot
      //TODO remove this kludge; explore how to stop BuyExoSuit(Max) sending a tardy ObjectHeldMessage(me, 255)
      if(player.ExoSuit != ExoSuitType.MAX && (player.DrawnSlot = held_holsters) != before) {
        avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ObjectHeld(player.GUID, player.LastDrawnSlot))
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
          if(player.VehicleOwned.contains(object_guid) && vehicle.Owner.contains(player.GUID)) {
            vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(object_guid)
            vehicleService ! VehicleServiceMessage.RequestDeleteVehicle(vehicle, continent)
            log.info(s"RequestDestroy: vehicle $object_guid")
          }
          else {
            log.info(s"RequestDestroy: must own vehicle $object_guid in order to deconstruct it")
          }

        case Some(obj : Equipment) =>
          val findFunc : PlanetSideGameObject with Container => Option[(PlanetSideGameObject with Container, Option[Int])] = FindInLocalContainer(object_guid)

          findFunc(player)
            .orElse(findFunc(player.Locker))
            .orElse(accessedContainer match {
              case Some(parent) =>
                findFunc(parent)
              case None =>
                None
            }) match {
            case Some((parent, Some(slot))) =>
              taskResolver ! RemoveEquipmentFromSlot(parent, obj, slot)
              log.info(s"RequestDestroy: equipment $object_guid")

            case _ =>
              //TODO search for item on ground
              sendResponse(ObjectDeleteMessage(object_guid, 0))
              log.warn(s"RequestDestroy: object $object_guid not found")
          }

        case None =>
          log.warn(s"RequestDestroy: object $object_guid not found")

        case _ =>
          log.warn(s"RequestDestroy: not allowed to delete object $object_guid")
      }

    case msg @ ObjectDeleteMessage(object_guid, unk1) =>
      sendResponse(ObjectDeleteMessage(object_guid, 0))
      log.info("ObjectDelete: " + msg)

    case msg @ MoveItemMessage(item_guid, source_guid, destination_guid, dest, unk1) =>
      log.info(s"MoveItem: $msg")
      (continent.GUID(source_guid), continent.GUID(destination_guid), continent.GUID(item_guid)) match {
        case (Some(source : Container), Some(destination : Container), Some(item : Equipment)) =>
          source.Find(item_guid) match {
            case Some(index) =>
              val indexSlot = source.Slot(index)
              val destSlot = destination.Slot(dest)
              val destItem = destSlot.Equipment
              if( {
                val tile = item.Definition.Tile
                destination.Collisions(dest, tile.Width, tile.Height) match {
                  case Success(Nil) =>
                    destItem.isEmpty //no item swap; abort if encountering an unexpected item
                  case Success(entry :: Nil) =>
                    destItem.contains(entry.obj) //one item to swap; abort if destination item is missing or is wrong
                  case Success(_) | scala.util.Failure(_) =>
                    false //abort when too many items at destination or other failure case
                }
              } && indexSlot.Equipment.contains(item)) {
                log.info(s"MoveItem: $item_guid moved from $source_guid @ $index to $destination_guid @ $dest")
                indexSlot.Equipment = None
                destItem match { //do we have a swap item?
                  case Some(item2) => //yes, swap
                    destSlot.Equipment = None //remove item2 to make room for item
                    destSlot.Equipment = item
                    (indexSlot.Equipment = item2) match {
                      case Some(_) => //item and item2 swapped places successfully
                        log.info(s"MoveItem: ${item2.GUID} swapped to $source_guid @ $index")
                        //cleanly shuffle items around to avoid losing icons
                        sendResponse(ObjectDetachMessage(source_guid, item_guid, Vector3(0f, 0f, 0f), 0f, 0f, 0f)) //ground; A -> C
                        sendResponse(ObjectAttachMessage(source_guid, item2.GUID, index)) //B -> A
                        source match {
                          case (obj : Vehicle) =>
                            val player_guid = player.GUID
                            vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player_guid, item_guid))
                            vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player_guid, source_guid, index, item2))
                          //TODO visible slot verification, in the case of BFR arms
                          case (_ : Player) =>
                            if(source.VisibleSlots.contains(index)) {
                              avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(source_guid, index, item2))
                            }
                          case _ => ;
                            //TODO something?
                        }

                      case None => //item2 does not fit; drop on ground
                        val pos = source.Position
                        val sourceOrientZ = source.Orientation.z
                        val orient : Vector3 = Vector3(0f, 0f, sourceOrientZ)
                        continent.Actor ! Zone.DropItemOnGround(item2, pos, orient)
                        sendResponse(ObjectDetachMessage(source_guid, item2.GUID, pos, 0f, 0f, sourceOrientZ)) //ground
                        val objDef = item2.Definition
                        avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentOnGround(player.GUID, pos, orient, objDef.ObjectId, item2.GUID, objDef.Packet.ConstructorData(item2).get))
                    }

                  case None => //just move item over
                    destSlot.Equipment = item
                    source match {
                      case (obj : Vehicle) =>
                        vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.UnstowEquipment(player.GUID, item_guid))
                        //TODO visible slot verification, in the case of BFR arms
                      case _ => ;
                      //TODO something?
                    }

                }
                sendResponse(ObjectAttachMessage(destination_guid, item_guid, dest))
                destination match {
                  case (obj : Vehicle) =>
                    vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player.GUID, destination_guid, dest, item))
                  //TODO visible slot verification, in the case of BFR arms
                  case (_ : Player) =>
                    if(destination.VisibleSlots.contains(dest)) {
                      avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(destination_guid, dest, item))
                    }
                  case _ => ;
                    //TODO something?
                }
              }
              else if(indexSlot.Equipment.nonEmpty) {
                log.error(s"MoveItem: wanted to move $item_guid, but unexpected item ${indexSlot.Equipment.get} at origin")
              }
              else {
                log.error(s"MoveItem: wanted to move $item_guid, but unexpected item(s) at destination")
              }
            case _ =>
              log.error(s"MoveItem: wanted to move $item_guid, but could not find it")
          }

        case (None, _, _) =>
          log.error(s"MoveItem: wanted to move $item_guid from $source_guid, but could not find source")
        case (_, None, _) =>
          log.error(s"MoveItem: wanted to move $item_guid from $source_guid to $destination_guid, but could not find destination")
        case (_, _, None) =>
          log.error(s"MoveItem: wanted to move $item_guid, but could not find it")
        case _ =>
          log.error(s"MoveItem: wanted to move $item_guid from $source_guid to $destination_guid, but multiple problems were encountered")
      }

    case msg @ LootItemMessage(item_guid, target_guid) =>
      log.info("LootItem: " + msg)

    case msg @ AvatarImplantMessage(_, _, _, _) => //(player_guid, unk1, unk2, implant) =>
      log.info("AvatarImplantMessage: " + msg)

    case msg @ UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType) =>
      log.info("UseItem: " + msg)
      // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
      // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
      continent.GUID(object_guid) match {
        case Some(door : Door) =>
          if(player.Faction == door.Faction || ((continent.Map.DoorToLock.get(object_guid.guid) match {
            case Some(lock_guid) => continent.GUID(lock_guid).get.asInstanceOf[IFFLock].HackedBy.isDefined
            case None => !door.isOpen
          }) || Vector3.ScalarProjection(door.Outwards, player.Position - door.Position) < 0f)) {
            door.Actor ! Door.Use(player, msg)
          }
          else if(door.isOpen) {
            //the door is open globally ... except on our screen
            sendResponse(GenericObjectStateMsg(object_guid, 16))
          }

        case Some(panel : IFFLock) =>
          if(panel.Faction != player.Faction && panel.HackedBy.isEmpty) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool : SimpleItem) =>
                if(tool.Definition == GlobalDefinitions.remote_electronics_kit) {
                  //TODO get player hack level (for now, presume 15s in intervals of 4/s)
                  progressBarValue = Some(-2.66f)
                  self ! WorldSessionActor.ItemHacking(player, panel, tool.GUID, 2.66f, FinishHackingDoor(panel, 1114636288L))
                  log.info("Hacking a door~")
                }
              case _ => ;
            }
          }

        case Some(obj : Locker) =>
          if(player.Faction == obj.Faction) {
            log.info(s"UseItem: $player accessing a locker")
            val container = player.Locker
            accessedContainer = Some(container)
            sendResponse(UseItemMessage(avatar_guid, unk1, container.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, 456))
          }
          else {
            log.info(s"UseItem: not $player's locker")
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
              if(obj.AccessingTrunk.isEmpty) {
                obj.AccessingTrunk = player.GUID
                accessedContainer = Some(obj)
                AccessContents(obj)
                sendResponse(UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
              }
              else {
                log.info(s"UseItem: $player can not cut in line while player ${obj.AccessingTrunk.get} is using $obj's trunk")
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

        case Some(obj : Terminal) =>
          if(obj.Definition.isInstanceOf[MatrixTerminalDefinition]) {
            //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
            sendResponse(BindPlayerMessage(1, "@ams", true, true, 0, 0, 0, obj.Position))
          }
          else {
            sendResponse(UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
          }

        case Some(obj : PlanetSideGameObject) =>
          if(itemType != 121) {
            sendResponse(UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
          }
          else if(itemType == 121 && !unk3) { // TODO : medkit use ?!
            sendResponse(UseItemMessage(avatar_guid, unk1, object_guid, 0, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
            sendResponse(PlanetsideAttributeMessage(avatar_guid, 0, 100)) // avatar with 100 hp
            sendResponse(ObjectDeleteMessage(PlanetSideGUID(unk1), 2))
          }

        case None => ;
      }

    case msg @ UnuseItemMessage(player_guid, object_guid) =>
      log.info("UnuseItem: " + msg)
      continent.GUID(object_guid) match {
        case Some(obj : Vehicle) =>
          if(obj.AccessingTrunk.contains(player.GUID)) {
            obj.AccessingTrunk = None
            UnAccessContents(obj)
          }

        case _ =>;
      }
      accessedContainer = None

    case msg @ DeployObjectMessage(guid, unk1, pos, roll, pitch, yaw, unk2) =>
      log.info("DeployObject: " + msg)

    case msg @ GenericObjectStateMsg(object_guid, unk1) =>
      log.info("GenericObjectState: " + msg)

    case msg @ ItemTransactionMessage(terminal_guid, _, _, _, _, _) =>
      log.info("ItemTransaction: " + msg)
      continent.GUID(terminal_guid) match {
        case Some(term : Terminal) =>
          if(player.Faction == term.Faction) {
            term.Actor ! Terminal.Request(player, msg)
          }
        case Some(obj : PlanetSideGameObject) => ;
        case None => ;
      }

    case msg @ FavoritesRequest(player_guid, unk, action, line, label) =>
      if(player.GUID == player_guid) {
        val name = label.getOrElse("missing_loadout_name")
        action match {
          case FavoritesAction.Unknown => ;
          case FavoritesAction.Save =>
            player.SaveLoadout(name, line)
            sendResponse(FavoritesMessage(0, player_guid, line, name))
          case FavoritesAction.Delete =>
            player.DeleteLoadout(line)
            sendResponse(FavoritesMessage(0, player_guid, line, ""))
        }
      }
      log.info("FavoritesRequest: " + msg)

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

    case msg @ MountVehicleMsg(player_guid, mountable_guid, unk) =>
      log.info("MountVehicleMsg: "+msg)
      continent.GUID(mountable_guid) match {
        case Some(obj : Mountable) =>
          obj.GetSeatFromMountPoint(unk) match {
            case Some(seat_num) =>
              obj.Actor ! Mountable.TryMount(player, seat_num)
            case None =>
              log.warn(s"MountVehicleMsg: attempted to board mountable $mountable_guid's seat $unk, but no seat exists there")
          }
        case None | Some(_) =>
          log.warn(s"MountVehicleMsg: not a mountable thing")
      }

    case msg @ DismountVehicleMsg(player_guid, unk1, unk2) =>
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
            continent.GUID(obj_guid) match {
              case Some(obj : Mountable) =>
                obj.PassengerInSeat(player) match {
                  case Some(seat_num : Int) =>
                    obj.Actor ! Mountable.TryDismount(player, seat_num)
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
            log.error(s"DeployRequest: can not find $vehicle_guid in scope; removing ownership to mitigate confusion")
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
                    vehicle.Definition.MountPoints.values.foreach(seat_num => {
                      val seat = vehicle.Seat(seat_num).get
                      seat.Occupant match {
                        case Some(tplayer) =>
                          if(vehicle.SeatPermissionGroup(seat_num).contains(group) && tplayer != player) {
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
            localService ! AvatarServiceMessage(continent.Id, AvatarAction.EquipmentInHand(localTarget.GUID, localIndex, localObject))
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
      }, List(GUIDTask.RegisterAvatar(tplayer)(continent.GUID))
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
        private val localAnnounce = vehicleService
        private val localSession : String = sessionId.toString
        private val localPlayer = player
        private val localVehicleService = vehicleService
        private val localZone = continent

        override def isComplete : Task.Resolution.Value = {
          if(localVehicle.Actor != ActorRef.noSender) {
            Task.Resolution.Success
          }
          else {
            Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localAnnounce ! VehicleServiceMessage.GiveActorControl(obj, localSession)
          localPad ! VehicleSpawnPad.VehicleOrder(localPlayer, localVehicle)
          localVehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(localVehicle, localZone, 60L)
          resolver ! scala.util.Success(this)
        }
      }, List(RegisterVehicle(obj)))
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
    * The process of hacking the `Door` `IFFLock` is completed.
    * Pass the message onto the lock and onto the local events system.
    * @param target the `IFFLock` belonging to the door that is being hacked
    * @param unk na;
    *            used by `HackingMessage` as `unk5`
    * @see `HackMessage`
    */
  //TODO add params here depending on which params in HackMessage are important
  //TODO sound should be centered on IFFLock, not on player
  private def FinishHackingDoor(target : IFFLock, unk : Long)() : Unit = {
    target.Actor ! CommonMessages.Hack(player)
    localService ! LocalServiceMessage(continent.Id, LocalAction.TriggerSound(player.GUID, TriggeredSound.HackDoor, player.Position, 30, 0.49803925f))
    localService ! LocalServiceMessage(continent.Id, LocalAction.HackTemporarily(player.GUID, continent, target, unk))
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
    * Gives a target player positive battle experience points only.
    * If the player has access to more implant slots as a result of changing battle experience points, unlock those slots.
    * @param tplayer the player
    * @param bep the change in experience points, positive by assertion
    * @return the player's current battle experience points
    */
  def AwardBattleExperiencePoints(tplayer : Player, bep : Long) : Long = {
    val oldBep = tplayer.BEP
    if(bep <= 0) {
      log.error(s"trying to set $bep battle experience points on $tplayer; value can not be negative")
      oldBep
    }
    else {
      val oldSlots = DetailedCharacterData.numberOfImplantSlots(oldBep)
      val newBep = oldBep + bep
      val newSlots = DetailedCharacterData.numberOfImplantSlots(newBep)
      tplayer.BEP = newBep
      if(newSlots > oldSlots) {
        (oldSlots until newSlots).foreach(slotNumber => {
          tplayer.Implants(slotNumber).Unlocked = true
          log.info(s"unlocking implant slot $slotNumber for $tplayer")
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
    vehicle.Trunk.Items.foreach({
      case ((_, entry)) =>
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
    vehicle.Trunk.Items.foreach({
      case ((_, entry)) =>
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
    * Within a specified `Container`, find the smallest number of `AmmoBox` objects of a certain type of `Ammo`
    * whose sum capacities is greater than, or equal to, a `desiredAmount`.<br>
    * <br>
    * In an occupied `List` of returned `Inventory` entries, all but the last entry is considered emptied.
    * The last entry may require having its `Capacity` be set to a non-zero number.
    * @param obj the `Container` to search
    * @param ammoType the type of `Ammo` to search for
    * @param desiredAmount how much ammunition is requested to be found
    * @return a `List` of all discovered entries totaling approximately the amount of the requested `Ammo`
    */
  def FindReloadAmmunition(obj : Container, ammoType : Ammo.Value, desiredAmount : Int) : List[InventoryItem] = {
    var currentAmount : Int = 0
    obj.Inventory.Items
      .map({ case ((_, item)) => item })
      .filter(obj => {
        obj.obj match {
          case (box : AmmoBox) =>
            box.AmmoType == ammoType
          case _ =>
            false
        }
      })
      .toList
      .sortBy(_.start)
      .takeWhile(entry => {
        val previousAmount = currentAmount
        currentAmount += entry.obj.asInstanceOf[AmmoBox].Capacity
        previousAmount < desiredAmount
      })
  }

  /**
    * Given an object that contains a box of amunition in its `Inventory` at a certain location,
    * remove it permanently.
    * @param obj the `Container`
    * @param start where the ammunition can be found
    * @param item an object to unregister (should have been the ammunition that was removed);
    *             not explicitly checked
    */
  private def DeleteAmmunition(obj : PlanetSideGameObject with Container)(start : Int, item : AmmoBox) : Unit = {
    val item_guid = item.GUID
    obj.Inventory -= start
    taskResolver ! GUIDTask.UnregisterEquipment(item)(continent.GUID)
    sendResponse(ObjectDeleteMessage(item_guid, 0))
  }

  /**
    * Given a vehicle that contains a box of amunition in its `Trunk` at a certain location,
    * remove it permanently.
    * @see `DeleteAmmunition`
    * @param obj the `Vehicle`
    * @param start where the ammunition can be found
    * @param item an object to unregister (should have been the ammunition that was removed);
    *             not explicitly checked
    */
  private def DeleteAmmunitionInVehicle(obj : Vehicle)(start : Int, item : AmmoBox) : Unit = {
    val item_guid = item.GUID
    DeleteAmmunition(obj)(start, item)
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
    * @see `StowAmmunitionInVehicles`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    */
  def StowAmmunition(obj : PlanetSideGameObject with Container)(index : Int, item : AmmoBox) : Unit = {
    obj.Inventory += index -> item
    sendResponse(ObjectAttachMessage(obj.GUID, item.GUID, index))
  }

  /**
    * Announce that an already-registered `AmmoBox` object exists in a given position in some vehicle's inventory.
    * @see `StowAmmunition`
    * @see `ChangeAmmoMessage`
    * @param obj the `Vehicle` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    */
  def StowAmmunitionInVehicles(obj : Vehicle)(index : Int, item : AmmoBox) : Unit = {
    StowAmmunition(obj)(index, item)
    vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.StowEquipment(player.GUID, obj.GUID, index, item))
  }

  /**
    * Prepare tasking that registers an `AmmoBox` object
    * and announces that it exists in a given position in some `Container` object's inventory.
    * `PutEquipmentInSlot` is the fastest way to achieve these goals.
    * @see `StowNewAmmunitionInVehicles`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    * @return a `TaskResolver.GiveTask` chain that executes the action
    */
  def StowNewAmmunition(obj : PlanetSideGameObject with Container)(index : Int, item : AmmoBox) : TaskResolver.GiveTask = {
    PutEquipmentInSlot(obj, item, index)
  }

  /**
    * Prepare tasking that registers an `AmmoBox` object
    * and announces that it exists in a given position in some vehicle's inventory.
    * `PutEquipmentInSlot` is the fastest way to achieve these goals.
    * @see `StowNewAmmunition`
    * @see `ChangeAmmoMessage`
    * @param obj the `Container` object
    * @param index an index in `obj`'s inventory
    * @param item an `AmmoBox`
    * @return a `TaskResolver.GiveTask` chain that executes the action
    */
  def StowNewAmmunitionInVehicles(obj : Vehicle)(index : Int, item : AmmoBox) : TaskResolver.GiveTask = {
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
      List(StowNewAmmunition(obj)(index, item))
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
      taskResolver ! RemoveEquipmentFromSlot(player, tool, player.Find(tool).get)
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
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 81, 1))
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
    building match {
      case _ : WarpGate =>
        initGate(continentNumber, buildingNumber, building)
      case _ : Building =>
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
    sendResponse(
      BuildingInfoUpdateMessage(
        continentNumber, //Zone
        buildingNumber, //Facility
        8, //NTU%
        false, //Hacked
        PlanetSideEmpire.NEUTRAL, //Base hacked by
        0, //Time remaining for hack (ms)
        building.Faction, //Base owned by
        0, //!! Field != 0 will cause malformed packet. See class def.
        None,
        PlanetSideGeneratorState.Normal, //Generator state
        true, //Respawn tubes operating state
        false, //Force dome state
        0, //Lattice benefits
        0, //!! Field > 0 will cause malformed packet. See class def.
        Nil,
        0,
        false,
        8, //!! Field != 8 will cause malformed packet. See class def.
        None,
        false, //Boosted spawn room pain field
        false //Boosted generator room pain field
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
    sendResponse(
      BuildingInfoUpdateMessage(
        continentNumber, buildingNumber,
        0,
        false,
        PlanetSideEmpire.NEUTRAL,
        0,
        building.Faction,
        0,
        None,
        PlanetSideGeneratorState.Normal,
        true,
        false,
        0,
        0,
        Nil,
        0,
        false,
        8,
        None,
        false,
        false
      )
    )
    sendResponse(DensityLevelUpdateMessage(continentNumber, buildingNumber, List(0,0, 0,0, 0,0, 0,0)))
    sendResponse(BroadcastWarpgateUpdateMessage(continentNumber, buildingNumber, false, false, true))
  }

  def configZone(zone : Zone) : Unit = {
    zone.Buildings.foreach({case (id, building) =>
      sendResponse(SetEmpireMessage(PlanetSideGUID(id), building.Faction))
      building.Amenities.foreach(amenity => {
        val amenityId = amenity.GUID
        sendResponse(PlanetsideAttributeMessage(amenityId, 50, 0))
        sendResponse(PlanetsideAttributeMessage(amenityId, 51, 0))
      })
      sendResponse(HackMessage(3, PlanetSideGUID(id), PlanetSideGUID(0), 0, 3212836864L, HackState.HackCleared, 8))
    })
  }

  def failWithError(error : String) = {
    log.error(error)
    sendResponse(ConnectionClose())
  }

  def sendResponse(cont : PlanetSideControlPacket) : Unit = {
    sendResponse(PacketCoding.CreateControlPacket(cont))
  }

  def sendResponse(cont : PlanetSideGamePacket) : Unit = {
    sendResponse(PacketCoding.CreateGamePacket(0, cont))
  }
  
  def sendResponse(cont : PlanetSidePacketContainer) : Unit = {
    log.trace("WORLD SEND: " + cont)
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
  private final case class PlayerLoaded(tplayer : Player)
  private final case class PlayerFailedToLoad(tplayer : Player)
  private final case class ListAccountCharacters()
  private final case class SetCurrentAvatar(tplayer : Player)
  private final case class VehicleLoaded(vehicle : Vehicle)

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
}
