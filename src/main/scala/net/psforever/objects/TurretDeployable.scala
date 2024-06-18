// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployableCategory, DeployedItem, InteractWithTurrets}
import net.psforever.objects.definition.{DeployableDefinition, WithShields}
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.hackable.{GenericHackables, Hackable}
import net.psforever.objects.serverobject.mount.{InteractWithRadiationCloudsSeatedInEntity, Mountable}
import net.psforever.objects.serverobject.turret.auto.AutomatedTurret.Target
import net.psforever.objects.serverobject.turret.auto.{AffectedByAutomaticTurretFire, AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.serverobject.turret.{MountableTurretControl, TurretDefinition, WeaponTurret, WeaponTurrets}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.resistance.StandardResistanceProfile
import net.psforever.objects.vital.{InGameActivity, ShieldCharge, SimpleResolutions, StandardVehicleResistance}
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.packet.game.TriggeredSound
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

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

  override def MaxShields: Int = {
    Definition.MaxShields
  }

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
    with MountableTurretControl
    with AutomatedTurretBehavior
    with AffectedByAutomaticTurretFire {
  def TurretObject: TurretDeployable          = turret
  def DeployableObject: TurretDeployable      = turret
  def MountableObject: TurretDeployable       = turret
  def JammableObject: TurretDeployable        = turret
  def FactionObject: TurretDeployable         = turret
  def DamageableObject: TurretDeployable      = turret
  def RepairableObject: TurretDeployable      = turret
  def AutomatedTurretObject: TurretDeployable = turret
  def AffectedObject: TurretDeployable        = turret

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
    selfReportingDatabaseUpdate()
    automaticTurretPostStop()
  }

  def receive: Receive =
    commonBehavior
      .orElse(deployableBehavior)
      .orElse(checkBehavior)
      .orElse(mountBehavior)
      .orElse(automatedTurretBehavior)
      .orElse(takeAutomatedDamage)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
          if item.Definition == GlobalDefinitions.remote_electronics_kit &&
            turret.Definition.DeployCategory == DeployableCategory.FieldTurrets &&
            turret.Faction != player.Faction =>
          sender() ! CommonMessages.Progress(
            GenericHackables.GetHackSpeed(player, turret),
            WeaponTurrets.FinishHackingTurretDeployable(turret, player),
            GenericHackables.HackingTickAction(progressType = 1, player, turret, item.GUID)
          )

        case CommonMessages.ChargeShields(amount, motivator)
        if turret.Definition.DeployCategory == DeployableCategory.FieldTurrets=>
          chargeShields(amount, motivator.collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) })

        case _ => ()
      }

  protected def engageNewDetectedTarget(
                                         target: AutomatedTurret.Target,
                                         channel: String,
                                         turretGuid: PlanetSideGUID,
                                         weaponGuid: PlanetSideGUID
                                       ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
  }

  protected def noLongerEngageTarget(
                                      target: AutomatedTurret.Target,
                                      channel: String,
                                      turretGuid: PlanetSideGUID,
                                      weaponGuid: PlanetSideGUID
                                    ): Option[AutomatedTurret.Target] = {
    val zone = target.Zone
    AutomatedTurretBehavior.stopTracking(zone, channel, turretGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
    None
  }

  protected def testNewDetected(
                                 target: AutomatedTurret.Target,
                                 channel: String,
                                 turretGuid: PlanetSideGUID,
                                 weaponGuid: PlanetSideGUID
                               ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
  }

  protected def testKnownDetected(
                                   target: AutomatedTurret.Target,
                                   channel: String,
                                   turretGuid: PlanetSideGUID,
                                   weaponGuid: PlanetSideGUID
                                 ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
  }

  override protected def suspendTargetTesting(
                                               target: Target,
                                               channel: String,
                                               turretGuid: PlanetSideGUID,
                                               weaponGuid: PlanetSideGUID
                                             ): Unit = {
    AutomatedTurretBehavior.stopTracking(target.Zone, channel, turretGuid)
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
    if (JammableObject.Jammed && AutomatedTurretObject.Definition.AutoFire.exists(_.retaliatoryDelay > 0)) {
      if (startsUnjammed) {
        AutomaticOperation = false
      }
      //look in direction of cause of jamming
      val zone = JammableObject.Zone
      AutomatedTurretBehavior.getAttackVectorFromCause(zone, cause).foreach { attacker =>
        AutomatedTurretBehavior.startTracking(zone, zone.id, AutomatedTurretObject.GUID, List(attacker.GUID))
      }
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    val startsJammed = JammableObject.Jammed
    super.CancelJammeredStatus(target)
    if (startsJammed && AutomaticOperation_=(state = true)) {
      val zone = TurretObject.Zone
      AutomatedTurretBehavior.stopTracking(zone, zone.id, TurretObject.GUID)
    }
  }

  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any): Unit = {
    amount match {
      case 0 => ()
      case _ => attemptRetaliation(target, cause)
    }
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
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
      //it's possible to request deconstruction of one's own field turret while seated in it
      val wasKickedByDriver = false
      seats.foreach { seat =>
        seat.occupant.collect {
          case player: Player =>
            seat.unmount(player)
            player.VehicleSeated = None
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.id,
              VehicleAction.KickPassenger(player.GUID, 4, wasKickedByDriver, turret.GUID)
            )
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

  //make certain vehicles don't charge shields too quickly
  def canChargeShields: Boolean = {
    val func: InGameActivity => Boolean = WithShields.LastShieldChargeOrDamage(System.currentTimeMillis(), turret.Definition)
    turret.Health > 0 && turret.Shields < turret.MaxShields &&
      turret.History.findLast(func).isEmpty
  }

  def chargeShields(amount: Int, motivator: Option[SourceEntry]): Unit = {
    if (canChargeShields) {
      turret.LogActivity(ShieldCharge(amount, motivator))
      turret.Shields = turret.Shields + amount
      turret.Zone.VehicleEvents ! VehicleServiceMessage(
        s"${turret.Actor}",
        VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), turret.GUID, turret.Definition.shieldUiAttribute, turret.Shields)
      )
    }
  }
}
