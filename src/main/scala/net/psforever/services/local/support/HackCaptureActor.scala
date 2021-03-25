package net.psforever.services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.packet.game.PlanetsideAttributeEnum
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{FiniteDuration, _}

class HackCaptureActor extends Actor {
  private[this] val log = org.log4s.getLogger

  private var clearTrigger: Cancellable = Default.Cancellable

  /** A `List` of currently hacked server objects */
  private var hackedObjects: List[HackCaptureActor.HackEntry] = Nil

  def receive: Receive = {
    case HackCaptureActor.StartCaptureTerminalHack(target, zone, unk1, unk2, startTime) =>
      log.trace(s"StartCaptureTerminalHack: ${target.GUID} is hacked.")

      val duration = target.Definition match {
        case GlobalDefinitions.capture_terminal =>
          // Base CC
          15 minutes
        case GlobalDefinitions.secondary_capture =>
          // Tower CC
          1 nanosecond
        case GlobalDefinitions.vanu_control_console =>
          // Cavern CC
          10 minutes
      }

      target.HackedBy match {
        case Some(hackInfo) =>
          target.HackedBy = hackInfo.Duration(duration.toNanos)
        case None =>
          log.error(s"Initial $target hack information is missing")
      }

      hackedObjects.find(_.target == target) match {
        case Some(_) =>
          log.trace(
            s"StartCaptureTerminalHack: ${target.GUID} was already hacked - removing it from the hacked objects list before re-adding it."
          )
          hackedObjects = hackedObjects.filterNot(x => x.target == target)
        case _ => ;
      }

      hackedObjects = hackedObjects :+ HackCaptureActor.HackEntry(target, zone, unk1, unk2, duration, startTime)

      // Restart the timer, in case this is the first object in the hacked objects list or the object was removed and re-added
      RestartTimer()

      NotifyHackStateChange(target, isResecured = false)

    case HackCaptureActor.ProcessCompleteHacks() =>
      log.trace("Processing complete hacks")
      clearTrigger.cancel()
      val now: Long     = System.nanoTime
      val stillHacked   = hackedObjects.filter(x => now - x.hack_timestamp <= x.duration.toNanos)
      val finishedHacks = hackedObjects.filter(x => now - x.hack_timestamp >= x.duration.toNanos)
      hackedObjects = stillHacked
      finishedHacks.foreach(entry => {
        log.trace(s"ProcessCompleteHacks: capture terminal hack timeout reached for terminal ${entry.target.GUID}")

        val hackedByFaction = entry.target.HackedBy.get.hackerFaction
        entry.target.Actor ! CommonMessages.ClearHack()

        HackCompleted(entry.target, hackedByFaction)
      })

      // If there's hacked objects left in the list restart the timer with the shortest hack time left
      RestartTimer()

    case HackCaptureActor.ResecureCaptureTerminal(target, _) =>
      hackedObjects = hackedObjects.filterNot(x => x.target == target)

      NotifyHackStateChange(target, isResecured = true)

      // Restart the timer in case the object we just removed was the next one scheduled
      RestartTimer()
    case _ => ;
  }

  private def NotifyHackStateChange(target: CaptureTerminal, isResecured: Boolean): Unit = {
    val attribute_value = HackCaptureActor.GetHackUpdateAttributeValue(target, isResecured)

    // Notify all clients that CC has been hacked
    target.Zone.LocalEvents ! LocalServiceMessage(
      target.Zone.id,
      LocalAction.SendPlanetsideAttributeMessage(
        PlanetSideGUID(-1),
        target.GUID,
        PlanetsideAttributeEnum.ControlConsoleHackUpdate,
        attribute_value
      )
    )

    // Notify parent building that state has changed
    target.Owner.Actor ! BuildingActor.AmenityStateChange(target, Some(isResecured))

    // Push map update to clients
    target.Owner.asInstanceOf[Building].Zone.actor ! ZoneActor.ZoneMapUpdate()
  }

  private def HackCompleted(terminal: CaptureTerminal with Hackable, hackedByFaction: PlanetSideEmpire.Value): Unit = {
    val building = terminal.Owner.asInstanceOf[Building]
    if (building.NtuLevel > 0) {
      log.info(s"Setting base ${building.GUID} / MapId: ${building.MapId} as owned by $hackedByFaction")
      building.Actor! BuildingActor.SetFaction(hackedByFaction)
    } else {
      log.info("Base hack completed, but base was out of NTU.")
    }

    NotifyHackStateChange(terminal, isResecured = true)

    // todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work.
    context.parent ! HackClearActor.SendHackMessageHackCleared(building.GUID, terminal.Zone.id, 3212836864L, 8L) //call up to the `LocalService`
  }

  private def RestartTimer(): Unit = {
    if (hackedObjects.nonEmpty) {
      val now = System.nanoTime()
      def minTimeLeft(
          entry1: HackCaptureActor.HackEntry,
          entry2: HackCaptureActor.HackEntry
      ): HackCaptureActor.HackEntry = {
        val entry1TimeLeft = entry1.duration.toNanos - (now - entry1.hack_timestamp)
        val entry2TimeLeft = entry2.duration.toNanos - (now - entry2.hack_timestamp)
        if (entry1TimeLeft < entry2TimeLeft) entry1 else entry2
      }

      val hackEntry = hackedObjects.reduceLeft(minTimeLeft)
      val short_timeout: FiniteDuration =
        math.max(1, hackEntry.duration.toNanos - (System.nanoTime - hackEntry.hack_timestamp)) nanoseconds

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

  final case class ResecureCaptureTerminal(target: CaptureTerminal, zone: Zone)

  private final case class ProcessCompleteHacks()

  private final case class HackEntry(
      target: CaptureTerminal with Hackable,
      zone: Zone,
      unk1: Long,
      unk2: Long,
      duration: FiniteDuration,
      hack_timestamp: Long
  )

  def GetHackUpdateAttributeValue(target: CaptureTerminal, isResecured: Boolean): Long = {
    if (isResecured) {
      17039360L
    } else {
      target.HackedBy match {
        case Some(Hackable.HackInfo(_, _, hackingFaction, _, start, length)) =>
          // See PlanetSideAttributeMessage #20 documentation for an explanation of how the timer is calculated
          val hack_time_remaining_ms =
            TimeUnit.MILLISECONDS.convert(math.max(0, start + length - System.nanoTime), TimeUnit.NANOSECONDS)

          val start_num = hackingFaction match {
            case PlanetSideEmpire.TR => 0x10000
            case PlanetSideEmpire.NC => 0x20000
            case PlanetSideEmpire.VS => 0x30000
          }

          start_num + (hack_time_remaining_ms / 100) // Add time remaining as deciseconds

        case _ =>
          0
      }
    }
  }
}

