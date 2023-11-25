// Copyright (c) 2021 PSForever
package net.psforever.services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Default, Player}
import net.psforever.packet.game.{GenericAction, PlanetsideAttributeEnum}
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.zones.ZoneHotSpotProjector
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import net.psforever.util.Config

import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Random, Success}

/**
 * Responsible for handling the aspects related to hacking control consoles and capturing bases.
 */
class HackCaptureActor extends Actor {
  private[this] val log = org.log4s.getLogger
  /** main timer for completing or clearing hacked states */
  private var clearTrigger: Cancellable = Default.Cancellable
  /** list of currently hacked server objects */
  private var hackedObjects: List[HackCaptureActor.HackEntry] = Nil

  def receive: Receive = {
    case HackCaptureActor.StartCaptureTerminalHack(target, zone, unk1, unk2, startTime) =>
      log.trace(s"StartCaptureTerminalHack: ${target.GUID} is hacked.")
      val duration = target.Definition.FacilityHackTime
      target.HackedBy match {
        case Some(hackInfo) =>
          target.HackedBy = hackInfo.Duration(duration.toNanos)
        case None =>
          log.error(s"Initial $target hack information is missing")
      }
      hackedObjects.find(_.target == target).foreach { _ =>
        log.trace(
          s"StartCaptureTerminalHack: ${target.GUID} was already hacked - removing it from the hacked objects list before re-adding it."
        )
        hackedObjects = hackedObjects.filterNot(x => x.target == target)
      }
      hackedObjects = hackedObjects :+ HackCaptureActor.HackEntry(target, zone, unk1, unk2, duration, startTime)
      // Restart the timer, in case this is the first object in the hacked objects list or the object was removed and re-added
      RestartTimer()
      NotifyHackStateChange(target, isResecured = false)
      TrySpawnCaptureFlag(target)

    case HackCaptureActor.ProcessCompleteHacks() =>
      log.trace("Processing complete hacks")
      clearTrigger.cancel()
      val now: Long     = System.nanoTime
      val (stillHacked, finishedHacks) = hackedObjects.partition(x => now - x.hack_timestamp < x.duration.toNanos)
      hackedObjects = stillHacked
      finishedHacks.foreach { entry =>
        val terminal = entry.target
        log.trace(s"ProcessCompleteHacks: capture terminal hack timeout reached for terminal ${terminal.GUID}")
        val hackInfo = terminal.HackedBy.get
        val hacker = hackInfo.player
        val hackedByFaction = hackInfo.hackerFaction
        terminal.Actor ! CommonMessages.ClearHack()
        // If the base has a socket, but no flag spawned it means the hacked base is neutral with no friendly neighbouring bases to deliver to, making it a timed hack.
        val building = terminal.Owner.asInstanceOf[Building]
        building.GetFlag match {
          case Some(llu) =>
            // LLU was not delivered in time. Send resecured notifications
            terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(llu, CaptureFlagLostReasonEnum.TimedOut)
            NotifyHackStateChange(terminal, isResecured = true)

          case _ =>
            // Timed hack finished (or neutral LLU base with no neighbour as timed hack), capture the base
            HackCompleted(terminal, hackedByFaction)
            HackCaptureActor.RewardFacilityCaptureParticipants(
              building,
              terminal,
              hacker,
              now - entry.hack_timestamp,
              isResecured = false
            )
        }
      }
      // If there's hacked objects left in the list restart the timer with the shortest hack time left
      RestartTimer()

    case HackCaptureActor.ResecureCaptureTerminal(target, _, hacker) =>
      val (results, remainder) = hackedObjects.partition(x => x.target eq target)
      target.HackedBy = None
      hackedObjects = remainder
      val building = target.Owner.asInstanceOf[Building]
      // If LLU exists it was not delivered. Send resecured notifications
      building.GetFlag.collect {
        case flag: CaptureFlag => target.Zone.LocalEvents ! CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Resecured)
      }
      NotifyHackStateChange(target, isResecured = true)
//      HackCaptureActor.RewardFacilityCaptureParticipants(
//        building,
//        target,
//        hacker,
//        System.currentTimeMillis() - results.head.hack_timestamp,
//        isResecured = true
//      )
      // Restart the timer in case the object we just removed was the next one scheduled
      RestartTimer()

    case HackCaptureActor.FlagCaptured(flag) =>
      log.warn(hackedObjects.toString())
      val building = flag.Owner.asInstanceOf[Building]
      val bguid = building.CaptureTerminal.map { _.GUID }
      hackedObjects.find(entry => bguid.contains(entry.target.GUID)) match {
        case Some(entry) =>
          val terminal = entry.target
          val hackInfo = terminal.HackedBy.get
          val hacker =  hackInfo.player
          val hackedByFaction = hackInfo.hackerFaction
          hackedObjects = hackedObjects.filterNot(x => x == entry)
          HackCompleted(terminal, hackedByFaction)
//          HackCaptureActor.RewardFacilityCaptureParticipants(
//            building,
//            terminal,
//            hacker,
//            System.currentTimeMillis() - entry.hack_timestamp,
//            isResecured = false
//          )
          entry.target.Actor ! CommonMessages.ClearHack()
          flag.Zone.LocalEvents ! CaptureFlagManager.Captured(flag)
          // If there's hacked objects left in the list restart the timer with the shortest hack time left
          RestartTimer()

        case _ =>
          log.error(s"Attempted LLU capture for ${flag.Owner.asInstanceOf[Building].Name} but CC GUID ${flag.Owner.asInstanceOf[Building].CaptureTerminal.get.GUID} was not in list of hacked objects")
      }

    case _ => ()
  }

  private def TrySpawnCaptureFlag(terminal: CaptureTerminal): Boolean = {
    // Handle LLUs if the base contains a LLU socket
    // If there are no neighbouring bases belonging to the hacking faction this will be handled as a regular timed hack (e.g. neutral base in enemy territory)
    val hackingFaction = HackCaptureActor.GetHackingFaction(terminal).get
    (terminal.Owner match {
      case owner: Building if owner.IsCtfBase => Some((owner, owner.GetFlag, owner.Neighbours(hackingFaction)))
      case _ => None
    }) match {
      case Some((owner, None, Some(neighbours))) if neighbours.nonEmpty =>
        log.info(s"An LLU is being spawned for facility ${owner.Name} by $hackingFaction")
        spawnCaptureFlag(neighbours, terminal, hackingFaction)
        true
      case Some((owner, Some(flag), Some(neighbours))) if neighbours.nonEmpty && hackingFaction != flag.Faction =>
        log.info(s"$hackingFaction is overriding the ongoing LLU hack of facility ${owner.Name} by ${flag.Faction}")
        terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Ended)
        NotifyHackStateChange(terminal, isResecured = false)
        RestartTimer()
        spawnCaptureFlag(neighbours, terminal, hackingFaction)
        true
      case Some((owner, Some(flag), _)) if hackingFaction == flag.Faction =>
        log.error(s"TrySpawnCaptureFlag: owning faction and hacking faction match for facility ${owner.Name}; should we be resecuring instead?")
        false
      case Some((owner, _, _)) =>
        log.error(s"TrySpawnCaptureFlag: couldn't find any neighbouring $hackingFaction facilities of ${owner.Name} for LLU hack")
        owner.GetFlagSocket.foreach { _.clearOldFlagData() }
        terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(owner.GetFlag.get, CaptureFlagLostReasonEnum.Ended)
        false
      case _ =>
        log.error(s"TrySpawnCaptureFlag: expecting a terminal ${terminal.GUID.guid} with the ctf owning facility")
        false
    }
  }

  private def spawnCaptureFlag(
                                neighbours: Set[Building],
                                terminal: CaptureTerminal,
                                hackingFaction: PlanetSideEmpire.Value
                              ): Unit = {
    // Find a random neighbouring base matching the hacking faction
    val targetBase = neighbours.toVector((new Random).nextInt(neighbours.size))
    // Request LLU is created by CaptureFlagActor via LocalService
    terminal.Zone.LocalEvents ! CaptureFlagManager.SpawnCaptureFlag(terminal, targetBase, hackingFaction)
  }

  private def NotifyHackStateChange(
                                     terminal: CaptureTerminal,
                                     isResecured: Boolean
                                   ): Unit = {
    val attributeValue = HackCaptureActor.GetHackUpdateAttributeValue(terminal, isResecured)
    // Notify all clients that CC has had its hack state changed
    terminal.Zone.LocalEvents ! LocalServiceMessage(
      terminal.Zone.id,
      LocalAction.SendPlanetsideAttributeMessage(
        PlanetSideGUID(-1),
        terminal.GUID,
        PlanetsideAttributeEnum.ControlConsoleHackUpdate,
        attributeValue
      )
    )
    val owner = terminal.Owner.asInstanceOf[Building]
    // Notify parent building that state has changed
    owner.Actor ! BuildingActor.AmenityStateChange(terminal, Some(isResecured))
    // Push map update to clients
    owner.Zone.actor ! ZoneActor.ZoneMapUpdate()
  }

  private def HackCompleted(terminal: CaptureTerminal with Hackable, hackedByFaction: PlanetSideEmpire.Value): Unit = {
    val building = terminal.Owner.asInstanceOf[Building]
    if (building.NtuLevel > 0) {
      log.info(s"Setting base ${building.GUID} / MapId: ${building.MapId} as owned by $hackedByFaction")
      building.Actor! BuildingActor.SetFaction(hackedByFaction)
      //dispatch to players aligned with the capturing faction within the SOI
      val events = building.Zone.LocalEvents
      val msg = LocalAction.SendGenericActionMessage(Service.defaultPlayerGUID, GenericAction.FacilityCaptureFanfare)
      building
        .PlayersInSOI
        .collect { case p if p.Faction == hackedByFaction =>
          events ! LocalServiceMessage(p.Name, msg)
        }
    } else {
      log.info("Base hack completed, but base was out of NTU.")
    }
    NotifyHackStateChange(terminal, isResecured = true)
    // todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work.
    context.parent ! HackClearActor.SendHackMessageHackCleared(building.GUID, terminal.Zone.id, 3212836864L, 8L) //call up
  }

  private def RestartTimer(): Unit = {
    if (hackedObjects.nonEmpty) {
      val hackEntry = hackedObjects.reduceLeft(HackCaptureActor.minTimeLeft(System.nanoTime()))
      val short_timeout: FiniteDuration =
        math.max(1, hackEntry.duration.toNanos - (System.nanoTime - hackEntry.hack_timestamp)).nanoseconds
      log.trace(s"RestartTimer: still items left in hacked objects list. Checking again in ${short_timeout.toSeconds} seconds")
      import scala.concurrent.ExecutionContext.Implicits.global
      clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackCaptureActor.ProcessCompleteHacks())
    }
  }
}

object HackCaptureActor {
  final case class StartCaptureTerminalHack(
                                             target: CaptureTerminal,
                                             zone: Zone,
                                             unk1: Long,
                                             unk2: Long,
                                             startTime: Long = System.nanoTime()
                                           )

  final case class ResecureCaptureTerminal(target: CaptureTerminal, zone: Zone, hacker: PlayerSource)
  final case class FlagCaptured(flag: CaptureFlag)

  private final case class ProcessCompleteHacks()

  sealed case class HackEntry(
                               target: CaptureTerminal with Hackable,
                               zone: Zone,
                               unk1: Long,
                               unk2: Long,
                               duration: FiniteDuration,
                               hack_timestamp: Long
                             )

  def GetHackingFaction(terminal: CaptureTerminal): Option[PlanetSideEmpire.Value] = {
    terminal.HackedBy.map { a => a.player.Faction }
  }

  def GetHackUpdateAttributeValue(terminal: CaptureTerminal, isResecured: Boolean): Long = {
    terminal.HackedBy match {
      case _ if isResecured =>
        17039360L
      case Some(Hackable.HackInfo(p, _, start, length)) =>
        // See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
        val hackTimeRemainingMS =
          TimeUnit.MILLISECONDS.convert(math.max(0, start + length - System.nanoTime), TimeUnit.NANOSECONDS)
        val startNum = p.Faction match {
          case PlanetSideEmpire.TR => 0x10000
          case PlanetSideEmpire.NC => 0x20000
          case PlanetSideEmpire.VS => 0x30000
        }
        startNum + (hackTimeRemainingMS / 100) // Add time remaining as deciseconds
      case _ =>
        0L
    }
  }

  def minTimeLeft(now: Long)(
    entry1: HackCaptureActor.HackEntry,
    entry2: HackCaptureActor.HackEntry
  ): HackCaptureActor.HackEntry = {
    val entry1TimeLeft = entry1.duration.toNanos - (now - entry1.hack_timestamp)
    val entry2TimeLeft = entry2.duration.toNanos - (now - entry2.hack_timestamp)
    if (entry1TimeLeft < entry2TimeLeft) {
      entry1
    } else {
      entry2
    }
  }

  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  private implicit val timeout: Timeout = Timeout(5.seconds)

  private def RewardFacilityCaptureParticipants(
                                                 building: Building,
                                                 terminal: CaptureTerminal,
                                                 hacker: PlayerSource,
                                                 time: Long,
                                                 isResecured: Boolean
                                               ): Unit = {
    val faction: PlanetSideEmpire.Value = terminal.Faction
    val (contributionVictor, contributionAgainst) = building.PlayerContribution.keys.partition { _.Faction == faction }
    val contributionVictorSize = contributionVictor.size
    val flagCarrier = if (!isResecured) {
      building.GetFlagSocket.flatMap(_.previousFlag).flatMap(_.Carrier)
    } else {
      None
    }
    val request = ask(building.Zone.Activity, ZoneHotSpotProjector.ExposeHeatForRegion(building.Position, building.Definition.SOIRadius.toFloat))
    request.onComplete {
      case Success(ZoneHotSpotProjector.ExposedHeat(_, _, activity)) =>
        val (heatVictor, heatAgainst) = {
          val reports = activity.map { _.Activity }
          val allHeat: List[Long] = reports.map { a => a.values.foldLeft(0L)(_ + _.Heat) }
          val _rewardedHeat: List[Long] = reports.flatMap { rep => rep.get(faction).map { _.Heat.toLong } }
          val _enemyHeat = allHeat.indices.map { index =>
            val allHeatValue = allHeat(index)
            val rewardedHeatValue = _rewardedHeat(index)
            allHeatValue - rewardedHeatValue
          }
          (_rewardedHeat, _enemyHeat.toList)
        }
        val heatVictorSum: Long = heatVictor.sum[Long]
        val heatAgainstSum: Long = heatAgainst.sum[Long]
        if  (contributionVictorSize > 0) {
          val contributionRate = if (heatVictorSum * heatAgainstSum != 0) {
            math.log(heatVictorSum * contributionVictorSize / heatAgainstSum.toFloat).toFloat
          } else {
            contributionAgainst.size / contributionVictorSize.toFloat
          }
          RewardFacilityCaptureParticipants(building, terminal, faction, hacker, building.PlayersInSOI, flagCarrier, isResecured, time, contributionRate)
        }
      case _ =>
        RewardFacilityCaptureParticipants(building, terminal, faction, hacker, building.PlayersInSOI, flagCarrier, isResecured, time, victorContributionRate = 1.0f)
    }
    request.recover {
      _ => RewardFacilityCaptureParticipants(building, terminal, faction, hacker, building.PlayersInSOI, flagCarrier, isResecured, time, victorContributionRate = 1.0f)
    }
  }

  private def RewardFacilityCaptureParticipants(
                                                 building: Building,
                                                 terminal: CaptureTerminal,
                                                 faction: PlanetSideEmpire.Value,
                                                 hacker: PlayerSource,
                                                 targets: List[Player],
                                                 flagCarrier: Option[Player],
                                                 isResecured: Boolean,
                                                 hackTime: Long,
                                                 victorContributionRate: Float
                                               ): Unit = {
    val contribution = building.PlayerContribution
    val (contributionVictor, contributionAgainst) = contribution.keys.partition { _.Faction == faction }
    val contributionVictorSize = contributionVictor.size
    val contributionAgainstSize = contributionAgainst.size
    val (contributionByTime, contributionByTimePartitioned) = {
      val curr = System.currentTimeMillis()
      val interval = 300000
      val range: Seq[Long] = {
        val htime = hackTime.toInt
        (
          if (htime < 60000) {
            Seq(htime, interval + htime, 2 * interval + htime)
          } else if (htime <= interval) {
            Seq(60000, htime, interval + htime, 2 * interval + htime)
          } else {
            (60000 +: (interval to htime by interval)) ++ Seq(interval + htime, 2 * interval + htime)
          }
          ).map { _.toLong }
      }
      val playerMap = Array.fill[mutable.ListBuffer[Player]](range.size)(mutable.ListBuffer.empty)
      contribution.foreach { case (p, t) =>
        playerMap(range.lastIndexWhere(time => curr - t <= time)).addOne(p)
      }
      (playerMap, playerMap.map { _.partition(_.Faction == faction) })
    }
    val contributionByTimeSize = contributionByTime.length

    val base: Long = 50L
    val overallPopulationBonus = {
      contributionByTime.map { _.size }.sum * contributionByTimeSize +
        contributionByTime.zipWithIndex.map { case (lst, index) =>
          ((contributionByTimeSize - index) * lst.size *
            {
              val lists = contributionByTimePartitioned(index)
              lists._2.size / math.max(lists._1.size, 1).toFloat
            }).toLong
        }.sum
    }
    val competitionBonus: Long = if (contributionAgainstSize * 1.5f < contributionVictorSize.toFloat) {
      //steamroll by the victor
      25L * (contributionVictorSize - contributionAgainstSize)
    } else if (contributionVictorSize * 1.5f <= contributionAgainstSize.toFloat) {
      //victory against overwhelming odds
      500L + 50L * contribution.keys.size
    } else {
      //still a battle
      10L * math.min(contributionAgainstSize, contributionVictorSize)
    }
    val timeMultiplier: Float = {
      val buildingHackTimeMilli = terminal.Definition.FacilityHackTime.toMillis.toFloat
      1f + (if (isResecured) {
        (buildingHackTimeMilli - hackTime) / buildingHackTimeMilli
      } else {
        0f
      })
    }
    val finalCep: Long = ((base + overallPopulationBonus + competitionBonus) * timeMultiplier * Config.app.game.cepRate).toLong
    //reward participant(s)
//    targets
//      .filter { player =>
//        player.Faction == faction && !player.Name.equals(hacker.Name)
//      }
//      .foreach { player =>
//        events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(0, finalCep))
//      }
//    events ! AvatarServiceMessage(hacker.Name, AvatarAction.AwardCep(hacker.CharId, finalCep))
//    flagCarrier match {
//      case Some(player) => events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(player.CharId, finalCep / 2))
//      case None => ;
//    }
  }
}
