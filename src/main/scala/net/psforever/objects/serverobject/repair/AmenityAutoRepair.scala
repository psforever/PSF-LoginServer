//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.{Actor, Cancellable}
import akka.actor.typed.{ActorRef => TypedActorRef}
import akka.actor.typed.scaladsl.adapter.ClassicActorRefOps
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, Ntu}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.structures.{Amenity, AutoRepairStats}

import scala.concurrent.duration._

trait AmenityAutoRepair {
  _: Damageable with RepairableEntity with Actor =>
  private lazy val ntuGrantActorRef: TypedActorRef[NtuCommand.Grant] =
    new ClassicActorRefOps(self).toTyped[NtuCommand.Grant]
  private var autoRepairStartFunc: ()=>Unit                          = startAutoRepairIfStopped
  private var autoRepairTimer: Cancellable                           = Default.Cancellable

  def AutoRepairObject: Amenity

  final val autoRepairBehavior: Receive = {
    case BuildingActor.PowerOn() =>
      powerOnCallback()

    case BuildingActor.PowerOff() =>
      powerOffCallback()

    case Ntu.Grant(_, 0) | NtuCommand.Grant(_, 0) =>
      autoRepairTimer.cancel()

    case Ntu.Grant(_, _) | NtuCommand.Grant(_, _) =>
      val obj = AutoRepairObject
      obj.Definition.autoRepair match {
        case Some(repair : AutoRepairStats) =>
          PerformRepairs(obj, repair.amount)
        case _ => ;
      }
  }

  def powerOnCallback(): Unit = {
    startAutoRepairFunctionality()
  }

  def powerOffCallback(): Unit = {
    stopAutoRepairFunctionality()
  }

  final def startAutoRepairFunctionality(): Unit = {
    retimeAutoRepair()
    autoRepairStartFunc = startAutoRepairIfStopped
  }

  final def stopAutoRepairFunctionality(): Unit = {
    autoRepairTimer.cancel()
    autoRepairStartFunc = ()=>{}
  }

  final def startAutoRepairIfStopped(): Unit = {
    if(autoRepairTimer.isCancelled) {
      retimeAutoRepair()
    }
  }

  final def stopAutoRepair(): Unit = {
    autoRepairTimer.cancel()
  }

  final def retimeAutoRepair(): Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(AutoRepairStats(_, start, interval, drain))
        if obj.Definition.Damageable && obj.Health < obj.Definition.MaxHealth =>
        retimeAutoRepair(start, interval, drain)
      case _ => ;
    }
  }

  final def retimeAutoRepair(initialDelay: Long, delay: Long, drain: Float): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    autoRepairTimer.cancel()
    autoRepairTimer = context.system.scheduler.scheduleWithFixedDelay(
      initialDelay milliseconds,
      delay milliseconds,
      AutoRepairObject.Owner.Actor,
      BuildingActor.Ntu(NtuCommand.Request(drain, ntuGrantActorRef))
    )
  }
}
