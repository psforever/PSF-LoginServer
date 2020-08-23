// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class AvatarFirstTimeEventMessageTest extends Specification {
  val string = hex"69 4b00 c000 01000000 9e 766973697465645f63657274696669636174696f6e5f7465726d696e616c"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, event_name) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        object_guid mustEqual PlanetSideGUID(192)
        unk1 mustEqual 1
        event_name mustEqual "visited_certification_terminal"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarFirstTimeEventMessage(PlanetSideGUID(75), PlanetSideGUID(192), 1, "visited_certification_terminal")
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
