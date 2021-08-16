// Copyright (c) 2021 PSForever
package net.psforever.objects.guid

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Parts of the task resolution lifecycle.
  */
sealed trait TaskBehaviors {
  /** What the task is supposed to accomplish. */
  def action(): Future[Any]
  /** A reversal of 'what the task is supposed to accomplish'. */
  def undo(): Unit
  /** Has the task been successfully completed? */
  def isSuccessful(): Boolean
  /** Describe this task's actions. */
  def description(): String = getClass.getSimpleName
}

/**
  * A primary unit of work in a workflow.
  */
trait Task
  extends TaskBehaviors {
  /** A careful determination if the task can be attempted.
    * @see `Task.action`
    */
  private[guid] def performAction(): Future[Any] = {
    if (!isSuccessful()) {
      action()
    } else {
      Future(Failure(new TaskNotExecutedException(task = this)))
    }
  }
  /** A careful determination if the task needs to be undone.
    * @see `Task.undo`
    */
  private[guid] def performUndo(): Unit = {
    if (isSuccessful()) undo() else ()
  }
}

/**
  * A primary unit of work in a workflow that is set up to execute and never be taken back.
  * Good for top-level tasking that only reports on the success of work carried out by subtasks.
  */
trait StraightforwardTask
  extends Task {
  def undo(): Unit = { /* blank */ }

  def isSuccessful(): Boolean = false /* always primed to be executed */
}

/**
  * The packaging of a more complicated unit of work in a workflow
  * in which one task relies on the successful completion of other tasks.
  * @param mainTask the primary task
  * @param subTasks tasks that are necessary to complete before starting on the primary one
  */
final case class TaskBundle(mainTask: Task, subTasks: Seq[TaskBundle])
  extends TaskBehaviors {
  /** Attempt 'what the [primary] task is supposed to accomplish'. */
  def action(): Future[Any] = mainTask.performAction()

  /** Attempt a reversal of what the all the connected tasks are 'supposed to accomplish'. */
  def undo() : Unit = {
    mainTask.performUndo()
    subTasks.foreach { _.undo() }
  }

  /** A hierarchical analysis of whether `the task been successfully completed`. */
  def isSuccessful(): Boolean = mainTask.isSuccessful() && subTasks.forall { _.isSuccessful() }

  override def description(): String = {
    val subCount: String = if (subTasks.nonEmpty) s" (${subTasks.size} subtasks)" else ""
    s"${mainTask.description()}$subCount"
  }
}

object TaskBundle {
  /**
    * The packaging of a unit of work in a workflow.
    * @param task the task
    */
  def apply(task: Task): TaskBundle = TaskBundle(task, List())
  /**
    * The packaging of a unit of work in a workflow
    * and a single task required to be completed first.
    * @param task the primary task
    * @param subTask the task that must be completed before the primary task
    */
  def apply(task: Task, subTask: Task): TaskBundle = TaskBundle(task, TaskBundle(subTask))
  /**
    * The packaging of a unit of work in a workflow
    * and the task(s) required to be completed first.
    * @param task the primary task
    * @param subTask the task(s) that must be completed before the primary task
    */
  def apply(task: Task, subTask: TaskBundle): TaskBundle = TaskBundle(task, Seq(subTask))
}

class TaskNotExecutedException(task: TaskBehaviors, msg: String) extends Exception(msg) {
  def this(task: Task) = {
    this(task, s"task '${task.description()}' was not successful")
  }

  def this(task: TaskBundle) = {
    this(task, s"task ${task.description()} was not successful")
  }
}

object TaskWorkflow {
  /**
    * The entry into the task workflow resolution process.
    * @param taskTree the packaged tasks that need to be completed
    * @return the anticipation of a task to be completed
    */
  def execute(taskTree: TaskBundle): Future[Any] = {
    evaluateTaskAndSubs(taskTree)
  }

  private def evaluateTaskAndSubs(task: TaskBundle): Future[Any] = {
    val promise = Promise[Any]()
    val (result, subResults) = composeTaskAndSubs(task)
    result.onComplete { _ =>
      if (matchOnFutureFailure(result)) {
        //every subtask that has already succeeded must be undone
        subResults
          .zip(task.subTasks)
          .collect { case (a, b) if matchOnFutureSuccess(a) => b }
          .foreach { _.undo() }
      }
      promise.completeWith(result)
    }
    promise.future
  }

  private def composeTaskAndSubs(task: TaskBundle): (Future[Any], Seq[Future[Any]]) = {
    val promise = Promise[Any]()
    val composedSubs = task.subTasks.map(evaluateTaskAndSubs)
    composedSubs match {
      case Nil =>
        //no subtasks; just execute the main task
        promise.completeWith(task.action())
      case list =>
        var unassignedCompletion: Boolean = true //shared mutex
        //wait for subtasks to complete
        list.foreach { result =>
          result.onComplete { _ =>
            unassignedCompletion.synchronized {
              if (unassignedCompletion && composedSubs.forall(matchOnFutureCompletion)) {
                unassignedCompletion = false
                if (composedSubs.forall(matchOnFutureSuccess)) {
                  //if all subtasks passed, execute the main task
                  promise.completeWith(task.action())
                } else {
                  //if some subtasks did not succeed, pass on wrapped failure
                  promise.completeWith(Future(Failure(new TaskNotExecutedException(task))))
                }
              }
            }
          }
        }
    }
    (promise.future, composedSubs)
  }

  /**
    * Does this anticipation of a task report having completed?
    * @param f the anticipation
    * @return whether it has been completed (passed or failed)
    */
  def matchOnFutureCompletion(f: Future[Any]): Boolean = {
    /*
    if 'matchOnFutureCompletion(FUTURE) == false' then 'matchOnFutureSuccess(FUTURE) == matchOnFutureFailure(FUTURE)'
    if 'matchOnFutureCompletion(FUTURE) == true'  then 'matchOnFutureSuccess(FUTURE) != matchOnFutureFailure(FUTURE)'
    */
    f.value match {
      case Some(_) => true
      case None    => false
    }
  }

  /**
    * Does this anticipation of a task report having succeeded?
    * The only true success is one where there is no `Failure` and no `Exception`.
    * @param f the anticipation
    * @return whether it has succeeded
    */
  def matchOnFutureSuccess(f: Future[Any]): Boolean = {
    f.value match {
      case Some(Success(_: Exception)) => false
      case Some(Success(Failure(_)))   => false
      case Some(Success(_))            => true
      case _                           => false
    }
  }

  /**
    * Does this anticipation of a task report having failed?
    * Having not yet completed does not count as a failure.
    * @param f the anticipation
    * @return whether it has failed
    */
  def matchOnFutureFailure(f: Future[Any]): Boolean = {
    f.value match {
      case Some(Failure(_))            => true
      case Some(Success(_: Exception)) => true
      case Some(Success(Failure(_)))   => true
      case _                           => false
    }
  }
}
