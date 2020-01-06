// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class AegisShieldGeneratorDataTest extends Specification {
  val string_aegis = hex"17 10010000 F80FC09 9DF96 0C676 801C 00 00 00 443E09FF0000000000000000000000000"

  "AegisShieldGeneratorData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_aegis).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 272
          cls mustEqual ObjectClass.deployable_shield_generator
          guid mustEqual PlanetSideGUID(2556)
          parent.isDefined mustEqual false
          data match {
            case AegisShieldGeneratorData(basic, health) =>
              basic.pos.coord mustEqual Vector3(3571.2266f, 3278.0938f, 114.0f)
              basic.pos.orient mustEqual Vector3.z(90.0f)

              basic.data.faction mustEqual PlanetSideEmpire.VS
              basic.data.bops mustEqual false
              basic.data.alternate mustEqual false
              basic.data.v1 mustEqual true
              basic.data.v2.isDefined mustEqual false
              basic.data.jammered mustEqual false
              basic.data.v5.isDefined mustEqual false
              basic.data.guid mustEqual PlanetSideGUID(2366)

              health mustEqual 255
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = AegisShieldGeneratorData(
        CommonFieldDataWithPlacement(
          PlacementData(Vector3(3571.2266f, 3278.0938f, 114.0f), Vector3(0, 0, 90)),
          PlanetSideEmpire.VS, 2, PlanetSideGUID(2366)
        ),
        255
      )
      val msg = ObjectCreateMessage(ObjectClass.deployable_shield_generator, PlanetSideGUID(2556), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_aegis
    }
  }
}
