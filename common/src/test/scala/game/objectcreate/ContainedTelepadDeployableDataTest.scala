// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class ContainedTelepadDeployableDataTest extends Specification {
  val string = hex"178f0000004080f42b00182cb0202000100000"

  "ContainedTelepadDeployableData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 143
          cls mustEqual 744
          guid mustEqual PlanetSideGUID(432)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(385)
          parent.get.slot mustEqual 2
          data.isDefined mustEqual true
          data.get.isInstanceOf[ContainedTelepadDeployableData] mustEqual true
          data.get.asInstanceOf[ContainedTelepadDeployableData].unk mustEqual 101
          data.get.asInstanceOf[ContainedTelepadDeployableData].router_guid mustEqual PlanetSideGUID(385)
        case _ =>
          ko
      }
    }
    "encode" in {
      val obj = ContainedTelepadDeployableData(101, PlanetSideGUID(385))
      val msg = ObjectCreateMessage(744, PlanetSideGUID(432), ObjectCreateMessageParent(PlanetSideGUID(385), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string
    }
  }
}
