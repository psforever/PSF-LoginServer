// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{ExoSuitType, PlanetSideGUID}
import scodec.bits._

class ArmorChangedMessageTest extends Specification {
  val string = hex"3E 11 01 4C"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ArmorChangedMessage(player_guid, armor, subtype) =>
        player_guid mustEqual PlanetSideGUID(273)
        armor mustEqual ExoSuitType.MAX
        subtype mustEqual 3
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ArmorChangedMessage(PlanetSideGUID(273), ExoSuitType.MAX, 3)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
