// Copyright (c) 2026 PSForever
package base

import akka.testkit.TestProbe
import net.psforever.services.base.envelope.{BundledEnvelope, GenericMessageEnvelope}
import net.psforever.services.base.message.SendResponse
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._

/**
  * Helpers for asserting on the envelopes that production code hands to an event service.
  *
  * An event service may receive one envelope per message or several envelopes packaged
  * together in a `BundledEnvelope`, and it may reorder independent messages. Subscribers
  * observe the same content either way, so tests should flatten first and assert on the
  * content instead of on the packing or the order.
  */
object EventEnvelopeAssertions {
  /** Routing data and payload extracted from a single message envelope. */
  type FlatEnvelope = (String, PlanetSideGUID, Any)

  /** Unwraps nested bundles and extracts the channel, filter, and payload of each envelope. */
  def flatten(received: Iterable[Any]): Seq[FlatEnvelope] = received.toSeq.flatMap(flattenOne)

  private def flattenOne(received: Any): Seq[FlatEnvelope] = received match {
    case BundledEnvelope(msgs) => msgs.toSeq.flatMap(flattenOne)
    case envelope: GenericMessageEnvelope =>
      Seq((envelope.channel, envelope.filter, envelope.msg))
    case _ => Seq.empty
  }

  /** True if any flattened envelope satisfies `test`. */
  def contains(received: Iterable[Any])(test: PartialFunction[FlatEnvelope, Boolean]): Boolean =
    flatten(received).exists(envelope => test.applyOrElse(envelope, (_: FlatEnvelope) => false))

  /** Number of flattened envelopes that satisfy `test`. */
  def count(received: Iterable[Any])(test: PartialFunction[FlatEnvelope, Boolean]): Int =
    flatten(received).count(envelope => test.applyOrElse(envelope, (_: FlatEnvelope) => false))

  /**
    * True if any `SendResponse` payload in the received envelopes contains a packet that satisfies `test`.
    * Production commonly bundles several packets into one `SendResponse`, so packet-level
    * assertions should look inside the response instead of counting envelopes.
    */
  def containsPacket(received: Iterable[Any])(test: PartialFunction[Any, Boolean]): Boolean =
    flatten(received).exists {
      case (_, _, SendResponse(packets)) =>
        packets.exists(packet => test.applyOrElse(packet, (_: Any) => false))
      case _ => false
    }

  /**
    * Collects everything the probe receives until it has been idle for `idle`.
    * Use this in place of `receiveN` when the number of envelopes depends on bundling.
    */
  def drain(probe: TestProbe, max: FiniteDuration = 2.seconds, idle: FiniteDuration = 250.milliseconds): Seq[Any] =
    probe.receiveWhile(max, idle) { case message => message }
}
