// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DroppodLaunchResponseMessageTest extends Specification {
  val string_1 = hex"55 21506000000000000000000000"
  val string_2 = hex"55 9150605000000000000000000640000000a0000000"

  "DroppodLaunchResponseMessage" should {
    "decode (1)" in {
      PacketCoding.decodePacket(string_1).require match {
        case DroppodLaunchResponseMessage(error, info, queue) =>
          error mustEqual DroppodError.BlockedBySOI
          info.guid mustEqual PlanetSideGUID(1557)
          info.zone_number mustEqual 0
          info.xypos mustEqual Vector3.Zero
          queue.isEmpty mustEqual true
        case _ =>
          ko
      }
    }

    "decode (2)" in {
      PacketCoding.decodePacket(string_2).require match {
        case DroppodLaunchResponseMessage(error, info, queue) =>
          error mustEqual DroppodError.ZoneFullWarpQueue
          info.guid mustEqual PlanetSideGUID(1557)
          info.zone_number mustEqual 5
          info.xypos mustEqual Vector3.Zero
          queue.contains(WarpQueuePrompt(100, 10)) mustEqual true
        case _ =>
          ko
      }
    }

    "encode (1)" in {
      val msg = DroppodLaunchResponseMessage(DroppodError.BlockedBySOI, PlanetSideGUID(1557), 0, Vector3.Zero)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_1
    }

    "encode (2)" in {
      val msg = DroppodLaunchResponseMessage(PlanetSideGUID(1557), 5, 100, 10)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_2
    }

    "invalid constructors" in {
      //invalid
      DroppodLaunchResponseMessage(
        DroppodError.ZoneFullWarpQueue,
        DroppodLaunchInfo(PlanetSideGUID(1557), 5, Vector3.Zero),
        None
      ) must throwA[AssertionError]

      //acceptable
      DroppodLaunchResponseMessage(
        DroppodError.BlockedBySOI,
        DroppodLaunchInfo(PlanetSideGUID(1557), 5, Vector3.Zero),
        Some(WarpQueuePrompt(100, 10))
      )
      ok
    }

    "equivalent constructors (1)" in {
      DroppodLaunchResponseMessage(
        DroppodError.BlockedBySOI,
        PlanetSideGUID(1557)
      ) mustEqual
      DroppodLaunchResponseMessage(
        DroppodError.BlockedBySOI,
        DroppodLaunchInfo(PlanetSideGUID(1557), 0, Vector3.Zero),
        None)
    }

    "equivalent constructors (2)" in {
      DroppodLaunchResponseMessage(PlanetSideGUID(1557), 5, 100, 10) mustEqual
      DroppodLaunchResponseMessage(
        DroppodError.ZoneFullWarpQueue,
        DroppodLaunchInfo(PlanetSideGUID(1557), 5, Vector3.Zero),
        Some(WarpQueuePrompt(100, 10))
      )
    }
  }
}
