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
          data match {
            case DroppedItemData(pos, telepad) =>
              pos.coord mustEqual Vector3(6559.961f, 1960.1172f, 13.640625f)
              pos.orient mustEqual Vector3.z(109.6875f)
              pos.vel.isDefined mustEqual false

              telepad match {
                case TelepadDeployableData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), u1, u2) =>
                  faction mustEqual PlanetSideEmpire.TR
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.contains(385) mustEqual true
                  fguid mustEqual PlanetSideGUID(430)

                  u1 mustEqual 87
                  u2 mustEqual 12
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DroppedItemData(
        PlacementData(
          Vector3(6559.961f, 1960.1172f, 13.640625f),
          Vector3.z(109.6875f)
        ),
        TelepadDeployableData(
          CommonFieldData(
            PlanetSideEmpire.TR,
            bops = false,
            alternate = false,
            true,
            None,
            false,
            None,
            Some(385),
            PlanetSideGUID(430)
          ),
          87, 12
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.router_telepad_deployable, PlanetSideGUID(353), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}
