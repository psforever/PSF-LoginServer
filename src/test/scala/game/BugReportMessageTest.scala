// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class BugReportMessageTest extends Specification {
  val string =
    hex"89 03000000 0F000000 8B4465632020322032303039 1 1 0 19 6C511 656B1 7A11 830610062006300 843100320033003400"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case BugReportMessage(major, minor, date, btype, repeat, unk, zone, loc, summary, desc) =>
        major mustEqual 3
        minor mustEqual 15
        date mustEqual "Dec  2 2009"
        btype mustEqual BugType.GAMEPLAY
        repeat mustEqual true
        zone mustEqual 25
        loc.x mustEqual 674.84375f
        loc.y mustEqual 726.78906f
        loc.z mustEqual 69.90625f
        summary mustEqual "abc"
        desc mustEqual "1234"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BugReportMessage(
      3,
      15,
      "Dec  2 2009",
      BugType.GAMEPLAY,
      true,
      0,
      25,
      Vector3(674.84375f, 726.78906f, 69.90625f),
      "abc",
      "1234"
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
