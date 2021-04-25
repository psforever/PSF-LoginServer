// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployedItem, TelepadLike}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.vehicles.UtilityType
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class TelepadDeployable(ddef: TelepadDeployableDefinition)
  extends Deployable(ddef) with TelepadLike {
  override def Definition: TelepadDeployableDefinition = ddef
}

class TelepadDeployableDefinition(objectId: Int) extends DeployableDefinition(objectId) {
  Model = SimpleResolutions.calculate

  var linkTime: FiniteDuration = 60.seconds

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
        case TelepadLike.Activate(tpad: TelepadDeployable)
          if isConstructed.contains(true) =>
          val zone = tpad.Zone
          (zone.GUID(tpad.Router) match {
            case Some(vehicle : Vehicle) => vehicle.Utility(UtilityType.internal_router_telepad_deployable)
            case _                             => None
          }) match {
            case Some(obj: InternalTelepad) =>
              import scala.concurrent.ExecutionContext.Implicits.global
              setup = context.system.scheduler.scheduleOnce(
                tpad.Definition.linkTime,
                obj.Actor,
                TelepadLike.RequestLink(tpad)
              )
            case _ =>
              deconstructDeployable(None)
              tpad.OwnerName match {
                case Some(owner) =>
                  TelepadControl.TelepadError(zone, owner, msg = "@Telepad_NoDeploy_RouterLost")
                case None => ;
              }
          }

        case TelepadLike.Activate(obj: InternalTelepad)
          if isConstructed.contains(true) =>
          if (obj.Telepad.contains(tpad.GUID) && tpad.Router.contains(obj.Owner.GUID)) {
            tpad.Active = true
            TelepadLike.LinkTelepad(tpad.Zone, tpad.GUID)
          }

        case TelepadLike.SeverLink(obj: InternalTelepad)
          if isConstructed.contains(true) =>
          if (tpad.Router.contains(obj.Owner.GUID)) {
            tpad.Router = None
            tpad.Active = false
            tpad.Actor ! Deployable.Deconstruct()
          }

        case _ =>
      }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    TelepadControl.DestructionAwareness(tpad)
    Deployables.AnnounceDestroyDeployable(tpad, None)
    Zone.causeExplosion(target.Zone, target, Some(cause))
  }

  override def startOwnerlessDecay(): Unit = {
    //telepads do not decay when they become ownerless
    //telepad decay is tied to their lifecycle with routers
    tpad.Owner = None
    tpad.OwnerName = None
  }

  override def finalizeDeployable(callback: ActorRef): Unit = {
    super.finalizeDeployable(callback)
    decay.cancel() //telepad does not decay if unowned; but, deconstruct if router link fails
    self ! TelepadLike.Activate(tpad)
  }

  override def deconstructDeployable(time : Option[FiniteDuration]) : Unit = {
    TelepadControl.DestructionAwareness(tpad)
    super.deconstructDeployable(time)
  }
}

object TelepadControl {
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
