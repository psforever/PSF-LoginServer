// Copyright (c) 2021 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class RadiationCloudConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj: Projectile): Try[RadiationCloudData] = {
    Success(RadiationCloudData(PlacementData(obj.Position, obj.Orientation), obj.owner.Faction))
  }

  override def DetailedConstructorData(obj: Projectile): Try[RadiationCloudData] =
    Failure(new Exception("RadiationCloudConverter should not be used to generate detailed RadiationCloudData (nothing should)"))
}
