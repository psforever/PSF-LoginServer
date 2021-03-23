// Copyright (c) 2018 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.{PlayerSource, SourceEntry}
import net.psforever.objects.ce._
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.geometry.Geometry3D
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.vital.{SimpleResolutions, Vitality}
import net.psforever.objects.vital.etc.TriggerUsedReason
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class ExplosiveDeployable(cdef: ExplosiveDeployableDefinition)
  extends ComplexDeployable(cdef)
  with JammableUnit {

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
        case CommonMessages.Use(player, Some(trigger: BoomerTrigger)) if {
          mine match {
            case boomer: BoomerDeployable => boomer.Trigger.contains(trigger) && mine.Definition.Damageable
            case _                        => false
          }
        } =>
          // the trigger damages the mine, which sets it off, which causes an explosion
          // think of this as an initiator to the proper explosion
          mine.Destroyed = true
          ExplosiveDeployableControl.DamageResolution(
            mine,
            DamageInteraction(
              SourceEntry(mine),
              TriggerUsedReason(PlayerSource(player), trigger.GUID),
              mine.Position
            ).calculate()(mine),
            damage = 0
          )
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
      if (CanDetonate(mine, damage, cause.interaction)) {
        ExplosiveDeployableControl.DamageResolution(mine, cause, damage)
      } else {
        mine.Health = originalHealth
      }
    }
  }

  /**
    * A supplement for checking target susceptibility
    * to account for sympathetic explosives even if there is no damage.
    * This does not supercede other underlying checks or undo prior damage checks.
    * @see `Damageable.CanDamageOrJammer`
    * @see `DamageProperties.SympatheticExplosives`
    * @param obj the entity being damaged
    * @param damage the amount of damage
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanDetonate(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    !mine.Destroyed && (if (damage == 0 && data.cause.source.SympatheticExplosion) {
      Damageable.CanDamageOrJammer(mine, damage = 1, data)
    } else {
      Damageable.CanDamageOrJammer(mine, damage, data)
    })
  }
}

object ExplosiveDeployableControl {
  /**
    * na
    * @param target na
    * @param cause na
    * @param damage na
    */
  def DamageResolution(target: ExplosiveDeployable, cause: DamageResult, damage: Int): Unit = {
    target.History(cause)
    if (cause.interaction.cause.source.SympatheticExplosion) {
      explodes(target, cause)
      DestructionAwareness(target, cause)
    } else if (target.Health == 0) {
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
        if (target.Definition.DetonateOnJamming) {
          explodes(target, cause)
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
  def explodes(target: Damageable.Target, cause: DamageResult): Unit = {
    target.Health = 1 // short-circuit logic in DestructionAwareness
    val zone = target.Zone
    zone.Activity ! Zone.HotSpot.Activity(cause)
    zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.Detonate(target.GUID, target))
    Zone.causeExplosion(zone, target, Some(cause), ExplosiveDeployableControl.detectionForExplosiveSource(target))
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  def DestructionAwareness(target: ExplosiveDeployable, cause: DamageResult): Unit = {
    val zone = target.Zone
    val attribution = DamageableEntity.attributionTo(cause, target.Zone)
    Deployables.AnnounceDestroyDeployable(
      target,
      Some(if (target.Jammed || target.Destroyed) 0 seconds else 500 milliseconds)
    )
    target.Destroyed = true
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

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * For explosives, the source of the explosion is always typically constant.
    * @see `detectsTarget`
    * @see `ObjectDefinition.Geometry`
    * @see `Vector3.relativeUp`
    * @param obj a game entity that explodes
    * @return a function that resolves a potential target as detected
    */
  def detectionForExplosiveSource(obj: PlanetSideGameObject): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    val up = Vector3.relativeUp(obj.Orientation) //check relativeUp; rotate as little as necessary!
    val g1 = obj.Definition.Geometry(obj)
    detectTarget(g1, up)
  }

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * For explosives, targets in the damage radius in the direction of the blast (above the explosive) are valid targets.
    * Targets that are ~0.5916f units in the opposite direction of the blast (below the explosive) are also selected.
    * @see `ObjectDefinition.Geometry`
    * @see `PrimitiveGeometry.pointOnOutside`
    * @see `Vector3.DistanceSquared`
    * @see `Vector3.neg`
    * @see `Vector3.relativeUp`
    * @see `Vector3.ScalarProjection`
    * @see `Vector3.Unit`
    * @param g1 a cached geometric representation that should belong to `obj1`
    * @param up a cached vector in the direction of "above `obj1`'s geometric representation"
    * @param obj1 a game entity that explodes
    * @param obj2 a game entity that suffers the explosion
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the target entities are near enough to each other;
    *        `false`, otherwise
    */
  def detectTarget(g1: Geometry3D, up: Vector3)(obj1: PlanetSideGameObject, obj2: PlanetSideGameObject, maxDistance: Float) : Boolean = {
    val g2 = obj2.Definition.Geometry(obj2)
    val dir = g2.center.asVector3 - g1.center.asVector3
    //val scalar = Vector3.ScalarProjection(dir, up)
    val point1 = g1.pointOnOutside(dir).asVector3
    val point2 = g2.pointOnOutside(Vector3.neg(dir)).asVector3
    val scalar = Vector3.ScalarProjection(point2 - point1, up)
    (scalar >= 0 || Vector3.MagnitudeSquared(up * scalar) < 0.35f) &&
    math.min(
      Vector3.DistanceSquared(g1.center.asVector3, g2.center.asVector3),
      Vector3.DistanceSquared(point1, point2)
    ) <= maxDistance
  }
}
