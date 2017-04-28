// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class BattleplanMessageTest extends Specification {
  val string = hex"b3 3a197902 94 59006500740041006e006f0074006800650072004600610069006c0075007200650041006c007400 0000 01 e0"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case BattleplanMessage(unk1, mastermind, unk2, diagrams) =>
        unk1 mustEqual 41490746
        mastermind mustEqual "YetAnotherFailureAlt"
        unk2 mustEqual 0
        diagrams.size mustEqual 1
        //h0
        diagrams.head.pageNum mustEqual 14
        diagrams.head.sheet.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BattleplanMessage(
      41490746,
      "YetAnotherFailureAlt",
      0,
      BattleDiagram(14) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
