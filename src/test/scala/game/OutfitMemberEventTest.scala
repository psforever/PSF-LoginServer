// Copyright (c) 2024 PSForever
package game

import net.psforever.packet._

import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._
import scodec.bits._

class OutfitMemberEventTest extends Specification {

  //val unk0_ABC: ByteVector = hex"90 3518 4000 1a4e 4100 2 180 450078007000650072007400 8483 07e0 119d bfe0 70" // 0x90048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010
val unk0_ABC: ByteVector = hex"90 048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010"
  "decode Unk0 ABC" in {
    PacketCoding.decodePacket(unk0_ABC).require match {
      case OutfitMemberEvent(unk00, outfit_guid, unk1, unk2, unk3, member_name, unk7, unk8, unk9, unk10, unk12, unk13, unk14, unk15) =>
        unk00 mustEqual 0
        outfit_guid mustEqual PlanetSideGUID(6418)
        unk1 mustEqual 0
        unk2 mustEqual 49984
        unk3 mustEqual 10
        member_name mustEqual "Lazer1982"
        unk7 mustEqual 244
        unk8 mustEqual 58
        unk9 mustEqual 57413
        unk10 mustEqual 11
        unk12 mustEqual 76
        unk13 mustEqual 96
        unk14 mustEqual 64
        unk15 mustEqual 16
      case _ =>
        ko
    }
  }

  "encode Unk0 ABC" in {
    val msg = OutfitMemberEvent(
      unk00 = 0,
      outfit_id = PlanetSideGUID(6418),
      unk1 = 0,
      unk2 = 49984,
      unk3 = 10,
      member_name = "Lazer1982",
      unk7 = 244,
      unk8 = 58,
      unk9 = 57413,
      unk10 = 11,
      unk12 = 76,
      unk13 = 96,
      unk14 = 64,
      unk15 = 16,
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk0_ABC
  }
}
