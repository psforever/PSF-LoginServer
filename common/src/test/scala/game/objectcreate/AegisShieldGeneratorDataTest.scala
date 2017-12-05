// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
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
          data.isDefined mustEqual true
          data.get.isInstanceOf[AegisShieldGeneratorData] mustEqual true
          val aegis = data.get.asInstanceOf[AegisShieldGeneratorData]
          aegis.deploy.pos.coord.x mustEqual 3571.2266f
          aegis.deploy.pos.coord.y mustEqual 3278.0938f
          aegis.deploy.pos.coord.z mustEqual 114.0f
          aegis.deploy.pos.orient.x mustEqual 0f
          aegis.deploy.pos.orient.y mustEqual 0f
          aegis.deploy.pos.orient.z mustEqual 90.0f
          aegis.deploy.faction mustEqual PlanetSideEmpire.VS
          aegis.deploy.unk mustEqual 2
          aegis.health mustEqual 255
          aegis.deploy.player_guid mustEqual PlanetSideGUID(2366)
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = AegisShieldGeneratorData(
        CommonFieldData(
          PlacementData(3571.2266f, 3278.0938f, 114.0f, 0f, 0f, 90.0f),
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
