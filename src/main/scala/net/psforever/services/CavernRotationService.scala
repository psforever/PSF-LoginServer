// Copyright (c) 2022 PSForever
package net.psforever.services

import akka.actor.{ActorRef, Cancellable}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.Default
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
import net.psforever.types.ChatMessageType

import scala.concurrent.duration._

object CavernRotationService {
  val CavernRotationServiceKey: ServiceKey[Command] =
    ServiceKey[CavernRotationService.Command]("cavernRotationService")

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

  final case class SwitchZone(index: Int) extends Command

  final case class SendCavernRotationUpdates(sendToSession: ActorRef) extends Command

  case object HurryNextRotation extends Command

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long)

  final case class UnlockedZoneUpdate(zone: Zone)

  private case class ClosingWarning(index: Int, counter: Int) extends Command

  class ZoneMonitor(val zone: Zone) {
    var locked: Boolean = true
    var duration: Long = 0L
    var start: Long = 0L
    var timer: Cancellable = Default.Cancellable
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
        if (managedZones.isEmpty) {
          managedZones = zones.filter(_.map.cavern).map(zone => new ZoneMonitor(zone)).toList
          val lockTimes = List(3, 6)
          val managedWithIndex = managedZones.zipWithIndex
          val (lockedZones, unlockedZones) = managedWithIndex.splitAt(lockTimes.size)
          //locked zones
          lockedZones.foreach { case (z, i) =>
            val duration = lockTimes(i).hours
            z.locked = true
            z.duration = duration.toMillis
            z.timer = context.scheduleOnce(duration, context.self, SwitchZone(i))
            z.start = System.currentTimeMillis()
          }
          //next
          val nextZoneToLock = unlockedZones.head
          nextToLock = nextZoneToLock._2
          nextZoneToLock._1.timer = context.scheduleOnce(30.seconds - 15.seconds, context.self, ClosingWarning(nextToLock, 15))
        }
        Behaviors.same

      case ClosingWarning(index, counter)
        if counter == 15 || counter == 10 =>
        val zone = managedZones(nextToLock)
        if (!zone.locked) {
          galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
            ChatMsg(ChatMessageType.UNK_229, s"@cavern_closing_warning^@${zone.zone.id}~^@$counter~")
          ))
          val next = counter - 5
          zone.timer = context.scheduleOnce(next.minutes, context.self, ClosingWarning(index, next))
        }
        Behaviors.same

      case ClosingWarning(index, counter) =>
        val zone = managedZones(nextToLock)
        if (!zone.locked) {
          galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
            ChatMsg(ChatMessageType.UNK_229, s"@cavern_closing_warning^@${zone.zone.id}~^@$counter~")
          ))
        }
        Behaviors.same

      case SwitchZone(index) =>
        val curr = System.currentTimeMillis()
        val durationBase = 9
        val duration = durationBase.hours
        val locking = managedZones(index)
        val unlocked = managedZones(nextToLock)
        nextToLock = (nextToLock + 1) % managedZones.size
        locking.locked = true
        locking.timer = context.scheduleOnce(duration, context.self, SwitchZone(index))
        locking.start = curr
        unlocked.locked = false
        unlocked.timer = context.scheduleOnce(duration - 15.minutes, context.self, ClosingWarning(index, 15))
        unlocked.start = curr
        galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
          ChatMsg(ChatMessageType.UNK_229, s"@cavern_switched^@${locking.zone.id}~^@${unlocked.zone.id}")
        ))
        galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(locking.zone, duration.toMillis))
        galaxyService ! GalaxyServiceMessage(GalaxyAction.UnlockedZoneUpdate(unlocked.zone))
        Behaviors.same

      case HurryNextRotation =>
        Behaviors.same

      case SendCavernRotationUpdates(sendToSession) =>
        val curr = System.currentTimeMillis()
        val (lockedZones, unlockedZones) = managedZones.partition(_.locked)
        //borrow GalaxyService response structure, but send to the specific endpoint
        lockedZones.foreach { zone =>
          sendToSession ! GalaxyServiceResponse(
            "",
            GalaxyResponse.LockedZoneUpdate(zone.zone, math.max(0, zone.duration - (curr - zone.start)))
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
}
