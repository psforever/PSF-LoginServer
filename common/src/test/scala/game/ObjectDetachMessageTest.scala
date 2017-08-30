// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class ObjectDetachMessageTest extends Specification {
  val string = hex"27 640B C609 92F76 01D65 F611 00 00 40"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ObjectDetachMessage(parent_guid, child_guid, pos, roll, pitch, yaw) =>
        parent_guid mustEqual PlanetSideGUID(2916)
        child_guid mustEqual PlanetSideGUID(2502)
        pos.x mustEqual 3567.1406f
        pos.y mustEqual 2988.0078f
        pos.z mustEqual 71.84375f
        roll mustEqual 0f
        pitch mustEqual 0f
        yaw mustEqual 270f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectDetachMessage(PlanetSideGUID(2916), PlanetSideGUID(2502), Vector3(3567.1406f, 2988.0078f, 71.84375f), 0f, 0f, 270f)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
