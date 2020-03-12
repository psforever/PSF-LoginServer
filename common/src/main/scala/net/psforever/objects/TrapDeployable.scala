// Copyright (c) 2020 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployedItem}
import net.psforever.objects.definition.converter.TRAPConverter
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.StandardResolutions

class TrapDeployable(cdef : TrapDeployableDefinition) extends ComplexDeployable(cdef)

class TrapDeployableDefinition(objectId : Int) extends ComplexDeployableDefinition(objectId) {
  Model = StandardResolutions.SimpleDeployables
  Packet = new TRAPConverter

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TrapDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object TrapDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : TrapDeployableDefinition = {
    new TrapDeployableDefinition(dtype.id)
  }
}

class TrapDeployableControl(trap : TrapDeployable) extends Actor
  with DamageableEntity
  with RepairableEntity {
  def DamageableObject = trap
  def RepairableObject = trap

  def receive : Receive = takesDamage
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case _ =>
    }

  override protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(trap, None)
  }
}
