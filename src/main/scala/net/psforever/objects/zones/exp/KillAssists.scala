// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.ActorRef
import net.psforever.objects.avatar.scoring.{Assist, Death, Kill}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.vital.{DamagingActivity, HealingActivity, InGameActivity, RepairingActivity, RevivingActivity, SpawningActivity}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable
import scala.concurrent.duration._

object KillAssists {
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

  private[exp] def determineKiller(
                                    lastDamageActivity: Option[DamageResult],
                                    history: List[InGameActivity]
                                  ): Option[(DamageResult, SourceEntry)] = {
    val now = System.currentTimeMillis()
    val compareTimeMillis = 10.seconds.toMillis
    lastDamageActivity
      .collect { case dam if now - dam.interaction.hitTime < compareTimeMillis => dam }
      .flatMap { dam => Some(dam, dam.adversarial) }
      .orElse {
        history.collect { case damage: DamagingActivity
          if now - damage.time < compareTimeMillis && damage.data.adversarial.nonEmpty =>
          damage.data
        }
          .flatMap { dam => Some(dam, dam.adversarial) }.lastOption
      }
      .collect { case (dam, Some(adv)) => (dam, adv.attacker) }
  }

  /**
   * "Menace" is a crude measurement of how much consistent destructive power a player has been demonstrating.
   * Within the last ten kills, the rate of the player's killing speed is measured.
   * The measurement - a "streak" in modern lingo - is transformed into the form of an `Integer` for simplicity.
   * @param player the player
   * @param mercy a time value that can be used to continue a missed streak
   * @return an `Integer` between 0 and 7
   */
  private[exp] def calculateMenace(player: PlayerSource, mercy: Long = 2500L): Int = {
    val allKills = player.progress.kills.reverse
    val restBetweenKills = allKills.take(10) match {
      case firstKill :: kills if kills.size == 9 =>
        var xTime = firstKill.time.toDate.getTime
        kills.map { kill =>
          val time = kill.time.toDate.getTime
          val timeOut = time - xTime
          xTime = time
          timeOut
        }
      case _ =>
        Nil
    }
    //TODO the math here is not very meaningful
    math.floor(math.sqrt(
      math.max(0, takeWhileDelay(restBetweenKills, testValue = 20000L, mercy).size - 1) +
        math.max(0, takeWhileDelay(restBetweenKills, testValue = 10000L, mercy).size - 5) * 3 +
        math.max(0, takeWhileDelay(restBetweenKills, testValue = 5000L, mercy = 1000L).size - 4) * 5
    )).toInt
  }

  private def takeWhileDelay(list: Iterable[Long], testValue: Long, mercy: Long = 2500L): Iterable[Long] = {
    var onGoingMercy: Long = mercy
    list.takeWhile { time =>
      if (time < testValue) {
        true
      } else if (time - onGoingMercy - 1 < testValue) {
        onGoingMercy = math.ceil(onGoingMercy * 0.65).toLong
        true
      } else {
        false
      }
    }
  }

  private def calculateExperience(
                                   killer: PlayerSource,
                                   victim: PlayerSource,
                                   history: Iterable[InGameActivity]
                                 ): Long = {
    //base value (the kill experience before modifiers)
    val base = Support.baseExperience(victim, history)
    if (base > 1) {
      //battle rank disparity modifiers
      val battleRankDisparity = {
        import net.psforever.objects.avatar.BattleRank
        val killerLevel = BattleRank.withExperience(killer.bep).value
        val victimLevel = BattleRank.withExperience(victim.bep).value
        if (victimLevel > killerLevel || killerLevel - victimLevel < 6) {
          if (killerLevel < 7) {
            6 * victimLevel + 10
          } else if (killerLevel < 12) {
            (12 - killerLevel) * victimLevel + 10
          } else if (killerLevel < 25) {
            25 + victimLevel - killerLevel
          } else {
            25
          }
        } else {
          math.floor(-0.15f * base - killerLevel + victimLevel).toLong
        }
      }
      //menace modifiers
      val menace = 1f + calculateMenace(victim) * 0.14f
      math.max(1, (base + battleRankDisparity) * menace).toLong
    } else {
      base
    }
  }

  private[exp] def rewardThisPlayerDeath(
                                          victim: PlayerSource,
                                          lastDamage: Option[DamageResult],
                                          history: Iterable[InGameActivity],
                                          eventBus: ActorRef
                                        ): Unit = {
    val shortHistory = limitHistoryToThisLife(history.toList)
    val everyone = determineKiller(lastDamage, shortHistory) match {
      case Some((result, killer: PlayerSource)) =>
        val assists = collectKillAssistsForPlayer(victim, shortHistory, Some(killer))
        val fullBep = calculateExperience(killer, victim, shortHistory)
        val hitSquad = (killer, Kill(victim, result, fullBep)) +: assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, shortHistory.last.time - shortHistory.head.time, fullBep)) +: hitSquad

      case _ =>
        val assists = collectKillAssistsForPlayer(victim, shortHistory, None)
        val fullBep = Support.baseExperience(victim, shortHistory)
        val hitSquad = assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, shortHistory.last.time - shortHistory.head.time, fullBep)) +: hitSquad
    }
    everyone.foreach { case (p, kda) =>
      eventBus ! AvatarServiceMessage(p.Name, AvatarAction.UpdateKillsDeathsAssists(p.CharId, kda))
    }
  }

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
    if (Support.wasEverAMax(victim, history)) { //a cardinal sin
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

  private def healthDamageContributors(
                                             history: List[InGameActivity],
                                             faction: PlanetSideEmpire.Value,
                                             participants: mutable.LongMap[ContributionStats]
                                           ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
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

  private def armorDamageContributors(
                                            history: List[InGameActivity],
                                            faction: PlanetSideEmpire.Value,
                                            participants: mutable.LongMap[ContributionStats]
                                          ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
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

  private[exp] def contributeWithRecoveryActivity(
                                                   amount: Int,
                                                   participants: mutable.LongMap[ContributionStats],
                                                   order: Seq[(Long, Int)]
                                                 ): Seq[(Long, Int)] = {
    var amt = amount
    var count = 0
    var newOrder: Seq[(Long, Int)] = Nil
    order.takeWhile { entry =>
      val (id, total) = entry
      if (id > 0 && total > 0) {
        val part = participants(id)
        if (amount > total) {
          //drop this entry
          participants.put(id, part.copy(amount = 0, weapons = Nil)) //just in case
          amt = amt - total
        } else {
          //edit around the inclusion of this entry
          val newTotal = total - amt
          val trimmedWeapons = {
            var index = -1
            var weaponSum = 0
            val pweapons = part.weapons
            while (weaponSum < amt) {
              index += 1
              weaponSum = weaponSum + pweapons(index).amount
            }
            (pweapons(index).copy(amount = weaponSum - amt) +: pweapons.slice(index+1, pweapons.size)) ++
              pweapons.slice(0, index).map(_.copy(amount = 0))
          }
          newOrder = (id, newTotal) +: newOrder
          participants.put(id, part.copy(amount = part.amount - amount, weapons = trimmedWeapons))
          amt = 0
        }
      }
      count += 1
      amt > 0
    }
    newOrder ++ order.drop(count)
  }
}
