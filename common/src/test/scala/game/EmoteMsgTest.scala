// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.EmoteType
import scodec.bits._

class EmoteMsgTest extends Specification {
  val string = hex"25 4B00 15"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case EmoteMsg(avatar_guid, emote) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        emote mustEqual EmoteType.Thumbsdown
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = EmoteMsg(PlanetSideGUID(75), EmoteType.Thumbsdown)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
