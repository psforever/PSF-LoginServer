// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.projectile

import net.psforever.objects.ballistics.{Projectile => ActualProjectile}
import net.psforever.objects.vital.base._
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, ResolutionCalculations}

final case class ProjectileReason(
                                   resolution : DamageResolution.Value,
                                   projectile: ActualProjectile,
                                   damageModel: DamageAndResistance
                                 ) extends DamageReason {
  def source: DamageProperties = projectile.profile.asInstanceOf[DamageProperties]

  def same(test: DamageReason): Boolean = {
    test match {
      case o: ProjectileReason => o.projectile.id == projectile.id
      case _ => false
    }
  }

  override def staticModifiers: List[DamageProfile] = List(projectile.fire_mode.Add)

  override def unstructuredModifiers: List[DamageModifiers.Mod] = projectile.fire_mode.Modifiers

  override def calculate(data: DamageInteraction): ResolutionCalculations.Output = {
    damageModel.calculate(data)
  }

  override def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output = {
    damageModel.calculate(data, dtype)
  }
}
