// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.{PlayerSource, SourceUniqueness}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.vital.{DamagingActivity, HealFromEquipment, HealFromTerminal, HealingActivity, InGameActivity, RepairFromEquipment, RepairFromTerminal, RepairingActivity, RevivingActivity, SupportActivityCausedByAnother}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

sealed trait RecoveryExperienceContribution {
  def submit(history: List[InGameActivity]): Unit
  def output(): mutable.LongMap[ContributionStats]
  def clear(): Unit
}

object RecoveryExperienceContribution {
  private[exp] def contributeWithDamagingActivity(
                                              activity: DamagingActivity,
                                              amount: Int,
                                              damageParticipants: mutable.LongMap[PlayerSource],
                                              recoveryParticipants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    //mark entries from the ordered recovery list to truncate
    val data: DamageResult = activity.data
    val time: Long = activity.time
    var lastCharId: Long = 0L
    var lastValue: Int = 0
    var ramt: Int = amount
    var rindex: Int = 0
    val riter = recoveryOrder.iterator
    while (riter.hasNext && ramt > 0) {
      val (id, value) = riter.next()
      if (value > 0) {
        /*
        if the amount on the previous recovery node is positive, reduce it by the damage value for that user's last used equipment
        keep traversing recovery nodes, and lobbing them off, until the recovery amount is zero
        if the user can not be found having an entry, skip the update but lob off the recovery progress node all the same
        if the amount is zero, do not check any further recovery progress nodes
         */
        recoveryParticipants
          .get(id)
          .foreach { entry =>
            val weapons = entry.weapons
            lastCharId = id
            lastValue = value
            if (value > ramt) {
              //take from the value on the last-used equipment, at the front of the list
              recoveryParticipants.put(
                id,
                entry.copy(
                  weapons = weapons.head.copy(amount = math.max(0, weapons.head.amount - ramt), time = time) +: weapons.tail,
                  amount = math.max(0, entry.amount - ramt),
                  time = time
                )
              )
              ramt = 0
              lastValue = lastValue - value
            } else {
              //take from the value on the last-used equipment, at the front of the list
              //move that entry to the end of the list
              recoveryParticipants.put(
                id,
                entry.copy(
                  weapons = weapons.tail :+ weapons.head.copy(amount = 0, time = time),
                  amount = math.max(0, entry.amount - ramt),
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
    }
    //damage order and damage contribution entry
    val newDamageEntry = data
      .adversarial
      .collect { case Adversarial(p: PlayerSource, _, _) => (p, damageParticipants.get(p.CharId)) }
      .collect {
        case (player, Some(PlayerSource.Nobody)) =>
          damageParticipants.put(player.CharId, player)
          Some(player)
        case (player, Some(_)) =>
          damageParticipants.getOrElseUpdate(player.CharId, player)
          Some(player)
      }
      .collect {
        case Some(player) => (player.CharId, amount) //for damageOrder
      }
      .orElse {
        Some((0L, amount)) //for damageOrder
      }
    //re-combine output list(s)
    val leftovers = if (lastValue > 0) {
      Seq((lastCharId, lastValue))
    } else {
      Nil
    }
    (newDamageEntry.toList ++ damageOrder, leftovers ++ recoveryOrder.slice(rindex, recoveryOrder.size) ++ recoveryOrder.take(rindex).map { case (id, _) => (id, 0) })
  }

  private[exp] def contributeWithRecoveryActivity(
                                              user: PlayerSource,
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              damageParticipants: mutable.LongMap[PlayerSource],
                                              recoveryParticipants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    contributeWithRecoveryActivity(user, user.CharId, wepid, faction, amount, time, damageParticipants, recoveryParticipants, damageOrder, recoveryOrder)
  }

  private[exp] def contributeWithRecoveryActivity(
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              damageParticipants: mutable.LongMap[PlayerSource],
                                              recoveryParticipants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    contributeWithRecoveryActivity(PlayerSource.Nobody, charId = 0, wepid, faction, amount, time, damageParticipants, recoveryParticipants, damageOrder, recoveryOrder)
  }

  private[exp] def contributeWithRecoveryActivity(
                                              user: PlayerSource,
                                              charId: Long,
                                              wepid: Int,
                                              faction: PlanetSideEmpire.Value,
                                              amount: Int,
                                              time: Long,
                                              damageParticipants: mutable.LongMap[PlayerSource],
                                              recoveryParticipants: mutable.LongMap[ContributionStats],
                                              damageOrder: Seq[(Long, Int)],
                                              recoveryOrder: Seq[(Long, Int)]
                                            ): (Seq[(Long, Int)], Seq[(Long, Int)]) = {
    //mark entries from the ordered damage list to truncate
    val damageEntries = damageOrder.iterator
    var amtToReduce: Int = amount
    var amtToGain: Int = 0
    var lastValue: Int = -1
    var damageRemoveCount: Int = 0
    var damageRemainder: Seq[(Long, Int)] = Nil
    //keep reducing previous damage until recovery amount is depleted, or no more damage entries remain, or the last damage entry was depleted already
    while (damageEntries.hasNext && amtToReduce > 0 && lastValue != 0) {
      val (id, value) = damageEntries.next()
      lastValue = value
      if (value > 0) {
        damageParticipants
          .get(id)
          .collect {
            case player if player.Faction != faction =>
              //if previous attacker was an enemy, the recovery counts towards contribution
              if (value > amtToReduce) {
                damageRemainder = Seq((id, value - amtToReduce))
                amtToGain = amtToGain + amtToReduce
                amtToReduce = 0
              } else {
                amtToGain = amtToGain + value
                amtToReduce = amtToReduce - value
              }
              Some(player)
            case player =>
              //if the previous attacker was friendly fire, the recovery doesn't count towards contribution
              if (value > amtToReduce) {
                damageRemainder = Seq((id, value - amtToReduce))
                amtToReduce = 0
              } else {
                amtToReduce = amtToReduce - value
              }
              Some(player)
          }
          .orElse {
            //if we couldn't find an entry, just give the contribution to the user anyway
            damageParticipants.put(id, PlayerSource.Nobody)
            if (value > amtToReduce) {
              damageRemainder = Seq((id, value - amtToReduce))
              amtToGain = amtToGain + amtToReduce
              amtToReduce = 0
            } else {
              amtToGain = amtToGain + value
              amtToReduce = amtToReduce - value
            }
            None
          }
        //keep track of entries whose damage was depleted
        damageRemoveCount += 1
      }
    }
    amtToGain = amtToGain + amtToReduce //if early termination, gives leftovers as gain
    if (amtToGain > 0) {
      val newWeaponStats = WeaponStats(wepid, amtToGain, 1, time, 1f)
      //try: add first contribution entry
      //then: add accumulation of last weapon entry to contribution entry
      //last: add new weapon entry to contribution entry
      recoveryParticipants
        .getOrElseUpdate(
          charId,
          ContributionStats(user, Seq(newWeaponStats), amtToGain, amtToGain, 1, time)
        ) match {
        case entry if entry.weapons.size > 1 =>
          if (entry.weapons.head.equipment_id == wepid) {
            val head = entry.weapons.head
            recoveryParticipants.put(
              charId,
              entry.copy(
                weapons = head.copy(amount = head.amount + amtToGain, shots = head.shots + 1, time = time) +: entry.weapons.tail,
                amount = entry.amount + amtToGain,
                total = entry.total + amtToGain,
                shots = entry.shots + 1,
                time = time
              )
            )
          } else {
            recoveryParticipants.put(
              charId,
              entry.copy(
                weapons = newWeaponStats +: entry.weapons,
                amount = entry.amount + amtToGain,
                total = entry.total + amtToGain,
                shots = entry.shots + 1,
                time = time
              )
            )
          }
        case _ => ()
        //not technically possible
      }
    }
    val newRecoveryEntry = if (amtToGain == 0) {
      Seq((0L, amount))
    } else if (amtToGain < amount) {
      Seq((0L, amount - amtToGain), (charId, amtToGain))
    } else {
      Seq((charId, amount))
    }
    (
      damageRemainder ++ damageOrder.drop(damageRemoveCount) ++ damageOrder.take(damageRemoveCount).map { case (id, _) => (id, 0) },
      newRecoveryEntry ++ recoveryOrder
    )
  }

  private[exp] def contributeWithSupportRecoveryActivity(
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
    val damageParticipants: mutable.LongMap[PlayerSource] = mutable.LongMap[PlayerSource]()
    users.zip {
      val numberOfUsers = users.size
      val out = Array.fill(numberOfUsers)(numberOfUsers / amount)
      (0 to numberOfUsers % amount).foreach {
        out(_) += 1
      }
      out
    }.foreach { case (user, subAmount) =>
      val (a, b) = contributeWithRecoveryActivity(user, user.CharId, wepid, faction, subAmount, time, damageParticipants, participants, outputDamageOrder, outputRecoveryOrder)
      outputDamageOrder = a
      outputRecoveryOrder = b
    }
    (outputDamageOrder, outputRecoveryOrder)
  }
}

//noinspection ScalaUnusedSymbol
private abstract class RecoveryExperienceContributionProcess(
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

private class HealthRecoveryExperienceContributionProcess(
                                                           private val faction : PlanetSideEmpire.Value,
                                                           private val contributions: Map[SourceUniqueness, List[InGameActivity]]
                                                         ) extends RecoveryExperienceContributionProcess(faction, contributions) {
  def submit(history: List[InGameActivity]): Unit = {
    history.foreach {
      case d: DamagingActivity if d.health > 0 =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithDamagingActivity(d, d.health, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case ht: HealFromTerminal =>
        val time = ht.time
        val users = KillContributions.contributeWithTerminalActivity(Seq((ht, ht.term, ht.term.hacked)), faction, contributions, contributionsBy)
          .collect { case entry: SupportActivityCausedByAnother => entry }
          .groupBy(_.user.unique)
          .map(_._2.head.user)
          .toSeq
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithSupportRecoveryActivity(users, ht.term.Definition.ObjectId, faction, ht.amount, time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealFromEquipment =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(h.user, h.equipment_def.ObjectId, faction, h.amount, h.time, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(wepid = 0, faction, h.amount, h.time, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RevivingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(r.equipment.ObjectId, faction, r.amount, r.time, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case _ => ()
    }
  }
}

private class ArmorRecoveryExperienceContributionProcess(
                                                          private val faction : PlanetSideEmpire.Value,
                                                          private val contributions: Map[SourceUniqueness, List[InGameActivity]]
                                                        ) extends RecoveryExperienceContributionProcess(faction, contributions) {
  def submit(history: List[InGameActivity]): Unit = {
    history.foreach {
      case d: DamagingActivity if d.amount - d.health > 0 =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithDamagingActivity(d, d.amount - d.health, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case rt: RepairFromTerminal =>
        val time = rt.time
        val users = KillContributions.contributeWithTerminalActivity(Seq((rt, rt.term, rt.term.hacked)), faction, contributions, contributionsBy)
          .collect { case entry: SupportActivityCausedByAnother => entry }
          .groupBy(_.user.unique)
          .map(_._2.head.user)
          .toSeq
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithSupportRecoveryActivity(users, rt.term.Definition.ObjectId, faction, rt.amount, time, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RepairFromEquipment =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(r.user, r.equipment_def.ObjectId, faction, r.amount, r.time, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RepairingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(wepid = 0, faction, r.amount, r.time, damageParticipants, participants, damageInOrder, recoveryInOrder)
        damageInOrder = damage
        recoveryInOrder = recovery
      case _ => ()
    }
  }
}

private class CombinedHealthAndArmorContributionProcess(
                                                         private val faction : PlanetSideEmpire.Value,
                                                         private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                         otherSubmissions: Seq[RecoveryExperienceContribution]
                                                       ) extends RecoveryExperienceContribution {
  private val process: Seq[RecoveryExperienceContributionProcess] = Seq(
    new HealthRecoveryExperienceContributionProcess(faction, contributions),
    new ArmorRecoveryExperienceContributionProcess(faction, contributions)
  )

  def submit(history: List[InGameActivity]): Unit = {
    for (elem <- process ++ otherSubmissions) { elem.submit(history) }
  }

  def output(): mutable.LongMap[ContributionStats] = {
    val output = combineRecoveryContributions(
      KillContributions.cullContributorImplements(process.head.output()),
      KillContributions.cullContributorImplements(process(1).output())
    )
    clear()
    output
  }

  def clear(): Unit = {
    process.foreach ( _.clear() )
  }

  private def combineRecoveryContributions(
                                            healthAssists: mutable.LongMap[ContributionStats],
                                            armorAssists: mutable.LongMap[ContributionStats]
                                          ): mutable.LongMap[ContributionStats] = {
    healthAssists
      .map {
        case out@(id, healthEntry) =>
          armorAssists.get(id) match {
            case Some(armorEntry) =>
              //healthAssists && armorAssists
              (id, healthEntry.copy(weapons = healthEntry.weapons ++ armorEntry.weapons))
            case None =>
              //healthAssists only
              out
          }
      }
      .addAll {
        //armorAssists only
        val healthKeys = healthAssists.keys.toSeq
        armorAssists.filter { case (id, _) => !healthKeys.contains(id) }
      }
      .map {
        case (id, entry) =>
          val groupedWeapons = entry.weapons
            .groupBy(_.equipment_id)
            .map {
              case (weaponId, weaponEntries) =>
                val specificEntries = weaponEntries.filter(_.equipment_id == weaponId)
                val amount = specificEntries.foldLeft(0)(_ + _.amount)
                val shots = specificEntries.foldLeft(0)(_ + _.shots)
                WeaponStats(weaponId, amount, shots, specificEntries.maxBy(_.time).time, 1f)
            }
            .toSeq
          (id, ContributionStats(
            player = entry.player,
            weapons = groupedWeapons,
            amount = entry.amount + entry.amount,
            total = entry.total + entry.total,
            shots = groupedWeapons.foldLeft(0)(_ + _.shots),
            time = groupedWeapons.maxBy(_.time).time
          ))
      }
  }
}
