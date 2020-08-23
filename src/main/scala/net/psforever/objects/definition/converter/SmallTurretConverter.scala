// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.TurretDeployable
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class SmallTurretConverter extends ObjectCreateConverter[TurretDeployable]() {
  override def ConstructorData(obj: TurretDeployable): Try[SmallTurretData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 0) {
      Success(
        SmallTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = false,
              false,
              None,
              jammered = obj.Jammed,
              Some(true),
              None,
              obj.Owner match {
                case Some(owner) => owner
                case None        => PlanetSideGUID(0)
              }
            )
          ),
          health,
          Some(InventoryData(SmallTurretConverter.MakeMountings(obj)))
        )
      )
    } else {
      Success(
        SmallTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = true,
              false,
              None,
              false,
              Some(false),
              None,
              PlanetSideGUID(0)
            )
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj: TurretDeployable): Try[SmallTurretData] =
    Failure(new Exception("converter should not be used to generate detailed SmallTurretData"))
}

object SmallTurretConverter {
  private def MakeMountings(obj: WeaponTurret): List[InventoryItemData.InventoryItem] = {
    obj.Weapons
      .map({
        case ((index, slot)) =>
          val equip: Equipment = slot.Equipment.get
          val equipDef         = equip.Definition
          InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
      })
      .toList
  }
}
