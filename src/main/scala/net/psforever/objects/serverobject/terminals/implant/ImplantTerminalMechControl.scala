// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals.implant

import akka.actor.ActorRef
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity, DamageableMountable}
import net.psforever.objects.serverobject.hackable.{GenericHackables, Hackable, HackableBehavior}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity, RepairableEntity}
import net.psforever.objects.serverobject.structures.{Amenity, Building, PoweredAmenityControl}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAwareBehavior}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.objects.{GlobalDefinitions, Player, SimpleItem}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.annotation.unused

object ImplantTerminalMechControl {
  private def FindPairedTerminalInterface(
                                           zone: Zone,
                                           mechGuid: PlanetSideGUID
                                         ): Option[Amenity with Hackable] = {
    zone
      .map
      .terminalToInterface
      .find { case (guid, _) => guid == mechGuid.guid }
      .flatMap { case (_, interfaceGuid) => zone.GUID(interfaceGuid) }
      .collect { case terminal: Terminal if !terminal.Destroyed && terminal.HackedBy.isEmpty => terminal }
  }
}

/**
 * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
 * @param mech the "mech" object being governed
 */
class ImplantTerminalMechControl(mech: ImplantTerminalMech)
  extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with MountableBehavior
    with HackableBehavior.GenericHackable
    with DamageableEntity
    with RepairableEntity
    with AmenityAutoRepair
    with CaptureTerminalAwareBehavior {
  def MountableObject: ImplantTerminalMech            = mech
  def HackableObject: ImplantTerminalMech             = mech
  def FactionObject: ImplantTerminalMech              = mech
  def DamageableObject: ImplantTerminalMech           = mech
  def RepairableObject: ImplantTerminalMech           = mech
  def AutoRepairObject: ImplantTerminalMech           = mech
  def CaptureTerminalAwareObject: ImplantTerminalMech = mech

  def commonBehavior: Receive =
    checkBehavior
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)
      .orElse(captureTerminalAwareBehaviour)

  def poweredStateLogic : Receive =
    commonBehavior
      .orElse(hackableBehavior)
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
            case _ => ()
          }
        case _ => ()
      }

  def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse(hackableBehavior)
      .orElse {
        case _ => ()
      }

  override protected def mountTest(
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
          super.mountTest(obj, seatNumber, player)
      case None =>
        false
    }
  }

  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any): Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
    val damageTo = amount match {
      case a: Int => a
      case _ => 0
    }
    DamageableMountable.DamageAwareness(DamageableObject, cause, damageTo)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    DamageableMountable.DestructionAwareness(DamageableObject, cause)
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
      seat.occupant.collect {
        case player =>
          seat.unmount(player)
          player.VehicleSeated = None
          events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, unk2=false, guid))
      }
    )
  }

  def powerTurnOnCallback(): Unit = {
    tryAutoRepair()
  }

  override def Restoration(obj: Target): Unit = {
    super.Restoration(obj)
    RepairableAmenity.RestorationOfHistory(obj)
  }

  override def performHack(player: Player, data: Option[Any], replyTo: ActorRef): Unit = {
    //todo don't now how to properly hack this amenity
    super.performHack(player, data, replyTo)
    val zone = HackableObject.Zone
    val guid = HackableObject.GUID
    val localFaction = mech.Faction
    val events = zone.LocalEvents
    if (player.Faction == localFaction) {
      if (mech.Owner.asInstanceOf[Building].CaptureTerminalIsHacked) {
        //this is actually futile, as a hacked base does not grant access to the terminal
        events ! LocalServiceMessage(localFaction.toString, LocalAction.SetEmpire(guid, localFaction))
      }
      kickAllOccupantsNotOfFaction(zone, guid, mech, localFaction)
    } else {
      opposingFactionsMayAccess(zone, guid, localFaction)
      kickAllOccupantsOfFaction(zone, guid, mech, localFaction)
    }
    ImplantTerminalMechControl
      .FindPairedTerminalInterface(zone, guid)
      .foreach(GenericHackables.FinishHacking(_, player, unk = 3212836864L)())
  }

  override def performClearHack(data: Option[Any], replyTo: ActorRef): Unit = {
    //todo don't now how to properly unhack this amenity
    HackableObject.HackedBy.collect { _ =>
      super.performClearHack(data, replyTo)
      val toFaction = HackableObject.Faction
      val zone = HackableObject.Zone
      val guid = HackableObject.GUID
      noAccessByOpposingFactions(zone, guid, toFaction)
      kickAllOccupantsNotOfFaction(zone, guid, mech, toFaction)
    }
  }

  override protected def captureTerminalIsHacked(@unused terminal: CaptureTerminal): Unit = {
    //todo don't now how to properly handle a hacked mech
    super.captureTerminalIsHacked(terminal)
    val zone = HackableObject.Zone
    val guid = HackableObject.GUID
    kickAllOccupantsNotOfFactionWithTest(zone, guid, mech, (a: PlanetSideEmpire.Value) => { true })
  }

  override protected def captureTerminalIsResecured(terminal: CaptureTerminal): Unit = {
    //todo don't now how to properly handle a hacked mech
    super.captureTerminalIsResecured(terminal)
    //if hacked, correct
    val zone = HackableObject.Zone
    val guid = HackableObject.GUID
    val toFaction = HackableObject.Faction
    HackableObject.HackedBy.collect {
      case hackInfo if hackInfo.hackerFaction != toFaction =>
        opposingFactionsMayAccess(zone, guid, toFaction)
    }
  }

  private def opposingFactionsMayAccess(
                                         zone: Zone,
                                         guid: PlanetSideGUID,
                                         setToFaction: PlanetSideEmpire.Value
                                       ): Unit = {
    val events = zone.LocalEvents
    opposingFactionsAre(setToFaction).foreach { faction =>
      events ! LocalServiceMessage(faction.toString, LocalAction.SetEmpire(guid, faction))
    }
  }

  private def noAccessByOpposingFactions(
                                          zone: Zone,
                                          guid: PlanetSideGUID,
                                          setToFaction: PlanetSideEmpire.Value
                                        ): Unit = {
    val events = zone.LocalEvents
    opposingFactionsAre(setToFaction).foreach { faction =>
      events ! LocalServiceMessage(faction.toString, LocalAction.SetEmpire(guid, setToFaction))
    }
  }

  private def opposingFactionsAre(faction: PlanetSideEmpire.Value): PlanetSideEmpire.ValueSet = {
    PlanetSideEmpire
      .values
      .filterNot { f => f == PlanetSideEmpire.NEUTRAL && f == faction }
  }

  private def kickAllOccupantsOfFaction(
                                         zone: Zone,
                                         guid: PlanetSideGUID,
                                         obj: Mountable,
                                         isFaction: PlanetSideEmpire.Value
                                       ): Unit = {
    kickAllOccupantsNotOfFactionWithTest(zone, guid, obj, (a: PlanetSideEmpire.Value) => { a == isFaction })
  }

  private def kickAllOccupantsNotOfFaction(
                                            zone: Zone,
                                            guid: PlanetSideGUID,
                                            obj: Mountable,
                                            isFaction: PlanetSideEmpire.Value
                                          ): Unit = {
    kickAllOccupantsNotOfFactionWithTest(zone, guid, obj, (a: PlanetSideEmpire.Value) => { a != isFaction })
  }

  private def kickAllOccupantsNotOfFactionWithTest(
                                                    zone: Zone,
                                                    guid: PlanetSideGUID,
                                                    obj: Mountable,
                                                    test: PlanetSideEmpire.Value => Boolean
                                                  ): Unit = {
    val zoneId = zone.id
    val events = zone.LocalEvents
    obj.Seats.values.foreach(seat =>
      seat.occupant.collect {
        case player if test(player.Faction) =>
          seat.unmount(player)
          player.VehicleSeated = None
          events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, unk2 = false, guid))
      }
    )
  }
}
