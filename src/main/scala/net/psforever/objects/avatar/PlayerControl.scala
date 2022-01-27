// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.{Actor, ActorRef, Props, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.login.WorldSession.{DropEquipmentFromInventory, HoldNewEquipmentUp, PutNewEquipmentInInventoryOrDrop, RemoveOldEquipmentFromInventory}
import net.psforever.objects.{Player, _}
import net.psforever.objects.ballistics.PlayerSource
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
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.repair.Repairable
import net.psforever.objects.serverobject.shuttle.OrbitalShuttlePad
import net.psforever.objects.vital.collision.CollisionReason
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.{PainboxReason, SuicideReason}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.services.hart.ShuttleState
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
  def JammableObject = player

  def DamageableObject = player

  def ContainerObject = player

  def AggravatedObject = player

  def AuraTargetObject = player
  ApplicableEffect(Aura.Plasma)
  ApplicableEffect(Aura.Napalm)
  ApplicableEffect(Aura.Comet)
  ApplicableEffect(Aura.Fire)

  def InteractiveObject = player
  SetInteraction(EnvironmentAttribute.Water, doInteractingWithWater)
  SetInteraction(EnvironmentAttribute.Lava, doInteractingWithLava)
  SetInteraction(EnvironmentAttribute.Death, doInteractingWithDeath)
  SetInteraction(EnvironmentAttribute.GantryDenialField, doInteractingWithGantryField)
  SetInteractionStop(EnvironmentAttribute.Water, stopInteractingWithWater)
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
            PerformDamage(player, reason.calculate())
            suicide()
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
              player.History(
                HealFromEquipment(
                  PlayerSource(player),
                  PlayerSource(user),
                  newHealth - originalHealth,
                  GlobalDefinitions.medicalapplicator
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
              Players.FinishRevivingPlayer(player, user.Name, item),
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
              player.History(
                RepairFromEquipment(
                  PlayerSource(player),
                  PlayerSource(user),
                  newArmor - originalArmor,
                  GlobalDefinitions.bank
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
                      player.History(HealFromKit(PlayerSource(player), 25, kdef))
                      player.Health = player.Health + 25
                      (Some(index), 0, player.Health, "")
                    }
                  } else if (kdef == GlobalDefinitions.super_medkit) {
                    if (player.Health == player.MaxHealth) {
                      (None, 0, 0, "@HealComplete")
                    } else {
                      player.History(HealFromKit(PlayerSource(player), 100, kdef))
                      player.Health = player.Health + 100
                      (Some(index), 0, player.Health, "")
                    }
                  } else if (kdef == GlobalDefinitions.super_armorkit) {
                    if (player.Armor == player.MaxArmor) {
                      (None, 0, 0, "Armor at maximum - No repairing required.")
                    } else {
                      player.History(RepairFromKit(PlayerSource(player), 200, kdef))
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

        case Terminal.TerminalMessage(_, msg, order) =>
          order match {
            case Terminal.BuyExosuit(exosuit, subtype) =>
              val result = setExoSuit(exosuit, subtype)
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, result)
              )

            case Terminal.InfantryLoadout(exosuit, subtype, holsters, inventory) =>
              log.info(s"${player.Name} wants to change equipment loadout to their option #${msg.unk1 + 1}")
              val fallbackSubtype = 0
              val fallbackSuit = ExoSuitType.Standard
              val originalSuit = player.ExoSuit
              val originalSubtype = Loadout.DetermineSubtype(player)
              //sanitize exo-suit for change
              val dropPred = ContainableBehavior.DropPredicate(player)
              val oldHolsters = Players.clearHolsters(player.Holsters().iterator)
              val dropHolsters = oldHolsters.filter(dropPred)
              val oldInventory = player.Inventory.Clear()
              val dropInventory = oldInventory.filter(dropPred)
              val toDeleteOrDrop : List[InventoryItem] = (player.FreeHand.Equipment match {
                case Some(obj) =>
                  val out = InventoryItem(obj, -1)
                  player.FreeHand.Equipment = None
                  if (dropPred(out)) {
                    List(out)
                  } else {
                    Nil
                  }
                case _ =>
                  Nil
              }) ++ dropHolsters ++ dropInventory
              //a loadout with a prohibited exo-suit type will result in the fallback exo-suit type
              //imposed 5min delay on mechanized exo-suit switches
              val (nextSuit, nextSubtype) =
              if (
                Players.CertificationToUseExoSuit(player, exosuit, subtype) &&
                (if (exosuit == ExoSuitType.MAX) {
                  val weapon = GlobalDefinitions.MAXArms(subtype, player.Faction)
                  player.avatar.purchaseCooldown(weapon) match {
                    case Some(_) => false
                    case None =>
                      avatarActor ! AvatarActor.UpdatePurchaseTime(weapon)
                      true
                  }
                } else {
                  true
                })
              ) {
                (exosuit, subtype)
              } else {
                log.warn(
                  s"${player.Name} no longer has permission to wear the exo-suit type $exosuit; will wear $fallbackSuit instead"
                )
                (fallbackSuit, fallbackSubtype)
              }
              //sanitize (incoming) inventory
              //TODO equipment permissions; these loops may be expanded upon in future
              val curatedHolsters = for {
                item <- holsters
                //id = item.obj.Definition.ObjectId
                //lastTime = player.GetLastUsedTime(id)
                if true
              } yield item
              val curatedInventory = for {
                item <- inventory
                //id = item.obj.Definition.ObjectId
                //lastTime = player.GetLastUsedTime(id)
                if true
              } yield item
              //update suit internally
              val originalArmor = player.Armor
              player.ExoSuit = nextSuit
              val toMaxArmor = player.MaxArmor
              val toArmor =
                if (originalSuit != nextSuit || originalSubtype != nextSubtype || originalArmor > toMaxArmor) {
                  player.History(HealFromExoSuitChange(PlayerSource(player), nextSuit))
                  player.Armor = toMaxArmor
                } else {
                  player.Armor = originalArmor
                }
              //ensure arm is down, even if it needs to go back up
              if (player.DrawnSlot != Player.HandsDownSlot) {
                player.DrawnSlot = Player.HandsDownSlot
              }
              //a change due to exo-suit permissions mismatch will result in (more) items being re-arranged and/or dropped
              //dropped items are not registered and can just be forgotten
              val (afterHolsters, afterInventory) = if (nextSuit == exosuit) {
                (
                  //melee slot preservation for MAX
                  if (nextSuit == ExoSuitType.MAX) {
                    holsters.filter(_.start == 4)
                  } else {
                    curatedHolsters.filterNot(dropPred)
                  },
                  curatedInventory.filterNot(dropPred)
                )
              } else {
                //our exo-suit type was hijacked by changing permissions; we shouldn't even be able to use that loadout(!)
                //holsters
                val leftoversForInventory = Players.fillEmptyHolsters(
                  player.Holsters().iterator,
                  (curatedHolsters ++ curatedInventory).filterNot(dropPred)
                )
                val finalHolsters = player.HolsterItems()
                //inventory
                val (finalInventory, _) = GridInventory.recoverInventory(leftoversForInventory, player.Inventory)
                (finalHolsters, finalInventory)
              }
              (afterHolsters ++ afterInventory).foreach { entry => entry.obj.Faction = player.Faction }
              afterHolsters.foreach {
                case InventoryItem(citem: ConstructionItem, _) =>
                  Deployables.initializeConstructionItem(player.avatar.certifications, citem)
                case _ => ;
              }
              toDeleteOrDrop.foreach { entry => entry.obj.Faction = PlanetSideEmpire.NEUTRAL }
              //deactivate non-passive implants
              avatarActor ! AvatarActor.DeactivateActiveImplants()
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Zone.id,
                AvatarAction.ChangeLoadout(
                  player.GUID,
                  toArmor,
                  nextSuit,
                  nextSubtype,
                  player.LastDrawnSlot,
                  exosuit == ExoSuitType.MAX,
                  oldHolsters.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
                  afterHolsters,
                  oldInventory.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
                  afterInventory,
                  toDeleteOrDrop
                )
              )
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, true)
              )
            case _ => assert(false, msg.toString)
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
          deployablePair match {
            case Some((deployable, tool)) if deployable eq obj =>
              val zone = player.Zone
              //boomers
              val trigger = new BoomerTrigger
              trigger.Companion = obj.GUID
              obj.Trigger = trigger
              //TODO sufficiently delete the tool
              zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(player.GUID, tool.GUID))
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
            case _ => ;
          }
          deployablePair = None

        case Zone.Deployable.IsBuilt(obj: TelepadDeployable) =>
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
            case _ => ;
          }
          deployablePair = None

        case Zone.Deployable.IsBuilt(obj) =>
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
            case _ => ;
          }
          deployablePair = None

        case Player.LoseDeployable(obj) =>
          if (player.avatar.deployables.Remove(obj)) {
            player.Zone.LocalEvents ! LocalServiceMessage(player.Name, LocalAction.DeployableUIFor(obj.Definition.Item))
          }

        case _ => ;
      }

  def setExoSuit(exosuit: ExoSuitType.Value, subtype: Int): Boolean = {
    var toDelete : List[InventoryItem] = Nil
    val originalSuit = player.ExoSuit
    val originalSubtype = Loadout.DetermineSubtype(player)
    val requestToChangeArmor = originalSuit != exosuit || originalSubtype != subtype
    val allowedToChangeArmor = Players.CertificationToUseExoSuit(player, exosuit, subtype) &&
                               (if (exosuit == ExoSuitType.MAX) {
                                 val weapon = GlobalDefinitions.MAXArms(subtype, player.Faction)
                                 player.avatar.purchaseCooldown(weapon) match {
                                   case Some(_) =>
                                     false
                                   case None =>
                                     avatarActor ! AvatarActor.UpdatePurchaseTime(weapon)
                                     true
                                 }
                               }
                               else {
                                 true
                               })
    if (requestToChangeArmor && allowedToChangeArmor) {
      log.info(s"${player.Name} wants to change to a different exo-suit - $exosuit")
      val beforeHolsters = Players.clearHolsters(player.Holsters().iterator)
      val beforeInventory = player.Inventory.Clear()
      //change suit
      val originalArmor = player.Armor
      player.ExoSuit = exosuit //changes the value of MaxArmor to reflect the new exo-suit
      val toMaxArmor = player.MaxArmor
      val toArmor = if (originalSuit != exosuit || originalSubtype != subtype || originalArmor > toMaxArmor) {
        player.History(HealFromExoSuitChange(PlayerSource(player), exosuit))
        player.Armor = toMaxArmor
      }
      else {
        player.Armor = originalArmor
      }
      //ensure arm is down, even if it needs to go back up
      if (player.DrawnSlot != Player.HandsDownSlot) {
        player.DrawnSlot = Player.HandsDownSlot
      }
      val normalHolsters = if (originalSuit == ExoSuitType.MAX) {
        val (maxWeapons, normalWeapons) = beforeHolsters.partition(elem => elem.obj.Size == EquipmentSize.Max)
        toDelete ++= maxWeapons
        normalWeapons
      }
      else {
        beforeHolsters
      }
      //populate holsters
      val (afterHolsters, finalInventory) = if (exosuit == ExoSuitType.MAX) {
        (
          normalHolsters,
          Players.fillEmptyHolsters(List(player.Slot(4)).iterator, normalHolsters) ++ beforeInventory
        )
      }
      else if (originalSuit == exosuit) { //note - this will rarely be the situation
        (normalHolsters, Players.fillEmptyHolsters(player.Holsters().iterator, normalHolsters))
      }
      else {
        val (afterHolsters, toInventory) =
          normalHolsters.partition(elem => elem.obj.Size == player.Slot(elem.start).Size)
        afterHolsters.foreach({ elem => player.Slot(elem.start).Equipment = elem.obj })
        val remainder = Players.fillEmptyHolsters(player.Holsters().iterator, toInventory ++ beforeInventory)
        (
          player.HolsterItems(),
          remainder
        )
      }
      //put items back into inventory
      val (stow, drop) = if (originalSuit == exosuit) {
        (finalInventory, Nil)
      }
      else {
        val (a, b) = GridInventory.recoverInventory(finalInventory, player.Inventory)
        (
          a,
          b.map {
            InventoryItem(_, -1)
          }
        )
      }
      stow.foreach { elem =>
        player.Inventory.InsertQuickly(elem.start, elem.obj)
      }
      //deactivate non-passive implants
      avatarActor ! AvatarActor.DeactivateActiveImplants()
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Zone.id,
        AvatarAction.ChangeExosuit(
          player.GUID,
          toArmor,
          exosuit,
          subtype,
          player.LastDrawnSlot,
          exosuit == ExoSuitType.MAX && requestToChangeArmor,
          beforeHolsters.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
          afterHolsters,
          beforeInventory.map { case InventoryItem(obj, _) => (obj, obj.GUID) },
          stow,
          drop,
          toDelete.map { case InventoryItem(obj, _) => (obj, obj.GUID) }
        )
      )
      true
    }
    else {
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
        tool.Definition match {
          case GlobalDefinitions.ace | /* animation handled in deployable lifecycle */
               GlobalDefinitions.router_telepad => ; /* no special animation */
          case GlobalDefinitions.advanced_ace
            if obj.Definition.deployAnimation == DeployAnimation.Fdu =>
            zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.PutDownFDU(player.GUID))
          case _ =>
            org.log4s.getLogger(name = "Deployables").warn(
              s"not sure what kind of construction item to animate - ${tool.Definition.Name}"
            )
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
      if (target.Health <= 25 && target.Health + damageToHealth > 25 &&
          (player.avatar.implants.flatten.find { _.definition.implantType == ImplantType.SecondWind } match {
            case Some(wind) => wind.initialized
            case _          => false
      })) {
        //activate second wind
        player.Health += 25
        player.History(HealFromImplant(PlayerSource(player), 25, ImplantType.SecondWind))
        avatarActor ! AvatarActor.ResetImplant(ImplantType.SecondWind)
        avatarActor ! AvatarActor.RestoreStamina(25)
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
    target.History(cause)
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
    avatarActor ! AvatarActor.DeinitializeImplants()

    //log historical event
    target.History(cause)
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
        AvatarDeadStateMessage(DeadState.Dead, respawnTimer, respawnTimer, pos, target.Faction, true)
      )
    )
    //TODO other methods of death?
    val pentry = PlayerSource(target)
    (cause.adversarial match {
      case out @ Some(_) =>
        out
      case _ =>
        target.LastDamage match {
          case Some(attack) if System.currentTimeMillis() - attack.interaction.hitTime < (10 seconds).toMillis =>
            attack.adversarial
          case _ =>
            None
        }
      }) match {
      case Some(adversarial) =>
        events ! AvatarServiceMessage(
          zoneChannel,
          AvatarAction.DestroyDisplay(adversarial.attacker, pentry, adversarial.implement)
        )
      case None =>
        events ! AvatarServiceMessage(zoneChannel, AvatarAction.DestroyDisplay(pentry, pentry, 0))
    }
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
    avatarActor ! AvatarActor.DeinitializeImplants()
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(5 seconds)
    super.StartJammeredStatus(target, dur)
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    avatarActor ! AvatarActor.InitializeImplants()
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

  /**
    * Water causes players to slowly suffocate.
    * When they (finally) drown, they will die.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable;
    *             for players, this will be data from any mounted vehicles
    */
  def doInteractingWithWater(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val (effect: Boolean, time: Long, percentage: Float) =
      RespondsToZoneEnvironment.drowningInWateryConditions(obj, submergedCondition, interactionTime)
    if (effect) {
      import scala.concurrent.ExecutionContext.Implicits.global
      interactionTime = System.currentTimeMillis() + time
      submergedCondition = Some(OxygenState.Suffocation)
      interactionTimer = context.system.scheduler.scheduleOnce(delay = time milliseconds, self, Player.Die())
      //inform the player that they are in trouble
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Name,
        AvatarAction.OxygenState(OxygenStateTarget(player.GUID, OxygenState.Suffocation, percentage), data)
      )
    } else if (data.isDefined) {
      //inform the player that their mounted vehicle is in trouble (that they are in trouble)
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Name,
        AvatarAction.OxygenState(OxygenStateTarget(player.GUID, OxygenState.Suffocation, percentage), data)
      )
    }
  }

  /**
    * Lava causes players to take (considerable) damage until they inevitably die.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWithLava(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    if (player.isAlive) {
      PerformDamage(
        player,
        DamageInteraction(
          PlayerSource(player),
          EnvironmentReason(body, player),
          player.Position
        ).calculate()
      )
      if (player.Health > 0) {
        StartAuraEffect(Aura.Fire, duration = 1250L) //burn
        import scala.concurrent.ExecutionContext.Implicits.global
        interactionTimer = context.system.scheduler.scheduleOnce(delay = 250 milliseconds, self, InteractingWithEnvironment(player, body, None))
      }
    }
  }

  /**
    * Death causes players to die outright.
    * It's not even considered as environmental damage anymore.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWithDeath(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    suicide()
  }

  def doInteractingWithGantryField(
                                    obj: PlanetSideServerObject,
                                    body: PieceOfEnvironment,
                                    data: Option[OxygenStateTarget]
                                  ): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val field = body.asInstanceOf[GantryDenialField]
    val zone = player.Zone
    (zone.GUID(field.obbasemesh) match {
      case Some(pad : OrbitalShuttlePad) => zone.GUID(pad.shuttle)
      case _                             => None
    }) match {
      case Some(shuttle: Vehicle)
        if shuttle.Flying.contains(ShuttleState.State11.id) || shuttle.Faction != player.Faction =>
        val (pos, zang) = Vehicles.dismountShuttle(shuttle, field.mountPoint)
        shuttle.Zone.AvatarEvents ! AvatarServiceMessage(
          player.Name,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            PlayerStateShiftMessage(ShiftState(0, pos, zang, None)))
        )
      case Some(_: Vehicle) =>
        interactionTimer = context.system.scheduler.scheduleOnce(
          delay = 250 milliseconds,
          self,
          InteractingWithEnvironment(player, body, None)
        )
      case _ => ;
        //something configured incorrectly; no need to keep checking
    }
  }

  /**
    * When out of water, the player is no longer suffocating.
    * The player does have to endure a recovery period to get back to normal, though.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable;
    *             for players, this will be data from any mounted vehicles
    */
  def stopInteractingWithWater(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val (effect: Boolean, time: Long, percentage: Float) =
      RespondsToZoneEnvironment.recoveringFromWateryConditions(obj, submergedCondition, interactionTime)
    if (percentage == 100f) {
      recoverFromEnvironmentInteracting()
    }
    if (effect) {
      import scala.concurrent.ExecutionContext.Implicits.global
      submergedCondition = Some(OxygenState.Recovery)
      interactionTime = System.currentTimeMillis() + time
      interactionTimer = context.system.scheduler.scheduleOnce(delay = time milliseconds, self, RecoveredFromEnvironmentInteraction())
      //inform the player
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Name,
        AvatarAction.OxygenState(OxygenStateTarget(player.GUID, OxygenState.Recovery, percentage), data)
      )
    } else if (data.isDefined) {
      //inform the player
      player.Zone.AvatarEvents ! AvatarServiceMessage(
        player.Name,
        AvatarAction.OxygenState(OxygenStateTarget(player.GUID, OxygenState.Recovery, percentage), data)
      )
    }
  }

  override def recoverFromEnvironmentInteracting(): Unit = {
    super.recoverFromEnvironmentInteracting()
    submergedCondition = None
  }
}

object PlayerControl {
  /** na */
  final case class SetExoSuit(exosuit: ExoSuitType.Value, subtype: Int)

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
}
