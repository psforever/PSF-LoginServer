// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

import org.specs2.mutable._
import net.psforever.actors.net.SMPFragmentOrder
import scodec.bits._

/**
  * The PlanetSide transport's fragment channel is order-sensitive (per the shipped 3.15.84
  * client: `SOEChannelProcessSlottedPacket` holds every frame that arrives early in a ring and
  * only ever delivers frames to the fragment channel - `SOEChannelDeliverInOrderPacket`,
  * channel 1 - in sequence). The reassembly fed out of order would misread a continuation
  * fragment's leading bytes as its message's declared size, corrupting both its own message and
  * the next one to pass through the channel.
  *
  * `SMPFragmentOrder` mirrors that discipline for the server's inbound half: frames are held by
  * subslot, and released only once the caller reports that no subslots are still outstanding
  * below them (anything that was outstanding has either arrived and been held itself, or has
  * been given up on).
  */
class SMPFragmentOrderTest extends Specification {

  private def frame(n: Int): ByteVector =
    ByteVector((0x40 + n).toByte, (n & 0xff).toByte, 0x11, 0x22)

  "the fragment orderer" should {

    "release an in-order frame immediately when nothing is outstanding" in {
      val order = new SMPFragmentOrder

      order.hold(slot = 4, subslot = 10, inner = frame(10)) must beTrue
      order.release(subslotsOutstanding = false) mustEqual List((4, 10, frame(10)))
      order.held mustEqual 0
    }

    "hold frames while earlier subslots are still outstanding, and release them in subslot order once none are" in {
      val order = new SMPFragmentOrder

      order.hold(4, 12, frame(12)) must beTrue
      order.release(subslotsOutstanding = true) must beEmpty
      order.held mustEqual 1

      order.hold(4, 13, frame(13)) must beTrue
      order.release(subslotsOutstanding = true) must beEmpty

      //the missing subslot 11 arrives (its predecessors are all accounted for now)
      order.hold(4, 11, frame(11)) must beTrue
      order.held mustEqual 3

      order.release(subslotsOutstanding = false) mustEqual List(
        (4, 11, frame(11)),
        (4, 12, frame(12)),
        (4, 13, frame(13))
      )
      order.held mustEqual 0
      order.release(subslotsOutstanding = false) must beEmpty
    }

    "release frames in subslot order regardless of arrival order" in {
      val order = new SMPFragmentOrder

      //a burst arrives completely shuffled around the missing subslots 20 and 24
      Seq(26, 22, 25, 21, 23).foreach(s => order.hold(5, s, frame(s)))
      order.held mustEqual 5

      order.hold(4, 24, frame(24)) must beTrue
      order.hold(4, 20, frame(20)) must beTrue

      order.release(subslotsOutstanding = false).map { case (_, subslot, _) => subslot } mustEqual
        Seq(20, 21, 22, 23, 24, 25, 26)
    }

    "drop a copy of a subslot that was already released" in {
      val order = new SMPFragmentOrder

      order.hold(4, 10, frame(10))
      order.release(subslotsOutstanding = false)
      //a late retransmission of a subslot already handed over can no longer belong to the ordered stream
      order.hold(4, 10, frame(10)) must beFalse
      order.held mustEqual 0
      order.release(subslotsOutstanding = false) must beEmpty
    }

    "keep one copy of a subslot when it is held again while still unreleased" in {
      val order = new SMPFragmentOrder

      order.hold(4, 10, frame(10)) must beTrue
      order.release(subslotsOutstanding = true) must beEmpty
      //a retransmission of a frame that was already held but not released overwrites the held copy
      order.hold(4, 10, frame(99)) must beTrue
      order.held mustEqual 1
      order.release(subslotsOutstanding = false) mustEqual List((4, 10, frame(99)))
    }

    "keep the re-held copy of a subslot while an older one is still outstanding" in {
      val order = new SMPFragmentOrder

      order.hold(4, 10, frame(10))
      order.release(subslotsOutstanding = true) must beEmpty
      //a retransmission of subslot 11 while the gap below it stays open overwrites the held copy
      order.held mustEqual 1
      order.hold(4, 11, frame(11)) must beTrue
      order.held mustEqual 2
      order.release(subslotsOutstanding = true) must beEmpty

      order.hold(4, 9, frame(9)) must beTrue
      order.release(subslotsOutstanding = false) mustEqual List(
        (4, 9, frame(9)),
        (4, 10, frame(10)),
        (4, 11, frame(11))
      )
    }

    "forget its held state when cleared" in {
      val order = new SMPFragmentOrder

      order.hold(4, 10, frame(10))
      order.release(subslotsOutstanding = false)
      order.clear()
      order.held mustEqual 0
      //after the teardown the orderer starts fresh, and its released-through memory is gone
      order.hold(4, 10, frame(10)) must beTrue
      order.release(subslotsOutstanding = false) mustEqual List((4, 10, frame(10)))
    }
  }
}
