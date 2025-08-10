// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits.ByteVector

class OutfitListEventTest extends Specification {
  val unk0_ABC: ByteVector = ByteVector.fromValidHex("98 5e83a000 0000 e180 0080 0000 11404e0069006700680074004c006f00720064007300 854e005900430061007400")
  val unk0_DEF: ByteVector = ByteVector.fromValidHex("98 4ec28100 151a 6280 0340 0000 11a0490052004f004e004600490053005400200043006c0061006e00 8654006f006c006a00")
  val unk1_ABC: ByteVector = ByteVector.fromValidHex("98 4723c000 02aa 81e0 0220 0000 11006900470061006d00650073002d004500 906900670061006d006500730043005400460057006800610063006b002d004500")
  val unk2_ABC: ByteVector = ByteVector.fromValidHex("98 49a3c000 116d a4e0 0040 0000 11a042006c006f006f00640020006f0066002000560061006e007500 864b00610072006e002d004500")
  val unk3_ABC: ByteVector = ByteVector.fromValidHex("98 49c3c000 0df5 87c0 0140 0000 11a054006800650020004e00650076006500720068006f006f006400 8e6f00460058006f00530074006f006e0065004d0061006e002d004700")
  val unk4_ABC: ByteVector = ByteVector.fromValidHex("98 4c03c000 0240 6040 0060 0000 1220540068006500200042006c00610063006b0020004b006e0069006700680074007300 874400720061007a00760065006e00")
  val unk5_ABC: ByteVector = ByteVector.fromValidHex("98 5383c000 14b7 09a0 00c0 0000 10a03e005400760053003c00 89430061007000650062006f00610074007300")
  val unk6_ABC: ByteVector = ByteVector.fromValidHex("98 5b03c000 035d 6700 0040 0000 11404c006f0073007400200043006100750073006500 895a00650072006f004b00650077006c006c00")
  val unk7_ABC: ByteVector = ByteVector.fromValidHex("98 4043e000 19fb 8261 6140 0000 11e0540068006500200042006c00610063006b00200054006f00770065007200 874b00720075007000680065007800")
  val unk8_ABC: ByteVector = ByteVector.fromValidHex("98 4a03e000 17e2") // broken, limit of SMP

  "decode unk0_ABC" in {
    PacketCoding.decodePacket(unk0_ABC).require match {
      case OutfitListEvent(outfit_score, unk1, unk2, unk3, outfit_name, outfit_leader) =>
        outfit_score mustEqual 1585684480L
        unk1 mustEqual 2162229248L
        unk2 mustEqual 32768
        unk3 mustEqual 0
        outfit_name mustEqual "NightLords"
        outfit_leader mustEqual "NYCat"
      case _ =>
        ko
    }
  }

  "encode unk0_ABC" in {
    val msg = OutfitListEvent(1585684480L, 2162229248L, 32768, 0, "NightLords", "NYCat")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk0_ABC
  }

}
