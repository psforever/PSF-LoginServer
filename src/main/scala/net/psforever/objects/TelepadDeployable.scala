// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem, TelepadLike}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone

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

class TelepadDeployableControl(tpad: TelepadDeployable) extends Actor with DamageableEntity {
  def DamageableObject = tpad

  def receive: Receive =
    takesDamage
      .orElse {
        case _ =>
      }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(tpad, None)
    Zone.causeExplosion(target.Zone, target, Some(cause))
  }
}
