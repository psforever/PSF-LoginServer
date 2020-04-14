// Copyright (c) 2018 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce._
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.vital.{StandardResolutions, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideGUID, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class ExplosiveDeployable(cdef : ExplosiveDeployableDefinition) extends ComplexDeployable(cdef)
  with JammableUnit {

  override def Definition : ExplosiveDeployableDefinition = cdef
}

class ExplosiveDeployableDefinition(private val objectId : Int) extends ComplexDeployableDefinition(objectId) {
  Name = "explosive_deployable"
  DeployCategory = DeployableCategory.Mines
  Model = StandardResolutions.SimpleDeployables
  Packet = new SmallDeployableConverter

  private var detonateOnJamming : Boolean = true

  def DetonateOnJamming : Boolean = detonateOnJamming

  def DetonateOnJamming_=(detonate : Boolean) : Boolean = {
    detonateOnJamming = detonate
    DetonateOnJamming
  }

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[ExplosiveDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object ExplosiveDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : ExplosiveDeployableDefinition = {
    new ExplosiveDeployableDefinition(dtype.id)
  }
}

class ExplosiveDeployableControl(mine : ExplosiveDeployable) extends Actor
  with Damageable {
  def DamageableObject = mine

  def receive : Receive = takesDamage
    .orElse {
      case _ => ;
    }

  protected def TakesDamage : Receive = {
    case Vitality.Damage(applyDamageTo) =>
      if(mine.CanDamage) {
        val originalHealth = mine.Health
        val cause = applyDamageTo(mine)
        val damage = originalHealth - mine.Health
        if(Damageable.CanDamageOrJammer(mine, damage, cause)) {
          ExplosiveDeployableControl.DamageResolution(mine, cause, damage)
        }
        else {
          mine.Health = originalHealth
        }
      }
  }
}

object ExplosiveDeployableControl {
  def DamageResolution(target : ExplosiveDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
    target.History(cause)
    if(target.Health == 0) {
      DestructionAwareness(target, cause)
    }
    else if(!target.Jammed && Damageable.CanJammer(target, cause)) {
      if(target.Jammed = {
        val radius = cause.projectile.profile.DamageRadius
        Vector3.DistanceSquared(cause.hit_pos, cause.target.Position) < radius * radius
      }) {
        if(target.Definition.DetonateOnJamming) {
          val zone = target.Zone
          zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
          zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.Detonate(target.GUID, target))
        }
        DestructionAwareness(target, cause)
      }
    }
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  def DestructionAwareness(target : ExplosiveDeployable, cause : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    val attribution = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    target.Destroyed = true
    Deployables.AnnounceDestroyDeployable(target, Some(if(target.Jammed) 0 seconds else 500 milliseconds))
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.Destroy(target.GUID, attribution, Service.defaultPlayerGUID, target.Position))
    if(target.Health == 0) {
      zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.TriggerEffect(Service.defaultPlayerGUID, "detonate_damaged_mine", target.GUID))
    }
  }
}
