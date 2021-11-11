// Copyright (c) 2021 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class RadiationCloudDataTest extends Specification {
  val string = hex"17 a5000000 e6a5905 35e6e 52141 bf02 00 00 00 2400000"

  "CaptureFlagData" in {
    "decode" in {
      PacketCoding.decodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.radiator_cloud
          guid mustEqual PlanetSideGUID(1369)
          parent.isDefined mustEqual false
          data match {
            case RadiationCloudData(pos, faction) =>
              pos.coord mustEqual Vector3(7628.414f, 552.6406f, 10.984375f)
              pos.orient mustEqual Vector3.z(value = 90)
              pos.vel.isEmpty mustEqual true
              faction mustEqual PlanetSideEmpire.VS
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = RadiationCloudData(PlacementData(7628.414f, 552.6406f, 10.984375f, 0f, 0f, 90f), PlanetSideEmpire.VS)
      val msg = ObjectCreateMessage(ObjectClass.radiator_cloud, PlanetSideGUID(1369), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string
    }
  }
}
