// Copyright (c) 2017 PSForever
package services

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.zones.Zone
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject}
import net.psforever.types.Vector3
import services.support.{SimilarityComparator, SupportActor, SupportActorCaseConversions}

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
abstract class RemoverActor extends SupportActor[RemoverActor.Entry] {
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

  protected var taskResolver : ActorRef = Actor.noSender

  val sameEntryComparator = new SimilarityComparator[RemoverActor.Entry]() {
    def Test(entry1 : RemoverActor.Entry, entry2 : RemoverActor.Entry) : Boolean = {
      entry1.obj == entry2.obj && entry1.zone == entry2.zone && entry1.obj.GUID == entry2.obj.GUID
    }
  }

  /**
    * Send the initial message that requests a task resolver for assisting in the removal process.
    */
  override def preStart() : Unit = {
    super.preStart()
    self ! Service.Startup()
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
    case Service.Startup() =>
      ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case msg =>
      debug(s"received message $msg before being properly initialized")
  }

  def Processing : Receive = entryManagementBehaviors
    .orElse {
      case RemoverActor.AddTask(obj, zone, duration) =>
        val entry = RemoverActor.Entry(obj, zone, duration.getOrElse(FirstStandardDuration).toNanos)
        if(InclusionTest(entry) && !secondHeap.exists(test => sameEntryComparator.Test(test, entry) )) {
          InitialJob(entry)
          if(entry.duration == 0) {
            //skip the first queue altogether
            FirstJob(entry)
            secondHeap = secondHeap ++ List(RepackageEntry(entry))
            if(secondHeap.size == 1) {
              import scala.concurrent.ExecutionContext.Implicits.global
              secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
            }
          }
          else if(firstHeap.isEmpty) {
            //we were the only entry so the event must be started from scratch
            firstHeap = List(entry)
            trace(s"a remover task has been added: $entry")
            RetimeFirstTask()
          }
          else {
            //unknown number of entries; append, sort, then re-time tasking
            val oldHead = firstHeap.head
            if(!firstHeap.exists(test => sameEntryComparator.Test(test, entry))) {
              firstHeap = (firstHeap :+ entry).sortBy(entry => entry.time + entry.duration)
              trace(s"a remover task has been added: $entry")
              if(oldHead != firstHeap.head) {
                RetimeFirstTask()
              }
            }
            else {
              trace(s"$obj is already queued for removal")
            }
          }
        }
        else {
          trace(s"$obj either does not qualify for this Remover or is already queued")
        }

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
        trace(s"item removal task has found ${in.size} items to remove")

      case RemoverActor.TryDelete() =>
        secondTask.cancel
        val (in, out) = secondHeap.partition { ClearanceTest }
        secondHeap = out
        in.foreach { SecondJob }
        if(out.nonEmpty) {
          import scala.concurrent.ExecutionContext.Implicits.global
          secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
        }
        trace(s"item removal task has removed ${in.size} items")

      case RemoverActor.FailureToWork(entry, ex) =>
        debug(s"${entry.obj} from ${entry.zone} not properly deleted - $ex")

      case _ => ;
    }

  /**
    * Expedite some entries from the first pool into the second.
    * @param targets a list of objects to pick
    * @param zone the zone in which these objects must be discovered;
    *             all targets must be in this zone, with the assumption that this is the zone where they were registered
    */
  def HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit = {
    PartitionTargetsFromList(firstHeap, targets.map { RemoverActor.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been hurried")
      case (in, out) =>
        debug(s"the following tasks have been hurried: $in")
        firstHeap = out //.sortBy(entry => entry.time + entry.duration)
        if(out.nonEmpty) {
          RetimeFirstTask()
        }
        secondTask.cancel
        in.foreach { FirstJob }
        secondHeap = secondHeap ++ in.map { RepackageEntry }
        import scala.concurrent.ExecutionContext.Implicits.global
        secondTask = context.system.scheduler.scheduleOnce(SecondStandardDuration, self, RemoverActor.TryDelete())
    }
  }

  /**
    * Expedite all entries from the first pool into the second.
    */
  def HurryAll() : Unit = {
    trace("all tasks have been hurried")
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
    PartitionTargetsFromList(firstHeap, targets.map { RemoverActor.Entry(_, zone, 0) }, zone) match {
      case (Nil, _) =>
        debug(s"no tasks matching the targets $targets have been cleared")
      case (in, out) =>
        debug(s"the following tasks have been cleared: $in")
        firstHeap = out //.sortBy(entry => entry.time + entry.duration)
        if(out.nonEmpty) {
          RetimeFirstTask()
        }
    }
  }

  /**
    * No entries in the first pool.
    */
  def ClearAll() : Unit = {
    trace("all tasks have been cleared")
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

object RemoverActor extends SupportActorCaseConversions {
  /**
    * All information necessary to apply to the removal process to produce an effect.
    * Internally, all entries have a "time created" field.
    * @param _obj the target
    * @param _zone the zone in which this target is registered
    * @param _duration how much longer the target will exist in its current state (in nanoseconds)
    */
  case class Entry(_obj : PlanetSideGameObject, _zone : Zone, _duration : Long) extends SupportActor.Entry(_obj, _zone, _duration)

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
}
