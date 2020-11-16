// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.{GlobalDefinitions, Player, SimpleItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity, DamageableMountable}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableEntity}
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech: ImplantTerminalMech)
    extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with MountableBehavior.Mount
    with MountableBehavior.Dismount
    with HackableBehavior.GenericHackable
    with DamageableEntity
    with RepairableEntity
    with AmenityAutoRepair {
  def MountableObject  = mech
  def HackableObject   = mech
  def FactionObject    = mech
  def DamageableObject = mech
  def RepairableObject = mech
  def AutoRepairObject = mech

  def commonBehavior: Receive =
    checkBehavior
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)

  def poweredStateLogic : Receive =
    commonBehavior
      .orElse(mountBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
          if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          //TODO setup certifications check
          mech.Owner match {
            case b: Building if (b.Faction != player.Faction || b.CaptureTerminalIsHacked) && mech.HackedBy.isEmpty =>
              sender() ! CommonMessages.Progress(
                GenericHackables.GetHackSpeed(player, mech),
                GenericHackables.FinishHacking(mech, player, 3212836864L),
                GenericHackables.HackingTickAction(progressType = 1, player, mech, item.GUID)
              )
            case _ => ;
          }
        case _ => ;
      }

  def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ;
      }

  override protected def MountTest(
      obj: PlanetSideServerObject with Mountable,
      seatNumber: Int,
      player: Player
  ): Boolean = {
    val zone = obj.Zone
    zone.map.terminalToInterface.get(obj.GUID.guid) match {
      case Some(interface_guid) =>
        (zone.GUID(interface_guid) match {
          case Some(interface) => !interface.Destroyed
          case None            => false
        }) &&
          super.MountTest(obj, seatNumber, player)
      case None =>
        false
    }
  }

  override protected def DamageAwareness(target: Target, cause: ResolvedProjectile, amount: Any): Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
    val damageTo = amount match {
      case a: Int => a
      case _ => 0
    }
    DamageableMountable.DamageAwareness(DamageableObject, cause, damageTo)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    DamageableMountable.DestructionAwareness(DamageableObject, cause)
    target.ClearHistory()
  }

  override def PerformRepairs(target : Damageable.Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def powerTurnOffCallback(): Unit = {
    stopAutoRepair()
    //kick all occupants
    val guid = mech.GUID
    val zone = mech.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    mech.Seats.values.foreach(seat =>
      seat.Occupant match {
        case Some(player) =>
          seat.Occupant = None
          player.VehicleSeated = None
          if (player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, false, guid))
          }
        case None => ;
      }
    )
  }

  def powerTurnOnCallback(): Unit = {
    tryAutoRepair()
  }
}
