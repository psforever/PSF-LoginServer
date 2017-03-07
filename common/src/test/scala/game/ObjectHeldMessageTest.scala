// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ObjectHeldMessageTest extends Specification {
  val string = hex"33 4B00 02 00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        held_holsters mustEqual 2
        unk1 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectHeldMessage(PlanetSideGUID(75), 2, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
