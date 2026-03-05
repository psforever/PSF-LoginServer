// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Cancellable
import net.psforever.objects.Default
import net.psforever.services.Service
import net.psforever.services.base.envelope.{GenericMessageEnvelope, GenericResponseEnvelope, MessageEnvelope, MessageTransformationBehavior}
import net.psforever.services.base.message.EventMessage
import net.psforever.types.PlanetSideGUID

import scala.collection.concurrent.{Map => CMap}
import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

/*
Adapted from the rating limiting code in PSForever fork https://github.com/Pinapse/giant with permission
 */

/**
 * A framework for flagged messages to be cached
 * based on designation of a target globally unique identifier.
 * Suggestion: this identifier be associated with the source.
 */
trait CachedGenericEventEnvelope
  extends MessageTransformationBehavior {
  /** cache designator */
  def guid: PlanetSideGUID
}

final case class CachedEnvelope(
                                 guid: PlanetSideGUID,
                                 channel: String,
                                 filter: PlanetSideGUID,
                                 msg: EventMessage
                               ) extends CachedGenericEventEnvelope {
  assert(guid != Service.defaultPlayerGUID, "can not cache message under default GUID")
}

object CachedEnvelope {
  /**
   * If the filter is specified, but not the cache target, treat the filter like the cache target.
   * Treat invalid values as a reason to downgrade from a cached message envelope to a traditional message envelope.
   * Warn of this downgrade.e
   * @param channel set of subscribers on an event system bus the envelope should reach
   * @param filter specific subscriber endpoint to be excluded (the subscriber should filter themselves)
   * @param msg input payload transported by this envelope
   * @return either a `CacheEnvelope` or a `MessageEnvelope`, depending on cache-readiness of the `filter` value
   */
  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): GenericMessageEnvelope = {
    if (filter == Service.defaultPlayerGUID) {
      org.log4s.getLogger("CachedEnvelope").warn("(1) cached message envelope downgraded to normal message envelope")
      MessageEnvelope(channel, filter, msg)
    } else {
      CachedEnvelope(filter, channel, filter, msg)
    }
  }

  /**
   * If the cache target is specified, but is not valid,
   * downgrade from a cached message envelope to a traditional message envelope.
   * Warn of this downgrade.
   * If valid, the filter becomes its defaulted value in the corresponding message
   * @param guid cache designator
   * @param channel set of subscribers on an event system bus the envelope should reach
   * @param msg input payload transported by this envelope
   * @return either a `CacheEnvelope` or a `MessageEnvelope`, depending on cache-readiness of the target
   */
  def apply(guid: PlanetSideGUID, channel: String, msg: EventMessage): GenericMessageEnvelope = {
    if (guid == Service.defaultPlayerGUID) {
      org.log4s.getLogger("CachedEnvelope").warn("(2) cached message envelope downgraded to normal message envelope")
      MessageEnvelope(channel, guid, msg)
    } else {
      CachedEnvelope(guid, channel, Service.defaultPlayerGUID, msg)
    }
  }
}

/**
 * An internal message for the purpose of forcing cached messages to be flushed.
 * All of it's fields default to harmless values because it is not intended to be processed by an event system
 * but it must maintain the trappings of a message envelope to be processed.
 * @see `NoMessage`
 * @see `NoResponseEnvelope`
 */
private case object FlushCachedMessages extends GenericMessageEnvelope {
  def originalChannel: String = ""
  def msg: EventMessage = NoMessage
  def response(stamp: EventSystemStamp): GenericResponseEnvelope = NoResponseEnvelope
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
}

class GenericEventServiceWithCacheAndSupport
(
  stamp: EventSystemStamp,
  eventSupportServices: List[EventServiceSupport]
) extends GenericEventServiceWithSupport(stamp, eventSupportServices) {
  private val flushCacheWait: Long = 50L //milliseconds
  private var hasCachedMessages: Boolean = false
  private var nextTimeToFlushCache: Long = 0L
  private var emergencyFlush: Cancellable = Default.Cancellable

  private val cache: CMap[String, CMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]] =
    new ConcurrentHashMap[String, CMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]]().asScala

  override def postStop(): Unit = {
    flushCache()
    super.postStop()
  }

  /**
   * If there were previously no messages in the cache,
   * prepare to flush the cache after the intended interval passes,
   * whether the passing of that interval is detected by a new incoming message and the cache gets flushed naturally,
   * or if a safety timer expires and the cache is flushed in precaution.
   */
  private def tryRetimeFlushCache(): Unit = {
    if (!hasCachedMessages) {
      hasCachedMessages = true
      nextTimeToFlushCache = System.currentTimeMillis() + flushCacheWait
      emergencyFlush = context.system.scheduler.scheduleOnce(55 milliseconds, self, FlushCachedMessages)
    }
  }

  /**
   * Add messages to the cache based on their channel, then their type, then their cache target identifier.
   * Messages that arrive with the same cache profile as a previous message,
   * but before that previous message was dispatched,
   * will overwrite the previous message without fanfare or warning.
   * @param event event system message
   */
  private def pushToCache(event: CachedGenericEventEnvelope): Unit = {
    val eventClassName = event.msg.getClass.getName
    val updateBranch = cache
      .getOrElseUpdate(event.channel, new ConcurrentHashMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]().asScala)
      .getOrElseUpdate(eventClassName, new ConcurrentHashMap[PlanetSideGUID, GenericMessageEnvelope]().asScala)
    updateBranch.updateWith(event.guid) { _ => Some(event) }
    tryRetimeFlushCache()
  }

  /**
   * If the cache has messages and the current time exceeds the anticipated flush time,
   * flush the cache messages to the normal event system bus.
   */
  private def tryFlushCache(): Boolean = {
    val willFLush = hasCachedMessages && nextTimeToFlushCache < System.currentTimeMillis()
    if (willFLush) {
      flushCache()
    }
    willFLush
  }

  /**
   * Flush the cache messages to the normal event system bus.
   * Clear old messages then reset all flags that would force the messages to be flushed.
   */
  private def flushCache(): Unit = {
    cache.foreachEntry { (_, map) =>
      map.foreachEntry { (_, map) =>
        map.foreachEntry { (_, event) =>
          super.handleMessage(event)
        }
        map.clear()
      }
    }
    hasCachedMessages = false
    emergencyFlush.cancel()
    emergencyFlush = Default.Cancellable
  }

  /**
   * If the cache will be flushed after this message, flush the cache and then pass the message for processing.
   * If the cache will not be flushed, add the message to the cache.
   * In any case, procedure should always be ready to flush the cache when a message is received.
   * If there is a support actor involved with a cached message, it is resolved when the message is flushed.
   * @param event event system message that may be cached
   */
  override protected def handleMessage(event: GenericMessageEnvelope): Unit = {
    event match {
      case FlushCachedMessages =>
        flushCache()
      case _: CachedGenericEventEnvelope if tryFlushCache() =>
        super.handleMessage(event)
      case envelope: CachedGenericEventEnvelope =>
        pushToCache(envelope)
      case _ =>
        tryFlushCache()
        super.handleMessage(event)
    }
  }
}
