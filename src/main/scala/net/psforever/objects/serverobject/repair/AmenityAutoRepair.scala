//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.actor.typed.{ActorRef => TypedActorRef}
import akka.actor.typed.scaladsl.adapter.ClassicActorRefOps
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, NtuContainer, NtuStorageBehavior}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.structures.{Amenity, AutoRepairStats}

import scala.concurrent.duration._

trait AmenityAutoRepair
  extends NtuStorageBehavior {
  _: Damageable with RepairableEntity with Actor =>
  private lazy val ntuGrantActorRef: TypedActorRef[NtuCommand.Grant] =
    new ClassicActorRefOps(self).toTyped[NtuCommand.Grant]
  private var autoRepairStartFunc: ()=>Unit = startAutoRepairIfStopped
  private var autoRepairTimer: Cancellable  = Default.Cancellable

  def AutoRepairObject: Amenity

  final val autoRepairBehavior: Receive = storageBehavior.orElse {
    case BuildingActor.SuppliedWithNtu() =>
      withNtuSupplyCallback()

    case BuildingActor.NtuDepleted() =>
      noNtuSupplyCallback()
  }

  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit = { }

  def StopNtuBehavior(sender : ActorRef) : Unit = {
    autoRepairTimer.cancel()
  }

  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit = { }

  def HandleNtuGrant(sender : ActorRef, src : NtuContainer, amount : Float) : Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(repair : AutoRepairStats) if obj.Health < obj.Definition.MaxHealth =>
        PerformRepairs(obj, repair.amount)
      case _ => ;
    }
  }

  def withNtuSupplyCallback(): Unit = {
    startAutoRepairFunctionality()
  }

  def noNtuSupplyCallback(): Unit = {
    stopAutoRepairFunctionality()
  }

  private def startAutoRepairFunctionality(): Unit = {
    retimeAutoRepair()
    autoRepairStartFunc = startAutoRepairIfStopped
  }

  private def stopAutoRepairFunctionality(): Unit = {
    autoRepairTimer.cancel()
    autoRepairStartFunc = ()=>{}
  }

  private def startAutoRepairIfStopped(): Unit = {
    if(autoRepairTimer.isCancelled) {
      retimeAutoRepair()
    }
  }

  final def tryAutoRepair(): Unit = {
    autoRepairStartFunc()
  }

  final def stopAutoRepair(): Unit = {
    autoRepairTimer.cancel()
  }

  private def retimeAutoRepair(): Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(AutoRepairStats(_, start, interval, drain))
        if obj.Health < obj.Definition.MaxHealth =>
        retimeAutoRepair(start, interval, drain)
      case _ => ;
    }
  }

  private def retimeAutoRepair(initialDelay: Long, delay: Long, drain: Float): Unit = {
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
