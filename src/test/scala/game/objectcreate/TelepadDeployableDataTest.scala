// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class TelepadDeployableDataTest extends Specification {
  val string = hex"17 c8000000 f42 6101 fbcfc 0fd43 6903 00 00 79 05 8101 ae01 5700c"
  //TODO validate the unknown fields before router_guid for testing

  "TelepadData" should {
    "decode" in {
      PacketCoding.decodePacket(string).require match {
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
                case TelepadDeployableData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v5, fguid), u1, owner, u3, u4) =>
                  faction mustEqual PlanetSideEmpire.TR
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v5.contains(385) mustEqual true
                  fguid mustEqual PlanetSideGUID(430)

                  u1 mustEqual false
                  owner mustEqual PlanetSideGUID(430)
                  u3 mustEqual true
                  u4 mustEqual false
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
          CommonFieldData(PlanetSideEmpire.TR, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = Some(385), guid = PlanetSideGUID(430)),
          unk1 = false,
          owner_guid = PlanetSideGUID(430),
          unk3 = true,
          unk4 = false
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.router_telepad_deployable, PlanetSideGUID(353), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}
