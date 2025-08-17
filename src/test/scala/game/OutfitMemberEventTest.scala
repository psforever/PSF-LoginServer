// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._

import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class OutfitMemberEventTest extends Specification {

  //val unk0_ABC: ByteVector =  hex"90 3518 4000 1a4e 4100 2 180 450078007000650072007400 8483 07e0 119d bfe0 70" // 0x90048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010
val unk0_ABC_Lazer: ByteVector =      hex"90 048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010"

  val OpolE =                   hex"90 0 4864   0003 aad6 280a 14 0 4f0070006f006c004500                         c9a1 80e0 0d03 2040 10"
  val Billy =                   hex"90 0 4864   0003 a41a 280a 20 0 620069006c006c007900320035003600             935f 6000 186a b040 50"
  val Lazer =                   hex"90 0 4864   0001 030c 2802 24 0 4c0061007a00650072003100390038003200         e6dc 25a0 153e 6040 10"
  val Virus =                   hex"90 0 4864   0002 1b64 4c02 28 0 5600690072007500730047006900760065007200     2f89 0080 0000 0000 10"
  val PvtPa =                   hex"90 0 4864   0000 1e69 e80a 2c 0 500076007400500061006e00630061006b0065007300 705e a080 0a85 e060 10"
  val Night =                   hex"90 0 4864   0002 4cf0 3802 28 0 4e006900670068007400770069006e0067003100     b8fb 9a40 0da6 ec80 50"

  /*
  OutfitMemberEvent(0, ValidPlanetSideGUID(6418), 0, 0, 64, 195, 10, 0, Lazer1982, 230, 220, 37, 160, 21, 62, 96, 64, 16, BitVector(empty))
  OutfitMemberEvent(0, ValidPlanetSideGUID(6418), 0, 0, 7, 154, 122, 2, PvtPancakes, 112, 94, 160, 128, 10, 133, 224, 96, 16, BitVector(empty))
  OutfitMemberEvent(0, ValidPlanetSideGUID(6418), 0, 0, 134, 217, 19, 0, VirusGiver, 47, 137, 0, 128, 0, 0, 0, 0, 16, BitVector(empty))
  OutfitMemberEvent(0, ValidPlanetSideGUID(6418), 0, 0, 234, 181, 138, 2, OpolE, 201, 161, 128, 224, 13, 3, 32, 64, 16, BitVector(empty))
  OutfitMemberEvent(0, ValidPlanetSideGUID(6418), 0, 0, 233, 6, 138, 2, billy256, 147, 95, 96, 0, 24, 106, 176, 64, 80, BitVector(empty))


   */

  "decode Unk0 ABC" in {
    PacketCoding.decodePacket(unk0_ABC_Lazer).require match {
      case OutfitMemberEvent(unk00, outfit_guid, unk3, unk5, member_name, unk8, unk9, unk10, unk11, unk12, unk13,unk14,unk15,unk16) =>
        unk00 mustEqual 0
        outfit_guid mustEqual 6418L
        unk3 mustEqual 49984
        unk5 mustEqual 10
        member_name mustEqual "Lazer1982"
        unk8 mustEqual 244
        unk9 mustEqual 58
        unk10 mustEqual 69
        unk11 mustEqual 224
        unk12 mustEqual 11
        unk13 mustEqual 76
        unk14 mustEqual 96
        unk15 mustEqual 64
        unk16 mustEqual 16
      case _ =>
        ko
    }
  }

  "encode Unk0 ABC" in {
    val msg = OutfitMemberEvent(
      unk00 = 0,
      outfit_id = 6418L,
      unk3 = 49984,
      unk5 = 10,
      member_name = "Lazer1982",
      unk8 = 244,
      unk9 = 58,
      unk10 = 69,
      unk11 = 224,
      unk12 = 11,
      unk13 = 76,
      unk14 = 96,
      unk15 = 64,
      unk16 = 16,
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk0_ABC_Lazer
  }
}
