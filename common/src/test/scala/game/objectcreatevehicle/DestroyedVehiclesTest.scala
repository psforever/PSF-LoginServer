// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import org.specs2.mutable._
import scodec.bits._

class DestroyedVehiclesTest extends Specification {
  val string_ams_destroyed = hex"17 8D000000 978 3D10 002D765535CA16000000 0"

  "Destroyed vehicles" should {
    "decode (ams, destroyed)" in {
      PacketCoding.DecodePacket(string_ams_destroyed).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 141L
          cls mustEqual ObjectClass.ams_destroyed
          guid mustEqual PlanetSideGUID(4157)
          parent.isDefined mustEqual false
          data.isInstanceOf[DestroyedVehicleData] mustEqual true
          val dams = data.asInstanceOf[DestroyedVehicleData]
          dams.pos.coord.x mustEqual 3674.0f
          dams.pos.coord.y mustEqual 2726.789f
          dams.pos.coord.z mustEqual 91.15625f
          dams.pos.orient.x mustEqual 0f
          dams.pos.orient.y mustEqual 0f
          dams.pos.orient.z mustEqual 90.0f
        case _ =>
          ko
      }
    }

    "encode (ams, destroyed)" in {
      val obj = DestroyedVehicleData(PlacementData(3674.0f, 2726.789f, 91.15625f, 0f, 0f, 90.0f))
      val msg = ObjectCreateMessage(ObjectClass.ams_destroyed, PlanetSideGUID(4157), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ams_destroyed
    }
  }
}
