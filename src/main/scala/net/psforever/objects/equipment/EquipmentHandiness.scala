// Copyright (c) 2021 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.definition.EquipmentDefinition
import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class Hand(val value: String) extends StringEnumEntry

object Handiness extends StringEnum[Hand] {
  val values = findValues

  case object Generic extends Hand(value = "Generic")
  case object Left extends Hand(value = "Left")
  case object Right extends Hand(value = "Right")
}

final case class EquipmentHandiness(
                                      generic: EquipmentDefinition,
                                      left: EquipmentDefinition,
                                      right: EquipmentDefinition
                                    ) {
  def transform(handiness: Hand): EquipmentDefinition = {
    handiness match {
      case Handiness.Generic => generic
      case Handiness.Left    => left
      case Handiness.Right   => right
    }
  }

  def contains(findDef: EquipmentDefinition): Boolean = {
    generic == findDef || left == findDef || right == findDef
  }
}
