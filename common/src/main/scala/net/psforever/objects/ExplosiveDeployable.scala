// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce._
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.{StandardResolutions, Vitality}
import net.psforever.types.{PlanetSideGUID, Vector3}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class ExplosiveDeployable(cdef : ExplosiveDeployableDefinition) extends ComplexDeployable(cdef)
  with JammableUnit {
  private var exploded : Boolean = false

  def Exploded : Boolean = exploded

  def Exploded_=(fuse : Boolean) : Boolean = {
    exploded = fuse
    Exploded
  }

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
    obj.Actor = context.actorOf(Props(classOf[ExplosiveDeployableControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
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

class ExplosiveDeployableControl(mine : ExplosiveDeployable) extends Actor {
  def receive : Receive = {
    case Vitality.Damage(damage_func) =>
      val originalHealth = mine.Health
      if(originalHealth > 0) {
        val cause = damage_func(mine)
        ExplosiveDeployableControl.HandleDamageResolution(mine, cause, originalHealth - mine.Health)
      }

    case _ => ;
  }
}

object ExplosiveDeployableControl {
  def HandleDamageResolution(target : ExplosiveDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health == 0) {
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    else if(!target.Jammed && cause.projectile.profile.JammerProjectile) {
      if(target.Jammed = {
        val radius = cause.projectile.profile.DamageRadius
        Vector3.DistanceSquared(cause.hit_pos, cause.target.Position) < radius * radius
      }) {
        if(target.Definition.DetonateOnJamming) {
          target.Zone.LocalEvents ! LocalServiceMessage(target.Zone.Id, LocalAction.Detonate(target.GUID, target))
        }
        HandleDestructionAwareness(target, playerGUID, cause)
      }
    }
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : ExplosiveDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    Deployables.AnnounceDestroyDeployable(target, Some(if(target.Jammed) 0 seconds else 500 milliseconds))
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
  }
}

