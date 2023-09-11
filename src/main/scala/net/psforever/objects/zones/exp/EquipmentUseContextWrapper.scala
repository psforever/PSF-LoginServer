// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import enumeratum.values.IntEnumEntry

sealed abstract class EquipmentUseContextWrapper(val value: Int) extends IntEnumEntry {
  def equipment: Int
  def intermediate: Int
}

sealed abstract class NoIntermediateUseContextWrapper(override val value: Int)
  extends EquipmentUseContextWrapper(value) {
  def intermediate: Int = 0
}

final case class NoUse() extends NoIntermediateUseContextWrapper(value = -1) {
  def equipment: Int = 0
}

final case class DamageWith(equipment: Int) extends NoIntermediateUseContextWrapper(value = 0)

final case class Destroyed(equipment: Int) extends NoIntermediateUseContextWrapper(value = 1)
final case class ReviveAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 4)
final case class AmenityDestroyed(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 10)
final case class DriverKilled(equipment: Int) extends NoIntermediateUseContextWrapper(value = 12)
final case class GunnerKilled(equipment: Int) extends NoIntermediateUseContextWrapper(value = 13)
final case class PassengerKilled(equipment: Int) extends NoIntermediateUseContextWrapper(value = 14)
final case class CargoDestroyed(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 15)
final case class DriverAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 18)
final case class HealKillAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 20)
final case class ReviveKillAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 21)
final case class RepairKillAssist(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 22)
final case class AmsRespawnKillAssist(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 23)
final case class HotDropKillAssist(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 24)
final case class HackKillAssist(equipment: Int, intermediate: Int) extends EquipmentUseContextWrapper(value = 25)
final case class LodestarRearmKillAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 26)
final case class AmsResupplyKillAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 27)
final case class RouterKillAssist(equipment: Int) extends NoIntermediateUseContextWrapper(value = 28)
