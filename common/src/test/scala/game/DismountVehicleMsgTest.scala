// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._
import net.psforever.types.BailType

class DismountVehicleMsgTest extends Specification {
  val string = hex"0F C609 00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) =>
        player_guid mustEqual PlanetSideGUID(2502)
        bailType mustEqual BailType.Normal
        wasKickedByDriver mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DismountVehicleMsg(PlanetSideGUID(2502), BailType.Normal, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
