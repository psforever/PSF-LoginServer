// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.EffectTarget

import scala.collection.mutable
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * The definition mix-in for any game object that possesses a proximity-based effect.
  * This includes the limited proximity-based functionality of the formal medical terminals
  * and the actual proximity-based functionality of the cavern crystals.
  * Objects created by this definition being linked by their communication
  * between the server and client using `ProximityTerminalUseMessage` game packets.
  */
trait ProximityDefinition {
  private var interval: FiniteDuration = Duration(0, "seconds")
  private val targetValidation: mutable.HashMap[EffectTarget.Category.Value, PlanetSideGameObject => Boolean] =
    new mutable.HashMap[EffectTarget.Category.Value, PlanetSideGameObject => Boolean]()

  def UseRadius: Float

  def Interval: FiniteDuration = interval

  def Interval_=(amount: Int): FiniteDuration = {
    Interval_=(Duration(amount, "milliseconds"))
  }

  def Interval_=(amount: FiniteDuration): FiniteDuration = {
    interval = amount
    Interval
  }

  def TargetValidation: mutable.HashMap[EffectTarget.Category.Value, PlanetSideGameObject => Boolean] = targetValidation

  def Validations: Seq[PlanetSideGameObject => Boolean] = {
    targetValidation.headOption match {
      case Some(_) =>
        targetValidation.values.toSeq
      case None =>
        Seq(EffectTarget.Validation.Invalid)
    }
  }
}
