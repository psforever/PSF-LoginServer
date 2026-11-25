// Copyright (c) 2026 PSForever
package api

import java.util.concurrent.atomic.AtomicInteger

import net.psforever.api.CachedValue
import org.specs2.mutable._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

class CachedValueTest extends Specification {
  private implicit val ec: ExecutionContext = ExecutionContext.global

  private def await[T](f: Future[T]): T = Await.result(f, 5.seconds)

  private class Clock { @volatile var now = 0L; def apply(): Long = now }

  "CachedValue" should {
    "reuse a value until it expires" in {
      val clock = new Clock
      val loads = new AtomicInteger
      val cache =
        new CachedValue[Int](5.seconds, 60.seconds, () => Future.successful(loads.incrementAndGet()), () => clock())
      await(cache.get()) mustEqual 1
      await(cache.get()) mustEqual 1
      clock.now += 6.seconds.toNanos
      await(cache.get()) mustEqual 2
    }

    "share one reload between concurrent callers" in {
      val loads   = new AtomicInteger
      val pending = Promise[Int]()
      val cache =
        new CachedValue[Int](5.seconds, 60.seconds, () => { loads.incrementAndGet(); pending.future }, () => 0L)
      val callers = (1 to 50).map(_ => cache.get())
      pending.success(7)
      callers.map(f => await(f)) must contain(be_==(7)).forall
      loads.get mustEqual 1
    }

    "serve the last good value when a reload fails" in {
      val clock = new Clock
      var fail  = false
      val cache = new CachedValue[Int](
        5.seconds,
        60.seconds,
        () => if (fail) Future.failed(new RuntimeException("world stalled")) else Future.successful(3),
        () => clock()
      )
      await(cache.get()) mustEqual 3
      fail = true
      clock.now += 6.seconds.toNanos
      await(cache.get()) mustEqual 3
    }

    "stop serving the last good value once it is older than the limit" in {
      val clock = new Clock
      var fail  = false
      val cache = new CachedValue[Int](
        5.seconds,
        60.seconds,
        () => if (fail) Future.failed(new RuntimeException("world stalled")) else Future.successful(3),
        () => clock()
      )
      await(cache.get()) mustEqual 3
      fail = true
      clock.now += 61.seconds.toNanos
      await(cache.get()) must throwA[RuntimeException]
    }

    "fail when there has never been a value" in {
      val cache = new CachedValue[Int](5.seconds, 60.seconds, () => throw new RuntimeException("not started"), () => 0L)
      await(cache.get()) must throwA[RuntimeException]
    }
  }
}
