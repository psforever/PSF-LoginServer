// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance

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
  * @param source na
  */
final case class CollisionReason(source: DamageProperties) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Unresolved

  def same(test: DamageReason): Boolean = false

  def damageModel: DamageAndResistance = null

  override def adversary : Option[SourceEntry] = None
}
