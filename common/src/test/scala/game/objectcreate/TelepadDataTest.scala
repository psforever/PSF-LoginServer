// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class TelepadDataTest extends Specification {
  val string = hex"17 86000000 5700 f3a a201 80 0302020000000"
  //TODO validate the unknown fields before router_guid for testing

  "TelepadData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 134
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(418)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(430)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[TelepadData] mustEqual true
          data.get.asInstanceOf[TelepadData].router_guid mustEqual Some(PlanetSideGUID(385))
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = TelepadData(0, PlanetSideGUID(385))
      val msg = ObjectCreateMessage(ObjectClass.router_telepad, PlanetSideGUID(418), ObjectCreateMessageParent(PlanetSideGUID(430), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}
