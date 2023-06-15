// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef
import net.psforever.objects.avatar.scoring.{Assist, Kill}
import net.psforever.objects.serverobject.hackable.Hackable.HackInfo
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource, SourceEntry, SourceUniqueness, VehicleSource}
import net.psforever.objects.vital.{Contribution, InGameActivity, RevivingActivity, TerminalUsedActivity, VehicleDismountActivity}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

object KillContributions {
  final val RecoveryItems: Seq[Int] = {
    import net.psforever.objects.GlobalDefinitions._
    Seq(
      bank,
      nano_dispenser,
      medicalapplicator,
      medical_terminal,
      adv_med_terminal,
      crystals_health_a,
      crystals_health_b
    ).collect { _.ObjectId }
  }

  private[exp] def rewardTheseSupporters(
                                          target: PlayerSource,
                                          history: Iterable[InGameActivity],
                                          kill: Kill,
                                          bep: Long,
                                          eventBus: ActorRef
                                        ): Unit = {
    //setup
    val killTime = kill.time.toDate.getTime
    val faction = target.Faction
    val limitedHistory = limitHistoryToThisLife(history.toList, killTime)
    //divide by applicable time periods (long=10minutes, short=5minutes)
    val shortPeriod = killTime - 300000L
    val (contributions, (longHistory, shortHistory)) = {
      val (contrib, onlyHistory) = limitedHistory.partition { _.isInstanceOf[Contribution] }
      (
        contrib
          .collect { case Contribution(unique, entries) => (unique, entries) }
          .toMap[SourceUniqueness, List[InGameActivity]],
        onlyHistory.partition { _.time > shortPeriod }
      )
    }
    //sort by applicable time periods, as long as the longer period is represented by activity
    val otherContributionCalculations = contributionScoringAndCulling(faction, kill, Seq(target.CharId), contributions, bep)(_, _)
    val finalContributions = if (longHistory.isEmpty) {
      val contributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      contributionProcess.submit(shortHistory)
      val contributionEntries = otherContributionCalculations(shortHistory, contributionProcess.output())
      contributionEntries
        .keys
        .map { composeContributionOutput(_, contributionEntries, contributionEntries, bep) }
    } else {
      val longContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      val shortContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Seq(longContributionProcess))
      longContributionProcess.submit(longHistory)
      shortContributionProcess.submit(shortHistory)
      val longContributionEntries = otherContributionCalculations(longHistory, longContributionProcess.output())
      val shortContributionEntries = otherContributionCalculations(shortHistory, shortContributionProcess.output())
      (longContributionEntries.keys ++ shortContributionEntries.keys)
        .toSeq
        .distinct
        .map { composeContributionOutput(_, shortContributionEntries, longContributionEntries, bep) }
    }
    //take the output and transform that into contribution distribution data
    val victim = kill.victim
    finalContributions.foreach { case (charId, ContributionStatsOutput(player, weapons, exp)) =>
      eventBus ! AvatarServiceMessage(
        player.Name,
        AvatarAction.UpdateKillsDeathsAssists(charId, Assist(victim, weapons, 1f, exp.toLong))
      )
    }
  }

  /**
   * Only historical activity that falls within the valid period matters.<br>
   * Unlike an expected case where the history would be bound by being spawned and being killed, respectively,
   * this imposes only the long contribution time limit on events since the latest entry;
   * and, it may stop some time after the otherwise closest activity for being spawned.
   * @param history the original history
   * @param eventTime from which time to start counting backwards
   * @return the potentially truncated history
   */
  private def limitHistoryToThisLife(history: List[InGameActivity], eventTime: Long): List[InGameActivity] = {
    val longLimit: Long = eventTime - 600000L
    history.collect {
      case event if event.time < eventTime && event.time >= longLimit => event
    }
  }

  private def contributionScoringAndCulling(
                                             faction: PlanetSideEmpire.Value,
                                             kill: Kill,
                                             excludeTargets: Seq[Long],
                                             contributions: Map[SourceUniqueness, List[InGameActivity]],
                                             bep: Long
                                           )
                                           (
                                             history: List[InGameActivity],
                                             contributionEntries: mutable.LongMap[ContributionStats]
                                           ): mutable.LongMap[ContributionStats] = {
    contributionEntries.map { case (id, stat) =>
      val newWeaponStats = stat.weapons.map { weaponStat =>
        weaponStat.copy(contributions = (weaponStat.shots + (bep * weaponStat.amount / stat.total)).toFloat)
      }
      contributionEntries.put(id, stat.copy(weapons = newWeaponStats))
    }
    contributeWithKillWhileMountedActivity(faction, kill, history, contributionEntries)
    contributeWithVehicleTransportActivity(history, contributionEntries)
    contributeWithRevivalActivity(history, contributionEntries)
    contributeWithTerminalActivity(faction, history, contributions, contributionEntries)
    contributionEntries.remove(0)
    excludeTargets.foreach { contributionEntries.remove }
    contributionEntries
  }

  private def contributeWithKillWhileMountedActivity(
                                                      faction: PlanetSideEmpire.Value,
                                                      kill: Kill,
                                                      history: List[InGameActivity],
                                                      participants: mutable.LongMap[ContributionStats]
                                                    ): List[InGameActivity] = {
    (kill
      .info
      .interaction
      .cause match {
      case p: ProjectileReason => p.projectile.mounted_in.map { _._2 }
      case _ => None
    })
      .collect {
        case mount: VehicleSource =>
          //repairs
          val contributions = history
            .collect { case Contribution(unique, entries) if mount.unique == unique => (unique, entries) }
            .toMap[SourceUniqueness, List[InGameActivity]]
          contributions
            .foreach {
              case (_, localHistory) =>
                val process = new ArmorRecoveryExperienceContributionProcess(faction, contributions)
                process.submit(localHistory)
                cullContributorImplements(process.output()).foreach {
                  case (id, stat) =>
                    val killExperience = kill.experienceEarned
                    val newWeapons = stat.weapons.map { weapon =>
                      val shots = weapon.shots
                      val amount = weapon.amount
                      val exp = math.min(0.5d * killExperience, shots + math.ceil(math.log(amount))).toFloat
                      weapon.copy(contributions = exp)
                    }.toList
                    contributeWithCombinedActivity(
                      id,
                      newWeapons,
                      stat.player,
                      stat.amount,
                      stat.total,
                      stat.shots,
                      stat.time,
                      participants
                    )
                }
            }
          //vehicle owner
          extractContributionsForEntityByUser(faction, mount, contributions, participants)
          mount.owner
            .collect {
              case owner =>
                val time = kill.time.toDate.getTime
                participants.getOrElseUpdate(
                  owner.CharId,
                  ContributionStats(owner, Seq(WeaponStats(0, 1, 1, time, 10f)), 1, 1, 1, time)
                )
            }
      }
    history
  }

  private def contributeWithVehicleTransportActivity(
                                                      history: List[InGameActivity],
                                                      participants: mutable.LongMap[ContributionStats]
                                                    ): List[InGameActivity] = {
    val shortHistory = history.collect { case out: VehicleDismountActivity => out }
    shortHistory
      .groupBy { a => a.vehicle.owner }
      .foreach { case (Some(owner), dismountsFromVehicle) =>
        val numberOfDismounts = dismountsFromVehicle.size
        contributeWithCombinedActivity(
          owner.CharId,
          dismountsFromVehicle.map { act => WeaponStats(act.vehicle.Definition.ObjectId, 1, 1, act.time, 15f) },
          dismountsFromVehicle.head.vehicle.owner.get,
          numberOfDismounts,
          numberOfDismounts,
          numberOfDismounts,
          dismountsFromVehicle.maxBy(_.time).time,
          participants
        )
      }
    shortHistory
  }

  private def contributeWithRevivalActivity(
                                             history: List[InGameActivity],
                                             participants: mutable.LongMap[ContributionStats]
                                           ): List[InGameActivity] = {
    val shortHistory = history.collect { case rev: RevivingActivity => rev }
    shortHistory
      .groupBy(_.user.CharId)
      .foreach { case (id, revivesByThisPlayer) =>
        val numberOfRevives = revivesByThisPlayer.size
        contributeWithCombinedActivity(
          id,
          revivesByThisPlayer.map { stat => WeaponStats(stat.equipment.ObjectId, 100, 1, stat.time, 25f) },
          revivesByThisPlayer.head.user,
          amount = 100 * numberOfRevives,
          total = 100 * numberOfRevives,
          numberOfRevives,
          revivesByThisPlayer.maxBy(_.time).time,
          participants
        )
      }
    shortHistory
  }

  private def contributeWithTerminalActivity(
                                              faction: PlanetSideEmpire.Value,
                                              history: List[InGameActivity],
                                              contributions: Map[SourceUniqueness, List[InGameActivity]],
                                              participants: mutable.LongMap[ContributionStats]
                                            ): List[InGameActivity] = {
    contributeWithTerminalActivity(
      history.collect { case t: TerminalUsedActivity => (t, t.terminal, t.terminal.hacked) },
      faction,
      contributions,
      participants
    )
  }

  private[exp] def contributeWithTerminalActivity(
                                                   data: Seq[(InGameActivity, AmenitySource, Option[HackInfo])],
                                                   faction: PlanetSideEmpire.Value,
                                                   contributions:  Map[SourceUniqueness, List[InGameActivity]],
                                                   participants: mutable.LongMap[ContributionStats]
                                                 ): List[InGameActivity] = {
    data.collect {
      case (t, terminal, Some(info)) if terminal.Faction != faction =>
        participants.getOrElseUpdate(
          info.player.CharId,
          ContributionStats(info.player, Seq(WeaponStats(0, 1, 1, t.time, 10f)), 1, 1, 1, t.time)
        )
        t
      case (t, terminal, _) =>
        extractContributionsForEntityByUser(faction, terminal, contributions, participants)
        t
    }.toList
  }

  private def extractContributionsForEntityByUser(
                                                   faction: PlanetSideEmpire.Value,
                                                   target: SourceEntry,
                                                   contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                   contributionsBy: mutable.LongMap[ContributionStats]
                                                 ): List[InGameActivity] = {
    val shortHistory = contributions.getOrElse(target.unique, Nil)
    val process = new ArmorRecoveryExperienceContributionProcess(faction, contributions)
    process.submit(shortHistory)
    cullContributorImplements(process.output()).foreach {
      case (id, stats) => contributionsBy.getOrElseUpdate(id, stats)
    }
    shortHistory
  }

  private[exp] def cullContributorImplements(
                                              input: mutable.LongMap[ContributionStats]
                                            ): mutable.LongMap[ContributionStats] = {
    input.collect { case (id, entry) =>
      (id, entry.copy(weapons = entry.weapons.filter { stats => KillContributions.RecoveryItems.contains(stats.equipment_id) }))
    }.filter { case (_, entry) =>
      entry.weapons.nonEmpty
    }
  }

  private def contributeWithCombinedActivity(
                                              id: Long,
                                              newStats: List[WeaponStats],
                                              user: PlayerSource,
                                              amount: Int,
                                              total: Int,
                                              shots: Int,
                                              time: Long,
                                              participants: mutable.LongMap[ContributionStats]
                                            ): Unit = {
    participants.get(id) match {
      case Some(stats) =>
        val oldWeapons = stats.weapons
        val newWeapons = newStats.map { weapon =>
          oldWeapons.find { _.equipment_id == weapon.equipment_id } match {
            case Some(foundEntry) =>
              weapon.copy(
                amount = weapon.amount + foundEntry.amount,
                shots = weapon.shots + foundEntry.shots,
                contributions = weapon.contributions + foundEntry.contributions
              )
            case None =>
              weapon
          }
        }
        participants.put(id, stats.copy(weapons = newWeapons))
      case None =>
        participants.put(id, ContributionStats(user, newStats, amount, total, shots, time))
    }
  }

  private def composeContributionOutput(
                                         charId: Long,
                                         shortPeriod: mutable.LongMap[ContributionStats],
                                         longPeriod: mutable.LongMap[ContributionStats],
                                         bep: Long
                                       ): (Long, ContributionStatsOutput) = {
    shortPeriod
      .get(charId)
      .collect {
        case entry =>
          val weapons = entry.weapons
          (
            entry.player,
            weapons.map { _.equipment_id },
            math.min(weapons.foldLeft(0f)(_ + _.contributions).toLong, bep)
          )
      }
      .orElse {
        longPeriod
          .get(charId)
          .collect {
            case entry =>
              val weapons = entry.weapons
              (
                entry.player,
                weapons.filter { _.amount == 0 }.map { _.equipment_id },
                (0.9f * math.min(weapons.foldLeft(0f)(_ + _.contributions), bep.toFloat)).toLong
              )
          }
      }
      .orElse {
        Some((PlayerSource.Nobody, Seq(0), 0L))
      }
      .collect {
        case (player, weaponIds, experience) =>
          (charId, ContributionStatsOutput(player, weaponIds, experience.toFloat))
      }
      .get
  }
}
