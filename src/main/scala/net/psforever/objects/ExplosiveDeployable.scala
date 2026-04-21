// Copyright (c) 2018 PSForever
package net.psforever.objects

import akka.actor.Actor
import net.psforever.objects.ce._
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.vital.{SimpleResolutions, Vitality}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.annotation.unused
import scala.concurrent.duration._

class ExplosiveDeployable(cdef: ExplosiveDeployableDefinition)
  extends Deployable(cdef)
    with JammableUnit {

  override def Definition: ExplosiveDeployableDefinition = cdef
}

object ExplosiveDeployable {
  final case class TriggeredBy(obj: PlanetSideServerObject)
}

abstract class ExplosiveDeployableDefinition(private val objectId: Int)
  extends DeployableDefinition(objectId) {
  Name = "explosive_deployable"
  DeployCategory = DeployableCategory.Mines
  Model = SimpleResolutions.calculate
  Packet = new SmallDeployableConverter

  private var detonateOnJamming: Boolean = true

  private var stability: Boolean = false

  var triggerRadius: Float = 0f

  def DetonateOnJamming: Boolean = detonateOnJamming

  def DetonateOnJamming_=(detonate: Boolean): Boolean = {
    detonateOnJamming = detonate
    DetonateOnJamming
  }

  def Stable: Boolean = stability

  def Stable_=(stableState: Boolean): Boolean = {
    stability = stableState
    Stable
  }
}

abstract class ExplosiveDeployableControl(mine: ExplosiveDeployable)
  extends Actor
  with DeployableBehavior
  with Damageable {
  def DeployableObject: ExplosiveDeployable = mine
  def DamageableObject: ExplosiveDeployable = mine

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
  }

  def commonMineBehavior: Receive =
    deployableBehavior
      .orElse(takesDamage)

  override protected def PerformDamage(
    target: Target,
    applyDamageTo: Output
  ): Unit = {
    if (mine.CanDamage) {
      val originalHealth = mine.Health
      val cause          = applyDamageTo(mine)
      val damage         = originalHealth - mine.Health
      if (Interaction(mine, damage, cause.interaction)) {
        HandleDamage(mine, cause, damage)
      } else {
        mine.Health = originalHealth
      }
    }
  }

  final def HandleDamage(target: ExplosiveDeployable, cause: DamageResult, damage: Int): Unit = {
    target.LogActivity(cause)
    if (CanDetonate(target, damage, cause.interaction)) {
      ExplosiveDeployableControl.doExplosion(target, cause)
    } else if (target.Health == 0) {
      ExplosiveDeployableControl.DestructionAwareness(target, cause)
    } else {
      ExplosiveDeployableControl.DamageAwareness(target, cause, damage)
    }
  }

  def Interaction(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    val actualDamage: Int = if (!mine.Definition.Stable && data.cause.source.SympatheticExplosion) {
      math.max(damage, 1)
    } else {
      damage
    }
    !mine.Destroyed &&
      Damageable.adversarialOrHackableChecks(obj, data) &&
      (CanDetonate(obj, actualDamage, data) || Damageable.CanDamage(obj, actualDamage, data))
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
  def CanDetonate(obj: Vitality with FactionAffinity, @unused damage: Int, data: DamageInteraction): Boolean = {
    val sourceDef = data.cause.source
    val mineDef = mine.Definition
    val explodeFromSympathy: Boolean = sourceDef.SympatheticExplosion && !mineDef.Stable
    val explodeFromJammer: Boolean = ExplosiveDeployableControl.CanJammer(mine, data)
    !mine.Destroyed && (explodeFromSympathy || explodeFromJammer)
  }
}

object ExplosiveDeployableControl {
  def CanJammer(mine: ExplosiveDeployable, data: DamageInteraction): Boolean = {
    Damageable.adversarialOrHackableChecks(mine, data) &&
      data.cause.source.AdditionalEffect &&
      mine.Definition.DetonateOnJamming
  }

  /**
    * na
    * @param target na
    * @param cause na
    * @param damage na
    */
  def DamageAwareness(target: ExplosiveDeployable, cause: DamageResult, damage: Int): Unit = {
    if (
      !target.Jammed &&
        CanJammer(target, cause.interaction) &&
        {
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

  /**
    * na
    * @param target na
    * @param cause na
    */
  def explodes(target: Damageable.Target, cause: DamageResult): Unit = {
    target.Destroyed = true
    target.Health = 1 // short-circuit logic in DestructionAwareness
    val zone = target.Zone
    zone.Activity ! Zone.HotSpot.Activity(cause)
    zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.Detonate(target.GUID, target))
    Zone.serverSideDamage(
      zone,
      target,
      Zone.explosionDamage(Some(cause)),
      ExplosiveDeployableControl.detectionForExplosiveSource(target),
      Zone.findAllTargets
    )
  }

  def doExplosion(target: ExplosiveDeployable, cause: DamageResult): Unit = {
    explodes(target, cause)
    DestructionAwareness(target, cause)
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
  def detectTarget(
                    g1: VolumetricGeometry,
                    up: Vector3
                  )
                  (
                    obj1: PlanetSideGameObject,
                    obj2: PlanetSideGameObject,
                    maxDistance: Float
                  ) : Boolean = {
    val g2 = obj2.Definition.Geometry(obj2)
    val dir = g2.center.asVector3 - g1.center.asVector3
    //val scalar = Vector3.ScalarProjection(dir, up)
    val point1 = g1.pointOnOutside(dir).asVector3
    val point2 = g2.pointOnOutside(Vector3.neg(dir)).asVector3
    val scalar = Vector3.ScalarProjection(point2 - obj1.Position, up)
    (scalar >= 0 || Vector3.MagnitudeSquared(up * scalar) < 0.35f) &&
    math.min(
      Vector3.DistanceSquared(g1.center.asVector3, g2.center.asVector3),
      Vector3.DistanceSquared(point1, point2)
    ) <= maxDistance
  }
}
