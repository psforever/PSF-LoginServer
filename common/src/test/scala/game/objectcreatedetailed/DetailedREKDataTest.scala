// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class DetailedREKDataTest extends Specification {
  val string_rek = hex"18 97000000 2580 6C2 9F05 81 48000002000080000"

  "DetailedREKData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_rek).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 151
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(1439)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(75)
          parent.get.slot mustEqual 1
          data.isDefined mustEqual true
          data.get.asInstanceOf[DetailedREKData].unk1 mustEqual 4
          data.get.asInstanceOf[DetailedREKData].unk2 mustEqual 0
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedREKData(4)
      val msg = ObjectCreateDetailedMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(1439), ObjectCreateMessageParent(PlanetSideGUID(75), 1), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_rek
    }
  }
}
