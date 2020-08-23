// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import akka.actor.ActorRef

trait Task {
  def Description: String = "write_descriptive_task_message"
  def Execute(resolver: ActorRef): Unit
  def isComplete: Task.Resolution.Value = Task.Resolution.Incomplete
  def Timeout: Long                     = 200L //milliseconds
  def onSuccess(): Unit = {}
  def onFailure(ex: Throwable): Unit = {}
  def onTimeout(ex: Throwable): Unit = onFailure(ex)
  def onAbort(ex: Throwable): Unit = {}
  def Cleanup(): Unit = {}
}

object Task {
  def TimeNow: Long = {
    System.nanoTime()
    //java.time.Instant.now().getEpochSecond
  }

  object Resolution extends Enumeration {
    val Success, Incomplete, Failure = Value
  }
}
