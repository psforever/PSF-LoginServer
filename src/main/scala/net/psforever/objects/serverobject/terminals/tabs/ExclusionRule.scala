// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.definition.{EquipmentDefinition, VehicleDefinition}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

trait ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean
}

final case class NoExoSuitRule(illegalSuit: ExoSuitType.Value, illegalSubtype: Int = 0) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case exosuit: ExoSuitType.Value                 => exosuit == illegalSuit
      case (exosuit: ExoSuitType.Value, subtype: Int) => exosuit == illegalSuit && subtype == illegalSubtype
      case _                                          => false
    }
  }
}

final case class NoEquipmentRule(illegalDefinition: EquipmentDefinition) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case equipment: Equipment => equipment.Definition eq illegalDefinition
      case _                    => false
    }
  }
}

case object NoCavernEquipmentRule extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case equipment: Equipment => GlobalDefinitions.isCavernWeapon(equipment.Definition)
      case _                    => false
    }
  }
}

case object CavernEquipmentQuestion extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case equipment: Equipment =>
        import net.psforever.objects.serverobject.structures.Building
        if(GlobalDefinitions.isCavernWeapon(equipment.Definition)) {
          (player.Zone.GUID(msg.terminal_guid) match {
            case Some(term: Amenity) => Some(term.Owner)
            case _                   => None
          }) match {
            case Some(b: Building) => b.connectedCavern().isEmpty
            case _                 => true
          }
        } else {
          false
        }
      case _ =>
        false
    }
  }
}

final case class NoVehicleRule(illegalDefinition: VehicleDefinition) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case vehicleDef: VehicleDefinition => vehicleDef eq illegalDefinition
      case _                => false
    }
  }
}

case object CavernVehicleQuestion extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case vehicle: Vehicle =>
        import net.psforever.objects.serverobject.structures.Building
        if(GlobalDefinitions.isCavernVehicle(vehicle.Definition)) {
          (player.Zone.GUID(msg.terminal_guid) match {
            case Some(term: Amenity) => Some(term.Owner)
            case _                   => None
          }) match {
            case Some(b: Building) => b.connectedCavern().isEmpty
            case _                 => true
          }
        } else {
          false
        }
      case _ =>
        false
    }
  }
}
