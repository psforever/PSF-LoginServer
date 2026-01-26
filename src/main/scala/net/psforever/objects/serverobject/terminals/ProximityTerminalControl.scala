// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{ActorRef, Cancellable}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.sourcing.AmenitySource
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.HackState1
import net.psforever.services.local.ClearMessage
import net.psforever.services.local.support.HackClearActor
import org.log4s.Logger

import scala.annotation.unused
import scala.collection.mutable
import scala.concurrent.duration._
//
import net.psforever.objects._
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity}
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.vital.{HealFromTerminal, RepairFromTerminal, Vitality}
import net.psforever.objects.zones.ZoneAware
import net.psforever.packet.game.InventoryStateMessage
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

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
  def FactionObject: Terminal with ProximityUnit    = term
  def HackableObject: Terminal with ProximityUnit   = term
  def TerminalObject: Terminal with ProximityUnit   = term
  def DamageableObject: Terminal with ProximityUnit = term
  def RepairableObject: Terminal with ProximityUnit = term
  def AutoRepairObject: Terminal with ProximityUnit = term

  var terminalAction: Cancellable             = Default.Cancellable
  val callbacks: mutable.ListBuffer[ActorRef] = new mutable.ListBuffer[ActorRef]()
  val log: Logger                             = org.log4s.getLogger

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
                GenericHackables.FinishHacking(term, player, hackValue = -1, hackClearValue = -1),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID)
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
    .orElse(clearHackBehavior)
    .orElse {
      case CommonMessages.Use(_, _) =>
        log.warn(s"unexpected format for CommonMessages.Use in this context")

      case CommonMessages.Unuse(_, Some(target: PlanetSideGameObject)) =>
        Unuse(target, term.Continent)

      case CommonMessages.Unuse(_, _) =>
        log.warn(s"unexpected format for CommonMessages.Unuse in this context")
      case _ => ;
    }

  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any) : Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult) : Unit = {
    tryAutoRepair()
    if (term.HackedBy.nonEmpty) {
      val zone = term.Zone
      zone.LocalEvents ! ClearMessage(HackClearActor.ObjectIsResecured(term))
    }
    super.DestructionAwareness(target, cause)
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

  def Use(target: PlanetSideGameObject, @unused zone: String, callback: ActorRef): Unit = {
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
        val zone = TerminalObject.Zone
        zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ProximityTerminalEffect(TerminalObject.GUID, effectState = true))
      }
    } else {
      log.warn(s"ProximityTerminal.Use: $target was rejected by unit ${term.Definition.Name}@${term.GUID.guid}")
    }
  }

  def Unuse(target: PlanetSideGameObject, @unused zone: String): Unit = {
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
        val zone = TerminalObject.Zone
        zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ProximityTerminalEffect(TerminalObject.GUID, effectState = false))
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
      val zone = TerminalObject.Zone
      zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ProximityTerminalEffect(TerminalObject.GUID, effectState = true))
    }
    //clear hack state
    if (term.HackedBy.nonEmpty) {
      val zone = term.Zone
      zone.LocalEvents ! ClearMessage(HackClearActor.ObjectIsResecured(term))
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
                                         @unused callback: ActorRef,
                                         terminal: Terminal with ProximityUnit,
                                         target: PlanetSideGameObject
                                       ): Boolean = {
    (terminal.Definition, target) match {
      case (_: MedicalTerminalDefinition, p: Player)
        if terminal.Definition ==
          GlobalDefinitions.medical_terminal_healing_module  => HealthModule(terminal, p)
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
    val medDef = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val fullHeal = HealAction(unit, target, medDef.HealAmount, PlayerHealthCallback)
    val fullRepair = ArmorRepairAction(unit, target, medDef.ArmorAmount)
    fullHeal && fullRepair
  }

  /**
    * Activated by a facility having a linked cavern lock or health module installed. Friendly players
    * within the SOI receive constant healing as requested by the client
    */
  def HealthModule(unit: Terminal with ProximityUnit, target: Player): Boolean = {
    val medDef = unit.Definition.asInstanceOf[MedicalTerminalDefinition]
    val fullHeal = HealthModuleAction(unit, target, medDef.HealAmount, PlayerHealthCallback)
    fullHeal
  }

  /**
   * When driving a vehicle close to a rearm/repair silo,
   * restore the vehicle's health points.
   * If the vehicle is fully repaired, stop using the terminal.
   * @param unit the terminal
   * @param target the vehicle being repaired
   */
  def VehicleRepairTerminal(unit: Terminal with ProximityUnit, target: Vehicle): Boolean = {
    unit.Definition match {
      case medDef: MedicalTerminalDefinition if !target.Destroyed && unit.Validate(target) =>
        HealAction(unit, target, medDef.HealAmount, VehicleHealthCallback)
      case _ =>
        true
    }
  }

  /**
   * Restore, at most, a specific amount of health points on a player.
   * Send messages to connected client and to events system.
   * @param terminal na
   * @param target that which will accept the health
   * @param healAmount health value to be given to the target
   * @param updateFunc callback to update the UI
   * @return whether the target can be healed any further
   */
  def HealAction(
                  terminal: Terminal,
                  target: PlanetSideGameObject with Vitality with ZoneAware,
                  healAmount: Int,
                  updateFunc: PlanetSideGameObject with Vitality with ZoneAware=>Unit
                ): Boolean = {
    val health = target.Health
    val maxHealth = target.MaxHealth
    val nextHealth = health + healAmount
    if (healAmount != 0 && health < maxHealth) {
      val finalHealthAmount = if (nextHealth > maxHealth) {
        nextHealth - maxHealth
      } else {
        healAmount
      }
      target.Health = health + finalHealthAmount
      target.LogActivity(HealFromTerminal(AmenitySource(terminal), finalHealthAmount))
      updateFunc(target)
      target.Health == maxHealth
    } else {
      true
    }
  }

  /**
    * Heals players and increases their health/max health up to 120 if they enter the SOI this benefit is active in.
    */
  def HealthModuleAction(
                          terminal: Terminal,
                          target: PlanetSideGameObject with Vitality with ZoneAware,
                          healAmount: Int,
                          updateFunc: PlanetSideGameObject with Vitality with ZoneAware => Unit
                        ): Boolean = {
    val maxHealthCap = 120
    val zone = target.Zone
    val oldMax = target.MaxHealth
    val newMax = math.min(oldMax + healAmount, maxHealthCap)

    if (oldMax < maxHealthCap) {
      target.MaxHealth = newMax
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        target.GUID,
        AvatarAction.PlanetsideAttributeToAll(1, newMax)
      )
    }
    if (target.Health < newMax) {
      target.Health = math.min(target.Health + healAmount, newMax)
      target.LogActivity(HealFromTerminal(AmenitySource(terminal), 1))
      updateFunc(target)
    }
    target.Health == newMax
  }

  def PlayerHealthCallback(target: PlanetSideGameObject with Vitality with ZoneAware): Unit = {
    val zone = target.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      target.GUID,
      AvatarAction.PlanetsideAttributeToAll(0, target.Health)
    )
  }

  def VehicleHealthCallback(target: PlanetSideGameObject with Vitality with ZoneAware): Unit = {
    val zone = target.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 0, target.Health)
    )
  }

  /**
   * Restore, at most, a specific amount of personal armor points on a player.
   * Send messages to connected client and to events system.
   * @param terminal na
   * @param target that which will accept the repair
   * @param repairAmount armor value to be given to the target
   * @return whether the target can be repaired any further
   */
  def ArmorRepairAction(
                         terminal: Terminal,
                         target: Player,
                         repairAmount: Int
                       ): Boolean = {
    val armor = target.Armor
    val maxArmor = target.MaxArmor
    val nextArmor = armor + repairAmount
    if (repairAmount != 0 && armor < maxArmor) {
      val finalRepairAmount = if (nextArmor > maxArmor) {
        nextArmor - maxArmor
      } else {
        repairAmount
      }
      target.Armor = armor + finalRepairAmount
      target.LogActivity(RepairFromTerminal(AmenitySource(terminal), finalRepairAmount))
      val zone = target.Zone
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        target.GUID,
        AvatarAction.PlanetsideAttributeToAll(4, target.Armor)
      )
      target.Armor == maxArmor
    } else {
      true
    }
  }

  /**
   * When standing in a friendly SOI whose facility is under the influence of an Ancient Weapon Module benefit,
   * and the player is in possession of Ancient weaponry whose magazine is not full,
   * restore some ammunition to its magazine.
   * If no valid weapons are discovered or the discovered valid weapons have full magazines, stop using the terminal.
   * @param unit the terminal
   * @param target the player with weapons being recharged
   */
  def WeaponRechargeTerminal(unit: Terminal with ProximityUnit, target: Player): Boolean = {
    val result = WeaponsBeingRechargedWithSomeAmmunition(
      unit.Definition.asInstanceOf[WeaponRechargeTerminalDefinition].AmmoAmount,
      target.Holsters().flatMap { _.Equipment }.toSeq ++ target.Inventory.Items.map { _.obj }
    )
    val ancient = result.filter(rechargeMe => rechargeMe._1.Definition == GlobalDefinitions.maelstrom ||
      rechargeMe._1.Definition == GlobalDefinitions.spiker || rechargeMe._1.Definition == GlobalDefinitions.radiator)
    val events = unit.Zone.AvatarEvents
    val channel = target.Name
    ancient.foreach { case (weapon, slots) =>
      slots.foreach { slot =>
        events ! AvatarServiceMessage(
          channel,
          AvatarAction.SendResponse(InventoryStateMessage(slot.Box.GUID, weapon.GUID, slot.Box.Capacity))
        )
      }
    }
    !result.flatMap { _._2 }.exists { slot => slot.Magazine < slot.MaxMagazine() }
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
    !result.flatMap { _._2 }.exists { slot => slot.Magazine < slot.MaxMagazine() }
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
