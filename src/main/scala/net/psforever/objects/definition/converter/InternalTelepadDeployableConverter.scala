// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, PlanetSideGameObject}
import net.psforever.objects.ce.TelepadLike
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire

import scala.util.{Failure, Success, Try}

object InternalTelepadDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with TelepadLike] {
  override def ConstructorData(obj: PlanetSideGameObject with TelepadLike): Try[TelepadDeployableData] = {
    obj.Router match {
      case Some(Default.GUID0) =>
        Failure(new IllegalStateException("InternalTelepadDeployableConverter: knowledge of parent Router is null"))

      case Some(router) =>
        Success(
          TelepadDeployableData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = Some(router.guid), guid = Default.GUID0),
            unk1 = true,
            owner_guid = Default.GUID0,
            unk3 = false,
            unk4 = false
          )
        )

      case None =>
        Failure(
          new IllegalStateException("InternalTelepadDeployableConverter: telepad needs to know id of its parent Router")
        )
    }
  }
}
