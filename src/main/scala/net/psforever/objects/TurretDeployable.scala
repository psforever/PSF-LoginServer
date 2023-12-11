// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployedItem}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import net.psforever.objects.serverobject.turret.{AutomatedTurret, AutomatedTurretBehavior, TurretDefinition, WeaponTurret}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.{SimpleResolutions, StandardVehicleResistance, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration.FiniteDuration

class TurretDeployable(tdef: TurretDeployableDefinition)
    extends Deployable(tdef)
    with WeaponTurret
    with JammableUnit
    with Hackable
    with AutomatedTurret {
  WeaponTurret.LoadDefinition(turret = this)

  override def Definition: TurretDeployableDefinition = tdef
}

class TurretDeployableDefinition(private val objectId: Int)
    extends DeployableDefinition(objectId)
    with TurretDefinition {
  Name = "turret_deployable"
  Packet = new SmallTurretConverter
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardVehicleResistance
  Model = SimpleResolutions.calculate

  //override to clarify inheritance conflict
  override def MaxHealth: Int = super[DeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(max: Int): Int = super[DeployableDefinition].MaxHealth_=(max)

  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object TurretDeployableDefinition {
  def apply(dtype: DeployedItem.Value): TurretDeployableDefinition = {
    new TurretDeployableDefinition(dtype.id)
  }
}

/** control actors */

class TurretControl(turret: TurretDeployable)
    extends Actor
    with DeployableBehavior
    with FactionAffinityBehavior.Check
    with JammableMountedWeapons //note: jammable status is reported as vehicle events, not local events
    with MountableBehavior
    with DamageableWeaponTurret
    with RepairableWeaponTurret
    with AutomatedTurretBehavior {
  def DeployableObject: TurretDeployable      = turret
  def MountableObject: TurretDeployable       = turret
  def JammableObject: TurretDeployable        = turret
  def FactionObject: TurretDeployable         = turret
  def DamageableObject: TurretDeployable      = turret
  def RepairableObject: TurretDeployable      = turret
  def AutomatedTurretObject: TurretDeployable = turret

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
    damageableWeaponTurretPostStop()
    automaticTurretPostStop()
  }

  def receive: Receive =
    deployableBehavior
      .orElse(checkBehavior)
      .orElse(jammableBehavior)
      .orElse(mountBehavior)
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(automatedTurretBehavior)
      .orElse {
        case _ => ()
      }

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    player: Player): Boolean = {
    (!turret.Definition.FactionLocked || player.Faction == obj.Faction) && !obj.Destroyed
  }

  override def TryJammerEffectActivate(target: Any, cause: DamageResult): Unit = {
    val startsUnjammed = !JammableObject.Jammed
    super.TryJammerEffectActivate(target, cause)
    if (startsUnjammed && JammableObject.Jammed) {
      AutomaticOperation = false
      //look in direction of source of jamming
      val zone = JammableObject.Zone
      TurretControl.getAttackerFromCause(zone, cause).foreach {
        attacker =>
          val channel = zone.id
          val guid = AutomatedTurretObject.GUID
          AutomatedTurretBehavior.startTrackingTargets(zone, channel, guid, List(attacker.GUID))
          AutomatedTurretBehavior.stopTrackingTargets(zone, channel, guid) //TODO delay by a few milliseconds?
      }
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    val startsJammed = JammableObject.Jammed
    super.CancelJammeredStatus(target)
    startsJammed && AutomaticOperation_=(state = true)
  }

  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any): Unit = {
    //turret retribution
    if (AutomaticOperation) {
      TurretControl.getAttackerFromCause(target.Zone, cause).foreach {
        attacker =>
          engageNewDetectedTarget(attacker)
        }
    }
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    AutomaticOperation = false
    super.DestructionAwareness(target, cause)
    CancelJammeredSound(target)
    CancelJammeredStatus(target)
    Deployables.AnnounceDestroyDeployable(turret, None)
  }

  override def deconstructDeployable(time: Option[FiniteDuration]) : Unit = {
    AutomaticOperation = false
    val zone = turret.Zone
    val seats = turret.Seats.values
    //either we have no seats or no one gets to sit
    val retime = if (seats.count(_.isOccupied) > 0) {
      //unlike with vehicles, it's possible to request deconstruction of one's own field turret while seated in it
      val wasKickedByDriver = false
      seats.foreach { seat =>
        seat.occupant match {
          case Some(tplayer) =>
            seat.unmount(tplayer)
            tplayer.VehicleSeated = None
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.id,
              VehicleAction.KickPassenger(tplayer.GUID, 4, wasKickedByDriver, turret.GUID)
            )
          case None => ()
        }
      }
      Some(time.getOrElse(Deployable.cleanup) + Deployable.cleanup)
    } else {
      time
    }
    super.deconstructDeployable(retime)
  }

  override def finalizeDeployable(callback: ActorRef): Unit = {
    super.finalizeDeployable(callback)
    AutomaticOperation = true
  }

  override def unregisterDeployable(obj: Deployable): Unit = {
    val zone = obj.Zone
    TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(zone.GUID, turret))
  }
}

object TurretControl {
  private def getAttackerFromCause(zone: Zone, cause: DamageResult): Option[PlanetSideServerObject with Vitality] = {
    import net.psforever.objects.sourcing._
    cause
      .interaction
      .adversarial
      .collect { adversarial =>
        adversarial.attacker match {
          case p: PlayerSource =>
            p.seatedIn
              .map { _._1.unique }
              .collect {
                case v: UniqueVehicle => zone.Vehicles.find(SourceEntry(_).unique == v)
                case a: UniqueAmenity => zone.GUID(a.guid)
                case d: UniqueDeployable => zone.DeployableList.find(SourceEntry(_).unique == d)
              }
              .flatten
              .orElse {
                val name = p.Name
                zone.LivePlayers.find(_.Name.equals(name))
              }
          case o =>
            o.unique match {
              case v: UniqueVehicle => zone.Vehicles.find(SourceEntry(_).unique == v)
              case a: UniqueAmenity => zone.GUID(a.guid)
              case d: UniqueDeployable => zone.DeployableList.find(SourceEntry(_).unique == d)
              case _ => None
            }
        }
      }
      .flatten
      .collect {
        case out: PlanetSideServerObject with Vitality => out
      }
  }
}
