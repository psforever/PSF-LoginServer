// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.Actor
import net.psforever.objects.ce.{Deployable, DeployableBehavior, InteractWithTurrets}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.interaction.{InteractWithForceDomeProtectionSeatedInEntity, InteractWithRadiationCloudsSeatedInEntity}
import net.psforever.objects.serverobject.turret.auto.{AffectedByAutomaticTurretFire, AutomatedTurret}
import net.psforever.objects.serverobject.turret.{TurretControl, TurretDefinition, WeaponTurret}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.resistance.StandardResistanceProfile
import net.psforever.objects.vital.{SimpleResolutions, StandardVehicleResistance}
import net.psforever.objects.zones.interaction.InteractsWithZone
import net.psforever.packet.game.TriggeredSound
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration.FiniteDuration

class TurretDeployable(tdef: TurretDeployableDefinition)
  extends Deployable(tdef)
    with AutomatedTurret
    with WeaponTurret
    with JammableUnit
    with InteractsWithZone
    with StandardResistanceProfile
    with Hackable {
  HackSound = TriggeredSound.HackVehicle
  HackDuration = Array(0, 20, 10, 5)

  if (tdef.Seats.nonEmpty) {
    interaction(new InteractWithForceDomeProtectionSeatedInEntity)
    interaction(new InteractWithTurrets())
    interaction(new InteractWithRadiationCloudsSeatedInEntity(obj = this, range = 100f))
  }
  WeaponTurret.LoadDefinition(turret = this)

  def TurretOwner: SourceEntry = {
    Seats
      .values
      .headOption
      .flatMap(_.occupant)
      .map(p => PlayerSource.inSeat(PlayerSource(p), SourceEntry(this), seatNumber=0))
      .orElse(Owners.map(PlayerSource(_, Position)))
      .getOrElse(SourceEntry(this))
  }

  override def MaxShields: Int = Definition.MaxShields

  override def Definition: TurretDeployableDefinition = tdef
}

abstract class TurretDeployableDefinition(private val objectId: Int)
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
}

/** control actors */

abstract class TurretDeployableControl
    extends Actor
    with DeployableBehavior
    with FactionAffinityBehavior.Check
    with TurretControl
    with AffectedByAutomaticTurretFire {

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
  }

  override def commonBehavior: Receive =
    super.commonBehavior
      .orElse(deployableBehavior)
      .orElse(checkBehavior)
      .orElse(takeAutomatedDamage)

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(DeployableObject, None)
  }

  override def deconstructDeployable(time: Option[FiniteDuration]) : Unit = {
    val zone = TurretObject.Zone
    val seats = TurretObject.Seats.values
    //either we have no seats or no one gets to sit
    val retime = if (seats.count(_.isOccupied) > 0) {
      //it's possible to request deconstruction of one's own field turret while seated in it
      val wasKickedByDriver = false
      seats.foreach { seat =>
        seat.occupant.collect {
          case player: Player =>
            seat.unmount(player)
            player.VehicleSeated = None
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.id,
              VehicleAction.KickPassenger(player.GUID, 4, wasKickedByDriver, TurretObject.GUID)
            )
        }
      }
      Some(time.getOrElse(Deployable.cleanup) + Deployable.cleanup)
    } else {
      time
    }
    super.deconstructDeployable(retime)
  }

  override def unregisterDeployable(obj: Deployable): Unit = {
    val zone = obj.Zone
    TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(zone.GUID, TurretObject))
  }
}
