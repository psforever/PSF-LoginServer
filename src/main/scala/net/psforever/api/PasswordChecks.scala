package net.psforever.api

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{
  ArrayBlockingQueue,
  BlockingQueue,
  RejectedExecutionException,
  SynchronousQueue,
  ThreadFactory,
  ThreadPoolExecutor,
  TimeUnit
}

import scala.concurrent.{Future, Promise}
import scala.util.Try
import scala.util.control.NoStackTrace

/**
  * Runs password hashes on threads of their own, a few at a time.
  *
  * bcrypt is deliberately slow, so an unbounded number of sign-ins could take every core from the game. At
  * most `concurrent` checks run at once and `queued` more may wait; anything beyond that fails at once
  * with [[PasswordChecks.Busy]], which the API answers with 503 rather than letting work pile up.
  */
final class PasswordChecks(concurrent: Int, queued: Int) {
  require(concurrent > 0, "max-concurrent-password-checks must be positive")
  require(queued >= 0, "password-check-queue must not be negative")

  private val queue: BlockingQueue[Runnable] =
    if (queued == 0) new SynchronousQueue[Runnable]() else new ArrayBlockingQueue[Runnable](queued)

  private val executor =
    new ThreadPoolExecutor(concurrent, concurrent, 0L, TimeUnit.MILLISECONDS, queue, PasswordChecks.threads)

  /** Run one check, or fail with [[PasswordChecks.Busy]] if too many are already running or waiting. */
  def run[T](check: => T): Future[T] = {
    val result = Promise[T]()
    try {
      executor.execute(() => result.complete(Try(check)))
      result.future
    } catch {
      case _: RejectedExecutionException => Future.failed(PasswordChecks.Busy)
    }
  }

  /** Stop the threads once the running checks finish. */
  def shutdown(): Unit = executor.shutdown()
}

object PasswordChecks {

  /** Too many password checks are already running or waiting. */
  case object Busy extends RuntimeException("too many password checks in progress") with NoStackTrace

  private val threads: ThreadFactory = new ThreadFactory {
    private val count = new AtomicInteger()
    override def newThread(task: Runnable): Thread = {
      val thread = new Thread(task, s"psf-http-api-password-${count.incrementAndGet()}")
      thread.setDaemon(true)
      thread
    }
  }
}
