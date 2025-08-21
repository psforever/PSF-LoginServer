// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitListEvent
import net.psforever.packet.game.OutfitListEvent.RequestType
import net.psforever.packet.game.OutfitListEventAction.ListElementOutfit
import org.specs2.mutable._
import scodec.bits.ByteVector

class OutfitListEventTest extends Specification {
  val unk2_0_ABC: ByteVector = ByteVector.fromValidHex("98 5 e83a0000 000e1800 0800000 11404e0069006700680074004c006f00720064007300 854e005900430061007400")
  val unk2_0_DEF: ByteVector = ByteVector.fromValidHex("98 4 ec281001 51a62800 3400000 11a0490052004f004e004600490053005400200043006c0061006e00 8654006f006c006a00")
  val unk2_1_ABC: ByteVector = ByteVector.fromValidHex("98 4 723c0000 2aa81e00 2200000 11006900470061006d00650073002d004500 906900670061006d006500730043005400460057006800610063006b002d004500")
  val unk2_2_ABC: ByteVector = ByteVector.fromValidHex("98 4 9a3c0001 16da4e00 0400000 11a042006c006f006f00640020006f0066002000560061006e007500 864b00610072006e002d004500")
  val unk2_3_ABC: ByteVector = ByteVector.fromValidHex("98 4 9c3c0000 df587c00 1400000 11a054006800650020004e00650076006500720068006f006f006400 8e6f00460058006f00530074006f006e0065004d0061006e002d004700")
  val unk2_4_ABC: ByteVector = ByteVector.fromValidHex("98 4 c03c0000 24060400 0600000 1220540068006500200042006c00610063006b0020004b006e0069006700680074007300 874400720061007a00760065006e00")
  val unk2_5_ABC: ByteVector = ByteVector.fromValidHex("98 5 383c0001 4b709a00 0c00000 10a03e005400760053003c00 89430061007000650062006f00610074007300")
  val unk2_6_ABC: ByteVector = ByteVector.fromValidHex("98 5 b03c0000 35d67000 0400000 11404c006f0073007400200043006100750073006500 895a00650072006f004b00650077006c006c00")
  val unk2_7_ABC: ByteVector = ByteVector.fromValidHex("98 4 043e0001 9fb82616 1400000 11e0540068006500200042006c00610063006b00200054006f00770065007200 874b00720075007000680065007800")

  "decode unk0_ABC" in {
    PacketCoding.decodePacket(unk2_0_ABC).require match {
      case OutfitListEvent(code, ListElementOutfit(unk1, points, members, outfit_name, outfit_leader)) =>
        code mustEqual OutfitListEvent.RequestType.ListElementOutfit
        unk1 mustEqual 7668
        points mustEqual 788224
        members mustEqual 4
        outfit_name mustEqual "NightLords"
        outfit_leader mustEqual "NYCat"
      case _ =>
        ko
    }
  }

  "encode unk0_ABC" in {
    val msg = OutfitListEvent(RequestType.ListElementOutfit, ListElementOutfit(7668, 788224, 4, "NightLords", "NYCat"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk2_0_ABC
  }

}
