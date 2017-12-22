// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class ObjectCreateBaseTest extends Specification {
  val packet217 = hex"17 F8 00 00 00 BC 8C 10 90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44 00 69 00 66 00 66 00 45" //fake data
  val packet218 = hex"18 F8 00 00 00 BC 8C 10 90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44 00 69 00 66 00 66 00 45" //fake data

  "ObjectCreateDetailedMessage" should {
    "fail to decode" in {
      //an invalid bit representation will fail to turn into an object
      PacketCoding.DecodePacket(packet217).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 248
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(2497)
          parent mustEqual None
          data.isDefined mustEqual false
        case _ =>
          ko
      }
    }

    "fail to encode" in {
      //the lack of an object will fail to turn into a bad bitstream
      val msg = ObjectCreateMessage(0L, ObjectClass.avatar, PlanetSideGUID(2497), None, None)
      PacketCoding.EncodePacket(msg).isFailure mustEqual true
    }
  }

  "ObjectCreateDetailedMessage" should {
    "fail to decode" in {
      //an invalid bit representation will fail to turn into an object
      PacketCoding.DecodePacket(packet218).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 248
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(2497)
          parent mustEqual None
          data.isDefined mustEqual false
        case _ =>
          ko
      }
    }

    "fail to encode" in {
      //the lack of an object will fail to turn into a bad bitstream
      val msg = ObjectCreateDetailedMessage(0L, ObjectClass.avatar, PlanetSideGUID(2497), None, None)
      PacketCoding.EncodePacket(msg).isFailure mustEqual true
    }
  }

  "StreamBitSize" should {
    "have zero size by default" in {
      new StreamBitSize() {}.bitsize mustEqual 0L
    }
  }
}
