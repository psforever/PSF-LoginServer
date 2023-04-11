// Copyright (c) NOT VERSIONED PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.definition.WithShields
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, SourceWithHealthEntry, SourceWithShieldsEntry}
import net.psforever.objects.vital.{DamagingActivity, InGameActivity, RepairingActivity, ShieldCharge, SpawningActivity}
import net.psforever.objects.vital.interaction.Adversarial
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

object KillDeathAssists2 {
  private def collectKillAssists(
                                  victim: SourceEntry,
                                  history: List[InGameActivity],
                                  func: (List[InGameActivity], PlanetSideEmpire.Value) => mutable.LongMap[ContributionStats]
                                ): mutable.LongMap[ContributionStatsOutput] = {
    val assists = func(history, victim.Faction).filterNot { case (_, kda) => kda.amount <= 0 }
    val total = assists.values.foldLeft(0f)(_ + _.total)
    val output = assists.map { case (id, kda) =>
      (id, ContributionStatsOutput(kda.player, kda.weapons.map { _.weapon_id }, kda.amount / total))
    }
    output.remove(victim.CharId)
    output
  }

  private[exp] def collectAssistsForEntity(
                                            target: SourceWithHealthEntry,
                                            history: List[InGameActivity],
                                            killerOpt: Option[PlayerSource]
                                          ): Iterable[ContributionStatsOutput] = {
    val (_, maxShields) = maximumEntityHealthAndShields(history)
    val healthAssists = collectKillAssists(
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

  private[exp] def contributeWithDamagingActivity(
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
              time = time
            )
          case None =>
            //new attacker, new entry
            ContributionStats(
              user,
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
