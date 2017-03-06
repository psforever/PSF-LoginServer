// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class WeatherMessageTest extends Specification {
  val string = hex"9501000000004A0807C0D65B8FBF2427663F178608BE0B000000006CE13E0C390E3F64445CB7BF3E0C2FF23DA46264A3193FBA522E3F597D9A96093F95B99E3D0800096FE53E6CD6523F39198EAF683F9BA0363D01009C35503F9E5F3E3F3C304E46F23EF9668E3E6B56C8277F3FB084F33EB6C10291423FB17F663F00008C077F3E3135D03E320A"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case WeatherMessage(unk1, unk2) =>
        unk1.size mustEqual 1
        unk1.head.unk1 mustEqual 0
        unk1.head.unk2 mustEqual 3221686346L
        unk1.head.unk3 mustEqual 3213843414L
        unk1.head.unk4 mustEqual 1063659300L
        unk1.head.unk5 mustEqual 3188229655L

        unk2.size mustEqual 11
        //0
        unk2.head.unk1 mustEqual 1054960640L
        unk2.head.unk2 mustEqual 1057896716L
        unk2.head.unk3 mustEqual 100
        unk2.head.unk4 mustEqual 68
        //1
        unk2(1).unk1 mustEqual 1052751708L
        unk2(1).unk2 mustEqual 1039281932L
        unk2(1).unk3 mustEqual 164
        unk2(1).unk4 mustEqual 98
        //2
        unk2(2).unk1 mustEqual 1058644836L
        unk2(2).unk2 mustEqual 1060000442L
        unk2(2).unk3 mustEqual 89
        unk2(2).unk4 mustEqual 125
        //3
        unk2(3).unk1 mustEqual 1057592986L
        unk2(3).unk2 mustEqual 1033812373L
        unk2(3).unk3 mustEqual 8
        unk2(3).unk4 mustEqual 0
        //4
        unk2(4).unk1 mustEqual 1055223561L
        unk2(4).unk2 mustEqual 1062393452L
        unk2(4).unk3 mustEqual 57
        unk2(4).unk4 mustEqual 25
        //5
        unk2(5).unk1 mustEqual 1063825294L
        unk2(5).unk2 mustEqual 1026990235L
        unk2(5).unk3 mustEqual 1
        unk2(5).unk4 mustEqual 0
        //6
        unk2(6).unk1 mustEqual 1062221212L
        unk2(6).unk2 mustEqual 1061052318L
        unk2(6).unk3 mustEqual 60
        unk2(6).unk4 mustEqual 48
        //7
        unk2(7).unk1 mustEqual 1056065102L
        unk2(7).unk2 mustEqual 1049519865L
        unk2(7).unk3 mustEqual 107
        unk2(7).unk4 mustEqual 86
        //8
        unk2(8).unk1 mustEqual 1065297864L
        unk2(8).unk2 mustEqual 1056146608L
        unk2(8).unk3 mustEqual 182
        unk2(8).unk4 mustEqual 193
        //9
        unk2(9).unk1 mustEqual 1061327106L
        unk2(9).unk2 mustEqual 1063681969L
        unk2(9).unk3 mustEqual 0
        unk2(9).unk4 mustEqual 0
        //10
        unk2(10).unk1 mustEqual 1048512396L
        unk2(10).unk2 mustEqual 1053832497L
        unk2(10).unk3 mustEqual 50
        unk2(10).unk4 mustEqual 10
      case default =>
        ko
    }
  }

  "encode" in {
    val msg = WeatherMessage(
      Weather1(0, 3221686346L, 3213843414L, 1063659300L, 3188229655L) :: //00 4A0807C0 D65B8FBF 2427663F 178608BE
        Nil,
      Weather2(1054960640L, 1057896716L, 100, 68) :: //006CE13E 0C390E3F 64 44
        Weather2(1052751708L, 1039281932L, 164, 98) :: //5CB7BF3E 0C2FF23D A4 62
        Weather2(1058644836L, 1060000442L, 89, 125) :: //64A3193F BA522E3F 59 7D
        Weather2(1057592986L, 1033812373L, 8, 0) :: //9A96093F 95B99E3D 08 00
        Weather2(1055223561L, 1062393452L, 57, 25) :: //096FE53E 6CD6523F 39 19
        Weather2(1063825294L, 1026990235L, 1, 0) :: //8EAF683F 9BA0363D 01 00
        Weather2(1062221212L, 1061052318L, 60, 48) :: //9C35503F 9E5F3E3F 3C 30
        Weather2(1056065102L, 1049519865L, 107, 86) :: //4E46F23E F9668E3E 6B 56
        Weather2(1065297864L, 1056146608L, 182, 193) :: //C8277F3F B084F33E B6 C1
        Weather2(1061327106L, 1063681969L, 0, 0) :: //0291423F B17F663F 00 00
        Weather2(1048512396L, 1053832497L, 50, 10) :: //8C077F3E 3135D03E 32 0A
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
