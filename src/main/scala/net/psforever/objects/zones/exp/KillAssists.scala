// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef
import net.psforever.objects.avatar.scoring.{Assist, Death, KDAStat, Kill}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.vital.{DamagingActivity, HealingActivity, InGameActivity, RepairingActivity, RevivingActivity, SpawningActivity}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Config

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * One player will interact using any number of weapons they possess
 * that will affect a different player - the target.
 * A kill is counted as the last interaction that affects a target so as to drop their health to zero.
 * An assist is counted as every other interaction that affects the target up until the kill interaction
 * in a similar way to the kill interaction.
 * @see `ContributionStats`
 * @see `ContributionStatsOutput`
 * @see `DamagingActivity`
 * @see `HealingActivity`
 * @see `InGameActivity`
 * @see `InGameHistory`
 * @see `PlayerSource`
 * @see `RepairingActivity`
 * @see `SourceEntry`
 */
object KillAssists {
  /**
   * Primary landing point for calculating the rewards given for player death.
   * Rewards in the form of "battle experience points" are given:
   * to the player held responsible for the other player's death - the killer;
   * all players whose efforts managed to deal damage to the player who died prior to the killer - assists.
   * @param victim player that died
   * @param lastDamage purported as the in-game activity that resulted in the player dying
   * @param history chronology of activity the game considers noteworthy;
   *                `lastDamage` should be within this chronology
   * @param eventBus where to send the results of the experience determination(s)
   * @see `ActorRef`
   * @see `AvatarAction.UpdateKillsDeathsAssists`
   * @see `AvatarServiceMessage`
   * @see `DamageResult`
   * @see `rewardThisPlayerDeath`
   */
  private[exp] def rewardThisPlayerDeath(
                                          victim: PlayerSource,
                                          lastDamage: Option[DamageResult],
                                          history: Iterable[InGameActivity],
                                          eventBus: ActorRef
                                        ): Unit = {
    rewardThisPlayerDeath(victim, lastDamage, history).foreach { case (p, kda) =>
      eventBus ! AvatarServiceMessage(p.Name, AvatarAction.UpdateKillsDeathsAssists(p.CharId, kda))
    }
  }

  /**
   * Primary innards of the functionality of calculating the rewards given for player death.
   * @param victim player that died
   * @param lastDamage purported as the in-game activity that resulted in the player dying
   * @param history chronology of activity the game considers noteworthy;
   *                `lastDamage` should be within this chronology
   * @return na
   * @see `Assist`
   * @see `calculateExperience`
   * @see `collectKillAssistsForPlayer`
   * @see `DamageResult`
   * @see `Death`
   * @see `KDAStat`
   * @see `limitHistoryToThisLife`
   * @see `Support.baseExperience`
   */
  private def rewardThisPlayerDeath(
                                     victim: PlayerSource,
                                     lastDamage: Option[DamageResult],
                                     history: Iterable[InGameActivity],
                                   ):  Seq[(PlayerSource, KDAStat)] = {
    val truncatedHistory = limitHistoryToThisLife(history.toList)
    determineKiller(lastDamage, truncatedHistory) match {
      case Some((result, killer: PlayerSource)) =>
        val assists = collectKillAssistsForPlayer(victim, truncatedHistory, Some(killer))
        val fullBep = calculateExperience(killer, victim, truncatedHistory)
        val hitSquad = (killer, Kill(victim, result, fullBep)) +: assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, truncatedHistory.last.time - truncatedHistory.head.time, fullBep)) +: hitSquad

      case _ =>
        val assists = collectKillAssistsForPlayer(victim, truncatedHistory, None)
        val fullBep = Support.baseExperience(victim, truncatedHistory)
        val hitSquad = assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, truncatedHistory.last.time - truncatedHistory.head.time, fullBep)) +: hitSquad
    }
  }

  /**
   * Limit the chronology of in-game activity between an starting activity and a concluding activity for a player character.
   * The starting activity is signalled by one or two particular events.
   * The concluding activity is a condition of one of many common events.
   * All of the activities logged in between count.
   * @param history chronology of activity the game considers noteworthy
   * @return chronology of activity the game considers noteworthy, but truncated
   */
  private def limitHistoryToThisLife(history: List[InGameActivity]): List[InGameActivity] = {
    val spawnIndex = history.lastIndexWhere {
      case _: SpawningActivity => true
      case _: RevivingActivity => true
      case _ => false
    }
    val endIndex = history.lastIndexWhere {
      case damage: DamagingActivity => damage.data.targetAfter.asInstanceOf[PlayerSource].Health == 0
      case _ => false
    }
    if (spawnIndex == -1 || endIndex == -1 || spawnIndex > endIndex) {
      Nil
    } else {
      history.slice(spawnIndex, endIndex)
    }
  }

  /**
   * Determine the player who is the origin/owner of the bullet that reduced health to zero.
   * @param lastDamageActivity damage result that purports the player who is the killer
   * @param history chronology of activity the game considers noteworthy;
   *                referenced in the case that the suggested `DamageResult` is not suitable to determine a player
   * @return player associated
   * @see `limitHistoryToThisLife`
   */
  private[exp] def determineKiller(
                                    lastDamageActivity: Option[DamageResult],
                                    history: List[InGameActivity]
                                  ): Option[(DamageResult, SourceEntry)] = {
    val now = System.currentTimeMillis()
    val compareTimeMillis = 10.seconds.toMillis
    lastDamageActivity
      .collect { case dam
        if now - dam.interaction.hitTime < compareTimeMillis && dam.adversarial.nonEmpty =>
        (dam, dam.adversarial.get.attacker)
      }
      .orElse {
        limitHistoryToThisLife(history)
          .lastOption
          .collect { case dam: DamagingActivity if dam.data.adversarial.nonEmpty => dam.data }
          .map { data => (data, data.adversarial.get.attacker) }
      }
  }

  /**
   * Modify a base experience value to consider additional reasons for points.
   * @param killer player that delivers the interaction that reduces health to zero
   * @param victim player to which the final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @return the value of the kill in what the game called "battle experience points"
   * @see `BattleRank.withExperience`
   * @see `Support.baseExperience`
   * @see `Support.calculateMenace`
   */
  private def calculateExperience(
                                   killer: PlayerSource,
                                   victim: PlayerSource,
                                   history: Iterable[InGameActivity]
                                 ): Long = {
    val killerUnique = killer.unique
    if (killer.Faction == victim.Faction || killerUnique == victim.unique) {
      0L
    } else {
      val base = Support.baseExperience(victim, history)
      if (base > Support.TheShortestLifeIsWorth) {
        val bep = Config.app.game.experience.bep
        //battle rank disparity modifier
        val battleRankDisparity: Long = {
          import net.psforever.objects.avatar.BattleRank
          val killerLevel = BattleRank.withExperience(killer.bep).value
          val victimLevel = BattleRank.withExperience(victim.bep).value
          val victimMinusKiller = victimLevel - killerLevel
          if (victimMinusKiller > -1) {
            victimMinusKiller * 5 + victimLevel // 5 x (y - x) + y -> min = 1 @ (1, 1), max = 235 @ (40, 1)
          } else {
            victimMinusKiller //min = -39, max = -1
          }
        }.toLong
        //revenge modifier
        val revengeBonus: Long = {
          val revenge = bep.revenge
          val victimUnique = victim.unique
          val lastDeath = killer.progress.prior.flatMap(_.death)
          val sameLastKiller = lastDeath.map(_.assailant.map(_.unique)).flatMap(_.headOption).contains(victimUnique)
          if (revenge.experience != 0 && sameLastKiller) {
            revenge.experience
          } else if (sameLastKiller) {
            (lastDeath.map(_.experienceEarned).get * revenge.rate).toLong
          } else {
            0L
          }
        }
        base + battleRankDisparity + revengeBonus
      } else {
        base
      }
    }
  }

  /**
   * Evaluate chronological in-game activity within a scope of history and
   * isolate the interactions that lead to one player dying.
   * Factor in interactions that would have the dying player attempt to resist death, if only for a short while longer.
   * @param victim player to which the final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @param killerOpt optional player that delivers the interaction that reduces the `victim's` health to zero
   * @return summary of the interaction in terms of players, equipment activity, and experience
   * @see `armorDamageContributors`
   * @see `collectKillAssists`
   * @see `healthDamageContributors`
   * @see `Support.allocateContributors`
   * @see `Support.onlyOriginalAssistEntries`
   */
  private def collectKillAssistsForPlayer(
                                           victim: PlayerSource,
                                           history: List[InGameActivity],
                                           killerOpt: Option[PlayerSource]
                                         ): Iterable[ContributionStatsOutput] = {
    val healthAssists = collectKillAssists(
      victim,
      history,
      Support.allocateContributors(healthDamageContributors)
    )
    healthAssists.remove(0L)
    healthAssists.remove(victim.CharId)
    killerOpt.map { killer => healthAssists.remove(killer.CharId) }
    if (Support.wasEverAMax(victim, history)) {
      val armorAssists = collectKillAssists(
        victim,
        history,
        Support.allocateContributors(armorDamageContributors)
      )
      armorAssists.remove(0L)
      armorAssists.remove(victim.CharId)
      killerOpt.map { killer => armorAssists.remove(killer.CharId) }
      Support.onlyOriginalAssistEntries(healthAssists, armorAssists)
    } else {
      healthAssists.values
    }
  }

  /**
   * Analyze history based on a discriminating function and format the output.
   * @param victim player to which the final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @param func mechanism for discerning particular interactions and building a narrative around their history;
   *             tallies all activity by a certain player using certain equipment
   * @return summary of the interaction in terms of players, equipment activity, and experience
   */
  private def collectKillAssists(
                                  victim: SourceEntry,
                                  history: List[InGameActivity],
                                  func: (List[InGameActivity], PlanetSideEmpire.Value) => mutable.LongMap[ContributionStats]
                                ): mutable.LongMap[ContributionStatsOutput] = {
    val assists = func(history, victim.Faction).filterNot { case (_, kda) => kda.amount <= 0 }
    val total = assists.values.foldLeft(0f)(_ + _.total)
    val output = assists.map { case (id, kda) =>
      (id, ContributionStatsOutput(kda.player, kda.weapons.map { _.equipment }, kda.amount / total))
    }
    output.remove(victim.CharId)
    output
  }

  /**
   * In relation to a target player's health,
   * build a secondary chronology of how the health value is affected per interaction and
   * maintain a quantitative record of that activity in relation to the other players and their equipment.
   * @param history chronology of activity the game considers noteworthy
   * @param faction empire to target
   * @param participants quantitative record of activity in relation to the other players and their equipment
   * @return chronology of how the health value is affected per interaction
   * @see `contributeWithDamagingActivity`
   * @see `contributeWithRecoveryActivity`
   * @see `RevivingActivity`
   */
  private def healthDamageContributors(
                                        history: List[InGameActivity],
                                        faction: PlanetSideEmpire.Value,
                                        participants: mutable.LongMap[ContributionStats]
                                      ): Seq[(Long, Int)] = {
    /*
    damage as it is measured in order (with heal-countered damage eliminated)<br>
    key - character identifier,
    value - current damage contribution
    */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.foreach {
      case d: DamagingActivity if d.health > 0 =>
        inOrder = contributeWithDamagingActivity(d, faction, d.health, participants, inOrder)
      case r: RevivingActivity =>
        inOrder = contributeWithRecoveryActivity(r.amount, participants, inOrder)
      case h: HealingActivity =>
        inOrder = contributeWithRecoveryActivity(h.amount, participants, inOrder)
      case _ => ()
    }
    inOrder
  }

  /**
   * In relation to a target player's armor,
   * build a secondary chronology of how the armor value is affected per interaction and
   * maintain a quantitative record of that activity in relation to the other players and their equipment.
   * @param history chronology of activity the game considers noteworthy
   * @param faction empire to target
   * @param participants quantitative record of activity in relation to the other players and their equipment
   * @return chronology of how the armor value is affected per interaction
   * @see `contributeWithDamagingActivity`
   * @see `contributeWithRecoveryActivity`
   */
  private def armorDamageContributors(
                                       history: List[InGameActivity],
                                       faction: PlanetSideEmpire.Value,
                                       participants: mutable.LongMap[ContributionStats]
                                     ): Seq[(Long, Int)] = {
    /*
    damage as it is measured in order (with heal-countered damage eliminated)<br>
    key - character identifier,
    value - current damage contribution
    */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.foreach {
      case d: DamagingActivity if d.amount - d.health > 0 =>
        inOrder = contributeWithDamagingActivity(d, faction, d.amount - d.health, participants, inOrder)
      case r: RepairingActivity =>
        inOrder = contributeWithRecoveryActivity(r.amount, participants, inOrder)
      case _ => ()
    }
    inOrder
  }

  /**
   * Analyze damaging activity for quantitative records.
   * @param activity a particular in-game activity that negative affects a player's health
   * @param faction empire to target
   * @param amount value
   * @param participants quantitative record of activity in relation to the other players and their equipment
   * @param order chronology of how the armor value is affected per interaction
   * @return chronology of how the armor value is affected per interaction
   */
  private def contributeWithDamagingActivity(
                                                   activity: DamagingActivity,
                                                   faction: PlanetSideEmpire.Value,
                                                   amount: Int,
                                                   participants: mutable.LongMap[ContributionStats],
                                                   order: Seq[(Long, Int)]
                                                 ): Seq[(Long, Int)] = {
    val data = activity.data
    val playerOpt = data.adversarial.collect { case Adversarial(p: PlayerSource, _,_) => p }
    contributeWithDamagingActivity(
      playerOpt,
      data.interaction.cause.attribution,
      faction,
      amount,
      activity.time,
      participants,
      order
    )
  }

  /**
   * Analyze damaging activity for quantitative records.
   * @param userOpt optional player for the quantitative record
   * @param wepid weapon for the quantitative record
   * @param faction empire to target
   * @param amount value
   * @param participants quantitative record of activity in relation to the other players and their equipment
   * @param order chronology of how the armor value is affected per interaction
   * @return chronology of how the armor value is affected per interaction
   */
  private[exp] def contributeWithDamagingActivity(
                                                   userOpt: Option[PlayerSource],
                                                   wepid: Int,
                                                   faction: PlanetSideEmpire.Value,
                                                   amount: Int,
                                                   time: Long,
                                                   participants: mutable.LongMap[ContributionStats],
                                                   order: Seq[(Long, Int)]
                                                 ): Seq[(Long, Int)] = {
    userOpt match {
      case Some(user)
        if user.Faction != faction =>
        val whoId = user.CharId
        val percentage = amount / user.Definition.MaxHealth.toFloat
        val updatedEntry = participants.get(whoId) match {
          case Some(mod) =>
            //previous attacker, just add to entry
            val firstWeapon = mod.weapons.head
            val newEntry = DamageWith(wepid)
            val weapons = if (firstWeapon.equipment == newEntry) {
              firstWeapon.copy(
                amount = firstWeapon.amount + amount,
                shots = firstWeapon.shots + 1,
                time = time,
                contributions = firstWeapon.contributions + percentage
              ) +: mod.weapons.tail
            } else {
              WeaponStats(newEntry, amount, 1, time, percentage) +: mod.weapons
            }
            mod.copy(
              amount = mod.amount + amount,
              weapons = weapons,
              total = mod.total + amount,
              shots = mod.shots + 1,
              time = time
            )
          case None =>
            //new attacker, new entry
            ContributionStats(
              user,
              Seq(WeaponStats(DamageWith(wepid), amount, 1, time, percentage)),
              amount,
              amount,
              1,
              time
            )
        }
        participants.put(whoId, updatedEntry)
        order.indexWhere({ case (id, _) => id == whoId }) match {
          case 0 =>
            //ongoing attack by same player
            val entry = order.head
            (entry._1, entry._2 + amount) +: order.tail
          case _ =>
            //different player than immediate prior attacker
            (whoId, amount) +: order
        }
      case _ =>
        //damage that does not lead to contribution
        order.headOption match {
          case Some((id, dam)) =>
            if (id == 0L) {
              (0L, dam + amount) +: order.tail //pool
            } else {
              (0L, amount) +: order //new
            }
          case None =>
            order
        }
    }
  }

  /**
   * Analyze recovery activity for quantitative records.
   * @param amount value
   * @param participants quantitative record of activity in relation to the other players and their equipment
   * @param order chronology of how the armor value is affected per interaction
   * @return chronology of how the armor value is affected per interaction
   */
  private[exp] def contributeWithRecoveryActivity(
                                                   amount: Int,
                                                   participants: mutable.LongMap[ContributionStats],
                                                   order: Seq[(Long, Int)]
                                                 ): Seq[(Long, Int)] = {
    var amt = amount
    var count = 0
    var newOrderPos: Seq[(Long, Int)] = Nil
    var newOrderNeg: Seq[(Long, Int)] = Nil
    order.takeWhile { entry =>
      val (id, total) = entry
      if (id > 0 && total > 0) {
        participants.get(id) match {
          case Some(part) =>
            val reduceByValue = math.min(amt, total)
            val trimmedWeapons = {
              var index = 0
              var weaponSum = 0
              val pweapons = part.weapons
              val pwepiter = pweapons.iterator
              while (pwepiter.hasNext && weaponSum < reduceByValue) {
                weaponSum = weaponSum + pwepiter.next().amount
                index += 1
              }
              //output list(s)
              (if (weaponSum == reduceByValue) {
                newOrderNeg = newOrderNeg :+ (id, 0)
                index += 1
                pweapons.drop(index)
              } else if (weaponSum > reduceByValue) {
                newOrderPos = (id, total - amt) +: newOrderPos
                val remainder = pweapons.drop(index)
                remainder.headOption.map(_.copy(amount = weaponSum - reduceByValue)) ++ remainder.tail
              } else {
                newOrderPos = (id, total - amt) +: newOrderPos
                val remainder = pweapons.drop(index)
                remainder.headOption.map(_.copy(amount = reduceByValue - weaponSum)) ++ remainder.tail
              }) ++ pweapons.take(index).map(_.copy(amount = 0))
            }
            participants.put(id, part.copy(amount = part.amount - reduceByValue, weapons = trimmedWeapons.toSeq))
            amt = amt - reduceByValue
          case _ =>
            //we do not have contribution stat data for this id
            //perform no calculations; devalue the entry
            newOrderNeg = newOrderNeg :+ (id, 0)
        }
      }
      count += 1
      amt > 0
    }
    newOrderPos ++ order.drop(count) ++ newOrderNeg//.reverse
  }
}
