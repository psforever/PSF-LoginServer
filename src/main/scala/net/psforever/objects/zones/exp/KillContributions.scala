// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.avatar.scoring.{Kill, SupportActivity}
import net.psforever.objects.serverobject.hackable.Hackable.HackInfo
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource, SourceEntry, SourceUniqueness, VehicleSource}
import net.psforever.objects.vital.{Contribution, InGameActivity, RevivingActivity, TerminalUsedActivity, VehicleCargoDismountActivity, VehicleCargoMountActivity, VehicleCargoMountChange, VehicleDismountActivity, VehicleMountActivity, VehiclePassengerMountChange}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.exp.rec.{ArmorRecoveryExperienceContributionProcess, CombinedHealthAndArmorContributionProcess, MachineRecoveryExperienceContributionProcess}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.collection.mutable

object KillContributions {
  final val RecoveryItems: Seq[Int] = {
    import net.psforever.objects.GlobalDefinitions._
    Seq(
      bank,
      nano_dispenser,
      medicalapplicator,
      order_terminal,
      order_terminala,
      order_terminalb,
      medical_terminal,
      adv_med_terminal,
      bfr_rearm_terminal,
      multivehicle_rearm_terminal,
      lodestar_repair_terminal
    ).collect { _.ObjectId }
  } //TODO currently includes things that are not typical items but are used for expressing contribution implements

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
    //divide by applicable time periods (long=10minutes, short=5minutes)
    val shortPeriod = killTime - 300000L
    val (contributions, (longHistory, shortHistory)) = {
      val (contrib, onlyHistory) = history.partition { _.isInstanceOf[Contribution] }
      (
        contrib
          .collect { case Contribution(unique, entries) => (unique, entries) }
          .toMap[SourceUniqueness, List[InGameActivity]],
        limitHistoryToThisLife(onlyHistory.toList, killTime).partition { _.time > shortPeriod }
      )
    }
    //sort by applicable time periods, as long as the longer period is represented by activity
    val empty = mutable.ListBuffer[SourceUniqueness]()
    val otherContributionCalculations = contributionScoringAndCulling(faction, kill, contributions, bep)(_, _, _)
    val finalContributions = if (longHistory.nonEmpty && KillAssists.calculateMenace(target) > 2) {
      val longContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      val shortContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Seq(longContributionProcess))
      longContributionProcess.submit(longHistory)
      shortContributionProcess.submit(shortHistory)
      val longContributionEntries = otherContributionCalculations(longHistory, longContributionProcess.output(), empty)
      val shortContributionEntries = otherContributionCalculations(shortHistory, shortContributionProcess.output(), empty)
      longContributionEntries.remove(target.CharId)
      longContributionEntries.remove(kill.victim.CharId)
      shortContributionEntries.remove(target.CharId)
      shortContributionEntries.remove(kill.victim.CharId)
      (longContributionEntries ++ shortContributionEntries)
        .toSeq
        .distinctBy(_._2.player.unique)
        .map { case (_, stats) =>
          composeContributionOutput(stats.player, shortContributionEntries, longContributionEntries, bep)
        }
    } else {
      val contributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      contributionProcess.submit(shortHistory)
      val contributionEntries = otherContributionCalculations(shortHistory, contributionProcess.output(), empty)
      contributionEntries.remove(target.CharId)
      contributionEntries.remove(kill.victim.CharId)
      contributionEntries
        .map { case (_, stats) =>
          composeContributionOutput(stats.player, contributionEntries, contributionEntries, bep)
        }
    }
    //take the output and transform that into contribution distribution data
    val victim = kill.victim
    finalContributions.foreach { case (charId, ContributionStatsOutput(player, weapons, exp)) =>
      eventBus ! AvatarServiceMessage(
        player.Name,
        AvatarAction.UpdateKillsDeathsAssists(charId, SupportActivity(victim, weapons, exp.toLong))
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
                                             contributions: Map[SourceUniqueness, List[InGameActivity]],
                                             bep: Long
                                           )
                                           (
                                             history: List[InGameActivity],
                                             contributionEntries: mutable.LongMap[ContributionStats],
                                             excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                           ): mutable.LongMap[ContributionStats] = {
    contributionEntries.map { case (id, stat) =>
      val newWeaponStats = stat.weapons.map { weaponStat =>
        weaponStat.copy(contributions = (weaponStat.shots + (bep * weaponStat.amount / stat.total)).toFloat)
      }
      contributionEntries.put(id, stat.copy(weapons = newWeaponStats))
    }
    contributeWithKillWhileMountedActivity(faction, kill, history, contributionEntries, excludedTargets)
    contributeWithRevivalActivity(history, contributionEntries)
    contributeWithVehicleTransportActivity(history, contributionEntries)
    contributeWithVehicleCargoTransportActivity(history, contributionEntries)
    contributeWithTerminalActivity(faction, history, contributions, contributionEntries, excludedTargets)
    contributionEntries.remove(0)
    contributionEntries
  }

  private def contributeWithKillWhileMountedActivity(
                                                      faction: PlanetSideEmpire.Value,
                                                      kill: Kill,
                                                      history: List[InGameActivity],
                                                      participants: mutable.LongMap[ContributionStats],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                                    ): List[InGameActivity] = {
    (kill
      .info
      .interaction
      .cause match {
      case p: ProjectileReason => p.projectile.mounted_in.map { _._2 }
      case _ => None
    })
      .collect {
        case mount: VehicleSource if !excludedTargets.contains(mount.unique) =>
          //repairs
          val contributions = history
            .collect { case Contribution(unique, entries) if mount.unique == unique => (unique, entries) }
            .toMap[SourceUniqueness, List[InGameActivity]]
          contributions
            .foreach {
              case (_, localHistory) =>
                val process = new ArmorRecoveryExperienceContributionProcess(faction, contributions, excludedTargets :+ mount.unique)
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
          extractContributionsForEntityByUser(faction, mount, contributions, participants, excludedTargets)
          mount.owner
            .collect {
              case owner =>
                val time = kill.time.toDate.getTime
                participants.getOrElseUpdate(
                  owner.CharId,
                  ContributionStats(
                    owner,
                    Seq(WeaponStats(DriverAssist(mount.Definition.ObjectId), 1, 1, time, 10f)),
                    1,
                    1,
                    1,
                    time
                  )
                )
            }
      }
    history
  }

  private def contributeWithVehicleTransportActivity(
                                                      history: List[InGameActivity],
                                                      participants: mutable.LongMap[ContributionStats]
                                                    ): List[InGameActivity] = {
    /*
    collect the dismount activity of all vehicles from which this player is not the owner
    make certain all dismount activity can be paired with a mounting activity
    certain other qualifications of the prior mounting must be met before the support bonus applies
    */
    val dismountActivity = history
      .collect {
        case inAndOut: VehiclePassengerMountChange
          if !inAndOut.vehicle.owner.contains(inAndOut.player) => inAndOut
      }
      .grouped(2)
      .collect {
        case List(in: VehicleMountActivity, out: VehicleDismountActivity)
          if in.vehicle.unique == out.vehicle.unique &&
            out.vehicle.Faction == out.player.Faction &&
            (in.vehicle.Definition == GlobalDefinitions.router || {
              val inTime = in.time
              val outTime = out.time
              out.player.progress.kills.exists { death =>
                val deathTime = death.info.interaction.hitTime
                inTime < deathTime && deathTime <= outTime
              }
            } || {
              val sameZone = in.zoneNumber == out.zoneNumber
              val distanceMoved = Vector3.DistanceSquared(in.vehicle.Position.xy, out.vehicle.Position.xy)
              val timeSpent = out.time - in.time
              timeSpent >= 210000 /* 3:30 */ ||
                (sameZone && (distanceMoved > 160000f || distanceMoved > 10000f && timeSpent >= 60000)) |
                (!sameZone && (distanceMoved > 10000f || timeSpent >= 120000))
            }) =>
          out
      }.toList
    //apply
    dismountActivity
      .groupBy { a => a.vehicle.owner }
      .collect { case (Some(owner), dismountsFromVehicle) =>
        val numberOfDismounts = dismountsFromVehicle.size
        contributeWithCombinedActivity(
          owner.CharId,
          dismountsFromVehicle.map { act =>
            val statContext = act.vehicle.Definition match {
              case v @ GlobalDefinitions.router =>
                RouterKillAssist(v.ObjectId)
              case v =>
                HotDropKillAssist(v.ObjectId, 0)
            }
            WeaponStats(statContext, 0, numberOfDismounts, act.time, 15f)
          },
          dismountsFromVehicle.head.vehicle.owner.get,
          amount = 0,
          total = 0,
          numberOfDismounts,
          dismountsFromVehicle.maxBy(_.time).time,
          participants
        )
      }
    dismountActivity
  }

  private def contributeWithVehicleCargoTransportActivity(
                                                           history: List[InGameActivity],
                                                           participants: mutable.LongMap[ContributionStats]
                                                         ): List[InGameActivity] = {
    /*
    collect the dismount activity of all vehicles from which this player is not the owner
    make certain all dismount activity can be paired with a mounting activity
    certain other qualifications of the prior mounting must be met before the support bonus applies
    */
    val dismountActivity = history
      .collect {
        case inAndOut: VehicleCargoMountChange if inAndOut.vehicle.owner.nonEmpty => inAndOut
      }
      .grouped(2)
      .collect {
        case List(in: VehicleCargoMountActivity, out: VehicleCargoDismountActivity)
          if in.vehicle.unique == out.vehicle.unique &&
            out.vehicle.Faction == out.cargo.Faction &&
            (in.vehicle.Definition == GlobalDefinitions.router || {
              val distanceMoved = Vector3.DistanceSquared(in.vehicle.Position.xy, out.vehicle.Position.xy)
              val timeSpent = out.time - in.time
              timeSpent >= 210000 /* 3:30 */ || distanceMoved > 640000f
            }) =>
          out
      }.toList
    //apply
    dismountActivity
      .groupBy { a => a.vehicle.owner }
      .collect { case (Some(owner), dismountsFromVehicle) =>
        val numberOfDismounts = dismountsFromVehicle.size
        contributeWithCombinedActivity(
          owner.CharId,
          dismountsFromVehicle.map { act =>
            WeaponStats(
              HotDropKillAssist(act.vehicle.Definition.ObjectId, act.cargo.Definition.ObjectId),
              0,
              numberOfDismounts,
              act.time,
              15f
            )
          },
          dismountsFromVehicle.head.vehicle.owner.get,
          amount = 0,
          total = 0,
          numberOfDismounts,
          dismountsFromVehicle.maxBy(_.time).time,
          participants
        )
      }
    dismountActivity
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
          revivesByThisPlayer.map { stat =>
            WeaponStats(ReviveKillAssist(stat.equipment.ObjectId), 100, 1, stat.time, 25f)
          },
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
                                              participants: mutable.LongMap[ContributionStats],
                                              excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                            ): List[InGameActivity] = {
    contributeWithTerminalActivity(
      history.collect { case t: TerminalUsedActivity => (t, t.terminal, t.terminal.hacked) },
      faction,
      contributions,
      participants,
      excludedTargets
    )
  }

  private[exp] def contributeWithTerminalActivity(
                                                   data: Seq[(InGameActivity, AmenitySource, Option[HackInfo])],
                                                   faction: PlanetSideEmpire.Value,
                                                   contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                   participants: mutable.LongMap[ContributionStats],
                                                   excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                                 ): List[InGameActivity] = {
    data.collect {
      case (t, terminal, Some(info)) if terminal.Faction != faction =>
        /*
        if the terminal has been hacked,
        and the original terminal does not align with our own faction,
        then the support must be reported as a hack;
        if we are the same faction as the terminal, then the hacked condition is irrelevant
        */
        participants.getOrElseUpdate(
          info.player.CharId,
          ContributionStats(
            info.player,
            Seq(WeaponStats(HackKillAssist(terminal.Definition.ObjectId), 0, 1, t.time, 10f)),
            0,
            0,
            1,
            t.time
          )
        )
        t
      case (t, terminal, _) =>
        val (equipmentUseContext, ownerOpt) = terminal.installation match {
          case v: VehicleSource =>
            terminal.Definition match {
              case GlobalDefinitions.order_terminala =>
                extractContributionsForEntityByUser(faction, v, contributions, participants, excludedTargets)
                (AmsResupplyKillAssist(terminal.Definition.ObjectId), v.owner)
              case GlobalDefinitions.order_terminalb =>
                extractContributionsForEntityByUser(faction, v, contributions, participants, excludedTargets)
                (AmsResupplyKillAssist(terminal.Definition.ObjectId), v.owner)
              case GlobalDefinitions.lodestar_repair_terminal =>
                extractContributionsForEntityByUser(faction, v, contributions, participants, excludedTargets)
                (RepairKillAssist(terminal.Definition.ObjectId, v.Definition.ObjectId), v.owner)
              case GlobalDefinitions.bfr_rearm_terminal =>
                extractContributionsForEntityByUser(faction, v, contributions, participants, excludedTargets)
                (LodestarRearmKillAssist(terminal.Definition.ObjectId), v.owner)
              case GlobalDefinitions.multivehicle_rearm_terminal =>
                extractContributionsForEntityByUser(faction, v, contributions, participants, excludedTargets)
                (LodestarRearmKillAssist(terminal.Definition.ObjectId), v.owner)
              case _ =>
                (NoUse(0), None)
            }
          case _ =>
            (NoUse(0), None)
        }
        ownerOpt.collect {
          owner =>
            participants.getOrElseUpdate(
              owner.CharId,
              ContributionStats(
                owner,
                Seq(WeaponStats(equipmentUseContext, 0, 1, t.time, 10f)),
                0,
                0,
                1,
                t.time
              )
            )
        }
        t
    }.toList
  }

  private def extractContributionsForEntityByUser(
                                                   faction: PlanetSideEmpire.Value,
                                                   target: SourceEntry,
                                                   contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                   contributionsBy: mutable.LongMap[ContributionStats],
                                                   excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                                 ): List[InGameActivity] = {
    if (!excludedTargets.contains(target.unique)) {
      val shortHistory = contributions.getOrElse(target.unique, Nil)
      val process = new MachineRecoveryExperienceContributionProcess(faction, contributions, excludedTargets :+ target.unique)
      process.submit(shortHistory)
      cullContributorImplements(process.output()).foreach {
        case (id, stats) => contributionsBy.getOrElseUpdate(id, stats)
      }
      shortHistory
    } else {
      Nil
    }
  }

  private[exp] def cullContributorImplements(
                                              input: mutable.LongMap[ContributionStats]
                                            ): mutable.LongMap[ContributionStats] = {
    input.collect { case (id, entry) =>
      (id, entry.copy(weapons = entry.weapons.filter { stats => KillContributions.RecoveryItems.contains(stats.equipment.equipment) })) //TODO bad test; fix later
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
          oldWeapons.find { _.equipment == weapon.equipment } match {
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
                                         player: PlayerSource,
                                         shortPeriod: mutable.LongMap[ContributionStats],
                                         longPeriod: mutable.LongMap[ContributionStats],
                                         bep: Long
                                       ): (Long, ContributionStatsOutput) = {
    val charId = player.CharId
    longPeriod
      .get(charId)
      .collect {
        case entry =>
          val weapons = entry.weapons
          (
            entry.player,
            weapons.filter { _.amount == 0 }.map { _.equipment },
            (0.9f * math.min(weapons.foldLeft(0f)(_ + _.contributions), bep.toFloat)).toLong
          )
      }
      .orElse {
        shortPeriod
          .get(charId)
          .collect {
            case entry =>
              val weapons = entry.weapons
              (
                entry.player,
                weapons.map { _.equipment },
                math.min(weapons.foldLeft(0f)(_ + _.contributions).toLong, bep)
              )
          }
      }
      .orElse {
        Some((PlayerSource.Nobody, Seq(), 0L))
      }
      .collect {
        case (player, weaponIds, experience) if experience > 0 =>
          (charId, ContributionStatsOutput(player, weaponIds, experience.toFloat))
      }
      .get
  }
}
