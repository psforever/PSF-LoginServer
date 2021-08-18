// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance
import net.psforever.types.Vector3


trait CausedByColliding
  extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Collision

  def source: DamageProperties = CollisionReason.noDamage

  def velocity: Vector3

  def fall: Float
}

/**
  * A wrapper for a "damage source" in damage calculations that explains a collision.
  * @param velocity na
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
  * A wrapper for a "damage source" in damage calculations that explains a collision.
  * @param cause na
  * @param collidedWith na
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

  override def adversary: Option[SourceEntry] = Some(collidedWith)

  override def unstructuredModifiers: List[DamageModifiers.Mod] = List(
    GroundImpact2,
    HeadonImpact2
  )
}

object CollisionReason {
  /** The flags for calculating an absence of conventional damage for collision. */
  val noDamage = new DamageProperties {
    CausesDamageType = DamageType.Direct
  }
}
