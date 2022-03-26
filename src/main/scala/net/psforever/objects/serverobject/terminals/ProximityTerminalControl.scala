// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{ActorRef, Cancellable}
import net.psforever.objects._
import net.psforever.objects.ballistics.{PlayerSource, VehicleSource}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity}
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.vital.{HealFromTerm, RepairFromTerm}
import net.psforever.packet.game.InventoryStateMessage
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `ProximityTerminal`.
  * Although this "terminal" itself does not accept the same messages as a normal `Terminal` object,
  * it returns the same type of messages - wrapped in a `TerminalMessage` - to the `sender`.
  * @param term the proximity unit (terminal)
  */
class ProximityTerminalControl(term: Terminal with ProximityUnit)
    extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable
    with DamageableAmenity
    with RepairableAmenity
    with AmenityAutoRepair {
  def FactionObject    = term
  def HackableObject   = term
  def TerminalObject   = term
  def DamageableObject = term
  def RepairableObject = term
  def AutoRepairObject = term

  var terminalAction: Cancellable             = Default.Cancellable
  val callbacks: mutable.ListBuffer[ActorRef] = new mutable.ListBuffer[ActorRef]()
  val log                                     = org.log4s.getLogger

  val commonBehavior: Receive = checkBehavior
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse(autoRepairBehavior)
    .orElse {
      case CommonMessages.Unuse(_, Some(target: PlanetSideGameObject)) =>
        Unuse(target, term.Continent)

      case CommonMessages.Unuse(_, _) =>
        log.warn(s"unexpected format for CommonMessages.Unuse in this context")
    }

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse(hackableBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          //TODO setup certifications check
          term.Owner match {
            case b: Building if (b.Faction != player.Faction || b.CaptureTerminalIsHacked) && term.HackedBy.isEmpty =>
              sender() ! CommonMessages.Progress(
                GenericHackables.GetHackSpeed(player, term),
                GenericHackables.FinishHacking(term, player, 3212836864L),
                GenericHackables.HackingTickAction(progressType = 1, player, term, item.GUID)
              )
            case _ => ;
          }

        case CommonMessages.Use(_, Some(target: PlanetSideGameObject)) =>
          if (!term.Destroyed && term.Definition.asInstanceOf[ProximityDefinition].Validations.exists(p => p(target))) {
            Use(target, term.Continent, sender())
          }
        case CommonMessages.Use(_, Some((target: PlanetSideGameObject, callback: ActorRef))) =>
          if (!term.Destroyed && term.Definition.asInstanceOf[ProximityDefinition].Validations.exists(p => p(target))) {
            Use(target, term.Continent, callback)
          }

        case CommonMessages.Use(_, _) =>
          log.warn(s"unexpected format for CommonMessages.Use in this context")

        case ProximityTerminalControl.TerminalAction() =>
          val proxDef = term.Definition.asInstanceOf[ProximityDefinition]
          val validateFunc: PlanetSideGameObject => Boolean =
            term.Validate(proxDef.UseRadius * proxDef.UseRadius, proxDef.Validations)
          val callbackList = callbacks.toList
          term.Targets.zipWithIndex.foreach({
            case (target, index) =>
              if (!term.Destroyed && validateFunc(target)) {
                callbackList.lift(index) match {
                  case Some(cback) =>
                    cback ! ProximityUnit.Action(term, target)
                    if (ProximityTerminalControl.selectAndTryProximityUnitBehavior(cback, term, target)) {
                      Unuse(target, term.Zone.id)
                    }
                  case None =>
                    log.error(
                      s"improper callback registered for $target on $term in zone ${term.Owner.Continent}; this may be recoverable"
                    )
                }
              } else {
                Unuse(target, term.Continent)
              }
          })

        case ProximityUnit.Action(_, _) =>
        //reserved

        case _ =>
      }

  def unpoweredStateLogic : Receive = commonBehavior
    .orElse {
      case CommonMessages.Use(_, _) =>
        log.warn(s"unexpected format for CommonMessages.Use in this context")

      case CommonMessages.Unuse(_, Some(target: PlanetSideGameObject)) =>
        Unuse(target, term.Continent)

      case CommonMessages.Unuse(_, _) =>
        log.warn(s"unexpected format for CommonMessages.Unuse in this context")
      case _ => ;
    }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def Use(target: PlanetSideGameObject, zone: String, callback: ActorRef): Unit = {
    val hadNoUsers = term.NumberUsers == 0
    if (term.AddUser(target)) {
      log.trace(s"ProximityTerminal.Use: unit ${term.Definition.Name}@${term.GUID.guid} will act on $target")
      //add callback
      callbacks += callback
      //activation
      if (term.NumberUsers == 1 && hadNoUsers) {
        val tdef = term.Definition.asInstanceOf[ProximityDefinition]
        import scala.concurrent.ExecutionContext.Implicits.global
        terminalAction.cancel()
        terminalAction = context.system.scheduler.scheduleWithFixedDelay(
          500 milliseconds,
          tdef.Interval,
          self,
          ProximityTerminalControl.TerminalAction()
        )
        TerminalObject.Zone.LocalEvents ! Terminal.StartProximityEffect(term)
      }
    } else {
      log.warn(s"ProximityTerminal.Use: $target was rejected by unit ${term.Definition.Name}@${term.GUID.guid}")
    }
  }

  def Unuse(target: PlanetSideGameObject, zone: String): Unit = {
    val whereTarget   = term.Targets.indexWhere(_ eq target)
    val previousUsers = term.NumberUsers
    val hadUsers      = previousUsers > 0
    if (whereTarget > -1 && term.RemoveUser(target)) {
      log.trace(
        s"ProximityTerminal.Unuse: unit ${term.Definition.Name}@${term.GUID.guid} will cease operation on $target"
      )
      //remove callback
      callbacks.remove(whereTarget) ! ProximityUnit.StopAction(term, target)
      //de-activation (global / local)
      if (term.NumberUsers == 0 && hadUsers) {
        terminalAction.cancel()
        TerminalObject.Zone.LocalEvents ! Terminal.StopProximityEffect(term)
      }
    } else {
      log.debug(
        s"ProximityTerminal.Unuse: target by proximity $target is not known to $term, though the unit tried to 'Unuse' it"
      )
    }
  }

  def powerTurnOffCallback() : Unit = {
    stopAutoRepair()
    //clear effect callbacks
    terminalAction.cancel()
    if (callbacks.nonEmpty) {
      callbacks.clear()
      TerminalObject.Zone.LocalEvents ! Terminal.StopProximityEffect(term)
    }
    //clear hack state
    if (term.HackedBy.nonEmpty) {
      val zone = term.Zone
      zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ClearTemporaryHack(Service.defaultPlayerGUID, term))
    }
  }

  def powerTurnOnCallback() : Unit = {
    tryAutoRepair()
  }

  override def toString: String = term.Definition.Name
}

object ProximityTerminalControl {
  private case class TerminalAction()

  /**
    * Determine which functionality to pursue by a generic proximity-functional unit given the target for its activity.
    * @see `VehicleService:receive, ProximityUnit.Action`
    * @param terminal the proximity-based unit
    * @param target the object being affected by the unit
    */
  def selectAndTryProximityUnitBehavior(
                                   callback: ActorRef,
                                   terminal: Terminal with ProximityUnit,
                                   target: PlanetSideGameObject
                                 ): Boolean = {
    (terminal.Definition, target) match {
      case (_: MedicalTerminalDefinition, p: Player)         => HealthAndArmorTerminal(terminal, p)
      case (_: WeaponRechargeTerminalDefinition, p: Player)  => WeaponRechargeTerminal(terminal, p)
      case (_: MedicalTerminalDefinition, v: Vehicle)        => VehicleRepairTerminal(terminal, v)
      case (_: WeaponRechargeTerminalDefinition, v: Vehicle) => WeaponRechargeTerminal(terminal, v)
      case _                                                 => false
    }
  }

  /**
    * When standing on the platform of a(n advanced) medical terminal,
    * restore the player's health and armor points (when they need their health and armor points restored).
    * If the player is both fully healed and fully repaired, stop using the terminal.
    * @param unit the medical terminal
    * @param target the player being healed
    */
  def HealthAndArmorTerminal(unit: Terminal with ProximityUnit, target: Player): Boolean = {
    val medDef     = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val healAmount = medDef.HealAmount
    val healthFull: Boolean = if (healAmount != 0 && target.Health < target.MaxHealth) {
      target.History(HealFromTerm(PlayerSource(target), healAmount, 0, medDef))
      HealAction(target, healAmount)
    } else {
      true
    }
    val repairAmount = medDef.ArmorAmount
    val armorFull: Boolean = if (repairAmount != 0 && target.Armor < target.MaxArmor) {
      target.History(HealFromTerm(PlayerSource(target), 0, repairAmount, medDef))
      ArmorRepairAction(target, repairAmount)
    } else {
      true
    }
    healthFull && armorFull
  }

  /**
    * Restore, at most, a specific amount of health points on a player.
    * Send messages to connected client and to events system.
    * @param tplayer the player
    * @param healValue the amount to heal;
    *                    10 by default
    * @return whether the player can be repaired for any more health points
    */
  def HealAction(tplayer: Player, healValue: Int = 10): Boolean = {
    tplayer.Health = tplayer.Health + healValue
    val zone = tplayer.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.PlanetsideAttributeToAll(tplayer.GUID, 0, tplayer.Health)
    )
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
  def ArmorRepairAction(tplayer: Player, repairValue: Int = 10): Boolean = {
    tplayer.Armor = tplayer.Armor + repairValue
    val zone = tplayer.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.PlanetsideAttributeToAll(tplayer.GUID, 4, tplayer.Armor)
    )
    tplayer.Armor == tplayer.MaxArmor
  }

  /**
    * When driving a vehicle close to a rearm/repair silo,
    * restore the vehicle's health points.
    * If the vehicle is fully repaired, stop using the terminal.
    * @param unit the terminal
    * @param target the vehicle being repaired
    */
  def VehicleRepairTerminal(unit: Terminal with ProximityUnit, target: Vehicle): Boolean = {
    val medDef     = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val healAmount = medDef.HealAmount
    val maxHealth = target.MaxHealth
    val noMoreHeal = if (!target.Destroyed && unit.Validate(target)) {
      //repair vehicle
      if (healAmount > 0 && target.Health < maxHealth) {
        target.Health = target.Health + healAmount
        target.History(RepairFromTerm(VehicleSource(target), healAmount, medDef))
        val zone = target.Zone
        zone.VehicleEvents ! VehicleServiceMessage(
          zone.id,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 0, target.Health)
        )
        target.Health == maxHealth
      } else {
        true
      }
    } else {
      true
    }
    noMoreHeal
  }

  /**
    * When standing in a friendly SOI whose facility is under the influence of an Ancient Weapon Module benefit,
    * and the player is in possession of Ancient weaponnry whose magazine is not full,
    * restore some ammunition to its magazine.
    * If no valid weapons are discovered or the discovered valid weapons have full magazines, stop using the terminal.
    * @param unit the terminal
    * @param target the player with weapons being recharged
    */
  def WeaponRechargeTerminal(unit: Terminal with ProximityUnit, target: Player): Boolean = {
    val result = WeaponsBeingRechargedWithSomeAmmunition(
      unit.Definition.asInstanceOf[WeaponRechargeTerminalDefinition].AmmoAmount,
      target.Holsters().map { _.Equipment }.flatten.toIterable ++ target.Inventory.Items.map { _.obj }
    )
    val events = unit.Zone.AvatarEvents
    val channel = target.Name
    result.foreach { case (weapon, slots) =>
      slots.foreach { slot =>
        events ! AvatarServiceMessage(
          channel,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(slot.Box.GUID, weapon.GUID, slot.Box.Capacity))
        )
      }
    }
    !result.unzip._2.flatten.exists { slot => slot.Magazine < slot.MaxMagazine() }
  }

  /**
    * When driving close to a rearm/repair silo whose facility is under the influence of an Ancient Weapon Module benefit,
    * and the vehicle is an Ancient vehicle with mounted weaponry whose magazine(s) is not full,
    * restore some ammunition to the magazine(s).
    * If no valid weapons are discovered or the discovered valid weapons have full magazines, stop using the terminal.
    * @param unit the terminal
    * @param target the vehicle with weapons being recharged
    */
  def WeaponRechargeTerminal(unit: Terminal with ProximityUnit, target: Vehicle): Boolean = {
    val result = WeaponsBeingRechargedWithSomeAmmunition(
      unit.Definition.asInstanceOf[WeaponRechargeTerminalDefinition].AmmoAmount,
      target.Weapons.values.collect { case e if e.Equipment.nonEmpty => e.Equipment.get }
    )
    val events = unit.Zone.VehicleEvents
    val channel = target.Actor.toString
    result.foreach { case (weapon, slots) =>
      slots.foreach { slot =>
        events ! VehicleServiceMessage(
          channel,
          VehicleAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(slot.Box.GUID, weapon.GUID, slot.Box.Capacity))
        )
      }
    }
    !result.unzip._2.flatten.exists { slot => slot.Magazine < slot.MaxMagazine() }
  }

  /**
    * Collect all weapons with magazines that need to have ammunition reloaded,
    * and reload some ammunition into them.
    * @param ammoAdded the amount of ammo to be added to a weapon
    * @param equipment the equipment being considered;
    *                  weapons whose ammo will be increased will be isolated
    * @return na
    */
  def WeaponsBeingRechargedWithSomeAmmunition(
                                               ammoAdded: Int,
                                               equipment: Iterable[Equipment]
                                             ): Iterable[(Tool, Iterable[Tool.FireModeSlot])] = {
    equipment
      .collect {
        case weapon: Tool
          if weapon.AmmoSlots.exists(slot => slot.Box.Capacity < slot.Definition.Magazine) =>
          (weapon, WeaponAmmoRecharge(ammoAdded, weapon.AmmoSlots))
      }
  }

  /**
    * Collect all magazines from this weapon that need to have ammunition reloaded,
    * and reload some ammunition into them.
    * @param ammoAdded the amount of ammo to be added to a weapon
    * @param slots the vehicle with weapons being recharged
    * @return ammunition slots that were affected
    */
  def WeaponAmmoRecharge(
                          ammoAdded: Int,
                          slots: List[Tool.FireModeSlot]
                        ): List[Tool.FireModeSlot] = {
    val unfilledSlots = slots.filter { slot => slot.Magazine < slot.MaxMagazine() }
    if (unfilledSlots.nonEmpty) {
      unfilledSlots.foreach { slot => slot.Box.Capacity = slot.Box.Capacity + ammoAdded }
      unfilledSlots
    } else {
      Nil
    }
  }
}
