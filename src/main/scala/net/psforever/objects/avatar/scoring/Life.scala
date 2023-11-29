// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

final case class Life(
                       kills: Seq[Kill],
                       assists: Seq[Assist],
                       death: Option[Death],
                       equipmentStats: Seq[EquipmentStat],
                       supportExperience: Long,
                       prior: Option[Life]
                     )

object Life {
  def apply(): Life = Life(Nil, Nil, None, Nil, 0, None)

  def revive(prior: Life): Life = Life(Nil, Nil, None, Nil, 0, Some(prior))

  def bep(life: Life): Long = {
    life.kills.foldLeft(0L)(_ + _.experienceEarned) + life.assists.foldLeft(0L)(_ + _.experienceEarned)
  }
}
