// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployedItem, TelepadLike}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.services.RemoverActor
import net.psforever.services.local.LocalServiceMessage
import net.psforever.services.local.support.RouterTelepadActivation
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
  }

  def receive: Receive =
    deployableBehavior
      .orElse(takesDamage)
      .orElse {
        case _ =>
      }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(tpad, None)
    Zone.causeExplosion(target.Zone, target, Some(cause))
  }

  override def setupDeployable(tool : ConstructionItem) : Unit = {
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
            zone.LocalEvents ! LocalServiceMessage.Telepads(RouterTelepadActivation.AddTask(tpad, zone))
          case _ =>
            zone.LocalEvents ! LocalServiceMessage.Deployables(
              RemoverActor.AddTask(tpad, zone, Some(0 seconds))
            )
        }
      case _ => ;
    }
  }
}
