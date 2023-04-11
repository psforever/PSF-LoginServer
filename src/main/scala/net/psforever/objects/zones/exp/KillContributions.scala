// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef

import java.util.Date
import net.psforever.objects.avatar.scoring.{Assist, Kill}
import net.psforever.objects.sourcing.{PlayerSource, SourceUniqueness}
import net.psforever.objects.vital.{Contribution, DamagingActivity, HealFromEquipment, HealFromTerminal, HealingActivity, InGameActivity, RepairFromEquipment, RepairFromExoSuitChange, RepairFromTerminal, RepairingActivity, RevivingActivity, SupportActivityCausedByAnother}
import net.psforever.objects.vital.interaction.Adversarial
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire
import org.joda.time.Instant
import org.joda.time.{LocalDateTime => JodaLocalDateTime}

import scala.collection.mutable

object KillContributions {
  private lazy val recoveryItems: Seq[Int] = {
    import net.psforever.objects.GlobalDefinitions._
    Seq(
      bank.ObjectId,
      nano_dispenser.ObjectId,
      medicalapplicator.ObjectId,
      medical_terminal.ObjectId,
      adv_med_terminal.ObjectId
    )
  }

  private[exp] def rewardTheseSupporters(
                                          target: PlayerSource,
                                          history: Iterable[InGameActivity],
                                          kill: Kill,
                                          bep: Long,
                                          eventBus: ActorRef
                                        ): Unit = {
    val killTime = kill.time
    val faction = target.Faction
    val shortHistory = Support.limitHistoryToThisLife(history.toList)
    //direct heal assists and repair assists
    /*
    can be simplified by ignoring resolved damage and countered healing
    and just extracting the HealingActivity and DamagingActivity events,
    but the contribution percentage calculations will need to be adjusted
    */
    val healthAssists = cullContributorImplements(allocateContributors(healthRecoveryContributors)(shortHistory, faction))
    val armorAssists = cullContributorImplements(allocateContributors(armorRecoveryContributors)(shortHistory, faction))
    //combined
    val allAssists = healthAssists.map { case out @ (id, healthEntry) =>
      armorAssists.get(id) match {
        case Some(armorEntry) =>
          (id, healthEntry.copy(weapons = healthEntry.weapons ++ armorEntry.weapons))
        case None =>
          out
      }
    }
    //revival
    contributeWithRevivalActivity(shortHistory, allAssists)
    allAssists.remove(0)
    allAssists.remove(target.CharId)
    //divide by applicable time periods (long=10minutes, short=5minutes)
    val longEvents = findTimeApplicableActivities(allAssists.values.toSeq, killTime, timeOffset = 600)
    val shortEvents = findTimeApplicableActivities(longEvents, killTime, timeOffset = 300)
    val victim = kill.victim
    //convert to output format
    longEvents
      .map { stats =>
        val (_, ContributionStatsOutput(p, w, r)) = mapContributionPointsByPercentage(total = 100f, shortEvents)(stats.player.CharId, stats)
        (p, Assist(victim, w, r, (bep * r).toLong))
      }
      .foreach { case (player, kda) =>
        eventBus ! AvatarServiceMessage(player.Name, AvatarAction.UpdateKillsDeathsAssists(player.CharId, kda))
      }
  }

  private def allocateContributors(
                                    tallyFunc: (List[InGameActivity], PlanetSideEmpire.Value, mutable.LongMap[ContributionStats]) => Seq[(Long, Int)]
                                  )
                                  (
                                    history: List[InGameActivity],
                                    faction: PlanetSideEmpire.Value
                                  ): mutable.LongMap[ContributionStats] = {
    /** players who have contributed to this death, and how much they have contributed<br>
     * key - character identifier,
     * value - (player, damage, total damage, number of shots) */
    val participants: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
    tallyFunc(history, faction, participants)
    participants
  }

  private def cullContributorImplements(
                                         input: mutable.LongMap[ContributionStats]
                                       ): mutable.LongMap[ContributionStats] = {
    input.collect { case (id, entry) =>
      (id, entry.copy(weapons = entry.weapons.filter { stats => recoveryItems.contains(stats.weapon_id) }))
    }.filter { case (_, entry) =>
      entry.weapons.nonEmpty
    }
  }

  private def healthRecoveryContributors(
                                        history: List[InGameActivity],
                                        faction: PlanetSideEmpire.Value,
                                        participants: mutable.LongMap[ContributionStats]
                                      ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var damageInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    var recoveryInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    val contributions = history
      .collect { case Contribution(unique, entries) => (unique.unique, entries) }
      .toMap[SourceUniqueness, List[InGameActivity]]
    val contributionsBy: mutable.LongMap[List[InGameActivity]] =
      mutable.LongMap[List[InGameActivity]]()
    history.foreach {
      case d: DamagingActivity if d.health > 0 =>
        val (damage, recovery) = contributeWithDamagingActivity(d, faction, d.health, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealFromEquipment =>
        val (damage, recovery) = contributeWithRecoveryActivity(h.user, h.equipment_def.ObjectId, faction, h.amount, h.time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case ht: HealFromTerminal =>
        val time = ht.time
        val users = cacheContributionsForEntityByUser(ht.term.unique, contributions, contributionsBy)
          .collect { case entry: SupportActivityCausedByAnother
            if entry.time <= time && entry.time > time - 300000 => entry //short support activity time
          }
          .groupBy(_.user.unique)
          .map(_._2.head.user)
          .toSeq
        val (damage, recovery) = contributeWithSupportRecoveryActivity(users, ht.term.Definition.ObjectId, faction, ht.amount, time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealingActivity =>
        val (damage, recovery) = contributeWithRecoveryActivity(wepid = 0, faction, h.amount, h.time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RevivingActivity =>
        val (damage, recovery) = contributeWithRecoveryActivity(r.equipment.ObjectId, faction, r.amount, r.time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case _ => ()
    }
    recoveryInOrder
  }

  private def armorRecoveryContributors(
                                       history: List[InGameActivity],
                                       faction: PlanetSideEmpire.Value,
                                       participants: mutable.LongMap[ContributionStats]
                                     ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var damageInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    var recoveryInOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    val contributions = history
      .collect { case Contribution(unique, entries) => (unique.unique, entries) }
      .toMap[SourceUniqueness, List[InGameActivity]]
    val contributionsBy: mutable.LongMap[List[InGameActivity]] =
      mutable.LongMap[List[InGameActivity]]()
    history.foreach {
      case d: DamagingActivity if d.amount - d.health > 0 =>
        val (damage, recovery) = contributeWithDamagingActivity(d, faction, d.amount - d.health, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RepairFromEquipment =>
        val (damage, recovery) = contributeWithRecoveryActivity(r.user, r.equipment_def.ObjectId, faction, r.amount, r.time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case rt: RepairFromTerminal => ()
        val time = rt.time
        val users = cacheContributionsForEntityByUser(rt.term.unique, contributions, contributionsBy)
          .collect { case entry: SupportActivityCausedByAnother
            if entry.time <= time && entry.time > time - 300000 => entry //short support activity time
          }
          .groupBy(_.user.unique)
          .map(_._2.head.user)
          .toSeq
        val (damage, recovery) = contributeWithSupportRecoveryActivity(users, rt.term.Definition.ObjectId, faction, rt.amount, time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case rxc: RepairFromExoSuitChange =>
        //TODO use cacheContributionsForEntityByUser; what out what order terminal was used
        val (damage, recovery) = contributeWithRecoveryActivity(wepid = 0, faction, rxc.amount, rxc.time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RepairingActivity =>
        val (damage, recovery) = contributeWithRecoveryActivity(wepid = 0, faction, r.amount, r.time, participants, damageInOrder,recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case _ => ()
    }
    recoveryInOrder
  }

  def cacheContributionsForEntityByUser(
                                        target: SourceUniqueness,
                                        contributions: Map[SourceUniqueness, List[InGameActivity]],
                                        contributionsBy: mutable.LongMap[List[InGameActivity]]
                                      ): List[InGameActivity] = {
    contributions.get(target) match {
      case Some(list) if list.nonEmpty =>
        list
          .collect { case r: RepairFromEquipment => r }
          .groupBy(_.user.unique)
          .flatMap { case (_, activities) =>
            val charId = activities.head.user.CharId
            contributionsBy.get(charId) match {
              case Some(outList) =>
                outList
              case None =>
                contributionsBy.put(charId, activities).getOrElse(Nil)
            }
          }.toList
      case _ =>
        Nil
    }
  }

  private def contributeWithDamagingActivity(
                                              activity: DamagingActivity,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              participants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    //mark entries from the recovery list to truncate
    val data = activity.data
    val time = activity.time
    val playerOpt = data.adversarial.collect { case Adversarial(p: PlayerSource, _, _) => p }
    var lastCharId = 0L
    var lastValue = 0
    var ramt = amount
    var rindex = -1
    val riter = recoveryOrder.iterator
    while (riter.hasNext && ramt > 0) {
      val (id, value) = riter.next()
      if (value > 0) {
        val entry = participants(id)
        val weapons = entry.weapons
        lastCharId = id
        lastValue = value
        if (value > ramt) {
          participants.put(
            id,
            entry.copy(
              weapons = weapons.head.copy(amount = weapons.head.amount - ramt, time = time) +: weapons.tail,
              amount = entry.total - ramt,
              time = time
            )
          )
          ramt = 0
          lastValue = lastValue - value
        } else {
          participants.put(
            id,
            entry.copy(
              weapons = weapons.tail :+ weapons.head.copy(amount = 0, time = time),
              amount = entry.total - ramt,
              time = time
            )
          )
          ramt = ramt - value
          rindex += 1
          lastValue = 0
        }
      }
      rindex += 1
    }
    //damage calculations
    val newDamageOrder = KillAssists.contributeWithDamagingActivity(
      playerOpt,
      data.interaction.cause.attribution,
      faction,
      amount,
      time,
      participants,
      damageOrder
    )
    //re-combine output list(s)
    val leftovers = if (lastValue > 0) {
      Seq((lastCharId, lastValue))
    } else {
      Nil
    }
    (newDamageOrder, leftovers ++ recoveryOrder.slice(rindex, recoveryOrder.size) ++ recoveryOrder.take(rindex).map { case (id, _) => (id, 0) })
  }

  private def contributeWithRecoveryActivity(
                                              user: PlayerSource,
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              participants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    contributeWithRecoveryActivity(user, user.CharId, wepid, faction, amount, time, participants, damageOrder, recoveryOrder)
  }

  private def contributeWithRecoveryActivity(
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              participants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    contributeWithRecoveryActivity(PlayerSource.Nobody, charId = 0, wepid, faction, amount, time, participants, damageOrder, recoveryOrder)
  }

  private def contributeWithRecoveryActivity(
                                              user: PlayerSource,
                                              charId: Long,
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              participants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    //recovery calculations
    val newOrder = KillAssists.contributeWithRecoveryActivity(amount, participants, damageOrder)
    //detect potential instances of and remove approximate value for friendly-fire recovery (that doesn't count)
    val qualityRecoveryAmount = {
      val (entries, count) = {
        val value = damageOrder.size - newOrder.size
        if (value > 0) {
          if (newOrder.head == damageOrder.lift(value).getOrElse(newOrder.head)) {
            (damageOrder.take(value), value)
          } else {
            (damageOrder.take(value + 1), value + 1)
          }
        } else {
          if (newOrder.headOption != damageOrder.headOption) {
            (damageOrder.headOption.toList, 1)
          } else {
            (Nil, 1)
          }
        }
      }
      if (entries.nonEmpty) {
        //approximate the value as a percentage
        amount * entries.count { case (id, _) => participants.get(id).collect { _.player.Faction != faction }.getOrElse(false) } / count
      } else {
        amount
      }
    }
    if (qualityRecoveryAmount > 0) {
      val updatedEntry = participants.get(charId) match {
        case Some(entry) =>
          //existing entry
          entry.copy(
            weapons = WeaponStats(wepid, qualityRecoveryAmount, 1, time, 1f) +: entry.weapons,
            amount = entry.amount + qualityRecoveryAmount,
            total = entry.total + qualityRecoveryAmount,
            time = time
          )
        case None =>
          //new entry
          ContributionStats(user, Seq(WeaponStats(wepid, qualityRecoveryAmount, 1, time, 1f)), qualityRecoveryAmount, qualityRecoveryAmount, 1, time)
      }
      participants.put(charId, updatedEntry)
    }
    //re-combine output list(s) (bridge entry for output recovery list)
    (newOrder, (charId, amount) +: recoveryOrder)
  }

  private def contributeWithSupportRecoveryActivity(
                                                     users: Seq[PlayerSource],
                                                     wepid: Int,
                                                     faction: PlanetSideEmpire.Value,
                                                     amount: Int,
                                                     time: Long,
                                                     participants: mutable.LongMap[ContributionStats],
                                                     damageOrder: Seq[(Long, Int)],
                                                     recoveryOrder: Seq[(Long, Int)]
                                                   ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    var outputDamageOrder = damageOrder
    var outputRecoveryOrder = recoveryOrder
    users.zip {
      val numberOfUsers = users.size
      val out = Array.fill(numberOfUsers)(numberOfUsers / amount)
      (0 to numberOfUsers % amount).foreach {
        out(_) += 1
      }
      out
    }.foreach { case (user, subAmount) =>
      val (a, b) = contributeWithRecoveryActivity(user, user.CharId, wepid, faction, subAmount, time, participants, outputDamageOrder, outputRecoveryOrder)
      outputDamageOrder = a
      outputRecoveryOrder = b
    }
    (outputDamageOrder, outputRecoveryOrder)
  }

  private def contributeWithRevivalActivity(
                                             history: List[InGameActivity],
                                             participants: mutable.LongMap[ContributionStats]
                                           ): Unit = {
    history
      .collect { case rev: RevivingActivity => rev }
      .groupBy(_.user.CharId)
      .foreach { case (id, revivesByThisPlayer) =>
        val numberOfRevives = revivesByThisPlayer.size
        val latestTime = revivesByThisPlayer.maxBy(_.time).time
        val newStats = revivesByThisPlayer.map { stat => WeaponStats(stat.equipment.ObjectId, 100, 1, stat.time, 0.25f) }
        val newStat = WeaponStats(revivesByThisPlayer.head.equipment.ObjectId, 100 * numberOfRevives, numberOfRevives, latestTime, 0.25f)
        participants.get(id) match {
          case Some(stats) =>
            participants.put(id, stats.copy(weapons = stats.weapons ++ newStats))
          case None =>
            participants.put(id, ContributionStats(
              revivesByThisPlayer.head.user,
              newStats,
              newStat.amount,
              newStat.shots,
              revivesByThisPlayer.size,
              latestTime
            ))
        }
      }
  }

  private def findTimeApplicableActivities(
                                            activity: Seq[ContributionStats],
                                            killLastTime: JodaLocalDateTime,
                                            timeOffset: Int //s
                                          ): Seq[ContributionStats] = {
    val dateTimeConverter: Date=>JodaLocalDateTime = JodaLocalDateTime.fromDateFields
    val milliToInstant: Long=>org.joda.time.Instant = Instant.ofEpochMilli
    val targetTime = killLastTime.minusSeconds(timeOffset)
    //find all activities that occurred before the kill time but after the offset
    activity.collect { entry =>
      val weapons = entry.weapons.filter { stat =>
        val activityTime = dateTimeConverter(milliToInstant(stat.time).toDate)
        (activityTime.isBefore(killLastTime) || activityTime.equals(killLastTime)) && targetTime.isAfter(activityTime)
      }
        .groupBy(_.weapon_id)
        .collect { case (id, stats) =>
          WeaponStats(
            id,
            stats.foldLeft(0)(_ + _.amount),
            stats.foldLeft(0)(_ + _.shots),
            stats.maxBy(_.time).time,
            stats.foldLeft(0f)(_ + _.contributions)
          )
        }
      if (weapons.nonEmpty) {
        val totalMod = entry.total / entry.amount.toFloat
        val recoveryAmount = weapons.foldLeft(0)(_ + _.amount)
        val recoveryShots = weapons.foldLeft(0)(_ + _.shots)
        val recoveryTime = weapons.map { _.time }.max
        Some(ContributionStats(entry.player, weapons.toSeq, recoveryAmount, (recoveryAmount * totalMod).toInt, recoveryShots, recoveryTime))
      } else {
        None
      }
    }.flatten
  }

  private def mapContributionPointsByPercentage(
                                                 total: Float,
                                                 compareList: Iterable[ContributionStats]
                                               )
                                               (
                                                 charId: Long,
                                                 contribution: ContributionStats,
                                               ): (Long, ContributionStatsOutput) = {
    val user = contribution.player
    val unique = user.unique
    val points = contribution.amount
    val value = if (points < 75) {
      //a small contribution means the lower time limit
      if (compareList.exists { a => a.player.unique == unique }) {
        math.max(0.2f, points / total)
      } else {
        0f
      }
    } else {
      //large contribution is always okay
      if (points > 299) {
        1.0f
      } else if (points > 100) {
        0.75f
      } else {
        0.5f
      }
    }
    (charId, ContributionStatsOutput(user, contribution.weapons.map { _.weapon_id }, value))
  }
}
