// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.{PlayerSource, SourceUniqueness}
import net.psforever.objects.vital.InGameActivity
import net.psforever.objects.zones.exp.ContributionStats
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

//noinspection ScalaUnusedSymbol
abstract class RecoveryExperienceContributionProcess(
                                                              faction : PlanetSideEmpire.Value,
                                                              contributions: Map[SourceUniqueness, List[InGameActivity]]
                                                            ) extends RecoveryExperienceContribution {
  protected var damageInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
  protected var recoveryInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
  protected val contributionsBy: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
  protected val participants: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
  protected val damageParticipants: mutable.LongMap[PlayerSource] = mutable.LongMap[PlayerSource]()

  def submit(history: List[InGameActivity]): Unit

  def output(): mutable.LongMap[ContributionStats] = {
    val output = participants.map { a => a }
    clear()
    output
  }

  def clear(): Unit = {
    damageInOrder = Nil
    recoveryInOrder = Nil
    contributionsBy.clear()
    participants.clear()
    damageParticipants.clear()
  }
}
