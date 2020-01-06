// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class WeaponLazeTargetPositionMessageTest extends Specification {
  val string = hex"C8 4C00 6C2D7 65535 CA16 982D7 4A535 CA16"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case WeaponLazeTargetPositionMessage(weapon_uid, pos1, pos2) =>
        weapon_uid mustEqual PlanetSideGUID(76)
        pos1.x mustEqual 3674.8438f
        pos1.y mustEqual 2726.789f
        pos1.z mustEqual 91.15625f
        pos2.x mustEqual 3675.1875f
        pos2.y mustEqual 2726.5781f
        pos2.z mustEqual 91.15625f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WeaponLazeTargetPositionMessage(
      PlanetSideGUID(76),
      Vector3(3674.8438f, 2726.789f, 91.15625f),
      Vector3(3675.1875f, 2726.5781f, 91.15625f)
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
