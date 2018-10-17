// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class TelepadDeployableDataTest extends Specification {
  val string = hex"17 c8000000 f42 6101 fbcfc 0fd43 6903 00 00 79 05 8101 ae01 5700c"
  //TODO validate the unknown fields before router_guid for testing

  "TelepadData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 200
          cls mustEqual ObjectClass.router_telepad_deployable
          guid mustEqual PlanetSideGUID(353)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[TelepadDeployableData] mustEqual true
          val teledata = data.get.asInstanceOf[TelepadDeployableData]
          teledata.pos.coord mustEqual Vector3(6559.961f, 1960.1172f, 13.640625f)
          teledata.pos.orient mustEqual Vector3.z(109.6875f)
          teledata.pos.vel.isDefined mustEqual false
          teledata.faction mustEqual PlanetSideEmpire.TR
          teledata.bops mustEqual false
          teledata.destroyed mustEqual false
          teledata.unk1 mustEqual 2
          teledata.unk2 mustEqual true
          teledata.router_guid mustEqual PlanetSideGUID(385)
          teledata.owner_guid mustEqual PlanetSideGUID(430)
          teledata.unk3 mustEqual 87
          teledata.unk4 mustEqual 12
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = TelepadDeployableData(
        PlacementData(
          Vector3(6559.961f, 1960.1172f, 13.640625f),
          Vector3.z(109.6875f)
        ),
        PlanetSideEmpire.TR,
        false, false, 2, true,
        PlanetSideGUID(385),
        PlanetSideGUID(430),
        87, 12
      )
      val msg = ObjectCreateMessage(ObjectClass.router_telepad_deployable, PlanetSideGUID(353), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}
