// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class FriendsRequestTest extends Specification {
  val string = hex"72 3 0A0 46004A0048004E004300"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case FriendsRequest(action, friend) =>
        action mustEqual 1
        friend.length mustEqual 5
        friend mustEqual "FJHNC"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = FriendsRequest(1, "FJHNC")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
