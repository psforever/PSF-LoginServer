// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.Cancellable
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.objects.{DefaultCancellable, GlobalDefinitions, PlanetSideGameObject, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.types.DriveState
import services.support.{SimilarityComparator, SupportActor}

import scala.concurrent.duration._

class RouterActivation extends SupportActor[RouterActivation.Entry] {
  var activationTask : Cancellable = DefaultCancellable.obj
  var routerList : List[RouterActivation.Entry] = List()
  val sameEntryComparator = new SimilarityComparator[RouterActivation.Entry]() {
    def Test(entry1 : RouterActivation.Entry, entry2 : RouterActivation.Entry) : Boolean = {
      entry1.obj == entry2.obj && entry1.zone == entry2.zone && entry1.obj.GUID == entry2.obj.GUID
    }
  }
  val firstStandardTime : FiniteDuration = 10000 milliseconds //TODO 10s for testing

  def InclusionTest(entry : RouterActivation.Entry) : Boolean = {
    val obj = entry.obj
    obj.Definition == GlobalDefinitions.router &&
      (obj.asInstanceOf[Vehicle].DeploymentState == DriveState.Deployed ||
        obj.asInstanceOf[Vehicle].DeploymentState == DriveState.Deploying) &&
      (obj.asInstanceOf[Vehicle].Utility(UtilityType.internal_router_telepad_deployable) match {
        case Some(ipad : Utility.InternalTelepad) => !ipad.Active
        case _ => false
      })
  }

  def receive : Receive = entryManagementBehaviors
    .orElse {
      case RouterActivation.AddTask(obj, zone, duration) =>
        val entry = RouterActivation.Entry(obj, zone, duration.getOrElse(firstStandardTime).toNanos)
        if(InclusionTest(entry) && !routerList.exists(test => sameEntryComparator.Test(test, entry))) {
          if(entry.duration == 0) {
            //skip the queue altogether
            ActivationTask(entry)
          }
          else if(routerList.isEmpty) {
            //we were the only entry so the event must be started from scratch
            routerList = List(entry)
            trace(s"an activation task has been added: $entry")
            RetimeFirstTask()
          }
          else {
            //unknown number of entries; append, sort, then re-time tasking
            val oldHead = routerList.head
            if(!routerList.exists(test => sameEntryComparator.Test(test, entry))) {
              routerList = (routerList :+ entry).sortBy(entry => entry.time + entry.duration)
              trace(s"an activation task has been added: $entry")
              if(oldHead != routerList.head) {
                RetimeFirstTask()
              }
            }
            else {
              trace(s"$obj is already queued")
            }
          }
        }
        else {
          trace(s"$obj either does not qualify for this behavior or is already queued")
        }

      //private messages from self to self
      case RouterActivation.TryActivate() =>
        activationTask.cancel
        val now : Long = System.nanoTime
        val (in, out) = routerList.partition(entry => { now - entry.time >= entry.duration })
        routerList = out
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
  def RetimeFirstTask(now : Long = System.nanoTime) : Unit = {
    activationTask.cancel
    if(routerList.nonEmpty) {
      val short_timeout : FiniteDuration = math.max(1, routerList.head.duration - (now - routerList.head.time)) nanoseconds
      import scala.concurrent.ExecutionContext.Implicits.global
      activationTask = context.system.scheduler.scheduleOnce(short_timeout, self, RouterActivation.TryActivate())
    }
  }

  def HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit = {
    PartitionTargetsFromList(routerList, targets.map { RouterActivation.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been hurried")
      case (in, out) =>
        debug(s"the following tasks have been hurried: $in")
        routerList = out //.sortBy(entry => entry.time + entry.duration)
        if(out.nonEmpty) {
          RetimeFirstTask()
        }
        in.foreach { ActivationTask }
    }
  }

  def HurryAll() : Unit = {
    trace("all tasks have been hurried")
    activationTask.cancel
    routerList.foreach { ActivationTask }
    routerList = Nil
  }

  def ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit = {
    PartitionTargetsFromList(routerList, targets.map { RouterActivation.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been cleared")
      case (in, out) =>
        debug(s"the following tasks have been cleared: $in")
        routerList = out //.sortBy(entry => entry.time + entry.duration)
        if(out.nonEmpty) {
          RetimeFirstTask()
        }
    }
  }

  def ClearAll() : Unit = {
    trace("all tasks have been cleared")
    activationTask.cancel
    routerList = Nil
  }

  def ActivationTask(entry : SupportActor.Entry) : Unit = {
    context.parent ! RouterActivation.ActivateTeleportSystem(entry.obj, entry.zone)
  }
}

object RouterActivation {
  final case class Entry(_obj : PlanetSideGameObject, _zone : Zone, _duration : Long) extends SupportActor.Entry(_obj, _zone, _duration)

  final case class AddTask(obj : PlanetSideGameObject, zone : Zone, duration : Option[FiniteDuration] = None)

  final case class TryActivate()

  final case class ActivateTeleportSystem(router : PlanetSideGameObject, zone : Zone)
}
