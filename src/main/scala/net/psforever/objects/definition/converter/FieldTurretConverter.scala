// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.objects.TurretDeployable
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class FieldTurretConverter extends ObjectCreateConverter[TurretDeployable]() {
  override def ConstructorData(obj: TurretDeployable): Try[OneMannedFieldTurretData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 3) {
      Success(
        OneMannedFieldTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              faction = obj.Faction,
              bops = false,
              alternate = false,
              v1 = true,
              v2 = Some(CommonFieldDataExtra.Default),
              jammered = obj.Jammed,
              v4 = None,
              v5 = None,
              guid = obj.OwnerGuid.getOrElse(PlanetSideGUID(0))
            )
          ),
          health,
          Some(InventoryData(FieldTurretConverter.MakeMountings(obj)))
        )
      )
    } else {
      Success(
        OneMannedFieldTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              faction = obj.Faction,
              bops = false,
              alternate = true,
              v1 = true,
              v2 = Some(CommonFieldDataExtra.Default),
              jammered = obj.Jammed,
              v4 = None,
              v5 = None,
              guid = PlanetSideGUID(0)
            )
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj: TurretDeployable): Try[OneMannedFieldTurretData] =
    Failure(new Exception("converter should not be used to generate detailed OneMannedFieldTurretData"))
}

object FieldTurretConverter {
  private def MakeMountings(obj: WeaponTurret): List[InventoryItemData.InventoryItem] = {
    obj.Weapons
      .map({
        case (index, slot) =>
          val equip: Equipment = slot.Equipment.get
          val equipDef         = equip.Definition
          InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
      })
      .toList
  }
}
