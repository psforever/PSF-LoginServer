// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.objects.TurretDeployable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class FieldTurretConverter extends ObjectCreateConverter[TurretDeployable]() {
  override def ConstructorData(obj : TurretDeployable) : Try[OneMannedFieldTurretData] = {
    val health = 255 * obj.Health / obj.MaxHealth //TODO not precise
    if(health > 3) {
      Success(
        OneMannedFieldTurretData(
          SmallDeployableData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = false,
            unk1 = 0,
            obj.Jammered,
            unk2 = false,
            obj.Owner match {
              case Some(owner) => owner
              case None => PlanetSideGUID(0)
            }
          ),
          health,
          Some(InventoryData(FieldTurretConverter.MakeMountings(obj)))
        )
      )
    }
    else {
      Success(
        OneMannedFieldTurretData(
          SmallDeployableData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = true,
            unk1 = 0,
            jammered = false,
            unk2 = false,
            owner_guid = PlanetSideGUID(0)
          ),
          0,
          None
        )
      )
    }
  }

  override def DetailedConstructorData(obj : TurretDeployable) : Try[OneMannedFieldTurretData] =
    Failure(new Exception("converter should not be used to generate detailed OneMannedFieldTurretData"))
}

object FieldTurretConverter {
  private def MakeMountings(obj : WeaponTurret) : List[InventoryItemData.InventoryItem] = {
    obj.Weapons.map({
      case((index, slot)) =>
        val equip : Equipment = slot.Equipment.get
        val equipDef = equip.Definition
        InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
    }).toList
  }
}
