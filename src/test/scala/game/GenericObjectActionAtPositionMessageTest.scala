// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class GenericObjectActionAtPositionMessageTest extends Specification {
  val string = hex"d4 d504 09 00060 00110 000e" //faked

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case GenericObjectActionAtPositionMessage(object_guid, action, pos) =>
        object_guid mustEqual PlanetSideGUID(1237)
        action mustEqual 9
        pos mustEqual Vector3(12f, 34f, 56f)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericObjectActionAtPositionMessage(PlanetSideGUID(1237), 9, Vector3(12f, 34f, 56f))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

