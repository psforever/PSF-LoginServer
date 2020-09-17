// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class VehicleSubStateMessageTest extends Specification {
  val string = hex"6D D91C 300D 529F5845 D1953345 E51AB642 21000F63E6F80C1CCF80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case VehicleSubStateMessage(vehicle_guid, player_guid, vehicle_pos, vehicle_ang, vel, unk1, unk2) =>
        vehicle_guid mustEqual PlanetSideGUID(7385)
        player_guid mustEqual PlanetSideGUID(3376)
        vehicle_pos mustEqual Vector3(3465.9575f, 2873.3635f, 91.05253f)
        vehicle_ang mustEqual Vector3(11.6015625f, 0.0f, 3.515625f)
        vel.isDefined mustEqual true
        vel.get mustEqual Vector3(-0.40625f, 0.03125f, -0.8125f)
        unk1 mustEqual false
        unk2.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VehicleSubStateMessage(
      PlanetSideGUID(7385),
      PlanetSideGUID(3376),
      Vector3(3465.9575f, 2873.3635f, 91.05253f),
      Vector3(11.6015625f, 0.0f, 3.515625f),
      Some(Vector3(-0.40625f, 0.03125f, -0.8125f)),
      false,
      None
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
