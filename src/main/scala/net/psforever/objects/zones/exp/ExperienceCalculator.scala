// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.avatar.scoring.{Assist, Death, Kill}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.{DamagingActivity, HealingActivity, InGameActivity, InGameHistory, ReconstructionActivity, RepairFromExoSuitChange, RepairingActivity, SpawningActivity}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.zones.Zone
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{ExoSuitType, PlanetSideEmpire}

import scala.collection.mutable
import scala.concurrent.duration._

object ExperienceCalculator {
  def apply(zone: Zone): Behavior[Command] =
    Behaviors.supervise[Command] {
      Behaviors.setup(context => new ExperienceCalculator(context, zone))
    }.onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class RewardThisDeath(victim: SourceEntry, lastDamage: Option[DamageResult], history: Iterable[InGameActivity])
    extends ExperienceCalculator.Command

  object RewardThisDeath {
    def apply(obj: PlanetSideGameObject with FactionAffinity with InGameHistory): RewardThisDeath = {
      RewardThisDeath(SourceEntry(obj), obj.LastDamage, obj.History)
    }
  }

  def calculateExperience(
                           victim: PlayerSource,
                           history: Iterable[InGameActivity]
                         ): Long = {
    val lifespan = (history.headOption, history.lastOption) match {
      case (Some(spawn), Some(death)) => death.time - spawn.time
      case _                          => 0L
    }
    val wasEverAMax = victim.ExoSuit == ExoSuitType.MAX || history.exists {
      case SpawningActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case ReconstructionActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case RepairFromExoSuitChange(suit, _) => suit == ExoSuitType.MAX
      case _                                => false
    }
    val base = if (wasEverAMax) { //shamed
      250L
    } else if (victim.Seated || victim.kills.nonEmpty) {
      100L
    } else if (lifespan > 15000L) {
      50L
    } else {
      1L
    }
    if (base > 1) {
      //black ops modifier
      //TODO x10
      base
    } else {
      base
    }
  }
}

class ExperienceCalculator(context: ActorContext[ExperienceCalculator.Command], zone: Zone)
  extends AbstractBehavior[ExperienceCalculator.Command](context) {

  import ExperienceCalculator._

  def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RewardThisDeath(victim: PlayerSource, lastDamage, history) =>
        rewardThisPlayerDeath(
          victim,
          lastDamage,
          limitHistoryToThisLife(history.toList)
        )
      case _ =>
        ()
    }
    Behaviors.same
  }

  def rewardThisPlayerDeath(
                             victim: PlayerSource,
                             lastDamage: Option[DamageResult],
                             history: List[InGameActivity]
                           ): Unit = {
    val everyone = determineKiller(lastDamage, history) match {
      case Some((result, killer: PlayerSource)) =>
        val assists = collectAssistsForPlayer(victim, history, Some(killer))
        val fullBep = KillDeathAssists.calculateExperience(killer, victim, history)
        val hitSquad = (killer, Kill(victim, result, fullBep)) +: assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, history.last.time - history.head.time, fullBep)) +: hitSquad

      case _ =>
        val assists = collectAssistsForPlayer(victim, history, None)
        val fullBep = ExperienceCalculator.calculateExperience(victim, history)
        val hitSquad = assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, history.last.time - history.head.time, fullBep)) +: hitSquad
    }
    val events = zone.AvatarEvents
    everyone.foreach { case (p, kda) =>
      events ! AvatarServiceMessage(p.Name, AvatarAction.UpdateKillsDeathsAssists(p.CharId, kda))
    }
  }

  def limitHistoryToThisLife(history: List[InGameActivity]): List[InGameActivity] = {
    val spawnIndex = history.indexWhere {
      case SpawningActivity(_, _, _) => true
      case _ => false
    }
    val endIndex = history.lastIndexWhere {
      case damage: DamagingActivity => damage.data.targetAfter.asInstanceOf[PlayerSource].Health == 0
      case _ => false
    }
    if (spawnIndex == -1 || endIndex == -1) {
      Nil //throw VitalsHistoryException(history.head, "vitals history does not contain expected conditions")
//    } else
//    if (spawnIndex == -1) {
//      Nil  //throw VitalsHistoryException(history.head, "vitals history does not contain initial spawn conditions")
//    } else if (endIndex == -1) {
//      Nil  //throw VitalsHistoryException(history.last, "vitals history does not contain end of life conditions")
    } else {
      history.slice(spawnIndex, endIndex)
    }
  }

  def determineKiller(lastDamageActivity: Option[DamageResult], history: List[InGameActivity]): Option[(DamageResult, SourceEntry)] = {
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

  private[exp] def collectAssistsForPlayer(
                                            victim: PlayerSource,
                                            history: List[InGameActivity],
                                            killerOpt: Option[PlayerSource]
                                          ): Iterable[ContributionStatsOutput] = {
    //    val cardinalSin = victim.ExoSuit == ExoSuitType.MAX || history.exists {
    //      case SpawningActivity(p: PlayerSource,_,_) => p.ExoSuit == ExoSuitType.MAX
    //      case RepairFromExoSuitChange(suit, _) => suit == ExoSuitType.MAX
    //      case _ => false
    //    }
    val initialHealth = history
      .headOption
      .collect { case SpawningActivity(p: PlayerSource,_,_) => p.health } match {
      case Some(value) => value.toFloat
      case _ => 100f
    }
    val healthAssists = collectHealthAssists(
      victim,
      history,
      initialHealth,
      allocateContributors(healthDamageContributors)
    )
    healthAssists.remove(0L)
    killerOpt.map { killer => healthAssists.remove(killer.CharId) }
    healthAssists.values
  }

  private def allocateContributors(
                                    tallyFunc: (List[InGameActivity], PlanetSideEmpire.Value, mutable.LongMap[ContributionStats]) => Any
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



  private def healthDamageContributors(
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

  private def collectHealthAssists(
                                    victim: SourceEntry,
                                    history: List[InGameActivity],
                                    topHealth: Float,
                                    func: (List[InGameActivity], PlanetSideEmpire.Value)=>mutable.LongMap[ContributionStats]
                                  ): mutable.LongMap[ContributionStatsOutput] = {
    val healthAssists = func(history, victim.Faction)
      .filterNot { case (_, kda) => kda.amount <= 0 }
      .map { case (id, kda) =>
        (id, ContributionStatsOutput(kda.player, kda.weapons.map { _.weapon_id }, kda.amount / topHealth))
      }
    healthAssists.remove(victim.CharId)
    healthAssists
  }

  private def contributeWithDamagingActivity(
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
        val updatedEntry = participants.get(whoId) match {
          case Some(mod) =>
            //previous attacker, just add to entry
            val firstWeapon = mod.weapons.head
            val weapons = if (firstWeapon.weapon_id == wepid) {
              firstWeapon.copy(amount = firstWeapon.amount + amount, shots = firstWeapon.shots + 1, time = time) +: mod.weapons.tail
            } else {
              WeaponStats(wepid, amount, 1, time) +: mod.weapons
            }
            mod.copy(
              amount = mod.amount + amount,
              weapons = weapons,
              totalDamage = mod.totalDamage + amount,
              shots = mod.shots + 1,
              time = activity.time
            )
          case None =>
            //new attacker, new entry
            ContributionStats(
              attacker,
              Seq(WeaponStats(wepid, amount, 1, time)),
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

  private def contributeWithRecoveryActivity(
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
