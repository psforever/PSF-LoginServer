// Copyright (c) 2020-2024 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.objects.vital.Vitality
import net.psforever.types.Vector3

/**
 * A general description of environment and its interactive possibilities.
 */
abstract class EnvironmentTrait {
  def canInteractWith(obj: PlanetSideGameObject): Boolean
}

object EnvironmentAttribute {
  case object Water extends EnvironmentTrait {
    /** water can only interact with objects that are negatively affected by being exposed to water;
     * it's better this way */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj.Definition.DrownAtMaxDepth ||
        obj.Definition.DisableAtMaxDepth ||
        canInteractWithPlayersAndVehicles(obj) ||
        (obj match {
          case p: Player  => p.VehicleSeated.isEmpty
          case v: Vehicle => v.MountedIn.isEmpty
          case _          => false
        })
    }
  }

  case object Lava extends EnvironmentTrait {
    /** lava can only interact with anything capable of registering damage */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = canInteractWithDamagingFields(obj)
  }

  case object Death extends EnvironmentTrait {
    /** death can only interact with anything capable of registering damage */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = canInteractWithDamagingFields(obj)
  }

  case object GantryDenialField
    extends EnvironmentTrait {
    /** only interact with living player characters */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj match {
        case p: Player => p.isAlive
        case _         => false
      }
    }
  }

  case object MovementFieldTrigger
    extends EnvironmentTrait {
    /** only interact with living player characters or vehicles */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = canInteractWithPlayersAndVehicles(obj)
  }

  case object InteriorField
    extends EnvironmentTrait {
    /** only interact with living player characters or vehicles */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = canInteractWithPlayersAndVehicles(obj)
  }

  /**
   * This environment field only interacts with anything capable of registering damage.
   * Also, exclude targets that are located at the game world origin.
   * @param obj target entity
   * @return whether or not this field affects the target entity
   */
  def canInteractWithPlayersAndVehicles(obj: PlanetSideGameObject): Boolean = {
    (obj.Position != Vector3.Zero) ||
      (obj match {
        case p: Player  => p.isAlive
        case v: Vehicle => !v.Destroyed
        case _          => false
      })
  }

  /**
   * This environment field only interacts with living player characters or not-destroyed vehicles.
   * @param obj target entity
   * @return whether or not this field affects the target entity
   */
  def canInteractWithDamagingFields(obj: PlanetSideGameObject): Boolean = {
    obj match {
      case o: Vitality => !o.Destroyed && o.Definition.Damageable
      case _ => false
    }
  }
}
