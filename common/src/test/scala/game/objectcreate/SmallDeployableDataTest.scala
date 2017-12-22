// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
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
          boomer.deploy.pos.coord.x mustEqual 4704.172f
          boomer.deploy.pos.coord.y mustEqual 5546.4375f
          boomer.deploy.pos.coord.z mustEqual 82.234375f
          boomer.deploy.pos.orient.x mustEqual 0f
          boomer.deploy.pos.orient.y mustEqual 0f
          boomer.deploy.pos.orient.z mustEqual 272.8125f
          boomer.deploy.unk mustEqual 0
          boomer.deploy.player_guid mustEqual PlanetSideGUID(4145)
        case _ =>
          ko
      }
    }

    "encode (boomer)" in {
      val obj = SmallDeployableData(
        CommonFieldData(
          PlacementData(4704.172f, 5546.4375f, 82.234375f, 0f, 0f, 272.8125f),
          PlanetSideEmpire.TR, 0, PlanetSideGUID(4145)
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.boomer, PlanetSideGUID(3840), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_boomer
    }
  }
}
