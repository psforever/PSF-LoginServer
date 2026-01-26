// Copyright (c) 2026 PSForever
package net.psforever.services.base

import net.psforever.types.PlanetSideGUID
import scala.collection.concurrent.{Map => CMap}
import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import scala.annotation.unused

/*
Adapted from the rating limiting code in https://github.com/Pinapse/giant with permission
 */

trait GenericGuidEventBusMsg extends GenericEventBusMsg {
  def guid: PlanetSideGUID
}

class RateLimitScheduler[A <: GenericGuidEventBusMsg](eventBus: GenericGuidEventBus[A], interval: Long) extends Thread {
  private var hasWork: Boolean = false
  private var working: Boolean = false
  private var timeOfLastFlush: Long = 0L
  private val buffer: CMap[String, CMap[String, CMap[PlanetSideGUID, A]]] =
    new ConcurrentHashMap[String, CMap[String, CMap[PlanetSideGUID, A]]]().asScala

  override def run(): Unit = {
    while (working) {
      //originally, there was a Thread.sleep(interval) here, replaced by timeOfLastFlush logic
      flushBuffer()
    }
  }

  def push(event: A): Unit = {
    val eventClassName = event.inner.getClass.getName
    val cache =
      buffer
        .getOrElseUpdate(event.channel, new ConcurrentHashMap[String, CMap[PlanetSideGUID, A]]().asScala)
        .getOrElseUpdate(eventClassName, new ConcurrentHashMap[PlanetSideGUID, A]().asScala)
    cache.updateWith(event.guid) { _ => Some(event) }
    hasWork = true
  }

  def flushBuffer(): Unit = {
    val curr = System.currentTimeMillis()
    if (hasWork && timeOfLastFlush + interval <= curr) {
      flushBufferNow(curr)
    }
  }

  def flushBufferNow(curr: Long = System.currentTimeMillis()): Unit = {
    buffer.foreachEntry { (_, map) =>
      map.foreachEntry { (_, map) =>
        map.foreachEntry { (_, event) =>
          eventBus.publishForce(event)
        }
        map.clear()
      }
    }
    hasWork = false
    timeOfLastFlush = curr
  }

  override def start(): Unit = {
    working = true
    timeOfLastFlush = System.currentTimeMillis()
    super.start()
  }

  def isRunning: Boolean = {
    working
  }

  def stopRunning(): Unit = {
    working = false
    flushBufferNow()
  }
}

abstract class GenericGuidEventBus[A <: GenericGuidEventBusMsg](rateLimit: Double)
  extends GenericEventBus[A] {
  private val rateLimitedDispatch = new RateLimitScheduler[A](
    eventBus = this,
    scala.math.floor(1000.0 / rateLimit).toInt
  )
  rateLimitedDispatch.start()

  override def publish(event: Event): Unit = {
    if (rateLimit > 0 && shouldRateLimit(event) && rateLimitedDispatch.isRunning) {
      rateLimitedDispatch.push(event)
    } else {
      publishForce(event)
    }
  }

  def shouldRateLimit(@unused event: Event): Boolean

  def publishForce(event: Event): Unit = {
    super.publish(event)
  }

//  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
//    val trimmedEventClassName =
//      event.inner.getClass().getName().stripPrefix(event.inner.getClass.getPackageName() + ".").stripSuffix("$")
//    GenericGuidEventBus.genericGuidEventBusPublish.labels(event.channel, trimmedEventClassName).inc()
//    subscriber ! event
//  }
}
