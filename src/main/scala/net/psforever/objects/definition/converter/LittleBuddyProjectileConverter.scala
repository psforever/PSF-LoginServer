// Copyright (c) 2022 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Success, Try}

class LittleBuddyProjectileConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj: Projectile): Try[LittleBuddyProjectileData] = lilBudData(obj)

  override def DetailedConstructorData(obj: Projectile): Try[LittleBuddyProjectileData] = lilBudData(obj)

  private def lilBudData(obj: Projectile): Try[LittleBuddyProjectileData] = {
    Success(
      LittleBuddyProjectileData(
        CommonFieldDataWithPlacement(
          PlacementData(
            obj.Position,
            obj.Orientation,
            obj.Velocity
          ),
          CommonFieldData(
            obj.owner.Faction,
            bops = false,
            alternate = false,
            v1 = true,
            v2 = None,
            jammered = false,
            v4 = None,
            v5 = None,
            guid = PlanetSideGUID(0)
          )
        ),
        u2 = 0,
        u4 = true
      )
    )
  }
}
