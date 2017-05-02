// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class TargetingImplantRequestTest extends Specification {
  val string_single = hex"b5 061016"
  val string_long = hex"b5 41edeb12d4409f0144053f8010541ba91d03df376831b1e26000611041e1107c0209c0"//0510085013d9ffb6720d5b132900003770?

  "decode (single)" in {
    PacketCoding.DecodePacket(string_single).require match {
      case TargetingImplantRequest(target_list) =>
        target_list.length mustEqual 1
        //0
        target_list.head.target_guid mustEqual PlanetSideGUID(1412)
        target_list.head.unk mustEqual true
      case _ =>
        ko
    }
  }

  "decode (long)" in {
    PacketCoding.DecodePacket(string_long).require match {
      case TargetingImplantRequest(target_list) =>
        target_list.length mustEqual 16
        //0
        target_list.head.target_guid mustEqual PlanetSideGUID(31355)
        target_list.head.unk mustEqual true
        //1
        target_list(1).target_guid mustEqual PlanetSideGUID(27273)
        target_list(1).unk mustEqual false
        //2
        target_list(2).target_guid mustEqual PlanetSideGUID(40768)
        target_list(2).unk mustEqual false
        //3
        target_list(3).target_guid mustEqual PlanetSideGUID(34818)
        target_list(3).unk mustEqual false
        //4
        target_list(4).target_guid mustEqual PlanetSideGUID(65044)
        target_list(4).unk mustEqual false
        //5
        target_list(5).target_guid mustEqual PlanetSideGUID(33280)
        target_list(5).unk mustEqual true
        //6
        target_list(6).target_guid mustEqual PlanetSideGUID(47681)
        target_list(6).unk mustEqual true
        //7
        target_list(7).target_guid mustEqual PlanetSideGUID(40995)
        target_list(7).unk mustEqual false
        //8
        target_list(8).target_guid mustEqual PlanetSideGUID(52727)
        target_list(8).unk mustEqual true
        //9
        target_list(9).target_guid mustEqual PlanetSideGUID(6324)
        target_list(9).unk mustEqual true
        //10
        target_list(10).target_guid mustEqual PlanetSideGUID(58033)
        target_list(10).unk mustEqual false
        //11
        target_list(11).target_guid mustEqual PlanetSideGUID(192)
        target_list(11).unk mustEqual true
        //12
        target_list(12).target_guid mustEqual PlanetSideGUID(16772)
        target_list(12).unk mustEqual false
        //13
        target_list(13).target_guid mustEqual PlanetSideGUID(2063)
        target_list(13).unk mustEqual true
        //14
        target_list(14).target_guid mustEqual PlanetSideGUID(49159)
        target_list(14).unk mustEqual false
        //15
        target_list(15).target_guid mustEqual PlanetSideGUID(14401)
        target_list(15).unk mustEqual false
      case _ =>
        ko
    }
  }

  "encode (single)" in {
    val msg = TargetingImplantRequest(
      TargetRequest(PlanetSideGUID(1412), true) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_single
  }

  "encode (long)" in {
    val msg = TargetingImplantRequest(
      TargetRequest(PlanetSideGUID(31355), true) ::
        TargetRequest(PlanetSideGUID(27273), false) ::
        TargetRequest(PlanetSideGUID(40768), false) ::
        TargetRequest(PlanetSideGUID(34818), false) ::
        TargetRequest(PlanetSideGUID(65044), false) ::
        TargetRequest(PlanetSideGUID(33280), true) ::
        TargetRequest(PlanetSideGUID(47681), true) ::
        TargetRequest(PlanetSideGUID(40995), false) ::
        TargetRequest(PlanetSideGUID(52727), true) ::
        TargetRequest(PlanetSideGUID(6324), true) ::
        TargetRequest(PlanetSideGUID(58033), false) ::
        TargetRequest(PlanetSideGUID(192), true) ::
        TargetRequest(PlanetSideGUID(16772), false) ::
        TargetRequest(PlanetSideGUID(2063), true) ::
        TargetRequest(PlanetSideGUID(49159), false) ::
        TargetRequest(PlanetSideGUID(14401), false) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_long
  }
}
