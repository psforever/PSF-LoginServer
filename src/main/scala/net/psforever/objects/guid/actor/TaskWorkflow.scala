// Copyright (c) 2021 PSForever
package net.psforever.objects.guid.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

sealed trait TaskBehaviors {
  def action() : Future[Any]
  def undo() : Unit
  def isSuccessful() : Boolean
  def description(): String = getClass.getSimpleName
}

trait Task
  extends TaskBehaviors {
  private[actor] def performAction(): Future[Any] = {
    if (!isSuccessful()) {
      action()
    } else {
      Future(Failure(new TaskNotExecutedException(task = this)))
    }
  }
  def action(): Future[Any]

  private[actor] def performUndo(): Unit = {
    if (isSuccessful()) undo() else ()
  }
  def undo(): Unit
}

final case class TaskBundle(mainTask: Task, subTasks: Seq[TaskBundle])
  extends TaskBehaviors {
  def action(): Future[Any] = mainTask.performAction()

  def undo() : Unit = {
    mainTask.performUndo()
    subTasks.foreach { _.undo() }
  }

  def isSuccessful(): Boolean = mainTask.isSuccessful() && subTasks.forall { _.isSuccessful() }

  override def description(): String = {
    val subCount: String = if (subTasks.nonEmpty) s" (${subTasks.size} subtasks)" else ""
    s"${mainTask.description()}$subCount"
  }
}

object TaskBundle {
  def apply(task: Task): TaskBundle = TaskBundle(task, List())

  def apply(task: Task, subTask: Task): TaskBundle = TaskBundle(task, TaskBundle(subTask))

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
  def execute(taskTree: TaskBundle): Future[Any] = {
    evaluateTaskAndSubs(taskTree)
  }

  private def evaluateTaskAndSubs(task: TaskBundle): Future[Any] = {
    val promise = Promise[Any]()
    val (result, subResults) = composeTaskAndSubs(task)
    result.onComplete {
      _ =>
        if (matchOnFutureFailure(result)) {
          //every subtask that has already succeeded must be undone
          subResults
            .zip(task.subTasks)
            .collect { case (a, b) if matchOnFutureSuccess(a) => b }
            .foreach { _.undo() }
        }
        promise completeWith result
    }
    promise.future
  }

  private def composeTaskAndSubs(task: TaskBundle): (Future[Any], Seq[Future[Any]]) = {
    val promise = Promise[Any]()
    val composedSubs = task.subTasks.map { evaluateTaskAndSubs }
    composedSubs match {
      case Nil =>
        //no subtasks; just execute the main task
        promise completeWith task.action()
      case list =>
        var unassignedCompletion : Boolean = true //mutex
        //wait for subtasks to complete
        list.foreach { result =>
          result.onComplete {
            _ =>
              unassignedCompletion.synchronized {
                if (unassignedCompletion) {
                  if (composedSubs.forall { matchOnFutureCompletion }) {
                    unassignedCompletion = false
                    if (composedSubs.forall { matchOnFutureSuccess }) {
                      //if all subtasks passed, execute main task
                      promise completeWith task.action()
                    }
                    else {
                      //if some subtasks did not succeed, pass on failure
                      promise completeWith Future(Failure(new TaskNotExecutedException(task)))
                    }
                  }
                }
              }
          }
        }
    }
    (promise.future, composedSubs)
  }

  def matchOnFutureSuccess(f: Future[Any]): Boolean = {
    f.value match {
      case Some(Success(_: Exception)) => false
      case Some(Success(Failure(_)))   => false
      case Some(Success(_))            => true //only a non-exception success counts as a true success
      case _                           => false
    }
  }

  def matchOnFutureFailure(f: Future[Any]): Boolean = {
    f.value match {
      case Some(Failure(_))            => true
      case Some(Success(_: Exception)) => true
      case Some(Success(Failure(_)))   => true
      case _                           => false //not yet completed does not count as a failure
    }
  }

  def matchOnFutureCompletion(f: Future[Any]): Boolean = {
    f.value match {
      case Some(_) => true
      case None    => false
    }
  }
}
