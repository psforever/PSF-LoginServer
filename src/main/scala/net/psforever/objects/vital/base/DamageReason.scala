// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.ballistics.{ProjectileResolution, Projectile => ActualProjectile}
import net.psforever.objects.vital.DamageAndResistance

trait DamageReason {
  def same(test: DamageReason): Boolean

  def calculate(data: DamageInteraction): Any => DamageResult = (_: Any) => data
}

final case class ProjectileReason(
                                   resolution : ProjectileResolution.Value,
                                   projectile: ActualProjectile,
                                   damageModel: DamageAndResistance
                                 ) extends DamageReason {
  def same(test: DamageReason): Boolean = {
    test match {
      case o: ProjectileReason => o.projectile.id == projectile.id
      case _ => false
    }
  }

  override def calculate(data: DamageInteraction): Any => DamageResult = {
    damageModel.Calculate(data)
  }
}

final case class AdversarialCollisionReason() extends DamageReason {
  def same(test: DamageReason): Boolean = false
}

final case class CollisionReason() extends DamageReason {
  def same(test: DamageReason): Boolean = false
}

final case class ExplosionReason() extends DamageReason {
  def same(test: DamageReason): Boolean = false
}

final case class EnvironmentReason(body: Any, damage: Int) extends DamageReason {
  def same(test: DamageReason): Boolean = {
    test match {
      case o : EnvironmentReason => body == o.body //TODO eq
      case _ => false
    }
  }
}
