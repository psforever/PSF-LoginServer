// Copyright (c) 2019-2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class SquadMemberEventTest extends Specification {
  val string = hex"7000e008545180410848006f0066004400070051150800"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case SquadMemberEvent(event, u2, char_id, position, player_name, zone_number, outfit_id) =>
        event mustEqual MemberEvent.Add
        u2 mustEqual 7
        char_id mustEqual 42771010L
        position mustEqual 0
        player_name.contains("HofD") mustEqual true
        zone_number.contains(7) mustEqual true
        outfit_id.contains(529745L) mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SquadMemberEvent(MemberEvent.Add, 7, 42771010L, 0, Some("HofD"), Some(7), Some(529745L))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
