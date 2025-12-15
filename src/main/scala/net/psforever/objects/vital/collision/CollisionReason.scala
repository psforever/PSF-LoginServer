// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.sourcing.{DeployableSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance
import net.psforever.types.Vector3

/**
  * Common base for reporting damage for reasons of collisions.
  */
trait CausedByColliding
  extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Collision

  def source: DamageProperties = CollisionReason.noDamage

  def velocity: Vector3

  def fall: Float
}

/**
  * A wrapper for a "damage source" in damage calculations that explains a collision.
  * @param velocity how fast the target is moving prior to the collision
  * @param fall ongoing vertical displacement since before the collision
  * @param damageModel the functionality that is necessary for interaction
  *                    of a vital game object with the rest of the hostile game world
  */
final case class CollisionReason(
                                  velocity: Vector3,
                                  fall: Float,
                                  damageModel: DamageAndResistance
                                ) extends CausedByColliding {
  def same(test: DamageReason): Boolean = test match {
    case cr: CollisionReason => cr.velocity == velocity && math.abs(cr.fall - fall) < 0.05f
    case _ => false
  }

  override def adversary: Option[SourceEntry] = None

  override def unstructuredModifiers: List[DamageModifiers.Mod] = List(
    GroundImpact,
    HeadonImpact
  )
}

/**
  * A wrapper for a "damage source" in damage calculations that augment collision information
  * by providing information about a qualified target that was struck.
  * @param cause information about the collision
  * @param collidedWith information regarding the qualified target that was struck
  */
final case class CollisionWithReason(
                                      cause: CollisionReason,
                                      collidedWith: SourceEntry
                                    ) extends CausedByColliding {
  def same(test: DamageReason): Boolean = test match {
    case cr: CollisionWithReason =>
      cr.cause.same(cause) && cr.collidedWith == collidedWith
    case _ =>
      false
  }

  def velocity: Vector3 = cause.velocity

  def fall: Float = cause.fall

  def damageModel: DamageAndResistance = cause.damageModel

  override def adversary: Option[SourceEntry] = {
    collidedWith match {
      case v: VehicleSource =>
        v.occupants.head match {
          case SourceEntry.None => Some(collidedWith)
          case e => Some(e)
        }
      case d: DeployableSource =>
        d.owner match {
          case SourceEntry.None => Some(collidedWith)
          case e => Some(e)
        }
      case _ =>
        Some(collidedWith)
    }
  }

  override def unstructuredModifiers: List[DamageModifiers.Mod] = List(
    GroundImpactWith,
    HeadonImpactWithEntity
  ) ++ collidedWith.Definition.Modifiers

  override def attribution : Int = collidedWith.Definition.ObjectId
}

object CollisionReason {
  /** The flags for calculating an absence of conventional damage for collision.
    * Damage is considered `Direct`, however, which defines some resistance. */
  val noDamage = new DamageProperties {
    CausesDamageType = DamageType.Direct
    DamageToArmorFirst = true
  }
}
