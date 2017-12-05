// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class BoomerTriggerDataTest extends Specification {
  val string_boomertrigger = hex"17 76000000 58084A8100E80C00000000" //reconstructed from an inventory entry

  "BoomerTriggerData" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_boomertrigger).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.boomer_trigger
          guid mustEqual PlanetSideGUID(3600)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4272)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[BoomerTriggerData] mustEqual true
          data.get.asInstanceOf[BoomerTriggerData].unk mustEqual 0
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = BoomerTriggerData(0)
      val msg = ObjectCreateMessage(ObjectClass.boomer_trigger, PlanetSideGUID(3600), ObjectCreateMessageParent(PlanetSideGUID(4272), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_boomertrigger
    }
  }
}
