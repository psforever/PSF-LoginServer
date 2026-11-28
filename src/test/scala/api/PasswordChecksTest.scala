// Copyright (c) 2026 PSForever
package api

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import net.psforever.api.PasswordChecks
import org.specs2.mutable._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure

class PasswordChecksTest extends Specification {
  "password checks" should {
    "return the check's result" in {
      val checks = new PasswordChecks(2, 2)
      try Await.result(checks.run(21 * 2), 5.seconds) mustEqual 42
      finally checks.shutdown()
    }

    "run at most the configured number at once, queue a few, and refuse the rest as busy" in {
      val checks  = new PasswordChecks(2, 1)
      val release = new CountDownLatch(1)
      val started = new CountDownLatch(2)
      val running = new AtomicInteger()
      val peak    = new AtomicInteger()
      def slow(): Int = {
        val now = running.incrementAndGet()
        peak.accumulateAndGet(now, math.max)
        started.countDown()
        release.await()
        running.decrementAndGet()
      }
      try {
        val accepted = (1 to 3).map(_ => checks.run(slow()))
        val refused  = checks.run(slow())
        Await.ready(refused, 5.seconds).value must beSome(Failure(PasswordChecks.Busy))
        // Both workers must be inside the check before it is released, or the first can finish
        // before the second increments the running count and the observed peak depends on timing.
        started.await(5, TimeUnit.SECONDS) must beTrue
        release.countDown()
        accepted.foreach(f => Await.ready(f, 5.seconds))
        (accepted.forall(_.value.exists(_.isSuccess)), peak.get) mustEqual ((true, 2))
      } finally checks.shutdown()
    }

    "with no queue, refuse as soon as every thread is busy" in {
      val checks  = new PasswordChecks(1, 0)
      val release = new CountDownLatch(1)
      try {
        val first = checks.run(release.await())
        Thread.sleep(50)
        Await.ready(checks.run(()), 5.seconds).value must beSome(Failure(PasswordChecks.Busy))
        release.countDown()
        Await.ready(first, 5.seconds).value.exists(_.isSuccess) must beTrue
      } finally checks.shutdown()
    }
  }
}
