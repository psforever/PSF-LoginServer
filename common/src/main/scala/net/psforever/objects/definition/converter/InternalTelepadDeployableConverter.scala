// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.TelepadLike
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire

import scala.util.{Failure, Success, Try}

class InternalTelepadDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with TelepadLike]() {
  override def ConstructorData(obj : PlanetSideGameObject with TelepadLike) : Try[InternalTelepadDeployableData] = {
    obj.Router match {
      case Some(router) =>
        Success(
          InternalTelepadDeployableData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL,
              false,
              false,
              true,
              None,
              false,
              None,
              Some(router.guid),
              PlanetSideGUID(0)
            )
          )
        )
      case None =>
        Failure(new IllegalStateException("InternalTelepadDeployableConverter: telepad needs to know id of its router"))
    }
  }
}
