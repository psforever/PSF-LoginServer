// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ObjectDeployedMessageTest extends Specification {
  val string_boomer = hex"86 000086626F6F6D6572040000000100000019000000"

  "decode" in {
    PacketCoding.decodePacket(string_boomer).require match {
      case ObjectDeployedMessage(unk: Int, desc: String, act: DeployOutcome.Value, count: Long, max: Long) =>
        unk mustEqual 0
        desc mustEqual "boomer"
        act mustEqual DeployOutcome.Success
        count mustEqual 1
        max mustEqual 25
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectDeployedMessage("boomer", DeployOutcome.Success, 1, 25)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer
  }
}
