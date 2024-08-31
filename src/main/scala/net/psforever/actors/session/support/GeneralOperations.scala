// Copyright (c) 2024 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.sourcing.PlayerSource

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
//
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.login.WorldSession._
import net.psforever.objects._
import net.psforever.objects.avatar._
import net.psforever.objects.ce._
import net.psforever.objects.definition._
import net.psforever.objects.equipment._
import net.psforever.objects.guid._
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.locker.LockerContainer
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.vehicles._
import net.psforever.objects.vital._
import net.psforever.objects.zones._
import net.psforever.packet._
import net.psforever.packet.game.{ActionCancelMessage, AvatarFirstTimeEventMessage, AvatarImplantMessage, AvatarJumpMessage, BattleplanMessage, BindPlayerMessage, ChangeShortcutBankMessage, CharacterRequestMessage, ConnectToWorldRequestMessage, CreateShortcutMessage, DeployObjectMessage, DisplayedAwardMessage, EmoteMsg, FacilityBenefitShieldChargeRequestMessage, FriendsRequest, GenericActionMessage, GenericCollisionMsg, GenericObjectActionAtPositionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HitHint, InvalidTerrainMessage, LootItemMessage, MoveItemMessage, ObjectDetectedMessage, ObjectHeldMessage, PickupItemMessage, PlanetsideAttributeMessage, PlayerStateMessageUpstream, RequestDestroyMessage, TargetingImplantRequest, TradeMessage, UnuseItemMessage, UseItemMessage, ZipLineMessage}
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game._
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.Service
import net.psforever.types._
import net.psforever.util.Config

trait GeneralFunctions extends CommonSessionInterfacingFunctionality {
  def ops: GeneralOperations

  def handleConnectToWorldRequest(pkt: ConnectToWorldRequestMessage): Unit

  def handleCharacterCreateRequest(pkt: CharacterCreateRequestMessage): Unit

  def handleCharacterRequest(pkt: CharacterRequestMessage): Unit

  def handlePlayerStateUpstream(pkt: PlayerStateMessageUpstream): Unit

  def handleVoiceHostRequest(pkt: VoiceHostRequest): Unit

  def handleVoiceHostInfo(pkt: VoiceHostInfo): Unit

  def handleEmote(pkt: EmoteMsg): Unit

  def handleDropItem(pkt: DropItemMessage): Unit

  def handlePickupItem(pkt: PickupItemMessage): Unit

  def handleObjectHeld(pkt: ObjectHeldMessage): Unit

  def handleAvatarJump(pkt: AvatarJumpMessage): Unit

  def handleZipLine(pkt: ZipLineMessage): Unit

  def handleRequestDestroy(pkt: RequestDestroyMessage): Unit

  def handleMoveItem(pkt: MoveItemMessage): Unit

  def handleLootItem(pkt: LootItemMessage): Unit

  def handleAvatarImplant(pkt: AvatarImplantMessage): Unit

  def handleUseItem(pkt: UseItemMessage): Unit

  def handleUnuseItem(pkt: UnuseItemMessage): Unit

  def handleDeployObject(pkt: DeployObjectMessage): Unit

  def handlePlanetsideAttribute(pkt: PlanetsideAttributeMessage): Unit

  def handleGenericObjectAction(pkt: GenericObjectActionMessage): Unit

  def handleGenericObjectActionAtPosition(pkt: GenericObjectActionAtPositionMessage): Unit

  def handleGenericObjectState(pkt: GenericObjectStateMsg): Unit

  def handleGenericAction(pkt: GenericActionMessage): Unit

  def handleGenericCollision(pkt: GenericCollisionMsg): Unit

  def handleAvatarFirstTimeEvent(pkt: AvatarFirstTimeEventMessage): Unit

  def handleBugReport(pkt: PlanetSideGamePacket): Unit

  def handleFacilityBenefitShieldChargeRequest(pkt: FacilityBenefitShieldChargeRequestMessage): Unit

  def handleBattleplan(pkt: BattleplanMessage): Unit

  def handleBindPlayer(pkt: BindPlayerMessage): Unit

  def handleCreateShortcut(pkt: CreateShortcutMessage): Unit

  def handleChangeShortcutBank(pkt: ChangeShortcutBankMessage): Unit

  def handleFriendRequest(pkt: FriendsRequest): Unit

  def handleInvalidTerrain(pkt: InvalidTerrainMessage): Unit

  def handleActionCancel(pkt: ActionCancelMessage): Unit

  def handleTrade(pkt: TradeMessage): Unit

  def handleDisplayedAward(pkt: DisplayedAwardMessage): Unit

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit

  def handleHitHint(pkt: HitHint): Unit

  /* messages */

  def handleRenewCharSavedTimer(): Unit

  def handleRenewCharSavedTimerMsg(): Unit

  def handleSetAvatar(avatar: Avatar): Unit

  def handleReceiveAccountData(account: Account): Unit

  def handleUseCooldownRenew: BasicDefinition => Unit

  def handleAvatarResponse(avatar: Avatar): Unit

  def handleSetSpeed(speed: Float): Unit

  def handleSetFlying(flying: Boolean): Unit

  def handleSetSpectator(spectator: Boolean): Unit

  def handleKick(player: Player, time: Option[Long]): Unit

  def handleSilenced(isSilenced: Boolean): Unit

  def handleItemPutInSlot(msg: Containable.ItemPutInSlot): Unit

  def handleCanNotPutItemInSlot(msg: Containable.CanNotPutItemInSlot): Unit

  def handleReceiveDefaultMessage(default: Any, sender: ActorRef): Unit
}

class GeneralOperations(
                         val sessionLogic: SessionData,
                         val avatarActor: typed.ActorRef[AvatarActor.Command],
                         implicit val context: ActorContext
                       ) extends CommonSessionInterfacingFunctionality {
  private[session] var progressBarValue: Option[Float] = None
  private[session] var accessedContainer: Option[PlanetSideGameObject with Container] = None
  private[session] var recentTeleportAttempt: Long = 0
  private[session] var kitToBeUsed: Option[PlanetSideGUID] = None
  // If a special item (e.g. LLU) has been attached to the player the GUID should be stored here, or cleared when dropped, since the drop hotkey doesn't send the GUID of the object to be dropped.
  private[session] var specialItemSlotGuid: Option[PlanetSideGUID] = None
  private[session] val collisionHistory: mutable.HashMap[ActorRef, Long] = mutable.HashMap()
  private[session] var heightLast: Float = 0f
  private[session] var heightTrend: Boolean = false //up = true, down = false
  private[session] var heightHistory: Float = 0f
  private[session] var progressBarUpdate: Cancellable = Default.Cancellable
  private var charSavedTimer: Cancellable = Default.Cancellable

  /**
   * Enforce constraints on bulk purchases as determined by a given player's previous purchase times and hard acquisition delays.
   * Intended to assist in sanitizing loadout information from the perspective of the player, or target owner.
   * The equipment is expected to be unregistered and already fitted to their ultimate slot in the target container.
   * @param player the player whose purchasing constraints are to be tested
   * @param target the location in which the equipment will be stowed
   * @param slots  the equipment, in the standard object-slot format container
   */
  def applyPurchaseTimersBeforePackingLoadout(
                                               player: Player,
                                               target: PlanetSideServerObject with Container,
                                               slots: List[InventoryItem]
                                             ): Unit = {
    slots.foreach { item =>
      player.avatar.purchaseCooldown(item.obj.Definition) match {
        case Some(_) => ()
        case None if Avatar.purchaseCooldowns.contains(item.obj.Definition) =>
          avatarActor ! AvatarActor.UpdatePurchaseTime(item.obj.Definition)
          TaskWorkflow.execute(PutLoadoutEquipmentInInventory(target)(item.obj, item.start))
        case None =>
          TaskWorkflow.execute(PutLoadoutEquipmentInInventory(target)(item.obj, item.start))
      }
    }
  }

  def dropSpecialSlotItem(): Unit = {
    specialItemSlotGuid.foreach { guid =>
      specialItemSlotGuid = None
      player.Carrying = None
      (continent.GUID(guid) match {
        case Some(llu: CaptureFlag) => Some((llu, llu.Carrier))
        case _ => None
      }) match {
        case Some((llu, Some(carrier: Player)))
          if carrier.GUID == player.GUID && !player.isAlive =>
          player.LastDamage.foreach { damage =>
            damage
              .interaction
              .adversarial
              .map { _.attacker }
              .collect {
                case attacker
                  if attacker.Faction != player.Faction &&
                    System.currentTimeMillis() - llu.LastCollectionTime >= Config.app.game.experience.cep.lluSlayerCreditDuration.toMillis =>
                  continent.AvatarEvents ! AvatarServiceMessage(
                    attacker.Name,
                    AvatarAction.AwardCep(attacker.CharId, Config.app.game.experience.cep.lluSlayerCredit)
                  )
              }
          }
          if (!CaptureFlagManager.reasonToLoseFlagViolently(continent, Some(guid), player)) {
            continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
          }
        case Some((llu, Some(carrier: Player)))
          if carrier.GUID == player.GUID &&
            !CaptureFlagManager.reasonToLoseFlagViolently(continent, Some(guid), player) =>
          continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
        case Some((_, Some(carrier: Player))) =>
          log.warn(s"${player.toString} tried to drop LLU, but it is currently held by ${carrier.toString}")
        case Some((_, None)) =>
          log.warn(s"${player.toString} tried to drop LLU, but nobody is holding it.")
        case None =>
          log.warn(s"${player.toString} tried to drop a special item that wasn't recognized. GUID: $guid")
      }
    }
  }

  def setupProgressChange(rate: Float, finishedAction: () => Unit, stepAction: Float => Boolean): Unit = {
    if (progressBarValue.isEmpty) {
      progressBarValue = Some(-rate)
      context.self ! CommonMessages.ProgressEvent(rate, finishedAction, stepAction)
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
  def handleProgressChange(
                            delta: Float,
                            completionAction: () => Unit,
                            tickAction: Float => Boolean,
                            tick: Long
                          ): Unit = {
    progressBarUpdate.cancel()
    progressBarValue.foreach { value =>
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
            delay = 100 milliseconds,
            context.self,
            CommonMessages.ProgressEvent(delta, completionAction, tickAction)
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
            tick.milliseconds,
            context.self,
            CommonMessages.ProgressEvent(delta, completionAction, tickAction, tick)
          )
        } else {
          progressBarValue = None
        }
      }
    }
  }

  /**
   * For whatever container the character considers itself trying to access,
   * initiate protocol to "access" it.
   */
  def accessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        accessVehicleContents(v)
      case o: LockerContainer =>
        accessGenericContainer(o)
      case p: Player if p.isBackpack =>
        accessCorpseContents(p)
      case p: PlanetSideServerObject with Container =>
        accessedContainer = Some(p)
      case _ => ()
    }
  }

  /**
   * For the target container, initiate protocol to "access" it.
   */
  private def accessGenericContainer(container: PlanetSideServerObject with Container): Unit = {
    accessedContainer = Some(container)
    displayContainerContents(container.GUID, container.Inventory.Items)
  }

  /**
   * Common preparation for interfacing with a vehicle trunk.
   * Join a vehicle-specific group for shared updates.
   * Construct every object in the vehicle's inventory for shared manipulation updates.
   * @see `Container.Inventory`
   * @see `GridInventory.Items`
   * @param vehicle the vehicle
   */
  private def accessVehicleContents(vehicle: Vehicle): Unit = {
    accessedContainer = Some(vehicle)
    accessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    displayContainerContents(vehicle.GUID, vehicle.Inventory.Items)
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
  private def accessCorpseContents(tplayer: Player): Unit = {
    accessedContainer = Some(tplayer)
    accessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    displayContainerContents(tplayer.GUID, tplayer.HolsterItems())
    displayContainerContents(tplayer.GUID, tplayer.Inventory.Items)
  }

  /**
   * Join an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  private def accessContainerChannel(events: ActorRef, channel: String): Unit = {
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
  private def displayContainerContents(containerId: PlanetSideGUID, items: Iterable[InventoryItem]): Unit = {
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
   * For whatever container the character considers itself accessing,
   * initiate protocol to release it from "access".
   */
  def unaccessContainer(): Unit = {
    accessedContainer.foreach { container => unaccessContainer(container) }
  }

  /**
   * For the target container, initiate protocol to release it from "access".
   */
  def unaccessContainer(container: Container): Unit = {
    container match {
      case v: Vehicle =>
        unaccessVehicleContainer(v)
      case o: LockerContainer =>
        unaccessGenericContainer(o)
        avatarActor ! AvatarActor.SaveLocker()
      case p: Player if p.isBackpack =>
        unaccessCorpseContainer(p)
      case _: PlanetSideServerObject with Container =>
        accessedContainer = None
      case _ => ()
    }
  }

  private def unaccessGenericContainer(container: Container): Unit = {
    accessedContainer = None
    hideContainerContents(container.Inventory.Items)
  }

  /**
   * Common preparation for disengaging from a vehicle.
   * Leave the vehicle-specific group that was used for shared updates.
   * Deconstruct every object in the vehicle's inventory.
   * @param vehicle the vehicle
   */
  private def unaccessVehicleContainer(vehicle: Vehicle): Unit = {
    accessedContainer = None
    if (vehicle.AccessingTrunk.contains(player.GUID)) {
      vehicle.AccessingTrunk = None
    }
    unaccessContainerChannel(continent.VehicleEvents, vehicle.Actor.toString)
    hideContainerContents(vehicle.Inventory.Items)
  }

  /**
   * Common preparation for disengaging from a corpse.
   * Leave the corpse-specific group that was used for shared updates.
   * Deconstruct every object in the backpack's inventory.
   * @param tplayer the corpse
   */
  private def unaccessCorpseContainer(tplayer: Player): Unit = {
    accessedContainer = None
    unaccessContainerChannel(continent.AvatarEvents, tplayer.Actor.toString)
    hideContainerContents(tplayer.HolsterItems())
    hideContainerContents(tplayer.Inventory.Items)
  }

  /**
   * Leave an entity-specific group for shared updates.
   * @param events the event system bus to which to subscribe
   * @param channel the channel name
   */
  private def unaccessContainerChannel(events: ActorRef, channel: String): Unit = {
    events ! Service.Leave(Some(channel))
  }

  /**
   * Forget the contents of a container by deleting that content from the local client.
   * @see `InventoryItem`
   * @see `ObjectDeleteMessage`
   * @param items a list of the entities to be depicted
   */
  private def hideContainerContents(items: List[InventoryItem]): Unit = {
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
  def normalItemDrop(obj: PlanetSideServerObject with Container, zone: Zone)(item: Equipment): Unit = {
    zone.Ground.tell(Zone.Ground.DropItem(item, obj.Position, Vector3.z(obj.Orientation.z)), obj.Actor)
  }

  /**
   * Given an object globally unique identifier, search in a given location for it.
   * @param objectGuid the object
   * @param parent a `Container` object wherein to search
   * @return an optional tuple that contains two values;
   *         the first value is the container that matched correctly with the object's GUID;
   *         the second value is the slot position of the object
   */
  def findInLocalContainer(
                            objectGuid: PlanetSideGUID
                          )(parent: PlanetSideServerObject with Container): Option[(PlanetSideServerObject with Container, Option[Int])] = {
    parent.Find(objectGuid).flatMap { slot => Some((parent, Some(slot))) }
  }

  /**
   * na
   * @param targetGuid na
   * @param unk1 na
   * @param unk2 na
   */
  def hackObject(targetGuid: PlanetSideGUID, unk1: Long, unk2: HackState7): Unit = {
    sendResponse(HackMessage(HackState1.Unk0, targetGuid, player_guid=Service.defaultPlayerGUID, progress=100, unk1.toFloat, HackState.Hacked, unk2))
  }

  /**
   * Send a PlanetsideAttributeMessage packet to the client
   * @param targetGuid The target of the attribute
   * @param attributeNumber The attribute number
   * @param attributeValue The attribute value
   */
  def sendPlanetsideAttributeMessage(
                                      targetGuid: PlanetSideGUID,
                                      attributeNumber: PlanetsideAttributeEnum,
                                      attributeValue: Long
                                    ): Unit = {
    sendResponse(PlanetsideAttributeMessage(targetGuid, attributeNumber, attributeValue))
  }

  /**
   * The player has lost the will to live and must be killed.
   * @see `Vitality`<br>
   *       `PlayerSuicide`
   * @param tplayer the player to be killed
   */
  def suicide(tplayer: Player): Unit = {
    tplayer.LogActivity(PlayerSuicide(PlayerSource(tplayer)))
    tplayer.Actor ! Player.Die()
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
  def updateDeployableUIElements(list: List[(Int, Int, Int, Int)]): Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach {
      case (currElem, curr, maxElem, max) =>
        //fields must update in ordered pairs: max, curr
        sendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    }
  }

  /**
   * Animate(?) a player using a fully-linked Router teleportation system.
   * In reality, this seems to do nothing visually?
   * @param playerGUID the player being teleported
   * @param srcGUID the origin of the teleportation
   * @param destGUID the destination of the teleportation
   */
  def useRouterTelepadEffect(playerGUID: PlanetSideGUID, srcGUID: PlanetSideGUID, destGUID: PlanetSideGUID): Unit = {
    sendResponse(PlanetsideAttributeMessage(playerGUID, 64, 1)) //what does this do?
    sendResponse(GenericObjectActionMessage(srcGUID, 31))
    sendResponse(GenericObjectActionMessage(destGUID, 32))
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
  def toggleTeleportSystem(router: Vehicle, systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]): Unit = {
    systemPlan match {
      case Some((internalTelepad, remoteTelepad)) =>
        internalTelepad.Telepad = remoteTelepad.GUID //necessary; backwards link to the (new) telepad
        TelepadLike.StartRouterInternalTelepad(continent, router.GUID, internalTelepad)
        TelepadLike.LinkTelepad(continent, remoteTelepad.GUID)
      case _ =>
        router.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) =>
            sendResponse(ObjectDeleteMessage(util.GUID, 0))
          case _ => ()
        }
    }
  }

  def toggleMaxSpecialState(enable: Boolean): Unit = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      if (enable && player.UsingSpecial == SpecialExoSuitDefinition.Mode.Normal) {
        player.Faction match {
          case PlanetSideEmpire.TR if player.Capacitor == player.ExoSuitDef.MaxCapacitor =>
            player.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
            activateMaxSpecialStateMessage()
          case PlanetSideEmpire.NC if player.Capacitor > 0 =>
            player.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
            activateMaxSpecialStateMessage()
          case PlanetSideEmpire.VS =>
            log.warn(s"${player.Name} tried to use a MAX special ability but their faction doesn't have one")
          case _ => ()
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

  private def activateMaxSpecialStateMessage(): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlanetsideAttributeToAll(player.GUID, 8, 1)
    )
  }

  def administrativeKick(tplayer: Player): Unit = {
    log.warn(s"${tplayer.Name} has been kicked by ${player.Name}")
    tplayer.death_by = -1
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(tplayer.Name)
    //get out of that vehicle
    sessionLogic.vehicles.GetMountableAndSeat(None, tplayer, continent) match {
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

  def fallHeightTracker(zHeight: Float): Unit = {
    if ((heightTrend && heightLast - zHeight >= 0.5f) ||
      (!heightTrend && zHeight - heightLast >= 0.5f)) {
      heightTrend = !heightTrend
      heightHistory = zHeight
    }
    heightLast = zHeight
  }

  def canSeeReallyFar: Boolean = {
    sessionLogic.shooting.FindContainedWeapon match {
      case (Some(_: Vehicle), weapons) if weapons.nonEmpty =>
        player.avatar
          .implants
          .exists { p =>
            p.collect { implant => implant.definition.implantType == ImplantType.RangeMagnifier && implant.active }.nonEmpty
          }
      case (Some(_: Player), weapons) if weapons.nonEmpty =>
        val wep = weapons.head
        wep.Definition == GlobalDefinitions.bolt_driver ||
          wep.Definition == GlobalDefinitions.heavy_sniper ||
          (
            (wep.Projectile ne GlobalDefinitions.no_projectile) &&
              player.Crouching &&
              player.avatar
                .implants
                .exists { p =>
                  p.collect { implant => implant.definition.implantType == ImplantType.RangeMagnifier && implant.active }.nonEmpty
                }
            )
      case _ =>
        false
    }
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

  def noVoicedChat(pkt: PlanetSideGamePacket): Unit = {
    log.debug(s"$pkt")
    sendResponse(VoiceHostKill())
    sendResponse(
      ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, "", "Try our Discord at https://discord.gg/0nRe5TNbTYoUruA4", None)
    )
  }

  override protected[session] def actionsToCancel(): Unit = {
    progressBarValue = None
    kitToBeUsed = None
    collisionHistory.clear()
    accessedContainer match {
      case Some(v: Vehicle) =>
        val vguid = v.GUID
        sessionLogic.vehicles.ConditionalDriverVehicleControl(v)
        if (v.AccessingTrunk.contains(player.GUID)) {
          if (player.VehicleSeated.contains(vguid)) {
            v.AccessingTrunk = None //player is seated; just stop accessing trunk
            if (player.isAlive) {
              sendResponse(UnuseItemMessage(player.GUID, vguid))
            }
          } else {
            unaccessContainer(v)
          }
        }

      case Some(o) =>
        unaccessContainer(o)
        if (player.isAlive) {
          sendResponse(UnuseItemMessage(player.GUID, o.GUID))
        }

      case None => ()
    }
  }

  override protected[session] def stop(): Unit = {
    progressBarUpdate.cancel()
    charSavedTimer.cancel()
  }
}
