// Copyright (c) 2020-2024 PSForever
package net.psforever.objects.serverobject.environment

import enumeratum.{Enum, EnumEntry}
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.objects.vital.Vitality
import net.psforever.types.Vector3

/**
 * A general description of environment and its interactive possibilities.
 */
sealed abstract class EnvironmentTrait extends EnumEntry {
  def canInteractWith(obj: PlanetSideGameObject): Boolean
}

object EnvironmentAttribute extends Enum[EnvironmentTrait] {
  /** glue connecting `EnumEntry` to `Enumeration` */
  val values: IndexedSeq[EnvironmentTrait] = findValues

  case object Water extends EnvironmentTrait {
    /** water can only interact with objects that are negatively affected by being exposed to water;
     * it's better this way */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj.Definition.DrownAtMaxDepth || obj.Definition.DisableAtMaxDepth || (obj match {
        case p: Player => p.VehicleSeated.isEmpty
        case v: Vehicle => v.MountedIn.isEmpty
        case _ => true
      })
    }
  }

  case object Lava extends EnvironmentTrait {
    /** lava can only interact with anything capable of registering damage */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj match {
        case o: Vitality => o.Definition.Damageable
        case _ => false
      }
    }
  }

  case object Death extends EnvironmentTrait {
    /** death can only interact with anything capable of registering damage */
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj match {
        case o: Vitality => o.Definition.Damageable
        case _ => false
      }
    }
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
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj match {
        case p: Player  => p.isAlive && p.Position != Vector3.Zero
        case v: Vehicle => !v.Destroyed && v.Position != Vector3.Zero
        case _          => false
      }
    }
  }
}
