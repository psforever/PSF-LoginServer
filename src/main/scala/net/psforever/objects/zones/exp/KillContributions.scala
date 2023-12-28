// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.avatar.scoring.{Kill, SupportActivity}
import net.psforever.objects.sourcing.{BuildingSource, PlayerSource, SourceEntry, SourceUniqueness, TurretSource, VehicleSource}
import net.psforever.objects.vital.{Contribution, HealFromTerminal, InGameActivity, RepairFromTerminal, RevivingActivity, TerminalUsedActivity, VehicleCargoDismountActivity, VehicleCargoMountActivity, VehicleDismountActivity, VehicleMountActivity}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.exp.rec.{CombinedHealthAndArmorContributionProcess, MachineRecoveryExperienceContributionProcess}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import net.psforever.util.Config

import scala.collection.mutable

/**
 * Kills and assists consider the target, in an exchange of projectiles from the weapons of players towards the target.
 * Contributions consider actions of other allied players towards the player who is the source of the projectiles.
 * These actions are generally positive for the player.
 * @see `Contribution`
 * @see `ContributionStats`
 * @see `ContributionStatsOutput`
 * @see `DamagingActivity`
 * @see `GlobalDefinitions`
 * @see `HealingActivity`
 * @see `InGameActivity`
 * @see `InGameHistory`
 * @see `Kill`
 * @see `PlayerSource`
 * @see `RepairingActivity`
 * @see `SourceEntry`
 * @see `SourceUniqueness`
 * @see `VehicleSource`
 */
object KillContributions {
  /** the object type ids of various game elements that are recognized for "stat recovery" */
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
  } //TODO currently includes things that are not typical equipment but things that express contribution

  /** cached for empty collection returns; please do not add anything to it */
  private val emptyMap: mutable.LongMap[ContributionStats] = mutable.LongMap.empty[ContributionStats]

  /**
   * Primary landing point for calculating the rewards given for helping one player kill another player.
   * Rewards in the form of "support experience points" are given
   * to all allied players that have somehow been involved with the player who killed another player.
   * @param target player that delivers the interaction that killed another player;
   *               history is purportedly composed of events that have happened to this player within a time frame
   * @param history chronology of activity the game considers noteworthy
   * @param kill the in-game event that maintains information about the other player's death;
   *             originates from prior statistical management normally
   * @param bep battle experience points to be referenced for support experience points conversion
   * @param eventBus where to send the results of the experience determination(s)
   * @see `ActorRef`
   * @see `AvatarAction.UpdateKillsDeathsAssists`
   * @see `AvatarServiceMessage`
   * @see `rewardTheseSupporters`
   * @see `SupportActivity`
   */
  private[exp] def rewardTheseSupporters(
                                          target: PlayerSource,
                                          history: Iterable[InGameActivity],
                                          kill: Kill,
                                          bep: Long,
                                          eventBus: ActorRef
                                        ): Unit = {
    val victim = kill.victim
    //take the output and transform that into contribution distribution data
    rewardTheseSupporters(target, history, kill, bep)
      .foreach { case (charId, ContributionStatsOutput(player, weapons, exp)) =>
        eventBus ! AvatarServiceMessage(
          player.Name,
          AvatarAction.UpdateKillsDeathsAssists(charId, SupportActivity(victim, weapons, exp.toLong))
        )
    }
  }

  /**
   * Primary innards for calculating the rewards given for helping one player kill another player.
   * @param target player that delivers the interaction that killed another player;
   *               history is purportedly composed of events that have happened to this player within a time frame
   * @param history chronology of activity the game considers noteworthy
   * @param kill the in-game event that maintains information about the other player's death;
   *             originates from prior statistical management normally
   * @param bep battle experience points to be referenced for support experience points conversion
   * returns list of user unique identifiers and
   *         a summary of the interaction in terms of players, equipment activity, and experience
   * @see `ActorRef`
   * @see `additionalContributionSources`
   * @see `AvatarAction.UpdateKillsDeathsAssists`
   * @see `AvatarServiceMessage`
   * @see `CombinedHealthAndArmorContributionProcess`
   * @see `composeContributionOutput`
   * @see `initialScoring`
   * @see `Support.calculateMenace`
   * @see `limitHistoryToThisLife`
   * @see `rewardTheseSupporters`
   * @see `SupportActivity`
   */
  private[exp] def rewardTheseSupporters(
                                          target: PlayerSource,
                                          history: Iterable[InGameActivity],
                                          kill: Kill,
                                          bep: Long
                                        ): Iterable[(Long, ContributionStatsOutput)] = {
    val faction = target.Faction
    /*
    divide into applicable time periods;
    these two periods represent passes over the in-game history to evaluate statistic modification events;
    the short time period should stand on its own, but should also be represented in the long time period;
    more players should be rewarded if one qualifies for the longer time period's evaluation
    */
    val (contributions, (longHistory, shortHistory)) = {
      val killTime = kill.time.toDate.getTime
      val shortPeriod = killTime - Config.app.game.experience.shortContributionTime
      val (contrib, onlyHistory) = history.partition { _.isInstanceOf[Contribution] }
      (
        contrib
          .collect { case Contribution(unique, entries) => (unique, entries) }
          .toMap[SourceUniqueness, List[InGameActivity]],
        limitHistoryToThisLife(onlyHistory.toList, killTime).partition { _.time < shortPeriod }
      )
    }
    //events that are older than 5 minutes are enough to prove one has been alive that long
    val empty = mutable.ListBuffer[SourceUniqueness]()
    empty.addOne(target.unique)
    val otherContributionCalculations = additionalContributionSources(faction, kill, contributions)(_, _, _)
    if (longHistory.nonEmpty && Support.calculateMenace(target) > 3) {
      //long and short history
      val longContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      val shortContributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Seq(longContributionProcess))
      longContributionProcess.submit(longHistory)
      shortContributionProcess.submit(shortHistory)
      val longContributionEntries = otherContributionCalculations(
        longHistory,
        initialScoring(longContributionProcess.output(), bep.toFloat),
        empty
      )
      val shortContributionEntries = otherContributionCalculations(
        shortHistory,
        initialScoring(shortContributionProcess.output(), bep.toFloat),
        empty
      )
      longContributionEntries.remove(target.CharId)
      longContributionEntries.remove(kill.victim.CharId)
      shortContributionEntries.remove(target.CharId)
      shortContributionEntries.remove(kill.victim.CharId)
      //combine
      (longContributionEntries ++ shortContributionEntries)
        .toSeq
        .distinctBy(_._2.player.unique)
        .flatMap { case (_, stats) =>
          composeContributionOutput(stats.player.CharId, shortContributionEntries, longContributionEntries, bep)
        }
    } else {
      //short history only
      val contributionProcess = new CombinedHealthAndArmorContributionProcess(faction, contributions, Nil)
      contributionProcess.submit(shortHistory)
      val contributionEntries = otherContributionCalculations(
        shortHistory,
        initialScoring(contributionProcess.output(), bep.toFloat),
        empty
      )
      contributionEntries.remove(target.CharId)
      contributionEntries.remove(kill.victim.CharId)
      contributionEntries
        .flatMap { case (_, stats) =>
          composeContributionOutput(stats.player.CharId, contributionEntries, contributionEntries, bep)
        }
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
    limitHistoryToThisLife(history, eventTime, eventTime - Config.app.game.experience.longContributionTime)
  }

  /**
   * Only historical activity that falls within the valid period matters.
   * @param history the original history
   * @param eventTime from which time to start counting backwards
   * @param startTime after which time to start counting forwards
   * @return the potentially truncated history
   */
  private def limitHistoryToThisLife(
                                      history: List[InGameActivity],
                                      eventTime: Long,
                                      startTime: Long
                                    ): List[InGameActivity] = {
    history.filter { event => event.time <= eventTime && event.time >= startTime }
  }

  /**
   * Manipulate contribution scores that have been evaluated up to this point
   * for a fixed combination of users and different implements
   * by replacing the score using a flat predictable numerical evaluation.
   * @param existingParticipants quantitative record of activity in relation to the other players and their equipment
   * @param bep battle experience point
   * @return quantitative record of activity in relation to the other players and their equipment
   */
  private def initialScoring(
                              existingParticipants: mutable.LongMap[ContributionStats],
                              bep: Float
                            ): mutable.LongMap[ContributionStats] = {
    //the scoring up to this point should be rate based, but is not perfectly useful for us
    existingParticipants.map { case (id, stat) =>
      val newWeaponStats = stat.weapons.map { weaponStat =>
        weaponStat.copy(contributions = 10f + weaponStat.shots.toFloat + 0.05f * bep)
      }
      existingParticipants.put(id, stat.copy(weapons = newWeaponStats))
    }
    existingParticipants
  }

  /**
   * na
   * @param faction empire to target
   * @param kill the in-game event that maintains information about the other player's death;
   *             originates from prior statistical management normally
   * @param contributions na
   * @param history chronology of activity the game considers noteworthy
   * @param existingParticipants quantitative record of activity in relation to the other players and their equipment
   * @param excludedTargets do not repeat analysis on entities associated with these tokens
   * @return quantitative record of activity in relation to the other players and their equipment
   * @see `contributeWithRevivalActivity`
   * @see `contributeWithTerminalActivity`
   * @see `contributeWithVehicleTransportActivity`
   * @see `contributeWithVehicleCargoTransportActivity`
   * @see `contributeWithKillWhileMountedActivity`
   */
  private def additionalContributionSources(
                                             faction: PlanetSideEmpire.Value,
                                             kill: Kill,
                                             contributions: Map[SourceUniqueness, List[InGameActivity]]
                                           )
                                           (
                                             history: List[InGameActivity],
                                             existingParticipants: mutable.LongMap[ContributionStats],
                                             excludedTargets: mutable.ListBuffer[SourceUniqueness]
                                           ): mutable.LongMap[ContributionStats] = {
    contributeWithRevivalActivity(history, existingParticipants)
    contributeWithTerminalActivity(history, faction, contributions, excludedTargets, existingParticipants)
    contributeWithVehicleTransportActivity(kill, history, faction, contributions, excludedTargets, existingParticipants)
    contributeWithVehicleCargoTransportActivity(kill, history, faction, contributions, excludedTargets, existingParticipants)
    contributeWithKillWhileMountedActivity(kill, faction, contributions, excludedTargets, existingParticipants)
    existingParticipants.remove(0)
    existingParticipants
  }

  /**
   * Gather and reward specific in-game equipment use activity.<br>
   * If the player who performed the killing interaction is mounted in something,
   * determine if the mount is has been effected by previous in-game interactions
   * that resulted in positive stat maintenance or development.
   * Also, reward the owner, if an owner exists, for providing the mount.
   * @param kill the in-game event that maintains information about the other player's death
   * @param faction empire to target
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @param out quantitative record of activity in relation to the other players and their equipment
   * @see `combineStatsInto`
   * @see `extractContributionsForMachineByTarget`
   */
  private def contributeWithKillWhileMountedActivity(
                                                      kill: Kill,
                                                      faction: PlanetSideEmpire.Value,
                                                      contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                      out: mutable.LongMap[ContributionStats]
                                                    ): Unit = {
    val eventTime = kill.time.toDate.getTime
    (kill
      .info
      .interaction
      .cause match {
      case p: ProjectileReason => p.projectile.mounted_in.map { case (_, src) => Some((src, p.projectile.owner)) }
      case _ => None
    })
      .collect {
        case Some((mount: VehicleSource, attacker: PlayerSource)) if !excludedTargets.contains(mount.unique) =>
          mount.owner
            .collect {
              case owner if owner == attacker.unique =>
                //owner is gunner; reward only repairs
                excludedTargets.addOne(owner)
                owner
              case owner =>
                //gunner is different from owner; reward driver and repairs
                excludedTargets.addOne(owner)
                excludedTargets.addOne(attacker.unique)
                val time = kill.time.toDate.getTime
                val weaponStat = Support.calculateSupportExperience(
                  event = "mounted-kill",
                  WeaponStats(DriverAssist(mount.Definition.ObjectId), 1, 1, time, 1f)
                )
                combineStatsInto(
                  out,
                  (
                    owner.charId,
                    ContributionStats(
                      PlayerSource(owner, mount.Position),
                      Seq(weaponStat),
                      1,
                      1,
                      1,
                      time
                    )
                  )
                )
                owner
            }
          combineStatsInto(
            out,
            extractContributionsForMachineByTarget(mount, faction, eventTime, contributions, excludedTargets, eventOutputType="support-repair")
          )
        case Some((mount: TurretSource, _: PlayerSource)) if !excludedTargets.contains(mount.unique) =>
          combineStatsInto(
            out,
            extractContributionsForMachineByTarget(mount, faction, eventTime, contributions, excludedTargets, eventOutputType="support-repair-turret")
          )
      }
  }

  /**
   * Gather and reward specific in-game equipment use activity.<br>
   * na
   * @param kill the in-game event that maintains information about the other player's death
   * @param history chronology of activity the game considers noteworthy
   * @param faction empire to target
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @param out quantitative record of activity in relation to the other players and their equipment
   * @see `combineStatsInto`
   * @see `extractContributionsForMachineByTarget`
   */
  private def contributeWithVehicleTransportActivity(
                                                      kill: Kill,
                                                      history: List[InGameActivity],
                                                      faction: PlanetSideEmpire.Value,
                                                      contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                      out: mutable.LongMap[ContributionStats]
                                                    ): Unit = {
    /*
    collect the dismount activity of all vehicles from which this player is not the owner
    make certain all dismount activity can be paired with a mounting activity
    certain other qualifications of the prior mounting must be met before the support bonus applies
    */
    val dismountActivity = history
      .collect {
        case out: VehicleDismountActivity
          if !out.vehicle.owner.contains(out.player.unique) && out.pairedEvent.nonEmpty => (out.pairedEvent.get, out)
      }
      .collect {
        case (in: VehicleMountActivity, out: VehicleDismountActivity)
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
              val distanceTransported = Vector3.DistanceSquared(in.vehicle.Position.xy, out.vehicle.Position.xy)
              val distanceMoved = {
                val killLocation = kill.info.adversarial
                  .collect { adversarial => adversarial.attacker.Position.xy }
                  .getOrElse(Vector3.Zero)
                Vector3.DistanceSquared(killLocation, out.player.Position.xy)
              }
              val timeSpent = out.time - in.time
              distanceMoved < 5625f /* 75m */ &&
                (timeSpent >= 210000L /* 3:30 */ ||
                  (sameZone && (distanceTransported > 160000f /* 400m */ ||
                    distanceTransported > 10000f /* 100m */ && timeSpent >= 60000L /* 1:00m */)) ||
                  (!sameZone && (distanceTransported > 10000f /* 100m */ || timeSpent >= 120000L /* 2:00 */ )))
            }) =>
          out
      }
    //apply
    dismountActivity
      .groupBy { _.vehicle }
      .collect { case (mount, dismountsFromVehicle) if mount.owner.nonEmpty =>
        val promotedOwner = PlayerSource(mount.owner.get, mount.Position)
        val (equipmentUseContext, equipmentUseEvent) = mount.Definition match {
          case v @ GlobalDefinitions.router =>
            (RouterKillAssist(v.ObjectId), "router")
          case v =>
            (HotDropKillAssist(v.ObjectId, 0), "hotdrop")
        }
        val size = dismountsFromVehicle.size
        val time = dismountsFromVehicle.maxBy(_.time).time
        val weaponStat = Support.calculateSupportExperience(
          equipmentUseEvent,
          WeaponStats(equipmentUseContext, size, size, time, 1f)
        )
        combineStatsInto(
          out,
          (
            promotedOwner.CharId,
            ContributionStats(promotedOwner, Seq(weaponStat), size, size, size, time)
          )
        )
        contributions.get(mount.unique).collect {
          case list =>
            val mountHistory = dismountsFromVehicle
              .flatMap { event =>
                val eventTime = event.time
                val startTime = event.pairedEvent.get.time - Config.app.game.experience.longContributionTime
                limitHistoryToThisLife(list, eventTime, startTime)
              }
              .distinctBy(_.time)
            combineStatsInto(
              out,
              extractContributionsForMachineByTarget(mount, faction, mountHistory, contributions, excludedTargets, eventOutputType="support-repair")
            )
        }
      }
  }

  /**
   * Gather and reward specific in-game equipment use activity.<br>
   * na
   * @param kill the in-game event that maintains information about the other player's death
   * @param faction empire to target
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @param out quantitative record of activity in relation to the other players and their equipment
   * @see `combineStatsInto`
   * @see `extractContributionsForMachineByTarget`
   */
  private def contributeWithVehicleCargoTransportActivity(
                                                           kill: Kill,
                                                           history: List[InGameActivity],
                                                           faction: PlanetSideEmpire.Value,
                                                           contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                           excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                           out: mutable.LongMap[ContributionStats]
                                                         ): Unit = {
    /*
    collect the dismount activity of all vehicles from which this player is not the owner
    make certain all dismount activity can be paired with a mounting activity
    certain other qualifications of the prior mounting must be met before the support bonus applies
    */
    val dismountActivity = history
      .collect {
        case out: VehicleCargoDismountActivity
          if out.vehicle.owner.nonEmpty && out.pairedEvent.nonEmpty => (out.pairedEvent.get, out)
      }
      .collect {
        case (in: VehicleCargoMountActivity, out: VehicleCargoDismountActivity)
          if in.vehicle.unique == out.vehicle.unique &&
            out.vehicle.Faction == out.cargo.Faction &&
            (in.vehicle.Definition == GlobalDefinitions.router || {
              val distanceTransported = Vector3.DistanceSquared(in.vehicle.Position.xy, out.vehicle.Position.xy)
              val distanceMoved = {
                val killLocation = kill.info.adversarial
                  .collect { adversarial => adversarial.attacker.Position.xy }
                  .getOrElse(Vector3.Zero)
                Vector3.DistanceSquared(killLocation, out.cargo.Position.xy)
              }
              val timeSpent = out.time - in.time
              distanceMoved < 5625f /* 75m */ &&
                (timeSpent >= 210000 /* 3:30 */ || distanceTransported > 360000f /* 600m */)
            }) =>
          out
      }
    //apply
    dismountActivity
      .groupBy { _.cargo }
      .collect { case (mount, dismountsFromVehicle) if mount.owner.nonEmpty =>
        val promotedOwner = PlayerSource(mount.owner.get, mount.Position)
        val mountId = mount.Definition.ObjectId
        dismountsFromVehicle
          .groupBy(_.vehicle)
          .map { case (vehicle, events) =>
            val size = events.size
            val time = events.maxBy(_.time).time
            val weaponStat = Support.calculateSupportExperience(
              event = "hotdrop",
              WeaponStats(HotDropKillAssist(vehicle.Definition.ObjectId, mountId), size, size, time, 1f)
            )
            (vehicle, vehicle.owner, Seq(weaponStat))
          }
          .collect { case (vehicle, Some(owner), statContext) =>
            combineStatsInto(
              out,
              (
                owner.charId,
                ContributionStats(promotedOwner, statContext, 1, 1, 1, statContext.head.time)
              )
            )
            contributions.get(mount.unique).collect {
              case list =>
                val mountHistory = dismountsFromVehicle
                  .flatMap { event =>
                    val eventTime = event.time
                    val startTime = event.pairedEvent.get.time - Config.app.game.experience.longContributionTime
                    limitHistoryToThisLife(list, eventTime, startTime)
                  }
                  .distinctBy(_.time)
                combineStatsInto(
                  out,
                  extractContributionsForMachineByTarget(mount, faction, mountHistory, contributions, excludedTargets, eventOutputType="support-repair")
                )
            }
            contributions.get(vehicle.unique).collect {
              case list =>
                val carrierHistory = dismountsFromVehicle
                  .flatMap { event =>
                    val eventTime = event.time
                    val startTime = event.pairedEvent.get.time - Config.app.game.experience.longContributionTime
                    limitHistoryToThisLife(list, eventTime, startTime)
                  }
                  .distinctBy(_.time)
                combineStatsInto(
                  out,
                  extractContributionsForMachineByTarget(vehicle, faction, carrierHistory, contributions, excludedTargets, eventOutputType="support-repair")
                )
            }
          }
      }
  }

  /**
   * Gather and reward specific in-game equipment use activity.<br>
   * na
   * @param faction empire to target
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @param out quantitative record of activity in relation to the other players and their equipment
   * @see `AmsResupplyKillAssist`
   * @see `BuildingSource`
   * @see `combineStatsInto`
   * @see `contributeWithTerminalActivity`
   * @see `extractContributionsForMachineByTarget`
   * @see `HackKillAssist`
   * @see `HealFromTerminal`
   * @see `LodestarRearmKillAssist`
   * @see `RepairFromTerminal`
   * @see `RepairKillAssist`
   * @see `TerminalUsedActivity`
   */
  private def contributeWithTerminalActivity(
                                              history: List[InGameActivity],
                                              faction: PlanetSideEmpire.Value,
                                              contributions: Map[SourceUniqueness, List[InGameActivity]],
                                              excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                              out: mutable.LongMap[ContributionStats]
                                            ): Unit = {
    history
      .collect {
        case h: HealFromTerminal => (h.term, h)
        case r: RepairFromTerminal => (r.term, r)
        case t: TerminalUsedActivity => (t.terminal, t)
      }
      .groupBy(_._1.unique)
      .map {
        case (_, events1) =>
          val (termThings1, _) = events1.unzip
          val hackContext = HackKillAssist(GlobalDefinitions.remote_electronics_kit.ObjectId, termThings1.head.Definition.ObjectId)
          if (termThings1.exists(t => t.Faction != faction && t.hacked.nonEmpty)) {
            /*
            if the terminal has been hacked,
            and the original terminal does not align with our own faction,
            then the support must be reported as a hack;
            if we are the same faction as the terminal, then the hacked condition is irrelevant
            */
            events1
              .collect { case out @ (t, _) if t.hacked.nonEmpty => out }
              .groupBy { case (t, _) => t.hacked.get.player.unique }
              .foreach { case (_, events2) =>
                val (termThings2, events3) = events2.unzip
                val hacker = termThings2.head.hacked.get.player
                val size = events3.size
                val time = events3.maxBy(_.time).time
                val weaponStats = Support.calculateSupportExperience(
                  event = "hack",
                  WeaponStats(hackContext, size, size, time, 1f)
                )
                combineStatsInto(
                  out,
                  (
                    hacker.CharId,
                    ContributionStats(
                      hacker,
                      Seq(weaponStats),
                      size,
                      size,
                      size,
                      time
                    )
                  )
                )
              }
          } else if (termThings1.exists(_.Faction == faction)) {
            //faction-aligned terminal
            val (_, events2) = events1.unzip
            val eventTime = events2.maxBy(_.time).time
            val startTime = events2.minBy(_.time).time - Config.app.game.experience.longContributionTime
            val termThingsHead = termThings1.head
            val (equipmentUseContext, equipmentUseEvent, installationEvent, target) = termThingsHead.installation match {
              case v: VehicleSource =>
                termThingsHead.Definition match {
                  case GlobalDefinitions.order_terminala =>
                    (AmsResupplyKillAssist(GlobalDefinitions.order_terminala.ObjectId), "ams-resupply", "support-repair", Some(v))
                  case GlobalDefinitions.order_terminalb =>
                    (AmsResupplyKillAssist(GlobalDefinitions.order_terminalb.ObjectId), "ams-resupply", "support-repair", Some(v))
                  case GlobalDefinitions.lodestar_repair_terminal =>
                    (RepairKillAssist(GlobalDefinitions.lodestar_repair_terminal.ObjectId, v.Definition.ObjectId), "lodestar-repair", "support-repair", Some(v))
                  case GlobalDefinitions.bfr_rearm_terminal =>
                    (LodestarRearmKillAssist(GlobalDefinitions.bfr_rearm_terminal.ObjectId), "lodestar-rearm", "support-repair", Some(v))
                  case GlobalDefinitions.multivehicle_rearm_terminal =>
                    (LodestarRearmKillAssist(GlobalDefinitions.multivehicle_rearm_terminal.ObjectId), "lodestar-rearm", "support-repair", Some(v))
                  case _ =>
                    (NoUse(), "", "", None)
                }
              case _: BuildingSource =>
                (NoUse(), "", "support-repair-terminal", Some(termThingsHead))
              case _ =>
                (NoUse(), "", "", None)
            }
            target.map { src =>
              combineStatsInto(
                out,
                extractContributionsForMachineByTarget(src, faction, eventTime, startTime, contributions, excludedTargets, installationEvent)
              )
            }
            events1
              .map { case (a, b) => (a.installation, b) }
              .collect { case (installation: VehicleSource, evt) if installation.owner.nonEmpty => (installation, evt) }
              .groupBy(_._1.owner.get)
              .collect { case (owner, list) =>
                val (installations, events2) = list.unzip
                val size = events2.size
                val time = events2.maxBy(_.time).time
                val weaponStats = Support.calculateSupportExperience(
                  equipmentUseEvent,
                  WeaponStats(equipmentUseContext, size, size, time, 1f)
                )
                combineStatsInto(
                  out,
                  (
                    owner.charId,
                    ContributionStats(
                      PlayerSource(owner, installations.head.Position),
                      Seq(weaponStats),
                      size,
                      size,
                      size,
                      time
                    )
                  )
                )
              }
          }
          None
      }
  }

  /**
   * Gather and reward specific in-game equipment use activity.<br>
   * na
   * @param history chronology of activity the game considers noteworthy
   * @param out quantitative record of activity in relation to the other players and their equipment
   * @see `combineStatsInto`
   * @see `ReviveKillAssist`
   */
  private def contributeWithRevivalActivity(
                                             history: List[InGameActivity],
                                             out: mutable.LongMap[ContributionStats]
                                           ): Unit = {
    history
      .collect { case rev: RevivingActivity => rev }
      .groupBy(_.user.CharId)
      .map { case (id, revivesByThisPlayer) =>
        val user = revivesByThisPlayer.head.user
        revivesByThisPlayer
          .groupBy(_.equipment)
          .map { case (definition, events) =>
            val eventSize = events.size
            val objectId = definition.ObjectId
            val time = events.maxBy(_.time).time
            combineStatsInto(
              out,
              (
                id,
                ContributionStats(
                  user,
                  Seq({
                    Support.calculateSupportExperience(
                      event = "revival",
                      WeaponStats(ReviveKillAssist(objectId), 1, eventSize, time, 1f)
                    )
                  }),
                  eventSize,
                  eventSize,
                  eventSize,
                  time
                )
              )
            )
          }
      }
  }

  /**
   * na
   * Mainly produces repair events.
   * @param target entity external to the subject of the kill
   * @param faction empire to target
   * @param time na
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @return quantitative record of activity in relation to the other players and their equipment
   */
  private def extractContributionsForMachineByTarget(
                                                      target: SourceEntry,
                                                      faction: PlanetSideEmpire.Value,
                                                      time: Long,
                                                      contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                      eventOutputType: String
                                                    ): mutable.LongMap[ContributionStats] = {
    val start: Long = time - Config.app.game.experience.longContributionTime
    extractContributionsForMachineByTarget(target, faction, time, start, contributions, excludedTargets, eventOutputType)
  }

  /**
   * na
   * Mainly produces repair events.
   * @param target entity external to the subject of the kill
   * @param faction empire to target
   * @param eventTime na
   * @param startTime na
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @return quantitative record of activity in relation to the other players and their equipment
   * @see `limitHistoryToThisLife`
   */
  private def extractContributionsForMachineByTarget(
                                                      target: SourceEntry,
                                                      faction: PlanetSideEmpire.Value,
                                                      eventTime: Long,
                                                      startTime: Long,
                                                      contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                      eventOutputType: String
                                                    ): mutable.LongMap[ContributionStats] = {
    val unique = target.unique
    val history = limitHistoryToThisLife(contributions.getOrElse(unique, List()), eventTime, startTime)
    extractContributionsForMachineByTarget(target, faction, history, contributions, excludedTargets, eventOutputType)
  }

  /**
   * na
   * Mainly produces repair events.
   * @param target entity external to the subject of the kill
   * @param faction empire to target
   * @param history na
   * @param contributions mapping between external entities
   *                      the target has interacted with in the form of in-game activity
   *                      and history related to the time period in which the interaction ocurred
   * @param excludedTargets if a potential target is listed here already, skip processing it
   * @return quantitative record of activity in relation to the other players and their equipment
   * @see `cullContributorImplements`
   * @see `emptyMap`
   * @see `MachineRecoveryExperienceContributionProcess`
   */
  private def extractContributionsForMachineByTarget(
                                                      target: SourceEntry,
                                                      faction: PlanetSideEmpire.Value,
                                                      history: List[InGameActivity],
                                                      contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                      excludedTargets: mutable.ListBuffer[SourceUniqueness],
                                                      eventOutputType: String
                                                    ): mutable.LongMap[ContributionStats] = {
    val unique = target.unique
    if (!excludedTargets.contains(unique) && history.nonEmpty) {
      excludedTargets.addOne(unique)
      val process = new MachineRecoveryExperienceContributionProcess(faction, contributions, eventOutputType, excludedTargets)
      process.submit(history)
      cullContributorImplements(process.output())
    } else {
      emptyMap
    }
  }

  /**
   * na
   * @param main quantitative record of activity in relation to the other players and their equipment
   * @param transferFrom quantitative record of activity in relation to the other players and their equipment
   * @return quantitative record of activity in relation to the other players and their equipment
   * @see `combineStatsInto`
   */
  private def combineStatsInto(
                                main: mutable.LongMap[ContributionStats],
                                transferFrom: mutable.LongMap[ContributionStats]
                              ): mutable.LongMap[ContributionStats] = {
    transferFrom.foreach { (entry: (Long, ContributionStats)) => combineStatsInto(main, entry) }
    main
  }

  /**
   * na
   * @param main quantitative record of activity in relation to the other players and their equipment
   * @param entry two value tuple representing:
   *         a player's unique identifier,
   *         and a quantitative record of activity in relation to the other players and their equipment
   * @see `Support.combineWeaponStats`
   */
  private def combineStatsInto(main: mutable.LongMap[ContributionStats], entry: (Long, ContributionStats)): Unit = {
    val (id, sampleStats) = entry
    main.get(id) match {
      case Some(foundStats) =>
        main.put(id, foundStats.copy(weapons = Support.combineWeaponStats(foundStats.weapons, sampleStats.weapons)))
      case None =>
        main.put(id, sampleStats)
    }
  }

  /**
   * Filter quantitative records based on the presence of specific equipment used for statistic recovery.
   * @param input quantitative record of activity in relation to the other players and their equipment
   * @return quantitative record of activity in relation to the other players and their equipment
   * @see `RecoveryItems`
   */
  private[exp] def cullContributorImplements(
                                              input: mutable.LongMap[ContributionStats]
                                            ): mutable.LongMap[ContributionStats] = {
    input.collect { case (id, entry) =>
      (id, entry.copy(weapons = entry.weapons.filter { stats => RecoveryItems.contains(stats.equipment.equipment) }))
    }.filter { case (_, entry) =>
      entry.weapons.nonEmpty
    }
  }

  /**
   * na
   * @param charId the unique identifier being targeted
   * @param shortPeriod quantitative record of activity in relation to the other players and their equipment
   * @param longPeriod quantitative record of activity in relation to the other players and their equipment
   * @param max maximum value for the third output value
   * @return two value tuple representing:
   *         a player's unique identifier,
   *         and a summary of the interaction in terms of players, equipment activity, and experience
   * @see `composeContributionOutput`
   */
  private def composeContributionOutput(
                                         charId: Long,
                                         shortPeriod: mutable.LongMap[ContributionStats],
                                         longPeriod: mutable.LongMap[ContributionStats],
                                         max: Long
                                       ): Option[(Long, ContributionStatsOutput)] = {
    composeContributionOutput(charId, longPeriod, modifier=0.8f, max)
      .orElse { composeContributionOutput(charId, shortPeriod, modifier=1f, max) }
      .collect {
        case (player, weaponIds, experience) =>
          (charId, ContributionStatsOutput(player, weaponIds, experience))
      }
  }

  /**
   * na
   * @param charId the unique identifier being targeted
   * @param stats quantitative record of activity in relation to the other players and their equipment
   * @param modifier modifier value for the potential third output value
   * @param max maximum value for the third output value
   * @return three value tuple representing:
   *         player,
   *         the context in which certain equipment is being used,
   *         and a final value for the awarded support experience points
   */
  private def composeContributionOutput(
                                         charId: Long,
                                         stats: mutable.LongMap[ContributionStats],
                                         modifier: Float,
                                         max: Long
                                       ): Option[(PlayerSource, Seq[EquipmentUseContextWrapper], Float)] = {
    stats
      .get(charId)
      .collect {
        case entry =>
          val (weapons, contributions) = entry.weapons.map { entry => (entry.equipment, entry.contributions) }.unzip
          (
            entry.player,
            weapons.distinct,
            modifier * math.floor(math.min(contributions.foldLeft(0f)(_ + _), max.toFloat)).toFloat
          )
      }
  }
}
