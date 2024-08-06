// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.{Actor, ActorRef, Props, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.zone.ZoneActor
import net.psforever.login.WorldSession.{DropEquipmentFromInventory, HoldNewEquipmentUp, PutNewEquipmentInInventoryOrDrop, RemoveOldEquipmentFromInventory}
import net.psforever.objects._
import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.DeployAnimation
import net.psforever.objects.definition.converter.OCM
import net.psforever.objects.equipment._
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.loadouts.Loadout
import net.psforever.objects.serverobject.aura.{Aura, AuraEffectBehavior}
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, Damageable, DamageableEntity}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vital._
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.zones._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.objects.locker.LockerContainerControl
import net.psforever.objects.serverobject.environment.interaction.RespondsToZoneEnvironment
import net.psforever.objects.serverobject.repair.Repairable
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource}
import net.psforever.objects.vital.collision.CollisionReason
import net.psforever.objects.vital.etc.{PainboxReason, SuicideReason}
import net.psforever.objects.vital.interaction.{Adversarial, DamageInteraction, DamageResult}
import net.psforever.packet.PlanetSideGamePacket

import scala.concurrent.duration._

class PlayerControl(player: Player, avatarActor: typed.ActorRef[AvatarActor.Command])
    extends Actor
    with JammableBehavior
    with Damageable
    with ContainableBehavior
    with AggravatedBehavior
    with AuraEffectBehavior
    with RespondsToZoneEnvironment {
  def JammableObject: Player = player

  def DamageableObject: Player = player

  def ContainerObject: Player = player

  def AggravatedObject: Player = player

  def AuraTargetObject: Player = player
  ApplicableEffect(Aura.Plasma)
  ApplicableEffect(Aura.Napalm)
  ApplicableEffect(Aura.Comet)
  ApplicableEffect(Aura.Fire)

  def InteractiveObject: Player = player

  private[this] val log = org.log4s.getLogger(player.Name)
  private[this] val damageLog = org.log4s.getLogger(Damageable.LogChannel)
  /** suffocating, or regaining breath? */
  var submergedCondition: Option[OxygenState] = None
  /** assistance for deployable construction, retention of the construction item */
  var deployablePair: Option[(Deployable, ConstructionItem)] = None
  /** control agency for the player's locker container (dedicated inventory slot #5) */
  val lockerControlAgent: ActorRef = {
    val locker = player.avatar.locker
    locker.Zone = player.Zone
    locker.Actor = context.actorOf(
      Props(classOf[LockerContainerControl], locker, player.Name),
      PlanetSideServerObject.UniqueActorName(locker)
    )
  }

  override def postStop(): Unit = {
    lockerControlAgent ! akka.actor.PoisonPill
    player.avatar.locker.Actor = Default.Actor
    EndAllEffects()
    EndAllAggravation()
    respondToEnvironmentPostStop()
  }

  def receive: Receive =
    jammableBehavior
      .orElse(takesDamage)
      .orElse(aggravatedBehavior)
      .orElse(auraBehavior)
      .orElse(containerBehavior)
      .orElse(environmentBehavior)
      .orElse {
        case Player.Die(Some(reason)) =>
          if (player.isAlive) {
            //primary death
            val health = player.Health
            val psource = PlayerSource(player)
            player.Health = 0
            HandleDamage(
              player,
              DamageResult(psource, psource.copy(health = 0), reason),
              health,
              damageToArmor = 0,
              damageToStamina = 0,
              damageToCapacitor = 0
            )
            damageLog.info(s"${player.Name}-infantry: dead by explicit reason - ${reason.cause.resolution}")
          }

        case Player.Die(None) =>
          suicide()

        case CommonMessages.Use(user, Some(item: Tool))
          if item.Definition == GlobalDefinitions.medicalapplicator && player.isAlive =>
          //heal
          val originalHealth = player.Health
          val definition = player.Definition
          if (
            player.MaxHealth > 0 && originalHealth < player.MaxHealth &&
            user.Faction == player.Faction &&
            item.Magazine > 0 &&
            Vector3.Distance(user.Position, player.Position) < definition.RepairDistance
          ) {
            val zone = player.Zone
            val events = zone.AvatarEvents
            val uname = user.Name
            val guid = player.GUID
            if (!(player.isMoving || user.isMoving)) { //only allow stationary heals
              val newHealth = player.Health = originalHealth + 10
              val magazine = item.Discharge()
              events ! AvatarServiceMessage(
                uname,
                AvatarAction.SendResponse(
                  Service.defaultPlayerGUID,
                  InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)
                )
              )
              events ! AvatarServiceMessage(zone.id, AvatarAction.PlanetsideAttributeToAll(guid, 0, newHealth))
              player.LogActivity(
                HealFromEquipment(
                  PlayerSource(user),
                  GlobalDefinitions.medicalapplicator,
                  newHealth - originalHealth
                )
              )
            }
            if (player != user) {
              //"Someone is trying to heal you"
              events ! AvatarServiceMessage(player.Name, AvatarAction.PlanetsideAttributeToAll(guid, 55, 1))
              //progress bar remains visible for all heal attempts
              events ! AvatarServiceMessage(
                uname,
                AvatarAction.SendResponse(
                  Service.defaultPlayerGUID,
                  RepairMessage(guid, player.Health * 100 / definition.MaxHealth)
                )
              )
            }
          }

        case CommonMessages.Use(user, Some(item: Tool)) if item.Definition == GlobalDefinitions.medicalapplicator =>
          //revive
          if (
            user != player &&
            user.Faction == player.Faction &&
            user.isAlive && !user.isMoving &&
            !player.isAlive && !player.isBackpack &&
            item.Magazine >= 25
          ) {
            sender() ! CommonMessages.Progress(
              4,
              Players.FinishRevivingPlayer(player, user, item),
              Players.RevivingTickAction(player, user, item)
            )
          }

        case CommonMessages.Use(user, Some(item: Tool)) if item.Definition == GlobalDefinitions.bank =>
          val originalArmor = player.Armor
          val definition = player.Definition
          if (
            player.MaxArmor > 0 && originalArmor < player.MaxArmor &&
            user.Faction == player.Faction &&
            item.AmmoType == Ammo.armor_canister && item.Magazine > 0 &&
            Vector3.Distance(user.Position, player.Position) < definition.RepairDistance
          ) {
            val zone = player.Zone
            val events = zone.AvatarEvents
            val uname = user.Name
            val guid = player.GUID
            if (!(player.isMoving || user.isMoving)) { //only allow stationary repairs
              val newArmor = player.Armor =
                originalArmor + Repairable.applyLevelModifier(user, item, RepairToolValue(item)).toInt + definition.RepairMod
              val magazine = item.Discharge()
              events ! AvatarServiceMessage(
                uname,
                AvatarAction.SendResponse(
                  Service.defaultPlayerGUID,
                  InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)
                )
              )
              events ! AvatarServiceMessage(zone.id, AvatarAction.PlanetsideAttributeToAll(guid, 4, player.Armor))
              player.LogActivity(
                RepairFromEquipment(
                  PlayerSource(user),
                  GlobalDefinitions.bank,
                  newArmor - originalArmor
                )
              )
            }
            if (player != user) {
              if (player.isAlive) {
                //"Someone is trying to repair you" gets strobed twice for visibility
                val msg = AvatarServiceMessage(player.Name, AvatarAction.PlanetsideAttributeToAll(guid, 56, 1))
                events ! msg
                import scala.concurrent.ExecutionContext.Implicits.global
                context.system.scheduler.scheduleOnce(250 milliseconds, events, msg)
              }
              //progress bar remains visible for all repair attempts
              events ! AvatarServiceMessage(
                uname,
                AvatarAction
                  .SendResponse(Service.defaultPlayerGUID, RepairMessage(guid, player.Armor * 100 / player.MaxArmor))
              )
            }
          }

        case CommonMessages.Use(_, Some(kit: Kit)) if player.isAlive =>
          val kdef = kit.Definition
          val (thisKitIsUsed, attribute, value, msg): (Option[Int], Int, Long, String) = player.avatar.useCooldown(kdef) match {
            case Some(cooldown) =>
              (None, 0, 0, s"@TimeUntilNextUse^${cooldown.getStandardSeconds}")

            case None =>
              val indexOpt = player.Find(kit)
              indexOpt match {
                case Some(index) =>
                  if (kdef == GlobalDefinitions.medkit) {
                    if (player.Health == player.MaxHealth) {
                      (None, 0, 0, "@HealComplete")
                    } else {
                      player.LogActivity(HealFromKit(kdef, 25))
                      player.Health = player.Health + 25
                      (Some(index), 0, player.Health, "")
                    }
                  } else if (kdef == GlobalDefinitions.super_medkit) {
                    if (player.Health == player.MaxHealth) {
                      (None, 0, 0, "@HealComplete")
                    } else {
                      player.LogActivity(HealFromKit(kdef, 100))
                      player.Health = player.Health + 100
                      (Some(index), 0, player.Health, "")
                    }
                  } else if (kdef == GlobalDefinitions.super_armorkit) {
                    if (player.Armor == player.MaxArmor) {
                      (None, 0, 0, "Armor at maximum - No repairing required.")
                    } else {
                      player.LogActivity(RepairFromKit(kdef, 200))
                      player.Armor = player.Armor + 200
                      (Some(index), 4, player.Armor, "")
                    }
                  } else if (kdef == GlobalDefinitions.super_staminakit) {
                    if (player.avatar.staminaFull) {
                      (None, 0, 0, "Stamina at maximum - No recharge required.")
                    } else {
                      avatarActor ! AvatarActor.RestoreStamina(100)
                      //proper stamina update will occur due to above message; update something relatively harmless instead
                      (Some(index), 3, player.avatar.maxStamina, "")
                    }
                  } else {
                    log.warn(s"UseItem: Your $kit behavior is not supported, ${player.Name}")
                    (None, 0, 0, "")
                  }

                case None =>
                  log.error(s"UseItem: Anticipated a $kit for ${player.Name}, but can't find it")
                  (None, 0, 0, "")
              }
          }
          thisKitIsUsed match {
            case Some(slot) =>
              //kit was found belonging to player and is to be used
              val kguid = kit.GUID
              val zone = player.Zone
              avatarActor ! AvatarActor.UpdateUseTime(kdef)
              player.Slot(slot).Equipment = None //remove from slot immediately; must exist on client for now
              TaskWorkflow.execute(GUIDTask.unregisterEquipment(zone.GUID, kit))
              zone.AvatarEvents ! AvatarServiceMessage(
                zone.id,
                AvatarAction.PlanetsideAttributeToAll(player.GUID, attribute, value)
              )
              zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.UseKit(kguid, kdef.ObjectId)
              )
            case _ =>
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.KitNotUsed(kit.GUID, msg)
              )
          }

        case PlayerControl.SetExoSuit(exosuit: ExoSuitType.Value, subtype: Int) =>
          setExoSuit(exosuit, subtype)

        case PlayerControl.ObjectHeld(slot, updateMyHolsterArm) =>
          val before = player.DrawnSlot
          val events = player.Zone.AvatarEvents
          val resistance = player.TestArmMotion(slot)
          if (resistance && !updateMyHolsterArm) {
            events ! AvatarServiceMessage(
              player.Name,
              AvatarAction.ObjectHeld(player.GUID, before, -1)
            )
          } else if ((!resistance && before != slot && (player.DrawnSlot = slot) != before) && ItemSwapSlot != before) {
            val mySlot = if (updateMyHolsterArm) slot else -1 //use as a short-circuit
            events ! AvatarServiceMessage(
              player.Continent,
              AvatarAction.ObjectHeld(player.GUID, mySlot, player.LastDrawnSlot)
            )
            val isHolsters = player.VisibleSlots.contains(slot)
            val equipment = player.Slot(slot).Equipment.orElse { player.Slot(before).Equipment }
            if (isHolsters) {
              equipment match {
                case Some(unholsteredItem: Equipment) =>
                  log.info(s"${player.Name} has drawn a ${unholsteredItem.Definition.Name} from its holster")
                  if (unholsteredItem.Definition == GlobalDefinitions.remote_electronics_kit) {
                    //rek beam/icon colour must match the player's correct hack level
                    events ! AvatarServiceMessage(
                      player.Continent,
                      AvatarAction.PlanetsideAttribute(unholsteredItem.GUID, 116, player.avatar.hackingSkillLevel())
                    )
                  }
                case None => ;
              }
            } else {
              equipment match {
                case Some(holsteredEquipment) =>
                  log.info(s"${player.Name} has put ${player.Sex.possessive} ${holsteredEquipment.Definition.Name} down")
                case None =>
                  log.info(s"${player.Name} lowers ${player.Sex.possessive} hand")
              }
            }
            UpdateItemSwapSlot
          } else if (ItemSwapSlot == before) {
            UpdateItemSwapSlot
          }

        case Terminal.TerminalMessage(_, msg, order) =>
          lazy val terminalUsedAction = {
            player.Zone.GUID(msg.terminal_guid).collect {
              case t: Terminal =>
                player.LogActivity(TerminalUsedActivity(AmenitySource(t), msg.transaction_type))
            }
          }
          order match {
            case Terminal.BuyExosuit(exosuit, subtype) =>
              val result = setExoSuit(exosuit, subtype)
              if (exosuit == ExoSuitType.MAX) {
                player.ResistArmMotion(PlayerControl.maxRestriction)
              }
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, result)
              )
              terminalUsedAction

            case Terminal.InfantryLoadout(exosuit, subtype, holsters, inventory) =>
              log.info(s"${player.Name} wants to change equipment loadout to their option #${msg.unk1 + 1}")
              val originalSuit = player.ExoSuit
              val originalSubtype = Loadout.DetermineSubtype(player)
              val dropPred = ContainableBehavior.DropPredicate(player)
              //determine player's next exo-suit
              val (nextSuit, nextSubtype) = {
                lazy val fallbackSuit = if (Players.CertificationToUseExoSuit(player, originalSuit, originalSubtype)) {
                  //TODO will we ever need to check for the cooldown status of an original non-MAX exo-suit?
                  (originalSuit, originalSubtype)
                } else {
                  (ExoSuitType.Standard, 0)
                }
                if (Players.CertificationToUseExoSuit(player, exosuit, subtype)) {
                  if (exosuit == ExoSuitType.MAX) {
                    val weapon = GlobalDefinitions.MAXArms(subtype, player.Faction)
                    val cooldown = player.avatar.purchaseCooldown(weapon)
                    if (originalSubtype == subtype) {
                      (exosuit, subtype) //same MAX subtype is free
                    } else if (cooldown.nonEmpty) {
                      fallbackSuit //different MAX subtype can not have cooldown
                    } else {
                      avatarActor ! AvatarActor.UpdatePurchaseTime(weapon)
                      (exosuit, subtype) //switching for first time causes cooldown
                    }
                  } else {
                    (exosuit, subtype)
                  }
                } else {
                  log.warn(
                    s"${player.Name} no longer has permission to wear the exo-suit type $exosuit; will wear ${fallbackSuit._1} instead"
                  )
                  fallbackSuit
                }
              }
              //sanitize current exo-suit for change
              val (dropHolsters, oldHolsters) = Players.clearHolsters(player.Holsters().iterator).partition(dropPred)
              val (dropInventory, oldInventory) = player.Inventory.Clear().partition(dropPred)
              val (dropHand, deleteHand) = player.FreeHand.Equipment match {
                case Some(obj) =>
                  val out = InventoryItem(obj, -1)
                  player.FreeHand.Equipment = None
                  if (dropPred(out)) {
                    (List(out), Nil)
                  } else {
                    (Nil, List(out))
                  }
                case _ =>
                  (Nil, Nil)
              }
              //these dropped items exist and must be accounted for
              val itemsToDrop = dropHand ++ dropHolsters ++ dropInventory
              val newHolsters = for {
                item <- holsters
                //id = item.obj.Definition.ObjectId
                //lastTime = player.GetLastUsedTime(id)
                if true
              } yield item
              val newInventory = for {
                item <- inventory
                //id = item.obj.Definition.ObjectId
                //lastTime = player.GetLastUsedTime(id)
                if true
              } yield item
              //update suit internally
              val originalArmor = player.Armor
              player.ExoSuit = nextSuit
              val toMaxArmor = player.MaxArmor
              val toArmor = {
                if (originalSuit != nextSuit || originalSubtype != nextSubtype || originalArmor > toMaxArmor) {
                  player.LogActivity(RepairFromExoSuitChange(nextSuit, toMaxArmor - player.Armor))
                  player.Armor = toMaxArmor
                } else {
                  player.Armor = originalArmor
                }
              }
              val (afterHolsters, afterInventory) = if (exosuit == nextSuit) {
                //proposed loadout inventory matched the projected exo-suit selection
                if (nextSuit == ExoSuitType.MAX) {
                  //loadout for a MAX
                  player.ResistArmMotion(PlayerControl.maxRestriction)
                  player.DrawnSlot = Player.HandsDownSlot
                  (newHolsters.filter(_.start == 4), newInventory.filterNot(dropPred))
                } else {
                  //loadout for a vanilla exo-suit
                  player.ResistArmMotion(Player.neverRestrict)
                  (newHolsters.filterNot(dropPred), newInventory.filterNot(dropPred))
                }
              } else {
                //proposed loadout conforms to a different inventory layout than the projected exo-suit
                player.ResistArmMotion(Player.neverRestrict)
                //holsters (matching holsters will be inserted, the rest will deposited into the inventory)
                val (finalHolsters, leftoversForInventory) = Players.fillEmptyHolsters(
                  player.Holsters().iterator,
                  (newHolsters.filterNot(_.obj.Size == EquipmentSize.Max) ++ newInventory).filterNot(dropPred)
                )
                //inventory (items will be placed to accommodate the change, or dropped)
                val (finalInventory, _) = GridInventory.recoverInventory(leftoversForInventory, player.Inventory)
                (finalHolsters, finalInventory)
              }
              (afterHolsters ++ afterInventory).foreach { entry => entry.obj.Faction = player.Faction }
              afterHolsters.collect {
                case InventoryItem(citem: ConstructionItem, _) =>
                  Deployables.initializeConstructionItem(player.avatar.certifications, citem)
              }
              //deactivate non-passive implants
              avatarActor ! AvatarActor.DeactivateActiveImplants
              val zone = player.Zone
              zone.AvatarEvents ! AvatarServiceMessage(
                zone.id,
                AvatarAction.ChangeLoadout(
                  player.GUID,
                  toArmor,
                  nextSuit,
                  nextSubtype,
                  player.LastDrawnSlot,
                  nextSuit == ExoSuitType.MAX,
                  oldHolsters.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
                  afterHolsters,
                  (oldInventory ++ deleteHand).map { case InventoryItem(obj, _) => (obj, obj.GUID) },
                  afterInventory,
                  itemsToDrop
                )
              )
              zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, result=true)
              )
              terminalUsedAction
            case _ =>
              assert(assertion=false, msg.toString)
          }

        case Zone.Ground.ItemOnGround(item, _, _) =>
          item match {
            case trigger: BoomerTrigger =>
              //drop the trigger, lose the boomer; make certain whole faction is aware of that
              player.Zone.GUID(trigger.Companion) match {
                case Some(obj: BoomerDeployable) =>
                  loseDeployableOwnership(obj)
                case _ => ;
              }
            case _ => ;
          }

        case Zone.Ground.CanNotDropItem(_, item, reason) =>
          log.warn(s"${player.Name} tried to drop a ${item.Definition.Name} on the ground, but it $reason")

        case Zone.Ground.ItemInHand(_) => ;

        case Zone.Ground.CanNotPickupItem(_, item_guid, reason) =>
          log.warn(s"${player.Name} failed to pick up an item ($item_guid) from the ground because $reason")

        case Player.BuildDeployable(obj: TelepadDeployable, tool: Telepad) =>
          obj.Router = tool.Router //necessary; forwards link to the router that prodcued the telepad
          setupDeployable(obj, tool)

        case Player.BuildDeployable(obj, tool) =>
          setupDeployable(obj, tool)

        case Zone.Deployable.IsBuilt(obj: BoomerDeployable) =>
          obj.Actor ! Deployable.Ownership(player)
          deployablePair match {
            case Some((deployable, tool)) if deployable eq obj =>
              val zone = player.Zone
              //boomers
              val trigger = new BoomerTrigger
              trigger.Companion = obj.GUID
              obj.Trigger = trigger
              zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, tool.GUID))
              TaskWorkflow.execute(GUIDTask.unregisterEquipment(zone.GUID, tool))
              player.Find(tool) match {
                case Some(index) if player.VisibleSlots.contains(index) =>
                  player.Slot(index).Equipment = None
                  TaskWorkflow.execute(HoldNewEquipmentUp(player)(trigger, index))
                case Some(index) =>
                  player.Slot(index).Equipment = None
                  TaskWorkflow.execute(PutNewEquipmentInInventoryOrDrop(player)(trigger))
                case None =>
                  //don't know where boomer trigger "should" go
                  TaskWorkflow.execute(PutNewEquipmentInInventoryOrDrop(player)(trigger))
              }
              Players.buildCooldownReset(zone, player.Name, obj)
            case _ => ()
          }
          deployablePair = None

        case Zone.Deployable.IsBuilt(obj: TelepadDeployable) =>
          obj.Actor ! Deployable.Ownership(player)
          deployablePair match {
            case Some((deployable, tool: Telepad)) if deployable eq obj =>
              RemoveOldEquipmentFromInventory(player)(tool)
              val zone = player.Zone
              zone.GUID(obj.Router) match {
                case Some(v: Vehicle)
                  if v.Definition == GlobalDefinitions.router => ;
                case _ =>
                  player.Actor ! Player.LoseDeployable(obj)
                  TelepadControl.TelepadError(zone, player.Name, msg = "@Telepad_NoDeploy_RouterLost")
              }
              Players.buildCooldownReset(zone, player.Name, obj)
            case _ => ()
          }
          deployablePair = None

        case Zone.Deployable.IsBuilt(obj) =>
          obj.Actor ! Deployable.Ownership(player)
          deployablePair match {
            case Some((deployable, tool)) if deployable eq obj =>
              Players.buildCooldownReset(player.Zone, player.Name, obj)
              player.Find(tool) match {
                case Some(index) =>
                  Players.commonDestroyConstructionItem(player, tool, index)
                  Players.findReplacementConstructionItem(player, tool, index)
                case None =>
                  log.warn(s"${player.Name} should have destroyed a ${tool.Definition.Name} here, but could not find it")
              }
            case _ => ()
          }
          deployablePair = None

        case Player.LoseDeployable(obj) =>
          if (player.avatar.deployables.Remove(obj)) {
            player.Zone.LocalEvents ! LocalServiceMessage(player.Name, LocalAction.DeployableUIFor(obj.Definition.Item))
          }

        case _ => ;
      }

  def setExoSuit(exosuit: ExoSuitType.Value, subtype: Int): Boolean = {
    val willBecomeMax = exosuit == ExoSuitType.MAX
    val originalSuit = player.ExoSuit
    val originalSubtype = Loadout.DetermineSubtype(player)
    val changeSuit = originalSuit != exosuit
    val changeSubtype = originalSubtype != subtype
    val doChangeArmor = (changeSuit || changeSubtype) &&
      Players.CertificationToUseExoSuit(player, exosuit, subtype) &&
      (if (willBecomeMax) {
        val weapon = GlobalDefinitions.MAXArms(subtype, player.Faction)
        player.avatar.purchaseCooldown(weapon)
          .collect(_ => false)
          .getOrElse {
            avatarActor ! AvatarActor.UpdatePurchaseTime(weapon)
            true
          }
      } else {
        true
      })
    if (doChangeArmor) {
      log.info(s"${player.Name} wants to change to a different exo-suit - $exosuit")
      val beforeHolsters = Players.clearHolsters(player.Holsters().iterator)
      val beforeInventory = player.Inventory.Clear()
      //update suit internally
      val originalArmor = player.Armor
      player.ExoSuit = exosuit
      val toMaxArmor = player.MaxArmor
      val toArmor = toMaxArmor
      if (originalSuit != exosuit || originalArmor != toMaxArmor) {
        player.LogActivity(RepairFromExoSuitChange(exosuit, toMaxArmor - originalArmor))
      }
      player.Armor = toMaxArmor
      //ensure arm is down, even if it needs to go back up
      if (player.DrawnSlot != Player.HandsDownSlot) {
        player.DrawnSlot = Player.HandsDownSlot
      }
      val (toDelete, toDrop, afterHolsters, afterInventory) = if (originalSuit == ExoSuitType.MAX) {
        //was max
        val (delete, insert) = beforeHolsters.partition(elem => elem.obj.Size == EquipmentSize.Max)
        if (willBecomeMax) {
          //changing to a different kind(?) of max
          (delete, Nil, insert, beforeInventory)
        } else {
          //changing to a vanilla exo-suit
          val (newHolsters, unplacedHolsters) = Players.fillEmptyHolsters(player.Holsters().iterator, insert ++ beforeInventory)
          val (inventory, unplacedInventory) = GridInventory.recoverInventory(unplacedHolsters, player.Inventory)
          (delete, unplacedInventory.map(InventoryItem(_, -1)), newHolsters, inventory)
        }
      } else if (willBecomeMax) {
        //will be max, drop everything but melee slot
        val (melee, other) = beforeHolsters.partition(elem => elem.obj.Size == EquipmentSize.Melee)
        val (inventory, unplacedInventory) = GridInventory.recoverInventory(beforeInventory ++ other, player.Inventory)
        (Nil, unplacedInventory.map(InventoryItem(_, -1)), melee, inventory)
      } else {
        //was not a max nor will become a max; vanilla exo-suit to a vanilla-exo-suit
        val (insert, unplacedHolsters) = Players.fillEmptyHolsters(player.Holsters().iterator, beforeHolsters ++ beforeInventory)
        val (inventory, unplacedInventory) = GridInventory.recoverInventory(unplacedHolsters, player.Inventory)
        (Nil, unplacedInventory.map(InventoryItem(_, -1)), insert, inventory)
      }
      //insert
      afterHolsters.foreach(elem => player.Slot(elem.start).Equipment = elem.obj)
      afterInventory.foreach(elem => player.Inventory.InsertQuickly(elem.start, elem.obj))
      //deactivate non-passive implants
      avatarActor ! AvatarActor.DeactivateActiveImplants
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Zone.id,
        AvatarAction.ChangeExosuit(
          player.GUID,
          toArmor,
          exosuit,
          subtype,
          player.LastDrawnSlot,
          willBecomeMax,
          beforeHolsters.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
          afterHolsters,
          beforeInventory.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
          afterInventory,
          toDrop,
          toDelete.map { case InventoryItem(obj, _) => (obj, obj.GUID) }
        )
      )
      true
    } else {
      false
    }
  }

  def loseDeployableOwnership(obj: Deployable): Boolean = {
    if (player.avatar.deployables.Remove(obj)) {
      obj.Actor ! Deployable.Ownership(None)
      player.Zone.LocalEvents ! LocalServiceMessage(player.Name, LocalAction.DeployableUIFor(obj.Definition.Item))
      true
    }
    else {
      false
    }
  }

  def setupDeployable(obj: Deployable, tool: ConstructionItem): Unit = {
    if (deployablePair.isEmpty) {
      val zone = player.Zone
      val deployables = player.avatar.deployables
      if (deployables.Valid(obj) &&
          !deployables.Contains(obj) &&
          Players.deployableWithinBuildLimits(player, obj)) {
        //deployables, upon construction, may display an animation effect
        tool.Definition match {
          case GlobalDefinitions.router_telepad => () /* no special animation */
          case GlobalDefinitions.ace
            if obj.Definition.deployAnimation == DeployAnimation.Standard =>
            zone.LocalEvents ! LocalServiceMessage(
              zone.id,
              LocalAction.TriggerEffectLocation(
                obj.OwnerGuid.getOrElse(Service.defaultPlayerGUID),
                "spawn_object_effect",
                obj.Position,
                obj.Orientation
              )
            )
          case GlobalDefinitions.advanced_ace
            if obj.Definition.deployAnimation == DeployAnimation.Fdu =>
            zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.PutDownFDU(player.GUID))
          case _ =>
            org.log4s
              .getLogger(name = "Deployables")
              .warn(s"not sure what kind of construction item to animate - ${tool.Definition.Name}")
        }
        deployablePair = Some((obj, tool))
        obj.Faction = player.Faction
        obj.AssignOwnership(player)
        obj.Actor ! Zone.Deployable.Setup()
      }
      else {
        log.warn(s"cannot build a ${obj.Definition.Name}")
        DropEquipmentFromInventory(player)(tool, Some(obj.Position))
        Players.buildCooldownReset(zone, player.Name, obj)
        obj.Position = Vector3.Zero
        obj.AssignOwnership(None)
        zone.Deployables ! Zone.Deployable.Dismiss(obj)
      }
    } else {
      log.warn(s"already building one deployable, so cannot build a ${obj.Definition.Name}")
      obj.Position = Vector3.Zero
      obj.AssignOwnership(None)
      val zone = player.Zone
      zone.Deployables ! Zone.Deployable.Dismiss(obj)
      Players.buildCooldownReset(zone, player.Name, obj)
    }
  }

  override protected def PerformDamage(
    target: Target,
    applyDamageTo: Output
  ): Unit = {
    if (player.isAlive && !player.spectator) {
      val originalHealth    = player.Health
      val originalArmor     = player.Armor
      val originalStamina   = player.avatar.stamina
      val originalCapacitor = player.Capacitor.toInt
      val cause             = applyDamageTo(player)
      val health            = player.Health
      val armor             = player.Armor
      val stamina           = player.avatar.stamina
      val capacitor         = player.Capacitor.toInt
      val damageToHealth    = originalHealth - health
      val damageToArmor     = originalArmor - armor
      val damageToStamina   = originalStamina - stamina
      val damageToCapacitor = originalCapacitor - capacitor
      HandleDamage(player, cause, damageToHealth, damageToArmor, damageToStamina, damageToCapacitor)
      if (damageToHealth > 0 || damageToArmor > 0 || damageToStamina > 0 || damageToCapacitor > 0) {
        damageLog.info(
          s"${player.Name}-infantry: BEFORE=$originalHealth/$originalArmor/$originalStamina/$originalCapacitor, AFTER=$health/$armor/$stamina/$capacitor, CHANGE=$damageToHealth/$damageToArmor/$damageToStamina/$damageToCapacitor"
        )
      }
    }
  }

  /**
    * na
    * @param target na
    */
  def HandleDamage(
                    target: Player,
                    cause: DamageResult,
                    damageToHealth: Int,
                    damageToArmor: Int,
                    damageToStamina: Int,
                    damageToCapacitor: Int
                  ): Unit = {
    //always do armor update
    if (damageToArmor > 0) {
      val zone = target.Zone
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.PlanetsideAttributeToAll(target.GUID, 4, target.Armor)
      )
    }
    //choose
    if (target.Health > 0) {
      //alive
      if (target.Health <= 25 &&
          (player.avatar.implants.flatten.find { _.definition.implantType == ImplantType.SecondWind } match {
            case Some(wind) => wind.initialized
            case _          => false
      })) {
        //activate second wind
        player.Health += 25
        player.LogActivity(HealFromImplant(ImplantType.SecondWind, 25))
        avatarActor ! AvatarActor.RestoreStamina(25)
        avatarActor ! AvatarActor.ResetImplant(ImplantType.SecondWind)
      }
      //take damage/update
      DamageAwareness(target, cause, damageToHealth, damageToArmor, damageToStamina, damageToCapacitor)
    } else {
      //ded
      DestructionAwareness(target, cause)
    }
  }

  def DamageAwareness(
                       target: Player,
                       cause: DamageResult,
                       damageToHealth: Int,
                       damageToArmor: Int,
                       damageToStamina: Int,
                       damageToCapacitor: Int
                     ): Unit = {
    val targetGUID            = target.GUID
    val zone                  = target.Zone
    val zoneId                = zone.id
    val events                = zone.AvatarEvents
    val health                = target.Health
    var announceConfrontation = damageToArmor > 0
    //special effects
    if (Damageable.CanJammer(target, cause.interaction)) {
      TryJammerEffectActivate(target, cause)
    }
    val aggravated: Boolean = TryAggravationEffectActivate(cause) match {
      case Some(aggravation) =>
        StartAuraEffect(aggravation.effect_type, aggravation.timing.duration)
        announceConfrontation = true //useful if initial damage (to anything) is zero
        //initial damage for aggravation, but never treat as "aggravated"
        false
      case _ =>
        cause.interaction.cause.source.Aggravated.nonEmpty
    }
    //log historical event (always)
    target.LogActivity(cause)
    //stat changes
    if (damageToCapacitor > 0) {
      events ! AvatarServiceMessage(
        target.Name,
        AvatarAction.PlanetsideAttributeSelf(targetGUID, 7, target.Capacitor.toLong)
      )
      announceConfrontation = true //TODO should we?
    }
    if (damageToStamina > 0) {
      avatarActor ! AvatarActor.ConsumeStamina(damageToStamina)
      announceConfrontation = true //TODO should we?
    }
    if (damageToHealth > 0) {
      events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 0, health))
      announceConfrontation = true
    }
    val countableDamage = damageToHealth + damageToArmor
    if(announceConfrontation) {
      if (aggravated) {
        events ! AvatarServiceMessage(
          zoneId,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, AggravatedDamageMessage(targetGUID, countableDamage))
        )
      } else {
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause)
        //alert to damage source
        cause.adversarial match {
          case Some(adversarial) =>
            adversarial.attacker match {
              case pSource: PlayerSource => //player damage
                val name = pSource.Name
                zone.LivePlayers.find(_.Name == name).orElse(zone.Corpses.find(_.Name == name)) match {
                  case Some(tplayer) =>
                    zone.AvatarEvents ! AvatarServiceMessage(
                      target.Name,
                      AvatarAction.HitHint(tplayer.GUID, target.GUID)
                    )
                  case None =>
                    zone.AvatarEvents ! AvatarServiceMessage(
                      target.Name,
                      AvatarAction.SendResponse(
                        Service.defaultPlayerGUID,
                        DamageWithPositionMessage(countableDamage, pSource.Position)
                      )
                    )
                }
              case source =>
                zone.AvatarEvents ! AvatarServiceMessage(
                  target.Name,
                  AvatarAction.SendResponse(
                    Service.defaultPlayerGUID,
                    DamageWithPositionMessage(countableDamage, source.Position)
                  )
                )
            }
          case None =>
            cause.interaction.cause match {
              case o: PainboxReason =>
                zone.AvatarEvents ! AvatarServiceMessage(
                  target.Name,
                  AvatarAction.EnvironmentalDamage(target.GUID, o.entity.GUID, countableDamage)
                )
              case _: CollisionReason =>
                events ! AvatarServiceMessage(
                  zoneId,
                  AvatarAction.SendResponse(Service.defaultPlayerGUID, AggravatedDamageMessage(targetGUID, countableDamage))
                )
              case _ =>
                zone.AvatarEvents ! AvatarServiceMessage(
                  target.Name,
                  AvatarAction.EnvironmentalDamage(target.GUID, ValidPlanetSideGUID(0), countableDamage)
                )
            }
        }
      }
    }
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
    * @param target na
    * @param cause na
    */
  def DestructionAwareness(target: Player, cause: DamageResult): Unit = {
    val player_guid  = target.GUID
    val pos          = target.Position
    val respawnTimer = 300000 //milliseconds
    val zone         = target.Zone
    val events       = zone.AvatarEvents
    val nameChannel  = target.Name
    val zoneChannel  = zone.id
    target.Die
    //aura effects cancel
    EndAllEffects()
    //aggravation cancel
    EndAllAggravation()
    //unjam
    CancelJammeredSound(target)
    super.CancelJammeredStatus(target)
    //uninitialize implants
    avatarActor ! AvatarActor.DeinitializeImplants

    //log historical event
    target.LogActivity(cause)
    //log message
    cause.adversarial match {
      case Some(a) =>
        damageLog.info(s"${a.defender.Name} was killed by ${a.attacker.Name}")
      case _ =>
        damageLog.info(s"${player.Name} killed ${player.Sex.pronounObject}self")
    }

    // This would normally happen async as part of AvatarAction.Killed, but if it doesn't happen before deleting calling AvatarAction.ObjectDelete on the player the LLU will end up invisible to others if carried
    // Therefore, queue it up to happen first.
    events ! AvatarServiceMessage(nameChannel, AvatarAction.DropSpecialItem())

    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.Killed(player_guid, target.VehicleSeated)
    ) //align client interface fields with state
    zone.GUID(target.VehicleSeated) match {
      case Some(obj: Mountable) =>
        //boot cadaver from mount internally (vehicle perspective)
        obj.PassengerInSeat(target) match {
          case Some(index) =>
            obj.Seats(index).unmount(target)
          case _ => ;
        }
        //boot cadaver from mount on client
        events ! AvatarServiceMessage(
          nameChannel,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            ObjectDetachMessage(obj.GUID, player_guid, target.Position, Vector3.Zero)
          )
        )
        //make player invisible on client
        events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 29, 1))
        //only the dead player should "see" their own body, so that the death camera has something to focus on
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.ObjectDelete(player_guid, player_guid))
      case _ => ;
    }
    events ! AvatarServiceMessage(zoneChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 0, 0)) //health
    if (target.Capacitor > 0) {
      target.Capacitor = 0
      events ! AvatarServiceMessage(nameChannel, AvatarAction.PlanetsideAttributeToAll(player_guid, 7, 0)) // capacitor
    }
    val attribute = DamageableEntity.attributionTo(cause, target.Zone, player_guid)
    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        DestroyMessage(player_guid, attribute, Service.defaultPlayerGUID, pos)
      ) //how many players get this message?
    )
    events ! AvatarServiceMessage(
      nameChannel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        AvatarDeadStateMessage(DeadState.Dead, respawnTimer, respawnTimer, pos, target.Faction, unk5=true)
      )
    )
    //TODO other methods of death?
    val pentry = PlayerSource(target)
    cause
      .adversarial
      .collect { case out @ Adversarial(attacker, _, _) if attacker != PlayerSource.Nobody => out }
      .orElse {
        target.LastDamage.collect {
          case attack if System.currentTimeMillis() - attack.interaction.hitTime < (10 seconds).toMillis =>
            attack
              .adversarial
              .collect { case out @ Adversarial(attacker, _, _) if attacker != PlayerSource.Nobody => out }
        }.flatten
      } match {
      case Some(adversarial) =>
        events ! AvatarServiceMessage(
          zoneChannel,
          AvatarAction.DestroyDisplay(adversarial.attacker, pentry, adversarial.implement)
        )
      case _ =>
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(pentry, pentry, 0))
    }
    zone.actor ! ZoneActor.RewardThisDeath(player)
  }

  def suicide() : Unit = {
    if (player.Health > 0 || player.isAlive) {
      PerformDamage(
        player,
        DamageInteraction(
          PlayerSource(player),
          SuicideReason(),
          player.Position
        ).calculate()
      )
    }
  }

  /**
    * Start the jammered buzzing.
    * Although, as a rule, the jammering sound effect should last as long as the jammering status,
    * Infantry seem to hear the sound for a bit longer than the effect.
    * @see `JammableBehavior.StartJammeredSound`
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds;
    *            by default, 30000
    */
  override def StartJammeredSound(target: Any, dur: Int): Unit =
    target match {
      case obj: Player if !jammedSound =>
        obj.Zone.AvatarEvents ! AvatarServiceMessage(
          obj.Zone.id,
          AvatarAction.PlanetsideAttributeToAll(obj.GUID, 27, 1)
        )
        super.StartJammeredSound(obj, 3000)
      case _ => ;
    }

  /**
    * Perform a variety of tasks to indicate being jammered.
    * Deactivate implants (should also uninitialize them),
    * delay stamina regeneration for a certain number of turns,
    * and set the jammered status on specific holstered equipment.
    * @see `JammableBehavior.StartJammeredStatus`
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds
    */
  override def StartJammeredStatus(target: Any, dur: Int): Unit = {
    avatarActor ! AvatarActor.DeinitializeImplants
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(5 seconds)
    super.StartJammeredStatus(target, dur)
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    avatarActor ! AvatarActor.SoftResetImplants
    super.CancelJammeredStatus(target)
  }

  /**
    * Stop the jammered buzzing.
    * @see `JammableBehavior.CancelJammeredSound`
    * @param target an object that can be affected by the jammered status
    */
  override def CancelJammeredSound(target: Any): Unit =
    target match {
      case obj: Player if jammedSound =>
        obj.Zone.AvatarEvents ! AvatarServiceMessage(
          obj.Zone.id,
          AvatarAction.PlanetsideAttributeToAll(obj.GUID, 27, 0)
        )
        super.CancelJammeredSound(obj)
      case _ => ;
    }

  def RepairToolValue(item: Tool): Float = {
    item.AmmoSlot.Box.Definition.repairAmount +
    (if (player.ExoSuit != ExoSuitType.MAX) {
      item.FireMode.Add.Damage0
    }
    else {
      item.FireMode.Add.Damage3
    })
  }

  def MessageDeferredCallback(msg: Any): Unit = {
    msg match {
      case Containable.MoveItem(_, item, _) =>
        //momentarily depict item back where it was originally
        val obj = ContainerObject
        obj.Find(item) match {
          case Some(slot) =>
            PutItemInSlotCallback(item, slot)
          case None => ;
        }
      case _ => ;
    }
  }

  def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    val obj       = ContainerObject
    val zone      = obj.Zone
    val events    = zone.AvatarEvents
    val toChannel = if (player.isBackpack) {
      self.toString
    } else if (obj.VisibleSlots.contains(slot)) {
      zone.id
    } else {
      player.Name
    }
    item.Faction = PlanetSideEmpire.NEUTRAL
    if (slot == obj.DrawnSlot) {
      obj.DrawnSlot = Player.HandsDownSlot
    }
    events ! AvatarServiceMessage(toChannel, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID))
  }

  def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    val obj        = ContainerObject
    val guid       = obj.GUID
    val zone       = obj.Zone
    val events     = zone.AvatarEvents
    val name       = player.Name
    val faction    = obj.Faction
    val toChannel = if (player.isBackpack) { self.toString } else { name }
    val willBeVisible = obj.VisibleSlots.contains(slot)
    item.Faction = faction
    //handle specific types of items
    item match {
      case trigger: BoomerTrigger =>
        //pick up the trigger, own the boomer; make certain whole faction is aware of that
        zone.GUID(trigger.Companion) match {
          case Some(obj: BoomerDeployable) =>
            val deployables = player.avatar.deployables
            if (deployables.Valid(obj)) {
              Players.gainDeployableOwnership(player, obj, deployables.AddOverLimit)
            }
          case _ => ;
        }

      case citem: ConstructionItem
        if willBeVisible =>
        if (citem.AmmoTypeIndex > 0) {
          //can not preserve ammo type in construction tool packets
          citem.resetAmmoTypes()
        }
        Deployables.initializeConstructionItem(player.avatar.certifications, citem)

      case _ => ;
    }
    events ! AvatarServiceMessage(
      toChannel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        OCM.detailed(item, ObjectCreateMessageParent(guid, slot))
      )
    )
    if (!player.isBackpack && willBeVisible) {
      events ! AvatarServiceMessage(zone.id, AvatarAction.EquipmentInHand(guid, guid, slot, item))
    }
  }

  def SwapItemCallback(item: Equipment, fromSlot: Int): Unit = {
    val obj       = ContainerObject
    val zone      = obj.Zone
    val toChannel = if (player.isBackpack) {
      self.toString
    } else if (obj.VisibleSlots.contains(fromSlot)) {
      zone.id
    } else {
      player.Name
    }
    zone.AvatarEvents ! AvatarServiceMessage(
      toChannel,
      AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID)
    )
  }

  def UpdateAuraEffect(target: AuraEffectBehavior.Target) : Unit = {
    import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
    val zone = target.Zone
    val value = target.Aura.foldLeft(0)(_ + PlayerControl.auraEffectToAttributeValue(_))
    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.PlanetsideAttributeToAll(target.GUID, 54, value))
  }
}

object PlayerControl {
  /** na */
  final case class SetExoSuit(exosuit: ExoSuitType.Value, subtype: Int)

  /** na */
  final case class ObjectHeld(slot: Int, updateMyHolsterArm: Boolean)
  object ObjectHeld {
    def apply(slot: Int): ObjectHeld = ObjectHeld(slot, updateMyHolsterArm=false)
  }

  /**
    * Transform an applicable Aura effect into its `PlanetsideAttributeMessage` value.
    * @see `Aura`
    * @see `PlanetsideAttributeMessage`
    * @param effect the aura effect
    * @return the attribute value for that effect
    */
  private def auraEffectToAttributeValue(effect: Aura): Int = effect match {
    case Aura.Plasma => 1
    case Aura.Comet  => 2
    case Aura.Napalm => 4
    case Aura.Fire   => 8
    case _           => 0
  }

  def sendResponse(zone: Zone, channel: String, msg: PlanetSideGamePacket): Unit = {
    zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.SendResponse(Service.defaultPlayerGUID, msg))
  }

  def maxRestriction(player: Player, slot: Int): Boolean = {
    if (player.ExoSuit == ExoSuitType.MAX) {
      slot != 0
    } else {
      player.ResistArmMotion(Player.neverRestrict) //reset
      false
    }
  }
}
