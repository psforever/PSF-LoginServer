// Copyright (c) 2021 PSForever
package net.psforever.services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.Default
import net.psforever.objects.serverobject.structures.participation.MajorFacilityHackParticipation
import net.psforever.packet.game.{ChatMsg, GenericAction, HackState7, PlanetsideAttributeEnum}
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.services.local.support.HackCaptureActor.GetHackingFaction
import net.psforever.services.local.{FlagMessage, LocalAction, LocalServiceMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID}

import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.Seq
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Random

/**
 * Responsible for handling the aspects related to hacking control consoles and capturing bases.
 */
class HackCaptureActor extends Actor {
  private[this] val log = org.log4s.getLogger
  /** main timer for completing or clearing hacked states */
  private var clearTrigger: Cancellable = Default.Cancellable
  /** list of currently hacked server objects */
  private var hackedObjects: List[HackCaptureActor.HackEntry] = Nil
  private val scheduler = Executors.newScheduledThreadPool(2)

  def receive: Receive = {
    case HackCaptureActor.StartCaptureTerminalHack(target, _, _, _, _)
      if target.HackedBy.isEmpty =>
      log.error(s"StartCaptureTerminalHack: initial $target hack information is missing; can not proceed")

    case HackCaptureActor.StartCaptureTerminalHack(target, zone, unk1, unk2, startTime) =>
      log.trace(s"StartCaptureTerminalHack: ${target.GUID} is hacked")
      val hackingFaction = HackCaptureActor.GetHackingFaction(target).get
      val duration = target.Owner match {
          case b: Building if b.IsCtfBase && b.Neighbours(hackingFaction).nonEmpty =>
            15.minutes
          case _ =>
            target.Definition.FacilityHackTime
        }
      target.HackedBy.map {
        hackInfo => target.HackedBy = hackInfo.Duration(duration.toMillis)
      }
      hackedObjects = hackedObjects.filterNot(_.target == target) :+ HackCaptureActor.HackEntry(target, zone, unk1, unk2, duration, startTime)
      RestartTimer()
      NotifyHackStateChange(target, isResecured = false)
      TrySpawnCaptureFlag(target)

    case HackCaptureActor.ProcessCompleteHacks() =>
      log.trace("Processing complete hacks")
      clearTrigger.cancel()
      val now: Long     = System.currentTimeMillis()
      val (stillHacked, finishedHacks) = hackedObjects.partition(x => now - x.hack_timestamp < x.duration.toMillis)
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
          case Some(llu) if llu.Destroyed =>
            // LLU was destroyed while in the field. Send resecured notifications
            terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(llu, CaptureFlagLostReasonEnum.FlagLost)
            NotifyHackStateChange(terminal, isResecured = true)

          case Some(llu) =>
            // LLU was not delivered in time. Send resecured notifications
            terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(llu, CaptureFlagLostReasonEnum.TimedOut)
            NotifyHackStateChange(terminal, isResecured = true)

          case _ =>
            // Timed hack finished (or neutral LLU base with no neighbour as timed hack), capture the base
            val hackTime = terminal.Definition.FacilityHackTime.toMillis
            HackCompleted(terminal, hackedByFaction)
            building.Participation.RewardFacilityCapture(
              HackCaptureActor.GetDefendingFaction(terminal, building, hackedByFaction),
              hackedByFaction,
              hacker,
              hackTime,
              hackTime,
              isResecured = false
            )
        }
      }
      // If there's hacked objects left in the list restart the timer with the shortest hack time left
      RestartTimer()

    case HackCaptureActor.ResecureCaptureTerminal(target, _, hacker) =>
      val (results, remainder) = hackedObjects.partition(x => x.target eq target)
      val faction = GetHackingFaction(target).getOrElse(target.Faction)
      target.HackedBy = None
      hackedObjects = remainder
      val now: Long = System.currentTimeMillis()
      val facilityHackTime: Long = target.Definition.FacilityHackTime.toMillis
      val building = target.Owner.asInstanceOf[Building]
      val hackTime = results.headOption.map { now - _.hack_timestamp }.getOrElse(facilityHackTime)
      // If LLU exists it was not delivered. Send resecured notifications
      building.GetFlag.collect {
        case flag: CaptureFlag => target.Zone.LocalEvents ! CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Resecured)
      }
      NotifyHackStateChange(target, isResecured = true)
      building.Participation.RewardFacilityCapture(
        target.Faction,
        HackCaptureActor.GetAttackingFaction(building, faction),
        hacker,
        facilityHackTime,
        hackTime,
        isResecured = true
      )
      // Restart the timer in case the object we just removed was the next one scheduled
      RestartTimer()

    case HackCaptureActor.FlagCaptured(flag) =>
      log.debug(hackedObjects.toString())
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
          building.Participation.RewardFacilityCapture(
            HackCaptureActor.GetDefendingFaction(terminal, building, hackedByFaction),
            hackedByFaction,
            hacker,
            terminal.Definition.FacilityHackTime.toMillis,
            System.currentTimeMillis() - entry.hack_timestamp,
            isResecured = false
          )
          entry.target.Actor ! CommonMessages.ClearHack()
          flag.Zone.LocalEvents ! CaptureFlagManager.Captured(flag)
          // If there's hacked objects left in the list restart the timer with the shortest hack time left
          RestartTimer()

        case _ =>
          log.error(s"Attempted LLU capture for ${flag.Owner.asInstanceOf[Building].Name} but CC GUID ${flag.Owner.asInstanceOf[Building].CaptureTerminal.get.GUID} was not in list of hacked objects")
      }

    case HackCaptureActor.FlagLost(flag) =>
      val owner = flag.Owner.asInstanceOf[Building]
      val guid = owner.GUID
      val (found, remaining) = hackedObjects.partition(_.target.Owner.GUID == guid)
      hackedObjects = remaining
      found.collectFirst { _ =>
        val terminal = owner.CaptureTerminal.get
        log.info(s"FlagLost: ${flag.Carrier.map(_.Name).getOrElse("")} the flag carrier screwed up the capture for ${flag.Target.Name} and the LLU has been lost")
        terminal.Actor ! CommonMessages.ClearHack()
        NotifyHackStateChange(terminal, isResecured = true)
        RestartTimer() // If there's hacked objects left in the list restart the timer with the shortest hack time left
      }
      if (found.isEmpty) {
        log.warn(s"FlagLost: flag data does not match to an entry in the hacked objects list")
      }
      owner.Zone.LocalEvents ! FlagMessage(CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.FlagLost))

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
      case None =>
        //not an error; this is just not a ctf facility
        false
      case Some((owner, None, Some(neighbours))) if neighbours.nonEmpty =>
        log.info(s"An LLU is being spawned for facility ${owner.Name} by $hackingFaction")
        spawnCaptureFlag(neighbours, terminal, hackingFaction)
        true
      case Some((owner, Some(flag), Some(neighbours))) if neighbours.nonEmpty && hackingFaction != flag.Faction =>
        log.info(s"$hackingFaction is overriding the ongoing LLU hack of facility ${owner.Name} by ${flag.Faction}")
        terminal.Zone.LocalEvents ! FlagMessage(CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Ended))
        NotifyHackStateChange(terminal, isResecured = false)
        RestartTimer()
        spawnCaptureFlag(neighbours, terminal, hackingFaction)
        true
      case Some((_, Some(flag), _)) if hackingFaction == flag.Faction =>
        log.error(s"TrySpawnCaptureFlag: flag hacked facility can not be hacked twice by $hackingFaction")
        false
      case Some((owner, _, _)) if hackingFaction == terminal.Faction =>
        log.error(s"TrySpawnCaptureFlag: owning faction and hacking faction match for facility ${owner.Name}; should we be resecuring instead?")
        false
      case Some((owner, Some(flag), _)) =>
        log.warn(s"TrySpawnCaptureFlag: couldn't find any neighbouring $hackingFaction facilities of ${owner.Name} for LLU hack")
        owner.GetFlagSocket.foreach { _.clearOldFlagData() }
        terminal.Zone.LocalEvents ! FlagMessage(CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Ended))
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
    terminal.Zone.LocalEvents ! FlagMessage(CaptureFlagManager.SpawnCaptureFlag(terminal, targetBase, hackingFaction))
  }

  private def NotifyHackStateChange(
                                     terminal: CaptureTerminal,
                                     isResecured: Boolean
                                   ): Unit = {
    val attributeValue = HackCaptureActor.GetHackUpdateAttributeValue(terminal, isResecured)
    // Notify all clients that CC has had its hack state changed
    terminal.Zone.LocalEvents ! LocalServiceMessage(
      terminal.Zone.id,
      PlanetSideGUID(-1),
      LocalAction.PlanetsideAttribute(
        terminal.GUID,
        PlanetsideAttributeEnum.ControlConsoleHackUpdate,
        attributeValue
      )
    )
    val owner = terminal.Owner.asInstanceOf[Building]
    // Notify parent building that state has changed
    owner.Actor ! BuildingActor.AmenityStateChange(terminal, Some(isResecured))
    // If major facility, check NTU
    owner.CaptureTerminal
      .map(_.HackedBy)
      .collect {
        case Some(info: Hackable.HackInfo)
          if owner.BuildingType == StructureType.Facility && owner.NtuLevel == 0 =>
          MajorFacilityHackParticipation.warningMessageForHackOccupiers(
            owner,
            info,
            ChatMsg(ChatMessageType.UNK_227, "@FacilityRequiresResourcesForHackWarning")
          )
      }
    // Push map update to clients
    if (owner.BuildingType == StructureType.Tower)
      owner.Actor ! BuildingActor.MapUpdate()
    else
      owner.Zone.actor ! ZoneActor.ZoneMapUpdate()
  }

  private def HackCompleted(terminal: CaptureTerminal with Hackable, hackedByFaction: PlanetSideEmpire.Value): Unit = {
    val building = terminal.Owner.asInstanceOf[Building]
    if (building.NtuLevel > 0) {
      building.virusId = 8
      building.virusInstalledBy = None
      log.info(s"Setting base ${building.GUID} / MapId: ${building.MapId} as owned by $hackedByFaction")
      //dispatch to players aligned with the capturing faction within the SOI
      val events = building.Zone.LocalEvents
      val msg = LocalAction.GenericActionMessage(GenericAction.FacilityCaptureFanfare)
      building
        .PlayersInSOI
        .collect { case p if p.Faction == hackedByFaction =>
          events ! LocalServiceMessage(p.Name, msg)
        }
      val buildings = building.Zone.Buildings.values
      val hackedBaseId = building.GUID
      val facilities = if (building.Zone.id.startsWith("c")) {
        buildings.filter(b =>
          b.Name.startsWith("N") || b.Name.startsWith("S")).toSeq
        }
        else {
          buildings.filter(_.BuildingType == StructureType.Facility).toSeq
        }
      val ownedFacilities = facilities.filter(b =>
        b.Faction == hackedByFaction && b.GUID != hackedBaseId
      )
      val towersToCapture = buildings.filter(b =>
        b.BuildingType == StructureType.Tower && b.Faction != hackedByFaction
      ).toSeq
      if (ownedFacilities.size == facilities.size - 1) {
        building.Zone.lockedBy = hackedByFaction
        building.Zone.benefitRecipient = hackedByFaction
        building.Zone.NotifyContinentalLockBenefits(building.Zone, building)
        if (towersToCapture.nonEmpty) {
          processBuildingsWithDelay(towersToCapture, hackedByFaction, 100)
        }
      }
      else if (building.Zone.lockedBy != PlanetSideEmpire.NEUTRAL) {
        building.Zone.lockedBy = PlanetSideEmpire.NEUTRAL
        building.Zone.NotifyContinentalLockBenefits(building.Zone, building)
      }
      building.Actor ! BuildingActor.SetFaction(hackedByFaction)
    } else {
      log.info("Base hack completed, but base was out of NTU.")
    }
    NotifyHackStateChange(terminal, isResecured = true)
    // todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work.
    val zone = building.Zone
    zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.SendHackMessageHackCleared(building.GUID, 3212836864L, HackState7.Unk8))
  }

  private def RestartTimer(): Unit = {
    if (hackedObjects.nonEmpty) {
      val hackEntry = hackedObjects.reduceLeft(HackCaptureActor.minTimeLeft(System.currentTimeMillis()))
      val short_timeout: FiniteDuration = math.max(1, hackEntry.duration.toMillis - (System.currentTimeMillis() - hackEntry.hack_timestamp)).milliseconds
      log.trace(s"RestartTimer: still items left in hacked objects list. Checking again in ${short_timeout.toSeconds} seconds")
      import scala.concurrent.ExecutionContext.Implicits.global
      clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackCaptureActor.ProcessCompleteHacks())
    }
  }

  def processBuildingsWithDelay(
                                 buildings: Seq[Building],
                                 faction: PlanetSideEmpire.Value,
                                 delayMillis: Long
                               ): Unit = {
    val buildingIterator = buildings.iterator
    scheduler.scheduleAtFixedRate(
      () => {
        if (buildingIterator.hasNext) {
          val building = buildingIterator.next()
          val terminal = building.CaptureTerminal.get
          val buildingActor = building.Actor
          buildingActor ! BuildingActor.SetFaction(faction)
          buildingActor ! BuildingActor.AmenityStateChange(terminal, Some(false))
          buildingActor ! BuildingActor.MapUpdate()
        }
      },
      0,
      delayMillis,
      TimeUnit.MILLISECONDS
    )
  }

}

object HackCaptureActor {
  final case class StartCaptureTerminalHack(
                                             target: CaptureTerminal,
                                             zone: Zone,
                                             unk1: Long,
                                             unk2: Long,
                                             startTime: Long = System.currentTimeMillis()
                                           )

  final case class ResecureCaptureTerminal(target: CaptureTerminal, zone: Zone, hacker: PlayerSource)
  final case class FlagCaptured(flag: CaptureFlag)
  final case class FlagLost(flag: CaptureFlag)

  private final case class ProcessCompleteHacks()

  sealed case class HackEntry(
                               target: CaptureTerminal with Hackable,
                               zone: Zone,
                               unk1: Long,
                               unk2: Long,
                               duration: FiniteDuration,
                               hack_timestamp: Long
                             )

  def GetDefendingFaction(
                           terminal: CaptureTerminal,
                           building: Building,
                           excludeThisFaction: PlanetSideEmpire.Value
                         ): PlanetSideEmpire.Value = {
    terminal.HackedBy
      .map { _.originalFaction }
      .orElse { Some(terminal.Faction) }
      .collect {
        case PlanetSideEmpire.NEUTRAL =>
          val factionEfforts = building.Participation
            .PlayerContributionRaw
            .values
            .foldLeft(Array.fill(4)(0L))({ case (a, b) =>
              val (player, duration, _) = b
              val index = player.Faction.id
              a.update(index, a(index) + duration.toLong)
              a
            })
          factionEfforts.update(excludeThisFaction.id, Long.MinValue)
          factionEfforts
            .indices
            .maxByOption(factionEfforts)
            .map { index => PlanetSideEmpire(index) }
            .getOrElse {
              val test = PlanetSideEmpire(0)
              if (excludeThisFaction == test) {
                PlanetSideEmpire(1)
              } else {
                test
              }
            }
        case faction =>
          faction
      }
      .get
  }

  def GetAttackingFaction(
                           building: Building,
                           excludeThisFaction: PlanetSideEmpire.Value
                         ): PlanetSideEmpire.Value = {
    // Use PlayerContributionRaw to calculate attacking faction
    val factionEfforts = building.Participation
      .PlayerContributionRaw
      .values
      .foldLeft(Array.fill(4)(0L)) { case (efforts, (player, duration, _)) =>
        val factionId = player.Faction.id
        efforts.update(factionId, efforts(factionId) + duration)
        efforts
      }

    // Exclude the specified faction
    factionEfforts.update(excludeThisFaction.id, Long.MinValue)

    // Find the faction with the highest contribution
    factionEfforts.indices
      .maxByOption(factionEfforts)
      .map(PlanetSideEmpire.apply)
      .getOrElse(PlanetSideEmpire.NEUTRAL)
  }

  def GetHackingFaction(terminal: CaptureTerminal): Option[PlanetSideEmpire.Value] = {
    terminal.HackedBy.map { a => a.player.Faction }
  }

  def GetHackUpdateAttributeValue(terminal: CaptureTerminal, isResecured: Boolean): Long = {
    terminal.HackedBy match {
      case _ if isResecured =>
        17039360L
      case Some(Hackable.HackInfo(p, _, start, length, _)) =>
        // See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
        val hackTimeRemainingMS = math.max(0, start + length - System.currentTimeMillis())
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
    val entry1TimeLeft = entry1.duration.toMillis - (now - entry1.hack_timestamp)
    val entry2TimeLeft = entry2.duration.toMillis - (now - entry2.hack_timestamp)
    if (entry1TimeLeft < entry2TimeLeft) {
      entry1
    } else {
      entry2
    }
  }
}
