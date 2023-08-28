// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import enumeratum.values.IntEnumEntry

sealed abstract class EquipmentUseContextWrapper(val value: Int) extends IntEnumEntry {
  def equipment: Int
  def intermediate: Int = 0
}

final case class NoUse(equipment: Int) extends EquipmentUseContextWrapper(value = -1)

final case class DamageWith(equipment: Int) extends EquipmentUseContextWrapper(value = 0)

final case class Destroyed(equipment: Int) extends EquipmentUseContextWrapper(value = 1)
final case class ReviveAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 4)
final case class AmenityDestroyed(equipment: Int) extends EquipmentUseContextWrapper(value = 10)
final case class DriverKilled(equipment: Int) extends EquipmentUseContextWrapper(value = 12)
final case class GunnerKilled(equipment: Int) extends EquipmentUseContextWrapper(value = 13)
final case class PassengerKilled(equipment: Int) extends EquipmentUseContextWrapper(value = 14)
final case class CargoDestroyed(equipment: Int) extends EquipmentUseContextWrapper(value = 15)
final case class DriverAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 18)
final case class HealKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 20)
final case class ReviveKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 21)
final case class RepairKillAssist(equipment: Int, override val intermediate: Int) extends EquipmentUseContextWrapper(value = 22)
final case class AmsRespawnKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 23)
final case class HotDropKillAssist(equipment: Int, override val intermediate: Int) extends EquipmentUseContextWrapper(value = 24)
final case class HackKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 25)
final case class LodestarRearmKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 26)
final case class AmsResupplyKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 27)
final case class RouterKillAssist(equipment: Int) extends EquipmentUseContextWrapper(value = 28)
