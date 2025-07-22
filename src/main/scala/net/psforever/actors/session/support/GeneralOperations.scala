// Copyright (c) 2024 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, Cancellable, typed}
import net.psforever.objects.serverobject.containable.Containable
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.serverobject.terminals.{MatrixTerminalDefinition, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.sourcing.{DeployableSource, PlayerSource, VehicleSource}
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.objects.zones.exp.ToDatabase
import net.psforever.services.RemoverActor
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.Success
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

  def handleDropItem(pkt: DropItemMessage): GeneralOperations.ItemDropState.Behavior = {
    val DropItemMessage(itemGuid) = pkt
    (sessionLogic.validObject(itemGuid, decorator = "DropItem"), player.FreeHand.Equipment) match {
      case (Some(anItem: Equipment), Some(heldItem))
        if (anItem eq heldItem) && continent.GUID(player.VehicleSeated).nonEmpty =>
        RemoveOldEquipmentFromInventory(player)(heldItem)
        GeneralOperations.ItemDropState.Dropped
      case (Some(anItem: Equipment), Some(heldItem))
        if anItem eq heldItem =>
        DropEquipmentFromInventory(player)(heldItem)
        GeneralOperations.ItemDropState.Dropped
      case (Some(_), _) =>
        GeneralOperations.ItemDropState.NotDropped
      case _ =>
        GeneralOperations.ItemDropState.NotFound
    }
  }

  def handlePickupItem(pkt: PickupItemMessage): GeneralOperations.ItemPickupState.Behavior = {
    val PickupItemMessage(itemGuid, _, _, _) = pkt
    sessionLogic.validObject(itemGuid, decorator = "PickupItem") match {
      case Some(item: Equipment)
        if player.Fit(item).nonEmpty =>
        PickUpEquipmentFromGround(player)(item)
        GeneralOperations.ItemPickupState.PickedUp
      case Some(_: Equipment) =>
        GeneralOperations.ItemPickupState.Dropped
      case _ =>
        GeneralOperations.ItemPickupState.NotFound
    }
  }

  def handleZipLine(pkt: ZipLineMessage): GeneralOperations.ZiplineBehavior.Behavior = {
    val ZipLineMessage(playerGuid, forwards, action, pathId, pos) = pkt
    continent.zipLinePaths.find(x => x.PathId == pathId) match {
      case Some(path) if path.IsTeleporter =>
        val endPoint = path.ZipLinePoints.last
        sendResponse(ZipLineMessage(PlanetSideGUID(0), forwards, 0, pathId, pos))
        //todo: send to zone to show teleport animation to all clients
        sendResponse(PlayerStateShiftMessage(ShiftState(0, endPoint, (player.Orientation.z + player.FacingYawUpper) % 360f, None)))
        GeneralOperations.ZiplineBehavior.Teleporter
      case Some(_) =>
        //todo: send to zone to show zipline animation to all clients
        action match {
          case 0 =>
            //travel along the zipline in the direction specified
            sendResponse(ZipLineMessage(playerGuid, forwards, action, pathId, pos))
            GeneralOperations.ZiplineBehavior.Zipline
          case 1 =>
            //disembark from zipline at destination
            sendResponse(ZipLineMessage(playerGuid, forwards, action, 0, pos))
            GeneralOperations.ZiplineBehavior.Zipline
          case 2 =>
            //get off by force
            sendResponse(ZipLineMessage(playerGuid, forwards, action, 0, pos))
            GeneralOperations.ZiplineBehavior.Zipline
          case _ =>
            GeneralOperations.ZiplineBehavior.Unsupported
        }
      case _ =>
        GeneralOperations.ZiplineBehavior.NotFound
    }
  }

  def handleMoveItem(pkt: MoveItemMessage): Unit = {
    val MoveItemMessage(itemGuid, sourceGuid, destinationGuid, dest, _) = pkt
    (
      continent.GUID(sourceGuid),
      continent.GUID(destinationGuid),
      sessionLogic.validObject(itemGuid, decorator = "MoveItem")
    ) match {
      case (
        Some(source: PlanetSideServerObject with Container),
        Some(destination: PlanetSideServerObject with Container),
        Some(item: Equipment)
        ) =>
        ContainableMoveItem(player.Name, source, destination, item, destination.SlotMapResolution(dest))
      case (None, _, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid from $sourceGuid, but could not find source object"
        )
      case (_, None, _) =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid to $destinationGuid, but could not find destination object"
        )
      case (_, _, None) => ()
      case _ =>
        log.error(
          s"MoveItem: ${player.Name} wanted to move $itemGuid from $sourceGuid to $destinationGuid, but multiple problems were encountered"
        )
    }
  }

  def handleLootItem(pkt: LootItemMessage): Unit = {
    val LootItemMessage(itemGuid, targetGuid) = pkt
    (sessionLogic.validObject(itemGuid, decorator = "LootItem"), continent.GUID(targetGuid)) match {
      case (Some(item: Equipment), Some(destination: PlanetSideServerObject with Container)) =>
        //figure out the source
        (
          {
            val findFunc: PlanetSideServerObject with Container => Option[
              (PlanetSideServerObject with Container, Option[Int])
            ] = findInLocalContainer(itemGuid)
            findFunc(player.avatar.locker)
              .orElse(findFunc(player))
              .orElse(accessedContainer match {
                case Some(parent: PlanetSideServerObject) =>
                  findFunc(parent)
                case _ =>
                  None
              })
          },
          destination.Fit(item)
        ) match {
          case (Some((source, Some(_))), Some(dest)) =>
            ContainableMoveItem(player.Name, source, destination, item, dest)
          case (None, _) =>
            log.error(s"LootItem: ${player.Name} can not find where $item is put currently")
          case (_, None) =>
            log.error(s"LootItem: ${player.Name} can not find anywhere to put $item in $destination")
          case _ =>
            log.error(
              s"LootItem: ${player.Name}wanted to move $itemGuid to $targetGuid, but multiple problems were encountered"
            )
        }
      case (Some(obj), _) =>
        log.error(s"LootItem: item $obj is (probably) not lootable to ${player.Name}")
      case (None, _) => ()
      case (_, None) =>
        log.error(s"LootItem: ${player.Name} can not find where to put $itemGuid")
    }
  }

  def handleAvatarImplant(pkt: AvatarImplantMessage): GeneralOperations.ImplantActivationBehavior.Behavior = {
    val AvatarImplantMessage(_, action, slot, status) = pkt
    if (action == ImplantAction.Activation) {
      if (sessionLogic.zoning.zoningStatus == Zoning.Status.Deconstructing) {
        //do not activate; play deactivation sound instead
        sessionLogic.zoning.spawn.stopDeconstructing()
        avatar.implants(slot).collect {
          case implant if implant.active =>
            avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
          case implant =>
            sendResponse(PlanetsideAttributeMessage(player.GUID, 28, implant.definition.implantType.value * 2))
        }
        GeneralOperations.ImplantActivationBehavior.Failed
      } else {
        avatar.implants(slot) match {
          case Some(implant) =>
            if (status == 1) {
              avatarActor ! AvatarActor.ActivateImplant(implant.definition.implantType)
              GeneralOperations.ImplantActivationBehavior.Activate
            } else {
              avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
              GeneralOperations.ImplantActivationBehavior.Deactivate
            }
          case _ =>
            GeneralOperations.ImplantActivationBehavior.NotFound
        }
      }
    } else {
      GeneralOperations.ImplantActivationBehavior.Failed
    }
  }

  def handleCreateShortcut(pkt: CreateShortcutMessage): Unit = {
    val CreateShortcutMessage(_, slot, shortcutOpt) = pkt
    shortcutOpt match {
      case Some(shortcut) =>
        avatarActor ! AvatarActor.AddShortcut(slot - 1, shortcut)
      case None =>
        avatarActor ! AvatarActor.RemoveShortcut(slot - 1)
    }
  }

  def handleObjectDetected(pkt: ObjectDetectedMessage): Unit = {
    val ObjectDetectedMessage(_, _, _, targets) = pkt
    sessionLogic.shooting.FindWeapon.foreach {
      case weapon if weapon.Projectile.AutoLock =>
        //projectile with auto-lock instigates a warning on the target
        val detectedTargets = sessionLogic.shooting.FindDetectedProjectileTargets(targets)
        val mode = 7 + (if (weapon.Projectile == GlobalDefinitions.wasp_rocket_projectile) 1 else 0)
        detectedTargets.foreach { target =>
          continent.AvatarEvents ! AvatarServiceMessage(target, AvatarAction.ProjectileAutoLockAwareness(mode))
        }
      case _ => ()
    }
  }

  def handleTargetingImplantRequest(pkt: TargetingImplantRequest): Unit = {
    val TargetingImplantRequest(list) = pkt
    val targetInfo: List[TargetInfo] = list.flatMap { x =>
      continent.GUID(x.target_guid) match {
        case Some(player: Player) =>
          val health = player.Health.toFloat / player.MaxHealth
          val armor = if (player.MaxArmor > 0) {
            player.Armor.toFloat / player.MaxArmor
          } else {
            0
          }
          Some(TargetInfo(player.GUID, health, armor))
        case _ =>
          log.warn(
            s"TargetingImplantRequest: the info that ${player.Name} requested for target ${x.target_guid} is not for a player"
          )
          None
      }
    }
    sendResponse(TargetingInfoMessage(targetInfo))
  }

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
                  ToDatabase.reportFacilityCapture(
                    attacker.CharId,
                    continent.Number,
                    llu.Owner.GUID.guid,
                    Config.app.game.experience.cep.lluSlayerCredit,
                    expType = "llu-slayer"
                  )
              }
          }
          if (!CaptureFlagManager.ReasonToLoseFlagViolently(continent, Some(guid), player)) {
            continent.LocalEvents ! CaptureFlagManager.DropFlag(llu)
          }
        case Some((llu, Some(carrier: Player)))
          if carrier.GUID == player.GUID &&
            !CaptureFlagManager.ReasonToLoseFlagViolently(continent, Some(guid), player) =>
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
   * A simple object searching algorithm that is limited to containers currently known and accessible by the player.
   * If all relatively local containers are checked and the object is not found,
   * the player's locker inventory will be checked, and then
   * the game environment (items on the ground) will be checked too.
   * If the target object is discovered, it is removed from its current location and is completely destroyed.
   * @see `RequestDestroyMessage`
   * @see `Zone.ItemIs.Where`
   * @param objectGuid the target object's globally unique identifier;
   *                    it is not expected that the object will be unregistered, but it is also not gauranteed
   * @param obj the target object
   * @return `true`, if the target object was discovered and removed;
   *        `false`, otherwise
   */
  def findEquipmentToDelete(objectGuid: PlanetSideGUID, obj: Equipment): Boolean = {
    val findFunc
    : PlanetSideServerObject with Container => Option[(PlanetSideServerObject with Container, Option[Int])] =
      findInLocalContainer(objectGuid)

    findFunc(player)
      .orElse(accessedContainer match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      })
      .orElse(sessionLogic.vehicles.findLocalVehicle match {
        case Some(parent: PlanetSideServerObject) =>
          findFunc(parent)
        case _ =>
          None
      }) match {
      case Some((parent, Some(_))) =>
        obj.Position = Vector3.Zero
        RemoveOldEquipmentFromInventory(parent)(obj)
        true
      case _ if player.avatar.locker.Inventory.Remove(objectGuid) =>
        sendResponse(ObjectDeleteMessage(objectGuid, 0))
        true
      case _ if continent.EquipmentOnGround.contains(obj) =>
        obj.Position = Vector3.Zero
        continent.Ground ! Zone.Ground.RemoveItem(objectGuid)
        continent.AvatarEvents ! AvatarServiceMessage.Ground(RemoverActor.ClearSpecific(List(obj), continent))
        true
      case _ =>
        Zone.EquipmentIs.Where(obj, objectGuid, continent) match {
          case None =>
            true
          case Some(Zone.EquipmentIs.Orphaned()) if obj.HasGUID =>
            TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
            true
          case Some(Zone.EquipmentIs.Orphaned()) =>
            true
          case _ =>
            log.warn(s"RequestDestroy: equipment $obj exists, but ${player.Name} can not reach it to dispose of it")
            false
        }
    }
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
        if (player.Capacitor < 1f && player.UsingSpecial == SpecialExoSuitDefinition.Mode.Shielded) {
          sendResponse(ChatMsg(ChatMessageType.UNK_227, "@ArmorShieldOff"))
        }
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

  def administrativeKick(tplayer: Player, time: Option[Long]): Unit = {
    log.warn(s"${tplayer.Name} has been kicked by ${player.Name}")
    tplayer.death_by = -1
    sessionLogic.accountPersistence ! AccountPersistenceService.Kick(tplayer.Name, time)
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

  def handleDeployObject(
                          zone: Zone,
                          deployableType: DeployedItem.Value,
                          position: Vector3,
                          orientation: Vector3,
                          side: Sidedness,
                          faction: PlanetSideEmpire.Value,
                          owner: Player,
                          builtWith: ConstructionItem
                        ): Future[Deployable] = {
    val (deployableEntity, tasking) = commonHandleDeployObjectSetup(zone, deployableType, position, orientation, side, faction)
    deployableEntity.AssignOwnership(owner)
    val promisedDeployable: Promise[Deployable] = Promise()
    //execute
    val result = TaskWorkflow.execute(CallBackForTask(
      tasking,
      zone.Deployables,
      Zone.Deployable.BuildByOwner(deployableEntity, owner, builtWith),
      context.self
    ))
    result.onComplete {
      case Success(_) => promisedDeployable.success(deployableEntity)
      case _ => ()
    }
    promisedDeployable.future
  }

  def handleDeployObject(
                          zone: Zone,
                          deployableType: DeployedItem.Value,
                          position: Vector3,
                          orientation: Vector3,
                          side: Sidedness,
                          faction: PlanetSideEmpire.Value
                        ): Future[Deployable] = {
    val (deployableEntity, tasking) = commonHandleDeployObjectSetup(zone, deployableType, position, orientation, side, faction)
    val promisedDeployable: Promise[Deployable] = Promise()
    //execute
    val result = TaskWorkflow.execute(CallBackForTask(
      tasking,
      zone.Deployables,
      Zone.Deployable.Build(deployableEntity),
      context.self
    ))
    result.onComplete {
      case Success(_) =>
        Players.buildCooldownReset(zone, player.Name, deployableEntity.GUID)
        deployableEntity.Actor ! Deployable.Deconstruct(Some(20.minutes))
        if (deployableType == DeployedItem.boomer) {
          val trigger = new BoomerTrigger
          trigger.Companion = deployableEntity.GUID
          deployableEntity.asInstanceOf[BoomerDeployable].Trigger = trigger
          TaskWorkflow.execute(CallBackForTask(
            GUIDTask.registerEquipment(zone.GUID, trigger),
            zone.Ground,
            Zone.Ground.DropItem(trigger, position + Vector3.z(value = 0.5f), Vector3.z(orientation.z))
          ))
        }
        promisedDeployable.success(deployableEntity)
      case _ => ()
    }
    promisedDeployable.future
  }

  def handleUseDoor(door: Door, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
        val distance: Float = math.max(
          Config.app.game.doorsCanBeOpenedByMedAppFromThisDistance,
          door.Definition.initialOpeningDistance
        )
        door.Actor ! CommonMessages.Use(player, Some(distance))
      case _ =>
        door.Actor ! CommonMessages.Use(player)
    }
  }

  def handleUseResourceSilo(resourceSilo: ResourceSilo, equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    val vehicleOpt = continent.GUID(player.avatar.vehicle)
    (vehicleOpt, equipment) match {
      case (Some(vehicle: Vehicle), Some(item))
        if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
        resourceSilo.Actor ! CommonMessages.Use(player, Some(vehicle))
      case (Some(vehicle: Vehicle), _)
        if vehicle.Definition == GlobalDefinitions.ant &&
          vehicle.DeploymentState == DriveState.Deployed &&
          Vector3.DistanceSquared(resourceSilo.Position.xy, vehicle.Position.xy) < math.pow(resourceSilo.Definition.UseRadius, 2) =>
        resourceSilo.Actor ! CommonMessages.Use(player, Some(vehicle))
      case _ => ()
    }
  }

  def handleUsePlayer(obj: Player, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    if (obj.isBackpack) {
      if (equipment.isEmpty) {
        log.info(s"${player.Name} is looting the corpse of ${obj.Name}")
        sendResponse(msg)
        accessContainer(obj)
      }
    } else if (!msg.unk3 && player.isAlive) { //potential kit use
      (continent.GUID(msg.item_used_guid), kitToBeUsed) match {
        case (Some(kit: Kit), None) =>
          kitToBeUsed = Some(msg.item_used_guid)
          player.Actor ! CommonMessages.Use(player, Some(kit))
        case (Some(_: Kit), Some(_)) | (None, Some(_)) =>
          //a kit is already queued to be used; ignore this request
          sendResponse(ChatMsg(ChatMessageType.UNK_225, wideContents=false, "", "Please wait ...", None))
        case (Some(item), _) =>
          log.error(s"UseItem: ${player.Name} looking for Kit to use, but found $item instead")
        case (None, None) =>
          log.warn(s"UseItem: anticipated a Kit ${msg.item_used_guid} for ${player.Name}, but can't find it")              }
    } else if (msg.object_id == ObjectClass.avatar && msg.unk3) {
      equipment match {
        case Some(tool: Tool) if tool.Definition == GlobalDefinitions.bank =>
          obj.Actor ! CommonMessages.Use(player, equipment)

        case Some(tool: Tool) if tool.Definition == GlobalDefinitions.medicalapplicator =>
          obj.Actor ! CommonMessages.Use(player, equipment)
        case _ => ()
      }
    }
  }

  def handleUseLocker(locker: Locker, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(locker, item)
      case None if locker.Faction == player.Faction || locker.HackedBy.nonEmpty =>
        log.info(s"${player.Name} is accessing a locker")
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        val playerLocker = player.avatar.locker
        sendResponse(msg.copy(object_guid = playerLocker.GUID, object_id = 456))
        accessContainer(playerLocker)
      case _ => ()
    }
  }

  def handleUseCaptureTerminal(captureTerminal: CaptureTerminal, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(captureTerminal, item)
      case _ if specialItemSlotGuid.nonEmpty =>
        continent.GUID(specialItemSlotGuid) match {
          case Some(llu: CaptureFlag) =>
            if (llu.Target.GUID == captureTerminal.Owner.GUID) {
              continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.LluCaptured(llu))
            } else {
              log.info(
                s"LLU target is not this base. Target GUID: ${llu.Target.GUID} This base: ${captureTerminal.Owner.GUID}"
              )
            }
          case _ => log.warn("Item in specialItemSlotGuid is not registered with continent or is not a LLU")
        }
      case _ => ()
    }
  }

  def handleUseFacilityTurret(obj: FacilityTurret, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment.foreach { item =>
      sendUseGeneralEntityMessage(obj, item)
      obj.Actor ! CommonMessages.Use(player, Some((item, msg.unk2.toInt))) //try upgrade path
    }
  }

  def handleUseVehicle(obj: Vehicle, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(obj, item)
      case None if player.Faction == obj.Faction =>
        //access to trunk
        if (
          obj.AccessingTrunk.isEmpty &&
            (!obj.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked) || obj.OwnerGuid
              .contains(player.GUID))
        ) {
          log.info(s"${player.Name} is looking in the ${obj.Definition.Name}'s trunk")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          obj.AccessingTrunk = player.GUID
          accessContainer(obj)
          sendResponse(msg)
        }
      case _ => ()
    }
  }

  def handleUseTerminal(terminal: Terminal, equipment: Option[Equipment], msg: UseItemMessage): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(terminal, item)
      case None
        if terminal.Owner == Building.NoBuilding || terminal.Faction == player.Faction ||
          terminal.HackedBy.nonEmpty || terminal.Faction == PlanetSideEmpire.NEUTRAL =>
        val tdef = terminal.Definition
        if (tdef.isInstanceOf[MatrixTerminalDefinition]) {
          //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          sendResponse(
            BindPlayerMessage(BindStatus.Bind, "", display_icon=true, logging=true, SpawnGroup.Sanctuary, 0, 0, terminal.Position)
          )
        } else if (
          tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
            tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal
        ) {
          sessionLogic.vehicles.findLocalVehicle match {
            case Some(vehicle) =>
              log.info(
                s"${player.Name} is accessing a ${terminal.Definition.Name} for ${player.Sex.possessive} ${vehicle.Definition.Name}"
              )
              sendResponse(msg)
              sendResponse(msg.copy(object_guid = vehicle.GUID, object_id = vehicle.Definition.ObjectId))
            case None =>
              log.error(s"UseItem: Expecting a seated vehicle, ${player.Name} found none")
          }
        } else if (tdef == GlobalDefinitions.teleportpad_terminal) {
          //explicit request
          log.info(s"${player.Name} is purchasing a router telepad")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          terminal.Actor ! Terminal.Request(
            player,
            ItemTransactionMessage(msg.object_guid, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
          )
        } else if (tdef == GlobalDefinitions.targeting_laser_dispenser) {
          //explicit request
          log.info(s"${player.Name} is purchasing a targeting laser")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          terminal.Actor ! Terminal.Request(
            player,
            ItemTransactionMessage(msg.object_guid, TransactionType.Buy, 0, "flail_targeting_laser", 0, PlanetSideGUID(0))
          )
        } else {
          log.info(s"${player.Name} is accessing a ${terminal.Definition.Name}")
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
          sendResponse(msg)
        }
      case _ => ()
    }
  }

  def handleUseSpawnTube(obj: SpawnTube, equipment: Option[Equipment]): Unit = {
    equipment match {
      case Some(item) =>
        sendUseGeneralEntityMessage(obj, item)
      case None if player.Faction == obj.Faction =>
        //deconstruction
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        sessionLogic.actionsToCancel()
        sessionLogic.terminals.CancelAllProximityUnits()
        sessionLogic.zoning.spawn.startDeconstructing(obj)
      case _ => ()
    }
  }

  def handleUseTelepadDeployable(
                                  obj: TelepadDeployable,
                                  equipment: Option[Equipment],
                                  msg: UseItemMessage,
                                  useTelepadFunc: (Vehicle, InternalTelepad, TelepadDeployable, PlanetSideGameObject with TelepadLike, PlanetSideGameObject with TelepadLike) => Unit
                                ): Unit = {
    if (equipment.isEmpty) {
      (continent.GUID(obj.Router) match {
        case Some(vehicle: Vehicle) => Some((vehicle, vehicle.Utility(UtilityType.internal_router_telepad_deployable)))
        case Some(vehicle) => Some(vehicle, None)
        case None => None
      }) match {
        case Some((vehicle: Vehicle, Some(util: Utility.InternalTelepad))) =>
          sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel")
          player.WhichSide = vehicle.WhichSide
          useTelepadFunc(vehicle, util, obj, obj, util)
        case Some((vehicle: Vehicle, None)) =>
          log.error(
            s"telepad@${msg.object_guid.guid} is not linked to a router - ${vehicle.Definition.Name}"
          )
        case Some((o, _)) =>
          log.error(
            s"telepad@${msg.object_guid.guid} is linked to wrong kind of object - ${o.Definition.Name}, ${obj.Router}"
          )
          obj.Actor ! Deployable.Deconstruct()
        case _ => ()
      }
    }
  }

  def handleUseInternalTelepad(
                                obj: InternalTelepad,
                                msg: UseItemMessage,
                                useTelepadFunc: (Vehicle, InternalTelepad, TelepadDeployable, PlanetSideGameObject with TelepadLike, PlanetSideGameObject with TelepadLike) => Unit
                              ): Unit = {
    continent.GUID(obj.Telepad) match {
      case Some(pad: TelepadDeployable) =>
        player.WhichSide = pad.WhichSide
        useTelepadFunc(obj.Owner.asInstanceOf[Vehicle], obj, pad, obj, pad)
      case Some(o) =>
        log.error(
          s"internal telepad@${msg.object_guid.guid} is not linked to a remote telepad - ${o.Definition.Name}@${o.GUID.guid}"
        )
      case None => ()
    }
  }

  /**
   * A player uses a fully-linked Router teleportation system.
   * @param router the Router vehicle
   * @param internalTelepad the internal telepad within the Router vehicle
   * @param remoteTelepad the remote telepad that is currently associated with this Router
   * @param src the origin of the teleportation (where the player starts)
   * @param dest the destination of the teleportation (where the player is going)
   */
  def useRouterTelepadSystem(
                              router: Vehicle,
                              internalTelepad: InternalTelepad,
                              remoteTelepad: TelepadDeployable,
                              src: PlanetSideGameObject with TelepadLike,
                              dest: PlanetSideGameObject with TelepadLike
                            ): Unit = {
    val time = System.currentTimeMillis()
    if (
      time - recentTeleportAttempt > 2000L && router.DeploymentState == DriveState.Deployed &&
        internalTelepad.Active &&
        remoteTelepad.Active
    ) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      useRouterTelepadEffect(pguid, sguid, dguid)
      continent.LocalEvents ! LocalServiceMessage(
        continent.id,
        LocalAction.RouterTelepadTransport(pguid, pguid, sguid, dguid)
      )
      player.Position = dest.Position
      player.LogActivity(TelepadUseActivity(VehicleSource(router), DeployableSource(remoteTelepad), PlayerSource(player)))
    } else {
      log.warn(s"UseRouterTelepadSystem: ${player.Name} can not teleport")
    }
    recentTeleportAttempt = time
  }

  /**
   * A player uses a fully-linked Router teleportation system.
   * @param router the Router vehicle
   * @param internalTelepad the internal telepad within the Router vehicle
   * @param remoteTelepad the remote telepad that is currently associated with this Router
   * @param src the origin of the teleportation (where the player starts)
   * @param dest the destination of the teleportation (where the player is going)
   */
  def useRouterTelepadSystemSecretly(
                                      router: Vehicle,
                                      internalTelepad: InternalTelepad,
                                      remoteTelepad: TelepadDeployable,
                                      src: PlanetSideGameObject with TelepadLike,
                                      dest: PlanetSideGameObject with TelepadLike
                                    ): Unit = {
    val time = System.currentTimeMillis()
    if (
      time - recentTeleportAttempt > 2000L && router.DeploymentState == DriveState.Deployed &&
        internalTelepad.Active &&
        remoteTelepad.Active
    ) {
      val pguid = player.GUID
      val sguid = src.GUID
      val dguid = dest.GUID
      sendResponse(PlayerStateShiftMessage(ShiftState(0, dest.Position, player.Orientation.z)))
      useRouterTelepadEffect(pguid, sguid, dguid)
      player.Position = dest.Position
    } else {
      log.warn(s"UseRouterTelepadSystem: ${player.Name} can not teleport")
    }
    recentTeleportAttempt = time
  }

  def handleUseCaptureFlag(obj: CaptureFlag): Unit = {
    // LLU can normally only be picked up the faction that owns it
    specialItemSlotGuid match {
      case None if obj.Faction == player.Faction =>
        specialItemSlotGuid = Some(obj.GUID)
        player.Carrying = SpecialCarry.CaptureFlag
        continent.LocalEvents ! CaptureFlagManager.PickupFlag(obj, player)
      case None =>
        log.warn(s"${player.Faction} player ${player.toString} tried to pick up a ${obj.Faction} LLU -  ${obj.GUID}")
      case Some(guid) if guid != obj.GUID =>
        // Ignore duplicate pickup requests
        log.warn(
          s"${player.Faction} player ${player.toString} tried to pick up a ${obj.Faction} LLU, but their special slot already contains $guid"
        )
      case _ => ()
    }
  }

  def handleUseWarpGate(equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    (continent.GUID(player.VehicleSeated), equipment) match {
      case (Some(vehicle: Vehicle), Some(item))
        if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) &&
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) =>
        vehicle.Actor ! CommonMessages.Use(player, equipment)
      case _ => ()
    }
  }

  def handleUseGeneralEntity(obj: PlanetSideServerObject, equipment: Option[Equipment]): Unit = {
    equipment.foreach { item =>
      sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
      obj.Actor ! CommonMessages.Use(player, Some(item))
    }
  }

  def sendUseGeneralEntityMessage(obj: PlanetSideServerObject, equipment: Equipment): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    obj.Actor ! CommonMessages.Use(player, Some(equipment))
  }

  def handleUseDefaultEntity(obj: PlanetSideGameObject, equipment: Option[Equipment]): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    equipment match {
      case Some(item)
        if GlobalDefinitions.isBattleFrameArmorSiphon(item.Definition) ||
          GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition) => ()
      case _ =>
        log.warn(s"UseItem: ${player.Name} does not know how to handle $obj")
    }
  }

  def commonFacilityShieldCharging(obj: PlanetSideServerObject with BlockMapEntity): Unit = {
    obj.Actor ! CommonMessages.ChargeShields(
      15,
      Some(continent.blockMap.sector(obj).buildingList.maxBy(_.Definition.SOIRadius))
    )
  }

  private def commonHandleDeployObjectSetup(
                                             zone: Zone,
                                             deployableType: DeployedItem.Value,
                                             position: Vector3,
                                             orientation: Vector3,
                                             side: Sidedness,
                                             faction: PlanetSideEmpire.Value
                                           ): (Deployable, TaskBundle) = {
    val deployableEntity: Deployable = Deployables.Make(deployableType)()
    deployableEntity.Position = position
    deployableEntity.Orientation = orientation
    deployableEntity.WhichSide = side
    deployableEntity.Faction = faction
    val tasking: TaskBundle = deployableEntity match {
      case turret: TurretDeployable =>
        GUIDTask.registerDeployableTurret(zone.GUID, turret)
      case _ =>
        GUIDTask.registerObject(zone.GUID, deployableEntity)
    }
    (deployableEntity, tasking)
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

      case Some(o) if player.isAlive =>
        unaccessContainer(o)
        sendResponse(UnuseItemMessage(player.GUID, o.GUID))

      case Some(o) =>
        unaccessContainer(o)

      case None => ()
    }
  }

  override protected[session] def stop(): Unit = {
    progressBarUpdate.cancel()
    charSavedTimer.cancel()
  }
}

object GeneralOperations {
  object UseItem {
    sealed trait Behavior
    case object Handled extends Behavior
    case object HandledPassive extends Behavior
    case object Unhandled extends Behavior
  }

  object ItemDropState {
    sealed trait Behavior
    case object Dropped extends Behavior
    case object NotDropped extends Behavior
    case object NotFound extends Behavior
  }

  object ItemPickupState {
    sealed trait Behavior
    case object PickedUp extends Behavior
    case object Dropped extends Behavior
    case object NotFound extends Behavior
  }

  object ZiplineBehavior {
    sealed trait Behavior
    case object Teleporter extends Behavior
    case object Zipline extends Behavior
    case object Unsupported extends Behavior
    case object NotFound extends Behavior
  }

  object ImplantActivationBehavior {
    sealed trait Behavior
    case object Activate extends Behavior
    case object Deactivate extends Behavior
    case object Failed extends Behavior
    case object NotFound extends Behavior
  }
}
