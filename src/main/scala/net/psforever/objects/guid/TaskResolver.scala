// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.routing.Broadcast
import net.psforever.objects.Default

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class TaskResolver() extends Actor {

  /** list of all work currently managed by this resolver */
  private val tasks: ListBuffer[TaskResolver.TaskEntry] = new ListBuffer[TaskResolver.TaskEntry]

  /** scheduled termination of tardy managed work */
  private var timeoutCleanup: Cancellable = Default.Cancellable

  /** logging utilities; default to tracing */
  private[this] val log          = org.log4s.getLogger
  private def trace(msg: String) = log.trace(msg)

  /**
    * Deal with any tasks that are still enqueued with this expiring `TaskResolver`.
    */
  override def aroundPostStop() = {
    /*
    First, eliminate all timed-out tasks.
    Secondly, deal with all tasks that have reported "success" but have not yet been handled.
    Finally, all other remaining tasks should be treated as if they had failed.
     */
    timeoutCleanup.cancel()
    TimeoutCleanup()
    tasks.filter(entry => entry.task.isComplete == Task.Resolution.Success).foreach(entry => OnSuccess(entry.task))
    val ex: Throwable = new Exception(s"a task is being stopped")
    tasks.foreach(entry => {
      OnFailure(entry.task, ex)
    })
    super.aroundPostStop()
  }

  def receive: Receive = {
    case TaskResolver.GiveTask(aTask, Nil) =>
      GiveTask(aTask)

    case TaskResolver.GiveTask(aTask, subtasks) =>
      QueueSubtasks(aTask, subtasks)

    case TaskResolver.GiveSubtask(aTask, subtasks, resolver) =>
      QueueSubtasks(aTask, subtasks, resolver)

    case TaskResolver.CompletedSubtask(obj) => //inter-resolver calls
      ExecuteNewTasks(obj)

    case Success(obj: Task) => //inter-resolver calls
      OnSuccess(obj)

    case Success | Success(_) => //success redirected from called event
      OnSuccess()

    case TaskResolver.Failure(obj, ex) => //inter-resolver calls
      OnFailure(obj, ex)

    case Failure(ex) => //failure redirected from called event
      OnFailure(ex)

    case TaskResolver.AbortTask(task, ex) =>
      OnAbort(task, ex)

    case TaskResolver.TimeoutCleanup() =>
      TimeoutCleanup()

    case msg =>
      log.warn(s"$self received an unexpected message $msg from ${sender()}")
  }

  /**
    * Accept simple work and perform it.
    * @param aTask the work to be completed
    */
  private def GiveTask(aTask: Task): Unit = {
    val entry: TaskResolver.TaskEntry = TaskResolver.TaskEntry(aTask)
    tasks += entry
    trace(s"enqueue and start task ${aTask.Description}")
    entry.Execute(self)
    StartTimeoutCheck()
  }

  /**
    * Start the periodic checks for a task that has run for too long (timed-out), unless those checks are already running.
    */
  private def StartTimeoutCheck(): Unit = {
    if (timeoutCleanup.isCancelled) {
      timeoutCleanup = context.system.scheduler.scheduleWithFixedDelay(
        500 milliseconds,
        500 milliseconds,
        self,
        TaskResolver.TimeoutCleanup()
      )
    }
  }

  /**
    * Accept complicated work and divide it into a main task and tasks that must be handled before the main task.
    * Do not start the main task until all of the aforementioned "sub-tasks" are completed.<br>
    * <br>
    * Sub-tasks can be nested many times.
    * All immediate sub-tasks count as the primary sub-tasks for the current main task.
    * Each pair of main task and sub-tasks, for every sub-task discovered, is passed on to another `TaskResolver` for completion.
    * The parent of this `TaskResolver` is the router logic for all brethren `TaskResolver` `Actors`.
    * @param task the work to be completed
    * @param subtasks other work that needs to be completed first
    * @param resolver the `TaskResolver` that distributed this work, thus determining that this work is a sub-task;
    *                 by default, no one, as the work is identified as a main task
    */
  private def QueueSubtasks(
      task: Task,
      subtasks: List[TaskResolver.GiveTask],
      resolver: ActorRef = ActorRef.noSender
  ): Unit = {
    val entry: TaskResolver.TaskEntry = TaskResolver.TaskEntry(task, subtasks.map(task => task.task), resolver)
    tasks += entry
    trace(s"enqueue task ${task.Description}")
    if (subtasks.isEmpty) { //a leaf in terms of task dependency; so, not dependent on any other work
      trace(s"start task ${task.Description}")
      entry.Execute(self)
    } else {
      trace(s"enqueuing ${subtasks.length} substask(s) belonging to ${task.Description}")
      subtasks.foreach({ subtask =>
        context.parent ! TaskResolver.GiveSubtask(
          subtask.task,
          subtask.subs,
          self
        ) //route back to submit subtask to pool
      })
    }
    StartTimeoutCheck()
  }

  /**
    * Perform these checks when a task has reported successful completion to this TaskResolver.
    * Every task and subtask will be checked, starting from the end of the list of queued entries
    * and only the first discovered one will be used.
    */
  private def OnSuccess(): Unit = {
    //by reversing the List, we find the most outstanding Task with the completion state
    TaskResolver.filterCompletion(tasks.indices.reverseIterator, tasks.toList, Task.Resolution.Success) match {
      case Some(index) =>
        GeneralOnSuccess(index)
      case None => ;
    }
  }

  /**
    * Perform these checks when a task has reported successful completion to this TaskResolver.
    * @param task a `Task` object
    */
  private def OnSuccess(task: Task): Unit = {
    //find specific task and dequeue
    TaskResolver.findTask(tasks.iterator, task) match {
      case Some(index) =>
        GeneralOnSuccess(index)
      case None => ;
    }
  }

  /**
    * Perform these checks when a task has reported successful completion to this TaskResolver.
    * This is what actually happens upon completion.
    * @param index the `TaskEntry` index
    */
  private def GeneralOnSuccess(index: Int): Unit = {
    val entry = tasks(index)
    entry.task.onSuccess()
    trace(s"success with task ${entry.task.Description}")
    if (entry.supertaskRef != ActorRef.noSender) {
      entry.supertaskRef ! TaskResolver.CompletedSubtask(
        entry.task
      ) //alert our dependent task's resolver that we have completed
    }
    TaskCleanup(index)
  }

  /**
    * Scan across a group of sub-tasks and determine if the associated main `Task` may execute.
    * All of the sub-tasks must report a `Success` completion status before the main work can begin.
    * @param subtask a `Task` that is a subtask of some parent task in this resolver's group
    */
  private def ExecuteNewTasks(subtask: Task): Unit = {
    TaskResolver.findTaskWithSubtask(tasks.iterator, subtask) match {
      case Some(index) =>
        val entry = tasks(index)
        if (TaskResolver.filterCompletionMatch(entry.subtasks.iterator, Task.Resolution.Success)) {
          trace(s"start new task ${entry.task.Description}")
          entry.Execute(self)
          StartTimeoutCheck()
        }
      case None => ;
    }
  }

  /**
    * Perform these checks when a task has reported failure to this `TaskResolver`.
    * Since the `Failure(Throwable)` can not be associated with a specific task,
    * every task and subtask will be checked, starting from the end of the list of queued entries,
    * and only the first discovered one will be used.
    * Consequently, the specific `Throwable` that contains the error message may have nothing to do with the failed task.
    * @param ex a `Throwable` that reports what happened to the task
    */
  private def OnFailure(ex: Throwable): Unit = {
    //by reversing the List, we find the most outstanding Task with the completion state
    TaskResolver.filterCompletion(tasks.indices.reverseIterator, tasks.toList, Task.Resolution.Failure) match {
      case Some(index) =>
        GeneralOnFailure(index, ex)
      case None => ;
    }
  }

  /**
    * Perform these checks when a task has reported failure to this `TaskResolver`.
    * @param subtask the task that had reported failure from some other resolver
    * @param ex a `Throwable` that reports what happened to the task
    */
  private def OnFailure(subtask: Task, ex: Throwable): Unit = {
    TaskResolver.findTaskWithSubtask(tasks.iterator, subtask) match {
      case Some(index) =>
        GeneralOnFailure(index, ex)
      case None => ;
    }
  }

  /**
    * Perform these checks when a task has reported failure to this `TaskResolver`.
    * This is what actually happens upon completion.
    * @param index the `TaskEntry` index
    * @param ex a `Throwable` that reports what happened to the task
    */
  private def GeneralOnFailure(index: Int, ex: Throwable): Unit = {
    val entry = tasks(index)
    val task  = entry.task
    trace(s"failure with task ${task.Description}")
    task.onAbort(ex)
    task.onFailure(ex)
    if (entry.supertaskRef != ActorRef.noSender) {
      entry.supertaskRef ! TaskResolver.Failure(task, ex) //alert our superior task's resolver we have completed
    }
    FaultSubtasks(entry)
    TaskCleanup(index)
  }

  /**
    * Instruct all subtasks of a given `Task` to fail.
    * @param entry the target parent entry (that has failed)
    */
  private def FaultSubtasks(entry: TaskResolver.TaskEntry): Unit = {
    val ex: Throwable = new Exception(s"a task ${entry.task} had a subtask that failed")
    entry.subtasks.foreach(subtask => {
      context.parent ! Broadcast(TaskResolver.Failure(subtask, ex)) //we have no clue where this subtask was hosted
    })
  }

  /**
    * If a specific `Task` is governed by this `TaskResolver`, find its index and dispose of it and its known sub-tasks.
    * @param task the work to be found
    * @param ex a `Throwable` that reports what happened to the work
    */
  private def OnAbort(task: Task, ex: Throwable): Unit = {
    TaskResolver.findTask(tasks.iterator, task) match {
      case Some(index) =>
        PropagateAbort(index, ex)
        TaskCleanup(index)
      case None => ;
    }
  }

  /**
    * If a specific `Task` is governed by this `TaskResolver`, dispose of it and its known sub-tasks.
    * @param index the index of the discovered work
    * @param ex a `Throwable` that reports what happened to the work
    */
  private def PropagateAbort(index: Int, ex: Throwable): Unit = {
    tasks(index).subtasks.foreach({ subtask =>
      if (subtask.isComplete == Task.Resolution.Success) {
        trace(s"aborting task ${subtask.Description}")
        subtask.onAbort(ex)
      }
      context.parent ! Broadcast(TaskResolver.AbortTask(subtask, ex))
    })
  }

  /**
    * Find all tasks that have been running for too long and declare them as timed-out.
    * Run periodically, as long as work is being performed.
    */
  private def TimeoutCleanup(): Unit = {
    TaskResolver
      .filterTimeout(tasks.indices.reverseIterator, tasks.toList, Task.TimeNow)
      .foreach({ index =>
        val ex: Throwable = new TimeoutException(s"a task ${tasks(index).task} has timed out")
        tasks(index).task.onTimeout(ex)
        PropagateAbort(index, ex)
      })
  }

  /**
    * Remove a `Task` that has reported completion.
    * @param index an index of work in the `List` of `Task`s
    */
  private def TaskCleanup(index: Int): Unit = {
    tasks(index).task.Cleanup()
    tasks.remove(index)
    if (tasks.isEmpty) {
      timeoutCleanup.cancel()
    }
  }
}

object TaskResolver {

  /**
    * Give this `TaskResolver` simple work to be performed.
    * @param task the work to be completed
    * @param subs other work that needs to be completed first
    */
  final case class GiveTask(task: Task, subs: List[GiveTask] = Nil)

  /**
    * Pass around complex work to be performed.
    * @param task the work to be completed
    * @param subs other work that needs to be completed first
    * @param resolver the `TaskResolver` that will handle work that depends on the outcome of this work
    */
  private final case class GiveSubtask(task: Task, subs: List[GiveTask], resolver: ActorRef)

  /**
    * Run a scheduled timed-out `Task` check.
    */
  private final case class TimeoutCleanup()

  /**
    * A specific kind of `Failure` that reports on which specific `Task` has reported failure.
    * @param obj a task object
    * @param ex information about what went wrong
    */
  private final case class Failure(obj: Task, ex: Throwable)

  /**
    * A specific kind of `Success` that reports on which specific `Task` has reported Success where that `Task` was some other `Task`'s subtask.
    * @param obj a task object
    */
  private final case class CompletedSubtask(obj: Task)

  /**
    * A `Broadcast` message designed to find and remove a particular task from this series of routed `Actors`.
    * @param task the work to be removed
    * @param ex an explanation why the work is being aborted
    */
  private final case class AbortTask(task: Task, ex: Throwable)

  /**
    * Storage unit for a specific unit of work, plus extra information.
    * @param task the work to be completed
    * @param subtasks other work that needs to be completed first
    * //@param isASubtask whether this work is intermediary or the last in a dependency chain
    * @param supertaskRef the `TaskResolver` that will handle work that depends on the outcome of this work
    */
  private final case class TaskEntry(
      task: Task,
      subtasks: List[Task] = Nil,
      supertaskRef: ActorRef = ActorRef.noSender
  ) {
    private var start: Long          = 0L
    private var isExecuting: Boolean = false

    def Start: Long = start

    def Executing: Boolean = isExecuting

    /**
      * Only execute each task once.
      * @param ref the `TaskResolver` currently handling this `Task`/`TaskEntry`
      */
    def Execute(ref: ActorRef): Unit = {
      if (!isExecuting) {
        isExecuting = true
        start = Task.TimeNow
        task.Execute(ref)
      }
    }
  }

  /**
    * Scan across a group of tasks to determine which ones match the target completion status.
    * @param iter an `Iterator` of enqueued `TaskEntry` indices
    * @param resolution the target completion status
    * @return the first valid index when `TaskEntry` has its primary `Task` matching the completion status
    */
  @tailrec private def filterCompletion(
      iter: Iterator[Int],
      tasks: List[TaskEntry],
      resolution: Task.Resolution.Value
  ): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val index: Int = iter.next()
      if (tasks(index).task.isComplete == resolution) {
        Some(index)
      } else {
        filterCompletion(iter, tasks, resolution)
      }
    }
  }

  /**
    * Scan across a group of sub-tasks to determine if they all match the target completion status.
    * @param iter an `Iterator` of enqueued sub-tasks
    * @param resolution the target completion status
    * @return `true`, if all tasks match the complete status;
    *        `false`, otherwise
    */
  @tailrec private def filterCompletionMatch(iter: Iterator[Task], resolution: Task.Resolution.Value): Boolean = {
    if (!iter.hasNext) {
      true
    } else {
      if (iter.next().isComplete == resolution) {
        filterCompletionMatch(iter, resolution)
      } else {
        false
      }
    }
  }

  /**
    * Find the indices of all enqueued work that has timed-out.
    * @param iter an `Iterator` of enqueued `TaskEntry` indices
    * @param now the current time in milliseconds
    * @param indexList a persistent `List` of indices
    * @return the `List` of all valid `Task` indices
    */
  @tailrec private def filterTimeout(
      iter: Iterator[Int],
      tasks: List[TaskEntry],
      now: Long,
      indexList: List[Int] = Nil
  ): List[Int] = {
    if (!iter.hasNext) {
      indexList
    } else {
      val index: Int = iter.next()
      val taskEntry  = tasks(index)
      if (
        taskEntry.Executing && taskEntry.task.isComplete == Task.Resolution.Incomplete && now - taskEntry.Start > taskEntry.task.Timeout
      ) {
        filterTimeout(iter, tasks, now, indexList :+ index)
      } else {
        filterTimeout(iter, tasks, now, indexList)
      }
    }
  }

  /**
    * Find the index of the targeted `Task`, if it is enqueued here.
    * @param iter an `Iterator` of entries
    * @param target a target `Task`
    * @param index the current index in the aforementioned `List`;
    *              defaults to 0
    * @return the index of the discovered task, or `None`
    */
  @tailrec private def findTask(iter: Iterator[TaskEntry], target: Task, index: Int = 0): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      if (iter.next().task == target) {
        Some(index)
      } else {
        findTask(iter, target, index + 1)
      }
    }
  }

  /**
    * Find the index of the targeted `Task`, if it is enqueued here, given a specific "subtask" of that `Task`.
    * @param iter an `Iterator` of entries
    * @param target a target subtask
    * @param index the current index in the aforementioned `List`;
    *              defaults to 0
    * @return the index of the discovered task, or `None`
    */
  @tailrec private def findTaskWithSubtask(iter: Iterator[TaskEntry], target: Task, index: Int = 0): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val tEntry = iter.next()
      if (tEntry.subtasks.contains(target)) {
        Some(index)
      } else {
        findTaskWithSubtask(iter, target, index + 1)
      }
    }
  }
}
