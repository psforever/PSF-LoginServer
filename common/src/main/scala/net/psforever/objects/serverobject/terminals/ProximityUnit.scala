// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.PlanetSideGameObject
import net.psforever.types.Vector3

/**
  * A server object that provides a service, triggered when a certain distance from the unit itself (proximity-based).
  * Unlike conventional terminals, this one is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  */
trait ProximityUnit {
  this : Terminal =>

  /**
    * A list of targets that are currently affected by this proximity unit.
    */
  private var targets : Set[PlanetSideGameObject] = Set.empty

  def Targets : Seq[PlanetSideGameObject] = targets toSeq

  def NumberUsers : Int = targets.size

  def AddUser(target : PlanetSideGameObject) : Int = {
    targets += target
    NumberUsers
  }

  def RemoveUser(target : PlanetSideGameObject) : Int = {
    targets -= target
    NumberUsers
  }

  def Validate(target : PlanetSideGameObject) : Boolean = {
    val proxDef = Definition.asInstanceOf[ProximityDefinition]
    val radius = proxDef.UseRadius * proxDef.UseRadius
    val validation = proxDef.Validations
    Validate(radius, validation)(target)
  }

  def Validate(radius : Float, validations : Seq[(PlanetSideGameObject)=>Boolean])(target : PlanetSideGameObject) : Boolean = {
    targets.contains(target) && Vector3.DistanceSquared(Position, target.Position) <= radius && validations.exists(p => p(target))
  }
}

object ProximityUnit {
  final case class ProximityAction(terminal : Terminal with ProximityUnit, target : PlanetSideGameObject)
}
