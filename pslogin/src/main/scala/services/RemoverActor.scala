// Copyright (c) 2017 PSForever
package services

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.zones.Zone
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject}
import net.psforever.types.Vector3

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * The base class for a type of "destruction `Actor`" intended to be used for delaying object cleanup activity.
  * Objects submitted to this process should be registered to a global unique identified system for a given region
  * as is specified in their submission.<br>
  * <br>
  * Two waiting lists are used to pool the objects being removed.
  * The first list is a basic pooling list that precludes any proper removal actions
  * and is almost expressly for delaying the process.
  * Previously-submitted tasks can be removed from this list so long as a matching object can be found.
  * Tasks in this list can also be expedited into the second list without having to consider delays.
  * After being migrated to the secondary list, the object is considered beyond the point of no return.
  * Followup activity will lead to its inevitable unregistering and removal.<br>
  * <br>
  * Functions have been provided for `override` in order to interject the appropriate cleanup operations.
  * The activity itself is typically removing the object in question from a certain list,
  * dismissing it with a mass distribution of `ObjectDeleteMessage` packets,
  * and finally unregistering it.
  * Some types of object have (de-)implementation variations which should be made explicit through the overrides.
  */
abstract class RemoverActor extends Actor {
  /**
    * The timer that checks whether entries in the first pool are still eligible for that pool.
    */
  var firstTask : Cancellable = DefaultCancellable.obj
  /**
    * The first pool of objects waiting to be processed for removal.
    */
  var firstHeap : List[RemoverActor.Entry] = List()

  /**
    * The timer that checks whether entries in the second pool are still eligible for that pool.
    */
  var secondTask : Cancellable = DefaultCancellable.obj
  /**
    * The second pool of objects waiting to be processed for removal.
    */
  var secondHeap : List[RemoverActor.Entry] = List()

  private var taskResolver : ActorRef = Actor.noSender

  private[this] val log = org.log4s.getLogger

  /**
    * Send the initial message that requests a task resolver for assisting in the removal process.
    */
  override def preStart() : Unit = {
    super.preStart()
    self ! RemoverActor.Startup()
  }

  /**
    * Sufficiently clean up the current contents of these waiting removal jobs.
    * Cancel all timers, rush all entries in the lists through their individual steps, then empty the lists.
    * This is an improved `HurryAll`, but still faster since it also railroads entries through the second queue as well.
    */
  override def postStop() = {
    super.postStop()
    firstTask.cancel
    secondTask.cancel
    firstHeap.foreach(entry => {
      FirstJob(entry)
      SecondJob(entry)
    })
    secondHeap.foreach { SecondJob }
    firstHeap = Nil
    secondHeap = Nil
    taskResolver = ActorRef.noSender
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
      HurrySpecific(targets, zone)

    case RemoverActor.HurryAll() =>
      HurryAll()

    case RemoverActor.ClearSpecific(targets, zone) =>
      ClearSpecific(targets, zone)

    case RemoverActor.ClearAll() =>
      ClearAll()

    //private messages from RemoverActor to RemoverActor
    case RemoverActor.StartDelete() =>
      firstTask.cancel
      secondTask.cancel
      val now : Long = System.nanoTime
      val (in, out) = firstHeap.partition(entry => { now - entry.time >= entry.duration })
      firstHeap = out
      secondHeap = secondHeap ++ in.map { RepackageEntry }
      in.foreach { FirstJob }
      RetimeFirstTask()
      if(secondHeap.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
      }
      log.trace(s"item removal task has found ${in.size} items to remove")

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

    case _ => ;
  }

  /**
    * Expedite some entries from the first pool into the second.
    * @param targets a list of objects to pick
    * @param zone the zone in which these objects must be discovered;
    *             all targets must be in this zone, with the assumption that this is the zone where they were registered
    */
  def HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit = {
    CullTargetsFromFirstHeap(targets, zone) match {
      case Nil => ;
      case list =>
        secondTask.cancel
        list.foreach { FirstJob }
        secondHeap =  secondHeap ++ list.map { RepackageEntry }
        import scala.concurrent.ExecutionContext.Implicits.global
        secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
    }
  }

  /**
    * Expedite all entries from the first pool into the second.
    */
  def HurryAll() : Unit = {
    firstTask.cancel
    firstHeap.foreach { FirstJob }
    secondHeap = secondHeap ++ firstHeap.map { RepackageEntry }
    firstHeap = Nil
    secondTask.cancel
    import scala.concurrent.ExecutionContext.Implicits.global
    secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
  }

  /**
    * Remove specific entries from the first pool.
    */
  def ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit = {
    CullTargetsFromFirstHeap(targets, zone)
  }

  /**
    * No entries in the first pool.
    */
  def ClearAll() : Unit = {
    firstTask.cancel
    firstHeap = Nil
  }

  /**
    * Retime an individual entry by recreating it.
    * @param entry an existing entry
    * @return a new entry, containing the same object and zone information;
    *         this new entry is always set to last for the duration of the second pool
    */
  private def RepackageEntry(entry : RemoverActor.Entry) : RemoverActor.Entry = {
    RemoverActor.Entry(entry.obj, entry.zone, SecondStandardDuration.toNanos)
  }

  /**
    * Search the first pool of entries awaiting removal processing.
    * If any entry has the same object as one of the targets and belongs to the same zone, remove it from the first pool.
    * If no targets are selected (an empty list), all discovered targets within the appropriate zone are removed.
    * @param targets a list of objects to pick
    * @param zone the zone in which these objects must be discovered;
    *             all targets must be in this zone, with the assumption that this is the zone where they were registered
    * @return all of the discovered entries
    */
  private def CullTargetsFromFirstHeap(targets : List[PlanetSideGameObject], zone : Zone) : List[RemoverActor.Entry] = {
    val culledEntries = if(targets.nonEmpty) {
      if(targets.size == 1) {
        log.debug(s"a target submitted: ${targets.head}")
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
        log.trace(s"multiple targets submitted: $targets")
        //cumbersome partition
        //a - find targets from entries
        val locatedTargets = for {
          a <- targets.map(RemoverActor.Entry(_, zone, 0))
          b <- firstHeap//.filter(entry => entry.zone == zone)
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
    }
    else {
      log.trace(s"all targets within the specified zone $zone will be submitted")
      //no specific targets; split on all targets in the given zone instead
      val (in, out) = firstHeap.partition(entry => entry.zone == zone)
      firstHeap = out.sortBy(_.duration)
      in
    }
    if(culledEntries.nonEmpty) {
      RetimeFirstTask()
      culledEntries
    }
    else {
      Nil
    }
  }

  /**
    * Common function to reset the first task's delayed execution.
    * Cancels the scheduled timer and will only restart the timer if there is at least one entry in the first pool.
    * @param now the time (in nanoseconds);
    *            defaults to the current time (in nanoseconds)
    */
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

  /**
    * Default time for entries waiting in the first list.
    * Override.
    * @return the time as a `FiniteDuration` object (to be later transformed into nanoseconds)
    */
  def FirstStandardDuration : FiniteDuration

  /**
    * Default time for entries waiting in the second list.
    * Override.
    * @return the time as a `FiniteDuration` object (to be later transformed into nanoseconds)
    */
  def SecondStandardDuration : FiniteDuration

  /**
    * Determine whether or not the resulting entry is valid for this removal process.
    * The primary purpose of this function should be to determine if the appropriate type of object is being submitted.
    * Override.
    * @param entry the entry
    * @return `true`, if it can be processed; `false`, otherwise
    */
  def InclusionTest(entry : RemoverActor.Entry) : Boolean

  /**
    * Performed when the entry is initially added to the first list.
    * Override.
    * @param entry the entry
    */
  def InitialJob(entry : RemoverActor.Entry) : Unit

  /**
    * Performed when the entry is shifted from the first list to the second list.
    * Override.
    * @param entry the entry
    */
  def FirstJob(entry : RemoverActor.Entry) : Unit

  /**
    * Performed to determine when an entry can be shifted off from the second list.
    * Override.
    * @param entry the entry
    */
  def ClearanceTest(entry : RemoverActor.Entry) : Boolean

  /**
    * The specific action that is necessary to complete the removal process.
    * Override.
    * @see `GUIDTask`
    * @param entry the entry
    */
  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask
}

object RemoverActor {
  /**
    * All information necessary to apply to the removal process to produce an effect.
    * Internally, all entries have a "time created" field.
    * @param obj the target
    * @param zone the zone in which this target is registered
    * @param duration how much longer the target will exist in its current state (in nanoseconds)
    */
  case class Entry(obj : PlanetSideGameObject, zone : Zone, duration : Long) {
    /** The time when this entry was created (in nanoseconds) */
    val time : Long = System.nanoTime
  }

  /**
    * A message that prompts the retrieval of a `TaskResolver` for us in the removal process.
    */
  case class Startup()

  /**
    * Message to submit an object to the removal process.
    * @see `FirstStandardDuration`
    * @param obj the target
    * @param zone the zone in which this target is registered
    * @param duration how much longer the target will exist in its current state (in nanoseconds);
    *                 a default time duration is provided by implementation
    */
  case class AddTask(obj : PlanetSideGameObject, zone : Zone, duration : Option[FiniteDuration] = None)

  /**
    * "Hurrying" shifts entries with the discovered objects (in the same `zone`)
    * through their first task and into the second pool.
    * If the list of targets is empty, all discovered objects in the given zone will be considered targets.
    * @param targets a list of objects to match
    * @param zone the zone in which these objects exist;
    *             the assumption is that all these target objects are registered to this zone
    */
  case class HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone)
  /**
    * "Hurrying" shifts all entries through their first task and into the second pool.
    */
  case class HurryAll()

  /**
    * "Clearing" cancels entries with the discovered objects (in the same `zone`)
    * if they are discovered in the first pool of objects.
    * Those entries will no longer be affected by any actions performed by the removal process until re-submitted.
    * If the list of targets is empty, all discovered objects in the given zone will be considered targets.
    * @param targets a list of objects to match
    * @param zone the zone in which these objects exist;
    *             the assumption is that all these target objects are registered to this zone
    */
  case class ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone)
  /**
    * "Clearing" cancels all entries if they are discovered in the first pool of objects.
    * Those entries will no longer be affected by any actions performed by the removal process until re-submitted.
    */
  case class ClearAll()

  /**
    * Message that indicates that the final stage of the remover process has failed.
    * Since the last step is generally unregistering the object, it could be a critical error.
    * @param entry the entry that was not properly removed
    * @param ex the reason the last entry was not properly removed
    */
  protected final case class FailureToWork(entry : RemoverActor.Entry, ex : Throwable)

  /**
    * Internal message to flag operations by data in the first list if it has been in that list long enough.
    */
  private final case class StartDelete()

  /**
    * Internal message to flag operations by data in the second list if it has been in that list long enough.
    */
  private final case class TryDelete()

  /**
    * Match two entries by object and by zone information.
    * @param entry1 the first entry
    * @param entry2 the second entry
    * @return if they match
    */
  private def Similarity(entry1 : RemoverActor.Entry, entry2 : RemoverActor.Entry) : Boolean = {
    entry1.obj == entry2.obj && entry1.zone == entry2.zone && entry1.obj.GUID == entry2.obj.GUID
  }

  /**
    * Get the index of an entry in the list of entries.
    * @param iter an `Iterator` of entries
    * @param target the specific entry to be found
    * @param index the incrementing index value
    * @return the index of the entry in the list, if a match to the target is found
    */
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
