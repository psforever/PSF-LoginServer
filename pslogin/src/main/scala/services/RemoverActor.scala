// Copyright (c) 2017 PSForever
package services

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.zones.Zone
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject}
import net.psforever.types.Vector3

import scala.annotation.tailrec
import scala.concurrent.duration._

abstract class RemoverActor extends Actor {
  protected var firstTask : Cancellable = DefaultCancellable.obj
  protected var firstHeap : List[RemoverActor.Entry] = List()

  protected var secondTask : Cancellable = DefaultCancellable.obj
  protected var secondHeap : List[RemoverActor.Entry] = List()

  protected var taskResolver : ActorRef = Actor.noSender

  protected[this] val log = org.log4s.getLogger

  override def preStart() : Unit = {
    super.preStart()
    self ! RemoverActor.Startup()
  }

  override def postStop() = {
    super.postStop()
    firstTask.cancel
    secondTask.cancel

    firstHeap.foreach(entry => {
      FirstJob(entry)
      SecondJob(entry)
    })
    secondHeap.foreach { SecondJob }
  }

  def receive : Receive = {
    case RemoverActor.Startup() =>
      ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case msg =>
      log.error(s"received message $msg before being properly initialized")
  }

  def Processing : Receive = {
    case RemoverActor.AddTask(obj, zone, duration) =>
      val entry = RemoverActor.Entry(obj, zone, duration.getOrElse(FirstStandardDuration).toNanos)
      if(InclusionTest(entry) && !secondHeap.exists(test => RemoverActor.Similarity(test, entry) )) {
        InitialJob(entry)
        if(firstHeap.isEmpty) {
          //we were the only entry so the event must be started from scratch
          firstHeap = List(entry)
          RetimeFirstTask()
        }
        else {
          //unknown number of entries; append, sort, then re-time tasking
          val oldHead = firstHeap.head
          if(!firstHeap.exists(test => RemoverActor.Similarity(test, entry))) {
            firstHeap = (firstHeap :+ entry).sortBy(_.duration)
            if(oldHead != firstHeap.head) {
              RetimeFirstTask()
            }
          }
          else {
            log.trace(s"$obj is already queued for removal")
          }
        }
      }
      else {
        log.trace(s"$obj either does not qualify for this Remover or is already queued")
      }

    case RemoverActor.HurrySpecific(targets, zone) =>
      CullTargetsFromFirstHeap(targets, zone) match {
        case Nil => ;
        case list =>
          secondTask.cancel
          list.foreach { FirstJob }
          secondHeap = list ++ secondHeap
          import scala.concurrent.ExecutionContext.Implicits.global
          secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
      }

    case RemoverActor.HurryAll() =>
      firstTask.cancel
      firstHeap.foreach { FirstJob }
      secondHeap = secondHeap ++ firstHeap
      firstHeap = Nil
      secondTask.cancel
      import scala.concurrent.ExecutionContext.Implicits.global
      secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())

    case RemoverActor.ClearSpecific(targets, zone) =>
      CullTargetsFromFirstHeap(targets, zone)

    case RemoverActor.ClearAll() =>
      firstTask.cancel
      firstHeap = Nil

      //private messages
    case RemoverActor.StartDelete() =>
      firstTask.cancel
      secondTask.cancel
      val now : Long = System.nanoTime
      val (in, out) = firstHeap.partition(entry => { now - entry.time >= entry.duration })
      firstHeap = out
      secondHeap = secondHeap ++ in
      in.foreach { FirstJob }
      RetimeFirstTask()
      if(secondHeap.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
      }
      log.trace(s"item removal task has found ${secondHeap.size} items to remove")

    case RemoverActor.TryDelete() =>
      secondTask.cancel
      val (in, out) = secondHeap.partition { ClearanceTest }
      secondHeap = out
      in.foreach { SecondJob }
      if(out.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
      }
      log.trace(s"item removal task has removed ${in.size} items")

    case RemoverActor.FailureToWork(entry, ex) =>
      log.error(s"${entry.obj} from ${entry.zone} not properly unregistered - $ex")
  }

  private def CullTargetsFromFirstHeap(targets : List[PlanetSideGameObject], zone : Zone) : List[RemoverActor.Entry] = {
    if(targets.nonEmpty) {
      firstTask.cancel
      val culledEntries = if(targets.size == 1) {
        log.debug(s"a target submitted for early cleanup: ${targets.head}")
        //simple selection
        RemoverActor.recursiveFind(firstHeap.iterator, RemoverActor.Entry(targets.head, zone, 0)) match {
          case None => ;
            Nil
          case Some(index) =>
            val entry = firstHeap(index)
            firstHeap = (firstHeap.take(index) ++ firstHeap.drop(index + 1)).sortBy(_.duration)
            List(entry)
        }
      }
      else {
        log.trace(s"multiple targets submitted for early cleanup: $targets")
        //cumbersome partition
        //a - find targets from entries
        val locatedTargets = for {
          a <- targets.map(RemoverActor.Entry(_, zone, 0))
          b <- firstHeap
          if b.obj.HasGUID && a.obj.HasGUID && RemoverActor.Similarity(b, a)
        } yield b
        if(locatedTargets.nonEmpty) {
          //b - entries, after the found targets are removed (cull any non-GUID entries while at it)
          firstHeap = (for {
            a <- locatedTargets
            b <- firstHeap
            if b.obj.HasGUID && a.obj.HasGUID && !RemoverActor.Similarity(b, a)
          } yield b).sortBy(_.duration)
          locatedTargets
        }
        else {
          Nil
        }
      }
      RetimeFirstTask()
      culledEntries
    }
    else {
      Nil
    }
  }

  def RetimeFirstTask(now : Long = System.nanoTime) : Unit = {
    firstTask.cancel
    if(firstHeap.nonEmpty) {
      val short_timeout : FiniteDuration = math.max(1, firstHeap.head.duration - (now - firstHeap.head.time)) nanoseconds
      import scala.concurrent.ExecutionContext.Implicits.global
      firstTask = context.system.scheduler.scheduleOnce(short_timeout, self, RemoverActor.StartDelete())
    }
  }

  def SecondJob(entry : RemoverActor.Entry) : Unit = {
    entry.obj.Position = Vector3.Zero //somewhere it will not disturb anything
    taskResolver ! FinalTask(entry)
  }

  def FinalTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    import net.psforever.objects.guid.Task
    TaskResolver.GiveTask (
      new Task() {
        private val localEntry = entry
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = if(!localEntry.obj.HasGUID) {
          Task.Resolution.Success
        }
        else {
          Task.Resolution.Incomplete
        }

        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }

        override def onFailure(ex : Throwable): Unit = {
          localAnnounce ! RemoverActor.FailureToWork(localEntry, ex)
        }
      }, List(DeletionTask(entry))
    )
  }

  def FirstStandardDuration : FiniteDuration

  def SecondStandardDuration : FiniteDuration

  def InclusionTest(entry : RemoverActor.Entry) : Boolean

  def InitialJob(entry : RemoverActor.Entry) : Unit

  def FirstJob(entry : RemoverActor.Entry) : Unit

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask
}

object RemoverActor {
  /**
    * na
    * @param obj the target
    * @param zone the zone in which this target is registered
    * @param duration how much longer the target will exist (in nanoseconds)
    * @param time when this entry was created (in nanoseconds)
    */
  case class Entry(obj : PlanetSideGameObject, zone : Zone, duration : Long, time : Long = System.nanoTime)

  case class Startup()

  case class AddTask(obj : PlanetSideGameObject, zone : Zone, duration : Option[FiniteDuration] = None)

  case class HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone)

  case class HurryAll()

  case class ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone)

  case class ClearAll()

  protected final case class FailureToWork(entry : RemoverActor.Entry, ex : Throwable)

  private final case class StartDelete()

  private final case class TryDelete()

  private def Similarity(entry1 : RemoverActor.Entry, entry2 : RemoverActor.Entry) : Boolean = {
    entry1.obj == entry2.obj && entry1.zone == entry2.zone && entry1.obj.GUID == entry2.obj.GUID
  }

  @tailrec private def recursiveFind(iter : Iterator[RemoverActor.Entry], target : RemoverActor.Entry, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val entry = iter.next
      if(entry.obj.HasGUID && target.obj.HasGUID && Similarity(entry, target)) {
        Some(index)
      }
      else {
        recursiveFind(iter, target, index + 1)
      }
    }
  }
}
