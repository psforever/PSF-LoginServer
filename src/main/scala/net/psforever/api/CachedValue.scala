package net.psforever.api

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
  * A value that is reloaded at most once per `ttl`, however many callers ask for it.
  *
  * This is what keeps public request volume from turning into load on the game world: while a value is
  * fresh every caller gets the same copy, and once it expires concurrent callers share one reload rather
  * than starting one each. A failed reload is not cached; callers are given the last good value instead,
  * so a brief stall in the world does not make the API go dark -- but only while that value is younger
  * than `maxStale`. After that the failure is reported, rather than old data passed off as current.
  *
  * @param maxStale how old a value may be and still stand in for a failed reload
  * @param nanoTime the clock, replaceable in tests
  */
final class CachedValue[T](
    ttl: FiniteDuration,
    maxStale: FiniteDuration,
    load: () => Future[T],
    nanoTime: () => Long = () => System.nanoTime()
)(implicit ec: ExecutionContext) {
  private var value: Option[(T, Long)]    = None
  private var inFlight: Option[Future[T]] = None

  def get(): Future[T] =
    synchronized {
      val now = nanoTime()
      value match {
        case Some((v, at)) if now - at < ttl.toNanos => Future.successful(v)
        case _ =>
          inFlight.getOrElse {
            val promise = Promise[T]()
            inFlight = Some(promise.future)
            val loaded =
              try load()
              catch { case e: Throwable => Future.failed(e) }
            loaded.onComplete { result =>
              val outcome = synchronized {
                inFlight = None
                result match {
                  case Success(v) =>
                    value = Some((v, nanoTime()))
                    Success(v)
                  case Failure(e) =>
                    value match {
                      case Some((v, at)) if nanoTime() - at < maxStale.toNanos => Success(v)
                      case _                                                   => Failure(e)
                    }
                }
              }
              promise.complete(outcome)
            }
            promise.future
          }
      }
    }
}
