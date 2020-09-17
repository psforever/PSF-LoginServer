// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class LashMessageTest extends Specification {
  val string = hex"4f644a82e2c297a738a1ed0b01b886c0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case LashMessage(seq_time, player, victim, bullet, pos, unk1) =>
        seq_time mustEqual 356
        player mustEqual PlanetSideGUID(2858)
        victim mustEqual PlanetSideGUID(2699)
        bullet mustEqual PlanetSideGUID(40030)
        pos mustEqual Vector3(5903.7656f, 3456.5156f, 111.53125f)
        unk1 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = LashMessage(
      356,
      PlanetSideGUID(2858),
      PlanetSideGUID(2699),
      PlanetSideGUID(40030),
      Vector3(5903.7656f, 3456.5156f, 111.53125f),
      0
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
