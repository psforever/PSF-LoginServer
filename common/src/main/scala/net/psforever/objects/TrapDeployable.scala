// Copyright (c) 2020 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployedItem}
import net.psforever.objects.definition.converter.TRAPConverter
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.{StandardResolutions, Vitality}
import net.psforever.types.PlanetSideGUID
import services.avatar.{AvatarAction, AvatarServiceMessage}

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
  with RepairableEntity {
  def RepairableObject = trap

  def receive : Receive = canBeRepairedByNanoDispenser
    .orElse {
      case Vitality.Damage(damage_func) =>
        val originalHealth = trap.Health
        if(originalHealth > 0) {
          val cause = damage_func(trap)
          TrapDeployableControl.HandleDamageResolution(trap, cause, originalHealth - trap.Health)
        }

      case _ =>
    }
}

object TrapDeployableControl {
  def HandleDamageResolution(target : TrapDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health == 0) {
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(target.GUID, 0, target.Health))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : TrapDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Destroyed = true
    val zone = target.Zone
    Deployables.AnnounceDestroyDeployable(target, None)
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
  }
}
