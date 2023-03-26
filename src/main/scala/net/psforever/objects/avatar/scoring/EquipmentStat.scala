// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

final case class EquipmentStat(objectId: Int, shotsFired: Int, shotsLanded: Int, kills: Int, assists: Int)

object EquipmentStat {
  def apply(objectId: Int): EquipmentStat = EquipmentStat(objectId, 0, 1, 0, 0)
}
