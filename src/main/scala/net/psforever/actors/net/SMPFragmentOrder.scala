// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.actors.net

import scala.collection.mutable

import scodec.bits.ByteVector

/**
  * Holds the frames of the PlanetSide transport's fragment channel until they can be handed to the
  * reassembly in subslot order.
  *
  * A fragmented message is only meaningful as an in-order byte stream: the message's opening fragment
  * leads with the 4-byte big-endian size of the whole message, and every later frame of the channel
  * is an unmarked continuation of that stream (see `SMPFragmentAssembler`). The frames must therefore
  * be reassembled strictly in subslot order, never in the order they happen to arrive. The shipped
  * 3.15.84 client arranges the same discipline on its side: `SOEChannelProcessSlottedPacket` holds
  * every frame that arrives early in a ring and only ever delivers frames to the fragment channel in
  * sequence (`SOEChannelDeliverInOrderPacket`, channel 1).
  *
  * Frames are *held* by subslot as they arrive, and *released* only once no earlier subslot can still
  * arrive - practically, once the caller reports that no subslots are outstanding in its
  * missing-subslot request machinery. Until every earlier subslot has arrived - and been held - or has
  * been given up on, a frame might still be preceded by fragments of its own message.
  */
final class SMPFragmentOrder {

  /** frames waiting for their predecessors, ordered by subslot */
  private val heldFrames: mutable.TreeMap[Int, (Int, ByteVector)] = mutable.TreeMap()

  /** the highest subslot ever released; a frame at or below it is a duplicate of released data */
  private var releasedThrough: Int = -1

  /** the number of fragment frames currently held */
  def held: Int = heldFrames.size

  /**
    * Hold one fragment channel frame for ordered release.
    * A frame whose subslot was already released is a duplicate of data that was handed over before;
    * it can no longer belong to the ordered stream and is dropped.
    * @param slot    the SMP slot the frame arrived on
    * @param subslot the SMP sequence number of the frame
    * @param inner   the raw frame contents
    * @return true if the frame was held; false if it was dropped as a stale duplicate
    */
  def hold(slot: Int, subslot: Int, inner: ByteVector): Boolean = {
    if (subslot <= releasedThrough) {
      false
    } else {
      heldFrames.update(subslot, (slot, inner))
      true
    }
  }

  /**
    * Release the held frames, in subslot order, that can no longer be preceded by anything in flight.
    * Every held frame is eligible at once or none of them are: a frame is releasable when no subslots
    * at all are still outstanding below them (any that were outstanding have either arrived and been
    * held, or have been given up on and will never arrive).
    * @param subslotsOutstanding whether the caller's missing-subslot request machinery still has any subslots outstanding
    * @return the released frames as `(slot, subslot, contents)`, in subslot order; the entries are removed
    */
  def release(subslotsOutstanding: Boolean): List[(Int, Int, ByteVector)] = {
    if (!subslotsOutstanding && heldFrames.nonEmpty) {
      val frames = heldFrames.toList.map { case (subslot, (slot, inner)) => (slot, subslot, inner) }
      releasedThrough = heldFrames.lastKey
      heldFrames.clear()
      frames
    } else {
      Nil
    }
  }

  /** Forget all held frames and release state; used when the connection or the fragment stream is torn down */
  def clear(): Unit = {
    heldFrames.clear()
    releasedThrough = -1
  }
}
