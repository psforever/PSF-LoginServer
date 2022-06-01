// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.definition.{EquipmentDefinition, VehicleDefinition}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

/**
  * An allowance test to be utilized by tabs and pages of an order terminal.
  * @see `ScrutinizedTab`
  */
trait ExclusionRule {
  /**
    * An allowance test to be utilized by tabs and pages of an order terminal.
    * @param player the player
    * @param msg the original order message from the client
    * @param obj the produced item that is being tested
    * @return `true`, if the item qualifies this test and should be excluded;
    *        `false` if the item did not pass the test and can be included
    */
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean
}

/**
  * Do not allow the player to don certain exo-suits.
  * @param illegalSuit the banned exo-suit type
  * @param illegalSubtype the banned exo-suit subtype
  */
final case class NoExoSuitRule(illegalSuit: ExoSuitType.Value, illegalSubtype: Int = 0) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case exosuit: ExoSuitType.Value                 => exosuit == illegalSuit
      case (exosuit: ExoSuitType.Value, subtype: Int) => exosuit == illegalSuit && subtype == illegalSubtype
      case _                                          => false
    }
  }
}

/**
  * Do not allow the player to acquire certain equipment.
  * @param illegalDefinition the definition entry for the specific type of equipment
  */
final case class NoEquipmentRule(illegalDefinition: EquipmentDefinition) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case equipment: Equipment => equipment.Definition eq illegalDefinition
      case _                    => false
    }
  }
}

/**
  * Do not allow cavern equipment.
  */
case object NoCavernEquipmentRule extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case equipment: Equipment => GlobalDefinitions.isCavernWeapon(equipment.Definition)
      case _                    => false
    }
  }
}

/**
  * Do not allow the player to spawn cavern equipment if not pulled from a facility and
  * only if the facility is subject to the benefit of an appropriate cavern perk.
  */
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

/**
  * Do not allow the player to acquire certain vehicles.
  * @param illegalDefinition the definition entry for the specific type of vehicle
  */
final case class NoVehicleRule(illegalDefinition: VehicleDefinition) extends ExclusionRule {
  def checkRule(player: Player, msg: ItemTransactionMessage, obj: Any): Boolean = {
    obj match {
      case vehicleDef: VehicleDefinition => vehicleDef eq illegalDefinition
      case _                => false
    }
  }
}

/**
  * Do not allow the player to spawn cavern vehicles if not pulled from a facility and
  * only if the facility is subject to the benefit of an appropriate cavern perk.
  */
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
