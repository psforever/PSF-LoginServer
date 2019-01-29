// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class InternalTelepadDeployableDataTest extends Specification {
  val string = hex"178f0000004080f42b00182cb0202000100000"

  "InternalTelepadDeployableData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 143
          cls mustEqual 744
          guid mustEqual PlanetSideGUID(432)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(385)
          parent.get.slot mustEqual 2
          data match {
            case InternalTelepadDeployableData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid)) =>
              faction mustEqual PlanetSideEmpire.NEUTRAL
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual true
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.isEmpty mustEqual true
              v5.contains(385) mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = InternalTelepadDeployableData(
        CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, Some(385), PlanetSideGUID(0))
      )
      val msg = ObjectCreateMessage(744, PlanetSideGUID(432), ObjectCreateMessageParent(PlanetSideGUID(385), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string
    }
  }
}
