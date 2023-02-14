// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

final case class Life(
                       kills: Seq[Kill],
                       assists: Seq[Assist],
                       death: Option[Death],
                       equipmentStats: Seq[EquipmentStat]
                     )

object Life {
  def apply(): Life = Life(Nil, Nil, None, Nil)

  def bep(life: Life): Long = {
    life.kills.foldLeft(0L)(_ + _.experienceEarned) + life.assists.foldLeft(0L)(_ + _.experienceEarned)
  }
}
