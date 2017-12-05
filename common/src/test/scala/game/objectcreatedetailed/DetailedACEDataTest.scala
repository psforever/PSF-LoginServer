// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class DetailedACEDataTest extends Specification {
  val string_ace = hex"18 87000000 1006 100 C70B 80 8800000200008"

  "DetailedACEData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_ace).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.ace
          guid mustEqual PlanetSideGUID(3015)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3104)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[DetailedACEData] mustEqual true
          data.get.asInstanceOf[DetailedACEData].unk mustEqual 8
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedACEData(8)
      val msg = ObjectCreateDetailedMessage(ObjectClass.ace, PlanetSideGUID(3015), ObjectCreateMessageParent(PlanetSideGUID(3104), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ace
    }
  }
}
