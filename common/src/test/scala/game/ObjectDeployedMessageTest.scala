// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class ObjectDeployedMessageTest extends Specification {
  val string_boomer = hex"86 000086626F6F6D6572040000000100000019000000"

  "decode" in {
    PacketCoding.DecodePacket(string_boomer).require match {
      case ObjectDeployedMessage(guid : PlanetSideGUID, desc : String, unk : Long, count : Long, max : Long) =>
        guid mustEqual PlanetSideGUID(0)
        desc mustEqual "boomer"
        unk mustEqual 4
        count mustEqual 1
        max mustEqual 25
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectDeployedMessage("boomer", 4, 1, 25)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer
  }
}
