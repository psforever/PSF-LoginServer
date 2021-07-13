// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.avatar.Certification
import net.psforever.login.WorldSession.FindEquipmentStock
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.ExoSuitDefinition
import net.psforever.objects.equipment.EquipmentSlot
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.guid.actor.TaskWorkflow
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.InfantryLoadout
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.{ChatMessageType, ExoSuitType, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.annotation.tailrec

object Players {
  private val log = org.log4s.getLogger("Players")

  /**
    * Evaluate the progress of the user applying a tool to modify some other server object.
    * This action is using the medical applicator to revive a fallen (dead but not released) ally.
    *
    * @param target   the player being affected by the revive action
    * @param user the player performing the revive action
    * @param item the tool being used to revive the target player
    * @param progress the current progress value
    * @return `true`, if the next cycle of progress should occur;
    *         `false`, otherwise
    */
  def RevivingTickAction(target: Player, user: Player, item: Tool)(progress: Float): Boolean = {
    if (
      !target.isAlive && !target.isBackpack &&
      user.isAlive && !user.isMoving &&
      user.Slot(user.DrawnSlot).Equipment.contains(item) && item.Magazine >= 25 &&
      Vector3.Distance(target.Position, user.Position) < target.Definition.RepairDistance
    ) {
      val events = target.Zone.AvatarEvents
      val uname  = user.Name
      events ! AvatarServiceMessage(
        uname,
        AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(target.GUID, progress.toInt))
      )
      true
    } else {
      false
    }
  }

  /**
    * na
    * @see `AvatarAction.Revive`
    * @see `AvatarResponse.Revive`
    * @param target the player being revived
    * @param medic the name of the player doing the reviving
    * @param item the tool being used to revive the target player
    */
  def FinishRevivingPlayer(target: Player, medic: String, item: Tool)(): Unit = {
    val name = target.Name
    log.info(s"$medic had revived $name")
    val magazine = item.Discharge(Some(25))
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      medic,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine)
      )
    )
    target.Zone.AvatarEvents ! AvatarServiceMessage(name, AvatarAction.Revive(target.GUID))
  }

  /**
    * Iterate over a group of `EquipmentSlot`s, some of which may be occupied with an item.
    * Remove any encountered items and add them to an output `List`.
    * @param iter the `Iterator` of `EquipmentSlot`s
    * @param index a number that equals the "current" holster slot (`EquipmentSlot`)
    * @param list a persistent `List` of `Equipment` in the holster slots
    * @return a `List` of `Equipment` in the holster slots
    */
  @tailrec def clearHolsters(
      iter: Iterator[EquipmentSlot],
      index: Int = 0,
      list: List[InventoryItem] = Nil
  ): List[InventoryItem] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot = iter.next()
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
  @tailrec def fillEmptyHolsters(iter: Iterator[EquipmentSlot], list: List[InventoryItem]): List[InventoryItem] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot = iter.next()
      if (slot.Equipment.isEmpty) {
        list.find(item => item.obj.Size == slot.Size) match {
          case Some(obj) =>
            val index = list.indexOf(obj)
            slot.Equipment = obj.obj
            fillEmptyHolsters(iter, list.take(index) ++ list.drop(index + 1))
          case None =>
            fillEmptyHolsters(iter, list)
        }
      } else {
        fillEmptyHolsters(iter, list)
      }
    }
  }

  /**
    * The player may don this exo-suit if the exo-suit has no requirements
    * or if the player has fulfilled the requirements of the exo-suit.
    * The "requirements" are certification purchases.
    * @param player the player
    * @param exosuit the exo-suit the player is trying to wear
    * @param subtype the variant of this exo-suit type;
    *                matters for mechanized assault exo-suits, mainly
    * @return `true`, if the player and the exo-suit are compatible;
    *        `false`, otherwise
    */
  def CertificationToUseExoSuit(player: Player, exosuit: ExoSuitType.Value, subtype: Int): Boolean = {
    ExoSuitDefinition.Select(exosuit, player.Faction).Permissions match {
      case Nil =>
        true
      case permissions if subtype != 0 =>
        val certs = player.avatar.certifications
        certs.intersect(permissions.toSet).nonEmpty &&
        certs.intersect(InfantryLoadout.DetermineSubtypeC(subtype)).nonEmpty
      case permissions =>
        player.avatar.certifications.intersect(permissions.toSet).nonEmpty
    }
  }

  /**
    * For a given selection of certification, find the player's engineer level.
    * The level of a player with no engineering certification is 0.
    * The certifications that matter are all related to the ability to repair damaged equipment and
    * the ability to place combat support utilities - combat engineering (ce) deployables - in the game world.
    * @see `Avatar.certifications`
    * @see `Certification`
    * @param player the player whose certifications are to be tested
    * @return the engineering level
    */
  def repairModifierLevel(player: Player): Int = {
    val certs = player.avatar.certifications
    if (certs.contains(Certification.AdvancedEngineering) ||
        certs.contains(Certification.AssaultEngineering) ||
        certs.contains(Certification.FortificationEngineering)) {
      3
    }
    else if (certs.contains(Certification.CombatEngineering)) {
      2
    }
    else if (certs.contains(Certification.Engineering)) {
      1
    }
    else {
      0
    }
  }

  /**
    * Test whether this deployable can be constructed by this given player.
    * The test actually involves a number of checks against numerical limits for supporting the deployable
    * (the first of which is whether there is any limit at all).
    * Depending on the result against limits successfully, various status messages can be dispatched to the client
    * and the deployable will be considered permitted to be constructed.<br>
    * <br>
    * The first placement limit is the actual number of a specific type of deployable.
    * The second placement limit is the actual number of a specific group (category) of deployables.
    * Depending on which limit is encountered, an "oldest entry" is struck from the list to make space.
    * This generates the first message - "@*OldestDestroyed."
    * Another message is generated if the number of that specific type of deployable
    * or the number of deployables available in its category matches against the maximum count allowed.
    * This generates the second message - "@*LimitReached."
    * These messages are mutually exclusive, with "@*OldestDestroyed" taking priority over "@*LimitReached."<br>
    * <br>
    * Finally, the player needs to actually manage the deployable.
    * Once that responsibility is proven, all tests are considered passed.
    * @see `ChatMsg`
    * @see `DeployableToolbox`
    * @see `DeployableToolbox.Add`
    * @see `DeployableToolbox.Available`
    * @see `DeployableToolbox.CountDeployable`
    * @see `DeployableToolbox.DisplaceFirst`
    * @see `Deployable.Deconstruct`
    * @see `Deployable.Ownership`
    * @see `gainDeployableOwnership`
    * @see `ObjectDeployedMessage`
    * @see `PlayerControl.sendResponse`
    * @param player the player that would manage the deployable
    * @param obj the deployable
    * @return `true`, if the deployable can be constructed under the control of and be supported by the player;
    *        `false`, otherwise
    */
  def deployableWithinBuildLimits(player: Player, obj: Deployable): Boolean = {
    val zone = obj.Zone
    val channel = player.Name
    val definition = obj.Definition
    val item = definition.Item
    val deployables = player.avatar.deployables
    val (curr, max) = deployables.CountDeployable(item)
    val tryAddToOwnedDeployables = if (!deployables.Available(obj)) {
      val (removed, msg) = {
        if (curr == max) { //too many of a specific type of deployable
          (deployables.DisplaceFirst(obj), max > 1)
        } else if (curr > max) { //somehow we have too many deployables
          (None, true)
        } else { //make room by eliminating a different type of deployable
          (deployables.DisplaceFirst(obj, { d => d.Definition.Item != item }), true)
        }
      }
      removed match {
        case Some(telepad: TelepadDeployable) =>
          //telepad is not explicitly deconstructed
          telepad.Actor ! Deployable.Ownership(None)
          true
        case Some(old) =>
          old.Actor ! Deployable.Deconstruct()
          if (msg) { //max test
            PlayerControl.sendResponse(
              zone,
              channel,
              ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}OldestDestroyed", None)
            )
          }
          true
        case None =>
          org.log4s.getLogger(name = "Deployables").warn(
            s"${player.Name} has no allowance for ${definition.DeployCategory} deployables; is something wrong?"
          )
          PlayerControl.sendResponse(zone, channel, ObjectDeployedMessage.Failure(definition.Name))
          false
      }
    } else if (obj.isInstanceOf[TelepadDeployable]) {
      //always treat the telepad we are putting down as the first and only one
      PlayerControl.sendResponse(zone, channel, ObjectDeployedMessage.Success(definition.Name, count = 1, max = 1))
      true
    } else {
      PlayerControl.sendResponse(zone, channel, ObjectDeployedMessage.Success(definition.Name, curr + 1, max))
      val (catCurr, catMax) = deployables.CountCategory(item)
      if ((max > 1 && curr + 1 == max) || (catMax > 1 && catCurr + 1 == catMax)) {
        PlayerControl.sendResponse(
          zone,
          channel,
          ChatMsg(ChatMessageType.UNK_229, false, "", s"@${definition.Descriptor}LimitReached", None)
        )
      }
      true
    }
    tryAddToOwnedDeployables && gainDeployableOwnership(player, obj, player.avatar.deployables.Add)
  }

  /**
    * Grant ownership over a deployable to a player and calls for an update to the UI for that deployable.
    * Although the formal the ownership change is delayed slightly by messaging protocol,
    * the outcome of this function is reliant more on the function parameter
    * used to append the deployable to the management system of the to-be-owning player.
    * The difference is between technical ownership and indirect knowledge of ownership
    * and how these ownership awareness states operate differently on management of the deployable.
    * @see `Deployable.Ownership`
    * @see `LocalAction.DeployableUIFor`
    * @param player the player who would own the deployable
    * @param obj the deployable
    * @param addFunc the process for assigning management of the deployable to the player
    * @return `true`, if the player was assignment management of the deployable;
    *        `false`, otherwise
    */
  def gainDeployableOwnership(
                               player: Player,
                               obj: Deployable,
                               addFunc: Deployable=>Boolean
                             ): Boolean = {
    if (player.Zone == obj.Zone && addFunc(obj)) {
      obj.Actor ! Deployable.Ownership(player)
      player.Zone.LocalEvents ! LocalServiceMessage(player.Name, LocalAction.DeployableUIFor(obj.Definition.Item))
      true
    } else {
      false
    }
  }

  /**
    * Common actions related to constructing a new `Deployable` object in the game environment.
    * @param zone in which zone these messages apply
    * @param channel to whom to send the messages
    * @param obj the `Deployable` object
    */
  def successfulBuildActivity(zone: Zone, channel: String, obj: Deployable): Unit = {
    //sent to avatar event bus to preempt additional tool management
    buildCooldownReset(zone, channel, obj)
    //sent to local event bus to cooperate with deployable management
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.DeployableUIFor(obj.Definition.Item)
    )
  }

  /**
    * Common actions related to constructing a new `Deployable` object in the game environment.
    * @param zone in which zone these messages apply
    * @param channel to whom to send the messages
    * @param obj the `Deployable` object
    */
  def buildCooldownReset(zone: Zone, channel: String, obj: Deployable): Unit = {
    //sent to avatar event bus to preempt additional tool management
    zone.AvatarEvents ! AvatarServiceMessage(
      channel,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, GenericObjectActionMessage(obj.GUID, 21))
    )
  }

  /**
    * Destroy a `ConstructionItem` object that can be found in the indexed slot.
    * @see `Player.Find`
    * @param tool the `ConstructionItem` object currently in the slot (checked)
    * @param index the slot index
    */
  def commonDestroyConstructionItem(player: Player, tool: ConstructionItem, index: Int): Unit = {
    val zone = player.Zone
    if (safelyRemoveConstructionItemFromSlot(player, tool, index, "CommonDestroyConstructionItem")) {
      TaskWorkflow.execute(GUIDTask.unregisterEquipment(zone.GUID, tool))
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
  def safelyRemoveConstructionItemFromSlot(
                                            player: Player,
                                            tool: ConstructionItem,
                                            index: Int,
                                            logDecorator: String = "SafelyRemoveConstructionItemFromSlot"
                                          ): Boolean = {
    if ({
      val holster = player.Slot(index)
      if (holster.Equipment.contains(tool)) {
        holster.Equipment = None
        true
      } else {
        player.Find(tool) match {
          case Some(newIndex) =>
            log.warn(s"$logDecorator: ${player.Name} was looking for an item in his hand $index, but item was found at $newIndex instead")
            player.Slot(newIndex).Equipment = None
            true
          case None =>
            log.warn(s"$logDecorator: ${player.Name} could not find the target ${tool.Definition.Name}")
            false
        }
      }
    }) {
      val zone = player.Zone
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.ObjectDelete(Service.defaultPlayerGUID, tool.GUID, 0)
      )
      true
    } else {
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
  def findReplacementConstructionItem(player: Player, tool: ConstructionItem, index: Int): Unit = {
    val definition = tool.Definition
    if (player.Slot(index).Equipment.isEmpty) {
      FindEquipmentStock(player, { e => e.Definition == definition }, 1) match {
        case x :: _ =>
          val zone = player.Zone
          val events = zone.AvatarEvents
          val name = player.Name
          val pguid = player.GUID
          val obj  = x.obj.asInstanceOf[ConstructionItem]
          if ((player.Slot(index).Equipment = obj).contains(obj)) {
            val fireMode = tool.FireModeIndex
            val ammoType = tool.AmmoTypeIndex
            player.Inventory -= x.start
            obj.FireModeIndex = fireMode
            //TODO any penalty for being handed an OCM version of the tool?
            events ! AvatarServiceMessage(
              zone.id,
              AvatarAction.EquipmentInHand(Service.defaultPlayerGUID, pguid, index, obj)
            )
            if (obj.AmmoTypeIndex != ammoType) {
              obj.AmmoTypeIndex = ammoType
              events ! AvatarServiceMessage(
                name,
                AvatarAction.SendResponse(Service.defaultPlayerGUID, ChangeAmmoMessage(obj.GUID, ammoType))
              )
            }
            if (player.DrawnSlot == Player.HandsDownSlot) {
              player.DrawnSlot = index
              events ! AvatarServiceMessage(
                name,
                AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectHeldMessage(pguid, index, true))
              )
              events ! AvatarServiceMessage(
                zone.id,
                AvatarAction.ObjectHeld(pguid, index)
              )
            }
          }
        case Nil => ; //no replacements found
      }
    } else {
      log.warn(
        s"FindReplacementConstructionItem: ${player.Name}, your $index hand needs to be empty before a replacement ${definition.Name} can be installed"
      )
    }
  }
}
