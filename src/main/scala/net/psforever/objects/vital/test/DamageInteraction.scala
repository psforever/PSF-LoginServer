// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.test

import net.psforever.objects.ballistics.{Projectile=>ActualProjectile, ProjectileResolution, SourceEntry}
import net.psforever.objects.vital.DamageResistanceModel
import net.psforever.types.Vector3

trait DamageReason

object DamageReason {
  final case class Projectile(
                               resolution : ProjectileResolution.Value,
                               projectile: ActualProjectile,
                               damageModel: DamageResistanceModel
                             ) extends DamageReason

  final case class AdversarialCollision() extends DamageReason

  final case class Collision() extends DamageReason

  final case class Environment(body: Any, damage: Int) extends DamageReason
}

trait DamageInteraction {
  def hitTime: Long

  def target: SourceEntry

  def cause: DamageReason

  def hitPos: Vector3
}

final case class GenericDamageInteraction(
                                           target: SourceEntry,
                                           cause: DamageReason,
                                           hitPos: Vector3,
                                           hitTime: Long = System.currentTimeMillis()
                                         ) extends DamageInteraction

final case class ProjectileDamageInteraction(
                                              target: SourceEntry,
                                              cause: DamageReason.Projectile,
                                              hitPos: Vector3,
                                              hitTime: Long = System.currentTimeMillis()
                                            ) extends DamageInteraction

object DamageInteraction {
  def apply(target: SourceEntry, cause: DamageReason, hitPos: Vector3): DamageInteraction = {
    GenericDamageInteraction(target, cause, hitPos)
  }

  object Projectile {
    def unapply(obj: DamageInteraction): Option[ProjectileDamageInteraction] = {
      obj.cause match {
        case o: DamageReason.Projectile => Some(ProjectileDamageInteraction(obj.target, o, obj.hitPos, obj.hitTime))
        case _ => None
      }
    }
  }
}


