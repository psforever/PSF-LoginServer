// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class AmmoBoxDataTest extends Specification {
  val string_shotgunshell_dropped = hex"17 A5000000 F9A7D0D 5E269 BED5A F114 0000596000000"

  "AmmoBoxData" should {
    "decode (shotgun shells, dropped)" in {
      PacketCoding.DecodePacket(string_shotgunshell_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.shotgun_shell
          guid mustEqual PlanetSideGUID(3453)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4684.7344f
          drop.pos.coord.y mustEqual 5547.4844f
          drop.pos.coord.z mustEqual 83.765625f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 199.6875f
          drop.obj.isInstanceOf[AmmoBoxData] mustEqual true
          val box = drop.obj.asInstanceOf[AmmoBoxData]
          box.unk mustEqual 0
        case _ =>
          ko
      }
    }

    "encode (shotgun shells, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4684.7344f, 5547.4844f, 83.765625f, 0f, 0f, 199.6875f),
        AmmoBoxData()
      )
      val msg = ObjectCreateMessage(ObjectClass.shotgun_shell, PlanetSideGUID(3453), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_shotgunshell_dropped
    }
  }
}
