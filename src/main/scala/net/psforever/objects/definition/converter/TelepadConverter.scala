// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, Telepad}
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedConstructionToolData, HandheldData}

import scala.util.{Failure, Success, Try}

object TelepadConverter extends ObjectCreateConverter[Telepad] {
  override def ConstructorData(obj: Telepad): Try[HandheldData] = {
    obj.Router match {
      case Some(router) =>
        Success(
          HandheldData(
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = None, jammered = false, v5 = Some(router.guid), guid = Default.GUID0)
          )
        )
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }

  override def DetailedConstructorData(obj: Telepad): Try[DetailedConstructionToolData] = {
    obj.Router match {
      case Some(router) =>
        Success(
          DetailedConstructionToolData(
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = Some(router.guid), guid = Default.GUID0)
          )
        )
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }
}
