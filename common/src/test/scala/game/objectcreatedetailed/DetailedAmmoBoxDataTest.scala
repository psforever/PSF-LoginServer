// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class DetailedAmmoBoxDataTest extends Specification {
  val string_9mm = hex"18 7C000000 2580 0E0 0005 A1 C8000064000"

  "DetailedAmmoBoxData" should {
    "decode (9mm)" in {
      PacketCoding.DecodePacket(string_9mm).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 124
          cls mustEqual ObjectClass.bullet_9mm
          guid mustEqual PlanetSideGUID(1280)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(75)
          parent.get.slot mustEqual 33
          data.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
        case _ =>
          ko
      }
    }

    "encode (9mm)" in {
      val obj = DetailedAmmoBoxData(8, 50)
      val msg = ObjectCreateDetailedMessage(ObjectClass.bullet_9mm, PlanetSideGUID(1280), ObjectCreateMessageParent(PlanetSideGUID(75), 33), obj)
      val out = PacketCoding.EncodePacket(msg)
      val pkt = out.require.toByteVector

      pkt mustEqual string_9mm
    }
  }
}
