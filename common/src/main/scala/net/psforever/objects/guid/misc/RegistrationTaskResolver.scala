// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.misc

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.entity.IdentifiableEntity

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Accept a task in waiting and series of lesser tasks that complete the provided primary task.
  * Receive periodic updates on the states of the lesser tasks and, when these sub-tasks have been accomplished,
  * declare the primary task accomplished as well.<br>
  * <br>
  * This ia admittedly a simplistic model of task resolution, currently, and is rather specific and limited.
  * Generalizing and expanding on this class in the future might be beneficial.
  * @param obj the primary task
  * @param list a series of sub-tasks that need to be completed before the pimrary task can be completed
  * @param callback where to report about the pirmary task having succeeded or failed
  * @param timeoutDuration a delay during which sub-tasks are permitted to be accomplished;
  *                        after this grave period is over, the task has failed
  */
class RegistrationTaskResolver[T <: IdentifiableEntity](private val obj : T, private val list : List[T], callback : ActorRef, timeoutDuration : FiniteDuration) extends Actor {
  /** sub-tasks that contribute to completion of the task */
  private val checklist : Array[Boolean] = Array.fill[Boolean](list.length)(false)
  /** whether or not it matters that sub-tasks are coming in */
  private var valid : Boolean = true
  /** declares when the task has taken too long to complete */
  private val taskTimeout : Cancellable = context.system.scheduler.scheduleOnce(timeoutDuration, self, Failure(new TimeoutException(s"a task for $obj has timed out")))
  private[this] val log = org.log4s.getLogger
  ConfirmTask(Success(true)) //check for auto-completion

  def receive : Receive = {
    case Success(objn)=>
      ConfirmTask(ConfirmSubtask(objn.asInstanceOf[T]))

    case Failure(ex)=>
      FailedTask(ex)

    case msg =>
      log.warn(s"unexpected message received - ${msg.toString}")
  }

  /**
    * If this object is still accepting task resolutions, determine if that sub-task can be checked off.
    * @param objn the sub-task entry
    * @return a successful pass or a failure if the task can't be found;
    *         a "successful failure" if task resolutions are no longer accepted
    */
  private def ConfirmSubtask(objn : T) : Try[Boolean] = {
    if(valid) {
      if(MatchSubtask(objn, list.iterator)) {
        Success(true)
      }
      else {
        Failure(new Exception(s"can not find a subtask to check off - ${objn.toString}"))
      }
    }
    else {
      Success(false)
    }
  }

  /**
    * Find a sub-task from a `List` of sub-tasks and mark it as completed, if found.
    * @param objn the sub-task entry
    * @param iter_list an `Iterator` to the list of sub-tasks
    * @param index the index of this entry;
    *              defaults to zero
    * @return whether or not the subtask has been marked as completed
    */
  @tailrec private def MatchSubtask(objn : T, iter_list : Iterator[T], index : Int = 0) : Boolean = {
    if(!iter_list.hasNext) {
      false
    }
    else {
      val subtask = iter_list.next
      if(subtask.equals(objn)) {
        checklist(index) = true
        true
      }
      else {
        MatchSubtask(objn, iter_list, index + 1)
      }
    }
  }

  /**
    * Determine whether all sub-tasks have been completed successfully.
    * If so, complete the primary task.
    * @param subtaskComplete the status of the recent sub-task confirmation that triggered this confirmation request
    */
  private def ConfirmTask(subtaskComplete : Try[Boolean]) : Unit = {
    if(valid) {
      subtaskComplete match {
        case Success(true) =>
          if(!checklist.contains(false)) {
            FulfillTask()
          }
        case Success(false) =>
          log.warn(s"when checking a task for ${obj.toString}, arrived at a state where we previously failed a subtask but main task still valid")
        case Failure(ex) =>
          FailedTask(ex)
      }
    }
  }

  /**
    * All sub-tasks have been completed; the main task can also be completed.
    * Alert interested parties that the task is performed successfully.
    * Stop as soon as possible.
    */
  private def FulfillTask() : Unit = {
    valid = false
    callback ! Success(obj)
    taskTimeout.cancel()
    context.stop(self)
  }

  /**
    * The main task can not be completed.
    * Clean up as much as possible and alert interested parties that the task has been dropped.
    * Let this `Actor` stop gracefully.
    * @param ex why the main task can not be completed
    */
  private def FailedTask(ex : Throwable) : Unit = {
    valid = false
    callback ! Failure(ex)
    taskTimeout.cancel()
    import akka.pattern.gracefulStop
    gracefulStop(self, 2 seconds) //give time for any other messages; avoid dead letters
  }
}
