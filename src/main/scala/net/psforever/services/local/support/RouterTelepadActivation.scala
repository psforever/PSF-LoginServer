// Copyright (c) 2017 PSForever
package net.psforever.services.local.support

import akka.actor.Cancellable
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.services.support.{SimilarityComparator, SupportActor}

import scala.concurrent.duration._

class RouterTelepadActivation extends SupportActor[RouterTelepadActivation.Entry] {
  var activationTask: Cancellable                      = Default.Cancellable
  var telepadList: List[RouterTelepadActivation.Entry] = List()
  val sameEntryComparator = new SimilarityComparator[RouterTelepadActivation.Entry]() {
    def Test(entry1: RouterTelepadActivation.Entry, entry2: RouterTelepadActivation.Entry): Boolean = {
      (entry1.obj eq entry2.obj) && (entry1.zone eq entry2.zone) && entry1.obj.GUID == entry2.obj.GUID
    }
  }
  val firstStandardTime: FiniteDuration = 60 seconds

  def InclusionTest(entry: RouterTelepadActivation.Entry): Boolean = {
    val obj = entry.obj
    obj.isInstanceOf[TelepadDeployable] && !obj.asInstanceOf[TelepadDeployable].Active
  }

  def receive: Receive =
    entryManagementBehaviors
      .orElse {
        case RouterTelepadActivation.AddTask(obj, zone, duration) =>
          val entry = RouterTelepadActivation.Entry(obj, zone, duration.getOrElse(firstStandardTime).toNanos)
          if (InclusionTest(entry) && !telepadList.exists(test => sameEntryComparator.Test(test, entry))) {
            if (entry.duration == 0) {
              //skip the queue altogether
              ActivationTask(entry)
            } else if (telepadList.isEmpty) {
              //we were the only entry so the event must be started from scratch
              telepadList = List(entry)
              trace(s"an activation task has been added: $entry")
              RetimeFirstTask()
            } else {
              //unknown number of entries; append, sort, then re-time tasking
              val oldHead = telepadList.head
              if (!telepadList.exists(test => sameEntryComparator.Test(test, entry))) {
                telepadList = (telepadList :+ entry).sortBy(entry => entry.time + entry.duration)
                trace(s"an activation task has been added: $entry")
                if (oldHead != telepadList.head) {
                  RetimeFirstTask()
                }
              } else {
                trace(s"$obj is already queued")
              }
            }
          } else {
            trace(s"$obj either does not qualify for this behavior or is already queued")
          }

        //private messages from self to self
        case RouterTelepadActivation.TryActivate() =>
          activationTask.cancel()
          val now: Long = System.nanoTime
          val (in, out) = telepadList.partition(entry => { now - entry.time >= entry.duration })
          telepadList = out
          in.foreach { ActivationTask }
          RetimeFirstTask()
          trace(s"router activation task has found ${in.size} items to process")

        case _ => ;
      }

  /**
    * Common function to reset the first task's delayed execution.
    * Cancels the scheduled timer and will only restart the timer if there is at least one entry in the first pool.
    * @param now the time (in nanoseconds);
    *            defaults to the current time (in nanoseconds)
    */
  def RetimeFirstTask(now: Long = System.nanoTime): Unit = {
    activationTask.cancel()
    if (telepadList.nonEmpty) {
      val short_timeout: FiniteDuration =
        math.max(1, telepadList.head.duration - (now - telepadList.head.time)) nanoseconds
      import scala.concurrent.ExecutionContext.Implicits.global
      activationTask = context.system.scheduler.scheduleOnce(short_timeout, self, RouterTelepadActivation.TryActivate())
    }
  }

  def HurrySpecific(targets: List[PlanetSideGameObject], zone: Zone): Unit = {
    PartitionTargetsFromList(telepadList, targets.map { RouterTelepadActivation.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been hurried")
      case (in, out) =>
        debug(s"the following tasks have been hurried: $in")
        telepadList = out
        if (out.nonEmpty) {
          RetimeFirstTask()
        }
        in.foreach { ActivationTask }
    }
  }

  def HurryAll(): Unit = {
    trace("all tasks have been hurried")
    activationTask.cancel()
    telepadList.foreach {
      ActivationTask
    }
    telepadList = Nil
  }

  def ClearSpecific(targets: List[PlanetSideGameObject], zone: Zone): Unit = {
    PartitionTargetsFromList(telepadList, targets.map { RouterTelepadActivation.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been cleared")
      case (in, out) =>
        debug(s"the following tasks have been cleared: $in")
        telepadList = out //.sortBy(entry => entry.time + entry.duration)
        if (out.nonEmpty) {
          RetimeFirstTask()
        }
    }
  }

  def ClearAll(): Unit = {
    trace("all tasks have been cleared")
    activationTask.cancel()
    telepadList = Nil
  }

  def ActivationTask(entry: SupportActor.Entry): Unit = {
    entry.obj.asInstanceOf[TelepadDeployable].Active = true
    context.parent ! RouterTelepadActivation.ActivateTeleportSystem(entry.obj, entry.zone)
  }
}

object RouterTelepadActivation {
  final case class Entry(_obj: PlanetSideGameObject, _zone: Zone, _duration: Long)
      extends SupportActor.Entry(_obj, _zone, _duration)

  final case class AddTask(obj: PlanetSideGameObject, zone: Zone, duration: Option[FiniteDuration] = None)

  final case class TryActivate()

  final case class ActivateTeleportSystem(telepad: PlanetSideGameObject, zone: Zone)
}
