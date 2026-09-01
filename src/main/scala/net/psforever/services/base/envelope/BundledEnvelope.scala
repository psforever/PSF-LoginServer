// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.objects.Default
import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse}
import net.psforever.types.PlanetSideGUID

import scala.annotation.tailrec

/**
 * A message when there should be none, but a message is required to be defined anyway.
 */
case object NoMessage extends EventMessage {
  override def response(): EventResponse = NoReply
}

/**
 * A response envelope when there should be none, but an envelope for messaging product is required to be defined anyway.
 */
case object NoResponse extends GenericResponseEnvelope {
  override def reply: EventResponse = NoReply
  override def stamp: EventSystemStamp = Undelivered
  override def channel: String = ""
  override def filter: PlanetSideGUID = Default.GUID0
}

/**
 * A collection of messaging envelopes to be dispatched to an event system at the same time
 * within the conditions of event system synchronization between different messages.
 * All messages contained within the bundling are to be processed at the time of processing by the bundling.
 * The order of the bundled message envelopes should be considered important.
 * @see `SendResponse(Seq[PlanetSideGamePacket])`
 * @param msgs list of message envelopes
 */
final case class BundledEnvelope(msgs: Iterable[GenericMessageEnvelope])
  extends GenericMessageEnvelope {
  assert(!msgs.exists(_.isInstanceOf[BundledEnvelope]), "do not nest bundled event system envelopes")

  override def msg: EventMessage = NoMessage
  override def response(stamp: EventSystemStamp): GenericResponseEnvelope = NoResponse
  override def channel: String = ""
  override def filter: PlanetSideGUID = Default.GUID0
}

object BundledEnvelope {
  /**
   * Overloaded constructor for `BundledEnvelope` objects.
   * The entities are separated between "the first" and "any others" to distinguish from
   * the `case class` constructor that accepts any number of message envelopes
   * including no message envelopes at all.
   * @param first single, required `GenericMessageEnvelope` entity for bundling
   * @param msgs any other `GenericMessageEnvelope` entity for bundling
   * @return a `BundledEnvelope` object
   */
  def apply(first: GenericMessageEnvelope, msgs: GenericMessageEnvelope*): BundledEnvelope = {
    new BundledEnvelope(unwind(first +: msgs))
  }

  /**
   * Input sanitization method that unpacks `BundledEnvelope` message envelopes
   * producing a single-dimensional list of `GenericMessageEnvelope` entities.
   * An assertion in the constructor of `BundledEnvelope` aborts object creation
   * if a `BundledEnvelope` entity is within that `BundledEnvelope` entity.
   * @param in list of `GenericMessageEnvelope` entities to be processed
   * @param out list of `GenericMessageEnvelope` entities that have been processed
   * @return list of `GenericMessageEnvelope` entities that have been processed
   */
  @tailrec
  private def unwind(in: Iterable[GenericMessageEnvelope], out: List[GenericMessageEnvelope] = Nil): List[GenericMessageEnvelope] = {
    if (in.isEmpty) {
      out.reverse
    } else {
      in.head match {
        case bundle: BundledEnvelope =>
          unwind(bundle.msgs ++ in.tail, out)
        case first =>
          unwind(in.tail, first :: out)
      }
    }
  }
}
