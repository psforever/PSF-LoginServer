// Copyright (c) 2022 PSForever
package net.psforever.services

import akka.actor.{ActorRef, Cancellable}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.actors.zone.BuildingActor
import net.psforever.actors.zone.building.WarpGateLogic
import net.psforever.objects.Default
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
import net.psforever.types.ChatMessageType

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

  case object HurryNextRotation extends Command

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

  private def toggleZoneWarpGateAccessibility(zone: Zone, lockState: Boolean): Iterable[WarpGate] = {
    zone.Buildings.values
      .collect {
        case wg: WarpGate =>
          val neighborhood = wg.AllNeighbours.getOrElse(Nil)
          (
            WarpGateLogic.findNeighborhoodWarpGate(neighborhood),
            WarpGateLogic.findNeighborhoodNormalBuilding(neighborhood)
          ) match {
            case (Some(otherWg: WarpGate), Some(building)) =>
              wg.Active = lockState
              otherWg.Active = lockState
              wg.Actor ! BuildingActor.AlertToFactionChange(building)
              if (!lockState) {
                //must trigger the connection test from the other side to equalize
                WarpGateLogic.findNeighborhoodNormalBuilding(otherWg.Neighbours.getOrElse(Nil)) match {
                  case Some(b) => otherWg.Actor ! BuildingActor.AlertToFactionChange(b)
                  case None => ;
                }
              }
            case _ => ;
          }
          wg
      }
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
  val hoursBetweenRotations: Int = 3
  var fullHoursBetweenRotations: Int = 0
  val simultaneousOpenZones: Int = 2
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
        val collectedZones = zones.filter(_.map.cavern)
        if (managedZones.isEmpty && collectedZones.nonEmpty) {
          managedZones = collectedZones.map(zone => new ZoneMonitor(zone)).toList
          val rotationSize = managedZones.size
          fullHoursBetweenRotations = rotationSize * hoursBetweenRotations
          val curr = System.currentTimeMillis()
          val lockTimes = (1 to rotationSize).map { i => (i * hoursBetweenRotations).hours.toMillis }
          val fullDurationAsHours = fullHoursBetweenRotations.hours
          val fullDurationAsMillis = fullDurationAsHours.toMillis
          val startingInThePast = curr - fullDurationAsMillis
          val (lockedZones, unlockedZones) = managedZones.splitAt(simultaneousOpenZones)
          var i = -1
          //locked zones
          lockedZones.foreach { z =>
            i += 1
            z.locked = true
            z.start = startingInThePast + lockTimes(i)
            z.duration = fullDurationAsMillis
          }
          nextToUnlock = simultaneousOpenZones
          unlockTimerToSwitchZone(hoursBetweenRotations.hours)
          //unlocked zones
          unlockedZones.foreach { z =>
            i += 1
            z.locked = false
            z.start = startingInThePast + lockTimes(i)
            z.duration = fullDurationAsMillis
            //CavernRotationService.toggleZoneWarpGateAccessibility(z.zone, lockState = true)
          }
          nextToLock = 0
          lockTimerToDisplayWarning(hoursBetweenRotations.hours - firstClosingWarningAt.minutes)
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

      case SwitchZone =>
        switchZoneFunc(galaxyService)
        Behaviors.same

      case HurryNextRotation =>
        val curr = System.currentTimeMillis()
        val locking = managedZones(nextToLock)
        val timeToNextClosingEvent = locking.start + locking.duration - curr
        val fiveMinutes = 5.minutes
        val (excluded, correctionTime) = if (timeToNextClosingEvent > fiveMinutes.toMillis) {
          //instead of transitioning immediately, jump to the 5 minute rotation warning for the benefit of players
          lockTimer.cancel() //won't need to retime until zone change
          CavernRotationService.closedCavernWarning(locking, counter=5, galaxyService)
          unlockTimerToSwitchZone(fiveMinutes)
          (Set.empty[Int], timeToNextClosingEvent.milliseconds - fiveMinutes)
        } else {
          //zone transition immediately
          switchZoneFunc(galaxyService)
          unlockTimer.cancel()
          lockTimerToDisplayWarning(hoursBetweenRotations.hours - firstClosingWarningAt.minutes)
          (Set(nextToUnlock, nextToLock), timeToNextClosingEvent.milliseconds)
        }
        retimeLockedAndUnlockedZones(excluded, correctionTime, galaxyService)
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
    lockTimerToDisplayWarning(hoursBetweenRotationsAsHours - firstClosingWarningAt.minutes)
    //alert clients
    galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
      ChatMsg(ChatMessageType.UNK_229, s"@cavern_switched^@${lockingZone.id}~^@${unlockingZone.id}")
    ))
    galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(locking.zone, fullHoursBetweenRotationsAsMillis))
    galaxyService ! GalaxyServiceMessage(GalaxyAction.UnlockedZoneUpdate(unlockingZone))
    //change warp gate statuses to reflect zone lock state
    CavernRotationService.toggleZoneWarpGateAccessibility(lockingZone, lockState = false)
    CavernRotationService.toggleZoneWarpGateAccessibility(unlockingZone, lockState = true)
  }

  def retimeLockedAndUnlockedZones(
                                    excludedIndices: Set[Int],
                                    advanceTimeBy: FiniteDuration,
                                    galaxyService: ActorRef
                                  ) : Unit = {
    val curr = System.currentTimeMillis()
    val advanceByTimeAsMillies = advanceTimeBy.toMillis
    val (locked, unlocked) = managedZones
      .zipWithIndex
      .filterNot { case (_, i) => excludedIndices.contains(i) }
      .partition { case (z, _) => z.locked }
    locked.foreach { case (zone, _) =>
      zone.start = zone.start - advanceByTimeAsMillies
      galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(zone.zone, zone.start + zone.duration - curr))
    }
    unlocked.foreach { case (zone, _) =>
      zone.start = zone.start - advanceByTimeAsMillies
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
