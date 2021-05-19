package net.psforever.services.local.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.packet.game.{GenericActionEnum, PlanetsideAttributeEnum}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Random

/**
  * Responsible for handling the aspects related to hacking control consoles and capturing bases.
  */
class HackCaptureActor(val taskResolver: ActorRef) extends Actor {
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
        TrySpawnCaptureFlag(target)

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

        // If the base has a socket, but no flag spawned it means the hacked base is neutral with no friendly neighbouring bases to deliver to, making it a timed hack.
        (entry.target.Owner.asInstanceOf[Building].GetFlagSocket, entry.target.Owner.asInstanceOf[Building].GetFlag) match {
          case (Some(socket), Some(_)) =>
            // LLU was not delivered in time. Send resecured notifications
            entry.target.Owner.asInstanceOf[Building].GetFlag match {
              case Some(flag: CaptureFlag) => entry.target.Zone.LocalEvents ! CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.TimedOut)
              case None => log.warn(s"Failed to find capture flag matching socket ${socket.GUID}")
            }

            NotifyHackStateChange(entry.target, isResecured = true)
          case _ =>
            // Timed hack finished (or neutral LLU base with no neighbour as timed hack), capture the base
            HackCompleted(entry.target, hackedByFaction)
        }
      })

      // If there's hacked objects left in the list restart the timer with the shortest hack time left
      RestartTimer()

    case HackCaptureActor.ResecureCaptureTerminal(target, _) =>
      hackedObjects = hackedObjects.filterNot(x => x.target == target)

      // If LLU exists it was not delivered. Send resecured notifications
      target.Owner.asInstanceOf[Building].GetFlag match {
        case Some(flag: CaptureFlag) => target.Zone.LocalEvents ! CaptureFlagManager.Lost(flag, CaptureFlagLostReasonEnum.Resecured)
        case None => ;
      }

      NotifyHackStateChange(target, isResecured = true)

      // Restart the timer in case the object we just removed was the next one scheduled
      RestartTimer()
    case HackCaptureActor.FlagCaptured(flag) =>
      log.warn(hackedObjects.toString())
      hackedObjects.find(_.target.GUID == flag.Owner.asInstanceOf[Building].CaptureTerminal.get.GUID) match {
        case Some(entry) =>
          val hackedByFaction = entry.target.HackedBy.get.hackerFaction
          hackedObjects = hackedObjects.filterNot(x => x == entry)
          HackCompleted(entry.target, hackedByFaction)

          entry.target.Actor ! CommonMessages.ClearHack()

          flag.Zone.LocalEvents ! CaptureFlagManager.Captured(flag)

          // If there's hacked objects left in the list restart the timer with the shortest hack time left
          RestartTimer()
        case _ =>
          log.error(s"Attempted LLU capture for ${flag.Owner.asInstanceOf[Building].Name} but CC GUID ${flag.Owner.asInstanceOf[Building].CaptureTerminal.get.GUID} was not in list of hacked objects")
      }

    case _ => ;
  }

  private def TrySpawnCaptureFlag(terminal: CaptureTerminal): Boolean = {
    // Handle LLUs if the base contains a LLU socket
    // If there are no neighbouring bases belonging to the hacking faction this will be handled as a regular timed hack (e.g. neutral base in enemy territory)
    terminal.Owner match {
      case owner: Building if owner.IsCtfBase =>
        val socket = owner.GetFlagSocket.get
        val flag = socket.captureFlag
        val owningFaction = owner.Faction
        val hackingFaction = HackCaptureActor.GetHackingFaction(terminal).get
        owner.Neighbours(hackingFaction) match {
          case Some(neighbours) =>
            if (flag.isEmpty) {
              log.info(s"An LLU is being spawned for facility ${owner.Name} by $hackingFaction")
              spawnCaptureFlag(neighbours, terminal, hackingFaction)
              true
            } else if (hackingFaction != flag.get.Faction) {
              log.info(s"$hackingFaction is overriding the ongoing LLU hack of facility ${owner.Name} by ${flag.get.Faction}")
              terminal.Zone.LocalEvents ! CaptureFlagManager.Lost(flag.get, CaptureFlagLostReasonEnum.Ended)
              NotifyHackStateChange(terminal, isResecured = false)
              RestartTimer()
              spawnCaptureFlag(neighbours, terminal, hackingFaction)
              true
            } else if (hackingFaction == owningFaction) {
              log.error(s"Owning faction and hacking faction match for facility ${owner.Name}; should we be resecuring instead?")
              false
            } else {
              log.warn(s"LLU hack of facility ${owner.Name} by $hackingFaction in progress - no change")
              false
            }
          case None => ;
            log.info(s"Couldn't find any neighbouring $hackingFaction facilities of ${owner.Name} for LLU hack")
            false
        }

      case thing =>
        log.error(s"Capture terminal has unexpected owner - $thing - that is not a facility")
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

  private def NotifyHackStateChange(terminal: CaptureTerminal, isResecured: Boolean): Unit = {
    val attribute_value = HackCaptureActor.GetHackUpdateAttributeValue(terminal, isResecured)

    // Notify all clients that CC has been hacked
    terminal.Zone.LocalEvents ! LocalServiceMessage(
      terminal.Zone.id,
      LocalAction.SendPlanetsideAttributeMessage(
        PlanetSideGUID(-1),
        terminal.GUID,
        PlanetsideAttributeEnum.ControlConsoleHackUpdate,
        attribute_value
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

      // todo: This should probably only go to those within the captured SOI who belong to the capturing faction
      building.Zone.LocalEvents ! LocalServiceMessage(building.Zone.id, LocalAction.SendGenericActionMessage(PlanetSideGUID(-1), GenericActionEnum.BaseCaptureFanfare))
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
  final case class FlagCaptured(flag: CaptureFlag)

  private final case class ProcessCompleteHacks()

  private final case class HackEntry(
      target: CaptureTerminal with Hackable,
      zone: Zone,
      unk1: Long,
      unk2: Long,
      duration: FiniteDuration,
      hack_timestamp: Long
  )

  def GetHackingFaction(terminal: CaptureTerminal): Option[PlanetSideEmpire.Value] = {
    terminal.HackedBy match {
      case Some(Hackable.HackInfo(_, _, hackingFaction, _, _, _)) =>
        Some(hackingFaction)
      case _ => None
    }
  }

  def GetHackUpdateAttributeValue(terminal: CaptureTerminal, isResecured: Boolean): Long = {
    if (isResecured) {
      17039360L
    } else {
      terminal.HackedBy match {
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

