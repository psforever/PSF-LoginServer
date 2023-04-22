package game

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{CaptureFlagUpdateMessage, FlagInfo}
import org.specs2.control.Debug
import org.specs2.mutable.Specification
import scodec.bits._

class CaptureFlagUpdateMessageTest extends Specification with Debug {
  val stringZero = hex"c0 0a0000"
  val stringOne = hex"c0 0a0014300018025281dd852830803000"

  "decode (zero)" in {
    PacketCoding.decodePacket(stringZero).require match {
      case CaptureFlagUpdateMessage(zone_number, flagInfoList) =>
        zone_number mustEqual 10
        flagInfoList.length mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (one)" in {
    PacketCoding.decodePacket(stringOne).require match {
      case CaptureFlagUpdateMessage(zone_number, flagInfoList) =>
        zone_number mustEqual 10
        flagInfoList.length mustEqual 1
        flagInfoList.head mustEqual FlagInfo(1, 12, 6, 3905.1562f, 5160.922f, 794636L, false)
      case _ =>
        ko
    }
  }

  "encode (zero)" in {
    val msg = CaptureFlagUpdateMessage(10, Nil)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual stringZero
  }

  "encode (one)" in {
    val msg = CaptureFlagUpdateMessage(10, List(FlagInfo(1, 12, 6, 3905.1562f, 5160.922f, 794636L, false)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual stringOne
  }
}
