// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.projectile

import net.psforever.objects.vital.base.{DamageResult, ProjectileDamageInteraction}

/**
  * The base for all projectile-induced damage calculation function literals.
  */
trait ProjectileCalculations {

  /**
    * The exposed entry for the calculation function literal defined by this base.
    * @param data the historical damage information
    * @return the calculated value
    */
  def Calculate(data: DamageResult): Int = {
    data.interaction match {
      case o: ProjectileDamageInteraction => Calculate(o)
      case _ => 0
    }
  }

  def Calculate(data: ProjectileDamageInteraction): Int
}

object ProjectileCalculations {
  type Form = ProjectileDamageInteraction => Int
}
