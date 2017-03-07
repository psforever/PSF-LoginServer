// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class WeaponJammedMessageTest extends Specification {
  val string = hex"66 4C00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case WeaponJammedMessage(weapon_guid) =>
        weapon_guid mustEqual PlanetSideGUID(76)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WeaponJammedMessage(PlanetSideGUID(76))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
