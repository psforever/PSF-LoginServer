// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.vital.base.DamageReason
import net.psforever.objects.vital.prop.DamageProperties

final case class AdversarialCollisionReason(source: DamageProperties) extends DamageReason {
  def same(test: DamageReason): Boolean = false
}

final case class CollisionReason(source: DamageProperties) extends DamageReason {
  def same(test: DamageReason): Boolean = false
}
