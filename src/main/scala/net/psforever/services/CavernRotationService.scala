// Copyright (c) 2022 PSForever
package net.psforever.services

import akka.actor.{ActorRef, Cancellable}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.actors.session.SessionActor
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.actors.zone.building.WarpGateLogic
import net.psforever.objects.Default
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage, GalaxyServiceResponse}
import net.psforever.types.ChatMessageType
import net.psforever.util.Config
import net.psforever.zones.Zones

import scala.concurrent.duration._

object CavernRotationService {
  val CavernRotationServiceKey: ServiceKey[Command] =
    ServiceKey[CavernRotationService.Command](id = "cavernRotationService")

  def apply(): Behavior[Command] =
    Behaviors
      .supervise[Command] {
      Behaviors.withStash(100) { buffer =>
        Behaviors.setup { context =>
          context.system.receptionist ! Receptionist.Register(CavernRotationServiceKey, context.self)
          new CavernRotationService(context, buffer).start()
        }
      }
    }.onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  final case class ManageCaverns(zones: Iterable[Zone]) extends Command

  final case class SendCavernRotationUpdates(sendToSession: ActorRef) extends Command

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long)

  final case class UnlockedZoneUpdate(zone: Zone)

  sealed trait HurryRotation extends Command {
    def zoneid: String
  }

  case object HurryNextRotation extends HurryRotation { def zoneid = "" }

  final case class HurryRotationToZoneLock(zoneid: String) extends HurryRotation

  final case class HurryRotationToZoneUnlock(zoneid: String) extends HurryRotation

  final case class ReportRotationOrder(sendToSession: ActorRef) extends Command

  private case object SwitchZone extends Command

  private case class ClosingWarning(counter: Int) extends Command

  /**
    * A token designed to keep track of the managed cavern zone.
    * @param zone the zone
    */
  class ZoneMonitor(val zone: Zone) {
    /** is the zone currently accessible */
    var locked: Boolean = true
    /** when did the timer start (ms) */
    var start: Long = 0L
    /** for how long does the timer go on (ms) */
    var duration: Long = 0L
  }

  /**
    * The periodic warning for when a cavern closes,
    * usually announcing fifteen, ten, then five minutes before closure.
    * @see `ChatMsg`
    * @see `GalaxyService`
    * @param zone zone monitor
    * @param counter current time until closure
    * @param galaxyService callback to display the warning;
    *                      should be the reference to `GalaxyService`, hence the literal name
    * @return `true`, if the zone was actually locked and the message was shown;
    *        `false`, otherwise
    */
  private def closedCavernWarning(zone: ZoneMonitor, counter: Int, galaxyService: ActorRef): Boolean = {
    if (!zone.locked) {
      galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
        ChatMsg(ChatMessageType.UNK_229, s"@cavern_closing_warning^@${zone.zone.id}~^@$counter~")
      ))
      true
    } else {
      false
    }
  }

  /**
    * Configure the cavern zones lattice links
    * for cavern access.
    * @param zones the cavern zones being configured
    */
  private def activateLatticeLinksAndWarpGateAccessibility(zones: Seq[Zone]): Unit = {
    val sortedZones = zones.sortBy(_.Number)
    establishLatticeLinksForUnlockedCaverns(sortedZones)
    sortedZones.foreach { zone =>
      openZoneWarpGateAccessibility(zone)
    }
  }

  /**
    * Apply the lattice links that connect geowarp gates and cavern warp gates to the lattices for each zone.
    * Separate the connection entry strings,
    * locate each individual zone and warp gate in that zone,
    * then add one to the other's lattice connectivity on the fly.
    * @param zones the cavern zones
    */
  private def establishLatticeLinksForUnlockedCaverns(zones: Seq[Zone]): Unit = {
    val key = s"caverns-${zones.map(_.id).mkString("-")}"
    Zones.cavernLattice.get(key) match {
      case Some(links) =>
        links.foreach { link =>
          val entryA = link.head
          val entryB = link.last
          val splitA = entryA.split("/")
          val splitB = entryB.split("/")
          ((zones.find { _.id.equals(splitA.head) }, Zones.zones.find { _.id.equals(splitB.head) }) match {
            case (Some(zone1), Some(zone2)) => (zone1.Building(splitA.last), zone2.Building(splitB.last))
            case _                          => (None, None)
          }) match {
            case (Some(gate1), Some(gate2)) =>
              gate1.Zone.AddIntercontinentalLatticeLink(gate1, gate2)
              gate2.Zone.AddIntercontinentalLatticeLink(gate2, gate1)
            case _ => ;
          }
        }
      case _ =>
        org.log4s.getLogger("CavernRotationService").error(s"can not find mapping to open $key")
    }
  }

  /**
    * Collect all of the warp gates in a (cavern) zone and the adjacent building along the lattice
    * and update the connectivity of the gate pairs
    * so that the gate pair is active and broadcasts correctly.
    * @param zone the zone
    * @return all of the affected warp gates
    */
  private def openZoneWarpGateAccessibility(zone: Zone): Iterable[WarpGate] = {
    findZoneWarpGatesForChangingAccessibility(zone).map { case (wg, otherWg, building) =>
      wg.Active = true
      otherWg.Active = true
      wg.Actor ! BuildingActor.AlertToFactionChange(building)
      otherWg.Zone.actor ! ZoneActor.ZoneMapUpdate()
      wg
    }
  }

  /**
    * Configure the cavern zones lattice links
    * for cavern closures.
    * @param zones the cavern zones being configured
    */
  private def disableLatticeLinksAndWarpGateAccessibility(zones: Seq[Zone]): Unit = {
    val sortedZones = zones.sortBy(_.Number)
    sortedZones.foreach { zone =>
      closeZoneWarpGateAccessibility(zone)
    }
    revokeLatticeLinksForUnlockedCaverns(sortedZones)
  }

  /**
    * Disconnect the lattice links that connect geowarp gates and cavern warp gates to the lattices for each zone.
    * Separate the connection entry strings,
    * locate each individual zone and warp gate in that zone,
    * then remove one from the other's lattice connectivity on the fly.
    * @param zones the cavern zones
    */
  private def revokeLatticeLinksForUnlockedCaverns(zones: Seq[Zone]): Unit = {
    val key = s"caverns-${zones.map(_.id).mkString("-")}"
    Zones.cavernLattice.get(key) match {
      case Some(links) =>
        links.foreach { link =>
          val entryA = link.head
          val entryB = link.last
          val splitA = entryA.split("/")
          val splitB = entryB.split("/")
          ((zones.find { _.id.equals(splitA.head) }, Zones.zones.find { _.id.equals(splitB.head) }) match {
            case (Some(zone1), Some(zone2)) => (zone1.Building(splitA.last), zone2.Building(splitB.last))
            case _                          => (None, None)
          }) match {
            case (Some(gate1), Some(gate2)) =>
              gate1.Zone.RemoveIntercontinentalLatticeLink(gate1, gate2)
              gate2.Zone.RemoveIntercontinentalLatticeLink(gate2, gate1)
            case _ => ;
          }
        }
      case _ =>
        org.log4s.getLogger("CavernRotationService").error(s"can not find mapping to close $key")
    }
  }

  /**
    * Collect all of the warp gates in a (cavern) zone and the adjacent building along the lattice
    * and update the connectivity of the gate pairs
    * so that the gate pair is inactive and stops broadcasting.
    * @param zone the zone
    * @return all of the affected warp gates
    */
  private def closeZoneWarpGateAccessibility(zone: Zone): Iterable[WarpGate] = {
    findZoneWarpGatesForChangingAccessibility(zone).map { case (wg, otherWg, building) =>
      wg.Active = false
      otherWg.Active = false
      wg.Actor ! BuildingActor.AlertToFactionChange(building)
      otherWg.Zone.actor ! ZoneActor.ZoneMapUpdate()
      //must trigger the connection test from the other side to equalize
      WarpGateLogic.findNeighborhoodNormalBuilding(otherWg.Neighbours.getOrElse(Nil)) match {
        case Some(b) => otherWg.Actor ! BuildingActor.AlertToFactionChange(b)
        case None    => ;
      }
      wg
    }
  }

  /**
    * Within a given zone, find:
    * (1) all warp gates;
    * (2) the warp gates that are adjacent along the intercontinental lattice (in the other zone); and,
    * (3) the facility building that is adjacent to the warp gate (in this zone).
    * Will be using the recovered grouping for manipulation of the intercontinental lattice extending from the zone.
    * @param zone the zone
    * @return the triples
    */
  private def findZoneWarpGatesForChangingAccessibility(zone: Zone): Iterable[(WarpGate, WarpGate, Building)] = {
    zone.Buildings.values
      .collect {
        case wg: WarpGate =>
          val neighborhood = wg.AllNeighbours.getOrElse(Nil)
          (
            WarpGateLogic.findNeighborhoodWarpGate(neighborhood),
            WarpGateLogic.findNeighborhoodNormalBuilding(neighborhood)
          ) match {
            case (Some(otherWg: WarpGate), Some(building)) =>
              Some(wg, otherWg, building)
            case _ =>
              None
          }
      }.flatten
  }

  /**
    * Take two zone monitors and swap the order of the zones.
    * Keep the timers from each other the same.
    * @param list the ordered zone monitors
    * @param to index of one zone monitor
    * @param from index of another zone monitor
    */
  private def swapMonitors(list: List[ZoneMonitor], to: Int, from: Int): Unit = {
    val toMonitor = list(to)
    val fromMonitor = list(from)
    list.updated(to, new ZoneMonitor(fromMonitor.zone) {
      locked = toMonitor.locked
      start = toMonitor.start
      duration = toMonitor.duration
    })
    list.updated(from, new ZoneMonitor(toMonitor.zone) {
      locked = fromMonitor.locked
      start = fromMonitor.start
      duration = fromMonitor.duration
    })
  }
}

/**
  * A service that assists routine access to a series of game zones
  * through the manipulation of connections between transmit point structures.<br>
  * <br>
  * The caverns were a group of game zones that were intended to be situated underground.
  * Access to the caverns was only sometimes possible
  * through the use of special above-ground warp gates called geowarps (geowarp gates)
  * and those geowarps were not always functional.
  * Usually, two caverns were available at a time and connections to these caverns were fixed
  * to specific active geowarp gates.
  * The changing availability of the caverns through the change of geowarp gate activity
  * was colloquially referred to as a "rotation" since it followed a predictable cycle.
  * The cycle was not just one of time but one of route
  * as one specific geowarp gates would open to the same destination cavern.<br>
  * <br>
  * The client controls warp gate destinations.
  * The server can only confirm those destinations.
  * The connectivity of a geowarp gate to a cavern warp gate had to have been determined
  * by opening the cavern with an appropriate packet
  * and checking the map description of the cavern gates.
  * The description text explains which of the geowarp gates in whichever zone has been connected; and,
  * where usually static and inanimate, that geowarp gate will bubble online and begin to rotate
  * and have a complementary destination map description.
  * Opening different combinations of caverns changes the destination these warp gate pairs will connect
  * and not always being connected at all.
  * The warp gate pairs for the cavern connections must be re-evaluated for each combination and with each rotation
  * and all relevant pairings must be defined in advance.
  * @see `ActorContext`
  * @see `Building`
  * @see `ChatMsg`
  * @see `Config.app.game.cavernRotation`
  * @see `GalaxyService`
  * @see `GalaxyAction.LockedZoneUpdate`
  * @see `GalaxyResponse.UnlockedZoneUpdate`
  * @see `InterstellarClusterService`
  * @see `org.log4s.getLogger`
  * @see `resources/zonemaps/lattice.json`
  * @see `SessionActor`
  * @see `SessionActor.SendResponse`
  * @see `StashBuffer`
  * @see `WarpGate`
  * @see `Zone`
  * @see `ZoneForcedCavernConnectionsMessage`
  * @see `ZoneInfoMessage`
  */
//TODO currently, can only support any 1 cavern unlock order and the predetermined 2 cavern unlock order
class CavernRotationService(
                             context: ActorContext[CavernRotationService.Command],
                             buffer: StashBuffer[CavernRotationService.Command]
                           ) {
  import CavernRotationService._

  ServiceManager.serviceManager ! ServiceManager.LookupFromTyped(
    "galaxy",
    context.messageAdapter[ServiceManager.LookupResult](ServiceManagerLookupResult)
  )

  /** monitors for the cavern zones */
  var managedZones: List[ZoneMonitor] = Nil
  /** index of the next cavern that will lock */
  var nextToLock: Int = 0
  /** index of the next cavern that will unlock */
  var nextToUnlock: Int = 0
  /** timer for cavern rotation - the cavern closing warning */
  var lockTimer: Cancellable = Default.Cancellable
  /** timer for cavern rotation - the actual opening and closing functionality */
  var unlockTimer: Cancellable = Default.Cancellable
  var simultaneousUnlockedZones: Int = Config.app.game.cavernRotation.simultaneousUnlockedZones
  /** time between individual cavern rotation events (hours) */
  val timeBetweenRotationsHours: Float = Config.app.game.cavernRotation.hoursBetweenRotation
  /** number of zones unlocked at the same time */
  /** period of all caverns having rotated (hours) */
  var timeToCompleteAllRotationsHours: Float = 0f
  /** how long before any given cavern closure that the first closing message is shown (minutes) */
  val firstClosingWarningAtMinutes: Int = 15

  def start(): Behavior[CavernRotationService.Command] = {
    Behaviors.receiveMessage {
      case ServiceManagerLookupResult(ServiceManager.LookupResult(request, endpoint)) =>
        request match {
          case "galaxy" =>
            buffer.unstashAll(active(endpoint))
          case _ =>
            Behaviors.same
        }

      case other =>
        buffer.stash(other)
        Behaviors.same
    }
  }

  def active(galaxyService: ActorRef): Behavior[CavernRotationService.Command] = {
    Behaviors.receiveMessage {
      case ManageCaverns(zones) =>
        manageCaverns(zones.toSeq)
        Behaviors.same

      case ClosingWarning(counter)
        if counter == 15 || counter == 10 =>
        if (CavernRotationService.closedCavernWarning(managedZones(nextToLock), counter, galaxyService)) {
          val next = counter - 5
          lockTimerToDisplayWarning(next.minutes, next)
        }
        Behaviors.same

      case ClosingWarning(counter) =>
        CavernRotationService.closedCavernWarning(managedZones(nextToLock), counter, galaxyService)
        Behaviors.same

      case ReportRotationOrder(sendToSession) =>
        reportRotationOrder(sendToSession)
        Behaviors.same

      case SwitchZone =>
        zoneRotationFunc(galaxyService)
        Behaviors.same

      case HurryNextRotation =>
        hurryNextRotation(galaxyService)
        Behaviors.same

      case HurryRotationToZoneLock(zoneid) =>
        hurryRotationToZoneLock(zoneid, galaxyService)
        Behaviors.same

      case HurryRotationToZoneUnlock(zoneid) =>
        hurryRotationToZoneUnlock(zoneid, galaxyService)
        Behaviors.same

      case SendCavernRotationUpdates(sendToSession) =>
        sendCavernRotationUpdates(sendToSession)
        Behaviors.same

      case _ =>
        Behaviors.same
    }
  }

  /**
    * na
    * @param zones the zones for submission
    * @return `true`, if the setup has been completed;
    *        `false`, otherwise
    */
  def manageCaverns(zones: Seq[Zone]): Boolean = {
    if (managedZones.isEmpty) {
      val onlyCaverns = zones.filter{ z => z.map.cavern }
      val collectedZones = Config.app.game.cavernRotation.enhancedRotationOrder match {
        case Nil  => onlyCaverns
        case list => list.flatMap { index => onlyCaverns.find(_.Number == index ) }
      }
      if (collectedZones.nonEmpty) {
        simultaneousUnlockedZones = math.min(simultaneousUnlockedZones, collectedZones.size)
        managedZones = collectedZones.map(zone => new ZoneMonitor(zone)).toList
        val rotationSize = managedZones.size
        timeToCompleteAllRotationsHours = rotationSize.toFloat * timeBetweenRotationsHours
        val curr = System.currentTimeMillis()
        val fullDurationAsHours = timeToCompleteAllRotationsHours.hours
        val fullDurationAsMillis = fullDurationAsHours.toMillis
        val startingInThePast = curr - fullDurationAsMillis
        val (unlockedZones, lockedZones) = managedZones.splitAt(simultaneousUnlockedZones)
        var i = 0
        //the timer data in all zone monitors
        (lockedZones ++ unlockedZones).foreach { zone =>
          i += 1
          zone.locked = true
          zone.start = startingInThePast + (i * timeBetweenRotationsHours).hours.toMillis
          zone.duration = fullDurationAsMillis
        }
        //unlocked zones
        unlockedZones.foreach { zone =>
          zone.locked = false
        }
        CavernRotationService.activateLatticeLinksAndWarpGateAccessibility(unlockedZones.map(_.zone))
        nextToLock = 0
        lockTimerToDisplayWarning(timeBetweenRotationsHours.hours - firstClosingWarningAtMinutes.minutes)
        //locked zones ...
        nextToUnlock = simultaneousUnlockedZones
        unlockTimerToSwitchZone(timeBetweenRotationsHours.hours)
        //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  /**
    * na
    * @param sendToSession callback reference
    */
  def reportRotationOrder(sendToSession: ActorRef): Unit = {
    val zoneStates = managedZones.collect {
      case zone =>
        if (zone.locked) {
          s"<${zone.zone.id}>"
        } else {
          s"${zone.zone.id}"
        }
    }.mkString(" ")
    sendToSession ! SessionActor.SendResponse(
      ChatMsg(ChatMessageType.UNK_229, s"[$zoneStates]")
    )
    Behaviors.same
  }

  /**
    * na
    * @see `GalaxyService`
    * @param zoneid zone to lock next
    * @param galaxyService callback to update the server and clients;
    *                      should be the reference to `GalaxyService`, hence the literal name
    * @return `true`, if the target zone is locked when complete;
    *        `false`, otherwise
    */
  def hurryRotationToZoneLock(zoneid: String, galaxyService: ActorRef): Boolean = {
    //TODO currently, can only switch for 1 active cavern
    if (simultaneousUnlockedZones == 1) {
      if ((nextToLock until nextToLock + simultaneousUnlockedZones)
        .map { i => managedZones(i % managedZones.size) }
        .indexWhere { _.zone.id.equals(zoneid) } match {
        case -1 =>
          false
        case  0 =>
          true
        case  index =>
          CavernRotationService.swapMonitors(managedZones, nextToLock, index)
          true
      }) {
        hurryNextRotation(galaxyService, forcedRotationOverride=true)
      }
      true
    } else {
      org.log4s.getLogger("CavernRotationService").warn(s"can not alter cavern order")
      false
    }
  }

  /**
    * na
    * @see `GalaxyService`
    * @param zoneid zone to unlock next
    * @param galaxyService callback to update the server and clients;
    *                      should be the reference to `GalaxyService`, hence the literal name
    * @return `true`, if the target zone is unlocked when complete;
    *        `false`, otherwise
    */
  def hurryRotationToZoneUnlock(zoneid: String, galaxyService: ActorRef): Boolean = {
    //TODO currently, can only switch for 1 active cavern
    if (simultaneousUnlockedZones == 1) {
      if (managedZones(nextToUnlock).zone.id.equals(zoneid)) {
        hurryNextRotation(galaxyService, forcedRotationOverride = true)
        true
      } else {
        managedZones.indexWhere { z => z.zone.id.equals(zoneid) } match {
          case -1 =>
            false //not found
          case index if nextToLock <= index && index < nextToUnlock + simultaneousUnlockedZones =>
            true //already unlocked
          case index =>
            CavernRotationService.swapMonitors(managedZones, nextToUnlock, index)
            hurryNextRotation(galaxyService, forcedRotationOverride = true)
            true
        }
      }
    } else {
      org.log4s.getLogger("CavernRotationService").error(s"can not alter cavern order")
      false
    }
  }

  /**
    *
    * @param sendToSession callback reference
    */
  def sendCavernRotationUpdates(sendToSession: ActorRef): Unit = {
    val curr = System.currentTimeMillis()
    val (lockedZones, unlockedZones) = managedZones.partition(_.locked)
    //borrow GalaxyService response structure, but send to the specific endpoint math.max(0, monitor.start + monitor.duration - curr)
    unlockedZones.foreach { monitor =>
      sendToSession ! GalaxyServiceResponse("", GalaxyAction.UnlockedZoneUpdate(monitor.zone))
    }
    val sortedLocked = lockedZones.sortBy(z => z.start)
    sortedLocked.take(2).foreach { monitor =>
      sendToSession ! GalaxyServiceResponse("", GalaxyAction.LockedZoneUpdate(monitor.zone, math.max(0, monitor.start + monitor.duration - curr)))
    }
    sortedLocked.takeRight(2).foreach { monitor =>
      sendToSession ! GalaxyServiceResponse("", GalaxyAction.LockedZoneUpdate(monitor.zone, 0L))
    }
  }

  def sendCavernRotationUpdatesToAll(galaxyService: ActorRef): Unit = {
    val curr = System.currentTimeMillis()
    val (lockedZones, unlockedZones) = managedZones.partition(_.locked)
    unlockedZones.foreach { z =>
      galaxyService ! GalaxyServiceMessage(GalaxyAction.UnlockedZoneUpdate(z.zone))
    }
    val sortedLocked = lockedZones.sortBy(z => z.start)
    sortedLocked.take(2).foreach { z =>
      galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(z.zone, z.start + z.duration - curr))
    }
    sortedLocked.takeRight(2).foreach { z =>
      galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(z.zone, 0L))
    }
  }

  /**
    * Progress to the next significant cavern rotation event.<br>
    * <br>
    * If the time until the next rotation is greater than the time where the cavern closing warning would be displayed,
    * progress to that final cavern closing warning.
    * Adjust the timing for that advancement.
    * If the final cavern closing warning was already displayed,
    * just perform the cavern rotation.
    * @see `GalaxyService`
    * @param galaxyService callback to update the server and clients;
    *                      should be the reference to `GalaxyService`, hence the literal name
    * @param forcedRotationOverride force a cavern rotation in a case where a closing warning would be displayed instead
    */
  def hurryNextRotation(
                         galaxyService: ActorRef,
                         forcedRotationOverride: Boolean = false
                       ): Unit = {
    val curr = System.currentTimeMillis() //ms
    val unlocking = managedZones(nextToUnlock)
    val timeToNextClosingEvent = unlocking.start + unlocking.duration - curr //ms
    val fiveMinutes = 5.minutes //minutes duration
    if (
      forcedRotationOverride || Config.app.game.cavernRotation.forceRotationImmediately ||
      timeToNextClosingEvent < fiveMinutes.toMillis
    ) {
      //zone transition immediately
      lockTimer.cancel()
      unlockTimer.cancel()
      retimeZonesUponForcedRotation(galaxyService)
      zoneRotationFunc(galaxyService)
      lockTimerToDisplayWarning(timeBetweenRotationsHours.hours - firstClosingWarningAtMinutes.minutes)
    } else {
      //instead of transitioning immediately, jump to the 5 minute rotation warning for the benefit of players
      lockTimer.cancel() //won't need to retime until zone change
      CavernRotationService.closedCavernWarning(managedZones(nextToLock), counter=5, galaxyService)
      unlockTimerToSwitchZone(fiveMinutes)
      retimeZonesUponForcedAdvancement(timeToNextClosingEvent.milliseconds - fiveMinutes, galaxyService)
    }
  }

  /**
    * Actually perform zone rotation as determined by the managed zone monitors and the timers.<br>
    * <br>
    * The process of zone rotation occurs by having a zone that is determined to be closing
    * and a zone that is determied to be opening
    * and a potential series of zones "in between" the two that are also open.
    * All of the currently opened zones are locked and the zone to be permanently closed is forgotten.
    * The zone that should be opening is added to the aforementioned sequence of zones
    * and then the zones in that sequence are opened.
    * The zones that would otherwise be unaffected by a single zone opening and a single cone closing must be affected
    * because the cavern gates will not connect to the same geowarp gates with the change in the sequence.
    * After the rotation, the indices to the next closing zone and next opening zone are updated.
    * Modifying the zone monitor timekeeping and the actual timers and the indices are the easy parts.
    * @see `GalaxyService`
    * @param galaxyService callback to update the server and clients;
    *                      should be the reference to `GalaxyService`, hence the literal name
    */
  def zoneRotationFunc(
                        galaxyService: ActorRef
                      ): Unit = {
    val curr = System.currentTimeMillis()
    val locking = managedZones(nextToLock)
    val unlocking = managedZones(nextToUnlock)
    val lockingZone = locking.zone
    val unlockingZone = unlocking.zone
    //val fullHoursBetweenRotationsAsHours = timeToCompleteAllRotationsHours.hours
    //val fullHoursBetweenRotationsAsMillis = fullHoursBetweenRotationsAsHours.toMillis
    val hoursBetweenRotationsAsHours = timeBetweenRotationsHours.hours
    val prevToLock = nextToLock
    nextToLock = (nextToLock + 1) % managedZones.size
    nextToUnlock = (nextToUnlock + 1) % managedZones.size
    //this zone will be locked; open when the timer runs out
    locking.locked = true
    locking.start = curr
    unlockTimerToSwitchZone(hoursBetweenRotationsAsHours)
    //this zone will be unlocked; alert the player that it will lock soon when the timer runs out
    unlocking.locked = false
    unlocking.start = curr
    lockTimerToDisplayWarning(hoursBetweenRotationsAsHours - firstClosingWarningAtMinutes.minutes)
    //alert clients to change
    if (lockingZone ne unlockingZone) {
      galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
        ChatMsg(ChatMessageType.UNK_229, s"@cavern_switched^@${lockingZone.id}~^@${unlockingZone.id}")
      ))
      //change warp gate statuses to reflect zone lock state
      CavernRotationService.disableLatticeLinksAndWarpGateAccessibility(
        ((prevToLock until managedZones.size) ++ (0 until prevToLock))
          .take(simultaneousUnlockedZones)
          .map(managedZones(_).zone)
      )
      CavernRotationService.activateLatticeLinksAndWarpGateAccessibility(
        ((nextToLock until managedZones.size) ++ (0 until nextToLock))
          .take(simultaneousUnlockedZones)
          .map(managedZones(_).zone)
      )
    }
    sendCavernRotationUpdatesToAll(galaxyService)
  }

  /**
    * If the zones are forced to rotate before the timer would normally complete,
    * correct all of the zone monitors to give the impression of the rotation that occurred.
    * Only affect the backup parameters of the timers that are maintained by the zone monitors.
    * Do not actually affect the functional timers.
    * @see `GalaxyService`
    * @param galaxyService callback to update the zone timers;
    *                      should be the reference to `GalaxyService`, hence the literal name
    */
  def retimeZonesUponForcedRotation(galaxyService: ActorRef) : Unit = {
    val curr = System.currentTimeMillis()
    val rotationSize = managedZones.size
    val fullDurationAsMillis = timeToCompleteAllRotationsHours.hours.toMillis
    val startingInThePast = curr - fullDurationAsMillis
    //this order allows the monitors to be traversed in order of ascending time to unlock
    (0 +: ((nextToUnlock until rotationSize) ++ (0 until nextToUnlock)))
      .zipWithIndex
      .drop(1)
      .foreach { case (monitorIndex, index) =>
        val zone = managedZones(monitorIndex)
        val newStart = startingInThePast + (index * timeBetweenRotationsHours).hours.toMillis
        zone.start = newStart
      }
    //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
  }

  /**
    * If the natural process of switching between caverns is hurried,
    * advance the previous start time of each zone monitor to give the impression of the hastened rotation.
    * This does not actually affect the functional timers
    * nor is it in response to an actual zone rotation event.
    * It only affects the backup parameters of the timers that are maintained by the zone monitors.
    * @see `GalaxyService`
    * @param advanceTimeBy amount of time advancement
    * @param galaxyService callback to update the zone timers;
    *                      should be the reference to `GalaxyService`, hence the literal name
    */
  def retimeZonesUponForcedAdvancement(
                                        advanceTimeBy: FiniteDuration,
                                        galaxyService: ActorRef
                                      ) : Unit = {
    //val curr = System.currentTimeMillis()
    val advanceByTimeAsMillis = advanceTimeBy.toMillis
    managedZones.foreach { zone =>
      zone.start = zone.start - advanceByTimeAsMillis
    }
    sendCavernRotationUpdatesToAll(galaxyService)
    //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
  }

  /**
    * Update the timer for the cavern closing message.
    * @param duration new time until message display
    * @param counter the counter that indicates the next message to display
    */
  def lockTimerToDisplayWarning(
                                 duration: FiniteDuration,
                                 counter: Int = firstClosingWarningAtMinutes
                               ): Unit = {
    lockTimer.cancel()
    lockTimer = context.scheduleOnce(duration, context.self, ClosingWarning(counter))
  }

  /**
    * Update the timer for the zone switching process.
    * @param duration new time until switching
    */
  def unlockTimerToSwitchZone(duration: FiniteDuration): Unit = {
    unlockTimer.cancel()
    unlockTimer = context.scheduleOnce(duration, context.self, SwitchZone)
  }
}
