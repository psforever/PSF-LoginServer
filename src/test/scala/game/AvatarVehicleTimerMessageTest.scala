// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class AvatarVehicleTimerMessageTest extends Specification {
  val string  = hex"57bd16866d65646b69740500000000"
  val string2 = hex"57971b84667572794800000080"

  "decode medkit" in {
    PacketCoding.decodePacket(string).require match {
      case AvatarVehicleTimerMessage(player_guid, text, time, u1) =>
        player_guid mustEqual PlanetSideGUID(5821)
        text mustEqual "medkit"
        time mustEqual 5
        u1 mustEqual false
      case _ =>
        ko
    }
  }
  "decode fury" in {
    PacketCoding.decodePacket(string2).require match {
      case AvatarVehicleTimerMessage(player_guid, text, time, u1) =>
        player_guid mustEqual PlanetSideGUID(7063)
        text mustEqual "fury"
        time mustEqual 72
        u1 mustEqual true
      case _ =>
        ko
    }
  }

  "encode medkit" in {
    val msg = AvatarVehicleTimerMessage(PlanetSideGUID(5821), "medkit", 5, false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
  "encode fury" in {
    val msg = AvatarVehicleTimerMessage(PlanetSideGUID(7063), "fury", 72, true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}
