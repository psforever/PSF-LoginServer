// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
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
  this: ObjectDefinition =>

  private var interval: FiniteDuration = Duration(0, "seconds")
  private var useRadius: Float = 0f //TODO belongs on a wider range of object definitions
  private val targetValidation: mutable.HashMap[EffectTarget.Category.Value, PlanetSideGameObject => Boolean] =
    new mutable.HashMap[EffectTarget.Category.Value, PlanetSideGameObject => Boolean]()

  def Interval: FiniteDuration = interval

  def Interval_=(amount: Int): FiniteDuration = {
    Interval_=(Duration(amount, "milliseconds"))
  }

  def Interval_=(amount: FiniteDuration): FiniteDuration = {
    interval = amount
    Interval
  }

  def UseRadius: Float = useRadius

  def UseRadius_=(radius: Float): Float = {
    useRadius = radius
    UseRadius
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
