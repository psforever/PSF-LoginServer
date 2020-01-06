// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.TelepadLike
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Failure, Success, Try}

class InternalTelepadDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with TelepadLike]() {
  override def ConstructorData(obj : PlanetSideGameObject with TelepadLike) : Try[TelepadDeployableData] = {
    obj.Router match {
      case Some(PlanetSideGUID(0)) =>
        Failure(new IllegalStateException("InternalTelepadDeployableConverter: knowledge of parent Router is null"))

      case Some(router) =>
        Success(
          TelepadDeployableData(
            CommonFieldData(
              PlanetSideEmpire.NEUTRAL,
              bops = false,
              alternate = false,
              true,
              None,
              false,
              None,
              Some(router.guid),
              PlanetSideGUID(0)
            ),
            unk1 = 128,
            unk2 = 0
          )
        )

      case None =>
        Failure(new IllegalStateException("InternalTelepadDeployableConverter: telepad needs to know id of its parent Router"))
    }
  }
}
