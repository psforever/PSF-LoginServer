// Copyright (c) 2024 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.WithShields
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.turret.{MountableTurret, WeaponTurrets}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.{InGameActivity, ShieldCharge}
import net.psforever.packet.game.HackState1
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

/** definition */

class FieldTurretDeployableDefinition(private val objectId: Int)
  extends TurretDeployableDefinition(objectId) {
  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor = context.actorOf(Props(classOf[FieldTurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object FieldTurretDeployableDefinition {
  def apply(dtype: DeployedItem.Value): FieldTurretDeployableDefinition = {
    new FieldTurretDeployableDefinition(dtype.id)
  }
}

/** control actor */

class FieldTurretControl(turret: TurretDeployable)
  extends TurretDeployableControl
    with MountableBehavior {
  def TurretObject: TurretDeployable     = turret
  def DeployableObject: TurretDeployable = turret
  def JammableObject: TurretDeployable   = turret
  def FactionObject: TurretDeployable    = turret
  def DamageableObject: TurretDeployable = turret
  def RepairableObject: TurretDeployable = turret
  def AffectedObject: TurretDeployable   = turret
  def MountableObject: TurretDeployable  = turret

  def receive: Receive =
    commonBehavior
      .orElse(mountBehavior)
      .orElse(dismountBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
          if item.Definition == GlobalDefinitions.remote_electronics_kit &&
            turret.Faction != player.Faction =>
          sender() ! CommonMessages.Progress(
            GenericHackables.GetHackSpeed(player, turret),
            WeaponTurrets.FinishHackingTurretDeployable(turret, player),
            GenericHackables.HackingTickAction(HackState1.Unk1, player, turret, item.GUID)
          )

        case CommonMessages.ChargeShields(amount, motivator) =>
          chargeShields(amount, motivator.collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) })

        case _ => ()
      }

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    player: Player
                                  ): Boolean = MountableTurret.MountTest(TurretObject, player)

  //make certain vehicles don't charge shields too quickly
  private def canChargeShields: Boolean = {
    val func: InGameActivity => Boolean = WithShields.LastShieldChargeOrDamage(System.currentTimeMillis(), turret.Definition)
    turret.Health > 0 && turret.Shields < turret.MaxShields &&
      turret.History.findLast(func).isEmpty
  }

  private def chargeShields(amount: Int, motivator: Option[SourceEntry]): Unit = {
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
