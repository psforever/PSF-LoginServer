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
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.mount.Mountable
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles.{AccessPermissionGroup, VehicleLockState}
import net.psforever.objects.zones.{InterstellarCluster, Zone}
import net.psforever.packet.game.objectcreate.{DetailedCharacterData, _}
import net.psforever.types._
import services._
import services.avatar._
import services.local._
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
              vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(tplayer.GUID, 0, true))
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
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage()))

    case AvatarServiceResponse(_, guid, reply) =>
      reply match {
        case AvatarResponse.ArmorChanged(suit, subtype) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(guid, suit, subtype)))
          }

        case AvatarResponse.ConcealPlayer() =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectActionMessage(guid, 36)))
          }

        case AvatarResponse.EquipmentInHand(slot, item) =>
          if(player.GUID != guid) {
            val definition = item.Definition
            sendResponse(
              PacketCoding.CreateGamePacket(0,
                ObjectCreateMessage(
                  definition.ObjectId,
                  item.GUID,
                  ObjectCreateMessageParent(guid, slot),
                  definition.Packet.ConstructorData(item).get
                )
              )
            )
          }

        case AvatarResponse.EquipmentOnGround(pos, orient, item) =>
          if(player.GUID != guid) {
            val definition = item.Definition
            sendResponse(
              PacketCoding.CreateGamePacket(0,
                ObjectCreateMessage(
                  definition.ObjectId,
                  item.GUID,
                  DroppedItemData(PlacementData(pos, Vector3(0f, 0f, orient.z)), definition.Packet.ConstructorData(item).get)
                )
              )
            )
          }

        case AvatarResponse.LoadPlayer(pdata) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectCreateMessage(ObjectClass.avatar, guid, pdata)))
          }

        case AvatarResponse.ObjectDelete(item_guid, unk) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(item_guid, unk)))
          }

        case AvatarResponse.ObjectHeld(slot) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectHeldMessage(guid, slot, true)))
          }

        case AvatarResponse.PlanetsideAttribute(attribute_type, attribute_value) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(guid, attribute_type, attribute_value)))
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
              (distanceSq < 160000 && (msg.is_jumping || time < 200)) ||
              (distanceSq < 160000 && msg.vel.isEmpty && time > 2000) ||
              (distanceSq < 160000 && time > 1000) ||
              (distanceSq > 160000 && time > 5000))
            {
              sendResponse(
                PacketCoding.CreateGamePacket(0,
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
              )
              player.lastSeenStreamMessage(guid.guid) = now
            }
          }

        case AvatarResponse.Reload(mag) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ReloadMessage(guid, mag, 0)))
          }

        case _ => ;
      }

    case LocalServiceResponse(_, guid, reply) =>
      reply match {
        case LocalResponse.DoorOpens(door_guid) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(door_guid, 16)))
          }

        case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
          sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(door_guid, 17)))

        case LocalResponse.HackClear(target_guid, unk1, unk2) =>
          sendResponse(PacketCoding.CreateGamePacket(0, HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2)))

        case LocalResponse.HackObject(target_guid, unk1, unk2) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, HackMessage(0, target_guid, guid, 100, unk1, HackState.Hacked, unk2)))
          }

        case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
          sendResponse(PacketCoding.CreateGamePacket(0, TriggerSoundMessage(sound, pos, unk, volume)))

        case _ => ;
      }

    case VehicleServiceResponse(_, guid, reply) =>
      reply match {
        case VehicleResponse.Awareness(vehicle_guid) =>
          //resets exclamation point fte marker (once)
          sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(guid, 21, vehicle_guid.guid.toLong)))

        case VehicleResponse.ChildObjectState(object_guid, pitch, yaw) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ChildObjectStateMessage(object_guid, pitch, yaw)))
          }

        case VehicleResponse.DismountVehicle(unk1, unk2) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, DismountVehicleMsg(guid, unk1, unk2)))
          }

        case VehicleResponse.KickPassenger(unk1, unk2) =>
          sendResponse(PacketCoding.CreateGamePacket(0, DismountVehicleMsg(guid, unk1, unk2)))

        case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) =>
          //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectCreateMessage(vtype, vguid, vdata)))
            ReloadVehicleAccessPermissions(vehicle)
          }

        case VehicleResponse.MountVehicle(vehicle_guid, seat) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(vehicle_guid, guid, seat)))
          }

        case VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, seat_group, permission)))
          }

        case VehicleResponse.UnloadVehicle(vehicle_guid) =>
          sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(vehicle_guid, 0)))

        case VehicleResponse.VehicleState(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6)))
          }

        case _ => ;
      }

    case Door.DoorMessage(tplayer, msg, order) =>
      val door_guid = msg.object_guid
      order match {
        case Door.OpenEvent() =>
          continent.GUID(door_guid) match {
            case Some(door : Door) =>
              sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(door_guid, 16)))
              localService ! LocalServiceMessage(continent.Id, LocalAction.DoorOpens (tplayer.GUID, continent, door) )

            case _ =>
              log.warn(s"door $door_guid wanted to be opened but could not be found")
          }

        case Door.CloseEvent() =>
          sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(door_guid, 17)))
          localService ! LocalServiceMessage(continent.Id, LocalAction.DoorCloses(tplayer.GUID, door_guid))

        case Door.NoEvent() => ;
      }

    case Mountable.MountMessages(tplayer, reply) =>
      reply match {
        case Mountable.CanMount(obj : ImplantTerminalMech, seat_num) =>
          val player_guid : PlanetSideGUID = tplayer.GUID
          val obj_guid : PlanetSideGUID = obj.GUID
          log.info(s"MountVehicleMsg: $player_guid mounts $obj @ $seat_num")
          tplayer.VehicleSeated = Some(obj_guid)
          sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(obj_guid, 0, 1000L))) //health of mech
          sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(obj_guid, player_guid, seat_num)))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seat_num))

        case Mountable.CanMount(obj : Vehicle, seat_num) =>
          val obj_guid : PlanetSideGUID = obj.GUID
          val player_guid : PlanetSideGUID = tplayer.GUID
          log.info(s"MountVehicleMsg: $player_guid mounts $obj_guid @ $seat_num")
          vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(obj_guid) //clear all deconstruction timers
          tplayer.VehicleSeated = Some(obj_guid)
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
              val magazine = weapon.AmmoSlots(weapon.FireModeIndex).Box //update the magazine in the weapon, specifically
              sendResponse(PacketCoding.CreateGamePacket(0, InventoryStateMessage(magazine.GUID, 0, weapon.GUID, weapon.Magazine.toLong)))
              //update all related ammunition objects in trunk
              obj.Trunk.Items
                .filter({ case ((_, item)) => item.obj.isInstanceOf[AmmoBox] && item.obj.asInstanceOf[AmmoBox].AmmoType == weapon.AmmoType })
                .foreach({ case ((_, item)) =>
                  val box = item.obj.asInstanceOf[AmmoBox]
                  sendResponse(PacketCoding.CreateGamePacket(0, InventoryStateMessage(box.GUID, 0, obj_guid, box.Capacity.toLong)))
                })
            case _ => ; //no weapons to update
          }
          sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(obj_guid, player_guid, seat_num)))
          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.MountVehicle(player_guid, obj_guid, seat_num))

        case Mountable.CanMount(obj : Mountable, seat_num) =>
          log.warn(s"MountVehicleMsg: $obj is some generic mountable object and nothing will happen")

        case Mountable.CanNotMount(obj, seat_num) =>
          log.warn(s"MountVehicleMsg: $tplayer attempted to mount $obj's seat $seat_num, but was not allowed")
      }

    case Terminal.TerminalMessage(tplayer, msg, order) =>
      order match {
        case Terminal.BuyExosuit(exosuit, subtype) =>
          if(tplayer.ExoSuit == exosuit) { //just refresh armor points
            //we should never actually reach this point through conventional in-game methods
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true)))
            tplayer.Armor = tplayer.MaxArmor
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor)))
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
          }
          else { //load a complete new exo-suit and shuffle the inventory around
            //TODO if we're transitioning into a MAX suit, the subtype dictates the type of arm(s) if the holster list is empty
            //save inventory before it gets cleared (empty holsters)
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true)))
            val beforeHolsters = clearHolsters(tplayer.Holsters().iterator)
            val beforeInventory = tplayer.Inventory.Clear()
            //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
            Player.SuitSetup(tplayer, exosuit)
            tplayer.Armor = tplayer.MaxArmor
            //delete everything
            (beforeHolsters ++ beforeInventory).foreach({ elem =>
              sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(elem.obj.GUID, 0)))
            })
            beforeHolsters.foreach({ elem =>
              avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(tplayer.GUID, elem.obj.GUID))
            })
            //report change
            sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(tplayer.GUID, exosuit, subtype)))
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor)))
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
            //fill holsters
            val (afterHolsters, toInventory) = beforeHolsters.partition(elem => elem.obj.Size == tplayer.Slot(elem.start).Size)
            afterHolsters.foreach({elem => tplayer.Slot(elem.start).Equipment = elem.obj })
            val finalInventory = fillEmptyHolsters(tplayer.Holsters().iterator, toInventory ++ beforeInventory)
            //draw holsters
            (0 until 5).foreach({index =>
              tplayer.Slot(index).Equipment match {
                case Some(obj) =>
                  val definition = obj.Definition
                  sendResponse(
                    PacketCoding.CreateGamePacket(0,
                      ObjectCreateDetailedMessage(
                        definition.ObjectId,
                        obj.GUID,
                        ObjectCreateMessageParent(tplayer.GUID, index),
                        definition.Packet.DetailedConstructorData(obj).get
                      )
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
                  PacketCoding.CreateGamePacket(0,
                    ObjectCreateDetailedMessage(
                      definition.ObjectId,
                      item.GUID,
                      ObjectCreateMessageParent(tplayer.GUID, Player.FreeHandSlot),
                      definition.Packet.DetailedConstructorData(item).get
                    )
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
                PacketCoding.CreateGamePacket(0,
                  ObjectCreateDetailedMessage(
                    definition.ObjectId,
                    obj.GUID,
                    ObjectCreateMessageParent(tplayer.GUID, elem.start),
                    definition.Packet.DetailedConstructorData(obj).get
                  )
                )
              )
            })
            //drop items on ground
            val pos = tplayer.Position
            val orient = tplayer.Orientation
            drop.foreach(obj => {
              obj.Position = pos
              obj.Orientation = orient
              val definition = obj.Definition
              sendResponse(
                PacketCoding.CreateGamePacket(0,
                  ObjectCreateMessage(
                    definition.ObjectId,
                    obj.GUID,
                    DroppedItemData(PlacementData(pos, Vector3(0f, 0f, orient.z)), definition.Packet.ConstructorData(obj).get)
                  )
                )
              )
              avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.EquipmentOnGround(tplayer.GUID, pos, orient, obj))
            })
          }
          sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true)))

        case Terminal.BuyEquipment(item) => ;
          tplayer.Fit(item) match {
            case Some(index) =>
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true)))
              taskResolver ! PutEquipmentInSlot(tplayer, item, index)
            case None =>
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, false)))
          }

        case Terminal.SellEquipment() =>
          tplayer.FreeHand.Equipment match {
            case Some(item) =>
              if(item.GUID == msg.item_guid) {
                sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Sell, true)))
                taskResolver ! RemoveEquipmentFromSlot(tplayer, item, Player.FreeHandSlot)
              }
            case None =>
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Sell, false)))
          }

        case Terminal.InfantryLoadout(exosuit, subtype, holsters, inventory) =>
          //TODO optimizations against replacing Equipment with the exact same Equipment and potentially for recycling existing Equipment
          log.info(s"$tplayer wants to change equipment loadout to their option #${msg.unk1 + 1}")
          sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.InfantryLoadout, true)))
          val beforeHolsters = clearHolsters(tplayer.Holsters().iterator)
          val beforeInventory = tplayer.Inventory.Clear()
          val beforeFreeHand = tplayer.FreeHand.Equipment
          //change suit (clear inventory and change holster sizes; note: holsters must be empty before this point)
          Player.SuitSetup(tplayer, exosuit)
          tplayer.Armor = tplayer.MaxArmor
          //delete everything
          beforeHolsters.foreach({ elem =>
            avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(tplayer.GUID, elem.obj.GUID))
          })
          (beforeHolsters ++ beforeInventory).foreach({ elem =>
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(elem.obj.GUID, 0)))
            taskResolver ! GUIDTask.UnregisterEquipment(elem.obj)(continent.GUID)
          })
          //report change
          sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(tplayer.GUID, exosuit, 0)))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
          sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor)))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
          //re-draw equipment held in free hand
          beforeFreeHand match {
            case Some(item) =>
              tplayer.FreeHand.Equipment = beforeFreeHand
              val definition = item.Definition
              sendResponse(
                PacketCoding.CreateGamePacket(0,
                  ObjectCreateDetailedMessage(
                    definition.ObjectId,
                    item.GUID,
                    ObjectCreateMessageParent(tplayer.GUID, Player.FreeHandSlot),
                    definition.Packet.DetailedConstructorData(item).get
                  )
                )
              )
            case None => ;
          }
          //draw holsters
          holsters.foreach(entry => {
            taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //put items into inventory
          inventory.foreach(entry => {
            taskResolver ! PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //TODO drop items on ground
          sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.InfantryLoadout, true)))

        case Terminal.LearnCertification(cert, cost) =>
          if(!player.Certifications.contains(cert)) {
            log.info(s"$tplayer is learning the $cert certification for $cost points")
            tplayer.Certifications += cert
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 24, cert.id.toLong)))
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, true)))
          }
          else {
            log.warn(s"$tplayer already knows the $cert certification, so he can't learn it")
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false)))
          }

        case Terminal.SellCertification(cert, cost) =>
          if(player.Certifications.contains(cert)) {
            log.info(s"$tplayer is forgetting the $cert certification for $cost points")
            tplayer.Certifications -= cert
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 25, cert.id.toLong)))
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Sell, true)))
          }
          else {
            log.warn(s"$tplayer doesn't know what a $cert certification is, so he can't forget it")
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, false)))
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
            sendResponse(PacketCoding.CreateGamePacket(0, AvatarImplantMessage(tplayer.GUID, 0, slot, implant_type.id)))
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(terminal_guid, TransactionType.Learn, true)))
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
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(terminal_guid, TransactionType.Learn, false)))
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
            sendResponse(PacketCoding.CreateGamePacket(0, AvatarImplantMessage(tplayer.GUID, 1, slot, 0)))
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(terminal_guid, TransactionType.Sell, true)))
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
            sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(terminal_guid, TransactionType.Sell, false)))
          }


        case Terminal.BuyVehicle(vehicle, loadout) =>
          continent.Map.TerminalToSpawnPad.get(msg.terminal_guid.guid) match {
            case Some(pad_guid) =>
              val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad]
              vehicle.Faction = tplayer.Faction
              vehicle.Position = pad.Position
              vehicle.Orientation = pad.Orientation
              taskResolver ! RegisterNewVehicle(vehicle, pad)
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Learn, true)))
            case None =>
              log.error(s"$tplayer wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it")
          }

        case Terminal.NoDeal() =>
          log.warn(s"$tplayer made a request but the terminal rejected the order $msg")
          sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false)))
      }

    case VehicleSpawnPad.ConcealPlayer =>
      sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectActionMessage(player.GUID, 36)))
      avatarService ! AvatarServiceMessage(continent.Id, AvatarAction.ConcealPlayer(player.GUID))

    case VehicleSpawnPad.LoadVehicle(vehicle, _/*pad*/) =>
      val player_guid = player.GUID
      val definition = vehicle.Definition
      val objedtId = definition.ObjectId
      val vehicle_guid = vehicle.GUID
      val vdata = definition.Packet.ConstructorData(vehicle).get
      sendResponse(PacketCoding.CreateGamePacket(0, ObjectCreateMessage(objedtId, vehicle_guid, vdata)))
      continent.Transport ! Zone.SpawnVehicle(vehicle)
      vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.LoadVehicle(player_guid, vehicle, objedtId, vehicle_guid, vdata))
      sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 22, 1L))) //mount points off?
      //sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 21, player_guid.guid))) //fte and ownership?
      //sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(vehicle_guid, player_guid, 0)))
      vehicleService ! VehicleServiceMessage.UnscheduleDeconstruction(vehicle_guid) //cancel queue timeout delay
      vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(vehicle, continent, 21L) //temporary drive away from pad delay
      vehicle.Actor ! Mountable.TryMount(player, 0)

    case VehicleSpawnPad.PlayerSeatedInVehicle(vehicle) =>
      vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(vehicle, continent, 21L) //sitting in the vehicle clears the drive away delay
      val vehicle_guid = vehicle.GUID
      sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 22, 0L))) //mount points on?
      //sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 0, vehicle.Definition.MaxHealth)))
      sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 68, 0L))) //???
      sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(vehicle_guid, 113, 0L))) //???
      ReloadVehicleAccessPermissions(vehicle)

    case VehicleSpawnPad.SpawnPadBlockedWarning(vehicle, warning_count) =>
      if(warning_count > 2) {
        sendResponse(PacketCoding.CreateGamePacket(0, TriggerSoundMessage(TriggeredSound.Unknown14, vehicle.Position, 20, 1f)))
        sendResponse(PacketCoding.CreateGamePacket(0,
          ChatMsg(ChatMessageType.CMT_TELL, true, "", "\\#FYour vehicle is blocking the spawn pad, and will be deconstructed if not moved.", None))
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
      sendResponse(PacketCoding.CreateGamePacket(0,
        ObjectCreateDetailedMessage(ObjectClass.avatar, player.GUID, converter.DetailedConstructorData(player).get)
      ))
      if(health > 0) { //player can not be dead; stay spawned as alive
        player.Health = health
        player.Stamina = stamina
        player.Armor = armor
      }
      sendResponse(PacketCoding.CreateGamePacket(0, CharacterInfoMessage(15,PlanetSideZoneID(10000), 41605313, player.GUID, false, 6404428)))
      RemoveCharacterSelectScreenGUID(player)

      sendResponse(PacketCoding.CreateGamePacket(0, CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0)))

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

    case Zone.ClientInitialization(/*initList*/_) =>
      //TODO iterate over initList; for now, just do this
      sendResponse(
        PacketCoding.CreateGamePacket(0,
          BuildingInfoUpdateMessage(
            PlanetSideGUID(6),    //Ceryshen
            PlanetSideGUID(2),    //Anguta
            8,                    //80% NTU
            true,                 //Base hacked
            PlanetSideEmpire.NC,  //Base hacked by NC
            600000,               //10 minutes remaining for hack
            PlanetSideEmpire.VS,  //Base owned by VS
            0,                    //!! Field != 0 will cause malformed packet. See class def.
            None,
            PlanetSideGeneratorState.Critical, //Generator critical
            true,                 //Respawn tubes destroyed
            true,                 //Force dome active
            16,                   //Tech plant lattice benefit
            0,
            Nil,                  //!! Field > 0 will cause malformed packet. See class def.
            0,
            false,
            8,                    //!! Field != 8 will cause malformed packet. See class def.
            None,
            true,                 //Boosted spawn room pain field
            true                  //Boosted generator room pain field
          )
        )
      )
      sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(13), PlanetSideEmpire.VS))) // "The VS have captured the VS Sanctuary."
      sendResponse(PacketCoding.CreateGamePacket(0, BroadcastWarpgateUpdateMessage(PlanetSideGUID(13), PlanetSideGUID(1), false, false, true))) // VS Sanctuary: Inactive Warpgate -> Broadcast Warpgate
      sendResponse(PacketCoding.CreateGamePacket(0, ZonePopulationUpdateMessage(PlanetSideGUID(13), 414, 138, 0, 138, 0, 138, 0, 138, 0)))

    case InterstellarCluster.ClientInitializationComplete(tplayer)=>
      //this will cause the client to send back a BeginZoningMessage packet (see below)
      sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage(continent.Map.Name, continent.Id, 40100,25,true,3770441820L))) //VS Sanctuary
      log.info("Load the now-registered player")
      //load the now-registered player
      tplayer.Spawn
      sendResponse(PacketCoding.CreateGamePacket(0,
        ObjectCreateDetailedMessage(ObjectClass.avatar, tplayer.GUID, tplayer.Definition.Packet.DetailedConstructorData(tplayer).get)
      ))
      avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.LoadPlayer(tplayer.GUID, tplayer.Definition.Packet.ConstructorData(tplayer).get))
      log.debug(s"ObjectCreateDetailedMessage: ${tplayer.Definition.Packet.DetailedConstructorData(tplayer).get}")

    case SetCurrentAvatar(tplayer) =>
      val guid = tplayer.GUID
      LivePlayerList.Assign(continent.Number, sessionId, guid)
      sendResponse(PacketCoding.CreateGamePacket(0, SetCurrentAvatarMessage(guid,0,0)))
      sendResponse(PacketCoding.CreateGamePacket(0, CreateShortcutMessage(guid, 1, 0, true, Shortcut.MEDKIT)))
      sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_EXPANSIONS, true, "", "1 on", None))) //CC on

    case Zone.ItemFromGround(tplayer, item) =>
      val obj_guid = item.GUID
      val player_guid = tplayer.GUID
      tplayer.Fit(item) match {
        case Some(slot) =>
          tplayer.Slot(slot).Equipment = item
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(player_guid, obj_guid))
          val definition = item.Definition
          sendResponse(
            PacketCoding.CreateGamePacket(0,
              ObjectCreateDetailedMessage(
                definition.ObjectId,
                obj_guid,
                ObjectCreateMessageParent(player_guid, slot),
                definition.Packet.DetailedConstructorData(item).get
              )
            )
          )
          if(-1 < slot && slot < 5) {
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
        sendResponse(PacketCoding.CreateGamePacket(0, HackMessage(1, target.GUID, player.GUID, progressBarVal.toInt, 0L, vis, 8L)))
        if(progressBarVal > 100) { //done
          progressBarValue = None
          log.info(s"Hacked a $target")
          sendResponse(PacketCoding.CreateGamePacket(0, HackMessage(0, target.GUID, player.GUID, 100, 1114636288L, HackState.Hacked, 8L)))
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
        sendResponse(PacketCoding.CreateControlPacket(SlottedMetaAck(slot, subslot)))

        PacketCoding.DecodePacket(innerPacket) match {
          case Failure(e) =>
            log.error(s"Failed to decode inner packet of SlottedMetaPacket: $e")
          case Successful(v) =>
            handlePkt(v)
        }
      case sync @ ControlSync(diff, unk, f1, f2, f3, f4, fa, fb) =>
        log.debug(s"SYNC: $sync")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(PacketCoding.CreateControlPacket(ControlSyncResp(diff, serverTick,
          fa, fb, fb, fa)))
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
      player.Position = Vector3(3523.039f, 2855.5078f, 90.859375f)
      player.Orientation = Vector3(0f, 0f, 90f)
      player.Certifications += CertificationType.StandardAssault
      player.Certifications += CertificationType.MediumAssault
      player.Certifications += CertificationType.StandardExoSuit
      player.Certifications += CertificationType.AgileExoSuit
      player.Certifications += CertificationType.ReinforcedExoSuit
      player.Certifications += CertificationType.ATV
      player.Certifications += CertificationType.Harasser
      //
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
      AwardBattleExperiencePoints(player, 1000000L)
//      player.ExoSuit = ExoSuitType.MAX //TODO strange issue; divide number above by 10 when uncommenting
      player.Slot(0).Equipment = Tool(beamer)
      player.Slot(2).Equipment = Tool(suppressor)
      player.Slot(4).Equipment = Tool(forceblade)
      player.Slot(6).Equipment = AmmoBox(bullet_9mm)
      player.Slot(9).Equipment = AmmoBox(bullet_9mm)
      player.Slot(12).Equipment = AmmoBox(bullet_9mm)
      player.Slot(33).Equipment = AmmoBox(bullet_9mm_AP)
      player.Slot(36).Equipment = AmmoBox(energy_cell)
      player.Slot(39).Equipment = SimpleItem(remote_electronics_kit)
      player.Slot(5).Equipment.get.asInstanceOf[LockerContainer].Inventory += 0 -> SimpleItem(remote_electronics_kit)
      //TODO end temp player character auto-loading
      self ! ListAccountCharacters
      import scala.concurrent.duration._
      import scala.concurrent.ExecutionContext.Implicits.global
      clientKeepAlive.cancel
      clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())

    case msg @ CharacterCreateRequestMessage(name, head, voice, gender, empire) =>
      log.info("Handling " + msg)
      sendResponse(PacketCoding.CreateGamePacket(0, ActionResultMessage(true, None)))
      self ! ListAccountCharacters

    case msg @ CharacterRequestMessage(charId, action) =>
      log.info("Handling " + msg)
      action match {
        case CharacterRequestAction.Delete =>
          sendResponse(PacketCoding.CreateGamePacket(0, ActionResultMessage(false, Some(1))))
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
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage()))

    case msg @ BeginZoningMessage() =>
      log.info("Reticulating splines ...")
      //map-specific initializations
      //TODO continent.ClientConfiguration()
      sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(2), PlanetSideEmpire.VS))) //HART building C
      sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(29), PlanetSideEmpire.NC))) //South Villa Gun Tower

      sendResponse(PacketCoding.CreateGamePacket(0, TimeOfDayMessage(1191182336)))
      sendResponse(PacketCoding.CreateGamePacket(0, ReplicationStreamMessage(5, Some(6), Vector(SquadListing())))) //clear squad list

      //render Equipment that was dropped into zone before the player arrived
      continent.EquipmentOnGround.foreach(item => {
        val definition = item.Definition
        sendResponse(
          PacketCoding.CreateGamePacket(0,
            ObjectCreateMessage(
              definition.ObjectId,
              item.GUID,
              DroppedItemData(PlacementData(item.Position, item.Orientation), definition.Packet.ConstructorData(item).get)
            )
          )
        )
      })
      //load active players in zone
      LivePlayerList.ZonePopulation(continent.Number, _ => true).foreach(char => {
        sendResponse(
          PacketCoding.CreateGamePacket(0,
            ObjectCreateMessage(ObjectClass.avatar, char.GUID, char.Definition.Packet.ConstructorData(char).get)
          )
        )
      })
      //load active vehicles in zone
      continent.Vehicles.foreach(vehicle => {
        val definition = vehicle.Definition
        sendResponse(PacketCoding.CreateGamePacket(0,
          ObjectCreateMessage(
            definition.ObjectId,
            vehicle.GUID,
            definition.Packet.ConstructorData(vehicle).get
          )
        ))
        //seat vehicle occupants
        vehicle.Definition.MountPoints.values.foreach(seat_num => {
          vehicle.Seat(seat_num).get.Occupant match {
            case Some(tplayer) =>
              if(tplayer.HasGUID) {
                sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(vehicle.GUID, tplayer.GUID, seat_num)))
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
            val obj_def = obj.Definition
            val obj_uid = obj_def.ObjectId
            val obj_data = obj_def.Packet.ConstructorData(obj).get
            sendResponse(PacketCoding.CreateGamePacket(0,
              ObjectCreateMessage(
                obj_uid,
                PlanetSideGUID(interface_guid),
                ObjectCreateMessageParent(parent_guid, 1),
                obj_data
              )
            ))
          case _ => ;
        }
        //seat terminal occupants
        continent.GUID(terminal_guid) match {
          case Some(obj : Mountable) =>
            obj.MountPoints.foreach({ case((_, seat_num)) =>
              obj.Seat(seat_num).get.Occupant match {
                case Some(tplayer) =>
                  if(tplayer.HasGUID) {
                    sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(parent_guid, tplayer.GUID, seat_num)))
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
          if(obj.Seat(0).get.Occupant.contains(player)) { //we're driving the vehicle
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

    case msg @ ProjectileStateMessage(projectile_guid, shot_pos, shot_vector, unk1, unk2, unk3, unk4, time_alive) =>
      //log.info("ProjectileState: " + msg)

    case msg @ ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
      // TODO: Prevents log spam, but should be handled correctly
      if (messagetype != ChatMessageType.CMT_TOGGLE_GM) {
        log.info("Chat: " + msg)
      }

      if (messagetype == ChatMessageType.CMT_VOICE) {
        sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_VOICE, false, "IlllIIIlllIlIllIlllIllI", contents, None)))
      }

      // TODO: handle this appropriately
      if(messagetype == ChatMessageType.CMT_QUIT) {
        sendResponse(DropCryptoSession())
        sendResponse(DropSession(sessionId, "user quit"))
      }

      // TODO: Depending on messagetype, may need to prepend sender's name to contents with proper spacing
      // TODO: Just replays the packet straight back to sender; actually needs to be routed to recipients!
      sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents)))

    case msg @ VoiceHostRequest(unk, PlanetSideGUID(player_guid), data) =>
      log.info("Player "+player_guid+" requested in-game voice chat.")
      sendResponse(PacketCoding.CreateGamePacket(0, VoiceHostKill()))

    case msg @ VoiceHostInfo(player_guid, data) =>
      sendResponse(PacketCoding.CreateGamePacket(0, VoiceHostKill()))

    case msg @ ChangeFireModeMessage(item_guid, fire_mode) =>
      log.info("ChangeFireMode: " + msg)

    case msg @ ChangeFireStateMessage_Start(item_guid) =>
      log.info("ChangeFireState_Start: " + msg)

    case msg @ ChangeFireStateMessage_Stop(item_guid) =>
      log.info("ChangeFireState_Stop: " + msg)
      progressBarUpdate.cancel

    case msg @ EmoteMsg(avatar_guid, emote) =>
      log.info("Emote: " + msg)
      sendResponse(PacketCoding.CreateGamePacket(0, EmoteMsg(avatar_guid, emote)))

    case msg @ DropItemMessage(item_guid) =>
      log.info("DropItem: " + msg)
      player.FreeHand.Equipment match {
        case Some(item) =>
          if(item.GUID == item_guid) {
            val orient : Vector3 = Vector3(0f, 0f, player.Orientation.z)
            player.FreeHand.Equipment = None
            continent.Ground ! Zone.DropItemOnGround(item, player.Position, orient)
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectDetachMessage(player.GUID, item.GUID, player.Position, 0f, 0f, player.Orientation.z)))
            avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentOnGround(player.GUID, player.Position, orient, item))
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
      val reloadValue : Int = player.VehicleSeated match {
        case Some(vehicle_guid) => //weapon is vehicle turret?
          continent.GUID(vehicle_guid) match {
            case Some(vehicle : Vehicle) =>
              vehicle.PassengerInSeat(player) match {
                case Some(seat_num) =>
                  vehicle.WeaponControlledFromSeat(seat_num) match {
                    case Some(item : Tool) =>
                      item.FireMode.Magazine
                    case _ => ;
                      0
                  }
                case None => ;
                  0
              }
            case _ => ;
              0
          }
        case None => //not in vehicle; weapon in hand?
          player.Slot(player.DrawnSlot).Equipment match { //TODO check that item in hand is item_guid?
            case Some(item : Tool) =>
              item.FireMode.Magazine
            case Some(_) | None => ;
              0
          }
      }
      if(reloadValue > 0) {
        //TODO hunt for ammunition in backpack/trunk
        sendResponse(PacketCoding.CreateGamePacket(0, ReloadMessage(item_guid, reloadValue, unk1)))
      }

    case msg @ ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
      val before = player.DrawnSlot
      val after = player.DrawnSlot = held_holsters
      if(before != after) {
        val slot = if(after == Player.HandsDownSlot) { before } else { after }
        avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.ObjectHeld(player.GUID, slot))
      }
      log.info("ObjectHeld: " + msg)

    case msg @ AvatarJumpMessage(state) =>
      //log.info("AvatarJump: " + msg)

    case msg @ ZipLineMessage(player_guid,origin_side,action,id,pos) =>
      log.info("ZipLineMessage: " + msg)
      if (!origin_side && action == 0) {
        //doing this lets you use the zip line in one direction, cant come back
        sendResponse(PacketCoding.CreateGamePacket(0, ZipLineMessage(player_guid, origin_side, action, id, pos)))
      }
      else if (!origin_side && action == 1) {
        //disembark from zipline at destination !
        sendResponse(PacketCoding.CreateGamePacket(0, ZipLineMessage(player_guid, origin_side, action, 0, pos)))
      }
      else if (!origin_side && action == 2) {
        //get off by force
        sendResponse(PacketCoding.CreateGamePacket(0, ZipLineMessage(player_guid, origin_side, action, 0, pos)))
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
          player.Find(object_guid) match { //player should be holding it
            case Some(slot) =>
              taskResolver ! RemoveEquipmentFromSlot(player, player.Slot(slot).Equipment.get, slot)
              log.info(s"RequestDestroy: equipment $object_guid")
            case None =>
              sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(object_guid, 0)))
              log.warn(s"RequestDestroy: object $object_guid not found in player hands")
          }

        case None =>
          log.warn(s"RequestDestroy: object $object_guid not found")

        case _ =>
          log.warn(s"RequestDestroy: not allowed to delete object $object_guid")
      }

    case msg @ ObjectDeleteMessage(object_guid, unk1) =>
      sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(object_guid, 0)))
      log.info("ObjectDelete: " + msg)

    case msg @ MoveItemMessage(item_guid, source_guid, destination_guid, dest, unk1) =>
      player.Find(item_guid) match {
        case Some(index) =>
          val indexSlot = player.Slot(index)
          var itemOpt : Option[Equipment] = indexSlot.Equipment
          //use this to short circuit
          val item = itemOpt.get
          val destSlot = player.Slot(dest)
          val destItem = if((-1 < dest && dest < 5) || dest == Player.FreeHandSlot) {
            destSlot.Equipment match {
              case Some(found) =>
                Some(InventoryItem(found, dest))
              case None =>
                None
            }
          }
          else {
            val tile = item.Definition.Tile
            player.Inventory.CheckCollisionsVar(dest, tile.Width, tile.Height) match {
              case Success(Nil) => None //no item swap
              case Success(entry :: Nil) => Some(entry) //one item to swap
              case Success(_) | scala.util.Failure(_) => itemOpt = None; None //abort item move altogether
            }
          }
          if(itemOpt.isDefined) {
            log.info(s"MoveItem: $item_guid moved from $source_guid @ $index to $source_guid @ $dest")
            indexSlot.Equipment = None
            destItem match {
              //do we have a swap item?
              case Some(entry) => //yes, swap
                val item2 = entry.obj
                player.Slot(entry.start).Equipment = None //remove item2 to make room for item
                destSlot.Equipment = item //in case dest and index could block each other
                (indexSlot.Equipment = entry.obj) match {
                  case Some(_) => //item and item2 swapped places successfully
                    log.info(s"MoveItem: ${item2.GUID} swapped to $source_guid @ $index")
                    //we must shuffle items around cleanly to avoid causing icons to "disappear"
                    if(index == Player.FreeHandSlot) {
                      //temporarily put in safe location, A -> C
                      sendResponse(PacketCoding.CreateGamePacket(0, ObjectDetachMessage(player.GUID, item.GUID, Vector3(0f, 0f, 0f), 0f, 0f, 0f))) //ground
                    }
                    else {
                      sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(player.GUID, item.GUID, Player.FreeHandSlot))) //free hand
                    }
                    sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(player.GUID, item2.GUID, index))) //B -> A
                    if(0 <= index && index < 5) {
                      avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(player.GUID, index, item2))
                    }

                  case None => //item2 does not fit; drop on ground
                    val pos = player.Position
                    val playerOrient = player.Orientation
                    val orient : Vector3 = Vector3(0f, 0f, playerOrient.z)
                    continent.Actor ! Zone.DropItemOnGround(item2, pos, orient)
                    sendResponse(PacketCoding.CreateGamePacket(0, ObjectDetachMessage(player.GUID, item2.GUID, pos, 0f, 0f, playerOrient.z))) //ground
                    avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentOnGround(player.GUID, pos, orient, item2))
                }

              case None => //just move item over
                destSlot.Equipment = item
            }
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(source_guid, item_guid, dest)))
            if(0 <= dest && dest < 5) {
              avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(player.GUID, dest, item))
            }
          }
        case None =>
          log.info(s"MoveItem: $source_guid wanted to move the item $item_guid but could not find it")
      }

    case msg @ ChangeAmmoMessage(item_guid, unk1) =>
      log.info("ChangeAmmo: " + msg)

    case msg @ AvatarImplantMessage(_, _, _, _) => //(player_guid, unk1, unk2, implant) =>
      log.info("AvatarImplantMessage: " + msg)

    case msg @ UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType) =>
      log.info("UseItem: " + msg)
      // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
      // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
      continent.GUID(object_guid) match {
        case Some(door : Door) =>
          continent.Map.DoorToLock.get(object_guid.guid) match { //check for IFF Lock
            case Some(lock_guid) =>
              val lock_hacked = continent.GUID(lock_guid).get.asInstanceOf[IFFLock].HackedBy match {
                case Some((tplayer, _, _)) =>
                  tplayer.Faction == player.Faction
                case None =>
                  false
              }
              continent.Map.ObjectToBase.get(lock_guid) match { //check for associated base
                case Some(base_id) =>
                  if(continent.Base(base_id).get.Faction == player.Faction || lock_hacked) { //either base allegiance aligns or locks is hacked
                    door.Actor ! Door.Use(player, msg)
                  }
                case None =>
                  if(lock_hacked) { //is lock hacked? this may be a weird case
                    door.Actor ! Door.Use(player, msg)
                  }
              }
            case None =>
              door.Actor ! Door.Use(player, msg) //let door open freely
          }

        case Some(panel : IFFLock) =>
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

        case Some(obj : Vehicle) =>
          if(obj.Faction == player.Faction) {
            player.Slot(player.DrawnSlot).Equipment match {
              case Some(tool : Tool) =>
                if(tool.Definition == GlobalDefinitions.nano_dispenser) {
                  //TODO repairing behavior
                }
              case Some(_) | None =>
                //TODO trunk access
                sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType)))
            }
          }

        case Some(obj : PlanetSideGameObject) =>
          if(itemType != 121) {
            sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType)))
          }
          else if(itemType == 121 && !unk3) { // TODO : medkit use ?!
            sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, 0, unk3, unk4, unk5, unk6, unk7, unk8, itemType)))
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(avatar_guid, 0, 100))) // avatar with 100 hp
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(PlanetSideGUID(unk1), 2)))
          }

        case None => ;
      }

    case msg @ UnuseItemMessage(player_guid, item) =>
      log.info("UnuseItem: " + msg)

    case msg @ DeployObjectMessage(guid, unk1, pos, roll, pitch, yaw, unk2) =>
      log.info("DeployObject: " + msg)

    case msg @ GenericObjectStateMsg(object_guid, unk1) =>
      log.info("GenericObjectState: " + msg)

    case msg @ ItemTransactionMessage(terminal_guid, _, _, _, _, _) =>
      log.info("ItemTransaction: " + msg)
      continent.GUID(terminal_guid) match {
        case Some(term : Terminal) =>
          term.Actor ! Terminal.Request(player, msg)
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
            sendResponse(PacketCoding.CreateGamePacket(0, FavoritesMessage(0, player_guid, line, name)))
          case FavoritesAction.Delete =>
            player.DeleteLoadout(line)
            sendResponse(PacketCoding.CreateGamePacket(0, FavoritesMessage(0, player_guid, line, "")))
        }
      }
      log.info("FavoritesRequest: " + msg)

    case msg @ WeaponDelayFireMessage(seq_time, weapon_guid) =>
      log.info("WeaponDelayFire: " + msg)

    case msg @ WeaponFireMessage(seq_time, weapon_guid, projectile_guid, shot_origin, unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
      log.info("WeaponFire: " + msg)

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
      if(player.GUID == player_guid) {
        //common warning for this section
        def dismountWarning(msg : String) : Unit = {
          log.warn(s"$msg; some vehicle might not know that a player is no longer sitting in it")
        }
        //normally disembarking from a seat
        player.VehicleSeated match {
          case Some(obj_guid) =>
            continent.GUID(obj_guid) match {
              case Some(obj : Mountable) =>
                val seats = obj.Seats.values
                seats.find(seat => seat.Occupant.contains(player)) match {
                  case Some(seat) =>
                    val vel = obj.Velocity.getOrElse(Vector3(0f, 0f, 0f))
                    val has_vel : Int = math.abs(vel.x * vel.y * vel.z).toInt
                    if(seat.Bailable || obj.Velocity.isEmpty || has_vel == 0) { //ugh, float comparison
                      seat.Occupant = None
                      //special actions
                      obj match {
                        case (veh : Vehicle) =>
                          if(seats.count(seat => seat.isOccupied) == 0) {
                            vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(veh, continent, 600L) //start vehicle decay (10m)
                          }
                        case _ => ;
                      }
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
        //should be safe
        player.VehicleSeated = None
        sendResponse(PacketCoding.CreateGamePacket(0, DismountVehicleMsg(player_guid, unk1, unk2)))
        vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.DismountVehicle(player_guid, unk1, unk2))
      }
      else {
        //kicking someone else out of a seat; need to own that seat
        player.VehicleOwned match {
          case Some(vehicle_guid) =>
            continent.GUID(player_guid) match {
              case Some(tplayer : Player) =>
                if(tplayer.VehicleSeated.contains(vehicle_guid)) {
                  continent.GUID(vehicle_guid) match {
                    case Some(obj : Vehicle) =>
                      val seats = obj.Seats.values
                      seats.find(seat => seat.Occupant.contains(tplayer)) match {
                        case Some(seat) =>
                          seat.Occupant = None
                          tplayer.VehicleSeated = None
                          vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(player_guid, unk1, unk2))
                          if(seats.count(seat => seat.isOccupied) == 0) {
                            vehicleService ! VehicleServiceMessage.DelayedVehicleDeconstruction(obj, continent, 600L) //start vehicle decay (10m)
                          }
                        case None =>
                          log.warn(s"DismountVehicleMsg: can not find where player $player_guid is seated in vehicle $vehicle_guid")
                      }
                    case _ =>
                      log.warn(s"DismountVehicleMsg: can not find vehicle $vehicle_guid")
                  }
                }
                else {
                  log.warn(s"DismountVehicleMsg: non-owner player $player trying to kick player $tplayer out of his seat")
                }
              case _ =>
                log.warn(s"DismountVehicleMsg: player $player_guid could not be found to kick")
            }
          case None =>
            log.warn(s"DismountVehicleMsg: $player does not own a vehicle")
        }
      }

    case msg @ DeployRequestMessage(player_guid, entity, unk1, unk2, unk3, pos) =>
      //if you try to deploy, can not undeploy
      log.info("DeployRequest: " + msg)

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
                            vehicleService ! VehicleServiceMessage(continent.Id, VehicleAction.KickPassenger(tplayer.GUID, 4, false))
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
          sendResponse(PacketCoding.CreateGamePacket(0,PlanetsideAttributeMessage(object_guid, attribute_type, attribute_value)))
      }

    case msg @ BattleplanMessage(char_id, player_name, zonr_id, diagrams) =>
      log.info("Battleplan: "+msg)

    case msg @ CreateShortcutMessage(player_guid, slot, unk, add, shortcut) =>
      log.info("CreateShortcutMessage: "+msg)

    case msg @ FriendsRequest(action, friend) =>
      log.info("FriendsRequest: "+msg)

    case msg @ HitHint(source, player_guid) =>
      log.info("HitHint: "+msg)

    case msg @ WeaponDryFireMessage(weapon) =>
      log.info("WeaponDryFireMessage: "+msg)

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
  private def PutEquipmentInSlot(target : Player, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
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
  private def RemoveEquipmentFromSlot(target : Player, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
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
  private def PutInSlot(target : Player, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
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
            PacketCoding.CreateGamePacket(0,
              ObjectCreateDetailedMessage(
                definition.ObjectId,
                localObject.GUID,
                ObjectCreateMessageParent(localTarget.GUID, localIndex),
                definition.Packet.DetailedConstructorData(localObject).get
              )
            )
          )
          if(0 <= localIndex && localIndex < 5) {
            localService ! AvatarServiceMessage(localTarget.Continent, AvatarAction.EquipmentInHand(localTarget.GUID, localIndex, localObject))
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
        private val localAnnounce = vehicleService
        private val localSession : String = sessionId.toString
        private val localPad = pad.Actor
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
  private def RemoveFromSlot(target : Player, obj : Equipment, index : Int) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localTarget = target
        private val localIndex = index
        private val localObject = obj
        private val localObjectGUID = obj.GUID
        private val localAnnounce = self //self may not be the same when it executes
        private val localService = avatarService

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
          localAnnounce ! ResponseToSelf(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(localObjectGUID, 0)))
          if(0 <= localIndex && localIndex < 5) {
            localService ! AvatarServiceMessage(localTarget.Continent, AvatarAction.ObjectDelete(localTarget.GUID, localObjectGUID))
          }
        }
      }
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
    * 2 November 2017
    * Unexpected behavior causes seat mount points to become blocked when a new driver claims the vehicle.
    * For the purposes of ensuring that other players are always aware of the proper permission state of the trunk and seats,
    * packets are intentionally dispatched to the current client to update the states.
    * Perform this action just after any instance where the client would initially gain awareness of the vehicle.
    * The most important examples include either the player or the vehicle itself spawning in for the first time.
    * @param vehicle the `Vehicle`
    */
  def ReloadVehicleAccessPermissions(vehicle : Vehicle) : Unit = {
    val vehicle_guid = vehicle.GUID
    (0 to 3).foreach(group => {
      sendResponse(PacketCoding.CreateGamePacket(0,
        PlanetsideAttributeMessage(vehicle_guid, group + 10, vehicle.PermissionGroup(group).get.id.toLong)
      ))
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

  def failWithError(error : String) = {
    log.error(error)
    sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
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
  final case class ResponseToSelf(pkt : GamePacket)

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
