// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployedItem, TelepadLike}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.vehicles.UtilityType
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.services.RemoverActor
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.DriveState

import scala.concurrent.duration._

class TelepadDeployable(ddef: TelepadDeployableDefinition) extends Deployable(ddef) with TelepadLike

class TelepadDeployableDefinition(objectId: Int) extends DeployableDefinition(objectId) {
  Model = SimpleResolutions.calculate

  override def Initialize(obj: Deployable, context: ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TelepadDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object TelepadDeployableDefinition {
  def apply(dtype: DeployedItem.Value): TelepadDeployableDefinition = {
    new TelepadDeployableDefinition(dtype.id)
  }
}

class TelepadDeployableControl(tpad: TelepadDeployable)
  extends Actor
  with DeployableBehavior
  with DamageableEntity {
  def DeployableObject = tpad
  def DamageableObject = tpad

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
    TelepadControl.DestructionAwareness(tpad)
  }

  def receive: Receive =
    deployableBehavior
      .orElse(takesDamage)
      .orElse {
        case TelepadLike.Activate(_: TelepadDeployable) =>
          val zone = tpad.Zone
          (zone.GUID(tpad.Router) match {
            case Some(vehicle : Vehicle) => vehicle.Utility(UtilityType.internal_router_telepad_deployable)
            case _                             => None
          }) match {
            case Some(obj: InternalTelepad) =>
              obj.Actor ! TelepadLike.RequestLink(tpad)
            case _ =>
              zone.LocalEvents ! LocalServiceMessage.Deployables(
                RemoverActor.AddTask(tpad, zone, Some(0 seconds))
              )
              TelepadControl.TelepadError(zone, tpad.OwnerName.getOrElse(""), msg = "@Telepad_NoDeploy_RouterLost")
          }

        case TelepadLike.Activate(obj: InternalTelepad) =>
          if (obj.Telepad.contains(tpad.GUID) && tpad.Router.contains(obj.Owner.GUID)) {
            tpad.Active = true
            TelepadLike.LinkTelepad(tpad.Zone, tpad.GUID)
          }

        case TelepadLike.SeverLink(obj: InternalTelepad) =>
          if (tpad.Router.contains(obj.Owner.GUID)) {
            tpad.Router = None
            tpad.Active = false
            val zone = tpad.Zone
            zone.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(tpad, zone, Some(0 seconds))
            )
          }

        case _ =>
      }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    TelepadControl.DestructionAwareness(tpad)
    Deployables.AnnounceDestroyDeployable(tpad, None)
    Zone.causeExplosion(target.Zone, target, Some(cause))
  }

  override def setupDeployable(tool: ConstructionItem): Unit = {
    super.setupDeployable(tool)
    val zone = tpad.Zone
    tool match {
      case tele: Telepad =>
        //a telepad is connected to the router that dispensed it
        //the telepad deployable must also be connected, but only if the router is in the correct state
        zone.GUID(tele.Router) match {
          case Some(vehicle: Vehicle)
            if tpad.Health > 0 && !vehicle.Destroyed && vehicle.DeploymentState == DriveState.Deployed =>
            tpad.Router = tele.Router //necessary; forwards link to the router
          case _ =>
            zone.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(tpad, zone, Some(0 seconds))
            )
            TelepadControl.TelepadError(zone, tpad.OwnerName.getOrElse(""), msg = "@Telepad_NoDeploy_RouterLost")
        }
      case _ => ;
    }
  }

  override def finalizeDeployable(tool: ConstructionItem): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    setup.cancel()
    setup = context.system.scheduler.scheduleOnce(
      TelepadControl.LinkTime,
      self,
      TelepadLike.Activate(tpad)
    )
  }
}

object TelepadControl {
  val LinkTime = 60 seconds

  def DestructionAwareness(tpad: TelepadDeployable): Unit = {
    if (tpad.Active) {
      tpad.Active = false
      (tpad.Zone.GUID(tpad.Router) match {
        case Some(vehicle : Vehicle) => vehicle.Utility(UtilityType.internal_router_telepad_deployable)
        case _                             => None
      }) match {
        case Some(obj: InternalTelepad) => obj.Actor ! TelepadLike.SeverLink(tpad)
        case _ => ;
      }
    }
  }

  def TelepadError(zone: Zone, channel: String, msg: String): Unit = {
    zone.LocalEvents ! LocalServiceMessage(channel, LocalAction.RouterTelepadMessage(msg))
  }
}
