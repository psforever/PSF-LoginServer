// Copyright (c) 2017 PSForever
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import ServiceManager.Lookup
import net.psforever.objects._
import net.psforever.objects.continent.{Zone, IntergalacticCluster}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment._
import net.psforever.objects.guid.{Task, TaskResolver}
import net.psforever.objects.guid.actor.{Register, Unregister}
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.terminals.{OrderTerminalDefinition, Terminal}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._

import scala.annotation.tailrec
import scala.util.Success

class WorldSessionActor extends Actor with MDCContextAware {
  import WorldSessionActor._
  private[this] val log = org.log4s.getLogger

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var avatarService = Actor.noSender
  var taskResolver = Actor.noSender
  var galaxy = Actor.noSender
  var continent : Zone = Zone.Nowhere

  var clientKeepAlive : Cancellable = WorldSessionActor.DefaultCancellable

  override def postStop() = {
    if(clientKeepAlive != null)
      clientKeepAlive.cancel()

    avatarService ! Leave()
    LivePlayerList.Remove(sessionId) match {
      case Some(tplayer) =>
        if(tplayer.HasGUID) {
          val guid = tplayer.GUID
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ObjectDelete(guid, guid))
          taskResolver ! UnregisterAvatar(tplayer)
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
      } else {
        rightRef = sender()
      }
      context.become(Started)
      ServiceManager.serviceManager ! Lookup("avatar")
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
        case AvatarServiceResponse.ArmorChanged(suit, subtype) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(guid, suit, subtype)))
          }

        case AvatarServiceResponse.EquipmentInHand(slot, item) =>
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

        case AvatarServiceResponse.EquipmentOnGround(pos, orient, item) =>
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

        case AvatarServiceResponse.LoadPlayer(pdata) =>
          if(player.GUID != guid) {
            sendResponse(
              PacketCoding.CreateGamePacket(
                0,
                ObjectCreateMessage(ObjectClass.avatar, guid, pdata)
              )
            )
          }

        case AvatarServiceResponse.ObjectDelete(item_guid, unk) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(item_guid, unk)))
          }

        case AvatarServiceResponse.ObjectHeld(slot) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectHeldMessage(guid, slot, true)))
          }

        case AvatarServiceResponse.PlanetSideAttribute(attribute_type, attribute_value) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(guid, attribute_type, attribute_value)))
          }

        case AvatarServiceResponse.PlayerState(msg, spectating, weaponInHand) =>
          if(player.GUID != guid) {
            val now = System.currentTimeMillis()
            val (location, time, distanceSq) : (Vector3, Long, Float) = if(spectating) {
              (Vector3(2, 2, 2), 0L, 0f)
            }
            else {
              val before = player.lastSeenStreamMessage(guid.guid)
              val dist = WorldSessionActor.DistanceSquared(player.Position, msg.pos)
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

        case AvatarServiceResponse.Reload(mag) =>
          if(player.GUID != guid) {
            sendResponse(PacketCoding.CreateGamePacket(0, ReloadMessage(guid, mag, 0)))
          }

        case _ => ;
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

        case Terminal.BuyEquipment(item) => ;
          tplayer.Fit(item) match {
            case Some(index) =>
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, true)))
              PutEquipmentInSlot(tplayer, item, index)
            case None =>
              sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Buy, false)))
          }

        case Terminal.SellEquipment() =>
          tplayer.FreeHand.Equipment match {
            case Some(item) =>
              if(item.GUID == msg.item_guid) {
                sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage (msg.terminal_guid, TransactionType.Sell, true)))
                RemoveEquipmentFromSlot(tplayer, item, Player.FreeHandSlot)
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
            taskResolver ! UnregisterEquipment(elem.obj)
          })
          //report change
          sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(tplayer.GUID, exosuit, 0)))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.ArmorChanged(tplayer.GUID, exosuit, subtype))
          sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(tplayer.GUID, 4, tplayer.Armor)))
          avatarService ! AvatarServiceMessage(tplayer.Continent, AvatarAction.PlanetsideAttribute(tplayer.GUID, 4, tplayer.Armor))
          //re-draw equipment held in free hand
          beforeFreeHand match {
            //TODO was any previous free hand item deleted?
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
            PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //put items into inventory
          inventory.foreach(entry => {
            PutEquipmentInSlot(tplayer, entry.obj, entry.start)
          })
          //TODO drop items on ground

        case Terminal.NoDeal() =>
          log.warn(s"$tplayer made a request but the terminal rejected the order $msg")
          sendResponse(PacketCoding.CreateGamePacket(0, ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, false)))
      }

    case ListAccountCharacters =>
      val gen : AtomicInteger = new AtomicInteger(1)

      //load characters
      SetCharacterSelectScreenGUID(player, gen)
      val health = player.Health
      val stamina = player.Stamina
      val armor = player.Armor
      player.Spawn
      sendResponse(PacketCoding.CreateGamePacket(0,
        ObjectCreateMessage(ObjectClass.avatar, player.GUID, player.Definition.Packet.ConstructorData(player).get)
      ))
      if(health > 0) { //player can not be dead; stay spawned as alive
        player.Health = health
        player.Stamina = stamina
        player.Armor = armor
      }
      sendResponse(PacketCoding.CreateGamePacket(0, CharacterInfoMessage(15,PlanetSideZoneID(10000), 41605313, player.GUID, false, 6404428)))
      RemoveCharacterSelectScreenGUID(player)

      sendResponse(PacketCoding.CreateGamePacket(0, CharacterInfoMessage(0, PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0)))

    case IntergalacticCluster.GiveWorld(zoneId, zone) =>
      log.info(s"Zone $zoneId has been loaded")
      player.Continent = zoneId
      continent = zone
      taskResolver ! RegisterAvatar(player)

    case PlayerLoaded(tplayer) =>
      log.info(s"Player $tplayer has been loaded")
      //init for whole server
      galaxy ! IntergalacticCluster.RequestZoneInitialization(tplayer)

    case PlayerFailedToLoad(tplayer) =>
      player.Continent match {
        case "tzshvs" =>
          failWithError(s"$tplayer failed to load anywhere")
          //self ! IntergalacticCluster.GiveWorld("", Zone.Nowhere)
        case "tzdrvs" =>
          galaxy ! IntergalacticCluster.GetWorld("tzshvs")
        case "home3" =>
          galaxy ! IntergalacticCluster.GetWorld("tzdrvs")
        case _ =>
          galaxy ! IntergalacticCluster.GetWorld("home3")
      }

    case Zone.ZoneInitialization(/*initList*/_) =>
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

    case IntergalacticCluster.ZoneInitializationComplete(tplayer)=>
      //this will cause the client to send back a BeginZoningMessage packet (see below)
      sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage(continent.Map, continent.ZoneId, 40100,25,true,3770441820L))) //VS Sanctuary
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
      LivePlayerList.Assign(continent.ZoneNumber, sessionId, guid)
      sendResponse(PacketCoding.CreateGamePacket(0, SetCurrentAvatarMessage(guid,0,0)))
      sendResponse(PacketCoding.CreateGamePacket(0, CreateShortcutMessage(guid, 1, 0, true, Shortcut.MEDKIT)))

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
          continent.Actor ! Zone.DropItemOnGround(item, item.Position, item.Orientation) //restore
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

  val terminal = Terminal(PlanetSideGUID(55000), new OrderTerminalDefinition)

  import net.psforever.objects.GlobalDefinitions._
  //this part is created by the player (should be in case of ConnectToWorldRequestMessage, maybe)
  val energy_cell_box1 = AmmoBox(energy_cell)
  val energy_cell_box2 = AmmoBox(energy_cell, 16)
  val bullet_9mm_box1 = AmmoBox(bullet_9mm)
  val bullet_9mm_box2 = AmmoBox(bullet_9mm)
  val bullet_9mm_box3 = AmmoBox(bullet_9mm)
  val bullet_9mm_box4 = AmmoBox(bullet_9mm, 25)
  val bullet_9mm_AP_box = AmmoBox(bullet_9mm_AP)
  val melee_ammo_box = AmmoBox(melee_ammo)
  val
  beamer1 = Tool(beamer)
  beamer1.AmmoSlots.head.Box = energy_cell_box2
  val
  suppressor1 = Tool(suppressor)
  suppressor1.AmmoSlots.head.Box = bullet_9mm_box4
  val
  forceblade1 = Tool(forceblade)
  forceblade1.AmmoSlots.head.Box = melee_ammo_box
  val rek = SimpleItem(remote_electronics_kit)
  val extra_rek = SimpleItem(remote_electronics_kit)
  val
  player = Player("IlllIIIlllIlIllIlllIllI", PlanetSideEmpire.VS, CharacterGender.Female, 41, 1)
  player.Position = Vector3(3674.8438f, 2726.789f, 91.15625f)
  player.Orientation = Vector3(0f, 0f, 90f)
  //player.Continent = "home3"
  player.Slot(0).Equipment = beamer1
  player.Slot(2).Equipment = suppressor1
  player.Slot(4).Equipment = forceblade1
  player.Slot(6).Equipment = bullet_9mm_box1
  player.Slot(9).Equipment = bullet_9mm_box2
  player.Slot(12).Equipment = bullet_9mm_box3
  player.Slot(33).Equipment = bullet_9mm_AP_box
  player.Slot(36).Equipment = energy_cell_box1
  player.Slot(39).Equipment = rek
  player.Slot(5).Equipment.get.asInstanceOf[LockerContainer].Inventory += 0 -> extra_rek

  //for player2
  val energy_cell_box3 = AmmoBox(PlanetSideGUID(187), energy_cell)
  val energy_cell_box4 = AmmoBox(PlanetSideGUID(177), energy_cell, 16)
  val bullet_9mm_box5 = AmmoBox(PlanetSideGUID(183), bullet_9mm)
  val bullet_9mm_box6 = AmmoBox(PlanetSideGUID(184), bullet_9mm)
  val bullet_9mm_box7 = AmmoBox(PlanetSideGUID(185), bullet_9mm)
  val bullet_9mm_box8 = AmmoBox(PlanetSideGUID(179), bullet_9mm, 25)
  val bullet_9mm_AP_box2 = AmmoBox(PlanetSideGUID(186), bullet_9mm_AP)
  val melee_ammo_box2 = AmmoBox(PlanetSideGUID(181), melee_ammo)

  val
  beamer2 = Tool(PlanetSideGUID(176), beamer)
  beamer2.AmmoSlots.head.Box = energy_cell_box4
  val
  suppressor2 = Tool(PlanetSideGUID(178), suppressor)
  suppressor2.AmmoSlots.head.Box = bullet_9mm_box8
  val
  forceblade2 = Tool(PlanetSideGUID(180), forceblade)
  forceblade2.AmmoSlots.head.Box = melee_ammo_box2
  val
  rek2 = SimpleItem(PlanetSideGUID(188), remote_electronics_kit)
  val
  player2 = Player(PlanetSideGUID(275), "Doppelganger", PlanetSideEmpire.NC, CharacterGender.Female, 41, 1)
  player2.Position = Vector3(3680f, 2726.789f, 91.15625f)
  player2.Orientation = Vector3(0f, 0f, 0f)
  player2.Continent = "home3"
  player2.Slot(0).Equipment = beamer2
  player2.Slot(2).Equipment = suppressor2
  player2.Slot(4).Equipment = forceblade2
  player2.Slot(5).Equipment.get.GUID = PlanetSideGUID(182)
  player2.Slot(6).Equipment = bullet_9mm_box5
  player2.Slot(9).Equipment = bullet_9mm_box6
  player2.Slot(12).Equipment = bullet_9mm_box7
  player2.Slot(33).Equipment = bullet_9mm_AP_box2
  player2.Slot(36).Equipment = energy_cell_box3
  player2.Slot(39).Equipment = rek2
  player2.Spawn

  val hellfire_ammo_box = AmmoBox(PlanetSideGUID(432), hellfire_ammo)

  val
  fury1 = Vehicle(PlanetSideGUID(313), fury)
  fury1.Faction = PlanetSideEmpire.VS
  fury1.Position = Vector3(3674.8438f, 2732f, 91.15625f)
  fury1.Orientation = Vector3(0.0f, 0.0f, 90.0f)
  fury1.WeaponControlledFromSeat(0).get.GUID = PlanetSideGUID(300)
  fury1.WeaponControlledFromSeat(0).get.AmmoSlots.head.Box = hellfire_ammo_box

  val object2Hex = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(275), player2.Definition.Packet.ConstructorData(player2).get)
  val furyHex = ObjectCreateMessage(ObjectClass.fury, PlanetSideGUID(313), fury1.Definition.Packet.ConstructorData(fury1).get)

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>
      val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
      log.info(s"New world login to $server with Token:$token. $clientVersion")
      self ! ListAccountCharacters

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
          galaxy ! IntergalacticCluster.GetWorld("home3")

          import scala.concurrent.duration._
          import scala.concurrent.ExecutionContext.Implicits.global
          clientKeepAlive.cancel
          clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())
        case default =>
          log.error("Unsupported " + default + " in " + msg)
      }

    case KeepAliveMessage(code) =>
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage()))

    case msg @ BeginZoningMessage() =>
      log.info("Reticulating splines ...")
      //map-specific initializations
      //TODO continent.ZoneConfiguration()
      sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(2), PlanetSideEmpire.VS))) //HART building C
      sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(29), PlanetSideEmpire.NC))) //South Villa Gun Tower
      //sendResponse(PacketCoding.CreateGamePacket(0, object2Hex))
      //sendResponse(PacketCoding.CreateGamePacket(0, furyHex))

      sendResponse(PacketCoding.CreateGamePacket(0, TimeOfDayMessage(1191182336)))
      sendResponse(PacketCoding.CreateGamePacket(0, ReplicationStreamMessage(5, Some(6), Vector(SquadListing())))) //clear squad list

      //load active players in zone
      LivePlayerList.ZonePopulation(continent.ZoneNumber, _ => true).foreach(char => {
        sendResponse(
          PacketCoding.CreateGamePacket(0,
            ObjectCreateMessage(ObjectClass.avatar, char.GUID, char.Definition.Packet.ConstructorData(char).get)
          )
        )
      })
      //render Equipment that was dropped into zone before the player arrived
      continent.EquipmentOnGround.toList.foreach(item => {
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

      avatarService ! Join(player.Continent)
      self ! SetCurrentAvatar(player)

    case msg @ PlayerStateMessageUpstream(avatar_guid, pos, vel, yaw, pitch, yaw_upper, seq_time, unk3, is_crouching, is_jumping, unk4, is_cloaking, unk5, unk6) =>
      player.Position = pos
      player.Velocity = vel
      player.Orientation = Vector3(player.Orientation.x, pitch, yaw)
      player.FacingYawUpper = yaw_upper
      player.Crouching = is_crouching
      player.Jumping = is_jumping

      val wepInHand : Boolean = player.Slot(player.DrawnSlot).Equipment match {
        case Some(item) => item.Definition == bolt_driver
        case None => false
      }
      avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.PlayerState(avatar_guid, msg, player.Spectator, wepInHand))
    //log.info("PlayerState: " + msg)

    case msg @ ChildObjectStateMessage(object_guid, pitch, yaw) =>
      //log.info("ChildObjectState: " + msg)

    case msg @ VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk5, unk6, unk7, wheels, unk9, unkA) =>
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
            continent.Actor ! Zone.DropItemOnGround(item, player.Position, orient) //TODO do I need to wait for callback?
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
      continent.Actor ! Zone.GetItemOnGround(player, item_guid)

    case msg @ ReloadMessage(item_guid, ammo_clip, unk1) =>
      log.info("Reload: " + msg)
      val reloadValue = player.Slot(player.DrawnSlot).Equipment match {
        case Some(item) =>
          item match {
            case tool : Tool =>
              tool.FireMode.Magazine
            case _ =>
              0
          }
        case None =>
          0
      }
      //TODO hunt for ammunition in inventory
      if(reloadValue > 0) {
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
      // TODO: Make sure this is the correct response in all cases
      player.Find(object_guid) match {
        case Some(slot) =>
          taskResolver ! RemoveEquipmentFromSlot(player, player.Slot(slot).Equipment.get, slot)
          log.info("RequestDestroy: " + msg)
        case None =>
          sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(object_guid, 0)))
          log.warn(s"RequestDestroy: object $object_guid not found")
      }

    case msg @ ObjectDeleteMessage(object_guid, unk1) =>
      sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(object_guid, 0)))
      log.info("ObjectDelete: " + msg)

    case msg @ MoveItemMessage(item_guid, avatar_guid_1, avatar_guid_2, dest, unk1) =>
      player.Find(item_guid) match {
        case Some(index) =>
          val indexSlot = player.Slot(index)
          var itemOpt = indexSlot.Equipment //use this to short circuit
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
            log.info(s"MoveItem: $item_guid moved from $avatar_guid_1 @ $index to $avatar_guid_1 @ $dest")
            indexSlot.Equipment = None
            destItem match { //do we have a swap item?
              case Some(entry) => //yes, swap
                val item2 = entry.obj
                player.Slot(entry.start).Equipment = None //remove item2 to make room for item
                destSlot.Equipment = item //in case dest and index could block each other
                (indexSlot.Equipment = entry.obj) match {
                  case Some(_) => //item and item2 swapped places successfully
                    log.info(s"MoveItem: ${item2.GUID} swapped to $avatar_guid_1 @ $index")
                    //we must shuffle items around cleanly to avoid causing icons to "disappear"
                    if(index == Player.FreeHandSlot) { //temporarily put in safe location, A -> C
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
            sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(avatar_guid_1, item_guid, dest)))
            if(0 <= dest && dest < 5) {
              avatarService ! AvatarServiceMessage(player.Continent, AvatarAction.EquipmentInHand(player.GUID, dest, item))
            }
          }
        case None =>
          log.info(s"MoveItem: $avatar_guid_1 wanted to move the item $item_guid but could not find it")
      }

    case msg @ ChangeAmmoMessage(item_guid, unk1) =>
      log.info("ChangeAmmo: " + msg)

    case msg @ UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType) =>
      log.info("UseItem: " + msg)
      // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
      // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
      if (itemType != 121) sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType)))
      if (itemType == 121 && !unk3){ // TODO : medkit use ?!
        sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, 0, unk3, unk4, unk5, unk6, unk7, unk8, itemType)))
        sendResponse(PacketCoding.CreateGamePacket(0, PlanetsideAttributeMessage(avatar_guid, 0, 100))) // avatar with 100 hp
        sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(PlanetSideGUID(unk1), 2)))
      }
      if (unk1 == 0 && !unk3 && unk7 == 25) {
        // TODO: This should only actually be sent to doors upon opening; may break non-door items upon use
        sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(object_guid, 16)))
      }
    
    case msg @ UnuseItemMessage(player_guid, item) =>
      log.info("UnuseItem: " + msg)

    case msg @ DeployObjectMessage(guid, unk1, pos, roll, pitch, yaw, unk2) =>
      log.info("DeployObject: " + msg)

    case msg @ GenericObjectStateMsg(object_guid, unk1) =>
      log.info("GenericObjectState: " + msg)

    case msg @ ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
      terminal.Actor ! Terminal.Request(player, msg)
      log.info("ItemTransaction: " + msg)

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

    case msg @ MountVehicleMsg(player_guid, vehicle_guid, unk) =>
      sendResponse(PacketCoding.CreateGamePacket(0, ObjectAttachMessage(vehicle_guid,player_guid,0)))
      log.info("MounVehicleMsg: "+msg)

    case msg @ DismountVehicleMsg(player_guid, unk1, unk2) =>
      sendResponse(PacketCoding.CreateGamePacket(0, msg)) //should be safe; replace with ObjectDetachMessage later
      log.info("DismountVehicleMsg: " + msg)

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

    case msg @ PlanetsideAttributeMessage(avatar_guid, attribute_type, attribute_value) =>
      log.info("PlanetsideAttributeMessage: "+msg)
      sendResponse(PacketCoding.CreateGamePacket(0,PlanetsideAttributeMessage(avatar_guid, attribute_type, attribute_value)))

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
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Use `func` on any discovered `Equipment` to transform items into tasking, and add the tasking to a `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param func the function used to build tasking from any discovered `Equipment`
    * @param list a persistent `List` of `Equipment` tasking
    * @return a `List` of `Equipment` tasking
    */
  @tailrec private def recursiveHolsterTaskBuilding(iter : Iterator[EquipmentSlot], func : ((Equipment)=>TaskResolver.GiveTask), list : List[TaskResolver.GiveTask] = Nil) : List[TaskResolver.GiveTask] = {
    if(!iter.hasNext) {
      list
    }
    else {
      iter.next.Equipment match {
        case Some(item) =>
          recursiveHolsterTaskBuilding(iter, func, list :+ func(item))
        case None =>
          recursiveHolsterTaskBuilding(iter, func, list)
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
    * @see `RegisterEquipment`
    * @see `PutInSlot`
    */
  private def PutEquipmentInSlot(target : Player, obj : Equipment, index : Int) : Unit = {
    val regTask = RegisterEquipment(obj)
    obj match {
      case tool : Tool =>
        val linearToolTask = TaskResolver.GiveTask(regTask.task) +: regTask.subs
        taskResolver ! TaskResolver.GiveTask(PutInSlot(target, tool, index).task, linearToolTask)
      case _ =>
        taskResolver ! TaskResolver.GiveTask(PutInSlot(target, obj, index).task, List(regTask))
    }
  }

  /**
    * Construct tasking that coordinates the following:<br>
    * 1) Remove a new piece of `Equipment` from where it is currently stored.<br>
    * 2) Once it is removed, un-register the `Equipment`'s globally unique identifier.
    * @param target the object that currently possesses the `Equipment`
    * @param obj the `Equipment`
    * @param index the slot from where the `Equipment` will be removed
    * @see `UnregisterEquipment`
    * @see `RemoveFromSlot`
    */
  private def RemoveEquipmentFromSlot(target : Player, obj : Equipment, index : Int) : Unit = {
    val regTask = UnregisterEquipment(obj)
    //to avoid an error from a GUID-less object from being searchable, it is removed from the inventory first
    obj match {
      case _ : Tool =>
        taskResolver ! TaskResolver.GiveTask(regTask.task, RemoveFromSlot(target, obj, index) +: regTask.subs)
      case _ =>
        taskResolver ! TaskResolver.GiveTask(regTask.task, RemoveFromSlot(target, obj, index) :: Nil)
    }
  }

  /**
    * Construct tasking that registers an object with the a globally unique identifier selected from a pool of numbers.
    * The object in question is not considered to have any form of internal complexity.
    * @param obj the object being registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterObjectTask(obj : IdentifiableEntity) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localObject = obj
        private val localAccessor = continent.GUID

        override def isComplete : Task.Resolution.Value = {
          try {
            localObject.GUID
            Task.Resolution.Success
          }
          catch {
            case _ : Exception =>
              Task.Resolution.Incomplete
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localAccessor ! Register(localObject, resolver)
        }
      })
  }

  /**
    * Construct tasking that registers an object that is an object of type `Tool`.
    * `Tool` objects have internal structures called "ammo slots;"
    * each ammo slot contains a register-able `AmmoBox` object.
    * @param obj the object being registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterTool(obj : Tool) : TaskResolver.GiveTask = {
    val ammoTasks : List[TaskResolver.GiveTask] = (0 until obj.MaxAmmoSlot).map(ammoIndex => RegisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(RegisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that registers an object, determining whether it is a complex object of type `Tool` or a more simple object type.
    * @param obj the object being registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def RegisterEquipment(obj : Equipment) : TaskResolver.GiveTask = {
    obj match {
      case tool : Tool =>
        RegisterTool(tool)
      case _ =>
        RegisterObjectTask(obj)
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
          resolver ! scala.util.Success(localObject)
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
            avatarService ! AvatarServiceMessage(localTarget.Continent, AvatarAction.EquipmentInHand(localTarget.GUID, localIndex, localObject))
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
    val holsterTasks = recursiveHolsterTaskBuilding(tplayer.Holsters().iterator, RegisterEquipment)
    val fifthHolsterTask = tplayer.Slot(5).Equipment match {
      case Some(locker) =>
        RegisterObjectTask(locker) :: locker.asInstanceOf[LockerContainer].Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => RegisterEquipment(entry.obj)}).toList
      case None =>
        List.empty[TaskResolver.GiveTask];
    }
    val inventoryTasks = tplayer.Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => RegisterEquipment(entry.obj)})
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
          localAnnounce ! PlayerLoaded(localPlayer) //alerts WSA
          resolver ! scala.util.Success(localPlayer)
        }

        override def onFailure(ex : Throwable) : Unit = {
          localAnnounce ! PlayerFailedToLoad(localPlayer) //alerts WSA
        }
      }, RegisterObjectTask(tplayer) +: (holsterTasks ++ fifthHolsterTask ++ inventoryTasks)
    )
  }

  /**
    * Construct tasking that un-registers an object.
    * The object in question is not considered to have any form of internal complexity.
    * @param obj the object being un-registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def UnregisterObjectTask(obj : IdentifiableEntity) : TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localObject = obj
        private val localAccessor = continent.GUID

        override def isComplete : Task.Resolution.Value = {
          try {
            localObject.GUID
            Task.Resolution.Incomplete
          }
          catch {
            case _ : Exception =>
              Task.Resolution.Success
          }
        }

        def Execute(resolver : ActorRef) : Unit = {
          localAccessor ! Unregister(localObject, resolver)
        }
      }
    )
  }

  /**
    * Construct tasking that un-registers an object that is an object of type `Tool`.
    * `Tool` objects have internal structures called "ammo slots;"
    * each ammo slot contains a register-able `AmmoBox` object.
    * @param obj the object being un-registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def UnregisterTool(obj : Tool) : TaskResolver.GiveTask = {
    val ammoTasks : List[TaskResolver.GiveTask] = (0 until obj.MaxAmmoSlot).map(ammoIndex => UnregisterObjectTask(obj.AmmoSlots(ammoIndex).Box)).toList
    TaskResolver.GiveTask(UnregisterObjectTask(obj).task, ammoTasks)
  }

  /**
    * Construct tasking that un-registers an object, determining whether it is a complex object of type `Tool` or a more simple object type.
    * @param obj the object being registered
    * @return a `TaskResolver.GiveTask` message
    */
  private def UnregisterEquipment(obj : Equipment) : TaskResolver.GiveTask = {
    obj match {
      case tool : Tool =>
        UnregisterTool(tool)
      case _ =>
        UnregisterObjectTask(obj)
    }
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
          resolver ! scala.util.Success(localObject)
        }

        override def onSuccess() : Unit = {
          localAnnounce ! ResponseToSelf(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(localObjectGUID, 0)))
          if(0 <= localIndex && localIndex < 5) {
            avatarService ! AvatarServiceMessage(localTarget.Continent, AvatarAction.ObjectDelete(localTarget.GUID, localObjectGUID))
          }
        }
      })
  }

  /**
    * Construct tasking that un-registers all aspects of a `Player` avatar.
    * `Players` are complex objects that contain a variety of other register-able objects and each of these objects much be handled.
    * @param tplayer the avatar `Player`
    * @return a `TaskResolver.GiveTask` message
    */
  private def UnregisterAvatar(tplayer : Player) : TaskResolver.GiveTask = {
    val holsterTasks = recursiveHolsterTaskBuilding(tplayer.Holsters().iterator, UnregisterEquipment)
    val inventoryTasks = tplayer.Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => UnregisterEquipment(entry.obj)})
    val fifthHolsterTask = tplayer.Slot(5).Equipment match {
      case Some(locker) =>
        UnregisterObjectTask(locker) :: locker.asInstanceOf[LockerContainer].Inventory.Items.map({ case((_ : Int, entry : InventoryItem)) => UnregisterEquipment(entry.obj)}).toList
      case None =>
        List.empty[TaskResolver.GiveTask];
    }
    TaskResolver.GiveTask(UnregisterObjectTask(tplayer).task, holsterTasks ++ fifthHolsterTask ++ inventoryTasks)
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

  /**
    * A placeholder `Cancellable` object.
    */
  private final val DefaultCancellable = new Cancellable() {
    def cancel : Boolean = true
    def isCancelled() : Boolean = true
  }

  def Distance(pos1 : Vector3, pos2 : Vector3) : Float = {
    math.sqrt(DistanceSquared(pos1, pos2)).toFloat
  }

  def DistanceSquared(pos1 : Vector3, pos2 : Vector3) : Float = {
    val dx : Float = pos1.x - pos2.x
    val dy : Float = pos1.y - pos2.y
    val dz : Float = pos1.z - pos2.z
    (dx * dx) + (dy * dy) + (dz * dz)
  }
}
