// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.PlanetSideGameObject
import net.psforever.types.Vector3

import scala.collection.mutable

/**
  * A server object that provides a service, triggered when a certain distance from the unit itself (proximity-based).
  * Unlike conventional terminals, this one is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  */
trait ProximityUnit {
  this: Terminal =>

  /**
    * A list of targets that are currently affected by this proximity unit.
    */
  private var targets: mutable.ListBuffer[PlanetSideGameObject] = mutable.ListBuffer[PlanetSideGameObject]()

  def Targets: Seq[PlanetSideGameObject] = targets toList

  def NumberUsers: Int = targets.size

  /**
    * Accept a new target for this unit.
    * @param target the new target
    * @return `true`, if the entrant has been added and is new to the list;
    *        `false` if the entrant is already in the list or can not be added
    */
  def AddUser(target: PlanetSideGameObject): Boolean = {
    val alreadyContains = targets.contains(target)
    if (!alreadyContains) {
      targets += target
      targets.contains(target)
    } else {
      false
    }
  }

  /**
    * Remove an existing target for this unit.
    * @param target the target
    * @return `true`, if the submitted entity was previously in the list but is not longer in the list;
    *        `false`, if the submitted entity was never in the list or can not be removed
    */
  def RemoveUser(target: PlanetSideGameObject): Boolean = {
    val alreadyContains = targets.contains(target)
    if (alreadyContains) {
      targets -= target
      !targets.contains(target)
    } else {
      false
    }
  }

  /**
    * Confirm whether the entity is a valid target for the effects of this unit.
    * @param target the submitted entity
    * @return `true`, if the entity passes the validation tests;
    *        `false`, otherwise
    */
  def Validate(target: PlanetSideGameObject): Boolean = {
    val proxDef    = Definition.asInstanceOf[ProximityDefinition]
    val radius     = proxDef.UseRadius * proxDef.UseRadius
    val validation = proxDef.Validations
    Validate(radius, validation)(target)
  }

  /**
    * Confirm whether the entity is a valid target for the effects of this unit.
    * Curried to accept parameters for the tests separately from the entity to be tested.
    * In general, the two requirements beyond the custom validations involve
    * distance (from the unit)
    * and inclusiveness (known to the unit beforehand).
    * @param radius the squared minimum activation distance
    * @param validations the custom tests that the entity must pass to be considered valid;
    *                    in general, regardless of the type of the target, any of the tests must be passed
    * @param target the submitted entity
    * @return `true`, if the entity passes the validation tests;
    *        `false`, otherwise
    */
  def Validate(radius: Float, validations: Seq[(PlanetSideGameObject) => Boolean])(
      target: PlanetSideGameObject
  ): Boolean = {
    //org.log4s.getLogger("ProximityUnit").info(s"vehicle: ${Owner.Position}, terminal: $Position, target: ${target.Position}, toOwner: ${Vector3.Distance(Position, Owner.Position)}, toTarget: ${Vector3.Distance(Position, target.Position)}")
    targets.contains(target) && Vector3.DistanceSquared(Position, target.Position) <= radius && validations.exists(p =>
      p(target)
    )
  }
}

object ProximityUnit {
  final case class Action(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject)
}
