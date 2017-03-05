// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class WeaponDelayFireMessageTest extends Specification {
  val string = hex"88 A3140000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case WeaponDelayFireMessage(seq_time, weapon_guid) =>
        seq_time mustEqual 163
        weapon_guid mustEqual PlanetSideGUID(80)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WeaponDelayFireMessage(163, PlanetSideGUID(80))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
