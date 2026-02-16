// Copyright (c) 2026 PSForever
package net.psforever.services.base

import net.psforever.services.Service
import net.psforever.services.base.envelope.{GenericMessageEnvelope, MessageEnvelope, MessageTransformationBehavior}
import net.psforever.services.base.message.EventMessage
import net.psforever.types.PlanetSideGUID

import scala.collection.concurrent.{Map => CMap}
import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap

/*
Adapted from the rating limiting code in https://github.com/Pinapse/giant with permission
 */

trait CachedGenericEventMessageEnvelope
  extends MessageTransformationBehavior {
  def guid: PlanetSideGUID
}

final case class CachedMessage(
                                guid: PlanetSideGUID,
                                originalChannel: String,
                                override val filter: PlanetSideGUID,
                                override val msg: EventMessage
                              ) extends CachedGenericEventMessageEnvelope {
  assert(guid != Service.defaultPlayerGUID, "can not cache message under default GUID")
}

object CachedMessage {
  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): GenericMessageEnvelope = {
    if (filter == Service.defaultPlayerGUID) {
      MessageEnvelope(channel, filter, msg)
    } else {
      CachedMessage(filter, channel, filter, msg)
    }
  }
}

private case object FlushCachedMessages

abstract class GenericEventServiceWithCacheAndSupport
(
  stamp: EventSystemStamp,
  eventSupportServices: List[EventServiceSupport]
) extends GenericEventServiceWithSupport(stamp, eventSupportServices) {
  private val flushCacheWait: Long = 50 //milliseconds
  private var hasCachedMessages: Boolean = false
  private var nextTimeToFlushCache: Long = 0L
  private val cache: CMap[String, CMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]] =
    new ConcurrentHashMap[String, CMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]]().asScala

  override def postStop(): Unit = {
    flushCache()
    super.postStop()
  }

  private def tryRetimeFlushCache(): Unit = {
    if (!hasCachedMessages) {
      hasCachedMessages = true
      nextTimeToFlushCache = System.currentTimeMillis() + flushCacheWait
    }
  }

  private def pushToCache(event: CachedGenericEventMessageEnvelope): Unit = {
    val eventClassName = event.msg.getClass.getName
    val updateBranch = cache
      .getOrElseUpdate(event.channel, new ConcurrentHashMap[String, CMap[PlanetSideGUID, GenericMessageEnvelope]]().asScala)
      .getOrElseUpdate(eventClassName, new ConcurrentHashMap[PlanetSideGUID, GenericMessageEnvelope]().asScala)
    updateBranch.updateWith(event.guid) { _ => Some(event) }
    tryRetimeFlushCache()
  }

  private def tryFlushCache(): Unit = {
    if (hasCachedMessages && nextTimeToFlushCache < System.currentTimeMillis()) {
      flushCache()
    }
  }

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
  }

  override protected def handleMessage(event: GenericMessageEnvelope): Unit = {
    event match {
      case envelope: CachedGenericEventMessageEnvelope =>
        pushToCache(envelope)
      case _ =>
        super.handleMessage(event)
    }
    tryFlushCache()
  }
}
