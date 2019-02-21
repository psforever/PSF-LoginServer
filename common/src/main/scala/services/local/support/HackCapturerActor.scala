package services.local.support

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.DefaultCancellable
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.CaptureTerminal
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

import scala.concurrent.duration.{FiniteDuration, _}

class HackCaptureActor extends Actor {
  private [this] val log = org.log4s.getLogger

  private var clearTrigger : Cancellable = DefaultCancellable.obj

  /** A `List` of currently hacked server objects */
  private var hackedObjects : List[HackCaptureActor.HackEntry] = Nil

  def receive : Receive = {
    case HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration, time) =>
      log.trace(s"${target.GUID} is hacked.")

      hackedObjects.filter(x => x.target == target).headOption match {
        case Some(x) =>
          log.trace(s"${target.GUID} was already hacked - removing it from the hacked objects list before re-adding it.")
          hackedObjects = hackedObjects.filterNot(x => x.target == target)
          log.warn(s"len: ${hackedObjects.length}")
        case _ => ;
      }

      hackedObjects = hackedObjects :+ HackCaptureActor.HackEntry(target, zone, unk1, unk2, duration, time)

      // Restart the timer, in case this is the first object in the hacked objects list or the object was removed and re-added
      RestartTimer()

      target.Owner.Actor ! Building.SendMapUpdate(all_clients = true)

    case HackCaptureActor.ProcessCompleteHacks() =>
      log.trace("Processing complete hacks")
      clearTrigger.cancel
      val now : Long = System.nanoTime
      val stillHacked = hackedObjects.filter(x => now - x.hack_timestamp <= x.duration.toNanos)
      val unhackObjects = hackedObjects.filter(x => now - x.hack_timestamp >= x.duration.toNanos)
      hackedObjects = stillHacked
      unhackObjects.foreach(entry => {
        log.trace(s"Capture terminal hack timeout reached for terminal ${entry.target.GUID}")

        val hackedByFaction = entry.target.HackedBy.get._1.Faction
        entry.target.Actor ! CommonMessages.ClearHack()

        context.parent ! HackCaptureActor.HackTimeoutReached(entry.target.GUID, entry.zone.Id, entry.unk1, entry.unk2, hackedByFaction) //call up to the main event system
      })

      // If there's hacked objects left in the list restart the timer with the shortest hack time left
      RestartTimer()

    case HackCaptureActor.ClearHack(target, zone) =>
      hackedObjects = hackedObjects.filterNot(x => x.target == target)
      target.Owner.Actor ! Building.SendMapUpdate(all_clients = true)

      // Restart the timer in case the object we just removed was the next one scheduled
      RestartTimer()

    case HackCaptureActor.GetHackTimeRemainingNanos(capture_console_guid) =>
      hackedObjects.filter(x => x.target.GUID == capture_console_guid).headOption match {
        case Some(obj: HackCaptureActor.HackEntry) =>
          val time_left: Long = obj.duration.toNanos - (System.nanoTime - obj.hack_timestamp)
          sender ! time_left
        case _ =>
          log.warn(s"Couldn't find capture terminal guid ${capture_console_guid} in hackedObjects list")
          sender ! 0L
      }
    case _ => ;
  }

  private def RestartTimer(): Unit = {
    if(hackedObjects.length != 0) {
      val now = System.nanoTime()
      def minTimeLeft(entry1: HackCaptureActor.HackEntry, entry2: HackCaptureActor.HackEntry): HackCaptureActor.HackEntry = {
        val entry1TimeLeft = entry1.duration.toNanos - (now - entry1.hack_timestamp)
        val entry2TimeLeft = entry2.duration.toNanos - (now - entry2.hack_timestamp)
        if(entry1TimeLeft < entry2TimeLeft) entry1 else entry2
      }

      val hackEntry = hackedObjects.reduceLeft(minTimeLeft)
      val short_timeout : FiniteDuration = math.max(1, hackEntry.duration.toNanos - (System.nanoTime - hackEntry.hack_timestamp)) nanoseconds

      log.trace(s"Still items left in hacked objects list. Checking again in ${short_timeout.toSeconds} seconds")
      import scala.concurrent.ExecutionContext.Implicits.global
      clearTrigger = context.system.scheduler.scheduleOnce(short_timeout, self, HackCaptureActor.ProcessCompleteHacks())
    }
  }
}

object HackCaptureActor {
  final case class ObjectIsHacked(target : CaptureTerminal, zone : Zone, unk1 : Long, unk2 : Long, duration: FiniteDuration, time : Long = System.nanoTime())

  final case class HackTimeoutReached(capture_terminal_guid : PlanetSideGUID, zone_id : String, unk1 : Long, unk2 : Long, hackedByFaction : PlanetSideEmpire.Value)

  final case class ClearHack(target : CaptureTerminal, zone : Zone)

  final case class GetHackTimeRemainingNanos(capture_console_guid: PlanetSideGUID)


  private final case class ProcessCompleteHacks()

  private final case class HackEntry(target : PlanetSideServerObject with Hackable, zone : Zone, unk1 : Long, unk2 : Long, duration: FiniteDuration, hack_timestamp : Long)
}