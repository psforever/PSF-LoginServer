// Copyright (c) 2021 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.vehicles.VehicleSubsystemEntry
import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.types.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class BattleFrameRoboticsConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj: Vehicle): Try[BattleFrameRoboticsData] =
    Failure(new Exception("BattleFrameRoboticsConverter should not be used to generate detailed BattleFrameRoboticsData (nothing should)"))

  override def ConstructorData(obj: Vehicle): Try[BattleFrameRoboticsData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if(health > 0) { //active
      Success(
        BattleFrameRoboticsData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          CommonFieldData(
            obj.Faction,
            bops = false,
            alternate = false,
            v1 = true,
            v2 = None,
            jammered = obj.Jammed,
            v4 = None,
            v5 = None,
            obj.Owner match {
              case Some(owner) => owner
              case None => PlanetSideGUID(0)
            }
          ),
          health,
          StatConverter.Health(obj.Shields, obj.MaxShields),
          unk1 = 0,
          unk2 = false,
          no_mount_points = false,
          driveState = 60,
          proper_anim = true,
          unk3 = 0,
          show_bfr_shield = showBfrShield(obj),
          unk4 = None,
          Some(InventoryData(MakeDriverSeat(obj) ++ MakeUtilities(obj) ++ MakeMountings(obj)))
        )
      )
    }
    else { //destroyed
      Success(
        BattleFrameRoboticsData(
          PlacementData(obj.Position, obj.Orientation),
          CommonFieldData(
            obj.Faction,
            bops = false,
            alternate = false,
            v1 = true,
            v2 = None,
            jammered = false,
            v4 = None,
            v5 = None,
            guid = PlanetSideGUID(0)
          ),
          0,
          0,
          unk1 = 0,
          unk2 = false,
          no_mount_points = false,
          driveState = 0,
          proper_anim = true,
          unk3 = 0,
          show_bfr_shield = false,
          unk4 = None,
          inventory = None
        )
      )
    }
  }

  private def MakeDriverSeat(obj: Vehicle): List[InventoryItemData.InventoryItem] = {
    val offset: Long = MountableInventory.InitialStreamLengthToSeatEntries(obj.Velocity.nonEmpty, VehicleFormat.Battleframe)
    obj.Seats(0).occupant match {
      case Some(player) =>
        List(InventoryItemData(ObjectClass.avatar, player.GUID, 0, SeatConverter.MakeSeat(player, offset)))
      case None =>
        Nil
    }
  }

  private def MakeMountings(obj: Vehicle): List[InventoryItemData.InventoryItem] = {
    obj.Weapons.collect {
      case (index, slot: EquipmentSlot) if slot.Equipment.nonEmpty =>
        val equip: Equipment = slot.Equipment.get
        val equipDef         = equip.Definition
        InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
    }.toList
  }

  protected def MakeUtilities(obj: Vehicle): List[InventoryItemData.InventoryItem] = {
    Vehicle
      .EquipmentUtilities(obj.Utilities)
      .map({
        case (index, utilContainer) =>
          val util: PlanetSideGameObject = utilContainer()
          val utilDef                    = util.Definition
          InventoryItemData(utilDef.ObjectId, util.GUID, index, utilDef.Packet.ConstructorData(util).get)
      })
      .toList
  }

  def showBfrShield(obj: Vehicle): Boolean = {
    obj.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator).get.Enabled && obj.Shields > 0
  }
}
