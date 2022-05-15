// Copyright (c) 2022 PSForever
package net.psforever.services

import akka.actor.{ActorRef, Cancellable}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.{actor => classic}
import net.psforever.objects.Default
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
import net.psforever.types.ChatMessageType

import scala.concurrent.duration._
import scala.util.Random

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

  final case class ManageCaverns(zones: Iterable[Zone]) extends Command

  final case class SwitchZone(index: Int) extends Command

  final case class SendCavernRotationUpdates(sendToSession: ActorRef) extends Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  final case class LockedZoneUpdate(zone: Zone, timeUntilUnlock: Long)

  final case class UnlockedZoneUpdate(zone: Zone)

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

  var managedZones: List[ZoneMonitor]         = Nil
  var galaxyService: classic.ActorRef = classic.ActorRef.noSender

  def start(): Behavior[CavernRotationService.Command] = {
    Behaviors.receiveMessage {
      case ServiceManagerLookupResult(ServiceManager.LookupResult(request, endpoint)) =>
        request match {
          case "galaxy" =>
            galaxyService = endpoint
            buffer.unstashAll(active())
          case _ =>
            Behaviors.same
        }

      case other =>
        buffer.stash(other)
        Behaviors.same
    }
  }

  def active(): Behavior[CavernRotationService.Command] = {
    Behaviors.receiveMessage {
      case ManageCaverns(zones) =>
        if (managedZones.isEmpty) {
          managedZones = zones.filter(_.map.cavern).map(zone => new ZoneMonitor(zone)).toList
          val lockTimes = List(3, 6, 12)
          //locked zones
          lockTimes.zipWithIndex.foreach { case (t, i) =>
            val duration = t.minutes
            val managed = managedZones(i)
            managed.locked = true
            managed.duration = duration.toMillis
            managed.timer = context.scheduleOnce(duration, context.self, SwitchZone(i))
            managed.start = System.currentTimeMillis()
          }
          //unlocked zones
          managedZones.drop(lockTimes.size).foreach { managed =>
            managed.locked = false
          }
        }
        Behaviors.same

      case SwitchZone(index) =>
        val (locking, i) = managedZones.zipWithIndex(index)
        val unlocked = Random.shuffle(managedZones.filter { z => z.locked }).head
        locking.locked = true
        locking.timer = context.scheduleOnce(15.minutes, context.self, SwitchZone(i))
        locking.start = System.currentTimeMillis()
        unlocked.locked = false
        unlocked.timer = Default.Cancellable
        unlocked.start = System.currentTimeMillis()
        galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(
          ChatMsg(ChatMessageType.UNK_229, s"@cavern_switched~^@${locking.zone.id}~^@${unlocked.zone.id}")
        ))
        galaxyService ! GalaxyServiceMessage(GalaxyAction.LockedZoneUpdate(locking.zone, 15*60*1000L))
        galaxyService ! GalaxyServiceMessage(GalaxyAction.UnlockedZoneUpdate(unlocked.zone))
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
