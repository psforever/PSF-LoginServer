// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class GenericCollisionMsgTest extends Specification {
  //TODO find a better test later
  val string =
    hex"3C 92C00000190000001B2A8010932CEF505C70946F00000000000000000000000017725EBC6D6A058000000000000000000000000000003F8FF45140"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case GenericCollisionMsg(ct, p, php, ppos, pv, t, thp, tpos, tv, unk2, unk3, unk4) =>
        ct mustEqual CollisionIs.OfInfantry
        p mustEqual PlanetSideGUID(75)
        t mustEqual PlanetSideGUID(0)
        php mustEqual 100
        thp mustEqual 0
        pv.x mustEqual 115.79913f
        pv.y mustEqual 85.365166f
        pv.z mustEqual -0.046089742f
        tv.x mustEqual 0.0f
        tv.z mustEqual 0.0f
        tv.x mustEqual 0.0f
        ppos.x mustEqual 3986.7266f
        ppos.y mustEqual 2615.3672f
        ppos.z mustEqual 90.625f
        tpos.x mustEqual 0.0f
        tpos.y mustEqual 0.0f
        tpos.z mustEqual 0.0f
        unk2 mustEqual 0L
        unk3 mustEqual 0L
        unk4 mustEqual 1171341310L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericCollisionMsg(
      CollisionIs.OfInfantry,
      PlanetSideGUID(75),
      100,
      Vector3(3986.7266f, 2615.3672f, 90.625f),
      Vector3(115.79913f, 85.365166f, -0.046089742f),
      PlanetSideGUID(0),
      0,
      Vector3(0.0f, 0.0f, 0.0f),
      Vector3(0.0f, 0.0f, 0.0f),
      0L,
      0L,
      1171341310L
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    //pkt mustEqual string
    PacketCoding.decodePacket(pkt).require match {
      case GenericCollisionMsg(ct, p, php, ppos, pv, t, thp, tpos, tv, unk2, unk3, unk4) =>
        ct mustEqual CollisionIs.OfInfantry
        p mustEqual PlanetSideGUID(75)
        t mustEqual PlanetSideGUID(0)
        php mustEqual 100
        thp mustEqual 0
        pv.x mustEqual 115.79913f
        pv.y mustEqual 85.365166f
        pv.z mustEqual -0.046089742f
        tv.x mustEqual 0.0f
        tv.z mustEqual 0.0f
        tv.x mustEqual 0.0f
        ppos.x mustEqual 3986.7266f
        ppos.y mustEqual 2615.3672f
        ppos.z mustEqual 90.625f
        tpos.x mustEqual 0.0f
        tpos.y mustEqual 0.0f
        tpos.z mustEqual 0.0f
        unk2 mustEqual 0L
        unk3 mustEqual 0L
        unk4 mustEqual 1171341310L
      case _ =>
        ko
    }
  }
}
