// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class TargetingInfoMessageTest extends Specification {
  val string = hex"50 05 3D10C200 570EFF3C 2406EC00 2B068C00 2A069400"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case TargetingInfoMessage(target_list) =>
        target_list.size mustEqual 5
        //0
        target_list.head.target_guid mustEqual PlanetSideGUID(4157)
        target_list.head.health mustEqual 0.7607844f
        target_list.head.armor mustEqual 0f
        //1
        target_list(1).target_guid mustEqual PlanetSideGUID(3671)
        target_list(1).health mustEqual 1.0000001f
        target_list(1).armor mustEqual 0.23529413f
        //2
        target_list(2).target_guid mustEqual PlanetSideGUID(1572)
        target_list(2).health mustEqual 0.92549026f
        target_list(2).armor mustEqual 0f
        //3
        target_list(3).target_guid mustEqual PlanetSideGUID(1579)
        target_list(3).health mustEqual 0.54901963f
        target_list(3).armor mustEqual 0f
        //4
        target_list(4).target_guid mustEqual PlanetSideGUID(1578)
        target_list(4).health mustEqual 0.5803922f
        target_list(4).armor mustEqual 0f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TargetingInfoMessage(
      TargetInfo(PlanetSideGUID(4157), 0.7607844f) ::
        TargetInfo(PlanetSideGUID(3671), 1.0000001f, 0.23529413f) ::
        TargetInfo(PlanetSideGUID(1572), 0.92549026f) ::
        TargetInfo(PlanetSideGUID(1579), 0.54901963f) ::
        TargetInfo(PlanetSideGUID(1578), 0.5803922f) ::
        Nil
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
