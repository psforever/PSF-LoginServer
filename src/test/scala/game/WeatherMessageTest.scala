// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{WeatherMessage, CloudInfo, StormInfo}
import net.psforever.types.Vector3
import scodec.bits._

class WeatherMessageTest extends Specification {
  val string =
    hex"9501000000004A0807C0D65B8FBF2427663F178608BE0B000000006CE13E0C390E3F64445CB7BF3E0C2FF23DA46264A3193FBA522E3F597D9A96093F95B99E3D0800096FE53E6CD6523F39198EAF683F9BA0363D01009C35503F9E5F3E3F3C304E46F23EF9668E3E6B56C8277F3FB084F33EB6C10291423FB17F663F00008C077F3E3135D03E320A"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case WeatherMessage(clouds, storms) =>
        clouds.size mustEqual 1
        clouds.head.id mustEqual 0
        clouds.head.unk1.x mustEqual -2.109881f
        clouds.head.unk1.y mustEqual -1.1199901f
        clouds.head.unk2.x mustEqual 0.89903474f
        clouds.head.unk2.y mustEqual -0.13332401f

        storms.size mustEqual 11
        //0
        storms.head.loc.x mustEqual 0.4402771f
        storms.head.loc.y mustEqual 0.55555797f
        storms.head.intensity mustEqual 100
        storms.head.radius mustEqual 68
        //1
        storms(1).loc.x mustEqual 0.3744458f
        storms(1).loc.y mustEqual 0.1182538f
        storms(1).intensity mustEqual 164
        storms(1).radius mustEqual 98
        //2
        storms(2).loc.x mustEqual 0.6001494f
        storms(2).loc.y mustEqual 0.6809498f
        storms(2).intensity mustEqual 89
        storms(2).radius mustEqual 125
        //3
        storms(3).loc.x mustEqual 0.53745425f
        storms(3).loc.y mustEqual 0.07750241f
        storms(3).intensity mustEqual 8
        storms(3).radius mustEqual 0
        //4
        storms(4).loc.x mustEqual 0.44811276f
        storms(4).loc.y mustEqual 0.8235843f
        storms(4).intensity mustEqual 57
        storms(4).radius mustEqual 25
        //5
        storms(5).loc.x mustEqual 0.90892875f
        storms(5).loc.y mustEqual 0.04458676f
        storms(5).intensity mustEqual 1
        storms(5).radius mustEqual 0
        //6
        storms(6).loc.x mustEqual 0.813318f
        storms(6).loc.y mustEqual 0.7436465f
        storms(6).intensity mustEqual 60
        storms(6).radius mustEqual 48
        //7
        storms(7).loc.x mustEqual 0.47319263f
        storms(7).loc.y mustEqual 0.27812937f
        storms(7).intensity mustEqual 107
        storms(7).radius mustEqual 86
        //8
        storms(8).loc.x mustEqual 0.99670076f
        storms(8).loc.y mustEqual 0.4756217f
        storms(8).intensity mustEqual 182
        storms(8).radius mustEqual 193
        //9
        storms(9).loc.x mustEqual 0.76002514f
        storms(9).loc.y mustEqual 0.9003859f
        storms(9).intensity mustEqual 0
        storms(9).radius mustEqual 0
        //10
        storms(10).loc.x mustEqual 0.24905223f
        storms(10).loc.y mustEqual 0.40665582f
        storms(10).intensity mustEqual 50
        storms(10).radius mustEqual 10
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WeatherMessage(
      CloudInfo(0, Vector3(-2.109881f, -1.1199901f, 0.0f), Vector3(0.89903474f, -0.13332401f, 0.0f)) ::
        Nil,
      StormInfo(Vector3(0.4402771f, 0.55555797f, 0.0f), 100, 68) ::
        StormInfo(Vector3(0.3744458f, 0.1182538f, 0.0f), 164, 98) ::
        StormInfo(Vector3(0.6001494f, 0.6809498f, 0.0f), 89, 125) ::
        StormInfo(Vector3(0.53745425f, 0.07750241f, 0.0f), 8, 0) ::
        StormInfo(Vector3(0.44811276f, 0.8235843f, 0.0f), 57, 25) ::
        StormInfo(Vector3(0.90892875f, 0.04458676f, 0.0f), 1, 0) ::
        StormInfo(Vector3(0.813318f, 0.7436465f, 0.0f), 60, 48) ::
        StormInfo(Vector3(0.47319263f, 0.27812937f, 0.0f), 107, 86) ::
        StormInfo(Vector3(0.99670076f, 0.4756217f, 0.0f), 182, 193) ::
        StormInfo(Vector3(0.76002514f, 0.9003859f, 0.0f), 0, 0) ::
        StormInfo(Vector3(0.24905223f, 0.40665582f, 0.0f), 50, 10) ::
        Nil
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
