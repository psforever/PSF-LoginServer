// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class MountVehicleMsgTest extends Specification {
  val string = hex"0E E104 6704 06"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case MountVehicleMsg(player_guid, vehicle_guid, entry) =>
        player_guid mustEqual PlanetSideGUID(1249)
        vehicle_guid mustEqual PlanetSideGUID(1127)
        entry mustEqual 6
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = MountVehicleMsg(PlanetSideGUID(1249), PlanetSideGUID(1127), 6)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
