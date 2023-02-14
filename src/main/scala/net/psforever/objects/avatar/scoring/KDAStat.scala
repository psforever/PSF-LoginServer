// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.interaction.DamageResult
import org.joda.time.LocalDateTime

trait KDAStat {
  def experienceEarned: Long
  val time: LocalDateTime = LocalDateTime.now()
}

final case class Kill(victim: PlayerSource, info: DamageResult, experienceEarned: Long) extends KDAStat

final case class Assist(victim: PlayerSource, weapons: Seq[Int], damageInflictedPercentage: Float, experienceEarned: Long) extends KDAStat

final case class Death(assailant: Seq[PlayerSource], timeAlive: Long, bep: Long) extends KDAStat {
  def experienceEarned: Long = 0
}
