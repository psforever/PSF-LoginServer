// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.routing.Broadcast

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class TaskResolver() extends Actor {
  /** list of all work currently managed by this TaskResolver */
  private val tasks : ListBuffer[TaskResolver.TaskEntry] = new ListBuffer[TaskResolver.TaskEntry]
  /** scheduled examination of all managed work */
  private var timeoutCleanup : Cancellable = TaskResolver.DefaultCancellable
  //private[this] val log = org.log4s.getLogger

  /**
    * Deal with any tasks that are still enqueued with this expiring `TaskResolver`.<br>
    * <br>
    * First, eliminate all timed-out tasks.
    * Secondly, deal with all tasks that have reported "success" but have not yet been handled.
    * Finally, all other remaining tasks should be treated as if they had failed.
    */
  override def aroundPostStop() = {
    super.aroundPostStop()

    timeoutCleanup.cancel()
    TimeoutCleanup()
    OnSuccess()
    val ex : Throwable = new Exception(s"a task is being stopped")
    OnFailure(ex)
    tasks.indices.foreach({index =>
      val entry = tasks(index)
      PropagateAbort(index, ex)
      if(entry.isASubtask) {
        entry.supertaskRef ! Failure(ex) //alert our superior task's resolver we have completed
      }
    })
  }

  def receive : Receive = {
    case TaskResolver.GiveTask(aTask, Nil) =>
      GiveTask(aTask)

    case TaskResolver.GiveTask(aTask, subtasks) =>
      QueueSubtasks(aTask, subtasks)

    case TaskResolver.GiveSubtask(aTask, subtasks, resolver) =>
      QueueSubtasks(aTask, subtasks, true, resolver)

    case TaskResolver.CompletedSubtask() =>
      ExecuteNewTasks()

    case Success(_) => //ignore the contents as unreliable
      OnSuccess()

    case Failure(ex) =>
      OnFailure(ex)

    case TaskResolver.AbortTask(task, ex) =>
      OnAbort(task, ex)

    case TaskResolver.TimeoutCleanup() =>
      TimeoutCleanup()

    case _ => ;
  }

  /**
    * Accept simple work and perform it.
    * @param aTask the work to be completed
    */
  private def GiveTask(aTask : Task) : Unit = {
    val entry : TaskResolver.TaskEntry = TaskResolver.TaskEntry(aTask)
    tasks += entry
    entry.Execute(self) //send this Actor; aesthetically pleasant expression
    StartTimeoutCheck()
  }

  /**
    * Start the periodic checks for a task that has run for too long (timed-out), unless those checks are already running.
    */
  private def StartTimeoutCheck() : Unit = {
    if(timeoutCleanup.isCancelled) {
      timeoutCleanup = context.system.scheduler.schedule(500 milliseconds, 500 milliseconds, self, TaskResolver.TimeoutCleanup())
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
    * @param isSubTask `true`, if this task counts as internal or as a leaf in the chain of `Task` dependency;
    *                 `false`, by default, if we are the top of the chain fo dependency
    * @param resolver the `TaskResolver` that distributed this work, thus determining that this work is a sub-task;
    *                 by default, no one, as the work is identified as a main task
    */
  private def QueueSubtasks(task : Task, subtasks : List[TaskResolver.GiveTask], isSubTask : Boolean = false, resolver : ActorRef = Actor.noSender) : Unit = {
    val sublist : List[Task] = subtasks.map(task => task.task)
    val entry : TaskResolver.TaskEntry = TaskResolver.TaskEntry(task, sublist, isSubTask, resolver)
    tasks += entry
    if(sublist.isEmpty) { //a leaf in terms of task dependency; so, not dependent on any other work
      entry.Execute(self)
    }
    else {
      subtasks.foreach({subtask =>
        context.parent ! TaskResolver.GiveSubtask(subtask.task, subtask.subs, self) //route back to submit subtask to pool
      })
    }
    StartTimeoutCheck()
  }

  /**
    * Perform these checks when a task has reported successful completion to this TaskResolver.
    * Since the `Success(_)` can not be associated with a specific task, every task and subtask will be checked.
    */
  private def OnSuccess(): Unit = {
    //by reversing the List, we can remove TaskEntries without disrupting the order
    TaskResolver.filterCompletion(tasks.indices.reverseIterator, tasks.toList, Task.Resolution.Success).foreach({index =>
      val entry = tasks(index)
      entry.task.onSuccess()
      if(entry.isASubtask) {
        entry.supertaskRef ! TaskResolver.CompletedSubtask() //alert our dependent task's resolver that we have completed
      }
      TaskCleanup(index)
    })
  }

  /**
    * Scan across a group of sub-tasks and determine if the associated main `Task` may execute.
    * All of the sub-tasks must report a `Success` completion status before the main work can begin.
    */
  private def ExecuteNewTasks() : Unit = {
    tasks.filter({taskEntry => taskEntry.subtasks.nonEmpty}).foreach(entry => {
      if(TaskResolver.filterCompletionMatch(entry.subtasks.iterator, Task.Resolution.Success)) {
        entry.Execute(self)
        StartTimeoutCheck()
      }
    })
  }

  /**
    * Perform these checks when a task has reported failure to this TaskResolver.
    * Since the `Failure(Throwable)` can not be associated with a specific task, every task and subtask will be checked.
    * Consequently, the specific `Throwable` that contains the error message may have nothing to do with the failed task.
    * @param ex a `Throwable` that reports what happened to the task
    */
  private def OnFailure(ex : Throwable) : Unit = {
    TaskResolver.filterCompletion(tasks.indices.reverseIterator, tasks.toList, Task.Resolution.Failure).foreach({index =>
      val entry = tasks(index)
      PropagateAbort(index, ex)
      entry.task.onFailure(ex) //TODO let the error be disjoint?
      if(entry.isASubtask) {
        entry.supertaskRef ! Failure(ex) //alert our superior task's resolver we have completed
      }
    })
    FaultSubtasks()
  }

  /**
    * Scan across a group of sub-tasks and, if any have reported `Failure`, report to the main `Task` that it should fail as well.
    */
  private def FaultSubtasks() : Unit = {
    tasks.indices.filter({index => tasks(index).subtasks.nonEmpty}).reverse.foreach(index => {
      val entry = tasks(index)
      if(TaskResolver.filterCompletionMatch(entry.subtasks.iterator, Task.Resolution.Failure)) {
        val ex : Throwable = new Exception(s"a task ${entry.task} had a subtask that failed")
        entry.task.onFailure(ex)
        if(entry.isASubtask) {
          entry.supertaskRef ! Failure(ex) //alert our superior task's resolver we have completed
        }
        TaskCleanup(index)
      }
    })
  }

  /**
    * If a specific `Task` is governed by this `TaskResolver`, find its index and dispose of it and its known sub-tasks.
    * @param task the work to be found
    * @param ex a `Throwable` that reports what happened to the work
    */
  private def OnAbort(task : Task, ex : Throwable) : Unit = {
    TaskResolver.findTaskIndex(tasks.iterator, task) match {
      case Some(index) =>
        PropagateAbort(index, ex)
      case None => ;
    }
  }

  /**
    * If a specific `Task` is governed by this `TaskResolver`, dispose of it and its known sub-tasks.
    * @param index the index of the discovered work
    * @param ex a `Throwable` that reports what happened to the work
    */
  private def PropagateAbort(index : Int, ex : Throwable) : Unit = {
    tasks(index).subtasks.foreach({subtask =>
      if(subtask.isComplete == Task.Resolution.Success) {
        subtask.onAbort(ex)
      }
      context.parent ! Broadcast(TaskResolver.AbortTask(subtask, ex))
    })
    TaskCleanup(index)
  }

  /**
    * Find all tasks that have been running for too long and declare them as timed-out.
    * Run periodically, as long as work is being performed.
    */
  private def TimeoutCleanup() : Unit = {
    TaskResolver.filterTimeout(tasks.indices.reverseIterator, tasks.toList, Task.TimeNow).foreach({index =>
      val ex : Throwable = new TimeoutException(s"a task ${tasks(index).task} has timed out")
      tasks(index).task.onTimeout(ex)
      PropagateAbort(index, ex)
    })
  }

  /**
    * Remove a `Task` that has reported completion.
    * @param index an index of work in the `List` of `Task`s
    */
  private def TaskCleanup(index : Int) : Unit = {
    tasks(index).task.Cleanup()
    tasks.remove(index)
    if(tasks.isEmpty) {
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
  final case class GiveTask(task : Task, subs : List[GiveTask] = Nil)

  /**
    * Pass around complex work to be performed.
    * @param task the work to be completed
    * @param subs other work that needs to be completed first
    * @param resolver the `TaskResolver` that will handle work that depends on the outcome of this work
    */
  private final case class GiveSubtask(task : Task, subs : List[GiveTask], resolver : ActorRef)

  /**
    * Run a scheduled timed-out `Task` check.
    */
  private final case class TimeoutCleanup()

  /**
    *
    */
  private final case class CompletedSubtask()

  /**
    * A `Broadcast` message designed to find and remove a particular task from this series of routed `Actors`.
    * @param task the work to be removed
    * @param ex an explanation why the work is being aborted
    */
  private final case class AbortTask(task : Task, ex : Throwable)

  /**
    * Storage unit for a specific unit of work, plus extra information.
    * @param task the work to be completed
    * @param subtasks other work that needs to be completed first
    * @param isASubtask whether this work is intermediary or the last in a dependency chain
    * @param supertaskRef the `TaskResolver` that will handle work that depends on the outcome of this work
    */
  private final case class TaskEntry(task : Task, subtasks : List[Task] = Nil, isASubtask : Boolean = false, supertaskRef : ActorRef = Actor.noSender) {
    private var start : Long = 0L
    private var isExecuting : Boolean = false

    def Start : Long = start

    def Executing : Boolean = isExecuting

    def Execute(ref : ActorRef) : Unit = {
      if(!isExecuting) {
        start = Task.TimeNow
        isExecuting = true
        task.Execute(ref)
      }
    }
  }

  /**
    * A placeholder `Cancellable` object for the time-out checking functionality.
    */
  private final val DefaultCancellable = new Cancellable() {
    def cancel : Boolean = true
    def isCancelled() : Boolean = true
  }

  /**
    * Find the index of the targeted `Task`, if it is enqueued here.
    * @param iter an `Iterator` of
    * @param task a target `Task`
    * @param index the current index in the aforementioned `List`;
    *              defaults to 0
    * @return the index of the discovered task, or `None`
    */
  @tailrec private def findTaskIndex(iter : Iterator[TaskResolver.TaskEntry], task : Task, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      if(iter.next.task == task) {
        Some(index)
      }
      else {
        findTaskIndex(iter, task, index + 1)
      }
    }
  }

  /**
    * Scan across a group of tasks to determine which ones match the target completion status.
    * @param iter an `Iterator` of enqueued `TaskEntry` indices
    * @param resolution the target completion status
    * @param indexList a persistent `List` of indices
    * @return the `List` of all valid `Task` indices
    */
  @tailrec private def filterCompletion(iter : Iterator[Int], tasks : List[TaskEntry], resolution : Task.Resolution.Value, indexList : List[Int] = Nil) : List[Int] = {
    if(!iter.hasNext) {
      indexList
    }
    else {
      val index : Int = iter.next
      if(tasks(index).task.isComplete == resolution) {
        filterCompletion(iter, tasks, resolution, indexList :+ index)
      }
      else {
        filterCompletion(iter, tasks, resolution, indexList)
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
  @tailrec private def filterCompletionMatch(iter : Iterator[Task], resolution : Task.Resolution.Value) : Boolean = {
    if(!iter.hasNext) {
      true
    }
    else {
      if(iter.next.isComplete == resolution) {
        filterCompletionMatch(iter, resolution)
      }
      else {
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
  @tailrec private def filterTimeout(iter : Iterator[Int], tasks : List[TaskEntry], now : Long, indexList : List[Int] = Nil) : List[Int] = {
    if(!iter.hasNext) {
      indexList
    }
    else {
      val index : Int = iter.next
      val taskEntry = tasks(index)
      if(taskEntry.Executing && taskEntry.task.isComplete == Task.Resolution.Incomplete && now - taskEntry.Start > taskEntry.task.Timeout) {
        filterTimeout(iter, tasks, now, indexList :+ index)
      }
      else {
        filterTimeout(iter, tasks, now, indexList)
      }
    }
  }
}
