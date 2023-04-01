// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.definition.{ExoSuitDefinition, WithShields}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, SourceWithHealthEntry, SourceWithShieldsEntry}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.vital.{DamagingActivity, HealingActivity, InGameActivity, RepairFromExoSuitChange, RepairingActivity, ShieldCharge, SpawningActivity}
import net.psforever.types.{ExoSuitType, PlanetSideEmpire}

import scala.collection.mutable
import scala.concurrent.duration._

object KillDeathAssists {
  private [exp] def determineKiller(
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

  private[exp] def calculateExperience(
                                        killer: PlayerSource,
                                        victim: PlayerSource,
                                        history: Iterable[InGameActivity]
                                      ): Long = {
    //base value (the kill experience before modifiers)
    val base = ExperienceCalculator.calculateExperience(victim, history)
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
      math.max(1, base + battleRankDisparity)
    } else {
      base
    }
  }

  private[exp] def collectAssistsForEntity(
                                            target: SourceWithHealthEntry,
                                            history: List[InGameActivity],
                                            killerOpt: Option[PlayerSource]
                                          ): Iterable[ContributionStatsOutput] = {
    val (_, maxShields) = maximumEntityHealthAndShields(history)
    val healthAssists = Support.collectHealthAssists(
      target,
      history,
      Support.allocateContributors(entityHealthDamageContributors)
    )
    if (maxShields > 0) {
      val shieldAssists = collectShieldAssists(target.asInstanceOf[SourceWithShieldsEntry], history)
      killerOpt.collect {
        case killer =>
          healthAssists.remove(killer.CharId)
          shieldAssists.remove(killer.CharId)
      }
      Support.onlyOriginalAssistEntries(healthAssists, shieldAssists)
    } else {
      killerOpt.collect {
        case killer =>
          healthAssists.remove(killer.CharId)
      }
      healthAssists.values
    }
  }

  private[exp] def collectMaxArmorAssists(
                                           victim: PlayerSource,
                                           history: List[InGameActivity],
                                           fullArmor: Float
                                         ): mutable.LongMap[ContributionStatsOutput] = {
    val initialArmorAssists = Support.allocateContributors(maxArmorDamageContributors)(history, victim.Faction)
    val allArmorDamage = initialArmorAssists.values.foldLeft(0f)(_ + _.total)
    val flatComparativeRate = if (allArmorDamage > fullArmor * 1.35f) {
      0.4f //this max has been damaged and repaired a lot
    } else {
      0.65f
    }
    val armorAssists = initialArmorAssists.map { case (id, kda) =>
      val averageRateOfAllWeapons = kda.weapons.map { stat => stat.contributions / stat.shots }.sum / kda.weapons.size
      val finalRate = math.max(flatComparativeRate, (flatComparativeRate + averageRateOfAllWeapons) * 0.5f)
      (id, ContributionStatsOutput(kda.player, kda.weapons.map { _.weapon_id }, finalRate))
    }
    armorAssists.remove(victim.CharId)
    armorAssists
  }

  private def maxArmorDamageContributors(
                                          history: List[InGameActivity],
                                          faction: PlanetSideEmpire.Value,
                                          participants: mutable.LongMap[ContributionStats]
                                        ): Seq[(Long, Int)] = {
    var isMax = history.head match {
      case SpawningActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case _                         => false
    }
    /** damage as it is measured in order (with repair-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.tail.collect {
      case d: DamagingActivity if isMax || d.amount - d.health == d.amount =>
        inOrder = contributeWithDamagingActivity(d, faction, d.amount, participants, inOrder)

      case RepairFromExoSuitChange(suit, _) =>
        isMax = suit == ExoSuitType.MAX

      case r: RepairingActivity =>
        inOrder = contributeWithRecoveryActivity(r.amount, participants, inOrder)
    }
    inOrder
  }

  private[exp] def collectShieldAssists(
                                         victim: SourceWithShieldsEntry,
                                         history: List[InGameActivity],
                                       ): mutable.LongMap[ContributionStatsOutput] = {
    val initialShieldAssists = Support.allocateContributors(entityShieldDamageContributors)(history, victim.Faction)
      .filterNot { case (_, kda) => kda.amount <= 0 }
    val fullFloatShield = initialShieldAssists.values.foldLeft(0)(_ + _.total).toFloat
    val shieldAssists = initialShieldAssists.map { case (id, kda) =>
      (id, ContributionStatsOutput(kda.player, kda.weapons.map { _.weapon_id }, math.min(kda.total / fullFloatShield, 0.4f)))
    }
    shieldAssists.remove(victim.CharId)
    shieldAssists
  }

  private def entityShieldDamageContributors(
                                              history: List[InGameActivity],
                                              faction: PlanetSideEmpire.Value,
                                              participants: mutable.LongMap[ContributionStats]
                                            ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.tail.foreach {
      case d: DamagingActivity if d.amount - d.health > 0 =>
        inOrder = contributeWithDamagingActivity(d, faction, d.amount - d.health, participants, inOrder)
      case v: ShieldCharge if v.amount > 0 =>
        inOrder = contributeWithRecoveryActivity(v.amount, participants, inOrder)
      case _ => ()
    }
    inOrder
  }

  /**
   * Determine the maximum amount of health and armor that a player character had
   * before their event and time of death.<br>
   * <br>
   * For health value, the number is determined by starting with their spawn health and tracking heals and damage.
   * For armor value, the number is determined in the same way at first,
   * by starting with their spawn armor and tracking repairs and damage.
   * If and only after being bulk set by an exosuit change event once, however,
   * the only things that matter are exosuit change events.
   * @param history na
   * @return a pair of the aforementioned integers, the maximum health value and the maximum armor value
   */
  private[exp] def maximumPlayerHealthAndArmor(history: List[InGameActivity]): (Int, Int) = {
    var (health, armor, faction) = (0, 0, PlanetSideEmpire.NEUTRAL)
    history.collect {
      case SpawningActivity(target: PlayerSource, _, _) =>
        health = math.max(target.Definition.MaxHealth, health)
        armor = math.max(ExoSuitDefinition.Select(target.ExoSuit, target.Faction).MaxArmor, armor)
        faction = target.Faction
      case d: DamagingActivity =>
        val target = d.data.targetBefore.asInstanceOf[PlayerSource]
        health = math.max(target.Definition.MaxHealth, health)
        armor = math.max(ExoSuitDefinition.Select(target.ExoSuit, target.Faction).MaxArmor, armor)
    }
    (health, armor)
  }

//  private def initialPlayerHealthAndArmor(history: List[InGameActivity]): (Int, Int, PlanetSideEmpire.Value) = {
//    history.collectFirst {
//      case SpawningActivity(target: PlayerSource, _, _) =>
//        (target.health, target.armor, target.Faction)
//      case d: DamagingActivity =>
//        val target = d.data.targetBefore.asInstanceOf[PlayerSource]
//        (target.health, target.armor, target.Faction)
//    }.getOrElse((0, 0, PlanetSideEmpire.NEUTRAL))
//  }

  private def maximumEntityHealthAndShields(history: List[InGameActivity]): (Int, Int) = {
    var (health, shields) = (0, 0)
    history.collect {
      case SpawningActivity(target: SourceWithHealthEntry with SourceWithShieldsEntry, _, _) =>
        health = math.max(target.Definition.MaxHealth, health)
        shields = math.max(target.Definition.asInstanceOf[WithShields].MaxShields, shields)
      case SpawningActivity(target: SourceWithHealthEntry, _, _) =>
        health = math.max(target.Definition.MaxHealth, health)
      case d: DamagingActivity =>
        d.data.targetBefore match {
          case target: SourceWithHealthEntry with SourceWithShieldsEntry =>
            health = math.max(target.Definition.MaxHealth, health)
            shields = math.max(target.Definition.asInstanceOf[WithShields].MaxShields, shields)
          case target: SourceWithHealthEntry =>
            health = math.max(target.health, health)
          case _ => ()
        }
    }
    (health, shields)
  }

//  private def initialEntityHealthAndShields(history: List[InGameActivity]): (Int, Int, PlanetSideEmpire.Value) = {
//    history.collectFirst {
//      case SpawningActivity(target: SourceWithHealthEntry with SourceWithShieldsEntry, _, _) =>
//        (target.health, target.shields, target.Faction)
//      case SpawningActivity(target: SourceWithHealthEntry, _, _) =>
//        (target.health, 0, target.Faction)
//      case d: DamagingActivity =>
//        d.data.targetBefore match {
//          case target: SourceWithHealthEntry with SourceWithShieldsEntry =>
//            (target.health, target.shields, target.Faction)
//          case target: SourceWithHealthEntry =>
//            (target.health, 0, target.Faction)
//          case _ =>
//            (0, 0, PlanetSideEmpire.NEUTRAL)
//        }
//    }.getOrElse((0, 0, PlanetSideEmpire.NEUTRAL))
//  }

  private[exp] def healthDamageContributors(
                                             history: List[InGameActivity],
                                             faction: PlanetSideEmpire.Value,
                                             participants: mutable.LongMap[ContributionStats]
                                           ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.tail.foreach {
      case d: DamagingActivity if d.health > 0 =>
        inOrder = contributeWithDamagingActivity(d, faction, d.health, participants, inOrder)
      case _: RepairingActivity => ()
      case h: HealingActivity =>
        inOrder = contributeWithRecoveryActivity(h.amount, participants, inOrder)
      case _ => ()
    }
    inOrder
  }

  private[exp] def contributeWithDamagingActivity(
                                              activity: DamagingActivity,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              participants: mutable.LongMap[ContributionStats],
                                              order: Seq[(Long, Int)]
                                            ): Seq[(Long, Int)] = {
    activity.data.adversarial match {
      case Some(Adversarial(attacker: PlayerSource, _, _))
        if attacker.Faction != faction =>
        val whoId = attacker.CharId
        val wepid = activity.data.interaction.cause.attribution
        val time = activity.time
        val percentage = amount / attacker.Definition.MaxHealth.toFloat
        val updatedEntry = participants.get(whoId) match {
          case Some(mod) =>
            //previous attacker, just add to entry
            val firstWeapon = mod.weapons.head
            val weapons = if (firstWeapon.weapon_id == wepid) {
              firstWeapon.copy(
                amount = firstWeapon.amount + amount,
                shots = firstWeapon.shots + 1,
                time = time,
                contributions = firstWeapon.contributions + percentage
              ) +: mod.weapons.tail
            } else {
              WeaponStats(wepid, amount, 1, time, percentage) +: mod.weapons
            }
            mod.copy(
              amount = mod.amount + amount,
              weapons = weapons,
              total = mod.total + amount,
              shots = mod.shots + 1,
              time = activity.time
            )
          case None =>
            //new attacker, new entry
            ContributionStats(
              attacker,
              Seq(WeaponStats(wepid, amount, 1, time, percentage)),
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

  private def entityHealthDamageContributors(
                                              history: List[InGameActivity],
                                              faction: PlanetSideEmpire.Value,
                                              participants: mutable.LongMap[ContributionStats]
                                            ): Seq[(Long, Int)] = {
    /** damage as it is measured in order (with heal-countered damage eliminated)<br>
     * key - character identifier,
     * value - current damage contribution */
    var inOrder: Seq[(Long, Int)] = Seq[(Long, Int)]()
    history.collect {
      case d: DamagingActivity if d.health > 0 =>
        inOrder = contributeWithDamagingActivity(d, faction, d.health, participants, inOrder)

      case r: RepairingActivity if r.amount > 0 =>
        inOrder = contributeWithRecoveryActivity(r.amount, participants, inOrder)
    }
    inOrder
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
            pweapons(index).copy(amount = weaponSum - amt) +: pweapons.slice(index+1, pweapons.size)
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
