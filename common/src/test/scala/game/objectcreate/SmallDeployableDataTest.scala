// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class SmallDeployableDataTest extends Specification {
  val string_boomer = hex"17 A5000000 CA0000F1630938D5A8F1400003F0031100"

  "SmallDeployableData" should {
    "decode (boomer)" in {
      PacketCoding.DecodePacket(string_boomer).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.boomer
          guid mustEqual PlanetSideGUID(3840)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[SmallDeployableData] mustEqual true
          val boomer = data.get.asInstanceOf[SmallDeployableData]
          boomer.pos.coord.x mustEqual 4704.172f
          boomer.pos.coord.y mustEqual 5546.4375f
          boomer.pos.coord.z mustEqual 82.234375f
          boomer.pos.orient.x mustEqual 0f
          boomer.pos.orient.y mustEqual 0f
          boomer.pos.orient.z mustEqual 272.8125f
          boomer.unk1 mustEqual 0
          boomer.owner_guid mustEqual PlanetSideGUID(8290)
        case _ =>
          ko
      }
    }

    "encode (boomer)" in {
      val obj = SmallDeployableData(
        PlacementData(Vector3(4704.172f, 5546.4375f, 82.234375f), Vector3.z(272.8125f)),
        PlanetSideEmpire.TR, false, false, 0, false, false, PlanetSideGUID(8290)
      )
      val msg = ObjectCreateMessage(ObjectClass.boomer, PlanetSideGUID(3840), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_boomer
    }
  }
}
