// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.actors.net

import scodec.bits.ByteVector

/**
  * Reassembles the fragmented messages of the PlanetSide transport's fragment channel.
  *
  * `SlottedMetaPacket` frames divide into two halves by slot (ground truth from the shipped
  * 3.15.84 client, `SOEChannelProcessSlottedPacket` + `SOEChannelDeliverInOrderPacket`):
  * * slots 0-3 (opcodes `0x09`-`0x0c`): each frame's contents are one *complete* message;
  * * slots 4-7 (opcodes `0x0d`-`0x10`): each frame's contents are a *fragment* of a message that is
  *   too large for a single frame. The first fragment starts with a 4-byte big-endian `u32`
  *   declaring the size of the whole message, followed by message bytes; the message spans however
  *   many consecutive frames it takes, and only the declared number of bytes are collected.
  *   Bytes a frame carries beyond the declared size are discarded - the client's own reader
  *   (`SOEChannelDeliverInOrderPacket`, channel 1) collects exactly `min(remaining, available)` per frame.
  *
  * This class implements the fragment-half state machine only; order of delivery is arranged
  * by the subslot sequence machinery around it, and decoding of completed messages belongs to the caller.
  *
  * @param reassemblyLimitBytes the largest declared message size accepted; a larger declaration
  *                             can not be genuine on this protocol and is rejected outright,
  *                             bounding the memory a single connection can pin
  */
final class SmpFragmentAssembler(reassemblyLimitBytes: Int) {

  /** the size declared by the message's first fragment; negative while no message is being assembled */
  private var expectedBytes: Long = -1L

  /** the message bytes collected so far */
  private var assembled: ByteVector = ByteVector.empty

  /** the number of fragments collected for the message under assembly */
  private var fragmentCount: Long = 0L

  /** the size declared by the message whose fragments are being collected (0 if none) */
  def declaredSize: Long = math.max(expectedBytes, 0L)

  /** the number of fragments collected so far for the message under assembly */
  def fragments: Long = fragmentCount

  /** whether a message is currently under assembly */
  def isActive: Boolean = expectedBytes >= 0

  /**
    * Feed one frame of the fragment channel.
    * A frame arriving with no message under assembly is expected to be the message's first fragment
    * (its leading 4 bytes carry the declared total size).
    * @param frame the raw frame contents
    * @return the reassembled message bytes once complete, the instruction to keep waiting for
    *         more frames, or the instruction to discard (with reason) when the stream is not
    *         acceptable
    */
  def append(frame: ByteVector): SmpFragmentAssembler.Result = {
    if (expectedBytes < 0) {
      if (frame.length < 4) {
        SmpFragmentAssembler.Rejected(
          s"a ${frame.length} B continuation fragment arrived without its leading fragment"
        )
      } else {
        val declared = ((frame.get(0) & 0xffL) << 24) |
          ((frame.get(1) & 0xffL) << 16) |
          ((frame.get(2) & 0xffL) << 8) |
          (frame.get(3) & 0xffL)
        if (declared <= 0 || declared > reassemblyLimitBytes) {
          SmpFragmentAssembler.Rejected(
            s"a first fragment declared an out-of-bounds message size ($declared B; limit $reassemblyLimitBytes B)"
          )
        } else {
          expectedBytes = declared
          assembled = ByteVector.empty
          fragmentCount = 0
          absorb(frame.drop(4))
        }
      }
    } else {
      absorb(frame)
    }
  }

  private def absorb(fragment: ByteVector): SmpFragmentAssembler.Result = {
    fragmentCount += 1
    val remaining = expectedBytes - assembled.length
    assembled = assembled ++ fragment.take(remaining)
    if (assembled.length >= expectedBytes) {
      val message = assembled
      expectedBytes = -1
      assembled = ByteVector.empty
      val fragmentsTaken = fragmentCount
      fragmentCount = 0
      SmpFragmentAssembler.Completed(message, fragmentsTaken)
    } else {
      SmpFragmentAssembler.Waiting
    }
  }
}

object SmpFragmentAssembler {

  sealed trait Result

  /** the stream needs more fragments before a message is complete */
  case object Waiting extends Result

  /**
    * The message under assembly is complete.
    * @param message the reassembled message bytes, in whole
    * @param fragments the number of frames the message spanned
    */
  final case class Completed(message: ByteVector, fragments: Long) extends Result

  /**
    * The fed data can not be part of a valid stream and was dropped;
    * no message is under assembly anymore.
    * @param reason the reason for the rejection, for logging
    */
  final case class Rejected(reason: String) extends Result
}
