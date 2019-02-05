// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition

import scala.collection.mutable

/**
  * The definition mix-in for any game object that possesses a proximity-based effect.
  * This includes the limited proximity-based functionality of the formal medical terminals
  * and the actual proximity-based functionality of the cavern crystals.
  * Objects created by this definition being linked by their communication
  * between the server and client using `ProximityTerminalUseMessage` game packets.
  */
trait ProximityDefinition {
  this : ObjectDefinition =>

  private var useRadius : Float = 0f //TODO belongs on a wider range of object definitions
  private val targetValidation : mutable.HashMap[ProximityTarget.Value, PlanetSideGameObject=>Boolean] = new mutable.HashMap[ProximityTarget.Value, PlanetSideGameObject=>Boolean]()

  def UseRadius : Float = useRadius

  def UseRadius_=(radius : Float) : Float = {
    useRadius = radius
    UseRadius
  }

  def TargetValidation : mutable.HashMap[ProximityTarget.Value, PlanetSideGameObject=>Boolean] = targetValidation

  def Validations : Seq[PlanetSideGameObject=>Boolean] = {
    targetValidation.headOption match {
      case Some(_) =>
        targetValidation.values.toSeq
      case None =>
        Seq(ProximityDefinition.Invalid)
    }
  }
}

object ProximityDefinition {
  protected val Invalid : PlanetSideGameObject=>Boolean = (_ : PlanetSideGameObject) => false
}
