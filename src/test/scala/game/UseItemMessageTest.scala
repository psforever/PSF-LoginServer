// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class UseItemMessageTest extends Specification {
  val string = hex"10 4B00 0000 7401 FFFFFFFF 4001000000000000000000000000058C803600800000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        unk1 mustEqual PlanetSideGUID(0)
        object_guid mustEqual PlanetSideGUID(372)
        unk2 mustEqual 0xffffffffL
        unk3 mustEqual false
        unk4 mustEqual Vector3(5.0f, 0.0f, 0.0f)
        unk5 mustEqual Vector3(0.0f, 0.0f, 0.0f)
        unk6 mustEqual 11
        unk7 mustEqual 25
        unk8 mustEqual 0
        itemType mustEqual 364
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = UseItemMessage(
      PlanetSideGUID(75),
      PlanetSideGUID(0),
      PlanetSideGUID(372),
      0xffffffffL,
      false,
      Vector3(5.0f, 0.0f, 0.0f),
      Vector3(0.0f, 0.0f, 0.0f),
      11,
      25,
      0,
      364
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
