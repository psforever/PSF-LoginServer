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
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
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

  class ZoneMonitor(val zone: Zone) {
    var locked: Boolean = true
    var start: Long = 0L
    var duration: Long = 0L
  }

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

  private def activateLatticeLinksAndWarpGateAccessibility(zoneA: Zone, zoneB: Zone): Unit = {
    establishLatticeLinksForUnlockedCavernPair(zoneA, zoneB)
    openZoneWarpGateAccessibility(zoneA)
    openZoneWarpGateAccessibility(zoneB)
  }

  private def disableLatticeLinksAndWarpGateAccessibility(zoneA: Zone, zoneB: Zone): Unit = {
    closeZoneWarpGateAccessibility(zoneA)
    closeZoneWarpGateAccessibility(zoneB)
    revokeLatticeLinksForUnlockedCavernPair(zoneA, zoneB)
  }

  private def establishLatticeLinksForUnlockedCavernPair(zoneA: Zone, zoneB: Zone): Unit = {
    val key = if (zoneA.Number < zoneB.Number) {
      s"caverns-${zoneA.id}-${zoneB.id}"
    } else {
      s"caverns-${zoneB.id}-${zoneA.id}"
    }
    Zones.cavernLattice.get(key) match {
      case Some(links) =>
        links.foreach { link =>
          val entryA = link.head
          val entryB = link.last
          val testA = entryA.split("/")
          val testB = entryB.split("/")
          (Zones.zones.find { _.id.equals(testB.head) } match {
            case Some(zoneC) =>
              (
                if (testA.head.equals(zoneA.id)) {
                  zoneA.Building(testA.last)
                } else {
                  zoneB.Building(testA.last)
                },
                zoneC.Building(testB.last)
              )
            case None =>
              (None, None)
          }) match {
            case (Some(gate1), Some(gate2)) =>
              gate1.Zone.AddIntercontinentalLatticeLink(gate1, gate2)
              gate2.Zone.AddIntercontinentalLatticeLink(gate2, gate1)
            case _ => ;
          }
        }
      case _ => ;
    }
  }

  private def revokeLatticeLinksForUnlockedCavernPair(zoneA: Zone, zoneB: Zone): Unit = {
    val key = if (zoneA.Number < zoneB.Number) {
      s"caverns-${zoneA.id}-${zoneB.id}"
    } else {
      s"caverns-${zoneB.id}-${zoneA.id}"
    }
    Zones.cavernLattice.get(key) match {
      case Some(links) =>
        links.foreach { link =>
          val entryA = link.head
          val entryB = link.last
          val testA = entryA.split("/")
          val testB = entryB.split("/")
          (Zones.zones.find { _.id.equals(testB.head) } match {
            case Some(zoneC) =>
              (
                if (testA.head.equals(zoneA.id)) {
                  zoneA.Building(testA.last)
                } else {
                  zoneB.Building(testA.last)
                },
                zoneC.Building(testB.last)
              )
            case None =>
              (None, None)
          }) match {
            case (Some(gate1), Some(gate2)) =>
              gate1.Zone.RemoveIntercontinentalLatticeLink(gate1, gate2)
              gate2.Zone.RemoveIntercontinentalLatticeLink(gate2, gate1)
            case _ => ;
          }
        }
      case _ => ;
    }
  }

  private def openZoneWarpGateAccessibility(zone: Zone): Iterable[WarpGate] = {
    findZoneWarpGatesForChangingAccessibility(zone).map { case (wg, otherWg, building) =>
      wg.Active = true
      otherWg.Active = true
      wg.Actor ! BuildingActor.AlertToFactionChange(building)
      otherWg.Zone.actor ! ZoneActor.ZoneMapUpdate()
      wg
    }
  }

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

class CavernRotationService(
                             context: ActorContext[CavernRotationService.Command],
                             buffer: StashBuffer[CavernRotationService.Command]
                           ) {
  import CavernRotationService._

  ServiceManager.serviceManager ! ServiceManager.LookupFromTyped(
    "galaxy",
    context.messageAdapter[ServiceManager.LookupResult](ServiceManagerLookupResult)
  )

  var managedZones: List[ZoneMonitor] = Nil
  var nextToLock: Int = 0
  var nextToUnlock: Int = 0
  var lockTimer: Cancellable = Default.Cancellable
  var unlockTimer: Cancellable = Default.Cancellable
  val hoursBetweenRotations: Int = Config.app.game.cavernRotation.hoursBetweenRotation
  val simultaneousUnlockedZones: Int = 2 //Config.app.game.cavernRotation.simultaneousUnlockedZones
  var fullHoursBetweenRotations: Int = 0
  val firstClosingWarningAt: Int = 15

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
        val onlyCaverns = zones.filter{ z => z.map.cavern }
        val collectedZones = Config.app.game.cavernRotation.enhancedRotationOrder match {
          case Nil  => onlyCaverns
          case list => list.flatMap { index => onlyCaverns.find(_.Number == index ) }
        }
        if (managedZones.isEmpty && collectedZones.nonEmpty) {
          managedZones = collectedZones.map(zone => new ZoneMonitor(zone)).toList
          val rotationSize = managedZones.size
          fullHoursBetweenRotations = rotationSize * hoursBetweenRotations
          val curr = System.currentTimeMillis()
          val lockTimes = (1 to rotationSize).map { i => (i * hoursBetweenRotations).hours.toMillis }
          val fullDurationAsHours = fullHoursBetweenRotations.hours
          val fullDurationAsMillis = fullDurationAsHours.toMillis
          val startingInThePast = curr - fullDurationAsMillis
          val (unlockedZones, lockedZones) = managedZones.splitAt(simultaneousUnlockedZones)
          var i = -1
          //unlocked zones
          unlockedZones.foreach { z =>
            i += 1
            z.locked = false
            z.start = startingInThePast + lockTimes(i)
            z.duration = fullDurationAsMillis
          }
          CavernRotationService.activateLatticeLinksAndWarpGateAccessibility(unlockedZones.head.zone, unlockedZones.last.zone)
          nextToLock = 0
          lockTimerToDisplayWarning(hoursBetweenRotations.hours - firstClosingWarningAt.minutes)
          //locked zones
          lockedZones.foreach { z =>
            i += 1
            z.locked = true
            z.start = startingInThePast + lockTimes(i)
            z.duration = fullDurationAsMillis
          }
          nextToUnlock = simultaneousUnlockedZones
          unlockTimerToSwitchZone(hoursBetweenRotations.hours)
          //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
        }
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

      case ReportRotationOrder(sendTo) =>
        val zoneStates = managedZones.collect {
          case zone =>
            if (zone.locked) {
              s"<${zone.zone.id}>"
            } else {
              s"${zone.zone.id}"
            }
        }.mkString(" ")
        sendTo ! SessionActor.SendResponse(
          ChatMsg(ChatMessageType.UNK_229, s"rotation=[$zoneStates]")
        )
        Behaviors.same

      case SwitchZone =>
        switchZoneFunc(galaxyService)
        Behaviors.same

      case HurryNextRotation =>
        hurryNextRotation(galaxyService)
        Behaviors.same

      case HurryRotationToZoneLock(zoneid) =>
//        if ((nextToLock until nextToLock + simultaneousUnlockedZones)
//          .map { i => managedZones(i % managedZones.size) }
//          .indexWhere { _.zone.id.equals(zoneid) } match {
//          case -1 =>
//            false
//          case  0 =>
//            true
//          case index =>
//            CavernRotationService.swapMonitors(managedZones, nextToLock, index)
//            true
//        }) {
//          hurryNextRotation(galaxyService, forcedRotationOverride=true)
//        }
        Behaviors.same

      case HurryRotationToZoneUnlock(zoneid) =>
//        if (!(nextToLock until nextToLock + simultaneousUnlockedZones)
//          .map { i => managedZones(i % managedZones.size) }
//          .exists { _.zone.id.equals(zoneid) }) {
//          if (managedZones(nextToUnlock).zone.id.equals(zoneid)) {
//            hurryNextRotation(galaxyService, forcedRotationOverride = true)
//          }
//          else {
//            //for unlocking E next, A [B C] D E F -> A [B C] E D F
//            //TODO, for unlocking F next, A [B C] D E F D -> A [B C] F E D D can not be allowed!
//            managedZones.indexWhere { z => z.zone.id.equals(zoneid) } match {
//              case -1 => ;
//              case index =>
//                CavernRotationService.swapMonitors(managedZones, nextToUnlock, index)
//                hurryNextRotation(galaxyService, forcedRotationOverride = true)
//            }
//          }
//        }
        Behaviors.same

      case SendCavernRotationUpdates(sendToSession) =>
        val curr = System.currentTimeMillis()
        val (lockedZones, unlockedZones) = managedZones.partition(_.locked)
        //borrow GalaxyService response structure, but send to the specific endpoint
        lockedZones.foreach { zone =>
          sendToSession ! GalaxyServiceResponse(
            "",
            GalaxyResponse.LockedZoneUpdate(zone.zone, math.max(0, zone.start + zone.duration - curr))
          )
        }
        unlockedZones.foreach { zone =>
          sendToSession ! GalaxyServiceResponse(
            "",
            GalaxyResponse.UnlockedZoneUpdate(zone.zone)
          )
        }
        Behaviors.same

      case _ =>
        Behaviors.same
    }
  }

  def hurryNextRotation(
                         galaxyService: ActorRef,
                         forcedRotationOverride: Boolean = false
                       ): Unit = {
    val curr = System.currentTimeMillis()
    val locking = managedZones(nextToLock)
    val timeToNextClosingEvent = locking.start + locking.duration - curr
    val fiveMinutes = 5.minutes
    if (
      forcedRotationOverride || Config.app.game.cavernRotation.forceRotationImmediately ||
      timeToNextClosingEvent < fiveMinutes.toMillis
    ) {
      //zone transition immediately
      lockTimer.cancel()
      unlockTimer.cancel()
      switchZoneFunc(galaxyService)
      lockTimerToDisplayWarning(hoursBetweenRotations.hours - firstClosingWarningAt.minutes)
      retimeZonesUponForcedRotation(galaxyService)
    } else {
      //instead of transitioning immediately, jump to the 5 minute rotation warning for the benefit of players
      lockTimer.cancel() //won't need to retime until zone change
      CavernRotationService.closedCavernWarning(locking, counter=5, galaxyService)
      unlockTimerToSwitchZone(fiveMinutes)
      retimeZonesUponForcedAdvancement(timeToNextClosingEvent.milliseconds - fiveMinutes, galaxyService)
    }
  }

  def switchZoneFunc(
                      galaxyService: ActorRef
                    ): Unit = {
    val curr = System.currentTimeMillis()
    val locking = managedZones(nextToLock)
    val unlocking = managedZones(nextToUnlock)
    val lockingZone = locking.zone
    val unlockingZone = unlocking.zone
    val fullHoursBetweenRotationsAsHours = fullHoursBetweenRotations.hours
    val fullHoursBetweenRotationsAsMillis = fullHoursBetweenRotationsAsHours.toMillis
    val hoursBetweenRotationsAsHours = hoursBetweenRotations.hours
    nextToLock = (nextToLock + 1) % managedZones.size
    nextToUnlock = (nextToUnlock + 1) % managedZones.size
    //this zone will be locked; open when the timer runs out
    locking.locked = true
    locking.start = curr
    locking.duration = fullHoursBetweenRotationsAsMillis
    unlockTimerToSwitchZone(hoursBetweenRotationsAsHours)
    //this zone will be unlocked; alert the player that it will lock soon when the timer runs out
    unlocking.locked = false
    unlocking.start = curr
    unlocking.duration = fullHoursBetweenRotationsAsMillis
    lockTimerToDisplayWarning(hoursBetweenRotationsAsHours - firstClosingWarningAt.minutes)
    //alert clients to changes
    if (lockingZone ne unlockingZone) {
      galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
        ChatMsg(ChatMessageType.UNK_229, s"@cavern_switched^@${lockingZone.id}~^@${unlockingZone.id}")
      ))
      galaxyService ! GalaxyServiceMessage(GalaxyAction.UnlockedZoneUpdate(unlockingZone))
      //change warp gate statuses to reflect zone lock state
      //TODO assumption that unlocked cavern zones are limited to 2; not a solution for 2+ simultaneous unlocks
      val bridgeZone = managedZones(nextToLock).zone
      CavernRotationService.disableLatticeLinksAndWarpGateAccessibility(lockingZone, bridgeZone)
      CavernRotationService.activateLatticeLinksAndWarpGateAccessibility(bridgeZone, unlockingZone)
    }
    galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(locking.zone, fullHoursBetweenRotationsAsMillis))
  }

  def retimeZonesUponForcedRotation(galaxyService: ActorRef) : Unit = {
    val curr = System.currentTimeMillis()
    val rotationSize = managedZones.size
    val lockTimes = (1 to rotationSize).map(i => (i * hoursBetweenRotations).hours.toMillis)
    val fullDurationAsMillis = fullHoursBetweenRotations.hours.toMillis
    val startingInThePast = curr - fullDurationAsMillis
    ((nextToLock until managedZones.size) ++ (0 until nextToLock))
      .map { managedZones(_) }
      .zipWithIndex
      .drop(1)
      .foreach { case(zone, index) =>
        zone.start = startingInThePast + lockTimes(index)
        zone.duration = fullDurationAsMillis
      }
    managedZones
      .filter { _.locked }
      .foreach { zone =>
        galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(zone.zone, zone.start + zone.duration - curr))
      }
    //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
  }

  def retimeZonesUponForcedAdvancement(
                                        advanceTimeBy: FiniteDuration,
                                        galaxyService: ActorRef
                                      ) : Unit = {
    val curr = System.currentTimeMillis()
    val advanceByTimeAsMillis = advanceTimeBy.toMillis
    managedZones.foreach { m =>
      m.start = m.start - advanceByTimeAsMillis
    }
    managedZones
      .filter { _.locked }
      .foreach { zone =>
        galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(zone.zone, zone.start + zone.duration - curr))
      }
    //println(managedZones.flatMap { z => s"[${z.start + z.duration - curr}]"}.mkString(""))
  }

  def lockTimerToDisplayWarning(
                                 duration: FiniteDuration,
                                 counter: Int = firstClosingWarningAt
                               ): Unit = {
    lockTimer.cancel()
    lockTimer = context.scheduleOnce(duration, context.self, ClosingWarning(counter))
  }

  def unlockTimerToSwitchZone(duration: FiniteDuration): Unit = {
    unlockTimer.cancel()
    unlockTimer = context.scheduleOnce(duration, context.self, SwitchZone)
  }
}
