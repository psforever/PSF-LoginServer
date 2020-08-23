// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class ObjectDetectedMessageTest extends Specification {
  val string = hex"61 E60F E60F 00 1C9C39F8304030AC18A8183436D42C"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ObjectDetectedMessage(guid1, guid2, unk1, list) =>
        guid1 mustEqual PlanetSideGUID(4070)
        guid2 mustEqual PlanetSideGUID(4070)
        unk1 mustEqual 0
        list.size mustEqual 7
        list.head mustEqual PlanetSideGUID(3623)
        list(1) mustEqual PlanetSideGUID(3198)
        list(2) mustEqual PlanetSideGUID(3088)
        list(3) mustEqual PlanetSideGUID(1579)
        list(4) mustEqual PlanetSideGUID(1578)
        list(5) mustEqual PlanetSideGUID(3341)
        list(6) mustEqual PlanetSideGUID(2997)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectDetectedMessage(
      PlanetSideGUID(4070),
      PlanetSideGUID(4070),
      0,
      PlanetSideGUID(3623) ::
        PlanetSideGUID(3198) ::
        PlanetSideGUID(3088) ::
        PlanetSideGUID(1579) ::
        PlanetSideGUID(1578) ::
        PlanetSideGUID(3341) ::
        PlanetSideGUID(2997) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
