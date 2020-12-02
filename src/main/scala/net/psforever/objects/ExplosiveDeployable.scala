// Copyright (c) 2018 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce._
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class ExplosiveDeployable(cdef: ExplosiveDeployableDefinition) extends ComplexDeployable(cdef) with JammableUnit {

  override def Definition: ExplosiveDeployableDefinition = cdef
}

class ExplosiveDeployableDefinition(private val objectId: Int) extends ComplexDeployableDefinition(objectId) {
  Name = "explosive_deployable"
  DeployCategory = DeployableCategory.Mines
  Model = SimpleResolutions.calculate
  Packet = new SmallDeployableConverter

  private var detonateOnJamming: Boolean = true

  def DetonateOnJamming: Boolean = detonateOnJamming

  def DetonateOnJamming_=(detonate: Boolean): Boolean = {
    detonateOnJamming = detonate
    DetonateOnJamming
  }

  override def Initialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    obj.Actor =
      context.actorOf(Props(classOf[ExplosiveDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object ExplosiveDeployableDefinition {
  def apply(dtype: DeployedItem.Value): ExplosiveDeployableDefinition = {
    new ExplosiveDeployableDefinition(dtype.id)
  }
}

class ExplosiveDeployableControl(mine: ExplosiveDeployable) extends Actor with Damageable {
  def DamageableObject = mine

  def receive: Receive =
    takesDamage
      .orElse {
        case _ => ;
      }

  override protected def PerformDamage(
    target: Target,
    applyDamageTo: Output
  ): Unit = {
    if (mine.CanDamage) {
      val originalHealth = mine.Health
      val cause          = applyDamageTo(mine)
      val damage         = originalHealth - mine.Health
      if (Damageable.CanDamageOrJammer(mine, damage, cause.interaction)) {
        ExplosiveDeployableControl.DamageResolution(mine, cause, damage)
      } else {
        mine.Health = originalHealth
      }
    }
  }
}

object ExplosiveDeployableControl {
  def DamageResolution(target: ExplosiveDeployable, cause: DamageResult, damage: Int): Unit = {
    target.History(cause)
    if (target.Health == 0) {
      DestructionAwareness(target, cause)
    } else if (!target.Jammed && Damageable.CanJammer(target, cause.interaction)) {
      if ( {
        target.Jammed = cause.interaction.cause match {
          case o: ProjectileReason =>
            val radius = o.projectile.profile.DamageRadius
            Vector3.DistanceSquared(cause.interaction.hitPos, cause.interaction.target.Position) < radius * radius
          case _ =>
            true
        }
      }
      ) {
        if (cause.interaction.cause.source.SympatheticExplosion || target.Definition.DetonateOnJamming) {
          val zone = target.Zone
          zone.Activity ! Zone.HotSpot.Activity(cause)
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.Detonate(target.GUID, target))
          Zone.causeExplosion(zone, target, Some(cause))
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
  def DestructionAwareness(target: ExplosiveDeployable, cause: DamageResult): Unit = {
    val zone = target.Zone
    val attribution = DamageableEntity.attributionTo(cause, target.Zone)
    target.Destroyed = true
    Deployables.AnnounceDestroyDeployable(target, Some(if (target.Jammed) 0 seconds else 500 milliseconds))
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.Destroy(target.GUID, attribution, Service.defaultPlayerGUID, target.Position)
    )
    if (target.Health == 0) {
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.TriggerEffect(Service.defaultPlayerGUID, "detonate_damaged_mine", target.GUID)
      )
    }
  }
}
