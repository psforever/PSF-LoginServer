// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, TelepadDeployable}
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

object TelepadDeployableConverter extends ObjectCreateConverter[TelepadDeployable] {
  override def ConstructorData(obj: TelepadDeployable): Try[DroppedItemData[TelepadDeployableData]] = {
    obj.Router match {
      case Some(Default.GUID0) =>
        Failure(new IllegalStateException("TelepadDeployableConverter: knowledge of associated Router is null"))

      case Some(router) =>
        if (obj.Health > 0) {
          val ownerGuid = GetOwner(obj)
          Success(
            DroppedItemData(
              PlacementData(obj.Position, obj.Orientation),
              TelepadDeployableData(
                CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = Some(router.guid), guid = ownerGuid),
                unk1 = false,
                owner_guid = ownerGuid,
                unk3 = true,
                unk4 = false
              )
            )
          )
        } else {
          Success(
            DroppedItemData(
              PlacementData(obj.Position, obj.Orientation),
              TelepadDeployableData(
                CommonFieldData(obj.Faction, bops = false, alternate = true, v1 = true, v2 = None, jammered = false, v5 = Some(router.guid), guid = Default.GUID0),
                unk1 = false,
                Default.GUID0,
                unk3 = true,
                unk4 = true
              )
            )
          )
        }

      case None =>
        Failure(
          new IllegalStateException("TelepadDeployableConverter: telepad needs to know id of its associated Router")
        )
    }
  }
}
