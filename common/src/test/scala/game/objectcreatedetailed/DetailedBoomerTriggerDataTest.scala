// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class DetailedBoomerTriggerDataTest extends Specification {
  val string_boomer_trigger = hex"18 87000000 6304CA8760B 80 C800000200008"

  "DetailedBoomerTriggerData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_boomer_trigger).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.boomer_trigger
          guid mustEqual PlanetSideGUID(2934)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(2502)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[DetailedBoomerTriggerData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedBoomerTriggerData()
      val msg = ObjectCreateDetailedMessage(ObjectClass.boomer_trigger, PlanetSideGUID(2934), ObjectCreateMessageParent(PlanetSideGUID(2502), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_boomer_trigger
    }
  }
}
