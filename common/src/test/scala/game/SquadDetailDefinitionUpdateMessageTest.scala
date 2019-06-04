// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class SquadDetailDefinitionUpdateMessageTest extends Specification {
  val string = hex"e80300848180038021514601288a8400420048006f0066004400bf5c0023006600660064006300300030002a002a002a005c0023003900360034003000660066003d004b004f004b002b005300500043002b0046004c0059003d005c0023006600660064006300300030002a002a002a005c002300460046003400300034003000200041006c006c002000570065006c0063006f006d006500070000009814010650005c00230066006600300030003000300020007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c008000000000800100000c00020c8c5c00230066006600640063003000300020002000200043008000000000800100000c00020c8c5c002300660066006400630030003000200020002000480080eab58a02854f0070006f006c0045000100000c00020c8d5c002300660066006400630030003000200020002000200049008072d47a028b42006f006200610046003300740074003900300037000100000c00020c8c5c0023006600660064006300300030002000200020004e008000000000800100000c00020c8c5c00230066006600640063003000300020002000200041008000000000800100000c00020ca05c00230066006600300030003000300020007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c008000000000800100000c00020c8c5c0023003900360034003000660066002000200020004b008000000000800100000c00020c8c5c0023003900360034003000660066002000200020004f008042a28c028448006f00660044000100000c00020c8c5c0023003900360034003000660066002000200020004b008000000000800100000c0000"

  "SquadDetailDefinitionUpdateMessage" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case SquadDetailDefinitionUpdateMessage(guid, unk, leader, task, zone, member_info) =>
          ok
        case _ =>
          ko
      }
    }

    "encode" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        "HofD",
        "\\#ffdc00***\\#9640ff=KOK+SPC+FLY=\\#ffdc00***\\#FF4040 All Welcome",
        PlanetSideZoneID(7),
        List(
          SquadPositionDetail("\\#ff0000 |||||||||||||||||||||||", ""),
          SquadPositionDetail("\\#ffdc00   C", ""),
          SquadPositionDetail("\\#ffdc00   H", "", "OpoIE"),
          SquadPositionDetail("\\#ffdc00    I", "", "BobaF3tt907"),
          SquadPositionDetail("\\#ffdc00   N", ""),
          SquadPositionDetail("\\#ffdc00   A", ""),
          SquadPositionDetail("\\#ff0000 |||||||||||||||||||||||", ""),
          SquadPositionDetail("\\#9640ff   K", ""),
          SquadPositionDetail("\\#9640ff   O", "", "HofD"),
          SquadPositionDetail("\\#9640ff   K", "")
        )
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      ok
    }
  }
}
