// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance
import net.psforever.types.Vector3

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a collision.
  * Being "adversarial" requires that the damage be performed as an aggressive action between individuals.
  * @param source na
  */
final case class AdversarialCollisionReason(source: DamageProperties) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Unresolved

  def same(test: DamageReason): Boolean = false

  def damageModel: DamageAndResistance = null

  override def adversary : Option[SourceEntry] = None
}

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a collision.
  * @param velocity na
  */
final case class CollisionReason(
                                  velocity: Vector3,
                                  damageModel: DamageAndResistance
                                ) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Collision

  def source: DamageProperties = CollisionReason.noDamage

  def same(test: DamageReason): Boolean = test match {
    case cr: CollisionReason => cr.velocity == velocity
    case _ => false
  }

  override def adversary : Option[SourceEntry] = None

  override def unstructuredModifiers : List[DamageModifiers.Mod] = List(Impact)
}

object CollisionReason {
  /** The flags for calculating an absence of environment damage. */
  private val noDamage = new DamageProperties { }
}
