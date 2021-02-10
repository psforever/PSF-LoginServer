// Copyright (c) 2020 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.converter.TRAPConverter
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone

class TrapDeployable(cdef: TrapDeployableDefinition) extends Deployable(cdef)

class TrapDeployableDefinition(objectId: Int) extends DeployableDefinition(objectId) {
  Model = SimpleResolutions.calculate
  Packet = new TRAPConverter

  override def Initialize(obj: Deployable, context: ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TrapDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object TrapDeployableDefinition {
  def apply(dtype: DeployedItem.Value): TrapDeployableDefinition = {
    new TrapDeployableDefinition(dtype.id)
  }
}

class TrapDeployableControl(trap: TrapDeployable) extends Actor with DamageableEntity with RepairableEntity {
  def DamageableObject = trap
  def RepairableObject = trap

  def receive: Receive =
    takesDamage
      .orElse(canBeRepairedByNanoDispenser)
      .orElse {
        case _ =>
      }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(trap, None)
    Zone.serverSideDamage(target.Zone, target, Zone.explosionDamage(Some(cause)))
  }
}
