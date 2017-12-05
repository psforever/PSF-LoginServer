// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class DetailedCommandDetonaterDataTest extends Specification {
  val string_detonater = hex"18 87000000 6506 EA8 7420 80 8000000200008"

  "DetailedCommandDetonaterData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_detonater).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.command_detonater
          guid mustEqual PlanetSideGUID(8308)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3530)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[DetailedCommandDetonaterData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedCommandDetonaterData()
      val msg = ObjectCreateDetailedMessage(ObjectClass.command_detonater, PlanetSideGUID(8308), ObjectCreateMessageParent(PlanetSideGUID(3530), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater
    }
  }
}
